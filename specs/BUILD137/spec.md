# Build 137 Specification — Alternate departures navigation split

Goal: move the existing Photon-based “departures at another location” flow off the Home screen into a dedicated protected page reachable from the Home overflow menu. Preserve the existing station-search and departure semantics exactly. Do not implement the RoutePlanner yet.

Acceptance: Home exposes Refresh + overflow menu, Settings remains reachable, alternate departures open on their own route, station search behaves as before, and leaving the route restores Home to current-location/Idle state without stale alternate results.
