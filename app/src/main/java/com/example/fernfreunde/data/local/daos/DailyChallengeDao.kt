package com.example.fernfreunde.data.local.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.fernfreunde.data.local.entities.DailyChallenge

@Dao
interface DailyChallengeDao {

    // ***************************************************************** //
    // INSERT DAILYCHALLENGES                                            //
    // ***************************************************************** //

    @Upsert
    suspend fun upsert(daily: DailyChallenge)

    // ***************************************************************** //
    // GET/OBSERVE DAILYCHALLENGES                                       //
    // ***************************************************************** //

    @Query("SELECT * FROM daily_challenges ORDER BY startAt DESC LIMIT 1")
    suspend fun getCached(): DailyChallenge?
}