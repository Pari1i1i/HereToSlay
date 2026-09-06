package com.example.heretoslay.data.model

/**
 * A single card in the HTS deck.
 *
 * Design notes:
 * - [id] is a stable, unique string — safe to store in Firestore deckState lists.
 * - All nullable fields are null for irrelevant categories (e.g. [heroClass] is null for Magic cards).
 * - [abilityApCost] only applies to Hero cards that have an activated ability.
 * - [effectText] is placeholder for now; will be fleshed out with real card effects in later parts.
 */
data class Card(
    /** Stable unique identifier — format: "{category_prefix}_{index}", e.g. "hero_fighter_01" */
    val id: String,

    /** Display name */
    val name: String,

    /** Card category */
    val category: CardCategory,

    /**
     * Hero class association.
     * - Non-null for [CardCategory.HERO] and [CardCategory.PARTY_LEADER]
     * - Null for all other categories
     */
    val heroClass: HeroClass? = null,

    /**
     * The roll requirement for a Hero ability activation (1d6).
     * Only relevant for [CardCategory.HERO] cards with an activated ability.
     * Null if the hero has a passive effect only.
     */
    val abilityRollRequirement: Int? = null,

    /**
     * AP cost to use this hero's ability (usually 1).
     * Only relevant for [CardCategory.HERO].
     */
    val abilityApCost: Int = 1,

    /**
     * Plain-text description of the card's effect.
     * Currently placeholder text; will be filled with real effects later.
     */
    val effectText: String = "",

    /**
     * Whether this card is from an expansion (affects deck-building modes).
     */
    val isExpansion: Boolean = false,
)
