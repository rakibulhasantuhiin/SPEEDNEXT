package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SpeedTestRecord
import com.example.data.network.TestPhase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Distinctive SPEEDNEXT Logo:
 * Features a custom aerodynamic speed-trail into forward supersonic dual chevrons (>>),
 * and the iconic "SPEED" + "[ NEXT ]" branded badge wordmark.
 */
@Composable
fun SpeedNextLogo(
    modifier: Modifier = Modifier,
    isDark: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // Distinctive SPEEDNEXT Speed & Velocity Emblem
        Canvas(modifier = Modifier.size(width = 86.dp, height = 48.dp)) {
            val w = size.width
            val h = size.height
            val primaryColor = Color(0xFFE50914)
            val secondaryColor = Color(0xFFFF3B30)
            val accentOrange = Color(0xFFFF7A00)
            val darkColor = if (isDark) Color.White else Color(0xFF1E1E1E)

            // Speed lines on the left representing rapid acceleration (SPEED)
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, primaryColor.copy(alpha = 0.5f), primaryColor),
                    startX = 0f,
                    endX = w * 0.36f
                ),
                start = Offset(4.dp.toPx(), h * 0.28f),
                end = Offset(w * 0.36f, h * 0.28f),
                strokeWidth = 3.5.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, primaryColor.copy(alpha = 0.6f), accentOrange),
                    startX = 0f,
                    endX = w * 0.32f
                ),
                start = Offset(0f, h * 0.50f),
                end = Offset(w * 0.32f, h * 0.50f),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, primaryColor.copy(alpha = 0.4f), primaryColor),
                    startX = 0f,
                    endX = w * 0.40f
                ),
                start = Offset(8.dp.toPx(), h * 0.72f),
                end = Offset(w * 0.40f, h * 0.72f),
                strokeWidth = 3.5.dp.toPx(),
                cap = StrokeCap.Round
            )

            // First Forward Chevron (Speed Vector)
            val chevron1Path = Path().apply {
                moveTo(w * 0.38f, h * 0.12f)
                lineTo(w * 0.56f, h * 0.50f)
                lineTo(w * 0.38f, h * 0.88f)
                lineTo(w * 0.48f, h * 0.88f)
                lineTo(w * 0.66f, h * 0.50f)
                lineTo(w * 0.48f, h * 0.12f)
                close()
            }
            drawPath(
                path = chevron1Path,
                brush = Brush.verticalGradient(
                    colors = listOf(darkColor, darkColor.copy(alpha = 0.8f))
                )
            )

            // Second Forward Supersonic Chevron (NEXT - Glowing Turbo Red/Orange)
            val chevron2Path = Path().apply {
                moveTo(w * 0.60f, h * 0.12f)
                lineTo(w * 0.80f, h * 0.50f)
                lineTo(w * 0.60f, h * 0.88f)
                lineTo(w * 0.72f, h * 0.88f)
                lineTo(w * 0.92f, h * 0.50f)
                lineTo(w * 0.72f, h * 0.12f)
                close()
            }
            drawPath(
                path = chevron2Path,
                brush = Brush.linearGradient(
                    colors = listOf(primaryColor, secondaryColor, accentOrange),
                    start = Offset(w * 0.60f, 0f),
                    end = Offset(w * 0.92f, h)
                )
            )

            // High-speed energetic pulse dot at the tip of the forward momentum
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, secondaryColor),
                    center = Offset(w * 0.93f, h * 0.50f),
                    radius = 4.dp.toPx()
                ),
                radius = 3.5.dp.toPx(),
                center = Offset(w * 0.93f, h * 0.50f)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // SPEEDNEXT Wordmark: "SPEED" + "[ NEXT ]" styled pill
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "SPEED",
                fontSize = 28.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 1.sp,
                color = if (isDark) Color.White else Color(0xFF161616)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFFE50914), Color(0xFFFF3333))
                        )
                    )
                    .padding(horizontal = 7.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "NEXT",
                    fontSize = 22.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 1.5.sp,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Exact Speed Display matching the screenshot:
 * Massive bold number on the left (e.g., 51),
 * with "Mbps" and the green-bordered reload circle stacked vertically on the right.
 */
@Composable
fun SpeedDisplayHero(
    speedMbps: Double,
    isDimmed: Boolean = false,
    phase: TestPhase,
    isDark: Boolean,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isTesting = phase == TestPhase.CONNECTING || phase == TestPhase.TESTING_DOWNLOAD

    val animatedAlpha by animateFloatAsState(
        targetValue = if (isDimmed) 0.35f else 1.0f,
        animationSpec = tween(durationMillis = 350),
        label = "speed_alpha"
    )

    val speedText = when {
        phase == TestPhase.IDLE -> "0"
        isDimmed -> if (speedMbps < 10.0 && speedMbps > 0) String.format(Locale.US, "%.1f", speedMbps) else String.format(Locale.US, "%.0f", speedMbps)
        phase == TestPhase.CONNECTING -> if (speedMbps > 0) String.format(Locale.US, if (speedMbps < 10.0) "%.1f" else "%.0f", speedMbps) else "0"
        speedMbps < 10.0 -> String.format(Locale.US, "%.1f", speedMbps)
        else -> String.format(Locale.US, "%.0f", speedMbps)
    }

    val numberColor = if (isDark) Color.White else Color(0xFF181818)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        // Giant Speed Number (Exact font size & scale from screenshot)
        Text(
            text = speedText,
            fontSize = 145.sp,
            lineHeight = 145.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = FontFamily.SansSerif,
            color = numberColor.copy(alpha = animatedAlpha),
            modifier = Modifier.testTag("speed_number_text")
        )

        Spacer(modifier = Modifier.width(14.dp))

        // Right side: "Mbps" on top, green reload circle below
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // "Mbps" Unit
            Text(
                text = "Mbps",
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                color = if (isDark) Color(0xFFE5E7EB) else Color(0xFF222222)
            )

            // Google Colorful Circular Loading Spinner & Reload Button
            GoogleColorfulSpinnerButton(
                isTesting = isTesting,
                isDark = isDark,
                onClick = onRestart
            )
        }
    }
}

/**
 * Google-style colorful circular spinner and reload button.
 * Both the circular border and the inner elements are colorful in Google's iconic 4 colors
 * (Blue, Red, Yellow, Green). When clicked, the colorful ring and icon spin together with
 * vibrant Google animated colors.
 */
@Composable
fun GoogleColorfulSpinnerButton(
    isTesting: Boolean,
    isDark: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "google_spinner")

    // Smooth continuous 360 degree rotation
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 950, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Breathing sweep arc length for iconic Google Material spinner
    val sweepArc by infiniteTransition.animateFloat(
        initialValue = 85f,
        targetValue = 280f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sweepArc"
    )

    // Continuous cycling through Google's 4 brand colors
    val colorCycleProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "colorCycle"
    )

    // Google's 4 iconic brand colors
    val googleColors = remember {
        listOf(
            Color(0xFF4285F4), // Google Blue
            Color(0xFFEA4335), // Google Red
            Color(0xFFFBBC05), // Google Yellow
            Color(0xFF34A853), // Google Green
            Color(0xFF4285F4)  // Loop back to Blue
        )
    }

    val dynamicGoogleColor = when {
        colorCycleProgress < 1f -> lerp(Color(0xFF4285F4), Color(0xFFEA4335), colorCycleProgress)
        colorCycleProgress < 2f -> lerp(Color(0xFFEA4335), Color(0xFFFBBC05), colorCycleProgress - 1f)
        colorCycleProgress < 3f -> lerp(Color(0xFFFBBC05), Color(0xFF34A853), colorCycleProgress - 2f)
        else -> lerp(Color(0xFF34A853), Color(0xFF4285F4), colorCycleProgress - 3f)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .testTag("restart_button")
    ) {
        Crossfade(
            targetState = isTesting,
            animationSpec = tween(durationMillis = 350),
            label = "spinner_mode_transition"
        ) { testing ->
            if (testing) {
                // Testing state: Rotating Google 4-Color Sweep Arc with rotating colorful icon inside!
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(3.dp)
                            .rotate(rotation)
                    ) {
                        val strokeWidth = 2.4.dp.toPx()
                        val radius = (size.minDimension - strokeWidth) / 2f
                        val centerOffset = Offset(size.width / 2f, size.height / 2f)

                        // Subtle translucent colorful track underneath
                        drawCircle(
                            brush = Brush.sweepGradient(colors = googleColors, center = centerOffset),
                            radius = radius,
                            center = centerOffset,
                            alpha = 0.2f,
                            style = Stroke(width = strokeWidth)
                        )

                        // Vibrant Google multi-color sweep arc
                        val sweepBrush = Brush.sweepGradient(
                            colors = googleColors,
                            center = centerOffset
                        )

                        drawArc(
                            brush = sweepBrush,
                            startAngle = 0f,
                            sweepAngle = sweepArc,
                            useCenter = false,
                            topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius),
                            size = Size(radius * 2, radius * 2),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    // Pause/stop bars in the center matching the user's screenshot
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.5.dp)
                                .height(15.dp)
                                .background(
                                    color = if (isDark) Color(0xFFD1D5DB) else Color(0xFF757575),
                                    shape = RoundedCornerShape(1.dp)
                                )
                        )
                        Box(
                            modifier = Modifier
                                .width(3.5.dp)
                                .height(15.dp)
                                .background(
                                    color = if (isDark) Color(0xFFD1D5DB) else Color(0xFF757575),
                                    shape = RoundedCornerShape(1.dp)
                                )
                        )
                    }
                }
            } else {
                // Completed state: Colorful Google 4-color circular border with reload icon inside!
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(3.dp)
                    ) {
                        val strokeWidth = 2.4.dp.toPx()
                        val radius = (size.minDimension - strokeWidth) / 2f
                        val centerOffset = Offset(size.width / 2f, size.height / 2f)

                        val sweepBrush = Brush.sweepGradient(
                            colors = googleColors,
                            center = centerOffset
                        )

                        // Beautiful Google 4-color ring
                        drawCircle(
                            brush = sweepBrush,
                            radius = radius,
                            center = centerOffset,
                            style = Stroke(width = strokeWidth)
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Restart Speed Test",
                        tint = if (isDark) Color.White else Color(0xFF181818),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

/**
 * Exact "Show more info" button matching the screenshot:
 * Rectangular outlined button with subtle rounded corners and clean gray border.
 */
@Composable
fun ShowMoreInfoButton(
    isExpanded: Boolean,
    language: AppLanguage,
    isDark: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .border(
                width = 1.dp,
                color = if (isDark) Color(0xFF4B5563) else Color(0xFFCCCCCC),
                shape = RoundedCornerShape(4.dp)
            )
            .background(if (isDark) Color(0xFF1F2937) else Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 26.dp, vertical = 9.dp)
            .testTag("show_more_button")
    ) {
        Text(
            text = if (isExpanded) SpeedTestStrings.showLess(language) else SpeedTestStrings.showMore(language),
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = FontFamily.SansSerif,
            color = if (isDark) Color(0xFFD1D5DB) else Color(0xFF555555)
        )
    }
}

/**
 * Help Circle Button (?) from the screenshot.
 */
@Composable
fun HelpCircleButton(
    onClick: () -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(if (isDark) Color(0xFF374151) else Color(0xFF444444))
            .clickable(onClick = onClick)
            .testTag("help_button")
    ) {
        Text(
            text = "?",
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

/**
 * "POWERED BY TUHINEXT" footer brand badge.
 */
@Composable
fun PoweredByTuhiNextBadge(
    modifier: Modifier = Modifier,
    isDark: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
        modifier = modifier
    ) {
        Text(
            text = "POWERED BY ",
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp,
            color = if (isDark) Color(0xFF888888) else Color(0xFF737373)
        )
        Text(
            text = "TUHI",
            fontSize = 15.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.5.sp,
            color = if (isDark) Color.White else Color(0xFF222222)
        )
        Text(
            text = "NEXT",
            fontSize = 15.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.5.sp,
            color = Color(0xFFE50914) // Signature red color
        )
    }
}

/**
 * Expanded Information Section (Revealed when "Show more info" is tapped).
 */
@Composable
fun DetailedInfoSection(
    state: SpeedTestUiState,
    isDark: Boolean,
    onTestAgain: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardBg = if (isDark) Color(0xFF1E293B) else Color(0xFFF9FAFB)
    val borderColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    val labelColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val valueColor = if (isDark) Color.White else Color(0xFF0F172A)
    val accentBlue = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Latency and Upload metrics grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Latency Box
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = SpeedTestStrings.latency(state.language),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = labelColor
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = SpeedTestStrings.unloaded(state.language),
                                fontSize = 11.sp,
                                color = labelColor
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (state.unloadedLatencyMs > 0) "${state.unloadedLatencyMs} ms" else "--",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = valueColor
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = SpeedTestStrings.loaded(state.language),
                                fontSize = 11.sp,
                                color = labelColor
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (state.loadedLatencyMs > 0) "${state.loadedLatencyMs} ms" else "--",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = valueColor
                            )
                        }
                    }
                }
            }

            // Upload Speed Box
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = SpeedTestStrings.upload(state.language),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = labelColor
                        )
                        if (state.phase == TestPhase.TESTING_UPLOAD) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(14.dp),
                                color = accentBlue
                            )
                        } else if (state.uploadSpeed > 0) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = accentBlue,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (state.uploadSpeed > 0) String.format(Locale.US, "%.1f", state.uploadSpeed) else "--",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = if (state.uploadSpeed > 0) accentBlue else valueColor
                    )
                    Text(
                        text = "Mbps",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = labelColor
                    )
                }
            }
        }

        // Network Metadata Card (Client IP, ISP, Location, Server)
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, borderColor, RoundedCornerShape(12.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DetailRow(
                    label = SpeedTestStrings.isp(state.language),
                    value = state.networkMeta.ispName,
                    isDark = isDark
                )

                HorizontalDivider(color = borderColor)

                DetailRow(
                    label = SpeedTestStrings.connection(state.language),
                    value = state.networkMeta.connectionType,
                    isDark = isDark
                )

                HorizontalDivider(color = borderColor)

                DetailRow(
                    label = SpeedTestStrings.ipAddress(state.language),
                    value = state.networkMeta.clientIp,
                    isDark = isDark
                )

                HorizontalDivider(color = borderColor)

                DetailRow(
                    label = SpeedTestStrings.location(state.language),
                    value = state.networkMeta.clientLocation,
                    isDark = isDark
                )

                HorizontalDivider(color = borderColor)

                DetailRow(
                    label = SpeedTestStrings.server(state.language),
                    value = state.networkMeta.serverLocation,
                    isDark = isDark
                )

                if (state.totalDownloadBytes > 0 || state.totalUploadBytes > 0) {
                    HorizontalDivider(color = borderColor)
                    val downMb = state.totalDownloadBytes / (1024.0 * 1024.0)
                    val upMb = state.totalUploadBytes / (1024.0 * 1024.0)
                    DetailRow(
                        label = SpeedTestStrings.dataTransferred(state.language),
                        value = String.format(Locale.US, "%.1f MB ↓ • %.1f MB ↑", downMb, upMb),
                        isDark = isDark
                    )
                }
            }
        }

        // Action Buttons Row (Test Again / Share)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onTestAgain,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDark) Color(0xFF2563EB) else Color(0xFF181818)
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("test_again_button")
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = SpeedTestStrings.testAgain(state.language), fontSize = 14.sp)
            }

            OutlinedButton(
                onClick = onShare,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (isDark) Color.White else Color(0xFF181818)
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(borderColor)
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("share_button")
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = SpeedTestStrings.share(state.language), fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun DetailRow(
    label: String,
    value: String,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isDark) Color(0xFFF1F5F9) else Color(0xFF0F172A)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorySheet(
    records: List<SpeedTestRecord>,
    language: AppLanguage,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onDeleteRecord: (SpeedTestRecord) -> Unit,
    onClearAll: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = if (isDark) Color(0xFF111827) else Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = SpeedTestStrings.history(language),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color(0xFF111827)
                )

                if (records.isNotEmpty()) {
                    IconButton(
                        onClick = onClearAll,
                        modifier = Modifier.testTag("clear_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = SpeedTestStrings.clearHistory(language),
                            tint = Color(0xFFEF4444)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (records.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp)
                ) {
                    Text(
                        text = SpeedTestStrings.noHistory(language),
                        color = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280),
                        fontSize = 14.sp
                    )
                }
            } else {
                records.forEach { record ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDark) Color(0xFF1F2937) else Color(0xFFF3F4F6)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = dateFormat.format(Date(record.timestamp)),
                                    fontSize = 11.sp,
                                    color = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                    Text(
                                        text = "↓ ${String.format(Locale.US, "%.1f", record.downloadSpeedMbps)} Mbps",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = if (isDark) Color(0xFF34D399) else Color(0xFF00A82D)
                                    )
                                    if (record.uploadSpeedMbps > 0) {
                                        Text(
                                            text = "↑ ${String.format(Locale.US, "%.1f", record.uploadSpeedMbps)} Mbps",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = if (isDark) Color(0xFF60A5FA) else Color(0xFF0284C7)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "${record.ispName} • ${record.unloadedLatencyMs} ms",
                                    fontSize = 12.sp,
                                    color = if (isDark) Color(0xFFD1D5DB) else Color(0xFF4B5563)
                                )
                            }

                            IconButton(onClick = { onDeleteRecord(record) }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Delete",
                                    tint = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    currentStreams: Int,
    currentDurationSec: Int,
    language: AppLanguage,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onUpdate: (streams: Int, durationSec: Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = if (isDark) Color(0xFF111827) else Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = SpeedTestStrings.settings(language),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else Color(0xFF111827)
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = SpeedTestStrings.parallelStreams(language),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDark) Color(0xFFD1D5DB) else Color(0xFF374151)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(2, 3, 4, 8).forEach { streamCount ->
                    FilterChip(
                        selected = currentStreams == streamCount,
                        onClick = { onUpdate(streamCount, currentDurationSec) },
                        label = { Text("$streamCount Streams") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = SpeedTestStrings.duration(language),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDark) Color(0xFFD1D5DB) else Color(0xFF374151)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(5, 10, 15, 20).forEach { dur ->
                    FilterChip(
                        selected = currentDurationSec == dur,
                        onClick = { onUpdate(currentStreams, dur) },
                        label = { Text("${dur}s") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
