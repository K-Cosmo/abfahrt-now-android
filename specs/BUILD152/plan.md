# Build 152 Plan

1. Bump app `versionCode` from 1510 to 1520; keep `versionName 1.1.0`.
2. Fix startup preference readiness with one explicit app-level access path:
   - `AccessGateViewModel` alone decides when the startup destination may be chosen;
   - its nullable initial state stays closed until the first real `UserPreferencesRepository.preferencesFlow` emission;
   - `DepartureViewModel` and `RoutePlannerViewModel` are instantiated only after that access snapshot exists;
   - the existing `DepartureViewModel.preferences` remains feature/settings state and does not participate in startup navigation.
3. Simplify onboarding typography:
   - use start alignment instead of justification;
   - use spacing/layout to separate the required-key explanation from the optional ORS note without adding a technical tutorial.
4. Compact RoutePlanner endpoint input:
   - retain the existing `RouteEndpointField` search behavior;
   - render origin/destination inside one bordered surface with a divider;
   - place swap action compactly at the trailing side rather than as a full vertical row;
   - show the existing search panel only for the focused endpoint.
5. Settings footer cleanup:
   - collapse redundant top-level spacers around the footer divider;
   - delete the unused legacy footer/legal composables and obsolete import.
6. Converge `/doc`, run CI and collect real-device UI/startup evidence before acceptance.
