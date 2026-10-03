# API Contracts

## 1. abfahrt.now

Produktionsbasis: `https://api.abfahrt.now/`

Primäre externe Vertragsreferenz im Projekt: [`/evidence/external-contracts/abfahrt-openapi-2026-09-12.yaml`](../evidence/external-contracts/abfahrt-openapi-2026-09-12.yaml).

### `/departures`

Die App nutzt:

- `lat`, `lon`
- `radius`
- `limit`
- `mode`
- `from`, `to`
- `dedup`
- `stops`

Authentifizierung erfolgt über den Header `x-api-key`. Ab Build 136 ist ein persönlicher abfahrt.now-API-Key **Produktvoraussetzung**: Der Abfahrts-Hauptflow darf ohne nichtleeren Key nicht betreten werden. Ein HTTP 401 führt zurück in die Key-Korrektur. Die vorhandene Keystore/AES-GCM-Persistenz bleibt unverändert. ORS ist davon unabhängig und weiterhin optional.

Für Build 122 sind insbesondere relevant:

- `Departure.line`
- `Departure.direction`
- `Departure.time`
- `Departure.timestamp` — Unix Millisekunden
- `Departure.stop` — lesbarer Stop-/Stationsname gemäß Vertrag
- `Departure.platform`
- `Departure.delay`
- `Departure.cancelled`
- `Departure.occupancy`
- `Departure.mode`
- `stations[].id`
- `stations[].name`
- `stations[].distance` — Luftlinie in Metern
- `stations[].lat` / `stations[].lon`
- optional `stations[].walkSeconds` — OSM-basierte Gehzeit in Sekunden

### Interne `stationDistance`

`Departure.stationDistance` ist ein **internes angereichertes App-Feld**, kein erforderliches externes Departure-Feld. `TransitRepository.enrichDeparturesWithStations()` ordnet eine Departure einer Station zu und übernimmt `Station.distance` als `stationDistance`. Damit sind HERE-Override, Luftlinien-Fallback und die bisherige Distanzpipeline mit der aktualisierten OpenAPI konsistent.

### Aktualisierte Contract-Bewertung

Die frühere DOC1-Annahme eines harten Contract-Gaps für `stationDistance` ist damit geschlossen. Die aktuelle OpenAPI passt in den für Build 122 verwendeten Kerndaten zum Code: `timestamp` ist Millisekunden, `stop` ist der Name und die Stationsdistanz liegt in `stations[].distance`.

Öffentliche Web-/PDF-Beispiele können weiterhin abweichen. Solche Abweichungen lösen keine Codeänderung auf Verdacht aus; bei Parser-/Schemaänderungen zählen aktuelle OpenAPI plus echte API-Antworten.

### Provider-Stop-IDs

Historische reale Direct-Stop-Antworten haben gezeigt, dass `Departure.stop` in einzelnen `stops=`-Antworten trotz Vertragsbeschreibung technische IDs wie `de:...::N` enthalten konnte. App-seitig bleibt deshalb der defensive Schutz bestehen:

- `Departure.stop`: nach erfolgreichem Enrichment lesbarer UI-/Matchingname.
- `Departure.providerStopId`: erhaltener technischer Rohwert.
- `Station.id`: Provider-ID aus Stationsdaten.
- `Departure.platform`: separates Plattformfeld, sofern geliefert.

Das `::N`-Suffix wird nicht ohne Mehrregionenbeleg als universelle Plattform-/Steig-Semantik verwendet. Die Build-122-Feldabnahme bestätigt, dass technische IDs in der sichtbaren Darstellung nicht mehr aufgefallen sind.

### Neue bzw. noch nicht produktiv genutzte Felder/Fähigkeiten

Die aktuelle OpenAPI enthält zusätzlich:

- `regionBounds`
- `routingBounds`
- `Station.walkSeconds`
- `/journey` für die Weiterfahrt/kommenden Stopps einer konkreten Abfahrt

Ab Build 134 wird `Station.walkSeconds` als optionales Feld im bestehenden `Station`-Modell **erfasst und beobachtet**, aber noch nicht für Anzeige, Reachability, Sortierung, ORS-Skip oder RoutePreview verwendet. Ein kompakter `AbfahrtContract`-Logeintrag misst die reale Feldabdeckung im initialen Stationssatz.

Das Feld bleibt bewusst Beobachtungsdaten: es liefert eine providerseitige OSM-Gehzeit, aber keine Gehwegdistanz, keine Fahrradzeit und keine Routengeometrie. Erst echte Laufzeit-Evidence entscheidet, ob und wo es später ORS-Matrix-Arbeit sinnvoll ergänzen oder reduzieren kann. Die übrigen neuen Felder/Fähigkeiten bleiben unverwendet.

## 2. `/trips`

Client- und Repository-Methode existieren bereits. Ab Build 138 ist `/trips` die Datenquelle des Route-planen-MVP. Der Flow startet über das Startseiten-Suchfeld, das via Photon ein Ziel liefert. `from_lat`, `from_lon`, `to_lat`, `to_lon` werden unverändert als Koordinaten an abfahrt.now übergeben; der Server bestimmt die passenden ÖPNV-Haltestellen.

Das Modell bildet ab Build 138 zusätzlich die optionalen Contract-Felder `stopNames`, `intermediateStops` und `sameVehicle` ab. `sameVehicle=true` bedeutet ausdrücklich **kein Umstieg**; die UI zeigt „im Fahrzeug bleiben“. `intermediateStops` wird sicher geparst; Build 140 stellt reale Zwischenhalte inline expandierbar dar und zeigt vorhandene Ankunftszeiten. Count-only-Legs bleiben nicht künstlich expandierbar. HTTP 401 aus `/trips` unterliegt demselben Pflicht-Key-Gate wie `/departures`.

ORS bleibt für Geh-/Radwege und die bestehende Detail-Routenvorschau zuständig; eine Transitstrecke darf nicht als künstliche ORS-Straßengeometrie dargestellt werden.

## 3. `/journey`

Die aktualisierte OpenAPI bietet `/journey` für die verbleibenden Stopps einer konkreten Abfahrt. Auch im aktuellen Build 149 existiert dafür noch kein Client-/UI-Pfad. Die vorhandenen Detailsheet-Folgezeiten sind etwas anderes: Sie zeigen weitere Abfahrtszeiten derselben Linie/Richtung aus dem lokalen Rohdatenhorizont.

## 4. Photon

Basis: `https://photon.komoot.io/`

Photon dient der Suche/Geokodierung. Bis Build 136 priorisiert der bestehende Alternativstandort-Flow Stations-/Haltestellentreffer. Ab Build 137 wird dieser Flow auf eine eigene Seite verschoben; ab Build 138 nutzt die Startseite Photon als allgemeine Zielsuche für Routen (Adressen, Orte, POIs und Haltestellen).

Ab Build 148 gilt für die **allgemeine** Zielsuche: `q` ist der getrimmte Nutzereingabetext ohne angehängte City/PLZ-Heuristik; `lat`/`lon` dienen ausschließlich als weicher Standort-Bias. Die Reihenfolge der Photon-Features wird erhalten, abgesehen von der Entfernung exakter Anzeige-Duplikate. Die separate Stationssuche für „Abfahrten an anderem Ort“ bleibt fachlich getrennt und darf weiterhin transit-spezifisch ranken.

## 5. OpenRouteService

Ab Build 123 verwendet die App die offizielle HEIGIT-Basis:

`https://api.heigit.org/openrouteservice/`

Die bestehenden relativen Pfade bleiben unverändert:

- Matrix: `v2/matrix/foot-walking` bzw. `v2/matrix/cycling-regular`
- Directions/GeoJSON: `v2/directions/foot-walking/geojson` bzw. `v2/directions/cycling-regular/geojson`

Damit entstehen die von ORS dokumentierten Zielpfade unter `https://api.heigit.org/openrouteservice/v2/...`. Authentifizierung, Request-Bodies, WALK/BIKE-Profile, Cache-, Cooldown- und Fallback-Verhalten wurden in Build 123 nicht verändert.

Der frühere Host `api.openrouteservice.org` bleibt ausschließlich historische Evidence und darf im Runtime-Code nicht mehr verwendet werden.

## Quellen / Evidence

- Aktuelle Projekt-OpenAPI: [`/evidence/external-contracts/abfahrt-openapi-2026-09-12.yaml`](../evidence/external-contracts/abfahrt-openapi-2026-09-12.yaml)
- Contract-Konvergenz: [`/evidence/external-contracts/2026-09-12_openapi-convergence.md`](../evidence/external-contracts/2026-09-12_openapi-convergence.md)
- Historische Eingabe-OpenAPI/PDF bleiben unter `/evidence/legacy-docs/`.
- ORS-Migrationsankündigung: <https://ask.openrouteservice.org/t/deprecating-api-openrouteservice-org-in-favour-of-api-heigit-org/7912>
- ORS-Quotenreduktion/Abschaltung Alt-Host: <https://ask.openrouteservice.org/t/reducing-the-quota-of-deprecated-api-api-openrouteservice-org/8013>

## 6. Linienfarben

Zielvertrag für eine spätere datengetriebene Lösung:

- `routeColor`
- `routeTextColor`

Bevorzugt aus GTFS `route_color` / `route_text_color` oder providerseitig gleichwertiger normalisierter Quelle. Keine europaweiten Annahmen aus einem lokalen VBB-Mapping ableiten.
