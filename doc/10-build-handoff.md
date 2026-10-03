# Build-Handoff

## Aktuell akzeptiert: v1.1.0 Build 151 (`versionCode 1510`) — Community identity accepted

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

## Build 152 UI/UX-Paket — implementation complete / pending real evidence

Draft-PR #6 (`feature/build152-ui-ux`) enthält inzwischen den vollständigen Build-152-Scope. Build 152 ist **noch nicht accepted** und wird vor der Realgeräte-/UX-Abnahme nicht gemergt.

Implementiert:
1. `versionCode = 1520`, `versionName = 1.1.0`.
2. Onboarding-Fließtext ist linksbündig statt im Blocksatz; Pflicht-Key und optionaler ORS-Hinweis bleiben einfach lesbar.
3. `AccessGateViewModel` ist alleiniger Owner der Startup-Access-Entscheidung. Der nullable Preference-State bleibt geschlossen, bis eine echte Repository/DataStore-Emission vorliegt; erst danach werden `DepartureViewModel` und `RoutePlannerViewModel` erzeugt. Der bestehende `DepartureViewModel.preferences`-State ist damit Feature-/Settings-State und wählt kein Startziel.
4. RoutePlanner-Start/Ziel liegen in einer gemeinsamen kompakten Surface mit Trenner und kleiner Tauschaktion rechts. Die vorhandenen `RouteEndpointField`-/`RouteSearchPanel`-Pfade, Photon, Current location, Home/Work, Swap und `/trips` werden weiterverwendet.
5. Der Settings-Footer ist als ein zusammenhängender Block in den 24-dp-Settings-Rhythmus integriert; die vorherige Kombination aus Root-Spacern + `Arrangement.spacedBy(24.dp)` erzeugt keinen kumulierten Leerraum mehr.
6. Der ungenutzte historische `AppFooter` inklusive alter Riles-Tech-/Legal-Hilfsfunktionen ist entfernt. `CommunityFooter` ist die einzige Runtime-Footer-Implementierung.

### Automatisierte Evidence

- Android CI #60: Access-Gate-Konvergenz vollständig grün — Static/Governance, committed Wrapper, Unit Tests, Debug und Release/R8.
- Android CI #62: vollständiger Implementierungsstand einschließlich RoutePlanner-/Footer-Teil vollständig grün — dieselben Gates inklusive Release/R8.

F-152-001 ist damit **fixed in code / pending evidence**, nicht mehr als offener Architekturkonflikt zu behandeln.

### Vor Abnahme noch erforderlich

1. Eingerichtetes Realgerät mehrfach kalt starten: kein sichtbarer Onboarding-/API-Key-Flash.
2. RoutePlanner: Startsuche, Zielsuche, Current location, Home/Work, Swap und `Route finden` praktisch prüfen.
3. Compact-Width-Smoke: gemeinsamer RoutePlanner-Kopf sowie beide Onboarding-Schritte; keine abgeschnittenen oder unbedienbaren Elemente.
4. Settings bis ORS/Community-Footer scrollen und neuen Abstand visuell akzeptieren.
5. keine neue App-FATAL-/ANR-/Navigation-Signatur im getesteten Lauf.
6. Danach erst Build 152 abschließend als accepted nach `/doc` konvergieren, PR aus Draft nehmen und mergen.

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
