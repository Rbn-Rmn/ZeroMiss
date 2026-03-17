package com.example.devflow

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.devflow.data.model.Task
import com.example.devflow.receiver.ReminderReceiver
import java.text.SimpleDateFormat
import java.util.*

object AlarmScheduler {

    fun scheduleTaskReminders(context: Context, task: Task) {
        if (!task.hasTime) return
        if (task.reminderOffsets.isBlank()) return

        cancelTaskReminders(context, task.id)

        val offsets = task.reminderOffsets
            .split(",")
            .mapNotNull { it.trim().toLongOrNull() }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        offsets.forEachIndexed { index, offsetMinutes ->
            val reminderTimeMillis = task.deadline - (offsetMinutes * 60 * 1000L)
            if (reminderTimeMillis <= System.currentTimeMillis()) return@forEachIndexed

            val label = ReminderScheduler.REMINDER_PRESETS
                .find { it.first == offsetMinutes }?.second
                ?: "$offsetMinutes min before"

            val timeFormatted = SimpleDateFormat("hh:mm a", Locale.getDefault())
                .format(Date(task.deadline))

            val intent = Intent(context, ReminderReceiver::class.java).apply {
                putExtra("title",    "⏰ ${task.title}")
                putExtra("message",  "$label — due at $timeFormatted")
                putExtra("notif_id", task.id * 100 + index)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                task.id * 100 + index,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        reminderTimeMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        reminderTimeMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderTimeMillis,
                    pendingIntent
                )
            }
        }
    }

    fun cancelTaskReminders(context: Context, taskId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        for (index in 0..5) {
            val intent = Intent(context, ReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                taskId * 100 + index,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
            )
            pendingIntent?.let { alarmManager.cancel(it) }
        }
    }

    fun scheduleContestReminder(
        context: Context,
        contestId: Int,
        contestName: String,
        startTimeMillis: Long,
        minutesBefore: Int
    ) {
        val reminderTimeMillis = startTimeMillis - (minutesBefore * 60 * 1000L)
        if (reminderTimeMillis <= System.currentTimeMillis()) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("title",    "🏆 Contest starting soon!")
            putExtra("message",  "$contestName starts in $minutesBefore minutes")
            putExtra("notif_id", contestId + 50000)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            contestId + 50000,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderTimeMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderTimeMillis,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminderTimeMillis,
                pendingIntent
            )
        }
    }

    fun cancelContestReminder(context: Context, contestId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            contestId + 50000,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )
        pendingIntent?.let { alarmManager.cancel(it) }
    }
}