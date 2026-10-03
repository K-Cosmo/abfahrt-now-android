# BUILD124 Plan

1. Bestehenden Stop-ID-Enrichment-Pfad erweitern, keine neue Parallelarchitektur.
2. Aus vorhandener Stationsmenge Set exakter `Station.id` bilden.
3. `Departure.stop` bei exaktem ID-Match als Provider-ID behandeln.
4. Post-Merge-Gate um denselben exakten Match erweitern.
5. Drei fokussierte Regressionstests ergänzen.
6. Version auf 1240 anheben.
7. Diff und statische Projektchecks ausführen.
8. Runtime-Evidence auf dem reproduzierten U6-Fall einholen.
