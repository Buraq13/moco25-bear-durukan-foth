package com.example.fernfreunde.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fernfreunde.data.local.entities.Settings
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: Settings)

    @Query("SELECT * FROM settings WHERE userId = :userId LIMIT 1")
    fun observeSettings(userId: String): Flow<Settings?>

    @Query("SELECT * FROM settings WHERE userId = :userId LIMIT 1")
    suspend fun getSettingsSync(userId: String): Settings?

}