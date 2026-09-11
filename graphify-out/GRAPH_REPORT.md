# Graph Report - Chatgpt Pruner  (2026-09-10)

## Corpus Check
- 101 files · ~502,348 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 1915 nodes · 4608 edges · 121 communities (56 shown, 23 thin omitted)
- Extraction: 96% EXTRACTED · 4% INFERRED · 0% AMBIGUOUS · INFERRED: 167 edges (avg confidence: 0.82)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `73ceb434`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- android.webkit.JavascriptInterface
- MainActivity
- browser_control.js
- .dpToPx
- popup.js
- .getTabById
- CaspianBridge
- CaspianBridge
- CaspianDownloadManager
- rippleframe.js
- WaveguardShield
- betac/MainActivity.java
- MainActivity
- MainActivity
- MainActivity
- .setupLiquidGlassYouTubeRemote
- android.annotation.SuppressLint
- android.content.Context
- BookmarkManager
- GitHubUpdateManager
- FileOutputStream
- CaspianBridge
- Caspian-Android/assets/mobile_control.js
- Caspian-Beta-A/assets/mobile_control.js
- android.view.View
- Caspian: AI Chat Pruner, Universal Media Speed Engine & Productivity Suite
- HistoryManager
- pdf_viewer.js
- CaspianMediaService
- android.os.Handler
- CaskManager
- .switchTab
- .switchTab
- ChatAdapter
- ChatGPTApiClient
- .setupFloatingYouTubeRemote
- content.js
- manifest.json
- stitch_designs/caspian_ai_browser/DESIGN.md
- stitch_caspian_ai_mobile_browser/caspian_ai_browser/DESIGN.md
- stitch_dark_card/DESIGN.md
- stitch_dark_list/DESIGN.md
- Stitch_history_UI/DESIGN.md
- stitch_light_card/DESIGN.md
- stitch_light_list/DESIGN.md
- .setupFloatingPod
- SwipeableViewFlipper
- .resolve
- .onCreate
- .onCreate
- speed_content.js
- main/assets/mobile_pruner.js
- AdBlockShield
- CaspianWebView
- .showCardGridEditDialog
- android.webkit.WebView
- main/assets/youtube_helper.js
- .showCaspianCustomPopup
- 🌊 Caspian Mobile - Standalone Android Application
- Caspian-Beta-B/assets/mobile_control.js
- SpeechWaveformView
- commands
- WhisperLib
- .onBackPressed
- Caspian-Android/assets/youtube_helper.js
- background.js
- .onBackPressed
- .setupFloatingYouTubeRemote
- Caspian-Beta-A/assets/youtube_helper.js
- Caspian-Beta-B/assets/mobile_pruner.js
- Caspian-Android/assets/mobile_pruner.js
- Caspian-Beta-A/assets/mobile_pruner.js
- default_icon
- betaC/gradlew
- Caspian-Android/gradlew
- Caspian-Beta-A/gradlew
- 🌊 Caspian Mobile - Standalone Android Application
- Caspian-Beta-B/gradlew
- applyYouTubeOptimizations

## God Nodes (most connected - your core abstractions)
1. `MainActivity` - 377 edges
2. `CaspianBridge` - 150 edges
3. `MainActivity` - 114 edges
4. `MainActivity` - 114 edges
5. `CaspianBridge` - 52 edges
6. `CaspianBridge` - 52 edges
7. `MainActivity` - 51 edges
8. `CaspianDownloadManager` - 38 edges
9. `WaveguardShield` - 38 edges
10. `playSFX()` - 35 edges

## Surprising Connections (you probably didn't know these)
- `CaspianBridge` --references--> `MainActivity`  [EXTRACTED]
  Caspian-Android/app/src/main/java/com/caspian/ai/CaspianBridge.java → Caspian-Android/app/src/main/java/com/caspian/ai/MainActivity.java
- `MainActivity` --references--> `SpeechWaveformView`  [EXTRACTED]
  Caspian-Android/app/src/main/java/com/caspian/ai/MainActivity.java → Caspian-Android/app/src/main/java/com/caspian/ai/SpeechWaveformView.java
- `CaspianBridge` --references--> `MainActivity`  [EXTRACTED]
  Caspian-Beta-A/app/src/main/java/com/caspian/betaa/CaspianBridge.java → Caspian-Beta-A/app/src/main/java/com/caspian/betaa/MainActivity.java
- `MainActivity` --references--> `SpeechWaveformView`  [EXTRACTED]
  Caspian-Beta-A/app/src/main/java/com/caspian/betaa/MainActivity.java → Caspian-Beta-A/app/src/main/java/com/caspian/betaa/SpeechWaveformView.java
- `CaspianBridge` --references--> `MainActivity`  [EXTRACTED]
  Caspian-Beta-B/app/src/main/java/com/caspian/betab/CaspianBridge.java → Caspian-Beta-B/app/src/main/java/com/caspian/betab/MainActivity.java

## Import Cycles
- None detected.

## Communities (121 total, 23 thin omitted)

### Community 1 - "MainActivity"
Cohesion: 0.03
Nodes (12): android.content.res.Configuration, AudioRecord, CustomViewCallback, Handler, Override, SpeechWaveformView, WakeLock, WebView (+4 more)

### Community 2 - "browser_control.js"
Cohesion: 0.05
Nodes (81): applyCustomBg(), applyCustomGradient(), applyInterfaceDensity(), attachHarborCardListeners(), autoFetchHarborFavicon(), closeControlCasksModal(), closeHarborEditorModal(), closeHarborModal() (+73 more)

### Community 3 - ".dpToPx"
Cohesion: 0.11
Nodes (12): Button, EditText, FrameLayout, ImageButton, LinearLayout, TextView, MultiTabDragState, TabGroup (+4 more)

### Community 4 - "popup.js"
Cohesion: 0.07
Nodes (50): accentPicker, applyFontScale(), applyUiZoom(), DEFAULT_PRESETS, DEFAULTS, DEV_FACTS, escapeHtml(), exportGoogleDocFile() (+42 more)

### Community 8 - "CaspianDownloadManager"
Cohesion: 0.11
Nodes (7): CaspianDownloadManager, DownloadItem, DownloadListener, DownloadTask, Handler, JSONObject, Override

### Community 10 - "rippleframe.js"
Cohesion: 0.13
Nodes (40): applyBlurToRegion(), applyCrop(), cancelCropMode(), clearStoredCaptures(), createPdfBlobFromImage(), drawArrow(), fitToScreen(), generatePDF() (+32 more)

### Community 11 - "WaveguardShield"
Cohesion: 0.09
Nodes (5): android.webkit.WebResourceResponse, JSONObject, WebResourceResponse, OnUpdateListener, WaveguardShield

### Community 12 - "betac/MainActivity.java"
Cohesion: 0.11
Nodes (28): android.content.BroadcastReceiver, android.content.Intent, android.media.AudioRecord, android.media.MediaPlayer, android.media.SoundPool, android.net.Uri, android.os.Bundle, android.speech.SpeechRecognizer (+20 more)

### Community 13 - "MainActivity"
Cohesion: 0.09
Nodes (6): androidx.drawerlayout.widget.DrawerLayout, ChatMessage, Intent, Override, WebView, MainActivity

### Community 14 - "MainActivity"
Cohesion: 0.08
Nodes (6): AudioRecord, Intent, MediaPlayer, PermissionRequest, TextureView, MainActivity

### Community 15 - "MainActivity"
Cohesion: 0.09
Nodes (6): AudioRecord, Intent, MediaPlayer, PermissionRequest, TextureView, MainActivity

### Community 17 - "android.annotation.SuppressLint"
Cohesion: 0.12
Nodes (15): android.annotation.SuppressLint, android.graphics.PointF, android.graphics.Rect, android.widget.Button, android.widget.HorizontalScrollView, android.widget.LinearLayout, DrawingView, Button (+7 more)

### Community 18 - "android.content.Context"
Cohesion: 0.12
Nodes (11): android.content.Context, android.graphics.Canvas, android.graphics.Paint, android.util.AttributeSet, Override, Override, Paint, SpeechWaveformView (+3 more)

### Community 19 - "BookmarkManager"
Cohesion: 0.15
Nodes (3): BookmarkItem, BookmarkManager, JSONObject

### Community 20 - "GitHubUpdateManager"
Cohesion: 0.16
Nodes (6): android.app.Activity, DownloadCallback, GitHubUpdateManager, JSONObject, UpdateCheckCallback, UpdateInfo

### Community 21 - "FileOutputStream"
Cohesion: 0.11
Nodes (6): android.app.RemoteAction, android.graphics.Bitmap, Intent, FileOutputStream, Rect, RemoteAction

### Community 23 - "Caspian-Android/assets/mobile_control.js"
Cohesion: 0.19
Nodes (18): applyCustomBg(), applyCustomGradient(), getSFXFileForType(), handleCreateNewTab(), openGroupOptionsMenu(), openTabOptionsMenu(), playSFX(), renderOpenTabs() (+10 more)

### Community 24 - "Caspian-Beta-A/assets/mobile_control.js"
Cohesion: 0.19
Nodes (18): applyCustomBg(), applyCustomGradient(), getSFXFileForType(), handleCreateNewTab(), openGroupOptionsMenu(), openTabOptionsMenu(), playSFX(), renderOpenTabs() (+10 more)

### Community 25 - "android.view.View"
Cohesion: 0.26
Nodes (6): android.view.View, androidx.dynamicanimation.animation.SpringAnimation, CaspianPhysics, SpringAnimation, ViewProperty, WeakHashMap

### Community 26 - "Caspian: AI Chat Pruner, Universal Media Speed Engine & Productivity Suite"
Cohesion: 0.09
Nodes (22): 1-Click Setup with Configuration File, 1. RippleFrame: Full-Page Scrolling Screenshot & Annotation Studio, 2. Real-Time AI Chat DOM Pruning, 3. Flow Speed: Universal Media Playback Controller, 4. Universal Transcript Exporter, 4. YouTube Home Feed Cleaner & Instant Feedback, 5. Temporary Chat Vault, 6. Settings Backup & Migration (JSON Import / Export) (+14 more)

### Community 27 - "HistoryManager"
Cohesion: 0.13
Nodes (5): android.database.sqlite.SQLiteDatabase, android.database.sqlite.SQLiteOpenHelper, HistoryEntry, HistoryManager, Override

### Community 28 - "pdf_viewer.js"
Cohesion: 0.23
Nodes (20): calculateFitWidthScale(), cancelAllRenderTasks(), clearSearchHighlights(), createPagePlaceholders(), executeSearch(), hideAiMenu(), init(), jumpToPage() (+12 more)

### Community 29 - "CaspianMediaService"
Cohesion: 0.17
Nodes (9): android.app.Notification, android.app.Service, android.os.IBinder, CaspianMediaService, Handler, Intent, Override, WakeLock (+1 more)

### Community 31 - "android.os.Handler"
Cohesion: 0.18
Nodes (8): android.app.NotificationManager, android.content.SharedPreferences, android.os.Handler, android.widget.FrameLayout, SearchSuggestionService, SuggestionCallback, java.io.FileOutputStream, org.json.JSONObject

### Community 32 - "CaskManager"
Cohesion: 0.27
Nodes (3): CaskItem, CaskManager, JSONObject

### Community 35 - "ChatAdapter"
Cohesion: 0.19
Nodes (9): Adapter, android.view.ViewGroup, androidx.annotation.NonNull, androidx.recyclerview.widget.RecyclerView, ChatAdapter, ChatViewHolder, Override, io.noties.markwon.Markwon (+1 more)

### Community 36 - "ChatGPTApiClient"
Cohesion: 0.20
Nodes (6): ChatGPTApiClient, Handler, ModelItem, ModelsCallback, StreamCallback, okhttp3.OkHttpClient

### Community 38 - "content.js"
Cohesion: 0.25
Nodes (11): applyTurbo(), checkAndRestoreTransferContext(), clearAllPruning(), domObserver, extractConversationData(), getChatTitle(), getTopLevelTurns(), isSiteDisabled() (+3 more)

### Community 39 - "manifest.json"
Cohesion: 0.14
Nodes (13): background, service_worker, content_scripts, description, host_permissions, icons, 128, 16 (+5 more)

### Community 40 - "stitch_designs/caspian_ai_browser/DESIGN.md"
Cohesion: 0.14
Nodes (13): Action Pods, Badges & Chips, Brand & Style, Cards, Colors, Components, Elevation & Depth, Input Fields (+5 more)

### Community 41 - "stitch_caspian_ai_mobile_browser/caspian_ai_browser/DESIGN.md"
Cohesion: 0.14
Nodes (13): Action Pods, Badges & Chips, Brand & Style, Cards, Colors, Components, Elevation & Depth, Input Fields (+5 more)

### Community 42 - "stitch_dark_card/DESIGN.md"
Cohesion: 0.14
Nodes (13): Action Pods, Badges & Chips, Brand & Style, Cards, Colors, Components, Elevation & Depth, Input Fields (+5 more)

### Community 43 - "stitch_dark_list/DESIGN.md"
Cohesion: 0.14
Nodes (13): Action Pods, Badges & Chips, Brand & Style, Cards, Colors, Components, Elevation & Depth, Input Fields (+5 more)

### Community 44 - "Stitch_history_UI/DESIGN.md"
Cohesion: 0.14
Nodes (13): Action Pods, Badges & Chips, Brand & Style, Cards, Colors, Components, Elevation & Depth, Input Fields (+5 more)

### Community 45 - "stitch_light_card/DESIGN.md"
Cohesion: 0.14
Nodes (13): Action Pods, Badges & Chips, Brand & Style, Cards, Colors, Components, Elevation & Depth, Input Fields (+5 more)

### Community 46 - "stitch_light_list/DESIGN.md"
Cohesion: 0.14
Nodes (13): Action Pods, Badges & Chips, Brand & Style, Cards, Colors, Components, Elevation & Depth, Input Fields (+5 more)

### Community 47 - ".setupFloatingPod"
Cohesion: 0.26
Nodes (3): android.graphics.RectF, CabRadialMenuView, OnRadialActionSelectedListener

### Community 48 - "SwipeableViewFlipper"
Cohesion: 0.27
Nodes (4): android.widget.ViewFlipper, Override, OnPageChangeListener, SwipeableViewFlipper

### Community 49 - ".resolve"
Cohesion: 0.24
Nodes (7): AICommandRouter, RouteResult, SearchEngine, BING, BRAVE, DUCKDUCKGO, GOOGLE

### Community 54 - "speed_content.js"
Cohesion: 0.29
Nodes (9): applySpeedToAllMedia(), attachMediaListeners(), cycleToNextSpeed(), injectHudStyles(), loadConfig(), observeMedia(), parseCycleList(), setSpeed() (+1 more)

### Community 55 - "main/assets/mobile_pruner.js"
Cohesion: 0.35
Nodes (9): applyPruningDirect(), clearAllPruning(), ensurePrunerStyles(), findVisibleCenterTurnIndex(), getTopLevelTurns(), initObserver(), loadState(), onScrollHandler() (+1 more)

### Community 60 - "main/assets/youtube_helper.js"
Cohesion: 0.28
Nodes (4): cleanYouTubeData(), handleSettingsInteraction(), reparentFsMenus(), set()

### Community 63 - "🌊 Caspian Mobile - Standalone Android Application"
Cohesion: 0.22
Nodes (8): Build Command, 🛠️ Building From Source, 🌊 Caspian Mobile - Standalone Android Application, 🎨 Design Fusion System, 📱 Key Features, Prerequisites, 🔒 Privacy Guarantee, 📁 Repository Structure

### Community 64 - "Caspian-Beta-B/assets/mobile_control.js"
Cohesion: 0.33
Nodes (6): renderOpenTabs(), restoreSavedSettings(), setTheme(), syncAppVersion(), syncHostPageTheme(), updateDebugRecUI()

### Community 65 - "SpeechWaveformView"
Cohesion: 0.29
Nodes (3): Override, Paint, SpeechWaveformView

### Community 66 - "commands"
Cohesion: 0.29
Nodes (8): commands, reset-colors, toggle-feature, description, suggested_key, default, description, suggested_key

### Community 70 - "background.js"
Cohesion: 0.43
Nodes (5): captureTabWithQuotaRetry(), performRippleFrameCapture(), saveCaptureToDB(), studioTabIds, updateSpeedBadge()

### Community 74 - "Caspian-Beta-B/assets/mobile_pruner.js"
Cohesion: 0.52
Nodes (6): applyPruning(), debouncedApplyPruning(), getTopLevelTurns(), loadState(), queryShadowSelectorAll(), startObserver()

### Community 75 - "Caspian-Android/assets/mobile_pruner.js"
Cohesion: 0.60
Nodes (5): applyPruning(), debouncedApplyPruning(), getTopLevelTurns(), loadState(), startObserver()

### Community 77 - "Caspian-Beta-A/assets/mobile_pruner.js"
Cohesion: 0.60
Nodes (5): applyPruning(), debouncedApplyPruning(), getTopLevelTurns(), loadState(), startObserver()

### Community 78 - "default_icon"
Cohesion: 0.33
Nodes (6): action, default_icon, default_popup, 128, 16, 48

### Community 80 - "betaC/gradlew"
Cohesion: 0.70
Nodes (4): gradlew script, die(), save(), warn()

### Community 81 - "Caspian-Android/gradlew"
Cohesion: 0.70
Nodes (4): gradlew script, die(), save(), warn()

### Community 82 - "Caspian-Beta-A/gradlew"
Cohesion: 0.70
Nodes (4): gradlew script, die(), save(), warn()

### Community 83 - "🌊 Caspian Mobile - Standalone Android Application"
Cohesion: 0.40
Nodes (4): 🌊 Caspian Mobile - Standalone Android Application, 🎨 Design Fusion System, 📱 Features, 📁 Repository Structure (Decoupled Android App)

### Community 84 - "Caspian-Beta-B/gradlew"
Cohesion: 0.70
Nodes (4): gradlew script, die(), save(), warn()

## Knowledge Gaps
- **147 isolated node(s):** `studioTabIds`, `manifest_version`, `name`, `version`, `description` (+142 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 391 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **23 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `MainActivity` connect `MainActivity` to `android.webkit.JavascriptInterface`, `.dpToPx`, `.getTabById`, `.updateOmniboxState`, `WaveguardShield`, `betac/MainActivity.java`, `.setupFloatingPod`, `.setupLiquidGlassYouTubeRemote`, `android.annotation.SuppressLint`, `.resolve`, `BookmarkManager`, `android.content.Context`, `FileOutputStream`, `android.view.View`, `.showCardGridEditDialog`, `android.webkit.WebView`, `.showCaspianCustomPopup`, `android.os.Handler`?**
  _High betweenness centrality (0.248) - this node is a cross-community bridge._
- **Why does `MainActivity` connect `MainActivity` to `.switchTab`, `SpeechWaveformView`, `.onBackPressed`, `.setupFloatingYouTubeRemote`, `CaspianBridge`, `betac/MainActivity.java`, `.onCreate`, `android.view.View`, `android.webkit.WebView`, `.getActiveTab`, `android.os.Handler`?**
  _High betweenness centrality (0.070) - this node is a cross-community bridge._
- **Why does `CaspianBridge` connect `android.webkit.JavascriptInterface` to `MainActivity`, `WaveguardShield`, `GitHubUpdateManager`, `.getInstance`, `CaspianBridge`, `.getCasksPayloadJson`?**
  _High betweenness centrality (0.060) - this node is a cross-community bridge._
- **What connects `studioTabIds`, `manifest_version`, `name` to the rest of the system?**
  _147 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `android.webkit.JavascriptInterface` be split into smaller, more focused modules?**
  _Cohesion score 0.031996000499937505 - nodes in this community are weakly interconnected._
- **Should `MainActivity` be split into smaller, more focused modules?**
  _Cohesion score 0.03065134099616858 - nodes in this community are weakly interconnected._
- **Should `browser_control.js` be split into smaller, more focused modules?**
  _Cohesion score 0.05490296220633299 - nodes in this community are weakly interconnected._