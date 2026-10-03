# Build-Handoff

## Aktuell: v1.1.0 Build 151 (`versionCode 1510`) — Community identity accepted

Build 150 ist technisch accepted und in `main` integriert. REPO1 bleibt die abgeschlossene Repository-/Governance-Baseline. DOC2 konvergiert am 03.10.2026 ausschließlich die Dokumentation auf diesen Stand; App-Source, Ressourcen und Versionierung bleiben unverändert.

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

## DOC2 — Dokumentationskonvergenz

DOC2 behebt ausschließlich den Dokumentationsdrift nach REPO1/Build 150/Build 151. Insbesondere werden Changelog, Decision Log, Test-/Evidence-Historie, Compatibility, Localization, Backlog, Release-Plan und Handoff auf denselben akzeptierten Stand gebracht.

DOC2 ist **kein Runtime-Build**:
- keine App-/Resource-/Gradle-Runtimeänderung;
- kein Versionssprung;
- keine neue Runtime-Abnahme nötig;
- normale Doku-/Governance-Gates bleiben erforderlich.

## Build 152 UI/UX-Paket — in Arbeit

PR #6 (`feature/build152-ui-ux`) enthält den ersten Teil von Build 152. Android CI #52 ist vollständig grün, einschließlich Governance-/Compatibility-Gates, Unit-Tests, Debug- und Release/R8-Build. Build 152 ist dadurch noch nicht accepted.

Bereits im Branch:
1. `versionCode = 1520`.
2. Onboarding-Fließtext linksbündig statt Blocksatz.
3. Access-Gate wartet über eine nullable Preference-Projektion auf eine echte Repository/DataStore-Emission, bevor ein Startziel gerendert wird.

Vor weiterer Runtime-Konvergenz ist **F-152-001** zu klären: Der neue `AccessGateViewModel` projiziert denselben Preference-Flow zusätzlich, während `DepartureViewModel` seinen bisherigen `stateIn(..., AppPreferences())`-/`preferencesLoaded`-Pfad weiterhin enthält. Ziel bleibt eine kleine, nachvollziehbare und eindeutige Readiness-Lösung ohne unbeabsichtigte Schattenlogik.

Danach offen:
1. RoutePlanner-Start/Ziel-Kopf in einen gemeinsamen kompakten Container überführen; vorhandene Such-/Swap-/Saved-Places-/`/trips`-Logik erhalten.
2. ORS→Community-Footer-Abstand reduzieren.
3. nicht mehr genutzten historischen `AppFooter`-Deadcode entfernen.
4. reale Kaltstart-Smokes mit eingerichtetem Key: kein Onboarding/API-Key-Flash.
5. RoutePlanner-Smoke: Start-/Zielsuche, Swap, Home/Work, `Route finden`.
6. Compact-Width-Smoke für RoutePlanner und beide Onboarding-Schritte.
7. Footer visuell abnehmen.
8. erst danach Build 152 abschließend nach `/doc` konvergieren und akzeptieren.

AB-018 Startup/Main-Thread-Messung folgt separat als **Build 153**. Dort gilt weiterhin: erst instrumentieren/messen, nicht vorab optimieren.

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
