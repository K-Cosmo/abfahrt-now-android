# Build-Handoff

## Aktuell akzeptiert: v1.1.0 Build 156 (`versionCode 1560`) — Abfahrtsradar Identity Migration

Build 156 ist am 06.10.2026 nach automatisierten Gates, realem Build-155→156-In-place-Update, Kernfunktions-Smoke und 16-KB-Runtime-Smoke akzeptiert und anschließend als `v1.1.0-b156` veröffentlicht. `releases/latest` zeigt auf Build 156.

### Build-156-Semantik

- sichtbarer Produktname **Abfahrtsradar**;
- kanonisches Repository `K-Cosmo/abfahrtsradar-android`;
- direkter neuer GitHub-`releases/latest`-Pfad und neue Release-/Community-Links;
- alle 22 Locale-Sets auf die neue sichtbare Community-Identität konvergiert;
- `applicationId = now.abfahrt.transit`, Package/Namespace, Signing-Lineage, DataStore/Keystore und fachliche Departure-/Routing-/ORS-/Photon-/Location-/Sortiersemantik unverändert.

### Acceptance-Evidence 06.10.2026

- Android CI #188 auf PR-Head `054847735011161840da0a28a8bb1c56a82637bc` vollständig grün.
- lokaler Gate `:app:testDebugUnitTest :app:assembleRelease --no-daemon` erfolgreich.
- veröffentlichter Build 155 vor Update per APK-SHA-256 `0FE1A8D7EB8A6038FF4446DD4F696BA37737875982945A60DF860ABE255BD9A5` bestätigt.
- alter Build-155-Updatechecker verarbeitet den Repository-Rename über HTTP 301 erfolgreich.
- signierter Build 156 installiert per `adb install -r`; danach `versionCode=1560`, `versionName=1.1.0`.
- Einstellungen, abfahrt.now-/ORS-Key-Status und Sortierprofil erhalten.
- Nutzer-Smoke: Name Abfahrtsradar, Standort/Abfahrten, ORS und RoutePlanner erfolgreich.
- Build-156-Updatechecker: `update_check_complete ... result=success`.
- kein App-FATAL/ANR im Acceptance-Smoke.
- signierter Kandidat SHA-256 `BC1143C08844E21B569BAB41F192BEFB10F8DACA6F681FAAFA216C908B2C5703`; Community-Signer-Zertifikat unverändert `23283ed09731c3711d5f223f0424323697243000a78cbbf0731e4553946325f8`; `zipalign -c -P 16` erfolgreich.
- 16-KB-Emulator: `PAGE_SIZE=16384`, `memoryPageSizeBytes=16384`, `lib/x86_64/libmaplibre.so` Native-Load `ok`.

Bereinigte Evidence: `/evidence/public/build-156/2026-10-06_acceptance.md`.

### Veröffentlichung v1.1.0-b156

- Runtime-Source-Commit: `6ccec47ce72b3e00cbc302ec65a226a4622b70b6`
- Asset: `abfahrtsradar-v1.1.0-b156.apk`
- Größe: 46.317.530 Byte
- finales APK SHA-256: `53DE476E02271C6545906DEC47D0F3B2E66E9AB2A060EE5542AD1C75E106591B`
- aktiver Signer: `CN=Abfahrt Now Community, C=DE`
- Signer Certificate SHA-256: `23283ed09731c3711d5f223f0424323697243000a78cbbf0731e4553946325f8`
- finales `apksigner verify` und `zipalign -c -P 16 -v 4` grün
- finales Release-Artefakt real 155→156 update-kompatibel und auf 16-KB-x86_64 mit MapLibre-Native-Load `ok`
- GitHub Release öffentlich, weder Draft noch Prerelease; APK + `SHA256SUMS.txt`; `releases/latest` = `v1.1.0-b156`
- Release: https://github.com/K-Cosmo/abfahrtsradar-android/releases/tag/v1.1.0-b156

## RELEASE1 veröffentlicht — 06.10.2026

Build 155 ist nicht mehr nur Release-Kandidat: **`v1.1.0-b155`** ist der erste öffentliche signierte GitHub-APK-Release.

- Source/Tag-Commit: `85df24b280f60e47d813d17aa93f400b22fca787`
- Asset: `abfahrt-now-v1.1.0-b155.apk`
- APK SHA-256: `0FE1A8D7EB8A6038FF4446DD4F696BA37737875982945A60DF860ABE255BD9A5`
- aktiver Signer: `CN=Abfahrt Now Community, C=DE`
- Signer Certificate SHA-256: `23283ed09731c3711d5f223f0424323697243000a78cbbf0731e4553946325f8`
- reales Old-Key→New-Key-In-place-Update erfolgreich
- finales APK mit `apksigner verify` und `zipalign -c -P 16 -v 4` verifiziert
- 16-KB-Runtime-Smoke auf offiziellem 16-KB-x86_64-Emulator grün
- GitHub `releases/latest` zeigt auf `v1.1.0-b155`

Release: https://github.com/K-Cosmo/abfahrtsradar-android/releases/tag/v1.1.0-b155

Der normale Gradle-Release bleibt absichtlich credential-frei/unsigned. Der lokale transparente Signing-Pfad mit Signing-Certificate-Lineage ist für RELEASE1 bewiesen; ein automatisierter signierter CI-Release-Workflow bleibt optionaler Folgeschritt.

## Nächster Produkt-/Hardening-Schritt

Build 156 ist accepted und als `v1.1.0-b156` veröffentlicht. Der nächste **fachliche** Hardening-Block ist F-ORS-001/B-ORS-001.

## Separates Finding: ORS-Key-Probe

F-ORS-001/B-ORS-001 bleibt unabhängig: Beim Hinterlegen/Ändern soll eine ORS-Probe erfolgen; 401/403 dürfen den neuen Key nicht übernehmen. Netzwerkfehler/5xx/429 werden nicht als „Key ungültig“ fehlklassifiziert.

## Lokaler Workspace

Android Studio öffnet den Repository-Root:

```text
D:\Android\abfahrt-now-android
```

Standard-Buildpfad lokal:

```text
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug :app:assembleRelease
```

`local.properties`, Build-Ausgaben, Keystores und rohe Runtime-Evidence bleiben lokal und werden nicht committed. Roh-Logcats enthalten potenziell Standort-/Geräteinformationen und werden nur bereinigt nach `/evidence/public/` übernommen.
