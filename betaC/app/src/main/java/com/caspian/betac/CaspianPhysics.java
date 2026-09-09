package com.caspian.betac;

import android.annotation.SuppressLint;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.FloatPropertyCompat;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

/**
 * CaspianPhysics - Centralized iOS UIKit/SwiftUI-inspired fluid physics engine.
 * Powers authentic velocity-preserving springs, dynamic drag tilt, elastic squish/pop,
 * and boundary bounce across BetaC native components.
 */
public final class CaspianPhysics {

    // Spring damping presets (calibrated to iOS standard dynamics)
    public static final float DAMPING_BOUNCY = 0.58f;
    public static final float DAMPING_SMOOTH = 0.78f;
    public static final float DAMPING_SNAPPY = 0.88f;

    // Spring stiffness presets
    public static final float STIFFNESS_PLAYFUL = 450f;
    public static final float STIFFNESS_RESPONSIVE = 750f;
    public static final float STIFFNESS_SNAPPY = 1200f;

    private CaspianPhysics() {}

    /**
     * Creates or retrieves a SpringAnimation on a specific view property.
     */
    public static SpringAnimation createSpring(View view, DynamicAnimation.ViewProperty property,
                                              float dampingRatio, float stiffness) {
        SpringAnimation spring = (SpringAnimation) view.getTag(property.hashCode());
        if (spring == null) {
            spring = new SpringAnimation(view, property);
            view.setTag(property.hashCode(), spring);
        }
        SpringForce force = spring.getSpring();
        if (force == null) {
            force = new SpringForce();
            spring.setSpring(force);
        }
        force.setDampingRatio(dampingRatio);
        force.setStiffness(stiffness);
        return spring;
    }

    /**
     * Animate a view property to a target value using spring dynamics.
     */
    public static SpringAnimation animateSpring(View view, DynamicAnimation.ViewProperty property,
                                                float targetValue, float dampingRatio, float stiffness) {
        SpringAnimation spring = createSpring(view, property, dampingRatio, stiffness);
        spring.getSpring().setFinalPosition(targetValue);
        spring.start();
        return spring;
    }

    /**
     * Animate a view property to target value with initial velocity preservation.
     */
    public static SpringAnimation animateSpringWithVelocity(View view, DynamicAnimation.ViewProperty property,
                                                            float targetValue, float startVelocity,
                                                            float dampingRatio, float stiffness) {
        SpringAnimation spring = createSpring(view, property, dampingRatio, stiffness);
        spring.setStartVelocity(startVelocity);
        spring.getSpring().setFinalPosition(targetValue);
        spring.start();
        return spring;
    }

    /**
     * Cancels any active spring animations for the specified property on this view.
     */
    public static void cancelSpring(View view, DynamicAnimation.ViewProperty property) {
        SpringAnimation spring = (SpringAnimation) view.getTag(property.hashCode());
        if (spring != null && spring.isRunning()) {
            spring.cancel();
        }
    }

    /**
     * Applies tactile iOS squish effect (scale down to 0.92x).
     */
    public static void applyPressSquish(View view) {
        cancelSpring(view, DynamicAnimation.SCALE_X);
        cancelSpring(view, DynamicAnimation.SCALE_Y);
        animateSpring(view, DynamicAnimation.SCALE_X, 0.92f, DAMPING_SNAPPY, STIFFNESS_SNAPPY);
        animateSpring(view, DynamicAnimation.SCALE_Y, 0.92f, DAMPING_SNAPPY, STIFFNESS_SNAPPY);
    }

    /**
     * Releases squish back to 1.0x with playful overshoot bounce.
     */
    public static void applyReleasePop(View view) {
        cancelSpring(view, DynamicAnimation.SCALE_X);
        cancelSpring(view, DynamicAnimation.SCALE_Y);
        animateSpring(view, DynamicAnimation.SCALE_X, 1.0f, DAMPING_BOUNCY, STIFFNESS_PLAYFUL);
        animateSpring(view, DynamicAnimation.SCALE_Y, 1.0f, DAMPING_BOUNCY, STIFFNESS_PLAYFUL);
    }

    /**
     * Attaches an iOS-style spring touch bounce listener to any interactive button or card.
     */
    @SuppressLint("ClickableViewAccessibility")
    public static void attachSpringTouchFeedback(View view, Runnable onClickAction) {
        view.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    applyPressSquish(v);
                    return true;
                case MotionEvent.ACTION_UP:
                    applyReleasePop(v);
                    v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                    if (onClickAction != null) {
                        onClickAction.run();
                    }
                    return true;
                case MotionEvent.ACTION_CANCEL:
                    applyReleasePop(v);
                    return true;
            }
            return false;
        });
    }

    /**
     * Applies dynamic drag tilt based on horizontal velocity or delta.
     */
    public static void applyDragTilt(View view, float velocityX, float maxAngleDeg) {
        float tilt = (velocityX / 220f);
        if (tilt > maxAngleDeg) tilt = maxAngleDeg;
        if (tilt < -maxAngleDeg) tilt = -maxAngleDeg;
        animateSpring(view, DynamicAnimation.ROTATION, tilt, DAMPING_SMOOTH, STIFFNESS_RESPONSIVE);
    }

    /**
     * Resets rotation tilt back to 0 with elastic spring.
     */
    public static void resetDragTilt(View view) {
        animateSpring(view, DynamicAnimation.ROTATION, 0f, DAMPING_BOUNCY, STIFFNESS_PLAYFUL);
    }

    /**
     * Flings a view smoothly using spring dynamics while respecting screen boundaries.
     * Conserves initial touch gesture velocity and bounces smoothly if boundary is hit.
     */
    public static void flingToRestWithSpring(View view, float velX, float velY,
                                            float minX, float maxX,
                                            float minY, float maxY) {
        float currentX = view.getX();
        float currentY = view.getY();

        // Project momentum rest target based on velocity
        float targetX = currentX + (velX * 0.16f);
        float targetY = currentY + (velY * 0.16f);

        // Clamp to screen boundaries
        if (targetX < minX) targetX = minX;
        if (targetX > maxX) targetX = maxX;
        if (targetY < minY) targetY = minY;
        if (targetY > maxY) targetY = maxY;

        cancelSpring(view, DynamicAnimation.X);
        cancelSpring(view, DynamicAnimation.Y);

        animateSpringWithVelocity(view, DynamicAnimation.X, targetX, velX, DAMPING_BOUNCY, STIFFNESS_RESPONSIVE);
        animateSpringWithVelocity(view, DynamicAnimation.Y, targetY, velY, DAMPING_BOUNCY, STIFFNESS_RESPONSIVE);
        resetDragTilt(view);
    }

    /**
     * Expands a satellite dock container smoothly from its collapsed ball.
     */
    public static void expandDockWithSpring(View container, View ballView, View contentView) {
        if (ballView != null) ballView.setVisibility(View.GONE);
        if (contentView != null) {
            contentView.setVisibility(View.VISIBLE);
            contentView.setScaleX(0.72f);
            contentView.setScaleY(0.72f);
            contentView.setAlpha(0.3f);
            animateSpring(contentView, DynamicAnimation.SCALE_X, 1.0f, DAMPING_BOUNCY, STIFFNESS_RESPONSIVE);
            animateSpring(contentView, DynamicAnimation.SCALE_Y, 1.0f, DAMPING_BOUNCY, STIFFNESS_RESPONSIVE);
            contentView.animate().alpha(1.0f).setDuration(160).start();
        }
    }

    /**
     * Collapses a satellite dock container back into its liquid glass ball.
     */
    public static void collapseDockWithSpring(View container, View ballView, View contentView) {
        if (contentView != null) {
            contentView.animate()
                    .scaleX(0.7f)
                    .scaleY(0.7f)
                    .alpha(0.0f)
                    .setDuration(140)
                    .withEndAction(() -> {
                        contentView.setVisibility(View.GONE);
                        if (ballView != null) {
                            ballView.setVisibility(View.VISIBLE);
                            ballView.setScaleX(0.6f);
                            ballView.setScaleY(0.6f);
                            animateSpring(ballView, DynamicAnimation.SCALE_X, 1.0f, DAMPING_BOUNCY, STIFFNESS_PLAYFUL);
                            animateSpring(ballView, DynamicAnimation.SCALE_Y, 1.0f, DAMPING_BOUNCY, STIFFNESS_PLAYFUL);
                        }
                    })
                    .start();
        } else if (ballView != null) {
            ballView.setVisibility(View.VISIBLE);
        }
    }
}
