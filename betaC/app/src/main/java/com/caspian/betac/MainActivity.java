package com.caspian.betac;

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
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.view.PixelCopy;
import android.webkit.RenderProcessGoneDetail;
import java.util.function.Consumer;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
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
import android.os.PowerManager;
import android.os.SystemClock;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.Editable;
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
import android.media.AudioManager;
import android.widget.ProgressBar;
import android.widget.SeekBar;
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
        public boolean isPlayingAudio = false;
        public boolean isFavorite = false;
        public boolean isIncognito = false;
        public String pendingPrompt = null;
        public Bitmap snapshotBitmap = null;
        public String caskId = CaskManager.DEFAULT_CASK_ID;
        public String caskName = "Caspian Cask";
        public String caskIcon = "🌊";
        public String caskColor = "#1B4264";

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
    public static final String ACTION_MEDIA_REWIND = "com.caspian.betac.MEDIA_REWIND";
    public static final String ACTION_MEDIA_FORWARD = "com.caspian.betac.MEDIA_FORWARD";
    public static final String ACTION_LOG_PAUSE_RESUME = "com.caspian.betac.LOG_PAUSE_RESUME";
    public static final String ACTION_LOG_STOP_SAVE = "com.caspian.betac.LOG_STOP_SAVE";

    private static final String CHANNEL_MEDIA_ID = "caspian_media_playback";
    private static final int NOTIFICATION_ID_MEDIA = 7001;
    private static final String CHANNEL_LOGGER_ID = "caspian_system_logger";
    private static final int NOTIFICATION_ID_LOGGER = 9002;

    private MediaSessionCompat mediaSession;
    private String currentMediaTitle = "YouTube";
    private String currentMediaThumbUrl = "";
    private Bitmap currentMediaThumbBitmap = null;
    private boolean isDebugRecordingPaused = false;
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

    private boolean isDebugRecording = false;
    private final StringBuilder debugLogBuffer = new StringBuilder();

    private View customView;
    private WebChromeClient.CustomViewCallback customViewCallback;
    private FrameLayout fullscreenContainer;

    private ValueCallback<Uri[]> uploadMessage;
    private final static int FILECHOOSER_RESULTCODE = 1;
    private final static int REQUEST_CODE_PDF_PICKER = 9182;
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
            float highElevation = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, getResources().getDisplayMetrics());
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
                float highElevation = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, getResources().getDisplayMetrics());
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

    private void captureTabSnapshot(TabItem tab) {
        if (tab == null || tab.webView == null) return;
        tab.webView.postDelayed(() -> {
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
                }
            } catch (Exception ignored) {}
        }, 500);
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
        updateYouTubeLiveState(isPlaying, isMuted, null);
    }

    public void updateYouTubeLiveState(boolean isPlaying, boolean isMuted, Integer tabId) {
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
            updateMediaPlaybackNotification(isPlaying);
        }
        boolean anyPlaying = false;
        for (TabItem t : tabsList) {
            if (t.isPlayingAudio) {
                anyPlaying = true;
                break;
            }
        }
        manageYouTubeWakeLock(anyPlaying || isPlaying);
    }

    private void manageYouTubeWakeLock(boolean acquire) {
        try {
            if (acquire) {
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
                if (youtubeWakeLock != null && youtubeWakeLock.isHeld()) {
                    youtubeWakeLock.release();
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
        if (tabId != null && tabId > 0 && tabId != activeTabId) {
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
        if (mediaSession != null && (now - lastMediaSessionTimeUpdateMs > 1000)) {
            lastMediaSessionTimeUpdateMs = now;
            TabItem cur = getTabById(activeTabId);
            boolean isPlaying = cur != null && cur.isPlayingAudio;
            int state = isPlaying ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED;
            long posMs = (long)(currentTime * 1000);
            PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
                    .setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PAUSE
                            | PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                            | PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SEEK_TO)
                    .setState(state, posMs, isPlaying ? ytCurrentSpeed : 0.0f, android.os.SystemClock.elapsedRealtime());
            mediaSession.setPlaybackState(stateBuilder.build());
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
        TabItem currentTab = getTabById(activeTabId);
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
        boolean isYt = currentTab != null && (
                "youtube".equalsIgnoreCase(currentTab.service) ||
                (currentTab.url != null && currentTab.url.toLowerCase().contains("youtube.com")) ||
                (currentTab.webView != null && currentTab.webView.getUrl() != null && currentTab.webView.getUrl().toLowerCase().contains("youtube.com"))
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
        menuItems.add(new CaspianMenuItem("📄 Open PDF", this::openPdfPicker));
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
                if (request != null && request.getUrl() != null) {
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
                }
                if (adBlockShield != null && adBlockShield.isBlocked(request.getUrl().toString())) {
                    return adBlockShield.getBlockedResponse(request.getUrl().toString());
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
                        view.evaluateJavascript("window.__caspian_tab_id = " + id + ";\n" + ytHelperJs, null);
                    }
                }

                if (adBlockShield != null && adBlockShield.isEnabled()) {
                    view.evaluateJavascript(AdBlockShield.COSMETIC_CSS_INJECTION, null);
                }

                if (tabItem.pendingPrompt != null && !tabItem.pendingPrompt.isEmpty()) {
                    injectAIPrompt(view, tabItem.pendingPrompt, false);
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
                    boolean isYt = curUrl.toLowerCase().contains("youtube.com") || "youtube".equalsIgnoreCase(tabItem.service);
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
        addNewTab(service, prompt, url, isIncognito, null);
    }

    public void addNewTab(String service, String prompt, String url, boolean isIncognito, String targetCaskId) {
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
            updateOmniboxState();
            saveOpenTabsState();
        } else {
            addNewTab(service, null, url, false, activeCask);
        }
    }

    public void switchToTab(int tabId) {
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

        activeTabId = tabId;
        if (splitModeState == 0) {
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
        } else {
            applySplitViewLayout();
        }
        updateOmniboxState();
        hideControlSheet(false);
        playUiFeedbackSound("tm_tabs");
        saveOpenTabsState();
    }

    public void closeTab(int tabId) {
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
            last.url = "file:///android_asset/launch_hub.html";
            last.service = "hub";
            last.title = "Caspian Hub";
            last.webView.loadUrl(last.url);
            updateOmniboxState();
            saveOpenTabsState();
            return;
        }
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
        Toast.makeText(this, "Vault switched to " + cask.name, Toast.LENGTH_SHORT).show();
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
            switchToTab(activeTabId);
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
                tab.webView.stopLoading();
                String url = tab.webView.getUrl();
                if (url == null || url.isEmpty()) url = tab.url;
                if (url != null && !url.isEmpty()) {
                    tab.webView.loadUrl(url);
                } else {
                    tab.webView.reload();
                }
            } catch (Exception e) {
                tab.webView.reload();
            }
        }
    }

    public void reloadActiveTab() {
        TabItem tab = getActiveOrDominantTab();
        if (tab != null && tab.webView != null) {
            try {
                tab.webView.stopLoading();
                String url = tab.webView.getUrl();
                if (url == null || url.isEmpty()) url = tab.url;
                if (url != null && !url.isEmpty()) {
                    tab.webView.loadUrl(url);
                } else {
                    tab.webView.reload();
                }
            } catch (Exception e) {
                tab.webView.reload();
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
                obj.put("isPlayingAudio", tab.isPlayingAudio);
                obj.put("isMuted", tab.isMuted);
                obj.put("isFavorite", tab.isFavorite);
                obj.put("caskId", tab.caskId != null ? tab.caskId : CaskManager.DEFAULT_CASK_ID);
                obj.put("caskName", tab.caskName != null ? tab.caskName : "Caspian Cask");
                obj.put("caskIcon", tab.caskIcon != null ? tab.caskIcon : "🌊");
                obj.put("caskColor", tab.caskColor != null ? tab.caskColor : "#1B4264");
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
                    if (isLongPressed && cabRadialMenu != null) {
                        cabRadialMenu.updateTouch(event.getRawX(), event.getRawY());
                        return true;
                    }

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
                        return true;
                    }

                    if (isLongPressedInThisGesture) {
                        isLongPressedInThisGesture = false;
                        return true;
                    }

                    if (!isDragging && !isLongPressed) {
                        if (currentWhirlpoolOverlay != null) {
                            currentWhirlpoolOverlay.dismiss();
                            currentWhirlpoolOverlay = null;
                            return true;
                        }
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
            controlWebView.addJavascriptInterface(new CaspianBridge(this), "CaspianBridge");
            controlWebView.loadUrl("file:///android_asset/browser_control.html");

            sheetBackdrop.setOnClickListener(v -> hideControlSheet());

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
        sheetOverlayContainer.setVisibility(View.VISIBLE);
        sheetOverlayContainer.setElevation(dpToPx(400));
        sheetOverlayContainer.bringToFront();

        if (floatingCaspianCard != null) {
            floatingCaspianCard.setAlpha(1.0f);
            float highElevation = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 410, getResources().getDisplayMetrics());
            floatingCaspianCard.setElevation(highElevation);
            floatingCaspianCard.setCardElevation(highElevation);
            floatingCaspianCard.bringToFront();
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
            float normalElevation = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());
            floatingCaspianCard.setElevation(normalElevation);
            floatingCaspianCard.setCardElevation(normalElevation);
            floatingCaspianCard.bringToFront();
        }
        TabItem curTab = getActiveOrDominantTab();
        String curUrl = (curTab != null && curTab.url != null) ? curTab.url.toLowerCase() : "";
        boolean isYt = curTab != null && (curUrl.contains("youtube.com") || "youtube".equalsIgnoreCase(curTab.service));
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
            exitFullscreenCustomView();
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
                } else if (ACTION_PIP_REWIND.equals(action) || ACTION_MEDIA_REWIND.equals(action)) {
                    seekYouTube(-10);
                } else if (ACTION_PIP_FORWARD.equals(action) || ACTION_MEDIA_FORWARD.equals(action)) {
                    seekYouTube(10);
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
        filter.addAction(ACTION_MEDIA_REWIND);
        filter.addAction(ACTION_MEDIA_FORWARD);
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
                    seekYouTube(-10);
                }

                @Override
                public void onSkipToNext() {
                    seekYouTube(10);
                }

                @Override
                public void onSeekTo(long pos) {
                    seekYouTubeTo(pos / 1000.0);
                }
            });
            mediaSession.setActive(true);
        } catch (Exception e) {
            Log.e(TAG, "setupMediaSession error", e);
        }
    }

    private Bitmap downloadHighQualityThumbnail(String urlStr) {
        if (urlStr == null || urlStr.trim().isEmpty()) return null;
        try {
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) new java.net.URL(urlStr).openConnection();
            conn.setConnectTimeout(4000);
            conn.setReadTimeout(4000);
            conn.connect();
            if (conn.getResponseCode() == 200) {
                return BitmapFactory.decodeStream(conn.getInputStream());
            }
        } catch (Exception ignored) {}

        if (urlStr.contains("maxresdefault.jpg")) {
            try {
                String hqUrl = urlStr.replace("maxresdefault.jpg", "hqdefault.jpg");
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) new java.net.URL(hqUrl).openConnection();
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
        if (tabId != null && tabId > 0 && tabId != activeTabId) return;
        updateMediaMetadata(title, thumbUrl);
    }

    public void updateMediaMetadata(String title, String thumbUrl) {
        if (title != null && !title.trim().isEmpty()) {
            this.currentMediaTitle = title.trim();
        }
        if (thumbUrl != null && !thumbUrl.trim().isEmpty() && !thumbUrl.equals(currentMediaThumbUrl)) {
            this.currentMediaThumbUrl = thumbUrl.trim();
            new Thread(() -> {
                Bitmap bmp = downloadHighQualityThumbnail(currentMediaThumbUrl);
                if (bmp != null) {
                    runOnUiThread(() -> {
                        currentMediaThumbBitmap = bmp;
                        TabItem cur = getTabById(activeTabId);
                        boolean isPlaying = cur != null && cur.isPlayingAudio;
                        updateMediaPlaybackNotification(isPlaying);
                    });
                }
            }).start();
        }
        TabItem cur = getTabById(activeTabId);
        boolean isPlaying = cur != null && cur.isPlayingAudio;
        updateMediaPlaybackNotification(isPlaying);
    }

    public void updateMediaPlaybackNotification(boolean isPlaying) {
        TabItem cur = getTabById(activeTabId);
        boolean isYt = cur != null && cur.url != null && (cur.url.toLowerCase().contains("youtube.com") || "youtube".equalsIgnoreCase(cur.service));
        if (!isYt) {
            dismissMediaNotification();
            return;
        }

        try {
            if (mediaSession != null) {
                int state = isPlaying ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED;
                long posMs = (long)(currentVideoTime * 1000);
                float speed = isPlaying ? ytCurrentSpeed : 0.0f;
                PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
                        .setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PAUSE
                                | PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                                | PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SEEK_TO)
                        .setState(state, posMs, speed, android.os.SystemClock.elapsedRealtime());
                mediaSession.setPlaybackState(stateBuilder.build());

                MediaMetadataCompat.Builder metaBuilder = new MediaMetadataCompat.Builder()
                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, currentMediaTitle)
                        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "YouTube • Caspian Flow")
                        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, "Caspian Flow")
                        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, (long)(currentVideoDuration * 1000));
                if (currentMediaThumbBitmap != null) {
                    metaBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, currentMediaThumbBitmap);
                }
                mediaSession.setMetadata(metaBuilder.build());
            }

            Intent appIntent = new Intent(this, MainActivity.class);
            PendingIntent pAppIntent = PendingIntent.getActivity(
                    this, 0, appIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            Intent rewIntent = new Intent(ACTION_MEDIA_REWIND).setPackage(getPackageName());
            PendingIntent pRew = PendingIntent.getBroadcast(this, 201, rewIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            Intent ppIntent = new Intent(ACTION_MEDIA_PLAY_PAUSE).setPackage(getPackageName());
            PendingIntent pPlayPause = PendingIntent.getBroadcast(this, 202, ppIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            Intent fwdIntent = new Intent(ACTION_MEDIA_FORWARD).setPackage(getPackageName());
            PendingIntent pFwd = PendingIntent.getBroadcast(this, 203, fwdIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            NotificationCompat.Builder notif = new NotificationCompat.Builder(this, CHANNEL_MEDIA_ID)
                    .setSmallIcon(R.drawable.ic_pod_play)
                    .setContentTitle(currentMediaTitle)
                    .setContentText("YouTube • Caspian Flow")
                    .setContentIntent(pAppIntent)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setOngoing(isPlaying)
                    .setShowWhen(false)
                    .addAction(R.drawable.ic_pod_rewind, "Rewind 10s", pRew)
                    .addAction(isPlaying ? R.drawable.ic_pod_pause : R.drawable.ic_pod_play, isPlaying ? "Pause" : "Play", pPlayPause)
                    .addAction(R.drawable.ic_pod_fastfwd, "Forward 10s", pFwd)
                    .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                            .setMediaSession(mediaSession != null ? mediaSession.getSessionToken() : null)
                            .setShowActionsInCompactView(0, 1, 2));

            if (currentMediaThumbBitmap != null) {
                notif.setLargeIcon(currentMediaThumbBitmap);
            }

            android.app.Notification builtNotif = notif.build();
            try {
                NotificationManagerCompat.from(this).notify(NOTIFICATION_ID_MEDIA, builtNotif);
            } catch (Exception ignored) {}

            if (isPlaying) {
                CaspianMediaService.startMediaForeground(this, builtNotif);
            } else {
                CaspianMediaService.stopMediaForeground(this);
            }
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
                        .setState(PlaybackStateCompat.STATE_STOPPED, 0, 1.0f);
                mediaSession.setPlaybackState(stateBuilder.build());
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
}
