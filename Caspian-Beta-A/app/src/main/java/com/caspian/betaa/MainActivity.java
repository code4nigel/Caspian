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
    private android.widget.VideoView splashVideoView;

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
            updateRefreshTimer();
            setupSplashScreen();
        } catch (Throwable t) {
            Log.e("CaspianDebugA", "Error during onCreate startup: " + t.getMessage(), t);
        }
    }

    private void setupSplashScreen() {
        splashOverlay = findViewById(R.id.splash_overlay);
        splashVideoView = findViewById(R.id.splash_videoview);
        if (splashOverlay != null && splashVideoView != null) {
            try {
                Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.caspian_splash_v22);
                splashVideoView.setVideoURI(videoUri);
                Runnable dismissSplash = new Runnable() {
                    @Override
                    public void run() {
                        if (splashOverlay != null && splashOverlay.getVisibility() == View.VISIBLE) {
                            splashOverlay.animate()
                                    .alpha(0f)
                                    .setDuration(300)
                                    .withEndAction(() -> {
                                        splashOverlay.setVisibility(View.GONE);
                                        if (splashVideoView != null) {
                                            try { splashVideoView.stopPlayback(); } catch (Exception e) {}
                                        }
                                    })
                                    .start();
                        }
                    }
                };

                // Center Crop Math to Cover 100% Display Edge-to-Edge with Zero Black Bars
                splashVideoView.setOnPreparedListener(mp -> {
                    try {
                        mp.setLooping(false);
                        int videoWidth = mp.getVideoWidth();
                        int videoHeight = mp.getVideoHeight();
                        if (videoWidth > 0 && videoHeight > 0) {
                            float videoAspect = (float) videoWidth / (float) videoHeight;
                            int screenWidth = splashOverlay.getWidth();
                            int screenHeight = splashOverlay.getHeight();
                            if (screenWidth == 0 || screenHeight == 0) {
                                android.util.DisplayMetrics metrics = getResources().getDisplayMetrics();
                                screenWidth = metrics.widthPixels;
                                screenHeight = metrics.heightPixels;
                            }
                            float screenAspect = (float) screenWidth / (float) screenHeight;
                            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) splashVideoView.getLayoutParams();
                            if (videoAspect > screenAspect) {
                                lp.width = (int) (screenHeight * videoAspect);
                                lp.height = screenHeight;
                            } else {
                                lp.width = screenWidth;
                                lp.height = (int) (screenWidth / videoAspect);
                            }
                            lp.gravity = android.view.Gravity.CENTER;
                            splashVideoView.setLayoutParams(lp);
                        }
                    } catch (Exception e) {}
                });

                splashVideoView.setOnCompletionListener(mp -> dismissSplash.run());
                splashVideoView.setOnErrorListener((mp, what, extra) -> {
                    dismissSplash.run();
                    return true;
                });
                splashOverlay.setOnClickListener(v -> dismissSplash.run());
                splashVideoView.start();

                // Timeout fallback after 4.5s
                splashOverlay.postDelayed(dismissSplash, 4500);
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
                            url.contains("youtube.com/api/stats/ads") ||
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            wv.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                if (scrollY - oldScrollY > 15) {
                    collapseSearchDock();
                } else if (oldScrollY - scrollY > 15 || scrollY <= 10) {
                    expandSearchDock();
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
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        
        controlWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        controlWebView.setBackgroundColor(0);
        controlWebView.addJavascriptInterface(new CaspianBridge(this), "CaspianBridge");

        controlWebView.setWebViewClient(new WebViewClient() {
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
                    String chosenTaSfx = prefs.getString("sfx_file_ta", "pop_click.wav");
                    playAssetSound("sfx/" + chosenTaSfx);
                    int tapDurationVal = 100;
                    try {
                        String tapDurStr = prefs.getString("theme_button_tap_duration", "100");
                        tapDurationVal = Integer.parseInt(tapDurStr);
                    } catch(Exception e) {}

                    final int finalTapDur = tapDurationVal;
                    if (finalTapDur > 0) {
                        floatingCaspianCard.animate()
                                .scaleX(0.88f)
                                .scaleY(0.88f)
                                .setDuration(finalTapDur)
                                .withEndAction(() -> {
                                    floatingCaspianCard.animate()
                                            .scaleX(1.0f)
                                            .scaleY(1.0f)
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
        saveTabsToPrefs();
    }

    private View searchDockExpanded;
    private View searchDockCollapsed;
    private TextView searchDockUrl;
    private float searchDockDX = 0f, searchDockDY = 0f;

    @SuppressLint("ClickableViewAccessibility")
    private void setupSearchDock() {
        searchNavContainer = findViewById(R.id.search_nav_container);
        searchDockExpanded = findViewById(R.id.search_dock_expanded);
        searchDockCollapsed = findViewById(R.id.search_dock_collapsed_btn);
        searchDockUrl = findViewById(R.id.search_dock_url);
        navBackBtn = findViewById(R.id.nav_back_btn);
        navForwardBtn = findViewById(R.id.nav_forward_btn);

        if (searchDockUrl != null && searchNavContainer != null) {
            searchDockUrl.setOnTouchListener((view, event) -> {
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

        if (searchDockCollapsed != null) {
            searchDockCollapsed.setOnClickListener(v -> expandSearchDock());
        }

        if (navBackBtn != null) {
            navBackBtn.setOnClickListener(v -> {
                TabItem activeTab = getActiveTab();
                if (activeTab != null && activeTab.webView != null && activeTab.webView.canGoBack()) {
                    activeTab.webView.goBack();
                    new CaspianBridge(MainActivity.this).showToast("⬅️ Back");
                } else {
                    new CaspianBridge(MainActivity.this).showToast("Already at initial search page");
                }
            });
        }

        if (navForwardBtn != null) {
            navForwardBtn.setOnClickListener(v -> {
                TabItem activeTab = getActiveTab();
                if (activeTab != null && activeTab.webView != null && activeTab.webView.canGoForward()) {
                    activeTab.webView.goForward();
                    new CaspianBridge(MainActivity.this).showToast("➡️ Forward");
                } else {
                    new CaspianBridge(MainActivity.this).showToast("No forward history");
                }
            });
        }
    }

    public void collapseSearchDock() {
        runOnUiThread(() -> {
            if (searchDockExpanded != null && searchDockCollapsed != null && searchDockExpanded.getVisibility() == View.VISIBLE) {
                searchDockExpanded.setVisibility(View.GONE);
                searchDockCollapsed.setVisibility(View.VISIBLE);
            }
        });
    }

    public void expandSearchDock() {
        runOnUiThread(() -> {
            if (searchDockExpanded != null && searchDockCollapsed != null && searchDockCollapsed.getVisibility() == View.VISIBLE) {
                searchDockCollapsed.setVisibility(View.GONE);
                searchDockExpanded.setVisibility(View.VISIBLE);
            }
        });
    }

    public void updateSearchNavVisibility() {
        runOnUiThread(() -> {
            TabItem active = getActiveTab();
            boolean isSheetOpen = this.isSheetOpen;
            if (active != null && searchNavContainer != null) {
                String u = active.url != null ? active.url.toLowerCase() : "";
                String s = active.service != null ? active.service.toLowerCase() : "";
                boolean isGoogleSearch = (s.equals("google") || u.contains("google.com/search") || u.contains("google.co") || u.contains("www.google.")) && !u.contains("gemini.google.com");
                boolean showPill = isGoogleSearch && !isSheetOpen;
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
                "    // DOM Layout Sweeper for both ChatGPT and Gemini\n" +
                "    try {\n" +
                "      if (activeService === 'gemini') {\n" +
                "        var elements = Array.from(document.querySelectorAll('.query-content, .user-query, div.query-text, .model-response, .model-reply, .reply-text-container'));\n" +
                "        for (var i = 0; i < elements.length; i++) {\n" +
                "          var el = elements[i];\n" +
                "          var isUser = el.classList.contains('query-content') || el.classList.contains('user-query') || el.classList.contains('query-text') || el.querySelector('.user-query');\n" +
                "          var text = el.innerText.trim();\n" +
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

    private String readAssetFile(String fileName) throws Exception {
        InputStream is = getAssets().open(fileName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        is.close();
        return sb.toString();
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
            
            String enabledStr = prefs.getString("sfx_enabled_ta", "true");
            boolean enabled = !"false".equalsIgnoreCase(enabledStr);
            if (!enabled) return;

            float effectiveVolume = (float) Math.pow(volume, 2.5);

            android.media.MediaPlayer mp = new android.media.MediaPlayer();
            android.content.res.AssetFileDescriptor afd = getAssets().openFd(assetPath);
            mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
            mp.setVolume(effectiveVolume, effectiveVolume);
            mp.prepare();
            mp.setOnCompletionListener(android.media.MediaPlayer::release);
            mp.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
