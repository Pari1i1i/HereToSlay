package com.fachri.heretoslay.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fachri.heretoslay.data.model.Player
import com.fachri.heretoslay.ui.theme.HtsBounty
import com.fachri.heretoslay.ui.theme.HtsCardSurface
import com.fachri.heretoslay.ui.theme.HtsCrimsonBright
import com.fachri.heretoslay.ui.theme.HtsDeepNavy
import com.fachri.heretoslay.ui.theme.HtsEmeraldBright
import com.fachri.heretoslay.ui.theme.HtsGold
import com.fachri.heretoslay.ui.theme.HtsGoldMuted
import com.fachri.heretoslay.ui.theme.HtsParchment
import com.fachri.heretoslay.ui.theme.HtsParchmentDim
import com.fachri.heretoslay.ui.theme.HtsSilver
import com.fachri.heretoslay.ui.theme.HtsSilverDim
import com.fachri.heretoslay.ui.theme.HtsSurfaceNavy

@Composable
fun OpponentsRow(
    opponents: List<Player>,
    currentTurnUid: String,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(opponents) { opponent ->
            val isCurrentTurn = opponent.uid == currentTurnUid
            OpponentCard(player = opponent, isCurrentTurn = isCurrentTurn)
        }
    }
}

@Composable
private fun OpponentCard(
    player: Player,
    isCurrentTurn: Boolean,
) {
    Box(
        modifier = Modifier
            .width(170.dp)
            .height(58.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isCurrentTurn) HtsCardSurface else HtsDeepNavy.copy(alpha = 0.6f))
            .border(
                1.dp,
                if (isCurrentTurn) HtsGold else HtsSilverDim.copy(alpha = 0.3f),
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 6.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            // Mini Avatar
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (isCurrentTurn) HtsGold else HtsSurfaceNavy),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = player.name.firstOrNull()?.uppercaseChar()?.toString() ?: "P",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isCurrentTurn) HtsDeepNavy else HtsParchment,
                        fontSize = 13.sp,
                    ),
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = player.name,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = HtsParchment,
                        ),
                        maxLines = 1,
                    )
                    if (player.isBounty) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(HtsBounty)
                                .padding(horizontal = 3.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "BOUNTY",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Black,
                                    color = HtsDeepNavy,
                                ),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Stats: Hand size, Party size, Monsters killed
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${player.handSize} Kartu",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 9.sp,
                            color = HtsSilver,
                        ),
                    )
                    Text(text = "•", color = HtsSilverDim, fontSize = 8.sp)
                    Text(
                        text = "${player.partyHeroIds.size}/6 Hero",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 9.sp,
                            color = HtsGoldMuted,
                        ),
                    )
                    Text(text = "•", color = HtsSilverDim, fontSize = 8.sp)
                    Text(
                        text = "${player.monstersDefeated.size}/3 ☠",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 9.sp,
                            color = if (player.monstersDefeated.isNotEmpty()) HtsCrimsonBright else HtsSilverDim,
                        ),
                    )
                }
            }
        }
    }
}
