package com.example.digitaldetox.domain.model

data class BlockedSession(
    val id: Long = 0,
    val packageName: String,
    val startTime: Long,
    val endTime: Long,
    val timestamp: Long = System.currentTimeMillis()
)