package now.abfahrt.transit.data.model

// ─── API Response Models ──────────────────────────────────────────────────────

data class DepartureResponse(
    val region: String,
    val city: String? = null,
    val realtime: Boolean,
    val routing: Boolean = false,
    val departures: List<Departure> = emptyList(),
    val stations: List<Station>? = null,
    val attribution: String? = null,
    val effectiveRadius: Int? = null,
    val isFinal: Boolean = true
)

data class Departure(
    val line: String,
    val direction: String,
    val time: String,
    val timestamp: Long,
    val stop: String,
    val platform: String? = null,
    val delay: Int = 0,
    val cancelled: Boolean = false,
    val occupancy: String? = null,
    val mode: String? = null,
    val stationDistance: Int = 0,
    val walkDistance: Int? = null,
    val walkDurationSeconds: Int? = null,
    val usesApproximateDistance: Boolean = false,
    /**
     * Raw provider stop-point ID when Departure.stop originally contained an ID
     * instead of a display name. Kept separately so UI enrichment can replace
     * stop with Station.name without losing platform/stop-point metadata.
     */
    val providerStopId: String? = null
)


const val HERE_DISTANCE_THRESHOLD_METERS: Int = 35

fun Departure.isHereOverride(): Boolean =
    stationDistance in 1..HERE_DISTANCE_THRESHOLD_METERS

fun Departure.displayDistanceMeters(): Int? =
    if (isHereOverride()) 0 else (walkDistance?.takeIf { it > 0 } ?: stationDistance.takeIf { it > 0 })

fun Departure.displayWalkDurationSeconds(): Int? =
    if (isHereOverride()) 0 else walkDurationSeconds

data class Station(
    val id: String,
    val name: String,
    val distance: Int,
    val hasRail: Boolean,
    val lat: Double? = null,
    val lon: Double? = null,
    /** Optional provider-side OSM walking time from the query origin, in seconds. */
    val walkSeconds: Int? = null
)

data class TripResponse(
    val region: String,
    val trips: List<Trip> = emptyList(),
    val attribution: String? = null,
    val effectiveRadius: Int? = null
)

data class Trip(
    val legs: List<TripLeg>,
    val departure: Long,
    val arrival: Long,
    val duration: Int,
    val changes: Int
)

data class TripLeg(
    val line: String,
    val direction: String,
    val mode: String? = null,
    val from: String,
    val to: String,
    val departure: Long,
    val arrival: Long,
    val departureDelay: Int = 0,
    val arrivalDelay: Int = 0,
    val departurePlatform: String? = null,
    val cancelled: Boolean = false,
    val stops: Int = 0,
    val stopNames: List<String>? = null,
    val intermediateStops: List<TripIntermediateStop>? = null,
    val sameVehicle: Boolean = false
)

data class TripIntermediateStop(
    val name: String,
    val arrival: Long? = null
)

// ─── Transport Mode Enum ──────────────────────────────────────────────────────

enum class TransportMode(
    val apiValue: String,
    val label: String,   // German fallback label
    val emoji: String,
    val labelRes: Int    // Localized string resource
) {
    SUBWAY(   "subway",   "U-Bahn",   "🚇", now.abfahrt.transit.R.string.mode_subway),
    SUBURBAN( "suburban", "S-Bahn",   "🚆", now.abfahrt.transit.R.string.mode_suburban),
    TRAM(     "tram",     "Tram",     "🚊", now.abfahrt.transit.R.string.mode_tram),
    BUS(      "bus",      "Bus",      "🚌", now.abfahrt.transit.R.string.mode_bus),
    REGIONAL( "regional", "Regional", "🚂", now.abfahrt.transit.R.string.mode_regional),
    EXPRESS(  "express",  "Express",  "🚄", now.abfahrt.transit.R.string.mode_express),
    FERRY(    "ferry",    "Fähre",    "⛴️", now.abfahrt.transit.R.string.mode_ferry);

    companion object {
        fun fromApiValue(value: String?): TransportMode? =
            entries.firstOrNull { it.apiValue == value }

        val all: Set<TransportMode> get() = entries.toSet()
    }
}



sealed interface SearchTarget {
    data object CurrentLocation : SearchTarget
    data class Station(
        val name: String,
        val subtitle: String = "",
        val lat: Double,
        val lon: Double
    ) : SearchTarget
}

data class SearchResult(
    val id: String,
    val title: String,
    val subtitle: String = "",
    val typeLabel: String = "",
    val lat: Double,
    val lon: Double,
    val matchQuality: Int = 0
)


data class SavedPlace(
    val title: String,
    val subtitle: String = "",
    val lat: Double,
    val lon: Double
)

enum class SavedPlaceType {
    HOME,
    WORK
}

data class SearchUiState(
    val query: String = "",
    val isSearching: Boolean = false,
    val results: List<SearchResult> = emptyList(),
    val selectedTarget: SearchTarget = SearchTarget.CurrentLocation,
    val errorMessage: String? = null
)

sealed interface RouteEndpoint {
    data object CurrentLocation : RouteEndpoint
    data class Place(
        val title: String,
        val subtitle: String = "",
        val lat: Double,
        val lon: Double
    ) : RouteEndpoint
}

data class PlaceSearchUiState(
    val query: String = "",
    val isSearching: Boolean = false,
    val results: List<SearchResult> = emptyList(),
    val errorMessage: String? = null
)

sealed interface TripPlanningUiState {
    data object Idle : TripPlanningUiState
    data object Loading : TripPlanningUiState
    data class Success(val response: TripResponse) : TripPlanningUiState
    data class Error(val message: String) : TripPlanningUiState
}

data class RoutePlannerUiState(
    val origin: RouteEndpoint = RouteEndpoint.CurrentLocation,
    val destination: RouteEndpoint? = null,
    val originSearch: PlaceSearchUiState = PlaceSearchUiState(),
    val destinationSearch: PlaceSearchUiState = PlaceSearchUiState(),
    val homePlace: SavedPlace? = null,
    val workPlace: SavedPlace? = null,
    val tripState: TripPlanningUiState = TripPlanningUiState.Idle
)

// ─── UI State ─────────────────────────────────────────────────────────────────

sealed interface DepartureUiState {
    data object Idle    : DepartureUiState
    data object Loading : DepartureUiState
    data class  Success(
        val response: DepartureResponse,
        val filtered: List<Departure>
    ) : DepartureUiState
    data class  Error(val message: String) : DepartureUiState
}

// ─── User Preferences Model ───────────────────────────────────────────────────

enum class AppLanguage(val code: String, val displayName: String) {
    SYSTEM("system", "Systemsprache"),
    DE("de",     "Deutsch"),
    EN("en",     "English"),
    NL("nl",     "Nederlands"),
    DA("da",     "Dansk"),
    NB("nb",     "Norsk bokmål"),
    SV("sv",     "Svenska"),
    FI("fi",     "Suomi"),
    IT("it",     "Italiano"),
    ES("es",     "Español"),
    PT("pt",     "Português"),
    FR("fr",     "Français"),
    PL("pl",     "Polski"),
    CS("cs",     "Čeština"),
    HU("hu",     "Magyar"),
    RO("ro",     "Română"),
    SK("sk",     "Slovenčina"),
    HR("hr",     "Hrvatski"),
    SL("sl",     "Slovenščina"),
    ET("et",     "Eesti"),
    LV("lv",     "Latviešu"),
    LT("lt",     "Lietuvių"),
    TLH("tlh",   "tlhIngan Hol");

    companion object {
        fun fromCode(code: String) = entries.firstOrNull { it.code == code } ?: SYSTEM
    }
}

/**
 * User-selectable ordering for the departures screen.
 * The enum names are persisted in DataStore, so they form a stable preference contract.
 */
enum class DepartureSortProfile {
    NEARBY,
    SOONEST,
    LINE_GROUPED;

    companion object {
        fun fromCode(code: String?): DepartureSortProfile =
            entries.firstOrNull { it.name.equals(code, ignoreCase = true) } ?: NEARBY
    }
}


enum class OrsTravelMode(
    val apiPath: String,
    val detailLabelRes: Int,
    val settingsLabelRes: Int
) {
    WALK(
        apiPath = "v2/directions/foot-walking/json",
        detailLabelRes = now.abfahrt.transit.R.string.detail_walk_time,
        settingsLabelRes = now.abfahrt.transit.R.string.ors_mode_walk
    ),
    BIKE(
        apiPath = "v2/directions/cycling-regular/json",
        detailLabelRes = now.abfahrt.transit.R.string.detail_bike_time,
        settingsLabelRes = now.abfahrt.transit.R.string.ors_mode_bike
    );

    companion object {
        fun fromCode(code: String?): OrsTravelMode =
            entries.firstOrNull { it.name.equals(code, ignoreCase = true) } ?: WALK
    }
}

data class AppPreferences(
    val apiKey: String              = "",
    val orsApiKey: String           = "",
    val onboardingCompleted: Boolean = false,
    val radius: Int                 = 800,
    val windowStartMinutes: Int     = 0,
    val windowEndMinutes: Int       = 30,
    val refreshIntervalMinutes: Int = 1,
    val maxPerDirection: Int        = 1,   // max departures per line+direction shown
    val departureSortProfile: DepartureSortProfile = DepartureSortProfile.NEARBY,
    val selectedModes: Set<TransportMode> = TransportMode.all,
    val quickFilterSlots: List<TransportMode> = listOf(
        TransportMode.SUBWAY,
        TransportMode.SUBURBAN,
        TransportMode.TRAM,
        TransportMode.BUS
    ),
    val language: AppLanguage = AppLanguage.SYSTEM,
    val orsTravelMode: OrsTravelMode = OrsTravelMode.WALK,
    val hideUnreachableDepartures: Boolean = false,
    val homePlace: SavedPlace? = null,
    val workPlace: SavedPlace? = null
)


data class WalkingRouteInfo(
    val distanceMeters: Int,
    val durationSeconds: Int
)


data class RoutePreviewDestination(
    val stationId: String? = null,
    val stationName: String,
    val lat: Double,
    val lon: Double
)

data class RoutePreviewData(
    val geoJson: String,
    val bbox: List<Double>? = null,
    val startLat: Double,
    val startLon: Double,
    val endLat: Double,
    val endLon: Double,
    val routeDistanceMeters: Int? = null,
    val routeDurationSeconds: Int? = null
)
