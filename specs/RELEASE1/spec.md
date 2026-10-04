# RELEASE1 Specification — first signed GitHub APK

## Ziel

RELEASE1 veröffentlicht den akzeptierten Build 155 (`versionCode = 1550`, `versionName = 1.1.0`) erstmals als öffentlichen GitHub-APK-Release unter Tag `v1.1.0-b155`.

Die App ist bereits auf drei realen Geräten mit einem vorhandenen privaten Release-Key installiert. RELEASE1 erzeugt deshalb **keinen neuen Key**, sondern übernimmt genau diesen bestehenden Signing-Key als dauerhafte Release-Identität.

RELEASE1 ist **Release Engineering**, kein neuer Produktbuild. Es ändert keine fachliche Runtime-Semantik und erhöht weder `versionCode` noch `versionName`.

## Baseline

- akzeptierter Produktstand: Build 155
- `applicationId = now.abfahrt.transit`
- `minSdk = 34`, `compileSdk = 37`, `targetSdk = 37`
- Release/R8 ist bereits CI-grün
- 16-KB-Readiness wurde in Build 130 real akzeptiert; RELEASE1 muss sie auf dem final signierten Artefakt erneut als Release-Gate prüfen
- Update-Checker erwartet Tag-Schema `v<versionName>-b<human build>`; RELEASE1 verwendet `v1.1.0-b155`
- drei bestehende Installationen bilden die reale Update-Kompatibilitätsbasis für den vorhandenen Release-Key

## Signing-Grundsätze

1. Der bereits vorhandene Release-Key wird unverändert weiterverwendet; ein neuer Key ist für RELEASE1 ausdrücklich ausgeschlossen.
2. Keystore und Passwörter dürfen **niemals** in Git, GitHub Actions Logs, `/doc`, `/evidence/public` oder anderen öffentlichen Artefakten landen.
3. Der vorhandene Keystore liegt außerhalb des Repository-Workspaces und muss mindestens ein separates, lesbares Backup haben.
4. Vor Verwendung muss der Signer Certificate SHA-256 des Keystore-Alias mit dem Zertifikat einer bereits installierten Release-App übereinstimmen.
5. Der öffentliche Repository-Code darf nur die **Mechanik** der Signing-Konfiguration enthalten, niemals Werte/Secrets.
6. Die bestehende normale Android-CI bleibt credential-frei und muss `assembleRelease` weiterhin ohne Signing-Secrets ausführen können.
7. Wenn eine Release-Signierung ausdrücklich angefordert wird, muss eine unvollständige Signing-Konfiguration hart fehlschlagen; ein versehentlich als Release-Kandidat behandeltes unsigniertes APK ist nicht zulässig.
8. Signing-Werte dürfen lokal über Gradle-Properties **oder** Environment-Variablen `ABFAHRT_RELEASE_*` bereitgestellt werden. Für den ersten lokalen Release wird die Eingabe der Passwörter als nicht persistente PowerShell-Environment-Variable bevorzugt.

## Konfigurationsvertrag

Unterstützte Werte:

- `ABFAHRT_RELEASE_STORE_FILE`
- `ABFAHRT_RELEASE_STORE_PASSWORD`
- `ABFAHRT_RELEASE_KEY_ALIAS`
- `ABFAHRT_RELEASE_KEY_PASSWORD`
- `releaseSigningRequired=true` als explizites Release-Gate

Semantik:

- keine Signing-Werte vorhanden: normale CI darf weiterhin einen unsignierten/minifizierten Release-Build erzeugen;
- alle vier Signing-Werte vorhanden: Release-Build verwendet den vorhandenen Release-Key;
- nur Teilmenge vorhanden: Build schlägt fehl;
- `releaseSigningRequired=true` und Signing-Werte fehlen/unvollständig: Build schlägt fehl.

## Key-Material

RELEASE1 definiert **keine neuen kryptografischen Sollparameter** für das vorhandene Keypair. Alias, Store-Typ, Algorithmus, Schlüsselgröße, Subject und Gültigkeit werden aus dem real bestehenden Keystore übernommen.

Entscheidend ist ausschließlich die Identität desselben privaten Keypairs, das bereits die drei installierten App-Instanzen signiert hat.

Vor dem Release:

1. vorhandenen Keystore mit `keytool -list -v` prüfen;
2. tatsächlichen Alias bestimmen;
3. Signer Certificate SHA-256 des Alias erfassen;
4. APK einer bestehenden Installation vom Gerät ziehen und mit `apksigner verify --print-certs` prüfen;
5. beide Signer Certificate SHA-256 müssen exakt identisch sein.

Bei Abweichung: RELEASE1 stoppen; keinen neuen Key erzeugen und nicht durch Deinstallation kaschieren.

## Finales Release-Gate

Vor GitHub-Veröffentlichung müssen auf **genau derselben signierten APK** alle folgenden Punkte erfüllt sein:

1. `:app:testDebugUnitTest` grün.
2. `:app:assembleRelease` mit `releaseSigningRequired=true` grün.
3. `apksigner verify --verbose --print-certs` erfolgreich.
4. Zertifikats-Fingerprint (SHA-256) dokumentiert; private Keydaten niemals dokumentieren.
5. APK-SHA-256 berechnet und für Release Notes festgehalten.
6. 16-KB-Artefaktcheck des signierten APK erfolgreich.
7. `adb install -r` auf mindestens einem bereits mit demselben Release-Key installierten Gerät erfolgreich; lokale Daten bleiben erhalten.
8. Installation/Update auf dem 16-KB-Testgerät erfolgreich.
9. Runtime `PAGE_SIZE=16384` / `memoryPageSizeBytes=16384` erneut bestätigt.
10. Kernsmoke auf Release-APK: Start/AccessGate, Current Location, Departure First Paint, mindestens ein Sortierprofil, ORS mit gültigem Nutzer-Key, RoutePlanner-Grundpfad, Settings/Community-Footer.
11. Keine App-FATAL-/ANR-Regression.
12. GitHub Release Tag exakt `v1.1.0-b155`.
13. Release Asset ist die verifizierte APK; Release Notes enthalten mindestens Version/Build, APK-SHA-256 und Signing-Fingerprint.

## Bestehende Release-Installationen

Die drei vorhandenen Release-Installationen sollen RELEASE1 als normales Update erhalten. Dafür wird die bestehende App **nicht** deinstalliert.

Wenn `adb install -r` oder die normale Android-Paketaktualisierung mit `INSTALL_FAILED_UPDATE_INCOMPATIBLE`/Signaturkonflikt scheitert, ist das ein Stop-Befund. Die Ursache muss über Package-/Signer-Identität geklärt werden.

Nur Geräte, die bislang ausschließlich eine Debug-signierte APK tragen, benötigen weiterhin einmalig Deinstallation/Neuinstallation. Dieser Fall ist getrennt von den drei bereits Release-signierten Geräten.

## Nicht-Ziele

- kein automatischer APK-Installer in der App
- kein neuer Produktbuild
- kein neuer Runtime-Dienst
- kein neuer Release-Key
- keine GitHub-Signing-Secrets im ersten Schritt
- kein automatisierter Tag-Release-Workflow als Voraussetzung für RELEASE1
- keine Änderung am Update-Checker-Vertrag
- keine Änderung an Build-155-Features

## Stop-Regel

Ohne Fingerprint-Match zwischen bestehendem Keystore und real installierter Release-App, gesicherten Keystore-Backup-Pfad, erfolgreiche `apksigner`-Verifikation, In-place-Update, 16-KB-Gate und Realgeräte-Smoke wird kein öffentlicher APK-Release erstellt.