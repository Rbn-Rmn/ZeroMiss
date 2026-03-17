package com.example.devflow.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String = "",
    val notes: String = "",
    val deadline: Long,
    val hasTime: Boolean = false,
    val priority: Int = 2,
    val category: String,
    val colorLabel: String = "#6650A4",
    val projectId: Int = 0,
    val isCompleted: Boolean = false,
    val repeatType: String = "none",
    val reminderOffsets: String = ""
)