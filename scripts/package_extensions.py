#!/usr/bin/env python3
"""
package_extensions.py
Utility script to package Caspian Chromium and Firefox extensions into .zip archives,
automatically locating version numbers from manifest.json and saving outputs directly into
'versions/Extension Versions/'.
"""

import os
import json
import zipfile
import shutil

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DEST_DIR = os.path.join(REPO_ROOT, "versions", "Extension Versions")

def get_manifest_version(dir_path):
    manifest_file = os.path.join(dir_path, "manifest.json")
    if not os.path.exists(manifest_file):
        raise FileNotFoundError(f"manifest.json not found in {dir_path}")
    with open(manifest_file, "r", encoding="utf-8") as f:
        data = json.load(f)
    return data.get("version", "1.0.0")

def zip_folder(source_dir, zip_filepath):
    os.makedirs(os.path.dirname(zip_filepath), exist_ok=True)
    with zipfile.ZipFile(zip_filepath, "w", zipfile.ZIP_DEFLATED) as zipf:
        for root, dirs, files in os.walk(source_dir):
            # Exclude git directories or existing zip files
            dirs[:] = [d for d in dirs if not d.startswith(".git")]
            for file in files:
                if file.endswith(".zip") or file.startswith(".git"):
                    continue
                file_path = os.path.join(root, file)
                arcname = os.path.relpath(file_path, source_dir)
                zipf.write(file_path, arcname)

def package_extension(name, folder_name, filename_prefix):
    source_dir = os.path.join(REPO_ROOT, folder_name)
    if not os.path.exists(source_dir):
        print(f"[-] Directory '{folder_name}' not found, skipping {name}.")
        return None

    version = get_manifest_version(source_dir)
    dest_zip = os.path.join(DEST_DIR, f"{filename_prefix}-v{version}.zip")
    print(f"[*] Packaging {name} v{version} -> {os.path.relpath(dest_zip, REPO_ROOT)}")
    zip_folder(source_dir, dest_zip)
    size_kb = os.path.getsize(dest_zip) / 1024
    print(f"[+] Created {os.path.basename(dest_zip)} ({size_kb:.1f} KB)")
    return dest_zip

def main():
    os.makedirs(DEST_DIR, exist_ok=True)
    print(f"=== Packaging Extensions to: {os.path.relpath(DEST_DIR, REPO_ROOT)} ===")
    
    # 1. Chromium Extension
    package_extension("Chromium Extension", "Caspian", "Caspian-Extension")
    
    # 2. Firefox Extension
    package_extension("Firefox Extension", "caspian fox extension", "Caspian-Fox-Extension")
    
    print("\nAll extension zip packages updated successfully.")

if __name__ == "__main__":
    main()
