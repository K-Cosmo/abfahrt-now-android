# Build 154 Specification — Current-Location First-Paint Fast Path

## Ziel

Build 154 behandelt ausschließlich F-153-001: den in Build 153 reproduzierten Warteblock von ca. 2,6–3,0 s zwischen Departure `Loading` und dem ersten abfahrt.now-Core-Request.

Die bestehende progressive Departure-Pipeline wird **nicht** neu entworfen. Sie ist bereits der gewollte Kompromiss aus frühem First Paint und ruhiger, schrittweiser Präzisierung:

1. initialer abfahrt.now-Call;
2. auf leerem Kaltstartscreen optionaler API-seitig deduplizierter First-Paint-Booster;
3. Direct-stop/Add-on-Nachladen mit `dedup=off`;
4. app-eigene Filter-/Dedup-/Sortierlogik;
5. finaler Core-State;
6. ORS-Enrichment asynchron danach;
7. Same-Origin-Refreshes bleiben Stable-Merge.

Build 154 optimiert deshalb **nur die Current-Location-Auflösung vor dem ersten Core-Request**.

## Runtime-Baseline

- Basis: akzeptierter Build 153.
- geplanter Runtime-Stand bei Implementierung: `versionCode = 1540`, `versionName = 1.1.0`.
- `minSdk = 34`, `compileSdk = 37`, `targetSdk = 37` unverändert.
- keine neue Dependency.
- keine Änderung an abfahrt.now-/ORS-/Photon-Verträgen.
- keine Änderung an Radius, Zeitfenster, Transportfiltern, Dedup, Sortierung, Folgezeiten, RoutePlanner oder Community-/Access-Gate-Semantik.

## Audit des bestehenden Standortvertrags

Die erneute Prüfung von `/doc`, aktuellem Code und Projektverlauf ergibt:

1. **Relevante Standortabweichung ist bereits definiert:** `MOVEMENT_THRESHOLD_M = 200f`.
   - `< 200 m` gilt als derselbe Standortkontext / Walking-Origin.
   - `>= 200 m` löst einen Location-Hard-Reset aus.
   - Läuft bereits ein Core-Load, wird der Hard-Reset genau über den bestehenden `pendingLocationRefresh`-/`pendingHardResetRefresh`-Pfad nachgezogen.
2. **Bei relevantem Standortwechsel bleibt die bisher sichtbare Liste bis zum Ersatzresultat stehen**, wird danach aber nicht per Same-Origin-Stable-Merge in den neuen Standortkontext übernommen.
3. **ORS darf den Core nicht blockieren** und vorhandene ORS-Metriken dürfen bei gleichem Origin weitergetragen werden.
4. **Der API-Dedup-Booster ist nur für den leeren Kaltstartscreen vorgesehen.** Sobald bereits Daten sichtbar sind, wird kein deduplizierter Zwischenstand mehr für normale Refreshes emittiert.
5. Für `FusedLocationProviderClient.lastLocation` existiert dagegen **keine separate, normative Alters- oder Accuracy-Schwelle**. Weder `/doc` noch der aktuelle Code definieren dafür z. B. 30 s, 60 s, 5 min oder einen Meterwert. Solche Werte dürfen Build 154 deshalb nicht neu erfinden.
6. Der aktuelle Code akzeptiert `lastLocation` bereits ohne Alters-/Accuracy-Prüfung als Fallback, allerdings **erst nachdem** `getCurrentLocation(PRIORITY_HIGH_ACCURACY)` keinen Wert geliefert hat. Build 154 ändert damit nicht die zulässige Standortquelle, sondern nur deren zeitliche Nutzung für First Paint.

## Neue Build-154-Semantik

### 1. Provisional Origin statt erfundener „Freshness“-Magic-Number

Auf einem **leeren Current-Location-Kaltstart** darf eine vorhandene `lastLocation` sofort als **provisorischer Origin** für den ersten Core-Request dienen.

`lastLocation` wird dabei ausdrücklich **nicht** als „frisch/final“ umgedeutet. Sie ist nur ein First-Paint-Anker, der parallel durch den bereits verwendeten frischen High-Accuracy-Fix überprüft wird.

Damit gilt:

- `lastLocation != null` und Koordinaten gültig: erster Core-Request darf starten, ohne auf `getCurrentLocation(PRIORITY_HIGH_ACCURACY)` zu warten;
- `lastLocation == null`: bisheriger High-Accuracy-Pfad bleibt unverändert;
- High-Accuracy-Fix wird parallel weiter angefordert;
- fällt der High-Accuracy-Fix aus bzw. liefert `null`, bleibt die provisorische Position wie beim heutigen Fallback die verfügbare Position; es wird kein künstlicher Retry-Sturm erzeugt.

Es wird **kein eigener persistenter Standortcache** in DataStore eingeführt.

### 2. Bereits definierte 200-m-Regel entscheidet über Re-Anchor

Wenn der spätere High-Accuracy-Fix gegenüber dem provisorischen Origin

- **< 200 m** abweicht: gleicher Standortkontext; **kein zweiter Core-Request**, kein Hard-Reset und kein neuer ORS-Zyklus allein wegen dieser Korrektur;
- **>= 200 m** abweicht: bestehende Movement-/Hard-Reset-Semantik greift; genau ein neuer Core-Load für den neuen Standortkontext wird ausgelöst bzw. bei laufendem Load über den bestehenden Pending-Pfad nachgezogen.

Es wird **keine zweite Re-Anchor-Schwelle** eingeführt.

### 3. Progressive Anzeige bleibt unverändert

Der schnelle provisorische Origin darf die bestehende First-Paint-Architektur nicht verändern:

- der initiale API-Dedup-Booster bleibt auf den leeren Kaltstartscreen begrenzt;
- Add-on-/Direct-stop-Abdeckung bleibt gebündelt und `dedup=off`;
- app-eigene Filter-/Dedup-/Sortierlogik bleibt Quelle der sichtbaren Ordnung;
- ORS bleibt asynchron nach dem Core;
- ein Re-Anchor `>= 200 m` ist ein Hard-Reset-Kontextwechsel: alte sichtbare Daten dürfen bis zum Ersatzresultat stehen bleiben, dürfen aber nicht in den neuen Kontext Stable-gemerged werden;
- stale asynchrone Ergebnisse des alten Kontexts dürfen den neuen Zustand nicht überschreiben.

### 4. Request- und ORS-Ruhe

Build 154 darf aus dem Fast Path keinen Request-Sturm erzeugen:

- bei High-Accuracy-Abweichung `< 200 m` bleibt es bei **einem** Core-Zyklus;
- bei High-Accuracy-Abweichung `>= 200 m` sind maximal der provisorische Core-Zyklus plus **ein** erzwungener Ersatz-Core-Zyklus zulässig;
- wenn der Re-Anchor bereits bekannt ist, bevor ORS für den provisorischen Origin gestartet wird, soll kein neuer ORS-Enrichment-Zyklus für den bereits verworfenen Origin begonnen werden;
- ein bereits laufendes ORS-Enrichment darf bei Context-Wechsel nicht mehr auf den neuen Zustand angewendet werden; bestehende Cancellation-/Stale-Guards sind zu verwenden, nicht zu duplizieren.

## Scope-Grenzen

Build 154 ändert **nicht**:

- den 200-m-Movement-Threshold;
- HERE `<= 35 m`;
- die 20-m-Nearest-Toleranz;
- Nutzer-Radius oder Zeitfenster;
- Refresh-Intervall bzw. `<60s`-Same-Snapshot-Throttle;
- ORS-Batching (max. drei Batches à 25 Stationen);
- First-Paint-Booster-/Stable-Merge-/Hard-Reset-Semantik;
- Alternate-Location-Flow;
- RoutePlanner;
- AccessGate/Permission-Verhalten aus Build 152;
- ORS-Key-Validierung (F-ORS-001/B-ORS-001 bleibt separat).

Insbesondere werden API-Daten-Freshness (`refreshIntervalMinutes`, 60-s-Throttle) und Location-Freshness **nicht** miteinander vermischt.

## Implementierungsgrenze

Der kleinstmögliche Eingriff bleibt im bestehenden `DepartureViewModel`-/FusedLocation-Pfad. Keine neue Location-Repository-Schicht, kein globaler Location-Singleton und keine neue State-Architektur.

Der Build darf einen kleinen internen Helper für testbare Re-Anchor-/Startentscheidung extrahieren, wenn dadurch die bestehende 200-m-Regel wiederverwendet statt dupliziert wird. Kein Big-Bang-Refactor des großen `DepartureViewModel`.

## Acceptance

### Automatisiert

1. Static/Governance/Compatibility-Gates grün.
2. committed Gradle wrapper grün.
3. Unit Tests grün.
4. Debug-Build grün.
5. Release/R8 grün.
6. Regressionstest/Policy-Test für:
   - provisorische Position vorhanden + frischer Fix `< 200 m` => kein zweiter Core;
   - provisorische Position vorhanden + frischer Fix `>= 200 m` => genau ein Hard-Reset-Re-Anchor;
   - keine `lastLocation` => bisheriger Fresh-Location-Pfad;
   - Target-Wechsel während ausstehender Korrektur => Korrektur darf nicht in falschen Zielkontext schreiben.

### Realgerät

1. Mindestens drei saubere Cold Starts mit vorhandener `lastLocation` und `AbfahrtStartup`/`AbfahrtLocation`/OkHttp-Korrelation.
2. Der erste Core-Request darf in diesem Fall nicht mehr auf den Abschluss des High-Accuracy-Fixes warten. Entscheidend ist die Reihenfolge im Log, nicht eine erfundene Millisekunden-Zielzahl.
3. Messwerte werden gegen Build-153-Baseline (`Loading`→Core ca. 2,59–3,02 s) verglichen und der reale Gewinn dokumentiert.
4. Fall `< 200 m`: genau ein Core-Zyklus, bestehende progressive Booster/Add-on/ORS-Kette, keine zusätzliche Listenersortierungs-/Refresh-Unruhe.
5. Fall `>= 200 m` soweit praktisch reproduzierbar: provisorischer Stand darf bis zum Ersatzresultat sichtbar bleiben; danach neuer Standortkontext ohne Same-Origin-Stable-Merge; kein dritter Core-Request.
6. Keine neue App-FATAL-/ANR-/Navigation-/Permission-/AccessGate-Regression.
7. ORS bleibt hinter Core und erzeugt keinen zusätzlichen Zyklus für einen bereits als falsch erkannten provisorischen Origin.

## Stop-Regel

Wenn der Fast Path auf dem Realgerät keinen klaren früheren ersten Core-Request bringt oder sichtbar störende Falschstand-/Refresh-Sprünge erzeugt, wird Build 154 nicht allein wegen theoretischer Plausibilität akzeptiert. Dann bleibt Build 153 die Runtime-Baseline und der Location-Pfad wird nicht weiter verkompliziert.
