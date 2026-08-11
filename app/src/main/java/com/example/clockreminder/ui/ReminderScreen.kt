package com.example.clockreminder.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.clockreminder.data.ReminderRepository
import com.example.clockreminder.model.IntervalUnit
import com.example.clockreminder.model.Reminder
import com.example.clockreminder.model.RepeatType
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderScreen(repo: ReminderRepository) {
    val scope = rememberCoroutineScope()
    val reminders by repo.observeAll().collectAsStateWithLifecycle(initialValue = emptyList())
    var showDialog by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Reminder?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Часы-напоминания") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { editing = null; showDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Добавить")
            }
        }
    ) { padding ->
        if (reminders.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Нет напоминаний. Нажмите + чтобы добавить.")
            }
        } else {
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(reminders, key = { it.id }) { reminder ->
                    ReminderRow(
                        reminder = reminder,
                        onClick = { editing = reminder; showDialog = true },
                        onDelete = { scope.launch { repo.delete(reminder) } }
                    )
                }
            }
        }
    }

    if (showDialog) {
        AddEditReminderDialog(
            existing = editing,
            onDismiss = { showDialog = false },
            onSave = { reminder ->
                scope.launch {
                    if (editing == null) repo.add(reminder) else repo.update(reminder)
                }
                showDialog = false
            }
        )
    }
}

@Composable
private fun ReminderRow(reminder: Reminder, onClick: () -> Unit, onDelete: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(Color(reminder.colorArgb))
        )
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(reminder.label.ifBlank { "Напоминание" }, style = MaterialTheme.typography.bodyLarge)
            Text(describeSchedule(reminder), style = MaterialTheme.typography.bodySmall)
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Delete, contentDescription = "Удалить")
        }
    }
}

private fun describeSchedule(r: Reminder): String {
    val start = Instant.ofEpochMilli(r.startEpochMillis)
        .atZone(ZoneId.systemDefault())
    val startStr = start.format(DateTimeFormatter.ofPattern("dd.MM HH:mm"))

    return when (r.repeatType) {
        RepeatType.ONE_TIME -> "Разовое: $startStr"
        RepeatType.REPEATING -> {
            val unitStr = when (r.intervalUnit) {
                IntervalUnit.MINUTES -> "мин"
                IntervalUnit.HOURS -> "ч"
                IntervalUnit.DAYS -> "дн"
            }
            val base = "Каждые ${r.intervalValue} $unitStr, с $startStr"
            if (r.endEpochMillis != null) {
                val end = Instant.ofEpochMilli(r.endEpochMillis)
                    .atZone(ZoneId.systemDefault())
                base + " по " + end.format(DateTimeFormatter.ofPattern("dd.MM HH:mm"))
            } else {
                base
            }
        }
    }
}
