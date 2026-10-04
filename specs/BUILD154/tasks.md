# Build 154 Tasks

## Specification / Audit
- [x] Build-153-Evidence als Baseline prüfen.
- [x] `/doc`-Regeln für First Paint, Stable-Merge, Hard Reset, ORS und Standortwechsel erneut prüfen.
- [x] aktuellen `DepartureViewModel`-/FusedLocation-Pfad prüfen.
- [x] Projektverlauf zur ursprünglichen 200-m-Layer-/Hard-Reset-Entscheidung gegenprüfen.
- [x] feststellen: Re-Anchor-Schwelle ist bereits 200 m; keine separate normative `lastLocation`-Alters-/Accuracy-Schwelle vorhanden.
- [x] Build-154-Spec/Plan ohne neue Magic Numbers erstellen.

## Implementation — abgeschlossen
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

## Automated Verification — Android CI #101/#106 grün
- [x] Static/Governance/Compatibility gates grün.
- [x] committed Gradle wrapper gate grün.
- [x] Unit Tests grün.
- [x] Debug build grün.
- [x] Release/R8 build grün.
- [x] Pure-Policy-Test: vorhandene/fehlende `lastLocation`-Eligibility.
- [x] Pure-Policy-Test: fresh `< 200 m` => `KEEP_PROVISIONAL`.
- [x] Pure-Policy-Test: fresh `>= 200 m` inklusive exakt 200 m => `REANCHOR`.
- [x] Pure-Policy-Test: Target-Wechsel vor Fresh-Fix => `IGNORE_STALE_TARGET`.
- [x] reale Anzahl der Core-/ORS-Zyklen im beobachteten Same-Origin-Fall durch Runtime-Evidence geprüft; JVM-Policy-Tests simulieren bewusst keine FusedLocation-/Repository-Nebenläufigkeit.

## Runtime Evidence — accepted 04.10.2026
- [x] drei saubere Cold Starts mit vorhandener `lastLocation` erfasst.
- [x] belegt: der Core-Pfad wartet bei vorhandener `lastLocation` nicht mehr auf High Accuracy. `Loading`→erster Core-Request: ca. 29 ms / 30 ms / 25 ms gegenüber Build-153-Baseline 2,59–3,02 s.
- [x] Same-Origin-Korrekturen: 6 m / 0 m / 8 m; kein Re-Anchor und kein zusätzlicher Core-/ORS-Zyklus allein wegen der Korrektur.
- [x] Cold 1/3 zeigen frühen First Paint; Cold 2 isoliert eine 9,161-s-Core-HTTP-Latenz als externe Netzwerk-/Provider-Varianz und nicht als Location-Startup-Regression.
- [x] Nutzer bestätigt die drastisch verkürzte Ladezeit auch visuell und meldet keine störende Standort-/Refresh-Unruhe.
- [x] Re-Anchor `>= 200 m` war im Feld nicht praktisch reproduziert; Verhalten bleibt durch bestehende 200-m-Hard-Reset-Semantik plus Policy-Tests geschützt und wird nicht als real getestet behauptet.
- [x] keine neue beobachtete Navigation-/Permission-/AccessGate-Regression im geprüften Scope.

## Convergence
- [x] tatsächlichen Performancegewinn und Requestverhalten ausgewertet.
- [x] F-153-001 mit positiver Build-154-Evidence schließen.
- [x] Acceptance-Formulierung präzisieren: maßgeblich ist, dass der Core-Pfad **nicht auf** den High-Accuracy-Fix wartet; der Fix darf bei einem Rennen zufällig vor dem HTTP-Start eintreffen.
- [x] dauerhafte akzeptierte Semantik nach `/doc` konvergieren.
- [x] F-ORS-001/B-ORS-001 weiterhin separat halten.
