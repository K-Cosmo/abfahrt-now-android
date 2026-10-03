package now.abfahrt.transit.data.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET

/** Public release metadata only; this API must never receive app credentials. */
interface GitHubReleaseApi {
    @GET("repos/K-Cosmo/abfahrt-now-android/releases/latest")
    suspend fun latestRelease(): GitHubReleaseDto
}

data class GitHubReleaseDto(
    @SerializedName("tag_name") val tagName: String
)
