# BUILD124 Specification — Numeric Provider Stop-ID Resolution

## Problem
Build 123 zeigt im realen U6-Fall `900009173` als sichtbaren Haltestellenwert. Derselbe Response-Kontext kennt `Station.id=900009173` und den lesbaren Namen `U Seestr./Turiner Str.`.

## Ziel
Technische Stop-IDs unabhängig von ihrem Stringformat erkennen, wenn `Departure.stop` exakt einer bekannten `Station.id` entspricht.

## Muss
- exakter Station-ID-Match löst auf `Station.name` auf;
- Roh-ID bleibt als `providerStopId` erhalten;
- Post-Merge-Pass deckt denselben Fall ab;
- keine pauschale Numeric-ID-Heuristik;
- keine Änderung an Dedup, ORS, Sortierung, Reachability oder UI.

## Abnahme
Realer U6-Fall zeigt keinen technischen Wert `900009173` mehr; Follow-up und RoutePreview bleiben korrekt.
