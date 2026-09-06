package com.fachri.heretoslay.data.repository

import com.fachri.heretoslay.data.model.CardCategory
import com.fachri.heretoslay.data.model.GameAction
import com.fachri.heretoslay.data.model.HeroClass
import com.fachri.heretoslay.data.model.Monster
import com.fachri.heretoslay.data.model.RoomState
import com.fachri.heretoslay.data.model.RoomStatus
import com.fachri.heretoslay.data.remote.FirestoreSchema
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

data class AttackResult(
    val die1: Int,
    val die2: Int,
    val totalRoll: Int,
    val isKill: Boolean,
    val isCounterAttack: Boolean,
    val monsterName: String,
)

class GameRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val cardRepository: CardRepository = CardRepository(),
    private val authRepository: AuthRepository = AuthRepository(),
) {
    private val roomsCollection = firestore.collection(FirestoreSchema.COLLECTION_ROOMS)

    fun observeGame(roomCode: String, localUid: String): Flow<RoomState?> = callbackFlow {
        val docRef = roomsCollection.document(roomCode.trim().uppercase())
        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val state = FirestoreSchema.documentToRoomState(snapshot, localUid)
                trySend(state)
            } else {
                trySend(null)
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun drawCard(roomCode: String, uid: String): Result<String> {
        return try {
            val docRef = roomsCollection.document(roomCode.trim().uppercase())
            var drawnCardId = ""

            firestore.runTransaction { tx ->
                val snapshot = tx.get(docRef)
                if (!snapshot.exists()) throw IllegalStateException("Room tidak ditemukan.")

                @Suppress("UNCHECKED_CAST")
                val playersRaw = (snapshot.get(FirestoreSchema.Fields.PLAYERS) as? List<Map<String, Any?>>)
                    ?: emptyList()
                val currentTurnIndex = (snapshot.getLong(FirestoreSchema.Fields.CURRENT_TURN_INDEX) ?: 0L).toInt()
                val currentPlayerMap = playersRaw.getOrNull(currentTurnIndex)
                    ?: throw IllegalStateException("Giliran tidak valid.")

                if (currentPlayerMap[FirestoreSchema.PlayerFields.UID] != uid) {
                    throw IllegalStateException("Bukan giliranmu saat ini!")
                }

                val currentAp = (currentPlayerMap[FirestoreSchema.PlayerFields.AP_REMAINING] as? Long ?: 0L).toInt()
                if (currentAp < 1) {
                    throw IllegalStateException("Action Point (AP) tidak cukup untuk Draw (butuh 1 AP).")
                }

                @Suppress("UNCHECKED_CAST")
                var remainingDeck = (snapshot.get(FirestoreSchema.Fields.REMAINING_DECK_IDS) as? List<String>)?.toMutableList()
                    ?: mutableListOf()
                @Suppress("UNCHECKED_CAST")
                val discardPile = (snapshot.get(FirestoreSchema.Fields.DISCARD_PILE_IDS) as? List<String>)?.toMutableList()
                    ?: mutableListOf()

                if (remainingDeck.isEmpty()) {
                    if (discardPile.isEmpty()) {
                        throw IllegalStateException("Deck dan Discard pile sudah habis.")
                    }
                    remainingDeck = discardPile.shuffled().toMutableList()
                    discardPile.clear()
                    tx.update(docRef, FirestoreSchema.Fields.DISCARD_PILE_IDS, discardPile)
                }

                drawnCardId = remainingDeck.removeAt(0)

                @Suppress("UNCHECKED_CAST")
                val handsMap = (snapshot.get(FirestoreSchema.Fields.HANDS) as? Map<String, List<String>>)
                    ?.mapValues { it.value.toMutableList() }?.toMutableMap() ?: mutableMapOf()
                val myHand = handsMap.getOrPut(uid) { mutableListOf() }.toMutableList()
                myHand.add(drawnCardId)
                handsMap[uid] = myHand

                val updatedPlayers = playersRaw.mapIndexed { idx, p ->
                    if (idx == currentTurnIndex) {
                        p.toMutableMap().apply {
                            this[FirestoreSchema.PlayerFields.AP_REMAINING] = (currentAp - 1).toLong()
                            this[FirestoreSchema.PlayerFields.HAND_SIZE] = myHand.size.toLong()
                        }
                    } else p
                }

                val lastActionMap = mapOf(
                    "type" to "DRAW_CARD",
                    "actorId" to uid,
                    "cardId" to drawnCardId,
                    "timestamp" to System.currentTimeMillis()
                )

                tx.update(docRef, FirestoreSchema.Fields.REMAINING_DECK_IDS, remainingDeck)
                tx.update(docRef, FirestoreSchema.Fields.HANDS, handsMap)
                tx.update(docRef, FirestoreSchema.Fields.PLAYERS, updatedPlayers)
                tx.update(docRef, FirestoreSchema.Fields.LAST_ACTION, lastActionMap)
                tx.update(docRef, FirestoreSchema.Fields.UPDATED_AT, System.currentTimeMillis())
            }.await()

            Result.success(drawnCardId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun playCard(roomCode: String, uid: String, cardId: String): Result<Unit> {
        return try {
            val docRef = roomsCollection.document(roomCode.trim().uppercase())
            val card = cardRepository.findCard(cardId)
                ?: throw IllegalStateException("Data kartu tidak ditemukan.")

            firestore.runTransaction { tx ->
                val snapshot = tx.get(docRef)
                if (!snapshot.exists()) throw IllegalStateException("Room tidak ditemukan.")

                @Suppress("UNCHECKED_CAST")
                val playersRaw = (snapshot.get(FirestoreSchema.Fields.PLAYERS) as? List<Map<String, Any?>>)
                    ?: emptyList()
                val currentTurnIndex = (snapshot.getLong(FirestoreSchema.Fields.CURRENT_TURN_INDEX) ?: 0L).toInt()
                val currentPlayerMap = playersRaw.getOrNull(currentTurnIndex)
                    ?: throw IllegalStateException("Giliran tidak valid.")

                if (currentPlayerMap[FirestoreSchema.PlayerFields.UID] != uid) {
                    throw IllegalStateException("Bukan giliranmu saat ini!")
                }

                val apCost = card.category.apCost
                val currentAp = (currentPlayerMap[FirestoreSchema.PlayerFields.AP_REMAINING] as? Long ?: 0L).toInt()
                if (currentAp < apCost) {
                    throw IllegalStateException("Action Point (AP) tidak cukup (butuh $apCost AP).")
                }

                @Suppress("UNCHECKED_CAST")
                val handsMap = (snapshot.get(FirestoreSchema.Fields.HANDS) as? Map<String, List<String>>)
                    ?.mapValues { it.value.toMutableList() }?.toMutableMap() ?: mutableMapOf()
                val myHand = handsMap.getOrPut(uid) { mutableListOf() }.toMutableList()
                if (!myHand.contains(cardId)) {
                    throw IllegalStateException("Kartu ini tidak ada di tanganmu.")
                }
                myHand.remove(cardId)
                handsMap[uid] = myHand

                @Suppress("UNCHECKED_CAST")
                val discardPile = (snapshot.get(FirestoreSchema.Fields.DISCARD_PILE_IDS) as? List<String>)?.toMutableList()
                    ?: mutableListOf()

                var isWin = false
                val updatedPlayers = playersRaw.mapIndexed { idx, p ->
                    if (idx == currentTurnIndex) {
                        p.toMutableMap().apply {
                            this[FirestoreSchema.PlayerFields.AP_REMAINING] = (currentAp - apCost).toLong()
                            this[FirestoreSchema.PlayerFields.HAND_SIZE] = myHand.size.toLong()

                            if (card.category == CardCategory.HERO) {
                                @Suppress("UNCHECKED_CAST")
                                val heroes = (this[FirestoreSchema.PlayerFields.PARTY_HERO_IDS] as? List<String>)?.toMutableList()
                                    ?: mutableListOf()
                                heroes.add(cardId)
                                this[FirestoreSchema.PlayerFields.PARTY_HERO_IDS] = heroes

                                @Suppress("UNCHECKED_CAST")
                                val heroClasses = (this[FirestoreSchema.PlayerFields.PARTY_HERO_CLASSES] as? List<String>)?.toMutableList()
                                    ?: mutableListOf()
                                val clsName = card.heroClass?.name
                                if (clsName != null && !heroClasses.contains(clsName)) {
                                    heroClasses.add(clsName)
                                }
                                this[FirestoreSchema.PlayerFields.PARTY_HERO_CLASSES] = heroClasses

                                // Win condition: 6 base classes completed
                                if (heroClasses.size >= 6) {
                                    isWin = true
                                }
                            } else {
                                discardPile.add(cardId)
                            }
                        }
                    } else p
                }

                val lastActionMap = mapOf(
                    "type" to "PLAY_CARD",
                    "actorId" to uid,
                    "cardId" to cardId,
                    "timestamp" to System.currentTimeMillis()
                )

                if (isWin) {
                    tx.update(docRef, FirestoreSchema.Fields.STATUS, RoomStatus.FINISHED.name)
                    tx.update(docRef, FirestoreSchema.Fields.WINNER_UID, uid)
                }

                tx.update(docRef, FirestoreSchema.Fields.HANDS, handsMap)
                tx.update(docRef, FirestoreSchema.Fields.DISCARD_PILE_IDS, discardPile)
                tx.update(docRef, FirestoreSchema.Fields.PLAYERS, updatedPlayers)
                tx.update(docRef, FirestoreSchema.Fields.LAST_ACTION, lastActionMap)
                tx.update(docRef, FirestoreSchema.Fields.UPDATED_AT, System.currentTimeMillis())
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun attackMonster(
        roomCode: String,
        uid: String,
        monsterId: String,
        die1: Int,
        die2: Int,
    ): Result<AttackResult> {
        return try {
            val docRef = roomsCollection.document(roomCode.trim().uppercase())
            val monster = cardRepository.findMonster(monsterId)
                ?: throw IllegalStateException("Monster tidak ditemukan.")

            val totalRoll = die1 + die2
            val isKill = monster.isKill(totalRoll)
            val isCounter = monster.isCounterAttack(totalRoll)
            var isWin = false

            firestore.runTransaction { tx ->
                val snapshot = tx.get(docRef)
                if (!snapshot.exists()) throw IllegalStateException("Room tidak ditemukan.")

                @Suppress("UNCHECKED_CAST")
                val playersRaw = (snapshot.get(FirestoreSchema.Fields.PLAYERS) as? List<Map<String, Any?>>)
                    ?: emptyList()
                val currentTurnIndex = (snapshot.getLong(FirestoreSchema.Fields.CURRENT_TURN_INDEX) ?: 0L).toInt()
                val currentPlayerMap = playersRaw.getOrNull(currentTurnIndex)
                    ?: throw IllegalStateException("Giliran tidak valid.")

                if (currentPlayerMap[FirestoreSchema.PlayerFields.UID] != uid) {
                    throw IllegalStateException("Bukan giliranmu saat ini!")
                }

                val currentAp = (currentPlayerMap[FirestoreSchema.PlayerFields.AP_REMAINING] as? Long ?: 0L).toInt()
                if (currentAp < 2) {
                    throw IllegalStateException("Action Point (AP) tidak cukup untuk Menyerang Monster (butuh 2 AP).")
                }

                @Suppress("UNCHECKED_CAST")
                val activeMonsters = (snapshot.get(FirestoreSchema.Fields.ACTIVE_MONSTER_IDS) as? List<String>)?.toMutableList()
                    ?: mutableListOf()
                @Suppress("UNCHECKED_CAST")
                val defeatedMonsters = (snapshot.get(FirestoreSchema.Fields.DEFEATED_MONSTER_IDS) as? List<String>)?.toMutableList()
                    ?: mutableListOf()
                @Suppress("UNCHECKED_CAST")
                val handsMap = (snapshot.get(FirestoreSchema.Fields.HANDS) as? Map<String, List<String>>)
                    ?.mapValues { it.value.toMutableList() }?.toMutableMap() ?: mutableMapOf()
                val myHand = handsMap.getOrPut(uid) { mutableListOf() }.toMutableList()

                val updatedPlayers = playersRaw.mapIndexed { idx, p ->
                    if (idx == currentTurnIndex) {
                        p.toMutableMap().apply {
                            this[FirestoreSchema.PlayerFields.AP_REMAINING] = (currentAp - 2).toLong()

                            if (isKill) {
                                @Suppress("UNCHECKED_CAST")
                                val kills = (this[FirestoreSchema.PlayerFields.MONSTERS_DEFEATED] as? List<String>)?.toMutableList()
                                    ?: mutableListOf()
                                kills.add(monsterId)
                                this[FirestoreSchema.PlayerFields.MONSTERS_DEFEATED] = kills

                                // Chaos rule trigger eligibility
                                val chaosEnabled = snapshot.getBoolean(FirestoreSchema.Fields.RULE_CHAOS) ?: true
                                if (chaosEnabled) {
                                    this[FirestoreSchema.PlayerFields.CHAOS_AVAILABLE] = true
                                }

                                // Win condition: 3 monsters slain
                                if (kills.size >= 3) {
                                    isWin = true
                                }
                            } else if (isCounter && myHand.isNotEmpty()) {
                                // Counter-attack penalty: discard 1 random card
                                myHand.removeAt(0)
                                handsMap[uid] = myHand
                                this[FirestoreSchema.PlayerFields.HAND_SIZE] = myHand.size.toLong()
                            }
                        }
                    } else p
                }

                if (isKill) {
                    activeMonsters.remove(monsterId)
                    defeatedMonsters.add(monsterId)
                    // Replenish active monsters up to 3 from all monsters
                    val allMonsterIds = cardRepository.allMonsters.map { it.id }
                    val unused = allMonsterIds.filter { !activeMonsters.contains(it) && !defeatedMonsters.contains(it) }
                    if (unused.isNotEmpty()) {
                        activeMonsters.add(unused.first())
                    }
                    tx.update(docRef, FirestoreSchema.Fields.ACTIVE_MONSTER_IDS, activeMonsters)
                    tx.update(docRef, FirestoreSchema.Fields.DEFEATED_MONSTER_IDS, defeatedMonsters)
                }

                if (isWin) {
                    tx.update(docRef, FirestoreSchema.Fields.STATUS, RoomStatus.FINISHED.name)
                    tx.update(docRef, FirestoreSchema.Fields.WINNER_UID, uid)
                }

                val lastActionMap = mapOf(
                    "type" to "ATTACK_MONSTER",
                    "actorId" to uid,
                    "monsterId" to monsterId,
                    "roll" to totalRoll,
                    "die1" to die1,
                    "die2" to die2,
                    "isKill" to isKill,
                    "isCounterAttack" to isCounter,
                    "timestamp" to System.currentTimeMillis()
                )

                tx.update(docRef, FirestoreSchema.Fields.PLAYERS, updatedPlayers)
                tx.update(docRef, FirestoreSchema.Fields.HANDS, handsMap)
                tx.update(docRef, FirestoreSchema.Fields.LAST_ACTION, lastActionMap)
                tx.update(docRef, FirestoreSchema.Fields.UPDATED_AT, System.currentTimeMillis())
            }.await()

            Result.success(
                AttackResult(
                    die1 = die1,
                    die2 = die2,
                    totalRoll = totalRoll,
                    isKill = isKill,
                    isCounterAttack = isCounter,
                    monsterName = monster.name,
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun endTurn(roomCode: String, uid: String): Result<Unit> {
        return try {
            val docRef = roomsCollection.document(roomCode.trim().uppercase())

            firestore.runTransaction { tx ->
                val snapshot = tx.get(docRef)
                if (!snapshot.exists()) throw IllegalStateException("Room tidak ditemukan.")

                @Suppress("UNCHECKED_CAST")
                val playersRaw = (snapshot.get(FirestoreSchema.Fields.PLAYERS) as? List<Map<String, Any?>>)
                    ?: emptyList()
                val currentTurnIndex = (snapshot.getLong(FirestoreSchema.Fields.CURRENT_TURN_INDEX) ?: 0L).toInt()
                val currentPlayerMap = playersRaw.getOrNull(currentTurnIndex)
                    ?: throw IllegalStateException("Giliran tidak valid.")

                if (currentPlayerMap[FirestoreSchema.PlayerFields.UID] != uid) {
                    throw IllegalStateException("Bukan giliranmu saat ini!")
                }

                val nextTurnIndex = (currentTurnIndex + 1) % playersRaw.size
                var globalTurnCount = (snapshot.getLong(FirestoreSchema.Fields.GLOBAL_TURN_COUNT) ?: 0L).toInt()
                if (nextTurnIndex == 0) {
                    globalTurnCount += 1
                }

                // Check Bounty status recalculation if enabled
                val bountyEnabled = snapshot.getBoolean(FirestoreSchema.Fields.RULE_BOUNTY) ?: true
                var highestScore = 0
                var highestPlayerUid: String? = null

                if (bountyEnabled) {
                    playersRaw.forEach { p ->
                        @Suppress("UNCHECKED_CAST")
                        val kills = (p[FirestoreSchema.PlayerFields.MONSTERS_DEFEATED] as? List<String>)?.size ?: 0
                        @Suppress("UNCHECKED_CAST")
                        val classes = (p[FirestoreSchema.PlayerFields.PARTY_HERO_CLASSES] as? List<String>)?.size ?: 0
                        val score = (kills * 3) + classes
                        if (score > highestScore && score >= 2) {
                            highestScore = score
                            highestPlayerUid = p[FirestoreSchema.PlayerFields.UID] as? String
                        }
                    }
                }

                val updatedPlayers = playersRaw.mapIndexed { idx, p ->
                    val pUid = p[FirestoreSchema.PlayerFields.UID] as? String
                    p.toMutableMap().apply {
                        if (idx == nextTurnIndex) {
                            // Reset AP to 3 for the new active player
                            this[FirestoreSchema.PlayerFields.AP_REMAINING] = 3L
                        }
                        if (bountyEnabled) {
                            this[FirestoreSchema.PlayerFields.IS_BOUNTY] = (pUid == highestPlayerUid)
                        }
                    }
                }

                val lastActionMap = mapOf(
                    "type" to "END_TURN",
                    "actorId" to uid,
                    "timestamp" to System.currentTimeMillis()
                )

                tx.update(docRef, FirestoreSchema.Fields.CURRENT_TURN_INDEX, nextTurnIndex.toLong())
                tx.update(docRef, FirestoreSchema.Fields.GLOBAL_TURN_COUNT, globalTurnCount.toLong())
                tx.update(docRef, FirestoreSchema.Fields.TURN_START_TIME_MILLIS, System.currentTimeMillis())
                tx.update(docRef, FirestoreSchema.Fields.PLAYERS, updatedPlayers)
                tx.update(docRef, FirestoreSchema.Fields.LAST_ACTION, lastActionMap)
                tx.update(docRef, FirestoreSchema.Fields.UPDATED_AT, System.currentTimeMillis())
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
