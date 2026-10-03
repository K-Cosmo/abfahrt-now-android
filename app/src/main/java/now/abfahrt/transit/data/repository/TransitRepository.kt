package now.abfahrt.transit.data.repository

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import now.abfahrt.transit.data.api.AbfahrtApiService
import now.abfahrt.transit.data.model.Departure
import now.abfahrt.transit.data.model.DepartureResponse
import now.abfahrt.transit.data.model.TransportMode
import now.abfahrt.transit.data.model.TripResponse
import now.abfahrt.transit.data.model.Station
import javax.inject.Inject
import javax.inject.Singleton
import now.abfahrt.transit.util.StationNameNormalizer

@Singleton
class TransitRepository @Inject constructor(
    private val api: AbfahrtApiService
) {
    private val STATION_RADIUS = 80
    private val INITIAL_LIMIT  = 60
    private val DIRECT_STOPS_LIMIT = 60
    private val DIRECT_STOPS_CHUNK_SIZE = 20
    private val MAX_COVERAGE_STATIONS = 60
    private val STATION_SEARCH_FALLBACK_RADII = listOf(150, 200)
    private val DEDUP_OFF = "off"

    /**
     * Build the mode query string.
     * If all modes selected → null (no param).
     * If fewer exclusions than inclusions → "-bus,ferry" style.
     * Otherwise → "subway,tram" style.
     */

    /**
     * Smart progressive loading:
     *
     * 1. Initial call with limit=30 + mode filter → emit immediately
     * 2. Check which nearby stations are MISSING from the response
     *    (station in the stations list but no departure from it)
     * 3. Only fire per-stop calls for the missing stations
     *    → if the first call is already complete, ZERO extra calls
     * 4. Emit final merged result only if something was added
     *
     * Typical results:
     *  - All modes selected, dense area → 1 call (initial covers everything)
     *  - One mode filtered, sparse → 1–3 extra calls for underserved stops
     *  - Worst case (many modes, many stops) → still only missing stops queried
     */
    fun getDeparturesProgressive(
        lat:           Double,
        lon:           Double,
        radius:        Int,
        selectedModes: Set<TransportMode> = TransportMode.all,
        fromMinutes:   Int? = null,
        toMinutes:     Int? = null,
        isStationSearch: Boolean = false,
        useLayeredLoading: Boolean = true,
        initialDedupBooster: Boolean = false,
        broadStopCoverage: Boolean = false
    ): Flow<DepartureResponse> = channelFlow {

        val modeParam = buildModeParamForRequest(selectedModes)

        fun visibleMetrics(response: DepartureResponse): Pair<Set<String>, Int> {
            val visibleStops = response.departures.map { it.stop }.toSet()
            return visibleStops to response.departures.size
        }

        // ── Step 1: initial call (with optional adaptive fallback for station search) ──
        // The API-side dedup response is used only as a cold-start booster. For normal
        // refreshes the screen already has departures, so we request dedup=off from the
        // beginning and let the app's own dedup/filter pipeline keep the list stable.
        val initialDedup = if (initialDedupBooster) null else DEDUP_OFF
        var effectiveRadius = radius
        var initial = fetchDepartures(lat, lon, effectiveRadius, INITIAL_LIMIT, modeParam, fromMinutes, toMinutes, initialDedup)
        var enriched = enrich(initial.departures, initial.stations.orEmpty(), initial.city)

        if (isStationSearch && enriched.isNotEmpty() && enriched.none { it.stationDistance <= effectiveRadius }) {
            Log.d("AbfahrtRepo", "🧭 stationSearch=true initialRadius=$radius fallbackTriggered=true fallbackLevel=0 raw=${enriched.size} radiusOk=0")
            for ((index, fallbackRadius) in STATION_SEARCH_FALLBACK_RADII.withIndex()) {
                Log.d("AbfahrtRepo", "🧭 stationSearch fallbackLevel=${index + 1} tryingRadius=$fallbackRadius")
                val fallbackResponse = fetchDepartures(lat, lon, fallbackRadius, INITIAL_LIMIT, modeParam, fromMinutes, toMinutes, initialDedup)
                val fallbackEnriched = enrich(fallbackResponse.departures, fallbackResponse.stations.orEmpty(), fallbackResponse.city)
                initial = fallbackResponse
                enriched = fallbackEnriched
                effectiveRadius = fallbackRadius
                val radiusOkCount = fallbackEnriched.count { it.stationDistance <= fallbackRadius }
                Log.d("AbfahrtRepo", "🧭 stationSearch fallbackLevel=${index + 1} fallbackRadius=$fallbackRadius raw=${fallbackEnriched.size} radiusOk=$radiusOkCount")
                if (radiusOkCount > 0) break
            }
        }

        val initialStations = initial.stations.orEmpty()
        logProviderWalkSecondsCoverage(initialStations)

        // ── Step 2: find stops with NO departures in the initial response ─────
        val stopsWithDepartures = enriched.map { it.stop }.toSet()
        val coveredStationsAfterInitial = initialStations.count { it.name in stopsWithDepartures }
        val missingStations = initialStations
            .sortedBy { it.distance }
            .filter { station ->
                station.id.isNotBlank() &&
                station.lat != null &&
                station.lon != null &&
                station.name !in stopsWithDepartures
            }

        Log.d(
            "AbfahrtRepo",
            "📊 initialStations=${initialStations.size} coveredAfterInitial=$coveredStationsAfterInitial missingStations=${missingStations.size} initialDepartures=${enriched.size}"
        )

        if (initialDedupBooster && missingStations.isNotEmpty() && enriched.isNotEmpty()) {
            Log.d(
                "AbfahrtRepo",
                "⚡ emitting cold-start dedup booster before stops add-ons departures=${enriched.size} missingStations=${missingStations.size}"
            )
            send(initial.copy(departures = enriched, effectiveRadius = effectiveRadius, isFinal = false))
        } else if (!initialDedupBooster) {
            Log.d("AbfahrtRepo", "🧘 initial dedup booster disabled for stable refresh; waiting for final app-deduped result")
        }

        if (missingStations.isEmpty() && !(broadStopCoverage && !isStationSearch)) {
            Log.d("AbfahrtRepo", "✅ Initial call complete — no extra calls needed")
            send(initial.copy(departures = enriched, effectiveRadius = effectiveRadius, isFinal = true))
            return@channelFlow
        }

        val coverageStations = if (broadStopCoverage && !isStationSearch) {
            initialStations
                .asSequence()
                .filter { station ->
                    station.id.isNotBlank() &&
                        station.lat != null &&
                        station.lon != null &&
                        station.distance <= effectiveRadius
                }
                .sortedBy { it.distance }
                .distinctBy { it.id }
                .take(MAX_COVERAGE_STATIONS)
                .toList()
        } else {
            emptyList()
        }

        val eligibleMissingStations = (missingStations + coverageStations)
            .distinctBy { it.id }
            .sortedBy { it.distance }

        if (eligibleMissingStations.isEmpty()) {
            Log.d("AbfahrtRepo", "✅ No eligible stops left after preselection")
            return@channelFlow
        }

        if (broadStopCoverage && !isStationSearch) {
            Log.d(
                "AbfahrtRepo",
                "🔄 coverage stops fetch stations=${eligibleMissingStations.size} missing=${missingStations.size} coverage=${coverageStations.size} horizonTo=$toMinutes: " +
                    eligibleMissingStations.joinToString { it.name.take(20) }
            )
        } else {
            Log.d("AbfahrtRepo", "🔄 ${eligibleMissingStations.size} stops missing, fetching via stops=: " +
                eligibleMissingStations.joinToString { it.name.take(20) })
        }

        // ── Step 3: batched calls for direct stop coverage ─────────────────────
        // Build 118 intentionally fetches more than the strictly visible list: all
        // relevant nearby stop IDs are queried in bounded batches so the overview
        // can still filter locally while the detail sheet has enough follow-up
        // departures and opposite directions do not disappear because the initial
        // limit=60 response was saturated by another stop or mode.
        val stationIdsByChunk = eligibleMissingStations
            .map { it.id }
            .chunked(DIRECT_STOPS_CHUNK_SIZE)

        val extraResults = stationIdsByChunk.mapIndexed { index, chunk ->
            try {
                val response = api.getDepartures(
                    lat = lat,
                    lon = lon,
                    radius = radius,
                    limit = DIRECT_STOPS_LIMIT,
                    mode = modeParam,
                    fromMinutes = fromMinutes,
                    toMinutes = toMinutes,
                    dedup = DEDUP_OFF,
                    stops = chunk.joinToString(",")
                )
                val responseStations = (response.stations.orEmpty() + eligibleMissingStations)
                    .distinctBy { station -> station.id.ifBlank { station.name } }
                val departures = enrich(response.departures, responseStations, response.city ?: initial.city)
                Log.d(
                    "AbfahrtRepo",
                    "🧩 stopsBatch ${index + 1}/${stationIdsByChunk.size} ids=${chunk.size} departures=${departures.size}"
                )
                chunk.size to departures
            } catch (e: Exception) {
                Log.w("AbfahrtRepo", "stopsBatch failed chunk=${index + 1}/${stationIdsByChunk.size}: ${e::class.java.simpleName}")
                chunk.size to emptyList()
            }
        }

        val added = extraResults.flatMap { it.second }
        val extraCallsCount = extraResults.size
        val extraCallsWithDepartures = extraResults.count { it.second.isNotEmpty() }
        val stopsCoveredByAddOns = added.map { it.stop }.toSet().size
        val coveredAfterAddOns = coveredStationsAfterInitial + stopsCoveredByAddOns

        Log.d(
            "AbfahrtRepo",
            "📈 extraCalls=$extraCallsCount extraCallsWithDepartures=$extraCallsWithDepartures coveredAfterAddOns=$coveredAfterAddOns addedDepartures=${added.size}"
        )

        val finalResponse = if (added.isEmpty()) {
            initial.copy(departures = enriched, effectiveRadius = effectiveRadius, isFinal = true)
        } else {
            Log.d("AbfahrtRepo", "✅ Added ${added.size} departures from $extraCallsCount batched stops calls")
            initial.copy(departures = enriched + added, effectiveRadius = effectiveRadius, isFinal = true)
        }

        // Progressive radius layers only for initial cold loads / location changes.
        if (!isStationSearch && useLayeredLoading && radius > 500) {
            emitLayeredResponses(finalResponse, radius)
        }
        send(finalResponse.copy(isFinal = true, effectiveRadius = radius))
    }

    private suspend fun kotlinx.coroutines.channels.ProducerScope<DepartureResponse>.emitLayeredResponses(
        response: DepartureResponse,
        requestedRadius: Int
    ) {
        val stations = response.stations.orEmpty().sortedBy { it.distance }
        if (stations.isEmpty()) {
            send(response)
            return
        }

        val layerSteps = listOf(500, 1000, 1500)
            .filter { it < requestedRadius }
            .plus(requestedRadius)
            .distinct()
            .sorted()

        var lastStationCount = 0
        for (layerRadius in layerSteps) {
            val layerStations = stations.filter { it.distance <= layerRadius }
            if (layerStations.isEmpty() || layerStations.size == lastStationCount) continue
            lastStationCount = layerStations.size

            val allowedNames = layerStations.map { it.name }.toSet()
            val layerDepartures = response.departures.filter { it.stop in allowedNames }
            Log.d(
                "AbfahrtRepo",
                "📦 layer radius=$layerRadius stations=${layerStations.size} departures=${layerDepartures.size}"
            )
            send(
                response.copy(
                    departures = layerDepartures,
                    stations = layerStations,
                    effectiveRadius = layerRadius,
                    isFinal = false
                )
            )
        }
    }



    private suspend fun fetchDepartures(
        lat: Double,
        lon: Double,
        radius: Int,
        limit: Int,
        modeParam: String?,
        fromMinutes: Int?,
        toMinutes: Int?,
        dedup: String?
    ): DepartureResponse = api.getDepartures(
        lat = lat,
        lon = lon,
        radius = radius,
        limit = limit,
        mode = modeParam,
        fromMinutes = fromMinutes,
        toMinutes = toMinutes,
        dedup = dedup,
        stops = null
    )


    private fun enrich(
        departures: List<Departure>,
        stations: List<Station>,
        city: String?
    ): List<Departure> = enrichDeparturesWithStations(departures, stations, city)


    suspend fun getTrips(
        fromLat: Double, fromLon: Double,
        toLat: Double, toLon: Double
    ): TripResponse = api.getTrips(fromLat, fromLon, toLat, toLon)
}

internal fun enrichDeparturesWithStations(
    departures: List<Departure>,
    stations: List<Station>,
    city: String?
): List<Departure> {
    if (departures.isEmpty()) return departures
    if (stations.isEmpty()) {
        logUnresolvedProviderStopIds(departures, stationCount = 0)
        return departures
    }

    val stationsById = stations
        .asSequence()
        .filter { it.id.isNotBlank() }
        .flatMap { station ->
            providerStopIdLookupKeys(station.id)
                .map { key -> key to station }
                .asSequence()
        }
        .groupBy(keySelector = { it.first }, valueTransform = { it.second })
    val stationsByExactName = stations.associateBy { it.name }
    val stationsByCanonicalName = stations
        .groupBy { canonicalStopName(it.name, city) }
    val exactStationIds = stations
        .asSequence()
        .map { it.id.trim() }
        .filter { it.isNotBlank() }
        .toSet()

    val enriched = departures.map { dep ->
        val providerStopId = dep.providerStopId
            ?: dep.stop.takeIf(::looksLikeProviderStopId)
            ?: dep.stop.trim().takeIf { it in exactStationIds }

        val matchedByProviderId = providerStopId?.let { rawId ->
            matchStationForDeparture(
                stopName = rawId,
                stationsById = stationsById,
                stationsByExactName = stationsByExactName,
                stationsByCanonicalName = stationsByCanonicalName,
                city = city
            )
        }
        val matchedByName = if (matchedByProviderId == null && providerStopId != dep.stop) {
            matchStationForDeparture(
                stopName = dep.stop,
                stationsById = stationsById,
                stationsByExactName = stationsByExactName,
                stationsByCanonicalName = stationsByCanonicalName,
                city = city
            )
        } else {
            null
        }
        val matchedStation = matchedByProviderId ?: matchedByName

        if (matchedStation != null) {
            dep.copy(
                stop = matchedStation.name,
                stationDistance = matchedStation.distance.takeIf { it > 0 } ?: dep.stationDistance,
                providerStopId = providerStopId
            )
        } else if (providerStopId != null && dep.providerStopId == null) {
            // Preserve the technical stop-point ID even if this batch does not
            // yet contain enough station metadata to resolve a display name.
            dep.copy(providerStopId = providerStopId)
        } else {
            dep
        }
    }
    logUnresolvedProviderStopIds(enriched, stationCount = stations.size)
    return enriched
}

/**
 * A stable refresh merges station lists from the previous and incoming response.
 * Direct stops= batches can be unable to resolve a provider stop ID against their
 * partial batch station list even though the merged response already contains the
 * exact platform station. Run one bounded second enrichment pass against that
 * complete station universe before filtering or rendering.
 */
internal fun enrichProviderStopIdsAfterResponseMerge(
    response: DepartureResponse
): DepartureResponse {
    val exactStationIds = response.stations.orEmpty()
        .asSequence()
        .map { it.id.trim() }
        .filter { it.isNotBlank() }
        .toSet()
    val unresolvedBefore = response.departures.count {
        needsProviderStopIdResolution(it, exactStationIds)
    }
    if (unresolvedBefore == 0) return response

    val enrichedDepartures = enrichDeparturesWithStations(
        departures = response.departures,
        stations = response.stations.orEmpty(),
        city = response.city
    )
    val unresolvedAfter = enrichedDepartures.count {
        needsProviderStopIdResolution(it, exactStationIds)
    }
    logRepoDebug(
        "post-merge stop-id enrichment before=$unresolvedBefore resolved=${unresolvedBefore - unresolvedAfter} remaining=$unresolvedAfter stationCount=${response.stations.orEmpty().size}"
    )
    return response.copy(departures = enrichedDepartures)
}

private fun needsProviderStopIdResolution(
    departure: Departure,
    exactStationIds: Set<String>
): Boolean {
    val stop = departure.stop.trim()
    return looksLikeProviderStopId(stop) || stop in exactStationIds
}

private fun matchStationForDeparture(
    stopName: String,
    stationsById: Map<String, List<Station>>,
    stationsByExactName: Map<String, Station>,
    stationsByCanonicalName: Map<String, List<Station>>,
    city: String?
): Station? {
    for (lookupKey in providerStopIdLookupKeys(stopName)) {
        val idCandidates = stationsById[lookupKey].orEmpty()
        if (idCandidates.isEmpty()) continue

        safeProviderIdCandidate(
            rawStopId = stopName,
            lookupKey = lookupKey,
            candidates = idCandidates,
            city = city
        )?.let { return it }

        // A provider-ID fallback may have crossed into a different physical stop.
        // In that case we intentionally do not continue into name matching: the
        // raw value is an ID, not a display name, and guessing would be worse
        // than leaving the unresolved-ID diagnostic visible for QA.
        if (looksLikeProviderStopId(stopName)) return null
    }
    stationsByExactName[stopName]?.let { return it }
    val canonical = canonicalStopName(stopName, city)
    val candidates = stationsByCanonicalName[canonical].orEmpty()
    return when {
        candidates.isEmpty() -> null
        candidates.size == 1 -> candidates.first()
        else -> candidates.minByOrNull { it.distance }
    }
}

private fun safeProviderIdCandidate(
    rawStopId: String,
    lookupKey: String,
    candidates: List<Station>,
    city: String?
): Station? {
    if (candidates.size == 1) return candidates.first()

    val canonicalNames = candidates
        .map { canonicalStopName(it.name, city) }
        .filter { it.isNotBlank() }
        .distinct()

    return if (canonicalNames.size <= 1) {
        candidates.minByOrNull { it.distance }
    } else {
        logRepoWarn(
            "ambiguous provider stop id match skipped raw=$rawStopId lookup=$lookupKey candidates=" +
                candidates.take(6).joinToString(" | ") { candidate ->
                    "${candidate.id}:${candidate.name}:${candidate.distance}m"
                }
        )
        null
    }
}


private fun providerStopIdLookupKeys(value: String): List<String> {
    val trimmed = value.trim()
    if (!looksLikeProviderStopId(trimmed)) return listOf(trimmed).filter { it.isNotBlank() }

    val baseWithoutPlatform = trimmed.substringBefore("::")
    return listOf(trimmed, baseWithoutPlatform)
        .filter { it.isNotBlank() }
        .distinct()
}

private fun looksLikeProviderStopId(value: String): Boolean {
    if (value.isBlank() || value.length > 128) return false
    if (value.any { it.isWhitespace() || it == ',' }) return false
    return value.count { it == ':' } >= 2
}

private fun logUnresolvedProviderStopIds(
    departures: List<Departure>,
    stationCount: Int
) {
    val unresolved = departures
        .asSequence()
        .map { it.stop }
        .filter { looksLikeProviderStopId(it) }
        .distinct()
        .take(5)
        .toList()
    if (unresolved.isNotEmpty()) {
        logRepoWarn(
            "unresolved provider stop ids after enrichment count=${unresolved.size} stationCount=$stationCount examples=${unresolved.joinToString()}"
        )
    }
}

private fun logProviderWalkSecondsCoverage(stations: List<Station>) {
    if (stations.isEmpty()) return

    val positive = stations.filter { (it.walkSeconds ?: 0) > 0 }
    val nonPositive = stations.count { it.walkSeconds != null && it.walkSeconds <= 0 }
    val absent = stations.size - positive.size - nonPositive
    val examples = positive
        .sortedBy { it.distance }
        .take(3)
        .joinToString(" | ") { station -> "${station.name}:${station.walkSeconds}s" }

    logContractDebug(
        "walkSeconds coverage=${positive.size}/${stations.size} absent=$absent nonPositive=$nonPositive" +
            if (examples.isNotBlank()) " examples=$examples" else ""
    )
}


private fun logRepoDebug(message: String) {
    bestEffortAndroidLog { Log.d("AbfahrtRepo", message) }
}

private fun logRepoWarn(message: String) {
    bestEffortAndroidLog { Log.w("AbfahrtRepo", message) }
}

private fun logContractDebug(message: String) {
    bestEffortAndroidLog { Log.d("AbfahrtContract", message) }
}

/**
 * Repository diagnostics must never affect enrichment results. Local JVM unit tests
 * execute against Android framework stubs whose Log methods throw RuntimeException;
 * device/runtime logging remains unchanged while those diagnostic-only failures are ignored.
 */
private inline fun bestEffortAndroidLog(block: () -> Unit) {
    try {
        block()
    } catch (_: RuntimeException) {
        // Android local-unit-test stub only; diagnostics are intentionally non-functional.
    }
}

private fun canonicalStopName(name: String, city: String?): String =
    StationNameNormalizer.canonicalStopName(name, city)
