package com.fachri.heretoslay.data.model

/**
 * Player state — stored both locally and mirrored in Firestore.
 *
 * The [handCardIds] list is **private to that player**; other players only see [handSize].
 * The [partyLeaderId] and [partyHeroIds] are public (visible to all).
 */
data class Player(
    val uid: String,
    val name: String,

    /** ID of the Party Leader card in play */
    val partyLeaderId: String? = null,

    /** Hero card IDs currently in the player's party (max 6 for base game win condition) */
    val partyHeroIds: List<String> = emptyList(),

    /** Hero classes currently in the party (derived, but stored for quick win-check) */
    val partyHeroClasses: List<HeroClass> = emptyList(),

    /** Monster IDs this player has defeated */
    val monstersDefeated: List<String> = emptyList(),

    /** Card IDs in hand — only populated on the local device; others see [handSize] */
    val handCardIds: List<String> = emptyList(),

    /** Number of cards in hand (visible to all players) */
    val handSize: Int = 0,

    /** Current action points remaining this turn (max 3) */
    val actionPointsRemaining: Int = 3,

    /** Whether this player is the room host */
    val isHost: Boolean = false,

    /** Whether this player has marked themselves as ready in the lobby */
    val isReady: Boolean = false,

    // ── Bounty / House Rule tracking ─────────────────────────────────────

    /** True if this player currently holds the Bounty status */
    val isBounty: Boolean = false,

    /**
     * Number of Challenges won consecutively while holding Bounty status.
     * Resets when Bounty status is lost. Triggers Curse at 3.
     */
    val challengeWinsWhileBounty: Int = 0,

    /** True if this player is currently Cursed */
    val isCursed: Boolean = false,

    /**
     * Chaos ability available.
     * Set to true each time the player kills a monster; consumed when Chaos is activated.
     */
    val chaosAvailable: Boolean = false,
) {
    /** Derived "dominance score" used by BountyManager */
    val dominanceScore: Int
        get() = (monstersDefeated.size * 3) + partyHeroClasses.size

    /** True if player has 3+ different hero classes in party (part of party-full win condition) */
    val hasFullParty: Boolean
        get() = partyHeroClasses.toSet().size >= HeroClass.BASE_CLASSES.size

    /** True if player has killed 3+ monsters */
    val hasThreeKills: Boolean
        get() = monstersDefeated.size >= 3
}
