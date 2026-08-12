package com.caspian.betaa;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Environment;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class CaspianBridge {

    private final MainActivity activity;
    private Toast mToast = null;

    public CaspianBridge(MainActivity activity) {
        this.activity = activity;
    }

    @JavascriptInterface
    public String getAppVersion() {
        if (activity != null) {
            try {
                PackageInfo pInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
                return pInfo.versionName;
            } catch (Exception e) {
                return "1.0.34";
            }
        }
        return "1.0.34";
    }

    @JavascriptInterface
    public String getOpenTabs() {
        if (activity != null) {
            return activity.getOpenTabsJson();
        }
        return "[]";
    }

    @JavascriptInterface
    public void createNewTab(String service) {
        if (activity != null) {
            activity.runOnUiThread(() -> activity.createNewTab(service));
        }
    }

    @JavascriptInterface
    public void switchTab(int tabId) {
        if (activity != null) {
            activity.runOnUiThread(() -> activity.switchTab(tabId));
        }
    }

    @JavascriptInterface
    public void closeSheet() {
        if (activity != null) {
            activity.runOnUiThread(activity::closeControlSheet);
        }
    }

    @JavascriptInterface
    public void closeTab(int tabId) {
        if (activity != null) {
            activity.runOnUiThread(() -> activity.closeTab(tabId));
        }
    }

    @JavascriptInterface
    public void closeAllTabs() {
        if (activity != null) {
            activity.runOnUiThread(activity::closeAllTabs);
        }
    }

    @JavascriptInterface
    public void toggleDebugRecording(boolean enable) {
        if (activity != null) {
            activity.runOnUiThread(() -> {
                if (enable) {
                    activity.startDebugRecording();
                } else {
                    activity.stopAndSaveDebugLog();
                }
            });
        }
    }

    @JavascriptInterface
    public boolean isDebugRecording() {
        return activity != null && activity.isDebugRecordingActive();
    }

    @JavascriptInterface
    public void setSystemNightMode(boolean isDark) {
        if (activity != null) {
            activity.runOnUiThread(() -> activity.toggleHostPageTheme(isDark));
        }
    }

    @JavascriptInterface
    public void exportConversation(String fmt) {
        if (activity != null) {
            activity.runOnUiThread(() -> activity.performExportOnMainWebView(fmt));
        }
    }

    @JavascriptInterface
    public void onConversationExtracted(String jsonStr, String fmt) {
        if (activity != null) {
            activity.runOnUiThread(() -> activity.handleExtractedConversation(jsonStr, fmt));
        }
    }

    @JavascriptInterface
    public void updateFloatingTheme(String startHex, String endHex, String shape) {
        if (activity != null) {
            activity.runOnUiThread(() -> activity.applyFloatingTheme(startHex, endHex, shape));
        }
    }

    @JavascriptInterface
    public void downloadFile(String fileName, String content, String mimeType) {
        try {
            // Determine subfolder based on file extension / content type
            String subFolder = "text";
            if (fileName.endsWith(".html") || (mimeType != null && mimeType.contains("html"))) {
                subFolder = "html";
            } else if (fileName.endsWith(".md") || (mimeType != null && mimeType.contains("markdown"))) {
                subFolder = "md";
            } else if (fileName.contains("Debug_Log") || fileName.contains("Log") || fileName.endsWith(".log")) {
                subFolder = "log";
            } else if (fileName.endsWith(".doc") || (mimeType != null && mimeType.contains("msword"))) {
                subFolder = "doc";
            } else if (fileName.endsWith(".pdf") || (mimeType != null && mimeType.contains("pdf"))) {
                subFolder = "pdf";
            }

            boolean success = false;
            String savedPath = "";

            // TIER 1: Try writing to root internal storage /Caspian/<subFolder>/
            try {
                File rootDir = new File(Environment.getExternalStorageDirectory(), "Caspian/" + subFolder);
                if (!rootDir.exists()) {
                    rootDir.mkdirs();
                }
                File targetFile = new File(rootDir, fileName);
                FileOutputStream fos = new FileOutputStream(targetFile);
                fos.write(content.getBytes(StandardCharsets.UTF_8));
                fos.flush();
                fos.close();
                success = true;
                savedPath = targetFile.getAbsolutePath();
            } catch (Exception e1) {
                // TIER 2: Try writing to public Downloads folder /Download/Caspian/<subFolder>/
                try {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                        android.content.ContentResolver resolver = activity.getContentResolver();
                        android.content.ContentValues values = new android.content.ContentValues();
                        values.put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                        values.put(android.provider.MediaStore.MediaColumns.MIME_TYPE, mimeType);
                        values.put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Caspian/" + subFolder);

                        android.net.Uri uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                        if (uri != null) {
                            java.io.OutputStream os = resolver.openOutputStream(uri);
                            if (os != null) {
                                os.write(content.getBytes(StandardCharsets.UTF_8));
                                os.flush();
                                os.close();
                                success = true;
                                savedPath = "Downloads/Caspian/" + subFolder + "/" + fileName;
                            }
                        }
                    } else {
                        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                        File targetFolder = new File(downloadsDir, "Caspian/" + subFolder);
                        if (!targetFolder.exists()) {
                            targetFolder.mkdirs();
                        }
                        File targetFile = new File(targetFolder, fileName);
                        FileOutputStream fos = new FileOutputStream(targetFile);
                        fos.write(content.getBytes(StandardCharsets.UTF_8));
                        fos.flush();
                        fos.close();
                        success = true;
                        savedPath = targetFile.getAbsolutePath();
                    }
                } catch (Exception e2) {
                    // TIER 3: Safe fallback to getExternalFilesDir (Android/data/com.caspian.betaa/files)
                    try {
                        File fallbackFolder = activity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                        File targetFolder = new File(fallbackFolder, "Caspian/" + subFolder);
                        if (!targetFolder.exists()) {
                            targetFolder.mkdirs();
                        }
                        File targetFile = new File(targetFolder, fileName);
                        FileOutputStream fos = new FileOutputStream(targetFile);
                        fos.write(content.getBytes(StandardCharsets.UTF_8));
                        fos.flush();
                        fos.close();
                        success = true;
                        savedPath = targetFile.getAbsolutePath();
                    } catch (Exception e3) {
                        e3.printStackTrace();
                    }
                }
            }

            if (success) {
                showToast("Saved: " + savedPath);
            } else {
                showToast("Failed to save file: " + fileName);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showToast("Error saving file: " + e.getLocalizedMessage());
        }
    }

    @JavascriptInterface
    public void printHtml(String jobName, String htmlContent) {
        if (activity != null) {
            activity.runOnUiThread(() -> {
                try {
                    activity.pauseInjectionTimer();
                    final WebView printWebView = new WebView(activity);
                    
                    printWebView.getSettings().setJavaScriptEnabled(true);
                    printWebView.getSettings().setDomStorageEnabled(true);
                    
                    final FrameLayout rootLayout = activity.findViewById(R.id.root_container);
                    printWebView.setVisibility(View.INVISIBLE);
                    FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(1, 1);
                    rootLayout.addView(printWebView, lp);

                    printWebView.setWebViewClient(new WebViewClient() {
                        @Override
                        public void onPageFinished(WebView view, String url) {
                            view.postDelayed(() -> {
                                try {
                                    PrintManager printManager = (PrintManager) activity.getSystemService(Context.PRINT_SERVICE);
                                    if (printManager != null) {
                                        PrintDocumentAdapter printAdapter = view.createPrintDocumentAdapter(jobName);
                                        printManager.print(jobName, printAdapter, new PrintAttributes.Builder().build());
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    showToast("Print error: " + e.getLocalizedMessage());
                                } finally {
                                    activity.resumeInjectionTimerLater();
                                    // Retain WebView for 30s in root layout to ensure print spooler completes document rendering
                                    view.postDelayed(() -> {
                                        try {
                                            rootLayout.removeView(printWebView);
                                            printWebView.destroy();
                                        } catch (Exception ex) {}
                                    }, 30000);
                                }
                            }, 3500);
                        }
                    });
                    printWebView.loadDataWithBaseURL("https://cdn.jsdelivr.net/", htmlContent, "text/html", "utf-8", null);
                } catch (Exception e) {
                    e.printStackTrace();
                    showToast("Print launch error: " + e.getLocalizedMessage());
                }
            });
        }
    }

    @JavascriptInterface
    public void printPage() {
        if (activity != null) {
            activity.runOnUiThread(() -> {
                try {
                    activity.pauseInjectionTimer();
                    WebView wv = activity.getWebView();
                    if (wv != null && wv.getContext() != null) {
                        PrintManager printManager = (PrintManager) activity.getSystemService(Context.PRINT_SERVICE);
                        if (printManager != null) {
                            PrintDocumentAdapter printAdapter = wv.createPrintDocumentAdapter("Caspian_Document");
                            printManager.print("Caspian_Document", printAdapter, new PrintAttributes.Builder().build());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showToast("Print error: " + e.getMessage());
                } finally {
                    activity.resumeInjectionTimerLater();
                }
            });
        }
    }

    @JavascriptInterface
    public void copyToClipboard(String text) {
        try {
            ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Caspian Transcript", text);
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
                showToast("Copied transcript to clipboard!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public void reloadActiveTab() {
        if (activity != null) {
            activity.runOnUiThread(() -> {
                MainActivity.TabItem active = activity.getActiveTab();
                if (active != null && active.webView != null) {
                    active.webView.reload();
                    showToast("↻ " + (active.title != null ? active.title : "Active tab") + " reloaded!");
                }
            });
        }
    }

    @JavascriptInterface
    public void updateTabDetails(int tabId, String nickname, String url) {
        if (activity != null) {
            activity.runOnUiThread(() -> {
                for (MainActivity.TabItem item : activity.getTabsList()) {
                    if (item.id == tabId) {
                        item.nickname = nickname;
                        if (url != null && !url.isEmpty() && !url.equals(item.url)) {
                            item.url = url;
                            if (item.webView != null) {
                                item.webView.loadUrl(url);
                            }
                        }
                        activity.saveTabsToPrefs();
                        showToast("Tab details updated successfully!");
                        break;
                    }
                }
            });
        }
    }

    @JavascriptInterface
    public void restoreLastClosedTab() {
        if (activity != null) {
            activity.runOnUiThread(() -> activity.restoreLastClosedTab());
        }
    }

    @JavascriptInterface
    public void reorderTabs(String newIdsJson) {
        if (activity != null) {
            activity.runOnUiThread(() -> {
                try {
                    org.json.JSONArray arr = new org.json.JSONArray(newIdsJson);
                    java.util.List<MainActivity.TabItem> reordered = new java.util.ArrayList<>();
                    for (int i = 0; i < arr.length(); i++) {
                        int id = arr.getInt(i);
                        for (MainActivity.TabItem item : activity.getTabsList()) {
                            if (item.id == id) {
                                reordered.add(item);
                                break;
                            }
                        }
                    }
                    if (reordered.size() == activity.getTabsList().size()) {
                        activity.getTabsList().clear();
                        activity.getTabsList().addAll(reordered);
                        activity.saveTabsToPrefs();
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    @JavascriptInterface
    public void switchService(String service) {
        if (activity != null) {
            activity.runOnUiThread(() -> activity.createNewTab(service));
        }
    }

    @JavascriptInterface
    public void convertAndLaunchTab(String promptContext) {
        if (activity != null) {
            activity.runOnUiThread(() -> activity.performExportOnMainWebView("convert"));
        }
    }

    @JavascriptInterface
    public void saveSetting(String key, String value) {
        SharedPreferences prefs = activity.getSharedPreferences("CaspianMobilePrefs", Context.MODE_PRIVATE);
        prefs.edit().putString(key, value).commit();

        if (activity != null) {
            activity.runOnUiThread(() -> {
                activity.applyPrunerInMainWebView();
                if ("active_refresh_rate".equals(key)) {
                    activity.updateRefreshTimer();
                }
            });
        }
    }

    @JavascriptInterface
    public String getSettings() {
        SharedPreferences prefs = activity.getSharedPreferences("CaspianMobilePrefs", Context.MODE_PRIVATE);
        Map<String, ?> allMap = prefs.getAll();
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, ?> entry : allMap.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(entry.getKey()).append("\":");
            Object val = entry.getValue();
            if (val instanceof String) {
                sb.append(JSONObject.quote((String) val));
            } else {
                sb.append(val.toString());
            }
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    @JavascriptInterface
    public void showToast(String msg) {
        if (activity != null) {
            activity.runOnUiThread(() -> {
                try {
                    if (mToast != null) {
                        mToast.cancel();
                    }
                    mToast = Toast.makeText(activity, msg, Toast.LENGTH_SHORT);
                    mToast.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    @JavascriptInterface
    public void seekYouTube(double seconds) {
        if (activity != null) {
            activity.runOnUiThread(() -> {
                MainActivity.TabItem activeTab = activity.getActiveTab();
                if (activeTab != null && activeTab.webView != null) {
                    activeTab.webView.evaluateJavascript(
                        "if (window.__CaspianYouTube) { window.__CaspianYouTube.seekBy(" + seconds + "); }", null
                    );
                }
            });
        }
    }

    @JavascriptInterface
    public void setYouTubeSpeed(double speed) {
        if (activity != null) {
            activity.runOnUiThread(() -> {
                MainActivity.TabItem activeTab = activity.getActiveTab();
                if (activeTab != null && activeTab.webView != null) {
                    activeTab.webView.evaluateJavascript(
                        "if (window.__CaspianYouTube) { window.__CaspianYouTube.setSpeed(" + speed + "); }", null
                    );
                }
            });
        }
    }

    @JavascriptInterface
    public void setYouTubeQuality(String quality) {
        if (activity != null) {
            activity.runOnUiThread(() -> {
                MainActivity.TabItem activeTab = activity.getActiveTab();
                if (activeTab != null && activeTab.webView != null) {
                    activeTab.webView.evaluateJavascript(
                        "if (window.__CaspianYouTube) { window.__CaspianYouTube.setQuality(" + JSONObject.quote(quality) + "); }", null
                    );
                }
            });
        }
    }

    @JavascriptInterface
    public void toggleTabMute(int tabId) {
        if (activity != null) {
            activity.runOnUiThread(() -> {
                MainActivity.TabItem targetTab = null;
                for (MainActivity.TabItem item : activity.getTabsList()) {
                    if (item.id == tabId) {
                        targetTab = item;
                        break;
                    }
                }
                if (targetTab != null && targetTab.webView != null) {
                    targetTab.isMuted = !targetTab.isMuted;
                    targetTab.webView.evaluateJavascript(
                        "(function(){ var vs = document.querySelectorAll('video, audio'); vs.forEach(function(v){ v.muted = " + targetTab.isMuted + "; }); })();", null
                    );
                    showToast(targetTab.isMuted ? "🔇 Tab Muted" : "🔊 Tab Unmuted");
                    activity.saveTabsToPrefs();
                }
            });
        }
    }

    @JavascriptInterface
    public void updateMediaPlaybackState(boolean isPlaying) {
        if (activity != null) {
            activity.runOnUiThread(() -> {
                MainActivity.TabItem activeTab = activity.getActiveTab();
                if (activeTab != null) {
                    activeTab.isPlayingAudio = isPlaying;
                }
            });
        }
    }

    @JavascriptInterface
    public void updateTabMediaPlaybackState(int tabId, boolean isPlaying) {
        if (activity != null) {
            activity.runOnUiThread(() -> {
                for (MainActivity.TabItem item : activity.getTabsList()) {
                    if (item.id == tabId || tabId == 0) {
                        item.isPlayingAudio = isPlaying;
                        if (tabId != 0) break;
                    }
                }
            });
        }
    }

    @JavascriptInterface
    public void toggleTabFavorite(int tabId) {
        if (activity != null) {
            activity.runOnUiThread(() -> {
                for (MainActivity.TabItem item : activity.getTabsList()) {
                    if (item.id == tabId) {
                        item.isFavorite = !item.isFavorite;
                        showToast(item.isFavorite ? "⭐ Tab Favorited (Protected from Close All)" : "★ Removed from Favorites");
                        activity.saveTabsToPrefs();
                        break;
                    }
                }
            });
        }
    }
}
