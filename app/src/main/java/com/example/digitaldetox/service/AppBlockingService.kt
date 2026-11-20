package com.example.digitaldetox.service

import android.accessibilityservice.AccessibilityService
import android.content.pm.ApplicationInfo
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import com.example.digitaldetox.data.repository.AppRepository
import com.example.digitaldetox.data.repository.SettingsRepository
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
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        if ((event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) && (event.packageName != null)) {
            val packageName = event.packageName.toString()
            if (packageName == this.packageName) return

            val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val isNightTime = currentHour >= 22 || currentHour < 7

            val isUserBlocked = blockedPackageNames.contains(packageName)
            val isNightModeActive = isNightModeEnabled && isNightTime
            val isNonSystemApp = !isSystemApp(packageName)

            if (isUserBlocked || (isNightModeActive && isNonSystemApp)) {
                performGlobalAction(GLOBAL_ACTION_HOME)
                val appLabel = try {
                    val appInfo = packageManager.getApplicationInfo(packageName, 0)
                    packageManager.getApplicationLabel(appInfo).toString()
                } catch (e: Exception) {
                    packageName
                }
                val toastMessage = if (isNightModeActive && isNonSystemApp && !isUserBlocked) {
                    "$appLabel is blocked during Nighttime Detox!"
                } else {
                    "$appLabel is currently blocked by Digital Detox!"
                }
                Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show()
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
