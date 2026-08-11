package com.example.clockreminder.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Одно напоминание, отображаемое цветным кружком на циферблате.
 *
 * @param startEpochMillis Время первого (или единственного) срабатывания.
 * @param intervalValue Значение интервала (для RepeatType.REPEATING).
 * @param intervalUnit Единица измерения интервала (для RepeatType.REPEATING).
 * @param endEpochMillis Время окончания повторений (null — бесконечно).
 */
@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String = "",
    val colorArgb: Int,
    val repeatType: RepeatType,

    val startEpochMillis: Long,

    val intervalValue: Int = 0,
    val intervalUnit: IntervalUnit = IntervalUnit.DAYS,
    val endEpochMillis: Long? = null,

    val enabled: Boolean = true
)
