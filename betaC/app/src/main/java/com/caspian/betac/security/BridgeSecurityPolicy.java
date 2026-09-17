package com.caspian.betac.security;

import android.util.Log;

/**
 * Access control policy for Javascript-to-Java bridge invocations.
 * Determines whether a given tab/caller URL is authorized to execute a specific bridge method.
 */
public class BridgeSecurityPolicy {
    private static final String TAG = "BridgeSecurity";

    public enum Category {
        /**
         * Privileged actions restricted to first-party internal HTML assets
         * (e.g., browser_control.html, launch_hub.html, incognito_hub.html).
         * Includes tabs state, settings, preferences, casks, wallpapers, downloads management.
         */
        LOCAL_UI,

        /**
         * Restricted to local PDF viewer asset (pdf_viewer.html).
         */
        PDF_VIEWER,

        /**
         * Restricted to trusted media streaming services (e.g., YouTube).
         * Exposes playback state, time updates, and scrobbler sync.
         */
        TRUSTED_MEDIA,

        /**
         * Restricted to trusted AI chat platforms (e.g., ChatGPT, Gemini).
         */
        TRUSTED_AI,

        /**
         * Fully public / benign methods.
         */
        PUBLIC
    }

    /**
     * Checks if a bridge invocation from the given URL is permitted for the specified category.
     *
     * @param methodName The name of the method being invoked (for auditing/logging).
     * @param category The security category required by the method.
     * @param callerUrl The current verified URL of the tab or calling WebView.
     * @param isControlSheet True if the caller is the dedicated native control sheet WebView.
     * @return true if authorized, false if forbidden.
     */
    public static boolean isAuthorized(String methodName, Category category, String callerUrl, boolean isControlSheet) {
        // The native control sheet overlay only ever renders browser_control.html
        if (isControlSheet) {
            return category == Category.LOCAL_UI || category == Category.PUBLIC;
        }

        if (callerUrl == null || callerUrl.trim().isEmpty()) {
            Log.w(TAG, "BLOCKED bridge call to '" + methodName + "' - null/empty caller URL");
            return false;
        }

        switch (category) {
            case PUBLIC:
                return true;

            case LOCAL_UI:
                boolean isAsset = OriginVerifier.isLocalAsset(callerUrl);
                if (!isAsset) {
                    Log.e(TAG, "SECURITY VIOLATION: Remote URL '" + callerUrl + "' attempted privileged LOCAL_UI method: " + methodName);
                }
                return isAsset;

            case PDF_VIEWER:
                boolean isPdf = OriginVerifier.isPdfViewer(callerUrl);
                if (!isPdf) {
                    Log.e(TAG, "SECURITY VIOLATION: URL '" + callerUrl + "' attempted PDF_VIEWER method: " + methodName);
                }
                return isPdf;

            case TRUSTED_MEDIA:
                boolean isMedia = OriginVerifier.isTrustedMediaHost(callerUrl);
                if (!isMedia) {
                    Log.e(TAG, "SECURITY VIOLATION: URL '" + callerUrl + "' attempted TRUSTED_MEDIA method: " + methodName);
                }
                return isMedia;

            case TRUSTED_AI:
                boolean isAi = OriginVerifier.isTrustedAiHost(callerUrl);
                if (!isAi) {
                    Log.e(TAG, "SECURITY VIOLATION: URL '" + callerUrl + "' attempted TRUSTED_AI method: " + methodName);
                }
                return isAi;

            default:
                Log.w(TAG, "BLOCKED unknown security category for method: " + methodName);
                return false;
        }
    }
}
