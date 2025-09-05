package com.example.fernfreunde

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
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

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Notification Channel erstellen
        createNotificationChannel()

        // Runtime-Permission für Android 13+ abfragen
        requestNotificationPermissionIfNeeded()

        // WorkManager für tägliche Challenge starten
        scheduleDailyChallenge()

        // Test-Notification direkt auslösen (kannst du später entfernen)
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
            val importance = NotificationManager.IMPORTANCE_HIGH // sorgt für Banner + Sound
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
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Standard-Icon zum Testen
            .setContentTitle("Test-Notification")
            .setContentText("Hey, das ist nur ein Test! 🎉")
            .setPriority(NotificationCompat.PRIORITY_HIGH) // sorgt für Banner + Sound
            .setAutoCancel(true) // Notification verschwindet beim Tippen

        with(NotificationManagerCompat.from(this)) {
            notify(999, builder.build()) // 999 = Test-ID, überschreibt keine echten Notifications
        }
    }
}

