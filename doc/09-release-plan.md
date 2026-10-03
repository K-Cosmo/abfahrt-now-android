# Release-Plan

Stand: **v1.1.0 / Build 149 (`versionCode 1490`)**. Historische Build-Details bleiben in [`CHANGELOG.md`](CHANGELOG.md), [`05-regression-ledger.md`](05-regression-ledger.md) und der Git-Historie; dieses Dokument beschreibt bewusst nur die aktuelle Release-Reihenfolge und die noch relevanten Gates.

## Aktuelle Baseline

- Build 148: Raw Photon Query + Location Bias — **accepted** durch realen Gradle-Gate und Runtime-/Ranking-Evidence.
- Build 149: HERE-Detailkarte — Implementierung vorhanden; der Feld-Screenshot bestätigt `Hier` / `Haltestelle erreicht` / `Standort` ohne blaue ORS-Polyline. Der kombinierte Build-149-Gradle-Gate bleibt als vollständige Build-Abnahme noch nachzureichen.
- Repository-Housekeeping nach Build 149: **keine neue App-Buildnummer**. `/doc` wird als einzige normative Dokumentationsfläche technisch erzwungen; Community-/EU-first-Policy, Gradle-Wrapper-Launcher/Bootstrap und GitHub Actions werden als Repository-Baseline ergänzt.

## Reihenfolge ab jetzt

### Build 150 — GitHub Update Checker

Ziel: Eine über GitHub Releases verteilte Installation soll unaufdringlich erkennen, dass eine neuere Community-App-Version verfügbar ist.

Scope:
- `versionCode = 1500`, `versionName = 1.1.0`.
- Quelle: `K-Cosmo/abfahrt-now-android` GitHub Releases, `latest`.
- verbindliches Release-Tag-Schema: `v<versionName>-b<build>`, zunächst `v1.1.0-b150`.
- Vergleich primär über die monotone Buildnummer; `versionName` dient Anzeige/Tag-Struktur, nicht allein der Update-Entscheidung.
- genau ein asynchroner Check pro App-Start; Fehler/Offline/Rate-Limit blockieren die App nicht und erzeugen keinen aggressiven Fehlerdialog.
- bei neuer Version ein kleiner Update-Hinweis mit `Aktualisieren` und `Später/Schließen`.
- `Aktualisieren` öffnet die konkrete GitHub-Release-Seite; kein stiller APK-Download, keine stille Installation.
- kein GitHub-Token in der App.
- GitHub erhält ausschließlich eine anonyme Release-Metadatenanfrage. Keine abfahrt.now-/ORS-Keys, keine Standortkoordinaten und keine Photon-Suchtexte.
- eigener anonymer OkHttp/Retrofit-Pfad; der `abfahrtClient` mit `ApiKeyInterceptor` darf nicht wiederverwendet werden.
- neue UI-Texte in allen 22 gebündelten Locale-Sets.
- Unit-Tests für Release-Tag-Parsing und Buildvergleich.

Gate:
1. `:app:testDebugUnitTest :app:assembleDebug` grün.
2. statische Governance/Security/Locale-Gates grün.
3. Build 149 gegen ein veröffentlichtes neueres Test-/Release-Tag starten: Update-Hinweis erscheint und öffnet die erwartete Release-Seite.
4. Build 150 gegen denselben Release starten: kein falscher Update-Hinweis.
5. Offline/GitHub-Fehler: Kern-App bleibt vollständig benutzbar.
6. Network-Evidence zeigt keine Projekt-Credentials im GitHub-Request.

### Build 151 — Community-Identität in der Runtime konvergieren

Finding: `F-DOC1-015`.

Ziel: Die In-App-About-/Legal-Darstellung darf vor dem ersten öffentlichen Community-Release nicht den Eindruck einer offiziellen abfahrt.now-App oder einer Zugehörigkeit zum API-Entwickler erzeugen.

Scope wird vor Implementierung separat spezifiziert. Zu prüfen sind insbesondere bestehende `Built by Riles Tech UG`-/Legal-Verweise sowie Attributionen. Provider-/Datenquellen-Hinweise bleiben sachlich erhalten; Community-Projekt und externe API-Anbieter werden klar getrennt.

Gate: kombinierter Gradle-Gate, alle Locale-Keys, visueller Settings/About-Smoke und Review der externen Links/Attributionen.

### Build 152 — AB-018 Startup-/Main-Thread-Instrumentierung

Ziel: reproduzierbare skipped-frame-/Startup-Hinweise messen, bevor optimiert wird.

- zunächst Instrumentierung und Zeitmarken, keine spekulative Optimierung;
- Hauptinitialisierungsschritte identifizieren;
- erst auf Basis realer Device-/Emulator-Evidence einen kleinen Optimierungsbuild planen.

Gate: reproduzierbare, vergleichbare Startup-Messung mit eindeutigem Hotspot oder belastbarer Aussage, dass kein app-seitiger Main-Thread-Hotspot nachweisbar ist.

## Weiterer Backlog nach diesen Builds

- produktive Bewertung von `Station.walkSeconds`;
- möglicher `/journey`-UX-Pfad;
- `routingBounds`/Coverage nur evidence-getrieben und getrennt von Suchsemantik;
- datengetriebene Linienfarben;
- finale Release/R8-/Resizable-/Gerätematrix;
- Zeitwahl/Arrive-by im RoutePlanner bleibt backend-blockiert, solange `/trips` keine entsprechende Contract-Unterstützung bietet.

## Release-weite Gates

Für eine öffentliche APK/AAB-Veröffentlichung gelten zusätzlich zu den Build-spezifischen Gates:

1. `:app:testDebugUnitTest`, `:app:assembleDebug` und der definierte Release/R8-Build sind grün.
2. Android-17/API-37-Kompatibilität bleibt grün.
3. 16-KB-Invariante bleibt erhalten; bei Release-Artefakten AAB/APK erneut prüfen.
4. keine Klartext-Secrets, keine fremden Credentials in externen Requests, Release-Network-Logging aus.
5. `/doc` ist konvergiert; offene P1-Findings blockieren die Veröffentlichung.
6. Locale-Parität für alle 22 mitgelieferten UI-Sprachen.
7. öffentliche Evidence ist sanitisiert; keine präzisen Standorte, Device-Identifier, lokalen Pfade oder Secrets.
8. Community-/Provider-Abgrenzung und Attributionen sind konsistent mit [`14-community-and-service-policy.md`](14-community-and-service-policy.md).

## Repository-/CI-Gate

Der öffentliche Repository-Stand soll reproduzierbar sein. Solange `gradle/wrapper/gradle-wrapper.jar` noch nicht eingecheckt ist, gilt `F-DOC1-008` als mitigiert/offen: CI und der Windows-Bootstrap dürfen ausschließlich den offiziellen Gradle-9.6.0-Wrapper laden und müssen dessen fest hinterlegten SHA-256 vor Nutzung prüfen. Das Finding wird erst geschlossen, wenn das verifizierte Wrapper-JAR im Repository liegt und ein frischer Checkout ohne lokale Gradle-Installation über `gradlew`/`gradlew.bat` starten kann.
