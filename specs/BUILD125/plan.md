# Build 125 Plan

1. Add a small `ApiKeyCipher` using `AndroidKeyStore` and `AES/GCM/NoPadding`.
2. Keep the existing DataStore keys, but persist versioned ciphertext (`enc:v1:`).
3. Add a one-time, loss-safe legacy plaintext migration before normal preference collection.
4. Decrypt only for in-process consumers; cache unchanged decrypted values in memory to avoid repeated Keystore work on every request.
5. Run preference/crypto flow work on `Dispatchers.IO`.
6. Retain existing backup/device-transfer exclusion.
7. Add a static source gate for the security invariants.
8. Update `/doc`, evidence and handoff; do not modify UI or network logic.
