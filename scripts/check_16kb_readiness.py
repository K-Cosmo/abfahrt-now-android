#!/usr/bin/env python3
"""Static source preflight for Android 16-KB page-size readiness.

This is deliberately NOT an artifact acceptance test. 16-KB compatibility is
ultimately a property of the built APK/AAB and its native libraries.
"""
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[1]
errors: list[str] = []
notes: list[str] = []


def version_tuple(value: str) -> tuple[int, ...]:
    parts = []
    for part in value.split('.'):
        m = re.match(r"(\d+)", part)
        parts.append(int(m.group(1)) if m else 0)
    return tuple(parts)


def catalog_version(catalog: str, key: str) -> str | None:
    m = re.search(rf'^{re.escape(key)}\s*=\s*"([^"]+)"', catalog, re.M)
    return m.group(1) if m else None


catalog = (ROOT / 'gradle/libs.versions.toml').read_text(encoding='utf-8')
app_gradle = (ROOT / 'app/build.gradle.kts').read_text(encoding='utf-8')
application = (ROOT / 'app/src/main/java/now/abfahrt/transit/AbfahrtApplication.kt').read_text(encoding='utf-8')

agp = catalog_version(catalog, 'agp')
if not agp:
    errors.append('AGP version not found in gradle/libs.versions.toml')
elif version_tuple(agp) < (8, 5, 1):
    errors.append(f'AGP {agp} is below the Android 16-KB recommendation 8.5.1+')
else:
    notes.append(f'AGP {agp} >= 8.5.1')

datastore = catalog_version(catalog, 'datastore')
if not datastore:
    errors.append('DataStore version not found in gradle/libs.versions.toml')
elif version_tuple(datastore) < (1, 2, 1):
    errors.append(f'DataStore {datastore} is below Build 127 remediation baseline 1.2.1')
else:
    notes.append(f'DataStore {datastore} >= 1.2.1')

# Build 130 deliberately excludes the published AndroidX graphics-path prebuilt.
# Its native binary failed the real arm64/x86_64 GNU_RELRO gate in Build 129.
compat_dir = ROOT / 'app/src/main/java/androidx/graphics/path'
if 'exclude(group = "androidx.graphics", module = "graphics-path")' not in app_gradle:
    errors.append('Build 130 graphics-path module exclusion is missing')
elif 'implementation(libs.androidx.graphics.path)' in app_gradle or 'androidx-graphics-path' in catalog:
    errors.append('Build 130 still declares the external graphics-path artifact')
elif not compat_dir.exists():
    errors.append('Build 130 API-34+ graphics-path compatibility source is missing')
else:
    compat_text = '\n'.join(p.read_text(encoding='utf-8') for p in compat_dir.glob('*.kt'))
    if 'System.loadLibrary' in compat_text or 'external fun' in compat_text:
        errors.append('Build 130 graphics-path compatibility source still contains JNI/native loading')
    elif 'path.pathIterator' not in compat_text or 'conicToQuadratics' not in compat_text:
        errors.append('Build 130 graphics-path compatibility source lacks platform iterator or Kotlin conic conversion')
    else:
        notes.append('AndroidX graphics-path prebuilt excluded; API-34+ pure-Kotlin/platform compatibility layer present')

min_sdk_match = re.search(r'\bminSdk\s*=\s*(\d+)', app_gradle)
if not min_sdk_match:
    errors.append('minSdk not found')
elif int(min_sdk_match.group(1)) < 34:
    errors.append(f'Build 130 graphics-path compatibility layer requires minSdk >= 34, found {min_sdk_match.group(1)}')
else:
    notes.append(f'minSdk {min_sdk_match.group(1)} satisfies platform PathIterator floor')

m = re.search(r'org\.maplibre\.gl:(android-sdk(?:-opengl)?):([^"\)]+)', app_gradle)
if not m:
    errors.append('MapLibre Android dependency not found')
else:
    artifact = m.group(1).strip()
    maplibre = m.group(2).strip()
    if artifact != 'android-sdk-opengl':
        errors.append(f'Build 129 expects explicit OpenGL MapLibre artifact, found {artifact}')
    elif version_tuple(maplibre) < (13, 6, 0):
        errors.append(f'MapLibre Android OpenGL SDK {maplibre} is below Build 129 evidence baseline 13.6.0')
    elif version_tuple(maplibre) >= (14, 0, 0):
        errors.append(f'MapLibre Android OpenGL SDK {maplibre} crosses the Build 129 tested 13.x boundary')
    else:
        notes.append(f'MapLibre Android OpenGL SDK {maplibre} meets Build 129 evidence baseline')

if 'useLegacyPackaging = true' in app_gradle or 'useLegacyPackaging true' in app_gradle:
    errors.append('Legacy JNI packaging is enabled; Build 127 expects modern AGP packaging')
else:
    notes.append('No legacy JNI packaging override')

if 'Os.sysconf(OsConstants._SC_PAGESIZE)' not in application or 'AbfahrtCompat' not in application:
    errors.append('Runtime page-size evidence log is missing')
else:
    notes.append('Runtime page-size diagnostic present')

# The app itself should not grow a local NDK build just for this readiness gate.
local_native_markers = [
    ROOT / 'app/src/main/cpp',
    ROOT / 'app/CMakeLists.txt',
    ROOT / 'app/src/main/jniLibs',
]
found = [str(p.relative_to(ROOT)) for p in local_native_markers if p.exists()]
if found:
    notes.append('Local native source/libs present: ' + ', '.join(found))
else:
    notes.append('No app-owned CMake/cpp/jniLibs tree; native code is dependency-provided')

if errors:
    print('16-KB source preflight FAILED')
    for e in errors:
        print('ERROR:', e)
    sys.exit(1)

print('16-KB source preflight OK')
for n in notes:
    print('NOTE:', n)
print('IMPORTANT: run scripts/audit_16kb_artifact.py against the built APK; source preflight alone is not acceptance evidence.')
