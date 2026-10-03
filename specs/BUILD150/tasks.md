# Build 150 Tasks

## Specification / plan
- [x] Scope limited to update availability + external release-page opening.
- [x] GitHub documented as a deliberate, minimized non-EU runtime exception.
- [x] Community About/Legal convergence explicitly kept outside Build 150.

## Implementation
- [x] `versionCode` bumped to 1500; `versionName` remains 1.1.0.
- [x] Public `releases/latest` API added.
- [x] Dedicated anonymous GitHub OkHttp/Retrofit client added.
- [x] No `ApiKeyInterceptor`/GitHub token/service credential on the GitHub client.
- [x] Strict `v<semver>-b<build>` parser and monotonic build comparison added.
- [x] Fixed-repository release URL derived only after tag validation.
- [x] One asynchronous check per Activity lifecycle.
- [x] Silent behavior for missing release/network/GitHub errors.
- [x] Localized update prompt added for all 22 bundled locales.
- [x] No APK auto-download, installer, or install permission added.

## Automated protection
- [x] Unit tests for valid/invalid tags and newer/same/older build comparison.
- [x] Unit test protecting GitHub-client credential isolation.
- [x] Locale parity gate expanded to all XML string resources per locale directory.
- [x] Final GitHub Actions run on the fully converged implementation branch is green (Android CI run #32, commit `2425ef465adc502b04d85d31d422104f3aec544a`).

## Runtime / acceptance evidence
- [x] App starts and works normally when no GitHub release exists; no update dialog is shown.
- [x] Same Build-150 code installed with temporary local `versionCode 1490` + real metadata-only release `v1.1.0-b150` shows the update prompt.
- [x] Update action opens the expected fixed GitHub release page for `v1.1.0-b150`.
- [x] Restored Build 150 / `versionCode 1500` does not show an update prompt for the same `b150` release.
- [x] No update-check-induced core startup failure was observed; credential isolation remains additionally protected by the focused unit test.
- [x] Release/minify gate required by I-064: `gradlew.bat :app:assembleRelease` completed with `BUILD SUCCESSFUL` in 52s.
- [x] Build 150 / F-150-001 is accepted on the supplied CI, Runtime-E2E and Release-build evidence.

## Release boundary
- [ ] First public community APK release remains blocked until B-COMMUNITY-001 / F-DOC1-015 is resolved in its own product change.
