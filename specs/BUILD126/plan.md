# Build 126 Plan

1. Accept Build 125 from device evidence and close the API-key-storage runtime gate.
2. Bump runtime build identity to 1260 without feature changes.
3. Log the OS memory page size once at application startup.
4. Add a static 16-KB source preflight for AGP/packaging/native-dependency assumptions.
5. Add an APK/AAB native-library audit implemented with Python stdlib only.
6. Preserve MapLibre 11.12.1 unless artifact evidence proves a compatibility problem.
7. Update `/doc` and Evidence with the distinction between source readiness and artifact/runtime acceptance.
