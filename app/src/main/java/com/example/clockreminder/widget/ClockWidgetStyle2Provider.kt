package com.example.clockreminder.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context

class ClockWidgetStyle2Provider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        WidgetCommon.updateWidgets(
            context,
            appWidgetManager,
            appWidgetIds,
            faceStyle = ClockFaceRenderer.WidgetStyle.STYLE_2
        )
    }

    override fun onEnabled(context: Context) {
        WidgetUpdateScheduler.schedule(context)
    }

    override fun onDisabled(context: Context) {
        WidgetUpdateScheduler.checkAndCancel(context)
    }
}
