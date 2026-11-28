package com.example.digitaldetox.presentation.intervention

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.digitaldetox.data.repository.SettingsRepository
import com.example.digitaldetox.service.MindfulInterventionManager
import com.example.digitaldetox.ui.components.CraftCard
import com.example.digitaldetox.ui.components.CraftPillBadge
import com.example.digitaldetox.ui.components.tactilePress
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class BreathingPhase(val label: String, val instruction: String) {
    INHALE("Breathe In", "Deep, slow breath through your nose"),
    HOLD("Hold", "Pause and notice the urge"),
    EXHALE("Breathe Out", "Slowly release and relax your mind")
}

@Composable
fun MindfulInterventionScreen(
    packageName: String,
    appLabel: String,
    settingsRepository: SettingsRepository,
    onCloseToHome: () -> Unit,
    onLaunchApp: () -> Unit
) {
    val totalSeconds = 10
    var secondsElapsed by remember { mutableIntStateOf(0) }
    var isBreathingCompleted by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var microSessionMinutes by remember { mutableIntStateOf(5) }
    var cooldownMinutes by remember { mutableIntStateOf(30) }

    BackHandler {
        coroutineScope.launch {
            if (packageName != "preview") {
                val currentSaves = settingsRepository.getInt("mindful_saves_count", 0)
                settingsRepository.saveInt("mindful_saves_count", currentSaves + 1)
            }
            onCloseToHome()
        }
    }

    LaunchedEffect(Unit) {
        microSessionMinutes = settingsRepository.getInt("micro_session_minutes", 5)
        cooldownMinutes = settingsRepository.getInt("cooldown_minutes", 30)
    }

    // Determine current breathing phase based on elapsed seconds
    // 0-4s: Inhale (4s)
    // 4-6s: Hold (2s)
    // 6-10s: Exhale (4s)
    val currentPhase = remember(secondsElapsed) {
        when {
            secondsElapsed < 4 -> BreathingPhase.INHALE
            secondsElapsed < 6 -> BreathingPhase.HOLD
            else -> BreathingPhase.EXHALE
        }
    }

    // Trigger haptic pulses on phase transitions
    LaunchedEffect(currentPhase) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    // Run the 10-second guided timer
    LaunchedEffect(Unit) {
        while (secondsElapsed < totalSeconds) {
            delay(1000L)
            secondsElapsed++
        }
        isBreathingCompleted = true
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    // Animated breathing scale (smoothly expands during inhale, holds, contracts during exhale)
    val targetScale = when (currentPhase) {
        BreathingPhase.INHALE -> 1.35f
        BreathingPhase.HOLD -> 1.35f
        BreathingPhase.EXHALE -> 0.85f
    }

    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(
            durationMillis = when (currentPhase) {
                BreathingPhase.INHALE -> 3800
                BreathingPhase.HOLD -> 1800
                BreathingPhase.EXHALE -> 3800
            },
            easing = FastOutSlowInEasing
        ),
        label = "breathing_scale"
    )

    val progress = (secondsElapsed.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(900, easing = LinearEasing),
        label = "progress"
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header: Target App Chip
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CraftPillBadge(
                        text = "Mindful Pause",
                        indicatorColor = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Opening $appLabel",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Take a breath before entering automatic mode",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Central Breathing Visualizer
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(260.dp)
                ) {
                    // Outer 10-second countdown track
                    val trackColor = MaterialTheme.colorScheme.surfaceVariant
                    val progressColor = MaterialTheme.colorScheme.primary

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val stroke = 4.dp.toPx()
                        drawCircle(color = trackColor, style = Stroke(width = stroke))
                        drawArc(
                            color = progressColor,
                            startAngle = -90f,
                            sweepAngle = 360f * animatedProgress,
                            useCenter = false,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                    }

                    // Concentric Animated Aura Rings
                    Box(
                        modifier = Modifier
                            .size(190.dp)
                            .graphicsLayer {
                                scaleX = animatedScale * 1.15f
                                scaleY = animatedScale * 1.15f
                                alpha = 0.18f
                            }
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )

                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .graphicsLayer {
                                scaleX = animatedScale
                                scaleY = animatedScale
                                alpha = 0.35f
                            }
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )

                    // Core Breathing Sphere
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .graphicsLayer {
                                scaleX = animatedScale * 0.9f
                                scaleY = animatedScale * 0.9f
                            }
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (!isBreathingCompleted) "${totalSeconds - secondsElapsed}s" else "✓",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Phase Instruction Text
                AnimatedContent(
                    targetState = currentPhase,
                    transitionSpec = {
                        fadeIn(tween(300)) togetherWith fadeOut(tween(200))
                    },
                    label = "phase_text"
                ) { phase ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (!isBreathingCompleted) phase.label else "Deep Breath Complete",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (!isBreathingCompleted) phase.instruction else "Notice how the initial craving feels now.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(36.dp))

                // Action Decisions (Revealed smoothly after breath completes)
                AnimatedVisibility(
                    visible = isBreathingCompleted,
                    enter = fadeIn(tween(250)) + scaleIn(initialScale = 0.95f, animationSpec = tween(250)),
                    exit = fadeOut(tween(150))
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Option 1: "I don't need this right now" (Mindful Save!)
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                coroutineScope.launch {
                                    if (packageName != "preview") {
                                        val currentSaves = settingsRepository.getInt("mindful_saves_count", 0)
                                        settingsRepository.saveInt("mindful_saves_count", currentSaves + 1)
                                        Toast.makeText(
                                            context,
                                            "Mindful Save recorded! Distraction avoided.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    onCloseToHome()
                                }
                            },
                            shape = CircleShape,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .tactilePress(scale = 0.96f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "I don't need this right now",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Option 2: "Continue for X minutes only" (Micro-Session Window)
                        OutlinedButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                if (packageName != "preview") {
                                    MindfulInterventionManager.startMicroSession(
                                        packageName = packageName,
                                        sessionMinutes = microSessionMinutes,
                                        cooldownMinutes = cooldownMinutes
                                    )
                                    Toast.makeText(
                                        context,
                                        "$microSessionMinutes-min micro-session started ($cooldownMinutes-min cooldown after).",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                onLaunchApp()
                            },
                            shape = CircleShape,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .tactilePress(scale = 0.96f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Text(
                                text = "Open for $microSessionMinutes minutes only",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // If still breathing, show gentle cancel button
                if (!isBreathingCompleted) {
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                if (packageName != "preview") {
                                    val currentSaves = settingsRepository.getInt("mindful_saves_count", 0)
                                    settingsRepository.saveInt("mindful_saves_count", currentSaves + 1)
                                }
                                onCloseToHome()
                            }
                        },
                        modifier = Modifier.tactilePress(scale = 0.94f)
                    ) {
                        Text(
                            text = "Nevermind, close app",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
