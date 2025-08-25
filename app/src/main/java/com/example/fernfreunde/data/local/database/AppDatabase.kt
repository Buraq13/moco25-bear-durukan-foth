package com.example.fernfreunde.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.fernfreunde.data.local.daos.ChallengeDao
import com.example.fernfreunde.data.local.daos.DailyChallengeDao
import com.example.fernfreunde.data.local.daos.GroupDao
import com.example.fernfreunde.data.local.daos.ParticipationDao
import com.example.fernfreunde.data.local.daos.PendingUploadDao
import com.example.fernfreunde.data.local.daos.PostDao
import com.example.fernfreunde.data.local.daos.SettingsDao
import com.example.fernfreunde.data.local.daos.UserDao
import com.example.fernfreunde.data.local.entities.Challenge
import com.example.fernfreunde.data.local.entities.DailyChallenge
import com.example.fernfreunde.data.local.entities.Group
import com.example.fernfreunde.data.local.entities.GroupMember
import com.example.fernfreunde.data.local.entities.Participation
import com.example.fernfreunde.data.local.entities.PendingUpload
import com.example.fernfreunde.data.local.entities.Post
import com.example.fernfreunde.data.local.entities.Settings
import com.example.fernfreunde.data.local.entities.User

@Database(
    entities = [
        User::class,
        Group::class,
        GroupMember::class,
        Challenge::class,
        DailyChallenge::class,
        Post::class,
        Participation::class,
        Settings::class,
        PendingUpload::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase: RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun groupDao(): GroupDao
    abstract fun postDao(): PostDao
    abstract fun participationDao(): ParticipationDao
    abstract fun settingsDao(): SettingsDao
    abstract fun pendingUploadDao(): PendingUploadDao
    abstract fun challengeDao(): ChallengeDao
    abstract fun dailyChallengeDao(): DailyChallengeDao
}