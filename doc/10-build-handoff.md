# Build-Handoff

## Aktuell akzeptiert: v1.1.0 Build 155 (`versionCode 1550`) — wählbare Abfahrts-Sortierung

Build 155 ist am 04.10.2026 nach vollständigem Android-CI-Gate und Realgeräte-Smoke akzeptiert. Build 154 bleibt die vorherige Performance-Baseline.

### Build-155-Semantik

Persistente Sortierprofile:

- **Nähe zuerst / `NEARBY`** — Entfernung → Abfahrtszeit → Richtung → Linie; Default.
- **Nächste Abfahrt / `SOONEST`** — Abfahrtszeit → Entfernung → Richtung → Linie.
- **Linien bündeln / `LINE_GROUPED`** — Entfernung → Linie → Abfahrtszeit → Richtung; Legacy-Sicht.

Die Auswahl liegt in den Einstellungen direkt nach „Abfahrten pro Richtung“ und vor den Quick-Filtern. `DepartureDisplayOrdering` bleibt einzige Comparator-Quelle. Profilwechsel refiltern den bereits vorhandenen Response-State lokal und verändern nicht die API-, Merge-, Dedup-, ORS-, Location- oder First-Paint-Pipeline.

HERE bleibt profilkonform: In Distanzprofilen wirkt die bestehende effektive Distanz 0; `SOONEST` besitzt keinen globalen HERE-Vorrang vor der Abfahrtszeit.

### Automatisierte Evidence

Android CI #125 auf Runtime-Head `02ac2a6…` ist vollständig grün:

```text
./gradlew :app:testDebugUnitTest :app:assembleDebug :app:assembleRelease --no-daemon
```

Locale-/Governance-/Compatibility-Gates, committed Wrapper, Unit Tests, Debug und Release/R8 sind erfolgreich. Tests decken alle drei Profile, Default/Fallback und die HERE-/SOONEST-Semantik ab.

### Realgeräte-Evidence 04.10.2026

- Settings-Screenshot zeigt die neue Sektion mit allen drei Radio-Profilen; Nutzer bestätigt die Darstellung als passend.
- Nutzer bestätigt die erwartete Sortierung auf der Abfahrtsseite für alle drei Profile.
- Logcat zeigt lokale `AbfahrtFilter`-Ergebnisse für `SOONEST`, `LINE_GROUPED` und `NEARBY` ohne `departure_state_loading`.
- Kein unmittelbar an den Sortierwechsel gekoppelter `/departures`-/ORS-Zyklus wurde beobachtet. Spätere Netzwerkaktivität gehört zum normalen Stable-Refresh; Same-Origin-ORS-Metriken werden wiederverwendet.
- Gewählte Sortierung bleibt nach `force-stop`/Neustart erhalten.
- Keine FATAL-/ANR-/Navigation-/Settings-Regression im Abnahmeumfang.

F-SORT-001 / B-155-001 ist geschlossen. Build 155 ist Release-Kandidat für RELEASE1.

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

Release: https://github.com/K-Cosmo/abfahrt-now-android/releases/tag/v1.1.0-b155

Der normale Gradle-Release bleibt absichtlich credential-frei/unsigned. Der lokale transparente Signing-Pfad mit Signing-Certificate-Lineage ist für RELEASE1 bewiesen; ein automatisierter signierter CI-Release-Workflow bleibt optionaler Folgeschritt.

## Nächster Produkt-/Hardening-Schritt

F-ORS-001/B-ORS-001 ist der nächste getrennte Hardening-Kandidat. RELEASE1 selbst benötigt keinen weiteren Produktbuild.

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
