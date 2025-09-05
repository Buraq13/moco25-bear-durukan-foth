package com.example.fernfreunde.data.repositories

import android.util.Log
import com.example.fernfreunde.data.local.daos.DailyChallengeDao
import com.example.fernfreunde.data.local.entities.DailyChallenge
import com.example.fernfreunde.data.remote.dataSources.FirestoreDailyChallengeDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DailyChallengeRepository @Inject constructor(
    private val firestoreDailyChallengeDataSource: FirestoreDailyChallengeDataSource,
    private val dailyChallengeDao: DailyChallengeDao
    ) {

    private val TAG = "DailyChallengeRepository"

    suspend fun getCurrentDailyChallenge(): DailyChallenge? = withContext(Dispatchers.IO) {

        val cached = dailyChallengeDao.getCached()
        if (cached != null && cached.expiresAt > System.currentTimeMillis()) {
            Log.i(TAG, "returning cached challenge id=${cached.challengeId}")
            return@withContext cached
        }

        val dailyChallenge = firestoreDailyChallengeDataSource.getCurrentDailyChallenge()
        if (dailyChallenge != null) {
            dailyChallengeDao.upsert(dailyChallenge)
            Log.i(TAG, "fetched & cached challenge id=${dailyChallenge.challengeId}")
        } else {
            Log.i(TAG, "no challenge fetched from Firestore")
        }

        return@withContext dailyChallenge
    }

    suspend fun getCurrentChallengeId(): String? = getCurrentDailyChallenge()?.challengeId

    // optional: noch Methode hinzufügen, um vergangene Challenges zu holen, z.B. für Rückblick
    // aber brauchen wir glaube ich nicht

    suspend fun debug(): Unit = withContext(Dispatchers.IO) {
        firestoreDailyChallengeDataSource.debugDumpAllDailyChallenges()
    }
}