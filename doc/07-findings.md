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
| F-DOC1-008 | P1 | closed REPO1 | Vollständiger Gradle-9.6.0-Wrapper inklusive `gradle-wrapper.jar` ist eingecheckt. Lokaler Windows-Nachweis (`gradlew.bat --version`, `:app:testDebugUnitTest :app:assembleDebug`) und GitHub Actions mit direkter Nutzung des committed Wrappers sind grün; kein Bootstrap-Schritt mehr erforderlich. |
| F-DOC1-009 | P2 | open | `DepartureViewModel.kt` bleibt ein großer Wartungshotspot. Refactoring nur problemgetrieben und in kleinen Schritten; kein Big-Bang-Refactor. |
| F-DOC1-010 | P2 | closed REPO1 | Paralleler Root-`CHANGELOG.md` und Legacy-`/docs` sind entfernt. Normative Historie bleibt ausschließlich `/doc/CHANGELOG.md`; `/doc` ist die einzige Policy-/Spec-Wahrheitsfläche. |
| F-DOC1-011 | P2 | open | `::N` in Provider-IDs wirkt in VBB-Daten wie Stop-Point/Steig/Plattform, ist aber nicht verbundübergreifend bestätigt und darf nicht als universelle Semantik genutzt werden. |
| F-DOC1-012 | P2 | closed Build 138 | `/trips` ist im RoutePlanner produktiv genutzt; Contract- und Runtime-Evidence liegen vor. |
| F-DOC1-013 | P2 | observed Build 134 | `Station.walkSeconds`, `/journey`, `regionBounds` und `routingBounds` sind im Contract vorhanden; Nutzung bleibt separat zu entscheiden. `walkSeconds` zeigte im geprüften Pfad reale Abdeckung, ersetzt ORS aber nicht pauschal. |
| F-DOC1-014 | P2 | open | Das öffentliche Community-Repository hat noch keine Projektlizenz. Bis zur Lizenzentscheidung ist es öffentlich einsehbarer Community-Source, aber nicht als Open-Source-Projekt mit allgemeinen Nutzungsrechten zu bezeichnen. |
| F-DOC1-015 | P1 | closed Build 151 | Build 151 ersetzt die sichtbare historische Riles-Tech-/Legal-Zuordnung im Settings-Footer durch die unabhängige Community-Identität. Der Realgeräte-Screenshot bestätigt die sichtbare Darstellung; Repository- und API-Provider-Links sind im `CommunityFooter` fest verdrahtet. Android CI #42 ist inklusive Locale-/Static-Gates, Unit-Tests, Debug-Build und Release/R8-Build grün. |
| F-148-001 | P1 | closed | Der erste Build-148-Overlayversuch ließ den alten Testpfad zurück; korrigiertes Source-Paket überschreibt den Legacy-Test. Reales Gradle-Gate und Runtime-Evidence waren anschließend grün. |
| F-149-001 | P2 | pending evidence | HERE-Standortkarte ist sichtbar wie gewünscht und Nutzer-Screenshot ist positiv. Formales kombiniertes Gradle-/Logcat-Gate bleibt gemäß `/doc/11-test-and-evidence.md` separat zu dokumentieren. |
| F-150-001 | P1 | closed Build 150 | Anonymer, credential-isolierter GitHub-Release-Update-Checker ist technisch abgenommen. Branch-CI ist grün; realer E2E bestätigt: ohne Release kein Dialog, älterer Build erkennt `v1.1.0-b150` und öffnet die feste Release-Seite, gleicher Build zeigt keinen Hinweis. Zusätzlich ist `gradlew.bat :app:assembleRelease` mit R8/Minify erfolgreich (`BUILD SUCCESSFUL in 52s`). |
| F-152-001 | P1 | closed Build 152 | Der frühere Onboarding/API-Key-Kaltstart-Flash ist auf dem eingerichteten Realgerät nicht mehr sichtbar. `AccessGateViewModel` bleibt alleiniger Owner der Start-/Onboarding-Entscheidung und liefert bis zur ersten echten `UserPreferencesRepository.preferencesFlow`-Emission `null`; geschützte Feature-ViewModels entstehen erst danach. |
| F-152-002 | P1 | closed Build 152 | Der erste Build-152-Smoke zeigte einen kurzen `Standort erlauben`-Flash trotz bereits erteilter Berechtigung. Ursache war `PermissionOrIdleContent` im `Idle`-Zweig. Der Minimalfix beendet die Composable bei bereits erteilter Permission sofort. Der erneute Realgeräte-Kaltstart am 04.10.2026 bestätigt: kein Location-Prompt-Flicker mehr; Location-Updates starten direkt. |
| F-153-001 | P1 | open / Build 154 specified | Build-153-Stage-A-Evidence zeigt in drei sauberen vollständig instrumentierten Cold Starts einen wiederholbaren Warteblock von grob 2,6–3,0 s zwischen Departure `Loading` und dem ersten abfahrt.now-Core-Request. Der Current-Location-Pfad wartet vor dem Netzwerk in `resolveCurrentTargetCoordinates()` → `getBestLocation()` zuerst auf `getCurrentLocation(PRIORITY_HIGH_ACCURACY)` und nutzt `lastLocation` erst bei `null`. Der erneute Audit für Build 154 bestätigt: Die Re-Anchor-Regel ist bereits `MOVEMENT_THRESHOLD_M = 200f` (`< 200 m` Same-Origin, `>= 200 m` Hard Reset); es existiert dagegen keine separate normative Zeit-/Accuracy-Freshness-Schwelle für `lastLocation`. `specs/BUILD154/` spezifiziert deshalb einen provisorischen Cold-Start-Origin aus derselben bereits zulässigen Fallback-Quelle plus parallelen High-Accuracy-Fix, ohne neue Magic Number. Die bestehende progressive Core/Add-on/ORS-/Stable-Merge-Semantik bleibt unverändert. |
| F-ORS-001 | P1 | open | Ein real falsch hinterlegter ORS-Key führte zu HTTP 403 für Matrix und Directions, wurde von der App aber als gespeicherter Wert akzeptiert. Beim Hinterlegen oder Ändern eines ORS-Keys soll deshalb vor Übernahme eine kleine ORS-Probe erfolgen. HTTP 401/403 bedeutet: Key nicht akzeptieren bzw. bestehenden gültigen Wert nicht überschreiben und verständlich auf den ungültigen/nicht berechtigten Key hinweisen. Netzwerkfehler/5xx oder 429 dürfen nicht fälschlich als ungültigen Key klassifiziert werden. Keine Keys oder Secret-Inhalte loggen. Separat vom Build-153-Startup-Scope umsetzen. |

## Aktuelle Konsequenzen

- **REPO1** ist als Repository-/Governance-Baseline abgeschlossen; der eingecheckte Gradle-Wrapper ist der Standard-Buildpfad.
- **Build 150** ist technisch accepted und in `main` integriert.
- **Build 151** schließt B-COMMUNITY-001/F-DOC1-015 und bleibt historische akzeptierte Baseline.
- **Build 152** ist nach Android CI #81 und Realgeräte-Smoke accepted; F-152-001 und F-152-002 sind geschlossen.
- **Build 153** ist nach Android CI #94 und finaler Realgeräte-Evidence accepted: drei saubere vollständig instrumentierte Cold Starts, Same-Process-Home→App-Resume, keine App-FATAL-/ANR-/Navigation-Regression im Abnahmeumfang. Stage A grenzt den dominanten Startup-Warteblock ausreichend auf die Current-Location-Auflösung ein; Stage B ist nicht erforderlich.
- **F-153-001 / Build 154**: Spezifikation ist erstellt. Es wird keine neue Location-Freshness-Magic-Number eingeführt. Der bestehende 200-m-Standortvertrag entscheidet über Same-Origin vs. Hard Reset; `lastLocation` darf nur als provisorischer First-Paint-Origin früher genutzt werden, während High Accuracy parallel validiert. Runtime-Code und Evidence stehen noch aus.
- **F-ORS-001** ist als separater Hardening-Punkt erfasst; der ORS-Key-Probe-Fix wird nicht mit der Location-Startup-Optimierung vermischt.
- Neue externe Runtime-Dienste werden vor Integration nach [`14-community-and-service-policy.md`](14-community-and-service-policy.md) bewertet.
