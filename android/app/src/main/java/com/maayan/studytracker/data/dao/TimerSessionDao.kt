package com.maayan.studytracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.maayan.studytracker.data.db.entities.TimerSessionEntity
import kotlinx.coroutines.flow.Flow

data class DailyTotal(val date: String, val totalSeconds: Long)

@Dao
interface TimerSessionDao {
    @Insert
    suspend fun insert(session: TimerSessionEntity): Long

    /**
     * Reactive totals per date across the entire table. We intentionally aggregate the
     * entire history rather than a narrow window so the ViewModel can compute today's
     * total, the current week, the trailing 14 days, prior week averages, and lifetime
     * totals from one single source of truth — avoiding multiple overlapping flows.
     */
    @Query("""
        SELECT date AS date, COALESCE(SUM(actualDurationSeconds), 0) AS totalSeconds
        FROM timer_sessions
        GROUP BY date
        ORDER BY date ASC
    """)
    fun observeAllDailyTotals(): Flow<List<DailyTotal>>

    /** Reactive lifetime session count. */
    @Query("SELECT COUNT(*) FROM timer_sessions")
    fun observeSessionCount(): Flow<Int>

    /**
     * One-shot snapshot of every session ever recorded. Used by the achievement
     * evaluator to compute single-session rules (Marathon, Night Owl, etc.) without
     * needing a continuously-streamed flow.
     */
    @Query("SELECT * FROM timer_sessions ORDER BY completedAt ASC")
    suspend fun getAllSessions(): List<TimerSessionEntity>
}
