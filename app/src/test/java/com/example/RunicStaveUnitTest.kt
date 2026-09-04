package com.example

import com.example.data.model.Rune
import com.example.data.model.RuneStroke
import com.example.data.model.StrokePoint
import com.example.data.translit.RuneTransliteration
import com.example.engine.SketchConfig
import com.example.engine.SketchStyle
import com.example.engine.StaveComposer
import com.example.engine.StaveLayoutType
import com.example.engine.SvgStaveRenderer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RunicStaveUnitTest {

    private lateinit var testRunes: List<Rune>

    @Before
    fun setUp() {
        testRunes = listOf(
            Rune("tiwaz", "elder", "Тейваз", "Tiwaz", "ᛏ", "Т", listOf("победа"), "Победа", "Слабость", "Оберег", "Честь",
                listOf(RuneStroke(listOf(StrokePoint(50f, 0f), StrokePoint(50f, 140f))))),
            Rune("sowilo", "elder", "Соуло", "Sowilo", "ᛋ", "С", listOf("солнце"), "Успех", "Ожог", "Энергия", "Триумф",
                listOf(RuneStroke(listOf(StrokePoint(20f, 0f), StrokePoint(80f, 70f))))),
            Rune("ansuz", "elder", "Ансуз", "Ansuz", "ᚨ", "А", listOf("мудрость"), "Слово", "Ложь", "Вдохновение", "Голос",
                listOf(RuneStroke(listOf(StrokePoint(35f, 0f), StrokePoint(35f, 140f))))),
            Rune("uruz", "elder", "Уруз", "Uruz", "ᚢ", "У", listOf("сила"), "Мощь", "Упадок", "Здоровье", "Зубр",
                listOf(RuneStroke(listOf(StrokePoint(35f, 0f), StrokePoint(35f, 140f))))),
            Rune("jera", "elder", "Йера", "Jera", "ᛃ", "Й", listOf("урожай"), "Цикл", "Застой", "Изобилие", "Плод",
                listOf(RuneStroke(listOf(StrokePoint(50f, 20f), StrokePoint(80f, 70f))))),
            Rune("algiz", "elder", "Альгиз", "Algiz", "ᛉ", "З", listOf("защита"), "Щит", "Уязвимость", "Охрана", "Осока",
                listOf(RuneStroke(listOf(StrokePoint(50f, 0f), StrokePoint(50f, 140f)))))
        )
    }

    @Test
    fun transliteration_caseInsensitive() {
        val upper = RuneTransliteration.transliterate("АННА", testRunes)
        val lower = RuneTransliteration.transliterate("анна", testRunes)
        assertEquals(upper.runes.map { it.id }, lower.runes.map { it.id })
    }

    @Test
    fun transliteration_compoundLetters_YuAndYa() {
        // Ю -> Й + У
        val resYu = RuneTransliteration.transliterate("Ю", testRunes)
        assertEquals(listOf("jera", "uruz"), resYu.runes.map { it.id })

        // Я -> Й + А
        val resYa = RuneTransliteration.transliterate("Я", testRunes)
        assertEquals(listOf("jera", "ansuz"), resYa.runes.map { it.id })
    }

    @Test
    fun transliteration_omitsSilentSigns() {
        val res = RuneTransliteration.transliterate("СЪЕЗДЬ", testRunes)
        // Should ignore Ъ and Ь with explanatory note
        assertTrue(res.notes.any { it.contains("не имеет прямого рунического звука") })
    }

    @Test
    fun svgGeneration_hasValidXmlTagsAndDimensions() {
        val stave = StaveComposer.compose(testRunes, StaveLayoutType.BINDRUNE)
        val config = SketchConfig(style = SketchStyle.ORNAMENTAL, seed = 12345L)
        val svg = SvgStaveRenderer.renderSvg(stave, config)

        assertTrue("SVG should start with <svg tag", svg.startsWith("<svg"))
        assertTrue("SVG should contain viewBox", svg.contains("""viewBox="0 0 500 500""""))
        assertTrue("SVG should close with </svg>", svg.trim().endsWith("</svg>"))
        assertTrue("SVG content should not be empty", svg.length > 100)
    }

    @Test
    fun seedDeterminism_sameSeedProducesIdenticalSvg() {
        val stave = StaveComposer.compose(testRunes, StaveLayoutType.BINDRUNE)
        val config1 = SketchConfig(style = SketchStyle.ORNAMENTAL, wobbleAmount = 0.3f, seed = 777L)
        val config2 = SketchConfig(style = SketchStyle.ORNAMENTAL, wobbleAmount = 0.3f, seed = 777L)

        val svg1 = SvgStaveRenderer.renderSvg(stave, config1)
        val svg2 = SvgStaveRenderer.renderSvg(stave, config2)

        assertEquals(svg1, svg2)

        val configDiff = SketchConfig(style = SketchStyle.ORNAMENTAL, wobbleAmount = 0.3f, seed = 888L)
        val svgDiff = SvgStaveRenderer.renderSvg(stave, configDiff)
        assertNotEquals(svg1, svgDiff)
    }

    @Test
    fun runeCoordinates_withinNormalizedBounds() {
        for (rune in testRunes) {
            for (stroke in rune.strokes) {
                for (pt in stroke.points) {
                    assertTrue("X point ${pt.x} in rune ${rune.id} out of 0..100", pt.x in 0f..100f)
                    assertTrue("Y point ${pt.y} in rune ${rune.id} out of 0..140", pt.y in 0f..140f)
                }
            }
        }
    }

    @Test
    fun allNewLayouts_produceStrokes() {
        for (layout in StaveLayoutType.values()) {
            val composed = StaveComposer.compose(testRunes, layout, seed = 1337L)
            assertTrue("Layout ${layout.name} should generate strokes", composed.strokes.isNotEmpty())
        }
    }

    @Test
    fun svgGeneration_includesOrnamentsAndStyles() {
        val stave = StaveComposer.compose(testRunes, StaveLayoutType.AEGISHJALMUR, seed = 42L)
        val config = SketchConfig(
            style = SketchStyle.AEGISHJALMUR,
            frameStyle = com.example.engine.FrameStyle.SACRED_OCTAGON,
            finialType = com.example.engine.FinialType.TRIDENT,
            centerEmblem = com.example.engine.CenterEmblem.AEGISHJALMUR_CORE,
            cornerStyle = com.example.engine.CornerStyle.NORSE_KNOTS,
            hasBranchNotches = true,
            hasRayBurst = true
        )
        val svg = SvgStaveRenderer.renderSvg(stave, config)
        assertTrue(svg.contains("<polygon") || svg.contains("<line") || svg.contains("<circle"))
        assertTrue("Should contain lines for rays or notches", svg.contains("<line"))
        assertTrue(svg.trim().endsWith("</svg>"))
    }

    @Test
    fun svgGeneration_celestialAstrolabe_rendersFiligreeOrbits() {
        val stave = StaveComposer.compose(testRunes, StaveLayoutType.BINDRUNE, seed = 999L)
        val config = SketchConfig(
            style = SketchStyle.SACRED_GOLD,
            frameStyle = com.example.engine.FrameStyle.CELESTIAL_ASTROLABE,
            finialType = com.example.engine.FinialType.ARROWS,
            centerEmblem = com.example.engine.CenterEmblem.FACETED_STAR
        )
        val svg = SvgStaveRenderer.renderSvg(stave, config)
        assertTrue("SVG should contain circle element for astrolabe orbit", svg.contains("""<circle cx="250""""))
        assertTrue("SVG should contain degree marks", svg.contains("<line"))
        assertTrue("SVG should end with valid closing tag", svg.trim().endsWith("</svg>"))
    }

    @Test
    fun svgGeneration_volumetricShadingAnd3dGradients() {
        val stave = StaveComposer.compose(testRunes, StaveLayoutType.BINDRUNE, seed = 2026L)
        val configGold = SketchConfig(
            style = SketchStyle.SACRED_GOLD,
            theme = com.example.engine.CanvasTheme.GOLDEN_EMBER,
            hasVolumetricShading = true
        )
        val svgGold = SvgStaveRenderer.renderSvg(stave, configGold)
        assertTrue("Gold SVG should contain 3D linearGradient def", svgGold.contains("""id="gold3dGrad""""))
        assertTrue("Gold SVG should contain drop shadow filter", svgGold.contains("""id="chiselDropShadow""""))
        assertTrue("Gold SVG should render specular highlight core lines", svgGold.contains("""transform="translate(-0.4, -0.5)"""") || svgGold.contains("""stroke="#FFF3BC""""))

        val configBronze = SketchConfig(
            style = SketchStyle.EMERALD_BRONZE,
            theme = com.example.engine.CanvasTheme.EMERALD_PATINA,
            hasVolumetricShading = true
        )
        val svgBronze = SvgStaveRenderer.renderSvg(stave, configBronze)
        assertTrue("Bronze SVG should be valid and non-empty", svgBronze.length > 200)

        val configFrost = SketchConfig(
            style = SketchStyle.FROST_CRYSTAL,
            theme = com.example.engine.CanvasTheme.FROST_ICE,
            hasVolumetricShading = true
        )
        val svgFrost = SvgStaveRenderer.renderSvg(stave, configFrost)
        assertTrue("Frost SVG should be valid and non-empty", svgFrost.length > 200)
    }

    @Test
    fun svgGeneration_yggdrasilTreeAndBranches_rendersCorrectly() {
        val stave = StaveComposer.compose(testRunes, StaveLayoutType.BINDRUNE, seed = 7777L)
        val configYggdrasil = SketchConfig(
            style = SketchStyle.EMERALD_BRONZE,
            theme = com.example.engine.CanvasTheme.EMERALD_PATINA,
            frameStyle = com.example.engine.FrameStyle.YGGDRASIL_BRANCHES,
            centerEmblem = com.example.engine.CenterEmblem.YGGDRASIL_TREE,
            hasVolumetricShading = true
        )
        val svg = SvgStaveRenderer.renderSvg(stave, configYggdrasil)
        assertTrue("SVG should contain paths for Yggdrasil tree trunk and roots", svg.contains("<path"))
        assertTrue("SVG should contain circles for 9 worlds orbs", svg.contains("<circle"))
        assertTrue("SVG should end with valid closing tag", svg.trim().endsWith("</svg>"))
    }

    @Test
    fun svgGeneration_runicSerpent_rendersDetailedJellingSerpent() {
        val stave = StaveComposer.compose(testRunes, StaveLayoutType.BINDRUNE, seed = 5555L)
        val configSerpent = SketchConfig(
            style = SketchStyle.WOODCARVE,
            theme = com.example.engine.CanvasTheme.RUNESTONE_GRAY,
            frameStyle = com.example.engine.FrameStyle.RUNIC_SERPENT,
            centerEmblem = com.example.engine.CenterEmblem.VALKNUT
        )
        val svg = SvgStaveRenderer.renderSvg(stave, configSerpent)
        assertTrue("SVG should contain drop shadow circles for serpent body", svg.contains("""opacity="0.2""""))
        assertTrue("SVG should contain polygon elements for fangs and tail barb", svg.contains("<polygon"))
        assertTrue("SVG should contain path elements for snout, jaw, tongue and tail knot", svg.contains("<path"))
        assertTrue("SVG should end with valid closing tag", svg.trim().endsWith("</svg>"))
    }

    @Test
    fun singleRuneIcon_strokeDataValidation_handlesSingleAndMultiPointStrokes() {
        val dotRune = Rune(
            id = "dot_rune",
            futhark = "elder",
            nameRu = "Точка",
            nameEn = "Dot",
            unicode = "•",
            phonetic = "•",
            keywordsRu = listOf("узел"),
            divinationDirect = "Узел",
            divinationReversed = "Узел",
            magicUse = "Точка",
            tattooSymbolism = "Центр",
            strokes = listOf(
                RuneStroke(listOf(StrokePoint(50f, 70f))),
                RuneStroke(listOf(StrokePoint(20f, 10f), StrokePoint(80f, 130f)))
            )
        )
        assertEquals(2, dotRune.strokes.size)
        assertEquals(1, dotRune.strokes[0].points.size)
        assertEquals(2, dotRune.strokes[1].points.size)
    }
}
