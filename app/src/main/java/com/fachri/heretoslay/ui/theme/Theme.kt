package com.fachri.heretoslay.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

// ─── Dark-only color scheme — this app never uses light mode ─────────────────
private val HtsDarkColorScheme = darkColorScheme(
    primary             = HtsGold,
    onPrimary           = HtsDeepNavy,
    primaryContainer    = HtsGoldMuted,
    onPrimaryContainer  = HtsParchment,

    secondary           = HtsSilver,
    onSecondary         = HtsDeepNavy,
    secondaryContainer  = HtsSilverDim,
    onSecondaryContainer = HtsParchment,

    tertiary            = HtsEmeraldBright,
    onTertiary          = HtsDeepNavy,
    tertiaryContainer   = HtsEmerald,
    onTertiaryContainer = HtsParchment,

    error               = HtsCrimsonBright,
    onError             = HtsWhite,
    errorContainer      = HtsCrimson,
    onErrorContainer    = HtsParchment,

    background          = HtsDeepNavy,
    onBackground        = HtsParchment,

    surface             = HtsSurfaceNavy,
    onSurface           = HtsParchment,
    surfaceVariant      = HtsCardSurface,
    onSurfaceVariant    = HtsSilver,

    outline             = HtsBorder,
    outlineVariant      = HtsBorderSubtle,

    scrim               = Color(0xCC000000),

    inverseSurface      = HtsParchment,
    inverseOnSurface    = HtsDeepNavy,
    inversePrimary      = HtsGoldMuted,
)

@Composable
fun HereToSlayTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            // Immersive fullscreen mode — hide status bar (battery/clock/wifi) and navigation bar
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val controller = WindowCompat.getInsetsController(window, view)
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
        }
    }

    MaterialTheme(
        colorScheme = HtsDarkColorScheme,
        typography  = HtsTypography,
        shapes      = HtsShapes,
        content     = content,
    )
}
