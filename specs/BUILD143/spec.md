# Build 143 specification — Saved places + route mode-column convergence

## Goal
Provide two deliberately small routing conveniences without changing routing semantics:

1. store exactly two user-managed local places: **Home** and **Work**;
2. make RoutePlanner leg identity use the same calm vertical visual grammar as the main departure cards: mode symbol above line badge in a fixed left column.

## Functional requirements
- Start-page overflow menu gains `Saved places`.
- Saved places screen exposes exactly two slots: Home and Work.
- Each slot can be set/changed using the existing general Photon place search and can be removed.
- Stored data is title, subtitle, latitude and longitude in the existing app DataStore; no cloud/account sync and no new dependency.
- Existing data-extraction rules keep the DataStore excluded from cloud backup/device transfer.
- RoutePlanner origin/destination pickers offer configured Home/Work as quick choices alongside Current location.
- Choosing a saved place only selects coordinates; `/trips` request behavior remains unchanged.
- Transit and walking legs use a fixed left mode column: emoji above line/walk badge; direction remains in the content column.
- All new user-facing strings exist in every existing locale resource set.

## Non-goals
- no arbitrary favorites list;
- no history/recent places;
- no cloud sync;
- no route sorting yet (Build 144);
- no time selection until `/trips` supports it;
- no API/dependency/ORS/MapLibre changes.
