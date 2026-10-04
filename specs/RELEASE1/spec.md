# RELEASE1 Specification — first signed GitHub APK

## Ziel

RELEASE1 veröffentlicht den akzeptierten Build 155 (`versionCode = 1550`, `versionName = 1.1.0`) erstmals als dauerhaft signierte GitHub-Release-APK unter Tag `v1.1.0-b155`.

RELEASE1 ist **Release Engineering**, kein neuer Produktbuild. Es ändert keine fachliche Runtime-Semantik und erhöht weder `versionCode` noch `versionName`.

## Baseline

- akzeptierter Produktstand: Build 155
- `applicationId = now.abfahrt.transit`
- `minSdk = 34`, `compileSdk = 37`, `targetSdk = 37`
- Release/R8 ist bereits CI-grün
- 16-KB-Readiness wurde in Build 130 real akzeptiert; RELEASE1 muss sie auf dem final signierten Artefakt erneut als Release-Gate prüfen
- Update-Checker erwartet Tag-Schema `v<versionName>-b<human build>`; RELEASE1 verwendet `v1.1.0-b155`

## Signing-Grundsätze

1. Ein dauerhaftes Release-Keypair wird genau einmal erzeugt und für alle künftigen direkten APK-Updates beibehalten.
2. Keystore und Passwörter dürfen **niemals** in Git, GitHub Actions Logs, `/doc`, `/evidence/public` oder anderen öffentlichen Artefakten landen.
3. Der Keystore liegt außerhalb des Repository-Workspaces und erhält mindestens ein separates Backup.
4. Der öffentliche Repository-Code darf nur die **Mechanik** der Signing-Konfiguration enthalten, niemals Werte/Secrets.
5. Die bestehende normale Android-CI bleibt credential-frei und muss `assembleRelease` weiterhin ohne Signing-Secrets ausführen können.
6. Wenn eine Release-Signierung ausdrücklich angefordert wird, muss eine unvollständige Signing-Konfiguration hart fehlschlagen; ein versehentlich als Release-Kandidat behandeltes unsigniertes APK ist nicht zulässig.
7. Signing-Werte dürfen lokal über Gradle-Properties **oder** Environment-Variablen `ABFAHRT_RELEASE_*` bereitgestellt werden. Für den ersten lokalen Release wird die Eingabe der Passwörter als nicht persistente PowerShell-Environment-Variable bevorzugt, damit Passwörter weder im Repo noch in der Shell-History landen.

## Konfigurationsvertrag

Unterstützte Werte:

- `ABFAHRT_RELEASE_STORE_FILE`
- `ABFAHRT_RELEASE_STORE_PASSWORD`
- `ABFAHRT_RELEASE_KEY_ALIAS`
- `ABFAHRT_RELEASE_KEY_PASSWORD`
- `releaseSigningRequired=true` als explizites Release-Gate

Semantik:

- keine Signing-Werte vorhanden: normale CI darf weiterhin einen unsignierten/minifizierten Release-Build erzeugen;
- alle vier Signing-Werte vorhanden: Release-Build verwendet den Release-Key;
- nur Teilmenge vorhanden: Build schlägt fehl;
- `releaseSigningRequired=true` und Signing-Werte fehlen/unvollständig: Build schlägt fehl.

## Key-Material

Empfohlener lokaler Erstaufbau:

- Key-Algorithmus RSA
- Keygröße 4096 Bit
- JKS-Keystore
- Alias `abfahrt-now-release`
- Gültigkeit mindestens 10.000 Tage
- Zertifikats-Subject ohne persönliche Geheimdaten; Community-/Projektbezug genügt

Diese Parameter definieren nicht die Produktidentität; entscheidend für Update-Fähigkeit ist, dass dasselbe private Keypair dauerhaft erhalten bleibt.

## Finales Release-Gate

Vor GitHub-Veröffentlichung müssen auf **genau derselben signierten APK** alle folgenden Punkte erfüllt sein:

1. `:app:testDebugUnitTest` grün.
2. `:app:assembleRelease` mit `releaseSigningRequired=true` grün.
3. `apksigner verify --verbose --print-certs` erfolgreich.
4. Zertifikats-Fingerprint (SHA-256) dokumentiert; private Keydaten niemals dokumentieren.
5. APK-SHA-256 berechnet und für Release Notes festgehalten.
6. 16-KB-Artefaktcheck des signierten APK erfolgreich.
7. Installation auf 16-KB-Realgerät erfolgreich.
8. Runtime `PAGE_SIZE=16384` / `memoryPageSizeBytes=16384` erneut bestätigt.
9. Kernsmoke auf Release-APK: Start/AccessGate, Current Location, Departure First Paint, mindestens ein Sortierprofil, ORS mit gültigem Nutzer-Key, RoutePlanner-Grundpfad, Settings/Community-Footer.
10. Keine App-FATAL-/ANR-Regression.
11. GitHub Release Tag exakt `v1.1.0-b155`.
12. Release Asset ist die verifizierte APK; Release Notes enthalten mindestens Version/Build, SHA-256 und Hinweis auf die erste Release-Key-Signatur.

## Wechsel von Debug zu Release

Die bisherige lokale Debug-App ist mit dem Android-Debug-Key signiert. Der erste Release-Key-Build besitzt eine andere Signatur und kann deshalb nicht als normales `adb install -r` darüber installiert werden.

Für RELEASE1 ist einmalig zulässig/erforderlich:

1. vorhandene API-Keys/Einstellungen bei Bedarf außerhalb der App bereithalten;
2. Debug-App deinstallieren;
3. signierte Release-APK frisch installieren;
4. benötigte API-Keys/Einstellungen neu hinterlegen;
5. vollständigen Release-Smoke durchführen.

Ab RELEASE1 müssen alle künftigen direkten APK-Updates mit demselben Release-Key signiert sein; danach muss `adb install -r` bzw. die normale Paketaktualisierung funktionieren.

## Nicht-Ziele

- kein automatischer APK-Installer in der App
- kein neuer Produktbuild
- kein neuer Runtime-Dienst
- keine GitHub-Signing-Secrets im ersten Schritt
- kein automatisierter Tag-Release-Workflow als Voraussetzung für RELEASE1
- keine Änderung am Update-Checker-Vertrag
- keine Änderung an Build-155-Features

## Stop-Regel

Ohne gesicherten Keystore-Backup-Pfad, erfolgreiche `apksigner`-Verifikation, 16-KB-Gate und Realgeräte-Smoke wird kein öffentlicher APK-Release erstellt.