package com.example.devflow.data.repository

import com.example.devflow.data.model.Contest
import com.example.devflow.data.remote.RetrofitClient

class ContestRepository {

    suspend fun getUpcomingContests(): List<Contest> {
        return try {
            val response = RetrofitClient.api.getContests()
            val now = System.currentTimeMillis() / 1000L
            response.result
                .filter { it.phase == "BEFORE" && it.startTimeSeconds > now }
                .sortedBy { it.startTimeSeconds }
                .take(10)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Alias used by DashboardViewModel
    suspend fun fetchContests(): List<Contest> = getUpcomingContests()

    // Get only today's contests
    suspend fun getTodayContests(): List<Contest> {
        return try {
            val allContests = getUpcomingContests()
            val now = System.currentTimeMillis()
            val endOfDay = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, 23)
                set(java.util.Calendar.MINUTE, 59)
                set(java.util.Calendar.SECOND, 59)
            }.timeInMillis

            allContests.filter { contest ->
                contest.startTimeMillis in now..endOfDay
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}