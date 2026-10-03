# Build 140 Specification — Route details / transfer clarity

## Goal
Refine the already accepted RoutePlanner presentation without changing the `/trips` contract or routing architecture.

## User-visible requirements
- A real transit-to-transit gap is shown as a localized transfer duration between the two legs.
- Access/egress walking legs must not inflate the visual transfer count.
- Transit legs with intermediate-stop names expose the existing stop count as an inline expand/collapse control.
- Expanded intermediate stops show cleaned stop names and arrival times when the API provides them.
- If only a count exists, keep the count visible but do not offer a fake expansion.
- All new user-facing/accessibility strings must exist in all 22 locale resource sets.

## Non-goals
- no API or model schema change;
- no new dependency;
- no transit map/polyline fabrication;
- no route ranking or departure-time feature;
- no changes to Departure sorting/dedup, ORS, MapLibre or mandatory API-key handling.

## Evidence gate
`:app:testDebugUnitTest :app:assembleDebug`, followed by a real route with at least one transit transfer and a leg containing intermediate stops.
