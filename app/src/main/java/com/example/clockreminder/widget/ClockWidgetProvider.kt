package com.example.clockreminder.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context

class ClockWidgetProvider : AppWidgetProvider() {

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
