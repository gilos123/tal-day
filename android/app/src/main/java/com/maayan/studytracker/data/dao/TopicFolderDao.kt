package com.maayan.studytracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.maayan.studytracker.data.db.entities.TopicFolderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TopicFolderDao {
    @Insert
    suspend fun insert(folder: TopicFolderEntity): Long

    @Query("SELECT * FROM topic_folders WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): TopicFolderEntity?

    @Query("""
        SELECT * FROM topic_folders
        WHERE topicName = :topicName AND parentFolderId IS NULL
        ORDER BY name ASC
    """)
    suspend fun getRoots(topicName: String): List<TopicFolderEntity>

    @Query("""
        SELECT * FROM topic_folders
        WHERE topicName = :topicName AND parentFolderId = :parentId
        ORDER BY name ASC
    """)
    fun observeChildren(topicName: String, parentId: Long): Flow<List<TopicFolderEntity>>

    @Query("""
        SELECT * FROM topic_folders
        WHERE topicName = :topicName AND parentFolderId IS NULL
        ORDER BY name ASC
    """)
    fun observeRoots(topicName: String): Flow<List<TopicFolderEntity>>

    @Query("DELETE FROM topic_folders WHERE id = :id")
    suspend fun deleteById(id: Long)
}
