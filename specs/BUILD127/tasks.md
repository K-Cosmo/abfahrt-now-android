# Build 127 Tasks

- [x] Archive Build-126 release-APK audit evidence.
- [x] Mark Build 126 not accepted for 16-KB compatibility.
- [x] Bump `versionCode` to 1270.
- [x] Update DataStore to 1.2.1.
- [x] Add explicit `androidx.graphics:graphics-path` 1.1.0 dependency.
- [x] Update MapLibre 11.12.1 -> 11.13.5.
- [x] Keep MapLibre on major 11.
- [x] Refine artifact audit: arm64-v8a/x86_64 hard gate, 32-bit warning.
- [x] Extend 16-KB source preflight for dependency baselines.
- [x] Update normative `/doc`.
- [x] Run static source/evidence gates available in this environment.
- [ ] Build release APK/AAB in complete Android environment.
- [ ] Re-run 16-KB artifact audit; require no 64-bit failures.
- [ ] Confirm AAB `PAGE_ALIGNMENT_16K` if AAB is produced.
- [ ] Run on a 16-KB runtime and capture `memoryPageSizeBytes=16384`.
- [ ] Verify persisted API keys/preferences, departures and MapLibre route preview.
