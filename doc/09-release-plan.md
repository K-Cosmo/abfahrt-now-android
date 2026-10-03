# Release-Plan

Dieser Plan beschreibt die **aktuelle Reihenfolge**. Historische Build-Details liegen in [`CHANGELOG.md`](CHANGELOG.md), Regressionen in [`05-regression-ledger.md`](05-regression-ledger.md) und Findings in [`07-findings.md`](07-findings.md).

## REPO1 — abgeschlossen

Das öffentliche Repository ist der kanonische Entwicklungsworkspace. `/doc` ist die einzige normative Dokumentationswurzel, Community-/EU-first-Policy ist dokumentiert, und der vollständige Gradle-9.6.0-Wrapper wird lokal sowie in GitHub Actions direkt und erfolgreich verwendet.

## Build 150 — abgeschlossen

Der GitHub-Release-Update-Checker ist technisch accepted und in `main` integriert: credential-isolierter anonymer Client, 22 Locale-Sets, realer `releases/latest`-E2E und erfolgreicher Release-/R8-Build.

## Build 151 — Community-Identität in der Runtime-UI

Scope:
- `versionCode 1510`, `versionName 1.1.0`;
- bestehendes Settings-/About-Footer-Layout bleibt grundsätzlich erhalten;
- sichtbare Identität wird auf unabhängige/unoffizielle Community-App konvergiert;
- abfahrt.now wird eindeutig als Daten-/API-Quelle bezeichnet, nicht als App-Betreiber;
- Projektlink zeigt auf das kanonische GitHub-Repository;
- externe Privacy-/Terms-Links werden ausdrücklich als API-Provider-Links bezeichnet;
- neue Texte in allen 22 gebündelten Locale-Sets;
- keine Änderung an Transitlogik, Routing, Credentials, Update-Checker oder Performance.

Gate:
1. Locale-/Static-/Governance-Gates grün;
2. `:app:testDebugUnitTest :app:assembleDebug` grün;
3. `:app:assembleRelease` grün;
4. realer Settings-Footer-Smoke: Community-Abgrenzung sichtbar, GitHub-Link korrekt, API Privacy/Terms korrekt beschriftet und verlinkt;
5. erst danach F-DOC1-015 schließen und ersten öffentlichen Community-App-Release freigeben.

## Build 152 — Startup/Main-Thread-Instrumentierung (AB-018)

Nach Build 151 ausschließlich messen/instrumentieren. Keine spekulative Optimierung; konkrete Verschiebung/Lazy-Initialisierung erst anhand reproduzierbarer Messwerte.

## Release-Grundsatz

Kein Build gilt aufgrund von Versionsnummer, Gradle-Sync oder KI-Einschätzung als akzeptiert. Maßgeblich sind die in [`11-test-and-evidence.md`](11-test-and-evidence.md) definierten realen Gates und Evidence.
