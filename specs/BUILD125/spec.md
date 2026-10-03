# Build 125 Specification — API-Key Security / Keystore Migration

## Goal

Protect the locally persisted abfahrt.now and OpenRouteService API keys at rest without changing the existing API-key UX, network contracts or preference architecture.

## Requirements

1. New or updated API keys must not be persisted as plaintext.
2. Use Android Keystore with AES-256/GCM and no new dependency.
3. Preserve DataStore as the existing preference store; do not introduce a parallel secret database.
4. Existing plaintext values from Build 124 and earlier must migrate in place on first use.
5. Migration must be loss-safe: overwrite a legacy value only after encryption succeeds.
6. Encryption failure during a new save must never fall back to a plaintext write.
7. Cryptographic work must not run on the main thread.
8. Do not log plaintext keys or ciphertext.
9. Keep DataStore excluded from cloud backup/device transfer because the Keystore key is device-bound.
10. UI, ORS behavior, abfahrt.now behavior, dedup and routing remain unchanged.

## Acceptance

On a device upgraded from Build 124 with both keys configured:
- migration status is logged without secret material;
- abfahrt.now works after migration and after app restart;
- ORS works after migration and after app restart;
- key replacement and deletion still work;
- no crash occurs during migration.
