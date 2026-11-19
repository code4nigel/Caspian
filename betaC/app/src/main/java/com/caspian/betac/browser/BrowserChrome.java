package com.caspian.betac.browser;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;

import java.util.Locale;

/** The browser's native and local WebView surfaces share this palette. */
public final class BrowserChrome {
    public final int background;
    public final int surface;
    public final int raised;
    public final int outline;
    public final int text;
    public final int muted;
    public final int accent;

    public BrowserChrome(boolean dark) {
        background = dark ? 0xff10191f : 0xffedf5f3;
        surface = dark ? 0xff19262c : 0xfff8fcfa;
        raised = dark ? 0xff24343a : 0xffe6f1ed;
        outline = dark ? 0xff3b5156 : 0xffb9cec7;
        text = dark ? 0xffecf8f3 : 0xff192d2a;
        muted = dark ? 0xffa5bdb9 : 0xff48635e;
        accent = dark ? 0xff8ce3cd : 0xff08705e;
    }

    public GradientDrawable panel(Context context, int radiusDp) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(surface);
        shape.setCornerRadius(dp(context, radiusDp));
        shape.setStroke(dp(context, 1), outline);
        return shape;
    }

    public static int dp(Context context, float value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    String cssArguments() {
        return String.format(Locale.ROOT, "'#%06x','#%06x','#%06x','#%06x','#%06x','#%06x','#%06x'",
                rgb(background), rgb(surface), rgb(raised), rgb(outline), rgb(text), rgb(muted), rgb(accent));
    }

    private static int rgb(int color) {
        return color & 0xffffff;
    }
}
