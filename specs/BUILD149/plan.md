# Build 149 Plan

1. Reuse the existing HERE predicate; do not create a second distance threshold.
2. Add a small route-preview load policy so HERE short-circuits before ORS.
3. Pass the active departure-query origin to the details sheet.
4. Reuse MapLibre/style/marker primitives for a two-marker HERE location map without a line source.
5. Add one localized HERE-map title across all existing locale resource sets.
6. Add focused JVM policy tests.
7. Bump only `versionCode` to 1490 and converge `/doc` + evidence/handoff.
8. Run all source/static gates; real Gradle/runtime acceptance remains user/device evidence.
