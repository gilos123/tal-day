package com.maayan.studytracker.data.repository

import com.maayan.studytracker.data.dao.ScheduleItemDao
import com.maayan.studytracker.data.db.entities.ScheduleItemEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleRepository @Inject constructor(
    private val scheduleItemDao: ScheduleItemDao
) {
    fun observeItems(): Flow<List<ScheduleItemEntity>> = scheduleItemDao.observeAll()

    suspend fun getItemById(id: Long): ScheduleItemEntity? = scheduleItemDao.getById(id)

    suspend fun addRow(topicName: String = "", minutes: Int = 30) {
        val nextOrder = scheduleItemDao.maxOrder() + 1
        scheduleItemDao.insert(
            ScheduleItemEntity(
                topicName = topicName,
                plannedDurationMinutes = minutes,
                orderIndex = nextOrder
            )
        )
    }

    suspend fun updateTopicName(itemId: Long, name: String) =
        scheduleItemDao.updateTopicName(itemId, name)

    suspend fun updateDuration(itemId: Long, minutes: Int) =
        scheduleItemDao.updateDuration(itemId, minutes)

    suspend fun setLastDoneDate(itemId: Long, dateIso: String?) =
        scheduleItemDao.updateLastDoneDate(itemId, dateIso)

    suspend fun deleteRow(itemId: Long) = scheduleItemDao.deleteById(itemId)
}
