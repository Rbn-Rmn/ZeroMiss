package com.example.devflow.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val subject: String = "",
    val grade: String = "",
    val guardianPhone: String = "",
    val scheduleDays: String = "",
    val colorLabel: String = "#007AFF",
    val notes: String = "",
    val fee: String = "",
    val photoUri: String = "",
    val createdAt: Long = System.currentTimeMillis()
)