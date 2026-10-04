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
- [ ] direkte `getBestLocation()`-Dauer ergänzen, falls Location-Phase unklar bleibt.
- [ ] Core-Response Main/Default/UI-Unterphasen ergänzen, falls Response-Verarbeitung als Verdächtiger übrig bleibt.
- [ ] ORS-Unterphasen ergänzen, falls vorhandene `AbfahrtWalk`-Zeitachse nicht ausreicht.

## Automated Verification
- [ ] Static/Governance/Compatibility gates grün.
- [ ] committed Gradle wrapper gate grün.
- [ ] Unit tests grün.
- [ ] Debug build grün.
- [ ] Release/R8 build grün.

## Runtime Evidence
- [ ] Drei Cold Starts mit `AbfahrtStartup` plus `AbfahrtLocation`/OkHttp/`AbfahrtWalk` erfassen.
- [ ] Warm-Relaunch innerhalb desselben Prozesses erfassen, sofern reproduzierbar.
- [ ] Home→App/Resume erfassen.
- [ ] Preference-Gate und Departure Idle→Loading→Success/final im Zeitstrahl vorhanden.
- [ ] Location-/Core-/ORS-Grenzen über bestehende Tags zeitlich zuordenbar.
- [ ] Keine App-FATAL-/ANR-/Navigation-Regression.

## Convergence
- [ ] Messwerte analysieren und echten Engpass benennen oder ausdrücklich festhalten, dass keiner belegt ist.
- [ ] Entscheiden, ob Instrumentierungsstufe B nötig ist.
- [ ] Erst danach Optimierungsentscheidung treffen.
- [ ] `/doc` auf Build-153-Evidence/Status konvergieren.
