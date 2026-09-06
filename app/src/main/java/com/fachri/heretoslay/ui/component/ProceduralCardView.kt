package com.fachri.heretoslay.ui.component

import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import com.fachri.heretoslay.data.model.Card
import com.fachri.heretoslay.data.model.CardCategory
import com.fachri.heretoslay.data.model.HeroClass
import com.fachri.heretoslay.ui.theme.CardShape
import com.fachri.heretoslay.ui.theme.HtsBorder
import com.fachri.heretoslay.ui.theme.HtsBorderSubtle
import com.fachri.heretoslay.ui.theme.HtsChallengeGradientEnd
import com.fachri.heretoslay.ui.theme.HtsChallengeGradientStart
import com.fachri.heretoslay.ui.theme.HtsDeepNavy
import com.fachri.heretoslay.ui.theme.HtsGold
import com.fachri.heretoslay.ui.theme.HtsGoldGlow
import com.fachri.heretoslay.ui.theme.HtsHeroGradientEnd
import com.fachri.heretoslay.ui.theme.HtsHeroGradientStart
import com.fachri.heretoslay.ui.theme.HtsItemGradientEnd
import com.fachri.heretoslay.ui.theme.HtsItemGradientStart
import com.fachri.heretoslay.ui.theme.HtsLeaderGradientEnd
import com.fachri.heretoslay.ui.theme.HtsLeaderGradientStart
import com.fachri.heretoslay.ui.theme.HtsMagicGradientEnd
import com.fachri.heretoslay.ui.theme.HtsMagicGradientStart
import com.fachri.heretoslay.ui.theme.HtsModifierGradientEnd
import com.fachri.heretoslay.ui.theme.HtsModifierGradientStart
import com.fachri.heretoslay.ui.theme.HtsParchment
import com.fachri.heretoslay.ui.theme.HtsParchmentDim
import com.fachri.heretoslay.ui.theme.HtsSilver
import com.fachri.heretoslay.ui.theme.HtsSilverDim

@Composable
fun ProceduralCardView(
    card: Card,
    isSelected: Boolean = false,
    isPlayable: Boolean = true,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val offsetY by animateDpAsState(
        targetValue = if (isSelected) (-12).dp else 0.dp,
        animationSpec = tween(200),
        label = "cardOffsetY"
    )

    val (startColor, endColor) = when (card.category) {
        CardCategory.HERO -> HtsHeroGradientStart to HtsHeroGradientEnd
        CardCategory.PARTY_LEADER -> HtsLeaderGradientStart to HtsLeaderGradientEnd
        CardCategory.ITEM -> HtsItemGradientStart to HtsItemGradientEnd
        CardCategory.MAGIC -> HtsMagicGradientStart to HtsMagicGradientEnd
        CardCategory.MODIFIER -> HtsModifierGradientStart to HtsModifierGradientEnd
        CardCategory.CHALLENGE -> HtsChallengeGradientStart to HtsChallengeGradientEnd
    }

    val borderColor = when {
        isSelected -> HtsGold
        isPlayable -> HtsBorder
        else -> HtsBorderSubtle
    }

    Box(
        modifier = modifier
            .offset(y = offsetY)
            .width(108.dp)
            .height(148.dp)
            .shadow(if (isSelected) 10.dp else 4.dp, CardShape)
            .clip(CardShape)
            .background(Brush.verticalGradient(listOf(startColor, endColor)))
            .border(if (isSelected) 2.dp else 1.dp, borderColor, CardShape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(6.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // ── Card Header (Category + AP badge) ───────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Category pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(HtsDeepNavy.copy(alpha = 0.6f))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = card.category.label.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 7.5.sp,
                            color = HtsGold,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                        ),
                        maxLines = 1,
                    )
                }

                // AP Cost Diamond
                if (card.category.apCost > 0) {
                    ApCostBadge(ap = card.category.apCost)
                }
            }

            // ── Card Center: Geometric Sigil & Name ────────────────────────
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ProceduralClassIcon(heroClass = card.heroClass, category = card.category)

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = card.name,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = HtsParchment,
                        textAlign = TextAlign.Center,
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // ── Card Footer: Effect / Roll Requirement ─────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(HtsDeepNavy.copy(alpha = 0.6f))
                    .padding(horizontal = 4.dp, vertical = 3.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (card.abilityRollRequirement != null) {
                    Text(
                        text = "Roll ${card.abilityRollRequirement}+",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = HtsGold,
                        ),
                    )
                } else {
                    Text(
                        text = if (card.category == CardCategory.HERO) "Passive" else card.category.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 8.sp,
                            color = HtsParchmentDim,
                        ),
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun ApCostBadge(ap: Int) {
    Box(
        modifier = Modifier
            .size(16.dp)
            .clip(CircleShape)
            .background(HtsGold)
            .border(1.dp, HtsDeepNavy, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "${ap}P",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                color = HtsDeepNavy,
            ),
        )
    }
}

@Composable
private fun ProceduralClassIcon(heroClass: HeroClass?, category: CardCategory) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(HtsDeepNavy.copy(alpha = 0.4f))
            .border(1.dp, HtsBorderSubtle, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(20.dp)) {
            val cx = size.width / 2f
            val cy = size.height / 2f

            when (heroClass) {
                HeroClass.FIGHTER -> {
                    // Crossed blades
                    drawLine(HtsGold, Offset(3f, size.height - 3f), Offset(size.width - 3f, 3f), strokeWidth = 2.dp.toPx())
                    drawLine(HtsGold, Offset(size.width - 3f, size.height - 3f), Offset(3f, 3f), strokeWidth = 2.dp.toPx())
                }
                HeroClass.WIZARD -> {
                    // Arcane star
                    val path = Path().apply {
                        moveTo(cx, 1f)
                        lineTo(size.width - 3f, cy)
                        lineTo(cx, size.height - 1f)
                        lineTo(3f, cy)
                        close()
                    }
                    drawPath(path, HtsGold, style = Stroke(width = 1.5.dp.toPx()))
                }
                HeroClass.GUARDIAN -> {
                    // Shield
                    val path = Path().apply {
                        moveTo(cx, 1f)
                        lineTo(size.width - 2f, 5f)
                        lineTo(size.width - 3f, size.height * 0.6f)
                        lineTo(cx, size.height - 1f)
                        lineTo(3f, size.height * 0.6f)
                        lineTo(2f, 5f)
                        close()
                    }
                    drawPath(path, HtsGold, style = Stroke(width = 1.5.dp.toPx()))
                }
                HeroClass.RANGER -> {
                    // Arrow
                    drawLine(HtsGold, Offset(cx, size.height - 2f), Offset(cx, 2f), strokeWidth = 2.dp.toPx())
                    drawLine(HtsGold, Offset(cx - 5f, 7f), Offset(cx, 2f), strokeWidth = 2.dp.toPx())
                    drawLine(HtsGold, Offset(cx + 5f, 7f), Offset(cx, 2f), strokeWidth = 2.dp.toPx())
                }
                HeroClass.BARD -> {
                    // Ring / lute circle
                    drawCircle(HtsGold, radius = 6.dp.toPx(), center = Offset(cx, cy), style = Stroke(width = 1.5.dp.toPx()))
                    drawCircle(HtsGold, radius = 2.dp.toPx(), center = Offset(cx, cy))
                }
                HeroClass.DRUID -> {
                    // Leaf / clover
                    drawCircle(HtsGold, radius = 4.dp.toPx(), center = Offset(cx - 3f, cy - 3f))
                    drawCircle(HtsGold, radius = 4.dp.toPx(), center = Offset(cx + 3f, cy - 3f))
                    drawCircle(HtsGold, radius = 4.dp.toPx(), center = Offset(cx, cy + 4f))
                }
                else -> {
                    // Category geometric sigil
                    val path = Path().apply {
                        moveTo(cx, 2f)
                        lineTo(size.width - 2f, size.height - 2f)
                        lineTo(2f, size.height - 2f)
                        close()
                    }
                    drawPath(path, HtsGold, style = Stroke(width = 1.5.dp.toPx()))
                }
            }
        }
    }
}
