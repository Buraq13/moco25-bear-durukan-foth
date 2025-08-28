package com.example.fernfreunde

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fernfreunde.data.local.daos.ChallengeDao
import com.example.fernfreunde.data.local.daos.DailyChallengeDao
import com.example.fernfreunde.data.local.daos.ParticipationDao
import com.example.fernfreunde.data.local.daos.PostDao
import com.example.fernfreunde.data.local.daos.UserDao
import com.example.fernfreunde.data.local.database.AppDatabase
import com.example.fernfreunde.data.local.entities.Challenge
import com.example.fernfreunde.data.local.entities.DailyChallenge
import com.example.fernfreunde.data.local.entities.User
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.format.DateTimeFormatter


/**
 * Instrumentation tests for Post backend (Room-only).
 * - Uses in-memory Room DB (disappears after db.close()).
 * - Tests createPostLocalOnly, participation and counts.
 */
@RunWith(AndroidJUnit4::class)
class PostBackendTest {

    private lateinit var db: AppDatabase
    private lateinit var postDao: PostDao
    private lateinit var participationDao: ParticipationDao
    private lateinit var challengeDao: ChallengeDao
    private lateinit var dailyChallengeDao: DailyChallengeDao
    private lateinit var userDao: UserDao
    private lateinit var repo: TestPostRepository

    @Before
    fun setup() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(ctx, AppDatabase::class.java)
            .allowMainThreadQueries() // only for tests - simplifies assertions
            .build()
        postDao = db.postDao()
        participationDao = db.participationDao()
        dailyChallengeDao = db.dailyChallengeDao()
        challengeDao = db.challengeDao()
        userDao = db.userDao()
        repo = TestPostRepository(postDao, dailyChallengeDao, participationDao)
    }

    @After
    fun teardown() {
        db.close() // in-memory DB is destroyed
    }

    @Test
    fun createPost_createsPostAndParticipation_and_respectsLimit() = runBlocking {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        val challengeId = "test-ch-1"

        val challenge = Challenge(
            challengeId = challengeId,
            title = "Test Challenge",
            text = "Test"
        )
        challengeDao.insert(challenge)

        // create a default daily challenge for today (maxPostsPerUser = 1)
        val daily = DailyChallenge(
            date = today,
            challengeId = challengeId,
            startAt = System.currentTimeMillis(),
            expiresAt = System.currentTimeMillis() + 24 * 60 * 60 * 1000,
            maxPostsPerUser = 1
        )
        dailyChallengeDao.insert(daily)

        val userId = "test-user-1"

        // WICHTIG: minimalen User anlegen, damit FK auf users.userId existiert
        val user = User(
            userId = userId,
            displayName = "Tester",
            username = "tester1",
            profileImageUrl = null,
            bio = null,
            createdAt = System.currentTimeMillis()
        )
        userDao.upsertUser(user)   // << neu: jetzt existiert der Parent-User

        // create first post -> should succeed
        val postId = repo.createPostLocalOnly(
            userId = userId,
            userName = "Tester",
            date = today,
            challengeId = challengeId,
            description = "Hallo Welt",
            mediaLocalPath = null
        )
        val post = postDao.getPostSync(postId)
        assertNotNull("Post should be present in DB after creation", post)

        val part = participationDao.getParticipationSync(userId, today, challengeId)
        assertNotNull("Participation should be created", part)

        val count = postDao.countPostsForUserInChallenge(userId, today, challengeId)
        assertEquals(1, count)

        // creating second post -> should throw (limit = 1)
        var threw = false
        try {
            repo.createPostLocalOnly(
                userId = userId,
                userName = "Tester",
                date = today,
                challengeId = challengeId,
                description = "Zweiter Post",
                mediaLocalPath = null
            )
        } catch (e: IllegalStateException) {
            threw = true
        }
        assertTrue("Second post should be blocked by maxPostsPerUser=1", threw)
    }

    @Test
    fun allowMultiplePosts_when_maxPostsIsNull() = runBlocking {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        val challengeId = "test-ch-multi"

        val challenge = Challenge(
            challengeId = challengeId,
            title = "Test Challenge",
            text = "Test"
        )
        challengeDao.insert(challenge)

        val daily = DailyChallenge(
            date = today,
            challengeId = challengeId,
            startAt = System.currentTimeMillis(),
            expiresAt = System.currentTimeMillis() + 24 * 60 * 60 * 1000,
            maxPostsPerUser = null // unlimited
        )
        dailyChallengeDao.insert(daily)

        val userId = "multi-user"

        val user = User(
            userId = userId,
            displayName = "Multi",
            username = "Multi",
            profileImageUrl = null,
            bio = null,
            createdAt = System.currentTimeMillis()
        )
        userDao.upsertUser(user)

        val firstId = repo.createPostLocalOnly(userId, "Multi", today, challengeId, "p1", null)
        val secondId = repo.createPostLocalOnly(userId, "Multi", today, challengeId, "p2", null)
        assertNotNull(postDao.getPostSync(firstId))
        assertNotNull(postDao.getPostSync(secondId))
        assertEquals(2, postDao.countPostsForUserInChallenge(userId, today, challengeId))
    }
}
