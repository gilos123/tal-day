package com.maayan.studytracker.ui.schedule

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maayan.studytracker.data.db.entities.ScheduleItemEntity
import com.maayan.studytracker.data.repository.ScheduleRepository
import com.maayan.studytracker.service.TimerForegroundService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val repository: ScheduleRepository
) : ViewModel() {

    val items: StateFlow<List<ScheduleItemEntity>> =
        repository.observeItems()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addRow() {
        viewModelScope.launch { repository.addRow() }
    }

    fun updateTopicName(itemId: Long, name: String) {
        viewModelScope.launch { repository.updateTopicName(itemId, name) }
    }

    fun updateDuration(itemId: Long, minutes: Int) {
        viewModelScope.launch { repository.updateDuration(itemId, minutes) }
    }

    fun deleteRow(itemId: Long) {
        viewModelScope.launch { repository.deleteRow(itemId) }
    }

    /**
     * Toggles the "done today" checkbox on a row. Auto-resets at midnight because the UI
     * compares `lastDoneDate` to today's date.
     */
    fun toggleDone(itemId: Long, currentlyDoneToday: Boolean) {
        viewModelScope.launch {
            val nextDate: String? = if (currentlyDoneToday) null else LocalDate.now().toString()
            repository.setLastDoneDate(itemId, nextDate)
        }
    }

    /**
     * Starts the foreground timer for the given row and stays on the schedule screen.
     * The persistent [com.maayan.studytracker.ui.timer.TimerMiniBar] displays the countdown.
     */
    fun startTimerFor(scheduleItemId: Long) {
        viewModelScope.launch {
            val item = repository.getItemById(scheduleItemId) ?: return@launch
            val todayIso = LocalDate.now().toString()
            TimerForegroundService.start(
                context = appContext,
                scheduleItemId = item.id,
                topicName = item.topicName.ifBlank { "Session" },
                date = todayIso,
                plannedSeconds = item.plannedDurationMinutes.toLong() * 60L
            )
        }
    }
}
