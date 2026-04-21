package com.maayan.studytracker.data.repository

import com.maayan.studytracker.data.dao.AchievementDao
import com.maayan.studytracker.data.db.entities.AchievementEntity
import com.maayan.studytracker.domain.AchievementCatalogue
import com.maayan.studytracker.domain.buildSnapshot
import com.maayan.studytracker.util.DateUtils
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AchievementsRepository @Inject constructor(
    private val achievementDao: AchievementDao,
    private val statsRepository: StatsRepository
) {
    fun observeUnlocked(): Flow<List<AchievementEntity>> = achievementDao.observeUnlocked()

    /**
     * Called from [com.maayan.studytracker.service.TimerForegroundService] right after
     * a session is persisted. Builds a fresh [com.maayan.studytracker.domain.StatsSnapshot]
     * from current DB state, runs every rule, and inserts new unlock rows for any rule
     * that is now satisfied and wasn't before. Safe to call repeatedly — the DAO's
     * unique index + IGNORE conflict strategy makes re-unlocks a no-op.
     */
    suspend fun checkAndUnlock() {
        val sessions = statsRepository.getAllSessions()
        val totals = sessions
            .mapNotNull { s ->
                val d = DateUtils.fromIsoOrNull(s.date) ?: return@mapNotNull null
                d to s.actualDurationSeconds.coerceAtLeast(0L)
            }
            .groupBy({ it.first }, { it.second })
            .mapValues { (_, secs) -> secs.sum() }

        val streak = computeStreak(totals, LocalDate.now())
        val snapshot = buildSnapshot(sessions = sessions, streakDays = streak)

        val now = System.currentTimeMillis()
        for (rule in AchievementCatalogue.all) {
            if (!rule.evaluate(snapshot)) continue
            if (achievementDao.isUnlocked(rule.code)) continue
            achievementDao.unlock(
                AchievementEntity(code = rule.code, unlockedAt = now)
            )
        }
    }
}
