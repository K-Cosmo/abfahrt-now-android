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
import now.abfahrt.transit.data.model.SavedPlace
import now.abfahrt.transit.data.model.SavedPlaceType
import now.abfahrt.transit.data.model.SearchResult
import now.abfahrt.transit.data.preferences.UserPreferencesRepository
import now.abfahrt.transit.data.repository.GeocodingRepository
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.net.ssl.SSLException

data class SavedPlacesUiState(
    val home: SavedPlace? = null,
    val work: SavedPlace? = null,
    val editing: SavedPlaceType? = null,
    val search: PlaceSearchUiState = PlaceSearchUiState()
)

@HiltViewModel
class SavedPlacesViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val prefsRepo: UserPreferencesRepository,
    private val geocodingRepository: GeocodingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SavedPlacesUiState())
    val uiState: StateFlow<SavedPlacesUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var locationHint: Pair<Double, Double>? = null

    init {
        viewModelScope.launch {
            prefsRepo.preferencesFlow.collect { prefs ->
                _uiState.value = _uiState.value.copy(
                    home = prefs.homePlace,
                    work = prefs.workPlace
                )
            }
        }
    }

    fun updateLocationHint(location: Pair<Double, Double>?) {
        if (location != null) locationHint = location
    }

    fun edit(type: SavedPlaceType) {
        searchJob?.cancel()
        _uiState.value = _uiState.value.copy(
            editing = type,
            search = PlaceSearchUiState()
        )
    }

    fun cancelEditing() {
        searchJob?.cancel()
        _uiState.value = _uiState.value.copy(
            editing = null,
            search = PlaceSearchUiState()
        )
    }

    fun updateQuery(query: String) {
        searchJob?.cancel()
        _uiState.value = _uiState.value.copy(
            search = PlaceSearchUiState(query = query)
        )
        val trimmed = query.trim()
        if (trimmed.length < 3) return

        searchJob = viewModelScope.launch {
            delay(350)
            _uiState.value = _uiState.value.copy(
                search = _uiState.value.search.copy(isSearching = true, errorMessage = null)
            )
            try {
                val results = geocodingRepository.searchPlaces(
                    query = trimmed,
                    biasLat = locationHint?.first,
                    biasLon = locationHint?.second
                )
                _uiState.value = _uiState.value.copy(
                    search = _uiState.value.search.copy(
                        isSearching = false,
                        results = results,
                        errorMessage = null
                    )
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w("SavedPlaces", "Photon search failed: ${e::class.java.simpleName}")
                _uiState.value = _uiState.value.copy(
                    search = _uiState.value.search.copy(
                        isSearching = false,
                        results = emptyList(),
                        errorMessage = friendlySearchMessage(e)
                    )
                )
            }
        }
    }

    fun select(result: SearchResult) {
        val type = _uiState.value.editing ?: return
        searchJob?.cancel()
        viewModelScope.launch {
            prefsRepo.updateSavedPlace(
                type,
                SavedPlace(
                    title = result.title,
                    subtitle = result.subtitle,
                    lat = result.lat,
                    lon = result.lon
                )
            )
            _uiState.value = _uiState.value.copy(
                editing = null,
                search = PlaceSearchUiState()
            )
        }
    }

    fun remove(type: SavedPlaceType) {
        searchJob?.cancel()
        viewModelScope.launch {
            prefsRepo.updateSavedPlace(type, null)
            _uiState.value = _uiState.value.copy(
                editing = null,
                search = PlaceSearchUiState()
            )
        }
    }

    private fun friendlySearchMessage(e: Exception): String = when (e) {
        is SocketTimeoutException -> context.getString(R.string.route_search_error_timeout)
        is UnknownHostException -> context.getString(R.string.route_search_error_network)
        is SSLException -> context.getString(R.string.route_search_error_network)
        is IOException -> context.getString(R.string.route_search_error_network)
        is HttpException -> context.getString(R.string.route_search_error_http, e.code())
        else -> context.getString(R.string.route_search_error_unknown)
    }
}
