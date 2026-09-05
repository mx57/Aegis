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
                // Jelling / Urnes Serpent (Jormungandr, Midgard Serpent) circular frame
                // Features 3D dual-rail ribbon body, drop shadow, herringbone scale armor,
                // authentic Urnes dragon head with fangs & tongue, Futhark rune ticks, and triquetra tail knot.
                val rMid = 224f
                val rWidth = 18f
                val rIn = rMid - rWidth / 2f  // 215f
                val rOut = rMid + rWidth / 2f // 233f

                // 1. Ambient Drop Shadow Ring under serpent body ribbon
                circles.add(CircleGeom(cx + 2.5f, cy + 3.0f, rOut, widthFactor = 1.6f, alpha = 0.22f))
                circles.add(CircleGeom(cx + 2.5f, cy + 3.0f, rIn, widthFactor = 1.2f, alpha = 0.22f))

                // 2. Main Outer and Inner Forged Body Rails
                circles.add(CircleGeom(cx, cy, rOut, widthFactor = 1.1f))
                circles.add(CircleGeom(cx, cy, rIn, widthFactor = 1.1f))
                // Central Spine Guide Line
                circles.add(CircleGeom(cx, cy, rMid, widthFactor = 0.45f, alpha = 0.50f))

                // Head position at angle ~ 0.75 rad (~43 deg)
                // Tail knot position at angle ~ 5.4 rad (~310 deg)
                val headAngle = 0.75f
                val tailAngle = 5.40f

                // 3. Scale Notches, Highlight Bevels & Dark Shading Along Body
                val scales = 48
                for (i in 0 until scales) {
                    val angle = (2 * PI * i / scales).toFloat()
                    // Leave opening between tail and head (from 5.25 to 0.88 rad)
                    if (angle > 5.25f || angle < 0.88f) continue

                    val cosA = cos(angle)
                    val sinA = sin(angle)
                    val tanX = -sinA
                    val tanY = cosA

                    // V-notch scale armor cuts
                    val pIn = StrokePoint(cx + rIn * cosA - tanX * 3f, cy + rIn * sinA - tanY * 3f)
                    val pMid = StrokePoint(cx + rMid * cosA + tanX * 4f, cy + rMid * sinA + tanY * 4f)
                    val pOut = StrokePoint(cx + rOut * cosA - tanX * 3f, cy + rOut * sinA - tanY * 3f)

                    lines.add(LineSegmentGeom(pIn.x, pIn.y, pMid.x, pMid.y, widthFactor = 0.70f))
                    lines.add(LineSegmentGeom(pMid.x, pMid.y, pOut.x, pOut.y, widthFactor = 0.70f))

                    // Directional shadow cross-hatching between scales for 3D depth
                    if (i % 2 == 0) {
                        val hIn = StrokePoint(cx + (rIn + 3f) * cosA, cy + (rIn + 3f) * sinA)
                        val hMid = StrokePoint(cx + (rMid - 2f) * cosA, cy + (rMid - 2f) * sinA)
                        lines.add(LineSegmentGeom(hIn.x, hIn.y, hMid.x, hMid.y, widthFactor = 0.45f, alpha = 0.65f))
                    }

                    // Inscribed Sacred Futhark Rune Ticks along Serpent Spine
                    if (i % 6 == 0) {
                        circles.add(CircleGeom(cx + rMid * cosA, cy + rMid * sinA, 2.0f, isFilled = true))
                    }
                }

                // 4. Detailed Urnes/Jelling Dragon Head
                val hx = cx + rMid * cos(headAngle)
                val hy = cy + rMid * sin(headAngle)
                val hTanX = -sin(headAngle)
                val hTanY = cos(headAngle)
                val hNormX = cos(headAngle)
                val hNormY = sin(headAngle)

                // Snout & Upper Jaw with nostril notch
                val upperSnout = listOf(
                    StrokePoint(hx + hNormX * 8f, hy + hNormY * 8f),
                    StrokePoint(hx + hTanX * 14f + hNormX * 10f, hy + hTanY * 14f + hNormY * 10f),
                    StrokePoint(hx + hTanX * 28f + hNormX * 6f, hy + hTanY * 28f + hNormY * 6f), // Snout tip
                    StrokePoint(hx + hTanX * 24f + hNormX * 1f, hy + hTanY * 24f + hNormY * 1f)   // Mouth corner
                )
                paths.add(PathGeom(upperSnout, isClosed = false, widthFactor = 1.35f))

                // Upper Fang
                val fangUpper = listOf(
                    StrokePoint(hx + hTanX * 26f + hNormX * 5f, hy + hTanY * 26f + hNormY * 5f),
                    StrokePoint(hx + hTanX * 28f - hNormX * 3f, hy + hTanY * 28f - hNormY * 3f),
                    StrokePoint(hx + hTanX * 22f + hNormX * 2f, hy + hTanY * 22f + hNormY * 2f)
                )
                polygons.add(PolygonGeom(fangUpper, isFilled = true))

                // Lower Jaw & Commissure
                val lowerJaw = listOf(
                    StrokePoint(hx + hTanX * 24f + hNormX * 1f, hy + hTanY * 24f + hNormY * 1f),
                    StrokePoint(hx + hTanX * 22f - hNormX * 8f, hy + hTanY * 22f - hNormY * 8f),
                    StrokePoint(hx + hTanX * 8f - hNormX * 9f, hy + hTanY * 8f - hNormY * 9f),
                    StrokePoint(hx - hNormX * 7f, hy - hNormY * 7f)
                )
                paths.add(PathGeom(lowerJaw, isClosed = false, widthFactor = 1.30f))

                // Lower Fang
                val fangLower = listOf(
                    StrokePoint(hx + hTanX * 18f - hNormX * 6f, hy + hTanY * 18f - hNormY * 6f),
                    StrokePoint(hx + hTanX * 20f + hNormX * 1f, hy + hTanY * 20f + hNormY * 1f),
                    StrokePoint(hx + hTanX * 15f - hNormX * 4f, hy + hTanY * 15f - hNormY * 4f)
                )
                polygons.add(PolygonGeom(fangLower, isFilled = true))

                // Bifurcated Serpentine Tongue
                val tongueMain = listOf(
                    StrokePoint(hx + hTanX * 22f, hy + hTanY * 22f),
                    StrokePoint(hx + hTanX * 34f, hy + hTanY * 34f),
                    StrokePoint(hx + hTanX * 42f + hNormX * 8f, hy + hTanY * 42f + hNormY * 8f)
                )
                val tongueFork = listOf(
                    StrokePoint(hx + hTanX * 34f, hy + hTanY * 34f),
                    StrokePoint(hx + hTanX * 40f - hNormX * 6f, hy + hTanY * 40f - hNormY * 6f)
                )
                paths.add(PathGeom(tongueMain, isClosed = false, widthFactor = 1.10f))
                paths.add(PathGeom(tongueFork, isClosed = false, widthFactor = 0.95f))

                // Crested Horn / Ear Tuft
                val hornTuft = listOf(
                    StrokePoint(hx - hTanX * 4f + hNormX * 8f, hy - hTanY * 4f + hNormY * 8f),
                    StrokePoint(hx - hTanX * 16f + hNormX * 18f, hy - hTanY * 16f + hNormY * 18f),
                    StrokePoint(hx - hTanX * 8f + hNormX * 12f, hy - hTanY * 8f + hNormY * 12f)
                )
                paths.add(PathGeom(hornTuft, isClosed = false, widthFactor = 1.25f))

                // Almond Serpent Eye with Pupil & Specular Glint
                val eyeX = hx + hTanX * 12f + hNormX * 4f
                val eyeY = hy + hTanY * 12f + hNormY * 4f
                circles.add(CircleGeom(eyeX, eyeY, 3.6f, isFilled = false, widthFactor = 1.10f))
                circles.add(CircleGeom(eyeX, eyeY, 2.2f, isFilled = true))
                circles.add(CircleGeom(eyeX - 0.8f, eyeY - 0.8f, 0.9f, isFilled = true)) // Specular catchlight

                // 5. Interlaced Urnes Knot Tail Loop
                val tx = cx + rMid * cos(tailAngle)
                val ty = cy + rMid * sin(tailAngle)
                val tTanX = -sin(tailAngle)
                val tTanY = cos(tailAngle)
                val tNormX = cos(tailAngle)
                val tNormY = sin(tailAngle)

                // Triquetra knot loop
                circles.add(CircleGeom(tx, ty, 10f, isFilled = false, widthFactor = 1.30f))
                circles.add(CircleGeom(tx, ty, 4f, isFilled = true))

                // Interlaced Tail Loop Extension & Spear Tip Barb
                val tailLoop = listOf(
                    StrokePoint(tx, ty),
                    StrokePoint(tx - tTanX * 16f + tNormX * 12f, ty - tTanY * 16f + tNormY * 12f),
                    StrokePoint(tx - tTanX * 28f, ty - tTanY * 28f),
                    StrokePoint(tx - tTanX * 20f - tNormX * 10f, ty - tTanY * 20f - tNormY * 10f),
                    StrokePoint(tx, ty)
                )
                paths.add(PathGeom(tailLoop, isClosed = true, widthFactor = 1.10f))

                // Spear Tip Barb at tail end
                val barbTip = StrokePoint(tx - tTanX * 38f, ty - tTanY * 38f)
                val barbL = StrokePoint(tx - tTanX * 28f + tNormX * 6f, ty - tTanY * 28f + tNormY * 6f)
                val barbR = StrokePoint(tx - tTanX * 28f - tNormX * 6f, ty - tTanY * 28f - tNormY * 6f)
                polygons.add(PolygonGeom(listOf(barbTip, barbL, barbR), isFilled = true))
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

            FrameStyle.YGGDRASIL_BRANCHES -> {
                // Interwoven roots and branches of Yggdrasil forming a sacred circular boundary
                val rOut = 236f
                val rIn = 210f
                circles.add(CircleGeom(cx, cy, rOut, widthFactor = 0.95f))
                circles.add(CircleGeom(cx, cy, rIn, widthFactor = 0.65f, alpha = 0.70f))

                // 12 Intertwined Root Arcs
                val arcs = 12
                for (i in 0 until arcs) {
                    val a1 = (2 * PI * i / arcs).toFloat()
                    val a2 = (2 * PI * (i + 1) / arcs).toFloat()
                    val aMid = (a1 + a2) / 2f

                    val p1 = StrokePoint(cx + rIn * cos(a1), cy + rIn * sin(a1))
                    val pMid = StrokePoint(cx + (rOut + 10f) * cos(aMid), cy + (rOut + 10f) * sin(aMid))
                    val p2 = StrokePoint(cx + rIn * cos(a2), cy + rIn * sin(a2))

                    paths.add(PathGeom(listOf(p1, pMid, p2), widthFactor = 1.10f))

                    // Leaf node tip
                    circles.add(CircleGeom(pMid.x, pMid.y, 2.8f, isFilled = true))
                    // Root knot circle
                    circles.add(CircleGeom(cx + (rIn + 8f) * cos(aMid), cy + (rIn + 8f) * sin(aMid), 1.8f, isFilled = true))
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
     * Scales an ornament geometry group proportionally around a center point (cx, cy).
     */
    fun scaleOrnaments(
        ornaments: GeneratedOrnaments,
        cx: Float = CENTER_X,
        cy: Float = CENTER_Y,
        scaleFactor: Float
    ): GeneratedOrnaments {
        if (Math.abs(scaleFactor - 1.0f) < 0.001f) return ornaments
        val s = scaleFactor.coerceIn(0.4f, 1.8f)
        val lines = ornaments.lines.map { l ->
            LineSegmentGeom(
                x1 = cx + (l.x1 - cx) * s,
                y1 = cy + (l.y1 - cy) * s,
                x2 = cx + (l.x2 - cx) * s,
                y2 = cy + (l.y2 - cy) * s,
                widthFactor = l.widthFactor,
                alpha = l.alpha
            )
        }
        val circles = ornaments.circles.map { c ->
            CircleGeom(
                cx = cx + (c.cx - cx) * s,
                cy = cy + (c.cy - cy) * s,
                radius = c.radius * s,
                isFilled = c.isFilled,
                widthFactor = c.widthFactor,
                alpha = c.alpha
            )
        }
        val polygons = ornaments.polygons.map { p ->
            PolygonGeom(
                points = p.points.map { pt ->
                    StrokePoint(cx + (pt.x - cx) * s, cy + (pt.y - cy) * s)
                },
                isFilled = p.isFilled,
                widthFactor = p.widthFactor,
                alpha = p.alpha
            )
        }
        val paths = ornaments.paths.map { p ->
            PathGeom(
                points = p.points.map { pt ->
                    StrokePoint(cx + (pt.x - cx) * s, cy + (pt.y - cy) * s)
                },
                isClosed = p.isClosed,
                isFilled = p.isFilled,
                widthFactor = p.widthFactor,
                alpha = p.alpha
            )
        }
        return GeneratedOrnaments(lines, circles, polygons, paths)
    }

    /**
     * Generates central emblem elements based on CenterEmblem, optionally scaled by scaleFactor.
     */
    fun generateCenterEmblem(emblem: CenterEmblem, strokeWidth: Float, scaleFactor: Float = 1.0f): GeneratedOrnaments {
        val cx = CENTER_X
        val cy = CENTER_Y

        val baseOrnaments = when (emblem) {
            CenterEmblem.NONE -> GeneratedOrnaments(emptyList(), emptyList(), emptyList(), emptyList())

            CenterEmblem.BEASTS_OF_ODIN -> {
                generateBeastsOfOdin(cx, cy, strokeWidth)
            }

            CenterEmblem.FACETED_STAR -> {
                generateFacetedStar(cx, cy, strokeWidth)
            }

            CenterEmblem.RUNIC_STELE -> {
                generateRunicStele(cx, cy, strokeWidth)
            }

            else -> {
                val lines = mutableListOf<LineSegmentGeom>()
                val circles = mutableListOf<CircleGeom>()
                val polygons = mutableListOf<PolygonGeom>()
                val paths = mutableListOf<PathGeom>()

                when (emblem) {
                    CenterEmblem.NONE,
                    CenterEmblem.BEASTS_OF_ODIN,
                    CenterEmblem.FACETED_STAR,
                    CenterEmblem.RUNIC_STELE -> {}

            CenterEmblem.YGGDRASIL_TREE -> {
                // World Tree Yggdrasil: Roots (3 Norns wells), Trunk, Branching Crown, and 9 Worlds Orbs
                val trunkBaseY = 320f
                val trunkTopY = 210f

                // 1. Triple Root Wells (Urdarbrunnr)
                val root1 = listOf(StrokePoint(cx, trunkBaseY), StrokePoint(cx - 30f, 360f), StrokePoint(cx - 50f, 390f))
                val root2 = listOf(StrokePoint(cx, trunkBaseY), StrokePoint(cx, 370f), StrokePoint(cx, 405f))
                val root3 = listOf(StrokePoint(cx, trunkBaseY), StrokePoint(cx + 30f, 360f), StrokePoint(cx + 50f, 390f))
                paths.add(PathGeom(root1, widthFactor = 1.40f))
                paths.add(PathGeom(root2, widthFactor = 1.40f))
                paths.add(PathGeom(root3, widthFactor = 1.40f))

                circles.add(CircleGeom(cx - 50f, 390f, 4.5f, isFilled = true))
                circles.add(CircleGeom(cx, 405f, 4.5f, isFilled = true))
                circles.add(CircleGeom(cx + 50f, 390f, 4.5f, isFilled = true))

                // 2. Powerful Twisted Tree Trunk
                lines.add(LineSegmentGeom(cx - 6f, trunkBaseY, cx - 4f, trunkTopY, widthFactor = 1.60f))
                lines.add(LineSegmentGeom(cx + 6f, trunkBaseY, cx + 4f, trunkTopY, widthFactor = 1.60f))
                lines.add(LineSegmentGeom(cx, trunkBaseY, cx, trunkTopY, widthFactor = 1.10f)) // Ridge

                // 3. Spreading Canopy Branches
                val b1 = listOf(StrokePoint(cx, trunkTopY), StrokePoint(cx - 35f, 170f), StrokePoint(cx - 65f, 140f))
                val b2 = listOf(StrokePoint(cx, trunkTopY), StrokePoint(cx - 20f, 155f), StrokePoint(cx - 30f, 115f))
                val b3 = listOf(StrokePoint(cx, trunkTopY), StrokePoint(cx, 140f), StrokePoint(cx, 95f))
                val b4 = listOf(StrokePoint(cx, trunkTopY), StrokePoint(cx + 20f, 155f), StrokePoint(cx + 30f, 115f))
                val b5 = listOf(StrokePoint(cx, trunkTopY), StrokePoint(cx + 35f, 170f), StrokePoint(cx + 65f, 140f))
                paths.add(PathGeom(b1, widthFactor = 1.30f))
                paths.add(PathGeom(b2, widthFactor = 1.20f))
                paths.add(PathGeom(b3, widthFactor = 1.40f))
                paths.add(PathGeom(b4, widthFactor = 1.20f))
                paths.add(PathGeom(b5, widthFactor = 1.30f))

                // 4. Nine Norse Worlds Orbs (Asgard, Midgard, Helheim, Alfheim, etc.)
                val worlds = listOf(
                    Pair(cx, 95f),        // Asgard
                    Pair(cx - 30f, 115f),  // Alfheim
                    Pair(cx + 30f, 115f),  // Vanaheim
                    Pair(cx - 65f, 140f),  // Jotunheim
                    Pair(cx, 180f),        // Midgard (center hub)
                    Pair(cx + 65f, 140f),  // Muspelheim
                    Pair(cx - 50f, 390f),  // Niflheim
                    Pair(cx, 405f),        // Helheim
                    Pair(cx + 50f, 390f)   // Svartalfheim
                )
                for ((wx, wy) in worlds) {
                    circles.add(CircleGeom(wx, wy, 3.5f, isFilled = true))
                    circles.add(CircleGeom(wx, wy, 6.5f, isFilled = false, widthFactor = 0.70f))
                }
            }

            CenterEmblem.VALKNUT -> {
                // Sacred Valknut of Odin (Hrungnir's Heart):
                // 3-fold Borromean interlocking triangular ribbons with 3D faceted bevels,
                // lit/shadow ridge facets, dark cross-hatching, and sacred central node.
                val rOuter = 34f
                val rInner = 22f
                val ribbonW = 5.5f

                // 3 Interlocking Triangles arranged at 0, 120, 240 degrees
                val triangleCenters = listOf(
                    Pair(0f, -12f),
                    Pair(-10.4f, 6f),
                    Pair(10.4f, 6f)
                )

                for ((idx, offset) in triangleCenters.withIndex()) {
                    val (ox, oy) = offset
                    val tcX = cx + ox
                    val tcY = cy + oy

                    // 3 Vertices for this triangle
                    val vOuter = mutableListOf<StrokePoint>()
                    val vInner = mutableListOf<StrokePoint>()

                    for (v in 0 until 3) {
                        val angle = (-PI / 2 + 2 * PI * v / 3 + idx * PI / 6).toFloat()
                        vOuter.add(StrokePoint(tcX + rOuter * cos(angle), tcY + rOuter * sin(angle)))
                        vInner.add(StrokePoint(tcX + rInner * cos(angle), tcY + rInner * sin(angle)))
                    }

                    // 1. Ambient Drop Shadow underneath triangle base
                    val shadowPoly = vOuter.map { StrokePoint(it.x + 2.5f, it.y + 3.2f) }
                    polygons.add(PolygonGeom(shadowPoly, isFilled = true, alpha = 0.22f))

                    // 2. Outer and Inner Ribbon Boundary Paths
                    val outerPath = vOuter + listOf(vOuter.first())
                    val innerPath = vInner + listOf(vInner.first())
                    paths.add(PathGeom(outerPath, isClosed = true, widthFactor = 1.35f))
                    paths.add(PathGeom(innerPath, isClosed = true, widthFactor = 0.95f))

                    // 3. Central Facet Ridge Lines (divides lit and shadow facets)
                    for (v in 0 until 3) {
                        val pOut1 = vOuter[v]
                        val pOut2 = vOuter[(v + 1) % 3]
                        val pIn1 = vInner[v]
                        val pIn2 = vInner[(v + 1) % 3]

                        val midOutX = (pOut1.x + pOut2.x) / 2f
                        val midOutY = (pOut1.y + pOut2.y) / 2f
                        val midInX = (pIn1.x + pIn2.x) / 2f
                        val midInY = (pIn1.y + pIn2.y) / 2f

                        // Longitudinal center ridge line along the ribbon edge
                        lines.add(LineSegmentGeom(midInX, midInY, midOutX, midOutY, widthFactor = 0.70f, alpha = 0.85f))

                        // Corner vertex facet join lines
                        lines.add(LineSegmentGeom(pIn1.x, pIn1.y, pOut1.x, pOut1.y, widthFactor = 1.10f))

                        // Volumetric Shadow Cross-Hatching on alternate facets
                        val hatchSteps = 4
                        for (h in 1..hatchSteps) {
                            val t = h.toFloat() / (hatchSteps + 1)
                            val hx1 = pIn1.x + t * (pIn2.x - pIn1.x)
                            val hy1 = pIn1.y + t * (pIn2.y - pIn1.y)
                            val hx2 = pOut1.x + t * (pOut2.x - pOut1.x)
                            val hy2 = pOut1.y + t * (pOut2.y - pOut1.y)
                            lines.add(LineSegmentGeom(hx1, hy1, hx2, hy2, widthFactor = 0.45f, alpha = 0.65f))
                        }
                    }

                    // Apex vertex node studs
                    for (v in vOuter) {
                        circles.add(CircleGeom(v.x, v.y, 2.2f, isFilled = true))
                    }
                }

                // Central Sacred Hub & Guard Rings
                circles.add(CircleGeom(cx, cy, 3.2f, isFilled = true))
                circles.add(CircleGeom(cx, cy, 6.5f, isFilled = false, widthFactor = 0.65f, alpha = 0.70f))
            }

            CenterEmblem.TRIQUETRA -> {
                // Sacred 3-leaf Celtic Triquetra Knot:
                // Interwoven arcuate dual-ribbon petals, central solar circle,
                // crossing node studs, and volumetric recess shading.
                val R = 32f // Radius of vesica piscis petal arc centers
                val ribbonHalfW = 2.8f

                // Intertwined Solar Circle
                circles.add(CircleGeom(cx, cy, 28f, isFilled = false, widthFactor = 1.30f))
                circles.add(CircleGeom(cx, cy, 28f - ribbonHalfW * 2f, isFilled = false, widthFactor = 0.75f, alpha = 0.65f))

                // 3 Petal Arcs oriented at angles -PI/2 (top), -PI/2 + 2PI/3 (right), -PI/2 + 4PI/3 (left)
                for (i in 0 until 3) {
                    val angle = (-PI / 2 + 2 * PI * i / 3).toFloat()
                    val leafCx = cx + 18f * cos(angle)
                    val leafCy = cy + 18f * sin(angle)

                    // Generate smooth arcuate petal path (Bezier-like arc)
                    val arcSteps = 16
                    val arcPtsOuter = mutableListOf<StrokePoint>()
                    val arcPtsInner = mutableListOf<StrokePoint>()
                    val arcPtsCenter = mutableListOf<StrokePoint>()

                    val startAngle = angle - PI.toFloat() / 1.8f
                    val endAngle = angle + PI.toFloat() / 1.8f

                    for (s in 0..arcSteps) {
                        val t = s.toFloat() / arcSteps
                        val a = startAngle + t * (endAngle - startAngle)
                        val radOut = R + ribbonHalfW
                        val radIn = R - ribbonHalfW

                        arcPtsOuter.add(StrokePoint(leafCx + radOut * cos(a), leafCy + radOut * sin(a)))
                        arcPtsInner.add(StrokePoint(leafCx + radIn * cos(a), leafCy + radIn * sin(a)))
                        arcPtsCenter.add(StrokePoint(leafCx + R * cos(a), leafCy + R * sin(a)))
                    }

                    // 1. Ambient Drop Shadow under petal ribbon
                    val shadowPath = arcPtsOuter.map { StrokePoint(it.x + 2.2f, it.y + 2.8f) }
                    paths.add(PathGeom(shadowPath, isClosed = false, widthFactor = 1.40f, alpha = 0.22f))

                    // 2. Dual Ribbon Outer and Inner Boundary Paths
                    paths.add(PathGeom(arcPtsOuter, isClosed = false, widthFactor = 1.15f))
                    paths.add(PathGeom(arcPtsInner, isClosed = false, widthFactor = 1.15f))
                    paths.add(PathGeom(arcPtsCenter, isClosed = false, widthFactor = 0.50f, alpha = 0.60f)) // Central spine

                    // 3. Recess Shading Lines across ribbon
                    for (s in 2 until arcSteps - 2 step 3) {
                        val pIn = arcPtsInner[s]
                        val pOut = arcPtsOuter[s]
                        lines.add(LineSegmentGeom(pIn.x, pIn.y, pOut.x, pOut.y, widthFactor = 0.50f, alpha = 0.65f))
                    }

                    // 4. Petal Tip Apex Nodes & Crossing Studs
                    val tipX = cx + 42f * cos(angle)
                    val tipY = cy + 42f * sin(angle)
                    circles.add(CircleGeom(tipX, tipY, 3.2f, isFilled = true))
                    circles.add(CircleGeom(tipX, tipY, 6.0f, isFilled = false, widthFactor = 0.65f, alpha = 0.70f))
                }

                // Central Sacred Hub & Trinity Studs
                circles.add(CircleGeom(cx, cy, 3.5f, isFilled = true))
                circles.add(CircleGeom(cx, cy, 7.5f, isFilled = false, widthFactor = 0.80f))

                for (i in 0 until 3) {
                    val angle = (-PI / 2 + 2 * PI * i / 3 + PI / 3).toFloat()
                    circles.add(CircleGeom(cx + 12f * cos(angle), cy + 12f * sin(angle), 1.8f, isFilled = true))
                }
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

            CenterEmblem.MJOLNIR -> {
                // Sacred Hammer of Thor (Mjolnir) with suspension ring, bound handle and flared head
                // 1. Suspension Ring at top
                circles.add(CircleGeom(cx, cy - 25f, 5.5f, isFilled = false, widthFactor = 1.2f))
                circles.add(CircleGeom(cx, cy - 25f, 2.0f, isFilled = true))

                // 2. Bound Handle with cross-wrapping
                lines.add(LineSegmentGeom(cx - 3.5f, cy - 20f, cx - 3.5f, cy + 6f, widthFactor = 1.3f))
                lines.add(LineSegmentGeom(cx + 3.5f, cy - 20f, cx + 3.5f, cy + 6f, widthFactor = 1.3f))
                lines.add(LineSegmentGeom(cx, cy - 20f, cx, cy + 6f, widthFactor = 0.8f)) // central ridge
                for (step in 0..4) {
                    val hy = cy - 17f + step * 4.5f
                    lines.add(LineSegmentGeom(cx - 3.5f, hy - 1.5f, cx + 3.5f, hy + 1.5f, widthFactor = 0.9f))
                }

                // 3. Flared Hammer Head Base & Crown
                val headTopY = cy + 6f
                val headBottomY = cy + 26f
                val headHalfWTop = 20f
                val headHalfWBottom = 26f

                val headContour = listOf(
                    StrokePoint(cx - headHalfWTop, headTopY),
                    StrokePoint(cx + headHalfWTop, headTopY),
                    StrokePoint(cx + headHalfWBottom, headBottomY),
                    StrokePoint(cx - headHalfWBottom, headBottomY),
                    StrokePoint(cx - headHalfWTop, headTopY)
                )
                paths.add(PathGeom(headContour, isClosed = true, widthFactor = 1.5f))

                // Inner facet bevels
                lines.add(LineSegmentGeom(cx - headHalfWTop + 4f, headTopY + 3f, cx - headHalfWBottom + 4f, headBottomY - 3f, widthFactor = 0.9f))
                lines.add(LineSegmentGeom(cx + headHalfWTop - 4f, headTopY + 3f, cx + headHalfWBottom - 4f, headBottomY - 3f, widthFactor = 0.9f))
                lines.add(LineSegmentGeom(cx - headHalfWTop + 4f, headTopY + 3f, cx + headHalfWTop - 4f, headTopY + 3f, widthFactor = 0.9f))
                lines.add(LineSegmentGeom(cx - headHalfWBottom + 4f, headBottomY - 3f, cx + headHalfWBottom - 4f, headBottomY - 3f, widthFactor = 0.9f))

                // 4. Central Thor's Lightning / Thurisaz Rune on Hammer
                val bolt = listOf(
                    StrokePoint(cx - 3f, cy + 10f),
                    StrokePoint(cx + 3f, cy + 14f),
                    StrokePoint(cx - 2f, cy + 17f),
                    StrokePoint(cx + 3f, cy + 22f)
                )
                paths.add(PathGeom(bolt, widthFactor = 1.3f))

                // 5. Radiating Sparks of Mjolnir
                val sparks = listOf(
                    Pair(cx - 31f, cy + 16f),
                    Pair(cx + 31f, cy + 16f),
                    Pair(cx - 28f, cy + 28f),
                    Pair(cx + 28f, cy + 28f),
                    Pair(cx, cy + 32f)
                )
                for ((sx, sy) in sparks) {
                    circles.add(CircleGeom(sx, sy, 2.0f, isFilled = true))
                    lines.add(LineSegmentGeom(sx - 2f, sy, sx + 2f, sy, widthFactor = 0.8f))
                    lines.add(LineSegmentGeom(sx, sy - 2f, sx, sy + 2f, widthFactor = 0.8f))
                }
            }

            CenterEmblem.RAVEN_ODIN -> {
                // Raven of Odin (Hugin) with spread wings, feathered arcs and sacred gaze
                // 1. Head & Beak
                val headY = cy - 18f
                circles.add(CircleGeom(cx, headY, 5f, isFilled = false, widthFactor = 1.3f))
                circles.add(CircleGeom(cx + 1.5f, headY - 1f, 1.5f, isFilled = true)) // eye
                // Sharp Beak
                val beak = listOf(
                    StrokePoint(cx - 4f, headY - 2f),
                    StrokePoint(cx - 12f, headY + 1f),
                    StrokePoint(cx - 3f, headY + 3f)
                )
                paths.add(PathGeom(beak, isClosed = true, widthFactor = 1.2f))

                // 2. Body & Sacred Heart Diamond
                lines.add(LineSegmentGeom(cx, headY + 5f, cx, cy + 14f, widthFactor = 1.6f))
                val heartDiamond = listOf(
                    StrokePoint(cx, cy - 3f),
                    StrokePoint(cx + 6f, cy + 4f),
                    StrokePoint(cx, cy + 11f),
                    StrokePoint(cx - 6f, cy + 4f),
                    StrokePoint(cx, cy - 3f)
                )
                paths.add(PathGeom(heartDiamond, isClosed = true, widthFactor = 1.2f))
                circles.add(CircleGeom(cx, cy + 4f, 2.0f, isFilled = true))

                // 3. Spreading Wings
                // Left wing primary feathers
                val leftWing1 = listOf(StrokePoint(cx, cy), StrokePoint(cx - 18f, cy - 12f), StrokePoint(cx - 34f, cy - 8f))
                val leftWing2 = listOf(StrokePoint(cx, cy + 4f), StrokePoint(cx - 16f, cy - 4f), StrokePoint(cx - 32f, cy + 2f))
                val leftWing3 = listOf(StrokePoint(cx, cy + 8f), StrokePoint(cx - 14f, cy + 4f), StrokePoint(cx - 26f, cy + 12f))
                paths.add(PathGeom(leftWing1, widthFactor = 1.4f))
                paths.add(PathGeom(leftWing2, widthFactor = 1.2f))
                paths.add(PathGeom(leftWing3, widthFactor = 1.1f))

                // Right wing primary feathers
                val rightWing1 = listOf(StrokePoint(cx, cy), StrokePoint(cx + 18f, cy - 12f), StrokePoint(cx + 34f, cy - 8f))
                val rightWing2 = listOf(StrokePoint(cx, cy + 4f), StrokePoint(cx + 16f, cy - 4f), StrokePoint(cx + 32f, cy + 2f))
                val rightWing3 = listOf(StrokePoint(cx, cy + 8f), StrokePoint(cx + 14f, cy + 4f), StrokePoint(cx + 26f, cy + 12f))
                paths.add(PathGeom(rightWing1, widthFactor = 1.4f))
                paths.add(PathGeom(rightWing2, widthFactor = 1.2f))
                paths.add(PathGeom(rightWing3, widthFactor = 1.1f))

                // 4. Tail Feathers
                val tail1 = listOf(StrokePoint(cx, cy + 14f), StrokePoint(cx - 8f, cy + 28f))
                val tail2 = listOf(StrokePoint(cx, cy + 14f), StrokePoint(cx, cy + 30f))
                val tail3 = listOf(StrokePoint(cx, cy + 14f), StrokePoint(cx + 8f, cy + 28f))
                paths.add(PathGeom(tail1, widthFactor = 1.2f))
                paths.add(PathGeom(tail2, widthFactor = 1.4f))
                paths.add(PathGeom(tail3, widthFactor = 1.2f))
                circles.add(CircleGeom(cx, cy + 30f, 2.5f, isFilled = true))
            }
        }
        GeneratedOrnaments(lines, circles, polygons, paths)
    }
}

        return if (Math.abs(scaleFactor - 1.0f) > 0.001f) {
            scaleOrnaments(baseOrnaments, cx, cy, scaleFactor)
        } else {
            baseOrnaments
        }
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
            FinialType.NONE -> {}

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
        // Outer and inner forged rim rails with subtle ambient occlusion
        circles.add(CircleGeom(cx, cy, 210f, widthFactor = 0.80f, alpha = 0.65f))
        circles.add(CircleGeom(cx, cy, 213f, widthFactor = 0.50f, alpha = 0.50f))
        circles.add(CircleGeom(cx, cy, 247f, widthFactor = 0.50f, alpha = 0.50f))
        circles.add(CircleGeom(cx, cy, 250f, widthFactor = 0.85f, alpha = 0.70f))

        val linkCount = 24
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

            val hl = 16f
            val hw = 8.5f
            val ihl = 9.5f
            val ihw = 3.8f

            // 1. Cast shadow base under each chain link
            val shadowOff = 3.0f
            val castShadow = listOf(
                StrokePoint(lx - cosT * (hl + 1f) + cosN * (hw + shadowOff), ly - sinT * (hl + 1f) + sinN * (hw + shadowOff)),
                StrokePoint(lx + cosT * (hl + 1f) + cosN * (hw + shadowOff), ly + sinT * (hl + 1f) + sinN * (hw + shadowOff)),
                StrokePoint(lx + cosT * (hl + 1f) + cosN * hw, ly + sinT * (hl + 1f) + sinN * hw),
                StrokePoint(lx - cosT * (hl + 1f) + cosN * hw, ly - sinT * (hl + 1f) + sinN * hw)
            )
            polygons.add(PolygonGeom(castShadow, isFilled = true, alpha = 0.25f))

            // 2. Main Outer Link Torus (thick forged 3D link)
            val linkOutline = listOf(
                StrokePoint(lx - cosT * hl + cosN * hw * 0.4f, ly - sinT * hl + sinN * hw * 0.4f),
                StrokePoint(lx - cosT * hl * 0.6f + cosN * hw, ly - sinT * hl * 0.6f + sinN * hw),
                StrokePoint(lx + cosT * hl * 0.6f + cosN * hw, ly + sinT * hl * 0.6f + sinN * hw),
                StrokePoint(lx + cosT * hl + cosN * hw * 0.4f, ly + sinT * hl + sinN * hw * 0.4f),
                StrokePoint(lx + cosT * hl - cosN * hw * 0.4f, ly + sinT * hl - sinN * hw * 0.4f),
                StrokePoint(lx + cosT * hl * 0.6f - cosN * hw, ly + sinT * hl * 0.6f - sinN * hw),
                StrokePoint(lx - cosT * hl * 0.6f - cosN * hw, ly - sinT * hl * 0.6f - sinN * hw),
                StrokePoint(lx - cosT * hl - cosN * hw * 0.4f, ly - sinT * hl - sinN * hw * 0.4f)
            )
            paths.add(PathGeom(linkOutline, isClosed = true, widthFactor = 1.35f))

            // Inner hole
            val innerHole = listOf(
                StrokePoint(lx - cosT * ihl, ly - sinT * ihl),
                StrokePoint(lx - cosT * ihl * 0.4f + cosN * ihw, ly - sinT * ihl * 0.4f + sinN * ihw),
                StrokePoint(lx + cosT * ihl * 0.4f + cosN * ihw, ly + sinT * ihl * 0.4f + sinN * ihw),
                StrokePoint(lx + cosT * ihl, ly + sinT * ihl),
                StrokePoint(lx + cosT * ihl * 0.4f - cosN * ihw, ly + sinT * ihl * 0.4f - sinN * ihw),
                StrokePoint(lx - cosT * ihl * 0.4f - cosN * ihw, ly - sinT * ihl * 0.4f - sinN * ihw)
            )
            paths.add(PathGeom(innerHole, isClosed = true, widthFactor = 1.05f))

            // 3. 3D Bevel highlight line on the lit edge
            lines.add(
                LineSegmentGeom(
                    lx - cosT * (hl * 0.7f) - cosN * (hw * 0.65f),
                    ly - sinT * (hl * 0.7f) - sinN * (hw * 0.65f),
                    lx + cosT * (hl * 0.7f) - cosN * (hw * 0.65f),
                    ly + sinT * (hl * 0.7f) - sinN * (hw * 0.65f),
                    widthFactor = 0.55f,
                    alpha = 0.60f
                )
            )

            // 4. Volumetric metallic cross-hatching on the shadow curve of the link
            val hatchCount = 5
            for (h in 0..hatchCount) {
                val t = (h.toFloat() / hatchCount) * 2f - 1f // -1..1
                val hx = lx + cosT * (hl * 0.65f * t)
                val hy = ly + sinT * (hl * 0.65f * t)
                lines.add(
                    LineSegmentGeom(
                        hx + cosN * (ihw * 0.9f),
                        hy + sinN * (ihw * 0.9f),
                        hx + cosN * (hw * 0.9f),
                        hy + sinN * (hw * 0.9f),
                        widthFactor = 0.45f,
                        alpha = 0.70f
                    )
                )
            }

            // 5. Interlocking connector side link to next link
            val nextAngle = (i + 1) * angleStep
            val midA = (angle + nextAngle) / 2f
            val mx = cx + r * cos(midA)
            val my = cy + r * sin(midA)
            val mCosT = cos(midA + PI.toFloat() / 2f)
            val mSinT = sin(midA + PI.toFloat() / 2f)
            val mCosN = cos(midA)
            val mSinN = sin(midA)

            // Interlocking bridge loop
            val interLink = listOf(
                StrokePoint(mx - mCosT * 8f - mCosN * 5f, my - mSinT * 8f - mSinN * 5f),
                StrokePoint(mx + mCosT * 8f - mCosN * 5f, my + mSinT * 8f - mSinN * 5f),
                StrokePoint(mx + mCosT * 8f + mCosN * 5f, my + mSinT * 8f + mSinN * 5f),
                StrokePoint(mx - mCosT * 8f + mCosN * 5f, my - mSinT * 8f + mSinN * 5f)
            )
            paths.add(PathGeom(interLink, isClosed = true, widthFactor = 0.90f))
            lines.add(LineSegmentGeom(mx - mCosT * 6f, my - mSinT * 6f, mx + mCosT * 6f, my + mSinT * 6f, widthFactor = 0.5f, alpha = 0.5f))

            // 6. 3D Forged Conical Spikes along outer rim
            val isMajorSpike = i % 2 == 0
            val spikeLen = if (isMajorSpike) 26f else 18f
            val spikeTip = StrokePoint(cx + (r + spikeLen) * cosN, cy + (r + spikeLen) * sinN)
            val spikeBaseL = StrokePoint(lx - cosT * 5.5f + cosN * hw, ly - sinT * 5.5f + sinN * hw)
            val spikeBaseR = StrokePoint(lx + cosT * 5.5f + cosN * hw, ly + sinT * 5.5f + sinN * hw)
            val spikeBaseMid = StrokePoint(lx + cosN * hw, ly + sinN * hw)

            // Spike outline
            polygons.add(PolygonGeom(listOf(spikeBaseL, spikeTip, spikeBaseR), isFilled = false, widthFactor = 1.20f))
            // Spike center ridge line
            lines.add(LineSegmentGeom(spikeBaseMid.x, spikeBaseMid.y, spikeTip.x, spikeTip.y, widthFactor = 0.90f))

            // Spike shadow side cross-hatching (gives 3D conical volume)
            for (sh in 1..4) {
                val st = sh / 5f
                val h1x = spikeBaseR.x + st * (spikeTip.x - spikeBaseR.x)
                val h1y = spikeBaseR.y + st * (spikeTip.y - spikeBaseR.y)
                val h2x = spikeBaseMid.x + st * (spikeTip.x - spikeBaseMid.x)
                val h2y = spikeBaseMid.y + st * (spikeTip.y - spikeBaseMid.y)
                lines.add(LineSegmentGeom(h1x, h1y, h2x, h2y, widthFactor = 0.50f, alpha = 0.70f))
            }

            // Inward forged studs on alternating links
            if (!isMajorSpike) {
                val inTip = StrokePoint(cx + (r - 15f) * cosN, cy + (r - 15f) * sinN)
                val inBaseL = StrokePoint(lx - cosT * 4.5f - cosN * hw, ly - sinT * 4.5f - sinN * hw)
                val inBaseR = StrokePoint(lx + cosT * 4.5f - cosN * hw, ly + sinT * 4.5f - sinN * hw)
                polygons.add(PolygonGeom(listOf(inBaseL, inTip, inBaseR), isFilled = false, widthFactor = 0.95f))
                lines.add(LineSegmentGeom(lx - cosN * hw, ly - sinN * hw, inTip.x, inTip.y, widthFactor = 0.70f))
            }
        }

        return GeneratedOrnaments(lines, circles, polygons, paths)
    }

    fun generateCelticLattice(cx: Float = CENTER_X, cy: Float = CENTER_Y, strokeWidth: Float = 3f): GeneratedOrnaments {
        val lines = mutableListOf<LineSegmentGeom>()
        val circles = mutableListOf<CircleGeom>()
        val polygons = mutableListOf<PolygonGeom>()
        val paths = mutableListOf<PathGeom>()

        // Circular background medallions and guard rings
        circles.add(CircleGeom(cx, cy, 142f, widthFactor = 0.85f, alpha = 0.65f))
        circles.add(CircleGeom(cx, cy, 145f, widthFactor = 0.45f, alpha = 0.45f))
        circles.add(CircleGeom(cx, cy, 178f, widthFactor = 0.55f, alpha = 0.45f))
        circles.add(CircleGeom(cx, cy, 206f, widthFactor = 0.45f, alpha = 0.45f))
        circles.add(CircleGeom(cx, cy, 210f, widthFactor = 0.85f, alpha = 0.65f))

        val loops = 16
        val step = (2 * PI / loops).toFloat()
        val ribbonWidth = 6.5f

        // Interlaced Celtic Braid with dual-edge ribbon and dark cross-hatched recesses
        for (i in 0 until loops) {
            val a1 = i * step
            val a2 = (i + 1) * step
            val aMid = (a1 + a2) / 2f
            val aNextMid = aMid + step

            val rIn = 148f
            val rMid = 178f
            val rOut = 206f

            // Ribbon Strand 1 (Inner to Outer wave)
            val p1A = StrokePoint(cx + (rIn - ribbonWidth / 2f) * cos(a1), cy + (rIn - ribbonWidth / 2f) * sin(a1))
            val pMidA = StrokePoint(cx + (rOut - ribbonWidth / 2f) * cos(aMid), cy + (rOut - ribbonWidth / 2f) * sin(aMid))
            val p2A = StrokePoint(cx + (rIn - ribbonWidth / 2f) * cos(a2), cy + (rIn - ribbonWidth / 2f) * sin(a2))

            val p1B = StrokePoint(cx + (rIn + ribbonWidth / 2f) * cos(a1), cy + (rIn + ribbonWidth / 2f) * sin(a1))
            val pMidB = StrokePoint(cx + (rOut + ribbonWidth / 2f) * cos(aMid), cy + (rOut + ribbonWidth / 2f) * sin(aMid))
            val p2B = StrokePoint(cx + (rIn + ribbonWidth / 2f) * cos(a2), cy + (rIn + ribbonWidth / 2f) * sin(a2))

            paths.add(PathGeom(listOf(p1A, pMidA, p2A), widthFactor = 0.85f))
            paths.add(PathGeom(listOf(p1B, pMidB, p2B), widthFactor = 0.85f))

            // Ribbon Strand 2 (Intersecting counter wave)
            val c1A = StrokePoint(cx + (rOut - ribbonWidth / 2f) * cos(a1), cy + (rOut - ribbonWidth / 2f) * sin(a1))
            val cMidA = StrokePoint(cx + (rIn + ribbonWidth / 2f) * cos(aMid), cy + (rIn + ribbonWidth / 2f) * sin(aMid))
            val c2A = StrokePoint(cx + (rOut - ribbonWidth / 2f) * cos(a2), cy + (rOut - ribbonWidth / 2f) * sin(a2))

            val c1B = StrokePoint(cx + (rOut + ribbonWidth / 2f) * cos(a1), cy + (rOut + ribbonWidth / 2f) * sin(a1))
            val cMidB = StrokePoint(cx + (rIn - ribbonWidth / 2f) * cos(aMid), cy + (rIn - ribbonWidth / 2f) * sin(aMid))
            val c2B = StrokePoint(cx + (rOut + ribbonWidth / 2f) * cos(a2), cy + (rOut + ribbonWidth / 2f) * sin(a2))

            paths.add(PathGeom(listOf(c1A, cMidA, c2A), widthFactor = 0.85f))
            paths.add(PathGeom(listOf(c1B, cMidB, c2B), widthFactor = 0.85f))

            // Dark Recess Cross-Hatching in the voids between ribbons (creates rich depth as in photo)
            val voidCenter = StrokePoint(cx + rMid * cos(aMid), cy + rMid * sin(aMid))
            circles.add(CircleGeom(voidCenter.x, voidCenter.y, 2.2f, isFilled = true, alpha = 0.85f))

            for (h in 1..4) {
                val off = (h - 2.5f) * 3.5f
                val perpA = aMid + PI.toFloat() / 2f
                val hx1 = voidCenter.x + off * cos(aMid) - 4.5f * cos(perpA)
                val hy1 = voidCenter.y + off * sin(aMid) - 4.5f * sin(perpA)
                val hx2 = voidCenter.x + off * cos(aMid) + 4.5f * cos(perpA)
                val hy2 = voidCenter.y + off * sin(aMid) + 4.5f * sin(perpA)
                lines.add(LineSegmentGeom(hx1, hy1, hx2, hy2, widthFactor = 0.50f, alpha = 0.60f))
            }

            // Outer and inner apex studs
            circles.add(CircleGeom(pMidB.x, pMidB.y, 2.5f, isFilled = true, alpha = 0.90f))
            circles.add(CircleGeom(pMidB.x, pMidB.y, 4.5f, isFilled = false, widthFactor = 0.55f))
            circles.add(CircleGeom(cMidB.x, cMidB.y, 2.0f, isFilled = true, alpha = 0.85f))
        }

        return GeneratedOrnaments(lines, circles, polygons, paths)
    }

    fun generateFacetedStar(cx: Float = CENTER_X, cy: Float = CENTER_Y, strokeWidth: Float = 3f): GeneratedOrnaments {
        val lines = mutableListOf<LineSegmentGeom>()
        val circles = mutableListOf<CircleGeom>()
        val polygons = mutableListOf<PolygonGeom>()
        val paths = mutableListOf<PathGeom>()

        // 8-pointed 3D Compass Star matching photo:
        // Dramatic towering North ray (length 202f up to Y=48f), South ray (196f), East & West (194f), diagonals (160f)
        val rays = listOf(
            Triple(-PI / 2, 202f, 16f),      // North (towering vertical spire)
            Triple(PI / 2, 196f, 15f),       // South
            Triple(0.0, 194f, 15f),          // East
            Triple(PI, 194f, 15f),           // West
            Triple(-PI / 4, 160f, 12f),      // North-East
            Triple(-3 * PI / 4, 160f, 12f),  // North-West
            Triple(PI / 4, 160f, 12f),       // South-East
            Triple(3 * PI / 4, 160f, 12f)    // South-West
        )

        for ((rad, rayLen, baseHalfW) in rays) {
            val angle = rad.toFloat()
            val cosA = cos(angle)
            val sinA = sin(angle)
            val perpX = -sinA
            val perpY = cosA

            val tipX = cx + rayLen * cosA
            val tipY = cy + rayLen * sinA

            val bLx = cx + perpX * baseHalfW
            val bLy = cy + perpY * baseHalfW
            val bRx = cx - perpX * baseHalfW
            val bRy = cy - perpY * baseHalfW

            // 1. Ambient Drop Shadow underneath the ray
            val shX = 3.5f
            val shY = 4.5f
            polygons.add(
                PolygonGeom(
                    listOf(
                        StrokePoint(cx + shX, cy + shY),
                        StrokePoint(tipX + shX, tipY + shY),
                        StrokePoint(bRx + shX, bRy + shY)
                    ),
                    isFilled = true,
                    alpha = 0.20f
                )
            )

            // 2. Central Ridge Line (divides lit face and shadow face with sharp 3D edge)
            lines.add(LineSegmentGeom(cx, cy, tipX, tipY, widthFactor = 1.45f))

            // 3. Lit Face (Left/Top) - clean facet with subtle highlight bevel
            polygons.add(PolygonGeom(listOf(StrokePoint(cx, cy), StrokePoint(tipX, tipY), StrokePoint(bLx, bLy)), isFilled = false, widthFactor = 1.05f))
            // Specular highlight parallel to ridge
            val hlSt = 0.35f
            lines.add(
                LineSegmentGeom(
                    cx + hlSt * (bLx - cx),
                    cy + hlSt * (bLy - cy),
                    tipX + hlSt * (bLx - tipX) * 0.4f,
                    tipY + hlSt * (bLy - tipY) * 0.4f,
                    widthFactor = 0.45f,
                    alpha = 0.45f
                )
            )

            // 4. Shadow Face (Right/Bottom) - Filled with dense, authentic angled graphite cross-hatching
            polygons.add(PolygonGeom(listOf(StrokePoint(cx, cy), StrokePoint(tipX, tipY), StrokePoint(bRx, bRy)), isFilled = false, widthFactor = 1.15f))

            val hatchSteps = 12
            for (h in 1..hatchSteps) {
                val t = h.toFloat() / (hatchSteps + 1)
                // From ridge to outer right border
                val sRidgeX = cx + t * (tipX - cx)
                val sRidgeY = cy + t * (tipY - cy)
                val sEdgeX = bRx + t * (tipX - bRx)
                val sEdgeY = bRy + t * (tipY - bRy)

                // Angled hatching lines (cross-directional for metallic texture)
                lines.add(LineSegmentGeom(sRidgeX, sRidgeY, sEdgeX, sEdgeY, widthFactor = 0.60f, alpha = 0.75f))
                if (h % 2 == 0) {
                    // Secondary micro cross-hatch near base
                    val subX = sRidgeX + 0.5f * (sEdgeX - sRidgeX)
                    val subY = sRidgeY + 0.5f * (sEdgeY - sRidgeY)
                    lines.add(LineSegmentGeom(sRidgeX, sRidgeY, subX + perpX * 3f, subY + perpY * 3f, widthFactor = 0.45f, alpha = 0.55f))
                }
            }
        }

        // Center faceted octagonal hub
        val hubR = 14f
        val octPts = mutableListOf<StrokePoint>()
        for (i in 0..8) {
            val a = (2 * PI * i / 8).toFloat()
            octPts.add(StrokePoint(cx + hubR * cos(a), cy + hubR * sin(a)))
        }
        paths.add(PathGeom(octPts, isClosed = true, widthFactor = 1.25f))
        circles.add(CircleGeom(cx, cy, 7.5f, isFilled = false, widthFactor = 1.10f))
        circles.add(CircleGeom(cx, cy, 3.5f, isFilled = true))

        return GeneratedOrnaments(lines, circles, polygons, paths)
    }

    fun generateWolfGuardian(cx: Float = CENTER_X, cy: Float = CENTER_Y, strokeWidth: Float = 3f): GeneratedOrnaments {
        val lines = mutableListOf<LineSegmentGeom>()
        val circles = mutableListOf<CircleGeom>()
        val polygons = mutableListOf<PolygonGeom>()
        val paths = mutableListOf<PathGeom>()

        // Realistic 3/4 wolf portrait on left side facing right toward stele:
        // 1. Forehead & Muzzle Bridge
        val browMuzzle = listOf(
            StrokePoint(138f, 154f), // Crown
            StrokePoint(155f, 168f), // Forehead
            StrokePoint(168f, 186f), // Brow ridge above eye
            StrokePoint(180f, 204f), // Upper muzzle
            StrokePoint(194f, 220f), // Nose bridge
            StrokePoint(206f, 230f)  // Nose tip
        )
        paths.add(PathGeom(browMuzzle, isClosed = false, widthFactor = 1.40f))

        // 2. Black Nose Leather (anatomical with nostrils and highlight)
        val noseLeather = listOf(
            StrokePoint(206f, 230f),
            StrokePoint(208f, 236f),
            StrokePoint(204f, 241f),
            StrokePoint(196f, 238f),
            StrokePoint(198f, 232f)
        )
        polygons.add(PolygonGeom(noseLeather, isFilled = true))
        // Nostril groove and highlight
        lines.add(LineSegmentGeom(205f, 233f, 201f, 235f, widthFactor = 0.9f))
        circles.add(CircleGeom(205f, 231.5f, 0.8f, isFilled = true)) // Specular catchlight

        // 3. Upper Lip & Mouth Line
        val upperLip = listOf(
            StrokePoint(196f, 238f),
            StrokePoint(188f, 244f),
            StrokePoint(176f, 248f),
            StrokePoint(164f, 252f) // Mouth corner (commissure)
        )
        paths.add(PathGeom(upperLip, isClosed = false, widthFactor = 1.30f))

        // Lower Jaw & Chin with soft fur
        val lowerJaw = listOf(
            StrokePoint(194f, 246f),
            StrokePoint(184f, 254f),
            StrokePoint(168f, 258f),
            StrokePoint(156f, 260f)
        )
        paths.add(PathGeom(lowerJaw, isClosed = false, widthFactor = 1.15f))
        // Chin fur tufts
        lines.add(LineSegmentGeom(186f, 252f, 182f, 258f, widthFactor = 0.6f, alpha = 0.7f))
        lines.add(LineSegmentGeom(178f, 255f, 174f, 262f, widthFactor = 0.6f, alpha = 0.7f))
        lines.add(LineSegmentGeom(170f, 257f, 164f, 265f, widthFactor = 0.6f, alpha = 0.7f))

        // 4. Whisker Pad Follicles & Curved Whiskers
        val whiskerDots = listOf(
            Pair(192f, 236f), Pair(188f, 238f), Pair(184f, 241f),
            Pair(194f, 239f), Pair(190f, 242f), Pair(186f, 245f),
            Pair(195f, 242f), Pair(191f, 245f), Pair(187f, 248f)
        )
        for ((wx, wy) in whiskerDots) {
            circles.add(CircleGeom(wx, wy, 0.9f, isFilled = true))
        }
        // Graceful curved whiskers
        val whiskers = listOf(
            listOf(StrokePoint(192f, 236f), StrokePoint(204f, 238f), StrokePoint(218f, 242f)),
            listOf(StrokePoint(188f, 238f), StrokePoint(202f, 242f), StrokePoint(220f, 248f)),
            listOf(StrokePoint(184f, 241f), StrokePoint(198f, 247f), StrokePoint(216f, 256f)),
            listOf(StrokePoint(190f, 242f), StrokePoint(205f, 249f), StrokePoint(222f, 260f)),
            listOf(StrokePoint(186f, 245f), StrokePoint(196f, 254f), StrokePoint(212f, 268f)),
            listOf(StrokePoint(176f, 250f), StrokePoint(188f, 260f), StrokePoint(204f, 274f))
        )
        for (w in whiskers) {
            paths.add(PathGeom(w, isClosed = false, widthFactor = 0.55f, alpha = 0.80f))
        }

        // 5. Piercing Almond Wolf Eye (almond outline, dark eyeliner, pupil, catchlight)
        val eyeTop = listOf(
            StrokePoint(160f, 198f), // Inner canthus
            StrokePoint(167f, 194f),
            StrokePoint(176f, 198f)  // Outer canthus
        )
        val eyeBottom = listOf(
            StrokePoint(160f, 198f),
            StrokePoint(167f, 202f),
            StrokePoint(176f, 198f)
        )
        paths.add(PathGeom(eyeTop, isClosed = false, widthFactor = 1.35f))
        paths.add(PathGeom(eyeBottom, isClosed = false, widthFactor = 1.10f))

        // Iris contour and deep dark pupil
        circles.add(CircleGeom(168f, 198f, 3.4f, isFilled = false, widthFactor = 0.85f))
        circles.add(CircleGeom(168f, 198f, 2.2f, isFilled = true))
        // Specular eye catchlight (gives life and intelligence to eye)
        circles.add(CircleGeom(170f, 196.5f, 0.9f, isFilled = true))
        // Eyelid crease and brow fur shading
        lines.add(LineSegmentGeom(158f, 192f, 178f, 190f, widthFactor = 0.90f, alpha = 0.85f))
        lines.add(LineSegmentGeom(162f, 188f, 175f, 187f, widthFactor = 0.65f, alpha = 0.70f))
        lines.add(LineSegmentGeom(164f, 185f, 172f, 184f, widthFactor = 0.55f, alpha = 0.60f))

        // 6. Upright Front Wolf Ear
        val frontEarOuter = listOf(
            StrokePoint(135f, 168f),
            StrokePoint(142f, 126f), // Ear tip
            StrokePoint(164f, 162f)
        )
        paths.add(PathGeom(frontEarOuter, isClosed = false, widthFactor = 1.45f))

        // Deep inner ear shadow cavity
        val innerEarCavity = listOf(
            StrokePoint(140f, 162f),
            StrokePoint(144f, 134f),
            StrokePoint(156f, 160f)
        )
        paths.add(PathGeom(innerEarCavity, isClosed = false, widthFactor = 0.95f))
        for (eh in 1..5) {
            val t = eh / 6f
            lines.add(LineSegmentGeom(140f + t * 4f, 162f - t * 26f, 148f + t * 6f, 161f - t * 15f, widthFactor = 0.55f, alpha = 0.75f))
        }

        // Soft feathery inner ear fur tufts protruding forward
        val earTufts = listOf(
            listOf(StrokePoint(146f, 160f), StrokePoint(154f, 150f), StrokePoint(162f, 154f)),
            listOf(StrokePoint(148f, 155f), StrokePoint(156f, 144f), StrokePoint(165f, 148f)),
            listOf(StrokePoint(143f, 148f), StrokePoint(150f, 138f), StrokePoint(158f, 142f))
        )
        for (et in earTufts) {
            paths.add(PathGeom(et, isClosed = false, widthFactor = 0.85f, alpha = 0.85f))
        }

        // Back ear in perspective behind crown
        val backEar = listOf(
            StrokePoint(116f, 162f),
            StrokePoint(120f, 134f),
            StrokePoint(132f, 160f)
        )
        paths.add(PathGeom(backEar, isClosed = false, widthFactor = 1.05f))
        lines.add(LineSegmentGeom(120f, 138f, 126f, 158f, widthFactor = 0.6f, alpha = 0.7f))

        // 7. Cheek Fur Ruff & Volumetric Mane Locks
        val cheekRuff = listOf(
            listOf(StrokePoint(164f, 252f), StrokePoint(146f, 264f), StrokePoint(155f, 260f)),
            listOf(StrokePoint(155f, 260f), StrokePoint(134f, 278f), StrokePoint(146f, 274f)),
            listOf(StrokePoint(146f, 274f), StrokePoint(122f, 302f), StrokePoint(136f, 296f)),
            listOf(StrokePoint(136f, 296f), StrokePoint(108f, 328f), StrokePoint(124f, 320f)),
            listOf(StrokePoint(124f, 320f), StrokePoint(92f, 356f), StrokePoint(110f, 348f)),
            listOf(StrokePoint(110f, 348f), StrokePoint(78f, 384f), StrokePoint(98f, 376f))
        )
        for (lock in cheekRuff) {
            paths.add(PathGeom(lock, isClosed = false, widthFactor = 1.25f))
        }

        // Back neck crest
        val neckCrest = listOf(
            StrokePoint(116f, 162f),
            StrokePoint(98f, 198f),
            StrokePoint(106f, 194f),
            StrokePoint(86f, 238f),
            StrokePoint(96f, 234f),
            StrokePoint(74f, 280f),
            StrokePoint(86f, 276f),
            StrokePoint(65f, 330f),
            StrokePoint(78f, 326f),
            StrokePoint(60f, 380f)
        )
        paths.add(PathGeom(neckCrest, isClosed = false, widthFactor = 1.30f))

        // 8. Volumetric Directional Fur Shading (Dense realistic cross-hatching)
        val furHatchings = listOf(
            // Muzzle bridge fur
            Pair(StrokePoint(176f, 206f), StrokePoint(184f, 216f)),
            Pair(StrokePoint(172f, 210f), StrokePoint(180f, 220f)),
            Pair(StrokePoint(168f, 216f), StrokePoint(176f, 226f)),
            Pair(StrokePoint(164f, 222f), StrokePoint(172f, 232f)),
            Pair(StrokePoint(160f, 228f), StrokePoint(168f, 238f)),
            Pair(StrokePoint(156f, 234f), StrokePoint(164f, 244f)),
            // Cheek volumetric shading
            Pair(StrokePoint(152f, 248f), StrokePoint(140f, 262f)),
            Pair(StrokePoint(146f, 254f), StrokePoint(134f, 268f)),
            Pair(StrokePoint(140f, 262f), StrokePoint(128f, 278f)),
            Pair(StrokePoint(134f, 270f), StrokePoint(122f, 286f)),
            Pair(StrokePoint(126f, 282f), StrokePoint(114f, 298f)),
            Pair(StrokePoint(120f, 292f), StrokePoint(106f, 310f)),
            Pair(StrokePoint(112f, 304f), StrokePoint(98f, 322f)),
            Pair(StrokePoint(104f, 318f), StrokePoint(90f, 336f)),
            Pair(StrokePoint(96f, 332f), StrokePoint(82f, 352f)),
            // Neck ruff cross-hatches
            Pair(StrokePoint(110f, 212f), StrokePoint(100f, 226f)),
            Pair(StrokePoint(102f, 228f), StrokePoint(92f, 242f)),
            Pair(StrokePoint(96f, 246f), StrokePoint(84f, 262f)),
            Pair(StrokePoint(88f, 264f), StrokePoint(76f, 280f)),
            Pair(StrokePoint(82f, 286f), StrokePoint(70f, 304f)),
            Pair(StrokePoint(76f, 310f), StrokePoint(64f, 330f))
        )
        for ((p1, p2) in furHatchings) {
            lines.add(LineSegmentGeom(p1.x, p1.y, p2.x, p2.y, widthFactor = 0.60f, alpha = 0.70f))
        }

        return GeneratedOrnaments(lines, circles, polygons, paths)
    }

    fun generateRavenGuardian(cx: Float = CENTER_X, cy: Float = CENTER_Y, strokeWidth: Float = 3f): GeneratedOrnaments {
        val lines = mutableListOf<LineSegmentGeom>()
        val circles = mutableListOf<CircleGeom>()
        val polygons = mutableListOf<PolygonGeom>()
        val paths = mutableListOf<PathGeom>()

        // Realistic raven portrait on right side facing left toward stele:
        // 1. Heavy Arched Raven Bill (Culmen, Hooked Tip, Mouth Commissure, Lower Mandible)
        val beakCulmen = listOf(
            StrokePoint(344f, 190f), // Bill base at forehead
            StrokePoint(326f, 202f),
            StrokePoint(308f, 218f),
            StrokePoint(292f, 236f)  // Sharp hooked bill tip
        )
        paths.add(PathGeom(beakCulmen, isClosed = false, widthFactor = 1.55f))

        val mouthSlit = listOf(
            StrokePoint(292f, 236f), // Tip
            StrokePoint(312f, 233f),
            StrokePoint(336f, 231f),
            StrokePoint(350f, 233f)  // Gape / corner of mouth
        )
        paths.add(PathGeom(mouthSlit, isClosed = false, widthFactor = 1.30f))

        val lowerBeak = listOf(
            StrokePoint(292f, 236f),
            StrokePoint(314f, 244f),
            StrokePoint(336f, 246f),
            StrokePoint(348f, 248f)
        )
        paths.add(PathGeom(lowerBeak, isClosed = false, widthFactor = 1.35f))

        // Glossy specular highlight along upper ridge of bill (creates solid 3D horn/keratin volume)
        lines.add(LineSegmentGeom(338f, 196f, 322f, 208f, widthFactor = 0.90f, alpha = 0.90f))
        lines.add(LineSegmentGeom(322f, 208f, 306f, 222f, widthFactor = 0.75f, alpha = 0.85f))
        lines.add(LineSegmentGeom(306f, 222f, 296f, 234f, widthFactor = 0.60f, alpha = 0.80f))

        // Bill underside shading
        lines.add(LineSegmentGeom(334f, 243f, 316f, 241f, widthFactor = 0.65f, alpha = 0.70f))
        lines.add(LineSegmentGeom(316f, 241f, 300f, 237f, widthFactor = 0.55f, alpha = 0.65f))

        // 2. Nasal Feather Bristles (Nares covering at base of bill)
        val nasalBristles = listOf(
            listOf(StrokePoint(340f, 194f), StrokePoint(328f, 206f)),
            listOf(StrokePoint(342f, 198f), StrokePoint(330f, 210f)),
            listOf(StrokePoint(344f, 202f), StrokePoint(332f, 214f)),
            listOf(StrokePoint(346f, 206f), StrokePoint(334f, 218f)),
            listOf(StrokePoint(348f, 210f), StrokePoint(336f, 222f))
        )
        for (bristle in nasalBristles) {
            lines.add(LineSegmentGeom(bristle[0].x, bristle[0].y, bristle[1].x, bristle[1].y, widthFactor = 0.75f, alpha = 0.85f))
        }

        // 3. Intelligent Raven Eye
        val eyeCx = 356f
        val eyeCy = 208f
        // Orbital eye ring (naked skin ring)
        circles.add(CircleGeom(eyeCx, eyeCy, 6.8f, isFilled = false, widthFactor = 1.15f))
        // Iris & Pupil
        circles.add(CircleGeom(eyeCx, eyeCy, 4.2f, isFilled = true))
        // Specular glint catchlight
        circles.add(CircleGeom(eyeCx - 1.2f, eyeCy - 1.2f, 1.2f, isFilled = true))
        // Eyelid creases and surrounding fine feathers
        lines.add(LineSegmentGeom(eyeCx - 9f, eyeCy - 4f, eyeCx + 9f, eyeCy - 5f, widthFactor = 0.75f, alpha = 0.80f))
        lines.add(LineSegmentGeom(eyeCx - 8f, eyeCy + 5f, eyeCx + 9f, eyeCy + 4f, widthFactor = 0.70f, alpha = 0.75f))

        // 4. Crown, Forehead & Nape Plumage
        val crownNape = listOf(
            StrokePoint(344f, 190f),
            StrokePoint(370f, 170f), // Crown
            StrokePoint(404f, 174f),
            StrokePoint(428f, 195f), // Nape
            StrokePoint(440f, 230f)
        )
        paths.add(PathGeom(crownNape, isClosed = false, widthFactor = 1.40f))

        // 5. Lanceolate Throat Hackles (Iconic long pointed raven feathers)
        val throatHackles = listOf(
            listOf(StrokePoint(348f, 248f), StrokePoint(330f, 272f), StrokePoint(346f, 266f)),
            listOf(StrokePoint(346f, 266f), StrokePoint(336f, 300f), StrokePoint(356f, 290f)),
            listOf(StrokePoint(356f, 290f), StrokePoint(352f, 332f), StrokePoint(375f, 318f)),
            listOf(StrokePoint(375f, 318f), StrokePoint(380f, 364f), StrokePoint(404f, 344f)),
            listOf(StrokePoint(404f, 344f), StrokePoint(415f, 396f), StrokePoint(434f, 374f))
        )
        for (hackle in throatHackles) {
            paths.add(PathGeom(hackle, isClosed = false, widthFactor = 1.30f))
        }

        // 6. Mantle Feathers along back of neck
        val mantleFeathers = listOf(
            listOf(StrokePoint(440f, 230f), StrokePoint(448f, 272f), StrokePoint(436f, 266f)),
            listOf(StrokePoint(436f, 266f), StrokePoint(445f, 312f), StrokePoint(430f, 304f)),
            listOf(StrokePoint(430f, 304f), StrokePoint(436f, 356f), StrokePoint(418f, 344f)),
            listOf(StrokePoint(418f, 344f), StrokePoint(424f, 400f), StrokePoint(402f, 386f))
        )
        for (mf in mantleFeathers) {
            paths.add(PathGeom(mf, isClosed = false, widthFactor = 1.25f))
        }

        // 7. Volumetric Feather Shading & Glossy Sheen Barbs
        val featherHatches = listOf(
            // Crown barbs
            Pair(StrokePoint(370f, 184f), StrokePoint(382f, 196f)),
            Pair(StrokePoint(380f, 188f), StrokePoint(394f, 202f)),
            Pair(StrokePoint(392f, 194f), StrokePoint(406f, 208f)),
            Pair(StrokePoint(404f, 204f), StrokePoint(418f, 220f)),
            // Cheek & ear covert feather volume
            Pair(StrokePoint(364f, 224f), StrokePoint(378f, 238f)),
            Pair(StrokePoint(372f, 230f), StrokePoint(386f, 246f)),
            Pair(StrokePoint(382f, 238f), StrokePoint(398f, 256f)),
            // Throat hackle deep charcoal cross-hatching
            Pair(StrokePoint(354f, 256f), StrokePoint(342f, 270f)),
            Pair(StrokePoint(360f, 262f), StrokePoint(348f, 278f)),
            Pair(StrokePoint(366f, 280f), StrokePoint(352f, 298f)),
            Pair(StrokePoint(374f, 286f), StrokePoint(360f, 306f)),
            Pair(StrokePoint(382f, 310f), StrokePoint(368f, 330f)),
            Pair(StrokePoint(392f, 318f), StrokePoint(378f, 340f)),
            Pair(StrokePoint(402f, 342f), StrokePoint(388f, 364f)),
            // Nape & mantle shading
            Pair(StrokePoint(416f, 244f), StrokePoint(432f, 260f)),
            Pair(StrokePoint(412f, 280f), StrokePoint(428f, 298f)),
            Pair(StrokePoint(406f, 318f), StrokePoint(422f, 338f))
        )
        for ((p1, p2) in featherHatches) {
            lines.add(LineSegmentGeom(p1.x, p1.y, p2.x, p2.y, widthFactor = 0.65f, alpha = 0.70f))
        }

        return GeneratedOrnaments(lines, circles, polygons, paths)
    }

    fun generateRunicStele(cx: Float = CENTER_X, cy: Float = CENTER_Y, strokeWidth: Float = 3f): GeneratedOrnaments {
        val lines = mutableListOf<LineSegmentGeom>()
        val circles = mutableListOf<CircleGeom>()
        val polygons = mutableListOf<PolygonGeom>()
        val paths = mutableListOf<PathGeom>()

        val topY = 154f
        val botY = 426f
        val peakY = 116f
        val topW = 25f
        val botW = 35f

        // 1. Drop shadow of monolith onto background
        val shadowPolygon = listOf(
            StrokePoint(cx - topW + 6f, topY + 6f),
            StrokePoint(cx + topW + 6f, topY + 6f),
            StrokePoint(cx + botW + 6f, botY + 6f),
            StrokePoint(cx - botW + 6f, botY + 6f)
        )
        polygons.add(PolygonGeom(shadowPolygon, isFilled = true, alpha = 0.22f))

        // 2. Main Stone Monolith Boundaries
        // Left pillar boundary line
        lines.add(LineSegmentGeom(cx - topW, topY, cx - botW, botY, widthFactor = 1.40f))
        // Right pillar boundary line
        lines.add(LineSegmentGeom(cx + topW, topY, cx + botW, botY, widthFactor = 1.40f))

        // Pyramidal stone top cap (faceted peak)
        lines.add(LineSegmentGeom(cx - topW, topY, cx, peakY, widthFactor = 1.40f))
        lines.add(LineSegmentGeom(cx + topW, topY, cx, peakY, widthFactor = 1.40f))
        // Center ridge of pyramid cap
        lines.add(LineSegmentGeom(cx, peakY, cx, topY, widthFactor = 1.25f))
        // Spire needle extending upward along northern ray
        lines.add(LineSegmentGeom(cx, peakY, cx, 50f, widthFactor = 1.50f))

        // 3. Multi-tiered Pedestal Base (chiseled stone steps)
        lines.add(LineSegmentGeom(cx - botW - 8f, botY, cx + botW + 8f, botY, widthFactor = 1.40f))
        lines.add(LineSegmentGeom(cx - botW - 16f, botY + 12f, cx + botW + 16f, botY + 12f, widthFactor = 1.40f))
        lines.add(LineSegmentGeom(cx - botW - 22f, botY + 24f, cx + botW + 22f, botY + 24f, widthFactor = 1.40f))

        lines.add(LineSegmentGeom(cx - botW, botY, cx - botW - 16f, botY + 12f, widthFactor = 1.10f))
        lines.add(LineSegmentGeom(cx + botW, botY, cx + botW + 16f, botY + 12f, widthFactor = 1.10f))
        lines.add(LineSegmentGeom(cx - botW - 16f, botY + 12f, cx - botW - 22f, botY + 24f, widthFactor = 1.10f))
        lines.add(LineSegmentGeom(cx + botW + 16f, botY + 12f, cx + botW + 22f, botY + 24f, widthFactor = 1.10f))

        // 4. Chamfered / Beveled Side Facets with directional stone shading
        // Left bevel (lit edge)
        lines.add(LineSegmentGeom(cx - topW + 6f, topY + 4f, cx - botW + 7f, botY - 4f, widthFactor = 0.65f, alpha = 0.55f))
        // Right bevel (shadow edge)
        lines.add(LineSegmentGeom(cx + topW - 6f, topY + 4f, cx + botW - 7f, botY - 4f, widthFactor = 0.75f, alpha = 0.75f))

        // Stone texture: fine vertical striations and chisel cross-hatching
        for (step in 1..10) {
            val sy = topY + step * ((botY - topY) / 11f)
            val sw = topW + (botW - topW) * (sy - topY) / (botY - topY)
            // Left facet chisel notches
            lines.add(LineSegmentGeom(cx - sw, sy, cx - sw + 7f, sy - 2f, widthFactor = 0.60f, alpha = 0.65f))
            // Right facet dense chisel shading
            lines.add(LineSegmentGeom(cx + sw - 8f, sy - 2f, cx + sw, sy, widthFactor = 0.70f, alpha = 0.75f))
            lines.add(LineSegmentGeom(cx + sw - 8f, sy + 2f, cx + sw, sy, widthFactor = 0.55f, alpha = 0.65f))
        }

        // Stone grain vertical striations
        lines.add(LineSegmentGeom(cx - 15f, topY + 15f, cx - 18f, botY - 15f, widthFactor = 0.45f, alpha = 0.35f))
        lines.add(LineSegmentGeom(cx + 15f, topY + 15f, cx + 18f, botY - 15f, widthFactor = 0.45f, alpha = 0.35f))

        return GeneratedOrnaments(lines, circles, polygons, paths)
    }

    fun generateBeastsOfOdin(cx: Float = CENTER_X, cy: Float = CENTER_Y, strokeWidth: Float = 3f): GeneratedOrnaments {
        val lines = mutableListOf<LineSegmentGeom>()
        val circles = mutableListOf<CircleGeom>()
        val polygons = mutableListOf<PolygonGeom>()
        val paths = mutableListOf<PathGeom>()

        // 1. Background Celtic Medallion Lattice
        val lattice = generateCelticLattice(cx, cy, strokeWidth)
        lines.addAll(lattice.lines)
        circles.addAll(lattice.circles)
        polygons.addAll(lattice.polygons)
        paths.addAll(lattice.paths)

        // 2. Dramatic 3D Faceted Compass Star
        val star = generateFacetedStar(cx, cy, strokeWidth)
        lines.addAll(star.lines)
        circles.addAll(star.circles)
        polygons.addAll(star.polygons)
        paths.addAll(star.paths)

        // 3. Central 3D Runic Stele Monolith
        val stele = generateRunicStele(cx, cy, strokeWidth)
        lines.addAll(stele.lines)
        circles.addAll(stele.circles)
        polygons.addAll(stele.polygons)
        paths.addAll(stele.paths)

        // 4. Lifelike Wolf Guardian (Left)
        val wolf = generateWolfGuardian(cx, cy, strokeWidth)
        lines.addAll(wolf.lines)
        circles.addAll(wolf.circles)
        polygons.addAll(wolf.polygons)
        paths.addAll(wolf.paths)

        // 5. Lifelike Raven Guardian (Right)
        val raven = generateRavenGuardian(cx, cy, strokeWidth)
        lines.addAll(raven.lines)
        circles.addAll(raven.circles)
        polygons.addAll(raven.polygons)
        paths.addAll(raven.paths)

        return GeneratedOrnaments(lines, circles, polygons, paths)
    }
}
