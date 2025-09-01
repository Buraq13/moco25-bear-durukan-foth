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

    // ***************************************************************** //
    // INSERT CHALLENGES                                                 //
    // ***************************************************************** //

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(challenge: Challenge)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(challenges: List<Challenge>)

    // ***************************************************************** //
    // GET/OBSERVE CHALLENGES                                            //
    // ***************************************************************** //

    @Query("SELECT * FROM challenges WHERE challengeId = :id LIMIT 1")
    suspend fun getChallengeById(id: String): Challenge?

    @Query("SELECT * FROM challenges ORDER BY challengeId")
    fun observeAll(): Flow<List<Challenge>>
}

@Dao
interface DailyChallengeDao {

    // ***************************************************************** //
    // INSERT DAILYCHALLENGES                                            //
    // ***************************************************************** //

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(daily: DailyChallenge)

    // ***************************************************************** //
    // GET/OBSERVE DAILYCHALLENGES                                       //
    // ***************************************************************** //

    @Query("SELECT * FROM daily_challenges WHERE date = :date AND challengeId = :challengeId LIMIT 1")
    suspend fun getDailyChallengeByDateAndId(date: String, challengeId: String): DailyChallenge?

    @Query("SELECT * FROM daily_challenges WHERE date = :date ORDER BY startAt ASC LIMIT 1")
    suspend fun getDefaultForDate(date: String): DailyChallenge?

    // aktuelle Tageschallenge abrufen ---> für ViewModel
    @Query("SELECT * FROM daily_challenges WHERE date = :date LIMIT 1")
    fun observeDailyChallengeByDate(date: String): Flow<DailyChallenge?>

    // vergangene Tageschallenges abrufen ---> für Viewmodel (History Screen?)
    @Query("SELECT * FROM daily_challenges ORDER BY date DESC")
    fun observeRecentDailyChallenges(): Flow<List<DailyChallenge>>

    // ***************************************************************** //
    // DELETE DAILYCHALLENGES                                            //
    // ***************************************************************** //

    @Query("DELETE FROM daily_challenges WHERE date = :date AND challengeId = :challengeId")
    suspend fun delete(date: String, challengeId: String)
}