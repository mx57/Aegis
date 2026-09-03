package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Rune
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * State of the sacred pouch divination ritual.
 */
enum class PouchRitualState {
    IDLE,       // Waiting for touch/shake
    SHAKING,    // Shaking pouch with mystical sparks
    DRAWING,    // Rune emerges upwards out of the pouch mouth
    REVEALED    // Rune is flipped and fully displayed with interpretation
}

/**
 * Sacred Nordic Pouch Component drawn entirely with precision vector geometry on Canvas.
 * Features realistic fabric pleats, golden runic embroidery (Triquetra/Aegishjalmur),
 * tied golden cord, and dynamic breathing/shake animations.
 */
@Composable
fun SacredRunePouch(
    modifier: Modifier = Modifier,
    isShaking: Boolean = false,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pouch_breath")
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathScale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    // Shake animation
    val shakeAnim = remember { Animatable(0f) }
    LaunchedEffect(isShaking) {
        if (isShaking) {
            // Rapid multi-frequency vibration
            for (i in 0 until 6) {
                shakeAnim.animateTo(
                    targetValue = if (i % 2 == 0) 14f else -14f,
                    animationSpec = tween(70, easing = LinearEasing)
                )
            }
            shakeAnim.animateTo(0f, animationSpec = spring(stiffness = Spring.StiffnessMedium))
        } else {
            shakeAnim.snapTo(0f)
        }
    }

    Box(
        modifier = modifier
            .size(240.dp, 260.dp)
            .graphicsLayer {
                rotationZ = shakeAnim.value
                scaleX = if (isShaking) 1.06f else breathScale
                scaleY = if (isShaking) 0.94f else breathScale
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawSacredPouch(glowAlpha = glowAlpha, isShaking = isShaking)
        }
    }
}

/**
 * Procedural Vector drawing of the Sacred Nordic Leather Pouch
 */
private fun DrawScope.drawSacredPouch(glowAlpha: Float, isShaking: Boolean) {
    val w = size.width
    val h = size.height

    val cx = w / 2f
    val cy = h / 2f + 10f

    // 1. Ambient Golden Halo behind pouch
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0x55E5C158),
                Color(0x22F3D97A),
                Color.Transparent
            ),
            center = Offset(cx, cy),
            radius = w * 0.52f
        ),
        radius = w * 0.52f,
        center = Offset(cx, cy)
    )

    // 2. Leather Bag Body Path (flared bottom with soft curves)
    val pouchBody = Path().apply {
        // Neck left
        moveTo(cx - 36f, cy - 65f)
        // Flaring out to left shoulder and rounded base
        cubicTo(cx - 75f, cy - 40f, cx - 100f, cy + 20f, cx - 88f, cy + 70f)
        cubicTo(cx - 78f, cy + 105f, cx - 40f, cy + 115f, cx, cy + 116f)
        cubicTo(cx + 40f, cy + 115f, cx + 78f, cy + 105f, cx + 88f, cy + 70f)
        cubicTo(cx + 100f, cy + 20f, cx + 75f, cy - 40f, cx + 36f, cy - 65f)
        close()
    }

    // Deep textured leather gradient (Obsidian Slate with rich shading)
    val leatherGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF262A36),
            Color(0xFF1B1E28),
            Color(0xFF12141C),
            Color(0xFF0D0F14)
        ),
        startY = cy - 65f,
        endY = cy + 116f
    )
    drawPath(path = pouchBody, brush = leatherGradient)

    // Highlight ridge on the left side of pouch
    val highlightPath = Path().apply {
        moveTo(cx - 36f, cy - 60f)
        cubicTo(cx - 70f, cy - 35f, cx - 90f, cy + 20f, cx - 78f, cy + 70f)
        cubicTo(cx - 68f, cy + 98f, cx - 35f, cy + 108f, cx - 5f, cy + 110f)
    }
    drawPath(
        path = highlightPath,
        color = Color(0x33F3D97A),
        style = Stroke(width = 3.5f, cap = StrokeCap.Round)
    )

    // Outer subtle gold trim contour
    drawPath(
        path = pouchBody,
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFFE5C158), Color(0xFF9E7C20), Color(0xFFE5C158))
        ),
        style = Stroke(width = 1.8f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // 3. Vertical leather fold/seam grooves
    val seam1 = Path().apply {
        moveTo(cx - 24f, cy - 60f)
        cubicTo(cx - 40f, cy - 10f, cx - 45f, cy + 45f, cx - 30f, cy + 95f)
    }
    val seam2 = Path().apply {
        moveTo(cx + 24f, cy - 60f)
        cubicTo(cx + 40f, cy - 10f, cx + 45f, cy + 45f, cx + 30f, cy + 95f)
    }
    drawPath(seam1, color = Color(0x22000000), style = Stroke(width = 4f, cap = StrokeCap.Round))
    drawPath(seam1, color = Color(0x33F3D97A), style = Stroke(width = 1.2f, cap = StrokeCap.Round))
    drawPath(seam2, color = Color(0x22000000), style = Stroke(width = 4f, cap = StrokeCap.Round))
    drawPath(seam2, color = Color(0x33F3D97A), style = Stroke(width = 1.2f, cap = StrokeCap.Round))

    // 4. Center Gold Sacred Embroidery (Triquetra / Aegis Node)
    val embCy = cy + 22f
    val goldColor = Color(0xFFE5C158).copy(alpha = glowAlpha)
    val goldShine = Color(0xFFFFF2A8)

    // Concentric sacred ring
    drawCircle(
        color = goldColor,
        radius = 28f,
        center = Offset(cx, embCy),
        style = Stroke(width = 1.5f)
    )
    drawCircle(
        color = goldColor.copy(alpha = 0.6f),
        radius = 33f,
        center = Offset(cx, embCy),
        style = Stroke(width = 0.8f, pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(5f, 4f)))
    )

    // Sacred Triquetra Knot
    val rKnot = 18f
    for (i in 0 until 3) {
        val angle = (i * 120.0 - 90.0) * PI / 180.0
        val kx = cx + (rKnot * 0.6f * cos(angle)).toFloat()
        val ky = embCy + (rKnot * 0.6f * sin(angle)).toFloat()
        drawCircle(
            color = goldColor,
            radius = rKnot * 0.75f,
            center = Offset(kx, ky),
            style = Stroke(width = 1.4f)
        )
    }
    // Center bind eye
    drawCircle(color = goldShine, radius = 3.2f, center = Offset(cx, embCy))

    // 5. Cinched Neck & Folded Collar Top
    val collarPath = Path().apply {
        moveTo(cx - 36f, cy - 65f)
        cubicTo(cx - 45f, cy - 82f, cx - 50f, cy - 98f, cx - 38f, cy - 104f)
        cubicTo(cx - 20f, cy - 96f, cx - 10f, cy - 108f, cx, cy - 100f)
        cubicTo(cx + 10f, cy - 108f, cx + 20f, cy - 96f, cx + 38f, cy - 104f)
        cubicTo(cx + 50f, cy - 98f, cx + 45f, cy - 82f, cx + 36f, cy - 65f)
        close()
    }
    drawPath(
        path = collarPath,
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF2C3140), Color(0xFF1B1E28)),
            startY = cy - 104f,
            endY = cy - 65f
        )
    )
    drawPath(
        path = collarPath,
        color = Color(0xFFE5C158),
        style = Stroke(width = 1.4f)
    )

    // Deep interior mouth shadow (where runes emerge)
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF000000), Color(0xFF0A0C10)),
            center = Offset(cx, cy - 70f),
            radius = 32f
        ),
        topLeft = Offset(cx - 34f, cy - 78f),
        size = Size(68f, 20f)
    )

    // 6. Golden Braided Cinch Cord & Hanging Tassels
    val cordY = cy - 64f
    // Braided wrap around the neck
    for (step in -2..2) {
        val yOffset = cordY + step * 2.5f
        drawLine(
            brush = Brush.horizontalGradient(
                colors = listOf(Color(0xFF9E7C20), Color(0xFFFFF2A8), Color(0xFFE5C158), Color(0xFF9E7C20)),
                startX = cx - 36f,
                endX = cx + 36f
            ),
            start = Offset(cx - 35f, yOffset),
            end = Offset(cx + 35f, yOffset),
            strokeWidth = 2.4f,
            cap = StrokeCap.Round
        )
    }

    // Sacred knot in the center of the cord
    drawCircle(color = Color(0xFFFFF2A8), radius = 4.5f, center = Offset(cx, cordY))
    drawCircle(color = Color(0xFF9E7C20), radius = 4.5f, center = Offset(cx, cordY), style = Stroke(width = 1f))

    // Left Hanging Cord with Rune Bead
    val leftCord = Path().apply {
        moveTo(cx - 2f, cordY + 2f)
        cubicTo(cx - 15f, cordY + 20f, cx - 22f, cordY + 45f, cx - 18f, cordY + 70f)
    }
    drawPath(leftCord, color = Color(0xFFE5C158), style = Stroke(width = 2.2f, cap = StrokeCap.Round))
    // Left wooden/gold bead
    drawCircle(color = Color(0xFFF3D97A), radius = 4f, center = Offset(cx - 18f, cordY + 68f))
    drawCircle(color = Color(0xFF1E212B), radius = 2f, center = Offset(cx - 18f, cordY + 68f))

    // Right Hanging Cord with Rune Bead
    val rightCord = Path().apply {
        moveTo(cx + 2f, cordY + 2f)
        cubicTo(cx + 18f, cordY + 25f, cx + 26f, cordY + 50f, cx + 22f, cordY + 78f)
    }
    drawPath(rightCord, color = Color(0xFFE5C158), style = Stroke(width = 2.2f, cap = StrokeCap.Round))
    // Right bead
    drawCircle(color = Color(0xFFF3D97A), radius = 4.5f, center = Offset(cx + 22f, cordY + 76f))
    drawCircle(color = Color(0xFF1E212B), radius = 2.2f, center = Offset(cx + 22f, cordY + 76f))

    // 7. Mystical Sparks floating when shaking
    if (isShaking) {
        val sparkPoints = listOf(
            Offset(cx - 50f, cy - 30f),
            Offset(cx + 48f, cy - 40f),
            Offset(cx - 20f, cy - 90f),
            Offset(cx + 25f, cy - 85f),
            Offset(cx, cy - 110f),
            Offset(cx - 65f, cy + 30f),
            Offset(cx + 70f, cy + 20f)
        )
        for (sp in sparkPoints) {
            drawCircle(color = Color(0xFFFFF7C2), radius = 2.8f, center = sp)
            drawLine(
                color = Color(0xAAFFF7C2),
                start = Offset(sp.x - 4f, sp.y),
                end = Offset(sp.x + 4f, sp.y),
                strokeWidth = 1f
            )
            drawLine(
                color = Color(0xAAFFF7C2),
                start = Offset(sp.x, sp.y - 4f),
                end = Offset(sp.x, sp.y + 4f),
                strokeWidth = 1f
            )
        }
    }
}

/**
 * Sacred Rune Stone Tablet.
 * Can be displayed face-down (wooden/stone Celtic knot backing) or face-up (golden engraved rune),
 * with smooth 3D flip animation support and bevel highlights.
 */
@Composable
fun SacredRuneTablet(
    rune: Rune,
    isReversed: Boolean = false,
    flipProgress: Float = 1f, // 0f = face-down, 1f = face-up
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    onClick: () -> Unit = {}
) {
    // Rotation Y for 3D flip effect: 0..180 deg
    val rotationY = (1f - flipProgress) * 180f
    val isFrontVisible = rotationY < 90f

    val infiniteTransition = rememberInfiniteTransition(label = "tablet_glow")
    val ambientGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambientGlow"
    )

    Box(
        modifier = modifier
            .size(size, size * 1.35f)
            .graphicsLayer {
                this.rotationY = rotationY
                cameraDistance = 14f * density
                if (isFrontVisible && isReversed) {
                    rotationZ = 180f
                }
            }
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(22.dp),
                spotColor = Color(0xFFE5C158),
                ambientColor = Color(0xFFE5C158)
            )
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isFrontVisible) {
            // FRONT FACE: Slate/Wood tablet with gold engraved rune
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawTabletFront(ambientGlow)
            }

            // The Rune symbol itself in metallic gold
            SingleRuneIcon(
                rune = rune,
                size = size * 0.65f,
                color = Color(0xFFFFE082),
                strokeWidthDp = 4.5.dp,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            // BACK FACE: Mystical Celtic interlacing knot on sacred dark stone
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { this.rotationY = 180f }
            ) {
                drawTabletBack()
            }
        }
    }
}

private fun DrawScope.drawTabletFront(glowAlpha: Float) {
    val w = size.width
    val h = size.height
    val cornerRadius = CornerRadius(44f, 44f)

    // Base dark slate with rich subtle radial sheen
    drawRoundRect(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF242834), Color(0xFF141720), Color(0xFF0C0E14)),
            center = Offset(w * 0.4f, h * 0.35f),
            radius = w * 0.9f
        ),
        size = size,
        cornerRadius = cornerRadius
    )

    // Chiseled Outer Bevel Highlight (Light from top-left)
    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0x88F3D97A),
                Color(0x22E5C158),
                Color(0x11000000),
                Color(0x55000000)
            ),
            start = Offset(0f, 0f),
            end = Offset(w, h)
        ),
        size = size,
        cornerRadius = cornerRadius,
        style = Stroke(width = 3.5f)
    )

    // Inner Inset Sacred Gold Frame
    val inset = 12f
    val innerSize = Size(w - inset * 2, h - inset * 2)
    val innerCorner = CornerRadius(28f, 28f)

    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFFFFF2A8).copy(alpha = glowAlpha),
                Color(0xFFE5C158).copy(alpha = 0.7f),
                Color(0xFF9E7C20).copy(alpha = 0.5f),
                Color(0xFFE5C158).copy(alpha = glowAlpha)
            )
        ),
        topLeft = Offset(inset, inset),
        size = innerSize,
        cornerRadius = innerCorner,
        style = Stroke(width = 1.6f)
    )

    // Corner rivet dots
    val rivets = listOf(
        Offset(inset + 8f, inset + 8f),
        Offset(w - inset - 8f, inset + 8f),
        Offset(inset + 8f, h - inset - 8f),
        Offset(w - inset - 8f, h - inset - 8f)
    )
    for (r in rivets) {
        drawCircle(color = Color(0xFFFFF2A8), radius = 2.5f, center = r)
        drawCircle(color = Color(0xFF9E7C20), radius = 1.2f, center = r)
    }
}

private fun DrawScope.drawTabletBack() {
    val w = size.width
    val h = size.height
    val cornerRadius = CornerRadius(44f, 44f)

    // Dark obsidian textured stone
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF1E212B), Color(0xFF13151D), Color(0xFF0B0C10))
        ),
        size = size,
        cornerRadius = cornerRadius
    )

    // Outer border
    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFFB59334), Color(0xFF5E4915), Color(0xFFB59334))
        ),
        size = size,
        cornerRadius = cornerRadius,
        style = Stroke(width = 2.5f)
    )

    // Celtic Knotwork Diamond Backing
    val cx = w / 2f
    val cy = h / 2f

    val diamondPath = Path().apply {
        moveTo(cx, cy - h * 0.35f)
        lineTo(cx + w * 0.35f, cy)
        lineTo(cx, cy + h * 0.35f)
        lineTo(cx - w * 0.35f, cy)
        close()
    }
    drawPath(
        diamondPath,
        color = Color(0x44E5C158),
        style = Stroke(width = 2f)
    )

    // Center sacred solar ring
    drawCircle(
        color = Color(0x66E5C158),
        radius = w * 0.22f,
        center = Offset(cx, cy),
        style = Stroke(width = 1.5f)
    )
    drawCircle(
        color = Color(0xFFFFF2A8),
        radius = 4f,
        center = Offset(cx, cy)
    )

    // 4 Intersecting arcs
    for (i in 0 until 4) {
        val angle = i * 90.0 * PI / 180.0
        val ax = cx + (w * 0.15f * cos(angle)).toFloat()
        val ay = cy + (h * 0.15f * sin(angle)).toFloat()
        drawCircle(
            color = Color(0x33F3D97A),
            radius = w * 0.14f,
            center = Offset(ax, ay),
            style = Stroke(width = 1.2f)
        )
    }
}
