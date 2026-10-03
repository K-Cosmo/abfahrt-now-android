#!/usr/bin/env python3
"""Check Android string key parity and placeholder compatibility for AbfahrtApp locales.

Base Android resource set: app/src/main/res/values/strings.xml
Fails when any declared values-xx/strings.xml file misses a key from the base file
or when printf-style placeholders differ.
"""
from __future__ import annotations

import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

PLACEHOLDER_RE = re.compile(r"%(?:\d+\$)?[sdif]")

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "app" / "src" / "main" / "res"
BASE = RES / "values" / "strings.xml"


def load_strings(path: Path) -> dict[str, str]:
    tree = ET.parse(path)
    out: dict[str, str] = {}
    for elem in tree.getroot().findall("string"):
        name = elem.attrib.get("name")
        if name:
            out[name] = "".join(elem.itertext())
    return out


def placeholders(value: str) -> list[str]:
    return PLACEHOLDER_RE.findall(value)


def main() -> int:
    base = load_strings(BASE)
    failed = False
    for locale_file in sorted(RES.glob("values-*/strings.xml")):
        locale = locale_file.parent.name
        values = load_strings(locale_file)
        missing = sorted(set(base) - set(values))
        extra = sorted(set(values) - set(base))
        mismatches = []
        for key in sorted(set(base) & set(values)):
            if placeholders(base[key]) != placeholders(values[key]):
                mismatches.append((key, placeholders(base[key]), placeholders(values[key])))
        if missing or extra or mismatches:
            failed = True
            print(f"[{locale}]", file=sys.stderr)
            if missing:
                print("  missing:", ", ".join(missing), file=sys.stderr)
            if extra:
                print("  extra:", ", ".join(extra), file=sys.stderr)
            for key, base_ph, loc_ph in mismatches:
                print(f"  placeholder mismatch {key}: base={base_ph} locale={loc_ph}", file=sys.stderr)
    if failed:
        return 1
    print("Locale key parity OK")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
