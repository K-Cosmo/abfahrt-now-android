# Abfahrt! — Android App

Location-aware Android app for nearby public transport departures and route planning.

Abfahrt! is a native Android app that prioritizes nearby departures, supports location-aware place search via Photon, plans public-transport trips through abfahrt.now, and can optionally enrich walking and cycling access with OpenRouteService.

## Current state

- Version: **1.1.0**
- Build: **149** (`versionCode 1490`)
- Android: **minSdk 34**, **compileSdk 37**, **targetSdk 37**
- Stack: Kotlin, Jetpack Compose, Material 3, Hilt, Retrofit/OkHttp, DataStore, MapLibre
- Build 148 introduced raw Photon queries with location bias and provider ranking.
- Build 149 fixes the HERE details case: an already-reached stop shows a marker-based location map instead of requesting/drawing a walking route.

## Source of truth

The only normative product and technical documentation lives in [`/doc`](doc/00-index.md).

Useful entry points:

- [`doc/00-index.md`](doc/00-index.md) — documentation index
- [`doc/10-build-handoff.md`](doc/10-build-handoff.md) — current development handoff
- [`doc/07-findings.md`](doc/07-findings.md) — open findings
- [`doc/08-backlog.md`](doc/08-backlog.md) — backlog
- [`doc/09-release-plan.md`](doc/09-release-plan.md) — release plan
- [`doc/11-test-and-evidence.md`](doc/11-test-and-evidence.md) — test/evidence process

`README.md`, `/specs`, and `/evidence` are not competing sources of product truth.

## Build / setup

Open the project in a current Android Studio installation and run Gradle Sync.

A personal **abfahrt.now API key** is required by the app. An **OpenRouteService API key** is optional and enables precise walking/cycling route enrichment. Keys are entered at runtime and stored locally using Android Keystore-backed encryption; no keys are part of this repository.

The standalone CLI wrapper packaging issue is still tracked as [`F-DOC1-008`](doc/07-findings.md): this source state contains `gradle-wrapper.properties`, but the wrapper scripts/JAR are not yet part of the repository. Until that finding is closed, use Android Studio or a locally available compatible Gradle environment.

## Public evidence policy

Raw Logcat/device evidence is intentionally not published by default because it may contain precise coordinates, device identifiers, or local paths. Only sanitized evidence belongs under [`/evidence`](evidence/README.md).

## License

No project license has been selected yet. Until a license is added, the source is publicly visible but no open-source usage rights are granted beyond those provided by applicable law. Third-party components keep their respective licenses and notices.
