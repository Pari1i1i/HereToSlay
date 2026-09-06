package com.fachri.heretoslay.data.model

/**
 * A Monster card that players can attack.
 *
 * Attack resolution:
 * - Player rolls 2d6 + any modifiers
 * - If roll >= [rollToKill] → monster is defeated
 * - If roll is within [counterAttackLow]..[counterAttackHigh] → counter-attack triggers
 *
 * Counter-attack means the attacking player suffers a penalty (discard a card).
 */
data class Monster(
    /** Stable unique identifier — format: "monster_{index}", e.g. "monster_01" */
    val id: String,

    /** Display name */
    val name: String,

    /**
     * Minimum 2d6 roll needed to kill this monster.
     * Range in base game: 5–10.
     */
    val rollToKill: Int,

    /**
     * Lower bound of the counter-attack range (inclusive).
     * If the attack roll falls in [counterAttackLow..counterAttackHigh], counter triggers.
     * Null means this monster has no counter-attack.
     */
    val counterAttackLow: Int? = null,

    /**
     * Upper bound of the counter-attack range (inclusive).
     */
    val counterAttackHigh: Int? = null,

    /**
     * Reward for killing this monster.
     * Currently stored as descriptive text; will be implemented as effect logic later.
     */
    val rewardText: String = "",

    /**
     * Penalty when counter-attack triggers.
     */
    val counterAttackText: String = "Discard 1 card from your hand.",

    /** Expansion flag */
    val isExpansion: Boolean = false,
) {
    /** True if this monster has a counter-attack range defined */
    val hasCounterAttack: Boolean
        get() = counterAttackLow != null && counterAttackHigh != null

    /** Returns true if [roll] triggers the counter-attack */
    fun isCounterAttack(roll: Int): Boolean =
        hasCounterAttack && roll in (counterAttackLow!!..counterAttackHigh!!)

    /** Returns true if [roll] kills the monster */
    fun isKill(roll: Int): Boolean = roll >= rollToKill
}
