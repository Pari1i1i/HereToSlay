package com.example.heretoslay.data.model

/**
 * Complete game room state — the single source of truth mirrored from Firestore.
 *
 * This is the top-level snapshot that all clients observe and reconstruct their
 * local UI from. The [lastAction] field is the key mechanism for triggering
 * synchronized animations across all devices.
 */
data class RoomState(
    val roomCode: String = "",
    val hostId: String = "",
    val status: RoomStatus = RoomStatus.WAITING,

    val players: List<Player> = emptyList(),

    /** Index into [players] — whose turn it is */
    val currentTurnIndex: Int = 0,

    // ── Deck state ────────────────────────────────────────────────────────
    /** Card IDs remaining in the draw pile */
    val remainingDeckIds: List<String> = emptyList(),
    /** Card IDs in the discard pile */
    val discardPileIds: List<String> = emptyList(),

    // ── Monster state ─────────────────────────────────────────────────────
    /** Monster IDs still active (face-up, available to attack) */
    val activeMonsterIds: List<String> = emptyList(),
    /** Monster IDs that have been defeated (removed from play) */
    val defeatedMonsterIds: List<String> = emptyList(),

    // ── House rules toggles ───────────────────────────────────────────────
    val houseRuleVetoEnabled: Boolean = true,
    val houseRuleChaosEnabled: Boolean = true,
    val houseRuleBountyEnabled: Boolean = true,
    val houseRuleCurseEnabled: Boolean = true,

    // ── Veto tracking ─────────────────────────────────────────────────────
    /** How many vetos have been used in the current game */
    val vetoUsedCount: Int = 0,
    /** The turn number on which the last veto was used (0 = never) */
    val lastVetoTurn: Int = 0,
    /** Global turn counter (increments each time currentTurnIndex cycles) */
    val globalTurnCount: Int = 0,

    // ── Last action — drives synchronized animation on all clients ────────
    val lastAction: GameAction? = null,

    // ── Winner ────────────────────────────────────────────────────────────
    val winnerUid: String? = null,
) {
    /** The player whose turn it currently is */
    val currentPlayer: Player?
        get() = players.getOrNull(currentTurnIndex)

    /** True if a veto can be called this turn (global cooldown: 1 per 5 turns) */
    val isVetoAvailable: Boolean
        get() = houseRuleVetoEnabled && (globalTurnCount - lastVetoTurn) >= 5

    /** Number of players in the room */
    val playerCount: Int get() = players.size
}

enum class RoomStatus {
    WAITING,   // In lobby, waiting for players
    PLAYING,   // Game in progress
    FINISHED,  // Game over
}
