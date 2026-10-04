# Release-Signing und APK-Veröffentlichung

Dieses Dokument ist die normative Release-Signing-Regel für direkte GitHub-APK-Releases der AbfahrtApp.

## Grundsatz

Ab RELEASE1 wird jede öffentlich als Release angebotene APK mit demselben dauerhaften privaten Android-Release-Key signiert, der bereits für die aktuell auf realen Geräten installierten Release-Builds verwendet wurde. Der Verlust oder Austausch dieses Keys unterbricht die direkte Update-Kette für bereits installierte APKs.

Der private Key, Keystore und Passwörter sind **kein Repository-Inhalt** und **keine Evidence**.

## Release-Key

Für RELEASE1 wird **kein neuer Key erzeugt**. Es wird der bereits vorhandene Release-Keystore weiterverwendet, mit dem die App heute auf drei realen Geräten installiert ist. Die Identität dieses bestehenden privaten Keypairs ist damit ein dauerhafter Release-Invariant.

Vor dem ersten öffentlichen GitHub-APK-Release muss nachgewiesen werden, dass:

1. der vorhandene Keystore lesbar ist;
2. der korrekte Alias bekannt ist;
3. das Zertifikat dieses Alias exakt dem Signer-Zertifikat einer bereits signierten Release-APK entspricht;
4. ein unabhängiges Backup des vorhandenen Keystores existiert und lesbar ist.

Alias, Store-Typ, Algorithmus, Schlüsselgröße und historische Gültigkeit werden **nicht** nachträglich auf neu erfundene Sollwerte umgestellt. Für die Update-Fähigkeit ist die Identität desselben privaten Keypairs entscheidend.

Zertifikatsmetadaten und Fingerprints dürfen öffentlich sein. Private Keydaten und Passwörter dürfen niemals öffentlich werden.

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

## Signer-Identität vor dem Build prüfen

Vor dem ersten öffentlichen Release muss der Signer-Fingerprint des vorhandenen Keystores gegen eine bereits signierte Release-APK geprüft werden.

Keystore-Seite:

```text
keytool -list -v -keystore <bestehender-keystore> -alias <bestehender-alias>
```

Referenz-APK-Seite: Entweder eine noch vorhandene frühere signierte APK verwenden oder die APK einer noch Release-signierten Installation vom Gerät ziehen und mit `apksigner verify --print-certs` prüfen. Die Signer Certificate SHA-256-Werte müssen identisch sein.

Erst nach diesem Match gilt der vorhandene Keystore als RELEASE1-Key.

## Verifikation vor Veröffentlichung

Ein GitHub-APK-Release ist erst zulässig, wenn **genau das hochzuladende APK** erfolgreich geprüft wurde:

1. `apksigner verify --verbose --print-certs`
2. Signer Certificate SHA-256 dokumentieren
3. APK-Datei-SHA-256 dokumentieren
4. `zipalign -c -P 16 -v 4`
5. In-place-Update auf mindestens einem der bereits mit demselben Key installierten Geräte
6. Installation/Update auf dem 16-KB-Testgerät
7. Runtime `PAGE_SIZE=16384` und `memoryPageSizeBytes=16384`
8. Kernsmoke ohne FATAL/ANR

## Bestehende Release-Installationen

Da die App bereits auf drei Geräten mit dem vorhandenen Release-Key installiert ist, ist für diese Geräte **keine Deinstallation** vorgesehen. RELEASE1 muss dort als normales Update funktionieren.

Acceptance:

```text
adb install -r <signierte-release-apk>
```

muss auf mindestens einem bestehenden Release-Gerät ohne Signaturfehler erfolgreich sein und lokale App-Daten/Preferences erhalten.

Falls Android `INSTALL_FAILED_UPDATE_INCOMPATIBLE` oder einen Signaturkonflikt meldet, wird RELEASE1 gestoppt. Es wird **nicht** deinstalliert, um den Fehler zu umgehen; stattdessen wird zuerst die Signer-Identität geklärt.

Wichtig: Ein Gerät, auf dem während der Entwicklung inzwischen eine Debug-APK installiert wurde, ist für diesen Update-Kompatibilitätstest nicht geeignet. Mindestens eines der drei Geräte muss noch eine mit dem bestehenden Release-Key signierte Installation tragen, oder es muss eine frühere signierte APK-Datei als Fingerprint-Referenz vorliegen.

Geräte, die heute nur eine Debug-Signatur tragen, benötigen weiterhin einmalig Deinstallation/Neuinstallation. Dieser Debug→Release-Sonderfall darf nicht mit den drei bestehenden Release-Installationen verwechselt werden.

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
- Hinweis, dass der bereits bestehende dauerhafte Release-Key weiterverwendet wird

## CI

Die normale Android-CI bleibt credential-frei. Ein späterer signierter GitHub-Actions-Release-Workflow ist optional und wird erst eingeführt, nachdem RELEASE1 lokal erfolgreich mit dem bestehenden Key signiert, verifiziert, als In-place-Update installiert und veröffentlicht wurde.