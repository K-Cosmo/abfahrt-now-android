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

## Nächster Runtime-Build: Build 153 — AB-018 Startup/Main-Thread-Instrumentierung

Build 153 beginnt **nicht** mit Optimierung, sondern mit Messung. Baseline ist der akzeptierte Build 152.

Ziel der ersten Runde:
1. Cold/Warm-Start getrennt erfassen.
2. Process-/Activity-/Compose-/Preference-Gate-/Location-/initiale-Departure-Fetch-Zeitpunkte instrumentieren.
3. Main-Thread-Arbeit lokalisieren.
4. erst danach gezielt verschieben/lazy initialisieren, sofern reale Messdaten das begründen.
5. keine parallele UI-/Routing-/Provider-Semantikänderung.

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
