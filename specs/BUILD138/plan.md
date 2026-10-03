# Build 138 Plan

1. Aktuelle `/trips`-OpenAPI-Felder im Modell konvergieren.
2. Allgemeine Photon-Zielsuche ergänzen, bestehende Stationssuche unverändert lassen.
3. Route-spezifischen State in einem kleinen `RoutePlannerViewModel` kapseln und bestehende Repository-Schichten wiederverwenden.
4. Startseiten-Suchfeld als Zielsuche wieder einführen; Treffer direkt in `route_planner` übergeben.
5. Planner-UI mit Von/Nach, Tausch und explizitem `/trips`-Call bauen.
6. Trip-/Leg-MVP darstellen; `sameVehicle` korrekt markieren.
7. 22 Locale-Pakete ergänzen und AutoMirrored-Warning aus Build 137 bereinigen.
8. Contract-/Source-Gates ausführen; realer Android-Build/Runtime bleibt Acceptance-Evidence.
