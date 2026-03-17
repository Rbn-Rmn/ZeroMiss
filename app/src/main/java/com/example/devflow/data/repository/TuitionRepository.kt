package com.example.devflow.data.repository

import com.example.devflow.data.local.AttendanceDao
import com.example.devflow.data.local.StudentDao
import com.example.devflow.data.model.Attendance
import com.example.devflow.data.model.Student
import kotlinx.coroutines.flow.Flow
import java.util.*

class TuitionRepository(
    private val studentDao: StudentDao,
    private val attendanceDao: AttendanceDao
) {
    // ── Students ──────────────────────────────────────────
    fun getAllStudents(): Flow<List<Student>> = studentDao.getAllStudents()
    suspend fun addStudent(student: Student) = studentDao.insertStudent(student)
    suspend fun updateStudent(student: Student) = studentDao.updateStudent(student)
    suspend fun deleteStudent(student: Student) {
        studentDao.deleteStudent(student)
        attendanceDao.deleteAllForStudent(student.id)
    }

    // ── Attendance ────────────────────────────────────────
    fun getAttendanceForStudent(studentId: Int): Flow<List<Attendance>> =
        attendanceDao.getAttendanceForStudent(studentId)

    fun getMonthlyAttendance(
        studentId: Int,
        year: Int,
        month: Int
    ): Flow<List<Attendance>> {
        val start = getMonthStart(year, month)
        val end = getMonthEnd(year, month)
        return attendanceDao.getMonthlyAttendance(studentId, start, end)
    }

    suspend fun countByStatus(
        studentId: Int,
        status: String,
        year: Int,
        month: Int
    ): Int {
        val start = getMonthStart(year, month)
        val end = getMonthEnd(year, month)
        return attendanceDao.countByStatus(studentId, status, start, end)
    }

    suspend fun getAttendanceForDay(studentId: Int, dateMillis: Long): Attendance? {
        val cal = Calendar.getInstance().apply { timeInMillis = dateMillis }
        val start = Calendar.getInstance().apply {
            set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val end = Calendar.getInstance().apply {
            set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 23, 59, 59)
        }.timeInMillis
        return attendanceDao.getAttendanceForDay(studentId, start, end)
    }

    suspend fun markAttendance(attendance: Attendance) =
        attendanceDao.insertAttendance(attendance)

    suspend fun updateAttendance(attendance: Attendance) =
        attendanceDao.updateAttendance(attendance)

    suspend fun deleteAttendance(attendance: Attendance) =
        attendanceDao.deleteAttendance(attendance)

    // ── Helpers ───────────────────────────────────────────
    private fun getMonthStart(year: Int, month: Int): Long {
        return Calendar.getInstance().apply {
            set(year, month, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun getMonthEnd(year: Int, month: Int): Long {
        return Calendar.getInstance().apply {
            set(year, month, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.MONTH, 1)
            add(Calendar.MILLISECOND, -1)
        }.timeInMillis
    }
}