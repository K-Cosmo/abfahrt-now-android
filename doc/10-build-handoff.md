# Build-Handoff

## Aktuell: v1.1.0 Build 149 (`versionCode 1490`) — HERE location map

Basis: der korrigierte Build 148 ist real abgenommen. `:app:testDebugUnitTest :app:assembleDebug` lief mit `BUILD SUCCESSFUL in 1s`; Runtime-Evidence bestätigt D-069 mit raw Photon query + `lat/lon`-Bias und sinnvoller Provider-Reihenfolge (`Bad Saarow`, `Potsdam`, `Bad Belzig`). Nutzerfeedback bestätigt das Ranking.

Neue Feld-Evidence vom 2026-09-23 zeigt AB-053: Ein Departure mit `Entfernung: Hier` und `Haltestelle erreicht` rendert darunter dennoch `Routenkarte` und eine blaue ORS-Polyline.

### Build-149-Scope
- bestehender HERE-Threshold <=35 m bleibt unverändert und zentral über `Departure.isHereOverride()`.
- HERE ruft `loadRoutePreview()` nicht mehr auf und erzeugt keine ORS-Route/Polyline.
- HERE zeigt bei vorhandenem Query-Origin und Stationskoordinaten eine MapLibre-Standortkarte mit zwei Markern; sie ist nicht vom ORS-Key abhängig.
- für Alternativort-Abfahrten ist der gewählte Suchort der Detailkarten-Origin.
- Nicht-HERE-RoutePreview bleibt unverändert.
- neuer Locale-Key `detail_here_map_title` in allen 22 Resource-Sets.
- fokussierter JVM-Policy-Test schützt „HERE lädt nie RoutePreview“.
- keine neue Dependency/API-/Persistence-/Sortier-/Photon-Änderung.
- AB-018 Startup/Main-Thread-Jank bleibt getrennt und folgt erst nach Build-149-Abnahme mit Messinstrumentierung.

### Acceptance
```text
:app:testDebugUnitTest :app:assembleDebug
```
Runtime: mindestens einen echten HERE-Fall öffnen. Erwartung: `Hier` + erreicht, Überschrift `Standort`, Markerkarte ohne blaue Linie; Log `state=Hidden reason=here_no_route`; keine ORS route-preview request für diesen Tap. Danach einen Nicht-HERE-Fall öffnen und normale RoutePreview als Regression-Smoke bestätigen.

## Frühere Handoffs
## Historisch: v1.1.0 Build 146 (`versionCode 1460`) — Layout/Navigation grün, Locality-Scope superseded

Basis: Build 145 ist real grün (`:app:testDebugUnitTest :app:assembleDebug`) und die vier lokalen Routensortierungen sind funktional angekommen. Der Build-145-Logcat zeigt erfolgreiche `/trips`-200-Antworten im getesteten Flow.

### Build-146-Scope
- Zeitspalte in Route-Legs an Symbol/Linienbadge ausgerichtet; Uhrzeiten zentriert, Inhalt konsistent eingerückt.
- erster und letzter Geh-Leg erhalten eine obere rechte Navigation-Aktion; kein Navigation-SDK, impliziter Intent + Karten-Fallback.
- gespeicherte Orte nur als Schnellwahl bei leerem Suchfeld; beim Tippen sofort ausgeblendet.
- Photon-Ortssuche erhält standardmäßig `current city` aus `DepartureResponse.city`, solange keine PLZ oder explizite Komma-Ortsangabe vorliegt.
- explizite Fremdorts-Suche bleibt z. B. über `28195 ...` oder `Hauptbahnhof, Bremen` möglich.
- keine Änderung an `/trips`, Routing-Sortierung, ORS, MapLibre, DataStore oder Dependencies.

### Acceptance
```text
:app:testDebugUnitTest :app:assembleDebug
```
Runtime: Route mit erstem/letztem Fußweg; beide Navigation-Icons öffnen; Uhrzeiten optisch unter der Modusspalte; Zuhause/Arbeit verschwinden beim ersten Zeichen; `Hauptbahnhof` liefert lokale City, PLZ/`Ort, Stadt` überschreibt.

## Frühere Handoffs

### Historisch: v1.1.0 Build 143 (`versionCode 1430`) — accepted

Verbindliche korrigierte Baseline: Nutzer-Upload `SHA-256 6c95246b6782ecd385a011616abeff8d650b0014a31fe7c0fcb774670b53825a`. Der reale Gate `:app:testDebugUnitTest :app:assembleDebug` lief mit `BUILD SUCCESSFUL in 22s`; Gespeicherte Orte wurden funktional bestätigt. Die frühere Compile-Korrektur für `onOpenSavedPlaces` ist Bestandteil dieser Baseline.

### Historisch: v1.1.0 Build 142 (`versionCode 1420`)

Basis: Build 141 ist abgenommen. Der kombinierte Gradle-Gate lief mit `BUILD SUCCESSFUL in 6s`; Runtime blieb im getesteten Flow unauffällig. Nutzerfeedback: Provider-Region hat auf der Routingseite keinen Mehrwert; Transportmodus-Symbole aus der Abfahrtsansicht sollen dort ebenfalls verwendet werden.

### Build-142-Scope
- sichtbare `TripResponse.region` aus Success- und Empty-State entfernt; Modell/Logging bleiben unverändert.
- Nicht-Geh-Legs zeigen das bestehende `TransportMode.emoji` vor dem Linien-Badge.
- Attribution bleibt sichtbar, falls geliefert.
- kein neuer String, keine Dependency, keine API-/Repository-/Routing-/ORS-/MapLibre-Änderung.
- Build 143 bleibt für gespeicherte Orte `Zuhause`/`Arbeit`; Build 144 für lokale Routensortierung; Zeitwahl bleibt bis zu einer `/trips`-API-Erweiterung blockiert.

### Acceptance
```text
:app:testDebugUnitTest :app:assembleDebug
```
Runtime: normale Route öffnen; Region darf nicht mehr sichtbar sein; bei Transit-Legs müssen vorhandene U/S/Tram/Bus/Regional/Express/Ferry-Symbole konsistent zur Abfahrtsansicht erscheinen.

## Frühere Handoffs



### Historisch: v1.1.0 Build 141 (`versionCode 1410`)

Basis: Build 140 ist abgenommen. Der kombinierte Gradle-Gate lief in 2 s grün; die reale `/trips`-Route lieferte Berlin/Brandenburg mit sechs Optionen. Der Screenshot bestätigt `4 min Umstieg`, keinen künstlichen Walking-Umstieg und aufklappbare Zwischenhalte.

### Build-141-Scope
- RoutePlanner Empty/Error wird als klare Statuskarte mit vorhandenem lokalisiertem Retry dargestellt.
- ein leeres 200-Ergebnis behält die Provider-Region sichtbar, sofern vorhanden.
- optionale `TripResponse.attribution` wird angezeigt.
- `AbfahrtTrips` protokolliert `success`, `empty`, `error` plus Region/Count bzw. Fehlerklasse/HTTP-Code, aber keine gesuchten Ortsnamen.
- bewusst keine heuristische „außerhalb der Abdeckung“-Meldung aus leerem Ergebnis oder HTTP 400.
- keine neue Dependency, kein API-/Modell-Schemawechsel, kein Ranking, keine Transitgeometrie.

### Acceptance
```text
:app:testDebugUnitTest :app:assembleDebug
```
Danach normaler Success-Smoke plus — wenn praktikabel — Empty/Error mit sichtbarem „Erneut versuchen“.

## Frühere Handoffs

### Historisch: v1.1.0 Build 140 (`versionCode 1400`)

Basis: Build 139 ist für seinen isolierten Route-Presentation-Scope abgenommen. Der reale kombinierte Gradle-Gate war grün; der deutsche Dark-Theme-Screenshot zeigt lokalisierte `Zu Fuß`-Segmente, keine sichtbaren technischen Koordinaten und die gewünschte freundlichere Kartenhierarchie. Im mitgelieferten Logcat wurde keine App-FATAL-/Linker-/ANR-Signatur gefunden; zwei `/departures`-Timeouts liefen durch die bestehende Retry-Logik und liegen außerhalb des Build-139-Scope.

### Build-140-Scope
- reale Transit-zu-Transit-Wartezeit wird als lokalisierter Umstieg zwischen Leg-Karten angezeigt.
- Walking-Zugang/Abgang erzeugt keinen zusätzlichen visuellen Umstieg; `sameVehicle` bleibt separat.
- `intermediateStops`/`stopNames` werden auf Wunsch inline aufgeklappt; Zeiten nur, wenn vorhanden.
- Count-only bleibt sichtbar, aber nicht künstlich expandierbar.
- drei neue Route-Detail-Strings in allen 22 Locale-Paketen.
- keine neue Dependency, kein API-/Modell-Schemawechsel, keine Änderung an Routing, Departure-Sortierung, ORS/MapLibre oder Pflicht-Key.

### Acceptance
```text
:app:testDebugUnitTest :app:assembleDebug
```
Runtime: Route mit echtem Transit-Umstieg und Zwischenhalten; Transferdauer plausibel; Fußwege zählen nicht als Umstieg; Expand/Collapse funktioniert; Locale-Smoke.

### Compile correction
Der erste reale Build-140-Gate scheiterte ausschließlich in `:app:mergeDebugResources`: zwei neue Klingon-Strings enthielten nicht maskierte ASCII-Apostrophe. Beide Ressourcen sind im korrigierten Build 140 mit `\'` maskiert. Kotlin-Kompilation war in diesem Lauf bereits erfolgreich; der kombinierte Gradle-Gate muss dennoch vollständig erneut laufen.

## Frühere Handoffs



### Historisch: v1.1.0 Build 138 (`versionCode 1380`)

Basis: Build 137 korrigiert und real gebaut. Der kombinierte Gate `:app:testDebugUnitTest :app:assembleDebug` war erfolgreich; einzig ein nicht-blockierender Compose-Icon-Deprecation-Warning (`Icons.Filled.ArrowBack`) blieb und ist in Build 138 auf AutoMirrored umgestellt.

### Build-138-Scope
- Startseitensuche ist wieder sichtbar und heißt „Route planen – Ziel eingeben“.
- allgemeine Photon-Zielsuche; kein Stations-only-Ranking im Route-Flow.
- Zielauswahl navigiert direkt nach `route_planner`.
- neuer `RoutePlannerViewModel` hält ausschließlich Route-/Search-State und nutzt bestehende `GeocodingRepository`-/`TransitRepository`-Schichten.
- RoutePlanner: Von/Nach, aktueller Standort, Tausch, „Route finden“.
- `/trips`-Ergebnis-MVP zeigt Zeit, Dauer, Umstiege und Legs; `sameVehicle` ist Durchbindung.
- OpenAPI-Felder `stopNames`, `intermediateStops`, `sameVehicle` im Modell.
- alle neuen Texte in 22 Locale-Paketen.
- keine neue Dependency; ORS/MapLibre/Departure-Sortierung unverändert.

### Acceptance
```text
:app:testDebugUnitTest :app:assembleDebug
```
Danach Runtime-Smoke: Zielsuche auf Home, direkter Planner, Route finden, Start/Ziel editieren/tauschen, Back, Empty/Error und 401.

# Build Handoff — Runtime Build 137

## Baseline
- v1.1.0 / `versionCode = 1370`
- minSdk 34 / compileSdk 37 / targetSdk 37
- Android-17-, 16-KB-, Keystore- und Pflicht-Key-Baseline unverändert

## Build 136 evidence
- Pflicht-Key beim Erststart real bestätigt: ohne abfahrt.now-Key kein Weiter.
- normaler App-Start nach Key-Eingabe bestätigt.
- mitgelieferter Emulator-Log enthält Standort-/Systemrauschen, aber keinen beobachteten app-seitigen Fatal-/Linker-Crash.
- Settings-Delete und gezielter 401-Pfad wurden in diesem Lauf nicht separat provoziert und bleiben Regressionstests.

## Build 137 scope
- bestehende „Abfahrten an anderem Ort“-Photon-Suche aus der Startseite ausgelagert.
- Startseite: Refresh bleibt direkt; `⋮` enthält „Abfahrten an anderem Ort“ und „Einstellungen“.
- eigene geschützte Route `alternate_departures` mit bestehender Stationssuche und bestehender Departure-Darstellung.
- kein neuer Photon-Modus, keine Änderung an Stationsranking/API-Aufruf/Departure-Filterung.
- Back aus dem Nebenflow setzt gemeinsamen Search/UI-State auf CurrentLocation/Idle; Home lädt frisch.
- Startseiten-Routing-Suche folgt erst in Build 138, damit Build 137 keinen halbfertigen Planner zeigt.

## Acceptance
In Android Studio:

```text
:app:testDebugUnitTest :app:assembleDebug
```

Runtime:
1. Startseite zeigt Refresh und Overflow-Menü; Settings funktionieren.
2. Menüpunkt „Abfahrten an anderem Ort“ öffnet die neue Seite.
3. dort Station suchen/auswählen; alternative Abfahrten müssen wie bisher laden.
4. Header-Back und System-Back prüfen: Rückkehr zur Startseite, danach aktuelle Standort-Abfahrten statt Alternate-Daten.
5. kein Fatal/ANR/Navigation-Crash.

## Danach
- Build 138: Startseitensuche „Route planen – Ziel eingeben“, allgemeine Photon-Ziele, direkter RoutePlanner und `/trips`-MVP.
- Build 139: Route-Details/Feinschliff.

## Build 137 compile correction
The first Build 137 source package failed Kotlin compilation because `DepartureScreen` was public while its `DepartureScreenMode` parameter was internal. The corrected package makes `DepartureScreen` internal. Re-run `:app:testDebugUnitTest :app:assembleDebug` as the acceptance gate.

Compile correction: the first real Build 138 compile exposed one stale `promptText` reference in the alternate-location empty-state. Corrected in-place to `stringResource(R.string.search_station_prompt)`; product behavior unchanged. Re-run `:app:testDebugUnitTest :app:assembleDebug` before acceptance.
