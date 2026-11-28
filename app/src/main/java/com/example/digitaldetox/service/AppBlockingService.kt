package com.example.digitaldetox.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import com.example.digitaldetox.data.repository.AppRepository
import com.example.digitaldetox.data.repository.SettingsRepository
import com.example.digitaldetox.presentation.intervention.CooldownActivity
import com.example.digitaldetox.presentation.intervention.MindfulInterventionActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
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
    private var dailyOpenLimit: Int = 5
    private var microSessionMinutes: Int = 5
    private var cooldownMinutes: Int = 30

    private var currentForegroundPackage: String? = null
    private var lastInterceptedPackage: String? = null
    private var lastInterceptTime: Long = 0L

    private var sessionWatcherJob: Job? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        MindfulInterventionManager.init(applicationContext)
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
        serviceScope.launch {
            settingsRepository.getIntFlow("daily_open_limit", 5).collect { limit ->
                dailyOpenLimit = limit
            }
        }
        serviceScope.launch {
            settingsRepository.getIntFlow("micro_session_minutes", 5).collect { mins ->
                microSessionMinutes = mins
            }
        }
        serviceScope.launch {
            settingsRepository.getIntFlow("cooldown_minutes", 30).collect { mins ->
                cooldownMinutes = mins
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        if ((event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) && (event.packageName != null)) {
            val packageName = event.packageName.toString()
            if (packageName == this.packageName) return
            currentForegroundPackage = packageName

            // Avoid redundant rapid events within 1.5 seconds for the same package
            val now = System.currentTimeMillis()
            if (packageName == lastInterceptedPackage && (now - lastInterceptTime) < 1500L) {
                return
            }

            val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val isNightTime = currentHour >= 22 || currentHour < 7

            val isUserBlocked = blockedPackageNames.contains(packageName)
            val isNightModeActive = isNightModeEnabled && isNightTime
            val isBlockableNightApp = !isSystemApp(packageName) && !isHomeLauncher(packageName)

            if (isUserBlocked || (isNightModeActive && isBlockableNightApp)) {
                val appLabel = try {
                    val appInfo = packageManager.getApplicationInfo(packageName, 0)
                    packageManager.getApplicationLabel(appInfo).toString()
                } catch (e: Exception) {
                    packageName
                }

                // Check Micro-Session & Cooldown Status
                val accessStatus = MindfulInterventionManager.checkAccessStatus(packageName, dailyOpenLimit)
                when (accessStatus) {
                    AccessStatus.ACTIVE_SESSION -> {
                        // User is within their allowed micro-session window!
                        // Monitor for when the session timer reaches zero
                        watchSessionExpiration(packageName, appLabel)
                        return
                    }

                    AccessStatus.IN_COOLDOWN -> {
                        // Mandatory cooldown window is active!
                        lastInterceptedPackage = packageName
                        lastInterceptTime = now
                        performGlobalAction(GLOBAL_ACTION_HOME)
                        CooldownActivity.start(
                            context = this,
                            packageName = packageName,
                            appLabel = appLabel,
                            isDailyLimit = false
                        )
                    }

                    AccessStatus.DAILY_LIMIT_REACHED -> {
                        // Daily open quota exhausted!
                        lastInterceptedPackage = packageName
                        lastInterceptTime = now
                        performGlobalAction(GLOBAL_ACTION_HOME)
                        CooldownActivity.start(
                            context = this,
                            packageName = packageName,
                            appLabel = appLabel,
                            isDailyLimit = true
                        )
                    }

                    AccessStatus.ALLOWED_TO_REQUEST -> {
                        lastInterceptedPackage = packageName
                        lastInterceptTime = now

                        if (isOneSecEnabled) {
                            // "One Sec" Mindful Intervention Guided Breath
                            MindfulInterventionActivity.start(this, packageName, appLabel)
                        } else {
                            // Standard abrupt block
                            performGlobalAction(GLOBAL_ACTION_HOME)
                            val toastMessage = if (isNightModeActive && isBlockableNightApp && !isUserBlocked) {
                                "$appLabel is blocked during Nighttime Detox!"
                            } else {
                                "$appLabel is currently blocked by Digital Detox!"
                            }
                            Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun watchSessionExpiration(packageName: String, appLabel: String) {
        sessionWatcherJob?.cancel()
        sessionWatcherJob = serviceScope.launch {
            val remainingMs = MindfulInterventionManager.getRemainingSessionMillis(packageName)
            if (remainingMs > 0) {
                delay(remainingMs)
            }
            // If the user is still in the app when the session expires:
            if (currentForegroundPackage == packageName) {
                performGlobalAction(GLOBAL_ACTION_HOME)
                CooldownActivity.start(
                    context = this@AppBlockingService,
                    packageName = packageName,
                    appLabel = appLabel,
                    isDailyLimit = false
                )
            }
        }
    }

    private fun isHomeLauncher(packageName: String): Boolean {
        return try {
            val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
            val resolveInfo = packageManager.resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
            resolveInfo?.activityInfo?.packageName == packageName
        } catch (e: Exception) {
            false
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
        sessionWatcherJob?.cancel()
        serviceScope.cancel()
    }
}
