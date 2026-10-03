package now.abfahrt.transit.data.api

import com.google.gson.Gson
import now.abfahrt.transit.data.model.DepartureResponse
import now.abfahrt.transit.data.model.TripResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AbfahrtApiContractTest {
    private val gson = Gson()

    @Test
    fun parsesCurrentDepartureAndStationContract() {
        val response = gson.fromJson(
            """
            {
              "region": "Berlin/Brandenburg",
              "city": "Berlin",
              "realtime": true,
              "routing": true,
              "departures": [
                {
                  "line": "U2",
                  "direction": "Pankow",
                  "time": "3 min",
                  "timestamp": 1789297200000,
                  "stop": "S+U Alexanderplatz",
                  "platform": "2",
                  "delay": 2,
                  "cancelled": false,
                  "occupancy": "low",
                  "mode": "subway"
                }
              ],
              "stations": [
                {
                  "id": "900100003",
                  "name": "S+U Alexanderplatz",
                  "distance": 120,
                  "walkSeconds": 480,
                  "hasRail": true,
                  "lat": 52.521,
                  "lon": 13.413
                }
              ],
              "attribution": "contract fixture"
            }
            """.trimIndent(),
            DepartureResponse::class.java
        )

        assertEquals("Berlin/Brandenburg", response.region)
        assertEquals("Berlin", response.city)
        assertTrue(response.realtime)
        assertTrue(response.routing)

        val departure = response.departures.single()
        assertEquals("U2", departure.line)
        assertEquals("Pankow", departure.direction)
        assertEquals(1_789_297_200_000L, departure.timestamp)
        assertEquals("S+U Alexanderplatz", departure.stop)
        assertEquals("2", departure.platform)
        assertEquals(2, departure.delay)
        assertEquals("low", departure.occupancy)
        assertEquals("subway", departure.mode)

        val station = response.stations.orEmpty().single()
        assertEquals("900100003", station.id)
        assertEquals("S+U Alexanderplatz", station.name)
        assertEquals(120, station.distance)
        assertEquals(480, station.walkSeconds)
        assertTrue(station.hasRail)
        assertEquals(52.521, station.lat!!, 0.000001)
        assertEquals(13.413, station.lon!!, 0.000001)
    }

    @Test
    fun keepsWalkSecondsAndOtherOptionalFieldsOptional() {
        val response = gson.fromJson(
            """
            {
              "region": "Example",
              "realtime": false,
              "routing": false,
              "departures": [
                {
                  "line": "10",
                  "direction": "Central",
                  "time": "12 min",
                  "timestamp": 1789297500000,
                  "stop": "Market Square",
                  "delay": 0,
                  "cancelled": false
                }
              ],
              "stations": [
                {
                  "id": "stop-10",
                  "name": "Market Square",
                  "distance": 240,
                  "hasRail": false
                }
              ]
            }
            """.trimIndent(),
            DepartureResponse::class.java
        )

        val departure = response.departures.single()
        assertNull(departure.platform)
        assertNull(departure.occupancy)
        assertNull(departure.mode)

        val station = response.stations.orEmpty().single()
        assertEquals(240, station.distance)
        assertNull(station.walkSeconds)
        assertNull(station.lat)
        assertNull(station.lon)
    }

    @Test
    fun ignoresForwardCompatibleUnknownFields() {
        val response = gson.fromJson(
            """
            {
              "region": "Example",
              "city": "Example City",
              "realtime": true,
              "routing": true,
              "departures": [],
              "stations": [],
              "regionBounds": {"north": 1, "south": 0, "east": 1, "west": 0},
              "routingBounds": [{"north": 1, "south": 0, "east": 1, "west": 0}],
              "futureField": "ignored"
            }
            """.trimIndent(),
            DepartureResponse::class.java
        )

        assertEquals("Example", response.region)
        assertEquals("Example City", response.city)
        assertTrue(response.departures.isEmpty())
        assertTrue(response.stations.orEmpty().isEmpty())
    }
    @Test
    fun parsesTripContractIncludingThroughRunningAndIntermediateStops() {
        val response = gson.fromJson(
            """
            {
              "region": "Berlin/Brandenburg",
              "trips": [
                {
                  "departure": 1789297200000,
                  "arrival": 1789299000000,
                  "duration": 30,
                  "changes": 0,
                  "legs": [
                    {
                      "line": "U3",
                      "direction": "Warschauer Str.",
                      "mode": "subway",
                      "from": "Wittenbergplatz",
                      "to": "Nollendorfplatz",
                      "departure": 1789297200000,
                      "arrival": 1789297440000,
                      "departureDelay": 1,
                      "arrivalDelay": 1,
                      "departurePlatform": "2",
                      "cancelled": false,
                      "stops": 1,
                      "stopNames": ["Viktoria-Luise-Platz"],
                      "intermediateStops": [
                        {"name": "Viktoria-Luise-Platz", "arrival": 1789297320000}
                      ],
                      "sameVehicle": false
                    },
                    {
                      "line": "U1",
                      "direction": "Warschauer Str.",
                      "mode": "subway",
                      "from": "Nollendorfplatz",
                      "to": "Warschauer Str.",
                      "departure": 1789297440000,
                      "arrival": 1789299000000,
                      "departureDelay": 1,
                      "arrivalDelay": 1,
                      "cancelled": false,
                      "stops": 8,
                      "sameVehicle": true
                    }
                  ]
                }
              ],
              "attribution": "contract fixture"
            }
            """.trimIndent(),
            TripResponse::class.java
        )

        val trip = response.trips.single()
        assertEquals(30, trip.duration)
        assertEquals(0, trip.changes)
        assertEquals(2, trip.legs.size)
        assertEquals("Viktoria-Luise-Platz", trip.legs.first().stopNames.orEmpty().single())
        assertEquals(1_789_297_320_000L, trip.legs.first().intermediateStops.orEmpty().single().arrival)
        assertTrue(trip.legs.last().sameVehicle)
    }

    @Test
    fun keepsOptionalTripFieldsBackwardCompatible() {
        val response = gson.fromJson(
            """
            {
              "region": "Example",
              "trips": [
                {
                  "departure": 1789297200000,
                  "arrival": 1789297800000,
                  "duration": 10,
                  "changes": 0,
                  "legs": [
                    {
                      "line": "10",
                      "direction": "Central",
                      "from": "A",
                      "to": "B",
                      "departure": 1789297200000,
                      "arrival": 1789297800000,
                      "departureDelay": 0,
                      "arrivalDelay": 0,
                      "cancelled": false,
                      "stops": 0
                    }
                  ]
                }
              ]
            }
            """.trimIndent(),
            TripResponse::class.java
        )

        val leg = response.trips.single().legs.single()
        assertTrue(leg.stopNames.orEmpty().isEmpty())
        assertTrue(leg.intermediateStops.orEmpty().isEmpty())
        assertTrue(!leg.sameVehicle)
        assertNull(leg.departurePlatform)
    }

}
