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

### Acceptance

Automatisch:

```text
./gradlew :app:testDebugUnitTest :app:assembleDebug
```

plus alle bestehenden Static-/Governance-/Locale-Gates in GitHub Actions.

Runtime:
- ohne verfügbares Release bzw. bei fehlender Verbindung normale Kernfunktion ohne Update-Fehler;
- E2E mit älterem Build + höherem Release-Tag: Hinweis erscheint und „Update öffnen“ führt zur festen GitHub-Release-Seite;
- gleiche/neuste Buildnummer: kein Hinweis.

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
