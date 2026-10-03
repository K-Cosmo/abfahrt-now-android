# Build-Handoff

## Aktueller belastbarer Stand

Runtime-Baseline bleibt **v1.1.0 Build 149 (`versionCode 1490`)**. Das öffentliche Repository `K-Cosmo/abfahrt-now-android` ist ab REPO1 der kanonische Entwicklungsworkspace.

Build 148 ist real abgenommen: kombinierter Gradle-Gate grün; Runtime-Evidence bestätigt D-069 mit roher Photon-Query, `lat/lon`-Bias und sinnvoller Provider-Reihenfolge. Build 149 implementiert D-070: HERE ist Standortzustand statt Routingfall. Der Nutzer-Screenshot bestätigt die gewünschte sichtbare Darstellung (`Hier`, `Haltestelle erreicht`, `Standort`, keine blaue Route); das formale Build-/Logcat-Gate bleibt gemäß Evidence-Regel separat zu dokumentieren.

## REPO1 — Repository-/Governance-Housekeeping

REPO1 ändert **keine Runtime-Buildnummer**. Zielzustand:
- `/doc` ist die einzige normative Produkt-/Technikquelle; kein paralleles `/docs` und kein zweiter Root-Changelog;
- README stellt das Projekt als unabhängige Community-App dar und grenzt es von der offiziellen abfahrt.now-App/API-Entwicklung ab;
- EU-first, Datenminimierung, Service-Inventar und Credential-Isolation sind in `14-community-and-service-policy.md` normativ;
- 22 gebündelte UI-Sprachen sowie erforderlicher abfahrt.now-Key und optionaler HeiGIT/openrouteservice-Key sind öffentlich dokumentiert;
- Gradle-Wrapper-Launcher und verifizierter Bootstrap stehen bereit; GitHub Actions führen Static-Gates, Unit-Tests und Debug-Build aus;
- F-DOC1-008 bleibt bis zum eingecheckten verifizierten `gradle-wrapper.jar` nur mitigiert, nicht geschlossen.

## Nächste Reihenfolge

1. **REPO1 mergen** und finalen GitHub-Actions-Gate grün halten.
2. **Build 150:** GitHub-Release-Update-Checker mit separatem anonymem Client; kein Credential-Leak, kein Auto-Install.
3. **Build 151:** AB-018 Startup/Main-Thread-Instrumentierung; erst messen, dann evidenzbasiert optimieren.

## Lokaler Workspace

Android Studio soll den Repository-Root öffnen, unter Windows beispielsweise:

```text
D:\Android\abfahrt-now-android
```

Nicht nur `...\app` öffnen. `local.properties`, Build-Ausgaben, Keystores und rohe Runtime-Evidence bleiben lokal und werden nicht committed.

## Verbindliche Gates

Für App-Builds bleibt mindestens der kombinierte Gate:

```text
:app:testDebugUnitTest :app:assembleDebug
```

Zusätzlich gelten die passenden Runtime-/Evidence-Gates aus [`11-test-and-evidence.md`](11-test-and-evidence.md). Repository-/CI-Grün ersetzt keine fachliche Runtime-Abnahme.