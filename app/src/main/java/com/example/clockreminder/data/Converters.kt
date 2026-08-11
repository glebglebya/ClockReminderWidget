package com.example.clockreminder.data

import androidx.room.TypeConverter
import com.example.clockreminder.model.RepeatType

class Converters {
    @TypeConverter
    fun fromRepeatType(value: RepeatType): String = value.name

    @TypeConverter
    fun toRepeatType(value: String): RepeatType = RepeatType.valueOf(value)
}
