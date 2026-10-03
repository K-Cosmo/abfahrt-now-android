package now.abfahrt.transit.util

import now.abfahrt.transit.data.model.Departure
import org.junit.Assert.assertEquals
import org.junit.Test

class DepartureDisplayOrderingTest {
    private val baseNow = 1_800_000_000_000L

    @Test
    fun hereDeparturesAlwaysSortFirst() {
        val departures = listOf(
            dep(stop = "Far", line = "50", stationDistance = 120, walkDistance = 120, minutes = 2),
            dep(stop = "Here", line = "999", stationDistance = 35, walkDistance = null, minutes = 30)
        )

        val sorted = departures.sortedWith(DepartureDisplayOrdering.comparator())

        assertEquals("Here", sorted.first().stop)
        assertEquals(0, DepartureDisplayOrdering.effectiveDistanceForSort(sorted.first()))
    }

    @Test
    fun sortsByDistanceThenLineThenDepartureTime() {
        val departures = listOf(
            dep(stop = "B", line = "128", stationDistance = 80, walkDistance = 80, minutes = 2),
            dep(stop = "A", line = "120", stationDistance = 80, walkDistance = 80, minutes = 12),
            dep(stop = "C", line = "50", stationDistance = 160, walkDistance = 160, minutes = 1),
            dep(stop = "D", line = "120", stationDistance = 80, walkDistance = 80, minutes = 5)
        )

        val sorted = departures.sortedWith(DepartureDisplayOrdering.comparator())

        assertEquals(listOf("D", "A", "B", "C"), sorted.map { it.stop })
    }

    @Test
    fun walkDistanceWinsOverStationDistanceForDisplayedDistance() {
        val departure = dep(
            stop = "Curvy Walk",
            line = "50",
            stationDistance = 200,
            walkDistance = 900,
            minutes = 5
        )

        assertEquals(900, DepartureDisplayOrdering.effectiveDistanceForSort(departure))
    }

    private fun dep(
        stop: String,
        line: String,
        stationDistance: Int,
        walkDistance: Int?,
        minutes: Int
    ): Departure = Departure(
        line = line,
        direction = "Center",
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
