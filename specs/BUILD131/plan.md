# Build 131 Plan

1. Converge Build-130 16-KB acceptance into `/doc` and `/evidence`.
2. Bump versionCode to 1310.
3. Move compileSdk to 37 while targetSdk remains 36.
4. Upgrade AGP/Gradle to an API-37-supported stable pair.
5. Migrate from kotlin-android to AGP built-in Kotlin; align Compose compiler/KGP, KSP and Hilt.
6. Keep all app/product code unchanged.
7. Run source/static gates and package source.
8. User runs real Gradle/R8 build and API-37 runtime smoke.
9. Only after acceptance, Build 132 raises targetSdk to 37.
