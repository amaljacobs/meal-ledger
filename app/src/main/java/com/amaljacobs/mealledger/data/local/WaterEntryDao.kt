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
interface WaterEntryDao {
    @Query("SELECT * FROM water_entries WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): WaterEntryEntity?

    @Query(
        """
        SELECT * FROM water_entries
        WHERE consumed_at >= :startInclusive AND consumed_at < :endExclusive
        ORDER BY consumed_at DESC, id DESC
        """,
    )
    fun observeBetween(startInclusive: Instant, endExclusive: Instant): Flow<List<WaterEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entry: WaterEntryEntity): Long

    @Update
    suspend fun update(entry: WaterEntryEntity)

    @Delete
    suspend fun delete(entry: WaterEntryEntity)
}
