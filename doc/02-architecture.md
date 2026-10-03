# Architektur

## Runtime-Baseline

- Kotlin / JVM 17
- Jetpack Compose + Material 3
- Hilt
- Retrofit / OkHttp / Gson
- Preferences DataStore
- Android Keystore für die at-rest-Verschlüsselung der beiden API-Keys
- Google Play Services Location
- MapLibre Android SDK
- Coroutines / Flow
- `minSdk 34`, `compileSdk 37`, `targetSdk 37`

## Externe Runtime-Dienste und Trust Boundaries

Die App trennt externe Dienste fachlich und credential-seitig. abfahrt.now, Photon, HeiGIT/openrouteservice, OpenStreetMap-Tiles, Google Play Services Location und künftig GitHub-Release-Metadaten sind **keine** gemeinsame Backend-Schicht. Ein Client/Interceptor mit Credential darf nicht für einen fremden Host wiederverwendet werden.

Das normative Service-Inventar einschließlich EU-first-/Ausnahmeregeln steht in [`14-community-and-service-policy.md`](14-community-and-service-policy.md). Insbesondere erhält GitHub für den geplanten Update-Checker einen separaten anonymen HTTP-Client; abfahrt.now-/ORS-Keys, Standortkoordinaten und Suchtexte dürfen nicht in diesen Pfad gelangen.

## Access-Gate

Ab Build 136 wird der Departure-Hauptflow nur gerendert, wenn `onboardingCompleted == true` **und** der entschlüsselte abfahrt.now-Key nichtleer ist. Diese Entscheidung ist ein eigener, reiner Access-Gate und keine Netzwerk-/UI-Heuristik. HTTP 401 setzt nur den Access-Status zurück; der gespeicherte Key bleibt zur Korrektur erhalten. ORS ist von diesem Gate unabhängig.

## Hauptfluss

```text
Compose UI
  -> DepartureViewModel
      -> TransitRepository -> abfahrt.now
      -> WalkingRouteRepository -> OpenRouteService
      -> GeocodingRepository -> Photon
      -> UserPreferencesRepository -> ApiKeyCipher -> Android Keystore
                                 -> DataStore (API-Keys nur als Ciphertext)
```

Die fachliche Verarbeitung sichtbarer Abfahrten bleibt zentral im ViewModel/den dafür extrahierten Utilities. UI-Composables dürfen keine alternative Dedup-/Sortierlogik aufbauen.

## Zentrale fachliche Bausteine

- `DepartureDisplayOrdering`: zentrale sichtbare Reihenfolge und effektive Distanz.
- `DepartureServiceIdentity`: zentrale Linie+Richtung-Identität.
- `DepartureStableMerger`: Stable-Merge bei Same-Origin-Refreshes.
- `DepartureFollowUpTimes`: lokale Folgeabfahrten im Detailsheet.
- `StationNameNormalizer`: generische Normalisierung für technische Zuordnung/UI.
- `DepartureFetchPolicy`: breiter Rohdatenhorizont für Detailsheet-Folgezeiten.

## Refresh-/ORS-Modell

1. Core-Abfahrten über abfahrt.now laden.
2. Direct-stop/Add-on-Abdeckung und Stationsdaten zusammenführen.
3. Provider-Stop-IDs soweit möglich in lesbare Stationsnamen auflösen.
4. Filter/Dedup/Sortierung anwenden und UI aktualisieren.
5. ORS-Anreicherung asynchron nachziehen.
6. Neue ORS-Daten dürfen denselben Kandidatenpool neu bewerten; stale Ergebnisse eines alten Ziel-/Request-Kontexts werden verworfen.

Ein laufendes ORS-Enrichment darf den Core-Refresh fachlich nicht blockieren.

## Standortkontext

- Relevante Bewegungsschwelle: 200 m.
- Unterhalb der Schwelle können vorhandene ORS-Metriken weiterverwendet werden.
- Bei relevantem Ziel-/Standortwechsel wird das neue Ergebnis ohne Same-Origin-Stable-Merge bewertet.
- Bereits sichtbare alte Daten dürfen während des Ersatzloads stehen bleiben, um keinen Loading-Blackout zu erzeugen; sie dürfen aber nicht dauerhaft in den neuen Standortkontext gemerged werden.

## Bekannte Architektur-Schulden

- `DepartureViewModel.kt` ist mit rund 2.000 Zeilen ein Wartbarkeitshotspot.
- API-Keys liegen in Preferences DataStore nur als versionierter AES-GCM-Ciphertext. Der AES-256-Schlüssel wird nicht exportierbar im Android Keystore gehalten. Klartext existiert nur zur Laufzeit im App-Prozess, wenn Requests oder Key-Editor ihn benötigen.
- Linienfarb-Mapping ist regional teilweise hardcodiert; langfristig datengetriebene Quelle erwünscht.
- Der öffentliche Repo-Stand enthält die Gradle-Wrapper-Properties und Launcher. Solange `gradle-wrapper.jar` noch nicht eingecheckt ist, bootstrappt CI den offiziellen Gradle-9.6.0-Wrapper mit festem SHA-256; vollständige Offline-/Source-Selbstständigkeit bleibt bis zum eingecheckten JAR als F-DOC1-008 offen.

Große Refactorings sind **kein** Selbstzweck. Aufteilung nur bei konkretem Nutzen und in kleinen, regressionsgesicherten Schritten.


## Geplanter Routingfluss

Build 137 lagert die Photon-Nebenfunktion „Abfahrten an anderem Ort“ aus der Startseite in die geschützte Nav-Route `alternate_departures` aus. Startseite und Nebenflow teilen weiterhin denselben `DepartureViewModel`-/Repository-Unterbau; es entsteht keine zweite Departure-Architektur. Beim Verlassen der Nebenroute wird der SearchTarget-Zustand auf `CurrentLocation` und der UI-State auf `Idle` zurückgesetzt, damit keine Alternate-Response auf der Startseite aufblitzt. Build 138 verwendet dasselbe `GeocodingRepository` zusätzlich für allgemeine Photon-Zielsuche und ruft danach den bereits vorhandenen `/trips`-Pfad im `TransitRepository` auf. Route-spezifischer UI-State liegt im kleinen `RoutePlannerViewModel`; Departure-Sortierung/-Filterung bleibt vollständig im bestehenden `DepartureViewModel`. Beide nutzen dieselben Repository-/API-Schichten; keine zweite Routing- oder Departure-Architektur.
