package com.example.devflow

import android.content.Context
import androidx.work.*
import com.example.devflow.data.model.Task
import com.example.devflow.worker.TaskReminderWorker
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    val REMINDER_PRESETS = listOf(
        5L    to "5 min before",
        10L   to "10 min before",
        30L   to "30 min before",
        60L   to "1 hour before",
        180L  to "3 hours before",
        1440L to "1 day before"
    )

    fun scheduleReminders(context: Context, task: Task) {
        if (!task.hasTime) return
        if (task.reminderOffsets.isBlank()) return

        cancelReminders(context, task.id)

        val offsets = task.reminderOffsets
            .split(",")
            .mapNotNull { it.trim().toLongOrNull() }

        offsets.forEachIndexed { index, offsetMinutes ->
            val reminderTimeMillis = task.deadline - (offsetMinutes * 60 * 1000L)
            val delayMillis = reminderTimeMillis - System.currentTimeMillis()

            if (delayMillis <= 0) return@forEachIndexed

            val timeFormatted = SimpleDateFormat("hh:mm a", Locale.getDefault())
                .format(Date(task.deadline))

            val label = REMINDER_PRESETS
                .find { it.first == offsetMinutes }?.second
                ?: "$offsetMinutes min before"

            val inputData = workDataOf(
                "title"    to "⏰ ${task.title}",
                "message"  to "$label — due at $timeFormatted",
                "notif_id" to (task.id * 100 + index)
            )

            val request = OneTimeWorkRequestBuilder<TaskReminderWorker>()
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag("task_${task.id}")
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    "task_${task.id}_reminder_$index",
                    ExistingWorkPolicy.REPLACE,
                    request
                )
        }
    }

    fun cancelReminders(context: Context, taskId: Int) {
        WorkManager.getInstance(context)
            .cancelAllWorkByTag("task_$taskId")
    }
}