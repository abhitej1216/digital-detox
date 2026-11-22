package com.example.digitaldetox.presentation.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.digitaldetox.data.entity.FocusMode
import com.example.digitaldetox.presentation.viewmodel.FocusTimerViewModel
import com.example.digitaldetox.ui.components.AnimatedSegmentedControl
import com.example.digitaldetox.ui.components.CraftCard
import com.example.digitaldetox.ui.components.CraftPillBadge
import com.example.digitaldetox.ui.components.tactilePress
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusTimerScreen(
    viewModel: FocusTimerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current

    val totalSeconds = uiState.durationMinutes * 60L
    val targetProgress = if (totalSeconds > 0) uiState.remainingTimeSeconds.toFloat() / totalSeconds.toFloat() else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "timer_progress"
    )

    // Emil Kowalski-inspired subtle breathing pulse while active
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val breathingScale by if (uiState.isRunning) {
        infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.025f,
            animationSpec = infiniteRepeatable(
                animation = tween(2800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_scale"
        )
    } else {
        remember { mutableFloatStateOf(1.0f) }
    }

    val minutes = uiState.remainingTimeSeconds / 60
    val seconds = uiState.remainingTimeSeconds % 60
    val timeString = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

    val activeAccent = if (uiState.selectedMode == FocusMode.STUDY) {
        MaterialTheme.colorScheme.secondary
    } else {
        MaterialTheme.colorScheme.primary
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Focus Session",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Block noise & enter deep flow",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    CraftPillBadge(
                        text = if (uiState.isRunning) "Running" else if (uiState.isPaused) "Paused" else "Idle",
                        indicatorColor = if (uiState.isRunning) activeAccent else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            // Segmented Mode Switcher (Study vs Work)
            AnimatedSegmentedControl(
                items = listOf(FocusMode.STUDY, FocusMode.WORK),
                selectedItem = uiState.selectedMode,
                onItemSelected = { viewModel.selectMode(it) },
                labelProvider = { mode -> if (mode == FocusMode.STUDY) "Study Session" else "Work Session" },
                modifier = Modifier.fillMaxWidth()
            )

            // Duration Selection Pills (15m, 25m, 45m, 60m)
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(15, 25, 45, 60).forEach { mins ->
                    val isSelected = uiState.durationMinutes == mins
                    val isEnabled = !uiState.isRunning && !uiState.isPaused

                    val bg by animateColorAsState(
                        targetValue = if (isSelected) activeAccent else MaterialTheme.colorScheme.surfaceVariant,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        label = "duration_bg"
                    )
                    val textCol by animateColorAsState(
                        targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        label = "duration_text"
                    )

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(bg)
                            .then(
                                if (isEnabled) {
                                    Modifier.tactilePress(scale = 0.94f) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.setDuration(mins)
                                    }
                                } else {
                                    Modifier
                                }
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${mins}m",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = textCol
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Precision Ring Gauge with Breathing Motion
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(240.dp)
                    .graphicsLayer {
                        scaleX = breathingScale
                        scaleY = breathingScale
                    }
            ) {
                val trackColor = MaterialTheme.colorScheme.surfaceVariant
                val ringColor = activeAccent

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = 12.dp.toPx()
                    // Background track
                    drawCircle(
                        color = trackColor,
                        style = Stroke(width = stroke)
                    )
                    // Animated Progress Arc
                    drawArc(
                        color = ringColor,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = timeString,
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-1.5).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (uiState.selectedMode == FocusMode.STUDY) "Deep Study" else "Deep Work",
                        style = MaterialTheme.typography.bodySmall,
                        color = activeAccent,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons with Tactile Spring Press
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isRunning = uiState.isRunning
                val isPaused = uiState.isPaused

                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (isRunning) viewModel.pauseTimer() else viewModel.startTimer()
                    },
                    modifier = Modifier
                        .height(54.dp)
                        .weight(1f)
                        .tactilePress(scale = 0.95f),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRunning) MaterialTheme.colorScheme.error else activeAccent
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isRunning) "Pause" else if (isPaused) "Resume" else "Start Focus",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (isRunning || isPaused) {
                    OutlinedButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.stopTimer()
                        },
                        modifier = Modifier
                            .height(54.dp)
                            .tactilePress(scale = 0.95f),
                        shape = CircleShape,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Stop",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Reset",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
