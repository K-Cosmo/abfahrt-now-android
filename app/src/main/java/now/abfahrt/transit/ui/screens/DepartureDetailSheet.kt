package now.abfahrt.transit.ui.screens

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsTransit
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.res.Resources
import androidx.compose.ui.viewinterop.AndroidView
import now.abfahrt.transit.BuildConfig
import now.abfahrt.transit.R
import now.abfahrt.transit.data.model.Departure
import now.abfahrt.transit.data.model.OrsTravelMode
import now.abfahrt.transit.data.model.RoutePreviewData
import now.abfahrt.transit.data.model.RoutePreviewDestination
import now.abfahrt.transit.data.model.TransportMode
import now.abfahrt.transit.data.model.displayDistanceMeters
import now.abfahrt.transit.data.model.displayWalkDurationSeconds
import now.abfahrt.transit.data.model.isHereOverride
import now.abfahrt.transit.ui.components.badgeTextColor
import now.abfahrt.transit.ui.components.formatDirectionNameForDisplay
import now.abfahrt.transit.ui.components.formatStopNameForDisplay
import now.abfahrt.transit.ui.components.inferModeFromLine
import now.abfahrt.transit.ui.components.lineBadgeColor
import now.abfahrt.transit.ui.theme.CancelledGrey
import now.abfahrt.transit.ui.theme.DelayRed
import now.abfahrt.transit.ui.theme.OnTimeGreen
import now.abfahrt.transit.util.DepartureFollowUpTimes
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory.circleColor
import org.maplibre.android.style.layers.PropertyFactory.circleRadius
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeColor
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeWidth
import org.maplibre.android.style.layers.PropertyFactory.lineCap
import org.maplibre.android.style.layers.PropertyFactory.lineColor
import org.maplibre.android.style.layers.PropertyFactory.lineJoin
import org.maplibre.android.style.layers.PropertyFactory.lineWidth
import org.maplibre.android.style.sources.GeoJsonSource
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

private const val ROUTE_PREVIEW_STYLE_URL = "asset://route_preview_style.json"
private const val ROUTE_SOURCE_ID = "route-source"
private const val ROUTE_LAYER_ID = "route-layer"
private const val START_SOURCE_ID = "route-start-source"
private const val START_LAYER_ID = "route-start-layer"
private const val END_SOURCE_ID = "route-end-source"
private const val END_LAYER_ID = "route-end-layer"
private const val ROUTE_PREVIEW_LOG_TAG = "AbfahrtRoutePreview"

private sealed interface RoutePreviewUiState {
    data object Hidden : RoutePreviewUiState
    data object Loading : RoutePreviewUiState
    data object Unavailable : RoutePreviewUiState
    data class Ready(val data: RoutePreviewData) : RoutePreviewUiState
}

internal fun shouldLoadRoutePreview(
    isHere: Boolean,
    orsApiKeyConfigured: Boolean,
    destinationAvailable: Boolean
): Boolean = !isHere && orsApiKeyConfigured && destinationAvailable

private fun debugRoutePreview(message: String) {
    if (BuildConfig.DEBUG) {
        Log.d(ROUTE_PREVIEW_LOG_TAG, message)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepartureDetailSheet(
    departure: Departure,
    followUpDepartures: List<Departure> = emptyList(),
    orsApiKeyConfigured: Boolean,
    orsTravelMode: OrsTravelMode = OrsTravelMode.WALK,
    routePreviewOrigin: Pair<Double, Double>? = null,
    routePreviewDestination: RoutePreviewDestination? = null,
    loadRoutePreview: suspend (RoutePreviewDestination) -> RoutePreviewData? = { null },
    onDismiss: () -> Unit,
    onActivateWalkFeature: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val mode = TransportMode.fromApiValue(departure.mode) ?: inferModeFromLine(departure.line)
    val badgeColor = lineBadgeColor(departure.line, mode)
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val departureTime = timeFormat.format(Date(departure.timestamp))
    val detailInValue = remember(departure, followUpDepartures) {
        val values = followUpDepartures
            .ifEmpty { listOf(departure) }
            .map { DepartureFollowUpTimes.formatCompactValue(it) }
            .distinct()
        values.joinToString(", ")
    }
    val isHere = departure.isHereOverride()
    val displayedDistance = departure.displayDistanceMeters()
    val displayedWalkDurationSeconds = departure.displayWalkDurationSeconds()
    val hereReachedText = when (mode) {
        TransportMode.BUS, TransportMode.TRAM, TransportMode.FERRY -> stringResource(R.string.detail_here_stop_reached)
        else -> stringResource(R.string.detail_here_station_reached)
    }
    val normalizedPlatform = departure.platform
        ?.substringBefore(" (")
        ?.trim()
        .orEmpty()
    val platformLabel = if ((departure.mode ?: "").equals("bus", ignoreCase = true)) {
        stringResource(R.string.stop)
    } else {
        stringResource(R.string.detail_platform)
    }

    val routePreviewState by produceState<RoutePreviewUiState>(
        initialValue = RoutePreviewUiState.Hidden,
        orsApiKeyConfigured,
        orsTravelMode,
        routePreviewDestination?.stationId,
        routePreviewDestination?.lat,
        routePreviewDestination?.lon,
        departure.stop,
        isHere
    ) {
        val destination = routePreviewDestination
        val shouldLoadPreview = shouldLoadRoutePreview(
            isHere = isHere,
            orsApiKeyConfigured = orsApiKeyConfigured,
            destinationAvailable = destination != null
        )
        value = if (!shouldLoadPreview) {
            when {
                isHere -> {
                    debugRoutePreview("state=Hidden reason=here_no_route stop='${departure.stop}'")
                    RoutePreviewUiState.Hidden
                }
                !orsApiKeyConfigured -> {
                    debugRoutePreview("state=Hidden reason=ors_key_missing stop='${departure.stop}'")
                    RoutePreviewUiState.Hidden
                }
                else -> {
                    debugRoutePreview("state=Unavailable reason=missing_destination stop='${departure.stop}'")
                    RoutePreviewUiState.Unavailable
                }
            }
        } else {
            val resolvedDestination = requireNotNull(destination)
            debugRoutePreview("state=Loading stationId=${resolvedDestination.stationId} lat=${resolvedDestination.lat} lon=${resolvedDestination.lon} mode=$orsTravelMode")
            value = RoutePreviewUiState.Loading
            val preview = loadRoutePreview(resolvedDestination)
            if (preview != null) {
                debugRoutePreview("state=Ready stationId=${resolvedDestination.stationId} bbox=${preview.bbox?.joinToString()} hasGeoJson=${preview.geoJson.isNotBlank()} routeDistance=${preview.routeDistanceMeters} routeDuration=${preview.routeDurationSeconds}")
                RoutePreviewUiState.Ready(preview)
            } else {
                debugRoutePreview("state=Unavailable reason=repository_returned_null stationId=${resolvedDestination.stationId}")
                RoutePreviewUiState.Unavailable
            }
        }
    }


    val hereMapData = if (isHere) {
        val origin = routePreviewOrigin
        val destination = routePreviewDestination
        if (origin != null && destination != null) {
            RoutePreviewData(
                geoJson = "",
                startLat = origin.first,
                startLon = origin.second,
                endLat = destination.lat,
                endLon = destination.lon
            )
        } else {
            null
        }
    } else {
        null
    }

    val previewMetric = (routePreviewState as? RoutePreviewUiState.Ready)?.data
    val previewRouteDistance = previewMetric?.routeDistanceMeters
        ?.takeIf { !isHere && it > 0 && (departure.usesApproximateDistance || displayedWalkDurationSeconds == null) }
    val previewRouteDuration = previewMetric?.routeDurationSeconds
        ?.takeIf { !isHere && it >= 0 && (departure.usesApproximateDistance || displayedWalkDurationSeconds == null) }
    val effectiveDisplayedDistance = previewRouteDistance ?: displayedDistance
    val effectiveWalkStatusText = when {
        isHere -> hereReachedText
        previewRouteDuration != null -> formatWalkDuration(context = context, durationSeconds = previewRouteDuration)
        displayedWalkDurationSeconds != null && displayedWalkDurationSeconds >= 0 -> formatWalkDuration(context = context, durationSeconds = displayedWalkDurationSeconds)
        departure.usesApproximateDistance -> stringResource(R.string.detail_walk_time_unavailable)
        orsApiKeyConfigured -> stringResource(R.string.detail_walk_time_pending)
        else -> stringResource(R.string.detail_walk_time_locked)
    }
    val effectiveDistanceIsResolvedRoute = previewRouteDistance != null || (departure.walkDistance != null && !departure.usesApproximateDistance)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    color = badgeColor,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = departure.line,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = badgeTextColor(departure.line, mode, badgeColor)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = formatDirectionNameForDisplay(departure.direction),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = formatStopNameForDisplay(departure.stop),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            HorizontalDivider()
            Spacer(Modifier.height(4.dp))

            DetailRow(
                icon = Icons.Default.Schedule,
                label = stringResource(R.string.detail_departure),
                value = departureTime,
                valueColor = MaterialTheme.colorScheme.onSurface
            )
            DetailRow(
                icon = Icons.Default.Timer,
                label = stringResource(R.string.detail_in),
                value = detailInValue,
                valueColor = MaterialTheme.colorScheme.onSurface
            )

            when {
                departure.cancelled -> DetailRow(
                    icon = Icons.Default.Cancel,
                    label = stringResource(R.string.detail_status),
                    value = stringResource(R.string.detail_status_cancelled),
                    valueColor = CancelledGrey
                )
                departure.delay > 0 -> DetailRow(
                    icon = Icons.Default.Warning,
                    label = stringResource(R.string.detail_delay),
                    value = stringResource(R.string.detail_delay_value, departure.delay),
                    valueColor = DelayRed
                )
                departure.delay < 0 -> DetailRow(
                    icon = Icons.Default.CheckCircle,
                    label = stringResource(R.string.detail_deviation),
                    value = stringResource(R.string.detail_early_value, departure.delay),
                    valueColor = OnTimeGreen
                )
                else -> DetailRow(
                    icon = Icons.Default.CheckCircle,
                    label = stringResource(R.string.detail_status),
                    value = stringResource(R.string.on_time_cap),
                    valueColor = OnTimeGreen
                )
            }

            if (normalizedPlatform.isNotBlank()) {
                DetailRow(
                    icon = Icons.Default.Train,
                    label = platformLabel,
                    value = normalizedPlatform,
                    valueColor = MaterialTheme.colorScheme.onSurface
                )
            }

            departure.occupancy?.takeIf { it.isNotBlank() }?.let { occ ->
                val (label, color) = translateOccupancy(occ)
                DetailRow(
                    icon = Icons.Default.People,
                    label = stringResource(R.string.detail_occupancy),
                    value = label,
                    valueColor = color
                )
            }

            if (effectiveDisplayedDistance != null && effectiveDisplayedDistance < Int.MAX_VALUE) {
                DetailRow(
                    icon = if (isHere || effectiveDistanceIsResolvedRoute) {
                        Icons.Default.NearMe
                    } else {
                        Icons.Default.Flight
                    },
                    label = stringResource(R.string.detail_distance),
                    value = if (isHere) {
                        stringResource(R.string.distance_here)
                    } else if (effectiveDisplayedDistance < 1000) {
                        stringResource(R.string.distance_m, effectiveDisplayedDistance)
                    } else {
                        stringResource(R.string.distance_km, effectiveDisplayedDistance / 1000.0)
                    },
                    valueColor = MaterialTheme.colorScheme.onSurface
                )
            }

            DetailRow(
                icon = if (orsTravelMode == OrsTravelMode.BIKE) Icons.AutoMirrored.Filled.DirectionsBike else Icons.AutoMirrored.Filled.DirectionsWalk,
                label = stringResource(orsTravelMode.detailLabelRes),
                value = effectiveWalkStatusText,
                valueColor = MaterialTheme.colorScheme.onSurface,
                onValueClick = if (!orsApiKeyConfigured && (displayedWalkDurationSeconds ?: 0) <= 0 && !isHere) onActivateWalkFeature else null
            )

            mode?.let {
                DetailRow(
                    icon = Icons.Default.DirectionsTransit,
                    label = stringResource(R.string.detail_type),
                    value = "${it.emoji} ${stringResource(it.labelRes)}",
                    valueColor = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            if (isHere) {
                Text(
                    text = stringResource(R.string.detail_here_map_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(10.dp))
                if (hereMapData != null) {
                    RoutePreviewMapCard(
                        routePreviewData = hereMapData,
                        drawRoute = false,
                        focusHere = true
                    )
                } else {
                    RoutePreviewUnavailableCard()
                }
                Spacer(Modifier.height(16.dp))
            } else if (orsApiKeyConfigured) {
                Text(
                    text = stringResource(R.string.detail_route_preview_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(10.dp))
                when (val previewState = routePreviewState) {
                    RoutePreviewUiState.Hidden -> Unit
                    RoutePreviewUiState.Loading -> RoutePreviewLoadingCard()
                    RoutePreviewUiState.Unavailable -> RoutePreviewUnavailableCard()
                    is RoutePreviewUiState.Ready -> RoutePreviewMapCard(previewState.data)
                }
                Spacer(Modifier.height(16.dp))
            }

            val mapQueryStop = stringResource(R.string.map_query_stop_prefix, departure.stop)
            Button(
                onClick = {
                    val query = Uri.encode(mapQueryStop)
                    val geoUri = Uri.parse("geo:0,0?q=$query")
                    val intent = Intent(Intent.ACTION_VIEW, geoUri)
                    runCatching { context.startActivity(intent) }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Icon(Icons.Default.Map, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.open_in_map), fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.map_open_hint),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun RoutePreviewLoadingCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 1.dp,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(184.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
                Spacer(Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.detail_route_preview_loading),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RoutePreviewUnavailableCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 1.dp,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(184.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.detail_route_preview_unavailable),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


@Composable
private fun RoutePreviewMapCard(
    routePreviewData: RoutePreviewData,
    drawRoute: Boolean = true,
    focusHere: Boolean = false
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 1.dp,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        RoutePreviewMap(
            routePreviewData = routePreviewData,
            drawRoute = drawRoute,
            focusHere = focusHere,
            modifier = Modifier
                .fillMaxWidth()
                .height(184.dp)
        )
    }
}

@Composable
private fun RoutePreviewMap(
    routePreviewData: RoutePreviewData,
    drawRoute: Boolean = true,
    focusHere: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply {
            onCreate(null)
            onStart()
            onResume()
            getMapAsync { map ->
                debugRoutePreview("map=getMapAsync initial")
                map.uiSettings.apply {
                    isCompassEnabled = false
                    isLogoEnabled = false
                    isAttributionEnabled = true
                    isRotateGesturesEnabled = false
                    isTiltGesturesEnabled = false
                    isScrollGesturesEnabled = true
                    isZoomGesturesEnabled = true
                    isDoubleTapGesturesEnabled = true
                    isQuickZoomGesturesEnabled = true
                }
                map.setStyle(Style.Builder().fromUri(ROUTE_PREVIEW_STYLE_URL)) { style ->
                    debugRoutePreview("map=style_loaded initial source=$ROUTE_PREVIEW_STYLE_URL")
                    applyRoutePreviewStyle(style, routePreviewData, drawRoute)
                    post { moveCameraForPreview(map, routePreviewData, focusHere) }
                }
            }
        }
    }

    DisposableEffect(mapView) {
        onDispose {
            mapView.onPause()
            mapView.onStop()
            mapView.onDestroy()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { mapView },
        update = { currentMapView ->
            currentMapView.getMapAsync { map ->
                val style = map.style
                if (style == null) {
                    debugRoutePreview("map=style_missing update=set_style")
                    map.setStyle(Style.Builder().fromUri(ROUTE_PREVIEW_STYLE_URL)) { loadedStyle ->
                        debugRoutePreview("map=style_loaded update source=$ROUTE_PREVIEW_STYLE_URL")
                        applyRoutePreviewStyle(loadedStyle, routePreviewData, drawRoute)
                        currentMapView.post { moveCameraForPreview(map, routePreviewData, focusHere) }
                    }
                } else {
                    debugRoutePreview("map=style_reused update")
                    applyRoutePreviewStyle(style, routePreviewData, drawRoute)
                    currentMapView.post { moveCameraForPreview(map, routePreviewData, focusHere) }
                }
            }
        }
    )
}

private fun applyRoutePreviewStyle(
    style: Style,
    routePreviewData: RoutePreviewData,
    drawRoute: Boolean
) {
    debugRoutePreview("map=apply_style drawRoute=$drawRoute bbox=${routePreviewData.bbox?.joinToString()} geoJsonBytes=${routePreviewData.geoJson.length}")
    if (drawRoute) {
        val routeSource = style.getSourceAs<GeoJsonSource>(ROUTE_SOURCE_ID)
        if (routeSource == null) {
            style.addSource(GeoJsonSource(ROUTE_SOURCE_ID, routePreviewData.geoJson))
            style.addLayer(
                LineLayer(ROUTE_LAYER_ID, ROUTE_SOURCE_ID).withProperties(
                    lineColor("#2563EB"),
                    lineWidth(5f),
                    lineCap(Property.LINE_CAP_ROUND),
                    lineJoin(Property.LINE_JOIN_ROUND)
                )
            )
        } else {
            routeSource.setGeoJson(routePreviewData.geoJson)
        }
    }

    val startGeoJson = buildPointFeatureCollection(routePreviewData.startLon, routePreviewData.startLat)
    val endGeoJson = buildPointFeatureCollection(routePreviewData.endLon, routePreviewData.endLat)

    val startSource = style.getSourceAs<GeoJsonSource>(START_SOURCE_ID)
    if (startSource == null) {
        style.addSource(GeoJsonSource(START_SOURCE_ID, startGeoJson))
        style.addLayer(
            CircleLayer(START_LAYER_ID, START_SOURCE_ID).withProperties(
                circleRadius(5f),
                circleColor("#2563EB"),
                circleStrokeColor("#FFFFFF"),
                circleStrokeWidth(2f)
            )
        )
    } else {
        startSource.setGeoJson(startGeoJson)
    }

    val endSource = style.getSourceAs<GeoJsonSource>(END_SOURCE_ID)
    if (endSource == null) {
        style.addSource(GeoJsonSource(END_SOURCE_ID, endGeoJson))
        style.addLayer(
            CircleLayer(END_LAYER_ID, END_SOURCE_ID).withProperties(
                circleRadius(6f),
                circleColor("#DC2626"),
                circleStrokeColor("#FFFFFF"),
                circleStrokeWidth(2f)
            )
        )
    } else {
        endSource.setGeoJson(endGeoJson)
    }
}

private fun moveCameraForPreview(
    map: org.maplibre.android.maps.MapLibreMap,
    routePreviewData: RoutePreviewData,
    focusHere: Boolean
) {
    if (focusHere) {
        val centerLat = (routePreviewData.startLat + routePreviewData.endLat) / 2.0
        val centerLon = (routePreviewData.startLon + routePreviewData.endLon) / 2.0
        map.moveCamera(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.Builder()
                    .target(LatLng(centerLat, centerLon))
                    .zoom(17.5)
                    .build()
            )
        )
    } else {
        moveCameraToRoute(map, routePreviewData)
    }
}

private fun moveCameraToRoute(map: org.maplibre.android.maps.MapLibreMap, routePreviewData: RoutePreviewData) {
    val bounds = buildLatLngBounds(routePreviewData)
    val density = Resources.getSystem().displayMetrics.density
    runCatching {
        debugRoutePreview("map=move_camera bounds_ready")
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 36), 250)
    }.onFailure {
        debugRoutePreview("map=move_camera fallback reason=${it.javaClass.simpleName}")
        val centerLat = (routePreviewData.startLat + routePreviewData.endLat) / 2.0
        val centerLon = (routePreviewData.startLon + routePreviewData.endLon) / 2.0
        map.moveCamera(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.Builder()
                    .target(LatLng(centerLat, centerLon))
                    .zoom(15.0)
                    .build()
            )
        )
    }
}

private fun buildLatLngBounds(routePreviewData: RoutePreviewData): LatLngBounds {
    val builder = LatLngBounds.Builder()
    val bbox = routePreviewData.bbox
    if (bbox != null && bbox.size == 4) {
        builder.include(LatLng(bbox[1], bbox[0]))
        builder.include(LatLng(bbox[3], bbox[2]))
    }
    builder.include(LatLng(routePreviewData.startLat, routePreviewData.startLon))
    builder.include(LatLng(routePreviewData.endLat, routePreviewData.endLon))
    return builder.build()
}

private fun buildPointFeatureCollection(lon: Double, lat: Double): String =
    """{"type":"FeatureCollection","features":[{"type":"Feature","properties":{},"geometry":{"type":"Point","coordinates":[${lon},${lat}]}}]}"""

@Composable
private fun translateOccupancy(raw: String): Pair<String, Color> {
    val key = raw.trim().lowercase()
    return when (key) {
        "low", "gering" -> Pair(stringResource(R.string.occupancy_low), Color(0xFF388E3C))
        "medium", "mid", "moderate", "mittel" -> Pair(stringResource(R.string.occupancy_medium), Color(0xFFF57F17))
        "high", "hoch" -> Pair(stringResource(R.string.occupancy_high), Color(0xFFD32F2F))
        "full", "voll", "standing only" -> Pair(stringResource(R.string.occupancy_full), Color(0xFFB71C1C))
        "unknown", "" -> Pair("–", Color(0xFF9E9E9E))
        else -> Pair(raw, Color(0xFF546E7A))
    }
}

@Composable
private fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color,
    onValueClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (onValueClick != null) MaterialTheme.colorScheme.primary else valueColor,
            textDecoration = if (onValueClick != null) TextDecoration.Underline else null,
            fontSize = 15.sp,
            modifier = if (onValueClick != null) Modifier.clickable(onClick = onValueClick) else Modifier
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
}

private fun formatWalkDuration(context: android.content.Context, durationSeconds: Int): String {
    val minutes = kotlin.math.max(0, durationSeconds / 60)
    return context.getString(R.string.walk_duration_minutes, minutes)
}
