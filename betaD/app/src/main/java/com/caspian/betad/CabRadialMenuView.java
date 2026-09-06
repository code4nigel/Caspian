package com.caspian.betad;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;

/**
 * CabRadialMenuView - Radial gesture dial triggered on long-pressing the Caspian Action Button.
 * Sliding UP selects Caspian Whirlpool (Circle to Search / Visual AI).
 * Sliding DOWN selects Caspian Drift (Voice / Speech-to-Text).
 * Releasing inside the center deadzone cleanly cancels without action.
 */
public class CabRadialMenuView extends View {

    public interface OnRadialActionSelectedListener {
        void onActionSelected(int action);
        void onCancelled();
    }

    public static final int ACTION_NONE = 0;
    public static final int ACTION_WHIRLPOOL = 1;
    public static final int ACTION_DRIFT = 2;

    private float centerX = 0;
    private float centerY = 0;
    private float outerRadius;
    private float innerRadius;
    private float animScale = 0f;

    private int selectedAction = ACTION_NONE;
    private int previousAction = ACTION_NONE;

    private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint subTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RectF outerRect = new RectF();
    private final RectF innerRect = new RectF();
    private final Path arcPath = new Path();

    public CabRadialMenuView(Context context) {
        super(context);
        init();
    }

    private void init() {
        setLayerType(LAYER_TYPE_HARDWARE, null);
        float density = getResources().getDisplayMetrics().density;
        outerRadius = 115f * density;
        innerRadius = 40f * density;

        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setColor(Color.parseColor("#EE060A17")); // Deep dark frosted glass

        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setStrokeWidth(1.8f * density);
        ringPaint.setColor(Color.parseColor("#33FFFFFF"));

        highlightPaint.setStyle(Paint.Style.FILL);

        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(12f * density);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);

        subTextPaint.setColor(Color.parseColor("#94A3B8"));
        subTextPaint.setTextSize(9.5f * density);
        subTextPaint.setTextAlign(Paint.Align.CENTER);

        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(1.2f * density);
        linePaint.setColor(Color.parseColor("#25FFFFFF"));
    }

    public void showAt(float cx, float cy, ViewGroup parent) {
        this.centerX = cx;
        this.centerY = cy;
        this.selectedAction = ACTION_NONE;
        this.previousAction = ACTION_NONE;

        if (getParent() == null && parent != null) {
            parent.addView(this, new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));
            setElevation(250f);
        }

        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(240);
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
        if (dist > innerRadius * 0.85f) {
            if (dy < -20f) {
                newAction = ACTION_WHIRLPOOL;
            } else if (dy > 20f) {
                newAction = ACTION_DRIFT;
            }
        }

        if (newAction != selectedAction) {
            selectedAction = newAction;
            if (selectedAction != ACTION_NONE && selectedAction != previousAction) {
                performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            }
            previousAction = selectedAction;
            invalidate();
        }
    }

    public void finishGesture(OnRadialActionSelectedListener listener) {
        int finalAction = selectedAction;
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
                    if (finalAction == ACTION_WHIRLPOOL || finalAction == ACTION_DRIFT) {
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

        float r = outerRadius;
        float ir = innerRadius;
        outerRect.set(centerX - r, centerY - r, centerX + r, centerY + r);
        innerRect.set(centerX - ir, centerY - ir, centerX + ir, centerY + ir);

        // 1. Draw Background Dial Disc
        canvas.drawCircle(centerX, centerY, r, bgPaint);

        // 2. Draw Active Highlight Sector
        if (selectedAction == ACTION_WHIRLPOOL) {
            highlightPaint.setColor(Color.parseColor("#3838BDF8")); // Cyan glow
            arcPath.reset();
            arcPath.arcTo(outerRect, 180f, 180f);
            arcPath.arcTo(innerRect, 0f, -180f);
            arcPath.close();
            canvas.drawPath(arcPath, highlightPaint);
        } else if (selectedAction == ACTION_DRIFT) {
            highlightPaint.setColor(Color.parseColor("#3810B981")); // Emerald glow
            arcPath.reset();
            arcPath.arcTo(outerRect, 0f, 180f);
            arcPath.arcTo(innerRect, 180f, -180f);
            arcPath.close();
            canvas.drawPath(arcPath, highlightPaint);
        }

        // 3. Draw Outer & Inner Border Rings
        ringPaint.setColor(Color.parseColor("#33FFFFFF"));
        canvas.drawCircle(centerX, centerY, r, ringPaint);
        canvas.drawCircle(centerX, centerY, ir, ringPaint);

        // 4. Draw Horizontal Divider Line
        canvas.drawLine(centerX - r, centerY, centerX - ir, centerY, linePaint);
        canvas.drawLine(centerX + ir, centerY, centerX + r, centerY, linePaint);

        // 5. Draw Top Sector (Whirlpool)
        float density = getResources().getDisplayMetrics().density;
        float topLabelY = centerY - (r + ir) / 2f + 4f;
        textPaint.setColor(selectedAction == ACTION_WHIRLPOOL ? Color.parseColor("#38BDF8") : Color.WHITE);
        canvas.drawText("🌀 WHIRLPOOL", centerX, topLabelY, textPaint);
        subTextPaint.setColor(selectedAction == ACTION_WHIRLPOOL ? Color.parseColor("#BAE6FD") : Color.parseColor("#94A3B8"));
        canvas.drawText("Circle to Search", centerX, topLabelY + 16f * density, subTextPaint);

        // 6. Draw Bottom Sector (Drift)
        float bottomLabelY = centerY + (r + ir) / 2f - 10f;
        textPaint.setColor(selectedAction == ACTION_DRIFT ? Color.parseColor("#10B981") : Color.WHITE);
        canvas.drawText("🎙️ CASPIAN DRIFT", centerX, bottomLabelY, textPaint);
        subTextPaint.setColor(selectedAction == ACTION_DRIFT ? Color.parseColor("#A7F3D0") : Color.parseColor("#94A3B8"));
        canvas.drawText("Voice Engine", centerX, bottomLabelY + 16f * density, subTextPaint);

        // 7. Center Deadzone Label
        subTextPaint.setColor(Color.parseColor("#64748B"));
        canvas.drawText(selectedAction == ACTION_NONE ? "Release" : "Release to", centerX, centerY - 2f, subTextPaint);
        canvas.drawText(selectedAction == ACTION_NONE ? "to Cancel" : "Activate", centerX, centerY + 12f, subTextPaint);

        canvas.restore();
    }
}
