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
        
        // Определяем какой это провайдер, чтобы взять нужный стиль
        val isHorizontal = appWidgetIds.any { id ->
            val info = appWidgetManager.getAppWidgetInfo(id)
            info?.provider?.className?.contains("HorizontalWidgetProvider") == true
        }
        
        val settings = WidgetSettingsRepository(context)
        val style = if (isHorizontal) settings.getHorizontalStyle() else settings.getCircularStyle()
        
        CoroutineScope(Dispatchers.IO).launch {
            val reminders = AppDatabase.get(context).reminderDao().getAllEnabled()
            
            for (id in appWidgetIds) {
                val options = appWidgetManager.getAppWidgetOptions(id)
                val minWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
                val minHeightDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
                
                // Расчет пикселей
                val density = context.resources.displayMetrics.density
                val wPx = (minWidthDp * density).toInt().coerceAtLeast(300)
                
                // Ограничиваем высоту, чтобы при 3х1 или 5х1 она оставалась примерно как у 1й ячейки
                // Обычная высота ячейки ~70-100dp. Возьмем 80dp как константу для горизонтального стиля.
                val hPx = if (isHorizontal) (80 * density).toInt() else (minHeightDp * density).toInt().coerceAtLeast(100)
                
                val bmp = ClockFaceRenderer.render(wPx, hPx, reminders, style)
                
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

    /** Обновить все виджеты всех типов. */
    fun updateAllStyles(context: Context) {
        updateAllInstances(context, ClockWidgetProvider::class.java)
        updateAllInstances(context, HorizontalWidgetProvider::class.java)
    }
}
