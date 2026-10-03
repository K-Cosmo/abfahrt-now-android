# Plan
1. Add dedicated `alternate_departures` route under the existing mandatory API-key gate.
2. Replace Home settings icon with overflow menu containing alternate departures + Settings; keep Refresh direct.
3. Reuse the existing Photon station search and DepartureScreen logic in an explicit alternate-location screen mode.
4. Suppress current-location fetch while the alternate page awaits a station selection.
5. Reset shared ViewModel target/UI state to CurrentLocation/Idle when leaving the alternate route.
6. Update locales and normative `/doc`; keep Build 138 RoutePlanner work separate.
