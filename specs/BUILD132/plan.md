# Build 132 Plan

1. Converge Build-131 acceptance into `/doc` and `/evidence`.
2. Bump `versionCode` to 1320.
3. Set `targetSdk` from 36 to 37; leave compileSdk/minSdk/toolchain/dependencies unchanged.
4. Extend the Android-17 static gate for app-relevant Target-37 behavior risks.
5. Do not alter product code or clean unrelated Kotlin warnings.
6. Run all source/static gates and compare `app/src` byte-for-byte with Build 131.
7. Package source.
8. User runs real Gradle/R8 build and API-37 Target-37 behavior smoke.
9. Close Android-17 migration only after real runtime evidence.
