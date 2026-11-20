package com.example.digitaldetox.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_usage_stats")
data class DailyUsageStatEntity(
    @PrimaryKey
    val date: String,
    val totalFocusMinutes: Int = 0,
    val studyMinutes: Int = 0,
    val workMinutes: Int = 0,
    val totalAppUsageMinutes: Int = 0
)