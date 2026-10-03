# Build 144 specification — Saved destinations on the Home route search

## Goal
Make the two already-configured saved places available one interaction earlier, directly from the existing start-page route field, without changing routing or search semantics.

## Functional requirements
- Baseline is the user-supplied corrected Build-143 source package, not the earlier pre-fix package.
- When `Route planen – Ziel eingeben` gains focus while still empty, configured Home and/or Work are shown immediately.
- Only configured slots are shown.
- Selecting Home/Work opens the existing RoutePlanner directly with Current location as origin and the saved place as destination.
- The stored title/subtitle/latitude/longitude are reused directly; no Photon request is needed for the shortcut.
- As soon as text is entered, the existing general Photon destination search behaves exactly as before.
- Existing RoutePlanner Home/Work quick selections remain unchanged.
- Existing 22-locale key parity remains green; no new user-facing string is required.

## Non-goals
- no arbitrary favorites/history;
- no new DataStore fields or migration;
- no route sorting in this build (shifted to Build 145);
- no time selection (Build 146, blocked by `/trips` contract);
- no API, ORS, MapLibre or dependency change.
