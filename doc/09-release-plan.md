# Release-Plan

Dieser Plan beschreibt die **aktuelle Reihenfolge**. Historische Build-Details liegen in [`CHANGELOG.md`](CHANGELOG.md), Regressionen in [`05-regression-ledger.md`](05-regression-ledger.md) und Findings in [`07-findings.md`](07-findings.md).

## REPO1 — abgeschlossen

Das öffentliche Repository ist der kanonische Entwicklungsworkspace. `/doc` ist die einzige normative Dokumentationswurzel, Community-/EU-first-Policy ist dokumentiert, und der vollständige Gradle-9.6.0-Wrapper wird lokal sowie in GitHub Actions direkt und erfolgreich verwendet.

## Build 150 — abgeschlossen

Der GitHub-Release-Update-Checker ist technisch accepted und in `main` integriert: credential-isolierter anonymer Client, 22 Locale-Sets, realer `releases/latest`-E2E und erfolgreicher Release-/R8-Build.

## Build 151 — abgeschlossen

Die Runtime-UI zeigt die unabhängige/unoffizielle Community-Identität. abfahrt.now bleibt als Daten-/API-Quelle sichtbar. Android CI #42 ist inklusive Locale-/Static-/Governance-Gates, Unit-Tests, Debug- und Release/R8-Build grün.

## Build 152 — UI/UX-Konvergenz — accepted 04.10.2026

Startup-Access-Gate ohne API-Key-/Onboarding-Flicker, Permission-Idle-Flicker behoben, kompakter RoutePlanner-Kopf und verdichteter Settings-/Community-Footer. Android CI #81 sowie Realgeräte-Evidence sind grün.

## Build 153 — Startup/Main-Thread-Instrumentierung — accepted 04.10.2026

Build 153 (`versionCode = 1530`) lokalisierte den dominanten wiederholbaren Kaltstart-Warteblock auf die Current-Location-Auflösung vor dem ersten abfahrt.now-Core-Request. Drei saubere Cold Starts ergaben `Loading`→Core ca. 2,72 / 2,59 / 3,02 s. Application/MapLibre, Compose, Preference-Gate und der erste Core-HTTP-Call waren nicht der dominante Block. ORS blieb asynchron nach dem Core.

## Build 154 — Current-Location First-Paint Fast Path — accepted 04.10.2026

Build 154 (`versionCode = 1540`, `versionName = 1.1.0`) schließt F-153-001. Die vorhandene System-`lastLocation` wird auf einem leeren Current-Location-Kaltstart ausschließlich als provisorischer First-Paint-Origin früher genutzt; High Accuracy validiert parallel. Drei reale Cold Starts reduzierten `Loading`→Core auf ca. **29 / 30 / 25 ms** gegenüber Build-153-Baseline **2,59–3,02 s**. Bestehender 200-m-Re-Anchor-Vertrag, Progressive First Paint und ORS-after-Core bleiben unverändert.

Bereinigte Evidence: `/evidence/public/build-154/2026-10-04_acceptance.md`.

## Build 155 — wählbare Abfahrts-Sortierung — accepted 04.10.2026

Build 155 (`versionCode = 1550`, `versionName = 1.1.0`) schließt F-SORT-001.

Dauerhafte Produktsemantik:
- **Nähe zuerst / `NEARBY`** — Entfernung → Abfahrtszeit → Richtung → Linie; neuer Default.
- **Nächste Abfahrt / `SOONEST`** — Abfahrtszeit → Entfernung → Richtung → Linie.
- **Linien bündeln / `LINE_GROUPED`** — Entfernung → Linie → Abfahrtszeit → Richtung; Legacy-Sicht.
- Auswahl liegt in Settings direkt nach „Abfahrten pro Richtung“ und vor Quick-Filtern.
- persistente Speicherung in bestehendem DataStore/`AppPreferences`; keine neue Dependency.
- `DepartureDisplayOrdering` bleibt Single Source of Truth.
- `SOONEST` besitzt keinen versteckten globalen HERE-Vorrang; HERE wirkt in Distanzprofilen über die bestehende effektive Distanz 0.
- Profilwechsel löst nur lokales `refilter()` aus und verändert nicht API-, Merge-, Dedup-, ORS-, Location- oder First-Paint-Pipeline.

Acceptance-Evidence:
1. Android CI #125 auf Runtime-Head `02ac2a6…` vollständig grün: Locale/Governance/Compatibility, committed Wrapper, Unit Tests, Debug und Release/R8.
2. Realgeräte-Screenshot bestätigt die neue Sortiersektion und alle drei Radio-Profile; Nutzer bewertet die Darstellung als passend.
3. Nutzer bestätigt, dass die Abfahrtsseite bei allen drei Profilen entsprechend sortiert.
4. Logcat zeigt lokale Refilter-Zustände für `SOONEST`, `LINE_GROUPED` und `NEARBY` ohne `departure_state_loading`.
5. Kein unmittelbar an einen Sortierwechsel gekoppelter `/departures`-/ORS-Zyklus beobachtet; spätere Requests sind normaler Stable-Refresh, Same-Origin-ORS-Metriken werden wiederverwendet.
6. Gewählte Sortierung bleibt nach `force-stop`/Neustart erhalten.
7. Keine FATAL-/ANR-/Navigation-/Settings-Regression im Abnahmeumfang.

Build 155 wurde am 06.10.2026 als RELEASE1 `v1.1.0-b155` veröffentlicht.

## RELEASE1 — erste signierte GitHub-APK — released 06.10.2026

F-REL-001/B-REL-001 ist abgeschlossen. **`v1.1.0-b155`** wurde als erster öffentlicher signierter GitHub-APK-Release veröffentlicht.

Release-Evidence:
1. Release-Tag `v1.1.0-b155` zeigt auf Commit `85df24b280f60e47d813d17aa93f400b22fca787`.
2. Finaler Tag-Build: `:app:testDebugUnitTest :app:assembleRelease` erfolgreich.
3. APK vor Signing mit `zipalign -P 16` ausgerichtet; danach mit historischem Signer + Community-Signer + Signing-Certificate-Lineage signiert.
4. `apksigner verify --min-sdk-version 34 --verbose --print-certs`: `Verifies`, v3=true, aktueller Signer `CN=Abfahrt Now Community, C=DE`.
5. Community-Signer Certificate SHA-256: `23283ed09731c3711d5f223f0424323697243000a78cbbf0731e4553946325f8`.
6. Finales APK `abfahrt-now-v1.1.0-b155.apk`: SHA-256 `0FE1A8D7EB8A6038FF4446DD4F696BA37737875982945A60DF860ABE255BD9A5`; abschließendes `zipalign -c -P 16 -v 4` erfolgreich.
7. Reales Old-Key→New-Key-In-place-Update ohne Deinstallation erfolgreich; 16-KB-Runtime separat auf offiziellem 16-KB-x86_64-Emulator mit `memoryPageSizeBytes=16384` und erfolgreichem MapLibre-Native-Load bewiesen.
8. GitHub Release ist weder Draft noch Prerelease; APK und `SHA256SUMS.txt` sind öffentliche Assets. `releases/latest` liefert `v1.1.0-b155`.

Release: https://github.com/K-Cosmo/abfahrtsradar-android/releases/tag/v1.1.0-b155

## Nächster Produktbuild — Namensdifferenzierung

F-NAME-001 ist entschieden: **Abfahrtsradar**. Build 156 migriert App-Label, brandtragende Locale-Texte, Updatechecker und öffentliche Projektlinks auf `K-Cosmo/abfahrtsradar-android`. Package-ID, Signing-Lineage, Persistenz und fachliche Runtime-Semantik bleiben unverändert. Acceptance umfasst zusätzlich den Build-155-Redirect-Smoke sowie ein signiertes In-place-Update 155→156.

## Separater Hardening-Block — ORS-Key-Probe

F-ORS-001/B-ORS-001 bleibt unabhängig: Beim Hinterlegen/Ändern ORS-Key probeweise validieren; HTTP 401/403 darf den neuen Key nicht persistieren bzw. einen vorhandenen gültigen Key nicht überschreiben. Netzwerkfehler/5xx/429 dürfen nicht als ungültiger Key fehlklassifiziert werden.

## Release-Grundsatz

Kein Build gilt aufgrund von Versionsnummer, Gradle-Sync, erfolgreichem CI allein oder KI-Einschätzung als akzeptiert. Maßgeblich sind die in [`11-test-and-evidence.md`](11-test-and-evidence.md) definierten realen Gates und Evidence.
