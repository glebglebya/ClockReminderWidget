package com.example.clockreminder.data

import android.content.Context
import com.example.clockreminder.widget.ClockFaceRenderer

class WidgetSettingsRepository(private val context: Context) {
    private val prefs = context.getSharedPreferences("widget_settings", Context.MODE_PRIVATE)

    fun getCircularStyle(): ClockFaceRenderer.WidgetStyle {
        val name = prefs.getString("circular_style", ClockFaceRenderer.WidgetStyle.STYLE_1.name)
        return try {
            ClockFaceRenderer.WidgetStyle.valueOf(name!!)
        } catch (e: Exception) {
            ClockFaceRenderer.WidgetStyle.STYLE_1
        }
    }

    fun setCircularStyle(style: ClockFaceRenderer.WidgetStyle) {
        prefs.edit().putString("circular_style", style.name).apply()
    }

    fun getHorizontalStyle(): ClockFaceRenderer.WidgetStyle {
        val name = prefs.getString("horizontal_style", ClockFaceRenderer.WidgetStyle.STYLE_5.name)
        return try {
            ClockFaceRenderer.WidgetStyle.valueOf(name!!)
        } catch (e: Exception) {
            ClockFaceRenderer.WidgetStyle.STYLE_5
        }
    }

    fun setHorizontalStyle(style: ClockFaceRenderer.WidgetStyle) {
        prefs.edit().putString("horizontal_style", style.name).apply()
    }
}
