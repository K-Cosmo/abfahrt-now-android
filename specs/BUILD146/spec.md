# Build 146 Specification — Routing UX / local search convergence

## Goal
Refine route leg readability and make route search behave more locally without changing the transit-routing contract.

## Requirements
1. Route-leg times share the fixed leading visual column with the mode icon/line badge and are centered in that column.
2. Stop names, direction text, details and intermediate-stop content use a consistent content axis to the right of the leading column.
3. First and final walking legs expose a top-right action to open walking navigation to the leg destination. No new navigation SDK/dependency.
4. Saved Home/Work quick rows are visible only while the route search field is empty; they disappear immediately when text is entered.
5. General Photon search adds the current city from `DepartureResponse.city` when no postcode or explicit comma-locality is present. Existing coordinate bias remains.
6. A postcode or explicit `query, locality` must bypass city appending.
7. No changes to `/trips`, ORS, MapLibre, DataStore schema or Build-145 route sorting semantics.
