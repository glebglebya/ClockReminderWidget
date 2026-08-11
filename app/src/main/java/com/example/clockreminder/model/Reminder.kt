package com.example.clockreminder.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Одно напоминание, отображаемое цветным кружком на циферблате.
 *
 * Поля startEpochMillis / intervalHours используются только для RepeatType.INTERVAL_HOURS.
 * Поля timeOfDayMinutes / rangeStartEpochDay / rangeEndEpochDay используются только
 * для RepeatType.DAILY_RANGE.
 */
@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String = "",
    val colorArgb: Int,
    val repeatType: RepeatType,

    // --- INTERVAL_HOURS ---
    val startEpochMillis: Long = 0L,
    val intervalHours: Int = 0,

    // --- DAILY_RANGE ---
    val timeOfDayMinutes: Int = 0,       // минуты от полуночи, например 16:00 -> 960
    val rangeStartEpochDay: Long = 0L,   // LocalDate.toEpochDay()
    val rangeEndEpochDay: Long = 0L,

    val enabled: Boolean = true
)
