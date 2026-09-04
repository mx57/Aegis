package com.example.ui.components

import android.graphics.Paint as AndroidPaint
import android.graphics.Typeface as AndroidTypeface
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import com.example.data.model.StrokePoint
import com.example.engine.CanvasTheme
import com.example.engine.CenterEmblem
import com.example.engine.ComposedStave
import com.example.engine.CornerStyle
import com.example.engine.ELDER_FUTHARK_RUNES
import com.example.engine.FinialType
import com.example.engine.FrameStyle
import com.example.engine.GeneratedOrnaments
import com.example.engine.OrnamentGeometry
import com.example.engine.SketchConfig
import com.example.engine.SketchStyle
import java.util.Random
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Animated and visually refined interactive canvas for Runic Staves.
 * Supports phased magical carving animation, chisel sparks, Elder Futhark rune ring,
 * and high-fidelity themes.
 */
@Composable
fun RunicCanvas(
    stave: ComposedStave,
    config: SketchConfig,
    modifier: Modifier = Modifier,
    overrideColor: Color? = null,
    animateOnAppear: Boolean = true,
    animationProgress: Float? = null,
    animationKey: Any? = null,
    animationDurationMs: Int = 4200
) {
    val themePrimary = MaterialTheme.colorScheme.primary
    val themeOnPrimaryContainer = MaterialTheme.colorScheme.onPrimaryContainer
    val themeSecondary = MaterialTheme.colorScheme.secondary
    val themeOnSurface = MaterialTheme.colorScheme.onSurface

    // Animation driver
    val anim = remember(stave, animationKey) {
        Animatable(if (animateOnAppear) 0f else 1f)
    }

    LaunchedEffect(stave, animationKey, animationDurationMs) {
        if (animateOnAppear) {
            anim.snapTo(0f)
            anim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = animationDurationMs, easing = FastOutSlowInEasing)
            )
        }
    }

    val currentProgress = (animationProgress ?: anim.value).coerceIn(0f, 1f)

    // Subtle ambient breathing aura pulse after carving finishes
    val infiniteTransition = rememberInfiniteTransition(label = "stavePulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.82f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        val canvasSize = size.minDimension
        val scale = canvasSize / 500f
        val prng = Random(config.seed)

        // Palette resolution
        val (strokeColor, glowColor) = if (overrideColor != null) {
            Pair(overrideColor, overrideColor.copy(alpha = 0.35f))
        } else if (config.isStencil) {
            Pair(Color.Black, Color.Transparent)
        } else {
            when (config.effectiveTheme) {
                CanvasTheme.GRAPHITE_SKETCH -> {
                    Pair(Color(0xFF1E1A16), Color(0xFF423830).copy(alpha = 0.25f))
                }
                CanvasTheme.CHARCOAL_DARK -> {
                    Pair(Color(0xFFE8DFD0), Color(0xFFC8B69B).copy(alpha = 0.35f))
                }
                CanvasTheme.DARK_SLATE -> {
                    val color = when (config.style) {
                        SketchStyle.SACRED_GOLD -> Color(0xFFE5C158)
                        SketchStyle.EMERALD_BRONZE -> Color(0xFFCD9B51)
                        SketchStyle.FROST_CRYSTAL -> Color(0xFFB0E0E6)
                        SketchStyle.NORDIC_TATTOO -> Color(0xFFF8FAFC)
                        SketchStyle.VALKYRIE_SILVER -> Color(0xFFE2E8F0)
                        SketchStyle.STRICT -> themeOnPrimaryContainer
                        SketchStyle.ORNAMENTAL -> Color(0xFFE5C07B)
                        SketchStyle.WOODCARVE -> Color(0xFFD19A66)
                        SketchStyle.CELTIC_KNOT -> Color(0xFF98C379)
                        SketchStyle.AEGISHJALMUR -> Color(0xFF61AFEF)
                        SketchStyle.DOTWORK -> themeSecondary
                        SketchStyle.BLACKWORK -> themeOnSurface
                        else -> Color(android.graphics.Color.parseColor(config.effectiveTheme.strokeHex))
                    }
                    Pair(color, color.copy(alpha = 0.30f))
                }
                CanvasTheme.GOLDEN_EMBER -> {
                    Pair(Color(0xFFE5C158), Color(0xFFF3D882).copy(alpha = 0.40f))
                }
                CanvasTheme.EMERALD_PATINA -> {
                    Pair(Color(0xFFCD9B51), Color(0xFF52B788).copy(alpha = 0.40f))
                }
                CanvasTheme.AURORA_NIGHT -> {
                    Pair(Color(0xFF7EE0D2), Color(0xFF64FFDA).copy(alpha = 0.40f))
                }
                CanvasTheme.VALKYRIE_MITHRIL -> {
                    Pair(Color(0xFFE2E8F0), Color(0xFFCBD5E1).copy(alpha = 0.35f))
                }
                CanvasTheme.FROST_ICE -> {
                    Pair(Color(0xFFB0E0E6), Color(0xFF87CEFA).copy(alpha = 0.40f))
                }
                CanvasTheme.ANCIENT_PARCHMENT -> {
                    Pair(Color(0xFF2E1B0F), Color(0xFF8C5835).copy(alpha = 0.28f))
                }
                CanvasTheme.RUNESTONE_GRAY -> {
                    Pair(Color(0xFF88C0D0), Color(0xFF5E81AC).copy(alpha = 0.35f))
                }
                CanvasTheme.STENCIL -> {
                    Pair(Color.Black, Color.Transparent)
                }
            }
        }

        val effectiveStrokeWidth = when (config.style) {
            SketchStyle.BLACKWORK -> (config.lineWidth * 2.2f).coerceAtLeast(5.5f)
            SketchStyle.NORDIC_TATTOO -> (config.lineWidth * 1.35f).coerceAtLeast(3.8f)
            SketchStyle.ODIN_TOTEM -> (config.lineWidth * 1.25f).coerceAtLeast(3.2f)
            SketchStyle.VIKING_CHAIN -> (config.lineWidth * 1.30f).coerceAtLeast(3.4f)
            SketchStyle.WOODCUT_ENGRAVING -> config.lineWidth * 1.15f
            SketchStyle.RUNIC_OBELISK -> config.lineWidth * 1.20f
            SketchStyle.WOODCARVE -> config.lineWidth * 1.25f
            SketchStyle.SACRED_GOLD -> config.lineWidth * 0.95f
            SketchStyle.VALKYRIE_SILVER -> config.lineWidth * 0.90f
            SketchStyle.STRICT -> config.lineWidth * 0.85f
            else -> config.lineWidth
        } * scale

        // Ambient Paper & Stone Texture Background
        if (!config.isStencil && config.hasTextureGrain) {
            val bgTheme = config.effectiveTheme
            val bgCenter = try { Color(android.graphics.Color.parseColor(bgTheme.bgHex)) } catch (_: Exception) { Color.Transparent }
            val bgEdge = try { Color(android.graphics.Color.parseColor(bgTheme.bgEdgeHex)) } catch (_: Exception) { Color.Transparent }
            drawRect(
                brush = androidx.compose.ui.graphics.Brush.radialGradient(
                    colors = listOf(bgCenter, bgEdge),
                    center = Offset(250f * scale, 250f * scale),
                    radius = 350f * scale
                )
            )

            // Micro-speckled paper/slate texture stipples
            val noisePrng = Random(config.seed xor 0x5EEDCAFE)
            val noiseColor = strokeColor.copy(alpha = 0.045f)
            for (n in 0 until 140) {
                val nx = noisePrng.nextFloat() * 500f * scale
                val ny = noisePrng.nextFloat() * 500f * scale
                val nr = (noisePrng.nextFloat() * 1.6f + 0.6f) * scale
                drawCircle(color = noiseColor, radius = nr, center = Offset(nx, ny))
            }
        }

        val highlightColor = try { Color(android.graphics.Color.parseColor(config.effectiveTheme.highlightHex)) } catch (_: Exception) { Color.White }
        val shadowColor = try { Color(android.graphics.Color.parseColor(config.effectiveTheme.shadowHex)) } catch (_: Exception) { Color.Black }
        val isVolumetric = config.hasVolumetricShading && !config.isStencil
        val chiselOff = (effectiveStrokeWidth * 0.35f * config.runeChiselDepth).coerceAtLeast(1.2f)

        val metallicBrush = if (!config.isStencil && config.hasVolumetricShading) {
            when {
                config.style == SketchStyle.SACRED_GOLD || config.effectiveTheme == CanvasTheme.GOLDEN_EMBER -> {
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFFEEA0),
                            Color(0xFFE5C158),
                            Color(0xFFC49118),
                            Color(0xFFFFDF7A),
                            Color(0xFFB8860B)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(500f * scale, 500f * scale)
                    )
                }
                config.style == SketchStyle.VALKYRIE_SILVER || config.effectiveTheme == CanvasTheme.VALKYRIE_MITHRIL -> {
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFFFFFF),
                            Color(0xFFCBD5E1),
                            Color(0xFF94A3B8),
                            Color(0xFFF1F5F9),
                            Color(0xFF64748B)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(500f * scale, 500f * scale)
                    )
                }
                config.style == SketchStyle.EMERALD_BRONZE || config.effectiveTheme == CanvasTheme.EMERALD_PATINA -> {
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFF0CCA0),
                            Color(0xFFCD9B51),
                            Color(0xFF8C5D23),
                            Color(0xFFDFB370),
                            Color(0xFF5E3A10)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(500f * scale, 500f * scale)
                    )
                }
                else -> null
            }
        } else null

        // Helper to draw GeneratedOrnaments with alpha factor and volumetric 3D relief
        fun drawOrnaments(ornaments: GeneratedOrnaments, alphaFactor: Float = 1f) {
            if (alphaFactor <= 0.001f) return
            for (line in ornaments.lines) {
                val sw = (effectiveStrokeWidth * line.widthFactor).coerceAtLeast(1f)
                val lineAlpha = (line.alpha * alphaFactor).coerceIn(0f, 1f)
                if (isVolumetric) {
                    drawLine(
                        color = shadowColor.copy(alpha = lineAlpha * 0.45f),
                        start = Offset(line.x1 * scale + chiselOff, line.y1 * scale + chiselOff),
                        end = Offset(line.x2 * scale + chiselOff, line.y2 * scale + chiselOff),
                        strokeWidth = sw * 1.35f,
                        cap = StrokeCap.Round
                    )
                }
                if (metallicBrush != null) {
                    drawLine(
                        brush = metallicBrush,
                        alpha = lineAlpha,
                        start = Offset(line.x1 * scale, line.y1 * scale),
                        end = Offset(line.x2 * scale, line.y2 * scale),
                        strokeWidth = sw,
                        cap = StrokeCap.Round
                    )
                } else {
                    drawLine(
                        color = strokeColor.copy(alpha = lineAlpha),
                        start = Offset(line.x1 * scale, line.y1 * scale),
                        end = Offset(line.x2 * scale, line.y2 * scale),
                        strokeWidth = sw,
                        cap = StrokeCap.Round
                    )
                }
                if (isVolumetric && sw > 1.2f) {
                    drawLine(
                        color = highlightColor.copy(alpha = lineAlpha * 0.70f),
                        start = Offset(line.x1 * scale - chiselOff * 0.45f, line.y1 * scale - chiselOff * 0.45f),
                        end = Offset(line.x2 * scale - chiselOff * 0.45f, line.y2 * scale - chiselOff * 0.45f),
                        strokeWidth = (sw * 0.35f).coerceAtLeast(0.8f),
                        cap = StrokeCap.Round
                    )
                }
            }
            for (circle in ornaments.circles) {
                val circleAlpha = (circle.alpha * alphaFactor).coerceIn(0f, 1f)
                if (circle.isFilled) {
                    if (isVolumetric) {
                        drawCircle(
                            color = shadowColor.copy(alpha = circleAlpha * 0.45f),
                            radius = circle.radius * scale,
                            center = Offset(circle.cx * scale + chiselOff, circle.cy * scale + chiselOff)
                        )
                    }
                    if (metallicBrush != null) {
                        drawCircle(
                            brush = metallicBrush,
                            alpha = circleAlpha,
                            radius = circle.radius * scale,
                            center = Offset(circle.cx * scale, circle.cy * scale)
                        )
                    } else {
                        drawCircle(
                            color = strokeColor.copy(alpha = circleAlpha),
                            radius = circle.radius * scale,
                            center = Offset(circle.cx * scale, circle.cy * scale)
                        )
                    }
                    if (isVolumetric && circle.radius * scale > 2.0f) {
                        drawCircle(
                            color = highlightColor.copy(alpha = circleAlpha * 0.85f),
                            radius = (circle.radius * 0.45f * scale).coerceAtLeast(0.8f),
                            center = Offset(circle.cx * scale - chiselOff * 0.45f, circle.cy * scale - chiselOff * 0.45f)
                        )
                    }
                } else {
                    val sw = (effectiveStrokeWidth * circle.widthFactor).coerceAtLeast(1f)
                    if (isVolumetric) {
                        drawCircle(
                            color = shadowColor.copy(alpha = circleAlpha * 0.45f),
                            radius = circle.radius * scale,
                            center = Offset(circle.cx * scale + chiselOff, circle.cy * scale + chiselOff),
                            style = Stroke(width = sw * 1.35f)
                        )
                    }
                    if (metallicBrush != null) {
                        drawCircle(
                            brush = metallicBrush,
                            alpha = circleAlpha,
                            radius = circle.radius * scale,
                            center = Offset(circle.cx * scale, circle.cy * scale),
                            style = Stroke(width = sw)
                        )
                    } else {
                        drawCircle(
                            color = strokeColor.copy(alpha = circleAlpha),
                            radius = circle.radius * scale,
                            center = Offset(circle.cx * scale, circle.cy * scale),
                            style = Stroke(width = sw)
                        )
                    }
                    if (isVolumetric && sw > 1.2f) {
                        drawCircle(
                            color = highlightColor.copy(alpha = circleAlpha * 0.70f),
                            radius = circle.radius * scale,
                            center = Offset(circle.cx * scale - chiselOff * 0.45f, circle.cy * scale - chiselOff * 0.45f),
                            style = Stroke(width = (sw * 0.35f).coerceAtLeast(0.8f))
                        )
                    }
                }
            }
            for (poly in ornaments.polygons) {
                if (poly.points.size < 3) continue
                val polyAlpha = (poly.alpha * alphaFactor).coerceIn(0f, 1f)
                val sw = (effectiveStrokeWidth * poly.widthFactor).coerceAtLeast(1f)
                if (isVolumetric) {
                    val shadowPath = Path().apply {
                        moveTo(poly.points[0].x * scale + chiselOff, poly.points[0].y * scale + chiselOff)
                        for (i in 1 until poly.points.size) {
                            lineTo(poly.points[i].x * scale + chiselOff, poly.points[i].y * scale + chiselOff)
                        }
                        close()
                    }
                    if (poly.isFilled) {
                        drawPath(path = shadowPath, color = shadowColor.copy(alpha = polyAlpha * 0.45f))
                    } else {
                        drawPath(path = shadowPath, color = shadowColor.copy(alpha = polyAlpha * 0.45f), style = Stroke(width = sw * 1.35f, join = StrokeJoin.Round))
                    }
                }
                val path = Path().apply {
                    moveTo(poly.points[0].x * scale, poly.points[0].y * scale)
                    for (i in 1 until poly.points.size) {
                        lineTo(poly.points[i].x * scale, poly.points[i].y * scale)
                    }
                    close()
                }
                if (poly.isFilled) {
                    drawPath(path = path, color = strokeColor.copy(alpha = polyAlpha))
                } else {
                    drawPath(
                        path = path,
                        color = strokeColor.copy(alpha = polyAlpha),
                        style = Stroke(width = sw, join = StrokeJoin.Round)
                    )
                }
                if (isVolumetric) {
                    val highlightPath = Path().apply {
                        moveTo(poly.points[0].x * scale - chiselOff * 0.45f, poly.points[0].y * scale - chiselOff * 0.45f)
                        for (i in 1 until poly.points.size) {
                            lineTo(poly.points[i].x * scale - chiselOff * 0.45f, poly.points[i].y * scale - chiselOff * 0.45f)
                        }
                        close()
                    }
                    if (poly.isFilled) {
                        drawPath(path = highlightPath, color = highlightColor.copy(alpha = polyAlpha * 0.75f), style = Stroke(width = (sw * 0.3f).coerceAtLeast(0.8f), join = StrokeJoin.Round))
                    } else if (sw > 1.2f) {
                        drawPath(path = highlightPath, color = highlightColor.copy(alpha = polyAlpha * 0.70f), style = Stroke(width = (sw * 0.35f).coerceAtLeast(0.8f), join = StrokeJoin.Round))
                    }
                }
            }
            for (pathGeom in ornaments.paths) {
                if (pathGeom.points.size < 2) continue
                val pathAlpha = (pathGeom.alpha * alphaFactor).coerceIn(0f, 1f)
                val sw = (effectiveStrokeWidth * pathGeom.widthFactor).coerceAtLeast(1f)
                if (isVolumetric) {
                    val shadowPath = Path().apply {
                        moveTo(pathGeom.points[0].x * scale + chiselOff, pathGeom.points[0].y * scale + chiselOff)
                        for (i in 1 until pathGeom.points.size) {
                            lineTo(pathGeom.points[i].x * scale + chiselOff, pathGeom.points[i].y * scale + chiselOff)
                        }
                        if (pathGeom.isClosed) close()
                    }
                    if (pathGeom.isFilled) {
                        drawPath(path = shadowPath, color = shadowColor.copy(alpha = pathAlpha * 0.45f))
                    } else {
                        drawPath(path = shadowPath, color = shadowColor.copy(alpha = pathAlpha * 0.45f), style = Stroke(width = sw * 1.35f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                    }
                }
                val path = Path().apply {
                    moveTo(pathGeom.points[0].x * scale, pathGeom.points[0].y * scale)
                    for (i in 1 until pathGeom.points.size) {
                        lineTo(pathGeom.points[i].x * scale, pathGeom.points[i].y * scale)
                    }
                    if (pathGeom.isClosed) close()
                }
                if (pathGeom.isFilled) {
                    drawPath(path = path, color = strokeColor.copy(alpha = pathAlpha))
                } else {
                    drawPath(
                        path = path,
                        color = strokeColor.copy(alpha = pathAlpha),
                        style = Stroke(width = sw, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }
                if (isVolumetric) {
                    val highlightPath = Path().apply {
                        moveTo(pathGeom.points[0].x * scale - chiselOff * 0.45f, pathGeom.points[0].y * scale - chiselOff * 0.45f)
                        for (i in 1 until pathGeom.points.size) {
                            lineTo(pathGeom.points[i].x * scale - chiselOff * 0.45f, pathGeom.points[i].y * scale - chiselOff * 0.45f)
                        }
                        if (pathGeom.isClosed) close()
                    }
                    if (pathGeom.isFilled) {
                        drawPath(path = highlightPath, color = highlightColor.copy(alpha = pathAlpha * 0.75f))
                    } else if (sw > 1.2f) {
                        drawPath(path = highlightPath, color = highlightColor.copy(alpha = pathAlpha * 0.70f), style = Stroke(width = (sw * 0.35f).coerceAtLeast(0.8f), cap = StrokeCap.Round, join = StrokeJoin.Round))
                    }
                }
            }
        }

        // --- Phase 1: Sacred center aura, Ray Burst, and Celestial Orbits (0.00..0.25) ---
        val auraProgress = (currentProgress / 0.25f).coerceIn(0f, 1f)
        if (config.hasRayBurst && !config.isStencil && auraProgress > 0f) {
            val cx = 250f * scale
            val cy = 250f * scale
            val rays = 32
            val rayMaxR = 50f + 160f * auraProgress
            val rayAlpha = 0.20f * auraProgress * (if (currentProgress >= 1f) pulseAlpha else 1f)
            for (i in 0 until rays) {
                val angle = 2 * PI * i / rays
                drawLine(
                    color = strokeColor.copy(alpha = rayAlpha),
                    start = Offset((cx + 50f * scale * cos(angle)).toFloat(), (cy + 50f * scale * sin(angle)).toFloat()),
                    end = Offset((cx + rayMaxR * scale * cos(angle)).toFloat(), (cy + rayMaxR * scale * sin(angle)).toFloat()),
                    strokeWidth = 0.8f * scale
                )
            }
        }

        // Celestial Astrolabe background (Sacred Gold)
        if (config.style == SketchStyle.SACRED_GOLD && !config.isStencil && auraProgress > 0f) {
            val cx = 250f * scale
            val cy = 250f * scale
            val orbitAlpha = 0.30f * auraProgress
            val orbitRadii = listOf(46f, 92f, 142f, 236f)
            for (r in orbitRadii) {
                drawCircle(
                    color = strokeColor.copy(alpha = orbitAlpha),
                    radius = r * scale,
                    center = Offset(cx, cy),
                    style = Stroke(width = 0.75f * scale)
                )
            }
            for (i in 0 until 8) {
                val a = (PI / 4 * i).toFloat()
                drawLine(
                    color = strokeColor.copy(alpha = 0.18f * auraProgress),
                    start = Offset((cx + 38f * scale * cos(a)).toFloat(), (cy + 38f * scale * sin(a)).toFloat()),
                    end = Offset((cx + 236f * scale * cos(a)).toFloat(), (cy + 236f * scale * sin(a)).toFloat()),
                    strokeWidth = 0.6f * scale
                )
            }
        }

        // --- Phase 2: Runic Stave Strokes Carving (0.10..0.75) ---
        val strokeCount = stave.strokes.size.coerceAtLeast(1)
        val elemScale = config.elementScale.coerceIn(0.4f, 1.8f)
        for (i in stave.strokes.indices) {
            val stroke = stave.strokes[i]
            val basePts = if (Math.abs(elemScale - 1f) > 0.001f) {
                stroke.points.map { p ->
                    StrokePoint(
                        x = 250f + (p.x - 250f) * elemScale,
                        y = 250f + (p.y - 250f) * elemScale
                    )
                }
            } else {
                stroke.points
            }
            val pts = if (config.wobbleAmount > 0.01f) {
                applyComposeWobble(basePts, config.wobbleAmount, prng)
            } else {
                basePts
            }
            if (pts.size < 2) continue

            // Stagger each stroke's drawing window
            val strokeStart = 0.10f + 0.50f * (i.toFloat() / strokeCount.toFloat())
            val strokeWindow = (0.50f / strokeCount.toFloat()).coerceAtLeast(0.12f) * 1.35f
            val rawProg = ((currentProgress - strokeStart) / strokeWindow).coerceIn(0f, 1f)

            if (rawProg <= 0f) continue

            val (trimmedPts, tipPoint) = trimPoints(pts, rawProg)
            if (trimmedPts.size < 2) continue

            val strokeW = if (stroke.isHairlineGuide) {
                (effectiveStrokeWidth * 0.45f).coerceAtLeast(1f)
            } else {
                effectiveStrokeWidth
            }
            val strokeAlpha = if (stroke.isHairlineGuide) 0.55f else 1.0f

            if (config.style == SketchStyle.DOTWORK) {
                for (j in 0 until trimmedPts.size - 1) {
                    val p1 = trimmedPts[j]
                    val p2 = trimmedPts[j + 1]
                    val dist = sqrt((p2.x - p1.x) * (p2.x - p1.x) + (p2.y - p1.y) * (p2.y - p1.y))
                    val steps = (dist / 6.5f).toInt().coerceAtLeast(2)
                    for (s in 0..steps) {
                        val t = s.toFloat() / steps
                        val bx = (p1.x + t * (p2.x - p1.x)) * scale
                        val by = (p1.y + t * (p2.y - p1.y)) * scale
                        drawCircle(color = strokeColor.copy(alpha = strokeAlpha), radius = 2.0f * scale, center = Offset(bx, by))
                        if (prng.nextFloat() > 0.35f) {
                            val jx = bx + (prng.nextFloat() - 0.5f) * 6.5f * scale
                            val jy = by + (prng.nextFloat() - 0.5f) * 6.5f * scale
                            drawCircle(color = strokeColor.copy(alpha = 0.75f * strokeAlpha), radius = 1.1f * scale, center = Offset(jx, jy))
                        }
                    }
                }
            } else {
                val path = Path().apply {
                    moveTo(trimmedPts[0].x * scale, trimmedPts[0].y * scale)
                    for (k in 1 until trimmedPts.size) {
                        lineTo(trimmedPts[k].x * scale, trimmedPts[k].y * scale)
                    }
                }

                // Volumetric 3D Metallic / Chiseled Bevels (Specular highlights and ambient occlusion)
                if (config.hasVolumetricShading && !config.isStencil) {
                    val chiselOff = (strokeW * 0.38f * config.runeChiselDepth).coerceAtLeast(1.2f)
                    val shadowPath = Path().apply {
                        moveTo(trimmedPts[0].x * scale + chiselOff, trimmedPts[0].y * scale + chiselOff)
                        for (k in 1 until trimmedPts.size) {
                            lineTo(trimmedPts[k].x * scale + chiselOff, trimmedPts[k].y * scale + chiselOff)
                        }
                    }
                    drawPath(
                        path = shadowPath,
                        color = shadowColor.copy(alpha = 0.40f * strokeAlpha),
                        style = Stroke(
                            width = strokeW * 1.35f,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    val highlightPath = Path().apply {
                        moveTo(trimmedPts[0].x * scale - chiselOff * 0.5f, trimmedPts[0].y * scale - chiselOff * 0.5f)
                        for (k in 1 until trimmedPts.size) {
                            lineTo(trimmedPts[k].x * scale - chiselOff * 0.5f, trimmedPts[k].y * scale - chiselOff * 0.5f)
                        }
                    }
                    drawPath(
                        path = highlightPath,
                        color = highlightColor.copy(alpha = 0.55f * strokeAlpha),
                        style = Stroke(
                            width = (strokeW * 0.35f).coerceAtLeast(0.8f),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }

                // Glowing halo layer (if glow enabled or actively carving)
                if ((config.hasGlowEffect && !config.isStencil) || (rawProg in 0.05f..0.98f)) {
                    val activeGlowAlpha = if (rawProg < 1f) 0.50f else 0.28f * (if (currentProgress >= 1f) pulseAlpha else 1f)
                    drawPath(
                        path = path,
                        color = glowColor.copy(alpha = activeGlowAlpha * strokeAlpha),
                        style = Stroke(
                            width = strokeW * 2.3f,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }

                // Main stroke
                if (metallicBrush != null) {
                    drawPath(
                        path = path,
                        brush = metallicBrush,
                        alpha = strokeAlpha,
                        style = Stroke(
                            width = strokeW,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                } else {
                    drawPath(
                        path = path,
                        color = strokeColor.copy(alpha = strokeAlpha),
                        style = Stroke(
                            width = strokeW,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }

                if (config.style == SketchStyle.WOODCARVE) {
                    val shadowPath = Path().apply {
                        val off = strokeW * 0.45f
                        moveTo(trimmedPts[0].x * scale + off, trimmedPts[0].y * scale + off)
                        for (k in 1 until trimmedPts.size) {
                            lineTo(trimmedPts[k].x * scale + off, trimmedPts[k].y * scale + off)
                        }
                    }
                    drawPath(
                        path = shadowPath,
                        color = strokeColor.copy(alpha = 0.6f * strokeAlpha),
                        style = Stroke(
                            width = strokeW * 0.4f,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }

                if ((config.style == SketchStyle.BLACKWORK || config.style == SketchStyle.NORDIC_TATTOO) && stroke.isOuterPole && rawProg >= 0.95f) {
                    val dSize = 5.0f * scale
                    for (pt in listOf(trimmedPts.first(), trimmedPts.last())) {
                        val diamondPath = Path().apply {
                            moveTo(pt.x * scale, pt.y * scale - dSize)
                            lineTo(pt.x * scale + dSize, pt.y * scale)
                            lineTo(pt.x * scale, pt.y * scale + dSize)
                            lineTo(pt.x * scale - dSize, pt.y * scale)
                            close()
                        }
                        drawPath(diamondPath, color = strokeColor)
                    }
                }

                // Magical Chisel Spark on active stroke tip
                if (rawProg in 0.02f..0.99f && tipPoint != null) {
                    val tipX = tipPoint.x * scale
                    val tipY = tipPoint.y * scale
                    // Outer glow spark
                    drawCircle(
                        color = Color(0xFFFFE699).copy(alpha = 0.70f),
                        radius = 8f * scale,
                        center = Offset(tipX, tipY)
                    )
                    // Core fiery point
                    drawCircle(
                        color = Color(0xFFFFFFFF),
                        radius = 3.5f * scale,
                        center = Offset(tipX, tipY)
                    )
                    // Radiating mini sparks
                    for (sp in 0 until 3) {
                        val sa = (prng.nextFloat() * 2 * PI).toFloat()
                        val sr = (5f + prng.nextFloat() * 9f) * scale
                        drawCircle(
                            color = Color(0xFFFFD54F).copy(alpha = 0.85f),
                            radius = 1.4f * scale,
                            center = Offset(tipX + sr * cos(sa), tipY + sr * sin(sa))
                        )
                    }
                }
            }

            // --- Phase 3: Branch notches & Finials (0.65..0.85) ---
            val finialProgress = ((currentProgress - 0.65f) / 0.20f).coerceIn(0f, 1f)
            if (finialProgress > 0f) {
                // Branch Notches - ONLY on central stems to prevent clutter
                if (config.hasBranchNotches && stroke.isStem && pts.size >= 2) {
                    val notches = OrnamentGeometry.generateStrokeNotches(pts.first(), pts.last())
                    val nw = effectiveStrokeWidth * 0.75f
                    for (notch in notches) {
                        drawLine(
                            color = strokeColor.copy(alpha = finialProgress),
                            start = Offset(notch.x1 * scale, notch.y1 * scale),
                            end = Offset(notch.x2 * scale, notch.y2 * scale),
                            strokeWidth = nw,
                            cap = StrokeCap.Round
                        )
                    }
                }

                // Finials - ONLY on outer poles
                if (config.finialType != FinialType.DEFAULT && stroke.isOuterPole && pts.size >= 2) {
                    val pLast = pts.last()
                    val pPrev = pts[pts.size - 2]
                    val finials = OrnamentGeometry.generateFinial(pLast, pLast.x - pPrev.x, pLast.y - pPrev.y, config.finialType)
                    drawOrnaments(finials, alphaFactor = finialProgress)
                } else if (config.style == SketchStyle.ORNAMENTAL && stroke.isOuterPole) {
                    drawCircle(color = strokeColor.copy(alpha = finialProgress), radius = 3.2f * scale, center = Offset(pts.first().x * scale, pts.first().y * scale))
                    drawCircle(color = strokeColor.copy(alpha = finialProgress), radius = 3.2f * scale, center = Offset(pts.last().x * scale, pts.last().y * scale))
                }
            }
        }

        // --- Phase 4: Decorative Frame & Elder Futhark Rune Ring (0.75..0.95) ---
        val frameProgress = ((currentProgress - 0.72f) / 0.22f).coerceIn(0f, 1f)
        val effectiveFrame = if (!config.hasFrameCircle) FrameStyle.NONE else config.frameStyle
        if (effectiveFrame != FrameStyle.NONE && frameProgress > 0f) {
            val frameOrnaments = OrnamentGeometry.generateFrame(effectiveFrame, effectiveStrokeWidth)
            drawOrnaments(frameOrnaments, alphaFactor = frameProgress)
        }

        // Elder Futhark Rune Ring (concentric sacred boundary with ancient runes)
        val runeringProgress = ((currentProgress - 0.78f) / 0.22f).coerceIn(0f, 1f)
        if (config.hasRunering && !config.isStencil && runeringProgress > 0f) {
            val cx = 250f * scale
            val cy = 250f * scale
            val rInner = 218f * scale
            val rOuter = 244f * scale
            val rText = 231f * scale
            val ringAlpha = 0.65f * runeringProgress

            if (isVolumetric) {
                drawCircle(
                    color = shadowColor.copy(alpha = 0.40f * runeringProgress),
                    radius = rInner,
                    center = Offset(cx + chiselOff, cy + chiselOff),
                    style = Stroke(width = (effectiveStrokeWidth * 0.55f).coerceAtLeast(1.2f))
                )
                drawCircle(
                    color = shadowColor.copy(alpha = 0.40f * runeringProgress),
                    radius = rOuter,
                    center = Offset(cx + chiselOff, cy + chiselOff),
                    style = Stroke(width = (effectiveStrokeWidth * 0.55f).coerceAtLeast(1.2f))
                )
            }

            drawCircle(
                color = strokeColor.copy(alpha = ringAlpha),
                radius = rInner,
                center = Offset(cx, cy),
                style = Stroke(width = (effectiveStrokeWidth * 0.45f).coerceAtLeast(1f))
            )
            drawCircle(
                color = strokeColor.copy(alpha = ringAlpha),
                radius = rOuter,
                center = Offset(cx, cy),
                style = Stroke(width = (effectiveStrokeWidth * 0.45f).coerceAtLeast(1f))
            )

            if (isVolumetric) {
                drawCircle(
                    color = highlightColor.copy(alpha = 0.65f * runeringProgress),
                    radius = rInner,
                    center = Offset(cx - chiselOff * 0.45f, cy - chiselOff * 0.45f),
                    style = Stroke(width = (effectiveStrokeWidth * 0.22f).coerceAtLeast(0.6f))
                )
                drawCircle(
                    color = highlightColor.copy(alpha = 0.65f * runeringProgress),
                    radius = rOuter,
                    center = Offset(cx - chiselOff * 0.45f, cy - chiselOff * 0.45f),
                    style = Stroke(width = (effectiveStrokeWidth * 0.22f).coerceAtLeast(0.6f))
                )
            }

            val totalRunes = ELDER_FUTHARK_RUNES.size
            val visibleRunes = if (currentProgress >= 1f) totalRunes else (runeringProgress * totalRunes).toInt().coerceIn(0, totalRunes)

            drawIntoCanvas { composeCanvas ->
                val shadowTextPaint = if (isVolumetric) {
                    try {
                        AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
                            color = android.graphics.Color.parseColor(config.effectiveTheme.shadowHex)
                            textSize = 14f * scale
                            textAlign = AndroidPaint.Align.CENTER
                            typeface = AndroidTypeface.SERIF
                            isFakeBoldText = true
                            alpha = (140 * runeringProgress).toInt().coerceIn(0, 255)
                        }
                    } catch (_: Exception) { null }
                } else null

                val highlightTextPaint = if (isVolumetric) {
                    try {
                        AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
                            color = android.graphics.Color.parseColor(config.effectiveTheme.highlightHex)
                            textSize = 14f * scale
                            textAlign = AndroidPaint.Align.CENTER
                            typeface = AndroidTypeface.SERIF
                            isFakeBoldText = true
                            alpha = (180 * runeringProgress).toInt().coerceIn(0, 255)
                        }
                    } catch (_: Exception) { null }
                } else null

                val textPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
                    color = if (overrideColor != null) {
                        android.graphics.Color.rgb(
                            (overrideColor.red * 255).toInt(),
                            (overrideColor.green * 255).toInt(),
                            (overrideColor.blue * 255).toInt()
                        )
                    } else {
                        android.graphics.Color.parseColor(config.effectiveTheme.strokeHex)
                    }
                    textSize = 14f * scale
                    textAlign = AndroidPaint.Align.CENTER
                    typeface = AndroidTypeface.SERIF
                    isFakeBoldText = true
                    alpha = (235 * runeringProgress).toInt().coerceIn(0, 255)
                }

                for (rIdx in 0 until visibleRunes) {
                    val deg = rIdx * (360f / totalRunes)
                    val rune = ELDER_FUTHARK_RUNES[rIdx]
                    if (shadowTextPaint != null) {
                        composeCanvas.nativeCanvas.save()
                        composeCanvas.nativeCanvas.rotate(deg, cx, cy)
                        composeCanvas.nativeCanvas.drawText(rune, cx + chiselOff * 0.7f, cy - rText + 5.5f * scale + chiselOff * 0.7f, shadowTextPaint)
                        composeCanvas.nativeCanvas.restore()
                    }
                    composeCanvas.nativeCanvas.save()
                    composeCanvas.nativeCanvas.rotate(deg, cx, cy)
                    composeCanvas.nativeCanvas.drawText(rune, cx, cy - rText + 5.5f * scale, textPaint)
                    composeCanvas.nativeCanvas.restore()
                    if (highlightTextPaint != null) {
                        composeCanvas.nativeCanvas.save()
                        composeCanvas.nativeCanvas.rotate(deg, cx, cy)
                        composeCanvas.nativeCanvas.drawText(rune, cx - chiselOff * 0.35f, cy - rText + 5.5f * scale - chiselOff * 0.35f, highlightTextPaint)
                        composeCanvas.nativeCanvas.restore()
                    }
                }
            }
        }

        // --- Phase 4.5: Central Sacred Emblem (0.78..0.98) ---
        val emblemProgress = ((currentProgress - 0.76f) / 0.22f).coerceIn(0f, 1f)
        if (config.centerEmblem != CenterEmblem.NONE && emblemProgress > 0f) {
            val centerOrnaments = OrnamentGeometry.generateCenterEmblem(config.centerEmblem, effectiveStrokeWidth, config.elementScale)
            drawOrnaments(centerOrnaments, alphaFactor = emblemProgress)
        }

        // --- Phase 5: Corner Accents (0.88..1.00) ---
        val cornerProgress = ((currentProgress - 0.88f) / 0.12f).coerceIn(0f, 1f)
        if (config.hasSymmetryAccents && !config.isStencil && config.cornerStyle != CornerStyle.NONE && cornerProgress > 0f) {
            val cornerOrnaments = OrnamentGeometry.generateCorners(config.cornerStyle, effectiveStrokeWidth)
            drawOrnaments(cornerOrnaments, alphaFactor = cornerProgress)
        }

        // Floating golden embers (Golden Ember theme)
        if (config.effectiveTheme == CanvasTheme.GOLDEN_EMBER && !config.isStencil && currentProgress > 0.5f) {
            val emberAlpha = ((currentProgress - 0.5f) / 0.5f) * 0.7f * pulseAlpha
            for (i in 0 until 24) {
                val ex = (50f + prng.nextFloat() * 400f) * scale
                val ey = (50f + prng.nextFloat() * 400f) * scale
                drawCircle(
                    color = Color(0xFFFFD700).copy(alpha = emberAlpha * (prng.nextFloat() * 0.6f + 0.4f)),
                    radius = (prng.nextFloat() * 2f + 1f) * scale,
                    center = Offset(ex, ey)
                )
            }
        }
    }
}

/**
 * Trims a polyline proportionally according to progress [0.0..1.0],
 * returning the trimmed points and the current tip position.
 */
private fun trimPoints(pts: List<StrokePoint>, progress: Float): Pair<List<StrokePoint>, StrokePoint?> {
    if (pts.size < 2) return Pair(pts, pts.lastOrNull())
    if (progress >= 0.999f) return Pair(pts, null)

    var totalLen = 0f
    val segmentLengths = mutableListOf<Float>()
    for (i in 0 until pts.size - 1) {
        val dx = pts[i + 1].x - pts[i].x
        val dy = pts[i + 1].y - pts[i].y
        val d = sqrt(dx * dx + dy * dy)
        segmentLengths.add(d)
        totalLen += d
    }

    if (totalLen <= 0.001f) return Pair(pts, pts.first())

    val targetLen = totalLen * progress.coerceIn(0f, 1f)
    var accum = 0f
    val result = mutableListOf<StrokePoint>()
    result.add(pts[0])
    var tip: StrokePoint = pts[0]

    for (i in 0 until pts.size - 1) {
        val segLen = segmentLengths[i]
        if (accum + segLen <= targetLen) {
            result.add(pts[i + 1])
            accum += segLen
            tip = pts[i + 1]
        } else {
            val remain = targetLen - accum
            val frac = (remain / segLen).coerceIn(0f, 1f)
            val ix = pts[i].x + frac * (pts[i + 1].x - pts[i].x)
            val iy = pts[i].y + frac * (pts[i + 1].y - pts[i].y)
            tip = StrokePoint(ix, iy)
            result.add(tip)
            break
        }
    }

    return Pair(result, tip)
}

private fun applyComposeWobble(points: List<StrokePoint>, wobble: Float, prng: Random): List<StrokePoint> {
    val result = mutableListOf<StrokePoint>()
    for (i in points.indices) {
        val pt = points[i]
        val factor = if (i == 0 || i == points.size - 1) 0.25f else 1.0f
        val jx = pt.x + (prng.nextFloat() - 0.5f) * 6f * wobble * factor
        val jy = pt.y + (prng.nextFloat() - 0.5f) * 6f * wobble * factor
        result.add(StrokePoint(jx, jy))
    }
    return result
}
