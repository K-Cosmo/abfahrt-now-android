package now.abfahrt.transit.ui.screens

import now.abfahrt.transit.data.model.Trip
import now.abfahrt.transit.data.model.TripIntermediateStop
import now.abfahrt.transit.data.model.TripLeg
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNull
import org.junit.Test

class RouteTripPresentationTest {

    @Test
    fun `coordinate endpoints are replaced by fallback label`() {
        assertEquals(
            "Aktueller Standort",
            tripEndpointDisplayLabel("52.558673, 13.360095", fallback = "Aktueller Standort")
        )
    }

    @Test
    fun `provider stop ids are treated as technical`() {
        assertTrue(looksTechnicalRouteReference("900009173"))
        assertTrue(looksTechnicalRouteReference("de:11000:900009173"))
        assertFalse(looksTechnicalRouteReference("U Bernauer Str. (Berlin)"))
    }

    @Test
    fun `provider context is removed from normal stop names`() {
        assertEquals(
            "U Eberswalder Str.",
            tripEndpointDisplayLabel("U Eberswalder Str. (Berlin)")
        )
    }

    @Test
    fun `walking legs are detected via mode or coordinate pattern`() {
        val explicitWalk = TripLeg(
            line = "Walk",
            direction = "52.558673, 13.360095",
            mode = null,
            from = "52.558673, 13.360095",
            to = "U Franz-Neumann-Platz (Am Schäfersee) (Berlin)",
            departure = 0L,
            arrival = 0L
        )
        val transitLeg = explicitWalk.copy(
            line = "U8",
            mode = "subway",
            from = "U Franz-Neumann-Platz (Am Schäfersee) (Berlin)",
            to = "U Bernauer Str. (Berlin)"
        )

        assertTrue(isWalkingLeg(explicitWalk))
        assertFalse(isWalkingLeg(transitLeg))
    }

    @Test
    fun `transfer minutes are only returned for real positive gaps`() {
        val previous = TripLeg(
            line = "327",
            direction = "U Leopoldplatz",
            mode = "bus",
            from = "A",
            to = "B",
            departure = 1_000_000L,
            arrival = 1_200_000L
        )
        val current = previous.copy(
            line = "147",
            direction = "Hauptbahnhof",
            from = "B",
            to = "C",
            departure = 1_320_000L,
            arrival = 1_500_000L
        )

        assertEquals(2, routeTransferMinutes(previous, current))
        assertNull(routeTransferMinutes(previous, current.copy(departure = 1_240_000L)))
        assertNull(routeTransferMinutes(previous, current.copy(sameVehicle = true)))
    }

    @Test
    fun `intermediate stop display prefers timed stops and cleans provider context`() {
        val leg = TripLeg(
            line = "U8",
            direction = "Hermannstraße",
            mode = "subway",
            from = "A",
            to = "B",
            departure = 0L,
            arrival = 10L,
            stops = 2,
            stopNames = listOf("Fallback 1", "Fallback 2"),
            intermediateStops = listOf(
                TripIntermediateStop("U Osloer Str. (Berlin)", 5L),
                TripIntermediateStop("900009173", 6L)
            )
        )

        val display = routeIntermediateStopsForDisplay(leg)
        assertEquals(1, display.size)
        assertEquals("U Osloer Str.", display.single().name)
        assertEquals(5L, display.single().arrival)
    }

    @Test
    fun `route sorting uses deterministic business priorities`() {
        val earlySlow = trip(
            departure = 1_000_000L,
            duration = 30,
            changes = 0,
            walkingMinutes = 8
        )
        val laterFast = trip(
            departure = 1_120_000L,
            duration = 15,
            changes = 1,
            walkingMinutes = 6
        )
        val laterFewChanges = trip(
            departure = 1_180_000L,
            duration = 20,
            changes = 0,
            walkingMinutes = 10
        )
        val laterLeastWalk = trip(
            departure = 1_240_000L,
            duration = 22,
            changes = 1,
            walkingMinutes = 2
        )
        val input = listOf(laterFewChanges, laterFast, laterLeastWalk, earlySlow)

        assertEquals(earlySlow, sortTripsForRoute(input, RouteSortMode.EARLIEST).first())
        assertEquals(laterFast, sortTripsForRoute(input, RouteSortMode.FASTEST).first())
        assertEquals(laterFewChanges, sortTripsForRoute(input, RouteSortMode.FEWEST_CHANGES).first())
        assertEquals(laterLeastWalk, sortTripsForRoute(input, RouteSortMode.LEAST_WALKING).first())
    }

    @Test
    fun `walking metric only sums walking legs and clamps negative durations`() {
        val walk = TripLeg(
            line = "Walk",
            direction = "",
            mode = null,
            from = "A",
            to = "B",
            departure = 0L,
            arrival = 180_000L
        )
        val transit = TripLeg(
            line = "U8",
            direction = "Hermannstraße",
            mode = "subway",
            from = "B",
            to = "C",
            departure = 180_000L,
            arrival = 600_000L
        )
        val invalidWalk = walk.copy(departure = 900_000L, arrival = 800_000L)
        val trip = Trip(
            legs = listOf(walk, transit, invalidWalk),
            departure = 0L,
            arrival = 900_000L,
            duration = 15,
            changes = 0
        )

        assertEquals(180_000L, routeWalkingMillis(trip))
        assertEquals(3, routeLegDurationMinutes(walk))
        assertEquals(0, routeLegDurationMinutes(invalidWalk))
    }

    @Test
    fun `walking navigation prefers raw destination coordinates`() {
        val leg = TripLeg(
            line = "Walk",
            direction = "",
            mode = null,
            from = "U Seestr.",
            to = "52.5435823, 13.3397881",
            departure = 0L,
            arrival = 60_000L
        )

        assertEquals(
            "52.5435823, 13.3397881",
            routeWalkingNavigationQuery(leg, "Seestraße 10")
        )
    }

    @Test
    fun `walking navigation falls back to cleaned destination label`() {
        val leg = TripLeg(
            line = "Walk",
            direction = "",
            mode = null,
            from = "52.558673, 13.360095",
            to = "U Osloer Str. (Berlin)",
            departure = 0L,
            arrival = 60_000L
        )

        assertEquals(
            "U Osloer Str.",
            routeWalkingNavigationQuery(leg, "U Osloer Str.")
        )
    }

    private fun trip(
        departure: Long,
        duration: Int,
        changes: Int,
        walkingMinutes: Int
    ): Trip {
        val walkDuration = walkingMinutes * 60_000L
        val arrival = departure + duration * 60_000L
        val walk = TripLeg(
            line = "Walk",
            direction = "",
            mode = null,
            from = "Start",
            to = "Stop",
            departure = departure,
            arrival = departure + walkDuration
        )
        val transit = TripLeg(
            line = "U8",
            direction = "Hermannstraße",
            mode = "subway",
            from = "Stop",
            to = "Ziel",
            departure = departure + walkDuration,
            arrival = arrival
        )
        return Trip(
            legs = listOf(walk, transit),
            departure = departure,
            arrival = arrival,
            duration = duration,
            changes = changes
        )
    }

}
