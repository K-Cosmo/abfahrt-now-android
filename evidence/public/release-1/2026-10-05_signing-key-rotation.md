# RELEASE1 signing-key rotation evidence — 2026-10-05

## Scope

Public, non-secret acceptance evidence for the signing-key rotation used by `v1.1.0-b155`.

No keystore, lineage file, passwords, device serials or private-key material are stored here.

## Historical signer

- alias: `abfahrt`
- key: RSA 2048
- certificate SHA-256: `3B:49:8E:F9:B0:EA:98:18:C5:6F:A9:2C:8D:34:57:FA:CA:83:B5:72:F9:DA:83:A0:D2:39:9F:B5:56:05:94:9F`
- `keytool -list -v` on the historical keystore matched the certificate fingerprint of the historical release APK exactly.

## New Community signer

- alias: `abfahrt-now-community`
- keystore type: PKCS12
- key: RSA 4096
- subject: `CN=Abfahrt Now Community, C=DE`
- certificate SHA-256: `23:28:3E:D0:97:31:C3:71:1D:5F:22:3F:04:24:32:36:97:24:30:00:A7:8C:BB:F0:73:1E:45:53:94:63:25:F8`

A signing-certificate lineage `old -> new` was generated outside the repository with `apksigner rotate`.

## Candidate build

Source branch head used for the locally signed candidate before evidence-convergence commits:

- `chore/release1-signing`
- commit `fe7d8e5`
- `versionCode=1550`
- `versionName=1.1.0`

Build:

- `:app:testDebugUnitTest :app:assembleRelease` successful
- unsigned/minified Release APK generated
- `zipalign -P 16 -f 4` successful before signing
- final signing performed with historical signer, `--next-signer` Community signer and the lineage

`apksigner verify --min-sdk-version 34 --verbose --print-certs` on the signed candidate reported:

- `Verifies`
- APK Signature Scheme v3: `true`
- APK Signature Scheme v3.1: `false`
- current signer: `CN=Abfahrt Now Community, C=DE`
- current signer certificate SHA-256: `23283ed09731c3711d5f223f0424323697243000a78cbbf0731e4553946325f8`

The signed candidate also passed `zipalign -c -P 16 -v 4`.

Candidate APK SHA-256:

`9A165BCABC74CC40600A96BF6B7D83116B20FE9456EFF7344D39A4A26223BC3F`

## Real old-key in-place update

On a real supported Android device, the installed `base.apk` was first pulled and verified as the historical release signer:

- signer certificate SHA-256: `3b498ef9b0ea9818c56fa92c8d3457faca83b572f9da83a0d2399fb55605949f`

The rotated candidate was then installed **without uninstalling the existing app**:

```text
adb install -r <candidate.apk>
Success
```

After the update:

- `versionCode=1550`
- `versionName=1.1.0`

This proves real Android update compatibility from the historical signer to the new Community signer for the supported product range.

## 16-KB runtime proof

The same signed candidate was installed on an official Android 16-KB x86_64 emulator.

Observed cold-start evidence:

- runtime page size: `16384`
- app log: `AbfahrtCompat: memoryPageSizeBytes=16384`
- `lib/x86_64/libmaplibre.so` loaded successfully via the native loader
- no `FATAL EXCEPTION`
- no app ANR observed in the captured run

The real end-user update device used for the signing-rotation proof runs with 4-KB pages. RELEASE1 therefore deliberately separates the two proofs: real-hardware old->new signer update compatibility on 4-KB Android, and exact-candidate native/runtime compatibility on the official 16-KB emulator.

## Remaining gates

Before public GitHub release:

- confirm Preferences/API-key retention on the updated device
- core release smoke on real hardware: Current Location, departure first paint, sorting persistence, ORS enrichment, RoutePlanner
- verify separate backups for historical keystore, Community keystore and signing lineage
- generate the final post-merge release APK from `main`, verify it again, and publish that exact artifact
