# Produkt

## Zielbild

Abfahrt! beantwortet eine enge, alltagsnahe Frage: **Welche relevanten öffentlichen Verkehrsmittel fahren in meiner Umgebung als Nächstes ab, und welche Haltestelle ist dafür aus Nutzersicht die sinnvollste?**

Die App soll ausdrücklich **kein Google-Maps-Ersatz** sein. Kern ist eine schnell erfassbare Abfahrtsanzeige mit möglichst wenig Bedienaufwand.

## Produktprinzipien

- KIS: wenige verständliche Einstellungen, keine technisch erklärungsbedürftigen Sonderwege.
- Entfernung ist zentrales Ordnungsprinzip.
- Pro Linie und Richtung soll die lokal relevante Haltestelle gewinnen.
- Beide Fahrtrichtungen bleiben fachlich getrennt.
- Nutzer kann Verkehrsmittel wählen; mindestens eines bleibt immer aktiv.
- Echtzeit-Abfahrten haben Priorität vor optionaler ORS-Anreicherung.
- Teilresultate dürfen früh erscheinen, solange sie nicht wissentlich fachlich falsch dargestellt werden.
- Fehlende Zusatzdaten dürfen die Kernfunktion nicht blockieren.
- Update-Verfügbarkeit ist Zusatzkomfort: ein fehlgeschlagener Update-Check darf die App weder blockieren noch als Kernfehler erscheinen.
- Die App ist ein unabhängiges Community-Projekt. Externe Daten-/API-Anbieter werden klar als solche benannt und dürfen nicht als App-Betreiber erscheinen.

## Aktueller Funktionsumfang Build 151

### Community-Identität Build 151
- Settings-Footer bezeichnet Abfahrt! sichtbar als unabhängige/unoffizielle Community-App und grenzt sie von abfahrt.now ab.
- abfahrt.now bleibt als Quelle der Transitdaten/API sichtbar.
- der Projektlink führt auf das kanonische GitHub-Repository `K-Cosmo/abfahrt-now-android`.
- externe Privacy-/Terms-Links sind ausdrücklich als API-Provider-Links bezeichnet.
- die Community-Texte liegen in allen 22 gebündelten UI-Locale-Sets vor.

### Routing-/Such-UX Build 146–148
- Uhrzeiten in Route-Legs sind in derselben festen linken Spalte wie Modus-Symbol und Linienbadge zentriert; Namen/Details beginnen auf einer gemeinsamen Inhaltsachse.
- erster und letzter Fußweg bieten oben rechts eine Aktion zum Öffnen der Geh-Navigation; Android-Intent mit Karten-Fallback, ohne neue App-Abhängigkeit.
- gespeicherte Orte `Zuhause`/`Arbeit` werden nur bei leerem Suchfeld als Schnellwahl gezeigt und verschwinden sofort beim Tippen.
- allgemeine Photon-Zielsuche verändert den Nutzereingabetext nicht mehr. Photon erhält den getrimmten Originaltext plus vorhandenen `lat/lon`-Standort-Bias; es gibt keine PLZ-/City-/Transit-Heuristik und kein clientseitiges Re-Ranking dieser allgemeinen Treffer. Der separate Flow „Abfahrten an anderem Ort“ behält dagegen seine stationsspezifische Transit-Priorisierung.

### Update-Hinweis Build 150
- pro Activity-/App-Start wird einmal asynchron die öffentliche `latest`-Release-Metadatenquelle dieses Community-Repositories geprüft;
- nur ein Release-Tag im Schema `v<semver>-b<build>` kann einen Hinweis auslösen;
- die monotone Buildnummer entscheidet, ob das Release neuer ist;
- ein verfügbares Update erscheint als kompakter lokalisierter Dialog;
- „Update öffnen“ öffnet ausschließlich die konkrete GitHub-Release-Seite im Browser;
- Fehler, Offline oder fehlendes Release bleiben still;
- kein automatischer APK-Download und keine stille Installation.

Im Quellstand nachweisbar:

- Standortbasierte Abfahrtssuche auf der Startseite.
- „Abfahrten an anderem Ort“ als eigener Menü-Flow mit der bisherigen Photon-Station-/Haltestellensuche.
- Allgemeine Photon-Zielsuche auf der Startseite mit direktem Übergang in den `/trips`-RoutePlanner.
- Suchradius: 100–2.000 m, Default 800 m.
- Sichtbares Abfahrtsfenster: 0–120 min, Default 0–30 min.
- Automatischer Refresh: aus oder 1–5 min, Default 1 min.
- Auswahl der Modi U-Bahn, S-Bahn, Tram, Bus, Regional, Express, Fähre.
- Maximal 1–3 sichtbare Abfahrten pro Linie+Richtung, Default 1.
- Vier konfigurierbare Schnellwahl-Slots.
- verpflichtender persönlicher abfahrt.now-API-Key; ohne Key kein Zugang zum Abfahrts-Hauptflow.
- optionaler OpenRouteService-Key.
- ORS-Modus WALK oder BIKE.
- optionaler Filter „nicht erreichbare Abfahrten ausblenden“; arbeitet nur mit finalen ORS-Dauern.
- HERE-Override bei `stationDistance <= 35 m`: Anzeige „Hier“, 0 min, höchste Sortierpriorität.
- HERE-Detailsheet: keine ORS-Routenberechnung/Polyline; stattdessen kompakte Standortkarte mit Query-Origin und Haltestellenmarker.
- Stable Refresh Merge gegen transient unvollständige API-Antworten.
- Detailsheet mit bis zu drei lokal vorhandenen Folgeabfahrten ohne zusätzlichen Departure-API-Request.
- ORS-Routenvorschau mit MapLibre und externem Karten-Fallback.
- mehrsprachige UI mit 22 Sprach-Ressourcensätzen plus Systemsprache.
- anonymer GitHub-Release-Update-Hinweis ohne Credential-Weitergabe.
- sichtbare unabhängige Community-Identität im Settings-Footer.

## Bewusste Nicht-Ziele

- der Routenplaner bleibt ein direkter, aber sekundärer Flow neben der Abfahrtstafel; er ersetzt nicht den schnellen Departure-Hauptzweck;
- keine zweite parallele Daten-/Sortierarchitektur im UI;
- keine aggressive Optimierung oder Cache-Komplexität ohne reale Messdaten;
- keine providerübergreifend hartkodierten Linienfarben ohne belastbare Quelle;
- kein eigener Paketinstaller/Background-Downloader für Updates.

## Route-planen-Flow ab Build 138

- Das bestehende Suchfeld auf der Startseite bleibt erhalten und wird zum direkten Einstieg **„Route planen – Ziel eingeben“**.
- Auswahl eines Photon-Ziels öffnet ohne zusätzlichen Zwischentap den RoutePlanner mit `Von = aktueller Standort` und `Nach = gewähltes Ziel`.
- Die bisherige Funktion „Abfahrten an anderem Ort“ ist seit Build 137 auf eine eigene Seite hinter dem Startseiten-Menü verschoben.
- Routenverbindungen stammen aus dem vorhandenen abfahrt.now-`/trips`-Pfad; keine simulierte Transitgeometrie über ORS.
- Build 137 entflechtete Navigation/Photon-Nebenflow. Build 138 liefert das MVP: allgemeine Photon-Ziele, direkter Planner, Von/Nach änderbar, Tausch und `/trips`-Ergebnisse. Builds 139–145 verfeinern Darstellung, Zwischenhalte, Transferklarheit, gespeicherte Orte und lokale Routensortierung. Zeitwahl bleibt bis zu einer offiziellen `/trips`-Zeitparameter-Erweiterung blockiert.
