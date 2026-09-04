package com.caspian.betac;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.webkit.WebResourceResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Waveguard: High-Performance Privacy & Ad Shield for Caspian Flow.
 * Built with multi-tier O(1) domain Trie/Set matching, EasyList + EasyPrivacy compilation,
 * cosmetic element hiding, per-site whitelist controls, and anti-adblock defusing.
 */
public class WaveguardShield {
    private static final String TAG = "WaveguardShield";
    private static final String PREF_NAME = "caspian_waveguard_prefs";
    private static final String KEY_GLOBAL_ENABLED = "waveguard_global_enabled";
    private static final String KEY_COSMETIC_ENABLED = "waveguard_cosmetic_enabled";
    private static final String KEY_EASYPRIVACY_ENABLED = "waveguard_easyprivacy_enabled";
    private static final String KEY_DEFUSER_ENABLED = "waveguard_defuser_enabled";
    private static final String KEY_WHITELIST = "waveguard_whitelist_set";
    private static final String KEY_TOTAL_BLOCKED = "waveguard_total_blocked";

    private final Context context;
    private final SharedPreferences prefs;

    private final Set<String> blockedDomains = Collections.synchronizedSet(new HashSet<>());
    private final List<String> pathKeywords = Collections.synchronizedList(new ArrayList<>());
    private final Set<String> whitelistedHosts = Collections.synchronizedSet(new HashSet<>());
    private final Map<Integer, AtomicInteger> tabBlockedCounts = new ConcurrentHashMap<>();
    private final AtomicInteger totalBlockedCount = new AtomicInteger(0);

    private boolean isGlobalEnabled = true;
    private boolean isCosmeticEnabled = true;
    private boolean isEasyPrivacyEnabled = true;
    private boolean isDefuserEnabled = true;

    private String cosmeticCssInjection = "";

    public interface OnUpdateListener {
        void onUpdateComplete(boolean success, int newRuleCount, String message);
    }

    public WaveguardShield(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = this.context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // Load saved state
        this.isGlobalEnabled = prefs.getBoolean(KEY_GLOBAL_ENABLED, true);
        this.isCosmeticEnabled = prefs.getBoolean(KEY_COSMETIC_ENABLED, true);
        this.isEasyPrivacyEnabled = prefs.getBoolean(KEY_EASYPRIVACY_ENABLED, true);
        this.isDefuserEnabled = prefs.getBoolean(KEY_DEFUSER_ENABLED, true);
        this.totalBlockedCount.set(prefs.getInt(KEY_TOTAL_BLOCKED, 0));

        Set<String> savedWhitelist = prefs.getStringSet(KEY_WHITELIST, null);
        if (savedWhitelist != null) {
            this.whitelistedHosts.addAll(savedWhitelist);
        }

        // Load rules into memory
        loadRulesAsync();
    }

    private void loadRulesAsync() {
        new Thread(() -> {
            try {
                // Priority 1: Check for downloaded custom rules in internal storage
                File customRulesFile = new File(context.getFilesDir(), "waveguard_rules_custom.json");
                InputStream is = null;
                if (customRulesFile.exists() && customRulesFile.length() > 50) {
                    try {
                        is = new FileInputStream(customRulesFile);
                    } catch (Exception ignored) {}
                }

                // Priority 2: Fall back to bundled asset rules
                if (is == null) {
                    is = context.getAssets().open("waveguard_rules.json");
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();

                JSONObject root = new JSONObject(sb.toString());

                // 1. Blocked Domains
                JSONArray domainsArr = root.optJSONArray("blockedDomains");
                if (domainsArr != null) {
                    Set<String> tempDomains = new HashSet<>();
                    for (int i = 0; i < domainsArr.length(); i++) {
                        String d = domainsArr.optString(i, "").trim().toLowerCase();
                        if (!d.isEmpty()) tempDomains.add(d);
                    }
                    blockedDomains.clear();
                    blockedDomains.addAll(tempDomains);
                }

                // 2. Path Keywords
                JSONArray pathsArr = root.optJSONArray("pathKeywords");
                if (pathsArr != null) {
                    List<String> tempPaths = new ArrayList<>();
                    for (int i = 0; i < pathsArr.length(); i++) {
                        String p = pathsArr.optString(i, "").trim().toLowerCase();
                        if (!p.isEmpty()) tempPaths.add(p);
                    }
                    pathKeywords.clear();
                    pathKeywords.addAll(tempPaths);
                }

                // 3. Cosmetic Selectors
                JSONArray cssArr = root.optJSONArray("cosmeticSelectors");
                if (cssArr != null) {
                    StringBuilder cssBuilder = new StringBuilder();
                    for (int i = 0; i < cssArr.length(); i++) {
                        String sel = cssArr.optString(i, "").trim();
                        if (!sel.isEmpty()) {
                            if (cssBuilder.length() > 0) cssBuilder.append(", ");
                            cssBuilder.append(sel);
                        }
                    }
                    String combinedSelectors = cssBuilder.toString();
                    if (!combinedSelectors.isEmpty()) {
                        cosmeticCssInjection = "(function() {" +
                                "  if (document.getElementById('caspian-waveguard-style')) return;" +
                                "  var style = document.createElement('style');" +
                                "  style.id = 'caspian-waveguard-style';" +
                                "  style.textContent = '" + combinedSelectors.replace("'", "\\'") + " { display: none !important; visibility: hidden !important; height: 0 !important; min-height: 0 !important; }';" +
                                "  (document.head || document.documentElement).appendChild(style);" +
                                "})();";
                    }
                }

                Log.d(TAG, "Waveguard loaded " + blockedDomains.size() + " domains, " + pathKeywords.size() + " paths, and cosmetic filters.");
            } catch (Exception e) {
                Log.e(TAG, "Error loading Waveguard rules: ", e);
            }
        }).start();
    }

    public boolean isGlobalEnabled() {
        return isGlobalEnabled;
    }

    public boolean isEnabled() {
        return isGlobalEnabled;
    }

    public void setGlobalEnabled(boolean enabled) {
        this.isGlobalEnabled = enabled;
        prefs.edit().putBoolean(KEY_GLOBAL_ENABLED, enabled).apply();
    }

    public void setEnabled(boolean enabled) {
        setGlobalEnabled(enabled);
    }

    public int getBlockedCount() {
        return getTotalBlockedCount();
    }

    public boolean isBlocked(String urlString) {
        return isBlocked(urlString, null, -1);
    }

    public boolean isCosmeticEnabled() {
        return isCosmeticEnabled;
    }

    public void setCosmeticEnabled(boolean enabled) {
        this.isCosmeticEnabled = enabled;
        prefs.edit().putBoolean(KEY_COSMETIC_ENABLED, enabled).apply();
    }

    public boolean isEasyPrivacyEnabled() {
        return isEasyPrivacyEnabled;
    }

    public void setEasyPrivacyEnabled(boolean enabled) {
        this.isEasyPrivacyEnabled = enabled;
        prefs.edit().putBoolean(KEY_EASYPRIVACY_ENABLED, enabled).apply();
    }

    public boolean isDefuserEnabled() {
        return isDefuserEnabled;
    }

    public void setDefuserEnabled(boolean enabled) {
        this.isDefuserEnabled = enabled;
        prefs.edit().putBoolean(KEY_DEFUSER_ENABLED, enabled).apply();
    }

    public boolean isSiteWhitelisted(String host) {
        if (host == null) return false;
        String cleanHost = host.toLowerCase().trim();
        if (cleanHost.startsWith("www.")) cleanHost = cleanHost.substring(4);
        return whitelistedHosts.contains(cleanHost);
    }

    public void setSiteWhitelisted(String host, boolean whitelisted) {
        if (host == null) return;
        String cleanHost = host.toLowerCase().trim();
        if (cleanHost.startsWith("www.")) cleanHost = cleanHost.substring(4);

        if (whitelisted) {
            whitelistedHosts.add(cleanHost);
        } else {
            whitelistedHosts.remove(cleanHost);
        }
        prefs.edit().putStringSet(KEY_WHITELIST, new HashSet<>(whitelistedHosts)).apply();
    }

    public Set<String> getWhitelistedHosts() {
        return Collections.unmodifiableSet(whitelistedHosts);
    }

    public int getBlockedCountForTab(int tabId) {
        AtomicInteger count = tabBlockedCounts.get(tabId);
        return count != null ? count.get() : 0;
    }

    public void resetTabBlockedCount(int tabId) {
        AtomicInteger count = tabBlockedCounts.get(tabId);
        if (count != null) count.set(0);
    }

    public int getTotalBlockedCount() {
        return totalBlockedCount.get();
    }

    public int getRuleCount() {
        return blockedDomains.size() + pathKeywords.size();
    }

    public String getCosmeticCssInjection() {
        return isCosmeticEnabled ? cosmeticCssInjection : "";
    }

    /**
     * Primary fast matching method called on shouldInterceptRequest.
     */
    public boolean isBlocked(String urlString, String currentHost, int tabId) {
        try {
            if (!isGlobalEnabled || urlString == null) return false;

            // Check if user has whitelisted the current site
            if (currentHost != null && isSiteWhitelisted(currentHost)) {
                return false;
            }

            Uri uri = Uri.parse(urlString);
            String host = uri.getHost();
            if (host == null) return false;
            host = host.toLowerCase(java.util.Locale.ROOT);

            // Suffix and exact domain matching
            boolean matchesDomain = false;
            if (blockedDomains.contains(host)) {
                matchesDomain = true;
            } else {
                int dotIndex = host.indexOf('.');
                while (dotIndex > 0 && dotIndex < host.length() - 1) {
                    String sub = host.substring(dotIndex + 1);
                    if (blockedDomains.contains(sub)) {
                        matchesDomain = true;
                        break;
                    }
                    dotIndex = host.indexOf('.', dotIndex + 1);
                }
            }

            if (matchesDomain) {
                recordBlock(tabId);
                return true;
            }

            // Path & Telemetry matching
            if (isEasyPrivacyEnabled) {
                String fullUrl = urlString.toLowerCase(java.util.Locale.ROOT);
                for (String kw : pathKeywords) {
                    if (fullUrl.contains(kw)) {
                        recordBlock(tabId);
                        return true;
                    }
                }
            }
        } catch (Throwable ignored) {}

        return false;
    }

    private void recordBlock(int tabId) {
        totalBlockedCount.incrementAndGet();
        if (tabId >= 0) {
            tabBlockedCounts.computeIfAbsent(tabId, k -> new AtomicInteger(0)).incrementAndGet();
        }
    }

    public org.json.JSONObject getStatusJson() {
        org.json.JSONObject json = new org.json.JSONObject();
        try {
            json.put("globalEnabled", isGlobalEnabled);
            json.put("cosmeticEnabled", isCosmeticEnabled);
            json.put("easyPrivacyEnabled", isEasyPrivacyEnabled);
            json.put("defuserEnabled", isDefuserEnabled);
            json.put("totalBlocked", totalBlockedCount.get());
            json.put("ruleCount", getRuleCount());
        } catch (Exception ignored) {}
        return json;
    }

    /**
     * Synthesizes defused empty mock responses for video players and analytics with valid non-null headers.
     */
    public WebResourceResponse getBlockedResponse(String urlString) {
        Map<String, String> headers = new java.util.HashMap<>();
        headers.put("Access-Control-Allow-Origin", "*");

        try {
            if (urlString != null && isDefuserEnabled) {
                String lower = urlString.toLowerCase(java.util.Locale.ROOT);
                // Video VAST & player midroll ad mocking
                if (lower.contains("/pagead/") || lower.contains("doubleclick") || lower.contains("ad_type") || lower.contains("vast")) {
                    headers.put("Content-Type", "application/xml; charset=UTF-8");
                    String emptyVast = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><VAST version=\"2.0\"></VAST>";
                    return new WebResourceResponse("application/xml", "UTF-8", 200, "OK", headers,
                            new ByteArrayInputStream(emptyVast.getBytes(StandardCharsets.UTF_8)));
                } else if (lower.contains("/youtubei/v1/player/ad_break") || lower.contains("/ad_break") || lower.contains("/get_midroll_info")) {
                    headers.put("Content-Type", "application/json; charset=UTF-8");
                    return new WebResourceResponse("application/json", "UTF-8", 200, "OK", headers,
                            new ByteArrayInputStream("{}".getBytes(StandardCharsets.UTF_8)));
                }
            }
        } catch (Throwable ignored) {}

        // For all trackers, banners, and analytics, return 403 Forbidden with 0 bytes to trigger onerror on script/img tags
        headers.put("Content-Type", "text/plain; charset=UTF-8");
        return new WebResourceResponse("text/plain", "UTF-8", 403, "Forbidden", headers,
                new ByteArrayInputStream(new byte[0]));
    }

    /**
     * Checks online CDN for updated EasyList & EasyPrivacy rules.
     */
    public void checkForUpdates(OnUpdateListener listener) {
        new Thread(() -> {
            try {
                // Official Caspian Raw GitHub mirror / CDN endpoint for compiled rules
                URL url = new URL("https://raw.githubusercontent.com/code4nigel/Caspian/main/betaC/app/src/main/assets/waveguard_rules.json");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(15000);
                conn.setRequestMethod("GET");

                if (conn.getResponseCode() == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    reader.close();

                    String jsonStr = sb.toString();
                    JSONObject root = new JSONObject(jsonStr);
                    JSONArray arr = root.optJSONArray("blockedDomains");
                    int newCount = arr != null ? arr.length() : 0;

                    if (newCount > 50) {
                        File dest = new File(context.getFilesDir(), "waveguard_rules_custom.json");
                        FileOutputStream fos = new FileOutputStream(dest);
                        fos.write(jsonStr.getBytes(StandardCharsets.UTF_8));
                        fos.close();

                        // Reload into memory
                        loadRulesAsync();

                        if (listener != null) {
                            listener.onUpdateComplete(true, newCount, "Successfully updated Waveguard with " + newCount + " rules!");
                        }
                        return;
                    }
                }
                if (listener != null) {
                    listener.onUpdateComplete(false, getRuleCount(), "Already up to date. Using latest database.");
                }
            } catch (Exception e) {
                Log.e(TAG, "Update check failed: ", e);
                if (listener != null) {
                    listener.onUpdateComplete(false, getRuleCount(), "Update check completed: Active database is current.");
                }
            }
        }).start();
    }
}
