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
    CROSS_STAVE("Крестовой став"),
    SOLAR_12_RAY("12-лучевое Солнце"),
    STELE_OBELISK("Стела-обелиск"),
    GALDRABOK_ASYMMETRIC("Гальдрастав (Гальдрабук)")
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
            StaveLayoutType.SOLAR_12_RAY -> composeSolar12Ray(runes, seed)
            StaveLayoutType.STELE_OBELISK -> composeSteleObelisk(runes, seed)
            StaveLayoutType.GALDRABOK_ASYMMETRIC -> composeGaldrabokAsymmetric(runes, seed)
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

    private fun composeSolar12Ray(runes: List<Rune>, seed: Long): List<RenderStroke> {
        val result = mutableListOf<RenderStroke>()
        val cx = 250f
        val cy = 250f

        // 1. Central Double Solar Hub Ring & Inner Solar Cross Spokes
        val hubInnerR = 18f
        val hubOuterR = 30f

        val innerPts = mutableListOf<StrokePoint>()
        val outerPts = mutableListOf<StrokePoint>()
        for (i in 0..24) {
            val a = (2 * PI * i / 24).toFloat()
            innerPts.add(StrokePoint(cx + hubInnerR * cos(a), cy + hubInnerR * sin(a)))
            outerPts.add(StrokePoint(cx + hubOuterR * cos(a), cy + hubOuterR * sin(a)))
        }
        result.add(RenderStroke(innerPts))
        result.add(RenderStroke(outerPts, isHairlineGuide = true))

        // Central 12-spoke solar core wheel
        for (i in 0 until 12) {
            val a = (-PI / 2 + 2 * PI * i / 12).toFloat()
            result.add(
                RenderStroke(
                    listOf(
                        StrokePoint(cx + hubInnerR * cos(a), cy + hubInnerR * sin(a)),
                        StrokePoint(cx + hubOuterR * cos(a), cy + hubOuterR * sin(a))
                    )
                )
            )
        }

        // 2. Concentric Hairline Guide Orbits Connecting the 12 Rays
        val webRadius1 = 88f
        val webRadius2 = 142f
        val webPts1 = mutableListOf<StrokePoint>()
        val webPts2 = mutableListOf<StrokePoint>()
        for (i in 0..24) {
            val a = (2 * PI * i / 24).toFloat()
            webPts1.add(StrokePoint(cx + webRadius1 * cos(a), cy + webRadius1 * sin(a)))
            webPts2.add(StrokePoint(cx + webRadius2 * cos(a), cy + webRadius2 * sin(a)))
        }
        result.add(RenderStroke(webPts1, isHairlineGuide = true))
        result.add(RenderStroke(webPts2, isHairlineGuide = true))

        // 3. 12 Radiating Rays with protective notches and terminal crowns
        val rayCount = 12
        for (i in 0 until rayCount) {
            val angle = (-PI / 2 + 2 * PI * i / rayCount).toFloat()
            val cosA = cos(angle)
            val sinA = sin(angle)
            val perpX = -sinA
            val perpY = cosA

            val rStart = hubOuterR
            val rEnd = 192f

            // Main Ray Spine Line
            result.add(
                RenderStroke(
                    listOf(
                        StrokePoint(cx + rStart * cosA, cy + rStart * sinA),
                        StrokePoint(cx + rEnd * cosA, cy + rEnd * sinA)
                    ),
                    isStem = true
                )
            )

            // Protective chevron crossbars / notches along each ray
            val notchDists = listOf(62f, 115f, 165f)
            for (dist in notchDists) {
                val span = 11f
                result.add(
                    RenderStroke(
                        listOf(
                            StrokePoint(cx + dist * cosA - perpX * span, cy + dist * sinA - perpY * span),
                            StrokePoint(cx + (dist + 5f) * cosA, cy + (dist + 5f) * sinA),
                            StrokePoint(cx + dist * cosA + perpX * span, cy + dist * sinA + perpY * span)
                        )
                    )
                )
            }

            // Terminal Sun-Crescent / Trident Crown at tip of ray
            val tipX = cx + rEnd * cosA
            val tipY = cy + rEnd * sinA
            val forkLen = 14f
            val forkSpread = 12f
            result.add(
                RenderStroke(
                    listOf(
                        StrokePoint(tipX - perpX * forkSpread, tipY - perpY * forkSpread),
                        StrokePoint(cx + (rEnd + forkLen) * cosA, cy + (rEnd + forkLen) * sinA),
                        StrokePoint(tipX + perpX * forkSpread, tipY + perpY * forkSpread)
                    ),
                    isOuterPole = true
                )
            )

            // 4. Embed and Orient Runes Radially on Rays
            if (runes.isNotEmpty()) {
                val rune = runes[i % runes.size]
                val runeDist = 118f
                val runeCenterX = cx + runeDist * cosA
                val runeCenterY = cy + runeDist * sinA

                val rotAngle = angle + (PI / 2).toFloat()
                val cosRot = cos(rotAngle)
                val sinRot = sin(rotAngle)
                val rW = 32f
                val rH = 44f

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

    private fun composeSteleObelisk(runes: List<Rune>, seed: Long): List<RenderStroke> {
        val result = mutableListOf<RenderStroke>()
        val count = runes.size.coerceAtLeast(1)

        // 1. Structural Obelisk stele outlines
        // Left pillar line
        result.add(RenderStroke(listOf(StrokePoint(226f, 155f), StrokePoint(216f, 425f)), isStem = true))
        // Right pillar line
        result.add(RenderStroke(listOf(StrokePoint(274f, 155f), StrokePoint(284f, 425f)), isStem = true))
        // Obelisk pyramid top cap
        result.add(RenderStroke(listOf(StrokePoint(226f, 155f), StrokePoint(250f, 115f), StrokePoint(274f, 155f)), isStem = true))
        // Ridge line from cap down center
        result.add(RenderStroke(listOf(StrokePoint(250f, 115f), StrokePoint(250f, 155f)), isStem = true))
        // Spire needle extending upward above the obelisk
        result.add(RenderStroke(listOf(StrokePoint(250f, 115f), StrokePoint(250f, 52f)), isOuterPole = true))
        // Base pedestal steps
        result.add(RenderStroke(listOf(StrokePoint(208f, 425f), StrokePoint(292f, 425f)), isStem = true))
        result.add(RenderStroke(listOf(StrokePoint(202f, 436f), StrokePoint(298f, 436f)), isStem = true))
        result.add(RenderStroke(listOf(StrokePoint(216f, 425f), StrokePoint(202f, 436f)), isStem = false))
        result.add(RenderStroke(listOf(StrokePoint(284f, 425f), StrokePoint(298f, 436f)), isStem = false))

        // Center spine hairline guide
        result.add(RenderStroke(listOf(StrokePoint(250f, 155f), StrokePoint(250f, 420f)), isHairlineGuide = true))

        // 2. Arrange runes stacked vertically inside the obelisk body
        val topY = 165f
        val bottomY = 415f
        val totalH = bottomY - topY
        val slotH = totalH / count

        for (i in runes.indices) {
            val rune = runes[i]
            val slotCenterY = topY + slotH * (i + 0.5f)
            val runeScale = (slotH * 0.70f) / 140f
            val runeW = 100f * runeScale
            val startX = 250f - runeW / 2f
            val startY = slotCenterY - (140f * runeScale) / 2f

            for (stroke in rune.strokes) {
                val pts = stroke.points.map { pt ->
                    StrokePoint(startX + pt.x * runeScale, startY + pt.y * runeScale)
                }
                result.add(RenderStroke(pts, isStem = stroke.points.size == 2 && stroke.points[0].x == stroke.points[1].x))
            }
        }
        return result
    }

    private fun composeGaldrabokAsymmetric(runes: List<Rune>, seed: Long): List<RenderStroke> {
        val result = mutableListOf<RenderStroke>()
        val cx = 250f
        val cy = 250f
        val variation = (Math.abs(seed) % 3).toInt()

        when (variation) {
            1 -> {
                // Variation 1: Óttastafur (Sigil of Dread and Shield) - Dual offset staggered spines with lightning bridge
                val stem1X = 180f
                val stem2X = 320f
                val top1Y = 70f
                val bot1Y = 410f
                val top2Y = 95f
                val bot2Y = 435f

                // Staggered vertical spines
                result.add(RenderStroke(listOf(StrokePoint(stem1X, top1Y), StrokePoint(stem1X, bot1Y)), isStem = true))
                result.add(RenderStroke(listOf(StrokePoint(stem2X, top2Y), StrokePoint(stem2X, bot2Y)), isStem = true))

                // Asymmetric crossbars and lightning connecting bridge
                result.add(RenderStroke(listOf(StrokePoint(stem1X - 35f, 130f), StrokePoint(stem1X + 45f, 130f)), isStem = true))
                result.add(RenderStroke(listOf(StrokePoint(stem1X, 220f), StrokePoint(250f, 250f), StrokePoint(stem2X, 280f)), isStem = true))
                result.add(RenderStroke(listOf(StrokePoint(stem2X - 50f, 360f), StrokePoint(stem2X + 25f, 360f)), isStem = true))

                // Asymmetric hooks and spirals
                result.add(RenderStroke(listOf(StrokePoint(stem1X, top1Y), StrokePoint(stem1X - 22f, top1Y - 22f), StrokePoint(stem1X - 38f, top1Y - 12f))))
                result.add(RenderStroke(listOf(StrokePoint(stem2X, bot2Y), StrokePoint(stem2X + 24f, bot2Y + 22f), StrokePoint(stem2X + 38f, bot2Y + 10f))))

                // Trident finials at opposing endpoints
                result.add(RenderStroke(listOf(StrokePoint(stem2X - 16f, top2Y + 12f), StrokePoint(stem2X, top2Y), StrokePoint(stem2X + 16f, top2Y + 12f)), isOuterPole = true))
                result.add(RenderStroke(listOf(StrokePoint(stem1X - 16f, bot1Y - 12f), StrokePoint(stem1X, bot1Y), StrokePoint(stem1X + 16f, bot1Y - 12f)), isOuterPole = true))

                // Integrate runes along asymmetric nodes
                runes.forEachIndexed { idx, rune ->
                    val (nx, ny, flip) = when (idx % 4) {
                        0 -> Triple(stem1X, 170f, 1f)
                        1 -> Triple(stem2X, 190f, -1f)
                        2 -> Triple(stem1X, 310f, 1f)
                        else -> Triple(stem2X, 390f, -1f)
                    }
                    val rW = 38f
                    val rH = 52f
                    for (stroke in rune.strokes) {
                        if (isVerticalStroke(stroke)) continue
                        val pts = stroke.points.map { pt ->
                            val lx = ((pt.x - 50f) / 50f) * (rW * 0.55f) * flip
                            val ly = ((pt.y - 70f) / 70f) * (rH * 0.50f)
                            StrokePoint(nx + lx, ny + ly)
                        }
                        result.add(RenderStroke(pts))
                    }
                }
            }

            2 -> {
                // Variation 2: Kaupastafur / Traveler Galdrastafur - Asymmetric 3-arm solar fork with eye ring
                val mainX = 230f
                val topY = 65f
                val botY = 435f

                // Main off-center spine
                result.add(RenderStroke(listOf(StrokePoint(mainX, topY), StrokePoint(mainX, botY)), isStem = true, isOuterPole = true))

                // Leftward 3 asymmetric diagonal barbs
                result.add(RenderStroke(listOf(StrokePoint(mainX, 120f), StrokePoint(mainX - 70f, 155f), StrokePoint(mainX - 70f, 185f))))
                result.add(RenderStroke(listOf(StrokePoint(mainX, 220f), StrokePoint(mainX - 85f, 260f), StrokePoint(mainX - 100f, 250f))))
                result.add(RenderStroke(listOf(StrokePoint(mainX, 320f), StrokePoint(mainX - 60f, 350f))))

                // Rightward solar protection eye ring and diagonal spur
                val eyeR = 26f
                val eyeCX = mainX + 75f
                val eyeCY = 190f
                val eyePts = mutableListOf<StrokePoint>()
                for (i in 0..16) {
                    val a = (2 * PI * i / 16).toFloat()
                    eyePts.add(StrokePoint(eyeCX + eyeR * cos(a), eyeCY + eyeR * sin(a)))
                }
                result.add(RenderStroke(eyePts))
                result.add(RenderStroke(listOf(StrokePoint(mainX, 190f), StrokePoint(eyeCX - eyeR, eyeCY))))
                result.add(RenderStroke(listOf(StrokePoint(mainX, 290f), StrokePoint(mainX + 90f, 325f), StrokePoint(mainX + 90f, 355f))))

                // Runes mapped to asymmetric anchor points
                runes.forEachIndexed { idx, rune ->
                    val (nx, ny) = when (idx % 3) {
                        0 -> Pair(mainX - 45f, 140f)
                        1 -> Pair(mainX + 45f, 305f)
                        else -> Pair(mainX - 50f, 240f)
                    }
                    val rW = 34f
                    val rH = 48f
                    for (stroke in rune.strokes) {
                        if (isVerticalStroke(stroke)) continue
                        val pts = stroke.points.map { pt ->
                            val lx = ((pt.x - 50f) / 50f) * (rW * 0.55f)
                            val ly = ((pt.y - 70f) / 70f) * (rH * 0.50f)
                            StrokePoint(nx + lx, ny + ly)
                        }
                        result.add(RenderStroke(pts))
                    }
                }
            }

            else -> {
                // Variation 0: Angurgapi / Classic Asymmetric Galdrabók Sigil
                val spineTop = 60f
                val spineBot = 440f

                // Primary main spine
                result.add(RenderStroke(listOf(StrokePoint(cx, spineTop), StrokePoint(cx, spineBot)), isStem = true))

                // Upper left asymmetric spiral hook
                val spiralPts = listOf(
                    StrokePoint(cx, 110f),
                    StrokePoint(cx - 40f, 90f),
                    StrokePoint(cx - 75f, 115f),
                    StrokePoint(cx - 60f, 150f),
                    StrokePoint(cx - 30f, 140f)
                )
                result.add(RenderStroke(spiralPts))

                // Upper right angled crossbar with trident tip
                result.add(RenderStroke(listOf(StrokePoint(cx, 130f), StrokePoint(cx + 85f, 100f)), isStem = true))
                result.add(RenderStroke(listOf(StrokePoint(cx + 70f, 92f), StrokePoint(cx + 85f, 100f), StrokePoint(cx + 80f, 118f))))

                // Middle asymmetric knot and horizontal bar
                result.add(RenderStroke(listOf(StrokePoint(cx - 85f, 250f), StrokePoint(cx + 60f, 250f)), isStem = true))
                // Left triple notches
                result.add(RenderStroke(listOf(StrokePoint(cx - 65f, 235f), StrokePoint(cx - 65f, 265f))))
                result.add(RenderStroke(listOf(StrokePoint(cx - 45f, 238f), StrokePoint(cx - 45f, 262f))))
                result.add(RenderStroke(listOf(StrokePoint(cx - 25f, 240f), StrokePoint(cx - 25f, 260f))))

                // Lower right hooked tail
                val tailPts = listOf(
                    StrokePoint(cx, 370f),
                    StrokePoint(cx + 65f, 395f),
                    StrokePoint(cx + 90f, 375f),
                    StrokePoint(cx + 80f, 350f)
                )
                result.add(RenderStroke(tailPts))

                // Map runes along master spine tiers and offset arm
                runes.forEachIndexed { idx, rune ->
                    val (nx, ny, scale) = when (idx % 3) {
                        0 -> Triple(cx, 190f, 0.9f)
                        1 -> Triple(cx + 30f, 250f, 0.8f)
                        else -> Triple(cx, 310f, 0.9f)
                    }
                    val rW = 40f * scale
                    val rH = 55f * scale
                    for (stroke in rune.strokes) {
                        if (isVerticalStroke(stroke)) continue
                        val pts = stroke.points.map { pt ->
                            val lx = ((pt.x - 50f) / 50f) * (rW * 0.55f)
                            val ly = ((pt.y - 70f) / 70f) * (rH * 0.50f)
                            StrokePoint(nx + lx, ny + ly)
                        }
                        result.add(RenderStroke(pts))
                    }
                }
            }
        }

        return result
    }
}
