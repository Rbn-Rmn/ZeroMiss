package com.example.devflow.data.local

import androidx.room.*
import com.example.devflow.data.model.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks WHERE deadline BETWEEN :start AND :end ORDER BY priority DESC")
    fun getTasksBetween(start: Long, end: Long): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE deadline < :now AND isCompleted = 0 ORDER BY deadline ASC")
    fun getMissedTasks(now: Long): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE projectId = :projectId ORDER BY deadline ASC")
    fun getTasksByProject(projectId: Int): Flow<List<Task>>

    @Query("SELECT * FROM tasks ORDER BY deadline ASC")
    fun getAllTasks(): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 1 AND deadline BETWEEN :start AND :end")
    suspend fun countCompletedToday(start: Long, end: Long): Int
}