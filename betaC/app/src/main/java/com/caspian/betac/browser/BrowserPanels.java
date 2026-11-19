package com.caspian.betac.browser;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.animation.PathInterpolator;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.caspian.betac.security.OriginVerifier;

import org.json.JSONObject;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/** Owns the local browser destinations and their back stack. */
public final class BrowserPanels {
    public enum Destination { SHEET, SETTINGS, DOWNLOADS }

    private final View container;
    private final View backdrop;
    private final WebView web;
    private final BooleanSupplier motion;
    private final BooleanSupplier dark;
    private final Consumer<Destination> onOpen;
    private final Runnable onClosed;
    private final Deque<Destination> backStack = new ArrayDeque<>();
    private Destination destination;
    private String sheetTab;
    private boolean ready;

    @SuppressLint("SetJavaScriptEnabled")
    public BrowserPanels(View container, View backdrop, WebView web, Object bridge,
                         BooleanSupplier motion, BooleanSupplier dark, Consumer<Destination> onOpen, Runnable onClosed) {
        this.container = container;
        this.backdrop = backdrop;
        this.web = web;
        this.motion = motion;
        this.dark = dark;
        this.onOpen = onOpen;
        this.onClosed = onClosed;

        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDomStorageEnabled(true);
        web.getSettings().setAllowFileAccess(true);
        web.setBackgroundColor(0);
        web.setOverScrollMode(View.OVER_SCROLL_NEVER);
        web.setVerticalScrollBarEnabled(false);
        web.setHorizontalScrollBarEnabled(false);
        web.setWebViewClient(new WebViewClient() {
            @Override public void onPageStarted(WebView view, String url, Bitmap favicon) {
                ready = false;
            }

            @Override public void onPageFinished(WebView view, String url) {
                ready = OriginVerifier.CONTROL_SHEET_URL.equals(url);
                if (isOpen()) render();
            }

            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                return request.isForMainFrame() ? !OriginVerifier.CONTROL_SHEET_URL.equals(url) : !OriginVerifier.isLocalAsset(url);
            }
        });
        web.addJavascriptInterface(bridge, "CaspianBridge");
        web.loadUrl(OriginVerifier.CONTROL_SHEET_URL);
        backdrop.setOnClickListener(v -> back());
    }

    public boolean isOpen() {
        return destination != null;
    }

    public void open(Destination next, String tab) {
        boolean wasOpen = isOpen();
        if (wasOpen && destination != next) backStack.push(destination);
        if (next == Destination.SHEET) sheetTab = tab;
        destination = next;
        onOpen.accept(next);
        render();
        show(!wasOpen);
    }

    public boolean back() {
        if (!isOpen()) return false;
        if (!backStack.isEmpty()) {
            destination = backStack.pop();
            onOpen.accept(destination);
            render();
        } else {
            close();
        }
        return true;
    }

    public void close() {
        if (!isOpen()) return;
        destination = null;
        backStack.clear();
        stopAnimations();
        int duration = duration("sheetCloseDuration", 150);
        if (duration == 0) {
            finishClose();
            return;
        }
        backdrop.animate().alpha(0f).setDuration(duration).start();
        web.animate().alpha(0f).translationY(BrowserChrome.dp(web.getContext(), 16))
                .setDuration(duration).withEndAction(this::finishClose).start();
    }

    public void refreshTheme() {
        if (ready) {
            web.evaluateJavascript("window.setBrowserChrome?.(" + new BrowserChrome(dark.getAsBoolean()).cssArguments() + ");", null);
        }
    }

    private void render() {
        if (!ready || destination == null) return;
        refreshTheme();
        web.evaluateJavascript("window.showBrowserDestination?.(" + JSONObject.quote(destination.name().toLowerCase(java.util.Locale.ROOT))
                + "," + JSONObject.quote(sheetTab == null ? "" : sheetTab) + ");", null);
        sheetTab = null;
    }

    private void show(boolean animate) {
        stopAnimations();
        boolean wasHidden = container.getVisibility() != View.VISIBLE;
        container.setVisibility(View.VISIBLE);
        container.setClickable(true);
        container.setFocusable(true);
        if (wasHidden) {
            web.setAlpha(0f);
            web.setTranslationY(BrowserChrome.dp(web.getContext(), 24));
            backdrop.setAlpha(0f);
        }
        int duration = animate ? duration("sheetOpenDuration", 210) : 0;
        if (duration == 0) {
            backdrop.setAlpha(1f);
            web.setAlpha(1f);
            web.setTranslationY(0f);
            return;
        }
        backdrop.animate().alpha(1f).setDuration(duration).start();
        web.animate().alpha(1f).translationY(0f).setDuration(duration)
                .setInterpolator(new PathInterpolator(.2f, .85f, .2f, 1f)).start();
    }

    private void stopAnimations() {
        web.animate().cancel();
        backdrop.animate().cancel();
    }

    private void finishClose() {
        if (isOpen()) return;
        container.setVisibility(View.INVISIBLE);
        container.setClickable(false);
        container.setFocusable(false);
        onClosed.run();
    }

    private int duration(String key, int fallback) {
        if (!motion.getAsBoolean()) return 0;
        android.content.SharedPreferences prefs = web.getContext().getSharedPreferences("CaspianFlowPrefs", Context.MODE_PRIVATE);
        if ("none".equals(prefs.getString("sheetAnimationStyle", "slide"))) return 0;
        try {
            return Math.max(0, Math.min(1000, Integer.parseInt(prefs.getString(key, String.valueOf(fallback)))));
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }
}
