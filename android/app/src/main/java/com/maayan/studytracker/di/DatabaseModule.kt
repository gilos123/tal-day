package com.maayan.studytracker.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.maayan.studytracker.data.dao.NoteDao
import com.maayan.studytracker.data.dao.ScheduleItemDao
import com.maayan.studytracker.data.dao.TimerSessionDao
import com.maayan.studytracker.data.dao.TopicFolderDao
import com.maayan.studytracker.data.db.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /** v2 → v3: add `lastDoneDate` column to schedule_items for the per-row done checkbox. */
    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE schedule_items ADD COLUMN lastDoneDate TEXT")
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "maayan.db")
            .addMigrations(MIGRATION_2_3)
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideScheduleItemDao(db: AppDatabase): ScheduleItemDao = db.scheduleItemDao()
    @Provides fun provideTimerSessionDao(db: AppDatabase): TimerSessionDao = db.timerSessionDao()
    @Provides fun provideTopicFolderDao(db: AppDatabase): TopicFolderDao = db.topicFolderDao()
    @Provides fun provideNoteDao(db: AppDatabase): NoteDao = db.noteDao()
}
