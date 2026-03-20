package com.example.devflow

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.devflow.worker.AutoAbsentWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        deleteOldChannels()
        createNotificationChannels()
        scheduleAutoAbsentWorker()
    }

    private fun deleteOldChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            manager.deleteNotificationChannel("task_channel")
            manager.deleteNotificationChannel("contest_channel")
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val taskChannel = NotificationChannel(
                "task_channel_v2",
                "Task Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for task deadlines"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                setSound(soundUri, audioAttributes)
                setBypassDnd(true)
            }

            val contestChannel = NotificationChannel(
                "contest_channel_v2",
                "Contest Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for Codeforces contests"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                setSound(soundUri, audioAttributes)
                setBypassDnd(true)
            }

            manager.createNotificationChannel(taskChannel)
            manager.createNotificationChannel(contestChannel)
        }
    }

    private fun scheduleAutoAbsentWorker() {
        val now = Calendar.getInstance()

        // Schedule for 11 PM every day
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            // If 11 PM already passed today, schedule for tomorrow
            if (before(now)) add(Calendar.DAY_OF_MONTH, 1)
        }

        val delay = target.timeInMillis - now.timeInMillis

        val request = PeriodicWorkRequestBuilder<AutoAbsentWorker>(
            1, TimeUnit.DAYS
        )
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "auto_absent_work",
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }
}