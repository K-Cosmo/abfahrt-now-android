# AbfahrtApp Development Constitution

Diese Datei regelt **wie** entwickelt wird. Sie ist keine fachliche Quelle der Wahrheit.

## 1. Normative Grenze

- `/doc` ist die einzige normative Produkt-/Technikquelle.
- Diese Constitution darf keine konkurrierenden Produktregeln, API-Verträge oder Invariants definieren.
- Bei Widerspruch gewinnt `/doc`.

## 2. Arbeitsweise

- kleine isolierte Änderungen;
- bestehenden Code erweitern statt Schattenarchitektur bauen;
- keine neue Dependency ohne echten Mehrwert;
- Realität/Logs/Tests vor Vermutung;
- KIS und Nutzerverständlichkeit vor technischer Eleganz;
- keine stillen Architekturwechsel;
- ein Ziel pro Build/Schleife, soweit praktikabel.

## 3. Workflow

Standard:

`Specification -> Plan -> Tasks -> Implementierung -> Analyse/Converge -> Tests/Evidence -> Abnahme`

Für größere Änderungen zusätzlich Clarify/Checklist/Analyze. Kleine Fixes dürfen verkürzt werden, aber nicht ohne reale Verifikation abgeschlossen werden.

## 4. Statusdisziplin

KI- oder Entwickleraussage ist kein Qualitätsnachweis. „Fixed“ und „closed“ werden entsprechend `/doc/11-test-and-evidence.md` verwendet.

## 5. Konvergenz

Nach Abnahme:
- dauerhafte Regeln/Entscheidungen nach `/doc`;
- reale Belege nach `/evidence`;
- Specs bleiben Prozesshistorie, nicht zweite Policy-Welt.
