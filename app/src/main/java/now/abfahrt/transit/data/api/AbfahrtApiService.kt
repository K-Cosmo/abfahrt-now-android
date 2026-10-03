package now.abfahrt.transit.data.api

import now.abfahrt.transit.data.model.DepartureResponse
import now.abfahrt.transit.data.model.TripResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface AbfahrtApiService {

    /**
     * Returns real-time departures near [lat]/[lon].
     *
     * New parameters (API v1.1):
     * - [limit]  max departures returned (default 15, max 60)
     * - [mode]   comma-separated modes to include, or prefix "-" to exclude
     *            e.g. "subway,tram" or "-bus,ferry"
     */
    @GET("departures")
    suspend fun getDepartures(
        @Query("lat")    lat:    Double,
        @Query("lon")    lon:    Double,
        @Query("radius") radius: Int    = 800,
        @Query("limit")  limit:  Int?   = null,
        @Query("mode")   mode:   String? = null,
        @Query("from")   fromMinutes: Int? = null,
        @Query("to")     toMinutes: Int? = null,
        @Query("dedup")  dedup: String? = null,
        @Query("stops")  stops: String? = null
    ): DepartureResponse

    @GET("trips")
    suspend fun getTrips(
        @Query("from_lat") fromLat: Double,
        @Query("from_lon") fromLon: Double,
        @Query("to_lat")   toLat:   Double,
        @Query("to_lon")   toLon:   Double
    ): TripResponse
}
