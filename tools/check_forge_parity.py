"""Verify the pinned Forge/Fabric Java source inventory without starting Minecraft."""

import argparse
import hashlib
import json
from pathlib import Path
import re
import subprocess
import sys


def digest(content):
    return hashlib.sha256(content.replace(b"\r\n", b"\n")).hexdigest()


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--forge-repo", type=Path, required=True)
    parser.add_argument("--manifest", type=Path)
    arguments = parser.parse_args()
    root = Path(__file__).resolve().parents[1]
    manifest_path = arguments.manifest or root / "docs" / "forge-source-parity.json"
    manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
    commit = manifest["forgeCommit"]
    if not re.fullmatch(r"[0-9a-f]{40}", commit):
        raise ValueError("The manifest must pin a full Forge commit SHA")

    forge_repo = arguments.forge_repo.resolve()

    def git(*command):
        return subprocess.check_output(["git", "-C", str(forge_repo), *command])

    source_root = "src/main/java"
    forge_files = {path for path in git("ls-tree", "-r", "--name-only", commit, "--", source_root)
                   .decode("utf-8").splitlines() if path.endswith(".java")}
    fabric_files = {path.relative_to(root).as_posix()
                    for path in (root / source_root).rglob("*.java")}
    entries = manifest["files"]
    listed = [entry["path"] for entry in entries]
    if len(set(listed)) != len(listed):
        raise ValueError("Duplicate source paths in parity manifest")
    if set(listed) != forge_files or set(listed) != fabric_files:
        raise ValueError("Unreviewed source inventory difference: " + json.dumps({
            "forgeMissingFromManifest": sorted(forge_files - set(listed)),
            "fabricMissingFromManifest": sorted(fabric_files - set(listed)),
            "absentFromForge": sorted(set(listed) - forge_files),
            "absentFromFabric": sorted(set(listed) - fabric_files),
        }))
    counts = {"shared": 0, "adapted": 0}
    for entry in entries:
        path = entry["path"]
        forge_hash = digest(git("show", f"{commit}:{path}"))
        fabric_hash = digest((root / path).read_bytes())
        if forge_hash != entry["forgeSha256"] or fabric_hash != entry["fabricSha256"]:
            raise ValueError(f"Source changed without parity review: {path}")
        mode = entry["mode"]
        if mode not in counts or (mode == "shared" and forge_hash != fabric_hash):
            raise ValueError(f"Invalid source parity classification: {path}")
        if mode == "adapted" and not entry.get("reason"):
            raise ValueError(f"Missing Fabric adaptation explanation: {path}")
        counts[mode] += 1
    baseline = manifest["forgeBaseline"]
    if not re.fullmatch(r"[0-9a-f]{40}", baseline):
        raise ValueError("The manifest must pin a full Forge baseline commit SHA")
    changed = set(git("-c", "core.quotepath=false", "diff", "--name-only", baseline, commit)
                  .decode("utf-8").splitlines())
    ownership = manifest["forgeChanges"]
    ownership_paths = [entry["path"] for entry in ownership]
    if len(ownership_paths) != len(set(ownership_paths)) or set(ownership_paths) != changed:
        raise ValueError("A Forge change is missing from the ownership/port checklist")
    if any(not entry.get("ownership") or not entry.get("fabricHandling") for entry in ownership):
        raise ValueError("Every Forge change must explain its source ownership and Fabric handling")
    print(f"Forge {commit}: {len(entries)} Java sources covered; "
          f"{counts['shared']} shared, {counts['adapted']} reviewed Fabric adaptations; "
          f"{len(changed)} Forge changed paths accounted for.")


if __name__ == "__main__":
    try:
        main()
    except (OSError, ValueError, KeyError, subprocess.CalledProcessError) as error:
        print(f"Parity verification failed: {error}", file=sys.stderr)
        sys.exit(1)
