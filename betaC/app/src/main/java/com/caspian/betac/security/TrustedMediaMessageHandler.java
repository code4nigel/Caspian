package com.caspian.betac.security;

import android.net.Uri;
import android.util.Log;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.webkit.JavaScriptReplyProxy;
import androidx.webkit.WebMessageCompat;
import androidx.webkit.WebViewCompat;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Origin-restricted message listener for YouTube media events.
 * Replaces global reflection-based JavaScript interfaces on web tabs.
 * Uses AndroidX WebMessageListener with origin validation, strict payload limits,
 * and a deny-by-default command allowlist.
 */
public class TrustedMediaMessageHandler implements WebViewCompat.WebMessageListener {
    private static final String TAG = "TrustedMediaHandler";
    public static final int MAX_PAYLOAD_BYTES = 16 * 1024; // 16 KB cap

    public static final Set<String> ALLOWED_ORIGIN_RULES = new HashSet<>(Arrays.asList(
            "https://www.youtube.com",
            "https://m.youtube.com",
            "https://music.youtube.com",
            "https://youtube.com"
    ));

    public interface MediaMessageCallback {
        void onTimeUpdate(int tabId, double currentTime, double duration);
        void onStateUpdate(int tabId, boolean isPlaying, boolean isMuted);
        void onMetadataUpdate(int tabId, String title, String artist, String thumbnailUrl);
        void onPlaybackModesUpdate(int tabId, int repeatMode, boolean shuffleOn);
        void onVideoEnded(int tabId);
        void onShowSettingsMenu(int tabId);
        void onScrobbyPlayerState(int tabId, String stateJson);
    }

    private final int boundTabId;
    private final MediaMessageCallback callback;

    public TrustedMediaMessageHandler(int boundTabId, MediaMessageCallback callback) {
        this.boundTabId = boundTabId;
        this.callback = callback;
    }

    @Override
    public void onPostMessage(@NonNull WebView view,
                              @NonNull WebMessageCompat message,
                              @NonNull Uri sourceOrigin,
                              boolean isMainFrame,
                              @NonNull JavaScriptReplyProxy replyProxy) {
        String data = message.getData();
        if (data == null || data.isEmpty()) {
            return;
        }

        // Verify Origin
        String originStr = sourceOrigin.toString();
        if (!isOriginAllowed(originStr)) {
            Log.w(TAG, "BLOCKED message from unauthorized origin: " + originStr);
            return;
        }

        // Validate payload length
        if (data.length() > MAX_PAYLOAD_BYTES) {
            Log.w(TAG, "BLOCKED oversized message from: " + originStr + " (size: " + data.length() + ")");
            return;
        }

        parseAndDispatch(boundTabId, data, callback);
    }

    public static boolean isOriginAllowed(String origin) {
        if (origin == null) return false;
        String lower = origin.toLowerCase().trim();
        for (String rule : ALLOWED_ORIGIN_RULES) {
            if (lower.equals(rule) || lower.startsWith(rule + "/") || lower.startsWith(rule + ":")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Parses the JSON payload and dispatches strictly allowlisted commands.
     * Pure Java parsing for deterministic unit testing.
     */
    public static boolean parseAndDispatch(int tabId, String payload, MediaMessageCallback cb) {
        if (payload == null || cb == null) return false;
        if (payload.length() > MAX_PAYLOAD_BYTES) return false;

        try {
            JSONObject obj = new JSONObject(payload);
            String action = obj.optString("action", "");

            switch (action) {
                case "updateTime": {
                    double cur = obj.optDouble("currentTime", 0.0);
                    double dur = obj.optDouble("duration", 0.0);
                    if (cur < 0 || dur < 0) return false;
                    cb.onTimeUpdate(tabId, cur, dur);
                    return true;
                }

                case "updateState": {
                    if (!obj.has("isPlaying") || !obj.has("isMuted")) return false;
                    boolean isPlaying = obj.optBoolean("isPlaying", false);
                    boolean isMuted = obj.optBoolean("isMuted", false);
                    cb.onStateUpdate(tabId, isPlaying, isMuted);
                    return true;
                }

                case "updateMetadata": {
                    String title = obj.optString("title", "");
                    String artist = obj.optString("artist", "");
                    String thumb = obj.optString("thumbnailUrl", "");

                    // Length sanitization
                    if (title.length() > 300) title = title.substring(0, 300);
                    if (artist.length() > 300) artist = artist.substring(0, 300);
                    if (thumb.length() > 1024) thumb = "";

                    cb.onMetadataUpdate(tabId, title, artist, thumb);
                    return true;
                }

                case "updatePlaybackModes": {
                    int repeatMode = obj.optInt("repeatMode", 0);
                    boolean shuffleOn = obj.optBoolean("shuffleOn", false);
                    cb.onPlaybackModesUpdate(tabId, repeatMode, shuffleOn);
                    return true;
                }

                case "videoEnded": {
                    cb.onVideoEnded(tabId);
                    return true;
                }

                case "showSettingsMenu": {
                    cb.onShowSettingsMenu(tabId);
                    return true;
                }

                case "scrobbyState": {
                    String stateJson = obj.optString("stateJson", "");
                    if (stateJson.length() > 8192) return false;
                    cb.onScrobbyPlayerState(tabId, stateJson);
                    return true;
                }

                default:
                    Log.w(TAG, "BLOCKED unknown action: " + action);
                    return false;
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to parse message payload: " + e.getMessage());
            return false;
        }
    }
}
