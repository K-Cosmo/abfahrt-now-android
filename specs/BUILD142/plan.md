# Build 142 plan

1. Bump `versionCode` to 1420.
2. Remove RoutePlanner rendering of `TripResponse.region` from success/empty UI only.
3. Keep `TripResponse.region` in the model/repository/logging for diagnostics and API context.
4. Render `TransportMode.emoji` ahead of each non-walking route-leg badge, reusing existing mode inference and the existing enum.
5. Do not add locale strings or dependencies.
6. Run all repository static gates; real Gradle/unit/runtime acceptance remains external evidence.
