package com.maayan.studytracker.data.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "achievements",
    indices = [Index(value = ["code"], unique = true)]
)
data class AchievementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** Stable code matching an entry in [com.maayan.studytracker.domain.AchievementRule]. */
    val code: String,
    /** Epoch millis when this achievement was unlocked. */
    val unlockedAt: Long
)
