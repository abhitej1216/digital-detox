package com.example.digitaldetox.presentation.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.digitaldetox.presentation.screens.BlockedAppsScreen
import com.example.digitaldetox.presentation.screens.DashboardScreen
import com.example.digitaldetox.presentation.screens.FocusTimerScreen
import com.example.digitaldetox.presentation.screens.SettingsScreen
import com.example.digitaldetox.ui.components.tactilePress

@Composable
fun NavHostRoot() {
    val navController = rememberNavController()

    data class NavItem(val screen: Screen, val icon: ImageVector, val label: String)
    val navItems = listOf(
        NavItem(Screen.Dashboard, Icons.Default.Home, "Overview"),
        NavItem(Screen.Blocked, Icons.Default.Lock, "Limits"),
        NavItem(Screen.FocusTimer, Icons.Default.PlayArrow, "Focus"),
        NavItem(Screen.Settings, Icons.Default.Settings, "Settings")
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Dashboard.route
    val haptic = LocalHapticFeedback.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            // Emil Kowalski / Apple-grade floating pill dock
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .navigationBarsPadding(),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant
                ),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    navItems.forEach { item ->
                        val isSelected = currentRoute == item.screen.route

                        val animatedPillColor by animateColorAsState(
                            targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMedium
                            ),
                            label = "nav_pill"
                        )
                        val animatedContentColor by animateColorAsState(
                            targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            animationSpec = spring(stiffness = Spring.StiffnessMedium),
                            label = "nav_content"
                        )

                        Row(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(animatedPillColor)
                                .tactilePress(scale = 0.92f) {
                                    if (!isSelected) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        navController.navigate(item.screen.route) {
                                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = animatedContentColor,
                                modifier = Modifier.size(20.dp)
                            )
                            if (isSelected) {
                                Text(
                                    text = item.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = animatedContentColor,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding()),
            navController = navController,
            startDestination = Screen.Dashboard.route,
            enterTransition = {
                fadeIn(tween(220, easing = FastOutSlowInEasing)) +
                        scaleIn(initialScale = 0.96f, animationSpec = tween(220, easing = FastOutSlowInEasing))
            },
            exitTransition = {
                fadeOut(tween(160, easing = FastOutSlowInEasing)) +
                        scaleOut(targetScale = 0.96f, animationSpec = tween(160, easing = FastOutSlowInEasing))
            },
            popEnterTransition = {
                fadeIn(tween(220, easing = FastOutSlowInEasing)) +
                        scaleIn(initialScale = 0.96f, animationSpec = tween(220, easing = FastOutSlowInEasing))
            },
            popExitTransition = {
                fadeOut(tween(160, easing = FastOutSlowInEasing)) +
                        scaleOut(targetScale = 0.96f, animationSpec = tween(160, easing = FastOutSlowInEasing))
            }
        ) {
            composable(Screen.Dashboard.route) { DashboardScreen() }
            composable(Screen.Blocked.route) { BlockedAppsScreen() }
            composable(Screen.FocusTimer.route) { FocusTimerScreen() }
            composable(Screen.Settings.route) { SettingsScreen() }
        }
    }
}
