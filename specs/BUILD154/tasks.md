# Build 154 Tasks

## Specification / Audit
- [x] Build-153-Evidence als Baseline prüfen.
- [x] `/doc`-Regeln für First Paint, Stable-Merge, Hard Reset, ORS und Standortwechsel erneut prüfen.
- [x] aktuellen `DepartureViewModel`-/FusedLocation-Pfad prüfen.
- [x] Projektverlauf zur ursprünglichen 200-m-Layer-/Hard-Reset-Entscheidung gegenprüfen.
- [x] feststellen: Re-Anchor-Schwelle ist bereits 200 m; keine separate normative `lastLocation`-Alters-/Accuracy-Schwelle vorhanden.
- [x] Build-154-Spec/Plan ohne neue Magic Numbers erstellen.

## Implementation — noch offen
- [ ] `versionCode = 1540` setzen.
- [ ] provisorischen `lastLocation`-Fast-Path nur für leeren Current-Location-Kaltstart implementieren.
- [ ] High-Accuracy-Fix parallel beibehalten.
- [ ] bei fehlender `lastLocation` bisherigen High-Accuracy-Pfad unverändert verwenden.
- [ ] High-Accuracy-Korrektur gegen bestehende `MOVEMENT_THRESHOLD_M = 200f` klassifizieren.
- [ ] `< 200 m`: keinen zweiten Core-Request erzeugen.
- [ ] `>= 200 m`: bestehenden Hard-Reset-/Pending-Refresh-Pfad genau einmal verwenden.
- [ ] Target-/Generation-Guard für verspätete Korrektur erhalten.
- [ ] keinen neuen DataStore-Standortcache und keine neue Dependency einführen.
- [ ] deduplizierten Cold-Start-Booster, Add-on-Pipeline, Filter/Dedup/Sortierung und Stable-Merge fachlich unverändert lassen.
- [ ] ORS für bereits invalidierten provisorischen Origin nicht neu starten; vorhandene Cancellation-/Stale-Guards wiederverwenden.
- [ ] nur minimale nicht-sensitive Diagnosemarker für provisional/fresh/re-anchor ergänzen.

## Automated Verification
- [ ] Static/Governance/Compatibility gates grün.
- [ ] committed Gradle wrapper gate grün.
- [ ] Unit Tests grün.
- [ ] Debug build grün.
- [ ] Release/R8 build grün.
- [ ] Policy-Test: provisional + fresh `< 200 m` => kein zweiter Core.
- [ ] Policy-Test: provisional + fresh `>= 200 m` => genau ein Hard-Reset-Re-Anchor.
- [ ] Policy-Test: keine `lastLocation` => bisheriger Fresh-Location-Pfad.
- [ ] Policy-Test: Target-Wechsel vor Fresh-Fix => verspätete Korrektur verworfen.

## Runtime Evidence
- [ ] mindestens drei saubere Cold Starts mit vorhandener `lastLocation` erfassen.
- [ ] belegen, dass der erste Core-Request nicht mehr auf High Accuracy wartet.
- [ ] realen `Loading`→Core-Gewinn gegen Build-153-Baseline 2,59–3,02 s dokumentieren.
- [ ] Same-Origin-Korrektur `< 200 m`: genau ein Core-Zyklus, kein Extra-ORS-Zyklus, ruhige progressive Anzeige.
- [ ] Re-Anchor `>= 200 m` soweit praktisch reproduzierbar: maximal provisorischer + ein Ersatz-Core; kein Cross-Origin-Stable-Merge.
- [ ] Home→App-Resume regressionsfrei.
- [ ] keine neue App-FATAL-/ANR-/Navigation-/Permission-/AccessGate-Regression.

## Convergence
- [ ] tatsächlichen Performancegewinn und Requestanzahl auswerten.
- [ ] bei positiver Evidence F-153-001 schließen bzw. auf Restbefund präzisieren.
- [ ] bei negativer Evidence Fast Path verwerfen; keine weitere Komplexität nur für theoretischen Gewinn.
- [ ] dauerhafte akzeptierte Semantik nach `/doc` konvergieren.
- [ ] F-ORS-001/B-ORS-001 weiterhin separat halten.
