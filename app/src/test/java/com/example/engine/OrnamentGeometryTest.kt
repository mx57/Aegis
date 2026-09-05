package com.example.engine

import com.example.data.model.StrokePoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class OrnamentGeometryTest {

    private val delta = 0.001f

    @Test
    fun scaleOrnaments_scaleFactorOne_returnsOriginalOrnaments() {
        val original = GeneratedOrnaments(
            lines = listOf(LineSegmentGeom(100f, 100f, 200f, 200f, widthFactor = 1.2f, alpha = 0.8f)),
            circles = listOf(CircleGeom(250f, 250f, 50f, isFilled = true, widthFactor = 1.0f, alpha = 0.9f)),
            polygons = listOf(PolygonGeom(listOf(StrokePoint(100f, 100f), StrokePoint(150f, 200f)), isFilled = false)),
            paths = listOf(PathGeom(listOf(StrokePoint(50f, 50f), StrokePoint(75f, 100f)), isClosed = true))
        )

        val result = OrnamentGeometry.scaleOrnaments(original, scaleFactor = 1.0f)

        assertSame("Should return exact same instance when scaleFactor is 1.0f", original, result)
    }

    @Test
    fun scaleOrnaments_emptyOrnaments_returnsEmptyOrnaments() {
        val empty = GeneratedOrnaments()

        val result = OrnamentGeometry.scaleOrnaments(empty, scaleFactor = 1.5f)

        assertTrue(result.lines.isEmpty())
        assertTrue(result.circles.isEmpty())
        assertTrue(result.polygons.isEmpty())
        assertTrue(result.paths.isEmpty())
    }

    @Test
    fun scaleOrnaments_scalesLineSegmentsCorrectly() {
        val cx = 250f
        val cy = 250f
        val originalLine = LineSegmentGeom(
            x1 = 200f, // dx = -50 -> scaled dx = -75 -> x = 175
            y1 = 200f, // dy = -50 -> scaled dy = -75 -> y = 175
            x2 = 300f, // dx = 50  -> scaled dx = 75  -> x = 325
            y2 = 350f, // dy = 100 -> scaled dy = 150 -> y = 400
            widthFactor = 1.5f,
            alpha = 0.7f
        )
        val ornaments = GeneratedOrnaments(lines = listOf(originalLine))

        val result = OrnamentGeometry.scaleOrnaments(ornaments, cx, cy, scaleFactor = 1.5f)

        assertEquals(1, result.lines.size)
        val scaledLine = result.lines[0]
        assertEquals(175f, scaledLine.x1, delta)
        assertEquals(175f, scaledLine.y1, delta)
        assertEquals(325f, scaledLine.x2, delta)
        assertEquals(400f, scaledLine.y2, delta)
        assertEquals(1.5f, scaledLine.widthFactor, delta)
        assertEquals(0.7f, scaledLine.alpha, delta)
    }

    @Test
    fun scaleOrnaments_scalesCirclesCorrectly() {
        val cx = 250f
        val cy = 250f
        val originalCircle = CircleGeom(
            cx = 200f, // dx = -50 -> scaled dx = -25 -> cx = 225
            cy = 300f, // dy = 50  -> scaled dy = 25  -> cy = 275
            radius = 20f, // scaled radius = 10
            isFilled = true,
            widthFactor = 2.0f,
            alpha = 0.5f
        )
        val ornaments = GeneratedOrnaments(circles = listOf(originalCircle))

        val result = OrnamentGeometry.scaleOrnaments(ornaments, cx, cy, scaleFactor = 0.5f)

        assertEquals(1, result.circles.size)
        val scaledCircle = result.circles[0]
        assertEquals(225f, scaledCircle.cx, delta)
        assertEquals(275f, scaledCircle.cy, delta)
        assertEquals(10f, scaledCircle.radius, delta)
        assertTrue(scaledCircle.isFilled)
        assertEquals(2.0f, scaledCircle.widthFactor, delta)
        assertEquals(0.5f, scaledCircle.alpha, delta)
    }

    @Test
    fun scaleOrnaments_scalesPolygonsAndPathsCorrectly() {
        val cx = 100f
        val cy = 100f
        val originalPolygon = PolygonGeom(
            points = listOf(
                StrokePoint(100f, 50f),  // dx=0, dy=-50 -> scaled dy = -100 -> (100, 0)
                StrokePoint(150f, 150f)  // dx=50, dy=50 -> scaled dx=100, dy=100 -> (200, 200)
            ),
            isFilled = true,
            widthFactor = 1.1f,
            alpha = 0.9f
        )
        val originalPath = PathGeom(
            points = listOf(
                StrokePoint(80f, 80f) // dx=-20, dy=-20 -> scaled dx=-40, dy=-40 -> (60, 60)
            ),
            isClosed = true,
            isFilled = false,
            widthFactor = 0.8f,
            alpha = 0.6f
        )
        val ornaments = GeneratedOrnaments(polygons = listOf(originalPolygon), paths = listOf(originalPath))

        val result = OrnamentGeometry.scaleOrnaments(ornaments, cx, cy, scaleFactor = 2.0f) // Will be coerced to 1.8f

        // scaleFactor = 2.0f coerced to 1.8f
        // Polygon point 1: cx + (100-100)*1.8 = 100, cy + (50-100)*1.8 = 100 - 90 = 10
        // Polygon point 2: cx + (150-100)*1.8 = 100 + 90 = 190, cy + (150-100)*1.8 = 100 + 90 = 190
        val scaledPoly = result.polygons[0]
        assertEquals(100f, scaledPoly.points[0].x, delta)
        assertEquals(10f, scaledPoly.points[0].y, delta)
        assertEquals(190f, scaledPoly.points[1].x, delta)
        assertEquals(190f, scaledPoly.points[1].y, delta)
        assertTrue(scaledPoly.isFilled)
        assertEquals(1.1f, scaledPoly.widthFactor, delta)
        assertEquals(0.9f, scaledPoly.alpha, delta)

        // Path point 1: cx + (80-100)*1.8 = 100 - 36 = 64, cy + (80-100)*1.8 = 100 - 36 = 64
        val scaledPath = result.paths[0]
        assertEquals(64f, scaledPath.points[0].x, delta)
        assertEquals(64f, scaledPath.points[0].y, delta)
        assertTrue(scaledPath.isClosed)
        assertEquals(false, scaledPath.isFilled)
        assertEquals(0.8f, scaledPath.widthFactor, delta)
        assertEquals(0.6f, scaledPath.alpha, delta)
    }

    @Test
    fun generateCenterEmblem_valknut_generatesInterlockingTrianglesAndOrnaments() {
        val valknut = OrnamentGeometry.generateCenterEmblem(com.example.engine.CenterEmblem.VALKNUT, 3.0f)

        // Valknut should contain 3 drop shadow polygons, boundary paths, facet ridge lines, and central node circles
        assertTrue("Valknut should generate drop shadow polygons", valknut.polygons.isNotEmpty())
        assertTrue("Valknut should generate boundary paths for interlocking ribbons", valknut.paths.size >= 6)
        assertTrue("Valknut should generate facet ridge and shading lines", valknut.lines.isNotEmpty())
        assertTrue("Valknut should generate apex and hub circles", valknut.circles.isNotEmpty())
    }

    @Test
    fun generateCenterEmblem_triquetra_generatesArcuatePetalsAndOrnaments() {
        val triquetra = OrnamentGeometry.generateCenterEmblem(com.example.engine.CenterEmblem.TRIQUETRA, 3.0f)

        // Triquetra should generate arcuate dual-ribbon petal paths, solar circle, shading lines, and central studs
        assertTrue("Triquetra should generate arcuate petal and shadow paths", triquetra.paths.isNotEmpty())
        assertTrue("Triquetra should generate central solar circle and studs", triquetra.circles.size >= 5)
        assertTrue("Triquetra should generate recess shading lines across ribbon", triquetra.lines.isNotEmpty())
    }

    @Test
    fun scaleOrnaments_coercesScaleFactorBoundaries() {
        val circle = CircleGeom(cx = 250f, cy = 250f, radius = 100f)
        val ornaments = GeneratedOrnaments(circles = listOf(circle))

        // Test scaleFactor below lower bound (0.4f)
        val resultMin = OrnamentGeometry.scaleOrnaments(ornaments, scaleFactor = 0.1f)
        assertEquals(40f, resultMin.circles[0].radius, delta)

        // Test scaleFactor above upper bound (1.8f)
        val resultMax = OrnamentGeometry.scaleOrnaments(ornaments, scaleFactor = 3.0f)
        assertEquals(180f, resultMax.circles[0].radius, delta)
    }
}
