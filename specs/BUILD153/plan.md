# Build 153 Plan

## Vorgehen

### Phase A — Diagnostik-Grundlage
- `versionCode` auf 1530 erhöhen.
- Kleine Utility `StartupTrace` unter bestehendem `util`-Package ergänzen.
- Monotone Zeitbasis und einheitliches `AbfahrtStartup`-Format zentralisieren.
- Activity-Sessions als erster Create im Prozess = `cold`, weitere Creates im selben Prozess = `warm` kennzeichnen.
- Android `Trace` nur für kurze synchrone Abschnitte verwenden.

### Phase B — minimal-invasive Startup-Marker
- `AbfahrtApplication`: Application-Lifecycle + MapLibre-Initialisierung.
- `MainActivity`: Activity-Lifecycle, setContent/Compose commit/erster Frame.
- `AccessGateViewModel` + `AppNavHost`: echte Preferences-Emission und Freigabe der geschützten Navigation.
- `UpdateViewModel`: Start/Ende des nicht-kritischen GitHub-Release-Checks.
- neuer read-only `StartupDiagnosticsObserver`: vorhandene `DepartureViewModel.uiState`-Übergänge messen, ohne den großen ViewModel-Datenpfad zu verändern.

### Phase C — Korrelation statt Eingriff
- vorhandenes `AbfahrtLocation` zeitlich neben die Startup-Marker legen.
- OkHttp-Logs für tatsächliche Core-/GitHub-/Photon-/ORS-Netzwerkgrenzen verwenden.
- vorhandenes `AbfahrtWalk` für ORS-Start/Apply verwenden.
- Android-Davey-/Skipped-Frame-Signaturen gegen dieselbe Logcat-Zeitachse legen.

### Phase D — optionale Nachinstrumentierung
Nur falls die erste Runde keine belastbare Ursache liefert:
- direkte `getBestLocation()`-Dauer;
- Core-Response Main-/Default-/UI-Unterphasen;
- ORS-Enrichment-Unterphasen.

Diese Marker werden nicht vorsorglich in `DepartureViewModel` eingebaut.

### Phase E — Evidence
- GitHub Actions: Static/Governance, Wrapper, Unit, Debug, Release/R8.
- Realgerät: Cold/Warm/Resume-Smokes mit `AbfahrtStartup` plus vorhandenen Runtime-Tags.
- Messwerte nach `/doc` konvergieren und erst dann eine Optimierungsentscheidung treffen.

## Designgrenzen
- Kein globales `Looper.setMessageLogging` und kein aggressives StrictMode: beides würde den Messgegenstand stärker verändern.
- Kein JankStats/Macrobenchmark/Benchmark-Plugin in Runde 1; Plattformmarker reichen zunächst, um die groben Phasen zu lokalisieren.
- Keine sensiblen Werte im Diagnostiklog.
- Kein Umbau des großen `DepartureViewModel` ohne konkrete Evidence.

## Entscheidungsregel nach der Messung
Eine Optimierung wird nur vorgenommen, wenn wiederholte reale Messungen eine relevante Phase zeigen. Kandidaten können z. B. MapLibre-Initialisierung, Preference-Gate, Location-/Netzwerk-Wartezeit, Core-Response-Verarbeitung oder ein anderer tatsächlich sichtbarer Abschnitt sein. Ohne reproduzierbaren Befund bleibt der Code unverändert.
