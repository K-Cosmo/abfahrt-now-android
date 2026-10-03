# Lokalisierung

## Unterstützte UI-Sprachressourcen

Build 151 enthält den deutschen Basissatz plus:

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

## Build 146 Walking-Navigation

Build 146 ergänzt `route_open_walking_navigation` in allen 22 vorhandenen Resource-Sets. Locale-Key-Parität und XML-Parse bleiben Pflichtgate.

## Build 150 Update-Hinweis

Die Nutzertexte des GitHub-Release-Update-Hinweises liegen in allen 22 gebündelten Resource-Sets vor. Der Update-Check selbst benötigt keinen Runtime-Übersetzungsdienst; alle Texte bleiben paketiert und werden über Android-Ressourcen aufgelöst.

## Build 151 Community-Identität

Die sechs Community-/Provider-Texte des neuen `CommunityFooter` liegen in allen 22 gebündelten Resource-Sets vor. Damit wird die unabhängige/unoffizielle App-Identität nicht nur im deutschen Basissatz, sondern in jeder ausgelieferten UI-Sprache sichtbar.

## Build 152 — in Arbeit

Teil A ändert bislang nur Layout/Typografie bestehender Onboarding-Texte und fügt keine neuen Locale-Keys hinzu. Für Teil B gilt unverändert: neue oder geänderte Nutzertexte müssen im selben Build in allen 22 Resource-Sets gepflegt werden; `scripts/check_locale_keys.py` bleibt Acceptance-Gate.
