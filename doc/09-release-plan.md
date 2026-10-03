# Release-Plan

Dieser Plan beschreibt die **aktuelle Reihenfolge**. Historische Build-Details liegen in [`CHANGELOG.md`](CHANGELOG.md), Regressionen in [`05-regression-ledger.md`](05-regression-ledger.md) und Findings in [`07-findings.md`](07-findings.md).

## REPO1 — abgeschlossen

Das öffentliche Repository ist der kanonische Entwicklungsworkspace. `/doc` ist die einzige normative Dokumentationswurzel, Community-/EU-first-Policy ist dokumentiert, und der vollständige Gradle-9.6.0-Wrapper wird lokal sowie in GitHub Actions direkt und erfolgreich verwendet.

## Build 150 — abgeschlossen

Der GitHub-Release-Update-Checker ist technisch accepted und in `main` integriert: credential-isolierter anonymer Client, 22 Locale-Sets, realer `releases/latest`-E2E und erfolgreicher Release-/R8-Build.

## Build 151 — abgeschlossen

Die Runtime-UI zeigt die unabhängige/unoffizielle Community-Identität. abfahrt.now bleibt als Daten-/API-Quelle sichtbar, Projektlink und API-Provider-Rechtslinks sind eindeutig beschriftet. Der Realgeräte-Screenshot ist positiv; Android CI #42 ist inklusive Locale-/Static-/Governance-Gates, Unit-Tests, Debug- und Release/R8-Build grün. F-DOC1-015 ist geschlossen.

Der Android-CI-Gate baut ab Build 151 dauerhaft sowohl Debug als auch Release, damit der Release-/R8-Nachweis nicht mehr manuell nachgeführt werden muss.

## DOC2 — Dokumentationskonvergenz nach Build 151

DOC2 ändert ausschließlich `/doc` und die zugehörige Prozessdokumentation. `versionCode 1510` / `versionName 1.1.0` und sämtliche Runtime-Dateien bleiben unverändert. Zweck ist, REPO1 sowie die bereits akzeptierten Builds 150/151 konsistent in Changelog, Entscheidungen, Evidence, Compatibility, Localization, Backlog und Handoff nachzuführen.

## Build 152 — UI/UX-Konvergenz — implementation complete / pending evidence

Draft-PR #6 enthält den vollständigen Build-152-Scope. Android CI #60 bestätigte die konvergierte Startup-Access-Gate-Architektur; Android CI #62 bestätigte anschließend den kompletten Implementierungsstand inklusive Static-/Governance-/Locale-Gates, Unit-Tests, Debug- und Release/R8-Build. Das ist ein technischer Gate, **keine** Build-152-Abnahme.

Implementierter Scope:
- `versionCode 1520`, `versionName 1.1.0`;
- Onboarding-Fließtext linksbündig und verständlich strukturiert;
- kompakter RoutePlanner-Kopf mit gemeinsamer Start-/Ziel-Fläche und platzsparender Tauschaktion;
- bestehende Photon-, Saved-Places-, Current-location-, Swap- und `/trips`-Logik bleibt fachlich unverändert;
- `AccessGateViewModel` ist alleiniger Owner der Startup-Access-Entscheidung und wartet auf eine echte Preference-Repository/DataStore-Emission; geschützte Feature-ViewModels werden erst danach erzeugt;
- ORS→Community-Footer-Abstand ist strukturell reduziert, ohne negative Padding-/Offset-Hacks;
- historischer, nicht mehr genutzter `AppFooter`-/Legal-Deadcode ist entfernt; `CommunityFooter` bleibt alleinige Footer-Implementierung.

Gate-Status:
1. **erfüllt im Code:** F-152-001 ist architektonisch konvergiert; `closed` erst nach Realgeräte-Kaltstart-Evidence;
2. **erfüllt:** Locale-/Static-/Governance-Gates grün;
3. **erfüllt:** Unit-Tests + Debug + Release/R8 in GitHub Actions grün (#62);
4. **offen:** Realgerät mit eingerichtetem Key startet wiederholt ohne sichtbaren Onboarding/API-Key-Flash;
5. **offen:** RoutePlanner-Kopf auf schmalem Smartphone kompakt und ohne Funktionsverlust; Start-/Zielsuche, Current location, Home/Work, Swap und `Route finden` prüfen;
6. **offen:** Onboarding Schritt 1 und 2 bleiben auf kleinen Displays scrollbar und verständlich;
7. **offen:** ORS→Community-Footer-Abstand visuell akzeptieren;
8. **offen:** nach realer Evidence finalen Build-152-Status nach `/doc` konvergieren, PR aus Draft nehmen und erst dann mergen.

## Build 153 — Startup/Main-Thread-Instrumentierung (AB-018)

Nach Build 152 ausschließlich messen/instrumentieren. Keine spekulative Optimierung; konkrete Verschiebung/Lazy-Initialisierung erst anhand reproduzierbarer Messwerte.

## Release-Grundsatz

Kein Build gilt aufgrund von Versionsnummer, Gradle-Sync, erfolgreichem CI allein oder KI-Einschätzung als akzeptiert. Maßgeblich sind die in [`11-test-and-evidence.md`](11-test-and-evidence.md) definierten realen Gates und Evidence.
