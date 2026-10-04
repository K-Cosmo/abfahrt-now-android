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

## Nächster Schritt: RELEASE1 — erste signierte GitHub-APK

Zielartefakt: **v1.1.0-b155** auf Basis des akzeptierten Build 155.

Aktueller Stand:
- `assembleRelease` inklusive R8 ist grün;
- es gibt noch keine projektdefinierte dauerhafte Release-`signingConfig`;
- Android CI bleibt credential-frei;
- der bisherige GitHub-Release `v1.1.0-b150` war nur ein metadata-only Update-Checker-Test ohne APK.

RELEASE1 muss vor Veröffentlichung erfüllen:

1. dauerhaftes Android-Release-Keypair/Keystore lokal erzeugen;
2. Keystore außerhalb des Repos sicher verwahren und separat sichern;
3. Gradle-Signing nur über private lokale Properties/Environment anbinden; keine Secrets oder Keystore-Datei committen;
4. signierte Release-APK mit `apksigner verify --verbose --print-certs` prüfen;
5. Release-/R8-/16-KB-Smoke auf genau diesem Artefakt;
6. auf Realgerät installieren und Kernpfade prüfen;
7. SHA-256 der APK berechnen und in GitHub Release Notes veröffentlichen;
8. Release-Tag `v1.1.0-b155`.

Wichtig: Die aktuelle Testinstallation ist eine Debug-Signatur. Der erste Wechsel auf den Release-Key erfordert eine Deinstallation/Neuinstallation; lokale Preferences und API-Keys gehen dabei verloren. Ab dem ersten signierten Release muss derselbe Release-Key dauerhaft für alle Updates beibehalten werden.

Ein automatisierter signierter GitHub-Release-Workflow ist optionaler Folgeschritt. Für RELEASE1 bevorzugen wir zunächst den transparenten lokalen Signing-/Verify-Pfad, bevor Signing-Secrets in CI eingeführt werden.

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
