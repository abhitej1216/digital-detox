package com.example.digitaldetox.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.digitaldetox.data.dao.AppDao
import com.example.digitaldetox.data.dao.BlockedSessionDao
import com.example.digitaldetox.data.dao.FocusSessionDao
import com.example.digitaldetox.data.dao.DailyUsageStatDao
import com.example.digitaldetox.data.dao.SettingsDao
import com.example.digitaldetox.data.entity.AppEntity
import com.example.digitaldetox.data.entity.BlockedSessionEntity
import com.example.digitaldetox.data.entity.FocusSessionEntity
import com.example.digitaldetox.data.entity.DailyUsageStatEntity
import com.example.digitaldetox.data.entity.SettingsEntity

@Database(
    entities = [
        AppEntity::class,
        BlockedSessionEntity::class,
        FocusSessionEntity::class,
        DailyUsageStatEntity::class,
        SettingsEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
    abstract fun blockedSessionDao(): BlockedSessionDao
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun dailyUsageStatDao(): DailyUsageStatDao
    abstract fun settingsDao(): SettingsDao
}