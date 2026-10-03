# Build 152 Plan

1. Bump app `versionCode` from 1510 to 1520; keep `versionName 1.1.0`.
2. Fix preference readiness at the source of the navigation decision:
   - keep the existing preference StateFlow/API;
   - prevent its synthetic default value from marking preferences as loaded;
   - only the first actual upstream preference emission opens the navigation gate.
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
