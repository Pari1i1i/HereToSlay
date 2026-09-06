package com.fachri.heretoslay.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fachri.heretoslay.ui.theme.HtsBorder
import com.fachri.heretoslay.ui.theme.HtsBorderSubtle
import com.fachri.heretoslay.ui.theme.HtsCardSurface
import com.fachri.heretoslay.ui.theme.HtsCrimson
import com.fachri.heretoslay.ui.theme.HtsDeepNavy
import com.fachri.heretoslay.ui.theme.HtsGold
import com.fachri.heretoslay.ui.theme.HtsGoldGlow
import com.fachri.heretoslay.ui.theme.HtsGoldMuted
import com.fachri.heretoslay.ui.theme.HtsParchment
import com.fachri.heretoslay.ui.theme.HtsParchmentDim
import com.fachri.heretoslay.ui.theme.HtsSilver
import com.fachri.heretoslay.ui.theme.HtsSilverDim
import com.fachri.heretoslay.ui.theme.HtsSurfaceNavy
import kotlinx.coroutines.delay

/**
 * Home screen — landscape layout split into two halves:
 *   Left  — branding / sigil wordmark
 *   Right — action panel (player name input + Create Room / Join Room)
 *
 * The "Join Room" flow expands an inline panel below the main actions
 * rather than navigating away, keeping the landscape space fully utilized.
 *
 * For Part 1, room code generation is local (random string).
 * In Part 2, it will be replaced with Firestore calls.
 */
@Composable
fun HomeScreen(
    onNavigateToLobby: (roomCode: String) -> Unit,
) {
    var playerName by rememberSaveable { mutableStateOf("") }
    var joinCode   by rememberSaveable { mutableStateOf("") }
    var showJoin   by remember { mutableStateOf(false) }
    var nameError  by remember { mutableStateOf(false) }
    var joinError  by remember { mutableStateOf<String?>(null) }

    // Entry animation
    val panelReveal = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(80)
        panelReveal.animateTo(1f, tween(500, easing = EaseOutCubic))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(HtsDeepNavy, HtsSurfaceNavy),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                )
            ),
    ) {
        Row(modifier = Modifier.fillMaxSize()) {

            // ── Left panel — Branding ──────────────────────────────────────────
            Box(
                modifier = Modifier
                    .weight(0.42f)
                    .fillMaxHeight()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(HtsGoldGlow, HtsDeepNavy),
                            radius = 600f,
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .graphicsLayer {
                            alpha = panelReveal.value
                            translationY = (1f - panelReveal.value) * 30f
                        }
                ) {
                    HtsSmallSigil()
                    Spacer(Modifier.height(20.dp))
                    Text(
                        text = "HERE TO SLAY",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            color = HtsGold,
                            letterSpacing = 4.sp,
                        ),
                    )
                    Text(
                        text = "CUSTOM EDITION",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = HtsParchmentDim,
                            letterSpacing = 3.sp,
                        ),
                        modifier = Modifier.padding(top = 6.dp),
                    )
                    Spacer(Modifier.height(16.dp))
                    // Decorative divider
                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .height(1.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(HtsDeepNavy, HtsGoldMuted, HtsDeepNavy)
                                )
                            )
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "2 – 6 Players  •  Online",
                        style = MaterialTheme.typography.bodySmall.copy(color = HtsSilver),
                    )
                }
            }

            // Vertical divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(HtsBorder)
            )

            // ── Right panel — Actions ──────────────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(0.58f)
                    .fillMaxHeight()
                    .padding(horizontal = 40.dp, vertical = 32.dp)
                    .graphicsLayer {
                        alpha = panelReveal.value
                        translationX = (1f - panelReveal.value) * 40f
                    },
                verticalArrangement = Arrangement.Center,
            ) {

                Text(
                    text = "Enter your name",
                    style = MaterialTheme.typography.titleMedium.copy(color = HtsParchmentDim),
                )
                Spacer(Modifier.height(8.dp))

                // ── Player name input ────────────────────────────────────────
                OutlinedTextField(
                    value = playerName,
                    onValueChange = {
                        playerName = it.take(20)
                        nameError = false
                    },
                    placeholder = {
                        Text(
                            "Your name",
                            style = MaterialTheme.typography.bodyMedium.copy(color = HtsSilverDim)
                        )
                    },
                    isError = nameError,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = {}),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor      = HtsGold,
                        unfocusedBorderColor    = HtsBorder,
                        errorBorderColor        = HtsCrimson,
                        focusedTextColor        = HtsParchment,
                        unfocusedTextColor      = HtsParchment,
                        cursorColor             = HtsGold,
                        focusedContainerColor   = HtsCardSurface,
                        unfocusedContainerColor = HtsCardSurface,
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .widthIn(max = 420.dp)
                        .fillMaxWidth(),
                )
                if (nameError) {
                    Text(
                        text = "Please enter your name first",
                        style = MaterialTheme.typography.labelSmall.copy(color = HtsCrimson),
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp),
                    )
                }

                Spacer(Modifier.height(28.dp))

                // ── Create Room button ───────────────────────────────────────
                Button(
                    onClick = {
                        if (playerName.isBlank()) { nameError = true; return@Button }
                        val code = generateRoomCode()
                        onNavigateToLobby(code)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HtsGold,
                        contentColor   = HtsDeepNavy,
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .widthIn(max = 420.dp)
                        .fillMaxWidth()
                        .height(52.dp),
                ) {
                    Text(
                        text = "Create Room",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = HtsDeepNavy,
                            letterSpacing = 1.sp,
                        ),
                    )
                }

                Spacer(Modifier.height(12.dp))

                // ── Join Room button / expand panel ──────────────────────────
                OutlinedButton(
                    onClick = {
                        if (playerName.isBlank()) { nameError = true; return@OutlinedButton }
                        showJoin = !showJoin
                        joinError = null
                    },
                    border = androidx.compose.foundation.BorderStroke(1.dp, HtsBorder),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = HtsParchment,
                    ),
                    modifier = Modifier
                        .widthIn(max = 420.dp)
                        .fillMaxWidth()
                        .height(52.dp),
                ) {
                    Text(
                        text = if (showJoin) "Cancel" else "Join Room",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = HtsParchment,
                            letterSpacing = 1.sp,
                        ),
                    )
                }

                // ── Inline join panel ────────────────────────────────────────
                AnimatedVisibility(
                    visible = showJoin,
                    enter = fadeIn(tween(250)) + slideInVertically(
                        tween(300, easing = EaseOutBack),
                        initialOffsetY = { -it / 2 }
                    ),
                ) {
                    Column(modifier = Modifier.padding(top = 16.dp)) {
                        Text(
                            text = "Enter room code",
                            style = MaterialTheme.typography.labelMedium.copy(color = HtsParchmentDim),
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .widthIn(max = 420.dp)
                                .fillMaxWidth(),
                        ) {
                            OutlinedTextField(
                                value = joinCode,
                                onValueChange = {
                                    joinCode = it.uppercase().take(6)
                                        .filter { c -> c.isLetterOrDigit() }
                                    joinError = null
                                },
                                placeholder = {
                                    Text(
                                        "XXXXXX",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = HtsSilverDim
                                        )
                                    )
                                },
                                isError = joinError != null,
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Ascii,
                                    imeAction = ImeAction.Go,
                                    capitalization = KeyboardCapitalization.Characters,
                                ),
                                keyboardActions = KeyboardActions(onGo = {
                                    if (joinCode.length == 6) onNavigateToLobby(joinCode)
                                    else joinError = "Code must be 6 characters"
                                }),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor      = HtsGold,
                                    unfocusedBorderColor    = HtsBorderSubtle,
                                    errorBorderColor        = HtsCrimson,
                                    focusedTextColor        = HtsParchment,
                                    unfocusedTextColor      = HtsParchment,
                                    cursorColor             = HtsGold,
                                    focusedContainerColor   = HtsCardSurface,
                                    unfocusedContainerColor = HtsCardSurface,
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f),
                            )
                            Button(
                                onClick = {
                                    if (joinCode.length == 6) onNavigateToLobby(joinCode)
                                    else joinError = "Code must be 6 characters"
                                },
                                enabled = joinCode.length == 6,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = HtsGold,
                                    contentColor   = HtsDeepNavy,
                                    disabledContainerColor = HtsSilverDim,
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .height(56.dp)
                                    .width(88.dp),
                            ) {
                                Text(
                                    "Join",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        color = if (joinCode.length == 6) HtsDeepNavy
                                                else HtsSilver
                                    ),
                                )
                            }
                        }
                        if (joinError != null) {
                            Text(
                                text = joinError!!,
                                style = MaterialTheme.typography.labelSmall.copy(color = HtsCrimson),
                                modifier = Modifier.padding(top = 4.dp, start = 4.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Helpers ─────────────────────────────────────────────────────────────────

/** Generates a random 6-character alphanumeric room code. In Part 2, replaced by Firestore. */
private fun generateRoomCode(): String {
    val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    return (1..6).map { chars.random() }.joinToString("")
}

/** Small procedural sigil for the home screen branding panel */
@Composable
private fun HtsSmallSigil() {
    Canvas(
        modifier = Modifier
            .width(80.dp)
            .height(80.dp),
    ) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r  = size.minDimension / 2f * 0.85f

        // Outer ring
        drawCircle(
            color = HtsGoldMuted,
            radius = r,
            center = Offset(cx, cy),
            style = Stroke(width = 1.5.dp.toPx()),
        )
        // Hexagon
        val hexPath = Path()
        (0 until 6).forEach { i ->
            val angle = Math.toRadians((-90.0 + i * 60.0))
            val x = cx + r * 0.72f * Math.cos(angle).toFloat()
            val y = cy + r * 0.72f * Math.sin(angle).toFloat()
            if (i == 0) hexPath.moveTo(x, y) else hexPath.lineTo(x, y)
        }
        hexPath.close()
        drawPath(hexPath, HtsGold, style = Stroke(width = 1.5.dp.toPx()))

        // Center dot
        drawCircle(HtsGold, radius = 4.dp.toPx(), center = Offset(cx, cy))
    }
}
