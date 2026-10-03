# Build 133 Plan

1. Converge Build-132 targetSdk-37 acceptance into `/doc` and `/evidence`.
2. Bump versionCode to 1330.
3. Apply explicit `@param:ApplicationContext` to the two known injected constructor parameters.
4. Remove only the six compiler-reported redundant nullability operators.
5. Preserve all Android-17, 16-KB, API, UI and dependency baselines.
6. Run existing source/static gates and inspect the source diff.
7. Package source.
8. User performs real Gradle build; acceptance requires the known warnings to be gone.
