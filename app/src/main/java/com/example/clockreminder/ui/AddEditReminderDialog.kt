package com.example.clockreminder.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.clockreminder.model.Reminder
import com.example.clockreminder.model.RepeatType
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val PALETTE = listOf(
    Color(0xFFFF3B30), Color(0xFF34C759), Color(0xFF007AFF),
    Color(0xFFFF9500), Color(0xFFAF52DE), Color(0xFFFFCC00), Color(0xFF5AC8FA)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditReminderDialog(
    existing: Reminder?,
    onDismiss: () -> Unit,
    onSave: (Reminder) -> Unit
) {
    val context = LocalContext.current
    var label by remember { mutableStateOf(existing?.label ?: "") }
    var color by remember { mutableStateOf(existing?.colorArgb?.let { Color(it) } ?: PALETTE[0]) }
    var mode by remember { mutableStateOf(existing?.repeatType ?: RepeatType.INTERVAL_HOURS) }

    // режим "раз в N часов"
    var startDate by remember {
        mutableStateOf(
            existing?.takeIf { it.repeatType == RepeatType.INTERVAL_HOURS }
                ?.let { Instant.ofEpochMilli(it.startEpochMillis).atZone(ZoneId.systemDefault()).toLocalDate() }
                ?: LocalDate.now()
        )
    }
    var startTime by remember {
        mutableStateOf(
            existing?.takeIf { it.repeatType == RepeatType.INTERVAL_HOURS }
                ?.let { Instant.ofEpochMilli(it.startEpochMillis).atZone(ZoneId.systemDefault()).toLocalTime() }
                ?: LocalTime.of(11, 0)
        )
    }
    var intervalHoursText by remember { mutableStateOf((existing?.intervalHours ?: 7).toString()) }

    // режим "каждый день в диапазоне дат"
    var dailyTime by remember {
        mutableStateOf(
            existing?.takeIf { it.repeatType == RepeatType.DAILY_RANGE }
                ?.let { LocalTime.ofSecondOfDay((it.timeOfDayMinutes * 60).toLong()) }
                ?: LocalTime.of(16, 0)
        )
    }
    var rangeStart by remember {
        mutableStateOf(
            existing?.takeIf { it.repeatType == RepeatType.DAILY_RANGE }
                ?.let { LocalDate.ofEpochDay(it.rangeStartEpochDay) }
                ?: LocalDate.now()
        )
    }
    var rangeEnd by remember {
        mutableStateOf(
            existing?.takeIf { it.repeatType == RepeatType.DAILY_RANGE }
                ?.let { LocalDate.ofEpochDay(it.rangeEndEpochDay) }
                ?: LocalDate.now().plusDays(3)
        )
    }

    fun pickDate(current: LocalDate, onPicked: (LocalDate) -> Unit) {
        DatePickerDialog(
            context,
            { _, y, m, d -> onPicked(LocalDate.of(y, m + 1, d)) },
            current.year, current.monthValue - 1, current.dayOfMonth
        ).show()
    }

    fun pickTime(current: LocalTime, onPicked: (LocalTime) -> Unit) {
        TimePickerDialog(
            context,
            { _, h, min -> onPicked(LocalTime.of(h, min)) },
            current.hour, current.minute, true
        ).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Новое напоминание" else "Изменить напоминание") },
        text = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Название") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))
                Text("Цвет")
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    PALETTE.forEach { c ->
                        val isSelected = c == color
                        Box(
                            Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) c.copy(alpha = 0.4f) else Color.Transparent)
                                .then(
                                    if (isSelected) Modifier.border(2.dp, c, CircleShape)
                                    else Modifier
                                )
                                .clickable { color = c }
                                .padding(6.dp)
                        ) {
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(c)
                            )
                        }
                        Spacer(Modifier.width(6.dp))
                    }
                }


                Spacer(Modifier.height(12.dp))
                Row {
                    FilterChip(
                        selected = mode == RepeatType.INTERVAL_HOURS,
                        onClick = { mode = RepeatType.INTERVAL_HOURS },
                        label = { Text("Через N часов") }
                    )
                    Spacer(Modifier.width(8.dp))
                    FilterChip(
                        selected = mode == RepeatType.DAILY_RANGE,
                        onClick = { mode = RepeatType.DAILY_RANGE },
                        label = { Text("Ежедневно, диапазон дат") }
                    )
                }

                Spacer(Modifier.height(12.dp))

                if (mode == RepeatType.INTERVAL_HOURS) {
                    OutlinedTextField(
                        value = intervalHoursText,
                        onValueChange = { intervalHoursText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Интервал, часов") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { pickDate(startDate) { startDate = it } },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Дата начала: " + startDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                    }
                    Spacer(Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = { pickTime(startTime) { startTime = it } },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Время начала: " + startTime.format(DateTimeFormatter.ofPattern("HH:mm")))
                    }
                } else {
                    OutlinedButton(
                        onClick = { pickTime(dailyTime) { dailyTime = it } },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Время: " + dailyTime.format(DateTimeFormatter.ofPattern("HH:mm")))
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { pickDate(rangeStart) { rangeStart = it } },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("С: " + rangeStart.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                    }
                    Spacer(Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = { pickDate(rangeEnd) { rangeEnd = it } },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("По: " + rangeEnd.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val reminder = when (mode) {
                    RepeatType.INTERVAL_HOURS -> {
                        val startMillis = LocalDateTime.of(startDate, startTime)
                            .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        Reminder(
                            id = existing?.id ?: 0,
                            label = label,
                            colorArgb = color.toArgbInt(),
                            repeatType = RepeatType.INTERVAL_HOURS,
                            startEpochMillis = startMillis,
                            intervalHours = intervalHoursText.toIntOrNull()?.coerceAtLeast(1) ?: 1
                        )
                    }
                    RepeatType.DAILY_RANGE -> Reminder(
                        id = existing?.id ?: 0,
                        label = label,
                        colorArgb = color.toArgbInt(),
                        repeatType = RepeatType.DAILY_RANGE,
                        timeOfDayMinutes = dailyTime.hour * 60 + dailyTime.minute,
                        rangeStartEpochDay = rangeStart.toEpochDay(),
                        rangeEndEpochDay = maxOf(rangeEnd.toEpochDay(), rangeStart.toEpochDay())
                    )
                }
                onSave(reminder)
            }) { Text("Сохранить") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}

private fun Color.toArgbInt(): Int = android.graphics.Color.argb(
    (alpha * 255).toInt(),
    (red * 255).toInt(),
    (green * 255).toInt(),
    (blue * 255).toInt()
)
