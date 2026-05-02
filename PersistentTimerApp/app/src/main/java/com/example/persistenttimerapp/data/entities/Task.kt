package com.example.persistenttimerapp.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "tasks",
    foreignKeys = [ForeignKey(
        entity = Category::class,
        parentColumns = ["id"],
        childColumns = ["categoryId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val categoryId: Int,
    val taskName: String,
    val durationMinutes: Int,
    val dateCompleted: Long // Store as timestamp
)
