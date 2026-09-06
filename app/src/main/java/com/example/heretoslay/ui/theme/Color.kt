package com.example.heretoslay.ui.theme

import androidx.compose.ui.graphics.Color

// ─── Brand Palette ────────────────────────────────────────────────────────────

/** Almost-black deep navy — primary background */
val HtsDeepNavy        = Color(0xFF0A0E1A)
/** Mid-layer navy — cards, surface elevations */
val HtsSurfaceNavy     = Color(0xFF111827)
/** Card surfaces and secondary surfaces */
val HtsCardSurface     = Color(0xFF1C2333)
/** Subtle border and divider */
val HtsBorder          = Color(0xFF252D40)
/** Even subtler border for inactive elements */
val HtsBorderSubtle    = Color(0xFF1A2030)

// ─── Accent Palette ───────────────────────────────────────────────────────────

/** Primary gold — hero actions, highlights, buttons */
val HtsGold            = Color(0xFFD4A844)
/** Muted gold — secondary highlights, icons */
val HtsGoldMuted       = Color(0xFF9A7830)
/** Gold glow for animation effects */
val HtsGoldGlow        = Color(0x40D4A844)

/** Parchment — card text backgrounds, readable on dark */
val HtsParchment       = Color(0xFFF5EDD6)
/** Parchment dim — secondary text on cards */
val HtsParchmentDim    = Color(0xFFB8A87A)

// ─── Semantic Colors ──────────────────────────────────────────────────────────

/** Crimson — combat, danger, monster attacks */
val HtsCrimson         = Color(0xFFB03030)
/** Crimson bright — active combat indicator */
val HtsCrimsonBright   = Color(0xFFE53935)
/** Crimson glow — animation backing */
val HtsCrimsonGlow     = Color(0x50B03030)

/** Emerald — success, heal, party complete */
val HtsEmerald         = Color(0xFF2E7D52)
/** Emerald bright — active positive state */
val HtsEmeraldBright   = Color(0xFF43A868)
/** Emerald glow — animation backing */
val HtsEmeraldGlow     = Color(0x4043A868)

/** Sapphire — magic cards */
val HtsSapphire        = Color(0xFF2255A0)
/** Sapphire bright — active magic */
val HtsSapphireBright  = Color(0xFF4C7FD0)

/** Amethyst — modifier cards */
val HtsAmethyst        = Color(0xFF6A3EA0)
/** Amethyst bright */
val HtsAmethystBright  = Color(0xFF9B5FD6)

/** Silver — neutral, muted elements */
val HtsSilver          = Color(0xFF8A94A6)
/** Silver dim — disabled states */
val HtsSilverDim       = Color(0xFF4A5568)

// ─── Bounty / House Rules ─────────────────────────────────────────────────────

/** Bounty amber — the bounty marker colour */
val HtsBounty          = Color(0xFFE87722)
/** Bounty glow */
val HtsBountyGlow      = Color(0x60E87722)

/** Curse purple-black — curse/raid overlay base */
val HtsCurse           = Color(0xFF2D0A3A)
/** Curse bright — curse visual accent */
val HtsCurseBright     = Color(0xFF8B00FF)
/** Curse glow */
val HtsCurseGlow       = Color(0x70570099)

// ─── Utility ─────────────────────────────────────────────────────────────────

val HtsBlack           = Color(0xFF000000)
val HtsWhite           = Color(0xFFFFFFFF)
val HtsTransparent     = Color(0x00000000)

// ─── Card Category Colors ────────────────────────────────────────────────────
// Each card category has a base + gradient pair for prosedural card rendering

val HtsHeroGradientStart   = Color(0xFF1A3A5C)
val HtsHeroGradientEnd     = Color(0xFF0D1F35)
val HtsLeaderGradientStart = Color(0xFF3A2A0A)
val HtsLeaderGradientEnd   = Color(0xFF1A1205)
val HtsItemGradientStart   = Color(0xFF1A3A2A)
val HtsItemGradientEnd     = Color(0xFF0D1F15)
val HtsMagicGradientStart  = Color(0xFF1A1A4A)
val HtsMagicGradientEnd    = Color(0xFF0D0D28)
val HtsModifierGradientStart = Color(0xFF2A1A3A)
val HtsModifierGradientEnd   = Color(0xFF150D1F)
val HtsChallengeGradientStart = Color(0xFF3A1A1A)
val HtsChallengeGradientEnd   = Color(0xFF1F0D0D)
val HtsMonsterGradientStart = Color(0xFF2A1505)
val HtsMonsterGradientEnd   = Color(0xFF150A02)