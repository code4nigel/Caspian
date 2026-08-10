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
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

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

    // Debug Log Recording Variables
    private boolean isDebugRecording = false;
    private final StringBuilder debugLogBuffer = new StringBuilder();

    // Touch Drag Variables for Native Wave Button
    private float dX, dY;
    private float startRawX, startRawY;
    private boolean isDragging = false;

    private static final String DEFAULT_CHROME_UA = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36";

    @SuppressLint({"SetJavaScriptEnabled", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Maximum Window-Level GPU Hardware Acceleration for 60fps/120fps smooth scrolling
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        );

        setContentView(R.layout.activity_main);

        webViewContainer = findViewById(R.id.webview_container);
        controlWebView = findViewById(R.id.control_webview);
        sheetOverlayContainer = findViewById(R.id.sheet_overlay_container);
        sheetBackdrop = findViewById(R.id.sheet_backdrop);
        floatingCaspianCard = findViewById(R.id.floating_caspian_card);

        // Persistent Cookie Sync across all tabs and Google / ChatGPT OAuth
        CookieManager.getInstance().setAcceptCookie(true);

        setupControlWebView();
        setupNativeFloatingButton();
        setupSmartKeyboardAvoidance();

        sheetBackdrop.setOnClickListener(v -> closeControlSheet());

        // Load Persistent Tabs from Preferences
        loadTabsFromPrefs();

        // Load Saved Floating Theme on startup
        SharedPreferences prefs = getSharedPreferences("CaspianMobilePrefs", MODE_PRIVATE);
        String startColor = prefs.getString("theme_start_color", "#A2A9A9");
        String endColor = prefs.getString("theme_end_color", "#1B4264");
        String iconShape = prefs.getString("theme_icon_shape", "circle");
        applyFloatingTheme(startColor, endColor, iconShape);
    }

    @Override
    protected void onResume() {
        super.onResume();
        CookieManager.getInstance().flush();
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

    private TabItem getActiveTab() {
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
            if (jsonStr != null) {
                JSONArray arr = new JSONArray(jsonStr);
                if (arr.length() > 0) {
                    tabsList.clear();
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        int id = obj.getInt("id");
                        String title = obj.getString("title");
                        String url = obj.getString("url");
                        String service = obj.getString("service");
                        String nickname = obj.optString("nickname", "");

                        TabItem tab = new TabItem(id, title, url, service, null);
                        tab.nickname = nickname;
                        createTabWebView(tab);
                        tabsList.add(tab);
                        if (id >= nextTabId) nextTabId = id + 1;
                    }
                    switchTab(savedActiveId);
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Default initial Tab
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
                tab.url = url;
                String t = view.getTitle();
                if (t != null && !t.isEmpty() && !t.startsWith("file://")) {
                    tab.title = t;
                }
                saveTabsToPrefs();
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
            }
        });

        // OAuth Multi-Window Transport: Dedicated temporary popup WebView for Google & Apple logins!
        wv.setWebChromeClient(new WebChromeClient() {
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

        webViewContainer.addView(wv);
        tab.webView = wv;
        wv.loadUrl(tab.url);
        return wv;
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupControlWebView() {
        controlWebView.getSettings().setJavaScriptEnabled(true);
        controlWebView.getSettings().setDomStorageEnabled(true);
        controlWebView.getSettings().setAllowFileAccess(true);
        controlWebView.setBackgroundColor(0);
        controlWebView.addJavascriptInterface(new CaspianBridge(this), "CaspianBridge");

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
                    return true;

                case MotionEvent.ACTION_MOVE:
                    float deltaX = Math.abs(event.getRawX() - startRawX);
                    float deltaY = Math.abs(event.getRawY() - startRawY);

                    if (deltaX > 10 || deltaY > 10) {
                        isDragging = true;
                        float newX = event.getRawX() + dX;
                        float newY = event.getRawY() + dY;

                        View parent = (View) view.getParent();
                        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
                        int statusBarHeight = resourceId > 0 ? getResources().getDimensionPixelSize(resourceId) : 40;

                        newX = Math.max(0, Math.min(parent.getWidth() - view.getWidth(), newX));
                        // Hard top ceiling boundary so card NEVER goes out of screen at top!
                        newY = Math.max(statusBarHeight + 10, Math.min(parent.getHeight() - view.getHeight(), newY));

                        view.animate()
                                .x(newX)
                                .y(newY)
                                .setDuration(0)
                                .start();
                    }
                    return true;

                case MotionEvent.ACTION_UP:
                    if (!isDragging) {
                        toggleControlSheet();
                    }
                    return true;

                default:
                    return false;
            }
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
        activeTabId = tabId;
        for (TabItem item : tabsList) {
            if (item.webView != null) {
                if (item.id == tabId) {
                    item.webView.setVisibility(View.VISIBLE);
                    item.webView.onResume();
                    item.webView.resumeTimers();
                    item.webView.bringToFront();
                    item.webView.requestFocus();
                } else {
                    item.webView.setVisibility(View.GONE);
                    item.webView.onPause();
                    item.webView.pauseTimers();
                }
            }
        }
        if (closeSheet) {
            closeControlSheet();
        }
        saveTabsToPrefs();
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
            saveTabsToPrefs();
            switchTab(lastClosedTab.id);
            
            // Re-attach its webview
            if (lastClosedTab.webView != null) {
                webViewContainer.addView(lastClosedTab.webView);
            }
            
            lastClosedTab = null;
        }
    }

    public void closeAllTabs() {
        for (TabItem item : tabsList) {
            if (item.webView != null) {
                webViewContainer.removeView(item.webView);
                item.webView.destroy();
            }
        }
        tabsList.clear();
        activeTabId = 1;
        nextTabId = 2;

        TabItem hubTab = new TabItem(1, "Caspian Hub", "file:///android_asset/launch_hub.html", "hub", null);
        createTabWebView(hubTab);
        tabsList.add(hubTab);
        switchTab(1);
    }

    public void toggleControlSheet() {
        if (sheetOverlayContainer.getVisibility() == View.VISIBLE) {
            closeControlSheet();
        } else {
            openControlSheet();
        }
    }

    public void openControlSheet() {
        sheetOverlayContainer.setVisibility(View.VISIBLE);
        controlWebView.evaluateJavascript("if (typeof restoreSavedSettings === 'function') { restoreSavedSettings(); }", null);
    }

    public void closeControlSheet() {
        sheetOverlayContainer.setVisibility(View.GONE);
        applyPrunerInMainWebView();
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
                "        var turnDivs = Array.from(document.querySelectorAll('[data-testid^=\"conversation-turn-\"], div.w-full.text-token-text-primary'));\n" +
                "        for (var i = 0; i < turnDivs.length; i++) {\n" +
                "          var row = turnDivs[i];\n" +
                "          var text = '';\n" +
                "          var isUser = false;\n" +
                "          if (row.querySelector('[data-testid=\"user-turn\"], div[data-message-author-role=\"user\"]') || row.querySelector('div.bg-token-main-surface-secondary') || row.innerText.includes('User Prompt')) {\n" +
                "            isUser = true;\n" +
                "          }\n" +
                "          var markdownDiv = row.querySelector('.markdown, div.markdown');\n" +
                "          if (markdownDiv) {\n" +
                "            text = markdownDiv.innerText.trim();\n" +
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

            view.evaluateJavascript(prunerJs + prefillJs, null);
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
        if (sheetOverlayContainer.getVisibility() == View.VISIBLE) {
            closeControlSheet();
        } else {
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
    }
}
