package com.amaljacobs.mealledger.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyGoalDao {
    @Query("SELECT * FROM daily_goals ORDER BY effective_date ASC")
    fun observeAll(): Flow<List<DailyGoalEntity>>

    @Query("SELECT COUNT(*) FROM daily_goals")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(goal: DailyGoalEntity)
}
