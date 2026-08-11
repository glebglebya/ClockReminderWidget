package com.example.clockreminder.model

/**
 * Тип повторения напоминания.
 *
 * INTERVAL_HOURS — "раз в N часов начиная с даты/времени X"
 * DAILY_RANGE    — "каждый день в HH:mm, в диапазоне дат [start; end]"
 */
enum class RepeatType {
    INTERVAL_HOURS,
    DAILY_RANGE
}
