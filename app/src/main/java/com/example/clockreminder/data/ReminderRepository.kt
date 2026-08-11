package com.example.clockreminder.data

import android.content.Context
import com.example.clockreminder.model.Reminder
import com.example.clockreminder.widget.ClockWidgetProvider
import kotlinx.coroutines.flow.Flow

class ReminderRepository(private val context: Context) {

    private val dao = AppDatabase.get(context).reminderDao()

    fun observeAll(): Flow<List<Reminder>> = dao.observeAll()

    suspend fun add(reminder: Reminder) {
        dao.insert(reminder)
        ClockWidgetProvider.updateAll(context)
    }

    suspend fun update(reminder: Reminder) {
        dao.update(reminder)
        ClockWidgetProvider.updateAll(context)
    }

    suspend fun delete(reminder: Reminder) {
        dao.delete(reminder)
        ClockWidgetProvider.updateAll(context)
    }
}
