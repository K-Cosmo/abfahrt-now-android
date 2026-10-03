package now.abfahrt.transit.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Test

class RoutePreviewLookupTest {
    @Test
    fun removesMultipleProviderQualifiersAnywhereInStopName() {
        assertEquals(
            "franz neumann platz",
            routePreviewLookupName("U Franz-Neumann-Platz (Am Schäfersee) (Berlin)")
        )
    }

    @Test
    fun removesLeadingProviderCityPrefixForBerlinStops() {
        assertEquals(
            "grindelwaldweg",
            routePreviewLookupName("Berlin, Grindelwaldweg")
        )
    }

    @Test
    fun removesLeadingProviderCityPrefixForFrenchStops() {
        assertEquals(
            "châtelet les halles",
            routePreviewLookupName("Paris, Châtelet - Les Halles (Métro)")
        )
    }

    @Test
    fun removesLeadingProviderCityPrefixForSpanishStops() {
        assertEquals(
            "atocha",
            routePreviewLookupName("Madrid, Atocha (Cercanías) [Andén 2]")
        )
    }

    @Test
    fun removesLeadingProviderCityPrefixForGermanNonBerlinStops() {
        assertEquals(
            "heinrich heine allee",
            routePreviewLookupName("Düsseldorf, Heinrich-Heine-Allee (U)")
        )
    }

    @Test
    fun keepsAbbreviatedPlacePrefixWhenItLooksLikeARealDestinationName() {
        assertEquals(
            "franz buchholz guyotstr",
            routePreviewLookupName("Franz. Buchholz, Guyotstr.")
        )
    }
}
