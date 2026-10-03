#!/usr/bin/env python3
"""Static Android 17 / API-37 baseline gate.

Build 132 proved the targetSdk-37 runtime path. Later builds must preserve that
accepted compatibility baseline unless a dedicated migration decision changes it.
"""
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[1]
errors: list[str] = []
notes: list[str] = []

build = (ROOT / "app/build.gradle.kts").read_text(encoding="utf-8")
root_build = (ROOT / "build.gradle.kts").read_text(encoding="utf-8")
versions = (ROOT / "gradle/libs.versions.toml").read_text(encoding="utf-8")
wrapper = (ROOT / "gradle/wrapper/gradle-wrapper.properties").read_text(encoding="utf-8")
props = (ROOT / "gradle.properties").read_text(encoding="utf-8")
manifest = (ROOT / "app/src/main/AndroidManifest.xml").read_text(encoding="utf-8")

source_files = list((ROOT / "app/src/main/java").rglob("*.kt")) + list((ROOT / "app/src/main/java").rglob("*.java"))
source_text = "\n".join(p.read_text(encoding="utf-8", errors="ignore") for p in source_files)


def require(label: str, condition: bool, detail: str) -> None:
    if not condition:
        errors.append(f"{label}: {detail}")


def value(name: str) -> int | None:
    m = re.search(rf"\b{name}\s*=\s*(\d+)", build)
    return int(m.group(1)) if m else None


def catalog(name: str) -> str | None:
    m = re.search(rf'^{re.escape(name)}\s*=\s*"([^"]+)"', versions, re.MULTILINE)
    return m.group(1) if m else None


compile_sdk = value("compileSdk")
target_sdk = value("targetSdk")
min_sdk = value("minSdk")
version_code = value("versionCode")

require("compileSdk", compile_sdk == 37, "current baseline must compile against Android 17 / API 37")
require("targetSdk", target_sdk == 37, "accepted Android-17 target baseline must remain API 37")
require("minSdk", min_sdk == 34, "minSdk must remain 34 for product policy and graphics-path compatibility shim")
require("versionCode", version_code is not None and version_code >= 1320, "Android-17 baseline builds must remain at versionCode 1320 or newer")

agp = catalog("agp")
kotlin = catalog("kotlin")
ksp = catalog("ksp")
hilt = catalog("hilt")
require("AGP", agp == "9.4.0", f"expected accepted Build-131 AGP 9.4.0, found {agp}")
require("Compose/Kotlin", kotlin == "2.3.21", f"expected accepted Kotlin/Compose plugin 2.3.21, found {kotlin}")
require("KSP", ksp == "2.3.12", f"expected accepted KSP 2.3.12, found {ksp}")
require("Hilt", hilt == "2.60.1", f"expected accepted Hilt 2.60.1, found {hilt}")
require("Gradle", "gradle-9.6.0-bin.zip" in wrapper, "accepted Build-131 Gradle 9.6.0 must remain unchanged")

# Preserve the accepted AGP-9 built-in Kotlin toolchain.
require("built-in Kotlin", "alias(libs.plugins.kotlin.android)" not in build, "kotlin-android plugin must not reappear")
require("built-in Kotlin root", "libs.plugins.kotlin.android" not in root_build, "kotlin-android plugin must not reappear in root build")
require("legacy kotlinOptions", "kotlinOptions" not in build, "android.kotlinOptions DSL must stay removed")
require("KGP alignment", 'classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.3.21")' in root_build, "built-in Kotlin KGP runtime must remain aligned")
require("Compose compiler plugin", "alias(libs.plugins.kotlin.compose)" in build, "Compose compiler plugin must remain applied")
require("No built-in Kotlin opt-out", re.search(r"^\s*android\.builtInKotlin\s*=\s*false\s*$", props, re.MULTILINE) is None, "do not opt out of AGP 9 built-in Kotlin")
require("No unsupported compile suppression", "android.suppressUnsupportedCompileSdk" not in props, "toolchain must support API 37 natively")

# Android 17 target-37 behavior checks that are relevant to this app.
require("predictive back", 'android:enableOnBackInvokedCallback="true"' in manifest, "predictive back callback opt-in must remain enabled")
require("locale config", 'android:localeConfig="@xml/locales_config"' in manifest, "per-app language configuration must remain declared")
require("LAN permission not added", "android.permission.ACCESS_LOCAL_NETWORK" not in manifest, "app has no LAN feature; do not request Android 17 local-network permission")
require("orientation unrestricted", "screenOrientation" not in manifest and "resizeableActivity" not in manifest, "large-screen orientation/resizability restrictions must not be introduced")
require("cleartext disabled by default", 'android:usesCleartextTraffic="true"' not in manifest, "do not weaken HTTPS behavior for target 37")
require("no custom CT disable", "networkSecurityConfig" not in manifest, "keep Android 17 default Certificate Transparency enabled")

# App-local source does not use target-37-sensitive APIs/features that would need code changes.
for label, token, detail in [
    ("MessageQueue reflection", "android.os.MessageQueue", "no app-local MessageQueue internals/reflection expected"),
    ("Content capture override", "setContentCaptureEnabled", "no app-local ContentCapture disable path expected"),
    ("RemoteViews/widget", "RemoteViews", "app has no widget/RemoteViews path"),
    ("Contacts provider", "ContactsContract", "app has no contacts-provider path"),
    ("SMS receive", "SMS_RECEIVED", "app has no SMS/OTP receive path"),
    ("Bluetooth RFCOMM", "BluetoothSocket", "app has no Bluetooth RFCOMM path"),
    ("native dynamic load", "System.load(", "app must not dynamically load writable native files"),
]:
    require(label, token not in source_text, detail)

# HTTP endpoints in app source/resources must remain public HTTPS endpoints; local-network
# permission must not be added just because INTERNET is used.
require("core API HTTPS", 'private const val BASE_URL = "https://api.abfahrt.now/"' in source_text, "abfahrt.now endpoint must remain HTTPS")
require("Photon HTTPS", 'private const val PHOTON_BASE_URL = "https://photon.komoot.io/"' in source_text, "Photon endpoint must remain HTTPS")
require("ORS HTTPS", 'private const val ORS_BASE_URL = "https://api.heigit.org/openrouteservice/"' in source_text, "ORS endpoint must remain HTTPS")

if errors:
    print("Android 17 API-37 baseline readiness check FAILED")
    for error in errors:
        print(f"- {error}")
    sys.exit(1)

print("Android 17 API-37 baseline readiness check OK")
print("- compileSdk=37, targetSdk=37, minSdk=34")
print("- accepted Build-131 AGP/Kotlin/KSP/Hilt toolchain preserved")
print("- no app-local LAN/widget/SMS/contacts/Bluetooth/MessageQueue/native-DCL blockers found")
print("- default Android 17 Certificate Transparency remains enabled")
