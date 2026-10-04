# Release-Signing und APK-Veröffentlichung

Dieses Dokument ist die normative Release-Signing-Regel für direkte GitHub-APK-Releases der AbfahrtApp.

## Grundsatz

Ab RELEASE1 wird jede öffentlich als Release angebotene APK mit demselben dauerhaften privaten Android-Release-Key signiert. Der Verlust oder Austausch dieses Keys unterbricht die direkte Update-Kette für bereits installierte APKs.

Der private Key, Keystore und Passwörter sind **kein Repository-Inhalt** und **keine Evidence**.

## Release-Key

Für RELEASE1:

- Algorithmus: RSA
- Schlüsselgröße: 4096 Bit
- Keystore: JKS
- Alias: `abfahrt-now-release`
- Gültigkeit: mindestens 10.000 Tage
- Speicherort: außerhalb des Repository-Workspaces
- mindestens ein separates Backup vor Veröffentlichung

Zertifikatsmetadaten dürfen öffentlich sein. Private Keydaten und Passwörter dürfen niemals öffentlich werden.

## Gradle-Konfiguration

Der Build unterstützt vier externe Werte:

- `ABFAHRT_RELEASE_STORE_FILE`
- `ABFAHRT_RELEASE_STORE_PASSWORD`
- `ABFAHRT_RELEASE_KEY_ALIAS`
- `ABFAHRT_RELEASE_KEY_PASSWORD`

Jeder Wert kann als Gradle-Property oder gleichnamige Environment-Variable bereitgestellt werden.

Regeln:

1. Kein Wert vorhanden: normale CI darf den Release-Build weiterhin credential-frei und unsigned für Compile/R8-Verifikation bauen.
2. Alle vier Werte vorhanden: Release-Build wird mit diesem Key signiert.
3. Nur ein Teil vorhanden: Build muss fehlschlagen.
4. `releaseSigningRequired=true`: vollständige Signing-Konfiguration ist zwingend; fehlt sie, muss der Build fehlschlagen.
5. Existiert der konfigurierte Keystore-Pfad nicht, muss der Build fehlschlagen.
6. Secret-Werte dürfen nicht geloggt werden.

Für einen echten Release-Kandidaten ist `releaseSigningRequired=true` verpflichtend.

## Lokale Secret-Übergabe

Für RELEASE1 werden Passwörter bevorzugt nur in der aktuellen PowerShell-Session gehalten. Sie werden per `Read-Host -AsSecureString` abgefragt und anschließend nur als Prozess-Environment an Gradle weitergegeben.

Passwörter dürfen nicht:

- als Klartext in Git-Dateien stehen;
- als Klartext in Build-Skripten stehen;
- als Literal in der PowerShell-History landen;
- in `/doc` oder `/evidence/public` auftauchen.

## Verifikation vor Veröffentlichung

Ein GitHub-APK-Release ist erst zulässig, wenn **genau das hochzuladende APK** erfolgreich geprüft wurde:

1. `apksigner verify --verbose --print-certs`
2. Signer Certificate SHA-256 dokumentieren
3. APK-Datei-SHA-256 dokumentieren
4. `zipalign -c -P 16 -v 4`
5. Installation auf dem 16-KB-Testgerät
6. Runtime `PAGE_SIZE=16384` und `memoryPageSizeBytes=16384`
7. Kernsmoke ohne FATAL/ANR

## Erster Wechsel von Debug auf Release

Eine mit dem Android-Debug-Key installierte App kann nicht als normales Update durch die erste Release-Key-APK ersetzt werden.

Für RELEASE1:

1. benötigte Nutzer-API-Keys/Einstellungen außerhalb der App griffbereit halten;
2. Debug-App deinstallieren;
3. signierte Release-APK frisch installieren;
4. API-Keys/Einstellungen neu setzen;
5. Release-Smoke durchführen.

Ab dann müssen alle Folge-APKs mit demselben Release-Key signiert werden. Ein reguläres Update muss ohne Deinstallation möglich sein.

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
- Signer Certificate SHA-256
- Hinweis, dass dies die erste APK mit dem dauerhaften Release-Key ist
- Hinweis für frühere Debug-Tester auf die einmalig erforderliche Neuinstallation

## CI

Die normale Android-CI bleibt credential-frei. Ein späterer signierter GitHub-Actions-Release-Workflow ist optional und wird erst eingeführt, nachdem RELEASE1 lokal erfolgreich signiert, verifiziert, installiert und veröffentlicht wurde.