package com.fachri.heretoslay.ui.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fachri.heretoslay.ui.theme.HtsBorder
import com.fachri.heretoslay.ui.theme.HtsDeepNavy
import com.fachri.heretoslay.ui.theme.HtsGold
import com.fachri.heretoslay.ui.theme.HtsGoldGlow
import com.fachri.heretoslay.ui.theme.HtsGoldMuted
import com.fachri.heretoslay.ui.theme.HtsParchment
import com.fachri.heretoslay.ui.theme.HtsParchmentDim
import com.fachri.heretoslay.ui.theme.HtsSilverDim
import kotlinx.coroutines.delay

/**
 * Splash screen with procedural animated logo.
 *
 * Animation sequence:
 *  1. Background fades in (deep navy)
 *  2. Central sigil draws itself (rotating hexagon + inner cross + orbiting particles)
 *  3. Title text fades + scales up
 *  4. Subtitle fades in
 *  5. Hold for a beat → callback to navigate to Home
 *
 * Zero external assets — everything drawn with Canvas geometry.
 */
@Composable
fun SplashScreen(onAnimationFinished: () -> Unit) {

    // ── Animation state ───────────────────────────────────────────────────────
    val sigilReveal = remember { Animatable(0f) }
    val titleReveal  = remember { Animatable(0f) }
    val subtitleReveal = remember { Animatable(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "splash_orbit")
    val orbitAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue  = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "orbitAngle",
    )
    val innerPulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue  = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = EaseOutCubic),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "innerPulse",
    )

    LaunchedEffect(Unit) {
        sigilReveal.animateTo(1f, tween(durationMillis = 900, easing = EaseOutCubic))
        delay(100)
        titleReveal.animateTo(1f, tween(durationMillis = 600, easing = EaseOutCubic))
        delay(200)
        subtitleReveal.animateTo(1f, tween(durationMillis = 500, easing = EaseOutCubic))
        delay(1400)
        onAnimationFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HtsDeepNavy),
        contentAlignment = Alignment.Center,
    ) {
        // ── Procedural sigil ─────────────────────────────────────────────
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = sigilReveal.value },
        ) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val baseRadius = minOf(size.width, size.height) * 0.18f

            drawBackgroundGlow(cx, cy, baseRadius * 2.2f)
            drawHexagon(cx, cy, baseRadius * innerPulse, sigilReveal.value)
            drawInnerCross(cx, cy, baseRadius * 0.6f * innerPulse, sigilReveal.value)
            drawOrbitingParticles(cx, cy, baseRadius * 1.35f, orbitAngle)
            drawCenterDiamond(cx, cy, baseRadius * 0.22f * innerPulse)
        }

        // ── Title ─────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 24.dp),
            contentAlignment = Alignment.BottomCenter,
        ) {
            androidx.compose.foundation.layout.Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "HERE TO SLAY",
                    style = MaterialTheme.typography.displayMedium.copy(
                        color = HtsGold,
                        letterSpacing = 6.sp,
                    ),
                    modifier = Modifier.graphicsLayer {
                        alpha = titleReveal.value
                        scaleX = 0.85f + 0.15f * titleReveal.value
                        scaleY = 0.85f + 0.15f * titleReveal.value
                    },
                )
                Text(
                    text = "CUSTOM EDITION",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = HtsParchmentDim,
                        letterSpacing = 4.sp,
                    ),
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .graphicsLayer { alpha = subtitleReveal.value },
                )
            }
        }
    }
}

// ─── Canvas drawing helpers ───────────────────────────────────────────────────

private fun DrawScope.drawBackgroundGlow(cx: Float, cy: Float, radius: Float) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(HtsGoldGlow, Color.Transparent),
            center = Offset(cx, cy),
            radius = radius,
        ),
        radius = radius,
        center = Offset(cx, cy),
    )
}

private fun DrawScope.drawHexagon(
    cx: Float, cy: Float, radius: Float, reveal: Float,
) {
    val path = Path()
    val sides = 6
    val angleStep = 360f / sides
    val startAngle = -90f

    val points = (0 until sides).map { i ->
        val angle = Math.toRadians((startAngle + i * angleStep).toDouble())
        Offset(
            x = cx + radius * Math.cos(angle).toFloat(),
            y = cy + radius * Math.sin(angle).toFloat(),
        )
    }

    val drawCount = (points.size * reveal).toInt().coerceAtLeast(1)
    path.moveTo(points[0].x, points[0].y)
    for (i in 1 until drawCount) path.lineTo(points[i].x, points[i].y)
    if (reveal >= 1f) path.close()

    drawPath(
        path = path,
        color = HtsGold,
        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round),
    )

    // Inner hex fill — subtle
    if (reveal >= 1f) {
        val innerPath = Path().apply {
            moveTo(points[0].x, points[0].y)
            points.drop(1).forEach { lineTo(it.x, it.y) }
            close()
        }
        drawPath(
            path = innerPath,
            brush = Brush.radialGradient(
                colors = listOf(Color(0x18D4A844), Color.Transparent),
                center = Offset(cx, cy),
                radius = radius,
            ),
        )
    }
}

private fun DrawScope.drawInnerCross(
    cx: Float, cy: Float, size: Float, reveal: Float,
) {
    val alpha = reveal.coerceIn(0f, 1f)
    val color = HtsGoldMuted.copy(alpha = alpha)
    val strokeWidth = 1.5.dp.toPx()

    // Vertical
    drawLine(
        color = color,
        start = Offset(cx, cy - size),
        end = Offset(cx, cy + size),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round,
    )
    // Horizontal
    drawLine(
        color = color,
        start = Offset(cx - size, cy),
        end = Offset(cx + size, cy),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round,
    )
    // Diagonal 1
    val d = size * 0.707f
    drawLine(
        color = color.copy(alpha = alpha * 0.5f),
        start = Offset(cx - d, cy - d),
        end = Offset(cx + d, cy + d),
        strokeWidth = strokeWidth * 0.7f,
        cap = StrokeCap.Round,
    )
    // Diagonal 2
    drawLine(
        color = color.copy(alpha = alpha * 0.5f),
        start = Offset(cx + d, cy - d),
        end = Offset(cx - d, cy + d),
        strokeWidth = strokeWidth * 0.7f,
        cap = StrokeCap.Round,
    )
}

private fun DrawScope.drawOrbitingParticles(
    cx: Float, cy: Float, orbitRadius: Float, angleDeg: Float,
) {
    val particleCount = 6
    val angleStep = 360f / particleCount

    repeat(particleCount) { i ->
        val angle = Math.toRadians((angleDeg + i * angleStep).toDouble())
        val px = cx + orbitRadius * Math.cos(angle).toFloat()
        val py = cy + orbitRadius * Math.sin(angle).toFloat()
        val alpha = if (i % 2 == 0) 0.9f else 0.5f
        val radius = if (i % 2 == 0) 4.dp.toPx() else 2.5.dp.toPx()

        drawCircle(
            color = HtsGold.copy(alpha = alpha),
            radius = radius,
            center = Offset(px, py),
        )
        // Trail glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(HtsGoldGlow, Color.Transparent),
                center = Offset(px, py),
                radius = radius * 3f,
            ),
            radius = radius * 3f,
            center = Offset(px, py),
        )
    }
}

private fun DrawScope.drawCenterDiamond(cx: Float, cy: Float, size: Float) {
    val path = Path().apply {
        moveTo(cx, cy - size)
        lineTo(cx + size, cy)
        lineTo(cx, cy + size)
        lineTo(cx - size, cy)
        close()
    }
    drawPath(
        path = path,
        brush = Brush.radialGradient(
            colors = listOf(HtsGold, HtsGoldMuted),
            center = Offset(cx, cy),
            radius = size,
        ),
    )
}
