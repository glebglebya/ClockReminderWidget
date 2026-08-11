package com.example.clockreminder.data

import android.content.Context
import androidx.core.content.edit
import com.example.clockreminder.widget.ClockFaceRenderer

class WidgetSettingsRepository(context: Context) {
    private val prefs = context.getSharedPreferences("widget_settings", Context.MODE_PRIVATE)

    fun getCircularStyle(): ClockFaceRenderer.WidgetStyle {
        val name = prefs.getString("circular_style", ClockFaceRenderer.WidgetStyle.STYLE_1.name)
        return try {
            ClockFaceRenderer.WidgetStyle.valueOf(name!!)
        } catch (_: Exception) {
            ClockFaceRenderer.WidgetStyle.STYLE_1
        }
    }

    fun setCircularStyle(style: ClockFaceRenderer.WidgetStyle) {
        prefs.edit { putString("circular_style", style.name) }
    }

    fun getHorizontalStyle(): ClockFaceRenderer.WidgetStyle {
        val name = prefs.getString("horizontal_style", ClockFaceRenderer.WidgetStyle.STYLE_5.name)
        return try {
            ClockFaceRenderer.WidgetStyle.valueOf(name!!)
        } catch (_: Exception) {
            ClockFaceRenderer.WidgetStyle.STYLE_5
        }
    }

    fun setHorizontalStyle(style: ClockFaceRenderer.WidgetStyle) {
        prefs.edit { putString("horizontal_style", style.name) }
    }

    enum class WidgetThemeMode {
        SYSTEM, LIGHT, DARK
    }

    fun getThemeMode(): WidgetThemeMode {
        val name = prefs.getString("theme_mode", WidgetThemeMode.SYSTEM.name)
        return try {
            WidgetThemeMode.valueOf(name!!)
        } catch (_: Exception) {
            WidgetThemeMode.SYSTEM
        }
    }

    fun setThemeMode(mode: WidgetThemeMode) {
        prefs.edit { putString("theme_mode", mode.name) }
    }
}
