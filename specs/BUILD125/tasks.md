# Build 125 Tasks

- [x] Accept Build 124 runtime evidence and close AB-047/052.
- [x] Add `ApiKeyCipher` with AES-256/GCM and Android Keystore.
- [x] Encrypt new abfahrt.now key writes.
- [x] Encrypt new ORS key writes.
- [x] Add in-place migration for legacy plaintext keys.
- [x] Keep migration loss-safe and retryable on crypto failure.
- [x] Keep crypto off the main thread.
- [x] Keep backup/device-transfer exclusion.
- [x] Add `scripts/check_api_key_storage.py`.
- [x] Bump `versionCode` to 1250.
- [x] Update normative `/doc` and source evidence.
- [ ] Device migration smoke with pre-existing abfahrt.now + ORS keys.
- [ ] Restart smoke after migration.
- [ ] Replace/delete both keys on device.
