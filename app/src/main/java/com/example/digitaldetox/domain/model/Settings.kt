package com.example.digitaldetox.domain.model

data class Settings(
    val studyTargetMinutes: Int = 240,
    val workTargetMinutes: Int = 240,
    val nighttimeDetoxEnabled: Boolean = true,
    val nightStartTime: String = "22:00",
    val nightEndTime: String = "06:00"
)