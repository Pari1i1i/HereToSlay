package com.fachri.heretoslay.ui.component

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fachri.heretoslay.data.model.Monster
import com.fachri.heretoslay.ui.theme.HtsBorder
import com.fachri.heretoslay.ui.theme.HtsBorderSubtle
import com.fachri.heretoslay.ui.theme.HtsCardSurface
import com.fachri.heretoslay.ui.theme.HtsCrimson
import com.fachri.heretoslay.ui.theme.HtsCrimsonBright
import com.fachri.heretoslay.ui.theme.HtsDeepNavy
import com.fachri.heretoslay.ui.theme.HtsGold
import com.fachri.heretoslay.ui.theme.HtsMonsterGradientEnd
import com.fachri.heretoslay.ui.theme.HtsMonsterGradientStart
import com.fachri.heretoslay.ui.theme.HtsParchment
import com.fachri.heretoslay.ui.theme.HtsParchmentDim
import com.fachri.heretoslay.ui.theme.HtsSilver
import com.fachri.heretoslay.ui.theme.HtsSilverDim
import com.fachri.heretoslay.ui.theme.HtsSurfaceNavy
import com.fachri.heretoslay.ui.theme.MonsterCardShape

@Composable
fun ProceduralMonsterCard(
    monster: Monster,
    canAttack: Boolean,
    onAttackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .width(136.dp)
            .height(178.dp)
            .shadow(6.dp, MonsterCardShape)
            .clip(MonsterCardShape)
            .background(Brush.verticalGradient(listOf(HtsMonsterGradientStart, HtsMonsterGradientEnd)))
            .border(1.dp, if (canAttack) HtsCrimson.copy(alpha = 0.8f) else HtsBorderSubtle, MonsterCardShape)
            .padding(8.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // ── Top Header: Monster Badge + Slay Target ─────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(HtsCrimson.copy(alpha = 0.4f))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "MONSTER",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 7.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = HtsCrimsonBright,
                            letterSpacing = 0.5.sp,
                        ),
                    )
                }

                // Slay roll badge (e.g. 7+)
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(HtsGold)
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${monster.rollToKill}+",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Black,
                            color = HtsDeepNavy,
                        ),
                    )
                }
            }

            // ── Center: Monster Icon & Name ────────────────────────────────
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                MonsterDemonSigil()

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = monster.name,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = HtsParchment,
                        textAlign = TextAlign.Center,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                // Counter-attack warning pill
                if (monster.hasCounterAttack) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Counter: ${monster.counterAttackLow} - ${monster.counterAttackHigh}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 8.sp,
                            color = HtsCrimsonBright,
                            fontWeight = FontWeight.Medium,
                        ),
                    )
                }
            }

            // ── Reward Text & Attack Action Button ─────────────────────────
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = monster.rewardText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 8.5.sp,
                        color = HtsParchmentDim,
                        textAlign = TextAlign.Center,
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = onAttackClick,
                    enabled = canAttack,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HtsCrimson,
                        contentColor = HtsParchment,
                        disabledContainerColor = HtsSurfaceNavy,
                        disabledContentColor = HtsSilverDim,
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp),
                ) {
                    Text(
                        text = if (canAttack) "Serang (2 AP)" else "Serang",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun MonsterDemonSigil() {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(HtsDeepNavy.copy(alpha = 0.5f))
            .border(1.dp, HtsCrimson.copy(alpha = 0.5f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(20.dp)) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            // Horns
            val hornLeft = Path().apply {
                moveTo(cx - 3f, cy + 4f)
                lineTo(3f, 2f)
                lineTo(cx - 1f, cy - 2f)
                close()
            }
            drawPath(hornLeft, HtsCrimson)

            val hornRight = Path().apply {
                moveTo(cx + 3f, cy + 4f)
                lineTo(size.width - 3f, 2f)
                lineTo(cx + 1f, cy - 2f)
                close()
            }
            drawPath(hornRight, HtsCrimson)

            // Center eye
            drawCircle(HtsGold, radius = 3.dp.toPx(), center = Offset(cx, cy + 2f))
        }
    }
}
