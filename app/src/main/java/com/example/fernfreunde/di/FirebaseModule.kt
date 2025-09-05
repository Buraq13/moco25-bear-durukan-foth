// file: di/FirebaseModule.kt
package com.example.fernfreunde.di

import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
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
import com.google.firebase.FirebaseApp
import dagger.hilt.android.qualifiers.ApplicationContext

private const val EMULATOR_HOST = "10.0.2.2" // android emulator host
private const val FIRESTORE_PORT = 8080
private const val AUTH_PORT = 9099
private const val STORAGE_PORT = 9199

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseApp(@ApplicationContext context: Context): FirebaseApp {
        // initializeApp ist idempotent: wenn bereits initialisiert, wird die bestehende zurückgegeben.
        return FirebaseApp.initializeApp(context)!!
    }

    @Provides
    @Singleton
    // fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
    fun provideFirestore(@ApplicationContext context: Context): FirebaseFirestore {
        val instance = FirebaseFirestore.getInstance()
        val isDebuggable = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        if (isDebuggable) {
            instance.useEmulator(EMULATOR_HOST, FIRESTORE_PORT)
            FirebaseFirestore.setLoggingEnabled(true)
        }
        return instance
    }

    @Provides
    @Singleton
    // fun provideStorage(): FirebaseStorage = FirebaseStorage.getInstance()
    fun provideStorage(@ApplicationContext context: Context): FirebaseStorage {
        val instance = FirebaseStorage.getInstance()
        val isDebuggable = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        if (isDebuggable) {
            instance.useEmulator(EMULATOR_HOST, STORAGE_PORT)
        }
        return instance
    }
    @Provides
    @Singleton
    //fun provideAuth(): FirebaseAuth = FirebaseAuth.getInstance()
    fun provideAuth(@ApplicationContext context: Context): FirebaseAuth {
        val instance = FirebaseAuth.getInstance()
        val isDebuggable = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        if (isDebuggable) {
            instance.useEmulator(EMULATOR_HOST, AUTH_PORT)
        }
        return instance
    }

    @Provides
    @Singleton
    fun provideFirestorePostDataSource(
        firestore: FirebaseFirestore,
        storage: FirebaseStorage
    ): FirestorePostDataSource = FirestorePostDataSource(firestore, storage)

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
    fun provideFirestoreDailyChallengeDataSource(
        firestore: FirebaseFirestore,
        storage: FirebaseStorage
    ): FirestoreDailyChallengeDataSource = FirestoreDailyChallengeDataSource(firestore, storage)

    @Provides
    @Singleton
    fun provideWorkManager(app: Application): WorkManager = WorkManager.getInstance(app)
}
