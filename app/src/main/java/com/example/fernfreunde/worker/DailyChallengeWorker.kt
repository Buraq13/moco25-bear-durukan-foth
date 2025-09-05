package com.example.fernfreunde

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class DailyChallengeWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        // Android 13+ Permission check
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            applicationContext.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Keine Permission → Notification wird nicht gezeigt
            return Result.success()
        }

        // Notification bauen
        val builder = NotificationCompat.Builder(applicationContext, "daily_challenges")
            .setSmallIcon(android.R.drawable.ic_dialog_info) // temporäres Icon
            .setContentTitle("Tägliche Challenge")
            .setContentText("Vergiss nicht deine heutige Challenge!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        // Notification anzeigen
        with(NotificationManagerCompat.from(applicationContext)) {
            notify(1, builder.build())
        }

        return Result.success()
    }
}