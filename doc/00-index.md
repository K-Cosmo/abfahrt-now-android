# AbfahrtApp — normative Dokumentation

Stand: **v1.1.0 / Build 149 (`versionCode 1490`)** auf Basis der DOC1.1-Governance.

Build 123–130 schlossen ORS-, Provider-ID-, Secret-Storage- und 16-KB-Themen; Build 131–135 migrierten auf Android 17/API 37 und stabilisierten Toolchain/Tests. Build 136 machte den abfahrt.now-Key verpflichtend. Build 137 trennte Alternativstandort-Abfahrten vom Startscreen, Build 138 führte den `/trips`-RoutePlanner ein. Builds 139–142 konvergierten Darstellung, Zwischenhalte, Fehlerzustände und Routing-UI. Build 143 ergänzte Zuhause/Arbeit, Build 144 machte diese Orte direkt auf der Startseite auswählbar. Build 145 ergänzte lokale Routensortierung. Build 146 konvergierte die Routing-UX mit sauberer Zeit-/Modusausrichtung und Walking-Navigation; die neue harte Photon-Stadteingrenzung erwies sich im Feld für Fernort-/Stationssuchen als zu restriktiv. Build 147 machte den City-Scope zunächst weich, reale Evidence zeigte aber mit `Bad Saarow -> Bad Saarow, Berlin`, dass auch diese Heuristik nicht robust ist. Build 148 entfernt deshalb die Query-Umschreibung aus der allgemeinen Zielsuche vollständig: Photon erhält den getrimmten Nutzereingabetext unverändert und den aktuellen Standort ausschließlich als weichen `lat/lon`-Bias; Photons Reihenfolge wird nicht clientseitig neu sortiert. Build 148 ist nach realem Gradle- und Runtime-Gate abgenommen. Build 149 schließt das per Feld-Screenshot wieder geöffnete HERE-/Map-Restthema: Bei `Hier` wird keine ORS-Route mehr geladen oder gezeichnet; die Detailkarte zeigt stattdessen nur Standort und Haltestelle.

## Harte Governance-Regel

`/doc` ist die **einzige normative Quelle der Wahrheit** für Produktregeln, Architekturregeln, technische Verträge, Invariants, Entscheidungen, offene Findings, Backlog, Release-Plan und Abnahmebedingungen.

Bei Widersprüchen gilt:

1. `/doc` beschreibt den gewollten Sollzustand.
2. `/evidence` beschreibt beobachtete Realität und Abnahmebelege.
3. Der Sourcecode ist die aktuelle Implementierung; weicht er von `/doc` ab, ist das eine Abweichung/Finding und **keine automatische Änderung der Norm**.
4. `/specs` und `.specify` steuern den Entwicklungsprozess, sind aber nicht fachlich normativ.
5. `README.md`, Chatverläufe und historische Dateien sind nicht normativ.

Eine neue fachliche oder technische Regel gilt erst als Projektentscheidung, wenn sie in `/doc` eingearbeitet wurde.

## Dokumente

| Datei | Zweck |
|---|---|
| [`01-product.md`](01-product.md) | Produktziel, UX-Prinzipien, aktueller Funktionsumfang |
| [`02-architecture.md`](02-architecture.md) | aktuelle Architektur und Datenflüsse |
| [`03-api-contracts.md`](03-api-contracts.md) | externe und interne API-Verträge |
| [`04-invariants.md`](04-invariants.md) | Regeln, die Builds nicht verletzen dürfen |
| [`05-regression-ledger.md`](05-regression-ledger.md) | bekannte Regressionen, Bugs und Schutzmaßnahmen |
| [`06-decisions.md`](06-decisions.md) | verbindliche Architektur-/Produktentscheidungen |
| [`07-findings.md`](07-findings.md) | offene, noch nicht entschiedene oder nicht abgenommene Befunde |
| [`08-backlog.md`](08-backlog.md) | priorisierte offene Arbeit |
| [`09-release-plan.md`](09-release-plan.md) | geplante Build-/Gate-Reihenfolge |
| [`10-build-handoff.md`](10-build-handoff.md) | letzter belastbarer Übergabestand |
| [`11-test-and-evidence.md`](11-test-and-evidence.md) | Test-, Evidence- und Abnahmeprozess |
| [`12-android-compatibility.md`](12-android-compatibility.md) | Android-/Toolchain-Kompatibilität |
| [`13-localization.md`](13-localization.md) | Lokalisierungsregeln |
| [`14-community-and-service-policy.md`](14-community-and-service-policy.md) | Community-Identität, EU-first-Prinzip und externe Runtime-Dienste |
| [`AI-CODING-GUARDRAILS.md`](AI-CODING-GUARDRAILS.md) | verbindliche Regeln für KI-gestützte Entwicklung |
| [`CHANGELOG.md`](CHANGELOG.md) | chronologische Build-Historie |

## Prozessverzeichnisse außerhalb von `/doc`

- `.specify/memory/constitution.md`: Prozess-Governance, verweist auf `/doc`; darf keine konkurrierende Produktwahrheit enthalten.
- `/specs/<build-or-feature>/`: Specification, Plan und Tasks für eine konkrete Änderung; nach Abnahme wird die bleibende Wahrheit nach `/doc` konvergiert.
- `/evidence`: reale Build-, Test-, Logcat-, Screenshot- und QA-Belege. Evidence ist beobachtete Realität, aber keine Policy.
- Ein paralleler `/docs`-Baum und ein zweiter Root-`CHANGELOG.md` sind nicht zulässig; historische Referenzen in alten Texten bleiben reine Historie.

## DOC1/DOC1.1 und Runtime Builds 123–138

DOC1 und DOC1.1 waren reine Dokumentations-/Governance-Revisionen auf Runtime Build 122. Build 123 migrierte ausschließlich ORS und ist durch Device-Logcats abgenommen. Build 124 schloss die numerische Provider-Stop-ID-Lücke und ist ebenfalls per Device-Logcat abgenommen. Build 125 änderte ausschließlich die lokale Secret-Speicherung und ist per Device-Evidence abgenommen. Builds 126–130 lieferten die vollständige 16-KB-Evidence-Kette: Dependency-Remediation, MapLibre 13.6.0, Eliminierung des nicht konformen `graphics-path`-Prebuilts, `PAGE_ALIGNMENT_16K` und echter 16-KB-Runtime-Smoke. Build 131 verändert keine Produktlogik; er migrierte die Build-Toolchain auf AGP 9.4 / Gradle 9.6 / built-in Kotlin, setzte `compileSdk = 37` bei `targetSdk = 36` und wurde mit realem Build, API-37-Runtime und erneutem AAB-`PAGE_ALIGNMENT_16K` abgenommen. Build 132 setzte ausschließlich `targetSdk = 37` und ist per realem Build/API-37-Kernflow abgenommen. Build 133 bereinigte isoliert die Kotlin-2.3-Warnings und ist per realem warning-freiem Build abgenommen. Build 134 erweiterte ausschließlich die Contract-Abdeckung und lieferte zwei reale 40/40-`walkSeconds`-Samples; Build 135 stellte den vollständigen lokalen Unit-Test-Gate wieder her. Build 136 führt die neue Pflichtauthentifizierung über den bereits vorhandenen verschlüsselten abfahrt.now-Key ein; der blockierte Erststart ohne Key und der anschließende App-Start sind real bestätigt. Build 137 trennt den Photon-Nebenflow „Abfahrten an anderem Ort“ in eine eigene geschützte Navigationsroute hinter dem Startseiten-Menü; beim Verlassen wird deterministisch auf den aktuellen Standort zurückgesetzt. Build 138 nutzt den dadurch frei gewordenen Startseiten-Suchplatz für „Route planen – Ziel eingeben“ und führt nach Photon-Zielauswahl direkt in einen `/trips`-basierten RoutePlanner.
