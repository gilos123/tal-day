package com.maayan.studytracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.maayan.studytracker.data.db.entities.ProjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY orderIndex ASC")
    fun observeAll(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects ORDER BY orderIndex ASC")
    suspend fun getAll(): List<ProjectEntity>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ProjectEntity?

    @Query("SELECT COUNT(*) FROM projects")
    suspend fun count(): Int

    @Query("SELECT COALESCE(MAX(orderIndex), -1) FROM projects")
    suspend fun maxOrder(): Int

    @Insert
    suspend fun insert(project: ProjectEntity): Long

    @Query("UPDATE projects SET name = :name WHERE id = :id")
    suspend fun updateName(id: Long, name: String)

    @Query("UPDATE projects SET color = :color WHERE id = :id")
    suspend fun updateColor(id: Long, color: String)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteById(id: Long)
}
