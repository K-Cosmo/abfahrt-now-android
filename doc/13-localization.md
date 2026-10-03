# Lokalisierung

## Unterstützte UI-Sprachressourcen

Build 122 enthält den deutschen Basissatz plus:

`en, nl, da, nb, sv, fi, it, es, pt, fr, pl, cs, hu, ro, sk, hr, sl, et, lv, lt, tlh`

Zusätzlich gibt es die App-Einstellung `SYSTEM`, die der Systemsprache folgt.

## Source of truth

Für Android-String-Keys ist technisch `app/src/main/res/values/strings.xml` der Basissatz. Alle `values-*/strings.xml` müssen dieselben Keys und kompatible printf-Platzhalter enthalten.

Das ist eine **technische Ressourcen-Invariant**, keine konkurrierende Dokumentationswahrheit: fachliche Textbedeutungen und neue UX-Regeln werden weiterhin in `/doc` entschieden.

## Gate

```bash
python3 scripts/check_locale_keys.py
```

Neue Strings gelten erst als fertig, wenn alle unterstützten Resource-Sets ergänzt sind oder bewusst eine klar dokumentierte Fallback-Strategie beschlossen wurde.


## Build 138 Routing-Strings

Der RoutePlanner führt neue Texte für Startseiten-Zielsuche, Von/Nach, Tausch, Route finden, Trip-Zusammenfassung, Durchbindung und Fehlerzustände ein. Diese Keys werden im selben Build in **allen 22** Resource-Sets gepflegt. Ein Build-138-Handoff ist ohne grünes `scripts/check_locale_keys.py` unvollständig; neue englische Fallback-Texte in nichtenglischen Paketen sind nur bei explizit dokumentierter Ausnahme zulässig.


## Build 140 Route-Details

Die neuen Route-Detail-Keys `route_transfer_minutes`, `route_show_intermediate_stops` und `route_hide_intermediate_stops` sind in **allen 22** Resource-Sets vorhanden. `route_transfer_minutes` muss in jedem Paket den `%1$d`-Platzhalter beibehalten. Expand/Collapse-Texte dienen zugleich der Accessibility und dürfen daher nicht nur im deutschen Basissatz existieren.


Build 146 ergänzt `route_open_walking_navigation` in allen 22 vorhandenen Resource-Sets. Locale-Key-Parität und XML-Parse bleiben Pflichtgate.
