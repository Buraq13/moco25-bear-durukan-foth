package com.example.fernfreunde.di

import android.app.Application
import androidx.room.Room
import com.example.fernfreunde.data.local.daos.DailyChallengeDao
import com.example.fernfreunde.data.local.daos.FriendshipDao
import com.example.fernfreunde.data.local.daos.PendingUploadDao
import com.example.fernfreunde.data.local.daos.PostDao
import com.example.fernfreunde.data.local.daos.SettingsDao
import com.example.fernfreunde.data.local.daos.UserDao
import com.example.fernfreunde.data.local.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(app: Application): AppDatabase =
        Room.databaseBuilder(
            app,
            AppDatabase::class.java,
            "app_db"
        )
            .fallbackToDestructiveMigration()
            .build()


    @Provides
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()

    @Provides
    fun provideFriendshipDao(db: AppDatabase): FriendshipDao = db.friendshipDao()

    @Provides
    fun provideDailyChallengeDao(db: AppDatabase): DailyChallengeDao = db.dailyChallengeDao()

    @Provides
    fun providePostDao(db: AppDatabase): PostDao = db.postDao()

    @Provides
    fun pendingUploadDao(db: AppDatabase): PendingUploadDao = db.pendingUploadDao()

    @Provides
    fun settingsDao(db: AppDatabase): SettingsDao = db.settingsDao()
}