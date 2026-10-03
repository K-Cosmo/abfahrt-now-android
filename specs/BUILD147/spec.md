# Build 147 Specification — Photon locality-scope correction

## Problem
Build-146 runtime evidence shows that current-city appending rewrites `Potsdam` to `Potsdam, Berlin` and `S Potsdam` to `S Potsdam, Berlin`. Valid remote-place/station searches become too restrictive.

## Scope
- keep current-city default for multi-word unqualified general-place queries;
- bypass city appending for one-token queries and explicit transit qualifiers;
- retain existing PLZ/comma/current-city overrides;
- add focused unit tests;
- converge `/doc` acceptance status drift found during Build-146 audit.

## Non-scope
No second Photon request, no dependency, no ranking rewrite, no `/trips`, ORS, MapLibre, DataStore, departure-filter or localization change.
