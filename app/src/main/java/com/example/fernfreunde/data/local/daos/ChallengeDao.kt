package com.example.fernfreunde.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fernfreunde.data.local.entities.Challenge
import com.example.fernfreunde.data.local.entities.DailyChallenge
import kotlinx.coroutines.flow.Flow

@Dao
interface ChallengeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(challenge: Challenge)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(challenges: List<Challenge>)

    @Query("SELECT * FROM challenges WHERE challengeId = :id LIMIT 1")
    suspend fun getChallengeById(id: String): Challenge?
}

@Dao
interface DailyChallengeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(daily: DailyChallenge)

    // aktuelle Tageschallenge abrufen ---> für ViewModel
    @Query("SELECT * FROM daily_challenges WHERE date = :date LIMIT 1")
    fun observeDailyChallenge(date: String): Flow<DailyChallenge?>

    // vergangene Tagescahllenges abrufen ---> für Viewmodel (History Screen?)
    @Query("SELECT * FROM daily_challenges ORDER BY date DESC")
    fun observeRecentDailyChallenges(): Flow<List<DailyChallenge>>
}