package now.abfahrt.transit.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import now.abfahrt.transit.data.model.DepartureUiState
import now.abfahrt.transit.ui.viewmodel.DepartureViewModel
import now.abfahrt.transit.util.StartupTrace

/**
 * Read-only Build-153 observer over existing public ViewModel state.
 * It deliberately does not trigger refreshes or alter the production data path.
 */
@Composable
internal fun StartupDiagnosticsObserver(viewModel: DepartureViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var loadingStartedUptimeMs by remember { mutableStateOf<Long?>(null) }
    var successEmissionCount by remember { mutableStateOf(0) }
    var firstFinalSeen by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        StartupTrace.mark(
            event = "departure_observer_attached",
            details = "originCached=${viewModel.getRoutePreviewOrigin() != null}"
        )
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            DepartureUiState.Idle -> {
                StartupTrace.mark("departure_state_idle")
            }

            DepartureUiState.Loading -> {
                loadingStartedUptimeMs = StartupTrace.nowUptimeMs()
                StartupTrace.mark("departure_state_loading")
            }

            is DepartureUiState.Success -> {
                successEmissionCount += 1
                val walkingMetrics = state.response.departures.count { departure ->
                    departure.walkDistance != null || departure.walkDurationSeconds != null || departure.usesApproximateDistance
                }
                val details = buildString {
                    append("seq=$successEmissionCount")
                    append(" final=${state.response.isFinal}")
                    append(" departures=${state.response.departures.size}")
                    append(" filtered=${state.filtered.size}")
                    append(" walkingMetrics=$walkingMetrics")
                }
                StartupTrace.mark("departure_state_success", details)

                loadingStartedUptimeMs?.let { started ->
                    StartupTrace.duration(
                        event = "departure_loading_to_success",
                        startedUptimeMs = started,
                        details = "final=${state.response.isFinal} departures=${state.response.departures.size}"
                    )
                    if (state.response.isFinal) {
                        loadingStartedUptimeMs = null
                    }
                }

                if (state.response.isFinal && !firstFinalSeen) {
                    firstFinalSeen = true
                    StartupTrace.mark(
                        event = "departure_first_final_success",
                        details = "departures=${state.response.departures.size} filtered=${state.filtered.size} walkingMetrics=$walkingMetrics"
                    )
                }
            }

            is DepartureUiState.Error -> {
                StartupTrace.mark("departure_state_error")
                loadingStartedUptimeMs = null
            }
        }
    }
}
