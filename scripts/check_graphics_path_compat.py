#!/usr/bin/env python3
"""Static Build-130 guard for the API-34+ graphics-path compatibility layer.

This check proves source intent only. The release APK audit remains the acceptance gate.
"""
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[1]
errors: list[str] = []
notes: list[str] = []

app_gradle = (ROOT / "app/build.gradle.kts").read_text(encoding="utf-8")
catalog = (ROOT / "gradle/libs.versions.toml").read_text(encoding="utf-8")
compat_dir = ROOT / "app/src/main/java/androidx/graphics/path"

m = re.search(r"\bminSdk\s*=\s*(\d+)", app_gradle)
if not m:
    errors.append("minSdk not found")
elif int(m.group(1)) < 34:
    errors.append(f"graphics-path compatibility layer requires minSdk >= 34, found {m.group(1)}")
else:
    notes.append(f"minSdk {m.group(1)} supports framework PathIterator")

if 'exclude(group = "androidx.graphics", module = "graphics-path")' not in app_gradle:
    errors.append("global androidx.graphics:graphics-path exclusion missing")
else:
    notes.append("external androidx.graphics:graphics-path module excluded")

for forbidden in (
    "implementation(libs.androidx.graphics.path)",
    'implementation("androidx.graphics:graphics-path',
    'implementation(\"androidx.graphics:graphics-path',
):
    if forbidden in app_gradle:
        errors.append(f"external graphics-path dependency still present: {forbidden}")

if re.search(r'^graphicsPath\s*=\s*"', catalog, re.M):
    errors.append("graphicsPath version alias remains in version catalog")
if "androidx-graphics-path" in catalog:
    errors.append("androidx-graphics-path library alias remains in version catalog")

expected_files = {
    "PathIterator.kt",
    "PathIteratorImpl.kt",
    "PathSegment.kt",
    "ConicConverter.kt",
    "ConicsImpl.kt",
}
actual_files = {p.name for p in compat_dir.glob("*.kt")} if compat_dir.exists() else set()
missing = sorted(expected_files - actual_files)
if missing:
    errors.append("compatibility source files missing: " + ", ".join(missing))
else:
    notes.append("graphics-path API compatibility sources present")

combined = "\n".join(
    p.read_text(encoding="utf-8") for p in compat_dir.glob("*.kt")
) if compat_dir.exists() else ""

for forbidden in ("System.loadLibrary", " external fun ", "private external fun", "native <methods>"):
    if forbidden in combined:
        errors.append(f"native/JNI marker remains in compatibility layer: {forbidden}")

required_api_tokens = (
    '@file:JvmName("PathUtilities")',
    "public class PathIterator",
    "public enum class ConicEvaluation",
    "public fun calculateSize",
    "public fun next(points: FloatArray",
    "public fun peek()",
    "public operator fun Path.iterator()",
    '@file:JvmName("PathSegmentUtilities")',
    "public class PathSegment",
    "public enum class Type",
)
for token in required_api_tokens:
    if token not in combined:
        errors.append(f"expected compatibility API token missing: {token}")

if "path.pathIterator" not in combined or "android.graphics.PathIterator as PlatformPathIterator" not in combined:
    errors.append("compatibility layer is not backed by framework PathIterator")
else:
    notes.append("compatibility layer delegates path iteration to Android framework")

if "conicToQuadratics" not in combined:
    errors.append("pure-Kotlin conic conversion implementation missing")
else:
    notes.append("pure-Kotlin conic conversion present; no JNI fallback required")

if errors:
    print("Build 130 graphics-path compatibility check FAILED")
    for error in errors:
        print("ERROR:", error)
    sys.exit(1)

print("Build 130 graphics-path compatibility check OK")
for note in notes:
    print("NOTE:", note)
print("IMPORTANT: acceptance requires a release APK with no libandroidx.graphics.path.so and a green 64-bit artifact audit.")
