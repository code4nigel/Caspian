# Graph Report - Chatgpt Pruner  (2026-09-17)

## Corpus Check
- 120 files · ~548,432 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 2337 nodes · 5924 edges · 149 communities (69 shown, 37 thin omitted)
- Extraction: 94% EXTRACTED · 6% INFERRED · 0% AMBIGUOUS · INFERRED: 366 edges (avg confidence: 0.81)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `81763703`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- android.webkit.JavascriptInterface
- MainActivity
- browser_control.js
- .onCreate
- popup.js
- .getYouTubeTab
- CaspianBridge
- CaspianBridge
- CaspianDownloadManager
- betac/MainActivity.java
- rippleframe.js
- .parseAndDispatch
- android.graphics.Bitmap
- MainActivity
- MainActivity
- MainActivity
- CaspianWebView
- BookmarkManager
- TabController
- .showCardGridEditDialog
- .getTabById
- scrobby_engine.js
- CaspianBridge
- Caspian-Android/assets/mobile_control.js
- Caspian-Beta-A/assets/mobile_control.js
- Caspian: AI Chat Pruner, Universal Media Speed Engine & Productivity Suite
- HistoryManager
- pdf_viewer.js
- TabState
- RecentsHorizontalScrollView
- TabControllerDeviceTest
- .switchTab
- .switchTab
- .dpToPx
- WaveguardShield
- org.junit.Test
- content.js
- .handleMessage
- stitch_designs/caspian_ai_browser/DESIGN.md
- stitch_caspian_ai_mobile_browser/caspian_ai_browser/DESIGN.md
- stitch_dark_card/DESIGN.md
- stitch_dark_list/DESIGN.md
- Stitch_history_UI/DESIGN.md
- stitch_light_card/DESIGN.md
- stitch_light_list/DESIGN.md
- android.content.Context
- ChatGPTApiClient
- .resolve
- .exitSplitMode
- android.annotation.SuppressLint
- speed_content.js
- main/assets/mobile_pruner.js
- ChatMessage
- .restoreLastClosedBatch
- android.util.AttributeSet
- CaspianMediaService
- main/assets/youtube_helper.js
- 🌊 Caspian Mobile - Standalone Android Application
- Caspian-Beta-B/assets/mobile_control.js
- android.webkit.WebView
- .setupFloatingYouTubeRemote
- WhisperLib
- publish-releases.sh
- Caspian-Android/assets/youtube_helper.js
- background.js
- commands
- android.view.MotionEvent
- Caspian-Beta-A/assets/youtube_helper.js
- Caspian-Beta-B/assets/mobile_pruner.js
- Caspian-Android/assets/mobile_pruner.js
- android.view.View
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
- android.os.Handler
- .setupFloatingPod
- CaskManager
- .onBackPressed
- Override
- .playUiFeedbackSound
- android.net.Uri
- betac/CaspianBridge.java
- AudioRecord
- Button
- CustomViewCallback
- EditText
- FrameLayout
- Handler
- HorizontalScrollView
- ImageButton
- Intent
- LinearLayout
- .updateOmniboxState
- LruCache
- Override
- SpeechWaveformView
- TextView
- WakeLock
- WebView

## God Nodes (most connected - your core abstractions)
1. `MainActivity` - 462 edges
2. `CaspianBridge` - 170 edges
3. `MainActivity` - 114 edges
4. `MainActivity` - 114 edges
5. `CaspianBridge` - 52 edges
6. `CaspianBridge` - 52 edges
7. `MainActivity` - 51 edges
8. `TabController` - 43 edges
9. `CaspianDownloadManager` - 38 edges
10. `playSFX()` - 37 edges

## Surprising Connections (you probably didn't know these)
- `CaspianBridge` --references--> `MainActivity`  [EXTRACTED]
  betaC/app/src/main/java/com/caspian/betac/CaspianBridge.java → betaC/app/src/main/java/com/caspian/betac/MainActivity.java
- `ChatAdapter` --references--> `ChatMessage`  [EXTRACTED]
  Caspian-Beta-B/app/src/main/java/com/caspian/betab/ChatAdapter.java → Caspian-Beta-B/app/src/main/java/com/caspian/betab/ChatMessage.java
- `MainActivity` --references--> `ChatAdapter`  [EXTRACTED]
  Caspian-Beta-B/app/src/main/java/com/caspian/betab/MainActivity.java → Caspian-Beta-B/app/src/main/java/com/caspian/betab/ChatAdapter.java
- `MainActivity` --references--> `ChatMessage`  [EXTRACTED]
  Caspian-Beta-B/app/src/main/java/com/caspian/betab/MainActivity.java → Caspian-Beta-B/app/src/main/java/com/caspian/betab/ChatMessage.java
- `CaspianBridge` --references--> `MainActivity`  [EXTRACTED]
  Caspian-Beta-B/app/src/main/java/com/caspian/betab/CaspianBridge.java → Caspian-Beta-B/app/src/main/java/com/caspian/betab/MainActivity.java

## Import Cycles
- None detected.

## Communities (149 total, 37 thin omitted)

### Community 1 - "MainActivity"
Cohesion: 0.03
Nodes (19): AudioRecord, TabController, MainActivity, MultiTabDragState, BookmarkManager, BroadcastReceiver, CustomViewCallback, Handler (+11 more)

### Community 2 - "browser_control.js"
Cohesion: 0.05
Nodes (83): applyCustomBg(), applyCustomGradient(), applyInterfaceDensity(), attachHarborCardListeners(), autoFetchHarborFavicon(), closeControlCasksModal(), closeHarborEditorModal(), closeHarborModal() (+75 more)

### Community 4 - "popup.js"
Cohesion: 0.07
Nodes (50): accentPicker, applyFontScale(), applyUiZoom(), DEFAULT_PRESETS, DEFAULTS, DEV_FACTS, escapeHtml(), exportGoogleDocFile() (+42 more)

### Community 8 - "CaspianDownloadManager"
Cohesion: 0.06
Nodes (14): android.app.Activity, CaspianDownloadManager, DownloadItem, DownloadListener, DownloadTask, Handler, JSONObject, Override (+6 more)

### Community 9 - "betac/MainActivity.java"
Cohesion: 0.12
Nodes (26): android.content.BroadcastReceiver, android.media.AudioRecord, android.media.MediaPlayer, android.media.SoundPool, android.os.Bundle, android.speech.SpeechRecognizer, android.support.v4.media.session.MediaSessionCompat, android.view.TextureView (+18 more)

### Community 10 - "rippleframe.js"
Cohesion: 0.13
Nodes (40): applyBlurToRegion(), applyCrop(), cancelCropMode(), clearStoredCaptures(), createPdfBlobFromImage(), drawArrow(), fitToScreen(), generatePDF() (+32 more)

### Community 11 - ".parseAndDispatch"
Cohesion: 0.18
Nodes (4): Override, MediaMessageCallback, TrustedMediaMessageHandler, TrustedMediaMessageHandlerTest

### Community 12 - "android.graphics.Bitmap"
Cohesion: 0.12
Nodes (14): android.graphics.Bitmap, android.graphics.PointF, android.graphics.Rect, android.widget.Button, android.widget.LinearLayout, DrawingView, Button, HorizontalScrollView (+6 more)

### Community 13 - "MainActivity"
Cohesion: 0.09
Nodes (5): androidx.drawerlayout.widget.DrawerLayout, Intent, Override, WebView, MainActivity

### Community 14 - "MainActivity"
Cohesion: 0.09
Nodes (6): AudioRecord, Intent, MediaPlayer, PermissionRequest, TextureView, MainActivity

### Community 15 - "MainActivity"
Cohesion: 0.09
Nodes (6): AudioRecord, Intent, MediaPlayer, PermissionRequest, TextureView, MainActivity

### Community 16 - "CaspianWebView"
Cohesion: 0.14
Nodes (4): CaspianWebView, Override, OnLinkLongPressListener, OnScrollStateListener

### Community 17 - "BookmarkManager"
Cohesion: 0.15
Nodes (3): BookmarkItem, BookmarkManager, JSONObject

### Community 20 - ".getTabById"
Cohesion: 0.06
Nodes (5): android.app.RemoteAction, android.content.res.Configuration, CaspianMenuItem, PopupWindow, RemoteAction

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
Cohesion: 0.15
Nodes (5): android.database.sqlite.SQLiteDatabase, android.database.sqlite.SQLiteOpenHelper, HistoryEntry, HistoryManager, Override

### Community 28 - "pdf_viewer.js"
Cohesion: 0.23
Nodes (20): calculateFitWidthScale(), cancelAllRenderTasks(), clearSearchHighlights(), createPagePlaceholders(), executeSearch(), hideAiMenu(), init(), jumpToPage() (+12 more)

### Community 29 - "TabState"
Cohesion: 0.18
Nodes (5): android.content.SharedPreferences, TabState, SessionSnapshot, TabStateRepository, TabStateRepositoryTest

### Community 31 - "RecentsHorizontalScrollView"
Cohesion: 0.24
Nodes (4): android.animation.ValueAnimator, android.widget.HorizontalScrollView, Override, RecentsHorizontalScrollView

### Community 32 - "TabControllerDeviceTest"
Cohesion: 0.15
Nodes (4): androidx.test.ext.junit.runners.AndroidJUnit4, TabControllerDeviceTest, org.junit.Before, org.junit.runner.RunWith

### Community 36 - "WaveguardShield"
Cohesion: 0.08
Nodes (5): android.webkit.WebResourceResponse, JSONObject, WebResourceResponse, OnUpdateListener, WaveguardShield

### Community 37 - "org.junit.Test"
Cohesion: 0.13
Nodes (5): OriginVerifier, BridgeSecurityPolicyTest, OriginVerifierTest, WebViewSecurityPolicyTest, org.junit.Test

### Community 38 - "content.js"
Cohesion: 0.25
Nodes (11): applyTurbo(), checkAndRestoreTransferContext(), clearAllPruning(), domObserver, extractConversationData(), getChatTitle(), getTopLevelTurns(), isSiteDisabled() (+3 more)

### Community 39 - ".handleMessage"
Cohesion: 0.11
Nodes (4): HubActionCallback, TrustedHubMessageHandler, TrustedHubMessageHandlerTest, WebMessageListener

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

### Community 47 - "android.content.Context"
Cohesion: 0.15
Nodes (3): android.content.Context, AdBlockShield, WebResourceResponse

### Community 48 - "ChatGPTApiClient"
Cohesion: 0.29
Nodes (5): ChatGPTApiClient, Handler, ModelItem, ModelsCallback, okhttp3.OkHttpClient

### Community 49 - ".resolve"
Cohesion: 0.24
Nodes (7): AICommandRouter, RouteResult, SearchEngine, BING, BRAVE, DUCKDUCKGO, GOOGLE

### Community 51 - ".exitSplitMode"
Cohesion: 0.20
Nodes (3): TabEventListener, TabController, TabControllerTest

### Community 52 - "android.annotation.SuppressLint"
Cohesion: 0.17
Nodes (3): android.annotation.SuppressLint, SoundPool, SpeechWaveformView

### Community 54 - "speed_content.js"
Cohesion: 0.29
Nodes (9): applySpeedToAllMedia(), attachMediaListeners(), cycleToNextSpeed(), injectHudStyles(), loadConfig(), observeMedia(), parseCycleList(), setSpeed() (+1 more)

### Community 55 - "main/assets/mobile_pruner.js"
Cohesion: 0.35
Nodes (9): applyPruningDirect(), clearAllPruning(), ensurePrunerStyles(), findVisibleCenterTurnIndex(), getTopLevelTurns(), initObserver(), loadState(), onScrollHandler() (+1 more)

### Community 58 - "android.util.AttributeSet"
Cohesion: 0.10
Nodes (12): android.graphics.Canvas, android.graphics.Paint, android.util.AttributeSet, Override, Paint, SpeechWaveformView, Override, Paint (+4 more)

### Community 59 - "CaspianMediaService"
Cohesion: 0.14
Nodes (12): android.app.Notification, android.app.Service, android.content.Intent, android.os.IBinder, CaspianMediaService, Handler, Intent, Override (+4 more)

### Community 60 - "main/assets/youtube_helper.js"
Cohesion: 0.19
Nodes (10): attachVideoListeners(), cleanYouTubeData(), dispatchScrobbyState(), executeFastForwardSkip(), handleSettingsInteraction(), postCaspianMediaMessage(), reparentFsMenus(), scheduleAdFallbackTick() (+2 more)

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

### Community 71 - "commands"
Cohesion: 0.29
Nodes (8): commands, reset-colors, toggle-feature, description, suggested_key, default, description, suggested_key

### Community 72 - "android.view.MotionEvent"
Cohesion: 0.24
Nodes (5): android.view.MotionEvent, android.widget.ViewFlipper, Override, OnPageChangeListener, SwipeableViewFlipper

### Community 74 - "Caspian-Beta-B/assets/mobile_pruner.js"
Cohesion: 0.52
Nodes (6): applyPruning(), debouncedApplyPruning(), getTopLevelTurns(), loadState(), queryShadowSelectorAll(), startObserver()

### Community 75 - "Caspian-Android/assets/mobile_pruner.js"
Cohesion: 0.60
Nodes (5): applyPruning(), debouncedApplyPruning(), getTopLevelTurns(), loadState(), startObserver()

### Community 76 - "android.view.View"
Cohesion: 0.25
Nodes (6): android.view.View, androidx.dynamicanimation.animation.SpringAnimation, CaspianPhysics, SpringAnimation, ViewProperty, WeakHashMap

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
Cohesion: 0.10
Nodes (19): action, default_icon, default_popup, background, service_worker, content_scripts, 128, 16 (+11 more)

### Community 122 - "ChatAdapter"
Cohesion: 0.26
Nodes (7): Adapter, androidx.recyclerview.widget.RecyclerView, ChatAdapter, ChatViewHolder, Override, io.noties.markwon.Markwon, ViewHolder

### Community 123 - "android.os.Handler"
Cohesion: 0.18
Nodes (8): android.app.NotificationManager, android.os.Handler, SearchSuggestionService, SuggestionCallback, JSONObject, java.net.HttpURLConnection, java.util.regex.Pattern, org.json.JSONObject

### Community 124 - ".setupFloatingPod"
Cohesion: 0.10
Nodes (11): android.graphics.RectF, android.util.LruCache, android.view.ViewGroup, CabRadialMenuView, Canvas, Override, Paint, OnRadialActionSelectedListener (+3 more)

### Community 125 - "CaskManager"
Cohesion: 0.15
Nodes (5): CaskItem, CaskManager, JSONObject, WebView, TabState

### Community 127 - "Override"
Cohesion: 0.15
Nodes (3): CustomViewCallback, FrameLayout, Override

### Community 128 - ".playUiFeedbackSound"
Cohesion: 0.12
Nodes (13): TabGroup, TabItem, BookmarkItem, Button, EditText, FrameLayout, GridLayout, HorizontalScrollView (+5 more)

### Community 129 - "android.net.Uri"
Cohesion: 0.19
Nodes (6): android.net.Uri, androidx.annotation.NonNull, androidx.webkit.JavaScriptReplyProxy, androidx.webkit.WebMessageCompat, Override, Override

### Community 130 - "betac/CaspianBridge.java"
Cohesion: 0.19
Nodes (9): android.widget.Toast, BridgeSecurityPolicy, Category, LOCAL_UI, PDF_VIEWER, PUBLIC, TRUSTED_AI, TRUSTED_MEDIA (+1 more)

## Knowledge Gaps
- **153 isolated node(s):** `default_popup`, `service_worker`, `content_scripts`, `128`, `16` (+148 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 435 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **37 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `MainActivity` connect `MainActivity` to `android.webkit.JavascriptInterface`, `android.net.Uri`, `.playUiFeedbackSound`, `.getYouTubeTab`, `CaspianDownloadManager`, `betac/MainActivity.java`, `android.graphics.Bitmap`, `.updateOmniboxState`, `CaspianWebView`, `.showCardGridEditDialog`, `.getTabById`, `.addTab`, `RecentsHorizontalScrollView`, `TabControllerDeviceTest`, `.dpToPx`, `WaveguardShield`, `.restoreLastClosedBatch`, `CaspianMediaService`, `.restoreOpenTabsState`, `android.webkit.WebView`, `android.view.View`, `android.os.Handler`, `.setupFloatingPod`, `CaskManager`?**
  _High betweenness centrality (0.295) - this node is a cross-community bridge._
- **Why does `CaspianBridge` connect `android.webkit.JavascriptInterface` to `MainActivity`, `betac/CaspianBridge.java`, `WaveguardShield`, `CaspianDownloadManager`, `.switchCaspianCask`, `.getInstance`, `CaskManager`?**
  _High betweenness centrality (0.088) - this node is a cross-community bridge._
- **Why does `MainActivity` connect `MainActivity` to `android.net.Uri`, `android.webkit.WebView`, `.setupFloatingYouTubeRemote`, `.onCreate`, `.switchTab`, `CaspianBridge`, `betac/MainActivity.java`, `CaspianMediaService`, `android.view.View`, `.getActiveTab`, `android.util.AttributeSet`, `android.os.Handler`, `.onBackPressed`?**
  _High betweenness centrality (0.080) - this node is a cross-community bridge._
- **What connects `default_popup`, `service_worker`, `content_scripts` to the rest of the system?**
  _153 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `android.webkit.JavascriptInterface` be split into smaller, more focused modules?**
  _Cohesion score 0.02846449325322565 - nodes in this community are weakly interconnected._
- **Should `MainActivity` be split into smaller, more focused modules?**
  _Cohesion score 0.028061893522161027 - nodes in this community are weakly interconnected._
- **Should `browser_control.js` be split into smaller, more focused modules?**
  _Cohesion score 0.05396825396825397 - nodes in this community are weakly interconnected._