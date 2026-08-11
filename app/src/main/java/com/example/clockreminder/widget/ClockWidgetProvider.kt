package com.example.clockreminder.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews
import com.example.clockreminder.R
import com.example.clockreminder.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ClockWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        updateWidgets(context, appWidgetManager, appWidgetIds)
    }

    override fun onEnabled(context: Context) {
        // первый виджет добавлен на экран — запускаем ежеминутное обновление
        WidgetUpdateScheduler.schedule(context)
    }

    override fun onDisabled(context: Context) {
        // последний виджет удалён — останавливаем будильник
        WidgetUpdateScheduler.cancel(context)
    }

    companion object {

        fun updateWidgets(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
            if (appWidgetIds.isEmpty()) return
            CoroutineScope(Dispatchers.IO).launch {
                val reminders = AppDatabase.get(context).reminderDao().getAllEnabled()
                val sizePx = 640
                val bmp = ClockFaceRenderer.render(sizePx, reminders)
                for (id in appWidgetIds) {
                    val views = RemoteViews(context.packageName, R.layout.widget_clock)
                    views.setImageViewBitmap(R.id.widget_clock_image, bmp)
                    appWidgetManager.updateAppWidget(id, views)
                }
            }
        }

        /** Перерисовать все экземпляры этого виджета на всех экранах. */
        fun updateAll(context: Context) {
            val mgr = AppWidgetManager.getInstance(context)
            val ids = mgr.getAppWidgetIds(ComponentName(context, ClockWidgetProvider::class.java))
            if (ids.isNotEmpty()) updateWidgets(context, mgr, ids)
        }
    }
}
