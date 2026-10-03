package now.abfahrt.transit.data.repository

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import now.abfahrt.transit.data.api.OpenRouteServiceApi
import now.abfahrt.transit.data.api.OrsDirectionsRequest
import now.abfahrt.transit.data.api.OrsMatrixRequest
import android.util.Log
import now.abfahrt.transit.data.model.WalkingRouteInfo
import now.abfahrt.transit.data.model.RoutePreviewData
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import java.util.Locale
import retrofit2.HttpException
import kotlin.math.roundToInt
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.firstOrNull
import now.abfahrt.transit.data.preferences.UserPreferencesRepository
import now.abfahrt.transit.data.model.OrsTravelMode

@Singleton
class WalkingRouteRepository @Inject constructor(
    private val api: OpenRouteServiceApi,
    private val prefsRepo: UserPreferencesRepository
) {


    data class MatrixStopRequest(
        val stationId: String?,
        val stationName: String,
        val endLat: Double,
        val endLon: Double,
        val apiDistanceMeters: Int,
        /**
         * Stable caller-side key used to associate ORS matrix cells with app stations.
         * Names are provider-dependent ("Berlin, Brienzer Str." vs. "Brienzer Str. (Berlin)").
         * Coordinates are the ORS source of truth, so the ViewModel passes a coordinate bucket here.
         */
        val lookupKey: String = stationName
    )

    private data class CacheEntry(
        val value: WalkingRouteInfo,
        val cachedAt: Long
    )

    private data class RouteCacheEntry(
        val value: RoutePreviewData,
        val cachedAt: Long
    )

    private val cache = LinkedHashMap<String, CacheEntry>()
    private val routeCache = LinkedHashMap<String, RouteCacheEntry>()
    private val inFlight = mutableMapOf<String, CompletableDeferred<WalkingRouteInfo?>>()
    private val cacheTtlMs = 5 * 60_000L
    @Volatile private var orsCooldownUntilMs: Long = 0L
    @Volatile private var orsCooldownStepMs: Long = 0L

    suspend fun getWalkingRoutesMatrix(
        startLat: Double,
        startLon: Double,
        stops: List<MatrixStopRequest>
    ): Map<String, WalkingRouteInfo> {
        if (stops.isEmpty()) return emptyMap()

        val prefs = prefsRepo.preferencesFlow.firstOrNull()
        val orsApiKey = prefs?.orsApiKey?.trim().orEmpty()
        if (orsApiKey.isBlank()) return emptyMap()
        val travelMode = prefs?.orsTravelMode ?: OrsTravelMode.WALK

        val cachedResults = linkedMapOf<String, WalkingRouteInfo>()
        val uncachedStops = mutableListOf<MatrixStopRequest>()
        val now = System.currentTimeMillis()

        synchronized(cache) {
            stops.forEach { stop ->
                val key = buildKey(startLat, startLon, stop.endLat, stop.endLon, stop.stationId, travelMode)
                val entry = cache[key]
                if (entry != null && now - entry.cachedAt <= cacheTtlMs) {
                    cachedResults[stop.lookupKey] = entry.value
                } else {
                    uncachedStops += stop
                }
            }
        }

        if (uncachedStops.isEmpty()) {
            Log.d("AbfahrtWalk", "💾 matrix cache hit all stops=${stops.size}")
            return cachedResults
        }

        val cooldownDelay = (orsCooldownUntilMs - System.currentTimeMillis()).coerceAtLeast(0L)
        if (cooldownDelay > 0L) {
            Log.w("AbfahrtWalk", "⏳ ORS global cooldown active for ${cooldownDelay}ms before matrix")
            delay(cooldownDelay)
        }

        val matrixPath = when (travelMode) {
            OrsTravelMode.WALK -> "v2/matrix/foot-walking"
            OrsTravelMode.BIKE -> "v2/matrix/cycling-regular"
        }
        val locations = buildList {
            add(listOf(startLon, startLat))
            uncachedStops.forEach { add(listOf(it.endLon, it.endLat)) }
        }
        val request = OrsMatrixRequest(
            locations = locations,
            sources = listOf("0"),
            destinations = uncachedStops.indices.map { (it + 1).toString() }
        )

        Log.d(
            "AbfahrtWalk",
            "🌐 ORS matrix request mode=${travelMode.name} stops=${uncachedStops.size} cached=${cachedResults.size} start=${formatLonLat(startLon, startLat)}"
        )

        val response = try {
            api.getMatrix(matrixPath, orsApiKey, request)
        } catch (e: HttpException) {
            if (e.code() == 429) {
                orsCooldownStepMs = if (orsCooldownStepMs <= 0L) 10_000L else (orsCooldownStepMs * 2).coerceAtMost(60_000L)
                orsCooldownUntilMs = System.currentTimeMillis() + orsCooldownStepMs
                Log.w("AbfahrtWalk", "ORS matrix rate limited, entering global cooldown=${orsCooldownStepMs}ms")
                return cachedResults
            }
            throw e
        }

        val matrixResults = parseMatrix(response, uncachedStops)
        if (matrixResults.isEmpty()) {
            val errorMessage = response.getStringOrNull("error")
            if (!errorMessage.isNullOrBlank()) {
                Log.w("AbfahrtWalk", "ORS matrix returned error: $errorMessage")
            } else {
                Log.w("AbfahrtWalk", "ORS matrix returned unknown response shape")
            }
            return cachedResults
        }

        orsCooldownStepMs = 0L
        orsCooldownUntilMs = 0L
        synchronized(cache) {
            uncachedStops.forEach { stop ->
                val route = matrixResults[stop.lookupKey] ?: return@forEach
                val key = buildKey(startLat, startLon, stop.endLat, stop.endLon, stop.stationId, travelMode)
                cache[key] = CacheEntry(route, now)
            }
            while (cache.size > 256) {
                val oldestKey = cache.entries.firstOrNull()?.key ?: break
                cache.remove(oldestKey)
            }
        }

        Log.d(
            "AbfahrtWalk",
            "✅ ORS matrix parsed stops=${matrixResults.size}/${uncachedStops.size} cached=${cachedResults.size}"
        )
        return linkedMapOf<String, WalkingRouteInfo>().apply {
            putAll(cachedResults)
            putAll(matrixResults)
        }
    }


    suspend fun getRoutePreview(
        startLat: Double,
        startLon: Double,
        endLat: Double,
        endLon: Double,
        stationId: String? = null
    ): RoutePreviewData? {
        val prefs = prefsRepo.preferencesFlow.firstOrNull()
        val orsApiKey = prefs?.orsApiKey?.trim().orEmpty()
        if (orsApiKey.isBlank()) return null
        val travelMode = prefs?.orsTravelMode ?: OrsTravelMode.WALK

        val key = "ROUTE|" + buildKey(startLat, startLon, endLat, endLon, stationId, travelMode)
        val now = System.currentTimeMillis()
        synchronized(routeCache) {
            routeCache[key]?.takeIf { now - it.cachedAt <= cacheTtlMs }?.let {
                Log.d("AbfahrtRoute", "💾 route cache hit key=$key")
                return it.value
            }
        }

        val cooldownDelay = (orsCooldownUntilMs - System.currentTimeMillis()).coerceAtLeast(0L)
        if (cooldownDelay > 0L) {
            Log.w("AbfahrtRoute", "⏳ ORS global cooldown active for ${cooldownDelay}ms before route preview")
            delay(cooldownDelay)
        }

        val geoJsonPath = when (travelMode) {
            OrsTravelMode.WALK -> "v2/directions/foot-walking/geojson"
            OrsTravelMode.BIKE -> "v2/directions/cycling-regular/geojson"
        }

        Log.d("AbfahrtRoute", "🗺️ ORS route preview request mode=${travelMode.name} start=${formatLonLat(startLon, startLat)} end=${formatLonLat(endLon, endLat)}")
        val response = try {
            api.getDirections(
                path = geoJsonPath,
                apiKey = orsApiKey,
                request = OrsDirectionsRequest(
                    coordinates = listOf(
                        listOf(startLon, startLat),
                        listOf(endLon, endLat)
                    )
                )
            )
        } catch (e: HttpException) {
            if (e.code() == 429) {
                orsCooldownStepMs = if (orsCooldownStepMs <= 0L) 10_000L else (orsCooldownStepMs * 2).coerceAtMost(60_000L)
                orsCooldownUntilMs = System.currentTimeMillis() + orsCooldownStepMs
                Log.w("AbfahrtRoute", "ORS route preview rate limited, entering global cooldown=${orsCooldownStepMs}ms")
                return null
            }
            throw e
        }

        val preview = parseRoutePreview(response, startLat, startLon, endLat, endLon) ?: return null
        orsCooldownStepMs = 0L
        orsCooldownUntilMs = 0L
        synchronized(routeCache) {
            routeCache[key] = RouteCacheEntry(preview, now)
            while (routeCache.size > 64) {
                val oldestKey = routeCache.entries.firstOrNull()?.key ?: break
                routeCache.remove(oldestKey)
            }
        }
        Log.d("AbfahrtRoute", "✅ ORS route preview parsed bbox=${preview.bbox?.joinToString()} hasGeoJson=${preview.geoJson.isNotBlank()}")
        return preview
    }

    suspend fun getWalkingRoute(
        startLat: Double,
        startLon: Double,
        endLat: Double,
        endLon: Double,
        stationId: String? = null
    ): WalkingRouteInfo? {
        val prefs = prefsRepo.preferencesFlow.firstOrNull()
        val orsApiKey = prefs?.orsApiKey?.trim().orEmpty()
        if (orsApiKey.isBlank()) return null
        val travelMode = prefs?.orsTravelMode ?: OrsTravelMode.WALK

        val key = buildKey(startLat, startLon, endLat, endLon, stationId, travelMode)
        val now = System.currentTimeMillis()
        synchronized(cache) {
            cache[key]?.takeIf { now - it.cachedAt <= cacheTtlMs }?.let {
                Log.d("AbfahrtWalk", "💾 cache hit start=${formatLonLat(startLon, startLat)} end=${formatLonLat(endLon, endLat)} distance=${it.value.distanceMeters} duration=${it.value.durationSeconds}")
                return it.value
            }
        }

        val (deferred, shouldExecute) = synchronized(inFlight) {
            val existing = inFlight[key]
            if (existing != null) {
                Log.d("AbfahrtWalk", "🌀 in-flight reuse key=$key")
                existing to false
            } else {
                CompletableDeferred<WalkingRouteInfo?>().also { inFlight[key] = it } to true
            }
        }
        if (!shouldExecute) return deferred.await()

        try {
            val start = formatLonLat(startLon, startLat)
            val end = formatLonLat(endLon, endLat)
            Log.d("AbfahrtWalk", "🌍 ORS request mode=${travelMode.name} start=$start end=$end")

            suspend fun executeRequest() = api.getDirections(
                path = travelMode.apiPath,
                apiKey = orsApiKey,
                request = OrsDirectionsRequest(
                    coordinates = listOf(
                        listOf(startLon, startLat),
                        listOf(endLon, endLat)
                    )
                )
            )

            val cooldownDelay = (orsCooldownUntilMs - System.currentTimeMillis()).coerceAtLeast(0L)
            if (cooldownDelay > 0L) {
                Log.w("AbfahrtWalk", "⏳ ORS global cooldown active for ${cooldownDelay}ms")
                delay(cooldownDelay)
            }

            val response = try {
                executeRequest()
            } catch (e: HttpException) {
                if (e.code() == 429) {
                    orsCooldownStepMs = if (orsCooldownStepMs <= 0L) 10_000L else (orsCooldownStepMs * 2).coerceAtMost(60_000L)
                    orsCooldownUntilMs = System.currentTimeMillis() + orsCooldownStepMs
                    Log.w("AbfahrtWalk", "ORS rate limited, entering global cooldown=${orsCooldownStepMs}ms for key=$key")
                    delay(orsCooldownStepMs)
                    executeRequest()
                } else {
                    throw e
                }
            }

            val route = parseRoute(response) ?: run {
                val errorMessage = response.getStringOrNull("error")
                if (!errorMessage.isNullOrBlank()) {
                    Log.w("AbfahrtWalk", "ORS returned error: $errorMessage")
                } else {
                    Log.w("AbfahrtWalk", "ORS returned unknown response shape")
                }
                deferred.complete(null)
                return null
            }
            Log.d("AbfahrtWalk", "✅ ORS parsed distance=${route.distanceMeters} duration=${route.durationSeconds}")
            orsCooldownStepMs = 0L
            orsCooldownUntilMs = 0L
            synchronized(cache) {
                cache[key] = CacheEntry(route, now)
                if (cache.size > 256) {
                    val oldestKey = cache.entries.firstOrNull()?.key
                    if (oldestKey != null) cache.remove(oldestKey)
                }
            }
            deferred.complete(route)
            return route
        } catch (e: Exception) {
            deferred.complete(null)
            throw e
        } finally {
            synchronized(inFlight) { inFlight.remove(key) }
        }
    }

    private fun buildKey(startLat: Double, startLon: Double, endLat: Double, endLon: Double, stationId: String?, travelMode: OrsTravelMode): String {
        val startLatBucket = bucket(startLat)
        val startLonBucket = bucket(startLon)
        return if (!stationId.isNullOrBlank()) {
            "${travelMode.name}|${stationId.trim()}|$startLatBucket|$startLonBucket"
        } else {
            listOf(travelMode.name, startLatBucket, startLonBucket, bucket(endLat), bucket(endLon))
                .joinToString("|")
        }
    }

    private fun bucket(value: Double): String =
        String.format(Locale.US, "%.3f", ((value * 1000.0).roundToInt() / 1000.0))

    private fun formatLonLat(lon: Double, lat: Double): String =
        String.format(Locale.US, "%.6f,%.6f", lon, lat)

    private fun parseMatrix(
        response: JsonObject,
        stops: List<MatrixStopRequest>
    ): Map<String, WalkingRouteInfo> {
        val distances = response.getAsJsonArrayOrNull("distances")
            ?.getOrNull(0)
            ?.asJsonArrayOrNull()
        val durations = response.getAsJsonArrayOrNull("durations")
            ?.getOrNull(0)
            ?.asJsonArrayOrNull()
            ?: return emptyMap()
        if (distances == null) return emptyMap()

        return buildMap {
            stops.forEachIndexed { index, stop ->
                val distance = distances.getOrNull(index)?.asDoubleOrNull()
                val duration = durations.getOrNull(index)?.asDoubleOrNull()
                if (distance != null && duration != null) {
                    put(
                        stop.lookupKey,
                        WalkingRouteInfo(
                            distanceMeters = distance.toInt(),
                            durationSeconds = duration.toInt()
                        )
                    )
                }
            }
        }
    }



    private fun parseRoutePreview(response: JsonObject, startLat: Double, startLon: Double, endLat: Double, endLon: Double): RoutePreviewData? {
        val features = response.getAsJsonArrayOrNull("features")
        if (features == null || features.size() == 0) return null
        val bbox = response.getAsJsonArrayOrNull("bbox")
            ?.let { array ->
                buildList {
                    for (index in 0 until array.size()) {
                        array.getOrNull(index)?.asDoubleOrNull()?.let(::add)
                    }
                }.takeIf { it.size == 4 }
            }
        val routeInfo = parseRoute(response)
        return RoutePreviewData(
            geoJson = response.toString(),
            bbox = bbox,
            startLat = startLat,
            startLon = startLon,
            endLat = endLat,
            endLon = endLon,
            routeDistanceMeters = routeInfo?.distanceMeters,
            routeDurationSeconds = routeInfo?.durationSeconds
        )
    }

    private fun parseRoute(response: JsonObject): WalkingRouteInfo? {
        val featureSummary = response.getAsJsonArrayOrNull("features")
            ?.takeIf { it.size() > 0 }
            ?.getOrNull(0)
            ?.asJsonObjectOrNull()
            ?.getAsJsonObjectOrNull("properties")
            ?.getAsJsonObjectOrNull("summary")

        val routeSummary = response.getAsJsonArrayOrNull("routes")
            ?.takeIf { it.size() > 0 }
            ?.getOrNull(0)
            ?.asJsonObjectOrNull()
            ?.getAsJsonObjectOrNull("summary")

        val summary = featureSummary ?: routeSummary ?: return null
        val distance = summary.getDoubleOrNull("distance") ?: return null
        val duration = summary.getDoubleOrNull("duration") ?: return null
        return WalkingRouteInfo(
            distanceMeters = distance.toInt(),
            durationSeconds = duration.toInt()
        )
    }
}

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

private fun JsonObject?.getDoubleOrNull(name: String): Double? =
    try {
        this?.get(name)?.asDouble
    } catch (_: Exception) {
        null
    }

private fun JsonArray.getOrNull(index: Int) =
    if (index in 0 until size()) get(index) else null

private fun com.google.gson.JsonElement?.asJsonObjectOrNull(): JsonObject? =
    try {
        this?.asJsonObject
    } catch (_: Exception) {
        null
    }

private fun JsonObject?.getStringOrNull(name: String): String? =
    try {
        this?.get(name)?.takeIf { !it.isJsonNull }?.asString
    } catch (_: Exception) {
        null
    }

private fun com.google.gson.JsonElement?.asJsonArrayOrNull(): JsonArray? =
    try {
        this?.asJsonArray
    } catch (_: Exception) {
        null
    }

private fun com.google.gson.JsonElement?.asDoubleOrNull(): Double? =
    try {
        if (this == null || this.isJsonNull) null else this.asDouble
    } catch (_: Exception) {
        null
    }
