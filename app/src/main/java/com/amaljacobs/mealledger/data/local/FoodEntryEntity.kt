package com.amaljacobs.mealledger.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "food_entries",
    indices = [Index(value = ["consumed_at"])],
)
data class FoodEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    @ColumnInfo(name = "consumed_at") val consumedAt: Instant,
    @ColumnInfo(name = "meal_type") val mealType: MealType? = null,
    @ColumnInfo(name = "portion_note") val portionNote: String? = null,
    val calories: Int? = null,
    @ColumnInfo(name = "price_minor") val priceMinor: Long? = null,
    @ColumnInfo(name = "currency_code") val currencyCode: String? = null,
    val note: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant,
)
