package com.amaljacobs.mealledger.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface FoodEntryDao {
    @Query("SELECT * FROM food_entries WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): FoodEntryEntity?

    @Query(
        """
        SELECT * FROM food_entries
        WHERE consumed_at >= :startInclusive AND consumed_at < :endExclusive
        ORDER BY consumed_at DESC, id DESC
        """,
    )
    fun observeBetween(startInclusive: Instant, endExclusive: Instant): Flow<List<FoodEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entry: FoodEntryEntity): Long

    @Update
    suspend fun update(entry: FoodEntryEntity)

    @Delete
    suspend fun delete(entry: FoodEntryEntity)
}
