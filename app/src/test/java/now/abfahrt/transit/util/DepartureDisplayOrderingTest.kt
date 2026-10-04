package now.abfahrt.transit.util

import now.abfahrt.transit.data.model.Departure
import now.abfahrt.transit.data.model.DepartureSortProfile
import org.junit.Assert.assertEquals
import org.junit.Test

class DepartureDisplayOrderingTest {
    private val baseNow = 1_800_000_000_000L

    @Test
    fun nearbySortsByDistanceThenDepartureTimeThenDirection() {
        val departures = listOf(
            dep(stop = "B", line = "128", direction = "Zoo", stationDistance = 80, walkDistance = 80, minutes = 2),
            dep(stop = "A", line = "120", direction = "Alex", stationDistance = 80, walkDistance = 80, minutes = 12),
            dep(stop = "C", line = "50", direction = "Mitte", stationDistance = 160, walkDistance = 160, minutes = 1),
            dep(stop = "D", line = "999", direction = "West", stationDistance = 80, walkDistance = 80, minutes = 5)
        )

        val sorted = departures.sortedWith(
            DepartureDisplayOrdering.comparator(DepartureSortProfile.NEARBY)
        )

        assertEquals(listOf("B", "D", "A", "C"), sorted.map { it.stop })
    }

    @Test
    fun soonestDoesNotGiveHereAHiddenGlobalPriority() {
        val departures = listOf(
            dep(stop = "Far soon", line = "50", direction = "Center", stationDistance = 120, walkDistance = 120, minutes = 2),
            dep(stop = "Here later", line = "999", direction = "Center", stationDistance = 35, walkDistance = null, minutes = 30)
        )

        val sorted = departures.sortedWith(
            DepartureDisplayOrdering.comparator(DepartureSortProfile.SOONEST)
        )

        assertEquals(listOf("Far soon", "Here later"), sorted.map { it.stop })
        assertEquals(0, DepartureDisplayOrdering.effectiveDistanceForSort(sorted.last()))
    }

    @Test
    fun nearbyKeepsHereFirstThroughEffectiveDistanceZero() {
        val departures = listOf(
            dep(stop = "Far soon", line = "50", direction = "Center", stationDistance = 120, walkDistance = 120, minutes = 2),
            dep(stop = "Here later", line = "999", direction = "Center", stationDistance = 35, walkDistance = null, minutes = 30)
        )

        val sorted = departures.sortedWith(
            DepartureDisplayOrdering.comparator(DepartureSortProfile.NEARBY)
        )

        assertEquals("Here later", sorted.first().stop)
    }

    @Test
    fun lineGroupedPreservesLegacyDistanceLineTimeOrder() {
        val departures = listOf(
            dep(stop = "B", line = "128", direction = "Center", stationDistance = 80, walkDistance = 80, minutes = 2),
            dep(stop = "A", line = "120", direction = "Center", stationDistance = 80, walkDistance = 80, minutes = 12),
            dep(stop = "C", line = "50", direction = "Center", stationDistance = 160, walkDistance = 160, minutes = 1),
            dep(stop = "D", line = "120", direction = "Center", stationDistance = 80, walkDistance = 80, minutes = 5)
        )

        val sorted = departures.sortedWith(
            DepartureDisplayOrdering.comparator(DepartureSortProfile.LINE_GROUPED)
        )

        assertEquals(listOf("D", "A", "B", "C"), sorted.map { it.stop })
    }

    @Test
    fun walkDistanceWinsOverStationDistanceForDisplayedDistance() {
        val departure = dep(
            stop = "Curvy Walk",
            line = "50",
            direction = "Center",
            stationDistance = 200,
            walkDistance = 900,
            minutes = 5
        )

        assertEquals(900, DepartureDisplayOrdering.effectiveDistanceForSort(departure))
    }

    private fun dep(
        stop: String,
        line: String,
        direction: String,
        stationDistance: Int,
        walkDistance: Int?,
        minutes: Int
    ): Departure = Departure(
        line = line,
        direction = direction,
        time = "+${minutes}min",
        timestamp = baseNow + minutes * 60_000L,
        stop = stop,
        mode = "bus",
        stationDistance = stationDistance,
        walkDistance = walkDistance,
        walkDurationSeconds = walkDistance?.let { it / 80 * 60 },
        usesApproximateDistance = false
    )
}
