# Build 134 Plan

1. Converge Build-133 warning-hygiene acceptance into `/doc` and `/evidence`.
2. Bump versionCode to 1340.
3. Map optional OpenAPI `stations[].walkSeconds` into the existing `Station` model.
4. Add focused Gson contract tests without introducing a new test dependency.
5. Add one bounded `AbfahrtContract` coverage diagnostic after the effective initial response is known.
6. Verify by source inspection that `walkSeconds` is not consumed by product logic.
7. Run existing static/source gates and inspect the diff.
8. Package source.
9. User runs `testDebugUnitTest`, `assembleDebug`, and one normal runtime refresh; evidence decides the next step.
