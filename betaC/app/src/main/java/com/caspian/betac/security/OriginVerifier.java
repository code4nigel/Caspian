package com.caspian.betac.security;

import java.io.File;
import java.net.URI;
import java.util.Locale;

/**
 * Validates URLs, origins, and file paths to enforce security boundaries
 * between local assets, trusted services (YouTube, ChatGPT, Gemini), and untrusted remote web pages.
 */
public class OriginVerifier {

    public static final String LOCAL_ASSET_PREFIX = "file:///android_asset/";
    public static final String PDF_VIEWER_URL = "file:///android_asset/pdf_viewer.html";
    public static final String LAUNCH_HUB_URL = "file:///android_asset/launch_hub.html";
    public static final String INCOGNITO_HUB_URL = "file:///android_asset/incognito_hub.html";
    public static final String CONTROL_SHEET_URL = "file:///android_asset/browser_control.html";

    /**
     * Checks if the URL points directly to an internal bundled Android asset.
     */
    public static boolean isLocalAsset(String url) {
        if (url == null) return false;
        return url.startsWith(LOCAL_ASSET_PREFIX);
    }

    /**
     * Checks if the URL is the local PDF viewer.
     */
    public static boolean isPdfViewer(String url) {
        if (url == null) return false;
        return url.startsWith(PDF_VIEWER_URL);
    }

    /**
     * Checks if the URL belongs to a trusted media domain (YouTube).
     */
    public static boolean isTrustedMediaHost(String url) {
        if (url == null || url.trim().isEmpty()) return false;
        try {
            URI uri = URI.create(url);
            String host = uri.getHost();
            if (host == null) return false;
            host = host.toLowerCase(Locale.ROOT);
            return host.equals("youtube.com") ||
                   host.equals("www.youtube.com") ||
                   host.equals("m.youtube.com") ||
                   host.equals("music.youtube.com") ||
                   host.endsWith(".youtube.com");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if the URL belongs to an approved AI service (ChatGPT or Gemini).
     */
    public static boolean isTrustedAiHost(String url) {
        if (url == null || url.trim().isEmpty()) return false;
        try {
            URI uri = URI.create(url);
            String host = uri.getHost();
            if (host == null) return false;
            host = host.toLowerCase(Locale.ROOT);
            return host.equals("chatgpt.com") ||
                   host.equals("www.chatgpt.com") ||
                   host.endsWith(".chatgpt.com") ||
                   host.equals("gemini.google.com");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validates that a file path is a safe, readable PDF file and prevents path traversal attacks
     * targeting app private credentials, preferences, databases, or sensitive system files.
     */
    public static boolean isSafePdfPath(String path) {
        if (path == null || path.trim().isEmpty()) return false;
        
        // Block path traversal attempts
        if (path.contains("\0") || path.contains("..")) return false;

        String lower = path.toLowerCase(Locale.ROOT);
        if (!lower.endsWith(".pdf")) return false;

        // Block attempts to read app internal preference files, databases, or key stores disguised as PDF
        if (lower.contains("shared_prefs") || 
            lower.contains("databases") || 
            lower.contains("app_webview") || 
            lower.contains("/etc/") || 
            lower.contains("/proc/")) {
            return false;
        }

        try {
            File f = new File(path);
            String canonical = f.getCanonicalPath().toLowerCase(Locale.ROOT);
            if (!canonical.endsWith(".pdf")) return false;
            if (canonical.contains("shared_prefs") || 
                canonical.contains("databases") || 
                canonical.contains("app_webview") ||
                canonical.contains("/etc/") || 
                canonical.contains("/proc/")) {
                return false;
            }
            return f.exists() && f.isFile() && f.canRead();
        } catch (Exception e) {
            return false;
        }
    }
}
