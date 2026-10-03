package now.abfahrt.transit.data.api

import com.google.gson.JsonObject
import retrofit2.http.GET
import retrofit2.http.Query

interface PhotonApiService {
    @GET("api")
    suspend fun searchPlaces(
        @Query("q") query: String,
        @Query("limit") limit: Int = 8,
        @Query("lang") language: String? = null,
        @Query("lat") latitude: Double? = null,
        @Query("lon") longitude: Double? = null
    ): JsonObject
}
