# Build 152 Tasks

## Implementation
- [x] `versionCode = 1520`.
- [x] Fix real preference-readiness gate; no synthetic-default startup decision.
- [x] Keep startup navigation on one explicit AccessGate path; create protected feature ViewModels only after real preferences exist.
- [x] Onboarding body copy left aligned; required/optional information visually separated.
- [x] Compact shared RoutePlanner origin/destination container with trailing swap action.
- [x] Preserve Current location, Home/Work, Photon search and endpoint swap behavior in the existing code paths.
- [x] Reduce ORS-to-footer whitespace without offset/negative-padding hacks.
- [x] Remove unused legacy `AppFooter`/legal dead code.

## Verification
- [x] Locale/static/governance gates green on the converged Part-A commit (Android CI #60).
- [x] `:app:testDebugUnitTest :app:assembleDebug :app:assembleRelease` green for the complete implementation (Android CI #62).
- [ ] Configured real device: repeated cold starts show no onboarding/API-key flash.
- [ ] RoutePlanner real-device smoke: origin search, destination search, Current location, Home/Work, swap, Route finden.
- [ ] Compact-width visual smoke for RoutePlanner and onboarding.
- [ ] Footer spacing visually accepted.
- [ ] No new app FATAL/ANR/navigation regression in the runtime smoke.

## Governance
- [x] `/doc` converged to implementation-complete / pending-evidence status.
- [ ] Final `/doc` acceptance convergence after real-device evidence.
- [x] AB-018 remains separate as Build 153.
