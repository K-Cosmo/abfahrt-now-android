package now.abfahrt.transit.ui.components

import now.abfahrt.transit.data.model.TransportMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ModeInferenceTest {

    @Test
    fun returnsNullForBlankLine() {
        assertNull(inferModeFromLine(null))
        assertNull(inferModeFromLine("   "))
    }

    @Test
    fun detectsRailPrefixes() {
        assertEquals(TransportMode.SUBURBAN, inferModeFromLine("S1"))
        assertEquals(TransportMode.SUBWAY, inferModeFromLine("U8"))
        assertEquals(TransportMode.REGIONAL, inferModeFromLine("RE1"))
        assertEquals(TransportMode.REGIONAL, inferModeFromLine("RB10"))
        assertEquals(TransportMode.REGIONAL, inferModeFromLine("FEX"))
        assertEquals(TransportMode.EXPRESS, inferModeFromLine("ICE"))
    }

    @Test
    fun keepsKnownBerlinTramsAsTram() {
        listOf(
            "M1", "M2", "M4", "M5", "M6", "M8", "M10", "M13", "M17",
            "12", "16", "18", "21", "27", "37", "50", "60", "61", "62", "63", "67", "68"
        ).forEach { line ->
            assertEquals(line, TransportMode.TRAM, inferModeFromLine(line))
        }
    }

    @Test
    fun classifiesBerlinMetroBusesAsBusWhenApiModeIsMissing() {
        listOf("M11", "M19", "M21", "M27", "M29", "M41", "M45", "M49", "M85").forEach { line ->
            assertEquals(line, TransportMode.BUS, inferModeFromLine(line))
        }
    }

    @Test
    fun classifiesKnownNumericBusLinesAsBusWhenApiModeIsMissing() {
        listOf("100", "200", "245").forEach { line ->
            assertEquals(line, TransportMode.BUS, inferModeFromLine(line))
        }
    }

    @Test
    fun detectsNumericFerryLines() {
        assertEquals(TransportMode.FERRY, inferModeFromLine("F10"))
    }
}
