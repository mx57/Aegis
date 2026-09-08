package com.example.engine

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.ui.geometry.Offset
import androidx.core.content.FileProvider
import com.example.ui.components.BodyZone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt

object TryOnRenderer {

    suspend fun renderTryOnToBitmap(
        context: Context,
        userPhotoUri: Uri?,
        selectedZone: BodyZone,
        stave: ComposedStave,
        config: SketchConfig,
        offset: Offset,
        scale: Float,
        rotation: Float,
        opacity: Float,
        isBlackInk: Boolean,
        targetWidth: Int = 1080,
        targetHeight: Int = 1317
    ): Bitmap = withContext(Dispatchers.IO) {
        val bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Fill default background
        val bgPaint = Paint().apply {
            color = Color.parseColor("#121318")
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, targetWidth.toFloat(), targetHeight.toFloat(), bgPaint)

        // 2. Draw user photo or body zone silhouette
        if (userPhotoUri != null) {
            var loaded = false
            try {
                context.contentResolver.openInputStream(userPhotoUri)?.use { input ->
                    val photoBitmap = BitmapFactory.decodeStream(input)
                    if (photoBitmap != null) {
                        val srcW = photoBitmap.width.toFloat()
                        val srcH = photoBitmap.height.toFloat()
                        val scaleFactor = maxOf(targetWidth / srcW, targetHeight / srcH)
                        val scaledW = srcW * scaleFactor
                        val scaledH = srcH * scaleFactor
                        val left = (targetWidth - scaledW) / 2f
                        val top = (targetHeight - scaledH) / 2f

                        canvas.drawBitmap(
                            photoBitmap,
                            null,
                            RectF(left, top, left + scaledW, top + scaledH),
                            Paint(Paint.FILTER_BITMAP_FLAG)
                        )
                        loaded = true
                    }
                }
            } catch (_: Exception) {
            }
            if (!loaded) {
                drawBodyZoneSilhouette(canvas, selectedZone, targetWidth.toFloat(), targetHeight.toFloat())
            }
        } else {
            drawBodyZoneSilhouette(canvas, selectedZone, targetWidth.toFloat(), targetHeight.toFloat())
        }

        // 3. Render Stave Overlay with transparent background
        val staveBasePx = (targetWidth * 0.55f).roundToInt()
        val inkColorInt = if (isBlackInk) Color.parseColor("#1A1A1A") else Color.parseColor("#FFD700")

        val staveBitmap = SvgStaveRenderer.renderToBitmap(
            stave = stave,
            config = config,
            targetSize = staveBasePx,
            transparentBg = true,
            overrideColorInt = inkColorInt
        )

        // Map gesture translation to output bitmap resolution
        val centerX = targetWidth / 2f
        val centerY = targetHeight / 2f

        // Nominal UI scale factor: UI preview width ~ 360dp vs output targetWidth
        val uiScaleRatio = targetWidth / 360f
        val mappedOffsetX = offset.x * uiScaleRatio
        val mappedOffsetY = offset.y * uiScaleRatio

        canvas.save()
        canvas.translate(centerX + mappedOffsetX, centerY + mappedOffsetY)
        canvas.rotate(rotation)
        canvas.scale(scale, scale)

        val stavePaint = Paint(Paint.FILTER_BITMAP_FLAG).apply {
            alpha = (opacity.coerceIn(0.2f, 1.0f) * 255).roundToInt()
        }
        canvas.drawBitmap(
            staveBitmap,
            -staveBasePx / 2f,
            -staveBasePx / 2f,
            stavePaint
        )
        canvas.restore()

        // 4. Draw watermark / branding overlay
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(160, 220, 220, 220)
            textSize = targetWidth * 0.026f
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("Рунический Став • Примерка", 32f, targetHeight - 36f, textPaint)

        bitmap
    }

    suspend fun saveTryOnToGallery(context: Context, bitmap: Bitmap): Boolean = withContext(Dispatchers.IO) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, "runic_tryon_${System.currentTimeMillis()}.png")
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/RunicStave")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
                val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    context.contentResolver.update(uri, contentValues, null, null)
                    return@withContext true
                }
            } else {
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val appDir = File(picturesDir, "RunicStave").apply { if (!exists()) mkdirs() }
                val file = File(appDir, "runic_tryon_${System.currentTimeMillis()}.png")
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), arrayOf("image/png"), null)
                return@withContext true
            }
        } catch (_: Exception) {
        }
        false
    }

    suspend fun shareTryOnPhoto(context: Context, bitmap: Bitmap) = withContext(Dispatchers.Main) {
        val file = SvgStaveRenderer.savePngForSharing(context, bitmap, "runic_tryon_share.png")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Поделиться фото примерки"))
    }

    private fun drawBodyZoneSilhouette(
        canvas: Canvas,
        zone: BodyZone,
        w: Float,
        h: Float
    ) {
        val outlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#9EABBC")
            alpha = 110
            style = Paint.Style.STROKE
            strokeWidth = w * 0.006f
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        val musclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#9EABBC")
            alpha = 55
            style = Paint.Style.STROKE
            strokeWidth = w * 0.004f
            strokeCap = Paint.Cap.ROUND
        }

        val path = Path()

        when (zone) {
            BodyZone.FOREARM -> {
                path.moveTo(w * 0.30f, h * 0.08f)
                path.cubicTo(w * 0.22f, h * 0.35f, w * 0.34f, h * 0.70f, w * 0.38f, h * 0.92f)
                path.lineTo(w * 0.62f, h * 0.92f)
                path.cubicTo(w * 0.66f, h * 0.70f, w * 0.78f, h * 0.35f, w * 0.70f, h * 0.08f)
                path.close()
                canvas.drawPath(path, outlinePaint)
                canvas.drawLine(w * 0.45f, h * 0.35f, w * 0.47f, h * 0.65f, musclePaint)
                canvas.drawLine(w * 0.55f, h * 0.32f, w * 0.53f, h * 0.60f, musclePaint)
            }
            BodyZone.SHOULDER -> {
                path.moveTo(w * 0.50f, h * 0.05f)
                path.cubicTo(w * 0.15f, h * 0.15f, w * 0.18f, h * 0.65f, w * 0.32f, h * 0.95f)
                path.lineTo(w * 0.68f, h * 0.95f)
                path.cubicTo(w * 0.82f, h * 0.65f, w * 0.85f, h * 0.15f, w * 0.50f, h * 0.05f)
                canvas.drawPath(path, outlinePaint)
                val deltoid = Path().apply {
                    moveTo(w * 0.50f, h * 0.15f)
                    cubicTo(w * 0.38f, h * 0.35f, w * 0.42f, h * 0.65f, w * 0.50f, h * 0.80f)
                }
                canvas.drawPath(deltoid, musclePaint)
            }
            BodyZone.WRIST -> {
                path.moveTo(w * 0.32f, h * 0.10f)
                path.lineTo(w * 0.32f, h * 0.60f)
                path.cubicTo(w * 0.25f, h * 0.75f, w * 0.30f, h * 0.95f, w * 0.40f, h * 0.95f)
                path.lineTo(w * 0.60f, h * 0.95f)
                path.cubicTo(w * 0.70f, h * 0.95f, w * 0.75f, h * 0.75f, w * 0.68f, h * 0.60f)
                path.lineTo(w * 0.68f, h * 0.10f)
                canvas.drawPath(path, outlinePaint)
                canvas.drawLine(w * 0.36f, h * 0.45f, w * 0.64f, h * 0.45f, musclePaint)
                canvas.drawLine(w * 0.38f, h * 0.50f, w * 0.62f, h * 0.50f, musclePaint)
            }
            BodyZone.NECK -> {
                path.moveTo(w * 0.10f, h * 0.85f)
                path.cubicTo(w * 0.28f, h * 0.70f, w * 0.32f, h * 0.40f, w * 0.32f, h * 0.10f)
                path.lineTo(w * 0.68f, h * 0.10f)
                path.cubicTo(w * 0.68f, h * 0.40f, w * 0.72f, h * 0.70f, w * 0.90f, h * 0.85f)
                canvas.drawPath(path, outlinePaint)
                canvas.drawLine(w * 0.20f, h * 0.82f, w * 0.46f, h * 0.78f, musclePaint)
                canvas.drawLine(w * 0.80f, h * 0.82f, w * 0.54f, h * 0.78f, musclePaint)
            }
            BodyZone.CHEST -> {
                path.moveTo(w * 0.12f, h * 0.15f)
                path.lineTo(w * 0.88f, h * 0.15f)
                path.lineTo(w * 0.80f, h * 0.85f)
                path.cubicTo(w * 0.65f, h * 0.88f, w * 0.55f, h * 0.70f, w * 0.50f, h * 0.65f)
                path.cubicTo(w * 0.45f, h * 0.70f, w * 0.35f, h * 0.88f, w * 0.20f, h * 0.85f)
                path.close()
                canvas.drawPath(path, outlinePaint)
                canvas.drawLine(w * 0.50f, h * 0.18f, w * 0.50f, h * 0.75f, musclePaint)
            }
            BodyZone.BACK -> {
                path.moveTo(w * 0.20f, h * 0.08f)
                path.lineTo(w * 0.80f, h * 0.08f)
                path.cubicTo(w * 0.85f, h * 0.45f, w * 0.75f, h * 0.75f, w * 0.70f, h * 0.95f)
                path.lineTo(w * 0.30f, h * 0.95f)
                path.cubicTo(w * 0.25f, h * 0.75f, w * 0.15f, h * 0.45f, w * 0.20f, h * 0.08f)
                path.close()
                canvas.drawPath(path, outlinePaint)
                canvas.drawLine(w * 0.50f, h * 0.10f, w * 0.50f, h * 0.92f, musclePaint)
            }
            BodyZone.CALF -> {
                path.moveTo(w * 0.36f, h * 0.05f)
                path.cubicTo(w * 0.18f, h * 0.32f, w * 0.25f, h * 0.65f, w * 0.40f, h * 0.95f)
                path.lineTo(w * 0.60f, h * 0.95f)
                path.cubicTo(w * 0.75f, h * 0.65f, w * 0.82f, h * 0.32f, w * 0.64f, h * 0.05f)
                path.close()
                canvas.drawPath(path, outlinePaint)
                canvas.drawLine(w * 0.50f, h * 0.18f, w * 0.50f, h * 0.55f, musclePaint)
            }
            BodyZone.ANKLE -> {
                path.moveTo(w * 0.38f, h * 0.05f)
                path.lineTo(w * 0.35f, h * 0.65f)
                path.cubicTo(w * 0.28f, h * 0.75f, w * 0.28f, h * 0.85f, w * 0.36f, h * 0.95f)
                path.lineTo(w * 0.64f, h * 0.95f)
                path.cubicTo(w * 0.72f, h * 0.85f, w * 0.72f, h * 0.75f, w * 0.65f, h * 0.65f)
                path.lineTo(w * 0.62f, h * 0.05f)
                canvas.drawPath(path, outlinePaint)
                canvas.drawCircle(w * 0.36f, h * 0.78f, 6f, musclePaint)
                canvas.drawCircle(w * 0.64f, h * 0.80f, 6f, musclePaint)
            }
        }
    }
}
