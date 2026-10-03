## v1.1.0 Build 149 — HERE detail location map

- `versionCode = 1490`.
- AB-053: HERE-Detailsheet lädt keine ORS-Routenvorschau mehr und zeichnet keine blaue Route/Polyline.
- bei verfügbaren Koordinaten zeigt HERE stattdessen eine MapLibre-Standortkarte mit Query-Origin und Haltestellenmarker; unabhängig vom ORS-Key.
- neue Entscheidung D-070 / Invariante I-053; Nicht-HERE-RoutePreview bleibt unverändert.
- `detail_here_map_title` in allen 22 Locale-Sets; fokussierter JVM-Policy-Test.
- Build 148 nach realem Gradle-/Runtime-Evidence als accepted konvergiert.
- AB-018 Startup/Main-Thread-Performance bewusst nicht verändert; folgt separat nach Messung.

## v1.1.0 Build 148 — source-overlay compile correction

- `versionCode = 1480` unchanged; no product/runtime behavior change.
- First real combined Gradle gate failed only because `GeocodingLocalityScopeTest.kt` from Build 147 remained in an overlaid Android Studio workspace and referenced removed `scopePlaceQueryToCity()`.
- The corrected source intentionally keeps that legacy test path and rewrites it to the D-069 raw-query policy, making the archive overlay-safe for this transition.
- Runtime Photon acceptance was pending at correction time and was later accepted from the real Build-148-fixed Gradle/logcat evidence.

## v1.1.0 Build 148 — Raw Photon query / location-bias ranking

- `versionCode = 1480`.
- real Build-147 evidence: Gradle gate green; `Potsdam`/`Hauptbahnhof` no longer receive city appending, but `Bad Saarow` still becomes `Bad Saarow, Berlin`.
- supersedes D-068 with D-069: the general Photon place/route search sends the trimmed user query without semantic rewriting.
- retains existing `lat`/`lon` location bias.
- removes the additional client-side text-score re-ranking from general `searchPlaces()`; Photon feature order is preserved while exact display duplicates are removed.
- adds `PhotonPlaceRank` debug evidence for query/bias and the first five raw candidates (name/city/state/OSM type).
- separate station-only alternate-departures search remains unchanged.
- no dependency, API, `/trips`, ORS, MapLibre, DataStore, route-sort, walking-navigation or localization change.

## v1.1.0 Build 147 — Photon locality-scope correction / doc convergence

- `versionCode = 1470`.
- fixes F-146-004: plain one-token remote places such as `Potsdam` are no longer rewritten to `Potsdam, Berlin`.
- explicit transit-location qualifiers (`S`, `U`, `S+U`, `U+S`, `Bf`, `Bhf`, `Hbf`, `Bahnhof`) suppress current-city appending, so `S Potsdam` and `Potsdam Hbf` remain global searches.
- multi-word unqualified targets such as `Brandenburger Tor` still receive the current-city default; `Hauptbahnhof` remains global as a one-token query; Photon lat/lon bias still favors nearby matches.
- adds focused JVM tests for the new scope boundaries.
- converges stale `/doc` acceptance markers for Builds 138/144/145 and updates Android-compatibility heading to the current build.
- no dependency, API, `/trips`, ORS, MapLibre, DataStore, route-sort or localization change.

## v1.1.0 Build 146 — Routing UX alignment / navigation / locality scope

- `versionCode = 1460`.
- route-leg departure/arrival times are centered in the same fixed leading column as mode icon + line badge; stop/detail content is aligned on a consistent right-hand axis.
- first and final walking legs get a top-right navigation action using an implicit Android navigation intent with map fallback.
- Home/Work quick rows are shown only while the relevant route search field is empty and disappear immediately when typing begins.
- general Photon place search appends the current abfahrt.now city when no postcode or explicit comma-locality is supplied; lat/lon bias remains and explicit remote-city searches can override the default.
- added JVM coverage for locality scoping and walking-navigation destination selection.
- no new dependency and no `/trips`, ORS, MapLibre, DataStore or route-sort semantic change.

## v1.1.0 Build 145 — Route sorting / result presentation convergence

- `versionCode = 1450`.
- Four client-side sort modes over the already loaded `/trips` alternatives: Früheste (default), Schnellste, Wenig Umstiege, Wenig Fußweg.
- Deterministic tie-breakers keep ordering stable; no extra API call and no mutation of raw provider data.
- `Wenig Fußweg` uses summed walking-leg duration because the current contract has no walking-distance field.
- Route-leg mode stack keeps the accepted emoji-over-line-badge grammar but direction or walking duration now aligns to the badge baseline.
- Walking legs no longer render meaningless transit delay/platform metadata; secondary leg details are indented from the time column.
- New sorting labels are present in all 22 locale sets; focused JVM tests cover ranking and walking-time calculation.
- No dependency/API/repository/DataStore/ORS/MapLibre/departure sorting/auth change.

### Gate
`:app:testDebugUnitTest :app:assembleDebug` plus runtime route with alternatives; verify all four sort orders and the refined leg header in dark theme.

## v1.1.0 Build 144 — Saved destinations on the Home route search

- `versionCode = 1440`.
- Baseline is the user-supplied corrected Build-143 source ZIP (`SHA-256 6c95246b6782ecd385a011616abeff8d650b0014a31fe7c0fcb774670b53825a`), not the earlier assistant-side pre-fix package.
- Build 143 is accepted: real `:app:testDebugUnitTest :app:assembleDebug` completed with `BUILD SUCCESSFUL in 22s`; the user confirmed Saved Places works.
- Start-page field `Route planen – Ziel eingeben` now exposes configured Home/Work immediately while focused and still empty.
- Choosing Home/Work opens the existing RoutePlanner with Current location as origin and the stored place as destination; no Photon call is needed for the shortcut.
- Typing into the field continues to use the existing general Photon destination search unchanged.
- No new localization key, dependency, DataStore format, API request, `/trips` semantics or route ranking.
- The supplied Logcat contains a `FATAL EXCEPTION` from `com.google.android.odad`; no fatal signature for `now.abfahrt.transit` was found in the inspected capture.
- Client-side route sorting is shifted one build to Build 145 to keep this explicit UX follow-up isolated; routing-time selection becomes Build 146 and remains API-blocked.

### Gate
`:app:testDebugUnitTest :app:assembleDebug`; runtime: tap the Home search field with saved Home/Work configured, select each shortcut, verify direct Planner navigation and unchanged typed Photon search.

## Build 143 compile correction — 2026-09-13

- Real Android compile found `DepartureScreen.kt:675:37 Unresolved reference onOpenSavedPlaces`.
- Cause: the new callback was present on `DepartureScreen` and in `AppNavHost`, but was not threaded into the private `DepartureHeader` composable that invokes the Saved Places menu item.
- Corrected the callback plumbing only; `versionCode` remains `1430`.
- No product logic, API, dependency, locale or persistence behavior changed.
- Re-run `:app:testDebugUnitTest :app:assembleDebug` for acceptance.

## v1.1.0 Build 143 — Saved places + route mode-column convergence

- `versionCode = 1430`.
- Overflow menu gains a protected `Saved places` screen with exactly Home and Work slots.
- Home/Work use the existing Photon general-place search and are persisted locally as title/subtitle/lat/lon in the existing DataStore; current backup/transfer exclusion for that DataStore remains in force.
- Configured Home/Work are quick choices in both RoutePlanner endpoint selectors.
- Route leg identity now mirrors the main departure-card grammar: transport emoji above the line badge in a fixed-width left column; walking gets the same stacked treatment.
- Build 142 real gate archived as accepted (`BUILD SUCCESSFUL in 5s`); Build 143 awaits real Android Gradle/runtime evidence.
- No `/trips`, ORS, MapLibre, dependency or route-ranking change.

## v1.1.0 Build 142 — Route UI convergence

- `versionCode = 1420`.
- provider `TripResponse.region` is no longer rendered on RoutePlanner success or empty states; it remains available internally for API context/diagnostics.
- non-walking route legs reuse the existing `TransportMode.emoji` symbols already shown on departure cards (U-Bahn/S-Bahn/tram/bus/regional/express/ferry).
- current walking badge, line colors, transfer separators, expandable intermediate stops and optional attribution remain unchanged.
- no locale-key, dependency, API/model, ORS, MapLibre, route-ranking or departure-list change.

### Gate
`:app:testDebugUnitTest :app:assembleDebug`; runtime smoke confirms no visible region and correct transport symbols.

## v1.1.0 Build 141 — RoutePlanner Empty/Error/Coverage convergence

- `versionCode = 1410`.
- route empty/error states now use explicit cards with the existing localized retry action.
- empty `/trips` responses keep a non-blank provider region visible instead of losing context.
- optional `TripResponse.attribution` is rendered when supplied.
- `AbfahrtTrips` debug logging now classifies `success`, `empty` and `error` outcomes without logging searched place labels.
- no synthetic out-of-coverage diagnosis is introduced; HTTP 400 remains generic per current OpenAPI.
- no new locale keys, dependency, API/model schema, route ranking or transit geometry.

### Gate
`:app:testDebugUnitTest :app:assembleDebug`; runtime success plus practical empty/error retry smoke.

## Build 140 resource correction — 2026-09-13

- First real Build 140 Gradle gate reached Kotlin compilation but failed at `:app:mergeDebugResources`.
- Cause: the two new Klingon (`values-b+tlh`) expand/collapse strings contained unescaped ASCII apostrophes; Android AAPT requires these apostrophes to be backslash-escaped in this resource syntax.
- Corrected `route_show_intermediate_stops` and `route_hide_intermediate_stops` in-place; `versionCode` remains `1400`.
- No product logic, routing behavior, API/model contract, locale key set or dependency changed.
- Static locale gate now additionally checks all string resources for unescaped ASCII apostrophes.
- Re-run `:app:testDebugUnitTest :app:assembleDebug` for acceptance.

## v1.1.0 Build 140 — Route details / transfer clarity

- `versionCode = 1400`.
- Build 139 accepted: combined unit/debug Gradle gate green; German route screenshot confirms localized walking legs, hidden technical coordinates and improved dark-theme readability.
- transit-to-transit gaps are shown as localized transfer duration; walking access/egress legs do not create extra visual transfers.
- existing `/trips` `intermediateStops` / `stopNames` become inline expandable details; if the provider exposes only a count, the count remains non-expandable.
- intermediate stop names use the existing display normalizer; arrival times are shown where supplied.
- three new route-detail strings are maintained in all 22 locale resource sets.
- no API/model schema, dependency, route ranking, ORS, MapLibre, Departure sorting/dedup or authentication change.

### Gate
`:app:testDebugUnitTest :app:assembleDebug`, then a runtime route containing one real transit transfer and an expandable intermediate-stop leg.

## v1.1.0 Build 139 — Route presentation cleanup / map-inspired polish

- `versionCode = 1390`.
- Route cards visually reworked with clearer hierarchy, friendlier contrast, rounded elevated cards and stronger blue primary action, inspired by modern maps apps without copying Google branding/assets.
- `Walk` is now rendered via the existing localized `ors_mode_walk` resource instead of leaking English provider text into non-English UI.
- route-leg rendering now hides technical coordinate / provider-id strings where possible and replaces them with user-facing labels for current location, origin or destination.
- normal stop names in route legs are cleaned through the existing display normalizer (for example provider suffixes such as `(Berlin)` are reduced in the UI where appropriate).
- transit legs now use colored line badges based on the existing departure badge palette for a more recognizable mode/line presentation.
- added JVM tests for route presentation helpers (walking-leg detection, coordinate/id detection, stop-label cleanup).
- raw API payload and trip models remain unchanged; cleanup is intentionally view-only, consistent with the earlier departure dedup/display decision.

### Gate
`:app:testDebugUnitTest :app:assembleDebug`; runtime smoke with current-location route, address target, walking-first/walking-last route, same-vehicle hint, German locale and dark theme readability.

## Build 138 compile correction — 2026-09-13

- Real Android compile found `DepartureScreen.kt:347:40 Unresolved reference promptText`.
- Corrected the alternate-location empty-state to use the existing localized `search_station_prompt` resource directly.
- No product behavior, API contract, routing logic, dependency, locale key or versionCode changed.
- Corrected Build 138 still awaits real `testDebugUnitTest + assembleDebug` evidence.

## v1.1.0 Build 138 — Route-planen-MVP

- `versionCode = 1380`.
- Startseitensuchfeld kehrt mit neuer Semantik „Route planen – Ziel eingeben“ zurück.
- allgemeine Photon-Zielsuche für Adresse/Ort/POI/Haltestelle; Auswahl öffnet RoutePlanner ohne Zwischentap.
- neuer RoutePlanner mit Von/Nach, aktuellem Standort, Tausch und explizitem „Route finden“.
- Verbindungen werden über den bestehenden abfahrt.now-`/trips`-Pfad geladen und als Trip-/Leg-MVP dargestellt.
- `TripLeg` bildet `stopNames`, `intermediateStops` und `sameVehicle` gemäß aktueller OpenAPI ab; `sameVehicle` wird als „im Fahrzeug bleiben“ dargestellt.
- `/trips`-401 führt über das bestehende Pflicht-Key-Gate zurück zur Key-Korrektur.
- alle neuen User-facing Strings in allen 22 Locale-Ressourcensätzen ergänzt.
- Build-137-Warning auf `Icons.AutoMirrored.Filled.ArrowBack` bereinigt.
- keine neue Dependency, keine ORS-Transitgeometrie, keine Änderung an Departure-Sortierung/Dedup.

### Gate
`:app:testDebugUnitTest :app:assembleDebug`; Startseiten-Zielsuche → Planner; Von/Nach ändern/tauschen; `/trips`-Ergebnisse/Empty/Error; 401; Locale-Smoke.

## v1.1.0 Build 137 — Alternate departures navigation split

- `versionCode = 1370`.
- existing Photon station search / „Abfahrten an anderem Ort“ moved from the home header to protected route `alternate_departures`.
- home header now keeps Refresh direct and exposes alternate departures + Settings via overflow menu.
- alternate flow reuses the existing `DepartureViewModel`, repositories, filters and departure rendering; no shadow architecture or API change.
- leaving the alternate flow resets SearchTarget to CurrentLocation and UI state to Idle, preventing stale alternate departures on Home.
- routing search is intentionally not half-implemented here; Build 138 restores the home search position as „Route planen – Ziel eingeben“.
- Build 136 core runtime evidence: no-key first start blocked as required; normal keyed start confirmed.

### Gate
Combined unit/debug build plus menu/navigation/search/back runtime smoke.

## v1.1.0 Build 136 — Mandatory abfahrt.now API key

- `versionCode = 1360`.
- abfahrt.now access now requires completed onboarding plus a non-blank API key.
- first-run onboarding no longer offers/permits skipping the abfahrt.now key; ORS remains optional.
- existing installations with a blank/decryption-unavailable abfahrt.now key re-enter onboarding; existing key values are prefilled for correction.
- abfahrt.now key cannot be deleted from Settings and the repository defensively rejects empty updates.
- HTTP 401 from the abfahrt.now core flow resets access and returns to key correction without deleting/logging the key.
- no dependency, SDK, ORS, sorting, filtering, MapLibre or 16-KB change.
- Build 135 accepted: `:app:testDebugUnitTest :app:assembleDebug` fully green.

### Gate
Combined unit/debug build plus runtime first-start/settings/401 access checks.

## v1.1.0 Build 135 — JVM Unit-Test Isolation / Test Hygiene

- `versionCode = 1350`.
- Keine Fachlogik-/Dependency-/SDK-Änderung.
- Diagnostic-only Repository-Logs in reinen Enrichment-Helfern sind best-effort, damit lokale Android-JVM-Stubs Tests nicht abbrechen.
- `LocaleParityTest` behandelt `File.parentFile` explizit nullable.
- Build-134-Evidence konvergiert: `walkSeconds` zweimal 40/40 positiv; drei Unit-Test-Fehler waren ausschließlich Log-Stubfehler.

# Changelog

## v1.1.0 Build 134 — abfahrt.now Contract / walkSeconds Observation

Basis: abgenommener Build 133.

### Changed / evidence pending
- `versionCode = 1340`.
- optionales OpenAPI-Feld `stations[].walkSeconds` wird im bestehenden `Station`-Modell nullable erfasst.
- `AbfahrtContract` loggt die reale Feldabdeckung des initialen Stationssatzes.
- neue Gson-Contract-Regressionstests für `timestamp` (Long/ms), `stop`, `stations[].distance`, `walkSeconds`, optionale Felder und unbekannte Forward-Compatibility-Felder.
- Android-17-Baseline-Check nicht mehr auf die überholte Build-133-Versionsnummer fest verdrahtet; er prüft jetzt die akzeptierte API-37-Baseline unabhängig von späteren Buildnummern.
- keine Nutzung von `walkSeconds` in Produkt-/ORS-/Reachability-/Sortier-/Dedup-/Map-Logik.
- keine Dependency-, Endpoint-, UI-, Android-17-, 16-KB- oder Security-Änderung.

### Build-133 acceptance
- realer `assembleDebug` erfolgreich in 6 s.
- die sieben bekannten Kotlin-2.3-Warnings sind verschwunden.
- Runtime-Capture zeigt weiterhin erfolgreiche abfahrt.now-Requests und stabile Refresh-Pipeline ohne App-FATAL-/Native-Linker-Signatur.

### Gate
`testDebugUnitTest` + `assembleDebug` und ein Runtime-Refresh mit `AbfahrtContract walkSeconds coverage=...`. Die Coverage wird nur beobachtet; sie löst noch keine ORS-Optimierung aus.

## v1.1.0 Build 133 — Kotlin 2.3 Warning Hygiene

Basis: abgenommener Build 132.

### Changed / evidence pending
- `versionCode = 1330`.
- beide bekannten Hilt-`@ApplicationContext`-Qualifier verwenden explizit `@param:`; keine globale Annotation-Target-Compileroption.
- sechs vom Kotlin-Compiler als redundant gemeldete Nullability-Operatoren entfernt.
- keine Produkt-/Transit-/ORS-/UI-/Map-/Security-Logikänderung.
- Android-17-Baseline minSdk 34 / compileSdk 37 / targetSdk 37 und Toolchain/Dependencies unverändert.

### Build-132 acceptance
- realer Build erfolgreich; öffentliche HTTPS-Aufrufe zu abfahrt.now, Photon und HEIGIT ORS erfolgreich; RoutePreview `Ready`; keine App-FATAL-/Native-Linker-Signatur; UI im getesteten Profil unauffällig.
- Android-17-/API-37-Migration damit abgeschlossen.

### Gate
Realer Gradle-Build muss erfolgreich sein und die bekannte Kotlin-Warning-Klasse verschwinden; anschließend kurzer Kernflow-Smoke.

## v1.1.0 Build 132 — Android 17 targetSdk 37

Basis: abgenommener Build 131.

### Changed / evidence pending
- `versionCode = 1320`.
- `targetSdk 36 -> 37`; `compileSdk 37` und `minSdk 34` unverändert.
- Toolchain und Dependencies bleiben exakt auf dem abgenommenen Build-131-Stand.
- keine Produkt-/Transit-/ORS-/UI-/Map-/Security-Logikänderung.
- keine `ACCESS_LOCAL_NETWORK`-Permission und keine CT-Ausnahme hinzugefügt.
- Android-17-Readiness-Check auf Target-37-spezifische app-lokale Risikopfade erweitert.

### Build-131 acceptance
- realer Kotlin/Android-Build erfolgreich; acht nicht-blockierende Kotlin-Warnings dokumentiert.
- API-37-Runtime-Kernflow mit abfahrt.now, ORS und MapLibre/RoutePreview ohne App-FATAL/Native-Linker-Crash.
- AAB nach AGP-9-Migration erneut `PAGE_ALIGNMENT_16K`.

### Gate
Release/R8 plus API-37-Runtime unter `targetSdk 37`; reale HTTPS-/CT-Aufrufe, Location/Search, Settings/Sheets, WALK/BIKE, Back, MapLibre sowie Large-/Resizable-Screen-Profil.

## v1.1.0 Build 131 — Android 17 compile/toolchain staging — accepted

Basis: vollständig abgenommener Build 130.

### Changed
- `versionCode = 1310`.
- `compileSdk 36 -> 37`; `targetSdk` bleibt bewusst 36.
- AGP `8.13.2 -> 9.4.0`; Gradle `8.14.5 -> 9.6.0`.
- Migration auf AGP-9-built-in-Kotlin; `org.jetbrains.kotlin.android` entfernt.
- KGP/Compose-Compiler `2.3.21`, KSP `2.3.12`, Hilt `2.60.1`.
- `android.kotlinOptions` entfernt; JVM 17 bleibt über `compileOptions` erhalten.
- keine Änderung an App-Produktlogik oder `app/src/main/java/now/abfahrt/transit`.

### Acceptance
Realer Build und API-37-Runtime-Smoke grün; AAB erneut `PAGE_ALIGNMENT_16K`. Die acht Kotlin-Warnings sind als separater Hygiene-Backlog erfasst. `targetSdk 37` folgt isoliert in Build 132.

## Build 130 — 16-KB accepted 13.09.2026
- Release-APK-Audit: null 64-Bit-Gating-Fehler; DataStore + MapLibre grün; `libandroidx.graphics.path.so` `OK ABSENT`.
- AAB: `PAGE_ALIGNMENT_16K`.
- System-/Runtime-Nachweis: `16384`.

## v1.1.0 Build 130 — API-34+ graphics-path native elimination

Basis: Build 129 + realer Release-APK-Audit vom 12.09.2026.

### Build-129-Evidence
- DataStore 1.2.1: arm64 + x86_64 `OK ELF`.
- MapLibre OpenGL 13.6.0: arm64 + x86_64 `OK ELF`.
- AndroidX graphics-path 1.1.0: arm64 + x86_64 `FAIL GNU_RELRO`, jeweils `endMod16K=0x2000`.
- alle ZIP-Datenoffsets `OK`.

### Changed / artifact evidence pending
- `versionCode = 1300`.
- externer `androidx.graphics:graphics-path`-Prebuilt aus direkter und transitiver Auflösung ausgeschlossen.
- app-lokale API-34+-Kompatibilität im selben Package, auf Framework `PathIterator` und Pure-Kotlin-Conic-Konvertierung basierend.
- keine JNI-, CMake-, NDK- oder `.so`-Ergänzung.
- MapLibre 13.6.0 und DataStore 1.2.1 unverändert.
- neuer statischer Guard `scripts/check_graphics_path_compat.py`.
- Artefakt-Audit auf Version 3 erweitert; er prüft zusätzlich die Abwesenheit von `libandroidx.graphics.path.so`.

### Gate
Release-APK-Audit muss `libandroidx.graphics.path.so` vollständig vermissen und null harte 64-Bit-Gating-Fehler zeigen. Danach folgt der echte 16-KB-Runtime-Smoke.

## Build 129 Artifact Result — MapLibre accepted, graphics-path only blocker

- `arm64-v8a`: graphics-path FAIL, DataStore OK, MapLibre OK.
- `x86_64`: graphics-path FAIL, DataStore OK, MapLibre OK.
- alle ZIP-Offsets OK.
- Ergebnis: zwei harte Gating-Fehler, beide ausschließlich graphics-path. MapLibre-Versionsiteration beendet.

## v1.1.0 Build 129 — MapLibre 13.6.0 final upstream 16-KB test

Basis: Build 128 + realer Release-APK-Audit vom 12.09.2026.

### Changed / artifact evidence pending
- `versionCode = 1290`.
- MapLibre OpenGL `12.3.1 -> 13.6.0`.
- `app/src` bleibt unverändert.
- Source-Preflight erwartet jetzt die Build-129-Evidence-Baseline.

### Trigger
Build 128 lieferte vier harte 64-Bit-ELF-Fehler: `graphics-path` und MapLibre jeweils auf arm64-v8a und x86_64. DataStore sowie alle ZIP-Datenoffsets blieben grün.

### Stop rule
Scheitert MapLibre 13.6.0 erneut, endet die Versionserprobung. Danach nur noch gezielte source-/binary-basierte Remediation.

### Gate
Release-APK bauen und Audit version 2 ausführen. MapLibre muss auf arm64-v8a und x86_64 vollständig grün sein.

## Build 128 Artifact Result — negative evidence

- `arm64-v8a`: graphics-path FAIL, DataStore OK, MapLibre FAIL.
- `x86_64`: graphics-path FAIL, DataStore OK, MapLibre FAIL.
- alle ZIP-Offsets OK.
- Ergebnis: vier harte Gating-Fehler; Build 128 nicht 16-KB-accepted.

## v1.1.0 Build 127 — 16-KB Native Dependency Refresh

Basis: Build 126 + negativer Release-APK-Audit vom 12.09.2026.

### Changed / pending artifact-runtime verification
- DataStore `1.1.2 -> 1.2.1`.
- `androidx.graphics:graphics-path` wird explizit als direkte Abhängigkeit in Version `1.1.0` gesetzt.
- MapLibre `11.12.1 -> 11.13.5`, bewusst innerhalb 11.x.
- `scripts/audit_16kb_artifact.py` verwendet `arm64-v8a` und `x86_64` als harte 64-Bit-Gates; 32-Bit-Abweichungen bleiben als Warnungen sichtbar.
- `scripts/check_16kb_readiness.py` prüft die Remediation-Baselines.
- `versionCode = 1270`.

### Evidence trigger from Build 126
- APK-ZIP-Ausrichtung aller 12 Native-Libraries war korrekt.
- 11/12 ELF-Prüfungen schlugen fehl; 64-Bit betroffen: alle drei arm64-v8a-Libraries sowie graphics-path/DataStore auf x86_64.
- `x86_64/libmaplibre.so` war bereits grün.

### Deliberately unchanged
- keine Änderung an API, UI, Dedup, Stop-ID-Enrichment, ORS, Reachability, Standortlogik oder Keystore-Speicherung.
- kein MapLibre-Major-Sprung.

### Runtime/Artifact-Gate
- neues Release-APK/AAB bauen und erneut prüfen; 64-Bit-ELFs müssen grün sein.
- AAB zusätzlich auf `PAGE_ALIGNMENT_16K` prüfen.
- danach 16-KB-Runtime-Smoke mit `memoryPageSizeBytes=16384` und MapLibre-RoutePreview.

## v1.1.0 Build 126 — 16-KB Page-Size Readiness

Basis: Build 125.

### Added / pending artifact-runtime verification
- Runtime-Diagnostik `AbfahrtCompat memoryPageSizeBytes=...` über `sysconf(_SC_PAGESIZE)`.
- `scripts/check_16kb_readiness.py` für Source-/Toolchain-Preflight.
- `scripts/audit_16kb_artifact.py` für ELF-PT_LOAD- und GNU_RELRO-Alignment aller `.so`; APK zusätzlich mit 16-KB-ZIP-Datenoffsetprüfung bei unkomprimierten nativen Libraries.
- Build-126-Spec/Plan/Tasks und Evidence-Gate.
- `versionCode = 1260`.

### Deliberately unchanged
- MapLibre bleibt 11.12.1; kein Major-Upgrade ohne negative Artefakt-Evidence.
- keine Änderung an UI, APIs, Dedup, ORS, Reachability, Standortlogik oder Key-Speicherung.

### Runtime/Artifact-Gate
- APK/AAB erzeugen und prüfen; bei AAB `PAGE_ALIGNMENT_16K` mit bundletool bestätigen.
- auf 16-KB-Android starten, `memoryPageSizeBytes=16384` nachweisen und MapLibre-RoutePreview öffnen.

### Artifact result — 12.09.2026
- Release-APK **nicht accepted**: 11 ELF-Failures bei 12 Native-Libraries.
- APK-ZIP-Alignment war vollständig grün; der Fehler liegt in vorgefertigten Native-Binaries.
- negative Evidence führte direkt zu Build 127.

## Build 125 Runtime Acceptance — 12.09.2026

- beide Legacy-Keys beim Upgrade erfolgreich migriert (`abfahrt=true ors=true`);
- abfahrt.now- und ORS-Key-Writes werden verschlüsselt gespeichert;
- abfahrt.now und ORS/BIKE arbeiten weiter erfolgreich;
- keine Crypto-/Keystore-Fehler im gelieferten Lauf;
- Build 125 für seinen Security-Scope accepted.

## v1.1.0 Build 125 — API-Key Security / Keystore Migration

Basis: Build 124.

### Security / pending runtime verification
- abfahrt.now- und ORS-API-Key werden at rest mit AES-256/GCM verschlüsselt.
- Der AES-Schlüssel wird als nicht exportierbarer SecretKey im Android Keystore erzeugt/gehalten.
- DataStore bleibt die einzige Preference-Ablage; persistiert wird ein versioniertes `enc:v1:`-Format.
- Bestehende Klartext-Keys werden vor normaler Preference-Nutzung best-effort in place migriert. Ein Altwert wird erst nach erfolgreicher Verschlüsselung ersetzt.
- Neue Key-Writes fallen bei Kryptofehlern nicht auf Klartext zurück.
- Kryptografische Flow-Arbeit läuft auf `Dispatchers.IO`; unveränderte Ciphertexts werden im Prozess entschlüsselt gecacht, damit Netzwerkrequests nicht jedes Mal den Keystore bemühen.
- DataStore bleibt wegen des gerätegebundenen Keystore-Schlüssels aus Cloud-Backup und Device-Transfer ausgeschlossen.
- Keine neue Dependency.
- `versionCode = 1250`.

### Source gate
- `scripts/check_api_key_storage.py` prüft Keystore/AES-GCM/256-Bit, versioniertes Ciphertextformat, verschlüsselte Writes, Off-Main-Flow und Backup-Ausschluss.

### Runtime-Gate
- Upgrade von Build 124 mit bereits gesetztem abfahrt.now- und ORS-Key.
- Migration + Neustart + erfolgreiche Requests zu beiden Providern.
- Key ändern/löschen.

## Build 124 Runtime Acceptance — 12.09.2026

- `900009173` nur noch in technischen `stops=`-Requestparametern; kein `stop=900009173` in der App-Pipeline.
- U6 Richtung Kurt-Schumacher-Platz wird als `U Seestr.` verarbeitet.
- Folgezeiten funktionieren weiter.
- ORS BIKE-Matrix bleibt erfolgreich.
- AB-047/AB-052 für den reproduzierten Fall geschlossen.

## v1.1.0 Build 124 — Numeric Provider Stop-ID Resolution

Basis: Build 123.

### Fixed / pending runtime verification
- AB-047/AB-052: `Departure.stop` kann providerseitig rein numerische IDs wie `900009173` enthalten. Diese wurden von der bisherigen Doppelpunkt-Heuristik nicht als technische IDs erkannt und konnten in der Hauptliste sichtbar werden.
- Exakter `Departure.stop == Station.id`-Match gilt jetzt unabhängig vom Stringformat als Provider-ID-Nachweis.
- Erfolgreiche Auflösung schreibt `Station.name` in `Departure.stop`, erhält aber den Rohwert separat als `providerStopId`.
- Der Post-Merge-Enrichment-Pass erkennt dieselbe Numeric-ID-Variante, wenn die benötigte Station erst in der gemergten Stationsmenge verfügbar ist.
- Keine pauschale Zahlenheuristik: numerische Stops ohne passenden `Station.id`-Match bleiben unverändert.
- `versionCode = 1240`.

### Regressionstests im Source
- Exact Numeric ID -> Station Name + `providerStopId`.
- Numeric ID erst nach Response-Merge auflösbar.
- Numeric String ohne `Station.id`-Match bleibt unverändert.

### Bewusst unverändert
- Dedup/20-m-Toleranz, Stable-Merge, ServiceIdentity, ORS, Reachability, UI und API-Contract.

### Runtime-Gate
- reproduzierten U6-Fall erneut prüfen: kein sichtbares `900009173`, RoutePreview/Folgezeiten weiterhin korrekt.

## Build 123 Runtime Acceptance — 12.09.2026

- ORS HEIGIT-Migration im Device-Logcat bestätigt.
- WALK-Matrix: HTTP 200, finale Stop-Anreicherung.
- WALK/BIKE-Directions: HTTP 200, RoutePreview `Ready`.
- kein beobachteter Request an `api.openrouteservice.org`.
- Build 123 ist für sein einziges Ziel **accepted**.

## v1.1.0 Build 123 — ORS Endpoint Migration

Basis: Build 122 + DOC1.1.

### Changed
- `versionCode = 1230`.
- OpenRouteService-Base-URL von `https://api.openrouteservice.org/` auf `https://api.heigit.org/openrouteservice/` migriert.
- Bestehende relative Matrix-/Directions-Pfade bleiben unverändert und ergeben damit `https://api.heigit.org/openrouteservice/v2/...`.

### Not changed
- keine Änderung an ORS-Request-Bodies, Authentifizierung, WALK/BIKE-Profilen, Cache, Cooldown, Rate-Limit-/Fallbacklogik oder Reachability;
- keine Änderung an UI, Dedup, 20-m-Toleranz, Sortierung, Stable-Merge oder Alias-/Stop-ID-Logik;
- keine Integration von `Station.walkSeconds`.

### Verification
- offizieller HEIGIT-Servicepfad gegen aktuelle ORS-Migrationsangaben verifiziert;
- statischer Check stellt sicher, dass der deprecated Host nicht mehr im Runtime-Kotlin-Source vorkommt;
- reale Matrix-/Directions-Abnahme mit gültigem ORS-Key bleibt als Runtime-Evidence offen.

## DOC1.1 — V122-Feldabnahme und API-Contract-Konvergenz (Runtime bleibt v1.1.0 Build 122)

### Converged
- AB-047/050/051 für die sichtbaren Regressionen geschlossen: mehrtägige Nutzung an verschiedenen Orten ohne sichtbare Provider-Stop-IDs und ohne erneute bekannte Alias-Dopplung.
- `nearestDist + 20 m` als bewusste technische GPS-/Stationspunkt-/Steig-Toleranz festgehalten; kein zusätzlicher Nutzer-Radius.
- Provider-Rohwerte bleiben Quelle; UI-Cleanup und `DepartureServiceIdentity` sind abgeleitete Sichten.
- aktualisierte abfahrt.now-OpenAPI als primäre externe Vertragsreferenz übernommen.
- internes `Departure.stationDistance` als Enrichment aus `Station.distance` dokumentiert; früherer Contract-Gap geschlossen.

### New API observations
- aktuelle OpenAPI enthält optional `Station.walkSeconds`, außerdem `/journey`, `regionBounds` und `routingBounds`.
- diese Fähigkeiten sind noch nicht Teil von Build 122 und werden nicht stillschweigend aktiviert.

### Runtime impact
**Keiner.** App-Quellcode, Ressourcen, Dependencies und Runtime-Version bleiben gegenüber Build 122 unverändert.

---

## DOC1 — `/doc` Governance-Migration (Runtime bleibt v1.1.0 Build 122)

### Added
- `/doc` als einzige normative Quelle der Wahrheit.
- konsolidierte Produkt-, Architektur-, API-, Invariant-, Regression-, Decision-, Findings-, Backlog-, Release-, Handoff-, Test-, Android- und Lokalisierungsdokumente.
- `.specify/memory/constitution.md` als reine Prozess-Governance.
- `/specs/DOC1/` mit Specification, Plan und Tasks.
- `/evidence/` als nicht normative reale Belegschicht; bisherige QA-Historie übernommen.

### Changed
- bisherige `/docs`-Inhalte wurden als historische Evidence gesichert; `/docs` enthält nur noch einen Redirect auf `/doc`.
- Root-README wurde auf einen nicht normativen Einstieg reduziert.
- Default-Doku-Check prüft jetzt die normative Produktdokumentation statt README.

### Important findings at DOC1 audit time
*Historischer Snapshot; DOC1.1 hat mehrere Punkte anschließend konvergiert.*
- AB-047/050/051 waren bei DOC1 noch pending; DOC1.1 schließt die sichtbaren Regressionen nach Feldabnahme.
- Build 122 nutzt weiterhin den deprecated ORS-Host `api.openrouteservice.org`.
- `nearest + 20 m` war bei DOC1 noch ungeklärt; DOC1.1 dokumentiert den Wert als bewusste technische Toleranz.
- Der damalige API-Contract-Befund wurde mit der aktualisierten OpenAPI in DOC1.1 neu bewertet.
- API-Key-Verschlüsselung, 16-KB-Evidence und API-37-Targeting bleiben offen.

### Runtime impact
**Keiner.** App-Quellcode, Ressourcen, Dependencies und Runtime-Version bleiben gegenüber dem gelieferten Build-122-Paket unverändert.

---

## Historische Build-Historie bis Build 122

## v1.1.0 Build 122 — Post-Merge-Stop-ID-Auflösung und stabile Richtungsidentität

Basis: Build 121.

### Runtime-Befund aus Build 121
- AB-047 bleibt offen: Der Logcat enthält weiterhin fünf Refresh-Zyklen mit `unresolved provider stop ids after enrichment`, 168 matchingrelevante `stop=de:`-Vorkommen und 66 `WIN`-Entscheidungen für rohe Provider-IDs.
- Die Build-121-Schutzlogik gegen stille Falschzuordnung greift im sichtbaren Gewinnerpfad besser: Linie 128 an `Walderseestr. (Berlin)` wurde in allen elf protokollierten Dedup-Entscheidungen verworfen. Die falsche Kandidatenkombination ist in den Rohdaten aber weiterhin vorhanden.
- Entscheidender Hinweis: `AbfahrtRouteDestination` konnte rohe IDs wie `de:11000:900011201::5/::6` im bereits gemergten Response exakt auf `Louise-Schroeder-Platz (Berlin)` auflösen. Die benötigte Stationszuordnung existiert also nach dem Response-/Stations-Merge, aber noch nicht zwingend während des einzelnen `stops=`-Batch-Enrichments.
- Der Stable-Merge-Rohpool wuchs während der Session von 145 auf 211 Abfahrten, während die sichtbare Liste stabil bei 15–17 Einträgen blieb. Provider-Namens- und Richtungsvarianten wurden damit weiterhin als getrennte Rohidentitäten mitgeschleppt.

### Fixed / pending runtime verification
- AB-050 neu: Nach dem Stable-Response-Merge wird ein zweiter, begrenzter Stop-ID-Enrichment-Pass gegen die vollständige gemergte Stationsliste ausgeführt. Damit können Plattform-IDs aufgelöst werden, die im einzelnen Add-on-Batch noch keine ausreichende Stationsmetadatenbasis hatten.
- AB-051 neu: Linie+Richtung besitzt jetzt eine zentrale Identität in `DepartureServiceIdentity`. Ein trailing Provider-Ortszusatz wie `(Berlin)` erzeugt keine zweite Richtung mehr. Dedup, `maxPerDirection`, Stable-Merge und Detailsheet-Folgezeiten verwenden dieselbe Identität.
- Rohe Provider-Stop-IDs werden beim Auflösen nicht verworfen, sondern intern separat als `providerStopId` erhalten. Die UI verwendet weiter den lesbaren Stationsnamen; RoutePreview kann weiterhin den exakten technischen Stop-Point nutzen.
- `versionCode = 1220`.

### Tests
- Post-Merge-Regressionsfall: Batch kann eine ID zunächst nicht auflösen; die später gemergte exakte Station löst sie auf und bewahrt `providerStopId`.
- Richtungsalias: `U Osloer Str.` und `U Osloer Str. (Berlin)` sind dieselbe Linie+Richtung; echte Gegenrichtungen bleiben getrennt.
- Stable-Merge und Folgezeiten verwenden dieselbe normalisierte Richtungsidentität.

### Open Finding
- AB-049 bleibt offen: Das Suffix `::1`, `::2`, `::4`, `::5` verhält sich in den beobachteten VBB-Daten wie ein Stop-Point-/Steig-/Plattformindikator. Es wird ab Build 122 technisch erhalten, aber noch nicht als verbundübergreifend garantierte Produktlogik verwendet. Vor einer Coverage-Regel muss geprüft werden, ob das Suffix in allen relevanten Regionen dieselbe Semantik besitzt und wie es sich zum expliziten API-Feld `platform` verhält.

### QA-Fokus
- Erwartetes Log: `post-merge stop-id enrichment before=... resolved=... remaining=0`.
- Kein `stop=de:` mehr in `AbfahrtDistanceDebug`, `AbfahrtDedupDebug`, `AbfahrtFollowUp`, Hauptliste oder Detailsheet. `stationId=de:` und internes `providerStopId` bleiben erlaubt.
- Keine doppelte Linie/Richtung nur wegen `U Osloer Str.` versus `U Osloer Str. (Berlin)`.
- Linie 128 darf nicht sichtbar an `Walderseestr. (Berlin)` gewinnen, solange die nähere korrekte Haltestelle vorhanden ist.
- AB-047/AB-050/AB-051 erst nach einem passenden Logcat-Lauf schließen.

## v1.1.0 Build 121 — Sichere Provider-Basis-ID-Zuordnung

Basis: Build 120.

### Fixed
- AB-048 neu/pending: Der Build-120-Fallback von Plattform-ID auf Basis-ID darf nicht mehr allein über die kürzeste Entfernung entscheiden, wenn unter derselben Basis-ID verschiedene Stationsnamen auftauchen. Das verhinderte Fälle wie Linie 128 → `Walderseestr. (Berlin)`, obwohl die Abfahrt zur `U Osloer Str.` bzw. `U Kurt-Schumacher-Platz` gehört.
- AB-047 bleibt bis zum nächsten Logcat-Lauf offen/pending: Roh-IDs werden weiterhin diagnostiziert, aber Build 121 vermeidet jetzt die gefährlichere stille Falschzuordnung.

### Changed
- `matchStationForDeparture()` verwendet für Provider-ID-Kandidaten mit mehreren Treffern nur dann `minBy(distance)`, wenn alle Kandidaten nach `StationNameNormalizer` denselben physischen Stationsnamen ergeben.
- Bei unterschiedlichen Kandidatennamen wird nicht geraten; stattdessen loggt das Repository `ambiguous provider stop id match skipped ...`.
- `versionCode = 1210`.

### Tests
- Ergänzt `TransitRepositoryEnrichmentTest.doesNotAssignProviderStopIdToDifferentPhysicalStopWhenBaseIdIsAmbiguous()`.
- Ergänzt Gegenprobe, dass mehrere ID-Kandidaten weiterhin aufgelöst werden, wenn sie nach Normalisierung dieselbe physische Haltestelle beschreiben.

### Open Finding
- AB-049 neu/offen: Das Suffix in Provider-IDs nach `::`, z. B. `de:11000:900011207::1`, wirkt wie ein Plattform-/Gleis-/Steig-Indikator. Das kann später helfen, pro Linie/Richtung die relevanten Steige (`::1`, `::2`, ggf. `::4`, `::5`) vollständig abzubilden. Build 121 setzt das bewusst noch nicht als Produktlogik um; Bugfixing der falschen Zuordnung hat Vorrang.

### QA-Fokus
- Kein `stop=de:` in `AbfahrtDistanceDebug`, `AbfahrtDedupDebug`, `AbfahrtFollowUp`, Hauptliste oder Detailsheet.
- Keine Zuordnung von Linie 128 zu `Walderseestr. (Berlin)`, sofern diese Station nicht tatsächlich zur jeweiligen Abfahrt gehört.
- Falls `ambiguous provider stop id match skipped` erscheint, ist das besser als eine falsche Stationszuordnung; der Fall bleibt als Daten-/Mapping-Finding auszuwerten.

## v1.1.0 Build 120 — Provider-Stop-ID-Auflösung mit Plattformsuffix

Basis: Build 119.

### Fixed
- AB-047 wieder geöffnet und tatsächlich erweitert: Direct-Stop-Add-ons können `Departure.stop` als Plattform-/Provider-ID wie `de:11000:900011201::6` liefern, während die bekannte `Station.id` nur als Basis-ID ohne Plattformsuffix vorliegt. Das Repository matcht Stop-IDs jetzt über exakte ID und zusätzlich über die Basis-ID vor `::`.
- Ungelöste Provider-IDs werden nach dem Enrichment gebündelt als `unresolved provider stop ids after enrichment` geloggt, damit der nächste QA-Lauf nicht mehr nur über sichtbare UI-Fragmente suchen muss.

### Added
- Regressionstest für Plattform-ID → Basis-Station-ID, zusätzlich zum bisherigen Exakt-ID-Test.
- QA-Report Build 115–119 und Build-119-Logcat-Findings unter `docs/qa/`.

### Changed
- `versionCode = 1200`.
- AB-047 wird nicht mehr als in Build 119 geschlossen geführt. Build 119 war ein Teilfix, der den realen Plattformsuffix-Fall nicht abgedeckt hat.

### QA-Fokus
- In `AbfahrtDistanceDebug`, `AbfahrtDedupDebug` und `AbfahrtFollowUp` darf kein `stop=de:` mehr auftreten.
- `stationId=de:` bleibt erlaubt.
- Falls `unresolved provider stop ids after enrichment` erscheint, ist AB-047 weiterhin nicht vollständig geschlossen und die Logzeile muss mit Stationslisten-Kontext ausgewertet werden.

## Build 119 — Stop-ID-Fragmente aus Direct-Stop-Add-ons bereinigt

Basis: Build 118.

### Fixed
- AB-047: Abfahrten aus direkten `stops=`-Batches dürfen `Departure.stop` als Provider-/GTFS-Stop-ID liefern, z. B. `de:11000:900011201::6`. Das Repository löst solche IDs jetzt über die Stationsliste auf und schreibt wieder den lesbaren Stationsnamen in `Departure.stop`.
- AB-047: Direct-Stop-Batches reichern ihre Antwort jetzt immer mit der bekannten Anfrage-Stationsliste an. Dadurch funktioniert die ID-Auflösung auch, wenn die API-Antwort selbst keine vollständige Stationsliste mitsendet.

### Added
- Unit-Test `TransitRepositoryEnrichmentTest` für Stop-ID → Stationsname und weiterhin funktionierendes Namensmatching.
- QA-Notiz zu Build 118 unter `docs/qa/2026-07-10_build118_Logcat_Findings.md`.

### Changed
- `versionCode = 1190`.

### QA-Fokus
- Logcat auf `stop=de:` in `AbfahrtDistanceDebug`, `AbfahrtDedupDebug`, Hauptliste und Detailsheet prüfen. Solche IDs dürfen nicht mehr als Haltestellenname erscheinen.
- Direct-Stop-Coverage aus Build 118 bleibt aktiv; nur das Enrichment wird korrigiert.

## v1.1.0 Build 117 — Detailsheet-Folgeabfahrten robuster

Basis: Build 116.

### Fixed
- AB-044 neu/geschlossen: Folgeabfahrten im Detailsheet waren zu streng an exakte Stop-/Plattformvarianten gebunden. Provider liefern dieselbe physische Haltestelle teils als unterschiedliche Stop- oder Plattformvariante. Build 117 nutzt für das Detailsheet weiterhin lokale Daten ohne neue API-Requests, erweitert das Matching aber um den lokalen Gewinner-Cluster: gleiche Linie, gleiche Richtung, gleicher Mode und entweder normalisierte gleiche Haltestelle oder sehr nahe effektive Distanz.

### Added
- Debug-Logging `AbfahrtFollowUp` mit Kandidatenzahlen: `sameLineDir`, `strict`, `local`, `output`, `times`.
- Zusätzliche Tests für Stop-/Plattformvarianten innerhalb desselben lokalen Haltestellenclusters und für klar weiter entfernte Haltestellen.

### Changed
- `versionCode = 1170`.
- Die Übersicht bleibt unverändert bei `maxPerDirection`; nur das Detailsheet nutzt die tolerantere lokale Folgezeiten-Ermittlung.

### QA-Fokus
- Detailsheet bei dichter Taktung erneut öffnen. Erwartung: `In` zeigt bis zu drei Werte, wenn lokale Kandidaten vorhanden sind.
- Logcat prüfen: `AbfahrtFollowUp output=... times=...`.
- Gegenprobe: klar weiter entfernte Haltestellen derselben Linie/Richtung dürfen nicht in die Zeiten übernommen werden.

## v1.1.0 Build 116 — Folgeabfahrten im Detailsheet

Basis: Build 115.

### Fixed
- AB-043 neu/geschlossen: Das Detailsheet zeigt im Feld „In“ bis zu drei Abfahrtszeiten derselben Linie/Richtung/gewinnenden Haltestelle aus dem bereits geladenen lokalen Datensatz, z. B. `5 min, 15 min, 25 min`. Dafür sind keine weiteren API-Requests nötig.

### Added
- Neue Utility `DepartureFollowUpTimes` zur zentralen Ermittlung der Folgeabfahrten für das Detailsheet.
- Neue JVM-Tests `DepartureFollowUpTimesTest` für Same-Service-Matching, generische Stop-Varianten, Plattformschutz und Limitierung auf drei Zeiten.

### Changed
- `versionCode = 1160`.
- Die Übersicht bleibt unverändert ruhig mit `maxPerDirection`; nur das Detailsheet nutzt zusätzliche lokale Abfahrten aus `response.departures`.

### QA-Fokus
- Detailsheet einer Linie mit dichtem Takt öffnen. Erwartung: Feld „In“ zeigt mehrere Zeiten, wenn sie im lokalen Datensatz vorhanden sind.
- Prüfen, dass Zeiten nicht von anderen Haltestellen derselben Linie/Richtung übernommen werden.
- Keine zusätzlichen Netzwerk-/ORS-Requests beim Öffnen des Detailsheets außer der bereits vorhandenen Routenvorschau.

## v1.1.0 Build 115 — Zentrale Sortierung und ORS-Vollständigkeit bei Stable Refresh

Basis: Build 114.

### Fixed
- AB-040 neu/geschlossen: Normale Refreshes am selben Walking-Origin überspringen ORS nicht mehr nur deshalb, weil irgendeine Geh-/Fahrradmetrik vorhanden ist. Wenn aktuelle relevante Stops im Radius noch keine Geh-/Fahrradmetrik oder bewussten Approx-Fallback haben, wird ORS erneut angestoßen. Damit können Haltestellen, die per Luftlinie kurzfristig gewinnen, später durch echte Walkdistance korrekt verlieren.
- AB-041 neu/geschlossen: Die fachliche Display-Sortierung ist wieder eindeutig: „Hier“ bis 35 m zuerst, danach effektive Entfernung, Linie und Abfahrtszeit.
- AB-042 neu/geschlossen: Die Sortier- und Effektivdistanzlogik liegt nicht mehr doppelt im ViewModel und im StableMerger, sondern zentral in `DepartureDisplayOrdering`.

### Added
- Neue Utility `DepartureDisplayOrdering` als Single Source of Truth für Display-Comparator und effektive Distanz.
- Neue JVM-Tests `DepartureDisplayOrderingTest`.
- `DepartureStableMergerTest` prüft zusätzlich, dass der Merger dieselbe zentrale Display-Sortierung nutzt.

### Changed
- `versionCode = 1150`.
- Stable-Merge bleibt aktiv, aber ORS-Skip ist jetzt vollständigkeitsorientiert: relevant ist nicht „existiert irgendwo ORS“, sondern „haben die aktuellen relevanten Stops im Radius eine verwertbare Metrik?“.

### QA-Fokus
- Fälle prüfen, bei denen eine weiter entfernte Haltestelle zuerst per Luftlinie gewinnt und nach ORS gegen eine bessere Walkdistance verlieren muss.
- Mehrere Refresh-Zyklen am selben Standort: `same-origin ORS refresh required unresolvedRelevant=...` darf bei neuen/missing Stops erscheinen; nach erfolgreicher ORS-Abdeckung sollte wieder `skipped ORS enrichment` erscheinen.
- Listenreihenfolge: Hier → Entfernung → Linie → Abfahrtszeit.

## v1.1.0 Build 114 — Stable-Merge-Testbasis und Hard-Reset-Dokumentation

Basis: Build 113.

### Fixed
- AB-038 neu/geschlossen: Der Hard-Reset-Pfad bei bereits sichtbaren Daten ist jetzt ausdrücklich kommentiert und dokumentiert. Bei Standortwechsel bleibt die alte Liste sichtbar, bis ein Ersatzresultat vorliegt; die finale Antwort wird trotzdem ohne Stable-Merge bewertet.
- AB-039 neu/geschlossen: Die Stable-Merge-Zeitfensterlogik ist jetzt explizit auf maximal 5 Minuten begrenzt. Damit werden dichte Takte, z. B. U-Bahn alle 4–6 Minuten, nicht über ein 6–10-Minuten-Fenster zu einer vermeintlich gleichen Abfahrt zusammengezogen.

### Added
- Neue Utility `DepartureStableMerger` für die pure Merge-Kernlogik.
- Neue JVM-Tests `DepartureStableMergerTest` für:
  - transient kleine API-/Add-on-Responses dürfen die Liste bei Stable-Merge nicht kollabieren lassen,
  - Hard-Replacement ohne Stable-Merge darf alte Einträge entfernen,
  - dichte-Takt-Abfahrten mit 6 Minuten Abstand dürfen nicht fälschlich gemerged werden,
  - ORS-Distanzen bleiben bei station-only Updates erhalten.
- QA-Report Build 111–113 und Build-113-Logcat-Findings im Repo verankert.

### Changed
- `versionCode = 1140`.
- `DepartureViewModel` delegiert die Refresh-Merge-Kernlogik an `DepartureStableMerger`; UI-/State-Entscheidungen bleiben im ViewModel.

### QA-Fokus
- Mehrere Auto-Refresh-Zyklen ohne Standortwechsel: Liste darf nicht auf 1–2 Einträge kollabieren.
- Dichte Takte prüfen: gleiche Linie/Richtung mit real getrennten Abfahrten im Abstand von 6+ Minuten dürfen nicht verschwinden.
- Standortwechsel >200 m: alte Liste darf bis zum Ersatzresultat sichtbar bleiben, aber der finale Stand darf nicht durch Stable-Merge künstlich alte Abfahrten festhalten.

## v1.1.0 Build 113 — Stable Merge bei normalem Refresh

Basis: Build 112.

### Fixed
- AB-037 neu/geschlossen: Normale Refreshes mit unverändertem Standort dürfen vorhandene Abfahrten nicht verwerfen, nur weil der API-/Add-on-Response temporär weniger Rohabfahrten liefert. Build 112 konnte dadurch wechselweise von vollständiger Liste auf 1–2 Einträge fallen und beim nächsten Refresh wieder zurückspringen.

### Changed
- Bei finalen Refresh-Ergebnissen am selben Walking-Origin wird der vorherige Rohstand als Stabilitätsbasis in die Merge-Pipeline einbezogen.
- Neue/aktualisierte Abfahrten aus dem API-Response gewinnen weiterhin; nicht erneut gelieferte, aber zeitlich noch gültige Abfahrten bleiben als Grace-State erhalten und werden anschließend normal durch Radius-, Zeit-, Reachability-, Target- und Dedup-Filter bewertet.
- ORS-Metriken werden wie in Build 112 weiter carry-forward übernommen; der Refresh aktualisiert primär Zeit/Delay/Status, ohne die Liste hart zu ersetzen.
- `versionCode = 1130`.

### QA-Fokus
- Mehrere Auto-Refresh-Zyklen ohne Standortwechsel: `filtered` darf nicht periodisch von 9 auf 1/2 und zurück fallen.
- Logcat: `AbfahrtMerge stable refresh merge active` und `preservePrevious=true stableRefreshMerge=true` sollten bei normalen Refreshes erscheinen.
- Weiterhin kein deduplizierter First-Paint-Booster bei normalem Refresh.

## v1.1.0 Build 112 — Stable Refresh und Cold-Start-Dedup-Booster

Basis: Build 111.

### Fixed
- AB-035 neu/geschlossen: Der API-seitig deduplizierte First-Paint-Response wird nur noch als Kaltstart-Booster verwendet, wenn noch keine Abfahrten sichtbar sind. Standortwechsel, manueller Refresh, Auto-Refresh und Settings-bedingte Server-Refreshes warten auf den finalen app-seitig deduplizierten Stand und emittieren keinen vorläufigen dedup-Stand mehr.
- AB-036 neu/geschlossen: Bei unverändertem Standort werden vorhandene ORS-Geh-/Fahrradmetriken auf neue Refresh-Ergebnisse übertragen. Dadurch wird bei normalem Refresh nicht erneut die Gehwegermittlung gestartet und die Liste bleibt ruhiger.

### Changed
- Normale Refreshes rufen den initialen API-Request mit `dedup=off` auf. Die App-eigene Dedup-/Filterpipeline bleibt Quelle der Wahrheit.
- Standortwechsel räumt die sichtbare Liste nicht mehr leer, wenn bereits Abfahrten sichtbar sind; die alte Liste bleibt bis zum Ersatzresultat stehen.
- Layered Loading bleibt auf echte leere Initialzustände begrenzt, damit spätere Refreshes keine zusätzlichen Zwischenzustände erzeugen.
- `versionCode = 1120`.

### QA-Fokus
- Kaltstart: `cold-start dedup booster` darf einmalig erscheinen, bevor Add-ons/ORS fertig sind.
- Manueller Refresh/Auto-Refresh/Settings-Refresh: kein vorläufiger dedup-Booster, keine leere Liste, keine unnötige ORS-Neuberechnung bei gleichem Standort.
- Logcat-Tags: `AbfahrtRepo`, `AbfahrtMerge`, `AbfahrtWalk`, `AbfahrtRefresh`.

## v1.1.0 Build 111 — Generische Stationsnormalisierung für Europa-Provider

Basis: Build 110.

### Fixed
- AB-034 verallgemeinert: Der RoutePreview-/Stationsnamen-Fix ist nicht mehr als Einzelfall `U Franz-Neumann-Platz...` gedacht, sondern über `StationNameNormalizer` generisch für Provider-Ortspräfixe, trailing Ortszusätze, Plattform-/Klammerqualifier und technische Lookup-Namen umgesetzt.
- RoutePreview, Repository-Enrichment, UI-Anzeige von Stop-/Direction-Namen und TargetFilter-Normalisierung nutzen jetzt dieselbe zentrale Normalisierungslogik statt separater Regex-Kopien.

### Added
- Neue zentrale Utility `StationNameNormalizer`.
- Zusätzliche JVM-Tests für Berlin, Düsseldorf, Paris und Madrid sowie für den Schutz von Directions wie `Wedding, Virchow-Klinikum`.
- QA-Review Build 110 unter `docs/qa/2026-07-10_build110_QA-Review.md`.

### Changed
- `versionCode = 1110`.
- AB-017 bleibt geschlossen; AB-034 wird als generischer Provider-Normalisierungsfix dokumentiert, nicht als stationsspezifischer Patch.

### QA-Fokus
- RoutePreview für Stationsnamen mit mehreren Klammer-/Plattformzusätzen öffnen.
- Stop-Namen dürfen Ortspräfixe wie `Berlin,`, `Düsseldorf,`, `Paris,`, `Madrid,` verlieren; Direction-Namen dürfen solche Präfixe nicht verlieren.
- `AbfahrtWalk finalized walk metrics` sollte weiterhin nahe 100 % ORS-Abdeckung zeigen.

## v1.1.0 Build 110 — RoutePreview-Restfall und First-Paint-Semantik

Basis: Build 109.

### Fixed
- AB-034: RoutePreview-Zielauflösung erkennt jetzt auch Stationsnamen mit zusätzlichen Provider-/Plattform-Klammern wie `U Franz-Neumann-Platz (Am Schäfersee) (Berlin)`. Solche Zusätze werden für die technische Zuordnung entfernt, bevor gegen Stationsdaten gematcht wird.
- AB-034: `TransitRepository.canonicalStopName()` nutzt dieselbe erweiterte Klammerbereinigung, damit `stationDistance` bei solchen Varianten häufiger schon im Daten-Enrichment korrekt gesetzt wird.

### Added
- Unit-Test `RoutePreviewLookupTest` für die neue Normalisierung der RoutePreview-Zielauflösung.
- QA-Review Build 109 unter `docs/qa/2026-07-10_build109_QA-Review.md`.

### Changed
- AB-031 dokumentiert: Der initiale, API-seitig deduplizierte Stand wird bei jedem echten Netzwerk-Refresh als vorläufige Liste emittiert, nicht nur beim ersten App-Start. Lokale Refilter-/Throttle-Pfade bleiben rein lokal.
- AB-017 wird mit Verweis auf AB-033 geschlossen; der Build-109-Restfall `stationDistance=0` wird als AB-034 separat geführt und in diesem Build adressiert.
- `versionCode = 1100`.

### QA-Fokus
- Detailsheet für `U Franz-Neumann-Platz (Am Schäfersee) (Berlin)` öffnen: `AbfahrtRouteDestination` sollte nicht mehr `unresolved ... stationDistance=0` melden.
- Kaltstart/Auto-Refresh: `⚡ emitting initial API response...` darf bei echten Netzwerk-Refreshes erscheinen; innerhalb des 60s-Throttle-Fensters sollte stattdessen lokal refiltert werden.

# CHANGELOG

## v1.1.0 Build 108 — ORS-Batching, Direction-Cleanup-Fix und Enrichment-Jank-Reduktion

Basis: Build 107 fix1.

### Fixed
- AB-029 neu/reduziert: CPU-lastige ORS-Enrichment- und Overlay-Arbeit läuft nicht mehr auf dem Main Thread, sondern über `Dispatchers.Default`. Ziel: die in Build 107 fix1 beobachteten Davey-/Skipped-Frame-Ereignisse beim Abschluss jedes ORS-Enrichment-Zyklus reduzieren.
- AB-030 neu/geschlossen: Richtungsnamen werden nicht mehr mit Haltestellen-Präfixlogik behandelt. Für Directions gilt ausschließlich: trailing Provider-Ortszusatz wie `(Berlin)` entfernen; führende kommahaltige Ziele wie `Wedding, Virchow-Klinikum`, `Franz. Buchholz, Guyotstr.` oder `Prenzlauer Berg, Björnsonstr.` bleiben erhalten.

### Changed
- `versionCode = 1080`.
- AB-026 präzisiert: Die Koordinatenzuordnung erreicht 25/25 für die erste Matrix-Charge; verbleibende sichtbare Stops sind eine Kapazitäts-/Batching-Frage, nicht mehr primär ein Name-Matching-Fehler.
- ORS-Matrix-Ermittlung lädt jetzt bis zu drei Chargen à 25 Stationen nach (`WALKING_MAX_STATION_BATCHES = 3`). Damit können auch Haltestellen außerhalb der ersten sichtbaren/nahen Matrix-Charge echte Geh-/Fahrraddistanzen erhalten und anschließend korrekt aus Radius/Reachability herausfallen.
- Matrix-Chargen werden unter `AbfahrtWalk` als `matrix batch X/Y` protokolliert.
- QA-Review Build 107 fix1 übernommen: `docs/qa/2026-07-10_build107_fix1_QA-Review.md`.

### Still open
- AB-026 bleibt zu verifizieren: nächster Logcat soll zeigen, dass nachgeladene Chargen die `approximateStops` deutlich senken und Radius-Filter realistisch nach ORS-Distanz greift.
- AB-029 bleibt bis zur nächsten debugger-freien Logcat-Session offen: keine wiederkehrenden Davey-/Skipped-Frames synchron zu `async ORS enrichment applied`.
- G2 bleibt offen: Kernlogik weiter automatisiert testen.

## v1.1.0 Build 107 fix1 — Kotlin-Regex-Buildfix und generischer Ortsnamen-Cleanup

Basis: Build 107.

### Fixed
- AB-028 neu/geschlossen: Build 107 schlug in `DepartureCard.kt` wegen nicht escapeter Kotlin-Regex-Strings fehl. Die Regexe für Anzeigenamen-Cleanup nutzen jetzt Kotlin-Raw-Strings und kompilieren syntaktisch sauber.
- AB-027 präzisiert: Mit Ortsname ist generisch jeder von der API vor- oder nachgestellte Ort gemeint, nicht Berlin-spezifisch. Trailing Zusätze wie `(Berlin)`, `(Potsdam)`, `(Wien)` usw. werden generisch entfernt; führende Provider-Präfixe nach Muster `Ort, Haltestelle` werden bei Haltestellennamen generisch entfernt.

### Changed
- `versionCode = 1071`.
- Buildfehler-Artefakt übernommen: `docs/qa/2026-07-10_build107_Build_Error.txt`.

### Still open
- AB-026 bleibt offen: Der nächste Logcat muss zeigen, ob die Koordinaten-Zuordnung die ORS-Abdeckung gegenüber 15/25 verbessert.
- G2 bleibt offen: Kernlogik weiter automatisiert testen.


## v1.1.0 Build 107 — ORS-Koordinatenzuordnung und Anzeigenamen-Cleanup

Basis: Build 106.

### Fixed
- AB-026 weitergeführt: ORS-Matrix-Ergebnisse werden nicht mehr über providerabhängige Stationsnamen zugeordnet, sondern caller-seitig über stabile Koordinaten-Buckets (`coord:lat|lon`). Stationsnamen bleiben nur noch Fallback für UI-/Departure-Zuordnung.
- Matrix-Cache-Treffer und Matrix-Response-Zellen behalten damit getrennte Haltestellenpositionen auch dann auseinander, wenn die API Namen mehrfach oder unterschiedlich formatiert liefert.
- Departure-Zuordnung nutzt zusätzlich konservative Distanzschlüssel, damit Haltestellen mit abweichender Schreibweise wie `Berlin, ...`, `... (Berlin)` oder Plattformzusätzen besser dieselben ORS-Metriken erhalten.
- AB-027 neu: Provider-Ortszusätze in Anzeigenamen werden UI-seitig entfernt. `(... )` am Ende und Präfixe wie `Berlin,` verschwinden aus Liste und Detailsheet; die Rohdaten bleiben unverändert.

### Changed
- `versionCode = 1070`.
- `formatStopNameForDisplay()` entfernt Orts-Suffixe jetzt generisch, nicht mehr nur hartkodiert für Berlin.
- Richtungsnamen entfernen ebenfalls trailing Ortszusätze, behalten aber bewusst kommahaltige Zielbezeichnungen wie `Wedding, Virchow-Klinikum`.
- QA-Review Build 106 übernommen: `docs/qa/2026-07-10_build106_QA-Review.md`.

### Still open
- AB-026 bleibt erst geschlossen, wenn die nächste Logcat-Session eine deutlich höhere ORS-Abdeckung zeigt. Ziel: 25/25 bzw. maximal 1–2 echte ORS-/OSM-Ausfälle bei schlechter Datenlage.
- G2: Kernlogik weiter in JVM-Tests absichern.


## v1.1.0 Build 105 — ORS-Fallback/Detailstatus-Hardening

Basis: Build 104 fix1.

### Fixed
- AB-024: Detail-Sheet bleibt bei nicht ORS-aufgelösten Haltestellen nicht mehr dauerhaft auf „Wird ermittelt…" hängen. Nach abgeschlossenem ORS-Enrichment werden verbleibende Stopps mit vorhandener API-/Stationsdistanz als ungefähre Luftlinien-/Fallback-Distanz markiert.
- Detailsheet zeigt für diese Fälle „Keine Gehzeit verfügbar" statt eines endlosen Ladezustands.
- Exakte ORS-Distanzen behalten weiterhin `NearMe`; ungefähre Fallback-Distanzen behalten bewusst das Flugzeug-Icon gemäß D-011.

### Changed
- `versionCode = 1050`.
- ORS-Enrichment loggt künftig unresolved Stopps zusammengefasst unter `AbfahrtWalk` mit `fallbackToApprox`, damit fehlende Matrix-/Directions-Abdeckung gezielt analysierbar bleibt.
- QA-Review zu Build 104 fix1 übernommen: `docs/qa/2026-07-10_build104_fix1_QA-Review.md`.

### Still open
- G2: Filter-/Dedup-/Merge-Pipeline weiter automatisiert testen.
- AB-017 bleibt bis zur nächsten Reproduktion offen, ist aber durch `AbfahrtRoutePreview` beobachtbar.
- AB-018 nur mit debugger-freiem Lauf endgültig bewerten.

## v1.1.0 Build 104 fix1 — Icon-Semantik wiederhergestellt

Basis: Build 104.

### Fixed
- AB-016 wurde als Fehlinterpretation des QA-/Build-Prozesses korrigiert: Das Flugzeug-Icon ist bewusstes Design für ungefähre Luftlinien-/Fallback-Distanz und wurde in Liste und Detailsheet wiederhergestellt.
- Exakte ORS-aufgelöste Geh-/Fahrrad-Distanzen nutzen weiterhin `NearMe`. Das Standort-Pin-Icon bleibt dem Haltestellennamen/Ort vorbehalten.

### Changed
- `versionCode = 1041`.
- Entscheidung zur Distanz-Icon-Semantik in `docs/DECISIONS.md` dokumentiert.
- Testplan und README korrigiert, damit AB-016 künftig nicht erneut als Bug interpretiert wird.

## v1.1.0 Build 104 — UI/RoutePreview/Performance-Hardening

Basis: Build 103 fix1.

### Fixed
- AB-016: zunächst fälschlich geändert; siehe Build 104 fix1. Das Flugzeug-Icon ist bewusstes Design für ungefähre Distanz, exakte ORS-Routen nutzen `NearMe`.
- AB-020: Berliner Straßenbahn-Fallback ergänzt. Neben den Metrotram-Linien werden jetzt auch die numerischen Berliner Tramlinien `12`, `16`, `18`, `21`, `27`, `37`, `50`, `60`, `61`, `62`, `63`, `67`, `68` als Tram erkannt, wenn das API-`mode`-Feld fehlt.
- AB-021: `AbfahrtTargetFilter` erzeugt nicht mehr für jeden O(n²)-Vergleich eine Debug-Zeile. Der Ziel-auf-nähere-Station-Filter berechnet normalisierte Stationsnamen und Distanzen einmalig vor und schreibt nur noch eine Zusammenfassung plus tatsächliche Hide-Entscheidungen.

### Changed
- `versionCode = 1040`.
- Teure Filterläufe werden an zentralen Pfaden über `Dispatchers.Default` ausgeführt: initialer Load, ORS-Enrichment-Resort und asynchrone Refilter-Aufrufe.
- AB-022: RoutePreview/MapLibre wurde instrumentiert (`AbfahrtRoutePreview`), damit State-Übergänge `Hidden`/`Loading`/`Ready`/`Unavailable`, Style-Ladevorgänge und Camera-Fallbacks im nächsten Logcat sichtbar sind.
- AB-023: Die `minSdk = 34`-Entscheidung wurde als Produktentscheidung mit Reichweiten-/Wartungsabwägung dokumentiert.
- QA-Review Build 103 fix1 wurde unter `docs/qa/2026-07-10_build103_fix1_QA-Review.md` übernommen.

### Still open
- AB-017 bleibt fachlich offen, bis die neue RoutePreview-Instrumentierung ein erneutes Auftreten erklärt oder entkräftet.
- AB-018 ist reduziert, aber erst mit einem neuen Logcat-/Release-Smoke-Test endgültig bewertbar.
- AB-008b: lokale API-Key-Verschlüsselung bleibt Security-Backlog.

## v1.1.0 Build 103 fix1 — SDK-37 Build-Fix / Android-17 Readiness gestuft

Basis: Build 103.

### Fixed
- AB-019: Build 103 war lokal nicht baubar, weil `compileSdk = 37` / `targetSdk = 37` gesetzt war, aber die lokale Android SDK Platform 37 nicht installiert/verfügbar war (`Failed to find Platform SDK with path: platforms;android-37`).
- `compileSdk` und `targetSdk` wurden auf 36 zurückgestellt, damit Build und Unit-Tests mit der vorhandenen Android-16/API-36-Umgebung wieder laufen.
- `versionCode = 1031`.

### Changed
- Android-17-Readiness ist ab Build 103 fix1 als gestufte Vorbereitung dokumentiert: adaptive UI, Backup/DataStore-Regeln, Back-Handling und Testmatrix bleiben Teil des Android-17-Plans; der API-37-Target-Schritt folgt erst, wenn Platform SDK 37 in der Buildumgebung verfügbar ist.
- `scripts/check_android17_readiness.py` prüft jetzt die Readiness-Grundlagen und akzeptiert SDK 36 als Staging-Stand sowie SDK 37 als späteren Target-Stand.
- `gradle.properties`: `android.suppressUnsupportedCompileSdk=37` entfernt, weil Build 103 fix1 nicht mehr gegen SDK 37 kompiliert.
- Buildfehler zu Build 103 wurde unter `docs/qa/2026-07-09_build103_sdk37_build_error.md` verankert.

### Not fixed in this build
- API-37-Targeting bleibt offen, bis die lokale Android SDK Platform 37 installierbar/verfügbar ist.
- AB-016, AB-017 und AB-018 bleiben für Build 104 geplant.

## v1.1.0 Build 103 — Android 17 Readiness

Basis: Build 102.

### Changed
- `versionCode = 1030`.
- `compileSdk = 37` und `targetSdk = 37` wurden als Android-17-Ziel gesetzt. Dieser Stand war in der Zielumgebung nicht baubar, weil Platform SDK 37 fehlte; siehe Build 103 fix1.
- `minSdk = 34` gemäß Projektentscheidung: maximal drei Android-Hauptversionen Rückwärtskompatibilität ab Android 17.
- README und `docs/ANDROID_COMPATIBILITY.md` auf Build 103 aktualisiert.

### Added
- `app/src/main/res/xml/data_extraction_rules.xml`: DataStore-Preferences `abfahrt_prefs.preferences_pb` werden aus Cloud-Backup und Gerätetransfer ausgeschlossen.
- `android:dataExtractionRules` im Manifest verdrahtet.
- `scripts/check_android17_readiness.py` als statischer Release-Gate-Check für SDK-Level, LocaleConfig, Back-Callback und Data-Extraction-Regeln.
- Build-102-Smoke-Hinweise aus Logcat/Screenshots in `docs/qa/2026-07-09_build102_smoke_notes.md`.
- Neue Backlog-Bugs aus Build-102-Smoke-Test: AB-016, AB-017, AB-018.

### Not fixed in this build
- AB-016: Distanz-/Navigationsicon wirkt wie Flugzeug statt Fußweg/Distanz.
- AB-017: Routenkartenvorschau kann trotz erfolgreichem ORS-GeoJSON als nicht verfügbar angezeigt werden.
- AB-018: sichtbarer Main-Thread-Jank beim ORS-Enrichment/Resort.

## v1.1.0 Build 102 — Testbasis und QA-Verifikation

Basis: Build 101.

### Fixed
- AB-003: Erweiterungen des Abfahrtsfensters lösen jetzt einen frischen Ladevorgang aus; Verkleinerungen refiltern lokal, wenn Daten frisch sind.
- AB-005: `inferModeFromLine()` klassifiziert bekannte Berliner Metrotram-Linien gezielt als Tram, aber Metrobusse wie M11/M29 und numerische Linien wie 100/200 als Bus.
- AB-014: Unklare Einzelbuchstaben-Farben `P`, `R`, `S`, `T` aus der lokalen Farbtabelle entfernt.
- AB-015: README-Abfahrtsfenster auf tatsächliche Code-Obergrenze 0–120 min korrigiert.

### Added
- Erste JVM-Unit-Testbasis unter `app/src/test`.
- Tests für Mode-Inferenz, API-Mode-Query und Locale-Key-Parität.
- `scripts/check_readme_defaults.py` als Doku/Code-Drift-Check für zentrale Settings-Werte.
- QA-Review zu Build 101 unter `docs/qa/2026-07-09_build101_QA-Review.md`.

### Changed
- `versionCode = 1020`.
- `docs/DECISIONS.md` dokumentiert das Buildnummern-Schema und die neue Zeitfenster-Entscheidung.

### Still open
- Vollständige Unit-Testabdeckung für Ziel-auf-nähere-Station, Dedup/Merge und ViewModel-Refresh-Pfade.
- AB-008 API-Key-Sicherheit / Backup-Ausschluss.
- AB-010/AB-011/AB-013 Clean-Code-Hardening.


## v1.1.0 Build 101 — Release-Hardening

Basis: Build 100 fix1.

### Fixed
- AB-001: ORS-Request-Modelle sind releasefest gegen R8/Gson-Obfuskation abgesichert.
  - `OrsDirectionsRequest` und `OrsMatrixRequest` erhalten `@SerializedName`.
  - ProGuard Keep-Regeln für ORS Request Bodies ergänzt.
- AB-004: Fehlender `stop`-String in Batch-2/Batch-3-Locales ergänzt.
- AB-007: README auf tatsächliche Code-Defaults aktualisiert.
- AB-009: OkHttp `HttpLoggingInterceptor` loggt nur noch in Debug-Builds; Release = `NONE`.
- AB-012: leere Stray-Datei `testfile` aus dem Source-Paket entfernt.

### Changed
- AB-006: Stadt-/verbundspezifische Paris/RATP-Farbzuordnung ohne Region-Kontext deaktiviert. Regionen ohne verlässliche Farbdaten nutzen den generischen Modus-Fallback.
- Repo-Gedächtnis eingeführt: Entscheidungen, Findings, Bugs, Backlog, Testplan, Release-Checkliste, API-Verträge, Lokalisierung und Android-Kompatibilität liegen als Markdown im Source-Paket.

### Not changed
- AB-003: Zeitfensteränderungen bleiben gemäß Code-Quelle lokales Refilter-Verhalten. README und Testplan dokumentieren das.
- AB-002: Automatisierte Tests werden in Build 102 aufgebaut.
- AB-008: Verschlüsselte API-Key-Speicherung / Backup-Ausschluss bleibt Security-Backlog.

## v1.1.0 Build 106

- AB-024 weitergeführt: Detailsheet-Routenvorschau liefert nun zusätzlich ORS-Distanz und -Dauer und ersetzt dort Fallback-/Luftlinienwerte direkt.
- AB-026 neu: ORS-Matrix-Abdeckung als Kernqualität dokumentiert; Matrix-Kandidaten nach normalisiertem Stationsnamen dedupliziert.
- AB-025 geschlossen: Gradle Wrapper 8.14.5, AGP 8.13.2, keine compileSdk-Unterdrückung.

## v1.1.0 Build 109

- AB-031: First Paint verbessert. Initialer deduplizierter API-Stand wird vor Stop-Add-ons sichtbar gemacht.
- AB-032: `Station = Destination` wird als Terminal-/Nicht-weiter-Verbindung ausgeblendet.
- AB-033: RoutePreview-Zielkoordinate wird robuster über ID/Name/Normalisierung/Distanz gefunden; ORS bleibt koordinatenbasiert.
- `versionCode = 1090`.

## Build 118

- AB-045 closed: data catchment window decoupled from visible window; detail sheet can show next follow-up times beyond the overview window without a detail API request.
- AB-046 closed: direct stops add-ons now use bounded nearby stop coverage, not only missing stations. This addresses cases where a station had at least one departure but was still incomplete, e.g. only one U-Bahn direction surfaced.

