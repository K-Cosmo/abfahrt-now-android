#!/usr/bin/env python3
"""Check Android string key parity and placeholder compatibility for AbfahrtApp locales.

All XML resource files below values/ and values-*/ are considered. This keeps locale
parity intact even when a feature uses a small dedicated string resource file instead
of growing the historical strings.xml further.
"""
from __future__ import annotations

import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

PLACEHOLDER_RE = re.compile(r"%(?:\d+\$)?[sdif]")

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "app" / "src" / "main" / "res"
BASE_DIR = RES / "values"


def load_strings(directory: Path) -> dict[str, str]:
    out: dict[str, str] = {}
    for path in sorted(directory.glob("*.xml")):
        tree = ET.parse(path)
        for elem in tree.getroot().findall("string"):
            name = elem.attrib.get("name")
            if not name:
                continue
            if name in out:
                raise ValueError(f"duplicate string key {name!r} in {directory.name}")
            out[name] = "".join(elem.itertext())
    return out


def placeholders(value: str) -> list[str]:
    return PLACEHOLDER_RE.findall(value)


def main() -> int:
    try:
        base = load_strings(BASE_DIR)
    except (ET.ParseError, ValueError) as error:
        print(f"[values] {error}", file=sys.stderr)
        return 1

    failed = False
    for locale_dir in sorted(path for path in RES.glob("values-*") if path.is_dir()):
        try:
            values = load_strings(locale_dir)
        except (ET.ParseError, ValueError) as error:
            failed = True
            print(f"[{locale_dir.name}] {error}", file=sys.stderr)
            continue

        missing = sorted(set(base) - set(values))
        extra = sorted(set(values) - set(base))
        mismatches = []
        for key in sorted(set(base) & set(values)):
            if placeholders(base[key]) != placeholders(values[key]):
                mismatches.append((key, placeholders(base[key]), placeholders(values[key])))
        if missing or extra or mismatches:
            failed = True
            print(f"[{locale_dir.name}]", file=sys.stderr)
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
