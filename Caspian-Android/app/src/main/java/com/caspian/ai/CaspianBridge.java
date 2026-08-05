package com.caspian.ai;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
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

    public CaspianBridge(MainActivity activity) {
        this.activity = activity;
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
            if (!caspianFolder.exists()) {
                caspianFolder.mkdirs();
            }

            File targetFile = new File(caspianFolder, fileName);
            FileOutputStream fos = new FileOutputStream(targetFile);
            fos.write(content.getBytes(StandardCharsets.UTF_8));
            fos.flush();
            fos.close();

            Toast.makeText(activity, "Saved to Downloads/Caspian/" + fileName, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(activity, "Error saving file: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(activity, "Print error: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            } finally {
                                activity.resumeInjectionTimerLater();
                            }
                        }
                    });
                    printWebView.loadDataWithBaseURL(null, htmlContent, "text/html", "utf-8", null);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(activity, "Print launch error: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(activity, "Print error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(activity, "Copied transcript to clipboard!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public void switchService(String service) {
        if (activity != null) {
            activity.runOnUiThread(() -> {
                if ("gemini".equalsIgnoreCase(service)) {
                    activity.loadUrl("https://gemini.google.com/");
                } else if ("hub".equalsIgnoreCase(service)) {
                    activity.loadUrl("file:///android_asset/launch_hub.html");
                } else {
                    activity.loadUrl("https://chatgpt.com/");
                }
            });
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
        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
    }
}
