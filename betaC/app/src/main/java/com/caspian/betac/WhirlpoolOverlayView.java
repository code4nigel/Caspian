package com.caspian.betac;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * WhirlpoolOverlayView - Fullscreen interactive Circle-to-Search overlay view.
 * Allows users to circle or box anything on any web page or document, extracts
 * text via ML Kit on-device OCR, and exposes Google Lens, ChatGPT, Gemini, Split Arena, and Copy.
 */
@SuppressLint("ViewConstructor")
public class WhirlpoolOverlayView extends FrameLayout {

    private static final String TAG = "WhirlpoolOverlay";

    private final MainActivity activity;
    private final Bitmap screenBitmap;
    private final DrawingView drawingView;
    private final HorizontalScrollView menuScrollView;
    private final LinearLayout menuContainer;
    private final TextView hintPill;

    private Bitmap croppedBitmap;
    private String recognizedText = "";
    private Rect selectionBounds;
    private boolean isOcrRunning = false;
    private Consumer<String> pendingAiAction = null;

    public WhirlpoolOverlayView(@NonNull MainActivity activity, Bitmap screenBitmap) {
        super(activity);
        this.activity = activity;
        this.screenBitmap = screenBitmap;

        setClickable(true);
        setFocusable(true);
        setElevation(300f);

        // 1. Drawing Canvas View
        drawingView = new DrawingView(activity);
        addView(drawingView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        // 2. Top Hint Pill
        float density = getResources().getDisplayMetrics().density;
        hintPill = new TextView(activity);
        hintPill.setText("🌀 Circle or drag a box around anything to search");
        hintPill.setTextColor(Color.parseColor("#38BDF8"));
        hintPill.setTextSize(12f);
        hintPill.setGravity(Gravity.CENTER);
        hintPill.setPadding((int) (16 * density), (int) (8 * density), (int) (16 * density), (int) (8 * density));

        GradientDrawable hintBg = new GradientDrawable();
        hintBg.setColor(Color.parseColor("#EE0B132B"));
        hintBg.setCornerRadius(999f);
        hintBg.setStroke((int) (1.5f * density), Color.parseColor("#5538BDF8"));
        hintPill.setBackground(hintBg);

        LayoutParams hintLp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        hintLp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        hintLp.topMargin = (int) (72 * density);
        addView(hintPill, hintLp);

        // 3. Floating Liquid Glass AI Action Pill Menu (Hidden initially)
        menuScrollView = new HorizontalScrollView(activity);
        menuScrollView.setHorizontalScrollBarEnabled(false);
        menuScrollView.setVisibility(GONE);

        menuContainer = new LinearLayout(activity);
        menuContainer.setOrientation(LinearLayout.HORIZONTAL);
        menuContainer.setGravity(Gravity.CENTER_VERTICAL);
        menuContainer.setPadding((int) (8 * density), (int) (7 * density), (int) (8 * density), (int) (7 * density));

        GradientDrawable menuBg = new GradientDrawable();
        menuBg.setColor(Color.parseColor("#F2070D1E")); // Deep Obsidian Liquid Glass
        menuBg.setCornerRadius(24 * density);
        menuBg.setStroke((int) (1.5f * density), Color.parseColor("#3838BDF8")); // Radiant Caspian Cyan Glow
        menuContainer.setBackground(menuBg);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            menuContainer.setElevation(24 * density);
        }

        setupActionButtons(density);
        menuScrollView.addView(menuContainer, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        addView(menuScrollView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    }

    private void setupActionButtons(float density) {
        // Drag capsule handle on the left
        FrameLayout dragCap = new FrameLayout(activity);
        int capW = (int) (24 * density);
        int capH = (int) (34 * density);
        LinearLayout.LayoutParams dragLp = new LinearLayout.LayoutParams(capW, capH);
        dragLp.setMargins(0, 0, (int) (3 * density), 0);
        dragCap.setLayoutParams(dragLp);

        GradientDrawable dragBg = new GradientDrawable();
        dragBg.setColor(Color.parseColor("#201E293B"));
        dragBg.setCornerRadius(999f);
        dragBg.setStroke((int) (1.2f * density), Color.parseColor("#3538BDF8"));
        dragCap.setBackground(dragBg);

        TextView dragHandle = new TextView(activity);
        dragHandle.setText("⠿");
        dragHandle.setTextColor(Color.parseColor("#94A3B8"));
        dragHandle.setTextSize(13f);
        dragHandle.setGravity(Gravity.CENTER);
        dragCap.addView(dragHandle, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        setupDragListener(dragCap);
        setupDragListener(menuContainer);
        menuContainer.addView(dragCap);

        menuContainer.addView(createDivider(density));

        // 1. Ask Google (Google Lens with Image)
        Button btnGoogle = createActionButton("🔍 Ask Google", "#221E3A8A", "#403B82F6", "#5060A5FA", "#93C5FD", density);
        btnGoogle.setOnClickListener(v -> {
            activity.playUiFeedbackSound("tap");
            v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            if (croppedBitmap != null) {
                activity.launchGoogleLensWithBitmap(croppedBitmap);
            } else if (!recognizedText.isEmpty()) {
                activity.addNewTab("google", "", "https://www.google.com/search?q=" + android.net.Uri.encode(recognizedText), false);
            }
            dismiss();
        });
        menuContainer.addView(btnGoogle);

        // 2. Ask ChatGPT (OCR Text)
        Button btnGpt = createActionButton("✳️ Ask ChatGPT", "#22064E3B", "#4010B981", "#5034D399", "#6EE7B7", density);
        btnGpt.setOnClickListener(v -> {
            activity.playUiFeedbackSound("tap");
            v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            dispatchAiAction(text -> {
                String query = (text != null && !text.isEmpty()) ? text : "Help me understand this content";
                String prompt = "Explain this concept in simple terms:\n\n\"" + query + "\"";
                activity.addNewTab("chatgpt", prompt, "https://chatgpt.com", false);
                dismiss();
            });
        });
        menuContainer.addView(btnGpt);

        // 3. Ask Gemini (OCR Text)
        Button btnGemini = createActionButton("✦ Ask Gemini", "#244C1D95", "#408B5CF6", "#50A78BFA", "#DDD6FE", density);
        btnGemini.setOnClickListener(v -> {
            activity.playUiFeedbackSound("tap");
            v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            dispatchAiAction(text -> {
                String query = (text != null && !text.isEmpty()) ? text : "Help me understand this content";
                String prompt = "Explain this concept in simple terms:\n\n\"" + query + "\"";
                activity.addNewTab("gemini", prompt, "https://gemini.google.com/app", false);
                dismiss();
            });
        });
        menuContainer.addView(btnGemini);

        menuContainer.addView(createDivider(density));

        // 4. Split Arena (OCR Text)
        Button btnSplit = createActionButton("◫ Split Arena", "#220C4A6E", "#400EA5E9", "#5038BDF8", "#7DD3FC", density);
        btnSplit.setOnClickListener(v -> {
            activity.playUiFeedbackSound("tap");
            v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            dispatchAiAction(text -> {
                String query = (text != null && !text.isEmpty()) ? text : "Help me understand this content";
                activity.handleAskAiFromPdf(query, "split");
                dismiss();
            });
        });
        menuContainer.addView(btnSplit);

        menuContainer.addView(createDivider(density));

        // 5. Copy Image Crop or Recognized Text to Clipboard
        Button btnCopy = createActionButton("📋 Copy", "#20334155", "#35FFFFFF", "#3594A3B8", "#F1F5F9", density);
        btnCopy.setOnClickListener(v -> {
            activity.playUiFeedbackSound("tap");
            v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            dispatchAiAction(text -> {
                if (croppedBitmap != null && (text == null || text.isEmpty())) {
                    activity.copyImageToClipboard(croppedBitmap);
                } else if (text != null && !text.isEmpty()) {
                    ClipboardManager cm = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                    if (cm != null) {
                        cm.setPrimaryClip(ClipData.newPlainText("Caspian Whirlpool", text));
                        Toast.makeText(activity, "Copied recognized text to clipboard", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(activity, "No selection found", Toast.LENGTH_SHORT).show();
                }
                dismiss();
            });
        });
        menuContainer.addView(btnCopy);

        // 6. Close / Cancel
        Button btnClose = createActionButton("✕", "#25EF4444", "#45EF4444", "#50F87171", "#FECACA", density);
        btnClose.setOnClickListener(v -> {
            activity.playUiFeedbackSound("tap");
            v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            dismiss();
        });
        menuContainer.addView(btnClose);
    }

    private View createDivider(float density) {
        View div = new View(activity);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams((int) (1 * density), (int) (20 * density));
        lp.setMargins((int) (4 * density), 0, (int) (4 * density), 0);
        div.setLayoutParams(lp);
        div.setBackgroundColor(Color.parseColor("#25FFFFFF"));
        return div;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupDragListener(View handle) {
        handle.setOnTouchListener(new OnTouchListener() {
            private float startRawX, startRawY;
            private float initialMenuX, initialMenuY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        startRawX = event.getRawX();
                        startRawY = event.getRawY();
                        initialMenuX = menuScrollView.getX();
                        initialMenuY = menuScrollView.getY();
                        v.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float dx = event.getRawX() - startRawX;
                        float dy = event.getRawY() - startRawY;

                        float newX = initialMenuX + dx;
                        float newY = initialMenuY + dy;

                        // Clamping to screen boundaries
                        int maxX = Math.max(10, getWidth() - menuScrollView.getWidth() - 10);
                        int maxY = Math.max(60, getHeight() - menuScrollView.getHeight() - 60);
                        newX = Math.max(10, Math.min(maxX, newX));
                        newY = Math.max(60, Math.min(maxY, newY));

                        menuScrollView.setX(newX);
                        menuScrollView.setY(newY);
                        return true;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        return true;
                }
                return false;
            }
        });
    }

    private Button createActionButton(String label, String bgColor, String pressedColor, String strokeColor, String textColor, float density) {
        Button btn = new Button(activity);
        btn.setText(label);
        btn.setTextColor(Color.parseColor(textColor));
        btn.setTextSize(12.5f);
        btn.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        btn.setGravity(Gravity.CENTER);
        btn.setAllCaps(false);
        btn.setMinimumHeight(0);
        btn.setMinimumWidth(0);
        btn.setIncludeFontPadding(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            btn.setLetterSpacing(0.02f);
        }

        int padH = (int) (13 * density);
        int padV = (int) (7 * density);
        btn.setPadding(padH, padV, padH, padV);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, (int) (34 * density));
        lp.setMargins((int) (2.5f * density), 0, (int) (2.5f * density), 0);
        btn.setLayoutParams(lp);

        GradientDrawable normalBg = new GradientDrawable();
        normalBg.setColor(Color.parseColor(bgColor));
        normalBg.setCornerRadius(999f);
        normalBg.setStroke((int) (1.2f * density), Color.parseColor(strokeColor));

        GradientDrawable pressedBg = new GradientDrawable();
        pressedBg.setColor(Color.parseColor(pressedColor));
        pressedBg.setCornerRadius(999f);
        pressedBg.setStroke((int) (1.5f * density), Color.parseColor(strokeColor));

        StateListDrawable sld = new StateListDrawable();
        sld.addState(new int[]{android.R.attr.state_pressed}, pressedBg);
        sld.addState(new int[]{}, normalBg);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ColorStateList rippleColor = ColorStateList.valueOf(Color.parseColor("#33FFFFFF"));
            btn.setBackground(new RippleDrawable(rippleColor, sld, pressedBg));
        } else {
            btn.setBackground(sld);
        }

        return btn;
    }

    public void dismiss() {
        activity.onWhirlpoolDismissed();
        animate()
                .alpha(0f)
                .setDuration(160)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (getParent() instanceof ViewGroup) {
                            ((ViewGroup) getParent()).removeView(WhirlpoolOverlayView.this);
                        }
                    }
                })
                .start();
    }

    private void onSelectionFinished(Rect bounds) {
        if (bounds == null || bounds.width() < 25 || bounds.height() < 25) {
            return;
        }
        this.selectionBounds = bounds;
        hintPill.setVisibility(GONE);

        // Crop bitmap from snapshot
        if (screenBitmap != null) {
            int cropX = Math.max(0, Math.min(bounds.left, screenBitmap.getWidth() - 1));
            int cropY = Math.max(0, Math.min(bounds.top, screenBitmap.getHeight() - 1));
            int cropW = Math.min(bounds.width(), screenBitmap.getWidth() - cropX);
            int cropH = Math.min(bounds.height(), screenBitmap.getHeight() - cropY);

            if (cropW > 10 && cropH > 10) {
                try {
                    croppedBitmap = Bitmap.createBitmap(screenBitmap, cropX, cropY, cropW, cropH);
                    runOcrOnCrop(croppedBitmap);
                } catch (Exception e) {
                    Log.e(TAG, "Crop bitmap error: " + e.getMessage());
                }
            }
        }

        positionMenu(bounds);
    }

    private void dispatchAiAction(Consumer<String> action) {
        if (isOcrRunning) {
            Toast.makeText(activity, "⏳ Extracting text from outline...", Toast.LENGTH_SHORT).show();
            pendingAiAction = action;
        } else {
            action.accept(recognizedText);
        }
    }

    private void runOcrOnCrop(Bitmap bitmap) {
        try {
            isOcrRunning = true;
            InputImage image = InputImage.fromBitmap(bitmap, 0);
            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
            recognizer.process(image)
                    .addOnSuccessListener(visionText -> {
                        isOcrRunning = false;
                        recognizedText = visionText.getText().trim();
                        if (!recognizedText.isEmpty()) {
                            String preview = recognizedText.length() > 24 ? recognizedText.substring(0, 24) + "..." : recognizedText;
                            Toast.makeText(activity, "⚡ Extracted: \"" + preview + "\"", Toast.LENGTH_SHORT).show();
                        }
                        if (pendingAiAction != null) {
                            Consumer<String> action = pendingAiAction;
                            pendingAiAction = null;
                            action.accept(recognizedText);
                        }
                    })
                    .addOnFailureListener(e -> {
                        isOcrRunning = false;
                        Log.w(TAG, "OCR recognition error: " + e.getMessage());
                        if (pendingAiAction != null) {
                            Consumer<String> action = pendingAiAction;
                            pendingAiAction = null;
                            action.accept(recognizedText);
                        }
                    });
        } catch (Exception e) {
            isOcrRunning = false;
            Log.e(TAG, "runOcrOnCrop exception: " + e.getMessage());
            if (pendingAiAction != null) {
                Consumer<String> action = pendingAiAction;
                pendingAiAction = null;
                action.accept(recognizedText);
            }
        }
    }

    private void positionMenu(Rect bounds) {
        menuScrollView.setVisibility(VISIBLE);
        menuScrollView.setAlpha(0f);
        menuScrollView.setScaleX(0.85f);
        menuScrollView.setScaleY(0.85f);
        menuScrollView.post(() -> {
            int menuWidth = menuScrollView.getWidth();
            int menuHeight = menuScrollView.getHeight();
            int screenWidth = getWidth();
            int screenHeight = getHeight();

            int left = bounds.centerX() - (menuWidth / 2);
            left = Math.max(20, Math.min(screenWidth - menuWidth - 20, left));

            int top = bounds.top - menuHeight - 24;
            if (top < 120) {
                top = bounds.bottom + 24;
            }
            top = Math.max(100, Math.min(screenHeight - menuHeight - 120, top));

            menuScrollView.setX(left);
            menuScrollView.setY(top);

            menuScrollView.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(220)
                    .setInterpolator(new OvershootInterpolator(1.2f))
                    .start();
        });
    }

    // Inner Drawing View
    private class DrawingView extends View {
        private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint boxPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint scrimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        private final List<PointF> points = new ArrayList<>();
        private final Path path = new Path();
        private boolean isDragging = false;
        private Rect currentBox = null;

        public DrawingView(Context context) {
            super(context);
            float density = getResources().getDisplayMetrics().density;

            strokePaint.setStyle(Paint.Style.STROKE);
            strokePaint.setColor(Color.parseColor("#38BDF8"));
            strokePaint.setStrokeWidth(2.5f * density);

            fillPaint.setStyle(Paint.Style.FILL);
            fillPaint.setColor(Color.parseColor("#1538BDF8"));

            boxPaint.setStyle(Paint.Style.STROKE);
            boxPaint.setColor(Color.parseColor("#10B981"));
            boxPaint.setStrokeWidth(2f * density);
            boxPaint.setPathEffect(new DashPathEffect(new float[]{14f, 10f}, 0));

            scrimPaint.setStyle(Paint.Style.FILL);
            scrimPaint.setColor(Color.parseColor("#44000000"));
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();

            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    isDragging = true;
                    points.clear();
                    points.add(new PointF(x, y));
                    path.reset();
                    path.moveTo(x, y);
                    currentBox = null;
                    menuScrollView.setVisibility(GONE);
                    invalidate();
                    return true;

                case MotionEvent.ACTION_MOVE:
                    if (!isDragging) return false;
                    points.add(new PointF(x, y));
                    path.lineTo(x, y);
                    invalidate();
                    return true;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (!isDragging) return false;
                    isDragging = false;
                    if (points.size() > 2) {
                        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE;
                        float maxX = Float.MIN_VALUE, maxY = Float.MIN_VALUE;
                        for (PointF p : points) {
                            if (p.x < minX) minX = p.x;
                            if (p.y < minY) minY = p.y;
                            if (p.x > maxX) maxX = p.x;
                            if (p.y > maxY) maxY = p.y;
                        }
                        currentBox = new Rect((int) minX, (int) minY, (int) maxX, (int) maxY);
                        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                        onSelectionFinished(currentBox);
                    }
                    invalidate();
                    return true;
            }
            return super.onTouchEvent(event);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            // Draw translucent scrim
            canvas.drawRect(0, 0, getWidth(), getHeight(), scrimPaint);

            // Draw user's drawn path
            if (!points.isEmpty()) {
                canvas.drawPath(path, fillPaint);
                canvas.drawPath(path, strokePaint);
            }

            // Draw bounding highlight box once selected
            if (currentBox != null) {
                canvas.drawRect(currentBox, boxPaint);
            }
        }
    }
}
