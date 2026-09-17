# Graph Report - Chatgpt Pruner  (2026-09-16)

## Corpus Check
- 105 files · ~538,507 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 2099 nodes · 5316 edges · 124 communities (61 shown, 22 thin omitted)
- Extraction: 96% EXTRACTED · 4% INFERRED · 0% AMBIGUOUS · INFERRED: 194 edges (avg confidence: 0.81)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `d4a7aa01`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- android.webkit.JavascriptInterface
- MainActivity
- browser_control.js
- android.annotation.SuppressLint
- popup.js
- android.content.Context
- CaspianBridge
- CaspianBridge
- CaspianDownloadManager
- betac/MainActivity.java
- rippleframe.js
- .getTabById
- WhirlpoolOverlayView
- MainActivity
- MainActivity
- MainActivity
- CaspianWebView
- CaspianMediaService
- .onCreate
- BookmarkManager
- GitHubUpdateManager
- scrobby_engine.js
- CaspianBridge
- Caspian-Android/assets/mobile_control.js
- Caspian-Beta-A/assets/mobile_control.js
- Caspian: AI Chat Pruner, Universal Media Speed Engine & Productivity Suite
- HistoryManager
- pdf_viewer.js
- CaskManager
- RecentsHorizontalScrollView
- .setupFloatingYouTubeRemote
- android.webkit.WebView
- .switchTab
- .getYouTubeTab
- .getActiveOrDominantTab
- content.js
- commands
- stitch_designs/caspian_ai_browser/DESIGN.md
- stitch_caspian_ai_mobile_browser/caspian_ai_browser/DESIGN.md
- stitch_dark_card/DESIGN.md
- stitch_dark_list/DESIGN.md
- Stitch_history_UI/DESIGN.md
- stitch_light_card/DESIGN.md
- stitch_light_list/DESIGN.md
- AdBlockShield
- ChatGPTApiClient
- .resolve
- ChatAdapter
- .restoreLastClosedBatch
- android.content.Intent
- speed_content.js
- main/assets/mobile_pruner.js
- Override
- WaveguardShield
- android.view.View
- ChatMessage
- main/assets/youtube_helper.js
- .setupNativeFloatingButton
- 🌊 Caspian Mobile - Standalone Android Application
- Caspian-Beta-B/assets/mobile_control.js
- .downloadApk
- SwipeableViewFlipper
- WhisperLib
- publish-releases.sh
- Caspian-Android/assets/youtube_helper.js
- background.js
- .onCreate
- default_icon
- Caspian-Beta-A/assets/youtube_helper.js
- Caspian-Beta-B/assets/mobile_pruner.js
- Caspian-Android/assets/mobile_pruner.js
- .showBrowserActionGrid
- Caspian-Beta-A/assets/mobile_pruner.js
- bump_and_release.py
- betaC/gradlew
- Caspian-Android/gradlew
- Caspian-Beta-A/gradlew
- 🌊 Caspian Mobile - Standalone Android Application
- Caspian-Beta-B/gradlew
- applyYouTubeOptimizations
- manifest.json
- .CaspianDownloadManager
- .dpToPx

## God Nodes (most connected - your core abstractions)
1. `MainActivity` - 455 edges
2. `CaspianBridge` - 168 edges
3. `MainActivity` - 114 edges
4. `MainActivity` - 114 edges
5. `CaspianBridge` - 52 edges
6. `CaspianBridge` - 52 edges
7. `MainActivity` - 51 edges
8. `WaveguardShield` - 39 edges
9. `CaspianDownloadManager` - 38 edges
10. `playSFX()` - 37 edges

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

## Communities (124 total, 22 thin omitted)

### Community 1 - "MainActivity"
Cohesion: 0.03
Nodes (13): android.graphics.Bitmap, Canvas, AudioRecord, CustomViewCallback, Handler, Intent, LruCache, WakeLock (+5 more)

### Community 2 - "browser_control.js"
Cohesion: 0.05
Nodes (83): applyCustomBg(), applyCustomGradient(), applyInterfaceDensity(), attachHarborCardListeners(), autoFetchHarborFavicon(), closeControlCasksModal(), closeHarborEditorModal(), closeHarborModal() (+75 more)

### Community 3 - "android.annotation.SuppressLint"
Cohesion: 0.17
Nodes (3): android.annotation.SuppressLint, SoundPool, SpeechWaveformView

### Community 4 - "popup.js"
Cohesion: 0.07
Nodes (50): accentPicker, applyFontScale(), applyUiZoom(), DEFAULT_PRESETS, DEFAULTS, DEV_FACTS, escapeHtml(), exportGoogleDocFile() (+42 more)

### Community 5 - "android.content.Context"
Cohesion: 0.22
Nodes (9): android.content.Context, android.content.SharedPreferences, android.os.Handler, JSONObject, SearchSuggestionService, SuggestionCallback, java.io.FileOutputStream, java.net.HttpURLConnection (+1 more)

### Community 8 - "CaspianDownloadManager"
Cohesion: 0.13
Nodes (5): CaspianDownloadManager, DownloadItem, DownloadListener, DownloadTask, Override

### Community 9 - "betac/MainActivity.java"
Cohesion: 0.10
Nodes (32): android.animation.ValueAnimator, android.app.NotificationManager, android.content.BroadcastReceiver, android.graphics.Rect, android.media.AudioRecord, android.media.MediaPlayer, android.media.SoundPool, android.os.Bundle (+24 more)

### Community 10 - "rippleframe.js"
Cohesion: 0.13
Nodes (40): applyBlurToRegion(), applyCrop(), cancelCropMode(), clearStoredCaptures(), createPdfBlobFromImage(), drawArrow(), fitToScreen(), generatePDF() (+32 more)

### Community 11 - ".getTabById"
Cohesion: 0.08
Nodes (4): android.app.RemoteAction, CaspianMenuItem, Rect, RemoteAction

### Community 12 - "WhirlpoolOverlayView"
Cohesion: 0.13
Nodes (11): android.graphics.PointF, android.widget.Button, DrawingView, Button, HorizontalScrollView, LinearLayout, Override, TextView (+3 more)

### Community 14 - "MainActivity"
Cohesion: 0.06
Nodes (8): AudioRecord, CustomViewCallback, FrameLayout, Intent, MediaPlayer, PermissionRequest, TextureView, MainActivity

### Community 15 - "MainActivity"
Cohesion: 0.09
Nodes (6): CustomViewCallback, FrameLayout, MediaPlayer, PermissionRequest, TextureView, MainActivity

### Community 16 - "CaspianWebView"
Cohesion: 0.11
Nodes (4): CaspianWebView, Override, OnLinkLongPressListener, OnScrollStateListener

### Community 17 - "CaspianMediaService"
Cohesion: 0.15
Nodes (11): android.app.Notification, android.app.Service, android.os.IBinder, CaspianMediaService, Handler, Intent, Override, WakeLock (+3 more)

### Community 18 - ".onCreate"
Cohesion: 0.21
Nodes (3): Intent, Override, WebView

### Community 19 - "BookmarkManager"
Cohesion: 0.17
Nodes (3): BookmarkItem, BookmarkManager, JSONObject

### Community 20 - "GitHubUpdateManager"
Cohesion: 0.22
Nodes (5): android.app.Activity, GitHubUpdateManager, JSONObject, UpdateCheckCallback, UpdateInfo

### Community 21 - "scrobby_engine.js"
Cohesion: 0.14
Nodes (26): authenticateWithToken(), callLastFm(), cleanTrackInfo(), disconnect(), emitState(), generateSignature(), getRecentScrobbles(), getSettings() (+18 more)

### Community 22 - "CaspianBridge"
Cohesion: 0.08
Nodes (3): android.widget.Toast, CaspianBridge, FileOutputStream

### Community 23 - "Caspian-Android/assets/mobile_control.js"
Cohesion: 0.19
Nodes (18): applyCustomBg(), applyCustomGradient(), getSFXFileForType(), handleCreateNewTab(), openGroupOptionsMenu(), openTabOptionsMenu(), playSFX(), renderOpenTabs() (+10 more)

### Community 24 - "Caspian-Beta-A/assets/mobile_control.js"
Cohesion: 0.19
Nodes (18): applyCustomBg(), applyCustomGradient(), getSFXFileForType(), handleCreateNewTab(), openGroupOptionsMenu(), openTabOptionsMenu(), playSFX(), renderOpenTabs() (+10 more)

### Community 26 - "Caspian: AI Chat Pruner, Universal Media Speed Engine & Productivity Suite"
Cohesion: 0.09
Nodes (22): 1-Click Setup with Configuration File, 1. RippleFrame: Full-Page Scrolling Screenshot & Annotation Studio, 2. Real-Time AI Chat DOM Pruning, 3. Flow Speed: Universal Media Playback Controller, 4. Universal Transcript Exporter, 4. YouTube Home Feed Cleaner & Instant Feedback, 5. Temporary Chat Vault, 6. Settings Backup & Migration (JSON Import / Export) (+14 more)

### Community 27 - "HistoryManager"
Cohesion: 0.13
Nodes (5): android.database.sqlite.SQLiteDatabase, android.database.sqlite.SQLiteOpenHelper, HistoryEntry, HistoryManager, Override

### Community 28 - "pdf_viewer.js"
Cohesion: 0.23
Nodes (20): calculateFitWidthScale(), cancelAllRenderTasks(), clearSearchHighlights(), createPagePlaceholders(), executeSearch(), hideAiMenu(), init(), jumpToPage() (+12 more)

### Community 29 - "CaskManager"
Cohesion: 0.21
Nodes (4): CaskItem, CaskManager, JSONObject, WebView

### Community 33 - "android.webkit.WebView"
Cohesion: 0.21
Nodes (3): android.webkit.WebView, WebView, TabItem

### Community 35 - ".getYouTubeTab"
Cohesion: 0.08
Nodes (3): android.content.res.Configuration, android.support.v4.media.session.PlaybackStateCompat, Override

### Community 38 - "content.js"
Cohesion: 0.25
Nodes (11): applyTurbo(), checkAndRestoreTransferContext(), clearAllPruning(), domObserver, extractConversationData(), getChatTitle(), getTopLevelTurns(), isSiteDisabled() (+3 more)

### Community 39 - "commands"
Cohesion: 0.29
Nodes (8): commands, reset-colors, toggle-feature, description, suggested_key, default, description, suggested_key

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

### Community 48 - "ChatGPTApiClient"
Cohesion: 0.29
Nodes (5): ChatGPTApiClient, Handler, ModelItem, ModelsCallback, okhttp3.OkHttpClient

### Community 49 - ".resolve"
Cohesion: 0.24
Nodes (7): AICommandRouter, RouteResult, SearchEngine, BING, BRAVE, DUCKDUCKGO, GOOGLE

### Community 50 - "ChatAdapter"
Cohesion: 0.22
Nodes (8): Adapter, androidx.annotation.NonNull, androidx.recyclerview.widget.RecyclerView, ChatAdapter, ChatViewHolder, Override, io.noties.markwon.Markwon, ViewHolder

### Community 54 - "speed_content.js"
Cohesion: 0.29
Nodes (9): applySpeedToAllMedia(), attachMediaListeners(), cycleToNextSpeed(), injectHudStyles(), loadConfig(), observeMedia(), parseCycleList(), setSpeed() (+1 more)

### Community 55 - "main/assets/mobile_pruner.js"
Cohesion: 0.35
Nodes (9): applyPruningDirect(), clearAllPruning(), ensurePrunerStyles(), findVisibleCenterTurnIndex(), getTopLevelTurns(), initObserver(), loadState(), onScrollHandler() (+1 more)

### Community 57 - "WaveguardShield"
Cohesion: 0.08
Nodes (5): android.webkit.WebResourceResponse, JSONObject, WebResourceResponse, OnUpdateListener, WaveguardShield

### Community 58 - "android.view.View"
Cohesion: 0.05
Nodes (26): android.graphics.Canvas, android.graphics.Paint, android.graphics.RectF, android.util.AttributeSet, android.util.LruCache, android.view.View, android.view.ViewGroup, androidx.dynamicanimation.animation.SpringAnimation (+18 more)

### Community 60 - "main/assets/youtube_helper.js"
Cohesion: 0.19
Nodes (7): cleanYouTubeData(), executeFastForwardSkip(), handleSettingsInteraction(), reparentFsMenus(), scheduleAdFallbackTick(), set(), triggerFastForwardSkipThrottled()

### Community 63 - "🌊 Caspian Mobile - Standalone Android Application"
Cohesion: 0.22
Nodes (8): Build Command, 🛠️ Building From Source, 🌊 Caspian Mobile - Standalone Android Application, 🎨 Design Fusion System, 📱 Key Features, Prerequisites, 🔒 Privacy Guarantee, 📁 Repository Structure

### Community 64 - "Caspian-Beta-B/assets/mobile_control.js"
Cohesion: 0.33
Nodes (6): renderOpenTabs(), restoreSavedSettings(), setTheme(), syncAppVersion(), syncHostPageTheme(), updateDebugRecUI()

### Community 66 - "SwipeableViewFlipper"
Cohesion: 0.23
Nodes (4): android.widget.ViewFlipper, Override, OnPageChangeListener, SwipeableViewFlipper

### Community 68 - "publish-releases.sh"
Cohesion: 1.00
Nodes (3): publish_flow_release(), run_with_retry(), publish-releases.sh script

### Community 70 - "background.js"
Cohesion: 0.43
Nodes (5): captureTabWithQuotaRetry(), performRippleFrameCapture(), saveCaptureToDB(), studioTabIds, updateSpeedBadge()

### Community 72 - "default_icon"
Cohesion: 0.33
Nodes (6): action, default_icon, default_popup, 128, 16, 48

### Community 74 - "Caspian-Beta-B/assets/mobile_pruner.js"
Cohesion: 0.52
Nodes (6): applyPruning(), debouncedApplyPruning(), getTopLevelTurns(), loadState(), queryShadowSelectorAll(), startObserver()

### Community 75 - "Caspian-Android/assets/mobile_pruner.js"
Cohesion: 0.60
Nodes (5): applyPruning(), debouncedApplyPruning(), getTopLevelTurns(), loadState(), startObserver()

### Community 77 - "Caspian-Beta-A/assets/mobile_pruner.js"
Cohesion: 0.60
Nodes (5): applyPruning(), debouncedApplyPruning(), getTopLevelTurns(), loadState(), startObserver()

### Community 78 - "bump_and_release.py"
Cohesion: 0.83
Nodes (3): bump_gradle_version(), main(), run_cmd()

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

### Community 121 - "manifest.json"
Cohesion: 0.14
Nodes (13): background, service_worker, content_scripts, description, host_permissions, icons, 128, 16 (+5 more)

### Community 128 - ".dpToPx"
Cohesion: 0.13
Nodes (13): Button, EditText, FrameLayout, HorizontalScrollView, ImageButton, LinearLayout, TextView, TabGroup (+5 more)

## Knowledge Gaps
- **148 isolated node(s):** `studioTabIds`, `manifest_version`, `name`, `version`, `description` (+143 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 407 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **22 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `MainActivity` connect `MainActivity` to `android.webkit.JavascriptInterface`, `.dpToPx`, `android.annotation.SuppressLint`, `android.content.Context`, `CaspianDownloadManager`, `betac/MainActivity.java`, `.getTabById`, `WhirlpoolOverlayView`, `CaspianWebView`, `BookmarkManager`, `.updateOmniboxState`, `RecentsHorizontalScrollView`, `android.webkit.WebView`, `.getYouTubeTab`, `.getActiveOrDominantTab`, `.resolve`, `.restoreLastClosedBatch`, `android.content.Intent`, `WaveguardShield`, `android.view.View`, `.showBrowserActionGrid`?**
  _High betweenness centrality (0.310) - this node is a cross-community bridge._
- **Why does `CaspianBridge` connect `android.webkit.JavascriptInterface` to `.downloadApk`, `MainActivity`, `GitHubUpdateManager`, `.getInstance`, `CaspianBridge`, `WaveguardShield`, `.getCasksPayloadJson`?**
  _High betweenness centrality (0.112) - this node is a cross-community bridge._
- **Why does `MainActivity` connect `MainActivity` to `.setupFloatingYouTubeRemote`, `android.webkit.WebView`, `.switchTab`, `android.content.Context`, `CaspianBridge`, `.onCreate`, `betac/MainActivity.java`, `android.content.Intent`, `android.view.View`?**
  _High betweenness centrality (0.066) - this node is a cross-community bridge._
- **What connects `studioTabIds`, `manifest_version`, `name` to the rest of the system?**
  _148 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `android.webkit.JavascriptInterface` be split into smaller, more focused modules?**
  _Cohesion score 0.028263403263403264 - nodes in this community are weakly interconnected._
- **Should `MainActivity` be split into smaller, more focused modules?**
  _Cohesion score 0.029131652661064426 - nodes in this community are weakly interconnected._
- **Should `browser_control.js` be split into smaller, more focused modules?**
  _Cohesion score 0.05396825396825397 - nodes in this community are weakly interconnected._