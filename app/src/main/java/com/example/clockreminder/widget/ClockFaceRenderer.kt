package com.example.clockreminder.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import com.example.clockreminder.model.Reminder
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
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

    fun render(
        context: Context,
        widthPx: Int,
        heightPx: Int,
        reminders: List<Reminder>,
        faceStyle: WidgetStyle = WidgetStyle.STYLE_1,
        isDark: Boolean = true,
        hidePast: Boolean = true
    ): Bitmap {
        if (faceStyle == WidgetStyle.STYLE_5) {
            return renderHorizontal(context, widthPx, heightPx, reminders, isDark, hidePast)
        }

        val sizePx = minOf(widthPx, heightPx)
        val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val cx = sizePx / 2f
        val cy = sizePx / 2f
        val radius = (sizePx / 2f) * 0.98f

        val is12h = faceStyle == WidgetStyle.STYLE_3 || faceStyle == WidgetStyle.STYLE_4
        val cycle = if (is12h) 12.0 else 24.0

        // Цвета Material 3 / Dynamic Colors
        val bgColor = getDynamicColor(context, isDark, "surface", if (isDark) "#1C1B1F" else "#FFFBFE")
        val tickColor = getDynamicColor(context, isDark, "outlineVariant", if (isDark) "#938F99" else "#79747E")
        val labelColor = getDynamicColor(context, isDark, "onSurface", if (isDark) "#FFFFFF" else "#000000")
        val handColor = getDynamicColor(context, isDark, "onSurface", if (isDark) "#FFFFFF" else "#000000")
        val accentColor = getDynamicColor(context, isDark, "primary", if (isDark) "#D0BCFF" else "#6750A4")

        // фон циферблата
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = bgColor
            style = Paint.Style.FILL
        }
        canvas.drawCircle(cx, cy, radius, bgPaint)


        val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = tickColor
            strokeWidth = sizePx * 0.012f
            strokeCap = Paint.Cap.ROUND
        }
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = labelColor
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

        // --- кружки-напоминания (Умная фильтрация) ---
        val now = LocalDateTime.now()
        val zone = ZoneId.systemDefault()
        
        // Определяем временной диапазон для отображения
        val isPm = now.hour >= 12
        val (rangeStart, rangeEnd) = if (is12h) {
            if (!isPm) {
                // AM Phase: от начала дня до полудня (если не скрываем прошедшие)
                val start = if (hidePast) now else now.toLocalDate().atStartOfDay()
                start to now.with(LocalTime.NOON)
            } else {
                // PM Phase: от полудня до полуночи (завтра 00:00)
                val start = if (hidePast) now else now.with(LocalTime.NOON)
                start to now.toLocalDate().plusDays(1).atStartOfDay()
            }
        } else {
            // 24h: от начала суток до полуночи (завтра 00:00)
            val start = if (hidePast) now else now.toLocalDate().atStartOfDay()
            start to now.toLocalDate().plusDays(1).atStartOfDay()
        }
        
        val allOccurrences = ReminderOccurrences.getOccurrencesInRange(reminders, rangeStart, rangeEnd, zone)
            .filter { occ ->
                // "не отображаем на 24часовых циферблатах 00:00 и не отображаем на двенадцатичасовых циферблатах 12:00 и 00:00 если те уже прошли"
                val isPassed = occ.dateTime.isBefore(now.minusMinutes(1)) // 1 мин запас
                if (isPassed) {
                    val time = occ.time
                    val isBoundary = if (is12h) {
                        (time.hour == 0 && time.minute == 0) || (time.hour == 12 && time.minute == 0)
                    } else {
                        (time.hour == 0 && time.minute == 0)
                    }
                    if (isBoundary) return@filter false
                }
                true
            }
        
        val occurrences = allOccurrences.groupBy { it.time }

        val dotRadius = sizePx * 0.02f
        val baseDotOrbit = radius * 0.625f
        
        for ((_, timeOccs) in occurrences) {
            val firstOcc = timeOccs.first()
            val hourValue = if (is12h) (firstOcc.time.hour % 12).toDouble() else firstOcc.time.hour.toDouble()
            val hourFraction = hourValue + firstOcc.time.minute / 60.0
            val angle = Math.toRadians((hourFraction / cycle) * 360.0 - 90.0)
            
            timeOccs.forEachIndexed { index, occ ->
                val currentOrbit = baseDotOrbit - (index * dotRadius * 2.2f)
                val dx = cx + currentOrbit * cos(angle).toFloat()
                val dy = cy + currentOrbit * sin(angle).toFloat()
                
                val dotFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = occ.colorArgb
                    style = Paint.Style.FILL
                    setShadowLayer(sizePx * 0.02f, 0f, 0f, occ.colorArgb)
                }
                canvas.drawCircle(dx, dy, dotRadius, dotFill)
            }
        }

        // --- стрелки ---
        val hourVal = if (is12h) (now.hour % 12).toDouble() else now.hour.toDouble()
        val hourFraction = hourVal + now.minute / 60.0
        val hourAngle = Math.toRadians((hourFraction / cycle) * 360.0 - 90.0)
        val minuteAngle = Math.toRadians((now.minute / 60.0) * 360.0 - 90.0)

        val hourHandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = handColor
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
            color = accentColor
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

        val centerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = handColor }
        canvas.drawCircle(cx, cy, sizePx * 0.025f, centerPaint)


        return bmp
    }

    private fun renderHorizontal(
        context: Context,
        width: Int,
        height: Int,
        reminders: List<Reminder>,
        isDark: Boolean,
        hidePast: Boolean
    ): Bitmap {
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        
        // Цвета Material 3 / Dynamic Colors
        val bgColor = getDynamicColor(context, isDark, "surface", if (isDark) "#1C1B1F" else "#FFFBFE")
        val tickColor = getDynamicColor(context, isDark, "outlineVariant", if (isDark) "#938F99" else "#79747E")
        val labelColor = getDynamicColor(context, isDark, "onSurface", if (isDark) "#FFFFFF" else "#000000")
        val accentColor = getDynamicColor(context, isDark, "primary", if (isDark) "#D0BCFF" else "#6750A4")

        val cornerRadius = height * 0.25f 
        val padding = width * 0.05f
        val scaleWidth = width - 2 * padding
        
        // Позиции по вертикали (динамические)
        val tickY = height * 0.82f
        val tickLen = height * 0.15f
        val labelY = height * 0.62f
        val dotY = height * 0.32f
        
        // Фон
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = bgColor
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), cornerRadius, cornerRadius, bgPaint)


        val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = tickColor
            strokeWidth = height * 0.02f.coerceAtMost(8f)
            strokeCap = Paint.Cap.ROUND
        }
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = labelColor
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
        val nowDateTime = LocalDateTime.now()
        val startRange = if (hidePast) nowDateTime else nowDateTime.toLocalDate().atStartOfDay()
        val endOfDay = nowDateTime.toLocalDate().plusDays(1).atStartOfDay()
        
        val allOccurrences = ReminderOccurrences.getOccurrencesInRange(reminders, startRange, endOfDay)
            .filter { occ ->
                // Для 24ч (горизонтальный) не показываем прошедшую полночь 00:00
                val isPassed = occ.dateTime.isBefore(nowDateTime.minusMinutes(1))
                if (isPassed && occ.time.hour == 0 && occ.time.minute == 0) return@filter false
                true
            }
        
        val occurrences = allOccurrences.groupBy { it.dateTime }
            
        val dotRadius = height * 0.08f.coerceAtMost(25f)
        for ((occDateTime, timeOccs) in occurrences) {
            // Рассчитываем позицию на шкале 0..24. 
            // Если это уже следующий день (полночь), ставим на 24.0
            val hourFraction = if (occDateTime.toLocalDate().isAfter(nowDateTime.toLocalDate())) {
                24.0
            } else {
                occDateTime.hour + occDateTime.minute / 60.0
            }
            
            val x = padding + (hourFraction.toFloat() / 24f) * scaleWidth
            
            timeOccs.forEachIndexed { index, occ ->
                val currentDotY = dotY + (index * dotRadius * 2.2f)
                
                val dotFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = occ.colorArgb
                    style = Paint.Style.FILL
                    setShadowLayer(dotRadius * 1.5f, 0f, 0f, occ.colorArgb)
                }
                canvas.drawCircle(x, currentDotY, dotRadius, dotFill)
            }
        }

        // Индикатор
        val now = LocalTime.now()
        val nowFraction = now.hour + now.minute / 60.0 + now.second / 3600.0
        val indicatorX = padding + (nowFraction.toFloat() / 24f) * scaleWidth
        
        val indicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
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

    private fun getDynamicColor(context: Context, isDark: Boolean, name: String, fallback: String): Int {
        if (Build.VERSION.SDK_INT >= 31) {
            val colorResName = when(name) {
                "primary" -> if (isDark) "system_accent1_200" else "system_accent1_600"
                "surface" -> if (isDark) "system_neutral1_900" else "system_neutral1_10"
                "onSurface" -> if (isDark) "system_neutral1_10" else "system_neutral1_900"
                "outline" -> if (isDark) "system_neutral2_400" else "system_neutral2_600"
                "outlineVariant" -> if (isDark) "system_neutral2_700" else "system_neutral2_300"
                else -> null
            }
            if (colorResName != null) {
                val resId = context.resources.getIdentifier(colorResName, "color", "android")
                if (resId != 0) return context.getColor(resId)
            }
        }
        return Color.parseColor(fallback)
    }
}
