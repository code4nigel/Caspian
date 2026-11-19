package com.caspian.betac.browser;

import android.content.Context;
import android.content.SharedPreferences;

import com.caspian.betac.AICommandRouter;

public final class BrowserPreferences {
    private final SharedPreferences prefs;

    public BrowserPreferences(Context context) {
        prefs = context.getSharedPreferences("CaspianFlowPrefs", Context.MODE_PRIVATE);
    }

    public boolean gesturesEnabled() {
        return prefs.getBoolean("browser_gestures", true);
    }

    public void setGesturesEnabled(boolean enabled) {
        prefs.edit().putBoolean("browser_gestures", enabled).apply();
    }

    public boolean reduceMotion() {
        return prefs.getBoolean("browser_reduce_motion", false);
    }

    public void setReduceMotion(boolean enabled) {
        prefs.edit().putBoolean("browser_reduce_motion", enabled).apply();
    }

    public AICommandRouter.SearchEngine searchEngine() {
        return searchEngine(prefs.getString("default_search_engine", "Google"));
    }

    public static AICommandRouter.SearchEngine searchEngine(String name) {
        for (AICommandRouter.SearchEngine engine : AICommandRouter.SearchEngine.values()) {
            if (engine.name.equalsIgnoreCase(name) || engine.name().equalsIgnoreCase(name)) return engine;
        }
        return AICommandRouter.SearchEngine.GOOGLE;
    }
}
