# Build 132 Specification — Android 17 targetSdk 37

## Goal
Activate Android 17 target behavior only after Build 131 proved the API-37 toolchain/compile/runtime path, without mixing in product or hygiene changes.

## Requirements
- `versionCode = 1320`.
- `compileSdk = 37`, `targetSdk = 37`, `minSdk = 34`.
- Keep AGP 9.4.0, Gradle 9.6.0, built-in Kotlin/KGP/Compose 2.3.21, KSP 2.3.12, Hilt 2.60.1.
- No product/runtime logic changes.
- Preserve Build-130 graphics-path exclusion/shim, MapLibre 13.6.0 and DataStore 1.2.1.
- Do not add `ACCESS_LOCAL_NETWORK` because the app has no LAN feature.
- Do not add a Certificate Transparency bypass or custom Network Security Configuration.
- Keep manifest free of fixed orientation/resizability restrictions.
- Keep Build-131 Kotlin warnings out of scope; record them as separate hygiene debt.

## Acceptance
Real debug/release-R8 build plus API-37 runtime under targetSdk 37. Verify successful public HTTPS/CT calls, location/search, departures, settings/details, WALK/BIKE, predictive Back/sheets, MapLibre route preview and a large/resizable profile. No FATAL/ANR, TLS/CT, permission, class/linker or native regression.
