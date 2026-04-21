package com.maayan.studytracker.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.maayan.studytracker.data.dao.AchievementDao
import com.maayan.studytracker.data.dao.NoteDao
import com.maayan.studytracker.data.dao.ProjectDao
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

    /**
     * v3 → v4: introduce Projects.
     *   1. Create the `projects` table.
     *   2. Seed one default project named "My Schedule" so migrated rows have a home.
     *   3. Add `projectId` columns + indices to `schedule_items` and `topic_folders`,
     *      defaulting every existing row to the freshly-inserted default project (id=1).
     *
     * We deliberately skip declaring a foreign key so this ALTER-TABLE based migration
     * stays simple (SQLite can't add FKs via ALTER). Cascading deletes of a project's
     * schedule items + folders + notes are handled in the repository layer.
     */
    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS projects (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    name TEXT NOT NULL,
                    orderIndex INTEGER NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL("INSERT INTO projects (name, orderIndex) VALUES ('My Schedule', 0)")
            // The insert above always produces rowid = 1 on a fresh `projects` table.
            db.execSQL("ALTER TABLE schedule_items ADD COLUMN projectId INTEGER NOT NULL DEFAULT 1")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_schedule_items_projectId ON schedule_items(projectId)")
            db.execSQL("ALTER TABLE topic_folders ADD COLUMN projectId INTEGER NOT NULL DEFAULT 1")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_topic_folders_projectId ON topic_folders(projectId)")
        }
    }

    /**
     * v4 → v5: Streak style overhaul data.
     *   1. Add `color` column to projects (default brand lime) for per-project color tags.
     *   2. Create the `achievements` table that records when each achievement was unlocked.
     */
    private val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE projects ADD COLUMN color TEXT NOT NULL DEFAULT '#5BE32A'"
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS achievements (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    code TEXT NOT NULL,
                    unlockedAt INTEGER NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS index_achievements_code ON achievements(code)"
            )
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "maayan.db")
            .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideProjectDao(db: AppDatabase): ProjectDao = db.projectDao()
    @Provides fun provideScheduleItemDao(db: AppDatabase): ScheduleItemDao = db.scheduleItemDao()
    @Provides fun provideTimerSessionDao(db: AppDatabase): TimerSessionDao = db.timerSessionDao()
    @Provides fun provideTopicFolderDao(db: AppDatabase): TopicFolderDao = db.topicFolderDao()
    @Provides fun provideNoteDao(db: AppDatabase): NoteDao = db.noteDao()
    @Provides fun provideAchievementDao(db: AppDatabase): AchievementDao = db.achievementDao()
}
