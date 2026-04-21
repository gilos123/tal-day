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
        WHERE projectId = :projectId AND topicName = :topicName AND parentFolderId IS NULL
        ORDER BY name ASC
    """)
    suspend fun getRoots(projectId: Long, topicName: String): List<TopicFolderEntity>

    @Query("""
        SELECT * FROM topic_folders
        WHERE projectId = :projectId AND topicName = :topicName AND parentFolderId = :parentId
        ORDER BY name ASC
    """)
    fun observeChildren(projectId: Long, topicName: String, parentId: Long): Flow<List<TopicFolderEntity>>

    @Query("""
        SELECT * FROM topic_folders
        WHERE projectId = :projectId AND topicName = :topicName AND parentFolderId IS NULL
        ORDER BY name ASC
    """)
    fun observeRoots(projectId: Long, topicName: String): Flow<List<TopicFolderEntity>>

    @Query("DELETE FROM topic_folders WHERE id = :id")
    suspend fun deleteById(id: Long)

    /** Used when deleting a project so all its folders (and via NoteEntity FK, notes) are removed. */
    @Query("DELETE FROM topic_folders WHERE projectId = :projectId")
    suspend fun deleteAllForProject(projectId: Long)
}
