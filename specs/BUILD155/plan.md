# Build 155 Plan

## Zielbild

Build 155 führt eine persistente Sortierpräferenz für die Abfahrtsseite ein. Der Eingriff bleibt lokal in Model/Preferences, `DepartureDisplayOrdering`, bestehendem `refilter()`-Pfad und Settings-UI.

Keine Netzwerk-/Provider-/Merge-/ORS-Änderung.

## Phase A — Sortiermodell

1. `DepartureSortMode` als kleines Enum im bestehenden Datenmodell ergänzen:
   - `NEARBY`
   - `SOONEST`
   - `LINE_GROUPED`
2. Default `NEARBY`.
3. robuste `fromCode()`-/Fallback-Logik für persistierte Werte.
4. `AppPreferences.departureSortMode` ergänzen.

## Phase B — DataStore

1. neuen String-Key `departure_sort_mode` in `UserPreferencesRepository` ergänzen.
2. Lesen mit Default/Fallback `NEARBY`.
3. `updateDepartureSortMode(mode)` ergänzen.
4. keine Migrationstabelle und keine zweite Preferences-Schicht.

## Phase C — Comparator als Single Source of Truth

`DepartureDisplayOrdering.comparator(mode)` implementieren.

### `NEARBY`

```text
effective distance
→ timestamp
→ direction
→ line
→ stop/platform/mode tie-breaker
```

### `SOONEST`

```text
timestamp
→ effective distance
→ direction
→ line
→ stop/platform/mode tie-breaker
```

### `LINE_GROUPED`

```text
effective distance
→ line
→ timestamp
→ direction
→ stop/platform/mode tie-breaker
```

Der bisher separat vorgeschaltete HERE-Schlüssel wird nicht global weitergeführt. `displayDistanceMeters()` liefert HERE bereits als Distanz 0. So bleiben Distance-first-Profile HERE-first, während `SOONEST` tatsächlich zeit-first ist.

## Phase D — ViewModel/refilter

1. `departureDisplayComparator()` verwendet `preferences.value.departureSortMode` bzw. den Snapshot, den der Filterpfad ohnehin erhält.
2. Preference-Collector merkt sich `prevDepartureSortMode`.
3. Änderung des Sortiermodus löst ausschließlich `refilter()` aus.
4. kein `refresh()`, kein neuer Core-Request, kein ORS-Neustart.
5. bestehende progressive Updates dürfen weiterhin bei neuen Walking-Metriken neu sortieren; Build 155 verändert dieses Verhalten nicht.

Falls `applyFilters()` den Comparator ohne Preferences-Snapshot aufruft, dessen Signatur minimal erweitern statt globale Nebenkanäle einzuführen.

## Phase E — Settings wiring

1. Callback vom App-/Screen-Wiring bis `prefsRepo.updateDepartureSortMode()` ergänzen.
2. neue `SettingsSection` nach „Abfahrten pro Richtung“ und vor Quick-Filtern.
3. drei full-width Single-Choice-Zeilen mit RadioButton:
   - Titel
   - kurze fachliche Reihenfolge als Subtitle
4. Auswahl sofort persistieren.
5. keine zusätzliche Apply-/Save-Schaltfläche.

## Phase F — Lokalisierung

Neue Keys sinngemäß:

```text
section_departure_sort
sort_nearby_title
sort_nearby_hint
sort_soonest_title
sort_soonest_hint
sort_line_grouped_title
sort_line_grouped_hint
```

Alle 22 Locale-Sets müssen Key-Parität halten. Formulierungen dürfen sprachlich natürlich sein; die fachliche Reihenfolge muss jedoch erhalten bleiben.

## Phase G — Tests

Fokussierte JVM-Tests für:

1. `NEARBY`: Entfernung vor Zeit; bei gleicher Entfernung Zeit vor Richtung.
2. `SOONEST`: Zeit vor Entfernung.
3. `LINE_GROUPED`: Entfernung vor Linie vor Zeit.
4. HERE 0 m in Distance-first-Profilen.
5. `SOONEST`: frühere Nicht-HERE-Abfahrt vor späterer HERE-Abfahrt.
6. deterministische Tie-Breaker.
7. Default/Fallback `NEARBY`.

Bestehende Sortier-/Filtertests auf neue API anpassen, nicht duplizieren.

## Phase H — Runtime Evidence

Auf Realgerät:

1. Build/Version prüfen (`versionCode 1550`).
2. Settings öffnen; Position und Lesbarkeit der Sortiersektion prüfen.
3. mit bereits geladener Abfahrtsliste nacheinander alle drei Profile wählen.
4. Logcat parallel beobachten: kein neuer `/departures`-/ORS-Request allein durch Sortierwechsel.
5. App vollständig neu starten und Persistenz prüfen.
6. Cold-Start-/First-Paint-Smoke, damit Build-154-Optimierung nicht regressiert.
7. mindestens ein HERE-Fall oder gezielter Comparator-Test belegt die profilspezifische HERE-Semantik.

## Nicht Teil dieses Builds

- Sortierbutton in der TopAppBar.
- frei anordenbare Sortierkriterien.
- auf-/absteigende Richtung pro Kriterium.
- eigene Sortierung im RoutePlanner.
- Release-Signing.
- ORS-Key-Probe.
