# Graph Report - Chatgpt Pruner  (2026-09-14)

## Corpus Check
- 105 files · ~535,069 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 2098 nodes · 5235 edges · 136 communities (61 shown, 34 thin omitted)
- Extraction: 97% EXTRACTED · 3% INFERRED · 0% AMBIGUOUS · INFERRED: 173 edges (avg confidence: 0.81)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `88f7d350`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- android.webkit.JavascriptInterface
- MainActivity
- browser_control.js
- .onCreate
- popup.js
- CaskManager
- CaspianBridge
- CaspianBridge
- CaspianDownloadManager
- betac/MainActivity.java
- rippleframe.js
- .updateOmniboxState
- WhirlpoolOverlayView
- MainActivity
- MainActivity
- MainActivity
- CaspianWebView
- CaspianMediaService
- android.view.View
- BookmarkManager
- GitHubUpdateManager
- scrobby_engine.js
- CaspianBridge
- Caspian-Android/assets/mobile_control.js
- Caspian-Beta-A/assets/mobile_control.js
- Caspian: AI Chat Pruner, Universal Media Speed Engine & Productivity Suite
- HistoryManager
- pdf_viewer.js
- .getTabById
- RecentsHorizontalScrollView
- android.annotation.SuppressLint
- .switchTab
- .switchTab
- android.os.Handler
- Override
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
- AdBlockShield
- SwipeableViewFlipper
- .resolve
- .switchToTab
- android.widget.TextView
- .getActiveTab
- speed_content.js
- main/assets/mobile_pruner.js
- .onBackPressed
- WaveguardShield
- android.content.Context
- main/assets/youtube_helper.js
- .setupFloatingPod
- 🌊 Caspian Mobile - Standalone Android Application
- Caspian-Beta-B/assets/mobile_control.js
- .dpToPx
- WhisperLib
- publish-releases.sh
- Caspian-Android/assets/youtube_helper.js
- background.js
- android.content.Intent
- android.webkit.WebView
- Caspian-Beta-A/assets/youtube_helper.js
- Caspian-Beta-B/assets/mobile_pruner.js
- Caspian-Android/assets/mobile_pruner.js
- Caspian-Beta-A/assets/mobile_pruner.js
- bump_and_release.py
- betaC/gradlew
- Caspian-Android/gradlew
- Caspian-Beta-A/gradlew
- 🌊 Caspian Mobile - Standalone Android Application
- Caspian-Beta-B/gradlew
- applyYouTubeOptimizations
- commands
- AudioRecord
- Button
- CustomViewCallback
- .updateOmniboxTabStrip
- EditText
- FrameLayout
- Handler
- HorizontalScrollView
- ImageButton
- Intent
- LinearLayout
- Override
- SpeechWaveformView
- TextView
- WakeLock
- WebView

## God Nodes (most connected - your core abstractions)
1. `MainActivity` - 448 edges
2. `CaspianBridge` - 163 edges
3. `MainActivity` - 114 edges
4. `MainActivity` - 114 edges
5. `CaspianBridge` - 52 edges
6. `CaspianBridge` - 52 edges
7. `MainActivity` - 51 edges
8. `CaspianDownloadManager` - 38 edges
9. `playSFX()` - 37 edges
10. `WaveguardShield` - 36 edges

## Surprising Connections (you probably didn't know these)
- `CaspianBridge` --references--> `MainActivity`  [EXTRACTED]
  betaC/app/src/main/java/com/caspian/betac/CaspianBridge.java → betaC/app/src/main/java/com/caspian/betac/MainActivity.java
- `ChatAdapter` --references--> `ChatMessage`  [EXTRACTED]
  Caspian-Beta-B/app/src/main/java/com/caspian/betab/ChatAdapter.java → Caspian-Beta-B/app/src/main/java/com/caspian/betab/ChatMessage.java
- `CaspianBridge` --references--> `MainActivity`  [EXTRACTED]
  Caspian-Beta-B/app/src/main/java/com/caspian/betab/CaspianBridge.java → Caspian-Beta-B/app/src/main/java/com/caspian/betab/MainActivity.java
- `MainActivity` --references--> `ChatAdapter`  [EXTRACTED]
  Caspian-Beta-B/app/src/main/java/com/caspian/betab/MainActivity.java → Caspian-Beta-B/app/src/main/java/com/caspian/betab/ChatAdapter.java
- `MainActivity` --references--> `ChatGPTApiClient`  [EXTRACTED]
  Caspian-Beta-B/app/src/main/java/com/caspian/betab/MainActivity.java → Caspian-Beta-B/app/src/main/java/com/caspian/betab/ChatGPTApiClient.java

## Import Cycles
- None detected.

## Communities (136 total, 34 thin omitted)

### Community 1 - "MainActivity"
Cohesion: 0.03
Nodes (17): android.graphics.Bitmap, AudioRecord, MainActivity, MultiTabDragState, CustomViewCallback, Intent, LruCache, MediaSessionCompat (+9 more)

### Community 2 - "browser_control.js"
Cohesion: 0.05
Nodes (83): applyCustomBg(), applyCustomGradient(), applyInterfaceDensity(), attachHarborCardListeners(), autoFetchHarborFavicon(), closeControlCasksModal(), closeHarborEditorModal(), closeHarborModal() (+75 more)

### Community 4 - "popup.js"
Cohesion: 0.07
Nodes (50): accentPicker, applyFontScale(), applyUiZoom(), DEFAULT_PRESETS, DEFAULTS, DEV_FACTS, escapeHtml(), exportGoogleDocFile() (+42 more)

### Community 5 - "CaskManager"
Cohesion: 0.21
Nodes (4): CaskItem, CaskManager, JSONObject, WebView

### Community 8 - "CaspianDownloadManager"
Cohesion: 0.10
Nodes (8): CaspianDownloadManager, DownloadItem, DownloadListener, DownloadTask, Handler, JSONObject, Override, FileOutputStream

### Community 9 - "betac/MainActivity.java"
Cohesion: 0.11
Nodes (25): android.content.BroadcastReceiver, android.media.AudioRecord, android.media.MediaPlayer, android.media.SoundPool, android.os.Bundle, android.speech.SpeechRecognizer, android.support.v4.media.session.MediaSessionCompat, android.view.TextureView (+17 more)

### Community 10 - "rippleframe.js"
Cohesion: 0.13
Nodes (40): applyBlurToRegion(), applyCrop(), cancelCropMode(), clearStoredCaptures(), createPdfBlobFromImage(), drawArrow(), fitToScreen(), generatePDF() (+32 more)

### Community 12 - "WhirlpoolOverlayView"
Cohesion: 0.12
Nodes (14): android.graphics.PointF, android.graphics.Rect, android.view.MotionEvent, android.widget.Button, android.widget.LinearLayout, DrawingView, Button, HorizontalScrollView (+6 more)

### Community 13 - "MainActivity"
Cohesion: 0.09
Nodes (5): ChatMessage, Intent, Override, WebView, MainActivity

### Community 14 - "MainActivity"
Cohesion: 0.09
Nodes (5): AudioRecord, MediaPlayer, PermissionRequest, TextureView, MainActivity

### Community 15 - "MainActivity"
Cohesion: 0.09
Nodes (6): AudioRecord, Intent, MediaPlayer, PermissionRequest, TextureView, MainActivity

### Community 16 - "CaspianWebView"
Cohesion: 0.15
Nodes (4): CaspianWebView, Override, OnLinkLongPressListener, OnScrollStateListener

### Community 17 - "CaspianMediaService"
Cohesion: 0.17
Nodes (9): android.app.Notification, android.app.Service, android.os.IBinder, CaspianMediaService, Handler, Intent, Override, WakeLock (+1 more)

### Community 18 - "android.view.View"
Cohesion: 0.22
Nodes (6): android.view.View, androidx.dynamicanimation.animation.SpringAnimation, CaspianPhysics, SpringAnimation, ViewProperty, WeakHashMap

### Community 19 - "BookmarkManager"
Cohesion: 0.15
Nodes (3): BookmarkItem, BookmarkManager, JSONObject

### Community 20 - "GitHubUpdateManager"
Cohesion: 0.17
Nodes (5): DownloadCallback, GitHubUpdateManager, JSONObject, UpdateCheckCallback, UpdateInfo

### Community 21 - "scrobby_engine.js"
Cohesion: 0.14
Nodes (26): authenticateWithToken(), callLastFm(), cleanTrackInfo(), disconnect(), emitState(), generateSignature(), getRecentScrobbles(), getSettings() (+18 more)

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

### Community 29 - ".getTabById"
Cohesion: 0.06
Nodes (5): android.app.RemoteAction, android.content.res.Configuration, android.support.v4.media.session.PlaybackStateCompat, Override, RemoteAction

### Community 31 - "RecentsHorizontalScrollView"
Cohesion: 0.29
Nodes (4): android.animation.ValueAnimator, android.widget.HorizontalScrollView, Override, RecentsHorizontalScrollView

### Community 32 - "android.annotation.SuppressLint"
Cohesion: 0.14
Nodes (3): android.annotation.SuppressLint, SoundPool, SpeechWaveformView

### Community 35 - "android.os.Handler"
Cohesion: 0.10
Nodes (14): android.app.Activity, android.app.NotificationManager, android.content.SharedPreferences, android.os.Handler, SearchSuggestionService, SuggestionCallback, ChatGPTApiClient, Handler (+6 more)

### Community 38 - "content.js"
Cohesion: 0.25
Nodes (11): applyTurbo(), checkAndRestoreTransferContext(), clearAllPruning(), domObserver, extractConversationData(), getChatTitle(), getTopLevelTurns(), isSiteDisabled() (+3 more)

### Community 39 - "manifest.json"
Cohesion: 0.10
Nodes (19): action, default_icon, default_popup, background, service_worker, content_scripts, 128, 16 (+11 more)

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

### Community 48 - "SwipeableViewFlipper"
Cohesion: 0.27
Nodes (4): android.widget.ViewFlipper, Override, OnPageChangeListener, SwipeableViewFlipper

### Community 49 - ".resolve"
Cohesion: 0.24
Nodes (7): AICommandRouter, RouteResult, SearchEngine, BING, BRAVE, DUCKDUCKGO, GOOGLE

### Community 51 - "android.widget.TextView"
Cohesion: 0.19
Nodes (9): Adapter, android.widget.TextView, androidx.annotation.NonNull, androidx.recyclerview.widget.RecyclerView, ChatAdapter, ChatViewHolder, Override, io.noties.markwon.Markwon (+1 more)

### Community 54 - "speed_content.js"
Cohesion: 0.29
Nodes (9): applySpeedToAllMedia(), attachMediaListeners(), cycleToNextSpeed(), injectHudStyles(), loadConfig(), observeMedia(), parseCycleList(), setSpeed() (+1 more)

### Community 55 - "main/assets/mobile_pruner.js"
Cohesion: 0.35
Nodes (9): applyPruningDirect(), clearAllPruning(), ensurePrunerStyles(), findVisibleCenterTurnIndex(), getTopLevelTurns(), initObserver(), loadState(), onScrollHandler() (+1 more)

### Community 57 - "WaveguardShield"
Cohesion: 0.08
Nodes (5): android.webkit.WebResourceResponse, JSONObject, WebResourceResponse, OnUpdateListener, WaveguardShield

### Community 58 - "android.content.Context"
Cohesion: 0.08
Nodes (14): android.content.Context, android.graphics.Canvas, android.graphics.Paint, android.util.AttributeSet, Override, Override, Paint, SpeechWaveformView (+6 more)

### Community 60 - "main/assets/youtube_helper.js"
Cohesion: 0.19
Nodes (7): cleanYouTubeData(), executeFastForwardSkip(), handleSettingsInteraction(), reparentFsMenus(), scheduleAdFallbackTick(), set(), triggerFastForwardSkipThrottled()

### Community 62 - ".setupFloatingPod"
Cohesion: 0.21
Nodes (5): android.graphics.RectF, android.view.ViewGroup, CabRadialMenuView, OnRadialActionSelectedListener, CabRadialMenuView

### Community 63 - "🌊 Caspian Mobile - Standalone Android Application"
Cohesion: 0.22
Nodes (8): Build Command, 🛠️ Building From Source, 🌊 Caspian Mobile - Standalone Android Application, 🎨 Design Fusion System, 📱 Key Features, Prerequisites, 🔒 Privacy Guarantee, 📁 Repository Structure

### Community 64 - "Caspian-Beta-B/assets/mobile_control.js"
Cohesion: 0.33
Nodes (6): renderOpenTabs(), restoreSavedSettings(), setTheme(), syncAppVersion(), syncHostPageTheme(), updateDebugRecUI()

### Community 66 - ".dpToPx"
Cohesion: 0.13
Nodes (9): CaspianMenuItem, BookmarkItem, BookmarkManager, EditText, LinearLayout, PopupWindow, ScrollView, SwipeableViewFlipper (+1 more)

### Community 68 - "publish-releases.sh"
Cohesion: 1.00
Nodes (3): publish_flow_release(), run_with_retry(), publish-releases.sh script

### Community 70 - "background.js"
Cohesion: 0.43
Nodes (5): captureTabWithQuotaRetry(), performRippleFrameCapture(), saveCaptureToDB(), studioTabIds, updateSpeedBadge()

### Community 72 - "android.webkit.WebView"
Cohesion: 0.30
Nodes (4): android.webkit.WebView, android.widget.FrameLayout, android.widget.Toast, java.io.FileOutputStream

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

### Community 121 - "commands"
Cohesion: 0.29
Nodes (8): commands, reset-colors, toggle-feature, description, suggested_key, default, description, suggested_key

### Community 128 - ".updateOmniboxTabStrip"
Cohesion: 0.12
Nodes (10): ClosedTabRecord, TabGroup, TabItem, Button, FrameLayout, GridLayout, Handler, HorizontalScrollView (+2 more)

## Knowledge Gaps
- **148 isolated node(s):** `description`, `description`, `RFC-1321`, `1-Click Setup with Configuration File`, `1. RippleFrame: Full-Page Scrolling Screenshot & Annotation Studio` (+143 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 417 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **34 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `MainActivity` connect `MainActivity` to `android.webkit.JavascriptInterface`, `.updateOmniboxTabStrip`, `CaspianDownloadManager`, `betac/MainActivity.java`, `.updateOmniboxState`, `WhirlpoolOverlayView`, `android.view.View`, `BookmarkManager`, `GitHubUpdateManager`, `.saveOpenTabsState`, `.getTabById`, `RecentsHorizontalScrollView`, `android.annotation.SuppressLint`, `android.os.Handler`, `.switchToTab`, `android.widget.TextView`, `WaveguardShield`, `.setupFloatingPod`, `.dpToPx`, `android.content.Intent`, `android.webkit.WebView`?**
  _High betweenness centrality (0.285) - this node is a cross-community bridge._
- **Why does `CaspianBridge` connect `android.webkit.JavascriptInterface` to `MainActivity`, `android.webkit.WebView`, `GitHubUpdateManager`, `.getInstance`, `WaveguardShield`, `.getCasksPayloadJson`?**
  _High betweenness centrality (0.101) - this node is a cross-community bridge._
- **Why does `MainActivity` connect `MainActivity` to `android.annotation.SuppressLint`, `.switchTab`, `android.os.Handler`, `Override`, `android.content.Intent`, `CaspianBridge`, `betac/MainActivity.java`, `android.webkit.WebView`, `android.view.View`, `android.widget.TextView`, `.getActiveTab`, `android.content.Context`, `.updateFloatingYTRemoteVisibility`?**
  _High betweenness centrality (0.058) - this node is a cross-community bridge._
- **What connects `description`, `description`, `RFC-1321` to the rest of the system?**
  _148 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `android.webkit.JavascriptInterface` be split into smaller, more focused modules?**
  _Cohesion score 0.02951443986036179 - nodes in this community are weakly interconnected._
- **Should `MainActivity` be split into smaller, more focused modules?**
  _Cohesion score 0.02731067382230173 - nodes in this community are weakly interconnected._
- **Should `browser_control.js` be split into smaller, more focused modules?**
  _Cohesion score 0.05396825396825397 - nodes in this community are weakly interconnected._