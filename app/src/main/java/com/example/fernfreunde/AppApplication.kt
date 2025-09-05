package com.example.fernfreunde

import android.app.Application
import android.content.pm.ApplicationInfo
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.FirebaseApp
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent

@HiltAndroidApp
class AppApplication : Application(), Configuration.Provider {

    override val workManagerConfiguration: Configuration
        get() {
            val entryPoint = EntryPointAccessors.fromApplication(
                this,
                HiltWorkerFactoryEntryPoint::class.java
            )
            val factory: HiltWorkerFactory = entryPoint.workerFactory()

            return Configuration.Builder()
                .setWorkerFactory(factory)
                .build()
        }

    override fun onCreate() {
        super.onCreate()

        // init Firebase
        FirebaseApp.initializeApp(this)

        // Debuggable-Check statt BuildConfig.DEBUG
        val isDebuggable = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        if (isDebuggable) {
            try {
                // optional: Log oder andere Debug-Only-Kram
                Log.i("AppApplication", "App is debuggable -> Firebase emulator routing enabled")
            } catch (e: Exception) {
                Log.w("AppApplication", "Failed to configure debug behaviour: ${e.message}")
            }
        }
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface HiltWorkerFactoryEntryPoint {
    fun workerFactory(): HiltWorkerFactory
}