# Build 154 — sanitized acceptance evidence — 2026-10-04

## Scope

Build 154 optimizes only the Current-Location cold-start origin resolution. No raw coordinates, device identifiers, API keys or full Logcat content are stored here.

## Baseline

Accepted Build 153 real-device baseline for `Departure Loading` → first abfahrt.now Core request:

- ~2.72 s
- ~2.59 s
- ~3.02 s

The repeated delay was localized before the network request, in the High-Accuracy location resolution path.

## Build 154 real-device runs

Three clean cold starts with an available system `lastLocation`:

| Run | Loading → first Core request | High-Accuracy correction | Result |
|---|---:|---:|---|
| Cold 1 | ~29 ms | 6 m | Same-Origin, no re-anchor |
| Cold 2 | ~30 ms | 0 m | Same-Origin, no re-anchor |
| Cold 3 | ~25 ms | 8 m | Same-Origin, no re-anchor |

The provisional origin therefore removes the previously repeated ~2.6–3.0 s wait before Core.

Cold 2 additionally showed a first Core HTTP response time of ~9.16 s. This is intentionally kept separate from app startup performance: the Core request itself started after ~30 ms, so the long wait occurred after the request had already left the app.

Cold 1 and Cold 3 returned the initial Core response in roughly 166 ms and 192 ms respectively and produced an early progressive departure list.

## High-Accuracy race semantics

In Cold 2 the High-Accuracy correction completed a few milliseconds before the HTTP request was emitted. This does not invalidate the fast path: the Core path had already been released by the provisional origin and did not wait for High Accuracy.

The durable acceptance condition is therefore:

> With a usable provisional `lastLocation`, the Core path must not be blocked by completion of the High-Accuracy correction.

It is not required that the HTTP request always appears chronologically before the correction in every scheduler/network race.

## Request / UI behavior

All three observed corrections were below the existing 200 m movement threshold. No correction-triggered replacement Core cycle was observed. The existing progressive pipeline continued: initial Core/first-paint booster, add-on coverage, final Core state and asynchronous ORS enrichment.

The >=200 m re-anchor case was not practically reproduced in this field run and is not claimed as real-device evidence. It remains protected by the existing Hard-Reset/Pending-Refresh semantics and focused policy tests.

User field feedback confirms that the loading time is visibly and drastically shorter and reports no disturbing location or refresh churn in the observed same-origin runs.

## Automated gate

Android CI on the Build-154 implementation/convergence heads is green, including governance/compatibility gates, committed Gradle wrapper, unit tests, Debug and Release/R8 builds.

## Acceptance

Build 154 is accepted for the observed same-origin cold-start optimization. F-153-001 can be closed. The accepted behavior introduces no new location-age or accuracy magic number and preserves the existing 200 m re-anchor contract.
