# Build 152 Tasks

## Implementation
- [x] `versionCode = 1520`.
- [x] Fix real preference-readiness gate; no synthetic-default startup decision.
- [x] Keep startup navigation on one explicit AccessGate path; create protected feature ViewModels only after real preferences exist.
- [x] Onboarding body copy left aligned; required/optional information visually separated.
- [ ] Compact shared RoutePlanner origin/destination container with trailing swap action.
- [ ] Preserve Current location, Home/Work, Photon search and endpoint swap behavior.
- [ ] Reduce ORS-to-footer whitespace.
- [ ] Remove unused legacy `AppFooter`/legal dead code.

## Verification
- [ ] Locale/static/governance gates green on the converged Part-A commit.
- [ ] `:app:testDebugUnitTest :app:assembleDebug :app:assembleRelease` green in CI.
- [ ] Configured real device: repeated cold starts show no onboarding/API-key flash.
- [ ] RoutePlanner real-device smoke: origin search, destination search, swap, Route finden.
- [ ] Compact-width visual smoke for RoutePlanner and onboarding.
- [ ] Footer spacing visually accepted.

## Governance
- [ ] `/doc` converged after implementation/evidence.
- [x] AB-018 remains separate as Build 153.
