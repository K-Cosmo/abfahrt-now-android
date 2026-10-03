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
| F-DOC1-015 | P1 | open | README/`/doc` definieren die unabhängige Community-Identität, während die bestehende In-App-About-/Legal-UX noch `Built by Riles Tech UG` und abfahrt.now-Rechtslinks enthält. Vor dem ersten Community-Release muss die Runtime-UI diese Abgrenzung eindeutig widerspiegeln; eigene Produktänderung mit Buildnummer/Gate. |
| F-148-001 | P1 | closed | Der erste Build-148-Overlayversuch ließ den alten Testpfad zurück; korrigiertes Source-Paket überschreibt den Legacy-Test. Reales Gradle-Gate und Runtime-Evidence waren anschließend grün. |
| F-149-001 | P2 | pending evidence | HERE-Standortkarte ist sichtbar wie gewünscht und Nutzer-Screenshot ist positiv. Formales kombiniertes Gradle-/Logcat-Gate bleibt gemäß `/doc/11-test-and-evidence.md` separat zu dokumentieren. |
| F-150-001 | P1 | closed Build 150 | Anonymer, credential-isolierter GitHub-Release-Update-Checker ist technisch abgenommen. Branch-CI ist grün; realer E2E bestätigt: ohne Release kein Dialog, älterer Build erkennt `v1.1.0-b150` und öffnet die feste Release-Seite, gleicher Build zeigt keinen Hinweis. Zusätzlich ist `gradlew.bat :app:assembleRelease` mit R8/Minify erfolgreich (`BUILD SUCCESSFUL in 52s`). Die Strip-Warnung für vorhandene Native-Libraries war nicht fatal; sie wurden unverändert paketiert. |

## Aktuelle Konsequenzen

- **REPO1** ist als Repository-/Governance-Baseline abgeschlossen; der eingecheckte Gradle-Wrapper ist der Standard-Buildpfad.
- **Build 150** ist technisch accepted. Der Update-Checker selbst benötigt keine weitere Produktänderung vor Merge.
- **B-COMMUNITY-001 / F-DOC1-015** bleibt Release-Blocker vor dem ersten öffentlichen Community-App-Release und wird nicht still in Build 150 vermischt.
- **Build 151** bleibt für AB-018 Startup/Main-Thread-Instrumentierung reserviert; erst messen, dann optimieren.
- Neue externe Runtime-Dienste werden vor Integration nach [`14-community-and-service-policy.md`](14-community-and-service-policy.md) bewertet.
