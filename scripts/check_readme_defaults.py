#!/usr/bin/env python3
"""Compatibility wrapper.

README stopped being normative in DOC1. Keep this filename for old automation,
but delegate to the canonical /doc defaults check without duplicating logic.
"""
from pathlib import Path
import runpy

runpy.run_path(str(Path(__file__).with_name("check_doc_defaults.py")), run_name="__main__")
