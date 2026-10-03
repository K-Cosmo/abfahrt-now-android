# Build 150 Plan

1. Keep the existing dependency stack; use Retrofit/OkHttp already present in the app.
2. Add a minimal GitHub Releases API model/service for `releases/latest`.
3. Add a dedicated anonymous GitHub client/Retrofit instance; never reuse `abfahrtClient` or any credential interceptor.
4. Put tag validation and build comparison in a small pure `UpdateReleasePolicy` utility.
5. Add a small `UpdateViewModel` that performs one asynchronous check per Activity lifecycle and exposes only update-available state.
6. Show a compact localized update dialog from the app root; dismiss is non-persistent and the next app start may check again.
7. Build the release URL only from the fixed repository plus a validated tag; do not trust arbitrary remote download/navigation URLs.
8. Add update prompt strings in all 22 locale sets and extend the locale parity gate to include split XML resource files.
9. Add focused unit tests for valid/invalid tags, newer/same/older builds, and GitHub-client credential isolation.
10. Converge `/doc` and BUILD150 process docs without marking runtime acceptance prematurely.
11. Run GitHub Actions on the final converged branch. Runtime E2E evidence follows as a separate acceptance step.
