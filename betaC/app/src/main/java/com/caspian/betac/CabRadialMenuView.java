package com.caspian.betac;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.Base64;
import android.util.LruCache;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * CabRadialMenuView - Vertical Left-Side Semicircular "Outer Space Liquid Glass" Radial Planetarium
 * 
 * Specifically optimized for Caspian Action Button (CAB) docked at bottom-right corner:
 * - Vertical left-side semicircle (90° to 270°): 100% of controls are in open screen space to the left of CAB.
 * - Zero items to the right: Drift mic and Whirlpool never go off-screen.
 * - Celestial Planetary Orbit: Shortcuts look like luminous planets orbiting the core star (CAB) with atmospheric coronas and planetary rings.
 * - Outer Space Liquid Glass: Cosmic obsidian glass with nebula gradient, specular starlight rim, and radiant Caspian Cyan (#22D3EE) accents.
 * - Dynamic shortcut sync with Hub and integrated "⚙️ Configure Orbit" manager.
 */
public class CabRadialMenuView extends View {

    public interface OnRadialActionSelectedListener {
        void onActionSelected(int action);
        default void onShortcutSelected(String title, String url) {}
        void onCancelled();
    }

    public static class ShortcutItem {
        public final String title;
        public final String url;
        public final String rawIcon;
        public Bitmap bitmap;
        public final boolean isTabsOverview;
        public final boolean isConfigureOrbit;
        public final int tabCount;

        public ShortcutItem(String title, String url, String icon) {
            this.title = title;
            this.url = url;
            this.rawIcon = (icon != null && !icon.isEmpty()) ? icon : "🌐";
            this.isTabsOverview = false;
            this.isConfigureOrbit = false;
            this.tabCount = 0;
            this.bitmap = null;
        }

        public ShortcutItem(String title, int tabCount) {
            this.title = title;
            this.url = "";
            this.rawIcon = String.valueOf(tabCount);
            this.isTabsOverview = true;
            this.isConfigureOrbit = false;
            this.tabCount = tabCount;
            this.bitmap = null;
        }

        private ShortcutItem(String title, boolean isConfig) {
            this.title = title;
            this.url = "";
            this.rawIcon = "⚙️";
            this.isTabsOverview = false;
            this.isConfigureOrbit = isConfig;
            this.tabCount = 0;
            this.bitmap = null;
        }

        public static ShortcutItem createConfigureOrbitItem() {
            return new ShortcutItem("Edit Orbit", true);
        }

        public String getFallbackDisplayGlyph() {
            if (isConfigureOrbit) return "⚙️";
            if (isTabsOverview) return "🪐";
            if (rawIcon != null && rawIcon.length() <= 4 && !rawIcon.startsWith("http") && !rawIcon.startsWith("data:")) {
                return rawIcon;
            }
            if (title != null && !title.trim().isEmpty()) {
                return title.trim().substring(0, 1).toUpperCase();
            }
            return "🌐";
        }
    }

    // 9 Actions
    public static final int ACTION_NONE = 0;
    public static final int ACTION_CLOSE_TAB = 1;
    public static final int ACTION_SEARCH = 2;
    public static final int ACTION_BACK = 3;
    public static final int ACTION_RELOAD = 4;
    public static final int ACTION_FORWARD = 5;
    public static final int ACTION_NEW_TAB = 6;
    public static final int ACTION_WHIRLPOOL = 7;
    public static final int ACTION_DRIFT = 8;
    public static final int ACTION_TABS_OVERVIEW = 9;
    public static final int ACTION_CONFIGURE_ORBIT = 10;

    // 8 Core Actions along the vertical left-side semicircle (from 90° South to 270° North)
    private static final int[] INNER_ACTIONS = {
        ACTION_CLOSE_TAB,
        ACTION_SEARCH,
        ACTION_BACK,
        ACTION_RELOAD,
        ACTION_FORWARD,
        ACTION_NEW_TAB,
        ACTION_WHIRLPOOL,
        ACTION_DRIFT
    };

    private static final String[] INNER_ACTION_ICONS = {
        "✕",
        "🔍",
        "◀",
        "⟳",
        "▶",
        "＋",
        "🌀",
        "🎙️"
    };

    private static final String[] INNER_ACTION_LABELS = {
        "Close Tab",
        "Search Omnibox",
        "Go Back",
        "Reload Page",
        "Go Forward",
        "New Tab",
        "Whirlpool AI",
        "Drift Voice"
    };

    // Shared Favicon Memory Cache
    private static final LruCache<String, Bitmap> sFaviconCache = new LruCache<>(80);

    private float centerX = 0;
    private float centerY = 0;
    private float density;

    // Geometry Radii (Optimized for left-side one-handed ergonomics)
    private float innerDeadzoneRadius;
    private float actionInnerRadius;
    private float actionCenterRadius;
    private float actionOuterRadius;
    private float outerOrbitRadius;
    private float bubbleRadius;

    private float animScale = 0f;

    private int selectedAction = ACTION_NONE;
    private int selectedShortcutIndex = -1;
    private int previousAction = ACTION_NONE;
    private int previousShortcutIndex = -1;

    private final List<ShortcutItem> shortcutsList = new ArrayList<>();

    // Paints (Outer Space Liquid Glass & Celestial Planetarium styling)
    private final Paint dialBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dialBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dialGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint chordLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint orbitalTrackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint sectorHighlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint sectorBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dividerLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint reticleRingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint reticleDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // Planet Paints
    private final Paint planetAtmospherePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint planetSpherePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint planetRingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint planetHighlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Paint actionIconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint actionLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tooltipBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tooltipBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tooltipTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint subTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

    private final RectF outerDialRect = new RectF();
    private final RectF innerDialRect = new RectF();
    private final RectF actionCenterRect = new RectF();
    private final RectF outerOrbitRect = new RectF();
    private final RectF tooltipRect = new RectF();
    private final Path semicircularPath = new Path();
    private final Path sectorPath = new Path();

    // Semicircle geometry pointing strictly to the LEFT (90° South to 270° North)
    private static final float SEMICIRCLE_START_ANGLE = 90f;
    private static final float SEMICIRCLE_SWEEP_ANGLE = 180f;

    public CabRadialMenuView(Context context) {
        super(context);
        init();
    }

    private void init() {
        setLayerType(LAYER_TYPE_HARDWARE, null);
        density = getResources().getDisplayMetrics().density;

        innerDeadzoneRadius = 38f * density;
        actionInnerRadius = 42f * density;
        actionCenterRadius = 80f * density;
        actionOuterRadius = 118f * density;
        outerOrbitRadius = 160f * density;
        bubbleRadius = 22f * density;

        // 1. Outer Space Liquid Glass Background
        dialBgPaint.setStyle(Paint.Style.FILL);

        // Specular Starlight Rim
        dialBorderPaint.setStyle(Paint.Style.STROKE);
        dialBorderPaint.setStrokeWidth(1.8f * density);
        dialBorderPaint.setColor(Color.parseColor("#55FFFFFF")); // Crisp specular starlight rim

        // Ambient Cyan Halo Glow
        dialGlowPaint.setStyle(Paint.Style.STROKE);
        dialGlowPaint.setStrokeWidth(3.8f * density);
        dialGlowPaint.setColor(Color.parseColor("#3322D3EE")); // Celestial cyan nebula glow

        chordLinePaint.setStyle(Paint.Style.STROKE);
        chordLinePaint.setStrokeWidth(1.2f * density);
        chordLinePaint.setColor(Color.parseColor("#26FFFFFF"));

        // Orbital Track Rings
        orbitalTrackPaint.setStyle(Paint.Style.STROKE);
        orbitalTrackPaint.setStrokeWidth(1f * density);
        orbitalTrackPaint.setColor(Color.parseColor("#1AFFFFFF"));

        // 2. Active Sector Highlighting (Celestial Cyan Aurora)
        sectorHighlightPaint.setStyle(Paint.Style.FILL);
        sectorHighlightPaint.setColor(Color.parseColor("#4422D3EE"));

        sectorBorderPaint.setStyle(Paint.Style.STROKE);
        sectorBorderPaint.setStrokeWidth(2f * density);
        sectorBorderPaint.setColor(Color.parseColor("#CC22D3EE"));

        dividerLinePaint.setStyle(Paint.Style.STROKE);
        dividerLinePaint.setStrokeWidth(1f * density);
        dividerLinePaint.setColor(Color.parseColor("#1AFFFFFF"));

        // 3. Central Core Reticle (The Star/Sun)
        reticleRingPaint.setStyle(Paint.Style.STROKE);
        reticleRingPaint.setStrokeWidth(1.8f * density);
        reticleRingPaint.setColor(Color.parseColor("#6622D3EE"));

        reticleDotPaint.setStyle(Paint.Style.FILL);
        reticleDotPaint.setColor(Color.parseColor("#22D3EE"));

        // 4. Celestial Planets Paints
        planetAtmospherePaint.setStyle(Paint.Style.FILL);
        planetSpherePaint.setStyle(Paint.Style.FILL);

        planetRingPaint.setStyle(Paint.Style.STROKE);
        planetRingPaint.setStrokeWidth(1.4f * density);
        planetRingPaint.setColor(Color.parseColor("#4D22D3EE")); // Luminous planetary ring

        planetHighlightPaint.setStyle(Paint.Style.STROKE);
        planetHighlightPaint.setStrokeWidth(1.5f * density);
        planetHighlightPaint.setColor(Color.parseColor("#44FFFFFF"));

        // 5. Typography & Tooltips
        actionIconPaint.setColor(Color.WHITE);
        actionIconPaint.setTextSize(16.5f * density);
        actionIconPaint.setTextAlign(Paint.Align.CENTER);
        actionIconPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        actionLabelPaint.setColor(Color.parseColor("#E0F2FE"));
        actionLabelPaint.setTextSize(9.5f * density);
        actionLabelPaint.setTextAlign(Paint.Align.CENTER);
        actionLabelPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        tooltipBgPaint.setStyle(Paint.Style.FILL);
        tooltipBgPaint.setColor(Color.parseColor("#F5050811")); // Cosmic obsidian capsule

        tooltipBorderPaint.setStyle(Paint.Style.STROKE);
        tooltipBorderPaint.setStrokeWidth(1.2f * density);
        tooltipBorderPaint.setColor(Color.parseColor("#6622D3EE"));

        tooltipTextPaint.setColor(Color.WHITE);
        tooltipTextPaint.setTextSize(11f * density);
        tooltipTextPaint.setTextAlign(Paint.Align.CENTER);
        tooltipTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        subTextPaint.setColor(Color.parseColor("#94A3B8"));
        subTextPaint.setTextSize(8.5f * density);
        subTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setShortcuts(List<ShortcutItem> items) {
        shortcutsList.clear();
        if (items != null) {
            shortcutsList.addAll(items);
            int targetSize = (int) (bubbleRadius * 1.5f);

            for (ShortcutItem item : shortcutsList) {
                if (item.isTabsOverview || item.isConfigureOrbit || item.rawIcon == null) continue;

                if (item.rawIcon.startsWith("data:image")) {
                    try {
                        int commaIdx = item.rawIcon.indexOf(",");
                        String b64 = commaIdx >= 0 ? item.rawIcon.substring(commaIdx + 1) : item.rawIcon;
                        byte[] decoded = Base64.decode(b64, Base64.DEFAULT);
                        Bitmap bmp = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                        if (bmp != null) {
                            item.bitmap = getCircularCroppedBitmap(bmp, targetSize);
                        }
                    } catch (Exception ignored) {}
                } else if (item.rawIcon.startsWith("http://") || item.rawIcon.startsWith("https://")) {
                    Bitmap cached = sFaviconCache.get(item.rawIcon);
                    if (cached != null) {
                        item.bitmap = cached;
                    } else {
                        final String fetchUrl = item.rawIcon;
                        new Thread(() -> {
                            try {
                                URL u = new URL(fetchUrl);
                                HttpURLConnection conn = (HttpURLConnection) u.openConnection();
                                conn.setConnectTimeout(3000);
                                conn.setReadTimeout(3000);
                                conn.setDoInput(true);
                                conn.connect();
                                InputStream is = conn.getInputStream();
                                Bitmap bmp = BitmapFactory.decodeStream(is);
                                is.close();
                                conn.disconnect();
                                if (bmp != null) {
                                    Bitmap circular = getCircularCroppedBitmap(bmp, targetSize);
                                    sFaviconCache.put(fetchUrl, circular);
                                    post(() -> {
                                        item.bitmap = circular;
                                        invalidate();
                                    });
                                }
                            } catch (Exception ignored) {}
                        }).start();
                    }
                }
            }
        }
        invalidate();
    }

    private static Bitmap getCircularCroppedBitmap(Bitmap bitmap, int diameter) {
        if (bitmap == null || diameter <= 0) return null;
        try {
            Bitmap output = Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            Rect srcRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            Rect dstRect = new Rect(0, 0, diameter, diameter);
            canvas.drawCircle(diameter / 2f, diameter / 2f, diameter / 2f, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, srcRect, dstRect, paint);
            return output;
        } catch (Exception e) {
            return bitmap;
        }
    }

    public void showAt(float cx, float cy, ViewGroup parent) {
        this.centerX = cx;
        this.centerY = cy;
        this.selectedAction = ACTION_NONE;
        this.selectedShortcutIndex = -1;
        this.previousAction = ACTION_NONE;
        this.previousShortcutIndex = -1;

        if (getParent() == null && parent != null) {
            parent.addView(this, new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));
            setElevation(300f);
        }

        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(220);
        animator.setInterpolator(new OvershootInterpolator(1.2f));
        animator.addUpdateListener(animation -> {
            animScale = (float) animation.getAnimatedValue();
            invalidate();
        });
        animator.start();
        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
    }

    public void updateTouch(float touchX, float touchY) {
        float dx = touchX - centerX;
        float dy = touchY - centerY;
        float dist = (float) Math.hypot(dx, dy);

        int newAction = ACTION_NONE;
        int newShortcut = -1;

        if (dist < innerDeadzoneRadius) {
            // Inside deadzone -> Release to cancel
            newAction = ACTION_NONE;
            newShortcut = -1;
        } else if (dist >= innerDeadzoneRadius && dist < actionOuterRadius + 14f * density) {
            // Inside Left Semicircle Actions
            float angle = (float) Math.toDegrees(Math.atan2(dy, dx));
            if (angle < 0) angle += 360f; // 0 to 360

            // Left semicircle spans from 90° (South) to 270° (North)
            if (angle >= SEMICIRCLE_START_ANGLE && angle <= (SEMICIRCLE_START_ANGLE + SEMICIRCLE_SWEEP_ANGLE)) {
                float rel = angle - SEMICIRCLE_START_ANGLE;
                float step = SEMICIRCLE_SWEEP_ANGLE / INNER_ACTIONS.length;
                int idx = (int) (rel / step);
                if (idx >= 0 && idx < INNER_ACTIONS.length) {
                    newAction = INNER_ACTIONS[idx];
                }
            }
        } else {
            // Inside Planetary Orbit
            float angle = (float) Math.toDegrees(Math.atan2(dy, dx));
            if (angle < 0) angle += 360f;

            int count = shortcutsList.size();
            if (count > 0 && angle >= 95f && angle <= 265f) {
                float orbitSweep = 170f;
                float step = orbitSweep / count;
                float rel = angle - 95f;
                int sIdx = (int) (rel / step);
                if (sIdx >= 0 && sIdx < count) {
                    newShortcut = sIdx;
                }
            }
        }

        if (newAction != selectedAction || newShortcut != selectedShortcutIndex) {
            selectedAction = newAction;
            selectedShortcutIndex = newShortcut;
            if (selectedAction != previousAction || selectedShortcutIndex != previousShortcutIndex) {
                performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            }
            previousAction = selectedAction;
            previousShortcutIndex = selectedShortcutIndex;
            invalidate();
        }
    }

    public void finishGesture(OnRadialActionSelectedListener listener) {
        final int finalAction = selectedAction;
        final int finalShortcut = selectedShortcutIndex;

        ValueAnimator animator = ValueAnimator.ofFloat(animScale, 0f);
        animator.setDuration(160);
        animator.addUpdateListener(animation -> {
            animScale = (float) animation.getAnimatedValue();
            invalidate();
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                dismiss();
                if (listener != null) {
                    if (finalShortcut >= 0 && finalShortcut < shortcutsList.size()) {
                        ShortcutItem item = shortcutsList.get(finalShortcut);
                        if (item.isConfigureOrbit) {
                            listener.onActionSelected(ACTION_CONFIGURE_ORBIT);
                        } else if (item.isTabsOverview) {
                            listener.onActionSelected(ACTION_TABS_OVERVIEW);
                        } else {
                            listener.onShortcutSelected(item.title, item.url);
                        }
                    } else if (finalAction != ACTION_NONE) {
                        listener.onActionSelected(finalAction);
                    } else {
                        listener.onCancelled();
                    }
                }
            }
        });
        animator.start();
    }

    public void dismiss() {
        if (getParent() instanceof ViewGroup) {
            ((ViewGroup) getParent()).removeView(this);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (animScale <= 0.01f) return;

        canvas.save();
        canvas.scale(animScale, animScale, centerX, centerY);

        float rOuter = actionOuterRadius;
        float rInner = actionInnerRadius;

        outerDialRect.set(centerX - rOuter, centerY - rOuter, centerX + rOuter, centerY + rOuter);
        innerDialRect.set(centerX - rInner, centerY - rInner, centerX + rInner, centerY + rInner);
        actionCenterRect.set(centerX - actionCenterRadius, centerY - actionCenterRadius,
                centerX + actionCenterRadius, centerY + actionCenterRadius);
        outerOrbitRect.set(centerX - outerOrbitRadius, centerY - outerOrbitRadius,
                centerX + outerOrbitRadius, centerY + outerOrbitRadius);

        // 1. Cosmic Outer Space Liquid Glass Semicircle (90° to 270°, Sweeping Strictly Left)
        RadialGradient cosmicGradient = new RadialGradient(
                centerX, centerY, rOuter,
                new int[] {
                        Color.parseColor("#EE050811"), // Deep space void
                        Color.parseColor("#F00A1526"), // Celestial cyan nebula dust
                        Color.parseColor("#FA03060C")  // Luminous starlight edge
                },
                new float[] { 0f, 0.6f, 1.0f },
                Shader.TileMode.CLAMP
        );
        dialBgPaint.setShader(cosmicGradient);

        semicircularPath.reset();
        semicircularPath.arcTo(outerDialRect, 90f, 180f);
        semicircularPath.lineTo(centerX, centerY - rInner);
        semicircularPath.arcTo(innerDialRect, 270f, -180f);
        semicircularPath.close();

        canvas.drawPath(semicircularPath, dialBgPaint);

        // Specular Curved Rim & Ambient Nebula Halo
        canvas.drawArc(outerDialRect, 90f, 180f, false, dialGlowPaint);
        canvas.drawArc(outerDialRect, 90f, 180f, false, dialBorderPaint);

        // Flat Right Anchor Chord
        canvas.drawLine(centerX, centerY - rOuter, centerX, centerY - rInner, chordLinePaint);
        canvas.drawLine(centerX, centerY + rInner, centerX, centerY + rOuter, chordLinePaint);

        // Orbital Ring Guide Tracks (Left Arcs)
        canvas.drawArc(actionCenterRect, 90f, 180f, false, orbitalTrackPaint);
        canvas.drawArc(outerOrbitRect, 90f, 180f, false, orbitalTrackPaint);

        // 2. Draw 8 Inner Action Sectors along Left Semicircle
        float step = SEMICIRCLE_SWEEP_ANGLE / INNER_ACTIONS.length;

        for (int i = 0; i < INNER_ACTIONS.length; i++) {
            float startAngle = SEMICIRCLE_START_ANGLE + (i * step);
            float midAngle = startAngle + (step / 2f);
            boolean isSelected = (selectedAction == INNER_ACTIONS[i] && selectedShortcutIndex == -1);

            // Active Sector Aurora Highlight
            if (isSelected) {
                sectorPath.reset();
                sectorPath.arcTo(outerDialRect, startAngle, step);
                sectorPath.arcTo(innerDialRect, startAngle + step, -step);
                sectorPath.close();
                canvas.drawPath(sectorPath, sectorHighlightPaint);
                canvas.drawPath(sectorPath, sectorBorderPaint);
            }

            // Divider Ray Lines between sectors
            float rad = (float) Math.toRadians(startAngle);
            float x1 = centerX + rInner * (float) Math.cos(rad);
            float y1 = centerY + rInner * (float) Math.sin(rad);
            float x2 = centerX + rOuter * (float) Math.cos(rad);
            float y2 = centerY + rOuter * (float) Math.sin(rad);
            canvas.drawLine(x1, y1, x2, y2, dividerLinePaint);

            // Action Icon
            float midRad = (float) Math.toRadians(midAngle);
            float iconDist = isSelected ? (actionCenterRadius - 2f * density) : actionCenterRadius;
            float iconX = centerX + iconDist * (float) Math.cos(midRad);
            float iconY = centerY + iconDist * (float) Math.sin(midRad);

            actionIconPaint.setTextSize((isSelected ? 20f : 16f) * density);
            actionIconPaint.setColor(isSelected ? Color.parseColor("#22D3EE") : Color.WHITE);
            canvas.drawText(INNER_ACTION_ICONS[i], iconX, iconY + 5.5f * density, actionIconPaint);

            if (isSelected) {
                // Floating cosmic tooltip pill next to active sector
                float labelDist = actionOuterRadius + 22f * density;
                float labelX = centerX + labelDist * (float) Math.cos(midRad);
                float labelY = centerY + labelDist * (float) Math.sin(midRad);

                String labelText = INNER_ACTION_LABELS[i];
                float textW = tooltipTextPaint.measureText(labelText);
                tooltipRect.set(labelX - textW / 2f - 10f * density, labelY - 14f * density,
                        labelX + textW / 2f + 10f * density, labelY + 6f * density);
                canvas.drawRoundRect(tooltipRect, 10f * density, 10f * density, tooltipBgPaint);
                canvas.drawRoundRect(tooltipRect, 10f * density, 10f * density, tooltipBorderPaint);
                canvas.drawText(labelText, labelX, labelY, tooltipTextPaint);
            }
        }

        // 3. Draw Celestial Planetary Orbit Shortcuts
        int scCount = shortcutsList.size();
        if (scCount > 0) {
            float orbitSweep = 160f;
            float angleStep = orbitSweep / Math.max(1, scCount - 1);

            for (int j = 0; j < scCount; j++) {
                ShortcutItem item = shortcutsList.get(j);
                float planetAngle = (scCount == 1) ? 180f : (100f + (j * angleStep));
                float pRad = (float) Math.toRadians(planetAngle);

                boolean isSelected = (selectedShortcutIndex == j);
                float curPlanetRadius = (isSelected ? bubbleRadius * 1.25f : bubbleRadius);

                float px = centerX + outerOrbitRadius * (float) Math.cos(pRad);
                float py = centerY + outerOrbitRadius * (float) Math.sin(pRad);

                // a) Atmospheric Corona Glow
                planetAtmospherePaint.setColor(isSelected ? Color.parseColor("#4422D3EE") : Color.parseColor("#2022D3EE"));
                canvas.drawCircle(px, py, curPlanetRadius + 5f * density, planetAtmospherePaint);

                // b) 3D Spherical Planet Body
                RadialGradient sphereGradient = new RadialGradient(
                        px - curPlanetRadius * 0.35f,
                        py - curPlanetRadius * 0.35f,
                        curPlanetRadius * 1.4f,
                        new int[] {
                                isSelected ? Color.parseColor("#2A374A") : Color.parseColor("#1E293B"),
                                Color.parseColor("#0F172A"),
                                Color.parseColor("#050811")
                        },
                        new float[] { 0f, 0.6f, 1f },
                        Shader.TileMode.CLAMP
                );
                planetSpherePaint.setShader(sphereGradient);
                canvas.drawCircle(px, py, curPlanetRadius, planetSpherePaint);

                // c) Specular Planet Rim & Planetary Ring
                planetHighlightPaint.setColor(isSelected ? Color.parseColor("#22D3EE") : Color.parseColor("#44FFFFFF"));
                planetHighlightPaint.setStrokeWidth(isSelected ? 2.2f * density : 1.4f * density);
                canvas.drawCircle(px, py, curPlanetRadius, planetHighlightPaint);

                // Delicate Planetary Orbit Ring
                canvas.drawCircle(px, py, curPlanetRadius + 2.5f * density, planetRingPaint);

                // d) Planetary Core Content
                if (item.isConfigureOrbit) {
                    actionIconPaint.setTextSize(14f * density);
                    actionIconPaint.setColor(isSelected ? Color.parseColor("#22D3EE") : Color.WHITE);
                    canvas.drawText("⚙️", px, py + 4.5f * density, actionIconPaint);
                } else if (item.isTabsOverview) {
                    actionIconPaint.setTextSize(12f * density);
                    actionIconPaint.setColor(Color.parseColor("#22D3EE"));
                    canvas.drawText("🪐", px, py - 2f * density, actionIconPaint);

                    actionLabelPaint.setTextSize(10f * density);
                    actionLabelPaint.setColor(Color.WHITE);
                    canvas.drawText(String.valueOf(item.tabCount), px, py + 9f * density, actionLabelPaint);
                } else if (item.bitmap != null) {
                    float bw = item.bitmap.getWidth();
                    float bh = item.bitmap.getHeight();
                    canvas.drawBitmap(item.bitmap, px - bw / 2f, py - bh / 2f, bitmapPaint);
                } else {
                    String glyph = item.getFallbackDisplayGlyph();
                    actionIconPaint.setTextSize((isSelected ? 17f : 14.5f) * density);
                    actionIconPaint.setColor(isSelected ? Color.parseColor("#22D3EE") : Color.WHITE);
                    canvas.drawText(glyph, px, py + 5f * density, actionIconPaint);
                }

                // e) Floating Cosmic Tooltip Pill
                if (isSelected) {
                    String titleText = item.isConfigureOrbit ? "Configure Orbit" :
                            (item.isTabsOverview ? ("Tabs (" + item.tabCount + ")") : item.title);
                    float tw = tooltipTextPaint.measureText(titleText);
                    float tipY = py - curPlanetRadius - 9f * density;
                    tooltipRect.set(px - tw / 2f - 10f * density, tipY - 14f * density,
                            px + tw / 2f + 10f * density, tipY + 6f * density);
                    canvas.drawRoundRect(tooltipRect, 10f * density, 10f * density, tooltipBgPaint);
                    canvas.drawRoundRect(tooltipRect, 10f * density, 10f * density, tooltipBorderPaint);
                    canvas.drawText(titleText, px, tipY, tooltipTextPaint);
                }
            }
        }

        // 4. Center Celestial Star Reticle & Deadzone
        canvas.drawCircle(centerX, centerY, innerDeadzoneRadius, dialBgPaint);
        canvas.drawCircle(centerX, centerY, innerDeadzoneRadius, reticleRingPaint);
        canvas.drawCircle(centerX, centerY, 15f * density, reticleRingPaint);
        canvas.drawCircle(centerX, centerY, 5f * density, reticleDotPaint);

        if (selectedAction == ACTION_NONE && selectedShortcutIndex == -1) {
            canvas.drawText("Release to", centerX - 18f * density, centerY - 8f * density, subTextPaint);
            canvas.drawText("Cancel", centerX - 18f * density, centerY + 4f * density, subTextPaint);
        }

        canvas.restore();
    }
}
