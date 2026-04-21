package com.maayan.studytracker.data.repository

import com.maayan.studytracker.data.dao.ProjectDao
import com.maayan.studytracker.data.dao.ScheduleItemDao
import com.maayan.studytracker.data.dao.TopicFolderDao
import com.maayan.studytracker.data.db.entities.ProjectEntity
import com.maayan.studytracker.ui.theme.DefaultProjectColorHex
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProjectRepository @Inject constructor(
    private val projectDao: ProjectDao,
    private val scheduleItemDao: ScheduleItemDao,
    private val topicFolderDao: TopicFolderDao
) {
    fun observeAll(): Flow<List<ProjectEntity>> = projectDao.observeAll()

    suspend fun getAll(): List<ProjectEntity> = projectDao.getAll()

    suspend fun getById(id: Long): ProjectEntity? = projectDao.getById(id)

    /**
     * Returns the id of an existing project to display. If none exist (fresh install —
     * no v3→v4 migration ran), creates a default "My Schedule" project on the fly so
     * the user never sees an empty project selector.
     */
    suspend fun ensureAtLeastOneProject(): Long {
        val existing = projectDao.getAll()
        if (existing.isNotEmpty()) return existing.first().id
        return createProject("My Schedule", DefaultProjectColorHex)
    }

    suspend fun createProject(name: String, color: String = DefaultProjectColorHex): Long {
        val trimmed = name.trim().ifBlank { "New project" }
        val nextOrder = projectDao.maxOrder() + 1
        return projectDao.insert(
            ProjectEntity(name = trimmed, orderIndex = nextOrder, color = color)
        )
    }

    suspend fun renameProject(id: Long, newName: String) {
        val trimmed = newName.trim().ifBlank { return }
        projectDao.updateName(id, trimmed)
    }

    suspend fun setColor(id: Long, color: String) {
        projectDao.updateColor(id, color)
    }

    /**
     * Deletes the project and every row that belonged to it (schedule items, topic
     * folders, and transitively — via the NoteEntity foreign key with CASCADE — notes).
     * Timer sessions are NOT deleted because stats are global per the product decision.
     */
    suspend fun deleteProject(id: Long) {
        scheduleItemDao.deleteAllForProject(id)
        topicFolderDao.deleteAllForProject(id)
        projectDao.deleteById(id)
    }
}
