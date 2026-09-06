package com.fachri.heretoslay.data.repository

import com.fachri.heretoslay.data.model.Card
import com.fachri.heretoslay.data.model.CardCategory
import com.fachri.heretoslay.data.model.HeroClass
import com.fachri.heretoslay.data.model.Monster
import com.fachri.heretoslay.data.seed.CardSeedData
import com.fachri.heretoslay.data.seed.MonsterSeedData

/**
 * In-memory card and monster repository.
 *
 * In Part 1 (Foundation), this serves all card/monster lookups.
 * In Part 2+ (Firebase), the deck state (ordered card IDs) is managed
 * server-side; this repository is still used for resolving card objects
 * from their IDs.
 */
class CardRepository {

    // ─── Card access ─────────────────────────────────────────────────────────

    /** All 115 base-game cards */
    val allCards: List<Card> = CardSeedData.allCards

    /** All Party Leader cards (6) */
    val partyLeaders: List<Card> = CardSeedData.partyLeaderCards

    /**
     * The 109-card draw deck (heroes + items + magic + modifiers + challenges).
     * Excludes Party Leaders, which are dealt separately at game start.
     */
    val drawDeck: List<Card> = CardSeedData.drawDeck

    /** Retrieve a card by its stable ID. Returns null if not found. */
    fun findCard(id: String): Card? = CardSeedData.findById(id)

    /** Retrieve multiple cards by their IDs, preserving order. Skips unknown IDs. */
    fun findCards(ids: List<String>): List<Card> = ids.mapNotNull { CardSeedData.findById(it) }

    /** All cards of a specific category */
    fun cardsByCategory(category: CardCategory): List<Card> =
        allCards.filter { it.category == category }

    /** All Hero cards belonging to a specific class */
    fun heroesByClass(heroClass: HeroClass): List<Card> =
        allCards.filter { it.heroClass == heroClass && it.category == CardCategory.HERO }

    /** Party Leader for a specific class */
    fun leaderForClass(heroClass: HeroClass): Card? =
        partyLeaders.firstOrNull { it.heroClass == heroClass }

    // ─── Monster access ───────────────────────────────────────────────────────

    /** All 15 base-game monsters */
    val allMonsters: List<Monster> = MonsterSeedData.allMonsters

    /** Retrieve a monster by its stable ID. Returns null if not found. */
    fun findMonster(id: String): Monster? = MonsterSeedData.findById(id)

    /** Retrieve multiple monsters by their IDs, preserving order. Skips unknown IDs. */
    fun findMonsters(ids: List<String>): List<Monster> =
        ids.mapNotNull { MonsterSeedData.findById(it) }

    // ─── Deck building ────────────────────────────────────────────────────────

    /**
     * Returns a shuffled list of card IDs for a new game.
     * Uses Fisher-Yates shuffle for true randomness.
     */
    fun buildShuffledDeckIds(): List<String> = drawDeck.map { it.id }.shuffled()

    /**
     * Returns all monster IDs in random order for a new game.
     * In base game, all 15 monsters are placed face-down and revealed 3 at a time.
     */
    fun buildShuffledMonsterIds(): List<String> = allMonsters.map { it.id }.shuffled()
}
