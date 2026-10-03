# Build 148 Plan

1. Remove `scopePlaceQueryToCity()` from the productive general-search path and stop passing city hints into that flow.
2. Keep the current location coordinates as the only locality signal for general Photon search.
3. Remove only the general `searchPlaces()` re-ranking; leave `searchStations()` transit ranking unchanged.
4. Log the raw query/bias and first five Photon candidates for runtime evidence.
5. Replace locality-exception tests with raw-query policy tests.
6. Bump only `versionCode` and converge D-069/F-147-001/B-148-001 plus handoff/test plan.
7. Acceptance via static project gates, combined Android Studio Gradle gate and runtime Photon smoke.
