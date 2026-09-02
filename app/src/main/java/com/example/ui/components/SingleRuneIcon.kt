package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.Rune

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

        for (stroke in rune.strokes) {
            if (stroke.points.size < 2) continue
            val path = Path().apply {
                moveTo(stroke.points[0].x * scaleX, stroke.points[0].y * scaleY)
                for (i in 1 until stroke.points.size) {
                    lineTo(stroke.points[i].x * scaleX, stroke.points[i].y * scaleY)
                }
            }
            drawPath(
                path = path,
                color = color,
                style = Stroke(
                    width = strokeWidthPx,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }
}
