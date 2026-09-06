package com.fachri.heretoslay.data.model

/**
 * Hero class / archetype.
 *
 * Base game has 6 classes. The expansion set adds 4 more (NECROMANCER, SHAPESHIFTER, ARCHER, MONK).
 * The [isBaseGame] flag lets the engine filter to base-game-only mode easily.
 *
 * Each class has a distinctive role that shapes its hero ability effects.
 */
enum class HeroClass(
    val label: String,
    val isBaseGame: Boolean = true,
) {
    // ── Base game (6 classes) ──────────────────────────────────────────────
    FIGHTER(label = "Fighter"),
    RANGER(label = "Ranger"),
    WIZARD(label = "Wizard"),
    GUARDIAN(label = "Guardian"),
    BARD(label = "Bard"),
    DRUID(label = "Druid"),

    // ── Expansion: Warband & Dragons ──────────────────────────────────────
    NECROMANCER(label = "Necromancer", isBaseGame = false),
    SHAPESHIFTER(label = "Shapeshifter", isBaseGame = false),

    // ── Expansion: Beasts & Nature ────────────────────────────────────────
    ARCHER(label = "Archer", isBaseGame = false),
    MONK(label = "Monk", isBaseGame = false),
    ;

    companion object {
        val BASE_CLASSES: List<HeroClass> = entries.filter { it.isBaseGame }
        val ALL_CLASSES: List<HeroClass>  = entries.toList()
    }
}
