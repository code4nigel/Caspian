# 🌊 Caspian Mobile - Standalone Android Application

> **Caspian Mobile is the native Android application for Caspian. It hosts official AI chat web services (ChatGPT & Gemini) inside a native Android WebView with Material 3 Expressive, iOS Glassmorphism, and Samsung One UI Pill design languages. It provides real-time DOM lag pruning, temporary chat saving, and direct multi-format exports (.md, .txt, .doc, .pdf) to Android phone storage.**

---

## 🎨 Design Fusion System

1. **Material 3 Expressive**:
   - Dynamic tonal color palettes and responsive touch feedback.
2. **iOS Glassmorphism**:
   - Frosted glass backdrop blur (`backdrop-filter: blur(16px)`), translucent card overlays, and 1px frosted glass borders.
3. **Samsung One UI Pill Language**:
   - Floating Action Pill button (`🚀 Caspian Engine`).
   - Rounded slide-up Bottom Control Sheet (`border-radius: 28px 28px 0 0`).
   - Pill turn selectors (`1`, `3`, `5`, `8`, `15`, `∞`) and action pill buttons (`Convert Chat`, `Export ▾`, `Copy`).

---

## 📁 Repository Structure (Decoupled Android App)

```
Caspian-Android/
├── AndroidManifest.xml                        # Android manifest configuration & permissions
├── app/
│   └── src/
│       └── main/
│           ├── java/com/caspian/ai/
│           │   ├── MainActivity.kt            # Native Android WebView Activity
│           │   └── CaspianBridge.kt           # Native file downloader & storage bridge
│           └── assets/
│               ├── mobile_control.html        # Glassmorphic One UI Bottom Control Sheet
│               ├── mobile_control.css         # M3 + iOS Glass + One UI CSS Tokens
│               ├── mobile_control.js          # Control Sheet state sync & export handlers
│               └── mobile_pruner.js           # Mobile DOM Pruner & Temporary Vault observer
└── README.md                                  # Mobile application documentation
```

---

## 📱 Features

- 🌟 **100% Real Official ChatGPT UI**: Runs official `https://chatgpt.com/` inside a native WebView (identical GPT-4o models, voice mode, formatting, and accounts).
- ⚡ **Mobile DOM Lag Pruner**: Prevents browser typing lag on mobile devices by auto-pruning older DOM turns (`1`, `3`, `5`, `8`, `15`, `∞`).
- 🛡️ **Temporary Chat Converter**: Saves and converts temporary chat sessions into permanent account history.
- 📥 **Native File Exporter**: Exports `.pdf`, `.md`, `.txt`, and `.doc` transcripts directly into your Android phone's **Downloads/Caspian/** folder.
- 🎨 **One UI Slide-Up Control Sheet**: Easily toggle settings, turn limits, and themes via the slide-up glassmorphic bottom sheet.
