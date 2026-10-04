# Build 155 Acceptance Evidence — 04.10.2026

## Scope

Build 155 (`versionCode = 1550`, `versionName = 1.1.0`) führt eine persistente, wählbare Sortierung der Abfahrtsseite ein.

Profile:

- `NEARBY`: Entfernung → Abfahrtszeit → Richtung → Linie (Default)
- `SOONEST`: Abfahrtszeit → Entfernung → Richtung → Linie
- `LINE_GROUPED`: Entfernung → Linie → Abfahrtszeit → Richtung

`DepartureDisplayOrdering` bleibt Single Source of Truth. Ein Profilwechsel darf nur lokal refiltern und keinen eigenen Core-/ORS-/Location-Refresh oder Loading-State erzeugen.

## Automated Evidence

Android CI #125 auf Runtime-Head `02ac2a698150c3cb9611ec700f68c8a00ffc0513` ist vollständig grün:

- Locale/Governance/Compatibility
- committed Gradle wrapper
- Unit Tests
- Debug build
- Release/R8 build

Die Unit-Tests decken die drei Profile, Default/Fallback und die HERE-/SOONEST-Semantik ab.

## Real-device Evidence

### UI

Ein Realgeräte-Screenshot bestätigt die neue Sortiersektion direkt nach „Abfahrten pro Richtung“ mit drei Radio-Optionen. Der Nutzer bewertet die Darstellung als passend.

### Fachliches Verhalten

Der Nutzer bestätigt, dass die Abfahrtsseite nach Auswahl jeweils entsprechend sortiert.

Im bereitgestellten Logcat sind lokale Refilter-Ergebnisse sichtbar:

- `sort=SOONEST`
- `sort=LINE_GROUPED`
- `sort=NEARBY`

Es erscheint im aufgezeichneten Sortier-Smoke kein `departure_state_loading`.

Die im Log vorhandenen späteren `/departures`-Requests gehören zum normalen Stable-Refresh und sind nicht unmittelbar an einen Sortier-Tap gekoppelt. Beim Stable-Refresh werden vorhandene Same-Origin-ORS-Metriken weiterverwendet; es entsteht kein zusätzlicher ORS-Zyklus allein wegen eines Sortierwechsels.

### Persistenz

Nach `force-stop` und Neustart bleibt das gewählte Sortierprofil erhalten. Dies wurde vom Nutzer real bestätigt.

### Regressionen

Im Abnahmeumfang wurden keine FATAL-/ANR-/Navigation-/Settings-Regressionen gemeldet oder beobachtet. Die Build-154-Location-/First-Paint-Architektur wird durch Build 155 nicht fachlich verändert.

## Acceptance

Build 155 ist fachlich und technisch accepted.

F-SORT-001 / B-155-001 ist geschlossen.

Build 155 ist der Release-Kandidat für RELEASE1 / `v1.1.0-b155`.