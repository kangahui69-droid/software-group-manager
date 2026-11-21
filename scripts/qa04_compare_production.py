#!/usr/bin/env python3
"""Compare a local QA-04 snapshot with hashes collected from the production VM."""

import argparse
import json
from pathlib import Path


def parse_hashes(path: Path) -> dict[str, str]:
    result = {}
    for raw_line in path.read_text(encoding="utf-8").splitlines():
        if raw_line.startswith("MISSING\t"):
            result[raw_line.split("\t", 1)[1]] = "MISSING"
            continue
        digest, separator, name = raw_line.partition("  ")
        if not separator or len(digest) != 64:
            raise ValueError(f"Invalid hash line: {raw_line}")
        result[name] = digest.lower()
    return result


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("local_manifest", type=Path)
    parser.add_argument("production_hashes", type=Path)
    args = parser.parse_args()
    manifest = json.loads(args.local_manifest.read_text(encoding="utf-8"))
    production = parse_hashes(args.production_hashes)
    matched = 0
    different = 0
    for item in manifest["files"]:
        name = item["path"]
        expected = item.get("sha256", "MISSING")
        actual = production.get(name, "NO_EVIDENCE")
        if expected == actual:
            matched += 1
        else:
            different += 1
            print(f"DIFFERENT\t{name}\tlocal={expected}\tproduction={actual}")
    print(f"matched={matched} different={different} base_commit={manifest['base_commit']}")
    raise SystemExit(1 if different else 0)


if __name__ == "__main__":
    main()
