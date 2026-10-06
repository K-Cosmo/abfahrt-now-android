# AbfahrtApp — normative Dokumentation

Stand: **v1.1.0 / Build 155 (`versionCode 1550`)** ist der aktuell akzeptierte Runtime-Stand und seit **06.10.2026** als erster öffentlicher signierter GitHub-APK-Release **`v1.1.0-b155`** veröffentlicht. Der Release basiert auf Commit `85df24b280f60e47d813d17aa93f400b22fca787`; das veröffentlichte APK `abfahrt-now-v1.1.0-b155.apk` hat SHA-256 `0FE1A8D7EB8A6038FF4446DD4F696BA37737875982945A60DF860ABE255BD9A5`. RELEASE1 rotierte kontrolliert vom historischen Signer auf den neutralen Community-Signer über eine Android Signing-Certificate-Lineage; der historische Key bleibt als Teil der Signer-Historie sicher erhalten. Der Old→New-In-place-Updatepfad wurde auf einem realen Altgerät bewiesen, der 16-KB-Runtime-Pfad auf einem offiziellen 16-KB-x86_64-Emulator. **B-REL-001/F-REL-001 ist damit released/closed.** Das Projekt steht unter MIT License und dokumentiert die KI-gestützte Entwicklung transparent. F-NAME-001/B-156-001 ist die nächste sichtbare Produktentscheidung; F-ORS-001/B-ORS-001 bleibt als separater technischer Hardening-Block.

Build 123–130 schlossen ORS-, Provider-ID-, Secret-Storage- und 16-KB-Themen; Build 131–135 migrierten auf Android 17/API 37 und stabilisierten Toolchain/Tests. Build 136 machte den abfahrt.now-Key verpflichtend. Build 137 trennte Alternativstandort-Abfahrten vom Startscreen, Build 138 führte den `/trips`-RoutePlanner ein. Builds 139–142 konvergierten Darstellung, Zwischenhalte, Fehlerzustände und Routing-UI. Build 143 ergänzte Zuhause/Arbeit, Build 144 machte diese Orte direkt auf der Startseite auswählbar. Build 145 ergänzte lokale Routensortierung. Build 146 konvergierte die Routing-UX; Build 147 zeigte die Grenzen der City-Heuristik. Build 148 ersetzte sie durch rohe Photon-Queries plus weichen Standort-Bias und ist real abgenommen. Build 149 schloss den HERE-/Map-Restpunkt. REPO1 machte GitHub zum kanonischen Workspace und schloss den reproduzierbaren Gradle-Wrapper. Build 150 ergänzte den technisch abgenommenen anonymen GitHub-Release-Update-Check. Build 151 konvergierte die sichtbare Runtime-Identität mit dem unabhängigen Community-Projekt. Build 152 konvergierte Onboarding-Lesbarkeit, Startup-Access-Gate, kompakten RoutePlanner-Kopf und Settings-Footer. Build 153 ergänzte die leichte `AbfahrtStartup`-Diagnostik und lokalisierte den dominanten Kaltstart-Warteblock. Build 154 beseitigte diesen Warteblock mit einem provisorischen Current-Location-First-Paint-Origin, ohne die progressive Departure-/Add-on-/ORS-Pipeline neu zu entwerfen. Build 155 ergänzte persistente wählbare Sortierprofile für die Abfahrtsseite.

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
| [`15-release-signing.md`](15-release-signing.md) | Signing-Key-Rotation, Release-Gates und APK-Veröffentlichung |
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