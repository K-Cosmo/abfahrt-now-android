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

Build 155 ist damit Release-Kandidat für RELEASE1.

## RELEASE1 — erste signierte GitHub-APK — nächster P0-Schritt

F-REL-001/B-REL-001 ist Release Engineering und kein normaler Produktbuild. Ziel ist **v1.1.0-b155** als erster echter signierter GitHub-APK-Release auf Basis des akzeptierten Build 155.

Vor Veröffentlichung erforderlich:
1. dauerhaftes Release-Keypair/Keystore lokal erzeugen und außerhalb des Repos sicher verwahren/backupen;
2. Signing-Konfiguration nur über private lokale Properties bzw. optional später CI-Environment/Secrets; keine Secrets oder Keystore-Datei im Repo;
3. finale APK signieren und mit `apksigner verify --verbose --print-certs` prüfen;
4. finalen Release-/R8-/16-KB- und Realgeräte-Smoke **auf genau der signierten APK** ausführen;
5. SHA-256 der APK in den Release Notes veröffentlichen;
6. Tag gemäß Update-Checker-Vertrag `v1.1.0-b155` verwenden.

Wichtiger Erstinstallationspunkt: bisherige lokale Debug-Installationen sind mit dem Debug-Key signiert. Die erste Release-Key-APK kann deshalb nicht als reguläres In-place-Update über die Debug-App installiert werden; Deinstallation/Neuinstallation löscht lokale App-Daten/API-Keys. Ab RELEASE1 muss derselbe Release-Key dauerhaft für alle Updates verwendet werden.

Die normale Android-CI bleibt credential-frei. Ein automatisierter signierter Release-Workflow ist optionaler Folgeschritt und wird nicht vor den ersten lokalen, verifizierten Release erzwungen.

## Separater Hardening-Block — ORS-Key-Probe

F-ORS-001/B-ORS-001 bleibt unabhängig: Beim Hinterlegen/Ändern ORS-Key probeweise validieren; HTTP 401/403 darf den neuen Key nicht persistieren bzw. einen vorhandenen gültigen Key nicht überschreiben. Netzwerkfehler/5xx/429 dürfen nicht als ungültiger Key fehlklassifiziert werden.

## Release-Grundsatz

Kein Build gilt aufgrund von Versionsnummer, Gradle-Sync, erfolgreichem CI allein oder KI-Einschätzung als akzeptiert. Maßgeblich sind die in [`11-test-and-evidence.md`](11-test-and-evidence.md) definierten realen Gates und Evidence.
