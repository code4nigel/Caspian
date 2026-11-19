#!/usr/bin/env python3
"""Package the Chromium and Firefox extensions for distribution."""

import json
from pathlib import Path
from zipfile import ZIP_DEFLATED, ZipFile, ZipInfo


ROOT = Path(__file__).resolve().parents[1]
EXTENSIONS = (
    ("Caspian", "chromium", "Caspian-Extension"),
    ("caspian fox extension", "firefox", "Caspian-Fox-Extension"),
)


def main():
    for folder, platform, prefix in EXTENSIONS:
        source = ROOT / folder
        version = json.loads((source / "manifest.json").read_text(encoding="utf-8"))["version"]
        dest = ROOT / "versions" / platform / f"{prefix}-v{version}.zip"
        dest.parent.mkdir(parents=True, exist_ok=True)
        with ZipFile(dest, "w") as archive:
            for path in sorted(source.rglob("*")):
                relative = path.relative_to(source)
                if not path.is_file() or any(part.startswith(".") for part in relative.parts):
                    continue
                if relative.parts[0] == "images" or path.suffix in {".md", ".zip", ".xpi"}:
                    continue
                entry = ZipInfo(relative.as_posix())
                archive.writestr(entry, path.read_bytes(), compress_type=ZIP_DEFLATED)
        print(dest.relative_to(ROOT))


if __name__ == "__main__":
    main()
