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
- [ ] Final GitHub Actions run on the fully converged Build-150 branch is green.

## Runtime / acceptance evidence
- [ ] App starts and works normally when no release exists or GitHub is unavailable.
- [ ] Older installed build + newer valid release tag shows the update prompt.
- [ ] Update action opens the expected fixed GitHub release page.
- [ ] Same/newest installed build does not show an update prompt.
- [ ] No observed credential leak or update-check-induced core startup failure.
- [ ] After required evidence, mark F-150-001/Build 150 accepted and add final changelog evidence.

## Release boundary
- [ ] First public community APK release remains blocked until B-COMMUNITY-001 / F-DOC1-015 is resolved in its own product change.
