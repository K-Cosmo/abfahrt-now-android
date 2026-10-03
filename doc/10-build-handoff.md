# Build-Handoff

## Aktuell: v1.1.0 Build 151 (`versionCode 1510`) — Community identity accepted

Build 150 ist technisch accepted und in `main` integriert. REPO1 bleibt die abgeschlossene Repository-/Governance-Baseline.

### Build-151-Scope

Build 151 schließt die sichtbare Lücke zwischen öffentlicher Community-Identität und historischer In-App-Zuordnung:

- `versionCode = 1510`, `versionName = 1.1.0`.
- Settings verwendet einen separaten `CommunityFooter`.
- sichtbarer Hinweis: unabhängige/unoffizielle Community-App; keine Zugehörigkeit zu abfahrt.now.
- abfahrt.now bleibt als Transitdaten-/API-Quelle sichtbar.
- Community-Projektlink öffnet `https://github.com/K-Cosmo/abfahrt-now-android`.
- Privacy/Terms-Links bleiben externe abfahrt.now-Links, sind aber ausdrücklich als **API**-Privacy/Terms beschriftet.
- sechs neue Texte liegen in allen 22 gebündelten Locale-Sets.
- bestehender API-Feedback- und Key-Anforderungsflow bleibt provider-spezifisch und unverändert.
- keine Änderung an Departure-/Routinglogik, ORS, Photon, `/trips`, Persistenz, Credentials, Update-Checker oder MapLibre.

### Acceptance / Evidence

Der Nutzer-Screenshot des realen Build-151-Settings-Footers bestätigt die sichtbare Community-Abgrenzung, Datenquellenkennzeichnung, GitHub-CTA, API-Rechtslink-Beschriftung und Versionsanzeige `Build 151`.

Android CI #42 führt erstmals dauerhaft den vollständigen kombinierten Gate aus:

```text
./gradlew :app:testDebugUnitTest :app:assembleDebug :app:assembleRelease --no-daemon
```

Ergebnis:

```text
BUILD SUCCESSFUL in 3m 47s
106 actionable tasks: 106 executed
```

Damit sind Locale-/Static-/Governance-Gates, Unit-Tests, Debug-Build und Release/R8-Build auf demselben PR-Stand grün. F-DOC1-015/B-COMMUNITY-001 ist geschlossen; der technische Community-Release-Blocker ist beseitigt.

### CI-Regel ab Build 151

Der Release-/R8-Build gehört künftig direkt zum normalen Android-CI-Gate. Ein separates manuelles `assembleRelease` ist nur noch nötig, wenn gezielte lokale Release-Evidence verlangt wird.

## Als Nächstes: Build 152 UI/UX-Paket

1. Onboarding-Fließtext linksbündig und sinnvoll in Pflicht-Key/optionalen ORS-Abschnitt gliedern.
2. RoutePlanner-Start/Ziel-Kopf in einen gemeinsamen kompakten Container überführen; vorhandene Such-/Swap-/Saved-Places-/`/trips`-Logik erhalten.
3. API-Key-Onboarding-Flash beim Start einer bereits eingerichteten App beseitigen. Konkreter Befund: `stateIn(..., AppPreferences())` liefert einen künstlichen leeren Initialzustand; `preferencesLoaded` wird derzeit dadurch zu früh geöffnet. Gate künftig erst nach echter `prefsRepo.preferencesFlow`-Emission.
4. nicht mehr genutzten historischen `AppFooter`-Deadcode entfernen.

AB-018 Startup/Main-Thread-Messung folgt separat als **Build 153**.

## Lokaler Workspace

Android Studio öffnet den Repository-Root:

```text
D:\Android\abfahrt-now-android
```

Standard-Buildpfad lokal:

```text
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug :app:assembleRelease
```

`local.properties`, Build-Ausgaben, Keystores und rohe Runtime-Evidence bleiben lokal und werden nicht committed.
