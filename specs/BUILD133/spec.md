# Build 133 Specification — Kotlin 2.3 Warning Hygiene

## Goal
Close the compiler-warning debt exposed by the AGP 9 / Kotlin 2.3 migration without changing product behavior.

## Requirements
- `versionCode = 1330`.
- Keep minSdk 34 / compileSdk 37 / targetSdk 37 and the accepted Build-132 toolchain/dependencies unchanged.
- Make `@ApplicationContext` constructor injection target explicit with `@param:`.
- Remove only compiler-proven redundant `?.` / `!!` operators.
- No changes to transit logic, matching semantics, ORS behavior, UI, security, networking or MapLibre.
- Preserve the Build-130 graphics-path exclusion/shim and 16-KB baseline.

## Acceptance
A real Gradle compile/build must succeed and the seven known Build-132 Kotlin warnings must disappear. Runtime smoke is intentionally lightweight because the code edits are nullability/annotation-preserving hygiene only.
