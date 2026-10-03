# Build 142 specification — Route UI convergence

## Goal
Polish the accepted RoutePlanner without changing routing semantics.

## User-visible requirements
1. Do not display the provider `region` on the RoutePlanner result or empty state.
2. Reuse the same transport-mode symbols already used in departure cards for transit route legs (U-Bahn, S-Bahn, tram, bus, regional, express, ferry).
3. Keep the current walking presentation, line badge colors, transfer durations and expandable intermediate stops.
4. Keep optional provider attribution visible when supplied.

## Non-goals
- no saved Home/Work places yet (Build 143)
- no route sorting yet (Build 144)
- no future departure/arrival time because current `/trips` contract has no such request parameter
- no API, dependency, ORS, MapLibre or departure-list logic change
