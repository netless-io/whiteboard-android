#!/usr/bin/env python3
"""Verify local Bridge assets and their exact contents in built APK/AAR files."""
import argparse
import hashlib
import json
from pathlib import Path
import re
import zipfile


def digest(data):
    return {"bytes": len(data), "sha256": hashlib.sha256(data).hexdigest()}


def directory_files(directory):
    return {str(p.relative_to(directory)): digest(p.read_bytes())
            for p in sorted(directory.rglob("*")) if p.is_file()}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--bridge", type=Path, help="Reference Bridge build directory")
    parser.add_argument("packages", type=Path, nargs="*", help="Built APK/AAR files")
    args = parser.parse_args()
    assets = Path(__file__).resolve().parents[1] / "sdk/src/main/assets/whiteboard"
    expected = directory_files(assets)
    markers = [name for name in expected if re.fullmatch(r"[0-9a-f]{7,40}", name)]
    assert len(markers) == 1 and expected[markers[0]]["bytes"] == 0, "Missing/ambiguous Bridge marker"
    for kind in ("full", "sub"):
        chunks = [name for name in expected
                  if re.fullmatch(r"appliance-worker-" + kind + r"-source\.[0-9a-f]+\.js", name)]
        assert len(chunks) == 1, "Missing/ambiguous " + kind + " Worker source chunk"
    html = (assets / "index.html").read_text()
    initial_scripts = re.findall(r'<script\b[^>]*\bsrc=[\"\']([^\"\']+)', html)
    assert initial_scripts, "No initial scripts"
    for script in initial_scripts:
        assert script in expected, "Missing/nonlocal initial script: " + script
        assert not script.startswith("appliance-worker-"), "Worker source loaded eagerly by HTML"
    if args.bridge:
        assert directory_files(args.bridge) == expected, "SDK assets differ from Bridge build"
    packages = {}
    prefix = "assets/whiteboard/"
    for package in args.packages:
        with zipfile.ZipFile(package) as archive:
            names = [name for name in archive.namelist() if name.startswith(prefix) and not name.endswith("/")]
            assert len(names) == len(set(names)), "Duplicate packaged assets"
            actual = {name[len(prefix):]: digest(archive.read(name)) for name in names}
        assert actual == expected, "Packaged resources differ: " + str(package)
        packages[str(package)] = digest(package.read_bytes())
    print(json.dumps({"marker": markers[0], "files": expected, "packages": packages}, indent=2))


if __name__ == "__main__":
    main()
