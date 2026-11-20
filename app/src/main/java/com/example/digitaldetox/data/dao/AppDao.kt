package com.example.digitaldetox.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.digitaldetox.data.entity.AppEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    @Query("SELECT * FROM apps WHERE blockEnabled = 1")
    fun getBlockedApps(): Flow<List<AppEntity>>

    @Query("SELECT * FROM apps")
    fun getAllApps(): Flow<List<AppEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApp(app: AppEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApps(apps: List<AppEntity>)

    @Query("UPDATE apps SET blockEnabled = :enabled WHERE packageName = :packageName")
    suspend fun updateBlockStatus(packageName: String, enabled: Boolean)

    @Query("DELETE FROM apps")
    suspend fun clearAll()
}