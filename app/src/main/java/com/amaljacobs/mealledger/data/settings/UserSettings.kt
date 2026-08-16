package com.amaljacobs.mealledger.data.settings

data class UserSettings(
    val currencyCode: String = "INR",
    val dailyWaterGoalMl: Int = 2_500,
    val cupSizeMl: Int = 250,
)
