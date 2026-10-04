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
- [x] Final follow-up/docs head green with Static/Governance, wrapper, unit, debug and release/R8 (Android CI #72).
- [x] Configured real device: original onboarding/API-key flash no longer visible.
- [x] Configured real device after follow-up: no granted-location prompt flash; Location updates start directly.
- [x] RoutePlanner compact header visually accepted; no observed regression.
- [x] Runtime route smoke confirms Photon destination search and successful `/trips` request/result path.
- [x] Compact-phone onboarding inspected after spacing follow-up; wording intentionally unchanged and flow remains usable/scrollable.
- [x] Simplified footer / sentence layout visually accepted after follow-up.
- [x] No app `FATAL EXCEPTION`, `AndroidRuntime` or ANR signature in the final runtime logcat.
- [x] Installed package metadata confirmed: versionCode 1520, versionName 1.1.0, minSdk 34, targetSdk 37.

## Governance
- [x] `/doc` converged to implementation-complete / pending-evidence status during development.
- [x] First real-device findings converged: F-152-001 closed, F-152-002 tracked separately.
- [x] Follow-up real-device evidence closes F-152-002.
- [x] Final `/doc` acceptance convergence completed.
- [x] AB-018 remains separate as Build 153.
