# 🌊 Caspian - AI Chat Pruner, Temporary Chat Saver & Multi-Format Exporter

> **Caspian is a high-performance, privacy-first tool suite available as a Desktop Chromium Extension and a Standalone Native Android Application. It eliminates typing lag in long AI conversations via DOM pruning, converts temporary chats into permanent account history, and exports multi-format transcripts (.md, .txt, .doc, .html, .pdf) for offline reading without internet.**

---

## 🚀 Two Powerful Platforms: Desktop Extension & Native Android App

| 🖥️ Desktop Extension (Chrome / Edge / Brave / Opera) | 📱 Caspian Mobile (Native Android Application) |
| :--- | :--- |
| Injectable Extension Control Panel | Touch-Draggable Floating Circle & Samsung One UI Control Sheet |
| Compatible with ChatGPT & Google Gemini | Full DOM Chat Pruning on Mobile ChatGPT |
| Multi-Format Exporters & Custom Accent Pickers | OLED Black (#050811) Theme, Nigel Facts & Direct Exports |

---

## 🌟 Core Pillars of Caspian

### 1. ⚡ DOM Pruning & Performance Booster (Lag Fixer)
When engaging in long conversation threads on AI platforms, mobile and desktop browsers slow down and introduce severe typing latency due to thousands of rendered DOM elements. 
**Caspian** dynamically prunes older conversation turns from the active browser viewport using high-performance CSS (`display: none !important`). This eliminates typing lag instantly while retaining 100% of your chat history in memory. Choose from `1`, `3`, `5`, `8`, `15`, or `∞ Unlimited` visible turns!

### 2. 🛡️ Temporary Chat Saver & Permanent Converter
**Temporary Chats** disappear forever once closed, risking data loss when a quick session evolves into an important conversation. 
Caspian automatically detects active temporary chat sessions, allowing you to convert temporary chats into permanent saved chats in your account history with 1-click!

### 3. 📥 Multi-Format Export Suite (Offline Backup & Study Notes)
Never lose your valuable chat transcripts! Caspian lets you export **both normal and temporary conversations** into 5 distinct formats:
- 📄 **Markdown (`.md`)**: Formatted Markdown document tagged as `[Title]_Caspian_Exported.md`.
- 📝 **Plain Text (`.txt`)**: Clean structured plain-text transcript log.
- 📘 **Google Doc / Word (`.doc`)**: Editable document with heading structure (`<h1>`/`<h2>`), math equations, and navigation outline.
- 📕 **Document PDF (`.pdf`)**: Native document print view with standard styling.
- 🌊 **Caspian PDF / HTML Format**: Styled document view with KaTeX LaTeX formula rendering, turn badges, and card formatting.

---

## 🖥️ 1. Caspian Desktop Extension (Chromium)

### ✨ Features
- **ChatGPT & Google Gemini Support**: Automatic DOM pruning and temporary chat converter on both platforms.
- **Custom Accent Engine**: Caspian gradient (`#A2A9A9` to `#1B4264`), dual custom hex pickers, and quick preset pinning system.
- **Copy & Export Suite**: 1-click Markdown copy and 5 multi-format file exporters.

### 📥 Desktop Installation Guide
1. Download or clone this repository:
   ```bash
   git clone https://github.com/code4nigel/Caspian.git
   ```
2. Open your browser and navigate to the extensions page:
   - **Chrome**: `chrome://extensions`
   - **Edge**: `edge://extensions`
   - **Brave**: `brave://extensions`
3. Turn **ON** **Developer Mode** (top-right toggle switch).
4. Click **Load unpacked** and select the [`Caspian`](Caspian) directory.
5. Open [ChatGPT](https://chatgpt.com) or [Google Gemini](https://gemini.google.com) and tap the Caspian extension icon!

---

## 📱 2. Caspian Mobile (Standalone Android Application)

### ✨ Mobile App Features
- **Touch-Draggable Circular Floating Button**: A 48px × 48px floating toolbar icon that can be moved anywhere on your screen using touch drag.
- **Single-Tap Control Sheet**: Tap the floating button once to open or close the Samsung One UI 3-Tab Control Sheet (`Engine`, `Sites`, `Settings`).
- **Interactive Height Drag Handle**: Touch and hold the top handle on the bottom sheet to resize UI height from `25vh` to `95vh`.
- **Default OLED Black (`#050811`) Theme**: Built-in background tone selector (`OLED Black #050811`, `Pitch Black #000000`, `Bluish Dark #0a1128`, `Pure White #ffffff`).
- **💡 Interactive Nigel Facts Card**: Tap the developer card in Settings to cycle through fun facts!
- **Multi-Format Export Suite**: Export Markdown (`.md`), Plain Text (`.txt`), Word (`.doc`), and HTML/PDF documents saved directly to your phone's `Downloads/Caspian/` folder.

### 📥 Android Installation Guide (`.apk`)
1. Download the latest compiled APK from the [`versions`](versions/) folder:
   - **Latest APK:** [`versions/Caspian-Mobile-v1.0.9.apk`](versions/Caspian-Mobile-v1.0.9.apk)
2. Transfer the `.apk` file to your Android phone.
3. Open your phone's File Manager and tap **`Caspian-Mobile-v1.0.9.apk`** to install.
4. Open **Caspian Mobile** to start chatting with zero typing lag!

---

## 🔒 Privacy & Security Guarantee

- 🛡️ **100% Local Execution**: Caspian operates entirely within your browser's local sandbox and native Android app sandbox.
- 🚫 **Zero Data Collection**: We **DO NOT** track, record, collect, or transmit any user prompts, personal data, or conversation transcripts.
- 🌐 **No Remote Servers**: Caspian makes **zero external API calls** to third-party servers. Your data stays 100% on your device.

---

## ⌨️ Keyboard Shortcuts (Desktop Extension)

| Action | Shortcut Command |
| :--- | :--- |
| **Toggle Chat Pruning** | <kbd>Ctrl</kbd> + <kbd>Shift</kbd> + <kbd>X</kbd> *(or <kbd>Ctrl</kbd> + <kbd>Shift</kbd> + <kbd>C</kbd>)* |

---

## 📁 Repository Structure

```
Caspian/
├── README.md               # Complete documentation & user guide
├── LICENSE                 # GNU General Public License v3.0 (GNU GPLv3)
├── Caspian/                # Chrome / Chromium Extension Directory
│   ├── manifest.json       # Extension Manifest V3 configuration
│   ├── background.js       # Background service worker
│   ├── content.js          # DOM observer, lag fixer, text extractor
│   ├── popup.html          # Glassmorphic control panel interface
│   ├── popup.css           # Design system tokens & themes
│   ├── popup.js            # Storage sync & multi-format exporter
│   └── developer.png       # NigelWeb developer avatar
├── Caspian-Android/        # Standalone Native Android Application Project
│   ├── app/                # Android native source code (Java, WebView, CaspianBridge)
│   ├── assets/             # Mobile Control Sheet (HTML, CSS, JS, Pruner)
│   └── gradlew.bat         # Gradle build script
└── versions/               # Packaged APKs & Extension releases
    ├── Caspian-Mobile-v1.0.9.apk
    └── Caspian-v6.0.0.zip
```

---

## 👨‍💻 Author

Created with ❤️ by **NigelWeb** ([github.com/code4nigel](https://github.com/code4nigel))  
*Lead Architect of Lsync, Caspian, and Scrobby.*

---

## 📄 License & Copyleft Guarantee

Distributed under the **GNU General Public License v3.0 (GNU GPLv3)**.  
*This software is free, copyleft, and open-source forever. Anyone is free to inspect, modify, and redistribute it under the condition that all derivative works remain free, open-source, and credit the original author (**NigelWeb**).*

See [`LICENSE`](LICENSE) for full license details.
