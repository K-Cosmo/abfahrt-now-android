# Backlog

### B-149-001 HERE-Detailsheet als Standortkarte
- **Status:** implemented in source; real Gradle/runtime evidence pending.
- Feld-Screenshot belegt AB-053: `Hier`/`Haltestelle erreicht`, aber weiterhin ORS-Polyline und `Routenkarte`.
- Build 149 short-circuited ORS für HERE und zeigt bei vorhandenen Koordinaten nur Query-Origin + Haltestellenmarker.
- Nicht-HERE-RoutePreview bleibt unverändert; AB-018 Performance folgt separat als Messbuild.

### B-148-001 Raw Photon query + location-bias ranking
- **Status:** accepted. Corrected Build 148 passed `:app:testDebugUnitTest :app:assembleDebug`; runtime + field feedback confirm D-069/Photon ranking.
- entfernt City-/PLZ-/Transit-Escape-Heuristik aus der allgemeinen Zielsuche; `q` bleibt semantisch unverändert.
- behält vorhandenen Photon-`lat/lon`-Bias, entfernt das zusätzliche allgemeine Client-Re-Ranking und protokolliert die ersten fünf Rohkandidaten.
- separater Stationsflow bleibt unverändert; kein zusätzlicher Request und keine neue Dependency.

### B-147-001 Photon locality-scope correction
- **Status:** superseded by Build 148 after real Build-147 evidence.
- Build-146-Regression F-146-004: `Potsdam` und `S Potsdam` wurden fälschlich auf `, Berlin` gescoped.
- Build 147 lässt einwortige Fernorte und explizite Transit-Qualifier (`S`, `U`, `S+U`, `U+S`, `Bf`, `Bhf`, `Hbf`, `Bahnhof`) global, behält den City-Default aber für mehrwortige unqualifizierte Ziele.
- fokussierte Unit-Tests; kein zusätzlicher Photon-Request, keine Dependency-/Ranking-/Routingänderung.

### B-142-001 Route UI convergence
- **Status:** accepted.
- Region aus sichtbarem RoutePlanner entfernt; bestehende Verkehrsmittel-Symbole in Transit-Legs wiederverwendet. Realer Gradle-Gate grün; Symbolplatzierung wurde als UX-Follow-up in Build 143 übernommen.

### B-143-001 Gespeicherte Orte Zuhause/Arbeit
- **Status:** accepted.
- zwei lokale Slots über bestehendes Menü und Photon; speichern Titel/Untertitel/Lat/Lon in bestehendem DataStore; im RoutePlanner als Schnellwahl für Von/Nach. Zusätzlich Route-Leg-Symbol + Linienbadge wie in der Hauptansicht vertikal gestapelt. Realer kombinierter Gradle-Gate grün; Nutzer bestätigt die Funktion.

### B-144-001 Gespeicherte Ziele direkt auf der Startseite
- **Status:** accepted.
- konfigurierte Zuhause-/Arbeit-Slots werden bei Fokus im leeren Startseiten-Routingfeld als Schnellziele angeboten; Auswahl öffnet direkt den bestehenden Planner. Realer kombinierter Gradle-Gate grün; Nutzer bestätigt Funktion.

### B-145-001 Route sorting / result presentation convergence
- **Status:** accepted.
- Früheste (default), Schnellste, Wenig Umstiege, Wenig Fußweg; rein clientseitig auf vorhandenen Trip-/Leg-Daten, deterministische Tie-Breaker.
- Route-Leg-Kopf visuell nachgezogen: Symbol über Linienbadge bleibt, Richtung/Gehzeit richtet an Badge-Basislinie aus; Walking-Legs zeigen keine transit-spezifische Delay-/Platform-Metadaten.

### B-146-001 Routing time selection
- **Status:** blocked by API contract.
- aktuelles `/trips` unterstützt nur Start-/Zielkoordinaten. Keine clientseitige Zeit-Simulation; erst nach offizieller API-Erweiterung um Abfahrts-/Ankunftszeit.


## P0 — nächstes Evidence-Gate

### B-141-001 RoutePlanner Empty/Error/Coverage convergence
- Empty/Error als retry-fähige Zustände statt passiver Meldung.
- Region bei leerem 200-Ergebnis sichtbar halten, optionale Attribution darstellen.
- `success`/`empty`/`error` für künftige Mehrregionen-/Coverage-Evidence protokollieren, ohne Ortsnamen.
- keine heuristische Out-of-Coverage-Aussage ohne API-Beleg.
- kombinierter Gradle-Gate + normaler Success-Smoke + praktischer Empty/Error-Smoke.

## P0 — danach

- **Status:** accepted. Retry/Empty/Error-Konvergenz real gebaut; keine Transitkarte ohne Geometrie.

## P2 — laufendes Hardening / Optionen


### B-DOC1-014 abfahrt.now Contract-Regressionstests
Build 134 implementierte die fokussierte Parser-/Modellbaseline. Der neue Contract-Test lief im kombinierten Gate ohne eigenen Fehler; der Gesamttask scheiterte ausschließlich an drei bestehenden Android-Log-Stubfehlern, die Build 135 isoliert behebt. Reale API-Samples ohne Secrets bleiben bei künftigen Contract-Änderungen erwünscht.

### B-DOC1-015 `Station.walkSeconds` bewerten
Build 134 beobachtete zweimal 40/40 positive `walkSeconds`. Damit ist das Feld für den getesteten Pfad real belegt. Nächste Produktentscheidung separat: höchstens als WALK-Fallback/Provisional-ETA evaluieren, nicht als pauschaler ORS-Ersatz; Gehwegdistanz, Bike-Zeit, Geometrie und Mehrregionen-Evidence fehlen weiterhin.

### B-DOC1-016 `/journey` bewerten
Die neue API-Fähigkeit für die verbleibenden Stopps einer konkreten Fahrt ist funktional von den vorhandenen lokalen Folgeabfahrtszeiten zu unterscheiden. Erst UX-Nutzen definieren, dann Integration planen.

### B-DOC1-009 Provider-Stop-Point-/Plattform-Semantik (AB-049)
Mehrregionen-/API-Beleg sammeln, Verhältnis von `providerStopId ::N` zu `platform` klären.

### B-DOC1-010 Datengetriebene Linienfarben
GTFS/API-Farben statt wachsender lokaler Hardcode-Liste, ohne Cross-Region-Fehlfarben.

### B-DOC1-011 ViewModel-/Lifecycle-Hotspots
AB-010/011/013 und B-005 nur mit konkretem Anlass abbauen; kein Big-Bang-Refactor.

### B-DOC1-012 ORS-/RoutePreview-Restbeobachtung
ORS-Abdeckung/Jank/MapLibre-Lifecycle nur anhand neuer Evidence weiter optimieren.

### B-DOC1-013 Reproduzierbares Source-Paket
Gradle-Wrapper-Skripte und Wrapper-JAR wieder vollständig ausliefern oder Verpackungsprozess entsprechend dokumentiert automatisieren.

## Abgeschlossen

- **B-142-001 Route UI convergence:** realer Gradle-Gate grün; Region entfernt und Transport-Symbolik im RoutePlanner angekommen; vertikale Symbolanordnung folgt in Build 143.

- **B-140-001 Route-Details / Transferklarheit:** kombinierter Gradle-Gate grün; Screenshot bestätigt `4 min Umstieg`, Access-Walk ohne künstlichen Umstieg und expandierte Zwischenhalte; `/trips` HTTP 200 mit sechs Optionen.

- **B-139-001 Route-Presentation-Cleanup:** kombinierter Gradle-Gate grün; realer deutscher Dark-Theme-Screenshot bestätigt lokalisierte Gehsegmente, keine sichtbaren technischen Koordinaten und klarere Linien-/Kartenhierarchie.
- **B-138-001 Route-planen-MVP:** korrigierter Build real gebaut; `/trips`-RoutePlanner runtime-fähig, Startseiten-Zielsuche und erster Routing-Flow bestätigt.

- **B-137-001 Photon-Nebenflow auf eigene Menü-Seite:** realer kombinierter Gradle-Gate grün; Menü/Sprachpakete und Nebenflow vom Nutzer bestätigt. Ein nicht-blockierender AutoMirrored-Icon-Warning wird in Build 138 bereinigt.

- **B-136-001 Pflichtauthentifizierung abfahrt.now:** Implementierung abgeschlossen; Erststart ohne Key und normaler Start mit Key real bestätigt. Settings-Delete/gezielter 401-Pfad bleiben normale Regressionstests.
- **B-135-001 JVM Unit-Test Isolation / Test Hygiene:** realer kombinierter Gradle-Gate vollständig grün (`testDebugUnitTest` + `assembleDebug`).
- **B-133-001 Kotlin-2.3-Warning-Hygiene:** realer Build erfolgreich, sieben bekannte Warnings verschwunden; Runtime-Smoke für den isolierten Scope unauffällig.
- **B-DOC1-008 Android 17 / API 37:** Build 131 Toolchain-Staging + Build 132 `targetSdk 37`; realer Build, öffentliche HTTPS-Endpunkte, Search, ORS und RoutePreview erfolgreich.
- **B-DOC1-006 API-Key-Sicherheit:** Build 125 implementiert und feldseitig abgenommen; Migration beider Altwerte, verschlüsselte Writes und Providerzugriffe bestätigt.
- **B-126-001 erster 16-KB-Artefaktaudit:** abgeschlossen mit negativer Evidence. ZIP-Ausrichtung war grün; inkompatible Native-ELFs lösten Build 127 aus.
- **B-DOC1-007 16-KB-Page-Size-Readiness:** Build 130 vollständig abgenommen: APK 64-Bit-Gate grün, AAB `PAGE_ALIGNMENT_16K`, echter Runtime-Smoke mit Page Size 16384.
- **B-124-001 Numerische Provider-Stop-ID Runtime-Abnahme:** Device-Logcat bestätigt den reproduzierten U6-Fall ohne sichtbare `900009173`; AB-047/052 geschlossen.

- **B-DOC1-002 ORS-Hostmigration:** Build 123 migriert ausschließlich auf `https://api.heigit.org/openrouteservice/`; Device-Logcats belegen WALK-Matrix und WALK/BIKE-Directions auf dem HEIGIT-Pfad ohne Rückfall auf den Alt-Host.

- **B-DOC1-001 Build-122-Runtime-Abnahme für AB-047/050/051:** durch mehrtägige Feldnutzung an verschiedenen Orten für die sichtbaren Regressionen abgenommen.
- **B-DOC1-003 abfahrt.now Contract-Klärung:** aktualisierte OpenAPI konvergiert; `stationDistance` ist intern aus `Station.distance` abgeleitet. Weitergehende Contract-Tests bleiben als B-DOC1-014.
- **B-DOC1-004 Nearest-20m-Abweichung:** als bewusste technische Toleranz entschieden (D-040).
- **B-DOC1-005 HERE-/Map-Restthema:** **wieder geöffnet durch reale Evidence 2026-09-23; Umsetzung in Build 149 (AB-053/D-070), Runtime-Abnahme ausstehend.**

## Übernommene historische Backlog-Punkte

Die früheren Punkte B-001 ff. bleiben als Historie unter `/evidence/legacy-docs/BACKLOG.md` erhalten. Soweit noch relevant, sind sie oben in die aktuelle Priorität konvergiert.

## 2026-09-12 16-KB remediation update after Build 129

- **Build 129 accepted for MapLibre scope:** MapLibre 13.6.0 und DataStore 1.2.1 sind auf arm64/x86_64 `OK ELF`; ZIP-Alignment bleibt grün.
- **P1 / Build 130:** veröffentlichtes `graphics-path:1.1.0` wird wegen der real belegten RELRO-Fehler vollständig ausgeschlossen und durch eine minSdk-34-gebundene Kotlin/Framework-Kompatibilitätsschicht ersetzt.
- **Stop rule:** kein weiterer Dependency-Versionssprung für 16 KB. Build-130-Release-APK entscheidet, danach nur noch Runtime-Smoke oder konkrete Compile/Runtime-Findings.
- **Android 17/API 37** bleibt getrennt und folgt erst nach Abschluss des 16-KB-Gates.

## 2026-09-13 Android-17 staging update

- **Build 130 accepted:** 16-KB-Thema geschlossen.
- **Build 131:** Toolchain + `compileSdk 37`, `targetSdk 36` — abgenommen.
- **Build 132:** `targetSdk 37` — abgenommen; Android-17-Migration abgeschlossen.
- **Build 133:** Kotlin-2.3-Warning-Hygiene — accepted.
- **Build 134:** API-Contract-/`walkSeconds`-Observation — observation accepted; Runtime 40/40 zweimal, Test-Gesamtgate mit drei reinen Android-Log-Stubfehlern an Build 135 übergeben.
- **Build 135:** JVM Unit-Test Isolation / Test Hygiene — accepted; kombinierter Unit-/Debug-Gate grün.
