# Build 134 Specification — abfahrt.now Contract / walkSeconds Observation

## Goal
Capture and verify the current `/departures` JSON contract while observing the optional provider-side `Station.walkSeconds` field without changing product behavior.

## Requirements
- `versionCode = 1340`.
- Keep minSdk 34 / compileSdk 37 / targetSdk 37 and all Build-133 dependencies unchanged.
- Extend the existing `Station` model with nullable `walkSeconds` matching the current project OpenAPI.
- Add focused Gson regression tests for:
  - `Departure.timestamp` as millisecond `Long`;
  - `Departure.stop`;
  - `Station.distance`;
  - optional departure fields;
  - optional `Station.walkSeconds`;
  - ignoring unknown forward-compatible JSON fields.
- Add one compact runtime diagnostic showing real `walkSeconds` coverage in the effective initial station list.
- Do not use `walkSeconds` for UI, sorting, reachability, dedup, ORS skipping, WALK/BIKE duration or route preview.
- No new dependency and no API request/endpoint change.

## Acceptance
A real `testDebugUnitTest` and `assembleDebug` must pass. Runtime evidence must contain an `AbfahrtContract walkSeconds coverage=X/Y` line during a normal departures refresh. The coverage value is observational; zero coverage is a valid result.
