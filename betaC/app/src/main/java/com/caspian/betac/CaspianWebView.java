package com.caspian.betac;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
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

    public interface OnScrollStateListener {
        void onScrollChanged(CaspianWebView webView, int scrollX, int scrollY, int oldScrollX, int oldScrollY);
        void onOverScrolled(CaspianWebView webView, int scrollX, int scrollY, boolean clampedX, boolean clampedY);
        void onScrollIdle(CaspianWebView webView);
        boolean onPreScrollDrag(CaspianWebView webView, float dragOffsetY);
        void onPreScrollDragEnd(CaspianWebView webView, float dragOffsetY);
    }

    public interface OnLinkLongPressListener {
        boolean onLinkLongPressed(CaspianWebView webView, String url, String extra);
    }

    private OnScrollStateListener scrollStateListener;
    private OnLinkLongPressListener linkLongPressListener;

    public void setScrollStateListener(OnScrollStateListener listener) {
        this.scrollStateListener = listener;
    }

    public void setOnLinkLongPressListener(OnLinkLongPressListener listener) {
        this.linkLongPressListener = listener;
        if (listener != null) {
            setOnLongClickListener(v -> {
                HitTestResult hr = getHitTestResult();
                if (hr != null) {
                    int type = hr.getType();
                    if (type == HitTestResult.SRC_ANCHOR_TYPE || type == HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
                        String url = hr.getExtra();
                        if (url != null && !url.trim().isEmpty() && !url.startsWith("javascript:")) {
                            return linkLongPressListener.onLinkLongPressed(this, url, null);
                        }
                    }
                }
                return false;
            });
        } else {
            setOnLongClickListener(null);
        }
    }

    public boolean isAtTop() {
        return computeVerticalScrollOffset() <= 0;
    }

    public boolean isAtBottom(int thresholdPx) {
        int offset = computeVerticalScrollOffset();
        int range = computeVerticalScrollRange();
        int extent = computeVerticalScrollExtent();
        return (offset + extent) >= (range - Math.max(0, thresholdPx));
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (scrollStateListener != null) {
            scrollStateListener.onScrollChanged(this, l, t, oldl, oldt);
        }
    }

    @Override
    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
        if (scrollStateListener != null) {
            scrollStateListener.onOverScrolled(this, scrollX, scrollY, clampedX, clampedY);
        }
    }

    private float touchDownRawY = 0f;
    private boolean isTouchDownAtTop = false;
    private boolean isTouchDownAtBottom = false;
    private boolean isHandlingPreScroll = false;
    private boolean preScrollPassedToSuper = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                touchDownRawY = event.getRawY();
                isTouchDownAtTop = (computeVerticalScrollOffset() <= 0);
                isTouchDownAtBottom = isAtBottom(30);
                isHandlingPreScroll = false;
                preScrollPassedToSuper = false;
                break;

            case MotionEvent.ACTION_MOVE:
                if (scrollStateListener != null) {
                    float dragOffsetY = touchDownRawY - event.getRawY();

                    if ((isTouchDownAtTop || isHandlingPreScroll) && !preScrollPassedToSuper) {
                        float touchSlop = android.view.ViewConfiguration.get(getContext()).getScaledTouchSlop();
                        if (isHandlingPreScroll || Math.abs(dragOffsetY) >= touchSlop) {
                            boolean consumed = scrollStateListener.onPreScrollDrag(this, dragOffsetY);
                            if (consumed) {
                                if (!isHandlingPreScroll) {
                                    isHandlingPreScroll = true;
                                    MotionEvent cancelEv = MotionEvent.obtain(event);
                                    cancelEv.setAction(MotionEvent.ACTION_CANCEL);
                                    super.onTouchEvent(cancelEv);
                                    cancelEv.recycle();
                                }
                                scrollTo(getScrollX(), 0);
                                return true;
                            } else {
                                // Pre-scroll drag finished or not consumed (e.g. fast swipe exceeded toolbarH immediately)
                                isHandlingPreScroll = false;
                                preScrollPassedToSuper = true;
                                touchDownRawY = event.getRawY();
                                MotionEvent fakeDown = MotionEvent.obtain(event);
                                fakeDown.setAction(MotionEvent.ACTION_DOWN);
                                super.onTouchEvent(fakeDown);
                                fakeDown.recycle();
                            }
                        }
                    }

                    if (isTouchDownAtTop && computeVerticalScrollOffset() <= 0) {
                        float dy = event.getRawY() - touchDownRawY;
                        if (dy > 45) { // User deliberately pulled down at the absolute top
                            scrollStateListener.onOverScrolled(this, 0, -100, false, true);
                        }
                    } else if (isTouchDownAtBottom && isAtBottom(30)) {
                        float dy = event.getRawY() - touchDownRawY;
                        if (dy < -45) { // User deliberately pulled up at the absolute bottom
                            scrollStateListener.onOverScrolled(this, 0, 100, false, true);
                        }
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (isHandlingPreScroll) {
                    isHandlingPreScroll = false;
                    preScrollPassedToSuper = false;
                    if (scrollStateListener != null) {
                        float dragOffsetY = touchDownRawY - event.getRawY();
                        scrollStateListener.onPreScrollDragEnd(this, dragOffsetY);
                    }
                    isTouchDownAtTop = false;
                    isTouchDownAtBottom = false;
                    return true;
                }
                isTouchDownAtTop = false;
                isTouchDownAtBottom = false;
                preScrollPassedToSuper = false;
                if (scrollStateListener != null) {
                    scrollStateListener.onScrollIdle(this);
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean isShown() {
        if (isBackgroundPlaybackEnabled) {
            return true;
        }
        return super.isShown();
    }
}
