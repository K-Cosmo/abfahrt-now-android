# Backlog

Diese Datei enthält den **aktiven** Arbeitsvorrat. Abgeschlossene Build-Historie liegt in [`CHANGELOG.md`](CHANGELOG.md), Regressionen und Schutzmaßnahmen in [`05-regression-ledger.md`](05-regression-ledger.md).

## P0 — aktuelle Reihenfolge

### B-156-001 Sichtbaren App-Namen differenzieren (F-NAME-001)
- **Status:** in progress / Build 156.
- Anlass: Der aktuelle sichtbare Name `Abfahrt!` kollidiert mit dem Namen der offiziellen abfahrt.now-Android-App und schwächt die in D-072 geforderte Community-Abgrenzung.
- Entscheidung: sichtbarer Produktname **Abfahrtsradar**; Package-ID `now.abfahrt.transit` und Update-Signing-Lineage bleiben unverändert. Kanonisches GitHub-Repository ist `K-Cosmo/abfahrtsradar-android`.
- Repository-Rename ist erfolgt; alter `releases/latest`-Pfad liefert HTTP 301, neuer Pfad liefert `v1.1.0-b155`. Build 156 stellt Runtime/Locales/öffentliche Links auf den neuen kanonischen Pfad um.
- KIS: keine Umbenennung von technischen Identitäten ohne Not; zunächst nur sichtbarer Produktname, App-Label und zugehörige Community-Texte.


### B-149-001 HERE-Detailsheet als Standortkarte
- **Status:** implemented; visueller Nutzer-Smoke positiv, formales Build-/Logcat-Gate bleibt gemäß Evidence-Regel zu dokumentieren.
- HERE short-circuited ORS und zeigt bei vorhandenen Koordinaten nur Query-Origin + Haltestellenmarker.
- Nicht-HERE-RoutePreview bleibt unverändert.

## P1 — nächster Hardening-Block

### B-ORS-001 ORS-Key beim Hinterlegen validieren
- **Status:** planned aus F-ORS-001; bewusst getrennt von Startup, Sortierung und Release-Signing.
- Beim erstmaligen Hinterlegen und beim Ändern des ORS-Keys vor persistenter Übernahme eine kleine, nicht-sensitive ORS-Probe ausführen.
- HTTP 401/403: neuen Key nicht akzeptieren; bei Änderung einen bereits gültigen gespeicherten Key nicht überschreiben; klare Fehlermeldung anzeigen.
- Netzwerkfehler/5xx: als temporär/unprüfbar behandeln, nicht als ungültigen Key klassifizieren. HTTP 429 bedeutet gültiger Zugriffspfad mit Limitproblem und darf den Key nicht als syntaktisch/fachlich ungültig markieren.
- Keine Keys, Authorization-Header oder Secret-Inhalte loggen.
- Separat prüfen, ob bei einem späteren 401/403 im normalen ORS-Enrichment weitere Fallback-Requests mit demselben Key früh beendet werden sollen.

## Abgeschlossen

### B-REL-001 Erstes signiertes GitHub-APK-Release (F-REL-001) — RELEASE1
- **Status:** closed / released 06.10.2026.
- Öffentlicher Release: `v1.1.0-b155` auf Commit `85df24b280f60e47d813d17aa93f400b22fca787`.
- Asset: `abfahrt-now-v1.1.0-b155.apk`, SHA-256 `0FE1A8D7EB8A6038FF4446DD4F696BA37737875982945A60DF860ABE255BD9A5`.
- Historischer Release-Signer wurde kontrolliert auf den neutralen Community-Signer rotiert; die Signing-Certificate-Lineage erhält die Update-Kompatibilität bestehender Installationen.
- reales Old-Key→New-Key-In-place-Update per `adb install -r` erfolgreich; kein Deinstallations-Workaround.
- finaler APK-Signer: `CN=Abfahrt Now Community, C=DE`, Zertifikat-SHA-256 `23283ed09731c3711d5f223f0424323697243000a78cbbf0731e4553946325f8`.
- finales APK nach Signing mit `apksigner verify` und `zipalign -c -P 16 -v 4` verifiziert; offizieller 16-KB-Emulator-Smoke grün.
- GitHub `releases/latest` liefert den veröffentlichten Tag `v1.1.0-b155` samt APK-Asset.
- Release: https://github.com/K-Cosmo/abfahrtsradar-android/releases/tag/v1.1.0-b155

### B-155-001 Wählbare Sortierprofile für die Abfahrtsseite (F-SORT-001) — Build 155
- **Status:** closed / accepted 04.10.2026.
- `versionCode = 1550`, `versionName = 1.1.0`; Android CI #125 auf Runtime-Head vollständig grün inklusive Governance/Compatibility, Wrapper, Unit Tests, Debug und Release/R8.
- persistente Profile in bestehendem DataStore/`AppPreferences`:
  1. **Nähe zuerst / `NEARBY`** — Entfernung → Abfahrtszeit → Richtung → Linie; neuer Default.
  2. **Nächste Abfahrt / `SOONEST`** — Abfahrtszeit → Entfernung → Richtung → Linie.
  3. **Linien bündeln / `LINE_GROUPED`** — Entfernung → Linie → Abfahrtszeit → Richtung; Legacy-Sicht.
- Settings-Platzierung direkt nach „Abfahrten pro Richtung“ und vor Quick-Filtern; Radio-Auswahl auf Realgerät positiv bestätigt.
- `DepartureDisplayOrdering` bleibt einzige Comparator-Quelle; kein Sortieren in Composables.
- Profilwechsel triggert ausschließlich lokales `refilter()` über den vorhandenen Response-State; im Realgeräte-Log kein `departure_state_loading` und kein unmittelbar gekoppelter `/departures`-/ORS-Zyklus.
- Nutzer bestätigt, dass die Hauptseite bei allen drei Profilen entsprechend sortiert und die gewählte Einstellung nach `force-stop`/Neustart erhalten bleibt.
- keine neue API-, Dedup-, Merge-, ORS-, Location- oder First-Paint-Semantik.
- Build 155 wurde als RELEASE1 / `v1.1.0-b155` am 06.10.2026 veröffentlicht.

### B-154-001 Current-Location-Startup beschleunigen (F-153-001) — Build 154
- **Status:** closed / accepted 04.10.2026.
- `versionCode = 1540`, `versionName = 1.1.0`; Android CI #101/#106 grün inklusive Release/R8.
- vorhandene `lastLocation` dient auf leerem Current-Location-Kaltstart als provisorischer First-Paint-Origin; High Accuracy validiert parallel. Keine neue Location-Freshness-Magic-Number.
- bestehender 200-m-Vertrag bleibt unverändert: `< 200 m` Same-Origin, `>= 200 m` bestehender Hard-Reset-/Pending-Refresh-Pfad.
- drei reale Cold Starts: `Loading`→erster Core-Request ca. **29 / 30 / 25 ms** gegenüber Build-153-Baseline **2,59–3,02 s**.
- High-Accuracy-Korrekturen 6 / 0 / 8 m; kein Korrektur-bedingter Ersatz-Core und keine zusätzliche ORS-Runde beobachtet.
- Nutzer bestätigt die drastisch verkürzte sichtbare Ladezeit ohne störende Standort-/Refresh-Unruhe.
- bereinigte Evidence: `/evidence/public/build-154/2026-10-04_acceptance.md`.

### B-153-001 Startup/Main-Thread-Instrumentierung (AB-018) — Build 153
- **Status:** closed / accepted 04.10.2026.
- `versionCode = 1530`, `versionName = 1.1.0`; dependency-freie Startup-Diagnostik und drei reale Cold Starts lokalisierten den wiederholbaren 2,59–3,02-s-Block vor dem ersten Core-Request auf die Current-Location-Auflösung.
- Android CI #94 grün; Same-Process-Resume und ORS-after-Core belegt.

### B-152-001 UI/UX-Konvergenz — Build 152
- **Status:** closed / accepted 04.10.2026.
- Startup-Access-Gate und Permission-Idle-Flicker behoben; kompakter RoutePlanner und Footer abgenommen; Android CI #81 grün.

### B-COMMUNITY-001 In-App-Community-Abgrenzung — Build 151
- **Status:** closed Build 151.
- unabhängige Community-Identität, GitHub-Link und API-Provider-Zuordnung; 22 Locale-Sets, Release/R8 grün.

### B-150-001 GitHub Release Update Checker
- **Status:** closed Build 150; in `main` integriert.
- anonymer credential-isolierter `releases/latest`-Check; striktes `v<versionName>-b<build>`-Schema; kein APK-Autodownload/Installer.

### B-REPO1-001 Public-Repository-/Governance-Baseline
- **Status:** closed.
- `/doc` ist einzige normative Quelle; vollständiger Gradle-9.6.0-Wrapper und GitHub Actions grün.

## Blockiert / abhängig von externem Contract

### B-146-001 Routing time selection
- **Status:** blocked by API contract.
- aktuelles `/trips` unterstützt nur Start-/Zielkoordinaten. Keine clientseitige Zeit-Simulation; erst nach offizieller API-Erweiterung um Abfahrts-/Ankunftszeit.

## P2 — laufendes Hardening / Optionen

### B-DOC1-014 abfahrt.now Contract-Regressionstests
Reale API-Samples ohne Secrets bei künftigen Contract-Änderungen weiter ausbauen; unbekannte Forward-Compatible-Felder müssen parserseitig toleriert werden.

### B-DOC1-015 `Station.walkSeconds` bewerten
Build 134 beobachtete positive Werte. Nutzung höchstens separat als WALK-Fallback/Provisional-ETA evaluieren; Gehwegdistanz, Bike-Zeit, Geometrie und Mehrregionen-Evidence fehlen weiterhin.

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
