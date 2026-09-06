package com.example.heretoslay.data.seed

import com.example.heretoslay.data.model.Monster

/**
 * Seed data for the 15 base-game Monsters.
 *
 * Roll values (2d6 system):
 *   Min possible roll: 2   Max possible roll: 12
 *
 * Difficulty tiers:
 *   Easy   — rollToKill 5–6   (trivially killed with modifiers)
 *   Medium — rollToKill 7–8
 *   Hard   — rollToKill 9–10
 *
 * Counter-attack ranges: a band of low rolls that punish failed attacks.
 * E.g. counterAttackLow=2, counterAttackHigh=4 means rolling 2,3,4 → counter.
 *
 * All names and reward text are placeholder for MVP. The mechanical values
 * (rollToKill, counter-attack ranges) are representative of the base game balance.
 */
object MonsterSeedData {

    val allMonsters: List<Monster> = listOf(
        // Easy tier (5 monsters)
        Monster(
            id = "monster_01",
            name = "Goblin Scout",
            rollToKill = 5,
            counterAttackLow = 2, counterAttackHigh = 2,
            rewardText = "Draw 1 card.",
            counterAttackText = "Discard 1 card.",
        ),
        Monster(
            id = "monster_02",
            name = "Kobold Raider",
            rollToKill = 5,
            counterAttackLow = null, counterAttackHigh = null,
            rewardText = "Gain 1 AP next turn.",
        ),
        Monster(
            id = "monster_03",
            name = "Cave Bat",
            rollToKill = 6,
            counterAttackLow = 2, counterAttackHigh = 3,
            rewardText = "Draw 2 cards.",
            counterAttackText = "Lose 1 AP.",
        ),
        Monster(
            id = "monster_04",
            name = "Skeletal Archer",
            rollToKill = 6,
            counterAttackLow = 2, counterAttackHigh = 2,
            rewardText = "Another player discards 1 card.",
        ),
        Monster(
            id = "monster_05",
            name = "Slime Mold",
            rollToKill = 6,
            counterAttackLow = null, counterAttackHigh = null,
            rewardText = "Place any 1 Item from discard into your hand.",
        ),

        // Medium tier (5 monsters)
        Monster(
            id = "monster_06",
            name = "Orc Berserker",
            rollToKill = 7,
            counterAttackLow = 2, counterAttackHigh = 4,
            rewardText = "Draw 1 card. You may play 1 additional Hero this turn.",
            counterAttackText = "Discard 2 cards.",
        ),
        Monster(
            id = "monster_07",
            name = "Harpy Witch",
            rollToKill = 7,
            counterAttackLow = 2, counterAttackHigh = 3,
            rewardText = "Steal 1 Item from another player's party.",
            counterAttackText = "Discard 1 card from your hand.",
        ),
        Monster(
            id = "monster_08",
            name = "Stone Golem",
            rollToKill = 8,
            counterAttackLow = 2, counterAttackHigh = 4,
            rewardText = "Your heroes cannot be targeted by Magic this round.",
            counterAttackText = "One of your heroes is returned to your hand.",
        ),
        Monster(
            id = "monster_09",
            name = "Shadow Stalker",
            rollToKill = 8,
            counterAttackLow = 2, counterAttackHigh = 3,
            rewardText = "Draw 3 cards, then discard 1.",
            counterAttackText = "Discard 1 card at random.",
        ),
        Monster(
            id = "monster_10",
            name = "Marsh Hydra",
            rollToKill = 8,
            counterAttackLow = 2, counterAttackHigh = 5,
            rewardText = "Gain an extra attack action this turn.",
            counterAttackText = "Discard your entire hand.",
        ),

        // Hard tier (5 monsters)
        Monster(
            id = "monster_11",
            name = "Fire Drake",
            rollToKill = 9,
            counterAttackLow = 2, counterAttackHigh = 4,
            rewardText = "All other players discard 1 card.",
            counterAttackText = "Lose all remaining AP this turn.",
        ),
        Monster(
            id = "monster_12",
            name = "Lich King",
            rollToKill = 9,
            counterAttackLow = 2, counterAttackHigh = 5,
            rewardText = "Revive 1 defeated monster under your control (it counts as your kill).",
            counterAttackText = "Return 1 Hero from your party to your hand.",
        ),
        Monster(
            id = "monster_13",
            name = "Wyvern",
            rollToKill = 10,
            counterAttackLow = 2, counterAttackHigh = 4,
            rewardText = "Draw 2 cards and gain 1 AP.",
            counterAttackText = "Discard 2 cards from your hand.",
        ),
        Monster(
            id = "monster_14",
            name = "Elder Titan",
            rollToKill = 10,
            counterAttackLow = 2, counterAttackHigh = 5,
            rewardText = "You win all Challenges this round without rolling.",
            counterAttackText = "All players draw 2 cards — you draw none.",
        ),
        Monster(
            id = "monster_15",
            name = "Void Serpent",
            rollToKill = 10,
            counterAttackLow = 2, counterAttackHigh = 6,
            rewardText = "Immediately take another full turn.",
            counterAttackText = "Discard your hand and skip your next turn.",
        ),
    )

    /** Look up a monster by its stable ID */
    private val monsterById: Map<String, Monster> = allMonsters.associateBy { it.id }
    fun findById(id: String): Monster? = monsterById[id]

    init {
        check(allMonsters.size == 15) {
            "Expected 15 monsters, got ${allMonsters.size}"
        }
    }
}
