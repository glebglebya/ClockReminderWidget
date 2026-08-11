package com.example.clockreminder.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.clockreminder.model.Reminder
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    @Query("SELECT * FROM reminders ORDER BY id DESC")
    fun observeAll(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE enabled = 1")
    suspend fun getAllEnabled(): List<Reminder>

    @Insert
    suspend fun insert(reminder: Reminder): Long

    @Update
    suspend fun update(reminder: Reminder)

    @Delete
    suspend fun delete(reminder: Reminder)
}
