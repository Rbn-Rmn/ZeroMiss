package com.example.devflow.data.remote

import com.example.devflow.data.model.Contest
import retrofit2.http.GET

data class ContestListResponse(
    val status: String,
    val result: List<Contest>
)

interface CodeforcesApi {
    @GET("contest.list?gym=false")
    suspend fun getContests(): ContestListResponse
}