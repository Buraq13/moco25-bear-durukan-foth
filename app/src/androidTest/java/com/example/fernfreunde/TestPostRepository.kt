package com.example.fernfreunde

import com.example.fernfreunde.data.local.daos.DailyChallengeDao
import com.example.fernfreunde.data.local.daos.ParticipationDao
import com.example.fernfreunde.data.local.daos.PostDao
import com.example.fernfreunde.data.local.entities.Participation
import com.example.fernfreunde.data.local.entities.Post
import com.example.fernfreunde.data.mappers.SyncStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

/**
 * Test-only repository: implementiert die Kernlogik, aber ohne WorkManager/Firebase/Auth.
 * - createPostLocalOnly(...) erzeugt Post + Participation in Room.
 * - canCreatePost(...) prüft maxPostsPerUser (DailyChallenge.maxPostsPerUser, default 1).
 *
 * Diese Klasse nutzt deine realen DAOs, deshalb testet sie echte DB-Interaktionen.
 */
class TestPostRepository(
    private val postDao: PostDao,
    private val dailyChallengeDao: DailyChallengeDao,
    private val participationDao: ParticipationDao
) {

    suspend fun canCreatePost(userId: String, date: String, challengeId: String?): Boolean = withContext(Dispatchers.IO) {
        val daily = if (challengeId != null) {
            dailyChallengeDao.getCached(date, challengeId)
        } else {
            dailyChallengeDao.getDefaultForDate(date)
        }
        val maxAllowed = daily?.maxPostsPerUser ?: 1
        if (maxAllowed == null) return@withContext true
        val currentCount = postDao.countPostsForUserAndChallenge(userId, date, challengeId)
        currentCount < maxAllowed
    }

    /**
     * Erzeugt nur lokal einen Post sowie Participation (offline-first).
     * Liefert die lokale postId zurück.
     * Wirft IllegalStateException, falls Limit erreicht.
     */
    suspend fun createPostLocalOnly(
        userId: String,
        userName: String?,
        date: String,          // yyyy-MM-dd
        challengeId: String?,
        description: String?,
        mediaLocalPath: String?
    ): String = withContext(Dispatchers.IO) {
        if (!canCreatePost(userId, date, challengeId)) throw IllegalStateException("User already participated")

        val postId = UUID.randomUUID().toString()
        val post = Post(
            localId = postId,
            remoteId = null,
            userId = userId,
            userName = userName,
            challengeId = challengeId,
            challengeDate = date,
            description = description,
            mediaLocalPath = mediaLocalPath,
            mediaRemoteUrl = null,
            createdAtClient = System.currentTimeMillis(),
            createdAtServer = null,
            syncStatus = SyncStatus.PENDING
        )
        postDao.insert(post)

        val did = challengeId ?: (dailyChallengeDao.getDefaultForDate(date)?.challengeId ?: "unknown")
        val participation = Participation(
            userId = userId,
            date = date,
            challengeId = did,
            postLocalId = postId,
            createdAtClient = System.currentTimeMillis()
        )
        participationDao.insertParticipation(participation)

        postId
    }
}
