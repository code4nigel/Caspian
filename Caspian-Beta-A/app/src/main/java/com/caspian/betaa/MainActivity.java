package com.caspian.betaa;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.ValueCallback;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.net.Uri;
import android.os.Build;
import android.content.Intent;
import android.provider.MediaStore;
import android.os.Handler;
import android.os.Looper;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;
import android.Manifest;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.widget.Toast;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.AudioFormat;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public static class TabItem {
        public int id;
        public String title;
        public String url;
        public String service;
        public String nickname;
        public WebView webView;
        public boolean isMuted = false;
        public boolean isPlayingAudio = false;
        public boolean isFavorite = false;

        public TabItem(int id, String title, String url, String service, WebView webView) {
            this.id = id;
            this.title = title;
            this.url = url;
            this.service = service;
            this.nickname = "";
            this.webView = webView;
        }
    }

    private final List<TabItem> tabsList = new ArrayList<>();
    private int activeTabId = 1;
    private int nextTabId = 2;

    private FrameLayout webViewContainer;
    private WebView controlWebView;
    private FrameLayout sheetOverlayContainer;
    private View sheetBackdrop;
    private CardView floatingCaspianCard;
    private String pendingPrefill = null;
    private boolean isPrinting = false;
    private TabItem lastClosedTab = null;
    private boolean lastClosedTabWasActive = false;

    // Debug Log Recording Variables
    private boolean isDebugRecording = false;
    private final StringBuilder debugLogBuffer = new StringBuilder();

    // Touch Drag Variables for Native Wave Button
    private float dX, dY;
    private float startRawX, startRawY;
    private boolean isDragging = false;

    private static final String DEFAULT_CHROME_UA = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36";

    // File Chooser & Camera Permissions Variables
    private android.webkit.ValueCallback<Uri[]> uploadMessage;
    private final static int FILECHOOSER_RESULTCODE = 1;
    private final static int PERMISSION_REQUEST_CODE = 1001;
    public static final int MIC_PERMISSION_REQUEST_CODE = 1002;
    private Uri cameraImageUri;
    private android.webkit.PermissionRequest pendingWebPermissionRequest;

    // HTML5 Fullscreen Video Handling (YouTube, Vimeo, Web Video Players)
    private View customView;
    private WebChromeClient.CustomViewCallback customViewCallback;
    private FrameLayout fullscreenContainer;

    // Speech Recognition (Whisper Flow - Caspian Current)
    private SpeechRecognizer speechRecognizer;
    private Intent speechIntent;
    private boolean isLongPressing = false;
    private final Handler longPressHandler = new Handler(Looper.getMainLooper());
    private Runnable longPressRunnable;
    private SpeechWaveformView speechWaveformView;

    // Search Navigation Controls (Google Search Tabs)
    private FrameLayout searchNavContainer;
    private android.widget.ImageButton navBackBtn;
    private android.widget.ImageButton navForwardBtn;

    // Video Splash Screen Overlay
    private FrameLayout splashOverlay;
    private android.view.TextureView splashTextureView;
    private android.media.MediaPlayer splashPlayer;

    // Native SoundPool for simultaneous, non-interrupting UI sound effects (never pauses background music)
    private android.media.SoundPool soundPool;
    private final java.util.Map<String, Integer> soundIdMap = new java.util.concurrent.ConcurrentHashMap<>();

    private void initSoundPool() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                android.media.AudioAttributes attributes = new android.media.AudioAttributes.Builder()
                        .setUsage(android.media.AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setFlags(android.media.AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                        .build();
                soundPool = new android.media.SoundPool.Builder()
                        .setMaxStreams(10)
                        .setAudioAttributes(attributes)
                        .build();
            } else {
                soundPool = new android.media.SoundPool(10, android.media.AudioManager.STREAM_MUSIC, 0);
            }
        } catch (Exception ignored) {}
    }

    // WebView active layout refresh timer
    private final Handler refreshHandler = new Handler(Looper.getMainLooper());
    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            TabItem activeTab = getActiveTab();
            if (activeTab != null && activeTab.webView != null && activeTab.webView.getVisibility() == View.VISIBLE) {
                activeTab.webView.postInvalidate();
            }
            SharedPreferences prefs = getSharedPreferences("CaspianMobilePrefs", MODE_PRIVATE);
            int rate = 100;
            try {
                rate = Integer.parseInt(prefs.getString("active_refresh_rate", "100"));
            } catch (Exception e) {}
            if (rate > 0) {
                refreshHandler.postDelayed(this, rate);
            }
        }
    };

    @SuppressLint({"SetJavaScriptEnabled", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Maximum Window-Level GPU Hardware Acceleration for 60fps/120fps smooth scrolling
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        );

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            try {
                String stackTrace = Log.getStackTraceString(throwable);
                Log.e("CaspianCrash", "UNCAUGHT CRASH: " + stackTrace, throwable);
                SharedPreferences prefs = getSharedPreferences("CaspianCrashPrefs", MODE_PRIVATE);
                prefs.edit().putString("last_crash_log", stackTrace).commit();
            } catch (Exception e) {}
        });

        // Check for previous crash log on startup
        try {
            SharedPreferences crashPrefs = getSharedPreferences("CaspianCrashPrefs", MODE_PRIVATE);
            String lastCrash = crashPrefs.getString("last_crash_log", null);
            if (lastCrash != null) {
                crashPrefs.edit().remove("last_crash_log").apply();
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Caspian Diagnostic Log")
                        .setMessage("An unexpected error occurred previously:\n\n" + lastCrash.substring(0, Math.min(500, lastCrash.length())) + "\n...")
                        .setPositiveButton("Copy Full Log & Continue", (dialog, which) -> {
                            try {
                                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("Caspian Crash Log", lastCrash);
                                if (clipboard != null) clipboard.setPrimaryClip(clip);
                                Toast.makeText(this, "Copied crash log to clipboard!", Toast.LENGTH_LONG).show();
                            } catch (Exception e) {}
                        })
                        .setNegativeButton("Close", null)
                        .show();
            }
        } catch (Exception e) {}

        setContentView(R.layout.activity_main);

        initSoundPool();
        new Thread(() -> {
            try {
                String[] commonSfx = {
                    "sfx/pop_click.mp3",
                    "sfx/tap_button.mp3",
                    "sfx/tap_main.mp3",
                    "sfx/tap_alternate.mp3",
                    "sfx/pop_button.mp3",
                    "sfx/pop_button_v2.mp3",
                    "sfx/pop_unknown_v1.mp3"
                };
                for (String path : commonSfx) {
                    try {
                        android.content.res.AssetFileDescriptor afd = getAssets().openFd(path);
                        int sid = soundPool.load(afd, 1);
                        afd.close();
                        soundIdMap.put(path, sid);
                    } catch (Exception ignored) {}
                }

                // In-Memory Static Script Caching (Zero disk I/O on tab switches & page navigations)
                try { readAssetFile("mobile_pruner.js"); } catch (Exception ignored) {}
                try { readAssetFile("youtube_helper.js"); } catch (Exception ignored) {}
            } catch (Exception ignored) {}
        }).start();

        webViewContainer = findViewById(R.id.webview_container);
        controlWebView = findViewById(R.id.control_webview);
        sheetOverlayContainer = findViewById(R.id.sheet_overlay_container);
        sheetBackdrop = findViewById(R.id.sheet_backdrop);
        if (sheetOverlayContainer != null) {
            sheetOverlayContainer.setVisibility(View.INVISIBLE);
            sheetOverlayContainer.setClickable(false);
            sheetOverlayContainer.setFocusable(false);
        }
        if (sheetBackdrop != null) {
            sheetBackdrop.setAlpha(0f);
        }
        if (controlWebView != null) {
            controlWebView.setAlpha(0f);
        }
        floatingCaspianCard = findViewById(R.id.floating_caspian_card);

        // Persistent Cookie Sync across all tabs and Google / ChatGPT OAuth
        CookieManager.getInstance().setAcceptCookie(true);

        searchNavContainer = findViewById(R.id.search_nav_container);
        navBackBtn = findViewById(R.id.nav_back_btn);
        navForwardBtn = findViewById(R.id.nav_forward_btn);

        // Safe dynamic initialization of SpeechWaveformView
        try {
            FrameLayout speechContainer = findViewById(R.id.speech_waveform_container);
            if (speechContainer != null) {
                speechWaveformView = new SpeechWaveformView(this);
                speechContainer.addView(speechWaveformView, new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                ));
                speechContainer.setVisibility(View.GONE);
            }
        } catch (Throwable t) {
            Log.e("CaspianDebugA", "SpeechWaveformView dynamic init error: " + t.getMessage());
        }

        if (navBackBtn != null) {
            navBackBtn.setOnClickListener(v -> {
                TabItem activeTab = getActiveTab();
                if (activeTab != null && activeTab.webView != null && activeTab.webView.canGoBack()) {
                    activeTab.webView.goBack();
                }
            });
        }

        if (navForwardBtn != null) {
            navForwardBtn.setOnClickListener(v -> {
                TabItem activeTab = getActiveTab();
                if (activeTab != null && activeTab.webView != null && activeTab.webView.canGoForward()) {
                    activeTab.webView.goForward();
                }
            });
        }

        try {
            setupControlWebView();
            setupNativeFloatingButton();
            setupSmartKeyboardAvoidance();
            setupSearchDock();
            setupFloatingYouTubeRemote();

            if (sheetBackdrop != null) {
                sheetBackdrop.setOnClickListener(v -> closeControlSheet());
            }

            // Load Persistent Tabs from Preferences
            loadTabsFromPrefs();

            // Load Saved Floating Theme on startup
            SharedPreferences prefs = getSharedPreferences("CaspianMobilePrefs", MODE_PRIVATE);
            String startColor = prefs.getString("theme_start_color", "#A2A9A9");
            String endColor = prefs.getString("theme_end_color", "#1B4264");
            String iconShape = prefs.getString("theme_icon_shape", "circle");
            applyFloatingTheme(startColor, endColor, iconShape);

            try {
                float actionBtnScale = Float.parseFloat(prefs.getString("action_button_scale", "1.0"));
                float ytPodScale = Float.parseFloat(prefs.getString("yt_pod_scale", "1.0"));
                float googleDockScale = Float.parseFloat(prefs.getString("google_dock_scale", "1.0"));
                applyWidgetScale("action_button", actionBtnScale);
                applyWidgetScale("yt_pod", ytPodScale);
                applyWidgetScale("google_dock", googleDockScale);
            } catch (Exception e) {}

            updateRefreshTimer();
            setupSplashScreen();
        } catch (Throwable t) {
            Log.e("CaspianDebugA", "Error during onCreate startup: " + t.getMessage(), t);
        }
    }

    private void setupSplashScreen() {
        splashOverlay = findViewById(R.id.splash_overlay);
        splashTextureView = findViewById(R.id.splash_textureview);
        if (splashOverlay != null && splashTextureView != null) {
            try {
                final boolean[] dismissed = {false};
                Runnable dismissSplash = new Runnable() {
                    @Override
                    public void run() {
                        if (dismissed[0]) return;
                        dismissed[0] = true;
                        if (splashPlayer != null) {
                            try {
                                splashPlayer.stop();
                                splashPlayer.release();
                            } catch (Exception e) {}
                            splashPlayer = null;
                        }
                        if (splashOverlay != null && splashOverlay.getVisibility() == View.VISIBLE) {
                            splashOverlay.animate()
                                    .alpha(0f)
                                    .setDuration(250)
                                    .withEndAction(() -> splashOverlay.setVisibility(View.GONE))
                                    .start();
                        }
                    }
                };

                splashTextureView.setSurfaceTextureListener(new android.view.TextureView.SurfaceTextureListener() {
                    @Override
                    public void onSurfaceTextureAvailable(android.graphics.SurfaceTexture surfaceTexture, int width, int height) {
                        try {
                            android.view.Surface surface = new android.view.Surface(surfaceTexture);
                            splashPlayer = new android.media.MediaPlayer();
                            splashPlayer.setSurface(surface);

                            // Configure as Sonification with ZERO volume so it NEVER claims Audio Focus or pauses background music!
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                android.media.AudioAttributes attr = new android.media.AudioAttributes.Builder()
                                        .setUsage(android.media.AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                        .build();
                                splashPlayer.setAudioAttributes(attr);
                            }
                            splashPlayer.setVolume(0f, 0f);

                            Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.caspian_splash_v22);
                            splashPlayer.setDataSource(MainActivity.this, videoUri);

                            splashPlayer.setOnVideoSizeChangedListener((mp, videoWidth, videoHeight) -> {
                                if (videoWidth > 0 && videoHeight > 0 && splashOverlay != null) {
                                    float videoAspect = (float) videoWidth / (float) videoHeight;
                                    int screenWidth = splashOverlay.getWidth();
                                    int screenHeight = splashOverlay.getHeight();
                                    if (screenWidth == 0 || screenHeight == 0) {
                                        android.util.DisplayMetrics metrics = getResources().getDisplayMetrics();
                                        screenWidth = metrics.widthPixels;
                                        screenHeight = metrics.heightPixels;
                                    }
                                    float screenAspect = (float) screenWidth / (float) screenHeight;
                                    FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) splashTextureView.getLayoutParams();
                                    if (videoAspect > screenAspect) {
                                        lp.width = (int) (screenHeight * videoAspect);
                                        lp.height = screenHeight;
                                    } else {
                                        lp.width = screenWidth;
                                        lp.height = (int) (screenWidth / videoAspect);
                                    }
                                    lp.gravity = android.view.Gravity.CENTER;
                                    splashTextureView.setLayoutParams(lp);
                                }
                            });

                            splashPlayer.setOnPreparedListener(mp -> {
                                mp.setLooping(false);
                                mp.start();
                            });
                            splashPlayer.setOnCompletionListener(mp -> dismissSplash.run());
                            splashPlayer.setOnErrorListener((mp, what, extra) -> {
                                dismissSplash.run();
                                return true;
                            });

                            splashPlayer.prepareAsync();
                        } catch (Throwable t) {
                            dismissSplash.run();
                        }
                    }

                    @Override public void onSurfaceTextureSizeChanged(android.graphics.SurfaceTexture surface, int width, int height) {}
                    @Override public boolean onSurfaceTextureDestroyed(android.graphics.SurfaceTexture surface) {
                        if (splashPlayer != null) {
                            try { splashPlayer.release(); } catch (Exception e) {}
                            splashPlayer = null;
                        }
                        return true;
                    }
                    @Override public void onSurfaceTextureUpdated(android.graphics.SurfaceTexture surface) {}
                });

                splashOverlay.setOnClickListener(v -> dismissSplash.run());
                splashOverlay.postDelayed(dismissSplash, 4000);
            } catch (Throwable t) {
                splashOverlay.setVisibility(View.GONE);
            }
        }
    }

    public void showCustomView(View view, WebChromeClient.CustomViewCallback callback) {
        if (customView != null) {
            hideCustomView();
            return;
        }
        customView = view;
        customViewCallback = callback;

        if (fullscreenContainer == null) {
            fullscreenContainer = new FrameLayout(this);
            fullscreenContainer.setBackgroundColor(0xFF000000);
            getWindow().addContentView(fullscreenContainer, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));
        }
        fullscreenContainer.addView(customView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        fullscreenContainer.setVisibility(View.VISIBLE);
        fullscreenContainer.bringToFront();

        // Immersive Full Screen System Flags
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );
        setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);

        if (ytRemoteFullscreen != null) {
            ytRemoteFullscreen.setImageResource(R.drawable.ic_pod_fullscreen_exit);
        }
    }

    public void hideCustomView() {
        if (customView == null) return;

        if (fullscreenContainer != null) {
            fullscreenContainer.removeView(customView);
            fullscreenContainer.setVisibility(View.GONE);
        }
        if (customViewCallback != null) {
            customViewCallback.onCustomViewHidden();
            customViewCallback = null;
        }
        customView = null;

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        if (ytRemoteFullscreen != null) {
            ytRemoteFullscreen.setImageResource(R.drawable.ic_pod_fullscreen);
        }
    }

    public View getCustomView() {
        return customView;
    }

    private void triggerVibration() {
        try {
            android.os.Vibrator v = (android.os.Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (v != null && v.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(android.os.VibrationEffect.createOneShot(60, android.os.VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    v.vibrate(60);
                }
            }
        } catch (Exception e) {}
    }

    private void silenceSystemAudioForSpeech(boolean silence) {
        try {
            android.media.AudioManager audioManager = (android.media.AudioManager) getSystemService(Context.AUDIO_SERVICE);
            if (audioManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                int adjust = silence ? android.media.AudioManager.ADJUST_MUTE : android.media.AudioManager.ADJUST_UNMUTE;
                audioManager.adjustStreamVolume(android.media.AudioManager.STREAM_NOTIFICATION, adjust, 0);
                audioManager.adjustStreamVolume(android.media.AudioManager.STREAM_SYSTEM, adjust, 0);
            }
        } catch (Exception e) {}
    }

    private void setupSpeechRecognizer() {
        try {
            if (SpeechRecognizer.isRecognitionAvailable(this)) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
                speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                speechIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);

                speechRecognizer.setRecognitionListener(new RecognitionListener() {
                    @Override public void onReadyForSpeech(Bundle params) {}
                    @Override public void onBeginningOfSpeech() {}
                    @Override
                    public void onRmsChanged(float rmsdB) {
                        if (speechWaveformView != null) {
                            speechWaveformView.setAmplitude(rmsdB);
                        }
                    }
                    @Override public void onBufferReceived(byte[] buffer) {}
                    @Override public void onEndOfSpeech() {}
                    @Override public void onError(int error) {
                        silenceSystemAudioForSpeech(false);
                    }

                    @Override
                    public void onResults(Bundle results) {
                        silenceSystemAudioForSpeech(false);
                        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                        if (matches != null && !matches.isEmpty()) {
                            String transcribedText = matches.get(0);
                            handleRecognizedText(transcribedText);
                        }
                    }

                    @Override public void onPartialResults(Bundle partialResults) {}
                    @Override public void onEvent(int eventType, Bundle params) {}
                });
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    // Multi-Engine Speech-to-Text Audio Recorder Fields
    private AudioRecord audioRecord;
    private boolean isRecordingPcmAudio = false;
    private ByteArrayOutputStream pcmAudioBuffer;
    private Thread pcmRecordingThread;
    private boolean isRecordingSpeechMode = false;
    private boolean justStartedSpeechDictation = false;

    public void startSpeechToText() {
        SharedPreferences prefs = getSharedPreferences("CaspianMobilePrefs", MODE_PRIVATE);
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
            String startHex = prefs.getString("theme_start_color", "#A2A9A9");
            String endHex = prefs.getString("theme_end_color", "#1B4264");
            int startColor = android.graphics.Color.parseColor(startHex);
            int endColor = android.graphics.Color.parseColor(endHex);
            FrameLayout speechContainer = findViewById(R.id.speech_waveform_container);
            if (speechContainer != null) {
                speechContainer.setVisibility(View.VISIBLE);
            }
            if (speechWaveformView != null) {
                speechWaveformView.setWaveColors(startColor, endColor);
                speechWaveformView.setVisibility(View.VISIBLE);
            }

            silenceSystemAudioForSpeech(true);

            if ("android_native".equalsIgnoreCase(sttEngine)) {
                if (speechRecognizer == null) setupSpeechRecognizer();
                if (speechRecognizer != null && speechIntent != null) {
                    speechRecognizer.startListening(speechIntent);
                }
            } else {
                startAudioRecording();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void stopSpeechToText() {
        isRecordingSpeechMode = false;
        silenceSystemAudioForSpeech(false);
        if (floatingCaspianCard != null) {
            floatingCaspianCard.animate().scaleX(1.0f).scaleY(1.0f).setDuration(150).start();
        }
        FrameLayout speechContainer = findViewById(R.id.speech_waveform_container);
        if (speechContainer != null) {
            speechContainer.setVisibility(View.GONE);
        }
        if (speechWaveformView != null) {
            speechWaveformView.setVisibility(View.GONE);
        }

        SharedPreferences prefs = getSharedPreferences("CaspianMobilePrefs", MODE_PRIVATE);
        String sttEngine = prefs.getString("stt_engine_mode", "android_native");

        if ("android_native".equalsIgnoreCase(sttEngine)) {
            if (speechRecognizer != null) {
                try {
                    speechRecognizer.stopListening();
                } catch(Exception e) {}
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

                        // Calculate RMS amplitude normalized to (-2.0 to 10.0) dB matching Android SpeechRecognizer
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
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private byte[] stopAudioRecordingAndGetWav() {
        isRecordingPcmAudio = false;
        if (audioRecord != null) {
            try {
                audioRecord.stop();
                audioRecord.release();
            } catch (Exception e) {}
            audioRecord = null;
        }
        if (pcmRecordingThread != null) {
            try { pcmRecordingThread.join(500); } catch (Exception e) {}
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
            new CaspianBridge(MainActivity.this).showToast("⚠️ Audio buffer empty.");
            return;
        }

        new Thread(() -> {
            try {
                String recognizedText = null;
                if ("deepgram".equalsIgnoreCase(engineMode)) {
                    String apiKey = prefs.getString("deepgram_api_key", "").trim();
                    if (apiKey.isEmpty()) {
                        runOnUiThread(() -> new CaspianBridge(MainActivity.this).showToast("⚠️ Deepgram API Key missing! Enter in Settings."));
                        return;
                    }
                    recognizedText = queryDeepgramApi(wavBytes, apiKey);
                    if (recognizedText != null) {
                        int pcmLen = wavBytes.length - 44;
                        int durationSec = Math.max(1, pcmLen / (16000 * 2));
                        long newTotal = prefs.getLong("deepgram_used_seconds", 0L) + durationSec;
                        SharedPreferences.Editor ed = prefs.edit();
                        ed.putLong("deepgram_used_seconds", newTotal);
                        ed.apply();

                        final long finalTotalSec = newTotal;
                        runOnUiThread(() -> {
                            if (controlWebView != null) {
                                controlWebView.evaluateJavascript("if(typeof window.updateDeepgramUsageBadge === 'function') { window.updateDeepgramUsageBadge(" + finalTotalSec + "); }", null);
                            }
                        });
                    }
                } else if ("huggingface".equalsIgnoreCase(engineMode)) {
                    String apiKey = prefs.getString("huggingface_api_key", "").trim();
                    if (apiKey.isEmpty()) {
                        runOnUiThread(() -> new CaspianBridge(MainActivity.this).showToast("⚠️ Hugging Face Token missing! Enter in Settings."));
                        return;
                    }
                    recognizedText = queryHuggingFaceApi(wavBytes, apiKey);
                }

                final String finalText = recognizedText;
                if (finalText != null && !finalText.trim().isEmpty()) {
                    runOnUiThread(() -> handleRecognizedText(finalText));
                } else {
                    runOnUiThread(() -> new CaspianBridge(MainActivity.this).showToast("⚠️ Speech not recognized. Speak louder."));
                }
            } catch (Exception e) {
                e.printStackTrace();
                final String err = e.getMessage();
                runOnUiThread(() -> new CaspianBridge(MainActivity.this).showToast("⚠️ STT API Error: " + (err != null ? err : "Network Failure")));
            }
        }).start();
    }

    private String readStreamString(InputStream is) throws Exception {
        if (is == null) return "";
        BufferedReader r = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        StringBuilder total = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) {
            total.append(line).append('\n');
        }
        return total.toString();
    }

    private String queryDeepgramApi(byte[] wavBytes, String apiKey) throws Exception {
        URL url = new URL("https://api.deepgram.com/v1/listen?model=nova-2&smart_format=true");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Token " + apiKey);
        conn.setRequestProperty("Content-Type", "audio/wav");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(15000);
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(wavBytes);
        }

        int code = conn.getResponseCode();
        InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
        String resp = readStreamString(is);
        JSONObject json = new JSONObject(resp);
        return json.getJSONObject("results")
                .getJSONArray("channels")
                .getJSONObject(0)
                .getJSONArray("alternatives")
                .getJSONObject(0)
                .getString("transcript");
    }

    private String queryHuggingFaceApi(byte[] wavBytes, String apiKey) throws Exception {
        String[] endpoints = new String[]{
            "https://router.huggingface.co/hf-inference/models/openai/whisper-large-v3",
            "https://api-inference.huggingface.co/models/openai/whisper-large-v3",
            "https://api-inference.huggingface.co/models/openai/whisper-large-v3-turbo"
        };

        Exception lastErr = null;
        for (String endpointUrl : endpoints) {
            try {
                URL url = new URL(endpointUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + apiKey);
                conn.setRequestProperty("Content-Type", "audio/flac");
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(15000);
                conn.setDoOutput(true);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(wavBytes);
                }

                int code = conn.getResponseCode();
                InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
                String resp = readStreamString(is);

                if (code == 401) {
                    throw new Exception("HF Token Invalid (401). Check Token in Settings.");
                } else if (code == 503) {
                    throw new Exception("HF Model Loading (503). Try again in a moment.");
                }

                if (resp != null && !resp.trim().isEmpty()) {
                    String trimmed = resp.trim();
                    if (trimmed.startsWith("{")) {
                        JSONObject json = new JSONObject(trimmed);
                        if (json.has("text")) {
                            return json.getString("text");
                        } else if (json.has("error")) {
                            lastErr = new Exception("HF: " + json.getString("error"));
                        }
                    } else if (trimmed.startsWith("[")) {
                        JSONArray arr = new JSONArray(trimmed);
                        if (arr.length() > 0 && arr.getJSONObject(0).has("text")) {
                            return arr.getJSONObject(0).getString("text");
                        }
                    }
                }
            } catch (Exception e) {
                lastErr = e;
            }
        }
        if (lastErr != null) throw lastErr;
        return null;
    }

    private String queryGroqApi(byte[] wavBytes, String apiKey) throws Exception {
        String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
        URL url = new URL("https://api.groq.com/openai/v1/audio/transcriptions");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(15000);
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(("--" + boundary + "\r\n").getBytes());
            os.write(("Content-Disposition: form-data; name=\"model\"\r\n\r\nwhisper-large-v3\r\n").getBytes());

            os.write(("--" + boundary + "\r\n").getBytes());
            os.write(("Content-Disposition: form-data; name=\"file\"; filename=\"audio.wav\"\r\n").getBytes());
            os.write(("Content-Type: audio/wav\r\n\r\n").getBytes());
            os.write(wavBytes);
            os.write(("\r\n--" + boundary + "--\r\n").getBytes());
        }

        int code = conn.getResponseCode();
        InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
        String resp = readStreamString(is);
        JSONObject json = new JSONObject(resp);
        return json.getString("text");
    }

    private void handleRecognizedText(String text) {
        if (text == null || text.trim().isEmpty()) return;
        TabItem activeTab = getActiveTab();
        if (activeTab != null && activeTab.webView != null) {
            String cursorJs = "(function() {\n" +
                    "  var txt = " + JSONObject.quote(text) + ";\n" +
                    "  var el = document.activeElement;\n" +
                    "  var inserted = false;\n" +
                    "  if (el && (el.tagName === 'TEXTAREA' || el.tagName === 'INPUT' || el.isContentEditable)) {\n" +
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
                    "    var ta = document.querySelector('#prompt-textarea, textarea, div[contenteditable=\"true\"], .input-area');\n" +
                    "    if (ta) {\n" +
                    "      ta.focus();\n" +
                    "      try { document.execCommand('insertText', false, txt); } catch(err) { ta.value = txt; }\n" +
                    "      ta.dispatchEvent(new Event('input', { bubbles: true }));\n" +
                    "      inserted = true;\n" +
                    "    }\n" +
                    "  }\n" +
                    "  return inserted;\n" +
                    "})();";

            activeTab.webView.evaluateJavascript(cursorJs, value -> {
                boolean wasInserted = "true".equalsIgnoreCase(value);
                if (wasInserted) {
                    new CaspianBridge(MainActivity.this).showToast("✨ Speech inserted at text cursor!");
                } else {
                    showFloatingCopyPill(text);
                }
            });
        } else {
            showFloatingCopyPill(text);
        }
    }

    private void showFloatingCopyPill(String text) {
        runOnUiThread(() -> {
            try {
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData.newPlainText("Transcribed Speech", text);
                if (clipboard != null) clipboard.setPrimaryClip(clip);
                new CaspianBridge(this).showToast("📋 Transcribed & copied to clipboard!\n\"" + text + "\"");
            } catch(Exception e) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        CookieManager.getInstance().flush();
        updateRefreshTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        refreshHandler.removeCallbacks(refreshRunnable);
    }

    // Debug Recording Control Methods
    public boolean isDebugRecordingActive() {
        return isDebugRecording;
    }

    public synchronized void startDebugRecording() {
        isDebugRecording = true;
        debugLogBuffer.setLength(0);
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());
        debugLogBuffer.append("=========================================\n")
                .append("CASPIAN BETA A DIAGNOSTIC LOG\n")
                .append("Started: ").append(timestamp).append("\n")
                .append("App Version: ").append(new CaspianBridge(this).getAppVersion()).append("\n")
                .append("=========================================\n\n");
        appendDebugLog("[SYSTEM]", "Started recording console errors, network events & app diagnostics.");
        new CaspianBridge(this).showToast("Console & System Logger Started!");
    }

    public synchronized void stopAndSaveDebugLog() {
        isDebugRecording = false;
        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(new Date());
        String fileName = "Caspian_BetaA_Debug_Log_" + timestamp + ".txt";
        String content = debugLogBuffer.toString();

        new CaspianBridge(this).downloadFile(fileName, content, "text/plain");
    }

    private synchronized void appendDebugLog(String tag, String message) {
        if (!isDebugRecording) return;
        String timeStr = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(new Date());
        debugLogBuffer.append("[").append(timeStr).append("] [").append(tag).append("] ").append(message).append("\n");
        Log.d("CaspianDebugA", "[" + tag + "] " + message);
    }

    public TabItem getActiveTab() {
        for (TabItem item : tabsList) {
            if (item.id == activeTabId) return item;
        }
        return tabsList.isEmpty() ? null : tabsList.get(0);
    }

    public void saveTabsToPrefs() {
        try {
            SharedPreferences prefs = getSharedPreferences("CaspianMobilePrefs", MODE_PRIVATE);
            JSONArray arr = new JSONArray();
            for (TabItem item : tabsList) {
                JSONObject obj = new JSONObject();
                obj.put("id", item.id);
                obj.put("title", item.title);
                obj.put("url", item.url);
                obj.put("service", item.service);
                obj.put("nickname", item.nickname != null ? item.nickname : "");
                obj.put("isFavorite", item.isFavorite);
                arr.put(obj);
            }
            prefs.edit().putString("saved_tabs", arr.toString()).putInt("active_tab_id", activeTabId).apply();
            CookieManager.getInstance().flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadTabsFromPrefs() {
        try {
            SharedPreferences prefs = getSharedPreferences("CaspianMobilePrefs", MODE_PRIVATE);
            String jsonStr = prefs.getString("saved_tabs", null);
            int savedActiveId = prefs.getInt("active_tab_id", 1);
            if (jsonStr != null && !jsonStr.trim().isEmpty()) {
                JSONArray arr = new JSONArray(jsonStr);
                if (arr.length() > 0) {
                    tabsList.clear();
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        int id = obj.optInt("id", i + 1);
                        String title = obj.optString("title", "Browser Tab");
                        String url = obj.optString("url", "file:///android_asset/launch_hub.html");
                        String service = obj.optString("service", "hub");
                        String nickname = obj.optString("nickname", "");
                        boolean isFav = obj.optBoolean("isFavorite", false);

                        TabItem tab = new TabItem(id, title, url, service, null);
                        tab.nickname = nickname;
                        tab.isFavorite = isFav;
                        WebView wv = createTabWebView(tab);
                        if (wv != null) {
                            tabsList.add(tab);
                            if (id >= nextTabId) nextTabId = id + 1;
                        }
                    }
                    if (!tabsList.isEmpty()) {
                        switchTab(savedActiveId, false);
                        return;
                    }
                }
            }
        } catch (Throwable t) {
            Log.e("CaspianDebugA", "Error loading saved tabs: " + t.getMessage(), t);
        }

        // Fallback default initial Tab
        tabsList.clear();
        createNewTab("hub");
    }

    @SuppressLint("SetJavaScriptEnabled")
    private WebView createTabWebView(TabItem tab) {
        WebView wv = new WebView(this);
        wv.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        wv.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        wv.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        CookieManager.getInstance().setAcceptThirdPartyCookies(wv, true);

        WebSettings settings = wv.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        settings.setOffscreenPreRaster(true);
        settings.setUserAgentString(DEFAULT_CHROME_UA);

        // Hardware Performance Optimizations
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        settings.setEnableSmoothTransition(true);
        settings.setMediaPlaybackRequiresUserGesture(true);

        // Single-Window OAuth Transport for Instant Google Account Login ("Continue with Google")
        settings.setSupportMultipleWindows(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);

        wv.addJavascriptInterface(new CaspianBridge(this), "CaspianBridge");

        wv.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                CookieManager.getInstance().flush();
                appendDebugLog("URL_PAGE_START", url);
                view.evaluateJavascript("window.__caspian_tab_id = " + tab.id + ";", null);
                if (url != null && url.toLowerCase().contains("youtube.com")) {
                    try {
                        String ytEarlyDefuser = readAssetFile("youtube_helper.js");
                        view.evaluateJavascript(ytEarlyDefuser, null);
                    } catch(Exception e) {}
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                CookieManager.getInstance().flush();
                if (request != null && request.getUrl() != null) {
                    String uStr = request.getUrl().toString();
                    appendDebugLog("URL_REDIRECT", uStr);

                    // 100% In-App WebView OAuth Interceptor with Header Stripping & Chrome User-Agent Injections
                    if (uStr.contains("accounts.google.com") || uStr.contains("appleid.apple.com") || uStr.contains("auth.openai.com")) {
                        appendDebugLog("OAUTH_WEBVIEW_HEADER_STRIP", "Intercepting OAuth URL inside WebView with header stripping: " + uStr);
                        view.getSettings().setUserAgentString(DEFAULT_CHROME_UA);
                        Map<String, String> headers = new HashMap<>();
                        headers.put("X-Requested-With", "");
                        headers.put("Sec-CH-UA", "\"Chromium\";v=\"128\", \"Not;A=Brand\";v=\"24\", \"Google Chrome\";v=\"128\"");
                        headers.put("Sec-CH-UA-Mobile", "?1");
                        headers.put("Sec-CH-UA-Platform", "\"Android\"");
                        view.loadUrl(uStr, headers);
                        return true;
                    }
                }
                return super.shouldOverrideUrlLoading(view, request);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                if (request != null && request.getUrl() != null && error != null) {
                    appendDebugLog("RESOURCE_ERROR", "Error " + error.getErrorCode() + ": " + error.getDescription() + " @ " + request.getUrl().toString());
                }
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                super.onReceivedHttpError(view, request, errorResponse);
                if (request != null && request.getUrl() != null && errorResponse != null) {
                    appendDebugLog("HTTP_ERROR", "HTTP " + errorResponse.getStatusCode() + " " + errorResponse.getReasonPhrase() + " @ " + request.getUrl().toString());
                }
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                super.onReceivedSslError(view, handler, error);
                if (error != null) {
                    appendDebugLog("SSL_ERROR", "SSL Error " + error.getPrimaryError() + " @ " + error.getUrl());
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                CookieManager.getInstance().flush();
                appendDebugLog("URL_PAGE_FINISH", url);

                // Auto-Recover from OAuth Callback Completion
                if (url != null && url.contains("chatgpt.com/api/auth/callback")) {
                    appendDebugLog("OAUTH_CALLBACK_FINISH", "OAuth callback complete. Flushing cookies & reloading main page.");
                    CookieManager.getInstance().flush();
                    view.postDelayed(() -> {
                        CookieManager.getInstance().flush();
                        view.loadUrl("https://chatgpt.com/");
                    }, 500);
                }

                if (!isPrinting && isSupportedUrl(url)) {
                    injectPrunerScript(view);
                }

                // Restore active theme mode immediately on finish load
                try {
                    SharedPreferences prefsShared = getSharedPreferences("CaspianMobilePrefs", MODE_PRIVATE);
                    String themeMode = prefsShared.getString("themeMode", "dark");
                    boolean isDark = "dark".equalsIgnoreCase(themeMode);
                    String themeJs = "(function() {" +
                            "  var isDark = " + isDark + ";" +
                            "  document.documentElement.classList.toggle('dark', isDark);" +
                            "  document.documentElement.classList.toggle('light', !isDark);" +
                            "  document.documentElement.setAttribute('data-theme', isDark ? 'dark' : 'light');" +
                            "  if (document.body) {" +
                            "    document.body.classList.toggle('dark-theme', isDark);" +
                            "    document.body.classList.toggle('light-theme', !isDark);" +
                            "    document.body.classList.toggle('dark', isDark);" +
                            "  }" +
                            "  var provider = document.querySelector('gds-theme-provider, .mat-app-background, mat-sidenav-container');" +
                            "  if (provider) {" +
                            "    provider.setAttribute('mode', isDark ? 'dark' : 'light');" +
                            "    provider.setAttribute('color-scheme', isDark ? 'dark' : 'light');" +
                            "  }" +
                            "  var overrideStyle = document.getElementById('caspian-theme-override');" +
                            "  if (isDark) {" +
                            "    if (!overrideStyle) {" +
                            "      overrideStyle = document.createElement('style');" +
                            "      overrideStyle.id = 'caspian-theme-override';" +
                            "      document.head.appendChild(overrideStyle);" +
                            "    }" +
                            "    overrideStyle.textContent = 'html, body { color-scheme: dark !important; background-color: #131314 !important; color: #e3e3e3 !important; }';" +
                            "  } else if (overrideStyle) {" +
                            "    overrideStyle.remove();" +
                            "  }" +
                            "  try { localStorage.setItem('theme', isDark ? 'dark' : 'light'); } catch(e){}" +
                            "  try { localStorage.setItem('colorMode', isDark ? 'dark' : 'light'); } catch(e){}" +
                            "})();";
                    view.evaluateJavascript(themeJs, null);
                } catch (Exception e) {}

                tab.url = url;
                String t = view.getTitle();
                if (t != null && !t.isEmpty() && !t.startsWith("file://")) {
                    tab.title = t;
                }
                saveTabsToPrefs();
                updateSearchNavVisibility();
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                if (request != null && request.getUrl() != null) {
                    String url = request.getUrl().toString().toLowerCase();
                    SharedPreferences prefs = getSharedPreferences("CaspianMobilePrefs", MODE_PRIVATE);
                    boolean adblockEnabled = "true".equalsIgnoreCase(prefs.getString("adblock_enabled", "true"));
                    if (adblockEnabled) {
                        if (url.contains("googleads") ||
                            url.contains("doubleclick.net") ||
                            url.contains("googlesyndication.com") ||
                            url.contains("adservice.google.com") ||
                            url.contains("/pagead/") ||
                            url.contains("/pcs/activeview")) {
                            return new WebResourceResponse("text/plain", "UTF-8", new java.io.ByteArrayInputStream(new byte[0]));
                        }
                    }
                }
                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
                super.doUpdateVisitedHistory(view, url, isReload);
                CookieManager.getInstance().flush();
                appendDebugLog("URL_NAV_HISTORY", url);

                if (!isPrinting && isSupportedUrl(url)) {
                    injectPrunerScript(view);
                }
                tab.url = url;
                updateSearchNavVisibility();
                updateFloatingYTRemoteVisibility();
            }
        });

        // OAuth Multi-Window Transport: Dedicated temporary popup WebView for Google & Apple logins!
        wv.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                MainActivity.this.showCustomView(view, callback);
            }

            @Override
            public void onHideCustomView() {
                MainActivity.this.hideCustomView();
            }

            @Override
            public void onPermissionRequest(final android.webkit.PermissionRequest request) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (request == null) return;
                        String[] resources = request.getResources();
                        boolean needsMedia = false;
                        if (resources != null) {
                            for (String res : resources) {
                                if (android.webkit.PermissionRequest.RESOURCE_AUDIO_CAPTURE.equals(res) ||
                                    android.webkit.PermissionRequest.RESOURCE_VIDEO_CAPTURE.equals(res)) {
                                    needsMedia = true;
                                    break;
                                }
                            }
                        }
                        if (needsMedia) {
                            boolean hasMic = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
                            boolean hasCam = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
                            if (!hasMic || !hasCam) {
                                pendingWebPermissionRequest = request;
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                                        Manifest.permission.RECORD_AUDIO,
                                        Manifest.permission.CAMERA,
                                        Manifest.permission.MODIFY_AUDIO_SETTINGS
                                }, MIC_PERMISSION_REQUEST_CODE);
                                return;
                            }
                        }
                        request.grant(request.getResources());
                    }
                });
            }

            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
                if (uploadMessage != null) {
                    uploadMessage.onReceiveValue(null);
                    uploadMessage = null;
                }
                uploadMessage = filePathCallback;
                if (checkPermissions()) {
                    openFileChooser();
                } else {
                    requestPermissions();
                }
                return true;
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                if (consoleMessage != null && consoleMessage.message() != null) {
                    appendDebugLog("WEB_CONSOLE", consoleMessage.message() + " (Line " + consoleMessage.lineNumber() + " @ " + consoleMessage.sourceId() + ")");
                }
                return super.onConsoleMessage(consoleMessage);
            }

            @SuppressLint("SetJavaScriptEnabled")
            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
                appendDebugLog("POPUP_CREATE_WINDOW", "Spawning temporary popup WebView for OAuth popup.");
                WebView popupWebView = new WebView(MainActivity.this);
                popupWebView.setLayoutParams(new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                ));

                WebSettings pSettings = popupWebView.getSettings();
                pSettings.setJavaScriptEnabled(true);
                pSettings.setDomStorageEnabled(true);
                pSettings.setDatabaseEnabled(true);
                pSettings.setSupportMultipleWindows(true);
                pSettings.setJavaScriptCanOpenWindowsAutomatically(true);
                pSettings.setUserAgentString(DEFAULT_CHROME_UA);

                CookieManager.getInstance().setAcceptCookie(true);
                CookieManager.getInstance().setAcceptThirdPartyCookies(popupWebView, true);

                popupWebView.setWebChromeClient(new WebChromeClient() {
                    @Override
                    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                        if (consoleMessage != null && consoleMessage.message() != null) {
                            appendDebugLog("POPUP_CONSOLE", consoleMessage.message() + " (Line " + consoleMessage.lineNumber() + " @ " + consoleMessage.sourceId() + ")");
                        }
                        return super.onConsoleMessage(consoleMessage);
                    }

                    @Override
                    public void onCloseWindow(WebView window) {
                        super.onCloseWindow(window);
                        appendDebugLog("POPUP_CLOSE_WINDOW", "Popup closed automatically. Destroying temporary popup WebView.");
                        webViewContainer.removeView(window);
                        window.destroy();
                        CookieManager.getInstance().flush();
                    }
                });

                popupWebView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageStarted(WebView pView, String pUrl, android.graphics.Bitmap favicon) {
                        super.onPageStarted(pView, pUrl, favicon);
                        CookieManager.getInstance().flush();
                        appendDebugLog("POPUP_PAGE_START", pUrl);
                    }

                    @Override
                    public boolean shouldOverrideUrlLoading(WebView pView, WebResourceRequest request) {
                        CookieManager.getInstance().flush();
                        if (request != null && request.getUrl() != null) {
                            String pUrl = request.getUrl().toString();
                            appendDebugLog("POPUP_REDIRECT", pUrl);
                            if (pUrl.contains("accounts.google.com") || pUrl.contains("appleid.apple.com") || pUrl.contains("auth.openai.com")) {
                                pView.getSettings().setUserAgentString(DEFAULT_CHROME_UA);
                                Map<String, String> headers = new HashMap<>();
                                headers.put("X-Requested-With", "");
                                headers.put("Sec-CH-UA", "\"Chromium\";v=\"128\", \"Not;A=Brand\";v=\"24\", \"Google Chrome\";v=\"128\"");
                                headers.put("Sec-CH-UA-Mobile", "?1");
                                headers.put("Sec-CH-UA-Platform", "\"Android\"");
                                pView.loadUrl(pUrl, headers);
                                return true;
                            }
                        }
                        return super.shouldOverrideUrlLoading(pView, request);
                    }

                    @Override
                    public void onPageFinished(WebView pView, String pUrl) {
                        super.onPageFinished(pView, pUrl);
                        CookieManager.getInstance().flush();
                        appendDebugLog("POPUP_PAGE_FINISH", pUrl);
                    }
                });

                webViewContainer.addView(popupWebView);
                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                transport.setWebView(popupWebView);
                resultMsg.sendToTarget();
                return true;
            }
        });

        wv.setFindListener((activeMatchOrdinal, numberOfMatches, isDoneCounting) -> {
            runOnUiThread(() -> {
                if (navFinderCount != null) {
                    if (numberOfMatches > 0) {
                        navFinderCount.setText((activeMatchOrdinal + 1) + "/" + numberOfMatches);
                    } else {
                        navFinderCount.setText("0/0");
                    }
                }
            });
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            wv.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                if (isGoogleDockScrollCollapseEnabled && !isFinderModeActive) {
                    if (scrollY - oldScrollY > 15) {
                        collapseSearchDock();
                    } else if (oldScrollY - scrollY > 15 || scrollY <= 10) {
                        expandSearchDock();
                    }
                }
            });
        }

        webViewContainer.addView(wv);
        tab.webView = wv;
        wv.loadUrl(tab.url);
        return wv;
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupControlWebView() {
        WebSettings settings = controlWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setOffscreenPreRaster(true);
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        settings.setMediaPlaybackRequiresUserGesture(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        
        controlWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        controlWebView.setBackgroundColor(0);
        controlWebView.addJavascriptInterface(new CaspianBridge(this), "CaspianBridge");

        controlWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (request != null && request.getUrl() != null) {
                    String url = request.getUrl().toString();
                    if (!url.startsWith("file:///android_asset/")) {
                        openNewTab(url, "browser");
                        closeControlSheet();
                        return true;
                    }
                }
                return false;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url != null && !url.startsWith("file:///android_asset/")) {
                    openNewTab(url, "browser");
                    closeControlSheet();
                    return true;
                }
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                controlWebView.evaluateJavascript("if (typeof window.renderOpenTabs === 'function') { window.renderOpenTabs(); }", null);
                controlWebView.evaluateJavascript("if (typeof restoreSavedSettings === 'function') { restoreSavedSettings(); }", null);
            }
        });

        controlWebView.loadUrl("file:///android_asset/mobile_control.html");
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupNativeFloatingButton() {
        floatingCaspianCard.setOnTouchListener((view, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    dX = view.getX() - event.getRawX();
                    dY = view.getY() - event.getRawY();
                    startRawX = event.getRawX();
                    startRawY = event.getRawY();
                    isDragging = false;
                    justStartedSpeechDictation = false;

                    if (!isRecordingSpeechMode) {
                        if (longPressRunnable != null) {
                            longPressHandler.removeCallbacks(longPressRunnable);
                        }
                        longPressRunnable = () -> {
                            if (!isDragging) {
                                SharedPreferences prefs = getSharedPreferences("CaspianMobilePrefs", MODE_PRIVATE);
                                boolean isDriftEnabled = !"false".equalsIgnoreCase(prefs.getString("caspian_current_enabled", "true"));
                                if (!isDriftEnabled) {
                                    triggerVibration();
                                    Toast.makeText(MainActivity.this, "⚠️ Caspian Drift Engine is OFF. Enable it in Caspian Engines tab.", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                isRecordingSpeechMode = true;
                                justStartedSpeechDictation = true;
                                triggerVibration();
                                startSpeechToText();
                                floatingCaspianCard.animate()
                                        .scaleX(1.25f)
                                        .scaleY(1.25f)
                                        .setDuration(150)
                                        .start();
                            }
                        };
                        longPressHandler.postDelayed(longPressRunnable, 450);
                    }
                    return true;

                case MotionEvent.ACTION_MOVE:
                    float deltaX = Math.abs(event.getRawX() - startRawX);
                    float deltaY = Math.abs(event.getRawY() - startRawY);

                    if (deltaX > 10 || deltaY > 10) {
                        if (longPressRunnable != null) {
                            longPressHandler.removeCallbacks(longPressRunnable);
                        }
                        isDragging = true;
                        float newX = event.getRawX() + dX;
                        float newY = event.getRawY() + dY;

                        View parent = (View) view.getParent();
                        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
                        int statusBarHeight = resourceId > 0 ? getResources().getDimensionPixelSize(resourceId) : 40;

                        newX = Math.max(0, Math.min(parent.getWidth() - view.getWidth(), newX));
                        newY = Math.max(statusBarHeight + 10, Math.min(parent.getHeight() - view.getHeight(), newY));

                        view.animate()
                                .x(newX)
                                .y(newY)
                                .setDuration(0)
                                .start();
                    }
                    return true;

                case MotionEvent.ACTION_UP:
                    if (longPressRunnable != null) {
                        longPressHandler.removeCallbacks(longPressRunnable);
                    }

                    if (isDragging) {
                        return true;
                    }

                    // If long-press just fired to start dictation, user released finger -> keep recording in background!
                    if (justStartedSpeechDictation) {
                        justStartedSpeechDictation = false;
                        return true;
                    }

                    // If speech recording is ACTIVE, tapping the button STOPS recording & sends audio!
                    if (isRecordingSpeechMode) {
                        isRecordingSpeechMode = false;
                        triggerVibration();
                        stopSpeechToText();
                        return true;
                    }

                    // Otherwise, normal tap toggles Caspian Control Sheet
                    SharedPreferences prefs = getSharedPreferences("CaspianMobilePrefs", MODE_PRIVATE);
                    String chosenTaSfx = prefs.getString("sfx_file_ta", "pop_click.mp3");
                    playAssetSound("sfx/" + chosenTaSfx);
                    int tapDurationVal = 100;
                    try {
                        String tapDurStr = prefs.getString("theme_button_tap_duration", "100");
                        tapDurationVal = Integer.parseInt(tapDurStr);
                    } catch(Exception e) {}

                    final int finalTapDur = tapDurationVal;
                    if (finalTapDur > 0) {
                        float targetScale = currentActionBtnScale;
                        floatingCaspianCard.animate()
                                .scaleX(targetScale * 0.88f)
                                .scaleY(targetScale * 0.88f)
                                .setDuration(finalTapDur)
                                .withEndAction(() -> {
                                    floatingCaspianCard.animate()
                                            .scaleX(targetScale)
                                            .scaleY(targetScale)
                                            .setDuration(finalTapDur)
                                            .setInterpolator(new android.view.animation.OvershootInterpolator(1.2f))
                                            .start();
                                })
                                .start();
                    }
                    toggleControlSheet();
                    return true;
            }
            return false;
        });
    }

    private void setupSmartKeyboardAvoidance() {
        View rootView = findViewById(R.id.root_container);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            private int lastKeyboardHeight = 0;
            private float originalCardY = -1;

            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                rootView.getWindowVisibleDisplayFrame(r);
                int screenHeight = rootView.getHeight();
                int keyboardHeight = screenHeight - r.height();

                int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
                int statusBarHeight = resourceId > 0 ? getResources().getDimensionPixelSize(resourceId) : 40;
                final int topCeiling = statusBarHeight + 20;

                if (keyboardHeight > 250) {
                    if (lastKeyboardHeight == 0) {
                        float currentY = floatingCaspianCard.getY();
                        float visibleBottom = r.bottom;
                        float cardHeight = floatingCaspianCard.getHeight();

                        if (currentY > visibleBottom - cardHeight - 20) {
                            originalCardY = currentY;
                            float shiftedY = visibleBottom - cardHeight - 20;
                            shiftedY = Math.max(topCeiling, shiftedY);
                            floatingCaspianCard.animate().y(shiftedY).setDuration(150).start();
                        }
                    }
                    lastKeyboardHeight = keyboardHeight;
                } else {
                    if (lastKeyboardHeight > 0) {
                        if (originalCardY != -1) {
                            floatingCaspianCard.animate().y(originalCardY).setDuration(150).start();
                            originalCardY = -1;
                        }
                    }
                    lastKeyboardHeight = 0;
                }
            }
        });
    }

    public void applyFloatingTheme(String startHex, String endHex, String shape) {
        try {
            int startColor = android.graphics.Color.parseColor(startHex);
            int endColor = android.graphics.Color.parseColor(endHex);

            android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable(
                    android.graphics.drawable.GradientDrawable.Orientation.TL_BR,
                    new int[]{startColor, endColor}
            );

            float density = getResources().getDisplayMetrics().density;
            float radius = 12 * density; // default squircle
            if ("rounded".equalsIgnoreCase(shape)) {
                radius = 8 * density;
            } else if ("circle".equalsIgnoreCase(shape)) {
                radius = 26 * density; // 52dp diameter / 2 = 26dp radius
            } else if ("square".equalsIgnoreCase(shape)) {
                radius = 0 * density;
            }

            gd.setCornerRadius(radius);
            floatingCaspianCard.setBackground(gd);
            floatingCaspianCard.setRadius(radius);

            // Save in Preferences
            SharedPreferences prefs = getSharedPreferences("CaspianMobilePrefs", MODE_PRIVATE);
            prefs.edit()
                    .putString("theme_start_color", startHex)
                    .putString("theme_end_color", endHex)
                    .putString("theme_icon_shape", shape)
                    .apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void toggleHostPageTheme(boolean isDark) {
        TabItem activeTab = getActiveTab();
        if (activeTab != null && activeTab.webView != null) {
            String themeJs = "(function() {" +
                    "  var isDark = " + isDark + ";" +
                    "  document.documentElement.classList.toggle('dark', isDark);" +
                    "  document.documentElement.classList.toggle('light', !isDark);" +
                    "  document.documentElement.setAttribute('data-theme', isDark ? 'dark' : 'light');" +
                    "  if (document.body) {" +
                    "    document.body.classList.toggle('dark-theme', isDark);" +
                    "    document.body.classList.toggle('light-theme', !isDark);" +
                    "    document.body.classList.toggle('dark', isDark);" +
                    "  }" +
                    "  var provider = document.querySelector('gds-theme-provider, .mat-app-background, mat-sidenav-container');" +
                    "  if (provider) {" +
                    "    provider.setAttribute('mode', isDark ? 'dark' : 'light');" +
                    "    provider.setAttribute('color-scheme', isDark ? 'dark' : 'light');" +
                    "  }" +
                    "  var overrideStyle = document.getElementById('caspian-theme-override');" +
                    "  if (isDark) {" +
                    "    if (!overrideStyle) {" +
                    "      overrideStyle = document.createElement('style');" +
                    "      overrideStyle.id = 'caspian-theme-override';" +
                    "      document.head.appendChild(overrideStyle);" +
                    "    }" +
                    "    overrideStyle.textContent = 'html, body { color-scheme: dark !important; background-color: #131314 !important; color: #e3e3e3 !important; }';" +
                    "  } else if (overrideStyle) {" +
                    "    overrideStyle.remove();" +
                    "  }" +
                    "  try { localStorage.setItem('theme', isDark ? 'dark' : 'light'); } catch(e){}" +
                    "  try { localStorage.setItem('colorMode', isDark ? 'dark' : 'light'); } catch(e){}" +
                    "})();";
            activeTab.webView.evaluateJavascript(themeJs, null);
        }
    }

    public String getOpenTabsJson() {
        try {
            JSONArray arr = new JSONArray();
            for (TabItem item : tabsList) {
                JSONObject obj = new JSONObject();
                obj.put("id", item.id);
                obj.put("title", item.title);
                obj.put("url", item.url);
                obj.put("service", item.service);
                obj.put("nickname", item.nickname != null ? item.nickname : "");
                obj.put("isMuted", item.isMuted);
                obj.put("isPlayingAudio", item.isPlayingAudio);
                obj.put("isFavorite", item.isFavorite);
                obj.put("active", item.id == activeTabId);
                arr.put(obj);
            }
            return arr.toString();
        } catch (Exception e) {
            return "[]";
        }
    }

    public List<TabItem> getTabsList() {
        return tabsList;
    }

    public void openNewTab(String url) {
        openNewTab(url, "browser");
    }

    public void openNewTab(String url, String service) {
        runOnUiThread(() -> {
            int newId = nextTabId++;
            String tabService = service != null ? service : "browser";
            String title = "Web";
            if (url != null) {
                if (url.contains("youtube.com") || url.contains("youtu.be")) {
                    tabService = "youtube";
                    title = "YouTube";
                } else if (url.contains("chatgpt.com")) {
                    tabService = "chatgpt";
                    title = "ChatGPT";
                } else if (url.contains("gemini.google.com")) {
                    tabService = "gemini";
                    title = "Google Gemini";
                } else if (url.contains("github.com")) {
                    tabService = "github";
                    title = "GitHub";
                }
            }
            TabItem newTab = new TabItem(newId, title, url != null ? url : "https://www.google.com/", tabService, null);
            createTabWebView(newTab);
            tabsList.add(newTab);
            switchTab(newId);
        });
    }

    public void createNewTab(String service) {
        int newId = nextTabId++;
        String url = "https://chatgpt.com/";
        String title = "ChatGPT";
        if ("gemini".equalsIgnoreCase(service)) {
            url = "https://gemini.google.com/";
            title = "Google Gemini";
        } else if ("hub".equalsIgnoreCase(service)) {
            url = "file:///android_asset/launch_hub.html";
            title = "Caspian Hub";
        } else if ("google".equalsIgnoreCase(service)) {
            url = "https://www.google.com/";
            title = "Google Search";
        } else if ("youtube".equalsIgnoreCase(service)) {
            url = "https://www.youtube.com/";
            title = "YouTube";
        }

        TabItem newTab = new TabItem(newId, title, url, service, null);
        createTabWebView(newTab);
        tabsList.add(newTab);
        switchTab(newId);
    }

    public void createNewTabWithPrefill(String service, String promptContext) {
        int newId = nextTabId++;
        String url = "https://chatgpt.com/";
        String title = "ChatGPT";
        if ("gemini".equalsIgnoreCase(service)) {
            url = "https://gemini.google.com/";
            title = "Google Gemini";
        } else if ("hub".equalsIgnoreCase(service)) {
            url = "file:///android_asset/launch_hub.html";
            title = "Caspian Hub";
        } else if ("google".equalsIgnoreCase(service)) {
            url = "https://www.google.com/";
            title = "Google Search";
        } else if ("youtube".equalsIgnoreCase(service)) {
            url = "https://www.youtube.com/";
            title = "YouTube";
        }

        this.pendingPrefill = promptContext;
        TabItem newTab = new TabItem(newId, title, url, service, null);
        createTabWebView(newTab);
        tabsList.add(newTab);
        switchTab(newId);
    }

    public void switchTab(int tabId) {
        switchTab(tabId, true);
    }

    public void switchTab(int tabId, boolean closeSheet) {
        if (tabsList.isEmpty()) return;

        boolean found = false;
        for (TabItem item : tabsList) {
            if (item.id == tabId) {
                found = true;
                break;
            }
        }
        int targetTabId = found ? tabId : tabsList.get(0).id;
        activeTabId = targetTabId;

        boolean anyYouTubeTabActive = false;
        for (TabItem item : tabsList) {
            if (item.service != null && item.service.toLowerCase().contains("youtube")) {
                anyYouTubeTabActive = true;
                break;
            }
        }

        for (TabItem item : tabsList) {
            if (item.webView != null) {
                if (item.id == targetTabId) {
                    item.webView.setVisibility(View.VISIBLE);
                    item.webView.onResume();
                    item.webView.bringToFront();
                    item.webView.requestFocus();
                } else {
                    item.webView.setVisibility(View.GONE);
                    boolean isYT = (item.service != null && item.service.toLowerCase().contains("youtube"));
                    if (!isYT) {
                        item.webView.onPause();
                    }
                }
            }
        }
        if (closeSheet) {
            closeControlSheet();
        }

        updateSearchNavVisibility();
        updateFloatingYTRemoteVisibility();
        saveTabsToPrefs();
    }

    private View searchDockScroll;
    private View searchDockExpanded;
    private View searchDockCollapsed;
    private TextView searchDockUrl;
    private ImageButton navDockClose;
    private ImageButton navDockReload;
    private ImageButton navFinderBtn;
    private View navFinderBox;
    private EditText navFinderInput;
    private TextView navFinderCount;
    private ImageButton navArrowUpBtn;
    private ImageButton navArrowDownBtn;
    private TextView navDragHandle;
    private ImageButton navShrinkBtn;
    private float searchDockDX = 0f, searchDockDY = 0f;
    private boolean isGoogleDockEnabled = true;
    private boolean isGoogleDockScrollCollapseEnabled = true;
    private boolean isFinderModeActive = false;
    private boolean isTwoFingerDraggingSearch = false;
    private float currentGoogleDockScale = 1.0f;

    @SuppressLint("ClickableViewAccessibility")
    private void setupSearchDock() {
        SharedPreferences prefs = getSharedPreferences("CaspianMobilePrefs", MODE_PRIVATE);
        isGoogleDockEnabled = !"false".equalsIgnoreCase(prefs.getString("google_dock_enabled", "true"));
        isGoogleDockScrollCollapseEnabled = !"false".equalsIgnoreCase(prefs.getString("google_dock_autocollapse", "true"));

        searchNavContainer = findViewById(R.id.search_nav_container);
        searchDockScroll = findViewById(R.id.search_dock_scroll);
        searchDockExpanded = findViewById(R.id.search_dock_expanded);
        searchDockCollapsed = findViewById(R.id.search_dock_collapsed_btn);
        searchDockUrl = findViewById(R.id.search_dock_url);
        navDockClose = findViewById(R.id.nav_dock_close);
        navDockReload = findViewById(R.id.nav_dock_reload);
        navBackBtn = findViewById(R.id.nav_back_btn);
        navForwardBtn = findViewById(R.id.nav_forward_btn);
        navFinderBtn = findViewById(R.id.nav_finder_btn);
        navFinderBox = findViewById(R.id.nav_finder_box);
        navFinderInput = findViewById(R.id.nav_finder_input);
        navFinderCount = findViewById(R.id.nav_finder_count);
        navArrowUpBtn = findViewById(R.id.nav_arrow_up_btn);
        navArrowDownBtn = findViewById(R.id.nav_arrow_down_btn);
        navDragHandle = findViewById(R.id.nav_drag_handle);
        navShrinkBtn = findViewById(R.id.nav_shrink_btn);

        // Touch Absorption on Dock background to prevent webview pass-through
        if (searchDockExpanded != null) {
            searchDockExpanded.setOnTouchListener((v, event) -> true);
        }

        // 1. Close Button (Left Side)
        if (navDockClose != null) {
            navDockClose.setOnClickListener(v -> {
                playAssetSound("sfx/tap_button.mp3");
                toggleGoogleSearchDock(false);
            });
        }

        // 1.5 Reload Tab Button
        if (navDockReload != null) {
            navDockReload.setOnClickListener(v -> {
                playAssetSound("sfx/pop_click.mp3");
                TabItem activeTab = getActiveTab();
                if (activeTab != null && activeTab.webView != null) {
                    activeTab.webView.reload();
                    new CaspianBridge(MainActivity.this).showToast("🔄 Reloading Tab...");
                }
            });
        }

        // 2. Back Navigation
        if (navBackBtn != null) {
            navBackBtn.setOnClickListener(v -> {
                playAssetSound("sfx/pop_click.mp3");
                TabItem activeTab = getActiveTab();
                if (activeTab != null && activeTab.webView != null && activeTab.webView.canGoBack()) {
                    activeTab.webView.goBack();
                    new CaspianBridge(MainActivity.this).showToast("⬅️ Back");
                } else {
                    new CaspianBridge(MainActivity.this).showToast("Already at initial search page");
                }
            });
        }

        // 3. Forward Navigation
        if (navForwardBtn != null) {
            navForwardBtn.setOnClickListener(v -> {
                playAssetSound("sfx/pop_click.mp3");
                TabItem activeTab = getActiveTab();
                if (activeTab != null && activeTab.webView != null && activeTab.webView.canGoForward()) {
                    activeTab.webView.goForward();
                    new CaspianBridge(MainActivity.this).showToast("➡️ Forward");
                } else {
                    new CaspianBridge(MainActivity.this).showToast("No forward history");
                }
            });
        }

        // 3.5 URL Pill Click -> Focuses and opens Google Search Box on the active page
        if (searchDockUrl != null) {
            searchDockUrl.setOnClickListener(v -> {
                playAssetSound("sfx/pop_click.mp3");
                TabItem activeTab = getActiveTab();
                if (activeTab != null && activeTab.webView != null) {
                    activeTab.webView.requestFocus();
                    activeTab.webView.evaluateJavascript(
                        "(function() {" +
                        "  var el = document.querySelector('textarea[name=\"q\"], input[name=\"q\"], input[type=\"search\"], textarea[aria-label*=\"Search\"], input[aria-label*=\"Search\"], input[name=\"p\"], #sb_form_q');" +
                        "  if (el) {" +
                        "    el.scrollIntoView({behavior: 'smooth', block: 'center'});" +
                        "    el.focus();" +
                        "    el.click();" +
                        "    if (typeof el.select === 'function') el.select();" +
                        "  } else {" +
                        "    window.scrollTo({top: 0, behavior: 'smooth'});" +
                        "  }" +
                        "})();",
                        null
                    );
                    try {
                        android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.showSoftInput(activeTab.webView, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
                        }
                    } catch (Exception ignored) {}
                    new CaspianBridge(MainActivity.this).showToast("🔍 Focusing Search Box...");
                }
            });
        }

        // 4. Finder Toggle Button
        if (navFinderBtn != null) {
            navFinderBtn.setOnClickListener(v -> {
                playAssetSound("sfx/pop_click.mp3");
                toggleFinderMode(!isFinderModeActive);
            });
        }

        // 4.1 Finder Input live search
        if (navFinderInput != null) {
            navFinderInput.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    TabItem activeTab = getActiveTab();
                    if (activeTab != null && activeTab.webView != null) {
                        String query = s.toString().trim();
                        if (!query.isEmpty()) {
                            activeTab.webView.findAllAsync(query);
                        } else {
                            activeTab.webView.clearMatches();
                            if (navFinderCount != null) navFinderCount.setText("0/0");
                        }
                    }
                }
                @Override
                public void afterTextChanged(android.text.Editable s) {}
            });

            navFinderInput.setOnEditorActionListener((v, actionId, event) -> {
                TabItem activeTab = getActiveTab();
                if (activeTab != null && activeTab.webView != null) {
                    activeTab.webView.findNext(true);
                }
                return true;
            });
        }

        // 5. Up Arrow (Finder: Prev Match | Normal: Scroll to Top)
        if (navArrowUpBtn != null) {
            navArrowUpBtn.setOnClickListener(v -> {
                playAssetSound("sfx/pop_click.mp3");
                TabItem activeTab = getActiveTab();
                if (activeTab != null && activeTab.webView != null) {
                    if (isFinderModeActive) {
                        activeTab.webView.findNext(false);
                    } else {
                        activeTab.webView.evaluateJavascript("window.scrollTo({top: 0, behavior: 'smooth'});", null);
                        new CaspianBridge(MainActivity.this).showToast("⬆️ Top of page");
                    }
                }
            });
        }

        // 6. Down Arrow (Finder: Next Match | Normal: Scroll to Bottom)
        if (navArrowDownBtn != null) {
            navArrowDownBtn.setOnClickListener(v -> {
                playAssetSound("sfx/pop_click.mp3");
                TabItem activeTab = getActiveTab();
                if (activeTab != null && activeTab.webView != null) {
                    if (isFinderModeActive) {
                        activeTab.webView.findNext(true);
                    } else {
                        activeTab.webView.evaluateJavascript("window.scrollTo({top: document.body.scrollHeight, behavior: 'smooth'});", null);
                        new CaspianBridge(MainActivity.this).showToast("⬇️ Bottom of page");
                    }
                }
            });
        }

        // 7. Drag Handle (Single-Finger Move)
        if (navDragHandle != null && searchNavContainer != null) {
            navDragHandle.setOnTouchListener((view, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        searchDockDX = searchNavContainer.getX() - event.getRawX();
                        searchDockDY = searchNavContainer.getY() - event.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        searchNavContainer.setX(event.getRawX() + searchDockDX);
                        searchNavContainer.setY(event.getRawY() + searchDockDY);
                        return true;
                    default:
                        return false;
                }
            });
        }

        // 8. Shrink Button -> Ball
        if (navShrinkBtn != null) {
            navShrinkBtn.setOnClickListener(v -> {
                playAssetSound("sfx/tap_button.mp3");
                collapseSearchDock();
            });
        }

        // 9. Collapsed Ball (Touch & Drag / Click to Expand)
        if (searchDockCollapsed != null && searchNavContainer != null) {
            searchDockCollapsed.setOnTouchListener(new View.OnTouchListener() {
                private float startX, startY;
                private boolean isDragging = false;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            startX = event.getRawX();
                            startY = event.getRawY();
                            searchDockDX = searchNavContainer.getX() - event.getRawX();
                            searchDockDY = searchNavContainer.getY() - event.getRawY();
                            isDragging = false;
                            return true;
                        case MotionEvent.ACTION_MOVE:
                            if (Math.hypot(event.getRawX() - startX, event.getRawY() - startY) > 10) {
                                isDragging = true;
                                searchNavContainer.setX(event.getRawX() + searchDockDX);
                                searchNavContainer.setY(event.getRawY() + searchDockDY);
                            }
                            return true;
                        case MotionEvent.ACTION_UP:
                            if (!isDragging) {
                                playAssetSound("sfx/pop_click.mp3");
                                expandSearchDock();
                            }
                            return true;
                        default:
                            return false;
                    }
                }
            });
        }
    }

    private void toggleFinderMode(boolean active) {
        isFinderModeActive = active;
        runOnUiThread(() -> {
            if (active) {
                if (searchDockUrl != null) searchDockUrl.setVisibility(View.GONE);
                if (navFinderBox != null) navFinderBox.setVisibility(View.VISIBLE);
                if (navFinderBtn != null) navFinderBtn.setColorFilter(0xFFFFCC00);
                if (navFinderInput != null) {
                    navFinderInput.requestFocus();
                    android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) imm.showSoftInput(navFinderInput, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
                    String text = navFinderInput.getText().toString().trim();
                    TabItem tab = getActiveTab();
                    if (tab != null && tab.webView != null && !text.isEmpty()) {
                        tab.webView.findAllAsync(text);
                    }
                }
                new CaspianBridge(MainActivity.this).showToast("🔍 Finder Active (Use ⬆️/⬇️ to jump)");
            } else {
                if (navFinderBox != null) navFinderBox.setVisibility(View.GONE);
                if (searchDockUrl != null) searchDockUrl.setVisibility(View.VISIBLE);
                if (navFinderBtn != null) navFinderBtn.setColorFilter(0xFFFFFFFF);
                android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null && navFinderInput != null) imm.hideSoftInputFromWindow(navFinderInput.getWindowToken(), 0);
                TabItem tab = getActiveTab();
                if (tab != null && tab.webView != null) {
                    tab.webView.clearMatches();
                }
                if (navFinderCount != null) navFinderCount.setText("0/0");
            }
        });
    }

    public void collapseSearchDock() {
        runOnUiThread(() -> {
            View dockView = (searchDockScroll != null) ? searchDockScroll : searchDockExpanded;
            if (dockView != null && searchDockCollapsed != null) {
                dockView.setVisibility(View.GONE);
                searchDockCollapsed.setVisibility(View.VISIBLE);
                searchDockCollapsed.setAlpha(0f);
                searchDockCollapsed.setScaleX(0.7f);
                searchDockCollapsed.setScaleY(0.7f);
                searchDockCollapsed.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(200)
                    .start();
            }
        });
    }

    public void expandSearchDock() {
        runOnUiThread(() -> {
            View dockView = (searchDockScroll != null) ? searchDockScroll : searchDockExpanded;
            if (dockView != null && searchDockCollapsed != null) {
                searchDockCollapsed.setVisibility(View.GONE);
                dockView.setVisibility(View.VISIBLE);
                dockView.setAlpha(0f);
                dockView.setScaleX(0.85f);
                dockView.setScaleY(0.85f);
                dockView.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(200)
                    .start();
            }
        });
    }

    public void toggleGoogleSearchDock(boolean show) {
        isGoogleDockEnabled = show;
        SharedPreferences prefs = getSharedPreferences("CaspianMobilePrefs", MODE_PRIVATE);
        prefs.edit().putString("google_dock_enabled", show ? "true" : "false").apply();
        runOnUiThread(() -> {
            if (show) {
                expandSearchDock();
            }
            updateSearchNavVisibility();
            if (controlWebView != null) {
                controlWebView.evaluateJavascript("if (typeof window.syncGoogleDockState === 'function') { window.syncGoogleDockState(" + show + "); }", null);
            }
        });
    }

    public void setGoogleDockAutoCollapse(boolean enabled) {
        isGoogleDockScrollCollapseEnabled = enabled;
        SharedPreferences prefs = getSharedPreferences("CaspianMobilePrefs", MODE_PRIVATE);
        prefs.edit().putString("google_dock_autocollapse", enabled ? "true" : "false").apply();
    }

    public void updateSearchNavVisibility() {
        runOnUiThread(() -> {
            TabItem active = getActiveTab();
            boolean isSheetOpen = this.isSheetOpen;
            SharedPreferences prefs = getSharedPreferences("CaspianMobilePrefs", MODE_PRIVATE);
            isGoogleDockEnabled = !"false".equalsIgnoreCase(prefs.getString("google_dock_enabled", "true"));
            if (active != null && searchNavContainer != null) {
                String u = active.url != null ? active.url.toLowerCase() : "";
                String s = active.service != null ? active.service.toLowerCase() : "";
                boolean isGoogleSearch = (s.equals("google") || u.contains("google.com") || u.contains("google.co") || u.contains("bing.com") || u.contains("duckduckgo.com") || u.contains("search")) && !u.contains("gemini.google.com") && !u.contains("youtube.com") && !u.contains("youtu.be");
                boolean showPill = isGoogleDockEnabled && isGoogleSearch && !isSheetOpen;
                searchNavContainer.setVisibility(showPill ? View.VISIBLE : View.GONE);
                if (showPill) {
                    searchNavContainer.bringToFront();
                    if (searchDockUrl != null && active.url != null) {
                        try {
                            Uri uri = Uri.parse(active.url);
                            String host = uri.getHost();
                            if (host != null) {
                                searchDockUrl.setText(host.replace("www.", ""));
                            } else {
                                searchDockUrl.setText("google.com");
                            }
                        } catch (Exception e) {
                            searchDockUrl.setText("google.com");
                        }
                    }
                }
            }
        });
    }

    private View ytFloatingRemoteContainer;
    private View ytFloatingRemoteScroll;
    private View ytFloatingRemoteDock;
    private View ytFloatingRemoteBall;
    private TextView ytRemoteDragHandle;
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
    private ImageButton ytRemoteShrinkBtn;
    private ImageButton ytRemoteClose;
    private float ytRemoteDX = 0f, ytRemoteDY = 0f;
    private boolean isYtFloatingRemoteEnabled = true;
    private boolean isTwoFingerDragging = false;
    private Boolean lastYtPlayingState = null;
    private Boolean lastYtMutedState = null;
    private float currentActionBtnScale = 1.0f;
    private float currentYtPodScale = 1.0f;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int action = ev.getActionMasked();
        int pointerCount = ev.getPointerCount();

        // 1. Handle Active Two-Finger Drag Continuation
        if (isTwoFingerDragging) {
            if (action == MotionEvent.ACTION_MOVE) {
                if (ytFloatingRemoteContainer != null) {
                    ytFloatingRemoteContainer.setX(ev.getRawX() + ytRemoteDX);
                    ytFloatingRemoteContainer.setY(ev.getRawY() + ytRemoteDY);
                }
                return true;
            } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_POINTER_UP) {
                isTwoFingerDragging = false;
                return true;
            }
        }

        if (isTwoFingerDraggingSearch) {
            if (action == MotionEvent.ACTION_MOVE) {
                if (searchNavContainer != null) {
                    searchNavContainer.setX(ev.getRawX() + searchDockDX);
                    searchNavContainer.setY(ev.getRawY() + searchDockDY);
                }
                return true;
            } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_POINTER_UP) {
                isTwoFingerDraggingSearch = false;
                return true;
            }
        }

        // Fast path for all standard 1-finger touches (scrolling, clicking, typing) - zero overhead
        if (pointerCount < 2) {
            return super.dispatchTouchEvent(ev);
        }

        // 2. Multi-Touch Gesture Detection (only evaluated when pointerCount >= 2 on pointer down)
        if (action == MotionEvent.ACTION_POINTER_DOWN || action == MotionEvent.ACTION_DOWN) {
            float x = ev.getRawX();
            float y = ev.getRawY();

            if (ytFloatingRemoteContainer != null && ytFloatingRemoteContainer.getVisibility() == View.VISIBLE) {
                int[] loc = new int[2];
                ytFloatingRemoteContainer.getLocationOnScreen(loc);
                int w = ytFloatingRemoteContainer.getWidth();
                int h = ytFloatingRemoteContainer.getHeight();
                if (x >= loc[0] - 30 && x <= loc[0] + w + 30 && y >= loc[1] - 30 && y <= loc[1] + h + 30) {
                    isTwoFingerDragging = true;
                    ytRemoteDX = ytFloatingRemoteContainer.getX() - ev.getRawX();
                    ytRemoteDY = ytFloatingRemoteContainer.getY() - ev.getRawY();
                    return true;
                }
            }

            if (searchNavContainer != null && searchNavContainer.getVisibility() == View.VISIBLE) {
                int[] loc = new int[2];
                searchNavContainer.getLocationOnScreen(loc);
                int w = searchNavContainer.getWidth();
                int h = searchNavContainer.getHeight();
                if (x >= loc[0] - 30 && x <= loc[0] + w + 30 && y >= loc[1] - 30 && y <= loc[1] + h + 30) {
                    isTwoFingerDraggingSearch = true;
                    searchDockDX = searchNavContainer.getX() - ev.getRawX();
                    searchDockDY = searchNavContainer.getY() - ev.getRawY();
                    return true;
                }
            }
        }

        return super.dispatchTouchEvent(ev);
    }

    public boolean isYouTubeTab(TabItem tab) {
        if (tab == null) return false;
        String s = tab.service != null ? tab.service.toLowerCase() : "";
        String u = tab.url != null ? tab.url.toLowerCase() : "";
        return s.contains("youtube") || u.contains("youtube.com") || u.contains("youtu.be");
    }

    public void updateFloatingYTRemoteVisibility() {
        runOnUiThread(() -> {
            TabItem active = getActiveTab();
            boolean isYT = isYouTubeTab(active);
            SharedPreferences prefs = getSharedPreferences("CaspianMobilePrefs", MODE_PRIVATE);
            boolean isYtEngineEnabled = !"false".equalsIgnoreCase(prefs.getString("yt_engine_enabled", "true"));
            boolean show = isYtFloatingRemoteEnabled && isYT && isYtEngineEnabled && !isSheetOpen;
            if (ytFloatingRemoteContainer != null) {
                if (show) {
                    ytFloatingRemoteContainer.setVisibility(View.VISIBLE);
                    ytFloatingRemoteContainer.bringToFront();
                    if (active != null && active.webView != null) {
                        active.webView.evaluateJavascript(
                            "if (window.__CaspianYouTube && typeof window.__CaspianYouTube.notifyState === 'function') { window.__CaspianYouTube.notifyState(); }", null
                        );
                    }
                } else {
                    ytFloatingRemoteContainer.setVisibility(View.GONE);
                }
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupFloatingYouTubeRemote() {
        ytFloatingRemoteContainer = findViewById(R.id.yt_floating_remote_container);
        ytFloatingRemoteScroll = findViewById(R.id.yt_floating_remote_scroll);
        ytFloatingRemoteDock = findViewById(R.id.yt_floating_remote_dock);
        ytFloatingRemoteBall = findViewById(R.id.yt_floating_remote_ball);
        ytRemoteDragHandle = findViewById(R.id.yt_remote_drag_handle);
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
        ytRemoteShrinkBtn = findViewById(R.id.yt_remote_shrink_btn);
        ytRemoteClose = findViewById(R.id.yt_remote_close);

        // Touch Absorption on Dock to prevent touch pass-through into underlying YouTube WebView
        if (ytFloatingRemoteDock != null) {
            ytFloatingRemoteDock.setOnTouchListener((v, event) -> true);
        }
        if (ytFloatingRemoteScroll != null) {
            ytFloatingRemoteScroll.setOnTouchListener((v, event) -> false);
        }

        // Draggable Move via Drag Handle (Enlarged Hitbox)
        if (ytRemoteDragHandle != null && ytFloatingRemoteContainer != null) {
            ytRemoteDragHandle.setOnTouchListener((view, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        ytRemoteDX = ytFloatingRemoteContainer.getX() - event.getRawX();
                        ytRemoteDY = ytFloatingRemoteContainer.getY() - event.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        ytFloatingRemoteContainer.setX(event.getRawX() + ytRemoteDX);
                        ytFloatingRemoteContainer.setY(event.getRawY() + ytRemoteDY);
                        return true;
                    default:
                        return false;
                }
            });
        }

        // Draggable Move on Collapsed Ball
        if (ytFloatingRemoteBall != null && ytFloatingRemoteContainer != null) {
            ytFloatingRemoteBall.setOnTouchListener(new View.OnTouchListener() {
                private float startX, startY;
                private boolean isDragging = false;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            startX = event.getRawX();
                            startY = event.getRawY();
                            ytRemoteDX = ytFloatingRemoteContainer.getX() - event.getRawX();
                            ytRemoteDY = ytFloatingRemoteContainer.getY() - event.getRawY();
                            isDragging = false;
                            return true;
                        case MotionEvent.ACTION_MOVE:
                            if (Math.hypot(event.getRawX() - startX, event.getRawY() - startY) > 10) {
                                isDragging = true;
                                ytFloatingRemoteContainer.setX(event.getRawX() + ytRemoteDX);
                                ytFloatingRemoteContainer.setY(event.getRawY() + ytRemoteDY);
                            }
                            return true;
                        case MotionEvent.ACTION_UP:
                            if (!isDragging) {
                                playAssetSound("sfx/pop_click.mp3");
                                expandYTRemoteDock();
                            }
                            return true;
                        default:
                            return false;
                    }
                }
            });
        }

        // Shrink Button -> Ball
        if (ytRemoteShrinkBtn != null) {
            ytRemoteShrinkBtn.setOnClickListener(v -> {
                playAssetSound("sfx/tap_button.mp3");
                shrinkYTRemoteToBall();
            });
        }

        if (ytRemoteReload != null) {
            ytRemoteReload.setOnClickListener(v -> {
                playAssetSound("sfx/pop_click.mp3");
                TabItem activeTab = getActiveTab();
                if (activeTab != null && activeTab.webView != null) {
                    activeTab.webView.reload();
                    new CaspianBridge(MainActivity.this).showToast("🔄 Reloading YouTube Tab");
                }
            });
        }

        if (ytRemoteFullscreen != null) {
            ytRemoteFullscreen.setOnClickListener(v -> {
                playAssetSound("sfx/pop_click.mp3");
                new CaspianBridge(MainActivity.this).toggleFullscreenYouTube();
            });
        }

        if (ytRemotePrevVideo != null) {
            ytRemotePrevVideo.setOnClickListener(v -> {
                playAssetSound("sfx/pop_click.mp3");
                TabItem activeTab = getActiveTab();
                if (activeTab != null && activeTab.webView != null) {
                    activeTab.webView.evaluateJavascript(
                        "if (window.__CaspianYouTube && typeof window.__CaspianYouTube.previousVideo === 'function') { window.__CaspianYouTube.previousVideo(); } else if (window.history.length > 1) { window.history.back(); }", null
                    );
                }
            });
        }

        if (ytRemoteSeekBack != null) {
            ytRemoteSeekBack.setOnClickListener(v -> {
                playAssetSound("sfx/pop_click.mp3");
                new CaspianBridge(MainActivity.this).seekYouTube(-5);
            });
        }

        if (ytRemotePlayPause != null) {
            ytRemotePlayPause.setOnClickListener(v -> {
                playAssetSound("sfx/pop_click.mp3");
                new CaspianBridge(MainActivity.this).togglePlayYouTube();
            });
        }

        if (ytRemoteSeekFwd != null) {
            ytRemoteSeekFwd.setOnClickListener(v -> {
                playAssetSound("sfx/pop_click.mp3");
                new CaspianBridge(MainActivity.this).seekYouTube(5);
            });
        }

        if (ytRemoteNextVideo != null) {
            ytRemoteNextVideo.setOnClickListener(v -> {
                playAssetSound("sfx/pop_click.mp3");
                TabItem activeTab = getActiveTab();
                if (activeTab != null && activeTab.webView != null) {
                    activeTab.webView.evaluateJavascript(
                        "if (window.__CaspianYouTube && typeof window.__CaspianYouTube.nextVideo === 'function') { window.__CaspianYouTube.nextVideo(); } else if (window.history.length > 1) { window.history.forward(); }", null
                    );
                }
            });
        }

        if (ytRemoteMute != null) {
            ytRemoteMute.setOnClickListener(v -> {
                playAssetSound("sfx/pop_click.mp3");
                new CaspianBridge(MainActivity.this).toggleMuteYouTube();
            });
        }

        if (ytRemoteSpeedBtn != null) {
            ytRemoteSpeedBtn.setOnClickListener(v -> {
                playAssetSound("sfx/pop_click.mp3");
                showSpeedSelectionDialog();
            });
        }

        if (ytRemoteQualityBtn != null) {
            ytRemoteQualityBtn.setOnClickListener(v -> {
                playAssetSound("sfx/pop_click.mp3");
                showQualitySelectionDialog();
            });
        }

        if (ytRemoteClose != null) {
            ytRemoteClose.setOnClickListener(v -> {
                playAssetSound("sfx/tap_button.mp3");
                toggleFloatingYTRemote(false);
            });
        }
    }

    public void updateYouTubePodState(boolean isPlaying, boolean isMuted) {
        runOnUiThread(() -> {
            if (lastYtPlayingState == null || lastYtPlayingState != isPlaying) {
                lastYtPlayingState = isPlaying;
                if (ytRemotePlayPause != null) {
                    ytRemotePlayPause.setImageResource(isPlaying ? R.drawable.ic_pod_pause : R.drawable.ic_pod_play);
                    ytRemotePlayPause.setContentDescription(isPlaying ? "Pause Video" : "Play Video");
                }
            }
            if (lastYtMutedState == null || lastYtMutedState != isMuted) {
                lastYtMutedState = isMuted;
                if (ytRemoteMute != null) {
                    ytRemoteMute.setImageResource(isMuted ? R.drawable.ic_pod_mute : R.drawable.ic_pod_unmute);
                    ytRemoteMute.setContentDescription(isMuted ? "Unmute Video" : "Mute Video");
                }
            }
            if (ytRemoteFullscreen != null) {
                boolean isFs = customView != null;
                ytRemoteFullscreen.setImageResource(isFs ? R.drawable.ic_pod_fullscreen_exit : R.drawable.ic_pod_fullscreen);
            }
        });
    }

    private void showSpeedSelectionDialog() {
        String[] speedLabels = { "0.25x", "0.5x", "0.75x", "1.0x (Normal)", "1.25x", "1.5x", "1.75x", "2.0x", "2.5x", "3.0x", "4.0x" };
        float[] speedVals = { 0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f, 2.5f, 3.0f, 4.0f };

        new androidx.appcompat.app.AlertDialog.Builder(this, androidx.appcompat.R.style.Theme_AppCompat_Dialog_Alert)
            .setTitle("⚡ Select Playback Speed")
            .setItems(speedLabels, (dialog, which) -> {
                playAssetSound("sfx/pop_click.mp3");
                float selectedSpeed = speedVals[which];
                String text = (selectedSpeed == (int)selectedSpeed ? String.valueOf((int)selectedSpeed) : String.valueOf(selectedSpeed)) + "x";
                if (ytRemoteSpeedBtn != null) {
                    ytRemoteSpeedBtn.setText(text);
                }
                new CaspianBridge(MainActivity.this).setYouTubeSpeed(selectedSpeed);
                new CaspianBridge(MainActivity.this).showToast("⚡ Speed: " + text);
            })
            .show();
    }

    private void showQualitySelectionDialog() {
        String[] qualityLabels = { "Auto (Recommended)", "1080p (Full HD)", "720p (HD)", "480p (Standard)", "360p (Medium)", "240p (Data Saver)" };
        String[] qualityPillLabels = { "Auto", "1080p", "720p", "480p", "360p", "240p" };
        String[] qualityCodes = { "auto", "hd1080", "hd720", "large", "medium", "small" };

        new androidx.appcompat.app.AlertDialog.Builder(this, androidx.appcompat.R.style.Theme_AppCompat_Dialog_Alert)
            .setTitle("🎬 Select Video Quality")
            .setItems(qualityLabels, (dialog, which) -> {
                playAssetSound("sfx/pop_click.mp3");
                String qPill = qualityPillLabels[which];
                String qCode = qualityCodes[which];
                if (ytRemoteQualityBtn != null) {
                    ytRemoteQualityBtn.setText(qPill);
                }
                new CaspianBridge(MainActivity.this).setYouTubeQuality(qCode);
                new CaspianBridge(MainActivity.this).showToast("🎬 Quality: " + qPill);
            })
            .show();
    }

    public void applyWidgetScale(String type, float scale) {
        runOnUiThread(() -> {
            if ("action_button".equalsIgnoreCase(type)) {
                currentActionBtnScale = scale;
                if (floatingCaspianCard != null) {
                    floatingCaspianCard.setScaleX(scale);
                    floatingCaspianCard.setScaleY(scale);
                }
            } else if ("yt_pod".equalsIgnoreCase(type)) {
                currentYtPodScale = scale;
                if (ytFloatingRemoteContainer != null) {
                    ytFloatingRemoteContainer.setScaleX(scale);
                    ytFloatingRemoteContainer.setScaleY(scale);
                }
            } else if ("google_dock".equalsIgnoreCase(type)) {
                currentGoogleDockScale = scale;
                if (searchNavContainer != null) {
                    searchNavContainer.setScaleX(scale);
                    searchNavContainer.setScaleY(scale);
                }
            }
        });
    }

    private void shrinkYTRemoteToBall() {
        View dockView = (ytFloatingRemoteScroll != null) ? ytFloatingRemoteScroll : ytFloatingRemoteDock;
        if (dockView != null && ytFloatingRemoteBall != null) {
            dockView.setVisibility(View.GONE);
            ytFloatingRemoteBall.setVisibility(View.VISIBLE);
            ytFloatingRemoteBall.setAlpha(0f);
            ytFloatingRemoteBall.setScaleX(0.7f);
            ytFloatingRemoteBall.setScaleY(0.7f);
            ytFloatingRemoteBall.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(200)
                .start();
        }
    }

    private void expandYTRemoteDock() {
        View dockView = (ytFloatingRemoteScroll != null) ? ytFloatingRemoteScroll : ytFloatingRemoteDock;
        if (dockView != null && ytFloatingRemoteBall != null) {
            ytFloatingRemoteBall.setVisibility(View.GONE);
            dockView.setVisibility(View.VISIBLE);
            dockView.setAlpha(0f);
            dockView.setScaleX(0.85f);
            dockView.setScaleY(0.85f);
            dockView.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(200)
                .start();
        }
    }

    public void toggleFloatingYTRemote(boolean show) {
        isYtFloatingRemoteEnabled = show;
        runOnUiThread(() -> {
            if (show) {
                expandYTRemoteDock();
            }
            updateFloatingYTRemoteVisibility();
            if (controlWebView != null) {
                controlWebView.evaluateJavascript("if (typeof window.syncYtFloatPodState === 'function') { window.syncYtFloatPodState(" + show + "); }", null);
            }
        });
    }

    public void closeTab(int tabId) {
        if (tabsList.size() <= 1) {
            closeAllTabs();
            return;
        }
        TabItem toRemove = null;
        for (TabItem item : tabsList) {
            if (item.id == tabId) {
                toRemove = item;
                break;
            }
        }
        if (toRemove != null) {
            // Destroy previously cached closed tab
            if (lastClosedTab != null && lastClosedTab.webView != null) {
                try {
                    lastClosedTab.webView.destroy();
                } catch(Exception e){}
            }

            if (toRemove.webView != null) {
                webViewContainer.removeView(toRemove.webView);
            }
            lastClosedTab = toRemove;
            lastClosedTabWasActive = (activeTabId == tabId);
            tabsList.remove(toRemove);

            if (activeTabId == tabId) {
                TabItem last = tabsList.get(tabsList.size() - 1);
                switchTab(last.id, false);
            } else {
                saveTabsToPrefs();
            }
        }
    }

    public void restoreLastClosedTab() {
        if (lastClosedTab != null) {
            tabsList.add(lastClosedTab);
            
            // Re-attach its webview
            if (lastClosedTab.webView != null) {
                webViewContainer.addView(lastClosedTab.webView);
            }
            
            if (lastClosedTabWasActive) {
                switchTab(lastClosedTab.id, true);
            } else {
                saveTabsToPrefs();
            }
            
            lastClosedTab = null;
        }
    }

    private List<TabItem> lastClosedGroupTabs = new ArrayList<>();

    public void closeMultipleTabs(List<Integer> tabIds) {
        lastClosedGroupTabs.clear();
        for (int tabId : tabIds) {
            TabItem toRemove = null;
            for (TabItem item : tabsList) {
                if (item.id == tabId) {
                    toRemove = item;
                    break;
                }
            }
            if (toRemove != null) {
                if (toRemove.webView != null) {
                    webViewContainer.removeView(toRemove.webView);
                }
                lastClosedGroupTabs.add(toRemove);
                tabsList.remove(toRemove);
            }
        }
        if (!tabsList.isEmpty()) {
            switchTab(tabsList.get(tabsList.size() - 1).id, false);
        } else {
            createNewTab("hub");
        }
        saveTabsToPrefs();
    }

    public void restoreLastClosedGroupTabs() {
        if (!lastClosedGroupTabs.isEmpty()) {
            for (TabItem tab : lastClosedGroupTabs) {
                if (!tabsList.contains(tab)) {
                    tabsList.add(tab);
                    if (tab.webView != null) {
                        webViewContainer.addView(tab.webView);
                        tab.webView.setVisibility(View.GONE);
                    }
                }
            }
            lastClosedGroupTabs.clear();
            saveTabsToPrefs();
        } else if (lastClosedTab != null) {
            restoreLastClosedTab();
        }
    }

    public void setGroupTabsFavorite(List<Integer> tabIds, boolean isFav) {
        for (TabItem item : tabsList) {
            if (tabIds.contains(item.id)) {
                item.isFavorite = isFav;
            }
        }
        saveTabsToPrefs();
    }

    public void closeAllTabs() {
        List<TabItem> toKeep = new ArrayList<>();
        for (TabItem item : tabsList) {
            if (item.isFavorite) {
                toKeep.add(item);
            } else {
                if (item.webView != null) {
                    webViewContainer.removeView(item.webView);
                    item.webView.destroy();
                }
            }
        }
        tabsList.clear();
        if (!toKeep.isEmpty()) {
            tabsList.addAll(toKeep);
            switchTab(tabsList.get(0).id);
        } else {
            activeTabId = 1;
            nextTabId = 2;
            TabItem hubTab = new TabItem(1, "Caspian Hub", "file:///android_asset/launch_hub.html", "hub", null);
            createTabWebView(hubTab);
            tabsList.add(hubTab);
            switchTab(1);
        }
    }

    private boolean isSheetOpen = false;

    public void toggleControlSheet() {
        if (isSheetOpen) {
            closeControlSheet();
        } else {
            openControlSheet();
        }
    }

    public void openControlSheet() {
        isSheetOpen = true;
        sheetOverlayContainer.setVisibility(View.VISIBLE);
        sheetOverlayContainer.setClickable(true);
        sheetOverlayContainer.setFocusable(true);
        sheetOverlayContainer.setScaleX(1f);
        sheetOverlayContainer.setScaleY(1f);
        sheetOverlayContainer.setAlpha(1f);
        sheetOverlayContainer.setTranslationY(0f);

        if (searchNavContainer != null) {
            searchNavContainer.setVisibility(View.GONE);
        }
        updateFloatingYTRemoteVisibility();
        
        SharedPreferences prefs = getSharedPreferences("CaspianMobilePrefs", MODE_PRIVATE);
        int openDuration = 180;
        try {
            String openDurStr = prefs.getString("sheetOpenDuration", "180");
            openDuration = Integer.parseInt(openDurStr);
        } catch(Exception e) {}
        String animStyle = prefs.getString("sheetAnimationStyle", "genie");

        // 1. Smooth In-Place Backdrop Fade
        sheetBackdrop.animate().cancel();
        sheetBackdrop.animate()
                .alpha(1f)
                .setDuration(openDuration)
                .start();

        Runnable onOpenComplete = () -> {
            controlWebView.evaluateJavascript("if (typeof window.renderOpenTabs === 'function') { window.renderOpenTabs(); }", null);
            controlWebView.evaluateJavascript("if (typeof restoreSavedSettings === 'function') { restoreSavedSettings(); }", null);
        };

        // 2. Animate Control WebView independently (Never scale or translate the backdrop!)
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
            controlWebView.setScaleX(0.05f);
            controlWebView.setScaleY(0.05f);
            controlWebView.setAlpha(0f);
            controlWebView.setTranslationY(0f);
            
            controlWebView.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .alpha(1f)
                    .setDuration(openDuration)
                    .setInterpolator(new android.view.animation.DecelerateInterpolator(1.8f))
                    .withEndAction(onOpenComplete)
                    .start();
        } else {
            int height = sheetOverlayContainer.getHeight();
            if (height <= 0) {
                height = getResources().getDisplayMetrics().heightPixels;
            }
            controlWebView.setScaleX(1f);
            controlWebView.setScaleY(1f);
            controlWebView.setAlpha(1f);
            controlWebView.setTranslationY(height);
            controlWebView.animate()
                    .translationY(0)
                    .setDuration(openDuration)
                    .setInterpolator(new android.view.animation.PathInterpolator(0.2f, 0f, 0f, 1f))
                    .withEndAction(onOpenComplete)
                    .start();
        }
    }

    public void closeControlSheet() {
        isSheetOpen = false;
        SharedPreferences prefs = getSharedPreferences("CaspianMobilePrefs", MODE_PRIVATE);
        int closeDuration = 160;
        try {
            String closeDurStr = prefs.getString("sheetCloseDuration", "160");
            closeDuration = Integer.parseInt(closeDurStr);
        } catch(Exception e) {}
        String animStyle = prefs.getString("sheetAnimationStyle", "genie");

        // 1. Smooth In-Place Backdrop Fade Out
        sheetBackdrop.animate().cancel();
        sheetBackdrop.animate()
                .alpha(0f)
                .setDuration(closeDuration)
                .start();

        // 2. Animate Control WebView independently
        controlWebView.animate().cancel();
        if ("none".equalsIgnoreCase(animStyle) || closeDuration <= 0) {
            sheetBackdrop.setAlpha(0f);
            sheetOverlayContainer.setVisibility(View.INVISIBLE);
            sheetOverlayContainer.setClickable(false);
            sheetOverlayContainer.setFocusable(false);
            applyPrunerInMainWebView();
            updateFloatingYTRemoteVisibility();
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
                    .setInterpolator(new android.view.animation.PathInterpolator(0.3f, 0f, 0.8f, 0.15f))
                    .withEndAction(() -> {
                        if (!isSheetOpen) {
                            sheetOverlayContainer.setVisibility(View.INVISIBLE);
                            sheetOverlayContainer.setClickable(false);
                            sheetOverlayContainer.setFocusable(false);
                            applyPrunerInMainWebView();
                            updateSearchNavVisibility();
                            updateFloatingYTRemoteVisibility();
                        }
                    })
                    .start();
        } else {
            int height = sheetOverlayContainer.getHeight();
            if (height <= 0) {
                height = getResources().getDisplayMetrics().heightPixels;
            }
            controlWebView.animate()
                    .translationY(height)
                    .setDuration(closeDuration)
                    .setInterpolator(new android.view.animation.PathInterpolator(0.3f, 0f, 0.8f, 0.15f))
                    .withEndAction(() -> {
                        if (!isSheetOpen) {
                            sheetOverlayContainer.setVisibility(View.INVISIBLE);
                            sheetOverlayContainer.setClickable(false);
                            sheetOverlayContainer.setFocusable(false);
                            applyPrunerInMainWebView();
                            updateSearchNavVisibility();
                            updateFloatingYTRemoteVisibility();
                        }
                    })
                    .start();
        }
    }

    public void applyPrunerInMainWebView() {
        TabItem activeTab = getActiveTab();
        if (activeTab != null && activeTab.webView != null && !isPrinting) {
            injectPrunerScript(activeTab.webView);
        }
    }

    public void performExportOnMainWebView(String fmt) {
        TabItem activeTab = getActiveTab();
        if (activeTab == null || activeTab.webView == null) return;
        WebView mainWebView = activeTab.webView;

        String targetFormat = fmt;
        if ("nativepdf".equalsIgnoreCase(fmt)) {
            targetFormat = "styledpdf";
        }
        final String exportFmt = targetFormat;

        SharedPreferences prefs = getSharedPreferences("CaspianMobilePrefs", MODE_PRIVATE);
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

        String method = "sweeper"; // fallback default
        if ("chatgpt".equalsIgnoreCase(service)) {
            method = isTemp ? prefs.getString("export_chatgpt_temp", "fiber") : prefs.getString("export_chatgpt_normal", "api");
        } else if ("gemini".equalsIgnoreCase(service)) {
            method = isTemp ? prefs.getString("export_gemini_temp", "sweeper") : prefs.getString("export_gemini_normal", "sweeper");
        }

        // Perfect relative fetch API session method with deep markdown, LaTeX formulas, HTML tables, and Base64 images
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
                "  // RUN TARGET METHOD\n" +
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
                "    // Fallback/direct React Fiber state tree extraction\n" +
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
                "    // DOM Layout Sweeper & Deep Crawler for ChatGPT and Gemini\n" +
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
                new CaspianBridge(MainActivity.this).showToast("No chat turns found to export!");
                return;
            }

            JSONArray turnsArray = new JSONArray(jsonStr);
            if (turnsArray.length() == 0) {
                new CaspianBridge(MainActivity.this).showToast("No chat turns found to export!");
                return;
            }

            closeControlSheet();

            String title = "AI Conversation";
            String dateStr = new java.util.Date().toLocaleString();
            String safeTitle = title.replaceAll("[^a-zA-Z0-9_-]", "_");

            if ("md".equalsIgnoreCase(exportFmt)) {
                StringBuilder sb = new StringBuilder("# " + title + "\n\n*Exported via Caspian Mobile on " + dateStr + "*\n\n---\n\n");
                for (int i = 0; i < turnsArray.length(); i++) {
                    JSONObject obj = turnsArray.getJSONObject(i);
                    sb.append("### Turn ").append(obj.getInt("index")).append("\n\n").append(obj.getString("text")).append("\n\n---\n\n");
                }
                saveAndDownloadFile(safeTitle + "_Caspian_Exported.md", sb.toString(), "text/markdown");

            } else if ("txt".equalsIgnoreCase(exportFmt)) {
                StringBuilder sb = new StringBuilder("======================================\n" + title.toUpperCase() + "\nExported via Caspian Mobile on " + dateStr + "\n======================================\n\n");
                for (int i = 0; i < turnsArray.length(); i++) {
                    JSONObject obj = turnsArray.getJSONObject(i);
                    sb.append("[TURN ").append(obj.getInt("index")).append("]\n").append(obj.getString("text")).append("\n\n--------------------------------------\n\n");
                }
                saveAndDownloadFile(safeTitle + "_Caspian_Exported.txt", sb.toString(), "text/plain");

            } else if ("doc".equalsIgnoreCase(exportFmt)) {
                StringBuilder sb = new StringBuilder("<html><body><h1>" + title + "</h1><p>Exported via Caspian Mobile on " + dateStr + "</p>");
                for (int i = 0; i < turnsArray.length(); i++) {
                    JSONObject obj = turnsArray.getJSONObject(i);
                    sb.append("<h3>Turn ").append(obj.getInt("index")).append("</h3><div>").append(obj.getString("html")).append("</div><hr>");
                }
                sb.append("</body></html>");
                saveAndDownloadFile(safeTitle + "_Caspian_Exported.doc", sb.toString(), "application/msword");

            } else if ("styledpdf".equalsIgnoreCase(exportFmt)) {
                // 1:1 Ditto ChatGPT CSS & HTML Template Engine with KaTeX Math & JetBrains Mono Code Styling
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
                            .append("<span style=\"font-size: 10px; color: #8e8ea0;\">Caspian Mobile</span>")
                            .append("</div>")
                            .append("<div class=\"chat-turn-content\" style=\"font-size: 14px; line-height: 1.7; color: #353740; font-family: 'Söhne', 'Inter', -apple-system, sans-serif;\">").append(html).append("</div></div>");
                }

                String badgeTitle = "gemini".equalsIgnoreCase(detectionService) ? "Caspian Mobile (Gemini)" : "Caspian Mobile (ChatGPT)";

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
                        "<div class=\"doc-header\"><div><h1>" + title + "</h1><div class=\"doc-meta\">Exported via Caspian Mobile &bull; " + dateStr + "</div></div>" +
                        "<div style=\"font-size: 12px; font-weight: 700; color: #1B4264; border: 1px solid #1B4264; padding: 4px 10px; border-radius: 6px;\">" + badgeTitle + "</div></div>" +
                        turnsHtml.toString() + "</body></html>";

                // Save HTML to Download/Caspian/html/
                saveAndDownloadFile(safeTitle + "_ChatGPT_Export.html", fullHtml, "text/html");

                // Print HTML via WebView Print Adapter
                new CaspianBridge(MainActivity.this).printHtml("Caspian_ChatGPT_Document", fullHtml);

            } else if ("convert".equalsIgnoreCase(exportFmt) || "copy".equalsIgnoreCase(exportFmt)) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < turnsArray.length(); i++) {
                    JSONObject obj = turnsArray.getJSONObject(i);
                    sb.append("[Turn ").append(obj.getInt("index")).append("]\n").append(obj.getString("text")).append("\n\n");
                }
                new CaspianBridge(MainActivity.this).copyToClipboard(sb.toString());

                if ("convert".equalsIgnoreCase(exportFmt)) {
                    String targetService = "chatgpt";
                    if (turnsArray.length() > 0) {
                        targetService = turnsArray.getJSONObject(0).optString("service", "chatgpt");
                    }
                    createNewTabWithPrefill(targetService, sb.toString());
                    new CaspianBridge(MainActivity.this).showToast("Copied to clipboard and opened in a new tab! Feel free to paste if auto-fill doesn't trigger.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            new CaspianBridge(MainActivity.this).showToast("Export failed: " + e.getLocalizedMessage());
        }
    }

    private void saveAndDownloadFile(String fileName, String content, String mimeType) {
        new CaspianBridge(this).downloadFile(fileName, content, mimeType);
    }

    private boolean isSupportedUrl(String url) {
        if (url == null) return false;
        return url.contains("chatgpt.com") || url.contains("gemini.google.com") || url.contains("gemini") || url.contains("google.com/app");
    }

    public void pauseInjectionTimer() {
        this.isPrinting = true;
    }

    public void resumeInjectionTimerLater() {
        TabItem activeTab = getActiveTab();
        if (activeTab != null && activeTab.webView != null) {
            activeTab.webView.postDelayed(() -> this.isPrinting = false, 8000);
        }
    }

    public WebView getWebView() {
        TabItem activeTab = getActiveTab();
        return activeTab != null ? activeTab.webView : null;
    }

    public void loadUrl(String url) {
        closeControlSheet();
        TabItem activeTab = getActiveTab();
        if (activeTab != null && activeTab.webView != null) {
            activeTab.webView.loadUrl(url);
        }
    }

    public void loadUrlWithPrefill(String url, String promptContext) {
        closeControlSheet();
        this.pendingPrefill = promptContext;
        TabItem activeTab = getActiveTab();
        if (activeTab != null && activeTab.webView != null) {
            activeTab.webView.loadUrl(url);
        }
    }

    private void injectPrunerScript(WebView view) {
        if (view == null || isPrinting) return;
        try {
            String prunerJs = readAssetFile("mobile_pruner.js");
            String prefillJs = "";
            if (pendingPrefill != null && !pendingPrefill.isEmpty()) {
                prefillJs = "\n(function() {\n" +
                        "  var textToPaste = " + JSONObject.quote(pendingPrefill) + ";\n" +
                        "  var attempts = 0;\n" +
                        "  function tryPrefill() {\n" +
                        "    var ta = document.querySelector('#prompt-textarea, textarea, div[contenteditable=\"true\"], .input-area, .textarea');\n" +
                        "    if (ta) {\n" +
                        "      ta.focus();\n" +
                        "      if (ta.tagName === 'TEXTAREA') {\n" +
                        "        ta.value = textToPaste;\n" +
                        "        ta.dispatchEvent(new Event('input', { bubbles: true }));\n" +
                        "      } else {\n" +
                        "        try {\n" +
                        "          document.execCommand('insertText', false, textToPaste);\n" +
                        "        } catch(err) {\n" +
                        "          ta.innerText = textToPaste;\n" +
                        "        }\n" +
                        "        ta.dispatchEvent(new Event('input', { bubbles: true }));\n" +
                        "      }\n" +
                        "    } else if (attempts < 20) {\n" +
                        "      attempts++;\n" +
                        "      setTimeout(tryPrefill, 500);\n" +
                        "    }\n" +
                        "  }\n" +
                        "  tryPrefill();\n" +
                        "})();\n";
                pendingPrefill = null;
            }

            String ytJs = "";
            String curUrl = view.getUrl();
            if (curUrl != null && curUrl.toLowerCase().contains("youtube.com")) {
                try {
                    ytJs = "\n" + readAssetFile("youtube_helper.js");
                } catch(Exception e) {}
            }

            view.evaluateJavascript(prunerJs + prefillJs + ytJs, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final java.util.Map<String, String> assetScriptCache = new java.util.concurrent.ConcurrentHashMap<>();

    private String readAssetFile(String fileName) throws Exception {
        String cached = assetScriptCache.get(fileName);
        if (cached != null) return cached;

        InputStream is = getAssets().open(fileName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
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
    }

    @Override
    public void onBackPressed() {
        if (customView != null) {
            hideCustomView();
            return;
        }
        if (isSheetOpen) {
            closeControlSheet();
            return;
        }
        TabItem activeTab = getActiveTab();
        if (activeTab != null && activeTab.webView != null) {
            String curUrl = activeTab.webView.getUrl();
            if (curUrl != null && (curUrl.contains("accounts.google.com") || curUrl.contains("auth.openai.com/api/accounts"))) {
                appendDebugLog("BACK_PRUNE_OAUTH", "Prevented back-navigation history trap on OAuth page. Returning directly to login.");
                activeTab.webView.loadUrl("https://chatgpt.com/");
                return;
            }
            if (activeTab.webView.canGoBack()) {
                activeTab.webView.goBack();
            } else {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }

    // File Chooser, Permissions & Active Repaint Invalidation Methods
    private boolean checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
            }, PERMISSION_REQUEST_CODE);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MIC_PERMISSION_REQUEST_CODE) {
            boolean granted = true;
            for (int r : grantResults) {
                if (r != PackageManager.PERMISSION_GRANTED) {
                    granted = false;
                    break;
                }
            }
            if (pendingWebPermissionRequest != null) {
                if (granted) {
                    pendingWebPermissionRequest.grant(pendingWebPermissionRequest.getResources());
                } else {
                    pendingWebPermissionRequest.deny();
                }
                pendingWebPermissionRequest = null;
            }
        } else if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean granted = true;
            for (int r : grantResults) {
                if (r != PackageManager.PERMISSION_GRANTED) {
                    granted = false;
                    break;
                }
            }
            if (granted) {
                openFileChooser();
            } else {
                new CaspianBridge(MainActivity.this).showToast("Permissions are required to upload files.");
                if (uploadMessage != null) {
                    uploadMessage.onReceiveValue(null);
                    uploadMessage = null;
                }
            }
        }
    }

    private void openFileChooser() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        java.io.File photoFile = null;
        try {
            photoFile = createTempImageFile();
        } catch (java.io.IOException ex) {
            ex.printStackTrace();
        }
        if (photoFile != null) {
            cameraImageUri = androidx.core.content.FileProvider.getUriForFile(this,
                    getApplicationContext().getPackageName() + ".fileprovider", photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
            takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            try {
                List<android.content.pm.ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);
                for (android.content.pm.ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    grantUriPermission(packageName, cameraImageUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
            } catch (Exception e) {}
        }

        Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
        contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
        contentSelectionIntent.setType("*/*");

        Intent[] intentArray;
        if (takePictureIntent.resolveActivity(getPackageManager()) != null && cameraImageUri != null) {
            intentArray = new Intent[]{takePictureIntent};
        } else {
            intentArray = new Intent[0];
        }

        Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
        chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
        chooserIntent.putExtra(Intent.EXTRA_TITLE, "Select File or Take Photo");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);

        startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);
    }

    private java.io.File createTempImageFile() throws java.io.IOException {
        String timeStamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(new java.util.Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        java.io.File storageDir = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES);
        return java.io.File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILECHOOSER_RESULTCODE) {
            if (uploadMessage == null) return;
            Uri[] results = null;
            if (resultCode == RESULT_OK) {
                if (data == null || data.getData() == null) {
                    if (cameraImageUri != null) {
                        results = new Uri[]{cameraImageUri};
                    }
                } else {
                    String dataString = data.getDataString();
                    if (dataString != null) {
                        results = new Uri[]{Uri.parse(dataString)};
                    }
                }
            }
            uploadMessage.onReceiveValue(results);
            uploadMessage = null;
        }
    }

    public void updateRefreshTimer() {
        refreshHandler.removeCallbacks(refreshRunnable);
        SharedPreferences prefs = getSharedPreferences("CaspianMobilePrefs", MODE_PRIVATE);
        int rate = 100;
        try {
            rate = Integer.parseInt(prefs.getString("active_refresh_rate", "100"));
        } catch (Exception e) {}
        if (rate > 0) {
            refreshHandler.post(refreshRunnable);
        }
    }

    public void playAssetSound(String assetPath) {
        try {
            SharedPreferences prefs = getSharedPreferences("CaspianMobilePrefs", MODE_PRIVATE);
            String masterMuteStr = prefs.getString("master_sfx_muted", "false");
            if ("true".equalsIgnoreCase(masterMuteStr)) return;

            float volume = 0.5f;
            try {
                String volStr = prefs.getString("sfx_volume", "0.5");
                volume = Float.parseFloat(volStr);
            } catch (Exception e) {}

            if (volume <= 0.001f) return;

            // UI Master Attenuation: Scale 0..1 slider to a comfortable, gentle 0..0.40f ambient gain
            float effectiveVolume = Math.max(0.001f, Math.min(1.0f, volume)) * 0.40f;

            if (soundPool == null) {
                initSoundPool();
            }

            if (soundPool != null) {
                Integer soundId = soundIdMap.get(assetPath);
                if (soundId != null && soundId > 0) {
                    soundPool.play(soundId, effectiveVolume, effectiveVolume, 1, 0, 1.0f);
                } else {
                    android.content.res.AssetFileDescriptor afd = getAssets().openFd(assetPath);
                    int newSoundId = soundPool.load(afd, 1);
                    afd.close();
                    soundIdMap.put(assetPath, newSoundId);
                    soundPool.setOnLoadCompleteListener((sp, sampleId, status) -> {
                        if (status == 0) {
                            sp.play(sampleId, effectiveVolume, effectiveVolume, 1, 0, 1.0f);
                        }
                    });
                }
            }
        } catch (Exception ignored) {}
    }
}
