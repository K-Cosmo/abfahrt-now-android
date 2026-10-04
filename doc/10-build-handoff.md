# Build-Handoff

## Aktuell akzeptiert: v1.1.0 Build 152 (`versionCode 1520`) — UI/UX convergence accepted

Build 152 ist am 04.10.2026 nach vollständigem CI-Gate und Realgeräte-Smoke akzeptiert. Build 151 bleibt die vorherige akzeptierte Community-Identity-Baseline; REPO1 bleibt die abgeschlossene Repository-/Governance-Basis.

### Build-152-Scope

- `versionCode = 1520`, `versionName = 1.1.0`.
- `minSdk = 34`, `targetSdk = 37`; Android-/Toolchain-Baseline unverändert.
- Onboarding-Fließtext linksbündig; Follow-up verdichtet Außenabstände, Kartenpadding, Zeilenhöhe und Zwischenräume, ohne den Wortlaut unnötig zu verändern. Pflicht-Key und optionaler ORS-Hinweis bleiben getrennt und die Seite bleibt scrollbar.
- `AccessGateViewModel` ist alleiniger Owner der Startup-Access-Entscheidung. Bis zur ersten echten Preference-Repository/DataStore-Emission bleibt der Gate-State geschlossen; geschützte Feature-ViewModels entstehen erst danach.
- Der frühere API-Key-/Onboarding-Kaltstart-Flash ist real bestätigt beseitigt (F-152-001 closed).
- Ein im ersten Build-152-Smoke entdeckter kurzer `Standort erlauben`-Flash trotz bereits erteilter Berechtigung wurde kausal im `PermissionOrIdleContent`-Pfad behoben. Der Follow-up-Retest bestätigt, dass der Flicker nicht mehr erscheint und Location-Updates direkt starten (F-152-002 closed).
- RoutePlanner-Start/Ziel liegen in einer gemeinsamen kompakten Surface mit Trenner und kleiner Tauschaktion. Photon, Current location, Home/Work, Swap und `/trips` bleiben fachlich auf den bestehenden Pfaden.
- Settings-Footer ist als zusammenhängender Block verdichtet. Der missverständliche zusätzliche Identitätskasten `nicht mit abfahrt.now verbunden` wurde entfernt; abfahrt.now bleibt als Daten-/API-Quelle sichtbar. Der Schluss-Disclaimer wird satzweise zentriert dargestellt.
- Der historische, ungenutzte `AppFooter` inklusive alter Riles-Tech-/Legal-Hilfsfunktionen ist entfernt; `CommunityFooter` bleibt einzige Runtime-Footer-Implementierung.

### Automatisierte Evidence

Android CI #72 bestätigte den Follow-up-Code plus den damaligen Pending-Evidence-Dokumentationsstand. Der **finale Acceptance-/Dokumentations-Head** wurde anschließend mit Android CI #81 vollständig geprüft:

```text
./gradlew :app:testDebugUnitTest :app:assembleDebug :app:assembleRelease --no-daemon
```

Ergebnis: Static-/Governance-Gates, committed Wrapper, Unit Tests, Debug und Release/R8 vollständig grün. Für den akzeptierten Stand ist CI #81 maßgeblich.

Frühere Build-152-Gates #60, #62 und #64 bleiben historische Zwischen-Evidence.

### Realgeräte-Evidence 04.10.2026

- installierter Stand bestätigt: `versionCode=1520`, `versionName=1.1.0`, `minSdk=34`, `targetSdk=37`.
- Footer visuell akzeptiert.
- kompakter RoutePlanner zeigt keine beobachtete Regression; Nutzerfeedback: unverändert gegenüber dem bereits positiv bewerteten Stand.
- Onboarding-/Starttext bleibt inhaltlich unverändert; nur Layout-/Spacing-Änderung war vorgesehen.
- ursprünglicher API-Key-/Onboarding-Flicker weg.
- Location-Prompt-Flicker nach Follow-up weg.
- Logcat trennt Einrichtungs-/Permission-Flow und späteren eingerichteten Prozessstart. Beim eingerichteten Start läuft die Activity ohne erneuten Permission-Dialog hoch; Location-Updates starten direkt.
- Photon-Zielsuche (`s potsd`) liefert reale Treffer; `/trips` antwortet HTTP 200 und `AbfahrtTrips` meldet sieben Verbindungen.
- kein `FATAL EXCEPTION`, kein `AndroidRuntime` und keine App-ANR-Signatur im finalen Logcat.

F-152-001 und F-152-002 sind geschlossen. B-152-001 ist abgeschlossen.

## Build 153 — AB-018 Startup/Main-Thread-Instrumentierung — in progress

Draft-PR #9 / Branch `feature/build153-startup-instrumentation` enthält Instrumentierungsstufe A. Build 152 bleibt bis zur realen Build-153-Evidence die akzeptierte Runtime-Baseline.

### Implementierter Stand

1. `versionCode = 1530`, `versionName = 1.1.0`; `minSdk 34`, `compileSdk 37`, `targetSdk 37` unverändert.
2. Neue dependency-freie Utility `StartupTrace` verwendet `Process.getStartUptimeMillis()` und `SystemClock.uptimeMillis()` als monotone Zeitbasis, `AbfahrtStartup` als Logcat-Tag und Android `Trace` für kurze synchrone Sections.
3. `AbfahrtApplication` misst Application-Lifecycle und die bisher unveränderte synchrone `MapLibre.getInstance()`-Initialisierung.
4. `MainActivity` kennzeichnet den ersten Activity-Create im Prozess als `cold`, weitere Creates als `warm` und misst Splash-Install, `super.onCreate`, `setContent`, Compose-Commit, ersten Frame sowie Start/Resume/Stop.
5. `AccessGateViewModel` markiert seine Erstellung und die erste echte DataStore-/Repository-Preference-Emission. `AppNavHost` markiert Waiting/Ready und den Zeitpunkt, zu dem geschützte Navigation/Feature-ViewModels komponiert werden.
6. `UpdateViewModel` misst den ohnehin vorhandenen, nicht-kritischen GitHub-Release-Check, ohne Release-Inhalte oder Credentials zu loggen.
7. `StartupDiagnosticsObserver` beobachtet **read-only** das bestehende `DepartureViewModel.uiState`: Idle, Loading, progressive Success-Emissionen, erster finaler Success und Error. Geloggt werden nur Status/Zähler, keine Standort- oder Suchwerte.
8. Der 94-kB-`DepartureViewModel` bleibt in Instrumentierungsstufe A bewusst unverändert. Location-/Netzwerk-/ORS-Phasen werden zunächst über vorhandene `AbfahrtLocation`, OkHttp und `AbfahrtWalk` zeitlich mit `AbfahrtStartup` korreliert.

### Bewusst nicht implementiert

- keine Performance-Optimierung;
- keine MapLibre-Lazy-Initialisierung;
- keine neue Dependency, JankStats-/Benchmark-Library oder globale Looper-Instrumentierung;
- keine Dispatcher-/Coroutine-, DataStore-/Keystore-, Netzwerk- oder ORS-Änderung;
- keine UI-, Routing-, Provider-, Filter- oder Sortieränderung;
- keine direkten `DepartureViewModel`-Marker, solange Stufe A nicht belegt, dass sie benötigt werden.

### Noch erforderliche Evidence

1. finaler Branch-Head: Static/Governance, committed Wrapper, Unit Tests, Debug und Release/R8 grün;
2. eingerichtetes Realgerät, Standortberechtigung bereits erteilt: drei Cold Starts (`force-stop` → Start), ohne App-Daten zu löschen;
3. Warm-Relaunch innerhalb desselben Prozesses, sofern reproduzierbar; zusätzlich Home → App als Resume-Fall;
4. Logcat muss `AbfahrtStartup` zusammen mit `AbfahrtLocation`, OkHttp und `AbfahrtWalk` enthalten; Davey-/Skipped-Frame-Signaturen werden zeitlich dagegen gelegt;
5. keine neue App-FATAL-/ANR-/Navigation-Regression;
6. Messwerte analysieren. Erst danach Entscheidung über optionale Instrumentierungsstufe B (`getBestLocation`, Core-Response-Unterphasen, ORS-Unterphasen) bzw. konkrete Optimierung.

### Erwartete Marker der ersten Runde

```text
application_onCreate_enter
maplibre_init
activity_onCreate_enter
compose_root_committed
compose_first_frame
access_gate_vm_created
preferences_first_real_emission
access_gate_ready
protected_navigation_composed
departure_state_idle
departure_state_loading
departure_state_success
departure_first_final_success
```

Zusätzlich können `update_check_start/update_check_complete` sowie Activity-Resume-/Stop-Marker erscheinen.

## Lokaler Workspace

Android Studio öffnet den Repository-Root:

```text
D:\Android\abfahrt-now-android
```

Standard-Buildpfad lokal:

```text
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug :app:assembleRelease
```

`local.properties`, Build-Ausgaben, Keystores und rohe Runtime-Evidence bleiben lokal und werden nicht committed. Roh-Logcats enthalten potenziell Standort-/Geräteinformationen und werden nur bereinigt nach `/evidence/public/` übernommen.
