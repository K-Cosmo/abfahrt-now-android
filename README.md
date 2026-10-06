# Abfahrtsradar — Community Android App

**Independent community Android client for nearby public transport departures and route planning.**

> [!IMPORTANT]
> This is an **independent community project**. It is **not the official abfahrt.now Android app**, is not affiliated with the operator or developer of the abfahrt.now API, and this project does not develop or operate that API. The app is an independent client that consumes public/authorised interfaces provided by external services.

Abfahrtsradar focuses on a simple everyday question: which useful public transport departures are near me right now, and how do I reach them? The UI deliberately stays compact while the app combines live departure data, location-aware search, optional walking/cycling routing, and public-transport trip planning.

## What you need

- **Android 14 or newer** (`minSdk 34`).
- A personal **abfahrt.now API key** — required for departures and public-transport routing. To request an API key, email [hi@abfahrt.now](mailto:hi@abfahrt.now?subject=API%20Key%20Request). See the [abfahrt.now API documentation](https://www.abfahrt.now/docs/).
- Optional: a **HeiGIT / openrouteservice API key** for precise walking/cycling routes and reachability enrichment. A free Standard API key can be requested through the [HeiGIT account signup](https://account.heigit.org/signup); API information is available at [api.heigit.org](https://api.heigit.org/).

API keys are entered at runtime and stored locally as Android-Keystore-backed encrypted values. Keys are never part of this repository.

## Features

- Nearby public transport departures with realtime/schedule status.
- Distance-aware ordering with a dedicated **Here** state for already reached stops.
- Individually selectable transport modes; at least one always remains active.
- Configurable radius, departure window and refresh interval.
- Location-aware place and station search with Photon/OpenStreetMap data.
- Public-transport route planning through the abfahrt.now `/trips` API.
- Optional walking/bicycle enrichment with HeiGIT/openrouteservice.
- MapLibre route/location previews using OpenStreetMap raster tiles.
- Saved Home/Work destinations stored locally.
- **22 bundled UI language resource sets**; no runtime translation service is required.

Bundled UI languages: German, English, Dutch, Danish, Norwegian Bokmål, Swedish, Finnish, Italian, Spanish, Portuguese, French, Polish, Czech, Hungarian, Romanian, Slovak, Croatian, Slovenian, Estonian, Latvian, Lithuanian and Klingon.

## EU-first service policy

The project follows an **EU-first, open-standards and data-minimisation** approach when choosing runtime services. This is a design preference, not a claim that every dependency or hosting component is located in the EU.

Current runtime/service relationships are documented normatively in [`doc/14-community-and-service-policy.md`](doc/14-community-and-service-policy.md). In short:

| Service | Purpose | Key | Notes |
|---|---|---:|---|
| [abfahrt.now](https://www.abfahrt.now/docs/) | departures and public-transport routing | required | European transit API consumed by this independent client |
| [Photon](https://github.com/komoot/photon) + OpenStreetMap | place/station geocoding | no | open-source geocoder; only query/bias data needed for search is sent |
| [HeiGIT / openrouteservice](https://api.heigit.org/) | optional walking/bicycle matrix and directions | optional | operated by HeiGIT in Heidelberg, Germany |
| [OpenStreetMap](https://www.openstreetmap.org/) tiles | map background | no | requested only when map previews are shown |
| Google Play Services Location | device location | no project key | Android platform dependency and documented EU-first exception |
| GitHub | source code and releases | no app key | project infrastructure; update metadata use is treated as a documented exception |

Credentials are service-scoped: an abfahrt.now key must never be sent to HeiGIT, Photon, GitHub or another host, and the HeiGIT key must only be sent to the HeiGIT/openrouteservice endpoint.

## Current development state

- Version: **1.1.0**
- Current accepted build: **156** (`versionCode 1560`)
- Android: **minSdk 34**, **compileSdk 37**, **targetSdk 37**
- Stack: Kotlin, Jetpack Compose, Material 3, Hilt, Retrofit/OkHttp, DataStore, MapLibre
- Latest public APK release: [GitHub Releases / latest](https://github.com/K-Cosmo/abfahrtsradar-android/releases/latest)

## Install

Signed APK releases are published on the [GitHub Releases page](https://github.com/K-Cosmo/abfahrtsradar-android/releases). For normal installation or updates, use the APK attached to the latest release.

Each release also publishes a `SHA256SUMS.txt` file so the downloaded APK can be verified independently. Existing release installations are updated in place; uninstalling the app is neither required nor recommended for a normal update because uninstalling removes local app data.

Abfahrtsradar also checks GitHub release metadata at app start and offers a link when a newer build is available. The app does not silently download or install APK files.

## Build / development

Open the **repository root** in a current Android Studio installation — the directory containing `settings.gradle.kts`, not the `app` subdirectory.

The repository contains the complete **Gradle 9.6.0 Wrapper** (`gradlew`, `gradlew.bat`, `gradle-wrapper.jar` and pinned wrapper properties). A fresh clone therefore needs no separately installed Gradle distribution.

Windows:

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug
```

POSIX:

```bash
./gradlew :app:testDebugUnitTest :app:assembleDebug
```

The wrapper pins and verifies the Gradle 9.6.0 distribution. GitHub Actions uses the committed wrapper directly and runs the repository's static governance/compatibility checks plus the same unit/debug-build gate.

`local.properties`, Android Studio metadata, build outputs, keystores, API keys and raw runtime logs remain local and are intentionally not committed.

## AI-assisted development

This project is developed with **AI-assisted engineering**. AI tools are used as collaborators for analysis, implementation, review, tests and documentation. They do not replace the project's evidence requirements: changes are expected to pass the same CI, runtime and acceptance gates regardless of how they were authored.

The binding guardrails for AI-assisted changes are documented in [`doc/AI-CODING-GUARDRAILS.md`](doc/AI-CODING-GUARDRAILS.md).

## Source of truth and contribution workflow

The only normative product and technical documentation lives in [`/doc`](doc/00-index.md). Spec Kit and [`/specs`](specs/) provide **process governance**, not a second product specification.

The development flow is:

**Specification → Plan → Tasks → implementation → analysis/convergence → real tests/evidence → acceptance**

Useful entry points:

- [`doc/00-index.md`](doc/00-index.md) — documentation index / source-of-truth rule
- [`doc/10-build-handoff.md`](doc/10-build-handoff.md) — current development handoff
- [`doc/07-findings.md`](doc/07-findings.md) — open findings
- [`doc/08-backlog.md`](doc/08-backlog.md) — backlog
- [`doc/11-test-and-evidence.md`](doc/11-test-and-evidence.md) — test/evidence process
- [`AGENTS.md`](AGENTS.md) — entry point for coding agents

`README.md`, `/specs`, source code and `/evidence` must not become competing policy/specification trees.

## Public evidence policy

Raw Logcat/device evidence is intentionally not published by default because it may contain precise coordinates, device identifiers or local paths. Only sanitised evidence belongs under [`/evidence`](evidence/README.md).

## License

This project is licensed under the [MIT License](LICENSE). Third-party components retain their respective licenses and notices.
