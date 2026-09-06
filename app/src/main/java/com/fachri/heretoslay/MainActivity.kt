package com.fachri.heretoslay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.fachri.heretoslay.ui.navigation.HtsNavGraph
import com.fachri.heretoslay.ui.theme.HereToSlayTheme

class   MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HereToSlayTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    HtsNavGraph()
                }
            }
        }
    }
}
