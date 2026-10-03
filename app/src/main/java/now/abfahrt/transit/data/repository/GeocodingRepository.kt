package now.abfahrt.transit.data.repository

import android.util.Log
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import now.abfahrt.transit.data.api.PhotonApiService
import now.abfahrt.transit.data.model.SearchResult
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeocodingRepository @Inject constructor(
    private val photonApi: PhotonApiService
) {
    suspend fun searchPlaces(
        query: String,
        biasLat: Double? = null,
        biasLon: Double? = null
    ): List<SearchResult> {
        val photonQuery = placeQueryForPhoton(query)
        if (photonQuery.length < 3) return emptyList()

        val lang = Locale.getDefault().language.takeIf { it.isNotBlank() }
        val response = photonApi.searchPlaces(
            query = photonQuery,
            limit = 16,
            language = lang,
            latitude = biasLat,
            longitude = biasLon
        )
        val features = response.getAsJsonArrayOrNull("features") ?: return emptyList()

        Log.d(
            "PhotonPlaceRank",
            "query='$photonQuery' biasLat=$biasLat biasLon=$biasLon rawResults=${features.size()} policy=raw_query_location_bias"
        )

        return features.mapIndexedNotNull { index, featureElement ->
            val feature = featureElement.asJsonObjectOrNull() ?: return@mapIndexedNotNull null
            val geometry = feature.getAsJsonObjectOrNull("geometry") ?: return@mapIndexedNotNull null
            val coordinates = geometry.getAsJsonArrayOrNull("coordinates") ?: return@mapIndexedNotNull null
            if (coordinates.size() < 2) return@mapIndexedNotNull null

            val lon = coordinates.getOrNull(0)?.asDoubleOrNull() ?: return@mapIndexedNotNull null
            val lat = coordinates.getOrNull(1)?.asDoubleOrNull() ?: return@mapIndexedNotNull null
            val properties = feature.getAsJsonObjectOrNull("properties")

            val name = properties.getStringOrNull("name")?.cleanPart()
            val street = properties.getStringOrNull("street")?.cleanPart()
            val houseNumber = properties.getStringOrNull("housenumber")?.cleanPart()
            val postcode = properties.getStringOrNull("postcode")?.cleanPart()
            val city = properties.getStringOrNull("city")?.cleanPart()
            val district = properties.getStringOrNull("district")?.cleanPart()
            val state = properties.getStringOrNull("state")?.cleanPart()
            val country = properties.getStringOrNull("country")?.cleanPart()
            val osmKey = properties.getStringOrNull("osm_key")?.cleanPart()
            val osmValue = properties.getStringOrNull("osm_value")?.cleanPart()

            if (index < 5) {
                Log.d(
                    "PhotonPlaceRank",
                    "rank=${index + 1} query='$photonQuery' name='${name.orEmpty()}' city='${city.orEmpty()}' " +
                        "state='${state.orEmpty()}' type='${listOfNotNull(osmKey, osmValue).joinToString(":")}'"
                )
            }

            val streetAddress = listOfNotNull(street, houseNumber)
                .filter { it.isNotBlank() }
                .joinToString(" ")
                .takeIf { it.isNotBlank() }

            val title = when {
                !name.isNullOrBlank() && !houseNumber.isNullOrBlank() && name.equals(street, ignoreCase = true) ->
                    "$name $houseNumber"
                !name.isNullOrBlank() -> name
                !streetAddress.isNullOrBlank() -> streetAddress
                else -> return@mapIndexedNotNull null
            }

            val locality = when {
                !postcode.isNullOrBlank() && !city.isNullOrBlank() -> "$postcode $city"
                !city.isNullOrBlank() -> city
                !district.isNullOrBlank() -> district
                !state.isNullOrBlank() -> state
                else -> country
            }
            val subtitleParts = buildList {
                if (!streetAddress.isNullOrBlank() && !streetAddress.equals(title, ignoreCase = true)) add(streetAddress)
                if (!locality.isNullOrBlank() && !locality.equals(title, ignoreCase = true)) add(locality)
                if (!country.isNullOrBlank() && !country.equals(locality, ignoreCase = true)) add(country)
            }.distinctBy { it.lowercase(Locale.ROOT) }
            val subtitle = subtitleParts.joinToString(", ")

            SearchResult(
                id = "$title|$subtitle|${lat.roundForId()}|${lon.roundForId()}",
                title = title,
                subtitle = subtitle,
                typeLabel = "",
                lat = lat,
                lon = lon
            )
        }
            // Photon already ranks by textual relevance, importance and the supplied lat/lon bias.
            // Keep that order; de-duplicate only exact display duplicates without re-ranking.
            .distinctBy { result ->
                listOf(result.title, result.subtitle)
                    .joinToString("|")
                    .lowercase(Locale.ROOT)
            }
            .take(8)
    }

    suspend fun searchStations(query: String, city: String? = null): List<SearchResult> {
        val lang = Locale.getDefault().language.takeIf { it.isNotBlank() }
        val scopedQuery = city
            ?.takeIf { it.isNotBlank() }
            ?.let { "$query, $it" }
            ?: query
        val normalizedQuery = query.normalizeForSearch()

        val response = photonApi.searchPlaces(
            query = scopedQuery,
            limit = 16,
            language = lang
        )

        val features = response.getAsJsonArrayOrNull("features") ?: return emptyList()

        val rankedResults = features.mapNotNull { featureElement ->
            val feature = featureElement.asJsonObjectOrNull() ?: return@mapNotNull null
            val geometry = feature.getAsJsonObjectOrNull("geometry") ?: return@mapNotNull null
            val coordinates = geometry.getAsJsonArrayOrNull("coordinates") ?: return@mapNotNull null
            if (coordinates.size() < 2) return@mapNotNull null

            val lon = coordinates.getOrNull(0)?.asDoubleOrNull() ?: return@mapNotNull null
            val lat = coordinates.getOrNull(1)?.asDoubleOrNull() ?: return@mapNotNull null

            val properties = feature.getAsJsonObjectOrNull("properties")
            val title = properties.getStringOrNull("name")?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            val cityName = properties.getStringOrNull("city")?.cleanPart()
            val district = properties.getStringOrNull("district")?.cleanPart()
            val state = properties.getStringOrNull("state")?.cleanPart()
            val country = properties.getStringOrNull("country")?.cleanPart()
            val osmKey = properties.getStringOrNull("osm_key")?.cleanPart()
            val osmValue = properties.getStringOrNull("osm_value")?.cleanPart()
            val typeLabel = inferTypeLabel(title = title, osmKey = osmKey, osmValue = osmValue)
            val subtitle = buildSubtitle(district = district, city = cityName, state = state, country = country)
            val matchQuality = scoreMatch(
                title = title,
                subtitle = subtitle,
                typeLabel = typeLabel,
                query = normalizedQuery
            )

            SearchResult(
                id = "$title|$subtitle|$typeLabel|${lat.roundForId()}|${lon.roundForId()}",
                title = title,
                subtitle = subtitle,
                typeLabel = typeLabel,
                lat = lat,
                lon = lon,
                matchQuality = matchQuality
            )
        }
            .sortedWith(
                compareByDescending<SearchResult> { it.matchQuality }
                    .thenBy { it.title.lowercase(Locale.ROOT) }
                    .thenBy { it.subtitle.lowercase(Locale.ROOT) }
            )

        val uniqueResults = rankedResults.fold(mutableListOf<SearchResult>()) { acc, result ->
            val duplicateIndex = acc.indexOfFirst { existing ->
                existing.title.equals(result.title, ignoreCase = true) &&
                    existing.subtitle.equals(result.subtitle, ignoreCase = true) &&
                    existing.typeLabel.equals(result.typeLabel, ignoreCase = true)
            }
            if (duplicateIndex >= 0) {
                if (result.matchQuality > acc[duplicateIndex].matchQuality) {
                    acc[duplicateIndex] = result
                }
            } else {
                acc += result
            }
            acc
        }

        val primaryTransit = uniqueResults.filter { it.isPrimaryTransitResult() }
        val secondaryTransit = uniqueResults.filter { it.isSecondaryTransitResult() }
        val fallback = uniqueResults.filterNot { it.isPrimaryTransitResult() || it.isSecondaryTransitResult() }

        return (primaryTransit + secondaryTransit + fallback)
            .take(6)
    }

    private fun buildSubtitle(
        district: String?,
        city: String?,
        state: String?,
        country: String?
    ): String {
        val cityPart = when {
            !district.isNullOrBlank() && !city.isNullOrBlank() && !district.equals(city, ignoreCase = true) -> "$district, $city"
            !city.isNullOrBlank() -> city
            !district.isNullOrBlank() -> district
            !state.isNullOrBlank() -> state
            else -> country.orEmpty()
        }
        return cityPart
    }

    private fun inferTypeLabel(title: String, osmKey: String?, osmValue: String?): String {
        val normalizedTitle = title.normalizeForSearch()
        val key = osmKey?.lowercase(Locale.ROOT).orEmpty()
        val value = osmValue?.lowercase(Locale.ROOT).orEmpty()
        return when {
            key == "railway" && (value == "station" || value == "halt") -> "Bahnhof"
            key == "public_transport" && value == "station" -> "Station"
            key == "public_transport" && value == "stop_position" -> "Haltestelle"
            key == "public_transport" && value == "platform" -> "Bahnsteig"
            key == "highway" && value == "bus_stop" -> "Bushaltestelle"
            key == "amenity" && value == "bus_station" -> "Busbahnhof"
            normalizedTitle.startsWith("u ") || normalizedTitle.startsWith("u-") -> "U-Bahn"
            normalizedTitle.startsWith("s ") || normalizedTitle.startsWith("s-") -> "S-Bahn"
            "tram" in normalizedTitle -> "Tram"
            "bahnhof" in normalizedTitle || " hbf" in normalizedTitle || normalizedTitle.endsWith("bf") -> "Bahnhof"
            else -> "Ort"
        }
    }

    private fun scoreMatch(
        title: String,
        subtitle: String,
        typeLabel: String,
        query: String
    ): Int {
        val titleNorm = title.normalizeForSearch()
        val subtitleNorm = subtitle.normalizeForSearch()
        val typeNorm = typeLabel.normalizeForSearch()
        var score = 0
        if (titleNorm == query) score += 120
        if (titleNorm.startsWith(query)) score += 80
        if (titleNorm.contains(query)) score += 40
        if (subtitleNorm.contains(query)) score += 15
        if (typeNorm.contains(query)) score += 10
        if (titleNorm.startsWith("u ") || titleNorm.startsWith("s ") || titleNorm.startsWith("s+u ")) score += 12
        score += when {
            typeLabel.isPrimaryTransitType() -> 40
            typeLabel.isSecondaryTransitType() -> 18
            else -> -20
        }
        if (typeNorm.contains("bahnhof") || typeNorm.contains("station") || typeNorm.contains("haltestelle")) score += 6
        return score
    }
}


internal fun placeQueryForPhoton(query: String): String = query.trim()

private fun SearchResult.isPrimaryTransitResult(): Boolean = typeLabel.isPrimaryTransitType()

private fun SearchResult.isSecondaryTransitResult(): Boolean = typeLabel.isSecondaryTransitType()

private fun String.isPrimaryTransitType(): Boolean = this in setOf(
    "Bahnhof",
    "Station",
    "U-Bahn",
    "S-Bahn",
    "Busbahnhof"
)

private fun String.isSecondaryTransitType(): Boolean = this in setOf(
    "Haltestelle",
    "Bushaltestelle",
    "Bahnsteig",
    "Tram"
)

private fun JsonObject?.getAsJsonObjectOrNull(name: String): JsonObject? =
    try {
        this?.get(name)?.asJsonObject
    } catch (_: Exception) {
        null
    }

private fun JsonObject?.getAsJsonArrayOrNull(name: String): JsonArray? =
    try {
        this?.get(name)?.asJsonArray
    } catch (_: Exception) {
        null
    }

private fun JsonObject?.getStringOrNull(name: String): String? {
    val element = try {
        this?.get(name)
    } catch (_: Exception) {
        null
    } ?: return null

    if (element.isJsonNull) return null
    return try {
        element.asString
    } catch (_: Exception) {
        element.toString().trim('"')
    }
}

private fun JsonArray.getOrNull(index: Int): JsonElement? =
    if (index in 0 until size()) get(index) else null

private fun JsonElement?.asJsonObjectOrNull(): JsonObject? =
    try {
        this?.asJsonObject
    } catch (_: Exception) {
        null
    }

private fun JsonElement?.asDoubleOrNull(): Double? =
    try {
        this?.asDouble
    } catch (_: Exception) {
        null
    }

private fun String.cleanPart(): String = trim().trim(',')

private fun String.normalizeForSearch(): String =
    lowercase(Locale.ROOT)
        .replace("straße", "strasse")
        .replace("ß", "ss")
        .replace("ä", "ae")
        .replace("ö", "oe")
        .replace("ü", "ue")
        .replace(Regex("\\s+"), " ")
        .trim()

private fun Double.roundForId(): String = String.format(Locale.US, "%.4f", this)
