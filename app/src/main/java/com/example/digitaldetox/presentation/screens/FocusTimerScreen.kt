package com.example.digitaldetox.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.digitaldetox.data.entity.FocusMode
import com.example.digitaldetox.presentation.viewmodel.FocusTimerViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusTimerScreen(
    viewModel: FocusTimerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val totalSeconds = uiState.durationMinutes * 60L
    val progress = if (totalSeconds > 0) uiState.remainingTimeSeconds.toFloat() / totalSeconds.toFloat() else 0f
    val minutes = uiState.remainingTimeSeconds / 60
    val seconds = uiState.remainingTimeSeconds % 60
    val timeString = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Focus Timer") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Focus Mode",
                style = MaterialTheme.typography.titleMedium
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = uiState.selectedMode == FocusMode.STUDY,
                    onClick = { viewModel.selectMode(FocusMode.STUDY) },
                    label = { Text("Study") },
                    enabled = !uiState.isRunning && !uiState.isPaused
                )
                FilterChip(
                    selected = uiState.selectedMode == FocusMode.WORK,
                    onClick = { viewModel.selectMode(FocusMode.WORK) },
                    label = { Text("Work") },
                    enabled = !uiState.isRunning && !uiState.isPaused
                )
            }

            Text(
                text = "Timer Duration",
                style = MaterialTheme.typography.titleMedium
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(15, 25, 45, 60).forEach { mins ->
                    FilterChip(
                        selected = uiState.durationMinutes == mins,
                        onClick = { viewModel.setDuration(mins) },
                        label = { Text("${mins}m") },
                        enabled = !uiState.isRunning && !uiState.isPaused
                    )
                }
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(200.dp)
                    .padding(8.dp)
            ) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 10.dp,
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = timeString,
                        style = MaterialTheme.typography.displayMedium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (uiState.selectedMode == FocusMode.STUDY) "Study Session" else "Work Session",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!uiState.isRunning) {
                    Button(
                        onClick = { viewModel.startTimer() },
                        modifier = Modifier
                            .height(50.dp)
                            .widthIn(min = 120.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Start")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (uiState.isPaused) "Resume" else "Start")
                    }
                } else {
                    Button(
                        onClick = { viewModel.pauseTimer() },
                        modifier = Modifier
                            .height(50.dp)
                            .widthIn(min = 120.dp)
                    ) {
                        Text("Pause")
                    }
                }

                if (uiState.isRunning || uiState.isPaused) {
                    OutlinedButton(
                        onClick = { viewModel.stopTimer() },
                        modifier = Modifier.height(50.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Stop")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Stop")
                    }
                }
            }
        }
    }
}
