package com.example.fernfreunde.data.remote.dataSources

import com.example.fernfreunde.data.local.entities.DailyChallenge
import com.example.fernfreunde.data.other.Constants.DAILY_CHALLENGE_COLLECTION
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirestoreDailyChallengeDataSource {

    private val firestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private val dailyChallengesCollection = firestore.collection(DAILY_CHALLENGE_COLLECTION)

    suspend fun getCurrentDailyChallenge(): DailyChallenge? = withContext(Dispatchers.IO) {
        val currentTime = System.currentTimeMillis()

        try {
            val snapshot = dailyChallengesCollection
                .whereLessThanOrEqualTo("startAt", currentTime)
                .orderBy("startAt", Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .await()

            val matchingChallenge = snapshot.documents.mapNotNull { document ->
                val startAt = document.getLong("startAt")
                val expiresAt = document.getLong("expiresAt")

                if (startAt != null && expiresAt != null && expiresAt > currentTime) {
                    DailyChallenge(
                        challengeId = document.id,
                        date = document.getString("date") ?: "",
                        title = document.getString("title") ?: "",
                        text = document.getString("text") ?: "",
                        startAt = startAt,
                        expiresAt = expiresAt,
                        maxPostsPerUser = document.getLong("maxPostsPerUser")?.toInt()
                    )
                } else null
            }

            val chosenChallenge = matchingChallenge
                .sortedWith(compareByDescending<DailyChallenge> { it.startAt }.thenBy { it.challengeId })
                .firstOrNull()

            if (chosenChallenge != null) {
                return@withContext chosenChallenge
            }
            return@withContext null

        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }
}