package com.maayan.studytracker.data.repository

import com.maayan.studytracker.data.dao.DailyTotal
import com.maayan.studytracker.data.dao.TimerSessionDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatsRepository @Inject constructor(
    private val dao: TimerSessionDao
) {
    fun observeAllDailyTotals(): Flow<List<DailyTotal>> = dao.observeAllDailyTotals()
    fun observeSessionCount(): Flow<Int> = dao.observeSessionCount()
}
