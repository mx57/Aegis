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
            FrameStyle.SPIKED_CHAIN -> {
                return generateSpikedChain(cx, cy, strokeWidth)
            }
            FrameStyle.CELTIC_MEDALLION -> {
                return generateCelticLattice(cx, cy, strokeWidth)
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
                // Sacred Astrolabe: Filigree golden orbits, 72 degree marks & micro-planetary nodes
                val r1 = 238f
                val r1Sub = 234f
                val r2 = 224f
                val r3 = 210f
                val r4 = 196f
                val r4Sub = 192f
                circles.add(CircleGeom(cx, cy, r1, widthFactor = 0.95f))
                circles.add(CircleGeom(cx, cy, r1Sub, widthFactor = 0.45f, alpha = 0.65f))
                circles.add(CircleGeom(cx, cy, r2, widthFactor = 0.55f, alpha = 0.75f))
                circles.add(CircleGeom(cx, cy, r3, widthFactor = 0.45f, alpha = 0.55f))
                circles.add(CircleGeom(cx, cy, r4, widthFactor = 0.85f))
                circles.add(CircleGeom(cx, cy, r4Sub, widthFactor = 0.45f, alpha = 0.65f))

                // 72 astrolabe degree marks with fine golden terminals
                val ticks = 72
                for (i in 0 until ticks) {
                    val angle = (2 * PI * i / ticks).toFloat()
                    val cosA = cos(angle)
                    val sinA = sin(angle)
                    val isCardinal = i % 18 == 0
                    val isMajor = i % 6 == 0
                    val tLen = if (isCardinal) 14f else if (isMajor) 9f else 5f
                    val wf = if (isCardinal) 1.35f else if (isMajor) 0.85f else 0.45f
                    lines.add(LineSegmentGeom(cx + (r2 - tLen) * cosA, cy + (r2 - tLen) * sinA, cx + r2 * cosA, cy + r2 * sinA, widthFactor = wf))

                    // Cardinal & intercardinal filigree star markers
                    if (isCardinal) {
                        val starR = r1 + 8f
                        circles.add(CircleGeom(cx + starR * cosA, cy + starR * sinA, 2.5f, isFilled = true))
                        circles.add(CircleGeom(cx + starR * cosA, cy + starR * sinA, 5.0f, isFilled = false, widthFactor = 0.65f))
                        circles.add(CircleGeom(cx + starR * cosA, cy + starR * sinA, 8.0f, isFilled = false, widthFactor = 0.35f, alpha = 0.6f))
                    }
                }

                // 8 sacred planetary orbit nodes with micro rings
                for (i in 0 until 8) {
                    val a = (2 * PI * (i + 0.5f) / 8).toFloat()
                    val orbitR = (r3 + r4) / 2f
                    circles.add(CircleGeom(cx + orbitR * cos(a), cy + orbitR * sin(a), 2.2f, isFilled = true))
                    circles.add(CircleGeom(cx + orbitR * cos(a), cy + orbitR * sin(a), 4.5f, isFilled = false, widthFactor = 0.45f, alpha = 0.7f))
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

            CenterEmblem.BEASTS_OF_ODIN -> {
                return generateBeastsOfOdin(cx, cy, strokeWidth)
            }

            CenterEmblem.FACETED_STAR -> {
                return generateFacetedStar(cx, cy, strokeWidth)
            }

            CenterEmblem.RUNIC_STELE -> {
                return generateRunicStele(cx, cy, strokeWidth)
            }

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

    fun generateSpikedChain(cx: Float = CENTER_X, cy: Float = CENTER_Y, strokeWidth: Float = 3f): GeneratedOrnaments {
        val lines = mutableListOf<LineSegmentGeom>()
        val circles = mutableListOf<CircleGeom>()
        val polygons = mutableListOf<PolygonGeom>()
        val paths = mutableListOf<PathGeom>()

        val r = 230f
        circles.add(CircleGeom(cx, cy, 215f, widthFactor = 0.75f, alpha = 0.70f))
        circles.add(CircleGeom(cx, cy, 245f, widthFactor = 0.75f, alpha = 0.70f))

        val linkCount = 28
        val angleStep = (2 * PI / linkCount).toFloat()

        for (i in 0 until linkCount) {
            val angle = i * angleStep
            val tanAngle = angle + (PI / 2).toFloat()
            val cosN = cos(angle)
            val sinN = sin(angle)
            val cosT = cos(tanAngle)
            val sinT = sin(tanAngle)

            val lx = cx + r * cosN
            val ly = cy + r * sinN

            val hl = 14f
            val hw = 7.5f
            val ihl = 8f
            val ihw = 3.5f

            val linkOutline = listOf(
                StrokePoint(lx - cosT * hl + cosN * hw * 0.5f, ly - sinT * hl + sinN * hw * 0.5f),
                StrokePoint(lx - cosT * hl * 0.5f + cosN * hw, ly - sinT * hl * 0.5f + sinN * hw),
                StrokePoint(lx + cosT * hl * 0.5f + cosN * hw, ly + sinT * hl * 0.5f + sinN * hw),
                StrokePoint(lx + cosT * hl + cosN * hw * 0.5f, ly + sinT * hl + sinN * hw * 0.5f),
                StrokePoint(lx + cosT * hl - cosN * hw * 0.5f, ly + sinT * hl - sinN * hw * 0.5f),
                StrokePoint(lx + cosT * hl * 0.5f - cosN * hw, ly + sinT * hl * 0.5f - sinN * hw),
                StrokePoint(lx - cosT * hl * 0.5f - cosN * hw, ly - sinT * hl * 0.5f - sinN * hw),
                StrokePoint(lx - cosT * hl - cosN * hw * 0.5f, ly - sinT * hl - sinN * hw * 0.5f)
            )
            paths.add(PathGeom(linkOutline, isClosed = true, widthFactor = 1.15f))

            val innerHole = listOf(
                StrokePoint(lx - cosT * ihl, ly - sinT * ihl),
                StrokePoint(lx + cosN * ihw, ly + sinN * ihw),
                StrokePoint(lx + cosT * ihl, ly + sinT * ihl),
                StrokePoint(lx - cosN * ihw, ly - sinN * ihw)
            )
            paths.add(PathGeom(innerHole, isClosed = true, widthFactor = 0.85f))

            val spikeLen = if (i % 2 == 0) 24f else 17f
            val spikeTip = StrokePoint(cx + (r + spikeLen) * cosN, cy + (r + spikeLen) * sinN)
            val spikeBaseL = StrokePoint(lx - cosT * 5f + cosN * hw, ly - sinT * 5f + sinN * hw)
            val spikeBaseR = StrokePoint(lx + cosT * 5f + cosN * hw, ly + sinT * 5f + sinN * hw)
            polygons.add(PolygonGeom(listOf(spikeBaseL, spikeTip, spikeBaseR), isFilled = false, widthFactor = 1.1f))
            lines.add(LineSegmentGeom(lx + cosN * hw, ly + sinN * hw, spikeTip.x, spikeTip.y, widthFactor = 0.65f, alpha = 0.85f))

            if (i % 2 == 1) {
                val inSpikeTip = StrokePoint(cx + (r - 14f) * cosN, cy + (r - 14f) * sinN)
                val inBaseL = StrokePoint(lx - cosT * 4f - cosN * hw, ly - sinT * 4f - sinN * hw)
                val inBaseR = StrokePoint(lx + cosT * 4f - cosN * hw, ly + sinT * 4f - sinN * hw)
                polygons.add(PolygonGeom(listOf(inBaseL, inSpikeTip, inBaseR), isFilled = false, widthFactor = 0.9f))
            }

            lines.add(LineSegmentGeom(lx - cosT * 7f + cosN * 2f, ly - sinT * 7f + sinN * 2f, lx - cosT * 3f + cosN * 5.5f, ly - sinT * 3f + sinN * 5.5f, widthFactor = 0.55f, alpha = 0.6f))
            lines.add(LineSegmentGeom(lx + cosT * 3f + cosN * 5.5f, ly + sinT * 3f + sinN * 5.5f, lx + cosT * 7f + cosN * 2f, ly + sinT * 7f + sinN * 2f, widthFactor = 0.55f, alpha = 0.6f))
        }

        return GeneratedOrnaments(lines, circles, polygons, paths)
    }

    fun generateCelticLattice(cx: Float = CENTER_X, cy: Float = CENTER_Y, strokeWidth: Float = 3f): GeneratedOrnaments {
        val lines = mutableListOf<LineSegmentGeom>()
        val circles = mutableListOf<CircleGeom>()
        val polygons = mutableListOf<PolygonGeom>()
        val paths = mutableListOf<PathGeom>()

        circles.add(CircleGeom(cx, cy, 150f, widthFactor = 0.7f, alpha = 0.65f))
        circles.add(CircleGeom(cx, cy, 185f, widthFactor = 0.85f, alpha = 0.75f))
        circles.add(CircleGeom(cx, cy, 214f, widthFactor = 0.8f, alpha = 0.70f))

        val loops = 16
        val step = (2 * PI / loops).toFloat()

        for (i in 0 until loops) {
            val a1 = i * step
            val a2 = (i + 1) * step
            val aMid = (a1 + a2) / 2f

            val pIn1 = StrokePoint(cx + 155f * cos(a1), cy + 155f * sin(a1))
            val pOutMid = StrokePoint(cx + 210f * cos(aMid), cy + 210f * sin(aMid))
            val pIn2 = StrokePoint(cx + 155f * cos(a2), cy + 155f * sin(a2))

            paths.add(PathGeom(listOf(pIn1, pOutMid, pIn2), isClosed = false, widthFactor = 0.9f))

            val pMidCross1 = StrokePoint(cx + 185f * cos(a1), cy + 185f * sin(a1))
            lines.add(LineSegmentGeom(pMidCross1.x, pMidCross1.y, pOutMid.x, pOutMid.y, widthFactor = 0.75f, alpha = 0.8f))

            circles.add(CircleGeom(pMidCross1.x, pMidCross1.y, 2.5f, isFilled = true))
            circles.add(CircleGeom(pOutMid.x, pOutMid.y, 3.0f, isFilled = false, widthFactor = 0.7f))

            val hx1 = cx + 170f * cos(aMid)
            val hy1 = cy + 170f * sin(aMid)
            val hx2 = cx + 195f * cos(aMid)
            val hy2 = cy + 195f * sin(aMid)
            lines.add(LineSegmentGeom(hx1, hy1, hx2, hy2, widthFactor = 0.5f, alpha = 0.55f))
        }

        return GeneratedOrnaments(lines, circles, polygons, paths)
    }

    fun generateFacetedStar(cx: Float = CENTER_X, cy: Float = CENTER_Y, strokeWidth: Float = 3f): GeneratedOrnaments {
        val lines = mutableListOf<LineSegmentGeom>()
        val circles = mutableListOf<CircleGeom>()
        val polygons = mutableListOf<PolygonGeom>()
        val paths = mutableListOf<PathGeom>()

        val rays = listOf(
            Triple(-PI / 2, 198f, 15f),
            Triple(PI / 2, 190f, 14f),
            Triple(PI, 190f, 14f),
            Triple(0.0, 190f, 14f),
            Triple(-PI / 4, 150f, 11f),
            Triple(-3 * PI / 4, 150f, 11f),
            Triple(PI / 4, 150f, 11f),
            Triple(3 * PI / 4, 150f, 11f)
        )

        for ((rad, rayLen, baseHalfW) in rays) {
            val angle = rad.toFloat()
            val cosA = cos(angle)
            val sinA = sin(angle)
            val perpX = -sinA
            val perpY = cosA

            val tipX = cx + rayLen * cosA
            val tipY = cy + rayLen * sinA

            lines.add(LineSegmentGeom(cx, cy, tipX, tipY, widthFactor = 1.35f))

            val bLx = cx + perpX * baseHalfW
            val bLy = cy + perpY * baseHalfW
            val bRx = cx - perpX * baseHalfW
            val bRy = cy - perpY * baseHalfW

            polygons.add(PolygonGeom(listOf(StrokePoint(cx, cy), StrokePoint(tipX, tipY), StrokePoint(bLx, bLy)), isFilled = false, widthFactor = 0.95f))
            polygons.add(PolygonGeom(listOf(StrokePoint(cx, cy), StrokePoint(tipX, tipY), StrokePoint(bRx, bRy)), isFilled = false, widthFactor = 0.95f))

            val hatchSteps = 7
            for (h in 1..hatchSteps) {
                val t = h.toFloat() / (hatchSteps + 1)
                val sTipX = cx + t * (tipX - cx)
                val sTipY = cy + t * (tipY - cy)
                val sBaseX = bRx + t * (tipX - bRx) * 0.75f
                val sBaseY = bRy + t * (tipY - bRy) * 0.75f
                lines.add(LineSegmentGeom(sTipX, sTipY, sBaseX, sBaseY, widthFactor = 0.55f, alpha = 0.65f))
            }
        }

        circles.add(CircleGeom(cx, cy, 12f, isFilled = false, widthFactor = 1.2f))
        circles.add(CircleGeom(cx, cy, 4f, isFilled = true))

        return GeneratedOrnaments(lines, circles, polygons, paths)
    }

    fun generateWolfGuardian(cx: Float = CENTER_X, cy: Float = CENTER_Y, strokeWidth: Float = 3f): GeneratedOrnaments {
        val lines = mutableListOf<LineSegmentGeom>()
        val circles = mutableListOf<CircleGeom>()
        val polygons = mutableListOf<PolygonGeom>()
        val paths = mutableListOf<PathGeom>()

        val frontEar = listOf(
            StrokePoint(135f, 178f),
            StrokePoint(145f, 132f),
            StrokePoint(168f, 168f)
        )
        paths.add(PathGeom(frontEar, isClosed = false, widthFactor = 1.3f))
        lines.add(LineSegmentGeom(145f, 138f, 150f, 165f, widthFactor = 0.7f, alpha = 0.75f))
        lines.add(LineSegmentGeom(148f, 145f, 156f, 168f, widthFactor = 0.6f, alpha = 0.70f))
        lines.add(LineSegmentGeom(140f, 155f, 146f, 172f, widthFactor = 0.6f, alpha = 0.70f))

        val backEar = listOf(
            StrokePoint(118f, 172f),
            StrokePoint(124f, 142f),
            StrokePoint(135f, 178f)
        )
        paths.add(PathGeom(backEar, isClosed = false, widthFactor = 1.0f))

        val browMuzzle = listOf(
            StrokePoint(168f, 168f),
            StrokePoint(176f, 192f),
            StrokePoint(188f, 212f),
            StrokePoint(204f, 226f)
        )
        paths.add(PathGeom(browMuzzle, isClosed = false, widthFactor = 1.3f))

        val noseLeather = listOf(
            StrokePoint(204f, 226f),
            StrokePoint(206f, 233f),
            StrokePoint(198f, 235f),
            StrokePoint(196f, 228f)
        )
        polygons.add(PolygonGeom(noseLeather, isFilled = true))
        lines.add(LineSegmentGeom(202f, 230f, 199f, 232f, widthFactor = 0.8f))

        val upperLip = listOf(
            StrokePoint(198f, 235f),
            StrokePoint(188f, 244f),
            StrokePoint(168f, 250f)
        )
        paths.add(PathGeom(upperLip, isClosed = false, widthFactor = 1.2f))

        val lowerJaw = listOf(
            StrokePoint(168f, 250f),
            StrokePoint(180f, 256f),
            StrokePoint(190f, 246f)
        )
        paths.add(PathGeom(lowerJaw, isClosed = false, widthFactor = 1.0f))

        val eyeTop = listOf(StrokePoint(162f, 198f), StrokePoint(170f, 196f), StrokePoint(176f, 201f))
        val eyeBottom = listOf(StrokePoint(162f, 198f), StrokePoint(169f, 203f), StrokePoint(176f, 201f))
        paths.add(PathGeom(eyeTop, isClosed = false, widthFactor = 1.25f))
        paths.add(PathGeom(eyeBottom, isClosed = false, widthFactor = 1.0f))
        circles.add(CircleGeom(169f, 199.5f, 2.0f, isFilled = true))
        circles.add(CircleGeom(171f, 198.5f, 0.7f, isFilled = true))
        lines.add(LineSegmentGeom(160f, 194f, 178f, 192f, widthFactor = 0.85f, alpha = 0.8f))

        circles.add(CircleGeom(190f, 234f, 0.9f, isFilled = true))
        circles.add(CircleGeom(186f, 236f, 0.9f, isFilled = true))
        circles.add(CircleGeom(192f, 239f, 0.9f, isFilled = true))
        circles.add(CircleGeom(188f, 241f, 0.9f, isFilled = true))
        circles.add(CircleGeom(182f, 240f, 0.9f, isFilled = true))

        val maneLocks = listOf(
            listOf(StrokePoint(168f, 250f), StrokePoint(146f, 268f), StrokePoint(158f, 264f)),
            listOf(StrokePoint(158f, 264f), StrokePoint(135f, 288f), StrokePoint(148f, 282f)),
            listOf(StrokePoint(148f, 282f), StrokePoint(120f, 314f), StrokePoint(136f, 306f)),
            listOf(StrokePoint(136f, 306f), StrokePoint(105f, 338f), StrokePoint(124f, 328f)),
            listOf(StrokePoint(124f, 328f), StrokePoint(90f, 362f), StrokePoint(112f, 350f))
        )
        for (lock in maneLocks) {
            paths.add(PathGeom(lock, isClosed = false, widthFactor = 1.15f))
        }

        val neckCrest = listOf(
            StrokePoint(118f, 172f),
            StrokePoint(98f, 210f),
            StrokePoint(108f, 206f),
            StrokePoint(88f, 248f),
            StrokePoint(98f, 244f),
            StrokePoint(78f, 290f),
            StrokePoint(92f, 286f),
            StrokePoint(72f, 336f)
        )
        paths.add(PathGeom(neckCrest, isClosed = false, widthFactor = 1.2f))

        val furHatches = listOf(
            Pair(StrokePoint(176f, 216f), StrokePoint(182f, 226f)),
            Pair(StrokePoint(172f, 220f), StrokePoint(178f, 230f)),
            Pair(StrokePoint(168f, 226f), StrokePoint(174f, 236f)),
            Pair(StrokePoint(154f, 246f), StrokePoint(144f, 260f)),
            Pair(StrokePoint(148f, 252f), StrokePoint(138f, 266f)),
            Pair(StrokePoint(140f, 270f), StrokePoint(128f, 286f)),
            Pair(StrokePoint(134f, 276f), StrokePoint(122f, 292f)),
            Pair(StrokePoint(124f, 298f), StrokePoint(110f, 316f)),
            Pair(StrokePoint(116f, 306f), StrokePoint(102f, 324f)),
            Pair(StrokePoint(106f, 224f), StrokePoint(96f, 236f)),
            Pair(StrokePoint(96f, 260f), StrokePoint(86f, 274f))
        )
        for ((p1, p2) in furHatches) {
            lines.add(LineSegmentGeom(p1.x, p1.y, p2.x, p2.y, widthFactor = 0.6f, alpha = 0.65f))
        }

        return GeneratedOrnaments(lines, circles, polygons, paths)
    }

    fun generateRavenGuardian(cx: Float = CENTER_X, cy: Float = CENTER_Y, strokeWidth: Float = 3f): GeneratedOrnaments {
        val lines = mutableListOf<LineSegmentGeom>()
        val circles = mutableListOf<CircleGeom>()
        val polygons = mutableListOf<PolygonGeom>()
        val paths = mutableListOf<PathGeom>()

        val crownNape = listOf(
            StrokePoint(336f, 196f),
            StrokePoint(364f, 172f),
            StrokePoint(398f, 176f),
            StrokePoint(422f, 195f),
            StrokePoint(434f, 228f)
        )
        paths.add(PathGeom(crownNape, isClosed = false, widthFactor = 1.3f))

        val beakCulmen = listOf(
            StrokePoint(336f, 196f),
            StrokePoint(318f, 206f),
            StrokePoint(302f, 220f),
            StrokePoint(292f, 236f)
        )
        paths.add(PathGeom(beakCulmen, isClosed = false, widthFactor = 1.4f))

        val mouthSlit = listOf(
            StrokePoint(292f, 236f),
            StrokePoint(312f, 232f),
            StrokePoint(338f, 230f)
        )
        paths.add(PathGeom(mouthSlit, isClosed = false, widthFactor = 1.25f))

        val lowerBeak = listOf(
            StrokePoint(292f, 236f),
            StrokePoint(312f, 242f),
            StrokePoint(334f, 244f)
        )
        paths.add(PathGeom(lowerBeak, isClosed = false, widthFactor = 1.2f))

        lines.add(LineSegmentGeom(328f, 208f, 318f, 214f, widthFactor = 0.85f))
        lines.add(LineSegmentGeom(334f, 202f, 324f, 206f, widthFactor = 0.6f, alpha = 0.8f))
        lines.add(LineSegmentGeom(332f, 205f, 322f, 210f, widthFactor = 0.6f, alpha = 0.8f))
        lines.add(LineSegmentGeom(330f, 208f, 320f, 214f, widthFactor = 0.6f, alpha = 0.8f))

        val eyeCx = 352f
        val eyeCy = 206f
        circles.add(CircleGeom(eyeCx, eyeCy, 6.5f, isFilled = false, widthFactor = 1.15f))
        circles.add(CircleGeom(eyeCx, eyeCy, 3.2f, isFilled = true))
        circles.add(CircleGeom(eyeCx - 1.2f, eyeCy - 1.2f, 1.0f, isFilled = true))
        lines.add(LineSegmentGeom(eyeCx - 8f, eyeCy - 3f, eyeCx + 8f, eyeCy - 4f, widthFactor = 0.7f, alpha = 0.75f))
        lines.add(LineSegmentGeom(eyeCx - 7f, eyeCy + 4f, eyeCx + 8f, eyeCy + 3f, widthFactor = 0.65f, alpha = 0.70f))

        val throatHackles = listOf(
            listOf(StrokePoint(334f, 244f), StrokePoint(320f, 268f), StrokePoint(335f, 262f)),
            listOf(StrokePoint(335f, 262f), StrokePoint(326f, 294f), StrokePoint(344f, 284f)),
            listOf(StrokePoint(344f, 284f), StrokePoint(346f, 324f), StrokePoint(365f, 310f)),
            listOf(StrokePoint(365f, 310f), StrokePoint(375f, 355f), StrokePoint(395f, 336f))
        )
        for (hackle in throatHackles) {
            paths.add(PathGeom(hackle, isClosed = false, widthFactor = 1.15f))
        }

        val mantleFeathers = listOf(
            listOf(StrokePoint(434f, 228f), StrokePoint(442f, 268f), StrokePoint(430f, 262f)),
            listOf(StrokePoint(430f, 262f), StrokePoint(438f, 305f), StrokePoint(424f, 298f)),
            listOf(StrokePoint(424f, 298f), StrokePoint(428f, 348f), StrokePoint(408f, 338f))
        )
        for (f in mantleFeathers) {
            paths.add(PathGeom(f, isClosed = false, widthFactor = 1.15f))
        }

        val featherHatches = listOf(
            Pair(StrokePoint(366f, 185f), StrokePoint(378f, 196f)),
            Pair(StrokePoint(375f, 188f), StrokePoint(388f, 200f)),
            Pair(StrokePoint(386f, 194f), StrokePoint(398f, 206f)),
            Pair(StrokePoint(342f, 252f), StrokePoint(330f, 266f)),
            Pair(StrokePoint(348f, 256f), StrokePoint(336f, 272f)),
            Pair(StrokePoint(354f, 275f), StrokePoint(340f, 292f)),
            Pair(StrokePoint(362f, 280f), StrokePoint(348f, 298f)),
            Pair(StrokePoint(372f, 305f), StrokePoint(358f, 324f)),
            Pair(StrokePoint(382f, 312f), StrokePoint(368f, 332f)),
            Pair(StrokePoint(412f, 240f), StrokePoint(426f, 255f)),
            Pair(StrokePoint(406f, 275f), StrokePoint(420f, 292f))
        )
        for ((p1, p2) in featherHatches) {
            lines.add(LineSegmentGeom(p1.x, p1.y, p2.x, p2.y, widthFactor = 0.6f, alpha = 0.65f))
        }

        return GeneratedOrnaments(lines, circles, polygons, paths)
    }

    fun generateRunicStele(cx: Float = CENTER_X, cy: Float = CENTER_Y, strokeWidth: Float = 3f): GeneratedOrnaments {
        val lines = mutableListOf<LineSegmentGeom>()
        val circles = mutableListOf<CircleGeom>()
        val polygons = mutableListOf<PolygonGeom>()
        val paths = mutableListOf<PathGeom>()

        val topY = 155f
        val botY = 425f
        val peakY = 115f
        val topW = 24f
        val botW = 34f

        lines.add(LineSegmentGeom(cx - topW, topY, cx - botW, botY, widthFactor = 1.3f))
        lines.add(LineSegmentGeom(cx + topW, topY, cx + botW, botY, widthFactor = 1.3f))
        lines.add(LineSegmentGeom(cx - topW, topY, cx, peakY, widthFactor = 1.3f))
        lines.add(LineSegmentGeom(cx + topW, topY, cx, peakY, widthFactor = 1.3f))
        lines.add(LineSegmentGeom(cx, peakY, cx, topY, widthFactor = 1.1f))
        lines.add(LineSegmentGeom(cx, peakY, cx, 52f, widthFactor = 1.4f))

        lines.add(LineSegmentGeom(cx - botW - 8f, botY, cx + botW + 8f, botY, widthFactor = 1.3f))
        lines.add(LineSegmentGeom(cx - botW - 14f, botY + 11f, cx + botW + 14f, botY + 11f, widthFactor = 1.3f))
        lines.add(LineSegmentGeom(cx - botW, botY, cx - botW - 14f, botY + 11f, widthFactor = 1.0f))
        lines.add(LineSegmentGeom(cx + botW, botY, cx + botW + 14f, botY + 11f, widthFactor = 1.0f))

        lines.add(LineSegmentGeom(cx - topW + 6f, topY + 5f, cx - botW + 8f, botY - 5f, widthFactor = 0.65f, alpha = 0.65f))
        lines.add(LineSegmentGeom(cx + topW - 6f, topY + 5f, cx + botW - 8f, botY - 5f, widthFactor = 0.65f, alpha = 0.65f))

        for (step in 1..8) {
            val sy = topY + step * ((botY - topY) / 9f)
            val sw = topW + (botW - topW) * (sy - topY) / (botY - topY)
            lines.add(LineSegmentGeom(cx - sw, sy, cx - sw + 6f, sy, widthFactor = 0.7f, alpha = 0.7f))
            lines.add(LineSegmentGeom(cx + sw - 6f, sy, cx + sw, sy, widthFactor = 0.7f, alpha = 0.7f))
        }

        return GeneratedOrnaments(lines, circles, polygons, paths)
    }

    fun generateBeastsOfOdin(cx: Float = CENTER_X, cy: Float = CENTER_Y, strokeWidth: Float = 3f): GeneratedOrnaments {
        val lines = mutableListOf<LineSegmentGeom>()
        val circles = mutableListOf<CircleGeom>()
        val polygons = mutableListOf<PolygonGeom>()
        val paths = mutableListOf<PathGeom>()

        val star = generateFacetedStar(cx, cy, strokeWidth)
        lines.addAll(star.lines)
        circles.addAll(star.circles)
        polygons.addAll(star.polygons)
        paths.addAll(star.paths)

        val stele = generateRunicStele(cx, cy, strokeWidth)
        lines.addAll(stele.lines)
        circles.addAll(stele.circles)
        polygons.addAll(stele.polygons)
        paths.addAll(stele.paths)

        val wolf = generateWolfGuardian(cx, cy, strokeWidth)
        lines.addAll(wolf.lines)
        circles.addAll(wolf.circles)
        polygons.addAll(wolf.polygons)
        paths.addAll(wolf.paths)

        val raven = generateRavenGuardian(cx, cy, strokeWidth)
        lines.addAll(raven.lines)
        circles.addAll(raven.circles)
        polygons.addAll(raven.polygons)
        paths.addAll(raven.paths)

        return GeneratedOrnaments(lines, circles, polygons, paths)
    }
}
