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
        layoutId: Int = R.layout.widget_clock,
    ) {
        if (appWidgetIds.isEmpty()) return
        
        // Определяем какой это провайдер, чтобы взять нужный стиль
        val isHorizontal = appWidgetIds.any { id ->
            val info = appWidgetManager.getAppWidgetInfo(id)
            info?.provider?.className?.contains("HorizontalWidgetProvider") == true
        }
        
        val settings = WidgetSettingsRepository(context)
        val style = if (isHorizontal) settings.getHorizontalStyle() else settings.getCircularStyle()
        
        val themeMode = settings.getThemeMode()
        val isSystemDark = (context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
        val isDark = when (themeMode) {
            WidgetSettingsRepository.WidgetThemeMode.SYSTEM -> isSystemDark
            WidgetSettingsRepository.WidgetThemeMode.LIGHT -> false
            WidgetSettingsRepository.WidgetThemeMode.DARK -> true
        }

        CoroutineScope(Dispatchers.IO).launch {
            val reminders = AppDatabase.get(context).reminderDao().getAllEnabled()
            
            for (id in appWidgetIds) {
                val options = appWidgetManager.getAppWidgetOptions(id)
                var minWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
                var minHeightDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
                
                // Если значения 0 (бывает при первой установке), берем из манифеста
                if (minWidthDp == 0 || minHeightDp == 0) {
                    val info = appWidgetManager.getAppWidgetInfo(id)
                    minWidthDp = info?.minWidth ?: 200
                    minHeightDp = info?.minHeight ?: 100
                }

                val density = context.resources.displayMetrics.density
                val wPx = (minWidthDp * density).toInt().coerceAtLeast(512)
                
                // Для горизонтального виджета разрешаем высоту от 70 до 180 dp
                // Для круглого - используем всю доступную высоту (он сам выберет minOf(w, h))
                val hPx = if (isHorizontal) {
                    val targetH = minHeightDp.coerceIn(70, 180)
                    (targetH * density).toInt()
                } else {
                    (minHeightDp * density).toInt().coerceAtLeast(512)
                }
                
                val bmp = ClockFaceRenderer.render(context, wPx, hPx, reminders, style, isDark)
                
                val views = RemoteViews(context.packageName, layoutId)
                // Для Material 3 на Android 12+ можно было бы использовать динамические цвета 
                // через RemoteViews, но так как мы рисуем Bitmap, мы имитируем их в рендерере.
                
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
