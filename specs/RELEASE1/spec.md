# RELEASE1 Specification — first signed GitHub APK

## Ziel

RELEASE1 veröffentlicht den akzeptierten Build 155 (`versionCode = 1550`, `versionName = 1.1.0`) erstmals als öffentlichen GitHub-APK-Release unter Tag `v1.1.0-b155`.

Die App ist bereits auf drei realen Geräten mit einem vorhandenen privaten Release-Key installiert. Das Zertifikat dieses alten Keys enthält historische Riles-Tech-/Leonard-Scharf-Metadaten, die für das heutige unabhängige Community-Projekt nicht als dauerhafte öffentliche Signer-Identität fortgeführt werden sollen.

RELEASE1 rotiert deshalb kontrolliert vom **alten Signer** auf einen **neuen neutralen Community-Signer**, erhält aber über APK Signature Scheme v3.1 eine kryptografische Signing-Certificate-Lineage `old -> new`, damit bestehende kompatible Installationen weiter aktualisiert werden können.

RELEASE1 ist **Release Engineering**, kein neuer Produktbuild. Es ändert keine fachliche Runtime-Semantik und erhöht weder `versionCode` noch `versionName`.

## Baseline

- akzeptierter Produktstand: Build 155
- `applicationId = now.abfahrt.transit`
- `minSdk = 34`, `compileSdk = 37`, `targetSdk = 37`
- Release/R8 ist bereits CI-grün
- Android 13/API 33+ unterstützt den v3.1-Rotationspfad; Build 155 unterstützt ab Android 14/API 34
- 16-KB-Readiness wurde in Build 130 real akzeptiert; RELEASE1 muss sie auf dem final signierten Artefakt erneut als Release-Gate prüfen
- Update-Checker erwartet Tag-Schema `v<versionName>-b<human build>`; RELEASE1 verwendet `v1.1.0-b155`
- drei bestehende Old-Key-Installationen bilden die reale Update-Kompatibilitätsbasis für den Rotations-Smoke

## Signing-Grundsätze

1. Der alte private Key bleibt erhalten und wird **nicht** gelöscht.
2. Ein neuer neutraler Community-Key wird einmalig erzeugt und wird nach erfolgreicher Rotation der aktive Signer für künftige Releases.
3. Die Rotation wird mit `apksigner rotate` als Signing-Certificate-Lineage `old -> new` erzeugt.
4. Das finale APK wird **nicht** nur mit dem neuen Key signiert, sondern mit beiden Signer-Kontexten plus `--lineage`.
5. Keystores, Lineage-Backup und Passwörter dürfen nicht in Git, GitHub Actions Logs, `/doc`, `/evidence/public` oder andere öffentliche Artefakte gelangen.
6. Der alte Keystore muss vor Rotation gegen eine frühere bzw. installierte Old-Key-APK per Zertifikat-SHA-256 verifiziert werden.
7. Die normale Android-CI bleibt credential-frei und erzeugt weiterhin ein unsigned/minifiziertes Release/R8-Artefakt.
8. Die eigentliche RELEASE1-Signierung erfolgt lokal nach `zipalign` mit `apksigner`.
9. Ein Signaturkonflikt beim In-place-Update ist ein Stop-Befund und darf nicht durch Deinstallation kaschiert werden.

## Neuer Community-Key

Empfohlene RELEASE1-Parameter:

- RSA 4096 Bit
- JKS
- Alias `abfahrt-now-community`
- Gültigkeit mindestens 10.000 Tage
- neutraler Subject, z. B. `CN=Abfahrt Now Community, C=DE`

Kein Firmenname und keine Person soll als vermeintlicher App-Eigentümer im neuen Zertifikat stehen, sofern dies nicht der tatsächlichen dauerhaften Projektidentität entspricht.

## Alte Signer-Identität vor Rotation prüfen

1. alten Keystore mit `keytool -list -v` öffnen;
2. realen alten Alias bestimmen;
3. Signer Certificate SHA-256 des alten Alias erfassen;
4. frühere signierte APK oder `base.apk` eines noch Old-Key-signierten Geräts mit `apksigner verify --verbose --print-certs` prüfen;
5. beide SHA-256-Fingerprints müssen exakt identisch sein.

Bei Abweichung: RELEASE1 stoppen.

## Lineage erzeugen

Schematisch:

```text
apksigner rotate --out <lineage-file> \
  --old-signer --ks <old-keystore> --ks-key-alias <old-alias> \
  --new-signer --ks <new-keystore> --ks-key-alias <new-alias>
```

Passwörter werden interaktiv oder über nicht persistente Environment-Variablen übergeben, niemals als Klartext-Literal in Shell-History oder Repo-Dateien.

## Finales Release-Gate

Vor GitHub-Veröffentlichung müssen auf **genau derselben APK** alle Punkte erfüllt sein:

1. `:app:testDebugUnitTest :app:assembleRelease` grün.
2. unsigned Release-APK vor Signierung mit `zipalign -P 16` ausrichten.
3. APK mit altem Signer + neuem Signer + `--lineage` signieren.
4. `apksigner verify --verbose --print-certs` erfolgreich.
5. erwartete Signer-/Lineage-Historie vorhanden.
6. neuer Signer Certificate SHA-256 dokumentiert.
7. APK-SHA-256 dokumentiert.
8. `zipalign -c -P 16 -v 4` erfolgreich.
9. `adb install -r` auf mindestens einem vorhandenen Old-Key-Gerät erfolgreich; lokale Daten/API-Keys bleiben erhalten.
10. Runtime `PAGE_SIZE=16384` / `memoryPageSizeBytes=16384` erneut bestätigt.
11. Kernsmoke auf Release-APK: Start/AccessGate, Current Location, Departure First Paint, mindestens ein Sortierprofil, ORS mit gültigem Nutzer-Key, RoutePlanner-Grundpfad, Settings/Community-Footer.
12. Keine App-FATAL-/ANR-Regression.
13. GitHub Release Tag exakt `v1.1.0-b155`.
14. Release Asset ist exakt die verifizierte APK.

## Folge-Releases

Nach erfolgreicher Rotation ist der neue Community-Key der aktive Signer. Die Signing-Certificate-Lineage wird bei direkten APK-Releases weitergeführt, solange Updates von Installationen aus der alten Signer-Historie unterstützt werden sollen.

Der alte Keystore bleibt archiviert und gesichert.

## Nicht-Ziele

- kein automatischer APK-Installer in der App
- kein neuer Produktbuild
- kein neuer Runtime-Dienst
- keine GitHub-Signing-Secrets im ersten Schritt
- kein automatisierter Tag-Release-Workflow als Voraussetzung für RELEASE1
- keine Änderung am Update-Checker-Vertrag
- keine Änderung an Build-155-Features
- keine Deinstallation als Workaround für einen Rotations-/Signaturfehler

## Stop-Regel

Ohne Old-Key-Fingerprint-Match, Backups beider Keystores und der Lineage, erfolgreiche `apksigner`-Verifikation, In-place-Update, 16-KB-Gate und Realgeräte-Smoke wird kein öffentlicher APK-Release erstellt.
