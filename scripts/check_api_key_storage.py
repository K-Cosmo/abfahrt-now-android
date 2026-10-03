#!/usr/bin/env python3
from pathlib import Path
import re
import sys

root = Path(__file__).resolve().parents[1]
repo = root / "app/src/main/java/now/abfahrt/transit/data/preferences/UserPreferencesRepository.kt"
cipher = root / "app/src/main/java/now/abfahrt/transit/data/security/ApiKeyCipher.kt"
rules = root / "app/src/main/res/xml/data_extraction_rules.xml"

access_gate = root / "app/src/main/java/now/abfahrt/transit/ui/screens/AppAccessGate.kt"
onboarding = root / "app/src/main/java/now/abfahrt/transit/ui/screens/OnboardingScreen.kt"
settings = root / "app/src/main/java/now/abfahrt/transit/ui/screens/SettingsSheet.kt"
view_model = root / "app/src/main/java/now/abfahrt/transit/ui/viewmodel/DepartureViewModel.kt"

failures = []

def require(text: str, token: str, label: str):
    if token not in text:
        failures.append(f"missing {label}: {token}")

repo_text = repo.read_text(encoding="utf-8")
cipher_text = cipher.read_text(encoding="utf-8")
rules_text = rules.read_text(encoding="utf-8")
access_gate_text = access_gate.read_text(encoding="utf-8")
onboarding_text = onboarding.read_text(encoding="utf-8")
settings_text = settings.read_text(encoding="utf-8")
view_model_text = view_model.read_text(encoding="utf-8")

require(cipher_text, '"AndroidKeyStore"', "Android Keystore provider")
require(cipher_text, '"AES/GCM/NoPadding"', "AES-GCM transformation")
require(cipher_text, '.setKeySize(256)', "AES-256 key size")
require(cipher_text, '.setRandomizedEncryptionRequired(true)', "randomized encryption")
require(cipher_text, 'private const val PREFIX = "enc:v1:"', "versioned ciphertext prefix")
require(repo_text, 'ensureApiKeysEncrypted()', "legacy-key migration")
require(repo_text, 'apiKeyCipher.encrypt', "encrypted writes")
require(repo_text, 'apiKeyCipher.decrypt', "encrypted reads")
require(repo_text, '.flowOn(Dispatchers.IO)', "off-main keystore work")
require(rules_text, 'datastore/abfahrt_prefs.preferences_pb', "backup exclusion")
require(access_gate_text, "onboardingCompleted && apiKey.isNotBlank()", "mandatory abfahrt.now access gate")
require(onboarding_text, "enabled = apiKey.isNotBlank()", "onboarding key requirement")
require(settings_text, "allowDelete = false", "required-key delete prevention")
require(repo_text, "refusing to remove required abfahrt.now API key", "repository blank-key guard")
require(view_model_text, "e.code() == 401", "401 authentication correction gate")
require(view_model_text, "prefsRepo.setOnboardingCompleted(false)", "401 onboarding reset")
if "next_without_key" in onboarding_text:
    failures.append("onboarding still references keyless continue action")

raw_write_patterns = [
    r'Keys\.API_KEY\]\s*=\s*key\.trim\(\)',
    r'Keys\.ORS_API_KEY\]\s*=\s*key\.trim\(\)',
]
for pattern in raw_write_patterns:
    if re.search(pattern, repo_text):
        failures.append(f"plaintext API-key write still present: {pattern}")

if failures:
    print("API-key storage check FAILED")
    for failure in failures:
        print(f"- {failure}")
    sys.exit(1)

print("API-key storage check OK")
