# Build 155 Tasks

## Specification / Audit
- [x] F-SORT-001 aus Feldnutzung aufnehmen.
- [x] aktuellen `DepartureDisplayOrdering`-Comparator prüfen.
- [x] Preferences-/DataStore- und Settings-Platzierung prüfen.
- [x] KIS-Entscheidung: drei Profile statt frei konfigurierbarer Sortiermatrix.
- [x] HERE-Semantik pro Profil festlegen.
- [x] Specification und Plan erstellen.

## Implementation
- [ ] `versionCode = 1550` setzen.
- [ ] `DepartureSortMode` mit `NEARBY`, `SOONEST`, `LINE_GROUPED` ergänzen.
- [ ] Default/Fallback `NEARBY`.
- [ ] `AppPreferences.departureSortMode` ergänzen.
- [ ] DataStore-Key + Reader + `updateDepartureSortMode()` ergänzen.
- [ ] `DepartureDisplayOrdering.comparator(mode)` als einzige Comparator-Quelle implementieren.
- [ ] globalen HERE-Vorrang aus dem profilabhängigen Comparator entfernen; HERE über bestehende effektive Distanz 0 behandeln.
- [ ] Preference-Änderung im `DepartureViewModel` ausschließlich über `refilter()` anwenden.
- [ ] Settings-Wiring ergänzen.
- [ ] Sortiersektion nach „Abfahrten pro Richtung“ und vor Quick-Filtern ergänzen.
- [ ] keine TopAppBar-/Hauptseiten-Sortiersteuerung hinzufügen.
- [ ] neue Texte in allen 22 Locale-Sets ergänzen.

## Automated Verification
- [ ] Locale-Key-Parität grün.
- [ ] Governance/Compatibility gates grün.
- [ ] Unit Tests grün.
- [ ] Debug build grün.
- [ ] Release/R8 build grün.
- [ ] Test `NEARBY`: distance → time → direction → line.
- [ ] Test `SOONEST`: time → distance → direction → line.
- [ ] Test `LINE_GROUPED`: distance → line → time → direction.
- [ ] Test HERE in Distance-first-Profilen.
- [ ] Test `SOONEST`: frühere Nicht-HERE-Abfahrt darf spätere HERE-Abfahrt überholen.
- [ ] Test Default/Fallback `NEARBY`.

## Runtime Evidence
- [ ] installierter Stand `versionCode=1550`, `versionName=1.1.0`.
- [ ] Settings-Sortiersektion visuell auf Realgerät prüfen.
- [ ] alle drei Profile mit bereits sichtbarer Liste umschalten.
- [ ] belegen: Sortierwechsel erzeugt keinen `/departures`-Request.
- [ ] belegen: Sortierwechsel erzeugt keinen ORS-Request und keinen Loading-State.
- [ ] Persistenz nach vollständigem App-Neustart prüfen.
- [ ] Build-154-Cold-Start-Fast-Path regressionsfrei.
- [ ] keine FATAL-/ANR-/Navigation-/Settings-Regression.

## Convergence
- [ ] F-SORT-001 nach positiver Evidence schließen.
- [ ] dauerhafte Sortierprofile und Default nach `/doc` konvergieren.
- [ ] Build 155 als Release-Kandidat für RELEASE1 markieren, sofern alle Gates grün sind.
- [ ] F-ORS-001/B-ORS-001 separat halten.
- [ ] RELEASE1 Signing separat halten; keine Secrets in Build 155.
