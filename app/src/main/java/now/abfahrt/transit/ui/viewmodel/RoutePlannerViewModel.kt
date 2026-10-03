package now.abfahrt.transit.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import now.abfahrt.transit.R
import now.abfahrt.transit.data.model.PlaceSearchUiState
import now.abfahrt.transit.data.model.RouteEndpoint
import now.abfahrt.transit.data.model.RoutePlannerUiState
import now.abfahrt.transit.data.model.SavedPlace
import now.abfahrt.transit.data.model.SearchResult
import now.abfahrt.transit.data.model.TripPlanningUiState
import now.abfahrt.transit.data.preferences.UserPreferencesRepository
import now.abfahrt.transit.data.repository.GeocodingRepository
import now.abfahrt.transit.data.repository.TransitRepository
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.net.ssl.SSLException

@HiltViewModel
class RoutePlannerViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val transitRepository: TransitRepository,
    private val geocodingRepository: GeocodingRepository,
    private val prefsRepo: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoutePlannerUiState())
    val uiState: StateFlow<RoutePlannerUiState> = _uiState.asStateFlow()

    private var currentLocationHint: Pair<Double, Double>? = null
    private var originSearchJob: Job? = null
    private var destinationSearchJob: Job? = null

    init {
        viewModelScope.launch {
            prefsRepo.preferencesFlow.collect { prefs ->
                _uiState.value = _uiState.value.copy(
                    homePlace = prefs.homePlace,
                    workPlace = prefs.workPlace
                )
            }
        }
    }

    fun prepareDestination(result: SearchResult, currentLocation: Pair<Double, Double>?) {
        prepareDestination(result.toRouteEndpoint(), currentLocation)
    }

    fun prepareDestination(place: SavedPlace, currentLocation: Pair<Double, Double>?) {
        prepareDestination(place.toRouteEndpoint(), currentLocation)
    }

    private fun prepareDestination(destination: RouteEndpoint.Place, currentLocation: Pair<Double, Double>?) {
        currentLocationHint = currentLocation ?: currentLocationHint
        destinationSearchJob?.cancel()
        _uiState.value = _uiState.value.copy(
            origin = RouteEndpoint.CurrentLocation,
            destination = destination,
            originSearch = PlaceSearchUiState(),
            destinationSearch = PlaceSearchUiState(),
            tripState = TripPlanningUiState.Idle
        )
    }

    fun updateCurrentLocationHint(location: Pair<Double, Double>?) {
        if (location != null) currentLocationHint = location
    }

    fun updateOriginQuery(query: String) {
        _uiState.value = _uiState.value.copy(
            originSearch = nextSearchState(_uiState.value.originSearch, query),
            tripState = TripPlanningUiState.Idle
        )
        originSearchJob?.cancel()
        originSearchJob = scheduleSearch(query, isOrigin = true)
    }

    fun updateDestinationQuery(query: String) {
        _uiState.value = _uiState.value.copy(
            destinationSearch = nextSearchState(_uiState.value.destinationSearch, query),
            tripState = TripPlanningUiState.Idle
        )
        destinationSearchJob?.cancel()
        destinationSearchJob = scheduleSearch(query, isOrigin = false)
    }

    fun clearOriginSearch() {
        originSearchJob?.cancel()
        _uiState.value = _uiState.value.copy(originSearch = PlaceSearchUiState())
    }

    fun clearDestinationSearch() {
        destinationSearchJob?.cancel()
        _uiState.value = _uiState.value.copy(destinationSearch = PlaceSearchUiState())
    }

    fun selectOrigin(result: SearchResult) {
        originSearchJob?.cancel()
        _uiState.value = _uiState.value.copy(
            origin = result.toRouteEndpoint(),
            originSearch = PlaceSearchUiState(),
            tripState = TripPlanningUiState.Idle
        )
    }

    fun selectDestination(result: SearchResult) {
        destinationSearchJob?.cancel()
        _uiState.value = _uiState.value.copy(
            destination = result.toRouteEndpoint(),
            destinationSearch = PlaceSearchUiState(),
            tripState = TripPlanningUiState.Idle
        )
    }

    fun useCurrentLocationAsOrigin() {
        originSearchJob?.cancel()
        _uiState.value = _uiState.value.copy(
            origin = RouteEndpoint.CurrentLocation,
            originSearch = PlaceSearchUiState(),
            tripState = TripPlanningUiState.Idle
        )
    }

    fun useCurrentLocationAsDestination() {
        destinationSearchJob?.cancel()
        _uiState.value = _uiState.value.copy(
            destination = RouteEndpoint.CurrentLocation,
            destinationSearch = PlaceSearchUiState(),
            tripState = TripPlanningUiState.Idle
        )
    }

    fun useSavedPlaceAsOrigin(place: SavedPlace) {
        originSearchJob?.cancel()
        _uiState.value = _uiState.value.copy(
            origin = place.toRouteEndpoint(),
            originSearch = PlaceSearchUiState(),
            tripState = TripPlanningUiState.Idle
        )
    }

    fun useSavedPlaceAsDestination(place: SavedPlace) {
        destinationSearchJob?.cancel()
        _uiState.value = _uiState.value.copy(
            destination = place.toRouteEndpoint(),
            destinationSearch = PlaceSearchUiState(),
            tripState = TripPlanningUiState.Idle
        )
    }

    fun swapEndpoints() {
        val state = _uiState.value
        val destination = state.destination ?: return
        _uiState.value = state.copy(
            origin = destination,
            destination = state.origin,
            originSearch = PlaceSearchUiState(),
            destinationSearch = PlaceSearchUiState(),
            tripState = TripPlanningUiState.Idle
        )
    }

    fun findRoutes() {
        val state = _uiState.value
        val destination = state.destination ?: run {
            _uiState.value = state.copy(
                tripState = TripPlanningUiState.Error(context.getString(R.string.route_error_destination_required))
            )
            return
        }

        val originCoordinates = resolveCoordinates(state.origin)
        val destinationCoordinates = resolveCoordinates(destination)
        if (originCoordinates == null || destinationCoordinates == null) {
            _uiState.value = state.copy(
                tripState = TripPlanningUiState.Error(context.getString(R.string.route_error_current_location_unavailable))
            )
            return
        }

        if (coordinatesAreEffectivelyEqual(originCoordinates, destinationCoordinates)) {
            _uiState.value = state.copy(
                tripState = TripPlanningUiState.Error(context.getString(R.string.route_error_same_endpoint))
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(tripState = TripPlanningUiState.Loading)
            try {
                val response = transitRepository.getTrips(
                    fromLat = originCoordinates.first,
                    fromLon = originCoordinates.second,
                    toLat = destinationCoordinates.first,
                    toLon = destinationCoordinates.second
                )
                val outcome = if (response.trips.isEmpty()) "empty" else "success"
                Log.d(
                    "AbfahrtTrips",
                    "outcome=$outcome region=${response.region.ifBlank { "<blank>" }} count=${response.trips.size} " +
                        "attribution=${!response.attribution.isNullOrBlank()}"
                )
                _uiState.value = _uiState.value.copy(tripState = TripPlanningUiState.Success(response))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val httpCode = (e as? HttpException)?.code()
                Log.w(
                    "AbfahrtTrips",
                    "outcome=error type=${e::class.java.simpleName} http=${httpCode ?: "-"}"
                )
                if (httpCode == 401) {
                    Log.w("AbfahrtAuth", "abfahrt.now rejected API key on /trips (401); returning to key entry")
                    prefsRepo.setOnboardingCompleted(false)
                }
                _uiState.value = _uiState.value.copy(
                    tripState = TripPlanningUiState.Error(friendlyTripMessage(e))
                )
            }
        }
    }

    private fun scheduleSearch(query: String, isOrigin: Boolean): Job? {
        val trimmed = query.trim()
        if (trimmed.length < 3) return null
        return viewModelScope.launch {
            delay(350)
            setSearching(isOrigin, true)
            try {
                val results = geocodingRepository.searchPlaces(
                    query = trimmed,
                    biasLat = currentLocationHint?.first,
                    biasLon = currentLocationHint?.second
                )
                Log.d("PhotonRoute", "field=${if (isOrigin) "origin" else "destination"} query='$trimmed' results=${results.size}")
                setSearchResult(isOrigin, results, null)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w("PhotonRoute", "search failed: ${e::class.java.simpleName}: ${e.message}")
                setSearchResult(isOrigin, emptyList(), friendlySearchMessage(e))
            }
        }
    }

    private fun setSearching(isOrigin: Boolean, searching: Boolean) {
        val state = _uiState.value
        _uiState.value = if (isOrigin) {
            state.copy(originSearch = state.originSearch.copy(isSearching = searching, errorMessage = null))
        } else {
            state.copy(destinationSearch = state.destinationSearch.copy(isSearching = searching, errorMessage = null))
        }
    }

    private fun setSearchResult(isOrigin: Boolean, results: List<SearchResult>, error: String?) {
        val state = _uiState.value
        _uiState.value = if (isOrigin) {
            state.copy(originSearch = state.originSearch.copy(isSearching = false, results = results, errorMessage = error))
        } else {
            state.copy(destinationSearch = state.destinationSearch.copy(isSearching = false, results = results, errorMessage = error))
        }
    }

    private fun nextSearchState(previous: PlaceSearchUiState, query: String): PlaceSearchUiState =
        previous.copy(
            query = query,
            isSearching = false,
            results = if (query.trim().length < 3) emptyList() else previous.results,
            errorMessage = null
        )

    private fun resolveCoordinates(endpoint: RouteEndpoint): Pair<Double, Double>? = when (endpoint) {
        RouteEndpoint.CurrentLocation -> currentLocationHint
        is RouteEndpoint.Place -> endpoint.lat to endpoint.lon
    }

    private fun friendlySearchMessage(e: Exception): String = when (e) {
        is SocketTimeoutException -> context.getString(R.string.route_search_error_timeout)
        is UnknownHostException -> context.getString(R.string.route_search_error_network)
        is SSLException -> context.getString(R.string.route_search_error_network)
        is IOException -> context.getString(R.string.route_search_error_network)
        is HttpException -> context.getString(R.string.route_search_error_http, e.code())
        else -> context.getString(R.string.route_search_error_unknown)
    }

    private fun friendlyTripMessage(e: Exception): String = when (e) {
        is HttpException -> when (e.code()) {
            400 -> context.getString(R.string.route_error_http_400)
            401 -> context.getString(R.string.error_http_401)
            429 -> context.getString(R.string.error_http_429)
            else -> context.getString(R.string.route_error_http, e.code())
        }
        is SocketTimeoutException -> context.getString(R.string.error_server_timeout)
        is UnknownHostException -> context.getString(R.string.error_network_generic)
        is SSLException -> context.getString(R.string.error_ssl)
        is IOException -> context.getString(R.string.error_network_generic)
        else -> context.getString(R.string.route_error_unknown)
    }
}

private fun SearchResult.toRouteEndpoint(): RouteEndpoint.Place = RouteEndpoint.Place(
    title = title,
    subtitle = subtitle,
    lat = lat,
    lon = lon
)

private fun SavedPlace.toRouteEndpoint(): RouteEndpoint.Place = RouteEndpoint.Place(
    title = title,
    subtitle = subtitle,
    lat = lat,
    lon = lon
)

private fun coordinatesAreEffectivelyEqual(
    first: Pair<Double, Double>,
    second: Pair<Double, Double>
): Boolean =
    kotlin.math.abs(first.first - second.first) < 0.00001 &&
        kotlin.math.abs(first.second - second.second) < 0.00001
