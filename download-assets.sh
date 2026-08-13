#!/usr/bin/env bash
set -euo pipefail

BASE="$HOME/.gradle/caches/minecraft"
VERSION_JSON="$BASE/versionJsons/1.10.2.json"
ASSETS="$BASE/assets"
OBJECTS="$ASSETS/objects"
VIRTUAL="$ASSETS/virtual/legacy"

if [[ ! -f "$VERSION_JSON" ]]; then
  echo "Missing:"
  echo "$VERSION_JSON"
  exit 1
fi

mkdir -p "$OBJECTS" "$VIRTUAL"

ASSET_INDEX_URL=$(
  python3 - "$VERSION_JSON" <<'PY'
import json
import sys

with open(sys.argv[1]) as f:
    data = json.load(f)

print(data["assetIndex"]["url"])
PY
)

echo "Asset index:"
echo "$ASSET_INDEX_URL"

TMP=$(mktemp)
trap 'rm -f "$TMP"' EXIT

curl -fL --retry 5 "$ASSET_INDEX_URL" -o "$TMP"

python3 - "$TMP" "$OBJECTS" "$VIRTUAL" <<'PY'
import json
import os
import sys
import urllib.request
import hashlib
import shutil

index_file = sys.argv[1]
objects_dir = sys.argv[2]
virtual_dir = sys.argv[3]

with open(index_file) as f:
    index = json.load(f)

objects = index["objects"]

print(f"Found {len(objects)} assets")

for i, (name, info) in enumerate(objects.items(), 1):
    sha1 = info["hash"]
    size = info["size"]

    prefix = sha1[:2]

    object_path = os.path.join(
        objects_dir,
        prefix,
        sha1
    )

    virtual_path = os.path.join(
        virtual_dir,
        name
    )

    os.makedirs(os.path.dirname(object_path), exist_ok=True)
    os.makedirs(os.path.dirname(virtual_path), exist_ok=True)

    # Check existing object.
    valid = False

    if os.path.isfile(object_path):
        if os.path.getsize(object_path) == size:
            h = hashlib.sha1()

            with open(object_path, "rb") as f:
                for chunk in iter(lambda: f.read(1024 * 1024), b""):
                    h.update(chunk)

            valid = h.hexdigest() == sha1

    if not valid:
        url = (
            "https://resources.download.minecraft.net/"
            f"{prefix}/{sha1}"
        )

        tmp = object_path + ".tmp"

        print(f"[{i}/{len(objects)}] {name}")

        urllib.request.urlretrieve(url, tmp)

        if os.path.getsize(tmp) != size:
            os.remove(tmp)
            raise RuntimeError(
                f"Size mismatch for {name}"
            )

        h = hashlib.sha1()

        with open(tmp, "rb") as f:
            for chunk in iter(lambda: f.read(1024 * 1024), b""):
                h.update(chunk)

        if h.hexdigest() != sha1:
            os.remove(tmp)
            raise RuntimeError(
                f"SHA1 mismatch for {name}"
            )

        os.replace(tmp, object_path)

    # ForgeGradle 2.2 uses the virtual/legacy layout for
    # asset indexes marked as virtual.
    if not os.path.exists(virtual_path):
        try:
            os.link(object_path, virtual_path)
        except OSError:
            shutil.copy2(object_path, virtual_path)

print("Done.")
PY
