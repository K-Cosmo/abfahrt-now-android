# Build 131 Specification — Android 17 compile/toolchain staging

## Goal
Migrate AbfahrtApp to an officially API-37-capable Android build toolchain and compile against Android 17 without activating targetSdk-37 behavior changes in the same build.

## Requirements
- `versionCode = 1310`.
- `compileSdk = 37`, `targetSdk = 36`, `minSdk = 34`.
- AGP 9.4.0, Gradle 9.6.0.
- Use AGP built-in Kotlin; remove `org.jetbrains.kotlin.android`.
- KGP/Compose Compiler 2.3.21, KSP 2.3.12, Hilt 2.60.1.
- Preserve JVM 17.
- No product/runtime logic changes.
- Preserve Build-130 graphics-path exclusion/shim, MapLibre 13.6.0 and DataStore 1.2.1.

## Acceptance
Real Gradle tests/debug/release-R8 build with Platform 37 plus API-37 runtime smoke. Target 37 is explicitly out of scope and follows in Build 132.
