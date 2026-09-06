package com.example.heretoslay.data.seed

import com.example.heretoslay.data.model.Card
import com.example.heretoslay.data.model.CardCategory
import com.example.heretoslay.data.model.HeroClass

/**
 * Seed data for the base game deck — 115 cards total.
 *
 * Distribution (matching official Here to Slay base game):
 *   Party Leaders :  6  (1 per class)
 *   Heroes        : 48  (8 per class × 6 classes)
 *   Items         : 12
 *   Magic         :  9
 *   Modifiers     : 19
 *   Challenges    : 21
 *   ─────────────────
 *   Total         :115
 *
 * All card names and effects are placeholder/dummy for MVP.
 * The IDs are stable — safe to reference in Firestore documents.
 */
object CardSeedData {

    // ─── Party Leaders (6) ────────────────────────────────────────────────────

    private val partyLeaders: List<Card> = HeroClass.BASE_CLASSES.mapIndexed { i, cls ->
        Card(
            id = "leader_${cls.name.lowercase()}",
            name = "${cls.label} Leader",
            category = CardCategory.PARTY_LEADER,
            heroClass = cls,
            effectText = "[${cls.label} Leader passive effect — to be filled]",
        )
    }

    // ─── Heroes (48 = 8 per class × 6 classes) ───────────────────────────────

    private val heroes: List<Card> = HeroClass.BASE_CLASSES.flatMap { cls ->
        (1..8).map { index ->
            val pad = index.toString().padStart(2, '0')
            Card(
                id = "hero_${cls.name.lowercase()}_$pad",
                name = "${cls.label} Hero $index",
                category = CardCategory.HERO,
                heroClass = cls,
                abilityRollRequirement = when (index % 4) {
                    0 -> null   // Passive hero — no roll required
                    1 -> 4
                    2 -> 5
                    else -> 6
                },
                abilityApCost = 1,
                effectText = "[${cls.label} hero ability $index — to be filled]",
            )
        }
    }

    // ─── Items (12) ───────────────────────────────────────────────────────────

    private val items: List<Card> = (1..12).map { i ->
        val pad = i.toString().padStart(2, '0')
        Card(
            id = "item_$pad",
            name = "Item $i",
            category = CardCategory.ITEM,
            effectText = "[Item $i effect — to be filled]",
        )
    }

    // ─── Magic (9) ────────────────────────────────────────────────────────────

    private val magic: List<Card> = (1..9).map { i ->
        val pad = i.toString().padStart(2, '0')
        Card(
            id = "magic_$pad",
            name = "Magic Spell $i",
            category = CardCategory.MAGIC,
            effectText = "[Magic $i effect — to be filled]",
        )
    }

    // ─── Modifiers (19) ──────────────────────────────────────────────────────

    private val modifiers: List<Card> = (1..19).map { i ->
        val pad = i.toString().padStart(2, '0')
        Card(
            id = "modifier_$pad",
            name = "Modifier $i",
            category = CardCategory.MODIFIER,
            effectText = "[Modifier $i effect — to be filled]",
        )
    }

    // ─── Challenges (21) ─────────────────────────────────────────────────────

    private val challenges: List<Card> = (1..21).map { i ->
        val pad = i.toString().padStart(2, '0')
        Card(
            id = "challenge_$pad",
            name = "Challenge $i",
            category = CardCategory.CHALLENGE,
            effectText = "[Challenge $i effect — to be filled]",
        )
    }

    // ─── Public accessors ─────────────────────────────────────────────────────

    /** All 115 base game cards (not including monsters) */
    val allCards: List<Card> = partyLeaders + heroes + items + magic + modifiers + challenges

    /** The 109-card draw deck (excludes Party Leaders which are dealt separately) */
    val drawDeck: List<Card> = heroes + items + magic + modifiers + challenges

    /** Only Party Leader cards — one per class */
    val partyLeaderCards: List<Card> = partyLeaders

    /** Look up a card by its stable ID */
    private val cardById: Map<String, Card> = allCards.associateBy { it.id }
    fun findById(id: String): Card? = cardById[id]

    init {
        // Sanity check — catches distribution errors immediately at startup
        val total = allCards.size
        check(total == 115) {
            "Expected 115 base game cards, got $total. " +
                "Breakdown: leaders=${partyLeaders.size}, heroes=${heroes.size}, " +
                "items=${items.size}, magic=${magic.size}, " +
                "modifiers=${modifiers.size}, challenges=${challenges.size}"
        }
    }
}
