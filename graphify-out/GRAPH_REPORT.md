# Graph Report - Chatgpt Pruner  (2026-10-18)

## Corpus Check
- 132 files · ~671,285 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 2566 nodes · 6446 edges · 138 communities (73 shown, 22 thin omitted)
- Extraction: 94% EXTRACTED · 6% INFERRED · 0% AMBIGUOUS · INFERRED: 409 edges (avg confidence: 0.81)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `96807fda`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- android.webkit.JavascriptInterface
- MainActivity
- browser_control.js
- .onCreate
- Caspian/popup.js
- .restoreLastClosedBatch
- CaspianBridge
- CaspianBridge
- CaspianDownloadManager
- betac/MainActivity.java
- rippleframe.js
- android.net.Uri
- WhirlpoolOverlayView
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
- GitHubUpdateManager
- Caspian: AI Chat Pruner, Universal Media Speed Engine & Productivity Suite
- .getYouTubeTab
- pdf_viewer.js
- android.view.MotionEvent
- .isAuthorized
- .switchTab
- .switchTab
- WaveguardShield
- SearchSuggestionService
- content.js
- .updateOmniboxState
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
- caspian fox extension/rippleframe.js
- TabController
- .setupFloatingYouTubeRemote
- Caspian/speed_content.js
- main/assets/mobile_pruner.js
- .onCreate
- .onBackPressed
- android.content.Context
- CaspianMediaService
- main/assets/youtube_helper.js
- caspian fox extension/manifest.json
- 🌊 Caspian Mobile - Standalone Android Application
- Caspian-Beta-B/assets/mobile_control.js
- CaspianPdfExporter.java
- .setupFloatingYouTubeRemote
- WhisperLib
- publish-releases.sh
- Caspian-Android/assets/youtube_helper.js
- background.js
- TabState
- SwipeableViewFlipper
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
- TabControllerDeviceTest
- BookmarkManager
- generate_amo_assets.py
- .onBackPressed
- caspian fox extension/content.js
- .playUiFeedbackSound
- HistoryManager
- Highlights & Features
- caspian fox extension/speed_content.js
- .setupFloatingPod
- caspian fox extension/background.js
- applyYouTubeOptimizations
- package_extensions.py
- .showCaspianCustomPopup

## God Nodes (most connected - your core abstractions)
1. `MainActivity` - 470 edges
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

## Communities (138 total, 22 thin omitted)

### Community 1 - "MainActivity"
Cohesion: 0.03
Nodes (17): android.graphics.Bitmap, Canvas, AudioRecord, CustomViewCallback, Handler, Intent, LruCache, SpeechWaveformView (+9 more)

### Community 2 - "browser_control.js"
Cohesion: 0.05
Nodes (83): applyCustomBg(), applyCustomGradient(), applyInterfaceDensity(), attachHarborCardListeners(), autoFetchHarborFavicon(), closeControlCasksModal(), closeHarborEditorModal(), closeHarborModal() (+75 more)

### Community 4 - "Caspian/popup.js"
Cohesion: 0.06
Nodes (54): accentPicker, applyFontScale(), applyUiZoom(), DEFAULT_PRESETS, DEFAULTS, DEV_FACTS, escapeHtml(), exportGoogleDocFile() (+46 more)

### Community 8 - "CaspianDownloadManager"
Cohesion: 0.11
Nodes (7): CaspianDownloadManager, DownloadItem, DownloadListener, DownloadTask, Handler, JSONObject, Override

### Community 9 - "betac/MainActivity.java"
Cohesion: 0.08
Nodes (37): android.app.NotificationManager, android.content.BroadcastReceiver, android.content.Intent, android.content.SharedPreferences, android.media.AudioRecord, android.media.MediaPlayer, android.media.SoundPool, android.os.Bundle (+29 more)

### Community 10 - "rippleframe.js"
Cohesion: 0.13
Nodes (40): applyBlurToRegion(), applyCrop(), cancelCropMode(), clearStoredCaptures(), createPdfBlobFromImage(), drawArrow(), fitToScreen(), generatePDF() (+32 more)

### Community 11 - "android.net.Uri"
Cohesion: 0.12
Nodes (10): android.net.Uri, androidx.annotation.NonNull, androidx.webkit.JavaScriptReplyProxy, androidx.webkit.WebMessageCompat, Override, Override, MediaMessageCallback, TrustedMediaMessageHandler (+2 more)

### Community 12 - "WhirlpoolOverlayView"
Cohesion: 0.12
Nodes (14): android.graphics.PointF, android.graphics.Rect, android.widget.Button, android.widget.HorizontalScrollView, android.widget.LinearLayout, DrawingView, Button, HorizontalScrollView (+6 more)

### Community 13 - "MainActivity"
Cohesion: 0.07
Nodes (8): android.annotation.SuppressLint, androidx.drawerlayout.widget.DrawerLayout, StreamCallback, ChatMessage, Intent, Override, WebView, MainActivity

### Community 14 - "MainActivity"
Cohesion: 0.09
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
Cohesion: 0.10
Nodes (5): HubActionCallback, TrustedHubMessageHandler, TrustedHubMessageHandlerTest, WebViewSecurityPolicyTest, org.junit.Test

### Community 21 - "scrobby_engine.js"
Cohesion: 0.14
Nodes (26): authenticateWithToken(), callLastFm(), cleanTrackInfo(), disconnect(), emitState(), generateSignature(), getRecentScrobbles(), getSettings() (+18 more)

### Community 23 - "Caspian-Android/assets/mobile_control.js"
Cohesion: 0.19
Nodes (18): applyCustomBg(), applyCustomGradient(), getSFXFileForType(), handleCreateNewTab(), openGroupOptionsMenu(), openTabOptionsMenu(), playSFX(), renderOpenTabs() (+10 more)

### Community 24 - "Caspian-Beta-A/assets/mobile_control.js"
Cohesion: 0.19
Nodes (18): applyCustomBg(), applyCustomGradient(), getSFXFileForType(), handleCreateNewTab(), openGroupOptionsMenu(), openTabOptionsMenu(), playSFX(), renderOpenTabs() (+10 more)

### Community 25 - "GitHubUpdateManager"
Cohesion: 0.17
Nodes (5): DownloadCallback, GitHubUpdateManager, JSONObject, UpdateCheckCallback, UpdateInfo

### Community 26 - "Caspian: AI Chat Pruner, Universal Media Speed Engine & Productivity Suite"
Cohesion: 0.07
Nodes (28): 1-Click Setup with Configuration File, 1. RippleFrame: Full-Page Scrolling Screenshot & Annotation Studio, 2. Real-Time AI Chat DOM Pruning, 3. Flow Speed: Universal Media Playback Controller, 4. Universal Transcript Exporter, 4. YouTube Home Feed Cleaner & Instant Feedback, 5. Temporary Chat Vault, 6. Settings Backup & Migration (JSON Import / Export) (+20 more)

### Community 27 - ".getYouTubeTab"
Cohesion: 0.07
Nodes (5): android.app.RemoteAction, android.content.res.Configuration, android.support.v4.media.session.PlaybackStateCompat, Override, RemoteAction

### Community 28 - "pdf_viewer.js"
Cohesion: 0.23
Nodes (20): calculateFitWidthScale(), cancelAllRenderTasks(), clearSearchHighlights(), createPagePlaceholders(), executeSearch(), hideAiMenu(), init(), jumpToPage() (+12 more)

### Community 31 - "android.view.MotionEvent"
Cohesion: 0.25
Nodes (4): android.animation.ValueAnimator, android.view.MotionEvent, Override, RecentsHorizontalScrollView

### Community 32 - ".isAuthorized"
Cohesion: 0.09
Nodes (10): BridgeSecurityPolicy, Category, LOCAL_UI, PDF_VIEWER, PUBLIC, TRUSTED_AI, TRUSTED_MEDIA, OriginVerifier (+2 more)

### Community 36 - "WaveguardShield"
Cohesion: 0.08
Nodes (5): android.webkit.WebResourceResponse, JSONObject, WebResourceResponse, OnUpdateListener, WaveguardShield

### Community 38 - "content.js"
Cohesion: 0.25
Nodes (11): applyTurbo(), checkAndRestoreTransferContext(), clearAllPruning(), domObserver, extractConversationData(), getChatTitle(), getTopLevelTurns(), isSiteDisabled() (+3 more)

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

### Community 50 - "caspian fox extension/rippleframe.js"
Cohesion: 0.13
Nodes (40): applyBlurToRegion(), applyCrop(), cancelCropMode(), clearStoredCaptures(), createPdfBlobFromImage(), drawArrow(), fitToScreen(), generatePDF() (+32 more)

### Community 51 - "TabController"
Cohesion: 0.10
Nodes (4): TabController, TabEventListener, TabGroupState, TabControllerTest

### Community 54 - "Caspian/speed_content.js"
Cohesion: 0.26
Nodes (12): applySpeedToAllMedia(), cycleToNextSpeed(), enforceRate(), findAllMediaElements(), injectHudStyles(), loadConfig(), observeRoot(), parseCycleList() (+4 more)

### Community 55 - "main/assets/mobile_pruner.js"
Cohesion: 0.35
Nodes (9): applyPruningDirect(), clearAllPruning(), ensurePrunerStyles(), findVisibleCenterTurnIndex(), getTopLevelTurns(), initObserver(), loadState(), onScrollHandler() (+1 more)

### Community 58 - "android.content.Context"
Cohesion: 0.09
Nodes (13): android.content.Context, android.graphics.Canvas, android.graphics.Paint, android.util.AttributeSet, Override, Paint, SpeechWaveformView, Override (+5 more)

### Community 59 - "CaspianMediaService"
Cohesion: 0.15
Nodes (11): android.app.Notification, android.app.Service, android.os.IBinder, CaspianMediaService, Handler, Intent, Override, WakeLock (+3 more)

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

### Community 65 - "CaspianPdfExporter.java"
Cohesion: 0.17
Nodes (10): android.app.Activity, android.graphics.pdf.PdfDocument, android.text.StaticLayout, android.text.TextPaint, CaspianPdfExporter, FormattedBlock, OnPdfGeneratedCallback, PdfDocument (+2 more)

### Community 68 - "publish-releases.sh"
Cohesion: 1.00
Nodes (3): publish_flow_release(), run_with_retry(), publish-releases.sh script

### Community 70 - "background.js"
Cohesion: 0.43
Nodes (5): captureTabWithQuotaRetry(), performRippleFrameCapture(), saveCaptureToDB(), studioTabIds, updateSpeedBadge()

### Community 71 - "TabState"
Cohesion: 0.15
Nodes (5): JSONObject, TabState, SessionSnapshot, TabStateRepository, TabStateRepositoryTest

### Community 72 - "SwipeableViewFlipper"
Cohesion: 0.25
Nodes (4): android.widget.ViewFlipper, Override, OnPageChangeListener, SwipeableViewFlipper

### Community 74 - "Caspian-Beta-B/assets/mobile_pruner.js"
Cohesion: 0.52
Nodes (6): applyPruning(), debouncedApplyPruning(), getTopLevelTurns(), loadState(), queryShadowSelectorAll(), startObserver()

### Community 75 - "Caspian-Android/assets/mobile_pruner.js"
Cohesion: 0.60
Nodes (5): applyPruning(), debouncedApplyPruning(), getTopLevelTurns(), loadState(), startObserver()

### Community 76 - "android.view.View"
Cohesion: 0.26
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

### Community 122 - "ChatAdapter"
Cohesion: 0.23
Nodes (7): Adapter, androidx.recyclerview.widget.RecyclerView, ChatAdapter, ChatViewHolder, Override, io.noties.markwon.Markwon, ViewHolder

### Community 123 - "TabControllerDeviceTest"
Cohesion: 0.24
Nodes (4): androidx.test.ext.junit.runners.AndroidJUnit4, TabControllerDeviceTest, org.junit.Before, org.junit.runner.RunWith

### Community 124 - "BookmarkManager"
Cohesion: 0.15
Nodes (3): BookmarkItem, BookmarkManager, JSONObject

### Community 125 - "generate_amo_assets.py"
Cohesion: 0.70
Nodes (4): create_gradient_canvas(), draw_pill(), draw_rounded_shadow_card(), render_slide()

### Community 127 - "caspian fox extension/content.js"
Cohesion: 0.25
Nodes (11): applyTurbo(), checkAndRestoreTransferContext(), clearAllPruning(), domObserver, extractConversationData(), getChatTitle(), getTopLevelTurns(), isSiteDisabled() (+3 more)

### Community 128 - ".playUiFeedbackSound"
Cohesion: 0.12
Nodes (12): Button, EditText, FrameLayout, HorizontalScrollView, ImageButton, LinearLayout, TextView, TabGroup (+4 more)

### Community 129 - "HistoryManager"
Cohesion: 0.13
Nodes (5): android.database.sqlite.SQLiteDatabase, android.database.sqlite.SQLiteOpenHelper, HistoryEntry, HistoryManager, Override

### Community 131 - "Highlights & Features"
Cohesion: 0.15
Nodes (12): 1. Real-Time AI Chat DOM Pruning, 2. Temporary Chat Vault & Session Converter, 3. Flow Speed: Universal Media Playback Controller, 4. YouTube Home Feed Cleaner & Instant Feedback, 5. RippleFrame: Full-Page Screenshot & Annotation Studio, Caspian for Firefox (Mozilla Firefox Browser Extension), Highlights & Features, How to Install in Firefox (+4 more)

### Community 132 - "caspian fox extension/speed_content.js"
Cohesion: 0.26
Nodes (12): applySpeedToAllMedia(), cycleToNextSpeed(), enforceRate(), findAllMediaElements(), injectHudStyles(), loadConfig(), observeRoot(), parseCycleList() (+4 more)

### Community 133 - ".setupFloatingPod"
Cohesion: 0.13
Nodes (8): android.graphics.RectF, android.util.LruCache, android.view.ViewGroup, CabRadialMenuView, Override, Paint, OnRadialActionSelectedListener, ShortcutItem

### Community 134 - "caspian fox extension/background.js"
Cohesion: 0.43
Nodes (5): captureTabWithQuotaRetry(), performRippleFrameCapture(), saveCaptureToDB(), studioTabIds, updateSpeedBadge()

### Community 138 - "package_extensions.py"
Cohesion: 0.53
Nodes (5): get_manifest_version(), main(), package_extension(), package_extensions.py Utility script to package Caspian Chromium and Firefox…, zip_folder()

## Knowledge Gaps
- **202 isolated node(s):** `studioTabIds`, `manifest_version`, `name`, `version`, `description` (+197 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 486 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **22 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `MainActivity` connect `MainActivity` to `android.webkit.JavascriptInterface`, `.playUiFeedbackSound`, `.setupFloatingPod`, `.restoreLastClosedBatch`, `CaspianDownloadManager`, `betac/MainActivity.java`, `android.net.Uri`, `WhirlpoolOverlayView`, `.showCaspianCustomPopup`, `CaspianWebView`, `.getTabById`, `.getYouTubeTab`, `android.view.MotionEvent`, `.dpToPx`, `WaveguardShield`, `.updateOmniboxState`, `.resolve`, `TabController`, `android.content.Context`, `SwipeableViewFlipper`, `android.view.View`, `BookmarkManager`?**
  _High betweenness centrality (0.219) - this node is a cross-community bridge._
- **Why does `CaspianBridge` connect `android.webkit.JavascriptInterface` to `MainActivity`, `WaveguardShield`, `betac/MainActivity.java`, `CaskManager`, `.getInstance`, `GitHubUpdateManager`, `.switchCaspianCask`?**
  _High betweenness centrality (0.073) - this node is a cross-community bridge._
- **Why does `MainActivity` connect `MainActivity` to `.switchTab`, `.setupFloatingYouTubeRemote`, `.onCreate`, `CaspianBridge`, `betac/MainActivity.java`, `android.net.Uri`, `android.view.View`, `.getActiveTab`, `android.content.Context`, `.onBackPressed`?**
  _High betweenness centrality (0.052) - this node is a cross-community bridge._
- **What connects `studioTabIds`, `manifest_version`, `name` to the rest of the system?**
  _202 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `android.webkit.JavascriptInterface` be split into smaller, more focused modules?**
  _Cohesion score 0.028160919540229885 - nodes in this community are weakly interconnected._
- **Should `MainActivity` be split into smaller, more focused modules?**
  _Cohesion score 0.027421541028047128 - nodes in this community are weakly interconnected._
- **Should `browser_control.js` be split into smaller, more focused modules?**
  _Cohesion score 0.05396825396825397 - nodes in this community are weakly interconnected._