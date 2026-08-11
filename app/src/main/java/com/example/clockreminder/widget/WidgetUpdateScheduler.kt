package com.example.clockreminder.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock

/**
 * Планирует широковещательный будильник раз в минуту, чтобы перерисовывать
 * стрелки и актуальные напоминания на циферблате виджета.
 *
 * Используется setInexactRepeating — точная поминутная точность не гарантируется
 * системой (Doze/батарея), но для аналоговых часов на виджете это стандартная
 * и не требующая специальных разрешений практика.
 */
object WidgetUpdateScheduler {
    private const val REQUEST_CODE = 1001

    private fun pendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, WidgetUpdateReceiver::class.java).apply {
            action = WidgetUpdateReceiver.ACTION_TICK
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(context, REQUEST_CODE, intent, flags)
    }

    fun schedule(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setInexactRepeating(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime(),
            60_000L,
            pendingIntent(context)
        )
    }

    fun checkAndCancel(context: Context) {
        val mgr = android.appwidget.AppWidgetManager.getInstance(context)
        val ids1 = mgr.getAppWidgetIds(android.content.ComponentName(context, ClockWidgetProvider::class.java))
        val ids2 = mgr.getAppWidgetIds(android.content.ComponentName(context, ClockWidgetStyle2Provider::class.java))
        
        if (ids1.isEmpty() && ids2.isEmpty()) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent(context))
        }
    }

    fun cancel(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent(context))
    }
}
