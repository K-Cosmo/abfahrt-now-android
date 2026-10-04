# Build 153 — sanitized acceptance evidence — 2026-10-04

Status: **accepted** for the Build-153 measurement/instrumentation scope.

This public evidence summary intentionally omits precise coordinates, device identifiers, local paths and secrets. Raw logcats remain local/private per `/doc/11-test-and-evidence.md`.

## Automated gate

- Android CI #94: success.
- Static/Governance/Compatibility: success.
- committed Gradle wrapper verification: success.
- JVM unit tests: success.
- Debug build: success.
- Release/R8 build: success.

## Real-device runtime evidence

Three clean, fully instrumented cold starts were evaluated on an already configured device with location permission granted.

| Phase | Cold 1 | Cold 2 | Cold 3 / final |
|---|---:|---:|---:|
| Compose first frame | ~436 ms | ~455 ms | ~520 ms |
| AccessGate ready | ~671 ms | ~705 ms | ~768 ms |
| Departure Loading | ~733 ms | ~753 ms | ~829 ms |
| First abfahrt.now Core request | ~3.45 s | ~3.34 s | ~3.85 s |
| Loading → first Core request | ~2.72 s | ~2.59 s | ~3.02 s |

The final clean run additionally showed:
- Application/MapLibre startup around 27 ms total;
- first Core main response HTTP 200 in about 629 ms;
- later add-on network timings varied, but occurred after the already identified initial wait block;
- final Core state reached before ORS enrichment;
- ORS matrix batch 1 and batch 2 both HTTP 200 with the corrected key and parsed successfully;
- Same-Process Home→App return: Activity `onStop`, later `onStart`/`onResume` without a new `onCreate`.

A separate warm Activity recreation was not reproducible. The gate required this only if reproducible; the actual same-process return path is covered by the Home→App resume evidence.

## Jank/crash classification

The final aggregate log also contains an earlier run disturbed by device Doze/Wake and an early Activity stop/resume. That disturbed run includes `Choreographer: Skipped` messages and is deliberately excluded from clean cold-start timing evidence.

The clean final cold-start segment contains no `Choreographer: Skipped` entry. No app `FATAL EXCEPTION`, app-process crash, app ANR or navigation regression was found in the acceptance scope. `AndroidRuntime` entries in the aggregate capture belong to the shell `monkey` process and terminate normally, not to `now.abfahrt.transit`.

## Finding

Across all three clean cold starts, the dominant repeatable delay occurs after Departure `Loading` but before the first abfahrt.now Core network request. The existing Current Location path resolves coordinates before the request through:

```text
resolveCurrentTargetCoordinates()
  -> getBestLocation()
     -> getCurrentLocation(PRIORITY_HIGH_ACCURACY)
     -> lastLocation only if fresh result is null
```

Application/MapLibre, Compose, the preference/access gate and the first Core HTTP call do not explain the repeated pre-network delay. Stage A therefore narrows the issue sufficiently; deeper Stage-B markers are not required.

Build 153 intentionally does not optimize this path. The remaining performance issue is tracked as F-153-001 / B-154-001 and will be handled in a separate build after an explicit freshness/accuracy/re-anchor policy is specified.

Separately, F-ORS-001 / B-ORS-001 tracks validation of a newly entered/changed ORS key before persistence; it is not part of Build 153 or the location-startup optimization.
