# Build 152 Tasks

## Implementation
- [x] `versionCode = 1520`.
- [x] Fix real preference-readiness gate; no synthetic-default startup decision.
- [x] Keep startup navigation on one explicit AccessGate path; create protected feature ViewModels only after real preferences exist.
- [x] Onboarding body copy left aligned; required/optional information visually separated.
- [x] Tighten onboarding spacing after real-device screenshot without removing compact-display scrollability.
- [x] Compact shared RoutePlanner origin/destination container with trailing swap action.
- [x] Preserve Current location, Home/Work, Photon search and endpoint swap behavior in the existing code paths.
- [x] Keep accepted RoutePlanner density unchanged in follow-up; optional further narrowing deferred by KIS.
- [x] Reduce ORS-to-footer whitespace without offset/negative-padding hacks.
- [x] Remove unused legacy `AppFooter`/legal dead code.
- [x] Remove the redundant/misleading visible `not connected to abfahrt.now` identity block from `CommunityFooter`.
- [x] Render footer disclaimer sentence-by-sentence as centered lines.
- [x] Suppress `Standort erlauben` idle content when location permission is already granted.

## Verification
- [x] Locale/static/governance gates green on the converged Part-A commit (Android CI #60).
- [x] `:app:testDebugUnitTest :app:assembleDebug :app:assembleRelease` green for the complete implementation (Android CI #62).
- [x] Final pre-device-smoke implementation/docs gate green (Android CI #64).
- [x] Configured real device: original onboarding/API-key flash no longer visible.
- [x] RoutePlanner compact header visually accepted on real device.
- [ ] Configured real device: repeated cold starts after follow-up show neither onboarding/API-key nor granted-location prompt flash.
- [ ] RoutePlanner real-device functional smoke: origin search, destination search, Current location, Home/Work, swap, Route finden.
- [ ] Compact-width visual smoke for both onboarding steps after spacing follow-up.
- [ ] Simplified footer / sentence layout visually accepted after follow-up.
- [ ] No new app FATAL/ANR/navigation regression in the final runtime smoke.

## Governance
- [x] `/doc` converged to implementation-complete / pending-evidence status.
- [x] First real-device findings converged: F-152-001 closed, F-152-002 pending evidence.
- [ ] Final `/doc` acceptance convergence after follow-up real-device evidence.
- [x] AB-018 remains separate as Build 153.
