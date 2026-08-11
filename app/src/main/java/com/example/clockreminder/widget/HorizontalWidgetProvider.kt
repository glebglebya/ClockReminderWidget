package com.example.clockreminder.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context

class HorizontalWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        WidgetCommon.updateWidgets(context, appWidgetManager, appWidgetIds)
    }

    override fun onEnabled(context: Context) {
        WidgetUpdateScheduler.schedule(context)
    }

    override fun onDisabled(context: Context) {
        WidgetUpdateScheduler.checkAndCancel(context)
    }
}
