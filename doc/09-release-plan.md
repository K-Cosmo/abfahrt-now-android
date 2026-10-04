# Release-Plan

Dieser Plan beschreibt die **aktuelle Reihenfolge**. Historische Build-Details liegen in [`CHANGELOG.md`](CHANGELOG.md), Regressionen in [`05-regression-ledger.md`](05-regression-ledger.md) und Findings in [`07-findings.md`](07-findings.md).

## REPO1 — abgeschlossen

Das öffentliche Repository ist der kanonische Entwicklungsworkspace. `/doc` ist die einzige normative Dokumentationswurzel, Community-/EU-first-Policy ist dokumentiert, und der vollständige Gradle-9.6.0-Wrapper wird lokal sowie in GitHub Actions direkt und erfolgreich verwendet.

## Build 150 — abgeschlossen

Der GitHub-Release-Update-Checker ist technisch accepted und in `main` integriert: credential-isolierter anonymer Client, 22 Locale-Sets, realer `releases/latest`-E2E und erfolgreicher Release-/R8-Build.

## Build 151 — abgeschlossen

Die Runtime-UI zeigt die unabhängige/unoffizielle Community-Identität. abfahrt.now bleibt als Daten-/API-Quelle sichtbar. Android CI #42 ist inklusive Locale-/Static-/Governance-Gates, Unit-Tests, Debug- und Release/R8-Build grün.

Der Android-CI-Gate baut ab Build 151 dauerhaft sowohl Debug als auch Release, damit der Release-/R8-Nachweis nicht mehr manuell nachgeführt werden muss.

## DOC2 — abgeschlossen

DOC2 konvergierte nach REPO1/Build 150/Build 151 ausschließlich die normative Dokumentation. Es war kein Runtime-Build und änderte weder App-Source noch Versionierung.

## Build 152 — UI/UX-Konvergenz — accepted 04.10.2026

Build 152 ist abgenommen: Startup-Access-Gate ohne API-Key-/Onboarding-Flicker, Permission-Idle-Flicker behoben, kompakter RoutePlanner-Kopf und verdichteter Settings-/Community-Footer. Android CI #81 sowie Realgeräte-Evidence sind grün. F-152-001 und F-152-002 sind geschlossen.

## Build 153 — Startup/Main-Thread-Instrumentierung (AB-018) — accepted 04.10.2026

Build 153 (`versionCode = 1530`, `versionName = 1.1.0`) ist als **Mess-/Diagnostik-Build** accepted. SDK-/Toolchain-Baseline und Produktsemantik bleiben unverändert.

Instrumentierungsstufe A:
- zentrale dependency-freie `StartupTrace`-Zeitbasis über Prozess-Uptime;
- Application-/MapLibre-Initialisierung;
- Activity-Create/Start/Resume/Stop, Compose-Commit und erster Frame;
- erste echte Preference-Emission und Access-Gate-Freigabe;
- nicht-kritischer GitHub-Update-Check;
- read-only Beobachtung der bestehenden Departure-State-Kette (`Idle` → `Loading` → progressive/finale `Success` oder `Error`);
- bestehende `AbfahrtLocation`-, OkHttp- und `AbfahrtWalk`-Logs zur zeitlichen Korrelation.

Acceptance-Evidence:
1. Android CI #94 auf dem letzten Runtime-Finding-Head vollständig grün: Static/Governance/Compatibility, committed Wrapper, Unit Tests, Debug und Release/R8.
2. Drei **saubere vollständig instrumentierte Cold Starts** auf eingerichtetem Realgerät. Die zwei früheren sauberen Läufe zeigen `Loading`→erster Core-Request ca. 2,72/2,59 s; der finale saubere Lauf ca. 3,02 s.
3. Finaler sauberer Lauf: Application/MapLibre ca. 27 ms; erster Compose-Frame ca. 0,52 s; AccessGate ready ca. 0,77 s; Departure `Loading` ca. 0,83 s; erster Core-Request erst ca. 3,85 s nach Prozessstart. Die erste Core-Hauptantwort brauchte 629 ms. Damit liegt der dominante initiale Block erneut **vor** dem Core-Netzwerk.
4. Der vorhandene Current-Location-Code führt vor dem Netzwerk `resolveCurrentTargetCoordinates()` → `getBestLocation()` aus. `getBestLocation()` fordert zuerst `PRIORITY_HIGH_ACCURACY` über `getCurrentLocation()` an und nutzt `lastLocation` nur bei `null`. Stage A plus Codepfad grenzen den wiederholbaren Block ausreichend auf die Location-Auflösung ein; Stage B ist nicht erforderlich.
5. ORS startet erst nach finalem Core-State asynchron. Mit korrigiertem Key antworten beide Matrix-Batches HTTP 200 und werden vollständig geparst.
6. Same-Process-Home→App-Resume ist erfasst: `onStop`, später `onStart`/`onResume` im selben Prozess ohne neues `onCreate`. Ein separater Warm-Activity-Recreate war nicht reproduzierbar und war gemäß Gate nur „sofern reproduzierbar“ erforderlich.
7. Ein zusätzlicher Lauf im Gesamtlog wurde durch Doze/Wake und frühen Activity-Stop/Resume verunreinigt und zeigt Choreographer-Skips. Er wird bewusst **nicht** als Cold-Start-Benchmark gewertet. Im sauberen finalen Cold-Start-Segment gibt es keine `Choreographer: Skipped`-Zeilen.
8. Keine App-`FATAL EXCEPTION`-, App-Prozess-`AndroidRuntime`-, ANR- oder Navigation-Regression im Abnahmeumfang. Die `AndroidRuntime`-Treffer im Gesamtlog stammen vom Shell-`monkey`-Prozess und enden regulär.

Ergebnis: AB-018 ist hinsichtlich **Messung/Lokalisierung** für Build 153 erfüllt. Der Performance-Befund selbst bleibt als F-153-001 offen und wird nicht durch den Messbuild kaschiert.

## Build 154 — Current-Location First-Paint Fast Path — spezifiziert / Implementierung offen

Build 154 behandelt ausschließlich F-153-001. `specs/BUILD154/` enthält Specification, Plan und Tasks. Die erneute Prüfung des Projektverlaufs korrigiert dabei die vorläufige Build-153-Planung: Die Re-Anchor-Schwelle muss **nicht neu festgelegt** werden; sie existiert bereits als 200-m-Movement-Regel.

Verbindliche bestehende Grenzen für die Umsetzung:
- `MOVEMENT_THRESHOLD_M = 200f`: `< 200 m` Same-Origin, `>= 200 m` Hard Reset;
- bei laufendem Load wird ein relevanter Standortwechsel über den vorhandenen Pending-Refresh-Pfad nachgezogen, nicht parallel gestartet;
- der deduplizierte API-Response bleibt ausschließlich First-Paint-Booster für den leeren Kaltstartscreen;
- Direct-stop/Add-ons, app-eigene Filter/Dedup/Sortierung, ORS-asynchron und Same-Origin-Stable-Merge bleiben unverändert;
- Cross-Origin-Hard-Reset hält alte sichtbare Daten bis zum Ersatzresultat, übernimmt sie danach aber nicht per Stable-Merge in den neuen Standortkontext.

Audit-Ergebnis zu `lastLocation`:
- Es existiert **keine** separate normative Alters- oder Accuracy-Schwelle für den System-`lastLocation`-Cache.
- Der heutige Code akzeptiert `lastLocation` bereits ungeprüft als Fallback, aber erst nachdem `getCurrentLocation(PRIORITY_HIGH_ACCURACY)` keinen Wert geliefert hat.
- Build 154 führt deshalb keine neue 30-s/60-s/5-min- oder Meter-Accuracy-Regel ein und vermischt die vorhandene API-Daten-Freshness (`refreshIntervalMinutes`/60-s-Snapshot-Throttle) nicht mit Location-Freshness.

Spezifizierter Fast Path:
1. Bei leerem Current-Location-Kaltstart darf eine vorhandene `lastLocation` als **provisorischer First-Paint-Origin** den ersten Core-Request starten.
2. Der frische High-Accuracy-Fix läuft parallel weiter.
3. Fresh-Fix-Abweichung `< 200 m`: kein zweiter Core-Request, kein zusätzlicher ORS-Zyklus allein wegen dieser Korrektur.
4. Fresh-Fix-Abweichung `>= 200 m`: genau der bestehende Hard-Reset-/Pending-Refresh-Pfad; maximal ein Ersatz-Core-Zyklus.
5. Fehlt `lastLocation`, bleibt der bisherige High-Accuracy-Startpfad unverändert.
6. Kein persistenter Standortcache, keine neue Dependency und keine zweite Location-/Loading-Architektur.

Acceptance ist bewusst kausal statt mit neuer Performance-Magic-Number formuliert: Bei vorhandener `lastLocation` muss der erste Core-Request real **vor Abschluss des High-Accuracy-Fixes** beginnen. Die tatsächliche Verbesserung wird gegen die Build-153-Baseline `Loading`→Core ca. 2,59–3,02 s gemessen. Erzeugt der Fast Path sichtbare Standort-/Refresh-Unruhe oder keinen klaren realen Gewinn, wird er verworfen statt weiter verkompliziert.

## Separater Hardening-Block — ORS-Key-Probe

F-ORS-001/B-ORS-001 bleibt unabhängig von Build 154: Beim Hinterlegen/Ändern ORS-Key probeweise validieren; HTTP 401/403 darf den neuen Key nicht persistieren bzw. einen vorhandenen gültigen Key nicht überschreiben. Netzwerkfehler/5xx/429 dürfen nicht als ungültiger Key fehlklassifiziert werden.

## Release-Grundsatz

Kein Build gilt aufgrund von Versionsnummer, Gradle-Sync, erfolgreichem CI allein oder KI-Einschätzung als akzeptiert. Maßgeblich sind die in [`11-test-and-evidence.md`](11-test-and-evidence.md) definierten realen Gates und Evidence.
