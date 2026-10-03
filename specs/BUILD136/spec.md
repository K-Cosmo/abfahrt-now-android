# Build 136 Specification — Mandatory abfahrt.now API key

Goal: the Departure app flow is no longer usable without a personal abfahrt.now API key. ORS remains optional. Existing encrypted storage is reused; no new auth architecture.

Acceptance: blank key cannot pass onboarding, keyless existing install returns to onboarding, key cannot be deleted in Settings, 401 returns to correction, normal valid-key flow remains functional.
