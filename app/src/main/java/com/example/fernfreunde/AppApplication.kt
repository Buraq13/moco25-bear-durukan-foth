package com.example.fernfreunde

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.BuildConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
// import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject

@HiltAndroidApp
class AppApplication : Application(), Configuration.Provider {

//    @Inject
//    lateinit var workerFactory: HiltWorkerFactory
//
//    override val workManagerConfiguration: Configuration =
//        Configuration.Builder()
//            .setWorkerFactory(workerFactory)
//            .build()

    override val workManagerConfiguration: Configuration by lazy {
        // Hole das HiltWorkerFactory sicher aus dem Hilt-Component via EntryPointAccessors
        val entryPoint = EntryPointAccessors.fromApplication(
            this,
            HiltWorkerFactoryEntryPoint::class.java
        )
        val workerFactory: HiltWorkerFactory = entryPoint.workerFactory()

        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }

    override fun onCreate() {
        if (BuildConfig.DEBUG) {
            FirebaseFirestore.getInstance().useEmulator("10.0.2.2", 8080)
            FirebaseStorage.getInstance().useEmulator("10.0.2.2", 9199)
            FirebaseAuth.getInstance().useEmulator("10.0.2.2", 9099)
        }
        super.onCreate()
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface HiltWorkerFactoryEntryPoint {
    fun workerFactory(): HiltWorkerFactory
}