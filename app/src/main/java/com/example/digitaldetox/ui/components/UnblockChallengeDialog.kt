package com.example.digitaldetox.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay
import java.util.Locale

private data class MathChallenge(
    val expression: String,
    val answer: Int
)

private enum class ChallengeStage {
    MATH_ONE,
    MATH_TWO,
    PLEDGE,
    COMPLETED
}

private val COMMITMENT_PHRASES = listOf(
    "I choose to pause my detox",
    "I accept breaking my limit",
    "I will return to focus soon",
    "I am unlocking intentionally"
)

private fun randomNonTen(range: IntRange): Int {
    var value = range.random()
    while (value % 10 == 0) {
        value = range.random()
    }
    return value
}

private fun generateMathChallenge(harder: Boolean): MathChallenge {
    return if (!harder) {
        when ((1..3).random()) {
            1 -> {
                val a = randomNonTen(16..29)
                val b = randomNonTen(12..23)
                val c = randomNonTen(37..129)
                MathChallenge("($a × $b) + $c = ?", (a * b) + c)
            }
            2 -> {
                val a = randomNonTen(17..33)
                val b = randomNonTen(13..22)
                val c = randomNonTen(28..97)
                MathChallenge("($a × $b) - $c = ?", (a * b) - c)
            }
            else -> {
                val a = randomNonTen(14..29)
                val b = randomNonTen(13..27)
                val c = randomNonTen(12..19)
                MathChallenge("($a + $b) × $c = ?", (a + b) * c)
            }
        }
    } else {
        when ((1..3).random()) {
            1 -> {
                val a = randomNonTen(13..24)
                val b = randomNonTen(7..14)
                val c = randomNonTen(11..19)
                val d = randomNonTen(6..13)
                MathChallenge("($a × $b) + ($c × $d) = ?", (a * b) + (c * d))
            }
            2 -> {
                val a = randomNonTen(18..29)
                val b = randomNonTen(12..19)
                val c = randomNonTen(7..13)
                val d = randomNonTen(6..11)
                MathChallenge("($a × $b) - ($c × $d) = ?", (a * b) - (c * d))
            }
            else -> {
                val a = randomNonTen(13..24)
                val b = randomNonTen(45..120)
                val c = randomNonTen(15..44)
                MathChallenge("($a × $a) + $b - $c = ?", (a * a) + b - c)
            }
        }
    }
}

/**
 * Hardened 60-second multi-stage Unblock Challenge Modal:
 * 1. Mandatory 60s countdown (pauses if app leaves foreground; +10s penalty on wrong math guesses).
 * 2. Two sequential multi-operation arithmetic problems with explicit verification.
 * 3. Manual typing of a mindful commitment phrase (paste blocked).
 */
@Composable
fun UnblockChallengeDialog(
    targetKey: Any,
    title: String,
    subtitle: String = "Pause for a full minute. Breaking your barrier requires deliberate effort across all 3 steps.",
    confirmButtonText: String = "Confirm Unblock",
    waitSeconds: Int = 60,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var isResumed by remember { mutableStateOf(true) }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> isResumed = true
                Lifecycle.Event.ON_PAUSE -> isResumed = false
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    var totalWaitSeconds by remember(targetKey) { mutableIntStateOf(waitSeconds) }
    var remainingSeconds by remember(targetKey) { mutableIntStateOf(waitSeconds) }
    var stage by remember(targetKey) { mutableStateOf(ChallengeStage.MATH_ONE) }

    var mathProblemOne by remember(targetKey) { mutableStateOf(generateMathChallenge(harder = false)) }
    var mathProblemTwo by remember(targetKey) { mutableStateOf(generateMathChallenge(harder = true)) }
    val commitmentPhrase = remember(targetKey) { COMMITMENT_PHRASES.random() }

    var userInput by remember(targetKey) { mutableStateOf("") }
    var wrongAttempts by remember(targetKey) { mutableIntStateOf(0) }
    var errorMessage by remember(targetKey) { mutableStateOf<String?>(null) }

    // Count down only while the app is actively in foreground
    LaunchedEffect(targetKey, isResumed) {
        while (remainingSeconds > 0 && isResumed) {
            delay(1000L)
            if (isResumed && remainingSeconds > 0) {
                remainingSeconds--
            }
        }
    }

    val rawProgress = if (totalWaitSeconds > 0) {
        ((totalWaitSeconds - remainingSeconds).toFloat() / totalWaitSeconds.toFloat()).coerceIn(0f, 1f)
    } else 1f
    val animatedProgress by animateFloatAsState(
        targetValue = rawProgress,
        animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
        label = "unblock_timer_progress"
    )

    val formattedTimer = remember(remainingSeconds) {
        val mins = remainingSeconds / 60
        val secs = remainingSeconds % 60
        String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
    }

    fun handleMathSubmission(
        expectedAnswer: Int,
        nextStage: ChallengeStage,
        regenerate: () -> Unit
    ) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        val parsed = userInput.trim().toIntOrNull()
        if (parsed == expectedAnswer) {
            userInput = ""
            wrongAttempts = 0
            errorMessage = null
            stage = nextStage
        } else {
            wrongAttempts++
            remainingSeconds += 10
            totalWaitSeconds = maxOf(totalWaitSeconds, remainingSeconds)
            userInput = ""
            if (wrongAttempts >= 2) {
                regenerate()
                wrongAttempts = 0
                errorMessage = "Incorrect. +10s penalty & new equation generated."
            } else {
                errorMessage = "Incorrect answer. +10s added to cooldown."
            }
        }
    }

    fun submitCurrentStep() {
        when (stage) {
            ChallengeStage.MATH_ONE -> handleMathSubmission(
                expectedAnswer = mathProblemOne.answer,
                nextStage = ChallengeStage.MATH_TWO,
                regenerate = { mathProblemOne = generateMathChallenge(harder = false) }
            )
            ChallengeStage.MATH_TWO -> handleMathSubmission(
                expectedAnswer = mathProblemTwo.answer,
                nextStage = ChallengeStage.PLEDGE,
                regenerate = { mathProblemTwo = generateMathChallenge(harder = true) }
            )
            ChallengeStage.PLEDGE -> {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                if (userInput.trim().equals(commitmentPhrase, ignoreCase = true)) {
                    userInput = ""
                    errorMessage = null
                    stage = ChallengeStage.COMPLETED
                } else {
                    errorMessage = "Phrase must match exactly: \"$commitmentPhrase\""
                }
            }
            ChallengeStage.COMPLETED -> Unit
        }
    }

    val isConfirmEnabled = remainingSeconds == 0 && stage == ChallengeStage.COMPLETED

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        ),
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "STRICT BARRIER • 1 MIN",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Step progress indicators (Math 1 -> Math 2 -> Pledge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val steps = listOf(
                        "1. Math" to (stage.ordinal > ChallengeStage.MATH_ONE.ordinal),
                        "2. Math" to (stage.ordinal > ChallengeStage.MATH_TWO.ordinal),
                        "3. Pledge" to (stage == ChallengeStage.COMPLETED)
                    )
                    steps.forEachIndexed { index, (label, done) ->
                        val active = stage.ordinal == index
                        val bgColor = when {
                            done -> MaterialTheme.colorScheme.primaryContainer
                            active -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                        val txtColor = when {
                            done -> MaterialTheme.colorScheme.onPrimaryContainer
                            active -> MaterialTheme.colorScheme.onSurface
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(CircleShape)
                                .background(bgColor)
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (done) "✓ $label" else label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (active || done) FontWeight.Bold else FontWeight.Medium,
                                color = txtColor
                            )
                        }
                    }
                }

                // 60-Second Countdown Bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(44.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier.fillMaxSize(),
                            strokeWidth = 3.5.dp,
                            color = if (remainingSeconds > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.outlineVariant,
                            strokeCap = StrokeCap.Round
                        )
                        Text(
                            text = if (remainingSeconds > 0) "$remainingSeconds" else "✓",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                            fontWeight = FontWeight.Bold,
                            color = if (remainingSeconds > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (remainingSeconds > 0) "Mandatory wait: $formattedTimer" else "1-minute wait completed",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (remainingSeconds > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (remainingSeconds > 0) {
                                "Timer pauses if you leave this screen."
                            } else {
                                "Timer passed. Finish verification to unlock."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Active Challenge Card
                AnimatedContent(
                    targetState = stage,
                    transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(150)) },
                    label = "challenge_stage"
                ) { currentStage ->
                    when (currentStage) {
                        ChallengeStage.MATH_ONE, ChallengeStage.MATH_TWO -> {
                            val problem = if (currentStage == ChallengeStage.MATH_ONE) mathProblemOne else mathProblemTwo
                            val stepLabel = if (currentStage == ChallengeStage.MATH_ONE) "EQUATION 1 OF 2" else "EQUATION 2 OF 2 (HARDER)"

                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                CraftCard(
                                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text(
                                        text = stepLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = problem.expression,
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                OutlinedTextField(
                                    value = userInput,
                                    onValueChange = { newValue ->
                                        userInput = newValue
                                        errorMessage = null
                                    },
                                    label = { Text("Enter result") },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(onDone = { submitCurrentStep() }),
                                    singleLine = true,
                                    isError = errorMessage != null,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedButton(
                                    onClick = { submitCurrentStep() },
                                    enabled = userInput.isNotBlank(),
                                    shape = CircleShape,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .tactilePress(scale = 0.96f)
                                ) {
                                    Text("Verify Answer", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        ChallengeStage.PLEDGE -> {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                CraftCard(
                                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text(
                                        text = "FINAL STEP • TYPE MANUALLY (NO PASTE)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = commitmentPhrase,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                OutlinedTextField(
                                    value = userInput,
                                    onValueChange = { newValue ->
                                        // Block multi-character pastes while still allowing backspace and typing
                                        if (newValue.length - userInput.length > 3) {
                                            errorMessage = "Please type the phrase manually — pasting is disabled."
                                        } else {
                                            userInput = newValue
                                            errorMessage = null
                                        }
                                    },
                                    label = { Text("Type the commitment phrase") },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Ascii,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(onDone = { submitCurrentStep() }),
                                    singleLine = true,
                                    isError = errorMessage != null,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                    )
                                )

                                OutlinedButton(
                                    onClick = { submitCurrentStep() },
                                    enabled = userInput.isNotBlank(),
                                    shape = CircleShape,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .tactilePress(scale = 0.96f)
                                ) {
                                    Text("Verify Pledge", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        ChallengeStage.COMPLETED -> {
                            CraftCard(
                                backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                                borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Column {
                                        Text(
                                            text = "All Challenges Cleared",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (remainingSeconds > 0) {
                                                "Waiting $formattedTimer remaining before unlocking."
                                            } else {
                                                "You may now proceed, or cancel to stay focused."
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                errorMessage?.let { err ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = err,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onConfirm()
                },
                enabled = isConfirmEnabled,
                shape = CircleShape,
                modifier = Modifier.tactilePress(scale = 0.95f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.size(6.dp))
                Text(confirmButtonText, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.tactilePress(scale = 0.95f)
            ) {
                Text(
                    "Stay Focused",
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    )
}
