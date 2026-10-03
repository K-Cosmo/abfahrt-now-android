# Build 149 Specification — HERE detail location map

## Problem
A departure already classified as HERE (`stationDistance <= 35 m`) displays `Hier` and `Haltestelle/Station erreicht`, but the details sheet can still load and draw an ORS walking route. That is semantically contradictory and creates unnecessary network work.

## Required behavior
- HERE keeps the existing threshold and 0 m / reached semantics.
- HERE must never invoke the ORS route-preview loader.
- The detail map remains visible when origin + stop coordinates are available, even without an ORS key.
- The HERE map shows only origin and stop markers; no route source/polyline.
- The HERE section title describes a location, not a route.
- Non-HERE route preview behavior is unchanged.
- No dependency/API/persistence/sorting/performance change.
