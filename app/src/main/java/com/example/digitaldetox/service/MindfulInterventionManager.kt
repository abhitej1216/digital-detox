package com.example.digitaldetox.service

import java.util.concurrent.ConcurrentHashMap

/**
 * Manages active temporary passes granted after a mindful intervention session.
 * For example: granting 5 minutes of intentional access to an app before the intervention kicks in again.
 */
object MindfulInterventionManager {

    private val temporaryAccessMap = ConcurrentHashMap<String, Long>()

    /**
     * Grant temporary access to [packageName] for [durationMinutes].
     */
    fun grantTemporaryAccess(packageName: String, durationMinutes: Int = 5) {
        val expiresAt = System.currentTimeMillis() + (durationMinutes * 60 * 1000L)
        temporaryAccessMap[packageName] = expiresAt
    }

    /**
     * Check if [packageName] currently has an active temporary pass.
     */
    fun hasTemporaryAccess(packageName: String): Boolean {
        val expiresAt = temporaryAccessMap[packageName] ?: return false
        return if (System.currentTimeMillis() < expiresAt) {
            true
        } else {
            temporaryAccessMap.remove(packageName)
            false
        }
    }

    /**
     * Revoke any granted temporary access for [packageName].
     */
    fun revokeAccess(packageName: String) {
        temporaryAccessMap.remove(packageName)
    }

    /**
     * Returns remaining temporary access time in seconds, or 0 if expired/not granted.
     */
    fun getRemainingAccessSeconds(packageName: String): Long {
        val expiresAt = temporaryAccessMap[packageName] ?: return 0L
        val diff = expiresAt - System.currentTimeMillis()
        return if (diff > 0) diff / 1000L else 0L
    }
}
