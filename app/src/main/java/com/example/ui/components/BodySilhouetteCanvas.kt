package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke

enum class BodyZone(
    val id: String,
    val titleRu: String,
    val zoneWidthCm: Float,
    val zoneHeightCm: Float
) {
    FOREARM("forearm", "Предплечье", 9.0f, 26.0f),
    SHOULDER("shoulder", "Плечо", 14.0f, 22.0f),
    WRIST("wrist", "Запястье", 6.5f, 15.0f),
    NECK("neck", "Шея", 12.0f, 16.0f),
    CHEST("chest", "Грудь", 28.0f, 24.0f),
    BACK("back", "Спина", 36.0f, 42.0f),
    CALF("calf", "Голень", 13.0f, 32.0f),
    ANKLE("ankle", "Щиколотка", 8.0f, 18.0f)
}

@Composable
fun BodySilhouetteCanvas(
    zone: BodyZone,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        val outlineColor = Color(0x669EABBC)
        val muscleLineColor = Color(0x339EABBC)
        val strokeStyle = Stroke(width = 2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val subtleStyle = Stroke(width = 1.5f, cap = StrokeCap.Round)

        when (zone) {
            BodyZone.FOREARM -> {
                // Forearm contour tapering towards wrist
                val path = Path().apply {
                    moveTo(w * 0.30f, h * 0.08f)
                    cubicTo(w * 0.22f, h * 0.35f, w * 0.34f, h * 0.70f, w * 0.38f, h * 0.92f)
                    lineTo(w * 0.62f, h * 0.92f)
                    cubicTo(w * 0.66f, h * 0.70f, w * 0.78f, h * 0.35f, w * 0.70f, h * 0.08f)
                    close()
                }
                drawPath(path, outlineColor, style = strokeStyle)
                // Muscle flex lines
                drawLine(muscleLineColor, Offset(w * 0.45f, h * 0.35f), Offset(w * 0.47f, h * 0.65f), strokeWidth = 1.8f)
                drawLine(muscleLineColor, Offset(w * 0.55f, h * 0.32f), Offset(w * 0.53f, h * 0.60f), strokeWidth = 1.8f)
            }
            BodyZone.SHOULDER -> {
                // Deltoid muscular contour
                val path = Path().apply {
                    moveTo(w * 0.50f, h * 0.05f)
                    cubicTo(w * 0.15f, h * 0.15f, w * 0.18f, h * 0.65f, w * 0.32f, h * 0.95f)
                    lineTo(w * 0.68f, h * 0.95f)
                    cubicTo(w * 0.82f, h * 0.65f, w * 0.85f, h * 0.15f, w * 0.50f, h * 0.05f)
                }
                drawPath(path, outlineColor, style = strokeStyle)
                // Deltoid fiber curves
                val deltoid = Path().apply {
                    moveTo(w * 0.50f, h * 0.15f)
                    cubicTo(w * 0.38f, h * 0.35f, w * 0.42f, h * 0.65f, w * 0.50f, h * 0.80f)
                }
                drawPath(deltoid, muscleLineColor, style = subtleStyle)
            }
            BodyZone.WRIST -> {
                // Wrist and palm baseline
                val path = Path().apply {
                    moveTo(w * 0.32f, h * 0.10f)
                    lineTo(w * 0.32f, h * 0.60f)
                    cubicTo(w * 0.25f, h * 0.75f, w * 0.30f, h * 0.95f, w * 0.40f, h * 0.95f)
                    lineTo(w * 0.60f, h * 0.95f)
                    cubicTo(w * 0.70f, h * 0.95f, w * 0.75f, h * 0.75f, w * 0.68f, h * 0.60f)
                    lineTo(w * 0.68f, h * 0.10f)
                }
                drawPath(path, outlineColor, style = strokeStyle)
                // Wrist crease lines
                drawLine(muscleLineColor, Offset(w * 0.36f, h * 0.45f), Offset(w * 0.64f, h * 0.45f), strokeWidth = 2f)
                drawLine(muscleLineColor, Offset(w * 0.38f, h * 0.50f), Offset(w * 0.62f, h * 0.50f), strokeWidth = 1.5f)
            }
            BodyZone.NECK -> {
                // Neck, trapezius slope and collarbone
                val path = Path().apply {
                    moveTo(w * 0.10f, h * 0.85f)
                    cubicTo(w * 0.28f, h * 0.70f, w * 0.32f, h * 0.40f, w * 0.32f, h * 0.10f)
                    lineTo(w * 0.68f, h * 0.10f)
                    cubicTo(w * 0.68f, h * 0.40f, w * 0.72f, h * 0.70f, w * 0.90f, h * 0.85f)
                }
                drawPath(path, outlineColor, style = strokeStyle)
                // Collarbones
                drawLine(muscleLineColor, Offset(w * 0.20f, h * 0.82f), Offset(w * 0.46f, h * 0.78f), strokeWidth = 2.2f)
                drawLine(muscleLineColor, Offset(w * 0.80f, h * 0.82f), Offset(w * 0.54f, h * 0.78f), strokeWidth = 2.2f)
            }
            BodyZone.CHEST -> {
                // Pectoral contours and sternum
                val path = Path().apply {
                    moveTo(w * 0.12f, h * 0.15f)
                    lineTo(w * 0.88f, h * 0.15f)
                    lineTo(w * 0.80f, h * 0.85f)
                    cubicTo(w * 0.65f, h * 0.88f, w * 0.55f, h * 0.70f, w * 0.50f, h * 0.65f)
                    cubicTo(w * 0.45f, h * 0.70f, w * 0.35f, h * 0.88f, w * 0.20f, h * 0.85f)
                    close()
                }
                drawPath(path, outlineColor, style = strokeStyle)
                // Sternum center line
                drawLine(muscleLineColor, Offset(w * 0.50f, h * 0.18f), Offset(w * 0.50f, h * 0.75f), strokeWidth = 2f)
            }
            BodyZone.BACK -> {
                // Back torso, spine and shoulder blades
                val path = Path().apply {
                    moveTo(w * 0.20f, h * 0.08f)
                    lineTo(w * 0.80f, h * 0.08f)
                    cubicTo(w * 0.85f, h * 0.45f, w * 0.75f, h * 0.75f, w * 0.70f, h * 0.95f)
                    lineTo(w * 0.30f, h * 0.95f)
                    cubicTo(w * 0.25f, h * 0.75f, w * 0.15f, h * 0.45f, w * 0.20f, h * 0.08f)
                    close()
                }
                drawPath(path, outlineColor, style = strokeStyle)
                // Spine line
                drawLine(muscleLineColor, Offset(w * 0.50f, h * 0.10f), Offset(w * 0.50f, h * 0.92f), strokeWidth = 2.0f)
                // Scapula curves
                val leftScapula = Path().apply {
                    moveTo(w * 0.32f, h * 0.25f)
                    cubicTo(w * 0.40f, h * 0.30f, w * 0.38f, h * 0.45f, w * 0.30f, h * 0.48f)
                }
                val rightScapula = Path().apply {
                    moveTo(w * 0.68f, h * 0.25f)
                    cubicTo(w * 0.60f, h * 0.30f, w * 0.62f, h * 0.45f, w * 0.70f, h * 0.48f)
                }
                drawPath(leftScapula, muscleLineColor, style = subtleStyle)
                drawPath(rightScapula, muscleLineColor, style = subtleStyle)
            }
            BodyZone.CALF -> {
                // Gastrocnemius (calf muscle) and Achilles tendon
                val path = Path().apply {
                    moveTo(w * 0.36f, h * 0.05f)
                    cubicTo(w * 0.18f, h * 0.32f, w * 0.25f, h * 0.65f, w * 0.40f, h * 0.95f)
                    lineTo(w * 0.60f, h * 0.95f)
                    cubicTo(w * 0.75f, h * 0.65f, w * 0.82f, h * 0.32f, w * 0.64f, h * 0.05f)
                    close()
                }
                drawPath(path, outlineColor, style = strokeStyle)
                // Calf bifurcation line
                drawLine(muscleLineColor, Offset(w * 0.50f, h * 0.18f), Offset(w * 0.50f, h * 0.55f), strokeWidth = 1.8f)
            }
            BodyZone.ANKLE -> {
                // Lower leg and ankle malleolus bones
                val path = Path().apply {
                    moveTo(w * 0.38f, h * 0.05f)
                    lineTo(w * 0.35f, h * 0.65f)
                    cubicTo(w * 0.28f, h * 0.75f, w * 0.28f, h * 0.85f, w * 0.36f, h * 0.95f)
                    lineTo(w * 0.64f, h * 0.95f)
                    cubicTo(w * 0.72f, h * 0.85f, w * 0.72f, h * 0.75f, w * 0.65f, h * 0.65f)
                    lineTo(w * 0.62f, h * 0.05f)
                }
                drawPath(path, outlineColor, style = strokeStyle)
                // Ankle bone landmarks
                drawCircle(muscleLineColor, radius = 6f, center = Offset(w * 0.36f, h * 0.78f), style = subtleStyle)
                drawCircle(muscleLineColor, radius = 6f, center = Offset(w * 0.64f, h * 0.80f), style = subtleStyle)
            }
        }
    }
}
