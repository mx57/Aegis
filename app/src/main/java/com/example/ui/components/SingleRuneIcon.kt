package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.Rune

/**
 * High-fidelity 3D chiseled vector icon for individual runes.
 * Renders multi-layered metallic gold/engraved relief with drop shadows,
 * main stroke body, and specular highlights.
 */
@Composable
fun SingleRuneIcon(
    rune: Rune,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    strokeWidthDp: Dp = 2.5.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val scaleX = w / 100f
        val scaleY = h / 140f
        val strokeWidthPx = strokeWidthDp.toPx()

        val chiselOffX = (strokeWidthPx * 0.28f).coerceAtLeast(0.8f)
        val chiselOffY = (strokeWidthPx * 0.32f).coerceAtLeast(0.9f)
        val shadowColor = Color.Black.copy(alpha = 0.42f)
        val highlightColor = Color.White.copy(alpha = 0.58f)

        for (stroke in rune.strokes) {
            val pts = stroke.points
            if (pts.isEmpty()) continue

            if (pts.size == 1) {
                // Sacred single node/dot rendering with 3D engraving relief
                val cx = pts[0].x * scaleX
                val cy = pts[0].y * scaleY
                val radius = (strokeWidthPx * 0.9f).coerceAtLeast(1.5f)

                // 1. Drop shadow dot
                drawCircle(
                    color = shadowColor,
                    radius = radius * 1.2f,
                    center = Offset(cx + chiselOffX, cy + chiselOffY)
                )
                // 2. Main dot body
                drawCircle(
                    color = color,
                    radius = radius,
                    center = Offset(cx, cy)
                )
                // 3. Specular catchlight glint
                drawCircle(
                    color = highlightColor,
                    radius = (radius * 0.4f).coerceAtLeast(0.7f),
                    center = Offset(cx - chiselOffX * 0.45f, cy - chiselOffY * 0.45f)
                )
            } else {
                // Multi-point polyline rendering with 3D chiseled bevels
                val mainPath = Path().apply {
                    moveTo(pts[0].x * scaleX, pts[0].y * scaleY)
                    for (i in 1 until pts.size) {
                        lineTo(pts[i].x * scaleX, pts[i].y * scaleY)
                    }
                }

                // 1. Deep chiseled drop shadow layer
                val shadowPath = Path().apply {
                    moveTo(pts[0].x * scaleX + chiselOffX, pts[0].y * scaleY + chiselOffY)
                    for (i in 1 until pts.size) {
                        lineTo(pts[i].x * scaleX + chiselOffX, pts[i].y * scaleY + chiselOffY)
                    }
                }
                drawPath(
                    path = shadowPath,
                    color = shadowColor,
                    style = Stroke(
                        width = strokeWidthPx * 1.30f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // 2. Main stroke body
                drawPath(
                    path = mainPath,
                    color = color,
                    style = Stroke(
                        width = strokeWidthPx,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // 3. Specular highlight line (sharp metallic bevel edge)
                val highlightPath = Path().apply {
                    moveTo(pts[0].x * scaleX - chiselOffX * 0.45f, pts[0].y * scaleY - chiselOffY * 0.45f)
                    for (i in 1 until pts.size) {
                        lineTo(pts[i].x * scaleX - chiselOffX * 0.45f, pts[i].y * scaleY - chiselOffY * 0.45f)
                    }
                }
                drawPath(
                    path = highlightPath,
                    color = highlightColor,
                    style = Stroke(
                        width = (strokeWidthPx * 0.35f).coerceAtLeast(0.7f),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }
    }
}
