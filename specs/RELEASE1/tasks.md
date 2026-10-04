# RELEASE1 Tasks

## Specification / Audit
- [x] Build 155 als akzeptierten Release-Kandidaten festlegen.
- [x] vorhandenen `app/build.gradle.kts`-Signing-Stand prüfen.
- [x] `.gitignore` auf Keystore-/Secret-Schutz prüfen.
- [x] Release-Tag-Vertrag `v1.1.0-b155` bestätigen.
- [x] Signing-/Verify-/16-KB-/Realgeräte-Gate spezifizieren.
- [x] Feldstand aufnehmen: App ist bereits auf drei Geräten mit vorhandenem Release-Key installiert; RELEASE1 darf keinen neuen Key erzeugen.

## Repository Implementation
- [x] `app/build.gradle.kts` um optionale `ABFAHRT_RELEASE_*`-Signing-Konfiguration erweitern.
- [x] vollständige Vierer-Konfiguration erzwingen; Teilkonfiguration hart ablehnen.
- [x] `releaseSigningRequired=true` als explizites Anti-Unsigned-Gate implementieren.
- [x] fehlende Keystore-Datei bei aktivem Signing hart ablehnen.
- [x] normale credential-freie CI ohne Release-Secrets erhalten.
- [x] keine Secrets oder Werte loggen.
- [x] öffentliche Signing-Anleitung ohne geheime Beispielwerte ergänzen.

## Automated Verification
- [ ] Static/Governance/Compatibility grün.
- [ ] committed Gradle wrapper grün.
- [ ] Unit Tests grün.
- [ ] Debug build grün.
- [ ] unsigned Release/R8 in normaler CI weiterhin grün.
- [ ] negativer lokaler Test: `releaseSigningRequired=true` ohne Signing-Werte schlägt erwartbar fehl.
- [ ] negativer lokaler Test: Teilkonfiguration schlägt erwartbar fehl.

## Existing Key Verification
- [ ] vorhandenen Release-Keystore außerhalb des Repos lokalisieren.
- [ ] tatsächlichen bestehenden Alias per `keytool -list -v` bestimmen.
- [ ] Signer Certificate SHA-256 des Keystore-Alias erfassen.
- [ ] separates Backup des **bestehenden** Keystores verifizieren.
- [ ] auf einem bereits Release-signierten Gerät APK-Pfad mit `adb shell pm path now.abfahrt.transit` ermitteln.
- [ ] installierte APK per `adb pull` lokal sichern.
- [ ] Signer Certificate SHA-256 der installierten APK mit `apksigner verify --print-certs` erfassen.
- [ ] Fingerprints müssen exakt übereinstimmen; bei Abweichung RELEASE1 stoppen.
- [ ] keine Passwörter im Terminalverlauf/Repo/Evidence hinterlassen.

## Signed Artifact Gate
- [ ] PowerShell-Session mit `ABFAHRT_RELEASE_*` sicher auf den bestehenden Keystore setzen.
- [ ] Unit Tests + Release/R8 mit `releaseSigningRequired=true` erfolgreich.
- [ ] `apksigner verify --verbose --print-certs` erfolgreich.
- [ ] Signer Certificate SHA-256 entspricht dem vorher bestätigten bestehenden Signer.
- [ ] APK-Datei-SHA-256 dokumentieren.
- [ ] `zipalign -c -P 16 -v 4` erfolgreich.

## Real-device Release Smoke
- [ ] auf mindestens einem bestehenden Release-Gerät **nicht deinstallieren**.
- [ ] exakt signierte Release-APK mit `adb install -r` erfolgreich aktualisieren.
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
- [ ] Release Notes inkl. APK-SHA-256 und Signing-Fingerprint vorbereiten.
- [ ] dokumentieren, dass derselbe bestehende Release-Key weiterverwendet wird.
- [ ] Tag/Release `v1.1.0-b155` erstellen.
- [ ] exakt verifiziertes APK als Asset hochladen.
- [ ] Update-Checker gegen echtes Release testen.
- [ ] RELEASE1-Evidence nach `/evidence/public/release-1/` konvergieren.

## Nicht blockierend / später
- [ ] optionalen signierten GitHub-Actions-Release-Workflow erst nach bewiesenem lokalen RELEASE1-Pfad planen.