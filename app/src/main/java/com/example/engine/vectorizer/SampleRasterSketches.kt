package com.example.engine.vectorizer

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import java.util.Random

/**
 * Procedural sample sketches modeled after historical Icelandic manuscripts and tattoo flash art.
 * Enables immediate out-of-the-box testing of the vectorizer.
 */
object SampleRasterSketches {

    data class SampleInfo(
        val id: String,
        val titleRu: String,
        val subtitleRu: String,
        val descriptionRu: String
    )

    val AVAILABLE_SAMPLES = listOf(
        SampleInfo(
            id = "vegvisir",
            titleRu = "Вегвизир",
            subtitleRu = "Исландский манускрипт",
            descriptionRu = "Рунический компас с 8 сложными лучами и старинной текстурой чернил"
        ),
        SampleInfo(
            id = "aegishjalmur",
            titleRu = "Шлем Ужаса (Агисхьяльм)",
            subtitleRu = "Сакральный оберег",
            descriptionRu = "Строгая геометрия с 8 трезубцами и защитными поперечными насечками"
        ),
        SampleInfo(
            id = "bindrune_strength",
            titleRu = "Вязаная Руна Силы",
            subtitleRu = "Тату-лайнворк",
            descriptionRu = "Комбинация Уруз, Тейваз и Альгиз с чистыми диагональными штрихами"
        ),
        SampleInfo(
            id = "odin_raven",
            titleRu = "Ворон Одина (Хугин)",
            subtitleRu = "Гравюрная штриховка",
            descriptionRu = "Крылатый силуэт со штрихованными перьями и руническим кольцом"
        )
    )

    fun createSampleBitmap(id: String, width: Int = 600, height: Int = 600): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val parchmentColor = Color.parseColor("#F6F2E8")
        bitmap.eraseColor(parchmentColor)
        val canvas = Canvas(bitmap)

        // Light parchment / aged paper background with organic subtle grain
        canvas.drawColor(parchmentColor)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1C1917") // Deep dark charcoal ink
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            strokeWidth = 6.0f
        }

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1C1917")
            style = Paint.Style.FILL
        }

        val scaleX = width / 600f
        val scaleY = height / 600f
        canvas.scale(scaleX, scaleY)

        val cx = 300f
        val cy = 300f

        when (id) {
            "vegvisir" -> drawVegvisirSample(canvas, cx, cy, paint, fillPaint)
            "aegishjalmur" -> drawAegishjalmurSample(canvas, cx, cy, paint, fillPaint)
            "bindrune_strength" -> drawBindruneSample(canvas, cx, cy, paint, fillPaint)
            "odin_raven" -> drawRavenSample(canvas, cx, cy, paint, fillPaint)
            else -> drawVegvisirSample(canvas, cx, cy, paint, fillPaint)
        }

        // Add subtle procedural noise & paper grain to simulate authentic raster scan
        addPaperGrainNoise(bitmap)

        return bitmap
    }

    private fun drawVegvisirSample(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        paint: Paint,
        fillPaint: Paint
    ) {
        val radius = cx * 0.72f

        // Central circle
        paint.strokeWidth = 5f
        canvas.drawCircle(cx, cy, 22f, paint)
        fillPaint.color = Color.parseColor("#1C1917")
        canvas.drawCircle(cx, cy, 7f, fillPaint)

        // 8 main rays
        for (i in 0 until 8) {
            val angle = (i * 45f) * PI.toFloat() / 180f
            val cosA = cos(angle)
            val sinA = sin(angle)

            val x1 = cx + cosA * 26f
            val y1 = cy + sinA * 26f
            val x2 = cx + cosA * radius
            val y2 = cy + sinA * radius

            paint.strokeWidth = 6f
            canvas.drawLine(x1, y1, x2, y2, paint)

            // Crossbars & finials
            val midX = cx + cosA * (radius * 0.55f)
            val midY = cy + sinA * (radius * 0.55f)
            val perpCos = -sinA
            val perpSin = cosA

            paint.strokeWidth = 4.5f
            // Crossbar
            canvas.drawLine(
                midX - perpCos * 24f, midY - perpSin * 24f,
                midX + perpCos * 24f, midY + perpSin * 24f,
                paint
            )

            // Finials
            when (i % 4) {
                0 -> {
                    // Forked prong
                    canvas.drawLine(x2, y2, x2 + cosA * 20f + perpCos * 20f, y2 + sinA * 20f + perpSin * 20f, paint)
                    canvas.drawLine(x2, y2, x2 + cosA * 20f - perpCos * 20f, y2 + sinA * 20f - perpSin * 20f, paint)
                }
                1 -> {
                    // Trident
                    canvas.drawLine(x2, y2, x2 + cosA * 28f, y2 + sinA * 28f, paint)
                    canvas.drawLine(x2, y2, x2 + cosA * 20f + perpCos * 18f, y2 + sinA * 20f + perpSin * 18f, paint)
                    canvas.drawLine(x2, y2, x2 + cosA * 20f - perpCos * 18f, y2 + sinA * 20f - perpSin * 18f, paint)
                }
                2 -> {
                    // Circle cap
                    canvas.drawCircle(x2 + cosA * 12f, y2 + sinA * 12f, 12f, paint)
                }
                3 -> {
                    // Arrowhead
                    canvas.drawLine(x2, y2, x2 - cosA * 18f + perpCos * 18f, y2 - sinA * 18f + perpSin * 18f, paint)
                    canvas.drawLine(x2, y2, x2 - cosA * 18f - perpCos * 18f, y2 - sinA * 18f - perpSin * 18f, paint)
                }
            }
        }
    }

    private fun drawAegishjalmurSample(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        paint: Paint,
        fillPaint: Paint
    ) {
        val radius = cx * 0.75f

        // Center jewel
        canvas.drawCircle(cx, cy, 14f, paint)
        canvas.drawCircle(cx, cy, 6f, fillPaint)

        for (i in 0 until 8) {
            val angle = (i * 45f) * PI.toFloat() / 180f
            val cosA = cos(angle)
            val sinA = sin(angle)
            val perpCos = -sinA
            val perpSin = cosA

            val xEnd = cx + cosA * radius
            val yEnd = cy + sinA * radius

            paint.strokeWidth = 6.5f
            canvas.drawLine(cx, cy, xEnd, yEnd, paint)

            // 3 tiers of protective crossbars
            for (step in 1..3) {
                val dist = radius * (0.28f + step * 0.18f)
                val bx = cx + cosA * dist
                val by = cy + sinA * dist
                val barLen = 14f + step * 5f

                paint.strokeWidth = 4.5f
                canvas.drawLine(
                    bx - perpCos * barLen, by - perpSin * barLen,
                    bx + perpCos * barLen, by + perpSin * barLen,
                    paint
                )
            }

            // Triple Trident Fork at pole
            paint.strokeWidth = 5f
            canvas.drawLine(xEnd, yEnd, xEnd + cosA * 30f, yEnd + sinA * 30f, paint)
            canvas.drawLine(xEnd, yEnd, xEnd + cosA * 22f + perpCos * 24f, yEnd + sinA * 22f + perpSin * 24f, paint)
            canvas.drawLine(xEnd, yEnd, xEnd + cosA * 22f - perpCos * 24f, yEnd + sinA * 22f - perpSin * 24f, paint)
        }
    }

    private fun drawBindruneSample(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        paint: Paint,
        fillPaint: Paint
    ) {
        paint.strokeWidth = 8.5f
        val topY = cy - 200f
        val botY = cy + 200f

        // Central main stem (Tiwas / Uruz)
        canvas.drawLine(cx, topY, cx, botY, paint)

        // Arrow finial at top (Tiwaz)
        canvas.drawLine(cx, topY, cx - 60f, topY + 60f, paint)
        canvas.drawLine(cx, topY, cx + 60f, topY + 60f, paint)

        // Left diagonal branches (Fehu / Algiz)
        canvas.drawLine(cx, cy - 80f, cx - 110f, cy - 170f, paint)
        canvas.drawLine(cx, cy, cx - 110f, cy - 90f, paint)

        // Right diagonal branches (Sowilo / Gebo)
        canvas.drawLine(cx, cy - 50f, cx + 110f, cy + 40f, paint)
        canvas.drawLine(cx + 110f, cy + 40f, cx, cy + 130f, paint)

        // Lower diamond (Ingwaz)
        val path = Path().apply {
            moveTo(cx, cy + 50f)
            lineTo(cx + 60f, cy + 110f)
            lineTo(cx, cy + 170f)
            lineTo(cx - 60f, cy + 110f)
            close()
        }
        paint.strokeWidth = 6.5f
        canvas.drawPath(path, paint)

        // Sacred dots
        fillPaint.color = Color.parseColor("#1C1917")
        canvas.drawCircle(cx, topY - 25f, 9f, fillPaint)
        canvas.drawCircle(cx, botY + 25f, 9f, fillPaint)
    }

    private fun drawRavenSample(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        paint: Paint,
        fillPaint: Paint
    ) {
        // Outer runic ring
        paint.strokeWidth = 4f
        canvas.drawCircle(cx, cy, cx * 0.82f, paint)
        canvas.drawCircle(cx, cy, cx * 0.74f, paint)

        // Silhouette of sacred raven in flight
        val ravenPath = Path().apply {
            moveTo(cx, cy - 140f) // Head & beak
            lineTo(cx + 25f, cy - 115f)
            lineTo(cx + 60f, cy - 90f)
            // Right wing
            cubicTo(cx + 120f, cy - 130f, cx + 190f, cy - 90f, cx + 200f, cy - 30f)
            cubicTo(cx + 160f, cy - 10f, cx + 120f, cy, cx + 50f, cy + 20f)
            // Tail
            lineTo(cx + 40f, cy + 120f)
            lineTo(cx, cy + 150f)
            lineTo(cx - 40f, cy + 120f)
            // Left wing
            lineTo(cx - 50f, cy + 20f)
            cubicTo(cx - 120f, cy, cx - 160f, cy - 10f, cx - 200f, cy - 30f)
            cubicTo(cx - 190f, cy - 90f, cx - 120f, cy - 130f, cx - 60f, cy - 90f)
            lineTo(cx - 25f, cy - 115f)
            close()
        }

        fillPaint.color = Color.parseColor("#1C1917")
        canvas.drawPath(ravenPath, fillPaint)

        // Fine interior linework in negative space
        val whiteLine = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F6F2E8")
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        canvas.drawLine(cx, cy - 100f, cx, cy + 110f, whiteLine)
        canvas.drawLine(cx, cy - 40f, cx + 70f, cy - 10f, whiteLine)
        canvas.drawLine(cx, cy - 40f, cx - 70f, cy - 10f, whiteLine)
    }

    private fun addPaperGrainNoise(bitmap: Bitmap) {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val rng = Random(42L)
        for (i in pixels.indices) {
            val p = pixels[i]
            val a = (p shr 24) and 0xFF
            var r = (p shr 16) and 0xFF
            var g = (p shr 8) and 0xFF
            var b = p and 0xFF

            // Add subtle salt-and-pepper noise to simulate scanned paper fiber
            val noise = (rng.nextGaussian() * 6.0).toInt()
            r = (r + noise).coerceIn(0, 255)
            g = (g + noise).coerceIn(0, 255)
            b = (b + noise).coerceIn(0, 255)

            pixels[i] = (a shl 24) or (r shl 16) or (g shl 8) or b
        }
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
    }
}
