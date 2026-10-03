# Build 144 plan

1. Start from the exact corrected Build-143 source ZIP supplied by the user.
2. Thread current Home/Work preference values into the Home `DepartureHeader` route-search UI.
3. Add a small saved-destination panel that is shown only while the Home route field is focused and empty.
4. Add a typed callback from `DepartureScreen` to `AppNavHost` for selecting a `SavedPlace`.
5. Reuse the existing RoutePlanner endpoint conversion so the shortcut opens the same planner state as a normal destination selection.
6. Keep typed Photon behavior untouched and run all source/governance/locale gates.
7. Real acceptance: `testDebugUnitTest + assembleDebug`, then Home/Work shortcut + typed Photon smoke.
