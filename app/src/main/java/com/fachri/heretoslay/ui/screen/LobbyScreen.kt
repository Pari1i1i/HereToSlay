package com.fachri.heretoslay.ui.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fachri.heretoslay.data.model.RoomStatus
import com.fachri.heretoslay.data.remote.FirestoreSchema
import com.fachri.heretoslay.ui.component.HouseRuleToggle
import com.fachri.heretoslay.ui.component.PlayerSlotCard
import com.fachri.heretoslay.ui.theme.HtsBorder
import com.fachri.heretoslay.ui.theme.HtsBorderSubtle
import com.fachri.heretoslay.ui.theme.HtsCardSurface
import com.fachri.heretoslay.ui.theme.HtsCrimson
import com.fachri.heretoslay.ui.theme.HtsCrimsonBright
import com.fachri.heretoslay.ui.theme.HtsDeepNavy
import com.fachri.heretoslay.ui.theme.HtsEmeraldBright
import com.fachri.heretoslay.ui.theme.HtsGold
import com.fachri.heretoslay.ui.theme.HtsParchment
import com.fachri.heretoslay.ui.theme.HtsParchmentDim
import com.fachri.heretoslay.ui.theme.HtsSilver
import com.fachri.heretoslay.ui.theme.HtsSilverDim
import com.fachri.heretoslay.ui.theme.HtsSurfaceNavy
import com.fachri.heretoslay.ui.viewmodel.LobbyViewModel

@Composable
fun LobbyScreen(
    roomCode: String,
    onGameStarted: (roomCode: String) -> Unit,
    onLeaveLobby: () -> Unit,
    viewModel: LobbyViewModel = viewModel { LobbyViewModel(roomCode) },
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showLeaveDialog by remember { mutableStateOf(false) }

    // Auto-navigate when game starts
    LaunchedEffect(uiState.roomState?.status) {
        if (uiState.roomState?.status == RoomStatus.PLAYING) {
            onGameStarted(roomCode)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(HtsDeepNavy, HtsSurfaceNavy),
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                )
            ),
    ) {
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = HtsGold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Menghubungkan ke room...", color = HtsParchmentDim, style = MaterialTheme.typography.bodyMedium)
                }
            }
            return
        }

        val room = uiState.roomState
        if (room == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = uiState.errorMessage ?: "Room tidak ditemukan.",
                        color = HtsCrimsonBright,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onLeaveLobby,
                        colors = ButtonDefaults.buttonColors(containerColor = HtsGold, contentColor = HtsDeepNavy),
                    ) {
                        Text("Kembali ke Menu")
                    }
                }
            }
            return
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // ── Top Bar ────────────────────────────────────────────────────────
            LobbyTopBar(
                roomCode = room.roomCode,
                playerCount = room.players.size,
                onCopyCode = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Room Code", room.roomCode))
                    Toast.makeText(context, "Kode room disalin ke clipboard!", Toast.LENGTH_SHORT).show()
                },
                onLeaveClick = { showLeaveDialog = true },
            )

            // Divider
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(HtsBorder))

            // ── Main Content Split ─────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
            ) {
                // ── Left: Players Grid (58%) ────────────────────────────────────
                Column(
                    modifier = Modifier
                        .weight(0.58f)
                        .fillMaxHeight(),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "PEMAIN DI ROOM (${room.players.size}/6)",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = HtsParchmentDim,
                                letterSpacing = 2.sp,
                            ),
                        )
                        Text(
                            text = "Minimal 2 pemain",
                            style = MaterialTheme.typography.bodySmall.copy(color = HtsSilverDim),
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(6) { index ->
                            val player = room.players.getOrNull(index)
                            PlayerSlotCard(
                                slotNumber = index + 1,
                                player = player,
                                isLocalPlayer = player?.uid == uiState.localUid,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Vertical Divider
                Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(HtsBorder))

                Spacer(modifier = Modifier.width(16.dp))

                // ── Right: House Rules & Actions (42%) ──────────────────────────
                Column(
                    modifier = Modifier
                        .weight(0.42f)
                        .fillMaxHeight(),
                ) {
                    // Scrollable House Rules Area
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "HOUSE RULES (CUSTOM)",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = HtsGold,
                                    letterSpacing = 2.sp,
                                ),
                            )
                            if (uiState.isHost) {
                                Text(
                                    text = "Host dapat toggle",
                                    style = MaterialTheme.typography.bodySmall.copy(color = HtsSilverDim, fontSize = 10.sp),
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        HouseRuleToggle(
                            title = "Hak Veto",
                            description = "Batal aksi via voting (1x per 5 turn global)",
                            enabled = room.houseRuleVetoEnabled,
                            isHost = uiState.isHost,
                            onToggle = {
                                viewModel.toggleHouseRule(
                                    FirestoreSchema.Fields.RULE_VETO,
                                    room.houseRuleVetoEnabled
                                )
                            },
                        )

                        Spacer(modifier = Modifier.height(5.dp))

                        HouseRuleToggle(
                            title = "Chaos Rule",
                            description = "Efek acak hand setelah bunuh Monster",
                            enabled = room.houseRuleChaosEnabled,
                            isHost = uiState.isHost,
                            onToggle = {
                                viewModel.toggleHouseRule(
                                    FirestoreSchema.Fields.RULE_CHAOS,
                                    room.houseRuleChaosEnabled
                                )
                            },
                        )

                        Spacer(modifier = Modifier.height(5.dp))

                        HouseRuleToggle(
                            title = "Bounty",
                            description = "Hadiah steal bagi yang kalahkan pemain terkuat",
                            enabled = room.houseRuleBountyEnabled,
                            isHost = uiState.isHost,
                            onToggle = {
                                viewModel.toggleHouseRule(
                                    FirestoreSchema.Fields.RULE_BOUNTY,
                                    room.houseRuleBountyEnabled
                                )
                            },
                        )

                        Spacer(modifier = Modifier.height(5.dp))

                        HouseRuleToggle(
                            title = "Curse & Raid",
                            description = "Raid party jika Bounty menang 3x challenge",
                            enabled = room.houseRuleCurseEnabled,
                            isHost = uiState.isHost,
                            onToggle = {
                                viewModel.toggleHouseRule(
                                    FirestoreSchema.Fields.RULE_CURSE,
                                    room.houseRuleCurseEnabled
                                )
                            },
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Turn Timer Selection
                        TurnTimerSelector(
                            currentSeconds = room.turnDurationSeconds,
                            isHost = uiState.isHost,
                            onSelectDuration = { seconds ->
                                viewModel.setTurnDuration(seconds)
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // ── Fixed Bottom Action Controls ────────────────────────────
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                    ) {
                        if (uiState.errorMessage != null) {
                            Text(
                                text = uiState.errorMessage ?: "",
                                style = MaterialTheme.typography.bodySmall.copy(color = HtsCrimsonBright),
                                modifier = Modifier.padding(bottom = 4.dp),
                                textAlign = TextAlign.Center,
                            )
                        }

                        if (uiState.isHost) {
                            val canStart = uiState.canStartGame
                            Button(
                                onClick = { viewModel.startGame { onGameStarted(roomCode) } },
                                enabled = canStart && !uiState.isStartingGame,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = HtsGold,
                                    contentColor = HtsDeepNavy,
                                    disabledContainerColor = HtsSilverDim,
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp),
                            ) {
                                if (uiState.isStartingGame) {
                                    CircularProgressIndicator(color = HtsDeepNavy, modifier = Modifier.size(20.dp))
                                } else {
                                    Text(
                                        text = if (room.players.size < 2) "Menunggu pemain lain (min. 2)..."
                                        else if (!canStart) "Menunggu semua pemain siap..."
                                        else "Mulai Permainan",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp,
                                        ),
                                    )
                                }
                            }
                        } else {
                            val isReady = uiState.isLocalPlayerReady
                            Button(
                                onClick = { viewModel.toggleReady() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isReady) HtsEmeraldBright else HtsGold,
                                    contentColor = HtsDeepNavy,
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp),
                            ) {
                                Text(
                                    text = if (isReady) "Kamu Sudah Siap (Klik Batal)" else "Saya Siap!",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                    ),
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Leave Confirmation Dialog
    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            containerColor = HtsCardSurface,
            title = {
                Text("Keluar dari Room?", color = HtsParchment, style = MaterialTheme.typography.titleLarge)
            },
            text = {
                Text(
                    if (uiState.isHost) "Kamu adalah Host. Jika kamu keluar, kepemimpinan room akan dialihkan atau room ditutup."
                    else "Kamu akan keluar dari lobby ini.",
                    color = HtsParchmentDim,
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLeaveDialog = false
                        viewModel.leaveRoom(onLeaveLobby)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HtsCrimson, contentColor = HtsParchment),
                ) {
                    Text("Keluar")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showLeaveDialog = false },
                    border = androidx.compose.foundation.BorderStroke(1.dp, HtsBorder),
                ) {
                    Text("Batal", color = HtsParchment)
                }
            },
        )
    }
}

@Composable
private fun TurnTimerSelector(
    currentSeconds: Int,
    isHost: Boolean,
    onSelectDuration: (Int) -> Unit,
) {
    val options = listOf(30, 60, 90, 120, 0) // 0 = unlimited

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(HtsCardSurface)
            .border(1.dp, HtsBorderSubtle, RoundedCornerShape(8.dp))
            .padding(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "WAKTU PER TURN",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = HtsGold,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                ),
            )
            Text(
                text = if (currentSeconds == 0) "Tanpa Batas" else "${currentSeconds}s",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = HtsParchment,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                ),
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            options.forEach { sec ->
                val isSelected = currentSeconds == sec
                val label = if (sec == 0) "∞" else "${sec}s"
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) HtsGold else HtsSurfaceNavy)
                        .border(
                            1.dp,
                            if (isSelected) HtsParchment else HtsBorderSubtle,
                            RoundedCornerShape(6.dp)
                        )
                        .clickable(enabled = isHost) { onSelectDuration(sec) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isSelected) HtsDeepNavy else HtsSilver,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                            fontSize = 10.sp,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun LobbyTopBar(
    roomCode: String,
    playerCount: Int,
    onCopyCode: () -> Unit,
    onLeaveClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        // Back / Leave button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onLeaveClick)
                .padding(horizontal = 10.dp, vertical = 6.dp),
        ) {
            Text(
                text = "← Keluar",
                style = MaterialTheme.typography.titleSmall.copy(color = HtsSilver),
            )
        }

        // Room Code Pill
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(30.dp))
                .background(HtsCardSurface)
                .border(1.dp, HtsGold.copy(alpha = 0.6f), RoundedCornerShape(30.dp))
                .clickable(onClick = onCopyCode)
                .padding(horizontal = 16.dp, vertical = 5.dp),
        ) {
            Text(
                text = "KODE ROOM: ",
                style = MaterialTheme.typography.labelSmall.copy(color = HtsParchmentDim, letterSpacing = 1.sp),
            )
            Text(
                text = roomCode,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = HtsGold,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                ),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(HtsGold.copy(alpha = 0.2f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "SALIN",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = HtsGold,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
            }
        }

        // Status badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(HtsSurfaceNavy)
                .border(1.dp, HtsBorderSubtle, RoundedCornerShape(20.dp))
                .padding(horizontal = 12.dp, vertical = 5.dp)
        ) {
            Text(
                text = "$playerCount / 6 Pemain",
                style = MaterialTheme.typography.labelMedium.copy(color = HtsSilver),
            )
        }
    }
}
