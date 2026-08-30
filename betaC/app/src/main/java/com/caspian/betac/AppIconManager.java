package com.caspian.betac;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages Dynamic Launcher App Icons for Caspian Flow using Android Activity Aliases.
 */
public class AppIconManager {
    private static final String TAG = "AppIconManager";
    private static final String PREF_NAME = "caspian_app_icon_prefs";
    private static final String KEY_CURRENT_ICON = "current_icon_alias";

    public static class IconOption {
        public final String id;
        public final String title;
        public final String subtitle;
        public final String primaryColor;
        public final String aliasClassName;

        public IconOption(String id, String title, String subtitle, String primaryColor, String aliasClassName) {
            this.id = id;
            this.title = title;
            this.subtitle = subtitle;
            this.primaryColor = primaryColor;
            this.aliasClassName = aliasClassName;
        }

        public JSONObject toJson() {
            try {
                JSONObject obj = new JSONObject();
                obj.put("id", id);
                obj.put("title", title);
                obj.put("subtitle", subtitle);
                obj.put("primaryColor", primaryColor);
                return obj;
            } catch (Exception e) {
                return new JSONObject();
            }
        }
    }

    public static List<IconOption> getAvailableIcons(Context context) {
        String pkg = context.getPackageName();
        List<IconOption> list = new ArrayList<>();
        list.add(new IconOption("default", "Classic Wave", "Steel Blue & Slate Wave", "#5A94C7", pkg + ".AliasDefault"));
        list.add(new IconOption("cyber_cyan", "Cyber Obsidian", "Neon Cyan & Deep Abyss", "#00E5FF", pkg + ".AliasCyberCyan"));
        list.add(new IconOption("golden_shimmer", "Cosmic Gold", "Golden Amber & Royal Shimmer", "#FFD700", pkg + ".AliasGoldenShimmer"));
        list.add(new IconOption("midnight_violet", "Midnight Violet", "Deep Galaxy Purple & Neon Aura", "#C084FC", pkg + ".AliasMidnightViolet"));
        list.add(new IconOption("matrix_emerald", "Matrix Emerald", "Cyber Emerald & Obsidian Dark", "#10B981", pkg + ".AliasMatrixEmerald"));
        return list;
    }

    public static String getActiveIconId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_CURRENT_ICON, "default");
    }

    public static boolean setAppIcon(Context context, String iconId) {
        List<IconOption> icons = getAvailableIcons(context);
        IconOption targetIcon = null;
        for (IconOption icon : icons) {
            if (icon.id.equalsIgnoreCase(iconId)) {
                targetIcon = icon;
                break;
            }
        }

        if (targetIcon == null) {
            Log.e(TAG, "Unknown icon id: " + iconId);
            return false;
        }

        PackageManager pm = context.getPackageManager();

        try {
            // Enable target alias first
            pm.setComponentEnabledSetting(
                    new ComponentName(context, targetIcon.aliasClassName),
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
            );

            // Disable all other aliases
            for (IconOption icon : icons) {
                if (!icon.id.equalsIgnoreCase(iconId)) {
                    pm.setComponentEnabledSetting(
                            new ComponentName(context, icon.aliasClassName),
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                            PackageManager.DONT_KILL_APP
                    );
                }
            }

            // Save preference synchronously
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putString(KEY_CURRENT_ICON, targetIcon.id)
                    .commit();

            Log.i(TAG, "Successfully switched app icon to: " + targetIcon.title);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error switching app icon", e);
            return false;
        }
    }

    public static String getAvailableIconsJson(Context context) {
        List<IconOption> icons = getAvailableIcons(context);
        String currentId = getActiveIconId(context);
        JSONArray arr = new JSONArray();
        try {
            for (IconOption icon : icons) {
                JSONObject obj = icon.toJson();
                obj.put("isActive", icon.id.equalsIgnoreCase(currentId));
                arr.put(obj);
            }
        } catch (Exception ignored) {}
        return arr.toString();
    }
}
