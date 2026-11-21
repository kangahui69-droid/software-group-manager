#!/usr/bin/env python3
"""Capture reviewable local changes without private or generated files.

The archive overlays the recorded HEAD commit. It is not a production backup.
"""

import argparse
import hashlib
import io
import json
import subprocess
import tarfile
from datetime import datetime, timezone
from pathlib import Path


EXCLUDED_PREFIXES = (
    ".claude/", ".git/", ".idea/", "backup/", "bin/", "docs/.ppt-build/",
    "docs/output/", "localstorage/", "nginx/tls/", "private/",
    "scripts/__pycache__/", "target/",
)
EXCLUDED_SUFFIXES = (".key", ".pem", ".p12", ".pfx")


def git(root: Path, *args: str) -> bytes:
    result = subprocess.run(
        ["git", *args], cwd=root, stdout=subprocess.PIPE, stderr=subprocess.PIPE,
        check=True,
    )
    return result.stdout


def safe_path(name: str) -> bool:
    parts = Path(name).parts
    if not name or name.startswith(("/", "\\")) or "\\" in name or ".." in parts:
        return False
    if any(name.startswith(prefix) for prefix in EXCLUDED_PREFIXES):
        return False
    if name.startswith("tomcat.") or any(part in ("target", "__pycache__") for part in parts):
        return False
    base = parts[-1].lower()
    if (base == ".env" or (base.startswith(".env.") and base != ".env.example")
            or base.startswith("config.local.") or base.endswith(EXCLUDED_SUFFIXES)):
        return False
    return True


def paths_from_git(root: Path, *args: str) -> set[str]:
    raw = git(root, *args)
    return {entry.decode("utf-8") for entry in raw.split(b"\0") if entry}


def capture(root: Path, output: Path) -> Path:
    root = root.resolve()
    if git(root, "diff", "--cached", "--name-only", "-z"):
        raise SystemExit("Index contains staged changes; commit or unstage them before capture")
    head = git(root, "rev-parse", "HEAD").decode().strip()
    changed = paths_from_git(root, "diff", "--name-only", "-z", "HEAD")
    untracked = paths_from_git(root, "ls-files", "--others", "--exclude-standard", "-z")
    requested = sorted(changed | untracked)
    included = [name for name in requested if safe_path(name)]
    excluded = [name for name in requested if not safe_path(name)]
    stamp = datetime.now(timezone.utc).strftime("%Y%m%dT%H%M%SZ")
    destination = output.resolve() / f"qa04-local-{stamp}"
    destination.mkdir(parents=True, exist_ok=False)
    files = []
    archive = destination / "changes.tar.gz"
    with tarfile.open(archive, "w:gz") as bundle:
        for name in included:
            source = root / name
            if not source.exists():
                files.append({"path": name, "state": "deleted"})
                continue
            if source.is_symlink() or not source.is_file() or not source.resolve().is_relative_to(root):
                raise SystemExit(f"Unsafe source path: {name}")
            data = source.read_bytes()
            files.append({"path": name, "state": "present", "sha256": hashlib.sha256(data).hexdigest()})
            entry = tarfile.TarInfo(f"files/{name}")
            entry.size = len(data)
            entry.mode = 0o600
            bundle.addfile(entry, io.BytesIO(data))
    manifest = {
        "schema": 1,
        "created_utc": datetime.now(timezone.utc).isoformat(),
        "base_commit": head,
        "archive": archive.name,
        "files": files,
        "excluded_local_or_generated_paths": excluded,
        "note": "Overlay on base_commit; excluded paths require separate private handling if needed.",
    }
    (destination / "manifest.json").write_text(
        json.dumps(manifest, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
    )
    (destination / "paths.txt").write_text(
        "".join(f"{item['path']}\n" for item in files), encoding="utf-8"
    )
    (destination / "changes.tar.gz.sha256").write_text(
        f"{hashlib.sha256(archive.read_bytes()).hexdigest()}  {archive.name}\n", encoding="ascii"
    )
    return destination


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--root", type=Path, default=Path(__file__).resolve().parent.parent)
    parser.add_argument("--output", type=Path, default=None)
    args = parser.parse_args()
    root = args.root.resolve()
    destination = capture(root, args.output or root / "backup")
    print(f"Snapshot: {destination}")
    print(f"Included: {len(json.loads((destination / 'manifest.json').read_text(encoding='utf-8'))['files'])} paths")


if __name__ == "__main__":
    main()
