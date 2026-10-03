# Build 148 Specification — Raw Photon query + location bias

## Problem
Build-147 runtime evidence confirms that escape-hatch heuristics are still incomplete: `Potsdam` and `Hauptbahnhof` can remain global, but the multi-word municipality `Bad Saarow` is still rewritten to `Bad Saarow, Berlin`. Continuing to add PLZ/city/transit exceptions is not robust or intuitive.

## Scope
- remove current-city text rewriting from the existing general Photon place/route search;
- send the trimmed user query unchanged;
- keep existing `lat`/`lon` as Photon location bias;
- preserve Photon feature order in the general search instead of applying client text-score re-ranking;
- keep exact display de-duplication and the existing visible result limit;
- add debug evidence for query/bias and the first five Photon candidates;
- add focused JVM policy tests and converge `/doc`.

## Non-scope
The separate station-only “departures at another location” flow keeps its existing city/transit ranking. No second Photon request, no new dependency, no `/trips`, ORS, MapLibre, DataStore, route-sort, walking-navigation or localization change.
