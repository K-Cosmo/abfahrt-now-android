# Build 155 Tasks

## Specification / Audit
- [x] F-SORT-001 aus Feldnutzung aufnehmen.
- [x] aktuellen `DepartureDisplayOrdering`-Comparator prüfen.
- [x] Preferences-/DataStore- und Settings-Platzierung prüfen.
- [x] KIS-Entscheidung: drei Profile statt frei konfigurierbarer Sortiermatrix.
- [x] HERE-Semantik pro Profil festlegen.
- [x] Specification und Plan erstellen.

## Implementation
- [x] `versionCode = 1550` setzen.
- [x] `DepartureSortProfile` mit `NEARBY`, `SOONEST`, `LINE_GROUPED` ergänzen.
- [x] Default/Fallback `NEARBY`.
- [x] `AppPreferences.departureSortProfile` ergänzen.
- [x] DataStore-Key + Reader + `updateDepartureSortProfile()` ergänzen.
- [x] `DepartureDisplayOrdering.comparator(profile)` als einzige Comparator-Quelle implementieren.
- [x] globalen HERE-Vorrang aus dem profilabhängigen Comparator entfernen; HERE über bestehende effektive Distanz 0 behandeln.
- [x] Preference-Änderung im `DepartureViewModel` ausschließlich über `refilter()` anwenden.
- [x] Settings-Wiring ergänzen.
- [x] Sortiersektion nach „Abfahrten pro Richtung“ und vor Quick-Filtern ergänzen.
- [x] keine TopAppBar-/Hauptseiten-Sortiersteuerung hinzufügen.
- [x] neue Texte in allen 22 Locale-Sets ergänzen.

## Automated Verification
- [x] Locale-Key-Parität grün.
- [x] Governance/Compatibility gates grün.
- [x] Unit Tests grün.
- [x] Debug build grün.
- [x] Release/R8 build grün.
- [x] Test `NEARBY`: distance → time → direction → line.
- [x] Test `SOONEST`: time → distance → direction → line.
- [x] Test `LINE_GROUPED`: distance → line → time → direction.
- [x] Test HERE in Distance-first-Profilen.
- [x] Test `SOONEST`: frühere Nicht-HERE-Abfahrt darf spätere HERE-Abfahrt überholen.
- [x] Test Default/Fallback `NEARBY`.
- [x] Android CI #125 auf Runtime-Head `02ac2a6…` vollständig grün.

## Runtime Evidence
- [x] installierter Build-155-Stand mit neuer Sortiersektion auf Realgerät geprüft.
- [x] Settings-Sortiersektion visuell geprüft und vom Nutzer als passend bestätigt.
- [x] alle drei Profile mit bereits sichtbarer Liste umgeschaltet; Hauptseite sortiert jeweils entsprechend.
- [x] Sortierwechsel erzeugt keinen unmittelbar gekoppelten `/departures`-Request; beobachtete spätere Requests gehören zum normalen Stable-Refresh.
- [x] Sortierwechsel erzeugt keinen zusätzlichen ORS-Zyklus und keinen `departure_state_loading`-Zustand.
- [x] Persistenz nach vollständigem App-Neustart geprüft; gewähltes Profil bleibt erhalten.
- [x] Build-154-Cold-Start-/Location-Pfad zeigt im Sortier-Smoke keine fachliche Regression.
- [x] keine FATAL-/ANR-/Navigation-/Settings-Regression im Abnahmeumfang beobachtet.

## Convergence
- [x] F-SORT-001 nach positiver Evidence schließen.
- [x] dauerhafte Sortierprofile und Default nach `/doc` konvergieren.
- [x] Build 155 als Release-Kandidat für RELEASE1 markieren.
- [x] F-ORS-001/B-ORS-001 separat halten.
- [x] RELEASE1 Signing separat halten; keine Secrets in Build 155.

## Acceptance 04.10.2026

Build 155 ist fachlich und technisch accepted. Reale Evidence:

- UI-Screenshot bestätigt die Settings-Platzierung nach „Abfahrten pro Richtung“ und die drei verständlichen Radio-Profile.
- Nutzer bestätigt, dass die Abfahrtsseite nach Auswahl jeweils entsprechend sortiert.
- Logcat zeigt lokale Refilter-Ergebnisse für `SOONEST`, `LINE_GROUPED` und `NEARBY` ohne `departure_state_loading`.
- Ein späterer `/departures`-Zyklus ist als normaler Stable-Refresh erkennbar und nicht an einen Sortier-Tap gekoppelt; Same-Origin-ORS-Metriken werden dabei wiederverwendet.
- Nutzer bestätigt die Persistenz der gewählten Sortierung nach `force-stop`/Neustart.

Damit ist Build 155 der Produktstand für den ersten signierten Release-Kandidaten `v1.1.0-b155`.