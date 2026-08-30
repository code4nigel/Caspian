package com.caspian.betac;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.CookieManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Manages "Caspian Casks" (Multi-Account Isolated Cookie & Session Vaults).
 * Each Cask maintains its own isolated cookie environment while sharing global app settings.
 */
public class CaskManager {
    private static final String TAG = "CaskManager";
    private static final String PREF_NAME = "caspian_casks_prefs";
    private static final String KEY_ACTIVE_CASK = "active_cask_id";
    private static final String KEY_CASKS_JSON = "casks_metadata_json";

    public static final String DEFAULT_CASK_ID = "cask_caspian";

    public static boolean isMultiProfileSupported() {
        try {
            return androidx.webkit.WebViewFeature.isFeatureSupported(androidx.webkit.WebViewFeature.MULTI_PROFILE);
        } catch (Throwable t) {
            return false;
        }
    }

    public static void applyProfileToWebView(android.webkit.WebView webView, String caskId) {
        if (webView == null || caskId == null) return;
        try {
            if (isMultiProfileSupported()) {
                androidx.webkit.ProfileStore store = androidx.webkit.ProfileStore.getInstance();
                store.getOrCreateProfile(caskId);
                androidx.webkit.WebViewCompat.setProfile(webView, caskId);
                Log.d(TAG, "Successfully assigned Multi-Profile: " + caskId + " to WebView");
            }
        } catch (Throwable t) {
            Log.e(TAG, "Error applying profile " + caskId + " to webView", t);
        }
    }

    // Known common domains for session cookie capture
    private static final List<String> MONITORED_DOMAINS = Arrays.asList(
            "https://chatgpt.com",
            "https://openai.com",
            "https://auth0.openai.com",
            "https://gemini.google.com",
            "https://google.com",
            "https://accounts.google.com",
            "https://youtube.com",
            "https://m.youtube.com",
            "https://bing.com",
            "https://claude.ai",
            "https://github.com",
            "https://x.com",
            "https://twitter.com",
            "https://reddit.com"
    );

    private final Context context;
    private final SharedPreferences prefs;
    private final File casksDir;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public static class CaskItem {
        public String id;
        public String name;
        public String icon;
        public String color;
        public boolean isDefault;
        public long createdAt;

        public CaskItem(String id, String name, String icon, String color, boolean isDefault, long createdAt) {
            this.id = id;
            this.name = name;
            this.icon = icon;
            this.color = color;
            this.isDefault = isDefault;
            this.createdAt = createdAt;
        }

        public JSONObject toJson() {
            try {
                JSONObject obj = new JSONObject();
                obj.put("id", id);
                obj.put("name", name);
                obj.put("icon", icon);
                obj.put("color", color);
                obj.put("isDefault", isDefault);
                obj.put("createdAt", createdAt);
                return obj;
            } catch (Exception e) {
                return new JSONObject();
            }
        }

        public static CaskItem fromJson(JSONObject obj) {
            return new CaskItem(
                    obj.optString("id", DEFAULT_CASK_ID),
                    obj.optString("name", "Caspian Cask"),
                    obj.optString("icon", "🌊"),
                    obj.optString("color", "#1B4264"),
                    obj.optBoolean("isDefault", false),
                    obj.optLong("createdAt", System.currentTimeMillis())
            );
        }
    }

    public CaskManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = this.context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.casksDir = new File(this.context.getFilesDir(), "caspian_casks");
        if (!casksDir.exists()) {
            casksDir.mkdirs();
        }
        ensureDefaultCasks();
    }

    private void ensureDefaultCasks() {
        if (!prefs.contains(KEY_CASKS_JSON)) {
            List<CaskItem> defaultList = new ArrayList<>();
            defaultList.add(new CaskItem("cask_caspian", "Caspian Cask", "🌊", "#1B4264", true, System.currentTimeMillis()));
            defaultList.add(new CaskItem("cask_pacific", "Pacific Cask", "⚓", "#00E5FF", false, System.currentTimeMillis() + 1));
            defaultList.add(new CaskItem("cask_atlantic", "Atlantic Cask", "🐬", "#3B82F6", false, System.currentTimeMillis() + 2));
            defaultList.add(new CaskItem("cask_arctic", "Arctic Cask", "🧊", "#38BDF8", false, System.currentTimeMillis() + 3));
            defaultList.add(new CaskItem("cask_coral", "Coral Cask", "🪸", "#F43F5E", false, System.currentTimeMillis() + 4));
            defaultList.add(new CaskItem("cask_abyss", "Abyss Cask", "🔱", "#8B5CF6", false, System.currentTimeMillis() + 5));

            saveCasksList(defaultList);
            prefs.edit().putString(KEY_ACTIVE_CASK, DEFAULT_CASK_ID).apply();
        }
    }

    public String getActiveCaskId() {
        return prefs.getString(KEY_ACTIVE_CASK, DEFAULT_CASK_ID);
    }

    public CaskItem getActiveCask() {
        String activeId = getActiveCaskId();
        List<CaskItem> list = getAllCasks();
        for (CaskItem item : list) {
            if (item.id.equals(activeId)) return item;
        }
        return list.isEmpty() ? new CaskItem("cask_caspian", "Caspian Cask", "🌊", "#1B4264", true, 0) : list.get(0);
    }

    public List<CaskItem> getAllCasks() {
        List<CaskItem> result = new ArrayList<>();
        String jsonStr = prefs.getString(KEY_CASKS_JSON, "[]");
        try {
            JSONArray arr = new JSONArray(jsonStr);
            for (int i = 0; i < arr.length(); i++) {
                result.add(CaskItem.fromJson(arr.getJSONObject(i)));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing casks JSON", e);
        }
        return result;
    }

    private void saveCasksList(List<CaskItem> list) {
        JSONArray arr = new JSONArray();
        for (CaskItem item : list) {
            arr.put(item.toJson());
        }
        prefs.edit().putString(KEY_CASKS_JSON, arr.toString()).apply();
    }

    public String getCasksPayloadJson() {
        try {
            JSONObject root = new JSONObject();
            root.put("activeCaskId", getActiveCaskId());
            CaskItem active = getActiveCask();
            root.put("activeCaskName", active.name);
            root.put("activeCaskIcon", active.icon);
            root.put("activeCaskColor", active.color);

            JSONArray arr = new JSONArray();
            for (CaskItem item : getAllCasks()) {
                arr.put(item.toJson());
            }
            root.put("casks", arr);
            return root.toString();
        } catch (Exception e) {
            return "{\"activeCaskId\":\"cask_caspian\",\"casks\":[]}";
        }
    }

    public boolean createCask(String name, String icon, String colorHex) {
        if (name == null || name.trim().isEmpty()) name = "Sea Cask";
        if (icon == null || icon.trim().isEmpty()) icon = "🌊";
        if (colorHex == null || colorHex.trim().isEmpty()) colorHex = "#00E5FF";

        String id = "cask_" + System.currentTimeMillis();
        CaskItem item = new CaskItem(id, name.trim(), icon.trim(), colorHex.trim(), false, System.currentTimeMillis());

        List<CaskItem> list = getAllCasks();
        list.add(item);
        saveCasksList(list);
        return true;
    }

    public boolean deleteCask(String caskId) {
        if (DEFAULT_CASK_ID.equals(caskId)) {
            Log.w(TAG, "Cannot delete default cask");
            return false;
        }

        List<CaskItem> list = getAllCasks();
        CaskItem toRemove = null;
        for (CaskItem item : list) {
            if (item.id.equals(caskId)) {
                toRemove = item;
                break;
            }
        }

        if (toRemove != null) {
            list.remove(toRemove);
            saveCasksList(list);

            // Delete associated cookie vault file
            File vaultFile = new File(casksDir, caskId + "_cookies.json");
            if (vaultFile.exists()) vaultFile.delete();

            // If active cask was deleted, switch back to default
            if (getActiveCaskId().equals(caskId)) {
                prefs.edit().putString(KEY_ACTIVE_CASK, DEFAULT_CASK_ID).apply();
            }
            return true;
        }
        return false;
    }

    public boolean renameCask(String caskId, String newName, String newIcon, String newColor) {
        List<CaskItem> list = getAllCasks();
        boolean found = false;
        for (CaskItem item : list) {
            if (item.id.equals(caskId)) {
                if (newName != null && !newName.trim().isEmpty()) item.name = newName.trim();
                if (newIcon != null && !newIcon.trim().isEmpty()) item.icon = newIcon.trim();
                if (newColor != null && !newColor.trim().isEmpty()) item.color = newColor.trim();
                found = true;
                break;
            }
        }
        if (found) {
            saveCasksList(list);
            return true;
        }
        return false;
    }

    public CaskItem getCaskById(String caskId) {
        if (caskId == null) return getActiveCask();
        for (CaskItem item : getAllCasks()) {
            if (item.id.equals(caskId)) return item;
        }
        return getActiveCask();
    }

    /**
     * Activates target Cask.
     * With AndroidX Multi-Profile, each WebView has its own permanent ProfileStore profile
     * and separate cookie jar, so open tabs in previous containers NEVER lose cookies or email sessions.
     */
    public void switchCask(String targetCaskId, Runnable onComplete) {
        if (targetCaskId == null || targetCaskId.isEmpty()) return;

        mainHandler.post(() -> {
            String currentId = getActiveCaskId();

            // Set target Cask as active in preferences
            prefs.edit().putString(KEY_ACTIVE_CASK, targetCaskId).apply();

            // Fallback for legacy devices lacking Multi-Profile support
            if (!isMultiProfileSupported()) {
                saveActiveCookiesToVault(currentId);
                CookieManager cookieManager = CookieManager.getInstance();
                cookieManager.removeAllCookies(null);
                cookieManager.flush();
                restoreCaskCookiesFromVault(targetCaskId);
            }

            // Notify completion
            if (onComplete != null) {
                onComplete.run();
            }
        });
    }

    /**
     * Saves cookies for monitored domains into a Cask JSON file.
     */
    public void saveActiveCookiesToVault(String caskId) {
        try {
            CookieManager cookieManager = CookieManager.getInstance();
            JSONObject vault = new JSONObject();

            for (String domainUrl : MONITORED_DOMAINS) {
                String cookieStr = cookieManager.getCookie(domainUrl);
                if (cookieStr != null && !cookieStr.isEmpty()) {
                    vault.put(domainUrl, cookieStr);
                }
            }

            File vaultFile = new File(casksDir, caskId + "_cookies.json");
            try (FileOutputStream fos = new FileOutputStream(vaultFile)) {
                fos.write(vault.toString().getBytes(StandardCharsets.UTF_8));
                fos.flush();
            }
            Log.d(TAG, "Saved cookie vault for cask: " + caskId);
        } catch (Exception e) {
            Log.e(TAG, "Error saving cookies for cask " + caskId, e);
        }
    }

    /**
     * Restores cookies from a Cask JSON file into CookieManager.
     */
    public void restoreCaskCookiesFromVault(String caskId) {
        try {
            File vaultFile = new File(casksDir, caskId + "_cookies.json");
            if (!vaultFile.exists()) {
                Log.d(TAG, "No existing cookie vault for cask: " + caskId + " (starting clean session)");
                return;
            }

            String content;
            try (FileInputStream fis = new FileInputStream(vaultFile)) {
                byte[] data = new byte[(int) vaultFile.length()];
                fis.read(data);
                content = new String(data, StandardCharsets.UTF_8);
            }

            JSONObject vault = new JSONObject(content);
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);

            java.util.Iterator<String> keys = vault.keys();
            while (keys.hasNext()) {
                String domainUrl = keys.next();
                String cookieStr = vault.optString(domainUrl, "");
                if (!cookieStr.isEmpty()) {
                    // Split multiple cookies if formatted as key=val; key2=val2
                    String[] cookiePairs = cookieStr.split(";");
                    for (String pair : cookiePairs) {
                        String trimmed = pair.trim();
                        if (!trimmed.isEmpty()) {
                            cookieManager.setCookie(domainUrl, trimmed);
                        }
                    }
                }
            }
            cookieManager.flush();
            Log.d(TAG, "Restored cookie vault for cask: " + caskId);
        } catch (Exception e) {
            Log.e(TAG, "Error restoring cookies for cask " + caskId, e);
        }
    }
}
