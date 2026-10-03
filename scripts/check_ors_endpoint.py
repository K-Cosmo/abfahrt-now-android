#!/usr/bin/env python3
"""Build 123 guard: ORS must use the current HEIGIT base path only."""
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
APP = ROOT / "app" / "src" / "main" / "java"
MODULE = APP / "now" / "abfahrt" / "transit" / "di" / "AppModule.kt"
REPOSITORY = APP / "now" / "abfahrt" / "transit" / "data" / "repository" / "WalkingRouteRepository.kt"
EXPECTED = 'private const val ORS_BASE_URL = "https://api.heigit.org/openrouteservice/"'
DEPRECATED = "api.openrouteservice.org"

errors = []
module_text = MODULE.read_text(encoding="utf-8")
if EXPECTED not in module_text:
    errors.append("AppModule.kt does not contain the expected HEIGIT ORS base URL")

repository_text = REPOSITORY.read_text(encoding="utf-8")
for relative_path in (
    "v2/matrix/foot-walking",
    "v2/matrix/cycling-regular",
    "v2/directions/foot-walking/geojson",
    "v2/directions/cycling-regular/geojson",
):
    if f'"{relative_path}"' not in repository_text:
        errors.append(f"expected relative ORS path missing: {relative_path}")

for path in APP.rglob("*.kt"):
    if DEPRECATED in path.read_text(encoding="utf-8"):
        errors.append(f"deprecated ORS host remains in runtime source: {path.relative_to(ROOT)}")

if errors:
    print("ORS endpoint check FAILED")
    for error in errors:
        print(f"- {error}")
    sys.exit(1)

print("ORS endpoint check OK")
