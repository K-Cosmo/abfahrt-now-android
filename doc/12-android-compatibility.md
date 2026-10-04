# Android Compatibility

## Aktueller Stand Build 152

| Wert | Stand |
|---|---:|
| minSdk | 34 |
| compileSdk | 37 |
| targetSdk | 37 |
| Java/Kotlin JVM | 17 |
| Gradle Distribution | 9.6.0 |
| Android Gradle Plugin | 9.4.0 |
| Kotlin Build Integration | AGP built-in Kotlin |
| KGP / Compose Compiler Plugin | 2.3.21 |
| KSP | 2.3.12 |
| Hilt | 2.60.1 |

`minSdk 34` bleibt bewusste Produktpolitik: drei Android-Hauptversionen hinter Android 17, moderne Plattformbasis und begrenzte Kompatibilitätslast.

REPO1 sowie Builds 150–152 haben diese Android-/Toolchain-Baseline nicht verändert. REPO1 hat den vollständigen Gradle-9.6.0-Wrapper als kanonischen Buildpfad eingecheckt. Seit Build 151 gehört `:app:assembleRelease` dauerhaft zum normalen Android-CI-Gate, sodass Unit-Tests, Debug- und Release/R8-Build auf demselben PR-Stand geprüft werden.

Build 152 ist Teil der akzeptierten Compatibility-Baseline. Der installierte Realgeräte-Stand bestätigt `versionCode 1520`, `minSdk 34`, `targetSdk 37` und `versionName 1.1.0`; Android CI #81 ist auf dem finalen Acceptance-Head inklusive Release/R8 grün. Der finale Runtime-Smoke zeigt keinen App-FATAL-/ANR-/Native-Linker-Crash. Der dabei beobachtete Testlauf lief auf einem 4-KB-Gerät; die separat bereits akzeptierte 16-KB-Readiness aus Build 130 bleibt dadurch unverändert bestehen.

## Android 17 / API 37 — gestufte Migration

Android 17 ist API 37. Die offizielle SDK-Dokumentation fordert für Zugriff auf API-37-APIs `compileSdk = 37`; die Target-Verhaltensweisen werden separat mit `targetSdk = 37` aktiviert.

Der frühere Toolchainstand AGP 8.13.2 war für API 37 nicht ausreichend: AGP 8.13 unterstützt offiziell maximal API 36.1. Für API 37 ist mindestens AGP 9.1.1 erforderlich. Build 131 nutzt deshalb den aktuellen stabilen AGP-9.4-Pfad mit Gradle 9.6.0.

### Phase 1 — Build 131 — accepted

- `compileSdk 37`;
- `targetSdk 36`;
- AGP 9.4 / Gradle 9.6;
- Migration auf built-in Kotlin;
- realer Build und API-37-Kernflow erfolgreich;
- AAB nach Toolchainwechsel erneut `PAGE_ALIGNMENT_16K`.

### Phase 2 — Build 132 — accepted

`targetSdk = 37` ist real gebaut und im API-37-Kernflow abgenommen. Die folgenden Punkte bleiben als reguläre Kompatibilitätsmatrix relevant:
- **Certificate Transparency:** für Target 37 standardmäßig aktiv; reale HTTPS-Calls gegen abfahrt.now, HEIGIT ORS, Photon und MapLibre-Ressourcen testen.
- **Local Network Permission:** AbfahrtApp nutzt nur Internet-Endpunkte und benötigt derzeit kein `ACCESS_LOCAL_NETWORK`; Permission nicht vorsorglich anfordern.
- **MessageQueue / static-final reflection:** im App-Source kein entsprechender Zugriff gefunden; Runtime/Dependencies trotzdem beobachten.
- **Safer native dynamic loading:** App lädt keine eigenen externen Native-Dateien über `System.load`; MapLibre/DataStore sind paketierte Dependencies.
- **Large/resizable screens:** Manifest enthält keine feste Orientation/Resizability-Sperre; Phone/Tablet/Foldable/resizable Profile testen.
- **App memory limits / UI:** Karten-/RoutePreview und lange Listen auf API 37 beobachten.
- **Back/Sheets/Location:** Predictive Back, Settings/BottomSheets, Permission- und Standortfluss erneut testen.

## 16-KB Page Size — accepted Build 130

Build 130 ist vollständig abgenommen:
- Release-APK: null 64-Bit-Gating-Fehler; DataStore + MapLibre grün; `libandroidx.graphics.path.so` abwesend.
- AAB: `PAGE_ALIGNMENT_16K`.
- System: Page Size `16384`.
- App: `AbfahrtCompat memoryPageSizeBytes=16384`.
- RoutePreview/Kernflow auf dem 16-KB-System erfolgreich.

Die Build-130-graphics-path-Kompatibilitätsschicht bleibt an `minSdk >= 34` gebunden. Jeder spätere Upstream-Rückwechsel benötigt erneut den Artefakt-Audit.

## Geräte-/UI-Matrix

Mindestens:
- API 34 (minSdk),
- API 36 (Rückwärtskompatibilitäts-/Regressionstest),
- API 37 (aktuelles Compile-/Target-/Runtime-Ziel),
- kleines Phone,
- großes/resizable Phone,
- Tablet/Foldable/resizable Profil,
- 16-KB-Systemimage als Regressionstest nach Toolchain-/Packagingwechseln.

## Offizielle Referenzen

- Android 17 SDK Setup / API 37: <https://developer.android.com/about/versions/17/setup-sdk>
- Android 17 Migration: <https://developer.android.com/about/versions/17/migration>
- Android 17 Behavior Changes (all apps): <https://developer.android.com/about/versions/17/behavior-changes-all>
- Android 17 Behavior Changes (target 37): <https://developer.android.com/about/versions/17/behavior-changes-17>
- AGP/API-level compatibility: <https://developer.android.com/build/releases/about-agp>
- AGP 9.4: <https://developer.android.com/build/releases/agp-9-4-0-release-notes>
- Built-in Kotlin migration: <https://developer.android.com/build/migrate-to-built-in-kotlin>
- Android 16-KB Page Size: <https://developer.android.com/guide/practices/page-sizes>