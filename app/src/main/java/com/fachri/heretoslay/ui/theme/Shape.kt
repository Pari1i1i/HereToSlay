package com.fachri.heretoslay.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val HtsShapes = Shapes(
    // ExtraSmall — AP dots, small badges
    extraSmall = RoundedCornerShape(4.dp),
    // Small — chips, tags, small buttons
    small = RoundedCornerShape(8.dp),
    // Medium — cards, dialogs, inputs
    medium = RoundedCornerShape(12.dp),
    // Large — bottom sheets, main panels
    large = RoundedCornerShape(16.dp),
    // ExtraLarge — full-screen overlays, modals
    extraLarge = RoundedCornerShape(24.dp),
)

// Custom shapes used outside of Material3 Shapes
val CardShape = RoundedCornerShape(10.dp)
val PillShape = RoundedCornerShape(percent = 50)
val MonsterCardShape = RoundedCornerShape(12.dp)
val OverlayShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
