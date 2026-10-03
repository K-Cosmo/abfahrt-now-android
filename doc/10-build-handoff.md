# Build-Handoff

## Runtime-Baseline: v1.1.0 Build 149 (`versionCode 1490`)

Build 148 ist real für D-069 abgenommen: allgemeine Photon-Zielsuche nutzt Raw Query + weichen `lat/lon`-Bias und übernimmt Photons Ranking ohne clientseitige City-/PLZ-/Transit-Heuristik.

Build 149 schließt das HERE-/Map-Restthema: Bei `Departure.isHereOverride()` wird keine ORS-Routenvorschau geladen und keine Walking-/Bike-Polyline gezeichnet. Stattdessen zeigt das Detailsheet bei verfügbaren Koordinaten eine Standortkarte mit Query-Origin und Haltestellenmarker. Der Nutzer-Screenshot bestätigt `Hier`, `Haltestelle erreicht`, Überschrift `Standort` und keine blaue ORS-Route. Der kombinierte Build-149-Gradle-Gate ist als vollständige Build-Abnahme noch nachzureichen.

## Repository-Baseline nach Build 149

Diese Housekeeping-Runde ändert **keine** Runtime-Buildnummer und keine App-Produktlogik.

Konvergiert wurden:

- `/doc` ist die einzige normative Produkt-/Technikdokumentation; der Legacy-Baum `/docs` und der doppelte Root-`CHANGELOG.md` wurden entfernt.
- `README.md` positioniert das Repository als unabhängiges Community-Projekt und grenzt es klar von der offiziellen abfahrt.now-App bzw. dem API-Entwickler/-Betreiber ab.
- [`14-community-and-service-policy.md`](14-community-and-service-policy.md) definiert EU-first, Datenminimierung, Service-Inventar und Credential-Isolation.
- 22 gebündelte UI-Locale-Sets sind dokumentiert; die normale UI-Lokalisierung benötigt keinen externen Übersetzungsdienst.
- `AGENTS.md` verweist Coding-Agenten auf `/doc` und den bestehenden Spec-Kit-Prozess statt eine zweite Policy-Schicht zu erzeugen.
- `gradlew`, `gradlew.bat`, gehärtete Gradle-9.6.0-Wrapper-Properties, Windows-Bootstrap und Android-CI sind ergänzt.
- `gradle-wrapper.jar` ist noch nicht im Repository; CI und Bootstrap laden ausschließlich das offizielle Gradle-9.6.0-Wrapper-JAR und prüfen den fest hinterlegten SHA-256. `F-DOC1-008` bleibt deshalb mitigiert/offen.
- `F-DOC1-015` dokumentiert eine neue notwendige Runtime-Konvergenz: vorhandene About-/Legal-Texte dürfen vor einem öffentlichen Community-Release keine offizielle Zugehörigkeit suggerieren.

## Nächster Produktbuild: Build 150 — GitHub Update Checker

Verbindlicher Scope:

- `versionCode = 1500`, `versionName = 1.1.0`.
- `K-Cosmo/abfahrt-now-android` GitHub Releases als Metadatenquelle.
- Tag-Schema `v<versionName>-b<build>`, zunächst `v1.1.0-b150`.
- Vergleich über monotone Buildnummer.
- ein asynchroner Check pro App-Start; Fehler/Offline blockieren die App nicht.
- unaufdringlicher Update-Hinweis mit Aktualisieren/Später; Aktualisieren öffnet die konkrete Release-Seite.
- kein automatischer APK-Download, keine stille Installation, kein GitHub-Token.
- **separater anonymer GitHub-HTTP-Client**. Der vorhandene `abfahrtClient` mit `ApiKeyInterceptor` darf nicht verwendet werden.
- keine abfahrt.now-/ORS-Credentials, Standortkoordinaten oder Photon-Suchtexte an GitHub.
- neue Strings in allen 22 Locale-Sets.
- Unit-Tests für Tag-Parsing und Buildvergleich.

### Build-150-Gate

```text
:app:testDebugUnitTest :app:assembleDebug
```

Zusätzlich:

1. statische Governance-/Security-/Locale-Gates grün;
2. ältere Installation gegen neueres GitHub Release → Hinweis erscheint und öffnet richtige Release-Seite;
3. aktuelle Installation gegen dasselbe Release → kein falscher Hinweis;
4. Offline/GitHub-Fehler → Kern-App bleibt normal nutzbar;
5. Network-Evidence zeigt keinerlei Projekt-Credentials im GitHub-Request.

## Danach

- **Build 151:** `F-DOC1-015`, Runtime-About-/Legal-/Community-Identität sauber konvergieren, bevor ein erstes öffentliches Community-Release als solches beworben wird.
- **Build 152:** `AB-018`, Startup-/Main-Thread-Instrumentierung; erst messen, dann optimieren.

Die vollständige Reihenfolge steht in [`09-release-plan.md`](09-release-plan.md).

## Lokaler Windows-Arbeitsstand

Android Studio soll den **Repository-Root** öffnen, z. B.:

`D:\Android\abfahrt-now-android`

Nicht `D:\Android\abfahrt-now-android\app` öffnen. `local.properties` bleibt lokal/ignoriert.

Nach dem Pull dieser Repository-Baseline und solange `gradle-wrapper.jar` noch fehlt:

```powershell
powershell -ExecutionPolicy Bypass -File .\tools\bootstrap-gradle-wrapper.ps1
.\gradlew.bat --version
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug
```

Nach erfolgreichem Bootstrap soll das verifizierte `gradle/wrapper/gradle-wrapper.jar` eingecheckt werden; erst dann ist `F-DOC1-008` vollständig geschlossen.
