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

/** Одно фактическое появление напоминания — дата/время + цвет + подпись. */
data class Occurrence(
    val time: LocalTime,
    val dateTime: LocalDateTime,
    val colorArgb: Int,
    val label: String
)

/**
 * Вычисляет точки на циферблате, исходя из правил повторения напоминаний.
 */
object ReminderOccurrences {

    fun todaysOccurrences(
        reminders: List<Reminder>,
        today: LocalDate = LocalDate.now(),
        zone: ZoneId = ZoneId.systemDefault(),
    ): List<Occurrence> {
        val start = today.atStartOfDay()
        val end = today.plusDays(1).atStartOfDay()
        return getOccurrencesInRange(reminders, start, end, zone)
    }

    fun getOccurrencesInRange(
        reminders: List<Reminder>,
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime,
        zone: ZoneId = ZoneId.systemDefault()
    ): List<Occurrence> {
        val result = mutableListOf<Occurrence>()
        val startInstant = startDateTime.atZone(zone).toInstant()
        val endInstant = endDateTime.atZone(zone).toInstant()
        
        val startMillis = startInstant.toEpochMilli()
        val endMillis = endInstant.toEpochMilli()

        for (r in reminders) {
            if (!r.enabled) continue
            
            when (r.repeatType) {
                RepeatType.ONE_TIME -> {
                    if (r.startEpochMillis in (startMillis..endMillis)) {
                        val ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(r.startEpochMillis), zone)
                        result.add(Occurrence(ldt.toLocalTime(), ldt, r.colorArgb, r.label))
                    }
                }
                RepeatType.REPEATING -> {
                    if (r.intervalValue <= 0) continue
                    if (r.startEpochMillis >= endMillis) continue
                    if (r.endEpochMillis != null && r.endEpochMillis < startMillis) continue

                    var current = Instant.ofEpochMilli(r.startEpochMillis).atZone(zone)
                    var occurrenceIndex = 0L

                    // Пропускаем интервалы до начала диапазона
                    if (current.toInstant().isBefore(startInstant)) {
                        when (r.intervalUnit) {
                            IntervalUnit.MINUTES, IntervalUnit.HOURS -> {
                                val unitMillis = if (r.intervalUnit == IntervalUnit.MINUTES) 60_000L else 3_600_000L
                                val intervalMillis = r.intervalValue * unitMillis
                                val diff = startMillis - r.startEpochMillis
                                occurrenceIndex = (diff + intervalMillis - 1) / intervalMillis
                                current = current.plus(occurrenceIndex * intervalMillis, ChronoUnit.MILLIS)
                            }
                            IntervalUnit.DAYS -> {
                                val startLocalDate = current.toLocalDate()
                                val daysBetween = ChronoUnit.DAYS.between(startLocalDate, startDateTime.toLocalDate())
                                occurrenceIndex = (daysBetween + r.intervalValue - 1) / r.intervalValue
                                current = current.plusDays(occurrenceIndex * r.intervalValue)
                                
                                // Если после прибавления дней мы всё еще до startMillis (из-за времени), шагаем еще
                                while (current.toInstant().toEpochMilli() < startMillis) {
                                    current = current.plusDays(r.intervalValue.toLong())
                                    occurrenceIndex++
                                }
                            }
                        }
                    }

                    // Собираем вхождения в диапазоне
                    while (!current.toInstant().isAfter(endInstant)) {
                        val t = current.toInstant().toEpochMilli()
                        
                        if (r.endEpochMillis != null && t > r.endEpochMillis) break
                        if (r.repeatCount != null && occurrenceIndex >= r.repeatCount) break

                        if (t >= startMillis) {
                            val ldt = current.toLocalDateTime()
                            result.add(Occurrence(ldt.toLocalTime(), ldt, r.colorArgb, r.label))
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
