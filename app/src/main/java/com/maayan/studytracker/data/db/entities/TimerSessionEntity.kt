package com.maayan.studytracker.data.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "timer_sessions",
    indices = [Index("date"), Index("topicName")]
)
data class TimerSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val scheduleItemId: Long?,
    val topicName: String,
    val date: String,
    val actualDurationSeconds: Long,
    val completedAt: Long
)
