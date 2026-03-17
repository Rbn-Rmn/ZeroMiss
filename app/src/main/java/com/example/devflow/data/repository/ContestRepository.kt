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
}