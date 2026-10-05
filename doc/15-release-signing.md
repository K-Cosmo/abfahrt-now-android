# Release-Signing und APK-Veröffentlichung

Dieses Dokument ist die normative Release-Signing-Regel für direkte GitHub-APK-Releases der AbfahrtApp.

## Ausgangslage RELEASE1

Es existieren bereits drei Installationen, die mit einem älteren privaten Release-Key signiert sind. Das Zertifikat dieses alten Keys enthält historische Riles-Tech-/Leonard-Scharf-Metadaten und soll für neue Community-Releases nicht als dauerhafte öffentliche Signer-Identität fortgeführt werden.

Wichtig: Die X.509-Subject-/Issuer-Metadaten bestimmen nicht technisch die Eigentümerschaft der Android-App. Für die Android-Update-Fähigkeit zählt die kryptografische Signer-Historie. Wegen der irreführenden historischen Attribution wird der Key trotzdem kontrolliert rotiert, statt ihn als neuen Community-Standard weiterzuführen.

## Grundsatz

RELEASE1 rotiert vom **alten Signer** auf einen **neuen neutralen Community-Signer**, ohne die Update-Kette der vorhandenen Installationen zu brechen.

Die App hat `minSdk = 34`. Android unterstützt APK Signature Scheme v3.1 und Signing-Key-Rotation auf Android 13/API 33 und höher. RELEASE1 verwendet deshalb eine `apksigner`-Signing-Certificate-Lineage `old -> new` und den Standard-Rotationspfad für API 33+.

Der alte private Key bleibt dauerhaft sicher verwahrt, weil er Bestandteil der Signer-Historie ist und zur Rekonstruktion/Prüfung der Lineage benötigt werden kann. Der neue private Key wird nach RELEASE1 der normale aktive Signer für künftige Releases. Beide Keystores und die Lineage liegen außerhalb des Repository-Workspaces und erhalten getrennte Backups.

## Neuer Community-Key

Empfehlung für RELEASE1:

- Algorithmus: RSA
- Schlüsselgröße: 4096 Bit
- Keystore: JKS
- Alias: `abfahrt-now-community`
- Gültigkeit: mindestens 10.000 Tage
- neutraler Subject, z. B. `CN=Abfahrt Now Community, C=DE`
- kein Firmenname und keine Person als vermeintlicher App-Eigentümer, sofern das nicht tatsächlich der dauerhaften Projektidentität entspricht

Zertifikatsmetadaten und Fingerprints dürfen öffentlich sein. Private Keydaten und Passwörter dürfen niemals öffentlich werden.

## Alte Signer-Identität zuerst verifizieren

Vor Rotation wird der alte Keystore gegen eine bereits installierte bzw. früher veröffentlichte APK geprüft:

1. alten Keystore mit `keytool -list -v` prüfen;
2. echten alten Alias und Zertifikat-SHA-256 erfassen;
3. eine alte, sicher mit diesem Key signierte APK mit `apksigner verify --verbose --print-certs` prüfen; alternativ `base.apk` von einem der drei bestehenden Geräte ziehen;
4. Zertifikat-SHA-256 von Keystore und APK müssen exakt übereinstimmen.

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
6. für den vorhandenen Android-14+-Support wird keine niedrigere `--rotation-min-sdk-version` erzwungen; der Standardpfad zielt auf API 33+.

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

## Verifikation vor Veröffentlichung

Ein GitHub-APK-Release ist erst zulässig, wenn **genau das hochzuladende APK** erfolgreich geprüft wurde:

1. `apksigner verify --verbose --print-certs`
2. alter und neuer Signer/Lineage wie erwartet vorhanden
3. neuer Signer Certificate SHA-256 dokumentiert
4. APK-Datei-SHA-256 dokumentiert
5. `zipalign -c -P 16 -v 4`
6. reales `adb install -r` auf mindestens einem der drei bestehenden Old-Key-Geräte erfolgreich
7. Einstellungen/API-Keys bleiben bei diesem In-place-Update erhalten
8. Runtime `PAGE_SIZE=16384` und `memoryPageSizeBytes=16384`
9. Kernsmoke ohne FATAL/ANR

Ein Signaturfehler darf **nicht** durch Deinstallation umgangen werden. Ein fehlgeschlagenes `adb install -r` ist ein Stop-Signal für RELEASE1.

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
