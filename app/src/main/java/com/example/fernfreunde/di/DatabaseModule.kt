package com.example.fernfreunde.di

import android.app.Application
import androidx.room.Room
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
        ).build()

    @Provides
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()
}