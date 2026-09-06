#!/usr/bin/env python3
"""
sync_assets_to_betaD.py
Syncs UI assets (browser_control HTML/CSS/JS, pdfjs, sfx, models) from betaC to betaD.
Run this script whenever you update betaC UI and want those upgrades in betaD (Caspian Flowy).
"""
import os
import shutil

ROOT_DIR = os.path.dirname(os.path.abspath(__file__))
SRC_ASSETS = os.path.join(ROOT_DIR, "betaC", "app", "src", "main", "assets")
DEST_ASSETS = os.path.join(ROOT_DIR, "betaD", "app", "src", "main", "assets")

def sync_assets():
    if not os.path.exists(SRC_ASSETS):
        print(f"Error: Source assets directory not found: {SRC_ASSETS}")
        return False

    os.makedirs(DEST_ASSETS, exist_ok=True)

    synced_count = 0
    for root, dirs, files in os.walk(SRC_ASSETS):
        rel_path = os.path.relpath(root, SRC_ASSETS)
        dest_dir = os.path.join(DEST_ASSETS, rel_path)
        os.makedirs(dest_dir, exist_ok=True)

        for file in files:
            src_file = os.path.join(root, file)
            dest_file = os.path.join(dest_dir, file)

            # Copy if missing or modified
            if not os.path.exists(dest_file) or os.path.getmtime(src_file) > os.path.getmtime(dest_file):
                shutil.copy2(src_file, dest_file)
                synced_count += 1

    print(f" Successfully synced {synced_count} asset files from betaC to betaD!")
    return True

if __name__ == "__main__":
    sync_assets()
