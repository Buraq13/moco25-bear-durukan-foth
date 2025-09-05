package com.example.fernfreunde.data.remote.dataSources

import android.util.Log
import com.example.fernfreunde.data.local.entities.ChallengeType
import com.example.fernfreunde.data.local.entities.DailyChallenge
import com.example.fernfreunde.data.other.Constants.DAILY_CHALLENGE_COLLECTION
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.max

class FirestoreDailyChallengeDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {
    private val dailyChallengesCollection = firestore.collection(DAILY_CHALLENGE_COLLECTION)

    private val TAG = "FirestoreDailyChallengeDS"

    suspend fun getCurrentDailyChallenge(): DailyChallenge? = withContext(Dispatchers.IO) {
        val currentTime = System.currentTimeMillis()

        try {
            val snapshot = dailyChallengesCollection
                .whereLessThanOrEqualTo("startAt", currentTime)
                .orderBy("startAt", Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .await()

            Log.i(TAG, "Firestore snapshot size=${snapshot.size()} ids=${snapshot.documents.map { it.id }}")

            val matchingChallenge = snapshot.documents.mapNotNull { document ->

                try {
                    val startAt: Long? = document.getLong("startAt")
                        ?: document.getTimestamp("startAt")?.toDate()?.time
                    val expiresAt: Long? = document.getLong("expiresAt")
                        ?: document.getTimestamp("expiresAt")?.toDate()?.time

                    if (startAt == null || expiresAt == null) {
                        Log.w(TAG, "doc ${document.id}: missing startAt/expiresAt -> skipping (startAt=$startAt, expiresAt=$expiresAt)")
                        return@mapNotNull null
                    }

                    val challengeTypeString = document.getString("challengeType")
                    val challengeType = if (challengeTypeString != null) {
                        ChallengeType.valueOf(challengeTypeString)
                    } else {
                        Log.w(TAG, "doc ${document.id}: unknown challengeType='$challengeTypeString' -> using ANY")
                        ChallengeType.ANY
                    }

                    val maxPostsAny = document.get("maxPostsPerUser")
                    val maxPostsInt: Int? = when (maxPostsAny) {
                        is Long -> maxPostsAny.toInt()
                        is Double -> maxPostsAny.toInt()
                        is Int -> maxPostsAny
                        else -> null
                    }

                    if (expiresAt > currentTime) {
                        DailyChallenge(
                            challengeId = document.id,
                            date = document.getString("date") ?: "",
                            title = document.getString("title") ?: "",
                            text = document.getString("text") ?: "",
                            challengeType = challengeType,
                            startAt = startAt,
                            expiresAt = expiresAt,
                            maxPostsPerUser = maxPostsInt
                        )
                    } else null
                } catch (e:Exception) {
                    Log.w(TAG, "Failed to map doc ${document.id}: ${e.localizedMessage}", e)
                    null
                }
            }
            val chosenChallenge = matchingChallenge
                .sortedWith(compareByDescending<DailyChallenge> { it.startAt }.thenBy { it.challengeId })
                .firstOrNull()

            if (chosenChallenge != null) {
                return@withContext chosenChallenge
            }
            return@withContext null

        } catch (e: Exception) {
            Log.e(TAG, "Firestore query failed: ${e.localizedMessage}", e)
            // e.printStackTrace()
            return@withContext null
        }
    }

    suspend fun debugDumpAllDailyChallenges(): Unit = withContext(Dispatchers.IO) {
        try {
            val snapAll = dailyChallengesCollection.get().await()
            Log.i("DEBUG-FS", "ALL DOCS size=${snapAll.size()} ids=${snapAll.documents.map { it.id }}")
            snapAll.documents.forEach { doc ->
                Log.i("DEBUG-FS", "DOC ${doc.id} raw=${doc.data}")
                doc.data?.forEach { (k, v) ->
                    Log.i("DEBUG-FS", "  field: $k -> ${v?.javaClass?.name} = $v")
                }
            }
        } catch (e: Exception) {
            Log.e("DEBUG-FS", "getAll failed: ${e.localizedMessage}", e)
        }
    }
}