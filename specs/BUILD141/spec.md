# Build 141 Specification — RoutePlanner Empty/Error/Coverage convergence

## Goal
Harden the already accepted RoutePlanner for empty and failed `/trips` outcomes without inventing unsupported coverage rules or transit geometry.

## User-visible requirements
- Empty route results are presented as a clear card rather than a passive line of text.
- Empty and error states offer a localized retry action using the existing `retry` resource.
- If `/trips` returns a non-blank region on an empty response, keep that region visible so the user can understand which provider region answered.
- Optional provider attribution is shown when supplied by the API.
- Successful route cards and Build-140 transfer/intermediate-stop behaviour remain unchanged.

## Observability requirements
- Debug logging distinguishes `/trips` success, empty and error outcomes without logging searched place labels.
- Empty/success logs retain provider region and trip count so future multi-region/coverage work is evidence-driven.

## Non-goals
- no synthetic out-of-coverage detection;
- no assumptions from HTTP 400 beyond the current OpenAPI contract;
- no transit polyline/map fabrication;
- no route ranking/profile selector;
- no API/model/dependency change;
- no new localized string keys.

## Evidence gate
`:app:testDebugUnitTest :app:assembleDebug`, then runtime smoke for normal success plus one empty/error path when practical.
