package com.example.clockreminder.widget

import com.example.clockreminder.model.Reminder
import com.example.clockreminder.model.RepeatType
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/** Одно фактическое появление напоминания сегодня — время + цвет + подпись. */
data class Occurrence(val time: LocalTime, val colorArgb: Int, val label: String)

/**
 * Вычисляет все точки на циферблате, которые должны быть показаны СЕГОДНЯ,
 * исходя из правил повторения каждого напоминания.
 */
object ReminderOccurrences {

    fun todaysOccurrences(
        reminders: List<Reminder>,
        today: LocalDate = LocalDate.now(),
        zone: ZoneId = ZoneId.systemDefault()
    ): List<Occurrence> {
        val result = mutableListOf<Occurrence>()
        val dayStart = today.atStartOfDay(zone).toInstant().toEpochMilli()
        val dayEnd = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()

        for (r in reminders) {
            if (!r.enabled) continue
            when (r.repeatType) {
                RepeatType.INTERVAL_HOURS -> {
                    if (r.intervalHours <= 0) continue
                    val intervalMillis = r.intervalHours * 3_600_000L
                    if (r.startEpochMillis >= dayEnd) continue // ещё не началось

                    // находим первое появление >= dayStart
                    var t = r.startEpochMillis
                    if (t < dayStart) {
                        val steps = (dayStart - t + intervalMillis - 1) / intervalMillis
                        t += steps * intervalMillis
                    }
                    while (t < dayEnd) {
                        if (t >= dayStart) {
                            val ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(t), zone)
                            result.add(Occurrence(ldt.toLocalTime(), r.colorArgb, r.label))
                        }
                        t += intervalMillis
                    }
                }

                RepeatType.DAILY_RANGE -> {
                    val todayEpochDay = today.toEpochDay()
                    if (todayEpochDay in r.rangeStartEpochDay..r.rangeEndEpochDay) {
                        val time = LocalTime.ofSecondOfDay((r.timeOfDayMinutes * 60).toLong())
                        result.add(Occurrence(time, r.colorArgb, r.label))
                    }
                }
            }
        }
        return result
    }
}
