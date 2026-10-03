# Build 126 Specification — 16-KB Page-Size Readiness

## Goal

Create reproducible evidence for Android 16-KB memory-page compatibility without changing transit behavior or performing an unrelated MapLibre/toolchain upgrade.

## Requirements

1. Keep the existing runtime feature set unchanged.
2. Keep AGP 8.13.2; it already exceeds Android's 8.5.1+ packaging recommendation.
3. Do not upgrade MapLibre solely from assumption; the final packaged native libraries are the acceptance truth.
4. Add a source preflight that detects incompatible packaging/toolchain regressions.
5. Add an artifact audit that checks every packaged `.so` for 16-KB-compatible ELF PT_LOAD alignment and GNU_RELRO segment-end alignment when RELRO is present.
6. For APKs with uncompressed `.so`, also verify 16-KB ZIP data alignment.
7. Add a startup diagnostic that logs the actual runtime page size without changing app behavior.
8. Final acceptance requires a built APK/AAB plus a 16-KB emulator/device smoke; source analysis alone is insufficient.
9. No change to API, dedup, ORS, location, UI or MapLibre usage.

## Acceptance

- source preflight passes;
- release/debug APK audit reports all native libraries 16-KB compatible;
- if AAB is used, `bundletool dump config` reports `PAGE_ALIGNMENT_16K`;
- on a 16-KB Android system, Logcat reports `AbfahrtCompat memoryPageSizeBytes=16384`;
- app launches, loads departures and opens a MapLibre route preview without native crash.
