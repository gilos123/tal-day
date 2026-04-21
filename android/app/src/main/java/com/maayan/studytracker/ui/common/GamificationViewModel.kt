package com.maayan.studytracker.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maayan.studytracker.data.repository.StatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlin.math.floor
import kotlin.math.sqrt
import javax.inject.Inject

data class GamificationState(
    val totalXp: Int = 0,
    val level: Int = 1,
    val streakDays: Int = 0
)

/**
 * Derives XP / level / streak from the existing reactive stats flows so the badges in
 * the top bar stay perfectly in sync with whatever the Stats screen would show.
 *
 *   XP          = total lifetime minutes studied (1 XP / minute)
 *   Level       = 1 + floor(sqrt(xp / 10)) — hits LVL 2 at 40 XP, LVL 5 at 250, LVL 10 at 1000
 *   Streak days = StatsRepository.observeCurrentStreakDays()
 */
@HiltViewModel
class GamificationViewModel @Inject constructor(
    private val statsRepository: StatsRepository
) : ViewModel() {

    val state: StateFlow<GamificationState> = combine(
        statsRepository.observeAllDailyTotals(),
        statsRepository.observeCurrentStreakDays()
    ) { totals, streak ->
        val totalSeconds = totals.sumOf { it.totalSeconds.coerceAtLeast(0L) }
        val xp = (totalSeconds / 60L).toInt().coerceAtLeast(0)
        val level = (1 + floor(sqrt(xp / 10.0)).toInt()).coerceAtLeast(1)
        GamificationState(totalXp = xp, level = level, streakDays = streak)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GamificationState())
}
