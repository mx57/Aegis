package com.example.ui.screens

import com.example.data.local.DivinationRecord
import com.example.data.model.Rune
import com.example.data.model.RuneStroke
import com.example.data.model.StrokePoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DivinationSpreadTest {

    private lateinit var testElderRunes: List<Rune>

    @Before
    fun setUp() {
        testElderRunes = (1..24).map { i ->
            Rune(
                id = "rune_$i",
                futhark = "elder",
                nameRu = "Руна $i",
                nameEn = "Rune $i",
                unicode = "ᚠ",
                phonetic = "R",
                keywordsRu = listOf("сила", "дух"),
                divinationDirect = "Прямое толкование $i",
                divinationReversed = "Перевернутое толкование $i",
                magicUse = "Магия $i",
                tattooSymbolism = "Символ $i",
                strokes = listOf(RuneStroke(listOf(StrokePoint(50f, 0f), StrokePoint(50f, 140f))))
            )
        }
    }

    @Test
    fun yggdrasilRealms_containsNineRealms() {
        assertEquals(9, YGGDRASIL_REALMS.size)
        YGGDRASIL_REALMS.forEach { realm ->
            assertTrue("Realm ID should not be blank", realm.id.isNotBlank())
            assertTrue("Realm nameRu should not be blank", realm.nameRu.isNotBlank())
            assertTrue("Realm titleRu should not be blank", realm.titleRu.isNotBlank())
            assertTrue("Realm descriptionRu should not be blank", realm.descriptionRu.isNotBlank())
        }
    }

    @Test
    fun weekDays_containsSevenDays() {
        assertEquals(7, WEEK_DAYS.size)
        WEEK_DAYS.forEach { day ->
            assertTrue("Day nameRu should not be blank", day.dayNameRu.isNotBlank())
            assertTrue("DeityRu should not be blank", day.deityRu.isNotBlank())
            assertTrue("SphereRu should not be blank", day.sphereRu.isNotBlank())
        }
    }

    @Test
    fun yggdrasilDrawing_selectsNineUniqueRunes() {
        val selected = testElderRunes.shuffled().take(9)
        assertEquals(9, selected.size)
        val uniqueIds = selected.map { it.id }.toSet()
        assertEquals(9, uniqueIds.size)
    }

    @Test
    fun weekDrawing_selectsSevenUniqueRunes() {
        val selected = testElderRunes.shuffled().take(7)
        assertEquals(7, selected.size)
        val uniqueIds = selected.map { it.id }.toSet()
        assertEquals(7, uniqueIds.size)
    }

    @Test
    fun divinationRecord_yggdrasilParsing_returnsCorrectLists() {
        val record = DivinationRecord(
            spreadType = "YGGDRASIL",
            spreadTitleRu = "Древо Иггдрасиль (9 миров)",
            runeIdsCsv = "rune_1,rune_2,rune_3,rune_4,rune_5,rune_6,rune_7,rune_8,rune_9",
            reversedFlagsCsv = "0,1,0,0,1,1,0,0,1",
            questionOrContext = "Вопрошание Иггдрасиля",
            interpretationSummary = "Толкование 9 миров"
        )

        val idList = record.getRuneIdList()
        val revList = record.getReversedList()

        assertEquals(9, idList.size)
        assertEquals("rune_1", idList[0])
        assertEquals("rune_9", idList[8])

        assertEquals(9, revList.size)
        assertFalse(revList[0])
        assertTrue(revList[1])
        assertTrue(revList[8])
    }

    @Test
    fun divinationRecord_weekParsing_returnsCorrectLists() {
        val record = DivinationRecord(
            spreadType = "WEEK",
            spreadTitleRu = "Расклад на 7 дней недели",
            runeIdsCsv = "rune_10,rune_11,rune_12,rune_13,rune_14,rune_15,rune_16",
            reversedFlagsCsv = "1,0,0,1,0,1,0",
            questionOrContext = "Прогноз недели",
            interpretationSummary = "Толкование недели"
        )

        val idList = record.getRuneIdList()
        val revList = record.getReversedList()

        assertEquals(7, idList.size)
        assertEquals("rune_10", idList[0])
        assertEquals("rune_16", idList[6])

        assertEquals(7, revList.size)
        assertTrue(revList[0])
        assertFalse(revList[1])
        assertFalse(revList[6])
    }
}
