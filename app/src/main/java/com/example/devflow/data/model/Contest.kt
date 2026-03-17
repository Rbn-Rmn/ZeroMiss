package com.example.devflow.data.model

data class Contest(
    val id: Int,
    val name: String,
    val phase: String,
    val startTimeSeconds: Long,
    val durationSeconds: Long,
    val type: String
) {
    val startTimeMillis get() = startTimeSeconds * 1000L
    val endTimeMillis get() = (startTimeSeconds + durationSeconds) * 1000L
}