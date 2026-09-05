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
        if (isBackgroundPlaybackEnabled && (visibility == View.GONE || visibility == View.INVISIBLE)) {
            super.onWindowVisibilityChanged(View.VISIBLE);
            return;
        }
        super.onWindowVisibilityChanged(visibility);
    }

    @Override
    public void dispatchWindowVisibilityChanged(int visibility) {
        if (isBackgroundPlaybackEnabled && (visibility == View.GONE || visibility == View.INVISIBLE)) {
            super.dispatchWindowVisibilityChanged(View.VISIBLE);
            return;
        }
        super.dispatchWindowVisibilityChanged(visibility);
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        if (isBackgroundPlaybackEnabled && (visibility == View.GONE || visibility == View.INVISIBLE)) {
            super.onVisibilityChanged(changedView, View.VISIBLE);
            return;
        }
        super.onVisibilityChanged(changedView, visibility);
    }

    @Override
    public int getWindowVisibility() {
        if (isBackgroundPlaybackEnabled) {
            return View.VISIBLE;
        }
        return super.getWindowVisibility();
    }

    @Override
    public boolean hasWindowFocus() {
        if (isBackgroundPlaybackEnabled) {
            return true;
        }
        return super.hasWindowFocus();
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        if (isBackgroundPlaybackEnabled) {
            super.onWindowFocusChanged(true);
            return;
        }
        super.onWindowFocusChanged(hasWindowFocus);
    }

    @Override
    public boolean isShown() {
        if (isBackgroundPlaybackEnabled) {
            return true;
        }
        return super.isShown();
    }
}
