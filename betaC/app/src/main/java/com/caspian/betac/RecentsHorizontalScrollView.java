package com.caspian.betac;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.HorizontalScrollView;

/**
 * RecentsHorizontalScrollView - Snappy, fluid horizontal scroll container for the
 * Recents Tab Switcher cards. Replaces heavy, sluggish Android fling physics with
 * calibrated 1-card paging, instant touch-down braking, and seamless yielding to
 * upward swipe-to-dismiss gestures.
 */
public class RecentsHorizontalScrollView extends HorizontalScrollView {

    private float mDownX = 0f;
    private float mDownY = 0f;
    private int mTouchSlop = 0;
    private boolean mIsSwipingUp = false;
    private ValueAnimator mScrollAnimator = null;

    public RecentsHorizontalScrollView(Context context) {
        super(context);
        init();
    }

    public RecentsHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RecentsHorizontalScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        setOverScrollMode(OVER_SCROLL_NEVER);
    }

    private ViewGroup getContainer() {
        if (getChildCount() > 0 && getChildAt(0) instanceof ViewGroup) {
            return (ViewGroup) getChildAt(0);
        }
        return null;
    }

    public void stopScrollAnimation() {
        if (mScrollAnimator != null) {
            mScrollAnimator.cancel();
            mScrollAnimator = null;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mTouchSlop == 0) {
            mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        }

        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = ev.getX();
                mDownY = ev.getY();
                mIsSwipingUp = false;
                stopScrollAnimation();
                super.onInterceptTouchEvent(ev);
                return false;

            case MotionEvent.ACTION_MOVE:
                if (mIsSwipingUp) {
                    return false;
                }
                float dx = ev.getX() - mDownX;
                float dy = ev.getY() - mDownY;

                // Priority check: If gesture is moving upwards to dismiss a card,
                // do NOT intercept. Yield completely so child card can track smoothly.
                if (dy < -mTouchSlop && Math.abs(dy) > Math.abs(dx) * 0.85f) {
                    mIsSwipingUp = true;
                    return false;
                }

                if (Math.abs(dx) > mTouchSlop && Math.abs(dx) > Math.abs(dy)) {
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsSwipingUp = false;
                break;
        }

        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
            stopScrollAnimation();
        }

        boolean handled = super.onTouchEvent(ev);

        if (ev.getActionMasked() == MotionEvent.ACTION_UP || ev.getActionMasked() == MotionEvent.ACTION_CANCEL) {
            // If slow release without fling, snap to nearest card promptly
            postDelayed(() -> {
                if (mScrollAnimator == null || !mScrollAnimator.isRunning()) {
                    snapToNearestCard();
                }
            }, 35);
        }

        return handled;
    }

    @Override
    public void fling(int velocityX) {
        // Intercept native fling: instead of coasting endlessly through multiple cards,
        // snap cleanly to the adjacent card based on fling direction.
        snapWithFlingVelocity(velocityX);
    }

    public void snapWithFlingVelocity(int velocityX) {
        stopScrollAnimation();
        ViewGroup container = getContainer();
        if (container == null || container.getChildCount() == 0) return;

        int currentScrollX = getScrollX();
        int viewportW = getWidth();
        if (viewportW <= 0) return;
        int currentCenter = currentScrollX + (viewportW / 2);

        int childCount = container.getChildCount();
        int closestIndex = 0;
        int minDistance = Integer.MAX_VALUE;

        for (int i = 0; i < childCount; i++) {
            View child = container.getChildAt(i);
            if (child == null || child.getVisibility() != View.VISIBLE) continue;
            int childCenter = child.getLeft() + (child.getWidth() / 2);
            int dist = Math.abs(childCenter - currentCenter);
            if (dist < minDistance) {
                minDistance = dist;
                closestIndex = i;
            }
        }

        int targetIndex = closestIndex;
        // Positive velocityX in fling() = scroll to right (next cards)
        // Negative velocityX in fling() = scroll to left (prev cards)
        if (velocityX > 300) {
            targetIndex = Math.min(childCount - 1, closestIndex + 1);
        } else if (velocityX < -300) {
            targetIndex = Math.max(0, closestIndex - 1);
        }

        smoothScrollToCardIndex(targetIndex);
    }

    public void snapToNearestCard() {
        ViewGroup container = getContainer();
        if (container == null || container.getChildCount() == 0) return;

        int currentScrollX = getScrollX();
        int viewportW = getWidth();
        if (viewportW <= 0) return;
        int currentCenter = currentScrollX + (viewportW / 2);

        int childCount = container.getChildCount();
        int closestIndex = 0;
        int minDistance = Integer.MAX_VALUE;

        for (int i = 0; i < childCount; i++) {
            View child = container.getChildAt(i);
            if (child == null || child.getVisibility() != View.VISIBLE) continue;
            int childCenter = child.getLeft() + (child.getWidth() / 2);
            int dist = Math.abs(childCenter - currentCenter);
            if (dist < minDistance) {
                minDistance = dist;
                closestIndex = i;
            }
        }

        smoothScrollToCardIndex(closestIndex);
    }

    public void smoothScrollToCardIndex(int index) {
        ViewGroup container = getContainer();
        if (container == null || index < 0 || index >= container.getChildCount()) return;
        View target = container.getChildAt(index);
        if (target == null) return;

        int viewportW = getWidth();
        if (viewportW <= 0) return;
        int targetScrollX = target.getLeft() - ((viewportW - target.getWidth()) / 2);
        targetScrollX = Math.max(0, targetScrollX);

        animateScrollTo(targetScrollX);
    }

    public void animateScrollTo(int targetX) {
        stopScrollAnimation();
        int startX = getScrollX();
        if (Math.abs(startX - targetX) <= 2) {
            scrollTo(targetX, 0);
            return;
        }

        mScrollAnimator = ValueAnimator.ofInt(startX, targetX);
        int distance = Math.abs(targetX - startX);
        // Snappy, swift response (180ms - 240ms)
        int duration = Math.min(240, Math.max(180, (int) (distance * 0.35f)));
        mScrollAnimator.setDuration(duration);
        mScrollAnimator.setInterpolator(new DecelerateInterpolator(1.8f));
        mScrollAnimator.addUpdateListener(animation -> {
            int val = (int) animation.getAnimatedValue();
            scrollTo(val, 0);
        });
        mScrollAnimator.start();
    }
}
