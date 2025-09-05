package com.example.fernfreunde

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.example.fernfreunde.data.auth.AnonymAuth
import com.example.fernfreunde.data.local.entities.User
import com.example.fernfreunde.data.remote.dtos.UserDto
import com.example.fernfreunde.data.repositories.UserRepository
import com.example.fernfreunde.ui.navigation.AppNavHost
import com.example.fernfreunde.ui.theme.FernfreundeTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.fernfreunde.worker.DailyChallengeWorker
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.fernfreunde.ui.navigation.AppNavHost
import com.example.fernfreunde.ui.theme.FernfreundeTheme
import java.util.concurrent.TimeUnit


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var anonymAuth: AnonymAuth

    @Inject lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        lifecycleScope.launchWhenCreated {

            try {
                val firebaseUser = anonymAuth.ensureSignedIn()
                firebaseUser?.let { user ->
                    val uid = user.uid
                    // create a default DTO/entity and persist it remotely + locally
                    val defaultUsername = "anon_${uid.take(6)}"
                    val defaultDisplayName = "Anonymous"

                    // If you have a UserRepository with createOrUpdateUser(UserDto) -> use it:
                    try {
                        // userRepository should expose a createOrUpdateUser(userDto) method.
                        userRepository.upsertLocalUser(
                            User(
                                userId = uid,
                                username = defaultUsername,
                                displayName = defaultDisplayName,
                                profileImageUrl = null,
                                bio = null,
                                createdAt = System.currentTimeMillis()
                            )
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    Log.i("MainActivity", "Anon sign-in done, uid=$uid")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.w("MainActivity", "Anon sign-in failed: ${e.message}")
            }

            setContent {
                FernfreundeTheme {
                    AppNavHost()
                }


        createNotificationChannel()


        requestNotificationPermissionIfNeeded()


        scheduleDailyChallenge()


        triggerTestNotification()

        setContent {
            FernfreundeTheme {
                AppNavHost()

            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "daily_challenges"
            val name = "Daily Challenges"
            val descriptionText = "Erinnert dich an deine tägliche Challenge"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    100
                )
            }
        }
    }

    private fun scheduleDailyChallenge() {
        val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyChallengeWorker>(
            1, TimeUnit.DAYS
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "daily_challenge",
            ExistingPeriodicWorkPolicy.REPLACE,
            dailyWorkRequest
        )
    }

    private fun triggerTestNotification() {
        // Permission Check (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return

        val builder = NotificationCompat.Builder(this, "daily_challenges")
            .setSmallIcon(android.R.drawable.ic_dialog_info) // StandardIcon zum testen
            .setContentTitle("Test-Notification")
            .setContentText("Only a test")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)) {
            notify(999, builder.build()) // 999 = Test-ID
        }
    }
}

