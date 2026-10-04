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
- `android.os.Trace` für synchrone Trace-Sections;
- Logcat-Tag `AbfahrtStartup` für reproduzierbare Feld-Evidence.

Keine neue Dependency.

## Zu instrumentierende Grenzen
1. Prozess/Application
   - `Application.onCreate` enter/exit;
   - Dauer `MapLibre.getInstance()`.
2. Activity/Compose
   - Activity-Create als `cold` beim ersten Activity-Create des Prozesses, danach `warm`;
   - `onCreate` enter/after-super/exit;
   - Root-Compose committed;
   - erster Compose-Frame;
   - `onResume` als zusätzliche Hot-/Resume-Evidence.
3. Access Gate
   - Erstellung `AccessGateViewModel`;
   - erste reale `UserPreferencesRepository.preferencesFlow`-Emission;
   - Gate ready / geschützte Navigation komponiert.
4. Location
   - Start Location-Updates;
   - Start/Ende `getBestLocation()` ohne Koordinaten im Diagnostiklog;
   - Quelle nur als `current`, `last` oder `none`.
5. Core Departures
   - tatsächlicher Core-Fetch-Start;
   - erster progressiver Response;
   - finaler Response;
   - Dauer synchroner Response-Vorbereitung;
   - Wartezeit auf `Dispatchers.Default`-Filter;
   - Dauer des finalen UI-State-Apply.
6. ORS/Walking
   - Dauer des bereits ausgelagerten Enrichment-Blocks;
   - Dauer Overlay+Filter auf `Dispatchers.Default`;
   - Dauer des anschließenden UI-State-Apply.

## Logformat
Jeder Diagnostikeintrag enthält soweit sinnvoll:
- `event=<name>`
- `sinceProcessMs=<ms>`
- `durationMs=<ms>` bei Spans
- `session=<n>`
- `startKind=cold|warm`
- `thread=main|background`
- nur nicht-sensitive Metadaten wie Response-Anzahl/Final-Flag/Retry-Nummer.

Nicht loggen:
- API-Keys oder Ciphertext;
- exakte Koordinaten;
- Suchtexte, Saved Places oder andere nutzerbezogene Inhalte.

## Nicht-Ziele
- keine Lazy-Initialisierung von MapLibre in diesem Build;
- keine Änderung an Update-Checker-Startzeitpunkt;
- keine DataStore-/Keystore-Architekturänderung;
- keine Coroutine-/Dispatcher-Umbauten;
- keine ORS-/Core-Request-Optimierung;
- kein neues Jank-/Profiler-Framework;
- keine UI-Änderung.

## Acceptance für die Instrumentierungsrunde
1. Android CI vollständig grün einschließlich Unit, Debug und Release/R8.
2. Realgerät: mindestens drei Cold Starts (`force-stop` → Launcher/Start) mit gefiltertem `AbfahrtStartup`-Log.
3. Mindestens ein Warm-Relaunch innerhalb desselben Prozesses, sofern auf dem Testgerät reproduzierbar; zusätzlich Home→App als Resume-Evidence.
4. Marker zeigen eine konsistente zeitliche Kette von Process/Application bis Preference-Gate, Location und erstem/finalem Core-Response.
5. Keine App-FATAL-/ANR-/Navigation-Regression.
6. Erst nach Auswertung der Messwerte wird entschieden, ob und wo Build 153 einen zweiten, gezielten Optimierungsschritt erhält oder ob die Optimierung in einen Folgebuild wandert.
