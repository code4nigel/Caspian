# Graph Report - Chatgpt Pruner  (2026-09-17)

## Corpus Check
- 117 files · ~545,540 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 2280 nodes · 5717 edges · 139 communities (62 shown, 39 thin omitted)
- Extraction: 95% EXTRACTED · 5% INFERRED · 0% AMBIGUOUS · INFERRED: 287 edges (avg confidence: 0.81)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `d45073d3`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- android.webkit.JavascriptInterface
- MainActivity
- browser_control.js
- .setupFloatingYouTubeRemote
- popup.js
- .getYouTubeTab
- CaspianBridge
- CaspianBridge
- CaspianDownloadManager
- betac/MainActivity.java
- rippleframe.js
- .saveOpenTabsState
- WhirlpoolOverlayView
- MainActivity
- MainActivity
- MainActivity
- CaspianWebView
- BookmarkManager
- .addTab
- .getTabById
- .restoreLastClosedBatch
- scrobby_engine.js
- CaspianBridge
- Caspian-Android/assets/mobile_control.js
- Caspian-Beta-A/assets/mobile_control.js
- CabRadialMenuView
- Caspian: AI Chat Pruner, Universal Media Speed Engine & Productivity Suite
- HistoryManager
- pdf_viewer.js
- .testTabGroupManagement
- android.view.MotionEvent
- .setUp
- .switchTab
- android.webkit.WebView
- .dpToPx
- android.view.View
- org.junit.Test
- content.js
- CaspianMediaService
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
- AudioRecord
- TabController
- .onCreate
- .getInstance
- speed_content.js
- main/assets/mobile_pruner.js
- Button
- WaveguardShield
- android.content.Context
- CustomViewCallback
- main/assets/youtube_helper.js
- .parseAndDispatch
- 🌊 Caspian Mobile - Standalone Android Application
- Caspian-Beta-B/assets/mobile_control.js
- EditText
- FrameLayout
- WhisperLib
- publish-releases.sh
- Caspian-Android/assets/youtube_helper.js
- background.js
- Handler
- SwipeableViewFlipper
- Caspian-Beta-A/assets/youtube_helper.js
- Caspian-Beta-B/assets/mobile_pruner.js
- Caspian-Android/assets/mobile_pruner.js
- .setupFloatingPod
- Caspian-Beta-A/assets/mobile_pruner.js
- bump_and_release.py
- betaC/gradlew
- Caspian-Android/gradlew
- Caspian-Beta-A/gradlew
- 🌊 Caspian Mobile - Standalone Android Application
- Caspian-Beta-B/gradlew
- applyYouTubeOptimizations
- manifest.json
- ChatAdapter
- HorizontalScrollView
- .showCardGridEditDialog
- .onBackPressed
- ImageButton
- Intent
- .playUiFeedbackSound
- LinearLayout
- LruCache
- Override
- TabEventListener
- SpeechWaveformView
- TextView
- WakeLock
- WebView
- JSONObject
- WebResourceResponse

## God Nodes (most connected - your core abstractions)
1. `MainActivity` - 459 edges
2. `CaspianBridge` - 170 edges
3. `MainActivity` - 114 edges
4. `MainActivity` - 114 edges
5. `CaspianBridge` - 52 edges
6. `CaspianBridge` - 52 edges
7. `MainActivity` - 51 edges
8. `TabController` - 44 edges
9. `WaveguardShield` - 39 edges
10. `CaspianDownloadManager` - 38 edges

## Surprising Connections (you probably didn't know these)
- `CaspianBridge` --references--> `MainActivity`  [EXTRACTED]
  betaC/app/src/main/java/com/caspian/betac/CaspianBridge.java → betaC/app/src/main/java/com/caspian/betac/MainActivity.java
- `MainActivity` --references--> `TabController`  [EXTRACTED]
  betaC/app/src/main/java/com/caspian/betac/MainActivity.java → betaC/app/src/main/java/com/caspian/betac/tabs/TabController.java
- `MainActivity` --references--> `WaveguardShield`  [EXTRACTED]
  betaC/app/src/main/java/com/caspian/betac/MainActivity.java → betaC/app/src/main/java/com/caspian/betac/WaveguardShield.java
- `TabControllerTest` --references--> `TabController`  [EXTRACTED]
  betaC/app/src/test/java/com/caspian/betac/tabs/TabControllerTest.java → betaC/app/src/main/java/com/caspian/betac/tabs/TabController.java
- `MainActivity` --references--> `ChatAdapter`  [EXTRACTED]
  Caspian-Beta-B/app/src/main/java/com/caspian/betab/MainActivity.java → Caspian-Beta-B/app/src/main/java/com/caspian/betab/ChatAdapter.java

## Import Cycles
- None detected.

## Communities (139 total, 39 thin omitted)

### Community 1 - "MainActivity"
Cohesion: 0.03
Nodes (18): android.graphics.Bitmap, AudioRecord, MainActivity, MultiTabDragState, BookmarkManager, BroadcastReceiver, CustomViewCallback, Handler (+10 more)

### Community 2 - "browser_control.js"
Cohesion: 0.05
Nodes (83): applyCustomBg(), applyCustomGradient(), applyInterfaceDensity(), attachHarborCardListeners(), autoFetchHarborFavicon(), closeControlCasksModal(), closeHarborEditorModal(), closeHarborModal() (+75 more)

### Community 4 - "popup.js"
Cohesion: 0.07
Nodes (50): accentPicker, applyFontScale(), applyUiZoom(), DEFAULT_PRESETS, DEFAULTS, DEV_FACTS, escapeHtml(), exportGoogleDocFile() (+42 more)

### Community 5 - ".getYouTubeTab"
Cohesion: 0.08
Nodes (3): android.content.res.Configuration, android.support.v4.media.session.PlaybackStateCompat, Override

### Community 8 - "CaspianDownloadManager"
Cohesion: 0.06
Nodes (18): android.app.Activity, android.app.NotificationManager, android.os.Handler, CaspianDownloadManager, DownloadItem, DownloadListener, DownloadTask, Handler (+10 more)

### Community 9 - "betac/MainActivity.java"
Cohesion: 0.08
Nodes (31): android.content.BroadcastReceiver, android.content.Intent, android.media.AudioRecord, android.media.MediaPlayer, android.media.SoundPool, android.net.Uri, android.os.Bundle, android.speech.SpeechRecognizer (+23 more)

### Community 10 - "rippleframe.js"
Cohesion: 0.13
Nodes (40): applyBlurToRegion(), applyCrop(), cancelCropMode(), clearStoredCaptures(), createPdfBlobFromImage(), drawArrow(), fitToScreen(), generatePDF() (+32 more)

### Community 12 - "WhirlpoolOverlayView"
Cohesion: 0.13
Nodes (12): android.graphics.PointF, android.graphics.Rect, android.widget.Button, DrawingView, Button, HorizontalScrollView, LinearLayout, Override (+4 more)

### Community 13 - "MainActivity"
Cohesion: 0.09
Nodes (6): android.annotation.SuppressLint, androidx.drawerlayout.widget.DrawerLayout, Intent, Override, WebView, MainActivity

### Community 14 - "MainActivity"
Cohesion: 0.05
Nodes (11): AudioRecord, CustomViewCallback, FrameLayout, Intent, MediaPlayer, Override, PermissionRequest, SoundPool (+3 more)

### Community 15 - "MainActivity"
Cohesion: 0.09
Nodes (6): AudioRecord, Intent, MediaPlayer, PermissionRequest, TextureView, MainActivity

### Community 16 - "CaspianWebView"
Cohesion: 0.15
Nodes (4): CaspianWebView, Override, OnLinkLongPressListener, OnScrollStateListener

### Community 17 - "BookmarkManager"
Cohesion: 0.06
Nodes (15): android.content.SharedPreferences, BookmarkItem, BookmarkManager, JSONObject, CaskItem, CaskManager, JSONObject, WebView (+7 more)

### Community 19 - ".getTabById"
Cohesion: 0.08
Nodes (5): android.app.RemoteAction, CaspianMenuItem, JSONObject, PopupWindow, RemoteAction

### Community 21 - "scrobby_engine.js"
Cohesion: 0.14
Nodes (26): authenticateWithToken(), callLastFm(), cleanTrackInfo(), disconnect(), emitState(), generateSignature(), getRecentScrobbles(), getSettings() (+18 more)

### Community 22 - "CaspianBridge"
Cohesion: 0.09
Nodes (5): android.widget.FrameLayout, android.widget.Toast, BridgeSecurityPolicy, CaspianBridge, java.io.FileOutputStream

### Community 23 - "Caspian-Android/assets/mobile_control.js"
Cohesion: 0.19
Nodes (18): applyCustomBg(), applyCustomGradient(), getSFXFileForType(), handleCreateNewTab(), openGroupOptionsMenu(), openTabOptionsMenu(), playSFX(), renderOpenTabs() (+10 more)

### Community 24 - "Caspian-Beta-A/assets/mobile_control.js"
Cohesion: 0.19
Nodes (18): applyCustomBg(), applyCustomGradient(), getSFXFileForType(), handleCreateNewTab(), openGroupOptionsMenu(), openTabOptionsMenu(), playSFX(), renderOpenTabs() (+10 more)

### Community 25 - "CabRadialMenuView"
Cohesion: 0.13
Nodes (8): android.graphics.RectF, android.util.LruCache, CabRadialMenuView, Canvas, Override, Paint, OnRadialActionSelectedListener, ShortcutItem

### Community 26 - "Caspian: AI Chat Pruner, Universal Media Speed Engine & Productivity Suite"
Cohesion: 0.09
Nodes (22): 1-Click Setup with Configuration File, 1. RippleFrame: Full-Page Scrolling Screenshot & Annotation Studio, 2. Real-Time AI Chat DOM Pruning, 3. Flow Speed: Universal Media Playback Controller, 4. Universal Transcript Exporter, 4. YouTube Home Feed Cleaner & Instant Feedback, 5. Temporary Chat Vault, 6. Settings Backup & Migration (JSON Import / Export) (+14 more)

### Community 27 - "HistoryManager"
Cohesion: 0.15
Nodes (5): android.database.sqlite.SQLiteDatabase, android.database.sqlite.SQLiteOpenHelper, HistoryEntry, HistoryManager, Override

### Community 28 - "pdf_viewer.js"
Cohesion: 0.23
Nodes (20): calculateFitWidthScale(), cancelAllRenderTasks(), clearSearchHighlights(), createPagePlaceholders(), executeSearch(), hideAiMenu(), init(), jumpToPage() (+12 more)

### Community 31 - "android.view.MotionEvent"
Cohesion: 0.28
Nodes (5): android.animation.ValueAnimator, android.view.MotionEvent, android.widget.HorizontalScrollView, Override, RecentsHorizontalScrollView

### Community 34 - "android.webkit.WebView"
Cohesion: 0.16
Nodes (3): android.webkit.WebView, WebView, TabItem

### Community 37 - "org.junit.Test"
Cohesion: 0.14
Nodes (5): OriginVerifier, BridgeSecurityPolicyTest, OriginVerifierTest, WebViewSecurityPolicyTest, org.junit.Test

### Community 38 - "content.js"
Cohesion: 0.25
Nodes (11): applyTurbo(), checkAndRestoreTransferContext(), clearAllPruning(), domObserver, extractConversationData(), getChatTitle(), getTopLevelTurns(), isSiteDisabled() (+3 more)

### Community 39 - "CaspianMediaService"
Cohesion: 0.15
Nodes (11): android.app.Notification, android.app.Service, android.os.IBinder, CaspianMediaService, Handler, Intent, Override, WakeLock (+3 more)

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
Cohesion: 0.18
Nodes (6): ChatGPTApiClient, Handler, ModelItem, ModelsCallback, StreamCallback, okhttp3.OkHttpClient

### Community 49 - ".resolve"
Cohesion: 0.24
Nodes (7): AICommandRouter, RouteResult, SearchEngine, BING, BRAVE, DUCKDUCKGO, GOOGLE

### Community 51 - "TabController"
Cohesion: 0.17
Nodes (3): TabState, TabController, SessionSnapshot

### Community 53 - ".getInstance"
Cohesion: 0.09
Nodes (6): Category, LOCAL_UI, PDF_VIEWER, PUBLIC, TRUSTED_AI, TRUSTED_MEDIA

### Community 54 - "speed_content.js"
Cohesion: 0.29
Nodes (9): applySpeedToAllMedia(), attachMediaListeners(), cycleToNextSpeed(), injectHudStyles(), loadConfig(), observeMedia(), parseCycleList(), setSpeed() (+1 more)

### Community 55 - "main/assets/mobile_pruner.js"
Cohesion: 0.35
Nodes (9): applyPruningDirect(), clearAllPruning(), ensurePrunerStyles(), findVisibleCenterTurnIndex(), getTopLevelTurns(), initObserver(), loadState(), onScrollHandler() (+1 more)

### Community 57 - "WaveguardShield"
Cohesion: 0.08
Nodes (4): android.webkit.WebResourceResponse, OnUpdateListener, WaveguardShield, WebResourceResponse

### Community 58 - "android.content.Context"
Cohesion: 0.09
Nodes (13): android.content.Context, android.graphics.Canvas, android.graphics.Paint, android.util.AttributeSet, Override, Paint, SpeechWaveformView, Override (+5 more)

### Community 60 - "main/assets/youtube_helper.js"
Cohesion: 0.19
Nodes (10): attachVideoListeners(), cleanYouTubeData(), dispatchScrobbyState(), executeFastForwardSkip(), handleSettingsInteraction(), postCaspianMediaMessage(), reparentFsMenus(), scheduleAdFallbackTick() (+2 more)

### Community 62 - ".parseAndDispatch"
Cohesion: 0.14
Nodes (8): androidx.annotation.NonNull, androidx.webkit.JavaScriptReplyProxy, androidx.webkit.WebMessageCompat, Override, MediaMessageCallback, TrustedMediaMessageHandler, TrustedMediaMessageHandlerTest, WebMessageListener

### Community 63 - "🌊 Caspian Mobile - Standalone Android Application"
Cohesion: 0.22
Nodes (8): Build Command, 🛠️ Building From Source, 🌊 Caspian Mobile - Standalone Android Application, 🎨 Design Fusion System, 📱 Key Features, Prerequisites, 🔒 Privacy Guarantee, 📁 Repository Structure

### Community 64 - "Caspian-Beta-B/assets/mobile_control.js"
Cohesion: 0.33
Nodes (6): renderOpenTabs(), restoreSavedSettings(), setTheme(), syncAppVersion(), syncHostPageTheme(), updateDebugRecUI()

### Community 68 - "publish-releases.sh"
Cohesion: 1.00
Nodes (3): publish_flow_release(), run_with_retry(), publish-releases.sh script

### Community 70 - "background.js"
Cohesion: 0.43
Nodes (5): captureTabWithQuotaRetry(), performRippleFrameCapture(), saveCaptureToDB(), studioTabIds, updateSpeedBadge()

### Community 72 - "SwipeableViewFlipper"
Cohesion: 0.27
Nodes (4): android.widget.ViewFlipper, Override, OnPageChangeListener, SwipeableViewFlipper

### Community 74 - "Caspian-Beta-B/assets/mobile_pruner.js"
Cohesion: 0.52
Nodes (6): applyPruning(), debouncedApplyPruning(), getTopLevelTurns(), loadState(), queryShadowSelectorAll(), startObserver()

### Community 75 - "Caspian-Android/assets/mobile_pruner.js"
Cohesion: 0.60
Nodes (5): applyPruning(), debouncedApplyPruning(), getTopLevelTurns(), loadState(), startObserver()

### Community 76 - ".setupFloatingPod"
Cohesion: 0.19
Nodes (7): androidx.dynamicanimation.animation.SpringAnimation, CaspianPhysics, CabRadialMenuView, ShortcutItem, SpringAnimation, ViewProperty, WeakHashMap

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
Cohesion: 0.07
Nodes (27): action, default_icon, default_popup, background, service_worker, commands, reset-colors, toggle-feature (+19 more)

### Community 122 - "ChatAdapter"
Cohesion: 0.21
Nodes (6): Adapter, ChatAdapter, ChatViewHolder, Override, ChatMessage, ViewHolder

### Community 128 - ".playUiFeedbackSound"
Cohesion: 0.12
Nodes (13): TabGroup, TabItem, BookmarkItem, Button, EditText, FrameLayout, GridLayout, HorizontalScrollView (+5 more)

## Knowledge Gaps
- **153 isolated node(s):** `default_popup`, `service_worker`, `content_scripts`, `128`, `16` (+148 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 437 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **39 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `MainActivity` connect `MainActivity` to `android.webkit.JavascriptInterface`, `.playUiFeedbackSound`, `.getYouTubeTab`, `CaspianDownloadManager`, `betac/MainActivity.java`, `.saveOpenTabsState`, `WhirlpoolOverlayView`, `.addTab`, `.getTabById`, `.restoreLastClosedBatch`, `CaspianBridge`, `android.view.MotionEvent`, `android.webkit.WebView`, `.dpToPx`, `android.view.View`, `TabController`, `WaveguardShield`, `.setupFloatingPod`, `.showCardGridEditDialog`?**
  _High betweenness centrality (0.318) - this node is a cross-community bridge._
- **Why does `CaspianBridge` connect `android.webkit.JavascriptInterface` to `MainActivity`, `CaspianDownloadManager`, `.getInstance`, `CaspianBridge`, `WaveguardShield`, `.getCasksPayloadJson`?**
  _High betweenness centrality (0.086) - this node is a cross-community bridge._
- **Why does `MainActivity` connect `MainActivity` to `android.webkit.WebView`, `android.view.View`, `CaspianBridge`, `CaspianDownloadManager`, `betac/MainActivity.java`, `MainActivity`, `CaspianBridge`, `android.content.Context`?**
  _High betweenness centrality (0.077) - this node is a cross-community bridge._
- **What connects `default_popup`, `service_worker`, `content_scripts` to the rest of the system?**
  _153 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `android.webkit.JavascriptInterface` be split into smaller, more focused modules?**
  _Cohesion score 0.028774062816616007 - nodes in this community are weakly interconnected._
- **Should `MainActivity` be split into smaller, more focused modules?**
  _Cohesion score 0.030131826741996232 - nodes in this community are weakly interconnected._
- **Should `browser_control.js` be split into smaller, more focused modules?**
  _Cohesion score 0.05396825396825397 - nodes in this community are weakly interconnected._