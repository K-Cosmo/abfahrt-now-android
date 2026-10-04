# Build 155 Specification — wählbare Abfahrts-Sortierprofile

## Ziel

Build 155 behandelt F-SORT-001: Die Abfahrtsseite erhält eine persistente, einfach verständliche Wahl der Sortierreihenfolge.

Der Build ändert **nur die Präsentationssortierung bereits geladener Abfahrten**. API-Abfragen, First-Paint-Booster, Direct-stop/Add-ons, Filter, Dedup, Stable-Merge, ORS und Location-Logik bleiben fachlich unverändert.

## Runtime-Baseline

- Basis: akzeptierter Build 154.
- geplanter Runtime-Stand: `versionCode = 1550`, `versionName = 1.1.0`.
- `minSdk = 34`, `compileSdk = 37`, `targetSdk = 37` unverändert.
- keine neue Dependency.

## Bestehender Zustand

`DepartureDisplayOrdering` ist die Single Source of Truth der sichtbaren Abfahrtsreihenfolge.

Aktuelle feste Reihenfolge:

1. HERE-Override
2. effektive Entfernung
3. Linie
4. Abfahrtszeit
5. Richtung
6. stabile Tie-Breaker

Die Feldnutzung zeigt, dass im Alltag oft eine zeitnähere Sortierung innerhalb der Entfernung sinnvoller ist. Gleichzeitig ist die bisherige linienorientierte Sicht legitim und soll auswählbar bleiben.

## Produktentscheidung: Sortierprofile statt Sortiermatrix

Die App bietet zunächst genau drei benannte Profile. Es gibt **keinen** freien Drag-and-drop-/Mehrfach-Prioritäteneditor.

### 1. `NEARBY` — „Nähe zuerst“ — neuer Default

Primärreihenfolge:

1. effektive Entfernung
2. Abfahrtszeit
3. Richtung
4. Linie
5. deterministische Tie-Breaker

Ziel: im Alltag zuerst nahe erreichbare Haltestellen, innerhalb derselben bzw. ähnlichen Distanz die nächste relevante Abfahrt.

### 2. `SOONEST` — „Nächste Abfahrt“

Primärreihenfolge:

1. Abfahrtszeit
2. effektive Entfernung
3. Richtung
4. Linie
5. deterministische Tie-Breaker

Ziel: zeitlich nächste Abfahrt unabhängig davon, ob eine andere Haltestelle wenige Meter näher liegt.

### 3. `LINE_GROUPED` — „Linien bündeln“

Primärreihenfolge:

1. effektive Entfernung
2. Linie
3. Abfahrtszeit
4. Richtung
5. deterministische Tie-Breaker

Dies bildet die bisherige fachliche Sortierlogik ab und bleibt als Option erhalten.

## HERE-Semantik

`Departure.displayDistanceMeters()` liefert für HERE (`stationDistance <= 35 m`) bereits effektive Distanz 0.

Daraus folgt:

- `NEARBY`: HERE gewinnt natürlich über Distanz 0. Es braucht keinen zusätzlichen globalen HERE-Schlüssel.
- `LINE_GROUPED`: HERE gewinnt ebenfalls natürlich über Distanz 0.
- `SOONEST`: Abfahrtszeit bleibt tatsächlich der Primärschlüssel. HERE wird erst beim sekundären Distanzvergleich bevorzugt.

Ein global vorgeschalteter HERE-Schlüssel ist deshalb für profilbasierte Sortierung **nicht zulässig**, weil er `SOONEST` semantisch verfälschen würde.

Die sichtbare Kennzeichnung „Hier“ und der bestehende `HERE_DISTANCE_THRESHOLD_METERS = 35` bleiben unverändert.

## Persistenz

Neue fachliche Präferenz:

```text
DepartureSortMode
- NEARBY
- SOONEST
- LINE_GROUPED
```

- Speicherung im bestehenden Preferences-DataStore.
- `AppPreferences` erhält `departureSortMode`.
- Default bei fehlendem gespeicherten Wert: `NEARBY`.
- bestehende Installationen wechseln damit nach Update auf den neuen gewünschten Default; das Legacy-Verhalten bleibt über `LINE_GROUPED` auswählbar.
- unbekannte/ungültige gespeicherte Werte fallen robust auf `NEARBY` zurück.

## UI

Platzierung im bestehenden Settings-Sheet:

```text
Abfahrten pro Richtung
→ Sortierung der Abfahrten
→ Quick-Filter
```

KIS-Vorgabe:

- eine kompakte SettingsSection;
- drei Single-Choice-Zeilen/Radio-Optionen mit Titel und kurzer Reihenfolge als Erläuterung;
- keine engen Segment-Buttons, weil die Bezeichnungen in 22 Locales und auf kleinen Displays robust bleiben müssen;
- kein permanenter Sortierbutton auf der Hauptseite in Build 155.

Vorgeschlagene deutsche Darstellung:

- **Nähe zuerst** — „Entfernung · Abfahrtszeit · Richtung“
- **Nächste Abfahrt** — „Abfahrtszeit · Entfernung · Richtung“
- **Linien bündeln** — „Entfernung · Linie · Abfahrtszeit“

Ein schneller Sortierbutton auf der Abfahrtsseite wird erst dann bewertet, wenn reale Nutzung zeigt, dass Nutzer häufig zwischen Profilen wechseln.

## Runtime-Verhalten bei Änderung

Eine Sortieränderung ist rein lokal:

- vorhandene `DepartureUiState.Success`-Daten werden neu gefiltert/sortiert;
- kein neuer abfahrt.now-Request;
- kein neuer ORS-Request;
- kein `Loading`-State;
- keine Veränderung von Radius, Zeitfenster, Transportmodus, `maxPerDirection`, Reachability oder Walking-Metriken.

`DepartureViewModel` soll die Preference-Änderung wie andere reine Darstellungs-/Filteränderungen über den bestehenden `refilter()`-Pfad anwenden.

## Comparator-Architektur

`DepartureDisplayOrdering` bleibt die einzige Comparator-Quelle.

Ziel-API sinngemäß:

```kotlin
fun comparator(mode: DepartureSortMode): Comparator<Departure>
```

Deterministische Tie-Breaker bleiben für alle Profile erhalten. Die Reihenfolge der fachlichen Primärschlüssel darf nicht an mehreren Stellen dupliziert werden.

## Lokalisierung

Alle neuen sichtbaren Texte müssen in allen 22 Locale-Sets vorhanden sein. Der bestehende Locale-Key-Paritätsgate bleibt verbindlich.

## Scope-Grenzen

Build 155 ändert nicht:

- First-Paint-/Dedup-Booster;
- API-Request-Parameter;
- Add-on-Abdeckung;
- `maxPerDirection`-Semantik;
- Departure-Dedup;
- Stable-Merge;
- Location/HERE-Schwelle;
- ORS-Enrichment und ORS-Key-Verhalten;
- RoutePlanner-Sortierung;
- Update-Checker;
- Signing/Release-Workflow (RELEASE1 bleibt separates Release-Engineering-Paket).

## Acceptance

### Automatisiert

1. Governance/Compatibility/Locale-Gates grün.
2. Unit Tests, Debug, Release/R8 grün.
3. Comparator-Tests für alle drei Profile.
4. HERE-Test:
   - `NEARBY`/`LINE_GROUPED`: HERE verhält sich über Distanz 0 als nächster Eintrag;
   - `SOONEST`: frühere Nicht-HERE-Abfahrt darf vor einer späteren HERE-Abfahrt stehen.
5. Persistenz-/Fallback-Test für `DepartureSortMode` soweit bestehende Testarchitektur dies ohne künstlichen Android-DataStore-Testaufbau sinnvoll erlaubt.

### Realgerät

1. Settings-Auswahl ist auf normalem und kompaktem Display verständlich und nicht abgeschnitten.
2. Default nach Update ohne gespeicherte Sortierpräferenz: „Nähe zuerst“.
3. Wechsel zwischen allen drei Profilen sortiert die bereits sichtbare Liste unmittelbar neu.
4. Sortierwechsel erzeugt keinen neuen abfahrt.now-/ORS-Request und keinen Loading-Screen.
5. Auswahl bleibt nach App-Neustart erhalten.
6. Progressive Build-154-First-Paint-/Add-on-/ORS-Kette bleibt ruhig und funktional.
7. Keine FATAL-/ANR-/Navigation-/Settings-Regression.

## Stop-Regel

Wenn drei Profile in der Settings-UI unnötig komplex wirken oder die Sortierung wegen konkurrierender Sortierpfade nicht lokal umsetzbar ist, wird **nicht** mit einer frei konfigurierbaren Sortiermatrix eskaliert. Dann wird zuerst die Comparator-Single-Source-of-Truth bereinigt.
