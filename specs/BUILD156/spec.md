# Build 156 Specification — Abfahrtsradar identity migration

## Status
Planned / implementation branch.

## Goal
Build 156 changes the visible Community app identity from **Abfahrt!** to **Abfahrtsradar** and makes the renamed GitHub repository `K-Cosmo/abfahrtsradar-android` the canonical project/release location without breaking existing Build-155 installations or the update path.

## Product decision
- Canonical product name: **Abfahrtsradar**.
- The name deliberately retains the semantic link to departures while being clearly distinguishable from the official app named **Abfahrt!**.
- “Radar” matches the product’s location-aware nearby-departure discovery model.
- `abfahrt.now` remains the external API/data provider name and must not be renamed or visually conflated with the Community app.
- The product name itself remains **Abfahrtsradar** in every locale for one stable cross-language identity. Surrounding UI copy remains localized.

## Repository migration
The GitHub repository was renamed from:
`K-Cosmo/abfahrt-now-android`

to:
`K-Cosmo/abfahrtsradar-android`

Observed migration evidence:
- local `origin` updated to the new canonical repository;
- old GitHub API endpoint `/repos/K-Cosmo/abfahrt-now-android/releases/latest` returns HTTP 301 to the stable repository-ID endpoint;
- new endpoint `/repos/K-Cosmo/abfahrtsradar-android/releases/latest` returns `v1.1.0-b155`.

Build 156 must use only the new canonical repository path. Build 155 remains supported through GitHub’s redirect and must be runtime-smoked before Build 156 acceptance.

## Runtime changes
- `versionCode = 1560`
- `versionName = 1.1.0` unchanged.
- `applicationId = now.abfahrt.transit` unchanged.
- namespace/Kotlin package unchanged.
- Android signing lineage unchanged.
- DataStore/Android-Keystore storage unchanged.
- no dependency changes.
- no departure, ORS, routing, Photon, location, sorting or refresh semantic changes.

## UI / localization
All 22 bundled string-resource sets must:
- expose `app_name = Abfahrtsradar`;
- replace user-visible Community-app references to `Abfahrt!` / bare app-brand `Abfahrt` with `Abfahrtsradar`;
- keep `abfahrt.now` unchanged when referring to the API/data provider;
- ensure feedback/API-key email copy addresses or refers to **abfahrt.now**, not “Abfahrt! API”;
- contain no visible historical Riles-Tech ownership attribution in current Community UI.

No new localization service or dependency is introduced.

## Update checker
Build 156 must use:
- metadata: `GET /repos/K-Cosmo/abfahrtsradar-android/releases/latest`
- release page: `https://github.com/K-Cosmo/abfahrtsradar-android/releases/tag/<tag>`

Existing privacy invariant remains:
- anonymous GitHub client;
- no abfahrt.now/ORS credentials;
- no location/search/transit payload sent to GitHub.

## Public project surfaces
README and current normative documentation must use:
- product name **Abfahrtsradar**;
- canonical repository `K-Cosmo/abfahrtsradar-android`;
- MIT license;
- transparent AI-assisted-development statement.

Historical evidence may retain old repository/name references when they describe observed historical state, provided the current canonical state is unambiguous.

## Acceptance
Build 156 is accepted only after:
1. Android CI including unit/debug/release-R8 is green.
2. all 22 locale sets pass resource-key parity and contain `app_name = Abfahrtsradar`.
3. source/static scan finds no current runtime link to `K-Cosmo/abfahrt-now-android`.
4. source/static scan finds no visible current Community-app identity `Abfahrt!` outside intentionally historical/provider contexts.
5. Build-155 installed app can still complete the GitHub update check through the old redirected endpoint.
6. Build-156 update checker directly uses the new repository and recognizes a newer test/release tag when appropriate.
7. in-place install from signed Build 155 to signed Build 156 succeeds without uninstall and preserves settings/API keys.
8. launcher/app UI visibly shows **Abfahrtsradar**.
9. Current Location, departure first paint, sorting persistence, ORS and RoutePlanner smoke remain healthy.
10. no FATAL/ANR regression.
