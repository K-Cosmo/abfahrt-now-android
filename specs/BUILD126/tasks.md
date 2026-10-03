# Build 126 Tasks

- [x] Accept Build 125 device evidence.
- [x] Close F-DOC1-005 / AB-008b.
- [x] Bump `versionCode` to 1260.
- [x] Add runtime page-size diagnostic (`AbfahrtCompat`).
- [x] Add `scripts/check_16kb_readiness.py`.
- [x] Add `scripts/audit_16kb_artifact.py`.
- [x] Keep MapLibre and all functional dependencies unchanged.
- [x] Update normative `/doc` and source evidence.
- [x] Build release APK in a complete Android environment.
- [x] Audit the produced APK — failed on native ELF alignment and triggered Build 127.
- [ ] Run on 16-KB emulator/device and capture `memoryPageSizeBytes=16384`.
- [ ] Smoke departure loading and MapLibre route preview on that 16-KB runtime.
