package com.maayan.studytracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.maayan.studytracker.data.db.entities.AchievementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements ORDER BY unlockedAt DESC")
    fun observeUnlocked(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements")
    suspend fun getAllUnlocked(): List<AchievementEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM achievements WHERE code = :code)")
    suspend fun isUnlocked(code: String): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun unlock(achievement: AchievementEntity)
}
