package now.abfahrt.transit.util

import now.abfahrt.transit.data.model.Departure
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class DepartureServiceIdentityTest {
    @Test
    fun trailingProviderCityDoesNotCreateAnotherDirection() {
        assertEquals(
            DepartureServiceIdentity.lineDirectionKey("128", "U Osloer Str."),
            DepartureServiceIdentity.lineDirectionKey("128", "U Osloer Str. (Berlin)")
        )
    }

    @Test
    fun genuinelyDifferentDirectionsStaySeparate() {
        assertNotEquals(
            DepartureServiceIdentity.lineDirectionKey("128", "U Osloer Str."),
            DepartureServiceIdentity.lineDirectionKey("128", "U Kurt-Schumacher-Platz")
        )
    }

    @Test
    fun lineWhitespaceAndCaseDoNotChangeIdentity() {
        val a = departure(line = " M13 ", direction = "S Warschauer Str. (Berlin)")
        val b = departure(line = "m13", direction = "S Warschauer Str.")

        assertEquals(
            DepartureServiceIdentity.lineDirectionKey(a),
            DepartureServiceIdentity.lineDirectionKey(b)
        )
    }

    private fun departure(line: String, direction: String): Departure = Departure(
        line = line,
        direction = direction,
        time = "5 min",
        timestamp = 1_800_000_000_000L,
        stop = "Test stop"
    )
}
