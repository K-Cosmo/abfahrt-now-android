# Findings Register

Statuswerte: `open`, `pending evidence`, `mitigated`, `closed`.

Dieses Dokument führt den **aktuellen** Finding-Stand. Ausführliche historische Build-Notizen bleiben in `/doc/CHANGELOG.md`, im Regression Ledger und in der Git-Historie erhalten; geschlossene Altbefunde werden hier nicht als zweites Langzeitarchiv dupliziert.

## Aktuelle Findings nach Build 149

| ID | Prio | Status | Befund / Konsequenz |
|---|---|---|---|
| F-DOC1-008 | P1 | mitigated / open | Repo-Housekeeping ergänzt `gradlew`, `gradlew.bat`, gehärtete Wrapper-Properties, einen SHA-256-verifizierenden Bootstrap und CI. `gradle-wrapper.jar` ist noch nicht im Repository eingecheckt; CI/bootstrap lädt den offiziellen Gradle-9.6.0-Wrapper und prüft dessen veröffentlichten SHA-256. Vollständige Source-Selbstständigkeit ist erst mit dem verifizierten eingecheckten JAR geschlossen. |
| F-DOC1-009 | P2 | open | `DepartureViewModel.kt` ist rund 2.000 Zeilen groß; Clean-Code-Befunde AB-010/011/013 bleiben offen. Refactoring nur problemgetrieben und in kleinen, regressionsgesicherten Schritten. |
| F-DOC1-011 | P2 | open | `::N` in Provider-IDs wirkt in VBB-Daten wie Stop-Point/Steig/Plattform, ist aber nicht verbundübergreifend bestätigt und darf nicht als universelle Semantik genutzt werden. |
| F-DOC1-013 | P2 | observed Build 134 | OpenAPI bietet `Station.walkSeconds`, `/journey`, `regionBounds` und `routingBounds`. `walkSeconds` wird erfasst und real beobachtet, aber noch nicht für Produktlogik genutzt; `/journey`, `regionBounds` und `routingBounds` bleiben ungenutzt. |
| F-DOC1-014 | P2 | open | Das öffentliche Community-Repository hat noch keine Projektlizenz. Bis zur bewussten Lizenzentscheidung ist es öffentlich einsehbarer Community-Source, darf aber nicht als Open-Source-Projekt mit allgemeinen Nutzungsrechten bezeichnet werden. |
| F-DOC1-015 | P1 | open | Die Community-Identität in README/`/doc` kollidiert noch mit bestehender In-App-About-/Legal-UX (`Built by Riles Tech UG` sowie abfahrt.now-Rechtslinks). Vor dem ersten öffentlichen Community-Release muss die Runtime-UI so konvergiert werden, dass sie keine offizielle Zugehörigkeit suggeriert. Das ist eine eigene Produktänderung mit Buildnummer und Runtime-Gate. |
| AB-018 | P2 | open | Reproduzierbare Main-Thread-/Startup-Jank-Indikatoren (u. a. skipped frames). Erst instrumentieren und messen, dann gezielt optimieren; keine Optimierung auf Verdacht. |

## Kürzlich geschlossene bzw. akzeptierte Baselines

| ID | Status | Abnahme / Konsequenz |
|---|---|---|
| F-DOC1-001 | closed | Sichtbare Provider-ID-/Alias-Regressionsfälle aus Build 122 feldseitig abgenommen. |
| F-DOC1-002 | closed | `nearestDist + 20 m` ist nur technische Toleranz, keine Radius-Erweiterung. |
| F-DOC1-003 | closed Build 123 | ORS-Runtime-Basis auf `api.heigit.org/openrouteservice/` migriert. |
| F-DOC1-004 | closed | `stationDistance` ist konsistent aus `Station.distance` abgeleitet; kein Kern-Contract-Gap. |
| F-B124-001 | closed Build 124 | Numerische Provider-Stop-ID wird nicht mehr als sichtbarer Stopname verwendet. |
| F-DOC1-005 | closed Build 125 | API-Key-Migration und Keystore/AES-GCM-Persistenz per Device-Evidence abgenommen. |
| F-DOC1-006 | closed Build 132 | Android-17/API-37-Target-Migration funktional abgenommen. |
| F-DOC1-007 | closed Build 130 | 16-KB-Release-APK/AAB/Runtime-Evidence vollständig grün. |
| F-DOC1-010 | closed | Root-`CHANGELOG.md` und Legacy-`/docs` sind entfernt; normative Produkt-/Technikdokumentation liegt ausschließlich in `/doc`. |
| F-DOC1-012 | closed Build 138 | `/trips` ist produktiv im RoutePlanner integriert und runtime-validiert. |
| F-148-002 | closed Build 148 | Raw Photon query + `lat/lon` bias und Photon-eigenes Ranking sind real abgenommen; kein Custom-Reranking erforderlich. |
| F-149-001 | closed Build 149 | Feld-Screenshot bestätigt HERE-Details als Standortkarte ohne ORS-Polyline; D-070 verhält sich wie vorgesehen. |

## Aktuelle Build-Evidence 146–149

### Build 146 / 147 — Photon Scope

Build 146 zeigte real zu harte City-Ergänzungen (`Potsdam, Berlin`). Build 147 reduzierte die Heuristik, reproduzierte aber weiterhin `Bad Saarow, Berlin`. Die Zwischenentscheidung D-068 war damit nicht robust genug.

### Build 148 — Raw Query + Location Bias

Build 148 superseded die City-Heuristik vollständig. Der korrigierte Build bestand den kombinierten Gradle-Gate. Runtime-Evidence bestätigte rohe Photon-Queries mit Standort-Bias: `Bad Saarow` rankte den brandenburgischen Ort zuerst, `Potsdam` rankte Potsdam zuerst und `Bad bel` rankte Bad Belzig zuerst. Der Nutzer bestätigte das Ranking im Feld als passend.

Der erste Build-148-Gate scheiterte ausschließlich an einer beim Source-Overlay stehen gebliebenen alten Testdatei, die auf die entfernte Heuristik verwies. Der korrigierte Source behielt den Legacy-Testpfad bewusst bei und ersetzte dessen Assertions durch D-069-Tests. Das war ein Packaging-/Overlay-Befund, kein Produktionscodefehler.

### Build 149 — HERE Detail Map

Ein Feld-Screenshot zeigte zunächst den semantischen Widerspruch `Hier / Haltestelle erreicht` zusammen mit einer blauen ORS-Gehroute. Build 149 setzt D-070 um: HERE lädt keine ORS-Routenvorschau und zeichnet keine Polyline; bei verfügbaren Koordinaten wird eine reine Standortkarte mit Query-Origin und Haltestellenmarker gezeigt. Der anschließende Nutzer-Screenshot bestätigt die erwartete Darstellung.

## Historische Detailspur

Für ältere geschlossene Build-/QA-Befunde gelten als langfristige Referenzen:

- [`05-regression-ledger.md`](05-regression-ledger.md)
- [`CHANGELOG.md`](CHANGELOG.md)
- die jeweilige Git-Historie

Raw Logcats, Geräte-Dumps und andere sensible Evidence werden gemäß [`11-test-and-evidence.md`](11-test-and-evidence.md) nicht automatisch im öffentlichen Repository versioniert.
