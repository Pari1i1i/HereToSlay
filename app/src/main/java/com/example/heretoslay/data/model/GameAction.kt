package com.example.heretoslay.data.model

/**
 * All game actions that can be performed and broadcast to Firestore.
 *
 * [GameAction] is the single mechanism for:
 * 1. Committing state changes server-side
 * 2. Triggering synchronized animations on all client devices
 *
 * The [actorId] is the UID of the player who initiated the action.
 * The [timestamp] (epoch millis) is used for ordering and animation sequencing.
 *
 * Sealed class hierarchy keeps exhaustive `when` expressions compiler-enforced.
 */
sealed class GameAction {
    abstract val actorId: String
    abstract val timestamp: Long

    // ── Core gameplay actions ────────────────────────────────────────────

    data class DrawCard(
        override val actorId: String,
        override val timestamp: Long = System.currentTimeMillis(),
        val cardId: String,
    ) : GameAction()

    data class PlayCard(
        override val actorId: String,
        override val timestamp: Long = System.currentTimeMillis(),
        val cardId: String,
        val targetPlayerId: String? = null,   // For cards targeting another player
        val targetCardId: String? = null,      // For cards modifying another card
    ) : GameAction()

    data class ActivateHeroAbility(
        override val actorId: String,
        override val timestamp: Long = System.currentTimeMillis(),
        val heroCardId: String,
        val roll: Int,
        val success: Boolean,
    ) : GameAction()

    data class AttackMonster(
        override val actorId: String,
        override val timestamp: Long = System.currentTimeMillis(),
        val monsterId: String,
        val roll: Int,                         // Sum of 2d6
        val die1: Int,                         // Individual die for animation
        val die2: Int,
        val isKill: Boolean,
        val isCounterAttack: Boolean,
    ) : GameAction()

    data class EndTurn(
        override val actorId: String,
        override val timestamp: Long = System.currentTimeMillis(),
    ) : GameAction()

    // ── Challenge actions ────────────────────────────────────────────────

    data class InitiateChallenge(
        override val actorId: String,          // The challenger
        override val timestamp: Long = System.currentTimeMillis(),
        val targetActionId: String,            // The action being challenged
        val challengeCardId: String,
    ) : GameAction()

    data class ResolveChallengeRoll(
        override val actorId: String,
        override val timestamp: Long = System.currentTimeMillis(),
        val roll: Int,
        val die1: Int,
        val die2: Int,
    ) : GameAction()

    data class ChallengeResolved(
        override val actorId: String,
        override val timestamp: Long = System.currentTimeMillis(),
        val challengerId: String,
        val defenderId: String,
        val challengerRoll: Int,
        val defenderRoll: Int,
        val challengerWon: Boolean,
        val actionCancelled: Boolean,
    ) : GameAction()

    // ── House rule actions ───────────────────────────────────────────────

    data class VetoInitiated(
        override val actorId: String,
        override val timestamp: Long = System.currentTimeMillis(),
        val targetActionType: String,          // Description of action being vetoed
    ) : GameAction()

    data class VetoVoted(
        override val actorId: String,
        override val timestamp: Long = System.currentTimeMillis(),
        val inFavor: Boolean,
    ) : GameAction()

    data class VetoResolved(
        override val actorId: String,          // Empty string = system-resolved
        override val timestamp: Long = System.currentTimeMillis(),
        val passed: Boolean,
        val votesFor: Int,
        val votesAgainst: Int,
    ) : GameAction()

    data class ChaosActivated(
        override val actorId: String,
        override val timestamp: Long = System.currentTimeMillis(),
        val effectIndex: Int,                  // Which of the 5 chaos effects was chosen
    ) : GameAction()

    data class BountyTransferred(
        override val actorId: String,          // System actor id
        override val timestamp: Long = System.currentTimeMillis(),
        val newBountyPlayerId: String,
        val previousBountyPlayerId: String?,
    ) : GameAction()

    data class CurseTriggered(
        override val actorId: String,          // The cursed player
        override val timestamp: Long = System.currentTimeMillis(),
    ) : GameAction()

    data class RaidSteal(
        override val actorId: String,          // The player doing the stealing
        override val timestamp: Long = System.currentTimeMillis(),
        val stolenCardId: String,
        val fromPlayerId: String,
    ) : GameAction()

    // ── Utility ──────────────────────────────────────────────────────────

    /** Sentinel — no action has been taken yet or state is at rest */
    data class Idle(
        override val actorId: String = "",
        override val timestamp: Long = 0L,
    ) : GameAction()
}

/** Compact string type tag used when serialising to Firestore */
val GameAction.typeTag: String get() = when (this) {
    is GameAction.DrawCard            -> "DRAW_CARD"
    is GameAction.PlayCard            -> "PLAY_CARD"
    is GameAction.ActivateHeroAbility -> "ACTIVATE_HERO"
    is GameAction.AttackMonster       -> "ATTACK_MONSTER"
    is GameAction.EndTurn             -> "END_TURN"
    is GameAction.InitiateChallenge   -> "CHALLENGE_INIT"
    is GameAction.ResolveChallengeRoll -> "CHALLENGE_ROLL"
    is GameAction.ChallengeResolved   -> "CHALLENGE_RESOLVED"
    is GameAction.VetoInitiated       -> "VETO_INIT"
    is GameAction.VetoVoted           -> "VETO_VOTE"
    is GameAction.VetoResolved        -> "VETO_RESOLVED"
    is GameAction.ChaosActivated      -> "CHAOS_ACTIVATED"
    is GameAction.BountyTransferred   -> "BOUNTY_TRANSFERRED"
    is GameAction.CurseTriggered      -> "CURSE_TRIGGERED"
    is GameAction.RaidSteal           -> "RAID_STEAL"
    is GameAction.Idle                -> "IDLE"
}
