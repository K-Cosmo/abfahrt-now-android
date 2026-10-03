# Build 123 Plan

1. Offizielle ORS-Migrationsangabe gegen aktuelle Quelle verifizieren.
2. Bestehende Retrofit-Base-URL und relative ORS-Pfade prüfen.
3. Ausschließlich die Base-URL auf `https://api.heigit.org/openrouteservice/` umstellen.
4. `versionCode` auf 1230 erhöhen.
5. Statischen Regression-Check für Alt-/Neu-Endpoint ausführen.
6. Bestehende DOC-/Locale-/Android-Readiness-Checks ausführen.
7. Diff gegen DOC1.1 prüfen und Evidence erzeugen.
8. `/doc` auf Build 123 konvergieren.
