package com.example.fernfreunde.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fernfreunde.data.local.entities.Participation
import kotlinx.coroutines.flow.Flow

@Dao
interface ParticipationDao {

    @Insert(onConflict = OnConflictStrategy.ABORT) // will throw if duplicate
    suspend fun insertParticipation(participation: Participation)

    @Query("SELECT * FROM participations WHERE userId = :userId AND date = :date LIMIT 1")
    fun observeParticipation(userId: String, date: String): Flow<Participation?>

    @Query("SELECT * FROM participations WHERE userId = :userId AND date = :date AND challengeId = :challengeId LIMIT 1")
    suspend fun getParticipationSync(userId: String, date: String, challengeId: String): Participation?

    @Query("DELETE FROM participations WHERE userId = :userId AND date = :date")
    suspend fun deleteParticipation(userId: String, date: String)
}