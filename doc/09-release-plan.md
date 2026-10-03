## Build 149 — HERE detail location map (implementiert, Evidence ausstehend)

- Build 148 ist real gebaut und für D-069/Photon-Ranking abgenommen.
- reale Feld-Evidence öffnet das HERE-/Map-Restthema wieder: Detailsheet zeigte trotz `Hier`/`Haltestelle erreicht` eine ORS-Route.
- Build 149 nutzt weiterhin denselben <=35-m-HERE-Override, verhindert dort aber jeden ORS-RoutePreview-Call.
- HERE-Karte zeigt nur Query-Origin + Haltestelle und heißt semantisch Standort statt Route.
- keine neue Dependency, keine API-/Sortier-/Persistenz-/Photon-Änderung.
- AB-018 Startup/Main-Thread-Performance bleibt bewusst Build 150/Instrumentierung.

Gate: `:app:testDebugUnitTest :app:assembleDebug` + Runtime-Screenshot/Logcat eines HERE-Falls (keine `AbfahrtRoute` ORS-preview-Anfrage/keine blaue Polyline) + Nicht-HERE-RoutePreview als Regression-Smoke.

## Build 148 — Raw Photon query + location bias (abgenommen)

- Build 147 ist real gebaut (`BUILD SUCCESSFUL in 7s`) und bestätigt die Escape-Hatches für `Potsdam`/`Hauptbahnhof`; `Bad Saarow` wird jedoch weiterhin als `Bad Saarow, Berlin` gesendet.
- Build 148 entfernt die City-Umschreibung vollständig aus der allgemeinen Photon-Zielsuche.
- `q` = getrimmter Originaltext; `lat`/`lon` bleiben als weicher Bias.
- allgemeine Treffer behalten Photon-Reihenfolge; nur exakte Anzeige-Duplikate werden entfernt.
- neues `PhotonPlaceRank`-Logging dokumentiert Query, Bias und die ersten fünf Photon-Kandidaten.
- keine Änderung an Stations-Nebenflow, `/trips`, ORS, MapLibre, DataStore, Routensortierung oder Dependencies.

Gate: `:app:testDebugUnitTest :app:assembleDebug` + Runtime mindestens `Bad Saarow`, `Brandenburger Tor`, `Potsdam`, `S Potsdam`, `Hauptbahnhof`. Im HTTP-Log darf bei keinem dieser Fälle automatisch `, Berlin` angehängt werden; `PhotonPlaceRank` muss die ersten Kandidaten in Photon-Reihenfolge zeigen. Danach Ranking qualitativ prüfen, **ohne** vorab eine neue Heuristik einzubauen.

## Build 147 — Photon locality-scope correction (real gebaut; Suchregel durch Build 148 superseded)

- Build 146 Gradle-Gate ist real grün; Routing-Layout und Walking-Navigation sind im Logcat funktional belegt.
- Build-146-Lokalitätsscope wird **nicht** vollständig akzeptiert: Runtime zeigt `Potsdam -> Potsdam, Berlin` und `S Potsdam -> S Potsdam, Berlin`.
- Build 147 macht den Scope weich: einwortige Ziele und Transit-Qualifier bleiben global; mehrwortige unqualifizierte Ziele bleiben lokal priorisiert.
- keine neue Dependency, kein zweiter Photon-Request und keine Änderung an `/trips`, ORS, MapLibre, DataStore oder Route-Sortierung.

Gate: `:app:testDebugUnitTest :app:assembleDebug` + Runtime mit `Potsdam`, `S Potsdam`, `Bhf Potsdam`/`Potsdam Hbf`, `Hauptbahnhof`, `Brandenburger Tor`, PLZ und `Ort, Stadt`. Im Logcat müssen die globalen Escape-Hatches ohne `, Berlin` an Photon gehen, während `Brandenburger Tor` weiterhin lokal gescoped wird.

## Build 146 — Routing UX convergence / local search scope (teilweise abgenommen; Suchscope superseded durch Build 147)

- Build 145 real grün und funktional bestätigt (Sortierung angekommen).
- Route-Leg-Zeiten werden in der festen linken Modus-/Linien-Spalte zentriert; Stop-/Detailinformationen erhalten eine gemeinsame Inhaltsachse.
- erster/letzter Walking-Leg: Navigation-Icon oben rechts, impliziter Android-Navigationsintent mit Karten-Fallback.
- Zuhause/Arbeit verschwinden auf Home und im RoutePlanner sofort, sobald Suchtext eingegeben wird.
- Photon-Allgemeinsuche ergänzt die aktuelle City, wenn keine PLZ bzw. explizite `query, locality`-Angabe vorhanden ist; bestehende Lat/Lon-Bias bleibt erhalten.
- keine neue Dependency, keine `/trips`-/ORS-/MapLibre-/Sortierlogikänderung.
- Zeitwahl bleibt API-blockiert; `routingBounds` bleibt nächster möglicher Coverage-Schritt, falls Backend-Zeitparameter noch fehlen.

Gate: `:app:testDebugUnitTest :app:assembleDebug` + visueller Route-Leg-Smoke + Navigation-Icon erster/letzter Fußweg + Home/Planner-Suche `Hauptbahnhof` war im Build-146-Scope lokal; diese Regel wird durch Build 147 zugunsten der weicheren D-068-Semantik superseded.

## Build 145 — Route sorting + result presentation convergence (abgenommen)

- Build 144 ist real grün und funktional bestätigt.
- vier lokale Sortierungen über vorhandene `/trips`-Alternativen: Früheste (default), Schnellste, Wenig Umstiege, Wenig Fußweg.
- kein zusätzlicher API-Call; deterministische Tie-Breaker.
- Wenig Fußweg nutzt Walking-Leg-Zeit als Proxy, weil der Vertrag keine Gehstrecke liefert.
- Route-Leg-Köpfe werden visuell ausgerichtet; Walking-Legs zeigen keine transit-spezifische Verspätungs-/Gleiszeile.
- alle neuen Labels in 22 Locale-Paketen.
- Build 146: Zeitwahl bleibt API-blockiert. Wenn die Backend-Erweiterung noch nicht vorliegt, folgt statt Simulation ein anderer separat entschiedener Backlog-Punkt.

Gate: `:app:testDebugUnitTest :app:assembleDebug` + Route mit mehreren Alternativen; alle vier Sortierungen umschalten und visuelle Header-/Walking-Darstellung prüfen.

## Build 144 — Saved destinations on Home search (abgenommen)

- Build-143-fixed ist die verbindliche Source-Baseline; Build 143 real grün und funktional bestätigt.
- gespeicherte Zuhause-/Arbeit-Orte erscheinen bei Fokus im leeren Startseitenfeld `Route planen – Ziel eingeben`.
- Auswahl springt direkt in den RoutePlanner mit aktuellem Standort als Start.
- Photon-Suche bleibt ab dem ersten getippten Text unverändert.
- keine neue Dependency, Persistenz oder API-/Rankingänderung.
- Build 145: lokale Routensortierung.
- Build 146: Zeitwahl, weiterhin durch aktuellen `/trips`-Vertrag blockiert.

Gate: `:app:testDebugUnitTest :app:assembleDebug` + Home/Work-Shortcut + typed Photon smoke.

## Build 143 — Saved places + route mode-column convergence (abgenommen)

- `Gespeicherte Orte` im bestehenden Startseiten-Menü; genau zwei Slots: Zuhause und Arbeit.
- bestehende Photon-Allgemeinsuche; lokale Persistenz Titel/Untertitel/Lat/Lon im vorhandenen DataStore.
- RoutePlanner bietet konfigurierte Zuhause/Arbeit für Von und Nach als Schnellwahl.
- Verkehrsmittel-Symbol und Linienbadge in Route-Legs vertikal in einer festen linken Spalte, analog Hauptansicht.
- keine neue Dependency und keine Änderung an `/trips`, ORS, MapLibre oder Routing-Ranking.
- Build 144 ergänzt die Startseiten-Schnellziele; Build 145 übernimmt Routensortierung; Zeitwahl wird Build 146 und bleibt bis API-Unterstützung blockiert.

Gate: `:app:testDebugUnitTest :app:assembleDebug` + Home/Work Set/Change/Remove + RoutePlanner From/To + visueller Leg-Layout-Smoke.

## Build 142 — Route UI convergence (abgenommen)

- realer kombinierter Gradle-Gate: `BUILD SUCCESSFUL in 5s`.
- sichtbare Regionsanzeige entfernt; Verkehrsmittel-Symbole in Route-Legs angekommen.
- UX-Follow-up: Symbol/Badge sollen wie in der Hauptansicht untereinander stehen; Umsetzung bewusst in Build 143.

# Release-Plan

## Build 141 — RoutePlanner Empty/Error/Coverage convergence (abgenommen)

Scope:
- Empty- und Error-State als klare Karten mit vorhandenem lokalisiertem „Erneut versuchen“.
- Provider-Region bleibt bei leerem 200-Ergebnis sichtbar, wenn vorhanden.
- optionale `/trips`-Attribution wird angezeigt.
- Debug-Log klassifiziert `success`/`empty`/`error` ohne Ortsnamen; Coverage bleibt Evidence statt Heuristik.
- keine synthetische Out-of-Coverage-Diagnose, keine Transitgeometrie, kein Ranking-/API-/Dependency-Wechsel.

Gate:
1. `:app:testDebugUnitTest :app:assembleDebug` vollständig grün.
2. normaler `/trips`-Erfolg unverändert.
3. Empty oder Error zeigt Retry und bleibt stabil.
4. bei leerem 200-Resultat Region sichtbar, wenn Provider sie liefert.
5. optionale Attribution sichtbar, wenn vorhanden.

## Build 140 — Route-Details / Transferklarheit (abgenommen)

- kombinierter Gradle-Gate grün.
- Runtime-Screenshot bestätigt echte Transit-zu-Transit-Umstiegsdauer (`4 min Umstieg`).
- Access-Walk erzeugt keinen künstlichen Umstieg.
- Zwischenhalte expandieren inline mit realen Namen/Zeiten.
- `/trips` antwortete HTTP 200, Region Berlin/Brandenburg, sechs Optionen; keine app-seitige Fatal-Signatur im gelieferten Log.

## Build 138 — Route-planen-MVP (abgenommen)

Scope:
- Startseiten-Zielsuche „Route planen – Ziel eingeben“; allgemeines Photon statt Stationsfilter.
- Treffer öffnet direkt den RoutePlanner.
- Von/Nach änderbar, aktueller Standort, Tausch, expliziter `/trips`-Call.
- Trip-/Leg-MVP inkl. `sameVehicle`; optionale Zwischenhaltefelder werden geparst.
- 22 Locale-Pakete vollständig ergänzt; AutoMirrored-Warning aus Build 137 bereinigt.

Gate:
1. `:app:testDebugUnitTest :app:assembleDebug` vollständig grün.
2. Startseite: allgemeines Ziel (Adresse/POI/Haltestelle) suchen und auswählen.
3. Planner öffnet direkt mit aktuellem Standort + gewähltem Ziel.
4. Von/Nach ändern und tauschen; „Route finden“ liefert `/trips`-Ergebnisse oder sauberen Empty/Error-State.
5. `sameVehicle` bei realem Beispiel als „im Fahrzeug bleiben“ ohne künstlichen Umstieg.
6. 401 aus `/trips` führt in Pflicht-Key-Korrektur.
7. mindestens DE/EN plus eine weitere Sprache visuell smoken; Source-Gate prüft alle 22 Keys.

## Historischer Folgeschritt — Build 139 (abgenommen)

Build 139 bereinigte technische Rohwerte und Lokalisierung im Route-UI und verbesserte die visuelle Hierarchie. Build 140 übernimmt die damals bewusst noch offene Zwischenhalte-/Transferdarstellung.

## Aktueller Stand

- Build 123: ORS-HEIGIT-Migration — **accepted**.
- Build 124: numerische Provider-Stop-ID — **accepted**.
- Build 125: API-Key-Keystore-Migration — **accepted**.
- Builds 126–129: 16-KB-Diagnose/Dependency-Remediation — historische Evidence.
- Build 130: 16-KB native elimination — **accepted vollständig** (APK, AAB, 16-KB-Runtime).
- Build 131: Android-17-Compile-/Toolchain-Staging — **accepted** (realer Build, API-37-Runtime, AAB `PAGE_ALIGNMENT_16K`).
- Build 132: Android-17-Target-37-Aktivierung — **accepted für funktionalen Migrationsscope** (realer Debug-Build + API-37-Kernflow; Release/R8 bleibt Release-Gate).
- Build 133: Kotlin-2.3-Warning-Hygiene — **accepted** (realer Build ohne die sieben bekannten Warnings).
- Build 134: abfahrt.now Contract / `Station.walkSeconds` Observation — **observation accepted**; `assembleDebug` grün, 54/57 Unit-Tests grün, drei bestehende Tests nur wegen nicht gemocktem `android.util.Log` rot; Runtime zweimal `40/40` positive Coverage.
- Build 135: JVM Unit-Test Isolation / Test Hygiene — **accepted** (`testDebugUnitTest` + `assembleDebug` grün).
- Build 136: verpflichtender abfahrt.now-Key — **core runtime accepted** (Erststart ohne Key blockiert; normaler Start mit Key bestätigt; Settings-Delete/401 bleiben Regressionstests).
- Build 137: Photon-Nebenflow auf eigene Menü-Seite — **accepted** (kombinierter Gradle-Gate grün; Menü/Locales bestätigt).
- Build 138: Route-planen-MVP — **accepted for MVP flow**.
- Build 139: Route-Presentation-Cleanup — **accepted** (Gradle gate + runtime screenshot; no app-fatal signature in supplied log).
- Build 140: Route-Details / Transferklarheit — **accepted** (realer Gradle-Gate + Runtime-Screenshot).
- Build 141: Empty/Error/Coverage convergence — **accepted** (realer Gradle-Gate + normaler Runtime-Smoke).
- Build 142: Route UI convergence — **accepted** (realer Gradle-Gate; Region entfernt, Symbole angekommen).
- Build 143: Saved places + mode-column convergence — **accepted**.
- Build 144: Saved destinations on Home search — **accepted**.
- Build 145: Route sorting + result presentation convergence — **accepted**.
- Build 146: Routing UX / Walking-Navigation — **Gradle + Kern-Runtime grün**; harte Locality-Scope-Regel wegen F-146-004 **nicht akzeptiert** und durch Build 147 korrigiert.
- Build 147: Photon Locality Scope Correction — **Gradle grün; Runtime teilweise erfolgreich, aber durch F-147-001/D-069 superseded**.
- Build 148: Raw Photon Query + Location Bias — **accepted** (realer Gradle-Gate + Runtime-Ranking-Evidence + Feldbestätigung).
- Build 149: HERE detail location map — **implemented in source; acceptance pending**.

## Build 131 — Android 17 Phase 1: Toolchain + compileSdk 37

Scope:
- `versionCode = 1310`;
- `compileSdk 36 -> 37`;
- `targetSdk = 36` bleibt absichtlich unverändert;
- AGP `8.13.2 -> 9.4.0`;
- Gradle `8.14.5 -> 9.6.0`;
- Migration auf AGP-9-built-in-Kotlin; `kotlin-android` wird entfernt;
- KGP/Compose-Compiler `2.3.21`, KSP `2.3.12`, Hilt `2.60.1`;
- keine Änderung an Transit-, ORS-, UI-, Security-, Sorting-, Dedup- oder Map-Logik;
- Build-130-16-KB-Remediation bleibt unverändert.

Gate:
1. Android SDK Platform 37 / aktuelle Build-Tools installiert.
2. Gradle Sync erfolgreich.
3. `testDebugUnitTest`, `assembleDebug`, `assembleRelease` inklusive R8 erfolgreich.
4. App auf API 37 starten; Kernflow: Standort/Berechnung, Abfahrten, Settings, Detailsheet, WALK/BIKE und RoutePreview.
5. Keine Toolchain-, KSP-, Hilt-, Compose-, Class-/Linker-Regression.
6. Optional/empfohlen: Release-APK erneut durch `audit_16kb_artifact.py`, damit die abgeschlossene 16-KB-Invariante durch den AGP-9-Packagingwechsel nicht regressiert.

## Build 132 — Android 17 Phase 2: targetSdk 37

Scope:
- `versionCode = 1320`;
- `targetSdk 36 -> 37`; `compileSdk 37` und `minSdk 34` unverändert;
- AGP 9.4 / Gradle 9.6 / built-in Kotlin / KSP / Hilt unverändert;
- keine App-/Produktlogikänderung;
- keine CT-Ausnahme und keine vorsorgliche `ACCESS_LOCAL_NETWORK`-Permission.

Gate:
1. Gradle Sync sowie Debug-/Release-R8-Build erfolgreich.
2. API-37-Gerät/Emulator mit Target 37: Appstart, Standort/Suche, Abfahrten, Settings, Detailsheet, WALK/BIKE, Back, RoutePreview.
3. Reale HTTPS-Calls gegen abfahrt.now, HEIGIT ORS, Photon und Kartenressourcen ohne CT/TLS-Fehler.
4. Keine Permission-/LAN-, MessageQueue-, Reflection-, Class-/Linker- oder Native-Regression.
5. Große/resizable Darstellung mindestens per Emulatorprofil prüfen.
6. 16-KB-Audit/AAB-Alignment optional erneut als Packaging-Regressionstest.


## Build 133 — Kotlin 2.3 Warning Hygiene

Scope:
- `versionCode = 1330`;
- Android-17-Baseline unverändert: minSdk 34 / compileSdk 37 / targetSdk 37;
- `@ApplicationContext` an den zwei bekannten Constructor-Injection-Stellen explizit als `@param:`;
- ausschließlich die sechs vom Kotlin-Compiler als redundant gemeldeten `?.`/`!!` entfernen;
- keine Produkt-/UI-/Transit-/ORS-/Map-/Security-Änderung.

Gate:
1. Gradle/Kotlin-Build erfolgreich.
2. Die bekannte Annotation-Default-Target-Warning-Klasse erscheint nicht mehr.
3. Die sechs bekannten Nullability-Warnings erscheinen nicht mehr.
4. Kurzer Runtime-Smoke: Abfahrten, Search und RoutePreview.


## Build 134 — abfahrt.now Contract / `Station.walkSeconds` Observation

Scope:
- `versionCode = 1340`;
- Android-17-/16-KB-/Security-/Dependency-Baseline unverändert;
- `Station.walkSeconds: Int?` bildet das bereits dokumentierte optionale OpenAPI-Feld ab;
- ein kompakter `AbfahrtContract`-Logeintrag misst die reale Feldabdeckung;
- neue Gson-Unit-Tests sichern `timestamp` als Long/ms, `stop`, `stations[].distance`, `walkSeconds`, optionale Felder und unbekannte Forward-Compatibility-Felder;
- `walkSeconds` bleibt fachlich ungenutzt.

Gate:
1. `testDebugUnitTest` erfolgreich, insbesondere `AbfahrtApiContractTest`.
2. `assembleDebug` erfolgreich und weiterhin ohne Kotlin-Warnings.
3. Normaler Abfahrtsrefresh erzeugt `AbfahrtContract walkSeconds coverage=...`.
4. Keine sichtbare Verhaltensänderung der Liste/ORS-/RoutePreview-Pfade.
5. Die Runtime-Evidence entscheidet ausschließlich über Feldabdeckung; noch keine Optimierungsentscheidung.

## Build 135 — JVM Unit-Test Isolation / Test Hygiene

Scope:
- `versionCode = 1350`;
- keine Fachlogikänderung und keine Dependency-/SDK-Änderung;
- diagnostic-only `AbfahrtRepo`/`AbfahrtContract`-Logs in den reinen Repository-Hilfsfunktionen sind best-effort, damit Android-Framework-Log-Stubs lokale JVM-Tests nicht abbrechen;
- `LocaleParityTest` behandelt `File.parentFile` explizit nullable;
- `Station.walkSeconds` bleibt weiterhin rein beobachtend und beeinflusst weder Anzeige noch Reachability noch ORS.

Gate:
1. `:app:testDebugUnitTest :app:assembleDebug` vollständig erfolgreich.
2. Erwartung: alle 57 Unit-Tests grün.
3. Keine Kotlin-Testcompiler-Warnung in `LocaleParityTest.kt`.
4. Kurzer Runtime-Smoke genügt; Repository-Diagnostik muss auf Gerät weiterhin normal im Logcat erscheinen.

## Release-Grundsatz

Kein Build wird aufgrund einer Versionsnummer, eines Gradle-Syncs oder einer KI-Einschätzung als kompatibel gewertet. Acceptance benötigt die in `/doc/11-test-and-evidence.md` definierte reale Evidence.


## Build 136 — verpflichtender abfahrt.now-Key

Scope:
- `versionCode = 1360`; keine Dependency-/SDK-/ORS-/Map-Änderung.
- Departure-Access nur bei abgeschlossenem Onboarding **und** nichtleerem abfahrt.now-Key.
- Erststart ohne Key kann Schritt 1 nicht verlassen.
- bestehende keylose Installationen fallen erneut ins Onboarding; vorhandener ORS-Key wird vorbefüllt/erhalten.
- Navigation zu Departures erfolgt erst nach erfolgreicher verschlüsselter Speicherung des Pflicht-Keys.
- Abfahrt-Key in Settings nicht löschbar; Repository lehnt leeren Update-Wert defensiv ab.
- HTTP 401 setzt den Access-Status zurück und öffnet damit die Key-Korrektur; vorhandener Wert bleibt erhalten.

Gate:
1. `:app:testDebugUnitTest :app:assembleDebug` erfolgreich.
2. Frische Installation: Weiter-Button ohne abfahrt.now-Key deaktiviert; mit Key gelangt man über optionalen ORS-Schritt zu Abfahrten.
3. Settings: Abfahrt-Key kann geändert, nicht gelöscht werden; ORS weiterhin optional/löschbar.
4. Bestehende Installation ohne abfahrt.now-Key wird erneut zur Key-Eingabe geführt.
5. Falscher Key / HTTP 401 führt zurück zur Key-Korrektur, ohne Secret zu loggen.

## Build 137 — Photon-Nebenflow auslagern

Scope:
- `versionCode = 1370`; keine Dependency-/SDK-/API-/ORS-/Map-Änderung.
- Startseiten-Zahnrad wird durch `⋮` ersetzt; Menü enthält „Abfahrten an anderem Ort“ und „Einstellungen“.
- bestehende Photon-Stationssuche zieht unverändert auf die eigene Route `alternate_departures`.
- Startseite zeigt in Build 137 bewusst kein halbfertiges Routing-Suchfeld; Build 138 belegt denselben UI-Platz mit „Route planen – Ziel eingeben“.
- Header-/System-Back aus `alternate_departures` setzt den gemeinsamen ViewModel-State auf CurrentLocation/Idle zurück; Home lädt anschließend frisch.
- Pflicht-Key-Gate schützt auch die neue Route; HTTP 401 kann von dort weiter ins Onboarding führen.

Gate:
1. `:app:testDebugUnitTest :app:assembleDebug` erfolgreich.
2. Startseite: Refresh + Overflow-Menü; Einstellungen weiterhin erreichbar.
3. „Abfahrten an anderem Ort“ öffnet eigene Seite; Stationssuche lädt dort die gleichen alternativen Abfahrten wie zuvor.
4. Zurück führt zur Startseite und zeigt/lädt wieder den aktuellen Standort, ohne Alternate-Ergebnisse stehen zu lassen.
5. kein Fatal/ANR/Navigation-Crash.

## Build 138 — Route planen MVP

Implementiert wie im aktuellen Abschnitt oben beschrieben. Zusätzlich sind `sameVehicle`, Gleis/Verspätung/Cancelled und Empty-/Error-Basis bereits Teil des MVP; `intermediateStops` wird contract-sicher geparst.

## Build 139 — Route-Presentation-Cleanup

- technische Rohwerte im Route-UI bereinigt, Walk lokalisiert, Kontrast/Badges/Kartenhierarchie verbessert; real abgenommen.

## Build 140 — Route-Details / Transferklarheit

- `intermediateStops`/`stopNames` sichtbar auf-/zuklappbar; Transit-Umstiegsdauer zwischen Legs.

Build 138 compile history: der erste Versuch scheiterte an einem stale `promptText`-Symbol; der korrigierte Build wurde anschließend real gebaut und der Route-MVP runtime-seitig bestätigt.
