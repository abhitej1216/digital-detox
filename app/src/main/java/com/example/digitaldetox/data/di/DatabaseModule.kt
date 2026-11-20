package com.example.digitaldetox.data.di

import android.content.Context
import androidx.room.Room
import com.example.digitaldetox.data.AppDatabase
import com.example.digitaldetox.data.dao.AppDao
import com.example.digitaldetox.data.dao.BlockedSessionDao
import com.example.digitaldetox.data.dao.DailyUsageStatDao
import com.example.digitaldetox.data.dao.FocusSessionDao
import com.example.digitaldetox.data.dao.SettingsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "digital_detox_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideAppDao(database: AppDatabase): AppDao {
        return database.appDao()
    }

    @Provides
    fun provideBlockedSessionDao(database: AppDatabase): BlockedSessionDao {
        return database.blockedSessionDao()
    }

    @Provides
    fun provideFocusSessionDao(database: AppDatabase): FocusSessionDao {
        return database.focusSessionDao()
    }

    @Provides
    fun provideDailyUsageStatDao(database: AppDatabase): DailyUsageStatDao {
        return database.dailyUsageStatDao()
    }

    @Provides
    fun provideSettingsDao(database: AppDatabase): SettingsDao {
        return database.settingsDao()
    }
}
