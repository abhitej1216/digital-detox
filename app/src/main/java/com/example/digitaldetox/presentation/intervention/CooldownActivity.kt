package com.example.digitaldetox.presentation.intervention

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.example.digitaldetox.data.repository.SettingsRepository
import com.example.digitaldetox.ui.theme.DigitalDetoxTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CooldownActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME) ?: ""
        val appLabel = intent.getStringExtra(EXTRA_APP_LABEL) ?: "this app"
        val isDailyLimit = intent.getBooleanExtra(EXTRA_IS_DAILY_LIMIT, false)

        lifecycleScope.launch {
            val microSessionMinutes = settingsRepository.getInt("micro_session_minutes", 5)
            val cooldownMinutes = settingsRepository.getInt("cooldown_minutes", 30)
            val dailyLimit = settingsRepository.getInt("daily_open_limit", 5)

            setContent {
                DigitalDetoxTheme {
                    CooldownScreen(
                        packageName = packageName,
                        appLabel = appLabel,
                        isDailyLimit = isDailyLimit,
                        microSessionMinutes = microSessionMinutes,
                        cooldownTotalMinutes = cooldownMinutes,
                        dailyLimit = dailyLimit,
                        onCloseToHome = {
                            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                                addCategory(Intent.CATEGORY_HOME)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            startActivity(homeIntent)
                            finish()
                        }
                    )
                }
            }
        }
    }

    companion object {
        const val EXTRA_PACKAGE_NAME = "extra_package_name"
        const val EXTRA_APP_LABEL = "extra_app_label"
        const val EXTRA_IS_DAILY_LIMIT = "extra_is_daily_limit"

        fun start(
            context: Context,
            packageName: String,
            appLabel: String,
            isDailyLimit: Boolean = false
        ) {
            val intent = Intent(context, CooldownActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NO_ANIMATION)
                putExtra(EXTRA_PACKAGE_NAME, packageName)
                putExtra(EXTRA_APP_LABEL, appLabel)
                putExtra(EXTRA_IS_DAILY_LIMIT, isDailyLimit)
            }
            context.startActivity(intent)
        }
    }
}
