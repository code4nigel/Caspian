#!/usr/bin/env bash
set -euo pipefail

# Dynamically detect Flow version from betaC/app/build.gradle.kts
FLOW_VER=$(grep 'versionName' betaC/app/build.gradle.kts | head -n1 | sed -E 's/.*"([^"]+)".*/\1/')
EXT_VER=$(node -p "try { require('./Caspian/manifest.json').version } catch(e) { '6.3.0' }")
echo "Detected Flow Version: v${FLOW_VER}, Extension Version: v${EXT_VER}"

COMMIT_MSG=$(git log -1 --pretty=%B || echo "Automated release")

# 1. Publish Caspian Flow (Beta C)
FLOW_TAG="Caspian-Flow-v${FLOW_VER}"
FLOW_TITLE="[Beta C] Caspian Flow v${FLOW_VER}"
FLOW_ASSET="versions/Caspian-Flow-v${FLOW_VER}.apk"
FLOW_NOTES="### 📱 Caspian Flow (Beta C)

${COMMIT_MSG}

- **Version Tag**: \`${FLOW_TAG}\`
- **Artifact**: \`Caspian-Flow-v${FLOW_VER}.apk\`

Download the asset below to install or use."

if [ -f "$FLOW_ASSET" ]; then
  echo "Publishing ${FLOW_TITLE}..."
  if gh release view "$FLOW_TAG" >/dev/null 2>&1; then
    gh release edit "$FLOW_TAG" --title "$FLOW_TITLE" --notes "$FLOW_NOTES" --prerelease
  else
    gh release create "$FLOW_TAG" --title "$FLOW_TITLE" --notes "$FLOW_NOTES" --prerelease
  fi
  gh release upload "$FLOW_TAG" "$FLOW_ASSET" --clobber
  echo "✓ Completed ${FLOW_TITLE}"
else
  echo "Warning: ${FLOW_ASSET} not found!"
fi

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
  if gh release view "$MOBILE_TAG" >/dev/null 2>&1; then
    gh release edit "$MOBILE_TAG" --title "$MOBILE_TITLE" --notes "$MOBILE_NOTES"
  else
    gh release create "$MOBILE_TAG" --title "$MOBILE_TITLE" --notes "$MOBILE_NOTES"
  fi
  gh release upload "$MOBILE_TAG" "$MOBILE_ASSET" --clobber
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
  if gh release view "$BETA_A_TAG" >/dev/null 2>&1; then
    gh release edit "$BETA_A_TAG" --title "$BETA_A_TITLE" --notes "$BETA_A_NOTES" --prerelease
  else
    gh release create "$BETA_A_TAG" --title "$BETA_A_TITLE" --notes "$BETA_A_NOTES" --prerelease
  fi
  gh release upload "$BETA_A_TAG" "$BETA_A_ASSET" --clobber
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
  if gh release view "$BETA_B_TAG" >/dev/null 2>&1; then
    gh release edit "$BETA_B_TAG" --title "$BETA_B_TITLE" --notes "$BETA_B_NOTES" --prerelease
  else
    gh release create "$BETA_B_TAG" --title "$BETA_B_TITLE" --notes "$BETA_B_NOTES" --prerelease
  fi
  gh release upload "$BETA_B_TAG" "$BETA_B_ASSET" --clobber
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
  if gh release view "$EXT_TAG" >/dev/null 2>&1; then
    gh release edit "$EXT_TAG" --title "$EXT_TITLE" --notes "$EXT_NOTES"
  else
    gh release create "$EXT_TAG" --title "$EXT_TITLE" --notes "$EXT_NOTES"
  fi
  gh release upload "$EXT_TAG" "$EXT_ASSET" --clobber
  echo "✓ Completed ${EXT_TITLE}"
fi

# 6. Publish Unified "🌟 Caspian Multi-Platform Suite (Latest)"
SUITE_TAG="Caspian-Latest-Suite"
SUITE_TITLE="🌟 Caspian Multi-Platform Suite (Latest)"
SUITE_NOTES="## 🌟 Caspian Multi-Platform Suite (Latest Releases)

This release bundles the **latest official builds** for all Caspian applications across Android and Desktop.

| Application | Latest Version | Platform | Asset Download |
| :--- | :--- | :--- | :--- |
| **Caspian Flow (Beta C)** | \`${FLOW_TAG}\` | Android APK | \`Caspian-Flow-v${FLOW_VER}.apk\` |
| **Caspian Mobile** | \`${MOBILE_TAG}\` | Android APK | \`Caspian-Mobile-v1.2.40.apk\` |
| **Caspian Beta A** | \`${BETA_A_TAG}\` | Android APK | \`Caspian-Beta-A-v1.2.48.apk\` |
| **Caspian Beta B** | \`${BETA_B_TAG}\` | Android APK | \`Caspian-Beta-B-v1.0.5.apk\` |
| **Caspian Extension** | \`${EXT_TAG}\` | Chromium Extension ZIP | \`Caspian-Extension-v${EXT_VER}.zip\` |

### 📦 Direct Downloads
Click on any of the attached assets below to download the latest build for your platform."

echo "Publishing ${SUITE_TITLE}..."
if gh release view "$SUITE_TAG" >/dev/null 2>&1; then
  gh release edit "$SUITE_TAG" --title "$SUITE_TITLE" --notes "$SUITE_NOTES" --latest
else
  gh release create "$SUITE_TAG" --title "$SUITE_TITLE" --notes "$SUITE_NOTES" --latest
fi

echo "Uploading all latest assets to Latest Suite..."
gh release upload "$SUITE_TAG" \
  "$FLOW_ASSET" \
  "$MOBILE_ASSET" \
  "$BETA_A_ASSET" \
  "$BETA_B_ASSET" \
  "$EXT_ASSET" \
  --clobber

# Clean up obsolete assets from Latest Suite
for old_asset in $(gh release view "$SUITE_TAG" --json assets -q '.assets[].name'); do
  case "$old_asset" in
    "Caspian-Flow-v${FLOW_VER}.apk"|"Caspian-Mobile-v1.2.40.apk"|"Caspian-Beta-A-v1.2.48.apk"|"Caspian-Beta-B-v1.0.5.apk"|"Caspian-Extension-v${EXT_VER}.zip")
      ;;
    *)
      echo "Removing obsolete asset from Latest Suite: $old_asset"
      gh release delete-asset "$SUITE_TAG" "$old_asset" --yes || true
      ;;
  esac
done

echo "🎉 Successfully published all releases and unified Latest Suite!"
