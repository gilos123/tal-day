package com.maayan.studytracker.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maayan.studytracker.data.dao.DailyTotal
import com.maayan.studytracker.data.repository.StatsRepository
import com.maayan.studytracker.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/** One bar in a day-level chart. Always present for every calendar day in the window. */
data class DailyBar(val date: LocalDate, val minutes: Double)

/** Summary of a completed week (7 days). */
data class WeekAverage(
    val weekStart: LocalDate,
    val weekEnd: LocalDate,
    val totalMinutes: Double,
    val averageMinutesPerDay: Double
)

data class StatsUiState(
    val isLoading: Boolean = true,
    val hasAnyData: Boolean = false,

    // Headline numbers
    val todayMinutes: Double = 0.0,
    val thisWeekTotalMinutes: Double = 0.0,
    val thisWeekDaysElapsed: Int = 0,
    val thisWeekAveragePerElapsedDay: Double = 0.0,
    val currentStreakDays: Int = 0,

    // Lifetime
    val totalStudyMinutes: Double = 0.0,
    val totalSessions: Int = 0,

    // Charts / lists
    val currentWeekBars: List<DailyBar> = emptyList(),
    val dailyLast14: List<DailyBar> = emptyList(),
    val previousWeeks: List<WeekAverage> = emptyList()
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val repository: StatsRepository
) : ViewModel() {

    val state: StateFlow<StatsUiState> = combine(
        repository.observeAllDailyTotals(),
        repository.observeSessionCount()
    ) { rawTotals, sessionCount ->
        buildUiState(rawTotals, sessionCount, today = LocalDate.now())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StatsUiState(isLoading = true)
    )

    internal fun buildUiState(
        rawTotals: List<DailyTotal>,
        sessionCount: Int,
        today: LocalDate
    ): StatsUiState {
        val byDate: Map<LocalDate, Long> = rawTotals
            .mapNotNull { dt ->
                val parsed = DateUtils.fromIsoOrNull(dt.date) ?: return@mapNotNull null
                parsed to dt.totalSeconds.coerceAtLeast(0L)
            }
            .toMap()

        val lifetimeSeconds = byDate.values.sum()
        val hasAnyData = lifetimeSeconds > 0L || sessionCount > 0

        val todaySeconds = byDate[today] ?: 0L

        val weekStart = DateUtils.startOfWeek(today)
        val daysElapsedInWeek = (ChronoUnit.DAYS.between(weekStart, today).toInt() + 1)
            .coerceIn(1, 7)
        val thisWeekSeconds = (0 until daysElapsedInWeek).sumOf { offset ->
            byDate[weekStart.plusDays(offset.toLong())] ?: 0L
        }
        val thisWeekAvgPerElapsed = if (daysElapsedInWeek > 0)
            (thisWeekSeconds.toDouble() / 60.0) / daysElapsedInWeek
        else 0.0

        val currentWeekBars: List<DailyBar> = (0..6).map { offset ->
            val date = weekStart.plusDays(offset.toLong())
            DailyBar(date = date, minutes = (byDate[date] ?: 0L) / 60.0)
        }

        val dailyLast14: List<DailyBar> = (0 until 14).map { offset ->
            val date = today.minusDays((13 - offset).toLong())
            DailyBar(date = date, minutes = (byDate[date] ?: 0L) / 60.0)
        }

        val previousWeeks: List<WeekAverage> = (1..6).map { weeksAgo ->
            val start = weekStart.minusWeeks(weeksAgo.toLong())
            val end = start.plusDays(6)
            val weekTotalSec = (0..6).sumOf { offset ->
                byDate[start.plusDays(offset.toLong())] ?: 0L
            }
            val totalMinutes = weekTotalSec / 60.0
            WeekAverage(
                weekStart = start,
                weekEnd = end,
                totalMinutes = totalMinutes,
                averageMinutesPerDay = totalMinutes / 7.0
            )
        }

        val streak = computeStreak(byDate, today)

        return StatsUiState(
            isLoading = false,
            hasAnyData = hasAnyData,
            todayMinutes = todaySeconds / 60.0,
            thisWeekTotalMinutes = thisWeekSeconds / 60.0,
            thisWeekDaysElapsed = daysElapsedInWeek,
            thisWeekAveragePerElapsedDay = thisWeekAvgPerElapsed,
            currentStreakDays = streak,
            totalStudyMinutes = lifetimeSeconds / 60.0,
            totalSessions = sessionCount,
            currentWeekBars = currentWeekBars,
            dailyLast14 = dailyLast14,
            previousWeeks = previousWeeks
        )
    }

    private fun computeStreak(byDate: Map<LocalDate, Long>, today: LocalDate): Int {
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
}
