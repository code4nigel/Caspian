package com.caspian.betac.browser;

import android.os.SystemClock;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import java.util.function.BooleanSupplier;

/** Interprets one-finger gestures originating on browser chrome. */
public final class BrowserGestures {
    public enum Swipe { UP, DOWN, LEFT, RIGHT, NONE }

    public interface Listener {
        boolean onSwipe(Swipe direction);
        void onTap();
    }

    private BrowserGestures() {}

    public static Swipe classify(float dx, float dy, float threshold) {
        float horizontal = Math.abs(dx);
        float vertical = Math.abs(dy);
        if (horizontal >= threshold && horizontal >= vertical * 1.18f) return dx < 0 ? Swipe.LEFT : Swipe.RIGHT;
        if (vertical >= threshold && vertical >= horizontal * 1.18f) return dy < 0 ? Swipe.UP : Swipe.DOWN;
        return Swipe.NONE;
    }

    public static View.OnTouchListener addressBar(BooleanSupplier trackTouch, BooleanSupplier gesturesEnabled,
                                                   Listener listener) {
        return new View.OnTouchListener() {
            private float x;
            private float y;
            private long started;
            private boolean tracking;

            @Override public boolean onTouch(View view, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        tracking = trackTouch.getAsBoolean();
                        if (!tracking) return false;
                        x = event.getRawX();
                        y = event.getRawY();
                        started = SystemClock.uptimeMillis();
                        return true;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        tracking = false;
                        return false;
                    case MotionEvent.ACTION_CANCEL:
                        tracking = false;
                        return false;
                    case MotionEvent.ACTION_UP:
                        if (!tracking) return false;
                        tracking = false;
                        float dx = event.getRawX() - x;
                        float dy = event.getRawY() - y;
                        float slop = ViewConfiguration.get(view.getContext()).getScaledTouchSlop();
                        Swipe swipe = classify(dx, dy, Math.max(slop, BrowserChrome.dp(view.getContext(), 26)));
                        if (gesturesEnabled.getAsBoolean() && swipe != Swipe.NONE && listener.onSwipe(swipe)) {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                        } else if (Math.hypot(dx, dy) <= slop && SystemClock.uptimeMillis() - started < 420) {
                            listener.onTap();
                        }
                        return true;
                    default:
                        return tracking;
                }
            }
        };
    }
}
