# Caspian for Firefox (Mozilla Firefox Browser Extension)

Caspian is a lightweight, privacy-focused productivity suite ported specifically for Mozilla Firefox (Manifest V3).

It eliminates typing lag in long AI conversations via real-time DOM pruning, provides universal playback speed control across all media sites, cleans up your YouTube homepage with feed limits and 1-click "Not interested" actions, converts temporary chats to permanent history, and provides the RippleFrame full-page scrolling screenshot and annotation studio.

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
- **Speed Cycle (<kbd>Alt</kbd> + <kbd>D</kbd>)**: Cycle through your preset playback rates.
- **Fine Adjustment (<kbd>]</kbd> / <kbd>[</kbd>)**: Increment or decrement speed by `0.25x`.
- **Dynamic Icon Badge & On-Screen HUD**: Live playback speed indicator on the toolbar icon and left-side HUD.

### 4. YouTube Home Feed Cleaner & Instant Feedback
- **Feed Limit Control**: Limit home recommendations to clean multiples of 3 (`3`, `6`, `9`, `12`, `15`, `18`, `21`, `24`, `30`, or `∞ All`).
- **1-Click "Not Interested"**: Fast floating button on video cards that automatically triggers YouTube's native dismissal menu without extra navigation.

### 5. RippleFrame: Full-Page Screenshot & Annotation Studio
- **Full-Page & Viewport Capture**: High-fidelity scrolling screenshot engine that suppresses scrollbars and freezes fixed navigation bars.
- **Interactive Annotation Studio**: Includes Crop, Privacy Blur, Blackout Boxes, Vector Arrows, Rectangles, Circles, Freehand Pen, Highlighter, and Text Labels.
- **Export Formats**: Download as **PNG**, **JPG** (with compression quality slider), or **High-Resolution Vector PDF (1.4)**.

---

## How to Install and Test in Firefox

### Option A: Load as Temporary Add-on (Development & Immediate Testing)

1. Open **Mozilla Firefox**.
2. In the address bar, type `about:debugging#/runtime/this-firefox` and press <kbd>Enter</kbd>.
3. Click the **"Load Temporary Add-on..."** button.
4. Browse to this directory:
   ```
   d:\Projects\Chatgpt Pruner\caspian fox extension\
   ```
5. Select the `manifest.json` file and click **Open**.
6. The Caspian extension icon will now appear in your Firefox toolbar!

### Option B: Build a Zip / XPI Archive

You can package the extension directory into a `.zip` or `.xpi` file for distribution or signing on addons.mozilla.org (AMO):

Using PowerShell:
```powershell
Compress-Archive -Path "d:\Projects\Chatgpt Pruner\caspian fox extension\*" -DestinationPath "d:\Projects\Chatgpt Pruner\Caspian-Fox-Extension-v6.3.0.zip" -Force
```

---

## Technical Porting Specifications (Firefox MV3)

- **Manifest Version**: 3
- **Gecko ID**: `caspian-fox@extension` (configured in `browser_specific_settings.gecko`)
- **Background Runner**: Non-persistent background event script (`background.js`) providing persistent IndexedDB tab capture caches and action badge updates.
- **Xray Vision Bypass**: Utilizes `window.wrappedJSObject` and `element.wrappedJSObject` in content scripts to reliably access ChatGPT Next.js page state (`__NEXT_DATA__`) and React Fiber props under Firefox's security boundaries.
- **Styles**: Custom Firefox scrollbar properties (`scrollbar-width: thin`, `scrollbar-color`) alongside glassmorphic dark and light themes.
