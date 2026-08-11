package com.caspian.betab;

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
import android.widget.Toast;

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
    public void downloadFile(String fileName, String content, String mimeType) {
        try {
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File caspianFolder = new File(downloadsDir, "Caspian");
            File targetFolder = caspianFolder;

            if (fileName.contains("Debug_Log") || fileName.contains("Log")) {
                targetFolder = new File(caspianFolder, "Logs");
            }
            if (!targetFolder.exists()) {
                targetFolder.mkdirs();
            }

            File targetFile = new File(targetFolder, fileName);
            FileOutputStream fos = new FileOutputStream(targetFile);
            fos.write(content.getBytes(StandardCharsets.UTF_8));
            fos.flush();
            fos.close();

            String displayPath = "Downloads/Caspian/" + (targetFolder.getName().equals("Logs") ? "Logs/" : "") + fileName;
            showToast("Saved: " + displayPath);
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
                    WebView printWebView = new WebView(activity);
                    printWebView.getSettings().setJavaScriptEnabled(false);
                    printWebView.setWebViewClient(new WebViewClient() {
                        @Override
                        public void onPageFinished(WebView view, String url) {
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
                            }
                        }
                    });
                    printWebView.loadDataWithBaseURL(null, htmlContent, "text/html", "utf-8", null);
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
        prefs.edit().putString(key, value).apply();

        if (activity != null) {
            activity.runOnUiThread(activity::applyPrunerInMainWebView);
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
            sb.append("\"").append(entry.getKey()).append("\":").append(entry.getValue().toString());
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
}
