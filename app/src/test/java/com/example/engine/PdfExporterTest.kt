package com.example.engine

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.Rune
import com.example.data.model.RuneStroke
import com.example.data.model.StrokePoint
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
class PdfExporterTest {

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
                phonetic = "Ф",
                keywordsRu = listOf("богатство", "изобилие"),
                divinationDirect = "Богатство, процветание, мобильная энергия",
                divinationReversed = "Потеря, застой ресурсов",
                magicUse = "Привлечение энергии",
                tattooSymbolism = "Материальный успех",
                strokes = listOf(
                    RuneStroke(listOf(StrokePoint(30f, 0f), StrokePoint(30f, 140f))),
                    RuneStroke(listOf(StrokePoint(30f, 30f), StrokePoint(70f, 10f))),
                    RuneStroke(listOf(StrokePoint(30f, 70f), StrokePoint(70f, 50f)))
                )
            ),
            Rune(
                id = "uruz",
                futhark = "elder",
                nameRu = "Уруз",
                nameEn = "Uruz",
                unicode = "ᚢ",
                phonetic = "У",
                keywordsRu = listOf("сила", "здоровье"),
                divinationDirect = "Дикая жизненная сила, преодоление",
                divinationReversed = "Утечка сил, болезни",
                magicUse = "Укрепление здоровья",
                tattooSymbolism = "Физическая мощь",
                strokes = listOf(
                    RuneStroke(listOf(StrokePoint(20f, 140f), StrokePoint(20f, 0f))),
                    RuneStroke(listOf(StrokePoint(20f, 0f), StrokePoint(70f, 40f))),
                    RuneStroke(listOf(StrokePoint(70f, 40f), StrokePoint(70f, 140f)))
                )
            )
        )
    }

    @Test
    fun testGenerateA4Pdf_createsNonEmptyValidPdfFile() = runBlocking {
        val stave = StaveComposer.compose(testRunes, StaveLayoutType.BINDRUNE, seed = 1234L)
        val config = SketchConfig(style = SketchStyle.SACRED_GOLD, seed = 1234L)

        val pdfFile = PdfExporter.generateA4Pdf(
            context = context,
            stave = stave,
            config = config,
            runes = testRunes,
            layoutTitleRu = "Вязаная руна",
            printSizeMm = 100,
            isStencilMode = true,
            fileName = "test_a4_stave.pdf"
        )

        assertNotNull("Generated PDF file should not be null", pdfFile)
        assertTrue("PDF file should exist on disk", pdfFile.exists())
        assertTrue("PDF file size should be greater than zero bytes", pdfFile.length() > 0)
        assertEquals("test_a4_stave.pdf", pdfFile.name)
    }

    @Test
    fun testGenerateA4Pdf_allPrintSizes_createsFilesSuccessfully() = runBlocking {
        val stave = StaveComposer.compose(testRunes, StaveLayoutType.SOLAR_12_RAY, seed = 999L)
        val config = SketchConfig(style = SketchStyle.ORNAMENTAL, seed = 999L)

        for (printSize in TattooPrintSize.values()) {
            val fileName = "test_stave_${printSize.sizeMm}mm.pdf"
            val pdfFile = PdfExporter.generateA4Pdf(
                context = context,
                stave = stave,
                config = config,
                runes = testRunes,
                layoutTitleRu = "12-лучевое солнце",
                printSizeMm = printSize.sizeMm,
                isStencilMode = true,
                fileName = fileName
            )

            assertNotNull("PDF file for size ${printSize.sizeMm}mm should not be null", pdfFile)
            assertTrue("PDF file for size ${printSize.sizeMm}mm should exist", pdfFile.exists())
            assertTrue("PDF file for size ${printSize.sizeMm}mm should not be empty", pdfFile.length() > 0)
        }
    }

    @Test
    fun testGenerateA4Pdf_stencilModeAndFullColorMode_createsFiles() = runBlocking {
        val stave = StaveComposer.compose(testRunes, StaveLayoutType.STELE_OBELISK, seed = 555L)
        val config = SketchConfig(style = SketchStyle.WOODCARVE, seed = 555L)

        val stencilPdf = PdfExporter.generateA4Pdf(
            context = context,
            stave = stave,
            config = config,
            runes = testRunes,
            layoutTitleRu = "Стела-обелиск",
            printSizeMm = 120,
            isStencilMode = true,
            fileName = "test_stencil.pdf"
        )

        val fullColorPdf = PdfExporter.generateA4Pdf(
            context = context,
            stave = stave,
            config = config,
            runes = testRunes,
            layoutTitleRu = "Стела-обелиск",
            printSizeMm = 120,
            isStencilMode = false,
            fileName = "test_fullcolor.pdf"
        )

        assertTrue("Stencil PDF should exist and be valid", stencilPdf.exists() && stencilPdf.length() > 0)
        assertTrue("FullColor PDF should exist and be valid", fullColorPdf.exists() && fullColorPdf.length() > 0)
    }
}
