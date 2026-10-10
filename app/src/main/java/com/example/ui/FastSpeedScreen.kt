package com.example.ui

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.network.TestPhase
import java.util.Locale

@Composable
fun FastSpeedScreen(
    viewModel: SpeedTestViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val history by viewModel.historyRecords.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showHistorySheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var languageMenuExpanded by remember { mutableStateOf(false) }

    // Fast.com screenshot is crisp pure white
    val bgColor = if (state.isDarkMode) Color(0xFF111111) else Color(0xFFFFFFFF)
    val textPrimary = if (state.isDarkMode) Color.White else Color(0xFF222222)
    val textSecondary = if (state.isDarkMode) Color(0xFF9E9E9E) else Color(0xFF555555)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = bgColor,
        contentWindowInsets = WindowInsets.statusBars
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Navigation Bar (English (US) ⌄ & Privacy) exactly as in screenshot
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left utility icons: History & Settings (minimal)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = { showHistorySheet = true },
                        modifier = Modifier.size(36.dp).testTag("history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = SpeedTestStrings.history(state.language),
                            tint = if (state.isDarkMode) Color(0xFF888888) else Color(0xFFAAAAAA),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { showSettingsSheet = true },
                        modifier = Modifier.size(36.dp).testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = SpeedTestStrings.settings(state.language),
                            tint = if (state.isDarkMode) Color(0xFF888888) else Color(0xFFAAAAAA),
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.toggleDarkMode() },
                        modifier = Modifier.size(36.dp).testTag("theme_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (state.isDarkMode) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                            contentDescription = "Toggle Dark Mode",
                            tint = if (state.isDarkMode) Color(0xFF888888) else Color(0xFFAAAAAA),
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }

                // Top Right: English (US) ⌄  and  Privacy (as in screenshot)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Language Dropdown
                    Box {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { languageMenuExpanded = true }
                                .padding(vertical = 4.dp, horizontal = 2.dp)
                                .testTag("language_toggle_button")
                        ) {
                            Text(
                                text = SpeedTestStrings.langName(state.language),
                                fontSize = 13.sp,
                                fontFamily = FontFamily.SansSerif,
                                color = textSecondary,
                                fontWeight = FontWeight.Normal
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Select Language",
                                tint = textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = languageMenuExpanded,
                            onDismissRequest = { languageMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("English (US)") },
                                onClick = {
                                    if (state.language != AppLanguage.EN) viewModel.toggleLanguage()
                                    languageMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("বাংলা (BD)") },
                                onClick = {
                                    if (state.language != AppLanguage.BN) viewModel.toggleLanguage()
                                    languageMenuExpanded = false
                                }
                            )
                        }
                    }

                    // Privacy Link
                    Text(
                        text = SpeedTestStrings.privacy(state.language),
                        fontSize = 13.sp,
                        fontFamily = FontFamily.SansSerif,
                        color = textSecondary,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier
                            .clickable { showPrivacyDialog = true }
                            .padding(vertical = 4.dp)
                            .testTag("privacy_button")
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Main Content Area constrained for clean desktop/tablet & phone display
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 620.dp)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Center SPEEDNEXT Logo
                SpeedNextLogo(
                    isDark = state.isDarkMode,
                    modifier = Modifier.testTag("speednext_logo")
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Title: "Your Internet speed is"
                Text(
                    text = SpeedTestStrings.title(state.language),
                    fontSize = 22.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Normal,
                    color = textPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Giant Speed Display: e.g. 51 Mbps with green reload circle
                SpeedDisplayHero(
                    speedMbps = state.currentDisplaySpeed,
                    isDimmed = state.isShowingDimmedPrevious,
                    phase = state.phase,
                    isDark = state.isDarkMode,
                    onRestart = { viewModel.handleHeroButtonClick() }
                )

                Spacer(modifier = Modifier.height(24.dp))

                val isTestActive = state.phase == TestPhase.CONNECTING || state.phase == TestPhase.TESTING_DOWNLOAD
                val shouldShowDetails = !isTestActive && state.currentDisplaySpeed > 0

                // Expanded Information Section (latency, upload, ISP, server)
                // Automatically reveals with smooth animation as soon as test finishes/pauses,
                // and automatically hides when testing is in progress!
                AnimatedVisibility(
                    visible = shouldShowDetails,
                    enter = fadeIn(tween(400)) + expandVertically(tween(450)),
                    exit = fadeOut(tween(250)) + shrinkVertically(tween(300))
                ) {
                    DetailedInfoSection(
                        state = state,
                        isDark = state.isDarkMode,
                        onTestAgain = { viewModel.startFullTest() },
                        onShare = {
                            val shareText = buildString {
                                append("⚡ SPEEDNEXT Speed Test Result\n")
                                append("📥 Download: ${String.format(Locale.US, "%.1f", state.downloadSpeed)} Mbps\n")
                                if (state.uploadSpeed > 0) {
                                    append("📤 Upload: ${String.format(Locale.US, "%.1f", state.uploadSpeed)} Mbps\n")
                                }
                                append("⏱ Ping: ${state.unloadedLatencyMs} ms\n")
                                append("🌐 ISP: ${state.networkMeta.ispName}\n")
                                append("📍 Location: ${state.networkMeta.clientLocation}\n")
                                append("⚡ Tested with SPEEDNEXT")
                            }
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Speed Result"))
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f, fill = false))
            Spacer(modifier = Modifier.height(48.dp))

            // Bottom Right "POWERED BY TUHINEXT" Badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                PoweredByTuhiNextBadge(isDark = state.isDarkMode)
            }
        }

        // Help (?) Dialog
        if (showHelpDialog) {
            AlertDialog(
                onDismissRequest = { showHelpDialog = false },
                title = {
                    Text(
                        text = SpeedTestStrings.aboutTitle(state.language),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Text(
                        text = SpeedTestStrings.aboutContent(state.language),
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = textSecondary
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showHelpDialog = false }) {
                        Text("OK", color = Color(0xFFE50914), fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        // Privacy Dialog
        if (showPrivacyDialog) {
            AlertDialog(
                onDismissRequest = { showPrivacyDialog = false },
                title = {
                    Text(
                        text = SpeedTestStrings.privacyTitle(state.language),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Text(
                        text = SpeedTestStrings.privacyContent(state.language),
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = textSecondary
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showPrivacyDialog = false }) {
                        Text("OK", color = Color(0xFFE50914), fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        // History Bottom Sheet
        if (showHistorySheet) {
            HistorySheet(
                records = history,
                language = state.language,
                isDark = state.isDarkMode,
                onDismiss = { showHistorySheet = false },
                onDeleteRecord = { record -> viewModel.deleteHistoryRecord(record) },
                onClearAll = { viewModel.clearAllHistory() }
            )
        }

        // Settings Bottom Sheet
        if (showSettingsSheet) {
            SettingsSheet(
                currentStreams = state.parallelStreams,
                currentDurationSec = state.testDurationSec,
                language = state.language,
                isDark = state.isDarkMode,
                onDismiss = { showSettingsSheet = false },
                onUpdate = { streams, dur ->
                    viewModel.updateSettings(streams, dur)
                    showSettingsSheet = false
                }
            )
        }
    }
}
