package com.caspian.betac;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.ViewFlipper;

public class SwipeableViewFlipper extends ViewFlipper {

    public interface OnPageChangeListener {
        void onPageChanged(int pageIndex);
    }

    private float startX = 0f;
    private float startY = 0f;
    private int touchSlop = 0;
    private boolean isSwiping = false;
    private OnPageChangeListener pageChangeListener = null;

    public SwipeableViewFlipper(Context context) {
        super(context);
        init(context);
    }

    public SwipeableViewFlipper(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public void setOnPageChangeListener(OnPageChangeListener listener) {
        this.pageChangeListener = listener;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                startX = ev.getX();
                startY = ev.getY();
                isSwiping = false;
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = ev.getX() - startX;
                float dy = ev.getY() - startY;
                if (Math.abs(dx) > touchSlop && Math.abs(dx) > Math.abs(dy) * 1.1f) {
                    isSwiping = true;
                    if (getParent() != null) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isSwiping = false;
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                startX = ev.getX();
                startY = ev.getY();
                isSwiping = false;
                return true;
            case MotionEvent.ACTION_MOVE:
                float dx = ev.getX() - startX;
                float dy = ev.getY() - startY;
                if (!isSwiping && Math.abs(dx) > touchSlop && Math.abs(dx) > Math.abs(dy) * 1.1f) {
                    isSwiping = true;
                    if (getParent() != null) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (isSwiping) {
                    float totalDx = ev.getX() - startX;
                    float minSwipeDistance = 40f * getResources().getDisplayMetrics().density;
                    if (totalDx < -minSwipeDistance) {
                        goToNextPage();
                    } else if (totalDx > minSwipeDistance) {
                        goToPreviousPage();
                    }
                    isSwiping = false;
                    return true;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                isSwiping = false;
                break;
        }
        return super.onTouchEvent(ev);
    }

    public void goToNextPage() {
        if (getDisplayedChild() < getChildCount() - 1) {
            setInAnimation(getContext(), R.anim.slide_in_right);
            setOutAnimation(getContext(), R.anim.slide_out_left);
            showNext();
            requestLayout();
            if (pageChangeListener != null) {
                pageChangeListener.onPageChanged(getDisplayedChild());
            }
        }
    }

    public void goToPreviousPage() {
        if (getDisplayedChild() > 0) {
            setInAnimation(getContext(), R.anim.slide_in_left);
            setOutAnimation(getContext(), R.anim.slide_out_right);
            showPrevious();
            requestLayout();
            if (pageChangeListener != null) {
                pageChangeListener.onPageChanged(getDisplayedChild());
            }
        }
    }

    public void setDisplayedChildWithAnim(int index) {
        int current = getDisplayedChild();
        if (index == current || index < 0 || index >= getChildCount()) return;
        if (index > current) {
            setInAnimation(getContext(), R.anim.slide_in_right);
            setOutAnimation(getContext(), R.anim.slide_out_left);
        } else {
            setInAnimation(getContext(), R.anim.slide_in_left);
            setOutAnimation(getContext(), R.anim.slide_out_right);
        }
        setDisplayedChild(index);
        requestLayout();
        if (pageChangeListener != null) {
            pageChangeListener.onPageChanged(index);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int maxHeight = 0;
        int count = getChildCount();
        int current = getDisplayedChild();
        android.view.View activeChild = (current >= 0 && current < count) ? getChildAt(current) : null;
        if (activeChild != null && activeChild.getVisibility() != GONE) {
            measureChildWithMargins(activeChild, widthMeasureSpec, 0, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), 0);
            maxHeight = activeChild.getMeasuredHeight();
        } else {
            for (int i = 0; i < count; i++) {
                android.view.View child = getChildAt(i);
                if (child != null && child.getVisibility() != GONE) {
                    measureChildWithMargins(child, widthMeasureSpec, 0, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), 0);
                    if (child.getMeasuredHeight() > maxHeight) {
                        maxHeight = child.getMeasuredHeight();
                    }
                }
            }
        }
        int desiredHeight = maxHeight + getPaddingTop() + getPaddingBottom();
        int finalHeight = resolveSize(desiredHeight, heightMeasureSpec);
        setMeasuredDimension(resolveSize(getDefaultSize(0, widthMeasureSpec), widthMeasureSpec), finalHeight);
    }
}
