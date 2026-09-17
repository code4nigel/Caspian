package com.caspian.betac.tabs;

import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles serialization, persistence, and safe recovery of browser tabs and session state.
 * Encapsulates the JSON schema and SharedPreferences keys previously scattered in MainActivity.
 */
public class TabStateRepository {
    private static final String TAG = "TabStateRepository";

    public static final String KEY_OPEN_TABS_JSON = "caspian_open_tabs_json";
    public static final String KEY_ACTIVE_TAB_ID = "caspian_active_tab_id";
    public static final String KEY_SECONDARY_SPLIT_ID = "caspian_secondary_split_id";
    public static final String KEY_SPLIT_MODE_STATE = "caspian_split_mode_state";
    public static final String KEY_SPLIT_RATIO = "caspian_split_ratio";
    public static final String KEY_NEXT_TAB_ID = "caspian_next_tab_id";

    public static final String DEFAULT_HUB_URL = "file:///android_asset/launch_hub.html";

    /**
     * Snapshot of the browser session.
     */
    public static class SessionSnapshot {
        public final List<TabState> tabs;
        public final int activeTabId;
        public final int secondarySplitId;
        public final int splitModeState;
        public final float splitRatio;
        public final int nextTabId;

        public SessionSnapshot(List<TabState> tabs, int activeTabId, int secondarySplitId,
                               int splitModeState, float splitRatio, int nextTabId) {
            this.tabs = tabs != null ? tabs : new ArrayList<>();
            this.activeTabId = activeTabId;
            this.secondarySplitId = secondarySplitId;
            this.splitModeState = splitModeState;
            this.splitRatio = splitRatio;
            this.nextTabId = nextTabId;
        }

        public static SessionSnapshot createDefault() {
            List<TabState> defaultTabs = new ArrayList<>();
            TabState initialTab = new TabState(1, "Caspian Hub", DEFAULT_HUB_URL, "hub", false);
            defaultTabs.add(initialTab);
            return new SessionSnapshot(defaultTabs, 1, -1, 0, 0.5f, 2);
        }
    }

    /**
     * Serializes a list of TabState objects into a JSON string, strictly excluding incognito tabs.
     */
    public static String serializeTabsToJson(List<TabState> tabs) {
        if (tabs == null) return "[]";
        JSONArray arr = new JSONArray();
        for (TabState tab : tabs) {
            if (tab == null || tab.isIncognito) continue;
            JSONObject obj = tab.toJson();
            if (obj != null) {
                arr.put(obj);
            }
        }
        return arr.toString();
    }

    /**
     * Deserializes tabs from a JSON string with safe error handling and fallback.
     */
    public static List<TabState> deserializeTabsFromJson(String tabsJson) {
        List<TabState> list = new ArrayList<>();
        if (tabsJson == null || tabsJson.trim().isEmpty()) {
            return list;
        }

        try {
            JSONArray arr = new JSONArray(tabsJson);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.optJSONObject(i);
                if (obj != null) {
                    TabState state = TabState.fromJson(obj, i + 1);
                    if (state != null) {
                        list.add(state);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "deserializeTabsFromJson failed: " + e.getMessage());
        }
        return list;
    }

    /**
     * Persists the current session to SharedPreferences.
     */
    public static void saveSession(SharedPreferences prefs, List<TabState> tabs,
                                   int activeTabId, int secondarySplitId,
                                   int splitModeState, float splitRatio, int nextTabId) {
        if (prefs == null) return;
        try {
            String json = serializeTabsToJson(tabs);
            prefs.edit()
                    .putString(KEY_OPEN_TABS_JSON, json)
                    .putInt(KEY_ACTIVE_TAB_ID, activeTabId)
                    .putInt(KEY_SECONDARY_SPLIT_ID, secondarySplitId)
                    .putInt(KEY_SPLIT_MODE_STATE, splitModeState)
                    .putFloat(KEY_SPLIT_RATIO, splitRatio)
                    .putInt(KEY_NEXT_TAB_ID, nextTabId)
                    .apply();
        } catch (Exception e) {
            Log.e(TAG, "saveSession failed: " + e.getMessage());
        }
    }

    /**
     * Restores a browser session from SharedPreferences, falling back to a default Hub session
     * if data is absent or corrupt.
     */
    public static SessionSnapshot restoreSession(SharedPreferences prefs) {
        if (prefs == null) {
            return SessionSnapshot.createDefault();
        }

        String tabsJson = prefs.getString(KEY_OPEN_TABS_JSON, null);
        int savedActiveId = prefs.getInt(KEY_ACTIVE_TAB_ID, 1);
        int savedSecondarySplitId = prefs.getInt(KEY_SECONDARY_SPLIT_ID, -1);
        int savedSplitMode = prefs.getInt(KEY_SPLIT_MODE_STATE, 0);
        float savedSplitRatio = prefs.getFloat(KEY_SPLIT_RATIO, 0.5f);
        int savedNextTabId = prefs.getInt(KEY_NEXT_TAB_ID, 2);

        List<TabState> tabs = deserializeTabsFromJson(tabsJson);
        if (tabs.isEmpty()) {
            return SessionSnapshot.createDefault();
        }

        // Validate max id
        int maxId = 0;
        boolean activeFound = false;
        boolean secondaryFound = false;
        for (TabState tab : tabs) {
            if (tab.id > maxId) maxId = tab.id;
            if (tab.id == savedActiveId) activeFound = true;
            if (tab.id == savedSecondarySplitId) secondaryFound = true;
        }

        int resolvedActiveId = activeFound ? savedActiveId : tabs.get(0).id;
        int resolvedSecondarySplitId = (savedSplitMode > 0 && secondaryFound && savedSecondarySplitId != resolvedActiveId)
                ? savedSecondarySplitId : -1;
        int resolvedSplitMode = (resolvedSecondarySplitId != -1) ? savedSplitMode : 0;
        int resolvedNextTabId = Math.max(savedNextTabId, maxId + 1);

        return new SessionSnapshot(
                tabs,
                resolvedActiveId,
                resolvedSecondarySplitId,
                resolvedSplitMode,
                savedSplitRatio,
                resolvedNextTabId
        );
    }
}
