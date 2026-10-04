package now.abfahrt.transit.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import now.abfahrt.transit.BuildConfig
import now.abfahrt.transit.R
import now.abfahrt.transit.data.model.*
import now.abfahrt.transit.ui.components.DepartureCard
import now.abfahrt.transit.ui.components.TransportModeChips
import now.abfahrt.transit.ui.viewmodel.DepartureViewModel
import now.abfahrt.transit.util.DepartureFollowUpTimes
import now.abfahrt.transit.util.StationNameNormalizer

private data class DepartureListItem(
    val key: String,
    val departure: Departure
)

private fun buildStableDepartureItems(departures: List<Departure>): List<DepartureListItem> {
    val counts = mutableMapOf<String, Int>()
    return departures.map { dep ->
        val base = listOf(
            dep.stop,
            dep.line,
            dep.direction,
            dep.platform.orEmpty(),
            dep.mode.orEmpty()
        ).joinToString("|")
        val ordinal = counts.getOrDefault(base, 0)
        counts[base] = ordinal + 1
        DepartureListItem(key = "$base|$ordinal", departure = dep)
    }
}

private const val ROUTE_DESTINATION_LOG_TAG = "AbfahrtRouteDestination"

private fun resolveRoutePreviewStation(departure: Departure, stations: List<Station>): Station? {
    if (stations.isEmpty()) return null

    stations.firstOrNull { station ->
        station.name == departure.stop ||
            (station.id.isNotBlank() && (
                station.id == departure.providerStopId ||
                    station.id == departure.stop
                ))
    }?.let { return it }

    val stationDistance = departure.stationDistance.takeIf { it > 0 }
    val departureLookup = routePreviewLookupName(departure.stop)
    if (departureLookup.isNotBlank()) {
        stations
            .filter { routePreviewLookupName(it.name) == departureLookup }
            .minByOrNull { station ->
                stationDistance?.let { kotlin.math.abs(station.distance - it) } ?: station.distance
            }
            ?.let { return it }
    }

    if (stationDistance != null) {
        stations
            .filter { kotlin.math.abs(it.distance - stationDistance) <= ROUTE_PREVIEW_DISTANCE_MATCH_TOLERANCE_M }
            .minByOrNull { kotlin.math.abs(it.distance - stationDistance) }
            ?.let { return it }
    }

    return null
}

private const val ROUTE_PREVIEW_DISTANCE_MATCH_TOLERANCE_M = 8

internal fun routePreviewLookupName(value: String): String =
    StationNameNormalizer.routeLookupName(value)

internal enum class DepartureScreenMode {
    Home,
    AlternateLocation
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
internal fun DepartureScreen(
    viewModel: DepartureViewModel,
    screenMode: DepartureScreenMode = DepartureScreenMode.Home,
    routeDestinationSearchUiState: PlaceSearchUiState = PlaceSearchUiState(),
    onRouteDestinationQueryChange: (String) -> Unit = {},
    onRouteDestinationSelected: (SearchResult) -> Unit = {},
    onRouteSavedDestinationSelected: (SavedPlace) -> Unit = {},
    onDismissRouteDestinationSearch: () -> Unit = {},
    onOpenAlternateDepartures: () -> Unit = {},
    onOpenSavedPlaces: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val prefs by viewModel.preferences.collectAsState()
    val backgroundError by viewModel.backgroundError.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val searchUiState by viewModel.searchUiState.collectAsState()
    var showSheet by remember { mutableStateOf(false) }
    var initialSettingsKeySheet by remember { mutableStateOf<String?>(null) }
    var selectedDeparture by remember { mutableStateOf<Departure?>(null) }
    var showOrsKeySheetFromDeparture by remember { mutableStateOf(false) }
    var departureOrsKeyDraft by remember { mutableStateOf("") }
    var departureOrsKeyVisible by remember { mutableStateOf(false) }

    val isUsingCurrentLocation = searchUiState.selectedTarget is SearchTarget.CurrentLocation
    val isAlternateAwaitingTarget =
        screenMode == DepartureScreenMode.AlternateLocation && isUsingCurrentLocation
    val shouldUseDeviceLocationFlow =
        screenMode == DepartureScreenMode.Home && isUsingCurrentLocation

    val locationPerm = rememberPermissionState(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    ) { granted ->
        if (granted && uiState is DepartureUiState.Idle && shouldUseDeviceLocationFlow) {
            viewModel.fetchDepartures()
        }
    }

    LaunchedEffect(locationPerm.status, uiState, shouldUseDeviceLocationFlow) {
        if (locationPerm.status.isGranted && uiState is DepartureUiState.Idle && shouldUseDeviceLocationFlow) {
            viewModel.fetchDepartures()
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner, locationPerm.status.isGranted, shouldUseDeviceLocationFlow, isUsingCurrentLocation) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            if (locationPerm.status.isGranted && shouldUseDeviceLocationFlow) {
                viewModel.startLocationUpdates()
                viewModel.refreshIfStale()
            } else {
                viewModel.stopLocationUpdates()
                if (!isUsingCurrentLocation) viewModel.refreshIfStale()
            }
        }
    }
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_PAUSE || event == androidx.lifecycle.Lifecycle.Event.ON_STOP) {
                viewModel.stopLocationUpdates()
                viewModel.cancelActiveOrsEnrichment()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }


    val pullState = rememberPullToRefreshState()
    val listState = rememberLazyListState()
    val selectedFollowUpDepartures = remember(selectedDeparture, uiState) {
        val dep = selectedDeparture
        val success = uiState as? DepartureUiState.Success
        if (dep == null || success == null) {
            emptyList()
        } else {
            val followUps = DepartureFollowUpTimes.forSelectedDeparture(
                selected = dep,
                allDepartures = success.response.departures
            )
            if (BuildConfig.DEBUG) {
                val stats = DepartureFollowUpTimes.candidateStats(dep, success.response.departures)
                Log.d(
                    "AbfahrtFollowUp",
                    "selected line=${dep.line} dir=${dep.direction} stop=${dep.stop} response=${success.response.departures.size} sameLineDir=${stats.sameLineDirection} strict=${stats.strictSameService} local=${stats.localFollowUps} output=${stats.output} times=${stats.outputTimes}"
                )
            }
            followUps
        }
    }

    val selectedRoutePreviewOrigin = when (val target = searchUiState.selectedTarget) {
        SearchTarget.CurrentLocation -> viewModel.getRoutePreviewOrigin()
        is SearchTarget.Station -> target.lat to target.lon
    }

    val selectedRouteDestination = remember(selectedDeparture, uiState, searchUiState.selectedTarget) {
        when {
            selectedDeparture == null -> null
            searchUiState.selectedTarget is SearchTarget.Station -> {
                val target = searchUiState.selectedTarget as SearchTarget.Station
                RoutePreviewDestination(
                    stationId = target.name,
                    stationName = target.name,
                    lat = target.lat,
                    lon = target.lon
                )
            }
            uiState !is DepartureUiState.Success -> null
            else -> {
                val stations = (uiState as DepartureUiState.Success).response.stations.orEmpty()
                val dep = selectedDeparture!!
                val station = resolveRoutePreviewStation(dep, stations)
                val lat = station?.lat
                val lon = station?.lon
                if (lat != null && lon != null) {
                    if (BuildConfig.DEBUG && station.name != dep.stop) {
                        Log.d(
                            ROUTE_DESTINATION_LOG_TAG,
                            "resolved stop='${dep.stop}' via station='${station.name}' id='${station.id}' lat=$lat lon=$lon"
                        )
                    }
                    RoutePreviewDestination(
                        stationId = station.id.ifBlank { station.name },
                        stationName = station.name,
                        lat = lat,
                        lon = lon
                    )
                } else {
                    if (BuildConfig.DEBUG) {
                        Log.d(
                            ROUTE_DESTINATION_LOG_TAG,
                            "unresolved stop='${dep.stop}' stationDistance=${dep.stationDistance} stations=${stations.size}"
                        )
                    }
                    null
                }
            }
        }
    }

    val showTopLoadingIndicator = !isAlternateAwaitingTarget &&
        (isRefreshing || uiState is DepartureUiState.Loading)
    val stableFilteredItems by remember(uiState) {
        derivedStateOf {
            val success = uiState as? DepartureUiState.Success
            buildStableDepartureItems(success?.filtered.orEmpty())
        }
    }


    Scaffold(
        topBar = {
            DepartureHeader(
                screenMode = screenMode,
                uiState = uiState,
                searchUiState = searchUiState,
                routeDestinationSearchUiState = routeDestinationSearchUiState,
                onSearchQueryChange = viewModel::updateSearchQuery,
                onSelectResult = { result -> viewModel.selectSearchResult(result) },
                onRouteDestinationQueryChange = onRouteDestinationQueryChange,
                onRouteDestinationSelected = onRouteDestinationSelected,
                onRouteSavedDestinationSelected = onRouteSavedDestinationSelected,
                homePlace = prefs.homePlace,
                workPlace = prefs.workPlace,
                onDismissRouteDestinationSearch = onDismissRouteDestinationSearch,
                onRefresh = {
                    when {
                        isAlternateAwaitingTarget -> Unit
                        shouldUseDeviceLocationFlow && !locationPerm.status.isGranted ->
                            locationPerm.launchPermissionRequest()
                        else -> viewModel.refresh()
                    }
                },
                onSettings = { showSheet = true },
                onOpenAlternateDepartures = onOpenAlternateDepartures,
                onOpenSavedPlaces = onOpenSavedPlaces,
                onNavigateBack = onNavigateBack,
                onDismissSearch = viewModel::clearSearchResults
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (!isAlternateAwaitingTarget) backgroundError?.let { errMsg ->
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = errMsg,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.clearBackgroundError() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = stringResource(R.string.close),
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            if (showTopLoadingIndicator) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                )
            }
            PullToRefreshBox(
                isRefreshing = false,
                indicator = {},
                onRefresh = {
                    when {
                        isAlternateAwaitingTarget -> Unit
                        shouldUseDeviceLocationFlow && !locationPerm.status.isGranted ->
                            locationPerm.launchPermissionRequest()
                        else -> viewModel.refresh(force = true)
                    }
                },
                state = pullState,
                modifier = Modifier.fillMaxSize()
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (isAlternateAwaitingTarget) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = stringResource(R.string.search_station_prompt),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(32.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else when (val state = uiState) {
                    is DepartureUiState.Idle -> {
                        if (shouldUseDeviceLocationFlow) {
                            PermissionOrIdleContent(locationPerm)
                        } else {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = stringResource(R.string.search_station_prompt),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(32.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    is DepartureUiState.Loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                stringResource(R.string.loading_departures),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    is DepartureUiState.Error -> {
                        ErrorContent(
                            message = state.message,
                            onRetry = {
                                if (isUsingCurrentLocation && !locationPerm.status.isGranted) {
                                    locationPerm.launchPermissionRequest()
                                } else {
                                    viewModel.refresh()
                                }
                            }
                        )
                    }
                    is DepartureUiState.Success -> {
                        val trailingColumnWidth = rememberTrailingColumnWidth(state.filtered)
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 8.dp,
                                bottom = 88.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                TransportModeChips(
                                    selectedModes = prefs.selectedModes,
                                    quickSlots = prefs.quickFilterSlots,
                                    onToggle = viewModel::toggleMode,
                                    modifier = Modifier.padding(horizontal = 0.dp)
                                )
                                Spacer(Modifier.height(4.dp))
                            }

                            if (state.filtered.isEmpty()) {
                                item {
                                    if (state.response.departures.isEmpty()) EmptyContent() else FilterEmptyContent()
                                }
                            } else {
                                items(
                                    items = stableFilteredItems,
                                    key = { it.key }
                                ) { item ->
                                    DepartureCard(
                                        departure = item.departure,
                                        trailingColumnWidth = trailingColumnWidth,
                                        onClick = { selectedDeparture = item.departure }
                                    )
                                }
                            }
                        }
                    }
                    }
                }
            }
        }
    }

    selectedDeparture?.let { dep ->
        DepartureDetailSheet(
            departure = dep,
            followUpDepartures = selectedFollowUpDepartures,
            orsApiKeyConfigured = prefs.orsApiKey.isNotBlank(),
            orsTravelMode = prefs.orsTravelMode,
            routePreviewOrigin = selectedRoutePreviewOrigin,
            routePreviewDestination = selectedRouteDestination,
            loadRoutePreview = viewModel::loadRoutePreview,
            onDismiss = { selectedDeparture = null },
            onActivateWalkFeature = {
                departureOrsKeyDraft = prefs.orsApiKey
                departureOrsKeyVisible = false
                showOrsKeySheetFromDeparture = true
            }
        )
    }

    if (showOrsKeySheetFromDeparture) {
        ApiKeyEditorSheet(
            title = stringResource(R.string.section_ors_apikey),
            hint = stringResource(R.string.ors_apikey_new_label_short),
            value = departureOrsKeyDraft,
            valueVisible = departureOrsKeyVisible,
            onValueChange = { departureOrsKeyDraft = it.trim() },
            onToggleVisibility = { departureOrsKeyVisible = !departureOrsKeyVisible },
            errorText = null,
            supportingText = stringResource(R.string.ors_apikey_sheet_hint),
            hasExistingKey = prefs.orsApiKey.isNotBlank(),
            linkPrimaryLabel = stringResource(R.string.ors_plans_label),
            linkPrimaryUrl = stringResource(R.string.ors_plans_url),
            linkSecondaryLabel = stringResource(R.string.ors_signup_label),
            linkSecondaryUrl = stringResource(R.string.ors_signup_url),
            onDismiss = { showOrsKeySheetFromDeparture = false },
            onDelete = {
                viewModel.saveOrsApiKey("")
                departureOrsKeyDraft = ""
                showOrsKeySheetFromDeparture = false
                viewModel.refresh()
            },
            onSave = {
                viewModel.saveOrsApiKey(departureOrsKeyDraft)
                showOrsKeySheetFromDeparture = false
                viewModel.refresh()
            }
        )
    }

    if (showSheet) {
        SettingsSheet(
            prefs = prefs,
            onDismiss = { showSheet = false; initialSettingsKeySheet = null },
            onRadiusChange = viewModel::saveRadius,
            onWindowChange = { s, e -> viewModel.saveWindowRange(s, e) },
            onRefreshChange = viewModel::saveRefreshInterval,
            onMaxPerDirChange = viewModel::saveMaxPerDirection,
            onLanguageChange = viewModel::saveLanguage,
            onModeToggle = viewModel::toggleMode,
            onQuickSlotsChange = viewModel::saveQuickSlots,
            onApiKeyChange = { key ->
                viewModel.saveApiKey(key)
                viewModel.refresh()
            },
            onOrsApiKeyChange = { key ->
                viewModel.saveOrsApiKey(key)
                viewModel.refresh()
            },
            onOrsTravelModeChange = { mode ->
                viewModel.saveOrsTravelMode(mode)
                if (prefs.orsApiKey.isNotBlank()) viewModel.refresh()
            },
            onHideUnreachableChange = viewModel::saveHideUnreachableDepartures,
            initialKeySheet = initialSettingsKeySheet
        )
    }
}

@Composable
private fun rememberTrailingColumnWidth(departures: List<Departure>): Dp {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val onTime = stringResource(R.string.on_time)
    val cancelled = stringResource(R.string.cancelled)

    fun measure(text: String, style: TextStyle): Int =
        textMeasurer.measure(text = text, style = style, maxLines = 1).size.width

    val timeStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
    val statusStyle = MaterialTheme.typography.labelSmall
    val cancelledStyle = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)

    val outerHorizontalPaddingPx = with(density) { 12.dp.roundToPx() }
    val delayBadgeHorizontalPaddingPx = with(density) { 10.dp.roundToPx() }
    val trailingSafetyBufferPx = with(density) { 12.dp.roundToPx() }

    val maxPx = remember(departures, onTime, cancelled, density) {
        var maxWidth = 0
        departures.forEach { dep ->
            val timeWidth = if (dep.cancelled) {
                measure(cancelled, cancelledStyle)
            } else {
                measure(dep.time, timeStyle)
            }

            val statusContentWidth = when {
                dep.cancelled -> 0
                dep.delay > 0 -> measure("+${dep.delay} min", statusStyle) + delayBadgeHorizontalPaddingPx
                dep.delay < 0 -> measure("${dep.delay} min", statusStyle) + delayBadgeHorizontalPaddingPx
                else -> measure(onTime, statusStyle)
            }

            val columnWidth = maxOf(timeWidth, statusContentWidth) + outerHorizontalPaddingPx + trailingSafetyBufferPx
            maxWidth = maxOf(maxWidth, columnWidth)
        }
        maxWidth
    }

    return with(density) { maxPx.toDp() }
}

@Composable
private fun DepartureHeader(
    screenMode: DepartureScreenMode,
    uiState: DepartureUiState,
    searchUiState: SearchUiState,
    routeDestinationSearchUiState: PlaceSearchUiState,
    onSearchQueryChange: (String) -> Unit,
    onSelectResult: (SearchResult) -> Unit,
    onRouteDestinationQueryChange: (String) -> Unit,
    onRouteDestinationSelected: (SearchResult) -> Unit,
    onRouteSavedDestinationSelected: (SavedPlace) -> Unit,
    homePlace: SavedPlace?,
    workPlace: SavedPlace?,
    onDismissRouteDestinationSearch: () -> Unit,
    onRefresh: () -> Unit,
    onSettings: () -> Unit,
    onOpenAlternateDepartures: () -> Unit,
    onOpenSavedPlaces: () -> Unit,
    onNavigateBack: () -> Unit,
    onDismissSearch: () -> Unit
) {
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val searchPlaceholder = stringResource(R.string.search_station_placeholder)
    val isAlternate = screenMode == DepartureScreenMode.AlternateLocation

    val selectedTargetLabel = when (val target = searchUiState.selectedTarget) {
        is SearchTarget.CurrentLocation -> ""
        is SearchTarget.Station -> target.name
    }

    var fieldValue by remember(screenMode) { mutableStateOf(selectedTargetLabel) }
    var isFocused by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    var routeFieldValue by remember(screenMode) { mutableStateOf("") }
    var routeFieldFocused by remember { mutableStateOf(false) }

    LaunchedEffect(selectedTargetLabel, searchUiState.query, isFocused, screenMode) {
        if (isAlternate) {
            fieldValue = when {
                searchUiState.query.isNotBlank() -> searchUiState.query
                !isFocused -> selectedTargetLabel
                else -> fieldValue
            }
        }
    }

    Surface(shadowElevation = 0.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isAlternate) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                    Text(
                        text = stringResource(R.string.alternate_departures_title),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onRefresh,
                        enabled = searchUiState.selectedTarget is SearchTarget.Station
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.refresh))
                    }
                } else {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🚌", fontSize = 32.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.app_name),
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.refresh))
                    }
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.more_options)
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.alternate_departures_title)) },
                                leadingIcon = {
                                    Icon(Icons.Default.LocationOn, contentDescription = null)
                                },
                                onClick = {
                                    menuExpanded = false
                                    onOpenAlternateDepartures()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.saved_places_title)) },
                                leadingIcon = {
                                    Icon(Icons.Default.Home, contentDescription = null)
                                },
                                onClick = {
                                    menuExpanded = false
                                    onOpenSavedPlaces()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.settings_title)) },
                                leadingIcon = {
                                    Icon(Icons.Default.Settings, contentDescription = null)
                                },
                                onClick = {
                                    menuExpanded = false
                                    onSettings()
                                }
                            )
                        }
                    }
                }
            }

            if (!isAlternate && uiState is DepartureUiState.Success) {
                Text(
                    text = uiState.response.region,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!isAlternate) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = routeFieldValue,
                        onValueChange = {
                            routeFieldValue = it
                            onRouteDestinationQueryChange(it)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                routeFieldFocused = focusState.isFocused
                                if (!focusState.isFocused && routeDestinationSearchUiState.query.isBlank()) {
                                    routeFieldValue = ""
                                    onDismissRouteDestinationSearch()
                                }
                            },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (routeDestinationSearchUiState.isSearching) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                } else if (routeDestinationSearchUiState.query.isNotBlank()) {
                                    IconButton(onClick = {
                                        routeFieldValue = ""
                                        focusManager.clearFocus()
                                        onDismissRouteDestinationSearch()
                                    }) {
                                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close))
                                    }
                                }
                            }
                        },
                        placeholder = { Text(stringResource(R.string.route_home_search_placeholder)) }
                    )

                    val showSavedDestinations = routeFieldFocused &&
                        routeFieldValue.isBlank() &&
                        routeDestinationSearchUiState.query.isBlank() &&
                        (homePlace != null || workPlace != null)

                    if (showSavedDestinations) {
                        SavedRouteDestinationPanel(
                            homePlace = homePlace,
                            workPlace = workPlace,
                            onSelect = { place ->
                                focusManager.clearFocus()
                                routeFieldValue = ""
                                onDismissRouteDestinationSearch()
                                onRouteSavedDestinationSelected(place)
                            }
                        )
                    } else {
                        SearchResultPanel(
                            visible = routeFieldFocused,
                            query = routeDestinationSearchUiState.query,
                            results = routeDestinationSearchUiState.results,
                            isSearching = routeDestinationSearchUiState.isSearching,
                            errorMessage = routeDestinationSearchUiState.errorMessage,
                            promptText = stringResource(R.string.route_search_prompt),
                            onSelectResult = { result ->
                                focusManager.clearFocus()
                                routeFieldValue = ""
                                onRouteDestinationSelected(result)
                            }
                        )
                    }
                }
            }

            if (isAlternate) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = fieldValue,
                        onValueChange = {
                            fieldValue = it
                            onSearchQueryChange(it)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                val focusedNow = focusState.isFocused
                                if (focusedNow && !isFocused && searchUiState.query.isBlank() && fieldValue == selectedTargetLabel) {
                                    fieldValue = ""
                                }
                                if (!focusedNow && isFocused) {
                                    if (searchUiState.query.isBlank()) {
                                        fieldValue = selectedTargetLabel
                                    }
                                    onDismissSearch()
                                }
                                isFocused = focusedNow
                            },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        },
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (searchUiState.isSearching) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                } else if (searchUiState.query.isNotBlank()) {
                                    IconButton(onClick = {
                                        fieldValue = selectedTargetLabel
                                        focusManager.clearFocus()
                                        onDismissSearch()
                                    }) {
                                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close))
                                    }
                                }
                            }
                        },
                        placeholder = { Text(searchPlaceholder) }
                    )

                    SearchResultPanel(
                        visible = isFocused && fieldValue != selectedTargetLabel,
                        query = searchUiState.query,
                        results = searchUiState.results,
                        isSearching = searchUiState.isSearching,
                        errorMessage = searchUiState.errorMessage,
                        promptText = stringResource(R.string.search_station_prompt),
                        onSelectResult = { result ->
                            focusManager.clearFocus()
                            fieldValue = result.title
                            onSelectResult(result)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SavedRouteDestinationPanel(
    homePlace: SavedPlace?,
    workPlace: SavedPlace?,
    onSelect: (SavedPlace) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 2.dp,
        shadowElevation = 1.dp,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            homePlace?.let { place ->
                SavedRouteDestinationRow(
                    icon = Icons.Default.Home,
                    label = stringResource(R.string.saved_places_home),
                    place = place,
                    onClick = { onSelect(place) }
                )
            }
            if (homePlace != null && workPlace != null) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
            workPlace?.let { place ->
                SavedRouteDestinationRow(
                    icon = Icons.Default.Work,
                    label = stringResource(R.string.saved_places_work),
                    place = place,
                    onClick = { onSelect(place) }
                )
            }
        }
    }
}

@Composable
private fun SavedRouteDestinationRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    place: SavedPlace,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null)
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontWeight = FontWeight.SemiBold)
            Text(
                text = place.title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            if (place.subtitle.isNotBlank() && place.subtitle != place.title) {
                Text(
                    text = place.subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun SearchResultPanel(
    visible: Boolean,
    query: String,
    results: List<SearchResult>,
    isSearching: Boolean,
    errorMessage: String?,
    promptText: String,
    onSelectResult: (SearchResult) -> Unit
) {
    if (!visible) return

    val showTypingHint = query.isBlank() || query.trim().length < 3

    Surface(
        tonalElevation = 2.dp,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            when {
                showTypingHint -> {
                    Text(
                        text = promptText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(14.dp)
                    )
                }
                isSearching -> {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Text(stringResource(R.string.search_loading))
                    }
                }
                errorMessage != null -> {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(14.dp)
                    )
                }
                results.isEmpty() -> {
                    Text(
                        text = stringResource(R.string.search_no_results),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(14.dp)
                    )
                }
                else -> {
                    results.forEachIndexed { index, result ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectResult(result) }
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                            if (result.typeLabel.isNotBlank()) {
                                Text(
                                    text = result.typeLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.height(2.dp))
                            }
                            Text(
                                text = result.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (result.subtitle.isNotBlank()) {
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = result.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        if (index < results.lastIndex) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun PermissionOrIdleContent(perm: PermissionState) {
    if (perm.status.isGranted) return

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text("📍", fontSize = 56.sp)
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.allow_location),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.permission_rationale_full),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { perm.launchPermissionRequest() },
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 14.dp)
            ) {
                Text(stringResource(R.string.allow_location), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text("⚠️", fontSize = 48.sp)
            Spacer(Modifier.height(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(20.dp))
            Button(onClick = onRetry) {
                Icon(Icons.Default.Refresh, null)
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.retry))
            }
        }
    }
}

@Composable
private fun EmptyContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 64.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🚉", fontSize = 48.sp)
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.no_departures_found),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FilterEmptyContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp, start = 16.dp, end = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("⏱️", fontSize = 40.sp)
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.no_results_time_window),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.no_results_time_window_hint),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
