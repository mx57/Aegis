package com.example

import com.example.ui.components.BodyZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BodySilhouetteTest {

    @Test
    fun bodyZone_enumContainsAllEightZones() {
        val zones = BodyZone.values()
        assertEquals(8, zones.size)
        val zoneIds = zones.map { it.id }.toSet()
        val expectedIds = setOf("forearm", "shoulder", "wrist", "neck", "chest", "back", "calf", "ankle")
        assertEquals(expectedIds, zoneIds)
    }

    @Test
    fun bodyZone_allZonesHaveValidDimensionsAndTitles() {
        for (zone in BodyZone.values()) {
            assertNotNull(zone.titleRu)
            assertTrue("Title for ${zone.id} should not be blank", zone.titleRu.isNotBlank())
            assertTrue("Zone width for ${zone.id} should be positive", zone.zoneWidthCm > 0f)
            assertTrue("Zone height for ${zone.id} should be positive", zone.zoneHeightCm > 0f)
        }
    }

    @Test
    fun bodyZone_lookupById_returnsCorrectZone() {
        val forearm = BodyZone.values().find { it.id == "forearm" }
        assertNotNull(forearm)
        assertEquals("Предплечье", forearm?.titleRu)
        assertEquals(9.0f, forearm?.zoneWidthCm)
        assertEquals(26.0f, forearm?.zoneHeightCm)
    }
}
