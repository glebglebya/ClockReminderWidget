package com.example.clockreminder.data

import android.content.Context
import com.example.clockreminder.widget.ClockFaceRenderer

class WidgetSettingsRepository(private val context: Context) {
    private val prefs = context.getSharedPreferences("widget_settings", Context.MODE_PRIVATE)

    fun getStyle(): ClockFaceRenderer.WidgetStyle {
        val name = prefs.getString("style", ClockFaceRenderer.WidgetStyle.STYLE_1.name)
        return try {
            ClockFaceRenderer.WidgetStyle.valueOf(name!!)
        } catch (e: Exception) {
            ClockFaceRenderer.WidgetStyle.STYLE_1
        }
    }

    fun setStyle(style: ClockFaceRenderer.WidgetStyle) {
        prefs.edit().putString("style", style.name).apply()
    }
}
