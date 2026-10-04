# Build 153 Tasks

## Specification / Plan
- [x] Build-152-Acceptance als Baseline prüfen.
- [x] Historischen AB-018-Kontext und aktuellen Startup-/ORS-Code prüfen.
- [x] Spec und Plan ohne Optimierungsannahme erstellen.
- [x] Erste Runde auf minimal-invasive Stufe A begrenzen; tiefe ViewModel-Marker nur bei Bedarf.

## Implementation — Instrumentierungsstufe A
- [x] `versionCode = 1530`.
- [x] `StartupTrace` Utility mit monotonic timing, Activity-Session und Android Trace ergänzen.
- [x] Application-/MapLibre-Marker ergänzen.
- [x] MainActivity-/Compose-/Frame-/Resume-Marker ergänzen.
- [x] AccessGate Preference-/Ready-Marker ergänzen.
- [x] nicht-kritischen Update-Check zeitlich erfassen.
- [x] read-only Departure-State-Observer für Idle/Loading/progressive Success/final Success/Error ergänzen.
- [x] Keine sensiblen Werte in `AbfahrtStartup`.
- [x] Keine Produkt-/Routing-/Provider-/UI-Semantik ändern.
- [x] `DepartureViewModel` in Stufe A bewusst unverändert lassen.

## Optionale Instrumentierungsstufe B — nur nach Evidence
- [x] Entscheidung nach Stage-A-Evidence: direkte `getBestLocation()`-Dauer nicht nötig; drei saubere Cold-Start-Zeitachsen plus vorhandener Codepfad grenzen den wiederholbaren Gap ausreichend auf Current-Location-Auflösung ein.
- [x] Core-Response-Unterphasen nicht nötig; der initiale Warteblock liegt vor dem ersten Core-Request. Netzwerkantwortzeiten variieren nachgelagert, ändern diese Lokalisierung aber nicht.
- [x] ORS-Unterphasen nicht nötig; vorhandene `AbfahrtWalk`-/OkHttp-Zeitachse zeigt asynchronen Start nach finalem Core-State und HTTP 200.

## Automated Verification
- [x] Static/Governance/Compatibility gates grün — final Android CI #94.
- [x] committed Gradle wrapper gate grün — final Android CI #94.
- [x] Unit tests grün — final Android CI #94.
- [x] Debug build grün — final Android CI #94.
- [x] Release/R8 build grün — final Android CI #94.

## Runtime Evidence
- [x] Drei **saubere vollständig instrumentierte** Cold Starts mit `AbfahrtStartup` plus `AbfahrtLocation`/OkHttp/`AbfahrtWalk` erfasst.
- [x] Separater Warm-Activity-Recreate war nicht reproduzierbar; das Gate verlangte ihn nur „sofern reproduzierbar“. Der reale Same-Process-Rückkehrpfad ist über Home→App/Resume abgedeckt.
- [x] Home→App/Resume im selben Prozess erfasst: `onStop` → später `onStart`/`onResume` ohne neues `onCreate`.
- [x] Preference-Gate und Departure Idle→Loading→Success/final im Zeitstrahl vorhanden.
- [x] Location-/Core-/ORS-Grenzen zeitlich zuordenbar; dominanter Gap liegt vor erstem Core-Request im Current-Location-Auflösungspfad.
- [x] Finaler sauberer Cold Start: `Loading`→erster Core-Request ca. 3,02 s; bestätigt die zwei vorherigen sauberen Läufe mit ca. 2,72/2,59 s.
- [x] Korrigierter ORS-Key real bestätigt: beide Matrix-Batches HTTP 200 und Walking-Enrichment erfolgreich angewendet.
- [x] Keine App-`FATAL EXCEPTION`-/App-Prozess-Crash-/ANR-/Navigation-Regression im Abnahmeumfang.
- [x] Verunreinigten Doze/Wake-Lauf mit Choreographer-Skips als Nicht-Benchmark klassifiziert; sauberes finales Cold-Start-Segment ohne `Choreographer: Skipped`-Zeile.

## Convergence
- [x] Stage-A-Messwerte analysiert: wiederholbarer Cold-Start-Warteblock ca. 2,6–3,0 s zwischen `Loading` und erstem Core-Request; vorhandener Codepfad weist auf `resolveCurrentTargetCoordinates()` → `getBestLocation()`.
- [x] Instrumentierungsstufe B nicht nötig; keine zusätzlichen Marker ohne neuen Erkenntnisbedarf.
- [x] Optimierungsentscheidung getroffen: Build 153 bleibt reiner Mess-/Diagnostik-Build; F-153-001 wird isoliert in Build 154 optimiert.
- [x] ORS-403-Fehlkonfiguration als separates F-ORS-001/B-ORS-001 erfasst; nicht mit Build 153/154 vermischen.
- [x] `/doc` auf Build-153-Acceptance und Build-154-Nächste-Schritte konvergiert.
- [x] Build 153 zur Abnahme freigegeben; nach finalem CI der Acceptance-Konvergenz PR #9 aus Draft nehmen und mergen.
