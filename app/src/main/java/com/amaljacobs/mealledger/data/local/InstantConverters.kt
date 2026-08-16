package com.amaljacobs.mealledger.data.local

import androidx.room.TypeConverter
import java.time.Instant

class InstantConverters {
    @TypeConverter
    fun fromEpochMillis(value: Long): Instant = Instant.ofEpochMilli(value)

    @TypeConverter
    fun instantToEpochMillis(value: Instant): Long = value.toEpochMilli()

    @TypeConverter
    fun mealTypeToString(value: MealType?): String? = value?.name

    @TypeConverter
    fun stringToMealType(value: String?): MealType? = value?.let(MealType::valueOf)
}
