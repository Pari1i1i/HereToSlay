package com.fachri.heretoslay.data.remote

import com.fachri.heretoslay.data.model.GameAction
import com.fachri.heretoslay.data.model.HeroClass
import com.fachri.heretoslay.data.model.Player
import com.fachri.heretoslay.data.model.RoomState
import com.fachri.heretoslay.data.model.RoomStatus
import com.google.firebase.firestore.DocumentSnapshot

/**
 * Constants and serialization/deserialization helpers for Cloud Firestore.
 */
object FirestoreSchema {
    const val COLLECTION_ROOMS = "rooms"

    object Fields {
        const val ROOM_CODE = "roomCode"
        const val HOST_ID = "hostId"
        const val STATUS = "status"
        const val PLAYERS = "players"
        const val HANDS = "hands" // Map<uid, List<cardId>>
        const val CURRENT_TURN_INDEX = "currentTurnIndex"
        const val REMAINING_DECK_IDS = "remainingDeckIds"
        const val DISCARD_PILE_IDS = "discardPileIds"
        const val ACTIVE_MONSTER_IDS = "activeMonsterIds"
        const val DEFEATED_MONSTER_IDS = "defeatedMonsterIds"
        const val TURN_DURATION_SECONDS = "turnDurationSeconds"
        const val TURN_START_TIME_MILLIS = "turnStartTimeMillis"
        const val RULE_VETO = "houseRuleVetoEnabled"
        const val RULE_CHAOS = "houseRuleChaosEnabled"
        const val RULE_BOUNTY = "houseRuleBountyEnabled"
        const val RULE_CURSE = "houseRuleCurseEnabled"
        const val VETO_USED_COUNT = "vetoUsedCount"
        const val LAST_VETO_TURN = "lastVetoTurn"
        const val GLOBAL_TURN_COUNT = "globalTurnCount"
        const val LAST_ACTION = "lastAction"
        const val WINNER_UID = "winnerUid"
        const val UPDATED_AT = "updatedAt"
    }

    object PlayerFields {
        const val UID = "uid"
        const val NAME = "name"
        const val PARTY_LEADER_ID = "partyLeaderId"
        const val PARTY_HERO_IDS = "partyHeroIds"
        const val PARTY_HERO_CLASSES = "partyHeroClasses"
        const val MONSTERS_DEFEATED = "monstersDefeated"
        const val HAND_SIZE = "handSize"
        const val AP_REMAINING = "actionPointsRemaining"
        const val IS_HOST = "isHost"
        const val IS_READY = "isReady"
        const val IS_BOUNTY = "isBounty"
        const val CHALLENGE_WINS_BOUNTY = "challengeWinsWhileBounty"
        const val IS_CURSED = "isCursed"
        const val CHAOS_AVAILABLE = "chaosAvailable"
    }

    @Suppress("UNCHECKED_CAST")
    fun documentToRoomState(doc: DocumentSnapshot, localUid: String): RoomState? {
        if (!doc.exists()) return null
        val data = doc.data ?: return null

        val roomCode = data[Fields.ROOM_CODE] as? String ?: doc.id
        val hostId = data[Fields.HOST_ID] as? String ?: ""
        val statusStr = data[Fields.STATUS] as? String ?: RoomStatus.WAITING.name
        val status = try {
            RoomStatus.valueOf(statusStr)
        } catch (_: Exception) {
            RoomStatus.WAITING
        }

        val rawHands = data[Fields.HANDS] as? Map<String, List<String>> ?: emptyMap()
        val localHand = rawHands[localUid] ?: emptyList()

        val rawPlayers = data[Fields.PLAYERS] as? List<Map<String, Any?>> ?: emptyList()
        val players = rawPlayers.map { pData ->
            val pUid = pData[PlayerFields.UID] as? String ?: ""
            val classNames = pData[PlayerFields.PARTY_HERO_CLASSES] as? List<String> ?: emptyList()
            val classes = classNames.mapNotNull { name ->
                try { HeroClass.valueOf(name) } catch (_: Exception) { null }
            }

            Player(
                uid = pUid,
                name = pData[PlayerFields.NAME] as? String ?: "Player",
                partyLeaderId = pData[PlayerFields.PARTY_LEADER_ID] as? String,
                partyHeroIds = (pData[PlayerFields.PARTY_HERO_IDS] as? List<String>) ?: emptyList(),
                partyHeroClasses = classes,
                monstersDefeated = (pData[PlayerFields.MONSTERS_DEFEATED] as? List<String>) ?: emptyList(),
                handCardIds = if (pUid == localUid) localHand else emptyList(),
                handSize = (pData[PlayerFields.HAND_SIZE] as? Long)?.toInt() ?: 0,
                actionPointsRemaining = (pData[PlayerFields.AP_REMAINING] as? Long)?.toInt() ?: 3,
                isHost = pData[PlayerFields.IS_HOST] as? Boolean ?: false,
                isReady = pData[PlayerFields.IS_READY] as? Boolean ?: false,
                isBounty = pData[PlayerFields.IS_BOUNTY] as? Boolean ?: false,
                challengeWinsWhileBounty = (pData[PlayerFields.CHALLENGE_WINS_BOUNTY] as? Long)?.toInt() ?: 0,
                isCursed = pData[PlayerFields.IS_CURSED] as? Boolean ?: false,
                chaosAvailable = pData[PlayerFields.CHAOS_AVAILABLE] as? Boolean ?: false,
            )
        }

        return RoomState(
            roomCode = roomCode,
            hostId = hostId,
            status = status,
            players = players,
            currentTurnIndex = (data[Fields.CURRENT_TURN_INDEX] as? Long)?.toInt() ?: 0,
            remainingDeckIds = (data[Fields.REMAINING_DECK_IDS] as? List<String>) ?: emptyList(),
            discardPileIds = (data[Fields.DISCARD_PILE_IDS] as? List<String>) ?: emptyList(),
            activeMonsterIds = (data[Fields.ACTIVE_MONSTER_IDS] as? List<String>) ?: emptyList(),
            defeatedMonsterIds = (data[Fields.DEFEATED_MONSTER_IDS] as? List<String>) ?: emptyList(),
            turnDurationSeconds = (data[Fields.TURN_DURATION_SECONDS] as? Long)?.toInt() ?: 60,
            turnStartTimeMillis = (data[Fields.TURN_START_TIME_MILLIS] as? Long) ?: 0L,
            houseRuleVetoEnabled = data[Fields.RULE_VETO] as? Boolean ?: true,
            houseRuleChaosEnabled = data[Fields.RULE_CHAOS] as? Boolean ?: true,
            houseRuleBountyEnabled = data[Fields.RULE_BOUNTY] as? Boolean ?: true,
            houseRuleCurseEnabled = data[Fields.RULE_CURSE] as? Boolean ?: true,
            vetoUsedCount = (data[Fields.VETO_USED_COUNT] as? Long)?.toInt() ?: 0,
            lastVetoTurn = (data[Fields.LAST_VETO_TURN] as? Long)?.toInt() ?: 0,
            globalTurnCount = (data[Fields.GLOBAL_TURN_COUNT] as? Long)?.toInt() ?: 0,
            lastAction = parseLastAction(data[Fields.LAST_ACTION] as? Map<String, Any?>),
            winnerUid = data[Fields.WINNER_UID] as? String,
        )
    }

    fun playerToMap(player: Player): Map<String, Any?> {
        return mapOf(
            PlayerFields.UID to player.uid,
            PlayerFields.NAME to player.name,
            PlayerFields.PARTY_LEADER_ID to player.partyLeaderId,
            PlayerFields.PARTY_HERO_IDS to player.partyHeroIds,
            PlayerFields.PARTY_HERO_CLASSES to player.partyHeroClasses.map { it.name },
            PlayerFields.MONSTERS_DEFEATED to player.monstersDefeated,
            PlayerFields.HAND_SIZE to player.handSize,
            PlayerFields.AP_REMAINING to player.actionPointsRemaining,
            PlayerFields.IS_HOST to player.isHost,
            PlayerFields.IS_READY to player.isReady,
            PlayerFields.IS_BOUNTY to player.isBounty,
            PlayerFields.CHALLENGE_WINS_BOUNTY to player.challengeWinsWhileBounty,
            PlayerFields.IS_CURSED to player.isCursed,
            PlayerFields.CHAOS_AVAILABLE to player.chaosAvailable,
        )
    }

    private fun parseLastAction(map: Map<String, Any?>?): GameAction? {
        if (map == null) return null
        val type = map["type"] as? String ?: return null
        val actorId = map["actorId"] as? String ?: ""
        val timestamp = (map["timestamp"] as? Long) ?: System.currentTimeMillis()

        return when (type) {
            "DRAW_CARD" -> GameAction.DrawCard(actorId, timestamp, map["cardId"] as? String ?: "")
            "PLAY_CARD" -> GameAction.PlayCard(
                actorId, timestamp,
                map["cardId"] as? String ?: "",
                map["targetPlayerId"] as? String,
                map["targetCardId"] as? String
            )
            "END_TURN" -> GameAction.EndTurn(actorId, timestamp)
            "ATTACK_MONSTER" -> GameAction.AttackMonster(
                actorId, timestamp,
                map["monsterId"] as? String ?: "",
                (map["roll"] as? Long)?.toInt() ?: 0,
                (map["die1"] as? Long)?.toInt() ?: 0,
                (map["die2"] as? Long)?.toInt() ?: 0,
                map["isKill"] as? Boolean ?: false,
                map["isCounterAttack"] as? Boolean ?: false
            )
            else -> GameAction.Idle(actorId, timestamp)
        }
    }
}
