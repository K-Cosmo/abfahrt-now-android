# Build 138 Specification — Route-planen-MVP

## Ziel
Das vorhandene Startseiten-Suchfeld wird zum direkten Einstieg in die Routenplanung. Ein ausgewähltes allgemeines Photon-Ziel öffnet ohne zusätzlichen Zwischenschritt einen RoutePlanner, der Verbindungen über abfahrt.now `/trips` lädt.

## Muss
- Startseite zeigt „Route planen – Ziel eingeben“.
- Photon-Zielsuche akzeptiert Adresse, Ort, POI und Haltestelle; sie darf nicht auf Transit-Treffer beschränkt werden.
- Auswahl navigiert direkt zum Planner.
- Default `Von = aktueller Standort`, `Nach = gewähltes Ziel`.
- Von/Nach änderbar, aktueller Standort auswählbar, Endpunkte tauschbar.
- `/trips` erst nach explizitem „Route finden“.
- Ergebnis zeigt Trip-Zeit, Dauer, Umstiege und Legs mit Linie/Richtung, Ein-/Ausstieg, Zeit, Verspätung und Abfahrtsgleis.
- `sameVehicle=true` wird als Durchbindung ohne Umstieg dargestellt.
- `stopNames` und `intermediateStops` werden contract-sicher geparst; vollständige Zwischenhalte-UI ist nicht Build-138-Scope.
- HTTP 401 aus `/trips` benutzt den bestehenden Pflicht-Key-Flow.
- alle neuen User-facing Strings in allen 22 Resource-Sets.

## Nicht-Ziele
- keine Transitkarte/Transitgeometrie via ORS.
- keine Abfahrts-/Ankunftszeitwahl, solange `/trips` dafür keinen Contract besitzt.
- keine Änderung an Departure-Sortierung, Dedup, ORS-WALK/BIKE oder MapLibre.
- keine neue Dependency.
