# Build 156 — sanitized acceptance evidence — 2026-10-06

## Scope

Build 156 migrates the visible Community identity to **Abfahrtsradar** and the canonical repository to `K-Cosmo/abfahrtsradar-android`. Package ID, namespace, signing lineage, persisted app identity and transit/routing behavior remain unchanged.

This file intentionally contains no raw coordinates, device serials, API keys, Wi-Fi identifiers or full Logcat content.

## Source / automated gate

Accepted PR head before convergence:

`054847735011161840da0a28a8bb1c56a82637bc`

Android CI #188 (workflow run 37491416897) completed successfully on that head, including static governance/compatibility gates, committed-wrapper verification, unit tests, Debug and Release/R8.

Local release-candidate build:

`./gradlew :app:testDebugUnitTest :app:assembleRelease --no-daemon` → BUILD SUCCESSFUL.

## Repository-rename compatibility

The public Build-155 installation still uses the old GitHub endpoint.

Observed:
- old `/repos/K-Cosmo/abfahrt-now-android/releases/latest` → HTTP 301 after repository rename;
- the real Build-155 update checker completed with `result=success`.

This proves that already-installed Build 155 can traverse the GitHub rename redirect before Build 156 replaces the endpoint with the new canonical repository path.

## Signed Build-156 candidate

Signed candidate:

- file: `abfahrtsradar-v1.1.0-b156.apk`
- size: 46,317,530 bytes
- SHA-256: `BC1143C08844E21B569BAB41F192BEFB10F8DACA6F681FAAFA216C908B2C5703`
- APK Signature Scheme v3: true
- active signer: `CN=Abfahrt Now Community, C=DE`
- signer certificate SHA-256: `23283ed09731c3711d5f223f0424323697243000a78cbbf0731e4553946325f8`
- `zipalign -c -P 16 -v 4`: successful

The Java native-access warning emitted by Build Tools 37 during `apksigner` execution did not invalidate signing or verification.

## Real in-place update 155 → 156

The installed starting APK was pulled from the test device and matched the published Build-155 SHA-256:

`0FE1A8D7EB8A6038FF4446DD4F696BA37737875982945A60DF860ABE255BD9A5`

The signed Build-156 candidate was then installed with `adb install -r` without uninstalling Build 155.

Result:
- install: `Success`
- installed `versionCode=1560`
- installed `versionName=1.1.0`
- existing preferences preserved
- abfahrt.now and ORS key status preserved
- deliberately selected non-default departure sort profile preserved

This is the accepted Build-155→156 signing-lineage and persistence compatibility proof.

## Real-device functional smoke

Observed after the in-place update:
- visible launcher/app name: **Abfahrtsradar**
- no repeated onboarding on the configured upgrade installation
- current location and departures: healthy
- persisted sorting: healthy
- ORS enrichment / route preview: healthy
- RoutePlanner: healthy
- Build-156 update checker: `update_check_complete ... result=success`
- no app `FATAL EXCEPTION` or app ANR signature in the acceptance smoke

The route-preview Logcat showed a successful ORS GeoJSON result, and the RoutePlanner logged a successful `/trips` result.

Because the public `releases/latest` result was still Build 155 during this acceptance, Build 156 correctly had no newer release to offer. A positive update dialog was therefore not expected or artificially created.

Permission-rationale/update-dialog identity strings were not separately forced on the already-configured upgrade installation; their resource coverage is provided by the automated locale/static gates.

## 16-KB runtime gate

The exact signed Build-156 candidate was tested on the official x86_64 16-KB Android emulator.

Observed:
- `getconf PAGE_SIZE` → `16384`
- install: `Success`
- `AbfahrtCompat: memoryPageSizeBytes=16384`
- native loader successfully loaded `lib/x86_64/libmaplibre.so` with result `ok`
- installed `versionCode=1560`, `versionName=1.1.0`
- no app `FATAL EXCEPTION` / ANR in the filtered runtime smoke

The physical-device in-place-update proof and the emulator 16-KB runtime proof are intentionally separate gates.

## Acceptance

Build 156 is **accepted** for the Abfahrtsradar identity migration. F-NAME-001 / B-156-001 can be closed.

Acceptance is not publication: the last public release remains `v1.1.0-b155` until Build 156 is merged and the exact final tag artifact for `v1.1.0-b156` is rebuilt, signed, verified and published.
