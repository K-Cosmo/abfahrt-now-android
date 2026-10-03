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
| F-DOC1-008 | P1 | open | Das Source-ZIP enthält `gradle-wrapper.properties`, aber weder `gradlew`/`gradlew.bat` noch `gradle-wrapper.jar`. Ein reproduzierbarer CLI-Build aus dem ZIP allein ist damit nicht möglich. |
| F-DOC1-009 | P2 | open | `DepartureViewModel.kt` ist ~2.057 Zeilen groß; Clean-Code-Befunde AB-010/011/013 und B-005 bleiben offen. Refactoring nur problemgetrieben und in kleinen Schritten. |
| F-DOC1-010 | P2 | mitigated | Vor DOC1 existierten Root-CHANGELOG und `/docs` als parallele Wahrheitsflächen. DOC1 verschiebt Historie nach `/evidence`, macht `/doc` normativ und lässt `/docs` nur als Redirect bestehen. |
| F-DOC1-011 | P2 | open | AB-049: `::N` in Provider-IDs wirkt in VBB-Daten wie Stop-Point/Steig/Plattform, ist aber nicht verbundübergreifend bestätigt und darf nicht als universelle Semantik genutzt werden. |
| F-DOC1-012 | P2 | **closed Build 138** | `/trips` ist seit Build 138 im RoutePlanner produktiv genutzt; Contract- und Runtime-Evidence liegen vor. |
| F-DOC1-013 | P2 | observed Build 134 | Die aktualisierte OpenAPI bietet `Station.walkSeconds`, `/journey`, `regionBounds` und `routingBounds`. Build 134 erfasst `walkSeconds` nullable und loggt seine reale Abdeckung, verwendet das Feld aber bewusst noch nicht für Produktlogik. `/journey`, `regionBounds` und `routingBounds` bleiben ungenutzt. Runtime-Evidence vom 13.09.2026 zeigte zweimal 40/40 positive Werte. Eine Nutzung bleibt dennoch separat zu entscheiden, da bisher nur ein realer Standortpfad belegt ist und Distanz/Bike/Geometrie fehlen. |

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
| AB-001 | Kritisch | geschlossen | ORS Request Bodies per `@SerializedName` und ProGuard Keep abgesichert. |
| AB-002 | Kritisch | teilweise geschlossen | Build 102 legt erste JVM-Unit-Testbasis an; weitere Filter-/Dedup-Extraktion bleibt offen. |
| AB-003 | Hoch | geschlossen | Zeitfenster-Erweiterung löst ab Build 102 frischen Ladevorgang aus; Verkleinerung refiltert lokal. |
| AB-004 | Hoch | geschlossen | `stop` in fehlenden Locales ergänzt; Locale-Key-Check-Skript eingeführt. |
| AB-005 | Hoch | geschlossen | Fallback-Heuristik für Metrobusse/numerische Linien korrigiert; AB-020 schließt die spätere Restlücke für numerische Berliner Tramlinien. |
| AB-006 | Hoch | teilweise geschlossen | Falsche Paris-Farben ohne Region-Kontext deaktiviert; echte Länderfarben ins Backlog/API. |
| AB-007 | Mittel | geschlossen | README auf Code-Defaults aktualisiert; AB-015 schließt verbliebene Fenster-Obergrenze. |
| AB-008 | Mittel | offen | Security-Backlog: Encrypted Storage oder Backup-Ausschluss. |
| AB-009 | Mittel | geschlossen | Release-Logging deaktiviert. |
| AB-010 | Mittel | offen | Clean-Code-Backlog: Companion-State prüfen. |
| AB-011 | Mittel | offen | Clean-Code-Backlog: Lifecycle-Steuerung konsolidieren. |
| AB-012 | Niedrig | geschlossen | `testfile` entfernt. |
| AB-013 | Niedrig | offen | Konstanten/Kommentar für Merge-Zeitfenster. |
| AB-014 | Niedrig | geschlossen | Unklare Einzelbuchstaben-Farben P/R/S/T entfernt; generischer Modus-Fallback bleibt. |

## Gate-Status nach Build 101

Build 101 reduziert Release-Risiken, ersetzt aber noch nicht die fehlende Testbasis. Kein Produkt-Release ohne Build-102-Testfundament.

## Build-101-Verifikation / Build-102-Übernahmen

Quelle: `docs/qa/2026-07-09_build101_QA-Review.md`.

| ID | Schwere | Status Build 102 | Entscheidung / Maßnahme |
|---|---:|---|---|
| AB-015 | Mittel | geschlossen | README-Abfahrtsfenster von 0–60 auf 0–120 min korrigiert; `scripts/check_readme_defaults.py` ergänzt. |



## Build-102 Smoke-Test Hinweise

Quelle: `docs/qa/2026-07-09_build102_smoke_notes.md`.

| ID | Schwere | Status Build 103 | Entscheidung / Maßnahme |
|---|---:|---|---|
| AB-016 | Niedrig | dokumentiert | Flugzeug-Icon ist bewusstes Design für ungefähre Luftlinien-/Fallback-Distanz; kein Bug. |
| AB-017 | Mittel | geschlossen | Build 109/AB-033 erklärt die Hauptursache über zu strenge Departure→Station-Zielauflösung; Build 110 schließt den Restfall mit zusätzlichem Stationsqualifier. |
| AB-018 | Mittel | offen | Main-Thread-Jank bei ORS-Enrichment/Resort; Performance-Backlog. |
| AB-008a | Mittel | geschlossen | DataStore-Preferences aus Backup/Gerätetransfer ausgeschlossen. |
| AB-008b | Mittel | **geschlossen / Build 125** | Keystore-geschützte AES-GCM-Persistenz und Altwertmigration sind im Device-Logcat bestätigt. |


## Build 103 fix1 Ergänzung

| ID | Schwere | Status Build 103 fix1 | Entscheidung / Maßnahme |
|---|---|---|---|
| AB-019 | Hoch | geschlossen | Build 103 war wegen fehlender Platform SDK 37 nicht baubar. SDK-Level auf 36/36 zurückgestellt; API-37-Targeting bleibt Readiness-Backlog bis `platforms;android-37` verfügbar ist. |


## Build 103 fix1 Review / Build 104 Übernahmen

Quelle: `docs/qa/2026-07-10_build103_fix1_QA-Review.md`.

| ID | Schwere | Status Build 104 | Entscheidung / Maßnahme |
|---|---:|---|---|
| AB-020 | Mittel | geschlossen | Berliner numerische Tramlinien in `inferModeFromLine()` ergänzt und Unit-Test korrigiert. |
| AB-021 | Hoch | reduziert | Per-Vergleich-Debug-Logging entfernt, Vorberechnung eingeführt, zentrale Filterpfade auf `Dispatchers.Default` verschoben. Finaler Status nach neuem Logcat. |
| AB-022 | Niedrig | teilweise geschlossen | RoutePreview-State und MapLibre-Style-/Camera-Pfade instrumentiert; AB-017 bleibt bis Reproduktion offen. |
| AB-023 | Niedrig | geschlossen | `minSdk = 34` als bewusste Produkt-/Reichweitenentscheidung dokumentiert. |


## Build 104 fix1 Review / Build 105 Übernahmen

Quelle: `docs/qa/2026-07-10_build104_fix1_QA-Review.md` plus Screenshot/Logcat Build 104 fix1.

| ID | Schwere | Status Build 105 | Entscheidung / Maßnahme |
|---|---:|---|---|
| AB-017 | Mittel | geschlossen | Nach Build 109/110 nicht weiter als offenes Dauerticket geführt; neue RoutePreview-Restfälle werden separat erfasst. |
| AB-018 | Mittel | reduziert | Sustained Jank im normalen Filter-/Detailfluss nicht mehr sichtbar; debugger-freier Kaltstart-Smoke bleibt empfohlen. |
| AB-024 | Mittel | geschlossen | Nicht ORS-aufgelöste Stopps erhalten nach Enrichment einen expliziten ungefähren Fallback statt endlosem „Wird ermittelt…". |
| G2 | Hoch | offen | Filter-/Dedup-/Merge-Pipeline weiter in testbare Kernlogik extrahieren und automatisiert abdecken. |


## Build 105 / 106

- **AB-024 bleibt nur teilweise geschlossen:** Der Endloszustand „Wird ermittelt…“ ist behoben, aber Detailansichten müssen bei Fallback-Distanz eine gezielte ORS-Directions-Ermittlung auslösen und die Route direkt für Geh-/Fahrradzeit nutzen. In Build 106 umgesetzt.
- **AB-026:** ORS-Abdeckung ist Kernqualität für Radius/Erreichbarkeit. 15/25 Matrix-Abdeckung ist zu schwach; Ziel ist 25/25 bzw. maximal 1–2 Ausfälle bei schlechter OSM/ORS-Datenlage. Build 106 reduziert Duplikatverbrauch durch normalisierte Stationsdeduplizierung.
- **AB-025:** AGP 8.7.3 war für compileSdk 36 nicht freigegeben. Build 106 aktualisiert auf AGP 8.13.2 und Gradle 8.14.5.


## Build 106 / 107

Quelle: QA-Review und Logcat zu Build 106 (`docs/qa/2026-07-10_build106_QA-Review.md`).

| ID | Schwere | Status Build 107 | Entscheidung / Maßnahme |
|---|---:|---|---|
| AB-026 | Mittel | offen/verbessert | Matrix-Zuordnung intern auf Koordinaten-Buckets umgestellt; nächste Messung muss zeigen, ob die 15/25-Abdeckung steigt. |
| AB-027 | Niedrig | geschlossen | Provider-Ortszusätze werden UI-seitig entfernt; Rohdaten bleiben unverändert. |
| G2 | Hoch | offen | Kernlogik weiter automatisiert testen; besonders Walking-Zuordnung/Normalisierung. |


## Build 107 / 107 fix1

Quelle: `docs/qa/2026-07-10_build107_Build_Error.txt` und Nutzerhinweis zur generischen Ortsnamen-Bereinigung.

| ID | Schwere | Status Build 107 fix1 | Entscheidung / Maßnahme |
|---|---:|---|---|
| AB-028 | Hoch | geschlossen | Kotlin-Regex-Strings in `DepartureCard.kt` auf Raw-Strings umgestellt; Buildfehler `Unsupported escape sequence` behoben. |
| AB-027 | Niedrig | präzisiert/geschlossen | Ortsname ist generisch zu verstehen; Anzeige entfernt beliebige trailing Klammer-Ortszusätze und führende `Ort, Haltestelle`-Providerpräfixe bei Stopps. |
| AB-026 | Mittel | offen | Nächster Logcat muss prüfen, ob Koordinaten-Zuordnung ORS-Abdeckung verbessert. |


## Build 107 fix1 QA — übernommen in Build 108

- ORS-Zuordnung über Koordinaten funktioniert für die erste Matrix-Charge: Logcat zeigt 25/25 aufgelöste Ziele.
- Verbleibende `approximateStops` sind nun überwiegend sichtbare Stops außerhalb der ersten 25 angefragten Ziele. Das ist eine Batching-/Kapazitätsfrage und wird in Build 108 adressiert.
- Neue Regression AB-029: wiederkehrender Main-Thread-Jank korreliert mit Abschluss des ORS-Enrichment-Zyklus. Build 108 verschiebt die CPU-lastige Enrichment-/Overlay-Arbeit auf `Dispatchers.Default`.
- UI-Cleanup-Korrektur: Direction-Namen dürfen nicht wie Stop-Namen führend gekürzt werden.

## Build 108 QA — übernommen in Build 109

Quelle: `docs/qa/2026-07-10_build108_QA-Review.md` plus Nutzer-Screenshot/Hinweise.

| ID | Schwere | Status Build 109 | Entscheidung / Maßnahme |
|---|---:|---|---|
| AB-031 | Mittel | geschlossen | Initialen deduplizierten API-Stand sofort anzeigen; Add-on-Calls finalisieren danach. |
| AB-032 | Mittel | geschlossen | Stop=Direction wird als Terminal-/Nicht-weiter-Verbindung ausgeblendet. |
| AB-033 | Mittel | geschlossen | RoutePreview-Zielstation robust über ID/Name/Normalisierung/Distanz auflösen; ORS bleibt koordinatenbasiert. |
| G2 | Hoch | offen | Automatisierte Tests für Filter-/Dedup-/Walking-/RoutePreview-Zuordnung bleiben nächster struktureller Schritt. |


## Build 109 QA — übernommen in Build 110

Quelle: `docs/qa/2026-07-10_build109_QA-Review.md`.

| ID | Schwere | Status Build 110 | Entscheidung / Maßnahme |
|---|---:|---|---|
| AB-031 | Mittel | dokumentiert | Initialer deduplizierter API-Stand ist ein vorläufiger First-Paint-Stand für jeden echten Netzwerk-Refresh; lokale Refilter/Throttle-Pfade bleiben lokal. |
| AB-017 | Mittel | geschlossen | Build 109/AB-033 erklärt die historische RoutePreview-Fehlerklasse durch zu strenge Departure→Station-Zuordnung; wird mit Verweis auf AB-033 geschlossen. |
| AB-034 | Niedrig | geschlossen | Restfall `stationDistance=0` mit zusätzlichem Klammerqualifier wie `(Am Schäfersee)` durch stärkere technische RoutePreview-/Station-Normalisierung adressiert. |
| G2 | Hoch | offen | Weiterhin struktureller Blocker: gemeinsame/testbare Normalisierungs- und Filterlogik weiter ausbauen. Build 110 ergänzt nur einen fokussierten RoutePreview-Normalisierungstest. |


## Build 111

### AB-034 — Generischer statt stationsspezifischer Normalisierungsfix

Build 110 schloss den beobachteten Restfall technisch, war aber als Implementierung zu eng am konkreten Beispiel `U Franz-Neumann-Platz (Am Schäfersee) (Berlin)` argumentiert. Build 111 zieht die Logik in `StationNameNormalizer` zusammen und testet zusätzlich Länder-/Stadtmuster wie Paris, Madrid und Düsseldorf. Damit wird der Fix als europataugliche Provider-Normalisierung geführt, nicht als lokaler Berlin-Sonderfall.

### G2 — Normalisierung weiter testgetrieben ausbauen

Die Konsolidierung reduziert das Risiko divergierender Regex-Logiken. G2 bleibt dennoch offen, bis weitere Kernpfade der Filter-/Dedup-/Merge-Pipeline automatisiert getestet sind.

- AB-035 | Mittel | geschlossen | API-Dedup-First-Paint verursachte bei jedem Refresh sichtbare Unruhe; jetzt nur noch Kaltstart-Booster.
- AB-036 | Mittel | geschlossen | ORS-Gehwegermittlung wurde trotz unverändertem Standort erneut gestartet; jetzt Carry-forward vorhandener Metriken.


## Build 112 / Build 113

- **AB-037:** Logcat Build 112 zeigt den Kollaps der Liste: ein Refresh liefert `previous=96 incoming=77 merged=77 filtered=1`, spätere Refreshes wieder `filtered=9`, danach erneut `filtered=2/5`. Ursache ist kein ORS-Problem, sondern ein zu harter finaler Listenersatz bei transient unvollständigem API-/Add-on-Response. Build 113 hält frühere, noch zeitlich gültige Abfahrten bei gleichem Standort im Merge-Pool.


## Build 111–113 QA — Stable-Merge bestätigt, Testlücke bleibt

Der QA-Report zu Build 111–113 bestätigt: Build 111 konsolidiert die Stationsnormalisierung sauber, Build 112 führte eine Listen-Kollaps-Regression ein, Build 113 behebt diese Regression nachweisbar über Stable-Merge. Offene Hinweise: Hard-Reset-Branch verständlich dokumentieren und Merge-Fenster bei dichtem Takt automatisiert absichern. Build 114 übernimmt beide Punkte.

## Build 115 — Stable-Merge und ORS-Vollständigkeit

Die Nutzerbeobachtung nach Build 114 zeigt: Listenruhe allein reicht nicht, wenn ein Stop im Stable-Merge-Pool mit Luftlinie/Stationsdistanz gewinnt und spätere echte Walkdistance-Kandidaten nicht nachziehen. Build 115 setzt deshalb den ORS-Skip auf Vollständigkeit relevanter Stops im Radius um und zentralisiert zugleich die Sortierung. Das reduziert das Risiko divergierender Entscheidungen zwischen Anzeige, Filter und Stable-Merge.

## Build 115 / Folgeabfahrten im Detailsheet

Logcat Build 115 zeigt, dass der lokale Datensatz vor der finalen Listenbegrenzung deutlich mehr Abfahrten enthält als sichtbar bleiben (`raw` deutlich größer als `limited`). Die Detailansicht kann diese lokalen Daten nutzen, um für die gewinnende Linie/Richtung/Haltestelle weitere Zeiten anzuzeigen, ohne API- oder ORS-Last zu erhöhen.

## Build 116 — Detailsheet-Folgeabfahrten zu streng gematcht

Der Nutzerbericht zu Build 116 zeigt: Das Feld „In“ zeigt weiterhin nur die nächste Abfahrt. Im Build-116-Logcat sind lokale Rohdaten aus initialem Request und Stop-Add-on vorhanden; die Fehlerklasse liegt daher nicht bei fehlenden API-Requests, sondern bei der zu strengen Zuordnung der lokalen Folgeabfahrten. Build 117 erweitert das Matching auf einen lokalen Haltestellencluster und ergänzt `AbfahrtFollowUp`-Diagnostik.


## Build 117 Logcat Findings

- Build 117 follow-up logic works when matching candidates are present in the local response.
- The logcat shows initial API saturation and add-on usage; the raw response is larger than the visible filtered list, which supports using a larger local catchment pool for detail-sheet follow-ups and view-local filtering.
- A transient first request timeout was retried successfully; no app crash/ANR signature was found in the relevant Abfahrt logs.


## Build 118 Logcat — übernommen in Build 119

| ID | Status | Hinweis |
|---|---|---|
| AB-047 | wieder geöffnet durch Build-119-QA; Build-120-Fix | `stop=de:11000:900011201::6` zeigte, dass Direct-Stop-Add-ons ID-Werte im Stop-Feld liefern können. Build 119 löste nur Exakt-ID-Fälle; Build 120 ergänzt Basis-ID-Matching für Plattformsuffixe. |


## Build 115–119 QA — übernommen in Build 120

### AB-047 — Build 119 war nur Teilfix

Der Build-115–119-QA-Report weist nach, dass `stop=de:` in Build 119 weiterhin in `AbfahrtDistanceDebug`, `AbfahrtDedupDebug` und `AbfahrtFollowUp` vorkommt. Ursache: Der Build-119-Test deckte nur den Idealfall ab, in dem `Departure.stop` exakt einer `Station.id` entspricht. Build 120 ergänzt die reale Providerform: platform-suffixed Stop-ID in der Abfahrt, Basis-ID in der Stationsliste.

### G12 — Findings-Status nur mit Diff und Verifikation

Die QA weist Phantom-Closures für AB-040 bis AB-042 in Build 116/117 aus. Der Prozess wird geschärft: Ein Ticket wird nur als geschlossen dokumentiert, wenn der Build entweder einen kausalen Code-/Test-Diff oder eine explizite Duplikat-/Superseded-Notiz enthält und der nächste QA-Fokus das behauptete Verhalten prüft.


## Build 120 QA — übernommen in Build 121

| ID | Priorität | Status | Hinweis |
|---|---:|---|---|
| AB-048 | Hoch | mitigated/pending | Build 120 konnte Provider-Basis-IDs bei mehreren Kandidaten rein nach Entfernung auflösen und dadurch Linie 128 fälschlich `Walderseestr. (Berlin)` zuordnen. Build 121 erlaubt Basis-ID-Distanz-Tiebreak nur noch bei gleichem normalisiertem Stationsnamen. |
| AB-049 | Mittel | offen | Das Suffix nach `::` in Provider-IDs wirkt wie ein Plattform-/Gleis-/Steig-Indikator (`::1`, `::2`, ...). Es kann später genutzt werden, um pro Linie/Richtung alle relevanten Steige sichtbar zu halten. Nicht in Build 121 umgesetzt, weil Bugfixing Priorität hat. |


## Build 121 Logcat — übernommen in Build 122

| ID | Priorität | Status | Laufzeitbefund |
|---|---:|---|---|
| AB-047 | Hoch | offen | Fünf Warnzyklen `unresolved provider stop ids after enrichment`; 168 `stop=de:`-Vorkommen in matchingrelevanten App-Tags und 66 rohe Provider-ID-Kandidaten mit `decision=WIN`. |
| AB-048 | Hoch | mitigated/pending | Linie 128 an `Walderseestr. (Berlin)` blieb als Rohkandidat vorhanden, verlor aber in allen elf protokollierten Dedup-Entscheidungen gegen die nähere Haltestelle. Kein Beleg, dass dieser Kandidat in der finalen sichtbaren Liste gewann. |
| AB-050 | Hoch | Build-122-Fix, pending | RoutePreview löste `de:11000:900011201::5/::6` im gemergten Response exakt auf, obwohl das frühere Batch-Enrichment dieselben IDs ungelöst ließ. Post-Merge-Enrichment ist damit durch reale Laufzeitdaten begründet. |
| AB-051 | Hoch | Build-122-Fix, pending | `U Osloer Str.` und `U Osloer Str. (Berlin)` bildeten getrennte Dedup-Keys. Die zentrale normalisierte Linie+Richtung-Identität verhindert diese Alias-Dopplung. |
| AB-049 | Mittel | offen | `::5` und `::6` treten an derselben physischen Haltestelle für unterschiedliche M13-Richtungen auf; `::1`, `::3`, `::4` erscheinen an weiteren Stop-Points. Plausibler Plattform-/Steighinweis, aber noch keine verbundübergreifend bestätigte Semantik. |

Weitere Laufzeitqualität: Die sichtbare Liste blieb über die protokollierten Refreshes bei 15–17 Einträgen stabil; kein App-Crash, keine FATAL-EXCEPTION und kein App-ANR. Die zahlreichen MapLibre-`Canceled`-Meldungen entstanden beim Verwerfen nicht mehr benötigter Kartenkacheln.

## Build-123-Abnahme

| ID | Prio | Status | Befund |
|---|---:|---|---|
| F-B123-001 | P1 | **closed Build 123** | Device-Logcats belegen ORS WALK-Matrix sowie WALK/BIKE-Directions über `api.heigit.org` mit HTTP 200 und ohne Rückfall auf den Alt-Host. |

## Build 127 artifact result / Build 128 remediation — 2026-09-12

Real Build-127 APK evidence reduced the 64-bit 16-KB blockers from five to three. DataStore 1.2.1 is now clean. Remaining gating failures are `graphics-path` on arm64-v8a and x86_64 plus MapLibre on arm64-v8a. ZIP alignment remains clean.

The supplied Build-127 output was generated with an older audit-script copy because it still classified 32-bit ABIs as `FAIL`. The normative Build-127/128 audit script gates only arm64-v8a and x86_64. This tooling provenance mismatch does not alter the failed gate because three 64-bit failures remain.

Build 128 isolates MapLibre remediation by switching to `org.maplibre.gl:android-sdk-opengl:12.3.1`. `graphics-path` remains a separate unresolved upstream-binary blocker; no local fork is introduced in this build.

## Build 128 artifact result / Build 129 MapLibre final upstream test — 2026-09-12

Der reale Build-128-Audit lief mit `16-KB audit version=2 gateAbis=arm64-v8a,x86_64`. Ergebnis: vier harte Fehler bei sechs 64-Bit-Libraries. DataStore ist auf arm64 und x86_64 vollständig grün; alle ZIP-Datenoffsets sind weiterhin korrekt ausgerichtet. `libandroidx.graphics.path.so` und `libmaplibre.so` scheitern dagegen auf beiden 64-Bit-ABIs am GNU_RELRO-Ende.

Build 129 testete daher genau einmal die stabile MapLibre-OpenGL-Linie 13.6.0. Der reale Build-129-Audit hat diesen Test inzwischen bestanden: MapLibre ist auf arm64 und x86_64 grün. Die Versionsiteration ist damit beendet; nur `graphics-path` blieb als harter Blocker und wird in Build 130 separat remediated.

## Build 129 artifact result / Build 130 graphics-path remediation — 2026-09-12

Der reale Build-129-Audit ist der entscheidende Konvergenzpunkt: DataStore 1.2.1 und MapLibre OpenGL 13.6.0 sind auf `arm64-v8a` und `x86_64` vollständig `OK ELF`; sämtliche zugehörigen ZIP-Datenoffsets sind 16-KB-ausgerichtet. Damit ist die MapLibre-Versionserprobung beendet und erfolgreich abgeschlossen.

Übrig bleiben genau zwei harte Fehler, beide aus derselben Library: `libandroidx.graphics.path.so` scheitert auf arm64 und x86_64 mit `GNU_RELRO endMod16K=0x2000`. Das ist kein Packaging-Problem. Build 130 entfernt deshalb den veröffentlichten Native-Prebuilt vollständig aus dem Dependency-Graph und ersetzt die für Compose benötigte API auf Basis der ab API 34 vorhandenen Framework-PathIterator-API plus Pure-Kotlin-Conic-Konvertierung.

Build 130 gilt **nicht** allein aufgrund des Source-Designs als gelöst. Der nächste Release-APK-Audit muss belegen, dass `libandroidx.graphics.path.so` nicht mehr enthalten ist und keine 64-Bit-Gating-Fehler verbleiben. Erst danach folgt der echte 16-KB-Systemtest.

## Build 130 Acceptance / Build 131 Android-17-Staging — 2026-09-13

Build 130 ist vollständig 16-KB-abgenommen: Release-APK-Audit grün, AAB `PAGE_ALIGNMENT_16K`, System-Page-Size `16384` und App-Log `AbfahrtCompat memoryPageSizeBytes=16384`. Damit ist F-DOC1-007 geschlossen.

Für Android 17 ist der frühere API-37-Verfügbarkeitsblocker entfallen. Der neue reale Toolchain-Befund ist jedoch relevant: AGP 8.13 unterstützt offiziell nur bis API 36.1; API 37 benötigt mindestens AGP 9.1.1. Build 131 migriert deshalb auf AGP 9.4.0 + Gradle 9.6.0 und gleichzeitig auf AGP-9-built-in-Kotlin. Um zwei Risikoachsen nicht zu vermischen, bleibt `targetSdk = 36`; das Target-37-Verhalten folgt erst nach erfolgreichem Build/R8/API-37-Smoke.

## Build 131 Acceptance / Build 132 Target-37 Activation — 2026-09-13

Build 131 ist für Phase 1 abgenommen. Der reale Kotlin-Compile lief mit acht Warnings, aber ohne Compilefehler. Zwei Warnings betreffen die zukünftige Default-Target-Semantik von Constructor-Parameter-Annotationen; sechs weitere sind reine Nullability-Hygiene (`?.`/`!!`). Sie sind nicht Teil der Android-17-Migration und werden nicht in Build 132 mitbereinigt.

Der API-37-Runtime-Log zeigt erfolgreiche abfahrt.now-Requests, ORS-Anreicherung und MapLibre/RoutePreview ohne App-FATAL, `UnsatisfiedLinkError` oder nativen Crash. Zusätzlich bestätigt das Build-131-AAB erneut `PAGE_ALIGNMENT_16K`. Damit ist der Toolchain-/compileSdk-Teil geschlossen.

Build 132 aktiviert ausschließlich `targetSdk = 37`. Offene Evidence betrifft jetzt die Target-37-Verhaltensänderungen, insbesondere Default-Certificate-Transparency und den allgemeinen Kernflow unter Target 37.


## Build 132 Acceptance / Build 133 Warning Hygiene — 2026-09-13

Build 132 ist für die funktionale Android-17-/Target-37-Migration abgenommen. Der reale Debug-Build war erfolgreich. Im gelieferten API-37-Logcat liefern abfahrt.now, Photon und HEIGIT ORS HTTP 200; RoutePreview erreicht `state=Ready` und MapLibre lädt den Style. Im App-Prozess wurden keine `AndroidRuntime`-FATALs, `UnsatisfiedLinkError` oder `SIGSEGV` beobachtet. Der Nutzer bestätigt zusätzlich, dass die UI im getesteten Profil unauffällig aussieht.

Offen blieb nur Compiler-Hygiene: Der Build-132-Lauf meldete sieben nicht-blockierende Kotlin-Warnings — ein Annotation-Default-Target-Hinweis und sechs redundante `?.`/`!!`. Aus Build 131 ist zusätzlich derselbe Annotation-Hinweis im `UserPreferencesRepository` bekannt. Build 133 bereinigt ausschließlich diese Warnklasse und ist nicht als Feature-/Behavior-Build zu behandeln. Ein Release/R8-Build wurde mit der gelieferten Build-132-Evidence nicht belegt und bleibt normales Release-Gate.

## Build 133 acceptance — 13.09.2026

Der reale `assembleDebug`-Lauf ist ohne die sieben bekannten Kotlin-2.3-Warnings erfolgreich. Der mitgelieferte Runtime-Capture zeigt weiterhin erfolgreiche abfahrt.now-Requests sowie Stable-Merge/Filter-Pipeline ohne App-FATAL-/Native-Linker-Signatur. Build 133 ist für seinen isolierten Compiler-Hygiene-Scope abgenommen.

## Build 134 observation focus

Die OpenAPI nennt `Station.walkSeconds`, aber seine reale Feldabdeckung in den tatsächlich verwendeten `/departures`-Antworten ist noch nicht belegt. Build 134 darf deshalb nur erfassen, testen und diagnostizieren. Eine Nutzung als ORS-Ersatz oder Reachability-Quelle wäre ohne diese Evidence verfrüht.



## Build 134 evidence result / Build 135 test-harness finding

- Runtime: zweimal `walkSeconds coverage=40/40 absent=0 nonPositive=0`; Werte änderten sich zwischen Refreshes geringfügig, passend zu einer originbezogenen serverseitigen Neuberechnung.
- Gradle: `assembleDebug` erfolgreich; 57 Unit-Tests liefen, 54 bestanden. Die drei Fehler sind keine Assertions, sondern `Method d/w in android.util.Log not mocked` in bestehenden Stop-ID-Enrichment-Pfaden.
- Zusätzlich eine Kotlin-Testcompiler-Warnung in `LocaleParityTest.kt` wegen nullable `File.parentFile`.
- Build 135 behebt ausschließlich diese Test-Infrastruktur-/Hygiene-Punkte.


## Build 135 acceptance / Build 136 auth requirement

- Build 135: `:app:testDebugUnitTest :app:assembleDebug` real erfolgreich in 2 s; keine vorherigen JVM-Log-Stubfehler und keine LocaleParity-Warnung mehr. Scope abgenommen.
- Neue Produktanforderung: abfahrt.now ist nicht mehr keylos nutzbar. Build 136 implementiert den Pflicht-Key als Access-Invariant; Erststart ohne Key und normaler Start mit Key sind real bestätigt. Settings-Delete/gezielter 401-Test wurden in diesem Evidence-Lauf nicht separat provoziert.
- Neue Produktanforderung Route planen: Startseitensuche bleibt, wird zum direkten Ziel-Einstieg. Bestehende Alternativstandort-Abfahrten wandern in Build 137 auf eine eigene Menü-Seite; MVP folgt Build 138, Details Build 139.

## Build 136 runtime / Build 137 navigation split

- Der Emulatorlauf für den Pflicht-Key zeigt den erwarteten Startpfad: ohne abfahrt.now-Key kein Weiter; nach Key-Eingabe startet der Departure-Flow. Das mitgelieferte Logcat enthält viel Emulator-/Standort-Rauschen, aber keinen app-seitigen Fatal-/Linker-Crash im beobachteten Lauf.
- Build 137 verschiebt ausschließlich den bestehenden Photon-Alternativstandort-Flow auf eine eigene Seite hinter `⋮`. Die Startseite zeigt in diesem Zwischenbuild bewusst noch kein neues Routing-Suchfeld; dieses wird erst mit der vollständigen Zielauswahl/RoutePlanner-Semantik in Build 138 wieder eingesetzt.
- Beim Zurücknavigieren aus dem Alternativstandort-Flow wird der gemeinsame State auf aktuellen Standort/Idle zurückgesetzt, um Stale-UI auf der Startseite zu verhindern.

## Build 137 compile correction
- Real `assembleDebug` evidence exposed a Kotlin visibility error: public `DepartureScreen` exposed internal `DepartureScreenMode`.
- Minimal correction: `DepartureScreen` is module-internal; its only consumer is the app-internal navigation host.
- No product/runtime behavior change and no versionCode bump because the failed Build 137 artifact was never accepted.

## Build 137/138 Routing-UX

- **Build 137 accepted:** korrigierter Source baut real mit `testDebugUnitTest + assembleDebug`; Menü und aktualisierte Sprachpakete wurden im Runtime-UI bestätigt. Der einzige Compilerhinweis war der deprecate `Icons.Filled.ArrowBack`; Build 138 verwendet AutoMirrored.
- **Build 138 accepted:** korrigierter Route-planen-MVP wurde real gebaut und im späteren Routing-Flow mit Photon und `/trips` funktional bestätigt. Mehrregionen-Coverage bzw. reale `sameVehicle`-Häufigkeit bleiben davon getrennte Beobachtungsthemen.

- **Build 138 first compile attempt failed (historisch):** `DepartureScreen.kt:347` referenced removed `promptText`; der korrigierte Source nutzt `search_station_prompt` und wurde später erfolgreich gebaut. Kein funktionaler Scopewechsel.


## Build 146 findings

- **F-146-001:** Build-145-Screenshot/Feedback zeigt, dass eine separate Zeitspalte links neben der Modusspalte visuell unruhig bleibt; Zeit und Modus gehören auf dieselbe Leading-Column-Achse.
- **F-146-002:** Photon-Lat/Lon-Bias priorisiert Nähe, verhindert aber keine gleichnamigen Fern-Treffer zuverlässig. Der vorhandene `DepartureResponse.city`-Wert ist ein billiger, bereits vertraglich verfügbarer Scope-Hinweis.
- **F-146-003:** Android bietet keinen vendor-neutralen Walking-Routing-Intent mit vollständiger Inter-App-Garantie. Build 146 nutzt deshalb best-effort `google.navigation` ohne Paket-Pinning und fällt bei fehlendem Handler auf `geo:` zurück; kein externes SDK.
- **F-146-004 (P1, reproduced / teilweise Build 147):** reale Build-146-Evidence zeigt `q=potsdam, Berlin` und `q=s potsdam, Berlin`. Build 147 behebt diese Fälle, die zugrunde liegende City-Heuristik bleibt aber laut F-147-001 für mehrwortige Fernorte fehleranfällig und wird in Build 148 vollständig entfernt.


## Build 147 findings

- **F-147-001 (P1, reproduced / Build 148 fix):** realer Build-147-Gradle-Gate ist grün (`BUILD SUCCESSFUL in 7s`). Runtime bestätigt, dass `Potsdam` und `Hauptbahnhof` ohne City-Anhang an Photon gehen, reproduziert aber für den mehrwortigen Fernort `Bad Saarow` weiterhin `q=Bad Saarow, Berlin`. D-068 löst das Grundproblem daher nicht vollständig. Build 148 superseded die Heuristik mit D-069.
- **F-147-002 (Beobachtung):** die allgemeine `searchPlaces()`-Implementierung sortierte Photon-Treffer zusätzlich über ein eigenes Text-Match-Scoring. Für Build 148 wird dieses Re-Ranking entfernt, damit zunächst die reale Photon-Reihenfolge mit Location Bias beobachtet werden kann. Die stationsspezifische Suche bleibt davon unberührt.
## Build 148 findings

- **F-148-001 (P1, compile/process / corrected source):** the first real `:app:testDebugUnitTest :app:assembleDebug` attempt failed only in `compileDebugUnitTestKotlin`: an obsolete Build-147 `GeocodingLocalityScopeTest.kt` remained in the Android Studio workspace after source overlay and still referenced removed `scopePlaceQueryToCity()`. The clean Build-148 archive had already replaced that test with a new filename, but archive overlay cannot delete stale files. Corrected Build 148 therefore retains the legacy path with D-069 assertions. No production-code defect was indicated by this gate.
- **F-148-002 (closed / accepted):** corrected Build 148 passed the real combined Gradle gate (`BUILD SUCCESSFUL in 1s`). Runtime evidence confirms raw Photon queries with location bias: `Bad Saarow` ranks the Brandenburg village first, `potsdam` ranks Potsdam first, and `Bad bel` ranks Bad Belzig first. User field feedback confirms the Photon ranking behaves as desired; no custom reranking/bias tuning is justified.

## Build 149 findings

- **F-149-001 (P1 UX / fixed in source, evidence pending):** real field screenshot on 2026-09-23 shows a departure already rendered as `Hier` / `Haltestelle erreicht`, while the detail card still shows `Routenkarte` plus an ORS blue polyline. This is a semantic contradiction and an unnecessary ORS call. Build 149 applies D-070: HERE never loads ORS route preview and renders a marker-only location map instead.

