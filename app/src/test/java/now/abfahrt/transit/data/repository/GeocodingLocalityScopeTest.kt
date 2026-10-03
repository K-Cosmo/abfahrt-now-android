package now.abfahrt.transit.data.repository

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Legacy filename intentionally retained for source-overlay compatibility.
 * Build 148 supersedes locality scoping with D-069: Photon receives the trimmed
 * raw user query and uses lat/lon only as a location bias.
 */
class GeocodingLocalityScopeTest {

    @Test
    fun `keeps local multi word query unchanged`() {
        assertEquals("Brandenburger Tor", placeQueryForPhoton("Brandenburger Tor"))
    }

    @Test
    fun `keeps remote multi word municipality unchanged`() {
        assertEquals("Bad Saarow", placeQueryForPhoton("Bad Saarow"))
    }

    @Test
    fun `keeps remote city and transit query unchanged`() {
        assertEquals("Potsdam", placeQueryForPhoton("Potsdam"))
        assertEquals("S Potsdam", placeQueryForPhoton("S Potsdam"))
        assertEquals("Potsdam Hauptbahnhof", placeQueryForPhoton("Potsdam Hauptbahnhof"))
    }

    @Test
    fun `keeps explicit locality and postal code unchanged`() {
        assertEquals("Hauptbahnhof, Bremen", placeQueryForPhoton("Hauptbahnhof, Bremen"))
        assertEquals("28195 Hauptbahnhof", placeQueryForPhoton("28195 Hauptbahnhof"))
    }

    @Test
    fun `trims only outer whitespace`() {
        assertEquals("Bad Saarow", placeQueryForPhoton("  Bad Saarow  "))
    }
}
