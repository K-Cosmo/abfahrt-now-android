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
- [ ] Verify launcher, onboarding, permission rationale, settings footer and update dialog.

## Public project surface
- [x] Update README title/text/current repository links.
- [x] Update current normative `/doc` references to Abfahrtsradar/new repository.
- [x] Record repository-rename/redirect evidence.
- [x] Keep MIT and AI-assisted-development disclosures intact.

## Automated verification
- [ ] Locale parity/static governance gates green.
- [ ] Unit tests green.
- [ ] Debug build green.
- [ ] Release/R8 build green.
- [x] No current runtime source reference to old repository slug (known runtime update/footer/policy paths statically checked).
- [x] No stale visible `Abfahrt!` Community-app identity in any of the 22 locale resource sets.

## Migration/runtime evidence
- [ ] Existing Build 155 update checker succeeds through renamed-repo redirect.
- [ ] Signed Build 156 installs in-place over signed Build 155.
- [ ] Preferences/API keys preserved.
- [ ] Sort profile preserved.
- [ ] Current Location / departure first paint healthy.
- [ ] ORS enrichment healthy.
- [ ] RoutePlanner basic path healthy.
- [ ] No FATAL/ANR.
- [ ] 16-KB release artifact/runtime gate remains green if a release candidate is produced.

## Acceptance / release
- [ ] Evidence converged into `/doc` and `/evidence/public/build-156/`.
- [ ] Build 156 accepted.
- [ ] Prepare `v1.1.0-b156` with `abfahrtsradar-v1.1.0-b156.apk`.
