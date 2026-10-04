# RELEASE1 Tasks

## Specification / Audit
- [x] Build 155 als akzeptierten Release-Kandidaten festlegen.
- [x] vorhandenen `app/build.gradle.kts`-Signing-Stand prüfen.
- [x] `.gitignore` auf Keystore-/Secret-Schutz prüfen.
- [x] Release-Tag-Vertrag `v1.1.0-b155` bestätigen.
- [x] Signing-/Verify-/16-KB-/Realgeräte-Gate spezifizieren.

## Repository Implementation
- [ ] `app/build.gradle.kts` um optionale `ABFAHRT_RELEASE_*`-Signing-Konfiguration erweitern.
- [ ] vollständige Vierer-Konfiguration erzwingen; Teilkonfiguration hart ablehnen.
- [ ] `releaseSigningRequired=true` als explizites Anti-Unsigned-Gate implementieren.
- [ ] fehlende Keystore-Datei bei aktivem Signing hart ablehnen.
- [ ] normale credential-freie CI ohne Release-Secrets erhalten.
- [ ] keine Secrets oder Werte loggen.
- [ ] öffentliche Signing-Anleitung ohne geheime Beispielwerte ergänzen.

## Automated Verification
- [ ] Static/Governance/Compatibility grün.
- [ ] committed Gradle wrapper grün.
- [ ] Unit Tests grün.
- [ ] Debug build grün.
- [ ] unsigned Release/R8 in normaler CI weiterhin grün.
- [ ] negativer lokaler Test: `releaseSigningRequired=true` ohne Signing-Werte schlägt erwartbar fehl.
- [ ] negativer lokaler Test: Teilkonfiguration schlägt erwartbar fehl.

## Local Key Material
- [ ] Release-Keystore außerhalb des Repos erzeugen.
- [ ] Alias `abfahrt-now-release` verwenden.
- [ ] RSA-4096 / JKS / lange Gültigkeit verwenden.
- [ ] separates Backup des Keystores erstellen.
- [ ] Backup-Zugriff vor Veröffentlichung verifizieren.
- [ ] keine Passwörter im Terminalverlauf/Repo/Evidence hinterlassen.

## Signed Artifact Gate
- [ ] PowerShell-Session mit `ABFAHRT_RELEASE_*` sicher setzen.
- [ ] Unit Tests + Release/R8 mit `releaseSigningRequired=true` erfolgreich.
- [ ] `apksigner verify --verbose --print-certs` erfolgreich.
- [ ] Signer Certificate SHA-256 dokumentieren.
- [ ] APK-Datei-SHA-256 dokumentieren.
- [ ] `zipalign -c -P 16 -v 4` erfolgreich.

## Real-device Release Smoke
- [ ] Debug-Installation bewusst entfernen; API-Keys vorher griffbereit halten.
- [ ] exakt signierte Release-APK frisch installieren.
- [ ] `versionCode=1550`, `versionName=1.1.0` bestätigen.
- [ ] `PAGE_SIZE=16384` bestätigen.
- [ ] `AbfahrtCompat memoryPageSizeBytes=16384` bestätigen.
- [ ] AccessGate/API-Key-Pfad erfolgreich.
- [ ] Current Location / Departure First Paint erfolgreich.
- [ ] mindestens ein Sortierprofil + Persistenz erfolgreich.
- [ ] ORS mit gültigem Key HTTP 200 / Enrichment erfolgreich.
- [ ] RoutePlanner-Grundpfad erfolgreich.
- [ ] keine FATAL-/ANR-Regression.

## GitHub Release
- [ ] finales APK sprechend als `abfahrt-now-v1.1.0-b155.apk` bereitstellen.
- [ ] Release Notes inkl. APK-SHA-256 und Signing-Fingerprint vorbereiten.
- [ ] Tag/Release `v1.1.0-b155` erstellen.
- [ ] exakt verifiziertes APK als Asset hochladen.
- [ ] Update-Checker gegen echtes Release testen.
- [ ] RELEASE1-Evidence nach `/evidence/public/release-1/` konvergieren.

## Nicht blockierend / später
- [ ] optionalen signierten GitHub-Actions-Release-Workflow erst nach bewiesenem lokalen RELEASE1-Pfad planen.