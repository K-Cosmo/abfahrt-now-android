package now.abfahrt.transit.util

import org.junit.Assert.assertEquals
import org.junit.Test

class StationNameNormalizerTest {
    @Test
    fun stopDisplayRemovesGenericProviderPlacePrefixAndTrailingPlaceSuffix() {
        assertEquals(
            "Heinrich-Heine-Allee",
            StationNameNormalizer.stopDisplayName("Düsseldorf, Heinrich-Heine-Allee (Düsseldorf)")
        )
    }

    @Test
    fun directionDisplayKeepsCommaSeparatedDestinationPrefix() {
        assertEquals(
            "Wedding, Virchow-Klinikum",
            StationNameNormalizer.directionDisplayName("Wedding, Virchow-Klinikum (Berlin)")
        )
    }

    @Test
    fun routeLookupDoesNotCollapseAirportTerminalNamesToTerminalOnly() {
        assertEquals(
            "aéroport cdg terminal 2",
            StationNameNormalizer.routeLookupName("Aéroport CDG, Terminal 2")
        )
    }

    @Test
    fun canonicalStopNameUsesSharedGenericCleanup() {
        assertEquals(
            "Châtelet - Les Halles",
            StationNameNormalizer.canonicalStopName("Paris, Châtelet - Les Halles (Métro)", city = null)
        )
    }
}
