package now.abfahrt.transit.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.*
import now.abfahrt.transit.ui.viewmodel.DepartureViewModel
import now.abfahrt.transit.ui.viewmodel.RoutePlannerViewModel
import now.abfahrt.transit.ui.viewmodel.SavedPlacesViewModel

/**
 * App-level navigation.
 *
 * Routing logic:
 *  • abfahrt.now access requires completed onboarding + a non-blank API key.
 *  • ORS remains optional.
 *  • A missing/rejected key re-enters onboarding from every protected app route.
 *  • Alternate-location departures are a dedicated secondary route from Build 137.
 */
@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val viewModel: DepartureViewModel = hiltViewModel()
    val routePlannerViewModel: RoutePlannerViewModel = hiltViewModel()
    val routePlannerState by routePlannerViewModel.uiState.collectAsState()
    val prefs by viewModel.preferences.collectAsState()
    val preferencesLoaded by viewModel.preferencesLoaded.collectAsState()

    val hasRequiredAccess = hasRequiredAbfahrtAccess(
        onboardingCompleted = prefs.onboardingCompleted,
        apiKey = prefs.apiKey
    )

    if (!preferencesLoaded) return

    NavHost(
        navController    = navController,
        startDestination = if (hasRequiredAccess) "departures" else "onboarding"
    ) {
        composable("onboarding") {
            OnboardingScreen(
                initialApiKey = prefs.apiKey,
                initialOrsApiKey = prefs.orsApiKey,
                initialApiKeyRejected = prefs.apiKey.isNotBlank() && !prefs.onboardingCompleted,
                onContinue = { apiKey, orsApiKey ->
                    if (apiKey.isNotBlank()) {
                        viewModel.completeOnboarding(apiKey = apiKey, orsApiKey = orsApiKey)
                    }
                }
            )
        }
        composable("departures") {
            DepartureScreen(
                viewModel = viewModel,
                screenMode = DepartureScreenMode.Home,
                routeDestinationSearchUiState = routePlannerState.destinationSearch,
                onRouteDestinationQueryChange = { query ->
                    routePlannerViewModel.updateCurrentLocationHint(viewModel.getRoutePreviewOrigin())
                    routePlannerViewModel.updateDestinationQuery(query)
                },
                onRouteDestinationSelected = { result ->
                    routePlannerViewModel.prepareDestination(
                        result = result,
                        currentLocation = viewModel.getRoutePreviewOrigin()
                    )
                    navController.navigate("route_planner")
                },
                onRouteSavedDestinationSelected = { place ->
                    routePlannerViewModel.prepareDestination(
                        place = place,
                        currentLocation = viewModel.getRoutePreviewOrigin()
                    )
                    navController.navigate("route_planner")
                },
                onDismissRouteDestinationSearch = routePlannerViewModel::clearDestinationSearch,
                onOpenAlternateDepartures = {
                    routePlannerViewModel.clearDestinationSearch()
                    navController.navigate("alternate_departures")
                },
                onOpenSavedPlaces = {
                    routePlannerViewModel.clearDestinationSearch()
                    navController.navigate("saved_places")
                }
            )
        }
        composable("alternate_departures") {
            val leaveAlternate: () -> Unit = {
                viewModel.leaveAlternateLocationMode()
                navController.popBackStack()
            }
            BackHandler(onBack = leaveAlternate)
            DepartureScreen(
                viewModel = viewModel,
                screenMode = DepartureScreenMode.AlternateLocation,
                onNavigateBack = leaveAlternate
            )
        }
        composable("saved_places") {
            val savedPlacesViewModel: SavedPlacesViewModel = hiltViewModel()
            LaunchedEffect(Unit) {
                savedPlacesViewModel.updateLocationHint(viewModel.getRoutePreviewOrigin())
            }
            SavedPlacesScreen(
                viewModel = savedPlacesViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("route_planner") {
            LaunchedEffect(Unit) {
                routePlannerViewModel.updateCurrentLocationHint(viewModel.getRoutePreviewOrigin())
            }
            RoutePlannerScreen(
                viewModel = routePlannerViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }

    LaunchedEffect(hasRequiredAccess) {
        val route = navController.currentDestination?.route
        when {
            hasRequiredAccess && route == "onboarding" -> {
                navController.navigate("departures") {
                    popUpTo("onboarding") { inclusive = true }
                }
            }
            !hasRequiredAccess && route in setOf("departures", "alternate_departures", "saved_places", "route_planner") -> {
                if (route == "alternate_departures") {
                    viewModel.leaveAlternateLocationMode()
                }
                navController.navigate("onboarding") {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }
}
