package com.example.heretoslay.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.heretoslay.ui.theme.HtsBorder
import com.example.heretoslay.ui.theme.HtsDeepNavy
import com.example.heretoslay.ui.theme.HtsGold
import com.example.heretoslay.ui.theme.HtsParchment
import com.example.heretoslay.ui.theme.HtsParchmentDim
import com.example.heretoslay.ui.theme.HtsSilver

/**
 * Placeholder screen used for destinations not yet implemented (Part 2+).
 * Shows the route label and a back button so navigation is still testable.
 */
@Composable
fun PlaceholderScreen(
    label: String,
    onBack: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HtsDeepNavy),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Decorative top rule
            Box(
                modifier = Modifier
                    .widthIn(max = 320.dp)
                    .height(1.dp)
                    .background(HtsBorder)
            )
            Spacer(Modifier.height(20.dp))
            Text(
                text = "COMING SOON",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = HtsSilver,
                    letterSpacing = 3.sp,
                ),
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.headlineSmall.copy(color = HtsParchment),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "This screen will be implemented in Part 2+",
                style = MaterialTheme.typography.bodySmall.copy(color = HtsParchmentDim),
            )
            Spacer(Modifier.height(28.dp))
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(
                    containerColor = HtsGold,
                    contentColor   = HtsDeepNavy,
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .widthIn(max = 220.dp)
                    .height(48.dp),
            ) {
                Text(
                    text = "Go Back",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = HtsDeepNavy,
                        letterSpacing = 1.sp,
                    ),
                )
            }
            Spacer(Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .widthIn(max = 320.dp)
                    .height(1.dp)
                    .background(HtsBorder)
            )
        }
    }
}
