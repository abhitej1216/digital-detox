package com.example.digitaldetox.service

import android.accessibilityservice.AccessibilityService
import android.content.pm.ApplicationInfo
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import com.example.digitaldetox.data.repository.AppRepository
import com.example.digitaldetox.data.repository.SettingsRepository
import com.example.digitaldetox.presentation.intervention.MindfulInterventionActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class AppBlockingService : AccessibilityService() {

    @Inject
    lateinit var appRepository: AppRepository

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var blockedPackageNames: Set<String> = emptySet()
    private var isNightModeEnabled: Boolean = false
    private var isOneSecEnabled: Boolean = true

    private var lastInterceptedPackage: String? = null
    private var lastInterceptTime: Long = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        serviceScope.launch {
            val initialBlockedApps = appRepository.getBlockedApps()
            blockedPackageNames = initialBlockedApps.map { it.packageName }.toSet()

            appRepository.getBlockedAppsFlow().collect { apps ->
                blockedPackageNames = apps.map { it.packageName }.toSet()
            }
        }
        serviceScope.launch {
            settingsRepository.getBooleanFlow("night_mode", false).collect { enabled ->
                isNightModeEnabled = enabled
            }
        }
        serviceScope.launch {
            settingsRepository.getBooleanFlow("one_sec_enabled", true).collect { enabled ->
                isOneSecEnabled = enabled
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        if ((event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) && (event.packageName != null)) {
            val packageName = event.packageName.toString()
            if (packageName == this.packageName) return

            // Avoid redundant rapid events within 1.5 seconds for the same package
            val now = System.currentTimeMillis()
            if (packageName == lastInterceptedPackage && (now - lastInterceptTime) < 1500L) {
                return
            }

            // Check if user has an active 5-minute temporary mindful pass
            if (MindfulInterventionManager.hasTemporaryAccess(packageName)) {
                return
            }

            val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val isNightTime = currentHour >= 22 || currentHour < 7

            val isUserBlocked = blockedPackageNames.contains(packageName)
            val isNightModeActive = isNightModeEnabled && isNightTime
            val isNonSystemApp = !isSystemApp(packageName)

            if (isUserBlocked || (isNightModeActive && isNonSystemApp)) {
                lastInterceptedPackage = packageName
                lastInterceptTime = now

                val appLabel = try {
                    val appInfo = packageManager.getApplicationInfo(packageName, 0)
                    packageManager.getApplicationLabel(appInfo).toString()
                } catch (e: Exception) {
                    packageName
                }

                if (isOneSecEnabled) {
                    // One Sec Mindful Guided Intervention
                    MindfulInterventionActivity.start(this, packageName, appLabel)
                } else {
                    // Standard abrupt block
                    performGlobalAction(GLOBAL_ACTION_HOME)
                    val toastMessage = if (isNightModeActive && isNonSystemApp && !isUserBlocked) {
                        "$appLabel is blocked during Nighttime Detox!"
                    } else {
                        "$appLabel is currently blocked by Digital Detox!"
                    }
                    Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun isSystemApp(packageName: String): Boolean {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        } catch (e: Exception) {
            false
        }
    }

    override fun onInterrupt() {
        // Required override for AccessibilityService
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
