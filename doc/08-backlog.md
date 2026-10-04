# Backlog

Diese Datei enthält den **aktiven** Arbeitsvorrat. Abgeschlossene Build-Historie liegt in [`CHANGELOG.md`](CHANGELOG.md), Regressionen und Schutzmaßnahmen in [`05-regression-ledger.md`](05-regression-ledger.md).

## P0 — aktuelle Reihenfolge

### B-154-001 Current-Location-Startup beschleunigen (F-153-001)
- **Status:** specified in `specs/BUILD154/`; Implementierung noch nicht begonnen.
- Ziel: den in drei sauberen Cold Starts reproduzierten Warteblock vor dem ersten abfahrt.now-Core-Request reduzieren, ohne die bereits bewusste progressive First-Paint-/Add-on-/ORS-/Stable-Merge-Semantik zu verändern.
- erneuter Audit von `/doc`, Code und Projektverlauf: Die relevante Re-Anchor-Schwelle ist **bereits** definiert. `MOVEMENT_THRESHOLD_M = 200f`; `< 200 m` bleibt Same-Origin, `>= 200 m` nutzt den bestehenden Hard-Reset-/Pending-Refresh-Pfad.
- für `FusedLocationProviderClient.lastLocation` existiert dagegen keine separate normative Alters- oder Accuracy-Schwelle. Der aktuelle Code akzeptiert `lastLocation` bereits ohne solche Prüfung als Fallback, wartet davor aber auf `getCurrentLocation(PRIORITY_HIGH_ACCURACY)`. Build 154 darf deshalb keine 30-s/60-s/5-min- oder Accuracy-Magic-Number erfinden.
- spezifizierte Fast-Path-Richtung: vorhandene `lastLocation` auf leerem Current-Location-Kaltstart nur als **provisorischen First-Paint-Origin** verwenden und den frischen High-Accuracy-Fix parallel nachziehen. Fehlt `lastLocation`, bleibt der heutige High-Accuracy-Pfad unverändert.
- spätere High-Accuracy-Korrektur `< 200 m`: kein zweiter Core-Request und kein zusätzlicher ORS-Zyklus allein wegen der Korrektur. `>= 200 m`: genau ein bestehender Hard-Reset-Re-Anchor; bei laufendem Load über `pendingLocationRefresh`/`pendingHardResetRefresh` nachziehen, kein paralleler Request-Sturm.
- der nachgelagerte Datenfluss bleibt unverändert: API-Dedup-Booster nur auf leerem Kaltstart, Direct-stop/Add-ons `dedup=off`, app-eigene Filter/Dedup/Sortierung, ORS asynchron, Same-Origin-Refresh als Stable-Merge, Cross-Origin-Hard-Reset ohne Loading-Blackout.
- kein persistenter Standortcache, keine neue Dependency, keine Vermischung von API-Daten-Freshness (`refreshIntervalMinutes`/60-s-Throttle) mit Location-Freshness.
- Acceptance: erster Core-Request muss bei vorhandener `lastLocation` real vor Abschluss des High-Accuracy-Fixes beginnen; realen Gewinn gegen Build-153-Baseline `Loading`→Core ca. 2,59–3,02 s messen. Bei störender Standort-/Refresh-Unruhe Fast Path verwerfen statt weiter zu verkomplizieren.

### B-149-001 HERE-Detailsheet als Standortkarte
- **Status:** implemented; visueller Nutzer-Smoke positiv, formales Build-/Logcat-Gate bleibt gemäß Evidence-Regel zu dokumentieren.
- HERE short-circuited ORS und zeigt bei vorhandenen Koordinaten nur Query-Origin + Haltestellenmarker.
- Nicht-HERE-RoutePreview bleibt unverändert.

## P1 — nächster Hardening-Block

### B-ORS-001 ORS-Key beim Hinterlegen validieren
- **Status:** planned aus F-ORS-001; bewusst getrennt von Build 153/154.
- Beim erstmaligen Hinterlegen und beim Ändern des ORS-Keys vor persistenter Übernahme eine kleine, nicht-sensitive ORS-Probe ausführen.
- HTTP 401/403: neuen Key nicht akzeptieren; bei Änderung einen bereits gültigen gespeicherten Key nicht überschreiben; klare Fehlermeldung anzeigen.
- Netzwerkfehler/5xx: als temporär/unprüfbar behandeln, nicht als ungültigen Key klassifizieren. HTTP 429 bedeutet gültiger Zugriffspfad mit Limitproblem und darf den Key nicht als syntaktisch/fachlich ungültig markieren.
- Keine Keys, Authorization-Header oder Secret-Inhalte loggen.
- Separat prüfen, ob bei einem späteren 401/403 im normalen ORS-Enrichment weitere Fallback-Requests mit demselben Key früh beendet werden sollen.

## Abgeschlossen

### B-153-001 Startup/Main-Thread-Instrumentierung (AB-018) — Build 153
- **Status:** closed / accepted 04.10.2026.
- `versionCode = 1530`, `versionName = 1.1.0`; Android-/Toolchain-Baseline unverändert.
- dependency-freie `StartupTrace`-Diagnostik misst Application/MapLibre, Activity/Compose/ersten Frame, AccessGate/erste echte Preferences, Update-Check und die vorhandene Departure-State-Kette; der große `DepartureViewModel` blieb in Stage A unverändert.
- Android CI #94 vollständig grün: Static/Governance/Compatibility, committed Wrapper, Unit Tests, Debug und Release/R8.
- drei saubere vollständig instrumentierte Cold Starts liegen vor. Die ersten beiden zeigen `Loading`→erster Core-Request ca. 2,72/2,59 s; der finale saubere Lauf ca. 3,02 s. Damit ist der vor dem Netzwerk liegende Current-Location-Warteblock reproduziert.
- finaler sauberer Lauf: Application/MapLibre ca. 27 ms, erster Compose-Frame ca. 0,52 s, AccessGate ready ca. 0,77 s, Departure Loading ca. 0,83 s; erster Core-Request erst ca. 3,85 s nach Prozessstart. Die erste Core-Hauptantwort brauchte in diesem Lauf 629 ms; spätere Add-on-Netzwerkantworten variierten stärker, ändern aber die Lokalisierung des initialen Warteblocks nicht.
- ORS startet nach finalem Core-State asynchron und antwortet mit korrigiertem Key HTTP 200; beide Matrix-Batches werden vollständig geparst.
- Same-Process-Home→App-Resume ist real erfasst: Activity `onStop`, später `onStart`/`onResume` im selben Prozess ohne neues `onCreate`. Ein separater Warm-Activity-Recreate war nicht reproduzierbar und war laut Gate nur „sofern reproduzierbar“ gefordert.
- der finale Gesamtlog enthält außerdem einen durch Doze/Wake und frühes Activity-Stop/Resume verunreinigten Lauf mit Choreographer-Skips; dieser wird ausdrücklich nicht als sauberer Cold-Start-Benchmark verwendet. Im sauberen finalen Cold-Start-Segment erscheinen keine `Choreographer: Skipped`-Zeilen.
- keine App-`FATAL EXCEPTION`-, App-`AndroidRuntime`-/Process-Crash-, ANR- oder Navigation-Regression im Abnahmeumfang. `AndroidRuntime`-Treffer des Gesamtlogs gehören zum Shell-`monkey`-Prozess und enden regulär.
- Stage A grenzt die Ursache ausreichend ein; zusätzliche Stage-B-Marker sind nicht erforderlich. Die Optimierung selbst wird isoliert in Build 154 umgesetzt.

### B-152-001 UI/UX-Konvergenz — Build 152
- **Status:** closed / accepted 04.10.2026.
- `versionCode = 1520`, `versionName = 1.1.0`, `minSdk 34`, `targetSdk 37` real bestätigt.
- Erststart/Onboarding: Fließtext linksbündig; kompakte, weiterhin scrollbare Darstellung.
- Startup-Access-Gate: `AccessGateViewModel` wartet auf eine echte Preference-Emission. Der frühere API-Key-/Onboarding-Flicker ist real bestätigt beseitigt (F-152-001 closed).
- Location-Follow-up: kein `Standort erlauben`-Flicker mehr bei bereits erteilter Berechtigung (F-152-002 closed).
- RoutePlanner, Settings-Footer und Community-Footer real/automatisiert abgenommen; Android CI #81 vollständig grün.

### B-COMMUNITY-001 In-App-Community-Abgrenzung — Build 151
- **Status:** closed Build 151.
- Settings-Footer zeigt die App als unabhängiges Community-Projekt; abfahrt.now bleibt klar als Daten-/API-Quelle sichtbar.
- Projektlink zeigt auf `K-Cosmo/abfahrt-now-android`; Privacy/Terms sind als API-Provider-Links beschriftet.
- alle sechs Community-Texte sind in 22 Locale-Sets vorhanden; Android CI #42 inklusive Release/R8 grün.

### B-150-001 GitHub Release Update Checker
- **Status:** closed Build 150; in `main` integriert.
- anonymer, credential-isolierter `releases/latest`-Check; kein GitHub-Token und keine API-Key-Weitergabe.
- striktes `v<versionName>-b<build>`-Schema und monotone Buildnummer.
- lokalisierter Update-Hinweis in 22 UI-Sprachen; feste Release-Seite; kein APK-Autodownload/Installer.
- CI, realer E2E und Release-/R8-Build erfolgreich.

### B-REPO1-001 Public-Repository-/Governance-Baseline
- **Status:** closed.
- `/doc` ist einzige normative Quelle; `/docs` und doppelter Root-`CHANGELOG.md` entfernt.
- README/Service-Policy grenzen die Community-App klar von abfahrt.now ab und dokumentieren EU-first, Runtime-Dienste und benötigte API-Keys.
- vollständiger Gradle-9.6.0-Wrapper inklusive JAR eingecheckt; lokaler Windows-Gate und GitHub Actions grün.

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
