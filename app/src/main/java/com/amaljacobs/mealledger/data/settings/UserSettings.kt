package com.amaljacobs.mealledger.data.settings

data class UserSettings(
    val currencyCode: String = "INR",
    val dailyWaterGoalMl: Int = 2_500,
    val dailyCalorieGoal: Int? = null,
    val dailyProteinGoalGrams: Int? = null,
    val cupSizeMl: Int = 250,
)
