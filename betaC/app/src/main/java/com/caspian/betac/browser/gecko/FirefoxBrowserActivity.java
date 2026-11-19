package com.caspian.betac.browser.gecko;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.caspian.betac.AICommandRouter;
import com.caspian.betac.R;
import com.caspian.betac.browser.BrowserChrome;
import com.caspian.betac.browser.BrowserGestures;
import com.caspian.betac.browser.BrowserPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.mozilla.geckoview.GeckoView;

public final class FirefoxBrowserActivity extends Activity {
    private static final String START_URL = "start_url";
    private static final String PRIVATE_MODE = "private_mode";
    private static final String DARK_CHROME = "dark_chrome";
    private static final String SAVED_TABS = "firefox_open_tabs";
    private static final String SELECTED_TAB = "firefox_selected_tab";

    private FirefoxEngine engine;
    private BrowserPreferences preferences;
    private BrowserChrome chrome;
    private boolean privateMode;
    private GeckoView renderer;
    private EditText address;
    private TextView empty;
    private ProgressBar progress;
    private ImageButton back;
    private ImageButton forward;
    private ImageButton reload;
    private Button tabCount;

    public static Intent intent(Context context, String url, boolean isPrivate, boolean dark) {
        return new Intent(context, FirefoxBrowserActivity.class)
                .putExtra(START_URL, url).putExtra(PRIVATE_MODE, isPrivate).putExtra(DARK_CHROME, dark);
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firefox_browser);
        preferences = new BrowserPreferences(this);
        privateMode = getIntent().getBooleanExtra(PRIVATE_MODE, false);
        chrome = new BrowserChrome(getIntent().getBooleanExtra(DARK_CHROME, true));
        renderer = findViewById(R.id.firefox_renderer);
        renderer.setViewBackend(GeckoView.BACKEND_TEXTURE_VIEW);
        address = findViewById(R.id.firefox_address);
        empty = findViewById(R.id.firefox_empty);
        progress = findViewById(R.id.firefox_progress);
        back = findViewById(R.id.firefox_back);
        forward = findViewById(R.id.firefox_forward);
        reload = findViewById(R.id.firefox_reload);
        tabCount = findViewById(R.id.firefox_tabs);
        style();
        bindControls();
        try {
            engine = new FirefoxEngine(this, renderer, privateMode, this::render);
        } catch (LinkageError | RuntimeException error) {
            android.util.Log.e("FirefoxBrowser", "Could not start GeckoView", error);
            Toast.makeText(this, "Firefox engine couldn't start on this device", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        restoreTabs();
    }

    private void style() {
        View root = findViewById(R.id.firefox_root);
        root.setBackgroundColor(chrome.background);
        findViewById(R.id.firefox_header).setBackgroundColor(chrome.surface);
        findViewById(R.id.firefox_rail).setBackgroundColor(chrome.surface);
        TextView title = findViewById(R.id.firefox_title);
        title.setTextColor(chrome.text);
        if (privateMode) title.setText("Firefox · Private");
        empty.setTextColor(chrome.muted);
        address.setTextColor(chrome.text);
        address.setHintTextColor(chrome.muted);
        address.setBackground(chrome.panel(this, 16));
        tabCount.setTextColor(chrome.accent);
        progress.setProgressTintList(ColorStateList.valueOf(chrome.accent));
        for (int id : new int[] {R.id.firefox_exit, R.id.firefox_back, R.id.firefox_forward,
                R.id.firefox_reload, R.id.firefox_new_tab}) {
            ((ImageButton) findViewById(id)).setColorFilter(chrome.accent);
        }
        getWindow().setStatusBarColor(chrome.surface);
        getWindow().setNavigationBarColor(chrome.surface);
    }

    private void bindControls() {
        findViewById(R.id.firefox_exit).setOnClickListener(v -> finish());
        findViewById(R.id.firefox_new_tab).setOnClickListener(v -> addTab());
        tabCount.setOnClickListener(v -> showTabs());
        back.setOnClickListener(v -> goBack());
        forward.setOnClickListener(v -> {
            FirefoxEngine.Tab tab = engine.selected();
            if (tab != null && tab.canGoForward) tab.session.goForward();
        });
        reload.setOnClickListener(v -> {
            FirefoxEngine.Tab tab = engine.selected();
            if (tab == null) return;
            if (tab.loading) tab.session.stop();
            else tab.session.reload();
        });
        address.setOnEditorActionListener((view, action, event) -> {
            if (action != EditorInfo.IME_ACTION_GO) return false;
            submitAddress();
            return true;
        });
        address.setOnTouchListener(BrowserGestures.addressBar(() -> !address.hasFocus(), preferences::gesturesEnabled,
                new BrowserGestures.Listener() {
                    @Override public boolean onSwipe(BrowserGestures.Swipe direction) {
                        switch (direction) {
                            case UP: showTabs(); return true;
                            case LEFT: return engine.switchBy(1);
                            case RIGHT: return engine.switchBy(-1);
                            case DOWN: return goBack();
                            default: return false;
                        }
                    }
                    @Override public void onTap() { focusAddress(); }
                }));
    }

    private void restoreTabs() {
        if (!privateMode) {
            SharedPreferences saved = getPreferences(MODE_PRIVATE);
            try {
                JSONArray urls = new JSONArray(saved.getString(SAVED_TABS, "[]"));
                for (int i = 0; i < urls.length(); i++) engine.addTab(urls.optString(i), false);
            } catch (JSONException ignored) {}
        }
        String start = getIntent().getStringExtra(START_URL);
        if (FirefoxEngine.isWebUrl(start)) {
            for (FirefoxEngine.Tab tab : engine.tabs()) {
                if (start.equals(tab.url)) {
                    engine.select(tab);
                    return;
                }
            }
            if (engine.addTab(start, true) != null) return;
        }
        if (engine.tabs().isEmpty()) engine.addTab("", true);
        else engine.select(engine.tabs().get(Math.max(0, Math.min(engine.tabs().size() - 1,
                getPreferences(MODE_PRIVATE).getInt(SELECTED_TAB, 0)))));
    }

    private void addTab() {
        if (engine.addTab("", true) == null) {
            Toast.makeText(this, "Close a Firefox tab to open another", Toast.LENGTH_SHORT).show();
        } else {
            address.setText("");
            focusAddress();
        }
    }

    private void focusAddress() {
        address.requestFocus();
        address.selectAll();
        InputMethodManager keyboard = getSystemService(InputMethodManager.class);
        if (keyboard != null) keyboard.showSoftInput(address, InputMethodManager.SHOW_IMPLICIT);
    }

    private void submitAddress() {
        String input = address.getText().toString().trim();
        if (input.isEmpty()) return;
        String url = AICommandRouter.resolve(input, preferences.searchEngine()).targetUrl;
        if (!FirefoxEngine.isWebUrl(url)) {
            address.setError("Enter a web address or search term");
            return;
        }
        InputMethodManager keyboard = getSystemService(InputMethodManager.class);
        if (keyboard != null) keyboard.hideSoftInputFromWindow(address.getWindowToken(), 0);
        address.clearFocus();
        renderer.requestFocus();
        engine.navigate(url);
    }

    private boolean goBack() {
        if (engine == null || engine.selected() == null || !engine.selected().canGoBack) return false;
        engine.selected().session.goBack();
        return true;
    }

    @Override public void onBackPressed() {
        if (!goBack()) finish();
    }

    private void render() {
        if (engine == null || engine.selected() == null) return;
        FirefoxEngine.Tab tab = engine.selected();
        if (!address.hasFocus() && !tab.url.contentEquals(address.getText())) address.setText(tab.url);
        empty.setVisibility(tab.url.isEmpty() && !tab.loading ? View.VISIBLE : View.GONE);
        back.setEnabled(tab.canGoBack);
        forward.setEnabled(tab.canGoForward);
        back.setAlpha(tab.canGoBack ? 1f : .4f);
        forward.setAlpha(tab.canGoForward ? 1f : .4f);
        reload.setImageResource(tab.loading ? R.drawable.ic_pod_close : R.drawable.ic_pod_reload);
        reload.setContentDescription(tab.loading ? "Stop loading" : "Reload");
        progress.setVisibility(tab.loading ? View.VISIBLE : View.INVISIBLE);
        progress.setProgress(tab.progress);
        tabCount.setText(Integer.toString(engine.tabs().size()));
    }

    private void showTabs() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout rows = new LinearLayout(this);
        rows.setOrientation(LinearLayout.VERTICAL);
        rows.setPadding(dp(12), 0, dp(12), dp(8));
        scroll.addView(rows);
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("Firefox tabs").setView(scroll)
                .setPositiveButton("New tab", (d, which) -> addTab()).setNegativeButton("Done", null).create();
        for (FirefoxEngine.Tab tab : engine.tabs()) {
            LinearLayout row = new LinearLayout(this);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setBackground(chrome.panel(this, 14));
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(-1, dp(54));
            rowParams.bottomMargin = dp(6);
            rows.addView(row, rowParams);

            Button select = new Button(this, null, android.R.attr.borderlessButtonStyle);
            select.setText(tab.title);
            select.setSingleLine(true);
            select.setEllipsize(android.text.TextUtils.TruncateAt.END);
            select.setAllCaps(false);
            select.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
            select.setTextColor(tab == engine.selected() ? chrome.accent : chrome.text);
            row.addView(select, new LinearLayout.LayoutParams(0, -1, 1));
            select.setOnClickListener(v -> {
                dialog.dismiss();
                address.clearFocus();
                engine.select(tab);
            });

            ImageButton close = new ImageButton(this);
            close.setImageResource(R.drawable.ic_pod_close);
            close.setColorFilter(chrome.accent);
            close.setContentDescription("Close " + tab.title);
            close.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            row.addView(close, new LinearLayout.LayoutParams(dp(48), -1));
            close.setOnClickListener(v -> {
                engine.remove(tab);
                rows.removeView(row);
                if (rows.getChildCount() == 0) dialog.dismiss();
            });
        }
        dialog.show();
    }

    @Override protected void onPause() {
        if (engine != null && !privateMode) {
            JSONArray urls = new JSONArray();
            for (FirefoxEngine.Tab tab : engine.tabs()) urls.put(tab.url);
            getPreferences(MODE_PRIVATE).edit().putString(SAVED_TABS, urls.toString())
                    .putInt(SELECTED_TAB, engine.tabs().indexOf(engine.selected())).apply();
        }
        super.onPause();
    }

    @Override protected void onDestroy() {
        if (engine != null) engine.close();
        super.onDestroy();
    }

    private int dp(int value) { return BrowserChrome.dp(this, value); }
}
