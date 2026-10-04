# Release-Plan

Dieser Plan beschreibt die **aktuelle Reihenfolge**. Historische Build-Details liegen in [`CHANGELOG.md`](CHANGELOG.md), Regressionen in [`05-regression-ledger.md`](05-regression-ledger.md) und Findings in [`07-findings.md`](07-findings.md).

## REPO1 — abgeschlossen

Das öffentliche Repository ist der kanonische Entwicklungsworkspace. `/doc` ist die einzige normative Dokumentationswurzel, Community-/EU-first-Policy ist dokumentiert, und der vollständige Gradle-9.6.0-Wrapper wird lokal sowie in GitHub Actions direkt und erfolgreich verwendet.

## Build 150 — abgeschlossen

Der GitHub-Release-Update-Checker ist technisch accepted und in `main` integriert: credential-isolierter anonymer Client, 22 Locale-Sets, realer `releases/latest`-E2E und erfolgreicher Release-/R8-Build.

## Build 151 — abgeschlossen

Die Runtime-UI zeigt die unabhängige/unoffizielle Community-Identität. abfahrt.now bleibt als Daten-/API-Quelle sichtbar. Android CI #42 ist inklusive Locale-/Static-/Governance-Gates, Unit-Tests, Debug- und Release/R8-Build grün.

## Build 152 — UI/UX-Konvergenz — accepted 04.10.2026

Startup-Access-Gate ohne API-Key-/Onboarding-Flicker, Permission-Idle-Flicker behoben, kompakter RoutePlanner-Kopf und verdichteter Settings-/Community-Footer. Android CI #81 sowie Realgeräte-Evidence sind grün.

## Build 153 — Startup/Main-Thread-Instrumentierung — accepted 04.10.2026

Build 153 (`versionCode = 1530`) lokalisierte den dominanten wiederholbaren Kaltstart-Warteblock auf die Current-Location-Auflösung vor dem ersten abfahrt.now-Core-Request. Drei saubere Cold Starts ergaben `Loading`→Core ca. 2,72 / 2,59 / 3,02 s. Application/MapLibre, Compose, Preference-Gate und der erste Core-HTTP-Call waren nicht der dominante Block. ORS blieb asynchron nach dem Core.

## Build 154 — Current-Location First-Paint Fast Path — accepted 04.10.2026

Build 154 (`versionCode = 1540`, `versionName = 1.1.0`) schließt F-153-001. Die vorhandene System-`lastLocation` wird auf einem leeren Current-Location-Kaltstart ausschließlich als provisorischer First-Paint-Origin früher genutzt; High Accuracy validiert parallel. Es wurde keine neue Alters-/Accuracy-Magic-Number eingeführt.

Bestehender Standortvertrag bleibt unverändert:
- `< 200 m`: Same-Origin, kein Reload allein wegen der High-Accuracy-Korrektur;
- `>= 200 m`: bestehender Hard-Reset-/Pending-Refresh-Pfad;
- Target-/Generation-Guards und ORS-Cancellation bleiben erhalten;
- API-Dedup-Booster, Direct-stop/Add-ons, app-eigene Filter/Dedup/Sortierung, Stable-Merge und ORS-after-Core bleiben fachlich unverändert.

Acceptance-Evidence:
1. Android CI #101/#106 grün inklusive Governance/Compatibility, Wrapper, Unit Tests, Debug und Release/R8.
2. Drei reale Cold Starts mit vorhandenem `lastLocation`: `Loading`→erster Core-Request ca. **29 / 30 / 25 ms** statt Build-153-Baseline **2,59–3,02 s**.
3. High-Accuracy-Korrekturen: **6 / 0 / 8 m**, damit Same-Origin; kein Korrektur-bedingter Ersatz-Core und kein zusätzlicher ORS-Zyklus beobachtet.
4. Cold 2 zeigt eine unabhängige Core-HTTP-Latenz von ca. 9,16 s. Der Request selbst startete nach ca. 30 ms; Netzwerk-/Provider-Latenz wird deshalb nicht als App-Startup-Regression fehlklassifiziert.
5. Nutzer bestätigt die drastisch verkürzte sichtbare Ladezeit ohne störende Standort-/Refresh-Unruhe.
6. Der >=200-m-Re-Anchor war im Feld nicht praktisch reproduzierbar und wird nicht als real getestet behauptet; Policy-Test und bestehende Hard-Reset-Semantik schützen den Pfad.

Dauerhafte Acceptance-Regel: Mit nutzbarer provisorischer Position darf der Core-Pfad **nicht auf** den High-Accuracy-Fix warten. Der High-Accuracy-Fix darf bei einem Scheduling-Rennen trotzdem vor dem eigentlichen HTTP-Start eintreffen.

Bereinigte Evidence: `/evidence/public/build-154/2026-10-04_acceptance.md`.

## Build 155 — wählbare Abfahrts-Sortierung — nächster Produktbuild

F-SORT-001 / B-155-001 wird als isolierter UI-/Preference-Build umgesetzt. Der heutige Comparator ist zentral in `DepartureDisplayOrdering`; API- und Merge-Pipeline müssen dafür nicht verändert werden.

KIS-Richtung:
- persistente Sortierpräferenz in bestehendem DataStore/`AppPreferences`;
- Settings-Platzierung direkt nach „Abfahrten pro Richtung“ und vor Quick-Filtern;
- wenige verständliche Profile statt frei konfigurierbarer Prioritätenmatrix;
- geplante Profile:
  - **Nähe zuerst**: Entfernung → Abfahrtszeit → Richtung → Linie; vorgesehener neuer Default;
  - **Nächste Abfahrt**: Abfahrtszeit → Entfernung → Richtung → Linie;
  - **Linien bündeln**: Entfernung → Linie → Abfahrtszeit → Richtung; bisheriges Verhalten.
- `DepartureDisplayOrdering` bleibt Single Source of Truth; stabile Tie-Breaker bleiben deterministisch.
- HERE-Semantik muss pro Profil ausdrücklich festgelegt werden, damit „Nächste Abfahrt“ tatsächlich zeitbasiert bleibt und nicht unbemerkt von einem globalen HERE-Vorrang übersteuert wird.

Build 155 erhält Specification → Plan → Tasks → Implementierung → automatisierte Tests → Realgeräte-/UI-Evidence.

## RELEASE1 — erste signierte GitHub-APK nach Build 155

F-REL-001/B-REL-001 ist Release Engineering und kein normaler Produktbuild. Technisch ist `assembleRelease` bereits R8-grün, aber der aktuelle Gradle-Build definiert noch keine dauerhafte Release-Signing-Konfiguration und die CI verwaltet keine Signing-Secrets oder Release-Artefakte.

Ziel: Wenn Build 155 akzeptiert ist, soll **v1.1.0-b155** der erste echte signierte GitHub-APK-Release werden, sofern das Signing-Gate erfolgreich ist.

Vor Veröffentlichung erforderlich:
1. dauerhaftes Release-Keypair/Keystore lokal erzeugen und außerhalb des Repos sicher verwahren/backupen;
2. Signing-Konfiguration nur über private lokale Properties bzw. CI-Environment/Secrets; keine Secrets oder Keystore-Datei im Repo;
3. finale APK signieren und mit `apksigner verify --verbose --print-certs` prüfen;
4. finalen Release-/R8-/16-KB- und Realgeräte-Smoke **auf genau der signierten APK** ausführen;
5. SHA-256 der APK in den Release Notes veröffentlichen;
6. Tag gemäß Update-Checker-Vertrag `v<versionName>-b<human build>` verwenden.

Wichtiger Erstinstallationspunkt: bisherige lokale Debug-Installationen sind mit dem Debug-Key signiert. Die erste Release-Key-APK kann deshalb typischerweise nicht per `adb install -r` über die Debug-App installiert werden; Deinstallation/Neuinstallation löscht lokale App-Daten/API-Keys. Ab der ersten Release-APK muss derselbe Release-Key dauerhaft für alle Updates verwendet werden.

## Separater Hardening-Block — ORS-Key-Probe

F-ORS-001/B-ORS-001 bleibt unabhängig: Beim Hinterlegen/Ändern ORS-Key probeweise validieren; HTTP 401/403 darf den neuen Key nicht persistieren bzw. einen vorhandenen gültigen Key nicht überschreiben. Netzwerkfehler/5xx/429 dürfen nicht als ungültiger Key fehlklassifiziert werden.

## Release-Grundsatz

Kein Build gilt aufgrund von Versionsnummer, Gradle-Sync, erfolgreichem CI allein oder KI-Einschätzung als akzeptiert. Maßgeblich sind die in [`11-test-and-evidence.md`](11-test-and-evidence.md) definierten realen Gates und Evidence.
