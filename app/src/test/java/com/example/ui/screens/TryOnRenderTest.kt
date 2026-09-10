package com.example.ui.screens

import android.content.Context
import androidx.compose.ui.geometry.Offset
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.Rune
import com.example.data.model.RuneStroke
import com.example.data.model.StrokePoint
import com.example.engine.SketchConfig
import com.example.engine.SketchStyle
import com.example.engine.StaveComposer
import com.example.engine.StaveLayoutType
import com.example.engine.TryOnRenderer
import com.example.ui.components.BodyZone
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TryOnRenderTest {

    private lateinit var context: Context
    private lateinit var testRunes: List<Rune>

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        testRunes = listOf(
            Rune("tiwaz", "elder", "Тейваз", "Tiwaz", "ᛏ", "Т", listOf("победа"), "Победа", "Слабость", "Оберег", "Честь",
                listOf(RuneStroke(listOf(StrokePoint(50f, 0f), StrokePoint(50f, 140f))))),
            Rune("algiz", "elder", "Альгиз", "Algiz", "ᛉ", "З", listOf("защита"), "Щит", "Уязвимость", "Охрана", "Осока",
                listOf(RuneStroke(listOf(StrokePoint(50f, 0f), StrokePoint(50f, 140f)))))
        )
    }

    @Test
    fun testRenderTryOnToBitmap_generatesValidBitmapForSilhouette() = runBlocking {
        val stave = StaveComposer.compose(testRunes, StaveLayoutType.BINDRUNE, seed = 12345L)
        val config = SketchConfig(style = SketchStyle.ORNAMENTAL, seed = 12345L)

        val bitmap = TryOnRenderer.renderTryOnToBitmap(
            context = context,
            userPhotoUri = null,
            selectedZone = BodyZone.FOREARM,
            stave = stave,
            config = config,
            offset = Offset.Zero,
            scale = 1.0f,
            rotation = 0f,
            opacity = 0.85f,
            isBlackInk = true,
            targetWidth = 540,
            targetHeight = 658
        )

        assertNotNull("Bitmap should not be null", bitmap)
        assertEquals(540, bitmap.width)
        assertEquals(658, bitmap.height)
        assertTrue("Bitmap should be allocated and valid", bitmap.byteCount > 0)
    }

    @Test
    fun testRenderTryOnToBitmap_allBodyZones_renderWithoutExceptions() = runBlocking {
        val stave = StaveComposer.compose(testRunes, StaveLayoutType.SOLAR_12_RAY, seed = 777L)
        val config = SketchConfig(style = SketchStyle.SACRED_GOLD, seed = 777L)

        for (zone in BodyZone.values()) {
            val bitmap = TryOnRenderer.renderTryOnToBitmap(
                context = context,
                userPhotoUri = null,
                selectedZone = zone,
                stave = stave,
                config = config,
                offset = Offset(10f, -10f),
                scale = 1.2f,
                rotation = 15f,
                opacity = 0.70f,
                isBlackInk = false,
                targetWidth = 300,
                targetHeight = 360
            )

            assertNotNull("Zone ${zone.name} bitmap should not be null", bitmap)
            assertEquals(300, bitmap.width)
            assertEquals(360, bitmap.height)
        }
    }

    @Test
    fun testRenderTryOnToBitmap_customOpacityAndColor_validBitmap() = runBlocking {
        val stave = StaveComposer.compose(testRunes, StaveLayoutType.SOLAR_12_RAY, seed = 9999L)
        val config = SketchConfig(style = SketchStyle.NORDIC_TATTOO, seed = 9999L)

        val bitmapBlackInk = TryOnRenderer.renderTryOnToBitmap(
            context = context,
            userPhotoUri = null,
            selectedZone = BodyZone.SHOULDER,
            stave = stave,
            config = config,
            offset = Offset.Zero,
            scale = 0.8f,
            rotation = 45f,
            opacity = 0.5f,
            isBlackInk = true,
            targetWidth = 400,
            targetHeight = 480
        )

        assertNotNull(bitmapBlackInk)

        val bitmapGoldInk = TryOnRenderer.renderTryOnToBitmap(
            context = context,
            userPhotoUri = null,
            selectedZone = BodyZone.SHOULDER,
            stave = stave,
            config = config,
            offset = Offset.Zero,
            scale = 0.8f,
            rotation = 45f,
            opacity = 0.5f,
            isBlackInk = false,
            targetWidth = 400,
            targetHeight = 480
        )

        assertNotNull(bitmapGoldInk)
    }
}
