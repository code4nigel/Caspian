package com.caspian.betac.security;

import android.net.Uri;
import android.util.Log;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.webkit.JavaScriptReplyProxy;
import androidx.webkit.WebMessageCompat;
import androidx.webkit.WebViewCompat;

import org.json.JSONObject;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Origin-restricted message listener for Caspian Launch Hub events.
 * Securely communicates with file:///android_asset/launch_hub.html and incognito_hub.html
 * using AndroidX WebMessageListener without exposing reflection interfaces to remote sites or iframes.
 */
public class TrustedHubMessageHandler implements WebViewCompat.WebMessageListener {
    private static final String TAG = "TrustedHubHandler";

    public static final Set<String> ALLOWED_ORIGIN_RULES = Collections.singleton("*");

    public static final Set<String> ALLOWED_SERVICES = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList("chatgpt", "gemini", "claude", "deepseek", "youtube", "google", "hub", "web"))
    );

    public static final int MAX_PAYLOAD_BYTES = 32 * 1024; // 32 KB cap
    public static final int MAX_WALLPAPER_BYTES = 2 * 1024 * 1024; // 2 MB cap
    public static final int MAX_TOAST_LENGTH = 256;

    private static final Pattern SAFE_SOUND_PATTERN = Pattern.compile("^[a-zA-Z0-9_/-]+\\.(mp3|wav|ogg)$");
    private static final Pattern SAFE_CASK_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{1,64}$");

    public static boolean isOriginAllowed(String url) {
        return OriginVerifier.isPermittedHubDocument(url);
    }

    public static boolean isLocalSourceOrigin(String sourceOriginStr) {
        if (sourceOriginStr == null) return true;
        String s = sourceOriginStr.trim();
        if (s.isEmpty() || "null".equalsIgnoreCase(s) || "file://".equalsIgnoreCase(s) || "file://*".equalsIgnoreCase(s)) {
            return true;
        }
        try {
            URI uri = URI.create(s);
            String scheme = uri.getScheme();
            if ("file".equalsIgnoreCase(scheme)) return true;
            if ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) return false;
        } catch (Exception ignored) {}
        return false;
    }

    public static boolean isLocalSourceOrigin(Uri sourceOrigin) {
        if (sourceOrigin == null) return true;
        try {
            String scheme = sourceOrigin.getScheme();
            if (scheme != null) {
                if ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) {
                    return false;
                }
                if ("file".equalsIgnoreCase(scheme)) {
                    return true;
                }
            }
        } catch (Throwable ignored) {}
        try {
            String s = sourceOrigin.toString();
            if (s != null) {
                return isLocalSourceOrigin(s);
            }
        } catch (Throwable ignored) {}
        return false;
    }

    public static boolean isValidHttpsUrl(String url) {
        if (url == null || url.trim().isEmpty()) return false;
        String trimmed = url.trim();
        if (!trimmed.toLowerCase(Locale.ROOT).startsWith("https://")) return false;
        try {
            URI uri = URI.create(trimmed);
            return "https".equalsIgnoreCase(uri.getScheme()) && uri.getHost() != null && !uri.getHost().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isValidServiceName(String service) {
        if (service == null) return false;
        return ALLOWED_SERVICES.contains(service.trim().toLowerCase(Locale.ROOT));
    }

    public static boolean isValidSoundName(String sound) {
        if (sound == null || sound.trim().isEmpty()) return false;
        String trimmed = sound.trim();
        if (trimmed.contains("..") || trimmed.contains("\0") || trimmed.startsWith("/")) return false;
        return SAFE_SOUND_PATTERN.matcher(trimmed).matches();
    }

    public static boolean isValidWallpaper(String wp) {
        if (wp == null || wp.trim().isEmpty()) return false;
        if (wp.length() > MAX_WALLPAPER_BYTES) return false;
        String lower = wp.toLowerCase(Locale.ROOT);
        return lower.startsWith("data:image/") ||
               lower.startsWith("https://") ||
               lower.startsWith("#") ||
               lower.startsWith("linear-gradient") ||
               lower.startsWith("radial-gradient") ||
               lower.startsWith("rgba(") ||
               lower.startsWith("rgb(");
    }

    public static boolean isValidCaskId(String caskId) {
        if (caskId == null || caskId.trim().isEmpty()) return false;
        return SAFE_CASK_ID_PATTERN.matcher(caskId.trim()).matches();
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
        String currentUrl = null;
        try {
            currentUrl = view.getUrl();
        } catch (Throwable ignored) {}

        String data = null;
        try {
            data = message.getData();
        } catch (Throwable ignored) {}

        String sourceOriginStr = null;
        try {
            sourceOriginStr = sourceOrigin.toString();
        } catch (Throwable ignored) {}

        handleMessage(currentUrl, data, sourceOriginStr, isMainFrame);
    }

    public boolean handleMessage(String currentUrl, String data, String sourceOriginStr, boolean isMainFrame) {
        // 1. Enforce main frame execution only (reject remote iframe message injection)
        if (!isMainFrame) {
            Log.w(TAG, "Hub message rejected: message not from main frame");
            return false;
        }

        // 2. Reject remote source origins
        if (!isLocalSourceOrigin(sourceOriginStr)) {
            Log.w(TAG, "Hub message rejected from non-local sourceOrigin: " + sourceOriginStr);
            return false;
        }

        if (data == null || data.isEmpty()) return false;

        if (data.length() > MAX_PAYLOAD_BYTES) {
            Log.w(TAG, "Hub payload exceeds maximum byte limit (" + data.length() + ")");
            return false;
        }

        // 3. Security check: top-level page must be exactly a permitted local Hub document
        if (currentUrl == null || !OriginVerifier.isPermittedHubDocument(currentUrl)) {
            Log.w(TAG, "Hub message rejected from non-permitted document: " + currentUrl);
            return false;
        }

        try {
            JSONObject json = new JSONObject(data);
            String action = json.optString("action", "");

            if (callback == null) return true;

            switch (action) {
                case "openUrl": {
                    String url = json.optString("url", "").trim();
                    if (isValidHttpsUrl(url)) {
                        callback.onOpenUrl(url);
                    } else {
                        Log.w(TAG, "openUrl rejected non-HTTPS or invalid URL: " + url);
                    }
                    break;
                }
                case "switchService": {
                    String service = json.optString("service", "").trim().toLowerCase(Locale.ROOT);
                    if (isValidServiceName(service)) {
                        callback.onSwitchService(service);
                    } else {
                        Log.w(TAG, "switchService rejected unknown service: " + service);
                    }
                    break;
                }
                case "addNewTab": {
                    String service = json.optString("service", "web").trim().toLowerCase(Locale.ROOT);
                    String url = json.optString("url", "").trim();
                    if (!isValidServiceName(service)) {
                        Log.w(TAG, "addNewTab rejected unknown service: " + service);
                        break;
                    }
                    if (!url.isEmpty() && !isValidHttpsUrl(url)) {
                        Log.w(TAG, "addNewTab rejected non-HTTPS or invalid URL: " + url);
                        break;
                    }
                    callback.onAddNewTab(service, url);
                    break;
                }
                case "playAssetSound": {
                    String sound = json.optString("sound", "tap_button.mp3").trim();
                    if (isValidSoundName(sound)) {
                        callback.onPlayAssetSound(sound);
                    } else {
                        Log.w(TAG, "playAssetSound rejected invalid sound asset: " + sound);
                    }
                    break;
                }
                case "showToast": {
                    String msg = json.optString("message", "");
                    if (!msg.isEmpty()) {
                        if (msg.length() > MAX_TOAST_LENGTH) msg = msg.substring(0, MAX_TOAST_LENGTH);
                        callback.onShowToast(msg);
                    }
                    break;
                }
                case "showKeyboard": {
                    callback.onShowKeyboard();
                    break;
                }
                case "saveWallpaper": {
                    String wp = json.optString("wallpaper", "").trim();
                    if (isValidWallpaper(wp)) {
                        callback.onSaveWallpaper(wp);
                    } else {
                        Log.w(TAG, "saveWallpaper rejected invalid or oversized wallpaper payload");
                    }
                    break;
                }
                case "switchCaspianCask": {
                    String caskId = json.optString("caskId", "").trim();
                    if (isValidCaskId(caskId)) {
                        callback.onSwitchCask(caskId);
                    } else {
                        Log.w(TAG, "switchCaspianCask rejected invalid caskId: " + caskId);
                    }
                    break;
                }
                default:
                    Log.w(TAG, "Unknown action received on CaspianHubChannel: " + action);
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse hub message JSON", e);
            return false;
        }
    }
}
