package com.example.clockreminder.widget

import com.example.clockreminder.model.IntervalUnit
import com.example.clockreminder.model.Reminder
import com.example.clockreminder.model.RepeatType
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

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
        zone: ZoneId = ZoneId.systemDefault(),
    ): List<Occurrence> {
        val result = mutableListOf<Occurrence>()
        val dayStartInstant = today.atStartOfDay(zone).toInstant()
        val dayEndInstant = today.plusDays(1).atStartOfDay(zone).toInstant()
        
        val dayStart = dayStartInstant.toEpochMilli()
        val dayEnd = dayEndInstant.toEpochMilli()

        for (r in reminders) {
            if (!r.enabled) continue
            
            when (r.repeatType) {
                RepeatType.ONE_TIME -> {
                    if (r.startEpochMillis in (dayStart until dayEnd)) {
                        val ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(r.startEpochMillis), zone)
                        result.add(Occurrence(ldt.toLocalTime(), r.colorArgb, r.label))
                    }
                }
                RepeatType.REPEATING -> {
                    if (r.intervalValue <= 0) continue
                    if (r.startEpochMillis >= dayEnd) continue
                    if (r.endEpochMillis != null && r.endEpochMillis < dayStart) continue

                    var current = Instant.ofEpochMilli(r.startEpochMillis).atZone(zone)
                    var occurrenceIndex = 0L

                    // Пропускаем интервалы до начала сегодняшнего дня
                    if (current.toInstant().isBefore(dayStartInstant)) {
                        when (r.intervalUnit) {
                            IntervalUnit.MINUTES, IntervalUnit.HOURS -> {
                                val unitMillis = if (r.intervalUnit == IntervalUnit.MINUTES) 60_000L else 3_600_000L
                                val intervalMillis = r.intervalValue * unitMillis
                                val diff = dayStart - r.startEpochMillis
                                occurrenceIndex = (diff + intervalMillis - 1) / intervalMillis
                                current = current.plus(occurrenceIndex * intervalMillis, ChronoUnit.MILLIS)
                            }
                            IntervalUnit.DAYS -> {
                                val startLocalDate = current.toLocalDate()
                                val daysBetween = ChronoUnit.DAYS.between(startLocalDate, today)
                                occurrenceIndex = (daysBetween + r.intervalValue - 1) / r.intervalValue
                                current = current.plusDays(occurrenceIndex * r.intervalValue)
                            }
                        }
                    }

                    // Собираем все вхождения в течение сегодняшнего дня
                    while (current.toInstant().isBefore(dayEndInstant)) {
                        val t = current.toInstant().toEpochMilli()
                        
                        // Проверяем лимиты: дата окончания и количество повторений
                        if (r.endEpochMillis != null && t > r.endEpochMillis) break
                        if (r.repeatCount != null && occurrenceIndex >= r.repeatCount) break

                        if (t >= dayStart) {
                            result.add(Occurrence(current.toLocalTime(), r.colorArgb, r.label))
                        }
                        
                        current = when (r.intervalUnit) {
                            IntervalUnit.MINUTES -> current.plusMinutes(r.intervalValue.toLong())
                            IntervalUnit.HOURS -> current.plusHours(r.intervalValue.toLong())
                            IntervalUnit.DAYS -> current.plusDays(r.intervalValue.toLong())
                        }
                        occurrenceIndex++
                    }
                }
            }
        }
        return result
    }
}
