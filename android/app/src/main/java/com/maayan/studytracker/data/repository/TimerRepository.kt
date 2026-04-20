package com.maayan.studytracker.data.repository

import com.maayan.studytracker.data.dao.TimerSessionDao
import com.maayan.studytracker.data.db.entities.TimerSessionEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimerRepository @Inject constructor(
    private val dao: TimerSessionDao
) {
    suspend fun recordSession(
        scheduleItemId: Long?,
        topicName: String,
        date: String,
        actualDurationSeconds: Long
    ) {
        dao.insert(
            TimerSessionEntity(
                scheduleItemId = scheduleItemId,
                topicName = topicName,
                date = date,
                actualDurationSeconds = actualDurationSeconds,
                completedAt = System.currentTimeMillis()
            )
        )
    }
}
