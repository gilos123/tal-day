package com.maayan.studytracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.maayan.studytracker.data.db.entities.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Insert
    suspend fun insert(note: NoteEntity): Long

    @Query("SELECT * FROM notes WHERE folderId = :folderId ORDER BY createdAt ASC")
    fun observeForFolder(folderId: Long): Flow<List<NoteEntity>>

    @Query("UPDATE notes SET content = :content WHERE id = :id")
    suspend fun updateContent(id: Long, content: String)

    @Query("UPDATE notes SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteById(id: Long)
}
