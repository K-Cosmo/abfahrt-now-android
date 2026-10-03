# Build-Handoff

## Aktuell: v1.1.0 Build 151 (`versionCode 1510`) — Community identity in runtime UI

Build 150 ist technisch accepted und in `main` integriert. Der GitHub-Release-Update-Checker hat CI, realen Runtime-E2E und Release-/R8-Build bestanden. REPO1 bleibt die abgeschlossene Repository-/Governance-Baseline.

### Build-151-Scope

Build 151 schließt die sichtbare Lücke zwischen öffentlicher Community-Identität und historischer In-App-Zuordnung:

- `versionCode = 1510`, `versionName = 1.1.0`.
- Settings verwendet einen kleinen separaten `CommunityFooter`, statt `SettingsSheet` weiter aufzublähen.
- sichtbarer Hinweis: unabhängige/unoffizielle Community-App; keine Zugehörigkeit zu abfahrt.now.
- abfahrt.now bleibt als Transitdaten-/API-Quelle sichtbar.
- Community-Projektlink öffnet `https://github.com/K-Cosmo/abfahrt-now-android`.
- Privacy/Terms-Links bleiben externe abfahrt.now-Links, sind aber ausdrücklich als **API**-Privacy/Terms beschriftet und nicht als Rechtsseiten der Community-App dargestellt.
- sechs neue Texte liegen in allen 22 gebündelten Locale-Sets; der bestehende All-XML-Locale-Gate prüft sie mit.
- bestehender API-Feedback- und Key-Anforderungsflow bleibt bewusst provider-spezifisch und unverändert.
- keine Änderung an Departure-/Routinglogik, ORS, Photon, `/trips`, Persistenz, Credentials, Update-Checker oder MapLibre.

### Noch offene Acceptance

```text
./gradlew :app:testDebugUnitTest :app:assembleDebug
./gradlew :app:assembleRelease
```

plus Static-/Governance-/Locale-Gates und realer Footer-Smoke auf Gerät:

1. Community-Hinweis sichtbar und verständlich;
2. GitHub-CTA öffnet das kanonische Projekt;
3. API Privacy/Terms sind als externe Provider-Links erkennbar und öffnen die vorgesehenen abfahrt.now-Seiten;
4. Versionsanzeige zeigt Build 151;
5. keine Regression im normalen Settings-/Key-Flow.

Bis diese Evidence vorliegt, bleibt F-DOC1-015 `pending evidence`; der erste öffentliche Community-App-Release bleibt blockiert.

## Danach

AB-018 Startup/Main-Thread-Performance ist auf **Build 152** verschoben. Erst instrumentieren/messen, danach nur bei belegter Ursache optimieren. Weitere UI/UX-Änderungen werden als eigene kleine Änderungen gegen die Build-151-Baseline geplant, damit Community-Identität und Produkt-UX nicht vermischt werden.

## Lokaler Workspace

Android Studio öffnet den Repository-Root:

```text
D:\Android\abfahrt-now-android
```

Standard-Buildpfad:

```text
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug
```

`local.properties`, Build-Ausgaben, Keystores und rohe Runtime-Evidence bleiben lokal und werden nicht committed.
