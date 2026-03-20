package com.example.devflow.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.devflow.data.local.AppDatabase
import com.example.devflow.data.model.Attendance
import com.example.devflow.data.repository.TuitionRepository
import kotlinx.coroutines.flow.first
import java.util.Calendar

class AutoAbsentWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val db = AppDatabase.getDatabase(applicationContext)
            val repository = TuitionRepository(db.studentDao(), db.attendanceDao())

            val today = Calendar.getInstance()
            val dayNameMap = mapOf(
                Calendar.MONDAY to "MON", Calendar.TUESDAY to "TUE",
                Calendar.WEDNESDAY to "WED", Calendar.THURSDAY to "THU",
                Calendar.FRIDAY to "FRI", Calendar.SATURDAY to "SAT",
                Calendar.SUNDAY to "SUN"
            )
            val todayName = dayNameMap[today.get(Calendar.DAY_OF_WEEK)] ?: ""
            val todayMillis = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 12)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val allStudents = repository.getAllStudents().first()
            val scheduledToday = allStudents.filter { student ->
                student.scheduleDays.split(",")
                    .map { it.trim() }
                    .contains(todayName)
            }

            scheduledToday.forEach { student ->
                val existing = repository.getAttendanceForDay(student.id, todayMillis)
                if (existing == null) {
                    repository.markAttendance(
                        Attendance(
                            studentId = student.id,
                            date = todayMillis,
                            status = "ABSENT",
                            note = "Auto-marked absent"
                        )
                    )
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}