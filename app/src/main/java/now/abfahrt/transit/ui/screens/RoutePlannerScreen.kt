package now.abfahrt.transit.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import now.abfahrt.transit.R
import now.abfahrt.transit.data.model.PlaceSearchUiState
import now.abfahrt.transit.data.model.RouteEndpoint
import now.abfahrt.transit.data.model.SavedPlace
import now.abfahrt.transit.data.model.SearchResult
import now.abfahrt.transit.data.model.TransportMode
import now.abfahrt.transit.data.model.Trip
import now.abfahrt.transit.data.model.TripLeg
import now.abfahrt.transit.data.model.TripPlanningUiState
import now.abfahrt.transit.ui.components.badgeTextColor
import now.abfahrt.transit.ui.components.formatDirectionNameForDisplay
import now.abfahrt.transit.ui.components.formatStopNameForDisplay
import now.abfahrt.transit.ui.components.inferModeFromLine
import now.abfahrt.transit.ui.components.lineBadgeColor
import now.abfahrt.transit.ui.theme.TransitBlue
import now.abfahrt.transit.ui.viewmodel.RoutePlannerViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RoutePlannerScreen(
    viewModel: RoutePlannerViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val currentLocationLabel = stringResource(R.string.search_current_location)
    val originLabel = routeEndpointLabel(state.origin, currentLocationLabel)
    val destinationLabel = routeEndpointLabel(state.destination, currentLocationLabel)
    var routeSortModeName by rememberSaveable { mutableStateOf(RouteSortMode.EARLIEST.name) }
    val routeSortMode = RouteSortMode.entries.firstOrNull { it.name == routeSortModeName } ?: RouteSortMode.EARLIEST

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.route_planner_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.9f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                RouteEndpointField(
                                    label = stringResource(R.string.route_from),
                                    endpoint = state.origin,
                                    searchState = state.originSearch,
                                    currentLocationLabel = currentLocationLabel,
                                    useCurrentLocationLabel = stringResource(R.string.use_current_location),
                                    homePlace = state.homePlace,
                                    workPlace = state.workPlace,
                                    prompt = stringResource(R.string.route_search_prompt),
                                    onQueryChange = viewModel::updateOriginQuery,
                                    onSelectResult = viewModel::selectOrigin,
                                    onUseCurrentLocation = viewModel::useCurrentLocationAsOrigin,
                                    onUseHome = viewModel::useSavedPlaceAsOrigin,
                                    onUseWork = viewModel::useSavedPlaceAsOrigin,
                                    onDismissSearch = viewModel::clearOriginSearch,
                                    embedded = true
                                )

                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )

                                RouteEndpointField(
                                    label = stringResource(R.string.route_to),
                                    endpoint = state.destination,
                                    searchState = state.destinationSearch,
                                    currentLocationLabel = currentLocationLabel,
                                    useCurrentLocationLabel = stringResource(R.string.use_current_location),
                                    homePlace = state.homePlace,
                                    workPlace = state.workPlace,
                                    prompt = stringResource(R.string.route_search_prompt),
                                    onQueryChange = viewModel::updateDestinationQuery,
                                    onSelectResult = viewModel::selectDestination,
                                    onUseCurrentLocation = viewModel::useCurrentLocationAsDestination,
                                    onUseHome = viewModel::useSavedPlaceAsDestination,
                                    onUseWork = viewModel::useSavedPlaceAsDestination,
                                    onDismissSearch = viewModel::clearDestinationSearch,
                                    embedded = true
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .width(52.dp)
                                    .padding(top = 39.dp),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                FilledTonalIconButton(
                                    onClick = viewModel::swapEndpoints,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        Icons.Default.SwapVert,
                                        contentDescription = stringResource(R.string.route_swap)
                                    )
                                }
                            }
                        }
                    }

                    Button(
                        onClick = viewModel::findRoutes,
                        enabled = state.destination != null &&
                            state.originSearch.query.isBlank() &&
                            state.destinationSearch.query.isBlank() &&
                            state.tripState !is TripPlanningUiState.Loading,
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 15.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TransitBlue,
                            contentColor = Color.White,
                            disabledContainerColor = TransitBlue.copy(alpha = 0.35f),
                            disabledContentColor = Color.White.copy(alpha = 0.72f)
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.route_find),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            when (val tripState = state.tripState) {
                TripPlanningUiState.Idle -> Unit
                TripPlanningUiState.Loading -> item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 28.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(10.dp))
                        Text(stringResource(R.string.route_loading))
                    }
                }
                is TripPlanningUiState.Error -> item {
                    RouteStatusCard(
                        message = tripState.message,
                        isError = true,
                        onRetry = viewModel::findRoutes
                    )
                }
                is TripPlanningUiState.Success -> {
                    val response = tripState.response
                    val attribution = response.attribution?.trim()?.takeIf { it.isNotBlank() }

                    if (response.trips.isEmpty()) {
                        item {
                            RouteStatusCard(
                                message = stringResource(R.string.route_no_results),
                                isError = false,
                                onRetry = viewModel::findRoutes
                            )
                        }
                    } else {
                        item {
                            RouteSortChips(
                                selected = routeSortMode,
                                onSelected = { selected -> routeSortModeName = selected.name }
                            )
                        }
                        val sortedTrips = sortTripsForRoute(response.trips, routeSortMode)
                        itemsIndexed(
                            items = sortedTrips,
                            key = { index, trip -> "${trip.departure}|${trip.arrival}|${trip.duration}|${trip.changes}|$index" }
                        ) { index, trip ->
                            TripCard(
                                index = index,
                                trip = trip,
                                originLabel = originLabel,
                                destinationLabel = destinationLabel
                            )
                        }
                    }

                    if (attribution != null) {
                        item {
                            RouteAttribution(attribution)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RouteStatusCard(
    message: String,
    isError: Boolean,
    onRetry: () -> Unit
) {
    Surface(
        color = if (isError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = if (isError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            OutlinedButton(onClick = onRetry) {
                Text(stringResource(R.string.retry))
            }
        }
    }
}

@Composable
private fun RouteAttribution(attribution: String) {
    Text(
        text = attribution,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

@Composable
private fun RouteEndpointField(
    label: String,
    endpoint: RouteEndpoint?,
    searchState: PlaceSearchUiState,
    currentLocationLabel: String,
    useCurrentLocationLabel: String,
    homePlace: SavedPlace?,
    workPlace: SavedPlace?,
    prompt: String,
    onQueryChange: (String) -> Unit,
    onSelectResult: (SearchResult) -> Unit,
    onUseCurrentLocation: () -> Unit,
    onUseHome: (SavedPlace) -> Unit,
    onUseWork: (SavedPlace) -> Unit,
    onDismissSearch: () -> Unit,
    embedded: Boolean = false
) {
    val focusManager = LocalFocusManager.current
    val selectedLabel = routeEndpointLabel(endpoint, currentLocationLabel)
    var fieldValue by remember(endpoint) { mutableStateOf(selectedLabel) }
    var focused by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(if (embedded) 2.dp else 6.dp)) {
        OutlinedTextField(
            value = fieldValue,
            onValueChange = {
                fieldValue = it
                onQueryChange(it)
            },
            label = { Text(label) },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    val nowFocused = focusState.isFocused
                    if (nowFocused && !focused && searchState.query.isBlank() && fieldValue == selectedLabel) {
                        fieldValue = ""
                    }
                    if (!nowFocused && focused) {
                        fieldValue = selectedLabel
                        onDismissSearch()
                    }
                    focused = nowFocused
                },
            singleLine = true,
            leadingIcon = {
                Icon(
                    if (endpoint is RouteEndpoint.CurrentLocation) Icons.Default.LocationOn else Icons.Default.Search,
                    contentDescription = null
                )
            },
            trailingIcon = {
                if (searchState.isSearching) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else if (searchState.query.isNotBlank()) {
                    IconButton(onClick = {
                        fieldValue = selectedLabel
                        focusManager.clearFocus()
                        onDismissSearch()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close))
                    }
                }
            },
            shape = if (embedded) RoundedCornerShape(0.dp) else RoundedCornerShape(18.dp),
            colors = if (embedded) {
                OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedLabelColor = TransitBlue,
                    cursorColor = TransitBlue
                )
            } else {
                OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TransitBlue,
                    focusedLabelColor = TransitBlue,
                    cursorColor = TransitBlue
                )
            }
        )

        if (focused) {
            RouteSearchPanel(
                searchState = searchState,
                prompt = prompt,
                useCurrentLocationLabel = useCurrentLocationLabel,
                homePlace = homePlace,
                workPlace = workPlace,
                onUseCurrentLocation = {
                    focusManager.clearFocus()
                    fieldValue = currentLocationLabel
                    onUseCurrentLocation()
                },
                onUseHome = { place ->
                    focusManager.clearFocus()
                    fieldValue = place.title
                    onUseHome(place)
                },
                onUseWork = { place ->
                    focusManager.clearFocus()
                    fieldValue = place.title
                    onUseWork(place)
                },
                onSelectResult = { result ->
                    focusManager.clearFocus()
                    fieldValue = result.title
                    onSelectResult(result)
                }
            )
        }
    }
}

@Composable
private fun RouteSearchPanel(
    searchState: PlaceSearchUiState,
    prompt: String,
    useCurrentLocationLabel: String,
    homePlace: SavedPlace?,
    workPlace: SavedPlace?,
    onUseCurrentLocation: () -> Unit,
    onUseHome: (SavedPlace) -> Unit,
    onUseWork: (SavedPlace) -> Unit,
    onSelectResult: (SearchResult) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 2.dp,
        shadowElevation = 1.dp,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onUseCurrentLocation)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null)
                Spacer(Modifier.width(10.dp))
                Text(useCurrentLocationLabel, fontWeight = FontWeight.SemiBold)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            if (searchState.query.isBlank()) {
                homePlace?.let { place ->
                    SavedPlaceQuickRow(
                        emoji = "🏠",
                        label = stringResource(R.string.saved_places_home),
                        place = place,
                        onClick = { onUseHome(place) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
                workPlace?.let { place ->
                    SavedPlaceQuickRow(
                        emoji = "💼",
                        label = stringResource(R.string.saved_places_work),
                        place = place,
                        onClick = { onUseWork(place) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }

            when {
                searchState.query.trim().length < 3 -> {
                    Text(
                        text = prompt,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(14.dp)
                    )
                }
                searchState.isSearching -> {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Text(stringResource(R.string.search_loading))
                    }
                }
                searchState.errorMessage != null -> {
                    Text(
                        text = searchState.errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(14.dp)
                    )
                }
                searchState.results.isEmpty() -> {
                    Text(
                        text = stringResource(R.string.search_no_results),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(14.dp)
                    )
                }
                else -> {
                    searchState.results.forEachIndexed { index, result ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectResult(result) }
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                            Text(result.title, fontWeight = FontWeight.SemiBold)
                            if (result.subtitle.isNotBlank()) {
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = result.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        if (index < searchState.results.lastIndex) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SavedPlaceQuickRow(
    emoji: String,
    label: String,
    place: SavedPlace,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = emoji, fontSize = 20.sp)
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontWeight = FontWeight.SemiBold)
            Text(
                text = place.title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

internal enum class RouteSortMode {
    EARLIEST,
    FASTEST,
    FEWEST_CHANGES,
    LEAST_WALKING
}

@Composable
private fun RouteSortChips(
    selected: RouteSortMode,
    onSelected: (RouteSortMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RouteSortMode.entries.forEach { mode ->
            val label = when (mode) {
                RouteSortMode.EARLIEST -> stringResource(R.string.route_sort_earliest)
                RouteSortMode.FASTEST -> stringResource(R.string.route_sort_fastest)
                RouteSortMode.FEWEST_CHANGES -> stringResource(R.string.route_sort_fewest_changes)
                RouteSortMode.LEAST_WALKING -> stringResource(R.string.route_sort_least_walking)
            }
            FilterChip(
                selected = selected == mode,
                onClick = { onSelected(mode) },
                label = { Text(label, maxLines = 1) }
            )
        }
    }
}

@Composable
private fun TripCard(
    index: Int,
    trip: Trip,
    originLabel: String,
    destinationLabel: String
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "${formatTripTime(trip.departure)} → ${formatTripTime(trip.arrival)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.route_option_number, index + 1),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SummaryPill(
                        label = stringResource(R.string.route_duration_minutes, trip.duration),
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    SummaryPill(
                        label = stringResource(R.string.route_changes_count, trip.changes),
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            trip.legs.forEachIndexed { legIndex, leg ->
                if (legIndex > 0) {
                    val previousLeg = trip.legs[legIndex - 1]
                    if (leg.sameVehicle) {
                        SummaryPill(
                            label = stringResource(R.string.route_same_vehicle),
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    } else if (!isWalkingLeg(previousLeg) && !isWalkingLeg(leg)) {
                        routeTransferMinutes(previousLeg, leg)?.let { minutes ->
                            TransferSeparator(minutes)
                        }
                    }
                }
                TripLegContent(
                    leg = leg,
                    legIndex = legIndex,
                    totalLegs = trip.legs.size,
                    originLabel = originLabel,
                    destinationLabel = destinationLabel
                )
            }
        }
    }
}

@Composable
private fun TransferSeparator(minutes: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant
        )
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(999.dp)
        ) {
            Text(
                text = stringResource(R.string.route_transfer_minutes, minutes),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            )
        }
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

@Composable
private fun SummaryPill(
    label: String,
    containerColor: Color,
    contentColor: Color
) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(999.dp)
    ) {
        Text(
            text = label,
            color = contentColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun TripLegContent(
    leg: TripLeg,
    legIndex: Int,
    totalLegs: Int,
    originLabel: String,
    destinationLabel: String
) {
    val context = LocalContext.current
    val isWalking = isWalkingLeg(leg)
    val fallbackMode = if (isWalking) null else inferModeFromLine(leg.line)
    val mode = if (isWalking) null else TransportMode.fromApiValue(leg.mode) ?: fallbackMode
    val walkLabel = stringResource(R.string.ors_mode_walk)
    val lineLabel = when {
        isWalking -> walkLabel
        leg.line.isNotBlank() -> leg.line.trim()
        mode != null -> stringResource(mode.labelRes)
        else -> leg.line.ifBlank { "?" }
    }
    val badgeBackground = if (isWalking) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        lineBadgeColor(lineLabel, mode)
    }
    val badgeForeground = if (isWalking) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        badgeTextColor(lineLabel, mode, badgeBackground)
    }

    val fromLabel = tripEndpointDisplayLabel(
        raw = leg.from,
        fallback = if (legIndex == 0) originLabel else null
    )
    val toLabel = tripEndpointDisplayLabel(
        raw = leg.to,
        fallback = if (legIndex == totalLegs - 1) destinationLabel else null
    )
    val directionLabel = leg.direction
        .takeUnless { isWalking }
        ?.takeIf { it.isNotBlank() }
        ?.takeUnless(::looksTechnicalRouteReference)
        ?.let(::formatDirectionNameForDisplay)
        ?.takeIf { it.isNotBlank() }

    val stopsCount = maxOf(
        leg.stops,
        leg.stopNames?.size ?: 0,
        leg.intermediateStops?.size ?: 0
    )
    val intermediateStops = remember(leg) { routeIntermediateStopsForDisplay(leg) }
    var intermediateExpanded by remember(leg) { mutableStateOf(false) }
    val details = buildList {
        if (!isWalking && !leg.departurePlatform.isNullOrBlank()) add(stringResource(R.string.platform, leg.departurePlatform))
        if (!isWalking && leg.departureDelay > 0) add(stringResource(R.string.detail_delay_value, leg.departureDelay))
        if (!isWalking && leg.departureDelay < 0) add(stringResource(R.string.detail_early_value, leg.departureDelay))
        if (leg.cancelled) add(stringResource(R.string.detail_status_cancelled))
    }
    val navigationQuery = if (isWalking && (legIndex == 0 || legIndex == totalLegs - 1)) {
        routeWalkingNavigationQuery(leg, toLabel)
    } else {
        null
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            RouteLegHeader(
                isWalking = isWalking,
                mode = mode,
                lineLabel = lineLabel,
                badgeBackground = badgeBackground,
                badgeForeground = badgeForeground,
                directionLabel = directionLabel,
                durationMinutes = routeLegDurationMinutes(leg),
                onOpenNavigation = navigationQuery?.let { query ->
                    { openWalkingNavigation(context, query) }
                }
            )

            RouteStopRow(
                time = formatTripTime(leg.departure),
                label = fromLabel,
                marker = "•",
                emphasize = true
            )
            RouteStopRow(
                time = formatTripTime(leg.arrival),
                label = toLabel,
                marker = "→",
                emphasize = false
            )

            if (details.isNotEmpty()) {
                Text(
                    text = details.joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (leg.cancelled) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = ROUTE_CONTENT_INDENT)
                )
            }

            if (!isWalking && stopsCount > 0) {
                Box(modifier = Modifier.padding(start = ROUTE_CONTENT_INDENT)) {
                    IntermediateStopsToggle(
                        count = stopsCount,
                        canExpand = intermediateStops.isNotEmpty(),
                        expanded = intermediateExpanded,
                        onToggle = { intermediateExpanded = !intermediateExpanded }
                    )
                }
                if (intermediateExpanded && intermediateStops.isNotEmpty()) {
                    IntermediateStopsList(intermediateStops)
                }
            }
        }
    }
}

@Composable
private fun RouteLegHeader(
    isWalking: Boolean,
    mode: TransportMode?,
    lineLabel: String,
    badgeBackground: Color,
    badgeForeground: Color,
    directionLabel: String?,
    durationMinutes: Int,
    onOpenNavigation: (() -> Unit)?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(ROUTE_LEADING_COLUMN_WIDTH)
        ) {
            Text(
                text = if (isWalking) "🚶" else mode?.emoji ?: "🚍",
                fontSize = 20.sp
            )
            Spacer(Modifier.height(4.dp))
            Surface(
                color = badgeBackground,
                shape = RoundedCornerShape(7.dp),
                modifier = Modifier.widthIn(min = 44.dp)
            ) {
                Text(
                    text = lineLabel,
                    color = badgeForeground,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelMedium,
                    fontSize = 12.sp,
                    maxLines = 1,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
        Spacer(Modifier.width(ROUTE_COLUMN_GAP))
        Spacer(Modifier.width(ROUTE_MARKER_WIDTH))
        Text(
            text = if (isWalking) {
                stringResource(R.string.route_duration_minutes, durationMinutes)
            } else {
                directionLabel?.let { stringResource(R.string.route_direction, it) }.orEmpty()
            },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isWalking) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isWalking) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 3.dp)
        )
        if (onOpenNavigation != null) {
            IconButton(
                onClick = onOpenNavigation,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Navigation,
                    contentDescription = stringResource(R.string.route_open_walking_navigation),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun IntermediateStopsToggle(
    count: Int,
    canExpand: Boolean,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    val modifier = if (canExpand) {
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
    } else {
        Modifier.fillMaxWidth()
    }

    Row(
        modifier = modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.route_stops_count, count),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        if (canExpand) {
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = stringResource(
                    if (expanded) R.string.route_hide_intermediate_stops else R.string.route_show_intermediate_stops
                ),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun IntermediateStopsList(stops: List<RouteIntermediateStopDisplay>) {
    Column(
        modifier = Modifier.padding(end = 4.dp, bottom = 2.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        stops.forEach { stop ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = stop.arrival?.let(::formatTripTime).orEmpty(),
                    modifier = Modifier.width(ROUTE_LEADING_COLUMN_WIDTH),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.width(ROUTE_COLUMN_GAP))
                Text(
                    text = "•",
                    modifier = Modifier.width(ROUTE_MARKER_WIDTH),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stop.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun RouteStopRow(
    time: String,
    label: String,
    marker: String,
    emphasize: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = time,
            modifier = Modifier.width(ROUTE_LEADING_COLUMN_WIDTH),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.width(ROUTE_COLUMN_GAP))
        Text(
            text = marker,
            modifier = Modifier.width(ROUTE_MARKER_WIDTH),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = if (emphasize) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = if (emphasize) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
    }
}

internal fun routeWalkingNavigationQuery(leg: TripLeg, displayDestination: String): String {
    val rawDestination = leg.to.trim()
    return if (looksCoordinatePair(rawDestination)) rawDestination else displayDestination.trim()
}

private fun openWalkingNavigation(context: android.content.Context, query: String) {
    val encoded = Uri.encode(query)
    val navigationIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("google.navigation:q=$encoded&mode=w")
    )
    val fallbackIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("geo:0,0?q=$encoded")
    )
    runCatching { context.startActivity(navigationIntent) }
        .onFailure { runCatching { context.startActivity(fallbackIntent) } }
}

private val ROUTE_LEADING_COLUMN_WIDTH = 60.dp
private val ROUTE_COLUMN_GAP = 10.dp
private val ROUTE_MARKER_WIDTH = 18.dp
private val ROUTE_CONTENT_INDENT = ROUTE_LEADING_COLUMN_WIDTH + ROUTE_COLUMN_GAP + ROUTE_MARKER_WIDTH

internal fun sortTripsForRoute(trips: List<Trip>, mode: RouteSortMode): List<Trip> {
    val indexedTrips = trips.mapIndexed { index, trip -> index to trip }
    val comparator = when (mode) {
        RouteSortMode.EARLIEST -> compareBy<Pair<Int, Trip>>(
            { it.second.departure },
            { it.second.arrival },
            { it.second.duration },
            { it.second.changes },
            { it.first }
        )
        RouteSortMode.FASTEST -> compareBy<Pair<Int, Trip>>(
            { it.second.duration },
            { it.second.departure },
            { it.second.changes },
            { it.second.arrival },
            { it.first }
        )
        RouteSortMode.FEWEST_CHANGES -> compareBy<Pair<Int, Trip>>(
            { it.second.changes },
            { it.second.duration },
            { it.second.departure },
            { it.second.arrival },
            { it.first }
        )
        RouteSortMode.LEAST_WALKING -> compareBy<Pair<Int, Trip>>(
            { routeWalkingMillis(it.second) },
            { it.second.duration },
            { it.second.departure },
            { it.second.changes },
            { it.first }
        )
    }

    return indexedTrips.sortedWith(comparator).map { it.second }
}

internal fun routeWalkingMillis(trip: Trip): Long = trip.legs
    .asSequence()
    .filter(::isWalkingLeg)
    .sumOf { leg -> (leg.arrival - leg.departure).coerceAtLeast(0L) }

internal fun routeLegDurationMinutes(leg: TripLeg): Int =
    ((leg.arrival - leg.departure).coerceAtLeast(0L) / 60_000L).toInt()

internal data class RouteIntermediateStopDisplay(
    val name: String,
    val arrival: Long?
)

internal fun routeTransferMinutes(previous: TripLeg, current: TripLeg): Int? {
    if (current.sameVehicle) return null
    val differenceMillis = current.departure - previous.arrival
    if (differenceMillis < 60_000L) return null
    return (differenceMillis / 60_000L).toInt()
}

internal fun routeIntermediateStopsForDisplay(leg: TripLeg): List<RouteIntermediateStopDisplay> {
    val timedStops = leg.intermediateStops.orEmpty()
        .mapNotNull { stop ->
            val name = stop.name.trim()
            if (name.isBlank() || looksTechnicalRouteReference(name)) return@mapNotNull null
            RouteIntermediateStopDisplay(
                name = formatStopNameForDisplay(name),
                arrival = stop.arrival
            )
        }
    if (timedStops.isNotEmpty()) return timedStops.distinctBy { it.name.lowercase() }

    return leg.stopNames.orEmpty()
        .mapNotNull { rawName ->
            val name = rawName.trim()
            if (name.isBlank() || looksTechnicalRouteReference(name)) return@mapNotNull null
            RouteIntermediateStopDisplay(
                name = formatStopNameForDisplay(name),
                arrival = null
            )
        }
        .distinctBy { it.name.lowercase() }
}

internal fun routeEndpointLabel(endpoint: RouteEndpoint?, currentLocationLabel: String): String = when (endpoint) {
    RouteEndpoint.CurrentLocation -> currentLocationLabel
    is RouteEndpoint.Place -> endpoint.title
    null -> ""
}

internal fun isWalkingLeg(leg: TripLeg): Boolean {
    val mode = leg.mode?.trim()?.lowercase().orEmpty()
    val line = leg.line.trim().lowercase()
    return mode == "walk" || line == "walk" || line == "foot" || line == "zu fuß" ||
        (mode.isBlank() && (looksCoordinatePair(leg.from) || looksCoordinatePair(leg.to)))
}

internal fun tripEndpointDisplayLabel(raw: String, fallback: String? = null): String {
    val trimmed = raw.trim()
    if (trimmed.isBlank()) return fallback.orEmpty()
    if (looksTechnicalRouteReference(trimmed)) return fallback?.takeIf { it.isNotBlank() } ?: trimmed
    return formatStopNameForDisplay(trimmed)
}

internal fun looksTechnicalRouteReference(value: String): Boolean =
    looksCoordinatePair(value) || looksLikeProviderStopId(value)

internal fun looksCoordinatePair(value: String): Boolean =
    COORDINATE_PAIR_REGEX.matches(value.trim())

internal fun looksLikeProviderStopId(value: String): Boolean {
    val trimmed = value.trim()
    if (trimmed.matches(Regex("^\\d{6,}$"))) return true
    if (!trimmed.contains(' ') && trimmed.length >= 8 && trimmed.any { it.isDigit() } && trimmed.any { it == ':' || it == '_' || it == '-' }) return true
    return false
}

private val COORDINATE_PAIR_REGEX = Regex("^-?\\d{1,3}(?:\\.\\d+)?\\s*,\\s*-?\\d{1,3}(?:\\.\\d+)?$")
private val tripTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

private fun formatTripTime(epochMillis: Long): String =
    Instant.ofEpochMilli(epochMillis)
        .atZone(ZoneId.systemDefault())
        .format(tripTimeFormatter)
