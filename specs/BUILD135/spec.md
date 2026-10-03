# Build 135 Specification — JVM Unit-Test Isolation / Test Hygiene

## Goal
Restore the complete local JVM regression gate exposed by Build 134 without changing product behavior.

## Scope
- versionCode 1350.
- Best-effort diagnostic logging only for pure repository enrichment helpers that are called by local JVM tests.
- Fix nullable `File.parentFile` usage in `LocaleParityTest`.
- No new dependency, no production decision change, no `walkSeconds` consumption.

## Acceptance
`:app:testDebugUnitTest :app:assembleDebug` must finish successfully with all 57 tests green and without the Build-134 test compiler warning.
