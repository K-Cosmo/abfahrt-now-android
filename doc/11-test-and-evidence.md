## DOC2 — documentation convergence gate

DOC2 is documentation/governance-only. It does **not** change app source, Android resources, dependencies, build configuration, `versionCode` or `versionName`.

Acceptance for DOC2 requires:
1. diff is limited to `/doc` and `/specs/DOC2`;
2. Build 151 remains the latest accepted runtime baseline (`versionCode 1510`, `versionName 1.1.0`);
3. REPO1, Build 150 and Build 151 status is consistent across changelog, decisions, evidence, compatibility, localization, backlog, release plan and handoff;
4. Build 152 is described only as in progress/pending runtime evidence;
5. documentation/governance CI is green.

No additional Android runtime smoke is required for DOC2 itself because runtime code is unchanged.

## Build 152 — in progress / pending runtime evidence

PR #6 / `feature/build152-ui-ux` has a successful Android CI #52. The run covers static governance/compatibility gates, committed-wrapper verification, unit tests, debug build and release/R8 build.

This is **not** Build-152 acceptance. Still required before acceptance:
1. F-152-001 access-gate state-path convergence;
2. configured real-device repeated cold starts without visible onboarding/API-key flash;
3. compact RoutePlanner origin/destination UI with origin search, destination search, Home/Work, swap and `Route finden` smoke;
4. compact-width/onboarding visual smoke;
5. ORS→Community-footer spacing acceptance and removal of the unused legacy footer;
6. final `/doc` convergence to the actually verified implementation.

## Build 151 Acceptance — accepted 03.10.2026

Evidence:
1. real-device Settings screenshot confirms independent/unofficial Community identity, abfahrt.now as API/data source, GitHub project CTA, API Privacy/Terms labeling and visible Build 151 version;
2. all six new Community/provider strings exist in all 22 bundled locale sets;
3. Android CI #42 completed static/locale/governance gates, unit tests, debug build and release/R8 build on the same PR state;
4. combined Gradle gate: `./gradlew :app:testDebugUnitTest :app:assembleDebug :app:assembleRelease --no-daemon` → `BUILD SUCCESSFUL in 3m 47s`, 106 actionable tasks executed;
5. no transit, routing, credential, update-check or MapLibre semantic change was part of Build 151.

F-DOC1-015 / B-COMMUNITY-001 are closed. Build 151 is the latest accepted runtime baseline.

## Build 150 Acceptance — accepted 03.10.2026

Evidence:
1. Branch CI green for the anonymous GitHub release update checker;
2. focused JVM coverage validates strict `v<semver>-b<build>` parsing/build comparison and verifies that the GitHub client has no `ApiKeyInterceptor`;
3. real E2E: without a release no dialog appears; an older build detects `v1.1.0-b150` and opens the fixed repository release page; the same build does not show an update prompt;
4. local `gradlew.bat :app:assembleRelease` with R8/Minify succeeded (`BUILD SUCCESSFUL in 52s`);
5. no GitHub token, app API-key forwarding, APK auto-download or silent install path exists.

F-150-001 / B-150-001 are closed.

## REPO1 Acceptance — accepted

Evidence:
1. public GitHub repository is the canonical workspace;
2. `/doc` is the only normative documentation root; legacy `/docs` and duplicate root changelog are removed;
3. full Gradle 9.6.0 wrapper including `gradle-wrapper.jar` is committed;
4. local Windows wrapper/build evidence is green;
5. GitHub Actions uses the committed wrapper directly and completes the repository CI path without bootstrap generation.

F-DOC1-008 and F-DOC1-010 are closed.

## Build 149 Acceptance — pending real evidence
1. `:app:testDebugUnitTest :app:assembleDebug` vollständig grün.
2. JVM policy: HERE => `shouldLoadRoutePreview=false`; Nicht-HERE benötigt ORS-Key + Ziel.
3. Runtime HERE: `Entfernung: Hier`, `Haltestelle/Station erreicht`, Kartenüberschrift `Standort`.
4. HERE-Map zeigt Origin + Haltestelle, aber keine Route-/LineLayer-Polyline.
5. Log enthält `AbfahrtRoutePreview state=Hidden reason=here_no_route`; für diesen Detailsheet-Tap darf kein `AbfahrtRoute`-ORS-route-preview request folgen.
6. HERE-Karte funktioniert auch ohne ORS-Key, sofern Origin/Zielkoordinaten vorhanden sind.
7. Nicht-HERE-Fall: bisherige ORS-RoutePreview weiterhin funktionsfähig.
8. AB-018 Performance wird in diesem Build nicht bewertet/optimiert; nur Regressionen beobachten.

## Build 148 Acceptance — accepted
0. First real gate (historical): failed only in `compileDebugUnitTestKotlin` because an obsolete overlaid Build-147 `GeocodingLocalityScopeTest.kt` still referenced removed `scopePlaceQueryToCity()` (F-148-001). Corrected source must overwrite that legacy path before rerun.
1. `:app:testDebugUnitTest :app:assembleDebug` grün.
2. Unit-Policy: `Bad Saarow`, `Brandenburger Tor`, `Potsdam`, `S Potsdam`, `Potsdam Hauptbahnhof`, PLZ und `Ort, Stadt` bleiben bis auf äußeres Whitespace unverändert.
3. Runtime: allgemeine Photon-HTTP-Requests enthalten für diese Fälle keinen automatisch angehängten City-Text.
4. `lat`/`lon` bleiben bei verfügbarem Standort vorhanden.
5. `PhotonPlaceRank` zeigt pro abgeschlossener Suche Raw Query/Bias sowie bis zu fünf Kandidaten mit Rank, Name, City, State und OSM-Typ.
6. Reihenfolge der allgemeinen UI-Treffer folgt Photon nach Entfernen exakter Anzeige-Duplikate; kein lokales Match-Scoring.
7. separater Stations-Nebenflow und normaler `/trips`-/Sortier-/Walking-Navigation-Flow regressionsfrei.
8. Reale Evidence: corrected Build 148 `BUILD SUCCESSFUL in 1s`; Logcat bestätigt Raw Query + Bias und Provider-Ranking; Feldfeedback 2026-09-23 bestätigt gewünschtes Verhalten.

## Build 147 Acceptance — abgeschlossen/superseded
1. `:app:testDebugUnitTest :app:assembleDebug` real grün (`BUILD SUCCESSFUL in 7s`).
2. Unit-Tests: `Potsdam` bleibt global; `S Potsdam`, `Bhf Potsdam`, `Potsdam Bahnhof`, `Potsdam Hbf` bleiben global; `Brandenburger Tor` wird weiterhin auf aktuelle City gescoped.
3. Runtime auf aktueller City Berlin: Photon-Request für die globalen Fälle darf **kein** `%2C%20Berlin` tragen.
4. `Hauptbahnhof` bleibt als einwortige Suche global; PLZ und explizites `Ort, Stadt` bleiben unverändert.
5. keine Regression im normalen `/trips`-/Sortier-/Walking-Navigation-Flow.

# Test- und Evidence-Prozess

## Build 140 — Acceptance-Gate

Source-Gates:
- Locale-Key-Parität aller 22 Pakete inklusive der neuen Transfer-/Zwischenhalt-Strings.
- JVM-Presentationtests für Transfer-Minuten und Intermediate-Stop-Fallback.
- API-Key-/Android-17-/16-KB-/graphics-path-/ORS-/DOC-Gates unverändert grün.

Reale Evidence:
- `:app:testDebugUnitTest :app:assembleDebug`.
- Route mit zwei aufeinanderfolgenden Transit-Legs: sichtbare, plausible Umstiegsdauer.
- Access-/Egress-Walking darf keinen zusätzlichen Transfer-Chip erzeugen.
- mindestens ein Leg mit realen Zwischenhaltestopps auf- und zuklappen; Namen/Zeiten prüfen.
- Count-only-Leg darf nicht vortäuschen, dass Details verfügbar sind.

## Build 138 — Acceptance-Gate

Source-Gates:
- Locale-Key-Parität aller 22 Pakete.
- API-Key-/Android-17-/16-KB-/graphics-path-/ORS-/DOC-Gates unverändert grün.
- Contract-Tests decken ab Build 138 zusätzlich `/trips` mit optionalen `stopNames`, `intermediateStops`, `sameVehicle` und Backward-Compatibility ab.

Reale Evidence:
- kombinierter Gradle-Lauf `:app:testDebugUnitTest :app:assembleDebug`.
- Home-Photon-Zielsuche mit mindestens Adresse/POI und Haltestelle.
- direkte Navigation zum Planner; Default-Origin aktueller Standort.
- `/trips`-Response und Tripkarten; Start/Ziel ändern/tauschen.
- 401-Key-Gate und Back-Navigation.
- mindestens DE/EN + eine weitere Locale visuell prüfen; alle übrigen werden über Key-Parität geschützt.

## Grundsatz

Kompilieren oder KI-Plausibilität sind keine Abnahme. Entscheidend sind reproduzierbare Tests und reale Laufzeitbelege.

## Workflow

1. Specification
2. Plan
3. Tasks
4. Implementierung
5. Analyse / Converge
6. automatisierte Tests
7. echter Build
8. Runtime-/Logcat-/Screenshot-/API-Evidence
9. Abnahme
10. dauerhafte Erkenntnisse nach `/doc` konvergieren

## Automatisierte statische Checks

```bash
python3 scripts/check_locale_keys.py
python3 scripts/check_doc_defaults.py
python3 scripts/check_android17_readiness.py
python3 scripts/check_api_key_storage.py
```

## Gradle-Gates

In vollständiger Android-/Gradle-Umgebung:

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
./gradlew assembleRelease
```

Release-Build muss Minify/R8 einschließen.

## Runtime-Gates Build 122

- Kaltstart + mindestens vier Same-Origin-Refreshes.
- Provider-Stop-ID-Enrichment.
- Linie/Richtung Osloer-Str.-Alias + echte Gegenrichtung.
- Stable-Merge ohne Listen-Kollaps.
- ORS WALK/BIKE und Approx-Fallback.
- HERE <=35 m.
- Reachability nur mit finaler ORS-Dauer.
- Detailsheet-Folgezeiten ohne zusätzlichen Departure-Request.
- RoutePreview + Fallback.

## Evidence-Struktur

`/evidence` ist append-only bzw. historisch nachvollziehbar. Empfohlen:

```text
evidence/
  DOC1/
  build-122/
  build-123/
  qa/
```

Artefakte dürfen enthalten:
- QA-Review `.md`
- Build-/Test-Logs `.txt`
- Logcat `.txt`
- dokumentierte Feld-/Nutzerabnahme mit klar benanntem Umfang
- API-Samples mit entfernten Geheimnissen
- Screenshots
- Hash-/Manifest-Dateien

Keine API-Keys oder Secrets in Evidence.

### Public-Repository-Regel

Raw Runtime-Evidence wird nicht automatisch in das öffentliche GitHub-Repository übernommen. Logcats, Geräte-Dumps und Screenshots können präzise Koordinaten, Gerätekennungen, lokale Pfade oder andere nicht für die Veröffentlichung bestimmte Daten enthalten. Öffentlich versioniert wird nur bewusst bereinigte Evidence unter `/evidence/public/`; die vollständige Roh-Evidence bleibt lokal/privat und wird nach der Abnahme in `/doc` zusammengefasst.

## Statusregeln

- `planned`: nur geplant.
- `fixed in code`: kausaler Diff/Test vorhanden, Runtime noch nicht belegt.
- `pending evidence`: Implementierung vorhanden, echte Abnahme fehlt.
- `closed`: erforderliche Evidence liegt vor und widerspricht dem Fix nicht. Für sichtbare UX-Regressionen kann wiederholte reale Feldnutzung zusammen mit einem kausalen Codefix ausreichend sein; interne nicht sichtbare Mechanismen benötigen bei Bedarf weiterhin Logs/Tests.
- `superseded`: durch explizite spätere Entscheidung ersetzt.

## DOC1-Evidence

DOC1 ist docs-/governance-only. Deshalb sind für DOC1 ausreichend:
- Runtime-Dateien unverändert gegen Ausgangspaket.
- statische Checks grün.
- `/doc`-Struktur vollständig.
- alte QA-Historie erhalten.
- keine Versionsänderung.

Ein Android-Runtime-Smoke ist für DOC1 selbst nicht nötig, ersetzt aber **nicht** die noch offene V122-Abnahme.

## V122 Feldabnahme 12.09.2026

Für AB-047/050/051 liegt dokumentierte Feldabnahme vor. Siehe [`/evidence/V122/2026-09-12_field-acceptance.md`](../evidence/V122/2026-09-12_field-acceptance.md).

## Build 123 — ORS Endpoint Runtime-Gate — accepted 12.09.2026

Device-Logcats belegen:
1. WALK-Matrix auf `api.heigit.org/openrouteservice/v2/matrix/foot-walking` mit HTTP 200 und finaler Anreicherung.
2. Directions/GeoJSON auf dem HEIGIT-Pfad; zusätzlich BIKE-Directions auf `cycling-regular` mit HTTP 200 und `RoutePreview state=Ready`.
3. Kein beobachteter Request an `api.openrouteservice.org`.
4. Kein 401/403/404 durch die Endpointmigration.
5. Core-Abfahrten werden weiterhin vor ORS angewendet; ORS-Metriken werden bei gleichem Ursprung wiederverwendet.

Evidence liegt unter `/evidence/BUILD123/`.

## Build 124 — Numeric Provider Stop-ID Runtime-Gate — accepted 12.09.2026

Device-Logcat belegt:
1. `900009173` erscheint nur noch als technische ID in `stops=`-Requests.
2. `stop=900009173` erscheint 0-mal in der App-Pipeline.
3. U6 Richtung Kurt-Schumacher-Platz wird als `stop=U Seestr.` verarbeitet.
4. Detailsheet-Folgezeiten funktionieren weiter.
5. ORS BIKE bleibt HTTP 200 und Matrix-Batches werden vollständig geparst.

Evidence liegt unter `/evidence/BUILD124/`.

## Build 125 — API-Key Security Runtime-Gate

Source-Abnahme allein genügt nicht. Auf einem Upgrade-Gerät mit bestehenden abfahrt.now- und ORS-Keys muss gelten:
1. Logtag `AbfahrtSecret` meldet die einmalige Migration ohne Secret-Inhalt.
2. abfahrt.now funktioniert unmittelbar nach Migration.
3. ORS WALK oder BIKE funktioniert unmittelbar nach Migration.
4. Nach vollständigem App-Neustart funktionieren beide Provider weiterhin.
5. Key-Änderung und Key-Löschen bleiben funktionsfähig.
6. Keine FATAL-Exception/ANR durch die Migration.

Evidence darf niemals API-Key oder Ciphertext enthalten.


## Build 126 — 16-KB Page-Size Runtime-/Artifact-Gate

Source-Preflight:

```bash
python scripts/check_16kb_readiness.py
```

APK-Audit (bevorzugt, weil ELF und ZIP-Offset direkt prüfbar sind):

```bash
python scripts/audit_16kb_artifact.py app/build/outputs/apk/release/app-release.apk
# Optional official Android SDK cross-check:
# zipalign -v -c -P 16 4 app/build/outputs/apk/release/app-release.apk
```

AAB-Audit:

```bash
python scripts/audit_16kb_artifact.py app/build/outputs/bundle/release/app-release.aab
bundletool dump config --bundle=app-release.aab | grep alignment
# Erwartung: PAGE_ALIGNMENT_16K
```

Runtime-Gate auf einem Android-16-KB-Image:
- Logcat enthält `AbfahrtCompat: memoryPageSizeBytes=16384`;
- App startet ohne Compatibility-/Native-Crash;
- Abfahrten laden;
- Detailsheet öffnen und MapLibre-RoutePreview darstellen;
- keine `SIGSEGV`, `UnsatisfiedLinkError` oder sonstige native Ladefehler.

Ein normaler 4-KB-Gerätetest ist sinnvoll als Regressionstest, ersetzt aber den 16-KB-Smoke nicht.
## Build 127 — 16-KB Dependency-Remediation Gate

Build 127 darf nicht allein über Gradle Sync oder erfolgreichen normalen 4-KB-Start abgenommen werden. Erforderlich:

```bash
python scripts/check_16kb_readiness.py
python scripts/audit_16kb_artifact.py app/build/outputs/apk/release/app-release.apk
```

Erwartung für das APK:
- kein `FAIL ELF` für `lib/arm64-v8a/...`;
- kein `FAIL ELF` für `lib/x86_64/...`;
- kein `FAIL ZIP` für die 64-Bit-Libraries;
- 32-Bit-Abweichungen dürfen als `WARN` sichtbar bleiben, blockieren aber dieses 64-Bit-Acceptance-Gate nicht.

Zusätzliche AAB-Prüfung:

```bash
bundletool dump config --bundle=app-release.aab | grep alignment
# Erwartung: PAGE_ALIGNMENT_16K
```

Finaler Runtime-Smoke auf echtem 16-KB-System:
- `AbfahrtCompat: memoryPageSizeBytes=16384`;
- Abfahrten laden;
- Detailsheet und MapLibre-RoutePreview öffnen;
- keine Native-Crashes/Linkerfehler.


## Build 128 — MapLibre-only 16-KB remediation gate

Build 128 is intentionally not expected to close the complete 16-KB finding if the current stable AndroidX `graphics-path` binary remains non-compliant.

Run:

```bash
python3 scripts/check_16kb_readiness.py
python3 scripts/audit_16kb_artifact.py app-release.apk
```

The audit must begin with `16-KB audit version=2 gateAbis=arm64-v8a,x86_64` so evidence provenance is explicit.

Build-128 success criterion:
- `lib/arm64-v8a/libmaplibre.so` = `OK ELF`;
- `lib/x86_64/libmaplibre.so` = `OK ELF`;
- no 64-bit MapLibre ZIP failure;
- any remaining hard failure must be attributable only to the separately tracked AndroidX `graphics-path` binary.

After the artifact audit, run a normal route-preview smoke because the dependency crosses from MapLibre 11.x to 12.x while deliberately retaining the OpenGL renderer artifact.

## Build 129 — final MapLibre upstream 16-KB artifact gate

Build 128 is negative evidence: audit version 2 reported four hard 64-bit failures (`graphics-path` + MapLibre on arm64 and x86_64), while DataStore and all ZIP offsets were green.

Build 129 acceptance for its isolated scope requires:

```bash
python3 scripts/audit_16kb_artifact.py app-release.apk
```

The output must start with `16-KB audit version=2 gateAbis=arm64-v8a,x86_64`. For Build 129's MapLibre scope:
- `lib/arm64-v8a/libmaplibre.so` must be `OK ELF`;
- `lib/x86_64/libmaplibre.so` must be `OK ELF`;
- both corresponding ZIP checks must be `OK`;
- a remaining `graphics-path` hard failure does not accept overall 16-KB readiness and remains a separate blocker.

If MapLibre fails either 64-bit ABI again, record the result as negative evidence and stop dependency-version iteration.

## Build 130 — graphics-path native elimination gate

Build 129 hat MapLibre 13.6.0 und DataStore 1.2.1 auf beiden 64-Bit-Gate-ABIs als `OK ELF` belegt. Build 130 darf daher ausschließlich an der graphics-path-Remediation gemessen werden.

### Source-Gates

```bash
python3 scripts/check_graphics_path_compat.py
python3 scripts/check_16kb_readiness.py
```

Erwartet: externer graphics-path-Prebuilt ausgeschlossen, minSdk >=34, Plattform-PathIterator + Pure-Kotlin-Conic-Konvertierung vorhanden, keine JNI/native Marker.

### Release-APK-Gate

```bash
python3 scripts/audit_16kb_artifact.py app-release.apk
```

Der Build-130-Audit muss mit `16-KB audit version=3 gateAbis=arm64-v8a,x86_64` beginnen. Version 3 prüft zusätzlich die Build-130-Invariante, dass `libandroidx.graphics.path.so` vollständig abwesend ist.

Acceptance:
- **keine** `libandroidx.graphics.path.so` im Audit;
- DataStore und MapLibre auf `arm64-v8a` + `x86_64` weiterhin `OK ELF`;
- relevante ZIP-Prüfungen `OK`;
- null 64-Bit/unknown Gating-Fehler.

Ein erfolgreicher Compile/Gradle-Sync allein reicht nicht, weil das Ziel ausdrücklich das erzeugte Artefakt ist.

### Finaler 16-KB-Runtime-Smoke

Erst nach grünem APK-Gate:
- App auf echtem/emuliertem 16-KB-System starten;
- Log `AbfahrtCompat ... memoryPageSizeBytes=16384`;
- Abfahrten normal laden;
- Detailsheet + MapLibre-RoutePreview öffnen;
- keine `NoClassDefFoundError`, `NoSuchMethodError`, `UnsatisfiedLinkError`, SIGSEGV oder Native-Linker-Fehler;
- RoutePreview muss funktional bleiben, damit Compose/graphics-path-Kompatibilität real mitabgedeckt ist.

Bei bestandenem Runtime-Smoke wird F-DOC1-007 geschlossen.


## Build 130 — 16-KB final acceptance — accepted 13.09.2026

Acceptance-Kette:
- Release-APK-Audit v3: null 64-Bit-Gating-Fehler, MapLibre/DataStore grün, `OK ABSENT libandroidx.graphics.path.so`;
- AAB-Konfiguration: `PAGE_ALIGNMENT_16K`;
- echtes 16-KB-System: Page Size `16384`;
- App-Log: `AbfahrtCompat memoryPageSizeBytes=16384`;
- Kernflow inklusive MapLibre-RoutePreview ohne Class-/Linker-/Native-Crash.

F-DOC1-007 ist geschlossen.

## Build 131 — Android 17 compile/toolchain gate — accepted

Build 131 hielt `targetSdk = 36` absichtlich fest und prüfte zuerst die neue Buildbasis. In einer vollständigen Android-Umgebung:

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
./gradlew assembleRelease
```

Erforderliche Evidence:
- Gradle Sync/Build mit Android SDK Platform 37;
- kein AGP-/built-in-Kotlin-/KSP-/Hilt-/Compose-Konfigurationsfehler;
- Release-Minify/R8 erfolgreich;
- API-37-Runtime: Appstart, Standort oder Ortssuche, Abfahrten, Settings, Detailsheet, WALK/BIKE, RoutePreview;
- kein FATAL/ANR, `NoClassDefFoundError`, `NoSuchMethodError`, Hilt/KSP-Codegen-Fehler oder Native-Linkerfehler.

Empfohlen nach dem Release-Build:

```bash
python3 scripts/audit_16kb_artifact.py app/build/outputs/apk/release/app-release.apk
```

Build-131-Evidence ist grün: Compile/Build lief mit nicht-blockierenden Kotlin-Warnings, der API-37-Kernflow lief ohne App-FATAL/Native-Linker-Crash und das AAB blieb `PAGE_ALIGNMENT_16K`. Damit darf Build 132 `targetSdk = 37` aktivieren.


## Build 132 — Android 17 targetSdk-37 behavior gate

Build 132 verändert nur `versionCode` und `targetSdk`. Abnahme in vollständiger Android-Umgebung:

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
./gradlew assembleRelease
```

Runtime-Evidence auf API 37 bei `targetSdk 37`:
- Appstart/Kaltstart;
- Standortfreigabe, aktueller Standort und Ortssuche;
- Abfahrtsliste/Refresh/Settings/Detailsheet;
- WALK und BIKE mit erfolgreichen HEIGIT-ORS-Calls;
- Photon-Suche;
- RoutePreview/MapLibre einschließlich OSM-Tiles;
- Back-Navigation/Sheets;
- großes/resizable Emulatorprofil.

Logcat gezielt prüfen auf:
- TLS/Certificate-Transparency-Handshakefehler;
- `SecurityException`/Permissionfehler, insbesondere kein unerwarteter LAN-Prompt;
- FATAL/ANR;
- `NoClassDefFoundError`/`NoSuchMethodError`;
- `UnsatisfiedLinkError`/SIGSEGV/native crashes.

Die beiden Build-131-Annotation-Warnings und sechs Nullability-Warnings sind bekannte Hygiene-Evidence, aber kein Build-132-Acceptance-Blocker, solange keine neue Warning-/Error-Klasse hinzukommt.


## Build 132 — Android 17 / targetSdk 37 — accepted 2026-09-13

Evidence:
- `assembleDebug`: erfolgreich;
- abfahrt.now HTTP 200;
- Photon HTTP 200;
- HEIGIT ORS Matrix + Directions HTTP 200;
- RoutePreview `state=Ready`;
- keine App-FATAL-/`UnsatisfiedLinkError`-/`SIGSEGV`-Signatur;
- UI im getesteten Profil laut Nutzer unauffällig.

Damit ist die funktionale Android-17-/API-37-Migration geschlossen. Release/R8 sowie die große Geräte-/Resizable-Matrix bleiben reguläre Release-/Regressionstest-Gates.

## Build 133 — Kotlin-2.3-Warning-Hygiene Gate

In vollständiger Android-/Gradle-Umgebung:

```bash
./gradlew assembleDebug
```

Akzeptanz:
- Build erfolgreich;
- keine Annotation-Default-Target-Warning für `@ApplicationContext`;
- keine der sechs bekannten `Unnecessary safe call` / `Unnecessary non-null assertion`-Warnings;
- kurzer Runtime-Smoke: Abfahrten laden, Ort suchen, RoutePreview öffnen.

## Build 134 — API contract / walkSeconds observation gate

Build 134 ist kein ORS-Optimierungsbuild. Abnahme:

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

Erforderlich:
- `AbfahrtApiContractTest` besteht;
- keine Rückkehr der Build-132/133-Kotlin-Warnings;
- normaler `/departures`-Refresh erfolgreich;
- Logtag `AbfahrtContract` meldet `walkSeconds coverage=X/Y` für den initialen Stationssatz;
- keine FATAL-/Parser-Regression.

Die Coverage-Zahl ist Beobachtung, kein Qualitätsziel. `0/Y` ist ein gültiges Ergebnis und würde gerade gegen eine vorschnelle Nutzung des Feldes sprechen.



## Build 135 — local JVM unit-test isolation gate

Build 134 Evidence:
- `assembleDebug` grün;
- 57 Tests gestartet, 54 grün;
- drei Stop-ID-Enrichment-Tests scheiterten ausschließlich an `android.util.Log`-Stubs;
- Runtime `walkSeconds` zweimal 40/40 positiv.

Build 135 Acceptance:

```text
:app:testDebugUnitTest :app:assembleDebug
```

Erforderlich:
- `BUILD SUCCESSFUL`;
- alle 57 Tests grün;
- keine `Method d/w in android.util.Log not mocked`;
- keine `LocaleParityTest.kt`-Nullability-Warnung;
- normaler Appstart/Refresh ohne Regression.


## Build 135 acceptance — 2026-09-13

Realer kombinierter Gradle-Lauf `:app:testDebugUnitTest :app:assembleDebug`: **BUILD SUCCESSFUL in 2s**. Der Build-134-False-Failure durch Android-Log-Stubs sowie die LocaleParity-Warnung sind damit geschlossen.

## Build 136 — Pflicht-Key-Gate

Zusätzlich zum grünen Unit-/Debug-Build ist Runtime-Evidence erforderlich: (1) Erststart ohne Key kann nicht fortfahren; (2) gültiger Key + optional leerer ORS-Key erreicht die Abfahrten; (3) abfahrt.now-Key ist in Settings nicht löschbar; (4) keylose Bestandsinstallation re-entert Onboarding; (5) HTTP 401 führt zur Key-Korrektur.

## Build 136 runtime — 2026-09-13

Erststart auf virtuellem Gerät real geprüft: ohne abfahrt.now-Key kein Fortfahren; nach Eingabe startet der App-/Departure-Flow. Das Logcat ist wegen nachträglich gesetztem Emulator-Standort stark verrauscht, zeigt im beobachteten App-Lauf aber keinen app-seitigen Fatal-/Linker-Crash. Settings-Delete und gezielter 401-Pfad wurden in diesem Lauf nicht separat provoziert.

## Build 137 — Navigation-Split-Gate

Erforderlich: (1) `testDebugUnitTest + assembleDebug` grün; (2) Startseiten-Overflow öffnet Alternate/Settings; (3) Alternate-Route sucht/lädt Stationsabfahrten wie zuvor; (4) Header- und System-Back resetten auf CurrentLocation/Idle und Home lädt Standortdaten frisch; (5) kein Fatal/ANR/Navigation-Crash.


## Build 146 Acceptance
1. `:app:testDebugUnitTest :app:assembleDebug` grün.
2. Route mit erstem und letztem Walking-Leg: Uhrzeiten zentriert in der Modusspalte; Navigation-Icon oben rechts; beide Intents öffnen mindestens eine Navigations-/Karten-App ohne Crash.
3. Startseiten- und Planner-Suche: Zuhause/Arbeit nur im leeren Feld, ab erstem Zeichen nicht mehr sichtbar.
4. Bei aktueller City Berlin: unqualifizierte Suche `Hauptbahnhof` sendet Photon als `Hauptbahnhof, Berlin`; `28195 Hauptbahnhof` und `Hauptbahnhof, Bremen` bleiben unverändert.
5. normaler `/trips`-Flow und Build-145-Sortierungen regressionsfrei.
