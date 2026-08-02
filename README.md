# 🌊 Caspian - Chromium Extension (AI Chat Pruner, Temporary Chat Saver & Multi-Format Exporter)

> **Caspian is a lightweight, privacy-first Chromium Browser Extension (compatible with Chrome, Edge, Brave, and Opera) that eliminates typing lag in long AI conversations via DOM pruning, converts temporary chats into permanent account history, and exports multi-format transcripts (.md, .txt, .doc, .pdf) for offline reading without internet.**

---

## 📸 Interface Showcase

| Engine Control Center | Multi-Format Export Suite |
| :---: | :---: |
| ![Caspian Main UI](Caspian/images/caspian_mainUI.png) | ![Caspian Export Options](Caspian/images/caspian_exportOptions.png) |

| Site Access & Shortcut Guide | Theme & Aesthetics Manager |
| :---: | :---: |
| ![Caspian Site Control](Caspian/images/caspian_siteUI.png) | ![Caspian Settings](Caspian/images/caspian_settingsUI.png) |

> 💡 **Dev Recommendation:** *While Dark Mode is fully supported, we recommend using **Light Mode** as Caspian's signature gradient and glassmorphism look exceptionally crisp and vibrant in Light Mode!*

---

## 🚀 What's New in v6.0.0 (Major Release)

- **🛡️ DOM Extractor Fallback for Temporary & Unsaved Chats**: Automatically falls back to DOM extraction on temporary chats (`chatgpt.com/?temporary-chat=true`) where backend API conversation IDs do not exist, ensuring 100% message extraction on every chat session!
- **🖨️ 100% Complete Native PDF Export (`📕 Document PDF (.pdf)`)**: Uses strict chronological turn array mapping (`data.turns`) to export 100% of all turns (168+ pages) with native ChatGPT styling, fonts, and code containers without duplicates or teleported turns.
- **🌊 Caspian Special PDF Exporter (`🌊 Caspian PDF Format`)**: Beautiful styled PDF document with blue/green turn cards, KaTeX LaTeX math rendering ($$M = (Q, \Sigma, \delta, q_0, F)$$), and page-break avoidance for code blocks and tables.
- **🏷️ Automated Export Filename Tagging**: Exported files now automatically append standard tags:
  - Standard Exports (`.md`, `.txt`, `.doc`, `.pdf`): `[Chat Title] Caspian_Exported.[ext]`
  - Caspian PDF Format (`.pdf`): `[Chat Title] Caspian_Special_Exported.pdf`
- **🎨 Action Row UI Redesign**: Equal-width pill buttons (`🚀 Convert Chat`, `Export ▾`) and a compact icon-only pill button for `Copy`.
- **🔧 Sites & Settings Layout Repair**: Fixed tab container scoping so visible turns limiter pills sit strictly inside the Engine tab, restoring clean top layout alignment on the `Sites` and `Settings` tabs.

---

## 🌟 3 Core Pillars of Caspian

### 1. ⚡ DOM Pruning & Performance Booster (Lag Fixer)
When engaging in long conversation threads on **ChatGPT** or **Google Gemini**, browsers slow down and introduce severe typing latency due to thousands of rendered DOM elements. 
**Caspian** dynamically prunes older conversation turns from the active browser viewport using high-performance CSS (`display: none !important`). This eliminates typing lag instantly while retaining 100% of your chat history in memory. Choose from `1`, `3`, `5`, `8`, `15`, or `∞ Unlimited` visible turns!

### 2. 🛡️ Temporary Chat Saver & Permanent Converter
**Temporary Chats** in AI tools disappear forever once closed, risking data loss when a quick session evolves into an important conversation. 
Caspian automatically detects active temporary chat sessions, allowing you to convert temporary chats into permanent saved chats in your account history with 1-click!

### 3. 📥 Multi-Format Export Suite (Offline Backup & Study Notes)
Never lose your valuable chat transcripts! Caspian lets you export **both normal and temporary conversations** into 5 distinct formats. Perfect for offline reading when internet is unavailable, creating hardcopy prints, or saving study notes:
- 📄 **Markdown (`.md`)**: Complete formatted Markdown file tagged as `[Title] Caspian_Exported.md`.
- 📝 **Plain Text (`.txt`)**: Clean structured plain-text transcript log tagged as `[Title] Caspian_Exported.txt`.
- 📘 **Google Doc / Word (`.doc`)**: Editable document with heading structure (`<h1>`/`<h2>`), bold text, math equations, and automatic Google Docs mobile **Chapter Navigation Outline**, tagged as `[Title] Caspian_Exported.doc`.
- 📕 **Document PDF (`.pdf`)**: Clean native ChatGPT document view tagged as `[Title] Caspian_Exported.pdf`.
- 🌊 **Caspian PDF Format**: Styled document view with KaTeX LaTeX formula rendering, turn badges, and card formatting tagged as `[Title] Caspian_Special_Exported.pdf`.

---

## ✨ Key Features & Capabilities

- 🚀 **Temporary Chat Converter**:
  - **Convert to Permanent Saved Chat**: Extracts full conversation history from a temporary session, opens a new normal session, and injects the transcript so your AI provider saves it to your account history.
  - 📋 **1-Click Copy**: Copies structured Markdown transcripts directly to your clipboard.
- 📥 **Multi-Format Export Suite (Normal & Temporary Chats)**:
  - 📄 **Markdown (`.md`)**
  - 📝 **Plain Text (`.txt`)**
  - 📘 **Google Doc / Word (`.doc`)**
  - 📕 **Document PDF (`.pdf`)**
  - 🌊 **Caspian PDF Format**
- ⚡ **Message Turn Limit Selection**: Choose visible turn limits: `1`, `3`, `5`, `8`, `15`, or `∞ Unlimited`.
- 🌐 **Supported Sites**:
  - **ChatGPT** (`chatgpt.com`) — Auto-enabled.
  - **Google Gemini** (`gemini.google.com`) — Auto-enabled.
  - Per-site toggles available in the **Sites** tab.
- 🎨 **Aesthetics & Custom Gradient Engine**:
  - Default **Caspian** gradient palette (`#A2A9A9` to `#1B4264`).
  - Dual custom hex color pickers & smart palette importer.
  - **Quick Preset Pinning System**: Pin your favorite gradient combinations directly to your dashboard.

---

## 🔒 Privacy & Security Guarantee

- 🛡️ **100% Local Execution**: Caspian operates entirely within your browser's local sandbox using Chrome Local Storage.
- 🚫 **Zero Data Collection**: We **DO NOT** track, record, collect, or transmit any user prompts, personal data, or conversation transcripts.
- 🌐 **No Remote Servers**: Caspian makes **zero external API calls** to third-party servers. Your data stays 100% on your device.

---

## ⚠️ Work In Progress (Known Limitations)

> [!NOTE]
> - **ChatGPT**: The **Convert Chat** auto-prompt injection works seamlessly out of the box.
> - **Google Gemini (WIP)**: Due to Gemini's complex Angular `<rich-textarea>` component architecture, automatic prompt text injection on new Gemini tabs is currently a **Work In Progress (WIP)**. 
>   - *Workaround for Gemini*: Click **Copy** (or **Export**) in the Caspian popup, open a new Gemini chat, and press <kbd>Ctrl</kbd> + <kbd>V</kbd> to paste the context manually!

---

## ⌨️ Keyboard Shortcuts

| Action | Shortcut Command |
| :--- | :--- |
| **Toggle Chat Pruning** | <kbd>Ctrl</kbd> + <kbd>Shift</kbd> + <kbd>X</kbd> *(or <kbd>Ctrl</kbd> + <kbd>Shift</kbd> + <kbd>C</kbd>)* |

> 🔧 **Browser Shortcut Setup Guide**:
> If the hotkey does not trigger immediately in your browser, configure it under your browser extensions shortcuts page:
> - **Chrome**: `chrome://extensions/shortcuts`
> - **Edge**: `edge://extensions/shortcuts`
> - **Brave**: `brave://extensions/shortcuts`

---

## 📥 Installation Guide

### Option 1: Quick Release Download (Easiest & Recommended)
1. Go to the [**Releases Page**](https://github.com/code4nigel/Caspian/releases) and download **`Caspian-v6.0.0.zip`** (from `versions/` folder).
2. Extract (unzip) `Caspian-v6.0.0.zip` to a folder on your computer.
3. Open your browser and navigate to the extensions page:
   - **Chrome**: `chrome://extensions`
   - **Edge**: `edge://extensions`
   - **Brave**: `brave://extensions`
4. Turn ON **Developer Mode** using the toggle switch in the top-right corner.
5. Click **Load unpacked** and select the unzipped `Caspian` folder.
6. Open [ChatGPT](https://chatgpt.com) or [Google Gemini](https://gemini.google.com) and click the **Caspian** icon!

---

### Option 2: Clone via Git (Developers)
```bash
git clone https://github.com/code4nigel/Caspian.git
```
Then select the [`Caspian`](Caspian) directory via **Load unpacked**.

---

## 📁 Project Repository Structure

```
Caspian/
├── README.md               # Complete documentation & user guide
├── LICENSE                 # GNU General Public License v3.0 (GNU GPLv3)
├── versions/               # Packaged release archives (.zip)
│   ├── Caspian-v5.0.0.zip
│   ├── Caspian-v5.0.1.zip
│   ├── Caspian-v5.0.2.zip
│   └── Caspian-v6.0.0.zip
└── Caspian/                # Chrome / Chromium Extension Directory
    ├── manifest.json       # Extension Manifest V3 configuration (v6.0.0)
    ├── background.js       # Background service worker
    ├── content.js          # DOM observer, lag fixer, text extractor & restorer
    ├── popup.html          # Glassmorphic control panel interface
    ├── popup.css           # Design system tokens, glassmorphism & themes
    ├── popup.js            # Storage sync, multi-format exporter & preset manager
    ├── developer.png       # NigelWeb developer avatar
    ├── icon16.png          # Caspian wave toolbar icon (16x16)
    ├── icon48.png          # Caspian wave toolbar icon (48x48)
    ├── icon128.png         # Caspian wave toolbar icon (128x128)
    └── images/             # Documentation screenshots
        ├── caspian_mainUI.png
        ├── caspian_exportOptions.png
        ├── caspian_siteUI.png
        ├── caspian_settingsUI.png
        └── Caspian_darkUI.png
```

---

## 👨‍💻 Author

Created with ❤️ by **NigelWeb** ([github.com/code4nigel](https://github.com/code4nigel))  
*Lead Architect of Lsync, Caspian, and Scrobby.*

---

## 📄 License & Copyleft Guarantee

Distributed under the **GNU General Public License v3.0 (GNU GPLv3)**.  
*This software is free, copyleft, and open-source forever. Anyone is free to inspect, modify, and redistribute it under the condition that all derivative works remain free, open-source, and credit the original author (**NigelWeb**). Commercial reselling without attribution or locking down the source code is strictly prohibited by law.*

See [`LICENSE`](LICENSE) for full license details.
