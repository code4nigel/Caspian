package com.caspian.ai;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static class TabItem {
        public int id;
        public String title;
        public String url;
        public String service;
        public WebView webView;

        public TabItem(int id, String title, String url, String service, WebView webView) {
            this.id = id;
            this.title = title;
            this.url = url;
            this.service = service;
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

    // Touch Drag Variables for Native Wave Button
    private float dX, dY;
    private float startRawX, startRawY;
    private boolean isDragging = false;

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
    }

    private TabItem getActiveTab() {
        for (TabItem item : tabsList) {
            if (item.id == activeTabId) return item;
        }
        return tabsList.isEmpty() ? null : tabsList.get(0);
    }

    private void saveTabsToPrefs() {
        try {
            SharedPreferences prefs = getSharedPreferences("CaspianMobilePrefs", MODE_PRIVATE);
            JSONArray arr = new JSONArray();
            for (TabItem item : tabsList) {
                JSONObject obj = new JSONObject();
                obj.put("id", item.id);
                obj.put("title", item.title);
                obj.put("url", item.url);
                obj.put("service", item.service);
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

                        TabItem tab = new TabItem(id, title, url, service, null);
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

        // Hardware Performance Optimizations
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        settings.setEnableSmoothTransition(true);

        // Single-Window OAuth Transport for Instant Google Account Login ("Continue with Google")
        settings.setSupportMultipleWindows(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);

        // Authentic Pixel 8 Pro Chrome Mobile User-Agent to bypass Google Login restrictions
        settings.setUserAgentString("Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Mobile Safari/537.36");

        wv.addJavascriptInterface(new CaspianBridge(this), "CaspianBridge");

        wv.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
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
                if (!isPrinting && isSupportedUrl(url)) {
                    injectPrunerScript(view);
                }
                tab.url = url;
            }
        });

        // Single-Window Transport Override: Directs OAuth popups into WebView without freezing!
        wv.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                transport.setWebView(view);
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

                        // ONLY shift UP if card's top edge is inside keyboard overlay area
                        if (currentY > visibleBottom - cardHeight - 20) {
                            originalCardY = currentY;
                            float shiftedY = visibleBottom - cardHeight - 20;
                            // Enforce hard top ceiling boundary
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

    // Direct Host Theme Switcher for ChatGPT AND Google Gemini (No Activity Recreation -> No Crash!)
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

    // Chrome-Style Multi-WebView Tab Manager Methods
    public String getOpenTabsJson() {
        try {
            JSONArray arr = new JSONArray();
            for (TabItem item : tabsList) {
                JSONObject obj = new JSONObject();
                obj.put("id", item.id);
                obj.put("title", item.title);
                obj.put("url", item.url);
                obj.put("service", item.service);
                obj.put("active", item.id == activeTabId);
                arr.put(obj);
            }
            return arr.toString();
        } catch (Exception e) {
            return "[]";
        }
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

    public void switchTab(int tabId) {
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
        closeControlSheet();
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
            if (toRemove.webView != null) {
                webViewContainer.removeView(toRemove.webView);
                toRemove.webView.destroy();
            }
            tabsList.remove(toRemove);

            if (activeTabId == tabId) {
                TabItem last = tabsList.get(tabsList.size() - 1);
                switchTab(last.id);
            } else {
                saveTabsToPrefs();
            }
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

        // Document PDF: Natively print the live page on mainWebView (including images & formatting)
        if ("nativepdf".equalsIgnoreCase(fmt)) {
            closeControlSheet();
            new CaspianBridge(MainActivity.this).printPage();
            return;
        }

        String extractorJs = "(function() {\n" +
                "  function queryShadowSelectorAll(selector, root) {\n" +
                "    root = root || document;\n" +
                "    var elements = Array.prototype.slice.call(root.querySelectorAll(selector));\n" +
                "    var shadowRoots = Array.prototype.slice.call(root.querySelectorAll('*')).map(function(el) { return el.shadowRoot; }).filter(Boolean);\n" +
                "    for (var i = 0; i < shadowRoots.length; i++) {\n" +
                "      elements = elements.concat(queryShadowSelectorAll(selector, shadowRoots[i]));\n" +
                "    }\n" +
                "    return elements;\n" +
                "  }\n" +
                "  var turnSelector = '[data-testid^=\"conversation-turn\"], article, user-query, model-response, chat-turn';\n" +
                "  var rawTurns = queryShadowSelectorAll(turnSelector, document);\n" +
                "  var turns = rawTurns.filter(function(t) { return !rawTurns.some(function(other) { return other !== t && other.contains(t); }); });\n" +
                "  var result = [];\n" +
                "  var seen = {};\n" +
                "  for (var j = 0; j < turns.length; j++) {\n" +
                "    var txt = (turns[j].innerText || turns[j].textContent || '').trim();\n" +
                "    if (txt && !seen[txt]) {\n" +
                "      seen[txt] = true;\n" +
                "      result.push({\n" +
                "        index: result.length + 1,\n" +
                "        text: txt,\n" +
                "        html: turns[j].innerHTML || ''\n" +
                "      });\n" +
                "    }\n" +
                "  }\n" +
                "  return JSON.stringify(result);\n" +
                "})();";

        mainWebView.evaluateJavascript(extractorJs, value -> {
            try {
                if (value == null || value.equals("null") || value.equals("\"[]\"")) {
                    Toast.makeText(MainActivity.this, "No chat turns found to export!", Toast.LENGTH_SHORT).show();
                    return;
                }

                String jsonStr = value;
                if (jsonStr.startsWith("\"") && jsonStr.endsWith("\"")) {
                    jsonStr = new JSONObject("{\"d\":" + jsonStr + "}").getString("d");
                }

                JSONArray turnsArray = new JSONArray(jsonStr);
                if (turnsArray.length() == 0) {
                    Toast.makeText(MainActivity.this, "No chat turns found to export!", Toast.LENGTH_SHORT).show();
                    return;
                }

                closeControlSheet();

                String title = "AI Conversation";
                String dateStr = new java.util.Date().toLocaleString();
                String safeTitle = title.replaceAll("[^a-zA-Z0-9_-]", "_");

                if ("md".equalsIgnoreCase(fmt)) {
                    StringBuilder sb = new StringBuilder("# " + title + "\n\n*Exported via Caspian Mobile on " + dateStr + "*\n\n---\n\n");
                    for (int i = 0; i < turnsArray.length(); i++) {
                        JSONObject obj = turnsArray.getJSONObject(i);
                        sb.append("### Turn ").append(obj.getInt("index")).append("\n\n").append(obj.getString("text")).append("\n\n---\n\n");
                    }
                    saveAndDownloadFile(safeTitle + "_Caspian_Exported.md", sb.toString(), "text/markdown");

                } else if ("txt".equalsIgnoreCase(fmt)) {
                    StringBuilder sb = new StringBuilder("======================================\n" + title.toUpperCase() + "\nExported via Caspian Mobile on " + dateStr + "\n======================================\n\n");
                    for (int i = 0; i < turnsArray.length(); i++) {
                        JSONObject obj = turnsArray.getJSONObject(i);
                        sb.append("[TURN ").append(obj.getInt("index")).append("]\n").append(obj.getString("text")).append("\n\n--------------------------------------\n\n");
                    }
                    saveAndDownloadFile(safeTitle + "_Caspian_Exported.txt", sb.toString(), "text/plain");

                } else if ("doc".equalsIgnoreCase(fmt)) {
                    StringBuilder sb = new StringBuilder("<html><body><h1>" + title + "</h1><p>Exported via Caspian Mobile on " + dateStr + "</p>");
                    for (int i = 0; i < turnsArray.length(); i++) {
                        JSONObject obj = turnsArray.getJSONObject(i);
                        sb.append("<h3>Turn ").append(obj.getInt("index")).append("</h3><div>").append(obj.getString("html")).append("</div><hr>");
                    }
                    sb.append("</body></html>");
                    saveAndDownloadFile(safeTitle + "_Caspian_Exported.doc", sb.toString(), "application/msword");

                } else if ("styledpdf".equalsIgnoreCase(fmt)) {
                    StringBuilder turnsHtml = new StringBuilder();

                    for (int i = 0; i < turnsArray.length(); i++) {
                        JSONObject obj = turnsArray.getJSONObject(i);
                        int idx = obj.getInt("index");
                        String html = obj.getString("html");

                        turnsHtml.append("<div style=\"margin-bottom: 20px; border-radius: 16px; border: 1px solid #cbd5e1; background: #ffffff; padding: 16px; box-shadow: 0 4px 12px rgba(0,0,0,0.06);\">")
                                .append("<div style=\"display: flex; align-items: center; justify-content: space-between; margin-bottom: 10px; border-bottom: 1px solid #f1f5f9; padding-bottom: 8px;\">")
                                .append("<span style=\"font-weight: 800; font-size: 13px; color: #1B4264;\">[ Turn ").append(idx).append(" ]</span>")
                                .append("<span style=\"font-size: 10px; font-weight: 700; color: #10b981; background: rgba(16,185,129,0.15); padding: 2px 8px; border-radius: 10px;\">Caspian Format</span>")
                                .append("</div>")
                                .append("<div style=\"font-size: 13px; line-height: 1.6; color: #1f2937; white-space: pre-wrap;\">").append(html).append("</div></div>");
                    }

                    String fullHtml = "<!DOCTYPE html><html><head><meta charset=\"utf-8\"><title>" + title + "</title>" +
                            "<style>body { font-family: system-ui, -apple-system, sans-serif; padding: 24px; background: #ffffff; color: #0f172a; }" +
                            "h1 { font-size: 22px; font-weight: 700; color: #0f172a; margin-bottom: 6px; }" +
                            ".doc-meta { font-size: 11px; color: #64748b; margin-bottom: 24px; border-bottom: 1px solid #e2e8f0; padding-bottom: 12px; }</style>" +
                            "</head><body><h1>" + title + "</h1><div class=\"doc-meta\">Full Conversation Transcript &bull; Exported via Caspian Mobile on " + dateStr + "</div>" +
                            turnsHtml.toString() + "</body></html>";

                    saveAndDownloadFile(safeTitle + "_Caspian.html", fullHtml, "text/html");
                    new CaspianBridge(MainActivity.this).printHtml("Caspian_PDF", fullHtml);

                } else if ("convert".equalsIgnoreCase(fmt) || "copy".equalsIgnoreCase(fmt)) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < turnsArray.length(); i++) {
                        JSONObject obj = turnsArray.getJSONObject(i);
                        sb.append("[Turn ").append(obj.getInt("index")).append("]\n").append(obj.getString("text")).append("\n\n");
                    }
                    new CaspianBridge(MainActivity.this).copyToClipboard(sb.toString());

                    if ("convert".equalsIgnoreCase(fmt)) {
                        loadUrlWithPrefill("https://chatgpt.com/", sb.toString());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Export failed: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
                prefillJs = "\nsetTimeout(function() {\n" +
                        "  var ta = document.querySelector('#prompt-textarea, textarea, div[contenteditable=\"true\"], .input-area, .textarea');\n" +
                        "  if (ta) {\n" +
                        "    ta.focus();\n" +
                        "    if (ta.tagName === 'TEXTAREA') { ta.value = " + JSONObject.quote(pendingPrefill) + "; }\n" +
                        "    else { ta.innerText = " + JSONObject.quote(pendingPrefill) + "; }\n" +
                        "    ta.dispatchEvent(new Event('input', { bubbles: true }));\n" +
                        "  }\n" +
                        "}, 1500);\n";
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
            if (activeTab != null && activeTab.webView != null && activeTab.webView.canGoBack()) {
                activeTab.webView.goBack();
            } else {
                super.onBackPressed();
            }
        }
    }
}
