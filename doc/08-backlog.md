# Backlog

Diese Datei enthält den **aktiven** Arbeitsvorrat. Abgeschlossene Build-Historie liegt in [`CHANGELOG.md`](CHANGELOG.md), Regressionen und Schutzmaßnahmen in [`05-regression-ledger.md`](05-regression-ledger.md).

## P0 — aktuelle Reihenfolge

### B-152-001 UI/UX-Konvergenz
- **Status:** implementation complete in Draft-PR #6; Android CI #62 ist grün, reale Abnahme steht aus.
- Erststart/Onboarding: Fließtext linksbündig statt Blocksatz; verpflichtender abfahrt.now-Key und optionaler ORS-Hinweis bleiben als getrennte, einfache Information sichtbar.
- RoutePlanner: Start/Ziel-Eingabe als gemeinsamer kompakter Container mit zwei Zeilen, Trenner und platzsparender Tauschaktion; bestehende Photon-/Saved-Places-/`/trips`-Logik wird weiterverwendet.
- Startup-Access-Gate: `AccessGateViewModel` ist alleiniger Owner der Start-/Onboarding-Entscheidung und wartet auf die erste echte Preference-Emission. Geschützte Feature-ViewModels entstehen erst danach; der `DepartureViewModel.preferences`-State bleibt Feature-/Settings-State.
- Settings-Footer: gestapelte Root-Spacer vor dem `CommunityFooter` entfernt; Footer als ein zusammenhängender Block in den 24-dp-Settings-Rhythmus integriert.
- historischer, ungenutzter `AppFooter`-/Legal-Deadcode inklusive Riles-Tech-Link ist entfernt; `CommunityFooter` bleibt die einzige Runtime-Footer-Implementierung.
- **Noch offen:** wiederholte Kaltstarts mit eingerichtetem Key ohne Onboarding-Flash; RoutePlanner-Smoke für Start-/Zielsuche, Current location, Home/Work, Swap und `Route finden`; Compact-Width-Smoke für RoutePlanner und Onboarding; Footer-Abstand visuell abnehmen; finale Build-152-Abnahme und Merge.

### B-153-001 Startup/Main-Thread-Instrumentierung (AB-018)
- **Status:** planned after Build 152.
- zunächst Messinstrumentierung der Kaltstart-/Main-Thread-Schritte; keine Optimierung ohne Ursache/Evidence.
- erst nach Messung gezielte Verschiebung/Lazy-Initialisierung, falls tatsächlich erforderlich.

### B-149-001 HERE-Detailsheet als Standortkarte
- **Status:** implemented; visueller Nutzer-Smoke positiv, formales Build-/Logcat-Gate bleibt gemäß Evidence-Regel zu dokumentieren.
- HERE short-circuited ORS und zeigt bei vorhandenen Koordinaten nur Query-Origin + Haltestellenmarker.
- Nicht-HERE-RoutePreview bleibt unverändert.

## Abgeschlossen

### B-COMMUNITY-001 In-App-Community-Abgrenzung — Build 151
- **Status:** closed Build 151.
- Settings-Footer zeigt die App als unabhängiges Community-Projekt und grenzt sie ausdrücklich von abfahrt.now ab.
- abfahrt.now bleibt als Daten-/API-Quelle sichtbar, nicht als App-Betreiber.
- Projektlink zeigt auf `K-Cosmo/abfahrt-now-android`; Privacy/Terms sind als API-Provider-Links beschriftet.
- alle sechs Community-Texte sind in 22 Locale-Sets vorhanden.
- Realgeräte-Screenshot positiv; Android CI #42 inklusive Debug-, Unit-Test- und Release/R8-Build grün.

### B-150-001 GitHub Release Update Checker
- **Status:** closed Build 150; in `main` integriert.
- anonymer, credential-isolierter `releases/latest`-Check; kein GitHub-Token und keine API-Key-Weitergabe.
- striktes `v<versionName>-b<build>`-Schema und monotone Buildnummer.
- lokalisierter Hinweis in 22 UI-Sprachen; feste Release-Seite; kein APK-Autodownload/Installer.
- CI, realer E2E und Release-/R8-Build erfolgreich.

### B-REPO1-001 Public-Repo-Housekeeping / Community-Baseline
- **Status:** closed.
- `/doc` ist einzige normative Quelle; `/docs` und doppelter Root-`CHANGELOG.md` entfernt.
- README/Service-Policy grenzen die Community-App klar von abfahrt.now ab und dokumentieren EU-first, 22 gebündelte UI-Locale-Sets und benötigte API-Keys.
- vollständiger Gradle-9.6.0-Wrapper inklusive JAR eingecheckt; lokaler Windows-Gate und GitHub Actions mit direkter Wrapper-Nutzung grün.

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
