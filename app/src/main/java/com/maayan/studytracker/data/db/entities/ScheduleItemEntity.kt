package com.maayan.studytracker.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedule_items")
data class ScheduleItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val topicName: String,
    val plannedDurationMinutes: Int,
    val orderIndex: Int,
    /**
     * ISO yyyy-MM-dd of the most recent day the user ticked this row's "done" checkbox.
     * The UI treats the checkbox as checked only when this equals today's date, so the
     * list of checkboxes auto-resets at midnight. Null = never marked done.
     */
    val lastDoneDate: String? = null
)
