# Build 127 Plan

1. Converge the negative Build-126 artifact evidence into `/doc` and `/evidence`.
2. Bump runtime build identity to 1270.
3. Update only the native-producing dependencies proven relevant by the artifact audit.
4. Refine the artifact audit to gate the official 64-bit ABIs while retaining 32-bit diagnostics.
5. Extend the source preflight with explicit remediation baselines.
6. Run all available static project gates and a synthetic audit-parser/gating smoke.
7. Package source for Android Studio build.
8. Require a fresh release APK/AAB audit plus 16-KB runtime smoke before acceptance.
