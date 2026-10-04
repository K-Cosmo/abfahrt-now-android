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
- [x] Entscheidung nach Stage-A-Evidence: direkte `getBestLocation()`-Dauer derzeit nicht nötig; Zeitachse + vorhandener Codepfad grenzen den wiederholbaren Gap bereits auf Current-Location-Auflösung ein.
- [x] Core-Response-Unterphasen derzeit nicht nötig; Core-Hauptantwort liegt in den vollständigen Cold Starts nur bei ca. 162–174 ms.
- [x] ORS-Unterphasen derzeit nicht nötig; vorhandene `AbfahrtWalk`-/OkHttp-Zeitachse zeigt asynchronen Start nach finalem Core-State und HTTP 200.

## Automated Verification
- [x] Static/Governance/Compatibility gates grün — Android CI #91.
- [x] committed Gradle wrapper gate grün — Android CI #91.
- [x] Unit tests grün — Android CI #91.
- [x] Debug build grün — Android CI #91.
- [x] Release/R8 build grün — Android CI #91.

## Runtime Evidence
- [ ] Drei **vollständig instrumentierte** Cold Starts mit `AbfahrtStartup` plus `AbfahrtLocation`/OkHttp/`AbfahrtWalk` erfassen — aktuell zwei vollständig + ein teilweise erfasster Cold Start.
- [ ] Warm-Relaunch innerhalb desselben Prozesses erfassen, sofern reproduzierbar.
- [ ] Home→App/Resume erfassen.
- [x] Preference-Gate und Departure Idle→Loading→Success/final im Zeitstrahl vorhanden.
- [x] Location-/Core-/ORS-Grenzen über bestehende Tags zeitlich zuordenbar; dominanter Gap liegt vor erstem Core-Request im Current-Location-Auflösungspfad.
- [x] Im aktuellen Realgeräte-Log keine App-`FATAL EXCEPTION`-/`AndroidRuntime`-/ANR-/Davey-Signatur; `Skipped`-Treffer gehören nicht zum App-Prozess.
- [x] Korrigierter ORS-Key real bestätigt: Matrix HTTP 200 und Walking-Enrichment erfolgreich angewendet.

## Convergence
- [x] Stage-A-Messwerte analysiert: wiederholbarer Cold-Start-Warteblock ca. 2,6–2,7 s zwischen `Loading` und erstem Core-Request; vorhandener Codepfad weist auf `resolveCurrentTargetCoordinates()` → `getBestLocation()`.
- [x] Instrumentierungsstufe B vorerst nicht nötig; keine zusätzlichen Marker ohne neuen Erkenntnisbedarf.
- [ ] Optimierungsentscheidung erst nach vollständigem drittem Cold Start + Warm/Resume-Gate treffen.
- [x] ORS-403-Fehlkonfiguration als separates F-ORS-001/B-ORS-001 erfasst; nicht mit Build 153 vermischen.
- [x] `/doc` auf aktuellen Build-153-Interim-Evidence-Status konvergiert.
