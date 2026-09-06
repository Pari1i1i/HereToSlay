package com.fachri.heretoslay.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fachri.heretoslay.data.model.Player
import com.fachri.heretoslay.ui.theme.HtsBorder
import com.fachri.heretoslay.ui.theme.HtsBorderSubtle
import com.fachri.heretoslay.ui.theme.HtsCardSurface
import com.fachri.heretoslay.ui.theme.HtsDeepNavy
import com.fachri.heretoslay.ui.theme.HtsEmerald
import com.fachri.heretoslay.ui.theme.HtsEmeraldBright
import com.fachri.heretoslay.ui.theme.HtsEmeraldGlow
import com.fachri.heretoslay.ui.theme.HtsGold
import com.fachri.heretoslay.ui.theme.HtsGoldGlow
import com.fachri.heretoslay.ui.theme.HtsGoldMuted
import com.fachri.heretoslay.ui.theme.HtsParchment
import com.fachri.heretoslay.ui.theme.HtsParchmentDim
import com.fachri.heretoslay.ui.theme.HtsSilver
import com.fachri.heretoslay.ui.theme.HtsSilverDim
import com.fachri.heretoslay.ui.theme.HtsSurfaceNavy

@Composable
fun PlayerSlotCard(
    slotNumber: Int,
    player: Player?,
    isLocalPlayer: Boolean,
    modifier: Modifier = Modifier,
) {
    val isOccupied = player != null
    val isReady = player?.isReady == true
    val isHost = player?.isHost == true

    val borderColor by animateColorAsState(
        targetValue = when {
            !isOccupied -> HtsBorderSubtle
            isHost -> HtsGold
            isReady -> HtsEmeraldBright
            else -> HtsBorder
        },
        animationSpec = tween(300),
        label = "slotBorderColor"
    )

    Box(
        modifier = modifier
            .height(78.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isOccupied) HtsCardSurface else HtsDeepNavy.copy(alpha = 0.5f))
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        if (player != null) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Procedural avatar circle
                ProceduralPlayerAvatar(
                    initial = player.name.firstOrNull()?.uppercaseChar() ?: 'P',
                    isHost = isHost,
                    isReady = isReady,
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Name and badge
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = player.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = HtsParchment,
                            ),
                            maxLines = 1,
                        )
                        if (isLocalPlayer) {
                            Text(
                                text = " (Kamu)",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = HtsGoldMuted,
                                    fontSize = 11.sp,
                                ),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (isHost) {
                            HostBadge()
                        }
                        ReadyBadge(isReady = isReady)
                    }
                }
            }
        } else {
            // Empty slot placeholder
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(HtsSurfaceNavy)
                        .border(1.dp, HtsBorderSubtle, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "$slotNumber",
                        style = MaterialTheme.typography.labelMedium.copy(color = HtsSilverDim),
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Slot $slotNumber",
                        style = MaterialTheme.typography.titleSmall.copy(color = HtsSilverDim),
                    )
                    Text(
                        text = "Menunggu pemain...",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = HtsSilverDim.copy(alpha = 0.6f),
                            fontSize = 11.sp,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun ProceduralPlayerAvatar(
    initial: Char,
    isHost: Boolean,
    isReady: Boolean,
) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = if (isHost) listOf(HtsGold, HtsGoldMuted)
                    else if (isReady) listOf(HtsEmeraldBright, HtsEmerald)
                    else listOf(HtsSurfaceNavy, HtsDeepNavy)
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val r = size.minDimension / 2f
            drawCircle(
                color = Color.White.copy(alpha = 0.15f),
                radius = r - 2.dp.toPx(),
                style = Stroke(width = 1.5.dp.toPx())
            )
        }
        Text(
            text = initial.toString(),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = if (isHost) HtsDeepNavy else HtsParchment,
            ),
        )
    }
}

@Composable
private fun HostBadge() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(HtsGold.copy(alpha = 0.2f))
            .border(1.dp, HtsGold.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Canvas(modifier = Modifier.size(8.dp)) {
                val path = Path().apply {
                    moveTo(0f, size.height)
                    lineTo(size.width * 0.2f, size.height * 0.3f)
                    lineTo(size.width * 0.5f, size.height * 0.8f)
                    lineTo(size.width * 0.8f, size.height * 0.3f)
                    lineTo(size.width, size.height)
                    close()
                }
                drawPath(path, HtsGold)
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "HOST",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = HtsGold,
                    letterSpacing = 0.5.sp,
                ),
            )
        }
    }
}

@Composable
private fun ReadyBadge(isReady: Boolean) {
    val bg = if (isReady) HtsEmerald.copy(alpha = 0.25f) else HtsSilverDim.copy(alpha = 0.25f)
    val color = if (isReady) HtsEmeraldBright else HtsSilver

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (isReady) "SIAP" else "MENUNGGU",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                color = color,
                letterSpacing = 0.5.sp,
            ),
        )
    }
}
