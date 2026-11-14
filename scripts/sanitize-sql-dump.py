"""Remove production rows from the historical software_group MySQL dump.

The script is intentionally idempotent so it can run for every commit during a
Git history rewrite. It preserves table definitions and refuses unknown data
statements instead of silently leaving records in the repository.
"""

from pathlib import Path
import re
import sys


DATA_BLOCK = re.compile(
    r"(?ms)^-- Dumping data for table.*?^UNLOCK TABLES;\n"
)
DATA_STATEMENT = re.compile(r"(?im)^\s*(?:INSERT|REPLACE|LOAD DATA)\b")
CONTACT = re.compile(
    r"[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}|(?<!\d)1[3-9]\d{9}(?!\d)"
)
HEADER = "-- Schema-only baseline. Never commit production data to this repository.\n"


def sanitize(content: str) -> str:
    cleaned = DATA_BLOCK.sub("", content)
    cleaned = re.sub(r" AUTO_INCREMENT=\d+", "", cleaned)
    if DATA_STATEMENT.search(cleaned) or CONTACT.search(cleaned):
        raise ValueError("SQL dump still contains possible data or contact information")
    if not cleaned.startswith(HEADER):
        cleaned = HEADER + cleaned
    return cleaned


def main() -> None:
    path = Path(sys.argv[1] if len(sys.argv) > 1 else "sql/software_group.sql")
    if not path.exists():
        return
    content = path.read_text(encoding="utf-8")
    cleaned = sanitize(content)
    if cleaned != content:
        path.write_text(cleaned, encoding="utf-8", newline="\n")


if __name__ == "__main__":
    main()
