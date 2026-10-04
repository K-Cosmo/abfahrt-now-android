# Build 154 Plan

## Ausgangspunkt

Build 153 hat den wiederholbaren Startup-Warteblock ausreichend lokalisiert. Die nachgelagerte Departure-Pipeline ist bewusst progressiv und wird nicht optimiert oder umgebaut:

- First Paint vor Vollständigkeit;
- API-Dedup-Booster nur bei leerem Kaltstart;
- Direct-stop/Add-on-Nachladen danach;
- app-eigene Filter/Dedup/Sortierung;
- ORS asynchron;
- Same-Origin-Refresh als Stable-Merge;
- relevanter Standortwechsel als Hard Reset ohne Loading-Blackout.

Build 154 arbeitet ausschließlich vor dem ersten Core-Request im Current-Location-Kaltstart.

## Phase A — vorhandene Regeln festhalten

Vor Codeänderung gelten als unverändert:

1. `MOVEMENT_THRESHOLD_M = 200f` ist die einzige Re-Anchor-Schwelle.
2. `< 200 m` = gleicher Standortkontext; kein zweiter Core allein wegen des späteren Fixes.
3. `>= 200 m` = bestehender Hard-Reset-Pfad; bei laufendem Load über `pendingLocationRefresh`/`pendingHardResetRefresh` genau einmal nachziehen.
4. D-019/D-025: deduplizierter API-First-Paint nur auf leerem Kaltstartscreen.
5. D-026/I-036: ORS-Metriken bei gleichem Walking-Origin weiterverwenden.
6. D-027/I-031: Same-Origin-Refresh bleibt Stable-Merge.
7. D-028/I-032/I-033: Standortwechsel hält alte sichtbare Liste bis Ersatz bereit, aber ohne Cross-Origin-Stable-Merge.
8. I-034: ORS darf Core nicht blockieren.

Keine neue Zeit-/Accuracy-Schwelle für `lastLocation` erfinden. Der heutige Code akzeptiert dieselbe Quelle bereits als Fallback ohne solche Schwellen.

## Phase B — minimaler Location-Fast-Path

Im bestehenden `DepartureViewModel`:

1. Nur für Current Location und einen leeren Kaltstart die System-`lastLocation` als mögliche provisorische Position abfragen.
2. High-Accuracy-`getCurrentLocation()` parallel weiter anfordern.
3. Ist eine gültige `lastLocation` vorhanden, mit ihr den bestehenden Core-Pfad starten, ohne auf High Accuracy zu warten.
4. Gibt es keine `lastLocation`, exakt auf den bisherigen High-Accuracy-Pfad zurückfallen.
5. Keine neue Persistenz, kein DataStore-Standortcache, keine neue Dependency.

Die bestehende `fetchDepartures()`-/`loadForCurrentTarget()`-/`loadWithCoordinates()`-Pipeline soll wiederverwendet werden. Keine zweite Ladepipeline.

## Phase C — High-Accuracy-Korrektur in vorhandene Movement-Semantik einspeisen

Der spätere High-Accuracy-Fix wird nur verarbeitet, wenn der Request-/Target-Kontext noch aktuell ist.

- Distanz zum provisorischen Origin `< 200 m`: keine weitere Core-Anfrage.
- Distanz `>= 200 m`: denselben Hard-Reset-Mechanismus benutzen, den bereits die laufenden Location-Updates verwenden.
- Läuft der Core noch: `pendingLocationRefresh`/`pendingHardResetRefresh`; kein paralleler zweiter Load.
- Ist der Core fertig: genau ein erzwungener Hard-Reset-Load.
- Wechsel zu `SearchTarget.Station`/Alternate-Location während der ausstehenden Korrektur: Ergebnis verwerfen.

Falls für die Wiederverwendung der Movement-Entscheidung ein kleiner interner Helper nötig ist, darf die bestehende 200-m-Regel dorthin zentralisiert werden. Keine neue Location-Architektur.

## Phase D — ORS-/UI-Ruhe schützen

1. Keine Änderung an Add-on-, Dedup-, Layer-, Filter- oder Sortierlogik.
2. Wenn ein Re-Anchor `>= 200 m` bereits feststeht, bevor ORS für den provisorischen Core-State startet, keinen neuen ORS-Zyklus für den verworfenen Origin beginnen.
3. Bereits laufende ORS-Arbeit beim echten Origin-Wechsel über vorhandene Cancellation-/Stale-Mechanismen entwerten; keine zweite Guard-Welt.
4. Bei `< 200 m` keine künstliche ORS-Neuberechnung.
5. Der Nutzer sieht weiterhin den frühen deduplizierten Booster und danach die schrittweise vorhandene Präzisierung — Build 154 darf diese Anzeige nicht unruhiger machen.

## Phase E — Diagnostik

Build-153-`AbfahrtStartup` bleibt aktiv. Ergänzt werden nur wenige, nicht-sensitive `AbfahrtLocation`-/`AbfahrtStartup`-Marker, soweit nötig:

- provisional location available / unavailable;
- provisional Core started;
- high-accuracy correction received;
- correction classified same-origin / re-anchor;
- deferred hard reset scheduled/applied.

Keine Koordinaten, API-Keys oder gespeicherte Orte loggen. Distanz darf als gerundeter Meterwert geloggt werden, weil sie bereits Teil der technischen Movement-Diagnostik ist.

## Phase F — automatisierte Verifikation

- vorhandene Static/Governance/Compatibility-Skripte;
- Wrapper-Verifikation;
- Unit Tests;
- Debug;
- Release/R8.

Fokussierte Policy-Tests sollen die 200-m-Grenze und die Anzahl zulässiger Reloads absichern. Falls dafür ein Pure-Kotlin-Helper extrahiert wird, muss er die bestehende Konstante nutzen bzw. deren einzige Quelle werden.

## Phase G — Realgeräte-Evidence

Vergleich gegen Build 153:

- mindestens drei saubere Cold Starts mit vorhandener `lastLocation`;
- Zeitpunkt `Loading`, provisional location, erster Core-Request, High-Accuracy-Korrektur, erster/progressiver/finaler Success und ORS;
- mindestens ein Same-Origin-Fall `< 200 m` mit genau einem Core-Zyklus;
- Re-Anchor-Fall `>= 200 m` soweit praktisch reproduzierbar;
- kein zusätzlicher API-/ORS-Sturm;
- Home→App-Resume regressionsfrei;
- keine App-FATAL-/ANR-/Navigation-/Permission-/AccessGate-Regression.

## Entscheidungsregel

Build 154 wird nur akzeptiert, wenn der erste Core-Request real früher startet **und** die bestehende ruhige progressive Anzeige erhalten bleibt.

Wenn der Gewinn gering ist oder der provisorische Origin in der Praxis sichtbare falsche Standortzustände/zusätzliche Sprünge erzeugt, wird die Änderung verworfen. Die bestehende 2,6–3,0-s-High-Accuracy-Wartezeit ist dann der Preis für die bisherige Genauigkeitssemantik und kein Anlass für weitere Architekturkomplexität.
