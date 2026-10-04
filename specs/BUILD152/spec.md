# Build 152 Specification — UI/UX convergence

## Goal
Reduce avoidable visual friction without changing transit semantics or introducing a second UX architecture.

## Scope
1. Settings footer spacing
   - reduce the excessive whitespace between the ORS section, divider and Community footer;
   - remove the unused historical `AppFooter` implementation so no shadow footer remains in source.
2. First-run onboarding readability
   - body copy is left aligned, never justified;
   - required abfahrt.now key and optional ORS setup are visually separated in a simple readable structure;
   - both onboarding steps remain scrollable on compact displays.
3. RoutePlanner origin/destination header
   - replace the vertically expensive pair of independent outlined fields plus centered swap button with one compact Material-3 container;
   - origin and destination remain separately editable/searchable;
   - swap, Current location, Home/Work, Photon search and existing `/trips` behavior remain unchanged;
   - compact layout must work on narrow phone widths.
4. Startup access gate
   - an already configured user must never see a transient API-key/onboarding screen while preferences are loading;
   - `AccessGateViewModel` is the sole owner of the startup access decision;
   - its nullable state stays unresolved until the first real `UserPreferencesRepository.preferencesFlow` emission;
   - protected feature ViewModels are created only after that access snapshot exists;
   - `DepartureViewModel.preferences` remains feature/settings state and does not select the app start destination.

## Non-goals
- no departure-list redesign in this build;
- no routing contract or result-card changes;
- no Photon ranking change;
- no credential format/storage migration;
- no startup-performance optimization beyond removing the incorrect access-gate state;
- AB-018 instrumentation remains Build 153.

## Acceptance
- Android CI: governance/static/locale gates + unit tests + debug + release/R8 are green;
- real device with configured key cold-starts without visible onboarding/API-key flash;
- RoutePlanner header is visibly more compact while both endpoint searches and swap still work;
- onboarding body has normal left-aligned word spacing on German and remains readable on a compact device;
- footer begins near the ORS divider without the previous stacked spacer gap.
