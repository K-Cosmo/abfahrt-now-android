# RELEASE1 Tasks

## Specification / Audit
- [x] Build 155 als akzeptierten Release-Kandidaten festlegen.
- [x] vorhandenen `app/build.gradle.kts`-Signing-Stand prüfen.
- [x] `.gitignore` auf Keystore-/Secret-Schutz prüfen.
- [x] Release-Tag-Vertrag `v1.1.0-b155` bestätigen.
- [x] Signing-/Verify-/16-KB-/Realgeräte-Gate spezifizieren.
- [x] Feldstand aufnehmen: App ist bereits auf drei Geräten mit altem Release-Key installiert.
- [x] Korrektur aufnehmen: historisches Zertifikat enthält irreführende Riles-Tech-/Leonard-Scharf-Metadaten; RELEASE1 rotiert deshalb kontrolliert auf neutralen Community-Key statt den alten Signer dauerhaft fortzuführen.
- [x] Signing-Certificate-Lineage für den `minSdk 34`-Produktstand festlegen und real per In-place-Update beweisen.

## Repository Implementation
- [x] Gradle-Release bewusst unsigned lassen; keine Single-Key-`signingConfig`, die versehentlich New-Key-only-APK erzeugen könnte.
- [x] normale credential-freie CI / Release-R8 erhalten.
- [x] normative Rotation in `doc/15-release-signing.md` dokumentieren.
- [x] RELEASE1 Spec/Plan auf `old -> new` Signing-Certificate-Lineage umstellen.
- [ ] optionalen lokalen Helper für zipalign/apksigner erst nach vollständig abgenommenem RELEASE1 ergänzen; kein Big-Bang-Release-Skript vor Evidence.

## Automated Verification
- [x] Static/Governance/Compatibility grün (Android CI #160).
- [x] committed Gradle wrapper grün.
- [x] Unit Tests grün.
- [x] Debug build grün.
- [x] unsigned Release/R8 in normaler CI weiterhin grün.

## Old-Key Verification
- [x] alten Release-Keystore außerhalb des Repos lokalisiert.
- [x] tatsächlichen alten Alias per `keytool -list -v` bestimmt: `abfahrt`.
- [x] Certificate SHA-256 des alten Alias erfasst.
- [ ] separates Backup des alten Keystores verifizieren.
- [x] historische Release-APK als Signer-Referenz verwendet.
- [x] Referenz-APK mit `apksigner verify --verbose --print-certs` geprüft.
- [x] Old-Key-Fingerprints stimmen exakt überein.

## New Community Key
- [x] neuen Keystore außerhalb des Repos erzeugt.
- [x] RSA 4096 verwendet.
- [x] Alias `abfahrt-now-community` verwendet.
- [x] neutralen Subject `CN=Abfahrt Now Community, C=DE` verwendet.
- [x] nach PKCS12 migriert und Zertifikat/Fingerprint verifiziert.
- [ ] neuen Keystore separat sichern und Backup-Zugriff verifizieren.
- [x] keine Passwörter im Terminalverlauf/Repo/Evidence hinterlassen.

## Signing Certificate Lineage
- [x] Lineage mit `apksigner rotate` old -> new erzeugt.
- [ ] Lineage außerhalb des Repos separat archivieren und Backup-Zugriff verifizieren.
- [x] kein künstlich niedrigerer Rotations-SDK-Wert erzwungen.

## Signed Artifact Gate
- [x] `:app:testDebugUnitTest :app:assembleRelease` erfolgreich.
- [x] unsigned Release-APK mit `zipalign -P 16 -f 4` ausgerichtet.
- [x] aligned APK mit altem Signer + neuem Signer + `--lineage` signiert.
- [x] `apksigner verify --verbose --print-certs` erfolgreich.
- [x] realen Old→New-Updatepfad über erfolgreiches `adb install -r` bewiesen.
- [x] neuen Signer Certificate SHA-256 dokumentiert: `23283ed09731c3711d5f223f0424323697243000a78cbbf0731e4553946325f8`.
- [x] APK-Datei-SHA-256 dokumentiert: `9A165BCABC74CC40600A96BF6B7D83116B20FE9456EFF7344D39A4A26223BC3F`.
- [x] `zipalign -c -P 16 -v 4` auf dem signierten Kandidaten erfolgreich.

## Real-device Release Smoke
- [x] auf bestehendem Old-Key-Gerät **nicht deinstalliert**.
- [x] exakt rotierte Release-APK mit `adb install -r` erfolgreich aktualisiert.
- [ ] lokale Preferences/API-Keys bleiben erhalten.
- [x] `versionCode=1550`, `versionName=1.1.0` bestätigt.
- [x] `PAGE_SIZE=16384` auf dem offiziellen 16-KB-Testsystem/Emulator bestätigt.
- [x] `AbfahrtCompat memoryPageSizeBytes=16384` im Cold-Start-Log bestätigt.
- [ ] Current Location / Departure First Paint erfolgreich.
- [ ] mindestens ein Sortierprofil + Persistenz erfolgreich.
- [ ] ORS mit gültigem Key HTTP 200 / Enrichment erfolgreich.
- [ ] RoutePlanner-Grundpfad erfolgreich.
- [x] keine FATAL-/ANR-Regression im 4-KB-Cold-Start und 16-KB-Emulator-Smoke.

## GitHub Release
- [ ] finales APK sprechend als `abfahrt-now-v1.1.0-b155.apk` bereitstellen.
- [ ] Release Notes inkl. APK-SHA-256 und neuem Signing-Fingerprint vorbereiten.
- [ ] kontrollierte Key-Rotation / erhaltene Android-Update-Lineage dokumentieren.
- [ ] Tag/Release `v1.1.0-b155` erstellen.
- [ ] exakt verifiziertes APK als Asset hochladen.
- [ ] Update-Checker gegen echtes Release testen.
- [ ] RELEASE1-Evidence nach `/evidence/public/release-1/` konvergieren.

## Nicht blockierend / später
- [ ] optionalen signierten GitHub-Actions-Release-Workflow erst nach bewiesenem lokalen RELEASE1-Rotationspfad planen.
