# Build 151 Specification — Community identity in runtime UI

## Problem
The public repository and `/doc` define Abfahrt! as an independent community Android app, while the existing settings footer still presents historical Riles Tech attribution and abfahrt.now legal links as if they belonged to the app itself. This conflicts with F-DOC1-015 and blocks the first public community app release.

## Required behavior
- Runtime UI clearly states that Abfahrt! is an independent/unofficial community app and is not affiliated with abfahrt.now.
- abfahrt.now remains clearly attributed as the transit-data/API source.
- The visible project link points to `K-Cosmo/abfahrt-now-android` on GitHub, not to a historical company profile.
- External privacy/terms links are explicitly labeled as API-provider links.
- No statement may imply that this repository develops or operates the abfahrt.now API.
- Existing API feedback/key flows remain provider-specific and unchanged.
- All new visible strings are present in all 22 bundled locale sets.
- No transit, routing, storage, credential, update-check or performance behavior changes.
