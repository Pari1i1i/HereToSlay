package com.fachri.heretoslay.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutBounce
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fachri.heretoslay.data.repository.AttackResult
import com.fachri.heretoslay.ui.theme.HtsCardSurface
import com.fachri.heretoslay.ui.theme.HtsCrimson
import com.fachri.heretoslay.ui.theme.HtsCrimsonBright
import com.fachri.heretoslay.ui.theme.HtsDeepNavy
import com.fachri.heretoslay.ui.theme.HtsEmerald
import com.fachri.heretoslay.ui.theme.HtsEmeraldBright
import com.fachri.heretoslay.ui.theme.HtsGold
import com.fachri.heretoslay.ui.theme.HtsParchment
import com.fachri.heretoslay.ui.theme.HtsParchmentDim
import kotlinx.coroutines.delay

@Composable
fun DiceRollOverlay(
    attackResult: AttackResult,
    onDismiss: () -> Unit,
) {
    var displayDie1 by remember { mutableIntStateOf(1) }
    var displayDie2 by remember { mutableIntStateOf(1) }
    var isSettled by remember { androidx.compose.runtime.mutableStateOf(false) }

    val scaleAnim = remember { Animatable(0.4f) }

    LaunchedEffect(attackResult) {
        scaleAnim.animateTo(1f, tween(300, easing = EaseOutBounce))
        // Spin numbers for 600ms
        repeat(8) {
            displayDie1 = (1..6).random()
            displayDie2 = (1..6).random()
            delay(75)
        }
        displayDie1 = attackResult.die1
        displayDie2 = attackResult.die2
        isSettled = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f))
            .clickable(enabled = false) {}, // Scrim
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scaleAnim.value
                    scaleY = scaleAnim.value
                }
                .clip(RoundedCornerShape(16.dp))
                .background(HtsCardSurface)
                .border(2.dp, if (attackResult.isKill) HtsEmeraldBright else if (attackResult.isCounterAttack) HtsCrimsonBright else HtsGold, RoundedCornerShape(16.dp))
                .padding(horizontal = 32.dp, vertical = 20.dp),
        ) {
            Text(
                text = "SERANGAN KE ${attackResult.monsterName.uppercase()}",
                style = MaterialTheme.typography.labelLarge.copy(
                    color = HtsGold,
                    letterSpacing = 2.sp,
                    fontSize = 12.sp,
                ),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Two Dice
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ProceduralDie(value = displayDie1)
                ProceduralDie(value = displayDie2)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Total Score
            Text(
                text = "Total Roll: ${if (isSettled) attackResult.totalRoll else displayDie1 + displayDie2}",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = HtsParchment,
                    fontWeight = FontWeight.Black,
                ),
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Result banner
            if (isSettled) {
                val bannerText = when {
                    attackResult.isKill -> "MONSTER BERHASIL DIBUNUH!"
                    attackResult.isCounterAttack -> "GAGAL! MONSTER MEMBALAS SERANG (DISCARD 1 KARTU)"
                    else -> "SERANGAN GAGAL (TIDAK CUKUP SKOR)"
                }
                val bannerColor = when {
                    attackResult.isKill -> HtsEmeraldBright
                    attackResult.isCounterAttack -> HtsCrimsonBright
                    else -> HtsParchmentDim
                }

                Text(
                    text = bannerText,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = bannerColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                    ),
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (attackResult.isKill) HtsEmerald else HtsGold,
                        contentColor = if (attackResult.isKill) HtsParchment else HtsDeepNavy,
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(42.dp),
                ) {
                    Text("Lanjutkan Permainan", fontWeight = FontWeight.Bold)
                }
            } else {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
private fun ProceduralDie(value: Int) {
    Box(
        modifier = Modifier
            .size(54.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(HtsParchment)
            .border(1.5.dp, HtsGold, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(40.dp)) {
            val r = 3.dp.toPx()
            val w = size.width
            val h = size.height
            val color = HtsDeepNavy

            fun drawPip(x: Float, y: Float) = drawCircle(color, radius = r, center = Offset(x, y))

            val left = w * 0.25f
            val midX = w * 0.5f
            val right = w * 0.75f

            val top = h * 0.25f
            val midY = h * 0.5f
            val bot = h * 0.75f

            when (value) {
                1 -> drawPip(midX, midY)
                2 -> {
                    drawPip(left, top)
                    drawPip(right, bot)
                }
                3 -> {
                    drawPip(left, top)
                    drawPip(midX, midY)
                    drawPip(right, bot)
                }
                4 -> {
                    drawPip(left, top)
                    drawPip(right, top)
                    drawPip(left, bot)
                    drawPip(right, bot)
                }
                5 -> {
                    drawPip(left, top)
                    drawPip(right, top)
                    drawPip(midX, midY)
                    drawPip(left, bot)
                    drawPip(right, bot)
                }
                6 -> {
                    drawPip(left, top)
                    drawPip(right, top)
                    drawPip(left, midY)
                    drawPip(right, midY)
                    drawPip(left, bot)
                    drawPip(right, bot)
                }
            }
        }
    }
}
