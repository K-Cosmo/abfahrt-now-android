#!/usr/bin/env python3
"""Minimal DOC1 governance and local-link check."""
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[1]
DOC = ROOT / "doc"
required = {
    "00-index.md", "01-product.md", "02-architecture.md", "03-api-contracts.md",
    "04-invariants.md", "05-regression-ledger.md", "06-decisions.md",
    "07-findings.md", "08-backlog.md", "09-release-plan.md", "10-build-handoff.md",
    "11-test-and-evidence.md", "12-android-compatibility.md", "13-localization.md",
    "14-community-and-service-policy.md", "AI-CODING-GUARDRAILS.md", "CHANGELOG.md",
}
errors: list[str] = []

missing = sorted(name for name in required if not (DOC / name).is_file())
if missing:
    errors.append("missing /doc files: " + ", ".join(missing))

# REPO1 deliberately removes the old /docs redirect tree. A second documentation
# root would undermine the single-source-of-truth rule.
legacy_docs = ROOT / "docs"
if legacy_docs.exists():
    errors.append("legacy /docs tree must not exist; /doc is the only documentation root")

index = (DOC / "00-index.md").read_text(encoding="utf-8") if (DOC / "00-index.md").exists() else ""
if "einzige normative Quelle der Wahrheit" not in index:
    errors.append("/doc/00-index.md does not declare the single normative source rule")

link_re = re.compile(r"\[[^\]]*\]\((?!https?://|mailto:|#)([^)]+)\)")
for md in [ROOT / "README.md", ROOT / "CHANGELOG.md", *sorted(DOC.glob("*.md")), ROOT / "evidence/README.md"]:
    if not md.exists():
        continue
    text = md.read_text(encoding="utf-8")
    for target in link_re.findall(text):
        target = target.split("#", 1)[0]
        if not target:
            continue
        resolved = (md.parent / target).resolve()
        if not resolved.exists():
            errors.append(f"broken local link in {md.relative_to(ROOT)} -> {target}")

if errors:
    print("DOC governance check FAILED")
    for err in errors:
        print(f"- {err}")
    sys.exit(1)
print("DOC governance check OK")
