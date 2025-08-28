package com.example.fernfreunde

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fernfreunde.data.local.daos.FriendshipDao
import com.example.fernfreunde.data.local.daos.UserDao
import com.example.fernfreunde.data.local.database.AppDatabase
import com.example.fernfreunde.data.local.entities.Friendship
import com.example.fernfreunde.data.local.entities.FriendshipStatus
import com.example.fernfreunde.data.local.entities.User
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FriendshipBackendTest {

    private lateinit var db: AppDatabase
    private lateinit var userDao: UserDao
    private lateinit var friendshipDao: FriendshipDao

    @Before
    fun setup() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(ctx, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        userDao = db.userDao()
        friendshipDao = db.friendshipDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun addAndRemoveFriend_works() = runBlocking {

        val emily = User(
            userId = "emily",
            displayName = "Emily",
            username = "emily",
            profileImageUrl = null,
            bio = null,
            createdAt = System.currentTimeMillis()
        )
        userDao.upsert(emily)

        val brianna = User(
            userId = "brianna",
            displayName = "Brianna",
            username = "brianna",
            profileImageUrl = null,
            bio = null,
            createdAt = System.currentTimeMillis()
        )
        userDao.upsert(emily)

        // Add friendship
        val f = Friendship(userIdA = "emily", userIdB = "brianna", status = FriendshipStatus.ACCEPTED, createdAt = System.currentTimeMillis())
        friendshipDao.insert(f)

        val friendIds = friendshipDao.getFriendIdsForUser("emily").first()
        assertTrue("brianna should be in emily's friend list", friendIds.contains("brianna"))

        // Remove
        friendshipDao.deleteFriendship("emily", "brianna")
        val after = friendshipDao.getFriendIdsForUser("emily")
        assertFalse(after.contains("brianna"))
    }
}
