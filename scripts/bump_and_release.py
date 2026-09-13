#!/usr/bin/env python3
import os
import re
import sys
import shutil
import subprocess
import argparse

def run_cmd(cmd, cwd=None, check=True):
    print(f"==> Running: {cmd} in {cwd or os.getcwd()}")
    res = subprocess.run(cmd, shell=True, cwd=cwd)
    if check and res.returncode != 0:
        print(f"Error: command failed with code {res.returncode}")
        sys.exit(res.returncode)
    return res.returncode

def bump_gradle_version(gradle_path):
    with open(gradle_path, 'r', encoding='utf-8') as f:
        content = f.read()

    # Find versionCode
    vc_match = re.search(r'versionCode\s*=\s*(\d+)', content)
    if not vc_match:
        print("Could not find versionCode in build.gradle.kts")
        sys.exit(1)
    old_vc = int(vc_match.group(1))
    new_vc = old_vc + 1

    # Find versionName (e.g. 1.3.10-BetaC)
    vn_match = re.search(r'versionName\s*=\s*"(\d+\.\d+\.)(\d+)(-[^"]+)"', content)
    if not vn_match:
        # Fallback regex for non-standard semver
        vn_match_simple = re.search(r'versionName\s*=\s*"([^"]+)"', content)
        if not vn_match_simple:
            print("Could not find versionName in build.gradle.kts")
            sys.exit(1)
        old_vn = vn_match_simple.group(1)
        print(f"Current versionName is '{old_vn}'. Could not auto-increment automatically.")
        sys.exit(1)

    prefix = vn_match.group(1)       # e.g. "1.3."
    patch_str = vn_match.group(2)    # e.g. "08" or "10"
    suffix = vn_match.group(3)       # e.g. "-BetaC"

    patch_len = len(patch_str)
    new_patch = int(patch_str) + 1
    new_patch_str = str(new_patch).zfill(patch_len)
    new_vn = f"{prefix}{new_patch_str}{suffix}"
    old_vn = f"{prefix}{patch_str}{suffix}"

    print(f"Bumping versionCode: {old_vc} -> {new_vc}")
    print(f"Bumping versionName: {old_vn} -> {new_vn}")

    content = re.sub(r'versionCode\s*=\s*\d+', f'versionCode = {new_vc}', content, count=1)
    content = re.sub(r'versionName\s*=\s*"[^"]+"', f'versionName = "{new_vn}"', content, count=1)

    with open(gradle_path, 'w', encoding='utf-8') as f:
        f.write(content)

    return new_vc, new_vn

def main():
    parser = argparse.ArgumentParser(description="Bump Caspian Flow version, build APK, tag and push.")
    parser.add_argument("-m", "--message", required=True, help="Commit message summary (e.g. 'feat(ui): add new tab switcher')")
    parser.add_argument("--skip-build", action="store_true", help="Skip building APK")
    parser.add_argument("--no-push", action="store_true", help="Do not push git commits and tags")
    args = parser.parse_args()

    repo_root = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
    beta_dir = os.path.join(repo_root, "betaC")
    gradle_path = os.path.join(beta_dir, "app", "build.gradle.kts")

    if not os.path.exists(gradle_path):
        print(f"Cannot find gradle file at {gradle_path}")
        sys.exit(1)

    new_vc, new_vn = bump_gradle_version(gradle_path)

    # 1. Update HTML assets
    print("==> Updating HTML assets...")
    assets_script = os.path.join(beta_dir, "build_control_assets.py")
    if os.path.exists(assets_script):
        run_cmd(f'python "{assets_script}"', cwd=beta_dir)

    # 2. Build APK
    flow_apk_name = f"Caspian-Flow-v{new_vn}.apk"
    beta_apk_dest = os.path.join(beta_dir, "versions", flow_apk_name)
    root_apk_dest = os.path.join(repo_root, "versions", flow_apk_name)

    if not args.skip_build:
        print("==> Building APK...")
        gradlew = os.path.join(beta_dir, "gradlew.bat" if os.name == "nt" else "gradlew")
        run_cmd(f'"{gradlew}" assembleDebug', cwd=beta_dir)

        built_apk = os.path.join(beta_dir, "app", "build", "outputs", "apk", "debug", "app-debug.apk")
        if not os.path.exists(built_apk):
            built_apk = os.path.join(beta_dir, "app", "build", "outputs", "apk", "release", "app-release-unsigned.apk")

        if os.path.exists(built_apk):
            os.makedirs(os.path.dirname(beta_apk_dest), exist_ok=True)
            os.makedirs(os.path.dirname(root_apk_dest), exist_ok=True)
            shutil.copyfile(built_apk, beta_apk_dest)
            shutil.copyfile(built_apk, root_apk_dest)
            print(f"Copied APK to {beta_apk_dest} and {root_apk_dest}")
        else:
            print(f"Warning: Could not find output APK at {built_apk}")

    # 3. Git commit & tag
    print("==> Committing and tagging...")
    tag_name = f"Caspian-Flow-v{new_vn}"
    commit_msg = f"{args.message} (v{new_vn})"

    run_cmd("git add -A", cwd=repo_root)
    run_cmd(f'git commit -m "{commit_msg}"', cwd=repo_root)
    run_cmd(f'git tag -a "{tag_name}" -m "{commit_msg}"', cwd=repo_root)

    # 4. Push to remote
    if not args.no_push:
        print("==> Pushing to GitHub...")
        run_cmd("git push origin main", cwd=repo_root)
        run_cmd(f'git push origin "{tag_name}"', cwd=repo_root)
        print(f"Successfully released and tagged: {tag_name}")
    else:
        print(f"Committed and tagged locally as {tag_name}. (Push skipped with --no-push)")

if __name__ == "__main__":
    main()
