# Build 154 Tasks

## Specification / Audit
- [x] Build-153-Evidence als Baseline prüfen.
- [x] `/doc`-Regeln für First Paint, Stable-Merge, Hard Reset, ORS und Standortwechsel erneut prüfen.
- [x] aktuellen `DepartureViewModel`-/FusedLocation-Pfad prüfen.
- [x] Projektverlauf zur ursprünglichen 200-m-Layer-/Hard-Reset-Entscheidung gegenprüfen.
- [x] feststellen: Re-Anchor-Schwelle ist bereits 200 m; keine separate normative `lastLocation`-Alters-/Accuracy-Schwelle vorhanden.
- [x] Build-154-Spec/Plan ohne neue Magic Numbers erstellen.

## Implementation — abgeschlossen / Runtime-Evidence offen
- [x] `versionCode = 1540` setzen.
- [x] provisorischen `lastLocation`-Fast-Path nur für leeren Current-Location-Kaltstart implementieren.
- [x] High-Accuracy-Fix parallel beibehalten.
- [x] bei fehlender `lastLocation` bisherigen High-Accuracy-Pfad unverändert verwenden.
- [x] High-Accuracy-Korrektur gegen denselben zentralen 200-m-Threshold klassifizieren.
- [x] `< 200 m`: Policy behält denselben Core-Kontext bei; kein Reload wird aus der Korrektur geplant.
- [x] `>= 200 m`: bestehenden Hard-Reset-/Pending-Refresh-Pfad wiederverwenden.
- [x] Target-/Generation-Guard für verspätete Korrektur erhalten.
- [x] keinen neuen DataStore-Standortcache und keine neue Dependency einführen.
- [x] deduplizierten Cold-Start-Booster, Add-on-Pipeline, Filter/Dedup/Sortierung und Stable-Merge fachlich unverändert lassen.
- [x] ORS bei bereits erkanntem Re-Anchor überspringen bzw. laufendes provisorisches Enrichment abbrechen; vorhandene Stale-/Cancellation-Mechanismen weiterverwenden.
- [x] nur minimale nicht-sensitive Diagnosemarker für provisional/fresh/re-anchor ergänzen.

## Automated Verification — Android CI #101 grün
- [x] Static/Governance/Compatibility gates grün.
- [x] committed Gradle wrapper gate grün.
- [x] Unit Tests grün.
- [x] Debug build grün.
- [x] Release/R8 build grün.
- [x] Pure-Policy-Test: vorhandene/fehlende `lastLocation`-Eligibility.
- [x] Pure-Policy-Test: fresh `< 200 m` => `KEEP_PROVISIONAL`.
- [x] Pure-Policy-Test: fresh `>= 200 m` inklusive exakt 200 m => `REANCHOR`.
- [x] Pure-Policy-Test: Target-Wechsel vor Fresh-Fix => `IGNORE_STALE_TARGET`.
- [ ] reale Anzahl der Core-/ORS-Zyklen bleibt bewusst Runtime-Evidence; JVM-Policy-Tests simulieren keine FusedLocation-/Repository-Nebenläufigkeit.

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
- [x] F-ORS-001/B-ORS-001 weiterhin separat halten.
