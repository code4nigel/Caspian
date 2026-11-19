package com.caspian.betac.browser;

import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

import com.caspian.betac.CabRadialMenuView;
import com.caspian.betac.CaspianPhysics;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/** Handles taps, repositioning and hold-and-slide shortcuts for the floating Pod. */
public final class PodGestures implements View.OnTouchListener {
    private final FrameLayout root;
    private final View anchor;
    private final BooleanSupplier motion;
    private final BooleanSupplier interruptVoice;
    private final Runnable closeMenu;
    private final Supplier<BrowserChrome> chrome;
    private final Supplier<List<CabRadialMenuView.ShortcutItem>> shortcuts;
    private final CabRadialMenuView.OnRadialActionSelectedListener selection;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable hold = this::showShortcuts;
    private final int[] rootPosition = new int[2];
    private VelocityTracker velocity;
    private CabRadialMenuView radial;
    private float startX, startY, offsetX, offsetY;
    private boolean dragging;

    public PodGestures(FrameLayout root, View anchor, BooleanSupplier motion, BooleanSupplier interruptVoice,
                       Runnable closeMenu, Supplier<BrowserChrome> chrome, Supplier<List<CabRadialMenuView.ShortcutItem>> shortcuts,
                       CabRadialMenuView.OnRadialActionSelectedListener selection) {
        this.root = root;
        this.anchor = anchor;
        this.motion = motion;
        this.interruptVoice = interruptVoice;
        this.closeMenu = closeMenu;
        this.chrome = chrome;
        this.shortcuts = shortcuts;
        this.selection = selection;
    }

    @Override public boolean onTouch(View view, MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                cancel();
                if (interruptVoice.getAsBoolean()) return true;
                velocity = VelocityTracker.obtain();
                velocity.addMovement(event);
                startX = event.getRawX();
                startY = event.getRawY();
                offsetX = anchor.getX() - startX;
                offsetY = anchor.getY() - startY;
                if (motion.getAsBoolean()) CaspianPhysics.applyPressSquish(anchor);
                handler.postDelayed(hold, ViewConfiguration.getLongPressTimeout());
                return true;
            case MotionEvent.ACTION_MOVE:
                if (velocity == null) return true;
                velocity.addMovement(event);
                if (radial != null) {
                    radial.updateTouch(event.getRawX() - rootPosition[0], event.getRawY() - rootPosition[1]);
                } else if (dragging || Math.hypot(event.getRawX() - startX, event.getRawY() - startY)
                        > ViewConfiguration.get(view.getContext()).getScaledTouchSlop()) {
                    if (!dragging) {
                        dragging = true;
                        closeMenu.run();
                        if (motion.getAsBoolean()) CaspianPhysics.applyReleasePop(anchor);
                    }
                    handler.removeCallbacks(hold);
                    anchor.setX(event.getRawX() + offsetX);
                    anchor.setY(event.getRawY() + offsetY);
                    if (motion.getAsBoolean()) {
                        velocity.computeCurrentVelocity(1000);
                        CaspianPhysics.applyDragTilt(anchor, velocity.getXVelocity(), 12f);
                    }
                }
                return true;
            case MotionEvent.ACTION_UP:
                handler.removeCallbacks(hold);
                if (velocity == null) return true;
                velocity.addMovement(event);
                if (radial != null) {
                    radial.finishGesture(selection);
                    radial = null;
                } else if (dragging) {
                    settle();
                } else {
                    release();
                    view.performClick();
                }
                endTracking();
                return true;
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_CANCEL:
                if (dragging && velocity != null) settle();
                cancel();
                return true;
            default:
                return true;
        }
    }

    private void showShortcuts() {
        if (velocity == null || dragging || !anchor.isShown()) return;
        closeMenu.run();
        release();
        root.getLocationOnScreen(rootPosition);
        radial = new CabRadialMenuView(root.getContext());
        radial.setPresentation(chrome.get(), motion.getAsBoolean());
        radial.setShortcuts(shortcuts.get());
        radial.showAt(anchor.getX() + anchor.getWidth() / 2f, anchor.getY() + anchor.getHeight() / 2f, root);
    }

    public void cancel() {
        handler.removeCallbacks(hold);
        if (radial != null) {
            radial.dismiss();
            radial = null;
        }
        if (velocity != null) release();
        endTracking();
    }

    private void settle() {
        int left = BrowserChrome.dp(root.getContext(), 8);
        int top = BrowserChrome.dp(root.getContext(), 24);
        float right = Math.max(left, root.getWidth() - anchor.getWidth() - left);
        float bottom = Math.max(top, root.getHeight() - anchor.getHeight() - top);
        if (motion.getAsBoolean()) {
            velocity.computeCurrentVelocity(1000);
            CaspianPhysics.flingToRestWithSpring(anchor, velocity.getXVelocity(), velocity.getYVelocity(), left, right, top, bottom);
        } else {
            anchor.setX(Math.max(left, Math.min(right, anchor.getX())));
            anchor.setY(Math.max(top, Math.min(bottom, anchor.getY())));
            anchor.setRotation(0f);
        }
    }

    private void release() {
        if (!motion.getAsBoolean()) return;
        CaspianPhysics.applyReleasePop(anchor);
        CaspianPhysics.resetDragTilt(anchor);
    }

    private void endTracking() {
        dragging = false;
        if (velocity != null) {
            velocity.recycle();
            velocity = null;
        }
    }
}
