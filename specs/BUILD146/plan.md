# Build 146 Plan

1. Preserve accepted Build-145 source as baseline and bump `versionCode` to 1460.
2. Refactor only RoutePlanner presentation geometry; reuse existing badge/mode data.
3. Use implicit Android navigation intent for first/final walking legs with `geo:` fallback.
4. Tighten Home/Work quick-row visibility in both Home route search and RoutePlanner endpoint search.
5. Extend `GeocodingRepository.searchPlaces` with optional city hint and a pure query-scoping helper.
6. Feed city hint from the already loaded current-location `DepartureResponse.city`; no reverse-geocoding call.
7. Add focused JVM tests and 22-locale string parity for the navigation content description.
8. Update `/doc` and evidence; real Android build/runtime remains external acceptance gate.
