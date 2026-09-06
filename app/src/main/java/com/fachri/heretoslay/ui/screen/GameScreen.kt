package com.fachri.heretoslay.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fachri.heretoslay.data.model.Card
import com.fachri.heretoslay.data.model.HeroClass
import com.fachri.heretoslay.data.model.Player
import com.fachri.heretoslay.ui.component.DiceRollOverlay
import com.fachri.heretoslay.ui.component.OpponentsRow
import com.fachri.heretoslay.ui.component.ProceduralCardView
import com.fachri.heretoslay.ui.component.ProceduralMonsterCard
import com.fachri.heretoslay.ui.theme.CardShape
import com.fachri.heretoslay.ui.theme.HtsBorder
import com.fachri.heretoslay.ui.theme.HtsBorderSubtle
import com.fachri.heretoslay.ui.theme.HtsCardSurface
import com.fachri.heretoslay.ui.theme.HtsCrimson
import com.fachri.heretoslay.ui.theme.HtsCrimsonBright
import com.fachri.heretoslay.ui.theme.HtsDeepNavy
import com.fachri.heretoslay.ui.theme.HtsEmerald
import com.fachri.heretoslay.ui.theme.HtsEmeraldBright
import com.fachri.heretoslay.ui.theme.HtsGold
import com.fachri.heretoslay.ui.theme.HtsGoldGlow
import com.fachri.heretoslay.ui.theme.HtsGoldMuted
import com.fachri.heretoslay.ui.theme.HtsParchment
import com.fachri.heretoslay.ui.theme.HtsParchmentDim
import com.fachri.heretoslay.ui.theme.HtsSilver
import com.fachri.heretoslay.ui.theme.HtsSilverDim
import com.fachri.heretoslay.ui.theme.HtsSurfaceNavy
import com.fachri.heretoslay.ui.viewmodel.GameViewModel

@Composable
fun GameScreen(
    roomCode: String,
    onNavigateHome: () -> Unit,
    viewModel: GameViewModel = viewModel { GameViewModel(roomCode) },
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(HtsSurfaceNavy, HtsDeepNavy),
                    radius = 1200f,
                )
            ),
    ) {
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = HtsGold)
            }
            return
        }

        val room = uiState.roomState ?: return
        val myPlayer = uiState.myPlayer ?: return
        val isMyTurn = uiState.isMyTurn

        val activeMonsters = viewModel.resolveMonsters(room.activeMonsterIds)
        val myHandCards = viewModel.resolveCards(myPlayer.handCardIds)
        val myPartyLeader = myPlayer.partyLeaderId?.let { viewModel.resolveCard(it) }
        val myPartyHeroes = viewModel.resolveCards(myPlayer.partyHeroIds)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // ── TOP TIER: Opponents Row & Game Status ───────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // Turn Indicator Pill
                TurnIndicatorBadge(isMyTurn = isMyTurn, currentTurnPlayer = room.currentPlayer?.name ?: "")

                Spacer(modifier = Modifier.width(10.dp))

                // Opponents List
                Box(modifier = Modifier.weight(1f)) {
                    OpponentsRow(
                        opponents = uiState.opponents,
                        currentTurnUid = room.currentPlayer?.uid ?: "",
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Room Code indicator
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(HtsCardSurface)
                        .border(1.dp, HtsBorderSubtle, RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = roomCode,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = HtsGoldMuted,
                            letterSpacing = 1.sp,
                        ),
                    )
                }
            }

            // ── CENTER TIER: Deck, 3 Monsters, Action Activity Feed ─────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(184.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // Deck and Discard
                DeckAndDiscardArea(
                    deckCount = room.remainingDeckIds.size,
                    discardCount = room.discardPileIds.size,
                    canDraw = isMyTurn && uiState.actionPointsRemaining >= 1,
                    onDrawClick = { viewModel.drawCard() },
                )

                Spacer(modifier = Modifier.width(12.dp))

                // 3 Active Monsters
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    activeMonsters.forEach { monster ->
                        ProceduralMonsterCard(
                            monster = monster,
                            canAttack = isMyTurn && uiState.actionPointsRemaining >= 2,
                            onAttackClick = { viewModel.attackMonster(monster.id) },
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Objective Tracker Panel (Win conditions)
                ObjectiveTrackerPanel(
                    monstersKilled = myPlayer.monstersDefeated.size,
                    classesCount = myPlayer.partyHeroClasses.size,
                )
            }

            // ── BOTTOM TIER: My Party + AP Dots + Hand Cards Carousel ──────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(154.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // My Party (Leader + Hero slots)
                MyPartySummary(
                    partyLeader = myPartyLeader,
                    heroes = myPartyHeroes,
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Action Point Control Box & End Turn
                ActionPointControlBox(
                    apRemaining = uiState.actionPointsRemaining,
                    isMyTurn = isMyTurn,
                    hasCardSelected = uiState.selectedCardId != null,
                    onPlayCardClick = { viewModel.playSelectedCard() },
                    onEndTurnClick = { viewModel.endTurn() },
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Hand Cards Carousel
                Box(modifier = Modifier.weight(1f)) {
                    if (myHandCards.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "Tanganmu kosong. Draw kartu (1 AP) dari deck!",
                                style = MaterialTheme.typography.bodySmall.copy(color = HtsSilverDim),
                            )
                        }
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            items(myHandCards) { card ->
                                ProceduralCardView(
                                    card = card,
                                    isSelected = uiState.selectedCardId == card.id,
                                    isPlayable = isMyTurn && uiState.actionPointsRemaining >= card.category.apCost,
                                    onClick = {
                                        viewModel.selectCard(card.id)
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }

        // Dice Roll Overlay
        uiState.attackResult?.let { result ->
            DiceRollOverlay(
                attackResult = result,
                onDismiss = { viewModel.dismissAttackResult() },
            )
        }

        // Game Over Banner
        if (uiState.isGameOver) {
            val winner = room.players.firstOrNull { it.uid == room.winnerUid }
            val isWinnerMe = room.winnerUid == uiState.localUid
            AlertDialog(
                onDismissRequest = {},
                containerColor = HtsCardSurface,
                title = {
                    Text(
                        text = if (isWinnerMe) "VICTORY! KAMU MENANG!" else "GAME OVER",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = if (isWinnerMe) HtsGold else HtsCrimsonBright,
                            fontWeight = FontWeight.Black,
                        ),
                    )
                },
                text = {
                    Text(
                        text = "Pemenang: ${winner?.name ?: "Pemain lain"} berhasil menyelesaikan objektif kemenangan!",
                        color = HtsParchment,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                },
                confirmButton = {
                    Button(
                        onClick = onNavigateHome,
                        colors = ButtonDefaults.buttonColors(containerColor = HtsGold, contentColor = HtsDeepNavy),
                    ) {
                        Text("Kembali ke Menu Utama")
                    }
                },
            )
        }

        // Error snackbar/banner
        if (uiState.errorMessage != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(HtsCrimson.copy(alpha = 0.9f))
                    .clickable { viewModel.clearError() }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = uiState.errorMessage ?: "",
                    color = HtsParchment,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

// ─── Sub-components ──────────────────────────────────────────────────────────

@Composable
private fun TurnIndicatorBadge(isMyTurn: Boolean, currentTurnPlayer: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "turnPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = Modifier
            .width(130.dp)
            .height(44.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(if (isMyTurn) HtsGold.copy(alpha = 0.2f) else HtsSurfaceNavy)
            .border(
                1.5.dp,
                if (isMyTurn) HtsGold.copy(alpha = pulseAlpha) else HtsBorderSubtle,
                RoundedCornerShape(22.dp)
            )
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (isMyTurn) "GILIRANMU!" else "GILIRAN",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isMyTurn) HtsGold else HtsSilverDim,
                    letterSpacing = 1.sp,
                ),
            )
            Text(
                text = if (isMyTurn) "Ambil Aksimu" else currentTurnPlayer,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isMyTurn) HtsParchment else HtsSilver,
                ),
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun DeckAndDiscardArea(
    deckCount: Int,
    discardCount: Int,
    canDraw: Boolean,
    onDrawClick: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        // Draw Deck Card
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(112.dp)
                .clip(CardShape)
                .background(Brush.verticalGradient(listOf(HtsSurfaceNavy, HtsDeepNavy)))
                .border(1.dp, if (canDraw) HtsGold else HtsBorderSubtle, CardShape)
                .clickable(enabled = canDraw, onClick = onDrawClick)
                .padding(6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "DECK",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = HtsGold,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                    ),
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(HtsDeepNavy)
                        .border(1.dp, HtsGoldMuted, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "$deckCount",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = HtsParchment,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                        ),
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (canDraw) "Draw (1 AP)" else "Kosong",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 8.sp,
                        color = if (canDraw) HtsGold else HtsSilverDim,
                    ),
                )
            }
        }

        // Discard Pile
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(112.dp)
                .clip(CardShape)
                .background(HtsDeepNavy.copy(alpha = 0.5f))
                .border(1.dp, HtsBorderSubtle, CardShape)
                .padding(6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "DISCARD",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = HtsSilverDim,
                        letterSpacing = 0.5.sp,
                    ),
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "$discardCount",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = HtsSilver,
                        fontWeight = FontWeight.Bold,
                    ),
                )
            }
        }
    }
}

@Composable
private fun ObjectiveTrackerPanel(monstersKilled: Int, classesCount: Int) {
    Box(
        modifier = Modifier
            .width(130.dp)
            .height(112.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(HtsCardSurface)
            .border(1.dp, HtsBorder, RoundedCornerShape(10.dp))
            .padding(8.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "TARGET MENANG",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 8.5.sp,
                    color = HtsGold,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                ),
            )

            // Objective 1: Slay 3 Monsters
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("3 Monster:", style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp, color = HtsParchmentDim))
                    Text("$monstersKilled / 3", style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp, color = if (monstersKilled >= 3) HtsEmeraldBright else HtsGold, fontWeight = FontWeight.Bold))
                }
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(HtsSurfaceNavy)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = (monstersKilled / 3f).coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .background(HtsCrimsonBright)
                    )
                }
            }

            // Objective 2: 6 Hero Classes
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("6 Kelas Hero:", style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp, color = HtsParchmentDim))
                    Text("$classesCount / 6", style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp, color = if (classesCount >= 6) HtsEmeraldBright else HtsGold, fontWeight = FontWeight.Bold))
                }
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(HtsSurfaceNavy)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = (classesCount / 6f).coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .background(HtsEmeraldBright)
                    )
                }
            }
        }
    }
}

@Composable
private fun MyPartySummary(
    partyLeader: Card?,
    heroes: List<Card>,
) {
    Box(
        modifier = Modifier
            .width(180.dp)
            .fillMaxHeight()
            .clip(RoundedCornerShape(10.dp))
            .background(HtsCardSurface)
            .border(1.dp, HtsBorder, RoundedCornerShape(10.dp))
            .padding(8.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "PARTY KAMU",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 8.5.sp,
                    color = HtsGold,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                ),
            )

            // Leader row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(HtsGold),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "★",
                        color = HtsDeepNavy,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = partyLeader?.name ?: "Party Leader",
                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = HtsParchment),
                        maxLines = 1,
                    )
                    Text(
                        text = partyLeader?.heroClass?.label ?: "Class",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 8.sp, color = HtsGoldMuted),
                    )
                }
            }

            // Heroes in party
            Column {
                Text(
                    text = "Heroes (${heroes.size}/6):",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, color = HtsParchmentDim),
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    (0 until 5).forEach { idx ->
                        val hero = heroes.getOrNull(idx)
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (hero != null) HtsEmerald else HtsSurfaceNavy)
                                .border(0.5.dp, if (hero != null) HtsEmeraldBright else HtsBorderSubtle, RoundedCornerShape(3.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (hero != null) {
                                Text(
                                    text = hero.heroClass?.label?.firstOrNull()?.toString() ?: "H",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HtsParchment,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionPointControlBox(
    apRemaining: Int,
    isMyTurn: Boolean,
    hasCardSelected: Boolean,
    onPlayCardClick: () -> Unit,
    onEndTurnClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(136.dp)
            .fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        // Action Points Indicator
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(HtsCardSurface)
                .border(1.dp, if (isMyTurn) HtsGold else HtsBorderSubtle, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "AP",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Black,
                        color = if (isMyTurn) HtsGold else HtsSilverDim,
                        fontSize = 12.sp,
                    ),
                )
                // 3 Glowing Dots
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    (1..3).forEach { index ->
                        val isFilled = index <= apRemaining && isMyTurn
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(if (isFilled) HtsGold else HtsSurfaceNavy)
                                .border(1.dp, if (isFilled) HtsParchment else HtsBorderSubtle, CircleShape)
                        )
                    }
                }
            }
        }

        // Play Selected Card Button
        Button(
            onClick = onPlayCardClick,
            enabled = isMyTurn && hasCardSelected,
            colors = ButtonDefaults.buttonColors(
                containerColor = HtsGold,
                contentColor = HtsDeepNavy,
                disabledContainerColor = HtsSurfaceNavy,
                disabledContentColor = HtsSilverDim,
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp),
        ) {
            Text(
                text = "Mainkan",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
            )
        }

        // End Turn Button
        Button(
            onClick = onEndTurnClick,
            enabled = isMyTurn,
            colors = ButtonDefaults.buttonColors(
                containerColor = HtsCrimson,
                contentColor = HtsParchment,
                disabledContainerColor = HtsSurfaceNavy,
                disabledContentColor = HtsSilverDim,
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp),
        ) {
            Text(
                text = "Akhiri Giliran",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
            )
        }
    }
}
