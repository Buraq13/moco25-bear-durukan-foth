package com.example.fernfreunde.data.local.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fernfreunde.data.local.entities.PendingUpload
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingUploadDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPending(pending: PendingUpload)

    @Query("SELECT * FROM pending_uploads ORDER BY createdAt ASC")
    fun observeAllPending(): Flow<List<PendingUpload>>

    @Query("SELECT * FROM pending_uploads WHERE id = :id LIMIT 1")
    suspend fun getPendingById(id: String): PendingUpload?

    @Query("SELECT * FROM pending_uploads WHERE nextRetryAt IS NULL OR nextRetryAt <= :now ORDER BY createdAt ASC LIMIT 1")
    suspend fun getNextPending(now: Long): PendingUpload?

    @Delete
    suspend fun deletePending(pending: PendingUpload)

    @Query("DELETE FROM pending_uploads WHERE postLocalId = :localId")
    suspend fun deletePendingByPostLocalId(localId: String)

    @Query("UPDATE pending_uploads SET attempts = attempts + 1, nextRetryAt = :nextRetry WHERE id = :id")
    suspend fun incrementAttempts(id: String, nextRetry: Long)
}