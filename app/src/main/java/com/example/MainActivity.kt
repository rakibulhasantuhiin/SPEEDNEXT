package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.FastSpeedScreen
import com.example.ui.SpeedTestViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: SpeedTestViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val systemDark = isSystemInDarkTheme()
            val state by viewModel.uiState.collectAsStateWithLifecycle()

            // Initialize dark mode preference with system default on first start if needed
            LaunchedEffect(systemDark) {
                if (systemDark && !state.isDarkMode) {
                    viewModel.toggleDarkMode()
                }
            }

            MyApplicationTheme(darkTheme = state.isDarkMode, dynamicColor = false) {
                FastSpeedScreen(viewModel = viewModel)
            }
        }
    }
}
