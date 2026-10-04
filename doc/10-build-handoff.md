# Build-Handoff

## Aktuell akzeptiert: v1.1.0 Build 153 (`versionCode 1530`) — Startup/Main-Thread-Instrumentierung accepted

Build 153 ist am 04.10.2026 nach vollständigem Android-CI-Gate und realer Stage-A-Evidence als Mess-/Diagnostik-Build akzeptiert. **Build 154 (`versionCode 1540`) ist implementiert und CI-grün, aber bis zur Realgeräte-Evidence noch nicht accepted.**

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

### Automatisierte Evidence Build 153

Android CI #94 ist auf dem letzten Build-153-Runtime-Finding-Head vollständig grün:

```text
./gradlew :app:testDebugUnitTest :app:assembleDebug :app:assembleRelease --no-daemon
```

Static/Governance/Compatibility, committed Wrapper, Unit Tests, Debug und Release/R8 sind erfolgreich. Frühere Build-153-Gates #91 und #93 bleiben historische Zwischen-Evidence.

### Realgeräte-Evidence Build 153 — 04.10.2026

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

Der Build-153-Current-Location-Pfad führte vor dem Netzwerk aus:

```text
resolveCurrentTargetCoordinates()
  -> getBestLocation()
     -> getCurrentLocation(PRIORITY_HIGH_ACCURACY)
     -> lastLocation nur bei null
```

Stage A plus realer Zeitstrahl grenzten den Befund ausreichend auf diese Location-Auflösung ein. Build 153 löste den Befund absichtlich nicht; F-153-001 wurde nach Build 154 getragen.

## Build 154 — Current-Location First-Paint Fast Path — implementiert / Realgeräte-Evidence offen

Branch: `feature/build154-location-startup`  
Draft-PR: #10  
Runtime: `versionCode = 1540`, `versionName = 1.1.0`.

Android CI #101 ist auf dem Runtime-Implementierungsstand vollständig grün: Static/Governance/Compatibility, committed Wrapper, Unit Tests, Debug und Release/R8.

### Implementierter Eingriff

Der erneute Audit von `/doc`, Code und Projektverlauf bestätigte zwei Grenzen:

1. **Die Re-Anchor-Schwelle ist bereits definiert.** Der 200-m-Vertrag ist jetzt in `CurrentLocationStartupPolicy.MOVEMENT_THRESHOLD_METERS` zentralisiert; `< 200 m` bleibt Same-Origin, `>= 200 m` nutzt den bestehenden Hard-Reset-/Pending-Refresh-Pfad.
2. Für `FusedLocationProviderClient.lastLocation` gibt es **keine** separate normative Zeit-/Accuracy-Freshness-Regel. Der Build-153-Code akzeptierte diese Quelle bereits ohne solche Prüfung als Fallback. 60-s-Request-Throttle und `refreshIntervalMinutes` betreffen Departure-Daten und werden nicht als Location-Freshness umgedeutet.

Der Build-154-Pfad lautet:

```text
leerer Current-Location-Kaltstart
  -> lastLocation vorhanden und != 0/0?
     -> ja: als provisorischen First-Paint-Origin bestehenden Core-Pfad starten
            + High-Accuracy-Fix parallel weiter anfordern
     -> nein: bisherigen High-Accuracy-first-Pfad beibehalten

High-Accuracy-Korrektur
  -> Abweichung < 200 m: KEEP_PROVISIONAL, kein Reload aus dieser Korrektur
  -> Abweichung >= 200 m: REANCHOR über vorhandenen Hard-Reset-/Pending-Refresh-Pfad
  -> Target inzwischen gewechselt: Korrektur verwerfen
```

Zusätzliche Schutzmaßnahmen:
- wenn ein Re-Anchor bereits feststeht, bevor der provisorische Core final wird, startet für den verworfenen Origin kein neues ORS-Enrichment;
- ein bereits laufendes provisorisches ORS-Enrichment wird beim Re-Anchor abgebrochen;
- Target-/Generation-Guards bleiben erhalten;
- kein persistenter Standortcache, keine neue Dependency und keine zweite Location-/Loading-Architektur;
- API-Dedup-Booster, Direct-stop/Add-ons, app-eigene Filter/Dedup/Sortierung, Stable-Merge und Hard-Reset-Darstellung bleiben fachlich unverändert.

`CurrentLocationStartupPolicyTest` schützt die reine Policy für vorhandene/fehlende provisorische Position, `< 200 m`, exakt/über 200 m und stale Target. Die reale Anzahl der Core-/ORS-Zyklen wird **nicht** durch eine künstliche JVM-Nebenläufigkeitssimulation behauptet, sondern ist Teil der nun ausstehenden Feld-Evidence.

### Jetzt erforderliche Realgeräte-Evidence

1. Mindestens drei saubere Cold Starts mit vorhandener `lastLocation`.
2. Im Log muss `⚡ provisional lastLocation available` **vor** dem ersten abfahrt.now-Core-Request erscheinen; der Core darf nicht mehr auf den Abschluss des High-Accuracy-Fixes warten.
3. `Loading`→Core gegen Build-153-Baseline ca. 2,59–3,02 s vergleichen.
4. Same-Origin-Fall `< 200 m`: genau ein Core-Zyklus, kein Extra-ORS nur wegen der Korrektur, ruhige progressive Liste.
5. Re-Anchor `>= 200 m` soweit praktisch reproduzierbar: höchstens provisorischer Core + ein Ersatz-Core, kein dritter Core und kein Cross-Origin-Stable-Merge.
6. Home→App-Resume regressionsfrei.
7. Keine App-FATAL-/ANR-/Navigation-/Permission-/AccessGate-Regression.

Build 154 bleibt bis dahin **pending evidence** und PR #10 bleibt Draft. Wenn der reale Gewinn gering ist oder der Fast Path störende falsche Standort-/Refresh-Sprünge erzeugt, wird die Änderung verworfen statt weiter verkompliziert.

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
