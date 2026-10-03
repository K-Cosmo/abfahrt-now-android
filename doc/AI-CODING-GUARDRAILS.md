Zweck

KI darf in diesem Projekt aktiv zur Code-Erstellung, Analyse, Fehlersuche und Weiterentwicklung eingesetzt werden.
Dabei gilt: KI beschleunigt die Umsetzung, ersetzt aber weder fachliche Plausibilitätsprüfung noch technische Absicherung.

Die Abfahrt-App wird deshalb nach dem Grundsatz entwickelt:

Kleine Änderungen, echte Tests, echte Debug-Daten, klare Fehlerbeschreibungen, keine blinde Magie.

1. Grundprinzipien
1.1 Kleine Schritte statt großer Umbauten

Es werden bevorzugt kleine, isolierte Änderungen umgesetzt.
Keine großflächigen Refactorings oder Komplettumbauten, wenn nicht ausdrücklich gefordert.

Ziel:
- Fehlerquellen eingrenzen
- Wirkung von Änderungen nachvollziehen
- Rückbau vereinfachen
- Debugging beherrschbar halten

1.2 Bestehenden Code ändern statt neu erfinden

KI soll bevorzugt bestehende Strukturen erweitern oder korrigieren, statt parallele Logiken, doppelte Klassen oder neue Architekturen einzuführen.

Ziel:
- keine unnötigen Duplikate
- keine zweite „Schatten-Architektur“
- geringere Komplexität

1.3 Realität schlägt Vermutung

Entscheidungen sollen möglichst auf Basis von:
- echtem Laufzeitverhalten
- Logs
- Debug-Ausgaben
- reproduzierbaren Fehlern
- echten API-Antworten
- echten Nutzungsfällen

getroffen werden, nicht auf Basis von Annahmen.

1.4 Fachliche Einfachheit vor technischer Eleganz

Die App soll für normale Nutzer ohne Erklärung verständlich bleiben.
Technische Lösungen müssen sich der Bedienbarkeit unterordnen.

KIS gilt auch hier:
- keine unnötigen Optionen
- keine versteckte Logik
- keine komplizierten Sonderwege ohne klaren Nutzen

2. Rolle der KI im Projekt
2.1 KI ist Umsetzungshelfer, nicht Wahrheitsquelle

KI-generierter Code gilt immer als Vorschlag, nicht als automatisch korrekte Lösung.

Das bedeutet:
- jede Änderung ist potenziell fehlerhaft
- jede Logik ist potenziell unvollständig
- jede Optimierung ist potenziell nur scheinbar besser

2.2 KI darf nicht sich selbst „beweisen“

Eine KI-Aussage über Code ist kein belastbarer Qualitätsnachweis.
Auch wenn KI Code erzeugt und anschließend selbst „prüft“, bleibt das fehleranfällig.

Deshalb gilt:
- Qualität wird nicht geglaubt, sondern geprüft
- technische Aussagen müssen sich am Verhalten messen lassen
- im Zweifel zählen Logs, API-Verhalten, Build-Ergebnis und reproduzierbare Tests mehr als die Begründung der KI

2.3 KI soll nachvollziehbar arbeiten

Änderungen sollen möglichst so angefordert werden, dass sie:
- konkret
- klein
- dateibezogen
- zielgerichtet
- begründbar

sind.

Bevorzugte Richtung:
- „Ändere nur X“
- „Baue keine neue Dependency ein“
- „Nutze vorhandene Modelle“
- „Füge Logging hinzu“
- „Mach zuerst nur die Analyse“
- „Liefere nur den kleinsten nötigen Fix“

3. Verbindliche Arbeitsweise
3.1 Analyse vor Änderung

Vor größeren Eingriffen soll zuerst geprüft werden:
- Was existiert bereits?
- Was davon ist schon umgesetzt?
- Wo greift die aktuelle Logik?
- Welche Annahmen sind im Code schon vorhanden?
- Welche Logs oder Fehlerbilder liegen vor?

Keine Änderung auf Verdacht, wenn der Ist-Zustand noch nicht verstanden ist.

3.2 Erst beobachten, dann optimieren

Bei Performance-, Caching- oder Nachlade-Themen wird zuerst gemessen oder geloggt:
- Was passiert tatsächlich?
- Wie oft wird geladen?
- Welche API-Aufrufe entstehen?
- Wo ist der Engpass?
- Was bringt die Änderung real?

Optimierungen ohne vorherige Sicht auf das Verhalten sind zu vermeiden.

3.3 Echte Fehlerbeschreibung statt allgemeiner Unzufriedenheit

Fehler sollen möglichst konkret beschrieben werden, zum Beispiel:
- Was wurde erwartet?
- Was ist stattdessen passiert?
- Seit wann?
- In welcher Ansicht?
- Bei welcher Eingabe?
- Mit welchem Log-Auszug?
- Mit welchen echten API-Daten?

Je klarer die Beschreibung, desto zielgenauer die KI-Unterstützung.

3.4 Eine Änderung pro Ziel

Wenn möglich, pro Schleife nur:
- ein Bugfix
- eine Beobachtungsmaßnahme
- eine kleine Optimierung
- eine isolierte Erweiterung

Nicht mehrere Baustellen gleichzeitig vermischen.

4. Qualitätsregeln für KI-generierten Code
4.1 Kein unnötiger Overengineering-Code

Nicht erwünscht sind:
- zusätzliche Abstraktionsschichten ohne Nutzen
- neue Interfaces ohne echten Bedarf
- generische Helferklassen für Einzelfälle
- doppelte Mapper
- parallele Datenmodelle
- komplizierte Workarounds statt klarer Logik

4.2 Keine stillen Architekturwechsel

KI darf nicht ohne ausdrückliche Entscheidung:
- Architekturprinzipien austauschen
- Datenflüsse neu aufbauen
- State-Handling neu erfinden
- Nebenläufigkeit grundlegend umbauen
- bestehende API-Nutzung konzeptionell ändern

4.3 Keine neue Dependency ohne klare Begründung

Neue Libraries nur, wenn:
- ein echter Mehrwert besteht
- vorhandene Mittel nicht ausreichen
- der Nutzen größer ist als zusätzlicher Wartungsaufwand

4.4 Lesbarkeit vor Cleverness

Code soll:
- verständlich
- wartbar
- benennbar
- lokalisierbar
- debugbar

sein.
„Schlau“ wirkende Einzeiler oder unnötig komplexe Konstrukte sind unerwünscht.

4.5 Kein Copy-Paste-Wachstum

Bestehende Logik soll nicht mehrfach leicht abgewandelt kopiert werden.
Wiederholungen sind ein Warnsignal und früh zu bereinigen.

5. Umgang mit Risiken im Projekt
5.1 Fehlerhafter KI-Code

Reaktion:
- kleine Änderungen
- schrittweise Integration
- Build prüfen
- Verhalten prüfen
- Logs prüfen
- echte Daten verwenden

5.2 KI-Feedback-Schleifen

Reaktion:
- KI-Erklärung niemals allein vertrauen
- echte Beobachtung über Theorie stellen
- Widersprüche zwischen Erklärung und Verhalten offen benennen
- bei Unsicherheit Analyse erzwingen statt blindem Umbau folgen

5.3 Ineffizienter oder aufgeblähter Code

Reaktion:
- auf unnötige Klassen, Schleifen, Konvertierungen und Duplikate achten
- nur den kleinsten nötigen Eingriff erlauben
- vorhandene Strukturen nutzen
- bei Performance-Themen zuerst messen/loggen

6. Was für die Abfahrt-App ausdrücklich gut funktioniert

Folgende Arbeitsweise ist ausdrücklich erwünscht und hat sich bewährt:

6.1 Magie der kleinen Schritte

Die App wird iterativ in kleinen, kontrollierbaren Schritten weiterentwickelt.

6.2 Feedbackschleifen mit echten Debug-Daten

Logs, Laufzeitverhalten und echte API-Antworten sind zentrale Entscheidungsgrundlage.

6.3 Klare Fehlerbeschreibungen aus Nutzersicht

Auch ohne tiefes Entwicklerwissen sind präzise Beobachtungen sehr wertvoll, zum Beispiel:
- „Liste lädt nach dem Scrollen doppelt“
- „Sortierung springt nach GPS-Update“
- „Nur Bus ist gewählt, trotzdem erscheinen Tram-Abfahrten“
- „Nach 429 wird nicht sinnvoll abgefangen“
- „Stationen im Radius sind da, aber Abfahrten fehlen“

Gerade diese fachlich sauberen Beschreibungen sind oft wichtiger als technisches Fachvokabular.

6.4 Debugging mit Realität statt Bauchgefühl

Wenn Debug-Daten einer Theorie widersprechen, gewinnt die Realität.

7. Praktische Anweisungen für KI-gestützte Umsetzung

Bei neuen Aufgaben soll nach Möglichkeit so gearbeitet werden:
- Bestehenden Code und aktuellen Stand prüfen
- Problem knapp benennen
- Nur kleinstmögliche Änderung vorschlagen
- Logging oder Nachweis ergänzen, falls unklar
- Ergebnis mit echten Daten prüfen
- Erst danach nächsten Schritt gehen

Bevorzugte Arten von KI-Unterstützung:
- kleine Bugfixes
- Logging/Diagnostik
- gezielte Erweiterungen
- bestehende Logik verständlich umschreiben
- Code vereinfachen
- Testfälle und Plausibilitätsprüfungen ableiten
- API-Verhalten gegen Implementierung spiegeln

Mit Vorsicht zu behandeln:
- Caching
- Nebenläufigkeit
- Fehlerbehandlung
- State-Management
- API-Retry-Logik
- Performance-Optimierungen
- große Refactorings

8. Abnahmekriterium für Änderungen

Eine Änderung gilt nicht als gut, nur weil sie kompiliert oder plausibel klingt.
Sie ist erst dann gut, wenn sie:
- das konkrete Problem adressiert
- keine unnötige Komplexität erzeugt
- mit dem bestehenden Projektstil harmoniert
- sich mit echten Daten nachvollziehen lässt
- keine neuen offensichtlichen Folgeprobleme erzeugt

9. Projekt-Motto für KI-Entwicklung
Nicht groß und glänzend.
Sondern klein, prüfbar, nachvollziehbar und robust.
KI darf schnell sein.
Die Entwicklung bleibt trotzdem kontrolliert.