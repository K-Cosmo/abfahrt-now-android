# Release-Signing und APK-Veröffentlichung

Dieses Dokument ist die normative Release-Signing-Regel für direkte GitHub-APK-Releases der AbfahrtApp.

## Ausgangslage RELEASE1

Es existieren bereits drei Installationen, die mit einem älteren privaten Release-Key signiert sind. Das Zertifikat dieses alten Keys enthält historische Riles-Tech-/Leonard-Scharf-Metadaten und soll für neue Community-Releases nicht als dauerhafte öffentliche Signer-Identität fortgeführt werden.

Wichtig: Die X.509-Subject-/Issuer-Metadaten bestimmen nicht technisch die Eigentümerschaft der Android-App. Für die Android-Update-Fähigkeit zählt die kryptografische Signer-Historie. Wegen der irreführenden historischen Attribution wird der Key trotzdem kontrolliert rotiert, statt ihn als neuen Community-Standard weiterzuführen.

## Grundsatz

RELEASE1 rotiert vom **alten Signer** auf einen **neuen neutralen Community-Signer**, ohne die Update-Kette der vorhandenen Installationen zu brechen.

Die App hat `minSdk = 34`. RELEASE1 verwendet eine `apksigner`-Signing-Certificate-Lineage `old -> new`. Der akzeptierte Rotationsnachweis ist **nicht** an die Bedingung `Verified using v3.1 = true` gebunden: Mit Android Build Tools 37.0.0 verifiziert das tatsächlich erzeugte RELEASE1-Kandidaten-APK als APK Signature Scheme v3, trägt den neuen Community-Signer und wurde auf einem Android-14+-Gerät mit noch installiertem alten Release-Signer erfolgreich per `adb install -r` aktualisiert. Dieser reale In-place-Update-Nachweis ist für die AbfahrtApp maßgeblich.

Der alte private Key bleibt dauerhaft sicher verwahrt, weil er Bestandteil der Signer-Historie ist und zur Rekonstruktion/Prüfung der Lineage benötigt werden kann. Der neue private Key wird nach RELEASE1 der normale aktive Signer für künftige Releases. Beide Keystores und die Lineage liegen außerhalb des Repository-Workspaces und erhalten getrennte Backups.

## Neuer Community-Key

Für RELEASE1 real verwendet:

- Algorithmus: RSA
- Schlüsselgröße: 4096 Bit
- Keystore: PKCS12
- Alias: `abfahrt-now-community`
- Subject: `CN=Abfahrt Now Community, C=DE`
- Certificate SHA-256: `23:28:3E:D0:97:31:C3:71:1D:5F:22:3F:04:24:32:36:97:24:30:00:A7:8C:BB:F0:73:1E:45:53:94:63:25:F8`

Zertifikatsmetadaten und Fingerprints dürfen öffentlich sein. Private Keydaten und Passwörter dürfen niemals öffentlich werden.

## Alte Signer-Identität zuerst verifizieren

Vor Rotation wird der alte Keystore gegen eine bereits installierte bzw. früher veröffentlichte APK geprüft:

1. alten Keystore mit `keytool -list -v` prüfen;
2. echten alten Alias und Zertifikat-SHA-256 erfassen;
3. eine alte, sicher mit diesem Key signierte APK mit `apksigner verify --verbose --print-certs` prüfen; alternativ `base.apk` von einem der drei bestehenden Geräte ziehen;
4. Zertifikat-SHA-256 von Keystore und APK müssen exakt übereinstimmen.

Für RELEASE1 wurde bestätigt:

- alter Alias: `abfahrt`
- alter Signer: RSA 2048 Bit
- Certificate SHA-256: `3B:49:8E:F9:B0:EA:98:18:C5:6F:A9:2C:8D:34:57:FA:CA:83:B5:72:F9:DA:83:A0:D2:39:9F:B5:56:05:94:9F`
- Keystore-Zertifikat und historische Release-APK stimmen exakt überein.

Ohne diesen Nachweis wird keine Rotation durchgeführt.

## Signing-Certificate-Lineage

Die Lineage wird mit beiden privaten Schlüsseln erzeugt:

```text
apksigner rotate --out <lineage-file> \
  --old-signer --ks <old-keystore> --ks-key-alias <old-alias> \
  --new-signer --ks <new-keystore> --ks-key-alias <new-alias>
```

Passwörter werden nicht inline in der Shell-History angegeben. `apksigner` darf interaktiv fragen oder `env:<NAME>` verwenden.

Die Lineage-Datei enthält keine privaten Schlüssel, ist aber releasekritischer Zustand und wird zusammen mit den Keystore-Backups sicher archiviert.

## Build- und Signierpfad

Der Gradle-Release-Build bleibt bewusst **unsigned**. Die Rotation wird nicht durch eine einfache Gradle-`signingConfig` modelliert, weil ein nur mit dem neuen Key signiertes APK die bestehenden Old-Key-Installationen nicht sicher aktualisieren würde.

RELEASE1:

1. `:app:testDebugUnitTest :app:assembleRelease` ausführen;
2. unsigned/minifiziertes Release-APK bestimmen;
3. vor dem Signieren mit `zipalign -P 16` ausrichten;
4. anschließend mit `apksigner sign` und `--lineage` signieren;
5. alter Signer wird als erster Signer, neuer Signer als `--next-signer` angegeben;
6. keinen künstlich niedrigeren Rotations-SDK-Wert erzwingen; der reale Installationspfad auf dem unterstützten Android-14+-Gerät ist das Akzeptanzkriterium.

Schematisch:

```text
apksigner sign \
  --ks <old-keystore> --ks-key-alias <old-alias> \
  --next-signer \
  --ks <new-keystore> --ks-key-alias <new-alias> \
  --lineage <lineage-file> \
  <aligned-apk>
```

Danach dürfen keinerlei Änderungen mehr am APK erfolgen.

## RELEASE1-Kandidaten-Evidence

Der auf RELEASE1-Branch `fe7d8e5` erzeugte Kandidat wurde erfolgreich gebaut, 16-KB-ausgerichtet, signiert und verifiziert.

`apksigner verify --min-sdk-version 34 --verbose --print-certs` meldete:

- `Verifies`
- APK Signature Scheme v3: `true`
- v3.1: `false`
- genau einen aktuellen Signer: `CN=Abfahrt Now Community, C=DE`
- neuer Certificate SHA-256: `23283ed09731c3711d5f223f0424323697243000a78cbbf0731e4553946325f8`

Das signierte Kandidaten-APK besitzt Datei-SHA-256:

`9A165BCABC74CC40600A96BF6B7D83116B20FE9456EFF7344D39A4A26223BC3F`

`zipalign -c -P 16 -v 4` war auf genau diesem signierten APK erfolgreich.

Auf einem realen Gerät wurde vor dem Update die installierte `base.apk` gezogen und als alter Release-Signer mit exakt dem bekannten alten Zertifikat verifiziert. Anschließend war `adb install -r` mit dem rotierenden Kandidaten erfolgreich (`Success`). Danach meldete das Paket `versionCode=1550`, `versionName=1.1.0`.

Damit ist die **kryptografische Update-Kompatibilität alt -> neuer Community-Signer real bewiesen**. Noch separat zu bestätigen bleiben Daten-/Preferences-Erhalt, 16-KB-Runtime und Kernsmoke auf diesem Endnutzergerät.

## Verifikation vor Veröffentlichung

Ein GitHub-APK-Release ist erst zulässig, wenn **genau das hochzuladende APK** erfolgreich geprüft wurde:

1. `apksigner verify --verbose --print-certs`
2. neuer Community-Signer wie erwartet aktiv
3. neuer Signer Certificate SHA-256 dokumentiert
4. APK-Datei-SHA-256 dokumentiert
5. `zipalign -c -P 16 -v 4`
6. reales `adb install -r` auf mindestens einem bestehenden Old-Key-Gerät erfolgreich
7. Einstellungen/API-Keys bleiben bei diesem In-place-Update erhalten
8. Runtime `PAGE_SIZE=16384` und `memoryPageSizeBytes=16384`
9. Kernsmoke ohne FATAL/ANR

Ein Signaturfehler darf **nicht** durch Deinstallation umgangen werden. Ein fehlgeschlagenes `adb install -r` auf einer tatsächlich Old-Key-signierten Installation ist ein Stop-Signal für RELEASE1.

## Folge-Releases

Nach erfolgreicher Rotation wird der neue Community-Key der aktive Release-Signer. Die Signing-Certificate-Lineage muss bei künftigen direkten APK-Releases weitergeführt werden, solange Updates von Installationen aus der alten Signer-Historie unterstützt werden sollen.

Der alte Keystore wird deshalb nicht gelöscht oder absichtlich unbrauchbar gemacht.

## GitHub Release Contract

Tag-Schema bleibt:

```text
v<versionName>-b<human build>
```

Für RELEASE1:

```text
v1.1.0-b155
```

Die Release Notes enthalten mindestens:

- Version und Build
- APK-Datei-SHA-256
- neuer Signer Certificate SHA-256
- Hinweis auf kontrollierte Signing-Key-Rotation mit erhaltener Android-Update-Lineage

## CI

Die normale Android-CI bleibt credential-frei und baut weiterhin das unsigned Release/R8-Artefakt zur Compile-/Shrink-Verifikation. Ein späterer signierter Release-Workflow ist optional und wird erst nach erfolgreichem lokalen RELEASE1-Rotationspfad eingeführt.
