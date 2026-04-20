package com.maayan.studytracker.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class NoteStatus { DONE, NOT_DONE, NONE }

@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = TopicFolderEntity::class,
            parentColumns = ["id"],
            childColumns = ["folderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("folderId")]
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val folderId: Long,
    val content: String,
    val status: String = NoteStatus.NONE.name,
    val createdAt: Long
)
