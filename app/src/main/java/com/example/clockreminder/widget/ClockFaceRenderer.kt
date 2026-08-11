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
 * Рисует круглый 24-часовой циферблат с часовой и минутной стрелками
 * и цветными кружками-напоминаниями вокруг края в виде Bitmap,
 * который затем вставляется в RemoteViews (ImageView) виджета.
 */
object ClockFaceRenderer {

    fun render(sizePx: Int, reminders: List<Reminder>): Bitmap {
        val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val cx = sizePx / 2f
        val cy = sizePx / 2f
        val radius = sizePx / 2f * 0.95f // Используем больше места, так как точки теперь внутри

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

        // 24 деления, подписи каждые 3 часа
        for (h in 0 until 24) {
            val angle = Math.toRadians((h / 24.0) * 360.0 - 90.0)
            val outerR = radius * 0.98f
            val innerR = if (h % 3 == 0) radius * 0.85f else radius * 0.92f
            val x1 = cx + outerR * cos(angle).toFloat()
            val y1 = cy + outerR * sin(angle).toFloat()
            val x2 = cx + innerR * cos(angle).toFloat()
            val y2 = cy + innerR * sin(angle).toFloat()
            canvas.drawLine(x1, y1, x2, y2, tickPaint)

            if (h % 3 == 0) {
                val labelR = radius * 0.74f
                val lx = cx + labelR * cos(angle).toFloat()
                val ly = cy + labelR * sin(angle).toFloat() + labelPaint.textSize * 0.35f
                canvas.drawText(h.toString(), lx, ly, labelPaint)
            }
        }

        // --- кружки-напоминания сегодняшнего дня ---
        val occurrences = ReminderOccurrences.todaysOccurrences(reminders, LocalDate.now())
        val dotRadius = sizePx * 0.02f
        val dotOrbit = radius * 0.8f
        for (occ in occurrences) {
            val hourFraction = occ.time.hour + occ.time.minute / 60.0
            val angle = Math.toRadians((hourFraction / 24.0) * 360.0 - 90.0)
            val dx = cx + dotOrbit * cos(angle).toFloat()
            val dy = cy + dotOrbit * sin(angle).toFloat()
            val dotFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = occ.colorArgb
                style = Paint.Style.FILL
            }
            canvas.drawCircle(dx, dy, dotRadius, dotFill)
        }

        // --- стрелки ---
        val now = LocalTime.now()
        val hourFraction = now.hour + now.minute / 60.0
        val hourAngle = Math.toRadians((hourFraction / 24.0) * 360.0 - 90.0) // полный круг за 24ч
        val minuteAngle = Math.toRadians((now.minute / 60.0) * 360.0 - 90.0) // полный круг за 60мин

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
