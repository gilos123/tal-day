package com.maayan.studytracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.maayan.studytracker.data.db.entities.ScheduleItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleItemDao {
    @Query("SELECT * FROM schedule_items WHERE projectId = :projectId ORDER BY orderIndex ASC")
    fun observeForProject(projectId: Long): Flow<List<ScheduleItemEntity>>

    @Query("SELECT * FROM schedule_items WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ScheduleItemEntity?

    @Query("SELECT COALESCE(MAX(orderIndex), -1) FROM schedule_items WHERE projectId = :projectId")
    suspend fun maxOrderInProject(projectId: Long): Int

    @Insert
    suspend fun insert(item: ScheduleItemEntity): Long

    @Query("UPDATE schedule_items SET topicName = :name WHERE id = :id")
    suspend fun updateTopicName(id: Long, name: String)

    @Query("UPDATE schedule_items SET plannedDurationMinutes = :minutes WHERE id = :id")
    suspend fun updateDuration(id: Long, minutes: Int)

    @Query("UPDATE schedule_items SET lastDoneDate = :date WHERE id = :id")
    suspend fun updateLastDoneDate(id: Long, date: String?)

    @Query("DELETE FROM schedule_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    /** Used when deleting a project so all its rows are removed too. */
    @Query("DELETE FROM schedule_items WHERE projectId = :projectId")
    suspend fun deleteAllForProject(projectId: Long)
}
