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

## Build 153 — Startup/Main-Thread-Instrumentierung (AB-018) — next

Build 153 beginnt mit Messinstrumentierung der Kaltstart-/Main-Thread-Schritte auf Basis des akzeptierten Build 152. Keine spekulative Performance-Optimierung. Erst reproduzierbare Messwerte, dann problembezogene Änderung in kleinem Scope.

Mindestziel der ersten Runde:
- Zeitpunkte für Process/Activity/Compose-/Preference-Gate/Location-/initialen Departure-Fetch nachvollziehbar messen;
- Main-Thread-Arbeit identifizieren statt vermuten;
- Cold/Warm-Start getrennt erfassen;
- keine UX-/Routing-/Provider-Semantik gleichzeitig ändern.

## Release-Grundsatz

Kein Build gilt aufgrund von Versionsnummer, Gradle-Sync, erfolgreichem CI allein oder KI-Einschätzung als akzeptiert. Maßgeblich sind die in [`11-test-and-evidence.md`](11-test-and-evidence.md) definierten realen Gates und Evidence.
