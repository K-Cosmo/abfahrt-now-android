# Findings Register

Statuswerte: `open`, `pending evidence`, `mitigated`, `closed`.

## DOC1-Audit 2026-09-12

| ID | Prio | Status | Befund / Konsequenz |
|---|---|---|---|
| F-DOC1-001 | P0 | closed | Build-122-Codefixes für AB-047/050/051 wurden durch mehrtägige Nutzung an verschiedenen Orten feldseitig abgenommen: keine sichtbaren Provider-IDs, erwartete Darstellung und keine erneute Alias-Dopplung beobachtet. Siehe `/evidence/V122/2026-09-12_field-acceptance.md`. |
| F-DOC1-002 | P0 | closed | `nearestDist + 20 m` ist bewusst: enge technische Toleranz gegen GPS-/Stationspunkt-/Steigabweichungen. Sie erweitert den Nutzer-Radius nicht. Entscheidung D-040. |
| F-DOC1-003 | P0 | **closed Build 123** | Runtime-Base-URL auf `https://api.heigit.org/openrouteservice/` migriert. Deprecated Host ist aus dem Runtime-Source entfernt. |
| F-DOC1-004 | P0 | closed | Die aktualisierte Projekt-OpenAPI bestätigt `timestamp` in Millisekunden, `Departure.stop` und `Station.distance`. Build 122 leitet sein internes `Departure.stationDistance` aus `Station.distance` ab; damit besteht für den verwendeten Kernpfad kein Contract-Gap mehr. Öffentliche Alt-/Webbeispiele bleiben nicht-normative Drift. |
| F-B124-001 | P0 | **closed Build 124** | Device-Logcat vom 12.09.2026: `900009173` erscheint nur noch als technische ID in `stops=`-Requests, nicht mehr als `stop=900009173`. U6 Richtung Kurt-Schumacher-Platz läuft als `stop=U Seestr.`; Follow-ups und ORS bleiben funktionsfähig. |
| F-DOC1-005 | P1 | **closed / Build 125 accepted** | Device-Logcat bestätigt Migration beider Altwerte, verschlüsselte Writes und weiterhin erfolgreiche abfahrt.now-/ORS-Zugriffe. |
| F-DOC1-006 | P1 | **closed / Build 132 accepted** | Build 131 stufte Toolchain/compileSdk vor; Build 132 lief real mit `targetSdk 37`. abfahrt.now, Photon und HEIGIT ORS lieferten HTTP 200, RoutePreview wurde `Ready`, keine App-FATAL-/Native-Linker-Signatur; UI im getesteten Profil unauffällig. |
| F-DOC1-007 | P1 | **closed / Build 130 accepted** | Release-APK-Audit: null 64-Bit-Gating-Fehler, DataStore + MapLibre `OK ELF`, `libandroidx.graphics.path.so` `OK ABSENT`. AAB meldet `PAGE_ALIGNMENT_16K`; echtes 16-KB-System und App-Runtime melden `16384`. 16-KB-Readiness ist abgenommen. |
| F-DOC1-008 | P1 | mitigated / open | Repo-Housekeeping ergänzt `gradlew`, `gradlew.bat`, gehärtete Wrapper-Properties, einen SHA-256-verifizierenden Bootstrap und CI. `gradle-wrapper.jar` ist noch nicht im Repository eingecheckt; CI/bootstrap lädt exakt den offiziellen Gradle-9.6.0-Wrapper und prüft SHA-256. Vollständige Source-Selbstständigkeit ist erst mit dem verifizierten eingecheckten JAR geschlossen. |
| F-DOC1-009 | P2 | open | `DepartureViewModel.kt` ist ~2.057 Zeilen groß; Clean-Code-Befunde AB-010/011/013 und B-005 bleiben offen. Refactoring nur problemgetrieben und in kleinen Schritten. |
| F-DOC1-010 | P2 | closed | Repo-Housekeeping nach Build 149 entfernt den parallelen Root-`CHANGELOG.md` und den Legacy-`/docs`-Redirect vollständig. Normative Historie bleibt ausschließlich `/doc/CHANGELOG.md`; `/doc` ist die einzige Policy-/Spec-Wahrheitsfläche. |
| F-DOC1-011 | P2 | open | AB-049: `::N` in Provider-IDs wirkt in VBB-Daten wie Stop-Point/Steig/Plattform, ist aber nicht verbundübergreifend bestätigt und darf nicht als universelle Semantik genutzt werden. |
| F-DOC1-012 | P2 | **closed Build 138** | `/trips` ist seit Build 138 im RoutePlanner produktiv genutzt; Contract- und Runtime-Evidence liegen vor. |
| F-DOC1-013 | P2 | observed Build 134 | Die aktualisierte OpenAPI bietet `Station.walkSeconds`, `/journey`, `regionBounds` und `routingBounds`. Build 134 erfasst `walkSeconds` nullable und loggt seine reale Abdeckung, verwendet das Feld aber bewusst noch nicht für Produktlogik. `/journey`, `regionBounds` und `routingBounds` bleiben ungenutzt. Runtime-Evidence vom 13.09.2026 zeigte zweimal 40/40 positive Werte. Eine Nutzung bleibt dennoch separat zu entscheiden, da bisher nur ein realer Standortpfad belegt ist und Distanz/Bike/Geometrie fehlen. |
| F-DOC1-014 | P2 | open | Das öffentliche Community-Repository hat noch keine Projektlizenz. Bis zur bewussten Lizenzentscheidung ist es öffentlich einsehbarer Community-Source, darf aber nicht als Open-Source-Projekt mit allgemeinen Nutzungsrechten bezeichnet werden. |
| F-DOC1-015 | P1 | open | Die neue Community-Identität in README/`/doc` kollidiert noch mit bestehender In-App-About-/Legal-UX (`Built by Riles Tech UG` sowie abfahrt.now-Rechtslinks). Vor dem ersten Community-Release muss die Runtime-UI so konvergiert werden, dass sie keine offizielle Zugehörigkeit suggeriert; dies ist eine eigene Produktänderung mit Buildnummer/Gate. |

## Build-123-Feldbefund / Build-124-Reopen 2026-09-12

Die frühere V122-Feldabnahme war für die damals beobachteten `de:...`-ID- und Aliasfälle belastbar, deckte aber nicht jede mögliche Provider-ID-Form ab. Build 123 zeigte reproduzierbar eine neue Variante: `Departure.stop=900009173` wurde sichtbar, obwohl dieselbe gemergte Stationsmenge `Station.id=900009173` mit dem lesbaren Namen `U Seestr./Turiner Str.` enthielt.

Die Ursache ist eingegrenzt: `looksLikeProviderStopId()` erkannte nur strukturierte Werte mit mindestens zwei Doppelpunkten. Dadurch wurden rein numerische IDs weder im normalen Enrichment noch im Post-Merge-Gate als technische IDs behandelt. Build 124 adressiert genau diese Lücke ohne Änderung an Dedup, ORS oder Sortierung.

## V122 Feldabnahme 2026-09-12

Die sichtbaren Regressionen AB-047/050/051 gelten für Build 122 als abgenommen. Der Nutzer hat die App über mehrere Tage und an verschiedenen Orten verwendet. Dabei traten keine technischen Station-IDs mehr in den Ansichten auf; die Darstellung war erwartungsgemäß und die bekannte Alias-Dopplung wurde nicht erneut beobachtet.

Diese Feldabnahme beweist nicht, dass intern niemals mehr eine Provider-ID als Rohwert auftreten kann. Genau deshalb bleibt `providerStopId` als technische Rohinformation erhalten. Sie reicht aber zusammen mit dem kausalen Build-122-Codefix aus, um die **sichtbaren** Regressionen zu schließen.

## Build-126-16-KB-Artefaktbefund / Build-127-Remediation 2026-09-12

Der reale Release-APK-Audit aus Build 126 ist negative Evidence und beendet die zuvor offene Vermutung, die vorhandenen vorkompilierten Native-Libraries seien bereits vollständig 16-KB-kompatibel. Alle ZIP-Datenoffsets waren auf 16 KiB ausgerichtet; das Packaging über AGP 8.13.2 ist damit nicht der Blocker.

ELF-seitig schlugen 11 von 12 Libraries fehl. Für die 64-Bit-Abnahme relevant:
- `arm64-v8a/libandroidx.graphics.path.so`: GNU_RELRO-Ende nicht 16-KB-ausgerichtet;
- `arm64-v8a/libdatastore_shared_counter.so`: GNU_RELRO-Ende nicht 16-KB-ausgerichtet;
- `arm64-v8a/libmaplibre.so`: GNU_RELRO-Ende nicht 16-KB-ausgerichtet;
- `x86_64/libandroidx.graphics.path.so`: GNU_RELRO-Ende nicht 16-KB-ausgerichtet;
- `x86_64/libdatastore_shared_counter.so`: GNU_RELRO-Ende nicht 16-KB-ausgerichtet;
- `x86_64/libmaplibre.so`: bereits vollständig grün.

Die ebenfalls gefundenen 32-Bit-Abweichungen bleiben sichtbar, sind aber nicht das Google-Play-64-Bit-Acceptance-Gate. Build 127 ersetzt deshalb keine App-Logik, sondern ausschließlich die betroffenen vorgefertigten Dependency-Binaries.

## Historische Findings

Nachfolgend die bisherige Findings-Historie. Aktuelle Priorität richtet sich nach der Tabelle oben und `/doc/08-backlog.md`.


Quelle: QA-Review zu Build 100 fix1 (`docs/qa/2026-07-09_build100_fix1_QA-Review.md`).

| ID | Schwere | Status Build 101 | Entscheidung / Maßnahme |
|---|---:|---|---|
| AB-001 | Kritisch | geschlossen | ORS Request Bodies per `@TerializedName` und ProGuard Keep abgesichert. |
| AB-002 | Kritisch | teilweise geschlossen | Build 102 legt erste JVM-Unit-Testbasis an; weitere Filter-/Dedup-Extraktion bleibt offen. |
| AB-003 | Hoch | geschlossen | Zeitfenster-Erweiterung löst ab Build 102 fM�