# Release-Plan

Dieser Plan beschreibt die **aktuelle Reihenfolge**. Historische Build-Details liegen in [`CHANGELOG.md`](CHANGELOG.md), Regressionen in [`05-regression-ledger.md`](05-regression-ledger.md) und Findings in [`07-findings.md`](07-findings.md).

## REPO1 — abgeschlossen

Das öffentliche Repository ist der kanonische Entwicklungsworkspace. `/doc` ist die einzige normative Dokumentationswurzel, Community-/EU-first-Policy ist dokumentiert, und der vollständige Gradle-9.6.0-Wrapper wird lokal sowie in GitHub Actions direkt und erfolgreich verwendet. REPO1 verändert keine Runtime-Buildnummer.

## Build 150 — GitHub Release Update Checker

Scope:
- `versionCode 1500`, `versionName 1.1.0`;
- separater anonymer GitHub-HTTP-Client ohne abfahrt.now-/ORS-Credentials;
- einmaliger asynchroner Check pro App-/Activity-Start gegen `releases/latest`;
- Release-Tag-Schema `v<versionName>-b<build>` und Vergleich über die monotone Buildnummer;
- Fehler/Offline/404/Rate-Limit bleiben still und beeinträchtigen die Kernfunktion nicht;
- lokalisierter Update-Hinweis in allen gebündelten UI-Sprachen;
- Aktion öffnet nur die konkrete Release-Seite des festen Community-Repositories;
- keine stille Installation und kein automatischer APK-Download.

Automatischer Gate:
- Locale-Parität über alle String-Ressourcen der 22 Locale-Sets;
- Unit-Tests für Tag-Parsing/Buildvergleich;
- Test, dass der GitHub-Client keinen `ApiKeyInterceptor` enthält;
- `:app:testDebugUnitTest :app:assembleDebug` in GitHub Actions.

Runtime-/Release-Gate:
1. ohne vorhandenes GitHub-Release bzw. bei Offline/Fehler: normale App-Nutzung ohne sichtbaren Update-Fehler;
2. für echten Ende-zu-Ende-Test: ältere App installieren, Release mit höherem Tag nach Schema bereitstellen, Update-Hinweis prüfen und Release-Seite öffnen;
3. nach Installation derselben/neuesten Buildnummer darf kein Update-Hinweis mehr erscheinen.

**Wichtig:** Ein öffentlicher Community-Release soll erst erfolgen, nachdem B-COMMUNITY-001/F-DOC1-015 die In-App-About-/Legal-Abgrenzung zur unabhängigen Community-App konvergiert hat. Build 150 kann technisch abgenommen werden, ohne diese Release-Sperre zu umgehen.

## Danach — Community-Release-Konvergenz und Performance

Vor dem ersten öffentlichen Community-App-Release wird F-DOC1-015 als eigene kleine Produktänderung geschlossen. Die bereits geplante AB-018 Startup/Main-Thread-Instrumentierung bleibt danach separat: zuerst messen, keine spekulative Optimierung.

## Release-Grundsatz

Kein Build gilt aufgrund von Versionsnummer, Gradle-Sync oder KI-Einschätzung als akzeptiert. Maßgeblich sind die in [`11-test-and-evidence.md`](11-test-and-evidence.md) definierten realen Gates und Evidence.
