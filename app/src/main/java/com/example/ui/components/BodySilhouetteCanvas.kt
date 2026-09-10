package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
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

/**
 * High-fidelity 3D Volumetric Anatomical Canvas for Body Silhouettes.
 * Renders multi-layered slate/metallic contours with drop shadows, specular highlights,
 * anatomical muscle & bone definition, and studio alignment grid ticks.
 */
@Composable
fun BodySilhouetteCanvas(
    zone: BodyZone,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // 1. Studio Background Vignette & Scale Grid
        drawRoundRect(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF1E222D), Color(0xFF12141C), Color(0xFF090A0E)),
                center = Offset(w * 0.5f, h * 0.45f),
                radius = w * 0.85f
            ),
            size = size
        )

        // Subtle Studio Alignment Measurement Grid Ticks (Golden Slate)
        val gridColor = Color(0x22E5C158)
        val lineGridColor = Color(0x129EABBC)
        val ticks = 10
        for (i in 1 until ticks) {
            val tx = w * (i.toFloat() / ticks)
            val ty = h * (i.toFloat() / ticks)
            // Vertical & Horizontal crosshair guide wires
            drawLine(lineGridColor, Offset(tx, 0f), Offset(tx, h), strokeWidth = 0.8f)
            drawLine(lineGridColor, Offset(0f, ty), Offset(w, ty), strokeWidth = 0.8f)
            // Perimeter millimeter ticks
            drawLine(gridColor, Offset(tx, 0f), Offset(tx, 8f), strokeWidth = 1.2f)
            drawLine(gridColor, Offset(tx, h - 8f), Offset(tx, h), strokeWidth = 1.2f)
            drawLine(gridColor, Offset(0f, ty), Offset(8f, ty), strokeWidth = 1.2f)
            drawLine(gridColor, Offset(w - 8f, ty), Offset(w, ty), strokeWidth = 1.2f)
        }

        // Color Palette for 3D Anatomical Vector Chiseled Lines
        val shadowColor = Color.Black.copy(alpha = 0.45f)
        val contourColor = Color(0xFF8A9BAE) // Slate metallic primary
        val muscleColor = Color(0xFF5A6B7E)  // Soft muscle fiber
        val highlightColor = Color(0xE0E2ECF4) // Specular highlight ridge
        val nodeGold = Color(0xFFE5C158)

        val shadowOffX = 2.2f
        val shadowOffY = 2.8f
        val highlightOffX = -0.9f
        val highlightOffY = -1.1f

        val strokeStyle = Stroke(width = 2.6f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val shadowStyle = Stroke(width = 3.6f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val highlightStyle = Stroke(width = 1.1f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val subtleStyle = Stroke(width = 1.4f, cap = StrokeCap.Round, join = StrokeJoin.Round)

        fun draw3dPath(path: Path, widthFactor: Float = 1.0f, alpha: Float = 1.0f) {
            val sw = strokeStyle.width * widthFactor
            // 1. Drop shadow
            val shadowPath = Path().apply {
                addPath(path, Offset(shadowOffX, shadowOffY))
            }
            drawPath(shadowPath, shadowColor.copy(alpha = alpha * 0.45f), style = Stroke(width = sw * 1.35f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            // 2. Main contour body
            drawPath(path, contourColor.copy(alpha = alpha), style = Stroke(width = sw, cap = StrokeCap.Round, join = StrokeJoin.Round))
            // 3. Specular highlight line
            val hlPath = Path().apply {
                addPath(path, Offset(highlightOffX, highlightOffY))
            }
            drawPath(hlPath, highlightColor.copy(alpha = alpha * 0.70f), style = Stroke(width = (sw * 0.35f).coerceAtLeast(0.7f), cap = StrokeCap.Round, join = StrokeJoin.Round))
        }

        fun draw3dLine(start: Offset, end: Offset, widthFactor: Float = 1.0f, alpha: Float = 1.0f) {
            val sw = strokeStyle.width * widthFactor
            // Drop shadow
            drawLine(shadowColor.copy(alpha = alpha * 0.45f), start + Offset(shadowOffX, shadowOffY), end + Offset(shadowOffX, shadowOffY), strokeWidth = sw * 1.35f, cap = StrokeCap.Round)
            // Main body
            drawLine(muscleColor.copy(alpha = alpha), start, end, strokeWidth = sw, cap = StrokeCap.Round)
            // Specular highlight
            drawLine(highlightColor.copy(alpha = alpha * 0.60f), start + Offset(highlightOffX, highlightOffY), end + Offset(highlightOffX, highlightOffY), strokeWidth = (sw * 0.35f).coerceAtLeast(0.7f), cap = StrokeCap.Round)
        }

        fun draw3dNode(center: Offset, radius: Float = 4.0f) {
            // Shadow
            drawCircle(shadowColor, radius = radius * 1.3f, center = center + Offset(shadowOffX, shadowOffY))
            // Gold Node
            drawCircle(nodeGold, radius = radius, center = center)
            // Catchlight
            drawCircle(Color.White, radius = radius * 0.45f, center = center + Offset(-0.8f, -0.9f))
        }

        when (zone) {
            BodyZone.FOREARM -> {
                // High-fidelity forearm contour with brachioradialis & flexor muscle bellies
                val path = Path().apply {
                    moveTo(w * 0.30f, h * 0.08f)
                    cubicTo(w * 0.20f, h * 0.28f, w * 0.26f, h * 0.55f, w * 0.36f, h * 0.92f)
                    lineTo(w * 0.64f, h * 0.92f)
                    cubicTo(w * 0.74f, h * 0.55f, w * 0.80f, h * 0.28f, w * 0.70f, h * 0.08f)
                    close()
                }
                draw3dPath(path, widthFactor = 1.15f)

                // Muscle flexor grooves & radial artery line
                val flexor1 = Path().apply {
                    moveTo(w * 0.34f, h * 0.22f)
                    cubicTo(w * 0.42f, h * 0.45f, w * 0.44f, h * 0.70f, w * 0.42f, h * 0.88f)
                }
                val flexor2 = Path().apply {
                    moveTo(w * 0.66f, h * 0.22f)
                    cubicTo(w * 0.58f, h * 0.45f, w * 0.56f, h * 0.70f, w * 0.58f, h * 0.88f)
                }
                draw3dPath(flexor1, widthFactor = 0.70f, alpha = 0.80f)
                draw3dPath(flexor2, widthFactor = 0.70f, alpha = 0.80f)

                // Wrist flexor crease line
                draw3dLine(Offset(w * 0.37f, h * 0.88f), Offset(w * 0.63f, h * 0.88f), widthFactor = 0.85f)

                // Radial styloid landmark nodes
                draw3dNode(Offset(w * 0.36f, h * 0.90f), 3.5f)
                draw3dNode(Offset(w * 0.64f, h * 0.90f), 3.5f)
            }

            BodyZone.SHOULDER -> {
                // Muscular deltoid outline (anterior, lateral, posterior heads)
                val path = Path().apply {
                    moveTo(w * 0.50f, h * 0.06f)
                    cubicTo(w * 0.12f, h * 0.18f, w * 0.14f, h * 0.65f, w * 0.34f, h * 0.94f)
                    lineTo(w * 0.66f, h * 0.94f)
                    cubicTo(w * 0.86f, h * 0.65f, w * 0.88f, h * 0.18f, w * 0.50f, h * 0.06f)
                }
                draw3dPath(path, widthFactor = 1.20f)

                // Deltoid fiber striations
                val deltoid1 = Path().apply {
                    moveTo(w * 0.50f, h * 0.16f)
                    cubicTo(w * 0.34f, h * 0.38f, w * 0.38f, h * 0.68f, w * 0.48f, h * 0.88f)
                }
                val deltoid2 = Path().apply {
                    moveTo(w * 0.50f, h * 0.16f)
                    cubicTo(w * 0.66f, h * 0.38f, w * 0.62f, h * 0.68f, w * 0.52f, h * 0.88f)
                }
                draw3dPath(deltoid1, widthFactor = 0.75f, alpha = 0.80f)
                draw3dPath(deltoid2, widthFactor = 0.75f, alpha = 0.80f)

                // Acromion collarbone insertion node
                draw3dNode(Offset(w * 0.50f, h * 0.06f), 4.2f)
            }

            BodyZone.WRIST -> {
                // Wrist base and palm baseline with distal styloid flares
                val path = Path().apply {
                    moveTo(w * 0.32f, h * 0.08f)
                    lineTo(w * 0.32f, h * 0.55f)
                    cubicTo(w * 0.22f, h * 0.72f, w * 0.28f, h * 0.94f, w * 0.40f, h * 0.94f)
                    lineTo(w * 0.60f, h * 0.94f)
                    cubicTo(w * 0.72f, h * 0.94f, w * 0.78f, h * 0.72f, w * 0.68f, h * 0.55f)
                    lineTo(w * 0.68f, h * 0.08f)
                }
                draw3dPath(path, widthFactor = 1.15f)

                // Double wrist flexor crease lines
                draw3dLine(Offset(w * 0.34f, h * 0.42f), Offset(w * 0.66f, h * 0.42f), widthFactor = 0.9f)
                draw3dLine(Offset(w * 0.36f, h * 0.48f), Offset(w * 0.64f, h * 0.48f), widthFactor = 0.75f)

                // Ulnar & Radial styloid bone nodes
                draw3dNode(Offset(w * 0.32f, h * 0.52f), 4.0f)
                draw3dNode(Offset(w * 0.68f, h * 0.52f), 4.0f)
            }

            BodyZone.NECK -> {
                // Trapezius slope, neck column, and collarbone junction
                val path = Path().apply {
                    moveTo(w * 0.08f, h * 0.88f)
                    cubicTo(w * 0.26f, h * 0.72f, w * 0.30f, h * 0.42f, w * 0.30f, h * 0.08f)
                    lineTo(w * 0.70f, h * 0.08f)
                    cubicTo(w * 0.70f, h * 0.42f, w * 0.74f, h * 0.72f, w * 0.92f, h * 0.88f)
                }
                draw3dPath(path, widthFactor = 1.20f)

                // Sternocleidomastoid muscle V-lines
                draw3dLine(Offset(w * 0.32f, h * 0.12f), Offset(w * 0.48f, h * 0.80f), widthFactor = 0.85f)
                draw3dLine(Offset(w * 0.68f, h * 0.12f), Offset(w * 0.52f, h * 0.80f), widthFactor = 0.85f)

                // Collarbones (clavicles)
                val leftClavicle = Path().apply {
                    moveTo(w * 0.12f, h * 0.86f)
                    cubicTo(w * 0.28f, h * 0.82f, w * 0.42f, h * 0.80f, w * 0.48f, h * 0.82f)
                }
                val rightClavicle = Path().apply {
                    moveTo(w * 0.88f, h * 0.86f)
                    cubicTo(w * 0.72f, h * 0.82f, w * 0.58f, h * 0.80f, w * 0.52f, h * 0.82f)
                }
                draw3dPath(leftClavicle, widthFactor = 0.95f)
                draw3dPath(rightClavicle, widthFactor = 0.95f)

                // Jugular notch central node
                draw3dNode(Offset(w * 0.50f, h * 0.82f), 4.5f)
            }

            BodyZone.CHEST -> {
                // Pectoral major contours, sternum center line and ribcage arch
                val path = Path().apply {
                    moveTo(w * 0.10f, h * 0.12f)
                    lineTo(w * 0.90f, h * 0.12f)
                    lineTo(w * 0.82f, h * 0.88f)
                    cubicTo(w * 0.66f, h * 0.92f, w * 0.55f, h * 0.72f, w * 0.50f, h * 0.66f)
                    cubicTo(w * 0.45f, h * 0.72f, w * 0.34f, h * 0.92f, w * 0.18f, h * 0.88f)
                    close()
                }
                draw3dPath(path, widthFactor = 1.25f)

                // Sternum center line with 3D chiseled depth
                draw3dLine(Offset(w * 0.50f, h * 0.14f), Offset(w * 0.50f, h * 0.78f), widthFactor = 1.0f)

                // Pectoral arch muscle curves
                val leftPec = Path().apply {
                    moveTo(w * 0.18f, h * 0.22f)
                    cubicTo(w * 0.32f, h * 0.52f, w * 0.46f, h * 0.58f, w * 0.50f, h * 0.58f)
                }
                val rightPec = Path().apply {
                    moveTo(w * 0.82f, h * 0.22f)
                    cubicTo(w * 0.68f, h * 0.52f, w * 0.54f, h * 0.58f, w * 0.50f, h * 0.58f)
                }
                draw3dPath(leftPec, widthFactor = 0.80f, alpha = 0.85f)
                draw3dPath(rightPec, widthFactor = 0.80f, alpha = 0.85f)

                // Solar plexus center node
                draw3dNode(Offset(w * 0.50f, h * 0.66f), 4.2f)
            }

            BodyZone.BACK -> {
                // Latissimus dorsi, trapezius, spinal column and scapula wings
                val path = Path().apply {
                    moveTo(w * 0.18f, h * 0.06f)
                    lineTo(w * 0.82f, h * 0.06f)
                    cubicTo(w * 0.88f, h * 0.42f, w * 0.78f, h * 0.74f, w * 0.72f, h * 0.95f)
                    lineTo(w * 0.28f, h * 0.95f)
                    cubicTo(w * 0.22f, h * 0.74f, w * 0.12f, h * 0.42f, w * 0.18f, h * 0.06f)
                    close()
                }
                draw3dPath(path, widthFactor = 1.25f)

                // Spinal column midline
                draw3dLine(Offset(w * 0.50f, h * 0.08f), Offset(w * 0.50f, h * 0.92f), widthFactor = 1.10f)

                // Thoracic vertebrae landmark nodes along spine
                val vertebraeCount = 7
                for (v in 1..vertebraeCount) {
                    val vy = h * (0.12f + v * 0.10f)
                    draw3dNode(Offset(w * 0.50f, vy), 2.8f)
                }

                // Scapula wing bevels (shoulder blades)
                val leftScapula = Path().apply {
                    moveTo(w * 0.30f, h * 0.22f)
                    cubicTo(w * 0.42f, h * 0.28f, w * 0.40f, h * 0.48f, w * 0.28f, h * 0.52f)
                }
                val rightScapula = Path().apply {
                    moveTo(w * 0.70f, h * 0.22f)
                    cubicTo(w * 0.58f, h * 0.28f, w * 0.60f, h * 0.48f, w * 0.72f, h * 0.52f)
                }
                draw3dPath(leftScapula, widthFactor = 0.85f, alpha = 0.85f)
                draw3dPath(rightScapula, widthFactor = 0.85f, alpha = 0.85f)
            }

            BodyZone.CALF -> {
                // Gastrocnemius (calf muscle belly), soleus & Achilles tendon
                val path = Path().apply {
                    moveTo(w * 0.36f, h * 0.05f)
                    cubicTo(w * 0.16f, h * 0.30f, w * 0.22f, h * 0.62f, w * 0.38f, h * 0.95f)
                    lineTo(w * 0.62f, h * 0.95f)
                    cubicTo(w * 0.78f, h * 0.62f, w * 0.84f, h * 0.30f, w * 0.64f, h * 0.05f)
                    close()
                }
                draw3dPath(path, widthFactor = 1.20f)

                // Calf muscle bifurcation groove
                draw3dLine(Offset(w * 0.50f, h * 0.16f), Offset(w * 0.50f, h * 0.52f), widthFactor = 0.95f)

                // Soleus & Achilles tendon transition lines
                draw3dLine(Offset(w * 0.44f, h * 0.58f), Offset(w * 0.44f, h * 0.92f), widthFactor = 0.75f)
                draw3dLine(Offset(w * 0.56f, h * 0.58f), Offset(w * 0.56f, h * 0.92f), widthFactor = 0.75f)

                // Achilles insertion node
                draw3dNode(Offset(w * 0.50f, h * 0.90f), 3.8f)
            }

            BodyZone.ANKLE -> {
                // Lower tibia shaft, malleolus bone flares and Achilles tendon
                val path = Path().apply {
                    moveTo(w * 0.38f, h * 0.05f)
                    lineTo(w * 0.35f, h * 0.62f)
                    cubicTo(w * 0.24f, h * 0.74f, w * 0.26f, h * 0.88f, w * 0.36f, h * 0.95f)
                    lineTo(w * 0.64f, h * 0.95f)
                    cubicTo(w * 0.74f, h * 0.88f, w * 0.76f, h * 0.74f, w * 0.65f, h * 0.62f)
                    lineTo(w * 0.62f, h * 0.05f)
                }
                draw3dPath(path, widthFactor = 1.20f)

                // Achilles tendon central guide
                draw3dLine(Offset(w * 0.50f, h * 0.08f), Offset(w * 0.50f, h * 0.72f), widthFactor = 0.85f)

                // Medial & Lateral Malleolus ankle bone nodes
                draw3dNode(Offset(w * 0.32f, h * 0.78f), 4.8f)
                draw3dNode(Offset(w * 0.68f, h * 0.80f), 4.8f)
            }
        }
    }
}
