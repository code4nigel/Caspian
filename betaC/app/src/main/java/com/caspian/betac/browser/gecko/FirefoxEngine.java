package com.caspian.betac.browser.gecko;

import android.content.Context;
import android.net.Uri;

import org.mozilla.geckoview.AllowOrDeny;
import org.mozilla.geckoview.GeckoResult;
import org.mozilla.geckoview.GeckoRuntime;
import org.mozilla.geckoview.GeckoSession;
import org.mozilla.geckoview.GeckoSessionSettings;
import org.mozilla.geckoview.GeckoView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Owns Gecko sessions. Browser UI and WebView-specific bridges stay outside this class. */
final class FirefoxEngine implements AutoCloseable {
    private static final String EMPTY_PAGE = "about:blank";
    private static final int MAX_TABS = 32;
    private static GeckoRuntime runtime;

    static final class Tab {
        final GeckoSession session;
        String url = "";
        String title = "New tab";
        boolean canGoBack;
        boolean canGoForward;
        boolean loading;
        int progress;

        Tab(boolean isPrivate) {
            session = new GeckoSession(new GeckoSessionSettings.Builder().usePrivateMode(isPrivate)
                    .useTrackingProtection(true).build());
        }
    }

    private final List<Tab> tabs = new ArrayList<>();
    private final GeckoView renderer;
    private final boolean privateMode;
    private final Runnable stateChanged;
    private Tab selected;

    FirefoxEngine(Context context, GeckoView renderer, boolean privateMode, Runnable stateChanged) {
        if (runtime == null) runtime = GeckoRuntime.create(context.getApplicationContext());
        this.renderer = renderer;
        this.privateMode = privateMode;
        this.stateChanged = stateChanged;
    }

    List<Tab> tabs() {
        return Collections.unmodifiableList(tabs);
    }

    Tab selected() {
        return selected;
    }

    Tab addTab(String url, boolean activate) {
        if (tabs.size() >= MAX_TABS) return null;
        Tab tab = new Tab(privateMode);
        if (isWebUrl(url)) tab.url = url;
        register(tab);
        if (activate) select(tab);
        return tab;
    }

    void select(Tab tab) {
        if (!tabs.contains(tab)) return;
        if (renderer.getSession() != tab.session) {
            renderer.releaseSession();
            selected = tab;
            boolean needsLoading = !tab.session.isOpen();
            if (needsLoading) tab.session.open(runtime);
            renderer.setSession(tab.session);
            if (needsLoading) tab.session.loadUri(tab.url.isEmpty() ? EMPTY_PAGE : tab.url);
        }
        stateChanged.run();
    }

    private void register(Tab tab) {
        tabs.add(tab);
        tab.session.setNavigationDelegate(new GeckoSession.NavigationDelegate() {
            @Override public void onLocationChange(GeckoSession session, String url,
                    List<GeckoSession.PermissionDelegate.ContentPermission> permissions, Boolean userGesture) {
                tab.url = isWebUrl(url) ? url : "";
                changed(tab);
            }

            @Override public void onCanGoBack(GeckoSession session, boolean value) {
                tab.canGoBack = value;
                changed(tab);
            }

            @Override public void onCanGoForward(GeckoSession session, boolean value) {
                tab.canGoForward = value;
                changed(tab);
            }

            @Override public GeckoResult<AllowOrDeny> onLoadRequest(GeckoSession session, LoadRequest request) {
                return GeckoResult.fromValue(isWebUrl(request.uri) || EMPTY_PAGE.equals(request.uri)
                        || "data".equalsIgnoreCase(Uri.parse(request.uri).getScheme())
                        ? AllowOrDeny.ALLOW : AllowOrDeny.DENY);
            }

            @Override public GeckoResult<GeckoSession> onNewSession(GeckoSession session, String uri) {
                if (!isWebUrl(uri)) return GeckoResult.fromValue(null);
                Tab popup = addTab(uri, false);
                if (popup == null) return GeckoResult.fromValue(null);
                // Gecko opens and loads the returned session. Attach it after opening.
                return GeckoResult.fromValue(popup.session);
            }
        });
        tab.session.setContentDelegate(new GeckoSession.ContentDelegate() {
            @Override public void onTitleChange(GeckoSession session, String title) {
                if (title != null && !title.trim().isEmpty()) tab.title = title;
                changed(tab);
            }

            @Override public void onFocusRequest(GeckoSession session) {
                if (session.isOpen()) select(tab);
            }

            @Override public void onCloseRequest(GeckoSession session) {
                remove(tab);
            }

            @Override public void onCrash(GeckoSession session) {
                tab.loading = false;
                changed(tab);
            }
        });
        tab.session.setProgressDelegate(new GeckoSession.ProgressDelegate() {
            @Override public void onPageStart(GeckoSession session, String url) {
                tab.loading = true;
                tab.progress = 0;
                changed(tab);
            }

            @Override public void onProgressChange(GeckoSession session, int progress) {
                tab.progress = progress;
                changed(tab);
            }

            @Override public void onPageStop(GeckoSession session, boolean success) {
                tab.loading = false;
                changed(tab);
            }
        });
    }

    void remove(Tab tab) {
        int index = tabs.indexOf(tab);
        if (index < 0) return;
        tabs.remove(index);
        if (tab == selected) {
            renderer.releaseSession();
            selected = null;
        }
        if (tab.session.isOpen()) tab.session.close();
        if (selected == null) {
            if (tabs.isEmpty()) addTab("", true);
            else select(tabs.get(Math.min(index, tabs.size() - 1)));
        } else {
            stateChanged.run();
        }
    }

    void navigate(String url) {
        if (selected != null && isWebUrl(url)) selected.session.loadUri(url);
    }

    boolean switchBy(int offset) {
        if (tabs.size() < 2 || selected == null) return false;
        select(tabs.get(Math.floorMod(tabs.indexOf(selected) + offset, tabs.size())));
        return true;
    }

    static boolean isWebUrl(String url) {
        if (url == null || url.isEmpty()) return false;
        Uri uri = Uri.parse(url);
        return ("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme()))
                && uri.getHost() != null && !uri.getHost().isEmpty();
    }

    private void changed(Tab tab) {
        if (tabs.contains(tab)) stateChanged.run();
    }

    @Override public void close() {
        renderer.releaseSession();
        List<Tab> closing = new ArrayList<>(tabs);
        tabs.clear();
        selected = null;
        for (Tab tab : closing) if (tab.session.isOpen()) tab.session.close();
    }
}
