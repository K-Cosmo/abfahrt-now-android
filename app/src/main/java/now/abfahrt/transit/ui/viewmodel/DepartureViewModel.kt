package now.abfahrt.transit.ui.viewmodel

import now.abfahrt.transit.BuildConfig
import now.abfahrt.transit.R
import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.location.Location
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority as LocationPriority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import now.abfahrt.transit.data.model.*
import now.abfahrt.transit.data.preferences.UserPreferencesRepository
import now.abfahrt.transit.data.repository.GeocodingRepository
import now.abfahrt.transit.data.repository.TransitRepository
import now.abfahrt.transit.data.repository.enrichProviderStopIdsAfterResponseMerge
import now.abfahrt.transit.data.repository.WalkingRouteRepository
import now.abfahrt.transit.ui.components.inferModeFromLine
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException
import javax.inject.Inject
import kotlin.coroutines.resume
import now.abfahrt.transit.util.CurrentLocationStartupPolicy
import now.abfahrt.transit.util.StartupLocationCorrectionDecision
import now.abfahrt.transit.util.DepartureDisplayOrdering
import now.abfahrt.transit.util.DepartureStableMerger
import now.abfahrt.transit.util.DepartureServiceIdentity
import now.abfahrt.transit.util.DepartureFetchPolicy
import now.abfahrt.transit.util.StationNameNormalizer

@HiltViewModel
@OptIn(FlowPreview::class)
class DepartureViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val repository: TransitRepository,
    private val geocodingRepository: GeocodingRepository,
    private val walkingRouteRepository: WalkingRouteRepository,
    private val prefsRepo: UserPreferencesRepository
) : ViewModel() {

    companion object {
        private var cachedUiState: DepartureUiState = DepartureUiState.Idle
        private var cachedBackgroundError: String? = null
        private var cachedLastLat: Double? = null
        private var cachedLastLon: Double? = null
        private var cachedLastSuccessfulLoadAt: Long? = null
        private var cachedLastRequestSnapshot: RequestSnapshot? = null
        private var cachedSearchUiState: SearchUiState = SearchUiState()
        private var cachedEffectiveStationRadius: Int = 80
        private var cachedTargetGeneration: Long = 0L
        private var cachedWalkingAnchorLat: Double? = null
        private var cachedWalkingAnchorLon: Double? = null
        private const val INITIAL_TIMEOUT_RETRY_DELAY_MS = 750L
    }

    private data class RequestSnapshot(
        val lat: Double,
        val lon: Double,
        val radius: Int,
        val selectedModes: Set<TransportMode>,
        val windowStartMinutes: Int,
        val windowEndMinutes: Int,
        val loadedAt: Long
    )

    private data class ResolvedTargetCoordinates(
        val lat: Double,
        val lon: Double,
        val provisional: Boolean = false
    )


    val preferences: StateFlow<AppPreferences> = prefsRepo.preferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppPreferences())

    private val _preferencesLoaded = MutableStateFlow(false)
    val preferencesLoaded: StateFlow<Boolean> = _preferencesLoaded.asStateFlow()

    private val _uiState = MutableStateFlow(cachedUiState)
    val uiState: StateFlow<DepartureUiState> = _uiState.asStateFlow()

    private val _backgroundError = MutableStateFlow(cachedBackgroundError)
    val backgroundError: StateFlow<String?> = _backgroundError.asStateFlow()
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _searchUiState = MutableStateFlow(cachedSearchUiState)
    val searchUiState: StateFlow<SearchUiState> = _searchUiState.asStateFlow()

    private val searchQueryFlow = MutableStateFlow(cachedSearchUiState.query)

    val isUsingCurrentLocation: Boolean
        get() = _searchUiState.value.selectedTarget is SearchTarget.CurrentLocation


    fun getRoutePreviewOrigin(): Pair<Double, Double>? {
        val lat = lastLat
        val lon = lastLon
        return if (lat != null && lon != null) lat to lon else null
    }

    suspend fun loadRoutePreview(destination: RoutePreviewDestination): RoutePreviewData? {
        val origin = getRoutePreviewOrigin() ?: return null
        return try {
            val preview = walkingRouteRepository.getRoutePreview(
                startLat = origin.first,
                startLon = origin.second,
                endLat = destination.lat,
                endLon = destination.lon,
                stationId = destination.stationId ?: destination.stationName
            )
            if (preview?.routeDistanceMeters != null && preview.routeDurationSeconds != null) {
                applyRoutePreviewWalkingMetric(destination, preview.routeDistanceMeters, preview.routeDurationSeconds)
            }
            preview
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w("AbfahrtRoute", "Route preview failed destination=${destination.stationName}: ${e.javaClass.simpleName}")
            null
        }
    }

    private suspend fun applyRoutePreviewWalkingMetric(
        destination: RoutePreviewDestination,
        distanceMeters: Int,
        durationSeconds: Int
    ) {
        val state = _uiState.value as? DepartureUiState.Success ?: return
        val metric = WalkingRouteInfo(distanceMeters = distanceMeters, durationSeconds = durationSeconds)
        val patchedResponse = enrichResponseWithWalking(
            response = state.response,
            walkingByStop = mapOf(destination.stationName to metric),
            approximateStops = emptySet()
        )
        if (patchedResponse == state.response) return

        val prefsSnapshot = preferences.value
        val patchedFiltered = withContext(Dispatchers.Default) { applyFilters(patchedResponse, prefsSnapshot) }
        withContext(Dispatchers.Main.immediate) {
            val latest = _uiState.value as? DepartureUiState.Success ?: return@withContext
            if (latest.response != state.response) return@withContext
            val patchedState = latest.copy(response = patchedResponse, filtered = patchedFiltered)
            _uiState.value = patchedState
            cachedUiState = patchedState
            Log.d(
                "AbfahrtWalk",
                "🎯 detail route metric applied stop=${destination.stationName} distance=$distanceMeters duration=$durationSeconds"
            )
        }
    }

    private fun bumpTargetGeneration(reason: String) {
        activeTargetGeneration += 1L
        cachedTargetGeneration = activeTargetGeneration
        Log.d("AbfahrtTarget", "🔀 target generation=$activeTargetGeneration reason=$reason")
    }

    private fun isCurrentTargetRequest(lat: Double, lon: Double, requestGeneration: Long): Boolean {
        if (requestGeneration != activeTargetGeneration) return false
        return when (val target = _searchUiState.value.selectedTarget) {
            is SearchTarget.CurrentLocation -> true
            is SearchTarget.Station -> approximatelySameLocation(target.lat, lat) &&
                approximatelySameLocation(target.lon, lon)
        }
    }

    fun clearBackgroundError() {
        _backgroundError.value = null
        cachedBackgroundError = null
    }

    private var lastLat: Double? = cachedLastLat
    private var lastLon: Double? = cachedLastLon
    private var lastSuccessfulLoadAt: Long? = cachedLastSuccessfulLoadAt
    private var lastRequestSnapshot: RequestSnapshot? = cachedLastRequestSnapshot
    private var effectiveStationRadius: Int = cachedEffectiveStationRadius
    private var activeTargetGeneration: Long = cachedTargetGeneration
    private var walkingAnchorLat: Double? = cachedWalkingAnchorLat
    private var walkingAnchorLon: Double? = cachedWalkingAnchorLon

    private val MANUAL_STATION_RADIUS_M = 80
    private val SEARCH_STATION_RADIUS_M = 200
    private val WALKING_REQUEST_DELAY_MS = 650L
    private val WALKING_MAX_STATIONS = 25
    private val WALKING_MAX_STATION_BATCHES = 3
    private val CONNECTIVITY_GRACE_MS = 1_500L

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val loc = result.lastLocation ?: return
            if (!isUsingCurrentLocation) return

            if (lastLat == null || lastLon == null) {
                lastLat = loc.latitude
                lastLon = loc.longitude
                cachedLastLat = loc.latitude
                cachedLastLon = loc.longitude
                viewModelScope.launch { loadForCurrentTarget() }
                return
            }
            val prev = android.location.Location("").apply {
                latitude = lastLat!!
                longitude = lastLon!!
            }.distanceTo(loc)

            if (prev >= CurrentLocationStartupPolicy.MOVEMENT_THRESHOLD_METERS) {
                Log.d("AbfahrtLocation", "📍 Moved ${prev.toInt()}m")
                lastLat = loc.latitude
                lastLon = loc.longitude
                cachedLastLat = loc.latitude
                cachedLastLon = loc.longitude
                pendingHardResetRefresh = true
                if (isLoadInProgress || provisionalStartupLoadActive) {
                    pendingLocationRefresh = true
                    Log.d("AbfahrtRefresh", "⏳ Deferred hard-reset location refresh while load is active")
                    return
                }
                viewModelScope.launch {
                    loadForCurrentTarget(force = true, hardReset = true)
                }
            }
        }
    }
    private var autoRefreshJob: Job? = null
    private var activeLoadSignature: String? = null
    private var walkingEnrichmentJob: Job? = null
    private var refilterJob: Job? = null
    private var startupLocationCorrectionJob: Job? = null
    private var lastWalkingResponseSignature: String? = null
    private var isLoadInProgress: Boolean = false
    private var provisionalStartupLoadActive: Boolean = false
    private var pendingLocationRefresh: Boolean = false
    private var pendingHardResetRefresh: Boolean = false
    private var pendingTravelModeRefresh: Boolean = false
    private var forceNextWalkingEnrichment: Boolean = false

    private val connectivityManager: ConnectivityManager by lazy {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    init {
        var prevMaxMinutes = -1
        var prevStartMinutes = -1
        var prevModes: Set<TransportMode>? = null
        var prevMaxPerDir = -1
        var prevDepartureSortProfile: DepartureSortProfile? = null
        var prevOrsTravelMode: OrsTravelMode? = null
        var prevHideUnreachable: Boolean? = null

        viewModelScope.launch {
            preferences.collect { prefs ->
                if (!_preferencesLoaded.value) _preferencesLoaded.value = true
                val minutesChanged = prevMaxMinutes != prefs.windowEndMinutes ||
                    prevStartMinutes != prefs.windowStartMinutes
                val maxDirChanged = prevMaxPerDir != prefs.maxPerDirection
                val departureSortChanged = prevDepartureSortProfile != null &&
                    prevDepartureSortProfile != prefs.departureSortProfile
                val prevM = prevModes
                val modesAdded = prevM != null && prefs.selectedModes.any { it !in prevM }
                val modesChanged = prevM != null && prevM != prefs.selectedModes
                val orsTravelModeChanged = prevOrsTravelMode != null && prevOrsTravelMode != prefs.orsTravelMode
                val hideUnreachableChanged = prevHideUnreachable != null && prevHideUnreachable != prefs.hideUnreachableDepartures
                if (modesAdded && shouldFetchForModeExpansion(prefs.selectedModes)) {
                    refresh(force = false)
                } else if (minutesChanged || modesChanged || maxDirChanged || departureSortChanged || hideUnreachableChanged) {
                    refilter()
                }
                prevMaxMinutes = prefs.windowEndMinutes
                prevStartMinutes = prefs.windowStartMinutes
                prevModes = prefs.selectedModes
                prevMaxPerDir = prefs.maxPerDirection
                prevDepartureSortProfile = prefs.departureSortProfile
                prevOrsTravelMode = prefs.orsTravelMode
                prevHideUnreachable = prefs.hideUnreachableDepartures
                restartAutoRefresh(prefs.refreshIntervalMinutes)
            }
        }

        viewModelScope.launch {
            searchQueryFlow
                .debounce(400)
                .distinctUntilChanged()
                .collectLatest { rawQuery ->
                    val query = rawQuery.trim()
                    if (query.length < 3) {
                        _searchUiState.value = _searchUiState.value.copy(
                            isSearching = false,
                            results = emptyList(),
                            errorMessage = null
                        ).also { cachedSearchUiState = it }
                        return@collectLatest
                    }

                    _searchUiState.value = _searchUiState.value.copy(
                        isSearching = true,
                        errorMessage = null
                    ).also { cachedSearchUiState = it }

                    try {
                        val cityHint = (_uiState.value as? DepartureUiState.Success)?.response?.city
                        val results = geocodingRepository.searchStations(query, cityHint)
                        Log.d("PhotonSearch", "query='$query' results=${results.size}")
                        _searchUiState.value = _searchUiState.value.copy(
                            isSearching = false,
                            results = results,
                            errorMessage = null
                        ).also { cachedSearchUiState = it }
                    } catch (e: CancellationException) {
                        Log.d("PhotonSearch", "query='$query' cancelled")
                        throw e
                    } catch (e: Exception) {
                        Log.e("PhotonSearch", "query='$query' failed: ${e::class.java.simpleName}: ${e.message}", e)
                        _searchUiState.value = _searchUiState.value.copy(
                            isSearching = false,
                            results = emptyList(),
                            errorMessage = friendlySearchMessage(e)
                        ).also { cachedSearchUiState = it }
                    }
                }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchUiState.value = _searchUiState.value.copy(
            query = query,
            errorMessage = null,
            results = if (query.trim().length < 3) emptyList() else _searchUiState.value.results
        ).also { cachedSearchUiState = it }
        searchQueryFlow.value = query
    }

    fun clearSearchResults() {
        _searchUiState.value = _searchUiState.value.copy(
            query = "",
            results = emptyList(),
            isSearching = false,
            errorMessage = null
        ).also { cachedSearchUiState = it }
        searchQueryFlow.value = ""
    }

    fun selectSearchResult(result: SearchResult) {
        bumpTargetGeneration("station:${result.title}")
        viewModelScope.launch {
            effectiveStationRadius = SEARCH_STATION_RADIUS_M
            cachedEffectiveStationRadius = effectiveStationRadius
            _backgroundError.value = null
            cachedBackgroundError = null
            _isRefreshing.value = true
            val loadingState = DepartureUiState.Loading
            _uiState.value = loadingState
            cachedUiState = loadingState
            val newState = _searchUiState.value.copy(
                query = "",
                results = emptyList(),
                isSearching = false,
                errorMessage = null,
                selectedTarget = SearchTarget.Station(
                    name = result.title,
                    subtitle = result.subtitle,
                    lat = result.lat,
                    lon = result.lon
                )
            )
            _searchUiState.value = newState
            cachedSearchUiState = newState
            loadForCurrentTarget(force = true)
        }
    }

    fun useCurrentLocation() {
        bumpTargetGeneration("current_location")
        effectiveStationRadius = MANUAL_STATION_RADIUS_M
        cachedEffectiveStationRadius = effectiveStationRadius
        val newState = _searchUiState.value.copy(
            query = "",
            results = emptyList(),
            isSearching = false,
            errorMessage = null,
            selectedTarget = SearchTarget.CurrentLocation
        )
        _searchUiState.value = newState
        cachedSearchUiState = newState
        searchQueryFlow.value = ""
    }

    /**
     * Leaves the dedicated alternate-location flow and restores the home screen to
     * its current-location baseline. The previous alternate response must not flash
     * on the home screen while a fresh location response is loaded.
     */
    fun leaveAlternateLocationMode() {
        bumpTargetGeneration("current_location_after_alternate")
        effectiveStationRadius = MANUAL_STATION_RADIUS_M
        cachedEffectiveStationRadius = effectiveStationRadius
        val newSearchState = _searchUiState.value.copy(
            query = "",
            results = emptyList(),
            isSearching = false,
            errorMessage = null,
            selectedTarget = SearchTarget.CurrentLocation
        )
        _searchUiState.value = newSearchState
        cachedSearchUiState = newSearchState
        searchQueryFlow.value = ""
        _backgroundError.value = null
        cachedBackgroundError = null
        _isRefreshing.value = false
        val idle = DepartureUiState.Idle
        _uiState.value = idle
        cachedUiState = idle
        cancelActiveOrsEnrichment()
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        if (!isUsingCurrentLocation) return
        val request = LocationRequest.Builder(
            LocationPriority.PRIORITY_BALANCED_POWER_ACCURACY,
            120_000L
        )
            .setMinUpdateDistanceMeters(CurrentLocationStartupPolicy.MOVEMENT_THRESHOLD_METERS)
            .setWaitForAccurateLocation(false)
            .build()

        LocationServices.getFusedLocationProviderClient(context)
            .requestLocationUpdates(request, locationCallback, context.mainLooper)
        Log.d(
            "AbfahrtLocation",
            "▶ Location updates started (threshold=${CurrentLocationStartupPolicy.MOVEMENT_THRESHOLD_METERS}m)"
        )
    }

    fun stopLocationUpdates() {
        LocationServices.getFusedLocationProviderClient(context)
            .removeLocationUpdates(locationCallback)
        Log.d("AbfahrtLocation", "⏹ Location updates stopped")
    }

    fun cancelActiveOrsEnrichment() {
        if (walkingEnrichmentJob?.isActive == true) {
            walkingEnrichmentJob?.cancel()
            Log.d("AbfahrtWalk", "🛑 cancelled active ORS enrichment because app is no longer visible")
        }
    }

    @SuppressLint("MissingPermission")
    fun fetchDepartures() {
        if (isLoadInProgress) {
            Log.d("AbfahrtRefresh", "🚫 fetchDepartures ignored while another load is active")
            _isRefreshing.value = false
            return
        }
        if (!isUsingCurrentLocation) {
            viewModelScope.launch { loadForCurrentTarget(force = true) }
            return
        }
        viewModelScope.launch {
            _uiState.value = DepartureUiState.Loading
            try {
                loadForCurrentTarget(
                    force = true,
                    allowProvisionalLocation = lastSuccessfulLoadAt == null
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val errorState = DepartureUiState.Error(friendlyMessage(e))
                _uiState.value = errorState
                cachedUiState = errorState
            }
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun getHighAccuracyLocation(): Location? {
        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
        val cts = CancellationTokenSource()
        return suspendCancellableCoroutine { cont ->
            fusedClient.getCurrentLocation(LocationPriority.PRIORITY_HIGH_ACCURACY, cts.token)
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.resume(null) }
            cont.invokeOnCancellation { cts.cancel() }
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun getLastKnownLocation(): Location? {
        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
        return suspendCancellableCoroutine { cont ->
            fusedClient.lastLocation
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.resume(null) }
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun getBestLocation(): Location? {
        val fresh = getHighAccuracyLocation()
        if (fresh != null) return fresh
        return getLastKnownLocation()
    }

    private fun isUsableLocation(location: Location): Boolean =
        location.latitude != 0.0 || location.longitude != 0.0

    private fun scheduleStartupLocationCorrection(
        provisionalLat: Double,
        provisionalLon: Double,
        requestGeneration: Long
    ) {
        startupLocationCorrectionJob?.cancel()
        startupLocationCorrectionJob = viewModelScope.launch {
            val fresh = try {
                getHighAccuracyLocation()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.d("AbfahrtLocation", "🎯 high-accuracy startup correction unavailable: ${e.javaClass.simpleName}")
                null
            }

            if (fresh == null || !isUsableLocation(fresh)) {
                Log.d("AbfahrtLocation", "🎯 high-accuracy startup correction unavailable; keeping provisional origin")
                return@launch
            }

            val targetStillCurrent = requestGeneration == activeTargetGeneration && isUsingCurrentLocation
            val correctionDistance = distanceMeters(
                provisionalLat,
                provisionalLon,
                fresh.latitude,
                fresh.longitude
            )

            when (
                CurrentLocationStartupPolicy.correctionDecision(
                    targetStillCurrent = targetStillCurrent,
                    distanceMeters = correctionDistance
                )
            ) {
                StartupLocationCorrectionDecision.IGNORE_STALE_TARGET -> {
                    Log.d("AbfahrtLocation", "🎯 dropping stale high-accuracy startup correction")
                }
                StartupLocationCorrectionDecision.KEEP_PROVISIONAL -> {
                    Log.d(
                        "AbfahrtLocation",
                        "🎯 high-accuracy startup correction same-origin distance=${correctionDistance.toInt()}m; keeping current Core context"
                    )
                }
                StartupLocationCorrectionDecision.REANCHOR -> {
                    lastLat = fresh.latitude
                    lastLon = fresh.longitude
                    cachedLastLat = fresh.latitude
                    cachedLastLon = fresh.longitude
                    pendingHardResetRefresh = true
                    pendingLocationRefresh = true
                    lastWalkingResponseSignature = null
                    if (walkingEnrichmentJob?.isActive == true) {
                        walkingEnrichmentJob?.cancel()
                        Log.d("AbfahrtWalk", "🛑 cancelled provisional-origin ORS because location re-anchor is pending")
                    }
                    Log.d(
                        "AbfahrtLocation",
                        "🧭 high-accuracy startup correction requires re-anchor distance=${correctionDistance.toInt()}m"
                    )
                    if (!provisionalStartupLoadActive) {
                        launchPendingLocationRefreshIfPossible()
                    }
                }
            }
        }
    }

    fun refreshIfStale() {
        val prefs = preferences.value
        val loadedAt = lastSuccessfulLoadAt ?: return
        if (prefs.refreshIntervalMinutes <= 0) return
        val maxAgeMs = prefs.refreshIntervalMinutes * 60_000L
        val ageMs = System.currentTimeMillis() - loadedAt
        if (_uiState.value is DepartureUiState.Success && ageMs >= maxAgeMs) refresh()
    }

    fun refresh(force: Boolean = false) {
        val target = _searchUiState.value.selectedTarget
        when (target) {
            is SearchTarget.CurrentLocation -> {
                val lat = lastLat
                val lon = lastLon
                if (lat != null && lon != null) {
                    viewModelScope.launch {
                        if (isLoadInProgress) {
                            Log.d("AbfahrtRefresh", "🚫 Manual refresh ignored while another load is active")
                            _isRefreshing.value = false
                            return@launch
                        }
                        if (!force && shouldThrottleRefresh(preferences.value, lat, lon, currentRequestRadius())) {
                            Log.d("AbfahrtRefresh", "⏱️ Manual refresh served locally (<60s, same request snapshot)")
                            _isRefreshing.value = false
                            refilter()
                        } else {
                            _isRefreshing.value = true
                            loadForCurrentTarget(force = force)
                        }
                    }
                } else {
                    fetchDepartures()
                }
            }
            is SearchTarget.Station -> {
                viewModelScope.launch {
                    if (isLoadInProgress) {
                        Log.d("AbfahrtRefresh", "🚫 Station refresh ignored while another load is active")
                        _isRefreshing.value = false
                        return@launch
                    }
                    if (!force && shouldThrottleRefresh(preferences.value, target.lat, target.lon, currentRequestRadius())) {
                        Log.d("AbfahrtRefresh", "⏱️ Station refresh served locally (<60s, same request snapshot)")
                        _isRefreshing.value = false
                        refilter()
                    } else {
                        _isRefreshing.value = true
                        loadForCurrentTarget(force = force)
                    }
                }
            }
        }
    }

    private fun isWithinThrottleWindow(now: Long = System.currentTimeMillis()): Boolean {
        val loadedAt = lastSuccessfulLoadAt ?: return false
        return now - loadedAt < 60_000L
    }

    private fun shouldThrottleRefresh(
        prefs: AppPreferences,
        lat: Double,
        lon: Double,
        radius: Int,
        now: Long = System.currentTimeMillis()
    ): Boolean {
        val snapshot = lastRequestSnapshot ?: return false
        if (!isWithinThrottleWindow(now)) return false
        return snapshot.radius == radius &&
            snapshot.selectedModes == prefs.selectedModes &&
            snapshot.windowStartMinutes == prefs.windowStartMinutes &&
            snapshot.windowEndMinutes == prefs.windowEndMinutes &&
            approximatelySameLocation(snapshot.lat, lat) &&
            approximatelySameLocation(snapshot.lon, lon)
    }

    private fun shouldThrottleLocationRefresh(now: Long = System.currentTimeMillis()): Boolean =
        isWithinThrottleWindow(now)

    private fun shouldFetchForModeExpansion(newModes: Set<TransportMode>): Boolean {
        val snapshot = lastRequestSnapshot ?: return true
        return !newModes.all { it in snapshot.selectedModes }
    }

    private fun approximatelySameLocation(a: Double, b: Double): Boolean =
        kotlin.math.abs(a - b) < 0.0001

    private fun currentRequestRadius(): Int =
        if (isUsingCurrentLocation) preferences.value.radius else effectiveStationRadius

    private suspend fun resolveCurrentTargetCoordinates(
        allowProvisionalLocation: Boolean = false
    ): ResolvedTargetCoordinates {
        return when (val target = _searchUiState.value.selectedTarget) {
            is SearchTarget.CurrentLocation -> {
                if (allowProvisionalLocation) {
                    val lastKnown = getLastKnownLocation()
                    if (
                        CurrentLocationStartupPolicy.shouldUseProvisionalOrigin(lastKnown != null) &&
                        lastKnown != null &&
                        isUsableLocation(lastKnown)
                    ) {
                        lastLat = lastKnown.latitude
                        lastLon = lastKnown.longitude
                        cachedLastLat = lastKnown.latitude
                        cachedLastLon = lastKnown.longitude
                        Log.d(
                            "AbfahrtLocation",
                            "⚡ provisional lastLocation available; Core may start before high-accuracy correction"
                        )
                        return ResolvedTargetCoordinates(
                            lat = lastKnown.latitude,
                            lon = lastKnown.longitude,
                            provisional = true
                        )
                    }
                    Log.d("AbfahrtLocation", "⚡ no usable provisional lastLocation; waiting for high accuracy")
                }

                val location = getBestLocation() ?: throw Exception(context.getString(R.string.error_location))
                if (!isUsableLocation(location)) {
                    throw Exception(context.getString(R.string.error_no_gps))
                }
                lastLat = location.latitude
                lastLon = location.longitude
                cachedLastLat = location.latitude
                cachedLastLon = location.longitude
                ResolvedTargetCoordinates(location.latitude, location.longitude)
            }
            is SearchTarget.Station -> ResolvedTargetCoordinates(target.lat, target.lon)
        }
    }

    private suspend fun loadForCurrentTarget(
        force: Boolean = false,
        hardReset: Boolean = false,
        allowProvisionalLocation: Boolean = false
    ) {
        val requestGeneration = activeTargetGeneration
        val resolved = resolveCurrentTargetCoordinates(allowProvisionalLocation = allowProvisionalLocation)
        val lat = resolved.lat
        val lon = resolved.lon
        if (!isCurrentTargetRequest(lat, lon, requestGeneration)) {
            Log.d("AbfahrtTarget", "🚫 Dropping stale target request before network generation=$requestGeneration active=$activeTargetGeneration lat=$lat lon=$lon")
            return
        }
        val radius = currentRequestRadius()
        if (!force && shouldThrottleRefresh(preferences.value, lat, lon, radius)) {
            refilter()
            return
        }
        val shouldHardReset = hardReset || pendingHardResetRefresh
        val provisionalFastPath = resolved.provisional && !shouldHardReset && isUsingCurrentLocation
        if (provisionalFastPath) {
            provisionalStartupLoadActive = true
            scheduleStartupLocationCorrection(
                provisionalLat = lat,
                provisionalLon = lon,
                requestGeneration = requestGeneration
            )
        }

        try {
            loadWithCoordinates(lat, lon, radius, requestGeneration, shouldHardReset)
        } finally {
            if (provisionalFastPath) {
                provisionalStartupLoadActive = false
                launchPendingLocationRefreshIfPossible()
            }
        }
    }

    private fun launchPendingLocationRefreshIfPossible(): Boolean {
        if (!pendingLocationRefresh || !isUsingCurrentLocation || isLoadInProgress || provisionalStartupLoadActive) {
            return false
        }
        pendingLocationRefresh = false
        viewModelScope.launch {
            if (!isLoadInProgress && isUsingCurrentLocation) {
                loadForCurrentTarget(force = true, hardReset = pendingHardResetRefresh)
            } else if (isUsingCurrentLocation) {
                pendingLocationRefresh = true
            }
        }
        return true
    }

    private fun restartAutoRefresh(intervalMinutes: Int) {
        autoRefreshJob?.cancel()
        if (intervalMinutes <= 0) return
        autoRefreshJob = viewModelScope.launch {
            delay(intervalMinutes * 60_000L)
            while (true) {
                val isForegrounded = ProcessLifecycleOwner.get()
                    .lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
                if (isForegrounded) {
                    if (isLoadInProgress) {
                        Log.d("AbfahrtRefresh", "🚫 Auto-refresh skipped while another load is active")
                    } else {
                        try {
                            loadForCurrentTarget(force = true, hardReset = pendingHardResetRefresh)
                        } catch (_: Exception) {
                            // handled in loadWithCoordinates / fetch path
                        }
                    }
                }
                delay(intervalMinutes * 60_000L)
            }
        }
    }

    private suspend fun loadWithCoordinates(lat: Double, lon: Double, radius: Int, requestGeneration: Long, hardReset: Boolean = false, retryAttempt: Int = 0) {
        val prefs = preferences.value
        val requestSignature = buildRequestSignature(lat, lon, radius, prefs)
        if (isLoadInProgress) {
            Log.d("AbfahrtRefresh", "🚫 Skipping refresh while load already in progress")
            return
        }
        if (activeLoadSignature == requestSignature) {
            Log.d("AbfahrtRefresh", "🚫 Skipping duplicate in-flight request: $requestSignature")
            return
        }

        val isBackground = _uiState.value is DepartureUiState.Success
        val sameWalkingOrigin = isSameWalkingOrigin(lat, lon)
        val useColdStartDedupBooster = !isBackground && !hardReset && isUsingCurrentLocation && retryAttempt == 0
        if (!awaitConnectivityForCoreRefresh()) {
            if (!isBackground && retryAttempt == 0) {
                Log.d("AbfahrtNet", "🔁 retrying initial core refresh once after connectivity check failed")
                _isRefreshing.value = false
                delay(INITIAL_TIMEOUT_RETRY_DELAY_MS)
                if (isCurrentTargetRequest(lat, lon, requestGeneration)) {
                    loadWithCoordinates(
                        lat = lat,
                        lon = lon,
                        radius = radius,
                        requestGeneration = requestGeneration,
                        hardReset = hardReset,
                        retryAttempt = retryAttempt + 1
                    )
                }
                return
            }

            val message = context.getString(R.string.error_no_internet)
            Log.d("AbfahrtNet", "❌ showing network error after connectivity + retry failed")
            if (isBackground) {
                _backgroundError.value = message
                cachedBackgroundError = message
            } else if (!hardReset) {
                val errorState = DepartureUiState.Error(message)
                _uiState.value = errorState
                cachedUiState = errorState
            }
            _isRefreshing.value = false
            return
        }
        if (!isBackground) {
            _uiState.value = DepartureUiState.Loading
        } else if (hardReset) {
            // Intentional stable-refresh behavior: keep the visible list on screen while
            // a location-change replacement is loaded. The final response is still
            // evaluated without stable merge because hardReset=true below.
            Log.d("AbfahrtRefresh", "🧭 location changed; keeping previous departures visible until replacement result is ready")
        }
        isLoadInProgress = true
        activeLoadSignature = requestSignature
        if (!sameWalkingOrigin || forceNextWalkingEnrichment) {
            lastWalkingResponseSignature = null
            walkingEnrichmentJob?.cancel()
        } else {
            Log.d("AbfahrtWalk", "🧘 same walking origin; preserving previous ORS metrics and active cache")
        }
        var retryInitialTimeout = false
        val searchDistanceReference = if (!isUsingCurrentLocation) {
            resolveSearchDistanceReference()
        } else {
            null
        }

        try {
            val fetchToMinutes = DepartureFetchPolicy.requestToMinutesForDisplayWindow(prefs.windowEndMinutes)
            if (fetchToMinutes != prefs.windowEndMinutes) {
                Log.d(
                    "AbfahrtRepo",
                    "🔭 data catchment widened displayTo=${prefs.windowEndMinutes} fetchTo=$fetchToMinutes for detail follow-ups"
                )
            }
            repository.getDeparturesProgressive(
                lat = lat,
                lon = lon,
                radius = radius,
                selectedModes = prefs.selectedModes,
                fromMinutes = prefs.windowStartMinutes,
                toMinutes = fetchToMinutes,
                isStationSearch = !isUsingCurrentLocation,
                useLayeredLoading = shouldUseLayeredLoading(lat, lon, radius) && !isBackground,
                initialDedupBooster = useColdStartDedupBooster,
                broadStopCoverage = isUsingCurrentLocation
            ).collect { response ->
                    if (!isCurrentTargetRequest(lat, lon, requestGeneration)) {
                        Log.d(
                            "AbfahrtTarget",
                            "🚫 Ignoring stale response generation=$requestGeneration active=$activeTargetGeneration lat=$lat lon=$lon target=${_searchUiState.value.selectedTarget}"
                        )
                        return@collect
                    }
                    if (!isUsingCurrentLocation) {
                        effectiveStationRadius = response.effectiveRadius ?: MANUAL_STATION_RADIUS_M
                        cachedEffectiveStationRadius = effectiveStationRadius
                    } else {
                        effectiveStationRadius = MANUAL_STATION_RADIUS_M
                        cachedEffectiveStationRadius = effectiveStationRadius
                    }

                    val previousResponse = if (hardReset) null else (_uiState.value as? DepartureUiState.Success)?.response
                    val stableRefreshMerge = previousResponse != null &&
                        response.isFinal &&
                        !hardReset &&
                        sameWalkingOrigin &&
                        isUsingCurrentLocation
                    val preservePrevious = !hardReset && (
                        !response.isFinal ||
                            ((response.effectiveRadius ?: radius) < radius) ||
                            stableRefreshMerge
                        )
                    if (stableRefreshMerge) {
                        Log.d(
                            "AbfahrtMerge",
                            "🧷 stable refresh merge active previous=${previousResponse.departures.size} incoming=${response.departures.size} reason=same_walking_origin"
                        )
                    }
                    val mergedResponse = mergeResponse(
                        previous = previousResponse,
                        incoming = response,
                        matchWindowMinutes = prefs.refreshIntervalMinutes.coerceIn(DepartureStableMerger.DEFAULT_STABLE_MATCH_WINDOW_MINUTES, DepartureStableMerger.MAX_MATCH_WINDOW_MINUTES),
                        preservePrevious = preservePrevious
                    )
                    val baseResponse = enrichProviderStopIdsAfterResponseMerge(mergedResponse)
                    val rawDisplayedResponse = searchDistanceReference?.let { (referenceLat, referenceLon) ->
                        rebaseSearchResponseDistances(
                            response = baseResponse,
                            referenceLat = referenceLat,
                            referenceLon = referenceLon
                        )
                    } ?: baseResponse
                    val displayedResponse = if (sameWalkingOrigin && previousResponse != null && !forceNextWalkingEnrichment) {
                        carryForwardWalkingMetrics(previousResponse, rawDisplayedResponse)
                    } else {
                        rawDisplayedResponse
                    }

                    val filtered = withContext(Dispatchers.Default) { applyFilters(displayedResponse, prefs) }
                    Log.d(
                        "AbfahrtMerge",
                        "response previous=${previousResponse?.departures?.size ?: 0} incoming=${response.departures.size} merged=${displayedResponse.departures.size} filtered=${filtered.size} preservePrevious=$preservePrevious stableRefreshMerge=$stableRefreshMerge final=${response.isFinal}"
                    )
                    val successState = DepartureUiState.Success(displayedResponse, filtered)
                    _uiState.value = successState
                    cachedUiState = successState

                    if (response.isFinal) {
                        if (pendingLocationRefresh && isUsingCurrentLocation) {
                            Log.d("AbfahrtWalk", "⏭️ skipping ORS enrichment because a location re-anchor is pending")
                        } else {
                            val walkingOrigin = when {
                                isUsingCurrentLocation -> lat to lon
                                searchDistanceReference != null -> searchDistanceReference
                                else -> null
                            }
                            if (walkingOrigin != null) {
                                val shouldEnrichWalking = shouldLaunchWalkingEnrichment(
                                    response = displayedResponse,
                                    sameWalkingOrigin = sameWalkingOrigin,
                                    force = forceNextWalkingEnrichment
                                )
                                if (shouldEnrichWalking) {
                                    forceNextWalkingEnrichment = false
                                    launchWalkingEnrichment(
                                        response = displayedResponse,
                                        originLat = walkingOrigin.first,
                                        originLon = walkingOrigin.second,
                                        requestGeneration = requestGeneration,
                                        requestSignature = requestSignature
                                    )
                                } else {
                                    Log.d("AbfahrtWalk", "🧘 skipped ORS enrichment: same walking origin and carried metrics are available")
                                }
                            } else if (BuildConfig.DEBUG && !isUsingCurrentLocation) {
                                Log.d("AbfahrtWalk", "⏭️ search-mode ORS skipped because current location is unavailable")
                            }
                        }
                    }

                    val loadedAt = System.currentTimeMillis()
                    if (hardReset) {
                        pendingHardResetRefresh = false
                    }
                    lastSuccessfulLoadAt = loadedAt
                    cachedLastSuccessfulLoadAt = loadedAt
                    lastRequestSnapshot = RequestSnapshot(
                        lat = lat,
                        lon = lon,
                        radius = radius,
                        selectedModes = prefs.selectedModes,
                        windowStartMinutes = prefs.windowStartMinutes,
                        windowEndMinutes = prefs.windowEndMinutes,
                        loadedAt = loadedAt
                    )
                    cachedLastRequestSnapshot = lastRequestSnapshot
                    _backgroundError.value = null
                    cachedBackgroundError = null
                }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            if (!isCurrentTargetRequest(lat, lon, requestGeneration)) {
                Log.d("AbfahrtTarget", "🚫 Ignoring stale error generation=$requestGeneration active=$activeTargetGeneration lat=$lat lon=$lon error=${e::class.java.simpleName}")
                return
            }
            if (e is HttpException && e.code() == 401) {
                Log.w("AbfahrtAuth", "abfahrt.now rejected API key (401); returning to key entry")
                prefsRepo.setOnboardingCompleted(false)
                return
            }
            Log.w(
                "AbfahrtNet",
                "request failed type=${e::class.java.simpleName} message=${e.message}",
                e
            )
            if (isTransientNetworkIssue(e) && retryAttempt == 0) {
                retryInitialTimeout = true
                Log.d("AbfahrtNet", "🔁 retrying initial core refresh once after transient network failure (${e::class.java.simpleName})")
                Log.d("AbfahrtNet", "📶 suppressing visible network error until retry is exhausted")
            } else {
                val msg = friendlyMessage(e)
                if (isBackground) {
                    _backgroundError.value = msg
                    cachedBackgroundError = msg
                    Log.d("AbfahrtNet", "❌ showing visible network error after initial request + retry failed")
                    if (e is HttpException && e.code() == 429) {
                        autoRefreshJob?.cancel()
                        Log.w("AbfahrtVM", "429 received — auto-refresh paused")
                    }
                } else {
                    Log.d("AbfahrtNet", "❌ showing visible network error after initial request + retry failed")
                    val errorState = DepartureUiState.Error(msg)
                    _uiState.value = errorState
                    cachedUiState = errorState
                }
            }
        } finally {
            if (activeLoadSignature == requestSignature) {
                activeLoadSignature = null
            }
            isLoadInProgress = false
            _isRefreshing.value = false
            if (pendingLocationRefresh && isUsingCurrentLocation) {
                launchPendingLocationRefreshIfPossible()
            } else if (pendingTravelModeRefresh) {
                pendingTravelModeRefresh = false
                viewModelScope.launch {
                    if (!isLoadInProgress) {
                        _isRefreshing.value = true
                        loadForCurrentTarget(force = true, hardReset = pendingHardResetRefresh)
                    }
                }
            }
        }
        if (retryInitialTimeout && isCurrentTargetRequest(lat, lon, requestGeneration)) {
            delay(INITIAL_TIMEOUT_RETRY_DELAY_MS)
            loadWithCoordinates(
                lat = lat,
                lon = lon,
                radius = radius,
                requestGeneration = requestGeneration,
                hardReset = hardReset,
                retryAttempt = retryAttempt + 1
            )
        }
    }


    private fun isSameWalkingOrigin(lat: Double, lon: Double): Boolean {
        if (!isUsingCurrentLocation) return false
        val anchorLat = walkingAnchorLat ?: return false
        val anchorLon = walkingAnchorLon ?: return false
        return distanceMeters(anchorLat, anchorLon, lat, lon) < CurrentLocationStartupPolicy.MOVEMENT_THRESHOLD_METERS
    }

    private fun hasWalkingMetrics(response: DepartureResponse): Boolean =
        response.departures.any { normalizedDistance(it.walkDistance) != null || it.usesApproximateDistance }

    private fun unresolvedRelevantWalkingMetricStops(response: DepartureResponse): Set<String> {
        if (!isUsingCurrentLocation) return emptySet()
        val effectiveRadius = currentRequestRadius()
        return response.departures
            .asSequence()
            .filterNot { it.isHereOverride() }
            .filter { dep ->
                val stationDistance = normalizedDistance(dep.stationDistance)
                stationDistance != null && stationDistance <= effectiveRadius
            }
            .filter { dep -> normalizedDistance(dep.walkDistance) == null && !dep.usesApproximateDistance }
            .map { it.stop }
            .filter { it.isNotBlank() }
            .toCollection(linkedSetOf<String>())
    }

    private fun shouldLaunchWalkingEnrichment(
        response: DepartureResponse,
        sameWalkingOrigin: Boolean,
        force: Boolean
    ): Boolean {
        if (force) return true
        if (!isUsingCurrentLocation) return true
        if (!sameWalkingOrigin) return true

        val unresolvedStops = unresolvedRelevantWalkingMetricStops(response)
        if (unresolvedStops.isNotEmpty()) {
            Log.d(
                "AbfahrtWalk",
                "🚶 same-origin ORS refresh required unresolvedRelevant=${unresolvedStops.size} stops=${unresolvedStops.take(8).joinToString(" | ")}"
            )
            return true
        }

        return !hasWalkingMetrics(response)
    }

    private fun carryForwardWalkingMetrics(
        previous: DepartureResponse,
        incoming: DepartureResponse
    ): DepartureResponse {
        val metricsByStop = previous.departures
            .groupBy { it.stop }
            .mapNotNull { (stop, departures) ->
                val metrics = departures.firstOrNull { normalizedDistance(it.walkDistance) != null || it.usesApproximateDistance }
                metrics?.let { stop to it }
            }
            .toMap()

        if (metricsByStop.isEmpty()) return incoming
        var applied = 0
        val updatedDepartures = incoming.departures.map { dep ->
            val metrics = lookupStopValue(dep.stop, metricsByStop, dep.stationDistance) ?: return@map dep
            applied++
            dep.copy(
                walkDistance = metrics.walkDistance,
                walkDurationSeconds = metrics.walkDurationSeconds,
                usesApproximateDistance = metrics.usesApproximateDistance
            )
        }
        if (applied > 0) {
            Log.d("AbfahrtWalk", "♻️ carried forward ORS metrics applied=$applied incoming=${incoming.departures.size}")
        }
        return incoming.copy(departures = updatedDepartures)
    }

    private suspend fun awaitConnectivityForCoreRefresh(): Boolean {
        if (isNetworkReady()) {
            Log.d("AbfahrtNet", "📶 connectivity ready, proceeding with core refresh")
            return true
        }

        Log.d("AbfahrtNet", "📶 connectivity gate entered, postponing core refresh")
        Log.d("AbfahrtNet", "⏳ connectivity grace started ${CONNECTIVITY_GRACE_MS}ms")
        delay(CONNECTIVITY_GRACE_MS)

        if (isNetworkReady()) {
            Log.d("AbfahrtNet", "✅ connectivity recovered within grace window")
            return true
        }

        Log.d("AbfahrtNet", "🚫 core refresh postponed due to connectivity after grace")
        Log.d("AbfahrtNet", "📶 suppressing network error during grace/retry path")
        return false
    }


    private fun isTransientNetworkIssue(error: Exception): Boolean =
        error is SocketTimeoutException || error is UnknownHostException || error is IOException

    private fun isNetworkReady(): Boolean {
        return try {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } catch (e: SecurityException) {
            Log.w("AbfahrtNet", "⚠️ ACCESS_NETWORK_STATE missing, skipping connectivity gate", e)
            true
        }
    }

    private fun shouldUseLayeredLoading(lat: Double, lon: Double, radius: Int): Boolean {
        if (!isUsingCurrentLocation) return false
        val snapshot = lastRequestSnapshot ?: return true
        val radiusExpanded = radius > snapshot.radius
        return radiusExpanded
    }

    private fun buildRequestSignature(lat: Double, lon: Double, radius: Int, prefs: AppPreferences): String =
        listOf(
            String.format(java.util.Locale.ROOT, "%.5f", lat),
            String.format(java.util.Locale.ROOT, "%.5f", lon),
            radius.toString(),
            prefs.windowStartMinutes.toString(),
            prefs.windowEndMinutes.toString(),
            prefs.selectedModes.map { it.apiValue }.sorted().joinToString(",")
        ).joinToString("|")


    
    private fun mergeResponse(
        previous: DepartureResponse?,
        incoming: DepartureResponse,
        matchWindowMinutes: Int,
        preservePrevious: Boolean
    ): DepartureResponse = DepartureStableMerger.mergeResponse(
        previous = previous,
        incoming = incoming,
        matchWindowMinutes = matchWindowMinutes,
        preservePrevious = preservePrevious
    )

    private fun applyFilters(response: DepartureResponse, prefs: AppPreferences): List<Departure> {
        val departures = response.departures
        val now = System.currentTimeMillis()
        val startMs = prefs.windowStartMinutes * 60_000L
        val endMs = prefs.windowEndMinutes * 60_000L
        val modeValues = prefs.selectedModes.map { it.apiValue.lowercase() }
        val effectiveRadius = currentRequestRadius()

        val radiusOk = if (isUsingCurrentLocation) {
            departures.filter { dep -> dep.isHereOverride() || effectiveDistanceForFilter(dep) <= effectiveRadius }
        } else {
            departures
        }
        val candidateDepartures = if (!isUsingCurrentLocation) {
            departures
        } else {
            radiusOk
        }
        val modeOk = candidateDepartures.filter { dep ->
            val depMode = dep.mode?.trim()?.lowercase()
            val inferred = if (depMode.isNullOrBlank())
                inferModeFromLine(dep.line)?.apiValue?.lowercase() else null
            val effective = depMode?.takeIf { it.isNotBlank() } ?: inferred
            effective == null || effective in modeValues
        }

        val timeOk = modeOk.filter { dep ->
            val diff = dep.timestamp - now
            diff in startMs..endMs
        }

        val reachableOk = if (isUsingCurrentLocation && prefs.hideUnreachableDepartures) {
            timeOk.filter { dep ->
                val routeDurationSeconds = dep.walkDurationSeconds
                if (routeDurationSeconds == null) {
                    true
                } else {
                    val etaSeconds = ((dep.timestamp - now) / 1000L).coerceAtLeast(0L)
                    etaSeconds >= (routeDurationSeconds + 30).toLong()
                }
            }
        } else {
            timeOk
        }

        val targetToNearerStopFiltered = applyTargetToNearerStopFilter(reachableOk)

        debugLogDistanceStatus(targetToNearerStopFiltered)

        val nearestDistPerLineDirection = targetToNearerStopFiltered
            .groupBy { nearestDedupKey(it) }
            .mapValues { (_, deps) -> deps.minOfOrNull { effectiveDistanceForSort(it) } ?: Int.MAX_VALUE }

        debugLogDedupDecisions(targetToNearerStopFiltered, nearestDistPerLineDirection)

        val atNearest = targetToNearerStopFiltered.filter { dep ->
            val nearestDist = nearestDistPerLineDirection[nearestDedupKey(dep)] ?: return@filter false
            effectiveDistanceForSort(dep) <= nearestDist + 20
        }

        val exactSeen = mutableSetOf<String>()
        val unique = atNearest.filter { dep ->
            val roundedMinute = dep.timestamp / 60_000L
            exactSeen.add("${nearestDedupKey(dep)}_$roundedMinute")
        }

        val countPerDir = mutableMapOf<String, Int>()
        val limited = unique
            .sortedBy { it.timestamp }
            .filter { dep ->
                val key = nearestDedupKey(dep)
                val count = countPerDir.getOrDefault(key, 0)
                (count < prefs.maxPerDirection).also { fits ->
                    if (fits) countPerDir[key] = count + 1
                }
            }

        val result = limited.sortedWith(departureDisplayComparator(prefs.departureSortProfile))

        if (BuildConfig.DEBUG) {
            Log.d(
                "AbfahrtFilter",
                "raw=${departures.size} → radiusOk=${radiusOk.size} → candidate=${candidateDepartures.size} → modeOk=${modeOk.size} → timeOk=${timeOk.size} → reachableOk=${reachableOk.size} → targetStopOk=${targetToNearerStopFiltered.size} → atNearest=${atNearest.size} → unique=${unique.size} → limited=${limited.size} | requestRadius=$effectiveRadius window=[+${prefs.windowStartMinutes}..+${prefs.windowEndMinutes}min] maxPerDir=${prefs.maxPerDirection} sort=${prefs.departureSortProfile} hideUnreachable=${prefs.hideUnreachableDepartures && isUsingCurrentLocation}"
            )
        }
        return result
    }


    private fun effectiveDistanceForFilter(departure: Departure): Int =
        effectiveDistanceOrNull(departure) ?: Int.MAX_VALUE

    @SuppressLint("MissingPermission")
    private suspend fun resolveSearchDistanceReference(): Pair<Double, Double>? {
        if (isUsingCurrentLocation) return null
        return try {
            val location = getBestLocation() ?: return null
            if (location.latitude == 0.0 && location.longitude == 0.0) {
                null
            } else {
                lastLat = location.latitude
                lastLon = location.longitude
                cachedLastLat = location.latitude
                cachedLastLon = location.longitude
                location.latitude to location.longitude
            }
        } catch (e: Exception) {
            Log.d("AbfahrtSearch", "📍 search-distance reference unavailable: ${e.javaClass.simpleName}")
            null
        }
    }

    private fun rebaseSearchResponseDistances(
        response: DepartureResponse,
        referenceLat: Double,
        referenceLon: Double
    ): DepartureResponse {
        if (isUsingCurrentLocation) return response

        val target = _searchUiState.value.selectedTarget as? SearchTarget.Station
            ?: return response

        fun normalizeLookupName(value: String): String =
            normalizeStationOrDirectionName(value)

        val distanceByStop = linkedMapOf<String, Int>()

        response.stations.orEmpty().forEach { station ->
            val lat = station.lat ?: return@forEach
            val lon = station.lon ?: return@forEach
            val distance = distanceMeters(referenceLat, referenceLon, lat, lon).toInt()
            distanceByStop[normalizeLookupName(station.name)] = distance
        }

        distanceByStop.putIfAbsent(
            normalizeLookupName(target.name),
            distanceMeters(referenceLat, referenceLon, target.lat, target.lon).toInt()
        )

        val updatedStations = response.stations?.map { station ->
            val normalizedName = normalizeLookupName(station.name)
            val rebasedDistance = distanceByStop[normalizedName] ?: station.distance
            station.copy(distance = rebasedDistance)
        }

        val updatedDepartures = response.departures.map { departure ->
            val normalizedStop = normalizeLookupName(departure.stop)
            val rebasedDistance = distanceByStop[normalizedStop]
            if (rebasedDistance != null && rebasedDistance > 0) {
                departure.copy(stationDistance = rebasedDistance)
            } else {
                departure
            }
        }

        if (BuildConfig.DEBUG) {
            val targetDistance = distanceByStop[normalizeLookupName(target.name)]
            Log.d(
                "AbfahrtSearch",
                "📍 rebased search distances from current location to target='${target.name}' targetDistance=${targetDistance ?: "unknown"}m stations=${updatedStations?.size ?: 0} departures=${updatedDepartures.size}"
            )
        }

        return response.copy(
            departures = updatedDepartures,
            stations = updatedStations
        )
    }

    private fun applyTargetToNearerStopFilter(departures: List<Departure>): List<Departure> {
        if (departures.size < 2) return departures

        val distances = departures.map { effectiveDistanceOrNull(it) }
        val normalizedStops = departures.map { normalizeStationOrDirectionName(it.stop) }
        val normalizedDirections = departures.map { normalizeStationOrDirectionName(it.direction) }
        var comparisons = 0
        var matches = 0
        var hidden = 0
        var terminalSelfHidden = 0

        val filtered = departures.filterIndexed { departureIndex, departure ->
            val departureDistance = distances[departureIndex] ?: return@filterIndexed true
            val departureStopNormalized = normalizedStops[departureIndex]
            val directionPointsToOwnStop = normalizedDirectionPointsToStation(
                normalizedDirection = normalizedDirections[departureIndex],
                normalizedStation = departureStopNormalized
            )
            if (directionPointsToOwnStop) {
                hidden++
                terminalSelfHidden++
                if (BuildConfig.DEBUG) {
                    Log.d(
                        "AbfahrtTargetFilter",
                        "↩️ hidden terminal stop='${departure.stop}' dir='${departure.direction}' depDist=${departureDistance}m reason=direction_equals_stop"
                    )
                }
                return@filterIndexed false
            }

            var bestCandidate: Departure? = null
            var bestDistance = Int.MAX_VALUE

            for (candidateIndex in departures.indices) {
                if (candidateIndex == departureIndex) continue
                val candidateDistance = distances[candidateIndex] ?: continue
                if (candidateDistance >= departureDistance || candidateDistance >= bestDistance) continue

                val candidateStopNormalized = normalizedStops[candidateIndex]
                if (candidateStopNormalized.isBlank() || candidateStopNormalized == departureStopNormalized) continue

                comparisons++
                val candidateMatches = normalizedDirectionPointsToStation(
                    normalizedDirection = normalizedDirections[departureIndex],
                    normalizedStation = candidateStopNormalized
                )
                if (!candidateMatches) continue

                matches++
                bestCandidate = departures[candidateIndex]
                bestDistance = candidateDistance
            }

            val keep = bestCandidate == null
            if (!keep) {
                hidden++
                if (BuildConfig.DEBUG) {
                    Log.d(
                        "AbfahrtTargetFilter",
                        "↩️ hidden stop='${departure.stop}' dir='${departure.direction}' nearer='${bestCandidate.stop}' depDist=${departureDistance}m nearerDist=${bestDistance}m"
                    )
                }
            }
            keep
        }

        if (BuildConfig.DEBUG) {
            Log.d(
                "AbfahrtTargetFilter",
                "summary input=${departures.size} output=${filtered.size} hidden=$hidden terminalSelf=$terminalSelfHidden comparisons=$comparisons matches=$matches"
            )
        }

        return filtered
    }

    private fun normalizedDirectionPointsToStation(
        normalizedDirection: String,
        normalizedStation: String
    ): Boolean {
        if (normalizedDirection.isBlank() || normalizedStation.isBlank()) return false

        return normalizedDirection.contains(normalizedStation) ||
            normalizedStation.contains(normalizedDirection)
    }

    private fun normalizeStationOrDirectionName(value: String): String =
        StationNameNormalizer.filterLookupName(value)

    private fun effectiveDistanceForSort(departure: Departure): Int =
        DepartureDisplayOrdering.effectiveDistanceForSort(departure)

    private fun departureDisplayComparator(
        profile: DepartureSortProfile = DepartureSortProfile.NEARBY
    ): Comparator<Departure> =
        DepartureDisplayOrdering.comparator(profile)

    private fun effectiveDistanceOrNull(departure: Departure): Int? =
        DepartureDisplayOrdering.effectiveDistanceOrNull(departure)

    private fun normalizedDistance(distance: Int?): Int? =
        DepartureDisplayOrdering.normalizedDistance(distance)

    private fun nearestDedupKey(departure: Departure): String =
        DepartureServiceIdentity.lineDirectionKey(departure)

    private fun debugLogDistanceStatus(departures: List<Departure>) {
        if (!BuildConfig.DEBUG || departures.isEmpty()) return

        val summary = departures
            .groupingBy { distanceStatus(it) }
            .eachCount()
            .entries
            .sortedBy { it.key }
            .joinToString { "${it.key}=${it.value}" }

        Log.d("AbfahrtDistanceDebug", "statusSummary[$summary] candidates=${departures.size}")

        departures
            .sortedWith(departureDisplayComparator())
            .take(12)
            .forEach { dep ->
                Log.d(
                    "AbfahrtDistanceDebug",
                    "candidate key=${nearestDedupKey(dep)} stop=${dep.stop} line=${dep.line} dir=${dep.direction} dist=${formatDistanceDebug(dep)} status=${distanceStatus(dep)} etaMin=${((dep.timestamp - System.currentTimeMillis()) / 60_000L)} approx=${dep.usesApproximateDistance}"
                )
            }
    }

    private fun debugLogDedupDecisions(
        departures: List<Departure>,
        nearestDistPerLineDirection: Map<String, Int>
    ) {
        if (!BuildConfig.DEBUG || departures.isEmpty()) return

        departures
            .groupBy { nearestDedupKey(it) }
            .toSortedMap()
            .forEach { (key, deps) ->
                if (deps.size < 2) return@forEach

                val nearestDist = nearestDistPerLineDirection[key] ?: Int.MAX_VALUE
                val threshold = if (nearestDist == Int.MAX_VALUE) Int.MAX_VALUE else nearestDist + 20

                deps.sortedWith(departureDisplayComparator())
                    .forEach { dep ->
                        val effectiveDistance = effectiveDistanceForSort(dep)
                        val winner = effectiveDistance <= threshold
                        val reason = when {
                            nearestDist == Int.MAX_VALUE -> "all_unknown_distance"
                            winner -> "within_nearest_window"
                            else -> "farther_than_nearest"
                        }

                        Log.d(
                            "AbfahrtDedupDebug",
                            "decision=${if (winner) "WIN" else "LOSE"} key=$key stop=${dep.stop} line=${dep.line} dir=${dep.direction} dist=${formatDistanceDebug(dep)} nearest=${if (nearestDist == Int.MAX_VALUE) "unknown" else "${nearestDist}m"} threshold=${if (threshold == Int.MAX_VALUE) "unknown" else "${threshold}m"} status=${distanceStatus(dep)} reason=$reason etaMin=${((dep.timestamp - System.currentTimeMillis()) / 60_000L)}"
                        )
                    }
            }
    }

    private fun distanceStatus(departure: Departure): String {
        val stationDistance = normalizedDistance(departure.stationDistance)
        val walkDistance = normalizedDistance(departure.walkDistance)
        return when {
            departure.isHereOverride() -> "HERE"
            isUsingCurrentLocation && walkDistance != null && departure.usesApproximateDistance -> "APPROXIMATE"
            isUsingCurrentLocation && walkDistance != null -> "FINAL"
            stationDistance != null -> "STATION"
            else -> "UNKNOWN"
        }
    }

    private fun formatDistanceDebug(departure: Departure): String {
        val effective = effectiveDistanceOrNull(departure)?.let { "${it}m" } ?: "unknown"
        val station = normalizedDistance(departure.stationDistance)?.let { "${it}m" } ?: "unknown"
        val walk = normalizedDistance(departure.walkDistance)?.let { "${it}m" } ?: "unknown"
        return "effective=$effective station=$station walk=$walk here=${departure.isHereOverride()}"
    }

    private fun launchWalkingEnrichment(
        response: DepartureResponse,
        originLat: Double,
        originLon: Double,
        requestGeneration: Long,
        requestSignature: String
    ) {
        val prefs = preferences.value
        if (prefs.orsApiKey.isBlank()) return
        val isForegrounded = ProcessLifecycleOwner.get()
            .lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
        if (!isForegrounded) {
            Log.d("AbfahrtWalk", "⏸️ skipping async ORS enrichment while app is not visible")
            return
        }

        walkingEnrichmentJob?.cancel()
        walkingEnrichmentJob = viewModelScope.launch {
            try {
                Log.d("AbfahrtWalk", "🚦 launching async ORS enrichment after core apply signature=$requestSignature")
                val enrichedResponse = withContext(Dispatchers.Default) {
                    maybeEnrichWalkingMetrics(
                        response = response,
                        originLat = originLat,
                        originLon = originLon,
                        requestGeneration = requestGeneration
                    )
                }
                val stillCurrent = when (val target = _searchUiState.value.selectedTarget) {
                    is SearchTarget.CurrentLocation -> isCurrentTargetRequest(originLat, originLon, requestGeneration)
                    is SearchTarget.Station -> isCurrentTargetRequest(target.lat, target.lon, requestGeneration)
                }
                if (!stillCurrent) {
                    Log.d("AbfahrtTarget", "🚫 Dropping stale async ORS enrichment generation=$requestGeneration active=$activeTargetGeneration")
                    return@launch
                }
                val isStillForegrounded = ProcessLifecycleOwner.get()
                    .lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
                if (!isStillForegrounded) {
                    Log.d("AbfahrtWalk", "⏸️ dropping async ORS result because app is no longer visible")
                    return@launch
                }
                if (enrichedResponse == response) return@launch

                val currentState = _uiState.value as? DepartureUiState.Success ?: return@launch
                val (patchedResponse, patchedFiltered) = withContext(Dispatchers.Default) {
                    val patchedResponse = overlayWalkingMetrics(
                        base = currentState.response,
                        enriched = enrichedResponse
                    )
                    patchedResponse to applyFilters(patchedResponse, preferences.value)
                }
                val patchedState = currentState.copy(
                    response = patchedResponse,
                    filtered = patchedFiltered
                )
                _uiState.value = patchedState
                cachedUiState = patchedState
                Log.d("AbfahrtWalk", "✅ async ORS enrichment applied and resorted visible departures")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w("AbfahrtWalk", "⚠️ async ORS enrichment failed", e)
            }
        }
    }

    private fun overlayWalkingMetrics(
        base: DepartureResponse,
        enriched: DepartureResponse
    ): DepartureResponse {
        val metricsByStop = enriched.departures
            .groupBy { it.stop }
            .mapNotNull { (stop, deps) ->
                val metricDeparture = deps.firstOrNull { normalizedDistance(it.walkDistance) != null || it.usesApproximateDistance }
                metricDeparture?.let { stop to it }
            }
            .toMap()

        if (metricsByStop.isEmpty()) return base

        val updatedDepartures = base.departures.map { dep ->
            val metrics = lookupStopValue(dep.stop, metricsByStop, dep.stationDistance) ?: return@map dep
            dep.copy(
                walkDistance = metrics.walkDistance,
                walkDurationSeconds = metrics.walkDurationSeconds,
                usesApproximateDistance = metrics.usesApproximateDistance
            )
        }
        return base.copy(departures = updatedDepartures)
    }

    private suspend fun maybeEnrichWalkingMetricsForSearchTarget(
        response: DepartureResponse,
        originLat: Double,
        originLon: Double,
        requestGeneration: Long
    ): DepartureResponse {
        if (response.departures.isEmpty()) return response

        val target = _searchUiState.value.selectedTarget as? SearchTarget.Station ?: return response
        val reference = resolveSearchDistanceReference() ?: run {
            Log.d("AbfahrtWalk", "📍 search walking skipped: no current location reference")
            return response
        }

        if (!isCurrentTargetRequest(target.lat, target.lon, requestGeneration)) {
            Log.d(
                "AbfahrtTarget",
                "🚫 Ignoring stale search walking generation=$requestGeneration active=$activeTargetGeneration target=${target.name} lat=${target.lat} lon=${target.lon}"
            )
            return response
        }

        val (startLat, startLon) = reference
        val targetDistance = distanceMeters(startLat, startLon, target.lat, target.lon).toInt()
        val matrixRequest = listOf(
            WalkingRouteRepository.MatrixStopRequest(
                stationId = target.name,
                stationName = target.name,
                endLat = target.lat,
                endLon = target.lon,
                apiDistanceMeters = targetDistance,
                lookupKey = stationCoordinateKey(target.lat, target.lon)
            )
        )

        val routeFromMatrix = runCatching {
            walkingRouteRepository.getWalkingRoutesMatrix(
                startLat = startLat,
                startLon = startLon,
                stops = matrixRequest
            )
        }.getOrElse { error ->
            if (error is UnknownHostException || error is SocketTimeoutException || error is IOException) {
                Log.w("AbfahrtWalk", "📶 transient connectivity issue during search-target ORS matrix, keeping existing metrics")
                return response
            }
            Log.w("AbfahrtWalk", "⚠️ search-target ORS matrix unavailable, falling back to directions")
            emptyMap()
        }[stationCoordinateKey(target.lat, target.lon)]

        val route = routeFromMatrix ?: runCatching {
            walkingRouteRepository.getWalkingRoute(
                startLat = startLat,
                startLon = startLon,
                endLat = target.lat,
                endLon = target.lon,
                stationId = target.name
            )
        }.getOrNull() ?: return response

        val updatedDepartures = response.departures.map { dep ->
            dep.copy(
                walkDistance = route.distanceMeters,
                walkDurationSeconds = route.durationSeconds,
                usesApproximateDistance = false
            )
        }

        Log.d(
            "AbfahrtWalk",
            "🚶 search-target metrics applied target=${target.name} departures=${updatedDepartures.size} distance=${route.distanceMeters} duration=${route.durationSeconds}"
        )
        return response.copy(departures = updatedDepartures)
    }

    private suspend fun maybeEnrichWalkingMetrics(
        response: DepartureResponse,
        originLat: Double,
        originLon: Double,
        requestGeneration: Long
    ): DepartureResponse {
        val responseSignature = buildWalkingResponseSignature(response, originLat, originLon)
        if (lastWalkingResponseSignature == responseSignature) {
            val unresolvedStops = unresolvedRelevantWalkingMetricStops(response)
            if (unresolvedStops.isEmpty()) {
                Log.d("AbfahrtWalk", "⏭️ skip walking queue duplicate signature=$responseSignature")
                return response
            }
            Log.d(
                "AbfahrtWalk",
                "🔁 duplicate walking signature but unresolved relevant stops remain count=${unresolvedStops.size}; retrying ORS enrichment"
            )
        }
        lastWalkingResponseSignature = responseSignature

        if (!isUsingCurrentLocation) {
            return maybeEnrichWalkingMetricsForSearchTarget(
                response = response,
                originLat = originLat,
                originLon = originLon,
                requestGeneration = requestGeneration
            )
        }

        val stationQueue = response.stations
            .orEmpty()
            .filter { it.lat != null && it.lon != null }
            .sortedBy { it.distance }
            .distinctBy { stationCoordinateKey(it.lat!!, it.lon!!) }
            .take(WALKING_MAX_STATIONS * WALKING_MAX_STATION_BATCHES)

        if (stationQueue.isEmpty()) return response

        val (cacheOriginLat, cacheOriginLon) = resolveWalkingOriginForCache(originLat, originLon)
        val walkingByStop = linkedMapOf<String, WalkingRouteInfo>()
        val approximateStops = linkedSetOf<String>()
        var resolvedStationCount = 0
        var approximateStationCount = 0
        var previousLoggedDistance: Int? = null
        val stationBatches = stationQueue.chunked(WALKING_MAX_STATIONS)

        stationBatches.forEachIndexed { batchIndex, stationBatch ->
            if (!isCurrentTargetRequest(originLat, originLon, requestGeneration)) {
                Log.d(
                    "AbfahrtTarget",
                    "🚫 Ignoring stale walking batch generation=$requestGeneration active=$activeTargetGeneration lat=$originLat lon=$originLon"
                )
                return response
            }

            val matrixRequests = stationBatch.map {
                WalkingRouteRepository.MatrixStopRequest(
                    stationId = it.id,
                    stationName = it.name,
                    endLat = it.lat!!,
                    endLon = it.lon!!,
                    apiDistanceMeters = it.distance,
                    lookupKey = stationCoordinateKey(it.lat, it.lon)
                )
            }

            Log.d(
                "AbfahrtWalk",
                "🚶 matrix batch ${batchIndex + 1}/${stationBatches.size} stops=${stationBatch.size} totalQueue=${stationQueue.size}"
            )

            val matrixResults = runCatching {
                walkingRouteRepository.getWalkingRoutesMatrix(
                    startLat = cacheOriginLat,
                    startLon = cacheOriginLon,
                    stops = matrixRequests
                )
            }.getOrElse { error ->
                if (error is UnknownHostException || error is SocketTimeoutException || error is IOException) {
                    Log.w("AbfahrtWalk", "📶 transient connectivity issue during ORS matrix, keeping existing metrics and postponing enrichment")
                    return response
                }
                Log.w("AbfahrtWalk", "⚠️ ORS matrix unavailable for batch ${batchIndex + 1}, falling back to per-stop directions")
                emptyMap()
            }

            stationBatch.forEachIndexed { indexInBatch, station ->
                if (!isCurrentTargetRequest(originLat, originLon, requestGeneration)) {
                    Log.d(
                        "AbfahrtTarget",
                        "🚫 Ignoring stale walking queue generation=$requestGeneration active=$activeTargetGeneration lat=$originLat lon=$originLon"
                    )
                    return response
                }

                val stationLookupKey = stationCoordinateKey(station.lat!!, station.lon!!)
                val globalIndex = batchIndex * WALKING_MAX_STATIONS + indexInBatch
                val routeFromMatrix = matrixResults[stationLookupKey]
                val route = routeFromMatrix ?: runCatching {
                    Log.d(
                        "AbfahrtWalk",
                        "🌍 ORS queue fallback ${globalIndex + 1}/${stationQueue.size} stop=${station.name} start=${cacheOriginLat},${cacheOriginLon}"
                    )
                    walkingRouteRepository.getWalkingRoute(
                        startLat = cacheOriginLat,
                        startLon = cacheOriginLon,
                        endLat = station.lat,
                        endLon = station.lon,
                        stationId = station.id
                    )
                }.getOrNull()

                if (route != null) {
                    resolvedStationCount++
                    addWalkingMetricKeys(walkingByStop, station, route)
                    val ascending = previousLoggedDistance == null || route.distanceMeters >= previousLoggedDistance!!
                    Log.d(
                        "AbfahrtWalk",
                        "✅ staged walk stop=${station.name} key=$stationLookupKey apiDistance=${station.distance} orsDistance=${route.distanceMeters} duration=${route.durationSeconds} ascending=$ascending staged=$resolvedStationCount/${stationQueue.size}"
                    )
                    previousLoggedDistance = route.distanceMeters
                } else {
                    approximateStationCount++
                    addApproximateStopKeys(approximateStops, station)
                    val ascending = previousLoggedDistance == null || station.distance >= previousLoggedDistance!!
                    Log.d("AbfahrtWalk", "✈️ fallback distance stop=${station.name} key=$stationLookupKey apiDistance=${station.distance} ascending=$ascending")
                    previousLoggedDistance = station.distance
                }

                if (routeFromMatrix == null && WALKING_REQUEST_DELAY_MS > 0 && globalIndex < stationQueue.lastIndex) delay(WALKING_REQUEST_DELAY_MS)
            }

            if (WALKING_REQUEST_DELAY_MS > 0 && batchIndex < stationBatches.lastIndex) delay(WALKING_REQUEST_DELAY_MS)
        }

        val unresolvedVisibleStops = collectUnresolvedVisibleStops(response, walkingByStop, approximateStops)
        if (unresolvedVisibleStops.isNotEmpty()) {
            approximateStops += unresolvedVisibleStops
            approximateStationCount += unresolvedVisibleStops.size
            Log.d(
                "AbfahrtWalk",
                "🧭 unresolved walk metrics fallbackToApprox count=${unresolvedVisibleStops.size} stops=${unresolvedVisibleStops.take(8).joinToString(" | ")}"
            )
        }

        if (walkingByStop.isEmpty() && approximateStops.isEmpty()) return response

        val updatedResponse = enrichResponseWithWalking(response, walkingByStop, approximateStops)
        Log.d(
            "AbfahrtWalk",
            "🚶 finalized walk metrics enrichedStops=$resolvedStationCount/${stationQueue.size} approximateStops=$approximateStationCount currentLocation=$isUsingCurrentLocation"
        )
        return updatedResponse
    }

    private fun enrichResponseWithWalking(
        response: DepartureResponse,
        walkingByStop: Map<String, WalkingRouteInfo>,
        approximateStops: Set<String>
    ): DepartureResponse {
        if (walkingByStop.isEmpty() && approximateStops.isEmpty()) return response
        val updatedDepartures = response.departures.map { departure ->
            val walking = lookupStopValue(departure.stop, walkingByStop, departure.stationDistance)
            when {
                walking != null -> departure.copy(
                    walkDistance = walking.distanceMeters,
                    walkDurationSeconds = walking.durationSeconds,
                    usesApproximateDistance = false
                )
                isApproximateStop(departure.stop, approximateStops, departure.stationDistance) -> departure.copy(
                    walkDistance = departure.stationDistance.takeIf { it > 0 },
                    walkDurationSeconds = null,
                    usesApproximateDistance = true
                )
                else -> departure
            }
        }
        return response.copy(departures = updatedDepartures)
    }

    private fun collectUnresolvedVisibleStops(
        response: DepartureResponse,
        walkingByStop: Map<String, WalkingRouteInfo>,
        approximateStops: Set<String>
    ): Set<String> {
        if (!isUsingCurrentLocation) return emptySet()
        return response.departures
            .asSequence()
            .filter { normalizedDistance(it.stationDistance) != null }
            .filter { lookupStopValue(it.stop, walkingByStop, it.stationDistance) == null }
            .filterNot { isApproximateStop(it.stop, approximateStops, it.stationDistance) }
            .map { it.stop }
            .toCollection(linkedSetOf<String>())
    }

    private fun <T> lookupStopValue(
        stopName: String,
        valuesByStop: Map<String, T>,
        stationDistance: Int? = null
    ): T? {
        valuesByStop[stopName]?.let { return it }
        stationDistanceLookupKey(stationDistance)?.let { key ->
            valuesByStop[key]?.let { return it }
        }
        val normalizedStop = normalizeStationOrDirectionName(stopName)
        if (normalizedStop.isBlank()) return null
        valuesByStop[normalizedStop]?.let { return it }
        return valuesByStop.entries.firstOrNull { (candidate, _) ->
            normalizedDirectionPointsToStation(
                normalizeStationOrDirectionName(candidate),
                normalizedStop
            )
        }?.value
    }

    private fun isApproximateStop(
        stopName: String,
        approximateStops: Set<String>,
        stationDistance: Int? = null
    ): Boolean {
        if (stopName in approximateStops) return true
        stationDistanceLookupKey(stationDistance)?.let { key ->
            if (key in approximateStops) return true
        }
        val normalizedStop = normalizeStationOrDirectionName(stopName)
        if (normalizedStop.isBlank()) return false
        return approximateStops.any { candidate ->
            normalizedDirectionPointsToStation(
                normalizeStationOrDirectionName(candidate),
                normalizedStop
            )
        }
    }

    private fun addWalkingMetricKeys(
        target: MutableMap<String, WalkingRouteInfo>,
        station: Station,
        route: WalkingRouteInfo
    ) {
        val lat = station.lat ?: return
        val lon = station.lon ?: return
        val keys = linkedSetOf<String>()
        keys += station.name
        keys += normalizeStationOrDirectionName(station.name)
        keys += stationCoordinateKey(lat, lon)
        stationDistanceLookupKey(station.distance)?.let { keys += it }
        station.id.takeIf { it.isNotBlank() }?.let { keys += it }
        keys.filter { it.isNotBlank() }.forEach { target.putIfAbsent(it, route) }
    }

    private fun addApproximateStopKeys(
        target: MutableSet<String>,
        station: Station
    ) {
        val lat = station.lat
        val lon = station.lon
        target += station.name
        target += normalizeStationOrDirectionName(station.name)
        if (lat != null && lon != null) target += stationCoordinateKey(lat, lon)
        stationDistanceLookupKey(station.distance)?.let { target += it }
        station.id.takeIf { it.isNotBlank() }?.let { target += it }
    }

    private fun stationCoordinateKey(lat: Double, lon: Double): String =
        "coord:" + String.format(java.util.Locale.US, "%.5f|%.5f", lat, lon)

    private fun stationDistanceLookupKey(distance: Int?): String? =
        normalizedDistance(distance)?.let { "distance:$it" }


    private fun buildWalkingResponseSignature(
        response: DepartureResponse,
        originLat: Double,
        originLon: Double
    ): String {
        val stationKey = response.stations
            .orEmpty()
            .filter { it.lat != null && it.lon != null }
            .sortedBy { it.id.ifBlank { it.name } }
            .joinToString(";") { it.id.ifBlank { it.name } }
        return listOf(
            String.format(java.util.Locale.US, "%.3f", originLat),
            String.format(java.util.Locale.US, "%.3f", originLon),
            stationKey
        ).joinToString("|")
    }

    private fun resolveWalkingOriginForCache(originLat: Double, originLon: Double): Pair<Double, Double> {
        if (!isUsingCurrentLocation) return originLat to originLon

        val anchorLat = walkingAnchorLat
        val anchorLon = walkingAnchorLon
        if (
            anchorLat == null ||
            anchorLon == null ||
            distanceMeters(anchorLat, anchorLon, originLat, originLon) >= CurrentLocationStartupPolicy.MOVEMENT_THRESHOLD_METERS
        ) {
            walkingAnchorLat = originLat
            walkingAnchorLon = originLon
            cachedWalkingAnchorLat = originLat
            cachedWalkingAnchorLon = originLon
            return originLat to originLon
        }
        return anchorLat to anchorLon
    }

    private fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val result = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, result)
        return result[0]
    }

    private fun clearDisplayedOrsMetrics() {
        val state = _uiState.value
        if (state !is DepartureUiState.Success) return

        val clearedResponse = state.response.copy(
            departures = state.response.departures.map { dep ->
                dep.copy(
                    walkDistance = null,
                    walkDurationSeconds = null,
                    usesApproximateDistance = false
                )
            }
        )
        val prefsSnapshot = preferences.value
        viewModelScope.launch {
            val clearedFiltered = withContext(Dispatchers.Default) { applyFilters(clearedResponse, prefsSnapshot) }
            val clearedState = state.copy(
                response = clearedResponse,
                filtered = clearedFiltered
            )
            _uiState.value = clearedState
            cachedUiState = clearedState
            lastWalkingResponseSignature = null
            Log.d("AbfahrtWalk", "♻️ Cleared displayed ORS metrics after travel mode change; waiting for next refresh")
        }
    }

    fun refilter() {
        val state = _uiState.value
        if (state !is DepartureUiState.Success) return

        val responseSnapshot = state.response
        val prefsSnapshot = preferences.value
        refilterJob?.cancel()
        refilterJob = viewModelScope.launch {
            val filtered = withContext(Dispatchers.Default) { applyFilters(responseSnapshot, prefsSnapshot) }
            val latest = _uiState.value as? DepartureUiState.Success ?: return@launch
            if (latest.response != responseSnapshot) {
                Log.d("AbfahrtFilter", "⏭️ Dropping stale refilter result because response changed")
                return@launch
            }
            val updatedState = latest.copy(filtered = filtered)
            _uiState.value = updatedState
            cachedUiState = updatedState
        }
    }

    private fun friendlySearchMessage(e: Exception): String = when (e) {
        is HttpException -> when (e.code()) {
            400 -> context.getString(R.string.search_error_http_400)
            401, 403 -> context.getString(R.string.search_error_http_403)
            404 -> context.getString(R.string.search_error_http_404)
            429 -> context.getString(R.string.search_error_http_429)
            else -> context.getString(R.string.search_error_http_other, e.code())
        }
        is UnknownHostException -> context.getString(R.string.search_error_unknown_host)
        is SocketTimeoutException -> context.getString(R.string.search_error_timeout)
        is SSLException -> context.getString(R.string.search_error_ssl)
        is IOException -> context.getString(R.string.search_error_network)
        else -> e.message?.takeIf { it.isNotBlank() } ?: context.getString(R.string.search_error_unknown)
    }

    private fun friendlyMessage(e: Exception): String = when (e) {
        is HttpException -> when (e.code()) {
            400 -> context.getString(R.string.error_http_400)
            401 -> context.getString(R.string.error_http_401)
            429 -> context.getString(R.string.error_http_429)
            503 -> context.getString(R.string.error_http_other, 503)
            else -> context.getString(R.string.error_http_other, e.code())
        }
        is java.net.SocketTimeoutException -> context.getString(R.string.error_server_timeout)
        is java.net.UnknownHostException -> context.getString(R.string.error_no_internet)
        is javax.net.ssl.SSLException -> context.getString(R.string.error_ssl)
        is IOException -> context.getString(R.string.error_network_generic)
        else -> e.message ?: context.getString(R.string.error_unknown)
    }

    fun saveApiKey(key: String) = viewModelScope.launch { prefsRepo.updateApiKey(key) }

    fun saveOrsApiKey(key: String) = viewModelScope.launch { prefsRepo.updateOrsApiKey(key) }
    fun saveOrsTravelMode(mode: OrsTravelMode) = viewModelScope.launch {
        val currentPrefs = preferences.value
        val previousMode = currentPrefs.orsTravelMode
        if (previousMode == mode) return@launch

        if (previousMode != OrsTravelMode.BIKE && mode == OrsTravelMode.BIKE) {
            val bumpedRadius = (currentPrefs.radius + 200).coerceAtMost(2000)
            if (bumpedRadius != currentPrefs.radius) {
                prefsRepo.updateRadius(bumpedRadius)
                Log.d(
                    "AbfahrtWalk",
                    "🚴 ORS mode switched to BIKE, bumping radius ${currentPrefs.radius}→$bumpedRadius for UI consistency"
                )
            }
        }

        prefsRepo.updateOrsTravelMode(mode)
        forceNextWalkingEnrichment = true
        lastWalkingResponseSignature = null
        Log.d("AbfahrtWalk", "🔄 ORS travel mode changed $previousMode→$mode, refreshing immediately")

        if (isLoadInProgress) {
            pendingTravelModeRefresh = true
            Log.d("AbfahrtRefresh", "⏳ Deferred travel-mode refresh while load is active")
            return@launch
        }

        _isRefreshing.value = true
        loadForCurrentTarget(force = true, hardReset = pendingHardResetRefresh)
    }

    fun completeOnboarding(apiKey: String, orsApiKey: String) = viewModelScope.launch {
        val normalizedApiKey = apiKey.trim()
        if (normalizedApiKey.isEmpty()) {
            Log.w("AbfahrtAuth", "onboarding completion rejected because abfahrt.now API key is blank")
            prefsRepo.setOnboardingCompleted(false)
            return@launch
        }
        val apiKeySaved = prefsRepo.updateApiKey(normalizedApiKey)
        if (!apiKeySaved) {
            Log.e("AbfahrtAuth", "onboarding completion deferred because required API key could not be stored")
            prefsRepo.setOnboardingCompleted(false)
            return@launch
        }
        prefsRepo.updateOrsApiKey(orsApiKey)
        prefsRepo.setOnboardingCompleted(true)
    }

    fun saveRadius(radius: Int) = viewModelScope.launch {
        val currentPrefs = preferences.value
        val oldRadius = currentPrefs.radius
        val newRadius = radius.coerceIn(100, 2000)
        prefsRepo.updateRadius(newRadius)

        if (isUsingCurrentLocation && newRadius <= oldRadius && isWithinThrottleWindow()) {
            Log.d("AbfahrtRefresh", "⏱️ Radius reduced $oldRadius→$newRadius, reusing fresh data locally")
            refilter()
        } else if (isUsingCurrentLocation) {
            refresh(force = false)
        } else {
            refilter()
        }
    }

    fun saveWindowRange(startMin: Int, endMin: Int) = viewModelScope.launch {
        val currentPrefs = preferences.value
        val oldStart = currentPrefs.windowStartMinutes
        val oldEnd = currentPrefs.windowEndMinutes
        val newStart = startMin.coerceIn(0, 115)
        val newEnd = endMin.coerceIn(newStart + 5, 120)

        prefsRepo.updateWindowRange(newStart, newEnd)

        val windowReducedOrSame = newStart >= oldStart && newEnd <= oldEnd
        if (windowReducedOrSame && isWithinThrottleWindow()) {
            Log.d("AbfahrtRefresh", "⏱️ Time window reduced [$oldStart,$oldEnd]→[$newStart,$newEnd], reusing fresh data locally")
            refilter()
        } else {
            Log.d("AbfahrtRefresh", "🔄 Time window expanded/stale [$oldStart,$oldEnd]→[$newStart,$newEnd], requesting fresh data")
            refresh(force = false)
        }
    }

    fun saveRefreshInterval(minutes: Int) = viewModelScope.launch { prefsRepo.updateRefreshInterval(minutes) }

    fun saveHideUnreachableDepartures(enabled: Boolean) = viewModelScope.launch {
        val currentPrefs = preferences.value
        if (enabled && currentPrefs.windowStartMinutes != 0) {
            prefsRepo.updateWindowRange(0, currentPrefs.windowEndMinutes)
        }
        prefsRepo.updateHideUnreachableDepartures(enabled)
    }
    fun saveMaxPerDirection(n: Int) = viewModelScope.launch { prefsRepo.updateMaxPerDirection(n) }

    fun saveDepartureSortProfile(profile: DepartureSortProfile) = viewModelScope.launch {
        prefsRepo.updateDepartureSortProfile(profile)
    }

    fun saveLanguage(lang: AppLanguage) {
        viewModelScope.launch {
            prefsRepo.updateLanguage(lang)
            val localeList = if (lang.code == "system") {
                androidx.core.os.LocaleListCompat.getEmptyLocaleList()
            } else {
                androidx.core.os.LocaleListCompat.forLanguageTags(lang.code)
            }
            withContext(Dispatchers.Main.immediate) {
                androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(localeList)
            }
        }
    }

    fun saveQuickSlots(slots: List<TransportMode>) = viewModelScope.launch { prefsRepo.updateQuickSlots(slots) }

    fun toggleMode(mode: TransportMode) = viewModelScope.launch {
        val current = preferences.value.selectedModes
        val updated = if (mode in current && current.size > 1) current - mode else current + mode
        prefsRepo.updateSelectedModes(updated)
    }

    override fun onCleared() {
        startupLocationCorrectionJob?.cancel()
        walkingEnrichmentJob?.cancel()
        refilterJob?.cancel()
        autoRefreshJob?.cancel()
        super.onCleared()
    }
}
