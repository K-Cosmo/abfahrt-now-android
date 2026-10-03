# Build 151 Plan

1. Keep the existing settings layout and replace only the footer identity surface.
2. Add a small `CommunityFooter` composable rather than expanding `SettingsSheet` further.
3. Link the project CTA to the canonical GitHub repository.
4. Keep abfahrt.now privacy/terms links only with explicit API-provider labels.
5. Add six community identity strings to all 22 bundled locale sets and rely on the existing all-XML locale parity gate.
6. Bump `versionCode` to 1510; keep `versionName` at 1.1.0.
7. Converge F-DOC1-015/B-COMMUNITY-001 and shift AB-018 instrumentation to Build 152.
8. Run static/locale/unit/debug gates, release-minify gate, and a small real-device footer smoke before acceptance.
