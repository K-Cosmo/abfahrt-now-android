# Build 123 Specification — ORS Endpoint Migration

## Problem
Build 122 verwendet als OpenRouteService-Basis noch `https://api.openrouteservice.org/`. Dieser Host ist seit 28.04.2026 zugunsten von `api.heigit.org` deprecated; ORS hat für den Alt-Host die Quote reduziert und die Abschaltung für 28.09.2026 angekündigt.

## Ziel
Die vorhandene ORS-Integration unverändert auf den offiziellen HEIGIT-Endpunkt migrieren:

`https://api.heigit.org/openrouteservice/`

Die bestehenden relativen Pfade für Matrix und Directions bleiben unverändert.

## Nicht-Ziele
- keine Änderung an ORS-Cache, Cooldown oder Retry;
- keine Änderung an WALK/BIKE-Profilen;
- keine Änderung an Reachability;
- keine Änderung an Dedup, Sortierung oder Stable-Merge;
- keine UI-Änderung;
- keine Integration von `Station.walkSeconds`;
- keine neue Dependency.

## Acceptance Criteria
1. Retrofit verwendet `https://api.heigit.org/openrouteservice/` als ORS-Base-URL.
2. Im Runtime-Code existiert kein Verweis mehr auf `api.openrouteservice.org`.
3. Die vorhandenen relativen Matrix-Pfade ergeben `.../openrouteservice/v2/matrix/{profile}`.
4. Die vorhandenen relativen Directions-Pfade ergeben `.../openrouteservice/v2/directions/{profile}/geojson`.
5. `versionCode = 1230`, `versionName = 1.1.0`.
6. Alle statischen Projektchecks bleiben grün.
7. Reale Matrix-/Directions-Abnahme mit einem gültigen ORS-Key bleibt als Device-/Runtime-Evidence erforderlich, falls sie in der Buildumgebung nicht ausführbar ist.
