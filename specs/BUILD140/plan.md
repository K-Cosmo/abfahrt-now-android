# Build 140 Plan

1. Reuse the existing `TripLeg.intermediateStops` / `stopNames` contract fields.
2. Add small view-only helpers for transfer-minute and intermediate-stop presentation.
3. Render transfer duration only between two non-walking transit legs and never for `sameVehicle`.
4. Make intermediate-stop count expandable only when names are actually present.
5. Add three localized route-detail strings to all 22 resource sets.
6. Extend JVM presentation tests.
7. Run all source/static gates and hand off for the real Gradle/runtime gate.
