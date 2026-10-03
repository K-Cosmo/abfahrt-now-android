# DOC1 Specification

## Problem
Das Repository enthält historisch wertvolle Dokumentation, aber mehrere mögliche Wahrheitsflächen (`README`, Root-CHANGELOG, `/docs`, Chat, Code). Das widerspricht der neuen Governance.

## Ziel
- `/doc` wird einzige normative Quelle.
- Historie/Evidence geht nicht verloren.
- Spec Kit bleibt Prozess-Governance.
- Runtime Build 122 bleibt unverändert.
- Code-/Plan-Widersprüche werden als Findings sichtbar.

## Nicht-Ziele
- kein Runtime-Codefix;
- keine Dependency-/SDK-/API-Änderung;
- kein Versionsbump;
- keine „Bereinigung“ offener Bugs durch Dokumentation.

## Acceptance Criteria
1. `/doc/00-index.md` definiert die Wahrheitsordnung.
2. Produkt, Architektur, API, Invariants, Regressionen, Decisions, Findings, Backlog, Release Plan, Handoff und Test/Evidence sind normativ abgedeckt.
3. `/docs` enthält keine konkurrierende Sachwahrheit mehr.
4. bisherige QA-/Doku-Historie bleibt unter `/evidence` erhalten.
5. Runtime-Dateien sind byte-identisch zum Eingangspaket.
6. statische Checks sind grün.
