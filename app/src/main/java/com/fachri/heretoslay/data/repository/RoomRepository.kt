package com.fachri.heretoslay.data.repository

import com.fachri.heretoslay.data.model.HeroClass
import com.fachri.heretoslay.data.model.Player
import com.fachri.heretoslay.data.model.RoomState
import com.fachri.heretoslay.data.model.RoomStatus
import com.fachri.heretoslay.data.remote.FirestoreSchema
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class RoomRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val authRepository: AuthRepository = AuthRepository(),
    private val cardRepository: CardRepository = CardRepository(),
) {
    private val roomsCollection = firestore.collection(FirestoreSchema.COLLECTION_ROOMS)

    suspend fun createRoom(playerName: String): Result<String> {
        return try {
            val uid = authRepository.getOrSignIn()
            authRepository.setPlayerName(playerName)

            val roomCode = generateUniqueRoomCode()
            val hostPlayer = Player(
                uid = uid,
                name = playerName.trim().ifBlank { "Host" },
                isHost = true,
                isReady = true, // Host is ready by default
            )

            val shuffledDeck = cardRepository.buildShuffledDeckIds()
            val shuffledMonsters = cardRepository.buildShuffledMonsterIds()

            val roomData = hashMapOf(
                FirestoreSchema.Fields.ROOM_CODE to roomCode,
                FirestoreSchema.Fields.HOST_ID to uid,
                FirestoreSchema.Fields.STATUS to RoomStatus.WAITING.name,
                FirestoreSchema.Fields.PLAYERS to listOf(FirestoreSchema.playerToMap(hostPlayer)),
                FirestoreSchema.Fields.HANDS to emptyMap<String, List<String>>(),
                FirestoreSchema.Fields.CURRENT_TURN_INDEX to 0,
                FirestoreSchema.Fields.REMAINING_DECK_IDS to shuffledDeck,
                FirestoreSchema.Fields.DISCARD_PILE_IDS to emptyList<String>(),
                FirestoreSchema.Fields.ACTIVE_MONSTER_IDS to shuffledMonsters.take(3),
                FirestoreSchema.Fields.DEFEATED_MONSTER_IDS to emptyList<String>(),
                FirestoreSchema.Fields.TURN_DURATION_SECONDS to 60,
                FirestoreSchema.Fields.TURN_START_TIME_MILLIS to 0L,
                FirestoreSchema.Fields.RULE_VETO to true,
                FirestoreSchema.Fields.RULE_CHAOS to true,
                FirestoreSchema.Fields.RULE_BOUNTY to true,
                FirestoreSchema.Fields.RULE_CURSE to true,
                FirestoreSchema.Fields.VETO_USED_COUNT to 0,
                FirestoreSchema.Fields.LAST_VETO_TURN to 0,
                FirestoreSchema.Fields.GLOBAL_TURN_COUNT to 0,
                FirestoreSchema.Fields.LAST_ACTION to null,
                FirestoreSchema.Fields.WINNER_UID to null,
                FirestoreSchema.Fields.UPDATED_AT to System.currentTimeMillis(),
            )

            roomsCollection.document(roomCode).set(roomData).await()
            Result.success(roomCode)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun joinRoom(roomCode: String, playerName: String): Result<Unit> {
        return try {
            val uid = authRepository.getOrSignIn()
            authRepository.setPlayerName(playerName)
            val cleanCode = roomCode.trim().uppercase()

            val docRef = roomsCollection.document(cleanCode)

            firestore.runTransaction { tx ->
                val snapshot = tx.get(docRef)
                if (!snapshot.exists()) {
                    throw IllegalStateException("Room dengan kode '$cleanCode' tidak ditemukan.")
                }

                val status = snapshot.getString(FirestoreSchema.Fields.STATUS)
                if (status != RoomStatus.WAITING.name) {
                    throw IllegalStateException("Permainan di room ini sudah dimulai atau telah berakhir.")
                }

                @Suppress("UNCHECKED_CAST")
                val playersRaw = (snapshot.get(FirestoreSchema.Fields.PLAYERS) as? List<Map<String, Any?>>)
                    ?: emptyList()

                // Check if already in room
                val existingIndex = playersRaw.indexOfFirst {
                    it[FirestoreSchema.PlayerFields.UID] == uid
                }

                if (existingIndex >= 0) {
                    // Update player name if changed
                    val updatedList = playersRaw.toMutableList()
                    val existing = updatedList[existingIndex].toMutableMap()
                    existing[FirestoreSchema.PlayerFields.NAME] = playerName.trim().ifBlank { "Player" }
                    updatedList[existingIndex] = existing
                    tx.update(docRef, FirestoreSchema.Fields.PLAYERS, updatedList)
                    tx.update(docRef, FirestoreSchema.Fields.UPDATED_AT, System.currentTimeMillis())
                    return@runTransaction
                }

                if (playersRaw.size >= 6) {
                    throw IllegalStateException("Room sudah penuh (maksimal 6 pemain).")
                }

                val newPlayer = Player(
                    uid = uid,
                    name = playerName.trim().ifBlank { "Player ${playersRaw.size + 1}" },
                    isHost = false,
                    isReady = false,
                )

                val updatedPlayers = playersRaw + FirestoreSchema.playerToMap(newPlayer)
                tx.update(docRef, FirestoreSchema.Fields.PLAYERS, updatedPlayers)
                tx.update(docRef, FirestoreSchema.Fields.UPDATED_AT, System.currentTimeMillis())
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeRoom(roomCode: String, localUid: String): Flow<RoomState?> = callbackFlow {
        val cleanCode = roomCode.trim().uppercase()
        val docRef = roomsCollection.document(cleanCode)

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

    suspend fun setPlayerReady(roomCode: String, uid: String, isReady: Boolean): Result<Unit> {
        return try {
            val docRef = roomsCollection.document(roomCode.trim().uppercase())
            firestore.runTransaction { tx ->
                val snapshot = tx.get(docRef)
                if (!snapshot.exists()) return@runTransaction

                @Suppress("UNCHECKED_CAST")
                val playersRaw = (snapshot.get(FirestoreSchema.Fields.PLAYERS) as? List<Map<String, Any?>>)
                    ?: return@runTransaction

                val updated = playersRaw.map { p ->
                    if (p[FirestoreSchema.PlayerFields.UID] == uid) {
                        p.toMutableMap().apply {
                            this[FirestoreSchema.PlayerFields.IS_READY] = isReady
                        }
                    } else p
                }

                tx.update(docRef, FirestoreSchema.Fields.PLAYERS, updated)
                tx.update(docRef, FirestoreSchema.Fields.UPDATED_AT, System.currentTimeMillis())
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateHouseRule(roomCode: String, ruleField: String, enabled: Boolean): Result<Unit> {
        return try {
            val docRef = roomsCollection.document(roomCode.trim().uppercase())
            docRef.update(
                ruleField, enabled,
                FirestoreSchema.Fields.UPDATED_AT, System.currentTimeMillis()
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTurnDuration(roomCode: String, seconds: Int): Result<Unit> {
        return try {
            val docRef = roomsCollection.document(roomCode.trim().uppercase())
            docRef.update(
                FirestoreSchema.Fields.TURN_DURATION_SECONDS, seconds,
                FirestoreSchema.Fields.UPDATED_AT, System.currentTimeMillis()
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addBotPlayer(roomCode: String): Result<Unit> {
        return try {
            val cleanCode = roomCode.trim().uppercase()
            val docRef = roomsCollection.document(cleanCode)

            firestore.runTransaction { tx ->
                val snapshot = tx.get(docRef)
                if (!snapshot.exists()) throw IllegalStateException("Room tidak ditemukan.")

                val status = snapshot.getString(FirestoreSchema.Fields.STATUS)
                if (status != RoomStatus.WAITING.name) {
                    throw IllegalStateException("Permainan sudah dimulai.")
                }

                @Suppress("UNCHECKED_CAST")
                val playersRaw = (snapshot.get(FirestoreSchema.Fields.PLAYERS) as? List<Map<String, Any?>>)
                    ?: emptyList()

                if (playersRaw.size >= 6) {
                    throw IllegalStateException("Room sudah penuh (maksimal 6 pemain).")
                }

                val botIndex = playersRaw.count { (it[FirestoreSchema.PlayerFields.UID] as? String)?.startsWith("bot_") == true } + 1
                val botNames = listOf("Sir Knight (Bot)", "Shadow Mage (Bot)", "Swift Archer (Bot)", "Wild Druid (Bot)", "Holy Priest (Bot)")
                val botName = botNames.getOrElse(botIndex - 1) { "Dummy Bot $botIndex" }

                val botPlayer = Player(
                    uid = "bot_${System.currentTimeMillis()}_$botIndex",
                    name = botName,
                    isHost = false,
                    isReady = true, // Bots are ready immediately
                )

                val updatedPlayers = playersRaw + FirestoreSchema.playerToMap(botPlayer)
                tx.update(docRef, FirestoreSchema.Fields.PLAYERS, updatedPlayers)
                tx.update(docRef, FirestoreSchema.Fields.UPDATED_AT, System.currentTimeMillis())
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeBotPlayer(roomCode: String, botUid: String): Result<Unit> {
        return try {
            val cleanCode = roomCode.trim().uppercase()
            val docRef = roomsCollection.document(cleanCode)

            firestore.runTransaction { tx ->
                val snapshot = tx.get(docRef)
                if (!snapshot.exists()) throw IllegalStateException("Room tidak ditemukan.")

                @Suppress("UNCHECKED_CAST")
                val playersRaw = (snapshot.get(FirestoreSchema.Fields.PLAYERS) as? List<Map<String, Any?>>)
                    ?: emptyList()

                val updatedPlayers = playersRaw.filter { it[FirestoreSchema.PlayerFields.UID] != botUid }
                tx.update(docRef, FirestoreSchema.Fields.PLAYERS, updatedPlayers)
                tx.update(docRef, FirestoreSchema.Fields.UPDATED_AT, System.currentTimeMillis())
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun startGame(roomCode: String): Result<Unit> {
        return try {
            val docRef = roomsCollection.document(roomCode.trim().uppercase())

            firestore.runTransaction { tx ->
                val snapshot = tx.get(docRef)
                if (!snapshot.exists()) throw IllegalStateException("Room tidak ditemukan.")

                @Suppress("UNCHECKED_CAST")
                val playersRaw = (snapshot.get(FirestoreSchema.Fields.PLAYERS) as? List<Map<String, Any?>>)
                    ?: emptyList()

                if (playersRaw.size < 2) {
                    throw IllegalStateException("Minimal 2 pemain untuk memulai permainan.")
                }

                val allReady = playersRaw.all { (it[FirestoreSchema.PlayerFields.IS_READY] as? Boolean) == true }
                if (!allReady) {
                    throw IllegalStateException("Semua pemain harus siap terlebih dahulu.")
                }

                @Suppress("UNCHECKED_CAST")
                var deck = (snapshot.get(FirestoreSchema.Fields.REMAINING_DECK_IDS) as? List<String>)?.toMutableList()
                    ?: cardRepository.buildShuffledDeckIds().toMutableList()

                // Deal 1 Party Leader per player randomly
                val availableLeaders = cardRepository.partyLeaders.shuffled().toMutableList()
                val handsMap = mutableMapOf<String, List<String>>()

                val updatedPlayers = playersRaw.map { pMap ->
                    val pUid = pMap[FirestoreSchema.PlayerFields.UID] as String
                    val leader = availableLeaders.removeFirstOrNull()
                    val leaderClass = leader?.heroClass?.name

                    // Deal 7 initial cards to hand
                    val initialHand = mutableListOf<String>()
                    repeat(7) {
                        if (deck.isNotEmpty()) {
                            initialHand.add(deck.removeAt(0))
                        }
                    }
                    handsMap[pUid] = initialHand

                    pMap.toMutableMap().apply {
                        this[FirestoreSchema.PlayerFields.PARTY_LEADER_ID] = leader?.id
                        this[FirestoreSchema.PlayerFields.PARTY_HERO_CLASSES] = if (leaderClass != null) listOf(leaderClass) else emptyList<String>()
                        this[FirestoreSchema.PlayerFields.HAND_SIZE] = initialHand.size.toLong()
                        this[FirestoreSchema.PlayerFields.AP_REMAINING] = 3L
                    }
                }

                tx.update(docRef, FirestoreSchema.Fields.STATUS, RoomStatus.PLAYING.name)
                tx.update(docRef, FirestoreSchema.Fields.PLAYERS, updatedPlayers)
                tx.update(docRef, FirestoreSchema.Fields.HANDS, handsMap)
                tx.update(docRef, FirestoreSchema.Fields.REMAINING_DECK_IDS, deck)
                tx.update(docRef, FirestoreSchema.Fields.CURRENT_TURN_INDEX, 0L)
                tx.update(docRef, FirestoreSchema.Fields.TURN_START_TIME_MILLIS, System.currentTimeMillis())
                tx.update(docRef, FirestoreSchema.Fields.UPDATED_AT, System.currentTimeMillis())
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun leaveRoom(roomCode: String, uid: String): Result<Unit> {
        return try {
            val docRef = roomsCollection.document(roomCode.trim().uppercase())
            firestore.runTransaction { tx ->
                val snapshot = tx.get(docRef)
                if (!snapshot.exists()) return@runTransaction

                @Suppress("UNCHECKED_CAST")
                val playersRaw = (snapshot.get(FirestoreSchema.Fields.PLAYERS) as? List<Map<String, Any?>>)
                    ?: emptyList()

                val remainingPlayers = playersRaw.filter { it[FirestoreSchema.PlayerFields.UID] != uid }

                if (remainingPlayers.isEmpty()) {
                    tx.delete(docRef)
                } else {
                    var newHostId = snapshot.getString(FirestoreSchema.Fields.HOST_ID)
                    val updatedList = remainingPlayers.mapIndexed { idx, p ->
                        if (p[FirestoreSchema.PlayerFields.UID] == newHostId) {
                            p
                        } else if (idx == 0 && (p[FirestoreSchema.PlayerFields.IS_HOST] as? Boolean) != true) {
                            // Transfer host to first remaining player if previous host left
                            newHostId = p[FirestoreSchema.PlayerFields.UID] as String
                            p.toMutableMap().apply {
                                this[FirestoreSchema.PlayerFields.IS_HOST] = true
                                this[FirestoreSchema.PlayerFields.IS_READY] = true
                            }
                        } else p
                    }

                    tx.update(docRef, FirestoreSchema.Fields.HOST_ID, newHostId)
                    tx.update(docRef, FirestoreSchema.Fields.PLAYERS, updatedList)
                    tx.update(docRef, FirestoreSchema.Fields.UPDATED_AT, System.currentTimeMillis())
                }
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun generateUniqueRoomCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..6).map { chars.random() }.joinToString("")
    }
}
