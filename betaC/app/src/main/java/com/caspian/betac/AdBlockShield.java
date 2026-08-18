package com.caspian.betac;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class AdBlockShield {
    private static final String TAG = "AdBlockShield";

    private final Set<String> blockedDomains = Collections.synchronizedSet(new HashSet<>());
    private final AtomicInteger blockedRequestCount = new AtomicInteger(0);
    private boolean isEnabled = true;

    private static final String[] DEFAULT_BLOCKED_DOMAINS = new String[]{
            "doubleclick.net",
            "googleadservices.com",
            "googlesyndication.com",
            "pagead2.googlesyndication.com",
            "adservice.google.com",
            "adnxs.com",
            "taboola.com",
            "outbrain.com",
            "criteo.com",
            "criteo.net",
            "amazon-adsystem.com",
            "scorecardresearch.com",
            "adroll.com",
            "rubiconproject.com",
            "pubmatic.com",
            "openx.net",
            "casalemedia.com",
            "advertising.com",
            "quantserve.com",
            "zedo.com",
            "moatads.com",
            "adsafeprotected.com",
            "serving-sys.com",
            "yieldmanager.com",
            "applovin.com",
            "unityads.unity3d.com",
            "vungle.com",
            "ironsrc.com",
            "chartboost.com",
            "flurry.com",
            "admob.com"
    };

    public AdBlockShield(Context context) {
        // Initialize default core domain filters
        for (String domain : DEFAULT_BLOCKED_DOMAINS) {
            blockedDomains.add(domain.toLowerCase());
        }

        // Load extended blocklist from assets asynchronously if available
        new Thread(() -> {
            try {
                InputStream is = context.getAssets().open("adblock_rules.json");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();

                JSONArray array = new JSONArray(sb.toString());
                for (int i = 0; i < array.length(); i++) {
                    blockedDomains.add(array.getString(i).trim().toLowerCase());
                }
                Log.d(TAG, "Loaded " + blockedDomains.size() + " ad/tracker blocking rules.");
            } catch (Exception e) {
                Log.d(TAG, "Using default ad blocker rule set (bundled).");
            }
        }).start();
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
    }

    public int getBlockedCount() {
        return blockedRequestCount.get();
    }

    public void resetBlockedCount() {
        blockedRequestCount.set(0);
    }

    public boolean isBlocked(String urlString) {
        if (!isEnabled || urlString == null) return false;

        try {
            Uri uri = Uri.parse(urlString);
            String host = uri.getHost();
            if (host == null) return false;
            host = host.toLowerCase();

            // Direct host match or subdomain match
            for (String blocked : blockedDomains) {
                if (host.equals(blocked) || host.endsWith("." + blocked)) {
                    blockedRequestCount.incrementAndGet();
                    Log.d(TAG, "[SHIELD BLOCKED] Ad/Tracker: " + host + " | Total: " + blockedRequestCount.get());
                    return true;
                }
            }

            // Path & telemetry indicators
            String path = uri.getPath();
            if (path != null) {
                String lowerPath = path.toLowerCase();
                if (lowerPath.contains("/pagead/") || lowerPath.contains("/adserver/") ||
                    lowerPath.contains("doubleclick") || lowerPath.contains("fbevents.js") ||
                    lowerPath.contains("google-analytics.com/analytics.js")) {
                    blockedRequestCount.incrementAndGet();
                    return true;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    /**
     * Strips UTM and tracking query parameters from URL
     */
    public String cleanTrackingParameters(String urlString) {
        if (urlString == null || !urlString.contains("?")) return urlString;
        try {
            Uri uri = Uri.parse(urlString);
            Uri.Builder builder = uri.buildUpon().clearQuery();
            for (String param : uri.getQueryParameterNames()) {
                if (param.startsWith("utm_") || param.equals("fbclid") ||
                    param.equals("gclid") || param.equals("msclkid") ||
                    param.equals("ref_src") || param.equals("mc_cid") ||
                    param.equals("mc_eid")) {
                    continue; // Skip tracking parameters
                }
                for (String val : uri.getQueryParameters(param)) {
                    builder.appendQueryParameter(param, val);
                }
            }
            return builder.build().toString();
        } catch (Exception e) {
            return urlString;
        }
    }

    /**
     * Cosmetic filter injection script
     */
    public static final String COSMETIC_CSS_INJECTION =
            "(function() {" +
            "  if (document.getElementById('caspian-ad-shield-style')) return;" +
            "  var style = document.createElement('style');" +
            "  style.id = 'caspian-ad-shield-style';" +
            "  style.innerHTML = '.ad, .ads, .adsbygoogle, [id^=\"google_ads_\"], [class*=\"ad-banner\"], [class*=\"sponsored\"], [aria-label*=\"advertisement\"] { display: none !important; }';" +
            "  (document.head || document.documentElement).appendChild(style);" +
            "})();";
}
