# Build-Handoff

## Aktuell akzeptiert: v1.1.0 Build 153 (`versionCode 1530`) — Startup/Main-Thread-Instrumentierung accepted

Build 153 ist am 04.10.2026 nach vollständigem Android-CI-Gate und realer Stage-A-Evidence als Mess-/Diagnostik-Build akzeptiert. Build 152 bleibt die vorherige akzeptierte UI/UX-Baseline; REPO1 bleibt die abgeschlossene Repository-/Governance-Basis.

### Build-153-Scope

1. `versionCode = 1530`, `versionName = 1.1.0`; `minSdk 34`, `compileSdk 37`, `targetSdk 37` unverändert.
2. Dependency-freie Utility `StartupTrace` verwendet Prozess-Uptime, `AbfahrtStartup` und Android `Trace` für leichte Messpunkte.
3. `AbfahrtApplication` misst Application-Lifecycle und die unveränderte synchrone MapLibre-Initialisierung.
4. `MainActivity` misst Cold-/Warm-Activity-Create, Splash, `super.onCreate`, `setContent`, Compose-Commit, ersten Frame sowie Start/Resume/Stop.
5. `AccessGateViewModel` markiert Erstellung und erste echte Preference-Emission; `AppNavHost` Waiting/Ready und protected navigation composition.
6. `UpdateViewModel` misst den vorhandenen nicht-kritischen GitHub-Release-Check ohne Release-Inhalte oder Credentials zu loggen.
7. `StartupDiagnosticsObserver` beobachtet read-only `DepartureViewModel.uiState`: Idle, Loading, progressive Success, erster finaler Success und Error.
8. Der große `DepartureViewModel` wurde in Stage A nicht umgebaut. Location/Core/ORS wurden über vorhandene Logs korreliert.
9. Keine Produkt-, Routing-, Provider-, Filter-, Sortier-, Credential- oder UI-Semantik wurde durch Build 153 verändert.

### Automatisierte Evidence

Android CI #94 ist auf dem letzten Runtime-Finding-Head vollständig grün:

```text
./gradlew :app:testDebugUnitTest :app:assembleDebug :app:assembleRelease --no-daemon
```

Static/Governance/Compatibility, committed Wrapper, Unit Tests, Debug und Release/R8 sind erfolgreich. Frühere Build-153-Gates #91 und #93 bleiben historische Zwischen-Evidence.

### Realgeräte-Evidence 04.10.2026

Drei **saubere vollständig instrumentierte Cold Starts** bilden die Acceptance-Basis:

- Cold 1: erster Compose-Frame ca. 436 ms, AccessGate ready ca. 671 ms, Departure `Loading` ca. 733 ms, erster Core-Request ca. 3,45 s; `Loading`→Core ca. 2,72 s.
- Cold 2: erster Compose-Frame ca. 455 ms, AccessGate ready ca. 705 ms, Departure `Loading` ca. 753 ms, erster Core-Request ca. 3,34 s; `Loading`→Core ca. 2,59 s.
- Cold 3/final: Application/MapLibre ca. 27 ms, erster Compose-Frame ca. 0,52 s, AccessGate ready ca. 0,77 s, Departure `Loading` ca. 0,83 s, erster Core-Request ca. 3,85 s; `Loading`→Core ca. 3,02 s.

Der finale Lauf bestätigt zusätzlich:
- der erste Core-Hauptrequest antwortet HTTP 200 in 629 ms; spätere Add-on-Netzwerkphasen variieren stärker, liegen aber **nach** dem bereits identifizierten initialen Warteblock;
- finaler Core-State wird erreicht, danach startet ORS asynchron;
- beide ORS-Matrix-Batches antworten mit korrigiertem Key HTTP 200 und werden vollständig geparst;
- Same-Process-Home→App: Activity `onStop`, später `onStart`/`onResume` im selben Prozess ohne neues `onCreate`; Location-Updates starten wieder;
- ein separater Warm-Activity-Recreate war nicht reproduzierbar. Das Gate verlangte ihn nur „sofern reproduzierbar“; der tatsächliche Same-Process-Resume-Pfad ist real belegt.

Ein zusätzlicher früher Lauf im finalen Gesamtlog wurde durch Doze/Wake sowie frühen Activity-Stop/Resume verunreinigt. Dieser Lauf enthält Choreographer-Skips und wird bewusst **nicht** als Cold-Start-Performance-Benchmark verwendet. Im sauberen finalen Cold-Start-Segment wurden keine `Choreographer: Skipped`-Zeilen beobachtet.

Keine App-`FATAL EXCEPTION`, kein App-Prozess-Crash/`AndroidRuntime`, keine App-ANR- oder Navigation-Regression im Abnahmeumfang. `AndroidRuntime`-Zeilen des Gesamtlogs gehören zum Shell-`monkey`-Prozess und enden regulär.

### Technisches Ergebnis aus AB-018

Der dominante wiederholbare Cold-Start-Warteblock liegt **vor dem ersten abfahrt.now-Core-Netzwerkrequest**. Application/MapLibre, Compose, Preference-Gate und der erste Core-HTTP-Call sind dafür nicht dominant.

Im Current-Location-Pfad wird vor dem Netzwerk ausgeführt:

```text
resolveCurrentTargetCoordinates()
  -> getBestLocation()
     -> getCurrentLocation(PRIORITY_HIGH_ACCURACY)
     -> lastLocation nur bei null
```

Stage A plus realer Zeitstrahl grenzen den Befund ausreichend auf diese Location-Auflösung ein. Direkte Stage-B-Marker sind daher nicht erforderlich. Build 153 löst den Befund absichtlich **nicht**; F-153-001 bleibt offen.

## Nächster Build: 154 — Current-Location First-Paint Fast Path

B-154-001 ist in `specs/BUILD154/` spezifiziert; Runtime-Code ist noch nicht geändert.

Der erneute Audit von `/doc`, aktuellem Code und Projektverlauf ergibt zwei wichtige Korrekturen zur vorläufigen Build-153-Handoff-Formulierung:

1. **Die Re-Anchor-Schwelle ist bereits definiert.** `MOVEMENT_THRESHOLD_M = 200f`; `< 200 m` bleibt Same-Origin, `>= 200 m` nutzt den bestehenden Hard-Reset-/Pending-Refresh-Pfad. Eine zweite Schwelle ist nicht zulässig.
2. Für `FusedLocationProviderClient.lastLocation` gibt es **keine** separate normative Zeit-/Accuracy-Freshness-Regel. Der heutige Code akzeptiert diese Quelle bereits ohne solche Prüfung als Fallback, wartet davor aber auf High Accuracy. 60-s-Request-Throttle und `refreshIntervalMinutes` betreffen Departure-Daten und werden nicht als Location-Freshness umgedeutet.

Spezifizierter kleinstmöglicher Eingriff:

```text
leerer Current-Location-Kaltstart
  -> lastLocation vorhanden?
     -> ja: als provisorischen First-Paint-Origin Core sofort starten
            + High-Accuracy-Fix parallel weiter anfordern
     -> nein: bisherigen High-Accuracy-Pfad beibehalten

High-Accuracy-Korrektur
  -> Abweichung < 200 m: kein zweiter Core, kein Extra-ORS nur wegen der Korrektur
  -> Abweichung >= 200 m: vorhandener Hard-Reset-/Pending-Refresh-Pfad genau einmal
```

Der provisorische Origin wird bewusst **nicht** als „frisch/final“ deklariert. Dadurch ist keine neue Alters-/Accuracy-Magic-Number nötig. Die bestehende progressive Pipeline bleibt vollständig erhalten: API-Dedup-Booster nur auf leerem Kaltstart, Direct-stop/Add-ons, app-eigene Filter/Dedup/Sortierung, ORS asynchron, Same-Origin-Stable-Merge und Cross-Origin-Hard-Reset ohne Loading-Blackout.

Kein persistenter Standortcache, keine neue Dependency und keine neue Location-/State-Architektur. Acceptance verlangt reale Evidence, dass der erste Core bei vorhandener `lastLocation` vor Abschluss des High-Accuracy-Fixes startet und die Anzeige dabei nicht unruhiger wird. Vergleichsbasis bleibt Build 153 mit `Loading`→Core ca. 2,59–3,02 s. Falls kein klarer Gewinn entsteht oder sichtbare Falschstand-/Refresh-Sprünge auftreten, wird der Fast Path verworfen statt weiter zu verkomplizieren.

## Separates Finding: ORS-Key-Probe

F-ORS-001 / B-ORS-001 bleibt unabhängig: Ein falsch hinterlegter ORS-Key wurde real mit HTTP 403 beantwortet, aber gespeichert/akzeptiert. Künftig soll beim Hinterlegen/Ändern eine ORS-Key-Probe erfolgen; 401/403 dürfen den neuen Key nicht übernehmen. Netzwerkfehler/5xx/429 werden nicht als „Key ungültig“ fehlklassifiziert.

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
