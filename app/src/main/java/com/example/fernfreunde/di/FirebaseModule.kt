// file: di/FirebaseModule.kt
package com.example.fernfreunde.di

import android.app.Application
import androidx.work.WorkManager
import com.example.fernfreunde.data.remote.dataSources.FirestoreDailyChallengeDataSource
import com.example.fernfreunde.data.remote.dataSources.FirestoreFriendshipDataSource
import com.example.fernfreunde.data.remote.dataSources.FirestorePostDataSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.example.fernfreunde.data.remote.dataSources.FirestoreUserDataSource

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideStorage(): FirebaseStorage = FirebaseStorage.getInstance()

    @Provides
    @Singleton
    fun provideAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirestorePostDataSource(): FirestorePostDataSource = FirestorePostDataSource()

    @Provides
    @Singleton
    fun provideFirestoreUserDataSource(
        firestore: FirebaseFirestore,
        storage: FirebaseStorage
    ): FirestoreUserDataSource = FirestoreUserDataSource(firestore, storage)

    @Provides
    @Singleton
    fun provideFirestoreFriendshipDataSource(
        firestore: FirebaseFirestore
    ): FirestoreFriendshipDataSource = FirestoreFriendshipDataSource(firestore)

    @Provides
    @Singleton
    fun provideFirestoreDailyChallengeDataSource(): FirestoreDailyChallengeDataSource = FirestoreDailyChallengeDataSource()

    @Provides
    @Singleton
    fun provideWorkManager(app: Application): WorkManager = WorkManager.getInstance(app)
}
