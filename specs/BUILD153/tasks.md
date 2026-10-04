# Build 153 Tasks

## Specification / Plan
- [x] Build-152-Acceptance als Baseline prüfen.
- [x] Historischen AB-018-Kontext und aktuellen Startup-/ORS-Code prüfen.
- [x] Spec und Plan ohne Optimierungsannahme erstellen.

## Implementation
- [ ] `versionCode = 1530`.
- [ ] `StartupTrace` Utility mit monotonic timing, Activity-Session und Android Trace ergänzen.
- [ ] Application-/MapLibre-Marker ergänzen.
- [ ] MainActivity-/Compose-/Frame-Marker ergänzen.
- [ ] AccessGate Preference-/Ready-Marker ergänzen.
- [ ] Location-/Core-Fetch-Marker ergänzen.
- [ ] Core-Response Main/Default/UI-Phasen getrennt messen.
- [ ] ORS-Enrichment/Overlay/UI-Apply zeitlich messen.
- [ ] Keine sensiblen Werte in `AbfahrtStartup`.
- [ ] Keine Produkt-/Routing-/Provider-/UI-Semantik ändern.

## Automated Verification
- [ ] Static/Governance/Compatibility gates grün.
- [ ] committed Gradle wrapper gate grün.
- [ ] Unit tests grün.
- [ ] Debug build grün.
- [ ] Release/R8 build grün.

## Runtime Evidence
- [ ] Drei Cold Starts mit `AbfahrtStartup` erfassen.
- [ ] Warm-Relaunch innerhalb desselben Prozesses erfassen, sofern reproduzierbar.
- [ ] Home→App/Resume erfassen.
- [ ] Preference-Gate, Location, Core first/final response im Zeitstrahl vorhanden.
- [ ] ORS-Phasen zeitlich sichtbar, falls ORS im Testlauf aktiv ist.
- [ ] Keine App-FATAL-/ANR-/Navigation-Regression.

## Convergence
- [ ] Messwerte analysieren und echten Engpass benennen oder ausdrücklich festhalten, dass keiner belegt ist.
- [ ] Erst danach Optimierungsentscheidung treffen.
- [ ] `/doc` auf Build-153-Evidence/Status konvergieren.
