package com.maayan.studytracker.ui.achievements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maayan.studytracker.data.db.entities.AchievementEntity
import com.maayan.studytracker.data.repository.AchievementsRepository
import com.maayan.studytracker.domain.AchievementCatalogue
import com.maayan.studytracker.domain.AchievementRule
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class AchievementUi(
    val rule: AchievementRule,
    val unlocked: Boolean,
    val unlockedAt: Long?
)

@HiltViewModel
class AchievementsViewModel @Inject constructor(
    achievementsRepository: AchievementsRepository
) : ViewModel() {

    val achievements: StateFlow<List<AchievementUi>> = achievementsRepository.observeUnlocked()
        .map { list: List<AchievementEntity> -> joinWithCatalogue(list) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), seedLockedCatalogue())

    private fun seedLockedCatalogue(): List<AchievementUi> =
        AchievementCatalogue.all.map { AchievementUi(it, unlocked = false, unlockedAt = null) }

    private fun joinWithCatalogue(unlockedEntries: List<AchievementEntity>): List<AchievementUi> {
        val byCode = unlockedEntries.associateBy { it.code }
        return AchievementCatalogue.all.map { rule ->
            val entry = byCode[rule.code]
            AchievementUi(rule = rule, unlocked = entry != null, unlockedAt = entry?.unlockedAt)
        }
    }
}
