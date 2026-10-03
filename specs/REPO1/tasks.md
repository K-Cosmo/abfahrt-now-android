# REPO1 tasks

- [x] Public README identifies the app as an independent community project.
- [x] Required/optional API keys and service links are documented.
- [x] 22 bundled UI locales are documented.
- [x] `/docs` legacy tree removed; `/doc` remains the only normative documentation tree.
- [x] EU-first/external-service policy added under `/doc`.
- [x] Agent entry point added without creating new policy.
- [x] Windows/POSIX Gradle wrapper launchers added.
- [x] Gradle distribution checksum pinned.
- [x] `gradle-wrapper.jar` generated with Gradle 9.6.0, locally verified, committed and pushed to `main`.
- [x] Windows fresh-wrapper gate: `gradlew.bat --version` → Gradle 9.6.0; `:app:testDebugUnitTest :app:assembleDebug` → `BUILD SUCCESSFUL`.
- [x] GitHub Actions static + unit/debug-build gate added.
- [x] REPO1 governance/community baseline merged to `main`.
- [x] GitHub Actions run using the committed wrapper directly is green.
- [x] F-DOC1-008 closed after local + direct-wrapper CI evidence.
