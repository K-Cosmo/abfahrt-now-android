# Entscheidungen

## G-001 — `/doc` ist einzige normative Quelle
Ab DOC1 liegt die bleibende Produkt-/Technikwahrheit ausschließlich in `/doc`. Chat, README, Specs, Evidence und Sourcecode sind Arbeits-/Implementierungs-/Beobachtungsebenen. Eine Codeabweichung ändert die Norm nicht automatisch, sondern erzeugt ein Finding bzw. einen Bug.

## G-002 — Spec Kit ist Prozess-Governance, keine zweite Wahrheit
`.specify/memory/constitution.md` beschreibt den Entwicklungsprozess. `/specs/<build-or-feature>/` hält temporär Specification, Plan und Tasks. Nach Implementierung und echter Evidence werden dauerhafte Entscheidungen nach `/doc` konvergiert.

## G-003 — Verbindlicher Workflow
Standardfolge: **Specification → Plan → Tasks → Implementierung → Analyse/Converge → echte Tests/Evidence → Abnahme**. Kleine Builds dürfen verkürzt werden, dürfen aber Evidence und Konvergenz nicht durch Behauptungen ersetzen.

## G-004 — DOC1 ist kein Runtime-Build
DOC1 ändert nur Dokumentation, QA-/Governance-Struktur und zugehörige Prüfscripte. `versionName 1.1.0` und `versionCode 1220` bleiben unverändert. Der nächste technische Runtime-Build ist 123.

## G-005 — D-002 „README folgt dem Code“ ist superseded
README ist ab DOC1 nur noch Einstieg. Bei Abweichungen zwischen `/doc` und Code wird nicht automatisch `/doc` angepasst; zuerst wird geklärt, ob Codebug, veraltete Norm oder bewusst noch nicht konvergierter Zwischenstand vorliegt.

## G-006 — Priorität nach DOC1
Vor Komfort-/Refactoring-Arbeit gelten: Build-122-Runtime-Abnahme, ORS-Hostmigration, API-Contract-Klärung, danach die bereits geplanten HERE/Map-, Key-Security-, 16-KB- und API-37-/Toolchain-Themen in kleinen getrennten Builds.


## D-046 — 16-KB-Kompatibilität wird am Artefakt abgenommen
Build 126 führte keinen vorsorglichen MapLibre-Major-Bump ein. Ausschließlich das erzeugte APK/AAB plus ein 16-KB-Runtime-Smoke gelten als belastbarer Nachweis. Der Build-126-Release-APK-Audit lieferte anschließend genau die negative Evidence, die einen Dependency-Refresh rechtfertigt: Packaging/ZIP-Ausrichtung war korrekt, mehrere vorgefertigte ELF-Binaries waren jedoch nicht 16-KB-kompatibel.

## D-047 — Build 127 repariert 16-KB-Evidence dependency-seitig und bleibt innerhalb bestehender Majors
Build 127 ändert keine Transit-, Routing-, UI- oder Security-Logik. Die Remediation ist auf die im Build-126-Artefakt tatsächlich auffälligen vorkompilierten Native-Libraries begrenzt:
- DataStore `1.1.2 -> 1.2.1`;
- `androidx.graphics:graphics-path` wird explizit als direkte Abhängigkeit in Version `1.1.0` gesetzt, statt eine ältere transitive Binary unkontrolliert zu übernehmen;
- MapLibre `11.12.1 -> 11.13.5`, bewusst innerhalb der 11.x-Linie.

Das Artifact-Audit behandelt `arm64-v8a` und `x86_64` als harte 64-Bit-Acceptance-Gates. 32-Bit-ABI-Abweichungen werden weiterhin ausgegeben, aber als Warning geführt. Unbekannte ABI-/Packaging-Pfade bleiben aus Sicherheitsgründen gating. Die tatsächliche Freigabe erfolgt erst nach erneutem Release-APK/AAB-Audit und einem 16-KB-Runtime-Smoke; Versionsnummern allein sind keine Evidence.

---

## D-048 — Build 128 ist negative 16-KB-Evidence, kein akzeptierter Remediation-Build
Der reale Release-APK-Audit von Build 128 (`audit version=2`) zeigt vier harte 64-Bit-Fehler: `graphics-path` und MapLibre auf jeweils `arm64-v8a` und `x86_64`. DataStore bleibt grün; sämtliche ZIP-Datenoffsets bleiben 16-KB-ausgerichtet. Der Wechsel auf MapLibre OpenGL 12.3.1 hat den MapLibre-Blocker damit nicht beseitigt und wird nicht als Erfolg gewertet.

## D-049 — Build 129 ist der letzte isolierte MapLibre-Upstream-Test vor source-/binary-basierter Remediation
Build 129 ändert ausschließlich MapLibre OpenGL von `12.3.1` auf die stabile Version `13.6.0` sowie `versionCode` auf 1290. `app/src`, AndroidX `graphics-path`, DataStore und sämtliche Produktlogik bleiben unverändert. Die neuere Upstream-Version rechtfertigt einen letzten klar abgegrenzten Artefakttest; sie gilt **nicht** aufgrund ihrer Versions- oder NDK-Nummer als 16-KB-kompatibel.

Wenn MapLibre 13.6.0 auf `arm64-v8a` oder `x86_64` erneut am ELF-Gate scheitert, endet die Versionsiteration. Ein weiterer Build muss dann gezielt eine source-/binary-basierte Remediation untersuchen. `graphics-path:1.1.0` bleibt unabhängig davon ein separater harter Blocker, solange keine grüne Artefakt-Evidence vorliegt.

## D-050 — Build 130 eliminiert den graphics-path-Prebuilt statt ihn weiter zu versionieren
Build 129 hat den MapLibre-Blocker geschlossen: MapLibre 13.6.0 und DataStore 1.2.1 sind auf `arm64-v8a` und `x86_64` `OK ELF`; einzig `libandroidx.graphics.path.so` bleibt auf beiden Gate-ABIs am GNU_RELRO-Ende rot. Für `androidx.graphics:graphics-path` existiert im Projektstand kein neuer veröffentlichter Stable-Build oberhalb 1.1.0, während AndroidX main inzwischen eine 16-KB-orientierte Clang-Linker-Konfiguration mit `max-page-size=16384` und `common-page-size=16384` verwendet.

Da AbfahrtApp bewusst `minSdk = 34` hat und Android 14 bereits `android.graphics.PathIterator` bereitstellt, baut Build 130 **keinen eigenen nativen Fork**. Stattdessen wird das externe `androidx.graphics:graphics-path`-Modul global ausgeschlossen und die von Compose benötigte öffentliche API app-lokal im selben Package bereitgestellt. Path-Iteration nutzt die Plattform-API; Conic-zu-Quadratic-Konvertierung nutzt die aus AndroidX abgeleitete Pure-Kotlin-Implementierung. Es gibt weder JNI noch `System.loadLibrary`, CMake oder app-eigene `.so`.

Diese Ausnahme ist an `minSdk >= 34` gebunden und bleibt technisch debt/temporary compatibility code. Sie darf erst entfernt werden, wenn ein veröffentlichter Upstream-Artefaktstand durch denselben realen 16-KB-Artefaktaudit grün belegt ist. Acceptance für Build 130 verlangt, dass `libandroidx.graphics.path.so` im Release-APK nicht mehr vorkommt und sämtliche verbleibenden 64-Bit-Native-Libraries den Artefakt-Gate bestehen.


## D-051 — Android 17 wird zweistufig aktiviert: compile/toolchain vor target behavior
Build 131 setzt `compileSdk = 37`, migriert auf AGP 9.4 / Gradle 9.6 / built-in Kotlin und hält `targetSdk = 36`. Hintergrund: API 37 benötigt mindestens AGP 9.1.1, während `targetSdk 37` zusätzlich neue Android-17-Verhaltensweisen aktiviert. Toolchain-/Compile-Risiko und Runtime-Behavior-Risiko werden daher nicht in demselben Build vermischt. Build 131 ist durch realen Build, API-37-Runtime und erneutes AAB-`PAGE_ALIGNMENT_16K` abgenommen; Build 132 aktiviert danach isoliert `targetSdk = 37`.

AGP 9 built-in Kotlin ist der Sollpfad; der alte `org.jetbrains.kotlin.android`-Pluginpfad wird nicht per Opt-out konserviert. KSP bleibt Annotation-Processing-Pfad für Hilt.

## D-052 — Build 132 aktiviert targetSdk 37 ohne vorsorgliche Workarounds
Build 132 ändert ausschließlich `versionCode 1310 -> 1320` und `targetSdk 36 -> 37`; `compileSdk 37`, AGP 9.4, Gradle 9.6, built-in Kotlin, KSP, Hilt, App-Source und die abgenommene 16-KB-Remediation bleiben unverändert.

Für die aktuell relevanten Android-17-Target-Änderungen gilt app-spezifisch:
- Certificate Transparency bleibt im Plattform-Default aktiv; es wird **keine** Network-Security-Ausnahme eingebaut. Reale HTTPS-Aufrufe gegen abfahrt.now, HEIGIT ORS, Photon und Kartenressourcen sind das Gate.
- `ACCESS_LOCAL_NETWORK` wird nicht angefordert, weil AbfahrtApp keine LAN-/Discovery-Funktion besitzt und nur öffentliche Internet-Endpunkte nutzt.
- Das Manifest erhält keine Orientation-/Resizability-Sperren; Large-Screen-/Resizable-Verhalten wird runtime-seitig getestet.
- App-lokal existieren keine Widget/RemoteViews-, SMS/OTP-, ContactsProvider-, Bluetooth-RFCOMM-, MessageQueue-Reflection-, ContentCapture-Abschalt- oder dynamischen `System.load()`-Pfade, die für die dokumentierten Target-37-Änderungen Codeanpassungen erzwingen.

Versionsnummern oder statische Scans gelten nicht als Acceptance. Freigabe erfolgt erst nach realem Release-Build und API-37-Runtime-Smoke unter `targetSdk 37`.

## Historischer Decision Log Build 101–122

Die folgenden Entscheidungen werden als Historie übernommen. Bei Widerspruch mit G-001 bis G-006 oder neueren `/doc`-Invariants gilt der neuere normative Stand.

## D-001 — Repo vor Chatverlauf

Entscheidungen, Findings, Bugs, Release-Planung, Testplan und README werden im Source-Paket gepflegt. Der Chat bleibt Arbeitskanal, nicht Wahrheitsspeicher.

## D-002 — README folgt dem Code

Wenn README und Implementierung voneinander abweichen, gilt der Code. Die README wird anschließend korrigiert. Beispiel Build 102: Radius 800 m, Fensterbereich 0–120 min, Fenster-Default 0–30 min, Refresh 1 min.

## D-003 — Zeitfensteränderung lädt bei Erweiterung neu

Ab Build 102 gilt: Verkleinerungen des Zeitfensters werden lokal refiltert, wenn die vorhandenen Daten frisch sind. Erweiterungen des Zeitfensters oder stale Daten lösen einen neuen Ladevorgang für das aktuelle Ziel aus. Begründung: Die API liefert Abfahrten serverseitig anhand von `from`/`to`; zusätzliche Minuten außerhalb des bisher geladenen Fensters können nicht aus dem lokalen Cache entstehen.

## D-004 — Keine europaweiten Linienfarben hartkodieren

Linienfarben dürfen nur angewendet werden, wenn der Geltungsbereich sicher ist. Ohne Region-/Provider-Kontext nutzt die App generische Modusfarben. Zielbild: `route_color` / `route_text_color` aus GTFS oder normalisierte Farbdaten aus der abfahrt.now API.

## D-005 — Android-Ready ist ein Testprozess

Android-17-Readiness wird nicht nur über `compileSdk`/`targetSdk` definiert, sondern über SDK-Update, Verhaltenstests, UI-Adaptivität, Permissions, Networking, Backup/DataStore und echte Emulator-/Geräteläufe.

## D-006 — Buildnummern-Schema

Ab Build 101 folgt `versionCode` dem Schema `Buildnummer × 10` für Hauptbuilds. Beispiel: Build 102 → `versionCode = 1020`. Fix-Builds können die letzte Stelle nutzen.


## D-007 — Android 17 SDK-Level gestuft aktivieren

Build 103 hat `compileSdk = 37` und `targetSdk = 37` gesetzt. Der lokale Build schlug fehl, weil die Android SDK Platform 37 in der Zielumgebung nicht gefunden wurde (`platforms;android-37`). Ab Build 103 fix1 gilt deshalb: Die App bleibt mit `compileSdk = 36` und `targetSdk = 36` baubar, `minSdk = 34` bleibt erhalten. Android-17-Readiness wird als Test- und Vorbereitungspaket weitergeführt; das echte API-37-Targeting wird erst aktiviert, wenn Platform SDK 37 in der lokalen Buildumgebung verfügbar ist.

## D-008 — API-Key-Backup-Ausschluss vor Verschlüsselung

Build 103 schließt die DataStore-Datei `files/datastore/abfahrt_prefs.preferences_pb` aus Cloud-Backup und Gerätetransfer aus. Das reduzierte AB-008 sofort. Die damals noch offene Verschlüsselung wurde mit Build 125 umgesetzt; D-045 beschreibt den aktuellen Sollzustand.


## D-009 — minSdk 34 als Produktentscheidung

`minSdk = 34` ist nicht nur ein Android-17-Technikdetail, sondern eine bewusste Produktentscheidung: Die App priorisiert aktuelle Android-Versionen, geringere Kompatibilitätslast und moderne UI-/Security-Verhaltensweisen gegenüber maximaler Geräteabdeckung. Diese Entscheidung folgt der Projektregel „maximal drei Android-Hauptversionen zurück". Vor einem öffentlichen Play-Store-Release sollte die reale Zielgruppenreichweite über Play-Console-/Nutzerdaten erneut geprüft werden.

## D-010 — Debug-Logging darf Messungen nicht verfälschen

Debug-Logs sind hilfreich, dürfen aber keine O(n²)-Hotpaths fluten. Für Performance-Bewertungen gilt ab Build 104: Hotpath-Logging nur zusammengefasst oder gezielt gedrosselt; Jank-Vergleiche bevorzugt mit Release- oder logging-armem Build.

## D-011 — Distanz-Icon-Semantik

Das Flugzeug-Icon ist bewusstes Design für ungefähre Luftlinien-/Fallback-Distanz, solange keine ORS-aufgelöste Geh-/Fahrradstrecke vorliegt. Sobald ORS eine reale Strecke geliefert hat oder die Haltestelle als erreicht gilt, verwendet die App `NearMe`. Das Standort-Pin-Icon bleibt für Haltestellen-/Ortsbezug reserviert. AB-016 war daher kein zu behebender Bug, sondern eine zu dokumentierende Produkt-/UX-Entscheidung.


## D-012 — ORS-Fallback darf keinen endlosen Ladezustand erzeugen

Wenn ORS nach einem abgeschlossenen Enrichment-Lauf nicht für jeden sichtbaren Stopp eine Geh-/Fahrradzeit liefern kann, darf die UI nicht dauerhaft „Wird ermittelt…" anzeigen. Die App markiert solche Stopps als ungefähre Luftlinien-/Fallback-Distanz und zeigt im Detailsheet einen endlichen Status („Keine Gehzeit verfügbar"). Das hält die bewusst gewählte Icon-Semantik aus D-011 ein: Flugzeug = Fallback/Luftlinie, `NearMe` = ORS-aufgelöste Strecke.


## D-012 — ORS-Gehwegermittlung ist Kernlogik, nicht nur Komfort

Die ORS-Geh-/Fahrradermittlung dient nicht nur der Detailansicht, sondern der realistischeren Radius-/Erreichbarkeitsbewertung. Luftlinie bleibt Fallback, darf aber nicht als gleichwertig zur echten Gehstrecke gelten. Zielqualität: Alle angezeigten relevanten Stationen im Suchradius bekommen eine ORS-Strecke; akzeptabel sind nur wenige Ausfälle bei schlechter ORS/OSM-Datenlage.

## D-013 — Keine Toolchain-Warnings unterdrücken

Build-Warnungen zu SDK-/AGP-Kompatibilität werden nicht per `android.suppressUnsupportedCompileSdk` versteckt. Stattdessen wird die Toolchain aktualisiert oder der SDK-Schritt zurückgestellt. Build 106 nutzt Gradle 8.14.5 und AGP 8.13.2 für compileSdk/targetSdk 36.


## D-014 — ORS-Zuordnung primär über Koordinaten, nicht über Stationsnamen

Stationsnamen aus Providerdaten sind nicht stabil genug für technische Zuordnung: Varianten wie `Berlin, Brienzer Str.`, `Brienzer Str. (Berlin)` oder Plattformzusätze können dieselbe reale Haltestelle beschreiben. Für ORS-Matrix-Zellen gilt ab Build 107: Die technische Zuordnung erfolgt primär über Koordinaten-Buckets. Namen, Normalisierung und Stationsdistanz sind nur Fallbacks für die Zuordnung der berechneten Metrik zur sichtbaren Departure.

## D-015 — Provider-Ortszusätze sind Rohdaten, nicht UI-Pflicht

Die Region steht bereits im App-Kopf und der Nutzer kennt seinen lokalen Kontext. Provider-Ortszusätze sind generisch zu verstehen, nicht Berlin-spezifisch: Beispiele sind `(Berlin)`, `(Potsdam)`, `(Wien)` oder Präfixe wie `Berlin, Haltestelle`. Solche Zusätze bleiben in den Rohdaten erhalten, werden aber in Liste und Detailsheet für bessere Lesbarkeit entfernt.


## D-016 — Build-Artefakte müssen lokal kompilierbare Kotlin-Syntax enthalten

Ein ZIP gilt erst als belastbar, wenn mindestens Syntax-/Buildfehler aus bekannten lokalen Reports zurückgeführt wurden. Build 107 fix1 schließt den Kotlin-Regex-Fehler aus Build 107 (`Unsupported escape sequence`) durch Raw-Strings.


## D-017 — Stop- und Direction-Namen haben unterschiedliche UI-Cleanup-Regeln

Haltestellennamen dürfen generisch von Provider-Ortszusätzen bereinigt werden: trailing `(Ortsname)` und führende Präfixe nach Muster `Ort, Haltestelle` können für die Anzeige entfernt werden, weil die Region im App-Kopf sichtbar ist. Richtungsnamen sind anders zu behandeln: kommahaltige Ziele können fachlich korrekt sein (`Wedding, Virchow-Klinikum`, `Franz. Buchholz, Guyotstr.`, `Prenzlauer Berg, Björnsonstr.`). Bei Directions wird deshalb nur ein trailing Provider-Ortszusatz wie `(Berlin)` entfernt, niemals ein führender `Ort,`-Teil.

## D-018 — ORS-Radiusbewertung braucht alle relevanten sichtbaren Stationen, nicht nur die erste Matrix-Charge

Geh-/Fahrraddistanzen sind Kernlogik für Radius und Erreichbarkeit. Eine Station mit 800 m Luftlinie kann real 1.500 m Gehweg entfernt sein und muss dann aus der Liste fallen können. Build 108 lädt deshalb nach der ersten ORS-Matrix-Charge weitere Chargen nach, statt nicht angefragte sichtbare Stationen dauerhaft als Luftlinien-Fallback stehen zu lassen. Die Grenze bleibt bewusst gedeckelt, um API-Kosten und Rate-Limits unter Kontrolle zu halten.

## D-019 — First Paint vor Vollständigkeit

Beim Kaltstart ist ein früher, brauchbarer erster Stand wichtiger als das Warten auf alle Add-on- und ORS-Anreicherungen. Build 109 zeigt deshalb die initiale, API-seitig deduplizierte Antwort sofort als vorläufige Liste. Die genauere eigene Dedup-/Stop-Anreicherung und ORS-Gehwegermittlung finalisieren den Stand anschließend.

## D-020 — Stop=Direction ist kein hilfreicher Abfahrtseintrag

Wenn die normalisierte Richtung einer Abfahrt auf die eigene Station zeigt, wird der Eintrag ausgeblendet. Das ist kein regulärer Ziel-auf-nähere-Station-Fall, sondern ein Terminal-/Nicht-weiter-Fall: Der Nutzer ist bereits an der angegebenen Zielstation.

## D-021 — RoutePreview-Zielauflösung muss Koordinaten finden, nicht Namen routen

ORS-Routing erfolgt weiterhin ausschließlich über Koordinaten. Namen werden nur genutzt, um aus den Provider-Stationsdaten die passende Zielkoordinate zu finden. Da Providernamen variieren können, nutzt die UI dafür ID, exakten Namen, generisch normalisierten Namen und als letzte konservative Stufe eine Stationsdistanz-Toleranz.

## D-022 — Superseded: Initialer deduplizierter Stand war Refresh-semantisch

Diese Entscheidung wurde durch D-025 ersetzt. Der deduplizierte API-Stand ist nicht mehr Refresh-semantisch, sondern nur noch Kaltstart-Booster für den leeren Screen.

## D-023 — RoutePreview-Normalisierung darf Stationszusätze entfernen

Für die technische RoutePreview-Zielauflösung dürfen Stationsnamen stärker normalisiert werden als für Richtungsnamen. Provider-/Ortszusätze und zusätzliche Klammerqualifier wie `(Am Schäfersee)` werden entfernt, damit Varianten wie `U Franz-Neumann-Platz (Am Schäfersee) (Berlin)` auf dieselbe Zielkoordinate wie `U Franz-Neumann-Platz` auflösbar sind. ORS selbst wird weiterhin ausschließlich mit Koordinaten aufgerufen.


## D-024 — Stationsnormalisierung ist generisch, nicht stationsspezifisch

Provider-Ortszusätze und Haltestellenqualifier dürfen nicht über einzelne bekannte Stationsnamen behoben werden. Für RoutePreview, Repository-Enrichment, Anzeige und Ziel-/Terminalfilter gilt ab Build 111 eine gemeinsame Normalisierungslogik. Sie muss für API-Daten aus allen unterstützten Ländern funktionieren: Beispiele sind `Berlin, ...`, `Düsseldorf, ...`, `Paris, ...`, `Madrid, ...`, trailing `(Ortsname)` sowie zusätzliche Plattform-/Lagehinweise in runden oder eckigen Klammern. Richtungsnamen bleiben bewusst konservativer: führende kommahaltige Zielpräfixe werden dort nicht entfernt.

## D-025 — Dedup-Booster nur für leeren Kaltstart

Der API-seitig deduplizierte Response ist ein First-Paint-Booster für den Zustand „ich sehe noch nichts“. Sobald bereits Abfahrten sichtbar sind, darf ein Refresh keinen vorläufigen deduplizierten Zwischenstand mehr emittieren. Normale Refreshes nutzen `dedup=off` und die App-eigene Dedup-/Filterlogik.

## D-026 — ORS-Metriken bleiben bei unverändertem Standort stabil

Wenn sich der Standort nicht wesentlich geändert hat, werden vorhandene Geh-/Fahrradmetriken auf neue Abfahrtsdaten übertragen. Ein Refresh aktualisiert dann primär Zeiten und Verfügbarkeit; ORS wird nur bei Kaltstart, Standortwechsel jenseits der Bewegungsschwelle oder explizitem Travel-Mode-Wechsel neu gestartet.


## D-027 — Normale Refreshes sind Stable-Merge, kein harter Listenersatz

Wenn der Standort unverändert bleibt und bereits Abfahrten sichtbar sind, darf ein finaler Refresh die Liste nicht nur deshalb reduzieren, weil der aktuelle API-/Add-on-Response weniger Rohdaten enthält. Neue API-Daten aktualisieren vorhandene Einträge und fügen neue hinzu; nicht erneut gelieferte, aber zeitlich noch gültige alte Einträge bleiben als Grace-State erhalten und werden durch dieselbe Filter-/Dedup-Pipeline geschickt. Entfernt wird über Zeitfenster, Radius/Reachability, Modusfilter, Ziel-/Terminalfilter und spätere echte Abwesenheit, nicht durch einen einzelnen transient kleineren Response.

## D-028 — Hard-Reset bei sichtbarer Liste ist kein Loading-Blackout

Wenn bereits Abfahrten sichtbar sind und der Standortwechsel einen Hard-Reset auslöst, bleibt die bisherige Liste sichtbar, bis das Ersatzresultat geladen ist. Das vermeidet einen störenden Loading-Blackout beim Gehen. Gleichzeitig darf dieser Hard-Reset nicht den Stable-Merge-Pfad nutzen: der neue Standort muss nach dem finalen Response eigenständig bewertet werden.

## D-029 — Stable-Merge-Toleranz bleibt dichtetakt-sicher

Stable-Merge ist ein Schutz gegen transient unvollständige API-/Add-on-Responses, kein Freibrief zum Verschmelzen nah beieinander liegender realer Abfahrten. Das Matchfenster ist daher explizit auf maximal 5 Minuten begrenzt. Abfahrten derselben Linie/Richtung im Abstand von 6 Minuten oder mehr bleiben getrennte Einträge.

## D-030 — Display-Sortierung ist zentrale Produktlogik

Die fachliche Reihenfolge der Abfahrtsliste lautet: „Hier“ bis einschließlich `HERE_DISTANCE_THRESHOLD_METERS = 35` zuerst, danach effektive Entfernung, Linie und Abfahrtszeit. Diese Logik darf nicht separat im ViewModel, StableMerger oder UI nachgebaut werden. Ab Build 115 ist `DepartureDisplayOrdering` die zentrale Quelle für Comparator und effektive Distanz.

## D-031 — ORS-Skip bei Stable Refresh braucht Vollständigkeitsprüfung

Bei unverändertem Walking-Origin sollen vorhandene ORS-Metriken weiterverwendet werden, aber ORS darf nicht pauschal übersprungen werden, sobald irgendeine Metrik vorhanden ist. Maßgeblich ist, ob aktuelle relevante Stops im Radius bereits eine echte Geh-/Fahrradmetrik oder einen bewussten Approx-Fallback besitzen. Fehlen diese Metriken, wird ORS erneut angestoßen, damit Luftliniengewinner später durch echte Walkdistance ersetzt werden können.

## D-032 — Detailsheet-Folgezeiten nutzen lokale Haltestellencluster

Folgeabfahrten im Detailsheet dürfen nicht ausschließlich an exakte Provider-Stop-/Plattformstrings gebunden sein. Für den Nutzer zählt die gewinnende lokale Haltestelle. Daher gilt: gleiche Linie, gleiche Richtung und gleicher Mode sind Pflicht; zusätzlich muss entweder die normalisierte Haltestelle übereinstimmen oder die effektive Entfernung sehr nahe am ausgewählten Eintrag liegen. Klar weiter entfernte Haltestellen derselben Linie/Richtung bleiben ausgeschlossen. Die Hauptliste bleibt weiterhin über `maxPerDirection` ruhig.


## D-027 — Data catchment is wider than the visible departure window

The user-selected window controls what the overview displays, not necessarily what the app fetches. Build 118 uses a 120-minute raw data catchment so the detail sheet can show the next departures without triggering additional detail API requests.

## D-028 — Nearby stop coverage beats missing-stop-only enrichment

A station can be incomplete even when it has at least one departure in the initial response. Direct stop add-ons therefore cover all bounded nearby station IDs, not only stations with zero departures. The UI still filters and deduplicates locally.


## D-031 — Stop-ID ist Rohdatenwert, kein UI-Haltestellenname

Wenn die API bei direkten `stops=`-Batches im Feld `Departure.stop` eine Provider-/GTFS-ID liefert, behandelt die App diesen Wert als Roh-ID. Vor Anzeige, Sortierung, Dedup, Follow-up-Zeiten und ORS-Zuordnung wird gegen `Station.id` aufgelöst und auf den lesbaren `Station.name` normalisiert. Nur wenn keine Station bekannt ist, bleibt der Rohwert als Debug-Indikator erhalten.


## D-033 — Provider-Stop-IDs werden über exakte ID und Basis-ID aufgelöst

Provider können Abfahrten mit Plattform-IDs wie `de:11000:900011201::6` liefern, während die Stationsliste nur die Basis-ID `de:11000:900011201` oder umgekehrt enthält. Für `Departure.stop` gilt deshalb: Roh-IDs sind keine UI-Namen. Das Repository indexiert Stations-IDs für Enrichment über exakte ID und Basis-ID vor `::`. `stationId=de:...` bleibt als technischer ORS-/RoutePreview-Schlüssel erlaubt; `stop=de:...` in sicht-/matchingrelevanten Logs ist ein Fehlerindikator.

## D-034 — Geschlossen-Status braucht kausalen Diff oder explizites Duplikat

Ein Finding darf nicht allein durch Dokumentation als geschlossen gelten. Für jeden Closed-Eintrag muss es entweder einen kausalen Code-/Test-Diff im jeweiligen Build geben oder eine klare Kennzeichnung als Duplikat/Superseded eines früheren Fixes. QA-Fokus und Release-Checkliste müssen das beobachtete Symptom erneut prüfen.


## D-035 — Basis-ID-Fallback darf nicht über Stationsnamen hinweg raten

Provider-Stop-IDs mit Plattformsuffix dürfen auf die Basis-ID vor `::` zurückfallen, aber nur konservativ. Wenn eine Basis-ID mehrere Stationskandidaten mit unterschiedlichem normalisiertem Stationsnamen liefert, darf die App nicht allein anhand der Entfernung entscheiden. Eine falsche, plausible Haltestelle ist gefährlicher als eine sichtbare Unaufgelöst-Diagnose. Distanz ist daher nur noch Tiebreaker innerhalb desselben normalisierten Stationsnamens.

## D-036 — Provider-ID-Suffix als möglicher Plattformhinweis bleibt offenes Finding

Das Suffix nach `::` in Provider-IDs wirkt in den beobachteten VBB-Daten wie ein Gleis-/Steig-/Plattformhinweis. Diese Information kann für künftige Coverage- und Dedup-Logik wertvoll sein, insbesondere an größeren Haltestellen mit mehreren Steigen. Bis die Semantik ausreichend verifiziert ist, wird sie nicht hart als Produktlogik verwendet.


## D-037 — Stop-ID-Auflösung wird nach dem Response-Merge wiederholt

Ein `stops=`-Batch kann weniger Stationsmetadaten enthalten als der später gemergte Response. Provider-Stop-IDs werden deshalb zunächst best-effort im Repository aufgelöst und nach dem Stable-Response-/Stations-Merge genau einmal erneut gegen die vollständige Stationsliste geprüft. Erst danach folgen Distanzanreicherung, Filter, Dedup und UI.

## D-038 — Technischer Stop-Point und UI-Haltestellenname sind getrennte Daten

Wenn `Departure.stop` eine Provider-ID enthält, wird diese als `providerStopId` erhalten. Nach erfolgreicher Auflösung enthält `Departure.stop` den lesbaren Stationsnamen. Dadurch kann die UI sauber bleiben, während Routing und das offene Plattform-/Steig-Finding weiterhin auf den exakten technischen Stop-Point zugreifen können.

## D-039 — Linie+Richtung hat eine zentrale normalisierte Identität

Trailing Provider-Ortskontext wie `(Berlin)` beschreibt keine neue Fahrtrichtung. Dedup, Minuten-Eindeutigkeit, `maxPerDirection`, Stable-Merge und Detailsheet-Folgezeiten verwenden `DepartureServiceIdentity`. Die Normalisierung entfernt nur solchen Provider-Kontext und darf echte Gegenrichtungen nicht zusammenführen.

## D-040 — 20-m-Nearest-Toleranz ist eine bewusste technische Stabilitätstoleranz

Die in `DepartureViewModel.applyFilters()` verwendete Grenze `nearestDist + 20 m` ist **keine Erweiterung des eingestellten Suchradius** und kein zusätzliches fachliches Stationsfenster. Sie ist eine bewusst eingeführte Toleranz gegen GPS-Ungenauigkeit, leicht abweichende Provider-Stationspunkte und Plattform-/Steig-Geometrien derselben fachlichen Haltestellensituation.

Regel:
- Der Nutzer-Radius bleibt die harte äußere Grenze.
- Pro Linie+Richtung wird zunächst die beste effektive Distanz ermittelt.
- Kandidaten bis maximal 20 m darüber dürfen als technisch gleichwertiger Nahbereich weiter berücksichtigt werden.
- Die Toleranz darf keine Gegenrichtungen zusammenführen und darf nicht als allgemeines Clustering verschiedener Haltestellen missverstanden werden.
- Eine Änderung der 20 m benötigt neue Runtime-Evidence; der Wert wird nicht auf Verdacht optimiert.

## D-041 — Provider-Rohwerte bleiben erhalten; Cleanup und Service-Identität sind abgeleitete Sichten

Providerdaten werden nicht destruktiv für die Darstellung umgeschrieben. Rohwerte bleiben die technische Ausgangsbasis. Anzeige- und Matchingformen werden daraus abgeleitet.

Für Linie+Richtung bedeutet das konkret: `Departure.direction` bleibt als Providerwert erhalten. `DepartureServiceIdentity` bildet daraus für Dedup, Stable-Merge, `maxPerDirection` und Folgezeiten einen konservativen Schlüssel und entfernt nur nicht-semantischen trailing Provider-Ortskontext wie `(Berlin)`. Die UI darf für die Darstellung separat bereinigte Namen verwenden.

Damit gilt zugleich: „Dedup auf Basis der API-Informationen“ bedeutet **Rohdaten als Quelle, kanonische Identität als abgeleitete technische Sicht** — nicht bytegenauer Stringvergleich und nicht Mutation der Providerdaten.

## D-042 — Aktualisierte abfahrt.now-OpenAPI ist primäre externe Vertragsreferenz

Für die App-Integration wird die im Projekt hinterlegte aktuelle OpenAPI 3.1 als primäre externe Vertragsreferenz verwendet. Historische PDF-/Webbeispiele sind Evidence, aber keine gleichrangige Vertragsquelle.

Die aktuelle OpenAPI bestätigt für `/departures` insbesondere:
- `timestamp` als Unix-Zeit in **Millisekunden**,
- `stop` als Haltestellen-/Stationsname,
- `stations[].distance` als Luftlinienentfernung,
- optional `stations[].walkSeconds` als OSM-basierte Gehzeit.

Build 122 erzeugt sein internes `Departure.stationDistance` beim Stations-Enrichment aus `Station.distance`; `stationDistance` muss deshalb nicht als eigenes Departure-Feld im externen Schema existieren. Neue OpenAPI-Fähigkeiten wie `walkSeconds`, `/journey`, `regionBounds` und `routingBounds` werden nicht automatisch Produktlogik, sondern separat bewertet.
## D-043 — ORS verwendet ab Build 123 ausschließlich den HEIGIT-Servicepfad

Die Retrofit-Basis für OpenRouteService lautet ab Build 123 verbindlich:

`https://api.heigit.org/openrouteservice/`

Matrix und Directions behalten ihre vorhandenen relativen `v2/...`-Pfade. Dadurch ändert sich nur der offiziell migrierte Host-/Servicepräfix, nicht die fachliche ORS-Integration.

Der deprecated Host `api.openrouteservice.org` darf nicht als Runtime-Fallback erhalten werden: Ein stiller Fallback würde die angekündigte Abschaltung lediglich verdecken und zwei Netzwerkpfade schaffen. Fehler am neuen Endpoint werden daher über die vorhandene Fehler-/Fallbacklogik behandelt.

## D-044 — Provider-Stop-ID-Erkennung folgt primär dem API-Zusammenhang, nicht einem Stringformat

Ein technischer Stop-Identifier muss nicht Doppelpunkte oder ein bestimmtes Präfix enthalten. Wenn `Departure.stop` **exakt** einer `Station.id` aus derselben bekannten Stationsmenge entspricht, wird der Wert als Provider-Stop-ID behandelt, unabhängig davon, ob er z. B. `de:11000:...` oder rein numerisch wie `900009173` ist.

Regel:
- Exakter `Departure.stop == Station.id`-Match ist ein belastbarer ID-Nachweis.
- Der sichtbare Wert wird auf `Station.name` aufgelöst.
- Der ursprüngliche technische Wert bleibt als `providerStopId` erhalten.
- Die bestehende Formatheuristik für strukturierte IDs bleibt als Fallback für Fälle ohne vollständige Stationsliste bestehen.
- Rein numerische Strings werden **nicht pauschal** als IDs klassifiziert. Ohne exakten `Station.id`-Match bleiben sie unangetastet.
- Derselbe exakte Match wird im begrenzten Post-Merge-Enrichment verwendet, damit ein im Einzelbatch ungelöster numerischer Stop später gegen die vollständige gemergte Stationsliste aufgelöst werden kann.


## D-045 — API-Keys werden mit Android Keystore + AES-256/GCM at rest geschützt

Ab Build 125 werden der abfahrt.now- und der ORS-API-Key nicht mehr als Klartext persistiert. `UserPreferencesRepository` behält DataStore als einzige Preference-Ablage; die beiden Secret-Werte werden vor dem Schreiben durch `ApiKeyCipher` verschlüsselt.

Verbindliche Regeln:
- Kryptografischer Schlüssel: AES 256 Bit, im Provider `AndroidKeyStore`, Zweck ausschließlich Encrypt/Decrypt.
- Cipher: `AES/GCM/NoPadding` mit zufälligem IV; gespeicherte Werte tragen das versionierte Präfix `enc:v1:`.
- Keine neue Security-Library und keine parallele Secret-Datenbank.
- Vor Build 125 vorhandene Klartextwerte werden beim ersten Repository-Zugriff best-effort in place migriert; erst nach erfolgreicher Verschlüsselung wird der Altwert ersetzt.
- Scheitert die Migration temporär, bleibt der vorhandene Klartextwert funktionsfähig und wird bei einem späteren Zugriff erneut migriert; ein Key darf durch die Migration nicht verloren gehen.
- Neue/aktualisierte Keys werden bei Verschlüsselungsfehler nicht als Klartext zurückgeschrieben.
- Kryptografische Arbeit läuft außerhalb des Main Threads.
- Die DataStore-Datei bleibt von Cloud-Backup und Device-Transfer ausgeschlossen, weil der Keystore-Schlüssel gerätegebunden ist.
- Logausgaben dürfen nur Status/Fehlerklasse nennen, niemals Key oder Ciphertext.

Die UI und Netzwerkverträge ändern sich nicht: Konsumenten erhalten den entschlüsselten Wert nur im laufenden Prozess.


## D-053 — Android 17 / API 37 ist ab Build 132 Produktbaseline

Build 132 hat `compileSdk = 37` und `targetSdk = 37` mit realem Debug-Build und API-37-Runtime-Evidence bestätigt. Erfolgreiche HTTPS-Aufrufe gegen abfahrt.now, Photon und HEIGIT ORS sowie `RoutePreview state=Ready` belegen den Kernflow unter den Target-37-Verhaltensschaltern. Es werden weder `ACCESS_LOCAL_NETWORK` noch Certificate-Transparency-Ausnahmen vorsorglich hinzugefügt.

Die breitere Phone-/Tablet-/Foldable-Matrix sowie Release/R8 bleiben reguläre Release-/Kompatibilitätsgates und werden nur bei konkretem Finding zum Migrationsblocker; sie sind kein Grund, die funktional nachgewiesene Target-37-Migration offen zu halten.

## D-054 — Kotlin-Warning-Hygiene ändert keine Fachlogik

Die mit Kotlin 2.3 sichtbaren Warnings werden isoliert bereinigt: Qualifier-Ziele werden explizit als `@param:` angegeben und nur vom Compiler als redundant erkannte Nullability-Operatoren entfernt. Keine globale Compileroption wird aktiviert und keine Datenfluss-/Fallback-/Filterlogik umgebaut.

## D-055 — `Station.walkSeconds` wird zuerst erfasst und beobachtet, nicht konsumiert

Die aktuelle abfahrt.now-OpenAPI beschreibt `stations[].walkSeconds` als optionale providerseitige OSM-Gehzeit. Ab Build 134 bildet das bestehende `Station`-Modell dieses Feld nullable ab und die Repository-Diagnostik misst seine reale Abdeckung. Das Feld darf in Build 134 **keine** Anzeige-, Reachability-, Sortier-, Dedup-, ORS-Skip- oder RoutePreview-Entscheidung beeinflussen.

Erst reale API-/Runtime-Evidence darf eine spätere Produktentscheidung auslösen. Hintergrund: `walkSeconds` kann ORS-Matrix-Last potenziell reduzieren, ersetzt aber keine Gehwegdistanz, keine Bike-Zeit und keine Routengeometrie.



## D-056 — `walkSeconds` ist belegt, ersetzt ORS aber noch nicht; lokale Diagnostics dürfen Tests nicht brechen

Build 134 lieferte zwei aufeinanderfolgende reale `/departures`-Samples mit `walkSeconds coverage=40/40`, `absent=0`, `nonPositive=0`. Das belegt die Feldverfügbarkeit für den getesteten Standort/Providerpfad und rechtfertigt eine spätere gezielte Nutzungsevaluierung. Es ist **kein** globaler Beleg für alle Regionen und ersetzt weder ORS-Gehwegdistanz noch Bike-Zeit noch Routengeometrie.

Der kombinierte lokale Gradle-Gate zeigte außerdem drei bestehende Repository-Tests, die ausschließlich an `android.util.Log.d/w` des Android-JVM-Stubs scheiterten. Build 135 behandelt diese reinen Diagnostics deshalb als best-effort: Loggingfehler dürfen die deterministische Stop-ID-Enrichment-Logik nicht beeinflussen. Keine neue Mock-/Robolectric-Dependency wird eingeführt.


## D-057 — abfahrt.now-Key ist ab Build 136 verpflichtende Zugangsvoraussetzung

abfahrt.now ist nicht mehr als keyloser App-Pfad zu behandeln. Ab Build 136 gilt:
- Erststart und bestehende keylose Installationen müssen vor dem Departure-Hauptflow einen nichtleeren persönlichen abfahrt.now-Key hinterlegen.
- „Weiter ohne Schlüssel“ entfällt vollständig; ORS bleibt optional.
- `onboardingCompleted` allein reicht nicht: Access benötigt zusätzlich einen aktuell entschlüsselbaren, nichtleeren abfahrt.now-Key. Onboarding wird erst nach erfolgreicher verschlüsselter Speicherung des Pflicht-Keys abgeschlossen; Navigation folgt dem Access-State statt einem vorauseilenden UI-Klick.
- Der verpflichtende Key kann in Settings ersetzt, aber nicht gelöscht werden.
- HTTP 401 aus dem abfahrt.now-Kernflow führt zurück zur Key-Eingabe; der vorhandene Wert wird zur Korrektur vorbefüllt und nicht automatisch verworfen.
- Die Keystore/AES-256-GCM-Sicherheitsarchitektur aus D-045 bleibt unverändert.

## D-058 — Route planen startet direkt im bestehenden Startseiten-Suchfeld

Der Nutzer soll für die Routenplanung keinen zusätzlichen Start-Button antippen müssen. Ab Build 138 trägt das vorhandene Startseiten-Suchfeld den Zweck **„Route planen – Ziel eingeben“**. Ein ausgewählter Photon-Treffer öffnet direkt den RoutePlanner; `Von` ist standardmäßig der aktuelle Standort, `Nach` der gewählte Treffer. Beide Punkte sind änder-/tauschbar, die Verbindung wird explizit über „Route finden“ geladen.

Die bisherige Photon-Funktion „Abfahrten an anderem Ort“ bleibt erhalten, wird aber ab Build 137 auf eine eigene Seite hinter dem Startseiten-Menü verschoben. Transitverbindungen kommen aus `/trips`; ORS darf keine erfundene Transitgeometrie zeichnen.

## D-059 — Alternativstandort-Abfahrten sind ab Build 137 ein eigener Nebenflow

Die bisherige Stations-/Haltestellensuche für Abfahrten an einem anderen Ort bleibt fachlich unverändert, ist aber nicht mehr Teil der Startseiten-Semantik. Sie liegt hinter dem Startseiten-Menü in der geschützten Route `alternate_departures`. Einstellungen wandern in dasselbe Overflow-Menü; Refresh bleibt direkt erreichbar.

Beim Verlassen des Nebenflows muss der gemeinsame `DepartureViewModel` deterministisch auf `SearchTarget.CurrentLocation` und `DepartureUiState.Idle` zurückgesetzt werden. Dadurch kann die Startseite frisch vom realen Standort laden, ohne kurz Ergebnisse des alternativen Zielorts anzuzeigen. Der Startseiten-Suchplatz wird erst in Build 138 mit der vollständigen Routingsemantik „Route planen – Ziel eingeben“ wieder belegt; Build 137 baut bewusst keinen halbfertigen Planner.


## D-060 — RoutePlanner bleibt fachlich auf Photon + `/trips` begrenzt

Build 138 trennt die Route-planen-UI als eigenen `RoutePlannerViewModel`, ohne eine neue Datenarchitektur einzuführen. Allgemeine Orte/Adressen/POIs werden über das bestehende Photon-Backend geokodiert; Transitverbindungen stammen ausschließlich aus `TransitRepository.getTrips()` bzw. abfahrt.now `/trips`.

Verbindliche Grenzen:
- Startseiten-Zielsuche öffnet den Planner direkt; kein zusätzlicher Start-Button.
- `Von` ist standardmäßig der zuletzt für die Startseite bekannte aktuelle Standort; fehlt er, muss der Nutzer einen Startpunkt auswählen.
- `Von` und `Nach` sind änderbar und tauschbar.
- `/trips` wird erst nach explizitem „Route finden“ aufgerufen, nicht bei jeder Eingabe.
- `sameVehicle=true` wird als Durchbindung ohne Umstieg dargestellt.
- ORS wird nicht zur Erzeugung einer vermeintlichen Transitlinien-Geometrie verwendet.
- Neue Nutzertexte müssen im selben Build in allen 22 Resource-Sets vorhanden sein; Locale-Key-Parität bleibt Pflichtgate.


## D-061 — Routendetails klappen inline auf; keine erfundene Transitkarte

Build 140 nutzt die bereits vertraglich vorhandenen `intermediateStops`/`stopNames` direkt im RoutePlanner. Eine Zwischenhalteanzahl ist nur dann aufklappbar, wenn tatsächlich Namen vorliegen; andernfalls bleibt sie rein informativ. Eine Umstiegsdauer wird nur zwischen zwei Nicht-Geh-Transit-Legs angezeigt und bei `sameVehicle` unterdrückt. Dadurch widersprechen Zugangs-/Abgangs-Fußwege nicht dem API-Feld `changes`.

Solange die API keine belastbare Transitgeometrie liefert, zeichnet die App keine synthetische Bus-/Bahn-Polyline. Karteninspirierte Hierarchie, Linien-Badges und Inline-Details sind zulässig; erfundene Transitgeometrie nicht.

## D-062 — Empty/Error/Coverage bleibt ehrlich und retry-fähig

Build 141 behandelt leere und fehlgeschlagene `/trips`-Antworten als explizite UI-Zustände mit Wiederholen-Aktion. Die App leitet aus einem leeren Ergebnis oder HTTP 400 **keine** behauptete „außerhalb der Abdeckung“-Diagnose ab, solange der API-Vertrag das nicht eindeutig liefert.

- ein leerer `trips`-Array bleibt ein gültiger 200-Erfolg ohne Verbindung;
- der vom Provider gelieferte `region`-Wert wurde in Build 141 zunächst im Empty-State sichtbar gehalten; die sichtbare Darstellung ist ab Build 142 durch D-063 superseded;
- `AbfahrtTrips` unterscheidet in Debug-Logs `success`, `empty` und `error`, ohne gesuchte Ortsnamen zu loggen;
- optionale `attribution` aus `TripResponse` wird angezeigt, wenn geliefert;
- Mehrregionen-/Coverage-Logik folgt erst auf reale Evidence bzw. einen eindeutigeren API-Vertrag.

## D-063 — Provider-Region ist Routing-Kontext, keine sichtbare Nutzerinformation

Ab Build 142 bleibt `TripResponse.region` Bestandteil des API-Modells und darf für Diagnostik/Coverage-Kontext verwendet werden, wird aber im RoutePlanner nicht mehr als eigener UI-Text angezeigt. Der Nutzer erhält durch eine generische Regionsbezeichnung an dieser Stelle keinen handlungsrelevanten Mehrwert. Optionale Provider-Attribution bleibt davon unberührt und wird weiterhin angezeigt, wenn geliefert.

Die Routendarstellung verwendet für Nicht-Geh-Legs dieselben `TransportMode.emoji`-Symbole wie die Abfahrtskarten. Die Symbolik wird aus dem API-Modus bzw. der bestehenden Linieninferenz abgeleitet; es entsteht keine zweite Mapping-Logik.



## D-064 — Zuhause/Arbeit sind zwei lokale Convenience-Slots, keine Favoritenplattform

Ab Build 143 unterstützt die App genau zwei benutzerverwaltete gespeicherte Orte: **Zuhause** und **Arbeit**. Sie werden über die bestehende allgemeine Photon-Suche gewählt und lokal als Titel, Untertitel, Latitude und Longitude im bestehenden DataStore gespeichert. Es entsteht bewusst keine generische Favoriten-/History-/Account-Synchronisationsarchitektur.

- beide Slots sind optional, änder- und löschbar;
- konfigurierte Slots stehen im RoutePlanner für `Von` und `Nach` als Schnellwahl zur Verfügung;
- die gespeicherten Koordinaten ändern nur die Endpoint-Auswahl, nicht `/trips`-Semantik oder Ranking;
- der DataStore bleibt durch die bestehenden `data_extraction_rules` von Cloud-Backup und Device-Transfer ausgeschlossen;
- keine neue Dependency oder zweite Preferences-Welt.

Die Route-Leg-Darstellung übernimmt zugleich die bereits bewährte visuelle Grammatik der Hauptansicht: Modus-Emoji über Linienbadge in einer festen linken Spalte. Dadurch beginnt die Richtungsinformation unabhängig von Emoji-/Linienbreite an einer stabilen horizontalen Position.


## D-065 — Gespeicherte Orte sind auch direkte Startseiten-Ziele

Ab Build 144 werden konfigurierte Slots **Zuhause** und **Arbeit** nicht nur innerhalb des RoutePlanners angeboten, sondern bereits beim Fokus des leeren Startseitenfelds `Route planen – Ziel eingeben`. Das spart den Umweg über eine erneute Photon-Suche und erfüllt den häufigen Flow „vom aktuellen Ort nach Hause/zur Arbeit“.

- sichtbar sind nur tatsächlich konfigurierte Slots;
- Auswahl setzt den bestehenden Planner auf `Von = aktueller Standort` und `Nach = gespeicherter Ort`;
- gespeicherte Koordinaten werden direkt verwendet, ohne neuen Photon-Request;
- sobald Text eingegeben wird, übernimmt unverändert die allgemeine Photon-Zielsuche;
- keine generische Favoriten-/History-Architektur wird eingeführt; D-064 bleibt gültig;
- die ursprünglich für Build 144 geplante lokale Routensortierung wird zugunsten dieses expliziten UX-Follow-ups auf Build 145 verschoben.


## D-066 — Routensortierung bleibt lokal und transparent

Build 145 sortiert ausschließlich die bereits von `/trips` gelieferten Alternativen. Es gibt keine zusätzliche Serveranfrage und kein verborgenes Re-Ranking des Providers. Standard bleibt **Früheste**, also die nächstmögliche Abfahrt. Weitere Modi sind **Schnellste**, **Wenig Umstiege** und **Wenig Fußweg**.

- Früheste: Abfahrt → Ankunft → Dauer → Umstiege.
- Schnellste: Gesamtdauer → Abfahrt → Umstiege → Ankunft.
- Wenig Umstiege: Umstiege → Gesamtdauer → Abfahrt → Ankunft.
- Wenig Fußweg: Summe der Walking-Leg-Zeiten → Gesamtdauer → Abfahrt → Umstiege.
- Bei vollständigem Gleichstand bleibt die ursprüngliche Provider-Reihenfolge als letzter Tie-Breaker erhalten.

`/trips` liefert aktuell für Walking-Legs Zeitstempel, aber keine Gehstrecke. Deshalb heißt der Modus bewusst „Wenig Fußweg“ und nutzt die verfügbare Gehzeit als Proxy; die App behauptet keine metrisch kürzeste Gehstrecke.

Die Route-Leg-Köpfe behalten die bereits etablierte Symbol-über-Linienbadge-Grammatik, richten Richtung bzw. Gehzeit aber an der Badge-Basislinie aus. Transit-spezifische Verspätungs-/Gleisinformationen werden bei Walking-Legs nicht angezeigt.


## D-067 — Lokale Suche ist City-default, aber explizit überschreibbar; Walking-Navigation bleibt Intent-basiert

**Suchregel superseded durch D-069:** Die Build-146-Regel war für Fernort-/Stationssuchen zu restriktiv. Walking-Navigation aus D-067 bleibt unverändert gültig.

Build 146 verwendet für allgemeine Photon-Zielsuchen zusätzlich zur bestehenden Lat/Lon-Nähe den aktuell von abfahrt.now gelieferten `DepartureResponse.city`-Wert. Der City-Scope wird nur ergänzt, wenn die Eingabe **keine 4–6-stellige Postleitzahl**, **keine explizite Komma-Ortsangabe** und nicht bereits den aktuellen Stadtnamen enthält. Damit liefert eine unqualifizierte Suche in Berlin standardmäßig Berliner Treffer, während `28195 ...` oder `Hauptbahnhof, Bremen` weiterhin bewusst andere Orte zulässt. Es entsteht kein zusätzlicher Reverse-Geocoding-Call.

Gespeicherte Orte sind reine Empty-Field-Shortcuts und werden beim ersten eingegebenen Zeichen ausgeblendet. Für den ersten und letzten Walking-Leg darf die UI per implizitem Android-Intent eine Geh-Navigation zum Leg-Ziel öffnen; falls kein Navigation-Handler vorhanden ist, fällt sie auf einen normalen Karten-Intent zurück. Die App pinnt keine externe Navigations-App und fügt keine Navigation-SDK-Abhängigkeit hinzu.

Die Route-Leg-Darstellung verwendet eine feste linke Modus-/Zeitspalte: Symbol und Linienbadge stehen oben, Abfahrts-/Ankunftszeiten werden darunter auf derselben Spaltenachse zentriert. Inhalt, Marker und Detailzeilen beginnen konsistent rechts davon.


## D-068 — Photon-City-Scope ist ein weicher Default mit einfachen Fernort-/Transit-Escape-Hatches

**Superseded durch D-069:** Build-147-Runtime zeigte mit `Bad Saarow -> Bad Saarow, Berlin`, dass auch ein weicher City-Default mit Escape-Hatches nicht robust genug ist.

Build 147 korrigiert die in Build 146 zu harte City-Anreicherung, ohne die lokale Suchpriorisierung wieder vollständig zu entfernen. Reale Runtime-Evidence zeigte, dass `Potsdam` als `Potsdam, Berlin` und `S Potsdam` als `S Potsdam, Berlin` an Photon gesendet wurden. Dadurch waren valide Fernort- und Stationssuchen praktisch blockiert.

Verbindliche Regel:
- PLZ oder explizite Komma-Ortsangabe bleiben unverändert global.
- Eingaben, die die aktuelle City bereits enthalten, bleiben unverändert.
- Einwortige Ziele bleiben unverändert; damit sind Städtenamen wie `Potsdam` wieder direkt suchbar. Die bestehende Lat/Lon-Nähe bleibt als Photon-Bias erhalten.
- Explizite Transit-Qualifier `S`, `U`, `S+U`, `U+S`, `Bf`, `Bhf`, `Hbf`, `Bahnhof` heben den City-Default auf, unabhängig davon, ob sie vor oder nach dem Ortsnamen stehen.
- Nur mehrwortige, unqualifizierte Ziele erhalten weiterhin `, <current city>` als lokalen Default.
- `Hauptbahnhof` bleibt ebenfalls global, weil einwortige Ziele grundsätzlich nicht mit der aktuellen City überschrieben werden; die bestehende Photon-Lat/Lon-Nähe priorisiert weiterhin lokale Treffer.

Die Korrektur erzeugt keinen zweiten Photon-Call, keine neue Dependency und keine neue Ranking-Architektur.

## D-069 — Allgemeine Photon-Zielsuche nutzt Raw Query + Location Bias; keine Client-Heuristik

Build 148 beendet die clientseitige Ortsklassifikation für die allgemeine Routen-/Zielsuche. Der Client darf nicht mehr aus Wortanzahl, PLZ, `S`/`U`/`Bhf`, Kommas oder der aktuellen `DepartureResponse.city` ableiten, welchen Ort der Nutzer gemeint haben könnte.

Verbindliche Regel:
- Photon erhält `q` als getrimmten Nutzereingabetext, ansonsten semantisch unverändert.
- vorhandene aktuelle Koordinaten werden weiterhin als `lat`/`lon` Location Bias übergeben; sie sind Priorisierung, keine harte Gebietseinschränkung.
- die Reihenfolge der Photon-Features bleibt in der allgemeinen Suche erhalten; der Client entfernt nur exakte Anzeige-Duplikate und begrenzt die sichtbare Trefferzahl.
- kein zweiter Photon-Call, kein Fallback mit automatisch angehängter City und keine neue Such-Dependency.
- Debug-Evidence protokolliert Raw Query sowie die ersten fünf Photon-Kandidaten mit Name/City/State/OSM-Typ, damit Ranking-Probleme anhand realer Antworten entschieden werden können.
- der separate Flow „Abfahrten an anderem Ort“ ist **nicht** Teil dieser Entscheidung; dessen stationsspezifische Filter-/Rankinglogik bleibt unverändert.

Damit werden `Bad Saarow`, `Brandenburger Tor`, `Potsdam`, `S Potsdam`, `Hauptbahnhof`, PLZ- und `Ort, Stadt`-Eingaben ohne clientseitige Bedeutungsänderung an Photon gesendet. Änderungen an Photons Bias-Stärke oder ein eigenes Re-Ranking benötigen neue Runtime-Evidence und eine separate Entscheidung.


## D-070 — HERE ist ein Standortzustand, kein Routingfall

Ab Build 149 wird die bereits bestehende HERE-Semantik (`stationDistance` 1–35 m) im Detailsheet konsequent bis zur Karte geführt. `Hier` bedeutet, dass für diese Darstellung keine ORS-Walking-/Bike-Route berechnet oder gezeichnet wird.

Verbindlich:
- `Departure.isHereOverride()` bleibt die einzige HERE-Entscheidung; kein zweiter Schwellwert im UI.
- HERE short-circuited den RoutePreview-Loader, auch wenn ein ORS-Key vorhanden ist.
- die Karte darf bei verfügbaren Koordinaten ohne ORS-Key angezeigt werden und enthält nur Query-Origin + Haltestellenmarker; keine Route-/Polyline-Quelle.
- für Abfahrten an einem explizit gewählten Alternativort ist der Query-Origin dieses Orts maßgeblich, nicht der physische Geräte-Standort.
- Nicht-HERE-Routenvorschau, ORS-Anreicherung und externe Kartenaktion bleiben unverändert.
- AB-018 Startup/Main-Thread-Performance wird nicht in Build 149 vermischt; erst messen/instrumentieren, dann optimieren.

## D-071 — Update-Metadaten sind ein separater anonymer Komfortpfad

Ab Build 150 ist der GitHub-Release-Check bewusst **nicht** Teil der Transit-/Routing- oder Credential-Architektur. Er verwendet einen separaten anonymen Client ausschließlich gegen `GET /repos/K-Cosmo/abfahrt-now-android/releases/latest`.

Verbindlich:
- kein GitHub-Token;
- keine Wiederverwendung des abfahrt.now-Clients oder `ApiKeyInterceptor`;
- keine Übertragung von abfahrt.now-/ORS-Keys, Standort-, Such- oder Transitdaten;
- nur Tags im Schema `v<semver>-b<build>` werden ausgewertet; die monotone Buildnummer entscheidet;
- Fehler/Offline/404/Rate-Limit blockieren Startup und Kernfunktion nicht;
- nach Nutzeraktion wird ausschließlich die feste Release-Seite des Community-Repositories geöffnet;
- kein eigener APK-Downloader und kein stiller Installer.

## D-072 — Runtime-Identität trennt Community-App und API-Anbieter sichtbar

Ab Build 151 muss die App in der sichtbaren Runtime klar als unabhängig entwickelte, inoffizielle Community-App erkennbar sein. abfahrt.now ist Daten-/API-Anbieter, nicht App-Betreiber oder Projektinhaber.

Verbindlich:
- Community-Projektlink zeigt auf `K-Cosmo/abfahrt-now-android`;
- externe Privacy-/Terms-Links werden als API-Provider-Links bezeichnet;
- die Abgrenzung muss in allen ausgelieferten UI-Sprachen vorhanden sein;
- Provider-/Markenhinweise dürfen nicht den Eindruck einer offiziellen Zugehörigkeit erzeugen.

## D-073 — Der normale Android-CI-Gate enthält Release/R8

Ab Build 151 umfasst der Standard-Gate auf Pull Requests dauerhaft:

`./gradlew :app:testDebugUnitTest :app:assembleDebug :app:assembleRelease --no-daemon`

Damit werden Unit-Tests, Debug-Build und minifizierter Release/R8-Build auf demselben Commit geprüft. Ein grüner CI-Gate bleibt trotzdem **nur ein technischer Gate**: sichtbare UX, reale Netzwerk-/Gerätepfade und andere definierte Runtime-Evidence werden dadurch nicht automatisch akzeptiert.
