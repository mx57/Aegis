package com.example.engine

import com.example.data.model.StrokePoint
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class LineSegmentGeom(
    val x1: Float,
    val y1: Float,
    val x2: Float,
    val y2: Float,
    val widthFactor: Float = 1f,
    val alpha: Float = 1f
)

data class CircleGeom(
    val cx: Float,
    val cy: Float,
    val radius: Float,
    val isFilled: Boolean = false,
    val widthFactor: Float = 1f,
    val alpha: Float = 1f
)

data class PolygonGeom(
    val points: List<StrokePoint>,
    val isFilled: Boolean = true,
    val widthFactor: Float = 1f,
    val alpha: Float = 1f
)

data class PathGeom(
    val points: List<StrokePoint>,
    val isClosed: Boolean = false,
    val isFilled: Boolean = false,
    val widthFactor: Float = 1f,
    val alpha: Float = 1f
)

data class GeneratedOrnaments(
    val lines: List<LineSegmentGeom> = emptyList(),
    val circles: List<CircleGeom> = emptyList(),
    val polygons: List<PolygonGeom> = emptyList(),
    val paths: List<PathGeom> = emptyList()
)

object OrnamentGeometry {

    const val CANVAS_SIZE = 500f
    const val CENTER_X = 250f
    const val CENTER_Y = 250f

    /**
     * Generates frame elements based on FrameStyle.
     */
    fun generateFrame(style: FrameStyle, strokeWidth: Float): GeneratedOrnaments {
        val lines = mutableListOf<LineSegmentGeom>()
        val circles = mutableListOf<CircleGeom>()
        val polygons = mutableListOf<PolygonGeom>()
        val paths = mutableListOf<PathGeom>()

        val cx = CENTER_X
        val cy = CENTER_Y

        when (style) {
            FrameStyle.NONE -> {
                // No frame elements
            }
            FrameStyle.SOLAR_CIRCLE -> {
                val r1 = 232f
                val r2 = 216f
                val r3 = 200f
                circles.add(CircleGeom(cx, cy, r1, widthFactor = 1.2f))
                circles.add(CircleGeom(cx, cy, r2, widthFactor = 0.8f))
                circles.add(CircleGeom(cx, cy, r3, widthFactor = 0.6f, alpha = 0.6f))

                // 24 Solar rays / Futhark ticks
                val ticks = 24
                for (i in 0 until ticks) {
                    val angle = (2 * PI * i / ticks).toFloat()
                    val cosA = cos(angle)
                    val sinA = sin(angle)
                    val isCardinal = i % 6 == 0
                    val tickR1 = if (isCardinal) r2 - 4f else r2
                    val tickR2 = if (isCardinal) r1 + 8f else r1
                    lines.add(LineSegmentGeom(cx + tickR1 * cosA, cy + tickR1 * sinA, cx + tickR2 * cosA, cy + tickR2 * sinA, widthFactor = if (isCardinal) 1.5f else 0.8f))

                    // Cardinal pointer arrowheads
                    if (isCardinal) {
                        val tipX = cx + (r1 + 14f) * cosA
                        val tipY = cy + (r1 + 14f) * sinA
                        val perpX = -sinA * 6f
                        val perpY = cosA * 6f
                        val baseX = cx + (r1 + 5f) * cosA
                        val baseY = cy + (r1 + 5f) * sinA
                        polygons.add(
                            PolygonGeom(
                                listOf(
                                    StrokePoint(tipX, tipY),
                                    StrokePoint(baseX + perpX, baseY + perpY),
                                    StrokePoint(baseX - perpX, baseY - perpY)
                                )
                            )
                        )
                    }
                }

                // Intermediate sacred dots in the inner ring
                for (i in 0 until 8) {
                    val angle = (2 * PI * (i + 0.5f) / 8).toFloat()
                    circles.add(CircleGeom(cx + 208f * cos(angle), cy + 208f * sin(angle), 2.2f, isFilled = true))
                }
            }

            FrameStyle.NORDIC_BRAID -> {
                val rOut = 236f
                val rIn = 212f
                circles.add(CircleGeom(cx, cy, rOut, widthFactor = 0.9f))
                circles.add(CircleGeom(cx, cy, rIn, widthFactor = 0.9f))

                // Interwoven double-chain Viking braid
                val count = 36
                val rMid = (rOut + rIn) / 2f
                val amp = (rOut - rIn) / 2.5f

                val wave1 = mutableListOf<StrokePoint>()
                val wave2 = mutableListOf<StrokePoint>()

                for (i in 0..count) {
                    val angle = (2 * PI * i / count).toFloat()
                    val phase1 = sin(angle * 9).toFloat()
                    val phase2 = sin(angle * 9 + PI).toFloat()

                    val curR1 = rMid + phase1 * amp
                    val curR2 = rMid + phase2 * amp

                    wave1.add(StrokePoint(cx + curR1 * cos(angle), cy + curR1 * sin(angle)))
                    wave2.add(StrokePoint(cx + curR2 * cos(angle), cy + curR2 * sin(angle)))
                }
                paths.add(PathGeom(wave1, widthFactor = 0.9f))
                paths.add(PathGeom(wave2, widthFactor = 0.9f))

                // Crossing braid node studs
                for (i in 0 until 18) {
                    val angle = (2 * PI * i / 18).toFloat()
                    circles.add(CircleGeom(cx + rMid * cos(angle), cy + rMid * sin(angle), 1.8f, isFilled = true))
                }
            }

            FrameStyle.RUNIC_SERPENT -> {
                // Jelling Serpent circular frame with scale notches, head & knot tail
                val r = 224f
                val rWidth = 14f
                circles.add(CircleGeom(cx, cy, r - rWidth / 2f, widthFactor = 0.8f))
                circles.add(CircleGeom(cx, cy, r + rWidth / 2f, widthFactor = 0.8f))

                // 48 scale notches along serpent body
                val scales = 44
                for (i in 0 until scales) {
                    val angle = (2 * PI * i / scales).toFloat()
                    if (angle in 0.5f..1.1f) continue // space for serpent head

                    val cosA = cos(angle)
                    val sinA = sin(angle)
                    // Angled herringbone scale cuts
                    val p1 = StrokePoint(cx + (r - rWidth / 2f) * cosA - sinA * 3f, cy + (r - rWidth / 2f) * sinA + cosA * 3f)
                    val p2 = StrokePoint(cx + (r + rWidth / 2f) * cosA + sinA * 3f, cy + (r + rWidth / 2f) * sinA - cosA * 3f)
                    lines.add(LineSegmentGeom(p1.x, p1.y, p2.x, p2.y, widthFactor = 0.7f))
                }

                // Serpent Head at angle ~ 0.8 radians (~45 deg)
                val headAngle = 0.8f
                val hx = cx + r * cos(headAngle)
                val hy = cy + r * sin(headAngle)
                val tangX = -sin(headAngle)
                val tangY = cos(headAngle)
                val normX = cos(headAngle)
                val normY = sin(headAngle)

                // Snout & jaws
                val jawUpper = StrokePoint(hx + tangX * 22f + normX * 8f, hy + tangY * 22f + normY * 8f)
                val jawLower = StrokePoint(hx + tangX * 18f - normX * 6f, hy + tangY * 18f - normY * 6f)
                val mouthThroat = StrokePoint(hx + tangX * 6f, hy + tangY * 6f)
                val hornTip = StrokePoint(hx - tangX * 10f + normX * 14f, hy - tangY * 10f + normY * 14f)

                lines.add(LineSegmentGeom(mouthThroat.x, mouthThroat.y, jawUpper.x, jawUpper.y, widthFactor = 1.4f))
                lines.add(LineSegmentGeom(mouthThroat.x, mouthThroat.y, jawLower.x, jawLower.y, widthFactor = 1.4f))
                lines.add(LineSegmentGeom(hx, hy, hornTip.x, hornTip.y, widthFactor = 1.2f))
                // Serpent Eye
                circles.add(CircleGeom(hx + tangX * 10f + normX * 4f, hy + tangY * 10f + normY * 4f, 3.2f, isFilled = true))

                // Serpent Tail Knot at angle ~ 5.3 radians (~300 deg)
                val tailAngle = 5.3f
                val tx = cx + r * cos(tailAngle)
                val ty = cy + r * sin(tailAngle)
                circles.add(CircleGeom(tx, ty, 8f, isFilled = false, widthFactor = 1.2f))
                circles.add(CircleGeom(tx - 6f, ty + 6f, 6f, isFilled = false, widthFactor = 1.0f))
            }

            FrameStyle.COMPASS_RAYS -> {
                // Vegvisir 8-ray compass frame
                val r1 = 226f
                val r2 = 214f
                circles.add(CircleGeom(cx, cy, r1, widthFactor = 1.1f))
                circles.add(CircleGeom(cx, cy, r2, widthFactor = 0.8f))

                // 8 Primary Compass Points
                for (i in 0 until 8) {
                    val angle = (2 * PI * i / 8).toFloat()
                    val cosA = cos(angle)
                    val sinA = sin(angle)
                    val perpX = -sinA
                    val perpY = cosA

                    // Ray line
                    lines.add(LineSegmentGeom(cx + r2 * cosA, cy + r2 * sinA, cx + (r1 + 18f) * cosA, cy + (r1 + 18f) * sinA, widthFactor = 1.6f))
                    // Compass crossbar
                    val barR = r1 + 8f
                    lines.add(LineSegmentGeom(
                        cx + barR * cosA - perpX * 8f, cy + barR * sinA - perpY * 8f,
                        cx + barR * cosA + perpX * 8f, cy + barR * sinA + perpY * 8f,
                        widthFactor = 1.2f
                    ))
                    // Spearhead tip
                    val tipX = cx + (r1 + 24f) * cosA
                    val tipY = cy + (r1 + 24f) * sinA
                    polygons.add(
                        PolygonGeom(
                            listOf(
                                StrokePoint(tipX, tipY),
                                StrokePoint(cx + (r1 + 16f) * cosA + perpX * 5f, cy + (r1 + 16f) * sinA + perpY * 5f),
                                StrokePoint(cx + (r1 + 16f) * cosA - perpX * 5f, cy + (r1 + 16f) * sinA - perpY * 5f)
                            )
                        )
                    )
                }

                // 16 Intermediate sub-ticks
                for (i in 0 until 16) {
                    if (i % 2 == 1) {
                        val angle = (2 * PI * i / 16).toFloat()
                        lines.add(LineSegmentGeom(cx + r2 * cos(angle), cy + r2 * sin(angle), cx + (r1 + 6f) * cos(angle), cy + (r1 + 6f) * sin(angle), widthFactor = 0.8f))
                    }
                }
            }

            FrameStyle.CELESTIAL_ASTROLABE -> {
                // Sacred Astrolabe: Concentric harmonic orbits + 72 degree tick marks + star nodes
                val r1 = 238f
                val r2 = 224f
                val r3 = 210f
                val r4 = 196f
                circles.add(CircleGeom(cx, cy, r1, widthFactor = 1.0f))
                circles.add(CircleGeom(cx, cy, r2, widthFactor = 0.6f, alpha = 0.7f))
                circles.add(CircleGeom(cx, cy, r3, widthFactor = 0.5f, alpha = 0.5f))
                circles.add(CircleGeom(cx, cy, r4, widthFactor = 0.8f))

                // 72 astrolabe degree marks
                val ticks = 72
                for (i in 0 until ticks) {
                    val angle = (2 * PI * i / ticks).toFloat()
                    val cosA = cos(angle)
                    val sinA = sin(angle)
                    val isCardinal = i % 18 == 0
                    val isMajor = i % 6 == 0
                    val tLen = if (isCardinal) 14f else if (isMajor) 9f else 5f
                    val wf = if (isCardinal) 1.5f else if (isMajor) 0.9f else 0.5f
                    lines.add(LineSegmentGeom(cx + (r2 - tLen) * cosA, cy + (r2 - tLen) * sinA, cx + r2 * cosA, cy + r2 * sinA, widthFactor = wf))

                    // Cardinal 8-pointed star markers
                    if (isCardinal) {
                        val starR = r1 + 8f
                        circles.add(CircleGeom(cx + starR * cosA, cy + starR * sinA, 2.5f, isFilled = true))
                        circles.add(CircleGeom(cx + starR * cosA, cy + starR * sinA, 5.0f, isFilled = false, widthFactor = 0.7f))
                    }
                }

                // 8 sacred planetary orbit dots
                for (i in 0 until 8) {
                    val a = (2 * PI * (i + 0.5f) / 8).toFloat()
                    circles.add(CircleGeom(cx + ((r3 + r4) / 2f) * cos(a), cy + ((r3 + r4) / 2f) * sin(a), 2.2f, isFilled = true))
                }
            }

            FrameStyle.SACRED_OCTAGON -> {
                // 8-pointed star / dual squares + inner octagon
                val r = 230f
                val rInner = 214f
                circles.add(CircleGeom(cx, cy, rInner, widthFactor = 0.8f, alpha = 0.7f))

                // Square 1 (0, 90, 180, 270)
                val sq1 = mutableListOf<StrokePoint>()
                for (i in 0..4) {
                    val a = (PI / 2 * i).toFloat()
                    sq1.add(StrokePoint(cx + r * cos(a), cy + r * sin(a)))
                }
                paths.add(PathGeom(sq1, isClosed = true, widthFactor = 1.1f))

                // Square 2 (45, 135, 225, 315)
                val sq2 = mutableListOf<StrokePoint>()
                for (i in 0..4) {
                    val a = (PI / 4 + PI / 2 * i).toFloat()
                    sq2.add(StrokePoint(cx + r * cos(a), cy + r * sin(a)))
                }
                paths.add(PathGeom(sq2, isClosed = true, widthFactor = 1.1f))

                // Sacred vertex nodes
                for (i in 0 until 8) {
                    val a = (2 * PI * i / 8).toFloat()
                    circles.add(CircleGeom(cx + r * cos(a), cy + r * sin(a), 3.5f, isFilled = true))
                    circles.add(CircleGeom(cx + r * cos(a), cy + r * sin(a), 6.5f, isFilled = false, widthFactor = 0.8f))
                }
            }
        }

        return GeneratedOrnaments(lines, circles, polygons, paths)
    }

    /**
     * Generates central emblem elements based on CenterEmblem.
     */
    fun generateCenterEmblem(emblem: CenterEmblem, strokeWidth: Float): GeneratedOrnaments {
        val lines = mutableListOf<LineSegmentGeom>()
        val circles = mutableListOf<CircleGeom>()
        val polygons = mutableListOf<PolygonGeom>()
        val paths = mutableListOf<PathGeom>()

        val cx = CENTER_X
        val cy = CENTER_Y

        when (emblem) {
            CenterEmblem.NONE -> {}

            CenterEmblem.VALKNUT -> {
                // Three interlocking triangles of Odin
                val r = 24f
                val offsets = listOf(
                    Pair(0f, -8f),
                    Pair(-7f, 5f),
                    Pair(7f, 5f)
                )

                for ((ox, oy) in offsets) {
                    val triPoints = mutableListOf<StrokePoint>()
                    for (i in 0..3) {
                        val angle = (-PI / 2 + 2 * PI * i / 3).toFloat()
                        triPoints.add(StrokePoint(cx + ox + r * cos(angle), cy + oy + r * sin(angle)))
                    }
                    paths.add(PathGeom(triPoints, isClosed = true, widthFactor = 1.2f))
                }
                circles.add(CircleGeom(cx, cy, 2.0f, isFilled = true))
            }

            CenterEmblem.TRIQUETRA -> {
                // Sacred 3-leaf Celtic knot + center circle
                circles.add(CircleGeom(cx, cy, 14f, isFilled = false, widthFactor = 1.2f))
                for (i in 0 until 3) {
                    val angle = (-PI / 2 + 2 * PI * i / 3).toFloat()
                    val leafX = cx + 22f * cos(angle)
                    val leafY = cy + 22f * sin(angle)
                    lines.add(LineSegmentGeom(cx, cy, leafX, leafY, widthFactor = 1.5f))
                    circles.add(CircleGeom(leafX, leafY, 4f, isFilled = true))
                }
                circles.add(CircleGeom(cx, cy, 3.5f, isFilled = true))
            }

            CenterEmblem.SOLAR_CROSS -> {
                // Solar Cross / Sun wheel
                val r = 18f
                circles.add(CircleGeom(cx, cy, r, isFilled = false, widthFactor = 1.4f))
                lines.add(LineSegmentGeom(cx - r - 8f, cy, cx + r + 8f, cy, widthFactor = 1.4f))
                lines.add(LineSegmentGeom(cx, cy - r - 8f, cx, cy + r + 8f, widthFactor = 1.4f))
                // 4 solar quadrant dots
                val dotDist = 9f
                circles.add(CircleGeom(cx - dotDist, cy - dotDist, 1.8f, isFilled = true))
                circles.add(CircleGeom(cx + dotDist, cy - dotDist, 1.8f, isFilled = true))
                circles.add(CircleGeom(cx - dotDist, cy + dotDist, 1.8f, isFilled = true))
                circles.add(CircleGeom(cx + dotDist, cy + dotDist, 1.8f, isFilled = true))
            }

            CenterEmblem.INGUZ_DIAMOND -> {
                // Inguz Rhombus + center dot & cross-ticks
                val hw = 22f
                val hh = 28f
                val outerRhombus = listOf(
                    StrokePoint(cx, cy - hh),
                    StrokePoint(cx + hw, cy),
                    StrokePoint(cx, cy + hh),
                    StrokePoint(cx - hw, cy)
                )
                paths.add(PathGeom(outerRhombus, isClosed = true, widthFactor = 1.4f))

                val innerRhombus = listOf(
                    StrokePoint(cx, cy - hh * 0.6f),
                    StrokePoint(cx + hw * 0.6f, cy),
                    StrokePoint(cx, cy + hh * 0.6f),
                    StrokePoint(cx - hw * 0.6f, cy)
                )
                paths.add(PathGeom(innerRhombus, isClosed = true, widthFactor = 0.8f))

                // Cardinal tick accents
                lines.add(LineSegmentGeom(cx, cy - hh, cx, cy - hh - 8f, widthFactor = 1.2f))
                lines.add(LineSegmentGeom(cx, cy + hh, cx, cy + hh + 8f, widthFactor = 1.2f))
                lines.add(LineSegmentGeom(cx - hw, cy, cx - hw - 8f, cy, widthFactor = 1.2f))
                lines.add(LineSegmentGeom(cx + hw, cy, cx + hw + 8f, cy, widthFactor = 1.2f))
                circles.add(CircleGeom(cx, cy, 2.5f, isFilled = true))
            }

            CenterEmblem.AEGISHJALMUR_CORE -> {
                // Central 8-spoke sacred hub of the Helm of Awe
                circles.add(CircleGeom(cx, cy, 14f, isFilled = false, widthFactor = 1.4f))
                circles.add(CircleGeom(cx, cy, 6f, isFilled = false, widthFactor = 0.8f))
                circles.add(CircleGeom(cx, cy, 2.2f, isFilled = true))

                for (i in 0 until 8) {
                    val a = (2 * PI * i / 8).toFloat()
                    val cosA = cos(a)
                    val sinA = sin(a)
                    lines.add(LineSegmentGeom(cx + 14f * cosA, cy + 14f * sinA, cx + 24f * cosA, cy + 24f * sinA, widthFactor = 1.2f))
                    circles.add(CircleGeom(cx + 25f * cosA, cy + 25f * sinA, 2.0f, isFilled = true))
                }
            }
        }

        return GeneratedOrnaments(lines, circles, polygons, paths)
    }

    /**
     * Generates terminal finials for a given endpoint and direction vector.
     */
    fun generateFinial(pt: StrokePoint, dirX: Float, dirY: Float, finial: FinialType): GeneratedOrnaments {
        val lines = mutableListOf<LineSegmentGeom>()
        val circles = mutableListOf<CircleGeom>()
        val polygons = mutableListOf<PolygonGeom>()
        val paths = mutableListOf<PathGeom>()

        val len = sqrt(dirX * dirX + dirY * dirY)
        val nx = if (len > 0.001f) dirX / len else 0f
        val ny = if (len > 0.001f) dirY / len else -1f
        val px = -ny
        val py = nx

        val x = pt.x
        val y = pt.y

        when (finial) {
            FinialType.DEFAULT -> {
                circles.add(CircleGeom(x, y, 3.0f, isFilled = true))
            }

            FinialType.TRIDENT -> {
                // Ægishjálmur 3-prong crown + crossbar
                val barLen = 9f
                lines.add(LineSegmentGeom(x - px * barLen, y - py * barLen, x + px * barLen, y + py * barLen, widthFactor = 1.2f))

                // Center tine
                val tipCenterX = x + nx * 16f
                val tipCenterY = y + ny * 16f
                lines.add(LineSegmentGeom(x, y, tipCenterX, tipCenterY, widthFactor = 1.4f))
                circles.add(CircleGeom(tipCenterX, tipCenterY, 1.8f, isFilled = true))

                // Left prong
                val leftTipX = x + nx * 13f + px * 10f
                val leftTipY = y + ny * 13f + py * 10f
                lines.add(LineSegmentGeom(x, y, leftTipX, leftTipY, widthFactor = 1.2f))
                circles.add(CircleGeom(leftTipX, leftTipY, 1.6f, isFilled = true))

                // Right prong
                val rightTipX = x + nx * 13f - px * 10f
                val rightTipY = y + ny * 13f - py * 10f
                lines.add(LineSegmentGeom(x, y, rightTipX, rightTipY, widthFactor = 1.2f))
                circles.add(CircleGeom(rightTipX, rightTipY, 1.6f, isFilled = true))
            }

            FinialType.ARROWS -> {
                // Spearhead of Tyr
                val spearLen = 16f
                val tipX = x + nx * spearLen
                val tipY = y + ny * spearLen
                val barbLeftX = x + nx * 7f + px * 7f
                val barbLeftY = y + ny * 7f + py * 7f
                val barbRightX = x + nx * 7f - px * 7f
                val barbRightY = y + ny * 7f - py * 7f

                polygons.add(
                    PolygonGeom(
                        listOf(
                            StrokePoint(tipX, tipY),
                            StrokePoint(barbLeftX, barbLeftY),
                            StrokePoint(x + nx * 4f, y + ny * 4f),
                            StrokePoint(barbRightX, barbRightY)
                        )
                    )
                )
            }

            FinialType.CIRCLES_DOTS -> {
                // Sacred ring with center dot + neck notch
                val ringDist = 8f
                val rx = x + nx * ringDist
                val ry = y + ny * ringDist
                circles.add(CircleGeom(rx, ry, 5.5f, isFilled = false, widthFactor = 1.2f))
                circles.add(CircleGeom(rx, ry, 2.0f, isFilled = true))
                lines.add(LineSegmentGeom(x - px * 6f, y - py * 6f, x + px * 6f, y + py * 6f, widthFactor = 1.0f))
            }

            FinialType.CROSSBARS -> {
                // Double / Triple crossbars
                val bars = listOf(Pair(3f, 8f), Pair(8f, 6f), Pair(13f, 4f))
                for ((dist, halfW) in bars) {
                    val bx = x + nx * dist
                    val by = y + ny * dist
                    lines.add(LineSegmentGeom(bx - px * halfW, by - py * halfW, bx + px * halfW, by + py * halfW, widthFactor = 1.1f))
                }
            }

            FinialType.SPIRALS -> {
                // Twin curled horns
                val h1 = mutableListOf<StrokePoint>()
                val h2 = mutableListOf<StrokePoint>()
                for (s in 0..10) {
                    val t = s / 10f
                    val angle = t * PI.toFloat() * 1.3f
                    val r = t * 10f
                    h1.add(StrokePoint(x + nx * (t * 12f) + px * (r * cos(angle)), y + ny * (t * 12f) + py * (r * cos(angle))))
                    h2.add(StrokePoint(x + nx * (t * 12f) - px * (r * cos(angle)), y + ny * (t * 12f) - py * (r * cos(angle))))
                }
                paths.add(PathGeom(h1, widthFactor = 1.1f))
                paths.add(PathGeom(h2, widthFactor = 1.1f))
            }
        }

        return GeneratedOrnaments(lines, circles, polygons, paths)
    }

    /**
     * Generates corner accents for the canvas corners.
     */
    fun generateCorners(style: CornerStyle, strokeWidth: Float): GeneratedOrnaments {
        val lines = mutableListOf<LineSegmentGeom>()
        val circles = mutableListOf<CircleGeom>()
        val polygons = mutableListOf<PolygonGeom>()
        val paths = mutableListOf<PathGeom>()

        val corners = listOf(
            Pair(35f, 35f),
            Pair(465f, 35f),
            Pair(35f, 465f),
            Pair(465f, 465f)
        )

        when (style) {
            CornerStyle.NONE -> {}

            CornerStyle.NORSE_KNOTS -> {
                for ((cx, cy) in corners) {
                    val dirX = if (cx < 250f) 1f else -1f
                    val dirY = if (cy < 250f) 1f else -1f
                    circles.add(CircleGeom(cx, cy, 6f, isFilled = false, widthFactor = 1.2f))
                    circles.add(CircleGeom(cx + dirX * 12f, cy, 5f, isFilled = false, widthFactor = 0.9f))
                    circles.add(CircleGeom(cx, cy + dirY * 12f, 5f, isFilled = false, widthFactor = 0.9f))
                    lines.add(LineSegmentGeom(cx, cy, cx + dirX * 18f, cy + dirY * 18f, widthFactor = 1.2f))
                    circles.add(CircleGeom(cx + dirX * 18f, cy + dirY * 18f, 2.5f, isFilled = true))
                }
            }

            CornerStyle.RUNIC_BINDS -> {
                // Algiz protective rune ᛉ in each corner
                for ((cx, cy) in corners) {
                    val dirX = if (cx < 250f) 1f else -1f
                    val dirY = if (cy < 250f) 1f else -1f
                    // Stem towards center
                    val endX = cx + dirX * 22f
                    val endY = cy + dirY * 22f
                    lines.add(LineSegmentGeom(cx, cy, endX, endY, widthFactor = 1.4f))
                    // Branches
                    val midX = cx + dirX * 12f
                    val midY = cy + dirY * 12f
                    lines.add(LineSegmentGeom(midX, midY, midX + dirX * 8f - dirY * 8f, midY + dirY * 8f + dirX * 8f, widthFactor = 1.2f))
                    lines.add(LineSegmentGeom(midX, midY, midX + dirX * 8f + dirY * 8f, midY + dirY * 8f - dirX * 8f, widthFactor = 1.2f))
                    circles.add(CircleGeom(endX, endY, 2.0f, isFilled = true))
                }
            }

            CornerStyle.SHIELD_STUDS -> {
                // Forged rivets and studs
                for ((cx, cy) in corners) {
                    circles.add(CircleGeom(cx, cy, 10f, isFilled = false, widthFactor = 1.4f))
                    circles.add(CircleGeom(cx, cy, 5f, isFilled = true))
                    // 4 stud notches
                    lines.add(LineSegmentGeom(cx - 13f, cy, cx - 10f, cy, widthFactor = 1.2f))
                    lines.add(LineSegmentGeom(cx + 10f, cy, cx + 13f, cy, widthFactor = 1.2f))
                    lines.add(LineSegmentGeom(cx, cy - 13f, cx, cy - 10f, widthFactor = 1.2f))
                    lines.add(LineSegmentGeom(cx, cy + 10f, cx, cy + 13f, widthFactor = 1.2f))
                }
            }

            CornerStyle.SUN_RAYS -> {
                for ((cx, cy) in corners) {
                    val dirX = if (cx < 250f) 1f else -1f
                    val dirY = if (cy < 250f) 1f else -1f
                    for (i in 0 until 5) {
                        val angle = (i * PI / 8).toFloat()
                        val rayLen = 22f
                        val rx = cx + (dirX * cos(angle) - dirY * sin(angle)) * rayLen
                        val ry = cy + (dirX * sin(angle) + dirY * cos(angle)) * rayLen
                        lines.add(LineSegmentGeom(cx, cy, rx, ry, widthFactor = 0.8f, alpha = 0.7f))
                    }
                }
            }
        }

        return GeneratedOrnaments(lines, circles, polygons, paths)
    }

    /**
     * Generates protective charm notches along a stroke line.
     */
    fun generateStrokeNotches(p1: StrokePoint, p2: StrokePoint): List<LineSegmentGeom> {
        val dx = p2.x - p1.x
        val dy = p2.y - p1.y
        val dist = sqrt(dx * dx + dy * dy)
        if (dist < 55f) return emptyList()

        val nx = dx / dist
        val ny = dy / dist
        val px = -ny
        val py = nx

        val notches = mutableListOf<LineSegmentGeom>()
        val tPoints = if (dist > 110f) listOf(0.3f, 0.5f, 0.7f) else listOf(0.4f, 0.6f)

        for (t in tPoints) {
            val midX = p1.x + t * dx
            val midY = p1.y + t * dy
            val notchLen = 7f
            // 45 degree angled tick
            val notchDx = (px + nx) * notchLen * 0.7f
            val notchDy = (py + ny) * notchLen * 0.7f
            notches.add(LineSegmentGeom(midX - notchDx, midY - notchDy, midX + notchDx, midY + notchDy, widthFactor = 0.8f))
        }

        return notches
    }
}
