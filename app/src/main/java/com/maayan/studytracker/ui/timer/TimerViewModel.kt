package com.maayan.studytracker.ui.timer

import android.content.Context
import androidx.lifecycle.ViewModel
import com.maayan.studytracker.service.TimerForegroundService
import com.maayan.studytracker.service.TimerStateHolder
import com.maayan.studytracker.service.TimerUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * Shared read-only view of the currently running timer, used by both the full [TimerScreen]
 * and the persistent [TimerMiniBar]. The actual countdown runs in [TimerForegroundService];
 * state is held in the singleton [TimerStateHolder].
 */
@HiltViewModel
class TimerViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val stateHolder: TimerStateHolder
) : ViewModel() {

    val state: StateFlow<TimerUiState> = stateHolder.state

    fun stop() {
        TimerForegroundService.stop(appContext)
    }

    fun clearFinishedState() = stateHolder.reset()
}
