package com.example.devflow.data.repository

import com.example.devflow.data.local.TaskDao
import com.example.devflow.data.model.Task
import kotlinx.coroutines.flow.Flow
import java.util.*

class TaskRepository(private val taskDao: TaskDao) {

    fun getTodayTasks(): Flow<List<Task>> {
        val start = getDayStart(System.currentTimeMillis())
        val end = getDayEnd(System.currentTimeMillis())
        return taskDao.getTasksBetween(start, end)
    }

    fun getTomorrowTasks(): Flow<List<Task>> {
        val tomorrowMillis = System.currentTimeMillis() + 86_400_000L
        val start = getDayStart(tomorrowMillis)
        val end = getDayEnd(tomorrowMillis)
        return taskDao.getTasksBetween(start, end)
    }

    fun getMissedTasks(): Flow<List<Task>> {
        return taskDao.getMissedTasks(System.currentTimeMillis())
    }

    fun getAllTasks(): Flow<List<Task>> = taskDao.getAllTasks()

    suspend fun addTask(task: Task) = taskDao.insertTask(task)
    suspend fun updateTask(task: Task) = taskDao.updateTask(task)
    suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)

    private fun getDayStart(millis: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    private fun getDayEnd(millis: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return cal.timeInMillis
    }
    fun getTasksByProject(projectId: Int): Flow<List<Task>> =
        taskDao.getTasksByProject(projectId)
}