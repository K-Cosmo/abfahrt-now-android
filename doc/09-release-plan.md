# Release-Plan

Dieser Plan beschreibt die **aktuelle Reihenfolge**. Historische Build-Details liegen in [`CHANGELOG.md`](CHANGELOG.md), Regressionen in [`05-regression-ledger.md`](05-regression-ledger.md) und Findings in [`07-findings.md`](07-findings.md).

## REPO1 — Public-Repo-Housekeeping (keine Runtime-Buildnummer)

Scope:
- öffentliches Community-Projekt eindeutig von der offiziellen abfahrt.now-App/API-Entwicklung abgrenzen;
- `/doc` als einzige normative Dokumentationswurzel konsolidieren; `/docs` und doppelten Root-CHANGELOG entfernen;
- EU-first-/Credential-Isolation und aktuelles Service-Inventar normativ dokumentieren;
- README mit 22 gebündelten UI-Sprachen, erforderlichem abfahrt.now-Key und optionalem HeiGIT/openrouteservice-Key;
- Gradle-Wrapper-Launcher, verifizierter Bootstrap und GitHub-Actions-Gate.

Gate: Repo-Governance-/Static-Checks plus GitHub Actions `:app:testDebugUnitTest :app:assembleDebug` grün. `versionCode` bleibt **1490**.

## Build 150 — GitHub Release Update Checker

Scope:
- separater anonymer GitHub-HTTP-Client ohne abfahrt.now-/ORS-Credentials;
- einmaliger asynchroner Check pro App-Start gegen die öffentliche GitHub-Release-Metadatenquelle;
- Release-Tag-Schema `v<versionName>-b<build>` und Vergleich über die monotone Buildnummer;
- Fehler/Offline bleiben still und beeinträchtigen die Kernfunktion nicht;
- unaufdringlicher Update-Hinweis; Aktion öffnet die konkrete Release-Seite;
- keine stille Installation und kein automatischer APK-Download.

Gate: Unit-Tests für Tag-Parsing/Buildvergleich, kombinierter Gradle-Gate und Ende-zu-Ende-Test **Build 149 installiert → Release `v1.1.0-b150` verfügbar → Hinweis erscheint → Build 150 installiert → kein Update-Hinweis**.

## Build 151 — Startup/Main-Thread-Instrumentierung (AB-018)

Scope: zuerst ausschließlich messen/instrumentieren, insbesondere Kaltstart und größere Main-Thread-Schritte. Keine spekulative Optimierung ohne Ursache/Evidence.

Gate: reproduzierbare Messwerte auf realem Startpfad; erst danach wird separat entschieden, welche Arbeit verschoben oder lazy initialisiert werden soll.

## Release-Grundsatz

Kein Build gilt aufgrund von Versionsnummer, Gradle-Sync oder KI-Einschätzung als akzeptiert. Maßgeblich sind die in [`11-test-and-evidence.md`](11-test-and-evidence.md) definierten realen Gates und Evidence.