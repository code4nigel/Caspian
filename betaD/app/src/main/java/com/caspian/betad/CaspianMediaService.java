package com.caspian.betad;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;

import androidx.core.content.ContextCompat;

/**
 * Foreground Service for continuous, unthrottled background media playback in Caspian Flow.
 * Holds FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK to ensure Android network stack and CPU
 * never throttle video/audio chunk streaming when the app is minimized or the screen is off.
 */
public class CaspianMediaService extends Service {
    public static final String ACTION_START_FOREGROUND = "com.caspian.betad.ACTION_START_MEDIA_FOREGROUND";
    public static final String ACTION_PAUSE_FOREGROUND = "com.caspian.betad.ACTION_PAUSE_MEDIA_FOREGROUND";
    public static final String ACTION_STOP_FOREGROUND = "com.caspian.betad.ACTION_STOP_MEDIA_FOREGROUND";
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

    public static void pauseMediaForeground(Context context) {
        if (context == null) return;
        try {
            Intent intent = new Intent(context, CaspianMediaService.class);
            intent.setAction(ACTION_PAUSE_FOREGROUND);
            context.startService(intent);
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

    private PowerManager.WakeLock wakeLock;
    private WifiManager.WifiLock wifiLock;

    private void acquireLocks() {
        try {
            if (wakeLock == null) {
                PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                if (pm != null) {
                    wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Caspian:MediaServiceWakeLock");
                }
            }
            if (wakeLock != null && !wakeLock.isHeld()) {
                wakeLock.acquire(4 * 60 * 60 * 1000L);
            }
        } catch (Exception ignored) {}

        try {
            if (wifiLock == null) {
                WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if (wm != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL_LOW_LATENCY, "Caspian:MediaServiceWifiLock");
                    } else {
                        wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "Caspian:MediaServiceWifiLock");
                    }
                    wifiLock.setReferenceCounted(false);
                }
            }
            if (wifiLock != null && !wifiLock.isHeld()) {
                wifiLock.acquire();
            }
        } catch (Exception ignored) {}
    }

    private void releaseLocks() {
        try {
            if (wakeLock != null && wakeLock.isHeld()) {
                wakeLock.release();
            }
        } catch (Exception ignored) {}
        try {
            if (wifiLock != null && wifiLock.isHeld()) {
                wifiLock.release();
            }
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
                        acquireLocks();
                    } catch (Exception ignored) {}
                }
            } else if (ACTION_PAUSE_FOREGROUND.equals(action)) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        stopForeground(STOP_FOREGROUND_DETACH);
                    } else {
                        stopForeground(false);
                    }
                } catch (Exception ignored) {}
                releaseLocks();
            } else if (ACTION_STOP_FOREGROUND.equals(action)) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        stopForeground(STOP_FOREGROUND_REMOVE);
                    } else {
                        stopForeground(true);
                    }
                } catch (Exception ignored) {}
                releaseLocks();
                isServiceRunning = false;
                stopSelf();
            }
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseLocks();
        isServiceRunning = false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
