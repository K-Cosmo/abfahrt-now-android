# Build 141 Plan

1. Preserve Build-140 RoutePlanner presentation and result logic.
2. Replace passive empty/error text with a small reusable status card and existing localized retry action.
3. Keep provider region visible for empty responses when present.
4. Render optional `/trips` attribution from the existing model field.
5. Make `AbfahrtTrips` logs classify success/empty/error without adding PII labels.
6. Update `/doc`, evidence and static gates; hand off for real Gradle/runtime validation.
