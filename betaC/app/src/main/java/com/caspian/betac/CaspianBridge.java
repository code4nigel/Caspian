package com.caspian.betac;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Environment;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CaspianBridge {

    private static final ExecutorService bgExecutor = Executors.newFixedThreadPool(2);
    private final MainActivity activity;
    private Toast mToast = null;

    public CaspianBridge(MainActivity activity) {
        this.activity = activity;
    }

    @JavascriptInterface
    public void savePref(String key, String val) {
        if (activity != null) {
            bgExecutor.execute(() -> {
                SharedPreferences prefs = activity.getSharedPreferences("CaspianFlowPrefs", Context.MODE_PRIVATE);
                prefs.edit().putString(key, val).apply();
                if ("master_sfx_muted".equals(key) || "sound_muted".equals(key)) {
                    activity.runOnUiThread(() -> activity.setMasterSfxMuted("true".equalsIgnoreCase(val)));
                }
            });
        }
    }

    @JavascriptInterface
    public void setMasterSfxMuted(boolean muted) {
        if (activity != null) {
            activity.runOnUiThread(() -> activity.setMasterSfxMuted(muted));
        }
    }

    @JavascriptInterface
    public void toggleMasterMute(boolean muted) {
        setMasterSfxMuted(muted);
    }

    @JavascriptInterface
    public String getPref(String key, String fallback) {
        if (activity != null) {
            SharedPreferences prefs = activity.getSharedPreferences("CaspianFlowPrefs", Context.MODE_PRIVATE);
            return prefs.getString(key, fallback);
        }
        return fallback;
    }

    @JavascriptInterface
    public void saveSetting(String key, String val) {
        if (activity != null) {
            bgExecutor.execute(() -> {
                SharedPreferences prefs = activity.getSharedPreferences("CaspianFlowPrefs", Context.MODE_PRIVATE);
                prefs.edit().putString(key, val).apply();
            });
        }
    }

    @JavascriptInterface
    public String getSettings() {
        if (activity == null) return "{}";
        try {
            SharedPreferences prefs = activity.getSharedPreferences("CaspianFlowPrefs", Context.MODE_PRIVATE);
            Map<String, ?> allMap = prefs.getAll();
            return new JSONObject(allMap).toString();
        } catch (Exception e) {
            return "{}";
        }
    }

    @JavascriptInterface
    public void switchService(String service) {
        if (activity != null && service != null) {
            activity.runOnUiThread(() -> activity.switchActiveTabService(service));
        }
    }

    @JavascriptInterface
    public String getAppVersion() {
        if (activity != null) {
            try {
                PackageInfo pInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
                return pInfo.versionName;
            } catch (Exception e) {
                return "1.0.2-BetaC";
            }
        }
        return "1.0.2-BetaC";
    }

    @JavascriptInterface
    public String getOpenTabs() {
        return getOpenTabsJson();
    }

    @JavascriptInterface
    public String getOpenTabsJson() {
        if (activity != null) {
            return activity.getOpenTabsJson();
        }
        return "[]";
    }

    @JavascriptInterface
    public void switchTab(int tabId) {
        if (activity != null) {
            activity.runOnUiThread(() -> activity.switchToTab(tabId));
        }
    }

    @JavascriptInterface
    public void closeTab(int tabId) {
        if (activity != null) {
            activity.runOnUiThread(() -> activity.closeTab(tabId));
        }
    }

    @JavascriptInterface
    public void closeMultipleTabs(String jsonIds) {
        if (activity != null) {
            try {
                JSONArray arr = new JSONArray(jsonIds);
                List<Integer> ids = new ArrayList<>();
                for (int i = 0; i < arr.length(); i++) ids.add(arr.getInt(i));
                activity.runOnUiThread(() -> activity.closeMultipleTabs(ids));
            } catch (Exception ignored) {}
        }
    }

    @JavascriptInterface
    public void restoreLastClosedGroupTabs() {
        if (activity != null) {
            activity.runOnUiThread(activity::restoreLastClosedGroupTabs);
        }
    }

    @JavascriptInterface
    public void setGroupTabsFavorite(String jsonIds, boolean isFav) {
        if (activity != null) {
            try {
                JSONArray arr = new JSONArray(jsonIds);
                List<Integer> ids = new ArrayList<>();
                for (int i = 0; i < arr.length(); i++) ids.add(arr.getInt(i));
                activity.runOnUiThread(() -> activity.setGroupTabsFavorite(ids, isFav));
            } catch (Exception ignored) {}
        }
    }

    @JavascriptInterface
    public void setTabsFavorite(String jsonIds, boolean isFav) {
        setGroupTabsFavorite(jsonIds, isFav);
    }

    @JavascriptInterface
    public void closeAllTabs() {
        if (activity != null) {
            activity.runOnUiThread(activity::closeAllTabs);
        }
    }

    @JavascriptInterface
    public void createNewTab(String service) {
        addNewTab(service, "");
    }

    @JavascriptInterface
    public void addNewTab(String service, String prompt) {
        if (activity != null) {
            activity.runOnUiThread(() -> activity.addNewTab(service, prompt));
        }
    }

    @JavascriptInterface
    public void addNewIncognitoTab() {
        if (activity != null) {
            activity.runOnUiThread(activity::addNewIncognitoTab);
        }
    }

    @JavascriptInterface
    public void openTab(String url) {
        openUrl(url);
    }

    @JavascriptInterface
    public void openNewTab(String url) {
        if (activity != null) {
            activity.runOnUiThread(() -> {
                String finalUrl = (url != null && !url.trim().isEmpty()) ? url : "file:///android_asset/launch_hub.html";
                String service = "file:///android_asset/launch_hub.html".equals(finalUrl) ? "hub" : "web";
                activity.addNewTab(service, null, finalUrl, false);
                activity.hideControlSheet();
            });
        }
    }

    @JavascriptInterface
    public void openLaunchHubInNewTab() {
        if (activity != null) {
            activity.runOnUiThread(() -> {
                activity.addNewTab("hub", null, "file:///android_asset/launch_hub.html", false);
                activity.hideControlSheet();
            });
        }
    }

    @JavascriptInterface
    public void openUrl(String url) {
        if (activity != null) {
            activity.runOnUiThread(() -> activity.navigateUrl(url));
        }
    }

    @JavascriptInterface
    public void openInOtherSplit(String url) {
        if (activity != null) {
            activity.runOnUiThread(() -> activity.openInOtherSplitPane(url));
        }
    }

    @JavascriptInterface
    public void closeSheet() {
        hideControlSheet();
    }

    @JavascriptInterface
    public void hideControlSheet() {
        if (activity != null) {
            activity.runOnUiThread(activity::hideControlSheet);
        }
    }

    @JavascriptInterface
    public void openExternalUrl(String url) {
        if (activity != null) {
            activity.runOnUiThread(() -> {
                activity.addNewTab("web", "", url, false);
                activity.hideControlSheet();
            });
        }
    }

    @JavascriptInterface
    public void addNewTab(String service, String prompt, String url, boolean isIncognito) {
        if (activity != null) {
            activity.runOnUiThread(() -> {
                activity.addNewTab(service, prompt, url, isIncognito);
                activity.hideControlSheet();
            });
        }
    }

    @JavascriptInterface
    public int getActionButtonClicks() {
        if (activity == null) return 0;
        return activity.getActionButtonClickCount();
    }

    @JavascriptInterface
    public void toggleGoogleDock(boolean enabled) {
        if (activity != null) {
            activity.runOnUiThread(() -> activity.toggleGoogleSearchDock(enabled));
        }
    }

    @JavascriptInterface
    public void toggleYouTubeEngine(boolean enabled) {
        if (activity != null) {
            activity.runOnUiThread(() -> activity.toggleFloatingYouTubeRemote(enabled));
        }
    }

    @JavascriptInterface
    public void toggleChatGPTDock(boolean enabled) {
        if (activity != null) {
            activity.runOnUiThread(() -> activity.toggleChatGPTDock(enabled));
        }
    }

    @JavascriptInterface
    public void setSystemNightMode(boolean isDark) {
        if (activity != null) {
            activity.runOnUiThread(() -> activity.setThemeMode(isDark ? "dark" : "light"));
        }
    }

    @JavascriptInterface
    public void toggleHostPageTheme(boolean isDark) {
        if (activity != null) {
            activity.runOnUiThread(() -> activity.setThemeMode(isDark ? "dark" : "light"));
        }
    }

    @JavascriptInterface
    public void playAssetSound(String assetPath) {
        if (activity != null && assetPath != null) {
            bgExecutor.execute(() -> activity.playAssetSound(assetPath));
        }
    }

    @JavascriptInterface
    public void setSfxVolume(float volume) {
        if (activity != null) {
            activity.runOnUiThread(() -> activity.setSfxVolume(volume));
        }
    }

    @JavascriptInterface
    public void toggleDesktopMode(int tabId) {
        if (activity != null) {
            activity.runOnUiThread(() -> activity.toggleDesktopMode(tabId));
        }
    }

    @JavascriptInterface
    public void toggleReaderMode() {
        if (activity != null) {
            activity.runOnUiThread(activity::toggleReaderMode);
        }
    }

    @JavascriptInterface
    public void toggleSplitMode() {
        if (activity != null) {
            activity.runOnUiThread(activity::toggleSplitView);
        }
    }

    @JavascriptInterface
    public void toggleAdBlock(boolean enabled) {
        if (activity != null) {
            activity.runOnUiThread(() -> activity.setAdBlockEnabled(enabled));
        }
    }

    @JavascriptInterface
    public void applyPruningSettings(int limit, String mode, boolean enabled) {
        if (activity != null) {
            SharedPreferences prefs = activity.getSharedPreferences("CaspianFlowPrefs", Context.MODE_PRIVATE);
            prefs.edit()
                    .putInt("chat_message_limit", limit)
                    .putString("chat_message_limit", String.valueOf(limit))
                    .putString("chat_pruning_mode", mode)
                    .putBoolean("chat_limit_enabled", enabled)
                    .putString("chat_limit_enabled", String.valueOf(enabled))
                    .apply();
            activity.runOnUiThread(() -> activity.applyPruningSettings(limit, mode, enabled));
        }
    }

    @JavascriptInterface
    public int getBlockedAdsCount() {
        return activity != null ? activity.getBlockedAdsCount() : 0;
    }

    @JavascriptInterface
    public void setSearchEngine(String engineName) {
        if (activity != null) {
            activity.runOnUiThread(() -> activity.setDefaultSearchEngine(engineName));
        }
    }

    @JavascriptInterface
    public void exportCurrentTab(String format) {
        if (activity != null) {
            activity.runOnUiThread(() -> activity.performExportOnMainWebView(format));
        }
    }

    @JavascriptInterface
    public void exportConversation(String format) {
        if (activity != null) {
            activity.runOnUiThread(() -> activity.performExportOnMainWebView(format));
        }
    }

    @JavascriptInterface
    public void onConversationExtracted(String jsonStr, String fmt) {
        if (activity != null) {
            activity.runOnUiThread(() -> activity.handleExtractedConversation(jsonStr, fmt));
        }
    }

    @JavascriptInterface
    public void setPageZoom(int zoomPercent) {
        if (activity != null) {
            activity.runOnUiThread(() -> activity.setPageZoom(zoomPercent));
        }
    }

    @JavascriptInterface
    public int getPageZoom() {
        return activity != null ? activity.getPageZoom() : 100;
    }

    @JavascriptInterface
    public void triggerVoiceSearch() {
        if (activity != null) {
            activity.runOnUiThread(activity::startVoiceRecognition);
        }
    }

    @JavascriptInterface
    public void playHaptic(String soundType) {
        if (activity != null) {
            activity.runOnUiThread(() -> activity.playUiFeedbackSound(soundType));
        }
    }

    @JavascriptInterface
    public void reloadTab(int tabId) {
        if (activity != null) {
            activity.runOnUiThread(() -> activity.reloadTab(tabId));
        }
    }

    @JavascriptInterface
    public void reloadActiveTab() {
        if (activity != null) {
            activity.runOnUiThread(() -> activity.reloadActiveTab());
        }
    }

    @JavascriptInterface
    public void copyToClipboard(String text) {
        if (activity != null) {
            try {
                ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Caspian Text", text);
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                    showToast("Copied to clipboard!");
                }
            } catch (Exception ignored) {}
        }
    }

    @JavascriptInterface
    public void showToast(String msg) {
        if (activity != null) {
            activity.runOnUiThread(() -> {
                try {
                    if (mToast != null) mToast.cancel();
                    mToast = Toast.makeText(activity, msg, Toast.LENGTH_SHORT);
                    mToast.show();
                } catch (Exception ignored) {}
            });
        }
    }

    // Pod Customization Bridge Methods
    @JavascriptInterface
    public void setPodShape(String shape) {
        if (activity != null) {
            activity.runOnUiThread(() -> activity.setPodShape(shape));
        }
    }

    @JavascriptInterface
    public void setPodScale(float scale) {
        if (activity != null) {
            activity.runOnUiThread(() -> activity.setPodScale(scale));
        }
    }

    @JavascriptInterface
    public void setPodColor(String hexColor) {
        if (activity != null) {
            activity.runOnUiThread(() -> activity.setPodColor(hexColor));
        }
    }

    @JavascriptInterface
    public void setPodOpacity(float opacity) {
        if (activity != null) {
            activity.runOnUiThread(() -> activity.setPodOpacity(opacity));
        }
    }

    @JavascriptInterface
    public String getPodSettingsJson() {
        return activity != null ? activity.getPodSettingsJson() : "{}";
    }

    @JavascriptInterface
    public void toggleDebugRecording(boolean enable) {
        if (activity != null) {
            activity.runOnUiThread(() -> {
                if (enable) activity.startDebugRecording();
                else activity.stopAndSaveDebugLog();
            });
        }
    }

    @JavascriptInterface
    public boolean isDebugRecording() {
        return activity != null && activity.isDebugRecordingActive();
    }

    @JavascriptInterface
    public void toggleTabFavorite(int tabId) {
        if (activity != null) {
            activity.runOnUiThread(() -> activity.toggleTabFavorite(tabId));
        }
    }

    @JavascriptInterface
    public void toggleTabMute(int tabId) {
        if (activity != null) {
            activity.runOnUiThread(() -> activity.toggleTabMute(tabId));
        }
    }

    @JavascriptInterface
    public void updateTabDetails(int tabId, String nickname, String url, String targetCaskId) {
        if (activity != null) {
            activity.runOnUiThread(() -> activity.updateTabDetails(tabId, nickname, url, targetCaskId));
        }
    }

    @JavascriptInterface
    public void updateTabDetails(int tabId, String nickname, String url) {
        if (activity != null) {
            activity.runOnUiThread(() -> activity.updateTabDetails(tabId, nickname, url, null));
        }
    }

    @JavascriptInterface
    public void changeTabCask(int tabId, String targetCaskId) {
        if (activity != null) {
            activity.runOnUiThread(() -> activity.changeTabCask(tabId, targetCaskId));
        }
    }

    @JavascriptInterface
    public void restoreLastClosedTab() {
        if (activity != null) {
            activity.runOnUiThread(activity::restoreLastClosedTab);
        }
    }

    @JavascriptInterface
    public void reorderTabs(String newIdsJson) {
        if (activity != null) {
            activity.runOnUiThread(() -> activity.reorderTabs(newIdsJson));
        }
    }


    @JavascriptInterface
    public void convertAndLaunchTab(String promptContext) {
        exportCurrentTab("convert");
    }

    @JavascriptInterface
    public void setWidgetScale(String type, double scale) {
        if (activity != null) {
            activity.runOnUiThread(() -> activity.applyWidgetScale(type, (float) scale));
        }
    }

    @JavascriptInterface
    public void updateFloatingTheme(String startHex, String endHex, String shape) {
        if (activity != null) {
            activity.runOnUiThread(() -> activity.applyFloatingTheme(startHex, endHex, shape));
        }
    }

    @JavascriptInterface
    public void updateYouTubeState(boolean isPlaying, boolean isMuted) {
        if (activity != null) {
            activity.runOnUiThread(() -> activity.updateYouTubeLiveState(isPlaying, isMuted));
        }
    }

    @JavascriptInterface
    public void togglePlayYouTube() {
        if (activity != null) activity.runOnUiThread(activity::togglePlayYouTube);
    }

    @JavascriptInterface
    public void toggleFullscreenYouTube() {
        if (activity != null) activity.runOnUiThread(activity::toggleFullscreenYouTube);
    }

    @JavascriptInterface
    public void showPlayerControls() {
        if (activity != null) activity.runOnUiThread(activity::showYouTubePlayerControls);
    }

    @JavascriptInterface
    public void showYouTubeSettingsMenu() {
        if (activity != null) activity.runOnUiThread(activity::showYouTubeSettingsMenu);
    }

    @JavascriptInterface
    public void toggleMuteYouTube() {
        if (activity != null) activity.runOnUiThread(activity::toggleMuteYouTube);
    }

    @JavascriptInterface
    public void seekYouTube(double seconds) {
        if (activity != null) activity.runOnUiThread(() -> activity.seekYouTube(seconds));
    }

    @JavascriptInterface
    public void setYouTubeSpeed(double speed) {
        if (activity != null) activity.runOnUiThread(() -> activity.setYouTubeSpeed(speed));
    }

    @JavascriptInterface
    public void setYouTubeQuality(String quality) {
        if (activity != null) activity.runOnUiThread(() -> activity.setYouTubeQuality(quality));
    }

    @JavascriptInterface
    public void toggleFloatingYouTubeRemote(boolean show) {
        if (activity != null) activity.runOnUiThread(() -> activity.toggleFloatingYouTubeRemote(show));
    }

    @JavascriptInterface
    public void toggleGoogleSearchDock(boolean show) {
        if (activity != null) activity.runOnUiThread(() -> activity.toggleGoogleSearchDock(show));
    }

    @JavascriptInterface
    public void setGoogleDockAutoCollapse(boolean enabled) {
        if (activity != null) activity.runOnUiThread(() -> activity.setGoogleDockAutoCollapse(enabled));
    }

    @JavascriptInterface
    public void setYtRemoteAutoCollapse(boolean enabled) {
        if (activity != null) activity.runOnUiThread(() -> activity.setYtRemoteAutoCollapse(enabled));
    }

    @JavascriptInterface
    public void setChatgptDockAutoCollapse(boolean enabled) {
        if (activity != null) activity.runOnUiThread(() -> activity.setChatgptDockAutoCollapse(enabled));
    }

    @JavascriptInterface
    public void setGeminiDockAutoCollapse(boolean enabled) {
        if (activity != null) activity.runOnUiThread(() -> activity.setGeminiDockAutoCollapse(enabled));
    }

    @JavascriptInterface
    public void toggleGeminiDock(boolean show) {
        if (activity != null) activity.runOnUiThread(() -> activity.toggleGeminiDock(show));
    }

    @JavascriptInterface
    public void downloadFile(String fileName, String content, String mimeType) {
        if (activity != null) {
            activity.runOnUiThread(() -> activity.downloadFile(fileName, content, mimeType));
        }
    }

    @JavascriptInterface
    public void printHtml(String jobName, String htmlContent) {
        if (activity != null) {
            activity.runOnUiThread(() -> activity.printHtml(jobName, htmlContent));
        }
    }

    @JavascriptInterface
    public void printPage() {
        if (activity != null) {
            activity.runOnUiThread(activity::printPage);
        }
    }

    @JavascriptInterface
    public void checkForAppUpdates(boolean isManual) {
        if (activity == null) return;
        if (isManual) {
            activity.runOnUiThread(() -> Toast.makeText(activity, "🔍 Checking GitHub for updates...", Toast.LENGTH_SHORT).show());
        }
        GitHubUpdateManager updateManager = new GitHubUpdateManager(activity);
        updateManager.checkForUpdates(isManual, new GitHubUpdateManager.UpdateCheckCallback() {
            @Override
            public void onResult(GitHubUpdateManager.UpdateInfo info) {
                    activity.evaluateJavascriptInControlSheet("if(window.onUpdateCheckResult) window.onUpdateCheckResult(" + info.toJson().toString() + ");");
            }

            @Override
            public void onError(String message) {
                if (activity != null) {
                    activity.runOnUiThread(() -> {
                        if (isManual) {
                            AlertDialog.Builder b = new AlertDialog.Builder(activity);
                            b.setTitle("⚠️ Update Check Notice");
                            b.setMessage("Could not reach GitHub:\n" + message + "\n\nPlease check your internet connection and try again.");
                            b.setPositiveButton("OK", null);
                            b.show();
                        }
                    });
                    activity.evaluateJavascriptInControlSheet("if(window.onUpdateCheckError) window.onUpdateCheckError(" + JSONObject.quote(message) + ");");
                }
            }
        });
    }

    @JavascriptInterface
    public void downloadAndInstallUpdate(String apkUrl, String apkFileName) {
        if (activity == null) return;
        GitHubUpdateManager updateManager = new GitHubUpdateManager(activity);
        updateManager.downloadApk(apkUrl, apkFileName, new GitHubUpdateManager.DownloadCallback() {
            @Override
            public void onProgress(int percent, long downloadedBytes, long totalBytes) {
                if (activity != null) {
                    activity.evaluateJavascriptInControlSheet("if(window.onUpdateDownloadProgress) window.onUpdateDownloadProgress(" + percent + ", " + downloadedBytes + ", " + totalBytes + ");");
                }
            }

            @Override
            public void onComplete(File apkFile) {
                if (activity != null) {
                    activity.runOnUiThread(() -> {
                        boolean initiated = updateManager.installApk(activity, apkFile);
                        activity.evaluateJavascriptInControlSheet("if(window.onUpdateDownloadComplete) window.onUpdateDownloadComplete(" + initiated + ");");
                    });
                }
            }

            @Override
            public void onError(String message) {
                if (activity != null) {
                    activity.evaluateJavascriptInControlSheet("if(window.onUpdateDownloadError) window.onUpdateDownloadError(" + JSONObject.quote(message) + ");");
                }
            }
        });
    }

    // ==========================================
    // CASPIAN CASKS (MULTI-ACCOUNT CONTAINERS)
    // ==========================================

    @JavascriptInterface
    public String getCaspianCasks() {
        if (activity == null) return "{\"activeCaskId\":\"cask_caspian\",\"casks\":[]}";
        try {
            CaskManager manager = new CaskManager(activity);
            return manager.getCasksPayloadJson();
        } catch (Exception e) {
            return "{\"activeCaskId\":\"cask_caspian\",\"casks\":[]}";
        }
    }

    @JavascriptInterface
    public String getCaspianCasksJson() {
        return getCaspianCasks();
    }

    @JavascriptInterface
    public boolean switchCaspianCask(String caskId) {
        if (activity == null || caskId == null) return false;
        try {
            CaskManager manager = new CaskManager(activity);
            manager.switchCask(caskId, () -> {
                activity.runOnUiThread(() -> {
                    CaskManager.CaskItem active = manager.getActiveCask();
                    showToast(active.icon + " Switched to " + active.name + "! ✨");
                    activity.reloadActiveTabOrHub();
                    String payload = manager.getCasksPayloadJson();
                    String js = "if(window.onCaspianCasksUpdated) window.onCaspianCasksUpdated(" + JSONObject.quote(payload) + ");";
                    activity.evaluateJavascriptInControlSheet(js);
                    activity.evaluateJavascriptInActiveTab(js);
                });
            });
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @JavascriptInterface
    public boolean createCaspianCask(String name, String icon, String colorHex) {
        if (activity == null) return false;
        try {
            CaskManager manager = new CaskManager(activity);
            boolean created = manager.createCask(name, icon, colorHex);
            if (created) {
                activity.runOnUiThread(() -> {
                    showToast("🌊 Cask '" + name + "' created!");
                    String payload = manager.getCasksPayloadJson();
                    String js = "if(window.onCaspianCasksUpdated) window.onCaspianCasksUpdated(" + JSONObject.quote(payload) + ");";
                    activity.evaluateJavascriptInControlSheet(js);
                    activity.evaluateJavascriptInActiveTab(js);
                });
            }
            return created;
        } catch (Exception e) {
            return false;
        }
    }

    @JavascriptInterface
    public boolean deleteCaspianCask(String caskId) {
        if (activity == null || caskId == null) return false;
        try {
            CaskManager manager = new CaskManager(activity);
            boolean deleted = manager.deleteCask(caskId);
            if (deleted) {
                activity.runOnUiThread(() -> {
                    showToast("Cask deleted.");
                    String payload = manager.getCasksPayloadJson();
                    String js = "if(window.onCaspianCasksUpdated) window.onCaspianCasksUpdated(" + JSONObject.quote(payload) + ");";
                    activity.evaluateJavascriptInControlSheet(js);
                    activity.evaluateJavascriptInActiveTab(js);
                });
            }
            return deleted;
        } catch (Exception e) {
            return false;
        }
    }

    @JavascriptInterface
    public boolean renameCaspianCask(String caskId, String newName, String newIcon, String newColor) {
        if (activity == null || caskId == null) return false;
        try {
            CaskManager manager = new CaskManager(activity);
            boolean renamed = manager.renameCask(caskId, newName, newIcon, newColor);
            if (renamed) {
                activity.runOnUiThread(() -> {
                    String payload = manager.getCasksPayloadJson();
                    String js = "if(window.onCaspianCasksUpdated) window.onCaspianCasksUpdated(" + JSONObject.quote(payload) + ");";
                    activity.evaluateJavascriptInControlSheet(js);
                    activity.evaluateJavascriptInActiveTab(js);
                });
            }
            return renamed;
        } catch (Exception e) {
            return false;
        }
    }

    @JavascriptInterface
    public void openControlCasksModal() {
        if (activity == null) return;
        activity.runOnUiThread(() -> {
            activity.openControlSheet();
            activity.evaluateJavascriptInControlSheet("if(typeof window.openControlCasksModal === 'function') window.openControlCasksModal();");
        });
    }

    // ==========================================
    // PDF STUDY READER BRIDGE
    // ==========================================

    @JavascriptInterface
    public void askAiFromPdf(String text, String targetService) {
        if (activity == null || text == null || text.trim().isEmpty()) return;
        activity.runOnUiThread(() -> {
            activity.handleAskAiFromPdf(text.trim(), targetService);
        });
    }

    @JavascriptInterface
    public void askAiFromPdfWithImage(String text, String base64Image, String targetService) {
        if (activity == null) return;
        activity.runOnUiThread(() -> {
            activity.handleAskAiFromPdfWithImage(text, base64Image, targetService);
        });
    }

    @JavascriptInterface
    public void launchGoogleLensWithBase64(String base64Image) {
        if (activity == null || base64Image == null) return;
        activity.runOnUiThread(() -> {
            activity.launchGoogleLensWithBase64(base64Image);
        });
    }

    @JavascriptInterface
    public void copyImageToClipboard(String base64Image) {
        if (activity == null || base64Image == null) return;
        activity.runOnUiThread(() -> {
            activity.copyImageToClipboard(base64Image);
        });
    }

    @JavascriptInterface
    public void triggerCaspianWhirlpool() {
        if (activity == null) return;
        activity.runOnUiThread(() -> {
            activity.startCaspianWhirlpool();
        });
    }

    @JavascriptInterface
    public String getPdfBase64(String path) {
        if (path == null || path.trim().isEmpty()) return "";
        try {
            File f = new File(path);
            if (!f.exists() || !f.canRead()) return "";
            byte[] bytes = new byte[(int) f.length()];
            try (java.io.FileInputStream fis = new java.io.FileInputStream(f)) {
                fis.read(bytes);
            }
            return android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP);
        } catch (Exception e) {
            return "";
        }
    }
}
