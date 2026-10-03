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

## Build 152 — UI/UX-Konvergenz — in Arbeit

PR #6 enthält Teil A; Android CI #52 ist mit Governance-/Static-/Locale-Gates, Unit-Tests, Debug- und Release/R8-Build grün. Das ist ein automatisierter technischer Gate, **keine** Build-152-Abnahme. F-152-001 zur Access-Gate-State-Konvergenz ist vor der Runtime-Abnahme zu klären.

Scope:
- `versionCode 1520`, `versionName 1.1.0`;
- Onboarding-Fließtext linksbündig und in verständliche Absätze gegliedert;
- kompakter RoutePlanner-Kopf mit gemeinsamer Start-/Ziel-Fläche statt zwei großen separaten Feldern und großem Zwischenraum;
- vorhandene Photon-, Saved-Places-, Swap- und `/trips`-Logik bleibt fachlich unverändert;
- API-Key-Onboarding darf bei bereits eingerichtetem Nutzer nicht kurz beim App-Start erscheinen;
- Access-Readiness wird erst nach einer echten Preference-Repository/DataStore-Emission geöffnet, nicht durch den künstlichen `stateIn`-Initialwert;
- historischer, nicht mehr genutzter `AppFooter`-Deadcode wird entfernt.

Gate:
1. F-152-001 geklärt; kein unbeabsichtigter paralleler Preference-State-Pfad bleibt zurück;
2. Locale-/Static-/Governance-Gates grün;
3. Unit-Tests + Debug + Release/R8 in GitHub Actions grün;
4. Realgerät: eingerichtete App startet ohne sichtbaren Onboarding/API-Key-Flash;
5. RoutePlanner-Kopf auf schmalem Smartphone kompakt und ohne Funktionsverlust;
6. Onboarding Schritt 1 und 2 bleiben auf kleinen Displays scrollbar und verständlich;
7. ORS→Community-Footer-Abstand visuell akzeptiert und Legacy-Footer entfernt;
8. abschließende `/doc`-Konvergenz auf den tatsächlich abgenommenen Build-152-Stand.

## Build 153 — Startup/Main-Thread-Instrumentierung (AB-018)

Nach Build 152 ausschließlich messen/instrumentieren. Keine spekulative Optimierung; konkrete Verschiebung/Lazy-Initialisierung erst anhand reproduzierbarer Messwerte.

## Release-Grundsatz

Kein Build gilt aufgrund von Versionsnummer, Gradle-Sync, erfolgreichem CI allein oder KI-Einschätzung als akzeptiert. Maßgeblich sind die in [`11-test-and-evidence.md`](11-test-and-evidence.md) definierten realen Gates und Evidence.
