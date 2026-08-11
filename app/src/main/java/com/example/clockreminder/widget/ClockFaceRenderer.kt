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
 * Рисует циферблат (круглый или горизонтальный) с напоминаниями.
 */
object ClockFaceRenderer {

    enum class WidgetStyle {
        STYLE_1, // 24ч круглый, подписи каждые 3 часа
        STYLE_2, // 24ч круглый, все подписи (промежуточные бледнее)
        STYLE_3, // 12ч круглый, подписи каждые 3 часа
        STYLE_4, // 12ч круглый, все подписи (промежуточные бледнее)
        STYLE_5  // Горизонтальный 24ч, подписи каждые 3 часа
    }

    fun render(widthPx: Int, heightPx: Int, reminders: List<Reminder>, faceStyle: WidgetStyle = WidgetStyle.STYLE_1): Bitmap {
        if (faceStyle == WidgetStyle.STYLE_5) {
            return renderHorizontal(widthPx, heightPx, reminders)
        }

        val sizePx = minOf(widthPx, heightPx)
        val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val cx = sizePx / 2f
        val cy = sizePx / 2f
        val radius = (sizePx / 2f) * 0.98f

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
            strokeWidth = sizePx * 0.02f
        }
        canvas.drawCircle(cx, cy, radius, ringPaint)

        val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#444446")
            strokeWidth = sizePx * 0.012f
            strokeCap = Paint.Cap.ROUND
        }
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = sizePx * 0.07f
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
                labelPaint.alpha = if (isMainTick) 220 else 80
                val labelText = if (is12h && h == 0) "12" else h.toString()
                val labelR = radius * 0.81f
                val lx = cx + labelR * cos(angle).toFloat()
                val ly = cy + labelR * sin(angle).toFloat() + labelPaint.textSize * 0.35f
                canvas.drawText(labelText, lx, ly, labelPaint)
            }
        }

        // --- кружки-напоминания сегодняшнего дня ---
        val now = LocalTime.now()
        val isNowPm = now.hour >= 12
        
        val occurrences = ReminderOccurrences.todaysOccurrences(reminders, LocalDate.now())
        val dotRadius = sizePx * 0.02f
        val dotOrbit = radius * 0.625f
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
                setShadowLayer(sizePx * 0.02f, 0f, 0f, occ.colorArgb)
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
            strokeWidth = sizePx * 0.025f
            strokeCap = Paint.Cap.ROUND
        }
        val hourLen = radius * 0.50f
        canvas.drawLine(
            cx, cy,
            cx + hourLen * cos(hourAngle).toFloat(),
            cy + hourLen * sin(hourAngle).toFloat(),
            hourHandPaint
        )

        val minuteHandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FF9500")
            strokeWidth = sizePx * 0.015f
            strokeCap = Paint.Cap.ROUND
        }
        val minuteLen = radius * 0.75f
        canvas.drawLine(
            cx, cy,
            cx + minuteLen * cos(minuteAngle).toFloat(),
            cy + minuteLen * sin(minuteAngle).toFloat(),
            minuteHandPaint
        )

        val centerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        canvas.drawCircle(cx, cy, sizePx * 0.025f, centerPaint)


        return bmp
    }

    private fun renderHorizontal(width: Int, height: Int, reminders: List<Reminder>): Bitmap {
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        
        val cornerRadius = height * 0.25f // Сбалансированное скругление
        val padding = width * 0.05f
        val scaleWidth = width - 2 * padding
        
        // Позиции по вертикали (динамические)
        val tickY = height * 0.82f
        val tickLen = height * 0.15f
        val labelY = height * 0.62f
        val dotY = height * 0.32f
        
        // Фон
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1C1C1E")
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), cornerRadius, cornerRadius, bgPaint)

        // Обводка
        val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#3A3A3C")
            style = Paint.Style.STROKE
            strokeWidth = height * 0.04f.coerceAtMost(6f) // Не слишком толстая на больших высотах
        }
        canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), cornerRadius, cornerRadius, ringPaint)

        val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#444446")
            strokeWidth = height * 0.02f.coerceAtMost(8f)
            strokeCap = Paint.Cap.ROUND
        }
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = height * 0.25f.coerceAtMost(width * 0.06f)
            textAlign = Paint.Align.CENTER
        }

        // Шкала 24 часа
        for (h in 0..24) {
            val x = padding + (h / 24f) * scaleWidth
            canvas.drawLine(x, tickY, x, tickY - tickLen, tickPaint)

            if (h % 3 == 0) {
                labelPaint.alpha = 220
                canvas.drawText(h.toString(), x, labelY, labelPaint)
            }
        }

        // Напоминания
        val occurrences = ReminderOccurrences.todaysOccurrences(reminders, LocalDate.now())
        val dotRadius = height * 0.08f.coerceAtMost(25f)
        for (occ in occurrences) {
            val hourFraction = occ.time.hour + occ.time.minute / 60.0
            val x = padding + (hourFraction.toFloat() / 24f) * scaleWidth
            val dotFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = occ.colorArgb
                style = Paint.Style.FILL
                setShadowLayer(dotRadius * 1.5f, 0f, 0f, occ.colorArgb)
            }
            canvas.drawCircle(x, dotY, dotRadius, dotFill)
        }

        // Индикатор
        val now = LocalTime.now()
        val nowFraction = now.hour + now.minute / 60.0 + now.second / 3600.0
        val indicatorX = padding + (nowFraction.toFloat() / 24f) * scaleWidth
        
        val indicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        val indWidth = height * 0.05f.coerceAtMost(10f)
        canvas.drawRoundRect(
            indicatorX - indWidth / 2,
            height * 0.18f,
            indicatorX + indWidth / 2,
            height * 0.82f,
            indWidth / 2,
            indWidth / 2,
            indicatorPaint
        )

        return bmp
    }

}
