# Build Invariants

Diese Regeln sind normativ. Ein Build, der sie verletzt, ist nicht abnahmefähig, solange die Regel nicht ausdrücklich geändert wurde.

## Governance

### I-001 `/doc` ist alleinige normative Wahrheit
Chat, README, Specs, Evidence und Code können Informationen liefern, ändern aber eine Norm nicht automatisch.

### I-002 Kleine, isolierte Änderungen
Keine stillen Architekturwechsel, kein paralleler Datenpfad, keine neue Dependency ohne begründeten Mehrwert.

### I-003 Evidence vor Behauptung
Build-/Runtime-Status wird über reale Tests, Logs, API-Antworten oder reproduzierbare Befunde abgenommen.

## Produkt / Filter

### I-010 Mindestens ein Verkehrsmittel aktiv
Ein leerer Mode-Satz ist nicht zulässig; bei leerem Persistenzwert fällt die App auf alle Modi zurück.

### I-011 Radius definiert den Suchraum; 20 m sind nur technische Nearest-Toleranz
Für Current-Location gilt der eingestellte Radius als harte äußere Grenze. Innerhalb dieses Radius wird pro Linie+Richtung die fachlich nächste relevante Haltestellensituation bestimmt. Kandidaten bis `nearestDist + 20 m` dürfen als technische Toleranz gegen GPS-/Stationspunkt-/Steigabweichungen gleichwertig behandelt werden.

Die 20 m erweitern **nicht** den Nutzer-Radius, sind kein allgemeines Stations-Clustering und dürfen keine Gegenrichtungen oder fachlich getrennten Haltestellen zusammenführen.

### I-012 Beide Richtungen bleiben getrennt
Dedup-Schlüssel ist Linie + normalisierte Richtung. Echte Gegenrichtungen dürfen nicht zusammengeführt werden.

### I-013 Provider-Kontext ist keine neue Richtung
Trailing Ortskontext wie `(Berlin)` erzeugt keine zweite fachliche Richtung. Der Provider-Rohwert bleibt erhalten; nur der daraus abgeleitete Service-Key ignoriert diesen nicht-semantischen Kontext.

### I-014 Terminal-/Stop=Direction-Einträge werden ausgeblendet
Zeigt die normalisierte Richtung auf die eigene Station, ist der Eintrag nicht hilfreich und wird nicht dargestellt.

## Distanz / Sortierung

### I-020 HERE bis 35 m
`stationDistance` von 1 bis einschließlich 35 m bedeutet fachlich „Hier“: 0 m/0 min in der Darstellung und höchste Listenpriorität. Rohdaten bleiben intern erhalten.

### I-021 Effektive Distanz
Nach HERE gilt für Current-Location primär eine echte ORS-Geh-/Fahrraddistanz; fehlt sie, wird Stations-/Fallback-Distanz verwendet. Approximate/Fallback ist visuell von finaler ORS-Distanz zu unterscheiden.

### I-022 Zentrale Sortierreihenfolge
Sichtbare Reihenfolge: HERE → effektive Distanz → Linie → Abfahrtszeit; weitere Felder dürfen nur deterministische Tie-Breaker sein. `DepartureDisplayOrdering` ist die Implementierungsquelle dafür.

### I-023 Distanzsymbolik ist konsistent
Finale ORS-Distanz: Richtungs-/NearMe-Symbol. Station/Approximate: Luftlinien-/Flugzeug-Symbol. Kein gültiger Distanzwert: kein Distanzquellen-Symbol. Standort-Pin kennzeichnet die Station, nicht die Distanzqualität.

## Merge / Refresh / Async

### I-030 Keine direkte UI-Fachlogik
Merge, Dedup, Sortierung und Identitätsbildung gehören nicht in Compose-Composables.

### I-031 Same-Origin-Refresh ist Stable-Merge
Ein transient kleiner finaler Response darf eine bereits vollständige Liste nicht allein durch temporäre API-Unvollständigkeit kollabieren lassen. Alte, noch gültige Abfahrten können als Grace-Pool weiter durch dieselben Filter laufen.

### I-032 Standortwechsel nutzt keinen Same-Origin-Stable-Merge
Bei relevantem Ziel-/Standortwechsel darf die alte Kandidatenmenge nicht fachlich in die neue Umgebung konserviert werden.

### I-033 Kein Loading-Blackout bei bereits sichtbaren Daten
Während Ersatzdaten geladen werden, darf die bisherige Liste sichtbar bleiben; nach erfolgreichem Wechsel gilt ausschließlich der neue Kontext.

### I-034 Core-Refresh wird nicht durch ORS blockiert
abfahrt.now ist Primärdatenquelle. ORS ist nachgelagerte Anreicherung.

### I-035 Stale Async-Ergebnisse werden verworfen
ORS-/Netzwerkergebnisse eines alten Request-/Target-Kontexts dürfen keinen neueren Zustand überschreiben.

### I-036 ORS-Metriken werden bei praktisch gleichem Ursprung wiederverwendet
Bewegungsschwelle aktuell 200 m. Ein bloßer Zeitablauf erzwingt keine globale Neuberechnung.

### I-037 Erreichbarkeitsfilter nur mit finaler Dauer
Fehlende oder approximate ORS-Daten dürfen eine Abfahrt nicht als „nicht erreichbar“ ausblenden.

## Datenidentität / API

### I-040 Roh-ID ist kein UI-Name
Technische Provider-Stop-ID darf nicht als sichtbarer Haltestellenname oder fachlicher Stopname durch die Pipeline laufen, wenn eine lesbare Zuordnung verfügbar ist.

### I-041 Technische Stop-ID bleibt erhalten
Wird eine Roh-ID auf einen lesbaren Namen angereichert, bleibt sie separat als `providerStopId` verfügbar.

### I-042 Keine Mehrregionenannahme aus `::N`
Plattform-/Steigbedeutung von Provider-ID-Suffixen wird erst nach externer oder Mehrregionen-Evidence zur Produktlogik.

### I-043 Cleanup ist abgeleitet, nicht destruktiv
UI-Cleanup und technische Normalisierung dürfen Provider-Rohwerte nicht als vermeintlich „saubere“ neue Wahrheit überschreiben. Für Matching/Dedup dürfen eng begrenzte, dokumentierte abgeleitete Schlüssel verwendet werden. Bei Stop-ID-Enrichment bleibt der technische Rohwert separat als `providerStopId` erhalten.

## UI / Details

### I-050 Follow-up-Zeiten lösen keinen zusätzlichen Departure-Request aus
Bis zu drei Folgeabfahrten stammen aus dem bereits geladenen lokalen Rohdatenhorizont.

### I-051 API-Key-UX bleibt kompakt und abfahrt.now ist verpflichtend
Settings zeigen Status und Aktion; Key-Eingabe erfolgt im separaten Detailsheet. Schlüssel werden nicht im Klartext in der Hauptansicht exponiert. Ab Build 136 ist ein nichtleerer abfahrt.now-Key Voraussetzung für den Departure-Hauptflow. Der verpflichtende Key darf in der UI nicht gelöscht werden; er kann ersetzt werden. ORS bleibt optional und löschbar.

### I-052 Version hat genau eine Runtime-Quelle
`versionName`/`versionCode` werden zentral in Gradle gepflegt; UI/User-Agent leiten daraus ab.

### I-053 HERE-Details zeigen Standort, keine Gehroute
Wenn `Departure.isHereOverride()` wahr ist, darf das Detailsheet keine ORS-Routenvorschau anfordern oder eine Walking-/Bike-Polyline zeichnen. Bei verfügbaren Koordinaten zeigt die MapLibre-Karte nur Query-Origin und Haltestelle; die normale ORS-Routenvorschau bleibt ausschließlich Nicht-HERE-Fällen vorbehalten.

## Security / Release

### I-060 Release-Network-Logging ist aus
OkHttp-Logging ist nur im Debug-Build aktiv.

### I-061 API-Key-Preferences bleiben aus Backup/Device-Transfer ausgeschlossen
Auch nach Build 125 bleibt die DataStore-Datei aus Cloud-Backup und Device-Transfer ausgeschlossen. Der Ciphertext ist an einen gerätegebundenen Android-Keystore-Schlüssel gekoppelt und darf nicht ohne diesen Schlüssel auf ein anderes Gerät restauriert werden.

### I-062 API-Keys werden nicht im Klartext persistiert
Ab Build 125 dürfen neue oder migrierte abfahrt.now-/ORS-API-Keys nicht als Klartext in DataStore geschrieben werden. Persistiert wird ausschließlich das versionierte `enc:v1:`-Format; der Verschlüsselungsschlüssel bleibt im Android Keystore. Logs dürfen weder Klartext-Key noch Ciphertext ausgeben.

### I-063 Fehlender/abgewiesener abfahrt.now-Key führt zur Key-Eingabe
Ein bestehendes `onboardingCompleted=true` reicht nicht, wenn der entschlüsselte abfahrt.now-Key leer ist. HTTP 401 aus dem abfahrt.now-Kernflow beendet den Access-Status und führt zurück zur Key-Korrektur; der vorhandene Key wird dabei nicht automatisch gelöscht.

### I-064 Release braucht echte Abnahme
Kompilieren allein ist keine Freigabe: Unit-Tests, Release-Minify-Build und definierte Smoke-/Runtime-Evidence sind erforderlich.

### I-065 Externe Runtime-Dienste brauchen dokumentierten Zweck
Jede neue externe Runtime-Anbindung muss vor Produktivnutzung in `14-community-and-service-policy.md` mit Zweck, übertragenen Daten, Credential-Bedarf, Alternativen und EU-/Open-Source-Einordnung dokumentiert sein.

### I-066 Credentials überschreiten keine Service-Grenze
Der abfahrt.now-Key darf ausschließlich an den vorgesehenen abfahrt.now-Host, der ORS-Key ausschließlich an den vorgesehenen HeiGIT/openrouteservice-Host gesendet werden. Credential-tragende OkHttp-/Retrofit-Clients oder Interceptors dürfen nicht für Photon, OSM-Tiles, GitHub oder andere Hosts wiederverwendet werden.

### I-067 UI-Lokalisierung ist lokal gebündelt
Die unterstützten UI-Sprachen werden als Android-Ressourcen mitgeliefert. Für die normale UI-Lokalisierung ist kein externer Übersetzungsdienst zur Laufzeit zulässig; neue Locale-Sets müssen den Paritätsgate erfüllen oder eine explizit dokumentierte Ausnahme haben.
