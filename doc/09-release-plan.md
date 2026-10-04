# Release-Plan

Dieser Plan beschreibt die **aktuelle Reihenfolge**. Historische Build-Details liegen in [`CHANGELOG.md`](CHANGELOG.md), Regressionen in [`05-regression-ledger.md`](05-regression-ledger.md) und Findings in [`07-findings.md`](07-findings.md).

## REPO1 — abgeschlossen

Das öffentliche Repository ist der kanonische Entwicklungsworkspace. `/doc` ist die einzige normative Dokumentationswurzel, Community-/EU-first-Policy ist dokumentiert, und der vollständige Gradle-9.6.0-Wrapper wird lokal sowie in GitHub Actions direkt und erfolgreich verwendet.

## Build 150 — abgeschlossen

Der GitHub-Release-Update-Checker ist technisch accepted und in `main` integriert: credential-isolierter anonymer Client, 22 Locale-Sets, realer `releases/latest`-E2E und erfolgreicher Release-/R8-Build.

## Build 151 — abgeschlossen

Die Runtime-UI zeigt die unabhängige/unoffizielle Community-Identität. abfahrt.now bleibt als Daten-/API-Quelle sichtbar, Projektlink und API-Provider-Rechtslinks sind eindeutig beschriftet. Der Realgeräte-Screenshot ist positiv; Android CI #42 ist inklusive Locale-/Static-/Governance-Gates, Unit-Tests, Debug- und Release/R8-Build grün. F-DOC1-015 ist geschlossen.

Der Android-CI-Gate baut ab Build 151 dauerhaft sowohl Debug als auch Release, damit der Release-/R8-Nachweis nicht mehr manuell nachgeführt werden muss.

## DOC2 — abgeschlossen

DOC2 konvergierte nach REPO1/Build 150/Build 151 ausschließlich die normative Dokumentation. Es war kein Runtime-Build und änderte weder App-Source noch Versionierung.

## Build 152 — UI/UX-Konvergenz — accepted 04.10.2026

Build 152 ist als neue Runtime-Baseline abgenommen.

Scope:
- `versionCode 1520`, `versionName 1.1.0`, `minSdk 34`, `targetSdk 37`;
- linksbündiges und kompakteres Onboarding ohne Änderung der fachlichen Pflicht-/Optional-Logik;
- kompakter gemeinsamer RoutePlanner-Kopf mit Start/Ziel, Trenner und platzsparender Tauschaktion;
- bestehende Photon-, Saved-Places-, Current-location-, Swap- und `/trips`-Logik fachlich unverändert;
- `AccessGateViewModel` wartet auf die erste echte Preference-Emission, bevor geschützte Feature-ViewModels entstehen;
- `PermissionOrIdleContent` wird bei bereits erteilter Standortberechtigung im `Idle`-Übergang nicht mehr transient angezeigt;
- Settings-Footer strukturell verdichtet, missverständlicher zusätzlicher Identitätskasten entfernt, Disclaimer satzweise zentriert;
- historischer ungenutzter `AppFooter`-/Legal-Deadcode entfernt.

Acceptance-Evidence:
1. Android CI #81 vollständig grün: Static/Governance, committed Wrapper, Unit Tests, Debug und Release/R8.
2. Realgerät: ursprünglicher API-Key-/Onboarding-Flicker nicht mehr sichtbar.
3. Realgerät nach Follow-up: kein `Standort erlauben`-Flicker mehr bei bereits erteilter Berechtigung; Location-Updates starten direkt.
4. Footer vom Nutzer visuell akzeptiert; RoutePlanner zeigt keine beobachtete Regression.
5. Runtime-Logcat: Photon-Zielsuche erfolgreich; `/trips` HTTP 200 mit sieben Ergebnissen.
6. kein `FATAL EXCEPTION`, kein `AndroidRuntime` und keine App-ANR-Signatur im finalen Runtime-Smoke.

F-152-001 und F-152-002 sind geschlossen.

## Build 153 — Startup/Main-Thread-Instrumentierung (AB-018) — in progress / pending evidence

Draft-PR #9 enthält Instrumentierungsstufe A auf Basis des akzeptierten Build 152. `versionCode = 1530`; `versionName`, SDK-/Toolchain-Baseline und Produktsemantik bleiben unverändert. Build 153 ist **nicht accepted**, solange die reale Cold/Warm/Resume-Evidence fehlt.

Instrumentierungsstufe A:
- zentrale, dependency-freie `StartupTrace`-Zeitbasis über Prozess-Uptime;
- Application-/MapLibre-Initialisierung;
- Activity-Create mit `cold`/`warm`, Compose-Commit, erster Frame und Lifecycle-Resume;
- erste echte Preference-Emission und Access-Gate-Freigabe;
- nicht-kritischer GitHub-Update-Check;
- read-only Beobachtung der bestehenden Departure-State-Kette (`Idle` → `Loading` → progressive/finale `Success` oder `Error`);
- bestehende `AbfahrtLocation`-, OkHttp-, `AbfahrtWalk`- und Android-Davey-/Skipped-Frame-Logs dienen der zeitlichen Korrelation.

Interim-Evidence 04.10.2026:
- zwei vollständig instrumentierte Cold Starts plus ein teilweise erfasster Cold Start liegen vor;
- vollständige Läufe: erster Compose-Frame ca. 436/455 ms, Access-Gate ready ca. 671/705 ms, Departure-Loading ca. 733/753 ms;
- erster abfahrt.now-Core-Request erst ca. 3,45/3,34 s nach Prozessstart. Damit liegt ein reproduzierbarer Block von ca. 2,72/2,59 s zwischen `Loading` und erstem Netzwerkrequest;
- der bestehende Codepfad löst bei Current Location vor dem Netzwerk synchron-suspendierend `resolveCurrentTargetCoordinates()` → `getBestLocation()` aus; `getBestLocation()` fordert zuerst `PRIORITY_HIGH_ACCURACY` über `getCurrentLocation()` an und nutzt `lastLocation` nur, wenn kein frischer Wert geliefert wird. Die Stage-A-Zeitachse grenzt den dominanten Cold-Start-Warteblock daher ausreichend auf die Location-Auflösung ein;
- Core-Netzwerk selbst ist nicht der dominante Engpass: erste Hauptantwort ca. 162–174 ms; erster progressiver Success ca. 3,97–4,09 s; finaler Core-Success ca. 5,43–6,04 s;
- ORS ist nicht Teil des initialen Blocks: Matrix startet erst nach finalem Core-State und antwortet mit gültigem Key HTTP 200; vollständiges Walking-Enrichment ca. 7,25–8,01 s;
- keine App-`FATAL EXCEPTION`-, `AndroidRuntime`-, ANR- oder Davey-Signatur; gefundene `Skipped`-Meldungen gehören nicht zum App-Prozess.

Bewusst **nicht** in Stufe A:
- kein Umbau des großen `DepartureViewModel`;
- keine neue Dependency oder Benchmark-/Jank-Library;
- keine Lazy-Initialisierung, Dispatcher-, DataStore-, Keystore-, Netzwerk-, ORS- oder UI-Optimierung;
- keine Änderung an Routing, Filtern, Sortierung oder Provider-Semantik.

Entscheidung nach der ersten Evidence: Instrumentierungsstufe B ist derzeit **nicht erforderlich**. Stage A plus vorhandener Codepfad grenzen den Cold-Start-Warteblock ausreichend ein. Eine Optimierung wird dennoch nicht begonnen, bevor das vereinbarte Runtime-Gate vollständig ist.

Gate vor jeder Optimierungsentscheidung:
1. vollständiges Android-CI-Gate auf dem finalen Instrumentierungs-Head;
2. mindestens drei vollständig erfasste Cold Starts auf eingerichtetem Realgerät — aktuell fehlen noch ein vollständiger Lauf, weil einer der drei vorhandenen Starts nur teilweise instrumentiert vorliegt;
3. Warm-Relaunch im selben Prozess, sofern reproduzierbar, plus Home→App-Resume;
4. `AbfahrtStartup` zeitlich mit Location, Core-Netzwerk, ORS und ggf. Davey-/Skipped-Frame-Signaturen abgleichen — für die vorhandenen vollständigen Cold Starts erfüllt;
5. erst nach Abschluss des Gates gezielte Optimierung beschließen oder verwerfen.

F-ORS-001/B-ORS-001 (ORS-Key-Probe beim Hinterlegen) ist separat erfasst und wird nicht in Build 153 hineingemischt.

## Release-Grundsatz

Kein Build gilt aufgrund von Versionsnummer, Gradle-Sync, erfolgreichem CI allein oder KI-Einschätzung als akzeptiert. Maßgeblich sind die in [`11-test-and-evidence.md`](11-test-and-evidence.md) definierten realen Gates und Evidence.
