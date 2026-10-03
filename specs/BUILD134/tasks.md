# Build 134 Tasks

- [x] Record Build-133 build/runtime acceptance.
- [x] Bump versionCode to 1340.
- [x] Add nullable `Station.walkSeconds` matching the project OpenAPI.
- [x] Add contract regression tests for core `/departures` fields and optional/unknown fields.
- [x] Add bounded runtime coverage logging for `walkSeconds`.
- [x] Keep `walkSeconds` out of product/ORS/reachability logic.
- [x] Keep dependencies/endpoints/UI/Android-17/16-KB/security baselines unchanged.
- [x] Run local static/source gates.
- [ ] Real `testDebugUnitTest`.
- [ ] Real `assembleDebug` without Kotlin warnings.
- [ ] Runtime `AbfahrtContract walkSeconds coverage=...` evidence.
