# Graph Report - Chatgpt Pruner  (2026-10-18)

## Corpus Check
- 131 files · ~665,009 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 2534 nodes · 6332 edges · 139 communities (72 shown, 23 thin omitted)
- Extraction: 94% EXTRACTED · 6% INFERRED · 0% AMBIGUOUS · INFERRED: 406 edges (avg confidence: 0.81)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `f7d514c0`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- android.webkit.JavascriptInterface
- MainActivity
- browser_control.js
- .onCreate
- Caspian/popup.js
- .getTabById
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
- CaskManager
- caspian fox extension/popup.js
- org.junit.Test
- scrobby_engine.js
- CaspianBridge
- Caspian-Android/assets/mobile_control.js
- Caspian-Beta-A/assets/mobile_control.js
- BookmarkManager
- Caspian: AI Chat Pruner, Universal Media Speed Engine & Productivity Suite
- android.webkit.WebView
- pdf_viewer.js
- RecentsHorizontalScrollView
- TabController
- .switchTab
- .switchTab
- WaveguardShield
- android.widget.FrameLayout
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
- android.os.Handler
- .resolve
- caspian fox extension/rippleframe.js
- .addTab
- .setupFloatingYouTubeRemote
- Caspian/speed_content.js
- main/assets/mobile_pruner.js
- .onCreate
- .onBackPressed
- android.util.AttributeSet
- CaspianMediaService
- main/assets/youtube_helper.js
- caspian fox extension/manifest.json
- 🌊 Caspian Mobile - Standalone Android Application
- Caspian-Beta-B/assets/mobile_control.js
- .evaluateJavascriptInControlSheet
- .setupFloatingYouTubeRemote
- WhisperLib
- publish-releases.sh
- Caspian-Android/assets/youtube_helper.js
- background.js
- TabState
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
- betab/MainActivity.java
- .setupFloatingPod
- generate_amo_assets.py
- .onBackPressed
- caspian fox extension/content.js
- .dpToPx
- HistoryManager
- SearchSuggestionService
- Highlights & Features
- caspian fox extension/speed_content.js
- ChatMessage
- caspian fox extension/background.js
- applyYouTubeOptimizations
- package_extensions.py
- .updateOmniboxState

## God Nodes (most connected - your core abstractions)
1. `MainActivity` - 462 edges
2. `CaspianBridge` - 172 edges
3. `MainActivity` - 114 edges
4. `MainActivity` - 114 edges
5. `CaspianBridge` - 52 edges
6. `CaspianBridge` - 52 edges
7. `MainActivity` - 51 edges
8. `TabController` - 47 edges
9. `WaveguardShield` - 39 edges
10. `CaspianDownloadManager` - 38 edges

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

## Communities (139 total, 23 thin omitted)

### Community 1 - "MainActivity"
Cohesion: 0.03
Nodes (13): android.net.Uri, android.support.v4.media.session.PlaybackStateCompat, AudioRecord, CustomViewCallback, Intent, LruCache, WakeLock, MainActivity (+5 more)

### Community 2 - "browser_control.js"
Cohesion: 0.05
Nodes (83): applyCustomBg(), applyCustomGradient(), applyInterfaceDensity(), attachHarborCardListeners(), autoFetchHarborFavicon(), closeControlCasksModal(), closeHarborEditorModal(), closeHarborModal() (+75 more)

### Community 4 - "Caspian/popup.js"
Cohesion: 0.06
Nodes (54): accentPicker, applyFontScale(), applyUiZoom(), DEFAULT_PRESETS, DEFAULTS, DEV_FACTS, escapeHtml(), exportGoogleDocFile() (+46 more)

### Community 5 - ".getTabById"
Cohesion: 0.05
Nodes (7): android.app.RemoteAction, android.content.res.Configuration, Canvas, CaspianMenuItem, ClosedTabRecord, Override, RemoteAction

### Community 8 - "CaspianDownloadManager"
Cohesion: 0.11
Nodes (7): android.app.NotificationManager, CaspianDownloadManager, DownloadItem, DownloadListener, DownloadTask, Handler, Override

### Community 9 - "betac/MainActivity.java"
Cohesion: 0.11
Nodes (25): android.content.BroadcastReceiver, android.media.AudioRecord, android.media.MediaPlayer, android.media.SoundPool, android.os.Bundle, android.speech.SpeechRecognizer, android.support.v4.media.session.MediaSessionCompat, android.view.TextureView (+17 more)

### Community 10 - "rippleframe.js"
Cohesion: 0.13
Nodes (40): applyBlurToRegion(), applyCrop(), cancelCropMode(), clearStoredCaptures(), createPdfBlobFromImage(), drawArrow(), fitToScreen(), generatePDF() (+32 more)

### Community 11 - ".parseAndDispatch"
Cohesion: 0.10
Nodes (8): androidx.test.ext.junit.runners.AndroidJUnit4, TabControllerDeviceTest, Override, MediaMessageCallback, TrustedMediaMessageHandler, TrustedMediaMessageHandlerTest, org.junit.Before, org.junit.runner.RunWith

### Community 12 - "android.graphics.Bitmap"
Cohesion: 0.11
Nodes (16): android.graphics.Bitmap, android.graphics.PointF, android.graphics.Rect, android.graphics.RectF, android.util.LruCache, android.widget.Button, DrawingView, Button (+8 more)

### Community 13 - "MainActivity"
Cohesion: 0.10
Nodes (4): Intent, Override, WebView, MainActivity

### Community 14 - "MainActivity"
Cohesion: 0.08
Nodes (6): AudioRecord, Intent, MediaPlayer, PermissionRequest, TextureView, MainActivity

### Community 15 - "MainActivity"
Cohesion: 0.09
Nodes (6): AudioRecord, Intent, MediaPlayer, PermissionRequest, TextureView, MainActivity

### Community 16 - "CaspianWebView"
Cohesion: 0.15
Nodes (4): CaspianWebView, Override, OnLinkLongPressListener, OnScrollStateListener

### Community 17 - "CaskManager"
Cohesion: 0.20
Nodes (4): CaskItem, CaskManager, JSONObject, WebView

### Community 18 - "caspian fox extension/popup.js"
Cohesion: 0.06
Nodes (54): accentPicker, applyFontScale(), applyUiZoom(), DEFAULT_PRESETS, DEFAULTS, DEV_FACTS, escapeHtml(), exportGoogleDocFile() (+46 more)

### Community 20 - "org.junit.Test"
Cohesion: 0.08
Nodes (6): OriginVerifier, BridgeSecurityPolicyTest, OriginVerifierTest, TrustedHubMessageHandlerTest, WebViewSecurityPolicyTest, org.junit.Test

### Community 21 - "scrobby_engine.js"
Cohesion: 0.14
Nodes (26): authenticateWithToken(), callLastFm(), cleanTrackInfo(), disconnect(), emitState(), generateSignature(), getRecentScrobbles(), getSettings() (+18 more)

### Community 23 - "Caspian-Android/assets/mobile_control.js"
Cohesion: 0.19
Nodes (18): applyCustomBg(), applyCustomGradient(), getSFXFileForType(), handleCreateNewTab(), openGroupOptionsMenu(), openTabOptionsMenu(), playSFX(), renderOpenTabs() (+10 more)

### Community 24 - "Caspian-Beta-A/assets/mobile_control.js"
Cohesion: 0.19
Nodes (18): applyCustomBg(), applyCustomGradient(), getSFXFileForType(), handleCreateNewTab(), openGroupOptionsMenu(), openTabOptionsMenu(), playSFX(), renderOpenTabs() (+10 more)

### Community 25 - "BookmarkManager"
Cohesion: 0.06
Nodes (12): android.app.Activity, BookmarkItem, BookmarkManager, JSONObject, JSONObject, DownloadCallback, GitHubUpdateManager, JSONObject (+4 more)

### Community 26 - "Caspian: AI Chat Pruner, Universal Media Speed Engine & Productivity Suite"
Cohesion: 0.07
Nodes (28): 1-Click Setup with Configuration File, 1. RippleFrame: Full-Page Scrolling Screenshot & Annotation Studio, 2. Real-Time AI Chat DOM Pruning, 3. Flow Speed: Universal Media Playback Controller, 4. Universal Transcript Exporter, 4. YouTube Home Feed Cleaner & Instant Feedback, 5. Temporary Chat Vault, 6. Settings Backup & Migration (JSON Import / Export) (+20 more)

### Community 28 - "pdf_viewer.js"
Cohesion: 0.23
Nodes (20): calculateFitWidthScale(), cancelAllRenderTasks(), clearSearchHighlights(), createPagePlaceholders(), executeSearch(), hideAiMenu(), init(), jumpToPage() (+12 more)

### Community 31 - "RecentsHorizontalScrollView"
Cohesion: 0.23
Nodes (5): android.animation.ValueAnimator, android.view.ViewGroup, android.widget.HorizontalScrollView, Override, RecentsHorizontalScrollView

### Community 36 - "WaveguardShield"
Cohesion: 0.08
Nodes (5): android.webkit.WebResourceResponse, JSONObject, WebResourceResponse, OnUpdateListener, WaveguardShield

### Community 37 - "android.widget.FrameLayout"
Cohesion: 0.19
Nodes (10): android.widget.FrameLayout, android.widget.Toast, BridgeSecurityPolicy, Category, LOCAL_UI, PDF_VIEWER, PUBLIC, TRUSTED_AI (+2 more)

### Community 38 - "content.js"
Cohesion: 0.25
Nodes (11): applyTurbo(), checkAndRestoreTransferContext(), clearAllPruning(), domObserver, extractConversationData(), getChatTitle(), getTopLevelTurns(), isSiteDisabled() (+3 more)

### Community 39 - ".handleMessage"
Cohesion: 0.12
Nodes (7): androidx.webkit.JavaScriptReplyProxy, androidx.webkit.WebMessageCompat, HubActionCallback, Override, TrustedHubMessageHandler, java.util.regex.Pattern, WebMessageListener

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

### Community 48 - "android.os.Handler"
Cohesion: 0.27
Nodes (6): android.os.Handler, ChatGPTApiClient, Handler, ModelItem, ModelsCallback, okhttp3.OkHttpClient

### Community 49 - ".resolve"
Cohesion: 0.24
Nodes (7): AICommandRouter, RouteResult, SearchEngine, BING, BRAVE, DUCKDUCKGO, GOOGLE

### Community 50 - "caspian fox extension/rippleframe.js"
Cohesion: 0.13
Nodes (40): applyBlurToRegion(), applyCrop(), cancelCropMode(), clearStoredCaptures(), createPdfBlobFromImage(), drawArrow(), fitToScreen(), generatePDF() (+32 more)

### Community 54 - "Caspian/speed_content.js"
Cohesion: 0.26
Nodes (12): applySpeedToAllMedia(), cycleToNextSpeed(), enforceRate(), findAllMediaElements(), injectHudStyles(), loadConfig(), observeRoot(), parseCycleList() (+4 more)

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

### Community 62 - "caspian fox extension/manifest.json"
Cohesion: 0.06
Nodes (35): action, default_icon, default_popup, background, scripts, browser_specific_settings, gecko, gecko_android (+27 more)

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

### Community 71 - "TabState"
Cohesion: 0.18
Nodes (5): android.content.SharedPreferences, TabState, SessionSnapshot, TabStateRepository, TabStateRepositoryTest

### Community 72 - "android.view.MotionEvent"
Cohesion: 0.23
Nodes (5): android.view.MotionEvent, android.widget.ViewFlipper, Override, OnPageChangeListener, SwipeableViewFlipper

### Community 74 - "Caspian-Beta-B/assets/mobile_pruner.js"
Cohesion: 0.52
Nodes (6): applyPruning(), debouncedApplyPruning(), getTopLevelTurns(), loadState(), queryShadowSelectorAll(), startObserver()

### Community 75 - "Caspian-Android/assets/mobile_pruner.js"
Cohesion: 0.60
Nodes (5): applyPruning(), debouncedApplyPruning(), getTopLevelTurns(), loadState(), startObserver()

### Community 76 - "android.view.View"
Cohesion: 0.22
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
Cohesion: 0.07
Nodes (27): action, default_icon, default_popup, background, service_worker, commands, reset-colors, toggle-feature (+19 more)

### Community 122 - "betab/MainActivity.java"
Cohesion: 0.18
Nodes (11): Adapter, android.widget.LinearLayout, android.widget.TextView, androidx.annotation.NonNull, androidx.drawerlayout.widget.DrawerLayout, androidx.recyclerview.widget.RecyclerView, ChatAdapter, ChatViewHolder (+3 more)

### Community 124 - ".setupFloatingPod"
Cohesion: 0.15
Nodes (5): CabRadialMenuView, Override, Paint, OnRadialActionSelectedListener, ShortcutItem

### Community 125 - "generate_amo_assets.py"
Cohesion: 0.70
Nodes (4): create_gradient_canvas(), draw_pill(), draw_rounded_shadow_card(), render_slide()

### Community 127 - "caspian fox extension/content.js"
Cohesion: 0.25
Nodes (11): applyTurbo(), checkAndRestoreTransferContext(), clearAllPruning(), domObserver, extractConversationData(), getChatTitle(), getTopLevelTurns(), isSiteDisabled() (+3 more)

### Community 128 - ".dpToPx"
Cohesion: 0.11
Nodes (13): Button, EditText, FrameLayout, HorizontalScrollView, ImageButton, LinearLayout, TextView, TabGroup (+5 more)

### Community 129 - "HistoryManager"
Cohesion: 0.15
Nodes (5): android.database.sqlite.SQLiteDatabase, android.database.sqlite.SQLiteOpenHelper, HistoryEntry, HistoryManager, Override

### Community 131 - "Highlights & Features"
Cohesion: 0.15
Nodes (12): 1. Real-Time AI Chat DOM Pruning, 2. Temporary Chat Vault & Session Converter, 3. Flow Speed: Universal Media Playback Controller, 4. YouTube Home Feed Cleaner & Instant Feedback, 5. RippleFrame: Full-Page Screenshot & Annotation Studio, Caspian for Firefox (Mozilla Firefox Browser Extension), Highlights & Features, How to Install in Firefox (+4 more)

### Community 132 - "caspian fox extension/speed_content.js"
Cohesion: 0.26
Nodes (12): applySpeedToAllMedia(), cycleToNextSpeed(), enforceRate(), findAllMediaElements(), injectHudStyles(), loadConfig(), observeRoot(), parseCycleList() (+4 more)

### Community 134 - "caspian fox extension/background.js"
Cohesion: 0.43
Nodes (5): captureTabWithQuotaRetry(), performRippleFrameCapture(), saveCaptureToDB(), studioTabIds, updateSpeedBadge()

### Community 138 - "package_extensions.py"
Cohesion: 0.53
Nodes (5): get_manifest_version(), main(), package_extension(), package_extensions.py Utility script to package Caspian Chromium and Firefox…, zip_folder()

### Community 142 - ".updateOmniboxState"
Cohesion: 0.06
Nodes (3): android.annotation.SuppressLint, SpeechWaveformView, BroadcastReceiver

## Knowledge Gaps
- **202 isolated node(s):** `studioTabIds`, `manifest_version`, `name`, `version`, `description` (+197 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 484 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **23 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `MainActivity` connect `MainActivity` to `android.webkit.JavascriptInterface`, `.dpToPx`, `.getTabById`, `CaspianDownloadManager`, `betac/MainActivity.java`, `android.graphics.Bitmap`, `.updateOmniboxState`, `CaspianWebView`, `.saveOpenTabsState`, `BookmarkManager`, `android.webkit.WebView`, `RecentsHorizontalScrollView`, `TabController`, `.transitionToOrbState`, `WaveguardShield`, `android.widget.FrameLayout`, `android.os.Handler`, `.resolve`, `.addTab`, `android.util.AttributeSet`, `CaspianMediaService`, `.evaluateJavascriptInControlSheet`, `android.view.MotionEvent`, `android.view.View`, `betab/MainActivity.java`, `.setupFloatingPod`?**
  _High betweenness centrality (0.235) - this node is a cross-community bridge._
- **Why does `CaspianBridge` connect `android.webkit.JavascriptInterface` to `MainActivity`, `WaveguardShield`, `android.widget.FrameLayout`, `CaskManager`, `.getInstance`, `BookmarkManager`, `.switchCaspianCask`?**
  _High betweenness centrality (0.077) - this node is a cross-community bridge._
- **Why does `MainActivity` connect `MainActivity` to `MainActivity`, `.setupFloatingYouTubeRemote`, `.onCreate`, `.switchTab`, `android.widget.FrameLayout`, `CaspianBridge`, `android.util.AttributeSet`, `android.webkit.WebView`, `betac/MainActivity.java`, `android.view.View`, `android.os.Handler`, `.getActiveTab`, `betab/MainActivity.java`, `CaspianMediaService`, `.onBackPressed`?**
  _High betweenness centrality (0.053) - this node is a cross-community bridge._
- **What connects `studioTabIds`, `manifest_version`, `name` to the rest of the system?**
  _202 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `android.webkit.JavascriptInterface` be split into smaller, more focused modules?**
  _Cohesion score 0.028457653457653456 - nodes in this community are weakly interconnected._
- **Should `MainActivity` be split into smaller, more focused modules?**
  _Cohesion score 0.02836637047163363 - nodes in this community are weakly interconnected._
- **Should `browser_control.js` be split into smaller, more focused modules?**
  _Cohesion score 0.05396825396825397 - nodes in this community are weakly interconnected._