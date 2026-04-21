package com.maayan.studytracker.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maayan.studytracker.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val useDynamicColor: StateFlow<Boolean> = settingsRepository.useDynamicColor
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun setUseDynamicColor(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setUseDynamicColor(enabled) }
    }
}
