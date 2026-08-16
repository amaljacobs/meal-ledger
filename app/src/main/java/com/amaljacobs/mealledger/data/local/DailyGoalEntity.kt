package com.amaljacobs.mealledger.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_goals")
data class DailyGoalEntity(
    @PrimaryKey
    @ColumnInfo(name = "effective_date") val effectiveDate: String,
    @ColumnInfo(name = "daily_water_goal_ml") val dailyWaterGoalMl: Int,
    @ColumnInfo(name = "daily_calorie_goal") val dailyCalorieGoal: Int?,
    @ColumnInfo(name = "daily_protein_goal_grams") val dailyProteinGoalGrams: Int?,
)
