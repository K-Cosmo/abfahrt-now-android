# AbfahrtApp — normative Dokumentation

Stand: **v1.1.0 / Build 152 (`versionCode 1520`)** ist der aktuell akzeptierte Runtime-Stand. Build 152 wurde am 04.10.2026 nach automatisierter CI-Evidence und Realgeräte-Smoke abgenommen. **Build 153** ist als nächster Schritt für AB-018 Startup/Main-Thread-Instrumentierung vorgesehen; dort gilt weiterhin: erst messen, dann gezielt optimieren.

Build 123–130 schlossen ORS-, Provider-ID-, Secret-Storage- und 16-KB-Themen; Build 131–135 migrierten auf Android 17/API 37 und stabilisierten Toolchain/Tests. Build 136 machte den abfahrt.now-Key verpflichtend. Build 137 trennte Alternativstandort-Abfahrten vom Startscreen, Build 138 führte den `/trips`-RoutePlanner ein. Builds 139–142 konvergierten Darstellung, Zwischenhalte, Fehlerzustände und Routing-UI. Build 143 ergänzte Zuhause/Arbeit, Build 144 machte diese Orte direkt auf der Startseite auswählbar. Build 145 ergänzte lokale Routensortierung. Build 146 konvergierte die Routing-UX; Build 147 zeigte die Grenzen der City-Heuristik. Build 148 ersetzte sie durch rohe Photon-Queries plus weichen Standort-Bias und ist real abgenommen. Build 149 schloss den HERE-/Map-Restpunkt. REPO1 machte GitHub zum kanonischen Workspace und schloss den reproduzierbaren Gradle-Wrapper. Build 150 ergänzte den technisch abgenommenen anonymen GitHub-Release-Update-Check. Build 151 konvergierte die sichtbare Runtime-Identität mit dem unabhängigen Community-Projekt. Build 152 konvergiert Onboarding-Lesbarkeit, Startup-Access-Gate, kompakten RoutePlanner-Kopf und Settings-Footer. Der erste Realgeräte-Smoke bestätigte den ursprünglichen API-Key-/Onboarding-Flicker als behoben und identifizierte separat einen kurzen Location-Prompt-Flash trotz erteilter Permission. Der kausale Zwei-Zeilen-Fix wurde im Follow-up erneut real getestet; der Flicker ist nicht mehr sichtbar. Der vereinfachte Footer ist visuell akzeptiert, der RoutePlanner zeigt keine beobachtete Regression, Photon-Zielsuche und `/trips` liefen real erfolgreich, und im finalen Logcat findet sich keine App-FATAL-/ANR-Signatur. Android CI #72 ist auf dem finalen Follow-up-/Dokumentations-Head vollständig grün.

## Harte Governance-Regel

`/doc` ist die **einzige normative Quelle der Wahrheit** für Produktregeln, Architekturregeln, technische Verträge, Invariants, Entscheidungen, offene Findings, Backlog, Release-Plan und Abnahmebedingungen.

Bei Widersprüchen gilt:

1. `/doc` beschreibt den gewollten Sollzustand.
2. `/evidence` beschreibt beobachtete Realität und Abnahmebelege.
3. Der Sourcecode ist die aktuelle Implementierung; weicht er von `/doc` ab, ist das eine Abweichung/Finding und **keine automatische Änderung der Norm**.
4. `/specs` und `.specify` steuern den Entwicklungsprozess, sind aber nicht fachlich normativ.
5. `README.md`, Chatverläufe und historische Dateien sind nicht normativ.

Eine neue fachliche oder technische Regel gilt erst als Projektentscheidung, wenn sie in `/doc` eingearbeitet wurde. Ein paralleles `/docs`-Verzeichnis wird nicht geführt; damit existiert bewusst nur eine Dokumentationswurzel.

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
| [`14-community-and-service-policy.md`](14-community-and-service-policy.md) | Community-Abgrenzung, EU-first und externe Runtime-Dienste |
| [`AI-CODING-GUARDRAILS.md`](AI-CODING-GUARDRAILS.md) | verbindliche Regeln für KI-gestützte Entwicklung |
| [`CHANGELOG.md`](CHANGELOG.md) | chronologische Build-Historie |

## Prozessverzeichnisse außerhalb von `/doc`

- `.specify/memory/constitution.md`: Prozess-Governance, verweist auf `/doc`; darf keine konkurrierende Produktwahrheit enthalten.
- `/specs/<build-or-feature>/`: Specification, Plan und Tasks für eine konkrete Änderung; nach Abnahme wird die bleibende Wahrheit nach `/doc` konvergiert.
- `/evidence`: reale Build-, Test-, Logcat-, Screenshot- und QA-Belege. Evidence ist beobachtete Realität, aber keine Policy.

## Entwicklungsworkflow

Der aus Spec Kit übernommene Prozess bleibt bewusst von der Produktwahrheit getrennt:

**Specification → Plan → Tasks → Implementierung → Analyse/Converge → echte Tests/Evidence → Abnahme**

Specs dürfen die Umsetzung strukturieren, aber keine zweite Policy-/Architekturwelt neben `/doc` erzeugen.
