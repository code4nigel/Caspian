package com.caspian.ai;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private String pendingPrefill = null;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable injectionRunnable;
    private boolean isPrinting = false;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        setContentView(webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        settings.setUserAgentString("Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Mobile Safari/537.36");

        webView.addJavascriptInterface(new CaspianBridge(this), "CaspianBridge");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (!isPrinting && url != null && (url.contains("chatgpt.com") || url.contains("gemini.google.com"))) {
                    injectCaspianUiAndScripts(view);
                }
            }

            @Override
            public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
                super.doUpdateVisitedHistory(view, url, isReload);
                if (!isPrinting && url != null && (url.contains("chatgpt.com") || url.contains("gemini.google.com"))) {
                    injectCaspianUiAndScripts(view);
                }
            }
        });

        webView.setWebChromeClient(new WebChromeClient());
        webView.loadUrl("file:///android_asset/launch_hub.html");

        // Periodically check and enforce Caspian UI injection every 2 seconds for SPA navigation
        injectionRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isPrinting && webView != null) {
                    String url = webView.getUrl();
                    if (url != null && (url.contains("chatgpt.com") || url.contains("gemini.google.com"))) {
                        injectCaspianUiAndScripts(webView);
                    }
                }
                if (!isPrinting) {
                    handler.postDelayed(this, 2000);
                }
            }
        };
        handler.postDelayed(injectionRunnable, 2000);
    }

    public void pauseInjectionTimer() {
        this.isPrinting = true;
        if (handler != null && injectionRunnable != null) {
            handler.removeCallbacks(injectionRunnable);
        }
    }

    public void resumeInjectionTimerLater() {
        handler.postDelayed(() -> {
            this.isPrinting = false;
            if (handler != null && injectionRunnable != null) {
                handler.postDelayed(injectionRunnable, 2000);
            }
        }, 8000);
    }

    public WebView getWebView() {
        return webView;
    }

    public void loadUrl(String url) {
        if (webView != null) {
            webView.loadUrl(url);
        }
    }

    public void loadUrlWithPrefill(String url, String promptContext) {
        this.pendingPrefill = promptContext;
        if (webView != null) {
            webView.loadUrl(url);
        }
    }

    private void injectCaspianUiAndScripts(WebView view) {
        if (view == null || isPrinting) return;
        try {
            String cssContent = readAssetFile("mobile_control.css");
            String htmlContent = readAssetFile("mobile_control.html");
            String prunerJs = readAssetFile("mobile_pruner.js");
            String controlJs = readAssetFile("mobile_control.js");

            String bodyHtml = htmlContent;
            if (htmlContent.contains("<body") && htmlContent.contains("</body>")) {
                int start = htmlContent.indexOf(">", htmlContent.indexOf("<body")) + 1;
                int end = htmlContent.indexOf("</body>");
                bodyHtml = htmlContent.substring(start, end);
            }

            String prefillJs = "";
            if (pendingPrefill != null && !pendingPrefill.isEmpty()) {
                prefillJs = "\nsetTimeout(function() {\n" +
                        "  var ta = document.querySelector('#prompt-textarea, textarea, div[contenteditable=\"true\"], .input-area');\n" +
                        "  if (ta) {\n" +
                        "    ta.focus();\n" +
                        "    if (ta.tagName === 'TEXTAREA') { ta.value = " + JSONObject.quote(pendingPrefill) + "; }\n" +
                        "    else { ta.innerText = " + JSONObject.quote(pendingPrefill) + "; }\n" +
                        "    ta.dispatchEvent(new Event('input', { bubbles: true }));\n" +
                        "  }\n" +
                        "}, 1500);\n";
                pendingPrefill = null;
            }

            String jsPayload = "(function initCaspian() {\n" +
                    "  function tryInject() {\n" +
                    "    if (!document.body) { setTimeout(tryInject, 100); return; }\n" +
                    "    if (!document.getElementById('caspian-injected-style')) {\n" +
                    "      var style = document.createElement('style');\n" +
                    "      style.id = 'caspian-injected-style';\n" +
                    "      style.textContent = " + JSONObject.quote(cssContent) + ";\n" +
                    "      (document.head || document.documentElement).appendChild(style);\n" +
                    "    }\n" +
                    "    if (!document.getElementById('caspian-injected-ui')) {\n" +
                    "      var container = document.createElement('div');\n" +
                    "      container.id = 'caspian-injected-ui';\n" +
                    "      container.innerHTML = " + JSONObject.quote(bodyHtml) + ";\n" +
                    "      document.body.appendChild(container);\n" +
                    "    }\n" +
                    "  }\n" +
                    "  tryInject();\n" +
                    "})();\n" +
                    prunerJs + "\n" +
                    controlJs +
                    prefillJs;

            view.evaluateJavascript(jsPayload, null);
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
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && injectionRunnable != null) {
            handler.removeCallbacks(injectionRunnable);
        }
    }
}
