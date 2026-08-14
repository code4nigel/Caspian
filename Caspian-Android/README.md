# 🌊 Caspian Mobile - Standalone Android Application

> **Caspian Mobile is the native Android application of the Caspian productivity suite. It runs official AI web platforms (ChatGPT, Google Gemini, Claude, Grok, DeepSeek, Perplexity, Google Search, and YouTube) inside high-performance native Android WebViews enhanced with Material 3 Expressive, iOS Glassmorphism, and Samsung One UI design languages. It provides real-time DOM lag pruning, temporary chat auto-saving, Whisper speech-to-text dictation, and direct multi-format exports (.pdf, .md, .txt, .doc, .html) straight to your device storage.**

---

## 🎨 Design Fusion System

1. **Material 3 Expressive**:
   - Dynamic tonal color palettes and responsive touch feedback across all cards and sheets.
2. **iOS Glassmorphism**:
   - Translucent frosted glass backdrop blur (`backdrop-filter: blur(16px)`), luminous card borders, and glassmorphic elevations.
3. **Samsung One UI Interaction Model**:
   - **Touch-Draggable Floating Action Pod**: Free movement anywhere on screen with customizable shapes (`Squircle`, `Rounded`, `Circle`, `Full Square`).
   - **Slide-Up Bottom Control Sheet**: Rounded bottom sheet with customizable snap heights (`55vh`, `75vh`, `90vh`, `100vh`) and animation modes (`Genie`, `Slide Overlay`).
   - **Turn Limit Selector**: Quick-tap turn pills (`1`, `3`, `5`, `8`, `15`, `∞`) and 1-click action buttons (`Convert Chat`, `Export ▾`, `Copy`).

---

## 📁 Repository Structure

```
Caspian-Android/
├── AndroidManifest.xml                        # Android manifest configuration & permissions
├── app/
│   ├── build.gradle.kts                       # App-level Gradle build configuration
│   └── src/
│       └── main/
│           ├── java/com/caspian/ai/
│           │   ├── MainActivity.java          # Native Multi-WebView Activity & SoundPool engine
│           │   ├── CaspianBridge.java         # Native JS-to-Java storage & downloader bridge
│           │   └── SpeechWaveformView.java    # Animated waveform view for Whisper STT mode
│           ├── assets/
│           │   ├── mobile_control.html        # Glassmorphic One UI Bottom Control Sheet
│           │   ├── mobile_control.css         # M3 + iOS Glass + One UI design tokens
│           │   ├── mobile_control.js          # Control sheet state sync & export handlers
│           │   ├── mobile_pruner.js           # Mobile DOM Pruner & Temporary Vault observer
│           │   ├── youtube_helper.js          # YouTube PIP & floating pod video controls
│           │   └── sfx/                       # UI sound effect audio assets (.mp3, .wav)
│           └── res/                           # Android drawables, icons, and layout XMLs
└── README.md                                  # Mobile application documentation
```

---

## 📱 Key Features

- 🌟 **Multi-Engine AI Support**:
  - 🟢 **ChatGPT** (`https://chatgpt.com/`)
  - 🟣 **Google Gemini** (`https://gemini.google.com/`)
  - 🟠 **Claude** (`https://claude.ai/`)
  - ⚪ **Grok** (`https://x.ai/`)
  - 🔵 **DeepSeek** (`https://chat.deepseek.com/`)
  - 🌐 **Perplexity** (`https://perplexity.ai/`)
  - 🔍 **Google Search** (`https://google.com/`)
  - 🔴 **YouTube Player** (`https://youtube.com/`)
- ⚡ **Mobile DOM Lag Pruner**: Stops mobile keyboard and scrolling lag in large chat sessions by dynamically pruning offscreen DOM turns (`1`, `3`, `5`, `8`, `15`, `∞`).
- 🛡️ **Temporary Chat Converter**: Automatically detects temporary chat sessions and converts them into permanent saved chats in your account history.
- 📥 **Universal Multi-Format Export Engine**:
  - **Caspian Styled PDF / HTML**: Beautiful PDF print layout with KaTeX LaTeX math formulas, JetBrains Mono dark code blocks with language tags (`PYTHON`, `JAVA`), and tables.
  - **Markdown (`.md`)**: Full Markdown export.
  - **Plain Text (`.txt`)**: Clean structured text logs.
  - **Microsoft Word (`.doc`)**: Structured editable document.
  - **1-Click Copy**: Instant clipboard copy.
- ⚙️ **Configurable Export Engines**:
  - **ChatGPT**: Direct Session API, React Fiber State, DOM Layout Sweeper.
  - **Google Gemini**: DOM Layout Sweeper, Deep Component Crawler.
- 🎙️ **Whisper Speech-to-Text Dictation**: Long-press the floating action pod to dictate prompts with an animated voice waveform visualizer.
- 🔊 **Non-Interrupting Audio SFX & Volume Control**: Android `SoundPool` engine with ambient master gain scaling (`0.40f`) and customizable sound effects (`pop_button`, `pop_click`, `tap_main`, `tap_button`, `tap_alternate`).

---

## 🛠️ Building From Source

### Prerequisites
- Android Studio Ladybug / Iguana or later
- JDK 17 or JDK 21
- Android SDK (API Level 34, Min SDK 24 / Android 7.0+)

### Build Command
```bash
# Debug APK
./gradlew assembleDebug

# Release APK
./gradlew assembleRelease
```
The output APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 🔒 Privacy Guarantee

- **100% Local Execution**: All DOM pruning, temporary chat caching, and multi-format document conversions happen entirely locally inside the Android WebView sandbox.
- **Zero Tracking**: No user messages, analytics, or credentials are ever recorded or transmitted.
