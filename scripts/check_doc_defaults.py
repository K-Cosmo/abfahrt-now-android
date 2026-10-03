#!/usr/bin/env python3
"""Check normative /doc product defaults against runtime defaults.

/doc is normative for documented product defaults. This script catches drift
between that documentation and the current implementation; it does not decide
which side should win when a mismatch is found.
"""
from pathlib import Path
import re
import sys

root = Path(__file__).resolve().parents[1]
product = (root / "doc/01-product.md").read_text(encoding="utf-8")
models = (root / "app/src/main/java/now/abfahrt/transit/data/model/Models.kt").read_text(encoding="utf-8")
prefs = (root / "app/src/main/java/now/abfahrt/transit/data/preferences/UserPreferencesRepository.kt").read_text(encoding="utf-8")

errors: list[str] = []

def require(label: str, condition: bool, detail: str) -> None:
    if not condition:
        errors.append(f"{label}: {detail}")

def int_default(name: str, text: str) -> int:
    match = re.search(rf"val\s+{re.escape(name)}\s*:\s*Int\s*=\s*(\d+)", text)
    if not match:
        raise RuntimeError(f"Could not find default for {name}")
    return int(match.group(1))

radius = int_default("radius", models)
window_start = int_default("windowStartMinutes", models)
window_end = int_default("windowEndMinutes", models)
refresh = int_default("refreshIntervalMinutes", models)

window_max_match = re.search(r"windowEndMinutes\s*=\s*end\.coerceIn\(start \+ 5,\s*(\d+)\)", prefs)
if not window_max_match:
    window_max_match = re.search(r"val\s+e\s*=\s*endMin\.coerceIn\(s \+ 5,\s*(\d+)\)", prefs)
if not window_max_match:
    raise RuntimeError("Could not find windowEndMinutes coerceIn max")
window_max = int(window_max_match.group(1))

require("radius default", f"Suchradius: 100–2.000 m, Default {radius} m." in product, "doc/01-product.md radius default does not match code")
require("window range", f"Sichtbares Abfahrtsfenster: 0–{window_max} min, Default {window_start}–{window_end} min." in product, "doc/01-product.md window range/default does not match code")
require("refresh default", f"Automatischer Refresh: aus oder 1–5 min, Default {refresh} min." in product, "doc/01-product.md refresh default does not match code")

if errors:
    print("/doc defaults check FAILED")
    for err in errors:
        print(f"- {err}")
    sys.exit(1)

print("/doc defaults check OK")
