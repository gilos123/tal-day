package com.maayan.studytracker.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.maayan.studytracker.data.dao.NoteDao
import com.maayan.studytracker.data.dao.ProjectDao
import com.maayan.studytracker.data.dao.ScheduleItemDao
import com.maayan.studytracker.data.dao.TimerSessionDao
import com.maayan.studytracker.data.dao.TopicFolderDao
import com.maayan.studytracker.data.db.entities.NoteEntity
import com.maayan.studytracker.data.db.entities.ProjectEntity
import com.maayan.studytracker.data.db.entities.ScheduleItemEntity
import com.maayan.studytracker.data.db.entities.TimerSessionEntity
import com.maayan.studytracker.data.db.entities.TopicFolderEntity

@Database(
    entities = [
        ProjectEntity::class,
        ScheduleItemEntity::class,
        TimerSessionEntity::class,
        TopicFolderEntity::class,
        NoteEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun scheduleItemDao(): ScheduleItemDao
    abstract fun timerSessionDao(): TimerSessionDao
    abstract fun topicFolderDao(): TopicFolderDao
    abstract fun noteDao(): NoteDao
}
