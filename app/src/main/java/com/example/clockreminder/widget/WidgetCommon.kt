package com.example.clockreminder.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews
import com.example.clockreminder.R
import com.example.clockreminder.data.AppDatabase
import com.example.clockreminder.data.WidgetSettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object WidgetCommon {

    fun updateWidgets(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
        layoutId: Int = R.layout.widget_clock
    ) {
        if (appWidgetIds.isEmpty()) return
        val style = WidgetSettingsRepository(context).getStyle()
        
        CoroutineScope(Dispatchers.IO).launch {
            val reminders = AppDatabase.get(context).reminderDao().getAllEnabled()
            val sizePx = 640
            val bmp = ClockFaceRenderer.render(sizePx, reminders, style)
            for (id in appWidgetIds) {
                val views = RemoteViews(context.packageName, layoutId)
                views.setImageViewBitmap(R.id.widget_clock_image, bmp)
                appWidgetManager.updateAppWidget(id, views)
            }
        }
    }

    fun updateAllInstances(context: Context, providerClass: Class<*>) {
        val mgr = AppWidgetManager.getInstance(context)
        val ids = mgr.getAppWidgetIds(ComponentName(context, providerClass))
        if (ids.isNotEmpty()) {
            updateWidgets(context, mgr, ids)
        }
    }

    /** Обновить единственный оставшийся виджет во всех его экземплярах. */
    fun updateAllStyles(context: Context) {
        updateAllInstances(context, ClockWidgetProvider::class.java)
    }
}
