package com.maayan.studytracker.data.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "topic_folders",
    indices = [Index("projectId"), Index("topicName"), Index("parentFolderId")]
)
data class TopicFolderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** Scopes the folder tree to a specific project so each project has its own notes. */
    val projectId: Long,
    val topicName: String,
    val parentFolderId: Long?,
    val name: String
)
