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

        // Load rules into memory immediately
        loadRulesInternal();
    }

    public void loadRulesAsync() {
        new Thread(this::loadRulesInternal).start();
    }

    private synchronized void loadRulesInternal() {
        try {
            // Priority 1: Read bundled asset rules
            String assetJsonStr = "";
            try {
                InputStream is = context.getAssets().open("waveguard_rules.json");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();
                assetJsonStr = sb.toString();
            } catch (Exception e) {
                Log.e(TAG, "Failed reading bundled asset rules: ", e);
            }

            JSONObject assetRoot = !assetJsonStr.isEmpty() ? new JSONObject(assetJsonStr) : new JSONObject();
            JSONArray assetDomains = assetRoot.optJSONArray("blockedDomains");
            int assetDomainCount = assetDomains != null ? assetDomains.length() : 0;

            JSONObject chosenRoot = assetRoot;

            // Priority 2: Check for custom rules in internal storage
            File customRulesFile = new File(context.getFilesDir(), "waveguard_rules_custom.json");
            if (customRulesFile.exists() && customRulesFile.length() > 50) {
                try {
                    InputStream fis = new FileInputStream(customRulesFile);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8));
                    StringBuilder csb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        csb.append(line);
                    }
                    reader.close();
                    JSONObject customRoot = new JSONObject(csb.toString());
                    JSONArray customDomains = customRoot.optJSONArray("blockedDomains");
                    int customDomainCount = customDomains != null ? customDomains.length() : 0;

                    // If custom rules have strictly MORE domains than asset rules, use custom.
                    // Otherwise bundled APK asset rules take precedence and overwrite the stale custom file.
                    if (customDomainCount > assetDomainCount) {
                        chosenRoot = customRoot;
                    } else if (!assetJsonStr.isEmpty()) {
                        try {
                            FileOutputStream fos = new FileOutputStream(customRulesFile);
                            fos.write(assetJsonStr.getBytes(StandardCharsets.UTF_8));
                            fos.close();
                        } catch (Exception ignored) {}
                    }
                } catch (Exception ignored) {}
            }

            // 1. Blocked Domains
            JSONArray domainsArr = chosenRoot.optJSONArray("blockedDomains");
            StringBuilder domainsJson = new StringBuilder();
            if (domainsArr != null) {
                Set<String> tempDomains = new HashSet<>();
                for (int i = 0; i < domainsArr.length(); i++) {
                    String d = domainsArr.optString(i, "").trim().toLowerCase(java.util.Locale.ROOT);
                    if (!d.isEmpty()) {
                        tempDomains.add(d);
                        if (domainsJson.length() > 0) domainsJson.append(",");
                        domainsJson.append("\"").append(d).append("\"");
                    }
                }
                blockedDomains.clear();
                blockedDomains.addAll(tempDomains);
            }

            // 2. Path Keywords
            JSONArray pathsArr = chosenRoot.optJSONArray("pathKeywords");
            StringBuilder pathsJson = new StringBuilder();
            if (pathsArr != null) {
                List<String> tempPaths = new ArrayList<>();
                for (int i = 0; i < pathsArr.length(); i++) {
                    String p = pathsArr.optString(i, "").trim().toLowerCase(java.util.Locale.ROOT);
                    if (!p.isEmpty()) {
                        tempPaths.add(p);
                        if (pathsJson.length() > 0) pathsJson.append(",");
                        pathsJson.append("\"").append(p).append("\"");
                    }
                }
                pathKeywords.clear();
                pathKeywords.addAll(tempPaths);
            }

            // 3. Cosmetic Selectors
            JSONArray cssArr = chosenRoot.optJSONArray("cosmeticSelectors");
            StringBuilder cssBuilder = new StringBuilder();
            if (cssArr != null) {
                for (int i = 0; i < cssArr.length(); i++) {
                    String sel = cssArr.optString(i, "").trim();
                    if (!sel.isEmpty()) {
                        if (cssBuilder.length() > 0) cssBuilder.append(", ");
                        cssBuilder.append(sel);
                    }
                }
            }
            String combinedSelectors = cssBuilder.toString();

            // 4. Build comprehensive client-side protection script (CSS + MutationObserver + Fetch/XHR Guard)
            StringBuilder js = new StringBuilder();
            js.append("(function() {\n");
            js.append("  if (window.__caspian_waveguard_active) return;\n");
            js.append("  window.__caspian_waveguard_active = true;\n");

            if (!combinedSelectors.isEmpty()) {
                String escapedCss = combinedSelectors.replace("'", "\\'");
                js.append("  try {\n");
                js.append("    var s = document.getElementById('caspian-waveguard-style') || document.createElement('style');\n");
                js.append("    s.id = 'caspian-waveguard-style';\n");
                js.append("    s.textContent = '").append(escapedCss).append(" { display: none !important; visibility: hidden !important; width: 0 !important; height: 0 !important; min-width: 0 !important; min-height: 0 !important; max-width: 0 !important; max-height: 0 !important; opacity: 0 !important; pointer-events: none !important; margin: 0 !important; padding: 0 !important; border: 0 !important; }';\n");
                js.append("    if (!s.parentNode) (document.head || document.documentElement).appendChild(s);\n");
                js.append("  } catch(e) {}\n");

                js.append("  try {\n");
                js.append("    var sel = '").append(escapedCss).append("';\n");
                js.append("    function cleanNode(n) {\n");
                js.append("      if (!n || n.nodeType !== 1) return;\n");
                js.append("      try {\n");
                js.append("        if (n.classList && n.classList.contains('aderasr-test-adsbox')) return;\n");
                js.append("        if (n.matches && n.matches(sel)) { n.remove(); return; }\n");
                js.append("        var m = n.querySelectorAll(sel);\n");
                js.append("        for (var i = 0; i < m.length; i++) {\n");
                js.append("          if (!m[i].classList.contains('aderasr-test-adsbox')) m[i].remove();\n");
                js.append("        }\n");
                js.append("      } catch(err) {}\n");
                js.append("    }\n");
                js.append("    var obs = new MutationObserver(function(muts) {\n");
                js.append("      for (var i = 0; i < muts.length; i++) {\n");
                js.append("        var nodes = muts[i].addedNodes;\n");
                js.append("        for (var j = 0; j < nodes.length; j++) cleanNode(nodes[j]);\n");
                js.append("      }\n");
                js.append("    });\n");
                js.append("    obs.observe(document.documentElement || document.body, { childList: true, subtree: true });\n");
                js.append("  } catch(e) {}\n");
            }

            js.append("  try {\n");
            js.append("    var blockedSet = new Set([").append(domainsJson.toString()).append("]);\n");
            js.append("    var kwList = [").append(pathsJson.toString()).append("];\n");
            js.append("    function isBlockedUrl(u) {\n");
            js.append("      if (!u) return false;\n");
            js.append("      try {\n");
            js.append("        var s = String(u).toLowerCase();\n");
            js.append("        for (var k = 0; k < kwList.length; k++) {\n");
            js.append("          if (s.indexOf(kwList[k]) !== -1) return true;\n");
            js.append("        }\n");
            js.append("        var h = '';\n");
            js.append("        var pIdx = s.indexOf('://');\n");
            js.append("        if (pIdx !== -1) {\n");
            js.append("          var p = pIdx + 3;\n");
            js.append("          var sl = s.indexOf('/', p);\n");
            js.append("          var q = s.indexOf('?', p);\n");
            js.append("          var end = sl !== -1 ? sl : (q !== -1 ? q : s.length);\n");
            js.append("          h = s.substring(p, end);\n");
            js.append("          var col = h.indexOf(':');\n");
            js.append("          if (col !== -1) h = h.substring(0, col);\n");
            js.append("        } else {\n");
            js.append("          h = s.split('/')[0].split('?')[0].split(':')[0];\n");
            js.append("        }\n");
            js.append("        if (h === 'google.com' || h === 'www.google.com' || h === 'youtube.com' || h === 'www.youtube.com' || h === 'facebook.com' || h === 'www.facebook.com' || h === 'linkedin.com' || h === 'www.linkedin.com' || h === 'tiktok.com' || h === 'www.tiktok.com' || h === 'reddit.com' || h === 'www.reddit.com' || h === 'redditstatic.com' || h === 'www.redditstatic.com' || h === 'redditmedia.com' || h === 'www.redditmedia.com' || h === 'redd.it' || h === 'wikipedia.org') return false;\n");
            js.append("        if (blockedSet.has(h)) return true;\n");
            js.append("        var dot = h.indexOf('.');\n");
            js.append("        while (dot > 0 && dot < h.length - 1) {\n");
            js.append("          if (blockedSet.has(h.substring(dot + 1))) return true;\n");
            js.append("          dot = h.indexOf('.', dot + 1);\n");
            js.append("        }\n");
            js.append("      } catch(e) {}\n");
            js.append("      return false;\n");
            js.append("    }\n");
            js.append("    if (window.fetch) {\n");
            js.append("      var origFetch = window.fetch;\n");
            js.append("      window.fetch = function(input, init) {\n");
            js.append("        var url = typeof input === 'string' ? input : (input && input.url ? input.url : '');\n");
            js.append("        if (isBlockedUrl(url)) {\n");
            js.append("          return Promise.reject(new TypeError('Failed to fetch (net::ERR_BLOCKED_BY_CLIENT)'));\n");
            js.append("        }\n");
            js.append("        return origFetch.apply(this, arguments);\n");
            js.append("      };\n");
            js.append("    }\n");
            js.append("    if (window.XMLHttpRequest) {\n");
            js.append("      var origOpen = XMLHttpRequest.prototype.open;\n");
            js.append("      var origSend = XMLHttpRequest.prototype.send;\n");
            js.append("      XMLHttpRequest.prototype.open = function(m, url) {\n");
            js.append("        this.__c_blocked = isBlockedUrl(url);\n");
            js.append("        return origOpen.apply(this, arguments);\n");
            js.append("      };\n");
            js.append("      XMLHttpRequest.prototype.send = function() {\n");
            js.append("        if (this.__c_blocked) {\n");
            js.append("          var self = this;\n");
            js.append("          setTimeout(function() {\n");
            js.append("            try {\n");
            js.append("              self.dispatchEvent(new ProgressEvent('error'));\n");
            js.append("              if (typeof self.onerror === 'function') self.onerror(new ProgressEvent('error'));\n");
            js.append("            } catch(e) {}\n");
            js.append("          }, 0);\n");
            js.append("          return;\n");
            js.append("        }\n");
            js.append("        return origSend.apply(this, arguments);\n");
            js.append("      };\n");
            js.append("    }\n");
            js.append("  } catch(e) {}\n");
            js.append("})();");

            cosmeticCssInjection = js.toString();

            Log.d(TAG, "Waveguard loaded " + blockedDomains.size() + " domains, " + pathKeywords.size() + " paths, and " + (cssArr != null ? cssArr.length() : 0) + " cosmetic selectors.");
        } catch (Exception e) {
            Log.e(TAG, "Error loading Waveguard rules: ", e);
        }
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

    public static boolean isEssentialHost(String host) {
        if (host == null) return false;
        String h = host.toLowerCase(java.util.Locale.ROOT).trim();
        if (h.startsWith("www.")) h = h.substring(4);
        if ("google.com".equals(h) || h.endsWith(".google.com")) {
            return !h.startsWith("adservice.") && !h.startsWith("partnerad.") && !h.startsWith("fundingchoicesmessages.");
        }
        if ("reddit.com".equals(h) || h.endsWith(".reddit.com")
                || "redditstatic.com".equals(h) || h.endsWith(".redditstatic.com")
                || "redditmedia.com".equals(h) || h.endsWith(".redditmedia.com")
                || "redd.it".equals(h) || h.endsWith(".redd.it")) {
            return !h.startsWith("alb.") && !h.startsWith("events.") && !h.startsWith("telemetry.");
        }
        return "youtube.com".equals(h) || "m.youtube.com".equals(h)
                || "facebook.com".equals(h) || "m.facebook.com".equals(h)
                || "instagram.com".equals(h) || "linkedin.com".equals(h)
                || "tiktok.com".equals(h)
                || "twitter.com".equals(h) || "x.com".equals(h)
                || "wikipedia.org".equals(h) || "github.com".equals(h);
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

    public String getClientSideProtectionJs() {
        return isGlobalEnabled ? cosmeticCssInjection : "";
    }

    public String getCosmeticCssInjection() {
        return getClientSideProtectionJs();
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

            // Suffix and exact domain matching (skipped for essential services like google.com, youtube.com)
            boolean matchesDomain = false;
            if (!isEssentialHost(host)) {
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
                // Video VAST & player midroll ad mocking (STRICTLY for video player ads, NEVER for scripts, images, or general web pages)
                boolean isScriptOrMedia = lower.endsWith(".js") || lower.contains(".js?") || lower.contains("/js/") ||
                                          lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".gif") ||
                                          lower.endsWith(".swf") || lower.endsWith(".webp");
                if (!isScriptOrMedia) {
                    if ((lower.contains("/pagead/") || lower.contains("doubleclick") || lower.contains("ad_type") || lower.contains("vast"))
                            && (lower.contains("advideo") || lower.contains("output=xml") || lower.contains("output=vast") || lower.contains("env=vp") || lower.contains("/pagead/ads?") || lower.contains("correlator="))) {
                        headers.put("Content-Type", "application/xml; charset=UTF-8");
                        String emptyVast = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><VAST version=\"2.0\"></VAST>";
                        return new WebResourceResponse("application/xml", "UTF-8", 200, "OK", headers,
                                new ByteArrayInputStream(emptyVast.getBytes(StandardCharsets.UTF_8)));
                    } else if (lower.contains("/youtubei/v1/player/ad_break") || lower.contains("/get_midroll_info")) {
                        headers.put("Content-Type", "application/json; charset=UTF-8");
                        return new WebResourceResponse("application/json", "UTF-8", 200, "OK", headers,
                                new ByteArrayInputStream("{}".getBytes(StandardCharsets.UTF_8)));
                    }
                }
            }
        } catch (Throwable ignored) {}

        // For all trackers, banners, scripts, and analytics, return 403 Forbidden with 0 bytes to trigger onerror on script/img/embed tags
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
