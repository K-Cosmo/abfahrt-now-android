# Build-Handoff

## Aktuell akzeptiert: v1.1.0 Build 154 (`versionCode 1540`) — Current-Location First-Paint Fast Path

Build 154 ist am 04.10.2026 nach vollständigem Android-CI-Gate und realer Cold-Start-Evidence akzeptiert. Build 153 bleibt die vorherige Mess-/Diagnostik-Baseline.

### Build-154-Eingriff

Der bestehende Standortvertrag bleibt unverändert:

- 200 m ist die einzige Re-Anchor-Schwelle;
- `< 200 m` = Same-Origin;
- `>= 200 m` = bestehender Hard-Reset-/Pending-Refresh-Pfad;
- keine neue Zeit-/Accuracy-Freshness-Regel für `lastLocation`.

Auf einem **leeren Current-Location-Kaltstart** darf eine vorhandene, nutzbare System-`lastLocation` als provisorischer First-Paint-Origin den bestehenden Core-Pfad sofort freigeben. `PRIORITY_HIGH_ACCURACY` validiert parallel. Fehlt `lastLocation`, bleibt der bisherige High-Accuracy-first-Pfad erhalten.

Die nachgelagerte Pipeline wurde nicht neu entworfen: API-Dedup-Booster, Direct-stop/Add-ons, app-eigene Filter/Dedup/Sortierung, Stable-Merge, Hard Reset und ORS-after-Core bleiben fachlich unverändert. Bei erkanntem Re-Anchor wird ORS für den verworfenen provisorischen Origin nicht neu gestartet bzw. laufendes provisorisches Enrichment abgebrochen.

### Automatisierte Evidence

Android CI #101 und #106 sind auf Build-154-Implementierungs-/Konvergenzständen vollständig grün:

```text
./gradlew :app:testDebugUnitTest :app:assembleDebug :app:assembleRelease --no-daemon
```

Governance/Compatibility, committed Wrapper, Unit Tests, Debug und Release/R8 sind erfolgreich. `CurrentLocationStartupPolicyTest` schützt Eligibility, `<200 m`, exakt/über 200 m und stale Target.

### Realgeräte-Evidence 04.10.2026

Drei saubere Cold Starts mit vorhandener `lastLocation`:

| Lauf | Loading → erster Core-Request | High-Accuracy-Korrektur |
|---|---:|---:|
| Cold 1 | ca. 29 ms | 6 m |
| Cold 2 | ca. 30 ms | 0 m |
| Cold 3 | ca. 25 ms | 8 m |

Build-153-Vergleich: ca. 2,59–3,02 s `Loading`→Core.

Alle drei Korrekturen sind Same-Origin; kein Korrektur-bedingter Ersatz-Core und keine zusätzliche ORS-Runde wurden beobachtet. Cold 2 hatte unabhängig davon etwa 9,16 s Latenz für die erste Core-HTTP-Antwort; der Request selbst war bereits nach rund 30 ms gestartet. Provider-/Netzwerk-Latenz wird daher nicht mit Startup-Latenz vermischt.

Die Nutzerabnahme bestätigt die drastisch verkürzte Ladezeit auch visuell und meldet keine störende Standort-/Refresh-Unruhe.

Der >=200-m-Re-Anchor wurde in dieser Feldrunde nicht praktisch reproduziert. Er wird nicht als real getestet behauptet; der bestehende Hard-Reset-/Pending-Refresh-Pfad und Policy-Tests schützen diesen Fall.

Dauerhafte Regel: Bei nutzbarer provisorischer Position darf der Core-Pfad **nicht auf** High Accuracy warten. Bei einem Scheduling-Rennen darf High Accuracy zufällig vor dem tatsächlichen HTTP-Start fertig werden, ohne daraus einen zweiten Core zu erzeugen.

Bereinigte Evidence: `/evidence/public/build-154/2026-10-04_acceptance.md`.

## Nächster Produktbuild: Build 155 — wählbare Abfahrts-Sortierung

Finding: F-SORT-001 / Backlog: B-155-001.

Aktueller Comparator in `DepartureDisplayOrdering`:

```text
HERE → effektive Entfernung → Linie → Abfahrtszeit → Richtung → stabile Tie-Breaker
```

Feldnutzung zeigt, dass der Alltag häufig besser mit **Entfernung → Abfahrtszeit → Richtung** funktioniert. Gleichzeitig soll die bisherige linienorientierte Sicht erhalten bleiben und eine zeitfokussierte Sicht möglich sein.

KIS-Plan:

1. Persistente Sortierpräferenz in bestehendem `AppPreferences`/DataStore.
2. Einstellungen direkt nach „Abfahrten pro Richtung“ und vor Quick-Filtern.
3. Keine frei kombinierbare Prioritätenmatrix, sondern wenige verständliche Profile:
   - **Nähe zuerst**: Entfernung → Abfahrtszeit → Richtung → Linie; geplanter Default.
   - **Nächste Abfahrt**: Abfahrtszeit → Entfernung → Richtung → Linie.
   - **Linien bündeln**: Entfernung → Linie → Abfahrtszeit → Richtung; bisheriges Verhalten.
4. `DepartureDisplayOrdering` bleibt Single Source of Truth; kein Sortieren in Composables und keine Änderung an API-/Merge-/ORS-Pipeline.
5. HERE-Semantik je Profil explizit spezifizieren. Ein Profil „Nächste Abfahrt“ darf nicht durch einen globalen HERE-Vorrang faktisch wieder entfernungsdominiert werden.

Build 155 beginnt wieder mit Specification → Plan → Tasks vor Runtime-Code.

## RELEASE1 — erste signierte GitHub-APK

Nach positiver Build-155-Abnahme ist **v1.1.0-b155** der vorgesehene erste echte signierte GitHub-APK-Release, sofern das Signing-Gate erfolgreich ist.

Aktueller Stand:
- `assembleRelease` inklusive R8 funktioniert;
- es gibt noch keine projektdefinierte Release-`signingConfig`;
- Android CI bleibt aktuell credential-frei und lädt keine signierte APK hoch;
- der bisherige GitHub-Release `v1.1.0-b150` ist nur ein metadata-only Update-Checker-Test ohne APK.

Vor RELEASE1 erforderlich:
1. dauerhaftes Android-Release-Keypair/Keystore lokal erzeugen und außerhalb des Repos sicher verwahren/backupen;
2. Gradle-Signing nur über private lokale Properties bzw. CI-Environment/Secrets;
3. finale APK mit `apksigner verify --verbose --print-certs` prüfen;
4. finalen Release-/R8-/16-KB-/Realgeräte-Smoke auf genau der signierten APK durchführen;
5. SHA-256 veröffentlichen;
6. Release-Tag gemäß vorhandenem Update-Contract `v<versionName>-b<human build>`.

Wichtig für unseren Testbestand: Die bisherigen Debug-APKs sind mit dem Debug-Key signiert. Die erste echte Release-Key-APK lässt sich daher typischerweise nicht als In-place-Update über die Debug-App installieren. Für diesen einmaligen Wechsel ist Deinstallation/Neuinstallation nötig; dadurch gehen lokale Preferences/API-Keys verloren und müssen neu hinterlegt werden. Ab RELEASE1 muss derselbe Release-Key dauerhaft beibehalten werden.

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
