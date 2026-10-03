# Community- und Service-Policy

## Projektidentität

Abfahrt! in diesem Repository ist eine **unabhängig entwickelte Community-Android-App**.

Sie ist **nicht** die offizielle Android-App von abfahrt.now, nicht mit dem Betreiber/Entwickler der abfahrt.now-API verbunden und entwickelt oder betreibt die abfahrt.now-API nicht. Die App ist ein eigenständiger Client, der externe Dienste über deren bereitgestellte Schnittstellen nutzt.

Diese Abgrenzung muss in öffentlichen Projektflächen klar erkennbar bleiben. Marken-/Anbieterhinweise dürfen nicht den Eindruck erwecken, das Community-Projekt vertrete einen externen API-Anbieter.

## EU-first

Bei neuen externen Runtime-Diensten gilt **EU-first** als Architektur- und Beschaffungsprinzip:

1. EU-basierte Betreiber, offene Standards und offene Daten/Open-Source-Lösungen werden bevorzugt, wenn sie den Produktbedarf gleichwertig erfüllen.
2. Es gilt Datenminimierung: Nur die Daten werden an einen Dienst übertragen, die für seinen konkreten Zweck erforderlich sind.
3. Nicht-EU- oder Plattformabhängigkeiten sind zulässig, wenn sie technisch oder produktseitig sinnvoll/notwendig sind, müssen aber als bewusste Ausnahme dokumentiert werden.
4. Eine neue externe Runtime-Anbindung benötigt vor Implementierung eine dokumentierte Bewertung von Zweck, übertragenen Daten, Credential-Bedarf, Alternativen und EU-/Open-Source-Optionen.
5. Eine bestehende Ausnahme begründet keine automatische Freigabe weiterer Dienste desselben Anbieters.

EU-first ist ausdrücklich **keine** Behauptung, dass sämtliche Infrastruktur oder sämtliche Upstream-Dienste innerhalb der EU betrieben werden.

## Aktuelles Service-Inventar

| Dienst | Zweck | Runtime-Daten | Credential | Einordnung |
|---|---|---|---|---|
| `api.abfahrt.now` | Abfahrten, Stationen, `/trips` | Standort-/Routingkoordinaten, Filter-/Transitparameter | persönlicher abfahrt.now-Key, verpflichtend | fachlicher Primärdienst; Community-App ist unabhängig vom API-Anbieter |
| `photon.komoot.io` | Orts-/Stations-Geocoding | Nutzersuchtext, Sprache, optionaler `lat/lon`-Bias | keines | Open-Source Photon auf OpenStreetMap; kein API-Key |
| `api.heigit.org/openrouteservice/` | optionale WALK/BIKE-Matrix und Directions | Start-/Zielkoordinaten, Routingprofil | persönlicher HeiGIT/ORS-Key, optional | HeiGIT, Heidelberg; bevorzugte europäische Routinganbindung |
| OpenStreetMap-Kartenressourcen | Kartenhintergrund für MapLibre-Vorschauen | Requests aus sichtbarem Kartenausschnitt | keines | offene Kartendaten; nur bei sichtbarer Karte |
| Google Play Services Location | Geräteposition | Standortzugriff über Android-Plattform-API | kein Projekt-Key | dokumentierte Plattformausnahme; kein eigener App-Backenddienst |
| `api.github.com` / GitHub Releases | anonymer Check auf ein neueres Community-App-Release | User-Agent und öffentliche Release-Metadatenanfrage; **keine** Transit-, Such- oder Standortdaten | keines | bewusste Nicht-EU-Runtime-Ausnahme für Distribution; strikt credential-isoliert |

## Credential-Isolation

Credentials sind strikt dienstgebunden:

- der abfahrt.now-Key darf ausschließlich an den vorgesehenen abfahrt.now-Host übertragen werden;
- der HeiGIT/ORS-Key darf ausschließlich an den vorgesehenen HeiGIT/openrouteservice-Host übertragen werden;
- Photon, Kartenressourcen und GitHub erhalten **keinen** dieser Keys;
- ein OkHttp-/Retrofit-Client mit Credential-Interceptor darf nicht für einen fremden Host wiederverwendet werden;
- Logs und öffentliche Evidence dürfen keine Keys/Ciphertexte enthalten.

Build 150 setzt den GitHub-Update-Checker deshalb über einen **eigenen anonymen OkHttp-/Retrofit-Client** um. Ein fokussierter JVM-Test schützt zusätzlich, dass der GitHub-Client keinen `ApiKeyInterceptor` installiert.

## Update- und Release-Metadaten

Für den Community-Update-Check gilt ab Build 150:

- Quelle ist ausschließlich `GET https://api.github.com/repos/K-Cosmo/abfahrt-now-android/releases/latest`;
- die App verwendet keinen GitHub-Token und sendet keine abfahrt.now-/ORS-Credentials an GitHub;
- Release-Tags für App-Updates folgen `v<semver>-b<build>`, z. B. `v1.1.0-b150`;
- die monotone **Buildnummer** ist für die Update-Entscheidung maßgeblich; Android `versionCode` entspricht derzeit `build * 10`;
- ein nicht passendes/unsicheres Tag wird ignoriert;
- Netzwerk-, 404-, Rate-Limit- oder sonstige GitHub-Fehler blockieren die App nicht und werden dem Nutzer nicht als Kernfehler präsentiert;
- ein verfügbares Update öffnet nach Nutzeraktion nur die feste Release-Seite dieses Repositories im Browser;
- kein automatischer APK-Download, keine stille Installation und kein eigener Updater mit Installationsrechten.

Damit bleibt GitHub eine kleine, klar abgegrenzte Distribution-Ausnahme innerhalb des EU-first-Prinzips. Sollte später ein gleichwertiger EU-gehosteter Release-Metadatenpfad betrieben werden, kann diese Ausnahme separat neu bewertet werden.

## Lokalisierung

Die App liefert aktuell **22 gebündelte UI-Sprachressourcen** aus. Die UI-Lokalisierung benötigt keinen externen Übersetzungsdienst zur Laufzeit. Maßgeblich für die Locale-Regeln ist [`13-localization.md`](13-localization.md).

## Änderungen an diesem Inventar

Jeder neue oder ersetzte externe Runtime-Dienst muss vor Produktivnutzung:

- in diesem Dokument mit Zweck und Datenfluss ergänzt werden,
- mit den Security-/Release-Invariants abgeglichen werden,
- durch passende Tests/Evidence abgesichert werden,
- und bei einer Ausnahme vom EU-first-Prinzip eine kurze Begründung enthalten.
