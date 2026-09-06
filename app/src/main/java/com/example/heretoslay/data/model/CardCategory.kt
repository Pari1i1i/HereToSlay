package com.example.heretoslay.data.model

/**
 * Category of a card in the HTS deck.
 * Designed to be extensible — expansion categories (e.g. CURSED_ITEM) can be added here
 * without touching game logic that iterates over categories.
 */
enum class CardCategory(
    /** Human-readable label shown in the UI */
    val label: String,
    /** Action Point cost to play this card type */
    val apCost: Int,
) {
    PARTY_LEADER(label = "Party Leader", apCost = 0),   // Played at game start, free
    HERO(label = "Hero", apCost = 1),
    ITEM(label = "Item", apCost = 1),
    MAGIC(label = "Magic", apCost = 1),
    MODIFIER(label = "Modifier", apCost = 0),           // Played in response (free AP)
    CHALLENGE(label = "Challenge", apCost = 0),         // Instant response (free AP)
    ;

    companion object {
        /** Categories that form the main draw deck (excludes PARTY_LEADER dealt separately) */
        val DECK_CATEGORIES = entries.filter { it != PARTY_LEADER }
    }
}
