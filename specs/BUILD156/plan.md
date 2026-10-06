# Build 156 Plan — Abfahrtsradar identity migration

## 1. Baseline
Start from `main` after RELEASE1 and the MIT/README convergence. Keep package ID, signing lineage, versionName and architecture unchanged.

## 2. Canonical identifiers
Change only identities that are intentionally public/user-visible:
- app display name -> `Abfahrtsradar`
- repository URLs -> `K-Cosmo/abfahrtsradar-android`
- update-check API path -> renamed repository
- update release-page URL -> renamed repository
- next Android `versionCode` -> 1560

Do not rename Kotlin packages, `AbfahrtApplication`, `Theme.Abfahrt`, DataStore files, API model classes or log tags merely for cosmetics.

## 3. Localized UI
Audit all 22 `strings.xml` sets for brand-bearing keys. Apply the canonical product name consistently and separate Community app identity from the provider:
- app/launcher name and welcome/location/onboarding copy -> Abfahrtsradar
- API feedback/support references -> abfahrt.now
- preserve existing translations except where the old product/provider identity is wrong
- do not expand this build into a broad translation-quality rewrite unrelated to identity

## 4. GitHub update migration
Update the three known runtime/code references:
- `GitHubReleaseApi.kt`
- `UpdateReleasePolicy.kt`
- `CommunityFooter.kt`

Update unit tests for the new canonical release URL. Preserve the isolated anonymous GitHub client.

## 5. Docs / public surface
Update README/current `/doc` canonical project references and record the repository rename plus Build-155 redirect evidence. Historical release evidence remains factually historical.

## 6. Automated gates
Run:
`./gradlew :app:testDebugUnitTest :app:assembleDebug :app:assembleRelease --no-daemon`

Also enforce static scans for:
- old repository slug in current runtime sources;
- old visible product identity in locale resources;
- locale key parity.

## 7. Runtime evidence
Before acceptance:
- Build 155: update check succeeds against old redirected GitHub endpoint.
- Build 156: launcher/settings/onboarding show Abfahrtsradar.
- signed Build 156 installs over signed Build 155 without uninstall.
- settings/API keys and selected sort profile persist.
- core transit/ORS/RoutePlanner smoke.
- no FATAL/ANR.

## 8. Release
If accepted, next release tag is `v1.1.0-b156` and preferred APK asset name is `abfahrtsradar-v1.1.0-b156.apk`.
