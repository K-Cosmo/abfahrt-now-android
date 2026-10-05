# RELEASE1 Plan

## Phase A — alten Signer verifizieren

1. alten Keystore mit `keytool -list -v` prüfen.
2. tatsächlichen alten Alias und Certificate SHA-256 erfassen.
3. frühere signierte APK verwenden oder `base.apk` von einem noch Old-Key-signierten Gerät ziehen.
4. Referenz-APK mit `apksigner verify --verbose --print-certs` prüfen.
5. Fingerprints müssen exakt übereinstimmen; sonst Stop.

## Phase B — neuen neutralen Community-Key erzeugen

1. neuen JKS außerhalb des Repository-Workspaces erzeugen.
2. RSA 4096 Bit, Alias `abfahrt-now-community`, lange Gültigkeit.
3. neutraler Subject, empfohlen `CN=Abfahrt Now Community, C=DE`.
4. kein historischer Firmen-/Personenname als vermeintlicher Eigentümer.
5. separates Backup des neuen Keystores anlegen und lesbar prüfen.
6. alten Keystore weiterhin unverändert sichern.

## Phase C — Signing-Certificate-Lineage erzeugen

Mit aktuellem Android-SDK-`apksigner`:

```text
apksigner rotate --out <lineage-file> \
  --old-signer --ks <old-keystore> --ks-key-alias <old-alias> \
  --new-signer --ks <new-keystore> --ks-key-alias <new-alias>
```

Lineage außerhalb des Repos archivieren und separat sichern.

## Phase D — unsigned Release bauen

1. Branch/Commit des akzeptierten Build-155-Release-Kandidaten verwenden.
2. `./gradlew :app:testDebugUnitTest :app:assembleRelease`.
3. Gradle signiert RELEASE1 bewusst nicht selbst.
4. unsigned/minifiziertes Release-APK identifizieren.

## Phase E — alignen und mit Rotation signieren

1. unsigned APK mit `zipalign -P 16 -f 4` in finales Kandidaten-APK überführen.
2. anschließend mit `apksigner sign` signieren:

```text
apksigner sign \
  --ks <old-keystore> --ks-key-alias <old-alias> \
  --next-signer \
  --ks <new-keystore> --ks-key-alias <new-alias> \
  --lineage <lineage-file> \
  <aligned-apk>
```

3. Passwörter interaktiv oder über flüchtige Environment-Variablen übergeben; keine Klartext-Literale in History/Repo.
4. Nach `apksigner sign` wird das APK nicht mehr verändert.

## Phase F — kryptografische und 16-KB-Verifikation

1. `apksigner verify --verbose --print-certs`.
2. erwartete Old→New-Lineage prüfen.
3. neuen Signer Certificate SHA-256 sichern.
4. Datei-SHA-256 mit `Get-FileHash -Algorithm SHA256` berechnen.
5. `zipalign -c -P 16 -v 4` auf derselben APK prüfen.

## Phase G — reales In-place-Update

1. mindestens eines der drei Geräte mit noch vorhandener Old-Key-Installation verwenden.
2. **nicht deinstallieren**.
3. `adb install -r <finale-apk>` muss erfolgreich sein.
4. vorhandene Preferences/API-Keys müssen erhalten bleiben.
5. Signaturkonflikt/`INSTALL_FAILED_UPDATE_INCOMPATIBLE` => Stop und Ursachenanalyse, keine Neuinstallation als Workaround.

## Phase H — Release-Smoke

1. `versionCode=1550`, `versionName=1.1.0`.
2. `PAGE_SIZE=16384` und `memoryPageSizeBytes=16384`.
3. Current Location / Departure First Paint.
4. Sortierprofil + Persistenz.
5. ORS mit gültigem Key.
6. RoutePlanner-Grundpfad.
7. Settings/Community-Footer.
8. kein FATAL/ANR.

## Phase I — GitHub Release

1. finale APK als `abfahrt-now-v1.1.0-b155.apk` bereitstellen.
2. Tag/Release `v1.1.0-b155`.
3. exakt verifizierte APK als Asset.
4. Release Notes mit APK-SHA-256, neuem Signer-Fingerprint und Hinweis auf kontrollierte Key-Rotation mit erhaltener Update-Lineage.
5. Update-Checker gegen echtes Release testen.

## Folgeschritt

Nach erfolgreichem lokalen Rotationsrelease kann ein separater manueller/Tag-basierter GitHub-Actions-Release-Workflow geplant werden. CI-Secrets sind kein RELEASE1-Gate.
