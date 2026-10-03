# Build 151 Tasks

## Implementation
- [x] Route settings footer to a dedicated community identity composable.
- [x] Community identity explicitly states independent/unofficial status.
- [x] Data-source attribution to abfahrt.now remains visible.
- [x] Project link points to the canonical GitHub repository.
- [x] API privacy/terms links are labeled as provider/API links.
- [x] New strings supplied for all 22 bundled locales.
- [ ] Bump versionCode to 1510.

## Governance / acceptance
- [ ] Converge `/doc` and close F-DOC1-015 only after evidence.
- [ ] Locale/static gates green.
- [ ] `:app:testDebugUnitTest :app:assembleDebug` green.
- [ ] `:app:assembleRelease` green.
- [ ] Real-device settings footer smoke: community identity visible, GitHub link correct, API legal links correctly labeled.
- [ ] AB-018 remains separate as Build 152.
