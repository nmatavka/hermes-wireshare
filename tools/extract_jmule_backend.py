#!/usr/bin/env python3
"""
Extract the JMule CVS head revision into WireShare-owned source trees.

This is a one-shot transplant utility. The final build must never depend on the
donor `jmule/` folder being present at compile time.
"""

from __future__ import annotations

import re
import shutil
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
DONOR_ROOT = ROOT / "jmule" / "jmule2" / "src"
BACKEND_JAVA_OUT = ROOT / "ed2k-backend" / "src" / "main" / "java"
BACKEND_RES_OUT = ROOT / "ed2k-backend" / "src" / "main" / "resources"

PROVENANCE_NOTE = """/*
 * Provenance note:
 * This file was extracted from the local JMule CVS donor tree by
 * tools/extract_jmule_backend.py and is now carried as WireShare-owned source.
 * The build must not depend on the donor jmule/ directory at compile time.
 */
"""


def parse_head_revision(rcs_text: str) -> str:
    match = re.search(r"head\s+([^;]+);", rcs_text)
    if not match:
        raise ValueError("Missing head revision")
    return match.group(1).strip()


def extract_head_text(rcs_path: Path) -> str:
    text = rcs_path.read_text("utf-8", errors="replace")
    head = parse_head_revision(text)
    marker = f"\n{head}\nlog\n@"
    marker_index = text.find(marker)
    if marker_index < 0:
        raise ValueError(f"Unable to locate head log marker for {rcs_path}")
    text_marker = text.find("\ntext\n@", marker_index)
    if text_marker < 0:
        raise ValueError(f"Unable to locate head text marker for {rcs_path}")

    pos = text_marker + len("\ntext\n@")
    out: list[str] = []
    while pos < len(text):
        ch = text[pos]
        if ch == "@":
            if pos + 1 < len(text) and text[pos + 1] == "@":
                out.append("@")
                pos += 2
                continue
            break
        out.append(ch)
        pos += 1
    return "".join(out)


def prepend_provenance(java_source: str) -> str:
    stripped = java_source.lstrip()
    if stripped.startswith("/*"):
        comment_end = java_source.find("*/")
        if comment_end >= 0:
            comment_end += 2
            return java_source[:comment_end] + "\n\n" + PROVENANCE_NOTE + java_source[comment_end:]
    return PROVENANCE_NOTE + "\n" + java_source


def should_skip(path: Path) -> bool:
    if "Attic" in path.parts:
        return True
    if not path.name.endswith(",v"):
        return True
    if path.name.endswith(".aj,v"):
        return True
    return False


def clean_outputs() -> None:
    shutil.rmtree(BACKEND_JAVA_OUT / "org" / "jmule", ignore_errors=True)
    shutil.rmtree(BACKEND_JAVA_OUT / "com" / "maxmind", ignore_errors=True)
    shutil.rmtree(BACKEND_RES_OUT / "org" / "jmule", ignore_errors=True)
    shutil.rmtree(BACKEND_RES_OUT / "com" / "maxmind", ignore_errors=True)


def write_java_tree() -> int:
    copied = 0
    for rcs_path in sorted(DONOR_ROOT.rglob("*,v")):
        if should_skip(rcs_path):
            continue

        relative = rcs_path.relative_to(DONOR_ROOT)
        if not (
            relative.parts[:3] == ("org", "jmule", "core")
            or relative.parts[:3] == ("org", "jmule", "countrylocator")
            or relative.parts[:2] == ("com", "maxmind")
        ):
            continue

        target_relative = Path(str(relative)[:-2])
        source = extract_head_text(rcs_path)

        if target_relative.suffix == ".java":
            source = prepend_provenance(source)
            target = BACKEND_JAVA_OUT / target_relative
        else:
            target = BACKEND_RES_OUT / target_relative

        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_text(source, encoding="utf-8")
        copied += 1
    return copied


def main() -> None:
    if not DONOR_ROOT.exists():
        raise SystemExit(f"Donor source root not found: {DONOR_ROOT}")

    clean_outputs()
    copied = write_java_tree()
    print(f"Extracted {copied} JMule donor files into {BACKEND_JAVA_OUT.parent}")


if __name__ == "__main__":
    main()
