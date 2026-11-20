package com.example.digitaldetox.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.digitaldetox.data.dao.SettingsDao
import com.example.digitaldetox.data.entity.SettingsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val settingsDao: SettingsDao,
    private val dataStore: DataStore<Preferences>
) {
    suspend fun getString(key: String, defaultValue: String): String {
        return settingsDao.getStringValue(key) ?: defaultValue
    }
    
    suspend fun getInt(key: String, defaultValue: Int): Int {
        return settingsDao.getStringValue(key)?.toIntOrNull() ?: defaultValue
    }
    
    suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return settingsDao.getStringValue(key)?.toBoolean() ?: defaultValue
    }
    
    fun getBooleanFlow(key: String, defaultValue: Boolean): Flow<Boolean> {
        return settingsDao.getStringValueFlow(key).map { value ->
            value?.toBoolean() ?: defaultValue
        }
    }
    
    suspend fun saveString(key: String, value: String) {
        settingsDao.insert(SettingsEntity(key, value))
    }
    
    suspend fun saveInt(key: String, value: Int) {
        settingsDao.insert(SettingsEntity(key, value.toString()))
    }
    
    suspend fun saveBoolean(key: String, value: Boolean) {
        settingsDao.insert(SettingsEntity(key, value.toString()))
    }
}
