# Regression Ledger

Dieses Dokument hält bereits bekannte Fehlerklassen und ihre Schutzmaßnahmen fest. Historische Einzelbefunde aus Build 101–122 werden zusätzlich im unteren AB-Register erhalten.

## Kritische wiederkehrende Regressionen

### R-001 Dedup nach Merge umgehen
**Symptom:** weiter entfernte Stopps derselben Linie+Richtung tauchen nach Nachladung/Refresh wieder auf.  
**Schutz:** jeder neue Rohdatenstand läuft erneut durch dieselbe Filter-/Identitäts-/Dedup-/Sortierlogik; kein Direkt-Merge in sichtbare UI-Listen.

### R-002 Distanzpriorität vor vollständiger Anreicherung verfälschen
**Symptom:** Stopps ohne belastbare Distanz verdrängen besser bewertete Kandidaten.  
**Schutz:** Distanzquelle explizit behandeln; finale ORS-Distanz vor Fallback, HERE als eng begrenzter Override.

### R-003 Gegenrichtungen zusammenwerfen
**Symptom:** eine Richtung einer Linie verschwindet.  
**Schutz:** Linie + normalisierte Richtung bleibt Dedup-Identität.

### R-004 Provider-Ortszusatz als neue Richtung
**Symptom:** `U Osloer Str.` und `U Osloer Str. (Berlin)` umgehen `maxPerDirection`.  
**Schutz:** zentrale `DepartureServiceIdentity`.

### R-005 Roh-ID wird UI-Name
**Symptom:** Werte wie `de:11000:...::6` erscheinen als Haltestelle oder beeinflussen Matching.  
**Schutz:** Enrichment vor sicht-/matchingrelevanter Verarbeitung; `providerStopId` separat erhalten.

### R-006 Stable Refresh kollabiert Liste
**Symptom:** transient unvollständiger API-Response reduziert die Anzeige auf wenige Einträge.  
**Schutz:** Same-Origin Stable-Merge mit zeitlich gültigem Grace-Pool.

### R-007 Stable-Merge verschmilzt echten dichten Takt
**Symptom:** zwei reale Abfahrten werden als dieselbe behandelt.  
**Schutz:** Stable-Merge-Matchfenster maximal 5 Minuten; 6 Minuten müssen getrennt bleiben.

### R-008 ORS blockiert Core-Refresh
**Symptom:** aktuelle Abfahrtszeiten erscheinen erst nach ORS.  
**Schutz:** Core zuerst anwenden, ORS asynchron.

### R-009 ORS-Skip trotz neuer relevanter Stopps
**Symptom:** neue Stopps bleiben dauerhaft mit Luftlinien-/Stationsdistanz.  
**Schutz:** Same-Origin-Skip nur, wenn für alle aktuell relevanten Stopps finale oder bewusste Approx-Metrik vorliegt.

### R-010 Stale ORS überschreibt neuen Zielkontext
**Symptom:** nach Ziel-/Standortwechsel erscheinen alte Distanzen.  
**Schutz:** Request-/Target-Generation prüfen und stale Ergebnisse verwerfen.

### R-011 Falscher HERE-Restweg
**Symptom:** Nutzer steht an der Station, UI zeigt noch 30–60 m oder Restzeit.  
**Schutz:** `stationDistance <= 35 m` → HERE, 0 m/0 min, höchste Priorität.

### R-012 Endlos „Wird ermittelt…“
**Symptom:** ORS liefert keinen Wert und Detailsheet bleibt dauerhaft im Ladezustand.  
**Schutz:** abgeschlossener ORS-Lauf darf auf endlichen Approx-/Unavailable-Status fallen.

### R-013 Refresh zeigt doppelten Spinner
**Symptom:** Pull-to-refresh und Topbar zeigen gleichzeitig denselben Ladevorgang.  
**Schutz:** nur ein sichtbarer Ladeindikator.

### R-014 UI-Layout verdrängt mittlere Card-Spalte
**Symptom:** Richtung/Station/Entfernung verschwinden durch rechte Zeitspalte.  
**Schutz:** gemeinsame, aber nicht kollabierende Breitenlogik.

### R-015 API-Key-Settings überladen
**Symptom:** große Inline-Key-Felder dominieren Settings.  
**Schutz:** kompakte Statuskarten + separates Key-Sheet.

### R-016 Version driftet zwischen UI und Netzwerk
**Symptom:** Settings/User-Agent zeigen unterschiedliche Buildstände.  
**Schutz:** `BuildConfig` → `AppVersionInfo` als zentrale Ableitung.

### R-017 Build-Beschreibung stimmt nicht mit Diff überein
**Symptom:** angekündigte Änderung ist nicht im Code angekommen.  
**Schutz:** Abnahme immer gegen echten Diff/Source und Evidence, nicht gegen Plantext.

### R-018 API-Dokumentation widerspricht Runtime/Schema
**Symptom:** Feldname/Zeiteinheit/Queryname werden aufgrund einer einzelnen Dokuquelle falsch implementiert.  
**Schutz:** OpenAPI, reale Antwort und App-Modelle gegeneinander prüfen; Widerspruch als Finding behandeln.

### R-019 Künstliches Nearest-Fenster verändert Produktlogik
**Symptom:** mehrere Stopps derselben Linie+Richtung bleiben sichtbar/werden als gleichwertig gewertet, obwohl die Produktregel nur die nächstgelegene relevante Station vorsieht.  
**Schutz:** kein zusätzliches Fenster ohne ausdrückliche Produktentscheidung. Die vorhandene `nearest + 20 m`-Toleranz ist seit D-040 ausdrücklich als technische Stabilitätstoleranz entschieden und damit keine offene Abweichung mehr.

---

## Historisches AB-Register Build 101–122

Der folgende Abschnitt ist aus dem bisherigen `docs/BUGS.md` übernommen. Statusangaben bleiben historische Aussagen; aktuelle offene Punkte werden zusätzlich in `/doc/07-findings.md` geführt.

# Bugs

## Geschlossen in Build 101

### AB-001 — ORS Request Bodies nicht releasefest

Release/R8 konnte Gson-Feldnamen von ORS-Request-Data-Classes obfuskieren. Behoben durch `@SerializedName` und Keep-Regeln.

### AB-004 — `stop`-String fällt in mehreren Locales auf Deutsch zurück

Fehlende Übersetzungen ergänzt.

### AB-009 — HTTP Logging im Release aktiv

OkHttp Logging ist jetzt abhängig von `BuildConfig.DEBUG`; Release nutzt `HttpLoggingInterceptor.Level.NONE`.

### AB-012 — Leere Stray-Datei `testfile`

Aus Source-Paket entfernt.

## Geschlossen in Build 102

### AB-003 — Zeitfenster-Erweiterung triggert keinen Netzwerk-Refresh

Fenster-Erweiterungen und stale Daten lösen jetzt `refresh(force = false)` aus; Verkleinerungen refiltern lokal, solange die Daten frisch sind.

### AB-005 — Mode-Fallback-Heuristik klassifiziert Buslinien als Tram

Berliner Metrotram-Linien sind jetzt explizit begrenzt; M11/M29 und numerische Buslinien wie 100/200 fallen auf Bus.

### AB-014 — Unklare P/R/S/T-Farben

Unklare Einzelbuchstaben-Farben wurden aus der lokalen Farbtabelle entfernt.

### AB-015 — README-Fensterobergrenze falsch

README korrigiert auf 0–120 min und per Script prüfbar gemacht.

## Geschlossen / reduziert in Build 103

### AB-008a — API-Key-Backup / Data Extraction

DataStore-Preferences werden ab Build 103 per `data_extraction_rules.xml` aus Cloud-Backup und Gerätetransfer ausgeschlossen. Die frühere Klartextspeicherung wurde mit Build 125 durch Keystore-geschützten AES-GCM-Ciphertext ersetzt und feldseitig abgenommen.

## Offen / weitergeführt in Build 107

### AB-026 — ORS-Matrix-Abdeckung bleibt zu niedrig

Build 106 zeigte weiterhin nur etwa 15/25 ORS-aufgelöste Matrixziele. Build 107 stellt die Zuordnung von Matrixzellen intern auf Koordinaten-Buckets um und nutzt Stationsdistanz/normalisierte Namen nur noch als Fallback für die Zuordnung zu Departures. Das Ticket bleibt offen, bis die nächste Logcat-Messung die Zielqualität belegt.

### AB-027 — Provider-Ortszusätze stören die UI-Lesbarkeit

Viele Namen enthalten API-/Provider-Ortszusätze wie `(Berlin)`, `(Potsdam)`, `(Wien)` oder führende Präfixe nach Muster `Ort, Haltestelle`. Diese Rohdaten bleiben im Datenmodell erhalten, werden aber ab Build 107/107 fix1 in Liste und Detailsheet für die Anzeige generisch bereinigt. Berlin ist nur ein Beispiel, keine Sonderregel.

## Geschlossen in Build 107 fix1

### AB-028 — Build 107: Unsupported escape sequence in Kotlin-Regex

Build 107 schlug bei `:app:compileDebugKotlin` fehl, weil Regex-Strings in `DepartureCard.kt` normale Kotlin-Strings mit Sequenzen wie `\s`/`\p` enthielten. Geschlossen in Build 107 fix1 durch Kotlin-Raw-Strings und Dokumentation des Buildfehler-Artefakts.


## Offen / weitergeführt in Build 108

### AB-026 — ORS-Matrix-Abdeckung: Zuordnung gelöst, Batching bleibt zu verifizieren

Build 107 fix1 belegt 25/25 aufgelöste Matrixziele für die erste Koordinaten-Charge. Verbleibende `approximateStops` entstehen aus sichtbaren Stopps außerhalb der ersten 25 Ziele. Build 108 lädt daher bis zu drei ORS-Matrix-Chargen à 25 Stationen nach. Ziel: alle relevanten sichtbaren Stationen im Radius erhalten echte ORS-Geh-/Fahrraddistanzen, damit Radius/Reachability nach realer Wegstrecke und nicht nur nach Luftlinie arbeitet.

### AB-029 — ORS-Enrichment verursacht Main-Thread-Jank

Build 107 fix1 zeigt 8/8 Davey-/Skipped-Frame-Ereignisse zeitgleich mit `async ORS enrichment applied`. Build 108 verlagert CPU-lastige Enrichment-/Overlay-Arbeit auf `Dispatchers.Default`. Das Ticket bleibt offen bis zur nächsten debugger-freien Logcat-Verifikation.

## Geschlossen in Build 108

### AB-030 — Direction-Namen wurden mit Stop-Präfixlogik bereinigt

Richtungsnamen dürfen nicht führend nach `Ort, Ziel` gekürzt werden. Ziele wie `Wedding, Virchow-Klinikum`, `Franz. Buchholz, Guyotstr.` oder `Prenzlauer Berg, Björnsonstr.` sind fachlich sinnvolle Zielbezeichnungen. Ab Build 108 wird bei Directions ausschließlich ein trailing Provider-Ortszusatz wie `(Berlin)` entfernt.

## Geschlossen in Build 114

### AB-038 — Hard-Reset-Branch war nicht als bewusste Stable-Refresh-Entscheidung erkennbar

Der Branch `else if (hardReset)` hält bei bereits sichtbaren Daten bewusst die alte Liste sichtbar, bis der Standortwechsel-Refresh ein Ersatzresultat liefert. Build 114 ergänzt Kommentar und Dokumentation; der finale Hard-Reset-Response wird weiterhin ohne Stable-Merge bewertet.

### AB-039 — Stable-Merge-Matchfenster für dichten Takt absichern

Die Review-Runde zu Build 111–113 markierte das Risiko, dass eine zu große Merge-Toleranz getrennte Abfahrten derselben Linie/Richtung zusammenzieht. Build 114 begrenzt die produktive Toleranz explizit auf maximal 5 Minuten und ergänzt einen JVM-Test, der zwei Abfahrten mit 6 Minuten Abstand getrennt hält.

## Geschlossen in Build 115

### AB-040 — Same-Origin-Refresh überspringt ORS trotz relevanter Stops ohne Gehwegmetrik

Build 112/113/114 übernahm vorhandene ORS-Metriken bei unverändertem Standort. Die Skip-Entscheidung war jedoch zu grob: Wenn irgendeine Gehwegmetrik vorhanden war, wurde ORS übersprungen. Dadurch konnten neue oder wieder auftauchende Stops im Radius nur mit Stations-/Luftliniendistanz in Dedup und Sortierung eingehen. Build 115 prüft stattdessen, ob aktuelle relevante Stops im Radius noch ohne Geh-/Fahrradmetrik und ohne bewussten Approx-Fallback sind; in diesem Fall wird ORS erneut gestartet.

### AB-041 — Display-Sortierung muss wieder der Produktlogik folgen

Die gewünschte Produktreihenfolge ist: „Hier“ bis 35 m zuerst, danach effektive Entfernung, Linie und Abfahrtszeit. Build 115 verankert diese Reihenfolge in einer zentralen Utility und ergänzt Tests.

### AB-042 — Sortierlogik doppelt in ViewModel und StableMerger

Build 114 enthielt faktisch zwei Comparator-Implementierungen. Build 115 führt `DepartureDisplayOrdering` als Single Source of Truth ein; ViewModel und `DepartureStableMerger` delegieren an dieselbe Logik.

## Offen

### AB-008b — API-Key-Speicherung lokal unverschlüsselt

Status: **Fixed and accepted in Build 125.**

Build 125 persistiert abfahrt.now- und ORS-Keys nur noch als versionierten AES-256/GCM-Ciphertext. Der AES-Schlüssel liegt im Android Keystore. Device-Evidence bestätigt die Migration beider Altwerte sowie verschlüsselte Writes und weiterhin erfolgreiche Providerzugriffe. Die DataStore-Datei bleibt aus Backup/Device-Transfer ausgeschlossen.

### AB-010 / AB-011 / AB-013

Clean-Code-/Hygiene-Themen für spätere Hardening-Zyklen.


### AB-017 — Routenkartenvorschau bleibt nicht verfügbar trotz ORS-Erfolg

Logcat zeigt erfolgreichen ORS-Route-Preview-Request und `hasGeoJson=true`; Screenshot zeigt trotzdem „Kartenvorschau aktuell nicht verfügbar". Zu klären: Race Condition, MapLibre-Initialisierung, Tile-/Style-Lifecycle oder UI-State-Verbindung.

### AB-018 — Main-Thread-Jank beim ORS-Enrichment/Resort

Logcat Build 102/103 zeigt Skipped-/Davey-Frames im App-Prozess. Build 104 reduziert den Hauptverdächtigen AB-021; endgültige Bewertung nach neuem Logcat.


## Geschlossen in Build 103 fix1

### AB-019 — Build 103 verlangt nicht verfügbare Android SDK Platform 37

Build 103 setzte `compileSdk = 37` und `targetSdk = 37`. In der lokalen Buildumgebung schlug bereits `:app:testDebugUnitTest` fehl, weil `platforms;android-37` nicht gefunden wurde. Build 103 fix1 stellt die Baubarkeit wieder her: `compileSdk = 36`, `targetSdk = 36`, `minSdk = 34`. API-37-Targeting bleibt als späterer Android-17-Gate-Schritt offen.


## Geschlossen / reduziert in Build 104

### AB-016 — Distanz-/Navigationsicon wirkt wie Flugzeug

Status korrigiert in Build 104 fix1: kein Bug. Das Flugzeug-Icon ist bewusstes Design für ungefähre Luftlinien-/Fallback-Distanz. Exakte ORS-Werte nutzen weiterhin `NearMe`; das Standort-Pin-Icon bleibt dem Haltestellen-/Ortsbezug vorbehalten.

### AB-020 — Berliner numerische Tramlinien fallen auf Bus zurück

`inferModeFromLine()` erkennt jetzt neben Metrotram-Linien auch numerische Berliner Tramlinien als Tram, sofern das API-Mode-Feld fehlt. Unit-Test aktualisiert.

### AB-021 — Debug-Logging in O(n²)-Filterschleife verursacht/verschärft Jank

Per-Vergleich-Logging im Ziel-auf-nähere-Station-Filter entfernt. Normalisierte Stationsnamen und Distanzen werden vorab berechnet. Zentrale Filterpfade laufen über `Dispatchers.Default`. Status: reduziert, per neuem Logcat zu verifizieren.

### AB-022 — RoutePreview-Beobachtungslücke

State-Übergänge und MapLibre-Style-/Camera-Pfade loggen unter `AbfahrtRoutePreview`. Build 109/110 haben die bisher wahrscheinlichste Ursache in der Zielkoordinaten-Auflösung geschlossen; neue Fälle werden künftig separat erfasst.


## Geschlossen in Build 105

### AB-024 — Detailsheet bleibt bei nicht aufgelöster ORS-Gehzeit auf „Wird ermittelt…"

Logcat Build 104 fix1 zeigt wiederholte ORS-Matrix-Läufe mit `enrichedStops=15/25`; mehrere sichtbare Abfahrten bleiben dadurch ohne `walkDurationSeconds`. Build 105 setzt nach abgeschlossenem Enrichment für verbleibende Stopps mit vorhandener Stationsdistanz einen expliziten ungefähren Fallback (`usesApproximateDistance=true`) und zeigt im Detailsheet „Keine Gehzeit verfügbar" statt eines endlosen Ladezustands. Zusätzlich wird `fallbackToApprox` geloggt, damit echte ORS-Abdeckungslücken später analysiert werden können.


## AB-024 — Gehwegermittlung bleibt bei Fallback unvollständig

**Status:** teilweise geschlossen / weitergeführt in Build 106.

Build 105 verhindert den dauerhaften Ladezustand, ersetzt fehlende ORS-Werte aber teils durch Fallback-/Luftliniendistanz. Das reicht nicht für eine Radius-/Erreichbarkeitsanzeige. Build 106 ergänzt eine Detailansicht-Nacherfassung: Beim Öffnen des Detailsheets liefert die Routenvorschau zugleich Distanz und Dauer aus ORS Directions und zeigt diese direkt im Detailsheet.

## AB-026 — ORS-Matrix-Abdeckung zu niedrig

**Status:** offen, verbessert in Build 106.

15/25 Matrix-Ergebnisse sind für die App zu schwach, da Geh-/Fahrradstrecken die Sortierung und Erreichbarkeit wesentlich prägen. Build 106 dedupliziert Matrix-Kandidaten nach normalisiertem Stationsnamen, damit nicht mehrere Schreibweisen derselben Station die Matrix-Slots verbrauchen. Ziel bleibt 25/25 bzw. maximal 1–2 Ausfälle bei schlechter ORS/OSM-Datenlage.

## AB-025 — AGP/compileSdk-Warnung

**Status:** geschlossen in Build 106.

Gradle Wrapper auf 8.14.5, Android Gradle Plugin auf 8.13.2. Keine `android.suppressUnsupportedCompileSdk=36`-Unterdrückung.

## Geschlossen in Build 109

### AB-031 — Kaltstart zeigt erste Ergebnisse erst nach vollständiger Add-on-Anreicherung

Build 108 wartet funktional auf initiale API-Daten plus Stop-Add-on-Calls, bevor der erste finale Stand sichtbar wird. Build 109 emittiert den initialen deduplizierten API-Stand sofort als vorläufige Liste und lädt fehlende Stopps danach nach.

### AB-032 — Verbindung bleibt sichtbar, obwohl Station und Ziel identisch sind

Wenn die normalisierte Richtung auf die eigene Station zeigt, wird die Abfahrt ausgeblendet. Beispiel: Linie 250 an `U Franz-Neumann-Platz` mit Richtung `U Franz-Neumann-Platz`.

### AB-033 — RoutePreview-Ziel wurde zu streng über exakten Stationsnamen aufgelöst

Die Detailkarte nutzt ORS zwar per Koordinate, aber die UI musste zuerst die Zielstation aus den Stationsdaten finden. Build 109 ergänzt robuste Auflösung über Stations-ID, exakten Namen, normalisierten Namen und konservativen Distanzabgleich.


## Geschlossen in Build 110

### AB-017 — Routenkartenvorschau bleibt nicht verfügbar trotz ORS-Erfolg

Build 109 hat die wahrscheinlich ursächliche Fehlerklasse geschlossen: Die UI fand teilweise keine Zielkoordinate, weil Departure-Stop und Stationsname nur exakt verglichen wurden. Mit AB-033 wird die Zielstation robust über ID, Name, Normalisierung und Distanz gefunden. Build 110 schließt den verbliebenen Restfall mit `stationDistance=0` und zusätzlichem Klammerqualifier.

### AB-034 — RoutePreview-Zielauflösung scheitert bei zusätzlichem Stationsqualifier

Beispiel aus Build 109: `U Franz-Neumann-Platz (Am Schäfersee) (Berlin)` konnte nicht aufgelöst werden, obwohl passende Stationsdaten vorhanden waren. Ursache war, dass die Normalisierung nur den trailing Ortszusatz entfernte, aber den mittleren Qualifier `(Am Schäfersee)` stehen ließ. Build 110 entfernt für technische Stop-/RoutePreview-Zuordnung alle Klammerqualifier und ergänzt einen Unit-Test.


## Geschlossen / generalisiert in Build 111

### AB-034 — RoutePreview-Restfall war zu stationsspezifisch gelöst

Der Build-110-Fix wurde auf eine generische Provider-Normalisierung umgestellt. Betroffen sind nicht nur Berliner Stationsnamen, sondern auch Muster wie `Düsseldorf, Heinrich-Heine-Allee`, `Paris, Châtelet - Les Halles`, `Madrid, Atocha`, trailing `(Ortsname)` und Plattformhinweise.

- AB-035 | geschlossen in Build 112 | Vorläufiger API-deduplizierter Stand wurde bei jedem echten Netzwerk-Refresh emittiert und ließ die Liste springen.
- AB-036 | geschlossen in Build 112 | Normale Refreshes mit unverändertem Standort lösten unnötige ORS-Enrichment-Zyklen aus.


## Geschlossen in Build 113

### AB-037 — Stable Refresh ersetzt vollständige Liste durch temporär unvollständigen API-Response

Build 112 machte die Anzeige ruhiger, setzte den finalen Listenstand aber weiterhin hart auf den aktuellen API-/Add-on-Response. Wenn der Response bei identischem Standort temporär weniger Rohabfahrten enthielt, fiel die gefilterte Liste auf 1–2 Einträge und sprang beim nächsten Refresh wieder zurück. Build 113 aktiviert für normale Refreshes am selben Walking-Origin einen Stable-Merge: vorherige, zeitlich noch gültige Abfahrten bleiben als Grace-State erhalten und werden weiter durch die normale Filter-/Dedup-Pipeline bewertet.

## Geschlossen in Build 116

### AB-043 — Detailsheet zeigt nur die nächste Abfahrtszeit, obwohl lokale Folgeabfahrten vorhanden sind

Die Übersicht begrenzt bewusst auf `maxPerDirection`, dadurch bleibt sie ruhig. Im Detailsheet ist dagegen zusätzlicher Kontext hilfreich: Build 116 sammelt bis zu drei Abfahrten derselben Linie/Richtung/gewinnenden Haltestelle aus dem bereits geladenen lokalen `response.departures`-Datensatz und zeigt sie im Feld „In“ als kompakte Liste. Es werden keine zusätzlichen API-Requests ausgelöst.

## Geschlossen in Build 117

### AB-044 — Detailsheet-Folgeabfahrten bleiben trotz lokaler Rohdaten bei nur einer Zeit

Build 116 filterte Follow-up-Zeiten zu streng über normalisierte Stop-Identität plus Plattformvergleich. Providerdaten können dieselbe physische Haltestelle jedoch als mehrere Stop-/Plattformvarianten liefern. Build 117 verwendet für das Detailsheet einen lokalen Gewinner-Cluster: gleiche Linie/Richtung/Mode und entweder normalisierte gleiche Haltestelle oder sehr nahe effektive Distanz. Dadurch können vorhandene lokale Folgezeiten angezeigt werden, ohne andere klar weiter entfernte Haltestellen derselben Linie/Richtung zu übernehmen.


## AB-045 — Folgeabfahrten im Detailsheet durch sichtbares Zeitfenster begrenzt

Status: Closed in Build 118.

Build 117 showed better follow-up times, but only from data that had already been fetched within the user-visible window. The detail sheet therefore could still show fewer than three times if later departures were outside the configured overview window. Build 118 fetches a wider raw data horizon while keeping the overview filter local.

## AB-046 — Station mit vorhandener Abfahrt kann trotzdem unvollständig sein

Status: Closed in Build 118.

The repository previously fetched direct stop add-ons only for stations with no departure at all in the initial response. This misses stations that are partially covered, for example one direction of a frequent subway line is present but the opposite direction is lost because the initial API response hit limit=60. Build 118 performs bounded nearby stop coverage in stops= batches.


## AB-053 — HERE-Detailsheet zeichnet trotz „Haltestelle erreicht“ eine Route

Status: **Fixed in Build 149 source; real build/runtime evidence pending.**

Symptom:
- Detailsheet zeigt `Entfernung: Hier` und `Zu Fuß: Haltestelle erreicht`, darunter aber weiterhin `Routenkarte` mit blauer ORS-Polyline.

Evidence:
- Feld-Screenshot vom 2026-09-23 zeigt den Widerspruch an Bus 128 / Brienzer Str.

Ursache:
- `isHereOverride()` beeinflusste Distanz-/Gehzeitdarstellung, der separate `produceState`-Pfad für RoutePreview kannte HERE aber nicht und rief bei vorhandenem ORS-Key weiterhin `loadRoutePreview()` auf.

Build-149-Schutz:
- dieselbe HERE-Entscheidung short-circuited die RoutePreview-Policy vor ORS;
- HERE rendert eine MapLibre-Standortkarte mit Query-Origin und Haltestellenmarker ohne Route-Source/LineLayer;
- normale Nicht-HERE-Routenvorschau bleibt unverändert;
- JVM-Test schützt die Policy „HERE lädt nie ORS-RoutePreview“.

## AB-047 — Direct-Stop-Add-ons zeigen Provider-Stop-IDs als Haltestellenname

Status: **Closed in Build 124; runtime accepted 2026-09-12.**

Die Build-122-Feldabnahme schloss die damals beobachteten strukturierten `de:...`-ID-Fälle. Build 123 reproduzierte jedoch den sichtbaren rein numerischen Providerwert `900009173`. Der Logcat belegt gleichzeitig, dass `Station.id=900009173` mit `Station.name=U Seestr./Turiner Str.` vorhanden und für RoutePreview auflösbar ist. Build 124 erweitert daher denselben Bugfixpfad um exakte ID-Erkennung unabhängig vom Stringformat.

Build 118 introduced broader nearby `stops=` coverage. In the Build 118 and Build 119 logcats, candidates carried provider stop IDs such as `de:11000:900011201::6` in `Departure.stop` instead of displayable station names. This is not a UI-label problem; it means repository enrichment did not resolve ID-based stop values back to `Station.name` before display, matching, deduplication and follow-up time handling.

Build 119 was only a partial fix: exact `Station.id` matches worked, but real API data can use platform-suffixed IDs in `Departure.stop` while the known station list contains only the basis ID without `::platform`.

Build 120 fix:
- Station enrichment matches `Departure.stop` against exact `Station.id` and against the basis ID before a `::` platform suffix.
- The ID lookup map also indexes station IDs by both exact and basis ID.
- Unresolved provider IDs are logged as `unresolved provider stop ids after enrichment` with station count and examples.
- Regression tests cover exact ID, platform-ID-via-base-ID and name-based enrichment.


## AB-052 — Rein numerische Provider-Stop-ID umgeht die bisherige ID-Heuristik

Status: **Closed in Build 124; runtime accepted 2026-09-12.**

Symptom:
- In der Hauptliste erscheint bei U6 Richtung Kurt-Schumacher-Platz `900009173` statt des Haltestellennamens.

Evidence:
- Build-123-Logcat enthält `stop=900009173` als dedup-fähigen Gewinner.
- Derselbe Lauf löst `900009173` im RoutePreview-Kontext exakt auf `U Seestr./Turiner Str.` auf.

Ursache:
- `looksLikeProviderStopId()` erkennt absichtlich nur strukturierte IDs mit mindestens zwei `:`.
- Rein numerische Provider-IDs aktivieren dadurch weder das normale ID-Enrichment noch den Post-Merge-Pass.

Build-124-Schutz:
- Ein exakter Match `Departure.stop == Station.id` klassifiziert den Stop als Provider-ID, unabhängig vom Format.
- Der Wert wird für die Anzeige auf `Station.name` aufgelöst und gleichzeitig als `providerStopId` erhalten.
- Rein numerische Stops ohne exakten Stations-ID-Match bleiben unangetastet.
- Regressionstests decken Exact-Numeric-ID, Post-Merge-Numeric-ID und den Nicht-ID-Gegenfall ab.


## AB-050 — Stop-ID bleibt im Batch ungelöst, obwohl der gemergte Response die exakte Station kennt

Status: **Closed after Build-122 field acceptance on 2026-09-12.**

Die mehrtägige Feldnutzung zeigt keine sichtbaren Station-IDs mehr und bestätigt damit den user-visible Fix. Der kausale Mechanismus bleibt der Post-Merge-Enrichment-Pass.

Build 121 zeigt gleichzeitig `stop=de:11000:900011201::6` in der Filter-/Dedup-Pipeline und eine spätere exakte RoutePreview-Auflösung derselben ID über `Station.id` auf `Louise-Schroeder-Platz (Berlin)`. Das einzelne `stops=`-Batch-Enrichment arbeitet also teilweise mit einer unvollständigen Stationssicht, während der Stable-Response-Merge die benötigte Station bereits enthält. Build 122 führt deshalb nach dem Response-Merge einen zweiten Enrichment-Pass gegen die vollständige gemergte Stationsliste aus.


## AB-051 — Provider-Ortszusatz erzeugt künstlich eine zweite Linie+Richtung

Status: **Closed after Build-122 field acceptance on 2026-09-12.**

Die bekannte Alias-Dopplung wurde bei Nutzung an mehreren Tagen/Orten nicht erneut beobachtet. Rohwerte bleiben erhalten; der dedup-relevante Service-Key normalisiert ausschließlich nicht-semantischen trailing Provider-Kontext.

Build 121 behandelt z. B. `128 + U Osloer Str.` und `128 + U Osloer Str. (Berlin)` in Teilen der Dedup-/Limit-Pipeline als verschiedene Richtungen. Dadurch kann ein Roh-ID-Duplikat separat gewinnen und `maxPerDirection=1` umgehen. Build 122 zentralisiert die Linie+Richtung-Identität und entfernt nur trailing Provider-Kontext; echte unterschiedliche Ziele bleiben getrennt.
