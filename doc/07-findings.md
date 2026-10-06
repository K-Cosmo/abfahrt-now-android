# Findings Register

Statuswerte: `open`, `pending evidence`, `mitigated`, `closed`.

Diese Datei hält **aktuelle bzw. noch entscheidungsrelevante Findings**. Abgeschlossene historische Build-Details liegen in [`05-regression-ledger.md`](05-regression-ledger.md) und [`CHANGELOG.md`](CHANGELOG.md); reale Belege gehören nach `/evidence`.

| ID | Prio | Status | Befund / Konsequenz |
|---|---|---|---|
| F-DOC1-001 | P0 | closed | Build-122-Codefixes für AB-047/050/051 wurden feldseitig abgenommen; sichtbare Provider-IDs/Alias-Dopplung traten im geprüften Verlauf nicht erneut auf. |
| F-DOC1-002 | P0 | closed | `nearestDist + 20 m` ist bewusste technische Toleranz gegen GPS-/Stationspunkt-/Steigabweichungen und erweitert den Nutzer-Radius nicht. Entscheidung D-040. |
| F-DOC1-003 | P0 | closed Build 123 | Runtime-Base-URL auf `https://api.heigit.org/openrouteservice/` migriert; deprecated Host entfernt. |
| F-DOC1-004 | P0 | closed | Projekt-OpenAPI bestätigt die verwendeten Kernfelder; kein Contract-Gap im produktiven Departure-Kernpfad. |
| F-B124-001 | P0 | closed Build 124 | Numerische Provider-Stop-ID wurde im realen U6-Fall nicht mehr sichtbar; technische ID bleibt nur für interne Requests/Zuordnung. |
| F-DOC1-005 | P1 | closed Build 125 | API-Key-Migration und Keystore-geschützte Speicherung real bestätigt. |
| F-DOC1-006 | P1 | closed Build 132 | Android-17/API-37-Toolchain und reale HTTPS-Kernpfade erfolgreich bestätigt. |
| F-DOC1-007 | P1 | closed Build 130 | 16-KB-Readiness abgenommen: Release-Artefakt-Audit, AAB `PAGE_ALIGNMENT_16K` und echter Runtime-Smoke mit Page Size 16384. |
| F-DOC1-008 | P1 | closed REPO1 | Vollständiger Gradle-9.6.0-Wrapper inklusive `gradle-wrapper.jar` ist eingecheckt. Lokaler Windows-Nachweis und GitHub Actions mit direkter Nutzung des committed Wrappers sind grün; kein Bootstrap-Schritt mehr erforderlich. |
| F-DOC1-009 | P2 | open | `DepartureViewModel.kt` bleibt ein großer Wartungshotspot. Refactoring nur problemgetrieben und in kleinen Schritten; kein Big-Bang-Refactor. |
| F-DOC1-010 | P2 | closed REPO1 | Paralleler Root-`CHANGELOG.md` und Legacy-`/docs` sind entfernt. Normative Historie bleibt ausschließlich `/doc/CHANGELOG.md`; `/doc` ist die einzige Policy-/Spec-Wahrheitsfläche. |
| F-DOC1-011 | P2 | open | `::N` in Provider-IDs wirkt in VBB-Daten wie Stop-Point/Steig/Plattform, ist aber nicht verbundübergreifend bestätigt und darf nicht als universelle Semantik genutzt werden. |
| F-DOC1-012 | P2 | closed Build 138 | `/trips` ist im RoutePlanner produktiv genutzt; Contract- und Runtime-Evidence liegen vor. |
| F-DOC1-013 | P2 | observed Build 134 | `Station.walkSeconds`, `/journey`, `regionBounds` und `routingBounds` sind im Contract vorhanden; Nutzung bleibt separat zu entscheiden. `walkSeconds` zeigte im geprüften Pfad reale Abdeckung, ersetzt ORS aber nicht pauschal. |
| F-DOC1-014 | P2 | closed 06.10.2026 | Projektlizenz entschieden: Repository-Root enthält die MIT License; README und normative Dokumentation weisen MIT als Projektlizenz aus. Drittanbieter-Komponenten behalten ihre jeweiligen Lizenzen. |
| F-DOC1-015 | P1 | closed Build 151 | Build 151 ersetzt die sichtbare historische Riles-Tech-/Legal-Zuordnung im Settings-Footer durch die unabhängige Community-Identität. Android CI #42 ist inklusive Locale-/Static-Gates, Unit-Tests, Debug-Build und Release/R8-Build grün. |
| F-148-001 | P1 | closed | Der erste Build-148-Overlayversuch ließ den alten Testpfad zurück; korrigiertes Source-Paket überschreibt den Legacy-Test. Reales Gradle-Gate und Runtime-Evidence waren anschließend grün. |
| F-149-001 | P2 | pending evidence | HERE-Standortkarte ist sichtbar wie gewünscht und Nutzer-Screenshot ist positiv. Formales kombiniertes Gradle-/Logcat-Gate bleibt gemäß `/doc/11-test-and-evidence.md` separat zu dokumentieren. |
| F-150-001 | P1 | closed Build 150 | Anonymer, credential-isolierter GitHub-Release-Update-Checker ist technisch abgenommen. Reales E2E bestätigt Release-Erkennung und feste Release-Seite; Release/R8-Build ist grün. |
| F-152-001 | P1 | closed Build 152 | Der frühere Onboarding/API-Key-Kaltstart-Flash ist auf dem eingerichteten Realgerät nicht mehr sichtbar. |
| F-152-002 | P1 | closed Build 152 | Der kurze `Standort erlauben`-Flash trotz bereits erteilter Permission ist nach kausalem Minimalfix nicht mehr sichtbar; Location-Updates starten direkt. |
| F-153-001 | P1 | closed Build 154 | Build 153 lokalisierte einen wiederholbaren 2,59–3,02-s-Warteblock vor dem ersten Core-Request. Build 154 nutzt vorhandene `lastLocation` nur als provisorischen First-Paint-Origin und validiert parallel mit High Accuracy. Drei reale Cold Starts reduzieren `Loading`→Core auf ca. 29/30/25 ms; Fresh-Korrekturen 6/0/8 m bleiben Same-Origin und erzeugen keinen zusätzlichen Core-/ORS-Zyklus. Nutzer bestätigt die drastisch kürzere sichtbare Ladezeit ohne störende Refresh-Unruhe. Der >=200-m-Re-Anchor wurde im Feld nicht reproduziert und wird nicht als real getestet behauptet; bestehende Hard-Reset-Semantik und Policy-Tests schützen diesen Pfad. Dauerhafte Acceptance-Regel: Der Core-Pfad darf bei nutzbarer provisorischer Position **nicht auf** High Accuracy warten; High Accuracy darf bei einem Rennen trotzdem vor dem HTTP-Start eintreffen. |
| F-SORT-001 | P1 | closed Build 155 | Build 155 ersetzt die feste globale Reihenfolge durch drei persistente KIS-Profile: `NEARBY` = Entfernung → Abfahrtszeit → Richtung → Linie (Default), `SOONEST` = Abfahrtszeit → Entfernung → Richtung → Linie und `LINE_GROUPED` = Entfernung → Linie → Abfahrtszeit → Richtung. `DepartureDisplayOrdering` bleibt Single Source of Truth; Profilwechsel refiltern lokal ohne eigenen Loading-State oder direkt gekoppelten `/departures`-/ORS-Zyklus. Settings-UI und Sortierung wurden auf Realgerät positiv bestätigt; die Auswahl bleibt nach `force-stop`/Neustart erhalten. |
| F-ORS-001 | P1 | open | Ein real falsch hinterlegter ORS-Key führte zu HTTP 403 für Matrix und Directions, wurde von der App aber als gespeicherter Wert akzeptiert. Beim Hinterlegen oder Ändern soll vor Übernahme eine kleine ORS-Probe erfolgen. HTTP 401/403 bedeutet: Key nicht akzeptieren bzw. bestehenden gültigen Wert nicht überschreiben. Netzwerkfehler/5xx oder 429 dürfen nicht fälschlich als ungültiger Key klassifiziert werden. |
| F-NAME-001 | P1 | open | Der aktuelle sichtbare App-Name `Abfahrt!` ist identisch mit dem Namen der offiziellen abfahrt.now-Android-App. Für den nächsten Produktbuild soll ein klar unterscheidbarer Community-Name gewählt werden; Package-ID und Signing-Historie müssen für die reine Display-Umbenennung nicht geändert werden. |
| F-REL-001 | P1 | closed RELEASE1 | `v1.1.0-b155` wurde am 06.10.2026 als erstes öffentliches signiertes GitHub-APK-Release veröffentlicht. Old→New-Signing-Lineage, reales In-place-Update, finaler `apksigner`-/`zipalign -P 16`-Gate, finaler APK-SHA-256 und Live-`releases/latest` sind dokumentiert. |

## Aktuelle Konsequenzen

- **Build 154** ist akzeptiert; F-153-001 ist geschlossen. Die bereinigte Evidence liegt unter `/evidence/public/build-154/`.
- **Build 155 / F-SORT-001** ist nach CI #125 und Realgeräte-Smoke accepted. Die drei Sortierprofile funktionieren sichtbar, refiltern lokal und bleiben nach Prozessneustart persistent.
- **F-REL-001 / RELEASE1** ist geschlossen; `v1.1.0-b155` ist öffentlich veröffentlicht.
- **F-NAME-001** ist für den nächsten Produktbuild offen: sichtbaren App-Namen klar von der offiziellen `Abfahrt!`-App differenzieren.
- **F-ORS-001** bleibt als separater Hardening-Punkt erhalten.
- **F-DOC1-014** ist geschlossen: das Projekt steht unter MIT License.
- Neue externe Runtime-Dienste werden vor Integration nach [`14-community-and-service-policy.md`](14-community-and-service-policy.md) bewertet.
