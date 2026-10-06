Stand: **v1.1.0 / Build 156 (`versionCode 1560`)** ist seit **06.10.2026** der aktuell akzeptierte Runtime-Stand und als öffentlicher signierter GitHub-APK-Release **`v1.1.0-b156`** veröffentlicht. Der Release basiert auf Source-Commit `6ccec47ce72b3e00cbc302ec65a226a4622b70b6`; das veröffentlichte APK `abfahrtsradar-v1.1.0-b156.apk` hat SHA-256 `53DE476E02271C6545906DEC47D0F3B2E66E9AB2A060EE5542AD1C75E106591B`. GitHub `releases/latest` liefert Build 156; Release ist weder Draft noch Prerelease und enthält APK plus `SHA256SUMS.txt`. Build 156 schließt F-NAME-001/B-156-001: sichtbarer Produktname **Abfahrtsradar**, kanonisches Repository `K-Cosmo/abfahrtsradar-android`, direkter neuer Updatechecker-Pfad; Package-ID, Signing-Lineage, Persistenz und fachliche Runtime-Semantik bleiben unverändert. Der reale Build-155→156-In-place-Updatepfad, Kernfunktionen und der 16-KB-Runtime-Pfad wurden auf dem finalen Release-Artefakt bestätigt. F-ORS-001/B-ORS-001 bleibt als separater technischer Hardening-Block.

Build 123–130 schlossen ORS-, Provider-ID-, Secret-Storage- und 16-KB-Themen; Build 131–135 migrierten auf Android 17/API 37 und stabilisierten Toolchain/Tests. Build 136 machte den abfahrt.now-Key verpflichtend. Build 137 trennte Alternativstandort-Abfahrten vom Startscreen, Build 138 führte den `/trips`-RoutePlanner ein. Builds 139–142 konvergierten Darstellung, Zwischenhalte, Fehlerzustände und Routing-UI. Build 143 ergänzte Zuhause/Arbeit, Build 144 machte diese Orte direkt auf der Startseite auswählbar. Build 145 ergänzte lokale Routensortierung. Build 146 konvergierte die Routing-UX; Build 147 zeigte die Grenzen der City-Heuristik. Build 148 ersetzte sie durch rohe Photon-Queries plus weichen Standort-Bias und ist real abgenommen. Build 149 schloss den HERE-/Map-Restpunkt. REPO1 machte GitHub zum kanonischen Workspace und schloss den reproduzierbaren Gradle-Wrapper. Build 150 ergänzte den technisch abgenommenen anonymen GitHub-Release-Update-Check. Build 151 konvergierte die sichtbare Runtime-Identität mit dem unabhängigen Community-Projekt. Build 152 konvergierte Onboarding-Lesbarkeit, Startup-Access-Gate, kompakten RoutePlanner-Kopf und Settings-Footer. Build 153 ergänzte die leichte `AbfahrtStartup`-Diagnostik und lokalisierte den dominanten Kaltstart-Warteblock. Build 154 beseitigte diesen Warteblock mit einem provisorischen Current-Location-First-Paint-Origin, ohne die progressive Departure-/Add-on-/ORS-Pipeline neu zu entwerfen. Build 155 ergänzte persistente wählbare Sortierprofile für die Abfahrtsseite. Build 156 migrierte die sichtbare Community-Identität auf **Abfahrtsradar** und das kanonische Repository, ohne technische Android-Identität oder fachliche Runtime-Semantik zu ändern.

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