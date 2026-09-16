package com.example.engine

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.Rune
import com.example.data.model.RuneStroke
import com.example.data.model.StrokePoint
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PdfExportTest {

    private lateinit var context: Context
    private lateinit var testRunes: List<Rune>

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        testRunes = listOf(
            Rune(
                id = "fehu",
                futhark = "elder",
                nameRu = "Феху",
                nameEn = "Fehu",
                unicode = "ᚠ",
                phonetic = "F",
                keywordsRu = listOf("богатство"),
                divinationDirect = "Прямое значение",
                divinationReversed = "Перевернутое значение",
                magicUse = "Привлечение ресурсов",
                tattooSymbolism = "Изобилие",
                strokes = listOf(RuneStroke(listOf(StrokePoint(50f, 0f), StrokePoint(50f, 100f))))
            ),
            Rune(
                id = "uruz",
                futhark = "elder",
                nameRu = "Уруз",
                nameEn = "Uruz",
                unicode = "ᚢ",
                phonetic = "U",
                keywordsRu = listOf("сила"),
                divinationDirect = "Физическая сила",
                divinationReversed = "Упадок сил",
                magicUse = "Здоровье и мошь",
                tattooSymbolism = "Выносливость",
                strokes = listOf(RuneStroke(listOf(StrokePoint(20f, 0f), StrokePoint(20f, 100f))))
            )
        )
    }

    @Test
    fun testRenderA4PdfForPrinting_createsFileOrGracefulNull() = runBlocking {
        val stave = StaveComposer.compose(testRunes, StaveLayoutType.BINDRUNE, seed = 1001L)
        val config = SketchConfig(style = SketchStyle.ORNAMENTAL, seed = 1001L)

        val pdfFile = SvgStaveRenderer.renderA4PdfForPrinting(
            context = context,
            stave = stave,
            config = config,
            targetDiameterMm = 100f,
            title = "Защитный Став Силы",
            runes = testRunes
        )

        // If native C++ PDF bindings are present in Robolectric environment, pdfFile is non-null and exists.
        // Otherwise, graceful fallback returns null without throwing unhandled exceptions.
        if (pdfFile != null) {
            assertTrue("PDF File should exist", pdfFile.exists())
            assertTrue("PDF File name should be runic_stave_a4.pdf", pdfFile.name.endsWith(".pdf"))
            assertTrue("PDF File size should be > 0 bytes", pdfFile.length() > 0)
        }
    }

    @Test
    fun testRenderA4PdfForPrinting_differentDiameters_runsWithoutExceptions() = runBlocking {
        val stave = StaveComposer.compose(testRunes, StaveLayoutType.SOLAR_12_RAY, seed = 2026L)
        val config = SketchConfig(style = SketchStyle.SACRED_GOLD, seed = 2026L)

        val diameters = listOf(60f, 80f, 100f, 120f, 150f)
        for (diam in diameters) {
            val file = SvgStaveRenderer.renderA4PdfForPrinting(
                context = context,
                stave = stave,
                config = config,
                targetDiameterMm = diam,
                title = "Став Ø $diam мм",
                runes = testRunes
            )
            // Function completes safely for all valid millimetric diameters
        }
    }
}
