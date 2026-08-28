# Caspian: AI Chat Pruner, Multi-Service Engine & Universal Document Exporter

> **Caspian** is a privacy-first AI productivity suite available as a Desktop Chromium Extension and standalone Native Android Applications (Caspian Mobile & Caspian Flow). It eliminates typing lag in massive conversations via real-time DOM pruning, converts temporary chats into permanent account history, integrates premier AI engines (ChatGPT and Google Gemini, plus Google Search and YouTube), and exports publication-ready multi-format transcripts (`.pdf`, `.md`, `.txt`, `.doc`, `.html`) with LaTeX math formulas and syntax-highlighted code blocks.

---

## Ecosystem and Platforms

| Desktop Extension (Chromium) | Caspian Flow / Mobile (Android) | Automatic Updates (Obtainium) |
| :--- | :--- | :--- |
| Injectable Extension Control Panel | Touch-Draggable Floating Action Pod & Bottom Sheet | One-click instant app updates via GitHub Releases |
| Compatible with ChatGPT & Google Gemini | Omnibox Multi-Tab Browser & Dedicated AI Docks | Import configuration included in repository |
| Multi-Format Exporters & Custom Themes | Zero-Lag DOM Pruner, Whisper STT & Video Remote | Background update notifications & direct installs |

---

## Caspian Flow Mobile Interface Showcase

<p align="center">
  <img src="Images/Capsian%20Flow/Flow_engine_example.jpg" width="31%" alt="Caspian Flow Engine Dashboard" />
  <img src="Images/Capsian%20Flow/Flow_tab_example.jpg" width="31%" alt="Caspian Flow Tabs Manager" />
  <img src="Images/Capsian%20Flow/Flow_settings_example.jpg" width="31%" alt="Caspian Flow Settings and Customization" />
</p>
<p align="center">
  <img src="Images/Capsian%20Flow/Flow_chat_AI_example.jpg" width="31%" alt="Caspian Flow AI Chat Interface" />
  <img src="Images/Capsian%20Flow/Flow_google_example.jpg" width="31%" alt="Caspian Flow Google Search Dock" />
  <img src="Images/Capsian%20Flow/Flow_Youtube_example.jpg" width="31%" alt="Caspian Flow YouTube Floating Remote" />
</p>

---

## Caspian Desktop Extension Showcase

<p align="center">
  <img src="Caspian/images/caspian_mainUI.png" width="45%" alt="Caspian Extension Engine UI" />
  <img src="Caspian/images/Caspian_darkUI.png" width="45%" alt="Caspian Dark Mode UI" />
</p>
<p align="center">
  <img src="Caspian/images/caspian_exportOptions.png" width="30%" alt="Caspian Export Options" />
  <img src="Caspian/images/caspian_siteUI.png" width="30%" alt="Caspian Active Site Options" />
  <img src="Caspian/images/caspian_settingsUI.png" width="30%" alt="Caspian Settings & Themes" />
</p>

---

## Core Features and Architecture

### 1. Real-Time DOM Pruning and Performance Booster
Long conversation threads on AI platforms introduce severe typing latency, frame drops, and browser crashes due to thousands of rendered DOM nodes. **Caspian** dynamically virtualizes offscreen conversation turns from the active viewport using high-performance CSS rules. This eliminates typing latency instantly while retaining 100% of your chat history in memory.

### 2. Temporary Chat Saver and Vault
Temporary chats disappear once closed, risking data loss when a quick query turns into valuable research. Caspian automatically detects active temporary chat sessions and enables 1-click conversion into permanent account history.

### 3. Universal Multi-Format Export Suite
Export both **normal and temporary chats** from ChatGPT and Google Gemini into 5 publication-ready formats:
- **Caspian Styled PDF / HTML**: Publication-ready document with KaTeX LaTeX formulas, syntax-highlighted code blocks with language tags (`PYTHON`, `JAVA`, `C++`, etc.), formatted tables, and conversation badges.
- **Markdown (`.md`)**: Complete Markdown transcript with code fences and headers.
- **Plain Text (`.txt`)**: Clean structured plain-text transcript log.
- **Microsoft Word / Google Docs (`.doc`)**: Editable structured document with heading outlines.
- **1-Click Copy**: Copies entire conversation directly to your clipboard.

### 4. Caspian Export Configurator
Granular control over conversation extraction engines:
- **ChatGPT Engines**:
  - `Direct Session API`: Fast, server-side raw Markdown retrieval with full equation fidelity.
  - `React Fiber State`: Deep memory extraction directly from React Fiber component tree.
  - `DOM Layout Sweeper`: Direct live DOM crawler.
- **Google Gemini Engines**:
  - `DOM Layout Sweeper`: Intelligent DOM extractor that translates Gemini web components (`<code-block>`, `<h3>`, `<table>`) into styled Caspian blocks.
  - `Deep Component Crawler`: Traverses custom shadow DOM component hierarchies.

---

## Automatic Updates via Obtainium

You can use [Obtainium](https://github.com/ImranR98/Obtainium) to receive automatic update notifications and install releases directly from GitHub without needing to manually download APKs for every update.

### Quick Setup with Import File
1. Install **Obtainium** on your Android device from the [official Obtainium repository](https://github.com/ImranR98/Obtainium).
2. Download the pre-configured import file from this repository: [`obtanium setup/Caspian_Obtainium_Setup.json`](obtanium%20setup/Caspian_Obtainium_Setup.json).
3. Open Obtainium ➔ Navigate to **Settings** ➔ Tap **Import/Export** ➔ Tap **Import configuration**.
4. Select `Caspian_Obtainium_Setup.json`.
5. All three Caspian variants (**Caspian Flow**, **Caspian Mobile**, and **Caspian Beta A**) will be automatically added with their correct release filters!

### Video Walkthrough Guide
A step-by-step video demonstration is available in the repository:
- Video Guide: [`obtanium setup/obtainium_setup_guide.mp4`](obtanium%20setup/obtainium_setup_guide.mp4)

---

## Caspian Desktop Extension (Chromium)

### Supported Browsers
- Google Chrome, Microsoft Edge, Brave, Opera, Vivaldi, Arc

### Desktop Installation Guide
1. Go to the [GitHub Releases Page](https://github.com/code4nigel/Caspian/releases) and download the latest extension release archive (`Caspian-Extension-v6.0.0.zip`).
2. Extract (unzip) the `.zip` file to a folder on your computer.
3. Open your browser and navigate to the extensions management page:
   - **Chrome**: `chrome://extensions`
   - **Edge**: `edge://extensions`
   - **Brave**: `brave://extensions`
4. Turn **ON** **Developer Mode** (toggle in top-right corner).
5. Click **Load unpacked** and select the unzipped `Caspian` folder.
6. Open [ChatGPT](https://chatgpt.com) or [Google Gemini](https://gemini.google.com) and click the Caspian extension icon.

---

## Caspian Flow and Caspian Mobile (Android)

### Mobile Highlights
- **Integrated Service Hub**: Seamlessly launch and switch between ChatGPT, Google Gemini, Google Search, and YouTube.
- **Dedicated Floating Docks**:
  - **ChatGPT & Gemini Docks**: Instant reload, message navigation step controls, limit switcher, and word finder.
  - **YouTube Floating Remote**: Custom obsidian frosted glass dropdowns for playback speed (`0.25x` to `2.0x`) and video resolution (`Auto` to `1080p`).
  - **Google Search Dock**: Quick query switcher and top/bottom page scrolling.
- **Liquid Glass Touch-Draggable Action Pod**: Move your control trigger anywhere on the screen with customizable shapes, gradient accents, and tactile SFX audio.
- **Omnibox Browser**: Integrated URL bar with instant soft keyboard summon, tab switcher, privacy shields, and clipboard search previews.
- **Samsung One UI Bottom Sheet**: Frosted glassmorphism slide-up sheet with customizable snap heights, animations, and theme tones.

### Direct APK Installation
1. Download the latest release (`.apk`) from [GitHub Releases](https://github.com/code4nigel/Caspian/releases):
   - **Caspian Flow (Beta C)**: `Caspian-Flow-v1.1.27-BetaC.apk`
   - **Caspian Mobile (Stable)**: `Caspian-Mobile-v1.2.40.apk`
   - **Caspian Beta A (Experimental)**: `Caspian-Beta-A-v1.2.48.apk`
2. Open the `.apk` file on your Android device (Android 7.0+ supported).
3. Allow "Install from Unknown Sources" if prompted.
4. Launch the application.

---

## Privacy and Security Guarantee

- **100% Local Processing**: Caspian operates entirely within your browser's local sandbox and native Android app sandbox.
- **Zero Data Collection**: No prompts, personal credentials, or conversation transcripts are tracked, collected, or transmitted.
- **Zero Third-Party Telemetry**: Caspian makes zero external tracking calls. All computation and DOM operations remain completely local to your device.

---

## Developer and License

- **Creator & Lead Developer**: **Nigel (code4nigel)**
- **Repository**: [https://github.com/code4nigel/Caspian](https://github.com/code4nigel/Caspian)
- **License**: GNU General Public License v3.0 (GPL-3.0)
