# Build 153 Plan

## Vorgehen

### Phase A — Diagnostik-Grundlage
- `versionCode` auf 1530 erhöhen.
- Kleine Utility `StartupTrace` unter bestehendem `util`-Package ergänzen.
- Monotone Zeitbasis und einheitliches `AbfahrtStartup`-Format zentralisieren.
- Activity-Sessions als erster Create im Prozess = `cold`, weitere Creates im selben Prozess = `warm` kennzeichnen.
- Android `Trace` nur für synchrone Abschnitte verwenden; suspendierende Arbeit ausschließlich über Zeitmarker messen.

### Phase B — Startup-Marker
- `AbfahrtApplication`: Application-Lifecycle + MapLibre-Initialisierung.
- `MainActivity`: Activity-Lifecycle, setContent/Compose commit/erster Frame.
- `AccessGateViewModel` + `AppNavHost`: echte Preferences-Emission und Freigabe der geschützten Navigation.

### Phase C — Location/Core-Fetch
- `DepartureViewModel`: Location-Ermittlung ohne Standortwerte im Diagnostiklog.
- tatsächlichen initialen Core-Fetch und progressive Responses markieren.
- CPU-/Main-Grenzen der Response-Verarbeitung separat messen, ohne bestehende Dispatchers zu verändern.

### Phase D — ORS-Verifikation
- vorhandenes `Dispatchers.Default`-Enrichment zeitlich erfassen.
- Overlay/Filter und UI-State-Apply getrennt messen.
- keine Dispatcher- oder Algorithmusänderung in dieser Runde.

### Phase E — Evidence
- GitHub Actions: Static/Governance, Wrapper, Unit, Debug, Release/R8.
- Realgerät: Cold/Warm/Resume-Smokes mit `AbfahrtStartup`.
- System-/bestehende Logcat-Jank-Signaturen zeitlich gegen Marker legen.
- Messwerte nach `/doc` konvergieren und erst dann eine Optimierungsentscheidung treffen.

## Designgrenzen
- Kein globales `Looper.setMessageLogging` und kein aggressives StrictMode: beides würde den Messgegenstand stärker verändern.
- Kein JankStats/Macrobenchmark/Benchmark-Plugin in Runde 1; Plattformmarker reichen zunächst, um die groben Phasen zu lokalisieren.
- Keine sensiblen Werte im Diagnostiklog.
- Kein Umbau des großen `DepartureViewModel`; Marker werden an vorhandene Grenzen gesetzt.

## Entscheidungsregel nach der Messung
Eine Optimierung wird nur vorgenommen, wenn wiederholte reale Messungen eine relevante Phase zeigen. Kandidaten können z. B. MapLibre-Initialisierung, Preference-Gate, Location-Ermittlung, Core-Response-Verarbeitung oder ein anderer tatsächlich sichtbarer Abschnitt sein. Ohne reproduzierbaren Befund bleibt der Code unverändert.
