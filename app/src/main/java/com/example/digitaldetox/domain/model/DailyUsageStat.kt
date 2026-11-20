package com.example.digitaldetox.domain.model

data class DailyUsageStat(
    val date: String,
    val totalFocusMinutes: Int = 0,
    val studyMinutes: Int = 0,
    val workMinutes: Int = 0,
    val totalAppUsageMinutes: Int = 0
)