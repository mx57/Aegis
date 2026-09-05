package com.example.engine

import com.example.engine.vectorizer.CurveFittingType
import com.example.engine.vectorizer.ImageVectorizer
import com.example.engine.vectorizer.SampleRasterSketches
import com.example.engine.vectorizer.ThresholdMode
import com.example.engine.vectorizer.TracingMode
import com.example.engine.vectorizer.VectorizerConfig
import com.example.engine.vectorizer.VectorizerPreset
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ImageVectorizerTest {

    @Test
    fun testVectorizerConfigPresets() {
        val ultra = VectorizerConfig.fromPreset(VectorizerPreset.ULTRA_FIDELITY_100)
        assertEquals(0.02f, ultra.detailLevel, 0.001f)
        assertEquals(TracingMode.OUTLINE, ultra.mode)
        assertEquals(ThresholdMode.OTSU_AUTO, ultra.thresholdMode)

        val stencil = VectorizerConfig.fromPreset(VectorizerPreset.TATTOO_STENCIL_LINEWORK)
        assertEquals(TracingMode.CENTERLINE, stencil.mode)
        assertEquals(4, stencil.minPathArea)

        val tonal = VectorizerConfig.fromPreset(VectorizerPreset.MULTI_TONE_REALISTIC)
        assertEquals(TracingMode.MULTILAYER_TONAL, tonal.mode)
        assertEquals(4, tonal.tonalLayers)
    }

    @Test
    fun testSampleSketchesGeneration() {
        for (sample in SampleRasterSketches.AVAILABLE_SAMPLES) {
            val bitmap = SampleRasterSketches.createSampleBitmap(sample.id, 200, 200)
            assertNotNull("Bitmap for ${sample.id} should not be null", bitmap)
            assertEquals(200, bitmap.width)
            assertEquals(200, bitmap.height)
        }
    }

    @Test
    fun testOutlineVectorizationProducesValidSvg() = runBlocking {
        val sample = SampleRasterSketches.createSampleBitmap("bindrune_strength", 240, 240)
        val config = VectorizerConfig.fromPreset(VectorizerPreset.ULTRA_FIDELITY_100)

        val result = ImageVectorizer.vectorize(sample, config)

        assertNotNull(result)
        assertTrue("Path count should be greater than 0", result.pathCount > 0)
        assertTrue("Node count should be greater than 0", result.nodeCount > 0)
        assertTrue("Similarity should be at least 85%", result.similarityPercent >= 85.0f)
        assertTrue("SVG XML must contain svg open tag", result.svgString.contains("<svg"))
        assertTrue("SVG XML must contain xmlns", result.svgString.contains("xmlns=\"http://www.w3.org/2000/svg\""))
        assertTrue("SVG XML must contain path commands", result.svgString.contains("<path d=\"M"))
        assertTrue("SVG XML must contain svg closing tag", result.svgString.contains("</svg>"))
        assertNotNull(result.binaryMaskBitmap)
        assertNotNull(result.vectorPreviewBitmap)
    }

    @Test
    fun testCenterlineVectorizationZhangSuen() = runBlocking {
        val sample = SampleRasterSketches.createSampleBitmap("aegishjalmur", 240, 240)
        val config = VectorizerConfig.fromPreset(VectorizerPreset.TATTOO_STENCIL_LINEWORK)

        val result = ImageVectorizer.vectorize(sample, config)

        assertNotNull(result)
        assertTrue("Centerline should produce strokes", result.pathCount > 0)
        assertTrue("SVG must contain stroke width attribute", result.svgString.contains("stroke-width="))
        assertTrue("Processing time should be measured", result.processingTimeMs >= 0)
    }

    @Test
    fun testMultilayerTonalVectorization() = runBlocking {
        val sample = SampleRasterSketches.createSampleBitmap("odin_raven", 240, 240)
        val config = VectorizerConfig.fromPreset(VectorizerPreset.MULTI_TONE_REALISTIC)

        val result = ImageVectorizer.vectorize(sample, config)

        assertNotNull(result)
        assertTrue("Multilayer should yield stacked layers", result.pathCount > 0)
        assertTrue("SVG should contain fill-opacity", result.svgString.contains("fill-opacity="))
    }

    @Test
    fun testAdaptiveThresholdMode() = runBlocking {
        val sample = SampleRasterSketches.createSampleBitmap("vegvisir", 400, 400)
        val config = VectorizerConfig.fromPreset(VectorizerPreset.HISTORICAL_MANUSCRIPT)

        val result = ImageVectorizer.vectorize(sample, config)

        assertNotNull(result)
        assertTrue("Adaptive threshold should detect paths (found ${result.pathCount})", result.pathCount > 0)
        assertTrue(result.similarityPercent >= 80.0f)
    }

    @Test
    fun testPolylineCurveFittingMode() = runBlocking {
        val sample = SampleRasterSketches.createSampleBitmap("bindrune_strength", 200, 200)
        val config = VectorizerConfig(
            mode = TracingMode.OUTLINE,
            curveFitting = CurveFittingType.POLYLINE,
            detailLevel = 0.1f
        )

        val result = ImageVectorizer.vectorize(sample, config)

        assertNotNull(result)
        assertTrue(result.pathCount > 0)
        assertTrue("Polyline SVG contains L commands", result.svgString.contains(" L "))
    }
}
