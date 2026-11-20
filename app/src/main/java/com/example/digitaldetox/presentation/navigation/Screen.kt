package com.example.digitaldetox.presentation.navigation

sealed class Screen(val route: String, val title: String) {
    object Home : Screen("home", "Home")
    object Dashboard : Screen("dashboard", "Dashboard")
    object Blocked : Screen("blocked", "Blocked Apps")
    object Settings : Screen("settings", "Settings")
    object FocusTimer : Screen("focus_timer", "Focus Timer")
}

object Routes {
    val HOME = Screen.Home.route
    val DASHBOARD = Screen.Dashboard.route
    val BLOCKED = Screen.Blocked.route
    val SETTINGS = Screen.Settings.route
    val FOCUS_TIMER = Screen.FocusTimer.route
}