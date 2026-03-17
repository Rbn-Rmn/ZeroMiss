package com.example.devflow.data.local

import androidx.room.*
import com.example.devflow.data.model.Attendance
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {

    @Query("SELECT * FROM attendance WHERE studentId = :studentId ORDER BY date DESC")
    fun getAttendanceForStudent(studentId: Int): Flow<List<Attendance>>

    @Query("""
        SELECT * FROM attendance 
        WHERE studentId = :studentId 
        AND date BETWEEN :startOfMonth AND :endOfMonth 
        ORDER BY date ASC
    """)
    fun getMonthlyAttendance(
        studentId: Int,
        startOfMonth: Long,
        endOfMonth: Long
    ): Flow<List<Attendance>>

    @Query("""
        SELECT COUNT(*) FROM attendance 
        WHERE studentId = :studentId 
        AND status = :status
        AND date BETWEEN :startOfMonth AND :endOfMonth
    """)
    suspend fun countByStatus(
        studentId: Int,
        status: String,
        startOfMonth: Long,
        endOfMonth: Long
    ): Int

    @Query("""
        SELECT * FROM attendance 
        WHERE studentId = :studentId 
        AND date BETWEEN :dayStart AND :dayEnd
        LIMIT 1
    """)
    suspend fun getAttendanceForDay(
        studentId: Int,
        dayStart: Long,
        dayEnd: Long
    ): Attendance?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: Attendance)

    @Update
    suspend fun updateAttendance(attendance: Attendance)

    @Delete
    suspend fun deleteAttendance(attendance: Attendance)

    @Query("DELETE FROM attendance WHERE studentId = :studentId")
    suspend fun deleteAllForStudent(studentId: Int)
}