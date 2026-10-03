# Build 143 plan

1. Extend `AppPreferences`/DataStore with nullable Home/Work `SavedPlace` values.
2. Add a small `SavedPlacesViewModel` using the existing `GeocodingRepository.searchPlaces()` and DataStore repository.
3. Add a protected `saved_places` navigation destination and menu entry.
4. Expose configured Home/Work in both RoutePlanner endpoint search panels.
5. Rework RoutePlanner leg identity into the same fixed vertical symbol+badge column used by `DepartureCard`.
6. Update all 22 locale sets, `/doc`, evidence and source gates.
7. Acceptance: real `testDebugUnitTest + assembleDebug`, set/change/remove Home/Work, reuse both in From/To, visual check of vertical transport column.
