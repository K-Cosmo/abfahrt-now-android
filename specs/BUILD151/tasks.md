# Build 151 Tasks

## Implementation
- [x] Route settings footer to a dedicated community identity composable.
- [x] Community identity explicitly states independent/unofficial status.
- [x] Data-source attribution to abfahrt.now remains visible.
- [x] Project link points to the canonical GitHub repository.
- [x] API privacy/terms links are labeled as provider/API links.
- [x] New strings supplied for all 22 bundled locales.
- [x] Bump versionCode to 1510.

## Governance / acceptance
- [x] `/doc` converged to Build 151.
- [x] Locale/static gates green.
- [x] `:app:testDebugUnitTest :app:assembleDebug` green.
- [x] `:app:assembleRelease` green in Android CI #42; R8/Minify included.
- [x] Real-device settings footer smoke: Community identity is visibly correct in the user-provided Build-151 screenshot; repository and API-provider URLs are fixed constants in `CommunityFooter`.
- [x] F-DOC1-015 can close with Build 151.
- [x] Next product change is Build 152 UI/UX package; AB-018 remains separate as Build 153.
