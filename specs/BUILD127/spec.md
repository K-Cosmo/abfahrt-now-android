# Build 127 Specification — 16-KB Native Dependency Refresh

## Goal

Remediate the native-library failures proven by the Build-126 release-APK audit without changing product behavior, API behavior, transit logic, UI, or security semantics.

## Evidence trigger

Build 126 packaged all native libraries at 16-KB ZIP boundaries, but the ELF audit failed 11 of 12 libraries. The 64-bit acceptance blockers were:
- arm64-v8a: `libandroidx.graphics.path.so`, `libdatastore_shared_counter.so`, `libmaplibre.so`;
- x86_64: `libandroidx.graphics.path.so`, `libdatastore_shared_counter.so`;
- x86_64 MapLibre was already compliant.

## Requirements

1. Keep AGP 8.13.2 and modern JNI packaging unchanged because Build-126 ZIP evidence is already green.
2. Update DataStore from 1.1.2 to stable 1.2.1.
3. Add an explicit direct dependency on stable `androidx.graphics:graphics-path` 1.1.0 so the native binary is deterministic.
4. Update MapLibre from 11.12.1 to 11.13.5 without crossing the 11.x major boundary.
5. Do not change MapLibre usage, map UI, routing logic or style logic.
6. Keep the Build-126 runtime page-size diagnostic.
7. Keep the artifact as acceptance truth; dependency versions alone do not prove 16-KB readiness.
8. Treat arm64-v8a and x86_64 as hard artifact gates; keep 32-bit ABI findings visible as non-gating warnings.
9. Preserve existing encrypted DataStore/API-key behavior across the dependency upgrade.
10. No unrelated dependency modernization.

## Acceptance

- source preflight passes;
- release APK builds successfully in a complete Android environment;
- `scripts/audit_16kb_artifact.py` reports no gating failures for arm64-v8a or x86_64;
- if AAB is used, bundle config reports `PAGE_ALIGNMENT_16K`;
- on a real/emulated 16-KB Android runtime, `AbfahrtCompat memoryPageSizeBytes=16384` is logged;
- existing preferences/API keys load successfully, departures load, and a MapLibre route preview renders without native crash.
