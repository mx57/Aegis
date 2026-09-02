package com.example.engine

import com.example.data.model.Rune
import com.example.data.model.RuneStroke
import com.example.data.model.StrokePoint
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class StaveLayoutType(val titleRu: String) {
    BINDRUNE("Связка (Биндруна)"),
    ROW("В ряд"),
    CIRCLE("Круговой став"),
    MIRROR("Зеркальный"),
    VEGVISIR("Вегвизир (Компас)"),
    AEGISHJALMUR("Шлем Ужаса"),
    CROSS_STAVE("Крестовой став")
}

data class RenderStroke(
    val points: List<StrokePoint>,
    val isStem: Boolean = false,
    val isOuterPole: Boolean = false,
    val isHairlineGuide: Boolean = false
)

data class ComposedStave(
    val runes: List<Rune>,
    val layoutType: StaveLayoutType,
    val strokes: List<RenderStroke>,
    val width: Float = 500f,
    val height: Float = 500f
)

object StaveComposer {

    /**
     * Composes given runes into a 500x500 normalized canvas based on layout type and seed.
     * Seed provides rich structural variations of the stave arrangement.
     */
    fun compose(runes: List<Rune>, layoutType: StaveLayoutType, seed: Long = 0L): ComposedStave {
        if (runes.isEmpty()) {
            return ComposedStave(emptyList(), layoutType, emptyList())
        }

        val strokes = when (layoutType) {
            StaveLayoutType.ROW -> composeRow(runes, seed)
            StaveLayoutType.BINDRUNE -> composeBindrune(runes, seed)
            StaveLayoutType.CIRCLE -> composeCircle(runes, seed)
            StaveLayoutType.MIRROR -> composeMirror(runes, seed)
            StaveLayoutType.VEGVISIR -> composeVegvisir(runes, seed)
            StaveLayoutType.AEGISHJALMUR -> composeAegishjalmur(runes, seed)
            StaveLayoutType.CROSS_STAVE -> composeCrossStave(runes, seed)
        }

        return ComposedStave(
            runes = runes,
            layoutType = layoutType,
            strokes = strokes,
            width = 500f,
            height = 500f
        )
    }

    private fun composeRow(runes: List<Rune>, seed: Long): List<RenderStroke> {
        val result = mutableListOf<RenderStroke>()
        val count = runes.size
        val runeWidth = 84f
        val runeHeight = 120f
        val spacing = 22f

        val totalWidth = count * runeWidth + (count - 1) * spacing
        val maxAvailableWidth = 420f
        val scale = if (totalWidth > maxAvailableWidth) maxAvailableWidth / totalWidth else 1.0f

        val effectiveRuneW = runeWidth * scale
        val effectiveRuneH = runeHeight * scale
        val effectiveSpacing = spacing * scale
        val effectiveTotalW = count * effectiveRuneW + (count - 1) * effectiveSpacing

        val startX = (500f - effectiveTotalW) / 2f
        val startY = (500f - effectiveRuneH) / 2f

        val variation = (Math.abs(seed) % 3).toInt()

        // Upper and lower archival stone carving boundary rails
        val railY1 = startY - 14f
        val railY2 = startY + effectiveRuneH + 14f
        val railX1 = (startX - 24f).coerceAtLeast(35f)
        val railX2 = (startX + effectiveTotalW + 24f).coerceAtMost(465f)

        result.add(RenderStroke(listOf(StrokePoint(railX1, railY1), StrokePoint(railX2, railY1)), isStem = true))
        result.add(RenderStroke(listOf(StrokePoint(railX1, railY2), StrokePoint(railX2, railY2)), isStem = true))
        // End finials on boundary rails
        result.add(RenderStroke(listOf(StrokePoint(railX1, railY1 - 9f), StrokePoint(railX1, railY1 + 9f)), isOuterPole = true))
        result.add(RenderStroke(listOf(StrokePoint(railX2, railY1 - 9f), StrokePoint(railX2, railY1 + 9f)), isOuterPole = true))
        result.add(RenderStroke(listOf(StrokePoint(railX1, railY2 - 9f), StrokePoint(railX1, railY2 + 9f)), isOuterPole = true))
        result.add(RenderStroke(listOf(StrokePoint(railX2, railY2 - 9f), StrokePoint(railX2, railY2 + 9f)), isOuterPole = true))

        runes.forEachIndexed { index, rune ->
            val offsetX = startX + index * (effectiveRuneW + effectiveSpacing)
            val waveOffset = if (variation == 2) sin(index * 1.3).toFloat() * 12f else 0f
            val offsetY = startY + waveOffset

            // Inter-rune sacred separating dots (•) as on historic runestones
            if (index > 0) {
                val dotX = offsetX - effectiveSpacing / 2f
                val dotY = startY + effectiveRuneH / 2f
                result.add(RenderStroke(listOf(StrokePoint(dotX, dotY - 4f), StrokePoint(dotX, dotY + 4f))))
            }

            for (stroke in rune.strokes) {
                val scaledPoints = stroke.points.map { pt ->
                    StrokePoint(
                        x = offsetX + (pt.x / 100f) * effectiveRuneW,
                        y = offsetY + (pt.y / 140f) * effectiveRuneH
                    )
                }
                result.add(RenderStroke(scaledPoints))
            }
        }

        return result
    }

    private fun composeBindrune(runes: List<Rune>, seed: Long): List<RenderStroke> {
        val result = mutableListOf<RenderStroke>()
        val centerX = 250f
        val centerY = 250f
        val count = runes.size

        val variation = (Math.abs(seed) % 5).toInt()

        when (variation) {
            1 -> {
                // Variation 1: Crux Sacra (Sacred Rune Cross)
                val stemTop = 75f
                val stemBottom = 425f
                val armLeft = 75f
                val armRight = 425f

                // Master Cross Stems - Only endpoints are outer poles!
                result.add(RenderStroke(listOf(StrokePoint(centerX, stemTop), StrokePoint(centerX, stemBottom)), isStem = true, isOuterPole = true))
                result.add(RenderStroke(listOf(StrokePoint(armLeft, centerY), StrokePoint(armRight, centerY)), isStem = true, isOuterPole = true))

                // Central sacred solar ring
                val ringPts = mutableListOf<StrokePoint>()
                for (i in 0..16) {
                    val a = (2 * PI * i / 16).toFloat()
                    ringPts.add(StrokePoint(centerX + 32f * cos(a), centerY + 32f * sin(a)))
                }
                result.add(RenderStroke(ringPts, isHairlineGuide = true))

                // Distribute runes cleanly along the 4 cardinal arms with ample breathing room
                val armLen = 100f
                runes.forEachIndexed { idx, rune ->
                    val arm = idx % 4
                    val (baseX, baseY, isHoriz, dir) = when (arm) {
                        0 -> Quad(centerX, centerY - 80f - (idx / 4) * 35f, false, -1f) // North
                        1 -> Quad(centerX, centerY + 80f + (idx / 4) * 35f, false, 1f)  // South
                        2 -> Quad(centerX - 80f - (idx / 4) * 35f, centerY, true, -1f)  // West
                        else -> Quad(centerX + 80f + (idx / 4) * 35f, centerY, true, 1f) // East
                    }

                    val runeSize = 65f
                    for (stroke in rune.strokes) {
                        // Skip duplicate vertical stems that would just trace over the master arm
                        if (isVerticalStroke(stroke) && (arm == 0 || arm == 1)) continue

                        val mapped = stroke.points.map { pt ->
                            if (!isHoriz) {
                                val relX = (pt.x - 50f) / 50f * (runeSize * 0.65f)
                                val relY = (pt.y - 70f) / 70f * (runeSize * 0.5f)
                                StrokePoint(baseX + relX, baseY + relY)
                            } else {
                                val relX = (pt.y - 70f) / 70f * (runeSize * 0.5f) * dir
                                val relY = (pt.x - 50f) / 50f * (runeSize * 0.65f)
                                StrokePoint(baseX + relX, baseY + relY)
                            }
                        }
                        result.add(RenderStroke(mapped))
                    }
                }
            }

            2 -> {
                // Variation 2: Aegis Inguz (Sacred Diamond Sigil / Lozenge Frame)
                val hw = 125f
                val hh = 155f
                val diamond = listOf(
                    StrokePoint(centerX, centerY - hh),
                    StrokePoint(centerX + hw, centerY),
                    StrokePoint(centerX, centerY + hh),
                    StrokePoint(centerX - hw, centerY),
                    StrokePoint(centerX, centerY - hh)
                )
                result.add(RenderStroke(diamond, isStem = true))

                // Delicate concentric inner diamond
                val innerDiamond = listOf(
                    StrokePoint(centerX, centerY - hh * 0.72f),
                    StrokePoint(centerX + hw * 0.72f, centerY),
                    StrokePoint(centerX, centerY + hh * 0.72f),
                    StrokePoint(centerX - hw * 0.72f, centerY),
                    StrokePoint(centerX, centerY - hh * 0.72f)
                )
                result.add(RenderStroke(innerDiamond, isHairlineGuide = true))

                // Master central vertical spine
                val stemTop = 60f
                val stemBottom = 440f
                result.add(RenderStroke(listOf(StrokePoint(centerX, stemTop), StrokePoint(centerX, stemBottom)), isStem = true, isOuterPole = true))

                // Integrate rune branches gracefully inside the diamond
                renderTieredBindruneBranches(runes, centerX, centerY, 190f, 130f, result)
            }

            3 -> {
                // Variation 3: Star of Odin / Solar Hexagram Sanctuary
                val r = 135f
                // Upward triangle
                val tri1 = listOf(
                    StrokePoint(centerX, centerY - r),
                    StrokePoint(centerX + r * cos(PI.toFloat() / 6f), centerY + r * sin(PI.toFloat() / 6f)),
                    StrokePoint(centerX - r * cos(PI.toFloat() / 6f), centerY + r * sin(PI.toFloat() / 6f)),
                    StrokePoint(centerX, centerY - r)
                )
                // Downward triangle
                val tri2 = listOf(
                    StrokePoint(centerX, centerY + r),
                    StrokePoint(centerX + r * cos(PI.toFloat() / 6f), centerY - r * sin(PI.toFloat() / 6f)),
                    StrokePoint(centerX - r * cos(PI.toFloat() / 6f), centerY - r * sin(PI.toFloat() / 6f)),
                    StrokePoint(centerX, centerY + r)
                )
                result.add(RenderStroke(tri1, isHairlineGuide = true))
                result.add(RenderStroke(tri2, isHairlineGuide = true))

                // Master vertical spine
                val stemTop = 65f
                val stemBottom = 435f
                result.add(RenderStroke(listOf(StrokePoint(centerX, stemTop), StrokePoint(centerX, stemBottom)), isStem = true, isOuterPole = true))

                renderTieredBindruneBranches(runes, centerX, centerY, 210f, 140f, result)
            }

            4 -> {
                // Variation 4: Twin Pillars of Valhalla (Dual Symmetrical Architecture)
                val stemLeft = centerX - 60f
                val stemRight = centerX + 60f
                val topY = 75f
                val botY = 425f

                // Two noble pillars - only outer tips have poles
                result.add(RenderStroke(listOf(StrokePoint(stemLeft, topY), StrokePoint(stemLeft, botY)), isStem = true, isOuterPole = true))
                result.add(RenderStroke(listOf(StrokePoint(stemRight, topY), StrokePoint(stemRight, botY)), isStem = true, isOuterPole = true))

                // Connective architectural lintels
                result.add(RenderStroke(listOf(StrokePoint(stemLeft, 150f), StrokePoint(stemRight, 150f)), isStem = true))
                result.add(RenderStroke(listOf(StrokePoint(stemLeft, 250f), StrokePoint(stemRight, 250f)), isStem = true))
                result.add(RenderStroke(listOf(StrokePoint(stemLeft, 350f), StrokePoint(stemRight, 350f)), isStem = true))

                // Elegant X-brace in the center
                result.add(RenderStroke(listOf(StrokePoint(stemLeft, 175f), StrokePoint(stemRight, 325f)), isHairlineGuide = true))
                result.add(RenderStroke(listOf(StrokePoint(stemLeft, 325f), StrokePoint(stemRight, 175f)), isHairlineGuide = true))

                val runeH = 110f
                val runeW = 75f

                runes.forEachIndexed { index, rune ->
                    val isLeft = index % 2 == 0
                    val axisX = if (isLeft) stemLeft else stemRight
                    val yOffset = 135f + (index / 2) * 95f
                    val flipX = if (isLeft) -1f else 1f

                    for (stroke in rune.strokes) {
                        // Skip the vertical stem that is already represented by the pillar
                        if (isVerticalStroke(stroke)) continue

                        val mapped = stroke.points.map { pt ->
                            val relX = (pt.x - 50f) / 50f * (runeW * 0.65f) * flipX
                            val relY = (pt.y - 70f) / 70f * (runeH * 0.5f)
                            StrokePoint(axisX + relX, yOffset + relY)
                        }
                        result.add(RenderStroke(mapped))
                    }
                }
            }

            else -> {
                // Variation 0: Imperial Monogram (Monogramma Aurea) - Supreme Royal Norse Sigil
                val stemTop = 70f
                val stemBottom = 430f

                // Master Central Spine with royal outer poles only at the very ends
                result.add(RenderStroke(listOf(StrokePoint(centerX, stemTop), StrokePoint(centerX, stemBottom)), isStem = true, isOuterPole = true))

                // Sacred concentric midpoint rings (anchor of power)
                val coreR = 28f
                val corePts = mutableListOf<StrokePoint>()
                for (i in 0..16) {
                    val a = (2 * PI * i / 16).toFloat()
                    corePts.add(StrokePoint(centerX + coreR * cos(a), centerY + coreR * sin(a)))
                }
                result.add(RenderStroke(corePts, isHairlineGuide = true))

                // Tiered, non-overlapping rune branch placement
                renderTieredBindruneBranches(runes, centerX, centerY, 240f, 150f, result)
            }
        }

        return result
    }

    /**
     * Helper to gracefully map distinct rune branches to vertical tiers along the master spine,
     * preventing collision, overlapping clumping, and chaotic antenna effects.
     */
    private fun renderTieredBindruneBranches(
        runes: List<Rune>,
        centerX: Float,
        centerY: Float,
        spanH: Float,
        spanW: Float,
        result: MutableList<RenderStroke>
    ) {
        val count = runes.size
        if (count == 0) return

        runes.forEachIndexed { index, rune ->
            // Calculate distinct, harmonious vertical tier
            val tierY = if (count == 1) {
                centerY
            } else {
                val step = spanH / (count - 1).coerceAtLeast(1)
                (centerY - spanH / 2f) + index * step
            }

            // Symmetrical / alternating branching balance
            val isSymmetricRune = rune.id in listOf("algiz", "tiwaz", "dagaz", "inguz", "gebo", "mannaz", "sowilo")
            val flipX = if (!isSymmetricRune && count > 1 && index % 2 == 1) -1f else 1f

            val runeH = 85f
            val runeW = spanW

            for (stroke in rune.strokes) {
                // Check if this is the vertical spine of the rune
                if (isVerticalStroke(stroke)) {
                    // For runes whose whole identity is the stem (Isa) or symmetric runes, skip drawing duplicate vertical lines
                    continue
                }

                // Map branch strokes so their base anchors seamlessly onto centerX
                val mapped = stroke.points.map { pt ->
                    val relX = ((pt.x - 50f) / 50f) * (runeW * 0.55f) * flipX
                    val relY = ((pt.y - 70f) / 70f) * (runeH * 0.50f)
                    StrokePoint(centerX + relX, tierY + relY)
                }
                result.add(RenderStroke(mapped))
            }
        }
    }

    private fun isVerticalStroke(stroke: RuneStroke): Boolean {
        if (stroke.points.size < 2) return false
        val p1 = stroke.points.first()
        val p2 = stroke.points.last()
        val dx = Math.abs(p1.x - p2.x)
        val dy = Math.abs(p1.y - p2.y)
        return dx <= 8f && dy >= 65f
    }

    private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

    private fun composeCircle(runes: List<Rune>, seed: Long): List<RenderStroke> {
        val result = mutableListOf<RenderStroke>()
        val centerX = 250f
        val centerY = 250f
        val count = runes.size
        val angleStep = (2 * PI / count).toFloat()

        val variation = (Math.abs(seed) % 3).toInt()

        // Center sacred mark
        when (variation) {
            1 -> {
                // Radiating Sun Spoke Wheel
                val spokeRadius = 30f
                for (i in 0 until 8) {
                    val a = (2 * PI * i / 8).toFloat()
                    result.add(RenderStroke(listOf(StrokePoint(centerX, centerY), StrokePoint(centerX + spokeRadius * cos(a), centerY + spokeRadius * sin(a)))))
                }
            }
            2 -> {
                // Central Sacred Triangle
                val triR = 24f
                val tri = listOf(
                    StrokePoint(centerX, centerY - triR),
                    StrokePoint(centerX + triR * cos(PI.toFloat() / 6f), centerY + triR * sin(PI.toFloat() / 6f)),
                    StrokePoint(centerX - triR * cos(PI.toFloat() / 6f), centerY + triR * sin(PI.toFloat() / 6f)),
                    StrokePoint(centerX, centerY - triR)
                )
                result.add(RenderStroke(tri))
            }
            else -> {
                // Solar Cross
                result.add(RenderStroke(listOf(StrokePoint(centerX - 22f, centerY), StrokePoint(centerX + 22f, centerY))))
                result.add(RenderStroke(listOf(StrokePoint(centerX, centerY - 22f), StrokePoint(centerX, centerY + 22f))))
                result.add(RenderStroke(listOf(StrokePoint(centerX - 12f, centerY - 12f), StrokePoint(centerX + 12f, centerY + 12f))))
                result.add(RenderStroke(listOf(StrokePoint(centerX - 12f, centerY + 12f), StrokePoint(centerX + 12f, centerY - 12f))))
            }
        }

        runes.forEachIndexed { index, rune ->
            val angle = -PI.toFloat() / 2f + index * angleStep
            val cosA = cos(angle)
            val sinA = sin(angle)

            val baseRadius = 145f
            // Variation 2: Oscillating star distance
            val radius = if (variation == 2) {
                if (index % 2 == 0) baseRadius + 18f else baseRadius - 18f
            } else baseRadius

            val runeW = 60f
            val runeH = 84f

            val rotAngle = if (variation == 1) angle - PI.toFloat() / 2f else angle + PI.toFloat() / 2f
            val cosRot = cos(rotAngle)
            val sinRot = sin(rotAngle)

            val runeCenterX = centerX + radius * cosA
            val runeCenterY = centerY + radius * sinA

            // Spoke ray from center to rune in variation 1
            if (variation == 1) {
                result.add(RenderStroke(listOf(StrokePoint(centerX + 35f * cosA, centerY + 35f * sinA), StrokePoint(runeCenterX - 25f * cosA, runeCenterY - 25f * sinA))))
            }

            for (stroke in rune.strokes) {
                val rotatedPoints = stroke.points.map { pt ->
                    val localX = (pt.x - 50f) / 100f * runeW
                    val localY = (pt.y - 70f) / 140f * runeH
                    val rx = localX * cosRot - localY * sinRot
                    val ry = localX * sinRot + localY * cosRot
                    StrokePoint(runeCenterX + rx, runeCenterY + ry)
                }
                result.add(RenderStroke(rotatedPoints))
            }
        }

        return result
    }

    private fun composeMirror(runes: List<Rune>, seed: Long): List<RenderStroke> {
        val result = mutableListOf<RenderStroke>()
        val centerX = 250f
        val centerY = 250f
        val count = runes.size

        val variation = (Math.abs(seed) % 3).toInt()

        if (variation == 1) {
            // Quad-Mandala Reflection (4-Quadrant Mirror)
            result.add(RenderStroke(listOf(StrokePoint(centerX, 60f), StrokePoint(centerX, 440f)), isStem = true))
            result.add(RenderStroke(listOf(StrokePoint(60f, centerY), StrokePoint(440f, centerY)), isStem = true))

            val runeH = 75f
            val runeW = 55f

            runes.take(3).forEachIndexed { idx, rune ->
                val dist = 55f + idx * 45f
                for (stroke in rune.strokes) {
                    // 4 quadrants
                    val quads = listOf(Pair(1f, 1f), Pair(-1f, 1f), Pair(1f, -1f), Pair(-1f, -1f))
                    for ((sx, sy) in quads) {
                        val mapped = stroke.points.map { pt ->
                            val lx = ((pt.x - 50f) / 50f * (runeW / 2f) + dist) * sx
                            val ly = ((pt.y - 70f) / 70f * (runeH / 2f) + dist) * sy
                            StrokePoint(centerX + lx, centerY + ly)
                        }
                        result.add(RenderStroke(mapped))
                    }
                }
            }
            return result
        }

        // Bilateral Mirror (Variation 0 & 2)
        result.add(RenderStroke(listOf(StrokePoint(centerX, 50f), StrokePoint(centerX, 450f)), isStem = true))

        val runeH = 95f
        val runeW = 68f
        val spacingY = 16f
        val totalH = count * runeH + (count - 1) * spacingY
        val startY = (500f - totalH) / 2f

        runes.forEachIndexed { index, rune ->
            val yOffset = startY + index * (runeH + spacingY)

            for (stroke in rune.strokes) {
                // Right side
                val rightPoints = stroke.points.map { pt ->
                    val localX = (pt.x - 50f) / 50f * (runeW / 2f)
                    val x = centerX + 30f + localX
                    val y = yOffset + (pt.y / 140f) * runeH
                    StrokePoint(x, y)
                }
                result.add(RenderStroke(rightPoints))

                // Left side
                val leftPoints = stroke.points.map { pt ->
                    val localX = (pt.x - 50f) / 50f * (runeW / 2f)
                    val x = centerX - 30f - localX
                    val y = yOffset + (pt.y / 140f) * runeH
                    StrokePoint(x, y)
                }
                result.add(RenderStroke(leftPoints))

                // Connective bridges
                if (stroke == rune.strokes.firstOrNull()) {
                    val connectY = yOffset + runeH / 2f
                    result.add(RenderStroke(listOf(StrokePoint(centerX - 30f, connectY), StrokePoint(centerX + 30f, connectY))))

                    if (variation == 2) {
                        // Diamond bridge between pairs
                        val dPts = listOf(
                            StrokePoint(centerX, connectY - 14f),
                            StrokePoint(centerX + 20f, connectY),
                            StrokePoint(centerX, connectY + 14f),
                            StrokePoint(centerX - 20f, connectY),
                            StrokePoint(centerX, connectY - 14f)
                        )
                        result.add(RenderStroke(dPts))
                    }
                }
            }
        }

        return result
    }

    private fun composeVegvisir(runes: List<Rune>, seed: Long): List<RenderStroke> {
        val result = mutableListOf<RenderStroke>()
        val cx = 250f
        val cy = 250f

        // Central sacred hub
        val hubR = 24f
        val hubPts = mutableListOf<StrokePoint>()
        for (i in 0..16) {
            val a = (2 * PI * i / 16).toFloat()
            hubPts.add(StrokePoint(cx + hubR * cos(a), cy + hubR * sin(a)))
        }
        result.add(RenderStroke(hubPts))

        // 8 Compass Rays
        val rayCount = 8
        for (i in 0 until rayCount) {
            val angle = (2 * PI * i / rayCount).toFloat()
            val cosA = cos(angle)
            val sinA = sin(angle)
            val perpX = -sinA
            val perpY = cosA

            val rStart = hubR
            val rEnd = 185f

            // Main ray spine
            result.add(
                RenderStroke(
                    listOf(StrokePoint(cx + rStart * cosA, cy + rStart * sinA), StrokePoint(cx + rEnd * cosA, cy + rEnd * sinA)),
                    isStem = true
                )
            )

            // Crossbars along ray
            val barDist1 = 85f
            val barDist2 = 135f
            val b1Len = 14f
            val b2Len = 22f

            result.add(
                RenderStroke(
                    listOf(
                        StrokePoint(cx + barDist1 * cosA - perpX * b1Len, cy + barDist1 * sinA - perpY * b1Len),
                        StrokePoint(cx + barDist1 * cosA + perpX * b1Len, cy + barDist1 * sinA + perpY * b1Len)
                    )
                )
            )
            result.add(
                RenderStroke(
                    listOf(
                        StrokePoint(cx + barDist2 * cosA - perpX * b2Len, cy + barDist2 * sinA - perpY * b2Len),
                        StrokePoint(cx + barDist2 * cosA + perpX * b2Len, cy + barDist2 * sinA + perpY * b2Len)
                    )
                )
            )

            // Galdrastafir Trident Crown at tip
            val forkLen = 16f
            val forkSpread = 14f
            result.add(
                RenderStroke(
                    listOf(
                        StrokePoint(cx + rEnd * cosA - perpX * forkSpread, cy + rEnd * sinA - perpY * forkSpread),
                        StrokePoint(cx + (rEnd + forkLen) * cosA, cy + (rEnd + forkLen) * sinA)
                    )
                )
            )
            result.add(
                RenderStroke(
                    listOf(
                        StrokePoint(cx + rEnd * cosA + perpX * forkSpread, cy + rEnd * sinA + perpY * forkSpread),
                        StrokePoint(cx + (rEnd + forkLen) * cosA, cy + (rEnd + forkLen) * sinA)
                    )
                )
            )

            // Embed rune onto the ray
            val rune = runes[i % runes.size]
            val runeDist = 110f
            val runeCenterX = cx + runeDist * cosA
            val runeCenterY = cy + runeDist * sinA

            val rotAngle = angle + PI.toFloat() / 2f
            val cosRot = cos(rotAngle)
            val sinRot = sin(rotAngle)
            val rW = 38f
            val rH = 50f

            for (stroke in rune.strokes) {
                val mapped = stroke.points.map { pt ->
                    val lx = (pt.x - 50f) / 100f * rW
                    val ly = (pt.y - 70f) / 140f * rH
                    val rx = lx * cosRot - ly * sinRot
                    val ry = lx * sinRot + ly * cosRot
                    StrokePoint(runeCenterX + rx, runeCenterY + ry)
                }
                result.add(RenderStroke(mapped))
            }
        }

        return result
    }

    private fun composeAegishjalmur(runes: List<Rune>, seed: Long): List<RenderStroke> {
        val result = mutableListOf<RenderStroke>()
        val cx = 250f
        val cy = 250f

        // Central Circle
        val ringPts = mutableListOf<StrokePoint>()
        for (i in 0..16) {
            val a = (2 * PI * i / 16).toFloat()
            ringPts.add(StrokePoint(cx + 18f * cos(a), cy + 18f * sin(a)))
        }
        result.add(RenderStroke(ringPts))

        // 8 Radiating Spines with triple cross-hatches and terminal tridents
        for (i in 0 until 8) {
            val angle = (2 * PI * i / 8).toFloat()
            val cosA = cos(angle)
            val sinA = sin(angle)
            val perpX = -sinA
            val perpY = cosA

            val r1 = 18f
            val r2 = 190f
            result.add(RenderStroke(listOf(StrokePoint(cx + r1 * cosA, cy + r1 * sinA), StrokePoint(cx + r2 * cosA, cy + r2 * sinA)), isStem = true))

            // 3 chevron / crossbar protective layers
            val notches = listOf(70f, 110f, 150f)
            for (dist in notches) {
                val span = 14f
                result.add(
                    RenderStroke(
                        listOf(
                            StrokePoint(cx + dist * cosA - perpX * span, cy + dist * sinA - perpY * span),
                            StrokePoint(cx + (dist + 6f) * cosA, cy + (dist + 6f) * sinA),
                            StrokePoint(cx + dist * cosA + perpX * span, cy + dist * sinA + perpY * span)
                        )
                    )
                )
            }

            // Terminal Trident Crown
            val tipX = cx + (r2 + 16f) * cosA
            val tipY = cy + (r2 + 16f) * sinA
            val tSpan = 15f
            result.add(RenderStroke(listOf(StrokePoint(cx + r2 * cosA, cy + r2 * sinA), StrokePoint(tipX, tipY))))
            result.add(RenderStroke(listOf(StrokePoint(cx + r2 * cosA, cy + r2 * sinA), StrokePoint(cx + (r2 + 12f) * cosA + perpX * tSpan, cy + (r2 + 12f) * sinA + perpY * tSpan))))
            result.add(RenderStroke(listOf(StrokePoint(cx + r2 * cosA, cy + r2 * sinA), StrokePoint(cx + (r2 + 12f) * cosA - perpX * tSpan, cy + (r2 + 12f) * sinA - perpY * tSpan))))

            // Embed rune in alternate quadrant
            if (i < 4) {
                val rune = runes[i % runes.size]
                val midDist = 130f
                val rcX = cx + midDist * cosA + perpX * 24f
                val rcY = cy + midDist * sinA + perpY * 24f
                val rW = 34f
                val rH = 46f

                for (stroke in rune.strokes) {
                    val mapped = stroke.points.map { pt ->
                        val lx = (pt.x - 50f) / 100f * rW
                        val ly = (pt.y - 70f) / 140f * rH
                        StrokePoint(rcX + lx, rcY + ly)
                    }
                    result.add(RenderStroke(mapped))
                }
            }
        }

        return result
    }

    private fun composeCrossStave(runes: List<Rune>, seed: Long): List<RenderStroke> {
        val result = mutableListOf<RenderStroke>()
        val cx = 250f
        val cy = 250f

        // Central intersecting solar ring
        val ringPts = mutableListOf<StrokePoint>()
        for (i in 0..20) {
            val a = (2 * PI * i / 20).toFloat()
            ringPts.add(StrokePoint(cx + 65f * cos(a), cy + 65f * sin(a)))
        }
        result.add(RenderStroke(ringPts))

        // 4 Cardinal Master Stems
        val dirs = listOf(
            Pair(0f, -1f), // North
            Pair(0f, 1f),  // South
            Pair(1f, 0f),  // East
            Pair(-1f, 0f)  // West
        )

        for ((dx, dy) in dirs) {
            val px = -dy
            val py = dx

            val endX = cx + dx * 195f
            val endY = cy + dy * 195f
            result.add(RenderStroke(listOf(StrokePoint(cx + dx * 20f, cy + dy * 20f), StrokePoint(endX, endY)), isStem = true))

            // Crossbars at ring intersection
            val ringX = cx + dx * 65f
            val ringY = cy + dy * 65f
            result.add(RenderStroke(listOf(StrokePoint(ringX - px * 18f, ringY - py * 18f), StrokePoint(ringX + px * 18f, ringY + py * 18f))))

            // Arrow spearhead terminal
            result.add(RenderStroke(listOf(StrokePoint(endX, endY), StrokePoint(endX - dx * 16f + px * 14f, endY - dy * 16f + py * 14f))))
            result.add(RenderStroke(listOf(StrokePoint(endX, endY), StrokePoint(endX - dx * 16f - px * 14f, endY - dy * 16f - py * 14f))))
        }

        // Distribute runes along the 4 cardinal arms
        val runeSize = 65f
        runes.take(4).forEachIndexed { index, rune ->
            val (dx, dy) = dirs[index % 4]
            val px = -dy
            val py = dx

            val armDist = 125f
            val rx = cx + dx * armDist
            val ry = cy + dy * armDist

            for (stroke in rune.strokes) {
                val mapped = stroke.points.map { pt ->
                    val lx = (pt.x - 50f) / 100f * runeSize
                    val ly = (pt.y - 70f) / 140f * runeSize
                    // Orient along the arm
                    val mx = lx * px + ly * dx
                    val my = lx * py + ly * dy
                    StrokePoint(rx + mx, ry + my)
                }
                result.add(RenderStroke(mapped))
            }
        }

        return result
    }
}
