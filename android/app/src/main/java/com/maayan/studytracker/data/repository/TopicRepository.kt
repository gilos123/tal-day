package com.maayan.studytracker.data.repository

import com.maayan.studytracker.data.dao.NoteDao
import com.maayan.studytracker.data.dao.TopicFolderDao
import com.maayan.studytracker.data.db.entities.NoteEntity
import com.maayan.studytracker.data.db.entities.NoteStatus
import com.maayan.studytracker.data.db.entities.TopicFolderEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TopicRepository @Inject constructor(
    private val folderDao: TopicFolderDao,
    private val noteDao: NoteDao
) {
    suspend fun getOrCreateRootFolder(projectId: Long, topicName: String): Long {
        val existing = folderDao.getRoots(projectId, topicName).firstOrNull()
        if (existing != null) return existing.id
        return folderDao.insert(
            TopicFolderEntity(
                projectId = projectId,
                topicName = topicName,
                parentFolderId = null,
                name = topicName
            )
        )
    }

    suspend fun getFolder(id: Long): TopicFolderEntity? = folderDao.getById(id)

    fun observeSubfolders(
        projectId: Long,
        topicName: String,
        parentFolderId: Long
    ): Flow<List<TopicFolderEntity>> =
        folderDao.observeChildren(projectId, topicName, parentFolderId)

    fun observeNotes(folderId: Long): Flow<List<NoteEntity>> = noteDao.observeForFolder(folderId)

    suspend fun createSubfolder(
        projectId: Long,
        topicName: String,
        parentFolderId: Long,
        name: String
    ): Long =
        folderDao.insert(
            TopicFolderEntity(
                projectId = projectId,
                topicName = topicName,
                parentFolderId = parentFolderId,
                name = name
            )
        )

    suspend fun createNote(folderId: Long, content: String): Long =
        noteDao.insert(
            NoteEntity(
                folderId = folderId,
                content = content,
                status = NoteStatus.NONE.name,
                createdAt = System.currentTimeMillis()
            )
        )

    suspend fun updateNoteContent(id: Long, content: String) = noteDao.updateContent(id, content)
    suspend fun updateNoteStatus(id: Long, status: NoteStatus) = noteDao.updateStatus(id, status.name)
    suspend fun deleteNote(id: Long) = noteDao.deleteById(id)
    suspend fun deleteFolder(id: Long) = folderDao.deleteById(id)
}
