package com.example.devflow.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attendance")
data class Attendance(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val studentId: Int,
    val date: Long,          // epoch millis of the session date
    val status: String,      // "PRESENT", "ABSENT", "EXTRA"
    val note: String = ""    // optional note for this session
)