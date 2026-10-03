# Build 150 Specification — GitHub release update checker

## Problem

The public community app is now developed and distributed through `K-Cosmo/abfahrt-now-android`, but an installed build has no simple way to tell the user that a newer community release exists. A custom update backend or in-app installer would add unnecessary operational and security complexity.

## Required behavior

- Build 150 uses `versionCode = 1500` and keeps `versionName = 1.1.0`.
- Once per Activity/app start, check the public GitHub `latest` release asynchronously.
- GitHub access uses a dedicated anonymous OkHttp/Retrofit client with no `ApiKeyInterceptor`, GitHub token, abfahrt.now key, ORS key, location, search text, or transit data.
- Only tags matching `v<major>.<minor>.<patch>-b<build>` are accepted.
- The monotonic build number determines whether an update is newer; Android `versionCode` currently follows `build * 10`.
- Invalid tags, missing releases, offline state, timeout, rate limiting, and other GitHub failures are silent/non-blocking.
- A newer release shows a small localized prompt.
- Explicit user action opens only the validated release page of the fixed community repository in the external browser.
- No automatic APK download, package installation, background installer, or install permission is added.
- Existing departure, Photon, ORS, `/trips`, persistence, sorting, map, and API-key behavior remains unchanged.
- New UI text must be present in all 22 bundled locale sets.

## Acceptance boundary

Build 150 is not accepted from source review alone. Required evidence is defined in `/doc/09-release-plan.md` and the BUILD150 tasks: final CI on the converged branch plus a real older-build/newer-release runtime E2E check. The separate community About/Legal convergence remains outside this build and blocks the first public community app release.
