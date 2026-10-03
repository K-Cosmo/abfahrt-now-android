# Build 145 specification

## Goal
Make route alternatives easier to compare and improve the leg header hierarchy without changing route acquisition.

## Functional requirements
- Default sort: earliest departure.
- Alternatives: fastest total duration, fewest transfers, least walking.
- Sorting is local over the current `/trips` response and deterministic.
- Least walking uses walking-leg time as the available proxy; do not claim physical walking distance.
- Preserve the accepted emoji-over-line-badge grammar but align route direction/duration with the badge baseline.
- Hide transit-style delay/platform metadata on walking legs.
- All new user-facing sort labels must exist in all 22 locale resource sets.

## Non-goals
- no backend ranking parameter;
- no time selection;
- no route filtering;
- no transit map geometry;
- no new dependency or architecture layer.
