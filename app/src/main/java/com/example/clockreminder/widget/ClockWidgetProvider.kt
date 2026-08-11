package com.example.clockreminder.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context

import android.content.Intent

class ClockWidgetProvider : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        // Обновляем при смене конфигурации (например, темы)
        if (intent.action == Intent.ACTION_CONFIGURATION_CHANGED) {
            updateAll(context)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        WidgetCommon.updateWidgets(context, appWidgetManager, appWidgetIds)
    }

    override fun onEnabled(context: Context) {
        WidgetUpdateScheduler.schedule(context)
    }

    override fun onDisabled(context: Context) {
        WidgetUpdateScheduler.checkAndCancel(context)
    }

    companion object {
        /** Перерисовать все экземпляры этого виджета на всех экранах. */
        fun updateAll(context: Context) {
            WidgetCommon.updateAllInstances(context, ClockWidgetProvider::class.java)
        }
    }
}
