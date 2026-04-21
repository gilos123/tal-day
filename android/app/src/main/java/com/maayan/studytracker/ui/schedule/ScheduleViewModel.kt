package com.maayan.studytracker.ui.schedule

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maayan.studytracker.data.db.entities.ProjectEntity
import com.maayan.studytracker.data.db.entities.ScheduleItemEntity
import com.maayan.studytracker.data.repository.ProjectRepository
import com.maayan.studytracker.data.repository.ScheduleRepository
import com.maayan.studytracker.service.TimerForegroundService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ScheduleViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val repository: ScheduleRepository,
    private val projectRepository: ProjectRepository
) : ViewModel() {

    // --- Projects ----------------------------------------------------------------------

    val projects: StateFlow<List<ProjectEntity>> =
        projectRepository.observeAll()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** id of the currently displayed project. Null until we resolve the first/default one. */
    private val _selectedProjectId = MutableStateFlow<Long?>(null)
    val selectedProjectId: StateFlow<Long?> = _selectedProjectId.asStateFlow()

    /** Convenience — the current project entity (or null while loading). */
    val selectedProject: StateFlow<ProjectEntity?> =
        combine(projects, _selectedProjectId) { list, id ->
            list.firstOrNull { it.id == id }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // --- Schedule items for the currently selected project ----------------------------

    val items: StateFlow<List<ScheduleItemEntity>> = _selectedProjectId
        .filterNotNull()
        .flatMapLatest { projectId -> repository.observeItemsForProject(projectId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            // On first launch after a v3→v4 migration the "My Schedule" project already
            // exists (id=1, seeded by the migration). On a fresh install no migration
            // runs — ensureAtLeastOneProject creates the default on the fly.
            val id = projectRepository.ensureAtLeastOneProject()
            if (_selectedProjectId.value == null) _selectedProjectId.value = id
        }
    }

    // --- Schedule CRUD ----------------------------------------------------------------

    fun addRow() {
        val projectId = _selectedProjectId.value ?: return
        viewModelScope.launch { repository.addRow(projectId) }
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

    // --- Project CRUD + selection -----------------------------------------------------

    fun selectProject(projectId: Long) {
        _selectedProjectId.value = projectId
    }

    fun createProject(name: String, color: String) {
        viewModelScope.launch {
            val id = projectRepository.createProject(name, color)
            _selectedProjectId.value = id
        }
    }

    fun renameProject(projectId: Long, newName: String) {
        viewModelScope.launch { projectRepository.renameProject(projectId, newName) }
    }

    fun setProjectColor(projectId: Long, colorHex: String) {
        viewModelScope.launch { projectRepository.setColor(projectId, colorHex) }
    }

    /**
     * Deletes a project and cascades its schedule items + notes. If the deleted project
     * was the currently-selected one, falls back to another project — creating a new
     * default one if the user had only this one.
     */
    fun deleteProject(projectId: Long) {
        viewModelScope.launch {
            projectRepository.deleteProject(projectId)
            if (_selectedProjectId.value == projectId) {
                val remaining = projectRepository.getAll()
                _selectedProjectId.value = if (remaining.isNotEmpty()) {
                    remaining.first().id
                } else {
                    projectRepository.createProject("My Schedule") // uses default color
                }
            }
        }
    }
}
