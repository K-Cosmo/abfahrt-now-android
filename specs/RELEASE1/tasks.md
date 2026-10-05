# RELEASE1 Tasks

## Specification / Audit
- [x] Build 155 als akzeptierten Release-Kandidaten festlegen.
- [x] vorhandenen `app/build.gradle.kts`-Signing-Stand prüfen.
- [x] `.gitignore` auf Keystore-/Secret-Schutz prüfen.
- [x] Release-Tag-Vertrag `v1.1.0-b155` bestätigen.
- [x] Signing-/Verify-/16-KB-/Realgeräte-Gate spezifizieren.
- [x] Feldstand aufnehmen: App ist bereits auf drei Geräten mit altem Release-Key installiert.
- [x] Korrektur aufnehmen: historisches Zertifikat enthält irreführende Riles-Tech-/Leonard-Scharf-Metadaten; RELEASE1 rotiert deshalb kontrolliert auf neutralen Community-Key statt den alten Signer dauerhaft fortzuführen.
- [x] Android-v3.1-Rotationspfad für API 33+ als passend zum `minSdk 34`-Produktstand festlegen.

## Repository Implementation
- [x] Gradle-Release bewusst unsigned lassen; keine Single-Key-`signingConfig`, die versehentlich New-Key-only-APK erzeugen könnte.
- [x] normale credential-freie CI / Release-R8 erhalten.
- [x] normative Rotation in `doc/15-release-signing.md` dokumentieren.
- [x] RELEASE1 Spec/Plan auf `old -> new` Signing-Certificate-Lineage umstellen.
- [ ] optionalen lokalen Helper für zipalign/apksigner erst nach manuell bewiesenem Rotationspfad ergänzen; kein Big-Bang-Release-Skript vor Evidence.

## Automated Verification
- [ ] Static/Governance/Compatibility grün.
- [ ] committed Gradle wrapper grün.
- [ ] Unit Tests grün.
- [ ] Debug build grün.
- [ ] unsigned Release/R8 in normaler CI weiterhin grün.

## Old-Key Verification
- [ ] alten Release-Keystore außerhalb des Repos lokalisieren.
- [ ] tatsächlichen alten Alias per `keytool -list -v` bestimmen.
- [ ] Certificate SHA-256 des alten Alias erfassen.
- [ ] separates Backup des alten Keystores verifizieren.
- [ ] Referenz: frühere signierte APK oder `base.apk` eines noch Old-Key-signierten Geräts verwenden.
- [ ] Referenz-APK mit `apksigner verify --verbose --print-certs` prüfen.
- [ ] Old-Key-Fingerprints müssen exakt übereinstimmen; sonst RELEASE1 stoppen.

## New Community Key
- [ ] neuen Keystore außerhalb des Repos erzeugen.
- [ ] RSA 4096 / JKS / lange Gültigkeit verwenden.
- [ ] Alias `abfahrt-now-community` verwenden.
- [ ] neutralen Subject verwenden, empfohlen `CN=Abfahrt Now Community, C=DE`.
- [ ] neuen Keystore separat sichern und Backup-Zugriff verifizieren.
- [ ] keine Passwörter im Terminalverlauf/Repo/Evidence hinterlassen.

## Signing Certificate Lineage
- [ ] Lineage mit `apksigner rotate` old -> new erzeugen.
- [ ] Lineage außerhalb des Repos archivieren und sichern.
- [ ] keinen künstlich niedrigeren `--rotation-min-sdk-version` setzen; Standard-API-33+-Pfad verwenden.

## Signed Artifact Gate
- [ ] `:app:testDebugUnitTest :app:assembleRelease` erfolgreich.
- [ ] unsigned Release-APK mit `zipalign -P 16 -f 4` ausrichten.
- [ ] aligned APK mit altem Signer + neuem Signer + `--lineage` signieren.
- [ ] `apksigner verify --verbose --print-certs` erfolgreich.
- [ ] erwartete Old→New-Lineage verifizieren.
- [ ] neuen Signer Certificate SHA-256 dokumentieren.
- [ ] APK-Datei-SHA-256 dokumentieren.
- [ ] `zipalign -c -P 16 -v 4` erfolgreich.

## Real-device Release Smoke
- [ ] auf mindestens einem bestehenden Old-Key-Gerät **nicht deinstallieren**.
- [ ] exakt rotierte Release-APK mit `adb install -r` erfolgreich aktualisieren.
- [ ] lokale Preferences/API-Keys bleiben erhalten.
- [ ] `versionCode=1550`, `versionName=1.1.0` bestätigen.
- [ ] `PAGE_SIZE=16384` auf dem 16-KB-Testgerät bestätigen.
- [ ] `AbfahrtCompat memoryPageSizeBytes=16384` bestätigen.
- [ ] Current Location / Departure First Paint erfolgreich.
- [ ] mindestens ein Sortierprofil + Persistenz erfolgreich.
- [ ] ORS mit gültigem Key HTTP 200 / Enrichment erfolgreich.
- [ ] RoutePlanner-Grundpfad erfolgreich.
- [ ] keine FATAL-/ANR-Regression.

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
