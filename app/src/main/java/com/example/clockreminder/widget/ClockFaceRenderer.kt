package com.example.clockreminder.widget

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.example.clockreminder.model.Reminder
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.cos
import kotlin.math.sin

/**
 * Рисует круглый циферблат (24ч или 12ч) с часовой и минутной стрелками
 * и цветными кружками-напоминаниями.
 */
object ClockFaceRenderer {

    enum class WidgetStyle {
        STYLE_1, // 24ч, подписи каждые 3 часа
        STYLE_2, // 24ч, все подписи (промежуточные бледнее)
        STYLE_3, // 12ч, подписи каждые 3 часа
        STYLE_4  // 12ч, все подписи (промежуточные бледнее)
    }

    fun render(sizePx: Int, reminders: List<Reminder>, faceStyle: WidgetStyle = WidgetStyle.STYLE_1): Bitmap {
        val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val cx = sizePx / 2f
        val cy = sizePx / 2f
        val radius = sizePx / 2f * 0.95f

        val is12h = faceStyle == WidgetStyle.STYLE_3 || faceStyle == WidgetStyle.STYLE_4
        val cycle = if (is12h) 12.0 else 24.0

        // фон циферблата
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1C1C1E")
            style = Paint.Style.FILL
        }
        canvas.drawCircle(cx, cy, radius, bgPaint)

        val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#3A3A3C")
            style = Paint.Style.STROKE
            strokeWidth = sizePx * 0.006f
        }
        canvas.drawCircle(cx, cy, radius, ringPaint)

        val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#8E8E93")
            strokeWidth = sizePx * 0.004f
        }
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = sizePx * 0.045f
            textAlign = Paint.Align.CENTER
        }

        // деления и подписи
        val totalTicks = cycle.toInt()
        for (h in 0 until totalTicks) {
            val angle = Math.toRadians((h / cycle) * 360.0 - 90.0)
            val outerR = radius * 0.98f
            val innerR = radius * 0.92f
            val x1 = cx + outerR * cos(angle).toFloat()
            val y1 = cy + outerR * sin(angle).toFloat()
            val x2 = cx + innerR * cos(angle).toFloat()
            val y2 = cy + innerR * sin(angle).toFloat()
            canvas.drawLine(x1, y1, x2, y2, tickPaint)

            val isMainTick = h % 3 == 0
            val shouldDrawLabel = isMainTick || (faceStyle == WidgetStyle.STYLE_2) || (faceStyle == WidgetStyle.STYLE_4)
            
            if (shouldDrawLabel) {
                labelPaint.alpha = if (isMainTick) 180 else 60
                val labelText = if (is12h && h == 0) "12" else h.toString()
                val labelR = radius * 0.83f
                val lx = cx + labelR * cos(angle).toFloat()
                val ly = cy + labelR * sin(angle).toFloat() + labelPaint.textSize * 0.3f
                canvas.drawText(labelText, lx, ly, labelPaint)
            }
        }

        // --- кружки-напоминания сегодняшнего дня ---
        val now = LocalTime.now()
        val isNowPm = now.hour >= 12
        
        val occurrences = ReminderOccurrences.todaysOccurrences(reminders, LocalDate.now())
        val dotRadius = sizePx * 0.02f
        val dotOrbit = radius * 0.68f
        for (occ in occurrences) {
            if (is12h) {
                val isOccPm = occ.time.hour >= 12
                if (isOccPm != isNowPm) continue
            }
            
            val hourValue = if (is12h) (occ.time.hour % 12).toDouble() else occ.time.hour.toDouble()
            val hourFraction = hourValue + occ.time.minute / 60.0
            val angle = Math.toRadians((hourFraction / cycle) * 360.0 - 90.0)
            val dx = cx + dotOrbit * cos(angle).toFloat()
            val dy = cy + dotOrbit * sin(angle).toFloat()
            val dotFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = occ.colorArgb
                style = Paint.Style.FILL
            }
            canvas.drawCircle(dx, dy, dotRadius, dotFill)
        }

        // --- стрелки ---
        val hourVal = if (is12h) (now.hour % 12).toDouble() else now.hour.toDouble()
        val hourFraction = hourVal + now.minute / 60.0
        val hourAngle = Math.toRadians((hourFraction / cycle) * 360.0 - 90.0)
        val minuteAngle = Math.toRadians((now.minute / 60.0) * 360.0 - 90.0)

        val hourHandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            strokeWidth = sizePx * 0.018f
            strokeCap = Paint.Cap.ROUND
        }
        val hourLen = radius * 0.55f
        canvas.drawLine(
            cx, cy,
            cx + hourLen * cos(hourAngle).toFloat(),
            cy + hourLen * sin(hourAngle).toFloat(),
            hourHandPaint
        )

        val minuteHandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FF9500")
            strokeWidth = sizePx * 0.012f
            strokeCap = Paint.Cap.ROUND
        }
        val minuteLen = radius * 0.78f
        canvas.drawLine(
            cx, cy,
            cx + minuteLen * cos(minuteAngle).toFloat(),
            cy + minuteLen * sin(minuteAngle).toFloat(),
            minuteHandPaint
        )

        val centerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        canvas.drawCircle(cx, cy, sizePx * 0.02f, centerPaint)

        return bmp
    }
}
