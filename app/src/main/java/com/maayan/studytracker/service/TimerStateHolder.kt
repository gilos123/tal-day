package com.maayan.studytracker.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class TimerUiState(
    val scheduleItemId: Long = -1L,
    val topicName: String = "",
    val date: String = "",
    val plannedSeconds: Long = 0,
    val remainingSeconds: Long = 0,
    val running: Boolean = false,
    val finished: Boolean = false
)

@Singleton
class TimerStateHolder @Inject constructor() {
    private val _state = MutableStateFlow(TimerUiState())
    val state: StateFlow<TimerUiState> = _state.asStateFlow()

    fun update(newState: TimerUiState) { _state.value = newState }
    fun reset() { _state.value = TimerUiState() }
}
