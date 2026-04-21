package com.maayan.studytracker.data.repository

import com.maayan.studytracker.data.dao.DailyTotal
import com.maayan.studytracker.data.dao.TimerSessionDao
import com.maayan.studytracker.data.db.entities.TimerSessionEntity
import com.maayan.studytracker.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatsRepository @Inject constructor(
    private val dao: TimerSessionDao
) {
    fun observeAllDailyTotals(): Flow<List<DailyTotal>> = dao.observeAllDailyTotals()
    fun observeSessionCount(): Flow<Int> = dao.observeSessionCount()

    /**
     * Reactive streak-days computation so both the Stats screen and the new
     * GamificationBar read the same number from the same source.
     */
    fun observeCurrentStreakDays(): Flow<Int> =
        observeAllDailyTotals().map { totals ->
            val byDate: Map<LocalDate, Long> = totals
                .mapNotNull { dt ->
                    val d = DateUtils.fromIsoOrNull(dt.date) ?: return@mapNotNull null
                    d to dt.totalSeconds.coerceAtLeast(0L)
                }.toMap()
            computeStreak(byDate, LocalDate.now())
        }

    suspend fun getAllSessions(): List<TimerSessionEntity> = dao.getAllSessions()
}

/**
 * Pure helper — promoted from StatsViewModel so other consumers (GamificationBar,
 * AchievementsRepository) share one definition. A day counts toward the streak if
 * it has any session; the count walks backwards from today (or yesterday if nothing
 * has been studied yet today — so the streak doesn't "break" just because it's 9 AM).
 */
internal fun computeStreak(byDate: Map<LocalDate, Long>, today: LocalDate): Int {
    val hasToday = (byDate[today] ?: 0L) > 0L
    var cursor = if (hasToday) today else today.minusDays(1)
    var count = 0
    while ((byDate[cursor] ?: 0L) > 0L) {
        count++
        cursor = cursor.minusDays(1)
        if (count > 10_000) break
    }
    return count
}
