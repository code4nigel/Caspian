package com.caspian.betac.security;

import android.net.Uri;
import android.util.Log;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.webkit.JavaScriptReplyProxy;
import androidx.webkit.WebMessageCompat;
import androidx.webkit.WebViewCompat;

import org.json.JSONObject;

/**
 * Origin-restricted message listener for Caspian Launch Hub events.
 * Securely replaces window.CaspianBridge on file:///android_asset/launch_hub.html
 * using AndroidX WebMessageListener without exposing reflection interfaces to remote sites.
 */
public class TrustedHubMessageHandler implements WebViewCompat.WebMessageListener {
    private static final String TAG = "TrustedHubHandler";
    public static final int MAX_PAYLOAD_BYTES = 32 * 1024; // 32 KB cap

    public static boolean isOriginAllowed(String url) {
        return OriginVerifier.isLocalAsset(url);
    }

    public interface HubActionCallback {
        void onOpenUrl(String url);
        void onSwitchService(String service);
        void onAddNewTab(String service, String url);
        void onPlayAssetSound(String sound);
        void onShowToast(String message);
        void onShowKeyboard();
        void onSaveWallpaper(String wallpaper);
        void onSwitchCask(String caskId);
    }

    private final HubActionCallback callback;

    public TrustedHubMessageHandler(HubActionCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onPostMessage(@NonNull WebView view,
                              @NonNull WebMessageCompat message,
                              @NonNull Uri sourceOrigin,
                              boolean isMainFrame,
                              @NonNull JavaScriptReplyProxy replyProxy) {
        String data = message.getData();
        if (data == null || data.isEmpty()) return;

        if (data.length() > MAX_PAYLOAD_BYTES) {
            Log.w(TAG, "Hub payload exceeds maximum byte limit (" + data.length() + ")");
            return;
        }

        // Security check: only allow execution if the current top-level page is an internal asset
        String currentUrl = view.getUrl();
        if (currentUrl == null || !OriginVerifier.isLocalAsset(currentUrl)) {
            Log.w(TAG, "Hub message rejected from non-local origin: " + currentUrl);
            return;
        }

        try {
            JSONObject json = new JSONObject(data);
            String action = json.optString("action", "");

            if (callback == null) return;

            switch (action) {
                case "openUrl": {
                    String url = json.optString("url", "");
                    if (!url.isEmpty()) callback.onOpenUrl(url);
                    break;
                }
                case "switchService": {
                    String service = json.optString("service", "");
                    if (!service.isEmpty()) callback.onSwitchService(service);
                    break;
                }
                case "addNewTab": {
                    String service = json.optString("service", "web");
                    String url = json.optString("url", "");
                    callback.onAddNewTab(service, url);
                    break;
                }
                case "playAssetSound": {
                    String sound = json.optString("sound", "tap_button.mp3");
                    callback.onPlayAssetSound(sound);
                    break;
                }
                case "showToast": {
                    String msg = json.optString("message", "");
                    if (!msg.isEmpty()) callback.onShowToast(msg);
                    break;
                }
                case "showKeyboard": {
                    callback.onShowKeyboard();
                    break;
                }
                case "saveWallpaper": {
                    String wp = json.optString("wallpaper", "");
                    callback.onSaveWallpaper(wp);
                    break;
                }
                case "switchCaspianCask": {
                    String caskId = json.optString("caskId", "");
                    if (!caskId.isEmpty()) callback.onSwitchCask(caskId);
                    break;
                }
                default:
                    Log.d(TAG, "Unhandled hub action: " + action);
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing hub web message: " + e.getMessage());
        }
    }
}
