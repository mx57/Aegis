package com.example.engine

import androidx.test.core.app.ApplicationProvider
import com.example.data.model.Rune
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class PdfStaveExporterTest {

    @Test
    fun testGenerateA4Pdf_CreatesValidFileAndContent() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()

        val testRunes = listOf(
            Rune("fehu", "elder", "Феху", "Fehu", "ᚠ", "Ф", listOf("богатство"), "Процветание", "Потеря", "Магия", "Богатство", emptyList()),
            Rune("uruz", "elder", "Уруз", "Uruz", "ᚢ", "У", listOf("сила"), "Мощь", "Упадок", "Здоровье", "Зубр", emptyList())
        )

        val stave = StaveComposer.compose(testRunes, StaveLayoutType.BINDRUNE, seed = 12345L)
        val config = SketchConfig(style = SketchStyle.ORNAMENTAL, isStencil = true)

        val pdfFile = PdfStaveExporter.generateA4Pdf(
            context = context,
            stave = stave,
            config = config,
            runes = testRunes,
            layoutTitle = "Тестовый Став",
            targetSizeCm = 12.0f,
            fileName = "test_a4_stave.pdf"
        )

        assertNotNull(pdfFile)
        assertTrue(pdfFile.exists())
        assertTrue(pdfFile.length() > 0)
    }

    @Test
    fun testPdfDimensionsConstants() {
        assertEquals(595, PdfStaveExporter.PAGE_WIDTH_PT)
        assertEquals(842, PdfStaveExporter.PAGE_HEIGHT_PT)
    }

    @Test
    fun testGenerateA4Pdf_HandlesDifferentPhysicalSizes() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val stave = StaveComposer.compose(emptyList(), StaveLayoutType.SOLAR_12_RAY, seed = 999L)
        val config = SketchConfig(style = SketchStyle.SACRED_GOLD)

        for (sizeCm in listOf(8.0f, 10.0f, 12.0f, 15.0f)) {
            val file = PdfStaveExporter.generateA4Pdf(
                context = context,
                stave = stave,
                config = config,
                runes = emptyList(),
                layoutTitle = "Солярный Став",
                targetSizeCm = sizeCm,
                fileName = "test_size_${sizeCm.toInt()}.pdf"
            )

            assertTrue(file.exists())
            assertTrue(file.length() > 0)
        }
    }
}
