#!/usr/bin/env bash
set -euo pipefail

# Resilient command runner with exponential backoff for GitHub API/network calls
run_with_retry() {
  local retries=4
  local delay=3
  local count=0
  until "$@"; do
    local exit_code=$?
    count=$((count + 1))
    if [ $count -lt $retries ]; then
      echo "Command failed (exit $exit_code). Retrying in ${delay}s... (Attempt $count of $retries)"
      sleep $delay
      delay=$((delay * 2))
    else
      echo "Command failed permanently after $retries attempts: $*"
      return $exit_code
    fi
  done
}

# Dynamically detect Flow version from betaC/app/build.gradle.kts
FLOW_VER=$(grep 'versionName' betaC/app/build.gradle.kts | head -n1 | sed -E 's/.*"([^"]+)".*/\1/')
EXT_VER=$(node -p "try { require('./Caspian/manifest.json').version } catch(e) { '6.3.0' }")
FOX_VER=$(node -p "try { require('./caspian fox extension/manifest.json').version } catch(e) { '6.3.0' }")
echo "Detected Flow Version: v${FLOW_VER}, Chromium Extension Version: v${EXT_VER}, Firefox Extension Version: v${FOX_VER}"

COMMIT_MSG=$(git log -1 --pretty=%B || echo "Automated release")

# 1. Publish Caspian Flow (Beta C) under BOTH Caspian-Flow-v* and v* tags
FLOW_TAG="Caspian-Flow-v${FLOW_VER}"
FLOW_TITLE="[Beta C] Caspian Flow v${FLOW_VER}"
FLOW_ASSET="versions/Caspian-Flow-v${FLOW_VER}.apk"
FLOW_ALT_ASSET="versions/app-v${FLOW_VER}.apk"
FLOW_NOTES="### 📱 Caspian Flow (Beta C)

${COMMIT_MSG}

- **Version Tag**: \`${FLOW_TAG}\`
- **Artifact**: \`Caspian-Flow-v${FLOW_VER}.apk\`

Download the asset below to install or use."

publish_flow_release() {
  local tag="$1"
  local title="$2"
  if [ -f "$FLOW_ASSET" ]; then
    echo "Publishing ${title} for tag ${tag}..."
    if run_with_retry gh release view "$tag" >/dev/null 2>&1; then
      run_with_retry gh release edit "$tag" --title "$title" --notes "$FLOW_NOTES" --prerelease
    else
      run_with_retry gh release create "$tag" --title "$title" --notes "$FLOW_NOTES" --prerelease
    fi
    run_with_retry gh release upload "$tag" "$FLOW_ASSET" --clobber
    if [ -f "$FLOW_ALT_ASSET" ]; then
      run_with_retry gh release upload "$tag" "$FLOW_ALT_ASSET" --clobber
    fi
    echo "✓ Completed ${title} (${tag})"
  else
    echo "Warning: ${FLOW_ASSET} not found!"
  fi
}

publish_flow_release "$FLOW_TAG" "$FLOW_TITLE"
publish_flow_release "v${FLOW_VER}" "v${FLOW_VER} - Caspian Flow"

# 2. Publish Caspian Mobile
MOBILE_TAG="Caspian-Mobile-v1.2.40"
MOBILE_TITLE="[Mobile] Caspian Mobile v1.2.40"
MOBILE_ASSET="versions/Caspian-Mobile-v1.2.40.apk"
MOBILE_NOTES="### 📱 Caspian Mobile

Official stable Android mobile application for ChatGPT, Gemini, YouTube and Web pruning.

- **Version Tag**: \`${MOBILE_TAG}\`
- **Artifact**: \`Caspian-Mobile-v1.2.40.apk\`"

if [ -f "$MOBILE_ASSET" ]; then
  echo "Publishing ${MOBILE_TITLE}..."
  if run_with_retry gh release view "$MOBILE_TAG" >/dev/null 2>&1; then
    run_with_retry gh release edit "$MOBILE_TAG" --title "$MOBILE_TITLE" --notes "$MOBILE_NOTES"
  else
    run_with_retry gh release create "$MOBILE_TAG" --title "$MOBILE_TITLE" --notes "$MOBILE_NOTES"
  fi
  run_with_retry gh release upload "$MOBILE_TAG" "$MOBILE_ASSET" --clobber
  echo "✓ Completed ${MOBILE_TITLE}"
fi

# 3. Publish Caspian Beta A
BETA_A_TAG="Caspian-Beta-A-v1.2.48"
BETA_A_TITLE="[Beta A] Caspian Beta A v1.2.48"
BETA_A_ASSET="versions/Caspian-Beta-A-v1.2.48.apk"
BETA_A_NOTES="### 📱 Caspian Beta A

Experimental build featuring rich Gemini HTML formatter, custom SFX mapping and full-height launch.

- **Version Tag**: \`${BETA_A_TAG}\`
- **Artifact**: \`Caspian-Beta-A-v1.2.48.apk\`"

if [ -f "$BETA_A_ASSET" ]; then
  echo "Publishing ${BETA_A_TITLE}..."
  if run_with_retry gh release view "$BETA_A_TAG" >/dev/null 2>&1; then
    run_with_retry gh release edit "$BETA_A_TAG" --title "$BETA_A_TITLE" --notes "$BETA_A_NOTES" --prerelease
  else
    run_with_retry gh release create "$BETA_A_TAG" --title "$BETA_A_TITLE" --notes "$BETA_A_NOTES" --prerelease
  fi
  run_with_retry gh release upload "$BETA_A_TAG" "$BETA_A_ASSET" --clobber
  echo "✓ Completed ${BETA_A_TITLE}"
fi

# 4. Publish Caspian Beta B
BETA_B_TAG="Caspian-Beta-B-v1.0.5"
BETA_B_TITLE="[Beta B] Caspian Beta B v1.0.5"
BETA_B_ASSET="versions/Caspian-Beta-B-v1.0.5.apk"
BETA_B_NOTES="### 📱 Caspian Beta B

Compact lightweight build for rapid AI browsing and split window testing.

- **Version Tag**: \`${BETA_B_TAG}\`
- **Artifact**: \`Caspian-Beta-B-v1.0.5.apk\`"

if [ -f "$BETA_B_ASSET" ]; then
  echo "Publishing ${BETA_B_TITLE}..."
  if run_with_retry gh release view "$BETA_B_TAG" >/dev/null 2>&1; then
    run_with_retry gh release edit "$BETA_B_TAG" --title "$BETA_B_TITLE" --notes "$BETA_B_NOTES" --prerelease
  else
    run_with_retry gh release create "$BETA_B_TAG" --title "$BETA_B_TITLE" --notes "$BETA_B_NOTES" --prerelease
  fi
  run_with_retry gh release upload "$BETA_B_TAG" "$BETA_B_ASSET" --clobber
  echo "✓ Completed ${BETA_B_TITLE}"
fi

# 5. Publish Caspian Extension
EXT_TAG="Caspian-Extension-v${EXT_VER}"
EXT_TITLE="[Extension] Caspian Extension v${EXT_VER}"
EXT_ASSET="versions/Caspian-Extension-v${EXT_VER}.zip"
EXT_NOTES="### 🧩 Caspian Extension

Official Chromium Desktop Browser Extension for ChatGPT/Gemini DOM optimization, universal Flow Speed playback controller, YouTube feed limits & multi-format document exporter.

- **Version Tag**: \`${EXT_TAG}\`
- **Artifact**: \`Caspian-Extension-v${EXT_VER}.zip\`"

if [ -f "$EXT_ASSET" ]; then
  echo "Publishing ${EXT_TITLE}..."
  if run_with_retry gh release view "$EXT_TAG" >/dev/null 2>&1; then
    run_with_retry gh release edit "$EXT_TAG" --title "$EXT_TITLE" --notes "$EXT_NOTES"
  else
    run_with_retry gh release create "$EXT_TAG" --title "$EXT_TITLE" --notes "$EXT_NOTES"
  fi
  run_with_retry gh release upload "$EXT_TAG" "$EXT_ASSET" --clobber
  echo "✓ Completed ${EXT_TITLE}"
fi

# 6. Publish Caspian Firefox Extension
FOX_TAG="Caspian-Fox-Extension-v${FOX_VER}"
FOX_TITLE="[Firefox Extension] Caspian Firefox Extension v${FOX_VER}"
FOX_ASSET="versions/Caspian-Fox-Extension-v${FOX_VER}.zip"
FOX_NOTES="### 🦊 Caspian for Firefox (Mozilla Firefox Extension)

Official Mozilla Firefox Desktop Browser Extension (Manifest V3) for ChatGPT/Gemini DOM optimization, universal Flow Speed playback controller, YouTube feed limits, Temporary Chat Vault with session authentication, and RippleFrame full-page screenshot studio.

- **Version Tag**: \`${FOX_TAG}\`
- **Artifact**: \`Caspian-Fox-Extension-v${FOX_VER}.zip\`

#### 🦊 How to Install in Firefox
1. Download \`Caspian-Fox-Extension-v${FOX_VER}.zip\` and unzip it.
2. Open Firefox and go to \`about:debugging#/runtime/this-firefox\`.
3. Click **Load Temporary Add-on...** and select \`manifest.json\` (or select the .zip archive)."

if [ -f "$FOX_ASSET" ]; then
  echo "Publishing ${FOX_TITLE}..."
  if run_with_retry gh release view "$FOX_TAG" >/dev/null 2>&1; then
    run_with_retry gh release edit "$FOX_TAG" --title "$FOX_TITLE" --notes "$FOX_NOTES"
  else
    run_with_retry gh release create "$FOX_TAG" --title "$FOX_TITLE" --notes "$FOX_NOTES"
  fi
  run_with_retry gh release upload "$FOX_TAG" "$FOX_ASSET" --clobber
  echo "✓ Completed ${FOX_TITLE}"
fi

# 7. Publish Unified "🌟 Caspian Multi-Platform Suite (Latest)"
SUITE_TAG="Caspian-Latest-Suite"
SUITE_TITLE="🌟 Caspian Multi-Platform Suite (Latest)"
SUITE_NOTES="## 🌟 Caspian Multi-Platform Suite (Latest Releases)

This release bundles the **latest official builds** for all Caspian applications across Android, Chromium, and Firefox.

| Application | Latest Version | Platform | Asset Download |
| :--- | :--- | :--- | :--- |
| **Caspian Flow (Beta C)** | \`${FLOW_TAG}\` | Android APK | \`Caspian-Flow-v${FLOW_VER}.apk\` |
| **Caspian Mobile** | \`${MOBILE_TAG}\` | Android APK | \`Caspian-Mobile-v1.2.40.apk\` |
| **Caspian Beta A** | \`${BETA_A_TAG}\` | Android APK | \`Caspian-Beta-A-v1.2.48.apk\` |
| **Caspian Beta B** | \`${BETA_B_TAG}\` | Android APK | \`Caspian-Beta-B-v1.0.5.apk\` |
| **Caspian Extension (Chromium)** | \`${EXT_TAG}\` | Chromium Extension ZIP | \`Caspian-Extension-v${EXT_VER}.zip\` |
| **Caspian Extension (Firefox)** | \`${FOX_TAG}\` | Firefox Extension ZIP | \`Caspian-Fox-Extension-v${FOX_VER}.zip\` |

### 📦 Direct Downloads
Click on any of the attached assets below to download the latest build for your platform."

echo "Publishing ${SUITE_TITLE}..."
if run_with_retry gh release view "$SUITE_TAG" >/dev/null 2>&1; then
  run_with_retry gh release edit "$SUITE_TAG" --title "$SUITE_TITLE" --notes "$SUITE_NOTES" --latest
else
  run_with_retry gh release create "$SUITE_TAG" --title "$SUITE_TITLE" --notes "$SUITE_NOTES" --latest
fi

echo "Uploading all latest assets to Latest Suite..."
run_with_retry gh release upload "$SUITE_TAG" \
  "$FLOW_ASSET" \
  "$MOBILE_ASSET" \
  "$BETA_A_ASSET" \
  "$BETA_B_ASSET" \
  "$EXT_ASSET" \
  "$FOX_ASSET" \
  --clobber

# Clean up obsolete assets from Latest Suite
for old_asset in $(gh release view "$SUITE_TAG" --json assets -q '.assets[].name' 2>/dev/null || true); do
  case "$old_asset" in
    "Caspian-Flow-v${FLOW_VER}.apk"|"app-v${FLOW_VER}.apk"|"Caspian-Mobile-v1.2.40.apk"|"Caspian-Beta-A-v1.2.48.apk"|"Caspian-Beta-B-v1.0.5.apk"|"Caspian-Extension-v${EXT_VER}.zip"|"Caspian-Fox-Extension-v${FOX_VER}.zip")
      ;;
    *)
      echo "Removing obsolete asset from Latest Suite: $old_asset"
      gh release delete-asset "$SUITE_TAG" "$old_asset" --yes 2>/dev/null || true
      ;;
  esac
done

echo "🎉 Successfully published all releases and unified Latest Suite!"
