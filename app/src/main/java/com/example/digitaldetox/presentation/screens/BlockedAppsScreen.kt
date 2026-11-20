package com.example.digitaldetox.presentation.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.digitaldetox.presentation.viewmodel.BlockedAppsViewModel
import com.example.digitaldetox.presentation.viewmodel.FilterMode
import com.example.digitaldetox.presentation.viewmodel.PendingUnblockApp
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockedAppsScreen(
    viewModel: BlockedAppsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkAccessibilityService()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    uiState.pendingUnblockApp?.let { pendingApp ->
        UnblockChallengeDialog(
            pendingApp = pendingApp,
            onConfirm = { viewModel.confirmUnblockApp() },
            onDismiss = { viewModel.cancelUnblock() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Blocked Apps") })
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                if (!uiState.isAccessibilityServiceEnabled) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "App Blocking Service Disabled. Enable in Settings to start blocking apps.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Button(
                                onClick = {
                                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                    context.startActivity(intent)
                                }
                            ) {
                                Text("Enable Service")
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    placeholder = { Text("Search apps...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.filterMode == FilterMode.ALL,
                        onClick = { viewModel.onFilterModeChanged(FilterMode.ALL) },
                        label = { Text("All") }
                    )
                    FilterChip(
                        selected = uiState.filterMode == FilterMode.BLOCKED_ONLY,
                        onClick = { viewModel.onFilterModeChanged(FilterMode.BLOCKED_ONLY) },
                        label = { Text("Blocked Only") }
                    )
                }

                if (uiState.apps.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No apps found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.apps, key = { it.packageName }) { app ->
                            val icon = remember(app.packageName) {
                                try {
                                    context.packageManager.getApplicationIcon(app.packageName).toBitmap().asImageBitmap()
                                } catch (_: Exception) {
                                    null
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    if (icon != null) {
                                        Image(
                                            bitmap = icon,
                                            contentDescription = app.label,
                                            modifier = Modifier.size(40.dp)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = app.label,
                                            modifier = Modifier.size(40.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Text(
                                        text = app.label,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                                Switch(
                                    checked = app.blockEnabled,
                                    onCheckedChange = { isChecked ->
                                        viewModel.toggleAppBlocked(app.packageName, isChecked)
                                    }
                                )
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UnblockChallengeDialog(
    pendingApp: PendingUnblockApp,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    data class MathChallenge(val expression: String, val answer: Int)

    val challenge = remember(pendingApp) {
        when ((1..3).random()) {
            1 -> {
                val a = (13..25).random()
                val b = (12..19).random()
                MathChallenge("$a × $b = ?", a * b)
            }
            2 -> {
                val a = (12..20).random()
                val b = (6..14).random()
                val c = (25..85).random()
                MathChallenge("($a × $b) + $c = ?", (a * b) + c)
            }
            else -> {
                val a = (150..450).random()
                val b = (120..380).random()
                val c = (60..190).random()
                MathChallenge("$a + $b - $c = ?", a + b - c)
            }
        }
    }

    var countdown by remember(pendingApp) { mutableIntStateOf(30) }
    var userAnswer by remember(pendingApp) { mutableStateOf("") }

    LaunchedEffect(pendingApp) {
        countdown = 30
        while (countdown > 0) {
            delay(1000L)
            countdown--
        }
    }

    val isAnswerCorrect = userAnswer.trim() == challenge.answer.toString()
    val isConfirmEnabled = countdown == 0 && isAnswerCorrect

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Unblock ${pendingApp.label}?") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Is opening this app right now worth breaking your digital detox?",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "Solve the challenge to confirm unblocking:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = challenge.expression,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (countdown > 0) "Mandatory wait: $countdown seconds remaining..." else "Timer finished!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (countdown > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
                OutlinedTextField(
                    value = userAnswer,
                    onValueChange = { userAnswer = it },
                    label = { Text("Enter Answer") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = isConfirmEnabled
            ) {
                Text("Confirm Unblock")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}
