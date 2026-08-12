package com.caspian.ai;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

public class SpeechWaveformView extends View {

    private Paint wavePaint;
    private Path wavePath;
    private float phase = 0f;
    private float amplitude = 20f;
    private float targetAmplitude = 20f;
    private int startColor = 0xFFA2A9A9;
    private int endColor = 0xFF1B4264;

    public SpeechWaveformView(Context context) {
        super(context);
        init();
    }

    public SpeechWaveformView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SpeechWaveformView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        wavePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        wavePaint.setStyle(Paint.Style.STROKE);
        wavePaint.setStrokeWidth(6f);
        wavePath = new Path();
    }

    public void setWaveColors(int startColor, int endColor) {
        this.startColor = startColor;
        this.endColor = endColor;
        invalidate();
    }

    public void setAmplitude(float rmsdB) {
        // Map rmsdB (usually -2 to 12) to target amplitude range
        this.targetAmplitude = Math.max(15f, Math.min(100f, (rmsdB + 2f) * 8f));
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        if (width == 0 || height == 0) return;

        // Smooth amplitude transition
        amplitude += (targetAmplitude - amplitude) * 0.15f;
        phase += 0.08f;

        LinearGradient gradient = new LinearGradient(
                0, 0, width, 0,
                startColor, endColor,
                Shader.TileMode.CLAMP
        );
        wavePaint.setShader(gradient);

        wavePath.reset();
        float centerY = height / 2f;

        wavePath.moveTo(0, centerY);
        for (float x = 0; x <= width; x += 10) {
            // Sine wave formula with tapering edges
            double scaling = Math.sin(Math.PI * x / width); // Envelope taper
            double y = centerY + Math.sin(x * 0.035 + phase) * amplitude * scaling;
            wavePath.lineTo(x, (float) y);
        }

        canvas.drawPath(wavePath, wavePaint);

        // Render secondary subtle accent wave
        Path subPath = new Path();
        subPath.moveTo(0, centerY);
        for (float x = 0; x <= width; x += 10) {
            double scaling = Math.sin(Math.PI * x / width);
            double y = centerY + Math.cos(x * 0.025 - phase) * (amplitude * 0.6f) * scaling;
            subPath.lineTo(x, (float) y);
        }
        wavePaint.setAlpha(120);
        canvas.drawPath(subPath, wavePaint);
        wavePaint.setAlpha(255);

        // Keep animating while visible
        if (getVisibility() == View.VISIBLE) {
            postInvalidateOnAnimation();
        }
    }
}
