package com.example.digitaldetox.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.digitaldetox.presentation.screens.DashboardScreen
import com.example.digitaldetox.presentation.screens.BlockedAppsScreen
import com.example.digitaldetox.presentation.screens.FocusTimerScreen
import com.example.digitaldetox.presentation.screens.SettingsScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding

import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun NavHostRoot() {
    val navController = rememberNavController()

    val items = listOf(
        Screen.Dashboard,
        Screen.Blocked,
        Screen.FocusTimer,
        Screen.Settings
    )
    val icons = listOf(
        Icons.Default.Home,
        Icons.Default.Lock,
        Icons.Default.PlayArrow,
        Icons.Default.Settings
    )

    Scaffold(
        modifier = androidx.compose.ui.Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                items.forEachIndexed { index, screen ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            modifier = androidx.compose.ui.Modifier.padding(innerPadding),
            navController = navController,
            startDestination = Screen.Dashboard.route
        ) {
            composable(Screen.Dashboard.route) { DashboardScreen() }
            composable(Screen.Blocked.route) { BlockedAppsScreen() }
            composable(Screen.FocusTimer.route) { FocusTimerScreen() }
            composable(Screen.Settings.route) { SettingsScreen() }
        }
    }
}