# 🌊 Caspian - AI Chat Pruner, Multi-Service Engine & Universal Document Exporter

> **Caspian is a privacy-first AI productivity suite available as a Desktop Chromium Extension and a Standalone Native Android Application. It eliminates typing lag in massive conversations via real-time DOM pruning, converts temporary chats into permanent account history, supports multiple premier AI engines (ChatGPT, Google Gemini, Claude, Grok, DeepSeek, Perplexity, Google Search, and YouTube), and exports professional multi-format transcripts (.pdf, .md, .txt, .doc, .html) with KaTeX LaTeX math formulas and syntax-highlighted code blocks.**

---

## 🚀 Two Powerful Platforms

| 🖥️ Desktop Extension (Chrome / Edge / Brave / Opera) | 📱 Caspian Mobile (Native Android Application) |
| :--- | :--- |
| Injectable Extension Control Panel | Touch-Draggable Floating Action Pod & Samsung One UI Sheet |
| Compatible with ChatGPT & Google Gemini | Chrome-Style Multi-WebView Tab Manager (Zero Memory Leak) |
| Multi-Format Exporters & Custom Accent Engine | Real-Time Whisper STT Voice Mode, Audio SFX & Video Controls |

---

## 📸 Interface Showcase

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

## 🌟 Core Features & Architecture

### 1. ⚡ Real-Time DOM Pruning & Performance Booster
Long conversation threads on AI platforms introduce severe typing lag and frame drops due to thousands of rendered DOM elements. 
**Caspian** dynamically prunes offscreen conversation turns from the active viewport using high-performance CSS (`display: none !important`). This eliminates typing latency instantly while retaining 100% of your chat history in memory. Choose from `1`, `3`, `5`, `8`, `15`, or `∞ Unlimited` visible turns!

### 2. 🛡️ Temporary Chat Saver & Vault
Temporary chats disappear once closed, risking data loss when a quick query turns into valuable research. Caspian automatically detects active temporary chat sessions and enables 1-click conversion into permanent account history.

### 3. 📥 Universal Multi-Format Export Suite
Export both **normal and temporary chats** from ChatGPT and Google Gemini into 5 publication-ready formats:
- 📕 **Caspian Styled PDF / HTML**: Publication-ready document with KaTeX LaTeX formulas, dark syntax-highlighted code blocks with language tags (`PYTHON`, `JAVA`, `C++`), clean tables, and turn badges.
- 📄 **Markdown (`.md`)**: Full Markdown transcript with code fences and headers.
- 📝 **Plain Text (`.txt`)**: Clean structured plain-text transcript log.
- 📘 **Microsoft Word / Google Docs (`.doc`)**: Editable structured document with heading outlines.
- 📋 **1-Click Copy**: Copies entire conversation directly to your clipboard.

### 4. ⚙️ Caspian Export Configurator
Granular control over how conversations are extracted:
- **ChatGPT Engines**:
  - `Direct Session API`: Fast, server-side raw Markdown retrieval with full equation fidelity.
  - `React Fiber State`: Deep memory extraction directly from React Fiber component tree.
  - `DOM Layout Sweeper`: Direct live DOM crawler.
- **Google Gemini Engines**:
  - `DOM Layout Sweeper`: Intelligent DOM extractor that translates Gemini web components (`<code-block>`, `<h3>`, `<table>`) into styled Caspian blocks.
  - `Deep Component Crawler`: Traverses custom shadow DOM component hierarchies.

---

## 🖥️ 1. Caspian Desktop Extension (Chromium)

### Supported Browsers
- Google Chrome, Microsoft Edge, Brave, Opera, Vivaldi, Arc

### 📥 Desktop Installation Guide
1. Go to the [**GitHub Releases Page**](https://github.com/code4nigel/Caspian/releases) and download the latest release archive (`.zip`).
2. Extract (unzip) the `.zip` file to a folder on your computer.
3. Open your browser and navigate to the extensions management page:
   - **Chrome**: `chrome://extensions`
   - **Edge**: `edge://extensions`
   - **Brave**: `brave://extensions`
4. Turn **ON** **Developer Mode** (toggle switch in top-right corner).
5. Click **Load unpacked** and select the unzipped `Caspian` folder.
6. Open [ChatGPT](https://chatgpt.com) or [Google Gemini](https://gemini.google.com) and click the Caspian extension icon!

---

## 📱 2. Caspian Mobile (Standalone Android Application)

### ✨ Mobile App Highlights
- **Multi-Service Hub**: Seamlessly launch and switch between:
  - 🟢 **ChatGPT** (`chatgpt.com`)
  - 🟣 **Google Gemini** (`gemini.google.com`)
  - 🟠 **Claude** (`claude.ai`)
  - ⚪ **Grok** (`x.ai`)
  - 🔵 **DeepSeek** (`chat.deepseek.com`)
  - 🌐 **Perplexity** (`perplexity.ai`)
  - 🔍 **Google Search** (`google.com`)
  - 🔴 **YouTube** (`youtube.com` with floating PIP video control pod)
- **Multi-WebView Tab Manager**: Open unlimited tabs with background state retention, custom favicons, search bar, and 1-click "Close All" protection for starred tabs.
- **Touch-Draggable Floating Action Pod**: Move your control trigger anywhere on the screen. Customize its shape (`Squircle`, `Rounded`, `Circle`, `Full Square`), gradient accents, and tap animation speeds.
- **Whisper Speech Recognition (STT)**: Long-press the floating pod for voice-to-text dictation with animated audio waveform visualizer.
- **Ambient UI SFX & SoundPool**: Gentle, non-interrupting tactile auditory feedback calibrated with ambient master gain scaling.
- **Samsung One UI Bottom Sheet**: Frosted glassmorphism slide-up sheet with customizable snap heights, animations (`Genie`, `Slide Overlay`), and theme tones (`OLED Black #050811`, `Pitch Black #000000`, `Bluish Dark #0a1128`, `Pure White #ffffff`).

### 📥 Android Installation Guide (`.apk`)
1. Download the latest **`Caspian-Mobile-v1.2.48.apk`** from [**GitHub Releases**](https://github.com/code4nigel/Caspian/releases).
2. Transfer or open the `.apk` file on your Android device (Android 7.0+ supported).
3. Allow "Install from Unknown Sources" if prompted.
4. Launch **Caspian Mobile** and enjoy zero typing lag!

---

## 🔒 Privacy & Security Guarantee

- 🛡️ **100% Local Processing**: Caspian operates entirely within your browser's local sandbox and native Android app sandbox.
- 🚫 **Zero Data Collection**: We **DO NOT** track, record, collect, or transmit any user prompts, personal credentials, or conversation transcripts.
- 🔐 **No Third-Party Telemetry**: Caspian makes **zero external tracking calls**. Your data never leaves your device.

---

## 👨‍💻 Developer & Credits

- **Creator & Lead Developer**: **Nigel (code4nigel)**
- **Repository**: [https://github.com/code4nigel/Caspian](https://github.com/code4nigel/Caspian)
- **License**: GNU General Public License v3.0 (GPL-3.0)
