package now.abfahrt.transit.data.api

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Url

interface OpenRouteServiceApi {
    @Headers(
        "Content-Type: application/json; charset=utf-8",
        "Accept: application/json, application/geo+json, application/gpx+xml, img/png; charset=utf-8"
    )
    @POST
    suspend fun getDirections(
        @Url path: String,
        @Header("Authorization") apiKey: String,
        @Body request: OrsDirectionsRequest
    ): JsonObject

    @Headers(
        "Content-Type: application/json; charset=utf-8",
        "Accept: application/json; charset=utf-8"
    )
    @POST
    suspend fun getMatrix(
        @Url path: String,
        @Header("Authorization") apiKey: String,
        @Body request: OrsMatrixRequest
    ): JsonObject
}

data class OrsDirectionsRequest(
    @SerializedName("coordinates")
    val coordinates: List<List<Double>>
)

data class OrsMatrixRequest(
    @SerializedName("locations")
    val locations: List<List<Double>>,
    @SerializedName("sources")
    val sources: List<String>,
    @SerializedName("destinations")
    val destinations: List<String>,
    @SerializedName("metrics")
    val metrics: List<String> = listOf("distance", "duration")
)
