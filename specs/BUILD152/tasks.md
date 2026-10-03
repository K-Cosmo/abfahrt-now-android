# Build 152 Tasks

## Implementation
- [ ] `versionCode = 1520`.
- [ ] Fix real preference-readiness gate; no synthetic-default access decision.
- [ ] Onboarding body copy left aligned; required/optional information visually separated.
- [ ] Compact shared RoutePlanner origin/destination container with trailing swap action.
- [ ] Preserve Current location, Home/Work, Photon search and endpoint swap behavior.
- [ ] Reduce ORS-to-footer whitespace.
- [ ] Remove unused legacy `AppFooter`/legal dead code.

## Verification
- [ ] Locale/static/governance gates green.
- [ ] `:app:testDebugUnitTest :app:assembleDebug :app:assembleRelease` green in CI.
- [ ] Configured real device: repeated cold starts show no onboarding/API-key flash.
- [ ] RoutePlanner real-device smoke: origin search, destination search, swap, Route finden.
- [ ] Compact-width visual smoke for RoutePlanner and onboarding.
- [ ] Footer spacing visually accepted.

## Governance
- [ ] `/doc` converged after implementation/evidence.
- [ ] AB-018 remains separate as Build 153.
