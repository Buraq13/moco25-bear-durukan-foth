package com.example.fernfreunde.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.fernfreunde.data.local.daos.DailyChallengeDao
import com.example.fernfreunde.data.local.daos.FriendshipDao
import com.example.fernfreunde.data.local.daos.PendingUploadDao
import com.example.fernfreunde.data.local.daos.PostDao
import com.example.fernfreunde.data.local.daos.SettingsDao
import com.example.fernfreunde.data.local.daos.UserDao
import com.example.fernfreunde.data.local.entities.DailyChallenge
import com.example.fernfreunde.data.local.entities.Friendship
import com.example.fernfreunde.data.local.entities.PendingUpload
import com.example.fernfreunde.data.local.entities.Post
import com.example.fernfreunde.data.local.entities.Settings
import com.example.fernfreunde.data.local.entities.User

@Database(
    entities = [
        User::class,
        DailyChallenge::class,
        Post::class,
        Friendship::class,
        Settings::class,
        PendingUpload::class
    ],
    version = 1,
    exportSchema = false //true
)
abstract class AppDatabase: RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun postDao(): PostDao
    abstract fun friendshipDao(): FriendshipDao
    abstract fun settingsDao(): SettingsDao
    abstract fun pendingUploadDao(): PendingUploadDao
    abstract fun dailyChallengeDao(): DailyChallengeDao
}