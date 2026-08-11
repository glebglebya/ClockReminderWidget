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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.clockreminder.model.IntervalUnit
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
    Color(0xFFFF9500), Color(0xFFAF52DE), Color(0xFFFFCC00), Color(0xFF5AC8FA),
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
    var repeatType by remember { mutableStateOf(existing?.repeatType ?: RepeatType.ONE_TIME) }

    // Дата и время начала
    var startDate by remember {
        mutableStateOf(
            existing?.let { Instant.ofEpochMilli(it.startEpochMillis).atZone(ZoneId.systemDefault()).toLocalDate() }
                ?: LocalDate.now()
        )
    }
    var startTime by remember {
        mutableStateOf(
            existing?.let { Instant.ofEpochMilli(it.startEpochMillis).atZone(ZoneId.systemDefault()).toLocalTime() }
                ?: LocalTime.now().withSecond(0).withNano(0)
        )
    }

    // Параметры повторения
    var intervalValueText by remember { mutableStateOf((existing?.intervalValue ?: 1).toString()) }
    var intervalUnit by remember { mutableStateOf(existing?.intervalUnit ?: IntervalUnit.DAYS) }

    // Окончание
    var hasEndDate by remember { mutableStateOf(existing?.endEpochMillis != null) }
    var endDate by remember {
        mutableStateOf(
            existing?.endEpochMillis?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate() }
                ?: LocalDate.now().plusWeeks(1)
        )
    }
    var endTime by remember {
        mutableStateOf(
            existing?.endEpochMillis?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalTime() }
                ?: LocalTime.of(23, 59)
        )
    }

    var hasRepeatCount by remember { mutableStateOf(existing?.repeatCount != null) }
    var repeatCountText by remember { mutableStateOf((existing?.repeatCount ?: 10).toString()) }

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

                Spacer(Modifier.height(16.dp))
                Text("Цвет")
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    PALETTE.forEach { c ->
                        val isSelected = c.toArgb() == color.toArgb()
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

                Spacer(Modifier.height(16.dp))
                Text("Тип")
                Row(Modifier.fillMaxWidth()) {
                    FilterChip(
                        selected = repeatType == RepeatType.ONE_TIME,
                        onClick = { repeatType = RepeatType.ONE_TIME },
                        label = { Text("Разовое") }
                    )
                    Spacer(Modifier.width(8.dp))
                    FilterChip(
                        selected = repeatType == RepeatType.REPEATING,
                        onClick = { repeatType = RepeatType.REPEATING },
                        label = { Text("Повторяющееся") }
                    )
                }

                Spacer(Modifier.height(16.dp))
                Text(if (repeatType == RepeatType.ONE_TIME) "Когда" else "Дата и время начала")
                Row(Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { pickDate(startDate) { startDate = it } },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(startDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                    }
                    Spacer(Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = { pickTime(startTime) { startTime = it } },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(startTime.format(DateTimeFormatter.ofPattern("HH:mm")))
                    }
                }

                if (repeatType == RepeatType.REPEATING) {
                    Spacer(Modifier.height(16.dp))
                    Text("Интервал")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = intervalValueText,
                            onValueChange = { intervalValueText = it.filter { ch -> ch.isDigit() } },
                            modifier = Modifier.width(80.dp),
                            singleLine = true
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Row {
                                FilterChip(
                                    selected = intervalUnit == IntervalUnit.MINUTES,
                                    onClick = { intervalUnit = IntervalUnit.MINUTES },
                                    label = { Text("Мин") }
                                )
                                Spacer(Modifier.width(4.dp))
                                FilterChip(
                                    selected = intervalUnit == IntervalUnit.HOURS,
                                    onClick = { intervalUnit = IntervalUnit.HOURS },
                                    label = { Text("Час") }
                                )
                                Spacer(Modifier.width(4.dp))
                                FilterChip(
                                    selected = intervalUnit == IntervalUnit.DAYS,
                                    onClick = { intervalUnit = IntervalUnit.DAYS },
                                    label = { Text("Дн") }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Ограничить количество повторений", modifier = Modifier.weight(1f))
                        Switch(checked = hasRepeatCount, onCheckedChange = { hasRepeatCount = it })
                    }
                    if (hasRepeatCount) {
                        OutlinedTextField(
                            value = repeatCountText,
                            onValueChange = { repeatCountText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Количество раз") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Ограничить дату окончания", modifier = Modifier.weight(1f))
                        Switch(checked = hasEndDate, onCheckedChange = { hasEndDate = it })
                    }

                    if (hasEndDate) {
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { pickDate(endDate) { endDate = it } },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(endDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                            }
                            Spacer(Modifier.width(8.dp))
                            OutlinedButton(
                                onClick = { pickTime(endTime) { endTime = it } },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(endTime.format(DateTimeFormatter.ofPattern("HH:mm")))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val startMillis = LocalDateTime.of(startDate, startTime)
                        .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

                    val endMillis = if (repeatType == RepeatType.REPEATING && hasEndDate) {
                        LocalDateTime.of(endDate, endTime)
                            .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    } else null

                    val repeatCount = if (repeatType == RepeatType.REPEATING && hasRepeatCount) {
                        repeatCountText.toIntOrNull()?.coerceAtLeast(1)
                    } else null

                    val reminder = Reminder(
                        id = existing?.id ?: 0,
                        label = label,
                        colorArgb = color.toArgb(),
                        repeatType = repeatType,
                        startEpochMillis = startMillis,
                        intervalValue = intervalValueText.toIntOrNull()?.coerceAtLeast(1) ?: 1,
                        intervalUnit = intervalUnit,
                        endEpochMillis = endMillis,
                        repeatCount = repeatCount,
                        enabled = existing?.enabled ?: true,
                    )
                    onSave(reminder)
                },
            ) { Text("Сохранить") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}
