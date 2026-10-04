# RELEASE1 Plan

## Phase A — Signing-Konfiguration im Repository

1. `app/build.gradle.kts` so erweitern, dass Release-Signing-Werte aus `ABFAHRT_RELEASE_*` gelesen werden können.
2. Quellen: Gradle-Property oder Environment-Variable gleichen Namens; Environment unterstützt später optional CI-Secrets.
3. Alle vier Werte müssen gemeinsam vorhanden sein. Teilkonfiguration => harter Gradle-Fehler.
4. `releaseSigningRequired=true` erzwingt vollständige Signing-Konfiguration.
5. Ohne Signing-Werte und ohne `releaseSigningRequired` bleibt die heutige credential-freie CI-Fähigkeit erhalten.
6. Wenn Signing konfiguriert ist, muss die angegebene Keystore-Datei existieren; sonst harter Fehler.
7. Kein Secret-Logging.

## Phase B — Repository-Gates

1. vorhandene `.gitignore`-Regeln für `*.jks`, `*.keystore`, `*.p12` beibehalten.
2. keine Beispieldatei mit echten Passwörtern anlegen.
3. öffentliche Release-Signing-Anleitung nur mit Variablennamen und sicheren Eingabemustern; niemals echte Werte.
4. normale Android-CI muss weiterhin erfolgreich sein.

## Phase C — bestehenden Release-Key verifizieren

Es wird **kein neuer Keystore erzeugt**. Die App ist bereits auf drei realen Geräten mit dem vorhandenen Release-Key installiert.

Auf dem Windows-Entwicklungsrechner:

1. vorhandenen Keystore außerhalb des Repository-Workspaces lokalisieren;
2. mit `keytool -list -v -keystore <pfad>` Alias/Certificate-Fingerprint prüfen;
3. mindestens ein separates Backup des bestehenden Keystores verifizieren;
4. als zweite Referenz entweder eine frühere mit demselben Key signierte APK-Datei oder ein Gerät verwenden, das noch eine solche Release-Installation trägt;
5. bei Geräte-Referenz APK per `adb shell pm path now.abfahrt.transit` lokalisieren und per `adb pull` sichern;
6. Referenz-APK mit `apksigner verify --print-certs` prüfen;
7. Signer Certificate SHA-256 von Keystore und Referenz-APK müssen exakt übereinstimmen.

Bei Abweichung wird gestoppt. Kein neuer Key und keine Deinstallation als Workaround.

## Phase D — lokale sichere Übergabe an Gradle

Für RELEASE1 bevorzugt PowerShell-Session-Variablen:

- Pfad und tatsächlicher bestehender Alias dürfen direkt als Environment gesetzt werden.
- Store-/Key-Passwort werden mit `Read-Host -AsSecureString` abgefragt und nur für den laufenden Prozess in Klartext konvertiert.
- keine Passwörter in PowerShell-History, Repo-Dateien oder öffentlichen Logs.

## Phase E — signierter Build

1. Branch/Commit des akzeptierten Build-155-Release-Kandidaten plus RELEASE1-Signing-Mechanik verwenden.
2. `releaseSigningRequired=true` setzen.
3. Unit Tests + Release/R8 bauen.
4. Ergebnis muss eine mit dem bestehenden Release-Key signierte `app-release.apk` sein.
5. Build darf bei fehlender/inkonsistenter Signing-Konfiguration nicht still auf unsigned zurückfallen.

## Phase F — kryptografische und Artefakt-Verifikation

1. `apksigner verify --verbose --print-certs app-release.apk`.
2. Signer Certificate SHA-256 muss mit dem in Phase C bestätigten bestehenden Signer übereinstimmen.
3. Datei-SHA-256 mit PowerShell `Get-FileHash -Algorithm SHA256` berechnen.
4. 16-KB-Alignment mit `zipalign -c -P 16 -v 4` auf derselben APK prüfen.
5. Keine Secret-Inhalte in Evidence übernehmen.

## Phase G — Realgeräte-Release-Smoke

1. Auf mindestens einem der drei bestehenden Release-Geräte **keine Deinstallation** durchführen.
2. Signierte Release-APK mit `adb install -r` als echtes In-place-Update installieren.
3. Paketversion prüfen: `versionCode=1550`, `versionName=1.1.0`.
4. Vorhandene Preferences/API-Keys müssen erhalten bleiben.
5. `adb shell getconf PAGE_SIZE` => `16384` auf dem 16-KB-Testgerät.
6. App starten und `AbfahrtCompat memoryPageSizeBytes=16384` bestätigen.
7. Current Location / erster Departure-State.
8. Sortierprofil und Persistenz prüfen.
9. ORS mit gültigem Nutzer-Key mindestens einmal erfolgreich.
10. RoutePlanner-Grundpfad prüfen.
11. kein FATAL/ANR.

Geräte mit reiner Debug-Signatur können getrennt behandelt werden; der bestehende Release-Updatepfad darf dadurch nicht verwässert werden.

## Phase H — GitHub Release

Erst nach positivem Gate:

1. finale verifizierte APK sprechend kopieren/benennen, z. B. `abfahrt-now-v1.1.0-b155.apk`;
2. Tag/Release `v1.1.0-b155` erstellen;
3. exakt dieses APK als Asset anhängen;
4. Release Notes mit Build-155-Highlights, APK-SHA-256 und Signing-Fingerprint;
5. vermerken, dass derselbe bereits bestehende Release-Key weiterverwendet wird;
6. Update-Checker gegen das echte Release testen.

## Folgeschritt, nicht RELEASE1-Gate

Nach erfolgreichem ersten öffentlichen Release kann ein separater manueller/Tag-basierter GitHub-Actions-Release-Workflow mit Repository-Secrets entworfen werden. Das wird erst getan, nachdem der lokale Signing-/Verify-/In-place-Update-Pfad bewiesen ist.