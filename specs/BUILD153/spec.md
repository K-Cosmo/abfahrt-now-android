# Build 153 Specification — Startup/Main-Thread Instrumentation

## Ziel
Build 153 misst den realen Startup- und Main-Thread-Pfad auf Basis des akzeptierten Build 152. Der Build darf noch **keine Performance-Optimierung** vorwegnehmen.

## Runtime-Baseline
- `versionName = 1.1.0`
- `versionCode = 1530`
- `minSdk = 34`
- `targetSdk = 37`
- keine Änderung an Produkt-, Routing-, Provider-, Filter-, Sortier-, Credential- oder UI-Semantik

## Messprinzip
Eine kleine app-eigene Diagnostik verwendet ausschließlich Android-Plattformmittel:
- `android.os.Process.getStartUptimeMillis()` als monotone Prozess-Referenz;
- `SystemClock.uptimeMillis()` für Marker/Dauern;
- `android.os.Trace` für kurze synchrone Trace-Sections;
- Logcat-Tag `AbfahrtStartup` für reproduzierbare Feld-Evidence.

Keine neue Dependency.

## Instrumentierungsstufe A — minimal-invasiv
1. Prozess/Application
   - `Application.onCreate` enter/exit;
   - Dauer `MapLibre.getInstance()`.
2. Activity/Compose
   - Activity-Create als `cold` beim ersten Activity-Create des Prozesses, danach `warm`;
   - `onCreate`-Phasen;
   - Root-Compose committed;
   - erster Compose-Frame;
   - `onStart`, `onResume`, `onStop` als Resume-/Hot-Evidence.
3. Access Gate
   - Erstellung `AccessGateViewModel`;
   - erste reale `UserPreferencesRepository.preferencesFlow`-Emission;
   - Gate waiting/ready und geschützte Navigation komponiert.
4. Nicht-kritischer Update-Check
   - ViewModel-Erstellung;
   - Start und Ende des GitHub-Release-Checks, ohne Release-Inhalte zu loggen.
5. Departure-State-Kette
   - read-only Compose-Observer auf dem bestehenden `DepartureViewModel`;
   - `Idle`, `Loading`, progressive `Success`-Emissionen, erster finaler Success und Error;
   - Zeit von `Loading` bis Success;
   - nur Zähler/Final-Flag/Anzahl gefilterter Einträge/Walking-Metric-Anzahl.
6. Korrelation mit bestehenden Runtime-Tags
   - `AbfahrtLocation` für Start/Stop/Movement;
   - OkHttp-Logs für tatsächliche Core-/GitHub-/Photon-/ORS-Netzwerkgrenzen;
   - `AbfahrtWalk` für Start/Apply des bereits ausgelagerten ORS-Enrichments;
   - vorhandene Android-Davey-/Skipped-Frame-Signaturen.

## Instrumentierungsstufe B — nur falls A nicht ausreicht
Direkte Marker in `DepartureViewModel` für `getBestLocation()`, Core-Response-Verarbeitung oder ORS-Unterphasen werden **erst** ergänzt, wenn Stufe A den Engpass nicht ausreichend eingrenzt. Dadurch bleibt der kritische, große ViewModel-Pfad in der ersten Runde unverändert.

## Logformat
Jeder `AbfahrtStartup`-Eintrag enthält soweit sinnvoll:
- `event=<name>`
- `sinceProcessMs=<ms>`
- `durationMs=<ms>` bei Spans
- `session=<n>`
- `startKind=process|cold|warm`
- `thread=main|background`
- nur nicht-sensitive Metadaten.

Nicht loggen:
- API-Keys oder Ciphertext;
- exakte Koordinaten;
- Suchtexte, Saved Places oder andere nutzerbezogene Inhalte.

## Nicht-Ziele
- keine Lazy-Initialisierung von MapLibre in dieser Messrunde;
- keine Änderung am Update-Checker-Startzeitpunkt;
- keine DataStore-/Keystore-Architekturänderung;
- keine Coroutine-/Dispatcher-Umbauten;
- keine ORS-/Core-Request-Optimierung;
- kein neues Jank-/Profiler-Framework;
- keine UI-Änderung.

## Acceptance für die Instrumentierungsrunde
1. Android CI vollständig grün einschließlich Unit, Debug und Release/R8.
2. Realgerät: mindestens drei Cold Starts (`force-stop` → Start) mit `AbfahrtStartup` plus den korrelierenden bestehenden Tags.
3. Mindestens ein Warm-Relaunch innerhalb desselben Prozesses, sofern auf dem Testgerät reproduzierbar; zusätzlich Home→App als Resume-Evidence.
4. Marker zeigen eine konsistente zeitliche Kette von Process/Application über Preference-Gate bis `Loading` und erstem/finalem Departure-Success; Location-/Netzwerk-/ORS-Grenzen lassen sich über bestehende Tags zeitlich zuordnen.
5. Keine App-FATAL-/ANR-/Navigation-Regression.
6. Erst nach Auswertung der Messwerte wird entschieden, ob Instrumentierungsstufe B nötig ist und ob anschließend eine gezielte Optimierung gerechtfertigt ist.
