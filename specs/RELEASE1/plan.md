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
3. optional kurze öffentliche Release-Signing-Anleitung nur mit Variablennamen und sicheren Eingabemustern; niemals echte Werte.
4. normale Android-CI muss weiterhin erfolgreich sein.

## Phase C — Key-Erzeugung lokal

Auf dem Windows-Entwicklungsrechner:

1. Secrets-Verzeichnis außerhalb `D:\Android\abfahrt-now-android` anlegen, z. B. `%USERPROFILE%\AbfahrtNow-Secrets`.
2. `keytool -genkeypair` mit RSA-4096, Alias `abfahrt-now-release`, JKS und langer Gültigkeit ausführen.
3. Passwörter interaktiv eingeben, nicht als Klartext in der Kommandozeile.
4. Keystore auf ein zweites, getrenntes Medium/Backup-Ziel kopieren.
5. Backup vor Release real lesbar/verfügbar bestätigen.

## Phase D — lokale sichere Übergabe an Gradle

Für RELEASE1 bevorzugt PowerShell-Session-Variablen:

- Pfad und Alias dürfen direkt als Environment gesetzt werden.
- Store-/Key-Passwort werden mit `Read-Host -AsSecureString` abgefragt und nur für den laufenden Prozess in Klartext konvertiert.
- keine Passwörter in PowerShell-History, Repo-Dateien oder öffentlichen Logs.

## Phase E — signierter Build

1. Branch/Commit des akzeptierten Build-155-Release-Kandidaten verwenden.
2. `releaseSigningRequired=true` setzen.
3. Unit Tests + Release/R8 bauen.
4. Ergebnis muss eine signierte `app-release.apk` sein.
5. Build darf bei fehlender/inkonsistenter Signing-Konfiguration nicht still auf unsigned zurückfallen.

## Phase F — kryptografische und Artefakt-Verifikation

1. `apksigner verify --verbose --print-certs app-release.apk`.
2. Signer Certificate SHA-256 sichern.
3. Datei-SHA-256 mit PowerShell `Get-FileHash -Algorithm SHA256` berechnen.
4. 16-KB-Alignment mit `zipalign -c -P 16 -v 4` auf derselben APK prüfen.
5. Keine Secret-Inhalte in Evidence übernehmen.

## Phase G — Realgeräte-Release-Smoke

1. API-Keys griffbereit halten; Debug-App deinstallieren.
2. Release-APK frisch installieren.
3. Paketversion prüfen: `versionCode=1550`, `versionName=1.1.0`.
4. `adb shell getconf PAGE_SIZE` => `16384`.
5. App starten und `AbfahrtCompat memoryPageSizeBytes=16384` bestätigen.
6. AccessGate/API-Key-Eingabe neu durchlaufen.
7. Current Location / erster Departure-State.
8. Sortierprofil setzen und Persistenz nach Neustart prüfen.
9. ORS mit gültigem Nutzer-Key mindestens einmal erfolgreich.
10. RoutePlanner-Grundpfad prüfen.
11. kein FATAL/ANR.

## Phase H — GitHub Release

Erst nach positivem Gate:

1. finale verifizierte APK sprechend kopieren/benennen, z. B. `abfahrt-now-v1.1.0-b155.apk`;
2. Tag/Release `v1.1.0-b155` erstellen;
3. exakt dieses APK als Asset anhängen;
4. Release Notes mit Build 155 Highlights, APK-SHA-256, Signing-Fingerprint und Installationshinweis für frühere Debug-Tester;
5. Update-Checker gegen das echte Release testen.

## Folgeschritt, nicht RELEASE1-Gate

Nach erfolgreichem ersten lokalen Release kann ein separater manueller/Tag-basierter GitHub-Actions-Release-Workflow mit Repository-Secrets entworfen werden. Das wird erst getan, nachdem der lokale Signing-/Verify-Pfad bewiesen ist.