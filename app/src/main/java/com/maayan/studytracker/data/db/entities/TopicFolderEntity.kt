package com.maayan.studytracker.data.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "topic_folders",
    indices = [Index("topicName"), Index("parentFolderId")]
)
data class TopicFolderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val topicName: String,
    val parentFolderId: Long?,
    val name: String
)
