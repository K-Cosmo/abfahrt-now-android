# Abfahrt! Community Android App

A community-developed Android client for nearby public-transport departures and route planning across Europe.

> **Unofficial community project**  
> This project uses the **abfahrt.now API**, but it is **not the official abfahrt.now Android app**. This project is not developed, endorsed, operated, or maintained by the developer/operator of abfahrt.now, and this repository does not develop or operate the abfahrt.now API.

## What the app does

Abfahrt! prioritizes nearby departures, supports location-aware place search, plans public-transport trips, and can optionally enrich access to stops with precise walking and cycling routes.

Current baseline:

- Version: **1.1.0**
- Build: **149** (`versionCode 1490`)
- Android: **Android 14+** (`minSdk 34`, `compileSdk 37`, `targetSdk 37`)
- Stack: Kotlin, Jetpack Compose, Material 3, Hilt, Retrofit/OkHttp, DataStore, MapLibre
- **22 bundled UI languages**; no cloud translation service is required at runtime

## What you need

### Required: abfahrt.now API key

The app needs a personal **abfahrt.now API key** for departure and public-transport routing data.

- Website: https://abfahrt.now/
- Documentation: https://www.abfahrt.now/docs/

The API key is entered in the app and stored locally using Android Keystore-backed encryption. No API key is part of this repository.

### Optional: OpenRouteService API key

An **OpenRouteService (ORS)** key enables precise walking and cycling route enrichment. The app also works without ORS; in that case these enhancements are unavailable or use the documented fallback behavior.

- OpenRouteService: https://openrouteservice.org/
- Developer signup: https://openrouteservice.org/dev/#/signup
- HeiGIT API portal: https://api.heigit.org/

ORS is operated by **HeiGIT (Heidelberg Institute for Geoinformation Technology)** in Germany.

## EU-first, open and transparent

The project follows an **EU-first** approach for operational runtime services. European and open providers are preferred where they are technically suitable. Deliberate exceptions are documented and kept as small as practical.

| Service | Purpose | Required? | Notes |
| --- | --- | --- | --- |
| **abfahrt.now** | Departures and public-transport routing | Yes | External API; this project is not its operator |
| **Photon / OpenStreetMap** | Place and destination search | Built in | Open-source geocoding based on OSM data; public Photon service operated by komoot |
| **OpenRouteService / HeiGIT** | Precise walking and cycling routing | Optional | European service operated from Germany |
| **GitHub** | Source code and releases | Development/distribution | Documented non-EU exception; no abfahrt.now or ORS API key is sent to GitHub |

A new external runtime service should not be added silently: purpose, data flow, provider, necessity, and an EU/open alternative must be considered first and documented in `/doc`.

## Languages

The Android app currently bundles **22 UI locales**:

Deutsch, English, Nederlands, Dansk, Norsk Bokmål, Svenska, Suomi, Italiano, Español, Português, Français, Polski, Čeština, Magyar, Română, Slovenčina, Hrvatski, Slovenščina, Eesti, Latviešu, Lietuvių and Klingon (`tlh`).

These are packaged Android resources. The UI does not depend on an online translation service.

## Development

Clone the repository and open the **repository root** in a current Android Studio installation.

Windows example:

```text
D:\Android\abfahrt-now-android
```

Do not open only the `app` subdirectory. `settings.gradle.kts`, the Gradle configuration, `/doc`, `/specs`, and Git metadata all live at repository root.

Expected local/CI Gradle gate once the wrapper bootstrap is complete:

```bash
./gradlew :app:testDebugUnitTest :app:assembleDebug
```

Windows:

```bat
gradlew.bat :app:testDebugUnitTest :app:assembleDebug
```

`local.properties`, build outputs, keystores, API keys, and raw runtime logs are intentionally not committed.

## Project governance

The **only normative product and technical source of truth is [`/doc`](doc/00-index.md)**.

- `/doc` — normative product, architecture, decisions, invariants, findings, release/test rules
- `/specs` — process history for Specification → Plan → Tasks; not a competing policy/specification world
- `/evidence` — sanitized test/evidence material
- `.specify` — process governance only

Useful entry points:

- [`doc/00-index.md`](doc/00-index.md) — documentation index
- [`doc/06-decisions.md`](doc/06-decisions.md) — architectural/product decisions
- [`doc/07-findings.md`](doc/07-findings.md) — findings
- [`doc/08-backlog.md`](doc/08-backlog.md) — backlog
- [`doc/09-release-plan.md`](doc/09-release-plan.md) — release plan
- [`doc/10-build-handoff.md`](doc/10-build-handoff.md) — current development handoff
- [`doc/11-test-and-evidence.md`](doc/11-test-and-evidence.md) — test/evidence process

## Public evidence policy

Raw Logcat/device evidence is intentionally not published by default because it may contain precise coordinates, device identifiers, or local filesystem paths. Only sanitized evidence belongs under [`/evidence`](evidence/README.md).

## License

No project license has been selected yet. Until a license is added, the source is publicly visible but no open-source usage rights are granted beyond those provided by applicable law. Third-party components keep their respective licenses and notices.
