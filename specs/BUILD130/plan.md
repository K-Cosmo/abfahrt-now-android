# Build 130 Plan

1. Archive and converge the Build-129 artifact evidence: MapLibre/DataStore green, graphics-path only remaining hard blocker.
2. Bump `versionCode` to 1300.
3. Remove the direct graphics-path catalog/dependency declaration and globally exclude the external module from transitive dependency resolution.
4. Add an API-34+ app-local compatibility implementation in package `androidx.graphics.path` using framework PathIterator and pure-Kotlin conic conversion.
5. Add static guardrails proving no JNI/native graphics-path path is reintroduced and minSdk remains >=34.
6. Preserve MapLibre 13.6.0, DataStore 1.2.1 and all product/runtime logic.
7. Converge `/doc`, run all available source gates, and package the source.
8. User builds a release APK and reruns the 16-KB artifact audit.
9. Only after a green artifact audit: run on a real/emulated 16-KB system and verify `memoryPageSizeBytes=16384`, normal departures and RoutePreview.
