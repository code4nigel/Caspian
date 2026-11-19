package com.caspian.betac.browser;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.PathInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

public final class PodMenu extends ScrollView {
    private final View anchor;
    private final LinearLayout actions;
    private BrowserChrome chrome;
    private boolean open;
    private boolean motion;

    public PodMenu(FrameLayout root, View anchor) {
        super(root.getContext());
        this.anchor = anchor;
        setClipToOutline(true);
        setElevation(BrowserChrome.dp(getContext(), 590));
        setVisibility(GONE);
        setClickable(true);
        ViewCompat.setAccessibilityPaneTitle(this, "Caspian Pod menu");
        actions = new LinearLayout(getContext());
        actions.setOrientation(LinearLayout.VERTICAL);
        actions.setPadding(dp(6), dp(6), dp(6), dp(6));
        addView(actions);
        root.addView(this);
    }

    public void reset(BrowserChrome chrome, boolean motion) {
        this.chrome = chrome;
        this.motion = motion;
        actions.removeAllViews();
        setBackground(chrome.panel(getContext(), 24));
    }

    public void addAction(String label, int icon, Runnable action) {
        Button button = new Button(getContext(), null, android.R.attr.borderlessButtonStyle);
        button.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(48)));
        button.setText(label);
        button.setAllCaps(false);
        button.setTextColor(chrome.text);
        button.setTextSize(14f);
        button.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        button.setPaddingRelative(dp(10), 0, dp(8), 0);
        Drawable drawable = ContextCompat.getDrawable(getContext(), icon);
        if (drawable != null) {
            drawable = drawable.mutate();
            drawable.setTint(chrome.accent);
            drawable.setBounds(0, 0, dp(19), dp(19));
            button.setCompoundDrawablesRelative(drawable, null, null, null);
            button.setCompoundDrawablePadding(dp(10));
        }
        button.setOnClickListener(view -> {
            if (!open) return;
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            close(true);
            action.run();
        });
        actions.addView(button);
    }

    public void open(FrameLayout root) {
        int edge = dp(8);
        int width = Math.min(dp(218), Math.max(dp(48), root.getWidth() - edge * 2));
        int height = Math.min(dp(48) * actions.getChildCount() + dp(12), Math.max(dp(48), root.getHeight() - dp(72)));
        float x = anchor.getX() + anchor.getWidth() / 2f;
        float y = anchor.getY() + anchor.getHeight() / 2f;
        int left = Math.max(edge, Math.min(Math.round(x - width / 2f), root.getWidth() - width - edge));
        int above = Math.round(anchor.getY()) - height - dp(10);
        int below = Math.round(anchor.getY()) + anchor.getHeight() + dp(10);
        int top = Math.max(edge, Math.min(above >= edge ? above : below, root.getHeight() - height - dp(64)));
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height, Gravity.TOP | Gravity.LEFT);
        params.leftMargin = left;
        params.topMargin = top;
        setLayoutParams(params);
        scrollTo(0, 0);
        setPivotX(x - left);
        setPivotY(y - top);
        animate().cancel();
        setVisibility(VISIBLE);
        open = true;
        ViewCompat.setStateDescription(anchor, "Expanded");
        setScaleX(motion ? .88f : 1f);
        setScaleY(motion ? .76f : 1f);
        setAlpha(motion ? 0f : 1f);
        if (motion) animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(190)
                .setInterpolator(new PathInterpolator(.2f, .85f, .2f, 1f)).start();
    }

    public boolean isOpen() {
        return open;
    }

    public boolean close(boolean animate) {
        if (getVisibility() != VISIBLE) return false;
        open = false;
        ViewCompat.setStateDescription(anchor, "Collapsed");
        animate().cancel();
        if (animate && motion) {
            animate().scaleX(.88f).scaleY(.76f).alpha(0f).setDuration(130)
                    .withEndAction(() -> setVisibility(GONE)).start();
        } else {
            setVisibility(GONE);
        }
        return true;
    }

    public static boolean contains(View view, MotionEvent event) {
        if (view == null || !view.isShown()) return false;
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        return event.getRawX() >= location[0] && event.getRawX() < location[0] + view.getWidth()
                && event.getRawY() >= location[1] && event.getRawY() < location[1] + view.getHeight();
    }

    private int dp(float value) {
        return BrowserChrome.dp(getContext(), value);
    }
}
