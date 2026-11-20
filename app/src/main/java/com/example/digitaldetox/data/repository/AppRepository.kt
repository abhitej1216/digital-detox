package com.example.digitaldetox.data.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import com.example.digitaldetox.data.AppDatabase
import com.example.digitaldetox.data.entity.AppEntity
import com.example.digitaldetox.domain.model.App
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepository @Inject constructor(
    private val database: AppDatabase,
    @ApplicationContext private val context: Context
) {
    suspend fun getBlockedApps(): List<App> {
        return database.appDao().getBlockedApps().first().map {
            App(it.packageName, it.label, it.blockEnabled, com.example.digitaldetox.domain.model.BlockType.valueOf(it.blockType.name))
        }
    }

    fun getBlockedAppsFlow(): Flow<List<App>> {
        return database.appDao().getBlockedApps().map { list ->
            list.map {
                App(it.packageName, it.label, it.blockEnabled, com.example.digitaldetox.domain.model.BlockType.valueOf(it.blockType.name))
            }
        }
    }

    suspend fun getAllApps(): List<App> {
        return database.appDao().getAllApps().first().map {
            App(it.packageName, it.label, it.blockEnabled, com.example.digitaldetox.domain.model.BlockType.valueOf(it.blockType.name))
        }
    }

    suspend fun toggleAppBlocked(packageName: String, enabled: Boolean) {
        database.appDao().updateBlockStatus(packageName, enabled)
    }

    suspend fun syncInstalledApps() {
        val pm = context.packageManager
        val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        
        // Get currently saved apps to preserve their blocked status
        val currentApps = database.appDao().getAllApps().first()
        val currentAppsMap = currentApps.associateBy { it.packageName }
        
        val appEntities = installedApps.mapNotNull { appInfo ->
            // Skip system apps
            if ((appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0) {
                return@mapNotNull null
            }
            val label = pm.getApplicationLabel(appInfo).toString()
            val existingApp = currentAppsMap[appInfo.packageName]
            
            AppEntity(
                packageName = appInfo.packageName,
                label = label,
                blockEnabled = existingApp?.blockEnabled ?: false,
                blockType = existingApp?.blockType ?: com.example.digitaldetox.data.entity.BlockType.SYSTEM
            )
        }
        database.appDao().insertApps(appEntities)
    }
}
