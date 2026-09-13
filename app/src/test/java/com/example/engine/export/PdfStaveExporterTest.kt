package com.example.engine.export

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.Rune
import com.example.engine.ComposedStave
import com.example.engine.SketchConfig
import com.example.engine.StaveComposer
import com.example.engine.StaveLayoutType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PdfStaveExporterTest {

    private lateinit var application: Application
    private lateinit var testRunes: List<Rune>
    private lateinit var composedStave: ComposedStave
    private val config = SketchConfig()

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
        testRunes = listOf(
            Rune(
                id = "fehu",
                futhark = "elder",
                nameRu = "Феху",
                nameEn = "Fehu",
                unicode = "ᚠ",
                phonetic = "f",
                keywordsRu = listOf("Богатство", "Огонь"),
                divinationDirect = "Изобилие",
                divinationReversed = "Потеря",
                magicUse = "Привлечение богатства",
                tattooSymbolism = "Богатство, изобилие, первичный огонь и материальные ресурсы",
                strokes = emptyList()
            ),
            Rune(
                id = "uruz",
                futhark = "elder",
                nameRu = "Уруз",
                nameEn = "Uruz",
                unicode = "ᚢ",
                phonetic = "u",
                keywordsRu = listOf("Сила", "Бык"),
                divinationDirect = "Здоровье",
                divinationReversed = "Слабость",
                magicUse = "Укрепление здоровья",
                tattooSymbolism = "Дикая сила быка, здоровье, выносливость и жизненная энергия",
                strokes = emptyList()
            )
        )
        composedStave = StaveComposer.compose(testRunes, StaveLayoutType.BINDRUNE, seed = 1337L)
    }

    @Test
    fun pageConstants_matchStandardA4AndMetricScale() {
        assertEquals(595, PdfStaveExporter.PAGE_WIDTH_PT)
        assertEquals(842, PdfStaveExporter.PAGE_HEIGHT_PT)
        assertEquals(100f, PdfStaveExporter.STAVE_SIZE_MM, 0.01f)
        assertTrue(PdfStaveExporter.STAVE_SIZE_PT > 280f && PdfStaveExporter.STAVE_SIZE_PT < 285f)
    }

    @Test
    fun generateA4Pdf_withValidStave_createsPdfFileInExportsCache() = runTest {
        val result = PdfStaveExporter.generateA4Pdf(
            context = application,
            stave = composedStave,
            config = config,
            runes = testRunes,
            title = "Тестовый Став",
            layoutTitleRu = "Вязаная Руна",
            styleTitleRu = "Сакральное Золото",
            targetFileName = "test_export_a4.pdf"
        )

        assertTrue(result.isSuccess)
        val file = result.getOrNull()
        assertTrue(file != null && file.exists())
        assertTrue(file!!.length() > 0)
        assertEquals("test_export_a4.pdf", file.name)
    }

    @Test
    fun generateA4Pdf_withEmptyRunesList_handlesGracefully() = runTest {
        val emptyStave = StaveComposer.compose(emptyList(), StaveLayoutType.AEGISHJALMUR, seed = 42L)
        val result = PdfStaveExporter.generateA4Pdf(
            context = application,
            stave = emptyStave,
            config = config,
            runes = emptyList(),
            title = "Шлем Ужаса Без Рун",
            layoutTitleRu = "Шлем Ужаса",
            styleTitleRu = "Тату-Блэкворк",
            targetFileName = "empty_runes_a4.pdf"
        )

        assertTrue(result.isSuccess)
        val file = result.getOrNull()
        assertTrue(file != null && file.exists())
        assertTrue(file!!.length() > 0)
    }
}
