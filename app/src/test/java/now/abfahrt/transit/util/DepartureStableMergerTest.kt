package now.abfahrt.transit.util

import now.abfahrt.transit.data.model.Departure
import now.abfahrt.transit.data.model.DepartureResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DepartureStableMergerTest {
    private val baseNow = 1_800_000_000_000L

    @Test
    fun stableRefreshPreservesPreviousWhenIncomingIsTransientlySmall() {
        val previous = DepartureResponse(
            region = "de-BE",
            realtime = true,
            departures = (1..9).map { idx ->
                dep(
                    line = "L$idx",
                    direction = "Center",
                    stop = "Stop $idx",
                    minutesFromNow = idx + 5,
                    walkDistance = 400 + idx
                )
            }
        )
        val incoming = DepartureResponse(
            region = "de-BE",
            realtime = true,
            departures = listOf(dep(line = "L1", direction = "Center", stop = "Stop 1", minutesFromNow = 7, walkDistance = null))
        )

        val merged = DepartureStableMerger.mergeResponse(
            previous = previous,
            incoming = incoming,
            matchWindowMinutes = DepartureStableMerger.DEFAULT_STABLE_MATCH_WINDOW_MINUTES,
            preservePrevious = true,
            nowMillis = baseNow
        )

        assertTrue("Stable merge must not collapse a visible list to the transient incoming size", merged.departures.size >= 9)
    }

    @Test
    fun hardReplacementDoesNotPreservePreviousWhenStableMergeIsDisabled() {
        val previous = DepartureResponse(
            region = "de-BE",
            realtime = true,
            departures = (1..9).map { idx -> dep(line = "L$idx", direction = "Center", stop = "Stop $idx", minutesFromNow = idx + 5) }
        )
        val incoming = DepartureResponse(
            region = "de-BE",
            realtime = true,
            departures = listOf(dep(line = "L1", direction = "Center", stop = "Stop 1", minutesFromNow = 7))
        )

        val merged = DepartureStableMerger.mergeResponse(
            previous = previous,
            incoming = incoming,
            matchWindowMinutes = DepartureStableMerger.DEFAULT_STABLE_MATCH_WINDOW_MINUTES,
            preservePrevious = false,
            nowMillis = baseNow
        )

        assertEquals(1, merged.departures.size)
    }

    @Test
    fun denseTaktDeparturesSixMinutesApartAreNotMerged() {
        val previous = DepartureResponse(
            region = "de-BE",
            realtime = true,
            departures = listOf(dep(line = "U8", direction = "Wittenau", stop = "U Osloer Str.", minutesFromNow = 6))
        )
        val incoming = DepartureResponse(
            region = "de-BE",
            realtime = true,
            departures = listOf(dep(line = "U8", direction = "Wittenau", stop = "U Osloer Str.", minutesFromNow = 12))
        )

        val merged = DepartureStableMerger.mergeResponse(
            previous = previous,
            incoming = incoming,
            matchWindowMinutes = 10, // Production clamps to max 5 min to protect dense service.
            preservePrevious = true,
            nowMillis = baseNow
        )

        assertEquals(2, merged.departures.size)
    }

    @Test
    fun sameDepartureWithinThreeMinutesIsUpdatedNotDuplicated() {
        val previous = DepartureResponse(
            region = "de-BE",
            realtime = true,
            departures = listOf(dep(line = "U8", direction = "Wittenau", stop = "U Osloer Str.", minutesFromNow = 6, walkDistance = 900))
        )
        val incoming = DepartureResponse(
            region = "de-BE",
            realtime = true,
            departures = listOf(dep(line = "U8", direction = "Wittenau", stop = "U Osloer Str.", minutesFromNow = 8, walkDistance = null))
        )

        val merged = DepartureStableMerger.mergeResponse(
            previous = previous,
            incoming = incoming,
            matchWindowMinutes = DepartureStableMerger.DEFAULT_STABLE_MATCH_WINDOW_MINUTES,
            preservePrevious = true,
            nowMillis = baseNow
        )

        assertEquals(1, merged.departures.size)
        assertEquals(baseNow + 8 * 60_000L, merged.departures.single().timestamp)
        assertEquals("Existing ORS distance should survive an incoming station-only update", 900, merged.departures.single().walkDistance)
    }


    @Test
    fun providerStopIdMatchesNamedPreviousWithRawIncoming() {
        val previous = DepartureResponse(
            region = "de-BE",
            realtime = true,
            departures = listOf(
                dep(
                    line = "M13",
                    direction = "S Warschauer Str.",
                    stop = "Louise-Schroeder-Platz (Berlin)",
                    minutesFromNow = 9,
                    providerStopId = "de:11000:900011201::5"
                )
            )
        )
        val incoming = DepartureResponse(
            region = "de-BE",
            realtime = true,
            departures = listOf(
                dep(
                    line = "M13",
                    direction = "S Warschauer Str. (Berlin)",
                    stop = "de:11000:900011201::5",
                    minutesFromNow = 10,
                    providerStopId = "de:11000:900011201::5"
                )
            )
        )

        val merged = DepartureStableMerger.mergeResponse(
            previous = previous,
            incoming = incoming,
            matchWindowMinutes = DepartureStableMerger.DEFAULT_STABLE_MATCH_WINDOW_MINUTES,
            preservePrevious = true,
            nowMillis = baseNow
        )

        assertEquals(1, merged.departures.size)
        assertEquals("Louise-Schroeder-Platz (Berlin)", merged.departures.single().stop)
        assertEquals("de:11000:900011201::5", merged.departures.single().providerStopId)
        assertEquals(baseNow + 10 * 60_000L, merged.departures.single().timestamp)
    }

    @Test
    fun providerCitySuffixDoesNotPreventStableMerge() {
        val previous = DepartureResponse(
            region = "de-BE",
            realtime = true,
            departures = listOf(
                dep(
                    line = "128",
                    direction = "U Osloer Str.",
                    stop = "Brienzer Str. (Berlin)",
                    minutesFromNow = 20
                )
            )
        )
        val incoming = DepartureResponse(
            region = "de-BE",
            realtime = true,
            departures = listOf(
                dep(
                    line = "128",
                    direction = "U Osloer Str. (Berlin)",
                    stop = "Brienzer Str. (Berlin)",
                    minutesFromNow = 21
                )
            )
        )

        val merged = DepartureStableMerger.mergeResponse(
            previous = previous,
            incoming = incoming,
            matchWindowMinutes = DepartureStableMerger.DEFAULT_STABLE_MATCH_WINDOW_MINUTES,
            preservePrevious = true,
            nowMillis = baseNow
        )

        assertEquals(1, merged.departures.size)
        assertEquals(baseNow + 21 * 60_000L, merged.departures.single().timestamp)
    }

    @Test
    fun mergedDeparturesUseCentralDisplayOrdering() {
        val incoming = DepartureResponse(
            region = "de-BE",
            realtime = true,
            departures = listOf(
                dep(line = "50", direction = "Center", stop = "Far", minutesFromNow = 1, walkDistance = 160),
                dep(line = "128", direction = "Center", stop = "Closer Later", minutesFromNow = 2, walkDistance = 80),
                dep(line = "120", direction = "Center", stop = "Closer Earlier", minutesFromNow = 5, walkDistance = 80)
            )
        )

        val merged = DepartureStableMerger.mergeResponse(
            previous = null,
            incoming = incoming,
            matchWindowMinutes = DepartureStableMerger.DEFAULT_STABLE_MATCH_WINDOW_MINUTES,
            preservePrevious = false,
            nowMillis = baseNow
        )

        assertEquals(listOf("Closer Earlier", "Closer Later", "Far"), merged.departures.map { it.stop })
    }

    private fun dep(
        line: String,
        direction: String,
        stop: String,
        minutesFromNow: Int,
        walkDistance: Int? = 600,
        providerStopId: String? = null
    ): Departure = Departure(
        line = line,
        direction = direction,
        time = "+${minutesFromNow}min",
        timestamp = baseNow + minutesFromNow * 60_000L,
        stop = stop,
        mode = if (line.startsWith("U")) "subway" else "bus",
        stationDistance = walkDistance ?: 500,
        walkDistance = walkDistance,
        walkDurationSeconds = walkDistance?.let { it / 80 * 60 },
        usesApproximateDistance = false,
        providerStopId = providerStopId
    )
}
