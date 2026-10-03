# Build 148 Tasks

- [x] Record Build-147 Gradle success and reproduce `Bad Saarow -> Bad Saarow, Berlin` from supplied runtime evidence.
- [x] Remove general-search city-hint/query rewriting.
- [x] Remove general-search client text-score re-ranking while preserving de-duplication/order.
- [x] Add `PhotonPlaceRank` debug evidence.
- [x] Replace locality-exception tests with raw-query policy tests.
- [x] Remove now-dead general-search city-hint plumbing.
- [x] Set versionCode 1480.
- [x] Converge `/doc` and project changelogs.
- [x] Run static governance/default/locale/API37/16KB/key/ORS/graphics-path source gates.
- [x] Correct first real compile-gate process finding F-148-001 by retaining/overwriting the legacy `GeocodingLocalityScopeTest.kt` path with D-069 raw-query assertions.
- [ ] Real `:app:testDebugUnitTest :app:assembleDebug` in Android Studio.
- [ ] Runtime Photon smoke: `Bad Saarow`, `Brandenburger Tor`, `Potsdam`, `S Potsdam`, `Hauptbahnhof`; inspect `PhotonPlaceRank` order.
