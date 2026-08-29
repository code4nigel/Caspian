package com.caspian.betac;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.widget.PopupWindow;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.PathInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewFeature;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "CaspianFlow";
    private static final String PREFS_NAME = "CaspianFlowPrefs";

    public static class TabItem {
        public int id;
        public String title;
        public String nickname;
        public String url;
        public String service;
        public WebView webView;
        public boolean isDesktop = false;
        public boolean isReaderMode = false;
        public boolean isMuted = false;
        public boolean isFavorite = false;
        public boolean isIncognito = false;
        public String pendingPrompt = null;
        public Bitmap snapshotBitmap = null;

        public TabItem(int id, String title, String url, String service, WebView webView, boolean isIncognito) {
            this.id = id;
            this.title = title;
            this.url = url;
            this.service = service;
            this.webView = webView;
            this.isIncognito = isIncognito;
        }
    }

    public static class TabGroup {
        public String id;
        public String title;
        public String color;
        public String icon;
        public boolean isFavorite = false;
        public final List<Integer> tabIds = new ArrayList<>();

        public TabGroup(String id, String title, String color, String icon) {
            this.id = id;
            this.title = title;
            this.color = color != null ? color : "#ef4444";
            this.icon = icon != null ? icon : "📁";
        }
    }

    private final List<TabItem> tabsList = new ArrayList<>();
    private final List<TabGroup> tabGroupsList = new ArrayList<>();
    private final List<TabItem> closedTabsHistory = new ArrayList<>();
    private final Set<Integer> selectedGridTabIds = new HashSet<>();
    private int activeTabId = 1;
    private int secondarySplitTabId = -1;
    private int nextTabId = 2;
    private String currentGridGroupId = null;
    
    private int splitModeState = 0; // 0 = single, 1 = horizontal, 2 = vertical
    private boolean openLeftLinksToRight = false;
    private float splitRatio = 0.5f;
    private boolean isSheetOpen = false;
    private boolean isMasterSfxMuted = false;

    private AdBlockShield adBlockShield;
    private AICommandRouter.SearchEngine currentSearchEngine = AICommandRouter.SearchEngine.GOOGLE;

    private FrameLayout rootContainer;
    private FrameLayout omniboxHeaderWrapper;
    private LinearLayout omniboxHeader;
    private FrameLayout omniboxCapsule;
    private ImageButton omniboxBackBtn;
    private ImageButton omniboxForwardBtn;
    private boolean isDarkTheme = true;
    
    private LinearLayout omniboxUrlContainer;
    private ImageView omniboxShieldIcon;
    private EditText omniboxEditText;
    private ImageButton omniboxClearBtn;
    private ImageButton omniboxFinderBtn;
    private ImageButton omniboxVoiceBtn;

    private LinearLayout omniboxFinderContainer;
    private ImageButton omniboxFinderClose;
    private EditText omniboxFinderInput;
    private TextView omniboxFinderCount;
    private ImageButton omniboxFinderPrev;
    private ImageButton omniboxFinderNext;

    private ImageButton omniboxReloadBtn;
    private ImageButton omniboxToolbarsBtn;
    private ImageButton omniboxSplitBtn;
    private FrameLayout omniboxTabsBtn;
    private TextView omniboxTabsCount;
    private ImageButton omniboxMenuBtn;
    private ProgressBar browserProgressBar;

    private FrameLayout omniboxSuggestionsContainer;
    private LinearLayout omniboxClipboardChip;
    private TextView omniboxClipboardText;
    private LinearLayout omniboxSuggestionsList;

    private FrameLayout webviewsParentContainer;
    private FrameLayout webViewContainer;
    private LinearLayout splitViewContainer;
    private FrameLayout splitLeftContainer;
    private FrameLayout splitRightContainer;
    private View splitLeftTapMask;
    private View splitRightTapMask;
    private FrameLayout splitDivider;
    private View splitDividerHandle;

    private LinearLayout splitArenaBroadcastContainer;
    private TextView splitArenaLabel;
    private EditText splitArenaInput;
    private ImageButton splitArenaSendBtn;
    private ImageButton splitArenaCloseBtn;
    private int currentTextZoom = 100;

    private LinearLayout splitLeftControl;
    private ImageButton splitLeftMenuBtn;
    private ImageButton splitLeftCloseBtn;
    private LinearLayout splitRightControl;
    private ImageButton splitRightMenuBtn;
    private ImageButton splitRightCloseBtn;

    private FrameLayout tabGridOverlay;
    private TextView tabGridCountBadge;
    private ImageButton tabGridIncognitoBtn;
    private ImageButton tabGridCloseViewBtn;
    private EditText tabGridSearchInput;
    private LinearLayout tabGridGroupBanner;
    private ImageButton btnTabGridGroupBack;
    private TextView tabGridGroupBannerTitle;
    private Button btnTabGridGroupUngroup;
    private LinearLayout tabGridContentLayout;
    private GridLayout tabGridContainer;
    private FrameLayout tabGridFabAdd;
    private LinearLayout tabGridSplitActionBar;
    private Button btnTabGridSplitSelected;
    private Button btnTabGridGroupSelected;
    private Button btnTabGridCloseSelected;
    private Button btnTabGridDeselect;

    private FrameLayout modalNewTabPlatform;
    private ImageButton btnClosePlatformModal;
    private LinearLayout tileNewTabHub;
    private LinearLayout tileNewTabChatgpt;
    private LinearLayout tileNewTabGemini;
    private LinearLayout tileNewTabGoogle;
    private LinearLayout tileNewTabYoutube;
    private LinearLayout tileNewTabClaude;

    private FrameLayout splashOverlay;
    private TextureView splashTextureView;
    private MediaPlayer splashPlayer;

    private CardView floatingCaspianCard;
    private ImageView floatingCaspianIcon;
    private FrameLayout sheetOverlayContainer;
    private View sheetBackdrop;
    private WebView controlWebView;

    private FrameLayout ytFloatingRemoteContainer;
    private HorizontalScrollView ytFloatingRemoteScroll;
    private LinearLayout ytFloatingRemoteDock;
    private FrameLayout ytFloatingRemoteBall;
    private ImageButton ytRemoteClose;
    private ImageButton ytRemoteReload;
    private ImageButton ytRemoteFullscreen;
    private ImageButton ytRemotePrevVideo;
    private ImageButton ytRemoteSeekBack;
    private ImageButton ytRemotePlayPause;
    private ImageButton ytRemoteSeekFwd;
    private ImageButton ytRemoteNextVideo;
    private ImageButton ytRemoteMute;
    private TextView ytRemoteSpeedBtn;
    private TextView ytRemoteQualityBtn;
    private TextView ytRemoteDragHandle;
    private ImageButton ytRemoteShrinkBtn;
    private float ytCurrentSpeed = 1.0f;
    private boolean isYtRemoteExplicitlyHidden = false;

    private FrameLayout searchNavContainer;
    private HorizontalScrollView searchDockScroll;
    private LinearLayout searchDockExpanded;
    private FrameLayout searchNavBall;
    private ImageButton navDockClose;
    private ImageButton navDockReload;
    private ImageButton navBackBtn;
    private ImageButton navForwardBtn;
    private TextView searchDockUrl;
    private LinearLayout navFinderBox;
    private EditText navFinderInput;
    private TextView navFinderCount;
    private ImageButton navFinderBtn;
    private ImageButton navFinderPrev;
    private ImageButton navFinderNext;
    private ImageButton navScrollTopBtn;
    private ImageButton navScrollBottomBtn;
    private TextView navDockDragHandle;
    private ImageButton navDockShrinkBtn;
    private boolean isSearchNavExplicitlyHidden = true;
    private boolean isGoogleDockAutoCollapse = true;

    private FrameLayout chatgptDockContainer;
    private HorizontalScrollView chatgptDockScroll;
    private LinearLayout chatgptDockExpanded;
    private FrameLayout chatgptDockBall;
    private ImageButton chatgptDockClose;
    private ImageButton chatgptDockReload;
    private TextView chatgptDockToggleBtn;
    private TextView chatgptDockModeBtn;
    private TextView chatgptDockLimitBtn;
    private LinearLayout chatgptFinderBox;
    private EditText chatgptFinderInput;
    private TextView chatgptFinderCount;
    private ImageButton chatgptFinderBtn;
    private ImageButton chatgptFinderPrev;
    private ImageButton chatgptFinderNext;
    private ImageButton chatgptMsgUpBtn;
    private ImageButton chatgptMsgDownBtn;
    private TextView chatgptDockDragHandle;
    private ImageButton chatgptDockShrinkBtn;
    private boolean isChatgptDockExplicitlyHidden = false;

    private FrameLayout geminiDockContainer;
    private HorizontalScrollView geminiDockScroll;
    private LinearLayout geminiDockExpanded;
    private FrameLayout geminiDockBall;
    private ImageButton geminiDockClose;
    private ImageButton geminiDockReload;
    private TextView geminiDockToggleBtn;
    private TextView geminiDockLimitBtn;
    private LinearLayout geminiFinderBox;
    private EditText geminiFinderInput;
    private TextView geminiFinderCount;
    private ImageButton geminiFinderBtn;
    private ImageButton geminiFinderPrev;
    private ImageButton geminiFinderNext;
    private ImageButton geminiMsgUpBtn;
    private ImageButton geminiMsgDownBtn;
    private TextView geminiDockDragHandle;
    private ImageButton geminiDockShrinkBtn;
    private boolean isGeminiDockExplicitlyHidden = false;

    private int actionButtonClickCount = 0;

    private String podShape = "circle";
    private float podScale = 1.0f;
    private String podStartColor = "#00C4FF";
    private String podEndColor = "#0077B6";
    private float podOpacity = 1.0f;

    private float dX, dY;
    private float startRawX, startRawY;
    private boolean isDragging = false;
    private boolean isLongPressed = false;
    private boolean isLongPressedInThisGesture = false;
    private final Handler longPressHandler = new Handler(Looper.getMainLooper());
    private Runnable longPressRunnable;

    private AudioRecord audioRecord;
    private boolean isRecordingPcmAudio = false;
    private ByteArrayOutputStream pcmAudioBuffer;
    private Thread pcmRecordingThread;
    private boolean isRecordingSpeechMode = false;

    private FrameLayout speechWaveformContainer;
    private SpeechWaveformView speechWaveformView;
    private SpeechRecognizer speechRecognizer;
    private Intent speechIntent;
    private final StringBuilder nativeSpeechBuffer = new StringBuilder();
    private final static int MIC_PERMISSION_REQUEST_CODE = 1002;
    private boolean isUniversalVoiceActive = false;

    private boolean isDebugRecording = false;
    private final StringBuilder debugLogBuffer = new StringBuilder();

    private View customView;
    private WebChromeClient.CustomViewCallback customViewCallback;
    private FrameLayout fullscreenContainer;

    private ValueCallback<Uri[]> uploadMessage;
    private final static int FILECHOOSER_RESULTCODE = 1;

    private SoundPool soundPool;
    private final Map<String, Integer> soundIdMap = new ConcurrentHashMap<>();
    private final Map<String, String> assetScriptCache = new ConcurrentHashMap<>();

    private static final String DESKTOP_UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36";
    private static final String MOBILE_UA = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
            setContentView(R.layout.activity_main);
        } catch (Throwable t) {
            Log.e(TAG, "setContentView error: ", t);
        }

        try { adBlockShield = new AdBlockShield(this); } catch (Throwable ignored) {}
        try { initSoundPool(); } catch (Throwable ignored) {}
        try { bindViews(); } catch (Throwable ignored) {}
        try { loadPodPreferences(); } catch (Throwable ignored) {}
        try { loadTabGroups(); } catch (Throwable ignored) {}

        try {
            SharedPreferences appPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            actionButtonClickCount = appPrefs.getInt("action_btn_click_count", 0);
            isYtRemoteExplicitlyHidden = !appPrefs.getBoolean("yt_dock_enabled", true);
            isSearchNavExplicitlyHidden = !appPrefs.getBoolean("google_dock_enabled", false);
            isChatgptDockExplicitlyHidden = !appPrefs.getBoolean("chatgpt_dock_enabled", true);
            isGeminiDockExplicitlyHidden = !appPrefs.getBoolean("gemini_dock_enabled", true);
            isDarkTheme = !"light".equalsIgnoreCase(appPrefs.getString("theme", "dark"));
        } catch (Throwable ignored) {}

        try { updateThemeStyling(); } catch (Throwable ignored) {}
        try { setupOmniboxListeners(); } catch (Throwable ignored) {}
        try { setupFloatingPod(); } catch (Throwable ignored) {}
        try { setupControlSheet(); } catch (Throwable ignored) {}
        try { setupVoiceVisualizer(); } catch (Throwable ignored) {}
        try { setupLiquidGlassYouTubeRemote(); } catch (Throwable ignored) {}
        try { setupLiquidGlassGoogleDock(); } catch (Throwable ignored) {}
        try { setupLiquidGlassChatGPTDock(); } catch (Throwable ignored) {}
        try { setupLiquidGlassGeminiDock(); } catch (Throwable ignored) {}
        try { setupSplitFloatingControls(); } catch (Throwable ignored) {}
        try { setupSplitDividerDrag(); } catch (Throwable ignored) {}
        try { setupModernTabGridOverlay(); } catch (Throwable ignored) {}
        try { setupPlatformModal(); } catch (Throwable ignored) {}
        try { setupOmniboxSwipeTabSwitcher(); } catch (Throwable ignored) {}
        try { setupOmniboxSuggestions(); } catch (Throwable ignored) {}
        try { initCaspianBetaASplash(); } catch (Throwable ignored) {}

        try {
            restoreOpenTabsState();
        } catch (Throwable t) {
            Log.e(TAG, "restoreOpenTabsState error: ", t);
        }
    }

    public void saveOpenTabsState() {
        try {
            JSONArray arr = new JSONArray();
            for (TabItem tab : tabsList) {
                if (tab.isIncognito) continue; // Privacy: Never persist incognito private tabs
                JSONObject obj = new JSONObject();
                obj.put("id", tab.id);
                obj.put("title", tab.title);
                obj.put("nickname", tab.nickname);
                obj.put("url", tab.url);
                obj.put("service", tab.service);
                obj.put("isDesktop", tab.isDesktop);
                obj.put("isIncognito", false);
                obj.put("isMuted", tab.isMuted);
                obj.put("isFavorite", tab.isFavorite);
                arr.put(obj);
            }
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            prefs.edit()
                    .putString("caspian_open_tabs_json", arr.toString())
                    .putInt("caspian_active_tab_id", activeTabId)
                    .putInt("caspian_secondary_split_id", secondarySplitTabId)
                    .putInt("caspian_split_mode_state", splitModeState)
                    .putFloat("caspian_split_ratio", splitRatio)
                    .putInt("caspian_next_tab_id", nextTabId)
                    .apply();
        } catch (Exception e) {
            Log.e(TAG, "saveOpenTabsState error: " + e.getMessage());
        }
    }

    public void restoreOpenTabsState() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String tabsJson = prefs.getString("caspian_open_tabs_json", null);
        int savedActiveId = prefs.getInt("caspian_active_tab_id", 1);
        int savedSecondarySplitId = prefs.getInt("caspian_secondary_split_id", -1);
        int savedSplitMode = prefs.getInt("caspian_split_mode_state", 0);
        float savedSplitRatio = prefs.getFloat("caspian_split_ratio", 0.5f);
        nextTabId = prefs.getInt("caspian_next_tab_id", 2);

        if (tabsJson != null && !tabsJson.isEmpty()) {
            try {
                JSONArray arr = new JSONArray(tabsJson);
                if (arr.length() > 0) {
                    tabsList.clear();
                    int maxId = 0;
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        int id = obj.optInt("id", i + 1);
                        String url = obj.optString("url", "https://www.google.com");
                        String service = obj.optString("service", "web");
                        String nickname = obj.optString("nickname", null);
                        boolean isDesktop = obj.optBoolean("isDesktop", false);
                        boolean isIncognito = obj.optBoolean("isIncognito", false);
                        boolean isMuted = obj.optBoolean("isMuted", false);
                        boolean isFavorite = obj.optBoolean("isFavorite", false);
                        String title = obj.optString("title", null);

                        TabItem item = createNewTabInstance(id, url, service, null, isIncognito);
                        item.title = title;
                        item.nickname = nickname;
                        item.isDesktop = isDesktop;
                        item.isMuted = isMuted;
                        item.isFavorite = isFavorite;
                        tabsList.add(item);
                        if (id > maxId) maxId = id;
                    }
                    if (nextTabId <= maxId) nextTabId = maxId + 1;

                    activeTabId = savedActiveId;
                    if (getTabById(activeTabId) == null && !tabsList.isEmpty()) {
                        activeTabId = tabsList.get(0).id;
                    }

                    if (savedSplitMode > 0 && savedSecondarySplitId != -1 && getTabById(savedSecondarySplitId) != null && savedSecondarySplitId != activeTabId) {
                        secondarySplitTabId = savedSecondarySplitId;
                        splitModeState = savedSplitMode;
                        splitRatio = savedSplitRatio;
                        applySplitViewLayout();
                    } else {
                        splitModeState = 0;
                        secondarySplitTabId = -1;
                        switchToTab(activeTabId);
                    }
                    updateOmniboxState();
                    return;
                }
            } catch (Exception e) {
                Log.e(TAG, "restoreOpenTabsState error: " + e.getMessage());
            }
        }

        // Fallback default: Open Caspian Hub Tab
        TabItem initialTab = createNewTabInstance(1, "file:///android_asset/launch_hub.html", "hub", null, false);
        initialTab.title = "Caspian Hub";
        tabsList.add(initialTab);
        activeTabId = 1;
        switchToTab(1);
        updateOmniboxState();
    }

    public void loadTabGroups() {
        tabGroupsList.clear();
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String jsonStr = prefs.getString("caspian_tab_groups", "[]");
        try {
            JSONArray arr = new JSONArray(jsonStr);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                TabGroup group = new TabGroup(
                        obj.optString("id", "group_" + i),
                        obj.optString("title", "Tab Group"),
                        obj.optString("color", "#ef4444"),
                        obj.optString("icon", "📁")
                );
                group.isFavorite = obj.optBoolean("isFavorite", false);
                JSONArray tabIdsArr = obj.optJSONArray("tabIds");
                if (tabIdsArr != null) {
                    for (int j = 0; j < tabIdsArr.length(); j++) {
                        group.tabIds.add(tabIdsArr.getInt(j));
                    }
                }
                tabGroupsList.add(group);
            }
        } catch (Exception e) {
            Log.e(TAG, "loadTabGroups error: " + e.getMessage());
        }
    }

    public void saveTabGroups() {
        JSONArray arr = new JSONArray();
        for (TabGroup g : tabGroupsList) {
            try {
                JSONObject obj = new JSONObject();
                obj.put("id", g.id);
                obj.put("title", g.title);
                obj.put("color", g.color);
                obj.put("icon", g.icon);
                obj.put("isFavorite", g.isFavorite);
                JSONArray idsArr = new JSONArray();
                for (int id : g.tabIds) idsArr.put(id);
                obj.put("tabIds", idsArr);
                arr.put(obj);
            } catch (Exception ignored) {}
        }
        String jsonStr = arr.toString();
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putString("caspian_tab_groups", jsonStr).apply();
    }

    private void initCaspianBetaASplash() {
        if (splashOverlay == null || splashTextureView == null) return;

        try {
            final Runnable dismissSplash = () -> {
                try {
                    if (splashPlayer != null) {
                        splashPlayer.stop();
                        splashPlayer.release();
                        splashPlayer = null;
                    }
                } catch (Exception ignored) {}
                if (splashOverlay != null) {
                    splashOverlay.animate()
                            .alpha(0f)
                            .setDuration(300)
                            .withEndAction(() -> {
                                if (splashOverlay != null) splashOverlay.setVisibility(View.GONE);
                            })
                            .start();
                }
            };

            splashTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
                    try {
                        Surface surface = new Surface(surfaceTexture);
                        splashPlayer = new MediaPlayer();
                        splashPlayer.setSurface(surface);

                        AudioAttributes attr = new AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .build();
                        splashPlayer.setAudioAttributes(attr);
                        splashPlayer.setVolume(1.0f, 1.0f);

                        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.caspian_splash_v22);
                        splashPlayer.setDataSource(MainActivity.this, videoUri);

                        splashPlayer.setOnVideoSizeChangedListener((mp, videoWidth, videoHeight) -> {
                            if (videoWidth > 0 && videoHeight > 0 && splashOverlay != null) {
                                int screenWidth = splashOverlay.getWidth();
                                int screenHeight = splashOverlay.getHeight();
                                if (screenWidth > 0 && screenHeight > 0) {
                                    float scaleX = (float) screenWidth / videoWidth;
                                    float scaleY = (float) screenHeight / videoHeight;
                                    float maxScale = Math.max(scaleX, scaleY);

                                    FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) splashTextureView.getLayoutParams();
                                    lp.width = (int) (videoWidth * maxScale);
                                    lp.height = (int) (videoHeight * maxScale);
                                    lp.gravity = Gravity.CENTER;
                                    splashTextureView.setLayoutParams(lp);
                                }
                            }
                        });

                        splashPlayer.setOnPreparedListener(mp -> splashPlayer.start());
                        splashPlayer.setOnCompletionListener(mp -> dismissSplash.run());
                        splashPlayer.setOnErrorListener((mp, what, extra) -> {
                            dismissSplash.run();
                            return true;
                        });

                        splashPlayer.prepareAsync();
                    } catch (Exception e) {
                        dismissSplash.run();
                    }
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {}

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                    if (splashPlayer != null) {
                        try { splashPlayer.release(); } catch (Exception ignored) {}
                        splashPlayer = null;
                    }
                    return true;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture surface) {}
            });

            splashOverlay.setOnClickListener(v -> dismissSplash.run());
            splashOverlay.postDelayed(dismissSplash, 3200);
        } catch (Exception e) {
            if (splashOverlay != null) splashOverlay.setVisibility(View.GONE);
        }
    }

    private void initSoundPool() {
        try {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            soundPool = new SoundPool.Builder()
                    .setMaxStreams(8)
                    .setAudioAttributes(audioAttributes)
                    .build();

            new Thread(() -> {
                String[] sfxFiles = {
                        "sfx/tap_main.mp3",
                        "sfx/tap_button.mp3",
                        "sfx/tap_alternate.mp3",
                        "sfx/pop_button.mp3",
                        "sfx/pop_button_v2.mp3",
                        "sfx/pop_click.mp3",
                        "sfx/pop_unknown_v1.mp3"
                };
                for (String path : sfxFiles) {
                    try {
                        AssetFileDescriptor afd = getAssets().openFd(path);
                        int sid = soundPool.load(afd, 1);
                        afd.close();
                        soundIdMap.put(path, sid);
                        soundIdMap.put(path.replace("sfx/", ""), sid);
                    } catch (Exception ignored) {}
                }
            }).start();
        } catch (Exception e) {
            Log.e(TAG, "SoundPool init error: " + e.getMessage());
        }
    }

    private float currentSfxVolume = 0.5f;

    public void setSfxVolume(float volume) {
        this.currentSfxVolume = Math.max(0.0f, Math.min(1.0f, volume));
        try {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            prefs.edit().putString("sfx_volume", String.valueOf(this.currentSfxVolume)).apply();
        } catch (Exception ignored) {}
    }

    public void playAssetSound(String assetPath) {
        if (isMasterSfxMuted || soundPool == null || assetPath == null) return;
        try {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            if (prefs.contains("sfx_volume")) {
                try {
                    currentSfxVolume = Float.parseFloat(prefs.getString("sfx_volume", "0.5"));
                } catch (Exception ignored) {}
            }
            Integer sid = soundIdMap.get(assetPath);
            if (sid == null) sid = soundIdMap.get(assetPath.replace("sfx/", ""));
            if (sid != null && sid > 0) {
                soundPool.play(sid, currentSfxVolume, currentSfxVolume, 1, 0, 1.0f);
            }
        } catch (Exception ignored) {}
    }

    public void setMasterSfxMuted(boolean muted) {
        this.isMasterSfxMuted = muted;
        try {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            prefs.edit().putBoolean("master_sfx_muted", muted).apply();
        } catch (Exception ignored) {}
    }

    public void playUiFeedbackSound(String soundType) {
        if (isMasterSfxMuted) return;
        try {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            String key = "tb_clicks";
            String defaultFile = "pop_click.mp3";

            if ("ta".equals(soundType) || "action_btn".equals(soundType) || "action".equals(soundType)) {
                key = "ta";
                defaultFile = "pop_click.mp3";
            } else if ("tm_tabs".equals(soundType) || "main_tabs".equals(soundType) || "main_tab".equals(soundType)) {
                key = "tm_tabs";
                defaultFile = "pop_button.mp3";
            } else if ("tb_clicks".equals(soundType) || "tabs".equals(soundType) || "tab".equals(soundType) || "browser_tab".equals(soundType) || "tap".equals(soundType)) {
                key = "tb_clicks";
                defaultFile = "pop_click.mp3";
            } else if ("tm_header".equals(soundType) || "header".equals(soundType) || "reload".equals(soundType)) {
                key = "tm_header";
                defaultFile = "tap_main.mp3";
            } else if ("tb_close".equals(soundType) || "close".equals(soundType)) {
                key = "tb_close";
                defaultFile = "tap_button.mp3";
            } else if ("tb_modal".equals(soundType) || "modal".equals(soundType)) {
                key = "tb_modal";
                defaultFile = "tap_button.mp3";
            } else if (soundType != null && !soundType.isEmpty()) {
                key = soundType;
            }

            boolean isEnabled = !"false".equalsIgnoreCase(prefs.getString("sfx_enabled_" + key, "true"));
            if (!isEnabled) return;

            String sfxFile = prefs.getString("sfx_file_" + key, defaultFile);
            if (sfxFile != null && !sfxFile.isEmpty()) {
                playAssetSound("sfx/" + sfxFile);
            }

            View view = getWindow().getDecorView();
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        } catch (Exception ignored) {}
    }

    private void bindViews() {
        try {
            rootContainer = findViewById(R.id.root_container);
            omniboxHeaderWrapper = findViewById(R.id.omnibox_header_wrapper);
            omniboxHeader = findViewById(R.id.omnibox_header);
            omniboxCapsule = findViewById(R.id.omnibox_capsule);
            omniboxBackBtn = findViewById(R.id.omnibox_back_btn);
            omniboxForwardBtn = findViewById(R.id.omnibox_forward_btn);

            omniboxUrlContainer = findViewById(R.id.omnibox_url_container);
            omniboxShieldIcon = findViewById(R.id.omnibox_shield_icon);
            omniboxEditText = findViewById(R.id.omnibox_edit_text);
            omniboxClearBtn = findViewById(R.id.omnibox_clear_btn);
            omniboxVoiceBtn = findViewById(R.id.omnibox_voice_btn);

            omniboxFinderContainer = findViewById(R.id.omnibox_finder_container);
            omniboxFinderClose = findViewById(R.id.omnibox_finder_close);
            omniboxFinderInput = findViewById(R.id.omnibox_finder_input);
            omniboxFinderCount = findViewById(R.id.omnibox_finder_count);
            omniboxFinderPrev = findViewById(R.id.omnibox_finder_prev);
            omniboxFinderNext = findViewById(R.id.omnibox_finder_next);

            omniboxReloadBtn = findViewById(R.id.omnibox_reload_btn);
            omniboxToolbarsBtn = findViewById(R.id.omnibox_toolbars_btn);
            omniboxSplitBtn = findViewById(R.id.omnibox_split_btn);
            omniboxTabsBtn = findViewById(R.id.omnibox_tabs_btn);
            omniboxTabsCount = findViewById(R.id.omnibox_tabs_count);
            omniboxMenuBtn = findViewById(R.id.omnibox_menu_btn);
            browserProgressBar = findViewById(R.id.browser_progress_bar);

            omniboxSuggestionsContainer = findViewById(R.id.omnibox_suggestions_container);
            omniboxClipboardChip = findViewById(R.id.omnibox_clipboard_chip);
            omniboxClipboardText = findViewById(R.id.omnibox_clipboard_text);
            omniboxSuggestionsList = findViewById(R.id.omnibox_suggestions_list);

            webviewsParentContainer = findViewById(R.id.webviews_parent_container);
            webViewContainer = findViewById(R.id.webview_container);
            splitViewContainer = findViewById(R.id.split_view_container);
            splitLeftContainer = findViewById(R.id.split_left_container);
            splitRightContainer = findViewById(R.id.split_right_container);
            splitLeftTapMask = findViewById(R.id.split_left_tap_mask);
            splitRightTapMask = findViewById(R.id.split_right_tap_mask);
            splitDivider = findViewById(R.id.split_divider);
            splitDividerHandle = findViewById(R.id.split_divider_handle);

            splitArenaBroadcastContainer = findViewById(R.id.split_arena_broadcast_container);
            splitArenaInput = findViewById(R.id.split_arena_input);
            splitArenaSendBtn = findViewById(R.id.split_arena_send_btn);
            splitArenaCloseBtn = findViewById(R.id.split_arena_close_btn);

            splitLeftControl = findViewById(R.id.split_left_control);
            splitLeftMenuBtn = findViewById(R.id.split_left_menu_btn);
            splitLeftCloseBtn = findViewById(R.id.split_left_close_btn);
            splitRightControl = findViewById(R.id.split_right_control);
            splitRightMenuBtn = findViewById(R.id.split_right_menu_btn);
            splitRightCloseBtn = findViewById(R.id.split_right_close_btn);

            tabGridOverlay = findViewById(R.id.tab_grid_overlay);
            tabGridCountBadge = findViewById(R.id.tab_grid_count_badge);
            tabGridCloseViewBtn = findViewById(R.id.tab_grid_close_view_btn);
            tabGridSearchInput = findViewById(R.id.tab_grid_search_input);
            tabGridGroupBanner = findViewById(R.id.tab_grid_group_banner);
            btnTabGridGroupBack = findViewById(R.id.btn_tab_grid_group_back);
            tabGridGroupBannerTitle = findViewById(R.id.tab_grid_group_banner_title);
            btnTabGridGroupUngroup = findViewById(R.id.btn_tab_grid_group_ungroup);
            tabGridContentLayout = findViewById(R.id.tab_grid_content_layout);
            tabGridContainer = findViewById(R.id.tab_grid_container);
            tabGridFabAdd = findViewById(R.id.tab_grid_fab_add);
            tabGridSplitActionBar = findViewById(R.id.tab_grid_split_action_bar);
            btnTabGridSplitSelected = findViewById(R.id.btn_tab_grid_split_selected);
            btnTabGridGroupSelected = findViewById(R.id.btn_tab_grid_group_selected);
            btnTabGridCloseSelected = findViewById(R.id.btn_tab_grid_close_selected);
            btnTabGridDeselect = findViewById(R.id.btn_tab_grid_deselect);

            modalNewTabPlatform = findViewById(R.id.modal_new_tab_platform);
            btnClosePlatformModal = findViewById(R.id.btn_close_platform_modal);
            tileNewTabHub = findViewById(R.id.tile_new_tab_hub);
            tileNewTabChatgpt = findViewById(R.id.tile_new_tab_chatgpt);
            tileNewTabGemini = findViewById(R.id.tile_new_tab_gemini);
            tileNewTabGoogle = findViewById(R.id.tile_new_tab_google);
            tileNewTabYoutube = findViewById(R.id.tile_new_tab_youtube);
            tileNewTabClaude = findViewById(R.id.tile_new_tab_claude);

            splashOverlay = findViewById(R.id.splash_overlay);
            splashTextureView = findViewById(R.id.splash_textureview);

            floatingCaspianCard = findViewById(R.id.floating_caspian_card);
            floatingCaspianIcon = findViewById(R.id.floating_caspian_icon);
            sheetOverlayContainer = findViewById(R.id.sheet_overlay_container);
            sheetBackdrop = findViewById(R.id.sheet_backdrop);
            controlWebView = findViewById(R.id.control_webview);

            ytFloatingRemoteContainer = findViewById(R.id.yt_floating_remote_container);
            ytFloatingRemoteScroll = findViewById(R.id.yt_floating_remote_scroll);
            ytFloatingRemoteDock = findViewById(R.id.yt_floating_remote_dock);
            ytFloatingRemoteBall = findViewById(R.id.yt_floating_remote_ball);
            ytRemoteClose = findViewById(R.id.yt_remote_close);
            ytRemoteReload = findViewById(R.id.yt_remote_reload);
            ytRemoteFullscreen = findViewById(R.id.yt_remote_fullscreen);
            ytRemotePrevVideo = findViewById(R.id.yt_remote_prev_video);
            ytRemoteSeekBack = findViewById(R.id.yt_remote_seek_back);
            ytRemotePlayPause = findViewById(R.id.yt_remote_play_pause);
            ytRemoteSeekFwd = findViewById(R.id.yt_remote_seek_fwd);
            ytRemoteNextVideo = findViewById(R.id.yt_remote_next_video);
            ytRemoteMute = findViewById(R.id.yt_remote_mute);
            ytRemoteSpeedBtn = findViewById(R.id.yt_remote_speed_btn);
            ytRemoteQualityBtn = findViewById(R.id.yt_remote_quality_btn);
            ytRemoteDragHandle = findViewById(R.id.yt_remote_drag_handle);
            ytRemoteShrinkBtn = findViewById(R.id.yt_remote_shrink_btn);

            searchNavContainer = findViewById(R.id.search_nav_container);
            searchDockScroll = findViewById(R.id.search_dock_scroll);
            searchDockExpanded = findViewById(R.id.search_dock_expanded);
            searchNavBall = findViewById(R.id.search_nav_ball);
            navDockClose = findViewById(R.id.nav_dock_close);
            navDockReload = findViewById(R.id.nav_dock_reload);
            navBackBtn = findViewById(R.id.nav_back_btn);
            navForwardBtn = findViewById(R.id.nav_forward_btn);
            searchDockUrl = findViewById(R.id.search_dock_url);
            navFinderBox = findViewById(R.id.nav_finder_box);
            navFinderInput = findViewById(R.id.nav_finder_input);
            navFinderCount = findViewById(R.id.nav_finder_count);
            navFinderBtn = findViewById(R.id.nav_finder_btn);
            navFinderPrev = findViewById(R.id.nav_finder_prev);
            navFinderNext = findViewById(R.id.nav_finder_next);
            navScrollTopBtn = findViewById(R.id.nav_scroll_top_btn);
            navScrollBottomBtn = findViewById(R.id.nav_scroll_bottom_btn);
            navDockDragHandle = findViewById(R.id.nav_dock_drag_handle);
            navDockShrinkBtn = findViewById(R.id.nav_dock_shrink_btn);

            chatgptDockContainer = findViewById(R.id.chatgpt_dock_container);
            chatgptDockScroll = findViewById(R.id.chatgpt_dock_scroll);
            chatgptDockExpanded = findViewById(R.id.chatgpt_dock_expanded);
            chatgptDockBall = findViewById(R.id.chatgpt_dock_ball);
            chatgptDockClose = findViewById(R.id.chatgpt_dock_close);
            chatgptDockReload = findViewById(R.id.chatgpt_dock_reload);
            chatgptDockToggleBtn = findViewById(R.id.chatgpt_dock_toggle_btn);
            chatgptDockModeBtn = findViewById(R.id.chatgpt_dock_mode_btn);
            chatgptDockLimitBtn = findViewById(R.id.chatgpt_dock_limit_btn);
            chatgptFinderBox = findViewById(R.id.chatgpt_finder_box);
            chatgptFinderInput = findViewById(R.id.chatgpt_finder_input);
            chatgptFinderCount = findViewById(R.id.chatgpt_finder_count);
            chatgptFinderBtn = findViewById(R.id.chatgpt_finder_btn);
            chatgptFinderPrev = findViewById(R.id.chatgpt_finder_prev);
            chatgptFinderNext = findViewById(R.id.chatgpt_finder_next);
            chatgptMsgUpBtn = findViewById(R.id.chatgpt_msg_up_btn);
            chatgptMsgDownBtn = findViewById(R.id.chatgpt_msg_down_btn);
            chatgptDockDragHandle = findViewById(R.id.chatgpt_dock_drag_handle);
            chatgptDockShrinkBtn = findViewById(R.id.chatgpt_dock_shrink_btn);

            geminiDockContainer = findViewById(R.id.gemini_dock_container);
            geminiDockScroll = findViewById(R.id.gemini_dock_scroll);
            geminiDockExpanded = findViewById(R.id.gemini_dock_expanded);
            geminiDockBall = findViewById(R.id.gemini_dock_ball);
            geminiDockClose = findViewById(R.id.gemini_dock_close);
            geminiDockReload = findViewById(R.id.gemini_dock_reload);
            geminiDockToggleBtn = findViewById(R.id.gemini_dock_toggle_btn);
            geminiDockLimitBtn = findViewById(R.id.gemini_dock_limit_btn);
            geminiFinderBox = findViewById(R.id.gemini_finder_box);
            geminiFinderInput = findViewById(R.id.gemini_finder_input);
            geminiFinderCount = findViewById(R.id.gemini_finder_count);
            geminiFinderBtn = findViewById(R.id.gemini_finder_btn);
            geminiFinderPrev = findViewById(R.id.gemini_finder_prev);
            geminiFinderNext = findViewById(R.id.gemini_finder_next);
            geminiMsgUpBtn = findViewById(R.id.gemini_msg_up_btn);
            geminiMsgDownBtn = findViewById(R.id.gemini_msg_down_btn);
            geminiDockDragHandle = findViewById(R.id.gemini_dock_drag_handle);
            geminiDockShrinkBtn = findViewById(R.id.gemini_dock_shrink_btn);

            speechWaveformContainer = findViewById(R.id.speech_waveform_container);
            fullscreenContainer = findViewById(R.id.fullscreen_container);

            if (floatingCaspianCard != null) floatingCaspianCard.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            if (omniboxHeaderWrapper != null) omniboxHeaderWrapper.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            if (sheetOverlayContainer != null) sheetOverlayContainer.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } catch (Exception e) {
            Log.e(TAG, "bindViews error: " + e.getMessage());
        }
    }

    private void setupPlatformModal() {
        if (modalNewTabPlatform == null) return;

        btnClosePlatformModal.setOnClickListener(v -> {
            playUiFeedbackSound("tap");
            modalNewTabPlatform.setVisibility(View.GONE);
        });

        modalNewTabPlatform.setOnClickListener(v -> modalNewTabPlatform.setVisibility(View.GONE));

        View.OnClickListener tileClick = v -> {
            playUiFeedbackSound("tap");
            modalNewTabPlatform.setVisibility(View.GONE);
            hideTabGridView();
            if (v == tileNewTabHub) addNewTab("hub", "", "file:///android_asset/launch_hub.html", false);
            else if (v == tileNewTabChatgpt) addNewTab("chatgpt", "", "https://chatgpt.com", false);
            else if (v == tileNewTabGemini) addNewTab("gemini", "", "https://gemini.google.com/app", false);
            else if (v == tileNewTabGoogle) addNewTab("web", "", "https://www.google.com", false);
            else if (v == tileNewTabYoutube) addNewTab("youtube", "", "https://m.youtube.com", false);
            else if (v == tileNewTabClaude) addNewTab("claude", "", "https://claude.ai/new", false);
        };

        tileNewTabHub.setOnClickListener(tileClick);
        tileNewTabChatgpt.setOnClickListener(tileClick);
        tileNewTabGemini.setOnClickListener(tileClick);
        tileNewTabGoogle.setOnClickListener(tileClick);
        tileNewTabYoutube.setOnClickListener(tileClick);
        tileNewTabClaude.setOnClickListener(tileClick);
    }

    private void captureTabSnapshot(TabItem tab) {
        if (tab == null || tab.webView == null) return;
        tab.webView.post(() -> {
            try {
                int w = tab.webView.getWidth();
                int h = tab.webView.getHeight();
                if (w > 0 && h > 0) {
                    Bitmap bmp = Bitmap.createBitmap(w / 2, h / 2, Bitmap.Config.RGB_565);
                    Canvas canvas = new Canvas(bmp);
                    canvas.scale(0.5f, 0.5f);
                    tab.webView.draw(canvas);
                    tab.snapshotBitmap = bmp;
                }
            } catch (Exception ignored) {}
        });
    }

    private void setupModernTabGridOverlay() {
        if (tabGridOverlay == null) return;

        tabGridCloseViewBtn.setOnClickListener(v -> {
            playUiFeedbackSound("tap");
            hideTabGridView();
        });

        if (btnTabGridGroupBack != null) {
            btnTabGridGroupBack.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                currentGridGroupId = null;
                renderTabGridCards(tabGridSearchInput.getText().toString());
            });
        }

        if (btnTabGridGroupUngroup != null) {
            btnTabGridGroupUngroup.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                if (currentGridGroupId != null) {
                    tabGroupsList.removeIf(g -> g.id.equals(currentGridGroupId));
                    saveTabGroups();
                    currentGridGroupId = null;
                    renderTabGridCards(tabGridSearchInput.getText().toString());
                    Toast.makeText(this, "Group dissolved", Toast.LENGTH_SHORT).show();
                }
            });
        }

        tabGridFabAdd.setOnClickListener(v -> {
            playUiFeedbackSound("tap");
            if (modalNewTabPlatform != null) {
                modalNewTabPlatform.setVisibility(View.VISIBLE);
                modalNewTabPlatform.bringToFront();
            }
        });

        btnTabGridDeselect.setOnClickListener(v -> {
            playUiFeedbackSound("tap");
            selectedGridTabIds.clear();
            tabGridSplitActionBar.setVisibility(View.GONE);
            renderTabGridCards(tabGridSearchInput.getText().toString());
        });

        btnTabGridSplitSelected.setOnClickListener(v -> {
            playUiFeedbackSound("tap");
            if (selectedGridTabIds.size() == 2) {
                List<Integer> ids = new ArrayList<>(selectedGridTabIds);
                activeTabId = ids.get(0);
                secondarySplitTabId = ids.get(1);
                selectedGridTabIds.clear();
                tabGridSplitActionBar.setVisibility(View.GONE);
                hideTabGridView();
                splitModeState = 1;
                applySplitViewLayout();
                Toast.makeText(this, "🔀 Split Screen Activated for Selected Tabs", Toast.LENGTH_SHORT).show();
            }
        });

        btnTabGridGroupSelected.setOnClickListener(v -> {
            playUiFeedbackSound("tap");
            promptCreateTabGroup();
        });

        btnTabGridCloseSelected.setOnClickListener(v -> {
            playUiFeedbackSound("tap");
            for (int id : new ArrayList<>(selectedGridTabIds)) {
                closeTab(id);
            }
            selectedGridTabIds.clear();
            tabGridSplitActionBar.setVisibility(View.GONE);
            renderTabGridCards(tabGridSearchInput.getText().toString());
            Toast.makeText(this, "Selected tabs closed", Toast.LENGTH_SHORT).show();
        });

        tabGridSearchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                renderTabGridCards(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void promptCreateTabGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("📁 Create Tab Group");
        final EditText input = new EditText(this);
        input.setHint("Group Name (e.g., Research, AI, Work)");
        input.setTextColor(Color.WHITE);
        input.setHintTextColor(0x88A2A9A9);
        input.setBackgroundColor(0xFF0D1524);
        input.setPadding(24, 20, 24, 20);
        builder.setView(input);

        builder.setPositiveButton("Create", (dialog, which) -> {
            String name = input.getText().toString().trim();
            if (name.isEmpty()) name = "Group " + (tabGroupsList.size() + 1);

            TabGroup newGroup = new TabGroup("group_" + System.currentTimeMillis(), name, "#3b82f6", "📁");
            newGroup.tabIds.addAll(selectedGridTabIds);

            for (TabGroup g : tabGroupsList) {
                g.tabIds.removeAll(selectedGridTabIds);
            }
            tabGroupsList.removeIf(g -> g.tabIds.isEmpty());
            tabGroupsList.add(newGroup);
            saveTabGroups();

            selectedGridTabIds.clear();
            tabGridSplitActionBar.setVisibility(View.GONE);
            renderTabGridCards(tabGridSearchInput.getText().toString());
            Toast.makeText(this, "📁 Tab Group '" + name + "' Created", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    public void showTabGridView() {
        if (tabGridOverlay == null) return;
        TabItem currentTab = getTabById(activeTabId);
        if (currentTab != null) captureTabSnapshot(currentTab);

        loadTabGroups();
        tabGridOverlay.setVisibility(View.VISIBLE);
        tabGridOverlay.bringToFront();
        if (floatingCaspianCard != null) floatingCaspianCard.bringToFront();
        tabGridCountBadge.setText(String.valueOf(tabsList.size()));
        selectedGridTabIds.clear();
        currentGridGroupId = null;
        tabGridSplitActionBar.setVisibility(View.GONE);
        renderTabGridCards("");
        playAssetSound("sfx/pop_click.mp3");
    }

    public void hideTabGridView() {
        if (tabGridOverlay != null) {
            tabGridOverlay.setVisibility(View.GONE);
            selectedGridTabIds.clear();
            currentGridGroupId = null;
            tabGridSplitActionBar.setVisibility(View.GONE);
            if (modalNewTabPlatform != null) modalNewTabPlatform.setVisibility(View.GONE);
            hideKeyboard();
        }
    }

    private void renderTabGridCards(String filterQuery) {
        if (tabGridContentLayout == null) return;
        tabGridContentLayout.removeAllViews();

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        // Total horizontal inset = 14dp padding left + 14dp padding right + 6dp margin * 4 = 52dp
        int totalHorizontalPaddingPx = dpToPx(14 * 2 + 6 * 4);
        int cardWidth = (screenWidth - totalHorizontalPaddingPx) / 2;

        if (currentGridGroupId != null) {
            TabGroup activeGroup = null;
            for (TabGroup g : tabGroupsList) {
                if (g.id.equals(currentGridGroupId)) {
                    activeGroup = g;
                    break;
                }
            }

            if (tabGridGroupBanner != null) {
                tabGridGroupBanner.setVisibility(View.VISIBLE);
                if (tabGridGroupBannerTitle != null && activeGroup != null) {
                    tabGridGroupBannerTitle.setText(activeGroup.icon + " " + activeGroup.title + " (" + activeGroup.tabIds.size() + " tabs)");
                }
            }

            GridLayout grid = new GridLayout(this);
            grid.setColumnCount(2);
            grid.setAlignmentMode(GridLayout.ALIGN_MARGINS);
            grid.setColumnOrderPreserved(false);
            grid.setUseDefaultMargins(false);

            if (activeGroup != null) {
                for (int tabId : activeGroup.tabIds) {
                    TabItem tab = getTabById(tabId);
                    if (tab == null) continue;
                    grid.addView(createSingleTabCard(tab, cardWidth, filterQuery));
                }
            }
            tabGridContentLayout.addView(grid);
            return;
        }

        if (tabGridGroupBanner != null) tabGridGroupBanner.setVisibility(View.GONE);

        Set<Integer> groupedTabIds = new HashSet<>();
        for (TabGroup g : tabGroupsList) {
            groupedTabIds.addAll(g.tabIds);
        }

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(2);
        grid.setAlignmentMode(GridLayout.ALIGN_MARGINS);
        grid.setColumnOrderPreserved(false);
        grid.setUseDefaultMargins(false);

        for (TabItem tab : tabsList) {
            if (!groupedTabIds.contains(tab.id)) {
                grid.addView(createSingleTabCard(tab, cardWidth, filterQuery));
            }
        }

        for (TabGroup group : tabGroupsList) {
            if (group.tabIds.isEmpty()) continue;
            grid.addView(createEdgeTabGroupCard(group, cardWidth, filterQuery));
        }

        grid.setOnDragListener((v, event) -> {
            if (event.getAction() == DragEvent.ACTION_DROP) {
                if (event.getLocalState() instanceof TabItem) {
                    TabItem sourceTab = (TabItem) event.getLocalState();
                    float dropX = event.getX();
                    float dropY = event.getY();
                    int targetIdx = -1;
                    for (int i = 0; i < grid.getChildCount(); i++) {
                        View child = grid.getChildAt(i);
                        if (dropY >= child.getTop() && dropY <= child.getBottom() &&
                            dropX >= child.getLeft() && dropX <= child.getRight()) {
                            targetIdx = i;
                            break;
                        }
                    }
                    if (targetIdx != -1 && targetIdx < tabsList.size()) {
                        int fromIdx = tabsList.indexOf(sourceTab);
                        if (fromIdx != -1 && fromIdx != targetIdx) {
                            tabsList.remove(fromIdx);
                            tabsList.add(targetIdx, sourceTab);
                            saveOpenTabsState();
                            renderTabGridCards(tabGridSearchInput != null ? tabGridSearchInput.getText().toString() : "");
                            playAssetSound("sfx/pop_click.mp3");
                            return true;
                        }
                    }
                }
            }
            return true;
        });

        tabGridContentLayout.addView(grid);
    }

    private View createEdgeTabGroupCard(TabGroup group, int cardWidth, String filterQuery) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = cardWidth;
        lp.height = (int) (cardWidth * 1.35f);
        lp.setMargins(dpToPx(6), dpToPx(6), dpToPx(6), dpToPx(6));
        card.setLayoutParams(lp);

        GradientDrawable gd = new GradientDrawable();
        gd.setColor(0xFF181B25); // Stitch Obsidian surface-container-low
        gd.setCornerRadius(dpToPx(18));
        gd.setStroke(dpToPx(2), Color.parseColor(group.color != null ? group.color : "#00E5FF"));
        card.setBackground(gd);
        card.setPadding(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10));

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        View colorDot = new View(this);
        LinearLayout.LayoutParams dotLp = new LinearLayout.LayoutParams(dpToPx(10), dpToPx(10));
        colorDot.setLayoutParams(dotLp);
        GradientDrawable dotGd = new GradientDrawable();
        dotGd.setShape(GradientDrawable.OVAL);
        dotGd.setColor(Color.parseColor(group.color != null ? group.color : "#00E5FF"));
        colorDot.setBackground(dotGd);

        TextView titleView = new TextView(this);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        titleLp.setMarginStart(dpToPx(8));
        titleView.setLayoutParams(titleLp);
        titleView.setText(group.title + " (" + group.tabIds.size() + ")");
        titleView.setTextColor(0xFFDFE2F0);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
        titleView.setSingleLine(true);
        titleView.setEllipsize(android.text.TextUtils.TruncateAt.END);

        header.addView(colorDot);
        header.addView(titleView);
        card.addView(header);

        GridLayout miniGrid = new GridLayout(this);
        LinearLayout.LayoutParams gridLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
        gridLp.topMargin = dpToPx(8);
        miniGrid.setLayoutParams(gridLp);
        miniGrid.setColumnCount(2);
        miniGrid.setRowCount(2);
        miniGrid.setUseDefaultMargins(false);

        int miniW = (cardWidth - dpToPx(32)) / 2;
        int miniH = ((int) (cardWidth * 1.35f) - dpToPx(56)) / 2;

        int count = 0;
        for (int tabId : group.tabIds) {
            if (count >= 4) break;
            TabItem tab = getTabById(tabId);
            FrameLayout miniTile = new FrameLayout(this);
            GridLayout.LayoutParams tileLp = new GridLayout.LayoutParams();
            tileLp.width = miniW;
            tileLp.height = miniH;
            tileLp.setMargins(dpToPx(2), dpToPx(2), dpToPx(2), dpToPx(2));
            miniTile.setLayoutParams(tileLp);

            GradientDrawable tileGd = new GradientDrawable();
            tileGd.setColor(0xFF0A0E17);
            tileGd.setCornerRadius(dpToPx(8));
            miniTile.setBackground(tileGd);
            miniTile.setClipToOutline(true);

            if (tab != null && tab.snapshotBitmap != null && !tab.snapshotBitmap.isRecycled()) {
                ImageView miniImg = new ImageView(this);
                miniImg.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                miniImg.setScaleType(ImageView.ScaleType.CENTER_CROP);
                miniImg.setImageBitmap(tab.snapshotBitmap);
                miniTile.addView(miniImg);
            } else {
                TextView miniIcon = new TextView(this);
                miniIcon.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER));
                miniIcon.setGravity(Gravity.CENTER);
                miniIcon.setText(tab != null && tab.url != null && tab.url.contains("youtube.com") ? "🎬" : "🌐");
                miniIcon.setTextSize(14);
                miniTile.addView(miniIcon);
            }
            miniGrid.addView(miniTile);
            count++;
        }

        while (count < 4) {
            FrameLayout emptyTile = new FrameLayout(this);
            GridLayout.LayoutParams tileLp = new GridLayout.LayoutParams();
            tileLp.width = miniW;
            tileLp.height = miniH;
            tileLp.setMargins(dpToPx(2), dpToPx(2), dpToPx(2), dpToPx(2));
            emptyTile.setLayoutParams(tileLp);

            GradientDrawable emptyGd = new GradientDrawable();
            emptyGd.setColor(0xFF0F131D);
            emptyGd.setCornerRadius(dpToPx(8));
            emptyTile.setBackground(emptyGd);

            miniGrid.addView(emptyTile);
            count++;
        }

        card.addView(miniGrid);

        card.setOnClickListener(v -> {
            playUiFeedbackSound("tap");
            currentGridGroupId = group.id;
            renderTabGridCards(tabGridSearchInput.getText().toString());
        });

        card.setOnDragListener((v, event) -> {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    return event.getLocalState() instanceof TabItem;

                case DragEvent.ACTION_DRAG_ENTERED:
                    if (event.getLocalState() instanceof TabItem) {
                        TabItem sourceTab = (TabItem) event.getLocalState();
                        if (!group.tabIds.contains(sourceTab.id)) {
                            gd.setStroke(dpToPx(3), 0xFF00E5FF);
                            v.animate().scaleX(1.05f).scaleY(1.05f).setDuration(120).start();
                        }
                    }
                    return true;

                case DragEvent.ACTION_DRAG_EXITED:
                    gd.setStroke(dpToPx(2), Color.parseColor(group.color != null ? group.color : "#00E5FF"));
                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(120).start();
                    return true;

                case DragEvent.ACTION_DROP:
                    if (event.getLocalState() instanceof TabItem) {
                        TabItem sourceTab = (TabItem) event.getLocalState();
                        v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                        addTabToGroup(sourceTab.id, group.id);
                        return true;
                    }
                    return false;

                case DragEvent.ACTION_DRAG_ENDED:
                    v.setAlpha(1.0f);
                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                    gd.setStroke(dpToPx(2), Color.parseColor(group.color != null ? group.color : "#00E5FF"));
                    return true;
            }
            return false;
        });

        return card;
    }

    private View createSingleTabCard(TabItem tab, int cardWidth, String filterQuery) {
        String title = tab.title != null ? tab.title : "New Tab";
        String url = tab.url != null ? tab.url : "";

        boolean isActive = (tab.id == activeTabId);
        boolean isSelected = selectedGridTabIds.contains(tab.id);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = cardWidth;
        lp.height = (int) (cardWidth * 1.35f);
        lp.setMargins(dpToPx(6), dpToPx(6), dpToPx(6), dpToPx(6));
        card.setLayoutParams(lp);

        GradientDrawable gd = new GradientDrawable();
        gd.setColor(0xFF181B25); // Stitch Obsidian surface-container-low
        gd.setCornerRadius(dpToPx(18));
        if (isSelected) {
            gd.setStroke(dpToPx(3), 0xFFFFCC00);
        } else if (isActive) {
            gd.setStroke(dpToPx(2), 0xFF00E5FF); // Stitch Neon Cyan
        } else {
            gd.setStroke(dpToPx(1), 0x22FFFFFF); // 1px translucent micro-border
        }
        card.setBackground(gd);
        card.setPadding(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10));

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        // Icon Chip
        FrameLayout iconChip = new FrameLayout(this);
        iconChip.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(22), dpToPx(22)));
        GradientDrawable iconChipGd = new GradientDrawable();
        iconChipGd.setColor(0xFF1C1F29);
        iconChipGd.setCornerRadius(dpToPx(6));
        iconChip.setBackground(iconChipGd);

        TextView iconView = new TextView(this);
        iconView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        iconView.setGravity(Gravity.CENTER);
        iconView.setText(tab.isIncognito ? "🕶️" : (url.contains("youtube.com") ? "🎬" : (url.contains("chatgpt.com") ? "🤖" : (url.contains("gemini.google.com") ? "♊" : "🌐"))));
        iconView.setTextSize(11);
        iconChip.addView(iconView);

        TextView titleView = new TextView(this);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        titleLp.setMarginStart(dpToPx(8));
        titleView.setLayoutParams(titleLp);
        titleView.setText(tab.nickname != null && !tab.nickname.isEmpty() ? tab.nickname : title);
        titleView.setTextColor(0xFFDFE2F0);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
        titleView.setSingleLine(true);
        titleView.setEllipsize(android.text.TextUtils.TruncateAt.END);

        FrameLayout closeCircle = new FrameLayout(this);
        closeCircle.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(22), dpToPx(22)));
        GradientDrawable closeGd = new GradientDrawable();
        closeGd.setColor(0xFF262A34);
        closeGd.setShape(GradientDrawable.OVAL);
        closeCircle.setBackground(closeGd);

        ImageButton closeBtn = new ImageButton(this);
        closeBtn.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        closeBtn.setBackgroundResource(android.R.color.transparent);
        closeBtn.setImageResource(R.drawable.ic_pod_close);
        closeBtn.setColorFilter(0xFFBAC9CC);
        closeBtn.setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
        closeBtn.setOnClickListener(v -> {
            playUiFeedbackSound("tap");
            closeTab(tab.id);
            selectedGridTabIds.remove(tab.id);
            renderTabGridCards(tabGridSearchInput.getText().toString());
        });
        closeCircle.addView(closeBtn);

        header.addView(iconChip);
        header.addView(titleView);
        header.addView(closeCircle);
        card.addView(header);

        FrameLayout body = new FrameLayout(this);
        LinearLayout.LayoutParams bodyLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
        bodyLp.topMargin = dpToPx(8);
        body.setLayoutParams(bodyLp);

        GradientDrawable bodyGd = new GradientDrawable();
        bodyGd.setColor(0xFF0A0E17);
        bodyGd.setCornerRadius(dpToPx(12));
        body.setBackground(bodyGd);
        body.setClipToOutline(true);

        if (tab.snapshotBitmap != null && !tab.snapshotBitmap.isRecycled()) {
            ImageView previewImage = new ImageView(this);
            previewImage.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            previewImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            previewImage.setImageBitmap(tab.snapshotBitmap);
            body.addView(previewImage);
        } else {
            TextView urlSnippet = new TextView(this);
            urlSnippet.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
            urlSnippet.setText(cleanDisplayUrl(url));
            urlSnippet.setTextColor(0xFF849396);
            urlSnippet.setTextSize(10);
            urlSnippet.setGravity(Gravity.CENTER);
            urlSnippet.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
            body.addView(urlSnippet);
        }

        // Domain pill badge pinned at bottom-left
        String displayDomain = cleanDisplayUrl(url);
        if (!displayDomain.isEmpty()) {
            TextView domainBadge = new TextView(this);
            FrameLayout.LayoutParams badgeLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            badgeLp.gravity = Gravity.BOTTOM | Gravity.START;
            badgeLp.setMargins(dpToPx(6), 0, 0, dpToPx(6));
            domainBadge.setLayoutParams(badgeLp);
            domainBadge.setText(displayDomain);
            domainBadge.setTextColor(0xFF00E5FF);
            domainBadge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9);
            domainBadge.setTypeface(null, android.graphics.Typeface.BOLD);
            domainBadge.setPadding(dpToPx(6), dpToPx(2), dpToPx(6), dpToPx(2));
            GradientDrawable badgeGd = new GradientDrawable();
            badgeGd.setColor(0xCC0F131D);
            badgeGd.setCornerRadius(dpToPx(6));
            domainBadge.setBackground(badgeGd);
            body.addView(domainBadge);
        }

        card.addView(body);

        card.setOnClickListener(v -> {
            playUiFeedbackSound("tap");
            if (!selectedGridTabIds.isEmpty()) {
                toggleTabSelectionForSplit(tab.id);
            } else {
                switchToTab(tab.id);
                hideTabGridView();
            }
        });

        card.setOnLongClickListener(v -> {
            if (!selectedGridTabIds.isEmpty()) {
                toggleTabSelectionForSplit(tab.id);
                return true;
            }
            v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            playAssetSound("sfx/pop_button_v2.mp3");
            ClipData clipData = ClipData.newPlainText("tab_id", String.valueOf(tab.id));
            View.DragShadowBuilder shadow = new View.DragShadowBuilder(v);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                v.startDragAndDrop(clipData, shadow, tab, 0);
            } else {
                v.startDrag(clipData, shadow, tab, 0);
            }
            v.setAlpha(0.35f);
            return true;
        });

        card.setOnDragListener((v, event) -> {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    return event.getLocalState() instanceof TabItem;

                case DragEvent.ACTION_DRAG_ENTERED:
                    if (event.getLocalState() instanceof TabItem) {
                        TabItem sourceTab = (TabItem) event.getLocalState();
                        if (sourceTab.id != tab.id) {
                            gd.setStroke(dpToPx(3), 0xFF00E5FF);
                            v.animate().scaleX(1.06f).scaleY(1.06f).setDuration(120).start();
                        }
                    }
                    return true;

                case DragEvent.ACTION_DRAG_EXITED:
                    if (event.getLocalState() instanceof TabItem) {
                        TabItem sourceTab = (TabItem) event.getLocalState();
                        if (sourceTab.id != tab.id) {
                            if (selectedGridTabIds.contains(tab.id)) {
                                gd.setStroke(dpToPx(3), 0xFFFFCC00);
                            } else if (tab.id == activeTabId) {
                                gd.setStroke(dpToPx(2), 0xFF00E5FF);
                            } else {
                                gd.setStroke(dpToPx(1), 0x22FFFFFF);
                            }
                            v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(120).start();
                        }
                    }
                    return true;

                case DragEvent.ACTION_DROP:
                    if (event.getLocalState() instanceof TabItem) {
                        TabItem sourceTab = (TabItem) event.getLocalState();
                        if (sourceTab.id != tab.id) {
                            v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                            int fromIdx = tabsList.indexOf(sourceTab);
                            int toIdx = tabsList.indexOf(tab);
                            if (fromIdx != -1 && toIdx != -1) {
                                tabsList.remove(fromIdx);
                                tabsList.add(toIdx, sourceTab);
                                saveOpenTabsState();
                                renderTabGridCards(tabGridSearchInput != null ? tabGridSearchInput.getText().toString() : "");
                                playAssetSound("sfx/pop_click.mp3");
                                return true;
                            }
                        }
                    }
                    return false;

                case DragEvent.ACTION_DRAG_ENDED:
                    v.setAlpha(1.0f);
                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                    if (selectedGridTabIds.contains(tab.id)) {
                        gd.setStroke(dpToPx(3), 0xFFFFCC00);
                    } else if (tab.id == activeTabId) {
                        gd.setStroke(dpToPx(2), 0xFF00E5FF);
                    } else {
                        gd.setStroke(dpToPx(1), 0x22FFFFFF);
                    }
                    return true;
            }
            return false;
        });

        return card;
    }

    private void mergeTabsIntoGroup(int sourceTabId, int targetTabId) {
        TabItem sourceTab = getTabById(sourceTabId);
        TabItem targetTab = getTabById(targetTabId);
        if (sourceTab == null || targetTab == null) return;

        TabGroup existingGroup = null;
        for (TabGroup g : tabGroupsList) {
            if (g.tabIds.contains(targetTabId)) {
                existingGroup = g;
                break;
            }
        }

        if (existingGroup != null) {
            for (TabGroup g : tabGroupsList) {
                g.tabIds.remove((Integer) sourceTabId);
            }
            if (!existingGroup.tabIds.contains(sourceTabId)) {
                existingGroup.tabIds.add(sourceTabId);
            }
        } else {
            for (TabGroup g : tabGroupsList) {
                g.tabIds.remove((Integer) sourceTabId);
                g.tabIds.remove((Integer) targetTabId);
            }
            String groupTitle = "Group (" + (targetTab.title != null ? targetTab.title : "Tabs") + ")";
            if (groupTitle.length() > 22) groupTitle = groupTitle.substring(0, 20) + "…";
            TabGroup newGroup = new TabGroup("group_" + System.currentTimeMillis(), groupTitle, "#00E5FF", "📁");
            newGroup.tabIds.add(targetTabId);
            newGroup.tabIds.add(sourceTabId);
            tabGroupsList.add(newGroup);
        }

        tabGroupsList.removeIf(g -> g.tabIds.isEmpty());
        saveTabGroups();
        saveOpenTabsState();
        renderTabGridCards(tabGridSearchInput != null ? tabGridSearchInput.getText().toString() : "");
        playAssetSound("sfx/pop_click.mp3");
        Toast.makeText(this, "📁 Grouped tabs together", Toast.LENGTH_SHORT).show();
    }

    private void addTabToGroup(int sourceTabId, String groupId) {
        TabItem sourceTab = getTabById(sourceTabId);
        if (sourceTab == null) return;

        TabGroup targetGroup = null;
        for (TabGroup g : tabGroupsList) {
            if (g.id.equals(groupId)) {
                targetGroup = g;
                break;
            }
        }
        if (targetGroup == null) return;

        for (TabGroup g : tabGroupsList) {
            g.tabIds.remove((Integer) sourceTabId);
        }
        if (!targetGroup.tabIds.contains(sourceTabId)) {
            targetGroup.tabIds.add(sourceTabId);
        }

        tabGroupsList.removeIf(g -> g.tabIds.isEmpty());
        saveTabGroups();
        saveOpenTabsState();
        renderTabGridCards(tabGridSearchInput != null ? tabGridSearchInput.getText().toString() : "");
        playAssetSound("sfx/pop_click.mp3");
        Toast.makeText(this, "📁 Added '" + sourceTab.title + "' to " + targetGroup.title, Toast.LENGTH_SHORT).show();
    }

    private void toggleTabSelectionForSplit(int tabId) {
        playUiFeedbackSound("tap");
        if (selectedGridTabIds.contains(tabId)) {
            selectedGridTabIds.remove(tabId);
        } else {
            selectedGridTabIds.add(tabId);
        }

        if (!selectedGridTabIds.isEmpty()) {
            tabGridSplitActionBar.setVisibility(View.VISIBLE);
            btnTabGridSplitSelected.setEnabled(selectedGridTabIds.size() == 2);
            btnTabGridSplitSelected.setAlpha(selectedGridTabIds.size() == 2 ? 1.0f : 0.4f);
            btnTabGridSplitSelected.setText("🔀 Split (" + selectedGridTabIds.size() + ")");
            btnTabGridGroupSelected.setText("📁 Group (" + selectedGridTabIds.size() + ")");
            btnTabGridCloseSelected.setText("🗑️ Close (" + selectedGridTabIds.size() + ")");
        } else {
            tabGridSplitActionBar.setVisibility(View.GONE);
        }

        renderTabGridCards(tabGridSearchInput.getText().toString());
    }

    public void updateYouTubeLiveState(boolean isPlaying, boolean isMuted) {
        if (ytRemotePlayPause != null) {
            ytRemotePlayPause.setImageResource(isPlaying ? R.drawable.ic_pod_pause : R.drawable.ic_pod_play);
        }
        if (ytRemoteMute != null) {
            ytRemoteMute.setImageResource(isMuted ? R.drawable.ic_pod_mute : R.drawable.ic_pod_unmute);
        }
    }

    public void applyPruningSettings(int limit, String mode, boolean enabled) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit()
                .putString("chat_message_limit", String.valueOf(limit))
                .putString("chat_pruning_mode", mode)
                .putString("chat_limit_enabled", String.valueOf(enabled))
                .apply();

        for (TabItem tab : tabsList) {
            if (tab.webView != null) {
                tab.webView.evaluateJavascript(
                        String.format("if (window.__CASPIAN_PRUNER_UPDATE) window.__CASPIAN_PRUNER_UPDATE(%d, '%s', %b);", limit, mode, enabled), null
                );
            }
        }

        if (controlWebView != null) {
            String sheetSyncJs = String.format("if (typeof window.syncPrunerSettingsFromNative === 'function') window.syncPrunerSettingsFromNative(%d, '%s', %b);", limit, mode, enabled);
            controlWebView.evaluateJavascript(sheetSyncJs, null);
        }

        updateChatgptDockButtons();
    }

    private void loadPodPreferences() {
        try {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            podShape = prefs.getString("pod_shape", "circle");
            podScale = prefs.getFloat("pod_scale", 1.0f);
            podStartColor = prefs.getString("pod_start_color", "#00C4FF");
            podEndColor = prefs.getString("pod_end_color", "#0077B6");
            podOpacity = 1.0f;
            isMasterSfxMuted = prefs.getBoolean("master_sfx_muted", false);
            isGoogleDockAutoCollapse = prefs.getBoolean("google_dock_autocollapse", true);
            applyPodCustomization();
        } catch (Exception e) {
            Log.e(TAG, "loadPodPreferences error: " + e.getMessage());
        }
    }

    private void savePodPreferences() {
        try {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            prefs.edit()
                    .putString("pod_shape", podShape)
                    .putFloat("pod_scale", podScale)
                    .putString("pod_start_color", podStartColor)
                    .putString("pod_end_color", podEndColor)
                    .putFloat("pod_opacity", 1.0f)
                    .apply();
        } catch (Exception ignored) {}
    }

    public void setPodShape(String shape) {
        this.podShape = shape;
        applyPodCustomization();
        savePodPreferences();
    }

    public void setPodScale(float scale) {
        this.podScale = scale;
        applyPodCustomization();
        savePodPreferences();
    }

    public void setPodColor(String hexColor) {
        this.podStartColor = hexColor;
        this.podEndColor = hexColor;
        applyPodCustomization();
        savePodPreferences();
    }

    public void setPodOpacity(float opacity) {
        this.podOpacity = 1.0f;
        applyPodCustomization();
        savePodPreferences();
    }

    public void applyWidgetScale(String type, float scale) {
        if ("action_button".equalsIgnoreCase(type) || "pod".equalsIgnoreCase(type)) {
            setPodScale(scale);
        }
    }

    public void applyFloatingTheme(String startHex, String endHex, String shape) {
        if (shape != null) this.podShape = shape;
        if (startHex != null) this.podStartColor = startHex;
        if (endHex != null) this.podEndColor = endHex;
        applyPodCustomization();
        savePodPreferences();
    }

    private void applyPodCustomization() {
        if (floatingCaspianCard == null) return;

        try {
            int baseDp = 52;
            int targetDp = Math.round(baseDp * podScale);
            int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, targetDp, getResources().getDisplayMetrics());

            ViewGroup.LayoutParams lp = floatingCaspianCard.getLayoutParams();
            if (lp != null) {
                lp.width = px;
                lp.height = px;
                floatingCaspianCard.setLayoutParams(lp);
            }

            float radiusDp = 26f;
            if ("squircle".equalsIgnoreCase(podShape) || "rounded".equalsIgnoreCase(podShape)) {
                radiusDp = 14f * podScale;
            } else if ("square".equalsIgnoreCase(podShape)) {
                radiusDp = 6f * podScale;
            } else {
                radiusDp = (targetDp / 2f);
            }
            float radiusPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, radiusDp, getResources().getDisplayMetrics());
            floatingCaspianCard.setRadius(radiusPx);

            int startC = Color.parseColor(podStartColor);
            int endC = Color.parseColor(podEndColor);
            GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{startC, endC});
            gd.setCornerRadius(radiusPx);
            if (floatingCaspianIcon != null) {
                floatingCaspianIcon.setBackground(gd);
            }
            floatingCaspianCard.setCardBackgroundColor(Color.TRANSPARENT);
            floatingCaspianCard.setAlpha(1.0f);

            if (omniboxShieldIcon != null) omniboxShieldIcon.setColorFilter(startC);
            if (omniboxVoiceBtn != null) omniboxVoiceBtn.setColorFilter(startC);
            if (omniboxTabsCount != null) omniboxTabsCount.setTextColor(startC);

            if (speechWaveformView != null) {
                speechWaveformView.setWaveColors(startC, endC);
            }
        } catch (Exception e) {
            floatingCaspianCard.setCardBackgroundColor(0xFF00C4FF);
        }
    }

    public String getPodSettingsJson() {
        try {
            JSONObject json = new JSONObject();
            json.put("shape", podShape);
            json.put("scale", podScale);
            json.put("startColor", podStartColor);
            json.put("endColor", podEndColor);
            json.put("opacity", 1.0f);
            return json.toString();
        } catch (Exception e) {
            return "{}";
        }
    }

    public void updateThemeStyling() {
        if (rootContainer != null) {
            rootContainer.setBackgroundColor(isDarkTheme ? 0xFF050811 : 0xFFF8FAFC);
        }
        if (omniboxHeaderWrapper != null) {
            TabItem currentTab = getActiveOrDominantTab();
            if (currentTab != null && currentTab.isIncognito) {
                omniboxHeaderWrapper.setBackgroundColor(0xFF1E102E);
            } else {
                omniboxHeaderWrapper.setBackgroundColor(isDarkTheme ? 0xFF0D1524 : 0xFFFFFFFF);
            }
        }
        if (omniboxHeader != null) {
            omniboxHeader.setBackgroundResource(isDarkTheme ? R.drawable.bg_liquid_glass : R.drawable.bg_liquid_glass_light);
        }
        if (omniboxCapsule != null) {
            omniboxCapsule.setBackgroundResource(isDarkTheme ? R.drawable.bg_liquid_glass_pill : R.drawable.bg_liquid_glass_pill_light);
        }
        if (omniboxEditText != null) {
            omniboxEditText.setTextColor(isDarkTheme ? 0xFFFFFFFF : 0xFF111827);
            omniboxEditText.setHintTextColor(isDarkTheme ? 0x88A2A9A9 : 0xFF9CA3AF);
        }
        if (omniboxFinderInput != null) {
            omniboxFinderInput.setTextColor(isDarkTheme ? 0xFFFFFFFF : 0xFF111827);
            omniboxFinderInput.setHintTextColor(isDarkTheme ? 0x88A2A9A9 : 0xFF9CA3AF);
        }

        int defaultIconTint = isDarkTheme ? 0xFFA2A9A9 : 0xFF4B5563;
        if (omniboxBackBtn != null) omniboxBackBtn.setColorFilter(defaultIconTint);
        if (omniboxForwardBtn != null) omniboxForwardBtn.setColorFilter(defaultIconTint);
        if (omniboxReloadBtn != null) omniboxReloadBtn.setColorFilter(defaultIconTint);
        if (omniboxMenuBtn != null) omniboxMenuBtn.setColorFilter(defaultIconTint);
        if (omniboxFinderBtn != null) omniboxFinderBtn.setColorFilter(defaultIconTint);
        if (omniboxClearBtn != null) omniboxClearBtn.setColorFilter(defaultIconTint);
        if (omniboxFinderClose != null) omniboxFinderClose.setColorFilter(defaultIconTint);
        if (omniboxFinderPrev != null) omniboxFinderPrev.setColorFilter(defaultIconTint);
        if (omniboxFinderNext != null) omniboxFinderNext.setColorFilter(defaultIconTint);
    }

    public void applyWebViewTheme(WebView webView, boolean isDark) {
        if (webView == null) return;
        try {
            if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                WebSettingsCompat.setForceDark(webView.getSettings(), isDark ? WebSettingsCompat.FORCE_DARK_ON : WebSettingsCompat.FORCE_DARK_OFF);
            }
        } catch (Throwable ignored) {}
        try {
            if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK_STRATEGY)) {
                WebSettingsCompat.setForceDarkStrategy(webView.getSettings(), WebSettingsCompat.DARK_STRATEGY_PREFER_WEB_THEME_OVER_USER_AGENT_DARKENING);
            }
        } catch (Throwable ignored) {}

        try {
            String themeJs = "(function() {\n" +
                    "  var isDark = " + isDark + ";\n" +
                    "  try {\n" +
                    "    var host = (window.location && window.location.host) ? window.location.host : '';\n" +
                    "    // Universal HTML5 Color Scheme\n" +
                    "    if (document.documentElement) {\n" +
                    "      document.documentElement.style.colorScheme = isDark ? 'dark' : 'light';\n" +
                    "      document.documentElement.setAttribute('data-theme', isDark ? 'dark' : 'light');\n" +
                    "      document.documentElement.classList.toggle('dark', isDark);\n" +
                    "      document.documentElement.classList.toggle('light', !isDark);\n" +
                    "    }\n" +
                    "    if (document.body) {\n" +
                    "      document.body.style.colorScheme = isDark ? 'dark' : 'light';\n" +
                    "      document.body.classList.toggle('dark', isDark);\n" +
                    "      document.body.classList.toggle('light', !isDark);\n" +
                    "    }\n" +
                    "    try { localStorage.setItem('theme', isDark ? 'dark' : 'light'); } catch(e){}\n" +
                    "    try { localStorage.setItem('colorMode', isDark ? 'dark' : 'light'); } catch(e){}\n" +
                    "\n" +
                    "    // 1. Google Gemini Theme Adaptation\n" +
                    "    if (host.includes('gemini.google.com') || host.includes('google.com')) {\n" +
                    "      if (document.documentElement) {\n" +
                    "        document.documentElement.classList.toggle('dark-theme', isDark);\n" +
                    "        document.documentElement.classList.toggle('light-theme', !isDark);\n" +
                    "        document.documentElement.setAttribute('dark-theme', isDark ? 'true' : 'false');\n" +
                    "      }\n" +
                    "      if (document.body) {\n" +
                    "        document.body.classList.toggle('dark-theme', isDark);\n" +
                    "        document.body.classList.toggle('light-theme', !isDark);\n" +
                    "        document.body.setAttribute('dark-theme', isDark ? 'true' : 'false');\n" +
                    "      }\n" +
                    "      try { localStorage.setItem('user_preferred_theme', isDark ? 'dark' : 'light'); } catch(e){}\n" +
                    "      if (document.head) {\n" +
                    "        var existingGeminiStyle = document.getElementById('caspian-gemini-theme-style');\n" +
                    "        if (!existingGeminiStyle) {\n" +
                    "          existingGeminiStyle = document.createElement('style');\n" +
                    "          existingGeminiStyle.id = 'caspian-gemini-theme-style';\n" +
                    "          document.head.appendChild(existingGeminiStyle);\n" +
                    "        }\n" +
                    "        if (isDark) {\n" +
                    "          existingGeminiStyle.textContent = 'body, html, .main-container, mat-sidenav-container, mat-sidenav-content, .conversation-container, .chat-history { background-color: #131314 !important; color: #e3e3e3 !important; } .header, .side-nav, input-area { background: #1e1f20 !important; }';\n" +
                    "        } else {\n" +
                    "          existingGeminiStyle.textContent = 'body, html, .main-container, mat-sidenav-container, mat-sidenav-content, .conversation-container, .chat-history { background-color: #ffffff !important; color: #1f1f1f !important; } .header, .side-nav, input-area { background: #f0f4f9 !important; }';\n" +
                    "        }\n" +
                    "      }\n" +
                    "    }\n" +
                    "\n" +
                    "    // 2. YouTube Theme Adaptation\n" +
                    "    if (host.includes('youtube.com')) {\n" +
                    "      if (document.documentElement) {\n" +
                    "        if (isDark) {\n" +
                    "          document.documentElement.setAttribute('dark', 'true');\n" +
                    "          document.documentElement.setAttribute('theme', 'dark');\n" +
                    "        } else {\n" +
                    "          document.documentElement.removeAttribute('dark');\n" +
                    "          document.documentElement.setAttribute('theme', 'light');\n" +
                    "        }\n" +
                    "      }\n" +
                    "      if (document.body) {\n" +
                    "        if (isDark) document.body.setAttribute('dark', 'true');\n" +
                    "        else document.body.removeAttribute('dark');\n" +
                    "      }\n" +
                    "      try { localStorage.setItem('yt-theme', isDark ? 'dark' : 'light'); } catch(e){}\n" +
                    "      try { document.cookie = 'PREF=f6=' + (isDark ? '400' : '0') + '; domain=.youtube.com; path=/'; } catch(e){}\n" +
                    "      if (document.head) {\n" +
                    "        var existingYtStyle = document.getElementById('caspian-yt-theme-style');\n" +
                    "        if (!existingYtStyle) {\n" +
                    "          existingYtStyle = document.createElement('style');\n" +
                    "          existingYtStyle.id = 'caspian-yt-theme-style';\n" +
                    "          document.head.appendChild(existingYtStyle);\n" +
                    "        }\n" +
                    "        if (isDark) {\n" +
                    "          existingYtStyle.textContent = 'html, body, ytm-app, ytd-app, #content, .watch-page, #page-manager { background-color: #0f0f0f !important; color: #f1f1f1 !important; } .mobile-topbar-header, ytm-header-bar { background-color: #0f0f0f !important; }';\n" +
                    "        } else {\n" +
                    "          existingYtStyle.textContent = 'html, body, ytm-app, ytd-app, #content, .watch-page, #page-manager { background-color: #ffffff !important; color: #0f0f0f !important; } .mobile-topbar-header, ytm-header-bar { background-color: #ffffff !important; }';\n" +
                    "        }\n" +
                    "      }\n" +
                    "    }\n" +
                    "\n" +
                    "    // 3. ChatGPT & AI Platforms Theme Adaptation\n" +
                    "    if (host.includes('chatgpt.com') || host.includes('openai.com') || host.includes('claude.ai') || host.includes('deepseek.com')) {\n" +
                    "      if (document.documentElement) {\n" +
                    "        document.documentElement.classList.toggle('dark', isDark);\n" +
                    "        document.documentElement.classList.toggle('light', !isDark);\n" +
                    "      }\n" +
                    "      if (document.body) {\n" +
                    "        document.body.classList.toggle('dark', isDark);\n" +
                    "        document.body.classList.toggle('light', !isDark);\n" +
                    "      }\n" +
                    "    }\n" +
                    "  } catch(e) {}\n" +
                    "})();";
            webView.evaluateJavascript(themeJs, null);
        } catch (Exception ignored) {}
    }

    public void setThemeMode(String theme) {
        this.isDarkTheme = !"light".equalsIgnoreCase(theme);
        try {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            prefs.edit().putString("theme", isDarkTheme ? "dark" : "light").apply();
        } catch (Exception ignored) {}
        toggleHostTheme(isDarkTheme);
    }

    public void toggleHostTheme(boolean isDark) {
        this.isDarkTheme = isDark;
        try {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            prefs.edit().putString("theme", isDark ? "dark" : "light").apply();
        } catch (Exception ignored) {}

        updateThemeStyling();

        for (TabItem tab : tabsList) {
            if (tab.webView != null) {
                applyWebViewTheme(tab.webView, isDark);
            }
        }
        updateOmniboxState();
    }

    public void togglePlayYouTube() {
        TabItem currentTab = getTabById(activeTabId);
        if (currentTab != null && currentTab.webView != null) {
            currentTab.webView.evaluateJavascript(
                    "if (window.__CaspianYouTube) window.__CaspianYouTube.togglePlay(); else { var v = document.querySelector('video'); if (v) { if (v.paused) v.play(); else v.pause(); } }", null
            );
        }
    }

    public void toggleFullscreenYouTube() {
        TabItem currentTab = getTabById(activeTabId);
        if (currentTab != null && currentTab.webView != null) {
            currentTab.webView.evaluateJavascript(
                    "if (window.__CaspianYouTube) window.__CaspianYouTube.toggleFullscreen(); else { var v = document.querySelector('video'); if (v) { if (v.paused) v.play().catch(()=>{}); if (v.webkitEnterFullscreen) v.webkitEnterFullscreen(); else if (v.requestFullscreen) v.requestFullscreen(); } }", null
            );
        }
    }

    public void toggleMuteYouTube() {
        TabItem currentTab = getTabById(activeTabId);
        if (currentTab != null && currentTab.webView != null) {
            currentTab.webView.evaluateJavascript(
                    "if (window.__CaspianYouTube) window.__CaspianYouTube.toggleMute(); else { var v = document.querySelector('video'); if (v) { v.muted = !v.muted; } }", null
            );
        }
    }

    public void seekYouTube(double seconds) {
        TabItem currentTab = getTabById(activeTabId);
        if (currentTab != null && currentTab.webView != null) {
            currentTab.webView.evaluateJavascript(
                    "if (window.__CaspianYouTube) window.__CaspianYouTube.seekBy(" + seconds + "); else { var v = document.querySelector('video'); if (v) { v.currentTime += " + seconds + "; } }", null
            );
        }
    }

    public void setYouTubeSpeed(double speed) {
        TabItem currentTab = getTabById(activeTabId);
        if (currentTab != null && currentTab.webView != null) {
            currentTab.webView.evaluateJavascript(
                    "if (window.__CaspianYouTube) window.__CaspianYouTube.setSpeed(" + speed + "); else { var v = document.querySelector('video'); if (v) { v.playbackRate = " + speed + "; } }", null
            );
        }
        ytCurrentSpeed = (float) speed;
        if (ytRemoteSpeedBtn != null) ytRemoteSpeedBtn.setText(speed + "x");
    }

    public void setYouTubeQuality(String quality) {
        TabItem currentTab = getTabById(activeTabId);
        if (currentTab != null && currentTab.webView != null) {
            currentTab.webView.evaluateJavascript("if (window.__CaspianYouTube) window.__CaspianYouTube.setQuality('" + quality + "');", null);
        }
        if (ytRemoteQualityBtn != null) {
            String label = "HD";
            if ("auto".equalsIgnoreCase(quality)) label = "Auto";
            else if ("hd1080".equalsIgnoreCase(quality)) label = "1080p";
            else if ("hd720".equalsIgnoreCase(quality)) label = "720p";
            else if ("large".equalsIgnoreCase(quality)) label = "480p";
            else if ("medium".equalsIgnoreCase(quality)) label = "360p";
            else if ("small".equalsIgnoreCase(quality)) label = "240p";
            else if ("tiny".equalsIgnoreCase(quality)) label = "144p";
            ytRemoteQualityBtn.setText(label);
        }
    }

    public void toggleFloatingYouTubeRemote(boolean show) {
        isYtRemoteExplicitlyHidden = !show;
        try {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            prefs.edit().putBoolean("yt_dock_enabled", show).apply();
        } catch (Exception ignored) {}
        if (ytFloatingRemoteContainer != null) {
            if (show) {
                ytFloatingRemoteScroll.setVisibility(View.GONE);
                ytFloatingRemoteBall.setVisibility(View.VISIBLE);
            }
            ytFloatingRemoteContainer.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        updateOmniboxState();
    }

    public void toggleGoogleSearchDock(boolean show) {
        isSearchNavExplicitlyHidden = !show;
        try {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            prefs.edit().putBoolean("google_dock_enabled", show).apply();
        } catch (Exception ignored) {}
        if (searchNavContainer != null) {
            if (show) {
                searchDockScroll.setVisibility(View.GONE);
                searchNavBall.setVisibility(View.VISIBLE);
            }
            searchNavContainer.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        updateOmniboxState();
    }

    public void toggleChatGPTDock(boolean show) {
        isChatgptDockExplicitlyHidden = !show;
        try {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            prefs.edit().putBoolean("chatgpt_dock_enabled", show).apply();
        } catch (Exception ignored) {}
        if (chatgptDockContainer != null) {
            if (show) {
                chatgptDockScroll.setVisibility(View.GONE);
                chatgptDockBall.setVisibility(View.VISIBLE);
            }
            chatgptDockContainer.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        updateOmniboxState();
    }

    public void toggleGeminiDock(boolean show) {
        isGeminiDockExplicitlyHidden = !show;
        try {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            prefs.edit().putBoolean("gemini_dock_enabled", show).apply();
        } catch (Exception ignored) {}
        if (geminiDockContainer != null) {
            if (show) {
                geminiDockScroll.setVisibility(View.GONE);
                geminiDockBall.setVisibility(View.VISIBLE);
            }
            geminiDockContainer.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        updateOmniboxState();
    }

    public void setGoogleDockAutoCollapse(boolean enabled) {
        this.isGoogleDockAutoCollapse = enabled;
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putBoolean("google_dock_autocollapse", enabled).apply();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupLiquidGlassChatGPTDock() {
        if (chatgptDockContainer == null) return;

        try {
            chatgptDockClose.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                toggleChatGPTDock(false);
            });

            chatgptDockReload.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                reloadActiveTab();
            });

            chatgptDockToggleBtn.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                boolean curEnabled = "true".equalsIgnoreCase(prefs.getString("chat_limit_enabled", "false"));
                boolean nextEnabled = !curEnabled;
                int limit = 5;
                try { limit = Integer.parseInt(prefs.getString("chat_message_limit", "5")); } catch(Exception ignored){}
                String mode = prefs.getString("chat_pruning_mode", "sliding_window");

                new CaspianBridge(this).applyPruningSettings(limit, mode, nextEnabled);
                Toast.makeText(this, "✂️ Pruning " + (nextEnabled ? "ENABLED" : "DISABLED"), Toast.LENGTH_SHORT).show();
            });

            chatgptDockModeBtn.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                String curMode = prefs.getString("chat_pruning_mode", "sliding_window");
                String nextMode = "sliding_window".equalsIgnoreCase(curMode) ? "tail" : "sliding_window";
                int limit = 5;
                try { limit = Integer.parseInt(prefs.getString("chat_message_limit", "5")); } catch(Exception ignored){}
                boolean enabled = "true".equalsIgnoreCase(prefs.getString("chat_limit_enabled", "false"));

                new CaspianBridge(this).applyPruningSettings(limit, nextMode, enabled);
                Toast.makeText(this, "Mode: " + ("sliding_window".equals(nextMode) ? "Sliding Window" : "Tail Window"), Toast.LENGTH_SHORT).show();
            });

            chatgptDockLimitBtn.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                PopupMenu popup = new PopupMenu(this, v);

                int[] limits = {2, 4, 6, 8, 10, 14, 18, 20, 28, 48, 9999};
                String[] labels = {"2 Messages", "4 Messages", "6 Messages", "8 Messages", "10 Messages", "14 Messages", "18 Messages", "20 Messages", "28 Messages", "48 Messages", "∞ Unlimited (Show All)"};

                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                int curLimit = 5;
                try { curLimit = Integer.parseInt(prefs.getString("chat_message_limit", "5")); } catch(Exception ignored){}

                for (int i = 0; i < limits.length; i++) {
                    String label = (curLimit == limits[i] ? "✓ " : "   ") + labels[i];
                    popup.getMenu().add(0, limits[i], i, label);
                }

                popup.setOnMenuItemClickListener(item -> {
                    playUiFeedbackSound("tap");
                    int selectedLimit = item.getItemId();
                    String mode = prefs.getString("chat_pruning_mode", "sliding_window");
                    boolean enabled = "true".equalsIgnoreCase(prefs.getString("chat_limit_enabled", "false"));

                    new CaspianBridge(this).applyPruningSettings(selectedLimit, mode, enabled);
                    updateChatgptDockButtons();
                    Toast.makeText(this, "Message Limit: " + (selectedLimit >= 9999 ? "Unlimited" : (selectedLimit + " msgs")), Toast.LENGTH_SHORT).show();
                    return true;
                });
                popup.show();
            });

            chatgptMsgUpBtn.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                TabItem currentTab = getTabById(activeTabId);
                if (currentTab != null && currentTab.webView != null) {
                    SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                    boolean enabled = "true".equalsIgnoreCase(prefs.getString("chat_limit_enabled", "false"));
                    int step = enabled ? -1 : -2;
                    currentTab.webView.evaluateJavascript("if (window.__CASPIAN_PRUNER_STEP) window.__CASPIAN_PRUNER_STEP(" + step + "); else window.scrollBy({top: -400, behavior: 'smooth'});", null);
                }
            });

            chatgptMsgDownBtn.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                TabItem currentTab = getTabById(activeTabId);
                if (currentTab != null && currentTab.webView != null) {
                    SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                    boolean enabled = "true".equalsIgnoreCase(prefs.getString("chat_limit_enabled", "false"));
                    int step = enabled ? 1 : 2;
                    currentTab.webView.evaluateJavascript("if (window.__CASPIAN_PRUNER_STEP) window.__CASPIAN_PRUNER_STEP(" + step + "); else window.scrollBy({top: 400, behavior: 'smooth'});", null);
                }
            });

            chatgptFinderBtn.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                boolean isFinderOpen = chatgptFinderBox.getVisibility() == View.VISIBLE;
                if (isFinderOpen) {
                    chatgptFinderBox.setVisibility(View.GONE);
                    chatgptFinderPrev.setVisibility(View.GONE);
                    chatgptFinderNext.setVisibility(View.GONE);
                    TabItem currentTab = getTabById(activeTabId);
                    if (currentTab != null && currentTab.webView != null) currentTab.webView.clearMatches();
                    hideKeyboard();
                } else {
                    chatgptFinderBox.setVisibility(View.VISIBLE);
                    chatgptFinderPrev.setVisibility(View.VISIBLE);
                    chatgptFinderNext.setVisibility(View.VISIBLE);
                    chatgptFinderInput.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) imm.showSoftInput(chatgptFinderInput, InputMethodManager.SHOW_IMPLICIT);
                }
            });

            chatgptFinderInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    TabItem currentTab = getTabById(activeTabId);
                    if (currentTab != null && currentTab.webView != null) {
                        currentTab.webView.findAllAsync(s.toString());
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            chatgptFinderPrev.setOnClickListener(v -> {
                TabItem currentTab = getTabById(activeTabId);
                if (currentTab != null && currentTab.webView != null) currentTab.webView.findNext(false);
            });

            chatgptFinderNext.setOnClickListener(v -> {
                TabItem currentTab = getTabById(activeTabId);
                if (currentTab != null && currentTab.webView != null) currentTab.webView.findNext(true);
            });

            chatgptDockShrinkBtn.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                chatgptDockScroll.setVisibility(View.GONE);
                chatgptDockBall.setVisibility(View.VISIBLE);
            });

            chatgptDockBall.setOnTouchListener(new View.OnTouchListener() {
                private float ballDX, ballDY, startX, startY;
                private boolean isBallDragging = false;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getActionMasked()) {
                        case MotionEvent.ACTION_DOWN:
                            ballDX = chatgptDockContainer.getX() - event.getRawX();
                            ballDY = chatgptDockContainer.getY() - event.getRawY();
                            startX = event.getRawX();
                            startY = event.getRawY();
                            isBallDragging = false;
                            return true;

                        case MotionEvent.ACTION_MOVE:
                            float deltaX = Math.abs(event.getRawX() - startX);
                            float deltaY = Math.abs(event.getRawY() - startY);
                            if (deltaX > 10 || deltaY > 10) {
                                isBallDragging = true;
                                chatgptDockContainer.animate()
                                        .x(event.getRawX() + ballDX)
                                        .y(event.getRawY() + ballDY)
                                        .setDuration(0)
                                        .start();
                            }
                            return true;

                        case MotionEvent.ACTION_UP:
                            if (!isBallDragging) {
                                playUiFeedbackSound("tap");
                                chatgptDockBall.setVisibility(View.GONE);
                                chatgptDockScroll.setVisibility(View.VISIBLE);
                            }
                            return true;
                    }
                    return false;
                }
            });

            View.OnTouchListener chatgptMover = new View.OnTouchListener() {
                private float dockDX, dockDY;

                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    if (event.getPointerCount() >= 2 || view == chatgptDockDragHandle) {
                        switch (event.getActionMasked()) {
                            case MotionEvent.ACTION_DOWN:
                            case MotionEvent.ACTION_POINTER_DOWN:
                                dockDX = chatgptDockContainer.getX() - event.getRawX();
                                dockDY = chatgptDockContainer.getY() - event.getRawY();
                                return true;

                            case MotionEvent.ACTION_MOVE:
                                chatgptDockContainer.animate()
                                        .x(event.getRawX() + dockDX)
                                        .y(event.getRawY() + dockDY)
                                        .setDuration(0)
                                        .start();
                                return true;
                        }
                    }
                    return false;
                }
            };
            chatgptDockDragHandle.setOnTouchListener(chatgptMover);
            chatgptDockScroll.setOnTouchListener(chatgptMover);

            updateChatgptDockButtons();
            chatgptDockScroll.setVisibility(View.GONE);
            chatgptDockBall.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            Log.e(TAG, "setupLiquidGlassChatGPTDock error: " + e.getMessage());
        }
    }

    private void updateChatgptDockButtons() {
        if (chatgptDockToggleBtn == null) return;
        try {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            boolean enabled = "true".equalsIgnoreCase(prefs.getString("chat_limit_enabled", "false"));
            String mode = prefs.getString("chat_pruning_mode", "sliding_window");
            int limit = 5;
            try { limit = Integer.parseInt(prefs.getString("chat_message_limit", "5")); } catch(Exception ignored){}

            chatgptDockToggleBtn.setText(enabled ? "✂️ Limit: ON" : "✂️ Limit: OFF");
            chatgptDockToggleBtn.setTextColor(enabled ? 0xFF00E5FF : 0x88FFFFFF);

            if (chatgptDockModeBtn != null) {
                chatgptDockModeBtn.setVisibility(enabled ? View.VISIBLE : View.GONE);
                chatgptDockModeBtn.setText("sliding_window".equalsIgnoreCase(mode) ? "🪟 Sliding" : "📜 Tail");
            }

            if (chatgptDockLimitBtn != null) {
                if (!enabled) {
                    chatgptDockLimitBtn.setText("2 msgs");
                } else {
                    chatgptDockLimitBtn.setText(limit >= 9999 ? "∞ msgs" : (limit + " msgs"));
                }
            }
        } catch (Exception ignored) {}
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupLiquidGlassGeminiDock() {
        if (geminiDockContainer == null) return;

        try {
            geminiDockClose.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                toggleGeminiDock(false);
            });

            geminiDockReload.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                reloadActiveTab();
            });

            geminiDockToggleBtn.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                boolean curEnabled = "true".equalsIgnoreCase(prefs.getString("chat_limit_enabled", "false"));
                boolean nextEnabled = !curEnabled;
                int limit = 5;
                try { limit = Integer.parseInt(prefs.getString("chat_message_limit", "5")); } catch(Exception ignored){}
                String mode = prefs.getString("chat_pruning_mode", "sliding_window");

                new CaspianBridge(this).applyPruningSettings(limit, mode, nextEnabled);
                updateGeminiDockButtons();
                Toast.makeText(this, "✂️ Pruning " + (nextEnabled ? "ENABLED" : "DISABLED"), Toast.LENGTH_SHORT).show();
            });

            geminiDockLimitBtn.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                PopupMenu popup = new PopupMenu(this, v);
                int[] limits = {2, 4, 6, 8, 10, 14, 18, 20, 28, 48, 9999};
                String[] labels = {"2 Messages", "4 Messages", "6 Messages", "8 Messages", "10 Messages", "14 Messages", "18 Messages", "20 Messages", "28 Messages", "48 Messages", "∞ Unlimited (Show All)"};

                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                int curLimit = 5;
                try { curLimit = Integer.parseInt(prefs.getString("chat_message_limit", "5")); } catch(Exception ignored){}

                for (int i = 0; i < limits.length; i++) {
                    String label = (curLimit == limits[i] ? "✓ " : "   ") + labels[i];
                    popup.getMenu().add(0, limits[i], i, label);
                }

                popup.setOnMenuItemClickListener(item -> {
                    playUiFeedbackSound("tap");
                    int selectedLimit = item.getItemId();
                    String mode = prefs.getString("chat_pruning_mode", "sliding_window");
                    boolean enabled = "true".equalsIgnoreCase(prefs.getString("chat_limit_enabled", "false"));

                    new CaspianBridge(this).applyPruningSettings(selectedLimit, mode, enabled);
                    updateGeminiDockButtons();
                    Toast.makeText(this, "Message Limit: " + (selectedLimit >= 9999 ? "Unlimited" : (selectedLimit + " msgs")), Toast.LENGTH_SHORT).show();
                    return true;
                });
                popup.show();
            });

            geminiMsgUpBtn.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                TabItem currentTab = getTabById(activeTabId);
                if (currentTab != null && currentTab.webView != null) {
                    SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                    boolean enabled = "true".equalsIgnoreCase(prefs.getString("chat_limit_enabled", "false"));
                    int step = enabled ? -1 : -2;
                    currentTab.webView.evaluateJavascript("if (window.__CASPIAN_PRUNER_STEP) window.__CASPIAN_PRUNER_STEP(" + step + "); else window.scrollBy({top: -400, behavior: 'smooth'});", null);
                }
            });

            geminiMsgDownBtn.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                TabItem currentTab = getTabById(activeTabId);
                if (currentTab != null && currentTab.webView != null) {
                    SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                    boolean enabled = "true".equalsIgnoreCase(prefs.getString("chat_limit_enabled", "false"));
                    int step = enabled ? 1 : 2;
                    currentTab.webView.evaluateJavascript("if (window.__CASPIAN_PRUNER_STEP) window.__CASPIAN_PRUNER_STEP(" + step + "); else window.scrollBy({top: 400, behavior: 'smooth'});", null);
                }
            });

            geminiFinderBtn.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                boolean isFinderOpen = geminiFinderBox.getVisibility() == View.VISIBLE;
                if (isFinderOpen) {
                    geminiFinderBox.setVisibility(View.GONE);
                    geminiFinderPrev.setVisibility(View.GONE);
                    geminiFinderNext.setVisibility(View.GONE);
                    TabItem currentTab = getTabById(activeTabId);
                    if (currentTab != null && currentTab.webView != null) currentTab.webView.clearMatches();
                    hideKeyboard();
                } else {
                    geminiFinderBox.setVisibility(View.VISIBLE);
                    geminiFinderPrev.setVisibility(View.VISIBLE);
                    geminiFinderNext.setVisibility(View.VISIBLE);
                    geminiFinderInput.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) imm.showSoftInput(geminiFinderInput, InputMethodManager.SHOW_IMPLICIT);
                }
            });

            geminiFinderInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    TabItem currentTab = getTabById(activeTabId);
                    if (currentTab != null && currentTab.webView != null) {
                        currentTab.webView.findAllAsync(s.toString());
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            geminiFinderPrev.setOnClickListener(v -> {
                TabItem currentTab = getTabById(activeTabId);
                if (currentTab != null && currentTab.webView != null) currentTab.webView.findNext(false);
            });

            geminiFinderNext.setOnClickListener(v -> {
                TabItem currentTab = getTabById(activeTabId);
                if (currentTab != null && currentTab.webView != null) currentTab.webView.findNext(true);
            });

            geminiDockShrinkBtn.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                geminiDockScroll.setVisibility(View.GONE);
                geminiDockBall.setVisibility(View.VISIBLE);
            });

            geminiDockBall.setOnTouchListener(new View.OnTouchListener() {
                private float ballDX, ballDY, startX, startY;
                private boolean isBallDragging = false;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getActionMasked()) {
                        case MotionEvent.ACTION_DOWN:
                            ballDX = geminiDockContainer.getX() - event.getRawX();
                            ballDY = geminiDockContainer.getY() - event.getRawY();
                            startX = event.getRawX();
                            startY = event.getRawY();
                            isBallDragging = false;
                            return true;

                        case MotionEvent.ACTION_MOVE:
                            float deltaX = Math.abs(event.getRawX() - startX);
                            float deltaY = Math.abs(event.getRawY() - startY);
                            if (deltaX > 10 || deltaY > 10) {
                                isBallDragging = true;
                                geminiDockContainer.animate()
                                        .x(event.getRawX() + ballDX)
                                        .y(event.getRawY() + ballDY)
                                        .setDuration(0)
                                        .start();
                            }
                            return true;

                        case MotionEvent.ACTION_UP:
                            if (!isBallDragging) {
                                playUiFeedbackSound("tap");
                                geminiDockBall.setVisibility(View.GONE);
                                geminiDockScroll.setVisibility(View.VISIBLE);
                            }
                            return true;
                    }
                    return false;
                }
            });

            View.OnTouchListener geminiMover = new View.OnTouchListener() {
                private float dockDX, dockDY;

                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    if (event.getPointerCount() >= 2 || view == geminiDockDragHandle) {
                        switch (event.getActionMasked()) {
                            case MotionEvent.ACTION_DOWN:
                            case MotionEvent.ACTION_POINTER_DOWN:
                                dockDX = geminiDockContainer.getX() - event.getRawX();
                                dockDY = geminiDockContainer.getY() - event.getRawY();
                                return true;

                            case MotionEvent.ACTION_MOVE:
                                geminiDockContainer.animate()
                                        .x(event.getRawX() + dockDX)
                                        .y(event.getRawY() + dockDY)
                                        .setDuration(0)
                                        .start();
                                return true;
                        }
                    }
                    return false;
                }
            };
            geminiDockDragHandle.setOnTouchListener(geminiMover);
            geminiDockScroll.setOnTouchListener(geminiMover);

            updateGeminiDockButtons();
            geminiDockScroll.setVisibility(View.GONE);
            geminiDockBall.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            Log.e(TAG, "setupLiquidGlassGeminiDock error: " + e.getMessage());
        }
    }

    private void updateGeminiDockButtons() {
        if (geminiDockToggleBtn == null) return;
        try {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            boolean enabled = "true".equalsIgnoreCase(prefs.getString("chat_limit_enabled", "false"));
            int limit = 5;
            try { limit = Integer.parseInt(prefs.getString("chat_message_limit", "5")); } catch(Exception ignored){}

            geminiDockToggleBtn.setText(enabled ? "✂️ Limit: ON" : "✂️ Limit: OFF");
            geminiDockToggleBtn.setTextColor(enabled ? 0xFF00E5FF : 0x88FFFFFF);

            if (geminiDockLimitBtn != null) {
                if (!enabled) {
                    geminiDockLimitBtn.setText("2 msgs");
                } else {
                    geminiDockLimitBtn.setText(limit >= 9999 ? "∞ msgs" : (limit + " msgs"));
                }
            }
        } catch (Exception ignored) {}
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupLiquidGlassYouTubeRemote() {
        if (ytFloatingRemoteContainer == null) return;

        try {
            ytRemoteClose.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                toggleFloatingYouTubeRemote(false);
            });

            ytRemoteReload.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                reloadActiveTab();
            });

            ytRemoteFullscreen.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                toggleFullscreenYouTube();
            });

            ytRemotePrevVideo.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                TabItem currentTab = getTabById(activeTabId);
                if (currentTab != null && currentTab.webView != null) {
                    currentTab.webView.evaluateJavascript("if (window.__CaspianYouTube) window.__CaspianYouTube.previousVideo();", null);
                }
            });

            ytRemoteSeekBack.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                seekYouTube(-5);
            });

            ytRemotePlayPause.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                togglePlayYouTube();
            });

            ytRemoteSeekFwd.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                seekYouTube(5);
            });

            ytRemoteNextVideo.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                TabItem currentTab = getTabById(activeTabId);
                if (currentTab != null && currentTab.webView != null) {
                    currentTab.webView.evaluateJavascript("if (window.__CaspianYouTube) window.__CaspianYouTube.nextVideo();", null);
                }
            });

            ytRemoteMute.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                toggleMuteYouTube();
            });

            ytRemoteSpeedBtn.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                List<CaspianMenuItem> speedItems = new ArrayList<>();
                speedItems.add(new CaspianMenuItem("0.25x", () -> setYouTubeSpeed(0.25)));
                speedItems.add(new CaspianMenuItem("0.5x", () -> setYouTubeSpeed(0.5)));
                speedItems.add(new CaspianMenuItem("0.75x", () -> setYouTubeSpeed(0.75)));
                speedItems.add(new CaspianMenuItem("1.0x (Normal)", () -> setYouTubeSpeed(1.0)));
                speedItems.add(new CaspianMenuItem("1.25x", () -> setYouTubeSpeed(1.25)));
                speedItems.add(new CaspianMenuItem("1.5x", () -> setYouTubeSpeed(1.5)));
                speedItems.add(new CaspianMenuItem("1.75x", () -> setYouTubeSpeed(1.75)));
                speedItems.add(new CaspianMenuItem("2.0x", () -> setYouTubeSpeed(2.0)));
                showCaspianCustomPopup(v, speedItems);
            });

            ytRemoteQualityBtn.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                List<CaspianMenuItem> qualityItems = new ArrayList<>();
                qualityItems.add(new CaspianMenuItem("🎯 Auto Quality", () -> setYouTubeQuality("auto")));
                qualityItems.add(new CaspianMenuItem("💎 1080p (HD)", () -> setYouTubeQuality("hd1080")));
                qualityItems.add(new CaspianMenuItem("📺 720p (HD)", () -> setYouTubeQuality("hd720")));
                qualityItems.add(new CaspianMenuItem("⚡ 480p", () -> setYouTubeQuality("large")));
                qualityItems.add(new CaspianMenuItem("📱 360p", () -> setYouTubeQuality("medium")));
                qualityItems.add(new CaspianMenuItem("🔋 240p", () -> setYouTubeQuality("small")));
                qualityItems.add(new CaspianMenuItem("🍃 144p (Data Saver)", () -> setYouTubeQuality("tiny")));
                showCaspianCustomPopup(v, qualityItems);
            });

            ytRemoteShrinkBtn.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                ytFloatingRemoteScroll.setVisibility(View.GONE);
                ytFloatingRemoteBall.setVisibility(View.VISIBLE);
            });

            ytFloatingRemoteBall.setOnTouchListener(new View.OnTouchListener() {
                private float ballDX, ballDY, startX, startY;
                private boolean isBallDragging = false;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getActionMasked()) {
                        case MotionEvent.ACTION_DOWN:
                            ballDX = ytFloatingRemoteContainer.getX() - event.getRawX();
                            ballDY = ytFloatingRemoteContainer.getY() - event.getRawY();
                            startX = event.getRawX();
                            startY = event.getRawY();
                            isBallDragging = false;
                            return true;

                        case MotionEvent.ACTION_MOVE:
                            float deltaX = Math.abs(event.getRawX() - startX);
                            float deltaY = Math.abs(event.getRawY() - startY);
                            if (deltaX > 10 || deltaY > 10) {
                                isBallDragging = true;
                                ytFloatingRemoteContainer.animate()
                                        .x(event.getRawX() + ballDX)
                                        .y(event.getRawY() + ballDY)
                                        .setDuration(0)
                                        .start();
                            }
                            return true;

                        case MotionEvent.ACTION_UP:
                            if (!isBallDragging) {
                                playUiFeedbackSound("tap");
                                ytFloatingRemoteBall.setVisibility(View.GONE);
                                ytFloatingRemoteScroll.setVisibility(View.VISIBLE);
                            }
                            return true;
                    }
                    return false;
                }
            });

            View.OnTouchListener remoteMover = new View.OnTouchListener() {
                private float podDX, podDY;

                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    if (event.getPointerCount() >= 2 || view == ytRemoteDragHandle) {
                        switch (event.getActionMasked()) {
                            case MotionEvent.ACTION_DOWN:
                            case MotionEvent.ACTION_POINTER_DOWN:
                                podDX = ytFloatingRemoteContainer.getX() - event.getRawX();
                                podDY = ytFloatingRemoteContainer.getY() - event.getRawY();
                                return true;

                            case MotionEvent.ACTION_MOVE:
                                ytFloatingRemoteContainer.animate()
                                        .x(event.getRawX() + podDX)
                                        .y(event.getRawY() + podDY)
                                        .setDuration(0)
                                        .start();
                                return true;
                        }
                    }
                    return false;
                }
            };
            ytRemoteDragHandle.setOnTouchListener(remoteMover);
            ytFloatingRemoteScroll.setOnTouchListener(remoteMover);
            ytFloatingRemoteScroll.setVisibility(View.GONE);
            ytFloatingRemoteBall.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            Log.e(TAG, "setupLiquidGlassYouTubeRemote error: " + e.getMessage());
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupLiquidGlassGoogleDock() {
        if (searchNavContainer == null) return;

        try {
            navDockClose.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                toggleGoogleSearchDock(false);
            });

            navDockReload.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                reloadActiveTab();
            });

            navBackBtn.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                TabItem currentTab = getTabById(activeTabId);
                if (currentTab != null && currentTab.webView.canGoBack()) currentTab.webView.goBack();
            });

            navForwardBtn.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                TabItem currentTab = getTabById(activeTabId);
                if (currentTab != null && currentTab.webView.canGoForward()) currentTab.webView.goForward();
            });

            searchDockUrl.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                omniboxEditText.requestFocus();
                omniboxEditText.selectAll();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) imm.showSoftInput(omniboxEditText, InputMethodManager.SHOW_IMPLICIT);
            });

            navFinderBtn.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                boolean isFinderOpen = navFinderBox.getVisibility() == View.VISIBLE;
                if (isFinderOpen) {
                    navFinderBox.setVisibility(View.GONE);
                    navFinderPrev.setVisibility(View.GONE);
                    navFinderNext.setVisibility(View.GONE);
                    searchDockUrl.setVisibility(View.VISIBLE);
                    TabItem currentTab = getTabById(activeTabId);
                    if (currentTab != null && currentTab.webView != null) currentTab.webView.clearMatches();
                    hideKeyboard();
                } else {
                    searchDockUrl.setVisibility(View.GONE);
                    navFinderBox.setVisibility(View.VISIBLE);
                    navFinderPrev.setVisibility(View.VISIBLE);
                    navFinderNext.setVisibility(View.VISIBLE);
                    navFinderInput.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) imm.showSoftInput(navFinderInput, InputMethodManager.SHOW_IMPLICIT);
                }
            });

            navFinderInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    TabItem currentTab = getTabById(activeTabId);
                    if (currentTab != null && currentTab.webView != null) {
                        currentTab.webView.findAllAsync(s.toString());
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            navFinderPrev.setOnClickListener(v -> {
                TabItem currentTab = getTabById(activeTabId);
                if (currentTab != null && currentTab.webView != null) currentTab.webView.findNext(false);
            });

            navFinderNext.setOnClickListener(v -> {
                TabItem currentTab = getTabById(activeTabId);
                if (currentTab != null && currentTab.webView != null) currentTab.webView.findNext(true);
            });

            navScrollTopBtn.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                TabItem currentTab = getTabById(activeTabId);
                if (currentTab != null && currentTab.webView != null) {
                    currentTab.webView.evaluateJavascript("window.scrollTo({top: 0, behavior: 'smooth'});", null);
                }
            });

            navScrollBottomBtn.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                TabItem currentTab = getTabById(activeTabId);
                if (currentTab != null && currentTab.webView != null) {
                    currentTab.webView.evaluateJavascript("window.scrollTo({top: document.body.scrollHeight, behavior: 'smooth'});", null);
                }
            });

            navDockShrinkBtn.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                searchDockScroll.setVisibility(View.GONE);
                searchNavBall.setVisibility(View.VISIBLE);
            });

            searchNavBall.setOnTouchListener(new View.OnTouchListener() {
                private float ballDX, ballDY, startX, startY;
                private boolean isBallDragging = false;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getActionMasked()) {
                        case MotionEvent.ACTION_DOWN:
                            ballDX = searchNavContainer.getX() - event.getRawX();
                            ballDY = searchNavContainer.getY() - event.getRawY();
                            startX = event.getRawX();
                            startY = event.getRawY();
                            isBallDragging = false;
                            return true;

                        case MotionEvent.ACTION_MOVE:
                            float deltaX = Math.abs(event.getRawX() - startX);
                            float deltaY = Math.abs(event.getRawY() - startY);
                            if (deltaX > 10 || deltaY > 10) {
                                isBallDragging = true;
                                searchNavContainer.animate()
                                        .x(event.getRawX() + ballDX)
                                        .y(event.getRawY() + ballDY)
                                        .setDuration(0)
                                        .start();
                            }
                            return true;

                        case MotionEvent.ACTION_UP:
                            if (!isBallDragging) {
                                playUiFeedbackSound("tap");
                                searchNavBall.setVisibility(View.GONE);
                                searchDockScroll.setVisibility(View.VISIBLE);
                            }
                            return true;
                    }
                    return false;
                }
            });

            View.OnTouchListener dockMover = new View.OnTouchListener() {
                private float dockDX, dockDY;

                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    if (event.getPointerCount() >= 2 || view == navDockDragHandle) {
                        switch (event.getActionMasked()) {
                            case MotionEvent.ACTION_DOWN:
                            case MotionEvent.ACTION_POINTER_DOWN:
                                dockDX = searchNavContainer.getX() - event.getRawX();
                                dockDY = searchNavContainer.getY() - event.getRawY();
                                return true;

                            case MotionEvent.ACTION_MOVE:
                                searchNavContainer.animate()
                                        .x(event.getRawX() + dockDX)
                                        .y(event.getRawY() + dockDY)
                                        .setDuration(0)
                                        .start();
                                return true;
                        }
                    }
                    return false;
                }
            };
            navDockDragHandle.setOnTouchListener(dockMover);
            searchDockScroll.setOnTouchListener(dockMover);
            searchDockScroll.setVisibility(View.GONE);
            searchNavBall.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            Log.e(TAG, "setupLiquidGlassGoogleDock error: " + e.getMessage());
        }
    }

    private void setupOmniboxListeners() {
        omniboxBackBtn.setOnClickListener(v -> {
            TabItem tab = getActiveOrDominantTab();
            if (tab != null && tab.webView != null && tab.webView.canGoBack()) {
                tab.webView.goBack();
            }
        });

        omniboxForwardBtn.setOnClickListener(v -> {
            TabItem tab = getActiveOrDominantTab();
            if (tab != null && tab.webView != null && tab.webView.canGoForward()) {
                tab.webView.goForward();
            }
        });

        omniboxReloadBtn.setOnClickListener(v -> {
            reloadActiveTab();
        });

        omniboxShieldIcon.setOnClickListener(v -> showShieldStatusDialog());

        omniboxEditText.setOnTouchListener(new View.OnTouchListener() {
            private long lastTapTime = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    long now = System.currentTimeMillis();
                    if (!omniboxEditText.hasFocus()) {
                        omniboxEditText.requestFocus();
                        omniboxEditText.post(() -> {
                            omniboxEditText.selectAll();
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            if (imm != null) imm.showSoftInput(omniboxEditText, InputMethodManager.SHOW_IMPLICIT);
                        });
                        lastTapTime = now;
                        return true;
                    } else if (now - lastTapTime > 400 && omniboxEditText.getSelectionStart() == 0 && omniboxEditText.getSelectionEnd() == omniboxEditText.getText().length()) {
                        lastTapTime = now;
                        return false;
                    }
                    lastTapTime = now;
                }
                return false;
            }
        });

        omniboxEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                String input = omniboxEditText.getText().toString().trim();
                handleOmniboxSubmission(input);
                hideKeyboard();
                omniboxEditText.clearFocus();
                return true;
            }
            return false;
        });

        omniboxClearBtn.setOnClickListener(v -> omniboxEditText.setText(""));

        if (omniboxFinderBtn != null) {
            omniboxFinderBtn.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                showOmniboxFinder();
            });
        }

        omniboxFinderClose.setOnClickListener(v -> {
            playUiFeedbackSound("tap");
            hideOmniboxFinder();
        });

        omniboxFinderInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                TabItem currentTab = getActiveOrDominantTab();
                if (currentTab != null && currentTab.webView != null) {
                    currentTab.webView.findAllAsync(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        omniboxFinderPrev.setOnClickListener(v -> {
            TabItem currentTab = getActiveOrDominantTab();
            if (currentTab != null && currentTab.webView != null) currentTab.webView.findNext(false);
        });

        omniboxFinderNext.setOnClickListener(v -> {
            TabItem currentTab = getActiveOrDominantTab();
            if (currentTab != null && currentTab.webView != null) currentTab.webView.findNext(true);
        });

        omniboxVoiceBtn.setOnClickListener(v -> {
            if (isRecordingSpeechMode) {
                stopSpeechToText();
            } else {
                isUniversalVoiceActive = true;
                startSpeechToText();
            }
        });

        omniboxToolbarsBtn.setOnClickListener(v -> {
            playUiFeedbackSound("tap");
            showQuickToolbarsPopup(v);
        });

        omniboxSplitBtn.setOnClickListener(v -> cycleSplitViewMode());

        omniboxTabsBtn.setOnClickListener(v -> {
            playUiFeedbackSound("tap");
            showTabGridView();
        });

        omniboxMenuBtn.setOnClickListener(v -> showBrowserMenu(v));
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void setupOmniboxSwipeTabSwitcher() {
        if (omniboxHeaderWrapper == null) return;

        omniboxHeaderWrapper.setOnTouchListener(new View.OnTouchListener() {
            private float startX = 0f;
            private float startY = 0f;
            private boolean isSwiping = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getRawX();
                        startY = event.getRawY();
                        isSwiping = false;
                        return false;

                    case MotionEvent.ACTION_MOVE:
                        float dx = event.getRawX() - startX;
                        float dy = Math.abs(event.getRawY() - startY);
                        if (Math.abs(dx) > 30 && dy < 40 && omniboxEditText != null && !omniboxEditText.hasFocus()) {
                            isSwiping = true;
                            return true;
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        if (isSwiping) {
                            float deltaX = event.getRawX() - startX;
                            if (Math.abs(deltaX) > 60) {
                                if (deltaX > 0) {
                                    switchToAdjacentTab(-1);
                                } else {
                                    switchToAdjacentTab(1);
                                }
                                return true;
                            }
                        }
                        break;
                }
                return false;
            }
        });
    }

    public void switchToAdjacentTab(int direction) {
        if (tabsList == null || tabsList.size() <= 1) return;
        int currentIndex = -1;
        for (int i = 0; i < tabsList.size(); i++) {
            if (tabsList.get(i).id == activeTabId) {
                currentIndex = i;
                break;
            }
        }
        if (currentIndex < 0) return;

        int targetIndex = (currentIndex + direction + tabsList.size()) % tabsList.size();
        TabItem targetTab = tabsList.get(targetIndex);

        if (webViewContainer != null) {
            float offset = direction > 0 ? 100f : -100f;
            webViewContainer.animate()
                    .translationX(-offset)
                    .alpha(0.5f)
                    .setDuration(90)
                    .withEndAction(() -> {
                        switchToTab(targetTab.id);
                        webViewContainer.setTranslationX(offset);
                        webViewContainer.animate()
                                .translationX(0f)
                                .alpha(1.0f)
                                .setDuration(120)
                                .start();
                    })
                    .start();
        } else {
            switchToTab(targetTab.id);
        }
    }

    private void setupOmniboxSuggestions() {
        if (omniboxEditText == null || omniboxSuggestionsContainer == null) return;

        omniboxEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                omniboxEditText.post(() -> {
                    omniboxEditText.selectAll();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) imm.showSoftInput(omniboxEditText, InputMethodManager.SHOW_IMPLICIT);
                });
                checkClipboardAndShowSuggestions(omniboxEditText.getText().toString());
            } else {
                omniboxSuggestionsContainer.setVisibility(View.GONE);
            }
        });

        omniboxEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (omniboxEditText.hasFocus()) {
                    checkClipboardAndShowSuggestions(s != null ? s.toString() : "");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void checkClipboardAndShowSuggestions(String currentText) {
        if (omniboxSuggestionsContainer == null) return;

        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        boolean hasClipUrl = false;
        if (clipboard != null && clipboard.hasPrimaryClip() && clipboard.getPrimaryClip() != null && clipboard.getPrimaryClip().getItemCount() > 0) {
            CharSequence clipText = clipboard.getPrimaryClip().getItemAt(0).getText();
            if (clipText != null && !clipText.toString().trim().isEmpty()) {
                String link = clipText.toString().trim();
                if (link.startsWith("http://") || link.startsWith("https://") || link.contains(".")) {
                    hasClipUrl = true;
                    if (omniboxClipboardText != null) omniboxClipboardText.setText(link);
                    if (omniboxClipboardChip != null) {
                        omniboxClipboardChip.setVisibility(View.VISIBLE);
                        omniboxClipboardChip.setOnClickListener(v -> {
                            playUiFeedbackSound("tap");
                            omniboxEditText.setText(link);
                            handleOmniboxSubmission(link);
                            omniboxSuggestionsContainer.setVisibility(View.GONE);
                            hideKeyboard();
                            omniboxEditText.clearFocus();
                        });
                    }
                }
            }
        }

        if (omniboxSuggestionsList != null) {
            omniboxSuggestionsList.removeAllViews();
        }

        if (hasClipUrl) {
            omniboxSuggestionsContainer.setVisibility(View.VISIBLE);
        } else {
            if (omniboxClipboardChip != null) omniboxClipboardChip.setVisibility(View.GONE);
            omniboxSuggestionsContainer.setVisibility(View.GONE);
        }
    }

    private void showOmniboxFinder() {
        if (omniboxUrlContainer != null && omniboxFinderContainer != null) {
            omniboxUrlContainer.setVisibility(View.GONE);
            omniboxFinderContainer.setVisibility(View.VISIBLE);
            omniboxFinderInput.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(omniboxFinderInput, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void hideOmniboxFinder() {
        if (omniboxUrlContainer != null && omniboxFinderContainer != null) {
            omniboxFinderContainer.setVisibility(View.GONE);
            omniboxUrlContainer.setVisibility(View.VISIBLE);
            TabItem currentTab = getActiveOrDominantTab();
            if (currentTab != null && currentTab.webView != null) currentTab.webView.clearMatches();
            hideKeyboard();
        }
    }

    private static class CaspianMenuItem {
        final String title;
        final Runnable action;
        final boolean isDanger;

        CaspianMenuItem(String title, Runnable action) {
            this(title, action, false);
        }

        CaspianMenuItem(String title, Runnable action, boolean isDanger) {
            this.title = title;
            this.action = action;
            this.isDanger = isDanger;
        }
    }

    private void showCaspianCustomPopup(View anchor, List<CaspianMenuItem> items) {
        View popupView = getLayoutInflater().inflate(R.layout.popup_caspian_menu, null);
        LinearLayout itemsContainer = popupView.findViewById(R.id.menu_items_container);

        PopupWindow popupWindow = new PopupWindow(
                popupView,
                dpToPx(230),
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setElevation(dpToPx(20));

        for (CaspianMenuItem item : items) {
            LinearLayout row = new LinearLayout(this);
            row.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(42)));
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dpToPx(12), 0, dpToPx(12), 0);
            row.setBackgroundResource(R.drawable.bg_liquid_glass_pill);

            TextView title = new TextView(this);
            title.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            title.setText(item.title);
            title.setTextColor(item.isDanger ? 0xFFFF6B6B : 0xFFDFE2F0);
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            title.setTypeface(null, android.graphics.Typeface.BOLD);

            row.addView(title);

            row.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                popupWindow.dismiss();
                if (item.action != null) item.action.run();
            });

            LinearLayout.LayoutParams rowLp = (LinearLayout.LayoutParams) row.getLayoutParams();
            rowLp.bottomMargin = dpToPx(4);
            row.setLayoutParams(rowLp);

            itemsContainer.addView(row);
        }

        popupWindow.showAsDropDown(anchor, 0, dpToPx(4));
    }

    private void showQuickToolbarsPopup(View anchor) {
        List<CaspianMenuItem> menuItems = new ArrayList<>();
        menuItems.add(new CaspianMenuItem(
                "🤖 ChatGPT Dock: " + (isChatgptDockExplicitlyHidden ? "OFF" : "ON"),
                () -> toggleChatGPTDock(isChatgptDockExplicitlyHidden)
        ));
        menuItems.add(new CaspianMenuItem(
                "♊ Gemini Dock: " + (isGeminiDockExplicitlyHidden ? "OFF" : "ON"),
                () -> toggleGeminiDock(isGeminiDockExplicitlyHidden)
        ));
        menuItems.add(new CaspianMenuItem(
                "🎬 YouTube Remote: " + (isYtRemoteExplicitlyHidden ? "OFF" : "ON"),
                () -> toggleFloatingYouTubeRemote(isYtRemoteExplicitlyHidden)
        ));
        menuItems.add(new CaspianMenuItem(
                "🌐 Google Dock: " + (isSearchNavExplicitlyHidden ? "OFF" : "ON"),
                () -> toggleGoogleSearchDock(isSearchNavExplicitlyHidden)
        ));
        menuItems.add(new CaspianMenuItem(
                "🛡️ Privacy Shield: " + (adBlockShield.isEnabled() ? "ON" : "OFF"),
                () -> {
                    adBlockShield.setEnabled(!adBlockShield.isEnabled());
                    updateOmniboxState();
                }
        ));

        showCaspianCustomPopup(anchor, menuItems);
    }

    public void handleOmniboxSubmission(String rawInput) {
        if (rawInput.isEmpty()) return;
        AICommandRouter.RouteResult route = AICommandRouter.resolve(rawInput, currentSearchEngine);

        TabItem currentTab = getActiveOrDominantTab();
        if (currentTab != null) {
            currentTab.service = route.service;
            currentTab.pendingPrompt = route.promptPayload;
            currentTab.isReaderMode = route.isReaderMode;
            currentTab.webView.loadUrl(route.targetUrl);
        } else {
            addNewTab(route.service, route.promptPayload, route.targetUrl, false);
        }
    }

    private void showShieldStatusDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🛡️ Caspian Privacy Shield");
        String message = "Status: " + (adBlockShield.isEnabled() ? "ACTIVE (Shielding)" : "PAUSED") +
                "\n\nBlocked Ads & Trackers: " + adBlockShield.getBlockedCount() +
                "\nHardware GPU Acceleration: 60/120 FPS Active" +
                "\nUniversal DOM Pruning: Dynamic Sliding Window Active";
        builder.setMessage(message);
        builder.setPositiveButton("OK", null);
        builder.setNeutralButton("Toggle Shield", (dialog, which) -> {
            adBlockShield.setEnabled(!adBlockShield.isEnabled());
            updateOmniboxState();
        });
        builder.setNegativeButton("Close", null);
        builder.show();
    }

    private void showBrowserMenu(View anchor) {
        TabItem currentTab = getActiveOrDominantTab();
        List<CaspianMenuItem> menuItems = new ArrayList<>();
        menuItems.add(new CaspianMenuItem("➕ New Tab", () -> addNewTab("web", null)));
        menuItems.add(new CaspianMenuItem(
                (currentTab != null && currentTab.isDesktop) ? "🖥️ Desktop site [ON]" : "🖥️ Desktop site [OFF]",
                () -> { if (currentTab != null) toggleDesktopMode(currentTab.id); }
        ));
        menuItems.add(new CaspianMenuItem("🔍 Find in page", () -> showOmniboxFinder()));
        menuItems.add(new CaspianMenuItem("⚡ Dual AI Ask", () -> launchDualAIAsk()));
        menuItems.add(new CaspianMenuItem("🔀 Split Screen", () -> cycleSplitViewMode()));
        menuItems.add(new CaspianMenuItem("📜 History", () -> showHistoryDialog()));
        menuItems.add(new CaspianMenuItem("📤 Share & Export", () -> showExportOptions()));
        menuItems.add(new CaspianMenuItem("📥 Downloads", () -> openDownloadsFolder()));
        menuItems.add(new CaspianMenuItem("🔍 Page Zoom (" + currentTextZoom + "%)", () -> showPageZoomDialog()));

        showCaspianCustomPopup(anchor, menuItems);
    }

    public void launchDualAIAsk() {
        playUiFeedbackSound("tap");
        if (splitModeState == 0) {
            TabItem gptTab = null;
            TabItem geminiTab = null;
            for (TabItem t : tabsList) {
                if (gptTab == null && (t.url.contains("chatgpt.com") || "chatgpt".equalsIgnoreCase(t.service))) gptTab = t;
                if (geminiTab == null && (t.url.contains("gemini.google.com") || "gemini".equalsIgnoreCase(t.service))) geminiTab = t;
            }

            if (gptTab == null) {
                int id = nextTabId++;
                gptTab = createNewTabInstance(id, "https://chatgpt.com", "chatgpt", null, false);
                gptTab.title = "ChatGPT";
                tabsList.add(gptTab);
            }
            if (geminiTab == null) {
                int id = nextTabId++;
                geminiTab = createNewTabInstance(id, "https://gemini.google.com/app", "gemini", null, false);
                geminiTab.title = "Gemini";
                tabsList.add(geminiTab);
            }
            activeTabId = gptTab.id;
            secondarySplitTabId = geminiTab.id;
            splitModeState = 1;
            splitRatio = 0.5f;
            applySplitViewLayout();
        }
        toggleSplitArenaBroadcast(true);
        Toast.makeText(this, "⚡ Dual AI Ask Active!", Toast.LENGTH_SHORT).show();
    }

    private void showHistoryDialog() {
        playUiFeedbackSound("tap");
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_history, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0xF2050811));
        }

        ImageButton closeBtn = dialogView.findViewById(R.id.history_close_btn);
        EditText searchInput = dialogView.findViewById(R.id.history_search_input);
        LinearLayout listContainer = dialogView.findViewById(R.id.history_list_container);
        TextView emptyView = dialogView.findViewById(R.id.history_empty_view);
        Button btn1h = dialogView.findViewById(R.id.btn_clear_history_1h);
        Button btn24h = dialogView.findViewById(R.id.btn_clear_history_24h);
        Button btnAll = dialogView.findViewById(R.id.btn_clear_history_all);
        Button btnCookies = dialogView.findViewById(R.id.btn_clear_cookies);

        Runnable refreshList = () -> {
            listContainer.removeAllViews();
            String query = searchInput.getText().toString();
            List<HistoryManager.HistoryEntry> entries = HistoryManager.getInstance(this).getHistory(query);
            if (entries.isEmpty()) {
                emptyView.setVisibility(View.VISIBLE);
            } else {
                emptyView.setVisibility(View.GONE);
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault());
                for (HistoryManager.HistoryEntry entry : entries) {
                    LinearLayout row = new LinearLayout(this);
                    row.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    row.setOrientation(LinearLayout.VERTICAL);
                    row.setPadding(dpToPx(12), dpToPx(10), dpToPx(12), dpToPx(10));
                    row.setBackgroundResource(R.drawable.bg_liquid_glass_pill);

                    TextView titleView = new TextView(this);
                    titleView.setText(entry.title);
                    titleView.setTextColor(0xFFDFE2F0);
                    titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
                    titleView.setTypeface(null, android.graphics.Typeface.BOLD);
                    titleView.setSingleLine(true);
                    titleView.setEllipsize(android.text.TextUtils.TruncateAt.END);

                    LinearLayout subRow = new LinearLayout(this);
                    subRow.setOrientation(LinearLayout.HORIZONTAL);
                    subRow.setGravity(Gravity.CENTER_VERTICAL);
                    subRow.setPadding(0, dpToPx(4), 0, 0);

                    TextView urlView = new TextView(this);
                    LinearLayout.LayoutParams urlLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                    urlView.setLayoutParams(urlLp);
                    urlView.setText(cleanDisplayUrl(entry.url));
                    urlView.setTextColor(0xFF00E5FF);
                    urlView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
                    urlView.setSingleLine(true);
                    urlView.setEllipsize(android.text.TextUtils.TruncateAt.END);

                    TextView timeView = new TextView(this);
                    timeView.setText(sdf.format(new java.util.Date(entry.timestamp)));
                    timeView.setTextColor(0xFF849396);
                    timeView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
                    timeView.setPadding(dpToPx(8), 0, 0, 0);

                    subRow.addView(urlView);
                    subRow.addView(timeView);

                    row.addView(titleView);
                    row.addView(subRow);

                    row.setOnClickListener(v -> {
                        playUiFeedbackSound("tap");
                        dialog.dismiss();
                        TabItem active = getActiveOrDominantTab();
                        if (active != null) {
                            active.webView.loadUrl(entry.url);
                        } else {
                            addNewTab("web", null, entry.url, false);
                        }
                    });

                    LinearLayout.LayoutParams rowLp = (LinearLayout.LayoutParams) row.getLayoutParams();
                    rowLp.bottomMargin = dpToPx(6);
                    row.setLayoutParams(rowLp);

                    listContainer.addView(row);
                }
            }
        };

        refreshList.run();

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { refreshList.run(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        closeBtn.setOnClickListener(v -> dialog.dismiss());

        btn1h.setOnClickListener(v -> {
            playUiFeedbackSound("tap");
            long oneHourAgo = System.currentTimeMillis() - (3600 * 1000);
            HistoryManager.getInstance(this).clearHistorySince(oneHourAgo);
            refreshList.run();
            Toast.makeText(this, "🧹 Cleared history from last hour", Toast.LENGTH_SHORT).show();
        });

        btn24h.setOnClickListener(v -> {
            playUiFeedbackSound("tap");
            long twentyFourHoursAgo = System.currentTimeMillis() - (24 * 3600 * 1000);
            HistoryManager.getInstance(this).clearHistorySince(twentyFourHoursAgo);
            refreshList.run();
            Toast.makeText(this, "🧹 Cleared history from last 24 hours", Toast.LENGTH_SHORT).show();
        });

        btnAll.setOnClickListener(v -> {
            playUiFeedbackSound("tap");
            HistoryManager.getInstance(this).clearAllHistory();
            refreshList.run();
            Toast.makeText(this, "🗑️ All history cleared", Toast.LENGTH_SHORT).show();
        });

        btnCookies.setOnClickListener(v -> {
            playUiFeedbackSound("tap");
            HistoryManager.clearCookiesAndCache();
            Toast.makeText(this, "🍪 Cookies and storage cache cleared", Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }

    public void showPageZoomDialog() {
        int[] zoomLevels = {50, 75, 90, 100, 110, 125, 150, 175, 200};
        String[] zoomLabels = {"50%", "75%", "90%", "100% (Default)", "110%", "125%", "150%", "175%", "200%"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Page Zoom");

        int checkedItem = 3;
        for (int i = 0; i < zoomLevels.length; i++) {
            if (zoomLevels[i] == currentTextZoom) {
                checkedItem = i;
                break;
            }
        }

        builder.setSingleChoiceItems(zoomLabels, checkedItem, (dialog, which) -> {
            setPageZoom(zoomLevels[which]);
            dialog.dismiss();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    public void setPageZoom(int zoomPercent) {
        currentTextZoom = Math.max(25, Math.min(300, zoomPercent));
        TabItem currentTab = getActiveOrDominantTab();
        if (currentTab != null && currentTab.webView != null) {
            currentTab.webView.getSettings().setTextZoom(currentTextZoom);
            Toast.makeText(this, "Zoom: " + currentTextZoom + "%", Toast.LENGTH_SHORT).show();
        }
    }

    public int getPageZoom() {
        return currentTextZoom;
    }

    private void openDownloadsFolder() {
        try {
            Intent intent = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Downloads folder located in device storage", Toast.LENGTH_SHORT).show();
        }
    }

    public void addNewIncognitoTab() {
        int newId = nextTabId++;
        TabItem incognitoTab = createNewTabInstance(newId, "file:///android_asset/incognito_hub.html", "web", null, true);
        incognitoTab.title = "Private Tab";
        tabsList.add(incognitoTab);
        switchToTab(newId);
        Toast.makeText(this, "🕶️ Private Tab Opened (No Cookies / History)", Toast.LENGTH_SHORT).show();
    }

    private void cycleSearchEngine() {
        if (currentSearchEngine == AICommandRouter.SearchEngine.GOOGLE) {
            currentSearchEngine = AICommandRouter.SearchEngine.DUCKDUCKGO;
        } else if (currentSearchEngine == AICommandRouter.SearchEngine.DUCKDUCKGO) {
            currentSearchEngine = AICommandRouter.SearchEngine.BRAVE;
        } else if (currentSearchEngine == AICommandRouter.SearchEngine.BRAVE) {
            currentSearchEngine = AICommandRouter.SearchEngine.BING;
        } else {
            currentSearchEngine = AICommandRouter.SearchEngine.GOOGLE;
        }
        Toast.makeText(this, "Search Engine: " + currentSearchEngine.name, Toast.LENGTH_SHORT).show();
    }

    public void setDefaultSearchEngine(String name) {
        for (AICommandRouter.SearchEngine engine : AICommandRouter.SearchEngine.values()) {
            if (engine.name.equalsIgnoreCase(name)) {
                currentSearchEngine = engine;
                break;
            }
        }
    }

    private void clearBrowserData() {
        CookieManager.getInstance().removeAllCookies(null);
        CookieManager.getInstance().flush();
        for (TabItem tab : tabsList) {
            tab.webView.clearCache(true);
            tab.webView.clearHistory();
        }
        Toast.makeText(this, "Browser Cache & Cookies Cleared", Toast.LENGTH_SHORT).show();
    }

    public void startDebugRecording() {
        isDebugRecording = true;
        debugLogBuffer.setLength(0);
        debugLogBuffer.append("=== CASPIAN FLOW DIAGNOSTIC LOG ===\n");
        debugLogBuffer.append("Started: ").append(new Date().toString()).append("\n\n");
        Toast.makeText(this, "🔴 Diagnostic Recording Started", Toast.LENGTH_SHORT).show();
    }

    public void stopAndSaveDebugLog() {
        if (!isDebugRecording) return;
        isDebugRecording = false;
        try {
            File exportDir = new File(getExternalFilesDir(null), "Logs");
            if (!exportDir.exists()) exportDir.mkdirs();
            File file = new File(exportDir, "Caspian_Log_" + System.currentTimeMillis() + ".txt");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(debugLogBuffer.toString().getBytes(StandardCharsets.UTF_8));
            fos.close();
            Toast.makeText(this, "Log Saved: " + file.getName(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to save log: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isDebugRecordingActive() {
        return isDebugRecording;
    }

    public void toggleSplitView() {
        cycleSplitViewMode();
    }

    public void cycleSplitViewMode() {
        splitModeState = (splitModeState + 1) % 3;
        if (splitModeState != 0) {
            if (tabsList.size() < 2) {
                TabItem secondTab = createNewTabInstance(nextTabId++, "https://gemini.google.com/app", "gemini", null, false);
                tabsList.add(secondTab);
                secondarySplitTabId = secondTab.id;
            } else {
                for (TabItem tab : tabsList) {
                    if (tab.id != activeTabId) {
                        secondarySplitTabId = tab.id;
                        break;
                    }
                }
            }
            applySplitViewLayout();
            Toast.makeText(this, splitModeState == 1 ? "🔀 Horizontal Split Active" : "🔀 Vertical Split Active", Toast.LENGTH_SHORT).show();
        } else {
            exitSplitView();
        }
    }

    public void checkAndFocusSplitPane(boolean isLeft) {
        if (splitModeState == 0) return;
        if (Math.abs(splitRatio - 0.5f) < 0.05f) return;

        boolean needFlip = false;
        if (isLeft && splitRatio < 0.5f) {
            splitRatio = 1.0f - splitRatio;
            needFlip = true;
        } else if (!isLeft && splitRatio > 0.5f) {
            splitRatio = 1.0f - splitRatio;
            needFlip = true;
        }

        if (needFlip) {
            LinearLayout.LayoutParams leftLp = (LinearLayout.LayoutParams) splitLeftContainer.getLayoutParams();
            LinearLayout.LayoutParams rightLp = (LinearLayout.LayoutParams) splitRightContainer.getLayoutParams();
            leftLp.weight = splitRatio;
            rightLp.weight = 1.0f - splitRatio;
            splitLeftContainer.setLayoutParams(leftLp);
            splitRightContainer.setLayoutParams(rightLp);

            if (splitLeftTapMask != null && splitRightTapMask != null) {
                if (Math.abs(splitRatio - 0.5f) < 0.05f) {
                    splitLeftTapMask.setVisibility(View.GONE);
                    splitRightTapMask.setVisibility(View.GONE);
                } else if (splitRatio > 0.5f) {
                    splitLeftTapMask.setVisibility(View.GONE);
                    splitRightTapMask.setVisibility(View.VISIBLE);
                } else {
                    splitLeftTapMask.setVisibility(View.VISIBLE);
                    splitRightTapMask.setVisibility(View.GONE);
                }
            }

            splitViewContainer.requestLayout();
            updateOmniboxState();
            saveOpenTabsState();
        }
    }

    public void applySplitViewLayout() {
        webViewContainer.setVisibility(View.GONE);
        splitViewContainer.setVisibility(View.VISIBLE);

        TabItem leftTab = getTabById(activeTabId);
        TabItem rightTab = getTabById(secondarySplitTabId);

        splitLeftContainer.removeAllViews();
        splitRightContainer.removeAllViews();

        if (leftTab != null && leftTab.webView.getParent() != null) {
            ((ViewGroup) leftTab.webView.getParent()).removeView(leftTab.webView);
        }
        if (rightTab != null && rightTab.webView.getParent() != null) {
            ((ViewGroup) rightTab.webView.getParent()).removeView(rightTab.webView);
        }

        if (splitModeState == 1) {
            splitViewContainer.setOrientation(LinearLayout.HORIZONTAL);
            splitDivider.setLayoutParams(new LinearLayout.LayoutParams((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()), ViewGroup.LayoutParams.MATCH_PARENT));
            if (splitDividerHandle != null) {
                FrameLayout.LayoutParams hl = new FrameLayout.LayoutParams((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 44, getResources().getDisplayMetrics()), Gravity.CENTER);
                splitDividerHandle.setLayoutParams(hl);
            }
            splitLeftContainer.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, splitRatio));
            splitRightContainer.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f - splitRatio));
        } else {
            splitViewContainer.setOrientation(LinearLayout.VERTICAL);
            splitDivider.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics())));
            if (splitDividerHandle != null) {
                FrameLayout.LayoutParams hl = new FrameLayout.LayoutParams((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 44, getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, getResources().getDisplayMetrics()), Gravity.CENTER);
                splitDividerHandle.setLayoutParams(hl);
            }
            splitLeftContainer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, splitRatio));
            splitRightContainer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f - splitRatio));
        }

        if (leftTab != null) splitLeftContainer.addView(leftTab.webView);
        if (rightTab != null) splitRightContainer.addView(rightTab.webView);

        if (splitLeftTapMask != null) {
            if (splitLeftTapMask.getParent() != null) ((ViewGroup) splitLeftTapMask.getParent()).removeView(splitLeftTapMask);
            splitLeftContainer.addView(splitLeftTapMask);
            splitLeftTapMask.bringToFront();
        }
        if (splitRightTapMask != null) {
            if (splitRightTapMask.getParent() != null) ((ViewGroup) splitRightTapMask.getParent()).removeView(splitRightTapMask);
            splitRightContainer.addView(splitRightTapMask);
            splitRightTapMask.bringToFront();
        }

        splitLeftContainer.addView(splitLeftControl);
        splitRightContainer.addView(splitRightControl);
        splitLeftControl.bringToFront();
        splitRightControl.bringToFront();

        if (splitLeftTapMask != null && splitRightTapMask != null) {
            if (Math.abs(splitRatio - 0.5f) < 0.05f) {
                splitLeftTapMask.setVisibility(View.GONE);
                splitRightTapMask.setVisibility(View.GONE);
            } else if (splitRatio > 0.5f) {
                splitLeftTapMask.setVisibility(View.GONE);
                splitRightTapMask.setVisibility(View.VISIBLE);
            } else {
                splitLeftTapMask.setVisibility(View.VISIBLE);
                splitRightTapMask.setVisibility(View.GONE);
            }
        }

        updateOmniboxState();
        saveOpenTabsState();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupSplitDividerDrag() {
        if (splitDivider == null) return;

        splitDivider.setOnTouchListener(new View.OnTouchListener() {
            private float downX, downY;
            private boolean isDraggingDivider = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        downX = event.getRawX();
                        downY = event.getRawY();
                        isDraggingDivider = false;
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float dx = Math.abs(event.getRawX() - downX);
                        float dy = Math.abs(event.getRawY() - downY);
                        if (dx > 10 || dy > 10) {
                            isDraggingDivider = true;
                        }
                        if (isDraggingDivider) {
                            if (splitModeState == 1) {
                                float totalWidth = splitViewContainer.getWidth();
                                if (totalWidth > 0) {
                                    float newRatio = event.getRawX() / totalWidth;
                                    newRatio = Math.max(0.15f, Math.min(0.85f, newRatio));
                                    splitRatio = newRatio;
                                    LinearLayout.LayoutParams leftLp = (LinearLayout.LayoutParams) splitLeftContainer.getLayoutParams();
                                    LinearLayout.LayoutParams rightLp = (LinearLayout.LayoutParams) splitRightContainer.getLayoutParams();
                                    leftLp.weight = splitRatio;
                                    rightLp.weight = 1.0f - splitRatio;
                                    splitLeftContainer.setLayoutParams(leftLp);
                                    splitRightContainer.setLayoutParams(rightLp);
                                    splitViewContainer.requestLayout();
                                }
                            } else if (splitModeState == 2) {
                                float totalHeight = splitViewContainer.getHeight();
                                int[] loc = new int[2];
                                splitViewContainer.getLocationOnScreen(loc);
                                float relativeY = event.getRawY() - loc[1];
                                if (totalHeight > 0) {
                                    float newRatio = relativeY / totalHeight;
                                    newRatio = Math.max(0.15f, Math.min(0.85f, newRatio));
                                    splitRatio = newRatio;
                                    LinearLayout.LayoutParams leftLp = (LinearLayout.LayoutParams) splitLeftContainer.getLayoutParams();
                                    LinearLayout.LayoutParams rightLp = (LinearLayout.LayoutParams) splitRightContainer.getLayoutParams();
                                    leftLp.weight = splitRatio;
                                    rightLp.weight = 1.0f - splitRatio;
                                    splitLeftContainer.setLayoutParams(leftLp);
                                    splitRightContainer.setLayoutParams(rightLp);
                                    splitViewContainer.requestLayout();
                                }
                            }
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                        if (!isDraggingDivider) {
                            playUiFeedbackSound("tap");
                            toggleSplitArenaBroadcast(splitArenaBroadcastContainer != null && splitArenaBroadcastContainer.getVisibility() != View.VISIBLE);
                        } else {
                            if (splitLeftTapMask != null && splitRightTapMask != null) {
                                if (Math.abs(splitRatio - 0.5f) < 0.05f) {
                                    splitLeftTapMask.setVisibility(View.GONE);
                                    splitRightTapMask.setVisibility(View.GONE);
                                } else if (splitRatio > 0.5f) {
                                    splitLeftTapMask.setVisibility(View.GONE);
                                    splitRightTapMask.setVisibility(View.VISIBLE);
                                } else {
                                    splitLeftTapMask.setVisibility(View.VISIBLE);
                                    splitRightTapMask.setVisibility(View.GONE);
                                }
                            }
                            updateOmniboxState();
                            saveOpenTabsState();
                        }
                        return true;

                    case MotionEvent.ACTION_CANCEL:
                        saveOpenTabsState();
                        return true;
                }
                return false;
            }
        });
    }

    public void toggleSplitArenaBroadcast(boolean show) {
        if (splitArenaBroadcastContainer == null) return;
        if (show) {
            splitArenaBroadcastContainer.setVisibility(View.VISIBLE);
            if (splitArenaInput != null) {
                splitArenaInput.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) imm.showSoftInput(splitArenaInput, InputMethodManager.SHOW_IMPLICIT);
            }
        } else {
            splitArenaBroadcastContainer.setVisibility(View.GONE);
            hideKeyboard();
        }
    }

    public void broadcastPromptToDualAI(String prompt) {
        if (prompt == null || prompt.trim().isEmpty()) return;
        playUiFeedbackSound("tap");

        TabItem leftTab = getTabById(activeTabId);
        TabItem rightTab = getTabById(secondarySplitTabId);

        boolean isLeftGpt = leftTab != null && (leftTab.url.contains("chatgpt.com") || "chatgpt".equalsIgnoreCase(leftTab.service));
        boolean isRightGemini = rightTab != null && (rightTab.url.contains("gemini.google.com") || "gemini".equalsIgnoreCase(rightTab.service));

        boolean isLeftGemini = leftTab != null && (leftTab.url.contains("gemini.google.com") || "gemini".equalsIgnoreCase(leftTab.service));
        boolean isRightGpt = rightTab != null && (rightTab.url.contains("chatgpt.com") || "chatgpt".equalsIgnoreCase(rightTab.service));

        boolean alreadyDualArena = (isLeftGpt && isRightGemini) || (isLeftGemini && isRightGpt);

        if (!alreadyDualArena || splitModeState == 0) {
            TabItem gptTab = null;
            TabItem geminiTab = null;
            for (TabItem t : tabsList) {
                if (gptTab == null && (t.url.contains("chatgpt.com") || "chatgpt".equalsIgnoreCase(t.service))) gptTab = t;
                if (geminiTab == null && (t.url.contains("gemini.google.com") || "gemini".equalsIgnoreCase(t.service))) geminiTab = t;
            }

            if (gptTab == null) {
                int id = nextTabId++;
                gptTab = createNewTabInstance(id, "https://chatgpt.com", "chatgpt", prompt, false);
                gptTab.title = "ChatGPT";
                tabsList.add(gptTab);
            } else {
                gptTab.pendingPrompt = prompt;
            }

            if (geminiTab == null) {
                int id = nextTabId++;
                geminiTab = createNewTabInstance(id, "https://gemini.google.com/app", "gemini", prompt, false);
                geminiTab.title = "Gemini";
                tabsList.add(geminiTab);
            } else {
                geminiTab.pendingPrompt = prompt;
            }

            activeTabId = gptTab.id;
            secondarySplitTabId = geminiTab.id;
            splitModeState = 1;
            splitRatio = 0.5f;
            applySplitViewLayout();
            Toast.makeText(this, "⚡ Dual AI Ask: ChatGPT & Gemini Ready!", Toast.LENGTH_SHORT).show();
        }

        TabItem curLeft = getTabById(activeTabId);
        TabItem curRight = getTabById(secondarySplitTabId);

        if (curLeft != null && curLeft.webView != null) {
            injectAndSubmitAIPrompt(curLeft.webView, prompt);
        }
        if (curRight != null && curRight.webView != null) {
            injectAndSubmitAIPrompt(curRight.webView, prompt);
        }

        if (splitArenaInput != null) splitArenaInput.setText("");
        Toast.makeText(this, "⚔️ Broadcasted message to ChatGPT & Gemini!", Toast.LENGTH_SHORT).show();
    }

    private void injectAndSubmitAIPrompt(WebView webView, String prompt) {
        if (webView == null || prompt == null || prompt.trim().isEmpty()) return;
        String js = "(function() {\n" +
                "  var txt = " + JSONObject.quote(prompt) + ";\n" +
                "  if (!txt) return;\n" +
                "  var pollCount = 0;\n" +
                "  var maxPolls = 60;\n" +
                "  function dispatchInputEvents(el, text) {\n" +
                "    try {\n" +
                "      el.focus();\n" +
                "      el.dispatchEvent(new Event('focus', { bubbles: true }));\n" +
                "      el.dispatchEvent(new InputEvent('beforeinput', { bubbles: true, cancelable: true, inputType: 'insertText', data: text }));\n" +
                "      el.dispatchEvent(new Event('input', { bubbles: true, composed: true }));\n" +
                "      el.dispatchEvent(new InputEvent('input', { bubbles: true, composed: true, inputType: 'insertText', data: text }));\n" +
                "      el.dispatchEvent(new Event('change', { bubbles: true, composed: true }));\n" +
                "      el.dispatchEvent(new KeyboardEvent('keydown', { bubbles: true, key: ' ', code: 'Space' }));\n" +
                "      el.dispatchEvent(new KeyboardEvent('keyup', { bubbles: true, key: ' ', code: 'Space' }));\n" +
                "    } catch(e) {}\n" +
                "  }\n" +
                "  function setElementText(el, text) {\n" +
                "    el.focus();\n" +
                "    if (el.tagName === 'TEXTAREA' || el.tagName === 'INPUT') {\n" +
                "      try {\n" +
                "        var proto = el.tagName === 'TEXTAREA' ? window.HTMLTextAreaElement.prototype : window.HTMLInputElement.prototype;\n" +
                "        var setter = Object.getOwnPropertyDescriptor(proto, 'value').set;\n" +
                "        setter.call(el, text);\n" +
                "      } catch(e) {\n" +
                "        el.value = text;\n" +
                "      }\n" +
                "      dispatchInputEvents(el, text);\n" +
                "    } else {\n" +
                "      try {\n" +
                "        var sel = window.getSelection();\n" +
                "        var range = document.createRange();\n" +
                "        range.selectNodeContents(el);\n" +
                "        sel.removeAllRanges();\n" +
                "        sel.addRange(range);\n" +
                "        document.execCommand('delete', false, null);\n" +
                "        document.execCommand('insertText', false, text);\n" +
                "      } catch(e) {}\n" +
                "      if (!el.innerText || !el.innerText.includes(text)) {\n" +
                "        var p = el.querySelector('p');\n" +
                "        if (!p) {\n" +
                "          p = document.createElement('p');\n" +
                "          el.innerHTML = '';\n" +
                "          el.appendChild(p);\n" +
                "        }\n" +
                "        p.textContent = text;\n" +
                "      }\n" +
                "      dispatchInputEvents(el, text);\n" +
                "    }\n" +
                "  }\n" +
                "  function findInput() {\n" +
                "    var gpt = document.querySelector('#prompt-textarea') ||\n" +
                "              document.querySelector('div[contenteditable=\"true\"].ProseMirror') ||\n" +
                "              document.querySelector('div#prompt-textarea') ||\n" +
                "              document.querySelector('textarea#prompt-textarea');\n" +
                "    if (gpt) return gpt;\n" +
                "    var gemini = document.querySelector('.ql-editor') ||\n" +
                "                 document.querySelector('rich-textarea div[contenteditable=\"true\"]') ||\n" +
                "                 document.querySelector('div[role=\"textbox\"][contenteditable=\"true\"]') ||\n" +
                "                 document.querySelector('rich-textarea');\n" +
                "    if (gemini) return gemini;\n" +
                "    return document.querySelector('div[contenteditable=\"true\"]') ||\n" +
                "           document.querySelector('textarea[data-id=\"root\"]') ||\n" +
                "           document.querySelector('textarea') ||\n" +
                "           document.querySelector('[role=\"textbox\"]');\n" +
                "  }\n" +
                "  function findSendButton() {\n" +
                "    var gptBtn = document.querySelector('button[data-testid=\"send-button\"]') ||\n" +
                "                 document.querySelector('button[data-testid=\"fruitjuice-send-button\"]') ||\n" +
                "                 document.querySelector('button[aria-label=\"Send prompt\"]') ||\n" +
                "                 document.querySelector('button[aria-label=\"Send message\"]');\n" +
                "    if (gptBtn) return gptBtn;\n" +
                "    var geminiBtn = document.querySelector('button.send-button') ||\n" +
                "                    document.querySelector('button.send-button-container') ||\n" +
                "                    document.querySelector('button[aria-label*=\"Send message\" i]') ||\n" +
                "                    document.querySelector('button[aria-label*=\"Send prompt\" i]') ||\n" +
                "                    document.querySelector('button:has(mat-icon[data-mat-icon-name=\"send\"])') ||\n" +
                "                    document.querySelector('button:has(span[data-mat-icon-name=\"send\"])');\n" +
                "    if (geminiBtn) return geminiBtn;\n" +
                "    return document.querySelector('form button[type=\"submit\"]') ||\n" +
                "           document.querySelector('button[aria-label*=\"send\" i]:not([aria-label*=\"menu\" i])') ||\n" +
                "           document.querySelector('button[aria-label*=\"submit\" i]');\n" +
                "  }\n" +
                "  function triggerSubmitClick(btn, inputEl) {\n" +
                "    if (btn) {\n" +
                "      try {\n" +
                "        btn.dispatchEvent(new MouseEvent('mouseover', { bubbles: true }));\n" +
                "        btn.dispatchEvent(new MouseEvent('mousedown', { bubbles: true }));\n" +
                "        btn.click();\n" +
                "        btn.dispatchEvent(new MouseEvent('mouseup', { bubbles: true }));\n" +
                "      } catch(e) {\n" +
                "        try { btn.click(); } catch(e2) {}\n" +
                "      }\n" +
                "    }\n" +
                "    if (inputEl) {\n" +
                "      try {\n" +
                "        var kd = new KeyboardEvent('keydown', { key: 'Enter', code: 'Enter', keyCode: 13, which: 13, bubbles: true, cancelable: true });\n" +
                "        inputEl.dispatchEvent(kd);\n" +
                "        var kp = new KeyboardEvent('keypress', { key: 'Enter', code: 'Enter', keyCode: 13, which: 13, bubbles: true, cancelable: true });\n" +
                "        inputEl.dispatchEvent(kp);\n" +
                "        var ku = new KeyboardEvent('keyup', { key: 'Enter', code: 'Enter', keyCode: 13, which: 13, bubbles: true, cancelable: true });\n" +
                "        inputEl.dispatchEvent(ku);\n" +
                "      } catch(e) {}\n" +
                "    }\n" +
                "  }\n" +
                "  function pollAndSubmit() {\n" +
                "    pollCount++;\n" +
                "    var input = findInput();\n" +
                "    if (!input) {\n" +
                "      if (pollCount < maxPolls) setTimeout(pollAndSubmit, 250);\n" +
                "      return;\n" +
                "    }\n" +
                "    setElementText(input, txt);\n" +
                "    var submitTries = 0;\n" +
                "    var submitted = false;\n" +
                "    function trySend() {\n" +
                "      if (submitted) return;\n" +
                "      submitTries++;\n" +
                "      if (!input.innerText && !input.value) setElementText(input, txt);\n" +
                "      var btn = findSendButton();\n" +
                "      var isDisabled = btn && (btn.disabled || btn.getAttribute('aria-disabled') === 'true' || btn.classList.contains('disabled'));\n" +
                "      if (btn && !isDisabled) {\n" +
                "        triggerSubmitClick(btn, input);\n" +
                "        submitted = true;\n" +
                "        return;\n" +
                "      }\n" +
                "      if (submitTries >= 3) {\n" +
                "        triggerSubmitClick(null, input);\n" +
                "      }\n" +
                "      if (!submitted && submitTries < 20) {\n" +
                "        setTimeout(trySend, 250);\n" +
                "      }\n" +
                "    }\n" +
                "    setTimeout(trySend, 200);\n" +
                "  }\n" +
                "  pollAndSubmit();\n" +
                "})();";
        webView.evaluateJavascript(js, null);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupDraggableSplitControl(View controlView) {
        if (controlView == null) return;
        controlView.setOnTouchListener(new View.OnTouchListener() {
            private float dX, dY;
            private float startX, startY;
            private boolean isDragging = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        dX = v.getX() - event.getRawX();
                        dY = v.getY() - event.getRawY();
                        startX = event.getRawX();
                        startY = event.getRawY();
                        isDragging = false;
                        return false;

                    case MotionEvent.ACTION_MOVE:
                        float distance = (float) Math.hypot(event.getRawX() - startX, event.getRawY() - startY);
                        if (distance > 10) {
                            isDragging = true;
                            v.animate().x(event.getRawX() + dX).y(event.getRawY() + dY).setDuration(0).start();
                            return true;
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        if (isDragging) return true;
                        break;
                }
                return false;
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupSplitFloatingControls() {
        if (splitLeftMenuBtn != null) {
            splitLeftMenuBtn.setOnClickListener(v -> showSplitPaneMenu(v, true));
        }
        if (splitRightMenuBtn != null) {
            splitRightMenuBtn.setOnClickListener(v -> showSplitPaneMenu(v, false));
        }

        if (splitLeftCloseBtn != null) {
            splitLeftCloseBtn.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                closeSplitPane(true);
            });
        }
        if (splitRightCloseBtn != null) {
            splitRightCloseBtn.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                closeSplitPane(false);
            });
        }

        if (splitLeftTapMask != null) {
            splitLeftTapMask.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                checkAndFocusSplitPane(true);
            });
        }

        if (splitRightTapMask != null) {
            splitRightTapMask.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                checkAndFocusSplitPane(false);
            });
        }

        if (splitArenaSendBtn != null) {
            splitArenaSendBtn.setOnClickListener(v -> {
                if (splitArenaInput != null) {
                    broadcastPromptToDualAI(splitArenaInput.getText().toString().trim());
                }
            });
        }

        if (splitArenaCloseBtn != null) {
            splitArenaCloseBtn.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                toggleSplitArenaBroadcast(false);
            });
        }

        if (splitArenaInput != null) {
            splitArenaInput.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEND || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                    broadcastPromptToDualAI(splitArenaInput.getText().toString().trim());
                    return true;
                }
                return false;
            });
        }

        setupDraggableSplitControl(splitLeftControl);
        setupDraggableSplitControl(splitRightControl);
    }

    private void showSplitPaneMenu(View anchor, boolean isLeftPane) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add(0, 1, 0, "↗️ Open this webpage in a new tab");
        popup.getMenu().add(0, 2, 1, "🔄 Switch to another open tab...");
        popup.getMenu().add(0, 3, 2, "⎘ Separate two tabs");
        popup.getMenu().add(0, 4, 3, (splitModeState == 1 ? "⇄ Switch left and right tabs" : "⇄ Switch top and bottom tabs"));
        popup.getMenu().add(0, 5, 4, (splitModeState == 1 ? "▤ Switch to vertical" : "▤ Switch to horizontal"));
        popup.getMenu().add(0, 6, 5, (openLeftLinksToRight ? "🔗 Open left links to the right (ON)" : "🔗 Open left links to the right (OFF)"));

        popup.setOnMenuItemClickListener(item -> {
            TabItem targetTab = getTabById(isLeftPane ? activeTabId : secondarySplitTabId);
            switch (item.getItemId()) {
                case 1:
                    if (targetTab != null) addNewTab(targetTab.service, "", targetTab.url, targetTab.isIncognito);
                    return true;
                case 2:
                    showSwitchSplitTabDialog(isLeftPane);
                    return true;
                case 3:
                    exitSplitView();
                    return true;
                case 4:
                    swapSplitTabs();
                    return true;
                case 5:
                    toggleSplitOrientation();
                    return true;
                case 6:
                    openLeftLinksToRight = !openLeftLinksToRight;
                    Toast.makeText(this, "Open left links to right: " + (openLeftLinksToRight ? "ENABLED" : "DISABLED"), Toast.LENGTH_SHORT).show();
                    return true;
            }
            return false;
        });
        popup.show();
    }

    private void showSwitchSplitTabDialog(boolean isLeftPane) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🔄 Switch Tab in " + (isLeftPane ? "Left" : "Right") + " Pane");

        List<String> titles = new ArrayList<>();
        List<Integer> tabIds = new ArrayList<>();

        for (TabItem tab : tabsList) {
            titles.add((tab.nickname != null && !tab.nickname.isEmpty() ? tab.nickname : tab.title) + " (" + cleanDisplayUrl(tab.url) + ")");
            tabIds.add(tab.id);
        }

        builder.setItems(titles.toArray(new String[0]), (dialog, which) -> {
            int selectedId = tabIds.get(which);
            if (isLeftPane) {
                activeTabId = selectedId;
            } else {
                secondarySplitTabId = selectedId;
            }
            applySplitViewLayout();
            Toast.makeText(this, "Switched pane tab", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    public void openInOtherSplitPane(String url) {
        if (splitModeState != 0 && secondarySplitTabId != -1) {
            TabItem rightTab = getTabById(secondarySplitTabId);
            if (rightTab != null && rightTab.webView != null) {
                rightTab.webView.loadUrl(url);
                Toast.makeText(this, "🔗 Opened link in split pane", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void swapSplitTabs() {
        int temp = activeTabId;
        activeTabId = secondarySplitTabId;
        secondarySplitTabId = temp;
        applySplitViewLayout();
        Toast.makeText(this, "Tabs Swapped", Toast.LENGTH_SHORT).show();
    }

    private void toggleSplitOrientation() {
        splitModeState = (splitModeState == 1) ? 2 : 1;
        applySplitViewLayout();
        Toast.makeText(this, splitModeState == 1 ? "Horizontal Side-by-Side" : "Vertical Top-and-Bottom", Toast.LENGTH_SHORT).show();
    }

    private void closeSplitPane(boolean isLeftPane) {
        if (isLeftPane && secondarySplitTabId != -1) {
            activeTabId = secondarySplitTabId;
        }
        exitSplitView();
    }

    private void exitSplitView() {
        splitModeState = 0;
        if (splitArenaBroadcastContainer != null) splitArenaBroadcastContainer.setVisibility(View.GONE);
        if (splitLeftContainer != null) splitLeftContainer.removeAllViews();
        if (splitRightContainer != null) splitRightContainer.removeAllViews();
        if (splitViewContainer != null) splitViewContainer.setVisibility(View.GONE);
        if (webViewContainer != null) {
            webViewContainer.removeAllViews();
            webViewContainer.setVisibility(View.VISIBLE);
        }

        TabItem activeTab = getTabById(activeTabId);
        if (activeTab != null && activeTab.webView != null) {
            if (activeTab.webView.getParent() != null) {
                ((ViewGroup) activeTab.webView.getParent()).removeView(activeTab.webView);
            }
            if (webViewContainer != null) webViewContainer.addView(activeTab.webView);
        }
        updateOmniboxState();
    }

    public void startVoiceRecognition() {
        startSpeechToText();
    }

    public void startSpeechToText() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isDriftEnabled = !"false".equalsIgnoreCase(prefs.getString("caspian_current_enabled", "true"));
        if (!isDriftEnabled) {
            isRecordingSpeechMode = false;
            Toast.makeText(this, "⚠️ Caspian Drift Engine is OFF. Enable it in Caspian Engines tab.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, MIC_PERMISSION_REQUEST_CODE);
            return;
        }

        String sttEngine = prefs.getString("stt_engine_mode", "deepgram");

        try {
            isRecordingSpeechMode = true;
            if (omniboxVoiceBtn != null) omniboxVoiceBtn.setColorFilter(0xFFFF3366);
            if (speechWaveformContainer != null) speechWaveformContainer.setVisibility(View.VISIBLE);
            if (speechWaveformView != null) {
                int startColor = Color.parseColor(podStartColor);
                int endColor = Color.parseColor(podEndColor);
                speechWaveformView.setWaveColors(startColor, endColor);
                speechWaveformView.setVisibility(View.VISIBLE);
            }

            if ("android_native".equalsIgnoreCase(sttEngine)) {
                nativeSpeechBuffer.setLength(0);
                setupNativeSpeechRecognizer();
                if (speechRecognizer != null && speechIntent != null) {
                    speechRecognizer.startListening(speechIntent);
                }
                Toast.makeText(this, "🎙️ Listening... Tap Mic / Action Button to Finish", Toast.LENGTH_SHORT).show();
            } else {
                startAudioRecording();
            }
        } catch (Exception e) {
            Log.e(TAG, "startSpeechToText error: " + e.getMessage());
        }
    }

    public void stopSpeechToText() {
        isRecordingSpeechMode = false;
        if (omniboxVoiceBtn != null) omniboxVoiceBtn.setColorFilter(Color.parseColor(podStartColor));
        if (speechWaveformContainer != null) speechWaveformContainer.setVisibility(View.GONE);
        if (speechWaveformView != null) speechWaveformView.setVisibility(View.GONE);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String sttEngine = prefs.getString("stt_engine_mode", "deepgram");

        if ("android_native".equalsIgnoreCase(sttEngine)) {
            if (speechRecognizer != null) {
                try { speechRecognizer.stopListening(); } catch (Exception ignored) {}
            }
            String totalSpoken = nativeSpeechBuffer.toString().trim();
            nativeSpeechBuffer.setLength(0);
            if (!totalSpoken.isEmpty()) {
                if (isUniversalVoiceActive) {
                    handleUniversalSpeechText(totalSpoken);
                } else {
                    omniboxEditText.setText(totalSpoken);
                    handleOmniboxSubmission(totalSpoken);
                }
            }
        } else {
            byte[] wavBytes = stopAudioRecordingAndGetWav();
            sendAudioToCloudStt(wavBytes, sttEngine, prefs);
        }
    }

    private void startAudioRecording() {
        try {
            int sampleRate = 16000;
            int channelConfig = AudioFormat.CHANNEL_IN_MONO;
            int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
            int bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
            if (bufferSize <= 0) bufferSize = 4096;
            final int finalBufSize = bufferSize;

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) return;
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConfig, audioFormat, finalBufSize);
            pcmAudioBuffer = new ByteArrayOutputStream();
            isRecordingPcmAudio = true;

            audioRecord.startRecording();
            pcmRecordingThread = new Thread(() -> {
                byte[] data = new byte[finalBufSize];
                while (isRecordingPcmAudio) {
                    int read = audioRecord.read(data, 0, data.length);
                    if (read > 0 && pcmAudioBuffer != null) {
                        pcmAudioBuffer.write(data, 0, read);

                        long sum = 0;
                        int samples = read / 2;
                        for (int i = 0; i < read - 1; i += 2) {
                            short sample = (short) ((data[i + 1] << 8) | (data[i] & 0xff));
                            sum += sample * sample;
                        }
                        double rms = Math.sqrt((double) sum / Math.max(1, samples));
                        final float rmsdB = (float) Math.max(-2.0, Math.min(10.0, (rms / 350.0) - 2.0));
                        if (speechWaveformView != null && speechWaveformView.getVisibility() == View.VISIBLE) {
                            runOnUiThread(() -> speechWaveformView.setAmplitude(rmsdB));
                        }
                    }
                }
            });
            pcmRecordingThread.start();
            Toast.makeText(this, "🎙️ Recording... Tap Action Button to Finish", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "startAudioRecording error: " + e.getMessage());
        }
    }

    private byte[] stopAudioRecordingAndGetWav() {
        isRecordingPcmAudio = false;
        if (audioRecord != null) {
            try {
                audioRecord.stop();
                audioRecord.release();
            } catch (Exception ignored) {}
            audioRecord = null;
        }
        if (pcmRecordingThread != null) {
            try { pcmRecordingThread.join(500); } catch (Exception ignored) {}
            pcmRecordingThread = null;
        }
        byte[] pcmData = pcmAudioBuffer != null ? pcmAudioBuffer.toByteArray() : new byte[0];
        return pcmToWav(pcmData, 16000, 1, 16);
    }

    private byte[] pcmToWav(byte[] pcm, int sampleRate, int channels, int bitsPerSample) {
        int totalDataLen = pcm.length + 36;
        int byteRate = sampleRate * channels * bitsPerSample / 8;
        byte[] header = new byte[44];
        header[0] = 'R'; header[1] = 'I'; header[2] = 'F'; header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W'; header[9] = 'A'; header[10] = 'V'; header[11] = 'E';
        header[12] = 'f'; header[13] = 'm'; header[14] = 't'; header[15] = ' ';
        header[16] = 16; header[17] = 0; header[18] = 0; header[19] = 0;
        header[20] = 1; header[21] = 0;
        header[22] = (byte) channels; header[23] = 0;
        header[24] = (byte) (sampleRate & 0xff);
        header[25] = (byte) ((sampleRate >> 8) & 0xff);
        header[26] = (byte) ((sampleRate >> 16) & 0xff);
        header[27] = (byte) ((sampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (channels * bitsPerSample / 8); header[33] = 0;
        header[34] = (byte) bitsPerSample; header[35] = 0;
        header[36] = 'd'; header[37] = 'a'; header[38] = 't'; header[39] = 'a';
        header[40] = (byte) (pcm.length & 0xff);
        header[41] = (byte) ((pcm.length >> 8) & 0xff);
        header[42] = (byte) ((pcm.length >> 16) & 0xff);
        header[43] = (byte) ((pcm.length >> 24) & 0xff);

        byte[] wav = new byte[header.length + pcm.length];
        System.arraycopy(header, 0, wav, 0, header.length);
        System.arraycopy(pcm, 0, wav, header.length, pcm.length);
        return wav;
    }

    private void sendAudioToCloudStt(byte[] wavBytes, String engineMode, SharedPreferences prefs) {
        if (wavBytes == null || wavBytes.length <= 44) {
            Toast.makeText(this, "⚠️ Audio buffer empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                String recognizedText = null;
                if ("deepgram".equalsIgnoreCase(engineMode)) {
                    String apiKey = prefs.getString("deepgram_api_key", "").trim();
                    if (apiKey.isEmpty()) {
                        apiKey = prefs.getString("deepgram_key", "").trim();
                    }
                    apiKey = apiKey.replace("\"", "").replace("'", "").trim();
                    if (apiKey.isEmpty()) {
                        runOnUiThread(() -> Toast.makeText(this, "⚠️ Deepgram API Key missing! Enter in Caspian Menu Settings.", Toast.LENGTH_LONG).show());
                        return;
                    }
                    recognizedText = queryDeepgramApi(wavBytes, apiKey);
                    if (recognizedText != null) {
                        int pcmLen = wavBytes.length - 44;
                        int durationSec = Math.max(1, pcmLen / (16000 * 2));
                        long newTotal = prefs.getLong("deepgram_used_seconds", 0L) + durationSec;
                        prefs.edit().putLong("deepgram_used_seconds", newTotal).apply();

                        final long finalTotalSec = newTotal;
                        runOnUiThread(() -> {
                            if (controlWebView != null) {
                                controlWebView.evaluateJavascript("if(typeof window.updateDeepgramUsageBadge === 'function') { window.updateDeepgramUsageBadge(" + finalTotalSec + "); }", null);
                            }
                        });
                    }
                } else if ("huggingface".equalsIgnoreCase(engineMode)) {
                    String apiKey = prefs.getString("huggingface_api_key", "").trim();
                    apiKey = apiKey.replace("\"", "").replace("'", "").trim();
                    if (apiKey.isEmpty()) {
                        runOnUiThread(() -> Toast.makeText(this, "⚠️ Hugging Face Token missing! Enter in Settings.", Toast.LENGTH_LONG).show());
                        return;
                    }
                    recognizedText = queryHuggingFaceApi(wavBytes, apiKey);
                }

                final String finalText = recognizedText;
                if (finalText != null && !finalText.trim().isEmpty()) {
                    runOnUiThread(() -> {
                        if (isUniversalVoiceActive) {
                            handleUniversalSpeechText(finalText);
                        } else {
                            omniboxEditText.setText(finalText);
                            handleOmniboxSubmission(finalText);
                        }
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "⚠️ Speech not recognized. Speak louder.", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "⚠️ STT: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private String readStreamString(InputStream is) throws Exception {
        if (is == null) return "";
        BufferedReader r = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder total = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) total.append(line).append('\n');
        return total.toString();
    }

    private String queryDeepgramApi(byte[] wavBytes, String apiKey) throws Exception {
        apiKey = apiKey.trim().replace("\"", "").replace("'", "");
        if (apiKey.isEmpty()) {
            throw new Exception("Deepgram API Key is missing. Enter it in Caspian Settings.");
        }

        URL url = new URL("https://api.deepgram.com/v1/listen?model=nova-2&smart_format=true");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Token " + apiKey);
        conn.setRequestProperty("Content-Type", "audio/wav");
        conn.setConnectTimeout(12000);
        conn.setReadTimeout(18000);
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(wavBytes);
        }

        int code = conn.getResponseCode();
        InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
        String resp = readStreamString(is);
        
        JSONObject json = new JSONObject(resp);
        if (code < 200 || code >= 300 || !json.has("results")) {
            String errMsg = json.optString("err_msg", json.optString("error", json.optString("message", "")));
            if (errMsg.isEmpty()) errMsg = "HTTP " + code + " - " + resp;
            throw new Exception(errMsg);
        }

        return json.getJSONObject("results")
                .getJSONArray("channels")
                .getJSONObject(0)
                .getJSONArray("alternatives")
                .getJSONObject(0)
                .getString("transcript");
    }

    private String queryHuggingFaceApi(byte[] wavBytes, String apiKey) throws Exception {
        apiKey = apiKey.trim().replace("\"", "").replace("'", "");
        URL url = new URL("https://router.huggingface.co/hf-inference/models/openai/whisper-large-v3");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setRequestProperty("Content-Type", "audio/wav");
        conn.setConnectTimeout(12000);
        conn.setReadTimeout(20000);
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(wavBytes);
        }

        int code = conn.getResponseCode();
        InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
        String resp = readStreamString(is);
        JSONObject json = new JSONObject(resp);
        return json.optString("text", "");
    }

    private void setupNativeSpeechRecognizer() {
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            speechIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);

            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {}

                @Override
                public void onBeginningOfSpeech() {}

                @Override
                public void onRmsChanged(float rmsdB) {
                    if (speechWaveformView != null) speechWaveformView.setAmplitude(rmsdB);
                }

                @Override
                public void onBufferReceived(byte[] buffer) {}

                @Override
                public void onEndOfSpeech() {
                    if (isRecordingSpeechMode && speechRecognizer != null && speechIntent != null) {
                        try {
                            speechRecognizer.startListening(speechIntent);
                        } catch (Exception ignored) {}
                    }
                }

                @Override
                public void onError(int error) {
                    if (isRecordingSpeechMode && speechRecognizer != null && speechIntent != null) {
                        try {
                            speechRecognizer.startListening(speechIntent);
                        } catch (Exception ignored) {}
                    }
                }

                @Override
                public void onResults(Bundle results) {
                    ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (matches != null && !matches.isEmpty()) {
                        String spoken = matches.get(0);
                        if (nativeSpeechBuffer.length() > 0) nativeSpeechBuffer.append(" ");
                        nativeSpeechBuffer.append(spoken);
                    }
                    if (isRecordingSpeechMode && speechRecognizer != null && speechIntent != null) {
                        try {
                            speechRecognizer.startListening(speechIntent);
                        } catch (Exception ignored) {}
                    }
                }

                @Override
                public void onPartialResults(Bundle partialResults) {
                    ArrayList<String> matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (matches != null && !matches.isEmpty() && !isUniversalVoiceActive) {
                        omniboxEditText.setText(matches.get(0));
                    }
                }

                @Override
                public void onEvent(int eventType, Bundle params) {}
            });
        }
    }

    private void handleUniversalSpeechText(String text) {
        if (text == null || text.trim().isEmpty()) return;

        // 1. Guaranteed safety net: Always copy transcription to clipboard
        try {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm != null) {
                cm.setPrimaryClip(ClipData.newPlainText("Caspian Dictation", text));
            }
        } catch (Exception ignored) {}

        // 2. Check if a native Android EditText currently has focus (e.g. Dual AI Arena input, Omnibox, Finder)
        View focusedView = getCurrentFocus();
        if (focusedView instanceof EditText) {
            EditText et = (EditText) focusedView;
            int start = Math.max(0, et.getSelectionStart());
            int end = Math.max(0, et.getSelectionEnd());
            Editable editable = et.getText();
            if (editable != null) {
                editable.replace(Math.min(start, end), Math.max(start, end), text, 0, text.length());
                et.setSelection(Math.min(start, end) + text.length());
            } else {
                et.setText(text);
                et.setSelection(text.length());
            }
            Toast.makeText(this, "✨ Speech inserted at cursor & copied to clipboard!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. Otherwise, target the active or dominant WebView
        TabItem currentTab = getActiveOrDominantTab();
        if (currentTab != null && currentTab.webView != null) {
            String cursorJs = "(function() {\n" +
                    "  var txt = " + JSONObject.quote(text) + ";\n" +
                    "  var el = document.activeElement;\n" +
                    "  var inserted = false;\n" +
                    "  if (el && (el.tagName === 'TEXTAREA' || el.tagName === 'INPUT' || el.isContentEditable || el.getAttribute('contenteditable') === 'true')) {\n" +
                    "    if (el.tagName === 'TEXTAREA' || el.tagName === 'INPUT') {\n" +
                    "      var s = el.selectionStart || 0;\n" +
                    "      var e = el.selectionEnd || 0;\n" +
                    "      var v = el.value || '';\n" +
                    "      el.value = v.substring(0, s) + txt + v.substring(e);\n" +
                    "      el.selectionStart = el.selectionEnd = s + txt.length;\n" +
                    "      el.dispatchEvent(new Event('input', { bubbles: true }));\n" +
                    "      inserted = true;\n" +
                    "    } else {\n" +
                    "      try {\n" +
                    "        document.execCommand('insertText', false, txt);\n" +
                    "        inserted = true;\n" +
                    "      } catch(err) {\n" +
                    "        el.innerText += txt;\n" +
                    "        inserted = true;\n" +
                    "      }\n" +
                    "      el.dispatchEvent(new Event('input', { bubbles: true }));\n" +
                    "    }\n" +
                    "  }\n" +
                    "  if (!inserted) {\n" +
                    "    var ta = document.querySelector('#prompt-textarea, rich-textarea .ql-editor, div[contenteditable=\"true\"], textarea, .input-area');\n" +
                    "    if (ta) {\n" +
                    "      ta.focus();\n" +
                    "      try { document.execCommand('insertText', false, txt); } catch(err) { ta.innerText = txt; }\n" +
                    "      ta.dispatchEvent(new Event('input', { bubbles: true }));\n" +
                    "      inserted = true;\n" +
                    "    }\n" +
                    "  }\n" +
                    "  return inserted;\n" +
                    "})();";

            currentTab.webView.evaluateJavascript(cursorJs, value -> {
                boolean wasInserted = "true".equalsIgnoreCase(value);
                if (wasInserted) {
                    Toast.makeText(this, "✨ Speech inserted at cursor & copied to clipboard!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "📋 Dictation copied to clipboard (ready to paste)!", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void setupVoiceVisualizer() {
        try {
            if (speechWaveformContainer != null) {
                speechWaveformView = new SpeechWaveformView(this);
                speechWaveformContainer.addView(speechWaveformView);
            }
        } catch (Exception e) {
            Log.e(TAG, "setupVoiceVisualizer error: " + e.getMessage());
        }
    }

    public void toggleReaderMode() {
        TabItem currentTab = getTabById(activeTabId);
        if (currentTab == null) return;
        currentTab.isReaderMode = !currentTab.isReaderMode;

        if (currentTab.isReaderMode) {
            String readerScript =
                    "(function() {" +
                    "  var article = document.querySelector('article') || document.querySelector('main') || document.body;" +
                    "  var title = document.title || 'Reader Mode';" +
                    "  var content = article ? article.innerText : document.body.innerText;" +
                    "  var readerHtml = '<div style=\"max-width:700px;margin:0 auto;padding:24px;font-family:system-ui,sans-serif;line-height:1.75;color:#E0E6ED;background:#050811;\"><h1 style=\"color:#00E5FF;font-size:26px;\">' + title + '</h1><hr style=\"border-color:#1B4264;margin:20px 0;\"/><div style=\"font-size:16px;white-space:pre-wrap;\">' + content.replace(/</g,'&lt;') + '</div></div>';" +
                    "  document.body.innerHTML = readerHtml;" +
                    "  document.body.style.background = '#050811';" +
                    "})();";
            currentTab.webView.evaluateJavascript(readerScript, null);
            Toast.makeText(this, "Reader Mode Active", Toast.LENGTH_SHORT).show();
        } else {
            currentTab.webView.reload();
        }
    }

    private void showExportOptions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Share & Export");
        String[] options = {"PDF Document (.pdf / Print)", "Markdown File (.md)", "Plain Text Transcript (.txt)", "Word Document (.doc)", "Convert Chat to Another AI"};
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) performExportOnMainWebView("styledpdf");
            else if (which == 1) performExportOnMainWebView("md");
            else if (which == 2) performExportOnMainWebView("txt");
            else if (which == 3) performExportOnMainWebView("doc");
            else if (which == 4) performExportOnMainWebView("convert");
        });
        builder.show();
    }

    public void exportCurrentDocument(String format) {
        performExportOnMainWebView(format);
    }

    public void performExportOnMainWebView(String fmt) {
        TabItem activeTab = getActiveOrDominantTab();
        if (activeTab == null || activeTab.webView == null) return;
        WebView mainWebView = activeTab.webView;

        String targetFormat = fmt;
        if ("nativepdf".equalsIgnoreCase(fmt) || "pdf".equalsIgnoreCase(fmt)) {
            targetFormat = "styledpdf";
        }
        final String exportFmt = targetFormat;

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String service = activeTab.service != null ? activeTab.service : "chatgpt";
        String url = activeTab.webView.getUrl() != null ? activeTab.webView.getUrl() : "";

        if (url.contains("gemini")) {
            service = "gemini";
        } else if (url.contains("chatgpt")) {
            service = "chatgpt";
        }

        boolean isTemp = true;
        if ("chatgpt".equalsIgnoreCase(service)) {
            if (url.contains("/c/")) {
                isTemp = false;
            }
        } else if ("gemini".equalsIgnoreCase(service)) {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile("/app/[a-zA-Z0-9_-]{4,}");
            if (p.matcher(url).find()) {
                isTemp = false;
            }
        }

        String method = "sweeper";
        if ("chatgpt".equalsIgnoreCase(service)) {
            method = isTemp ? prefs.getString("export_chatgpt_temp", "fiber") : prefs.getString("export_chatgpt_normal", "api");
        } else if ("gemini".equalsIgnoreCase(service)) {
            method = isTemp ? prefs.getString("export_gemini_temp", "sweeper") : prefs.getString("export_gemini_normal", "sweeper");
        }

        String extractorJs = "(async function() {\n" +
                "  var turns = [];\n" +
                "  var seen = new Set();\n" +
                "  var chosenMethod = " + JSONObject.quote(method) + ";\n" +
                "  var activeService = " + JSONObject.quote(service) + ";\n" +
                "\n" +
                "  function escapeHtml(str) {\n" +
                "    if (!str) return '';\n" +
                "    return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/\"/g, '&quot;').replace(/'/g, '&#039;');\n" +
                "  }\n" +
                "\n" +
                "  function renderTableHtml(tableLines) {\n" +
                "    if (tableLines.length < 2) return tableLines.join('\\n');\n" +
                "    const formatCell = (cStr) => {\n" +
                "      let c = escapeHtml(cStr);\n" +
                "      c = c.replace(/\\*\\*(.*?)\\*\\*/g, '<strong>$1</strong>');\n" +
                "      c = c.replace(/\\*(.*?)\\*/g, '<em>$1</em>');\n" +
                "      c = c.replace(/`([^`]+)`/g, '<code style=\"background: rgba(175,184,193,0.2); padding: 2px 6px; border-radius: 4px; font-family: monospace;\">$1</code>');\n" +
                "      return c;\n" +
                "    };\n" +
                "    const parseRow = (rowStr) => {\n" +
                "      return rowStr.split('|').slice(1, -1).map(cell => cell.trim());\n" +
                "    };\n" +
                "    const headers = parseRow(tableLines[0]);\n" +
                "    let startIdx = 1;\n" +
                "    if (tableLines.length > 1 && tableLines[1].includes('---')) {\n" +
                "      startIdx = 2;\n" +
                "    }\n" +
                "    let html = '<div class=\"table-wrapper\" style=\"overflow-x: auto; margin: 16px 0;\"><table style=\"width: 100%; border-collapse: collapse; margin: 12px 0; font-size: 13px; border: 1px solid #cbd5e1;\">';\n" +
                "    html += '<thead style=\"background-color: #f8fafc; border-bottom: 2px solid #cbd5e1;\"><tr>';\n" +
                "    headers.forEach(h => {\n" +
                "      html += '<th style=\"padding: 8px 12px; text-align: left; font-weight: 700; color: #0f172a; border: 1px solid #cbd5e1;\">' + formatCell(h) + '</th>';\n" +
                "    });\n" +
                "    html += '</tr></thead><tbody>';\n" +
                "    for (let i = startIdx; i < tableLines.length; i++) {\n" +
                "      const cells = parseRow(tableLines[i]);\n" +
                "      const rowBg = i % 2 === 0 ? '#ffffff' : '#f8fafc';\n" +
                "      html += '<tr style=\"background-color: ' + rowBg + ';\">';\n" +
                "      cells.forEach(c => {\n" +
                "        html += '<td style=\"padding: 8px 12px; color: #334155; border: 1px solid #cbd5e1;\">' + formatCell(c) + '</td>';\n" +
                "      });\n" +
                "      html += '</tr>';\n" +
                "    }\n" +
                "    html += '</tbody></table></div>';\n" +
                "    return html;\n" +
                "  }\n" +
                "\n" +
                "  function parseMarkdownAndLaTeX(mdText) {\n" +
                "    if (!mdText) return '';\n" +
                "    let text = mdText;\n" +
                "    const mathBlocks = [];\n" +
                "    text = text.replace(/(\\\\\\[[\\s\\S]*?\\\\\\]|\\$\\$[\\s\\S]*?\\$\\$|\\\\\\(.*?\\\\\\))/g, (match) => {\n" +
                "      const placeholder = '___MATH_BLOCK_' + mathBlocks.length + '___';\n" +
                "      mathBlocks.push(match);\n" +
                "      return placeholder;\n" +
                "    });\n" +
                "    const codeBlocks = [];\n" +
                "    text = text.replace(/```(\\w+)?\\n([\\s\\S]*?)```/g, (match, lang, code) => {\n" +
                "      const langName = lang || 'code';\n" +
                "      const placeholder = '___CODE_BLOCK_' + codeBlocks.length + '___';\n" +
                "      codeBlocks.push('<div class=\"code-block\" style=\"background: #202123; color: #ececf1; padding: 14px; border-radius: 8px; font-family: monospace; font-size: 13px; margin: 14px 0;\"><div class=\"code-header\" style=\"font-weight: bold; margin-bottom: 8px; color: #8e8ea0; font-size: 11px;\">' + escapeHtml(langName) + '</div><pre style=\"margin:0; overflow-x:auto;\"><code>' + escapeHtml(code.trim()) + '</code></pre></div>');\n" +
                "      return placeholder;\n" +
                "    });\n" +
                "    const tableBlocks = [];\n" +
                "    text = text.replace(/(?:^|\\n)(\\|[^\\n]+\\|\\n\\|[-:\\s|]+\\|\\n(?:\\|[^\\n]+\\|\\n?)+)/g, (match, tblStr) => {\n" +
                "      const placeholder = '___TABLE_BLOCK_' + tableBlocks.length + '___';\n" +
                "      const lines = tblStr.trim().split('\\n');\n" +
                "      tableBlocks.push(renderTableHtml(lines));\n" +
                "      return '\\n' + placeholder + '\\n';\n" +
                "    });\n" +
                "    text = escapeHtml(text);\n" +
                "    text = text.replace(/^#### (.*$)/gim, '<h4 style=\"font-size: 13px; font-weight:700; margin: 12px 0 6px 0;\">$1</h4>');\n" +
                "    text = text.replace(/^### (.*$)/gim, '<h3 style=\"font-size: 15px; font-weight:700; margin: 14px 0 8px 0;\">$1</h3>');\n" +
                "    text = text.replace(/^## (.*$)/gim, '<h2 style=\"font-size: 17px; font-weight:700; margin: 16px 0 10px 0;\">$1</h2>');\n" +
                "    text = text.replace(/^# (.*$)/gim, '<h1 style=\"font-size: 20px; font-weight:700; margin: 18px 0 12px 0;\">$1</h1>');\n" +
                "    text = text.replace(/^---$/gim, '<hr style=\"border:0; border-top:1px solid #e2e8f0; margin: 16px 0;\">');\n" +
                "    text = text.replace(/\\*\\*(.*?)\\*\\*/g, '<strong>$1</strong>');\n" +
                "    text = text.replace(/\\*(.*?)\\*/g, '<em>$1</em>');\n" +
                "    text = text.replace(/`([^`]+)`/g, '<code class=\"inline-code\" style=\"background: rgba(175,184,193,0.2); padding: 2px 6px; border-radius: 4px; font-family: monospace; font-size: 13px;\">$1</code>');\n" +
                "    text = text.replace(/^\\s*[\\-\\*]\\s+(.*$)/gim, '<li style=\"margin-left: 18px; list-style-type: disc;\">$1</li>');\n" +
                "    text = text.replace(/(<li.*<\\/li>)/gis, '<ul style=\"margin: 8px 0; padding-left: 0;\">$1</ul>');\n" +
                "    text = text.replace(/^\\s*(\\d+)\\.\\s+(.*$)/gim, '<li style=\"margin-left: 18px;\">$1. $2</li>');\n" +
                "    codeBlocks.forEach((block, idx) => {\n" +
                "      text = text.replace('___CODE_BLOCK_' + idx + '___', () => block);\n" +
                "    });\n" +
                "    tableBlocks.forEach((tBlock, idx) => {\n" +
                "      text = text.replace('___TABLE_BLOCK_' + idx + '___', () => tBlock);\n" +
                "    });\n" +
                "    text = text.replace(/\\n\\n/g, '<br>').replace(/\\n/g, '<br>');\n" +
                "    mathBlocks.forEach((mBlock, idx) => {\n" +
                "      text = text.replace('___MATH_BLOCK_' + idx + '___', () => mBlock);\n" +
                "    });\n" +
                "    return text;\n" +
                "  }\n" +
                "\n" +
                "  function extractGeminiTurnHtml(targetEl) {\n" +
                "    if (!targetEl) return '';\n" +
                "    try {\n" +
                "      var clone = targetEl.cloneNode(true);\n" +
                "      var removeEls = clone.querySelectorAll('button, mat-icon, .action-buttons, .copy-button, .response-feedback, .tts-button, .bottom-container, .sources-container, .drafts-container, [aria-label*=\"Copy\"], [aria-label*=\"draft\"], [aria-label*=\"Listen\"]');\n" +
                "      removeEls.forEach(function(el) { try { el.remove(); } catch(e){} });\n" +
                "      var codeBlocks = Array.from(clone.querySelectorAll('code-block, pre'));\n" +
                "      codeBlocks.forEach(function(cb) {\n" +
                "        if (!cb.parentNode) return;\n" +
                "        var lang = cb.getAttribute('language') || cb.getAttribute('data-language') || cb.getAttribute('ng-reflect-language') || '';\n" +
                "        if (!lang) {\n" +
                "          var langEl = cb.querySelector('.code-title, .code-header, .language-header, .header span, span');\n" +
                "          if (langEl && langEl.innerText) {\n" +
                "            var lt = langEl.innerText.trim();\n" +
                "            if (lt.length < 25 && !lt.includes('\\n')) lang = lt;\n" +
                "          }\n" +
                "        }\n" +
                "        if (!lang) lang = 'code';\n" +
                "        var codeEl = cb.querySelector('code') || cb.querySelector('pre') || cb;\n" +
                "        var rawCode = (codeEl.innerText || codeEl.textContent || '').trim();\n" +
                "        var replacement = document.createElement('div');\n" +
                "        replacement.className = 'code-block';\n" +
                "        replacement.setAttribute('style', 'background: #202123; color: #ececf1; padding: 14px; border-radius: 8px; font-family: monospace; font-size: 13px; margin: 14px 0;');\n" +
                "        replacement.innerHTML = '<div class=\"code-header\" style=\"font-weight: bold; margin-bottom: 8px; color: #8e8ea0; font-size: 11px; text-transform: uppercase;\">' + escapeHtml(lang) + '</div><pre style=\"margin:0; overflow-x:auto; white-space: pre-wrap; word-break: break-all; font-family: monospace;\"><code>' + escapeHtml(rawCode) + '</code></pre>';\n" +
                "        if (cb.parentNode) {\n" +
                "          cb.parentNode.replaceChild(replacement, cb);\n" +
                "        }\n" +
                "      });\n" +
                "      var tables = Array.from(clone.querySelectorAll('table'));\n" +
                "      tables.forEach(function(tbl) {\n" +
                "        tbl.setAttribute('style', 'width: 100%; border-collapse: collapse; margin: 14px 0; font-size: 13px; border: 1px solid #cbd5e1;');\n" +
                "        tbl.querySelectorAll('th').forEach(function(th) {\n" +
                "          th.setAttribute('style', 'padding: 8px 12px; background-color: #f8fafc; border: 1px solid #cbd5e1; font-weight: 700; color: #0f172a;');\n" +
                "        });\n" +
                "        tbl.querySelectorAll('td').forEach(function(td) {\n" +
                "          td.setAttribute('style', 'padding: 8px 12px; border: 1px solid #cbd5e1; color: #334155;');\n" +
                "        });\n" +
                "      });\n" +
                "      clone.querySelectorAll('h1, h2, h3, h4, h5, h6').forEach(function(h) {\n" +
                "        h.setAttribute('style', 'font-weight: 700; color: #0f172a; margin: 16px 0 8px 0; font-size: 15px;');\n" +
                "      });\n" +
                "      clone.querySelectorAll('p').forEach(function(p) {\n" +
                "        p.setAttribute('style', 'margin: 8px 0; line-height: 1.6;');\n" +
                "      });\n" +
                "      clone.querySelectorAll('ul, ol').forEach(function(l) {\n" +
                "        l.setAttribute('style', 'margin: 10px 0; padding-left: 22px;');\n" +
                "      });\n" +
                "      clone.querySelectorAll('li').forEach(function(li) {\n" +
                "        li.setAttribute('style', 'margin-bottom: 4px; color: #334155; line-height: 1.6;');\n" +
                "      });\n" +
                "      clone.querySelectorAll('code:not(.code-block code):not(pre code)').forEach(function(c) {\n" +
                "        c.setAttribute('style', 'background: rgba(175,184,193,0.2); padding: 2px 6px; border-radius: 4px; font-family: monospace; font-size: 13px;');\n" +
                "      });\n" +
                "      return clone.innerHTML ? clone.innerHTML.trim() : '';\n" +
                "    } catch(e) {\n" +
                "      return '';\n" +
                "    }\n" +
                "  }\n" +
                "\n" +
                "  async function imgToBase64(imgEl) {\n" +
                "    return new Promise((resolve) => {\n" +
                "      try {\n" +
                "        if (!imgEl.src) return resolve(null);\n" +
                "        if (imgEl.src.startsWith('data:')) return resolve(imgEl.src);\n" +
                "        var canvas = document.createElement('canvas');\n" +
                "        canvas.width = imgEl.naturalWidth || imgEl.width || 300;\n" +
                "        canvas.height = imgEl.naturalHeight || imgEl.height || 300;\n" +
                "        var ctx = canvas.getContext('2d');\n" +
                "        ctx.drawImage(imgEl, 0, 0);\n" +
                "        resolve(canvas.toDataURL('image/png'));\n" +
                "      } catch(e) { resolve(null); }\n" +
                "    });\n" +
                "  }\n" +
                "\n" +
                "  async function getTurnImages(turnIdx) {\n" +
                "    var selector = activeService === 'gemini' ? '.query-content, .user-query, div.query-text, .model-response, .model-reply, .reply-text-container' : '[data-testid^=\"conversation-turn-\"], div.w-full.text-token-text-primary';\n" +
                "    var rows = Array.from(document.querySelectorAll(selector));\n" +
                "    if (rows[turnIdx]) {\n" +
                "      var imgs = Array.from(rows[turnIdx].querySelectorAll('img')).filter(img => img.src && !img.src.includes('avatar') && !img.src.includes('profile') && img.width > 24);\n" +
                "      var base64s = [];\n" +
                "      for (var img of imgs) {\n" +
                "        var b64 = await imgToBase64(img);\n" +
                "        if (b64) base64s.push(b64);\n" +
                "      }\n" +
                "      return base64s;\n" +
                "    }\n" +
                "    return [];\n" +
                "  }\n" +
                "\n" +
                "  if (chosenMethod === 'api' && activeService === 'chatgpt') {\n" +
                "    try {\n" +
                "      var match = window.location.pathname.match(/\\/c\\/([a-f0-9-]+)/i);\n" +
                "      if (match && match[1]) {\n" +
                "        var convoId = match[1];\n" +
                "        var sessionResp = await fetch('/api/auth/session');\n" +
                "        if (sessionResp.ok) {\n" +
                "          var sessionData = await sessionResp.json();\n" +
                "          var token = sessionData.accessToken;\n" +
                "          if (token) {\n" +
                "            var resp = await fetch('/backend-api/conversation/' + convoId, {\n" +
                "              headers: { 'Authorization': 'Bearer ' + token }\n" +
                "            });\n" +
                "            if (resp.ok) {\n" +
                "              var json = await resp.json();\n" +
                "              if (json && json.mapping) {\n" +
                "                var nodes = [];\n" +
                "                var map = json.mapping;\n" +
                "                for (var key in map) {\n" +
                "                  var node = map[key];\n" +
                "                  if (node && node.message && node.message.content && node.message.content.parts) {\n" +
                "                    var author = (node.message.author && node.message.author.role) ? node.message.author.role : 'assistant';\n" +
                "                    if (author === 'user' || author === 'assistant') {\n" +
                "                      var parts = node.message.content.parts;\n" +
                "                      var textContent = parts.map(function(p){ return (typeof p === 'string') ? p : JSON.stringify(p); }).join('\\n').trim();\n" +
                "                      if (textContent) {\n" +
                "                        nodes.push({\n" +
                "                          create_time: node.message.create_time || 0,\n" +
                "                          author: author,\n" +
                "                          text: textContent\n" +
                "                        });\n" +
                "                      }\n" +
                "                    }\n" +
                "                  }\n" +
                "                }\n" +
                "                if (nodes.length > 0) {\n" +
                "                  nodes.sort(function(a,b){ return a.create_time - b.create_time; });\n" +
                "                  for (var k = 0; k < nodes.length; k++) {\n" +
                "                    if (!seen.has(nodes[k].text)) {\n" +
                "                      seen.add(nodes[k].text);\n" +
                "                      var parsedHtml = parseMarkdownAndLaTeX(nodes[k].text);\n" +
                "                      var localImgs = await getTurnImages(turns.length);\n" +
                "                      localImgs.forEach(b64 => {\n" +
                "                        parsedHtml += '<div style=\"margin-top:12px; text-align:center;\"><img src=\"' + b64 + '\" style=\"max-width:100%; border-radius:8px; box-shadow:0 4px 12px rgba(0,0,0,0.1);\" /></div>';\n" +
                "                      });\n" +
                "                      turns.push({\n" +
                "                        index: turns.length + 1,\n" +
                "                        author: nodes[k].author,\n" +
                "                        role: nodes[k].author === 'user' ? 'User' : 'ChatGPT',\n" +
                "                        text: nodes[k].text,\n" +
                "                        html: parsedHtml,\n" +
                "                        service: 'chatgpt'\n" +
                "                      });\n" +
                "                    }\n" +
                "                  }\n" +
                "                }\n" +
                "              }\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    } catch(e) {}\n" +
                "  }\n" +
                "\n" +
                "  if (turns.length === 0 && chosenMethod !== 'sweeper' && activeService === 'chatgpt') {\n" +
                "    try {\n" +
                "      var mainEl = document.querySelector('main') || document.body;\n" +
                "      var fiberKey = Object.keys(mainEl).find(function(k) { return k.startsWith('__reactFiber$') || k.startsWith('__reactProps$'); });\n" +
                "      if (fiberKey && mainEl[fiberKey]) {\n" +
                "        var curr = mainEl[fiberKey];\n" +
                "        var foundMessages = null;\n" +
                "        var depth = 0;\n" +
                "        while (curr && depth < 40 && !foundMessages) {\n" +
                "          depth++;\n" +
                "          var props = curr.memoizedProps || curr.pendingProps;\n" +
                "          if (props) {\n" +
                "            if (Array.isArray(props.messages)) { foundMessages = props.messages; }\n" +
                "            else if (props.conversation && Array.isArray(props.conversation)) { foundMessages = props.conversation; }\n" +
                "          }\n" +
                "          curr = curr.child || curr.sibling;\n" +
                "        }\n" +
                "        if (foundMessages && Array.isArray(foundMessages)) {\n" +
                "          for (var m = 0; m < foundMessages.length; m++) {\n" +
                "            var msg = foundMessages[m];\n" +
                "            var role = ((msg.author && msg.author.role === 'user') || msg.role === 'user') ? 'User' : 'ChatGPT';\n" +
                "            var text = '';\n" +
                "            if (typeof msg.content === 'string') text = msg.content;\n" +
                "            else if (msg.content && Array.isArray(msg.content.parts)) {\n" +
                "              text = msg.content.parts.filter(function(p) { return typeof p === 'string'; }).join('\\n');\n" +
                "            } else if (msg.text) text = msg.text;\n" +
                "            text = text ? text.trim() : '';\n" +
                "            if (text && !seen.has(text)) {\n" +
                "              seen.add(text);\n" +
                "              var parsedHtml = parseMarkdownAndLaTeX(text);\n" +
                "              var localImgs = await getTurnImages(turns.length);\n" +
                "              localImgs.forEach(b64 => {\n" +
                "                parsedHtml += '<div style=\"margin-top:12px; text-align:center;\"><img src=\"' + b64 + '\" style=\"max-width:100%; border-radius:8px; box-shadow:0 4px 12px rgba(0,0,0,0.1);\" /></div>';\n" +
                "              });\n" +
                "              turns.push({\n" +
                "                index: turns.length + 1,\n" +
                "                author: role === 'User' ? 'user' : 'assistant',\n" +
                "                role: role,\n" +
                "                text: text,\n" +
                "                html: parsedHtml,\n" +
                "                service: 'chatgpt'\n" +
                "              });\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    } catch(e) {}\n" +
                "  }\n" +
                "\n" +
                "  if (turns.length === 0) {\n" +
                "    try {\n" +
                "      if (activeService === 'gemini') {\n" +
                "        var geminiNodes = Array.from(document.querySelectorAll('user-query, model-response, .user-query, .model-response, [data-test-id=\"user-query\"], [data-test-id=\"model-response\"], .conversation-turn'));\n" +
                "        if (geminiNodes.length === 0 || !geminiNodes.some(function(n) { return (n.tagName && n.tagName.toLowerCase() === 'model-response') || n.classList.contains('model-response'); })) {\n" +
                "          var chatTurns = Array.from(document.querySelectorAll('chat-turn, .chat-turn, .conversation-container, div[role=\"region\"]'));\n" +
                "          for (var c = 0; c < chatTurns.length; c++) {\n" +
                "            var ct = chatTurns[c];\n" +
                "            var uq = ct.querySelector('user-query, .user-query, .query-content, .query-text');\n" +
                "            var mr = ct.querySelector('model-response, .model-response, message-content, .model-response-text, .response-container');\n" +
                "            if (uq && geminiNodes.indexOf(uq) === -1) geminiNodes.push(uq);\n" +
                "            if (mr && geminiNodes.indexOf(mr) === -1) geminiNodes.push(mr);\n" +
                "          }\n" +
                "        }\n" +
                "        if (geminiNodes.length === 0) {\n" +
                "          geminiNodes = Array.from(document.querySelectorAll('.query-content, .query-text, message-content, .model-response-text, .markdown, .presented-response-container, .response-content-wrapper'));\n" +
                "        }\n" +
                "        for (var i = 0; i < geminiNodes.length; i++) {\n" +
                "          var el = geminiNodes[i];\n" +
                "          var tag = el.tagName ? el.tagName.toLowerCase() : '';\n" +
                "          var isUser = tag === 'user-query' || el.classList.contains('user-query') || el.classList.contains('query-content') || el.classList.contains('query-text') || (el.hasAttribute('data-test-id') && el.getAttribute('data-test-id') === 'user-query');\n" +
                "          var text = '';\n" +
                "          if (isUser) {\n" +
                "            var qEl = el.querySelector('.query-text, .query-content, .user-query-container') || el;\n" +
                "            text = (qEl.innerText || qEl.textContent || '').trim();\n" +
                "          } else {\n" +
                "            var rEl = el.querySelector('message-content, .model-response-text, .response-container-content, .markdown, .presented-response-container, .response-content-wrapper') || el;\n" +
                "            text = (rEl.innerText || rEl.textContent || '').trim();\n" +
                "          }\n" +
                "          text = text.replace(/^(Show drafts|Google it|Share|Copy\\s*code|Modify\\s*response)\\b/gim, '').trim();\n" +
                "          if (text && !seen.has(text)) {\n" +
                "            seen.add(text);\n" +
                "            var parsedHtml = '';\n" +
                "            if (isUser) {\n" +
                "              parsedHtml = parseMarkdownAndLaTeX(text);\n" +
                "            } else {\n" +
                "              var rEl = el.querySelector('message-content, .model-response-text, .response-container-content, .markdown, .presented-response-container, .response-content-wrapper') || el;\n" +
                "              parsedHtml = extractGeminiTurnHtml(rEl);\n" +
                "              if (!parsedHtml || parsedHtml.trim().length === 0) {\n" +
                "                parsedHtml = parseMarkdownAndLaTeX(text);\n" +
                "              }\n" +
                "            }\n" +
                "            var localImgs = await getTurnImages(turns.length);\n" +
                "            localImgs.forEach(b64 => {\n" +
                "              parsedHtml += '<div style=\"margin-top:12px; text-align:center;\"><img src=\"' + b64 + '\" style=\"max-width:100%; border-radius:8px; box-shadow:0 4px 12px rgba(0,0,0,0.1);\" /></div>';\n" +
                "            });\n" +
                "            turns.push({\n" +
                "              index: turns.length + 1,\n" +
                "              author: isUser ? 'user' : 'assistant',\n" +
                "              role: isUser ? 'User' : 'Gemini',\n" +
                "              text: text,\n" +
                "              html: parsedHtml,\n" +
                "              service: 'gemini'\n" +
                "            });\n" +
                "          }\n" +
                "        }\n" +
                "      } else {\n" +
                "        var turnDivs = Array.from(document.querySelectorAll('article, [data-testid^=\"conversation-turn-\"], div.w-full.text-token-text-primary'));\n" +
                "        for (var i = 0; i < turnDivs.length; i++) {\n" +
                "          var row = turnDivs[i];\n" +
                "          var text = '';\n" +
                "          var isUser = false;\n" +
                "          if (row.querySelector('[data-testid=\"user-turn\"], [data-message-author-role=\"user\"]') || row.querySelector('div.bg-token-main-surface-secondary') || row.innerText.includes('User Prompt')) {\n" +
                "            isUser = true;\n" +
                "          }\n" +
                "          var textDiv = row.querySelector('.markdown, div.markdown, .prose, .whitespace-pre-wrap');\n" +
                "          if (textDiv) {\n" +
                "            text = textDiv.innerText.trim();\n" +
                "          } else {\n" +
                "            var contentDiv = row.querySelector('.content, div.text-token-text-primary');\n" +
                "            text = contentDiv ? contentDiv.innerText.trim() : row.innerText.trim();\n" +
                "          }\n" +
                "          if (text && !seen.has(text)) {\n" +
                "            seen.add(text);\n" +
                "            var parsedHtml = parseMarkdownAndLaTeX(text);\n" +
                "            var localImgs = await getTurnImages(turns.length);\n" +
                "            localImgs.forEach(b64 => {\n" +
                "              parsedHtml += '<div style=\"margin-top:12px; text-align:center;\"><img src=\"' + b64 + '\" style=\"max-width:100%; border-radius:8px; box-shadow:0 4px 12px rgba(0,0,0,0.1);\" /></div>';\n" +
                "            });\n" +
                "            turns.push({\n" +
                "              index: turns.length + 1,\n" +
                "              author: isUser ? 'user' : 'assistant',\n" +
                "              role: isUser ? 'User' : 'ChatGPT',\n" +
                "              text: text,\n" +
                "              html: parsedHtml,\n" +
                "              service: 'chatgpt'\n" +
                "            });\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    } catch(e) {}\n" +
                "  }\n" +
                "\n" +
                "  if (window.CaspianBridge && typeof window.CaspianBridge.onConversationExtracted === 'function') {\n" +
                "    window.CaspianBridge.onConversationExtracted(JSON.stringify(turns), '" + exportFmt + "');\n" +
                "  }\n" +
                "})();";

        mainWebView.evaluateJavascript(extractorJs, null);
    }

    public void handleExtractedConversation(String jsonStr, String exportFmt) {
        try {
            if (jsonStr == null || jsonStr.equals("null") || jsonStr.equals("[]")) {
                Toast.makeText(this, "No chat turns found to export!", Toast.LENGTH_SHORT).show();
                return;
            }

            JSONArray turnsArray = new JSONArray(jsonStr);
            if (turnsArray.length() == 0) {
                Toast.makeText(this, "No chat turns found to export!", Toast.LENGTH_SHORT).show();
                return;
            }

            hideControlSheet();

            String title = "AI Conversation";
            String dateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            String safeTitle = title.replaceAll("[^a-zA-Z0-9_-]", "_");

            if ("md".equalsIgnoreCase(exportFmt) || "markdown".equalsIgnoreCase(exportFmt)) {
                StringBuilder sb = new StringBuilder("# " + title + "\n\n*Exported via Caspian Flow on " + dateStr + "*\n\n---\n\n");
                for (int i = 0; i < turnsArray.length(); i++) {
                    JSONObject obj = turnsArray.getJSONObject(i);
                    sb.append("### Turn ").append(obj.getInt("index")).append(" (").append(obj.getString("role")).append(")\n\n").append(obj.getString("text")).append("\n\n---\n\n");
                }
                downloadFile(safeTitle + "_Caspian_Export.md", sb.toString(), "text/markdown");

            } else if ("txt".equalsIgnoreCase(exportFmt) || "text".equalsIgnoreCase(exportFmt)) {
                StringBuilder sb = new StringBuilder("======================================\n" + title.toUpperCase() + "\nExported via Caspian Flow on " + dateStr + "\n======================================\n\n");
                for (int i = 0; i < turnsArray.length(); i++) {
                    JSONObject obj = turnsArray.getJSONObject(i);
                    sb.append("[TURN ").append(obj.getInt("index")).append(" - ").append(obj.getString("role")).append("]\n").append(obj.getString("text")).append("\n\n--------------------------------------\n\n");
                }
                downloadFile(safeTitle + "_Caspian_Export.txt", sb.toString(), "text/plain");

            } else if ("doc".equalsIgnoreCase(exportFmt)) {
                StringBuilder sb = new StringBuilder("<html><body><h1>" + title + "</h1><p>Exported via Caspian Flow on " + dateStr + "</p>");
                for (int i = 0; i < turnsArray.length(); i++) {
                    JSONObject obj = turnsArray.getJSONObject(i);
                    sb.append("<h3>Turn ").append(obj.getInt("index")).append(" (").append(obj.getString("role")).append(")</h3><div>").append(obj.getString("html")).append("</div><hr>");
                }
                sb.append("</body></html>");
                downloadFile(safeTitle + "_Caspian_Export.doc", sb.toString(), "application/msword");

            } else if ("styledpdf".equalsIgnoreCase(exportFmt) || "pdf".equalsIgnoreCase(exportFmt)) {
                StringBuilder turnsHtml = new StringBuilder();
                String detectionService = "chatgpt";

                for (int i = 0; i < turnsArray.length(); i++) {
                    JSONObject obj = turnsArray.getJSONObject(i);
                    int idx = obj.getInt("index");
                    String html = obj.getString("html");
                    String author = obj.optString("author", (idx % 2 != 0) ? "user" : "assistant");
                    String service = obj.optString("service", "chatgpt");
                    detectionService = service;
                    boolean isUser = "user".equalsIgnoreCase(author);
                    boolean isGemini = "gemini".equalsIgnoreCase(service);

                    String bgStyle = isUser ? "background: #f7f7f8; border: 1px solid #e5e5e5;" : "background: #ffffff; border: 1px solid #e1e4e8;";
                    String badgeBg = isUser ? (isGemini ? "background: #153d6f; color: #ffffff;" : "background: #10a37f; color: #ffffff;")
                                            : "background: #1B4264; color: #ffffff;";
                    String senderLabel = isUser ? "User Prompt" : (isGemini ? "Gemini Response" : "ChatGPT Response");

                    turnsHtml.append("<div style=\"margin-bottom: 24px; border-radius: 12px; ").append(bgStyle).append(" padding: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.04); page-break-inside: avoid;\">")
                            .append("<div style=\"display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; border-bottom: 1px solid #ececf1; padding-bottom: 8px;\">")
                            .append("<span style=\"font-weight: 700; font-size: 12px; padding: 4px 10px; border-radius: 6px; ").append(badgeBg).append("\">").append(senderLabel).append(" #").append(idx).append("</span>")
                            .append("<span style=\"font-size: 10px; color: #8e8ea0;\">Caspian Flow</span>")
                            .append("</div>")
                            .append("<div class=\"chat-turn-content\" style=\"font-size: 14px; line-height: 1.7; color: #353740; font-family: 'Inter', -apple-system, sans-serif;\">").append(html).append("</div></div>");
                }

                String badgeTitle = "gemini".equalsIgnoreCase(detectionService) ? "Caspian Flow (Gemini)" : "Caspian Flow (ChatGPT)";

                String fullHtml = "<!DOCTYPE html><html><head><meta charset=\"utf-8\"><title>" + title + "</title>" +
                        "<style>" +
                        "@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&family=JetBrains+Mono:wght@400;500&display=swap');" +
                        "@import url('https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/katex.min.css');" +
                        "body { font-family: 'Inter', -apple-system, sans-serif; padding: 32px; background: #ffffff; color: #353740; max-width: 900px; margin: 0 auto; }" +
                        "h1 { font-size: 24px; font-weight: 700; color: #202123; margin-bottom: 8px; font-family: 'Inter', sans-serif; }" +
                        ".doc-header { border-bottom: 2px solid #1B4264; padding-bottom: 14px; margin-bottom: 28px; display: flex; justify-content: space-between; align-items: center; }" +
                        ".doc-meta { font-size: 11px; color: #8e8ea0; font-weight: 500; }" +
                        "pre { background: #202123 !important; color: #ececf1 !important; padding: 14px; border-radius: 8px; overflow-x: auto; font-family: 'JetBrains Mono', monospace; font-size: 13px; margin: 14px 0; }" +
                        "code { font-family: 'JetBrains Mono', monospace; font-size: 13px; background: rgba(175,184,193,0.2); padding: 2px 6px; border-radius: 4px; }" +
                        "blockquote { border-left: 4px solid #1B4264; margin: 12px 0; padding-left: 16px; color: #565869; font-style: italic; }" +
                        "table { width: 100%; border-collapse: collapse; margin: 16px 0; font-size: 13px; }" +
                        "th, td { border: 1px solid #d9d9e3; padding: 8px 12px; text-align: left; }" +
                        "th { background: #f7f7f8; font-weight: 600; }" +
                        "</style>" +
                        "<script src=\"https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/katex.min.js\"></script>" +
                        "<script src=\"https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/contrib/auto-render.min.js\"></script>" +
                        "<script>" +
                        "document.addEventListener('DOMContentLoaded', function() {" +
                        "  renderMathInElement(document.body, {" +
                        "    delimiters: [" +
                        "      {left: '$$', right: '$$', display: true}," +
                        "      {left: '$', right: '$', display: false}," +
                        "      {left: '\\\\(', right: '\\\\)', display: false}," +
                        "      {left: '\\\\[', right: '\\\\]', display: true}" +
                        "    ]," +
                        "    throwOnError : false" +
                        "  });" +
                        "});" +
                        "</script>" +
                        "</head><body>" +
                        "<div class=\"doc-header\"><div><h1>" + title + "</h1><div class=\"doc-meta\">Exported via Caspian Flow &bull; " + dateStr + "</div></div>" +
                        "<div style=\"font-size: 12px; font-weight: 700; color: #1B4264; border: 1px solid #1B4264; padding: 4px 10px; border-radius: 6px;\">" + badgeTitle + "</div></div>" +
                        turnsHtml.toString() + "</body></html>";

                downloadFile(safeTitle + "_AI_Export.html", fullHtml, "text/html");
                printHtml("Caspian_AI_Document", fullHtml);

            } else if ("convert".equalsIgnoreCase(exportFmt) || "copy".equalsIgnoreCase(exportFmt)) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < turnsArray.length(); i++) {
                    JSONObject obj = turnsArray.getJSONObject(i);
                    sb.append("[Turn ").append(obj.getInt("index")).append(" - ").append(obj.getString("role")).append("]\n").append(obj.getString("text")).append("\n\n");
                }
                copyToClipboard(sb.toString());

                if ("convert".equalsIgnoreCase(exportFmt)) {
                    String sourceService = "chatgpt";
                    if (turnsArray.length() > 0) {
                        sourceService = turnsArray.getJSONObject(0).optString("service", "chatgpt");
                    }
                    createNewTabWithPrefill(sourceService, sb.toString());
                    Toast.makeText(this, "Copied context to clipboard & opened in new tab!", Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Export failed: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            ClipData clip = ClipData.newPlainText("AI Transcript", text);
            clipboard.setPrimaryClip(clip);
        }
    }

    public void createNewTabWithPrefill(String sourceService, String prompt) {
        String targetUrl = "https://gemini.google.com/app";
        String targetService = "gemini";
        if ("gemini".equalsIgnoreCase(sourceService)) {
            targetUrl = "https://chatgpt.com";
            targetService = "chatgpt";
        }
        addNewTab(targetService, prompt, targetUrl, false);
    }

    public void downloadFile(String fileName, String content, String mimeType) {
        try {
            File exportDir = new File(getExternalFilesDir(null), "Downloads");
            if (!exportDir.exists()) exportDir.mkdirs();
            File file = new File(exportDir, fileName);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(content.getBytes(StandardCharsets.UTF_8));
            fos.close();
            Toast.makeText(this, "Saved: " + file.getName(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Download failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void printHtml(String jobName, String htmlContent) {
        runOnUiThread(() -> {
            try {
                WebView printWebView = new WebView(this);
                printWebView.getSettings().setJavaScriptEnabled(true);
                printWebView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
                        if (printManager != null) {
                            PrintDocumentAdapter printAdapter = view.createPrintDocumentAdapter(jobName);
                            printManager.print(jobName, printAdapter, new PrintAttributes.Builder().build());
                        }
                    }
                });
                printWebView.loadDataWithBaseURL("https://cdn.jsdelivr.net/", htmlContent, "text/html", "utf-8", null);
            } catch (Exception e) {
                Toast.makeText(this, "Print error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void printPage() {
        TabItem currentTab = getTabById(activeTabId);
        if (currentTab != null && currentTab.webView != null) {
            PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
            if (printManager != null) {
                PrintDocumentAdapter printAdapter = currentTab.webView.createPrintDocumentAdapter("Caspian_Document");
                printManager.print("Caspian_Document", printAdapter, new PrintAttributes.Builder().build());
            }
        }
    }

    private String readAssetScript(String fileName) {
        String cached = assetScriptCache.get(fileName);
        if (cached != null) return cached;
        try {
            InputStream is = getAssets().open(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
            is.close();
            String result = sb.toString();
            assetScriptCache.put(fileName, result);
            return result;
        } catch (Exception e) {
            return "";
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private TabItem createNewTabInstance(int id, String url, String service, String promptPayload, boolean isIncognito) {
        WebView webView = new WebView(this);
        webView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(!isIncognito);
        settings.setDatabaseEnabled(!isIncognito);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setUserAgentString(MOBILE_UA);

        if (!isIncognito) {
            CookieManager.getInstance().setAcceptCookie(true);
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        } else {
            settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        }

        webView.addJavascriptInterface(new CaspianBridge(this), "CaspianBridge");
        applyWebViewTheme(webView, isDarkTheme);

        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        settings.setOffscreenPreRaster(true);
        settings.setEnableSmoothTransition(true);

        TabItem tabItem = new TabItem(id, "New Tab", url, service, webView, isIncognito);
        tabItem.pendingPrompt = promptPayload;

        webView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (isGoogleDockAutoCollapse && searchNavContainer != null && searchNavContainer.getVisibility() == View.VISIBLE) {
                int delta = scrollY - oldScrollY;
                if (delta > 20 && searchDockScroll.getVisibility() == View.VISIBLE) {
                    searchDockScroll.setVisibility(View.GONE);
                    searchNavBall.setVisibility(View.VISIBLE);
                } else if (delta < -20 && searchNavBall.getVisibility() == View.VISIBLE) {
                    searchNavBall.setVisibility(View.GONE);
                    searchDockScroll.setVisibility(View.VISIBLE);
                }
            }
        });

        webView.setDownloadListener((downloadUrl, userAgent, contentDisposition, mimeType, contentLength) -> {
            try {
                String fileName = URLUtil.guessFileName(downloadUrl, contentDisposition, mimeType);
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
                request.setMimeType(mimeType);
                request.addRequestHeader("User-Agent", userAgent);
                request.setDescription("Downloading file via Caspian Flow...");
                request.setTitle(fileName);
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

                DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                if (dm != null) {
                    dm.enqueue(request);
                    Toast.makeText(this, "📥 Downloading " + fileName, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Download error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        webView.setFindListener((activeMatchOrdinal, numberOfMatches, isDoneCounting) -> {
            String countText = (numberOfMatches > 0 ? (activeMatchOrdinal + 1) : 0) + "/" + numberOfMatches;
            if (navFinderCount != null) navFinderCount.setText(countText);
            if (chatgptFinderCount != null) chatgptFinderCount.setText(countText);
            if (omniboxFinderCount != null) omniboxFinderCount.setText(countText);
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String targetUrl = request.getUrl().toString();
                if (splitModeState != 0 && openLeftLinksToRight && tabItem.id == activeTabId && secondarySplitTabId != -1) {
                    TabItem rightTab = getTabById(secondarySplitTabId);
                    if (rightTab != null && rightTab.webView != null) {
                        rightTab.webView.loadUrl(targetUrl);
                        return true;
                    }
                }
                return super.shouldOverrideUrlLoading(view, request);
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                if (adBlockShield != null && adBlockShield.isBlocked(request.getUrl().toString())) {
                    return new WebResourceResponse("text/plain", "UTF-8", new ByteArrayInputStream("".getBytes()));
                }
                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public void onPageStarted(WebView view, String pageUrl, Bitmap favicon) {
                tabItem.url = pageUrl;
                tabItem.service = AICommandRouter.detectServiceFromUrl(pageUrl);
                if (tabItem.id == activeTabId) {
                    browserProgressBar.setVisibility(View.VISIBLE);
                    browserProgressBar.setProgress(15);
                    updateOmniboxState();
                }
                if (pageUrl != null && pageUrl.contains("chatgpt.com")) {
                    String interceptorJs = readAssetScript("chatgpt_network_interceptor.js");
                    if (!interceptorJs.isEmpty()) view.evaluateJavascript(interceptorJs, null);
                }
            }

            @Override
            public void onPageFinished(WebView view, String pageUrl) {
                tabItem.url = pageUrl;
                tabItem.title = view.getTitle() != null ? view.getTitle() : "Caspian Flow";
                if (tabItem.id == activeTabId) {
                    browserProgressBar.setVisibility(View.GONE);
                    updateOmniboxState();
                }

                captureTabSnapshot(tabItem);

                if (pageUrl != null && !pageUrl.startsWith("file://") && !pageUrl.startsWith("caspian://") && !pageUrl.startsWith("about:")) {
                    HistoryManager.getInstance(MainActivity.this).addEntry(tabItem.title, pageUrl);
                }

                if (pageUrl != null && pageUrl.contains("chatgpt.com")) {
                    String interceptorJs = readAssetScript("chatgpt_network_interceptor.js");
                    if (!interceptorJs.isEmpty()) {
                        view.evaluateJavascript(interceptorJs, null);
                    }
                }

                String prunerJs = readAssetScript("mobile_pruner.js");
                if (!prunerJs.isEmpty()) {
                    SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                    int limit = 5;
                    try {
                        limit = Integer.parseInt(prefs.getString("chat_message_limit", "5"));
                    } catch (Exception ignored) {}
                    String mode = prefs.getString("chat_pruning_mode", "sliding_window");
                    boolean enabled = !"false".equalsIgnoreCase(prefs.getString("chat_limit_enabled", "true"));
                    view.evaluateJavascript(prunerJs + "\nif (window.__CASPIAN_PRUNER_UPDATE) window.__CASPIAN_PRUNER_UPDATE(" + limit + ", '" + mode + "', " + enabled + ");", null);
                }

                if (pageUrl != null && pageUrl.toLowerCase().contains("youtube.com")) {
                    String ytHelperJs = readAssetScript("youtube_helper.js");
                    if (!ytHelperJs.isEmpty()) {
                        view.evaluateJavascript(ytHelperJs, null);
                    }
                }

                if (adBlockShield != null && adBlockShield.isEnabled()) {
                    view.evaluateJavascript(AdBlockShield.COSMETIC_CSS_INJECTION, null);
                }

                if (tabItem.pendingPrompt != null && !tabItem.pendingPrompt.isEmpty()) {
                    injectAndSubmitAIPrompt(view, tabItem.pendingPrompt);
                    tabItem.pendingPrompt = null;
                }
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (tabItem.id == activeTabId) {
                    browserProgressBar.setProgress(newProgress);
                    if (newProgress == 100) browserProgressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                tabItem.title = title;
                if (tabItem.id == activeTabId) updateOmniboxState();
            }

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                if (customView != null) {
                    callback.onCustomViewHidden();
                    return;
                }
                customView = view;
                customViewCallback = callback;
                fullscreenContainer.addView(customView);
                fullscreenContainer.setVisibility(View.VISIBLE);
            }

            @Override
            public void onHideCustomView() {
                if (customView == null) return;
                fullscreenContainer.removeView(customView);
                customView = null;
                fullscreenContainer.setVisibility(View.GONE);
                if (customViewCallback != null) customViewCallback.onCustomViewHidden();
            }

            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                if (uploadMessage != null) uploadMessage.onReceiveValue(null);
                uploadMessage = filePathCallback;
                Intent intent = fileChooserParams.createIntent();
                try {
                    startActivityForResult(intent, FILECHOOSER_RESULTCODE);
                } catch (Exception e) {
                    uploadMessage = null;
                    return false;
                }
                return true;
            }
        });

        webView.loadUrl(url);
        return tabItem;
    }

    public void addNewTab(String service, String prompt) {
        String url = "file:///android_asset/launch_hub.html";
        if ("hub".equalsIgnoreCase(service)) url = "file:///android_asset/launch_hub.html";
        else if ("chatgpt".equalsIgnoreCase(service)) url = "https://chatgpt.com";
        else if ("gemini".equalsIgnoreCase(service)) url = "https://gemini.google.com/app";
        else if ("claude".equalsIgnoreCase(service)) url = "https://claude.ai/new";
        else if ("deepseek".equalsIgnoreCase(service)) url = "https://chat.deepseek.com";
        else if ("youtube".equalsIgnoreCase(service)) url = "https://m.youtube.com";
        else if ("google".equalsIgnoreCase(service)) url = "https://www.google.com";

        addNewTab(service, prompt, url, false);
    }

    public void addNewTab(String service, String prompt, String url, boolean isIncognito) {
        int id = nextTabId++;
        String finalUrl = (url != null && !url.trim().isEmpty()) ? url : "file:///android_asset/launch_hub.html";
        String finalService = (service != null && !service.trim().isEmpty()) ? service : ("file:///android_asset/launch_hub.html".equals(finalUrl) ? "hub" : "web");
        
        TabItem tab = createNewTabInstance(id, finalUrl, finalService, prompt, isIncognito);
        if ("file:///android_asset/launch_hub.html".equals(finalUrl)) {
            tab.title = "Caspian Hub";
        }
        tabsList.add(tab);
        switchToTab(id);
        saveOpenTabsState();
    }

    public void switchActiveTabService(String service) {
        String url = "https://www.google.com";
        if ("hub".equalsIgnoreCase(service)) url = "file:///android_asset/launch_hub.html";
        else if ("chatgpt".equalsIgnoreCase(service)) url = "https://chatgpt.com";
        else if ("gemini".equalsIgnoreCase(service)) url = "https://gemini.google.com/app";
        else if ("claude".equalsIgnoreCase(service)) url = "https://claude.ai/new";
        else if ("deepseek".equalsIgnoreCase(service)) url = "https://chat.deepseek.com";
        else if ("youtube".equalsIgnoreCase(service)) url = "https://m.youtube.com";
        else if ("google".equalsIgnoreCase(service)) url = "https://www.google.com";

        TabItem tab = getTabById(activeTabId);
        if (tab != null && tab.webView != null) {
            tab.service = service;
            tab.url = url;
            if ("hub".equalsIgnoreCase(service)) tab.title = "Caspian Hub";
            tab.webView.loadUrl(url);
            updateOmniboxState();
            saveOpenTabsState();
        } else {
            addNewTab(service, null, url, false);
        }
    }

    public void switchToTab(int tabId) {
        TabItem previousTab = getTabById(activeTabId);
        if (previousTab != null) captureTabSnapshot(previousTab);

        TabItem tab = getTabById(tabId);
        if (tab == null) return;

        activeTabId = tabId;
        if (splitModeState == 0) {
            webViewContainer.removeAllViews();
            if (tab.webView.getParent() != null) {
                ((ViewGroup) tab.webView.getParent()).removeView(tab.webView);
            }
            webViewContainer.addView(tab.webView);
        } else {
            applySplitViewLayout();
        }
        updateOmniboxState();
        hideControlSheet(false);
        playUiFeedbackSound("tm_tabs");
        saveOpenTabsState();
    }

    public void closeTab(int tabId) {
        if (tabsList.size() <= 1) {
            TabItem last = tabsList.get(0);
            last.url = "file:///android_asset/launch_hub.html";
            last.service = "hub";
            last.title = "Caspian Hub";
            last.webView.loadUrl(last.url);
            updateOmniboxState();
            saveOpenTabsState();
            return;
        }

        TabItem toRemove = getTabById(tabId);
        if (toRemove != null) {
            if (!toRemove.isIncognito) {
                closedTabsHistory.add(toRemove);
            } else {
                try {
                    toRemove.webView.clearCache(true);
                    toRemove.webView.clearHistory();
                    toRemove.webView.clearFormData();
                } catch(Exception ignored) {}
            }
            if (toRemove.webView.getParent() != null) {
                ((ViewGroup) toRemove.webView.getParent()).removeView(toRemove.webView);
            }
            toRemove.webView.destroy();
            tabsList.remove(toRemove);

            for (TabGroup g : tabGroupsList) {
                g.tabIds.remove((Integer) tabId);
            }
            tabGroupsList.removeIf(g -> g.tabIds.isEmpty());
            saveTabGroups();

            if (activeTabId == tabId) {
                activeTabId = tabsList.get(tabsList.size() - 1).id;
                switchToTab(activeTabId);
            }
        }
        updateOmniboxState();
        saveOpenTabsState();
    }

    public void closeMultipleTabs(List<Integer> ids) {
        for (int id : ids) {
            closeTab(id);
        }
        saveOpenTabsState();
    }

    public void restoreLastClosedTab() {
        if (!closedTabsHistory.isEmpty()) {
            TabItem restored = closedTabsHistory.remove(closedTabsHistory.size() - 1);
            addNewTab(restored.service, restored.pendingPrompt, restored.url, restored.isIncognito);
        }
    }

    public void restoreLastClosedGroupTabs() {
        restoreLastClosedTab();
    }

    public void setGroupTabsFavorite(List<Integer> ids, boolean isFav) {
        for (int id : ids) {
            TabItem tab = getTabById(id);
            if (tab != null) tab.isFavorite = isFav;
        }
        saveOpenTabsState();
    }

    public void toggleTabFavorite(int tabId) {
        TabItem tab = getTabById(tabId);
        if (tab != null) {
            tab.isFavorite = !tab.isFavorite;
            Toast.makeText(this, tab.isFavorite ? "⭐ Tab Favorited" : "★ Tab Unfavorited", Toast.LENGTH_SHORT).show();
            saveOpenTabsState();
        }
    }

    public void toggleTabMute(int tabId) {
        TabItem tab = getTabById(tabId);
        if (tab != null) {
            tab.isMuted = !tab.isMuted;
            if (tab.webView != null) {
                tab.webView.evaluateJavascript("(function(){ var vs = document.querySelectorAll('video, audio'); vs.forEach(function(v){ v.muted = " + tab.isMuted + "; }); })();", null);
            }
            Toast.makeText(this, tab.isMuted ? "🔇 Tab Muted" : "🔊 Tab Unmuted", Toast.LENGTH_SHORT).show();
            saveOpenTabsState();
        }
    }

    public void updateTabDetails(int tabId, String nickname, String url) {
        TabItem tab = getTabById(tabId);
        if (tab != null) {
            tab.nickname = nickname;
            if (url != null && !url.isEmpty() && !url.equals(tab.url)) {
                tab.url = url;
                if (tab.webView != null) tab.webView.loadUrl(url);
            }
            Toast.makeText(this, "Tab Details Updated", Toast.LENGTH_SHORT).show();
            saveOpenTabsState();
        }
    }

    public void reorderTabs(String newIdsJson) {
        try {
            JSONArray arr = new JSONArray(newIdsJson);
            List<TabItem> reordered = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++) {
                int id = arr.getInt(i);
                for (TabItem item : tabsList) {
                    if (item.id == id) {
                        reordered.add(item);
                        break;
                    }
                }
            }
            if (reordered.size() == tabsList.size()) {
                tabsList.clear();
                tabsList.addAll(reordered);
                saveOpenTabsState();
            }
        } catch (Exception ignored) {}
    }

    public void closeAllTabs() {
        for (int i = tabsList.size() - 1; i > 0; i--) {
            TabItem item = tabsList.get(i);
            if (item.isFavorite) continue;
            if (item.webView.getParent() != null) {
                ((ViewGroup) item.webView.getParent()).removeView(item.webView);
            }
            item.webView.destroy();
            tabsList.remove(i);
        }
        tabGroupsList.clear();
        saveTabGroups();

        if (!tabsList.isEmpty()) {
            TabItem first = tabsList.get(0);
            first.url = "https://www.google.com";
            first.service = "web";
            first.webView.loadUrl(first.url);
            activeTabId = first.id;
            switchToTab(first.id);
        }
        updateOmniboxState();
        saveOpenTabsState();
    }

    public void navigateUrl(String url) {
        handleOmniboxSubmission(url);
    }

    public TabItem getActiveOrDominantTab() {
        if (splitModeState != 0) {
            if (splitRatio < 0.48f && secondarySplitTabId != -1) {
                TabItem rightTab = getTabById(secondarySplitTabId);
                if (rightTab != null) return rightTab;
            }
            if (splitRatio >= 0.48f && activeTabId != -1) {
                TabItem leftTab = getTabById(activeTabId);
                if (leftTab != null) return leftTab;
            }
        }
        return getTabById(activeTabId);
    }

    public void reloadTab(int tabId) {
        TabItem tab = getTabById(tabId);
        if (tab != null && tab.webView != null) tab.webView.reload();
    }

    public void reloadActiveTab() {
        TabItem tab = getActiveOrDominantTab();
        if (tab != null && tab.webView != null) tab.webView.reload();
    }

    public void toggleDesktopMode(int tabId) {
        TabItem tab = getTabById(tabId);
        if (tab == null) tab = getActiveOrDominantTab();
        if (tab == null || tab.webView == null) return;
        tab.isDesktop = !tab.isDesktop;
        tab.webView.getSettings().setUserAgentString(tab.isDesktop ? DESKTOP_UA : MOBILE_UA);
        tab.webView.getSettings().setUseWideViewPort(tab.isDesktop);
        tab.webView.reload();
        Toast.makeText(this, tab.isDesktop ? "💻 Desktop Mode" : "📱 Mobile Mode", Toast.LENGTH_SHORT).show();
    }

    public void setAdBlockEnabled(boolean enabled) {
        adBlockShield.setEnabled(enabled);
        updateOmniboxState();
    }

    public int getBlockedAdsCount() {
        return adBlockShield.getBlockedCount();
    }

    private TabItem getTabById(int tabId) {
        for (TabItem item : tabsList) {
            if (item.id == tabId) return item;
        }
        return null;
    }

    private void updateOmniboxState() {
        TabItem currentTab = getActiveOrDominantTab();
        int themeAccent = Color.parseColor(podStartColor);

        if (currentTab != null) {
            String url = currentTab.url != null ? currentTab.url : "";
            if (!omniboxEditText.hasFocus()) {
                omniboxEditText.setText(cleanDisplayUrl(url));
            }
            omniboxBackBtn.setEnabled(currentTab.webView.canGoBack());
            omniboxBackBtn.setAlpha(currentTab.webView.canGoBack() ? 1.0f : 0.4f);

            omniboxForwardBtn.setEnabled(currentTab.webView.canGoForward());
            omniboxForwardBtn.setAlpha(currentTab.webView.canGoForward() ? 1.0f : 0.4f);

            boolean isYtTab = url.toLowerCase().contains("youtube.com") || "youtube".equalsIgnoreCase(currentTab.service);
            if (ytFloatingRemoteContainer != null) {
                if (isYtTab && !isYtRemoteExplicitlyHidden) {
                    if (ytFloatingRemoteContainer.getVisibility() != View.VISIBLE) {
                        ytFloatingRemoteScroll.setVisibility(View.GONE);
                        ytFloatingRemoteBall.setVisibility(View.VISIBLE);
                    }
                    ytFloatingRemoteContainer.setVisibility(View.VISIBLE);
                } else {
                    ytFloatingRemoteContainer.setVisibility(View.GONE);
                }
            }

            boolean isGoogleTab = (url.toLowerCase().contains("google.com/search") || url.toLowerCase().contains("google.com/url") || (url.toLowerCase().contains("google.com") && !url.toLowerCase().contains("gemini.google.com"))) || "google".equalsIgnoreCase(currentTab.service);
            if (searchNavContainer != null) {
                if (isGoogleTab && !isSearchNavExplicitlyHidden) {
                    if (searchNavContainer.getVisibility() != View.VISIBLE) {
                        searchDockScroll.setVisibility(View.GONE);
                        searchNavBall.setVisibility(View.VISIBLE);
                    }
                    searchNavContainer.setVisibility(View.VISIBLE);
                    String query = extractQueryFromUrl(url);
                    if (searchDockUrl != null) searchDockUrl.setText(query.isEmpty() ? "google.com" : query);
                } else {
                    searchNavContainer.setVisibility(View.GONE);
                }
            }

            boolean isChatgptTab = (url.toLowerCase().contains("chatgpt.com") || url.toLowerCase().contains("claude.ai") || url.toLowerCase().contains("chat.deepseek.com")) || "chatgpt".equalsIgnoreCase(currentTab.service);
            if (chatgptDockContainer != null) {
                if (isChatgptTab && !isChatgptDockExplicitlyHidden) {
                    if (chatgptDockContainer.getVisibility() != View.VISIBLE) {
                        chatgptDockScroll.setVisibility(View.GONE);
                        chatgptDockBall.setVisibility(View.VISIBLE);
                    }
                    chatgptDockContainer.setVisibility(View.VISIBLE);
                    updateChatgptDockButtons();
                } else {
                    chatgptDockContainer.setVisibility(View.GONE);
                }
            }

            boolean isGeminiTab = url.toLowerCase().contains("gemini.google.com") || "gemini".equalsIgnoreCase(currentTab.service);
            if (geminiDockContainer != null) {
                if (isGeminiTab && !isGeminiDockExplicitlyHidden) {
                    if (geminiDockContainer.getVisibility() != View.VISIBLE) {
                        geminiDockScroll.setVisibility(View.GONE);
                        geminiDockBall.setVisibility(View.VISIBLE);
                    }
                    geminiDockContainer.setVisibility(View.VISIBLE);
                    updateGeminiDockButtons();
                } else {
                    geminiDockContainer.setVisibility(View.GONE);
                }
            }

            if (currentTab.isIncognito) {
                omniboxHeaderWrapper.setBackgroundColor(0xFF1E102E);
            } else {
                omniboxHeaderWrapper.setBackgroundColor(isDarkTheme ? 0xFF0D1524 : 0xFFFFFFFF);
            }
        }

        int defaultIconTint = isDarkTheme ? 0xFFA2A9A9 : 0xFF4B5563;
        if (omniboxSplitBtn != null) {
            omniboxSplitBtn.setColorFilter(splitModeState != 0 ? themeAccent : defaultIconTint);
        }

        boolean anyDockActive = (ytFloatingRemoteContainer != null && ytFloatingRemoteContainer.getVisibility() == View.VISIBLE) ||
                               (searchNavContainer != null && searchNavContainer.getVisibility() == View.VISIBLE) ||
                               (chatgptDockContainer != null && chatgptDockContainer.getVisibility() == View.VISIBLE) ||
                               (geminiDockContainer != null && geminiDockContainer.getVisibility() == View.VISIBLE);
        if (omniboxToolbarsBtn != null) {
            omniboxToolbarsBtn.setColorFilter(anyDockActive ? themeAccent : defaultIconTint);
        }

        if (omniboxTabsCount != null) {
            omniboxTabsCount.setText(String.valueOf(tabsList.size()));
            omniboxTabsCount.setTextColor(themeAccent);
        }

        if (omniboxShieldIcon != null) {
            omniboxShieldIcon.setColorFilter(themeAccent);
            omniboxShieldIcon.setAlpha(adBlockShield.isEnabled() ? 1.0f : 0.4f);
        }

        if (omniboxVoiceBtn != null) {
            omniboxVoiceBtn.setColorFilter(isRecordingSpeechMode ? 0xFFFF3366 : themeAccent);
        }
    }

    private String extractQueryFromUrl(String url) {
        try {
            Uri uri = Uri.parse(url);
            String q = uri.getQueryParameter("q");
            if (q != null && !q.isEmpty()) return q;
        } catch (Exception ignored) {}
        return "";
    }

    private String cleanDisplayUrl(String url) {
        if (url == null) return "";
        if ("file:///android_asset/launch_hub.html".equalsIgnoreCase(url)) return "caspian://hub";
        try {
            Uri uri = Uri.parse(url);
            String host = uri.getHost();
            if (host != null) {
                if (host.startsWith("www.")) host = host.substring(4);
                return host + (uri.getPath() != null && !uri.getPath().equals("/") ? uri.getPath() : "");
            }
        } catch (Exception ignored) {}
        return url;
    }

    public String getOpenTabsJson() {
        JSONArray array = new JSONArray();
        for (TabItem tab : tabsList) {
            try {
                JSONObject obj = new JSONObject();
                obj.put("id", tab.id);
                obj.put("title", tab.title != null ? tab.title : "Tab " + tab.id);
                obj.put("nickname", tab.nickname);
                obj.put("url", tab.url);
                obj.put("service", tab.service);
                obj.put("active", tab.id == activeTabId);
                obj.put("isActive", tab.id == activeTabId);
                obj.put("isDesktop", tab.isDesktop);
                obj.put("isIncognito", tab.isIncognito);
                obj.put("isPlayingAudio", false);
                obj.put("isMuted", tab.isMuted);
                obj.put("isFavorite", tab.isFavorite);
                boolean isSplitTab = (splitModeState > 0 && (tab.id == activeTabId || tab.id == secondarySplitTabId));
                obj.put("isSplit", isSplitTab);
                obj.put("splitRole", (tab.id == activeTabId) ? "primary" : ((tab.id == secondarySplitTabId) ? "secondary" : "none"));
                array.put(obj);
            } catch (Exception ignored) {}
        }
        return array.toString();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupFloatingPod() {
        if (floatingCaspianCard == null) return;

        floatingCaspianCard.setOnTouchListener((view, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    if (isRecordingSpeechMode) {
                        stopSpeechToText();
                        return true;
                    }

                    dX = view.getX() - event.getRawX();
                    dY = view.getY() - event.getRawY();
                    startRawX = event.getRawX();
                    startRawY = event.getRawY();
                    isDragging = false;
                    isLongPressed = false;
                    isLongPressedInThisGesture = false;

                    longPressRunnable = () -> {
                        if (!isDragging) {
                            isLongPressed = true;
                            isLongPressedInThisGesture = true;
                            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                            playAssetSound("sfx/pop_button_v2.mp3");
                            isUniversalVoiceActive = true;
                            startSpeechToText();
                        }
                    };
                    longPressHandler.postDelayed(longPressRunnable, 450);
                    return true;

                case MotionEvent.ACTION_MOVE:
                    float deltaX = Math.abs(event.getRawX() - startRawX);
                    float deltaY = Math.abs(event.getRawY() - startRawY);
                    if (deltaX > 10 || deltaY > 10) {
                        isDragging = true;
                        if (longPressRunnable != null) longPressHandler.removeCallbacks(longPressRunnable);
                        view.animate()
                                .x(event.getRawX() + dX)
                                .y(event.getRawY() + dY)
                                .setDuration(0)
                                .start();
                    }
                    return true;

                case MotionEvent.ACTION_UP:
                    if (longPressRunnable != null) longPressHandler.removeCallbacks(longPressRunnable);

                    if (isLongPressedInThisGesture) {
                        isLongPressedInThisGesture = false;
                        return true;
                    }

                    if (!isDragging && !isLongPressed) {
                        actionButtonClickCount++;
                        SharedPreferences appPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                        appPrefs.edit().putInt("action_btn_click_count", actionButtonClickCount).apply();

                        floatingCaspianCard.animate()
                                .scaleX(0.88f)
                                .scaleY(0.88f)
                                .setDuration(100)
                                .withEndAction(() -> {
                                    floatingCaspianCard.animate()
                                            .scaleX(1.0f)
                                            .scaleY(1.0f)
                                            .setDuration(120)
                                            .setInterpolator(new OvershootInterpolator(1.2f))
                                            .start();
                                })
                                .start();
                        toggleControlSheet();
                    } else if (isDragging) {
                        // Roam freely anywhere on screen - no edge snapping
                    }
                    return true;

                case MotionEvent.ACTION_CANCEL:
                    if (longPressRunnable != null) longPressHandler.removeCallbacks(longPressRunnable);
                    isLongPressedInThisGesture = false;
                    return true;
            }
            return false;
        });
    }

    public int getActionButtonClickCount() {
        return actionButtonClickCount;
    }

    public void toggleControlSheet() {
        if (isSheetOpen) {
            hideControlSheet();
        } else {
            openControlSheet();
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupControlSheet() {
        try {
            WebSettings settings = controlWebView.getSettings();
            settings.setJavaScriptEnabled(true);
            settings.setDomStorageEnabled(true);
            settings.setAllowFileAccess(true);

            controlWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            controlWebView.setBackgroundColor(0x00000000);
            controlWebView.addJavascriptInterface(new CaspianBridge(this), "CaspianBridge");
            controlWebView.loadUrl("file:///android_asset/browser_control.html");

            sheetBackdrop.setOnClickListener(v -> hideControlSheet());
        } catch (Exception e) {
            Log.e(TAG, "setupControlSheet error: " + e.getMessage());
        }
    }

    public void openControlSheet() {
        isSheetOpen = true;
        sheetOverlayContainer.setVisibility(View.VISIBLE);
        sheetOverlayContainer.bringToFront();

        if (floatingCaspianCard != null) {
            floatingCaspianCard.setAlpha(1.0f);
            floatingCaspianCard.bringToFront();
        }

        if (ytFloatingRemoteContainer != null) ytFloatingRemoteContainer.setAlpha(0.2f);
        if (searchNavContainer != null) searchNavContainer.setAlpha(0.2f);
        if (chatgptDockContainer != null) chatgptDockContainer.setAlpha(0.2f);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int openDuration = 180;
        try {
            String openDurStr = prefs.getString("sheetOpenDuration", "180");
            openDuration = Integer.parseInt(openDurStr);
        } catch (Exception ignored) {}
        String animStyle = prefs.getString("sheetAnimationStyle", "genie");

        sheetBackdrop.animate().cancel();
        sheetBackdrop.setAlpha(0f);
        sheetBackdrop.animate()
                .alpha(1f)
                .setDuration(openDuration)
                .start();

        Runnable onOpenComplete = () -> {
            controlWebView.evaluateJavascript("if (typeof renderOpenTabs === 'function') renderOpenTabs(); if (typeof syncAppVersion === 'function') syncAppVersion(); if (typeof restoreSavedSettings === 'function') restoreSavedSettings(); if (typeof updateDevHudCounters === 'function') updateDevHudCounters();", null);
        };

        controlWebView.animate().cancel();
        if ("none".equalsIgnoreCase(animStyle) || openDuration <= 0) {
            controlWebView.setScaleX(1f);
            controlWebView.setScaleY(1f);
            controlWebView.setAlpha(1f);
            controlWebView.setTranslationY(0f);
            onOpenComplete.run();
        } else if ("genie".equalsIgnoreCase(animStyle)) {
            float buttonCenterX = floatingCaspianCard.getX() + floatingCaspianCard.getWidth() / 2f;
            float buttonCenterY = floatingCaspianCard.getY() + floatingCaspianCard.getHeight() / 2f;

            controlWebView.setPivotX(buttonCenterX);
            controlWebView.setPivotY(buttonCenterY);
            controlWebView.setScaleX(0.05f);
            controlWebView.setScaleY(0.05f);
            controlWebView.setAlpha(0f);
            controlWebView.setTranslationY(0f);

            controlWebView.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .alpha(1f)
                    .setDuration(openDuration)
                    .setInterpolator(new DecelerateInterpolator(1.8f))
                    .withEndAction(onOpenComplete)
                    .start();
        } else {
            int height = sheetOverlayContainer.getHeight();
            if (height <= 0) height = getResources().getDisplayMetrics().heightPixels;

            controlWebView.setScaleX(1f);
            controlWebView.setScaleY(1f);
            controlWebView.setAlpha(1f);
            controlWebView.setTranslationY(height);
            controlWebView.animate()
                    .translationY(0)
                    .setDuration(openDuration)
                    .setInterpolator(new PathInterpolator(0.2f, 0f, 0f, 1f))
                    .withEndAction(onOpenComplete)
                    .start();
        }

        playUiFeedbackSound("ta");
    }

    public void hideControlSheet() {
        hideControlSheet(true);
    }

    public void hideControlSheet(boolean playSound) {
        isSheetOpen = false;
        if (floatingCaspianCard != null) {
            floatingCaspianCard.setAlpha(1.0f);
            floatingCaspianCard.bringToFront();
        }
        if (ytFloatingRemoteContainer != null) ytFloatingRemoteContainer.setAlpha(1.0f);
        if (searchNavContainer != null) searchNavContainer.setAlpha(1.0f);
        if (chatgptDockContainer != null) chatgptDockContainer.setAlpha(1.0f);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int closeDuration = 160;
        try {
            String closeDurStr = prefs.getString("sheetCloseDuration", "160");
            closeDuration = Integer.parseInt(closeDurStr);
        } catch (Exception ignored) {}
        String animStyle = prefs.getString("sheetAnimationStyle", "genie");

        sheetBackdrop.animate().cancel();
        sheetBackdrop.animate()
                .alpha(0f)
                .setDuration(closeDuration)
                .start();

        controlWebView.animate().cancel();
        if ("none".equalsIgnoreCase(animStyle) || closeDuration <= 0) {
            sheetOverlayContainer.setVisibility(View.INVISIBLE);
        } else if ("genie".equalsIgnoreCase(animStyle)) {
            float buttonCenterX = floatingCaspianCard.getX() + floatingCaspianCard.getWidth() / 2f;
            float buttonCenterY = floatingCaspianCard.getY() + floatingCaspianCard.getHeight() / 2f;

            controlWebView.setPivotX(buttonCenterX);
            controlWebView.setPivotY(buttonCenterY);

            controlWebView.animate()
                    .scaleX(0.05f)
                    .scaleY(0.05f)
                    .alpha(0f)
                    .setDuration(closeDuration)
                    .setInterpolator(new PathInterpolator(0.3f, 0f, 0.8f, 0.15f))
                    .withEndAction(() -> {
                        if (!isSheetOpen) sheetOverlayContainer.setVisibility(View.INVISIBLE);
                    })
                    .start();
        } else {
            int height = sheetOverlayContainer.getHeight();
            if (height <= 0) height = getResources().getDisplayMetrics().heightPixels;

            controlWebView.animate()
                    .translationY(height)
                    .setDuration(closeDuration)
                    .setInterpolator(new PathInterpolator(0.3f, 0f, 0.8f, 0.15f))
                    .withEndAction(() -> {
                        if (!isSheetOpen) sheetOverlayContainer.setVisibility(View.INVISIBLE);
                    })
                    .start();
        }

        if (playSound) {
            playUiFeedbackSound("ta");
        }
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onBackPressed() {
        if (modalNewTabPlatform != null && modalNewTabPlatform.getVisibility() == View.VISIBLE) {
            modalNewTabPlatform.setVisibility(View.GONE);
            return;
        }
        if (tabGridOverlay != null && tabGridOverlay.getVisibility() == View.VISIBLE) {
            if (currentGridGroupId != null) {
                currentGridGroupId = null;
                renderTabGridCards(tabGridSearchInput.getText().toString());
                return;
            }
            hideTabGridView();
            return;
        }
        if (customView != null) {
            WebChromeClient client = getTabById(activeTabId) != null ? getTabById(activeTabId).webView.getWebChromeClient() : null;
            if (client != null) client.onHideCustomView();
            return;
        }
        if (isSheetOpen) {
            hideControlSheet();
            return;
        }
        TabItem currentTab = getTabById(activeTabId);
        if (currentTab != null && currentTab.webView.canGoBack()) {
            currentTab.webView.goBack();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILECHOOSER_RESULTCODE) {
            if (uploadMessage == null) return;
            uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
            uploadMessage = null;
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveOpenTabsState();
    }
}
