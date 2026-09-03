package com.caspian.betac;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebView;

/**
 * Enhanced WebView for Caspian Flow that intercepts window visibility changes
 * to preserve continuous background audio/video playback when the app is minimized.
 */
public class CaspianWebView extends WebView {
    private boolean isBackgroundPlaybackEnabled = true;

    public CaspianWebView(Context context) {
        super(context);
    }

    public CaspianWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CaspianWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setBackgroundPlaybackEnabled(boolean enabled) {
        this.isBackgroundPlaybackEnabled = enabled;
    }

    public boolean isBackgroundPlaybackEnabled() {
        return isBackgroundPlaybackEnabled;
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        // When app is minimized or window visibility changes to GONE/INVISIBLE,
        // intercept and pass View.VISIBLE so Chromium's native media engine keeps playing!
        if (isBackgroundPlaybackEnabled && (visibility == View.GONE || visibility == View.INVISIBLE)) {
            super.onWindowVisibilityChanged(View.VISIBLE);
            return;
        }
        super.onWindowVisibilityChanged(visibility);
    }
}
