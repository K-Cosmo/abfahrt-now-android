# Build-Handoff

## Aktuell: v1.1.0 Build 150 (`versionCode 1500`) — GitHub Release Update Checker

Basis: Build 149 ist die bisherige Runtime-Baseline. REPO1 ist abgeschlossen: das öffentliche Repository `K-Cosmo/abfahrt-now-android` ist der kanonische Workspace, `/doc` die einzige normative Dokumentationswurzel und der vollständige Gradle-9.6.0-Wrapper ist lokal sowie in GitHub Actions grün.

Build 148 ist real abgenommen: Runtime-Evidence bestätigt D-069 mit roher Photon-Query, `lat/lon`-Bias und sinnvoller Provider-Reihenfolge. Build 149 implementiert D-070: HERE ist Standortzustand statt Routingfall. Der Nutzer-Screenshot bestätigt die sichtbare Darstellung; formales Build-/Logcat-Evidence für F-149-001 bleibt separat nachzuführen.

### Build-150-Scope

- `versionCode = 1500`, `versionName = 1.1.0`.
- neuer `GitHubReleaseApi` nutzt ausschließlich `GET repos/K-Cosmo/abfahrt-now-android/releases/latest`.
- GitHub erhält einen **eigenen anonymen OkHttp-/Retrofit-Client**; kein `ApiKeyInterceptor`, kein GitHub-Token, keine abfahrt.now-/ORS-Credentials.
- `UpdateReleasePolicy` akzeptiert ausschließlich Tags `v<semver>-b<build>` und vergleicht die Buildnummer gegen `versionCode = build * 10`.
- die Release-Seite wird nur aus festem Repository + validiertem Tag gebildet; beliebige Remote-URLs werden nicht übernommen.
- `UpdateViewModel` prüft einmal asynchron pro Activity-Lebensdauer. Fehler bleiben still und blockieren den normalen App-Start nicht.
- ein neueres Release zeigt einen kleinen lokalisierten Dialog. Die Nutzeraktion öffnet die Release-Seite im Browser; kein APK-Autodownload, keine stille Installation.
- Update-Texte sind in allen 22 gebündelten UI-Locale-Sets vorhanden. Der Locale-Gate prüft nun alle String-XML-Dateien der echten Sprachverzeichnisse.
- fokussierte Tests schützen Release-Tag/Buildvergleich und die Credential-Isolation des GitHub-Clients.
- keine Änderung an Departure-Sortierung, Photon-Ranking, ORS-Routing, `/trips`, Persistenz oder MapLibre.

### Automatische Evidence

GitHub Actions auf dem vollständig konvergierten Branch ist grün (Android CI Run #32, Commit `2425ef465adc502b04d85d31d422104f3aec544a`):

```text
./gradlew :app:testDebugUnitTest :app:assembleDebug
```

plus bestehende Static-/Governance-/Compatibility-/Locale-Gates.

### Reale Runtime-/Release-E2E-Evidence

Der Updatepfad wurde auf einem realen Android-Gerät mit dem Build-150-Code geprüft:

1. **Kein GitHub Release vorhanden:** temporär lokal `versionCode 1490`; App startet normal und zeigt keinen Update-Dialog.
2. **Neueres Release vorhanden:** metadata-only GitHub Release `v1.1.0-b150`; derselbe Build-150-Code mit temporär `versionCode 1490` zeigt den Update-Hinweis für Build 150. „Update öffnen“ führt auf die erwartete feste Release-Seite `.../releases/tag/v1.1.0-b150`.
3. **Gleiche Buildnummer:** lokaler Test-Hack zurückgesetzt, echter Build 150 / `versionCode 1500` installiert; bei weiterhin vorhandenem `v1.1.0-b150` erscheint kein Update-Hinweis.

Damit sind Tag-/Buildvergleich, reale GitHub-Metadatenabfrage, sichtbare UI-Entscheidung und feste Release-URL im E2E bestätigt. Der Test-Release enthält kein APK/Asset und ersetzt keinen öffentlichen Community-App-Release.

### Noch offen vor vollständiger Build-Acceptance

I-064 verlangt zusätzlich einen Release-Minify-Build. Vor `accepted/closed` ist daher noch auszuführen:

```text
./gradlew :app:assembleRelease
```

Erst bei grünem Release-/R8-Gate wird F-150-001 geschlossen und der finale Build-150-Changelog-Eintrag geschrieben.

## Release-Sperre vor erstem öffentlichen Community-App-Release

F-DOC1-015/B-COMMUNITY-001 bleibt bewusst **außerhalb** von Build 150: die bestehende In-App-About-/Legal-UX enthält noch historische Riles-Tech-/abfahrt.now-Zuordnungen. Diese sichtbare Community-Abgrenzung muss als eigene kleine Produktänderung konvergiert werden, bevor erstmals ein öffentliches Community-App-Release veröffentlicht wird.

## Danach

AB-018 Startup/Main-Thread-Performance bleibt separat. Erst instrumentieren/messen, danach nur bei belegter Ursache optimieren.

## Lokaler Workspace

Android Studio öffnet den Repository-Root, unter Windows:

```text
D:\Android\abfahrt-now-android
```

Der eingecheckte Wrapper ist der Standard-Buildpfad:

```text
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug
```

`local.properties`, Build-Ausgaben, Keystores und rohe Runtime-Evidence bleiben lokal und werden nicht committed.
