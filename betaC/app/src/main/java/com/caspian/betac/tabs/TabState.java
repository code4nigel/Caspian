package com.caspian.betac.tabs;

import org.json.JSONObject;

/**
 * Pure data POJO representing the persistable state of a browser tab.
 * Contains zero Android UI/View references (no WebView, no Bitmaps, no Views)
 * making it 100% testable on standard JVM unit tests.
 */
public class TabState {
    public int id;
    public String title;
    public String nickname;
    public String url;
    public String service = "web";
    public boolean isDesktop = false;
    public boolean isIncognito = false;
    public boolean isMuted = false;
    public boolean isFavorite = false;
    public int splitPartnerId = -1;
    public String splitRole = "";
    public int splitOrientation = 1;
    public String splitName = "";
    public String caskId = "default";
    public String caskName = "Caspian Cask";
    public String caskIcon = "🌊";
    public String caskColor = "#1B4264";
    public boolean isRestoredFromSavedState = false;

    public TabState() {}

    public TabState(int id, String title, String url, String service, boolean isIncognito) {
        this.id = id;
        this.title = title;
        this.url = url;
        this.service = service != null ? service : "web";
        this.isIncognito = isIncognito;
    }

    public JSONObject toJson() {
        if (isIncognito) {
            // Privacy invariant: Never serialize private/incognito tabs
            return null;
        }
        try {
            JSONObject obj = new JSONObject();
            obj.put("id", id);
            obj.put("title", title);
            obj.put("nickname", nickname);
            obj.put("url", url);
            obj.put("service", service);
            obj.put("isDesktop", isDesktop);
            obj.put("isIncognito", false);
            obj.put("isMuted", isMuted);
            obj.put("isFavorite", isFavorite);
            obj.put("splitPartnerId", splitPartnerId);
            obj.put("splitRole", splitRole != null ? splitRole : "");
            obj.put("splitOrientation", splitOrientation);
            obj.put("splitName", splitName != null ? splitName : "");
            obj.put("caskId", caskId != null ? caskId : "default");
            obj.put("caskName", caskName != null ? caskName : "Caspian Cask");
            obj.put("caskIcon", caskIcon != null ? caskIcon : "🌊");
            obj.put("caskColor", caskColor != null ? caskColor : "#1B4264");
            return obj;
        } catch (Exception e) {
            return null;
        }
    }

    public static TabState fromJson(JSONObject obj, int fallbackId) {
        if (obj == null) return null;
        TabState state = new TabState();
        state.id = obj.optInt("id", fallbackId);
        state.title = obj.optString("title", null);
        state.nickname = obj.optString("nickname", null);
        state.url = obj.optString("url", "https://www.google.com");
        state.service = obj.optString("service", "web");
        state.isDesktop = obj.optBoolean("isDesktop", false);
        state.isIncognito = false; // Always false when restored from disk
        state.isMuted = obj.optBoolean("isMuted", false);
        state.isFavorite = obj.optBoolean("isFavorite", false);
        state.splitPartnerId = obj.optInt("splitPartnerId", -1);
        state.splitRole = obj.optString("splitRole", "");
        state.splitOrientation = obj.optInt("splitOrientation", 0);
        state.splitName = obj.optString("splitName", "");
        state.caskId = obj.optString("caskId", "default");
        state.caskName = obj.optString("caskName", "Caspian Cask");
        state.caskIcon = obj.optString("caskIcon", "🌊");
        state.caskColor = obj.optString("caskColor", "#1B4264");
        state.isRestoredFromSavedState = true;
        return state;
    }
}
