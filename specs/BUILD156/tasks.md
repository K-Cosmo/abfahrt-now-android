# Build 156 Tasks — Abfahrtsradar identity migration

## Specification / governance
- [x] Product name decided: Abfahrtsradar.
- [x] Repository renamed to `K-Cosmo/abfahrtsradar-android`.
- [x] Old `releases/latest` endpoint observed returning HTTP 301 after rename.
- [x] New `releases/latest` endpoint observed returning `v1.1.0-b155`.
- [x] Build-156 scope/spec/plan created.

## Runtime implementation
- [x] Set `versionCode = 1560`; keep `versionName = 1.1.0`.
- [x] Update GitHub release API path to new repository.
- [x] Update release-page URL generation to new repository.
- [x] Update Community footer repository link.
- [x] Keep applicationId/package/signing lineage unchanged.

## Localization / UI
- [x] Set `app_name = Abfahrtsradar` in all 22 locale sets.
- [x] Replace visible old Community-app brand references in all locale sets.
- [x] Correct API feedback/support wording to `abfahrt.now`.
- [x] Confirm no current Community UI claims Riles Tech ownership.
- [x] Real-device launcher/app label and normal post-upgrade startup verified.
- [x] Onboarding/permission-rationale/settings-footer/update-dialog identity resources covered by static locale/resource gates; during acceptance `latest=b155`, so a positive Build-156 update dialog was correctly not expected.

## Public project surface
- [x] Update README title/text/current repository links.
- [x] Update current normative `/doc` references to Abfahrtsradar/new repository.
- [x] Record repository-rename/redirect evidence.
- [x] Keep MIT and AI-assisted-development disclosures intact.

## Automated verification
- [x] Locale parity/static governance gates green.
- [x] Unit tests green.
- [x] Debug build green.
- [x] Release/R8 build green.
- [x] No current runtime source reference to old repository slug (known runtime update/footer/policy paths statically checked).
- [x] No stale visible `Abfahrt!` Community-app identity in any of the 22 locale resource sets.

## Migration/runtime evidence
- [x] Existing Build 155 update checker succeeds through renamed-repo redirect.
- [x] Signed Build 156 installs in-place over signed/public Build 155.
- [x] Preferences/API keys preserved.
- [x] Sort profile preserved.
- [x] Current Location / departure first paint healthy.
- [x] ORS enrichment healthy.
- [x] RoutePlanner basic path healthy.
- [x] Build-156 update checker succeeds against the new repository path.
- [x] No app FATAL/ANR.
- [x] Signed release-candidate artifact passes `apksigner`, `zipalign -c -P 16` and 16-KB runtime/MapLibre native-load gate.

## Acceptance / release
- [x] Evidence converged into `/doc` and `/evidence/public/build-156/`.
- [x] Build 156 accepted.
- [x] After merge, rebuild/sign/verify the exact final artifact and publish `v1.1.0-b156` with `abfahrtsradar-v1.1.0-b156.apk` + `SHA256SUMS.txt`; `releases/latest` verified on Build 156.
