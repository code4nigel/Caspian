# Caspian for Firefox (Mozilla Firefox Browser Extension)

<p align="left">
  <a href="https://addons.mozilla.org/en-US/firefox/addon/caspian-fox-extension/">
    <img src="https://img.shields.io/badge/Firefox_Add--ons-Install_Caspian-orange?style=for-the-badge&logo=firefox-browser&logoColor=white" alt="Install on Firefox Add-ons" />
  </a>
  <a href="https://github.com/code4nigel/Caspian/releases">
    <img src="https://img.shields.io/github/v/release/code4nigel/Caspian?style=for-the-badge&color=0284c7" alt="GitHub Release" />
  </a>
</p>

> **Now live on Firefox Add-ons!** You can install Caspian directly from the official store:  
> 👉 **[addons.mozilla.org/en-US/firefox/addon/caspian-fox-extension/](https://addons.mozilla.org/en-US/firefox/addon/caspian-fox-extension/)**

Caspian v6.4.7 is a Manifest V3 productivity suite for Firefox 128 or newer on desktop and Android. The development build in this folder can be loaded temporarily in desktop Firefox.

It eliminates typing lag in long AI conversations via real-time DOM pruning, provides universal playback speed control across all media sites (including Reddit, YouTube, and Shadow DOM players), cleans up your YouTube homepage with feed limits and 1-click "Not interested" actions, converts temporary chats to permanent history, and provides the RippleFrame full-page scrolling screenshot and annotation studio.

---

## Highlights & Features

### 1. Real-Time AI Chat DOM Pruning
- **Zero Typing Lag**: Virtualizes offscreen conversation turns on ChatGPT (`chatgpt.com`) and Google Gemini (`gemini.google.com`).
- **Sliding Window & Tail Pruning**: Choose between sliding viewport window or tail-only pruners.
- **Dynamic CSS Containment**: Preserves memory while keeping 100% of conversation history intact.

### 2. Temporary Chat Vault & Session Converter
- **Session Authentication & Next.js Token Extraction**: Uses Firefox-compatible `wrappedJSObject` to extract access tokens and conversation mapping trees directly from ChatGPT's internal backend API (`/backend-api/conversation/...`) and React Fiber state.
- **One-Click Convert**: Converts temporary or guest chat sessions into a permanent account thread in your history.
- **Multi-Format Transcript Exporters**: Export full conversations to **Markdown (`.md`)**, **Plain Text (`.txt`)**, **Word Doc (`.doc`)**, or **Styled PDF / HTML Document** with KaTeX math rendering and code styling.

### 3. Flow Speed: Universal Media Playback Controller
- **System-Wide Controller**: Controls `<video>` and `<audio>` playback across all websites (YouTube, Twitch, Netflix, Coursera, Vimeo, etc.).
- **Smart A/B Toggle (<kbd>Alt</kbd> + <kbd>S</kbd>)**: Instantly toggle between `1.0x` and your last customized speed.
- **Speed Cycle (<kbd>Alt</kbd> + <kbd>Shift</kbd> + <kbd>D</kbd>)**: Cycle through your preset playback rates. Click either shortcut field in Playback options and press the keys together to change it.
- **Fine Adjustment (<kbd>]</kbd> / <kbd>[</kbd>)**: Increment or decrement speed by `0.25x`.
- **Dynamic Icon Badge & On-Screen HUD**: A toolbar speed badge and a themed on-page indicator with six positions.

### 4. YouTube Customisation
- **Off by default**: Enable the feed limit or quick dismissal button independently in the Engine tab.
- **Feed Limit Control**: Limit home recommendations to clean multiples of 3 (`3`, `6`, `9`, `12`, `15`, `18`, `21`, `24`, `30`, or `∞ All`).
- **1-Click "Not Interested"**: Fast floating button on video cards that automatically triggers YouTube's native dismissal menu without extra navigation.

### 5. RippleFrame: Full-Page Screenshot & Annotation Studio
- **Full-Page & Viewport Capture**: High-fidelity scrolling screenshot engine that suppresses scrollbars and freezes fixed navigation bars.
- **Interactive Annotation Studio**: Includes Crop, Privacy Blur, Blackout Boxes, Vector Arrows, Rectangles, Circles, Freehand Pen, Highlighter, and Text Labels.
- **Export Formats**: Download as **PNG**, **JPG** with quality control, or **PDF** containing the annotated image.

---

## How to Install in Firefox

### Option 1: Official Mozilla Add-ons Store (Recommended)

Install directly with 1-click and automatic background updates:
👉 **[Install Caspian on Firefox Add-ons (AMO)](https://addons.mozilla.org/en-US/firefox/addon/caspian-fox-extension/)**

### Option 2: Load as Temporary Add-on (Development & Immediate Testing)

1. Open **Mozilla Firefox**.
2. In the address bar, type `about:debugging#/runtime/this-firefox` and press <kbd>Enter</kbd>.
3. Click the **"Load Temporary Add-on..."** button.
4. Browse to this directory:
   ```
   d:\Projects\Chatgpt Pruner\caspian fox extension\
   ```
5. Select the `manifest.json` file and click **Open**.
6. The Caspian extension icon will now appear in your Firefox toolbar!

## Package and update the Firefox listing

Run `python scripts/package_extensions.py` from the repository root. It creates the [Firefox upload ZIP](../versions/firefox/Caspian-Fox-Extension-v6.4.7.zip), with `manifest.json` at the root and the extension's readable JavaScript, HTML, and CSS.

1. Sign in to the [Firefox Add-ons Developer Hub](https://addons.mozilla.org/developers/addons) and open the existing Caspian listing.
2. Go to **Manage Status & Versions → Upload a New Version** and select the Firefox ZIP. Keep the existing Gecko ID. Review Mozilla's validation results, add release notes and reviewer instructions, then submit the version.
3. Under **Edit Product Page**, replace screenshots 1–5 with the PNGs in [Images/AMO_Showcase](../Images/AMO_Showcase). Keep their numbered order and update the captions. Save the listing.

See [Mozilla's submission guide](https://extensionworkshop.com/documentation/publish/submitting-an-add-on/) and the [v6.4.7 screenshot gallery](../README.md#caspian-v647-screenshots).

---

## Technical Porting Specifications (Firefox MV3)

- **Manifest Version**: 3
- **Gecko ID**: `caspian-fox@extension` (configured in `browser_specific_settings.gecko`)
- **Background Runner**: Non-persistent background event script (`background.js`) providing persistent IndexedDB tab capture caches and action badge updates.
- **Xray Vision Bypass**: Utilizes `window.wrappedJSObject` and `element.wrappedJSObject` in content scripts to reliably access ChatGPT Next.js page state (`__NEXT_DATA__`) and React Fiber props under Firefox's security boundaries.
- **Styles**: Custom Firefox scrollbar properties (`scrollbar-width: thin`, `scrollbar-color`) alongside glassmorphic dark and light themes.
