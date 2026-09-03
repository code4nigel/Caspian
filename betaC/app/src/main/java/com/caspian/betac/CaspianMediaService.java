package com.caspian.betac;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;

import androidx.core.content.ContextCompat;

/**
 * Foreground Service for continuous, unthrottled background media playback in Caspian Flow.
 * Holds FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK to ensure Android network stack and CPU
 * never throttle video/audio chunk streaming when the app is minimized or the screen is off.
 */
public class CaspianMediaService extends Service {
    public static final String ACTION_START_FOREGROUND = "com.caspian.betac.ACTION_START_MEDIA_FOREGROUND";
    public static final String ACTION_STOP_FOREGROUND = "com.caspian.betac.ACTION_STOP_MEDIA_FOREGROUND";
    public static final String EXTRA_NOTIFICATION = "extra_notification";
    public static final int NOTIFICATION_ID_MEDIA = 7001;

    private static boolean isServiceRunning = false;

    public static boolean isRunning() {
        return isServiceRunning;
    }

    public static void startMediaForeground(Context context, Notification notification) {
        if (context == null || notification == null) return;
        try {
            Intent intent = new Intent(context, CaspianMediaService.class);
            intent.setAction(ACTION_START_FOREGROUND);
            intent.putExtra(EXTRA_NOTIFICATION, notification);
            ContextCompat.startForegroundService(context, intent);
        } catch (Exception ignored) {}
    }

    public static void stopMediaForeground(Context context) {
        if (context == null) return;
        try {
            Intent intent = new Intent(context, CaspianMediaService.class);
            intent.setAction(ACTION_STOP_FOREGROUND);
            context.startService(intent);
        } catch (Exception ignored) {}
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_START_FOREGROUND.equals(action)) {
                Notification notification;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notification = intent.getParcelableExtra(EXTRA_NOTIFICATION, Notification.class);
                } else {
                    notification = intent.getParcelableExtra(EXTRA_NOTIFICATION);
                }
                if (notification != null) {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            startForeground(NOTIFICATION_ID_MEDIA, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
                        } else {
                            startForeground(NOTIFICATION_ID_MEDIA, notification);
                        }
                        isServiceRunning = true;
                    } catch (Exception ignored) {}
                }
            } else if (ACTION_STOP_FOREGROUND.equals(action)) {
                try {
                    stopForeground(true);
                } catch (Exception ignored) {}
                isServiceRunning = false;
                stopSelf();
            }
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isServiceRunning = false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
