# Build 130 Tasks

- [x] Archive Build-129 release-APK audit.
- [x] Mark MapLibre 13.6.0 and DataStore 1.2.1 64-bit artifact gates green.
- [x] Bump versionCode to 1300.
- [x] Remove external graphics-path dependency/catalog alias.
- [x] Add global graphics-path module exclusion.
- [x] Add API-34+ platform/Kotlin compatibility implementation.
- [x] Add static graphics-path compatibility guard.
- [x] Run synthetic Kotlin/JVM signature typecheck for the compatibility sources (explicitly not an Android/Gradle build).
- [x] Extend artifact audit to version 3 with an explicit `libandroidx.graphics.path.so` absence invariant.
- [x] Converge `/doc`.
- [x] Run available static/source checks.
- [ ] Build real release APK in Android Studio.
- [ ] Confirm APK contains no `libandroidx.graphics.path.so` and has zero 64-bit artifact failures.
- [ ] Run real 16-KB system smoke (`memoryPageSizeBytes=16384`, departures, RoutePreview).
