package com.caspian.betac;

import android.animation.ValueAnimator;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PictureInPictureParams;
import android.app.RemoteAction;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.graphics.drawable.Icon;
import android.util.Rational;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.PixelCopy;
import android.view.VelocityTracker;
import android.webkit.RenderProcessGoneDetail;
import java.util.function.Consumer;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.widget.PopupWindow;
import android.widget.ScrollView;
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
import android.os.PowerManager;
import android.os.SystemClock;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.PathInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
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
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.RelativeLayout;
import android.media.AudioManager;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewFeature;
import androidx.core.content.FileProvider;
import android.provider.MediaStore;
import android.webkit.PermissionRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
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
        public boolean isPlayingAudio = false;
        public boolean isFavorite = false;
        public boolean isIncognito = false;
        public String pendingPrompt = null;
        public Bitmap snapshotBitmap = null;
        public String faviconB64 = null;
        public String favicon64 = null;
        public String touchIconUrl = null;
        public String caskId = CaskManager.DEFAULT_CASK_ID;
        public String caskName = "Caspian Cask";
        public String caskIcon = "🌊";
        public String caskColor = "#1B4264";
        public int splitPartnerId = -1;
        public int splitOrientation = 1;
        public String splitRole = "";
        public String splitName = "";

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
    private final Set<String> collapsedStripGroupIds = new HashSet<>();
    private final Set<Integer> selectedGridTabIds = new HashSet<>();

    public static class MultiTabDragState {
        public final List<Integer> tabIds = new ArrayList<>();
        public final int anchorTabId;
        public MultiTabDragState(java.util.Collection<Integer> ids, int anchor) {
            this.tabIds.addAll(ids);
            this.anchorTabId = anchor;
        }
    }

    public static class ClosedTabRecord {
        public String service;
        public String pendingPrompt;
        public String url;
        public boolean isIncognito;
        public String title;
        public String caskId;
        public boolean isFavorite;
        public String faviconB64;
        public String favicon64;
        public String touchIconUrl;

        public ClosedTabRecord(TabItem tab) {
            this.service = tab.service;
            this.pendingPrompt = tab.pendingPrompt;
            this.url = tab.url;
            this.isIncognito = tab.isIncognito;
            this.title = tab.title;
            this.caskId = tab.caskId;
            this.isFavorite = tab.isFavorite;
            this.faviconB64 = tab.faviconB64;
            this.favicon64 = tab.favicon64;
            this.touchIconUrl = tab.touchIconUrl;
        }
    }

    private final List<List<ClosedTabRecord>> closedTabBatches = new ArrayList<>();
    private int activeTabId = 1;
    private int secondarySplitTabId = -1;
    private int nextTabId = 2;
    private String currentGridGroupId = null;
    
    private int splitModeState = 0; // 0 = single, 1 = horizontal, 2 = vertical
    private boolean openLeftLinksToRight = false;
    private float splitRatio = 0.5f;
    private boolean isSheetOpen = false;
    private boolean isDownloadsModalOpen = false;
    private boolean isMasterSfxMuted = false;

    private WaveguardShield waveguardShield;
    private AICommandRouter.SearchEngine currentSearchEngine = AICommandRouter.SearchEngine.GOOGLE;

    private FrameLayout rootContainer;
    private FrameLayout browserContentLayout;
    private FrameLayout omniboxHeaderWrapper;
    private View omniboxTabStripBar;
    private HorizontalScrollView omniboxTabStripScroll;
    private LinearLayout omniboxTabStripTabs;
    private View btnOmniboxAddTab;
    private LinearLayout omniboxHeader;
    private FrameLayout omniboxCapsule;
    private ImageButton omniboxBackBtn;
    private ImageButton omniboxForwardBtn;
    private boolean isDarkTheme = true;
    private String omniboxPosition = "top";
    private String omniboxMenuStyle = "grid";
    private boolean isTabStripEnabled = true;

    private View btnOmniboxUndoCloseTab;
    private TextView iconOmniboxUndoClose;
    private TextView textOmniboxUndoClose;
    private final Handler tabStripUndoHandler = new Handler(Looper.getMainLooper());
    private final Runnable tabStripUndoDismissRunnable = this::dismissTabStripUndoButton;
    private BookmarkManager bookmarkManager;
    
    private LinearLayout omniboxUrlContainer;
    private FrameLayout omniboxShieldBtn;
    private ImageView omniboxShieldIcon;
    private EditText omniboxEditText;
    private TextView omniboxPasteBtn;
    private ImageButton omniboxClearBtn;
    private ImageButton omniboxFinderBtn;
    private ImageButton omniboxVoiceBtn;
    private View omniboxDividerLeft;
    private View omniboxDividerRight;

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
    private View tabGridHeaderRow;
    private View tabGridHeaderCapsule;
    private TextView tabGridCountBadge;
    private ImageButton tabGridIncognitoBtn;
    private ImageButton tabGridCloseViewBtn;
    private LinearLayout tabGridTopActions;
    private TextView btnTabGridTopSplit;
    private TextView btnTabGridTopDeselect;
    private TextView btnTabGridTopDelete;
    private LinearLayout tabGridSearchBox;
    private ImageView tabGridSearchIcon;
    private EditText tabGridSearchInput;
    private TextView btnTabGridFavorite;
    private View btnTabGridFilter;
    private TextView tabGridFilterLabel;
    private String currentTabGridFilter = "all"; // "all", "groups", "single"
    private LinearLayout tabGridGroupBanner;
    private View tabGridGroupColorDot;
    private TextView tabGridGroupBannerTitle;
    private TextView tabGridGroupCountBadge;
    private TextView btnTabGridGroupClose;
    private Button btnTabGridGroupEdit;
    private Button btnTabGridGroupUngroup;
    private Button btnTabGridGroupDelete;
    private LinearLayout tabGridContentLayout;
    private GridLayout tabGridContainer;
    private LinearLayout tabGridBottomDock;
    private TextView btnTabDockMakeGroup;
    private FrameLayout tabGridFabAdd;
    private TextView btnTabDockSelect;
    private boolean isGridSelectionMode = false;

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
    private VelocityTracker cabVelocityTracker;
    private ImageView floatingCaspianIcon;
    private CabRadialMenuView cabRadialMenu;
    private WhirlpoolOverlayView currentWhirlpoolOverlay;
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
    private ImageButton ytRemoteTimeline;
    private ImageButton ytRemotePip;
    public static final String ACTION_PIP_PLAY_PAUSE = "com.caspian.betac.ACTION_PIP_PLAY_PAUSE";
    public static final String ACTION_PIP_REWIND = "com.caspian.betac.ACTION_PIP_REWIND";
    public static final String ACTION_PIP_FORWARD = "com.caspian.betac.ACTION_PIP_FORWARD";
    public static final String ACTION_MEDIA_PLAY_PAUSE = "com.caspian.betac.MEDIA_PLAY_PAUSE";
    public static final String ACTION_MEDIA_PREVIOUS = "com.caspian.betac.MEDIA_PREVIOUS";
    public static final String ACTION_MEDIA_NEXT = "com.caspian.betac.MEDIA_NEXT";
    public static final String ACTION_MEDIA_REPEAT = "com.caspian.betac.MEDIA_REPEAT";
    public static final String ACTION_MEDIA_SHUFFLE = "com.caspian.betac.MEDIA_SHUFFLE";
    public static final String ACTION_MEDIA_REWIND = "com.caspian.betac.MEDIA_REWIND";
    public static final String ACTION_MEDIA_FORWARD = "com.caspian.betac.MEDIA_FORWARD";
    public static final String ACTION_MEDIA_DISMISS = "com.caspian.betac.MEDIA_DISMISS";
    public static final String ACTION_LOG_PAUSE_RESUME = "com.caspian.betac.LOG_PAUSE_RESUME";
    public static final String ACTION_LOG_STOP_SAVE = "com.caspian.betac.LOG_STOP_SAVE";

    private static final String CHANNEL_MEDIA_ID = "caspian_media_playback";
    private static final int NOTIFICATION_ID_MEDIA = 7001;
    private static final String CHANNEL_LOGGER_ID = "caspian_system_logger";
    private static final int NOTIFICATION_ID_LOGGER = 9002;

    private MediaSessionCompat mediaSession;
    private String currentMediaTitle = "YouTube";
    private String currentMediaArtist = "";
    private int currentMediaRepeatMode = PlaybackStateCompat.REPEAT_MODE_NONE;
    private int currentMediaShuffleMode = PlaybackStateCompat.SHUFFLE_MODE_NONE;
    private String currentMediaThumbUrl = "";
    private Bitmap currentMediaThumbBitmap = null;
    private boolean isDebugRecordingPaused = false;
    private boolean hasYouTubePlaybackStarted = false;
    private BroadcastReceiver pipActionReceiver;
    private ImageButton ytRemoteLock;
    private ImageButton ytRemoteSettings;
    private TextView ytRemoteVolumeBtn;
    private LinearLayout ytFloatingTimelineBar;
    private FrameLayout ytTimelineScrubBubbleContainer;
    private TextView ytTimelineScrubBubble;
    private ImageButton ytTimelinePlayPause;
    private TextView ytTimelineCurrentTime;
    private SeekBar ytTimelineSeekbar;
    private TextView ytTimelineTotalTime;
    private ImageButton ytTimelineCollapse;
    private boolean isTimelineUserEnabled = false;
    private boolean isUserScrubbingTimeline = false;
    private double currentVideoTime = 0;
    private double currentVideoDuration = 0;
    private long lastMediaSessionTimeUpdateMs = 0;
    private View videoTouchLockOverlay;
    private boolean isScreenTouchLocked = false;
    private PopupWindow volumePopupWindow;
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
    private PowerManager.WakeLock youtubeWakeLock;

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
    private boolean isYtRemoteAutoCollapse = false;
    private boolean isChatgptDockAutoCollapse = false;
    private boolean isGeminiDockAutoCollapse = false;

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

    private volatile boolean isWhisperDownloading = false;
    private volatile int whisperDownloadProgress = 0;
    private volatile String currentlyDownloadingTier = "tiny";

    private static final String WHISPER_TINY_URL = "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-tiny.en-q5_1.bin";
    private static final String WHISPER_TINY_FILENAME = "whisper-tiny-q5_1.bin";

    private static final String WHISPER_BASE_URL = "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-base.en-q5_1.bin";
    private static final String WHISPER_BASE_FILENAME = "whisper-base-q5_1.bin";

    private boolean isDebugRecording = false;
    private final StringBuilder debugLogBuffer = new StringBuilder();

    private View customView;
    private WebChromeClient.CustomViewCallback customViewCallback;
    private FrameLayout fullscreenContainer;

    private ValueCallback<Uri[]> uploadMessage;
    private final static int FILECHOOSER_RESULTCODE = 1;
    private final static int REQUEST_CODE_PDF_PICKER = 9182;
    private final static int REQUEST_CODE_EXPORT_BOOKMARKS_TREE = 9410;
    private final static int REQUEST_CODE_IMPORT_BOOKMARKS_FILE = 9411;
    private Runnable currentBookmarksRefreshRunnable = null;
    private Uri cameraCapturedUri = null;
    private PermissionRequest pendingWebPermissionRequest = null;
    private static final int WEBVIEW_PERMISSION_REQUEST_CODE = 9021;

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

        try { android.webkit.WebIconDatabase.getInstance().open(getDir("icons", MODE_PRIVATE).getPath()); } catch (Throwable ignored) {}
        try { WebView.setWebContentsDebuggingEnabled(true); } catch (Throwable ignored) {}
        try { waveguardShield = new WaveguardShield(this); } catch (Throwable ignored) {}
        try { initSoundPool(); } catch (Throwable ignored) {}
        try { bindViews(); } catch (Throwable ignored) {}
        try { loadPodPreferences(); } catch (Throwable ignored) {}
        try { loadTabGroups(); } catch (Throwable ignored) {}
        try { bookmarkManager = new BookmarkManager(this); } catch (Throwable ignored) {}
        try { initDownloadManager(); } catch (Throwable ignored) {}

        try {
            SharedPreferences appPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            actionButtonClickCount = appPrefs.getInt("action_btn_click_count", 0);
            isYtRemoteExplicitlyHidden = !appPrefs.getBoolean("yt_dock_enabled", true);
            isSearchNavExplicitlyHidden = !appPrefs.getBoolean("google_dock_enabled", false);
            isChatgptDockExplicitlyHidden = !appPrefs.getBoolean("chatgpt_dock_enabled", true);
            isGeminiDockExplicitlyHidden = !appPrefs.getBoolean("gemini_dock_enabled", true);
            isGoogleDockAutoCollapse = appPrefs.getBoolean("google_dock_autocollapse", true);
            isYtRemoteAutoCollapse = appPrefs.getBoolean("yt_pod_autocollapse", false);
            isChatgptDockAutoCollapse = appPrefs.getBoolean("chatgpt_dock_autocollapse", false);
            isGeminiDockAutoCollapse = appPrefs.getBoolean("gemini_dock_autocollapse", false);
            isDarkTheme = !"light".equalsIgnoreCase(appPrefs.getString("theme", "dark"));
        } catch (Throwable ignored) {}

        try { updateThemeStyling(); } catch (Throwable ignored) {}
        try { setupOmniboxListeners(); } catch (Throwable ignored) {}
        try { setupFloatingPod(); } catch (Throwable ignored) {}
        try { setupControlSheet(); } catch (Throwable ignored) {}
        try { setupVoiceVisualizer(); } catch (Throwable ignored) {}
        try { setupLiquidGlassYouTubeRemote(); } catch (Throwable ignored) {}
        try { setupLiquidGlassGoogleDock(); } catch (Throwable ignored) {}
        try { setupPiPActionsReceiver(); } catch (Throwable ignored) {}
        try { setupMediaSession(); } catch (Throwable ignored) {}
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1004);
            }
        }
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
            CaskManager caskManager = new CaskManager(this);
            caskManager.restoreCaskCookiesFromVault(caskManager.getActiveCaskId());
        } catch (Throwable ignored) {}

        try {
            restoreOpenTabsState();
        } catch (Throwable t) {
            Log.e(TAG, "restoreOpenTabsState error: ", t);
        }

        try {
            handleIncomingPdfIntent(getIntent());
        } catch (Throwable t) {
            Log.e(TAG, "handleIncomingPdfIntent error: ", t);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        try {
            handleIncomingPdfIntent(intent);
        } catch (Throwable t) {
            Log.e(TAG, "handleIncomingPdfIntent onNewIntent error: ", t);
        }
    }

    public void openPdfPicker() {
        try {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/pdf");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            startActivityForResult(intent, REQUEST_CODE_PDF_PICKER);
        } catch (Exception e) {
            try {
                Intent fallback = new Intent(Intent.ACTION_GET_CONTENT);
                fallback.setType("application/pdf");
                fallback.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(Intent.createChooser(fallback, "Select PDF(s)"), REQUEST_CODE_PDF_PICKER);
            } catch (Exception ex) {
                Toast.makeText(this, "No file picker available", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PDF_PICKER && resultCode == RESULT_OK && data != null) {
            List<Uri> selectedUris = new ArrayList<>();
            if (data.getClipData() != null) {
                ClipData clipData = data.getClipData();
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    Uri uri = clipData.getItemAt(i).getUri();
                    if (uri != null) selectedUris.add(uri);
                }
            } else if (data.getData() != null) {
                selectedUris.add(data.getData());
            }

            for (Uri uri : selectedUris) {
                openPdfFromUriInNewTab(uri);
            }
            return;
        }

        if (requestCode == REQUEST_CODE_EXPORT_BOOKMARKS_TREE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            handleExportBookmarksToFolder(data.getData());
            return;
        }

        if (requestCode == REQUEST_CODE_IMPORT_BOOKMARKS_FILE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            handleImportBookmarksFromFile(data.getData());
            return;
        }

        if (requestCode == FILECHOOSER_RESULTCODE) {
            if (uploadMessage == null) return;
            Uri[] results = null;
            if (resultCode == RESULT_OK) {
                if (data == null || (data.getData() == null && data.getClipData() == null)) {
                    if (cameraCapturedUri != null) {
                        results = new Uri[]{ cameraCapturedUri };
                    }
                } else {
                    results = WebChromeClient.FileChooserParams.parseResult(resultCode, data);
                    if (results == null && data.getData() != null) {
                        results = new Uri[]{ data.getData() };
                    }
                }
            }
            uploadMessage.onReceiveValue(results);
            uploadMessage = null;
            cameraCapturedUri = null;
            return;
        }

        if (requestCode == SAVE_LOG_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            try {
                Uri uri = data.getData();
                OutputStream os = getContentResolver().openOutputStream(uri);
                if (os != null) {
                    os.write(pendingLogDataToSave.getBytes(StandardCharsets.UTF_8));
                    os.close();
                    Toast.makeText(this, "✅ Log successfully saved to chosen location!", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Error saving log file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void handleIncomingPdfIntent(Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();
        List<Uri> pdfUris = new ArrayList<>();

        if (Intent.ACTION_VIEW.equals(action)) {
            if (intent.getData() != null) {
                pdfUris.add(intent.getData());
            }
        } else if (Intent.ACTION_SEND.equals(action)) {
            if (intent.hasExtra(Intent.EXTRA_STREAM)) {
                try {
                    Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                    if (uri != null) pdfUris.add(uri);
                } catch (Exception ignored) {}
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action)) {
            if (intent.hasExtra(Intent.EXTRA_STREAM)) {
                try {
                    ArrayList<Uri> uris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                    if (uris != null) {
                        pdfUris.addAll(uris);
                    }
                } catch (Exception ignored) {}
            }
        }

        // Also check ClipData (frequently used by newer Android file pickers/shares)
        if (intent.getClipData() != null) {
            ClipData clip = intent.getClipData();
            for (int i = 0; i < clip.getItemCount(); i++) {
                Uri u = clip.getItemAt(i).getUri();
                if (u != null && !pdfUris.contains(u)) {
                    pdfUris.add(u);
                }
            }
        }

        for (Uri uri : pdfUris) {
            openPdfFromUriInNewTab(uri);
        }
    }

    public void openPdfFromUriInNewTab(Uri pdfUri) {
        if (pdfUri == null) return;
        String displayName = "Document.pdf";
        try {
            if ("content".equalsIgnoreCase(pdfUri.getScheme())) {
                try (android.database.Cursor cursor = getContentResolver().query(pdfUri, null, null, null, null)) {
                    if (cursor != null && cursor.moveToFirst()) {
                        int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                        if (nameIndex != -1) {
                            String name = cursor.getString(nameIndex);
                            if (name != null && !name.trim().isEmpty()) {
                                displayName = name;
                            }
                        }
                    }
                }
            } else if ("file".equalsIgnoreCase(pdfUri.getScheme())) {
                String lastSeg = pdfUri.getLastPathSegment();
                if (lastSeg != null && !lastSeg.trim().isEmpty()) {
                    displayName = lastSeg;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error resolving PDF display name: " + e.getMessage());
        }

        if (!displayName.toLowerCase().endsWith(".pdf")) {
            displayName += ".pdf";
        }

        try {
            File pdfDir = new File(getCacheDir(), "pdf_cache");
            if (!pdfDir.exists()) pdfDir.mkdirs();
            String safeFileName = System.currentTimeMillis() + "_" + displayName.replaceAll("[^a-zA-Z0-9._-]", "_");
            File targetFile = new File(pdfDir, safeFileName);

            try (InputStream in = getContentResolver().openInputStream(pdfUri);
                 FileOutputStream out = new FileOutputStream(targetFile)) {
                if (in == null) return;
                byte[] buffer = new byte[8192];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                out.flush();
            }

            openCachedPdfInNewTab(targetFile.getAbsolutePath(), displayName);
        } catch (Exception e) {
            Log.e(TAG, "Failed to copy incoming PDF: " + e.getMessage());
            Toast.makeText(this, "Unable to open PDF", Toast.LENGTH_SHORT).show();
        }
    }

    public void openCachedPdfInNewTab(String absolutePath, String displayName) {
        int id = nextTabId++;
        String encodedPath = Uri.encode(absolutePath);
        String encodedTitle = Uri.encode(displayName);
        String viewerUrl = "file:///android_asset/pdf_viewer.html?file=" + encodedPath + "&title=" + encodedTitle;

        CaskManager cm = new CaskManager(this);
        String caskId = cm.getActiveCaskId();
        TabItem tab = createNewTabInstance(id, viewerUrl, "pdf", null, false, caskId);
        tab.title = displayName;
        tabsList.add(tab);
        switchToTab(id);
        saveOpenTabsState();
        Toast.makeText(this, "Opened PDF: " + displayName, Toast.LENGTH_SHORT).show();
    }

    public void handleAskAiFromPdf(String selectedText, String targetService) {
        String prompt = "Explain this concept in simple terms from the document:\n\n\"" + selectedText + "\"";

        if ("split".equalsIgnoreCase(targetService)) {
            // Open Split Arena: Active PDF on the Left, ChatGPT on the Right
            int id = nextTabId++;
            TabItem gptTab = createNewTabInstance(id, "https://chatgpt.com", "chatgpt", prompt, false);
            gptTab.title = "ChatGPT";
            tabsList.add(gptTab);

            secondarySplitTabId = gptTab.id;
            splitModeState = 1;
            splitRatio = 0.5f;
            applySplitViewLayout();
            saveOpenTabsState();
            Toast.makeText(this, "Split Screen Study Active!", Toast.LENGTH_SHORT).show();
        } else if ("gemini".equalsIgnoreCase(targetService)) {
            addNewTab("gemini", prompt, "https://gemini.google.com/app", false);
        } else {
            addNewTab("chatgpt", prompt, "https://chatgpt.com", false);
        }
    }

    public void handleAskAiFromPdfWithImage(String selectedText, String base64Image, String targetService) {
        handleAskAiFromPdf(selectedText, targetService);
    }

    public void launchGoogleLensWithBase64(String base64Image) {
        if (base64Image == null || base64Image.isEmpty()) return;
        try {
            String cleanB64 = base64Image;
            int commaIdx = cleanB64.indexOf(",");
            if (commaIdx >= 0) {
                cleanB64 = cleanB64.substring(commaIdx + 1);
            }
            byte[] bytes = android.util.Base64.decode(cleanB64, android.util.Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            launchGoogleLensWithBitmap(bitmap);
        } catch (Exception e) {
            Log.e(TAG, "decodeBase64 for Lens error: " + e.getMessage());
            Toast.makeText(this, "Unable to process image for Google Lens", Toast.LENGTH_SHORT).show();
        }
    }

    public void launchGoogleLensWithBitmap(Bitmap bitmap) {
        if (bitmap == null) return;
        try {
            File cacheDir = new File(getCacheDir(), "whirlpool");
            if (!cacheDir.exists()) cacheDir.mkdirs();
            File imageFile = new File(cacheDir, "lens_query.png");
            try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            }
            Uri contentUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", imageFile);

            // 1. Google Lens direct intent
            Intent lensIntent = new Intent(Intent.ACTION_SEND);
            lensIntent.setType("image/png");
            lensIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            lensIntent.setPackage("com.google.android.apps.lens");
            lensIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            if (lensIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(lensIntent);
                return;
            }

            // 2. Google Search App (GSA) QuickSearchBox
            Intent gsaIntent = new Intent(Intent.ACTION_SEND);
            gsaIntent.setType("image/png");
            gsaIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            gsaIntent.setPackage("com.google.android.googlequicksearchbox");
            gsaIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            if (gsaIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(gsaIntent);
                return;
            }

            // 3. Chooser or fallback
            Intent chooser = new Intent(Intent.ACTION_SEND);
            chooser.setType("image/png");
            chooser.putExtra(Intent.EXTRA_STREAM, contentUri);
            chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(chooser, "Search with Google Lens..."));
        } catch (Exception e) {
            Log.e(TAG, "launchGoogleLens error: " + e.getMessage());
            Toast.makeText(this, "Google Lens search error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void searchGoogleWithText(String text) {
        if (text == null || text.trim().isEmpty()) return;
        addNewTab("google", "", "https://www.google.com/search?q=" + Uri.encode(text.trim()), false);
    }

    public void copyImageToClipboard(String base64Image) {
        if (base64Image == null || base64Image.isEmpty()) return;
        try {
            String cleanB64 = base64Image;
            int commaIdx = cleanB64.indexOf(",");
            if (commaIdx >= 0) {
                cleanB64 = cleanB64.substring(commaIdx + 1);
            }
            byte[] bytes = android.util.Base64.decode(cleanB64, android.util.Base64.DEFAULT);
            File cacheDir = new File(getCacheDir(), "whirlpool");
            if (!cacheDir.exists()) cacheDir.mkdirs();
            File imageFile = new File(cacheDir, "copied_crop.png");
            try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                fos.write(bytes);
            }
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", imageFile);
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm != null) {
                ClipData clip = ClipData.newUri(getContentResolver(), "Caspian Whirlpool Image", uri);
                cm.setPrimaryClip(clip);
                Toast.makeText(this, "Copied image crop to clipboard", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "copyImageToClipboard error: " + e.getMessage());
        }
    }

    public void copyImageToClipboard(Bitmap bitmap) {
        if (bitmap == null) return;
        try {
            File cacheDir = new File(getCacheDir(), "whirlpool");
            if (!cacheDir.exists()) cacheDir.mkdirs();
            File imageFile = new File(cacheDir, "copied_crop.png");
            try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            }
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", imageFile);
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm != null) {
                ClipData clip = ClipData.newUri(getContentResolver(), "Caspian Whirlpool Image", uri);
                cm.setPrimaryClip(clip);
                Toast.makeText(this, "Copied image crop to clipboard", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "copyImageToClipboard bitmap error: " + e.getMessage());
        }
    }

    public void onWhirlpoolDismissed() {
        currentWhirlpoolOverlay = null;
        if (floatingCaspianCard != null) {
            float highElevation = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 600, getResources().getDisplayMetrics());
            floatingCaspianCard.setElevation(highElevation);
            floatingCaspianCard.setCardElevation(highElevation);
            floatingCaspianCard.bringToFront();
        }
    }

    public void launchChatGPTWithBitmap(Bitmap bitmap) {
        if (bitmap == null) return;
        try {
            File cacheDir = new File(getCacheDir(), "whirlpool");
            if (!cacheDir.exists()) cacheDir.mkdirs();
            File imageFile = new File(cacheDir, "gpt_query.png");
            try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            }
            Uri contentUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", imageFile);

            // Copy image URI to clipboard so it is immediately pasteable in web or app
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm != null) {
                ClipData clip = ClipData.newUri(getContentResolver(), "Caspian Crop", contentUri);
                cm.setPrimaryClip(clip);
            }

            // Check if ChatGPT official app is installed
            Intent gptIntent = new Intent(Intent.ACTION_SEND);
            gptIntent.setType("image/png");
            gptIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            gptIntent.setPackage("com.openai.chatgpt");
            gptIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            if (gptIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(gptIntent);
                Toast.makeText(this, "Sent image crop to ChatGPT", Toast.LENGTH_SHORT).show();
                return;
            }

            // Otherwise open ChatGPT in Caspian Flow browser tab with image ready on clipboard
            addNewTab("chatgpt", "", "https://chatgpt.com", false);
            Toast.makeText(this, "Image copied to clipboard! Ready to paste into ChatGPT.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e(TAG, "launchChatGPTWithBitmap error: " + e.getMessage());
            Toast.makeText(this, "ChatGPT error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void launchGeminiWithBitmap(Bitmap bitmap) {
        if (bitmap == null) return;
        try {
            File cacheDir = new File(getCacheDir(), "whirlpool");
            if (!cacheDir.exists()) cacheDir.mkdirs();
            File imageFile = new File(cacheDir, "gemini_query.png");
            try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            }
            Uri contentUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", imageFile);

            // Copy image URI to clipboard
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm != null) {
                ClipData clip = ClipData.newUri(getContentResolver(), "Caspian Crop", contentUri);
                cm.setPrimaryClip(clip);
            }

            // Check if Gemini app is installed
            Intent geminiIntent = new Intent(Intent.ACTION_SEND);
            geminiIntent.setType("image/png");
            geminiIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            geminiIntent.setPackage("com.google.android.apps.bard");
            geminiIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            if (geminiIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(geminiIntent);
                Toast.makeText(this, "Sent image crop to Gemini", Toast.LENGTH_SHORT).show();
                return;
            }

            // Otherwise open Gemini in Caspian Flow browser tab
            addNewTab("gemini", "", "https://gemini.google.com/app", false);
            Toast.makeText(this, "Image copied to clipboard! Ready to paste into Gemini.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e(TAG, "launchGeminiWithBitmap error: " + e.getMessage());
            Toast.makeText(this, "Gemini error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void launchSplitWithBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            copyImageToClipboard(bitmap);
        }
        int id = nextTabId++;
        TabItem gptTab = createNewTabInstance(id, "https://chatgpt.com", "chatgpt", "", false);
        gptTab.title = "ChatGPT";
        tabsList.add(gptTab);

        secondarySplitTabId = gptTab.id;
        splitModeState = 1;
        splitRatio = 0.5f;
        applySplitViewLayout();
        saveOpenTabsState();
        Toast.makeText(this, "Image copied to clipboard! Split Screen Active.", Toast.LENGTH_SHORT).show();
    }

    public void startCaspianWhirlpool() {
        TabItem currentTab = getTabById(activeTabId);
        if (currentTab == null || currentTab.webView == null) {
            Toast.makeText(this, "No active tab to search", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hide CAB and radial dial during screen capture so they don't get stamped into the screenshot
        if (floatingCaspianCard != null) floatingCaspianCard.setVisibility(View.INVISIBLE);
        if (cabRadialMenu != null) cabRadialMenu.setVisibility(View.INVISIBLE);

        // Capture screen bitmap using PixelCopy for 100% hardware-accelerated GPU fidelity (PDF, Canvas, WebGL, Text)
        captureWindowBitmapForWhirlpool(bitmap -> {
            if (floatingCaspianCard != null) {
                floatingCaspianCard.setVisibility(View.VISIBLE);
            }

            if (bitmap == null) {
                Toast.makeText(this, "Could not capture screen for Whirlpool", Toast.LENGTH_SHORT).show();
                return;
            }

            WhirlpoolOverlayView overlay = new WhirlpoolOverlayView(this, bitmap);
            currentWhirlpoolOverlay = overlay;
            if (rootContainer != null) {
                rootContainer.addView(overlay, new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                ));
            }

            // Elevate CAB so it stays on top of Whirlpool and single-tapping it cancels Whirlpool
            if (floatingCaspianCard != null) {
                floatingCaspianCard.bringToFront();
                float highElevation = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 600, getResources().getDisplayMetrics());
                floatingCaspianCard.setElevation(highElevation);
                floatingCaspianCard.setCardElevation(highElevation);
            }

            Toast.makeText(this, "🌀 Caspian Whirlpool: Circle or drag to search", Toast.LENGTH_SHORT).show();
        });
    }

    private void captureWindowBitmapForWhirlpool(Consumer<Bitmap> onCaptured) {
        if (rootContainer == null) {
            onCaptured.accept(null);
            return;
        }

        int width = rootContainer.getWidth();
        int height = rootContainer.getHeight();
        if (width <= 0) width = 1080;
        if (height <= 0) height = 1920;

        // On Android 8.0+ (API 26+), PixelCopy copies the exact GPU surface buffer from Window/SurfaceFlinger
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && getWindow() != null) {
            try {
                Bitmap destBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                int[] loc = new int[2];
                rootContainer.getLocationInWindow(loc);
                Rect srcRect = new Rect(loc[0], loc[1], loc[0] + width, loc[1] + height);

                // Use post to ensure CAB hide layout pass is completed before capturing GPU front buffer
                final int finalW = width;
                final int finalH = height;
                rootContainer.post(() -> {
                    try {
                        PixelCopy.request(getWindow(), srcRect, destBitmap, copyResult -> {
                            if (copyResult == PixelCopy.SUCCESS) {
                                onCaptured.accept(destBitmap);
                            } else {
                                Log.w(TAG, "PixelCopy returned code: " + copyResult + ", falling back to software capture");
                                onCaptured.accept(fallbackSoftwareCapture(finalW, finalH));
                            }
                        }, new Handler(Looper.getMainLooper()));
                    } catch (Exception e) {
                        Log.e(TAG, "PixelCopy request failed: " + e.getMessage());
                        onCaptured.accept(fallbackSoftwareCapture(finalW, finalH));
                    }
                });
                return;
            } catch (Exception e) {
                Log.e(TAG, "PixelCopy setup failed: " + e.getMessage());
            }
        }

        // Fallback for API < 26
        onCaptured.accept(fallbackSoftwareCapture(width, height));
    }

    private Bitmap fallbackSoftwareCapture(int width, int height) {
        try {
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(0xFF050811);

            TabItem currentTab = getTabById(activeTabId);
            if (omniboxHeaderWrapper != null && omniboxHeaderWrapper.getVisibility() == View.VISIBLE) {
                int[] hdrLoc = new int[2];
                omniboxHeaderWrapper.getLocationInWindow(hdrLoc);
                int[] rootLoc = new int[2];
                if (rootContainer != null) rootContainer.getLocationInWindow(rootLoc);
                int offX = Math.max(0, hdrLoc[0] - rootLoc[0]);
                int offY = Math.max(0, hdrLoc[1] - rootLoc[1]);

                canvas.save();
                canvas.translate(offX, offY);
                omniboxHeaderWrapper.draw(canvas);
                canvas.restore();
            }

            if (currentTab != null && currentTab.webView != null) {
                int[] wvLoc = new int[2];
                currentTab.webView.getLocationInWindow(wvLoc);
                int[] rootLoc = new int[2];
                if (rootContainer != null) rootContainer.getLocationInWindow(rootLoc);
                int offX = Math.max(0, wvLoc[0] - rootLoc[0]);
                int offY = Math.max(0, wvLoc[1] - rootLoc[1]);

                canvas.save();
                canvas.translate(offX, offY);
                currentTab.webView.draw(canvas);
                canvas.restore();
            }
            return bitmap;
        } catch (Exception e) {
            Log.e(TAG, "fallbackSoftwareCapture error: " + e.getMessage());
            return null;
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
                obj.put("splitPartnerId", tab.splitPartnerId);
                obj.put("splitRole", tab.splitRole != null ? tab.splitRole : "");
                obj.put("splitOrientation", tab.splitOrientation);
                obj.put("splitName", tab.splitName != null ? tab.splitName : "");
                obj.put("caskId", tab.caskId != null ? tab.caskId : CaskManager.DEFAULT_CASK_ID);
                obj.put("caskName", tab.caskName != null ? tab.caskName : "Caspian Cask");
                obj.put("caskIcon", tab.caskIcon != null ? tab.caskIcon : "🌊");
                obj.put("caskColor", tab.caskColor != null ? tab.caskColor : "#1B4264");
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
                        String caskId = obj.optString("caskId", CaskManager.DEFAULT_CASK_ID);

                        TabItem item = createNewTabInstance(id, url, service, null, isIncognito, caskId);
                        item.title = title;
                        item.nickname = nickname;
                        item.isDesktop = isDesktop;
                        item.isMuted = isMuted;
                        item.isFavorite = isFavorite;
                        item.splitPartnerId = obj.optInt("splitPartnerId", -1);
                        item.splitRole = obj.optString("splitRole", "");
                        item.splitOrientation = obj.optInt("splitOrientation", 0);
                        item.splitName = obj.optString("splitName", "");
                        item.caskId = caskId;
                        item.caskName = obj.optString("caskName", "Caspian Cask");
                        item.caskIcon = obj.optString("caskIcon", "🌊");
                        item.caskColor = obj.optString("caskColor", "#1B4264");
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

    public void onTabGroupsSynced() {
        loadTabGroups();
        updateOmniboxTabStrip();
        if (tabGridSearchInput != null) {
            renderTabGridCards(tabGridSearchInput.getText().toString());
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
        if (controlWebView != null) {
            controlWebView.post(() -> controlWebView.evaluateJavascript(
                    "if (typeof reloadTabGroups === 'function') reloadTabGroups(); else if (typeof renderOpenTabs === 'function') renderOpenTabs();", null));
        }
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
            browserContentLayout = findViewById(R.id.browser_content_layout);
            omniboxHeaderWrapper = findViewById(R.id.omnibox_header_wrapper);
            omniboxTabStripBar = findViewById(R.id.omnibox_tab_strip_bar);
            omniboxTabStripScroll = findViewById(R.id.omnibox_tab_strip_scroll);
            omniboxTabStripTabs = findViewById(R.id.omnibox_tab_strip_tabs);
            btnOmniboxAddTab = findViewById(R.id.btn_omnibox_add_tab);
            if (btnOmniboxAddTab != null) {
                btnOmniboxAddTab.setOnClickListener(v -> {
                    playUiFeedbackSound("tap");
                    addNewTab("hub", null);
                });
            }

            btnOmniboxUndoCloseTab = findViewById(R.id.btn_omnibox_undo_close_tab);
            iconOmniboxUndoClose = findViewById(R.id.icon_omnibox_undo_close);
            textOmniboxUndoClose = findViewById(R.id.text_omnibox_undo_close);
            if (btnOmniboxUndoCloseTab != null) {
                btnOmniboxUndoCloseTab.setOnClickListener(v -> {
                    playUiFeedbackSound("tap");
                    restoreLastClosedTab();
                    updateOmniboxTabStrip();
                    if (hasClosedTabsToUndo()) {
                        tabStripUndoHandler.removeCallbacks(tabStripUndoDismissRunnable);
                        tabStripUndoHandler.postDelayed(tabStripUndoDismissRunnable, 30000);
                    } else {
                        dismissTabStripUndoButton();
                    }
                });
            }

            omniboxPosition = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getString("omnibox_position", "top");
            omniboxMenuStyle = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getString("omnibox_menu_style", "grid");
            isTabStripEnabled = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getBoolean("tab_strip_enabled", true);
            omniboxHeader = findViewById(R.id.omnibox_header);
            omniboxCapsule = findViewById(R.id.omnibox_capsule);
            omniboxBackBtn = findViewById(R.id.omnibox_back_btn);
            omniboxForwardBtn = findViewById(R.id.omnibox_forward_btn);

            omniboxDividerLeft = findViewById(R.id.omnibox_divider_left);
            omniboxDividerRight = findViewById(R.id.omnibox_divider_right);

            omniboxUrlContainer = findViewById(R.id.omnibox_url_container);
            omniboxShieldBtn = findViewById(R.id.omnibox_shield_btn);
            omniboxShieldIcon = findViewById(R.id.omnibox_shield_icon);
            omniboxEditText = findViewById(R.id.omnibox_edit_text);
            omniboxPasteBtn = findViewById(R.id.omnibox_paste_btn);
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

            applyOmniboxPosition(omniboxPosition);

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
            tabGridHeaderRow = findViewById(R.id.tab_grid_header_row);
            tabGridHeaderCapsule = findViewById(R.id.tab_grid_header_capsule);
            tabGridCountBadge = findViewById(R.id.tab_grid_count_badge);
            tabGridCloseViewBtn = findViewById(R.id.tab_grid_close_view_btn);
            tabGridTopActions = findViewById(R.id.tab_grid_top_actions);
            btnTabGridTopSplit = findViewById(R.id.btn_tab_grid_top_split);
            btnTabGridTopDeselect = findViewById(R.id.btn_tab_grid_top_deselect);
            btnTabGridTopDelete = findViewById(R.id.btn_tab_grid_top_delete);
            tabGridSearchBox = findViewById(R.id.tab_grid_search_box);
            tabGridSearchIcon = findViewById(R.id.tab_grid_search_icon);
            tabGridSearchInput = findViewById(R.id.tab_grid_search_input);
            btnTabGridFavorite = findViewById(R.id.btn_tab_grid_favorite);
            btnTabGridFilter = findViewById(R.id.btn_tab_grid_filter);
            tabGridFilterLabel = findViewById(R.id.tab_grid_filter_label);
            tabGridGroupBanner = findViewById(R.id.tab_grid_group_banner);
            tabGridGroupColorDot = findViewById(R.id.tab_grid_group_color_dot);
            tabGridGroupBannerTitle = findViewById(R.id.tab_grid_group_banner_title);
            tabGridGroupCountBadge = findViewById(R.id.tab_grid_group_count_badge);
            btnTabGridGroupClose = findViewById(R.id.btn_tab_grid_group_close);
            btnTabGridGroupEdit = findViewById(R.id.btn_tab_grid_group_edit);
            btnTabGridGroupUngroup = findViewById(R.id.btn_tab_grid_group_ungroup);
            btnTabGridGroupDelete = findViewById(R.id.btn_tab_grid_group_delete);
            tabGridContentLayout = findViewById(R.id.tab_grid_content_layout);
            tabGridContainer = findViewById(R.id.tab_grid_container);
            tabGridBottomDock = findViewById(R.id.tab_grid_bottom_dock);
            btnTabDockMakeGroup = findViewById(R.id.btn_tab_dock_make_group);
            tabGridFabAdd = findViewById(R.id.tab_grid_fab_add);
            btnTabDockSelect = findViewById(R.id.btn_tab_dock_select);

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
            ytRemoteSettings = findViewById(R.id.yt_remote_settings);
            ytRemoteTimeline = findViewById(R.id.yt_remote_timeline);
            ytRemotePip = findViewById(R.id.yt_remote_pip);
            ytRemoteLock = findViewById(R.id.yt_remote_lock);
            ytRemoteVolumeBtn = findViewById(R.id.yt_remote_volume_btn);
            ytFloatingTimelineBar = findViewById(R.id.yt_floating_timeline_bar);
            ytTimelineScrubBubbleContainer = findViewById(R.id.yt_timeline_scrub_bubble_container);
            ytTimelineScrubBubble = findViewById(R.id.yt_timeline_scrub_bubble);
            ytTimelinePlayPause = findViewById(R.id.yt_timeline_play_pause);
            ytTimelineCurrentTime = findViewById(R.id.yt_timeline_current_time);
            ytTimelineSeekbar = findViewById(R.id.yt_timeline_seekbar);
            ytTimelineTotalTime = findViewById(R.id.yt_timeline_total_time);
            ytTimelineCollapse = findViewById(R.id.yt_timeline_collapse);
            videoTouchLockOverlay = findViewById(R.id.video_touch_lock_overlay);
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

    private File getTabSnapshotFile(int tabId) {
        File dir = new File(getCacheDir(), "tab_snapshots");
        if (!dir.exists()) dir.mkdirs();
        return new File(dir, "tab_" + tabId + ".jpg");
    }

    private void saveTabSnapshotToDisk(int tabId, Bitmap bmp) {
        if (bmp == null || bmp.isRecycled()) return;
        new Thread(() -> {
            try {
                File file = getTabSnapshotFile(tabId);
                java.io.FileOutputStream fos = new java.io.FileOutputStream(file);
                bmp.compress(Bitmap.CompressFormat.JPEG, 85, fos);
                fos.flush();
                fos.close();
            } catch (Exception ignored) {}
        }).start();
    }

    private Bitmap getOrLoadTabSnapshot(TabItem tab) {
        if (tab == null) return null;
        if (tab.snapshotBitmap != null && !tab.snapshotBitmap.isRecycled()) {
            return tab.snapshotBitmap;
        }
        // Try loading from disk cache
        try {
            File file = getTabSnapshotFile(tab.id);
            if (file.exists() && file.length() > 0) {
                Bitmap diskBmp = BitmapFactory.decodeFile(file.getAbsolutePath());
                if (diskBmp != null) {
                    tab.snapshotBitmap = diskBmp;
                    return diskBmp;
                }
            }
        } catch (Exception ignored) {}

        // If WebView dimensions are measured, capture immediately!
        if (tab.webView != null) {
            try {
                int w = tab.webView.getWidth();
                int h = tab.webView.getHeight();
                if (w > 0 && h > 0) {
                    Bitmap bmp = Bitmap.createBitmap(w / 2, h / 2, Bitmap.Config.RGB_565);
                    Canvas canvas = new Canvas(bmp);
                    canvas.scale(0.5f, 0.5f);
                    tab.webView.draw(canvas);
                    tab.snapshotBitmap = bmp;
                    saveTabSnapshotToDisk(tab.id, bmp);
                    return bmp;
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    private void captureTabSnapshot(TabItem tab) {
        if (tab == null || tab.webView == null) return;
        try {
            if (tab.url != null && tab.url.contains("pdf_viewer.html")) return;
            int w = tab.webView.getWidth();
            int h = tab.webView.getHeight();
            if (w > 0 && h > 0) {
                Bitmap bmp = Bitmap.createBitmap(w / 2, h / 2, Bitmap.Config.RGB_565);
                Canvas canvas = new Canvas(bmp);
                canvas.scale(0.5f, 0.5f);
                tab.webView.draw(canvas);
                tab.snapshotBitmap = bmp;
                saveTabSnapshotToDisk(tab.id, bmp);
                return;
            }
        } catch (Exception ignored) {}

        tab.webView.post(() -> {
            try {
                if (tab.url != null && tab.url.contains("pdf_viewer.html")) return;
                int w = tab.webView.getWidth();
                int h = tab.webView.getHeight();
                if (w > 0 && h > 0) {
                    Bitmap bmp = Bitmap.createBitmap(w / 2, h / 2, Bitmap.Config.RGB_565);
                    Canvas canvas = new Canvas(bmp);
                    canvas.scale(0.5f, 0.5f);
                    tab.webView.draw(canvas);
                    tab.snapshotBitmap = bmp;
                    saveTabSnapshotToDisk(tab.id, bmp);
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

        if (btnTabGridFavorite != null) {
            btnTabGridFavorite.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                TabItem activeTab = getTabById(activeTabId);
                if (activeTab != null) {
                    toggleTabFavorite(activeTabId);
                    updateTabGridFavoriteButton();
                    updateControlSheetTabs();
                    renderTabGridCards(tabGridSearchInput != null ? tabGridSearchInput.getText().toString() : "");
                }
            });
        }

        if (btnTabGridFilter != null) {
            btnTabGridFilter.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                showTabGridFilterPopup(btnTabGridFilter);
            });
        }

        if (btnTabGridGroupClose != null) {
            btnTabGridGroupClose.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                currentGridGroupId = null;
                renderTabGridCards(tabGridSearchInput != null ? tabGridSearchInput.getText().toString() : "");
            });
        }

        if (btnTabGridGroupUngroup != null) {
            btnTabGridGroupUngroup.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                if (currentGridGroupId != null) {
                    tabGroupsList.removeIf(g -> g.id.equals(currentGridGroupId));
                    saveTabGroups();
                    updateOmniboxTabStrip();
                    currentGridGroupId = null;
                    renderTabGridCards(tabGridSearchInput != null ? tabGridSearchInput.getText().toString() : "");
                    Toast.makeText(this, "Group dissolved", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Center Yellow Circular '+' Button (Opens New Tab modal)
        tabGridFabAdd.setOnClickListener(v -> {
            playUiFeedbackSound("tap");
            if (modalNewTabPlatform != null) {
                modalNewTabPlatform.setVisibility(View.VISIBLE);
                modalNewTabPlatform.bringToFront();
            }
        });

        // Bottom Dock: "Group" Button
        if (btnTabDockMakeGroup != null) {
            btnTabDockMakeGroup.setText("Group");
            btnTabDockMakeGroup.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                if (selectedGridTabIds.isEmpty()) {
                    isGridSelectionMode = true;
                    if (getTabById(activeTabId) != null) {
                        selectedGridTabIds.add(activeTabId);
                    }
                    updateTabGridSelectionUi();
                    renderTabGridCards(tabGridSearchInput != null ? tabGridSearchInput.getText().toString() : "");
                    Toast.makeText(this, "Tap tabs to select, then tap Group", Toast.LENGTH_SHORT).show();
                } else {
                    promptCreateTabGroup();
                }
            });
        }

        // Bottom Dock: "Select" Button
        if (btnTabDockSelect != null) {
            btnTabDockSelect.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                isGridSelectionMode = !isGridSelectionMode;
                if (!isGridSelectionMode) {
                    selectedGridTabIds.clear();
                } else {
                    if (selectedGridTabIds.isEmpty() && getTabById(activeTabId) != null) {
                        selectedGridTabIds.add(activeTabId);
                    }
                }
                updateTabGridSelectionUi();
                renderTabGridCards(tabGridSearchInput != null ? tabGridSearchInput.getText().toString() : "");
            });
        }

        // Top Right Action: Split (visible when exactly 2 tabs are selected, without emoji)
        if (btnTabGridTopSplit != null) {
            btnTabGridTopSplit.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                if (selectedGridTabIds.size() == 2) {
                    List<Integer> ids = new ArrayList<>(selectedGridTabIds);
                    activeTabId = ids.get(0);
                    secondarySplitTabId = ids.get(1);
                    selectedGridTabIds.clear();
                    isGridSelectionMode = false;
                    updateTabGridSelectionUi();
                    hideTabGridView();
                    splitModeState = 1;
                    applySplitViewLayout();
                    Toast.makeText(this, "Split Screen Activated", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Top Right Action: Deselect
        if (btnTabGridTopDeselect != null) {
            btnTabGridTopDeselect.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                selectedGridTabIds.clear();
                isGridSelectionMode = false;
                updateTabGridSelectionUi();
                renderTabGridCards(tabGridSearchInput != null ? tabGridSearchInput.getText().toString() : "");
            });
        }

        // Top Right Action: Delete (multi-selected tabs, without emoji)
        if (btnTabGridTopDelete != null) {
            btnTabGridTopDelete.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                if (selectedGridTabIds.isEmpty()) return;
                int count = selectedGridTabIds.size();
                new AlertDialog.Builder(this)
                        .setTitle("Close Selected Tabs")
                        .setMessage("Close " + count + " selected tab(s)?")
                        .setPositiveButton("Close Tabs", (dialog, which) -> {
                            for (int id : new ArrayList<>(selectedGridTabIds)) {
                                closeTab(id);
                            }
                            selectedGridTabIds.clear();
                            isGridSelectionMode = false;
                            updateTabGridSelectionUi();
                            renderTabGridCards(tabGridSearchInput != null ? tabGridSearchInput.getText().toString() : "");
                            Toast.makeText(this, count + " tabs closed", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }

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

    private void updateTabGridSelectionUi() {
        int selCount = selectedGridTabIds.size();
        if (tabGridTopActions != null) {
            tabGridTopActions.setVisibility(selCount > 0 ? View.VISIBLE : View.GONE);
            if (selCount > 0) {
                tabGridTopActions.bringToFront();
            }
        }
        if (tabGridSearchInput != null) {
            tabGridSearchInput.setHint(selCount > 0 ? "" : "Search your tabs");
        }
        if (btnTabGridTopSplit != null) {
            btnTabGridTopSplit.setVisibility(selCount == 2 ? View.VISIBLE : View.GONE);
        }
        if (btnTabGridTopDelete != null) {
            btnTabGridTopDelete.setText(selCount > 1 ? "Delete (" + selCount + ")" : "Delete");
        }
        if (btnTabDockSelect != null) {
            btnTabDockSelect.setText(selCount > 0 || isGridSelectionMode ? "Done (" + selCount + ")" : "Select");
            btnTabDockSelect.setTextColor(selCount > 0 ? (isDarkTheme ? 0xFF00E5FF : 0xFF0284C7) : (isDarkTheme ? 0xFFDFE2F0 : 0xFF334155));
        }
        if (btnTabDockMakeGroup != null) {
            btnTabDockMakeGroup.setText("Group");
            btnTabDockMakeGroup.setTextColor(selCount > 0 ? (isDarkTheme ? 0xFFFFCC00 : 0xFFD97706) : (isDarkTheme ? 0xFFDFE2F0 : 0xFF334155));
        }
    }

    public void applyTabGridTheme() {
        if (tabGridOverlay == null) return;
        boolean isLight = !isDarkTheme;
        tabGridOverlay.setBackgroundColor(isLight ? 0xFFF1F5F9 : 0xFF050811);

        // Favorite & Filter Top Buttons
        if (btnTabGridFavorite != null) {
            GradientDrawable favGd = new GradientDrawable();
            favGd.setColor(isLight ? 0xFFFFFFFF : 0xE60F131D);
            favGd.setCornerRadius(dpToPx(20));
            favGd.setStroke(dpToPx(1.2f), isLight ? 0xFFCBD5E1 : 0x26FFFFFF);
            btnTabGridFavorite.setBackground(favGd);
        }
        if (btnTabGridFilter != null) {
            GradientDrawable filGd = new GradientDrawable();
            filGd.setColor(isLight ? 0xFFFFFFFF : 0xE60F131D);
            filGd.setCornerRadius(dpToPx(20));
            filGd.setStroke(dpToPx(1.2f), isLight ? 0xFFCBD5E1 : 0x26FFFFFF);
            btnTabGridFilter.setBackground(filGd);
        }
        if (tabGridFilterLabel != null) {
            tabGridFilterLabel.setTextColor(isLight ? 0xFF0F172A : 0xFFDFE2F0);
        }

        // Header Capsule
        if (tabGridHeaderCapsule != null) {
            GradientDrawable hGd = new GradientDrawable();
            hGd.setColor(isLight ? 0xFFFFFFFF : 0xE60F131D);
            hGd.setCornerRadius(dpToPx(22));
            hGd.setStroke(dpToPx(1.2f), isLight ? 0xFFCBD5E1 : 0x26FFFFFF);
            tabGridHeaderCapsule.setBackground(hGd);
            tabGridHeaderCapsule.setElevation(dpToPx(4));
        }

        if (tabGridCountBadge != null) {
            tabGridCountBadge.setTextColor(isLight ? 0xFF0284C7 : 0xFF00E5FF);
            GradientDrawable bGd = new GradientDrawable();
            bGd.setColor(isLight ? 0xFFE0F2FE : 0x3300E5FF);
            bGd.setCornerRadius(dpToPx(12));
            tabGridCountBadge.setBackground(bGd);
        }

        if (tabGridCloseViewBtn != null) {
            tabGridCloseViewBtn.setColorFilter(isLight ? 0xFF475569 : 0xFFA2A9A9);
        }

        // Top actions
        if (tabGridTopActions != null) {
            GradientDrawable aGd = new GradientDrawable();
            aGd.setColor(isLight ? 0xFFFFFFFF : 0xF50D111A);
            aGd.setCornerRadius(dpToPx(18));
            aGd.setStroke(dpToPx(1.2f), isLight ? 0xFFCBD5E1 : 0x3300E5FF);
            tabGridTopActions.setBackground(aGd);
            tabGridTopActions.setElevation(dpToPx(20));
        }

        if (btnTabGridTopSplit != null) {
            GradientDrawable spGd = new GradientDrawable();
            spGd.setColor(isLight ? 0xFFE0F2FE : 0x3300E5FF);
            spGd.setCornerRadius(dpToPx(16));
            spGd.setStroke(dpToPx(1), isLight ? 0xFFBAE6FD : 0x6600E5FF);
            btnTabGridTopSplit.setBackground(spGd);
            btnTabGridTopSplit.setTextColor(isLight ? 0xFF0284C7 : 0xFF00E5FF);
        }

        if (btnTabGridTopDeselect != null) {
            GradientDrawable dGd = new GradientDrawable();
            dGd.setColor(isLight ? 0xFFFFFFFF : 0xE6181B25);
            dGd.setCornerRadius(dpToPx(16));
            dGd.setStroke(dpToPx(1), isLight ? 0xFFCBD5E1 : 0x33FFFFFF);
            btnTabGridTopDeselect.setBackground(dGd);
            btnTabGridTopDeselect.setTextColor(isLight ? 0xFF475569 : 0xFFA2A9A9);
        }

        if (btnTabGridTopDelete != null) {
            GradientDrawable delGd = new GradientDrawable();
            delGd.setColor(isLight ? 0xFFFEE2E2 : 0x33FF4444);
            delGd.setCornerRadius(dpToPx(16));
            delGd.setStroke(dpToPx(1), isLight ? 0xFFFCA5A5 : 0x66FF4444);
            btnTabGridTopDelete.setBackground(delGd);
            btnTabGridTopDelete.setTextColor(isLight ? 0xFFDC2626 : 0xFFFF5555);
        }

        // Search Box
        if (tabGridSearchBox != null) {
            GradientDrawable sGd = new GradientDrawable();
            sGd.setColor(isLight ? 0xFFFFFFFF : 0xE60F131D);
            sGd.setCornerRadius(dpToPx(21));
            sGd.setStroke(dpToPx(1.2f), isLight ? 0xFFCBD5E1 : 0x26FFFFFF);
            tabGridSearchBox.setBackground(sGd);
        }
        if (tabGridSearchInput != null) {
            tabGridSearchInput.setTextColor(isLight ? 0xFF0F172A : 0xFFFFFFFF);
            tabGridSearchInput.setHintTextColor(isLight ? 0xFF94A3B8 : 0x88A2A9A9);
        }
        if (tabGridSearchIcon != null) {
            tabGridSearchIcon.setColorFilter(isLight ? 0xFF64748B : 0x88A2A9A9);
        }

        // Inside Group Header Banner
        if (tabGridGroupBanner != null) {
            GradientDrawable gGd = new GradientDrawable();
            gGd.setColor(isLight ? 0xFFFFFFFF : 0xE60F131D);
            gGd.setCornerRadius(dpToPx(16));
            gGd.setStroke(dpToPx(1), isLight ? 0xFFCBD5E1 : 0x26FFFFFF);
            tabGridGroupBanner.setBackground(gGd);
        }
        if (tabGridGroupBannerTitle != null) {
            tabGridGroupBannerTitle.setTextColor(isLight ? 0xFF0F172A : 0xFFFFFFFF);
        }
        if (tabGridGroupCountBadge != null) {
            GradientDrawable bgGd = new GradientDrawable();
            bgGd.setColor(isLight ? 0xFFF1F5F9 : 0x26FFFFFF);
            bgGd.setCornerRadius(dpToPx(10));
            tabGridGroupCountBadge.setBackground(bgGd);
        }
        if (btnTabGridGroupClose != null) {
            btnTabGridGroupClose.setTextColor(isLight ? 0xFF64748B : 0xFFA2A9A9);
        }
        if (btnTabGridGroupEdit != null) {
            GradientDrawable edGd = new GradientDrawable();
            edGd.setColor(isLight ? 0xFFF1F5F9 : 0x26FFFFFF);
            edGd.setCornerRadius(dpToPx(13));
            edGd.setStroke(dpToPx(1), isLight ? 0xFFCBD5E1 : 0x22FFFFFF);
            btnTabGridGroupEdit.setBackground(edGd);
            btnTabGridGroupEdit.setTextColor(isLight ? 0xFF334155 : 0xFFDFE2F0);
        }
        if (btnTabGridGroupUngroup != null) {
            GradientDrawable ugGd = new GradientDrawable();
            ugGd.setColor(isLight ? 0xFFF1F5F9 : 0x26FFFFFF);
            ugGd.setCornerRadius(dpToPx(13));
            ugGd.setStroke(dpToPx(1), isLight ? 0xFFCBD5E1 : 0x22FFFFFF);
            btnTabGridGroupUngroup.setBackground(ugGd);
            btnTabGridGroupUngroup.setTextColor(isLight ? 0xFF334155 : 0xFFDFE2F0);
        }
        if (btnTabGridGroupDelete != null) {
            GradientDrawable delGd = new GradientDrawable();
            delGd.setColor(isLight ? 0x1AEF4444 : 0x22EF4444);
            delGd.setCornerRadius(dpToPx(13));
            delGd.setStroke(dpToPx(1), 0x44EF4444);
            btnTabGridGroupDelete.setBackground(delGd);
            btnTabGridGroupDelete.setTextColor(0xFFEF4444);
        }

        // Bottom Dock Capsule (Image 2)
        if (tabGridBottomDock != null) {
            GradientDrawable dockGd = new GradientDrawable();
            dockGd.setColor(isLight ? 0xFFFFFFFF : 0xF0181B25);
            dockGd.setCornerRadius(dpToPx(26));
            dockGd.setStroke(dpToPx(1.5f), isLight ? 0xFFCBD5E1 : 0x33FFFFFF);
            tabGridBottomDock.setBackground(dockGd);
        }
        if (btnTabDockMakeGroup != null) {
            btnTabDockMakeGroup.setText("Group");
            btnTabDockMakeGroup.setTextColor(isLight ? 0xFF334155 : 0xFFDFE2F0);
        }
        if (btnTabDockSelect != null) {
            btnTabDockSelect.setTextColor(isLight ? 0xFF334155 : 0xFFDFE2F0);
        }
    }

    private void promptCreateTabGroup() {
        showModernTabGroupDialog(null);
    }

    public void updateTabGridFavoriteButton() {
        if (btnTabGridFavorite == null) return;
        TabItem activeTab = getTabById(activeTabId);
        boolean isFav = activeTab != null && activeTab.isFavorite;
        btnTabGridFavorite.setText(isFav ? "★" : "☆");
        btnTabGridFavorite.setTextColor(isFav ? 0xFFFBBF24 : (isDarkTheme ? 0xFFA2A9A9 : 0xFF64748B));
        btnTabGridFavorite.setTextSize(TypedValue.COMPLEX_UNIT_SP, isFav ? 25 : 24);
    }

    private void showTabGridFilterPopup(View anchor) {
        final String[] filterKeys = {"all", "groups", "single"};
        final String[] filterNames = {"All Tabs", "Tab Groups", "Single Tabs"};

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dpToPx(6), dpToPx(6), dpToPx(6), dpToPx(6));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(isDarkTheme ? 0xF0161E31 : 0xFFFFFFFF);
        bg.setCornerRadius(dpToPx(16));
        bg.setStroke(dpToPx(1.2f), isDarkTheme ? 0x2AFFFFFF : 0xFFCBD5E1);
        layout.setBackground(bg);

        android.widget.PopupWindow popup = new android.widget.PopupWindow(layout, dpToPx(125), ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popup.setElevation(dpToPx(14));

        for (int i = 0; i < filterKeys.length; i++) {
            final String key = filterKeys[i];
            final String name = filterNames[i];
            final boolean isSelected = key.equalsIgnoreCase(currentTabGridFilter);

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dpToPx(10), dpToPx(8), dpToPx(10), dpToPx(8));

            GradientDrawable rowGd = new GradientDrawable();
            rowGd.setCornerRadius(dpToPx(10));
            if (isSelected) {
                rowGd.setColor(isDarkTheme ? 0x3300E5FF : 0xFFE0F2FE);
            } else {
                rowGd.setColor(Color.TRANSPARENT);
            }
            row.setBackground(rowGd);

            TextView nameTv = new TextView(this);
            nameTv.setText(name);
            nameTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            nameTv.setTypeface(null, isSelected ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
            nameTv.setTextColor(isSelected ? (isDarkTheme ? 0xFF00E5FF : 0xFF0284C7) : (isDarkTheme ? 0xFFDFE2F0 : 0xFF334155));
            LinearLayout.LayoutParams nLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            nameTv.setLayoutParams(nLp);
            row.addView(nameTv);

            if (isSelected) {
                TextView checkTv = new TextView(this);
                checkTv.setText("✓");
                checkTv.setTextColor(isDarkTheme ? 0xFF00E5FF : 0xFF0284C7);
                checkTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                checkTv.setTypeface(null, android.graphics.Typeface.BOLD);
                row.addView(checkTv);
            }

            row.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                currentTabGridFilter = key;
                if (tabGridFilterLabel != null) {
                    tabGridFilterLabel.setText(key.equals("all") ? "All" : (key.equals("groups") ? "Groups" : "Single"));
                }
                renderTabGridCards(tabGridSearchInput != null ? tabGridSearchInput.getText().toString() : "");
                popup.dismiss();
            });

            layout.addView(row);
        }

        popup.showAsDropDown(anchor, 0, dpToPx(4));
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
        isGridSelectionMode = false;
        currentGridGroupId = null;
        applyTabGridTheme();
        updateTabGridSelectionUi();
        updateTabGridFavoriteButton();
        renderTabGridCards("");

        // iOS Smooth Zoom & Scale Entrance Animation
        if (omniboxTabsBtn != null && omniboxTabsBtn.getWidth() > 0) {
            float pivotX = omniboxTabsBtn.getX() + (omniboxTabsBtn.getWidth() / 2f);
            float pivotY = omniboxTabsBtn.getY() + (omniboxTabsBtn.getHeight() / 2f);
            tabGridOverlay.setPivotX(pivotX);
            tabGridOverlay.setPivotY(pivotY);
        } else {
            tabGridOverlay.setPivotX(getResources().getDisplayMetrics().widthPixels / 2f);
            tabGridOverlay.setPivotY(getResources().getDisplayMetrics().heightPixels * 0.85f);
        }
        tabGridOverlay.setScaleX(0.86f);
        tabGridOverlay.setScaleY(0.86f);
        tabGridOverlay.setAlpha(0.0f);
        tabGridOverlay.setTranslationY(dpToPx(24));

        tabGridOverlay.animate().cancel();
        tabGridOverlay.animate()
                .scaleX(1.0f)
                .scaleY(1.0f)
                .alpha(1.0f)
                .translationY(0f)
                .setDuration(240)
                .setInterpolator(new DecelerateInterpolator(2.0f))
                .start();

        if (browserContentLayout != null) {
            browserContentLayout.animate().cancel();
            browserContentLayout.animate()
                    .scaleX(0.92f)
                    .scaleY(0.92f)
                    .translationY(dpToPx(16))
                    .setDuration(240)
                    .setInterpolator(new DecelerateInterpolator(2.0f))
                    .start();
        }

        playAssetSound("sfx/pop_click.mp3");
    }

    public void hideTabGridView() {
        if (tabGridOverlay != null) {
            if (omniboxTabsBtn != null && omniboxTabsBtn.getWidth() > 0) {
                float pivotX = omniboxTabsBtn.getX() + (omniboxTabsBtn.getWidth() / 2f);
                float pivotY = omniboxTabsBtn.getY() + (omniboxTabsBtn.getHeight() / 2f);
                tabGridOverlay.setPivotX(pivotX);
                tabGridOverlay.setPivotY(pivotY);
            }
            tabGridOverlay.animate().cancel();
            tabGridOverlay.animate()
                    .scaleX(0.86f)
                    .scaleY(0.86f)
                    .alpha(0.0f)
                    .translationY(dpToPx(24))
                    .setDuration(190)
                    .setInterpolator(new PathInterpolator(0.3f, 0f, 0.8f, 0.15f))
                    .withEndAction(() -> {
                        tabGridOverlay.setVisibility(View.GONE);
                        tabGridOverlay.setScaleX(1.0f);
                        tabGridOverlay.setScaleY(1.0f);
                        tabGridOverlay.setAlpha(1.0f);
                        tabGridOverlay.setTranslationY(0f);
                    })
                    .start();

            if (browserContentLayout != null) {
                browserContentLayout.animate().cancel();
                browserContentLayout.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .translationY(0f)
                        .setDuration(200)
                        .setInterpolator(new OvershootInterpolator(1.08f))
                        .start();
            }

            selectedGridTabIds.clear();
            isGridSelectionMode = false;
            currentGridGroupId = null;
            updateTabGridSelectionUi();
            if (modalNewTabPlatform != null) modalNewTabPlatform.setVisibility(View.GONE);
            hideKeyboard();
        }
    }

    private boolean matchesTabFilter(TabItem tab, String query) {
        if (tab == null) return false;
        if (query == null || query.trim().isEmpty()) return true;
        String q = query.trim().toLowerCase();
        if (tab.title != null && tab.title.toLowerCase().contains(q)) return true;
        if (tab.nickname != null && tab.nickname.toLowerCase().contains(q)) return true;
        if (tab.url != null && tab.url.toLowerCase().contains(q)) return true;
        return false;
    }

    private boolean matchesGroupFilter(TabGroup group, String query) {
        if (group == null) return false;
        if (query == null || query.trim().isEmpty()) return true;
        String q = query.trim().toLowerCase();
        if (group.title != null && group.title.toLowerCase().contains(q)) return true;
        for (int tabId : group.tabIds) {
            TabItem tab = getTabById(tabId);
            if (matchesTabFilter(tab, query)) return true;
        }
        return false;
    }

    private void renderTabGridCards(String filterQuery) {
        if (tabGridContentLayout == null) return;
        tabGridContentLayout.removeAllViews();

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
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
                if (activeGroup != null) {
                    if (tabGridGroupBannerTitle != null) {
                        tabGridGroupBannerTitle.setText((activeGroup.icon != null ? activeGroup.icon + " " : "") + activeGroup.title);
                    }
                    int gCol = 0xFF00E5FF;
                    try { gCol = Color.parseColor(activeGroup.color != null ? activeGroup.color : "#00E5FF"); } catch (Exception ignored) {}
                    if (tabGridGroupColorDot != null) {
                        GradientDrawable dotGd = new GradientDrawable();
                        dotGd.setShape(GradientDrawable.OVAL);
                        dotGd.setColor(gCol);
                        tabGridGroupColorDot.setBackground(dotGd);
                    }
                    if (tabGridGroupCountBadge != null) {
                        int count = activeGroup.tabIds.size();
                        tabGridGroupCountBadge.setText(count + (count == 1 ? " Tab" : " Tabs"));
                        tabGridGroupCountBadge.setTextColor(gCol);
                    }
                    if (btnTabGridGroupClose != null) {
                        btnTabGridGroupClose.setOnClickListener(v -> {
                            playUiFeedbackSound("tap");
                            currentGridGroupId = null;
                            renderTabGridCards(tabGridSearchInput != null ? tabGridSearchInput.getText().toString() : "");
                        });
                    }
                    if (btnTabGridGroupEdit != null) {
                        final TabGroup gToEdit = activeGroup;
                        btnTabGridGroupEdit.setOnClickListener(v -> {
                            playUiFeedbackSound("tap");
                            showModernTabGroupDialog(gToEdit);
                        });
                    }
                    if (btnTabGridGroupUngroup != null) {
                        final TabGroup gToUngroup = activeGroup;
                        btnTabGridGroupUngroup.setOnClickListener(v -> {
                            playUiFeedbackSound("tap");
                            tabGroupsList.remove(gToUngroup);
                            currentGridGroupId = null;
                            saveTabGroups();
                            updateOmniboxTabStrip();
                            renderTabGridCards(tabGridSearchInput != null ? tabGridSearchInput.getText().toString() : "");
                            Toast.makeText(this, "Group dissolved", Toast.LENGTH_SHORT).show();
                        });
                    }
                    if (btnTabGridGroupDelete != null) {
                        final TabGroup gToDelete = activeGroup;
                        btnTabGridGroupDelete.setOnClickListener(v -> {
                            playUiFeedbackSound("tap");
                            for (int tid : new ArrayList<>(gToDelete.tabIds)) {
                                closeTab(tid);
                            }
                            tabGroupsList.remove(gToDelete);
                            currentGridGroupId = null;
                            saveTabGroups();
                            updateOmniboxTabStrip();
                            renderTabGridCards(tabGridSearchInput != null ? tabGridSearchInput.getText().toString() : "");
                            Toast.makeText(this, "Group deleted", Toast.LENGTH_SHORT).show();
                        });
                    }
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
                    if (!matchesTabFilter(tab, filterQuery)) continue;
                    grid.addView(createSingleTabCard(tab, cardWidth, filterQuery));
                }
            }

            // Allow dropping onto empty grid space inside group to reorder
            final TabGroup finalActiveGroup = activeGroup;
            grid.setOnDragListener((v, event) -> {
                if (event.getAction() == DragEvent.ACTION_DROP) {
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
                    if (finalActiveGroup != null && event.getLocalState() instanceof TabItem) {
                        TabItem sourceTab = (TabItem) event.getLocalState();
                        int fromIdx = finalActiveGroup.tabIds.indexOf(sourceTab.id);
                        int toIdx = (targetIdx >= 0 && targetIdx < finalActiveGroup.tabIds.size()) ? targetIdx : finalActiveGroup.tabIds.size() - 1;
                        if (fromIdx != -1 && toIdx != -1 && fromIdx != toIdx) {
                            finalActiveGroup.tabIds.remove(fromIdx);
                            finalActiveGroup.tabIds.add(toIdx, sourceTab.id);
                            saveTabGroups();
                            updateOmniboxTabStrip();
                            renderTabGridCards(tabGridSearchInput != null ? tabGridSearchInput.getText().toString() : "");
                            playAssetSound("sfx/pop_click.mp3");
                            return true;
                        }
                    }
                }
                return true;
            });

            if (grid.getChildCount() == 0) {
                TextView emptyView = new TextView(this);
                boolean hasQuery = (filterQuery != null && !filterQuery.trim().isEmpty());
                emptyView.setText(hasQuery ? "No tabs found matching \"" + filterQuery.trim() + "\"" : "No tabs in group");
                emptyView.setTextColor(isDarkTheme ? 0xFF849396 : 0xFF64748B);
                emptyView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                emptyView.setGravity(Gravity.CENTER);
                emptyView.setPadding(0, dpToPx(60), 0, dpToPx(60));
                tabGridContentLayout.addView(emptyView);
            } else {
                tabGridContentLayout.addView(grid);
            }
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

        Set<Integer> handledSplitTabIds = new HashSet<>();
        if (!"groups".equalsIgnoreCase(currentTabGridFilter)) {
            for (TabItem tab : tabsList) {
            if (groupedTabIds.contains(tab.id) || handledSplitTabIds.contains(tab.id)) continue;

            // Check if this tab is part of an active split pair
            if (tab.splitPartnerId != -1 && getTabById(tab.splitPartnerId) != null && !groupedTabIds.contains(tab.splitPartnerId)) {
                TabItem partner = getTabById(tab.splitPartnerId);
                handledSplitTabIds.add(tab.id);
                handledSplitTabIds.add(partner.id);
                TabItem leftTab = "secondary".equals(tab.splitRole) ? partner : tab;
                TabItem rightTab = leftTab == tab ? partner : tab;
                if (!matchesTabFilter(leftTab, filterQuery) && !matchesTabFilter(rightTab, filterQuery)) {
                    continue;
                }
                grid.addView(createSplitTabCard(leftTab, rightTab, cardWidth, filterQuery));
            } else {
                if (!matchesTabFilter(tab, filterQuery)) {
                    continue;
                }
                grid.addView(createSingleTabCard(tab, cardWidth, filterQuery));
            }
        }

        }

        if (!"single".equalsIgnoreCase(currentTabGridFilter)) {
            for (TabGroup group : tabGroupsList) {
                if (group.tabIds.isEmpty()) continue;
                if (!matchesGroupFilter(group, filterQuery)) continue;
                grid.addView(createEdgeTabGroupCard(group, cardWidth, filterQuery));
            }
        }

        if (grid.getChildCount() == 0) {
            TextView emptyView = new TextView(this);
            boolean hasQuery = (filterQuery != null && !filterQuery.trim().isEmpty());
            emptyView.setText(hasQuery ? "No tabs found matching \"" + filterQuery.trim() + "\"" : "No open tabs");
            emptyView.setTextColor(isDarkTheme ? 0xFF849396 : 0xFF64748B);
            emptyView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            emptyView.setGravity(Gravity.CENTER);
            emptyView.setPadding(0, dpToPx(60), 0, dpToPx(60));
            tabGridContentLayout.addView(emptyView);
            return;
        }

        // Multi-tab and single-tab drop onto empty space in the grid
        grid.setOnDragListener((v, event) -> {
            if (event.getAction() == DragEvent.ACTION_DROP) {
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

                if (event.getLocalState() instanceof MultiTabDragState) {
                    MultiTabDragState multiState = (MultiTabDragState) event.getLocalState();
                    List<TabItem> tabsToMove = new ArrayList<>();
                    for (TabItem t : tabsList) {
                        if (multiState.tabIds.contains(t.id)) {
                            tabsToMove.add(t);
                        }
                    }
                    if (!tabsToMove.isEmpty()) {
                        TabItem targetTab = (targetIdx >= 0 && targetIdx < tabsList.size()) ? tabsList.get(targetIdx) : null;
                        tabsList.removeAll(tabsToMove);
                        int insertIdx = targetTab != null ? tabsList.indexOf(targetTab) : tabsList.size();
                        if (insertIdx < 0) insertIdx = tabsList.size();
                        tabsList.addAll(insertIdx, tabsToMove);
                        saveOpenTabsState();
                        renderTabGridCards(tabGridSearchInput != null ? tabGridSearchInput.getText().toString() : "");
                        playAssetSound("sfx/pop_click.mp3");
                        Toast.makeText(this, "Moved " + tabsToMove.size() + " tabs", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                } else if (event.getLocalState() instanceof TabItem) {
                    TabItem sourceTab = (TabItem) event.getLocalState();
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
                } else if (event.getLocalState() instanceof TabGroup) {
                    TabGroup srcG = (TabGroup) event.getLocalState();
                    int fromIdx = tabGroupsList.indexOf(srcG);
                    int toIdx = tabGroupsList.size() - 1;
                    if (fromIdx != -1 && toIdx != -1 && fromIdx != toIdx) {
                        tabGroupsList.remove(fromIdx);
                        tabGroupsList.add(toIdx, srcG);
                        saveTabGroups();
                        updateOmniboxTabStrip();
                        renderTabGridCards(tabGridSearchInput != null ? tabGridSearchInput.getText().toString() : "");
                        playAssetSound("sfx/pop_click.mp3");
                        return true;
                    }
                }
            }
            return true;
        });

        tabGridContentLayout.addView(grid);
    }

    private void showGroupColorPickerPopup(View anchor, TabGroup group) {
        final String[] colors = {"#00E5FF", "#3B82F6", "#10B981", "#F59E0B", "#EF4444", "#EC4899", "#8B5CF6", "#64748B"};
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setPadding(dpToPx(10), dpToPx(8), dpToPx(10), dpToPx(8));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(isDarkTheme ? 0xF0181B25 : 0xFFFFFFFF);
        bg.setCornerRadius(dpToPx(20));
        bg.setStroke(dpToPx(1.2f), isDarkTheme ? 0x44FFFFFF : 0xFFCBD5E1);
        layout.setBackground(bg);

        PopupWindow popup = new PopupWindow(layout, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popup.setElevation(dpToPx(12));

        for (String c : colors) {
            FrameLayout dotContainer = new FrameLayout(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dpToPx(28), dpToPx(28));
            lp.setMargins(dpToPx(4), 0, dpToPx(4), 0);
            dotContainer.setLayoutParams(lp);

            View dot = new View(this);
            FrameLayout.LayoutParams dotLp = new FrameLayout.LayoutParams(dpToPx(20), dpToPx(20), Gravity.CENTER);
            dot.setLayoutParams(dotLp);
            GradientDrawable dotGd = new GradientDrawable();
            dotGd.setShape(GradientDrawable.OVAL);
            dotGd.setColor(Color.parseColor(c));
            if (c.equalsIgnoreCase(group.color)) {
                dotGd.setStroke(dpToPx(2), isDarkTheme ? 0xFFFFFFFF : 0xFF0F172A);
            }
            dot.setBackground(dotGd);
            dotContainer.addView(dot);

            dotContainer.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                group.color = c;
                saveTabGroups();
                updateOmniboxTabStrip();
                renderTabGridCards(tabGridSearchInput != null ? tabGridSearchInput.getText().toString() : "");
                popup.dismiss();
            });
            layout.addView(dotContainer);
        }

        popup.showAsDropDown(anchor, -dpToPx(60), dpToPx(4));
    }

    private void showEditTabGroupDialog(TabGroup group) {
        showModernTabGroupDialog(group);
    }

    private void showModernTabGroupDialog(final TabGroup existingGroup) {
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dpToPx(20), dpToPx(18), dpToPx(20), dpToPx(20));
        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(isDarkTheme ? 0xFF161E31 : 0xFFFFFFFF);
        cardBg.setCornerRadius(dpToPx(24));
        cardBg.setStroke(dpToPx(1.2f), isDarkTheme ? 0x2AFFFFFF : 0xFFCBD5E1);
        card.setBackground(cardBg);

        // Header Row: Title + Close Cross Button
        LinearLayout headerRow = new LinearLayout(this);
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView titleTv = new TextView(this);
        titleTv.setText(existingGroup == null ? "Create Tab Group" : "Edit Tab Group");
        titleTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        titleTv.setTypeface(null, android.graphics.Typeface.BOLD);
        titleTv.setTextColor(isDarkTheme ? 0xFFFFFFFF : 0xFF0F172A);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        titleTv.setLayoutParams(titleLp);
        headerRow.addView(titleTv);

        TextView closeBtn = new TextView(this);
        closeBtn.setText("✕");
        closeBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        closeBtn.setGravity(Gravity.CENTER);
        closeBtn.setTextColor(isDarkTheme ? 0xFF94A3B8 : 0xFF64748B);
        closeBtn.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(28), dpToPx(28)));
        closeBtn.setOnClickListener(v -> dialog.dismiss());
        headerRow.addView(closeBtn);

        card.addView(headerRow);

        // 1. GROUP NAME
        TextView nameLbl = new TextView(this);
        nameLbl.setText("GROUP NAME");
        nameLbl.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f);
        nameLbl.setTypeface(null, android.graphics.Typeface.BOLD);
        nameLbl.setTextColor(isDarkTheme ? 0xFF94A3B8 : 0xFF64748B);
        nameLbl.setPadding(0, dpToPx(16), 0, dpToPx(6));
        card.addView(nameLbl);

        EditText nameInput = new EditText(this);
        nameInput.setText(existingGroup != null ? existingGroup.title : "Tab Group " + (tabGroupsList.size() + 1));
        nameInput.setHint("Group Name...");
        nameInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f);
        nameInput.setTextColor(isDarkTheme ? 0xFFFFFFFF : 0xFF0F172A);
        nameInput.setHintTextColor(isDarkTheme ? 0x88A2A9A9 : 0xFF94A3B8);
        GradientDrawable inputGd = new GradientDrawable();
        inputGd.setColor(isDarkTheme ? 0xFF0E1424 : 0xFFF1F5F9);
        inputGd.setCornerRadius(dpToPx(12));
        inputGd.setStroke(dpToPx(1), isDarkTheme ? 0x2AFFFFFF : 0xFFCBD5E1);
        nameInput.setBackground(inputGd);
        nameInput.setPadding(dpToPx(14), dpToPx(11), dpToPx(14), dpToPx(11));
        card.addView(nameInput);

        // 2. GROUP EMOJI ICON
        TextView emojiLbl = new TextView(this);
        emojiLbl.setText("GROUP EMOJI ICON");
        emojiLbl.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f);
        emojiLbl.setTypeface(null, android.graphics.Typeface.BOLD);
        emojiLbl.setTextColor(isDarkTheme ? 0xFF94A3B8 : 0xFF64748B);
        emojiLbl.setPadding(0, dpToPx(14), 0, dpToPx(6));
        card.addView(emojiLbl);

        final String[] emojis = {"📁", "🚀", "🔥", "⭐", "🎨", "📚", "🎮", "💡", "💼", "⚡"};
        final String[] selectedEmoji = {existingGroup != null && existingGroup.icon != null ? existingGroup.icon : "📁"};

        HorizontalScrollView emojiScroll = new HorizontalScrollView(this);
        emojiScroll.setOverScrollMode(View.OVER_SCROLL_NEVER);
        emojiScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout emojiRow = new LinearLayout(this);
        emojiRow.setOrientation(LinearLayout.HORIZONTAL);
        emojiScroll.addView(emojiRow);

        List<TextView> emojiViews = new ArrayList<>();
        for (String em : emojis) {
            TextView emTv = new TextView(this);
            emTv.setText(em);
            emTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
            emTv.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dpToPx(40), dpToPx(40));
            lp.setMargins(0, 0, dpToPx(6), 0);
            emTv.setLayoutParams(lp);

            GradientDrawable emGd = new GradientDrawable();
            emGd.setCornerRadius(dpToPx(12));
            if (em.equals(selectedEmoji[0])) {
                emGd.setColor(isDarkTheme ? 0x3300E5FF : 0xFFE0F2FE);
                emGd.setStroke(dpToPx(2), isDarkTheme ? 0xFF00E5FF : 0xFF0284C7);
            } else {
                emGd.setColor(isDarkTheme ? 0xFF0E1424 : 0xFFF1F5F9);
                emGd.setStroke(dpToPx(1), isDarkTheme ? 0x22FFFFFF : 0xFFCBD5E1);
            }
            emTv.setBackground(emGd);

            emTv.setOnClickListener(v -> {
                selectedEmoji[0] = em;
                for (int i = 0; i < emojis.length; i++) {
                    TextView tv = emojiViews.get(i);
                    GradientDrawable g = new GradientDrawable();
                    g.setCornerRadius(dpToPx(12));
                    if (emojis[i].equals(em)) {
                        g.setColor(isDarkTheme ? 0x3300E5FF : 0xFFE0F2FE);
                        g.setStroke(dpToPx(2), isDarkTheme ? 0xFF00E5FF : 0xFF0284C7);
                    } else {
                        g.setColor(isDarkTheme ? 0xFF0E1424 : 0xFFF1F5F9);
                        g.setStroke(dpToPx(1), isDarkTheme ? 0x22FFFFFF : 0xFFCBD5E1);
                    }
                    tv.setBackground(g);
                }
            });
            emojiViews.add(emTv);
            emojiRow.addView(emTv);
        }
        card.addView(emojiScroll);

        // 3. GROUP COLOR ACCENT
        TextView colorLbl = new TextView(this);
        colorLbl.setText("GROUP COLOR ACCENT");
        colorLbl.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f);
        colorLbl.setTypeface(null, android.graphics.Typeface.BOLD);
        colorLbl.setTextColor(isDarkTheme ? 0xFF94A3B8 : 0xFF64748B);
        colorLbl.setPadding(0, dpToPx(14), 0, dpToPx(6));
        card.addView(colorLbl);

        final String[] colors = {"#EF4444", "#F97316", "#FBBF24", "#10B981", "#00E5FF", "#3B82F6", "#8B5CF6", "#EC4899"};
        final String[] selectedColor = {existingGroup != null && existingGroup.color != null ? existingGroup.color : "#EF4444"};

        LinearLayout colorRow = new LinearLayout(this);
        colorRow.setOrientation(LinearLayout.HORIZONTAL);
        colorRow.setGravity(Gravity.CENTER_VERTICAL);
        List<FrameLayout> colorViews = new ArrayList<>();
        for (String col : colors) {
            FrameLayout cContainer = new FrameLayout(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dpToPx(34), dpToPx(34));
            lp.setMargins(0, 0, dpToPx(6), 0);
            cContainer.setLayoutParams(lp);

            View cDot = new View(this);
            FrameLayout.LayoutParams dotLp = new FrameLayout.LayoutParams(dpToPx(24), dpToPx(24), Gravity.CENTER);
            cDot.setLayoutParams(dotLp);
            GradientDrawable cGd = new GradientDrawable();
            cGd.setShape(GradientDrawable.OVAL);
            cGd.setColor(Color.parseColor(col));
            cDot.setBackground(cGd);
            cContainer.addView(cDot);

            GradientDrawable ringGd = new GradientDrawable();
            ringGd.setShape(GradientDrawable.OVAL);
            if (col.equalsIgnoreCase(selectedColor[0])) {
                ringGd.setColor(Color.TRANSPARENT);
                ringGd.setStroke(dpToPx(2.5f), isDarkTheme ? 0xFFFFFFFF : 0xFF0F172A);
            } else {
                ringGd.setColor(Color.TRANSPARENT);
            }
            cContainer.setBackground(ringGd);

            cContainer.setOnClickListener(v -> {
                selectedColor[0] = col;
                for (int i = 0; i < colors.length; i++) {
                    FrameLayout fc = colorViews.get(i);
                    GradientDrawable g = new GradientDrawable();
                    g.setShape(GradientDrawable.OVAL);
                    if (colors[i].equalsIgnoreCase(col)) {
                        g.setColor(Color.TRANSPARENT);
                        g.setStroke(dpToPx(2.5f), isDarkTheme ? 0xFFFFFFFF : 0xFF0F172A);
                    } else {
                        g.setColor(Color.TRANSPARENT);
                    }
                    fc.setBackground(g);
                }
            });
            colorViews.add(cContainer);
            colorRow.addView(cContainer);
        }
        card.addView(colorRow);

        // 4. ACTION BUTTONS ROW
        LinearLayout btnRow = new LinearLayout(this);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams btnRowLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        btnRowLp.topMargin = dpToPx(22);
        btnRow.setLayoutParams(btnRowLp);

        if (existingGroup == null) {
            // CREATE MODE: Cancel & Save Group
            Button cancelBtn = new Button(this);
            cancelBtn.setText("Cancel");
            cancelBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            cancelBtn.setTextColor(isDarkTheme ? 0xFFDFE2F0 : 0xFF334155);
            cancelBtn.setTypeface(null, android.graphics.Typeface.BOLD);
            GradientDrawable cBg = new GradientDrawable();
            cBg.setColor(isDarkTheme ? 0xFF1E2638 : 0xFFE2E8F0);
            cBg.setCornerRadius(dpToPx(18));
            cancelBtn.setBackground(cBg);
            cancelBtn.setPadding(dpToPx(16), 0, dpToPx(16), 0);
            LinearLayout.LayoutParams cLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dpToPx(38));
            cLp.setMarginEnd(dpToPx(8));
            cancelBtn.setLayoutParams(cLp);
            cancelBtn.setOnClickListener(v -> dialog.dismiss());
            btnRow.addView(cancelBtn);

            Button saveBtn = new Button(this);
            saveBtn.setText("Save Group");
            saveBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            saveBtn.setTextColor(Color.WHITE);
            saveBtn.setTypeface(null, android.graphics.Typeface.BOLD);
            GradientDrawable sBg = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{0xFF00E5FF, 0xFF0284C7});
            sBg.setCornerRadius(dpToPx(18));
            saveBtn.setBackground(sBg);
            saveBtn.setPadding(dpToPx(20), 0, dpToPx(20), 0);
            saveBtn.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dpToPx(38)));
            saveBtn.setOnClickListener(v -> {
                String name = nameInput.getText().toString().trim();
                if (name.isEmpty()) name = "Group " + (tabGroupsList.size() + 1);

                TabGroup newGroup = new TabGroup("group_" + System.currentTimeMillis(), name, selectedColor[0], selectedEmoji[0]);
                if (!selectedGridTabIds.isEmpty()) {
                    newGroup.tabIds.addAll(selectedGridTabIds);
                    for (TabGroup g : tabGroupsList) {
                        g.tabIds.removeAll(selectedGridTabIds);
                    }
                } else if (getTabById(activeTabId) != null) {
                    newGroup.tabIds.add(activeTabId);
                    for (TabGroup g : tabGroupsList) {
                        g.tabIds.remove((Integer) activeTabId);
                    }
                }
                tabGroupsList.removeIf(g -> g.tabIds.isEmpty());
                tabGroupsList.add(newGroup);
                saveTabGroups();
                updateOmniboxTabStrip();

                selectedGridTabIds.clear();
                isGridSelectionMode = false;
                updateTabGridSelectionUi();
                renderTabGridCards(tabGridSearchInput != null ? tabGridSearchInput.getText().toString() : "");
                Toast.makeText(this, "📁 Tab Group '" + name + "' Created", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
            btnRow.addView(saveBtn);
        } else {
            // EDIT MODE: Ungroup, Delete Group, Save
            Button ungroupBtn = new Button(this);
            ungroupBtn.setText("Ungroup");
            ungroupBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            ungroupBtn.setTextColor(isDarkTheme ? 0xFF38BDF8 : 0xFF0284C7);
            ungroupBtn.setTypeface(null, android.graphics.Typeface.BOLD);
            GradientDrawable uBg = new GradientDrawable();
            uBg.setColor(isDarkTheme ? 0xFF1E2638 : 0xFFE2E8F0);
            uBg.setCornerRadius(dpToPx(18));
            ungroupBtn.setBackground(uBg);
            ungroupBtn.setPadding(dpToPx(12), 0, dpToPx(12), 0);
            LinearLayout.LayoutParams uLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dpToPx(38));
            uLp.setMarginEnd(dpToPx(6));
            ungroupBtn.setLayoutParams(uLp);
            ungroupBtn.setOnClickListener(v -> {
                tabGroupsList.remove(existingGroup);
                if (existingGroup.id.equals(currentGridGroupId)) currentGridGroupId = null;
                saveTabGroups();
                updateOmniboxTabStrip();
                renderTabGridCards(tabGridSearchInput != null ? tabGridSearchInput.getText().toString() : "");
                Toast.makeText(this, "Group dissolved", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
            btnRow.addView(ungroupBtn);

            Button deleteBtn = new Button(this);
            deleteBtn.setText("Delete Group");
            deleteBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            deleteBtn.setTextColor(0xFFEF4444);
            deleteBtn.setTypeface(null, android.graphics.Typeface.BOLD);
            GradientDrawable dBg = new GradientDrawable();
            dBg.setColor(0x18EF4444);
            dBg.setCornerRadius(dpToPx(18));
            dBg.setStroke(dpToPx(1), 0x44EF4444);
            deleteBtn.setBackground(dBg);
            deleteBtn.setPadding(dpToPx(12), 0, dpToPx(12), 0);
            LinearLayout.LayoutParams dLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dpToPx(38));
            dLp.setMarginEnd(dpToPx(6));
            deleteBtn.setLayoutParams(dLp);
            deleteBtn.setOnClickListener(v -> {
                for (int tid : new ArrayList<>(existingGroup.tabIds)) {
                    closeTab(tid);
                }
                tabGroupsList.remove(existingGroup);
                if (existingGroup.id.equals(currentGridGroupId)) currentGridGroupId = null;
                saveTabGroups();
                updateOmniboxTabStrip();
                renderTabGridCards(tabGridSearchInput != null ? tabGridSearchInput.getText().toString() : "");
                Toast.makeText(this, "Group deleted", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
            btnRow.addView(deleteBtn);

            Button saveBtn = new Button(this);
            saveBtn.setText("Save");
            saveBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
            saveBtn.setTextColor(Color.WHITE);
            saveBtn.setTypeface(null, android.graphics.Typeface.BOLD);
            GradientDrawable sBg = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{0xFF00E5FF, 0xFF0284C7});
            sBg.setCornerRadius(dpToPx(18));
            saveBtn.setBackground(sBg);
            saveBtn.setPadding(dpToPx(16), 0, dpToPx(16), 0);
            saveBtn.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dpToPx(38)));
            saveBtn.setOnClickListener(v -> {
                String newName = nameInput.getText().toString().trim();
                if (!newName.isEmpty()) existingGroup.title = newName;
                existingGroup.icon = selectedEmoji[0];
                existingGroup.color = selectedColor[0];
                saveTabGroups();
                updateOmniboxTabStrip();
                renderTabGridCards(tabGridSearchInput != null ? tabGridSearchInput.getText().toString() : "");
                Toast.makeText(this, "Tab Group updated", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
            btnRow.addView(saveBtn);
        }
        card.addView(btnRow);

        dialog.setContentView(card);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int dialogW = (int) (getResources().getDisplayMetrics().widthPixels * 0.90f);
            dialog.getWindow().setLayout(Math.min(dialogW, dpToPx(380)), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        dialog.show();
    }

    private View createEdgeTabGroupCard(TabGroup group, int cardWidth, String filterQuery) {
        boolean isLight = !isDarkTheme;
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = cardWidth;
        lp.height = (int) (cardWidth * 1.35f);
        lp.setMargins(dpToPx(6), dpToPx(6), dpToPx(6), dpToPx(6));
        card.setLayoutParams(lp);

        GradientDrawable gd = new GradientDrawable();
        String groupColorHex = group.color != null && !group.color.isEmpty() ? group.color : "#00E5FF";
        int groupColor = Color.parseColor(groupColorHex);
        int r = Color.red(groupColor);
        int g = Color.green(groupColor);
        int b = Color.blue(groupColor);

        // Dull/dark tinted variant underneath the group tile & contents (Image 2 fix)
        int darkGroupBg = Color.rgb(
            (int) (r * 0.22f + 16 * 0.78f),
            (int) (g * 0.22f + 20 * 0.78f),
            (int) (b * 0.22f + 28 * 0.78f)
        );
        int lightGroupBg = Color.rgb(
            (int) (r * 0.15f + 245 * 0.85f),
            (int) (g * 0.15f + 245 * 0.85f),
            (int) (b * 0.15f + 245 * 0.85f)
        );
        gd.setColor(isLight ? lightGroupBg : darkGroupBg);
        gd.setCornerRadius(dpToPx(18));
        gd.setStroke(dpToPx(2.5f), groupColor);
        card.setBackground(gd);
        card.setPadding(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10));

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        TextView emojiView = new TextView(this);
        emojiView.setText(group.icon != null && !group.icon.isEmpty() ? group.icon : "📁");
        emojiView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        emojiView.setGravity(Gravity.CENTER);
        header.addView(emojiView);

        TextView titleView = new TextView(this);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        titleLp.setMarginStart(dpToPx(6));
        titleView.setLayoutParams(titleLp);
        titleView.setText(group.title != null ? group.title : "Tab Group");
        titleView.setTextColor(isLight ? 0xFF0F172A : 0xFFDFE2F0);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
        titleView.setSingleLine(true);
        titleView.setEllipsize(android.text.TextUtils.TruncateAt.END);
        header.addView(titleView);

        LinearLayout rightControls = new LinearLayout(this);
        rightControls.setOrientation(LinearLayout.HORIZONTAL);
        rightControls.setGravity(Gravity.CENTER_VERTICAL);

        // 1. Number count tab badge
        TextView countBadge = new TextView(this);
        countBadge.setText(String.valueOf(group.tabIds.size()));
        countBadge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        countBadge.setTypeface(null, android.graphics.Typeface.BOLD);
        countBadge.setTextColor(groupColor);
        countBadge.setPadding(dpToPx(6), dpToPx(2), dpToPx(6), dpToPx(2));
        GradientDrawable badgeGd = new GradientDrawable();
        badgeGd.setColor(isLight ? 0xFFF1F5F9 : 0x3300E5FF);
        badgeGd.setCornerRadius(dpToPx(8));
        countBadge.setBackground(badgeGd);
        rightControls.addView(countBadge);

        // 2. Color Swatch Circle (taps open color picker popup)
        FrameLayout colorPickerBtn = new FrameLayout(this);
        LinearLayout.LayoutParams cpLp = new LinearLayout.LayoutParams(dpToPx(22), dpToPx(22));
        cpLp.setMarginStart(dpToPx(4));
        colorPickerBtn.setLayoutParams(cpLp);
        View colorDot = new View(this);
        FrameLayout.LayoutParams dotLp = new FrameLayout.LayoutParams(dpToPx(14), dpToPx(14), Gravity.CENTER);
        colorDot.setLayoutParams(dotLp);
        GradientDrawable dotGd = new GradientDrawable();
        dotGd.setShape(GradientDrawable.OVAL);
        dotGd.setColor(groupColor);
        dotGd.setStroke(dpToPx(1.5f), isLight ? 0xFFCBD5E1 : 0xFFFFFFFF);
        colorDot.setBackground(dotGd);
        colorPickerBtn.addView(colorDot);
        colorPickerBtn.setOnClickListener(v -> {
            playUiFeedbackSound("tap");
            showGroupColorPickerPopup(colorPickerBtn, group);
        });
        rightControls.addView(colorPickerBtn);

        // 3. Edit Button (⋮)
        FrameLayout editBtnFrame = new FrameLayout(this);
        LinearLayout.LayoutParams editLp = new LinearLayout.LayoutParams(dpToPx(22), dpToPx(22));
        editLp.setMarginStart(dpToPx(2));
        editBtnFrame.setLayoutParams(editLp);
        TextView editIcon = new TextView(this);
        editIcon.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        editIcon.setGravity(Gravity.CENTER);
        editIcon.setText("⋮");
        editIcon.setTextColor(isLight ? 0xFF64748B : 0xFFBAC9CC);
        editIcon.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        editIcon.setTypeface(null, android.graphics.Typeface.BOLD);
        editBtnFrame.addView(editIcon);
        editBtnFrame.setOnClickListener(v -> {
            playUiFeedbackSound("tap");
            showEditTabGroupDialog(group);
        });
        rightControls.addView(editBtnFrame);

        header.addView(rightControls);
        card.addView(header);

        // 2x2 Grid of 4 Mini Tabs inside
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
            int darkTileBg = Color.rgb((int) (r * 0.14f + 10 * 0.86f), (int) (g * 0.14f + 14 * 0.86f), (int) (b * 0.14f + 20 * 0.86f));
            int lightTileBg = Color.rgb((int) (r * 0.08f + 240 * 0.92f), (int) (g * 0.08f + 240 * 0.92f), (int) (b * 0.08f + 240 * 0.92f));
            tileGd.setColor(isLight ? lightTileBg : darkTileBg);
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
                Bitmap fav = (tab != null) ? getTabFaviconBitmap(tab) : null;
                if (fav != null && !fav.isRecycled()) {
                    ImageView favImg = new ImageView(this);
                    FrameLayout.LayoutParams favLp = new FrameLayout.LayoutParams(dpToPx(18), dpToPx(18), Gravity.CENTER);
                    favImg.setLayoutParams(favLp);
                    favImg.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    favImg.setImageBitmap(fav);
                    miniTile.addView(favImg);
                } else {
                    TextView miniIcon = new TextView(this);
                    miniIcon.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER));
                    miniIcon.setGravity(Gravity.CENTER);
                    miniIcon.setText(tab != null && tab.url != null && tab.url.contains("youtube.com") ? "🎬" : "🌐");
                    miniIcon.setTextSize(14);
                    miniTile.addView(miniIcon);
                }
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
            int darkEmptyBg = Color.rgb((int) (r * 0.14f + 10 * 0.86f), (int) (g * 0.14f + 14 * 0.86f), (int) (b * 0.14f + 20 * 0.86f));
            int lightEmptyBg = Color.rgb((int) (r * 0.08f + 240 * 0.92f), (int) (g * 0.08f + 240 * 0.92f), (int) (b * 0.08f + 240 * 0.92f));
            emptyGd.setColor(isLight ? lightEmptyBg : darkEmptyBg);
            emptyGd.setCornerRadius(dpToPx(8));
            emptyTile.setBackground(emptyGd);

            miniGrid.addView(emptyTile);
            count++;
        }

        card.addView(miniGrid);

        card.setOnClickListener(v -> {
            playUiFeedbackSound("tap");
            currentGridGroupId = group.id;
            renderTabGridCards(tabGridSearchInput != null ? tabGridSearchInput.getText().toString() : "");
        });

        // Allow long-pressing group card to drag and reposition it
        card.setOnLongClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            playAssetSound("sfx/pop_button_v2.mp3");
            ClipData clipData = ClipData.newPlainText("group_id", group.id);
            View.DragShadowBuilder shadow = new View.DragShadowBuilder(v);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                v.startDragAndDrop(clipData, shadow, group, 0);
            } else {
                v.startDrag(clipData, shadow, group, 0);
            }
            v.setAlpha(0.35f);
            return true;
        });

        card.setOnDragListener((v, event) -> {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    return (event.getLocalState() instanceof TabItem) || (event.getLocalState() instanceof MultiTabDragState) || (event.getLocalState() instanceof TabGroup);

                case DragEvent.ACTION_DRAG_ENTERED:
                    if (event.getLocalState() instanceof TabGroup) {
                        TabGroup srcG = (TabGroup) event.getLocalState();
                        if (!srcG.id.equals(group.id)) {
                            gd.setStroke(dpToPx(3.5f), 0xFF00E5FF);
                            v.animate().scaleX(1.05f).scaleY(1.05f).setDuration(120).start();
                        }
                    } else {
                        gd.setStroke(dpToPx(3.5f), 0xFF00E5FF);
                        v.animate().scaleX(1.05f).scaleY(1.05f).setDuration(120).start();
                    }
                    return true;

                case DragEvent.ACTION_DRAG_EXITED:
                    gd.setStroke(dpToPx(2.5f), groupColor);
                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(120).start();
                    return true;

                case DragEvent.ACTION_DROP:
                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                    if (event.getLocalState() instanceof TabGroup) {
                        TabGroup srcG = (TabGroup) event.getLocalState();
                        if (!srcG.id.equals(group.id)) {
                            int fromIdx = tabGroupsList.indexOf(srcG);
                            int toIdx = tabGroupsList.indexOf(group);
                            if (fromIdx != -1 && toIdx != -1 && fromIdx != toIdx) {
                                tabGroupsList.remove(fromIdx);
                                tabGroupsList.add(toIdx, srcG);
                                saveTabGroups();
                                updateOmniboxTabStrip();
                                renderTabGridCards(tabGridSearchInput != null ? tabGridSearchInput.getText().toString() : "");
                                playAssetSound("sfx/pop_click.mp3");
                                Toast.makeText(this, "Repositioned group", Toast.LENGTH_SHORT).show();
                                return true;
                            }
                        }
                        return false;
                    } else if (event.getLocalState() instanceof MultiTabDragState) {
                        MultiTabDragState multi = (MultiTabDragState) event.getLocalState();
                        for (int tid : multi.tabIds) {
                            addTabToGroup(tid, group.id);
                        }
                        return true;
                    } else if (event.getLocalState() instanceof TabItem) {
                        TabItem sourceTab = (TabItem) event.getLocalState();
                        addTabToGroup(sourceTab.id, group.id);
                        return true;
                    }
                    return false;

                case DragEvent.ACTION_DRAG_ENDED:
                    v.setAlpha(1.0f);
                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                    gd.setStroke(dpToPx(2.5f), groupColor);
                    return true;
            }
            return false;
        });

        return card;
    }

    private View createSplitTabCard(TabItem leftTab, TabItem rightTab, int cardWidth, String filterQuery) {
        boolean isLight = !isDarkTheme;
        boolean isActive = (leftTab.id == activeTabId || rightTab.id == activeTabId ||
                            leftTab.id == secondarySplitTabId || rightTab.id == secondarySplitTabId);
        boolean isSelected = selectedGridTabIds.contains(leftTab.id) || selectedGridTabIds.contains(rightTab.id);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = cardWidth;
        lp.height = (int) (cardWidth * 1.35f);
        lp.setMargins(dpToPx(6), dpToPx(6), dpToPx(6), dpToPx(6));
        card.setLayoutParams(lp);

        GradientDrawable gd = new GradientDrawable();
        gd.setColor(isLight ? 0xFFFFFFFF : 0xFF181B25);
        gd.setCornerRadius(dpToPx(18));
        if (isSelected) {
            gd.setStroke(dpToPx(3), isLight ? 0xFFD97706 : 0xFFFFCC00);
        } else if (isActive) {
            gd.setStroke(dpToPx(2), isLight ? 0xFF0284C7 : 0xFF00E5FF);
        } else {
            gd.setStroke(dpToPx(1.5f), isLight ? 0xFFBAE6FD : 0x4400E5FF);
        }
        card.setBackground(gd);
        card.setPadding(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10));

        // Header: Split Icon + Pair Titles + Unsplit button
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        // Split Icon Pill Badge
        TextView splitBadge = new TextView(this);
        splitBadge.setText("◫ Split");
        splitBadge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        splitBadge.setTypeface(null, android.graphics.Typeface.BOLD);
        splitBadge.setTextColor(isLight ? 0xFF0284C7 : 0xFF00E5FF);
        splitBadge.setPadding(dpToPx(6), dpToPx(2), dpToPx(6), dpToPx(2));
        GradientDrawable sbGd = new GradientDrawable();
        sbGd.setColor(isLight ? 0xFFE0F2FE : 0x3300E5FF);
        sbGd.setCornerRadius(dpToPx(6));
        splitBadge.setBackground(sbGd);

        TextView titleView = new TextView(this);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        titleLp.setMarginStart(dpToPx(6));
        titleView.setLayoutParams(titleLp);
        String leftTitle = leftTab.nickname != null && !leftTab.nickname.isEmpty() ? leftTab.nickname : (leftTab.title != null ? leftTab.title : "Tab 1");
        String rightTitle = rightTab.nickname != null && !rightTab.nickname.isEmpty() ? rightTab.nickname : (rightTab.title != null ? rightTab.title : "Tab 2");
        titleView.setText(leftTitle + " ⚡ " + rightTitle);
        titleView.setTextColor(isLight ? 0xFF0F172A : 0xFFDFE2F0);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
        titleView.setSingleLine(true);
        titleView.setEllipsize(android.text.TextUtils.TruncateAt.END);

        // Unsplit / Close Button Circle
        FrameLayout closeCircle = new FrameLayout(this);
        closeCircle.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(22), dpToPx(22)));
        GradientDrawable closeGd = new GradientDrawable();
        closeGd.setColor(isLight ? 0xFFF1F5F9 : 0xFF262A34);
        closeGd.setShape(GradientDrawable.OVAL);
        closeCircle.setBackground(closeGd);

        ImageButton closeBtn = new ImageButton(this);
        closeBtn.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        closeBtn.setBackgroundResource(android.R.color.transparent);
        closeBtn.setImageResource(R.drawable.ic_pod_close);
        closeBtn.setColorFilter(isLight ? 0xFF64748B : 0xFFBAC9CC);
        closeBtn.setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
        closeBtn.setOnClickListener(v -> {
            playUiFeedbackSound("tap");
            separateSplitTabs(leftTab.id);
            updateOmniboxTabStrip();
            renderTabGridCards(tabGridSearchInput != null ? tabGridSearchInput.getText().toString() : "");
            Toast.makeText(this, "Un-split tabs", Toast.LENGTH_SHORT).show();
        });
        closeCircle.addView(closeBtn);

        header.addView(splitBadge);
        header.addView(titleView);
        header.addView(closeCircle);
        card.addView(header);

        // Body: Side-by-side Dual Previews
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams bodyLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
        bodyLp.topMargin = dpToPx(8);
        body.setLayoutParams(bodyLp);
        GradientDrawable bodyGd = new GradientDrawable();
        bodyGd.setColor(isLight ? 0xFFF8FAFC : 0xFF0A0E17);
        bodyGd.setCornerRadius(dpToPx(12));
        body.setBackground(bodyGd);
        body.setClipToOutline(true);

        // Left Half
        FrameLayout leftFrame = new FrameLayout(this);
        leftFrame.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));
        Bitmap lBmp = getOrLoadTabSnapshot(leftTab);
        if (lBmp != null && !lBmp.isRecycled()) {
            ImageView img = new ImageView(this);
            img.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            img.setScaleType(ImageView.ScaleType.CENTER_CROP);
            img.setImageBitmap(lBmp);
            leftFrame.addView(img);
        } else {
            Bitmap lFav = getTabFaviconBitmap(leftTab);
            if (lFav != null && !lFav.isRecycled()) {
                ImageView favImg = new ImageView(this);
                FrameLayout.LayoutParams favLp = new FrameLayout.LayoutParams(dpToPx(24), dpToPx(24), Gravity.CENTER);
                favImg.setLayoutParams(favLp);
                favImg.setScaleType(ImageView.ScaleType.FIT_CENTER);
                favImg.setImageBitmap(lFav);
                leftFrame.addView(favImg);
            } else {
                TextView icon = new TextView(this);
                icon.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER));
                icon.setGravity(Gravity.CENTER);
                icon.setText(leftTab.url != null && leftTab.url.contains("youtube.com") ? "🎬" : "🌐");
                icon.setTextSize(14);
                leftFrame.addView(icon);
            }
        }

        // Vertical Divider
        View vDivider = new View(this);
        vDivider.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(1.5f), ViewGroup.LayoutParams.MATCH_PARENT));
        vDivider.setBackgroundColor(isLight ? 0xFFCBD5E1 : 0x4400E5FF);

        // Right Half
        FrameLayout rightFrame = new FrameLayout(this);
        rightFrame.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));
        Bitmap rBmp = getOrLoadTabSnapshot(rightTab);
        if (rBmp != null && !rBmp.isRecycled()) {
            ImageView img = new ImageView(this);
            img.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            img.setScaleType(ImageView.ScaleType.CENTER_CROP);
            img.setImageBitmap(rBmp);
            rightFrame.addView(img);
        } else {
            Bitmap rFav = getTabFaviconBitmap(rightTab);
            if (rFav != null && !rFav.isRecycled()) {
                ImageView favImg = new ImageView(this);
                FrameLayout.LayoutParams favLp = new FrameLayout.LayoutParams(dpToPx(24), dpToPx(24), Gravity.CENTER);
                favImg.setLayoutParams(favLp);
                favImg.setScaleType(ImageView.ScaleType.FIT_CENTER);
                favImg.setImageBitmap(rFav);
                rightFrame.addView(favImg);
            } else {
                TextView icon = new TextView(this);
                icon.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER));
                icon.setGravity(Gravity.CENTER);
                icon.setText(rightTab.url != null && rightTab.url.contains("youtube.com") ? "🎬" : "🌐");
                icon.setTextSize(14);
                rightFrame.addView(icon);
            }
        }

        body.addView(leftFrame);
        body.addView(vDivider);
        body.addView(rightFrame);
        card.addView(body);

        card.setOnClickListener(v -> {
            playUiFeedbackSound("tap");
            if (isGridSelectionMode || !selectedGridTabIds.isEmpty()) {
                if (selectedGridTabIds.contains(leftTab.id)) {
                    selectedGridTabIds.remove(leftTab.id);
                    selectedGridTabIds.remove(rightTab.id);
                } else {
                    selectedGridTabIds.add(leftTab.id);
                    selectedGridTabIds.add(rightTab.id);
                }
                updateTabGridSelectionUi();
                renderTabGridCards(tabGridSearchInput != null ? tabGridSearchInput.getText().toString() : "");
            } else {
                activeTabId = leftTab.id;
                secondarySplitTabId = rightTab.id;
                splitModeState = leftTab.splitOrientation > 0 ? leftTab.splitOrientation : 1;
                applySplitViewLayout();
                hideTabGridView();
            }
        });

        card.setOnLongClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            playAssetSound("sfx/pop_button_v2.mp3");
            List<Integer> pairIds = Arrays.asList(leftTab.id, rightTab.id);
            ClipData clipData = ClipData.newPlainText("split_pair", leftTab.id + "," + rightTab.id);
            MultiTabDragState state = new MultiTabDragState(pairIds, leftTab.id);
            View.DragShadowBuilder shadow = new View.DragShadowBuilder(v);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                v.startDragAndDrop(clipData, shadow, state, 0);
            } else {
                v.startDrag(clipData, shadow, state, 0);
            }
            v.setAlpha(0.35f);
            return true;
        });

        return card;
    }

    private View createSingleTabCard(TabItem tab, int cardWidth, String filterQuery) {
        String title = tab.title != null ? tab.title : "New Tab";
        String url = tab.url != null ? tab.url : "";

        boolean isActive = (tab.id == activeTabId);
        boolean isSelected = selectedGridTabIds.contains(tab.id);
        boolean isLight = !isDarkTheme;

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = cardWidth;
        lp.height = (int) (cardWidth * 1.35f);
        lp.setMargins(dpToPx(6), dpToPx(6), dpToPx(6), dpToPx(6));
        card.setLayoutParams(lp);

        GradientDrawable gd = new GradientDrawable();
        gd.setColor(isLight ? 0xFFFFFFFF : 0xFF181B25);
        gd.setCornerRadius(dpToPx(18));
        if (isSelected) {
            gd.setStroke(dpToPx(3), isLight ? 0xFFD97706 : 0xFFFFCC00);
        } else if (isActive) {
            gd.setStroke(dpToPx(2), isLight ? 0xFF0284C7 : 0xFF00E5FF);
        } else {
            gd.setStroke(dpToPx(1), isLight ? 0xFFE2E8F0 : 0x22FFFFFF);
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
        iconChipGd.setColor(isLight ? 0xFFF1F5F9 : 0xFF1C1F29);
        iconChipGd.setCornerRadius(dpToPx(6));
        iconChip.setBackground(iconChipGd);

        Bitmap favBmp = getTabFaviconBitmap(tab);
        if (favBmp != null && !favBmp.isRecycled()) {
            ImageView favImg = new ImageView(this);
            FrameLayout.LayoutParams favLp = new FrameLayout.LayoutParams(dpToPx(16), dpToPx(16), Gravity.CENTER);
            favImg.setLayoutParams(favLp);
            favImg.setScaleType(ImageView.ScaleType.FIT_CENTER);
            favImg.setImageBitmap(favBmp);
            iconChip.addView(favImg);
        } else {
            TextView iconView = new TextView(this);
            iconView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            iconView.setGravity(Gravity.CENTER);
            iconView.setText(tab.isIncognito ? "🕶️" : (url.contains("youtube.com") ? "🎬" : (url.contains("chatgpt.com") ? "🤖" : (url.contains("gemini.google.com") ? "♊" : "🌐"))));
            iconView.setTextSize(11);
            iconChip.addView(iconView);
        }

        TextView titleView = new TextView(this);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        titleLp.setMarginStart(dpToPx(8));
        titleView.setLayoutParams(titleLp);
        titleView.setText(tab.nickname != null && !tab.nickname.isEmpty() ? tab.nickname : title);
        titleView.setTextColor(isLight ? 0xFF0F172A : 0xFFDFE2F0);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
        titleView.setSingleLine(true);
        titleView.setEllipsize(android.text.TextUtils.TruncateAt.END);

        FrameLayout closeCircle = new FrameLayout(this);
        closeCircle.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(22), dpToPx(22)));
        GradientDrawable closeGd = new GradientDrawable();
        closeGd.setColor(isLight ? 0xFFF1F5F9 : 0xFF262A34);
        closeGd.setShape(GradientDrawable.OVAL);
        closeCircle.setBackground(closeGd);

        ImageButton closeBtn = new ImageButton(this);
        closeBtn.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        closeBtn.setBackgroundResource(android.R.color.transparent);
        closeBtn.setImageResource(R.drawable.ic_pod_close);
        closeBtn.setColorFilter(isLight ? 0xFF64748B : 0xFFBAC9CC);
        closeBtn.setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
        closeBtn.setOnClickListener(v -> {
            playUiFeedbackSound("tap");
            closeTab(tab.id);
            selectedGridTabIds.remove(tab.id);
            updateTabGridSelectionUi();
            renderTabGridCards(tabGridSearchInput != null ? tabGridSearchInput.getText().toString() : "");
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
        bodyGd.setColor(isLight ? 0xFFF8FAFC : 0xFF0A0E17);
        bodyGd.setCornerRadius(dpToPx(12));
        body.setBackground(bodyGd);
        body.setClipToOutline(true);

        Bitmap previewBmp = getOrLoadTabSnapshot(tab);
        if (previewBmp != null && !previewBmp.isRecycled()) {
            ImageView previewImage = new ImageView(this);
            previewImage.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            previewImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            previewImage.setImageBitmap(previewBmp);
            body.addView(previewImage);
        } else {
            LinearLayout placeholder = new LinearLayout(this);
            placeholder.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            placeholder.setOrientation(LinearLayout.VERTICAL);
            placeholder.setGravity(Gravity.CENTER);
            placeholder.setPadding(dpToPx(12), dpToPx(14), dpToPx(12), dpToPx(14));

            TextView bigIcon = new TextView(this);
            bigIcon.setText(tab.isIncognito ? "🕶️" : (url.contains("youtube.com") ? "🎬" : (url.contains("chatgpt.com") ? "🤖" : (url.contains("gemini.google.com") ? "♊" : "🌐"))));
            bigIcon.setTextSize(26);
            bigIcon.setGravity(Gravity.CENTER);
            placeholder.addView(bigIcon);

            TextView hostView = new TextView(this);
            hostView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            hostView.setText(cleanDisplayUrl(url));
            hostView.setTextColor(isLight ? 0xFF334155 : 0xFFCBD5E1);
            hostView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            hostView.setTypeface(null, android.graphics.Typeface.BOLD);
            hostView.setSingleLine(true);
            hostView.setEllipsize(android.text.TextUtils.TruncateAt.END);
            hostView.setPadding(0, dpToPx(6), 0, 0);
            placeholder.addView(hostView);

            body.addView(placeholder);
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
            domainBadge.setTextColor(isLight ? 0xFF0284C7 : 0xFF00E5FF);
            domainBadge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9);
            domainBadge.setTypeface(null, android.graphics.Typeface.BOLD);
            domainBadge.setPadding(dpToPx(6), dpToPx(2), dpToPx(6), dpToPx(2));
            GradientDrawable badgeGd = new GradientDrawable();
            badgeGd.setColor(isLight ? 0xE6F1F5F9 : 0xCC0F131D);
            badgeGd.setCornerRadius(dpToPx(6));
            domainBadge.setBackground(badgeGd);
            body.addView(domainBadge);
        }

        card.addView(body);

        card.setOnClickListener(v -> {
            playUiFeedbackSound("tap");
            if (isGridSelectionMode || !selectedGridTabIds.isEmpty()) {
                if (selectedGridTabIds.contains(tab.id)) {
                    selectedGridTabIds.remove(tab.id);
                    if (selectedGridTabIds.isEmpty()) {
                        isGridSelectionMode = false;
                    }
                } else {
                    selectedGridTabIds.add(tab.id);
                }
                updateTabGridSelectionUi();
                renderTabGridCards(tabGridSearchInput != null ? tabGridSearchInput.getText().toString() : "");
            } else {
                switchToTab(tab.id);
                hideTabGridView();
            }
        });

        // Multi-tab drag when selected, or single tab drag
        card.setOnLongClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            playAssetSound("sfx/pop_button_v2.mp3");

            if (selectedGridTabIds.size() > 1 && selectedGridTabIds.contains(tab.id)) {
                ClipData clipData = ClipData.newPlainText("multi_tabs", TextUtils.join(",", selectedGridTabIds));
                MultiTabDragState state = new MultiTabDragState(selectedGridTabIds, tab.id);
                View.DragShadowBuilder shadow = new View.DragShadowBuilder(v);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    v.startDragAndDrop(clipData, shadow, state, 0);
                } else {
                    v.startDrag(clipData, shadow, state, 0);
                }
                v.setAlpha(0.35f);
                return true;
            }

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
                    return (event.getLocalState() instanceof TabItem) || (event.getLocalState() instanceof MultiTabDragState);

                case DragEvent.ACTION_DRAG_ENTERED:
                    if (event.getLocalState() instanceof TabItem) {
                        TabItem sourceTab = (TabItem) event.getLocalState();
                        if (sourceTab.id != tab.id) {
                            gd.setStroke(dpToPx(3), isLight ? 0xFF0284C7 : 0xFF00E5FF);
                            v.animate().scaleX(1.06f).scaleY(1.06f).setDuration(120).start();
                        }
                    } else if (event.getLocalState() instanceof MultiTabDragState) {
                        MultiTabDragState multiState = (MultiTabDragState) event.getLocalState();
                        if (!multiState.tabIds.contains(tab.id)) {
                            gd.setStroke(dpToPx(3), isLight ? 0xFF0284C7 : 0xFF00E5FF);
                            v.animate().scaleX(1.06f).scaleY(1.06f).setDuration(120).start();
                        }
                    }
                    return true;

                case DragEvent.ACTION_DRAG_EXITED:
                    if (selectedGridTabIds.contains(tab.id)) {
                        gd.setStroke(dpToPx(3), isLight ? 0xFFD97706 : 0xFFFFCC00);
                    } else if (tab.id == activeTabId) {
                        gd.setStroke(dpToPx(2), isLight ? 0xFF0284C7 : 0xFF00E5FF);
                    } else {
                        gd.setStroke(dpToPx(1), isLight ? 0xFFE2E8F0 : 0x22FFFFFF);
                    }
                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(120).start();
                    return true;

                case DragEvent.ACTION_DROP:
                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                    if (event.getLocalState() instanceof MultiTabDragState) {
                        MultiTabDragState multiState = (MultiTabDragState) event.getLocalState();
                        List<TabItem> tabsToMove = new ArrayList<>();
                        for (TabItem t : tabsList) {
                            if (multiState.tabIds.contains(t.id)) {
                                tabsToMove.add(t);
                            }
                        }
                        int toIdx = tabsList.indexOf(tab);
                        if (toIdx != -1 && !tabsToMove.isEmpty()) {
                            tabsList.removeAll(tabsToMove);
                            int insertIdx = tabsList.indexOf(tab);
                            if (insertIdx < 0) insertIdx = toIdx;
                            tabsList.addAll(insertIdx, tabsToMove);
                            saveOpenTabsState();
                            renderTabGridCards(tabGridSearchInput != null ? tabGridSearchInput.getText().toString() : "");
                            playAssetSound("sfx/pop_click.mp3");
                            Toast.makeText(this, "Moved " + tabsToMove.size() + " tabs", Toast.LENGTH_SHORT).show();
                            return true;
                        }
                    } else if (event.getLocalState() instanceof TabItem) {
                        TabItem sourceTab = (TabItem) event.getLocalState();
                        if (sourceTab.id != tab.id) {
                            if (currentGridGroupId != null) {
                                TabGroup activeG = null;
                                for (TabGroup g : tabGroupsList) {
                                    if (g.id.equals(currentGridGroupId)) {
                                        activeG = g;
                                        break;
                                    }
                                }
                                if (activeG != null) {
                                    int fromIdx = activeG.tabIds.indexOf(sourceTab.id);
                                    int toIdx = activeG.tabIds.indexOf(tab.id);
                                    if (fromIdx != -1 && toIdx != -1 && fromIdx != toIdx) {
                                        activeG.tabIds.remove(fromIdx);
                                        activeG.tabIds.add(toIdx, sourceTab.id);
                                        saveTabGroups();
                                        updateOmniboxTabStrip();
                                        renderTabGridCards(tabGridSearchInput != null ? tabGridSearchInput.getText().toString() : "");
                                        playAssetSound("sfx/pop_click.mp3");
                                        return true;
                                    }
                                }
                            } else {
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
                    }
                    return false;

                case DragEvent.ACTION_DRAG_ENDED:
                    v.setAlpha(1.0f);
                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                    if (selectedGridTabIds.contains(tab.id)) {
                        gd.setStroke(dpToPx(3), isLight ? 0xFFD97706 : 0xFFFFCC00);
                    } else if (tab.id == activeTabId) {
                        gd.setStroke(dpToPx(2), isLight ? 0xFF0284C7 : 0xFF00E5FF);
                    } else {
                        gd.setStroke(dpToPx(1), isLight ? 0xFFE2E8F0 : 0x22FFFFFF);
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
        updateOmniboxTabStrip();
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
        updateOmniboxTabStrip();
        saveOpenTabsState();
        renderTabGridCards(tabGridSearchInput != null ? tabGridSearchInput.getText().toString() : "");
        playAssetSound("sfx/pop_click.mp3");
        Toast.makeText(this, "📁 Added '" + sourceTab.title + "' to " + targetGroup.title, Toast.LENGTH_SHORT).show();
    }

    private void toggleTabSelectionForSplit(int tabId) {
        playUiFeedbackSound("tap");
        if (selectedGridTabIds.contains(tabId)) {
            selectedGridTabIds.remove(tabId);
            if (selectedGridTabIds.isEmpty()) {
                isGridSelectionMode = false;
            }
        } else {
            selectedGridTabIds.add(tabId);
        }

        updateTabGridSelectionUi();
        renderTabGridCards(tabGridSearchInput != null ? tabGridSearchInput.getText().toString() : "");
    }

    public void updateYouTubeLiveState(boolean isPlaying, boolean isMuted) {
        updateYouTubeLiveState(isPlaying, isMuted, null);
    }

    public void updateYouTubeLiveState(boolean isPlaying, boolean isMuted, Integer tabId) {
        if (isPlaying) {
            hasYouTubePlaybackStarted = true;
        }
        if (tabId != null && tabId > 0) {
            TabItem tab = getTabById(tabId);
            if (tab != null) {
                tab.isPlayingAudio = isPlaying;
                tab.isMuted = isMuted;
            }
        } else {
            TabItem cur = getTabById(activeTabId);
            if (cur != null) {
                cur.isPlayingAudio = isPlaying;
                cur.isMuted = isMuted;
            }
        }
        if (tabId == null || tabId <= 0 || tabId == activeTabId) {
            if (ytRemotePlayPause != null) {
                ytRemotePlayPause.setImageResource(isPlaying ? R.drawable.ic_pod_pause : R.drawable.ic_pod_play);
            }
            if (ytTimelinePlayPause != null) {
                ytTimelinePlayPause.setImageResource(isPlaying ? R.drawable.ic_pod_pause : R.drawable.ic_pod_play);
            }
            if (ytRemoteMute != null) {
                ytRemoteMute.setImageResource(isMuted ? R.drawable.ic_pod_mute : R.drawable.ic_pod_unmute);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && isInPictureInPictureMode()) {
                updatePiPActions();
            }
        }
        if (isPlaying) {
            hasYouTubePlaybackStarted = true;
        }
        if (hasYouTubePlaybackStarted) {
            updateMediaPlaybackNotification(isPlaying);
        }
        if (!hasAnyYouTubeTab()) {
            hasYouTubePlaybackStarted = false;
            dismissMediaNotification();
        }
        manageYouTubeWakeLock(isPlaying);
    }

    private final Handler ytWakeLockHandler = new Handler(Looper.getMainLooper());
    private final Runnable ytWakeLockReleaseRunnable = () -> {
        try {
            if (youtubeWakeLock != null && youtubeWakeLock.isHeld()) {
                youtubeWakeLock.release();
            }
        } catch (Exception ignored) {}
    };

    private void manageYouTubeWakeLock(boolean acquire) {
        try {
            if (acquire) {
                ytWakeLockHandler.removeCallbacks(ytWakeLockReleaseRunnable);
                if (youtubeWakeLock == null) {
                    PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                    if (pm != null) {
                        youtubeWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Caspian:YouTubePlayback");
                    }
                }
                if (youtubeWakeLock != null && !youtubeWakeLock.isHeld()) {
                    youtubeWakeLock.acquire(4 * 60 * 60 * 1000L); // 4-hour max safety timeout
                }
            } else {
                if (hasYouTubePlaybackStarted && hasAnyYouTubeTab()) {
                    // Retain wake lock for 10-minute grace period during track transitions and lock screen pause
                    ytWakeLockHandler.removeCallbacks(ytWakeLockReleaseRunnable);
                    ytWakeLockHandler.postDelayed(ytWakeLockReleaseRunnable, 10 * 60 * 1000L);
                } else {
                    ytWakeLockHandler.removeCallbacks(ytWakeLockReleaseRunnable);
                    if (youtubeWakeLock != null && youtubeWakeLock.isHeld()) {
                        youtubeWakeLock.release();
                    }
                }
            }
        } catch (Exception ignored) {}
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
        } else if ("yt_pod".equalsIgnoreCase(type) || "yt_remote".equalsIgnoreCase(type)) {
            if (ytFloatingRemoteContainer != null) {
                ytFloatingRemoteContainer.setScaleX(scale);
                ytFloatingRemoteContainer.setScaleY(scale);
            }
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putString("yt_pod_scale", String.valueOf(scale)).apply();
        } else if ("google_dock".equalsIgnoreCase(type) || "google_search".equalsIgnoreCase(type)) {
            if (searchNavContainer != null) {
                searchNavContainer.setScaleX(scale);
                searchNavContainer.setScaleY(scale);
            }
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putString("google_dock_scale", String.valueOf(scale)).apply();
        } else if ("chatgpt_dock".equalsIgnoreCase(type)) {
            if (chatgptDockContainer != null) {
                chatgptDockContainer.setScaleX(scale);
                chatgptDockContainer.setScaleY(scale);
            }
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putString("chatgpt_dock_scale", String.valueOf(scale)).apply();
        } else if ("gemini_dock".equalsIgnoreCase(type)) {
            if (geminiDockContainer != null) {
                geminiDockContainer.setScaleX(scale);
                geminiDockContainer.setScaleY(scale);
            }
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putString("gemini_dock_scale", String.valueOf(scale)).apply();
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

            if (ytTimelineCurrentTime != null) {
                ytTimelineCurrentTime.setTextColor(startC);
            }
            if (ytTimelinePlayPause != null) {
                ytTimelinePlayPause.setColorFilter(startC);
            }
            if (ytTimelineCollapse != null) {
                ytTimelineCollapse.setColorFilter(startC);
            }
            if (ytTimelineSeekbar != null) {
                ytTimelineSeekbar.setThumbTintList(ColorStateList.valueOf(startC));
                ytTimelineSeekbar.setProgressTintList(ColorStateList.valueOf(startC));
            }
            if (ytRemoteVolumeBtn != null) {
                ytRemoteVolumeBtn.setTextColor(startC);
            }
            if (ytRemoteTimeline != null && ytFloatingTimelineBar != null && ytFloatingTimelineBar.getVisibility() == View.VISIBLE) {
                ytRemoteTimeline.setColorFilter(startC);
            }
            if (ytRemotePip != null) {
                ytRemotePip.setColorFilter(0xFFFFFFFF);
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
            updateOmniboxScrimBackground();
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
        if (omniboxPasteBtn != null) omniboxPasteBtn.setTextColor(isDarkTheme ? 0xFF00E5FF : 0xFF0284C7);
        if (omniboxDividerLeft != null) omniboxDividerLeft.setBackgroundColor(isDarkTheme ? 0x33FFFFFF : 0x22000000);
        if (omniboxDividerRight != null) omniboxDividerRight.setBackgroundColor(isDarkTheme ? 0x33FFFFFF : 0x22000000);

        updateOmniboxTabStrip();
        applyTabGridTheme();
        if (tabGridOverlay != null && tabGridOverlay.getVisibility() == View.VISIBLE && tabGridSearchInput != null) {
            renderTabGridCards(tabGridSearchInput.getText().toString());
        }
    }

    public void applyWebViewTheme(WebView webView, boolean isDark) {
        if (webView == null) return;
        String currentUrl = webView.getUrl();
        if (currentUrl != null && (currentUrl.contains("youtube.com") || currentUrl.contains("music.youtube.com"))) {
            // User requested: light mode in mobile site of youtube is broken with custom theme injection, do not color switch in youtube or youtube music tabs
            try {
                webView.evaluateJavascript("(function(){ try { var s = document.getElementById('caspian-yt-theme-style'); if (s) s.remove(); } catch(e){} })();", null);
            } catch (Throwable ignored) {}
            return;
        }

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
            if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
                WebSettingsCompat.setAlgorithmicDarkeningAllowed(webView.getSettings(), isDark);
            }
        } catch (Throwable ignored) {}

        try {
            String themeJs = "(function() {\n" +
                    "  var isDark = " + isDark + ";\n" +
                    "  try {\n" +
                    "    var host = (window.location && window.location.host) ? window.location.host : '';\n" +
                    "    // YouTube & YouTube Music: Do NOT do color switch, preserve native YouTube styles\n" +
                    "    if (host.includes('youtube.com') || host.includes('music.youtube.com')) {\n" +
                    "      try { var s = document.getElementById('caspian-yt-theme-style'); if (s) s.remove(); } catch(e){}\n" +
                    "      return;\n" +
                    "    }\n" +
                    "\n" +
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
                    "    // 1. Google Gemini Theme Adaptation (STRICTLY for gemini.google.com, NEVER for general google.com search)\n" +
                    "    if (host.includes('gemini.google.com')) {\n" +
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
                    "    } else if (host.includes('google.')) {\n" +
                    "      // Remove any previously injected Gemini style from Google Search\n" +
                    "      try {\n" +
                    "        var prevStyle = document.getElementById('caspian-gemini-theme-style');\n" +
                    "        if (prevStyle) prevStyle.remove();\n" +
                    "        if (!isDark) {\n" +
                    "          if (document.documentElement) {\n" +
                    "            document.documentElement.removeAttribute('dark');\n" +
                    "            document.documentElement.removeAttribute('dark-theme');\n" +
                    "            document.documentElement.classList.remove('dark-theme');\n" +
                    "          }\n" +
                    "          if (document.body) {\n" +
                    "            document.body.removeAttribute('dark');\n" +
                    "            document.body.removeAttribute('dark-theme');\n" +
                    "            document.body.classList.remove('dark-theme');\n" +
                    "          }\n" +
                    "        }\n" +
                    "      } catch(e) {}\n" +
                    "    }\n" +
                    "\n" +
                    "    // 2. ChatGPT & AI Platforms Theme Adaptation\n" +
                    "    if (host.includes('chatgpt.com') || host.includes('openai.com') || host.includes('claude.ai') || host.includes('deepseek.com')) {\n" +
                    "      if (document.documentElement) {\n" +
                    "        document.documentElement.classList.toggle('dark', isDark);\n" +
                    "        document.documentElement.classList.toggle('light', !isDark);\n" +
                    "        document.documentElement.style.colorScheme = isDark ? 'dark' : 'light';\n" +
                    "      }\n" +
                    "      if (document.body) {\n" +
                    "        document.body.classList.toggle('dark', isDark);\n" +
                    "        document.body.classList.toggle('light', !isDark);\n" +
                    "        document.body.style.colorScheme = isDark ? 'dark' : 'light';\n" +
                    "      }\n" +
                    "      try { localStorage.setItem('theme', isDark ? 'dark' : 'light'); } catch(e){}\n" +
                    "      try { localStorage.setItem('colorMode', isDark ? 'dark' : 'light'); } catch(e){}\n" +
                    "      try { localStorage.setItem('color-theme', isDark ? 'dark' : 'light'); } catch(e){}\n" +
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

        // Sync to control sheet webview (Caspian Menu sheet)
        if (controlWebView != null) {
            controlWebView.evaluateJavascript(
                "if (typeof window.setThemeFromHost === 'function') { window.setThemeFromHost('" + (isDark ? "dark" : "light") + "'); } " +
                "else if (typeof window.setTheme === 'function') { window.setTheme('" + (isDark ? "dark" : "light") + "'); }",
                null
            );
        }

        updateOmniboxState();
    }

    public TabItem getYouTubeTab() {
        TabItem cur = getActiveOrDominantTab();
        if (cur != null && cur.url != null && (cur.url.toLowerCase().contains("youtube.com") || "youtube".equalsIgnoreCase(cur.service) || "youtubemusic".equalsIgnoreCase(cur.service))) {
            return cur;
        }
        if (tabsList != null) {
            for (TabItem t : tabsList) {
                if (t != null && t.url != null && (t.url.toLowerCase().contains("youtube.com") || "youtube".equalsIgnoreCase(t.service) || "youtubemusic".equalsIgnoreCase(t.service))) {
                    return t;
                }
            }
        }
        return null;
    }

    public PlaybackStateCompat buildPlaybackState(boolean isPlaying, long posMs, float speed) {
        int state = isPlaying ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED;
        int repeatResId;
        String repeatLabel;
        if (currentMediaRepeatMode == PlaybackStateCompat.REPEAT_MODE_ONE) {
            repeatResId = R.drawable.ic_pod_repeat_one;
            repeatLabel = "Repeat: One";
        } else if (currentMediaRepeatMode == PlaybackStateCompat.REPEAT_MODE_ALL) {
            repeatResId = R.drawable.ic_pod_repeat;
            repeatLabel = "Repeat: All";
        } else {
            repeatResId = R.drawable.ic_pod_repeat_off;
            repeatLabel = "Repeat: Off";
        }

        int shuffleResId = (currentMediaShuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL)
                ? R.drawable.ic_pod_shuffle
                : R.drawable.ic_pod_shuffle_off;
        String shuffleLabel = (currentMediaShuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL)
                ? "Shuffle: On"
                : "Shuffle: Off";

        return new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PAUSE
                        | PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                        | PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SEEK_TO
                        | PlaybackStateCompat.ACTION_SET_REPEAT_MODE | PlaybackStateCompat.ACTION_SET_SHUFFLE_MODE)
                .addCustomAction(new PlaybackStateCompat.CustomAction.Builder("ACTION_TOGGLE_SHUFFLE", shuffleLabel, shuffleResId).build())
                .addCustomAction(new PlaybackStateCompat.CustomAction.Builder("ACTION_TOGGLE_REPEAT", repeatLabel, repeatResId).build())
                .setState(state, posMs, speed, android.os.SystemClock.elapsedRealtime())
                .build();
    }

    public void previousYouTubeTrack() {
        TabItem currentTab = getYouTubeTab();
        if (currentTab != null && currentTab.webView != null) {
            currentTab.webView.evaluateJavascript(
                    "if (window.__CaspianYouTube) { window.__CaspianYouTube.previousTrack(); } " +
                    "else { var v = document.querySelector('video'); if (v && v.currentTime > 10) { v.currentTime = 0; } " +
                    "else { var b = document.querySelector('ytmusic-player-bar .previous-button, .previous-button, [aria-label*=\"Previous\" i], .ytp-prev-button'); if (b) b.click(); } }", null
            );
        }
    }

    public void nextYouTubeTrack() {
        TabItem currentTab = getYouTubeTab();
        if (currentTab != null && currentTab.webView != null) {
            currentTab.webView.evaluateJavascript(
                    "if (window.__CaspianYouTube) window.__CaspianYouTube.nextTrack(); " +
                    "else { var p = document.getElementById('movie_player'); if (p && typeof p.nextVideo === 'function') p.nextVideo(); " +
                    "else { var b = document.querySelector('ytmusic-player-bar .next-button, .next-button, [aria-label*=\"Next\" i], .ytp-next-button'); if (b) b.click(); } }", null
            );
        }
    }

    public void toggleYouTubeRepeat() {
        TabItem currentTab = getYouTubeTab();
        if (currentTab != null && currentTab.webView != null) {
            currentTab.webView.evaluateJavascript(
                    "if (window.__CaspianYouTube) window.__CaspianYouTube.toggleRepeat(); " +
                    "else { var b = document.querySelector('ytmusic-player-bar .repeat, [aria-label*=\"repeat\" i], [aria-label*=\"Repeat\" i]'); if (b) b.click(); }", null
            );
        }
        if (currentMediaRepeatMode == PlaybackStateCompat.REPEAT_MODE_NONE) {
            currentMediaRepeatMode = PlaybackStateCompat.REPEAT_MODE_ALL;
        } else if (currentMediaRepeatMode == PlaybackStateCompat.REPEAT_MODE_ALL) {
            currentMediaRepeatMode = PlaybackStateCompat.REPEAT_MODE_ONE;
        } else {
            currentMediaRepeatMode = PlaybackStateCompat.REPEAT_MODE_NONE;
        }
        if (mediaSession != null) {
            mediaSession.setRepeatMode(currentMediaRepeatMode);
            TabItem yt = getYouTubeTab();
            boolean isPlaying = yt != null && yt.isPlayingAudio;
            long posMs = (long)(currentVideoTime * 1000);
            mediaSession.setPlaybackState(buildPlaybackState(isPlaying, posMs, isPlaying ? ytCurrentSpeed : 0.0f));
        }
        TabItem yt = getYouTubeTab();
        boolean isPlaying = yt != null && yt.isPlayingAudio;
        updateMediaPlaybackNotification(isPlaying);
    }

    public void toggleYouTubeShuffle() {
        TabItem currentTab = getYouTubeTab();
        if (currentTab != null && currentTab.webView != null) {
            currentTab.webView.evaluateJavascript(
                    "if (window.__CaspianYouTube) window.__CaspianYouTube.toggleShuffle(); " +
                    "else { var b = document.querySelector('ytmusic-player-bar .shuffle, [aria-label*=\"shuffle\" i], [aria-label*=\"Shuffle\" i]'); if (b) b.click(); }", null
            );
        }
        currentMediaShuffleMode = (currentMediaShuffleMode == PlaybackStateCompat.SHUFFLE_MODE_NONE)
                ? PlaybackStateCompat.SHUFFLE_MODE_ALL
                : PlaybackStateCompat.SHUFFLE_MODE_NONE;
        if (mediaSession != null) {
            mediaSession.setShuffleMode(currentMediaShuffleMode);
            TabItem yt = getYouTubeTab();
            boolean isPlaying = yt != null && yt.isPlayingAudio;
            long posMs = (long)(currentVideoTime * 1000);
            mediaSession.setPlaybackState(buildPlaybackState(isPlaying, posMs, isPlaying ? ytCurrentSpeed : 0.0f));
        }
        TabItem yt = getYouTubeTab();
        boolean isPlaying = yt != null && yt.isPlayingAudio;
        updateMediaPlaybackNotification(isPlaying);
    }

    public void updateMediaPlaybackModes(int repeatMode, boolean shuffleOn) {
        int newRepeat = (repeatMode == 2) ? PlaybackStateCompat.REPEAT_MODE_ONE : ((repeatMode == 1) ? PlaybackStateCompat.REPEAT_MODE_ALL : PlaybackStateCompat.REPEAT_MODE_NONE);
        int newShuffle = shuffleOn ? PlaybackStateCompat.SHUFFLE_MODE_ALL : PlaybackStateCompat.SHUFFLE_MODE_NONE;
        if (newRepeat != currentMediaRepeatMode || newShuffle != currentMediaShuffleMode) {
            currentMediaRepeatMode = newRepeat;
            currentMediaShuffleMode = newShuffle;
            if (mediaSession != null) {
                mediaSession.setRepeatMode(currentMediaRepeatMode);
                mediaSession.setShuffleMode(currentMediaShuffleMode);
                TabItem yt = getYouTubeTab();
                boolean isPlaying = yt != null && yt.isPlayingAudio;
                long posMs = (long)(currentVideoTime * 1000);
                mediaSession.setPlaybackState(buildPlaybackState(isPlaying, posMs, isPlaying ? ytCurrentSpeed : 0.0f));
            }
            TabItem yt = getYouTubeTab();
            boolean isPlaying = yt != null && yt.isPlayingAudio;
            updateMediaPlaybackNotification(isPlaying);
        }
    }

    public void togglePlayYouTube() {
        TabItem currentTab = getYouTubeTab();
        if (currentTab != null && currentTab.webView != null) {
            currentTab.webView.evaluateJavascript(
                    "if (window.__CaspianYouTube) window.__CaspianYouTube.togglePlay(); else { var v = document.querySelector('video'); if (v) { if (v.paused) v.play(); else v.pause(); } }", null
            );
        }
    }

    public void toggleFullscreenYouTube() {
        if (customView != null) {
            exitFullscreenCustomView();
            return;
        }
        TabItem currentTab = getTabById(activeTabId);
        if (currentTab != null && currentTab.webView != null) {
            final WebView wv = currentTab.webView;
            int w = wv.getWidth();
            int h = wv.getHeight();
            float cx = (w > 0) ? (w / 2f) : 300f;
            float cy = (h > 0) ? Math.min(h * 0.25f, (float) dpToPx(140)) : 300f;

            long now = SystemClock.uptimeMillis();
            MotionEvent evDown = MotionEvent.obtain(now, now, MotionEvent.ACTION_DOWN, cx, cy, 0);
            MotionEvent evUp = MotionEvent.obtain(now, now + 30, MotionEvent.ACTION_UP, cx, cy, 0);
            wv.dispatchTouchEvent(evDown);
            wv.dispatchTouchEvent(evUp);
            evDown.recycle();
            evUp.recycle();

            wv.postDelayed(() -> {
                wv.evaluateJavascript(
                        "(function(){ " +
                        "  if (window.__CaspianYouTube && typeof window.__CaspianYouTube.toggleFullscreen === 'function') { " +
                        "    window.__CaspianYouTube.toggleFullscreen(); " +
                        "  } else { " +
                        "    var fs = document.querySelector('.ytp-fullscreen-button, button.ytp-fullscreen-button, .fullscreen-icon, ytm-fullscreen-button, button[aria-label*=\"Fullscreen\"], button[aria-label*=\"fullscreen\"]'); " +
                        "    if (fs && (fs.offsetWidth > 0 || fs.offsetHeight > 0)) { fs.click(); return; } " +
                        "    var v = document.querySelector('video'); " +
                        "    if (v) { " +
                        "      if (v.paused) v.play().catch(()=>{}); " +
                        "      if (typeof v.webkitEnterFullscreen === 'function') { try { v.webkitEnterFullscreen(); return; } catch(e){} } " +
                        "      else if (typeof v.requestFullscreen === 'function') { try { v.requestFullscreen().catch(()=>{}); return; } catch(e){} } " +
                        "    } " +
                        "    if (fs) { fs.click(); } " +
                        "  } " +
                        "})()", null
                );
            }, 80);
        }
    }

    public void showYouTubePlayerControls() {
        TabItem currentTab = getTabById(activeTabId);
        if (currentTab != null && currentTab.webView != null) {
            currentTab.webView.evaluateJavascript(
                    "if (window.__CaspianYouTube && typeof window.__CaspianYouTube.showPlayerControls === 'function') { " +
                    "  window.__CaspianYouTube.showPlayerControls(); " +
                    "} else { " +
                    "  var v = document.querySelector('video'); " +
                    "  if (v) { v.controls = false; v.offsetHeight; v.controls = true; } " +
                    "}", null
            );
        }
    }

    public void exitFullscreenCustomView() {
        runOnUiThread(() -> {
            TabItem currentTab = getTabById(activeTabId);
            if (currentTab != null && currentTab.webView != null) {
                currentTab.webView.evaluateJavascript(
                        "try { var v = document.querySelector('video'); if (v) v.controls = false; } catch(e){}" +
                        "if (document.fullscreenElement || document.webkitFullscreenElement) { " +
                        "  if (document.exitFullscreen) document.exitFullscreen().catch(()=>{}); " +
                        "  else if (document.webkitExitFullscreen) document.webkitExitFullscreen(); " +
                        "}", null
                );
            }
            if (customView != null) {
                fullscreenContainer.removeView(customView);
                customView = null;
                fullscreenContainer.setVisibility(View.GONE);
                if (customViewCallback != null) {
                    try {
                        customViewCallback.onCustomViewHidden();
                    } catch (Exception ignored) {}
                    customViewCallback = null;
                }
            }
            try {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            } catch (Exception ignored) {}

            if (ytFloatingRemoteContainer != null) {
                if (ytFloatingRemoteContainer.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                    ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) ytFloatingRemoteContainer.getLayoutParams();
                    lp.bottomMargin = dpToPx(205);
                    lp.rightMargin = dpToPx(8);
                    ytFloatingRemoteContainer.setLayoutParams(lp);
                }
                ytFloatingRemoteContainer.setElevation(dpToPx(250));
                ytFloatingRemoteContainer.setTranslationZ(0f);
            }
            if (floatingCaspianCard != null) {
                floatingCaspianCard.setVisibility(View.VISIBLE);
            }
            applyScreenTouchLockState(false);
            String behavior = getYtTimelineDefaultBehavior();
            boolean keepInVertical = "vertical_only".equals(behavior) || "both".equals(behavior);
            if (keepInVertical) {
                isTimelineUserEnabled = true;
                if (ytFloatingTimelineBar != null) {
                    syncTimelineBarWidth();
                    ytFloatingTimelineBar.setVisibility(View.VISIBLE);
                    if (ytRemoteTimeline != null) {
                        try {
                            ytRemoteTimeline.setColorFilter(Color.parseColor(podStartColor));
                        } catch (Exception e) {
                            ytRemoteTimeline.setColorFilter(0xFF00E5FF);
                        }
                    }
                }
            } else {
                isTimelineUserEnabled = false;
                if (ytFloatingTimelineBar != null) {
                    ytFloatingTimelineBar.setVisibility(View.GONE);
                }
                if (ytRemoteTimeline != null) {
                    ytRemoteTimeline.setColorFilter(0xFFFFFFFF);
                }
            }
            if (volumePopupWindow != null && volumePopupWindow.isShowing()) {
                volumePopupWindow.dismiss();
            }
            if (ytRemoteFullscreen != null) {
                ytRemoteFullscreen.setImageResource(R.drawable.ic_pod_fullscreen);
                ytRemoteFullscreen.setContentDescription("Fullscreen Toggle");
            }
        });
    }

    public String getYtTimelineDefaultBehavior() {
        try {
            SharedPreferences prefs = getSharedPreferences("CaspianFlowPrefs", Context.MODE_PRIVATE);
            return prefs.getString("yt_timeline_default_behavior", "fullscreen_only");
        } catch (Exception e) {
            return "fullscreen_only";
        }
    }

    public void applyTimelineDefaultBehavior(String behavior) {
        boolean isFs = customView != null;
        boolean shouldShow = (isFs && ("fullscreen_only".equals(behavior) || "both".equals(behavior)))
                || (!isFs && ("vertical_only".equals(behavior) || "both".equals(behavior)));
        if (ytFloatingTimelineBar != null) {
            if (shouldShow) {
                isTimelineUserEnabled = true;
                syncTimelineBarWidth();
                ytFloatingTimelineBar.setVisibility(View.VISIBLE);
                if (ytRemoteTimeline != null) {
                    try {
                        ytRemoteTimeline.setColorFilter(Color.parseColor(podStartColor));
                    } catch (Exception e) {
                        ytRemoteTimeline.setColorFilter(0xFF00E5FF);
                    }
                }
            } else {
                isTimelineUserEnabled = false;
                ytFloatingTimelineBar.setVisibility(View.GONE);
                if (ytRemoteTimeline != null) ytRemoteTimeline.setColorFilter(0xFFFFFFFF);
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        syncTimelineBarWidth();
    }

    public void updateYouTubeTimeLive(Integer tabId, double currentTime, double duration) {
        TabItem ytTab = getYouTubeTab();
        if (tabId != null && tabId > 0 && ytTab != null && tabId != ytTab.id && tabId != activeTabId) {
            return;
        }
        updateYouTubeTimeLive(currentTime, duration);
    }

    public void updateYouTubeTimeLive(double currentTime, double duration) {
        if (isUserScrubbingTimeline) return;
        currentVideoTime = currentTime;
        currentVideoDuration = duration;
        if (ytTimelineCurrentTime != null) {
            ytTimelineCurrentTime.setText(formatTime(currentTime));
        }
        if (ytTimelineTotalTime != null) {
            ytTimelineTotalTime.setText(formatTime(duration));
        }
        if (ytTimelineSeekbar != null && duration > 0) {
            int progress = (int) Math.min(1000, Math.max(0, (currentTime / duration) * 1000));
            ytTimelineSeekbar.setProgress(progress);
        }

        long now = android.os.SystemClock.elapsedRealtime();
        if (mediaSession != null && (now - lastMediaSessionTimeUpdateMs > 8000)) {
            lastMediaSessionTimeUpdateMs = now;
            TabItem yt = getYouTubeTab();
            boolean isPlaying = yt != null && yt.isPlayingAudio;
            long posMs = (long)(currentTime * 1000);
            mediaSession.setPlaybackState(buildPlaybackState(isPlaying, posMs, isPlaying ? ytCurrentSpeed : 0.0f));
        }
    }

    private String formatTime(double seconds) {
        if (Double.isNaN(seconds) || seconds < 0) return "00:00";
        int totalSec = (int) Math.round(seconds);
        int m = totalSec / 60;
        int s = totalSec % 60;
        int h = m / 60;
        m = m % 60;
        if (h > 0) {
            return String.format(Locale.US, "%d:%02d:%02d", h, m, s);
        } else {
            return String.format(Locale.US, "%02d:%02d", m, s);
        }
    }

    public void seekYouTubeTo(double targetSec) {
        TabItem currentTab = getYouTubeTab();
        if (currentTab != null && currentTab.webView != null) {
            currentTab.webView.evaluateJavascript(
                    "if (window.__CaspianYouTube) window.__CaspianYouTube.seekTo(" + targetSec + "); else { var v = document.querySelector('video'); if (v) v.currentTime = " + targetSec + "; }", null
            );
        }
    }

    public void syncTimelineBarWidth() {
        if (ytFloatingTimelineBar == null) return;
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int screenW = dm.widthPixels;
        boolean isCollapsed = ytFloatingRemoteBall != null && ytFloatingRemoteBall.getVisibility() == View.VISIBLE;

        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) ytFloatingTimelineBar.getLayoutParams();
        if (lp != null) {
            if (isCollapsed) {
                // Ball is 44dp high, timeline is 38dp high.
                // (44 - 38) / 2 = 3dp top and 3dp bottom margin to align optical centers with exact precision!
                lp.rightMargin = dpToPx(50);
                lp.topMargin = dpToPx(3);
                lp.bottomMargin = dpToPx(3);
                lp.width = Math.min(dpToPx(360), screenW - dpToPx(72));
            } else {
                lp.rightMargin = 0;
                lp.topMargin = 0;
                lp.bottomMargin = dpToPx(6);
                int scrollW = 0;
                if (ytFloatingRemoteScroll != null) {
                    scrollW = ytFloatingRemoteScroll.getWidth();
                    if (scrollW <= 0) scrollW = ytFloatingRemoteScroll.getMeasuredWidth();
                }
                if (scrollW > 0) {
                    lp.width = Math.min(scrollW, screenW - dpToPx(16));
                } else {
                    lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                }
            }
            ytFloatingTimelineBar.setLayoutParams(lp);
        }
    }

    public void applyScreenTouchLockState(boolean locked) {
        isScreenTouchLocked = locked;
        TabItem currentTab = getActiveOrDominantTab();
        String curUrl = (currentTab != null && currentTab.url != null) ? currentTab.url : "";
        boolean isYt = currentTab != null && (
                "youtube".equalsIgnoreCase(currentTab.service) ||
                curUrl.toLowerCase().contains("youtube.com")
        );
        boolean shouldLock = isScreenTouchLocked && isYt;
        if (videoTouchLockOverlay != null) {
            videoTouchLockOverlay.setVisibility(shouldLock ? View.VISIBLE : View.GONE);
            if (shouldLock) {
                // Completely silent touch blocker - only locks YouTube tab touch input
                videoTouchLockOverlay.setOnTouchListener((v, event) -> true);
            }
        }
        if (ytRemoteLock != null) {
            ytRemoteLock.setImageResource(isScreenTouchLocked ? R.drawable.ic_pod_lock : R.drawable.ic_pod_unlock);
            ytRemoteLock.setColorFilter(isScreenTouchLocked ? 0xFFFF5252 : 0xFFFFFFFF);
        }
    }

    public void toggleScreenTouchLock() {
        playUiFeedbackSound("tap");
        applyScreenTouchLockState(!isScreenTouchLocked);
        Toast.makeText(this, isScreenTouchLocked ? "🔒 Screen Locked" : "🔓 Screen Unlocked", Toast.LENGTH_SHORT).show();
    }

    public void updateVolumeButtonDisplay() {
        try {
            AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            if (am != null) {
                int cur = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                int max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                int pct = max > 0 ? (cur * 100 / max) : 100;
                if (ytRemoteVolumeBtn != null) {
                    ytRemoteVolumeBtn.setText(pct + "%");
                }
            }
        } catch (Exception ignored) {}
    }

    public void showFloatingVolumePopup(View anchor) {
        playUiFeedbackSound("tap");
        if (volumePopupWindow != null && volumePopupWindow.isShowing()) {
            volumePopupWindow.dismiss();
            return;
        }

        View popupView = getLayoutInflater().inflate(R.layout.popup_caspian_volume, null);
        TextView percentTv = popupView.findViewById(R.id.popup_volume_percent);
        SeekBar volumeSeekBar = popupView.findViewById(R.id.popup_volume_seekbar);
        ImageView volumeIcon = popupView.findViewById(R.id.popup_volume_icon);

        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int max = (am != null) ? am.getStreamMaxVolume(AudioManager.STREAM_MUSIC) : 15;
        int cur = (am != null) ? am.getStreamVolume(AudioManager.STREAM_MUSIC) : 15;
        int currentPct = max > 0 ? (cur * 100 / max) : 100;

        percentTv.setText(currentPct + "%");
        volumeSeekBar.setProgress(currentPct);
        try {
            int accentColor = Color.parseColor(podStartColor);
            percentTv.setTextColor(accentColor);
            volumeSeekBar.setThumbTintList(ColorStateList.valueOf(accentColor));
            volumeSeekBar.setProgressTintList(ColorStateList.valueOf(accentColor));
        } catch (Exception ignored) {}

        if (volumeIcon != null) {
            volumeIcon.setImageResource(currentPct == 0 ? R.drawable.ic_pod_mute : R.drawable.ic_pod_sound);
        }

        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                percentTv.setText(progress + "%");
                if (ytRemoteVolumeBtn != null) ytRemoteVolumeBtn.setText(progress + "%");
                if (volumeIcon != null) {
                    volumeIcon.setImageResource(progress == 0 ? R.drawable.ic_pod_mute : R.drawable.ic_pod_sound);
                }
                if (fromUser) {
                    try {
                        if (am != null && max > 0) {
                            int targetStreamVol = (int) Math.round((progress / 100.0) * max);
                            am.setStreamVolume(AudioManager.STREAM_MUSIC, targetStreamVol, 0);
                        }
                        TabItem tab = getTabById(activeTabId);
                        if (tab != null && tab.webView != null) {
                            tab.webView.evaluateJavascript(
                                    "if (window.__CaspianYouTube) window.__CaspianYouTube.setVolume(" + (progress / 100.0) + "); else { var v = document.querySelector('video'); if (v) v.volume = " + (progress / 100.0) + "; }", null
                            );
                        }
                    } catch (Exception ignored) {}
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        volumePopupWindow = new PopupWindow(
                popupView,
                dpToPx(56),
                dpToPx(210),
                true
        );
        volumePopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        volumePopupWindow.setElevation(dpToPx(30));

        int[] anchorLoc = new int[2];
        anchor.getLocationOnScreen(anchorLoc);
        int anchorX = anchorLoc[0];
        int anchorY = anchorLoc[1];
        int targetW = dpToPx(56);
        int targetH = dpToPx(210);

        DisplayMetrics dm = getResources().getDisplayMetrics();
        int screenW = dm.widthPixels;
        int screenH = dm.heightPixels;

        int posX = anchorX + (anchor.getWidth() - targetW) / 2;
        if (posX + targetW > screenW - dpToPx(8)) posX = screenW - targetW - dpToPx(8);
        if (posX < dpToPx(8)) posX = dpToPx(8);

        int posY = anchorY - targetH - dpToPx(8);
        if (posY < dpToPx(8)) posY = anchorY + anchor.getHeight() + dpToPx(8);

        volumePopupWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, posX, posY);
    }

    public void showYouTubeFeaturesPopup(View anchor) {
        playUiFeedbackSound("tap");
        View popupView = getLayoutInflater().inflate(R.layout.popup_youtube_features, null);
        PopupWindow popup = new PopupWindow(
                popupView,
                dpToPx(210),
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );
        popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popup.setElevation(dpToPx(30));
        popup.setOutsideTouchable(true);

        TabItem currentTab = getTabById(activeTabId);

        // 1. Captions (CC)
        LinearLayout btnCaptions = popupView.findViewById(R.id.yt_feature_captions);
        if (btnCaptions != null) {
            btnCaptions.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                if (currentTab != null && currentTab.webView != null) {
                    currentTab.webView.evaluateJavascript("if (window.__CaspianYouTube) window.__CaspianYouTube.toggleCaptions();", null);
                }
                popup.dismiss();
            });
        }

        // 2. Loop Video
        LinearLayout btnLoop = popupView.findViewById(R.id.yt_feature_loop);
        if (btnLoop != null) {
            btnLoop.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                if (currentTab != null && currentTab.webView != null) {
                    currentTab.webView.evaluateJavascript("if (window.__CaspianYouTube) window.__CaspianYouTube.toggleLoop();", res -> {
                        boolean looping = "true".equalsIgnoreCase(res);
                        Toast.makeText(this, looping ? "🔁 Video Looping: ON" : "🔁 Video Looping: OFF", Toast.LENGTH_SHORT).show();
                    });
                }
                popup.dismiss();
            });
        }

        // 3. Ambient / Cinema Mode
        LinearLayout btnAmbient = popupView.findViewById(R.id.yt_feature_ambient);
        if (btnAmbient != null) {
            btnAmbient.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                if (currentTab != null && currentTab.webView != null) {
                    currentTab.webView.evaluateJavascript("if (window.__CaspianYouTube) window.__CaspianYouTube.toggleAmbient();", null);
                }
                popup.dismiss();
            });
        }

        // 4. Autoplay Next
        LinearLayout btnAutoplay = popupView.findViewById(R.id.yt_feature_autoplay);
        if (btnAutoplay != null) {
            btnAutoplay.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                if (currentTab != null && currentTab.webView != null) {
                    currentTab.webView.evaluateJavascript("if (window.__CaspianYouTube) window.__CaspianYouTube.toggleAutoplay();", null);
                }
                popup.dismiss();
            });
        }

        // 5. Picture-in-Picture
        LinearLayout btnPip = popupView.findViewById(R.id.yt_feature_pip);
        if (btnPip != null) {
            btnPip.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                if (currentTab != null && currentTab.webView != null) {
                    currentTab.webView.evaluateJavascript("if (window.__CaspianYouTube) window.__CaspianYouTube.togglePip();", null);
                }
                popup.dismiss();
            });
        }

        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int targetW = dpToPx(210);
        int targetH = popupView.getMeasuredHeight();

        int[] anchorLoc = new int[2];
        anchor.getLocationOnScreen(anchorLoc);
        int anchorX = anchorLoc[0];
        int anchorY = anchorLoc[1];

        DisplayMetrics dm = getResources().getDisplayMetrics();
        int screenW = dm.widthPixels;

        int posX = anchorX + (anchor.getWidth() - targetW) / 2;
        if (posX + targetW > screenW - dpToPx(8)) posX = screenW - targetW - dpToPx(8);
        if (posX < dpToPx(8)) posX = dpToPx(8);

        int posY = anchorY - targetH - dpToPx(8);
        if (posY < dpToPx(8)) posY = anchorY + anchor.getHeight() + dpToPx(8);

        popup.showAtLocation(anchor, Gravity.NO_GRAVITY, posX, posY);
    }

    public void showYouTubeQualityPopup(View anchor) {
        playUiFeedbackSound("tap");
        TabItem currentTab = getTabById(activeTabId);
        if (currentTab != null && currentTab.webView != null) {
            currentTab.webView.evaluateJavascript(
                    "(function(){ if (window.__CaspianYouTube) return window.__CaspianYouTube.getAvailableQualities(); return ''; })()",
                    value -> runOnUiThread(() -> renderDynamicQualityPopup(anchor, value))
            );
        } else {
            renderDynamicQualityPopup(anchor, null);
        }
    }

    private void renderDynamicQualityPopup(View anchor, String rawJson) {
        List<CaspianMenuItem> qualityItems = new ArrayList<>();
        boolean parsedSuccessfully = false;

        if (rawJson != null && !rawJson.isEmpty() && !"null".equalsIgnoreCase(rawJson) && !"\"\"".equals(rawJson)) {
            try {
                String cleanJson = rawJson;
                if (cleanJson.startsWith("\"") && cleanJson.endsWith("\"")) {
                    cleanJson = new org.json.JSONTokener(cleanJson).nextValue().toString();
                }
                org.json.JSONArray arr = new org.json.JSONArray(cleanJson);
                if (arr.length() > 0) {
                    for (int i = 0; i < arr.length(); i++) {
                        org.json.JSONObject obj = arr.getJSONObject(i);
                        final String code = obj.optString("code", "");
                        String label = obj.optString("label", code);
                        if (code.isEmpty()) continue;

                        if ("auto".equalsIgnoreCase(code)) {
                            qualityItems.add(new CaspianMenuItem("⚡ " + label, () -> setYouTubeQuality("auto")));
                        } else if (label.contains("2160") || label.contains("4K") || "hd2160".equalsIgnoreCase(code)) {
                            qualityItems.add(new CaspianMenuItem("💎 " + label, () -> setYouTubeQuality(code)));
                        } else if (label.contains("1440") || label.contains("2K") || "hd1440".equalsIgnoreCase(code)) {
                            qualityItems.add(new CaspianMenuItem("🌟 " + label, () -> setYouTubeQuality(code)));
                        } else if (label.contains("1080") || "hd1080".equalsIgnoreCase(code)) {
                            qualityItems.add(new CaspianMenuItem("✨ " + label, () -> setYouTubeQuality(code)));
                        } else if (label.contains("720") || "hd720".equalsIgnoreCase(code)) {
                            qualityItems.add(new CaspianMenuItem("✨ " + label, () -> setYouTubeQuality(code)));
                        } else if (label.contains("480") || "large".equalsIgnoreCase(code)) {
                            qualityItems.add(new CaspianMenuItem("📺 " + label, () -> setYouTubeQuality(code)));
                        } else if (label.contains("360") || "medium".equalsIgnoreCase(code)) {
                            qualityItems.add(new CaspianMenuItem("📱 " + label, () -> setYouTubeQuality(code)));
                        } else if (label.contains("240") || "small".equalsIgnoreCase(code)) {
                            qualityItems.add(new CaspianMenuItem("📶 " + label, () -> setYouTubeQuality(code)));
                        } else if (label.contains("144") || "tiny".equalsIgnoreCase(code)) {
                            qualityItems.add(new CaspianMenuItem("💾 " + label, () -> setYouTubeQuality(code)));
                        } else {
                            qualityItems.add(new CaspianMenuItem("▶ " + label, () -> setYouTubeQuality(code)));
                        }
                    }
                    parsedSuccessfully = true;
                }
            } catch (Exception e) {
                Log.w(TAG, "Failed parsing dynamic qualities: " + e.getMessage());
            }
        }

        if (!parsedSuccessfully || qualityItems.isEmpty()) {
            qualityItems.clear();
            qualityItems.add(new CaspianMenuItem("⚡ Auto Quality", () -> setYouTubeQuality("auto")));
            qualityItems.add(new CaspianMenuItem("💎 2160p (4K)", () -> setYouTubeQuality("hd2160")));
            qualityItems.add(new CaspianMenuItem("🌟 1440p (2K)", () -> setYouTubeQuality("hd1440")));
            qualityItems.add(new CaspianMenuItem("✨ 1080p (HD)", () -> setYouTubeQuality("hd1080")));
            qualityItems.add(new CaspianMenuItem("✨ 720p (HD)", () -> setYouTubeQuality("hd720")));
            qualityItems.add(new CaspianMenuItem("📺 480p", () -> setYouTubeQuality("large")));
            qualityItems.add(new CaspianMenuItem("📱 360p", () -> setYouTubeQuality("medium")));
            qualityItems.add(new CaspianMenuItem("📶 240p", () -> setYouTubeQuality("small")));
            qualityItems.add(new CaspianMenuItem("💾 144p (Data Saver)", () -> setYouTubeQuality("tiny")));
        }

        showCaspianCustomPopup(anchor, qualityItems);
    }

    public void showYouTubeSpeedPopup(View anchor) {
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
        showCaspianCustomPopup(anchor, speedItems);
    }

    public void showYouTubeSettingsMenu() {
        runOnUiThread(() -> {
            playUiFeedbackSound("tap");
            View anchor = (ytRemoteQualityBtn != null && ytRemoteQualityBtn.getVisibility() == View.VISIBLE)
                    ? ytRemoteQualityBtn
                    : ((ytFloatingRemoteContainer != null && ytFloatingRemoteContainer.getVisibility() == View.VISIBLE)
                        ? ytFloatingRemoteContainer
                        : fullscreenContainer);
            if (anchor == null) return;

            List<CaspianMenuItem> menuItems = new ArrayList<>();
            menuItems.add(new CaspianMenuItem("🎬 Quality Options", () -> showYouTubeQualityPopup(anchor)));
            menuItems.add(new CaspianMenuItem("⚡ Playback Speed", () -> showYouTubeSpeedPopup(anchor)));
            menuItems.add(new CaspianMenuItem("💬 Toggle Captions (CC)", () -> {
                TabItem currentTab = getTabById(activeTabId);
                if (currentTab != null && currentTab.webView != null) {
                    currentTab.webView.evaluateJavascript(
                            "(function(){ var cc = document.querySelector('.ytp-subtitles-button, button.ytp-subtitles-button, button[aria-label*=\"Captions\"], button[aria-label*=\"captions\"]'); if (cc) cc.click(); })()", null
                    );
                }
            }));
            menuItems.add(new CaspianMenuItem("🔁 Toggle Loop", () -> {
                TabItem currentTab = getTabById(activeTabId);
                if (currentTab != null && currentTab.webView != null) {
                    currentTab.webView.evaluateJavascript(
                            "(function(){ var v = document.querySelector('video'); if (v) { v.loop = !v.loop; } })()", null
                    );
                }
            }));
            menuItems.add(new CaspianMenuItem("🔊 Mute / Unmute", this::toggleMuteYouTube));

            showCaspianCustomPopup(anchor, menuItems);
        });
    }

    private void findAndConfigureSurfaceViews(View v) {
        if (v == null) return;
        if (v instanceof SurfaceView) {
            try {
                ((SurfaceView) v).setZOrderMediaOverlay(false);
            } catch (Exception ignored) {}
        } else if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            for (int i = 0; i < vg.getChildCount(); i++) {
                findAndConfigureSurfaceViews(vg.getChildAt(i));
            }
        }
    }

    public void toggleMuteYouTube() {
        TabItem currentTab = getYouTubeTab();
        if (currentTab != null && currentTab.webView != null) {
            currentTab.webView.evaluateJavascript(
                    "if (window.__CaspianYouTube) window.__CaspianYouTube.toggleMute(); else { var v = document.querySelector('video'); if (v) { v.muted = !v.muted; } }", null
            );
        }
    }

    public void seekYouTube(double seconds) {
        TabItem currentTab = getYouTubeTab();
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
            else if ("hd2160".equalsIgnoreCase(quality) || quality.contains("2160")) label = "4K";
            else if ("hd1440".equalsIgnoreCase(quality) || quality.contains("1440")) label = "1440p";
            else if ("hd1080".equalsIgnoreCase(quality) || quality.contains("1080")) label = "1080p";
            else if ("hd720".equalsIgnoreCase(quality) || quality.contains("720")) label = "720p";
            else if ("large".equalsIgnoreCase(quality) || quality.contains("480")) label = "480p";
            else if ("medium".equalsIgnoreCase(quality) || quality.contains("360")) label = "360p";
            else if ("small".equalsIgnoreCase(quality) || quality.contains("240")) label = "240p";
            else if ("tiny".equalsIgnoreCase(quality) || quality.contains("144")) label = "144p";
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

    public void setYtRemoteAutoCollapse(boolean enabled) {
        this.isYtRemoteAutoCollapse = enabled;
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putBoolean("yt_pod_autocollapse", enabled).apply();
    }

    public void setChatgptDockAutoCollapse(boolean enabled) {
        this.isChatgptDockAutoCollapse = enabled;
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putBoolean("chatgpt_dock_autocollapse", enabled).apply();
    }

    public void setGeminiDockAutoCollapse(boolean enabled) {
        this.isGeminiDockAutoCollapse = enabled;
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putBoolean("gemini_dock_autocollapse", enabled).apply();
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
                CaspianPhysics.collapseDockWithSpring(chatgptDockContainer, chatgptDockBall, chatgptDockScroll);
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
                                chatgptDockContainer.setX(event.getRawX() + ballDX);
                                chatgptDockContainer.setY(event.getRawY() + ballDY);
                            }
                            return true;

                        case MotionEvent.ACTION_UP:
                            if (!isBallDragging) {
                                playUiFeedbackSound("tap");
                                CaspianPhysics.expandDockWithSpring(chatgptDockContainer, chatgptDockBall, chatgptDockScroll);
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
            try {
                SharedPreferences p = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                float sc = Float.parseFloat(p.getString("chatgpt_dock_scale", "1.0"));
                chatgptDockContainer.setScaleX(sc);
                chatgptDockContainer.setScaleY(sc);
            } catch (Exception ignored) {}
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
                CaspianPhysics.collapseDockWithSpring(geminiDockContainer, geminiDockBall, geminiDockScroll);
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
                                geminiDockContainer.setX(event.getRawX() + ballDX);
                                geminiDockContainer.setY(event.getRawY() + ballDY);
                            }
                            return true;

                        case MotionEvent.ACTION_UP:
                            if (!isBallDragging) {
                                playUiFeedbackSound("tap");
                                CaspianPhysics.expandDockWithSpring(geminiDockContainer, geminiDockBall, geminiDockScroll);
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
            try {
                SharedPreferences p = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                float sc = Float.parseFloat(p.getString("gemini_dock_scale", "1.0"));
                geminiDockContainer.setScaleX(sc);
                geminiDockContainer.setScaleY(sc);
            } catch (Exception ignored) {}
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

            if (ytRemoteSettings != null) {
                ytRemoteSettings.setOnClickListener(this::showYouTubeFeaturesPopup);
            }

            ytRemoteFullscreen.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                toggleFullscreenYouTube();
            });

            if (ytRemoteTimeline != null) {
                ytRemoteTimeline.setOnClickListener(v -> {
                    playUiFeedbackSound("tap");
                    if (ytFloatingTimelineBar != null) {
                        boolean isShown = ytFloatingTimelineBar.getVisibility() == View.VISIBLE;
                        isTimelineUserEnabled = !isShown;
                        if (!isShown) syncTimelineBarWidth();
                        ytFloatingTimelineBar.setVisibility(isShown ? View.GONE : View.VISIBLE);
                        ytRemoteTimeline.setColorFilter(isShown ? 0xFFFFFFFF : 0xFF00E5FF);
                        if (!isShown) {
                            TabItem tab = getTabById(activeTabId);
                            if (tab != null && tab.webView != null) {
                                tab.webView.evaluateJavascript(
                                        "(function(){ var v = document.querySelector('video'); if (v && window.CaspianBridge) window.CaspianBridge.updateYouTubeTime(v.currentTime||0, v.duration||0); })()", null
                                );
                            }
                        }
                    }
                });
            }

            if (ytRemotePip != null) {
                ytRemotePip.setOnClickListener(v -> {
                    playUiFeedbackSound("tap");
                    enterYouTubePiP();
                });
            }

            if (ytTimelineSeekbar != null) {
                ytTimelineSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser && currentVideoDuration > 0) {
                            double targetSec = (progress / 1000.0) * currentVideoDuration;
                            String timeStr = formatTime(targetSec);
                            if (ytTimelineCurrentTime != null) {
                                ytTimelineCurrentTime.setText(timeStr);
                            }
                            if (ytTimelineScrubBubble != null && ytTimelineScrubBubbleContainer != null) {
                                ytTimelineScrubBubble.setText(timeStr + " / " + formatTime(currentVideoDuration));
                                int paddingLeft = seekBar.getPaddingLeft();
                                int paddingRight = seekBar.getPaddingRight();
                                int availableWidth = seekBar.getWidth() - paddingLeft - paddingRight;
                                float thumbFraction = (float) progress / (float) seekBar.getMax();
                                float thumbCenterX = seekBar.getLeft() + paddingLeft + (availableWidth * thumbFraction);
                                int bubbleWidth = ytTimelineScrubBubble.getWidth();
                                if (bubbleWidth == 0) {
                                    ytTimelineScrubBubble.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                                    bubbleWidth = ytTimelineScrubBubble.getMeasuredWidth();
                                }
                                float targetX = thumbCenterX - (bubbleWidth / 2f);
                                int maxTargetX = ytTimelineScrubBubbleContainer.getWidth() - bubbleWidth;
                                if (maxTargetX > 0) {
                                    targetX = Math.max(0, Math.min(maxTargetX, targetX));
                                }
                                ytTimelineScrubBubble.setX(targetX);
                                if (ytTimelineScrubBubbleContainer.getVisibility() != View.VISIBLE) {
                                    ytTimelineScrubBubbleContainer.setVisibility(View.VISIBLE);
                                    ytTimelineScrubBubbleContainer.setAlpha(1.0f);
                                }
                            }
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        isUserScrubbingTimeline = true;
                        if (ytTimelineScrubBubbleContainer != null) {
                            ytTimelineScrubBubbleContainer.animate().cancel();
                            ytTimelineScrubBubbleContainer.setVisibility(View.VISIBLE);
                            ytTimelineScrubBubbleContainer.setAlpha(1.0f);
                        }
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        if (currentVideoDuration > 0) {
                            double targetSec = (seekBar.getProgress() / 1000.0) * currentVideoDuration;
                            seekYouTubeTo(targetSec);
                        }
                        isUserScrubbingTimeline = false;
                        if (ytTimelineScrubBubbleContainer != null) {
                            ytTimelineScrubBubbleContainer.animate()
                                    .alpha(0.0f)
                                    .setDuration(250)
                                    .withEndAction(() -> {
                                        if (ytTimelineScrubBubbleContainer != null) {
                                            ytTimelineScrubBubbleContainer.setVisibility(View.GONE);
                                        }
                                    })
                                    .start();
                        }
                    }
                });
            }

            if (ytRemoteLock != null) {
                applyScreenTouchLockState(false);
                ytRemoteLock.setOnClickListener(v -> toggleScreenTouchLock());
            }

            ytRemotePrevVideo.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                TabItem currentTab = getTabById(activeTabId);
                if (currentTab != null && currentTab.webView != null) {
                    currentTab.webView.evaluateJavascript("if (window.__CaspianYouTube) window.__CaspianYouTube.previousVideo();", null);
                }
            });

            if (ytTimelinePlayPause != null) {
                ytTimelinePlayPause.setOnClickListener(v -> {
                    playUiFeedbackSound("tap");
                    togglePlayYouTube();
                });
            }

            if (ytTimelineCollapse != null) {
                ytTimelineCollapse.setOnClickListener(v -> {
                    playUiFeedbackSound("tap");
                    if (ytFloatingTimelineBar != null && ytFloatingTimelineBar.getVisibility() == View.VISIBLE) {
                        isTimelineUserEnabled = true;
                        ytFloatingTimelineBar.animate()
                                .alpha(0f)
                                .translationX(dpToPx(35))
                                .setDuration(200)
                                .withEndAction(() -> {
                                    ytFloatingTimelineBar.setVisibility(View.GONE);
                                    ytFloatingTimelineBar.setAlpha(1f);
                                    ytFloatingTimelineBar.setTranslationX(0f);
                                })
                                .start();
                    }
                });
            }

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

            if (ytRemoteVolumeBtn != null) {
                ytRemoteVolumeBtn.setOnClickListener(this::showFloatingVolumePopup);
                updateVolumeButtonDisplay();
            }

            ytRemoteSpeedBtn.setOnClickListener(this::showYouTubeSpeedPopup);
            ytRemoteQualityBtn.setOnClickListener(this::showYouTubeQualityPopup);

            ytRemoteShrinkBtn.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                ytFloatingRemoteScroll.setVisibility(View.GONE);
                ytFloatingRemoteBall.setVisibility(View.VISIBLE);
                syncTimelineBarWidth();
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
                                if (isTimelineUserEnabled && ytFloatingTimelineBar != null) {
                                    syncTimelineBarWidth();
                                    ytFloatingTimelineBar.setVisibility(View.VISIBLE);
                                    ytFloatingTimelineBar.setAlpha(0f);
                                    ytFloatingTimelineBar.setTranslationX(dpToPx(35));
                                    ytFloatingTimelineBar.animate()
                                            .alpha(1f)
                                            .translationX(0f)
                                            .setDuration(220)
                                            .start();
                                    if (ytRemoteTimeline != null) {
                                        ytRemoteTimeline.setColorFilter(0xFF00E5FF);
                                    }
                                } else {
                                    syncTimelineBarWidth();
                                }
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
            try {
                SharedPreferences p = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                float sc = Float.parseFloat(p.getString("yt_pod_scale", "1.0"));
                ytFloatingRemoteContainer.setScaleX(sc);
                ytFloatingRemoteContainer.setScaleY(sc);
            } catch (Exception ignored) {}
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
                TabItem currentTab = getActiveOrDominantTab();
                if (currentTab != null && currentTab.webView != null && currentTab.webView.canGoBack()) {
                    currentTab.webView.goBack();
                    v.postDelayed(this::updateOmniboxState, 150);
                }
            });

            navForwardBtn.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                TabItem currentTab = getActiveOrDominantTab();
                if (currentTab != null && currentTab.webView != null && currentTab.webView.canGoForward()) {
                    currentTab.webView.goForward();
                    v.postDelayed(this::updateOmniboxState, 150);
                }
            });

            searchDockUrl.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                omniboxEditText.requestFocus();
                if (omniboxEditText.getText() != null) {
                    int len = omniboxEditText.getText().length();
                    android.text.Selection.setSelection(omniboxEditText.getText(), len, 0);
                }
                omniboxEditText.scrollTo(0, 0);
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
                CaspianPhysics.collapseDockWithSpring(searchNavContainer, searchNavBall, searchDockScroll);
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
                                searchNavContainer.setX(event.getRawX() + ballDX);
                                searchNavContainer.setY(event.getRawY() + ballDY);
                            }
                            return true;

                        case MotionEvent.ACTION_UP:
                            if (!isBallDragging) {
                                playUiFeedbackSound("tap");
                                CaspianPhysics.expandDockWithSpring(searchNavContainer, searchNavBall, searchDockScroll);
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

        if (omniboxShieldBtn != null) {
            omniboxShieldBtn.setOnClickListener(this::showWaveguardFlyout);
        }
        if (omniboxShieldIcon != null) {
            omniboxShieldIcon.setOnClickListener(this::showWaveguardFlyout);
        }

        omniboxEditText.setOnTouchListener(new View.OnTouchListener() {
            private long lastTapTime = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    long now = System.currentTimeMillis();
                    if (!omniboxEditText.hasFocus()) {
                        omniboxEditText.requestFocus();
                        omniboxEditText.post(() -> {
                            if (omniboxEditText.getText() != null) {
                                int len = omniboxEditText.getText().length();
                                android.text.Selection.setSelection(omniboxEditText.getText(), len, 0);
                            }
                            omniboxEditText.scrollTo(0, 0);
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            if (imm != null) imm.showSoftInput(omniboxEditText, InputMethodManager.SHOW_IMPLICIT);
                        });
                        omniboxEditText.postDelayed(() -> {
                            if (omniboxEditText != null) {
                                omniboxEditText.scrollTo(0, 0);
                            }
                        }, 240);
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

        if (omniboxClearBtn != null) {
            omniboxClearBtn.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                omniboxEditText.setText("");
                omniboxEditText.clearFocus();
                hideKeyboard();
            });
        }

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

    private int dpToPx(float dp) {
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
        if (omniboxEditText == null) return;

        omniboxEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (omniboxHeader != null) {
                android.transition.TransitionSet transition = new android.transition.TransitionSet();
                android.transition.ChangeBounds changeBounds = new android.transition.ChangeBounds();
                changeBounds.setDuration(220);
                changeBounds.setInterpolator(new DecelerateInterpolator(1.8f));
                changeBounds.addListener(new android.transition.TransitionListenerAdapter() {
                    @Override
                    public void onTransitionEnd(android.transition.Transition transition) {
                        if (omniboxEditText != null) {
                            omniboxEditText.scrollTo(0, 0);
                        }
                    }
                });
                android.transition.Fade fade = new android.transition.Fade();
                fade.setDuration(160);
                transition.addTransition(changeBounds);
                transition.addTransition(fade);
                android.transition.TransitionManager.beginDelayedTransition(omniboxHeader, transition);
            }
            if (hasFocus) {
                // 1. Expand Omnibox URL section across toolbar by hiding other icon buttons
                if (omniboxBackBtn != null) omniboxBackBtn.setVisibility(View.GONE);
                if (omniboxForwardBtn != null) omniboxForwardBtn.setVisibility(View.GONE);
                if (omniboxReloadBtn != null) omniboxReloadBtn.setVisibility(View.GONE);
                if (omniboxDividerLeft != null) omniboxDividerLeft.setVisibility(View.GONE);
                if (omniboxDividerRight != null) omniboxDividerRight.setVisibility(View.GONE);
                if (omniboxToolbarsBtn != null) omniboxToolbarsBtn.setVisibility(View.GONE);
                if (omniboxSplitBtn != null) omniboxSplitBtn.setVisibility(View.GONE);
                if (omniboxTabsBtn != null) omniboxTabsBtn.setVisibility(View.GONE);
                if (omniboxMenuBtn != null) omniboxMenuBtn.setVisibility(View.GONE);

                // 2. Show clear button and paste button if clipboard has text
                if (omniboxClearBtn != null) omniboxClearBtn.setVisibility(View.VISIBLE);
                updateOmniboxPasteButton();

                // 3. Suppress floating suggestions dropdown
                if (omniboxSuggestionsContainer != null) omniboxSuggestionsContainer.setVisibility(View.GONE);

                omniboxEditText.post(() -> {
                    if (omniboxEditText.getText() != null) {
                        int len = omniboxEditText.getText().length();
                        android.text.Selection.setSelection(omniboxEditText.getText(), len, 0);
                    }
                    omniboxEditText.scrollTo(0, 0);
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) imm.showSoftInput(omniboxEditText, InputMethodManager.SHOW_IMPLICIT);
                });
                omniboxEditText.postDelayed(() -> {
                    if (omniboxEditText != null) {
                        omniboxEditText.scrollTo(0, 0);
                    }
                }, 240);
            } else {
                // Restore toolbar icon buttons when focus is lost
                if (omniboxBackBtn != null) omniboxBackBtn.setVisibility(View.VISIBLE);
                if (omniboxForwardBtn != null) omniboxForwardBtn.setVisibility(View.VISIBLE);
                if (omniboxReloadBtn != null) omniboxReloadBtn.setVisibility(View.VISIBLE);
                if (omniboxDividerLeft != null) omniboxDividerLeft.setVisibility(View.VISIBLE);
                if (omniboxDividerRight != null) omniboxDividerRight.setVisibility(View.VISIBLE);
                if (omniboxToolbarsBtn != null) omniboxToolbarsBtn.setVisibility(View.VISIBLE);
                if (omniboxSplitBtn != null) omniboxSplitBtn.setVisibility(View.VISIBLE);
                if (omniboxTabsBtn != null) omniboxTabsBtn.setVisibility(View.VISIBLE);
                if (omniboxMenuBtn != null) omniboxMenuBtn.setVisibility(View.VISIBLE);

                if (omniboxPasteBtn != null) omniboxPasteBtn.setVisibility(View.GONE);
                if (omniboxClearBtn != null) omniboxClearBtn.setVisibility(View.GONE);
                if (omniboxSuggestionsContainer != null) omniboxSuggestionsContainer.setVisibility(View.GONE);

                updateOmniboxState();
            }
        });

        omniboxEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (omniboxEditText.hasFocus()) {
                    updateOmniboxPasteButton();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void updateOmniboxPasteButton() {
        if (omniboxPasteBtn == null) return;
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        if (clipboard != null && clipboard.hasPrimaryClip() && clipboard.getPrimaryClip() != null && clipboard.getPrimaryClip().getItemCount() > 0) {
            CharSequence clipText = clipboard.getPrimaryClip().getItemAt(0).getText();
            if (clipText != null && !clipText.toString().trim().isEmpty()) {
                String link = clipText.toString().trim();
                omniboxPasteBtn.setVisibility(View.VISIBLE);
                omniboxPasteBtn.setTextColor(isDarkTheme ? 0xFF00E5FF : 0xFF0284C7);
                omniboxPasteBtn.setOnClickListener(v -> {
                    playUiFeedbackSound("tap");
                    omniboxEditText.setText(link);
                    omniboxEditText.requestFocus();
                    omniboxEditText.setSelection(omniboxEditText.getText().length());
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) imm.showSoftInput(omniboxEditText, InputMethodManager.SHOW_IMPLICIT);
                });
                return;
            }
        }
        omniboxPasteBtn.setVisibility(View.GONE);
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
        if (anchor == null || items == null || items.isEmpty()) return;

        View popupView = getLayoutInflater().inflate(R.layout.popup_caspian_menu, null);
        LinearLayout itemsContainer = popupView.findViewById(R.id.menu_items_container);

        int targetWidth = dpToPx(230);
        PopupWindow popupWindow = new PopupWindow(
                popupView,
                targetWidth,
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

        // Measure popup to calculate actual height
        popupView.measure(
                View.MeasureSpec.makeMeasureSpec(targetWidth, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );
        int measuredHeight = popupView.getMeasuredHeight();

        int[] anchorLoc = new int[2];
        anchor.getLocationOnScreen(anchorLoc);
        int anchorX = anchorLoc[0];
        int anchorY = anchorLoc[1];
        int anchorH = anchor.getHeight();

        DisplayMetrics dm = getResources().getDisplayMetrics();
        int screenH = dm.heightPixels;
        int screenW = dm.widthPixels;

        int spaceBelow = screenH - (anchorY + anchorH);
        int spaceAbove = anchorY;

        boolean showAbove = false;
        int maxAvailableH;

        if (spaceBelow >= measuredHeight + dpToPx(8)) {
            // Comfortably fits below
            showAbove = false;
            maxAvailableH = spaceBelow - dpToPx(16);
        } else if (spaceAbove > spaceBelow) {
            // Insufficient space below, more space above!
            showAbove = true;
            maxAvailableH = spaceAbove - dpToPx(16);
        } else {
            showAbove = false;
            maxAvailableH = spaceBelow - dpToPx(16);
        }

        int finalHeight = measuredHeight;
        if (measuredHeight > maxAvailableH && maxAvailableH > dpToPx(100)) {
            finalHeight = maxAvailableH;
            popupWindow.setHeight(finalHeight);
        }

        // Horizontal positioning: align with anchor but prevent overflow off screen sides
        int posX = anchorX;
        if (posX + targetWidth > screenW - dpToPx(8)) {
            posX = screenW - targetWidth - dpToPx(8);
        }
        if (posX < dpToPx(8)) {
            posX = dpToPx(8);
        }

        // Vertical positioning: place above or below anchor
        int posY;
        if (showAbove) {
            posY = anchorY - finalHeight - dpToPx(6);
        } else {
            posY = anchorY + anchorH + dpToPx(6);
        }

        // Safety clamp within screen bounds
        if (posY < dpToPx(8)) posY = dpToPx(8);
        if (posY + finalHeight > screenH - dpToPx(8)) {
            posY = screenH - finalHeight - dpToPx(8);
        }

        popupWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, posX, posY);
    }

    private void showQuickToolbarsPopup(View anchor) {
        com.google.android.material.bottomsheet.BottomSheetDialog dialog =
                new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_quick_toolbars, null);
        dialog.setContentView(dialogView);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setWindowAnimations(0); // Disable generic window animation in favor of S-shaped animation
            View bs = dialog.getWindow().findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bs != null) bs.setBackgroundResource(android.R.color.transparent);
        }

        final boolean[] isClosing = new boolean[]{false};
        final Runnable performSClosing = () -> {
            if (isClosing[0]) return;
            isClosing[0] = true;
            playUiFeedbackSound("tap");

            android.animation.ValueAnimator closeAnim = android.animation.ValueAnimator.ofFloat(0f, 1f);
            closeAnim.setDuration(280);
            closeAnim.setInterpolator(new android.view.animation.PathInterpolator(0.38f, 0.0f, 0.22f, 1.0f));
            closeAnim.addUpdateListener(anim -> {
                float f = anim.getAnimatedFraction(); // 0 -> 1
                // S-wave exit trajectory
                float sX = -(float) Math.sin(f * Math.PI) * dpToPx(26);
                float rot = -2.2f * (float) Math.sin(f * Math.PI);
                dialogView.setTranslationX(sX);
                dialogView.setTranslationY(dpToPx(380) * f);
                dialogView.setRotation(rot);
                dialogView.setScaleX(1.0f - (0.08f * f));
                dialogView.setScaleY(1.0f - (0.08f * f));
                dialogView.setAlpha(1f - f);
            });
            closeAnim.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    try {
                        dialog.dismiss();
                    } catch (Exception ignored) {}
                }
            });
            closeAnim.start();
        };

        // Close button
        View btnClose = dialogView.findViewById(R.id.btn_close_quick_toolbars);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> performSClosing.run());
        }

        // Drag handle tap dismiss
        View dragHandle = dialogView.findViewById(R.id.quick_toolbars_drag_handle);
        if (dragHandle != null) {
            dragHandle.setOnClickListener(v -> performSClosing.run());
        }

        // Back key intercept
        dialog.setOnKeyListener((dialogInterface, keyCode, event) -> {
            if (keyCode == android.view.KeyEvent.KEYCODE_BACK && event.getAction() == android.view.KeyEvent.ACTION_UP) {
                if (!isClosing[0]) {
                    performSClosing.run();
                    return true;
                }
            }
            return false;
        });

        // Outside backdrop tap intercept
        if (dialog.getWindow() != null) {
            View touchOutside = dialog.getWindow().findViewById(com.google.android.material.R.id.touch_outside);
            if (touchOutside != null) {
                touchOutside.setOnClickListener(v -> performSClosing.run());
            }
        }

        // 1. ChatGPT Dock
        View rowChatgpt = dialogView.findViewById(R.id.row_dock_chatgpt);
        androidx.appcompat.widget.SwitchCompat switchChatgpt = dialogView.findViewById(R.id.switch_dock_chatgpt);
        if (switchChatgpt != null) {
            switchChatgpt.setChecked(!isChatgptDockExplicitlyHidden);
            switchChatgpt.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                boolean targetShow = switchChatgpt.isChecked();
                toggleChatGPTDock(targetShow);
            });
        }
        if (rowChatgpt != null) {
            rowChatgpt.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                boolean newTarget = isChatgptDockExplicitlyHidden;
                toggleChatGPTDock(newTarget);
                if (switchChatgpt != null) switchChatgpt.setChecked(newTarget);
            });
        }

        // 2. Gemini Dock
        View rowGemini = dialogView.findViewById(R.id.row_dock_gemini);
        androidx.appcompat.widget.SwitchCompat switchGemini = dialogView.findViewById(R.id.switch_dock_gemini);
        if (switchGemini != null) {
            switchGemini.setChecked(!isGeminiDockExplicitlyHidden);
            switchGemini.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                boolean targetShow = switchGemini.isChecked();
                toggleGeminiDock(targetShow);
            });
        }
        if (rowGemini != null) {
            rowGemini.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                boolean newTarget = isGeminiDockExplicitlyHidden;
                toggleGeminiDock(newTarget);
                if (switchGemini != null) switchGemini.setChecked(newTarget);
            });
        }

        // 3. YouTube Remote
        View rowYouTube = dialogView.findViewById(R.id.row_dock_youtube);
        androidx.appcompat.widget.SwitchCompat switchYouTube = dialogView.findViewById(R.id.switch_dock_youtube);
        if (switchYouTube != null) {
            switchYouTube.setChecked(!isYtRemoteExplicitlyHidden);
            switchYouTube.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                boolean targetShow = switchYouTube.isChecked();
                toggleFloatingYouTubeRemote(targetShow);
            });
        }
        if (rowYouTube != null) {
            rowYouTube.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                boolean newTarget = isYtRemoteExplicitlyHidden;
                toggleFloatingYouTubeRemote(newTarget);
                if (switchYouTube != null) switchYouTube.setChecked(newTarget);
            });
        }

        // 4. Google Dock
        View rowGoogle = dialogView.findViewById(R.id.row_dock_google);
        androidx.appcompat.widget.SwitchCompat switchGoogle = dialogView.findViewById(R.id.switch_dock_google);
        if (switchGoogle != null) {
            switchGoogle.setChecked(!isSearchNavExplicitlyHidden);
            switchGoogle.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                boolean targetShow = switchGoogle.isChecked();
                toggleGoogleSearchDock(targetShow);
            });
        }
        if (rowGoogle != null) {
            rowGoogle.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                boolean newTarget = isSearchNavExplicitlyHidden;
                toggleGoogleSearchDock(newTarget);
                if (switchGoogle != null) switchGoogle.setChecked(newTarget);
            });
        }

        // 5. Waveguard Shield
        View rowWaveguard = dialogView.findViewById(R.id.row_dock_waveguard);
        androidx.appcompat.widget.SwitchCompat switchWaveguard = dialogView.findViewById(R.id.switch_dock_waveguard);
        if (switchWaveguard != null) {
            switchWaveguard.setChecked(waveguardShield != null && waveguardShield.isGlobalEnabled());
            switchWaveguard.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                if (waveguardShield != null) {
                    boolean target = switchWaveguard.isChecked();
                    waveguardShield.setGlobalEnabled(target);
                    updateOmniboxState();
                }
            });
        }
        if (rowWaveguard != null) {
            rowWaveguard.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                if (waveguardShield != null) {
                    boolean target = !waveguardShield.isGlobalEnabled();
                    waveguardShield.setGlobalEnabled(target);
                    updateOmniboxState();
                    if (switchWaveguard != null) switchWaveguard.setChecked(target);
                }
            });
        }

        applyQuickToolbarsTheme(dialogView, isDarkTheme);

        // Fluid S-shaped entrance animation on show
        dialog.setOnShowListener(d -> {
            dialogView.setAlpha(0f);
            dialogView.setTranslationY(dpToPx(380));
            dialogView.setScaleX(0.90f);
            dialogView.setScaleY(0.90f);

            android.animation.ValueAnimator sAnim = android.animation.ValueAnimator.ofFloat(0f, 1f);
            sAnim.setDuration(400);
            sAnim.setInterpolator(new android.view.animation.PathInterpolator(0.34f, 0.05f, 0.18f, 1.0f));
            sAnim.addUpdateListener(anim -> {
                float f = anim.getAnimatedFraction(); // 0 -> 1
                // Dynamic S-curve lateral wave: swings out and settles back smoothly
                float sX = (float) Math.sin(f * Math.PI) * (1f - f) * dpToPx(30);
                float rot = 2.4f * (1f - f) * (float) Math.cos(f * Math.PI * 0.7f);
                dialogView.setTranslationX(sX);
                dialogView.setTranslationY(dpToPx(380) * (1f - f));
                dialogView.setRotation(rot);
                dialogView.setScaleX(0.90f + (0.10f * f));
                dialogView.setScaleY(0.90f + (0.10f * f));
                dialogView.setAlpha(Math.min(1f, f * 2.2f));
            });
            sAnim.start();

            // Cascading entrance for dock rows
            int[] rows = {R.id.row_dock_chatgpt, R.id.row_dock_gemini, R.id.row_dock_youtube, R.id.row_dock_google, R.id.row_dock_waveguard};
            for (int i = 0; i < rows.length; i++) {
                View row = dialogView.findViewById(rows[i]);
                if (row != null) {
                    row.setAlpha(0f);
                    row.setTranslationX(dpToPx(24));
                    row.animate()
                            .alpha(1f)
                            .translationX(0f)
                            .setStartDelay(100 + (i * 35))
                            .setDuration(260)
                            .setInterpolator(new android.view.animation.DecelerateInterpolator(1.4f))
                            .start();
                }
            }
        });

        dialog.show();
    }

    private void applyQuickToolbarsTheme(View root, boolean isDark) {
        if (root == null) return;
        try {
            if (!isDark) {
                GradientDrawable rootGd = new GradientDrawable();
                rootGd.setColor(0xFFFFFFFF);
                rootGd.setCornerRadii(new float[]{dpToPx(22), dpToPx(22), dpToPx(22), dpToPx(22), 0, 0, 0, 0});
                root.setBackground(rootGd);

                View handle = root.findViewById(R.id.quick_toolbars_drag_handle);
                if (handle != null) {
                    GradientDrawable handleGd = new GradientDrawable();
                    handleGd.setColor(0xFFCBD5E1);
                    handleGd.setCornerRadius(dpToPx(3));
                    handle.setBackground(handleGd);
                }

                int slateDark = 0xFF0F172A;
                int subText = 0xFF64748B;
                int rowBg = 0xFFF1F5F9;

                TextView headerTitle = root.findViewById(R.id.text_quick_toolbars_title);
                if (headerTitle != null) headerTitle.setTextColor(0xFF475569);

                ImageButton btnClose = root.findViewById(R.id.btn_close_quick_toolbars);
                if (btnClose != null) btnClose.setColorFilter(0xFF64748B);

                int[] rowIds = {R.id.row_dock_chatgpt, R.id.row_dock_gemini, R.id.row_dock_youtube, R.id.row_dock_google, R.id.row_dock_waveguard};
                int[] titleIds = {R.id.title_dock_chatgpt, R.id.title_dock_gemini, R.id.title_dock_youtube, R.id.title_dock_google, R.id.title_dock_waveguard};
                int[] subIds = {R.id.sub_dock_chatgpt, R.id.sub_dock_gemini, R.id.sub_dock_youtube, R.id.sub_dock_google, R.id.sub_dock_waveguard};
                int[] squircleIds = {R.id.squircle_dock_chatgpt, R.id.squircle_dock_gemini, R.id.squircle_dock_youtube, R.id.squircle_dock_google, R.id.squircle_dock_waveguard};

                for (int i = 0; i < rowIds.length; i++) {
                    View row = root.findViewById(rowIds[i]);
                    if (row != null) {
                        GradientDrawable rowGd = new GradientDrawable();
                        rowGd.setColor(rowBg);
                        rowGd.setCornerRadius(dpToPx(14));
                        rowGd.setStroke(dpToPx(1), 0xFFE2E8F0);
                        row.setBackground(rowGd);
                    }
                    TextView title = root.findViewById(titleIds[i]);
                    if (title != null) title.setTextColor(slateDark);
                    TextView sub = root.findViewById(subIds[i]);
                    if (sub != null) sub.setTextColor(subText);
                    View squircle = root.findViewById(squircleIds[i]);
                    if (squircle != null) {
                        GradientDrawable sqGd = new GradientDrawable();
                        sqGd.setColor(0xFFE2E8F0);
                        sqGd.setCornerRadius(dpToPx(10));
                        squircle.setBackground(sqGd);
                    }
                }

                int[] switchIds = {R.id.switch_dock_chatgpt, R.id.switch_dock_gemini, R.id.switch_dock_youtube, R.id.switch_dock_google, R.id.switch_dock_waveguard};
                for (int sId : switchIds) {
                    androidx.appcompat.widget.SwitchCompat sw = root.findViewById(sId);
                    if (sw != null) {
                        sw.setTrackResource(R.drawable.bg_switch_modern_track_light);
                        sw.setThumbResource(R.drawable.bg_switch_modern_thumb_light);
                    }
                }
            } else {
                GradientDrawable rootGd = new GradientDrawable();
                rootGd.setColor(0xFF0C131D);
                rootGd.setCornerRadii(new float[]{dpToPx(24), dpToPx(24), dpToPx(24), dpToPx(24), 0, 0, 0, 0});
                rootGd.setStroke(dpToPx(1), 0xFF192535);
                root.setBackground(rootGd);

                int[] rowIds = {R.id.row_dock_chatgpt, R.id.row_dock_gemini, R.id.row_dock_youtube, R.id.row_dock_google, R.id.row_dock_waveguard};
                for (int rowId : rowIds) {
                    View row = root.findViewById(rowId);
                    if (row != null) {
                        GradientDrawable rowGd = new GradientDrawable();
                        rowGd.setColor(0xFF141D2A);
                        rowGd.setCornerRadius(dpToPx(14));
                        rowGd.setStroke(dpToPx(1), 0xFF1E2B3D);
                        row.setBackground(rowGd);
                    }
                }
            }
        } catch (Throwable ignored) {}
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

    private void showWaveguardFlyout(View anchor) {
        if (waveguardShield == null) return;
        try {
            LayoutInflater inflater = LayoutInflater.from(this);
            View popupView = inflater.inflate(R.layout.popup_waveguard_shield, null);

            int displayWidth = getResources().getDisplayMetrics().widthPixels;
            int targetWidth = Math.min((int)(getResources().getDisplayMetrics().density * 340), displayWidth - dpToPx(32));

            final PopupWindow popupWindow = new PopupWindow(
                    popupView,
                    targetWidth,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    true
            );
            popupWindow.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
            popupWindow.setElevation(28f);
            popupWindow.setOutsideTouchable(false);

            final boolean[] isWgClosing = {false};
            Runnable performWaveguardExit = () -> {
                if (isWgClosing[0]) return;
                isWgClosing[0] = true;
                boolean isBottom = "bottom".equalsIgnoreCase(omniboxPosition);
                float startY = isBottom ? dpToPx(85) : -dpToPx(85);

                android.animation.ValueAnimator exitAnim = android.animation.ValueAnimator.ofFloat(0f, 1f);
                exitAnim.setDuration(240);
                exitAnim.setInterpolator(new android.view.animation.PathInterpolator(0.38f, 0.0f, 0.20f, 1.0f));
                exitAnim.addUpdateListener(anim -> {
                    float f = anim.getAnimatedFraction();
                    float sX = (float) -Math.sin(f * Math.PI) * (1f - f) * dpToPx(isBottom ? 22 : -22);
                    float rot = (isBottom ? -1.8f : 1.8f) * f;
                    popupView.setTranslationX(sX);
                    popupView.setTranslationY(startY * f);
                    popupView.setRotation(rot);
                    popupView.setScaleX(1.0f - (0.08f * f));
                    popupView.setScaleY(1.0f - (0.08f * f));
                    popupView.setAlpha(1.0f - f);
                });
                exitAnim.addListener(new android.animation.AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(android.animation.Animator animation) {
                        try {
                            popupWindow.dismiss();
                        } catch (Exception ignored) {}
                    }
                });
                exitAnim.start();
            };

            popupWindow.setTouchInterceptor((v, event) -> {
                if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                    android.graphics.Rect rect = new android.graphics.Rect();
                    popupView.getGlobalVisibleRect(rect);
                    if (!rect.contains((int) event.getRawX(), (int) event.getRawY())) {
                        performWaveguardExit.run();
                        return true;
                    }
                }
                return false;
            });

            popupView.setFocusableInTouchMode(true);
            popupView.requestFocus();
            popupView.setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == android.view.KeyEvent.KEYCODE_BACK && event.getAction() == android.view.KeyEvent.ACTION_UP) {
                    if (!isWgClosing[0]) {
                        performWaveguardExit.run();
                        return true;
                    }
                }
                return false;
            });

            View btnClose = popupView.findViewById(R.id.btn_close_waveguard_popup);
            if (btnClose != null) {
                btnClose.setOnClickListener(v -> performWaveguardExit.run());
            }

            TabItem currentTab = getActiveOrDominantTab();
            String currentUrl = currentTab != null ? currentTab.url : null;
            String host = "";
            if (currentUrl != null && !currentUrl.isEmpty()) {
                try {
                    Uri u = Uri.parse(currentUrl);
                    host = u.getHost();
                } catch (Exception ignored) {}
            }
            if (host == null || host.isEmpty()) host = "Active Tab";

            TextView domainText = popupView.findViewById(R.id.current_site_domain);
            if (domainText != null) domainText.setText(host);

            final String cleanHost = host;
            boolean isWhitelisted = waveguardShield.isSiteWhitelisted(cleanHost);
            boolean isGlobalOn = waveguardShield.isGlobalEnabled();
            boolean siteActive = isGlobalOn && !isWhitelisted;

            androidx.appcompat.widget.SwitchCompat siteShieldSwitch = popupView.findViewById(R.id.site_shield_switch);
            View statusPill = popupView.findViewById(R.id.shield_status_badge_container);
            View statusDot = popupView.findViewById(R.id.shield_status_dot);
            TextView statusBadge = popupView.findViewById(R.id.shield_status_badge);
            TextView blockedBadge = popupView.findViewById(R.id.blocked_count_badge);
            TextView blockedTotalText = popupView.findViewById(R.id.blocked_total_text);
            TextView rulesVersionText = popupView.findViewById(R.id.rules_version_text);

            int tabBlocks = currentTab != null ? waveguardShield.getBlockedCountForTab(currentTab.id) : 0;
            int totalBlocks = waveguardShield.getTotalBlockedCount();
            if (blockedBadge != null) blockedBadge.setText(String.valueOf(tabBlocks));
            if (blockedTotalText != null) blockedTotalText.setText(totalBlocks + " blocked all-time across tabs");
            if (rulesVersionText != null) rulesVersionText.setText("Waveguard Active (" + waveguardShield.getRuleCount() + " filters)");

            Runnable updateStatusPill = () -> {
                boolean active = waveguardShield.isGlobalEnabled() && !waveguardShield.isSiteWhitelisted(cleanHost);
                if (statusBadge != null) {
                    statusBadge.setText(active ? "PROTECTED" : "PAUSED");
                    if (!isDarkTheme) {
                        statusBadge.setTextColor(active ? 0xFF0284C7 : 0xFFDC2626);
                    } else {
                        statusBadge.setTextColor(active ? 0xFF00E5FF : 0xFFEF4444);
                    }
                }
                if (statusDot != null) {
                    statusDot.setBackgroundResource(active ? R.drawable.bg_circle_cyan : R.drawable.bg_circle_red);
                }
                if (statusPill != null) {
                    GradientDrawable gd = new GradientDrawable();
                    gd.setCornerRadius(dpToPx(14));
                    if (!isDarkTheme) {
                        gd.setColor(active ? 0xFFE0F2FE : 0xFFFEE2E2);
                        gd.setStroke(dpToPx(1), active ? 0xFFBAE6FD : 0xFFFECACA);
                    } else {
                        gd.setColor(active ? 0xFF082635 : 0xFF2A1215);
                        gd.setStroke(dpToPx(1), active ? 0xFF0E495C : 0xFFEF4444);
                    }
                    statusPill.setBackground(gd);
                }
            };
            updateStatusPill.run();

            if (siteShieldSwitch != null) {
                siteShieldSwitch.setChecked(siteActive);
                siteShieldSwitch.setOnCheckedChangeListener((btn, isChecked) -> {
                    if (cleanHost.contains(".")) {
                        waveguardShield.setSiteWhitelisted(cleanHost, !isChecked);
                    } else {
                        waveguardShield.setGlobalEnabled(isChecked);
                    }
                    updateOmniboxState();
                    syncWaveguardToControlWeb();
                    updateStatusPill.run();
                    if (currentTab != null && currentTab.webView != null) {
                        currentTab.webView.reload();
                    }
                });
            }

            View headerAdv = popupView.findViewById(R.id.header_advanced_settings);
            View containerAdv = popupView.findViewById(R.id.container_advanced_settings);
            ImageView iconAdvChevron = popupView.findViewById(R.id.icon_advanced_chevron);
            if (headerAdv != null && containerAdv != null) {
                headerAdv.setOnClickListener(v -> {
                    boolean isCurrentlyExpanded = containerAdv.getVisibility() == View.VISIBLE;
                    containerAdv.setVisibility(isCurrentlyExpanded ? View.GONE : View.VISIBLE);
                    if (iconAdvChevron != null) {
                        iconAdvChevron.animate()
                                .rotation(isCurrentlyExpanded ? 0f : 180f)
                                .setDuration(200)
                                .start();
                    }
                });
            }

            androidx.appcompat.widget.SwitchCompat swAdblock = popupView.findViewById(R.id.switch_adblock);
            androidx.appcompat.widget.SwitchCompat swCosmetic = popupView.findViewById(R.id.switch_cosmetic);
            androidx.appcompat.widget.SwitchCompat swDefuser = popupView.findViewById(R.id.switch_defuser);
            androidx.appcompat.widget.SwitchCompat swPopups = popupView.findViewById(R.id.switch_popups);
            androidx.appcompat.widget.SwitchCompat swFingerprint = popupView.findViewById(R.id.switch_fingerprint);

            if (swAdblock != null) {
                swAdblock.setChecked(waveguardShield.isGlobalEnabled());
                swAdblock.setOnCheckedChangeListener((b, val) -> {
                    waveguardShield.setGlobalEnabled(val);
                    updateOmniboxState();
                    syncWaveguardToControlWeb();
                    updateStatusPill.run();
                });
            }

            if (swCosmetic != null) {
                swCosmetic.setChecked(waveguardShield.isCosmeticEnabled());
                swCosmetic.setOnCheckedChangeListener((b, val) -> {
                    waveguardShield.setCosmeticEnabled(val);
                    syncWaveguardToControlWeb();
                });
            }

            if (swDefuser != null) {
                swDefuser.setChecked(waveguardShield.isDefuserEnabled());
                swDefuser.setOnCheckedChangeListener((b, val) -> {
                    waveguardShield.setDefuserEnabled(val);
                    syncWaveguardToControlWeb();
                });
            }

            if (swPopups != null) {
                swPopups.setChecked(waveguardShield.isEasyPrivacyEnabled());
                swPopups.setOnCheckedChangeListener((b, val) -> {
                    waveguardShield.setEasyPrivacyEnabled(val);
                    syncWaveguardToControlWeb();
                });
            }

            if (swFingerprint != null) {
                swFingerprint.setChecked(waveguardShield.isFingerprintEnabled());
                swFingerprint.setOnCheckedChangeListener((b, val) -> {
                    waveguardShield.setFingerprintEnabled(val);
                    syncWaveguardToControlWeb();
                });
            }

            TextView btnUpdateRules = popupView.findViewById(R.id.btn_update_rules);
            if (btnUpdateRules != null) {
                btnUpdateRules.setOnClickListener(v -> {
                    btnUpdateRules.setText("Updating...");
                    waveguardShield.checkForUpdates((success, newCount, message) -> {
                        runOnUiThread(() -> {
                            btnUpdateRules.setText("Updated");
                            if (rulesVersionText != null) rulesVersionText.setText("Waveguard Active (" + newCount + " filters)");
                            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                            syncWaveguardToControlWeb();
                        });
                    });
                });
            }

            applyWaveguardTheme(popupView, isDarkTheme);

            // Screen Positioning: always horizontally centered, upward when bottom omnibox
            boolean isBottom = "bottom".equalsIgnoreCase(omniboxPosition);
            View targetParent = rootContainer != null ? rootContainer : getWindow().getDecorView();
            int gravity = (isBottom ? Gravity.BOTTOM : Gravity.TOP) | Gravity.CENTER_HORIZONTAL;
            int yOffset = isBottom ? dpToPx(72) : dpToPx(64);
            popupWindow.showAtLocation(targetParent, gravity, 0, yOffset);

            // Smooth Apple-like S-shaped entrance animation
            float startY = isBottom ? dpToPx(85) : -dpToPx(85);
            popupView.setAlpha(0f);
            popupView.setTranslationY(startY);
            popupView.setScaleX(0.88f);
            popupView.setScaleY(0.88f);
            popupView.setRotation(isBottom ? 2.2f : -2.2f);

            android.animation.ValueAnimator openAnim = android.animation.ValueAnimator.ofFloat(0f, 1f);
            openAnim.setDuration(360);
            openAnim.setInterpolator(new android.view.animation.PathInterpolator(0.24f, 1.0f, 0.32f, 1.0f));
            openAnim.addUpdateListener(anim -> {
                float f = anim.getAnimatedFraction();
                float sX = (float) Math.sin(f * Math.PI) * (1f - f) * dpToPx(isBottom ? 24 : -24);
                float rot = (isBottom ? 2.2f : -2.2f) * (1f - f) * (float) Math.cos(f * Math.PI * 0.75f);
                popupView.setTranslationX(sX);
                popupView.setTranslationY(startY * (1f - f));
                popupView.setRotation(rot);
                popupView.setScaleX(0.88f + (0.12f * f));
                popupView.setScaleY(0.88f + (0.12f * f));
                popupView.setAlpha(Math.min(1f, f * 2.2f));
            });
            openAnim.start();

            // Cascading entrance for WaveGuard internal cards
            View[] wgItems = {
                    popupView.findViewById(R.id.squircle_waveguard_brand),
                    popupView.findViewById(R.id.pill_active_domain),
                    popupView.findViewById(R.id.site_shield_toggle_row),
                    popupView.findViewById(R.id.stats_panel),
                    popupView.findViewById(R.id.header_advanced_settings)
            };
            for (int i = 0; i < wgItems.length; i++) {
                View item = wgItems[i];
                if (item != null) {
                    item.setAlpha(0f);
                    item.setTranslationX(dpToPx(12));
                    item.animate()
                            .alpha(1f)
                            .translationX(0f)
                            .setStartDelay(60 + (i * 25))
                            .setDuration(240)
                            .setInterpolator(new android.view.animation.DecelerateInterpolator(1.4f))
                            .start();
                }
            }

            // Spring touch physics on interactive Waveguard components
            attachSpringPhysics(btnClose);
            attachSpringPhysics(popupView.findViewById(R.id.pill_active_domain));
            attachSpringPhysics(popupView.findViewById(R.id.site_shield_toggle_row));
            attachSpringPhysics(popupView.findViewById(R.id.stats_panel));
            attachSpringPhysics(popupView.findViewById(R.id.btn_update_rules));
            attachSpringPhysics(popupView.findViewById(R.id.header_advanced_settings));
        } catch (Exception e) {
            Log.e(TAG, "Failed to show Waveguard flyout: ", e);
            showShieldStatusDialog();
        }
    }

    private void applyWaveguardTheme(View root, boolean isDark) {
        if (root == null) return;
        try {
            if (!isDark) {
                // 1. Root CardView & Scroll Container
                if (root instanceof androidx.cardview.widget.CardView) {
                    ((androidx.cardview.widget.CardView) root).setCardBackgroundColor(0xFFFFFFFF);
                }
                View scrollContent = root.findViewById(R.id.waveguard_scroll_content);
                if (scrollContent != null) {
                    GradientDrawable bg = new GradientDrawable();
                    bg.setColor(0xFFFFFFFF);
                    bg.setCornerRadius(dpToPx(22));
                    bg.setStroke(dpToPx(1), 0xFFE2E8F0);
                    scrollContent.setBackground(bg);
                }

                // 2. Header & Brand
                View brandSq = root.findViewById(R.id.squircle_waveguard_brand);
                if (brandSq != null) {
                    GradientDrawable sq = new GradientDrawable();
                    sq.setColor(0xFFE0F7FA);
                    sq.setCornerRadius(dpToPx(13));
                    sq.setStroke(dpToPx(1), 0xFFBAE6FD);
                    brandSq.setBackground(sq);
                }
                ImageView shieldLogo = root.findViewById(R.id.shield_logo);
                if (shieldLogo != null) shieldLogo.setColorFilter(0xFF0284C7);

                TextView shieldTitle = root.findViewById(R.id.shield_title);
                if (shieldTitle != null) shieldTitle.setTextColor(0xFF0F172A);

                TextView shieldSubtitle = root.findViewById(R.id.shield_subtitle);
                if (shieldSubtitle != null) shieldSubtitle.setTextColor(0xFF64748B);

                View btnClose = root.findViewById(R.id.btn_close_waveguard_popup);
                if (btnClose != null) {
                    GradientDrawable closeBg = new GradientDrawable();
                    closeBg.setColor(0xFFF1F5F9);
                    closeBg.setCornerRadius(dpToPx(10));
                    btnClose.setBackground(closeBg);
                }
                ImageView iconClose = root.findViewById(R.id.icon_close_waveguard);
                if (iconClose != null) iconClose.setColorFilter(0xFF64748B);

                // 3. Active Tab Row
                TextView labelActiveTab = root.findViewById(R.id.label_active_tab);
                if (labelActiveTab != null) labelActiveTab.setTextColor(0xFF475569);

                View pillDomain = root.findViewById(R.id.pill_active_domain);
                if (pillDomain != null) {
                    GradientDrawable dPill = new GradientDrawable();
                    dPill.setColor(0xFFF1F5F9);
                    dPill.setCornerRadius(dpToPx(12));
                    dPill.setStroke(dpToPx(1), 0xFFE2E8F0);
                    pillDomain.setBackground(dPill);
                }
                ImageView iconLock = root.findViewById(R.id.icon_lock_domain);
                if (iconLock != null) iconLock.setColorFilter(0xFF0284C7);

                TextView domainText = root.findViewById(R.id.current_site_domain);
                if (domainText != null) domainText.setTextColor(0xFF0F172A);

                // 4. Master Toggle Card
                View masterCard = root.findViewById(R.id.site_shield_toggle_row);
                if (masterCard != null) {
                    GradientDrawable cBg = new GradientDrawable();
                    cBg.setColor(0xFFF8FAFC);
                    cBg.setCornerRadius(dpToPx(16));
                    cBg.setStroke(dpToPx(1), 0xFFE2E8F0);
                    masterCard.setBackground(cBg);
                }
                TextView siteLabel = root.findViewById(R.id.site_shield_label);
                if (siteLabel != null) siteLabel.setTextColor(0xFF0F172A);

                TextView siteSublabel = root.findViewById(R.id.site_shield_sublabel);
                if (siteSublabel != null) siteSublabel.setTextColor(0xFF64748B);

                // 5. Stats Panel
                View statsCard = root.findViewById(R.id.stats_panel);
                if (statsCard != null) {
                    GradientDrawable cBg = new GradientDrawable();
                    cBg.setColor(0xFFF8FAFC);
                    cBg.setCornerRadius(dpToPx(16));
                    cBg.setStroke(dpToPx(1), 0xFFE2E8F0);
                    statsCard.setBackground(cBg);
                }
                View countSq = root.findViewById(R.id.squircle_blocked_count);
                if (countSq != null) {
                    GradientDrawable cSq = new GradientDrawable();
                    cSq.setColor(0xFFE0F2FE);
                    cSq.setCornerRadius(dpToPx(12));
                    cSq.setStroke(dpToPx(1), 0xFFBAE6FD);
                    countSq.setBackground(cSq);
                }
                TextView countBadge = root.findViewById(R.id.blocked_count_badge);
                if (countBadge != null) countBadge.setTextColor(0xFF0284C7);

                TextView labelThreats = root.findViewById(R.id.label_threats_blocked);
                if (labelThreats != null) labelThreats.setTextColor(0xFF0F172A);

                TextView totalText = root.findViewById(R.id.blocked_total_text);
                if (totalText != null) totalText.setTextColor(0xFF64748B);

                // 6. Advanced Settings
                TextView advTitle = root.findViewById(R.id.text_advanced_settings_title);
                if (advTitle != null) advTitle.setTextColor(0xFF64748B);

                ImageView advChevron = root.findViewById(R.id.icon_advanced_chevron);
                if (advChevron != null) advChevron.setColorFilter(0xFF64748B);

                View advContainer = root.findViewById(R.id.container_advanced_settings);
                if (advContainer != null) {
                    GradientDrawable aBg = new GradientDrawable();
                    aBg.setColor(0xFFF8FAFC);
                    aBg.setCornerRadius(dpToPx(16));
                    aBg.setStroke(dpToPx(1), 0xFFE2E8F0);
                    advContainer.setBackground(aBg);
                }

                if (advContainer instanceof ViewGroup) {
                    ViewGroup vg = (ViewGroup) advContainer;
                    for (int i = 0; i < vg.getChildCount(); i++) {
                        View child = vg.getChildAt(i);
                        if (child instanceof RelativeLayout) {
                            ViewGroup rel = (ViewGroup) child;
                            for (int j = 0; j < rel.getChildCount(); j++) {
                                View rc = rel.getChildAt(j);
                                if (rc instanceof LinearLayout) {
                                    ViewGroup ll = (ViewGroup) rc;
                                    for (int k = 0; k < ll.getChildCount(); k++) {
                                        View lvc = ll.getChildAt(k);
                                        if (lvc instanceof TextView) {
                                            TextView tv = (TextView) lvc;
                                            if (k == 0) tv.setTextColor(0xFF0F172A);
                                            else tv.setTextColor(0xFF64748B);
                                        }
                                    }
                                }
                            }
                        } else if (child != null && child.getLayoutParams() != null && child.getLayoutParams().height == 1) {
                            child.setBackgroundColor(0xFFE2E8F0);
                        }
                    }
                }

                // 7. Footer
                TextView rulesText = root.findViewById(R.id.rules_version_text);
                if (rulesText != null) rulesText.setTextColor(0xFF475569);

                TextView btnUpdate = root.findViewById(R.id.btn_update_rules);
                if (btnUpdate != null) {
                    GradientDrawable uBg = new GradientDrawable();
                    uBg.setColor(0xFFF0F9FF);
                    uBg.setCornerRadius(dpToPx(10));
                    uBg.setStroke(dpToPx(1), 0xFFBAE6FD);
                    btnUpdate.setBackground(uBg);
                    btnUpdate.setTextColor(0xFF0284C7);
                }

                // 8. Update Switches with Light Theme Drawables
                int[] switchIds = {
                        R.id.site_shield_switch,
                        R.id.switch_adblock,
                        R.id.switch_cosmetic,
                        R.id.switch_defuser,
                        R.id.switch_popups,
                        R.id.switch_fingerprint
                };
                for (int swId : switchIds) {
                    androidx.appcompat.widget.SwitchCompat sw = root.findViewById(swId);
                    if (sw != null) {
                        sw.setTrackResource(R.drawable.bg_switch_modern_track_light);
                        sw.setThumbResource(R.drawable.bg_switch_modern_thumb_light);
                    }
                }
            }
        } catch (Throwable ignored) {}
    }

    private void showShieldStatusDialog() {
        if (waveguardShield == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🛡️ Waveguard Privacy Shield");
        String message = "Status: " + (waveguardShield.isGlobalEnabled() ? "ACTIVE (Shielding)" : "PAUSED") +
                "\n\nBlocked Ads & Trackers: " + waveguardShield.getTotalBlockedCount() +
                "\nActive Filter Rules: " + waveguardShield.getRuleCount() +
                "\nHardware GPU Acceleration: 60/120 FPS Active" +
                "\nUniversal DOM Pruning: Dynamic Sliding Window Active";
        builder.setMessage(message);
        builder.setPositiveButton("OK", null);
        builder.setNeutralButton("Toggle Shield", (dialog, which) -> {
            waveguardShield.setGlobalEnabled(!waveguardShield.isGlobalEnabled());
            updateOmniboxState();
        });
        builder.setNegativeButton("Close", null);
        builder.show();
    }

    private void showBrowserMenu(View anchor) {
        if ("grid".equalsIgnoreCase(omniboxMenuStyle)) {
            showBrowserActionGrid();
            return;
        }
        showBrowserMenuList(anchor);
    }

    public void showBrowserMenuList(View anchor) {
        TabItem currentTab = getActiveOrDominantTab();
        com.google.android.material.bottomsheet.BottomSheetDialog dialog =
                new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_browser_action_list, null);
        dialog.setContentView(dialogView);

        if (dialog.getWindow() != null) {
            View bs = dialog.getWindow().findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bs != null) bs.setBackgroundResource(android.R.color.transparent);
        }

        // Section 1: Quick Actions (New Tab, Dual AI, Theme, Split, Desktop)
        View btnNewTab = dialogView.findViewById(R.id.action_list_tile_new_tab);
        if (btnNewTab != null) {
            btnNewTab.setOnClickListener(v -> {
                dialog.dismiss();
                addNewTab("web", null);
            });
        }

        View btnDualAi = dialogView.findViewById(R.id.action_list_tile_dual_ai);
        if (btnDualAi != null) {
            btnDualAi.setOnClickListener(v -> {
                dialog.dismiss();
                launchDualAIAsk();
            });
        }

        View btnTheme = dialogView.findViewById(R.id.action_list_tile_theme);
        if (btnTheme != null) {
            btnTheme.setOnClickListener(v -> {
                dialog.dismiss();
                toggleHostTheme(!isDarkTheme);
            });
        }

        View btnSplit = dialogView.findViewById(R.id.action_list_tile_split);
        if (btnSplit != null) {
            btnSplit.setOnClickListener(v -> {
                dialog.dismiss();
                cycleSplitViewMode();
            });
        }

        View btnDesktop = dialogView.findViewById(R.id.action_list_tile_desktop);
        if (btnDesktop != null) {
            btnDesktop.setOnClickListener(v -> {
                dialog.dismiss();
                if (currentTab != null) toggleDesktopMode(currentTab.id);
            });
        }

        // Section 2: Interactive Controls (Zoom & PDF)
        TextView textZoomVal = dialogView.findViewById(R.id.text_stepper_zoom_val);
        if (textZoomVal != null) {
            textZoomVal.setText(currentTextZoom + "%");
        }

        TextView btnMinus = dialogView.findViewById(R.id.btn_stepper_minus);
        if (btnMinus != null) {
            btnMinus.setOnClickListener(v -> {
                int next = Math.max(25, currentTextZoom - 10);
                setPageZoom(next);
                if (textZoomVal != null) textZoomVal.setText(next + "%");
            });
        }

        TextView btnPlus = dialogView.findViewById(R.id.btn_stepper_plus);
        if (btnPlus != null) {
            btnPlus.setOnClickListener(v -> {
                int next = Math.min(300, currentTextZoom + 10);
                setPageZoom(next);
                if (textZoomVal != null) textZoomVal.setText(next + "%");
            });
        }

        View widgetPdf = dialogView.findViewById(R.id.widget_open_pdf);
        if (widgetPdf != null) {
            widgetPdf.setOnClickListener(v -> {
                dialog.dismiss();
                openPdfPicker();
            });
        }

        // Section 3: Precision Action Rows
        View rowFind = dialogView.findViewById(R.id.action_list_row_find);
        if (rowFind != null) {
            rowFind.setOnClickListener(v -> {
                dialog.dismiss();
                showOmniboxFinder();
            });
        }

        View rowHistory = dialogView.findViewById(R.id.action_list_row_history);
        if (rowHistory != null) {
            rowHistory.setOnClickListener(v -> {
                dialog.dismiss();
                showHistoryDialog();
            });
        }

        View rowDownloads = dialogView.findViewById(R.id.action_list_row_downloads);
        if (rowDownloads != null) {
            rowDownloads.setOnClickListener(v -> {
                dialog.dismiss();
                openDownloadsManagerModal();
            });
        }

        // Export Chips
        TextView chipPdf = dialogView.findViewById(R.id.chip_export_pdf);
        if (chipPdf != null) {
            chipPdf.setOnClickListener(v -> {
                dialog.dismiss();
                exportCurrentDocument("pdf");
            });
        }
        TextView chipTxt = dialogView.findViewById(R.id.chip_export_txt);
        if (chipTxt != null) {
            chipTxt.setOnClickListener(v -> {
                dialog.dismiss();
                exportCurrentDocument("txt");
            });
        }
        TextView chipDoc = dialogView.findViewById(R.id.chip_export_doc);
        if (chipDoc != null) {
            chipDoc.setOnClickListener(v -> {
                dialog.dismiss();
                exportCurrentDocument("doc");
            });
        }
        TextView chipMd = dialogView.findViewById(R.id.chip_export_md);
        if (chipMd != null) {
            chipMd.setOnClickListener(v -> {
                dialog.dismiss();
                exportCurrentDocument("md");
            });
        }

        View rowSettings = dialogView.findViewById(R.id.action_list_row_settings);
        if (rowSettings != null) {
            rowSettings.setOnClickListener(v -> {
                dialog.dismiss();
                openControlSheet();
            });
        }

        // Section 4: Waveguard Card Settings Button
        View btnWaveguardSettings = dialogView.findViewById(R.id.btn_waveguard_pro_settings);
        if (btnWaveguardSettings != null) {
            btnWaveguardSettings.setOnClickListener(v -> {
                dialog.dismiss();
                showWaveguardFlyout(anchor != null ? anchor : dialogView);
            });
        }

        applyListTheme(dialogView, isDarkTheme);
        dialog.show();
    }

    private void applyListTheme(View root, boolean isDark) {
        if (root == null) return;
        try {
            if (!isDark) {
                // Pristine Light Theme for Action List (matching stitch_designs/stitch_light_list)
                GradientDrawable rootGd = new GradientDrawable();
                rootGd.setColor(0xFFFFFFFF);
                rootGd.setCornerRadii(new float[]{dpToPx(24), dpToPx(24), dpToPx(24), dpToPx(24), 0, 0, 0, 0});
                root.setBackground(rootGd);

                View handle = root.findViewById(R.id.action_list_drag_handle);
                if (handle != null) {
                    GradientDrawable handleGd = new GradientDrawable();
                    handleGd.setColor(0xFFCBD5E1);
                    handleGd.setCornerRadius(dpToPx(3));
                    handle.setBackground(handleGd);
                }

                int slateText = 0xFF1E293B;
                int subText = 0xFF334155;
                int borderLight = 0xFFE2E8F0;
                int bgSquircleLight = 0xFFF1F5F9;

                // Section 1: Quick Action Cards
                int[] squircles = {
                        R.id.squircle_list_new_tab,
                        R.id.squircle_list_split,
                        R.id.squircle_list_desktop
                };
                for (int id : squircles) {
                    View sq = root.findViewById(id);
                    if (sq != null) {
                        GradientDrawable sqGd = new GradientDrawable();
                        sqGd.setColor(bgSquircleLight);
                        sqGd.setStroke(dpToPx(1), borderLight);
                        sqGd.setCornerRadius(dpToPx(14));
                        sq.setBackground(sqGd);
                    }
                }

                ImageView iconNewTab = root.findViewById(R.id.icon_list_new_tab);
                if (iconNewTab != null) iconNewTab.setColorFilter(subText);
                TextView textNewTab = root.findViewById(R.id.text_list_new_tab);
                if (textNewTab != null) textNewTab.setTextColor(subText);

                ImageView iconSplit = root.findViewById(R.id.icon_list_split);
                if (iconSplit != null) iconSplit.setColorFilter(subText);
                TextView textSplit = root.findViewById(R.id.text_list_split);
                if (textSplit != null) textSplit.setTextColor(subText);

                ImageView iconDesktop = root.findViewById(R.id.icon_list_desktop);
                if (iconDesktop != null) iconDesktop.setColorFilter(subText);
                TextView textDesktop = root.findViewById(R.id.text_list_desktop);
                if (textDesktop != null) textDesktop.setTextColor(subText);

                // Dual AI Card (Highlighted Sky Blue Accent)
                View sqDualAi = root.findViewById(R.id.squircle_list_dual_ai);
                if (sqDualAi != null) {
                    GradientDrawable dualAiGd = new GradientDrawable();
                    dualAiGd.setColor(0xFFE0F2FE);
                    dualAiGd.setStroke(dpToPx(2), 0xFF0284C7);
                    dualAiGd.setCornerRadius(dpToPx(14));
                    sqDualAi.setBackground(dualAiGd);
                }
                ImageView iconDualAi = root.findViewById(R.id.icon_list_dual_ai);
                if (iconDualAi != null) iconDualAi.setColorFilter(0xFF0284C7);
                TextView textDualAi = root.findViewById(R.id.text_list_dual_ai);
                if (textDualAi != null) textDualAi.setTextColor(0xFF0284C7);

                // Theme Toggle Tile (Night / Light Mode)
                View sqTheme = root.findViewById(R.id.squircle_list_theme);
                if (sqTheme != null) {
                    GradientDrawable themeGd = new GradientDrawable();
                    themeGd.setColor(bgSquircleLight);
                    themeGd.setStroke(dpToPx(1), borderLight);
                    themeGd.setCornerRadius(dpToPx(14));
                    sqTheme.setBackground(themeGd);
                }
                ImageView iconTheme = root.findViewById(R.id.icon_list_theme);
                if (iconTheme != null) {
                    iconTheme.setImageResource(R.drawable.ic_menu_moon);
                    iconTheme.setColorFilter(0xFFD97706);
                }
                TextView textTheme = root.findViewById(R.id.text_list_theme);
                if (textTheme != null) {
                    textTheme.setText("Night");
                    textTheme.setTextColor(subText);
                }

                // Section 2: Interactive System Controls (Zoom & PDF)
                View widgetZoom = root.findViewById(R.id.widget_zoom_stepper);
                if (widgetZoom != null) {
                    GradientDrawable wzGd = new GradientDrawable();
                    wzGd.setColor(0xFFF8FAFC);
                    wzGd.setStroke(dpToPx(1), borderLight);
                    wzGd.setCornerRadius(dpToPx(16));
                    widgetZoom.setBackground(wzGd);
                }
                ImageView iconZoom = root.findViewById(R.id.icon_widget_zoom);
                if (iconZoom != null) iconZoom.setColorFilter(0xFF64748B);
                TextView textZoom = root.findViewById(R.id.text_zoom_label);
                if (textZoom != null) textZoom.setTextColor(slateText);

                View capsuleZoom = root.findViewById(R.id.capsule_zoom_stepper);
                if (capsuleZoom != null) {
                    GradientDrawable czGd = new GradientDrawable();
                    czGd.setColor(0xFFFFFFFF);
                    czGd.setStroke(dpToPx(1), borderLight);
                    czGd.setCornerRadius(dpToPx(10));
                    capsuleZoom.setBackground(czGd);
                }
                TextView btnMinus = root.findViewById(R.id.btn_stepper_minus);
                if (btnMinus != null) btnMinus.setTextColor(subText);
                TextView textZoomVal = root.findViewById(R.id.text_stepper_zoom_val);
                if (textZoomVal != null) textZoomVal.setTextColor(0xFF0284C7);
                TextView btnPlus = root.findViewById(R.id.btn_stepper_plus);
                if (btnPlus != null) btnPlus.setTextColor(subText);

                View widgetPdf = root.findViewById(R.id.widget_open_pdf);
                if (widgetPdf != null) {
                    GradientDrawable wpdfGd = new GradientDrawable();
                    wpdfGd.setColor(0xFFF8FAFC);
                    wpdfGd.setStroke(dpToPx(1), borderLight);
                    wpdfGd.setCornerRadius(dpToPx(16));
                    widgetPdf.setBackground(wpdfGd);
                }
                ImageView iconPdf = root.findViewById(R.id.icon_open_pdf);
                if (iconPdf != null) iconPdf.setColorFilter(0xFFE11D48);
                TextView textPdf = root.findViewById(R.id.text_open_pdf_label);
                if (textPdf != null) textPdf.setTextColor(slateText);

                // Dividers
                int[] dividers = {
                        R.id.divider_list_1, R.id.divider_list_2, R.id.divider_list_3, R.id.divider_list_4
                };
                for (int dId : dividers) {
                    View div = root.findViewById(dId);
                    if (div != null) div.setBackgroundColor(borderLight);
                }

                // Action List Rows
                int[] rowIcons = {
                        R.id.icon_row_find, R.id.icon_row_history, R.id.icon_row_downloads,
                        R.id.icon_row_share, R.id.icon_row_settings
                };
                for (int rIconId : rowIcons) {
                    ImageView riv = root.findViewById(rIconId);
                    if (riv != null) riv.setColorFilter(0xFF64748B);
                }

                int[] rowLabels = {
                        R.id.text_row_find_label, R.id.text_row_history_label,
                        R.id.text_row_downloads_label, R.id.text_row_share_label, R.id.text_row_settings_label
                };
                for (int rLblId : rowLabels) {
                    TextView rtv = root.findViewById(rLblId);
                    if (rtv != null) rtv.setTextColor(slateText);
                }

                // ⌘F Shortcut Badge
                TextView badgeFind = root.findViewById(R.id.badge_row_find_shortcut);
                if (badgeFind != null) {
                    GradientDrawable bfGd = new GradientDrawable();
                    bfGd.setColor(0xFFF1F5F9);
                    bfGd.setStroke(dpToPx(1), borderLight);
                    bfGd.setCornerRadius(dpToPx(6));
                    badgeFind.setBackground(bfGd);
                    badgeFind.setTextColor(0xFF475569);
                }

                TextView badgeHistory = root.findViewById(R.id.badge_row_history_time);
                if (badgeHistory != null) badgeHistory.setTextColor(0xFF94A3B8);

                View badgeDownloads = root.findViewById(R.id.badge_row_downloads_container);
                if (badgeDownloads != null) {
                    GradientDrawable bdGd = new GradientDrawable();
                    bdGd.setColor(0xFFDCFCE7);
                    bdGd.setCornerRadius(dpToPx(10));
                    badgeDownloads.setBackground(bdGd);
                }
                TextView textDownloadsBadge = root.findViewById(R.id.badge_row_downloads_count);
                if (textDownloadsBadge != null) textDownloadsBadge.setTextColor(0xFF15803D);

                // Share & Export Format Chips
                int[] exportChips = {
                        R.id.chip_export_pdf, R.id.chip_export_txt, R.id.chip_export_doc, R.id.chip_export_md
                };
                for (int cId : exportChips) {
                    TextView chip = root.findViewById(cId);
                    if (chip != null) {
                        GradientDrawable chipGd = new GradientDrawable();
                        chipGd.setColor(0xFFF1F5F9);
                        chipGd.setStroke(dpToPx(1), borderLight);
                        chipGd.setCornerRadius(dpToPx(8));
                        chip.setBackground(chipGd);
                        chip.setTextColor(subText);
                    }
                }

                TextView badgeSettings = root.findViewById(R.id.badge_row_settings);
                if (badgeSettings != null) badgeSettings.setTextColor(0xFF94A3B8);

                // Section 4: Caspian Waveguard Pro Card
                View cardWaveguard = root.findViewById(R.id.card_waveguard_pro);
                if (cardWaveguard != null) {
                    GradientDrawable wgCardGd = new GradientDrawable(
                            GradientDrawable.Orientation.TL_BR,
                            new int[]{0xFFECFDF5, 0xFFF0FDF4, 0xFFF0F9FF}
                    );
                    wgCardGd.setStroke(dpToPx(1), 0xFFA7F3D0);
                    wgCardGd.setCornerRadius(dpToPx(18));
                    cardWaveguard.setBackground(wgCardGd);
                }

                View sqWaveguard = root.findViewById(R.id.squircle_waveguard_pro);
                if (sqWaveguard != null) {
                    GradientDrawable sqWgGd = new GradientDrawable();
                    sqWgGd.setColor(0xFF059669);
                    sqWgGd.setCornerRadius(dpToPx(12));
                    sqWaveguard.setBackground(sqWgGd);
                }

                TextView titleWaveguard = root.findViewById(R.id.text_waveguard_pro_title);
                if (titleWaveguard != null) titleWaveguard.setTextColor(0xFF0F172A);

                TextView badgeWaveguard = root.findViewById(R.id.badge_waveguard_pro_active);
                if (badgeWaveguard != null) {
                    GradientDrawable bwGd = new GradientDrawable();
                    bwGd.setColor(0xFFD1FAE5);
                    bwGd.setCornerRadius(dpToPx(8));
                    badgeWaveguard.setBackground(bwGd);
                    badgeWaveguard.setTextColor(0xFF065F46);
                }

                TextView blockedWaveguard = root.findViewById(R.id.text_waveguard_pro_blocked);
                if (blockedWaveguard != null) blockedWaveguard.setTextColor(0xFF475569);

                TextView btnWaveguardSettings = root.findViewById(R.id.btn_waveguard_pro_settings);
                if (btnWaveguardSettings != null) {
                    GradientDrawable bwsGd = new GradientDrawable();
                    bwsGd.setColor(0xFFFFFFFF);
                    bwsGd.setStroke(dpToPx(1), 0xFFCBD5E1);
                    bwsGd.setCornerRadius(dpToPx(10));
                    btnWaveguardSettings.setBackground(bwsGd);
                    btnWaveguardSettings.setTextColor(0xFF334155);
                }
            } else {
                // Dark Theme Reset
                root.setBackgroundResource(R.drawable.bg_caspian_dialog);
                View handle = root.findViewById(R.id.action_list_drag_handle);
                if (handle != null) handle.setBackgroundResource(R.drawable.bg_stitch_handle_bar);

                int lightText = 0xFFDFE2F0;
                int mutedText = 0xFF94A3B8;

                int[] squircles = {
                        R.id.squircle_list_new_tab, R.id.squircle_list_split,
                        R.id.squircle_list_desktop, R.id.squircle_list_theme
                };
                for (int id : squircles) {
                    View sq = root.findViewById(id);
                    if (sq != null) sq.setBackgroundResource(R.drawable.bg_stitch_squircle);
                }

                View sqDualAi = root.findViewById(R.id.squircle_list_dual_ai);
                if (sqDualAi != null) sqDualAi.setBackgroundResource(R.drawable.bg_stitch_squircle_active);

                ImageView iconTheme = root.findViewById(R.id.icon_list_theme);
                if (iconTheme != null) {
                    iconTheme.setImageResource(R.drawable.ic_menu_sun);
                    iconTheme.setColorFilter(lightText);
                }
                TextView textTheme = root.findViewById(R.id.text_list_theme);
                if (textTheme != null) {
                    textTheme.setText("Light");
                    textTheme.setTextColor(lightText);
                }

                View widgetZoom = root.findViewById(R.id.widget_zoom_stepper);
                if (widgetZoom != null) widgetZoom.setBackgroundResource(R.drawable.bg_stitch_squircle);
                View capsuleZoom = root.findViewById(R.id.capsule_zoom_stepper);
                if (capsuleZoom != null) capsuleZoom.setBackgroundResource(R.drawable.bg_stitch_zoom_stepper);

                View widgetPdf = root.findViewById(R.id.widget_open_pdf);
                if (widgetPdf != null) widgetPdf.setBackgroundResource(R.drawable.bg_stitch_squircle);

                int[] exportChips = {
                        R.id.chip_export_pdf, R.id.chip_export_txt, R.id.chip_export_doc, R.id.chip_export_md
                };
                for (int cId : exportChips) {
                    TextView chip = root.findViewById(cId);
                    if (chip != null) {
                        chip.setBackgroundResource(R.drawable.bg_stitch_action_chip);
                        chip.setTextColor(lightText);
                    }
                }

                View cardWaveguard = root.findViewById(R.id.card_waveguard_pro);
                if (cardWaveguard != null) cardWaveguard.setBackgroundResource(R.drawable.bg_stitch_waveguard_card);

                TextView blockedWaveguard = root.findViewById(R.id.text_waveguard_pro_blocked);
                if (blockedWaveguard != null) blockedWaveguard.setTextColor(mutedText);

                TextView btnWaveguardSettings = root.findViewById(R.id.btn_waveguard_pro_settings);
                if (btnWaveguardSettings != null) {
                    btnWaveguardSettings.setBackgroundResource(R.drawable.bg_stitch_action_chip);
                    btnWaveguardSettings.setTextColor(lightText);
                }
            }
        } catch (Throwable ignored) {}
    }

    private void attachSpringPhysics(View v) {
        if (v == null) return;
        v.setOnTouchListener((view, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    view.animate()
                            .scaleX(0.92f)
                            .scaleY(0.92f)
                            .setDuration(110)
                            .setInterpolator(new DecelerateInterpolator())
                            .start();
                    break;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    view.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(240)
                            .setInterpolator(new OvershootInterpolator(1.8f))
                            .start();
                    break;
            }
            return false;
        });
    }

    private void animateGridTilesEntrance(SwipeableViewFlipper flipper) {
        if (flipper == null) return;
        View current = flipper.getCurrentView();
        if (!(current instanceof ViewGroup)) return;
        ViewGroup page = (ViewGroup) current;

        int tileIndex = 0;
        for (int i = 0; i < page.getChildCount(); i++) {
            View child = page.getChildAt(i);
            if (child instanceof TableLayout) {
                TableLayout table = (TableLayout) child;
                for (int r = 0; r < table.getChildCount(); r++) {
                    View row = table.getChildAt(r);
                    if (row instanceof TableRow) {
                        TableRow tr = (TableRow) row;
                        for (int c = 0; c < tr.getChildCount(); c++) {
                            View tile = tr.getChildAt(c);
                            if (tile != null && tile.getVisibility() == View.VISIBLE) {
                                tile.setAlpha(0f);
                                tile.setTranslationX(dpToPx(14));
                                tile.setTranslationY(dpToPx(16));
                                tile.setScaleX(0.88f);
                                tile.setScaleY(0.88f);
                                tile.animate()
                                        .alpha(1f)
                                        .translationX(0f)
                                        .translationY(0f)
                                        .scaleX(1f)
                                        .scaleY(1f)
                                        .setStartDelay(60 + (tileIndex * 22))
                                        .setDuration(280)
                                        .setInterpolator(new OvershootInterpolator(1.25f))
                                        .start();
                                tileIndex++;
                            }
                        }
                    }
                }
            } else if (child != null && child.getVisibility() == View.VISIBLE) {
                child.setAlpha(0f);
                child.setTranslationY(dpToPx(14));
                child.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setStartDelay(60 + (tileIndex * 22))
                        .setDuration(280)
                        .setInterpolator(new OvershootInterpolator(1.25f))
                        .start();
                tileIndex++;
            }
        }
    }

    public void showBrowserActionGrid() {
        TabItem currentTab = getActiveOrDominantTab();
        com.google.android.material.bottomsheet.BottomSheetDialog dialog =
                new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_browser_action_grid, null);
        dialog.setContentView(dialogView);

        if (dialog.getWindow() != null) {
            View bs = dialog.getWindow().findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bs != null) bs.setBackgroundResource(android.R.color.transparent);
        }

        final boolean[] isClosing = {false};
        Runnable performSClosing = () -> {
            if (isClosing[0]) return;
            isClosing[0] = true;
            ValueAnimator closeAnim = ValueAnimator.ofFloat(0f, 1f);
            closeAnim.setDuration(240);
            closeAnim.setInterpolator(new PathInterpolator(0.38f, 0.0f, 0.20f, 1.0f));
            closeAnim.addUpdateListener(anim -> {
                float f = anim.getAnimatedFraction();
                float sX = (float) -Math.sin(f * Math.PI) * (1f - f) * dpToPx(24);
                float rot = -2.0f * f;
                dialogView.setTranslationX(sX);
                dialogView.setTranslationY(dpToPx(380) * f);
                dialogView.setRotation(rot);
                dialogView.setScaleX(1.0f - (0.08f * f));
                dialogView.setScaleY(1.0f - (0.08f * f));
                dialogView.setAlpha(1.0f - f);
            });
            closeAnim.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    try {
                        dialog.dismiss();
                    } catch (Exception ignored) {}
                }
            });
            closeAnim.start();
        };

        java.util.function.Consumer<Runnable> dismissWithAction = action -> {
            if (isClosing[0]) return;
            isClosing[0] = true;
            ValueAnimator closeAnim = ValueAnimator.ofFloat(0f, 1f);
            closeAnim.setDuration(220);
            closeAnim.setInterpolator(new PathInterpolator(0.38f, 0.0f, 0.20f, 1.0f));
            closeAnim.addUpdateListener(anim -> {
                float f = anim.getAnimatedFraction();
                float sX = (float) -Math.sin(f * Math.PI) * (1f - f) * dpToPx(24);
                float rot = -2.0f * f;
                dialogView.setTranslationX(sX);
                dialogView.setTranslationY(dpToPx(380) * f);
                dialogView.setRotation(rot);
                dialogView.setScaleX(1.0f - (0.08f * f));
                dialogView.setScaleY(1.0f - (0.08f * f));
                dialogView.setAlpha(1.0f - f);
            });
            closeAnim.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    try {
                        dialog.dismiss();
                    } catch (Exception ignored) {}
                    if (action != null) action.run();
                }
            });
            closeAnim.start();
        };

        // Top drag handle tap dismiss
        View dragHandle = dialogView.findViewById(R.id.action_grid_drag_handle);
        if (dragHandle != null) {
            dragHandle.setOnClickListener(v -> performSClosing.run());
        }

        // Back key intercept
        dialog.setOnKeyListener((dialogInterface, keyCode, event) -> {
            if (keyCode == android.view.KeyEvent.KEYCODE_BACK && event.getAction() == android.view.KeyEvent.ACTION_UP) {
                if (!isClosing[0]) {
                    performSClosing.run();
                    return true;
                }
            }
            return false;
        });

        // Outside backdrop tap intercept
        if (dialog.getWindow() != null) {
            View touchOutside = dialog.getWindow().findViewById(com.google.android.material.R.id.touch_outside);
            if (touchOutside != null) {
                touchOutside.setOnClickListener(v -> performSClosing.run());
            }
        }

        // SwipeableViewFlipper Page Switcher & Dynamic Indicator Dots
        SwipeableViewFlipper flipper = dialogView.findViewById(R.id.action_grid_flipper);
        LinearLayout dotsLayout = dialogView.findViewById(R.id.action_grid_dots_layout);

        dialog.setOnShowListener(dialogInterface -> {
            com.google.android.material.bottomsheet.BottomSheetDialog d = (com.google.android.material.bottomsheet.BottomSheetDialog) dialogInterface;
            FrameLayout bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                com.google.android.material.bottomsheet.BottomSheetBehavior<FrameLayout> behavior =
                        com.google.android.material.bottomsheet.BottomSheetBehavior.from(bottomSheet);
                ViewGroup.LayoutParams lp = bottomSheet.getLayoutParams();
                if (lp != null) {
                    lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    bottomSheet.setLayoutParams(lp);
                }
                behavior.setFitToContents(true);
                behavior.setSkipCollapsed(true);
                behavior.setState(com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED);
            }

            // Smooth Apple-like S-shaped entrance animation
            dialogView.setAlpha(0f);
            dialogView.setTranslationY(dpToPx(380));
            dialogView.setScaleX(0.90f);
            dialogView.setScaleY(0.90f);

            ValueAnimator sAnim = ValueAnimator.ofFloat(0f, 1f);
            sAnim.setDuration(380);
            sAnim.setInterpolator(new PathInterpolator(0.24f, 1.0f, 0.32f, 1.0f));
            sAnim.addUpdateListener(anim -> {
                float f = anim.getAnimatedFraction(); // 0 -> 1
                float sX = (float) Math.sin(f * Math.PI) * (1f - f) * dpToPx(30);
                float rot = 2.2f * (1f - f) * (float) Math.cos(f * Math.PI * 0.75f);
                dialogView.setTranslationX(sX);
                dialogView.setTranslationY(dpToPx(380) * (1f - f));
                dialogView.setRotation(rot);
                dialogView.setScaleX(0.90f + (0.10f * f));
                dialogView.setScaleY(0.90f + (0.10f * f));
                dialogView.setAlpha(Math.min(1f, f * 2.2f));
            });
            sAnim.start();

            // Button physics cascade: tiles and widgets react to the momentum!
            animateGridTilesEntrance(flipper);
        });

        List<List<String>> pages = getCardGridAllPages();

        TextView badgeVersion = dialogView.findViewById(R.id.badge_app_version);
        if (badgeVersion != null) {
            try {
                String vName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
                badgeVersion.setText("v" + vName);
            } catch (Exception ignored) {}
            badgeVersion.setOnClickListener(v -> dismissWithAction.accept(() -> {
                playUiFeedbackSound("tap");
                openCaspianUpdateMenu();
            }));
        }

        // Caspian Cask Interactive Pill on Page 1
        View widgetCaskBar = dialogView.findViewById(R.id.widget_grid_cask_bar);
        TextView iconCask = dialogView.findViewById(R.id.icon_grid_cask);
        TextView textCaskName = dialogView.findViewById(R.id.text_grid_cask_name);
        View btnCaskSwitch = dialogView.findViewById(R.id.btn_grid_cask_switch);

        CaskManager cm = new CaskManager(this);
        String activeCaskId = (currentTab != null && currentTab.caskId != null) ? currentTab.caskId : cm.getActiveCaskId();
        CaskManager.CaskItem activeCask = cm.getCaskById(activeCaskId);
        if (activeCask == null) activeCask = cm.getActiveCask();

        if (iconCask != null && activeCask != null && activeCask.icon != null) {
            iconCask.setText(activeCask.icon);
        }
        if (textCaskName != null && activeCask != null && activeCask.name != null) {
            textCaskName.setText(activeCask.name);
        }

        View.OnClickListener caskSwitchListener = v -> dismissWithAction.accept(() -> {
            playUiFeedbackSound("tap");
            showCaskSwitcherDialog(currentTab);
        });
        if (btnCaskSwitch != null) btnCaskSwitch.setOnClickListener(caskSwitchListener);
        if (widgetCaskBar != null) widgetCaskBar.setOnClickListener(caskSwitchListener);

        // Zoom Stepper Interactive Pill on Page 2
        View widgetZoom = dialogView.findViewById(R.id.widget_grid_zoom_stepper);

        // Detach widgets from default layout so they can be placed dynamically into pages
        if (widgetCaskBar != null && widgetCaskBar.getParent() instanceof ViewGroup) {
            ((ViewGroup) widgetCaskBar.getParent()).removeView(widgetCaskBar);
        }
        if (widgetZoom != null && widgetZoom.getParent() instanceof ViewGroup) {
            ((ViewGroup) widgetZoom.getParent()).removeView(widgetZoom);
        }

        // Collect action tile views
        Map<String, View> tileMap = new HashMap<>();
        tileMap.put("night_mode", dialogView.findViewById(R.id.tile_night_mode));
        tileMap.put("desktop_site", dialogView.findViewById(R.id.tile_desktop_site));
        tileMap.put("bookmarks", dialogView.findViewById(R.id.tile_bookmarks));
        tileMap.put("history", dialogView.findViewById(R.id.tile_history));
        tileMap.put("downloads", dialogView.findViewById(R.id.tile_downloads));
        tileMap.put("incognito", dialogView.findViewById(R.id.tile_incognito));
        tileMap.put("find", dialogView.findViewById(R.id.tile_find));
        tileMap.put("share", dialogView.findViewById(R.id.tile_share));
        tileMap.put("split", dialogView.findViewById(R.id.tile_split));
        tileMap.put("settings", dialogView.findViewById(R.id.tile_settings));

        tileMap.put("new_tab", dialogView.findViewById(R.id.tile_new_tab));
        tileMap.put("dual_ai", dialogView.findViewById(R.id.tile_dual_ai));
        tileMap.put("pdf", dialogView.findViewById(R.id.tile_pdf));
        tileMap.put("print", dialogView.findViewById(R.id.tile_print));
        tileMap.put("shield", dialogView.findViewById(R.id.tile_shield));
        tileMap.put("clear_data", dialogView.findViewById(R.id.tile_clear_data));
        tileMap.put("view_bookmarks", dialogView.findViewById(R.id.tile_view_bookmarks));
        tileMap.put("edit_layout", dialogView.findViewById(R.id.tile_edit_layout));

        // Detach tiles from static layout to dynamically order into pages
        for (View tileView : tileMap.values()) {
            if (tileView != null && tileView.getParent() instanceof ViewGroup) {
                ((ViewGroup) tileView.getParent()).removeView(tileView);
            }
        }

        final SharedPreferences gridPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int savedPage = gridPrefs.getInt("last_card_grid_page", 0);
        int initialPage = Math.max(0, Math.min(savedPage, pages.size() - 1));

        try {
            if (flipper != null) {
                flipper.removeAllViews();
                for (int p = 0; p < pages.size(); p++) {
                    LinearLayout pageWrapper = new LinearLayout(this);
                    pageWrapper.setOrientation(LinearLayout.VERTICAL);
                    pageWrapper.setLayoutParams(new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                    TableLayout pageTable = new TableLayout(this);
                    pageTable.setLayoutParams(new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    populateGridTable(pageTable, pages.get(p), tileMap);
                    pageWrapper.addView(pageTable);

                    if (p == 0 && widgetCaskBar != null) {
                        if (widgetCaskBar.getParent() instanceof ViewGroup) {
                            ((ViewGroup) widgetCaskBar.getParent()).removeView(widgetCaskBar);
                        }
                        pageWrapper.addView(widgetCaskBar);
                    } else if (p == 1 && widgetZoom != null) {
                        if (widgetZoom.getParent() instanceof ViewGroup) {
                            ((ViewGroup) widgetZoom.getParent()).removeView(widgetZoom);
                        }
                        pageWrapper.addView(widgetZoom);
                    }

                    flipper.addView(pageWrapper);
                }

                flipper.setOnPageChangeListener(pageIndex -> {
                    gridPrefs.edit().putInt("last_card_grid_page", pageIndex).apply();
                    updateDynamicIndicatorDots(dotsLayout, flipper, pageIndex, pages.size(), isDarkTheme);
                });

                if (initialPage > 0 && initialPage < pages.size()) {
                    flipper.setDisplayedChild(initialPage);
                }
            }
        } catch (Throwable t) {
            android.util.Log.e("MainActivity", "Error populating action grid flipper", t);
        }

        updateDynamicIndicatorDots(dotsLayout, flipper, initialPage, pages.size(), isDarkTheme);

        // Wire tile click actions with smooth dismiss and spring physics
        View tileNight = tileMap.get("night_mode");
        if (tileNight != null) {
            tileNight.setOnClickListener(v -> dismissWithAction.accept(() -> toggleHostTheme(!isDarkTheme)));
        }
        View tileDesktop = tileMap.get("desktop_site");
        if (tileDesktop != null) {
            tileDesktop.setOnClickListener(v -> dismissWithAction.accept(() -> {
                if (currentTab != null) toggleDesktopMode(currentTab.id);
            }));
        }
        View tileBookmarks = tileMap.get("bookmarks");
        if (tileBookmarks != null) {
            tileBookmarks.setOnClickListener(v -> dismissWithAction.accept(() -> {
                if (bookmarkManager == null) bookmarkManager = new BookmarkManager(this);
                showAddBookmarkDialog(null);
            }));
        }
        View tileViewBookmarks = tileMap.get("view_bookmarks");
        if (tileViewBookmarks != null) {
            tileViewBookmarks.setOnClickListener(v -> dismissWithAction.accept(this::showBookmarksDialog));
        }
        View tileHistory = tileMap.get("history");
        if (tileHistory != null) {
            tileHistory.setOnClickListener(v -> dismissWithAction.accept(this::showHistoryDialog));
        }
        View tileDownloads = tileMap.get("downloads");
        if (tileDownloads != null) {
            tileDownloads.setOnClickListener(v -> dismissWithAction.accept(this::openDownloadsManagerModal));
        }
        View tileIncognito = tileMap.get("incognito");
        if (tileIncognito != null) {
            tileIncognito.setOnClickListener(v -> dismissWithAction.accept(() -> {
                addNewTab("web", null, "https://www.google.com", true);
                Toast.makeText(this, "🕶️ Incognito tab opened", Toast.LENGTH_SHORT).show();
            }));
        }
        View tileFind = tileMap.get("find");
        if (tileFind != null) {
            tileFind.setOnClickListener(v -> dismissWithAction.accept(this::showOmniboxFinder));
        }
        View tileShare = tileMap.get("share");
        if (tileShare != null) {
            tileShare.setOnClickListener(v -> dismissWithAction.accept(() -> {
                playUiFeedbackSound("tap");
                if (currentTab != null) {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    String shareUrl = (currentTab.url != null && !currentTab.url.isEmpty()) ? currentTab.url : (currentTab.webView != null ? currentTab.webView.getUrl() : "");
                    String shareTitle = (currentTab.title != null && !currentTab.title.isEmpty()) ? currentTab.title : "Caspian Flow";
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, shareTitle);
                    shareIntent.putExtra(Intent.EXTRA_TEXT, (shareUrl != null && !shareUrl.isEmpty()) ? shareUrl : shareTitle);
                    startActivity(Intent.createChooser(shareIntent, "Share Page via"));
                }
            }));
        }
        View tileSplit = tileMap.get("split");
        if (tileSplit != null) {
            tileSplit.setOnClickListener(v -> dismissWithAction.accept(this::cycleSplitViewMode));
        }
        View tileSettings = tileMap.get("settings");
        if (tileSettings != null) {
            tileSettings.setOnClickListener(v -> dismissWithAction.accept(this::openControlSheet));
        }

        View tileNewTab = tileMap.get("new_tab");
        if (tileNewTab != null) {
            tileNewTab.setOnClickListener(v -> dismissWithAction.accept(() -> addNewTab("hub", null)));
        }
        View tileDualAi = tileMap.get("dual_ai");
        if (tileDualAi != null) {
            tileDualAi.setOnClickListener(v -> dismissWithAction.accept(this::launchDualAIAsk));
        }
        View tilePdf = tileMap.get("pdf");
        if (tilePdf != null) {
            tilePdf.setOnClickListener(v -> dismissWithAction.accept(this::openPdfPicker));
        }
        View tilePrint = tileMap.get("print");
        if (tilePrint != null) {
            tilePrint.setOnClickListener(v -> dismissWithAction.accept(() -> {
                playUiFeedbackSound("tap");
                showPrintAndExportDialog(currentTab);
            }));
        }
        View tileShield = tileMap.get("shield");
        if (tileShield != null) {
            tileShield.setOnClickListener(v -> dismissWithAction.accept(() -> showWaveguardFlyout(dialogView)));
            tileShield.setOnLongClickListener(v -> {
                if (waveguardShield != null) {
                    boolean nextState = !waveguardShield.isGlobalEnabled();
                    waveguardShield.setGlobalEnabled(nextState);
                    updateWaveguardVisuals(dialogView, nextState, isDarkTheme);
                    updateOmniboxState();
                    syncWaveguardToControlWeb();
                    playUiFeedbackSound("tap");
                    Toast.makeText(this, nextState ? "🛡️ Waveguard Shields ON" : "🛡️ Waveguard Shields OFF", Toast.LENGTH_SHORT).show();
                    if (currentTab != null && currentTab.webView != null) {
                        currentTab.webView.reload();
                    }
                }
                return true;
            });
        }
        View tileClearData = tileMap.get("clear_data");
        if (tileClearData != null) {
            tileClearData.setOnClickListener(v -> dismissWithAction.accept(() -> {
                new AlertDialog.Builder(this)
                        .setTitle("Clear Browsing Data")
                        .setMessage("Clear browser cache and history?")
                        .setPositiveButton("Clear", (d, w) -> {
                            android.webkit.WebStorage.getInstance().deleteAllData();
                            Toast.makeText(this, "Browsing data cleared", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }));
        }
        View tileEdit = tileMap.get("edit_layout");
        if (tileEdit != null) {
            tileEdit.setOnClickListener(v -> dismissWithAction.accept(this::showCardGridEditDialog));
        }

        // Zoom Stepper Controls
        TextView textZoomVal = dialogView.findViewById(R.id.text_grid_zoom_val);
        if (textZoomVal != null) {
            textZoomVal.setText(getPageZoom() + "%");
        }
        View btnZoomMinus = dialogView.findViewById(R.id.btn_grid_zoom_minus);
        if (btnZoomMinus != null) {
            btnZoomMinus.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                int newZoom = Math.max(25, getPageZoom() - 10);
                setPageZoom(newZoom);
                if (textZoomVal != null) textZoomVal.setText(newZoom + "%");
            });
        }
        View btnZoomPlus = dialogView.findViewById(R.id.btn_grid_zoom_plus);
        if (btnZoomPlus != null) {
            btnZoomPlus.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                int newZoom = Math.min(300, getPageZoom() + 10);
                setPageZoom(newZoom);
                if (textZoomVal != null) textZoomVal.setText(newZoom + "%");
            });
        }
        View btnZoomReset = dialogView.findViewById(R.id.btn_grid_zoom_reset);
        if (btnZoomReset != null) {
            btnZoomReset.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                setPageZoom(100);
                if (textZoomVal != null) textZoomVal.setText("100%");
            });
        }
        if (textZoomVal != null) {
            textZoomVal.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                setPageZoom(100);
                textZoomVal.setText("100%");
            });
        }

        // Bottom Bar Controls
        View btnExit = dialogView.findViewById(R.id.btn_action_exit_app);
        if (btnExit != null) {
            btnExit.setOnClickListener(v -> dismissWithAction.accept(this::finishAffinity));
        }

        View btnCloseGrid = dialogView.findViewById(R.id.btn_action_close_grid);
        if (btnCloseGrid != null) {
            btnCloseGrid.setOnClickListener(v -> performSClosing.run());
        }

        View capsuleWg = dialogView.findViewById(R.id.waveguard_status_capsule);
        if (capsuleWg != null) {
            capsuleWg.setOnClickListener(v -> dismissWithAction.accept(() -> showWaveguardFlyout(dialogView)));
        }

        // Attach tactile spring physics to all buttons and tiles
        for (View tileView : tileMap.values()) {
            if (tileView != null) {
                attachSpringPhysics(tileView);
            }
        }
        attachSpringPhysics(btnExit);
        attachSpringPhysics(btnCloseGrid);
        attachSpringPhysics(capsuleWg);
        attachSpringPhysics(widgetCaskBar);
        attachSpringPhysics(btnCaskSwitch);
        attachSpringPhysics(btnZoomMinus);
        attachSpringPhysics(btnZoomPlus);
        attachSpringPhysics(btnZoomReset);
        attachSpringPhysics(badgeVersion);

        applyGridTheme(dialogView, isDarkTheme);
        dialog.show();
    }

    private void updateDynamicIndicatorDots(LinearLayout dotsLayout, SwipeableViewFlipper flipper,
                                            int activePage, int totalPages, boolean isDark) {
        if (dotsLayout == null || totalPages <= 1) {
            if (dotsLayout != null) dotsLayout.setVisibility(View.GONE);
            return;
        }
        dotsLayout.setVisibility(View.VISIBLE);
        dotsLayout.removeAllViews();

        int activeWidth = dpToPx(18);
        int inactiveWidth = dpToPx(6);
        int dotHeight = dpToPx(6);
        int activeColor = !isDark ? 0xFF0284C7 : 0xFF00E5FF;
        int inactiveColor = !isDark ? 0xFFCBD5E1 : 0xFF475569;

        for (int i = 0; i < totalPages; i++) {
            final int pageIdx = i;
            View dot = new View(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    i == activePage ? activeWidth : inactiveWidth, dotHeight);
            if (i < totalPages - 1) {
                lp.setMarginEnd(dpToPx(7));
            }
            dot.setLayoutParams(lp);

            GradientDrawable gd = new GradientDrawable();
            gd.setColor(i == activePage ? activeColor : inactiveColor);
            gd.setCornerRadius(dpToPx(3));
            dot.setBackground(gd);

            dot.setOnClickListener(v -> {
                if (flipper != null && flipper.getDisplayedChild() != pageIdx) {
                    flipper.setDisplayedChildWithAnim(pageIdx);
                    updateDynamicIndicatorDots(dotsLayout, flipper, pageIdx, totalPages, isDark);
                }
            });

            dotsLayout.addView(dot);
        }
    }

    private void animateIndicatorDots(View dot1, View dot2, int targetPage, boolean isDark, boolean animate) {
        if (dot1 == null || dot2 == null) return;
        int activeWidth = dpToPx(18);
        int inactiveWidth = dpToPx(6);
        int height = dpToPx(6);
        int activeColor = !isDark ? 0xFF0284C7 : 0xFF00E5FF;
        int inactiveColor = !isDark ? 0xFFCBD5E1 : 0xFF475569;

        if (!animate) {
            ViewGroup.LayoutParams lp1 = dot1.getLayoutParams();
            if (lp1 != null) {
                lp1.width = targetPage == 0 ? activeWidth : inactiveWidth;
                lp1.height = height;
                dot1.setLayoutParams(lp1);
            }
            GradientDrawable d1 = new GradientDrawable();
            d1.setColor(targetPage == 0 ? activeColor : inactiveColor);
            d1.setCornerRadius(dpToPx(3));
            dot1.setBackground(d1);

            ViewGroup.LayoutParams lp2 = dot2.getLayoutParams();
            if (lp2 != null) {
                lp2.width = targetPage == 1 ? activeWidth : inactiveWidth;
                lp2.height = height;
                dot2.setLayoutParams(lp2);
            }
            GradientDrawable d2 = new GradientDrawable();
            d2.setColor(targetPage == 1 ? activeColor : inactiveColor);
            d2.setCornerRadius(dpToPx(3));
            dot2.setBackground(d2);
            return;
        }

        int startW1 = dot1.getWidth() > 0 ? dot1.getWidth() : (targetPage == 0 ? inactiveWidth : activeWidth);
        int targetW1 = targetPage == 0 ? activeWidth : inactiveWidth;

        int startW2 = dot2.getWidth() > 0 ? dot2.getWidth() : (targetPage == 1 ? inactiveWidth : activeWidth);
        int targetW2 = targetPage == 1 ? activeWidth : inactiveWidth;

        ValueAnimator anim = ValueAnimator.ofFloat(0f, 1f);
        anim.setDuration(220);
        anim.setInterpolator(new android.view.animation.DecelerateInterpolator());
        anim.addUpdateListener(animation -> {
            float frac = (float) animation.getAnimatedValue();
            int w1 = (int) (startW1 + (targetW1 - startW1) * frac);
            int w2 = (int) (startW2 + (targetW2 - startW2) * frac);

            ViewGroup.LayoutParams lp1 = dot1.getLayoutParams();
            if (lp1 != null) {
                lp1.width = w1;
                lp1.height = height;
                dot1.setLayoutParams(lp1);
            }
            ViewGroup.LayoutParams lp2 = dot2.getLayoutParams();
            if (lp2 != null) {
                lp2.width = w2;
                lp2.height = height;
                dot2.setLayoutParams(lp2);
            }

            GradientDrawable gd1 = new GradientDrawable();
            int col1 = (Integer) new android.animation.ArgbEvaluator().evaluate(
                    frac,
                    targetPage == 0 ? inactiveColor : activeColor,
                    targetPage == 0 ? activeColor : inactiveColor
            );
            gd1.setColor(col1);
            gd1.setCornerRadius(dpToPx(3));
            dot1.setBackground(gd1);

            GradientDrawable gd2 = new GradientDrawable();
            int col2 = (Integer) new android.animation.ArgbEvaluator().evaluate(
                    frac,
                    targetPage == 1 ? inactiveColor : activeColor,
                    targetPage == 1 ? activeColor : inactiveColor
            );
            gd2.setColor(col2);
            gd2.setCornerRadius(dpToPx(3));
            dot2.setBackground(gd2);
        });
        anim.start();
    }

    private void applyGridTheme(View root, boolean isDark) {
        if (root == null) return;
        try {
            TabItem activeTab = getActiveOrDominantTab();
            boolean isDesktopActive = activeTab != null && activeTab.isDesktop;

            if (!isDark) {
                // Pristine Light Theme for Card Grid
                GradientDrawable rootGd = new GradientDrawable();
                rootGd.setColor(0xFFFFFFFF);
                rootGd.setCornerRadii(new float[]{dpToPx(24), dpToPx(24), dpToPx(24), dpToPx(24), 0, 0, 0, 0});
                root.setBackground(rootGd);

                View handle = root.findViewById(R.id.action_grid_drag_handle);
                if (handle != null) {
                    GradientDrawable handleGd = new GradientDrawable();
                    handleGd.setColor(0xFFCBD5E1);
                    handleGd.setCornerRadius(dpToPx(3));
                    handle.setBackground(handleGd);
                }

                TextView textTitle = root.findViewById(R.id.text_menu_title);
                if (textTitle != null) textTitle.setTextColor(0xFF64748B);

                TextView badgeVer = root.findViewById(R.id.badge_app_version);
                if (badgeVer != null) {
                    GradientDrawable bvGd = new GradientDrawable();
                    bvGd.setColor(0xFFE0F2FE);
                    bvGd.setCornerRadius(dpToPx(8));
                    badgeVer.setBackground(bvGd);
                    badgeVer.setTextColor(0xFF0284C7);
                }

                // High-contrast icons and labels on light theme
                int deepDarkIcon = 0xFF0F172A;
                int deepDarkLabel = 0xFF1E293B;
                int borderLight = 0xFFE2E8F0;
                int bgSquircleLight = 0xFFF1F5F9;

                // All Standard Squircles across Page 1 and Page 2
                int[] squircles = {
                        R.id.squircle_night_mode, R.id.squircle_desktop_site, R.id.squircle_bookmarks,
                        R.id.squircle_history, R.id.squircle_downloads, R.id.squircle_incognito,
                        R.id.squircle_find, R.id.squircle_share, R.id.squircle_split,
                        R.id.squircle_pdf, R.id.squircle_print,
                        R.id.squircle_new_tab, R.id.squircle_view_bookmarks, R.id.squircle_edit_layout
                };
                for (int id : squircles) {
                    View sq = root.findViewById(id);
                    if (sq != null) {
                        GradientDrawable sqGd = new GradientDrawable();
                        sqGd.setColor(bgSquircleLight);
                        sqGd.setStroke(dpToPx(1), borderLight);
                        sqGd.setCornerRadius(dpToPx(16));
                        sq.setBackground(sqGd);
                    }
                }

                // Highlighted / Accent Squircles
                View sqSettings = root.findViewById(R.id.squircle_settings);
                if (sqSettings != null) {
                    GradientDrawable sqsGd = new GradientDrawable();
                    sqsGd.setColor(0xFFE0F2FE);
                    sqsGd.setStroke(dpToPx(2), 0xFF0284C7);
                    sqsGd.setCornerRadius(dpToPx(16));
                    sqSettings.setBackground(sqsGd);
                }

                View sqDualAi = root.findViewById(R.id.squircle_dual_ai);
                if (sqDualAi != null) {
                    GradientDrawable sqdGd = new GradientDrawable();
                    sqdGd.setColor(0xFFE0F2FE);
                    sqdGd.setStroke(dpToPx(2), 0xFF0284C7);
                    sqdGd.setCornerRadius(dpToPx(16));
                    sqDualAi.setBackground(sqdGd);
                }

                View sqEdit = root.findViewById(R.id.squircle_edit_layout);
                if (sqEdit != null) {
                    GradientDrawable sqeGd = new GradientDrawable();
                    sqeGd.setColor(0xFFE0F2FE);
                    sqeGd.setStroke(dpToPx(1), 0xFF0284C7);
                    sqeGd.setCornerRadius(dpToPx(16));
                    sqEdit.setBackground(sqeGd);
                }

                View sqShield = root.findViewById(R.id.squircle_shield);
                if (sqShield != null) {
                    GradientDrawable sqshGd = new GradientDrawable();
                    sqshGd.setColor(0xFFECFDF5);
                    sqshGd.setStroke(dpToPx(1), 0xFFA7F3D0);
                    sqshGd.setCornerRadius(dpToPx(16));
                    sqShield.setBackground(sqshGd);
                }

                View sqClear = root.findViewById(R.id.squircle_clear_data);
                if (sqClear != null) {
                    GradientDrawable sqcGd = new GradientDrawable();
                    sqcGd.setColor(0xFFFEF2F2);
                    sqcGd.setStroke(dpToPx(1), 0xFFFECACA);
                    sqcGd.setCornerRadius(dpToPx(16));
                    sqClear.setBackground(sqcGd);
                }

                // ALL Regular Tile Icons
                int[] tileIcons = {
                        R.id.icon_desktop_site, R.id.icon_bookmarks, R.id.icon_history,
                        R.id.icon_downloads, R.id.icon_incognito, R.id.icon_find,
                        R.id.icon_share, R.id.icon_split,
                        R.id.icon_new_tab, R.id.icon_print, R.id.icon_view_bookmarks
                };
                for (int id : tileIcons) {
                    ImageView iv = root.findViewById(id);
                    if (iv != null) iv.setColorFilter(deepDarkIcon);
                }

                // Accent & Special Icons
                ImageView ivNight = root.findViewById(R.id.icon_night_mode);
                if (ivNight != null) {
                    ivNight.setImageResource(R.drawable.ic_menu_moon);
                    ivNight.setColorFilter(0xFFD97706);
                }
                ImageView ivSettings = root.findViewById(R.id.icon_settings);
                if (ivSettings != null) ivSettings.setColorFilter(0xFF0284C7);
                ImageView ivDualAi = root.findViewById(R.id.icon_dual_ai);
                if (ivDualAi != null) ivDualAi.setColorFilter(0xFF0284C7);
                ImageView ivEdit = root.findViewById(R.id.icon_edit_layout);
                if (ivEdit != null) ivEdit.setColorFilter(0xFF0284C7);
                ImageView ivShield = root.findViewById(R.id.icon_shield);
                if (ivShield != null) ivShield.setColorFilter(0xFF059669);
                ImageView ivPdf = root.findViewById(R.id.icon_pdf);
                if (ivPdf != null) ivPdf.setColorFilter(0xFFE11D48);
                ImageView ivClear = root.findViewById(R.id.icon_clear_data);
                if (ivClear != null) ivClear.setColorFilter(0xFFDC2626);

                // ALL Tile Labels
                int[] tileLabels = {
                        R.id.text_night_mode, R.id.text_desktop_site, R.id.text_bookmarks,
                        R.id.text_history, R.id.text_downloads, R.id.text_incognito,
                        R.id.text_find, R.id.text_share, R.id.text_split,
                        R.id.text_pdf, R.id.text_print,
                        R.id.text_new_tab, R.id.text_view_bookmarks, R.id.text_edit_layout
                };
                for (int id : tileLabels) {
                    TextView tv = root.findViewById(id);
                    if (tv != null) tv.setTextColor(deepDarkLabel);
                }
                TextView tvNight = root.findViewById(R.id.text_night_mode);
                if (tvNight != null) {
                    tvNight.setText("Night mode");
                    tvNight.setTextColor(deepDarkLabel);
                }
                TextView tvSettings = root.findViewById(R.id.text_settings);
                if (tvSettings != null) tvSettings.setTextColor(0xFF0284C7);
                TextView tvDualAi = root.findViewById(R.id.text_dual_ai);
                if (tvDualAi != null) tvDualAi.setTextColor(0xFF0284C7);
                TextView tvEdit = root.findViewById(R.id.text_edit_layout);
                if (tvEdit != null) tvEdit.setTextColor(0xFF0284C7);
                TextView tvShield = root.findViewById(R.id.text_shield);
                if (tvShield != null) tvShield.setTextColor(0xFF059669);
                TextView tvClear = root.findViewById(R.id.text_clear_data);
                if (tvClear != null) tvClear.setTextColor(0xFFDC2626);

                if (isDesktopActive) {
                    View sqDesktop = root.findViewById(R.id.squircle_desktop_site);
                    if (sqDesktop != null) {
                        GradientDrawable sqdGd = new GradientDrawable();
                        sqdGd.setColor(0xFFE0F2FE);
                        sqdGd.setStroke(dpToPx(2), 0xFF0284C7);
                        sqdGd.setCornerRadius(dpToPx(16));
                        sqDesktop.setBackground(sqdGd);
                    }
                    ImageView ivDesktop = root.findViewById(R.id.icon_desktop_site);
                    if (ivDesktop != null) ivDesktop.setColorFilter(0xFF0284C7);
                    TextView tvDesktop = root.findViewById(R.id.text_desktop_site);
                    if (tvDesktop != null) tvDesktop.setTextColor(0xFF0284C7);
                }

                // Interactive Cask Pill Bar Light Theme
                View widgetCask = root.findViewById(R.id.widget_grid_cask_bar);
                if (widgetCask != null) {
                    GradientDrawable wcGd = new GradientDrawable();
                    wcGd.setColor(0xFFF1F5F9);
                    wcGd.setStroke(dpToPx(1), borderLight);
                    wcGd.setCornerRadius(dpToPx(16));
                    widgetCask.setBackground(wcGd);
                }
                TextView tvCaskLbl = root.findViewById(R.id.text_grid_cask_label);
                if (tvCaskLbl != null) tvCaskLbl.setTextColor(0xFF64748B);
                TextView tvCaskNm = root.findViewById(R.id.text_grid_cask_name);
                if (tvCaskNm != null) tvCaskNm.setTextColor(0xFF0284C7);

                View btnCaskSw = root.findViewById(R.id.btn_grid_cask_switch);
                if (btnCaskSw != null) {
                    GradientDrawable bcGd = new GradientDrawable();
                    bcGd.setColor(0xFFE0F2FE);
                    bcGd.setStroke(dpToPx(1), 0xFFCBD5E1);
                    bcGd.setCornerRadius(dpToPx(14));
                    btnCaskSw.setBackground(bcGd);
                }
                ImageView ivCaskSw = root.findViewById(R.id.icon_grid_cask_switch);
                if (ivCaskSw != null) ivCaskSw.setColorFilter(0xFF0284C7);
                TextView tvCaskSw = root.findViewById(R.id.text_grid_cask_switch);
                if (tvCaskSw != null) tvCaskSw.setTextColor(0xFF0284C7);

                // Interactive Zoom Stepper Light Theme
                View widgetZoom = root.findViewById(R.id.widget_grid_zoom_stepper);
                if (widgetZoom != null) {
                    GradientDrawable wzGd = new GradientDrawable();
                    wzGd.setColor(0xFFF1F5F9);
                    wzGd.setStroke(dpToPx(1), borderLight);
                    wzGd.setCornerRadius(dpToPx(16));
                    widgetZoom.setBackground(wzGd);
                }
                ImageView ivZoom = root.findViewById(R.id.icon_grid_zoom);
                if (ivZoom != null) ivZoom.setColorFilter(deepDarkIcon);
                TextView tvZoomLabel = root.findViewById(R.id.text_grid_zoom_label);
                if (tvZoomLabel != null) tvZoomLabel.setTextColor(deepDarkLabel);

                View capsuleZoom = root.findViewById(R.id.capsule_grid_zoom_stepper);
                if (capsuleZoom != null) {
                    GradientDrawable czGd = new GradientDrawable();
                    czGd.setColor(0xFFFFFFFF);
                    czGd.setStroke(dpToPx(1), 0xFFCBD5E1);
                    czGd.setCornerRadius(dpToPx(14));
                    capsuleZoom.setBackground(czGd);
                }
                TextView btnZoomM = root.findViewById(R.id.btn_grid_zoom_minus);
                if (btnZoomM != null) btnZoomM.setTextColor(0xFF0284C7);
                TextView tvZoomVal = root.findViewById(R.id.text_grid_zoom_val);
                if (tvZoomVal != null) tvZoomVal.setTextColor(0xFF0284C7);
                TextView btnZoomP = root.findViewById(R.id.btn_grid_zoom_plus);
                if (btnZoomP != null) btnZoomP.setTextColor(0xFF0284C7);
                ImageView btnZoomR = root.findViewById(R.id.btn_grid_zoom_reset);
                if (btnZoomR != null) btnZoomR.setColorFilter(0xFF0284C7);

                // Page Indicator Dots
                View dot1 = root.findViewById(R.id.pill_dot_page1);
                if (dot1 != null) {
                    GradientDrawable d1Gd = new GradientDrawable();
                    d1Gd.setColor(0xFF0284C7);
                    d1Gd.setCornerRadius(dpToPx(3));
                    dot1.setBackground(d1Gd);
                }
                View dot2 = root.findViewById(R.id.pill_dot_page2);
                if (dot2 != null) {
                    GradientDrawable d2Gd = new GradientDrawable();
                    d2Gd.setColor(0xFFCBD5E1);
                    d2Gd.setCornerRadius(dpToPx(3));
                    dot2.setBackground(d2Gd);
                }

                // Waveguard Capsule
                View capsuleWg = root.findViewById(R.id.waveguard_status_capsule);
                if (capsuleWg != null) {
                    GradientDrawable cwgGd = new GradientDrawable();
                    cwgGd.setColor(0xFFECFDF5);
                    cwgGd.setStroke(dpToPx(1), 0xFFA7F3D0);
                    cwgGd.setCornerRadius(dpToPx(18));
                    capsuleWg.setBackground(cwgGd);
                }
                TextView wgTitle = root.findViewById(R.id.waveguard_capsule_title);
                if (wgTitle != null) wgTitle.setTextColor(0xFF0F172A);
                TextView wgCount = root.findViewById(R.id.waveguard_capsule_count);
                if (wgCount != null) wgCount.setTextColor(0xFF059669);

                // Exit & Close Buttons
                ImageButton btnExit = root.findViewById(R.id.btn_action_exit_app);
                if (btnExit != null) {
                    GradientDrawable beGd = new GradientDrawable();
                    beGd.setColor(0xFFF1F5F9);
                    beGd.setStroke(dpToPx(1), borderLight);
                    beGd.setShape(GradientDrawable.OVAL);
                    btnExit.setBackground(beGd);
                    btnExit.setColorFilter(deepDarkIcon);
                }
                ImageButton btnClose = root.findViewById(R.id.btn_action_close_grid);
                if (btnClose != null) {
                    GradientDrawable bcGd = new GradientDrawable();
                    bcGd.setColor(0xFFF1F5F9);
                    bcGd.setStroke(dpToPx(1), borderLight);
                    bcGd.setShape(GradientDrawable.OVAL);
                    btnClose.setBackground(bcGd);
                    btnClose.setColorFilter(deepDarkIcon);
                }
            } else {
                // Dark Theme Reset
                root.setBackgroundResource(R.drawable.bg_caspian_dialog);
                View handle = root.findViewById(R.id.action_grid_drag_handle);
                if (handle != null) handle.setBackgroundResource(R.drawable.bg_stitch_handle_bar);

                int lightText = 0xFFDFE2F0;

                int[] squircles = {
                        R.id.squircle_night_mode, R.id.squircle_desktop_site, R.id.squircle_bookmarks,
                        R.id.squircle_history, R.id.squircle_downloads, R.id.squircle_incognito,
                        R.id.squircle_find, R.id.squircle_share, R.id.squircle_split,
                        R.id.squircle_pdf, R.id.squircle_print,
                        R.id.squircle_new_tab, R.id.squircle_shield, R.id.squircle_clear_data,
                        R.id.squircle_view_bookmarks, R.id.squircle_edit_layout
                };
                for (int id : squircles) {
                    View sq = root.findViewById(id);
                    if (sq != null) sq.setBackgroundResource(R.drawable.bg_stitch_squircle);
                }

                View sqSettings = root.findViewById(R.id.squircle_settings);
                if (sqSettings != null) sqSettings.setBackgroundResource(R.drawable.bg_stitch_squircle_active);
                View sqDualAi = root.findViewById(R.id.squircle_dual_ai);
                if (sqDualAi != null) sqDualAi.setBackgroundResource(R.drawable.bg_stitch_squircle_active);
                View sqEdit = root.findViewById(R.id.squircle_edit_layout);
                if (sqEdit != null) sqEdit.setBackgroundResource(R.drawable.bg_stitch_squircle_active);

                int[] tileIcons = {
                        R.id.icon_desktop_site, R.id.icon_bookmarks, R.id.icon_history,
                        R.id.icon_downloads, R.id.icon_incognito, R.id.icon_find,
                        R.id.icon_share, R.id.icon_split,
                        R.id.icon_new_tab, R.id.icon_print, R.id.icon_view_bookmarks
                };
                for (int id : tileIcons) {
                    ImageView iv = root.findViewById(id);
                    if (iv != null) iv.setColorFilter(lightText);
                }

                ImageView ivNight = root.findViewById(R.id.icon_night_mode);
                if (ivNight != null) {
                    ivNight.setImageResource(R.drawable.ic_menu_sun);
                    ivNight.setColorFilter(lightText);
                }
                ImageView ivSettings = root.findViewById(R.id.icon_settings);
                if (ivSettings != null) ivSettings.setColorFilter(0xFF00E5FF);
                ImageView ivDualAi = root.findViewById(R.id.icon_dual_ai);
                if (ivDualAi != null) ivDualAi.setColorFilter(0xFF00E5FF);
                ImageView ivEdit = root.findViewById(R.id.icon_edit_layout);
                if (ivEdit != null) ivEdit.setColorFilter(0xFF38BDF8);
                ImageView ivShield = root.findViewById(R.id.icon_shield);
                if (ivShield != null) ivShield.setColorFilter(0xFF10B981);
                ImageView ivPdf = root.findViewById(R.id.icon_pdf);
                if (ivPdf != null) ivPdf.setColorFilter(0xFFFB7185);
                ImageView ivClear = root.findViewById(R.id.icon_clear_data);
                if (ivClear != null) ivClear.setColorFilter(0xFFFF6B6B);

                int[] tileLabels = {
                        R.id.text_night_mode, R.id.text_desktop_site, R.id.text_bookmarks,
                        R.id.text_history, R.id.text_downloads, R.id.text_incognito,
                        R.id.text_find, R.id.text_share, R.id.text_split,
                        R.id.text_pdf, R.id.text_print,
                        R.id.text_new_tab, R.id.text_shield, R.id.text_view_bookmarks, R.id.text_edit_layout
                };
                for (int id : tileLabels) {
                    TextView tv = root.findViewById(id);
                    if (tv != null) tv.setTextColor(lightText);
                }
                TextView tvNight = root.findViewById(R.id.text_night_mode);
                if (tvNight != null) tvNight.setText("Light mode");

                TextView tvSettings = root.findViewById(R.id.text_settings);
                if (tvSettings != null) tvSettings.setTextColor(0xFF00E5FF);
                TextView tvDualAi = root.findViewById(R.id.text_dual_ai);
                if (tvDualAi != null) tvDualAi.setTextColor(0xFF00E5FF);
                TextView tvEdit = root.findViewById(R.id.text_edit_layout);
                if (tvEdit != null) tvEdit.setTextColor(0xFF38BDF8);
                TextView tvClear = root.findViewById(R.id.text_clear_data);
                if (tvClear != null) tvClear.setTextColor(0xFFFF6B6B);

                if (isDesktopActive) {
                    View sqDesktop = root.findViewById(R.id.squircle_desktop_site);
                    if (sqDesktop != null) sqDesktop.setBackgroundResource(R.drawable.bg_stitch_squircle_active);
                    ImageView ivDesktop = root.findViewById(R.id.icon_desktop_site);
                    if (ivDesktop != null) ivDesktop.setColorFilter(0xFF00E5FF);
                    TextView tvDesktop = root.findViewById(R.id.text_desktop_site);
                    if (tvDesktop != null) tvDesktop.setTextColor(0xFF00E5FF);
                }

                // Interactive Cask Pill Bar Dark Theme
                View widgetCask = root.findViewById(R.id.widget_grid_cask_bar);
                if (widgetCask != null) {
                    GradientDrawable wcGd = new GradientDrawable();
                    wcGd.setColor(0xFF161B22);
                    wcGd.setStroke(dpToPx(1), 0xFF30363D);
                    wcGd.setCornerRadius(dpToPx(16));
                    widgetCask.setBackground(wcGd);
                }
                TextView tvCaskLbl = root.findViewById(R.id.text_grid_cask_label);
                if (tvCaskLbl != null) tvCaskLbl.setTextColor(0xFF94A3B8);
                TextView tvCaskNm = root.findViewById(R.id.text_grid_cask_name);
                if (tvCaskNm != null) tvCaskNm.setTextColor(0xFF00E5FF);

                View btnCaskSw = root.findViewById(R.id.btn_grid_cask_switch);
                if (btnCaskSw != null) {
                    GradientDrawable bcGd = new GradientDrawable();
                    bcGd.setColor(0xFF0D1117);
                    bcGd.setStroke(dpToPx(1), 0xFF30363D);
                    bcGd.setCornerRadius(dpToPx(14));
                    btnCaskSw.setBackground(bcGd);
                }
                ImageView ivCaskSw = root.findViewById(R.id.icon_grid_cask_switch);
                if (ivCaskSw != null) ivCaskSw.setColorFilter(0xFF00E5FF);
                TextView tvCaskSw = root.findViewById(R.id.text_grid_cask_switch);
                if (tvCaskSw != null) tvCaskSw.setTextColor(0xFF00E5FF);

                // Interactive Zoom Stepper Dark Theme
                View widgetZoom = root.findViewById(R.id.widget_grid_zoom_stepper);
                if (widgetZoom != null) {
                    GradientDrawable wzGd = new GradientDrawable();
                    wzGd.setColor(0xFF161B22);
                    wzGd.setStroke(dpToPx(1), 0xFF30363D);
                    wzGd.setCornerRadius(dpToPx(16));
                    widgetZoom.setBackground(wzGd);
                }
                ImageView ivZoom = root.findViewById(R.id.icon_grid_zoom);
                if (ivZoom != null) ivZoom.setColorFilter(lightText);
                TextView tvZoomLabel = root.findViewById(R.id.text_grid_zoom_label);
                if (tvZoomLabel != null) tvZoomLabel.setTextColor(lightText);

                View capsuleZoom = root.findViewById(R.id.capsule_grid_zoom_stepper);
                if (capsuleZoom != null) {
                    GradientDrawable czGd = new GradientDrawable();
                    czGd.setColor(0xFF0D1117);
                    czGd.setStroke(dpToPx(1), 0xFF30363D);
                    czGd.setCornerRadius(dpToPx(14));
                    capsuleZoom.setBackground(czGd);
                }
                TextView btnZoomM = root.findViewById(R.id.btn_grid_zoom_minus);
                if (btnZoomM != null) btnZoomM.setTextColor(0xFF00E5FF);
                TextView tvZoomVal = root.findViewById(R.id.text_grid_zoom_val);
                if (tvZoomVal != null) tvZoomVal.setTextColor(0xFF00E5FF);
                TextView btnZoomP = root.findViewById(R.id.btn_grid_zoom_plus);
                if (btnZoomP != null) btnZoomP.setTextColor(0xFF00E5FF);
                ImageView btnZoomR = root.findViewById(R.id.btn_grid_zoom_reset);
                if (btnZoomR != null) btnZoomR.setColorFilter(0xFF00E5FF);

                View capsuleWg = root.findViewById(R.id.waveguard_status_capsule);
                if (capsuleWg != null) capsuleWg.setBackgroundResource(R.drawable.bg_stitch_waveguard_pill);

                TextView wgTitle = root.findViewById(R.id.waveguard_capsule_title);
                if (wgTitle != null) wgTitle.setTextColor(lightText);
                TextView wgCount = root.findViewById(R.id.waveguard_capsule_count);
                if (wgCount != null) wgCount.setTextColor(0xFF10B981);

                ImageButton btnExit = root.findViewById(R.id.btn_action_exit_app);
                if (btnExit != null) {
                    btnExit.setBackgroundResource(R.drawable.bg_stitch_round_button);
                    btnExit.setColorFilter(lightText);
                }
                ImageButton btnClose = root.findViewById(R.id.btn_action_close_grid);
                if (btnClose != null) {
                    btnClose.setBackgroundResource(R.drawable.bg_stitch_round_button);
                    btnClose.setColorFilter(lightText);
                }
            }

            TabItem currentTab = getActiveOrDominantTab();
            boolean isWgOn = waveguardShield != null && waveguardShield.isGlobalEnabled();
            if (isWgOn && currentTab != null && currentTab.url != null) {
                try {
                    String host = Uri.parse(currentTab.url).getHost();
                    if (host != null && waveguardShield.isSiteWhitelisted(host)) {
                        isWgOn = false;
                    }
                } catch (Exception ignored) {}
            }
            updateWaveguardVisuals(root, isWgOn, isDark);
        } catch (Throwable ignored) {}
    }

    private void updateWaveguardVisuals(View root, boolean isEnabled, boolean isDark) {
        if (root == null) return;
        try {
            TabItem currentTab = getActiveOrDominantTab();
            int tabBlocks = (currentTab != null && waveguardShield != null) ? waveguardShield.getBlockedCountForTab(currentTab.id) : 0;

            View sqShield = root.findViewById(R.id.squircle_shield);
            ImageView ivShield = root.findViewById(R.id.icon_shield);
            TextView tvShield = root.findViewById(R.id.text_shield);
            View capsuleWg = root.findViewById(R.id.waveguard_status_capsule);
            ImageView ivCapsuleWg = root.findViewById(R.id.icon_waveguard_capsule);
            TextView wgTitle = root.findViewById(R.id.waveguard_capsule_title);
            TextView wgCount = root.findViewById(R.id.waveguard_capsule_count);

            int deepDarkLabel = 0xFF1E293B;
            int lightText = 0xFFDFE2F0;

            if (isEnabled) {
                // Waveguard ON: Active Green Visuals
                if (!isDark) {
                    if (sqShield != null) {
                        GradientDrawable sqshGd = new GradientDrawable();
                        sqshGd.setColor(0xFFECFDF5);
                        sqshGd.setStroke(dpToPx(1), 0xFFA7F3D0);
                        sqshGd.setCornerRadius(dpToPx(16));
                        sqShield.setBackground(sqshGd);
                    }
                    if (ivShield != null) ivShield.setColorFilter(0xFF059669);
                    if (tvShield != null) {
                        tvShield.setText("Waveguard");
                        tvShield.setTextColor(0xFF059669);
                    }
                    if (capsuleWg != null) {
                        GradientDrawable cwgGd = new GradientDrawable();
                        cwgGd.setColor(0xFFECFDF5);
                        cwgGd.setStroke(dpToPx(1), 0xFFA7F3D0);
                        cwgGd.setCornerRadius(dpToPx(18));
                        capsuleWg.setBackground(cwgGd);
                    }
                    if (ivCapsuleWg != null) ivCapsuleWg.setColorFilter(0xFF059669);
                    if (wgTitle != null) wgTitle.setTextColor(deepDarkLabel);
                    if (wgCount != null) {
                        wgCount.setText(tabBlocks + " blocked");
                        wgCount.setTextColor(0xFF059669);
                    }
                } else {
                    if (sqShield != null) {
                        GradientDrawable sqshGd = new GradientDrawable();
                        sqshGd.setColor(0xFF0B291B);
                        sqshGd.setStroke(dpToPx(1), 0xFF10B981);
                        sqshGd.setCornerRadius(dpToPx(16));
                        sqShield.setBackground(sqshGd);
                    }
                    if (ivShield != null) ivShield.setColorFilter(0xFF10B981);
                    if (tvShield != null) {
                        tvShield.setText("Waveguard");
                        tvShield.setTextColor(0xFF10B981);
                    }
                    if (capsuleWg != null) {
                        capsuleWg.setBackgroundResource(R.drawable.bg_stitch_waveguard_pill);
                    }
                    if (ivCapsuleWg != null) ivCapsuleWg.setColorFilter(0xFF10B981);
                    if (wgTitle != null) wgTitle.setTextColor(lightText);
                    if (wgCount != null) {
                        wgCount.setText(tabBlocks + " blocked");
                        wgCount.setTextColor(0xFF10B981);
                    }
                }
            } else {
                // Waveguard OFF: Turn OFF the green color on both waveguard button and bottom bar!
                if (!isDark) {
                    if (sqShield != null) {
                        GradientDrawable sqshGd = new GradientDrawable();
                        sqshGd.setColor(0xFFF1F5F9);
                        sqshGd.setStroke(dpToPx(1), 0xFFE2E8F0);
                        sqshGd.setCornerRadius(dpToPx(16));
                        sqShield.setBackground(sqshGd);
                    }
                    if (ivShield != null) ivShield.setColorFilter(0xFF64748B);
                    if (tvShield != null) {
                        tvShield.setText("Waveguard");
                        tvShield.setTextColor(0xFF64748B);
                    }
                    if (capsuleWg != null) {
                        GradientDrawable cwgGd = new GradientDrawable();
                        cwgGd.setColor(0xFFF1F5F9);
                        cwgGd.setStroke(dpToPx(1), 0xFFCBD5E1);
                        cwgGd.setCornerRadius(dpToPx(18));
                        capsuleWg.setBackground(cwgGd);
                    }
                    if (ivCapsuleWg != null) ivCapsuleWg.setColorFilter(0xFF64748B);
                    if (wgTitle != null) wgTitle.setTextColor(0xFF64748B);
                    if (wgCount != null) {
                        wgCount.setText("Paused");
                        wgCount.setTextColor(0xFF64748B);
                    }
                } else {
                    if (sqShield != null) {
                        sqShield.setBackgroundResource(R.drawable.bg_stitch_squircle);
                    }
                    if (ivShield != null) ivShield.setColorFilter(0xFF64748B);
                    if (tvShield != null) {
                        tvShield.setText("Waveguard");
                        tvShield.setTextColor(0xFF64748B);
                    }
                    if (capsuleWg != null) {
                        GradientDrawable cwgGd = new GradientDrawable();
                        cwgGd.setColor(0xFF161B22);
                        cwgGd.setStroke(dpToPx(1), 0xFF30363D);
                        cwgGd.setCornerRadius(dpToPx(18));
                        capsuleWg.setBackground(cwgGd);
                    }
                    if (ivCapsuleWg != null) ivCapsuleWg.setColorFilter(0xFF64748B);
                    if (wgTitle != null) wgTitle.setTextColor(0xFF64748B);
                    if (wgCount != null) {
                        wgCount.setText("Paused");
                        wgCount.setTextColor(0xFF64748B);
                    }
                }
            }
        } catch (Throwable ignored) {}
    }

    public void updateOmniboxScrimBackground() {
        if (omniboxHeaderWrapper == null) return;
        boolean isBottom = "bottom".equalsIgnoreCase(omniboxPosition);
        TabItem currentTab = getActiveOrDominantTab();
        boolean isIncognito = currentTab != null && currentTab.isIncognito;

        GradientDrawable.Orientation orientation = isBottom
                ? GradientDrawable.Orientation.BOTTOM_TOP
                : GradientDrawable.Orientation.TOP_BOTTOM;

        int solidBase;
        int fadeHigh;
        int fadeMid;
        int fadeLow;
        int fadeZero;

        if (isIncognito) {
            solidBase = 0xFF1A0B2E;
            fadeHigh  = 0xEE1A0B2E;
            fadeMid   = 0x881A0B2E;
            fadeLow   = 0x221A0B2E;
            fadeZero  = 0x001A0B2E;
        } else if (!isDarkTheme) {
            solidBase = 0xFFFFFFFF;
            fadeHigh  = 0xEEFFFFFF;
            fadeMid   = 0x88FFFFFF;
            fadeLow   = 0x22FFFFFF;
            fadeZero  = 0x00FFFFFF;
        } else {
            solidBase = 0xFF0D1117;
            fadeHigh  = 0xEE0D1117;
            fadeMid   = 0x880D1117;
            fadeLow   = 0x220D1117;
            fadeZero  = 0x000D1117;
        }

        // Multi-stop gradient with solid base directly behind the capsule and feathered soft fade into the web UI (no hard outline)
        GradientDrawable scrim = new GradientDrawable(orientation, new int[]{
                solidBase, solidBase, fadeHigh, fadeMid, fadeLow, fadeZero
        });
        omniboxHeaderWrapper.setBackground(scrim);
        omniboxHeaderWrapper.setClickable(true);
        omniboxHeaderWrapper.setFocusable(true);
    }

    private static final int CARD_GRID_PAGE_CAPACITY = 10;

    private static final List<String> DEFAULT_P1_GRID_KEYS = Arrays.asList(
            "night_mode", "pdf", "dual_ai", "new_tab", "desktop_site",
            "downloads", "find", "shield", "split", "settings"
    );
    private static final List<String> DEFAULT_P2_GRID_KEYS = Arrays.asList(
            "bookmarks", "view_bookmarks", "history", "incognito", "share", "print", "clear_data", "edit_layout"
    );

    public void resetCardGridLayout() {
        runOnUiThread(() -> {
            try {
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                        .edit()
                        .putInt("action_grid_layout_version", 2)
                        .remove("action_grid_pages_json")
                        .remove("action_grid_p1_keys")
                        .remove("action_grid_p2_keys")
                        .apply();
                Toast.makeText(this, "Card grid menu reset to default", Toast.LENGTH_SHORT).show();
            } catch (Throwable ignored) {}
        });
    }

    private void cascadeOverfill(List<List<String>> pages, int startPage) {
        if (pages == null) return;
        for (int p = startPage; p < pages.size(); p++) {
            while (pages.get(p).size() > CARD_GRID_PAGE_CAPACITY) {
                String overflow = pages.get(p).remove(pages.get(p).size() - 1);
                if (p + 1 < pages.size()) {
                    pages.get(p + 1).add(0, overflow);
                } else {
                    List<String> newPage = new ArrayList<>();
                    newPage.add(overflow);
                    pages.add(newPage);
                    break;
                }
            }
        }
    }

    private void pruneEmptyPages(List<List<String>> pages) {
        if (pages == null) return;
        while (pages.size() > 2 && pages.get(pages.size() - 1).isEmpty()) {
            pages.remove(pages.size() - 1);
        }
        while (pages.size() < 2) {
            pages.add(new ArrayList<>());
        }
    }

    private List<List<String>> getCardGridAllPages() {
        int layoutVersion = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getInt("action_grid_layout_version", 0);
        if (layoutVersion < 2) {
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                    .putInt("action_grid_layout_version", 2)
                    .remove("action_grid_pages_json")
                    .remove("action_grid_p1_keys")
                    .remove("action_grid_p2_keys")
                    .apply();
            List<List<String>> defaultPages = new ArrayList<>();
            defaultPages.add(new ArrayList<>(DEFAULT_P1_GRID_KEYS));
            defaultPages.add(new ArrayList<>(DEFAULT_P2_GRID_KEYS));
            saveCardGridPages(defaultPages);
            return defaultPages;
        }

        String json = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getString("action_grid_pages_json", null);
        if (json != null && !json.trim().isEmpty()) {
            try {
                org.json.JSONArray arr = new org.json.JSONArray(json);
                List<List<String>> pages = new ArrayList<>();
                for (int i = 0; i < arr.length(); i++) {
                    org.json.JSONArray pArr = arr.getJSONArray(i);
                    List<String> page = new ArrayList<>();
                    for (int j = 0; j < pArr.length(); j++) {
                        String k = pArr.getString(j);
                        if (k != null && !k.trim().isEmpty()) {
                            page.add(k.trim());
                        }
                    }
                    pages.add(page);
                }
                pruneEmptyPages(pages);

                Set<String> existing = new HashSet<>();
                for (List<String> p : pages) existing.addAll(p);
                for (String k : DEFAULT_P1_GRID_KEYS) {
                    if (!existing.contains(k)) {
                        pages.get(pages.size() - 1).add(k);
                        existing.add(k);
                    }
                }
                for (String k : DEFAULT_P2_GRID_KEYS) {
                    if (!existing.contains(k)) {
                        pages.get(pages.size() - 1).add(k);
                        existing.add(k);
                    }
                }
                cascadeOverfill(pages, 0);
                Set<String> seenKeys = new HashSet<>();
                for (List<String> p : pages) {
                    java.util.Iterator<String> it = p.iterator();
                    while (it.hasNext()) {
                        String k = it.next();
                        if (seenKeys.contains(k)) {
                            it.remove();
                        } else {
                            seenKeys.add(k);
                        }
                    }
                }
                pruneEmptyPages(pages);
                return pages;
            } catch (Exception ignored) {}
        }

        // Fallback to legacy keys or default
        List<List<String>> defaultPages = new ArrayList<>();
        defaultPages.add(new ArrayList<>(DEFAULT_P1_GRID_KEYS));
        defaultPages.add(new ArrayList<>(DEFAULT_P2_GRID_KEYS));
        return defaultPages;
    }

    private void saveCardGridPages(List<List<String>> pages) {
        if (pages == null) return;
        pruneEmptyPages(pages);
        try {
            org.json.JSONArray rootArr = new org.json.JSONArray();
            for (List<String> p : pages) {
                org.json.JSONArray pArr = new org.json.JSONArray();
                for (String k : p) pArr.put(k);
                rootArr.put(pArr);
            }
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                    .edit()
                    .putString("action_grid_pages_json", rootArr.toString())
                    .apply();
            if (pages.size() >= 1) {
                saveCardGridKeys(pages.get(0), pages.size() >= 2 ? pages.get(1) : new ArrayList<>());
            }
        } catch (Throwable ignored) {}
    }

    private List<String> getCardGridPage1Keys() {
        String p1Raw = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getString("action_grid_p1_keys", null);
        if (p1Raw != null && !p1Raw.trim().isEmpty()) {
            String[] parts = p1Raw.split(",");
            List<String> list = new ArrayList<>();
            for (String s : parts) {
                String trimmed = s.trim();
                if (!trimmed.isEmpty() && !list.contains(trimmed)) list.add(trimmed);
            }
            if (!list.isEmpty()) return list;
        }
        return new ArrayList<>(DEFAULT_P1_GRID_KEYS);
    }

    private List<String> getCardGridPage2Keys() {
        String p2Raw = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getString("action_grid_p2_keys", null);
        if (p2Raw != null && !p2Raw.trim().isEmpty()) {
            String[] parts = p2Raw.split(",");
            List<String> list = new ArrayList<>();
            for (String s : parts) {
                String trimmed = s.trim();
                if (!trimmed.isEmpty() && !list.contains(trimmed)) list.add(trimmed);
            }
            if (!list.isEmpty()) return list;
        }
        return new ArrayList<>(DEFAULT_P2_GRID_KEYS);
    }

    private void saveCardGridKeys(List<String> page1, List<String> page2) {
        StringBuilder sb1 = new StringBuilder();
        if (page1 != null) {
            for (int i = 0; i < page1.size(); i++) {
                if (i > 0) sb1.append(",");
                sb1.append(page1.get(i));
            }
        }
        StringBuilder sb2 = new StringBuilder();
        if (page2 != null) {
            for (int i = 0; i < page2.size(); i++) {
                if (i > 0) sb2.append(",");
                sb2.append(page2.get(i));
            }
        }
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putString("action_grid_p1_keys", sb1.toString())
                .putString("action_grid_p2_keys", sb2.toString())
                .apply();
    }

    private void populateGridTable(TableLayout table, List<String> keys, Map<String, View> tileMap) {
        if (table == null || keys == null || tileMap == null) return;
        table.setStretchAllColumns(true);
        int columnsPerRow = 5;
        TableRow currentRow = null;
        int colCount = 0;

        for (int i = 0; i < keys.size(); i++) {
            if (i % columnsPerRow == 0) {
                currentRow = new TableRow(this);
                TableLayout.LayoutParams trLp = new TableLayout.LayoutParams(
                        TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
                trLp.bottomMargin = dpToPx(12);
                currentRow.setLayoutParams(trLp);
                table.addView(currentRow);
                colCount = 0;
            }
            String key = keys.get(i);
            View tile = tileMap.get(key);
            if (tile != null && currentRow != null) {
                try {
                    if (tile.getParent() instanceof ViewGroup) {
                        ((ViewGroup) tile.getParent()).removeView(tile);
                    }
                    TableRow.LayoutParams tLp = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
                    tile.setLayoutParams(tLp);
                    currentRow.addView(tile);
                    colCount++;
                } catch (Throwable t) {
                    android.util.Log.e("MainActivity", "Error adding grid tile: " + key, t);
                }
            }
        }
        if (currentRow != null && colCount > 0 && colCount < columnsPerRow) {
            for (int p = colCount; p < columnsPerRow; p++) {
                try {
                    View dummy = new View(this);
                    TableRow.LayoutParams dLp = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
                    dummy.setLayoutParams(dLp);
                    currentRow.addView(dummy);
                } catch (Throwable ignored) {}
            }
        }
    }

    private int getCardGridItemIconRes(String key) {
        if (key == null) return R.drawable.ic_pod_settings;
        switch (key) {
            case "night_mode": return R.drawable.ic_menu_moon;
            case "desktop_site": return R.drawable.ic_browser_desktop;
            case "bookmarks": return R.drawable.ic_menu_bookmark;
            case "view_bookmarks": return R.drawable.ic_menu_bookmark;
            case "history": return R.drawable.ic_menu_history;
            case "downloads": return R.drawable.ic_menu_download;
            case "incognito": return R.drawable.ic_menu_incognito;
            case "find": return R.drawable.ic_pod_search;
            case "share": return R.drawable.ic_menu_share;
            case "split": return R.drawable.ic_browser_split;
            case "settings": return R.drawable.ic_pod_settings;
            case "new_tab": return R.drawable.ic_browser_tabs;
            case "dual_ai": return R.drawable.ic_arena_plus;
            case "pdf": return R.drawable.ic_menu_pdf;
            case "print": return R.drawable.ic_menu_print;
            case "shield": return R.drawable.ic_browser_shield;
            case "clear_data": return R.drawable.ic_pod_close;
            case "edit_layout": return R.drawable.ic_menu_edit;
            default: return R.drawable.ic_pod_settings;
        }
    }

    private String getCardGridItemCleanLabel(String key) {
        if (key == null) return "Unknown";
        switch (key) {
            case "night_mode": return "Night mode";
            case "desktop_site": return "Desktop";
            case "bookmarks": return "Save Bookmark";
            case "view_bookmarks": return "Bookmarks";
            case "history": return "History";
            case "downloads": return "Downloads";
            case "incognito": return "Incognito";
            case "find": return "Find";
            case "share": return "Share";
            case "split": return "Dual Split";
            case "settings": return "Settings";
            case "new_tab": return "New tab";
            case "dual_ai": return "Dual AI";
            case "pdf": return "Open PDF";
            case "print": return "Print";
            case "shield": return "Waveguard";
            case "clear_data": return "Clear Data";
            case "edit_layout": return "Edit Menu";
            default: return key;
        }
    }

    private String getCardGridItemDisplayName(String key) {
        if (key == null) return "Unknown";
        switch (key) {
            case "night_mode": return "🌙 Night mode";
            case "desktop_site": return "💻 Desktop site";
            case "bookmarks": return "💾 Save Bookmark";
            case "view_bookmarks": return "🔖 Bookmarks";
            case "history": return "🕒 History";
            case "downloads": return "⬇️ Downloads";
            case "incognito": return "🕶️ Incognito";
            case "find": return "🔍 Find in page";
            case "share": return "🔗 Share";
            case "split": return "🪟 Dual Split";
            case "settings": return "⚙️ Settings";
            case "new_tab": return "➕ New tab";
            case "dual_ai": return "✨ Dual AI";
            case "pdf": return "📄 Open PDF";
            case "print": return "🖨️ Print & Export";
            case "shield": return "🛡️ Waveguard";
            case "clear_data": return "🗑️ Clear Data";
            case "edit_layout": return "✏️ Edit Menu";
            default: return key;
        }
    }

    public void showCardGridEditDialog() {
        com.google.android.material.bottomsheet.BottomSheetDialog editDialog =
                new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_card_grid_edit, null);
        editDialog.setContentView(dialogView);

        if (editDialog.getWindow() != null) {
            View bs = editDialog.getWindow().findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bs != null) bs.setBackgroundResource(android.R.color.transparent);
        }

        editDialog.setOnShowListener(dialogInterface -> {
            com.google.android.material.bottomsheet.BottomSheetDialog d = (com.google.android.material.bottomsheet.BottomSheetDialog) dialogInterface;
            FrameLayout bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                com.google.android.material.bottomsheet.BottomSheetBehavior<FrameLayout> behavior =
                        com.google.android.material.bottomsheet.BottomSheetBehavior.from(bottomSheet);
                ViewGroup.LayoutParams lp = bottomSheet.getLayoutParams();
                if (lp != null) {
                    lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    bottomSheet.setLayoutParams(lp);
                }
                behavior.setFitToContents(true);
                behavior.setSkipCollapsed(true);
                behavior.setState(com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        final List<List<String>> pages = getCardGridAllPages();
        final String[] selectedKey = new String[]{null};
        final int[] selectedPage = new int[]{-1};
        final int[] selectedIdx = new int[]{-1};

        LinearLayout containerPages = dialogView.findViewById(R.id.container_edit_pages);
        LinearLayout selectedActionBar = dialogView.findViewById(R.id.container_selected_action_bar);
        TextView btnAddPage = dialogView.findViewById(R.id.btn_edit_add_page);
        TextView btnReset = dialogView.findViewById(R.id.btn_edit_reset);
        TextView btnDone = dialogView.findViewById(R.id.btn_edit_done);

        if (!isDarkTheme) {
            GradientDrawable rootGd = new GradientDrawable();
            rootGd.setColor(0xFFFFFFFF);
            rootGd.setCornerRadii(new float[]{dpToPx(24), dpToPx(24), dpToPx(24), dpToPx(24), 0, 0, 0, 0});
            dialogView.setBackground(rootGd);

            View handle = dialogView.findViewById(R.id.edit_grid_drag_handle);
            if (handle != null) {
                GradientDrawable hGd = new GradientDrawable();
                hGd.setColor(0xFFCBD5E1);
                hGd.setCornerRadius(dpToPx(3));
                handle.setBackground(hGd);
            }
            TextView title = dialogView.findViewById(R.id.text_edit_title);
            if (title != null) title.setTextColor(0xFF0284C7);

            if (btnAddPage != null) {
                GradientDrawable addGd = new GradientDrawable();
                addGd.setColor(0xFFF1F5F9);
                addGd.setStroke(dpToPx(1), 0xFFCBD5E1);
                addGd.setCornerRadius(dpToPx(10));
                btnAddPage.setBackground(addGd);
                btnAddPage.setTextColor(0xFF0284C7);
            }

            if (btnReset != null) btnReset.setTextColor(0xFF64748B);
            if (btnDone != null) {
                GradientDrawable doneGd = new GradientDrawable();
                doneGd.setColor(0xFFE0F2FE);
                doneGd.setStroke(dpToPx(1), 0xFF0284C7);
                doneGd.setCornerRadius(dpToPx(12));
                btnDone.setBackground(doneGd);
                btnDone.setTextColor(0xFF0284C7);
            }
        }

        final Runnable[] renderUi = new Runnable[1];
        renderUi[0] = () -> {
            if (selectedActionBar != null) {
                if (selectedKey[0] == null) {
                    selectedActionBar.setVisibility(View.GONE);
                    selectedActionBar.removeAllViews();
                } else {
                    selectedActionBar.setVisibility(View.VISIBLE);
                    selectedActionBar.removeAllViews();

                    // Style action bar banner
                    GradientDrawable abGd = new GradientDrawable();
                    abGd.setColor(!isDarkTheme ? 0xFFE0F2FE : 0xFF00384D);
                    abGd.setStroke(dpToPx(1), !isDarkTheme ? 0xFF0284C7 : 0xFF00E5FF);
                    abGd.setCornerRadius(dpToPx(14));
                    selectedActionBar.setBackground(abGd);

                    // Top row: Info + Cancel button
                    RelativeLayout topRow = new RelativeLayout(this);
                    topRow.setLayoutParams(new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                    TextView tvSelInfo = new TextView(this);
                    tvSelInfo.setText("Selected: " + getCardGridItemDisplayName(selectedKey[0]) + "  (Page " + (selectedPage[0] + 1) + ")");
                    tvSelInfo.setTextColor(!isDarkTheme ? 0xFF0369A1 : 0xFF00E5FF);
                    tvSelInfo.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
                    tvSelInfo.setTypeface(Typeface.DEFAULT_BOLD);
                    RelativeLayout.LayoutParams infoLp = new RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    infoLp.addRule(RelativeLayout.ALIGN_PARENT_START);
                    infoLp.addRule(RelativeLayout.CENTER_VERTICAL);
                    tvSelInfo.setLayoutParams(infoLp);
                    topRow.addView(tvSelInfo);

                    TextView btnCancelSel = new TextView(this);
                    btnCancelSel.setText("✕ Cancel");
                    btnCancelSel.setTextColor(!isDarkTheme ? 0xFF64748B : 0xFF94A3B8);
                    btnCancelSel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
                    btnCancelSel.setPadding(dpToPx(6), dpToPx(2), dpToPx(6), dpToPx(2));
                    RelativeLayout.LayoutParams cancelLp = new RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    cancelLp.addRule(RelativeLayout.ALIGN_PARENT_END);
                    cancelLp.addRule(RelativeLayout.CENTER_VERTICAL);
                    btnCancelSel.setLayoutParams(cancelLp);
                    btnCancelSel.setOnClickListener(v -> {
                        selectedKey[0] = null;
                        selectedPage[0] = -1;
                        selectedIdx[0] = -1;
                        playUiFeedbackSound("tap");
                        renderUi[0].run();
                    });
                    topRow.addView(btnCancelSel);

                    selectedActionBar.addView(topRow);

                    // Bottom row: Scrollable action chips
                    HorizontalScrollView hsv = new HorizontalScrollView(this);
                    hsv.setHorizontalScrollBarEnabled(false);
                    LinearLayout.LayoutParams hsvLp = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    hsvLp.topMargin = dpToPx(8);
                    hsv.setLayoutParams(hsvLp);

                    LinearLayout chipContainer = new LinearLayout(this);
                    chipContainer.setOrientation(LinearLayout.HORIZONTAL);
                    chipContainer.setGravity(Gravity.CENTER_VERTICAL);

                    TextView lblMove = new TextView(this);
                    lblMove.setText("Move to: ");
                    lblMove.setTextColor(!isDarkTheme ? 0xFF475569 : 0xFFCBD5E1);
                    lblMove.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
                    lblMove.setTypeface(Typeface.DEFAULT_BOLD);
                    chipContainer.addView(lblMove);

                    for (int p = 0; p < pages.size(); p++) {
                        if (p != selectedPage[0]) {
                            final int targetP = p;
                            TextView chip = new TextView(this);
                            chip.setText("➔ Page " + (targetP + 1));
                            chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
                            chip.setTypeface(Typeface.DEFAULT_BOLD);
                            chip.setTextColor(!isDarkTheme ? 0xFF0284C7 : 0xFF38BDF8);
                            chip.setPadding(dpToPx(10), dpToPx(5), dpToPx(10), dpToPx(5));

                            GradientDrawable cGd = new GradientDrawable();
                            cGd.setColor(!isDarkTheme ? 0xFFFFFFFF : 0xFF161B22);
                            cGd.setStroke(dpToPx(1), !isDarkTheme ? 0xFFBAE6FD : 0xFF38BDF8);
                            cGd.setCornerRadius(dpToPx(10));
                            chip.setBackground(cGd);

                            LinearLayout.LayoutParams cLp = new LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            cLp.setMarginEnd(dpToPx(8));
                            chip.setLayoutParams(cLp);

                            chip.setOnClickListener(v -> {
                                String keyToMove = selectedKey[0];
                                int srcP = selectedPage[0];
                                int srcI = selectedIdx[0];
                                if (srcP >= 0 && srcP < pages.size() && srcI >= 0 && srcI < pages.get(srcP).size()) {
                                    pages.get(srcP).remove(srcI);
                                    pages.get(targetP).add(keyToMove);
                                    cascadeOverfill(pages, targetP);
                                    pruneEmptyPages(pages);
                                }
                                selectedKey[0] = null;
                                selectedPage[0] = -1;
                                selectedIdx[0] = -1;
                                playUiFeedbackSound("tap");
                                renderUi[0].run();
                            });
                            chipContainer.addView(chip);
                        }
                    }

                    // New Page chip
                    TextView chipNew = new TextView(this);
                    chipNew.setText("➕ New Page");
                    chipNew.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
                    chipNew.setTypeface(Typeface.DEFAULT_BOLD);
                    chipNew.setTextColor(!isDarkTheme ? 0xFF059669 : 0xFF10B981);
                    chipNew.setPadding(dpToPx(10), dpToPx(5), dpToPx(10), dpToPx(5));

                    GradientDrawable nGd = new GradientDrawable();
                    nGd.setColor(!isDarkTheme ? 0xFFECFDF5 : 0xFF0B291B);
                    nGd.setStroke(dpToPx(1), !isDarkTheme ? 0xFFA7F3D0 : 0xFF10B981);
                    nGd.setCornerRadius(dpToPx(10));
                    chipNew.setBackground(nGd);

                    LinearLayout.LayoutParams nLp = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    nLp.setMarginEnd(dpToPx(8));
                    chipNew.setLayoutParams(nLp);

                    chipNew.setOnClickListener(v -> {
                        String keyToMove = selectedKey[0];
                        int srcP = selectedPage[0];
                        int srcI = selectedIdx[0];
                        if (srcP >= 0 && srcP < pages.size() && srcI >= 0 && srcI < pages.get(srcP).size()) {
                            pages.get(srcP).remove(srcI);
                            List<String> newP = new ArrayList<>();
                            newP.add(keyToMove);
                            pages.add(newP);
                            cascadeOverfill(pages, pages.size() - 1);
                            pruneEmptyPages(pages);
                        }
                        selectedKey[0] = null;
                        selectedPage[0] = -1;
                        selectedIdx[0] = -1;
                        playUiFeedbackSound("tap");
                        renderUi[0].run();
                    });
                    chipContainer.addView(chipNew);

                    // Reorder within page: Left / Right
                    if (selectedIdx[0] > 0) {
                        TextView chipLeft = new TextView(this);
                        chipLeft.setText("◀ Left");
                        chipLeft.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
                        chipLeft.setTextColor(!isDarkTheme ? 0xFF0F172A : 0xFFDFE2F0);
                        chipLeft.setPadding(dpToPx(8), dpToPx(5), dpToPx(8), dpToPx(5));
                        GradientDrawable lGd = new GradientDrawable();
                        lGd.setColor(!isDarkTheme ? 0xFFF1F5F9 : 0xFF21262D);
                        lGd.setCornerRadius(dpToPx(10));
                        chipLeft.setBackground(lGd);
                        LinearLayout.LayoutParams lLp = new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        lLp.setMarginEnd(dpToPx(8));
                        chipLeft.setLayoutParams(lLp);
                        chipLeft.setOnClickListener(v -> {
                            int p = selectedPage[0];
                            int idx = selectedIdx[0];
                            Collections.swap(pages.get(p), idx, idx - 1);
                            selectedIdx[0] = idx - 1;
                            playUiFeedbackSound("tap");
                            renderUi[0].run();
                        });
                        chipContainer.addView(chipLeft);
                    }

                    if (selectedPage[0] >= 0 && selectedPage[0] < pages.size() && selectedIdx[0] < pages.get(selectedPage[0]).size() - 1) {
                        TextView chipRight = new TextView(this);
                        chipRight.setText("▶ Right");
                        chipRight.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
                        chipRight.setTextColor(!isDarkTheme ? 0xFF0F172A : 0xFFDFE2F0);
                        chipRight.setPadding(dpToPx(8), dpToPx(5), dpToPx(8), dpToPx(5));
                        GradientDrawable rGd = new GradientDrawable();
                        rGd.setColor(!isDarkTheme ? 0xFFF1F5F9 : 0xFF21262D);
                        rGd.setCornerRadius(dpToPx(10));
                        chipRight.setBackground(rGd);
                        LinearLayout.LayoutParams rLp = new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        chipRight.setLayoutParams(rLp);
                        chipRight.setOnClickListener(v -> {
                            int p = selectedPage[0];
                            int idx = selectedIdx[0];
                            Collections.swap(pages.get(p), idx, idx + 1);
                            selectedIdx[0] = idx + 1;
                            playUiFeedbackSound("tap");
                            renderUi[0].run();
                        });
                        chipContainer.addView(chipRight);
                    }

                    hsv.addView(chipContainer);
                    selectedActionBar.addView(hsv);
                }
            }

            if (containerPages != null) {
                final boolean[] dragHandled = new boolean[]{false};
                containerPages.removeAllViews();
                for (int p = 0; p < pages.size(); p++) {
                    final int pageIndex = p;
                    List<String> pageItems = pages.get(pageIndex);

                    LinearLayout pageSection = new LinearLayout(this);
                    pageSection.setOrientation(LinearLayout.VERTICAL);
                    LinearLayout.LayoutParams psLp = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    psLp.bottomMargin = dpToPx(16);
                    pageSection.setLayoutParams(psLp);
                    pageSection.setPadding(dpToPx(12), dpToPx(10), dpToPx(12), dpToPx(10));

                    GradientDrawable secGd = new GradientDrawable();
                    secGd.setColor(!isDarkTheme ? 0xFFF8FAFC : 0xFF161B22);
                    secGd.setStroke(dpToPx(1), !isDarkTheme ? 0xFFE2E8F0 : 0xFF21262D);
                    secGd.setCornerRadius(dpToPx(16));
                    pageSection.setBackground(secGd);

                    // Drag and Drop target support on page section background
                    pageSection.setOnDragListener((v, event) -> {
                        switch (event.getAction()) {
                            case DragEvent.ACTION_DRAG_STARTED:
                                dragHandled[0] = false;
                                return true;
                            case DragEvent.ACTION_DRAG_ENTERED:
                                GradientDrawable dragEnterGd = new GradientDrawable();
                                dragEnterGd.setColor(!isDarkTheme ? 0xFFE0F2FE : 0xFF0D2838);
                                dragEnterGd.setStroke(dpToPx(2), !isDarkTheme ? 0xFF0284C7 : 0xFF00E5FF);
                                dragEnterGd.setCornerRadius(dpToPx(16));
                                pageSection.setBackground(dragEnterGd);
                                return true;
                            case DragEvent.ACTION_DRAG_EXITED:
                            case DragEvent.ACTION_DRAG_ENDED:
                                pageSection.setBackground(secGd);
                                return true;
                            case DragEvent.ACTION_DROP:
                                pageSection.setBackground(secGd);
                                if (dragHandled[0]) {
                                    dragHandled[0] = false;
                                    return true;
                                }
                                ClipData clipData = event.getClipData();
                                if (clipData != null && clipData.getItemCount() > 0) {
                                    String raw = clipData.getItemAt(0).getText().toString();
                                    String[] parts = raw.split(":");
                                    if (parts.length >= 3) {
                                        try {
                                            int srcP = Integer.parseInt(parts[0]);
                                            int srcIdx = Integer.parseInt(parts[1]);
                                            String itemKey = parts[2];
                                            if (srcP == pageIndex) {
                                                if (srcIdx < pages.get(srcP).size()) {
                                                    pages.get(srcP).remove(srcIdx);
                                                    pages.get(pageIndex).add(itemKey);
                                                    playUiFeedbackSound("tap");
                                                    selectedKey[0] = null;
                                                    selectedPage[0] = -1;
                                                    selectedIdx[0] = -1;
                                                    renderUi[0].run();
                                                }
                                            } else {
                                                if (srcP < pages.size() && srcIdx < pages.get(srcP).size()) {
                                                    pages.get(srcP).remove(srcIdx);
                                                    pages.get(pageIndex).add(itemKey);
                                                    cascadeOverfill(pages, pageIndex);
                                                    pruneEmptyPages(pages);
                                                    selectedKey[0] = null;
                                                    selectedPage[0] = -1;
                                                    selectedIdx[0] = -1;
                                                    playUiFeedbackSound("tap");
                                                    renderUi[0].run();
                                                }
                                            }
                                        } catch (Exception ignored) {}
                                    }
                                }
                                return true;
                        }
                        return true;
                    });

                    // Page Header (Title + Count badge + Optional Delete empty page)
                    RelativeLayout header = new RelativeLayout(this);
                    header.setLayoutParams(new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                    TextView tvTitle = new TextView(this);
                    tvTitle.setText("PAGE " + (pageIndex + 1));
                    tvTitle.setTextColor(!isDarkTheme ? 0xFF0284C7 : 0xFF38BDF8);
                    tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
                    tvTitle.setTypeface(Typeface.DEFAULT_BOLD);
                    tvTitle.setLetterSpacing(0.06f);
                    RelativeLayout.LayoutParams tLp = new RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    tLp.addRule(RelativeLayout.ALIGN_PARENT_START);
                    tLp.addRule(RelativeLayout.CENTER_VERTICAL);
                    tvTitle.setLayoutParams(tLp);
                    header.addView(tvTitle);

                    LinearLayout rightHeader = new LinearLayout(this);
                    rightHeader.setOrientation(LinearLayout.HORIZONTAL);
                    rightHeader.setGravity(Gravity.CENTER_VERTICAL);
                    RelativeLayout.LayoutParams rhLp = new RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    rhLp.addRule(RelativeLayout.ALIGN_PARENT_END);
                    rhLp.addRule(RelativeLayout.CENTER_VERTICAL);
                    rightHeader.setLayoutParams(rhLp);

                    TextView tvCount = new TextView(this);
                    tvCount.setText(pageItems.size() + "/10");
                    tvCount.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
                    tvCount.setTypeface(Typeface.DEFAULT_BOLD);
                    tvCount.setTextColor(pageItems.size() >= 10 ? 0xFF10B981 : (!isDarkTheme ? 0xFF64748B : 0xFF94A3B8));
                    rightHeader.addView(tvCount);

                    if (pages.size() > 2 && pageItems.isEmpty()) {
                        TextView btnDel = new TextView(this);
                        btnDel.setText("✕ Remove");
                        btnDel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
                        btnDel.setTextColor(0xFFEF4444);
                        btnDel.setPadding(dpToPx(8), 0, 0, 0);
                        btnDel.setOnClickListener(v -> {
                            pages.remove(pageIndex);
                            pruneEmptyPages(pages);
                            selectedKey[0] = null;
                            playUiFeedbackSound("tap");
                            renderUi[0].run();
                        });
                        rightHeader.addView(btnDel);
                    }
                    header.addView(rightHeader);
                    pageSection.addView(header);

                    // Items Grid (5 columns per row)
                    TableLayout table = new TableLayout(this);
                    TableLayout.LayoutParams tblLp = new TableLayout.LayoutParams(
                            TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
                    tblLp.topMargin = dpToPx(10);
                    table.setLayoutParams(tblLp);
                    table.setStretchAllColumns(true);

                    int colCount = 0;
                    TableRow currentRow = null;
                    for (int i = 0; i < pageItems.size(); i++) {
                        if (i % 5 == 0) {
                            currentRow = new TableRow(this);
                            TableLayout.LayoutParams trLp = new TableLayout.LayoutParams(
                                    TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
                            trLp.bottomMargin = dpToPx(8);
                            currentRow.setLayoutParams(trLp);
                            table.addView(currentRow);
                            colCount = 0;
                        }

                        final int itemIdx = i;
                        final String key = pageItems.get(i);
                        boolean isSelected = key.equals(selectedKey[0]);

                        LinearLayout itemCard = new LinearLayout(this);
                        itemCard.setOrientation(LinearLayout.VERTICAL);
                        itemCard.setGravity(Gravity.CENTER);
                        TableRow.LayoutParams cLp = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
                        itemCard.setLayoutParams(cLp);
                        itemCard.setPadding(dpToPx(2), dpToPx(4), dpToPx(2), dpToPx(4));

                        FrameLayout squircle = new FrameLayout(this);
                        LinearLayout.LayoutParams sqLp = new LinearLayout.LayoutParams(dpToPx(44), dpToPx(44));
                        squircle.setLayoutParams(sqLp);

                        GradientDrawable sqGd = new GradientDrawable();
                        if (isSelected) {
                            sqGd.setColor(!isDarkTheme ? 0xFFE0F2FE : 0xFF00384D);
                            sqGd.setStroke(dpToPx(2), !isDarkTheme ? 0xFF0284C7 : 0xFF00E5FF);
                        } else {
                            sqGd.setColor(!isDarkTheme ? 0xFFFFFFFF : 0xFF0D1117);
                            sqGd.setStroke(dpToPx(1), !isDarkTheme ? 0xFFE2E8F0 : 0xFF30363D);
                        }
                        sqGd.setCornerRadius(dpToPx(14));
                        squircle.setBackground(sqGd);

                        ImageView icon = new ImageView(this);
                        FrameLayout.LayoutParams iLp = new FrameLayout.LayoutParams(dpToPx(20), dpToPx(20), Gravity.CENTER);
                        icon.setLayoutParams(iLp);
                        icon.setImageResource(getCardGridItemIconRes(key));
                        icon.setColorFilter(isSelected ? (!isDarkTheme ? 0xFF0284C7 : 0xFF00E5FF) : (!isDarkTheme ? 0xFF0F172A : 0xFFDFE2F0));
                        squircle.addView(icon);
                        itemCard.addView(squircle);

                        TextView label = new TextView(this);
                        LinearLayout.LayoutParams lblLp = new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        lblLp.topMargin = dpToPx(4);
                        label.setLayoutParams(lblLp);
                        label.setText(getCardGridItemCleanLabel(key));
                        label.setTextColor(isSelected ? (!isDarkTheme ? 0xFF0284C7 : 0xFF00E5FF) : (!isDarkTheme ? 0xFF334155 : 0xFF94A3B8));
                        label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9.5f);
                        label.setSingleLine(true);
                        label.setEllipsize(android.text.TextUtils.TruncateAt.END);
                        itemCard.addView(label);

                        // Tap to select / deselect
                        itemCard.setOnClickListener(v -> {
                            playUiFeedbackSound("tap");
                            if (key.equals(selectedKey[0])) {
                                selectedKey[0] = null;
                                selectedPage[0] = -1;
                                selectedIdx[0] = -1;
                            } else {
                                selectedKey[0] = key;
                                selectedPage[0] = pageIndex;
                                selectedIdx[0] = itemIdx;
                            }
                            renderUi[0].run();
                        });

                        // Long press to Drag & Drop
                        itemCard.setOnLongClickListener(v -> {
                            playUiFeedbackSound("tap");
                            ClipData.Item clipItem = new ClipData.Item(pageIndex + ":" + itemIdx + ":" + key);
                            ClipData dragData = new ClipData("CARD_GRID_ITEM", new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN}, clipItem);
                            View.DragShadowBuilder shadow = new View.DragShadowBuilder(squircle);
                            v.setAlpha(0.35f);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                v.startDragAndDrop(dragData, shadow, null, 0);
                            } else {
                                v.startDrag(dragData, shadow, null, 0);
                            }
                            return true;
                        });

                        // Target Drag Listener for Intra-page and Inter-page precise tile drop
                        itemCard.setOnDragListener((v, event) -> {
                            switch (event.getAction()) {
                                case DragEvent.ACTION_DRAG_STARTED:
                                    return true;
                                case DragEvent.ACTION_DRAG_ENTERED: {
                                    GradientDrawable hoverGd = new GradientDrawable();
                                    hoverGd.setColor(!isDarkTheme ? 0xFFBAE6FD : 0xFF00384D);
                                    hoverGd.setStroke(dpToPx(2), !isDarkTheme ? 0xFF0284C7 : 0xFF00E5FF);
                                    hoverGd.setCornerRadius(dpToPx(14));
                                    squircle.setBackground(hoverGd);
                                    squircle.animate().scaleX(1.15f).scaleY(1.15f).setDuration(150).start();
                                    return true;
                                }
                                case DragEvent.ACTION_DRAG_EXITED: {
                                    squircle.setBackground(sqGd);
                                    squircle.animate().scaleX(1.0f).scaleY(1.0f).setDuration(150).start();
                                    return true;
                                }
                                case DragEvent.ACTION_DRAG_ENDED: {
                                    squircle.setBackground(sqGd);
                                    squircle.setScaleX(1.0f);
                                    squircle.setScaleY(1.0f);
                                    itemCard.setAlpha(1.0f);
                                    return true;
                                }
                                case DragEvent.ACTION_DROP: {
                                    squircle.setBackground(sqGd);
                                    squircle.setScaleX(1.0f);
                                    squircle.setScaleY(1.0f);
                                    ClipData clipData = event.getClipData();
                                    if (clipData != null && clipData.getItemCount() > 0) {
                                        String raw = clipData.getItemAt(0).getText().toString();
                                        String[] parts = raw.split(":");
                                        if (parts.length >= 3) {
                                            try {
                                                int srcP = Integer.parseInt(parts[0]);
                                                int srcIdx = Integer.parseInt(parts[1]);
                                                String itemKey = parts[2];
                                                dragHandled[0] = true;

                                                if (srcP == pageIndex) {
                                                    // Intra-page reordering within same page!
                                                    if (srcIdx != itemIdx && srcIdx < pages.get(srcP).size()) {
                                                        pages.get(srcP).remove(srcIdx);
                                                        int insertIdx = itemIdx;
                                                        if (srcIdx < itemIdx) {
                                                            insertIdx = Math.min(itemIdx, pages.get(srcP).size());
                                                        }
                                                        pages.get(srcP).add(insertIdx, itemKey);
                                                        playUiFeedbackSound("tap");
                                                        selectedKey[0] = null;
                                                        selectedPage[0] = -1;
                                                        selectedIdx[0] = -1;
                                                        renderUi[0].run();
                                                    }
                                                } else {
                                                    // Cross-page insertion at specific index!
                                                    if (srcP < pages.size() && srcIdx < pages.get(srcP).size()) {
                                                        pages.get(srcP).remove(srcIdx);
                                                        int insertIdx = Math.min(itemIdx, pages.get(pageIndex).size());
                                                        pages.get(pageIndex).add(insertIdx, itemKey);
                                                        cascadeOverfill(pages, pageIndex);
                                                        pruneEmptyPages(pages);
                                                        playUiFeedbackSound("tap");
                                                        selectedKey[0] = null;
                                                        selectedPage[0] = -1;
                                                        selectedIdx[0] = -1;
                                                        renderUi[0].run();
                                                    }
                                                }
                                            } catch (Exception ignored) {}
                                        }
                                    }
                                    return true;
                                }
                            }
                            return true;
                        });

                        if (currentRow != null) {
                            currentRow.addView(itemCard);
                            colCount++;
                        }
                    }

                    // Fill remaining slots in last row with dummy views to preserve 5-col alignment
                    if (currentRow != null && colCount > 0 && colCount < 5) {
                        for (int k = colCount; k < 5; k++) {
                            View dummy = new View(this);
                            TableRow.LayoutParams dLp = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
                            dummy.setLayoutParams(dLp);

                            // Dummy slot can also accept drag drop to place at the end of the page
                            dummy.setOnDragListener((v, event) -> {
                                switch (event.getAction()) {
                                    case DragEvent.ACTION_DRAG_STARTED:
                                        return true;
                                    case DragEvent.ACTION_DRAG_ENTERED: {
                                        GradientDrawable dEnter = new GradientDrawable();
                                        dEnter.setColor(!isDarkTheme ? 0xFFE0F2FE : 0xFF0D2838);
                                        dEnter.setStroke(dpToPx(1), !isDarkTheme ? 0xFF0284C7 : 0xFF00E5FF);
                                        dEnter.setCornerRadius(dpToPx(14));
                                        dummy.setBackground(dEnter);
                                        return true;
                                    }
                                    case DragEvent.ACTION_DRAG_EXITED:
                                    case DragEvent.ACTION_DRAG_ENDED: {
                                        dummy.setBackground(null);
                                        return true;
                                    }
                                    case DragEvent.ACTION_DROP: {
                                        dummy.setBackground(null);
                                        ClipData clipData = event.getClipData();
                                        if (clipData != null && clipData.getItemCount() > 0) {
                                            String raw = clipData.getItemAt(0).getText().toString();
                                            String[] parts = raw.split(":");
                                            if (parts.length >= 3) {
                                                try {
                                                    int srcP = Integer.parseInt(parts[0]);
                                                    int srcIdx = Integer.parseInt(parts[1]);
                                                    String itemKey = parts[2];
                                                    dragHandled[0] = true;
                                                    if (srcP == pageIndex) {
                                                        if (srcIdx < pages.get(srcP).size()) {
                                                            pages.get(srcP).remove(srcIdx);
                                                            pages.get(pageIndex).add(itemKey);
                                                            playUiFeedbackSound("tap");
                                                            selectedKey[0] = null;
                                                            selectedPage[0] = -1;
                                                            selectedIdx[0] = -1;
                                                            renderUi[0].run();
                                                        }
                                                    } else {
                                                        if (srcP < pages.size() && srcIdx < pages.get(srcP).size()) {
                                                            pages.get(srcP).remove(srcIdx);
                                                            pages.get(pageIndex).add(itemKey);
                                                            cascadeOverfill(pages, pageIndex);
                                                            pruneEmptyPages(pages);
                                                            playUiFeedbackSound("tap");
                                                            selectedKey[0] = null;
                                                            selectedPage[0] = -1;
                                                            selectedIdx[0] = -1;
                                                            renderUi[0].run();
                                                        }
                                                    }
                                                } catch (Exception ignored) {}
                                            }
                                        }
                                        return true;
                                    }
                                }
                                return true;
                            });

                            currentRow.addView(dummy);
                        }
                    }

                    pageSection.addView(table);
                    containerPages.addView(pageSection);
                }
            }
        };

        renderUi[0].run();

        if (btnAddPage != null) {
            btnAddPage.setOnClickListener(v -> {
                pages.add(new ArrayList<>());
                playUiFeedbackSound("tap");
                Toast.makeText(this, "Page " + pages.size() + " added", Toast.LENGTH_SHORT).show();
                renderUi[0].run();
            });
        }

        if (btnReset != null) {
            btnReset.setOnClickListener(v -> {
                pages.clear();
                pages.add(new ArrayList<>(DEFAULT_P1_GRID_KEYS));
                pages.add(new ArrayList<>(DEFAULT_P2_GRID_KEYS));
                selectedKey[0] = null;
                selectedPage[0] = -1;
                selectedIdx[0] = -1;
                playUiFeedbackSound("tap");
                Toast.makeText(this, "Layout reset to default", Toast.LENGTH_SHORT).show();
                renderUi[0].run();
            });
        }

        if (btnDone != null) {
            btnDone.setOnClickListener(v -> {
                pruneEmptyPages(pages);
                saveCardGridPages(pages);
                editDialog.dismiss();
                playUiFeedbackSound("tap");
                Toast.makeText(this, "Menu customized!", Toast.LENGTH_SHORT).show();
                showBrowserActionGrid();
            });
        }

        editDialog.show();
    }

    public void applyOmniboxPosition(String position) {
        runOnUiThread(() -> {
            try {
                omniboxPosition = (position != null && position.equalsIgnoreCase("bottom")) ? "bottom" : "top";
                boolean isBottom = "bottom".equalsIgnoreCase(omniboxPosition);

                if (omniboxHeaderWrapper != null) {
                    FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) omniboxHeaderWrapper.getLayoutParams();
                    if (lp == null) {
                        lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    }
                    lp.gravity = isBottom ? Gravity.BOTTOM : Gravity.TOP;
                    omniboxHeaderWrapper.setLayoutParams(lp);
                    omniboxHeaderWrapper.setPadding(
                            dpToPx(10),
                            dpToPx(isBottom ? 8 : 6),
                            dpToPx(10),
                            dpToPx(isBottom ? 12 : 6)
                    );
                }

                if (webviewsParentContainer != null) {
                    FrameLayout.LayoutParams wpLp = (FrameLayout.LayoutParams) webviewsParentContainer.getLayoutParams();
                    if (wpLp == null) {
                        wpLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    }
                    if (isBottom) {
                        wpLp.bottomMargin = dpToPx(100);
                        wpLp.topMargin = 0;
                    } else {
                        wpLp.topMargin = dpToPx(100);
                        wpLp.bottomMargin = 0;
                    }
                    webviewsParentContainer.setLayoutParams(wpLp);
                }

                if (browserProgressBar != null) {
                    FrameLayout.LayoutParams pbLp = (FrameLayout.LayoutParams) browserProgressBar.getLayoutParams();
                    if (pbLp == null) {
                        pbLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(3));
                    }
                    pbLp.gravity = isBottom ? Gravity.BOTTOM : Gravity.TOP;
                    if (isBottom) {
                        pbLp.bottomMargin = dpToPx(100);
                        pbLp.topMargin = 0;
                    } else {
                        pbLp.topMargin = dpToPx(100);
                        pbLp.bottomMargin = 0;
                    }
                    browserProgressBar.setLayoutParams(pbLp);
                }

                if (omniboxSuggestionsContainer != null) {
                    FrameLayout.LayoutParams sugLp = (FrameLayout.LayoutParams) omniboxSuggestionsContainer.getLayoutParams();
                    if (sugLp == null) {
                        sugLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    }
                    sugLp.gravity = isBottom ? Gravity.BOTTOM : Gravity.TOP;
                    if (isBottom) {
                        sugLp.bottomMargin = dpToPx(100);
                        sugLp.topMargin = 0;
                    } else {
                        sugLp.topMargin = dpToPx(100);
                        sugLp.bottomMargin = 0;
                    }
                    omniboxSuggestionsContainer.setLayoutParams(sugLp);
                }

                updateOmniboxScrimBackground();
            } catch (Throwable ignored) {}
        });
    }

    public void setOmniboxPosition(String position) {
        this.omniboxPosition = (position != null && position.equalsIgnoreCase("bottom")) ? "bottom" : "top";
        try {
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                    .edit()
                    .putString("omnibox_position", this.omniboxPosition)
                    .apply();
        } catch (Throwable ignored) {}
        applyOmniboxPosition(this.omniboxPosition);
    }

    public String getOmniboxPosition() {
        return this.omniboxPosition;
    }

    public void setOmniboxMenuStyle(String style) {
        this.omniboxMenuStyle = (style != null && style.equalsIgnoreCase("grid")) ? "grid" : "list";
        try {
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                    .edit()
                    .putString("omnibox_menu_style", this.omniboxMenuStyle)
                    .apply();
        } catch (Throwable ignored) {}
    }

    public String getOmniboxMenuStyle() {
        return this.omniboxMenuStyle;
    }

    public void setInterfaceDensity(String density) {
        String d = (density != null && (density.equalsIgnoreCase("compact") || density.equalsIgnoreCase("spacious"))) ? density.toLowerCase() : "default";
        try {
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                    .edit()
                    .putString("interface_density", d)
                    .apply();
        } catch (Throwable ignored) {}
        if (controlWebView != null) {
            controlWebView.evaluateJavascript("document.documentElement.setAttribute('data-density', '" + d + "');", null);
        }
    }

    public String getInterfaceDensity() {
        try {
            return getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                    .getString("interface_density", "default");
        } catch (Throwable ignored) {
            return "default";
        }
    }

    public void launchDualAIAsk() {
        playUiFeedbackSound("tap");
        int gptId = nextTabId++;
        TabItem gptTab = createNewTabInstance(gptId, "https://chatgpt.com", "chatgpt", null, false);
        gptTab.title = "ChatGPT";
        tabsList.add(gptTab);

        int geminiId = nextTabId++;
        TabItem geminiTab = createNewTabInstance(geminiId, "https://gemini.google.com/app", "gemini", null, false);
        geminiTab.title = "Gemini";
        tabsList.add(geminiTab);

        activeTabId = gptTab.id;
        secondarySplitTabId = geminiTab.id;
        splitModeState = 1;
        splitRatio = 0.5f;
        applySplitViewLayout();
        updateOmniboxState();
        updateOmniboxTabStrip();
        saveOpenTabsState();

        findViewById(android.R.id.content).postDelayed(() -> {
            toggleSplitArenaBroadcast(true);
        }, 300);
        Toast.makeText(this, "⚡ Dual AI Active!", Toast.LENGTH_SHORT).show();
    }

    public void showBookmarksDialog() {
        showBookmarksDialog(0);
    }

    public void showBookmarksDialog(int enterDirection) {
        playUiFeedbackSound("tap");
        if (bookmarkManager == null) bookmarkManager = new BookmarkManager(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_bookmarks, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(isDarkTheme ? 0xFF0A0E17 : 0xFFF8FAFC));
            dialog.getWindow().setWindowAnimations(0);
        }
        dialogView.setBackgroundColor(isDarkTheme ? 0xFF0A0E17 : 0xFFF8FAFC);

        // Single iOS Slide Entrance Transition
        if (enterDirection > 0) {
            dialogView.setTranslationX(dpToPx(80));
            dialogView.setAlpha(0.3f);
            dialogView.animate().translationX(0f).alpha(1f).setDuration(200).setInterpolator(new DecelerateInterpolator(1.6f)).start();
        } else if (enterDirection < 0) {
            dialogView.setTranslationX(-dpToPx(80));
            dialogView.setAlpha(0.3f);
            dialogView.animate().translationX(0f).alpha(1f).setDuration(200).setInterpolator(new DecelerateInterpolator(1.6f)).start();
        }

        // Header Views
        View bookmarksRoot = dialogView.findViewById(R.id.bookmarks_root);
        View headerBar = dialogView.findViewById(R.id.bookmarks_header_bar);
        ImageButton btnBack = dialogView.findViewById(R.id.btn_bookmarks_back);
        View capsuleCenter = dialogView.findViewById(R.id.capsule_bookmarks_center);
        ImageView iconBookmarksHeader = dialogView.findViewById(R.id.icon_bookmarks_header);
        TextView textBookmarksHeader = dialogView.findViewById(R.id.text_bookmarks_header);
        TextView badgeHeaderCount = dialogView.findViewById(R.id.badge_bookmarks_header_count);
        TextView btnGotoHistory = dialogView.findViewById(R.id.btn_bookmarks_goto_history);
        TextView btnGotoSettings = dialogView.findViewById(R.id.btn_bookmarks_goto_settings);

        // Content & Toolbar Views
        TextView textVaultTitle = dialogView.findViewById(R.id.text_vault_title);
        TextView badgeVaultSaved = dialogView.findViewById(R.id.badge_vault_saved);
        TextView btnBatchToggle = dialogView.findViewById(R.id.btn_bookmarks_batch_toggle);
        TextView btnBatchDelete = dialogView.findViewById(R.id.btn_bookmarks_batch_delete);



        View searchBar = dialogView.findViewById(R.id.bookmarks_search_bar);
        ImageView iconSearch = dialogView.findViewById(R.id.icon_bookmarks_search);
        EditText searchInput = dialogView.findViewById(R.id.bookmarks_search_input);
        View btnSort = dialogView.findViewById(R.id.btn_bookmarks_sort);
        TextView textSortLabel = dialogView.findViewById(R.id.text_bookmarks_sort_label);

        // Filter Chips
        TextView chipAll = dialogView.findViewById(R.id.chip_filter_all);
        TextView chipFav = dialogView.findViewById(R.id.chip_filter_favorites);
        LinearLayout layoutChips = dialogView.findViewById(R.id.layout_filter_chips);

        LinearLayout listContainer = dialogView.findViewById(R.id.bookmarks_list_container);
        View emptyView = dialogView.findViewById(R.id.layout_bookmarks_empty);
        TextView textEmpty = dialogView.findViewById(R.id.text_bookmarks_empty);

        // Bottom Action Dock
        View bottomDock = dialogView.findViewById(R.id.bookmarks_bottom_dock);
        View btnDockExport = dialogView.findViewById(R.id.btn_dock_export);
        View btnDockAdd = dialogView.findViewById(R.id.btn_dock_add_folder);
        View btnDockImport = dialogView.findViewById(R.id.btn_dock_import);
        TextView textDockExport = dialogView.findViewById(R.id.text_dock_export);
        TextView textDockImport = dialogView.findViewById(R.id.text_dock_import);
        ImageView iconDockExport = dialogView.findViewById(R.id.icon_dock_export);
        ImageView iconDockImport = dialogView.findViewById(R.id.icon_dock_import);

        // Apply Theme Backgrounds and Colors
        if (bookmarksRoot != null) bookmarksRoot.setBackgroundColor(isDarkTheme ? 0xFF0A0E17 : 0xFFF8FAFC);
        if (headerBar != null) headerBar.setBackgroundColor(isDarkTheme ? 0xFF0A0E17 : 0xFFF8FAFC);
        if (btnBack != null) btnBack.setColorFilter(isDarkTheme ? 0xFFBAC9CC : 0xFF334155);

        // Match Top Header Styling with History Dialog
        if (capsuleCenter != null) {
            GradientDrawable capBg = new GradientDrawable();
            capBg.setCornerRadius(dpToPx(16));
            capBg.setColor(isDarkTheme ? 0xFF1E2838 : 0xFFF1F5F9);
            capBg.setStroke(dpToPx(1), isDarkTheme ? 0xFF00E5FF : 0xFFCBD5E1);
            capsuleCenter.setBackground(capBg);
        }
        if (textBookmarksHeader != null) textBookmarksHeader.setTextColor(isDarkTheme ? 0xFFFFFFFF : 0xFF0284C7);
        if (iconBookmarksHeader != null) iconBookmarksHeader.setColorFilter(isDarkTheme ? 0xFF00E5FF : 0xFF0284C7);

        if (badgeHeaderCount != null) {
            GradientDrawable bBg = new GradientDrawable();
            bBg.setCornerRadius(dpToPx(10));
            bBg.setColor(isDarkTheme ? 0xFF161B22 : 0xFFE2E8F0);
            bBg.setStroke(dpToPx(1), isDarkTheme ? 0xFF21262D : 0xFFCBD5E1);
            badgeHeaderCount.setBackground(bBg);
            badgeHeaderCount.setTextColor(isDarkTheme ? 0xFFCBD5E1 : 0xFF0F172A);
        }

        if (btnGotoHistory != null) btnGotoHistory.setTextColor(isDarkTheme ? 0xFF64748B : 0xFF475569);
        if (btnGotoSettings != null) btnGotoSettings.setTextColor(isDarkTheme ? 0xFF64748B : 0xFF475569);

        if (textVaultTitle != null) textVaultTitle.setTextColor(isDarkTheme ? 0xFFDFE2F0 : 0xFF0F172A);
        if (badgeVaultSaved != null) {
            GradientDrawable vBg = new GradientDrawable();
            vBg.setCornerRadius(dpToPx(8));
            vBg.setColor(isDarkTheme ? 0xFF142232 : 0xFFE0F2FE);
            badgeVaultSaved.setBackground(vBg);
            badgeVaultSaved.setTextColor(isDarkTheme ? 0xFF00E5FF : 0xFF0284C7);
        }

        if (btnBatchToggle != null) {
            GradientDrawable btBg = new GradientDrawable();
            btBg.setCornerRadius(dpToPx(12));
            btBg.setColor(isDarkTheme ? 0xFF161B24 : 0xFFFFFFFF);
            btBg.setStroke(dpToPx(1), isDarkTheme ? 0xFF232B3E : 0xFFCBD5E1);
            btnBatchToggle.setBackground(btBg);
            btnBatchToggle.setTextColor(isDarkTheme ? 0xFFBAC9CC : 0xFF334155);
        }



        if (searchBar != null) {
            GradientDrawable sbBg = new GradientDrawable();
            sbBg.setCornerRadius(dpToPx(14));
            sbBg.setColor(isDarkTheme ? 0xFF141926 : 0xFFFFFFFF);
            sbBg.setStroke(dpToPx(1), isDarkTheme ? 0xFF1F293D : 0xFFE2E8F0);
            searchBar.setBackground(sbBg);
        }
        if (iconSearch != null) iconSearch.setColorFilter(isDarkTheme ? 0xFF849396 : 0xFF94A3B8);
        if (searchInput != null) {
            searchInput.setTextColor(isDarkTheme ? 0xFFDFE2F0 : 0xFF0F172A);
            searchInput.setHintTextColor(isDarkTheme ? 0xFF64748B : 0xFF94A3B8);
        }

        if (btnSort != null) {
            GradientDrawable sBg = new GradientDrawable();
            sBg.setCornerRadius(dpToPx(8));
            sBg.setColor(isDarkTheme ? 0xFF1E2838 : 0xFFF1F5F9);
            btnSort.setBackground(sBg);
        }
        if (textSortLabel != null) textSortLabel.setTextColor(isDarkTheme ? 0xFF00E5FF : 0xFF0284C7);

        // Bottom Dock Styling
        if (bottomDock != null) {
            GradientDrawable dockBg = new GradientDrawable();
            dockBg.setCornerRadius(dpToPx(26));
            dockBg.setColor(isDarkTheme ? 0xFF141926 : 0xFFFFFFFF);
            dockBg.setStroke(dpToPx(1), isDarkTheme ? 0xFF232B3E : 0xFFE2E8F0);
            bottomDock.setBackground(dockBg);
            bottomDock.setElevation(dpToPx(16));
        }
        if (textDockExport != null) textDockExport.setTextColor(isDarkTheme ? 0xFFDFE2F0 : 0xFF0F172A);
        if (textDockImport != null) textDockImport.setTextColor(isDarkTheme ? 0xFFDFE2F0 : 0xFF0F172A);
        if (iconDockExport != null) iconDockExport.setColorFilter(isDarkTheme ? 0xFF00E5FF : 0xFF0284C7);
        if (iconDockImport != null) iconDockImport.setColorFilter(isDarkTheme ? 0xFF00E5FF : 0xFF0284C7);

        if (btnDockAdd != null) {
            GradientDrawable plusBg = new GradientDrawable();
            plusBg.setShape(GradientDrawable.OVAL);
            plusBg.setColor(0xFF00E5FF);
            btnDockAdd.setBackground(plusBg);
        }

        // State variables
        final boolean[] isSelectMode = {false};
        final Set<String> selectedBookmarkIds = new HashSet<>();
        final String[] activeCategory = {"all"}; // "all", "favorites", or custom folder name
        final String[] activeSort = {"newest"}; // "newest", "oldest", "az", "za"

        // References for dynamic refresh
        final Runnable[] refreshRef = new Runnable[1];
        final Runnable[] renderChipsRef = new Runnable[1];

        // Dynamic Group Chips Renderer
        renderChipsRef[0] = () -> {
            if (layoutChips == null) return;
            while (layoutChips.getChildCount() > 2) {
                layoutChips.removeViewAt(2);
            }

            // Style static chips (All, Favorites)
            boolean isAllAct = "all".equalsIgnoreCase(activeCategory[0]);
            GradientDrawable allBg = new GradientDrawable();
            allBg.setCornerRadius(dpToPx(14));
            allBg.setColor(isAllAct ? (isDarkTheme ? 0xFF00E5FF : 0xFF0F172A) : (isDarkTheme ? 0xFF161B24 : 0xFFFFFFFF));
            if (!isAllAct) allBg.setStroke(dpToPx(1), isDarkTheme ? 0xFF232B3E : 0xFFE2E8F0);
            if (chipAll != null) {
                chipAll.setBackground(allBg);
                chipAll.setTextColor(isAllAct ? (isDarkTheme ? 0xFF000000 : 0xFFFFFFFF) : (isDarkTheme ? 0xFFCBD5E1 : 0xFF475569));
            }

            boolean isFavAct = "favorites".equalsIgnoreCase(activeCategory[0]);
            GradientDrawable favBg = new GradientDrawable();
            favBg.setCornerRadius(dpToPx(14));
            favBg.setColor(isFavAct ? (isDarkTheme ? 0xFF00E5FF : 0xFF0F172A) : (isDarkTheme ? 0xFF161B24 : 0xFFFFFFFF));
            if (!isFavAct) favBg.setStroke(dpToPx(1), isDarkTheme ? 0xFF232B3E : 0xFFE2E8F0);
            if (chipFav != null) {
                chipFav.setBackground(favBg);
                chipFav.setTextColor(isFavAct ? (isDarkTheme ? 0xFF000000 : 0xFFFFFFFF) : (isDarkTheme ? 0xFFCBD5E1 : 0xFF475569));
            }

            // Dynamic Custom Groups Chips
            List<String> folders = bookmarkManager.getFolders();
            for (String folder : folders) {
                if ("Favorites".equalsIgnoreCase(folder) || "All".equalsIgnoreCase(folder) || "Default".equalsIgnoreCase(folder)) {
                    continue;
                }
                TextView chip = new TextView(this);
                chip.setText(folder.toUpperCase(Locale.ROOT));
                chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
                chip.setTypeface(null, Typeface.BOLD);
                chip.setGravity(Gravity.CENTER);
                chip.setPadding(dpToPx(12), 0, dpToPx(12), 0);
                chip.setClickable(true);
                chip.setFocusable(true);

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, dpToPx(28));
                lp.setMarginStart(dpToPx(6));
                chip.setLayoutParams(lp);

                boolean isAct = folder.equalsIgnoreCase(activeCategory[0]);
                GradientDrawable cBg = new GradientDrawable();
                cBg.setCornerRadius(dpToPx(14));
                cBg.setColor(isAct ? (isDarkTheme ? 0xFF00E5FF : 0xFF0F172A) : (isDarkTheme ? 0xFF161B24 : 0xFFFFFFFF));
                if (!isAct) cBg.setStroke(dpToPx(1), isDarkTheme ? 0xFF232B3E : 0xFFE2E8F0);
                chip.setBackground(cBg);
                chip.setTextColor(isAct ? (isDarkTheme ? 0xFF000000 : 0xFFFFFFFF) : (isDarkTheme ? 0xFFCBD5E1 : 0xFF475569));

                chip.setOnClickListener(v -> {
                    playUiFeedbackSound("tap");
                    activeCategory[0] = folder;
                    renderChipsRef[0].run();
                    if (refreshRef[0] != null) refreshRef[0].run();
                });

                // Long-press: Rename or Delete Bookmark Group
                chip.setOnLongClickListener(v -> {
                    playUiFeedbackSound("tap");
                    PopupMenu groupMenu = new PopupMenu(this, chip);
                    groupMenu.getMenu().add(0, 1, 0, "✏️ Rename Group");
                    groupMenu.getMenu().add(0, 2, 1, "🗑️ Delete Group");
                    groupMenu.setOnMenuItemClickListener(item -> {
                        if (item.getItemId() == 1) {
                            showRenameGroupDialog(folder, () -> {
                                activeCategory[0] = "all";
                                renderChipsRef[0].run();
                                if (refreshRef[0] != null) refreshRef[0].run();
                            });
                            return true;
                        } else if (item.getItemId() == 2) {
                            showDeleteGroupDialog(folder, () -> {
                                activeCategory[0] = "all";
                                renderChipsRef[0].run();
                                if (refreshRef[0] != null) refreshRef[0].run();
                            });
                            return true;
                        }
                        return false;
                    });
                    groupMenu.show();
                    return true;
                });

                layoutChips.addView(chip);
            }
        };

        // Static chip click listeners
        if (chipAll != null) {
            chipAll.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                activeCategory[0] = "all";
                renderChipsRef[0].run();
                if (refreshRef[0] != null) refreshRef[0].run();
            });
        }
        if (chipFav != null) {
            chipFav.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                activeCategory[0] = "favorites";
                renderChipsRef[0].run();
                if (refreshRef[0] != null) refreshRef[0].run();
            });
        }

        // Header Navigation Listeners with Smooth Slide Exit Animations
        if (btnBack != null) btnBack.setOnClickListener(v -> dialog.dismiss());
        if (btnGotoHistory != null) {
            btnGotoHistory.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                dialog.dismiss();
                showHistoryDialog(1);
            });
        }
        if (btnGotoSettings != null) {
            btnGotoSettings.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                dialog.dismiss();
                openControlSheet();
            });
        }

        // Bookmark List Refresh Logic
        refreshRef[0] = () -> {
            if (listContainer == null) return;
            listContainer.removeAllViews();
            List<BookmarkManager.BookmarkItem> bms = bookmarkManager.getAllBookmarks();

            // Total count badges
            if (badgeHeaderCount != null) badgeHeaderCount.setText(String.valueOf(bms.size()));
            if (badgeVaultSaved != null) badgeVaultSaved.setText(bms.size() + " SAVED");
            if (chipAll != null) chipAll.setText("ALL (" + bms.size() + ")");

            // Filter
            String query = searchInput != null ? searchInput.getText().toString().trim().toLowerCase() : "";
            List<BookmarkManager.BookmarkItem> filtered = new ArrayList<>();
            for (BookmarkManager.BookmarkItem b : bms) {
                if (activeCategory[0].equalsIgnoreCase("favorites") && !b.isFavorite) continue;
                if (!activeCategory[0].equalsIgnoreCase("all") && !activeCategory[0].equalsIgnoreCase("favorites")) {
                    if (b.folder == null || !b.folder.equalsIgnoreCase(activeCategory[0])) continue;
                }

                if (!query.isEmpty()) {
                    boolean match = (b.title != null && b.title.toLowerCase().contains(query))
                            || (b.url != null && b.url.toLowerCase().contains(query))
                            || (b.folder != null && b.folder.toLowerCase().contains(query));
                    if (!match) continue;
                }
                filtered.add(b);
            }

            // Sort
            if ("newest".equals(activeSort[0])) {
                Collections.sort(filtered, (a, b) -> Long.compare(b.timestamp, a.timestamp));
            } else if ("oldest".equals(activeSort[0])) {
                Collections.sort(filtered, (a, b) -> Long.compare(a.timestamp, b.timestamp));
            } else if ("az".equals(activeSort[0])) {
                Collections.sort(filtered, (a, b) -> a.title.compareToIgnoreCase(b.title));
            } else if ("za".equals(activeSort[0])) {
                Collections.sort(filtered, (a, b) -> b.title.compareToIgnoreCase(a.title));
            }

            if (emptyView != null) emptyView.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);

            // Render each bookmark using item_stitch_bookmark_entry.xml
            for (BookmarkManager.BookmarkItem item : filtered) {
                View card = getLayoutInflater().inflate(R.layout.item_stitch_bookmark_entry, listContainer, false);
                LinearLayout itemRoot = card.findViewById(R.id.bookmark_item_root);
                android.widget.CheckBox itemCheck = card.findViewById(R.id.bookmark_item_checkbox);
                FrameLayout iconFrame = card.findViewById(R.id.bookmark_item_icon_frame);
                TextView iconEmoji = card.findViewById(R.id.bookmark_item_emoji);
                TextView titleView = card.findViewById(R.id.bookmark_item_title);
                TextView tagView = card.findViewById(R.id.bookmark_item_tag);
                TextView domainView = card.findViewById(R.id.bookmark_item_domain);
                TextView timeView = card.findViewById(R.id.bookmark_item_time);
                ImageButton starBtn = card.findViewById(R.id.bookmark_item_star);
                ImageButton moreBtn = card.findViewById(R.id.bookmark_item_more);

                // Card Background
                if (itemRoot != null) {
                    GradientDrawable cBg = new GradientDrawable();
                    cBg.setCornerRadius(dpToPx(18));
                    cBg.setColor(isDarkTheme ? 0xFF141926 : 0xFFFFFFFF);
                    cBg.setStroke(dpToPx(1), isDarkTheme ? 0xFF1F293D : 0xFFE2E8F0);
                    itemRoot.setBackground(cBg);
                }

                // Batch Selection Checkbox
                if (itemCheck != null) {
                    itemCheck.setVisibility(isSelectMode[0] ? View.VISIBLE : View.GONE);
                    itemCheck.setChecked(selectedBookmarkIds.contains(item.id));
                    itemCheck.setOnCheckedChangeListener((cb, checked) -> {
                        if (checked) selectedBookmarkIds.add(item.id);
                        else selectedBookmarkIds.remove(item.id);
                        if (btnBatchDelete != null) {
                            btnBatchDelete.setVisibility(!selectedBookmarkIds.isEmpty() ? View.VISIBLE : View.GONE);
                            btnBatchDelete.setText("Delete (" + selectedBookmarkIds.size() + ")");
                        }
                    });
                }

                // Title & Tag
                if (titleView != null) {
                    titleView.setText(item.title);
                    titleView.setTextColor(isDarkTheme ? 0xFFF1F5F9 : 0xFF0F172A);
                }
                if (tagView != null) {
                    if (item.folder != null && !item.folder.isEmpty() && !"Default".equalsIgnoreCase(item.folder)) {
                        tagView.setVisibility(View.VISIBLE);
                        String tagShort = item.folder.contains("&") ? item.folder.split("&")[0].trim() : item.folder;
                        if (tagShort.length() > 8) tagShort = tagShort.substring(0, 8);
                        tagView.setText(tagShort.toUpperCase());
                        GradientDrawable tBg = new GradientDrawable();
                        tBg.setCornerRadius(dpToPx(6));
                        tBg.setColor(isDarkTheme ? 0xFF1E2838 : 0xFFE0F2FE);
                        tagView.setBackground(tBg);
                        tagView.setTextColor(isDarkTheme ? 0xFF00E5FF : 0xFF0284C7);
                    } else {
                        tagView.setVisibility(View.GONE);
                    }
                }

                // Domain & Relative Time
                if (domainView != null) {
                    String domain = item.url;
                    try {
                        Uri u = Uri.parse(item.url);
                        if (u != null && u.getHost() != null) domain = u.getHost();
                    } catch (Exception ignored) {}
                    domainView.setText(domain);
                    domainView.setTextColor(isDarkTheme ? 0xFF849396 : 0xFF64748B);
                }

                if (timeView != null) {
                    long diff = System.currentTimeMillis() - item.timestamp;
                    String timeStr;
                    if (diff < 60000L) timeStr = "just now";
                    else if (diff < 3600000L) timeStr = (diff / 60000L) + "m ago";
                    else if (diff < 86400000L) timeStr = (diff / 3600000L) + "h ago";
                    else if (diff < 604800000L) timeStr = (diff / 86400000L) + "d ago";
                    else if (diff < 2592000000L) timeStr = (diff / 604800000L) + "w ago";
                    else timeStr = new java.text.SimpleDateFormat("MMM dd", java.util.Locale.US).format(new java.util.Date(item.timestamp));
                    timeView.setText(timeStr);
                    timeView.setTextColor(isDarkTheme ? 0xFF10B981 : 0xFF059669);
                }

                // Squircle Icon Frame
                if (iconFrame != null && iconEmoji != null) {
                    String u = item.url.toLowerCase();
                    String t = item.title.toLowerCase();
                    int squircleBg;
                    String emoji;
                    if (u.contains("notion") || t.contains("notion") || t.contains("spec") || t.contains("doc")) {
                        squircleBg = isDarkTheme ? 0xFF064E3B : 0xFFD1FAE5;
                        emoji = "📝";
                    } else if (u.contains("arxiv") || t.contains("ai") || t.contains("llm") || t.contains("prompt")) {
                        squircleBg = isDarkTheme ? 0xFF78350F : 0xFFFEF3C7;
                        emoji = "📖";
                    } else if (u.contains("figma") || t.contains("figma") || t.contains("design") || t.contains("token")) {
                        squircleBg = isDarkTheme ? 0xFF831843 : 0xFFFCE7F3;
                        emoji = "✏️";
                    } else if (u.contains("tailwind") || u.contains("github") || t.contains("code") || t.contains("dev")) {
                        squircleBg = isDarkTheme ? 0xFF164E63 : 0xFFCFFAFE;
                        emoji = "💻";
                    } else if (u.contains("chatgpt") || u.contains("gemini") || u.contains("claude")) {
                        squircleBg = isDarkTheme ? 0xFF312E81 : 0xFFEDE9FE;
                        emoji = "🤖";
                    } else {
                        squircleBg = isDarkTheme ? 0xFF1E293B : 0xFFF1F5F9;
                        emoji = "🔖";
                    }

                    GradientDrawable fBg = new GradientDrawable();
                    fBg.setCornerRadius(dpToPx(12));
                    fBg.setColor(squircleBg);
                    iconFrame.setBackground(fBg);
                    iconEmoji.setText(emoji);
                }

                // Favorite Star Button
                if (starBtn != null) {
                    starBtn.setImageResource(R.drawable.ic_menu_bookmark);
                    starBtn.setColorFilter(item.isFavorite ? 0xFFFBBF24 : (isDarkTheme ? 0xFF475569 : 0xFFCBD5E1));
                    starBtn.setOnClickListener(v -> {
                        playUiFeedbackSound("tap");
                        bookmarkManager.toggleFavorite(item.id);
                        refreshRef[0].run();
                    });
                }

                // Pencil Button: Options (Move to group, Edit, Tabs, Copy, Delete)
                if (moreBtn != null) {
                    moreBtn.setColorFilter(isDarkTheme ? 0xFF849396 : 0xFF64748B);
                    moreBtn.setOnClickListener(v -> {
                        playUiFeedbackSound("tap");
                        PopupMenu popup = new PopupMenu(this, moreBtn);
                        popup.getMenu().add(0, 1, 0, "📁 Move to Group");
                        popup.getMenu().add(0, 2, 1, "✏️ Edit Bookmark");
                        popup.getMenu().add(0, 3, 2, "Open in Active Tab");
                        popup.getMenu().add(0, 4, 3, "Open in New Tab");
                        popup.getMenu().add(0, 5, 4, "Copy Link");
                        popup.getMenu().add(0, 6, 5, "Delete Bookmark");
                        popup.setOnMenuItemClickListener(mi -> {
                            if (mi.getItemId() == 1) {
                                showMoveBookmarkToGroupDialog(item, refreshRef[0], renderChipsRef[0]);
                                return true;
                            } else if (mi.getItemId() == 2) {
                                showEditBookmarkDialog(item, refreshRef[0], renderChipsRef[0]);
                                return true;
                            } else if (mi.getItemId() == 3) {
                                dialog.dismiss();
                                TabItem active = getActiveOrDominantTab();
                                if (active != null && active.webView != null) active.webView.loadUrl(item.url);
                                else addNewTab("web", null, item.url, false);
                                return true;
                            } else if (mi.getItemId() == 4) {
                                dialog.dismiss();
                                addNewTab("web", null, item.url, false);
                                return true;
                            } else if (mi.getItemId() == 5) {
                                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                if (cm != null) cm.setPrimaryClip(ClipData.newPlainText("URL", item.url));
                                Toast.makeText(this, "Link copied to clipboard", Toast.LENGTH_SHORT).show();
                                return true;
                            } else if (mi.getItemId() == 6) {
                                bookmarkManager.deleteBookmark(item.id);
                                Toast.makeText(this, "Bookmark deleted", Toast.LENGTH_SHORT).show();
                                refreshRef[0].run();
                                return true;
                            }
                            return false;
                        });
                        popup.show();
                    });
                }

                // Card Click
                card.setOnClickListener(v -> {
                    if (isSelectMode[0]) {
                        if (itemCheck != null) itemCheck.toggle();
                    } else {
                        dialog.dismiss();
                        playUiFeedbackSound("tap");
                        TabItem active = getActiveOrDominantTab();
                        if (active != null && active.webView != null) active.webView.loadUrl(item.url);
                        else addNewTab("web", null, item.url, false);
                    }
                });

                listContainer.addView(card);
            }
        };

        currentBookmarksRefreshRunnable = refreshRef[0];

        // Batch Select Toggle Listener
        if (btnBatchToggle != null) {
            btnBatchToggle.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                isSelectMode[0] = !isSelectMode[0];
                btnBatchToggle.setText(isSelectMode[0] ? "CANCEL" : "SELECT");
                selectedBookmarkIds.clear();
                if (btnBatchDelete != null) btnBatchDelete.setVisibility(View.GONE);
                refreshRef[0].run();
            });
        }

        // Batch Delete Listener
        if (btnBatchDelete != null) {
            btnBatchDelete.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                if (!selectedBookmarkIds.isEmpty()) {
                    new AlertDialog.Builder(this)
                            .setTitle("Delete Bookmarks")
                            .setMessage("Delete " + selectedBookmarkIds.size() + " selected bookmarks?")
                            .setPositiveButton("Delete", (d, which) -> {
                                bookmarkManager.batchDelete(new ArrayList<>(selectedBookmarkIds));
                                selectedBookmarkIds.clear();
                                isSelectMode[0] = false;
                                btnBatchToggle.setText("SELECT");
                                btnBatchDelete.setVisibility(View.GONE);
                                refreshRef[0].run();
                                Toast.makeText(this, "Bookmarks deleted", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                }
            });
        }

        // Sort Button Listener
        if (btnSort != null) {
            btnSort.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                PopupMenu sortMenu = new PopupMenu(this, btnSort);
                sortMenu.getMenu().add(0, 1, 0, "Newest First");
                sortMenu.getMenu().add(0, 2, 1, "Oldest First");
                sortMenu.getMenu().add(0, 3, 2, "Title (A-Z)");
                sortMenu.getMenu().add(0, 4, 3, "Title (Z-A)");
                sortMenu.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == 1) { activeSort[0] = "newest"; textSortLabel.setText("Newest"); }
                    else if (item.getItemId() == 2) { activeSort[0] = "oldest"; textSortLabel.setText("Oldest"); }
                    else if (item.getItemId() == 3) { activeSort[0] = "az"; textSortLabel.setText("A-Z"); }
                    else if (item.getItemId() == 4) { activeSort[0] = "za"; textSortLabel.setText("Z-A"); }
                    refreshRef[0].run();
                    return true;
                });
                sortMenu.show();
            });
        }

        // Search Input Listener
        if (searchInput != null) {
            searchInput.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) { refreshRef[0].run(); }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        // Bottom Dock: Export Bookmarks Button
        if (btnDockExport != null) {
            btnDockExport.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                try {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    startActivityForResult(intent, REQUEST_CODE_EXPORT_BOOKMARKS_TREE);
                    Toast.makeText(this, "Select export destination folder", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, "Export unavailable: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Bottom Dock: Import Bookmarks Button
        if (btnDockImport != null) {
            btnDockImport.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                try {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("*/*");
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    startActivityForResult(Intent.createChooser(intent, "Select Bookmarks File (HTML/JSON)"), REQUEST_CODE_IMPORT_BOOKMARKS_FILE);
                } catch (Exception e) {
                    Toast.makeText(this, "Import unavailable: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Bottom Dock: Add Bookmark or Group Plus Button (Presents Action Menu)
        if (btnDockAdd != null) {
            btnDockAdd.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                showAddBookmarkOrGroupMenu(() -> {
                    renderChipsRef[0].run();
                    refreshRef[0].run();
                });
            });
        }

        renderChipsRef[0].run();
        refreshRef[0].run();
        dialog.show();
    }

    private void showAddBookmarkOrGroupMenu(Runnable onRefresh) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dpToPx(24), dpToPx(22), dpToPx(24), dpToPx(20));

        GradientDrawable rootBg = new GradientDrawable();
        rootBg.setCornerRadius(dpToPx(24));
        rootBg.setColor(isDarkTheme ? 0xFF141926 : 0xFFFFFFFF);
        rootBg.setStroke(dpToPx(1), isDarkTheme ? 0xFF1F293D : 0xFFCBD5E1);
        root.setBackground(rootBg);

        TextView title = new TextView(this);
        title.setText("Add to Caspian Vault");
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(isDarkTheme ? 0xFFFFFFFF : 0xFF0F172A);
        root.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Choose what you would like to create");
        subtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        subtitle.setTextColor(isDarkTheme ? 0xFF849396 : 0xFF64748B);
        subtitle.setPadding(0, dpToPx(4), 0, dpToPx(18));
        root.addView(subtitle);

        AlertDialog menuDialog = builder.setView(root).create();
        if (menuDialog.getWindow() != null) {
            menuDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Option 1: New Bookmark
        LinearLayout cardBookmark = createVaultMenuOptionCard(
                "🔖", "New Bookmark", "Save active page or enter a custom link",
                () -> {
                    menuDialog.dismiss();
                    showAddBookmarkDialog(onRefresh);
                }
        );
        root.addView(cardBookmark);

        // Option 2: New Bookmark Group
        LinearLayout cardGroup = createVaultMenuOptionCard(
                "📁", "New Bookmark Group", "Create a custom category folder for bookmarks",
                () -> {
                    menuDialog.dismiss();
                    showCreateBookmarkGroupDialog(onRefresh);
                }
        );
        root.addView(cardGroup);

        menuDialog.show();
    }

    private LinearLayout createVaultMenuOptionCard(String emoji, String titleText, String subtitleText, Runnable onClick) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dpToPx(14), dpToPx(14), dpToPx(14), dpToPx(14));
        card.setClickable(true);
        card.setFocusable(true);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dpToPx(10);
        card.setLayoutParams(lp);

        GradientDrawable cBg = new GradientDrawable();
        cBg.setCornerRadius(dpToPx(16));
        cBg.setColor(isDarkTheme ? 0xFF1B2232 : 0xFFF8FAFC);
        cBg.setStroke(dpToPx(1), isDarkTheme ? 0xFF27334D : 0xFFE2E8F0);
        card.setBackground(cBg);

        // Emoji Frame
        FrameLayout iconFrame = new FrameLayout(this);
        LinearLayout.LayoutParams ifLp = new LinearLayout.LayoutParams(dpToPx(40), dpToPx(40));
        ifLp.setMarginEnd(dpToPx(12));
        iconFrame.setLayoutParams(ifLp);

        GradientDrawable ifBg = new GradientDrawable();
        ifBg.setCornerRadius(dpToPx(12));
        ifBg.setColor(isDarkTheme ? 0xFF141926 : 0xFFEEF2F6);
        iconFrame.setBackground(ifBg);

        TextView tvEmoji = new TextView(this);
        tvEmoji.setText(emoji);
        tvEmoji.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f);
        tvEmoji.setGravity(Gravity.CENTER);
        iconFrame.addView(tvEmoji);
        card.addView(iconFrame);

        // Text details Column
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView tvTitle = new TextView(this);
        tvTitle.setText(titleText);
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
        tvTitle.setTypeface(null, Typeface.BOLD);
        tvTitle.setTextColor(isDarkTheme ? 0xFFF1F5F9 : 0xFF0F172A);
        col.addView(tvTitle);

        TextView tvSub = new TextView(this);
        tvSub.setText(subtitleText);
        tvSub.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
        tvSub.setTextColor(isDarkTheme ? 0xFF849396 : 0xFF64748B);
        tvSub.setPadding(0, dpToPx(2), 0, 0);
        col.addView(tvSub);

        card.addView(col);

        // Right arrow
        TextView arrow = new TextView(this);
        arrow.setText("›");
        arrow.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f);
        arrow.setTextColor(isDarkTheme ? 0xFF64748B : 0xFF94A3B8);
        card.addView(arrow);

        card.setOnClickListener(v -> {
            playUiFeedbackSound("tap");
            if (onClick != null) onClick.run();
        });

        return card;
    }

    private void showMoveBookmarkToGroupDialog(BookmarkManager.BookmarkItem item, Runnable onRefresh, Runnable onChipsRefresh) {
        if (item == null || bookmarkManager == null) return;
        playUiFeedbackSound("tap");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dpToPx(24), dpToPx(22), dpToPx(24), dpToPx(20));

        GradientDrawable rBg = new GradientDrawable();
        rBg.setCornerRadius(dpToPx(22));
        rBg.setColor(isDarkTheme ? 0xFF141926 : 0xFFFFFFFF);
        rBg.setStroke(dpToPx(1), isDarkTheme ? 0xFF232B3E : 0xFFCBD5E1);
        root.setBackground(rBg);

        TextView title = new TextView(this);
        title.setText("Move to Group");
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(isDarkTheme ? 0xFFFFFFFF : 0xFF0F172A);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("Select destination group for: \"" + item.title + "\"");
        sub.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        sub.setTextColor(isDarkTheme ? 0xFF849396 : 0xFF64748B);
        sub.setPadding(0, dpToPx(4), 0, dpToPx(14));
        root.addView(sub);

        List<String> folderList = new ArrayList<>();
        folderList.add("Default");
        List<String> existing = bookmarkManager.getFolders();
        for (String f : existing) {
            if (!f.equalsIgnoreCase("Favorites") && !f.equalsIgnoreCase("All") && !f.equalsIgnoreCase("Default")) {
                folderList.add(f);
            }
        }

        AlertDialog moveDialog = builder.setView(root).create();
        if (moveDialog.getWindow() != null) {
            moveDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        for (String folder : folderList) {
            TextView opt = new TextView(this);
            boolean isCurrent = folder.equalsIgnoreCase(item.folder);
            opt.setText((isCurrent ? "✓ 📁 " : "📁 ") + folder + (isCurrent ? " (Current)" : ""));
            opt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f);
            opt.setTextColor(isCurrent ? 0xFF00E5FF : (isDarkTheme ? 0xFFDFE2F0 : 0xFF0F172A));
            opt.setPadding(dpToPx(14), dpToPx(10), dpToPx(14), dpToPx(10));
            GradientDrawable optBg = new GradientDrawable();
            optBg.setCornerRadius(dpToPx(10));
            optBg.setColor(isCurrent ? (isDarkTheme ? 0xFF1B2A3D : 0xFFE0F2FE) : (isDarkTheme ? 0xFF19202E : 0xFFF1F5F9));
            opt.setBackground(optBg);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, dpToPx(6));
            opt.setLayoutParams(lp);
            opt.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                bookmarkManager.moveBookmarkToFolder(item.id, folder);
                Toast.makeText(this, "Moved to " + folder + "! 📁", Toast.LENGTH_SHORT).show();
                moveDialog.dismiss();
                if (onRefresh != null) onRefresh.run();
                if (onChipsRefresh != null) onChipsRefresh.run();
            });
            root.addView(opt);
        }

        // Add Create New Group & Move Here
        TextView optNew = new TextView(this);
        optNew.setText("➕ Create New Group & Move Here...");
        optNew.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        optNew.setTypeface(null, Typeface.BOLD);
        optNew.setTextColor(0xFF00E5FF);
        optNew.setPadding(dpToPx(14), dpToPx(10), dpToPx(14), dpToPx(10));
        GradientDrawable nBg = new GradientDrawable();
        nBg.setCornerRadius(dpToPx(10));
        nBg.setColor(isDarkTheme ? 0xFF122030 : 0xFFE0F2FE);
        nBg.setStroke(dpToPx(1), isDarkTheme ? 0xFF00E5FF : 0xFF0284C7);
        optNew.setBackground(nBg);
        LinearLayout.LayoutParams nlp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        nlp.setMargins(0, dpToPx(4), 0, dpToPx(12));
        optNew.setLayoutParams(nlp);
        optNew.setOnClickListener(v -> {
            playUiFeedbackSound("tap");
            moveDialog.dismiss();
            showCreateBookmarkGroupDialog(() -> {
                List<String> updated = bookmarkManager.getFolders();
                if (!updated.isEmpty()) {
                    String newest = updated.get(updated.size() - 1);
                    bookmarkManager.moveBookmarkToFolder(item.id, newest);
                    Toast.makeText(this, "Moved to " + newest + "! 📁", Toast.LENGTH_SHORT).show();
                }
                if (onRefresh != null) onRefresh.run();
                if (onChipsRefresh != null) onChipsRefresh.run();
            });
        });
        root.addView(optNew);

        TextView btnCancel = new TextView(this);
        btnCancel.setText("Cancel");
        btnCancel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        btnCancel.setTextColor(isDarkTheme ? 0xFF94A3B8 : 0xFF64748B);
        btnCancel.setGravity(Gravity.CENTER);
        btnCancel.setPadding(0, dpToPx(6), 0, 0);
        btnCancel.setOnClickListener(v -> moveDialog.dismiss());
        root.addView(btnCancel);

        moveDialog.show();
    }

    private void showEditBookmarkDialog(BookmarkManager.BookmarkItem item, Runnable onRefresh, Runnable onChipsRefresh) {
        if (item == null || bookmarkManager == null) return;
        playUiFeedbackSound("tap");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dpToPx(24), dpToPx(22), dpToPx(24), dpToPx(18));

        GradientDrawable rBg = new GradientDrawable();
        rBg.setCornerRadius(dpToPx(22));
        rBg.setColor(isDarkTheme ? 0xFF141926 : 0xFFFFFFFF);
        rBg.setStroke(dpToPx(1), isDarkTheme ? 0xFF232B3E : 0xFFCBD5E1);
        root.setBackground(rBg);

        TextView title = new TextView(this);
        title.setText("Edit Bookmark");
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(isDarkTheme ? 0xFFFFFFFF : 0xFF0F172A);
        root.addView(title);

        // Title Input
        TextView lblTitle = new TextView(this);
        lblTitle.setText("PAGE TITLE");
        lblTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f);
        lblTitle.setTypeface(null, Typeface.BOLD);
        lblTitle.setTextColor(isDarkTheme ? 0xFF849396 : 0xFF64748B);
        lblTitle.setPadding(0, dpToPx(14), 0, dpToPx(4));
        root.addView(lblTitle);

        EditText etTitle = new EditText(this);
        etTitle.setText(item.title);
        etTitle.setTextColor(isDarkTheme ? 0xFFDFE2F0 : 0xFF0F172A);
        etTitle.setHintTextColor(isDarkTheme ? 0xFF64748B : 0xFF94A3B8);
        etTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f);
        root.addView(etTitle);

        // URL Input
        TextView lblUrl = new TextView(this);
        lblUrl.setText("WEB URL");
        lblUrl.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f);
        lblUrl.setTypeface(null, Typeface.BOLD);
        lblUrl.setTextColor(isDarkTheme ? 0xFF849396 : 0xFF64748B);
        lblUrl.setPadding(0, dpToPx(10), 0, dpToPx(4));
        root.addView(lblUrl);

        EditText etUrl = new EditText(this);
        etUrl.setText(item.url);
        etUrl.setTextColor(isDarkTheme ? 0xFFDFE2F0 : 0xFF0F172A);
        etUrl.setHintTextColor(isDarkTheme ? 0xFF64748B : 0xFF94A3B8);
        etUrl.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f);
        root.addView(etUrl);

        // Group Selector
        TextView lblGroup = new TextView(this);
        lblGroup.setText("BOOKMARK GROUP");
        lblGroup.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f);
        lblGroup.setTypeface(null, Typeface.BOLD);
        lblGroup.setTextColor(isDarkTheme ? 0xFF849396 : 0xFF64748B);
        lblGroup.setPadding(0, dpToPx(10), 0, dpToPx(4));
        root.addView(lblGroup);

        List<String> folderList = new ArrayList<>();
        folderList.add("Default");
        List<String> existing = bookmarkManager.getFolders();
        for (String f : existing) {
            if (!f.equalsIgnoreCase("Favorites") && !f.equalsIgnoreCase("All") && !f.equalsIgnoreCase("Default")) {
                folderList.add(f);
            }
        }
        final String[] selectedFolder = { (item.folder != null && !item.folder.isEmpty()) ? item.folder : "Default" };

        TextView btnGroupPicker = new TextView(this);
        btnGroupPicker.setText("📁 " + selectedFolder[0]);
        btnGroupPicker.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        btnGroupPicker.setTextColor(isDarkTheme ? 0xFF00E5FF : 0xFF0284C7);
        btnGroupPicker.setPadding(dpToPx(12), dpToPx(8), dpToPx(12), dpToPx(8));

        GradientDrawable gpBg = new GradientDrawable();
        gpBg.setCornerRadius(dpToPx(10));
        gpBg.setColor(isDarkTheme ? 0xFF1E2838 : 0xFFE0F2FE);
        gpBg.setStroke(dpToPx(1), isDarkTheme ? 0xFF2B3A50 : 0xFFBAE6FD);
        btnGroupPicker.setBackground(gpBg);
        btnGroupPicker.setClickable(true);
        btnGroupPicker.setFocusable(true);
        btnGroupPicker.setOnClickListener(v -> {
            playUiFeedbackSound("tap");
            PopupMenu pm = new PopupMenu(this, btnGroupPicker);
            for (int i = 0; i < folderList.size(); i++) {
                pm.getMenu().add(0, i, i, "📁 " + folderList.get(i));
            }
            pm.getMenu().add(0, folderList.size(), folderList.size(), "➕ New Group...");
            pm.setOnMenuItemClickListener(mi -> {
                int id = mi.getItemId();
                if (id >= 0 && id < folderList.size()) {
                    selectedFolder[0] = folderList.get(id);
                    btnGroupPicker.setText("📁 " + selectedFolder[0]);
                } else if (id == folderList.size()) {
                    showCreateBookmarkGroupDialog(() -> {
                        List<String> up = bookmarkManager.getFolders();
                        if (!up.isEmpty()) {
                            selectedFolder[0] = up.get(up.size() - 1);
                            btnGroupPicker.setText("📁 " + selectedFolder[0]);
                        }
                    });
                }
                return true;
            });
            pm.show();
        });
        root.addView(btnGroupPicker);

        AlertDialog editDialog = builder.setView(root).create();
        if (editDialog.getWindow() != null) {
            editDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        LinearLayout btnRow = new LinearLayout(this);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setGravity(Gravity.END);
        btnRow.setPadding(0, dpToPx(20), 0, 0);

        TextView btnCancel = new TextView(this);
        btnCancel.setText("Cancel");
        btnCancel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        btnCancel.setTextColor(isDarkTheme ? 0xFF94A3B8 : 0xFF64748B);
        btnCancel.setPadding(dpToPx(14), dpToPx(8), dpToPx(14), dpToPx(8));
        btnCancel.setOnClickListener(v -> editDialog.dismiss());
        btnRow.addView(btnCancel);

        TextView btnSave = new TextView(this);
        btnSave.setText("Save Changes");
        btnSave.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        btnSave.setTypeface(null, Typeface.BOLD);
        btnSave.setTextColor(0xFF000000);
        btnSave.setPadding(dpToPx(16), dpToPx(8), dpToPx(16), dpToPx(8));
        GradientDrawable svBg = new GradientDrawable();
        svBg.setCornerRadius(dpToPx(10));
        svBg.setColor(0xFF00E5FF);
        btnSave.setBackground(svBg);
        btnSave.setOnClickListener(v -> {
            playUiFeedbackSound("tap");
            String t = etTitle.getText().toString().trim();
            String u = etUrl.getText().toString().trim();
            if (!u.isEmpty()) {
                if (!u.startsWith("http://") && !u.startsWith("https://")) u = "https://" + u;
                if (t.isEmpty()) t = u;
                bookmarkManager.updateBookmark(item.id, t, u, selectedFolder[0]);
                Toast.makeText(this, "Bookmark updated! ✨", Toast.LENGTH_SHORT).show();
                editDialog.dismiss();
                if (onRefresh != null) onRefresh.run();
                if (onChipsRefresh != null) onChipsRefresh.run();
            }
        });
        btnRow.addView(btnSave);
        root.addView(btnRow);

        editDialog.show();
    }

    public void showAddBookmarkDialog() {
        showAddBookmarkDialog(null);
    }

    private void showAddBookmarkDialog(Runnable onRefresh) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dpToPx(24), dpToPx(22), dpToPx(24), dpToPx(18));

        GradientDrawable rootBg = new GradientDrawable();
        rootBg.setCornerRadius(dpToPx(22));
        rootBg.setColor(isDarkTheme ? 0xFF141926 : 0xFFFFFFFF);
        rootBg.setStroke(dpToPx(1), isDarkTheme ? 0xFF1F293D : 0xFFCBD5E1);
        root.setBackground(rootBg);

        TextView title = new TextView(this);
        title.setText("Add Bookmark");
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(isDarkTheme ? 0xFFFFFFFF : 0xFF0F172A);
        root.addView(title);

        TabItem activeTab = getActiveOrDominantTab();

        // Title Input
        TextView lblTitle = new TextView(this);
        lblTitle.setText("PAGE TITLE");
        lblTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f);
        lblTitle.setTypeface(null, Typeface.BOLD);
        lblTitle.setTextColor(isDarkTheme ? 0xFF849396 : 0xFF64748B);
        lblTitle.setPadding(0, dpToPx(14), 0, dpToPx(4));
        root.addView(lblTitle);

        EditText etTitle = new EditText(this);
        etTitle.setHint("Bookmark Title");
        if (activeTab != null && activeTab.title != null) etTitle.setText(activeTab.title);
        etTitle.setTextColor(isDarkTheme ? 0xFFDFE2F0 : 0xFF0F172A);
        etTitle.setHintTextColor(isDarkTheme ? 0xFF64748B : 0xFF94A3B8);
        etTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f);
        root.addView(etTitle);

        // URL Input
        TextView lblUrl = new TextView(this);
        lblUrl.setText("WEB URL");
        lblUrl.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f);
        lblUrl.setTypeface(null, Typeface.BOLD);
        lblUrl.setTextColor(isDarkTheme ? 0xFF849396 : 0xFF64748B);
        lblUrl.setPadding(0, dpToPx(10), 0, dpToPx(4));
        root.addView(lblUrl);

        EditText etUrl = new EditText(this);
        etUrl.setHint("https://...");
        if (activeTab != null && activeTab.url != null) etUrl.setText(activeTab.url);
        etUrl.setTextColor(isDarkTheme ? 0xFFDFE2F0 : 0xFF0F172A);
        etUrl.setHintTextColor(isDarkTheme ? 0xFF64748B : 0xFF94A3B8);
        etUrl.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f);
        root.addView(etUrl);

        // Group Selector
        TextView lblGroup = new TextView(this);
        lblGroup.setText("BOOKMARK GROUP");
        lblGroup.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f);
        lblGroup.setTypeface(null, Typeface.BOLD);
        lblGroup.setTextColor(isDarkTheme ? 0xFF849396 : 0xFF64748B);
        lblGroup.setPadding(0, dpToPx(10), 0, dpToPx(4));
        root.addView(lblGroup);

        List<String> folderList = new ArrayList<>();
        folderList.add("Default");
        List<String> existing = bookmarkManager.getFolders();
        for (String f : existing) {
            if (!f.equalsIgnoreCase("Favorites") && !f.equalsIgnoreCase("All") && !f.equalsIgnoreCase("Default")) {
                folderList.add(f);
            }
        }
        final String[] selectedFolder = {folderList.get(0)};

        TextView btnGroupPicker = new TextView(this);
        btnGroupPicker.setText("📁 " + selectedFolder[0]);
        btnGroupPicker.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        btnGroupPicker.setTextColor(isDarkTheme ? 0xFF00E5FF : 0xFF0284C7);
        btnGroupPicker.setPadding(dpToPx(12), dpToPx(8), dpToPx(12), dpToPx(8));

        GradientDrawable gpBg = new GradientDrawable();
        gpBg.setCornerRadius(dpToPx(10));
        gpBg.setColor(isDarkTheme ? 0xFF1E2838 : 0xFFE0F2FE);
        gpBg.setStroke(dpToPx(1), isDarkTheme ? 0xFF2B3A50 : 0xFFBAE6FD);
        btnGroupPicker.setBackground(gpBg);
        btnGroupPicker.setClickable(true);
        btnGroupPicker.setFocusable(true);
        btnGroupPicker.setOnClickListener(v -> {
            playUiFeedbackSound("tap");
            PopupMenu pm = new PopupMenu(this, btnGroupPicker);
            for (int i = 0; i < folderList.size(); i++) {
                pm.getMenu().add(0, i, i, "📁 " + folderList.get(i));
            }
            pm.getMenu().add(0, folderList.size(), folderList.size(), "➕ New Group...");
            pm.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id >= 0 && id < folderList.size()) {
                    selectedFolder[0] = folderList.get(id);
                    btnGroupPicker.setText("📁 " + selectedFolder[0]);
                } else if (id == folderList.size()) {
                    showCreateBookmarkGroupDialog(() -> {
                        List<String> up = bookmarkManager.getFolders();
                        if (!up.isEmpty()) {
                            selectedFolder[0] = up.get(up.size() - 1);
                            btnGroupPicker.setText("📁 " + selectedFolder[0]);
                        }
                    });
                }
                return true;
            });
            pm.show();
        });
        root.addView(btnGroupPicker);

        AlertDialog addDialog = builder.setView(root).create();
        if (addDialog.getWindow() != null) {
            addDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Action Buttons Row
        LinearLayout btnRow = new LinearLayout(this);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setGravity(Gravity.END);
        btnRow.setPadding(0, dpToPx(20), 0, 0);

        TextView btnCancel = new TextView(this);
        btnCancel.setText("Cancel");
        btnCancel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        btnCancel.setTextColor(isDarkTheme ? 0xFF94A3B8 : 0xFF64748B);
        btnCancel.setPadding(dpToPx(14), dpToPx(8), dpToPx(14), dpToPx(8));
        btnCancel.setOnClickListener(v -> addDialog.dismiss());
        btnRow.addView(btnCancel);

        TextView btnSave = new TextView(this);
        btnSave.setText("Save Bookmark");
        btnSave.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        btnSave.setTypeface(null, Typeface.BOLD);
        btnSave.setTextColor(0xFF000000);
        btnSave.setPadding(dpToPx(16), dpToPx(8), dpToPx(16), dpToPx(8));

        GradientDrawable svBg = new GradientDrawable();
        svBg.setCornerRadius(dpToPx(10));
        svBg.setColor(0xFF00E5FF);
        btnSave.setBackground(svBg);
        btnSave.setOnClickListener(v -> {
            playUiFeedbackSound("tap");
            String t = etTitle.getText().toString().trim();
            String u = etUrl.getText().toString().trim();
            if (!u.isEmpty()) {
                if (!u.startsWith("http://") && !u.startsWith("https://")) u = "https://" + u;
                if (t.isEmpty()) t = u;
                bookmarkManager.addBookmark(t, u, selectedFolder[0], "");
                Toast.makeText(this, "Bookmark saved to " + selectedFolder[0] + "! ✨", Toast.LENGTH_SHORT).show();
                addDialog.dismiss();
                if (onRefresh != null) onRefresh.run();
            }
        });
        btnRow.addView(btnSave);
        root.addView(btnRow);

        addDialog.show();
    }

    private void showCreateBookmarkGroupDialog(Runnable onRefresh) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dpToPx(24), dpToPx(22), dpToPx(24), dpToPx(18));

        GradientDrawable rootBg = new GradientDrawable();
        rootBg.setCornerRadius(dpToPx(22));
        rootBg.setColor(isDarkTheme ? 0xFF141926 : 0xFFFFFFFF);
        rootBg.setStroke(dpToPx(1), isDarkTheme ? 0xFF1F293D : 0xFFCBD5E1);
        root.setBackground(rootBg);

        TextView title = new TextView(this);
        title.setText("New Bookmark Group");
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(isDarkTheme ? 0xFFFFFFFF : 0xFF0F172A);
        root.addView(title);

        TextView lblName = new TextView(this);
        lblName.setText("GROUP NAME");
        lblName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f);
        lblName.setTypeface(null, Typeface.BOLD);
        lblName.setTextColor(isDarkTheme ? 0xFF849396 : 0xFF64748B);
        lblName.setPadding(0, dpToPx(14), 0, dpToPx(4));
        root.addView(lblName);

        EditText etName = new EditText(this);
        etName.setHint("e.g. Research, Work, AI Prompts");
        etName.setTextColor(isDarkTheme ? 0xFFDFE2F0 : 0xFF0F172A);
        etName.setHintTextColor(isDarkTheme ? 0xFF64748B : 0xFF94A3B8);
        etName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f);
        root.addView(etName);

        AlertDialog groupDialog = builder.setView(root).create();
        if (groupDialog.getWindow() != null) {
            groupDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        LinearLayout btnRow = new LinearLayout(this);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setGravity(Gravity.END);
        btnRow.setPadding(0, dpToPx(20), 0, 0);

        TextView btnCancel = new TextView(this);
        btnCancel.setText("Cancel");
        btnCancel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        btnCancel.setTextColor(isDarkTheme ? 0xFF94A3B8 : 0xFF64748B);
        btnCancel.setPadding(dpToPx(14), dpToPx(8), dpToPx(14), dpToPx(8));
        btnCancel.setOnClickListener(v -> groupDialog.dismiss());
        btnRow.addView(btnCancel);

        TextView btnCreate = new TextView(this);
        btnCreate.setText("Create Group");
        btnCreate.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        btnCreate.setTypeface(null, Typeface.BOLD);
        btnCreate.setTextColor(0xFF000000);
        btnCreate.setPadding(dpToPx(16), dpToPx(8), dpToPx(16), dpToPx(8));

        GradientDrawable crBg = new GradientDrawable();
        crBg.setCornerRadius(dpToPx(10));
        crBg.setColor(0xFF00E5FF);
        btnCreate.setBackground(crBg);
        btnCreate.setOnClickListener(v -> {
            playUiFeedbackSound("tap");
            String gName = etName.getText().toString().trim();
            if (!gName.isEmpty()) {
                bookmarkManager.createFolder(gName);
                Toast.makeText(this, "Group \"" + gName + "\" created! 📁", Toast.LENGTH_SHORT).show();
                groupDialog.dismiss();
                if (onRefresh != null) onRefresh.run();
            }
        });
        btnRow.addView(btnCreate);
        root.addView(btnRow);

        groupDialog.show();
    }

    private void showRenameGroupDialog(String oldName, Runnable onComplete) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(dpToPx(24), dpToPx(20), dpToPx(24), dpToPx(16));

        GradientDrawable rBg = new GradientDrawable();
        rBg.setCornerRadius(dpToPx(20));
        rBg.setColor(isDarkTheme ? 0xFF141926 : 0xFFFFFFFF);
        rBg.setStroke(dpToPx(1), isDarkTheme ? 0xFF1F293D : 0xFFCBD5E1);
        l.setBackground(rBg);

        TextView tv = new TextView(this);
        tv.setText("Rename Bookmark Group");
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextColor(isDarkTheme ? 0xFFF1F5F9 : 0xFF0F172A);
        l.addView(tv);

        EditText et = new EditText(this);
        et.setText(oldName);
        et.setSelection(oldName.length());
        et.setTextColor(isDarkTheme ? 0xFFDFE2F0 : 0xFF0F172A);
        et.setHintTextColor(isDarkTheme ? 0xFF64748B : 0xFF94A3B8);
        et.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f);
        l.addView(et);

        AlertDialog renameDialog = b.setView(l).create();
        if (renameDialog.getWindow() != null) {
            renameDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        LinearLayout btnRow = new LinearLayout(this);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setGravity(Gravity.END);
        btnRow.setPadding(0, dpToPx(18), 0, 0);

        TextView btnCancel = new TextView(this);
        btnCancel.setText("Cancel");
        btnCancel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        btnCancel.setTextColor(isDarkTheme ? 0xFF94A3B8 : 0xFF64748B);
        btnCancel.setPadding(dpToPx(14), dpToPx(8), dpToPx(14), dpToPx(8));
        btnCancel.setOnClickListener(v -> renameDialog.dismiss());
        btnRow.addView(btnCancel);

        TextView btnRename = new TextView(this);
        btnRename.setText("Rename");
        btnRename.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        btnRename.setTypeface(null, Typeface.BOLD);
        btnRename.setTextColor(0xFF000000);
        btnRename.setPadding(dpToPx(16), dpToPx(8), dpToPx(16), dpToPx(8));

        GradientDrawable rnBg = new GradientDrawable();
        rnBg.setCornerRadius(dpToPx(10));
        rnBg.setColor(0xFF00E5FF);
        btnRename.setBackground(rnBg);
        btnRename.setOnClickListener(v -> {
            playUiFeedbackSound("tap");
            String newName = et.getText().toString().trim();
            if (!newName.isEmpty() && !newName.equalsIgnoreCase(oldName)) {
                bookmarkManager.renameFolder(oldName, newName);
                Toast.makeText(this, "Renamed to \"" + newName + "\"", Toast.LENGTH_SHORT).show();
                renameDialog.dismiss();
                if (onComplete != null) onComplete.run();
            }
        });
        btnRow.addView(btnRename);
        l.addView(btnRow);

        renameDialog.show();
    }

    private void showDeleteGroupDialog(String folderName, Runnable onComplete) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Group")
                .setMessage("Delete bookmark group \"" + folderName + "\"? Bookmarks inside this group will be moved to Default.")
                .setPositiveButton("Delete", (d, w) -> {
                    bookmarkManager.deleteFolder(folderName);
                    Toast.makeText(this, "Group \"" + folderName + "\" deleted", Toast.LENGTH_SHORT).show();
                    if (onComplete != null) onComplete.run();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void handleExportBookmarksToFolder(Uri treeUri) {
        if (treeUri == null || bookmarkManager == null) return;
        try {
            androidx.documentfile.provider.DocumentFile pickedDir = androidx.documentfile.provider.DocumentFile.fromTreeUri(this, treeUri);
            if (pickedDir != null && pickedDir.canWrite()) {
                androidx.documentfile.provider.DocumentFile newFile = pickedDir.createFile("text/html", "caspian_bookmarks_" + System.currentTimeMillis() + ".html");
                if (newFile != null) {
                    try (java.io.OutputStream os = getContentResolver().openOutputStream(newFile.getUri())) {
                        if (os != null) {
                            bookmarkManager.exportNetscapeHtml(os);
                            Toast.makeText(this, "Bookmarks exported successfully!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void handleImportBookmarksFromFile(Uri fileUri) {
        if (fileUri == null || bookmarkManager == null) return;
        try (java.io.InputStream is = getContentResolver().openInputStream(fileUri)) {
            if (is != null) {
                int count = bookmarkManager.importFromHtml(is);
                Toast.makeText(this, "Imported " + count + " bookmarks!", Toast.LENGTH_SHORT).show();
                if (currentBookmarksRefreshRunnable != null) currentBookmarksRefreshRunnable.run();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Import failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void showHistoryDialog() {
        showHistoryDialog(0);
    }

    public void showHistoryDialog(int enterDirection) {
        playUiFeedbackSound("tap");
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_history, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(isDarkTheme ? 0xFF0A0E17 : 0xFFF8FAFC));
            dialog.getWindow().setWindowAnimations(0);
        }
        dialogView.setBackgroundColor(isDarkTheme ? 0xFF0A0E17 : 0xFFF8FAFC);

        // Single iOS Slide Entrance Transition
        if (enterDirection > 0) {
            dialogView.setTranslationX(dpToPx(80));
            dialogView.setAlpha(0.3f);
            dialogView.animate().translationX(0f).alpha(1f).setDuration(200).setInterpolator(new DecelerateInterpolator(1.6f)).start();
        } else if (enterDirection < 0) {
            dialogView.setTranslationX(-dpToPx(80));
            dialogView.setAlpha(0.3f);
            dialogView.animate().translationX(0f).alpha(1f).setDuration(200).setInterpolator(new DecelerateInterpolator(1.6f)).start();
        }

        View historyRoot = dialogView.findViewById(R.id.history_root);
        View historyHeaderSection = dialogView.findViewById(R.id.history_header_section);
        View historyTabCapsule = dialogView.findViewById(R.id.history_tab_capsule);
        TextView historyTabCapsuleText = dialogView.findViewById(R.id.history_tab_capsule_text);
        TextView badgeCount = dialogView.findViewById(R.id.badge_history_count);
        TextView tabBookmarks = dialogView.findViewById(R.id.tab_header_bookmarks);
        TextView tabSettings = dialogView.findViewById(R.id.tab_header_settings);
        View historySearchCapsule = dialogView.findViewById(R.id.history_search_capsule);
        ImageView historySearchIcon = dialogView.findViewById(R.id.history_search_icon);
        EditText searchInput = dialogView.findViewById(R.id.history_search_input);
        View historySearchDivider = dialogView.findViewById(R.id.history_search_divider);
        TextView btnSelect = dialogView.findViewById(R.id.btn_history_select);
        ImageView btnOptions = dialogView.findViewById(R.id.btn_history_options);
        ImageView closeBtn = dialogView.findViewById(R.id.history_close_btn);

        if (historyRoot != null) {
            historyRoot.setBackgroundColor(isDarkTheme ? 0xFF0A0E17 : 0xFFF8FAFC);
        }
        if (historyHeaderSection != null) {
            GradientDrawable hSecBg = new GradientDrawable();
            hSecBg.setColor(isDarkTheme ? 0xFF0A0E17 : 0xFFFFFFFF);
            hSecBg.setStroke(dpToPx(1), isDarkTheme ? 0xFF1E2433 : 0xFFE2E8F0);
            historyHeaderSection.setBackground(hSecBg);
        }
        if (historyTabCapsule != null) {
            GradientDrawable capBg = new GradientDrawable();
            capBg.setCornerRadius(dpToPx(16));
            capBg.setColor(isDarkTheme ? 0xFF1E2838 : 0xFFF1F5F9);
            capBg.setStroke(dpToPx(1), isDarkTheme ? 0xFF00E5FF : 0xFFCBD5E1);
            historyTabCapsule.setBackground(capBg);
        }
        if (historyTabCapsuleText != null) {
            historyTabCapsuleText.setTextColor(isDarkTheme ? 0xFFFFFFFF : 0xFF0284C7);
        }
        if (badgeCount != null) {
            GradientDrawable bcBg = new GradientDrawable();
            bcBg.setCornerRadius(dpToPx(10));
            bcBg.setColor(isDarkTheme ? 0xFF161B22 : 0xFFE2E8F0);
            bcBg.setStroke(dpToPx(1), isDarkTheme ? 0xFF21262D : 0xFFCBD5E1);
            badgeCount.setBackground(bcBg);
            badgeCount.setTextColor(isDarkTheme ? 0xFFCBD5E1 : 0xFF0F172A);
        }
        if (tabBookmarks != null) {
            tabBookmarks.setTextColor(isDarkTheme ? 0xFF64748B : 0xFF475569);
        }
        if (tabSettings != null) {
            tabSettings.setTextColor(isDarkTheme ? 0xFF64748B : 0xFF475569);
        }

        if (historySearchCapsule != null) {
            GradientDrawable sBg = new GradientDrawable();
            sBg.setCornerRadius(dpToPx(14));
            sBg.setColor(isDarkTheme ? 0xFF0E131E : 0xFFFFFFFF);
            sBg.setStroke(dpToPx(1), isDarkTheme ? 0xFF1E2537 : 0xFFCBD5E1);
            historySearchCapsule.setBackground(sBg);
        }
        if (historySearchIcon != null) {
            historySearchIcon.setColorFilter(isDarkTheme ? 0xFF64748B : 0xFF64748B);
        }
        if (historySearchDivider != null) {
            historySearchDivider.setBackgroundColor(isDarkTheme ? 0xFF1E2433 : 0xFFE2E8F0);
        }
        if (searchInput != null) {
            searchInput.setTextColor(isDarkTheme ? 0xFFFFFFFF : 0xFF0F172A);
            searchInput.setHintTextColor(isDarkTheme ? 0xFF64748B : 0xFF94A3B8);
        }
        if (btnSelect != null) {
            GradientDrawable selBg = new GradientDrawable();
            selBg.setCornerRadius(dpToPx(8));
            selBg.setColor(isDarkTheme ? 0xFF162235 : 0xFFE0F2FE);
            selBg.setStroke(dpToPx(1), isDarkTheme ? 0xFF00E5FF : 0xFFBAE6FD);
            btnSelect.setBackground(selBg);
            btnSelect.setTextColor(isDarkTheme ? 0xFF00E5FF : 0xFF0284C7);
        }
        if (btnOptions != null) {
            GradientDrawable optBg = new GradientDrawable();
            optBg.setCornerRadius(dpToPx(8));
            optBg.setColor(isDarkTheme ? 0xFF161B22 : 0xFFF1F5F9);
            optBg.setStroke(dpToPx(1), isDarkTheme ? 0xFF21262D : 0xFFE2E8F0);
            btnOptions.setBackground(optBg);
            btnOptions.setColorFilter(isDarkTheme ? 0xFF94A3B8 : 0xFF475569);
        }
        if (closeBtn != null) {
            GradientDrawable clsBg = new GradientDrawable();
            clsBg.setCornerRadius(dpToPx(8));
            clsBg.setColor(isDarkTheme ? 0xFF161B22 : 0xFFF1F5F9);
            clsBg.setStroke(dpToPx(1), isDarkTheme ? 0xFF21262D : 0xFFE2E8F0);
            closeBtn.setBackground(clsBg);
            closeBtn.setColorFilter(isDarkTheme ? 0xFF94A3B8 : 0xFF475569);
        }

        TextView chipAll = dialogView.findViewById(R.id.chip_filter_all);
        TextView chip1h = dialogView.findViewById(R.id.chip_filter_1h);
        TextView chip24h = dialogView.findViewById(R.id.chip_filter_24h);
        TextView chip1w = dialogView.findViewById(R.id.chip_filter_1w);

        LinearLayout listContainer = dialogView.findViewById(R.id.history_list_container);
        TextView emptyView = dialogView.findViewById(R.id.history_empty_view);

        View historyBottomToolbar = dialogView.findViewById(R.id.history_bottom_toolbar);
        if (historyBottomToolbar != null) {
            historyBottomToolbar.setBackgroundColor(isDarkTheme ? 0xFF0A0E17 : 0xFFF8FAFC);
        }
        View historyBottomBarCard = dialogView.findViewById(R.id.history_bottom_bar_card);
        View bottomNormalBar = dialogView.findViewById(R.id.bottom_normal_bar);
        View bottomSelectBar = dialogView.findViewById(R.id.bottom_select_bar);
        View timeRangeSelector = dialogView.findViewById(R.id.layout_time_range_selector);
        View pillTimeRangeDropdown = dialogView.findViewById(R.id.pill_time_range_dropdown);
        ImageView iconTimeRangeClock = dialogView.findViewById(R.id.icon_time_range_clock);
        TextView labelTimeRange = dialogView.findViewById(R.id.label_time_range);
        ImageView iconTimeRangeChevron = dialogView.findViewById(R.id.icon_time_range_chevron);
        TextView textSelectedTimeRange = dialogView.findViewById(R.id.text_selected_time_range);

        View btnClearCookies = dialogView.findViewById(R.id.btn_clear_cookies);
        ImageView iconClearCookies = dialogView.findViewById(R.id.icon_clear_cookies);
        TextView textClearCookies = dialogView.findViewById(R.id.text_clear_cookies);

        View btnClearData = dialogView.findViewById(R.id.btn_clear_data);
        ImageView iconClearData = dialogView.findViewById(R.id.icon_clear_data);
        TextView textClearData = dialogView.findViewById(R.id.text_clear_data);

        View btnClearBoth = dialogView.findViewById(R.id.btn_clear_both);
        ImageView iconClearBoth = dialogView.findViewById(R.id.icon_clear_both);
        TextView textClearBoth = dialogView.findViewById(R.id.text_clear_both);

        if (historyBottomBarCard != null) {
            GradientDrawable bcBg = new GradientDrawable();
            bcBg.setCornerRadius(dpToPx(16));
            bcBg.setColor(isDarkTheme ? 0xFF0D1117 : 0xFFFFFFFF);
            bcBg.setStroke(dpToPx(1), isDarkTheme ? 0xFF1E2433 : 0xFFE2E8F0);
            historyBottomBarCard.setBackground(bcBg);
        }
        if (bottomNormalBar != null) {
            bottomNormalBar.setBackground(null);
        }
        if (bottomSelectBar != null) {
            bottomSelectBar.setBackground(null);
        }
        if (pillTimeRangeDropdown != null) {
            GradientDrawable pillBg = new GradientDrawable();
            pillBg.setCornerRadius(dpToPx(12));
            pillBg.setColor(isDarkTheme ? 0xFF161B22 : 0xFFF1F5F9);
            pillBg.setStroke(dpToPx(1), isDarkTheme ? 0xFF21262D : 0xFFCBD5E1);
            pillTimeRangeDropdown.setBackground(pillBg);
        }
        if (iconTimeRangeClock != null) iconTimeRangeClock.setColorFilter(isDarkTheme ? 0xFF94A3B8 : 0xFF64748B);
        if (labelTimeRange != null) labelTimeRange.setTextColor(isDarkTheme ? 0xFF94A3B8 : 0xFF64748B);
        if (iconTimeRangeChevron != null) iconTimeRangeChevron.setColorFilter(isDarkTheme ? 0xFF94A3B8 : 0xFF64748B);
        if (textSelectedTimeRange != null) textSelectedTimeRange.setTextColor(isDarkTheme ? 0xFFE2E8F0 : 0xFF0F172A);

        if (btnClearCookies != null) {
            GradientDrawable cBg = new GradientDrawable();
            cBg.setCornerRadius(dpToPx(10));
            cBg.setColor(isDarkTheme ? 0xFF161B22 : 0xFFF8FAFC);
            cBg.setStroke(dpToPx(1), isDarkTheme ? 0xFF21262D : 0xFFE2E8F0);
            btnClearCookies.setBackground(cBg);
        }
        if (iconClearCookies != null) iconClearCookies.setColorFilter(isDarkTheme ? 0xFF94A3B8 : 0xFF0284C7);
        if (textClearCookies != null) textClearCookies.setTextColor(isDarkTheme ? 0xFFCBD5E1 : 0xFF0F172A);

        if (btnClearData != null) {
            GradientDrawable dBg = new GradientDrawable();
            dBg.setCornerRadius(dpToPx(10));
            dBg.setColor(isDarkTheme ? 0xFF0B293B : 0xFFE0F2FE);
            dBg.setStroke(dpToPx(1), isDarkTheme ? 0xFF00E5FF : 0xFF38BDF8);
            btnClearData.setBackground(dBg);
        }
        if (iconClearData != null) iconClearData.setColorFilter(isDarkTheme ? 0xFF00E5FF : 0xFF0284C7);
        if (textClearData != null) textClearData.setTextColor(isDarkTheme ? 0xFFFFFFFF : 0xFF0369A1);

        if (btnClearBoth != null) {
            GradientDrawable bBg = new GradientDrawable();
            bBg.setCornerRadius(dpToPx(10));
            bBg.setColor(isDarkTheme ? 0xFF261217 : 0xFFFEF2F2);
            bBg.setStroke(dpToPx(1), isDarkTheme ? 0xFF4D1720 : 0xFFFECACA);
            btnClearBoth.setBackground(bBg);
        }
        if (iconClearBoth != null) iconClearBoth.setColorFilter(isDarkTheme ? 0xFFEF4444 : 0xFFEF4444);
        if (textClearBoth != null) textClearBoth.setTextColor(isDarkTheme ? 0xFFFCA5A5 : 0xFFDC2626);

        TextView textSelectedCount = dialogView.findViewById(R.id.text_selected_count);
        TextView btnSelectAllToggle = dialogView.findViewById(R.id.btn_select_all_toggle);
        TextView btnDeleteSelected = dialogView.findViewById(R.id.btn_delete_selected);
        TextView btnCancelSelect = dialogView.findViewById(R.id.btn_cancel_select);

        final boolean[] isSelectMode = new boolean[]{false};
        final Set<Long> selectedIds = new HashSet<>();
        final String[] activeFilter = new String[]{"all"};
        final String[] selectedTimeRange = new String[]{"24h"};
        final List<HistoryManager.HistoryEntry> currentVisibleEntries = new ArrayList<>();

        java.text.SimpleDateFormat timeSdf = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
        java.text.SimpleDateFormat dayFormat = new java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault());

        Runnable updateChipsVisuals = () -> {
            TextView[] chips = new TextView[]{chipAll, chip1h, chip24h, chip1w};
            String[] filterVals = new String[]{"all", "1h", "24h", "1w"};
            for (int ci = 0; ci < chips.length; ci++) {
                TextView cp = chips[ci];
                if (cp == null) continue;
                boolean act = filterVals[ci].equals(activeFilter[0]);
                GradientDrawable cBg = new GradientDrawable();
                cBg.setCornerRadius(dpToPx(14));
                if (act) {
                    cBg.setColor(isDarkTheme ? 0xFF00E5FF : 0xFF0284C7);
                    cBg.setStroke(dpToPx(1), isDarkTheme ? 0xFF00E5FF : 0xFF0284C7);
                    cp.setTextColor(isDarkTheme ? 0xFF0A0E17 : 0xFFFFFFFF);
                } else {
                    cBg.setColor(isDarkTheme ? 0xFF121620 : 0xFFFFFFFF);
                    cBg.setStroke(dpToPx(1), isDarkTheme ? 0xFF1E2537 : 0xFFCBD5E1);
                    cp.setTextColor(isDarkTheme ? 0xFF94A3B8 : 0xFF475569);
                }
                cp.setBackground(cBg);
            }
        };
        updateChipsVisuals.run();

        final Runnable[] refreshList = new Runnable[1];
        refreshList[0] = () -> {
            listContainer.removeAllViews();
            currentVisibleEntries.clear();
            String query = searchInput != null ? searchInput.getText().toString() : "";
            List<HistoryManager.HistoryEntry> rawEntries = HistoryManager.getInstance(this).getHistory(query);

            int totalCount = HistoryManager.getInstance(this).getTotalHistoryCount();
            if (badgeCount != null) badgeCount.setText(String.valueOf(totalCount));

            long now = System.currentTimeMillis();
            for (HistoryManager.HistoryEntry e : rawEntries) {
                if ("1h".equals(activeFilter[0])) {
                    if (e.timestamp < now - (3600L * 1000L)) continue;
                } else if ("24h".equals(activeFilter[0])) {
                    if (e.timestamp < now - (24L * 3600L * 1000L)) continue;
                } else if ("1w".equals(activeFilter[0])) {
                    if (e.timestamp < now - (7L * 24L * 3600L * 1000L)) continue;
                }
                currentVisibleEntries.add(e);
            }

            if (currentVisibleEntries.isEmpty()) {
                if (emptyView != null) emptyView.setVisibility(View.VISIBLE);
            } else {
                if (emptyView != null) emptyView.setVisibility(View.GONE);

                java.util.Calendar calNow = java.util.Calendar.getInstance();
                int curYear = calNow.get(java.util.Calendar.YEAR);
                int curDay = calNow.get(java.util.Calendar.DAY_OF_YEAR);

                // Group entries by date
                java.util.LinkedHashMap<String, List<HistoryManager.HistoryEntry>> groups = new java.util.LinkedHashMap<>();
                for (HistoryManager.HistoryEntry entry : currentVisibleEntries) {
                    java.util.Calendar calEntry = java.util.Calendar.getInstance();
                    calEntry.setTimeInMillis(entry.timestamp);
                    int entryYear = calEntry.get(java.util.Calendar.YEAR);
                    int entryDay = calEntry.get(java.util.Calendar.DAY_OF_YEAR);

                    String groupKey;
                    if (curYear == entryYear && curDay == entryDay) {
                        groupKey = "TODAY • " + dayFormat.format(new Date(entry.timestamp)).toUpperCase(Locale.ROOT);
                    } else if (curYear == entryYear && curDay - 1 == entryDay) {
                        groupKey = "YESTERDAY • " + dayFormat.format(new Date(entry.timestamp)).toUpperCase(Locale.ROOT);
                    } else {
                        groupKey = dayFormat.format(new Date(entry.timestamp)).toUpperCase(Locale.ROOT);
                    }

                    if (!groups.containsKey(groupKey)) {
                        groups.put(groupKey, new ArrayList<>());
                    }
                    groups.get(groupKey).add(entry);
                }

                for (Map.Entry<String, List<HistoryManager.HistoryEntry>> group : groups.entrySet()) {
                    String dateKey = group.getKey();
                    List<HistoryManager.HistoryEntry> dayEntries = group.getValue();

                    // Section Header
                    LinearLayout secHeader = new LinearLayout(this);
                    secHeader.setOrientation(LinearLayout.HORIZONTAL);
                    secHeader.setGravity(Gravity.CENTER_VERTICAL);
                    secHeader.setPadding(dpToPx(2), dpToPx(8), dpToPx(2), dpToPx(4));

                    TextView secTitle = new TextView(this);
                    secTitle.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                    secTitle.setText(dateKey);
                    secTitle.setTextColor(isDarkTheme ? 0xFF94A3B8 : 0xFF475569);
                    secTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
                    secTitle.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
                    secHeader.addView(secTitle);

                    TextView secDelete = new TextView(this);
                    secDelete.setText(dateKey.startsWith("TODAY") ? "Delete Today" : (dateKey.startsWith("YESTERDAY") ? "Delete Yesterday" : "Delete Day"));
                    secDelete.setTextColor(isDarkTheme ? 0xFF64748B : 0xFF94A3B8);
                    secDelete.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f);
                    secDelete.setClickable(true);
                    secDelete.setFocusable(true);
                    secDelete.setOnClickListener(v -> {
                        playUiFeedbackSound("tap");
                        List<Long> idsToDelete = new ArrayList<>();
                        for (HistoryManager.HistoryEntry e : dayEntries) idsToDelete.add(e.id);
                        HistoryManager.getInstance(this).deleteEntries(idsToDelete);
                        refreshList[0].run();
                        Toast.makeText(this, "Cleared " + dateKey, Toast.LENGTH_SHORT).show();
                    });
                    secHeader.addView(secDelete);
                    listContainer.addView(secHeader);

                    // Group Items
                    for (HistoryManager.HistoryEntry entry : dayEntries) {
                        LinearLayout card = new LinearLayout(this);
                        card.setOrientation(LinearLayout.HORIZONTAL);
                        card.setGravity(Gravity.CENTER_VERTICAL);
                        card.setPadding(dpToPx(12), dpToPx(10), dpToPx(10), dpToPx(10));

                        GradientDrawable cardBg = new GradientDrawable();
                        cardBg.setCornerRadius(dpToPx(14));
                        cardBg.setColor(isDarkTheme ? 0xFF121620 : 0xFFFFFFFF);
                        cardBg.setStroke(dpToPx(1), isDarkTheme ? 0xFF1E2433 : 0xFFE2E8F0);
                        card.setBackground(cardBg);

                        LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        cardLp.bottomMargin = dpToPx(7);
                        card.setLayoutParams(cardLp);

                        // Checkbox for Select Mode
                        if (isSelectMode[0]) {
                            TextView checkIcon = new TextView(this);
                            boolean isChecked = selectedIds.contains(entry.id);
                            checkIcon.setText(isChecked ? "✓" : "○");
                            checkIcon.setTextColor(isChecked ? 0xFF00E5FF : 0xFF64748B);
                            checkIcon.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
                            checkIcon.setTypeface(null, Typeface.BOLD);
                            checkIcon.setPadding(0, 0, dpToPx(10), 0);
                            card.addView(checkIcon);
                        }

                        // Squircle Icon Container (32dp x 32dp)
                        FrameLayout iconBox = new FrameLayout(this);
                        LinearLayout.LayoutParams ibLp = new LinearLayout.LayoutParams(dpToPx(32), dpToPx(32));
                        ibLp.setMarginEnd(dpToPx(10));
                        iconBox.setLayoutParams(ibLp);

                        GradientDrawable ibGd = new GradientDrawable();
                        ibGd.setColor(isDarkTheme ? 0xFF0D1117 : 0xFFF1F5F9);
                        ibGd.setCornerRadius(dpToPx(8));
                        ibGd.setStroke(dpToPx(1), isDarkTheme ? 0xFF1E2433 : 0xFFE2E8F0);
                        iconBox.setBackground(ibGd);

                        ImageView iv = new ImageView(this);
                        FrameLayout.LayoutParams ivLp = new FrameLayout.LayoutParams(dpToPx(16), dpToPx(16));
                        ivLp.gravity = Gravity.CENTER;
                        iv.setLayoutParams(ivLp);
                        if (entry.url.contains("google.")) {
                            iv.setImageResource(R.drawable.ic_stitch_google);
                        } else {
                            iv.setImageResource(R.drawable.ic_stitch_globe);
                            iv.setColorFilter(0xFF38BDF8);
                        }
                        iconBox.addView(iv);
                        card.addView(iconBox);

                        // Text Details Column
                        LinearLayout textCol = new LinearLayout(this);
                        textCol.setOrientation(LinearLayout.VERTICAL);
                        LinearLayout.LayoutParams tcLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                        textCol.setLayoutParams(tcLp);

                        TextView titleTv = new TextView(this);
                        titleTv.setText(entry.title != null && !entry.title.isEmpty() ? entry.title : entry.url);
                        titleTv.setTextColor(isDarkTheme ? 0xFFF1F5F9 : 0xFF0F172A);
                        titleTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
                        titleTv.setTypeface(null, Typeface.BOLD);
                        titleTv.setSingleLine(true);
                        titleTv.setEllipsize(android.text.TextUtils.TruncateAt.END);

                        TextView urlTv = new TextView(this);
                        urlTv.setText(cleanDisplayUrl(entry.url));
                        urlTv.setTextColor(isDarkTheme ? 0xFF94A3B8 : 0xFF64748B);
                        urlTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
                        urlTv.setSingleLine(true);
                        urlTv.setEllipsize(android.text.TextUtils.TruncateAt.END);

                        textCol.addView(titleTv);
                        textCol.addView(urlTv);
                        card.addView(textCol);

                        // Right Column: Time + 3-dots Menu
                        LinearLayout rightCol = new LinearLayout(this);
                        rightCol.setOrientation(LinearLayout.HORIZONTAL);
                        rightCol.setGravity(Gravity.CENTER_VERTICAL);
                        rightCol.setPadding(dpToPx(6), 0, 0, 0);

                        TextView timeTv = new TextView(this);
                        timeTv.setText(timeSdf.format(new Date(entry.timestamp)));
                        timeTv.setTextColor(isDarkTheme ? 0xFF64748B : 0xFF94A3B8);
                        timeTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f);
                        timeTv.setTypeface(Typeface.MONOSPACE);
                        rightCol.addView(timeTv);

                        ImageView moreBtn = new ImageView(this);
                        LinearLayout.LayoutParams mbLp = new LinearLayout.LayoutParams(dpToPx(24), dpToPx(24));
                        mbLp.setMarginStart(dpToPx(4));
                        moreBtn.setLayoutParams(mbLp);
                        moreBtn.setImageResource(R.drawable.ic_stitch_more_vert);
                        moreBtn.setColorFilter(isDarkTheme ? 0xFF94A3B8 : 0xFF64748B);
                        GradientDrawable mbBg = new GradientDrawable();
                        mbBg.setShape(GradientDrawable.OVAL);
                        mbBg.setColor(isDarkTheme ? 0xFF161B22 : 0xFFF1F5F9);
                        mbBg.setStroke(dpToPx(1), isDarkTheme ? 0xFF21262D : 0xFFE2E8F0);
                        moreBtn.setBackground(mbBg);
                        moreBtn.setClickable(true);
                        moreBtn.setFocusable(true);
                        moreBtn.setOnClickListener(v -> {
                            playUiFeedbackSound("tap");
                            PopupMenu popup = new PopupMenu(this, moreBtn);
                            popup.getMenu().add("Open in New Tab");
                            popup.getMenu().add("Copy URL");
                            popup.getMenu().add("Delete Entry");
                            popup.setOnMenuItemClickListener(item -> {
                                if ("Open in New Tab".equals(item.getTitle())) {
                                    dialog.dismiss();
                                    addNewTab("web", null, entry.url, false);
                                } else if ("Copy URL".equals(item.getTitle())) {
                                    android.content.ClipboardManager cm = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                    cm.setPrimaryClip(android.content.ClipData.newPlainText("URL", entry.url));
                                    Toast.makeText(this, "Copied URL to clipboard", Toast.LENGTH_SHORT).show();
                                } else if ("Delete Entry".equals(item.getTitle())) {
                                    HistoryManager.getInstance(this).deleteEntry(entry.id);
                                    refreshList[0].run();
                                }
                                return true;
                            });
                            popup.show();
                        });
                        rightCol.addView(moreBtn);
                        card.addView(rightCol);

                        card.setOnClickListener(v -> {
                            playUiFeedbackSound("tap");
                            if (isSelectMode[0]) {
                                if (selectedIds.contains(entry.id)) {
                                    selectedIds.remove(entry.id);
                                } else {
                                    selectedIds.add(entry.id);
                                }
                                if (textSelectedCount != null) {
                                    textSelectedCount.setText(selectedIds.size() + " selected");
                                }
                                refreshList[0].run();
                            } else {
                                dialog.dismiss();
                                TabItem active = getActiveOrDominantTab();
                                if (active != null) {
                                    active.webView.loadUrl(entry.url);
                                } else {
                                    addNewTab("web", null, entry.url, false);
                                }
                            }
                        });

                        listContainer.addView(card);
                    }
                }
            }
        };

        refreshList[0].run();

        // Search Input Listener
        if (searchInput != null) {
            searchInput.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) { refreshList[0].run(); }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        // Close Button
        if (closeBtn != null) {
            closeBtn.setOnClickListener(v -> dialog.dismiss());
        }

        // Header Bookmarks & Settings Navigation
        if (tabBookmarks != null) {
            tabBookmarks.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                dialog.dismiss();
                showBookmarksDialog(-1);
            });
        }
        if (tabSettings != null) {
            tabSettings.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                dialog.dismiss();
                openControlSheet();
            });
        }

        // Filter Chips Click Listeners
        View.OnClickListener chipListener = v -> {
            playUiFeedbackSound("tap");
            if (v == chipAll) {
                activeFilter[0] = "all";
                selectedTimeRange[0] = "all";
                if (textSelectedTimeRange != null) textSelectedTimeRange.setText("All Entries");
            } else if (v == chip1h) {
                activeFilter[0] = "1h";
                selectedTimeRange[0] = "1h";
                if (textSelectedTimeRange != null) textSelectedTimeRange.setText("Last 1 Hour");
            } else if (v == chip24h) {
                activeFilter[0] = "24h";
                selectedTimeRange[0] = "24h";
                if (textSelectedTimeRange != null) textSelectedTimeRange.setText("Last 24 Hours");
            } else if (v == chip1w) {
                activeFilter[0] = "1w";
                selectedTimeRange[0] = "1w";
                if (textSelectedTimeRange != null) textSelectedTimeRange.setText("Last 1 Week");
            }
            updateChipsVisuals.run();
            refreshList[0].run();
        };
        if (chipAll != null) chipAll.setOnClickListener(chipListener);
        if (chip1h != null) chip1h.setOnClickListener(chipListener);
        if (chip24h != null) chip24h.setOnClickListener(chipListener);
        if (chip1w != null) chip1w.setOnClickListener(chipListener);

        // Select Mode Toggle
        if (btnSelect != null) {
            btnSelect.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                isSelectMode[0] = !isSelectMode[0];
                if (isSelectMode[0]) {
                    btnSelect.setText("Done");
                    if (bottomNormalBar != null) bottomNormalBar.setVisibility(View.GONE);
                    if (bottomSelectBar != null) bottomSelectBar.setVisibility(View.VISIBLE);
                } else {
                    selectedIds.clear();
                    btnSelect.setText("Select");
                    if (bottomNormalBar != null) bottomNormalBar.setVisibility(View.VISIBLE);
                    if (bottomSelectBar != null) bottomSelectBar.setVisibility(View.GONE);
                }
                refreshList[0].run();
            });
        }

        // Options 3-dots Menu
        if (btnOptions != null) {
            btnOptions.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                PopupMenu popup = new PopupMenu(this, btnOptions);
                popup.getMenu().add("Select Multiple");
                popup.getMenu().add("Select All");
                popup.getMenu().add("Clear Entire History");
                popup.setOnMenuItemClickListener(item -> {
                    if ("Select Multiple".equals(item.getTitle())) {
                        isSelectMode[0] = true;
                        if (btnSelect != null) btnSelect.setText("Done");
                        if (bottomNormalBar != null) bottomNormalBar.setVisibility(View.GONE);
                        if (bottomSelectBar != null) bottomSelectBar.setVisibility(View.VISIBLE);
                        refreshList[0].run();
                    } else if ("Select All".equals(item.getTitle())) {
                        isSelectMode[0] = true;
                        if (btnSelect != null) btnSelect.setText("Done");
                        if (bottomNormalBar != null) bottomNormalBar.setVisibility(View.GONE);
                        if (bottomSelectBar != null) bottomSelectBar.setVisibility(View.VISIBLE);
                        selectedIds.clear();
                        for (HistoryManager.HistoryEntry e : currentVisibleEntries) selectedIds.add(e.id);
                        if (textSelectedCount != null) textSelectedCount.setText(selectedIds.size() + " selected");
                        if (btnSelectAllToggle != null) btnSelectAllToggle.setText("Deselect All");
                        refreshList[0].run();
                    } else if ("Clear Entire History".equals(item.getTitle())) {
                        new AlertDialog.Builder(this)
                                .setTitle("Clear History")
                                .setMessage("Are you sure you want to delete all browsing history?")
                                .setPositiveButton("Clear All", (d, w) -> {
                                    HistoryManager.getInstance(this).clearAllHistory();
                                    refreshList[0].run();
                                    Toast.makeText(this, "🗑️ All browsing history cleared", Toast.LENGTH_SHORT).show();
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                    }
                    return true;
                });
                popup.show();
            });
        }

        // Bottom Time Range Dropdown Selector (Custom Stitch Obsidian UI)
        View.OnClickListener timeRangePicker = v -> {
            playUiFeedbackSound("tap");
            com.google.android.material.bottomsheet.BottomSheetDialog pickerDialog =
                    new com.google.android.material.bottomsheet.BottomSheetDialog(this);
            View pickerView = LayoutInflater.from(this).inflate(R.layout.dialog_stitch_time_range_picker, null);
            GradientDrawable pickerBg = new GradientDrawable();
            pickerBg.setCornerRadii(new float[]{dpToPx(20), dpToPx(20), dpToPx(20), dpToPx(20), 0, 0, 0, 0});
            pickerBg.setColor(isDarkTheme ? 0xFF0A0E17 : 0xFFFFFFFF);
            pickerBg.setStroke(dpToPx(1), isDarkTheme ? 0xFF1E2433 : 0xFFE2E8F0);
            pickerView.setBackground(pickerBg);

            if (!isDarkTheme) {
                int[] optIds = new int[]{R.id.range_opt_1h, R.id.range_opt_24h, R.id.range_opt_1w, R.id.range_opt_all};
                for (int optId : optIds) {
                    View row = pickerView.findViewById(optId);
                    if (row instanceof ViewGroup) {
                        ViewGroup vg = (ViewGroup) row;
                        for (int c = 0; c < vg.getChildCount(); c++) {
                            View child = vg.getChildAt(c);
                            if (child instanceof ViewGroup) {
                                ViewGroup childVg = (ViewGroup) child;
                                for (int gc = 0; gc < childVg.getChildCount(); gc++) {
                                    View gChild = childVg.getChildAt(gc);
                                    if (gChild instanceof TextView) {
                                        TextView tv = (TextView) gChild;
                                        if (tv.getTextSize() > dpToPx(12)) {
                                            tv.setTextColor(0xFF0F172A);
                                        } else {
                                            tv.setTextColor(0xFF64748B);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            pickerDialog.setContentView(pickerView);
            if (pickerDialog.getWindow() != null) {
                View bs = pickerDialog.getWindow().findViewById(com.google.android.material.R.id.design_bottom_sheet);
                if (bs != null) bs.setBackgroundResource(android.R.color.transparent);
            }

            View check1h = pickerView.findViewById(R.id.check_range_1h);
            View check24h = pickerView.findViewById(R.id.check_range_24h);
            View check1w = pickerView.findViewById(R.id.check_range_1w);
            View checkAll = pickerView.findViewById(R.id.check_range_all);

            String cur = activeFilter[0];
            if (check1h != null) check1h.setVisibility("1h".equals(cur) ? View.VISIBLE : View.GONE);
            if (check24h != null) check24h.setVisibility("24h".equals(cur) ? View.VISIBLE : View.GONE);
            if (check1w != null) check1w.setVisibility("1w".equals(cur) ? View.VISIBLE : View.GONE);
            if (checkAll != null) checkAll.setVisibility("all".equals(cur) ? View.VISIBLE : View.GONE);

            View opt1h = pickerView.findViewById(R.id.range_opt_1h);
            View opt24h = pickerView.findViewById(R.id.range_opt_24h);
            View opt1w = pickerView.findViewById(R.id.range_opt_1w);
            View optAll = pickerView.findViewById(R.id.range_opt_all);

            View.OnClickListener optClick = optV -> {
                playUiFeedbackSound("tap");
                if (optV == opt1h) {
                    activeFilter[0] = "1h";
                    selectedTimeRange[0] = "1h";
                    if (textSelectedTimeRange != null) textSelectedTimeRange.setText("Last 1 Hour");
                } else if (optV == opt24h) {
                    activeFilter[0] = "24h";
                    selectedTimeRange[0] = "24h";
                    if (textSelectedTimeRange != null) textSelectedTimeRange.setText("Last 24 Hours");
                } else if (optV == opt1w) {
                    activeFilter[0] = "1w";
                    selectedTimeRange[0] = "1w";
                    if (textSelectedTimeRange != null) textSelectedTimeRange.setText("Last 1 Week");
                } else {
                    activeFilter[0] = "all";
                    selectedTimeRange[0] = "all";
                    if (textSelectedTimeRange != null) textSelectedTimeRange.setText("All Entries");
                }
                updateChipsVisuals.run();
                refreshList[0].run();
                pickerDialog.dismiss();
            };

            if (opt1h != null) opt1h.setOnClickListener(optClick);
            if (opt24h != null) opt24h.setOnClickListener(optClick);
            if (opt1w != null) opt1w.setOnClickListener(optClick);
            if (optAll != null) optAll.setOnClickListener(optClick);

            pickerDialog.show();
        };
        if (timeRangeSelector != null) timeRangeSelector.setOnClickListener(timeRangePicker);

        // Clear Buttons
        if (btnClearCookies != null) {
            btnClearCookies.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                HistoryManager.clearCookies();
                Toast.makeText(this, "🍪 Cookies cleared", Toast.LENGTH_SHORT).show();
            });
        }
        if (btnClearData != null) {
            btnClearData.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                HistoryManager.clearWebData();
                Toast.makeText(this, "🧹 Browsing cache & data cleared", Toast.LENGTH_SHORT).show();
            });
        }
        if (btnClearBoth != null) {
            btnClearBoth.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                long cutoff = 0;
                if ("1h".equals(selectedTimeRange[0])) cutoff = System.currentTimeMillis() - 3600_000L;
                else if ("24h".equals(selectedTimeRange[0])) cutoff = System.currentTimeMillis() - 86400_000L;
                else if ("1w".equals(selectedTimeRange[0])) cutoff = System.currentTimeMillis() - 7 * 86400_000L;

                if (cutoff > 0) {
                    HistoryManager.getInstance(this).clearHistorySince(cutoff);
                } else {
                    HistoryManager.getInstance(this).clearAllHistory();
                }
                HistoryManager.clearCookiesAndCache();
                refreshList[0].run();
                Toast.makeText(this, "✨ History, cookies & cache cleared", Toast.LENGTH_SHORT).show();
            });
        }

        // Selection Action Bar Controls
        if (btnSelectAllToggle != null) {
            btnSelectAllToggle.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                if (selectedIds.size() >= currentVisibleEntries.size() && !currentVisibleEntries.isEmpty()) {
                    selectedIds.clear();
                    btnSelectAllToggle.setText("Select All");
                } else {
                    for (HistoryManager.HistoryEntry e : currentVisibleEntries) {
                        selectedIds.add(e.id);
                    }
                    btnSelectAllToggle.setText("Deselect All");
                }
                if (textSelectedCount != null) textSelectedCount.setText(selectedIds.size() + " selected");
                refreshList[0].run();
            });
        }

        if (btnDeleteSelected != null) {
            btnDeleteSelected.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                if (selectedIds.isEmpty()) return;
                int count = selectedIds.size();
                HistoryManager.getInstance(this).deleteEntries(selectedIds);
                selectedIds.clear();
                isSelectMode[0] = false;
                if (btnSelect != null) btnSelect.setText("Select");
                if (bottomNormalBar != null) bottomNormalBar.setVisibility(View.VISIBLE);
                if (bottomSelectBar != null) bottomSelectBar.setVisibility(View.GONE);
                refreshList[0].run();
                Toast.makeText(this, "🗑️ Deleted " + count + " items", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnCancelSelect != null) {
            btnCancelSelect.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                selectedIds.clear();
                isSelectMode[0] = false;
                if (btnSelect != null) btnSelect.setText("Select");
                if (bottomNormalBar != null) bottomNormalBar.setVisibility(View.VISIBLE);
                if (bottomSelectBar != null) bottomSelectBar.setVisibility(View.GONE);
                refreshList[0].run();
            });
        }

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
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
        }
    }

    public int getPageZoom() {
        return currentTextZoom;
    }

    public void openDownloadsManagerModal() {
        runOnUiThread(() -> {
            isDownloadsModalOpen = true;
            sheetOverlayContainer.setVisibility(View.VISIBLE);
            sheetOverlayContainer.setClickable(true);
            sheetOverlayContainer.setFocusable(true);

            if (ytFloatingRemoteContainer != null) ytFloatingRemoteContainer.setVisibility(View.GONE);
            if (searchNavContainer != null) searchNavContainer.setVisibility(View.GONE);
            if (chatgptDockContainer != null) chatgptDockContainer.setVisibility(View.GONE);

            // Instantly activate standalone downloads mode in controlWebView so bottom sheet never flashes
            evaluateJavascriptInControlSheet("if (typeof window.openDownloadsModalStandalone === 'function') window.openDownloadsModalStandalone();");

            sheetBackdrop.animate().cancel();
            sheetBackdrop.setAlpha(0f);
            sheetBackdrop.animate().alpha(1f).setDuration(180).start();

            if (controlWebView != null) {
                if (!isSheetOpen) {
                    controlWebView.animate().cancel();
                    controlWebView.setPivotX(controlWebView.getWidth() / 2f);
                    controlWebView.setPivotY(controlWebView.getHeight() / 2f);
                    controlWebView.setScaleX(0.95f);
                    controlWebView.setScaleY(0.95f);
                    controlWebView.setAlpha(0f);
                    controlWebView.setTranslationY(0f);

                    controlWebView.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .alpha(1f)
                            .setDuration(180)
                            .setInterpolator(new DecelerateInterpolator(1.6f))
                            .start();
                }
            }
        });
    }

    public void hideDownloadsManagerModal() {
        runOnUiThread(() -> {
            isDownloadsModalOpen = false;
            if (isSheetOpen) {
                evaluateJavascriptInControlSheet("if (typeof window.onDownloadsStandaloneClosed === 'function') window.onDownloadsStandaloneClosed();");
                return;
            }

            sheetBackdrop.animate().cancel();
            sheetBackdrop.animate().alpha(0f).setDuration(150).start();

            if (controlWebView != null) {
                controlWebView.animate().cancel();
                controlWebView.animate()
                        .scaleX(0.92f)
                        .scaleY(0.92f)
                        .alpha(0f)
                        .setDuration(150)
                        .setInterpolator(new AccelerateInterpolator(1.6f))
                        .withEndAction(() -> {
                            sheetOverlayContainer.setVisibility(View.INVISIBLE);
                            sheetOverlayContainer.setClickable(false);
                            sheetOverlayContainer.setFocusable(false);
                            controlWebView.setScaleX(1f);
                            controlWebView.setScaleY(1f);
                            controlWebView.setAlpha(1f);
                            controlWebView.setTranslationY(0f);
                            evaluateJavascriptInControlSheet("if (typeof window.onDownloadsStandaloneClosed === 'function') window.onDownloadsStandaloneClosed();");
                        })
                        .start();
            } else {
                sheetOverlayContainer.setVisibility(View.INVISIBLE);
                sheetOverlayContainer.setClickable(false);
                sheetOverlayContainer.setFocusable(false);
            }
        });
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
        isDebugRecordingPaused = false;
        debugLogBuffer.setLength(0);
        debugLogBuffer.append("=== CASPIAN FLOW DIAGNOSTIC LOG ===\n");
        debugLogBuffer.append("Started: ").append(new Date().toString()).append("\n\n");
        Toast.makeText(this, "🔴 Diagnostic Recording Started", Toast.LENGTH_SHORT).show();
        updateLoggerNotification();
    }

    private final static int SAVE_LOG_REQUEST_CODE = 1003;
    private String pendingLogDataToSave = "";

    public void stopAndSaveDebugLog() {
        if (!isDebugRecording) return;
        isDebugRecording = false;
        try {
            NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID_LOGGER);
        } catch (Exception ignored) {}
        debugLogBuffer.append("\n=== END OF DIAGNOSTIC LOG ===\n");
        debugLogBuffer.append("Stopped: ").append(new Date().toString()).append("\n\n");
        debugLogBuffer.append("=== LOGCAT CAPTURE ===\n");

        try {
            Process process = Runtime.getRuntime().exec("logcat -d -v time");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            int count = 0;
            while ((line = bufferedReader.readLine()) != null && count < 3000) {
                debugLogBuffer.append(line).append("\n");
                count++;
            }
            bufferedReader.close();
        } catch (Exception e) {
            debugLogBuffer.append("Logcat read error: ").append(e.getMessage()).append("\n");
        }

        final String logData = debugLogBuffer.toString();
        pendingLogDataToSave = logData;
        final String fileName = "Caspian_BetaC_Log_" + System.currentTimeMillis() + ".txt";

        // 1. By default, save to public Downloads/Caspian/BetaC/Logs
        try {
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File caspianLogDir = new File(downloadsDir, "Caspian/BetaC/Logs");
            if (!caspianLogDir.exists()) caspianLogDir.mkdirs();
            File defaultLogFile = new File(caspianLogDir, fileName);
            FileOutputStream fos = new FileOutputStream(defaultLogFile);
            fos.write(logData.getBytes(StandardCharsets.UTF_8));
            fos.close();
            Log.d(TAG, "Log saved to default path: " + defaultLogFile.getAbsolutePath());
        } catch (Exception e) {
            Log.e(TAG, "Default download log save error: " + e.getMessage());
        }

        // 2. Prompt user with dialog for custom location / share
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("📋 Developer Log Captured");
        String message = "Log recorded (" + (logData.length() / 1024) + " KB).\n\n"
                + "📁 Auto-saved to:\nDownload/Caspian/BetaC/Logs/" + fileName;
        builder.setMessage(message);

        builder.setPositiveButton("💾 Save Custom Location", (dialog, which) -> {
            try {
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TITLE, fileName);
                startActivityForResult(intent, SAVE_LOG_REQUEST_CODE);
            } catch (Exception e) {
                Toast.makeText(this, "Could not open file picker: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNeutralButton("📤 Share / Copy", (dialog, which) -> {
            try {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Caspian BetaC Developer Log");
                shareIntent.putExtra(Intent.EXTRA_TEXT, logData);
                startActivity(Intent.createChooser(shareIntent, "Share or Copy Developer Log"));
            } catch (Exception e) {
                Toast.makeText(this, "Share failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("OK", (dialog, which) -> {
            dialog.dismiss();
            Toast.makeText(this, "✅ Log saved in Download/Caspian/BetaC/Logs", Toast.LENGTH_LONG).show();
        });

        builder.show();
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

        if (leftTab != null && leftTab.webView != null && leftTab.webView.getParent() != null) {
            ((ViewGroup) leftTab.webView.getParent()).removeView(leftTab.webView);
        }
        if (rightTab != null && rightTab.webView != null && rightTab.webView.getParent() != null) {
            ((ViewGroup) rightTab.webView.getParent()).removeView(rightTab.webView);
        }

        if (leftTab != null && rightTab != null) {
            leftTab.splitPartnerId = rightTab.id;
            leftTab.splitRole = "primary";
            leftTab.splitOrientation = splitModeState;

            rightTab.splitPartnerId = leftTab.id;
            rightTab.splitRole = "secondary";
            rightTab.splitOrientation = splitModeState;

            for (TabItem t : tabsList) {
                if (t != leftTab && t != rightTab) {
                    if (t.splitPartnerId == leftTab.id || t.splitPartnerId == rightTab.id) {
                        t.splitPartnerId = -1;
                        t.splitRole = "";
                        t.splitOrientation = 0;
                        t.splitName = "";
                    }
                }
            }
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
        injectAIPrompt(webView, prompt, true);
    }

    private void injectAIPrompt(WebView webView, String prompt, boolean autoSubmit) {
        if (webView == null || prompt == null || prompt.trim().isEmpty()) return;
        String js = "(function() {\n" +
                "  var txt = " + JSONObject.quote(prompt) + ";\n" +
                "  var autoSubmit = " + autoSubmit + ";\n" +
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
                "      } catch(e) {}\n" +
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
                "    try {\n" +
                "      input.focus();\n" +
                "      if (typeof window.getSelection === 'function') {\n" +
                "        var sel = window.getSelection();\n" +
                "        sel.selectAllChildren(input);\n" +
                "        sel.collapseToEnd();\n" +
                "      }\n" +
                "    } catch(e) {}\n" +
                "    if (!autoSubmit) return;\n" +
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

    public Bitmap getTabFaviconBitmap(TabItem tab) {
        if (tab == null) return null;
        try {
            if (tab.webView != null) {
                Bitmap fav = tab.webView.getFavicon();
                if (fav != null && !fav.isRecycled()) {
                    return fav;
                }
            }
            String b64 = tab.favicon64 != null ? tab.favicon64 : tab.faviconB64;
            if (b64 != null && b64.startsWith("data:image")) {
                int commaIdx = b64.indexOf(",");
                if (commaIdx != -1) {
                    String clean = b64.substring(commaIdx + 1);
                    byte[] bytes = android.util.Base64.decode(clean, android.util.Base64.DEFAULT);
                    Bitmap decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    if (decoded != null) return decoded;
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    public int getTabServiceIconRes(TabItem tab) {
        if (tab == null) return R.drawable.ic_tab_globe;
        String s = tab.service != null ? tab.service.toLowerCase() : "";
        String url = tab.url != null ? tab.url.toLowerCase() : "";
        if (s.contains("chatgpt") || url.contains("chatgpt.com") || url.contains("openai.com")) {
            return R.drawable.ic_platform_chatgpt;
        } else if (s.contains("gemini") || url.contains("gemini.google.com")) {
            return R.drawable.ic_platform_gemini;
        } else if (s.contains("claude") || url.contains("claude.ai")) {
            return R.drawable.ic_platform_claude;
        } else if (s.contains("youtube") || url.contains("youtube.com")) {
            return R.drawable.ic_platform_youtube;
        } else if (url.contains("google.com")) {
            return R.drawable.ic_platform_google;
        }
        return R.drawable.ic_tab_globe;
    }

    private void showSplitPaneMenu(View anchor, boolean isLeftPane) {
        if (anchor == null) return;
        View popupView = getLayoutInflater().inflate(R.layout.popup_split_pane_menu, null);

        TextView swapTitle = popupView.findViewById(R.id.split_menu_swap_title);
        TextView swapSub = popupView.findViewById(R.id.split_menu_swap_sub);
        TextView orientTitle = popupView.findViewById(R.id.split_menu_orientation_title);
        TextView orientSub = popupView.findViewById(R.id.split_menu_orientation_sub);
        TextView linksBadge = popupView.findViewById(R.id.split_menu_links_badge);

        if (splitModeState == 1) {
            if (swapTitle != null) swapTitle.setText("Swap Left & Right");
            if (swapSub != null) swapSub.setText("Invert left and right tabs");
            if (orientTitle != null) orientTitle.setText("Switch to Vertical");
            if (orientSub != null) orientSub.setText("Top and bottom layout");
        } else {
            if (swapTitle != null) swapTitle.setText("Swap Top & Bottom");
            if (swapSub != null) swapSub.setText("Invert top and bottom tabs");
            if (orientTitle != null) orientTitle.setText("Switch to Horizontal");
            if (orientSub != null) orientSub.setText("Side-by-side layout");
        }

        if (linksBadge != null) {
            if (openLeftLinksToRight) {
                linksBadge.setText("ON");
                linksBadge.setTextColor(Color.parseColor("#00E5FF"));
                linksBadge.setBackgroundResource(R.drawable.bg_pill_accent);
            } else {
                linksBadge.setText("OFF");
                linksBadge.setTextColor(Color.parseColor("#849396"));
                linksBadge.setBackgroundResource(R.drawable.bg_liquid_glass_pill);
            }
        }

        int targetWidth = dpToPx(270);
        PopupWindow popupWindow = new PopupWindow(
                popupView,
                targetWidth,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setElevation(dpToPx(80));

        View itemNewTab = popupView.findViewById(R.id.split_menu_item_new_tab);
        View itemSwitch = popupView.findViewById(R.id.split_menu_item_switch_tab);
        View itemSeparate = popupView.findViewById(R.id.split_menu_item_separate);
        View itemSwap = popupView.findViewById(R.id.split_menu_item_swap);
        View itemOrient = popupView.findViewById(R.id.split_menu_item_orientation);
        View itemLinks = popupView.findViewById(R.id.split_menu_item_links);

        if (itemNewTab != null) {
            itemNewTab.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                popupWindow.dismiss();
                TabItem targetTab = getTabById(isLeftPane ? activeTabId : secondarySplitTabId);
                if (targetTab != null) addNewTab(targetTab.service, "", targetTab.url, targetTab.isIncognito);
            });
        }

        if (itemSwitch != null) {
            itemSwitch.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                popupWindow.dismiss();
                showSwitchSplitTabDialog(isLeftPane);
            });
        }

        if (itemSeparate != null) {
            itemSeparate.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                popupWindow.dismiss();
                exitSplitView();
            });
        }

        if (itemSwap != null) {
            itemSwap.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                popupWindow.dismiss();
                swapSplitTabs();
            });
        }

        if (itemOrient != null) {
            itemOrient.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                popupWindow.dismiss();
                toggleSplitOrientation();
            });
        }

        if (itemLinks != null) {
            itemLinks.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                popupWindow.dismiss();
                openLeftLinksToRight = !openLeftLinksToRight;
                Toast.makeText(this, "Open left links to right: " + (openLeftLinksToRight ? "ENABLED" : "DISABLED"), Toast.LENGTH_SHORT).show();
            });
        }

        int[] location = new int[2];
        anchor.getLocationOnScreen(location);
        int anchorX = location[0];
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int xOffset = 0;
        if (anchorX + targetWidth > screenWidth - dpToPx(12)) {
            xOffset = -(targetWidth - anchor.getWidth());
        }
        popupWindow.showAsDropDown(anchor, xOffset, dpToPx(4));
    }

    private void showSwitchSplitTabDialog(boolean isLeftPane) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_switch_split_tab, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView titleView = dialogView.findViewById(R.id.dialog_split_tab_title);
        TextView subtitleView = dialogView.findViewById(R.id.dialog_split_tab_subtitle);
        ImageButton closeBtn = dialogView.findViewById(R.id.dialog_split_tab_close);
        EditText searchInput = dialogView.findViewById(R.id.dialog_split_tab_search);
        LinearLayout listContainer = dialogView.findViewById(R.id.dialog_split_tab_list);
        TextView emptyView = dialogView.findViewById(R.id.dialog_split_tab_empty);
        Button cancelBtn = dialogView.findViewById(R.id.btn_dialog_split_tab_cancel);

        String paneName = isLeftPane ? (splitModeState == 2 ? "Top" : "Left") : (splitModeState == 2 ? "Bottom" : "Right");
        if (titleView != null) {
            titleView.setText("Switch " + paneName + " Pane Tab");
        }
        if (subtitleView != null) {
            subtitleView.setText(tabsList.size() + " open tabs available");
        }

        int currentPaneTabId = isLeftPane ? activeTabId : secondarySplitTabId;
        int otherPaneTabId = isLeftPane ? secondarySplitTabId : activeTabId;

        Runnable populateList = () -> {
            listContainer.removeAllViews();
            String query = searchInput != null ? searchInput.getText().toString().trim().toLowerCase() : "";
            int visibleCount = 0;

            for (TabItem tab : tabsList) {
                String title = (tab.nickname != null && !tab.nickname.isEmpty()) ? tab.nickname : tab.title;
                if (title == null || title.isEmpty()) title = "Untitled Tab";
                String url = tab.url != null ? tab.url : "";
                String displayUrl = cleanDisplayUrl(url);

                if (!query.isEmpty()) {
                    boolean matches = title.toLowerCase().contains(query) || url.toLowerCase().contains(query) || displayUrl.toLowerCase().contains(query);
                    if (!matches) continue;
                }
                visibleCount++;

                boolean isCurrentPane = (tab.id == currentPaneTabId);
                boolean isOtherPane = (tab.id == otherPaneTabId);

                LinearLayout row = new LinearLayout(this);
                LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                rowLp.setMargins(0, 0, 0, dpToPx(8));
                row.setLayoutParams(rowLp);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setPadding(dpToPx(12), dpToPx(10), dpToPx(12), dpToPx(10));
                row.setBackgroundResource(isCurrentPane ? R.drawable.bg_caspian_card_active : R.drawable.bg_caspian_card);
                row.setClickable(true);
                row.setFocusable(true);

                ImageView faviconView = new ImageView(this);
                LinearLayout.LayoutParams favLp = new LinearLayout.LayoutParams(dpToPx(28), dpToPx(28));
                favLp.setMarginEnd(dpToPx(10));
                faviconView.setLayoutParams(favLp);
                faviconView.setScaleType(ImageView.ScaleType.FIT_CENTER);

                Bitmap favBmp = getTabFaviconBitmap(tab);
                if (favBmp != null) {
                    faviconView.setImageBitmap(favBmp);
                } else {
                    int serviceIcon = getTabServiceIconRes(tab);
                    faviconView.setImageResource(serviceIcon);
                    faviconView.setColorFilter(Color.parseColor("#00E5FF"));
                }
                row.addView(faviconView);

                LinearLayout textCol = new LinearLayout(this);
                LinearLayout.LayoutParams textLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                textCol.setLayoutParams(textLp);
                textCol.setOrientation(LinearLayout.VERTICAL);

                TextView tvTitle = new TextView(this);
                tvTitle.setText(title);
                tvTitle.setTextColor(isCurrentPane ? Color.parseColor("#00E5FF") : Color.parseColor("#DFE2F0"));
                tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
                tvTitle.setTypeface(null, Typeface.BOLD);
                tvTitle.setSingleLine(true);
                tvTitle.setEllipsize(TextUtils.TruncateAt.END);
                textCol.addView(tvTitle);

                TextView tvUrl = new TextView(this);
                tvUrl.setText(displayUrl);
                tvUrl.setTextColor(Color.parseColor("#849396"));
                tvUrl.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
                tvUrl.setSingleLine(true);
                tvUrl.setEllipsize(TextUtils.TruncateAt.END);
                textCol.addView(tvUrl);

                row.addView(textCol);

                if (isCurrentPane || isOtherPane) {
                    TextView badge = new TextView(this);
                    badge.setText(isCurrentPane ? "ACTIVE" : "OTHER");
                    badge.setTextColor(isCurrentPane ? Color.parseColor("#00E5FF") : Color.parseColor("#A855F7"));
                    badge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9);
                    badge.setTypeface(null, Typeface.BOLD);
                    badge.setPadding(dpToPx(8), dpToPx(3), dpToPx(8), dpToPx(3));
                    badge.setBackgroundResource(R.drawable.bg_liquid_glass_pill);
                    LinearLayout.LayoutParams badgeLp = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    badgeLp.setMarginStart(dpToPx(8));
                    badge.setLayoutParams(badgeLp);
                    row.addView(badge);
                }

                final int selectedId = tab.id;
                row.setOnClickListener(v -> {
                    playUiFeedbackSound("tap");
                    dialog.dismiss();
                    int oldPaneTabId = isLeftPane ? activeTabId : secondarySplitTabId;
                    if (oldPaneTabId != selectedId) {
                        TabItem oldTab = getTabById(oldPaneTabId);
                        if (oldTab != null) {
                            oldTab.splitPartnerId = -1;
                            oldTab.splitRole = "";
                            oldTab.splitOrientation = 0;
                            oldTab.splitName = "";
                        }
                    }
                    TabItem newlySelectedTab = getTabById(selectedId);
                    if (newlySelectedTab != null && newlySelectedTab.splitPartnerId != -1) {
                        TabItem oldPartner = getTabById(newlySelectedTab.splitPartnerId);
                        if (oldPartner != null) {
                            oldPartner.splitPartnerId = -1;
                            oldPartner.splitRole = "";
                            oldPartner.splitOrientation = 0;
                            oldPartner.splitName = "";
                        }
                    }
                    if (isLeftPane) {
                        activeTabId = selectedId;
                    } else {
                        secondarySplitTabId = selectedId;
                    }
                    applySplitViewLayout();
                    saveOpenTabsState();
                    updateControlSheetTabs();
                    Toast.makeText(this, "Switched pane tab", Toast.LENGTH_SHORT).show();
                });

                listContainer.addView(row);
            }

            if (emptyView != null) {
                emptyView.setVisibility(visibleCount == 0 ? View.VISIBLE : View.GONE);
            }
        };

        populateList.run();

        if (searchInput != null) {
            searchInput.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    populateList.run();
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        if (closeBtn != null) {
            closeBtn.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                dialog.dismiss();
            });
        }
        if (cancelBtn != null) {
            cancelBtn.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                dialog.dismiss();
            });
        }

        dialog.show();
        if (dialog.getWindow() != null) {
            int dialogWidth = (int) (getResources().getDisplayMetrics().widthPixels * 0.92f);
            int maxWidth = dpToPx(420);
            if (dialogWidth > maxWidth) dialogWidth = maxWidth;
            dialog.getWindow().setLayout(dialogWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
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

    public void renameSplitTabs(int tabId, String newName) {
        TabItem tab = getTabById(tabId);
        if (tab == null) return;
        String name = newName != null ? newName.trim() : "";
        tab.splitName = name;
        if (tab.splitPartnerId != -1) {
            TabItem partner = getTabById(tab.splitPartnerId);
            if (partner != null) {
                partner.splitName = name;
            }
        }
        saveOpenTabsState();
        updateControlSheetTabs();
    }

    public void deleteSplitTabs(int tabId) {
        TabItem tab = getTabById(tabId);
        if (tab == null) return;
        int partnerId = tab.splitPartnerId;
        separateSplitTabs(tabId);
        closeTab(tabId);
        if (partnerId != -1 && partnerId != tabId) {
            closeTab(partnerId);
        }
    }

    public void separateSplitTabs(int tabId) {
        TabItem tab = getTabById(tabId);
        if (tab == null) return;
        int partnerId = tab.splitPartnerId;
        TabItem partner = getTabById(partnerId);

        tab.splitPartnerId = -1;
        tab.splitRole = "";
        tab.splitOrientation = 0;
        tab.splitName = "";
        if (partner != null) {
            partner.splitPartnerId = -1;
            partner.splitRole = "";
            partner.splitOrientation = 0;
            partner.splitName = "";
        }

        if (splitModeState > 0 && (activeTabId == tabId || secondarySplitTabId == tabId ||
                (partner != null && (activeTabId == partner.id || secondarySplitTabId == partner.id)))) {
            splitModeState = 0;
            secondarySplitTabId = -1;
            activeTabId = tabId;
            switchToTab(tabId, false);
        }
        saveOpenTabsState();
        updateControlSheetTabs();
    }

    public void exitSplitView() {
        TabItem leftTab = getTabById(activeTabId);
        TabItem rightTab = getTabById(secondarySplitTabId);
        if (leftTab != null) {
            leftTab.splitPartnerId = -1;
            leftTab.splitRole = "";
            leftTab.splitOrientation = 0;
            leftTab.splitName = "";
        }
        if (rightTab != null) {
            rightTab.splitPartnerId = -1;
            rightTab.splitRole = "";
            rightTab.splitOrientation = 0;
            rightTab.splitName = "";
        }
        splitModeState = 0;
        secondarySplitTabId = -1;
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

        String sttEngine = prefs.getString("stt_engine_mode", "android_native");

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

            if ("whisper_on_device".equalsIgnoreCase(sttEngine)) {
                File whisperFile = getActiveWhisperModelFile();
                boolean modelReady = (whisperFile != null && whisperFile.exists() && whisperFile.length() > 5_000_000);
                if (!modelReady) {
                    Toast.makeText(this, "⚠️ Whisper Voice Pack not downloaded yet. Using Native Speech...", Toast.LENGTH_SHORT).show();
                    nativeSpeechBuffer.setLength(0);
                    setupNativeSpeechRecognizer();
                    if (speechRecognizer != null && speechIntent != null) {
                        speechRecognizer.startListening(speechIntent);
                    }
                    return;
                }
                startAudioRecording();
            } else {
                // Default: android_native
                nativeSpeechBuffer.setLength(0);
                setupNativeSpeechRecognizer();
                if (speechRecognizer != null && speechIntent != null) {
                    speechRecognizer.startListening(speechIntent);
                }
                Toast.makeText(this, "🎙️ Listening... Tap Mic / Action Button to Finish", Toast.LENGTH_SHORT).show();
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
        String sttEngine = prefs.getString("stt_engine_mode", "android_native");

        File whisperFile = getActiveWhisperModelFile();
        boolean whisperReady = (whisperFile != null && whisperFile.exists() && whisperFile.length() > 5_000_000);

        if ("android_native".equalsIgnoreCase(sttEngine) || !whisperReady) {
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
            byte[] pcmData = stopAudioRecordingAndGetPcm();
            processWhisperSpeech(pcmData);
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

    private byte[] stopAudioRecordingAndGetPcm() {
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
        return pcmAudioBuffer != null ? pcmAudioBuffer.toByteArray() : new byte[0];
    }

    private float[] pcm16ToFloat(byte[] pcmData) {
        if (pcmData == null || pcmData.length < 2) return new float[0];
        int numSamples = pcmData.length / 2;
        float[] floatSamples = new float[numSamples];
        for (int i = 0; i < numSamples; i++) {
            short sample = (short) ((pcmData[i * 2 + 1] << 8) | (pcmData[i * 2] & 0xff));
            floatSamples[i] = sample / 32768.0f;
        }

        // Fast silence trimming to eliminate empty frames for Whisper
        int start = 0;
        while (start < numSamples && Math.abs(floatSamples[start]) < 0.012f) {
            start++;
        }
        int end = numSamples - 1;
        while (end > start && Math.abs(floatSamples[end]) < 0.012f) {
            end--;
        }
        start = Math.max(0, start - 3200);
        end = Math.min(numSamples - 1, end + 3200);
        if (end - start > 1600) {
            float[] trimmed = new float[end - start + 1];
            System.arraycopy(floatSamples, start, trimmed, 0, trimmed.length);
            return trimmed;
        }
        return floatSamples;
    }

    private void processWhisperSpeech(byte[] pcmData) {
        if (pcmData == null || pcmData.length < 3200) {
            runOnUiThread(() -> Toast.makeText(this, "⚠️ Audio too short.", Toast.LENGTH_SHORT).show());
            return;
        }

        File modelFile = getActiveWhisperModelFile();
        if (modelFile == null || !modelFile.exists() || modelFile.length() < 5_000_000) {
            runOnUiThread(() -> Toast.makeText(this, "⚠️ Whisper model pack not ready.", Toast.LENGTH_SHORT).show());
            return;
        }

        runOnUiThread(() -> Toast.makeText(this, "🧠 Whisper transcribing...", Toast.LENGTH_SHORT).show());

        new Thread(() -> {
            long ctx = 0;
            try {
                if (!cz.vytvarenicher.whisper.WhisperLib.isAvailable()) {
                    runOnUiThread(() -> Toast.makeText(this, "⚠️ Whisper native engine unavailable on this device", Toast.LENGTH_LONG).show());
                    return;
                }

                float[] samples = pcm16ToFloat(pcmData);
                ctx = cz.vytvarenicher.whisper.WhisperLib.initFromFile(modelFile.getAbsolutePath());
                if (ctx == 0) {
                    runOnUiThread(() -> Toast.makeText(this, "⚠️ Failed to initialize Whisper model", Toast.LENGTH_LONG).show());
                    return;
                }

                String transcribed = cz.vytvarenicher.whisper.WhisperLib.transcribe(ctx, samples, "en");
                if (transcribed != null) {
                    final String finalText = transcribed.trim();
                    if (!finalText.isEmpty()) {
                        runOnUiThread(() -> {
                            if (isUniversalVoiceActive) {
                                handleUniversalSpeechText(finalText);
                            } else {
                                omniboxEditText.setText(finalText);
                                handleOmniboxSubmission(finalText);
                            }
                        });
                    } else {
                        runOnUiThread(() -> Toast.makeText(this, "⚠️ Whisper: No speech detected", Toast.LENGTH_SHORT).show());
                    }
                }
            } catch (Throwable t) {
                Log.e(TAG, "Whisper transcription failed: " + t.getMessage(), t);
                runOnUiThread(() -> Toast.makeText(this, "⚠️ Whisper error: " + t.getMessage(), Toast.LENGTH_LONG).show());
            } finally {
                if (ctx != 0) {
                    try { cz.vytvarenicher.whisper.WhisperLib.free(ctx); } catch (Exception ignored) {}
                }
            }
        }).start();
    }

    public File getWhisperModelFile(String tier) {
        File modelsDir = new File(getFilesDir(), "models");
        if (!modelsDir.exists()) modelsDir.mkdirs();
        String filename = "base".equalsIgnoreCase(tier) ? WHISPER_BASE_FILENAME : WHISPER_TINY_FILENAME;
        return new File(modelsDir, filename);
    }

    public File getActiveWhisperModelFile() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String tier = prefs.getString("whisper_selected_tier", "tiny");
        File file = getWhisperModelFile(tier);
        long minExpected = "base".equalsIgnoreCase(tier) ? 40_000_000 : 5_000_000;
        if (file.exists() && file.length() > minExpected) return file;
        File fallback = getWhisperModelFile("tiny".equalsIgnoreCase(tier) ? "base" : "tiny");
        long fallbackExpected = "tiny".equalsIgnoreCase(tier) ? 40_000_000 : 5_000_000;
        if (fallback.exists() && fallback.length() > fallbackExpected) return fallback;
        return file;
    }

    public String getWhisperModelStatus(String tier) {
        final String selectedTier = ("base".equalsIgnoreCase(tier)) ? "base" : "tiny";
        if (isWhisperDownloading && selectedTier.equalsIgnoreCase(currentlyDownloadingTier)) {
            return "DOWNLOADING";
        }
        File file = getWhisperModelFile(selectedTier);
        long minExpected = "base".equalsIgnoreCase(selectedTier) ? 40_000_000 : 5_000_000;
        if (file != null && file.exists() && file.length() > minExpected) {
            return "READY";
        }
        return "NOT_DOWNLOADED";
    }

    public long getWhisperModelSize(String tier) {
        final String selectedTier = ("base".equalsIgnoreCase(tier)) ? "base" : "tiny";
        File file = getWhisperModelFile(selectedTier);
        if (file != null && file.exists()) {
            return file.length();
        }
        return 0;
    }

    public void downloadWhisperModel(String tier) {
        if (isWhisperDownloading) {
            Toast.makeText(this, "⏳ Whisper model is already downloading...", Toast.LENGTH_SHORT).show();
            return;
        }
        final String selectedTier = ("base".equalsIgnoreCase(tier)) ? "base" : "tiny";
        currentlyDownloadingTier = selectedTier;
        isWhisperDownloading = true;
        whisperDownloadProgress = 0;
        String tierLabel = "base".equalsIgnoreCase(selectedTier) ? "Base (~75 MB)" : "Tiny (~39 MB)";
        Toast.makeText(this, "📥 Starting Whisper " + tierLabel + " download...", Toast.LENGTH_SHORT).show();
        notifyWhisperProgress(0, "DOWNLOADING", selectedTier);

        new Thread(() -> {
            File modelFile = getWhisperModelFile(selectedTier);
            File tempFile = new File(modelFile.getParentFile(), modelFile.getName() + ".tmp");
            HttpURLConnection conn = null;
            InputStream in = null;
            FileOutputStream out = null;
            try {
                String downloadUrl = "base".equalsIgnoreCase(selectedTier) ? WHISPER_BASE_URL : WHISPER_TINY_URL;
                URL url = new URL(downloadUrl);
                conn = (HttpURLConnection) url.openConnection();
                conn.setInstanceFollowRedirects(true);
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(30000);
                conn.connect();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_MOVED_PERM || responseCode == HttpURLConnection.HTTP_MOVED_TEMP || responseCode == 307 || responseCode == 308) {
                    String newUrl = conn.getHeaderField("Location");
                    conn.disconnect();
                    conn = (HttpURLConnection) new URL(newUrl).openConnection();
                    conn.setConnectTimeout(15000);
                    conn.setReadTimeout(30000);
                    conn.connect();
                }

                long contentLength = conn.getContentLengthLong();
                in = conn.getInputStream();
                out = new FileOutputStream(tempFile);

                byte[] buffer = new byte[8192];
                long totalRead = 0;
                int bytesRead;
                int lastReportedPercent = 0;

                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                    totalRead += bytesRead;
                    if (contentLength > 0) {
                        int percent = (int) ((totalRead * 100) / contentLength);
                        if (percent > lastReportedPercent) {
                            lastReportedPercent = percent;
                            whisperDownloadProgress = percent;
                            notifyWhisperProgress(percent, "DOWNLOADING", selectedTier);
                        }
                    }
                }
                out.flush();
                out.close();
                out = null;
                in.close();
                in = null;

                long minExpected = "base".equalsIgnoreCase(selectedTier) ? 40_000_000 : 5_000_000;
                if (tempFile.exists() && tempFile.length() > minExpected) {
                    if (modelFile.exists()) modelFile.delete();
                    tempFile.renameTo(modelFile);
                    isWhisperDownloading = false;
                    notifyWhisperProgress(100, "COMPLETED", selectedTier);
                    runOnUiThread(() -> Toast.makeText(this, "✅ Whisper " + tierLabel + " Downloaded & Ready!", Toast.LENGTH_LONG).show());
                } else {
                    throw new IOException("Downloaded file is incomplete or corrupted");
                }
            } catch (Exception e) {
                isWhisperDownloading = false;
                if (tempFile.exists()) tempFile.delete();
                Log.e(TAG, "Failed to download whisper model (" + selectedTier + "): " + e.getMessage());
                notifyWhisperProgress(0, "ERROR", selectedTier);
                runOnUiThread(() -> Toast.makeText(this, "⚠️ Voice Pack download failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
            } finally {
                try { if (in != null) in.close(); } catch (Exception ignored) {}
                try { if (out != null) out.close(); } catch (Exception ignored) {}
                if (conn != null) conn.disconnect();
            }
        }).start();
    }

    public boolean deleteWhisperModel(String tier) {
        final String selectedTier = ("base".equalsIgnoreCase(tier)) ? "base" : "tiny";
        File modelFile = getWhisperModelFile(selectedTier);
        boolean deleted = false;
        if (modelFile != null && modelFile.exists()) {
            deleted = modelFile.delete();
        }
        if (modelFile != null && modelFile.getParentFile() != null) {
            File tempFile = new File(modelFile.getParentFile(), modelFile.getName() + ".tmp");
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
        isWhisperDownloading = false;
        notifyWhisperProgress(0, "NOT_DOWNLOADED", selectedTier);
        String tierLabel = "base".equalsIgnoreCase(selectedTier) ? "Base (~75 MB)" : "Tiny (~39 MB)";
        runOnUiThread(() -> Toast.makeText(this, "🗑️ Whisper " + tierLabel + " removed", Toast.LENGTH_SHORT).show());
        return deleted;
    }

    private void notifyWhisperProgress(int percent, String status) {
        notifyWhisperProgress(percent, status, currentlyDownloadingTier);
    }

    private void notifyWhisperProgress(int percent, String status, String tier) {
        final String safeTier = (tier != null) ? tier : "tiny";
        runOnUiThread(() -> {
            if (controlWebView != null) {
                controlWebView.evaluateJavascript("if (typeof window.onWhisperDownloadProgress === 'function') { window.onWhisperDownloadProgress(" + percent + ", '" + status + "', '" + safeTier + "'); }", null);
            }
        });
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

    public void openCaspianUpdateMenu() {
        runOnUiThread(() -> {
            try {
                openControlSheet();
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    evaluateJavascriptInControlSheet(
                            "try {" +
                            "  var el = document.getElementById('card-app-updater');" +
                            "  if (el) el.scrollIntoView({ behavior: 'smooth', block: 'center' });" +
                            "  var btn = document.getElementById('check-updates-btn');" +
                            "  if (btn) btn.click();" +
                            "} catch(e) {}"
                    );
                }, 350);
            } catch (Throwable t) {
                Log.e(TAG, "openCaspianUpdateMenu error", t);
            }
        });
    }

    public void showCaskSwitcherDialog(TabItem currentTab) {
        try {
            CaskManager cm = new CaskManager(this);
            List<CaskManager.CaskItem> casks = cm.getAllCasks();
            if (casks == null || casks.isEmpty()) return;

            String activeCaskId = (currentTab != null && currentTab.caskId != null) ? currentTab.caskId : cm.getActiveCaskId();

            com.google.android.material.bottomsheet.BottomSheetDialog caskDialog =
                    new com.google.android.material.bottomsheet.BottomSheetDialog(this);
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(dpToPx(16), dpToPx(14), dpToPx(16), dpToPx(20));

            GradientDrawable rootBg = new GradientDrawable();
            rootBg.setColor(!isDarkTheme ? 0xFFFFFFFF : 0xFF0D1117);
            rootBg.setCornerRadii(new float[]{dpToPx(24), dpToPx(24), dpToPx(24), dpToPx(24), 0, 0, 0, 0});
            layout.setBackground(rootBg);

            View handle = new View(this);
            LinearLayout.LayoutParams hLp = new LinearLayout.LayoutParams(dpToPx(44), dpToPx(5));
            hLp.gravity = Gravity.CENTER_HORIZONTAL;
            hLp.bottomMargin = dpToPx(14);
            handle.setLayoutParams(hLp);
            GradientDrawable hBg = new GradientDrawable();
            hBg.setColor(!isDarkTheme ? 0xFFCBD5E1 : 0xFF30363D);
            hBg.setCornerRadius(dpToPx(3));
            handle.setBackground(hBg);
            layout.addView(handle);

            TextView title = new TextView(this);
            title.setText("Switch Caspian Cask");
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
            title.setTypeface(Typeface.DEFAULT_BOLD);
            title.setTextColor(!isDarkTheme ? 0xFF0F172A : 0xFFDFE2F0);
            title.setPadding(dpToPx(4), 0, dpToPx(4), dpToPx(4));
            layout.addView(title);

            TextView sub = new TextView(this);
            sub.setText("Each Cask maintains its own isolated cookie & session vault");
            sub.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
            sub.setTextColor(!isDarkTheme ? 0xFF64748B : 0xFF94A3B8);
            sub.setPadding(dpToPx(4), 0, dpToPx(4), dpToPx(14));
            layout.addView(sub);

            ScrollView sv = new ScrollView(this);
            LinearLayout itemsLayout = new LinearLayout(this);
            itemsLayout.setOrientation(LinearLayout.VERTICAL);

            for (CaskManager.CaskItem cask : casks) {
                boolean isCurrent = cask.id.equals(activeCaskId);
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setPadding(dpToPx(14), dpToPx(12), dpToPx(14), dpToPx(12));
                LinearLayout.LayoutParams rLp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                rLp.bottomMargin = dpToPx(8);
                row.setLayoutParams(rLp);

                GradientDrawable rBg = new GradientDrawable();
                if (isCurrent) {
                    rBg.setColor(!isDarkTheme ? 0xFFE0F2FE : 0xFF0B293B);
                    rBg.setStroke(dpToPx(2), !isDarkTheme ? 0xFF0284C7 : 0xFF00E5FF);
                } else {
                    rBg.setColor(!isDarkTheme ? 0xFFF8FAFC : 0xFF161B22);
                    rBg.setStroke(dpToPx(1), !isDarkTheme ? 0xFFE2E8F0 : 0xFF21262D);
                }
                rBg.setCornerRadius(dpToPx(14));
                row.setBackground(rBg);

                TextView tvIcon = new TextView(this);
                tvIcon.setText(cask.icon != null ? cask.icon : "🌊");
                tvIcon.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f);
                tvIcon.setPadding(0, 0, dpToPx(12), 0);
                row.addView(tvIcon);

                LinearLayout nameBox = new LinearLayout(this);
                nameBox.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams nbLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                nameBox.setLayoutParams(nbLp);

                TextView tvName = new TextView(this);
                tvName.setText(cask.name);
                tvName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
                tvName.setTypeface(Typeface.DEFAULT_BOLD);
                tvName.setTextColor(!isDarkTheme ? (isCurrent ? 0xFF0284C7 : 0xFF0F172A) : (isCurrent ? 0xFF00E5FF : 0xFFDFE2F0));
                nameBox.addView(tvName);

                if (cask.isDefault) {
                    TextView tvDef = new TextView(this);
                    tvDef.setText("Default Vault");
                    tvDef.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f);
                    tvDef.setTextColor(!isDarkTheme ? 0xFF64748B : 0xFF94A3B8);
                    nameBox.addView(tvDef);
                }
                row.addView(nameBox);

                if (isCurrent) {
                    TextView check = new TextView(this);
                    check.setText("✓ ACTIVE");
                    check.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
                    check.setTypeface(Typeface.DEFAULT_BOLD);
                    check.setTextColor(!isDarkTheme ? 0xFF0284C7 : 0xFF00E5FF);
                    row.addView(check);
                }

                row.setOnClickListener(v -> {
                    caskDialog.dismiss();
                    playUiFeedbackSound("tap");
                    cm.switchCask(cask.id, null);
                    if (currentTab != null) {
                        changeTabCask(currentTab.id, cask.id);
                    }
                });

                itemsLayout.addView(row);
            }

            sv.addView(itemsLayout);
            layout.addView(sv);

            caskDialog.setContentView(layout);
            if (caskDialog.getWindow() != null) {
                View bs = caskDialog.getWindow().findViewById(com.google.android.material.R.id.design_bottom_sheet);
                if (bs != null) bs.setBackgroundResource(android.R.color.transparent);
            }
            caskDialog.show();
        } catch (Throwable t) {
            Log.e(TAG, "showCaskSwitcherDialog error", t);
        }
    }

    public void showPrintAndExportDialog(TabItem currentTab) {
        try {
            com.google.android.material.bottomsheet.BottomSheetDialog printDialog =
                    new com.google.android.material.bottomsheet.BottomSheetDialog(this);
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(dpToPx(16), dpToPx(14), dpToPx(16), dpToPx(20));

            GradientDrawable rootBg = new GradientDrawable();
            rootBg.setColor(!isDarkTheme ? 0xFFFFFFFF : 0xFF0D1117);
            rootBg.setCornerRadii(new float[]{dpToPx(24), dpToPx(24), dpToPx(24), dpToPx(24), 0, 0, 0, 0});
            layout.setBackground(rootBg);

            View handle = new View(this);
            LinearLayout.LayoutParams hLp = new LinearLayout.LayoutParams(dpToPx(44), dpToPx(5));
            hLp.gravity = Gravity.CENTER_HORIZONTAL;
            hLp.bottomMargin = dpToPx(14);
            handle.setLayoutParams(hLp);
            GradientDrawable hBg = new GradientDrawable();
            hBg.setColor(!isDarkTheme ? 0xFFCBD5E1 : 0xFF30363D);
            hBg.setCornerRadius(dpToPx(3));
            handle.setBackground(hBg);
            layout.addView(handle);

            TextView title = new TextView(this);
            title.setText("Print & Export");
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
            title.setTypeface(Typeface.DEFAULT_BOLD);
            title.setTextColor(!isDarkTheme ? 0xFF0F172A : 0xFFDFE2F0);
            title.setPadding(dpToPx(4), 0, dpToPx(4), dpToPx(4));
            layout.addView(title);

            TextView sub = new TextView(this);
            sub.setText("Print or export web pages and AI conversations in various formats");
            sub.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
            sub.setTextColor(!isDarkTheme ? 0xFF64748B : 0xFF94A3B8);
            sub.setPadding(dpToPx(4), 0, dpToPx(4), dpToPx(14));
            layout.addView(sub);

            class ExportOpt {
                String icon, title, desc, action;
                ExportOpt(String icon, String title, String desc, String action) {
                    this.icon = icon; this.title = title; this.desc = desc; this.action = action;
                }
            }

            List<ExportOpt> options = Arrays.asList(
                    new ExportOpt("🖨️", "Print Page (Android System Print)", "Send document to connected printers or save as system PDF", "print_system"),
                    new ExportOpt("📄", "Save as PDF (.pdf)", "Clean, styled PDF document with reader formatting", "styledpdf"),
                    new ExportOpt("📝", "Markdown File (.md)", "Universal Markdown file for notes, Obsidian, and Notion", "md"),
                    new ExportOpt("📄", "Plain Text Transcript (.txt)", "Raw text without styling for lightweight sharing", "txt"),
                    new ExportOpt("📑", "Word Document (.doc)", "Microsoft Word compatible formatted document", "doc"),
                    new ExportOpt("🤖", "Convert Chat to Another AI", "Port conversation to ChatGPT, Claude, Gemini, or Grok", "convert")
            );

            for (ExportOpt opt : options) {
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setPadding(dpToPx(12), dpToPx(10), dpToPx(12), dpToPx(10));
                LinearLayout.LayoutParams rLp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                rLp.bottomMargin = dpToPx(6);
                row.setLayoutParams(rLp);

                GradientDrawable rBg = new GradientDrawable();
                rBg.setColor(!isDarkTheme ? 0xFFF8FAFC : 0xFF161B22);
                rBg.setStroke(dpToPx(1), !isDarkTheme ? 0xFFE2E8F0 : 0xFF21262D);
                rBg.setCornerRadius(dpToPx(14));
                row.setBackground(rBg);

                TextView tvIcon = new TextView(this);
                tvIcon.setText(opt.icon);
                tvIcon.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f);
                tvIcon.setPadding(0, 0, dpToPx(12), 0);
                row.addView(tvIcon);

                LinearLayout textBox = new LinearLayout(this);
                textBox.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams tbLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                textBox.setLayoutParams(tbLp);

                TextView tvT = new TextView(this);
                tvT.setText(opt.title);
                tvT.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
                tvT.setTypeface(Typeface.DEFAULT_BOLD);
                tvT.setTextColor(!isDarkTheme ? 0xFF0F172A : 0xFFDFE2F0);
                textBox.addView(tvT);

                TextView tvD = new TextView(this);
                tvD.setText(opt.desc);
                tvD.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f);
                tvD.setTextColor(!isDarkTheme ? 0xFF64748B : 0xFF94A3B8);
                textBox.addView(tvD);

                row.addView(textBox);

                row.setOnClickListener(v -> {
                    printDialog.dismiss();
                    playUiFeedbackSound("tap");
                    if ("print_system".equals(opt.action)) {
                        printActivePageViaSystem();
                    } else {
                        performExportOnMainWebView(opt.action);
                    }
                });

                layout.addView(row);
            }

            printDialog.setContentView(layout);
            if (printDialog.getWindow() != null) {
                View bs = printDialog.getWindow().findViewById(com.google.android.material.R.id.design_bottom_sheet);
                if (bs != null) bs.setBackgroundResource(android.R.color.transparent);
            }
            printDialog.show();
        } catch (Throwable t) {
            Log.e(TAG, "showPrintAndExportDialog error", t);
        }
    }

    public void printActivePageViaSystem() {
        runOnUiThread(() -> {
            try {
                TabItem currentTab = getActiveOrDominantTab();
                if (currentTab != null && currentTab.webView != null) {
                    PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
                    String jobName = (currentTab.title != null && !currentTab.title.trim().isEmpty())
                            ? currentTab.title
                            : "Caspian Document";
                    PrintDocumentAdapter printAdapter = currentTab.webView.createPrintDocumentAdapter(jobName);
                    if (printManager != null) {
                        printManager.print(jobName, printAdapter, new PrintAttributes.Builder().build());
                    }
                } else {
                    Toast.makeText(this, "No active page to print", Toast.LENGTH_SHORT).show();
                }
            } catch (Throwable t) {
                Log.e(TAG, "printActivePageViaSystem error", t);
                Toast.makeText(this, "Print error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showExportOptions() {
        showPrintAndExportDialog(getActiveOrDominantTab());
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
                "        var json = null;\n" +
                "\n" +
                "        // 1. Check in-memory unpruned conversation cache from interceptor\n" +
                "        if (window.__CASPIAN_RAW_CONVERSATION_CACHE && window.__CASPIAN_RAW_CONVERSATION_CACHE[convoId]) {\n" +
                "          json = window.__CASPIAN_RAW_CONVERSATION_CACHE[convoId];\n" +
                "        }\n" +
                "\n" +
                "        // 2. Fetch directly via un-intercepted native fetch\n" +
                "        if (!json) {\n" +
                "          var token = null;\n" +
                "          try {\n" +
                "            if (window.__NEXT_DATA__ && window.__NEXT_DATA__.props && window.__NEXT_DATA__.props.pageProps) {\n" +
                "              token = window.__NEXT_DATA__.props.pageProps.accessToken;\n" +
                "            }\n" +
                "          } catch(ne) {}\n" +
                "\n" +
                "          if (!token) {\n" +
                "            try {\n" +
                "              var sessionResp = await fetch('/api/auth/session', { credentials: 'include' });\n" +
                "              if (sessionResp.ok) {\n" +
                "                var sessionData = await sessionResp.json();\n" +
                "                token = sessionData.accessToken;\n" +
                "              }\n" +
                "            } catch(se) {}\n" +
                "          }\n" +
                "\n" +
                "          var nativeFetch = window.__CASPIAN_NATIVE_FETCH || window.fetch;\n" +
                "          var fetchHeaders = {\n" +
                "            'Accept': 'application/json',\n" +
                "            'X-Caspian-Export': 'full'\n" +
                "          };\n" +
                "          if (token) fetchHeaders['Authorization'] = 'Bearer ' + token;\n" +
                "\n" +
                "          var resp = await nativeFetch('/backend-api/conversation/' + convoId, {\n" +
                "            headers: fetchHeaders,\n" +
                "            credentials: 'include'\n" +
                "          });\n" +
                "          if (resp.ok) {\n" +
                "            json = await resp.json();\n" +
                "          }\n" +
                "        }\n" +
                "\n" +
                "        if (json && json.mapping) {\n" +
                "          var activeNodes = [];\n" +
                "          if (json.current_node && json.mapping[json.current_node]) {\n" +
                "            var currId = json.current_node;\n" +
                "            var visited = new Set();\n" +
                "            while (currId && json.mapping[currId] && !visited.has(currId)) {\n" +
                "              visited.add(currId);\n" +
                "              var node = json.mapping[currId];\n" +
                "              if (node && node.message && node.message.content && node.message.content.parts) {\n" +
                "                var author = (node.message.author && node.message.author.role) ? node.message.author.role : 'assistant';\n" +
                "                if (author === 'user' || author === 'assistant') {\n" +
                "                  var parts = node.message.content.parts;\n" +
                "                  var textContent = parts.map(function(p){ return (typeof p === 'string') ? p : JSON.stringify(p); }).join('\\n').trim();\n" +
                "                  if (textContent) {\n" +
                "                    activeNodes.unshift({\n" +
                "                      author: author,\n" +
                "                      text: textContent\n" +
                "                    });\n" +
                "                  }\n" +
                "                }\n" +
                "              }\n" +
                "              currId = node.parent;\n" +
                "            }\n" +
                "          }\n" +
                "\n" +
                "          // Fallback: sort mapping nodes if current_node was not found or yielded 0 nodes\n" +
                "          if (activeNodes.length === 0) {\n" +
                "            var map = json.mapping;\n" +
                "            for (var key in map) {\n" +
                "              var n = map[key];\n" +
                "              if (n && n.message && n.message.content && n.message.content.parts) {\n" +
                "                var a = (n.message.author && n.message.author.role) ? n.message.author.role : 'assistant';\n" +
                "                if (a === 'user' || a === 'assistant') {\n" +
                "                  var ps = n.message.content.parts;\n" +
                "                  var tc = ps.map(function(p){ return (typeof p === 'string') ? p : JSON.stringify(p); }).join('\\n').trim();\n" +
                "                  if (tc) {\n" +
                "                    activeNodes.push({\n" +
                "                      create_time: n.message.create_time || 0,\n" +
                "                      author: a,\n" +
                "                      text: tc\n" +
                "                    });\n" +
                "                  }\n" +
                "                }\n" +
                "              }\n" +
                "            }\n" +
                "            activeNodes.sort(function(a,b){ return (a.create_time || 0) - (b.create_time || 0); });\n" +
                "          }\n" +
                "\n" +
                "          for (var k = 0; k < activeNodes.length; k++) {\n" +
                "            if (!seen.has(activeNodes[k].text)) {\n" +
                "              seen.add(activeNodes[k].text);\n" +
                "              var parsedHtml = parseMarkdownAndLaTeX(activeNodes[k].text);\n" +
                "              var localImgs = await getTurnImages(turns.length);\n" +
                "              localImgs.forEach(function(b64) {\n" +
                "                parsedHtml += '<div style=\"margin-top:12px; text-align:center;\"><img src=\"' + b64 + '\" style=\"max-width:100%; border-radius:8px; box-shadow:0 4px 12px rgba(0,0,0,0.1);\" /></div>';\n" +
                "              });\n" +
                "              turns.push({\n" +
                "                index: turns.length + 1,\n" +
                "                author: activeNodes[k].author,\n" +
                "                role: activeNodes[k].author === 'user' ? 'User' : 'ChatGPT',\n" +
                "                text: activeNodes[k].text,\n" +
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
                "            text = (textDiv.innerText || textDiv.textContent || '').trim();\n" +
                "          } else {\n" +
                "            var contentDiv = row.querySelector('.content, div.text-token-text-primary');\n" +
                "            text = contentDiv ? (contentDiv.innerText || contentDiv.textContent || '').trim() : (row.innerText || row.textContent || '').trim();\n" +
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
                    TabItem currentTab = getActiveOrDominantTab();
                    if (currentTab == null) currentTab = getTabById(activeTabId);
                    if (currentTab != null && currentTab.webView != null && currentTab.webView.getUrl() != null) {
                        String curUrl = currentTab.webView.getUrl().toLowerCase(Locale.ROOT);
                        if (curUrl.contains("gemini")) {
                            sourceService = "gemini";
                        } else if (curUrl.contains("chatgpt")) {
                            sourceService = "chatgpt";
                        } else if (currentTab.service != null && !currentTab.service.isEmpty()) {
                            sourceService = currentTab.service;
                        }
                    } else if (currentTab != null && currentTab.service != null && !currentTab.service.isEmpty()) {
                        sourceService = currentTab.service;
                    } else if (turnsArray.length() > 0) {
                        sourceService = turnsArray.getJSONObject(0).optString("service", "chatgpt");
                    }
                    createNewTabWithPrefill(sourceService, sb.toString());
                    Toast.makeText(this, "Copied context to clipboard & opened in new " + ("gemini".equalsIgnoreCase(sourceService) ? "Gemini" : "ChatGPT") + " tab!", Toast.LENGTH_LONG).show();
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
        String targetUrl = "https://chatgpt.com";
        String targetService = "chatgpt";
        if ("gemini".equalsIgnoreCase(sourceService)) {
            targetUrl = "https://gemini.google.com/app";
            targetService = "gemini";
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

    private TabItem createNewTabInstance(int id, String url, String service, String promptPayload, boolean isIncognito) {
        return createNewTabInstance(id, url, service, promptPayload, isIncognito, null);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private TabItem createNewTabInstance(int id, String url, String service, String promptPayload, boolean isIncognito, String targetCaskId) {
        CaskManager cm = new CaskManager(this);
        String finalCaskId = (targetCaskId != null && !targetCaskId.trim().isEmpty()) ? targetCaskId : cm.getActiveCaskId();
        CaskManager.CaskItem cask = cm.getCaskById(finalCaskId);

        CaspianWebView webView = new CaspianWebView(this);
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
        settings.setMediaPlaybackRequiresUserGesture(false);

        if (!isIncognito) {
            settings.setCacheMode(WebSettings.LOAD_DEFAULT);
            CaskManager.applyProfileToWebView(webView, finalCaskId);
            CookieManager.getInstance().setAcceptCookie(true);
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        } else {
            settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        }

        webView.addJavascriptInterface(new CaspianBridge(this, id), "CaspianBridge");
        applyWebViewTheme(webView, isDarkTheme);

        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        settings.setOffscreenPreRaster(true);
        settings.setEnableSmoothTransition(true);

        if (waveguardShield != null && waveguardShield.isGlobalEnabled()) {
            try {
                if (androidx.webkit.WebViewFeature.isFeatureSupported(androidx.webkit.WebViewFeature.DOCUMENT_START_SCRIPT)) {
                    String protection = waveguardShield.getClientSideProtectionJs();
                    if (protection != null && !protection.isEmpty()) {
                        androidx.webkit.WebViewCompat.addDocumentStartJavaScript(webView, protection, java.util.Collections.singleton("*"));
                    }
                }
            } catch (Throwable t) {
                Log.e(TAG, "Error configuring DOCUMENT_START_SCRIPT: ", t);
            }
        }

        TabItem tabItem = new TabItem(id, "New Tab", url, service, webView, isIncognito);
        tabItem.pendingPrompt = promptPayload;
        tabItem.caskId = finalCaskId;
        if (cask != null) {
            tabItem.caskName = cask.name;
            tabItem.caskIcon = cask.icon;
            tabItem.caskColor = cask.color;
        }

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
                String referer = webView.getUrl();
                CaspianDownloadManager.getInstance(this).enqueueDownload(downloadUrl, userAgent, contentDisposition, mimeType, contentLength, referer);
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
                if (tabItem.isDesktop && targetUrl.contains("://m.youtube.com")) {
                    String desktopUrl = targetUrl.replace("://m.youtube.com", "://www.youtube.com");
                    view.loadUrl(desktopUrl);
                    return true;
                }
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
                if (request == null || request.getUrl() == null || request.isForMainFrame()) {
                    return super.shouldInterceptRequest(view, request);
                }
                Uri reqUri = request.getUrl();
                if ("caspian.pdf".equalsIgnoreCase(reqUri.getHost()) && "/stream".equalsIgnoreCase(reqUri.getPath())) {
                        String filePath = reqUri.getQueryParameter("path");
                        if (filePath != null) {
                            File file = new File(filePath);
                            if (file.exists() && file.canRead()) {
                                try {
                                    java.io.FileInputStream fis = new java.io.FileInputStream(file);
                                    Map<String, String> headers = new java.util.HashMap<>();
                                    headers.put("Access-Control-Allow-Origin", "*");
                                    headers.put("Accept-Ranges", "bytes");
                                    headers.put("Content-Type", "application/pdf");
                                    headers.put("Content-Disposition", "inline; filename=\"" + file.getName() + "\"");
                                    return new WebResourceResponse("application/pdf", "identity", 200, "OK", headers, fis);
                                } catch (Exception ignored) {}
                            }
                        }
                    }
                try {
                    String pageHost = null;
                    if (tabItem != null && tabItem.url != null) {
                        try {
                            pageHost = Uri.parse(tabItem.url).getHost();
                        } catch (Exception ignored) {}
                    }
                    if (isDebugRecording && request != null && request.getUrl() != null) {
                        synchronized (debugLogBuffer) {
                            debugLogBuffer.append("[NetRequest] ").append(request.getMethod()).append(" ").append(request.getUrl()).append("\n");
                        }
                    }
                    if (pageHost != null) {
                        String ph = pageHost.toLowerCase(java.util.Locale.ROOT);
                        if (ph.equals("instagram.com") || ph.endsWith(".instagram.com")
                                || ph.equals("facebook.com") || ph.endsWith(".facebook.com")) {
                            return super.shouldInterceptRequest(view, request);
                        }
                    }
                    if (waveguardShield != null && waveguardShield.isGlobalEnabled() && waveguardShield.isBlocked(request.getUrl().toString(), pageHost, tabItem != null ? tabItem.id : -1)) {
                        WebResourceResponse blockedResp = waveguardShield.getBlockedResponse(request.getUrl().toString());
                        if (blockedResp != null) return blockedResp;
                    }
                } catch (Throwable t) {
                    Log.e(TAG, "Waveguard interception error: ", t);
                }
                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                if (error != null && request != null) {
                    String err = "[WebError] Code=" + error.getErrorCode() + " Desc=" + error.getDescription() + " URL=" + request.getUrl();
                    Log.e("CaspianWebConsole", err);
                    if (isDebugRecording) {
                        synchronized (debugLogBuffer) {
                            debugLogBuffer.append(err).append("\n");
                        }
                    }
                }
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                super.onReceivedHttpError(view, request, errorResponse);
                if (errorResponse != null && request != null) {
                    String err = "[HttpError] Status=" + errorResponse.getStatusCode() + " " + errorResponse.getReasonPhrase() + " URL=" + request.getUrl();
                    Log.e("CaspianWebConsole", err);
                    if (isDebugRecording) {
                        synchronized (debugLogBuffer) {
                            debugLogBuffer.append(err).append("\n");
                        }
                    }
                }
            }

            @Override
            public void onPageStarted(WebView view, String pageUrl, Bitmap favicon) {
                if (tabItem.isDesktop && pageUrl != null && pageUrl.contains("://m.youtube.com")) {
                    String desktopUrl = pageUrl.replace("://m.youtube.com", "://www.youtube.com");
                    view.stopLoading();
                    view.loadUrl(desktopUrl);
                    return;
                }
                String oldUrl = tabItem.url;
                tabItem.url = pageUrl;
                if (waveguardShield != null && pageUrl != null && !pageUrl.equals(oldUrl)) {
                    waveguardShield.resetTabBlockedCount(tabItem.id);
                }
                tabItem.service = AICommandRouter.detectServiceFromUrl(pageUrl);
                if (oldUrl != null && oldUrl.toLowerCase().contains("youtube.com") && (pageUrl == null || !pageUrl.toLowerCase().contains("youtube.com"))) {
                    tabItem.isPlayingAudio = false;
                    if (!hasAnyYouTubeTab()) {
                        hasYouTubePlaybackStarted = false;
                        dismissMediaNotification();
                    }
                }
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

                applyWebViewTheme(view, isDarkTheme);

                if (tabItem.isDesktop) {
                    view.evaluateJavascript(
                            "(function() {" +
                            "  try {" +
                            "    var meta = document.querySelector('meta[name=\\'viewport\\']');" +
                            "    if (!meta) { meta = document.createElement('meta'); meta.name = 'viewport'; document.head.appendChild(meta); }" +
                            "    meta.setAttribute('content', 'width=1280, initial-scale=0.6, user-scalable=yes');" +
                            "    if (navigator.userAgentData) {" +
                            "      Object.defineProperty(navigator.userAgentData, 'mobile', { get: () => false, configurable: true });" +
                            "      Object.defineProperty(navigator.userAgentData, 'platform', { get: () => 'Windows', configurable: true });" +
                            "    }" +
                            "    window.dispatchEvent(new Event('resize'));" +
                            "  } catch(e) {}" +
                            "})();", null);
                }

                captureTabSnapshot(tabItem);

                if (pageUrl != null && !pageUrl.startsWith("file://") && !pageUrl.startsWith("caspian://") && !pageUrl.startsWith("about:")) {
                    HistoryManager.getInstance(MainActivity.this).addEntry(tabItem.title, pageUrl);

                    // Auto-detect high-res 64x64 or touch icons from HTML header
                    view.evaluateJavascript("(function(){ try { var l = document.querySelector('link[rel*=\"icon\"][sizes*=\"64\"], link[rel=\"apple-touch-icon\"][sizes*=\"64\"], link[rel=\"apple-touch-icon\"]'); return l ? l.href : ''; } catch(e) { return ''; } })();", iconVal -> {
                        if (iconVal != null && !iconVal.isEmpty() && !iconVal.equals("\"\"") && !iconVal.equals("null")) {
                            String cleanUrl = iconVal.replace("\"", "").trim();
                            if (!cleanUrl.isEmpty()) {
                                tabItem.touchIconUrl = cleanUrl;
                                tabItem.favicon64 = cleanUrl;
                            }
                        }
                    });
                }

                if (pageUrl != null && pageUrl.contains("chatgpt.com")) {
                    String interceptorJs = readAssetScript("chatgpt_network_interceptor.js");
                    if (!interceptorJs.isEmpty()) {
                        view.evaluateJavascript(interceptorJs, null);
                    }
                }

                if (pageUrl != null && (pageUrl.contains("chatgpt.com") || pageUrl.contains("gemini.google.com"))) {
                    String prunerJs = readAssetScript("mobile_pruner.js");
                    if (!prunerJs.isEmpty()) {
                        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                        int limit = 5;
                        try {
                            limit = Integer.parseInt(prefs.getString("chat_message_limit", "5"));
                        } catch (Exception ignored) {}
                        String mode = prefs.getString("chat_pruning_mode", "sliding_window");
                        boolean enabled = "true".equalsIgnoreCase(prefs.getString("chat_limit_enabled", "false"));
                        view.evaluateJavascript(prunerJs + "\nif (window.__CASPIAN_PRUNER_UPDATE) window.__CASPIAN_PRUNER_UPDATE(" + limit + ", '" + mode + "', " + enabled + ");", null);
                    }
                }

                if (pageUrl != null && pageUrl.toLowerCase().contains("youtube.com")) {
                    String ytHelperJs = readAssetScript("youtube_helper.js");
                    if (!ytHelperJs.isEmpty()) {
                        view.evaluateJavascript("window.__caspian_tab_id = " + id + ";\n" + ytHelperJs, null);
                    }
                }

                if (waveguardShield != null && waveguardShield.isGlobalEnabled() && waveguardShield.isCosmeticEnabled()) {
                    String pageHost = null;
                    if (pageUrl != null) {
                        try { pageHost = Uri.parse(pageUrl).getHost(); } catch (Exception ignored) {}
                    }
                    if (pageHost != null) {
                        String ph = pageHost.toLowerCase(java.util.Locale.ROOT);
                        if (ph.equals("instagram.com") || ph.endsWith(".instagram.com")
                                || ph.equals("facebook.com") || ph.endsWith(".facebook.com")) {
                            // Never inject cosmetic CSS into Instagram or Facebook
                        } else if (!waveguardShield.isSiteWhitelisted(pageHost)) {
                            try {
                                String cosmetic = waveguardShield.getCosmeticCssInjection();
                                if (cosmetic != null && !cosmetic.isEmpty()) {
                                    view.evaluateJavascript(cosmetic, null);
                                }
                            } catch (Throwable ignored) {}
                        }
                    }
                }

                if (pageUrl != null && (pageUrl.contains("google.com") || pageUrl.contains("google.co."))
                        && !pageUrl.contains("/search?") && !pageUrl.contains("/maps") && !pageUrl.contains("accounts.google")) {
                    String googleAutofocusJs =
                            "(function() {\n" +
                            "  var focused = false;\n" +
                            "  function tryFocus() {\n" +
                            "    if (focused) return true;\n" +
                            "    var el = document.querySelector('textarea[name=\"q\"], input[name=\"q\"], input[type=\"search\"], div[role=\"combobox\"] input');\n" +
                            "    if (el) {\n" +
                            "      focused = true;\n" +
                            "      try { el.focus(); el.click(); } catch(e) {}\n" +
                            "      if (window.CaspianBridge && typeof window.CaspianBridge.showKeyboard === 'function') {\n" +
                            "        window.CaspianBridge.showKeyboard();\n" +
                            "      }\n" +
                            "      return true;\n" +
                            "    }\n" +
                            "    return false;\n" +
                            "  }\n" +
                            "  if (!tryFocus()) {\n" +
                            "    var count = 0;\n" +
                            "    var intv = setInterval(function() {\n" +
                            "      count++;\n" +
                            "      if (tryFocus() || count >= 20) clearInterval(intv);\n" +
                            "    }, 150);\n" +
                            "  }\n" +
                            "})();";
                    view.evaluateJavascript(googleAutofocusJs, null);
                }

                // Intercept blob: and data: download links client-side
                view.evaluateJavascript(
                    "(function() {\n" +
                    "  if (window.__caspian_blob_interceptor) return;\n" +
                    "  window.__caspian_blob_interceptor = true;\n" +
                    "  document.addEventListener('click', function(e) {\n" +
                    "    var a = e.target ? e.target.closest('a') : null;\n" +
                    "    if (a && a.href && (a.href.indexOf('blob:') === 0 || a.href.indexOf('data:') === 0)) {\n" +
                    "      if (a.hasAttribute('download') && window.CaspianBridge && typeof window.CaspianBridge.saveBlobChunk === 'function') {\n" +
                    "        e.preventDefault();\n" +
                    "        e.stopPropagation();\n" +
                    "        var fn = a.getAttribute('download') || 'download';\n" +
                    "        fetch(a.href).then(function(r) {\n" +
                    "          var mime = r.headers.get('content-type') || 'application/octet-stream';\n" +
                    "          return r.blob().then(function(b) {\n" +
                    "            var reader = new FileReader();\n" +
                    "            reader.onloadend = function() {\n" +
                    "              var b64 = (reader.result || '').split(',')[1];\n" +
                    "              var dlId = 'blob_' + Date.now();\n" +
                    "              window.CaspianBridge.saveBlobChunk(dlId, fn, mime, b64, true);\n" +
                    "            };\n" +
                    "            reader.readAsDataURL(b);\n" +
                    "          });\n" +
                    "        }).catch(function(err) {});\n" +
                    "      }\n" +
                    "    }\n" +
                    "  }, true);\n" +
                    "})();", null);

                if (tabItem.pendingPrompt != null && !tabItem.pendingPrompt.isEmpty()) {
                    if ("chatgpt".equalsIgnoreCase(tabItem.service) || "gemini".equalsIgnoreCase(tabItem.service)
                            || "claude".equalsIgnoreCase(tabItem.service) || "deepseek".equalsIgnoreCase(tabItem.service)) {
                        injectAIPrompt(view, tabItem.pendingPrompt, false);
                    }
                    tabItem.pendingPrompt = null;
                }
            }

            @Override
            public boolean onRenderProcessGone(WebView view, RenderProcessGoneDetail detail) {
                Log.e(TAG, "WebView render process gone. Crashed: " + (detail != null && detail.didCrash()));
                try {
                    if (view != null && view.getParent() instanceof ViewGroup) {
                        ((ViewGroup) view.getParent()).removeView(view);
                        view.destroy();
                    }
                } catch (Exception e) {}
                return true;
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (tabItem.id == activeTabId) {
                    browserProgressBar.setProgress(newProgress);
                    if (newProgress == 100) browserProgressBar.setVisibility(View.GONE);
                }
                if (newProgress >= 25 && newProgress <= 80) {
                    if (waveguardShield != null && waveguardShield.isGlobalEnabled() && waveguardShield.isCosmeticEnabled()) {
                        String pageHost = null;
                        if (tabItem != null && tabItem.url != null) {
                            try { pageHost = Uri.parse(tabItem.url).getHost(); } catch (Exception ignored) {}
                        }
                        if (pageHost != null && (pageHost.toLowerCase().contains("instagram.com") || pageHost.toLowerCase().contains("facebook.com"))) {
                            // Skip cosmetic CSS injection on Instagram and Facebook
                        } else if (!waveguardShield.isSiteWhitelisted(pageHost)) {
                            String cosmetic = waveguardShield.getCosmeticCssInjection();
                            if (cosmetic != null && !cosmetic.isEmpty()) {
                                view.evaluateJavascript(cosmetic, null);
                            }
                        }
                    }
                }
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                if (consoleMessage != null) {
                    String msg = "[WebConsole " + consoleMessage.messageLevel() + "] " + consoleMessage.message() 
                            + " (" + consoleMessage.sourceId() + ":" + consoleMessage.lineNumber() + ")";
                    Log.d("CaspianWebConsole", msg);
                    if (isDebugRecording) {
                        synchronized (debugLogBuffer) {
                            debugLogBuffer.append(new java.text.SimpleDateFormat("HH:mm:ss.SSS", java.util.Locale.US).format(new java.util.Date()))
                                    .append(" ").append(msg).append("\n");
                        }
                    }
                }
                return true;
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                tabItem.title = title;
                if (tabItem.id == activeTabId) {
                    updateOmniboxState();
                } else {
                    runOnUiThread(MainActivity.this::updateOmniboxTabStrip);
                }
            }

            @Override
            public void onReceivedIcon(WebView view, Bitmap icon) {
                if (icon != null && tabItem != null) {
                    try {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        icon.compress(Bitmap.CompressFormat.PNG, 100, baos);
                        byte[] b = baos.toByteArray();
                        tabItem.faviconB64 = "data:image/png;base64," + android.util.Base64.encodeToString(b, android.util.Base64.NO_WRAP);

                        try {
                            Bitmap icon64 = Bitmap.createScaledBitmap(icon, 64, 64, true);
                            ByteArrayOutputStream baos64 = new ByteArrayOutputStream();
                            icon64.compress(Bitmap.CompressFormat.PNG, 100, baos64);
                            byte[] b64 = baos64.toByteArray();
                            tabItem.favicon64 = "data:image/png;base64," + android.util.Base64.encodeToString(b64, android.util.Base64.NO_WRAP);
                        } catch (Exception ignored) {}

                        evaluateJavascriptInControlSheet("if(window.onTabFaviconReceived) window.onTabFaviconReceived(" 
                                + tabItem.id + ", " + JSONObject.quote(tabItem.faviconB64) + ");");
                        runOnUiThread(MainActivity.this::updateOmniboxTabStrip);
                    } catch (Exception ignored) {}
                }
            }

            @Override
            public void onReceivedTouchIconUrl(WebView view, String url, boolean precomposed) {
                super.onReceivedTouchIconUrl(view, url, precomposed);
                if (url != null && tabItem != null) {
                    tabItem.touchIconUrl = url;
                    if (tabItem.favicon64 == null) {
                        tabItem.favicon64 = url;
                    }
                }
            }

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                if (customView != null) {
                    callback.onCustomViewHidden();
                    return;
                }
                customView = view;
                customViewCallback = callback;
                findAndConfigureSurfaceViews(customView);
                fullscreenContainer.addView(customView, new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        Gravity.CENTER
                ));
                fullscreenContainer.setVisibility(View.VISIBLE);

                try {
                    // Auto-rotate to landscape like native YouTube app
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);

                    // Immersive sticky fullscreen
                    getWindow().getDecorView().setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    );

                    // If vertical video (Shorts), detect and switch back to portrait
                    if (tabItem.webView != null) {
                        tabItem.webView.evaluateJavascript(
                                "(function(){ " +
                                "  try { " +
                                "    if (window.location.href.indexOf('/shorts/') !== -1) return true; " +
                                "    var v = document.querySelector('video'); " +
                                "    return !!(v && v.videoHeight > 0 && v.videoWidth > 0 && (v.videoHeight > v.videoWidth)); " +
                                "  } catch(e) { return false; } " +
                                "})()",
                                isPortrait -> {
                                    if (isPortrait != null && isPortrait.contains("true")) {
                                        try {
                                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                                        } catch (Exception ignored) {}
                                    }
                                }
                        );
                    }
                } catch (Exception ignored) {}

                // Keep Float Pod visible and elevated above fullscreen container
                if (ytFloatingRemoteContainer != null) {
                    ytFloatingRemoteContainer.setTranslationX(0f);
                    ytFloatingRemoteContainer.setTranslationY(0f);
                    if (ytFloatingRemoteContainer.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) ytFloatingRemoteContainer.getLayoutParams();
                        lp.bottomMargin = dpToPx(16);
                        lp.rightMargin = dpToPx(24);
                        ytFloatingRemoteContainer.setLayoutParams(lp);
                    }
                    ytFloatingRemoteContainer.bringToFront();
                    ytFloatingRemoteContainer.setElevation(dpToPx(300));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        ytFloatingRemoteContainer.setTranslationZ(dpToPx(100));
                    }
                    if (ytFloatingRemoteContainer.getParent() instanceof ViewGroup) {
                        ((ViewGroup) ytFloatingRemoteContainer.getParent()).requestLayout();
                        ((ViewGroup) ytFloatingRemoteContainer.getParent()).invalidate();
                    }
                    String curUrl = (tabItem.webView != null && tabItem.webView.getUrl() != null)
                            ? tabItem.webView.getUrl()
                            : (tabItem.url != null ? tabItem.url : "");
                    boolean isYtMusic = curUrl.toLowerCase().contains("music.youtube.com") || (tabItem != null && "youtubemusic".equalsIgnoreCase(tabItem.service));
                    boolean isYt = (curUrl.toLowerCase().contains("youtube.com") || (tabItem != null && "youtube".equalsIgnoreCase(tabItem.service))) && !isYtMusic;
                    if (isYt && !isYtRemoteExplicitlyHidden) {
                        ytFloatingRemoteContainer.setVisibility(View.VISIBLE);
                        if (ytFloatingRemoteScroll != null && ytFloatingRemoteBall != null) {
                            if (ytFloatingRemoteScroll.getVisibility() != View.VISIBLE && ytFloatingRemoteBall.getVisibility() != View.VISIBLE) {
                                ytFloatingRemoteBall.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }
                if (floatingCaspianCard != null) {
                    floatingCaspianCard.setVisibility(View.GONE);
                }
                if (ytFloatingRemoteScroll != null && ytFloatingRemoteScroll.getVisibility() == View.VISIBLE) {
                    if (ytFloatingRemoteBall != null) ytFloatingRemoteBall.setVisibility(View.GONE);
                    ytFloatingRemoteScroll.bringToFront();
                    ytFloatingRemoteScroll.setElevation(dpToPx(310));
                } else if (ytFloatingRemoteBall != null) {
                    if (ytFloatingRemoteScroll != null) ytFloatingRemoteScroll.setVisibility(View.GONE);
                    ytFloatingRemoteBall.setVisibility(View.VISIBLE);
                    ytFloatingRemoteBall.bringToFront();
                    ytFloatingRemoteBall.setElevation(dpToPx(310));
                }
                if (ytRemoteFullscreen != null) {
                    ytRemoteFullscreen.setImageResource(R.drawable.ic_pod_fullscreen_exit);
                    ytRemoteFullscreen.setContentDescription("Exit Fullscreen");
                }
                applyScreenTouchLockState(true);
                String behavior = getYtTimelineDefaultBehavior();
                if ("fullscreen_only".equals(behavior) || "both".equals(behavior)) {
                    isTimelineUserEnabled = true;
                    if (ytFloatingTimelineBar != null) {
                        ytFloatingTimelineBar.setVisibility(View.VISIBLE);
                        if (ytRemoteTimeline != null) {
                            try {
                                ytRemoteTimeline.setColorFilter(Color.parseColor(podStartColor));
                            } catch (Exception e) {
                                ytRemoteTimeline.setColorFilter(0xFF00E5FF);
                            }
                        }
                    }
                }
                syncTimelineBarWidth();
            }

            @Override
            public void onHideCustomView() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && isInPictureInPictureMode()) {
                    return;
                }
                exitFullscreenCustomView();
            }

            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                runOnUiThread(() -> {
                    String[] requestedResources = request.getResources();
                    boolean needAudio = false;
                    boolean needVideo = false;
                    for (String r : requestedResources) {
                        if (PermissionRequest.RESOURCE_AUDIO_CAPTURE.equals(r)) needAudio = true;
                        if (PermissionRequest.RESOURCE_VIDEO_CAPTURE.equals(r)) needVideo = true;
                    }

                    boolean hasAudioPerm = !needAudio || (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED);
                    boolean hasVideoPerm = !needVideo || (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);

                    if (hasAudioPerm && hasVideoPerm) {
                        request.grant(requestedResources);
                    } else {
                        List<String> permsToRequest = new ArrayList<>();
                        if (needAudio && !hasAudioPerm) permsToRequest.add(Manifest.permission.RECORD_AUDIO);
                        if (needVideo && !hasVideoPerm) permsToRequest.add(Manifest.permission.CAMERA);

                        if (!permsToRequest.isEmpty()) {
                            pendingWebPermissionRequest = request;
                            ActivityCompat.requestPermissions(MainActivity.this, permsToRequest.toArray(new String[0]), WEBVIEW_PERMISSION_REQUEST_CODE);
                        } else {
                            request.grant(requestedResources);
                        }
                    }
                });
            }

            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                if (uploadMessage != null) {
                    uploadMessage.onReceiveValue(null);
                    uploadMessage = null;
                }
                uploadMessage = filePathCallback;
                cameraCapturedUri = null;

                Intent takePictureIntent = null;
                try {
                    File photoFile = createImageFile();
                    if (photoFile != null) {
                        cameraCapturedUri = FileProvider.getUriForFile(
                            MainActivity.this,
                            getPackageName() + ".fileprovider",
                            photoFile
                        );
                        takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraCapturedUri);
                        takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    }
                } catch (Exception e) {
                    cameraCapturedUri = null;
                }

                boolean isCapture = fileChooserParams != null && fileChooserParams.isCaptureEnabled();
                String[] acceptTypes = fileChooserParams != null ? fileChooserParams.getAcceptTypes() : null;
                boolean isImage = false;
                if (acceptTypes != null) {
                    for (String t : acceptTypes) {
                        if (t != null && (t.toLowerCase().contains("image") || t.equals("*/*"))) {
                            isImage = true;
                            break;
                        }
                    }
                } else {
                    isImage = true;
                }

                // If website explicitly requested direct capture (like Camera in Gemini/ChatGPT), launch camera directly!
                if (isCapture && takePictureIntent != null) {
                    try {
                        startActivityForResult(takePictureIntent, FILECHOOSER_RESULTCODE);
                        return true;
                    } catch (Exception ignored) {}
                }

                // Otherwise, present standard chooser with Camera as an option
                Intent contentIntent = fileChooserParams != null ? fileChooserParams.createIntent() : new Intent(Intent.ACTION_GET_CONTENT).setType("*/*");
                Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentIntent);
                chooserIntent.putExtra(Intent.EXTRA_TITLE, "Select or Take Photo");
                if (isImage && takePictureIntent != null) {
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{ takePictureIntent });
                }

                try {
                    startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);
                } catch (Exception e) {
                    if (uploadMessage != null) {
                        uploadMessage.onReceiveValue(null);
                        uploadMessage = null;
                    }
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
        addNewTab(service, prompt, url, isIncognito, null, true);
    }

    public void addNewTab(String service, String prompt, String url, boolean isIncognito, String targetCaskId) {
        addNewTab(service, prompt, url, isIncognito, targetCaskId, true);
    }

    public void addNewTab(String service, String prompt, String url, boolean isIncognito, String targetCaskId, boolean switchTo) {
        int id = nextTabId++;
        String finalUrl = (url != null && !url.trim().isEmpty()) ? url : "file:///android_asset/launch_hub.html";
        String finalService = (service != null && !service.trim().isEmpty()) ? service : ("file:///android_asset/launch_hub.html".equals(finalUrl) ? "hub" : "web");
        
        CaskManager cm = new CaskManager(this);
        String caskId = (targetCaskId != null && !targetCaskId.trim().isEmpty()) ? targetCaskId : cm.getActiveCaskId();
        TabItem tab = createNewTabInstance(id, finalUrl, finalService, prompt, isIncognito, caskId);
        if ("file:///android_asset/launch_hub.html".equals(finalUrl)) {
            tab.title = "Caspian Hub";
        }
        tabsList.add(tab);
        if (switchTo || activeTabId == -1 || getTabById(activeTabId) == null) {
            switchToTab(id);
        } else {
            if (tab.webView != null && tab.webView.getParent() != webViewContainer) {
                if (tab.webView.getParent() != null) {
                    ((ViewGroup) tab.webView.getParent()).removeView(tab.webView);
                }
                tab.webView.setVisibility(View.INVISIBLE);
                webViewContainer.addView(tab.webView);
            }
        }
        updateOmniboxTabStrip();
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
        CaskManager cm = new CaskManager(this);
        String activeCask = cm.getActiveCaskId();

        if (tab != null && tab.webView != null) {
            tab.service = service;
            tab.url = url;
            if ("hub".equalsIgnoreCase(service)) tab.title = "Caspian Hub";

            // If the tab was Launch Hub and the user switched active cask on Hub,
            // ensure the newly launched service uses the selected container profile!
            if (!tab.caskId.equals(activeCask) && CaskManager.isMultiProfileSupported()) {
                tab.caskId = activeCask;
                CaskManager.CaskItem cask = cm.getCaskById(activeCask);
                if (cask != null) {
                    tab.caskName = cask.name;
                    tab.caskIcon = cask.icon;
                    tab.caskColor = cask.color;
                }
                if (tab.webView.getParent() != null) {
                    ((ViewGroup) tab.webView.getParent()).removeView(tab.webView);
                }
                tab.webView.destroy();
                TabItem refreshed = createNewTabInstance(tab.id, url, service, null, tab.isIncognito, activeCask);
                tab.webView = refreshed.webView;
                if (splitModeState == 0) {
                    webViewContainer.addView(tab.webView);
                } else {
                    applySplitViewLayout();
                }
            } else {
                tab.webView.loadUrl(url);
            }
            if (tab.webView != null) {
                tab.webView.setAlpha(1.0f);
                tab.webView.setScaleX(0.98f);
                tab.webView.setScaleY(0.98f);
                tab.webView.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(160)
                        .setInterpolator(new DecelerateInterpolator(1.4f))
                        .start();
            }
            updateOmniboxState();
            saveOpenTabsState();
        } else {
            addNewTab(service, null, url, false, activeCask);
        }
    }

    public void switchToTab(int tabId) {
        switchToTab(tabId, true);
    }

    public void switchToTab(int tabId, boolean closeSheet) {
        if (customView != null && tabId != activeTabId) {
            exitFullscreenCustomView();
        }
        TabItem previousTab = getTabById(activeTabId);
        if (previousTab != null) captureTabSnapshot(previousTab);

        TabItem tab = getTabById(tabId);
        if (tab == null) return;

        // On legacy devices lacking Multi-Profile, swap vault cookies when switching between tabs with different casks
        if (!CaskManager.isMultiProfileSupported() && previousTab != null && previousTab.caskId != null && !previousTab.caskId.equals(tab.caskId)) {
            CaskManager cm = new CaskManager(this);
            cm.saveActiveCookiesToVault(previousTab.caskId);
            CookieManager.getInstance().removeAllCookies(null);
            cm.restoreCaskCookiesFromVault(tab.caskId);
        }

        if (tab.splitPartnerId != -1 && getTabById(tab.splitPartnerId) != null) {
            TabItem partner = getTabById(tab.splitPartnerId);
            if ("secondary".equals(tab.splitRole)) {
                activeTabId = partner.id;
                secondarySplitTabId = tab.id;
            } else {
                activeTabId = tab.id;
                secondarySplitTabId = partner.id;
            }
            splitModeState = tab.splitOrientation > 0 ? tab.splitOrientation : 1;
            applySplitViewLayout();
        } else {
            splitModeState = 0;
            activeTabId = tabId;

            if (splitViewContainer != null) splitViewContainer.setVisibility(View.GONE);
            if (splitLeftContainer != null) splitLeftContainer.removeAllViews();
            if (splitRightContainer != null) splitRightContainer.removeAllViews();
            if (webViewContainer != null) webViewContainer.setVisibility(View.VISIBLE);

            for (TabItem t : tabsList) {
                if (t.webView != null) {
                    if (t.id == tabId) {
                        if (t.webView.getParent() != webViewContainer) {
                            if (t.webView.getParent() != null) {
                                ((ViewGroup) t.webView.getParent()).removeView(t.webView);
                            }
                            webViewContainer.addView(t.webView);
                        }
                        t.webView.setVisibility(View.VISIBLE);
                        t.webView.bringToFront();
                    } else {
                        // Keep background tab WebViews attached to container as INVISIBLE so background media playback continues uninterrupted
                        if (t.webView.getParent() == webViewContainer) {
                            t.webView.setVisibility(View.INVISIBLE);
                        }
                    }
                }
            }
        }
        updateOmniboxState();
        if (!hasAnyYouTubeTab()) {
            hasYouTubePlaybackStarted = false;
            dismissMediaNotification();
        }
        if (closeSheet) {
            hideControlSheet(false);
        }
        playUiFeedbackSound("tm_tabs");
        saveOpenTabsState();
    }

    public void notifyUndoStateChanged() {
        evaluateJavascriptInControlSheet("if(typeof updateUndoButtonState === 'function') updateUndoButtonState(" + hasClosedTabsToUndo() + ");");
    }

    public void closeTab(int tabId) {
        closeTab(tabId, true);
    }

    public void closeTab(int tabId, boolean recordHistory) {
        TabItem toRemove = getTabById(tabId);
        if (toRemove != null && toRemove.isFavorite) {
            Toast.makeText(this, "⭐ Favorited tabs are locked. Unfavorite first to close.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (customView != null && activeTabId == tabId) {
            exitFullscreenCustomView();
        }

        if (tabsList.size() <= 1) {
            TabItem last = tabsList.get(0);
            if (last != null && last.isFavorite) {
                Toast.makeText(this, "⭐ Favorited tabs are locked. Unfavorite first to close.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (last != null && recordHistory && !last.isIncognito && !"hub".equalsIgnoreCase(last.service)) {
                List<ClosedTabRecord> batch = new ArrayList<>();
                batch.add(new ClosedTabRecord(last));
                closedTabBatches.add(batch);
                if (closedTabBatches.size() > 30) closedTabBatches.remove(0);
                notifyUndoStateChanged();
            }
            last.url = "file:///android_asset/launch_hub.html";
            last.service = "hub";
            last.title = "Caspian Hub";
            last.webView.loadUrl(last.url);
            updateOmniboxState();
            if (!hasAnyYouTubeTab()) {
                hasYouTubePlaybackStarted = false;
                dismissMediaNotification();
            }
            saveOpenTabsState();
            return;
        }
        if (toRemove != null) {
            if (recordHistory && !toRemove.isIncognito) {
                List<ClosedTabRecord> batch = new ArrayList<>();
                batch.add(new ClosedTabRecord(toRemove));
                closedTabBatches.add(batch);
                if (closedTabBatches.size() > 30) closedTabBatches.remove(0);
                notifyUndoStateChanged();
            }
            if (toRemove.isIncognito) {
                try {
                    toRemove.webView.clearCache(true);
                    toRemove.webView.clearHistory();
                    toRemove.webView.clearFormData();
                } catch(Exception ignored) {}
            }
            if (toRemove.webView.getParent() != null) {
                ((ViewGroup) toRemove.webView.getParent()).removeView(toRemove.webView);
            }
            if (toRemove.splitPartnerId != -1) {
                TabItem partner = getTabById(toRemove.splitPartnerId);
                if (partner != null) {
                    partner.splitPartnerId = -1;
                    partner.splitRole = "";
                    partner.splitOrientation = 0;
                    partner.splitName = "";
                }
                if (splitModeState > 0 && (activeTabId == tabId || secondarySplitTabId == tabId)) {
                    splitModeState = 0;
                    secondarySplitTabId = -1;
                    if (partner != null) {
                        activeTabId = partner.id;
                    }
                }
            }
            tabsList.remove(toRemove);

            for (TabGroup g : tabGroupsList) {
                g.tabIds.remove((Integer) tabId);
            }
            tabGroupsList.removeIf(g -> g.tabIds.isEmpty());
            saveTabGroups();

            if (activeTabId == tabId) {
                if (!tabsList.isEmpty()) {
                    activeTabId = tabsList.get(tabsList.size() - 1).id;
                    switchToTab(activeTabId, !isSheetOpen);
                }
            }
        }
        if (!hasAnyYouTubeTab()) {
            hasYouTubePlaybackStarted = false;
            dismissMediaNotification();
        }
        updateOmniboxState();
        saveOpenTabsState();
    }

    public void closeMultipleTabs(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return;
        List<ClosedTabRecord> batch = new ArrayList<>();
        for (int id : ids) {
            TabItem tab = getTabById(id);
            if (tab != null && !tab.isIncognito && !tab.isFavorite) {
                batch.add(new ClosedTabRecord(tab));
            }
        }
        if (!batch.isEmpty()) {
            closedTabBatches.add(batch);
            if (closedTabBatches.size() > 30) closedTabBatches.remove(0);
            notifyUndoStateChanged();
        }
        for (int id : ids) {
            closeTab(id, false);
        }
        if (!hasAnyYouTubeTab()) {
            hasYouTubePlaybackStarted = false;
            dismissMediaNotification();
        }
        saveOpenTabsState();
    }

    public int restoreLastClosedBatch() {
        if (!closedTabBatches.isEmpty()) {
            List<ClosedTabRecord> batch = closedTabBatches.remove(closedTabBatches.size() - 1);
            boolean hadActiveTab = (activeTabId != -1 && getTabById(activeTabId) != null);
            for (ClosedTabRecord rec : batch) {
                // When restoring, keep restored tabs in background without stealing active status
                boolean shouldSwitch = !hadActiveTab;
                addNewTab(rec.service, rec.pendingPrompt, rec.url, rec.isIncognito, rec.caskId, shouldSwitch);
                if (!tabsList.isEmpty()) {
                    TabItem restoredTab = tabsList.get(tabsList.size() - 1);
                    if (restoredTab != null) {
                        if (rec.title != null) restoredTab.title = rec.title;
                        restoredTab.isFavorite = rec.isFavorite;
                        restoredTab.faviconB64 = rec.faviconB64;
                        restoredTab.favicon64 = rec.favicon64;
                        restoredTab.touchIconUrl = rec.touchIconUrl;
                    }
                }
            }
            notifyUndoStateChanged();
            evaluateJavascriptInControlSheet("if(typeof renderOpenTabs === 'function') renderOpenTabs();");
            return batch.size();
        }
        return 0;
    }

    public void restoreLastClosedTab() {
        restoreLastClosedBatch();
    }

    public void restoreLastClosedGroupTabs() {
        restoreLastClosedBatch();
    }

    public boolean hasClosedTabsToUndo() {
        return !closedTabBatches.isEmpty();
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

    public void changeTabCask(int tabId, String newCaskId) {
        TabItem tab = getTabById(tabId);
        if (tab == null || newCaskId == null || newCaskId.trim().isEmpty()) return;
        if (newCaskId.equals(tab.caskId)) return; // Didn't change, do nothing

        CaskManager cm = new CaskManager(this);
        CaskManager.CaskItem cask = cm.getCaskById(newCaskId);
        if (cask == null) return;

        tab.caskId = newCaskId;
        tab.caskName = cask.name;
        tab.caskIcon = cask.icon;
        tab.caskColor = cask.color;

        if (tab.webView != null) {
            String currentUrl = (tab.url != null && !tab.url.isEmpty()) ? tab.url : tab.webView.getUrl();
            if (currentUrl == null || currentUrl.isEmpty()) {
                currentUrl = "file:///android_asset/launch_hub.html";
            }
            if (CaskManager.isMultiProfileSupported()) {
                boolean isCurrentActive = (tab.id == activeTabId);
                if (tab.webView.getParent() != null) {
                    ((ViewGroup) tab.webView.getParent()).removeView(tab.webView);
                }
                tab.webView.destroy();
                TabItem refreshed = createNewTabInstance(tab.id, currentUrl, tab.service, null, tab.isIncognito, newCaskId);
                tab.webView = refreshed.webView;
                if (isCurrentActive) {
                    if (splitModeState == 0) {
                        webViewContainer.addView(tab.webView);
                    } else {
                        applySplitViewLayout();
                    }
                }
            } else {
                tab.webView.loadUrl(currentUrl);
            }
        }
        saveOpenTabsState();
    }

    public void updateTabDetails(int tabId, String nickname, String url, String newCaskId) {
        TabItem tab = getTabById(tabId);
        if (tab != null) {
            tab.nickname = nickname;
            boolean urlChanged = (url != null && !url.isEmpty() && !url.equals(tab.url));
            if (urlChanged) {
                tab.url = url;
            }
            if (newCaskId != null && !newCaskId.trim().isEmpty() && !newCaskId.equals(tab.caskId)) {
                changeTabCask(tabId, newCaskId);
            } else if (urlChanged && tab.webView != null) {
                tab.webView.loadUrl(url);
            }
            Toast.makeText(this, "Tab Details Updated", Toast.LENGTH_SHORT).show();
            updateOmniboxTabStrip();
            saveOpenTabsState();
        }
    }

    public void updateTabDetails(int tabId, String nickname, String url) {
        updateTabDetails(tabId, nickname, url, null);
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
                updateOmniboxTabStrip();
                saveOpenTabsState();
            }
        } catch (Exception ignored) {}
    }

    public void closeAllTabs() {
        List<TabItem> nonFavorites = new ArrayList<>();
        for (TabItem item : tabsList) {
            if (!item.isFavorite) {
                nonFavorites.add(item);
            }
        }

        if (nonFavorites.isEmpty()) {
            Toast.makeText(this, "⭐ All tabs are favorited and protected.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<ClosedTabRecord> batch = new ArrayList<>();
        for (TabItem item : nonFavorites) {
            if (!item.isIncognito) {
                batch.add(new ClosedTabRecord(item));
            }
        }
        if (!batch.isEmpty()) {
            closedTabBatches.add(batch);
            if (closedTabBatches.size() > 30) closedTabBatches.remove(0);
            notifyUndoStateChanged();
        }

        for (TabItem toRemove : nonFavorites) {
            if (toRemove.webView.getParent() != null) {
                ((ViewGroup) toRemove.webView.getParent()).removeView(toRemove.webView);
            }
            toRemove.webView.destroy();
            tabsList.remove(toRemove);
        }

        tabGroupsList.removeIf(g -> {
            for (TabItem removed : nonFavorites) {
                g.tabIds.remove((Integer) removed.id);
            }
            return g.tabIds.isEmpty();
        });
        saveTabGroups();

        if (tabsList.isEmpty()) {
            addNewTab("hub", null);
        } else {
            if (getTabById(activeTabId) == null) {
                activeTabId = tabsList.get(0).id;
            }
            switchToTab(activeTabId, !isSheetOpen);
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
        if (tab != null && tab.webView != null) {
            try {
                String curUrl = tab.webView.getUrl();
                if (curUrl == null || curUrl.isEmpty()) {
                    if (tab.url != null && !tab.url.isEmpty()) {
                        tab.webView.loadUrl(tab.url);
                    } else {
                        tab.webView.reload();
                    }
                } else {
                    tab.webView.reload();
                }
            } catch (Exception e) {
                try { tab.webView.reload(); } catch (Exception ignored) {}
            }
        }
    }

    public void reloadActiveTab() {
        TabItem tab = getActiveOrDominantTab();
        if (tab != null && tab.webView != null) {
            try {
                String curUrl = tab.webView.getUrl();
                if (curUrl == null || curUrl.isEmpty()) {
                    if (tab.url != null && !tab.url.isEmpty()) {
                        tab.webView.loadUrl(tab.url);
                    } else {
                        tab.webView.reload();
                    }
                } else {
                    tab.webView.reload();
                }
            } catch (Exception e) {
                try { tab.webView.reload(); } catch (Exception ignored) {}
            }
        }
    }

    public void toggleDesktopMode(int tabId) {
        TabItem tab = getTabById(tabId);
        if (tab == null) tab = getActiveOrDominantTab();
        if (tab == null || tab.webView == null) return;
        tab.isDesktop = !tab.isDesktop;
        tab.webView.getSettings().setUserAgentString(tab.isDesktop ? DESKTOP_UA : MOBILE_UA);
        tab.webView.getSettings().setUseWideViewPort(tab.isDesktop);
        tab.webView.getSettings().setLoadWithOverviewMode(tab.isDesktop);
        tab.webView.getSettings().setSupportZoom(true);
        tab.webView.getSettings().setBuiltInZoomControls(true);
        tab.webView.getSettings().setDisplayZoomControls(false);

        String url = tab.webView.getUrl();
        if (url != null) {
            if (tab.isDesktop && url.contains("://m.youtube.com")) {
                url = url.replace("://m.youtube.com", "://www.youtube.com");
                tab.webView.loadUrl(url);
            } else if (!tab.isDesktop && url.contains("://www.youtube.com")) {
                url = url.replace("://www.youtube.com", "://m.youtube.com");
                tab.webView.loadUrl(url);
            } else {
                tab.webView.reload();
            }
        } else {
            tab.webView.reload();
        }
        Toast.makeText(this, tab.isDesktop ? "💻 Desktop Mode" : "📱 Mobile Mode", Toast.LENGTH_SHORT).show();
    }

    public void setAdBlockEnabled(boolean enabled) {
        if (waveguardShield != null) {
            waveguardShield.setGlobalEnabled(enabled);
            updateOmniboxState();
        }
    }

    public int getBlockedAdsCount() {
        return waveguardShield != null ? waveguardShield.getTotalBlockedCount() : 0;
    }

    public WaveguardShield getWaveguardShield() {
        return waveguardShield;
    }

    private TabItem getTabById(int tabId) {
        for (TabItem item : tabsList) {
            if (item.id == tabId) return item;
        }
        return null;
    }

    public WebView getControlWebView() {
        return controlWebView;
    }

    public void syncWaveguardToControlWeb() {
        if (controlWebView != null) {
            controlWebView.evaluateJavascript("if (typeof syncWaveguardUI === 'function') syncWaveguardUI();", null);
        }
    }

    public void updateOmniboxState() {
        if (!hasAnyYouTubeTab()) {
            hasYouTubePlaybackStarted = false;
            dismissMediaNotification();
        }
        TabItem currentTab = getActiveOrDominantTab();
        int themeAccent = Color.parseColor(podStartColor);

        if (currentTab != null) {
            String url = currentTab.url != null ? currentTab.url : "";
            if (!omniboxEditText.hasFocus()) {
                omniboxEditText.setText(cleanDisplayUrl(url));
            }
            boolean canBack = false;
            boolean canFwd = false;
            try {
                if (currentTab.webView != null) {
                    canBack = currentTab.webView.canGoBack();
                    canFwd = currentTab.webView.canGoForward();
                }
            } catch (Exception ignored) {}
            omniboxBackBtn.setEnabled(canBack);
            omniboxBackBtn.setAlpha(canBack ? 1.0f : 0.4f);

            omniboxForwardBtn.setEnabled(canFwd);
            omniboxForwardBtn.setAlpha(canFwd ? 1.0f : 0.4f);

            boolean isYtMusic = url.toLowerCase().contains("music.youtube.com") || (currentTab != null && "youtubemusic".equalsIgnoreCase(currentTab.service));
            boolean isYtTab = (url.toLowerCase().contains("youtube.com") || (currentTab != null && "youtube".equalsIgnoreCase(currentTab.service))) && !isYtMusic;
            if (ytFloatingRemoteContainer != null) {
                if (isYtTab && !isYtRemoteExplicitlyHidden) {
                    if (ytFloatingRemoteContainer.getVisibility() != View.VISIBLE) {
                        ytFloatingRemoteScroll.setVisibility(View.GONE);
                        ytFloatingRemoteBall.setVisibility(View.VISIBLE);
                    }
                    ytFloatingRemoteContainer.setVisibility(View.VISIBLE);
                    if (customView == null) {
                        String behavior = getYtTimelineDefaultBehavior();
                        if ("both".equals(behavior) || "vertical_only".equals(behavior)) {
                            isTimelineUserEnabled = true;
                            if (ytFloatingTimelineBar != null) {
                                syncTimelineBarWidth();
                                ytFloatingTimelineBar.setVisibility(View.VISIBLE);
                                if (ytRemoteTimeline != null) {
                                    try {
                                        ytRemoteTimeline.setColorFilter(Color.parseColor(podStartColor));
                                    } catch (Exception e) {
                                        ytRemoteTimeline.setColorFilter(0xFF00E5FF);
                                    }
                                }
                            }
                        }
                    }
                } else {
                    ytFloatingRemoteContainer.setVisibility(View.GONE);
                    if (ytFloatingTimelineBar != null) ytFloatingTimelineBar.setVisibility(View.GONE);
                }
            }
            applyScreenTouchLockState(isScreenTouchLocked);

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

            updateOmniboxScrimBackground();
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
            boolean active = waveguardShield != null && waveguardShield.isGlobalEnabled();
            TabItem current = getActiveOrDominantTab();
            if (current != null && current.url != null && waveguardShield != null) {
                try {
                    String h = Uri.parse(current.url).getHost();
                    if (waveguardShield.isSiteWhitelisted(h)) {
                        active = false;
                    }
                } catch (Exception ignored) {}
            }
            omniboxShieldIcon.setAlpha(active ? 1.0f : 0.35f);
        }

        if (omniboxVoiceBtn != null) {
            omniboxVoiceBtn.setColorFilter(isRecordingSpeechMode ? 0xFFFF3366 : themeAccent);
        }

        updateOmniboxTabStrip();
    }

    public void setTabStripEnabled(boolean enabled) {
        this.isTabStripEnabled = enabled;
        try {
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                    .edit()
                    .putBoolean("tab_strip_enabled", enabled)
                    .apply();
        } catch (Throwable ignored) {}
        runOnUiThread(this::updateOmniboxTabStrip);
    }

    public boolean isTabStripEnabled() {
        return this.isTabStripEnabled;
    }

    public void showTabStripUndoButton() {
        runOnUiThread(() -> {
            if (btnOmniboxUndoCloseTab != null && hasClosedTabsToUndo()) {
                btnOmniboxUndoCloseTab.setVisibility(View.VISIBLE);
                tabStripUndoHandler.removeCallbacks(tabStripUndoDismissRunnable);
                tabStripUndoHandler.postDelayed(tabStripUndoDismissRunnable, 30000);
            }
        });
    }

    public void dismissTabStripUndoButton() {
        runOnUiThread(() -> {
            if (btnOmniboxUndoCloseTab != null) {
                btnOmniboxUndoCloseTab.setVisibility(View.GONE);
            }
        });
    }

    public void updateOmniboxTabStrip() {
        updateOmniboxTabStrip(true);
    }

    public void updateOmniboxTabStrip(boolean scrollToActive) {
        runOnUiThread(() -> {
            try {
                if (omniboxTabStripTabs == null || omniboxTabStripScroll == null) return;
                final int prevScrollX = omniboxTabStripScroll.getScrollX();
                omniboxTabStripTabs.removeAllViews();

                if (!isTabStripEnabled || tabsList.isEmpty()) {
                    if (omniboxTabStripBar != null) omniboxTabStripBar.setVisibility(View.GONE);
                    return;
                }
                if (omniboxTabStripBar != null) omniboxTabStripBar.setVisibility(View.VISIBLE);

                // Circular Add Tab Button
                if (btnOmniboxAddTab != null) {
                    GradientDrawable addBg = new GradientDrawable();
                    addBg.setShape(GradientDrawable.OVAL);
                    if (isDarkTheme) {
                        addBg.setColor(0xFF1E2838);
                        addBg.setStroke(dpToPx(1), 0xFF2A374A);
                    } else {
                        addBg.setColor(0xFFE2E8F0);
                        addBg.setStroke(dpToPx(1), 0xFFCBD5E1);
                    }
                    btnOmniboxAddTab.setBackground(addBg);
                    ImageView addIcon = findViewById(R.id.btn_omnibox_add_icon);
                    if (addIcon != null) {
                        addIcon.setColorFilter(isDarkTheme ? 0xFFCBD5E1 : 0xFF1E293B);
                    }
                }

                // Style Undo Closed Tab Button
                if (btnOmniboxUndoCloseTab != null) {
                    GradientDrawable undoBg = new GradientDrawable();
                    undoBg.setCornerRadius(dpToPx(16));
                    if (isDarkTheme) {
                        undoBg.setColor(0xFF1E2838);
                        undoBg.setStroke(dpToPx(1), 0xFF00E5FF);
                    } else {
                        undoBg.setColor(0xFFE0F2FE);
                        undoBg.setStroke(dpToPx(1), 0xFF0284C7);
                    }
                    btnOmniboxUndoCloseTab.setBackground(undoBg);
                    if (iconOmniboxUndoClose != null) iconOmniboxUndoClose.setTextColor(isDarkTheme ? 0xFF00E5FF : 0xFF0284C7);
                    if (textOmniboxUndoClose != null) textOmniboxUndoClose.setTextColor(isDarkTheme ? 0xFF00E5FF : 0xFF0284C7);
                }

                int activeId = activeTabId;
                View activeTabView = null;

                Set<Integer> renderedTabIds = new HashSet<>();

                // 1. Render Tab Groups
                for (TabGroup group : tabGroupsList) {
                    List<TabItem> groupTabs = new ArrayList<>();
                    for (int tid : group.tabIds) {
                        TabItem t = getTabById(tid);
                        if (t != null) groupTabs.add(t);
                    }
                    if (groupTabs.isEmpty()) continue;

                    boolean isCollapsed = collapsedStripGroupIds.contains(group.id);
                    int groupCol = 0xFF00E5FF;
                    try { groupCol = Color.parseColor(group.color); } catch (Exception ignored) {}

                    // Inflate Tab Group Chip Pill
                    View groupChip = LayoutInflater.from(this).inflate(R.layout.item_omnibox_tab_group_chip, omniboxTabStripTabs, false);
                    TextView groupIcon = groupChip.findViewById(R.id.omnibox_group_icon);
                    TextView groupTitle = groupChip.findViewById(R.id.omnibox_group_title);
                    TextView groupCount = groupChip.findViewById(R.id.omnibox_group_count);
                    TextView groupChevron = groupChip.findViewById(R.id.omnibox_group_chevron);

                    if (groupIcon != null) groupIcon.setText(group.icon != null ? group.icon : "📁");
                    if (groupTitle != null) {
                        groupTitle.setText(group.title != null ? group.title : "Group");
                        groupTitle.setTextColor(isDarkTheme ? 0xFFFFFFFF : 0xFF0F172A);
                    }
                    if (groupCount != null) {
                        groupCount.setText(String.valueOf(groupTabs.size()));
                        groupCount.setTextColor(groupCol);
                    }
                    if (groupChevron != null) {
                        groupChevron.setText(isCollapsed ? "▼" : "▲");
                        groupChevron.setTextColor(isDarkTheme ? 0xFF94A3B8 : 0xFF64748B);
                    }

                    // Group Pill Background
                    GradientDrawable gBg = new GradientDrawable();
                    gBg.setCornerRadius(dpToPx(17));
                    int alphaTint = (groupCol & 0x00FFFFFF) | 0x22000000;
                    gBg.setColor(isDarkTheme ? alphaTint : 0xFFF1F5F9);
                    gBg.setStroke(dpToPx(2), groupCol);
                    groupChip.setBackground(gBg);

                    groupChip.setOnClickListener(v -> {
                        playUiFeedbackSound("tap");
                        if (collapsedStripGroupIds.contains(group.id)) {
                            collapsedStripGroupIds.remove(group.id);
                        } else {
                            collapsedStripGroupIds.add(group.id);
                        }
                        updateOmniboxTabStrip(false);
                    });

                    omniboxTabStripTabs.addView(groupChip);

                    // If not collapsed, render member tabs
                    if (!isCollapsed) {
                        for (TabItem tab : groupTabs) {
                            renderedTabIds.add(tab.id);
                            boolean isActive = (tab.id == activeId);
                            View tabView = LayoutInflater.from(this).inflate(R.layout.item_omnibox_tab_chip, omniboxTabStripTabs, false);
                            ImageView iconView = tabView.findViewById(R.id.omnibox_tab_icon);
                            TextView titleView = tabView.findViewById(R.id.omnibox_tab_title);
                            View closeBtn = tabView.findViewById(R.id.omnibox_tab_close_btn);
                            ImageView closeIcon = tabView.findViewById(R.id.omnibox_tab_close_icon);

                            // Pill shape with subtle group color accent
                            GradientDrawable bg = new GradientDrawable();
                            bg.setCornerRadius(dpToPx(17));
                            if (isDarkTheme) {
                                if (isActive) {
                                    bg.setColor(0xFF1E2838);
                                    bg.setStroke(dpToPx(2), groupCol);
                                    activeTabView = tabView;
                                } else {
                                    bg.setColor(0xFF0F1420);
                                    bg.setStroke(dpToPx(1), (groupCol & 0x00FFFFFF) | 0x44000000);
                                }
                            } else {
                                if (isActive) {
                                    bg.setColor(0xFFFFFFFF);
                                    bg.setStroke(dpToPx(2), groupCol);
                                    activeTabView = tabView;
                                } else {
                                    bg.setColor(0xFFE8EDF5);
                                    bg.setStroke(dpToPx(1), (groupCol & 0x00FFFFFF) | 0x44000000);
                                }
                            }
                            tabView.setBackground(bg);

                            // Favicon
                            if (iconView != null) {
                                Bitmap favBmp = getTabFaviconBitmap(tab);
                                if (favBmp != null) {
                                    iconView.setImageBitmap(favBmp);
                                    iconView.clearColorFilter();
                                } else {
                                    iconView.setImageResource(getTabServiceIconRes(tab));
                                    iconView.setColorFilter(isActive ? groupCol : (isDarkTheme ? 0xFF64748B : 0xFF64748B));
                                }
                            }

                            // Title
                            if (titleView != null) {
                                String t = tab.title != null && !tab.title.trim().isEmpty() ? tab.title : ("Tab " + tab.id);
                                titleView.setText(t);
                                titleView.setTextColor(isDarkTheme ? (isActive ? 0xFFFFFFFF : 0xFF94A3B8) : (isActive ? 0xFF0F172A : 0xFF64748B));
                                titleView.setTypeface(null, isActive ? Typeface.BOLD : Typeface.NORMAL);
                            }

                            // Close Icon
                            if (closeIcon != null) {
                                closeIcon.setColorFilter(isDarkTheme ? (isActive ? 0xFF94A3B8 : 0xFF64748B) : (isActive ? 0xFF64748B : 0xFF94A3B8));
                            }
                            if (closeBtn != null) {
                                closeBtn.setOnClickListener(v -> {
                                    playUiFeedbackSound("tap");
                                    closeTab(tab.id);
                                    showTabStripUndoButton();
                                });
                            }

                            tabView.setOnClickListener(v -> {
                                playUiFeedbackSound("tap");
                                switchToTab(tab.id);
                            });

                            tabView.setOnLongClickListener(v -> {
                                playUiFeedbackSound("tap");
                                showOmniboxTabContextMenu(tab);
                                return true;
                            });

                            omniboxTabStripTabs.addView(tabView);
                        }
                    } else {
                        // If collapsed, mark member tab IDs as rendered so they don't appear ungrouped
                        for (TabItem tab : groupTabs) {
                            renderedTabIds.add(tab.id);
                        }
                    }
                }

                // 2. Render Interconnected Dual Split Capsule
                if (splitModeState > 0 && secondarySplitTabId != -1) {
                    TabItem sLeft = getTabById(activeTabId);
                    TabItem sRight = getTabById(secondarySplitTabId);
                    if (sLeft != null && sRight != null) {
                        renderedTabIds.add(sLeft.id);
                        renderedTabIds.add(sRight.id);

                        View splitView = LayoutInflater.from(this).inflate(R.layout.item_omnibox_split_tab_chip, omniboxTabStripTabs, false);
                        LinearLayout splitRoot = splitView.findViewById(R.id.omnibox_split_tab_root);
                        View leftHalf = splitView.findViewById(R.id.omnibox_split_left_root);
                        ImageView leftIcon = splitView.findViewById(R.id.omnibox_split_left_icon);
                        TextView leftTitle = splitView.findViewById(R.id.omnibox_split_left_title);
                        View leftClose = splitView.findViewById(R.id.omnibox_split_left_close);
                        ImageView leftCloseIcon = splitView.findViewById(R.id.omnibox_split_left_close_icon);

                        View divider = splitView.findViewById(R.id.omnibox_split_divider);

                        View rightHalf = splitView.findViewById(R.id.omnibox_split_right_root);
                        ImageView rightIcon = splitView.findViewById(R.id.omnibox_split_right_icon);
                        TextView rightTitle = splitView.findViewById(R.id.omnibox_split_right_title);
                        View rightClose = splitView.findViewById(R.id.omnibox_split_right_close);
                        ImageView rightCloseIcon = splitView.findViewById(R.id.omnibox_split_right_close_icon);

                        // Compound outer capsule background
                        GradientDrawable compoundBg = new GradientDrawable();
                        compoundBg.setCornerRadius(dpToPx(17));
                        compoundBg.setColor(isDarkTheme ? 0xFF0F1420 : 0xFFE2E8F0);
                        compoundBg.setStroke(dpToPx(2), isDarkTheme ? 0xFF00E5FF : 0xFF0284C7);
                        splitRoot.setBackground(compoundBg);

                        if (divider != null) {
                            divider.setBackgroundColor(isDarkTheme ? 0x4400E5FF : 0x440284C7);
                        }

                        // Left Half styling
                        if (leftHalf != null) {
                            GradientDrawable lBg = new GradientDrawable();
                            lBg.setCornerRadii(new float[]{dpToPx(15), dpToPx(15), 0, 0, 0, 0, dpToPx(15), dpToPx(15)});
                            lBg.setColor(isDarkTheme ? 0xFF1E2838 : 0xFFFFFFFF);
                            leftHalf.setBackground(lBg);
                        }
                        if (leftIcon != null) {
                            Bitmap fav = getTabFaviconBitmap(sLeft);
                            if (fav != null) {
                                leftIcon.setImageBitmap(fav);
                                leftIcon.clearColorFilter();
                            } else {
                                leftIcon.setImageResource(getTabServiceIconRes(sLeft));
                                leftIcon.setColorFilter(isDarkTheme ? 0xFF00E5FF : 0xFF0284C7);
                            }
                        }
                        if (leftTitle != null) {
                            String t = sLeft.title != null && !sLeft.title.trim().isEmpty() ? sLeft.title : "Tab 1";
                            leftTitle.setText(t);
                            leftTitle.setTextColor(isDarkTheme ? 0xFFFFFFFF : 0xFF0F172A);
                            leftTitle.setTypeface(null, Typeface.BOLD);
                        }
                        if (leftCloseIcon != null) {
                            leftCloseIcon.setColorFilter(isDarkTheme ? 0xFF94A3B8 : 0xFF64748B);
                        }
                        if (leftClose != null) {
                            leftClose.setOnClickListener(v -> {
                                playUiFeedbackSound("tap");
                                closeSplitPane(true);
                                updateOmniboxTabStrip();
                                showTabStripUndoButton();
                            });
                        }
                        if (leftHalf != null) {
                            leftHalf.setOnClickListener(v -> {
                                playUiFeedbackSound("tap");
                                checkAndFocusSplitPane(true);
                            });
                            leftHalf.setOnLongClickListener(v -> {
                                playUiFeedbackSound("tap");
                                showOmniboxTabContextMenu(sLeft);
                                return true;
                            });
                        }

                        // Right Half styling
                        if (rightHalf != null) {
                            GradientDrawable rBg = new GradientDrawable();
                            rBg.setCornerRadii(new float[]{0, 0, dpToPx(15), dpToPx(15), dpToPx(15), dpToPx(15), 0, 0});
                            rBg.setColor(isDarkTheme ? 0xFF162030 : 0xFFF8FAFC);
                            rightHalf.setBackground(rBg);
                        }
                        if (rightIcon != null) {
                            Bitmap fav = getTabFaviconBitmap(sRight);
                            if (fav != null) {
                                rightIcon.setImageBitmap(fav);
                                rightIcon.clearColorFilter();
                            } else {
                                rightIcon.setImageResource(getTabServiceIconRes(sRight));
                                rightIcon.setColorFilter(isDarkTheme ? 0xFF38BDF8 : 0xFF0284C7);
                            }
                        }
                        if (rightTitle != null) {
                            String t = sRight.title != null && !sRight.title.trim().isEmpty() ? sRight.title : "Tab 2";
                            rightTitle.setText(t);
                            rightTitle.setTextColor(isDarkTheme ? 0xFFE2E8F0 : 0xFF1E293B);
                            rightTitle.setTypeface(null, Typeface.BOLD);
                        }
                        if (rightCloseIcon != null) {
                            rightCloseIcon.setColorFilter(isDarkTheme ? 0xFF94A3B8 : 0xFF64748B);
                        }
                        if (rightClose != null) {
                            rightClose.setOnClickListener(v -> {
                                playUiFeedbackSound("tap");
                                closeSplitPane(false);
                                updateOmniboxTabStrip();
                                showTabStripUndoButton();
                            });
                        }
                        if (rightHalf != null) {
                            rightHalf.setOnClickListener(v -> {
                                playUiFeedbackSound("tap");
                                checkAndFocusSplitPane(false);
                            });
                            rightHalf.setOnLongClickListener(v -> {
                                playUiFeedbackSound("tap");
                                showOmniboxTabContextMenu(sRight);
                                return true;
                            });
                        }

                        activeTabView = splitView;
                        omniboxTabStripTabs.addView(splitView);
                    }
                }

                // 3. Render Ungrouped Tabs
                for (int i = 0; i < tabsList.size(); i++) {
                    TabItem tab = tabsList.get(i);
                    if (renderedTabIds.contains(tab.id)) continue;
                    boolean isActive = (tab.id == activeId);

                    View tabView = LayoutInflater.from(this).inflate(R.layout.item_omnibox_tab_chip, omniboxTabStripTabs, false);
                    ImageView iconView = tabView.findViewById(R.id.omnibox_tab_icon);
                    TextView titleView = tabView.findViewById(R.id.omnibox_tab_title);
                    View closeBtn = tabView.findViewById(R.id.omnibox_tab_close_btn);
                    ImageView closeIcon = tabView.findViewById(R.id.omnibox_tab_close_icon);

                    // Pill Shape capsule background (Radius = 17dp for 33dp height)
                    GradientDrawable bg = new GradientDrawable();
                    bg.setCornerRadius(dpToPx(17));
                    if (isDarkTheme) {
                        if (isActive) {
                            bg.setColor(0xFF1E2838);
                            bg.setStroke(dpToPx(1), 0xFF00E5FF);
                            activeTabView = tabView;
                        } else {
                            bg.setColor(0xFF0F1420);
                            bg.setStroke(dpToPx(1), 0xFF1E2533);
                        }
                    } else {
                        // Light Theme
                        if (isActive) {
                            bg.setColor(0xFFFFFFFF);
                            bg.setStroke(dpToPx(1), 0xFF0284C7);
                            activeTabView = tabView;
                        } else {
                            bg.setColor(0xFFE8EDF5);
                            bg.setStroke(dpToPx(1), 0xFFCBD5E1);
                        }
                    }
                    tabView.setBackground(bg);

                    // Real Favicons with fallback to platform service icon
                    if (iconView != null) {
                        Bitmap favBmp = getTabFaviconBitmap(tab);
                        if (favBmp != null) {
                            iconView.setImageBitmap(favBmp);
                            iconView.clearColorFilter();
                        } else {
                            iconView.setImageResource(getTabServiceIconRes(tab));
                            if (isDarkTheme) {
                                iconView.setColorFilter(isActive ? 0xFF00E5FF : 0xFF64748B);
                            } else {
                                iconView.setColorFilter(isActive ? 0xFF0284C7 : 0xFF64748B);
                            }
                        }
                    }

                    // Tab Title
                    if (titleView != null) {
                        String t = tab.title != null && !tab.title.trim().isEmpty() ? tab.title : ("Tab " + (i + 1));
                        titleView.setText(t);
                        if (isDarkTheme) {
                            titleView.setTextColor(isActive ? 0xFFFFFFFF : 0xFF94A3B8);
                        } else {
                            titleView.setTextColor(isActive ? 0xFF0F172A : 0xFF64748B);
                        }
                        titleView.setTypeface(null, isActive ? Typeface.BOLD : Typeface.NORMAL);
                    }

                    // Close Button Icon
                    if (closeIcon != null) {
                        if (isDarkTheme) {
                            closeIcon.setColorFilter(isActive ? 0xFF94A3B8 : 0xFF64748B);
                        } else {
                            closeIcon.setColorFilter(isActive ? 0xFF64748B : 0xFF94A3B8);
                        }
                    }

                    if (closeBtn != null) {
                        closeBtn.setOnClickListener(v -> {
                            playUiFeedbackSound("tap");
                            closeTab(tab.id);
                            showTabStripUndoButton();
                        });
                    }

                    tabView.setOnClickListener(v -> {
                        playUiFeedbackSound("tap");
                        switchToTab(tab.id);
                    });

                    tabView.setOnLongClickListener(v -> {
                        playUiFeedbackSound("tap");
                        showOmniboxTabContextMenu(tab);
                        return true;
                    });

                    omniboxTabStripTabs.addView(tabView);
                }

                if (scrollToActive && activeTabView != null) {
                    final View target = activeTabView;
                    omniboxTabStripScroll.post(() -> {
                        int scrollX = target.getLeft() - (omniboxTabStripScroll.getWidth() / 2) + (target.getWidth() / 2);
                        omniboxTabStripScroll.smoothScrollTo(Math.max(0, scrollX), 0);
                    });
                } else {
                    omniboxTabStripScroll.post(() -> {
                        omniboxTabStripScroll.scrollTo(prevScrollX, 0);
                    });
                }
            } catch (Throwable ignored) {}
        });
    }

    public void showOmniboxTabContextMenu(TabItem tab) {
        if (tab == null) return;
        playUiFeedbackSound("tap");
        com.google.android.material.bottomsheet.BottomSheetDialog dialog =
                new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_omnibox_tab_context_menu, null);
        dialog.setContentView(dialogView);
        if (dialog.getWindow() != null) {
            View bs = dialog.getWindow().findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bs != null) bs.setBackgroundResource(android.R.color.transparent);
        }

        // Apply Light vs Dark theme to Tab Context Menu BottomSheet
        GradientDrawable rootBg = new GradientDrawable();
        rootBg.setCornerRadii(new float[]{dpToPx(20), dpToPx(20), dpToPx(20), dpToPx(20), 0, 0, 0, 0});
        rootBg.setColor(isDarkTheme ? 0xFF0A0E17 : 0xFFFFFFFF);
        rootBg.setStroke(dpToPx(1), isDarkTheme ? 0xFF1E2433 : 0xFFE2E8F0);
        dialogView.setBackground(rootBg);

        ImageView iconView = dialogView.findViewById(R.id.ctx_tab_icon);
        TextView titleView = dialogView.findViewById(R.id.ctx_tab_title);
        TextView urlView = dialogView.findViewById(R.id.ctx_tab_url);

        Bitmap favBmp = getTabFaviconBitmap(tab);
        if (iconView != null) {
            if (favBmp != null) {
                iconView.setImageBitmap(favBmp);
                iconView.clearColorFilter();
            } else {
                iconView.setImageResource(getTabServiceIconRes(tab));
                iconView.setColorFilter(isDarkTheme ? 0xFF00E5FF : 0xFF0284C7);
            }
        }
        if (titleView != null) {
            titleView.setText(tab.title != null && !tab.title.isEmpty() ? tab.title : "Untitled Tab");
            titleView.setTextColor(isDarkTheme ? 0xFFFFFFFF : 0xFF0F172A);
        }
        if (urlView != null) {
            urlView.setText(cleanDisplayUrl(tab.url != null ? tab.url : ""));
            urlView.setTextColor(0xFF64748B);
        }

        int actionTextColor = isDarkTheme ? 0xFFE2E8F0 : 0xFF1E293B;
        int[] actionIds = new int[]{
                R.id.ctx_action_close_others,
                R.id.ctx_action_duplicate,
                R.id.ctx_action_split_screen,
                R.id.ctx_action_split_exit,
                R.id.ctx_action_split_swap,
                R.id.ctx_action_move_cask,
                R.id.ctx_action_move_tab_group,
                R.id.ctx_action_make_tab_group,
                R.id.ctx_action_share,
                R.id.ctx_action_copy_url
        };
        for (int id : actionIds) {
            View row = dialogView.findViewById(id);
            if (row instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup) row;
                for (int c = 0; c < vg.getChildCount(); c++) {
                    View child = vg.getChildAt(c);
                    if (child instanceof TextView) {
                        ((TextView) child).setTextColor(actionTextColor);
                    }
                }
            }
        }

        // 1. Close Tab
        View actionClose = dialogView.findViewById(R.id.ctx_action_close_tab);
        if (actionClose != null) {
            actionClose.setOnClickListener(v -> {
                dialog.dismiss();
                playUiFeedbackSound("tap");
                closeTab(tab.id);
            });
        }

        // 2. Close Other Tabs
        View actionCloseOthers = dialogView.findViewById(R.id.ctx_action_close_others);
        if (actionCloseOthers != null) {
            actionCloseOthers.setOnClickListener(v -> {
                dialog.dismiss();
                playUiFeedbackSound("tap");
                List<Integer> idsToClose = new ArrayList<>();
                for (TabItem t : tabsList) {
                    if (t.id != tab.id) idsToClose.add(t.id);
                }
                for (int id : idsToClose) {
                    closeTab(id);
                }
            });
        }

        // 3. Duplicate Tab
        View actionDuplicate = dialogView.findViewById(R.id.ctx_action_duplicate);
        if (actionDuplicate != null) {
            actionDuplicate.setOnClickListener(v -> {
                dialog.dismiss();
                playUiFeedbackSound("tap");
                addNewTab(tab.service, tab.pendingPrompt, tab.url, tab.isIncognito, tab.caskId, true);
            });
        }

        // Split Screen Actions
        boolean isTabInSplit = (splitModeState > 0 && (tab.id == activeTabId || tab.id == secondarySplitTabId));
        View actionSplit = dialogView.findViewById(R.id.ctx_action_split_screen);
        View actionSplitExit = dialogView.findViewById(R.id.ctx_action_split_exit);
        View actionSplitSwap = dialogView.findViewById(R.id.ctx_action_split_swap);

        if (actionSplit != null) {
            actionSplit.setVisibility(isTabInSplit ? View.GONE : View.VISIBLE);
            actionSplit.setOnClickListener(v -> {
                dialog.dismiss();
                playUiFeedbackSound("tap");
                showSplitPickerForTab(tab);
            });
        }
        if (actionSplitExit != null) {
            actionSplitExit.setVisibility(isTabInSplit ? View.VISIBLE : View.GONE);
            actionSplitExit.setOnClickListener(v -> {
                dialog.dismiss();
                playUiFeedbackSound("tap");
                exitSplitView();
                updateOmniboxTabStrip();
                Toast.makeText(this, "Exited Split Screen", Toast.LENGTH_SHORT).show();
            });
        }
        if (actionSplitSwap != null) {
            actionSplitSwap.setVisibility(isTabInSplit ? View.VISIBLE : View.GONE);
            actionSplitSwap.setOnClickListener(v -> {
                dialog.dismiss();
                playUiFeedbackSound("tap");
                swapSplitTabs();
                updateOmniboxTabStrip();
            });
        }

        // 4. Move to Cask
        View actionMoveCask = dialogView.findViewById(R.id.ctx_action_move_cask);
        if (actionMoveCask != null) {
            actionMoveCask.setOnClickListener(v -> {
                dialog.dismiss();
                playUiFeedbackSound("tap");
                showMoveTabToCaskDialog(tab);
            });
        }

        // 5. Move to Tab Group
        View actionMoveGroup = dialogView.findViewById(R.id.ctx_action_move_tab_group);
        if (actionMoveGroup != null) {
            actionMoveGroup.setOnClickListener(v -> {
                dialog.dismiss();
                playUiFeedbackSound("tap");
                showMoveTabToGroupDialog(tab);
            });
        }

        // 6. Make a Tab Group
        View actionMakeGroup = dialogView.findViewById(R.id.ctx_action_make_tab_group);
        if (actionMakeGroup != null) {
            actionMakeGroup.setOnClickListener(v -> {
                dialog.dismiss();
                playUiFeedbackSound("tap");
                showCreateTabGroupDialog(tab);
            });
        }

        // 7. Share Tab
        View actionShare = dialogView.findViewById(R.id.ctx_action_share);
        if (actionShare != null) {
            actionShare.setOnClickListener(v -> {
                dialog.dismiss();
                playUiFeedbackSound("tap");
                try {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, tab.title);
                    shareIntent.putExtra(Intent.EXTRA_TEXT, tab.url);
                    startActivity(Intent.createChooser(shareIntent, "Share Tab via"));
                } catch (Throwable ignored) {}
            });
        }

        // 8. Copy URL
        View actionCopy = dialogView.findViewById(R.id.ctx_action_copy_url);
        if (actionCopy != null) {
            actionCopy.setOnClickListener(v -> {
                dialog.dismiss();
                playUiFeedbackSound("tap");
                try {
                    ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    cm.setPrimaryClip(ClipData.newPlainText("URL", tab.url));
                    Toast.makeText(this, "Copied URL to clipboard", Toast.LENGTH_SHORT).show();
                } catch (Throwable ignored) {}
            });
        }

        dialog.show();
    }

    public void enterSplitMode(int tabId1, int tabId2) {
        activeTabId = tabId1;
        secondarySplitTabId = tabId2;
        splitModeState = 1; // Horizontal Split
        splitRatio = 0.5f;
        applySplitViewLayout();
        updateOmniboxState();
        updateOmniboxTabStrip();
        Toast.makeText(this, "🔀 Split Screen Active", Toast.LENGTH_SHORT).show();
    }

    public void showSplitPickerForTab(TabItem primaryTab) {
        if (primaryTab == null) return;
        playUiFeedbackSound("tap");
        com.google.android.material.bottomsheet.BottomSheetDialog dialog =
                new com.google.android.material.bottomsheet.BottomSheetDialog(this);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dpToPx(20), dpToPx(16), dpToPx(20), dpToPx(24));

        GradientDrawable rootBg = new GradientDrawable();
        rootBg.setCornerRadii(new float[]{dpToPx(20), dpToPx(20), dpToPx(20), dpToPx(20), 0, 0, 0, 0});
        rootBg.setColor(isDarkTheme ? 0xFF0A0E17 : 0xFFFFFFFF);
        rootBg.setStroke(dpToPx(1), isDarkTheme ? 0xFF1E2433 : 0xFFE2E8F0);
        root.setBackground(rootBg);

        // Header Handle
        View handle = new View(this);
        LinearLayout.LayoutParams handleLp = new LinearLayout.LayoutParams(dpToPx(36), dpToPx(4));
        handleLp.gravity = Gravity.CENTER_HORIZONTAL;
        handleLp.bottomMargin = dpToPx(14);
        handle.setLayoutParams(handleLp);
        GradientDrawable hBg = new GradientDrawable();
        hBg.setColor(isDarkTheme ? 0xFF30363D : 0xFFCBD5E1);
        hBg.setCornerRadius(dpToPx(2));
        handle.setBackground(hBg);
        root.addView(handle);

        TextView titleTv = new TextView(this);
        titleTv.setText("🔀 Split Screen with...");
        titleTv.setTextColor(isDarkTheme ? 0xFFFFFFFF : 0xFF0F172A);
        titleTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
        titleTv.setTypeface(null, Typeface.BOLD);
        root.addView(titleTv);

        TextView subTv = new TextView(this);
        subTv.setText("Select a tab to view side-by-side with \"" + (primaryTab.title != null ? primaryTab.title : "Tab") + "\"");
        subTv.setTextColor(isDarkTheme ? 0xFF94A3B8 : 0xFF64748B);
        subTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        subTv.setPadding(0, dpToPx(2), 0, dpToPx(14));
        root.addView(subTv);

        // Option 1: New Blank Tab
        LinearLayout newTabRow = new LinearLayout(this);
        newTabRow.setOrientation(LinearLayout.HORIZONTAL);
        newTabRow.setGravity(Gravity.CENTER_VERTICAL);
        newTabRow.setPadding(dpToPx(14), dpToPx(12), dpToPx(14), dpToPx(12));
        GradientDrawable ntb = new GradientDrawable();
        ntb.setCornerRadius(dpToPx(12));
        ntb.setColor(isDarkTheme ? 0xFF162235 : 0xFFE0F2FE);
        ntb.setStroke(dpToPx(1), isDarkTheme ? 0xFF00E5FF : 0xFF0284C7);
        newTabRow.setBackground(ntb);
        LinearLayout.LayoutParams ntLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ntLp.bottomMargin = dpToPx(10);
        newTabRow.setLayoutParams(ntLp);

        TextView ntIcon = new TextView(this);
        ntIcon.setText("➕");
        ntIcon.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
        ntIcon.setPadding(0, 0, dpToPx(10), 0);
        newTabRow.addView(ntIcon);

        TextView ntText = new TextView(this);
        ntText.setText("Open New Search Tab in Split Screen");
        ntText.setTextColor(isDarkTheme ? 0xFF00E5FF : 0xFF0284C7);
        ntText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        ntText.setTypeface(null, Typeface.BOLD);
        newTabRow.addView(ntText);

        newTabRow.setOnClickListener(v -> {
            dialog.dismiss();
            playUiFeedbackSound("tap");
            int id = nextTabId++;
            TabItem secondTab = createNewTabInstance(id, "https://google.com", "google", null, false);
            tabsList.add(secondTab);
            enterSplitMode(primaryTab.id, secondTab.id);
        });
        root.addView(newTabRow);

        ScrollView scroll = new ScrollView(this);
        LinearLayout.LayoutParams scrollLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(200));
        scroll.setLayoutParams(scrollLp);
        LinearLayout listLayout = new LinearLayout(this);
        listLayout.setOrientation(LinearLayout.VERTICAL);

        for (TabItem t : tabsList) {
            if (t.id == primaryTab.id) continue;

            LinearLayout tRow = new LinearLayout(this);
            tRow.setOrientation(LinearLayout.HORIZONTAL);
            tRow.setGravity(Gravity.CENTER_VERTICAL);
            tRow.setPadding(dpToPx(12), dpToPx(10), dpToPx(12), dpToPx(10));

            GradientDrawable rowBg = new GradientDrawable();
            rowBg.setCornerRadius(dpToPx(12));
            rowBg.setColor(isDarkTheme ? 0xFF121620 : 0xFFF8FAFC);
            rowBg.setStroke(dpToPx(1), isDarkTheme ? 0xFF1E2433 : 0xFFE2E8F0);
            tRow.setBackground(rowBg);

            LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rowLp.bottomMargin = dpToPx(6);
            tRow.setLayoutParams(rowLp);

            ImageView iv = new ImageView(this);
            LinearLayout.LayoutParams ivLp = new LinearLayout.LayoutParams(dpToPx(18), dpToPx(18));
            ivLp.setMarginEnd(dpToPx(10));
            iv.setLayoutParams(ivLp);
            Bitmap fav = getTabFaviconBitmap(t);
            if (fav != null) {
                iv.setImageBitmap(fav);
            } else {
                iv.setImageResource(getTabServiceIconRes(t));
                iv.setColorFilter(isDarkTheme ? 0xFF00E5FF : 0xFF0284C7);
            }
            tRow.addView(iv);

            LinearLayout textCol = new LinearLayout(this);
            textCol.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams tcLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            textCol.setLayoutParams(tcLp);

            TextView nameTv = new TextView(this);
            nameTv.setText(t.title != null && !t.title.isEmpty() ? t.title : ("Tab " + t.id));
            nameTv.setTextColor(isDarkTheme ? 0xFFFFFFFF : 0xFF0F172A);
            nameTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
            nameTv.setTypeface(null, Typeface.BOLD);
            nameTv.setSingleLine(true);
            nameTv.setEllipsize(android.text.TextUtils.TruncateAt.END);
            textCol.addView(nameTv);

            TextView urlTv = new TextView(this);
            urlTv.setText(cleanDisplayUrl(t.url != null ? t.url : ""));
            urlTv.setTextColor(isDarkTheme ? 0xFF94A3B8 : 0xFF64748B);
            urlTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
            urlTv.setSingleLine(true);
            urlTv.setEllipsize(android.text.TextUtils.TruncateAt.END);
            textCol.addView(urlTv);

            tRow.addView(textCol);

            tRow.setOnClickListener(v -> {
                dialog.dismiss();
                playUiFeedbackSound("tap");
                enterSplitMode(primaryTab.id, t.id);
            });

            listLayout.addView(tRow);
        }

        scroll.addView(listLayout);
        root.addView(scroll);

        dialog.setContentView(root);
        if (dialog.getWindow() != null) {
            View bs = dialog.getWindow().findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bs != null) bs.setBackgroundResource(android.R.color.transparent);
        }
        dialog.show();
    }

    public void showMoveTabToGroupDialog(TabItem tab) {
        if (tab == null) return;
        playUiFeedbackSound("tap");
        com.google.android.material.bottomsheet.BottomSheetDialog dialog =
                new com.google.android.material.bottomsheet.BottomSheetDialog(this);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dpToPx(20), dpToPx(16), dpToPx(20), dpToPx(24));

        GradientDrawable rootBg = new GradientDrawable();
        rootBg.setCornerRadii(new float[]{dpToPx(20), dpToPx(20), dpToPx(20), dpToPx(20), 0, 0, 0, 0});
        rootBg.setColor(isDarkTheme ? 0xFF0A0E17 : 0xFFFFFFFF);
        rootBg.setStroke(dpToPx(1), isDarkTheme ? 0xFF1E2433 : 0xFFE2E8F0);
        root.setBackground(rootBg);

        // Header Handle
        View handle = new View(this);
        LinearLayout.LayoutParams handleLp = new LinearLayout.LayoutParams(dpToPx(36), dpToPx(4));
        handleLp.gravity = Gravity.CENTER_HORIZONTAL;
        handleLp.bottomMargin = dpToPx(14);
        handle.setLayoutParams(handleLp);
        GradientDrawable hBg = new GradientDrawable();
        hBg.setColor(isDarkTheme ? 0xFF30363D : 0xFFCBD5E1);
        hBg.setCornerRadius(dpToPx(2));
        handle.setBackground(hBg);
        root.addView(handle);

        // Title
        TextView titleTv = new TextView(this);
        titleTv.setText("Move Tab to Group");
        titleTv.setTextColor(isDarkTheme ? 0xFFFFFFFF : 0xFF0F172A);
        titleTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
        titleTv.setTypeface(null, Typeface.BOLD);
        root.addView(titleTv);

        TextView subTv = new TextView(this);
        subTv.setText(tab.title != null && !tab.title.isEmpty() ? tab.title : "Current Tab");
        subTv.setTextColor(isDarkTheme ? 0xFF94A3B8 : 0xFF64748B);
        subTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        subTv.setSingleLine(true);
        subTv.setEllipsize(android.text.TextUtils.TruncateAt.END);
        subTv.setPadding(0, dpToPx(2), 0, dpToPx(14));
        root.addView(subTv);

        // Find current group if any
        TabGroup currentGroup = null;
        for (TabGroup g : tabGroupsList) {
            if (g.tabIds.contains(tab.id)) {
                currentGroup = g;
                break;
            }
        }

        // If in a group, allow removing from group
        if (currentGroup != null) {
            LinearLayout removeRow = new LinearLayout(this);
            removeRow.setOrientation(LinearLayout.HORIZONTAL);
            removeRow.setGravity(Gravity.CENTER_VERTICAL);
            removeRow.setPadding(dpToPx(14), dpToPx(12), dpToPx(14), dpToPx(12));
            GradientDrawable remBg = new GradientDrawable();
            remBg.setCornerRadius(dpToPx(12));
            remBg.setColor(isDarkTheme ? 0xFF1E1719 : 0xFFFEF2F2);
            remBg.setStroke(dpToPx(1), isDarkTheme ? 0xFF4D1720 : 0xFFFECACA);
            removeRow.setBackground(remBg);
            LinearLayout.LayoutParams remLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            remLp.bottomMargin = dpToPx(12);
            removeRow.setLayoutParams(remLp);

            TextView remIcon = new TextView(this);
            remIcon.setText("✕");
            remIcon.setTextColor(0xFFEF4444);
            remIcon.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
            remIcon.setPadding(0, 0, dpToPx(10), 0);
            removeRow.addView(remIcon);

            TextView remText = new TextView(this);
            remText.setText("Remove from current group (" + currentGroup.title + ")");
            remText.setTextColor(0xFFEF4444);
            remText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
            remText.setTypeface(null, Typeface.BOLD);
            removeRow.addView(remText);

            removeRow.setOnClickListener(v -> {
                dialog.dismiss();
                playUiFeedbackSound("tap");
                for (TabGroup g : tabGroupsList) {
                    g.tabIds.remove((Integer) tab.id);
                }
                tabGroupsList.removeIf(g -> g.tabIds.isEmpty());
                saveTabGroups();
                saveOpenTabsState();
                updateOmniboxTabStrip();
                Toast.makeText(this, "Removed from group", Toast.LENGTH_SHORT).show();
            });
            root.addView(removeRow);
        }

        ScrollView scroll = new ScrollView(this);
        LinearLayout.LayoutParams scrollLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(200));
        scroll.setLayoutParams(scrollLp);
        LinearLayout listLayout = new LinearLayout(this);
        listLayout.setOrientation(LinearLayout.VERTICAL);

        for (TabGroup group : tabGroupsList) {
            boolean isCur = (currentGroup != null && currentGroup.id.equals(group.id));
            LinearLayout gRow = new LinearLayout(this);
            gRow.setOrientation(LinearLayout.HORIZONTAL);
            gRow.setGravity(Gravity.CENTER_VERTICAL);
            gRow.setPadding(dpToPx(12), dpToPx(10), dpToPx(12), dpToPx(10));

            GradientDrawable rowBg = new GradientDrawable();
            rowBg.setCornerRadius(dpToPx(12));
            rowBg.setColor(isDarkTheme ? 0xFF121620 : 0xFFF8FAFC);
            int groupCol = 0xFF00E5FF;
            try { groupCol = Color.parseColor(group.color); } catch (Exception ignored) {}
            rowBg.setStroke(dpToPx(1), isCur ? groupCol : (isDarkTheme ? 0xFF1E2433 : 0xFFE2E8F0));
            gRow.setBackground(rowBg);

            LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rowLp.bottomMargin = dpToPx(8);
            gRow.setLayoutParams(rowLp);

            TextView iconTv = new TextView(this);
            iconTv.setText(group.icon != null ? group.icon : "📁");
            iconTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
            iconTv.setPadding(0, 0, dpToPx(10), 0);
            gRow.addView(iconTv);

            LinearLayout textCol = new LinearLayout(this);
            textCol.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams tcLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            textCol.setLayoutParams(tcLp);

            TextView nameTv = new TextView(this);
            nameTv.setText(group.title);
            nameTv.setTextColor(isDarkTheme ? 0xFFFFFFFF : 0xFF0F172A);
            nameTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
            nameTv.setTypeface(null, Typeface.BOLD);
            textCol.addView(nameTv);

            TextView cntTv = new TextView(this);
            cntTv.setText(group.tabIds.size() + " tabs");
            cntTv.setTextColor(isDarkTheme ? 0xFF94A3B8 : 0xFF64748B);
            cntTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
            textCol.addView(cntTv);
            gRow.addView(textCol);

            if (isCur) {
                TextView chk = new TextView(this);
                chk.setText("✓ Current");
                chk.setTextColor(groupCol);
                chk.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
                chk.setTypeface(null, Typeface.BOLD);
                gRow.addView(chk);
            }

            gRow.setOnClickListener(v -> {
                dialog.dismiss();
                playUiFeedbackSound("tap");
                addTabToGroup(tab.id, group.id);
                updateOmniboxTabStrip();
            });

            listLayout.addView(gRow);
        }

        scroll.addView(listLayout);
        root.addView(scroll);

        // "+ Create New Tab Group" button
        LinearLayout createBtn = new LinearLayout(this);
        createBtn.setOrientation(LinearLayout.HORIZONTAL);
        createBtn.setGravity(Gravity.CENTER);
        createBtn.setPadding(dpToPx(14), dpToPx(12), dpToPx(14), dpToPx(12));
        GradientDrawable crBg = new GradientDrawable();
        crBg.setCornerRadius(dpToPx(12));
        crBg.setColor(isDarkTheme ? 0xFF162235 : 0xFFE0F2FE);
        crBg.setStroke(dpToPx(1), isDarkTheme ? 0xFF00E5FF : 0xFF0284C7);
        createBtn.setBackground(crBg);
        LinearLayout.LayoutParams crLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        crLp.topMargin = dpToPx(8);
        createBtn.setLayoutParams(crLp);

        TextView crText = new TextView(this);
        crText.setText("+ Create New Group for this Tab");
        crText.setTextColor(isDarkTheme ? 0xFF00E5FF : 0xFF0284C7);
        crText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        crText.setTypeface(null, Typeface.BOLD);
        createBtn.addView(crText);

        createBtn.setOnClickListener(v -> {
            dialog.dismiss();
            playUiFeedbackSound("tap");
            showCreateTabGroupDialog(tab);
        });
        root.addView(createBtn);

        dialog.setContentView(root);
        if (dialog.getWindow() != null) {
            View bs = dialog.getWindow().findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bs != null) bs.setBackgroundResource(android.R.color.transparent);
        }
        dialog.show();
    }

    public void showCreateTabGroupDialog(TabItem initialTab) {
        playUiFeedbackSound("tap");
        com.google.android.material.bottomsheet.BottomSheetDialog dialog =
                new com.google.android.material.bottomsheet.BottomSheetDialog(this);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dpToPx(20), dpToPx(16), dpToPx(20), dpToPx(20));

        GradientDrawable rootBg = new GradientDrawable();
        rootBg.setCornerRadii(new float[]{dpToPx(20), dpToPx(20), dpToPx(20), dpToPx(20), 0, 0, 0, 0});
        rootBg.setColor(isDarkTheme ? 0xFF0A0E17 : 0xFFFFFFFF);
        rootBg.setStroke(dpToPx(1), isDarkTheme ? 0xFF1E2433 : 0xFFE2E8F0);
        root.setBackground(rootBg);

        // Header Handle
        View handle = new View(this);
        LinearLayout.LayoutParams handleLp = new LinearLayout.LayoutParams(dpToPx(36), dpToPx(4));
        handleLp.gravity = Gravity.CENTER_HORIZONTAL;
        handleLp.bottomMargin = dpToPx(12);
        handle.setLayoutParams(handleLp);
        GradientDrawable hBg = new GradientDrawable();
        hBg.setColor(isDarkTheme ? 0xFF30363D : 0xFFCBD5E1);
        hBg.setCornerRadius(dpToPx(2));
        handle.setBackground(hBg);
        root.addView(handle);

        TextView titleTv = new TextView(this);
        titleTv.setText("📁 Create Tab Group");
        titleTv.setTextColor(isDarkTheme ? 0xFFFFFFFF : 0xFF0F172A);
        titleTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
        titleTv.setTypeface(null, Typeface.BOLD);
        root.addView(titleTv);

        TextView subTv = new TextView(this);
        subTv.setText("Customize name, emoji, color, and tab members");
        subTv.setTextColor(isDarkTheme ? 0xFF94A3B8 : 0xFF64748B);
        subTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
        subTv.setPadding(0, dpToPx(2), 0, dpToPx(12));
        root.addView(subTv);

        // Group Name Input
        EditText nameInput = new EditText(this);
        nameInput.setHint("Group Name (e.g., Work, Research, AI)");
        nameInput.setHintTextColor(isDarkTheme ? 0xFF64748B : 0xFF94A3B8);
        nameInput.setTextColor(isDarkTheme ? 0xFFFFFFFF : 0xFF0F172A);
        nameInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        nameInput.setSingleLine(true);
        GradientDrawable niBg = new GradientDrawable();
        niBg.setCornerRadius(dpToPx(10));
        niBg.setColor(isDarkTheme ? 0xFF121620 : 0xFFF1F5F9);
        niBg.setStroke(dpToPx(1), isDarkTheme ? 0xFF1E2433 : 0xFFCBD5E1);
        nameInput.setBackground(niBg);
        nameInput.setPadding(dpToPx(12), dpToPx(10), dpToPx(12), dpToPx(10));
        LinearLayout.LayoutParams niLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        niLp.bottomMargin = dpToPx(12);
        nameInput.setLayoutParams(niLp);
        root.addView(nameInput);

        // Emoji Swatches
        final String[] emojis = new String[]{"📁", "🚀", "💡", "🔬", "🛒", "🎮", "📚", "🎨", "⚡", "🌐", "💼", "🔥"};
        final String[] selectedEmoji = new String[]{"📁"};
        HorizontalScrollView emojiScroll = new HorizontalScrollView(this);
        emojiScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout emojiRow = new LinearLayout(this);
        emojiRow.setOrientation(LinearLayout.HORIZONTAL);
        final List<TextView> emojiViews = new ArrayList<>();
        for (String em : emojis) {
            TextView emTv = new TextView(this);
            emTv.setText(em);
            emTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f);
            emTv.setGravity(Gravity.CENTER);
            int p = dpToPx(8);
            emTv.setPadding(p, p, p, p);
            GradientDrawable emBg = new GradientDrawable();
            emBg.setCornerRadius(dpToPx(8));
            boolean isSel = em.equals(selectedEmoji[0]);
            emBg.setColor(isSel ? (isDarkTheme ? 0xFF1E2838 : 0xFFE0F2FE) : Color.TRANSPARENT);
            emBg.setStroke(dpToPx(1), isSel ? (isDarkTheme ? 0xFF00E5FF : 0xFF0284C7) : Color.TRANSPARENT);
            emTv.setBackground(emBg);
            LinearLayout.LayoutParams emLp = new LinearLayout.LayoutParams(dpToPx(40), dpToPx(40));
            emLp.setMarginEnd(dpToPx(6));
            emTv.setLayoutParams(emLp);

            emTv.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                selectedEmoji[0] = em;
                for (int i = 0; i < emojis.length; i++) {
                    TextView t = emojiViews.get(i);
                    boolean cur = emojis[i].equals(selectedEmoji[0]);
                    GradientDrawable bg = new GradientDrawable();
                    bg.setCornerRadius(dpToPx(8));
                    bg.setColor(cur ? (isDarkTheme ? 0xFF1E2838 : 0xFFE0F2FE) : Color.TRANSPARENT);
                    bg.setStroke(dpToPx(1), cur ? (isDarkTheme ? 0xFF00E5FF : 0xFF0284C7) : Color.TRANSPARENT);
                    t.setBackground(bg);
                }
            });
            emojiViews.add(emTv);
            emojiRow.addView(emTv);
        }
        emojiScroll.addView(emojiRow);
        LinearLayout.LayoutParams emScrollLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        emScrollLp.bottomMargin = dpToPx(12);
        emojiScroll.setLayoutParams(emScrollLp);
        root.addView(emojiScroll);

        // Color Swatches
        final String[] colors = new String[]{"#00E5FF", "#3B82F6", "#8B5CF6", "#EC4899", "#EF4444", "#F59E0B", "#10B981", "#64748B"};
        final String[] selectedColor = new String[]{"#00E5FF"};
        LinearLayout colorRow = new LinearLayout(this);
        colorRow.setOrientation(LinearLayout.HORIZONTAL);
        final List<View> colorViews = new ArrayList<>();
        for (String colStr : colors) {
            int parsedColor = 0xFF00E5FF;
            try { parsedColor = Color.parseColor(colStr); } catch (Exception ignored) {}
            FrameLayout colBox = new FrameLayout(this);
            LinearLayout.LayoutParams colLp = new LinearLayout.LayoutParams(dpToPx(32), dpToPx(32));
            colLp.setMarginEnd(dpToPx(8));
            colBox.setLayoutParams(colLp);

            GradientDrawable colGd = new GradientDrawable();
            colGd.setShape(GradientDrawable.OVAL);
            colGd.setColor(parsedColor);
            boolean isSel = colStr.equals(selectedColor[0]);
            colGd.setStroke(dpToPx(isSel ? 3 : 1), isSel ? (isDarkTheme ? 0xFFFFFFFF : 0xFF0F172A) : 0x44FFFFFF);
            colBox.setBackground(colGd);

            colBox.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                selectedColor[0] = colStr;
                for (int i = 0; i < colors.length; i++) {
                    View b = colorViews.get(i);
                    boolean cur = colors[i].equals(selectedColor[0]);
                    int c = 0xFF00E5FF;
                    try { c = Color.parseColor(colors[i]); } catch (Exception ignored) {}
                    GradientDrawable bg = new GradientDrawable();
                    bg.setShape(GradientDrawable.OVAL);
                    bg.setColor(c);
                    bg.setStroke(dpToPx(cur ? 3 : 1), cur ? (isDarkTheme ? 0xFFFFFFFF : 0xFF0F172A) : 0x44FFFFFF);
                    b.setBackground(bg);
                }
            });
            colorViews.add(colBox);
            colorRow.addView(colBox);
        }
        LinearLayout.LayoutParams crLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        crLp.bottomMargin = dpToPx(14);
        colorRow.setLayoutParams(crLp);
        root.addView(colorRow);

        // Multi-Tab Selection Section
        TextView tabSelHeader = new TextView(this);
        tabSelHeader.setText("Include Tabs in Group:");
        tabSelHeader.setTextColor(isDarkTheme ? 0xFFE2E8F0 : 0xFF1E293B);
        tabSelHeader.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        tabSelHeader.setTypeface(null, Typeface.BOLD);
        tabSelHeader.setPadding(0, 0, 0, dpToPx(6));
        root.addView(tabSelHeader);

        ScrollView tabScroll = new ScrollView(this);
        LinearLayout.LayoutParams tsLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(140));
        tsLp.bottomMargin = dpToPx(14);
        tabScroll.setLayoutParams(tsLp);

        LinearLayout tabListLayout = new LinearLayout(this);
        tabListLayout.setOrientation(LinearLayout.VERTICAL);

        final Set<Integer> selectedTabIdsForNewGroup = new HashSet<>();
        if (initialTab != null) selectedTabIdsForNewGroup.add(initialTab.id);

        for (TabItem t : tabsList) {
            LinearLayout tRow = new LinearLayout(this);
            tRow.setOrientation(LinearLayout.HORIZONTAL);
            tRow.setGravity(Gravity.CENTER_VERTICAL);
            tRow.setPadding(dpToPx(10), dpToPx(8), dpToPx(10), dpToPx(8));
            GradientDrawable trBg = new GradientDrawable();
            trBg.setCornerRadius(dpToPx(8));
            trBg.setColor(isDarkTheme ? 0xFF121620 : 0xFFF8FAFC);
            trBg.setStroke(dpToPx(1), isDarkTheme ? 0xFF1E2433 : 0xFFE2E8F0);
            tRow.setBackground(trBg);

            LinearLayout.LayoutParams tLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            tLp.bottomMargin = dpToPx(5);
            tRow.setLayoutParams(tLp);

            TextView chkBox = new TextView(this);
            boolean isChecked = selectedTabIdsForNewGroup.contains(t.id);
            chkBox.setText(isChecked ? "☑" : "☐");
            chkBox.setTextColor(isChecked ? (isDarkTheme ? 0xFF00E5FF : 0xFF0284C7) : 0xFF64748B);
            chkBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
            chkBox.setPadding(0, 0, dpToPx(10), 0);
            tRow.addView(chkBox);

            TextView tTitle = new TextView(this);
            tTitle.setText(t.title != null && !t.title.isEmpty() ? t.title : ("Tab " + t.id));
            tTitle.setTextColor(isDarkTheme ? 0xFFFFFFFF : 0xFF0F172A);
            tTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
            tTitle.setSingleLine(true);
            tTitle.setEllipsize(android.text.TextUtils.TruncateAt.END);
            tRow.addView(tTitle);

            tRow.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                if (selectedTabIdsForNewGroup.contains(t.id)) {
                    if (selectedTabIdsForNewGroup.size() > 1) {
                        selectedTabIdsForNewGroup.remove(t.id);
                        chkBox.setText("☐");
                        chkBox.setTextColor(0xFF64748B);
                    }
                } else {
                    selectedTabIdsForNewGroup.add(t.id);
                    chkBox.setText("☑");
                    chkBox.setTextColor(isDarkTheme ? 0xFF00E5FF : 0xFF0284C7);
                }
            });

            tabListLayout.addView(tRow);
        }
        tabScroll.addView(tabListLayout);
        root.addView(tabScroll);

        // Buttons row
        LinearLayout btnRow = new LinearLayout(this);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView cancelBtn = new TextView(this);
        cancelBtn.setText("Cancel");
        cancelBtn.setTextColor(isDarkTheme ? 0xFF94A3B8 : 0xFF64748B);
        cancelBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        cancelBtn.setGravity(Gravity.CENTER);
        cancelBtn.setPadding(dpToPx(16), dpToPx(10), dpToPx(16), dpToPx(10));
        cancelBtn.setOnClickListener(v -> dialog.dismiss());
        LinearLayout.LayoutParams cancelLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        cancelBtn.setLayoutParams(cancelLp);
        btnRow.addView(cancelBtn);

        TextView createBtn = new TextView(this);
        createBtn.setText("Create Group");
        createBtn.setTextColor(isDarkTheme ? 0xFF0A0E17 : 0xFFFFFFFF);
        createBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        createBtn.setTypeface(null, Typeface.BOLD);
        createBtn.setGravity(Gravity.CENTER);
        GradientDrawable cbBg = new GradientDrawable();
        cbBg.setCornerRadius(dpToPx(10));
        cbBg.setColor(isDarkTheme ? 0xFF00E5FF : 0xFF0284C7);
        createBtn.setBackground(cbBg);
        createBtn.setPadding(dpToPx(16), dpToPx(10), dpToPx(16), dpToPx(10));
        LinearLayout.LayoutParams createLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        createBtn.setLayoutParams(createLp);

        createBtn.setOnClickListener(v -> {
            playUiFeedbackSound("tap");
            String name = nameInput.getText().toString().trim();
            if (name.isEmpty()) name = "Group " + (tabGroupsList.size() + 1);

            TabGroup newGroup = new TabGroup("group_" + System.currentTimeMillis(), name, selectedColor[0], selectedEmoji[0]);
            newGroup.tabIds.addAll(selectedTabIdsForNewGroup);

            for (TabGroup g : tabGroupsList) {
                g.tabIds.removeAll(selectedTabIdsForNewGroup);
            }
            tabGroupsList.removeIf(g -> g.tabIds.isEmpty());
            tabGroupsList.add(newGroup);
            saveTabGroups();
            saveOpenTabsState();
            updateOmniboxTabStrip();
            if (tabGridSearchInput != null) {
                renderTabGridCards(tabGridSearchInput.getText().toString());
            }
            dialog.dismiss();
            Toast.makeText(this, "📁 Tab Group '" + name + "' Created", Toast.LENGTH_SHORT).show();
        });
        btnRow.addView(createBtn);

        root.addView(btnRow);

        dialog.setContentView(root);
        if (dialog.getWindow() != null) {
            View bs = dialog.getWindow().findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bs != null) bs.setBackgroundResource(android.R.color.transparent);
        }
        dialog.show();
    }

    public void showMoveTabToCaskDialog(TabItem tab) {
        if (tab == null) return;
        CaskManager cm = new CaskManager(this);
        List<CaskManager.CaskItem> casks = cm.getAllCasks();
        if (casks == null || casks.isEmpty()) return;

        com.google.android.material.bottomsheet.BottomSheetDialog caskDialog =
                new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);

        GradientDrawable rootBg = new GradientDrawable();
        rootBg.setCornerRadii(new float[]{dpToPx(20), dpToPx(20), dpToPx(20), dpToPx(20), 0, 0, 0, 0});
        rootBg.setColor(isDarkTheme ? 0xFF0A0E17 : 0xFFFFFFFF);
        rootBg.setStroke(dpToPx(1), isDarkTheme ? 0xFF1E2433 : 0xFFE2E8F0);
        root.setBackground(rootBg);
        root.setPadding(dpToPx(18), dpToPx(12), dpToPx(18), dpToPx(24));

        View bar = new View(this);
        LinearLayout.LayoutParams barLp = new LinearLayout.LayoutParams(dpToPx(36), dpToPx(4));
        barLp.gravity = Gravity.CENTER_HORIZONTAL;
        barLp.bottomMargin = dpToPx(14);
        bar.setLayoutParams(barLp);
        bar.setBackgroundResource(R.drawable.bg_stitch_handle_bar);
        root.addView(bar);

        TextView title = new TextView(this);
        title.setText("MOVE TAB TO CASK");
        title.setTextColor(isDarkTheme ? 0xFF94A3B8 : 0xFF64748B);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
        title.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        title.setPadding(0, 0, 0, dpToPx(8));
        root.addView(title);

        for (CaskManager.CaskItem cask : casks) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10));
            row.setBackgroundResource(R.drawable.bg_menu_item_row);

            TextView iconTv = new TextView(this);
            iconTv.setText(cask.icon != null ? cask.icon : "🌊");
            iconTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
            iconTv.setPadding(0, 0, dpToPx(12), 0);
            row.addView(iconTv);

            TextView nameTv = new TextView(this);
            nameTv.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            nameTv.setText(cask.name);
            boolean isCurCask = cask.id.equals(tab.caskId);
            nameTv.setTextColor(isCurCask ? (isDarkTheme ? 0xFF00E5FF : 0xFF0284C7) : (isDarkTheme ? 0xFFE2E8F0 : 0xFF0F172A));
            nameTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
            nameTv.setTypeface(null, isCurCask ? Typeface.BOLD : Typeface.NORMAL);
            row.addView(nameTv);

            if (isCurCask) {
                TextView check = new TextView(this);
                check.setText("✓");
                check.setTextColor(isDarkTheme ? 0xFF00E5FF : 0xFF0284C7);
                check.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
                check.setTypeface(null, Typeface.BOLD);
                row.addView(check);
            }

            row.setOnClickListener(v -> {
                playUiFeedbackSound("tap");
                changeTabCask(tab.id, cask.id);
                updateOmniboxTabStrip();
                caskDialog.dismiss();
            });

            root.addView(row);
        }

        caskDialog.setContentView(root);
        if (caskDialog.getWindow() != null) {
            View bs = caskDialog.getWindow().findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bs != null) bs.setBackgroundResource(android.R.color.transparent);
        }
        caskDialog.show();
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
                obj.put("faviconB64", tab.faviconB64 != null ? tab.faviconB64 : "");
                obj.put("favicon64", tab.favicon64 != null ? tab.favicon64 : "");
                obj.put("touchIconUrl", tab.touchIconUrl != null ? tab.touchIconUrl : "");
                obj.put("active", tab.id == activeTabId);
                obj.put("isActive", tab.id == activeTabId);
                obj.put("isDesktop", tab.isDesktop);
                obj.put("isIncognito", tab.isIncognito);
                obj.put("isPlayingAudio", tab.isPlayingAudio);
                obj.put("isMuted", tab.isMuted);
                obj.put("isFavorite", tab.isFavorite);
                obj.put("caskId", tab.caskId != null ? tab.caskId : CaskManager.DEFAULT_CASK_ID);
                obj.put("caskName", tab.caskName != null ? tab.caskName : "Caspian Cask");
                obj.put("caskIcon", tab.caskIcon != null ? tab.caskIcon : "🌊");
                obj.put("caskColor", tab.caskColor != null ? tab.caskColor : "#1B4264");
                boolean hasSplitBond = (tab.splitPartnerId != -1 && getTabById(tab.splitPartnerId) != null);
                boolean isSplitActive = (splitModeState > 0 && (tab.id == activeTabId || tab.id == secondarySplitTabId));
                obj.put("isSplit", isSplitActive || hasSplitBond);
                obj.put("isSplitActive", isSplitActive);
                obj.put("splitPartnerId", tab.splitPartnerId);
                obj.put("splitRole", tab.splitRole != null ? tab.splitRole : "none");
                obj.put("splitOrientation", tab.splitOrientation);
                obj.put("splitName", tab.splitName != null ? tab.splitName : "");
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

                    if (cabVelocityTracker == null) {
                        cabVelocityTracker = VelocityTracker.obtain();
                    } else {
                        cabVelocityTracker.clear();
                    }
                    cabVelocityTracker.addMovement(event);

                    dX = view.getX() - event.getRawX();
                    dY = view.getY() - event.getRawY();
                    startRawX = event.getRawX();
                    startRawY = event.getRawY();
                    isDragging = false;
                    isLongPressed = false;
                    isLongPressedInThisGesture = false;

                    CaspianPhysics.applyPressSquish(view);

                    longPressRunnable = () -> {
                        if (!isDragging) {
                            isLongPressed = true;
                            isLongPressedInThisGesture = true;
                            CaspianPhysics.applyReleasePop(view);
                            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                            playAssetSound("sfx/pop_button_v2.mp3");

                            // Reveal CAB Radial Dial centered at CAB
                            float cx = view.getX() + (view.getWidth() / 2f);
                            float cy = view.getY() + (view.getHeight() / 2f);
                            cabRadialMenu = new CabRadialMenuView(MainActivity.this);
                            cabRadialMenu.showAt(cx, cy, rootContainer);
                        }
                    };
                    longPressHandler.postDelayed(longPressRunnable, 400);
                    return true;

                case MotionEvent.ACTION_MOVE:
                    if (cabVelocityTracker != null) {
                        cabVelocityTracker.addMovement(event);
                    }

                    if (isLongPressed && cabRadialMenu != null) {
                        cabRadialMenu.updateTouch(event.getRawX(), event.getRawY());
                        return true;
                    }

                    float deltaX = Math.abs(event.getRawX() - startRawX);
                    float deltaY = Math.abs(event.getRawY() - startRawY);
                    if (deltaX > 10 || deltaY > 10) {
                        if (!isDragging) {
                            isDragging = true;
                            CaspianPhysics.applyReleasePop(view);
                        }
                        if (longPressRunnable != null) longPressHandler.removeCallbacks(longPressRunnable);

                        view.setX(event.getRawX() + dX);
                        view.setY(event.getRawY() + dY);

                        if (cabVelocityTracker != null) {
                            cabVelocityTracker.computeCurrentVelocity(1000);
                            float vx = cabVelocityTracker.getXVelocity();
                            CaspianPhysics.applyDragTilt(view, vx, 14f);
                        }
                    }
                    return true;

                case MotionEvent.ACTION_UP:
                    if (longPressRunnable != null) longPressHandler.removeCallbacks(longPressRunnable);

                    if (cabVelocityTracker != null) {
                        cabVelocityTracker.addMovement(event);
                        cabVelocityTracker.computeCurrentVelocity(1000);
                    }

                    if (isLongPressed && cabRadialMenu != null) {
                        cabRadialMenu.finishGesture(new CabRadialMenuView.OnRadialActionSelectedListener() {
                            @Override
                            public void onActionSelected(int action) {
                                if (action == CabRadialMenuView.ACTION_WHIRLPOOL) {
                                    startCaspianWhirlpool();
                                } else if (action == CabRadialMenuView.ACTION_DRIFT) {
                                    isUniversalVoiceActive = true;
                                    startSpeechToText();
                                }
                            }

                            @Override
                            public void onCancelled() {
                                // User cancelled in center deadzone
                            }
                        });
                        cabRadialMenu = null;
                        isLongPressed = false;
                        isLongPressedInThisGesture = false;
                        if (cabVelocityTracker != null) {
                            cabVelocityTracker.recycle();
                            cabVelocityTracker = null;
                        }
                        return true;
                    }

                    if (isLongPressedInThisGesture) {
                        isLongPressedInThisGesture = false;
                        if (cabVelocityTracker != null) {
                            cabVelocityTracker.recycle();
                            cabVelocityTracker = null;
                        }
                        return true;
                    }

                    if (!isDragging && !isLongPressed) {
                        CaspianPhysics.applyReleasePop(view);
                        CaspianPhysics.resetDragTilt(view);
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);

                        if (currentWhirlpoolOverlay != null) {
                            currentWhirlpoolOverlay.dismiss();
                            currentWhirlpoolOverlay = null;
                            if (cabVelocityTracker != null) {
                                cabVelocityTracker.recycle();
                                cabVelocityTracker = null;
                            }
                            return true;
                        }
                        actionButtonClickCount++;
                        SharedPreferences appPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                        appPrefs.edit().putInt("action_btn_click_count", actionButtonClickCount).apply();

                        toggleControlSheet();
                    } else if (isDragging) {
                        DisplayMetrics dm = getResources().getDisplayMetrics();
                        float minX = dpToPx(8);
                        float maxX = dm.widthPixels - view.getWidth() - dpToPx(8);
                        float minY = dpToPx(24);
                        float maxY = dm.heightPixels - view.getHeight() - dpToPx(24);
                        float velX = cabVelocityTracker != null ? cabVelocityTracker.getXVelocity() : 0f;
                        float velY = cabVelocityTracker != null ? cabVelocityTracker.getYVelocity() : 0f;

                        CaspianPhysics.flingToRestWithSpring(view, velX, velY, minX, maxX, minY, maxY);
                    }

                    if (cabVelocityTracker != null) {
                        cabVelocityTracker.recycle();
                        cabVelocityTracker = null;
                    }
                    return true;

                case MotionEvent.ACTION_CANCEL:
                    if (longPressRunnable != null) longPressHandler.removeCallbacks(longPressRunnable);
                    if (cabRadialMenu != null) {
                        cabRadialMenu.dismiss();
                        cabRadialMenu = null;
                    }
                    isLongPressed = false;
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
            controlWebView.setOverScrollMode(View.OVER_SCROLL_NEVER);
            controlWebView.setVerticalScrollBarEnabled(false);
            controlWebView.setHorizontalScrollBarEnabled(false);
            settings.setCacheMode(WebSettings.LOAD_DEFAULT);
            controlWebView.addJavascriptInterface(new CaspianBridge(this), "CaspianBridge");
            controlWebView.loadUrl("file:///android_asset/browser_control.html");

            sheetBackdrop.setOnClickListener(v -> {
                if (isSheetOpen) {
                    hideControlSheet();
                } else if (isDownloadsModalOpen) {
                    hideDownloadsManagerModal();
                }
            });

            // Clean up any old downloaded APK files left in cache from previous updates
            new Thread(() -> GitHubUpdateManager.cleanOldApks(MainActivity.this)).start();

            // Check for updates in background (auto-throttled to 4+ hours)
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                new GitHubUpdateManager(this).checkForUpdates(false, new GitHubUpdateManager.UpdateCheckCallback() {
                    @Override
                    public void onResult(GitHubUpdateManager.UpdateInfo info) {
                        if (info.hasUpdate) {
                            evaluateJavascriptInControlSheet("if(window.onUpdateCheckResult) window.onUpdateCheckResult(" + info.toJson().toString() + ");");
                        }
                    }

                    @Override
                    public void onError(String message) {}
                });
            }, 3000);
        } catch (Exception e) {
            Log.e(TAG, "setupControlSheet error: " + e.getMessage());
        }
    }

    public void evaluateJavascriptInControlSheet(String js) {
        runOnUiThread(() -> {
            if (controlWebView != null) {
                controlWebView.evaluateJavascript(js, null);
            }
        });
    }

    public void updateControlSheetTabs() {
        evaluateJavascriptInControlSheet("if (typeof renderOpenTabs === 'function') renderOpenTabs();");
    }

    private void initDownloadManager() {
        CaspianDownloadManager.getInstance(this).setDownloadListener(new CaspianDownloadManager.DownloadListener() {
            @Override
            public void onDownloadStarted(CaspianDownloadManager.DownloadItem item) {
                evaluateJavascriptInControlSheet("if(window.onDownloadStarted) window.onDownloadStarted(" + item.toJson().toString() + ");");
            }

            @Override
            public void onDownloadProgress(CaspianDownloadManager.DownloadItem item) {
                evaluateJavascriptInControlSheet("if(window.onDownloadProgress) window.onDownloadProgress(" + item.toJson().toString() + ");");
            }

            @Override
            public void onDownloadCompleted(CaspianDownloadManager.DownloadItem item) {
                evaluateJavascriptInControlSheet("if(window.onDownloadCompleted) window.onDownloadCompleted(" + item.toJson().toString() + ");");
            }

            @Override
            public void onDownloadFailed(CaspianDownloadManager.DownloadItem item, String error) {
                evaluateJavascriptInControlSheet("if(window.onDownloadFailed) window.onDownloadFailed(" + item.toJson().toString() + ", " + org.json.JSONObject.quote(error != null ? error : "") + ");");
            }

            @Override
            public void onDownloadCancelled(CaspianDownloadManager.DownloadItem item) {
                evaluateJavascriptInControlSheet("if(window.onDownloadCancelled) window.onDownloadCancelled(" + item.toJson().toString() + ");");
            }
        });
    }

    public void evaluateJavascriptInActiveTab(String js) {
        runOnUiThread(() -> {
            try {
                TabItem activeTab = getTabById(activeTabId);
                if (activeTab != null && activeTab.webView != null) {
                    activeTab.webView.evaluateJavascript(js, null);
                }
            } catch (Exception ignored) {}
        });
    }

    public void reloadActiveTabOrHub() {
        runOnUiThread(() -> {
            try {
                TabItem activeTab = getTabById(activeTabId);
                if (activeTab != null && activeTab.webView != null) {
                    if ("file:///android_asset/launch_hub.html".equals(activeTab.url) || "hub".equalsIgnoreCase(activeTab.service)) {
                        activeTab.webView.reload();
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error reloading active tab on cask switch", e);
            }
        });
    }

    public void openControlSheet() {
        isSheetOpen = true;

        if (isDownloadsModalOpen) {
            evaluateJavascriptInControlSheet("if (typeof window.revealCaspianMenu === 'function') window.revealCaspianMenu();");
            playUiFeedbackSound("ta");
            return;
        }

        sheetOverlayContainer.setVisibility(View.VISIBLE);
        sheetOverlayContainer.setClickable(true);
        sheetOverlayContainer.setFocusable(true);

        if (floatingCaspianCard != null) {
            floatingCaspianCard.bringToFront();
            float topElevation = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 600, getResources().getDisplayMetrics());
            floatingCaspianCard.setElevation(topElevation);
            floatingCaspianCard.setCardElevation(topElevation);
        }

        if (ytFloatingRemoteContainer != null) ytFloatingRemoteContainer.setVisibility(View.GONE);
        if (searchNavContainer != null) searchNavContainer.setVisibility(View.GONE);
        if (chatgptDockContainer != null) chatgptDockContainer.setVisibility(View.GONE);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int openDuration = 180;
        try {
            String openDurStr = prefs.getString("sheetOpenDuration", "180");
            openDuration = Integer.parseInt(openDurStr);
        } catch (Exception ignored) {}
        String animStyle = prefs.getString("sheetAnimationStyle", "genie");

        // 1. Smooth In-Place Backdrop Fade
        sheetBackdrop.animate().cancel();
        sheetBackdrop.animate()
                .alpha(1f)
                .setDuration(openDuration)
                .start();

        if (browserContentLayout != null) {
            browserContentLayout.animate().cancel();
            browserContentLayout.animate()
                    .scaleX(0.94f)
                    .scaleY(0.94f)
                    .translationY(dpToPx(14))
                    .setDuration(openDuration)
                    .setInterpolator(new DecelerateInterpolator(1.8f))
                    .start();
        }

        Runnable onOpenComplete = () -> {
            String density = getInterfaceDensity();
            controlWebView.evaluateJavascript("if (typeof renderOpenTabs === 'function') renderOpenTabs(); if (typeof syncAppVersion === 'function') syncAppVersion(); if (typeof restoreSavedSettings === 'function') restoreSavedSettings(); if (typeof updateDevHudCounters === 'function') updateDevHudCounters(); if (typeof applyInterfaceDensity === 'function') applyInterfaceDensity('" + density + "');", null);
        };

        // 2. Animate Control WebView independently
        controlWebView.animate().cancel();
        if ("none".equalsIgnoreCase(animStyle) || openDuration <= 0) {
            sheetBackdrop.setAlpha(1f);
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

            // Only initialize to tiny scale if starting from fully closed state
            if (controlWebView.getScaleX() <= 0.06f || controlWebView.getAlpha() <= 0.05f) {
                controlWebView.setScaleX(0.05f);
                controlWebView.setScaleY(0.05f);
                controlWebView.setAlpha(0f);
            }
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

            if (controlWebView.getTranslationY() <= 0f || controlWebView.getTranslationY() >= height) {
                controlWebView.setTranslationY(height);
            }
            controlWebView.setScaleX(1f);
            controlWebView.setScaleY(1f);
            controlWebView.setAlpha(1f);

            controlWebView.animate()
                    .translationY(0)
                    .setDuration(openDuration)
                    .setInterpolator(new PathInterpolator(0.2f, 0f, 0f, 1f))
                    .withEndAction(onOpenComplete)
                    .start();
        }

        playUiFeedbackSound("ta");
    }

    public void showSoftKeyboardForCurrentTab() {
        TabItem current = getTabById(activeTabId);
        if (current != null && current.webView != null) {
            current.webView.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(current.webView, InputMethodManager.SHOW_IMPLICIT);
            }
        }
    }

    public void hideControlSheet() {
        hideControlSheet(true);
    }

    public void hideControlSheet(boolean playSound) {
        isSheetOpen = false;

        if (isDownloadsModalOpen) {
            evaluateJavascriptInControlSheet("if (typeof window.unrevealCaspianMenu === 'function') window.unrevealCaspianMenu();");
            if (playSound) playUiFeedbackSound("close");
            return;
        }

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int closeDuration = 160;
        try {
            String closeDurStr = prefs.getString("sheetCloseDuration", "160");
            closeDuration = Integer.parseInt(closeDurStr);
        } catch (Exception ignored) {}
        String animStyle = prefs.getString("sheetAnimationStyle", "genie");

        // 1. Smooth In-Place Backdrop Fade Out
        sheetBackdrop.animate().cancel();
        sheetBackdrop.animate()
                .alpha(0f)
                .setDuration(closeDuration)
                .start();

        if (browserContentLayout != null) {
            browserContentLayout.animate().cancel();
            browserContentLayout.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .translationY(0f)
                    .setDuration(closeDuration)
                    .setInterpolator(new OvershootInterpolator(1.08f))
                    .start();
        }

        // 2. Animate Control WebView independently
        controlWebView.animate().cancel();
        if ("none".equalsIgnoreCase(animStyle) || closeDuration <= 0) {
            sheetBackdrop.setAlpha(0f);
            sheetOverlayContainer.setVisibility(View.INVISIBLE);
            sheetOverlayContainer.setClickable(false);
            sheetOverlayContainer.setFocusable(false);
            restoreFloatingWidgetsOnClose();
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
                        if (!isSheetOpen) {
                            sheetOverlayContainer.setVisibility(View.INVISIBLE);
                            sheetOverlayContainer.setClickable(false);
                            sheetOverlayContainer.setFocusable(false);
                            restoreFloatingWidgetsOnClose();
                        }
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
                        if (!isSheetOpen) {
                            sheetOverlayContainer.setVisibility(View.INVISIBLE);
                            sheetOverlayContainer.setClickable(false);
                            sheetOverlayContainer.setFocusable(false);
                            restoreFloatingWidgetsOnClose();
                        }
                    })
                    .start();
        }

        if (playSound) {
            playUiFeedbackSound("ta");
        }
    }

    private void restoreFloatingWidgetsOnClose() {
        TabItem curTab = getActiveOrDominantTab();
        String curUrl = (curTab != null && curTab.url != null) ? curTab.url.toLowerCase() : "";
        boolean isYtMusic = curUrl.contains("music.youtube.com") || (curTab != null && "youtubemusic".equalsIgnoreCase(curTab.service));
        boolean isYt = curTab != null && (curUrl.contains("youtube.com") || "youtube".equalsIgnoreCase(curTab.service)) && !isYtMusic;
        if (ytFloatingRemoteContainer != null) {
            ytFloatingRemoteContainer.setAlpha(1.0f);
            if (isYt && !isYtRemoteExplicitlyHidden) {
                ytFloatingRemoteContainer.setVisibility(View.VISIBLE);
            }
        }
        if (searchNavContainer != null) {
            searchNavContainer.setAlpha(1.0f);
            if (!isSearchNavExplicitlyHidden) searchNavContainer.setVisibility(View.VISIBLE);
        }
        if (chatgptDockContainer != null) {
            chatgptDockContainer.setAlpha(1.0f);
            boolean isGpt = curTab != null && (curUrl.contains("chatgpt.com") || "chatgpt".equalsIgnoreCase(curTab.service));
            if (isGpt && !isChatgptDockExplicitlyHidden) chatgptDockContainer.setVisibility(View.VISIBLE);
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
        if (omniboxEditText != null && omniboxEditText.hasFocus()) {
            omniboxEditText.clearFocus();
            hideKeyboard();
            return;
        }
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
            exitFullscreenCustomView();
            return;
        }
        if (isSheetOpen) {
            hideControlSheet();
            return;
        }
        if (isDownloadsModalOpen) {
            hideDownloadsManagerModal();
            return;
        }
        TabItem currentTab = getTabById(activeTabId);
        if (currentTab != null && currentTab.webView.canGoBack()) {
            currentTab.webView.goBack();
            return;
        }
        super.onBackPressed();
    }

    private File createImageFile() {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            if (storageDir == null) {
                storageDir = getCacheDir();
            }
            return File.createTempFile(imageFileName, ".jpg", storageDir);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == WEBVIEW_PERMISSION_REQUEST_CODE) {
            if (pendingWebPermissionRequest != null) {
                boolean allGranted = true;
                for (int res : grantResults) {
                    if (res != PackageManager.PERMISSION_GRANTED) {
                        allGranted = false;
                        break;
                    }
                }
                if (allGranted) {
                    pendingWebPermissionRequest.grant(pendingWebPermissionRequest.getResources());
                } else {
                    pendingWebPermissionRequest.deny();
                }
                pendingWebPermissionRequest = null;
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveOpenTabsState();
    }

    private void setupPiPActionsReceiver() {
        pipActionReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent == null || intent.getAction() == null) return;
                String action = intent.getAction();
                if (ACTION_PIP_PLAY_PAUSE.equals(action) || ACTION_MEDIA_PLAY_PAUSE.equals(action)) {
                    togglePlayYouTube();
                    updatePiPActions();
                } else if (ACTION_MEDIA_PREVIOUS.equals(action)) {
                    previousYouTubeTrack();
                } else if (ACTION_MEDIA_NEXT.equals(action)) {
                    nextYouTubeTrack();
                } else if (ACTION_MEDIA_REPEAT.equals(action)) {
                    toggleYouTubeRepeat();
                } else if (ACTION_MEDIA_SHUFFLE.equals(action)) {
                    toggleYouTubeShuffle();
                } else if (ACTION_PIP_REWIND.equals(action) || ACTION_MEDIA_REWIND.equals(action)) {
                    seekYouTube(-10);
                } else if (ACTION_PIP_FORWARD.equals(action) || ACTION_MEDIA_FORWARD.equals(action)) {
                    seekYouTube(10);
                } else if (ACTION_MEDIA_DISMISS.equals(action)) {
                    hasYouTubePlaybackStarted = false;
                    dismissMediaNotification();
                } else if (ACTION_LOG_PAUSE_RESUME.equals(action)) {
                    isDebugRecordingPaused = !isDebugRecordingPaused;
                    Toast.makeText(MainActivity.this, isDebugRecordingPaused ? "Logger Paused" : "Logger Resumed", Toast.LENGTH_SHORT).show();
                    updateLoggerNotification();
                } else if (ACTION_LOG_STOP_SAVE.equals(action)) {
                    stopAndSaveDebugLog();
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_PIP_PLAY_PAUSE);
        filter.addAction(ACTION_PIP_REWIND);
        filter.addAction(ACTION_PIP_FORWARD);
        filter.addAction(ACTION_MEDIA_PLAY_PAUSE);
        filter.addAction(ACTION_MEDIA_PREVIOUS);
        filter.addAction(ACTION_MEDIA_NEXT);
        filter.addAction(ACTION_MEDIA_REPEAT);
        filter.addAction(ACTION_MEDIA_SHUFFLE);
        filter.addAction(ACTION_MEDIA_REWIND);
        filter.addAction(ACTION_MEDIA_FORWARD);
        filter.addAction(ACTION_MEDIA_DISMISS);
        filter.addAction(ACTION_LOG_PAUSE_RESUME);
        filter.addAction(ACTION_LOG_STOP_SAVE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(pipActionReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(pipActionReceiver, filter);
        }
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) {
                NotificationChannel mediaChan = new NotificationChannel(
                        CHANNEL_MEDIA_ID,
                        "Media Playback (YouTube)",
                        NotificationManager.IMPORTANCE_LOW
                );
                mediaChan.setDescription("Controls for active media playback and lock screen Now Bar");
                mediaChan.setShowBadge(false);
                nm.createNotificationChannel(mediaChan);

                NotificationChannel logChan = new NotificationChannel(
                        CHANNEL_LOGGER_ID,
                        "Caspian Diagnostic Logger",
                        NotificationManager.IMPORTANCE_LOW
                );
                logChan.setDescription("Status and controls for active console/diagnostic recording");
                logChan.setShowBadge(true);
                nm.createNotificationChannel(logChan);
            }
        }
    }

    private void setupMediaSession() {
        try {
            createNotificationChannels();
            mediaSession = new MediaSessionCompat(this, "CaspianMediaSession");
            mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
            mediaSession.setCallback(new MediaSessionCompat.Callback() {
                @Override
                public void onPlay() {
                    togglePlayYouTube();
                }

                @Override
                public void onPause() {
                    togglePlayYouTube();
                }

                @Override
                public void onSkipToPrevious() {
                    previousYouTubeTrack();
                }

                @Override
                public void onSkipToNext() {
                    nextYouTubeTrack();
                }

                @Override
                public void onSetRepeatMode(int repeatMode) {
                    toggleYouTubeRepeat();
                }

                @Override
                public void onSetShuffleMode(int shuffleMode) {
                    toggleYouTubeShuffle();
                }

                @Override
                public void onCustomAction(String action, Bundle extras) {
                    if ("ACTION_TOGGLE_REPEAT".equals(action)) {
                        toggleYouTubeRepeat();
                    } else if ("ACTION_TOGGLE_SHUFFLE".equals(action)) {
                        toggleYouTubeShuffle();
                    }
                }

                @Override
                public void onSeekTo(long pos) {
                    seekYouTubeTo(pos / 1000.0);
                }
            });
            mediaSession.setActive(false);
        } catch (Exception e) {
            Log.e(TAG, "setupMediaSession error", e);
        }
    }

    private Bitmap downloadHighQualityThumbnail(String urlStr) {
        if (urlStr == null || urlStr.trim().isEmpty()) return null;
        String hqUrl = urlStr;
        // Upgrade Google User Content / YouTube Music album art to 800x800 high definition
        if (hqUrl.contains("googleusercontent.com") || hqUrl.contains("ggpht.com")) {
            hqUrl = hqUrl.replaceAll("=w\\d+-h\\d+[^&?]*", "=w800-h800-l90-rj")
                         .replaceAll("=s\\d+[^&?]*", "=s800");
        } else if (hqUrl.contains("i.ytimg.com/vi/")) {
            hqUrl = hqUrl.replaceAll("/(?:default|mqdefault|hqdefault|sddefault)\\.jpg", "/maxresdefault.jpg");
        }

        try {
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) new java.net.URL(hqUrl).openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36");
            conn.setConnectTimeout(4000);
            conn.setReadTimeout(4000);
            conn.connect();
            if (conn.getResponseCode() == 200) {
                return BitmapFactory.decodeStream(conn.getInputStream());
            }
        } catch (Exception ignored) {}

        // Fallback to original URL or hqdefault if upgraded URL failed
        if (!hqUrl.equals(urlStr)) {
            try {
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) new java.net.URL(urlStr).openConnection();
                conn.setConnectTimeout(4000);
                conn.setReadTimeout(4000);
                conn.connect();
                if (conn.getResponseCode() == 200) {
                    return BitmapFactory.decodeStream(conn.getInputStream());
                }
            } catch (Exception ignored) {}
        }
        if (urlStr.contains("maxresdefault.jpg")) {
            try {
                String altUrl = urlStr.replace("maxresdefault.jpg", "hqdefault.jpg");
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) new java.net.URL(altUrl).openConnection();
                conn.setConnectTimeout(4000);
                conn.setReadTimeout(4000);
                conn.connect();
                if (conn.getResponseCode() == 200) {
                    return BitmapFactory.decodeStream(conn.getInputStream());
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    public void updateMediaMetadata(Integer tabId, String title, String thumbUrl) {
        updateMediaMetadata(tabId, title, "", thumbUrl);
    }

    public void updateMediaMetadata(Integer tabId, String title, String artist, String thumbUrl) {
        if (tabId != null && tabId > 0 && tabId != activeTabId) {
            TabItem activeTab = getTabById(activeTabId);
            boolean activeIsYt = activeTab != null && activeTab.url != null && (activeTab.url.toLowerCase().contains("youtube.com") || "youtube".equalsIgnoreCase(activeTab.service) || "youtubemusic".equalsIgnoreCase(activeTab.service));
            if (activeIsYt && activeTab.isPlayingAudio) {
                return;
            }
        }
        updateMediaMetadata(title, artist, thumbUrl);
    }

    public void updateMediaMetadata(String title, String thumbUrl) {
        updateMediaMetadata(title, "", thumbUrl);
    }

    public void updateMediaMetadata(String title, String artist, String thumbUrl) {
        boolean titleChanged = false;
        if (title != null && !title.trim().isEmpty() && !title.trim().equals(this.currentMediaTitle)) {
            this.currentMediaTitle = title.trim();
            titleChanged = true;
        }
        if (artist != null && !artist.trim().isEmpty()) {
            this.currentMediaArtist = artist.trim();
        } else if (titleChanged) {
            this.currentMediaArtist = "";
        }
        if (thumbUrl != null && !thumbUrl.trim().isEmpty() && !thumbUrl.equals(currentMediaThumbUrl)) {
            this.currentMediaThumbUrl = thumbUrl.trim();
            new Thread(() -> {
                Bitmap bmp = downloadHighQualityThumbnail(currentMediaThumbUrl);
                if (bmp != null) {
                    runOnUiThread(() -> {
                        currentMediaThumbBitmap = bmp;
                        TabItem yt = getYouTubeTab();
                        boolean isPlaying = yt != null && yt.isPlayingAudio;
                        updateMediaPlaybackNotification(isPlaying);
                    });
                }
            }).start();
        }
        TabItem yt = getYouTubeTab();
        boolean isPlaying = yt != null && yt.isPlayingAudio;
        updateMediaPlaybackNotification(isPlaying);
    }

    public boolean hasAnyYouTubeTab() {
        if (tabsList == null) return false;
        for (TabItem t : tabsList) {
            if (t != null && t.url != null && (t.url.toLowerCase().contains("youtube.com") || "youtube".equalsIgnoreCase(t.service) || "youtubemusic".equalsIgnoreCase(t.service))) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAnyPlayingYouTubeTab() {
        if (tabsList == null) return false;
        for (TabItem t : tabsList) {
            if (t != null && t.isPlayingAudio && t.url != null && (t.url.toLowerCase().contains("youtube.com") || "youtube".equalsIgnoreCase(t.service) || "youtubemusic".equalsIgnoreCase(t.service))) {
                return true;
            }
        }
        return false;
    }

    public void handleYouTubeVideoEnded(Integer tabId) {
        if (tabId != null && tabId > 0) {
            TabItem tab = getTabById(tabId);
            if (tab != null) {
                tab.isPlayingAudio = false;
            }
        } else {
            TabItem cur = getActiveOrDominantTab();
            if (cur != null) {
                cur.isPlayingAudio = false;
            }
        }
        if (ytRemotePlayPause != null) {
            ytRemotePlayPause.setImageResource(R.drawable.ic_pod_play);
        }
        if (ytTimelinePlayPause != null) {
            ytTimelinePlayPause.setImageResource(R.drawable.ic_pod_play);
        }
        if (hasAnyYouTubeTab()) {
            updateMediaPlaybackNotification(false);
        } else {
            hasYouTubePlaybackStarted = false;
            dismissMediaNotification();
        }
    }

    public void updateMediaPlaybackNotification(boolean isPlaying) {
        // If no YouTube tab exists at all in the browser, dismiss and exit immediately
        if (!hasAnyYouTubeTab()) {
            hasYouTubePlaybackStarted = false;
            dismissMediaNotification();
            return;
        }

        if (isPlaying) {
            hasYouTubePlaybackStarted = true;
        }

        // If playback has never started yet, don't show notification prematurely
        if (!hasYouTubePlaybackStarted) {
            return;
        }

        try {
            createNotificationChannels();

            TabItem ytTab = getYouTubeTab();
            boolean isYtMusic = ytTab != null && ((ytTab.url != null && ytTab.url.toLowerCase().contains("music.youtube.com")) || "youtubemusic".equalsIgnoreCase(ytTab.service));
            String serviceLabel = isYtMusic ? "YouTube Music" : "YouTube";

            String songTitle = (currentMediaTitle != null && !currentMediaTitle.trim().isEmpty() && !currentMediaTitle.equalsIgnoreCase("YouTube Music") && !currentMediaTitle.equalsIgnoreCase("YouTube"))
                    ? currentMediaTitle.trim()
                    : serviceLabel;
            String songArtist = (currentMediaArtist != null && !currentMediaArtist.trim().isEmpty())
                    ? currentMediaArtist.trim()
                    : serviceLabel;
            String notifSubtitle = (currentMediaArtist != null && !currentMediaArtist.trim().isEmpty())
                    ? (currentMediaArtist.trim() + " • " + serviceLabel)
                    : (serviceLabel + " • Caspian Flow");

            if (mediaSession != null) {
                if (!mediaSession.isActive()) {
                    mediaSession.setActive(true);
                }
                long posMs = (long)(currentVideoTime * 1000);
                float speed = isPlaying ? ytCurrentSpeed : 0.0f;
                mediaSession.setPlaybackState(buildPlaybackState(isPlaying, posMs, speed));

                MediaMetadataCompat.Builder metaBuilder = new MediaMetadataCompat.Builder()
                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, songTitle)
                        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, songArtist)
                        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, isYtMusic ? "YouTube Music" : "Caspian Flow")
                        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST, songArtist)
                        .putString(MediaMetadataCompat.METADATA_KEY_AUTHOR, songArtist)
                        .putString(MediaMetadataCompat.METADATA_KEY_COMPOSER, songArtist)
                        .putString(MediaMetadataCompat.METADATA_KEY_WRITER, songArtist)
                        .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, songTitle)
                        .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, songArtist)
                        .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, isYtMusic ? "YouTube Music" : "Caspian Flow")
                        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, (long)(currentVideoDuration * 1000));
                if (currentMediaThumbBitmap != null) {
                    metaBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, currentMediaThumbBitmap);
                    metaBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, currentMediaThumbBitmap);
                    metaBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, currentMediaThumbBitmap);
                }
                mediaSession.setMetadata(metaBuilder.build());
                mediaSession.setRepeatMode(currentMediaRepeatMode);
                mediaSession.setShuffleMode(currentMediaShuffleMode);
            }

            Intent appIntent = new Intent(this, MainActivity.class);
            PendingIntent pAppIntent = PendingIntent.getActivity(
                    this, 0, appIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            Intent prevIntent = new Intent(ACTION_MEDIA_PREVIOUS).setPackage(getPackageName());
            PendingIntent pPrev = PendingIntent.getBroadcast(this, 201, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            Intent ppIntent = new Intent(ACTION_MEDIA_PLAY_PAUSE).setPackage(getPackageName());
            PendingIntent pPlayPause = PendingIntent.getBroadcast(this, 202, ppIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            Intent nextIntent = new Intent(ACTION_MEDIA_NEXT).setPackage(getPackageName());
            PendingIntent pNext = PendingIntent.getBroadcast(this, 203, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            Intent shufIntent = new Intent(ACTION_MEDIA_SHUFFLE).setPackage(getPackageName());
            PendingIntent pShuffle = PendingIntent.getBroadcast(this, 205, shufIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            Intent repIntent = new Intent(ACTION_MEDIA_REPEAT).setPackage(getPackageName());
            PendingIntent pRepeat = PendingIntent.getBroadcast(this, 206, repIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            Intent dismissIntent = new Intent(ACTION_MEDIA_DISMISS).setPackage(getPackageName());
            PendingIntent pDismiss = PendingIntent.getBroadcast(this, 204, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            int repeatResId;
            String repeatLabel;
            if (currentMediaRepeatMode == PlaybackStateCompat.REPEAT_MODE_ONE) {
                repeatResId = R.drawable.ic_pod_repeat_one;
                repeatLabel = "Repeat One";
            } else if (currentMediaRepeatMode == PlaybackStateCompat.REPEAT_MODE_ALL) {
                repeatResId = R.drawable.ic_pod_repeat;
                repeatLabel = "Repeat All";
            } else {
                repeatResId = R.drawable.ic_pod_repeat_off;
                repeatLabel = "Repeat Off";
            }

            int shuffleResId = (currentMediaShuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL)
                    ? R.drawable.ic_pod_shuffle
                    : R.drawable.ic_pod_shuffle_off;
            String shuffleLabel = (currentMediaShuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL)
                    ? "Shuffle On"
                    : "Shuffle Off";

            NotificationCompat.Builder notif = new NotificationCompat.Builder(this, CHANNEL_MEDIA_ID)
                    .setSmallIcon(R.drawable.ic_caspian_notification)
                    .setContentTitle(songTitle)
                    .setContentText(notifSubtitle)
                    .setSubText(serviceLabel)
                    .setContentIntent(pAppIntent)
                    .setDeleteIntent(pDismiss)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setOngoing(isPlaying)
                    .setShowWhen(false)
                    .addAction(R.drawable.ic_pod_prev, "Previous", pPrev)
                    .addAction(isPlaying ? R.drawable.ic_pod_pause : R.drawable.ic_pod_play, isPlaying ? "Pause" : "Play", pPlayPause)
                    .addAction(R.drawable.ic_pod_next, "Next", pNext)
                    .addAction(shuffleResId, shuffleLabel, pShuffle)
                    .addAction(repeatResId, repeatLabel, pRepeat)
                    .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                            .setMediaSession(mediaSession != null ? mediaSession.getSessionToken() : null)
                            .setShowActionsInCompactView(0, 1, 2));

            if (currentMediaThumbBitmap != null) {
                notif.setLargeIcon(currentMediaThumbBitmap);
            }

            android.app.Notification builtNotif = notif.build();

            if (isPlaying) {
                CaspianMediaService.startMediaForeground(this, builtNotif);
            } else {
                CaspianMediaService.pauseMediaForeground(this);
            }

            try {
                NotificationManagerCompat.from(this).notify(NOTIFICATION_ID_MEDIA, builtNotif);
            } catch (Exception ignored) {}
        } catch (Exception e) {
            Log.e(TAG, "updateMediaPlaybackNotification error", e);
        }
    }

    public void dismissMediaNotification() {
        try {
            CaspianMediaService.stopMediaForeground(this);
            NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID_MEDIA);
            if (mediaSession != null) {
                PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
                        .setState(PlaybackStateCompat.STATE_NONE, 0, 0.0f);
                mediaSession.setPlaybackState(stateBuilder.build());
                mediaSession.setMetadata(null);
                mediaSession.setActive(false);
            }
        } catch (Exception ignored) {}
    }

    public void updateLoggerNotification() {
        if (!isDebugRecording) {
            try {
                NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID_LOGGER);
            } catch (Exception ignored) {}
            return;
        }

        try {
            createNotificationChannels();

            Intent appIntent = new Intent(this, MainActivity.class);
            PendingIntent pApp = PendingIntent.getActivity(this, 0, appIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            Intent pauseIntent = new Intent(ACTION_LOG_PAUSE_RESUME).setPackage(getPackageName());
            PendingIntent pPause = PendingIntent.getBroadcast(this, 301, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            Intent stopIntent = new Intent(ACTION_LOG_STOP_SAVE).setPackage(getPackageName());
            PendingIntent pStop = PendingIntent.getBroadcast(this, 302, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            NotificationCompat.Builder notif = new NotificationCompat.Builder(this, CHANNEL_LOGGER_ID)
                    .setSmallIcon(R.drawable.ic_pod_lock)
                    .setContentTitle(isDebugRecordingPaused ? "🟡 Caspian System Logger: Paused" : "🔴 Caspian System Logger: Recording Active")
                    .setContentText("Capturing console logs, network events & diagnostics to file")
                    .setContentIntent(pApp)
                    .setOngoing(true)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .addAction(R.drawable.ic_pod_play, isDebugRecordingPaused ? "Resume" : "Pause", pPause)
                    .addAction(R.drawable.ic_pod_close, "Stop & Save", pStop);

            NotificationManagerCompat.from(this).notify(NOTIFICATION_ID_LOGGER, notif.build());
        } catch (Exception e) {
            Log.e(TAG, "updateLoggerNotification error", e);
        }
    }

    private List<RemoteAction> buildPiPRemoteActions(boolean isPlaying) {
        List<RemoteAction> actions = new ArrayList<>();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return actions;

        try {
            Intent rewindIntent = new Intent(ACTION_PIP_REWIND).setPackage(getPackageName());
            PendingIntent rewindPendingIntent = PendingIntent.getBroadcast(
                    this, 101, rewindIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            Icon rewindIcon = Icon.createWithResource(this, R.drawable.ic_pod_rewind);
            actions.add(new RemoteAction(rewindIcon, "Rewind 10s", "Rewind 10s", rewindPendingIntent));

            Intent playPauseIntent = new Intent(ACTION_PIP_PLAY_PAUSE).setPackage(getPackageName());
            PendingIntent playPausePendingIntent = PendingIntent.getBroadcast(
                    this, 102, playPauseIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            Icon playPauseIcon = Icon.createWithResource(this, isPlaying ? R.drawable.ic_pod_pause : R.drawable.ic_pod_play);
            actions.add(new RemoteAction(playPauseIcon, isPlaying ? "Pause" : "Play", isPlaying ? "Pause" : "Play", playPausePendingIntent));

            Intent fwdIntent = new Intent(ACTION_PIP_FORWARD).setPackage(getPackageName());
            PendingIntent fwdPendingIntent = PendingIntent.getBroadcast(
                    this, 103, fwdIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            Icon fwdIcon = Icon.createWithResource(this, R.drawable.ic_pod_fastfwd);
            actions.add(new RemoteAction(fwdIcon, "Forward 10s", "Forward 10s", fwdPendingIntent));
        } catch (Exception ignored) {}

        return actions;
    }

    public void updatePiPActions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                if (isInPictureInPictureMode()) {
                    TabItem tab = getTabById(activeTabId);
                    boolean isPlaying = tab != null && tab.isPlayingAudio;
                    PictureInPictureParams.Builder builder = new PictureInPictureParams.Builder();
                    builder.setAspectRatio(new Rational(16, 9));
                    builder.setActions(buildPiPRemoteActions(isPlaying));
                    setPictureInPictureParams(builder.build());
                }
            } catch (Exception ignored) {}
        }
    }

    public void enterYouTubePiP() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            Toast.makeText(this, "Picture-in-Picture requires Android 8.0+", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)) {
            Toast.makeText(this, "PiP mode is not supported on this device", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            TabItem tab = getTabById(activeTabId);
            if (tab != null && tab.webView != null) {
                tab.webView.evaluateJavascript(
                        "(function(){ " +
                        "  var v = (window.__CaspianYouTube ? window.__CaspianYouTube.getVideo() : null) || document.querySelector('video'); " +
                        "  if (v && v.videoWidth > 0 && v.videoHeight > 0) return v.videoWidth + 'x' + v.videoHeight; " +
                        "  return '16x9'; " +
                        "})()",
                        dim -> runOnUiThread(() -> executeEnterPiPWithAspectRatio(dim))
                );
            } else {
                executeEnterPiPWithAspectRatio("16x9");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to enter PiP mode", e);
            Toast.makeText(this, "Failed to enter PiP", Toast.LENGTH_SHORT).show();
        }
    }

    private void executeEnterPiPWithAspectRatio(String rawDim) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        try {
            TabItem tab = getTabById(activeTabId);
            boolean isPlaying = tab != null && tab.isPlayingAudio;
            PictureInPictureParams.Builder pipBuilder = new PictureInPictureParams.Builder();

            Rational aspect = new Rational(16, 9);
            if (rawDim != null) {
                String clean = rawDim.replace("\"", "").trim();
                String[] parts = clean.split("x");
                if (parts.length == 2) {
                    try {
                        float w = Float.parseFloat(parts[0]);
                        float h = Float.parseFloat(parts[1]);
                        if (w > 0 && h > 0) {
                            float ratio = w / h;
                            // Clamp within Android OS allowed range [0.418410, 2.390000]
                            ratio = Math.max(0.418410f, Math.min(2.390000f, ratio));
                            aspect = new Rational((int)(ratio * 1000), 1000);
                        }
                    } catch (Exception ignored) {}
                }
            }
            pipBuilder.setAspectRatio(aspect);
            pipBuilder.setActions(buildPiPRemoteActions(isPlaying));

            Rect sourceRect = new Rect();
            if (customView != null) {
                customView.getGlobalVisibleRect(sourceRect);
                pipBuilder.setSourceRectHint(sourceRect);
            } else if (tab != null && tab.webView != null) {
                tab.webView.evaluateJavascript(
                        "(function(){ if (window.__CaspianYouTube) window.__CaspianYouTube.enterPipMode(); })()", null
                );
                tab.webView.getGlobalVisibleRect(sourceRect);
                pipBuilder.setSourceRectHint(sourceRect);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                pipBuilder.setAutoEnterEnabled(false);
                pipBuilder.setSeamlessResizeEnabled(true);
            }
            enterPictureInPictureMode(pipBuilder.build());
        } catch (Exception e) {
            Log.e(TAG, "Failed executeEnterPiPWithAspectRatio", e);
            Toast.makeText(this, "Failed to enter PiP", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        TabItem tab = getTabById(activeTabId);
        if (isInPictureInPictureMode) {
            if (omniboxHeader != null) omniboxHeader.setVisibility(View.GONE);
            if (floatingCaspianCard != null) floatingCaspianCard.setVisibility(View.GONE);
            if (ytFloatingRemoteContainer != null) ytFloatingRemoteContainer.setVisibility(View.GONE);
            if (ytFloatingTimelineBar != null) ytFloatingTimelineBar.setVisibility(View.GONE);
            if (searchNavContainer != null) searchNavContainer.setVisibility(View.GONE);
            if (browserProgressBar != null) browserProgressBar.setVisibility(View.GONE);
            if (videoTouchLockOverlay != null) videoTouchLockOverlay.setVisibility(View.GONE);

            if (tab != null && tab.webView != null) {
                tab.webView.evaluateJavascript(
                        "(function(){ if (window.__CaspianYouTube) window.__CaspianYouTube.enterPipMode(); var v = document.querySelector('video'); if (v && v.paused) v.play().catch(()=>{}); })()", null
                );
            }
        } else {
            if (omniboxHeader != null) omniboxHeader.setVisibility(View.VISIBLE);
            if (floatingCaspianCard != null && customView == null) {
                floatingCaspianCard.setVisibility(View.VISIBLE);
                floatingCaspianCard.setAlpha(1.0f);
                floatingCaspianCard.bringToFront();
            }
            if (tab != null && tab.webView != null) {
                tab.webView.evaluateJavascript(
                        "(function(){ if (window.__CaspianYouTube) window.__CaspianYouTube.exitPipMode(); })()", null
                );
            }
            if (tab != null) updateOmniboxState();
            syncTimelineBarWidth();
        }
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        // PiP is only entered explicitly via the YouTube Float Pod button.
        // On home gesture, continuous background audio playback continues without PiP!
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pipActionReceiver != null) {
            try {
                unregisterReceiver(pipActionReceiver);
            } catch (Exception ignored) {}
        }
        dismissMediaNotification();
        if (mediaSession != null) {
            try {
                mediaSession.release();
            } catch (Exception ignored) {}
        }
        try {
            NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID_LOGGER);
        } catch (Exception ignored) {}
        try {
            if (youtubeWakeLock != null && youtubeWakeLock.isHeld()) {
                youtubeWakeLock.release();
            }
        } catch (Exception ignored) {}
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText && v == omniboxEditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                boolean inEdit = outRect.contains((int) ev.getRawX(), (int) ev.getRawY());
                boolean inClear = false;
                if (omniboxClearBtn != null && omniboxClearBtn.getVisibility() == View.VISIBLE) {
                    Rect clearRect = new Rect();
                    omniboxClearBtn.getGlobalVisibleRect(clearRect);
                    inClear = clearRect.contains((int) ev.getRawX(), (int) ev.getRawY());
                }
                boolean inPaste = false;
                if (omniboxPasteBtn != null && omniboxPasteBtn.getVisibility() == View.VISIBLE) {
                    Rect pasteRect = new Rect();
                    omniboxPasteBtn.getGlobalVisibleRect(pasteRect);
                    inPaste = pasteRect.contains((int) ev.getRawX(), (int) ev.getRawY());
                }
                if (!inEdit && !inClear && !inPaste) {
                    v.clearFocus();
                    hideKeyboard();
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }
}
