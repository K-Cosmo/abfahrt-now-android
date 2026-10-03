## Build 149 — HERE detail location map

- Bumps `versionCode` to 1490.
- HERE departures never load ORS route-preview geometry.
- Replaces the contradictory HERE walking polyline with a MapLibre marker-only location map for query origin + stop.
- HERE map does not depend on an ORS key; non-HERE route preview remains unchanged.
- Adds focused policy tests and the HERE-map title across all locale sets.
- Converges Build-148 real acceptance evidence and leaves startup performance untouched for the next measurement build.

## Build 148 — source-overlay compile correction

- Keeps `versionCode 1480`; no product/runtime behavior change.
- Real first Android Studio gate exposed a stale Build-147 test file when the source archive was copied over an existing project instead of replacing it.
- Retains the legacy `GeocodingLocalityScopeTest.kt` path but updates its assertions to D-069 / `placeQueryForPhoton()`, so overlay installs overwrite the obsolete test instead of leaving it behind.
- The former renamed `GeocodingQueryPolicyTest.kt` is removed from the clean source package; a pre-existing copy is harmless if an earlier Build-148 archive was already overlaid.

## Build 148 — raw Photon query + location-bias ranking

- Removes current-city text rewriting from the general Photon place/route search.
- Sends the trimmed user query unchanged and keeps the existing Photon `lat`/`lon` location bias.
- Removes the general-search client re-ranking so Photon result order is preserved; only exact display duplicates are removed.
- Adds debug evidence for the raw query and the first five Photon candidates (name/city/state/OSM type).
- Leaves the separate station-only “departures at another location” search and its transit ranking unchanged.
- No dependency, `/trips`, ORS, MapLibre, DataStore, route-sort or localization change.

## Build 147 — Photon locality-scope correction

- Fixes the over-restrictive Build-146 city appending for one-token remote places and explicit transit-qualified location searches.
- Adds focused tests and converges `/doc` status drift.
- No dependency, routing, ORS, map, persistence or localization change.

## Build 146 — routing UX/local-search convergence

- Centers route-leg times below the existing mode/line column and aligns route content to a single axis.
- Adds navigation actions to the first/final walking legs via implicit Android intents.
- Hides Home/Work quick destinations immediately when typing starts.
- Scopes general Photon search to the current city unless the query includes a postcode or explicit locality.
- Adds focused JVM tests; no dependency or routing-contract change.

## Build 145 — route sorting + leg-header polish

- Adds four local route-result sort modes: earliest departure (default), fastest, fewest transfers and least walking.
- Sorting uses only the current `/trips` response; no additional backend call or provider-ranking mutation.
- "Least walking" intentionally sorts by summed walking-leg duration because the current trip contract exposes leg times, not walking distance.
- Refines the existing emoji-over-line-badge route header so direction/walk duration aligns with the badge instead of floating between emoji and badge.
- Suppresses transit delay/platform metadata on walking legs and indents secondary leg details away from the time column.
- Adds focused JVM tests and all four labels to all 22 locale sets.
- No dependency, API, repository, DataStore, ORS, MapLibre, departure-sort/dedup or authentication change.

## Build 144 — Saved destinations on the Home route search

- Starts exactly from the user-supplied corrected Build 143 source package.
- Configured Home/Work are now shown as immediate quick destinations when the start-page route field gains focus and no query has been typed yet.
- Selecting either shortcut opens the existing RoutePlanner directly with Current location as origin and the saved place as destination.
- Normal Photon destination search remains unchanged as soon as the user types.
- No new strings, dependency, persistence format, `/trips` behavior or route ranking.
- Route sorting is deliberately shifted to Build 145 so this user-requested navigation shortcut remains an isolated change.

## Build 143 compile correction

- Fixes the missing `onOpenSavedPlaces` callback plumbing between `DepartureScreen` and its private header composable.
- Keeps `versionCode 1430`; no functional or dependency change.

## Build 143 — Saved places + route mode-column convergence

- Adds exactly two local saved-place slots: Home and Work, managed from the existing start-page overflow menu.
- Reuses the existing Photon general place search; stores title/subtitle/latitude/longitude in the existing DataStore.
- Saved Home/Work become quick choices for both RoutePlanner From and To.
- RoutePlanner legs now stack transport emoji above the line/walk badge in a fixed left column, matching the main departure-card visual grammar.
- All new strings added across the existing locale sets.
- No route/API/ORS/MapLibre/dependency change; route sorting follows in Build 145 after the Build-144 Home shortcut refinement.

## Build 142 — Route UI convergence

- Removes provider region from visible RoutePlanner UI while retaining it internally.
- Reuses existing transport-mode emoji in transit route legs.
- No routing/API/dependency/localization behavior change.

## Build 141 — RoutePlanner empty/error convergence

- Adds retry-capable route empty/error cards.
- Keeps provider region visible for empty route responses and renders optional trip attribution.
- Adds structured non-PII route outcome logging for success/empty/error evidence.
- Does not infer unsupported coverage errors or fabricate transit geometry.

## Build 140 resource correction

- Corrects two Klingon route-detail strings whose apostrophes were not escaped for Android AAPT.
- No behavior/versionCode change; Build 140 must be rebuilt.

## Build 140 — route details / transfer clarity

- Shows a localized transfer duration between consecutive transit legs, without treating access/egress walking as an extra transfer.
- Makes provider-supplied intermediate stops expandable inline, with arrival times where available.
- Adds the new detail/accessibility strings to all 22 locale resource sets.
- No API schema, dependency or routing architecture change.

## Build 139 — route presentation cleanup

- Localizes walking legs in the route planner using the existing `ors_mode_walk` resource.
- Hides technical route endpoint strings (coordinates/provider-like IDs) in the UI where a user-facing label is known.
- Reworks route cards toward a cleaner map-style presentation with better contrast and line badges.
- Adds JVM tests for the new route display helpers.
- No API/repository schema or dependency change; this is a view-layer polish build.

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

## Build 135
- Restores local JVM testability for repository diagnostics without changing product logic.
- Fixes nullable `parentFile` test warning.
- Keeps `walkSeconds` observation-only.

# Changelog redirect

Die normative Build-Historie liegt ausschließlich unter [`doc/CHANGELOG.md`](doc/CHANGELOG.md).

Diese Root-Datei ist nur ein nicht normativer Einstieg und darf nicht separat gepflegt werden.
