# Backlog

Diese Datei enthält den **aktiven** Arbeitsvorrat. Abgeschlossene Build-Historie liegt in [`CHANGELOG.md`](CHANGELOG.md), Regressionen und Schutzmaßnahmen in [`05-regression-ledger.md`](05-regression-ledger.md).

## P0 — aktuelle Reihenfolge

### B-REPO1-001 Public-Repo-Housekeeping / Community-Baseline
- **Status:** implemented on housekeeping branch; final CI/merge evidence pending.
- `/doc` bleibt einzige normative Quelle; `/docs` und doppelter Root-`CHANGELOG.md` werden entfernt.
- README/Service-Policy grenzen die Community-App klar von abfahrt.now ab und dokumentieren EU-first, 22 gebündelte UI-Locale-Sets und benötigte API-Keys.
- Gradle-Wrapper-Launcher, verifizierter Bootstrap und GitHub Actions werden ergänzt; F-DOC1-008 bleibt bis zum eingecheckten verifizierten `gradle-wrapper.jar` offen.

### B-150-001 GitHub Release Update Checker
- **Status:** planned; follows REPO1 merge.
- einmaliger asynchroner Check pro App-Start gegen öffentliche GitHub-Release-Metadaten; kein GitHub-Token.
- separater anonymer GitHub-HTTP-Client; keinerlei Wiederverwendung des abfahrt.now-Clients/`ApiKeyInterceptor`.
- Release-Tag-Schema `v<versionName>-b<build>`; Vergleich über monotone Buildnummer.
- Update-Hinweis bleibt unaufdringlich; Aktion öffnet die konkrete Release-Seite. Keine stille Installation und kein automatischer APK-Download.

### B-151-001 Startup/Main-Thread-Instrumentierung (AB-018)
- **Status:** planned after Build 150.
- zunächst Messinstrumentierung der Kaltstart-/Main-Thread-Schritte; keine Optimierung ohne Ursache/Evidence.
- erst nach Messung gezielte Verschiebung/Lazy-Initialisierung, falls tatsächlich erforderlich.

### B-149-001 HERE-Detailsheet als Standortkarte
- **Status:** implemented; visueller Nutzer-Smoke positiv, formales Build-/Logcat-Gate bleibt gemäß Evidence-Regel zu dokumentieren.
- HERE short-circuited ORS und zeigt bei vorhandenen Koordinaten nur Query-Origin + Haltestellenmarker.
- Nicht-HERE-RoutePreview bleibt unverändert.

## Blockiert / abhängig von externem Contract

### B-146-001 Routing time selection
- **Status:** blocked by API contract.
- aktuelles `/trips` unterstützt nur Start-/Zielkoordinaten. Keine clientseitige Zeit-Simulation; erst nach offizieller API-Erweiterung um Abfahrts-/Ankunftszeit.

## P2 — laufendes Hardening / Optionen

### B-DOC1-014 abfahrt.now Contract-Regressionstests
Reale API-Samples ohne Secrets bei künftigen Contract-Änderungen weiter ausbauen; unbekannte Forward-Compatible-Felder müssen parserseitig toleriert werden.

### B-DOC1-015 `Station.walkSeconds` bewerten
Build 134 beobachtete zweimal 40/40 positive Werte. Nutzung höchstens separat als WALK-Fallback/Provisional-ETA evaluieren; Gehwegdistanz, Bike-Zeit, Geometrie und Mehrregionen-Evidence fehlen weiterhin.

### B-DOC1-016 `/journey` bewerten
Funktionalen/UX-Nutzen für verbleibende Stopps einer konkreten Fahrt definieren, bevor eine Integration geplant wird.

### B-DOC1-009 Provider-Stop-Point-/Plattform-Semantik (AB-049)
Mehrregionen-/API-Beleg sammeln; `::N` darf nicht ohne Contract als universelle Plattformsemantik interpretiert werden.

### B-DOC1-010 Datengetriebene Linienfarben
GTFS/API-Farben statt wachsender lokaler Hardcode-Liste, ohne Cross-Region-Fehlfarben.

### B-DOC1-011 ViewModel-/Lifecycle-Hotspots
AB-010/011/013 und B-005 nur mit konkretem Anlass abbauen; kein Big-Bang-Refactor.

### B-DOC1-012 ORS-/RoutePreview-Restbeobachtung
ORS-Abdeckung, Jank und MapLibre-Lifecycle nur anhand neuer Evidence weiter optimieren.

### B-DOC1-013 Reproduzierbares Source-Paket
Verifizierten `gradle-wrapper.jar` einchecken und damit F-DOC1-008 vollständig schließen.

### B-COMMUNITY-001 In-App-Community-Abgrenzung
Vor dem ersten öffentlichen Community-Release die bestehende About-/Legal-UX mit der neuen Community-Identität konvergieren. Insbesondere darf die App keine offizielle Zugehörigkeit zu abfahrt.now oder einem API-Entwickler suggerieren. Siehe F-DOC1-015.