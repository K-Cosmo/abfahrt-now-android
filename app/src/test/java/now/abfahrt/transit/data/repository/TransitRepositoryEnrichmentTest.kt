package now.abfahrt.transit.data.repository

import now.abfahrt.transit.data.model.Departure
import now.abfahrt.transit.data.model.Station
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TransitRepositoryEnrichmentTest {
    @Test
    fun resolvesExactProviderStopIdToStationName() {
        val departures = listOf(
            departure(stop = "de:11000:900011201::6")
        )
        val stations = listOf(
            station(
                id = "de:11000:900011201::6",
                name = "Louise-Schroeder-Platz (Berlin)",
                distance = 424
            )
        )

        val enriched = enrichDeparturesWithStations(departures, stations, city = "Berlin")

        assertEquals("Louise-Schroeder-Platz (Berlin)", enriched.single().stop)
        assertEquals(424, enriched.single().stationDistance)
    }

    @Test
    fun resolvesPlatformProviderStopIdViaBaseStationId() {
        val departures = listOf(
            departure(stop = "de:11000:900011201::6")
        )
        val stations = listOf(
            station(
                id = "de:11000:900011201",
                name = "Louise-Schroeder-Platz (Berlin)",
                distance = 424
            )
        )

        val enriched = enrichDeparturesWithStations(departures, stations, city = "Berlin")

        assertEquals("Louise-Schroeder-Platz (Berlin)", enriched.single().stop)
        assertEquals(424, enriched.single().stationDistance)
    }

    @Test
    fun resolvesPlatformProviderStopIdToNearestStationWhenBaseMatchesMultiplePlatforms() {
        val departures = listOf(
            departure(stop = "de:11000:900011207::2")
        )
        val stations = listOf(
            station(
                id = "de:11000:900011207::1",
                name = "U Osloer Str. (Berlin)",
                distance = 441
            ),
            station(
                id = "de:11000:900011207",
                name = "U Osloer Str. (Berlin)",
                distance = 456
            )
        )

        val enriched = enrichDeparturesWithStations(departures, stations, city = "Berlin")

        assertEquals("U Osloer Str. (Berlin)", enriched.single().stop)
        assertEquals(441, enriched.single().stationDistance)
    }

    @Test
    fun doesNotAssignProviderStopIdToDifferentPhysicalStopWhenBaseIdIsAmbiguous() {
        val departures = listOf(
            departure(line = "128", direction = "U Osloer Str.", stop = "de:11000:900011207::2")
        )
        val stations = listOf(
            station(
                id = "de:11000:900011207::1",
                name = "U Osloer Str. (Berlin)",
                distance = 441
            ),
            station(
                id = "de:11000:900011207",
                name = "Walderseestr. (Berlin)",
                distance = 370
            )
        )

        val enriched = enrichDeparturesWithStations(departures, stations, city = "Berlin")

        assertEquals("de:11000:900011207::2", enriched.single().stop)
        assertEquals(0, enriched.single().stationDistance)
    }

    @Test
    fun stillResolvesProviderStopIdWhenBaseCandidatesAreSamePhysicalStop() {
        val departures = listOf(
            departure(stop = "de:11000:900011207::2")
        )
        val stations = listOf(
            station(
                id = "de:11000:900011207::1",
                name = "U Osloer Str. (Berlin)",
                distance = 441
            ),
            station(
                id = "de:11000:900011207",
                name = "Berlin, U Osloer Str.",
                distance = 456
            )
        )

        val enriched = enrichDeparturesWithStations(departures, stations, city = "Berlin")

        assertEquals("U Osloer Str. (Berlin)", enriched.single().stop)
        assertEquals(441, enriched.single().stationDistance)
    }

    @Test
    fun postMergePassResolvesIdThatWasMissingFromBatchStationList() {
        val batchDepartures = enrichDeparturesWithStations(
            departures = listOf(departure(line = "M13", stop = "de:11000:900011201::6")),
            stations = emptyList(),
            city = "Berlin"
        )
        assertEquals("de:11000:900011201::6", batchDepartures.single().stop)

        val merged = now.abfahrt.transit.data.model.DepartureResponse(
            region = "Berlin/Brandenburg",
            city = "Berlin",
            realtime = true,
            departures = batchDepartures,
            stations = listOf(
                station(
                    id = "de:11000:900011201::6",
                    name = "Louise-Schroeder-Platz (Berlin)",
                    distance = 476
                )
            )
        )

        val resolved = enrichProviderStopIdsAfterResponseMerge(merged)

        assertEquals("Louise-Schroeder-Platz (Berlin)", resolved.departures.single().stop)
        assertEquals("de:11000:900011201::6", resolved.departures.single().providerStopId)
        assertEquals(476, resolved.departures.single().stationDistance)
    }

    @Test
    fun resolvedDisplayNameKeepsProviderStopPointMetadata() {
        val enriched = enrichDeparturesWithStations(
            departures = listOf(departure(stop = "de:11000:900011201::5")),
            stations = listOf(
                station(
                    id = "de:11000:900011201::5",
                    name = "Louise-Schroeder-Platz (Berlin)",
                    distance = 470
                )
            ),
            city = "Berlin"
        )

        assertEquals("Louise-Schroeder-Platz (Berlin)", enriched.single().stop)
        assertEquals("de:11000:900011201::5", enriched.single().providerStopId)
    }

    @Test
    fun resolvesExactNumericProviderStopIdToStationName() {
        val enriched = enrichDeparturesWithStations(
            departures = listOf(departure(line = "U6", stop = "900009173")),
            stations = listOf(
                station(
                    id = "900009173",
                    name = "U Seestr./Turiner Str.",
                    distance = 832
                )
            ),
            city = "Berlin"
        )

        assertEquals("U Seestr./Turiner Str.", enriched.single().stop)
        assertEquals("900009173", enriched.single().providerStopId)
        assertEquals(832, enriched.single().stationDistance)
    }

    @Test
    fun postMergePassResolvesNumericIdThatWasMissingFromBatchStationList() {
        val batchDepartures = enrichDeparturesWithStations(
            departures = listOf(departure(line = "U6", stop = "900009173")),
            stations = emptyList(),
            city = "Berlin"
        )
        assertEquals("900009173", batchDepartures.single().stop)
        assertNull(batchDepartures.single().providerStopId)

        val merged = now.abfahrt.transit.data.model.DepartureResponse(
            region = "Berlin/Brandenburg",
            city = "Berlin",
            realtime = true,
            departures = batchDepartures,
            stations = listOf(
                station(
                    id = "900009173",
                    name = "U Seestr./Turiner Str.",
                    distance = 832
                )
            )
        )

        val resolved = enrichProviderStopIdsAfterResponseMerge(merged)

        assertEquals("U Seestr./Turiner Str.", resolved.departures.single().stop)
        assertEquals("900009173", resolved.departures.single().providerStopId)
        assertEquals(832, resolved.departures.single().stationDistance)
    }

    @Test
    fun numericStopWithoutMatchingStationIdRemainsUntouched() {
        val enriched = enrichDeparturesWithStations(
            departures = listOf(departure(line = "X", stop = "123456789")),
            stations = listOf(
                station(
                    id = "987654321",
                    name = "Example Station",
                    distance = 200
                )
            ),
            city = "Example City"
        )

        assertEquals("123456789", enriched.single().stop)
        assertNull(enriched.single().providerStopId)
        assertEquals(0, enriched.single().stationDistance)
    }

    @Test
    fun preservesNameBasedStationMatching() {
        val departures = listOf(
            departure(
                direction = "Wedding, Virchow-Klinikum",
                stop = "Berlin, Louise-Schroeder-Platz"
            )
        )
        val stations = listOf(
            station(
                id = "de:11000:900011201::1",
                name = "Louise-Schroeder-Platz (Berlin)",
                distance = 424
            )
        )

        val enriched = enrichDeparturesWithStations(departures, stations, city = "Berlin")

        assertEquals("Louise-Schroeder-Platz (Berlin)", enriched.single().stop)
        assertEquals(424, enriched.single().stationDistance)
    }

    private fun departure(
        line: String = "50",
        direction: String = "Berlin, Virchow-Klinikum",
        stop: String
    ): Departure = Departure(
        line = line,
        direction = direction,
        time = "12:00",
        timestamp = 1_783_702_553_000,
        stop = stop
    )

    private fun station(
        id: String,
        name: String,
        distance: Int
    ): Station = Station(
        id = id,
        name = name,
        distance = distance,
        hasRail = false,
        lat = 52.55,
        lon = 13.36
    )
}
