package com.example.devflow.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val description: String = "",
    val color: String = "#7C6AF7",
    val createdAt: Long = System.currentTimeMillis()
)