package com.maayan.studytracker.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val orderIndex: Int,
    /** Hex color string for the project's tag dot / row stripe (e.g. "#5BE32A"). */
    val color: String = "#5BE32A"
)
