package com.caspian.betac;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.provider.Settings;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Enterprise-grade, high-performance download manager for Caspian Flow.
 * Handles multi-redirect hops (e.g. GitHub -> AWS S3), session cookie injection,
 * high-throughput 64KB NIO streaming, blob/data stream reconstruction,
 * live progress/speed/ETA calculation, pause/resume, Android system notifications,
 * and JSON-backed download history.
 */
public class CaspianDownloadManager {
    private static final String TAG = "CaspianDownloader";
    private static final String CHANNEL_ID = "caspian_downloads";
    private static final String HISTORY_FILE = "caspian_downloads.json";
    private static final int BUFFER_SIZE = 65536; // 64 KB high-speed buffer

    private static CaspianDownloadManager instance;

    private final Context context;
    private final Handler mainHandler;
    private final ExecutorService executor;
    private final NotificationManager notificationManager;

    private final Map<String, DownloadTask> activeTasks = new ConcurrentHashMap<>();
    private final List<DownloadItem> historyList = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, FileOutputStream> blobStreams = new ConcurrentHashMap<>();

    private DownloadListener downloadListener;

    public interface DownloadListener {
        void onDownloadStarted(DownloadItem item);
        void onDownloadProgress(DownloadItem item);
        void onDownloadCompleted(DownloadItem item);
        void onDownloadFailed(DownloadItem item, String error);
        void onDownloadCancelled(DownloadItem item);
    }

    public static class DownloadItem {
        public String id;
        public String url;
        public String fileName;
        public String mimeType;
        public long totalBytes = -1;
        public long downloadedBytes = 0;
        public long speedBytesPerSec = 0;
        public long etaSeconds = -1;
        public String status = "PENDING"; // PENDING, DOWNLOADING, PAUSED, COMPLETED, FAILED, CANCELLED
        public String filePath;
        public long timestamp;
        public String error;
        public int notificationId;

        public JSONObject toJson() {
            JSONObject obj = new JSONObject();
            try {
                obj.put("id", id);
                obj.put("url", url);
                obj.put("fileName", fileName);
                obj.put("mimeType", mimeType != null ? mimeType : "");
                obj.put("totalBytes", totalBytes);
                obj.put("downloadedBytes", downloadedBytes);
                obj.put("speedBytesPerSec", speedBytesPerSec);
                obj.put("etaSeconds", etaSeconds);
                obj.put("status", status);
                obj.put("filePath", filePath != null ? filePath : "");
                obj.put("timestamp", timestamp);
                obj.put("error", error != null ? error : "");
            } catch (Exception ignored) {}
            return obj;
        }

        public static DownloadItem fromJson(JSONObject obj) {
            DownloadItem item = new DownloadItem();
            try {
                item.id = obj.optString("id");
                item.url = obj.optString("url");
                item.fileName = obj.optString("fileName");
                item.mimeType = obj.optString("mimeType");
                item.totalBytes = obj.optLong("totalBytes", -1);
                item.downloadedBytes = obj.optLong("downloadedBytes", 0);
                item.status = obj.optString("status", "COMPLETED");
                item.filePath = obj.optString("filePath");
                item.timestamp = obj.optLong("timestamp", System.currentTimeMillis());
                item.error = obj.optString("error");
            } catch (Exception ignored) {}
            return item;
        }
    }

    private class DownloadTask implements Runnable {
        final DownloadItem item;
        final String userAgent;
        final String contentDisposition;
        final String referer;
        volatile boolean isPaused = false;
        volatile boolean isCancelled = false;
        HttpURLConnection connection;

        DownloadTask(DownloadItem item, String userAgent, String contentDisposition) {
            this(item, userAgent, contentDisposition, null);
        }

        DownloadTask(DownloadItem item, String userAgent, String contentDisposition, String referer) {
            this.item = item;
            this.userAgent = userAgent;
            this.contentDisposition = contentDisposition;
            this.referer = referer;
        }

        @Override
        public void run() {
            item.status = "DOWNLOADING";
            notifyStarted(item);
            updateNotification(item);

            int retryCount = 0;
            final int maxRetries = 5;

            while (retryCount < maxRetries) {
                if (isCancelled) {
                    handleCancelled(item);
                    return;
                }
                if (isPaused) {
                    item.status = "PAUSED";
                    notifyProgress(item);
                    updateNotification(item);
                    return;
                }

                InputStream in = null;
                FileOutputStream out = null;

                try {
                    URL currentUrl = new URL(item.url);
                    String currentReferer = this.referer;
                    int redirectCount = 0;
                    boolean connected = false;

                    // Follow up to 10 redirects, preserving cookies and updating referer across domains
                    while (redirectCount < 10) {
                        if (isCancelled) break;
                        connection = (HttpURLConnection) currentUrl.openConnection();
                        connection.setInstanceFollowRedirects(false);
                        connection.setConnectTimeout(15000);
                        connection.setReadTimeout(30000);

                        // Inject WebView session cookies from referer and download domain
                        try {
                            String cookies = null;
                            if (currentReferer != null && !currentReferer.isEmpty()) {
                                cookies = CookieManager.getInstance().getCookie(currentReferer);
                            }
                            if (cookies == null || cookies.isEmpty()) {
                                cookies = CookieManager.getInstance().getCookie(currentUrl.toString());
                            }
                            if (cookies != null && !cookies.isEmpty()) {
                                connection.setRequestProperty("Cookie", cookies);
                            }
                        } catch (Exception ignored) {}

                        // Standard modern browser headers so CDNs/Cloudflare don't drop the connection
                        if (currentReferer != null && !currentReferer.isEmpty()) {
                            connection.setRequestProperty("Referer", currentReferer);
                        } else {
                            try {
                                URL u = new URL(item.url);
                                connection.setRequestProperty("Referer", u.getProtocol() + "://" + u.getHost() + "/");
                            } catch (Exception ignored) {}
                        }

                        if (userAgent != null && !userAgent.isEmpty()) {
                            connection.setRequestProperty("User-Agent", userAgent);
                        } else {
                            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14; Mobile) CaspianFlow/1.2");
                        }
                        connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,application/vnd.android.package-archive,*/*;q=0.8");
                        connection.setRequestProperty("Accept-Language", "en-US,en;q=0.9");
                        connection.setRequestProperty("Accept-Encoding", "identity");
                        connection.setRequestProperty("Connection", "keep-alive");
                        connection.setRequestProperty("Sec-Fetch-Dest", "document");
                        connection.setRequestProperty("Sec-Fetch-Mode", "navigate");
                        connection.setRequestProperty("Sec-Fetch-Site", "same-origin");
                        connection.setRequestProperty("Upgrade-Insecure-Requests", "1");

                        // Resume support if partial bytes exist
                        if (item.downloadedBytes > 0) {
                            connection.setRequestProperty("Range", "bytes=" + item.downloadedBytes + "-");
                        }

                        connection.connect();
                        int code = connection.getResponseCode();

                        if (code == HttpURLConnection.HTTP_MOVED_TEMP ||
                            code == HttpURLConnection.HTTP_MOVED_PERM ||
                            code == HttpURLConnection.HTTP_SEE_OTHER ||
                            code == 307 || code == 308) {
                            String loc = connection.getHeaderField("Location");
                            connection.disconnect();
                            if (loc != null && !loc.isEmpty()) {
                                currentReferer = currentUrl.toString();
                                currentUrl = new URL(currentUrl, loc);
                                redirectCount++;
                                continue;
                            }
                        }

                        if (code >= 200 && code < 300) {
                            connected = true;
                            break;
                        } else if (code == 416) {
                            // Range Not Satisfiable - reset to start
                            item.downloadedBytes = 0;
                            connection.disconnect();
                            continue;
                        } else {
                            throw new Exception("HTTP Error: " + code + " " + connection.getResponseMessage());
                        }
                    }

                    if (isCancelled) {
                        handleCancelled(item);
                        return;
                    }

                    if (!connected) {
                        throw new Exception("Failed to connect after redirects");
                    }

                    int responseCode = connection.getResponseCode();
                    boolean isAppending = item.downloadedBytes > 0 && responseCode == 206;
                    if (responseCode == 200) {
                        item.downloadedBytes = 0;
                        isAppending = false;
                    }

                    // Resolve file name if not already set or guessed
                    long contentLength = connection.getContentLengthLong();
                    if (contentLength > 0 && (item.totalBytes <= 0 || responseCode == 200)) {
                        item.totalBytes = isAppending ? (item.downloadedBytes + contentLength) : contentLength;
                    }

                    String disposition = connection.getHeaderField("Content-Disposition");
                    if (disposition == null) disposition = this.contentDisposition;
                    String contentType = connection.getContentType();
                    if (contentType != null && (item.mimeType == null || item.mimeType.isEmpty())) {
                        item.mimeType = contentType.split(";")[0].trim();
                    }

                    if (item.fileName == null || item.fileName.isEmpty() || item.fileName.equals("downloadfile")) {
                        item.fileName = resolveFileName(item.url, disposition, item.mimeType);
                    }

                    File destFile = getDestinationFile(item.fileName, isAppending);
                    item.filePath = destFile.getAbsolutePath();
                    item.fileName = destFile.getName();

                    in = connection.getInputStream();
                    out = new FileOutputStream(destFile, isAppending);

                    byte[] buffer = new byte[BUFFER_SIZE];
                    int bytesRead;
                    long lastSpeedCalcTime = System.currentTimeMillis();
                    long bytesSinceLastSpeedCalc = 0;
                    long lastProgressNotifyTime = 0;

                    while (true) {
                        if (isCancelled) {
                            break;
                        }
                        if (isPaused) {
                            item.status = "PAUSED";
                            notifyProgress(item);
                            updateNotification(item);
                            return;
                        }

                        // If total size is known and all bytes have already been fetched, complete immediately!
                        if (item.totalBytes > 0 && item.downloadedBytes >= item.totalBytes) {
                            break;
                        }

                        int toRead = BUFFER_SIZE;
                        if (item.totalBytes > 0) {
                            long remaining = item.totalBytes - item.downloadedBytes;
                            if (remaining <= 0) break;
                            if (remaining < BUFFER_SIZE) toRead = (int) remaining;
                        }

                        bytesRead = in.read(buffer, 0, toRead);
                        if (bytesRead == -1) {
                            if (item.totalBytes > 0 && item.downloadedBytes < item.totalBytes) {
                                throw new java.io.IOException("Premature EOF: read " + item.downloadedBytes + " of " + item.totalBytes + " bytes");
                            }
                            break;
                        }

                        out.write(buffer, 0, bytesRead);
                        item.downloadedBytes += bytesRead;
                        bytesSinceLastSpeedCalc += bytesRead;

                        long now = System.currentTimeMillis();
                        if (now - lastSpeedCalcTime >= 500) {
                            long deltaMs = now - lastSpeedCalcTime;
                            if (deltaMs > 0) {
                                item.speedBytesPerSec = (bytesSinceLastSpeedCalc * 1000) / deltaMs;
                                if (item.speedBytesPerSec > 0 && item.totalBytes > item.downloadedBytes) {
                                    item.etaSeconds = (item.totalBytes - item.downloadedBytes) / item.speedBytesPerSec;
                                } else {
                                    item.etaSeconds = -1;
                                }
                            }
                            lastSpeedCalcTime = now;
                            bytesSinceLastSpeedCalc = 0;
                        }

                        if (now - lastProgressNotifyTime >= 250 || (item.totalBytes > 0 && item.downloadedBytes >= item.totalBytes)) {
                            lastProgressNotifyTime = now;
                            notifyProgress(item);
                            updateNotification(item);
                        }

                        if (item.totalBytes > 0 && item.downloadedBytes >= item.totalBytes) {
                            break;
                        }
                    }

                    out.flush();
                    try {
                        out.getFD().sync();
                    } catch (Exception ignored) {}

                    if (isCancelled) {
                        handleCancelled(item);
                        if (destFile.exists()) destFile.delete();
                        return;
                    }

                    // Download completed successfully
                    item.status = "COMPLETED";
                    item.speedBytesPerSec = 0;
                    item.etaSeconds = 0;
                    if (item.totalBytes <= 0) item.totalBytes = item.downloadedBytes;

                    // Register file in Android MediaStore / Downloads gallery
                    MediaScannerConnection.scanFile(context, new String[]{destFile.getAbsolutePath()}, new String[]{item.mimeType}, null);

                    activeTasks.remove(item.id);
                    saveHistory();
                    notifyCompleted(item);
                    showCompletionNotification(item);
                    return; // Success! Exit retry loop.

                } catch (Exception e) {
                    if (isCancelled) {
                        handleCancelled(item);
                        return;
                    }
                    if (isPaused) {
                        item.status = "PAUSED";
                        notifyProgress(item);
                        updateNotification(item);
                        return;
                    }

                    retryCount++;
                    if (retryCount < maxRetries && (item.downloadedBytes > 0 || e instanceof java.io.IOException)) {
                        Log.w(TAG, "Download interrupted for " + item.fileName + " (" + e.getMessage() + "), auto-resuming from " + item.downloadedBytes + " bytes (attempt " + retryCount + "/" + maxRetries + ")...");
                        try { Thread.sleep(1000); } catch (Exception ignored) {}
                        continue; // Reconnect and resume seamlessly!
                    }

                    Log.e(TAG, "Download failed for " + item.fileName, e);
                    item.status = "FAILED";
                    item.error = e.getMessage() != null ? e.getMessage() : "Network error";
                    activeTasks.remove(item.id);
                    saveHistory();
                    notifyFailed(item, item.error);
                    showFailedNotification(item);
                    return;
                } finally {
                    try {
                        if (in != null) in.close();
                        if (out != null) out.close();
                        if (connection != null) connection.disconnect();
                    } catch (Exception ignored) {}
                }
            }
        }

        private void handleCancelled(DownloadItem item) {
            item.status = "CANCELLED";
            activeTasks.remove(item.id);
            cancelNotification(item);
            saveHistory();
            notifyCancelled(item);
        }
    }

    public static synchronized CaspianDownloadManager getInstance(Context context) {
        if (instance == null) {
            instance = new CaspianDownloadManager(context.getApplicationContext());
        }
        return instance;
    }

    private CaspianDownloadManager(Context context) {
        this.context = context;
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.executor = Executors.newFixedThreadPool(4);
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        createNotificationChannel();
        loadHistory();
    }

    public void setDownloadListener(DownloadListener listener) {
        this.downloadListener = listener;
    }

    /**
     * Enqueue an HTTP/HTTPS download directly from WebView setDownloadListener or user click.
     */
    public String enqueueDownload(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {
        return enqueueDownload(url, userAgent, contentDisposition, mimeType, contentLength, null);
    }

    public String enqueueDownload(String url, String userAgent, String contentDisposition, String mimeType, long contentLength, String referer) {
        if (url == null || url.isEmpty()) return null;

        String id = "dl_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
        DownloadItem item = new DownloadItem();
        item.id = id;
        item.url = url;
        item.mimeType = mimeType;
        item.totalBytes = contentLength > 0 ? contentLength : -1;
        item.timestamp = System.currentTimeMillis();
        item.notificationId = (int) (System.currentTimeMillis() & 0xfffffff);
        item.fileName = resolveFileName(url, contentDisposition, mimeType);

        DownloadTask task = new DownloadTask(item, userAgent, contentDisposition, referer);
        activeTasks.put(id, task);

        // Prepend to history so it appears first
        synchronized (historyList) {
            historyList.add(0, item);
        }
        saveHistory();

        executor.execute(task);
        return id;
    }

    /**
     * Save client-side Blob or Data URI chunks streamed from JavaScript.
     */
    public void saveBlobChunk(String downloadId, String filename, String mimeType, String base64Data, boolean isLast) {
        try {
            DownloadItem item = null;
            for (DownloadItem di : historyList) {
                if (di.id.equals(downloadId)) {
                    item = di;
                    break;
                }
            }

            if (item == null) {
                item = new DownloadItem();
                item.id = downloadId;
                item.url = "blob:" + filename;
                item.fileName = filename != null && !filename.isEmpty() ? filename : "blob_download.bin";
                item.mimeType = mimeType;
                item.timestamp = System.currentTimeMillis();
                item.status = "DOWNLOADING";
                item.notificationId = (int) (System.currentTimeMillis() & 0xfffffff);

                File dest = getDestinationFile(item.fileName, false);
                item.filePath = dest.getAbsolutePath();

                historyList.add(0, item);
                notifyStarted(item);
                updateNotification(item);
            }

            FileOutputStream fos = blobStreams.get(downloadId);
            if (fos == null) {
                File file = new File(item.filePath);
                fos = new FileOutputStream(file, true);
                blobStreams.put(downloadId, fos);
            }

            if (base64Data != null && !base64Data.isEmpty()) {
                byte[] bytes = Base64.decode(base64Data, Base64.DEFAULT);
                fos.write(bytes);
                item.downloadedBytes += bytes.length;
                notifyProgress(item);
                updateNotification(item);
            }

            if (isLast) {
                fos.flush();
                fos.close();
                blobStreams.remove(downloadId);

                item.status = "COMPLETED";
                item.totalBytes = item.downloadedBytes;
                saveHistory();

                MediaScannerConnection.scanFile(context, new String[]{item.filePath}, new String[]{item.mimeType}, null);
                notifyCompleted(item);
                showCompletionNotification(item);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error saving blob chunk", e);
        }
    }

    public void pauseDownload(String id) {
        DownloadTask task = activeTasks.get(id);
        if (task != null) {
            task.isPaused = true;
            task.item.status = "PAUSED";
            notifyProgress(task.item);
            updateNotification(task.item);
        }
    }

    public void resumeDownload(String id) {
        DownloadTask task = activeTasks.get(id);
        if (task != null && task.isPaused) {
            task.isPaused = false;
            executor.execute(task);
        } else {
            // Check if in history as paused/failed and re-run
            for (DownloadItem item : historyList) {
                if (item.id.equals(id) && ("PAUSED".equals(item.status) || "FAILED".equals(item.status))) {
                    DownloadTask newTask = new DownloadTask(item, null, null);
                    activeTasks.put(id, newTask);
                    executor.execute(newTask);
                    break;
                }
            }
        }
    }

    public void cancelDownload(String id) {
        DownloadTask task = activeTasks.remove(id);
        if (task != null) {
            task.isCancelled = true;
            if (task.connection != null) {
                new Thread(() -> {
                    try { task.connection.disconnect(); } catch (Exception ignored) {}
                }).start();
            }
            task.handleCancelled(task.item);
        } else {
            synchronized (historyList) {
                for (DownloadItem item : historyList) {
                    if (item.id.equals(id)) {
                        item.status = "CANCELLED";
                        cancelNotification(item);
                        saveHistory();
                        notifyCancelled(item);
                        break;
                    }
                }
            }
        }
    }

    public void deleteDownload(String id, boolean deleteFile) {
        DownloadTask task = activeTasks.remove(id);
        if (task != null) {
            task.isCancelled = true;
            if (task.connection != null) {
                new Thread(() -> {
                    try { task.connection.disconnect(); } catch (Exception ignored) {}
                }).start();
            }
            cancelNotification(task.item);
        }
        DownloadItem toRemove = null;
        synchronized (historyList) {
            for (DownloadItem item : historyList) {
                if (item.id.equals(id)) {
                    toRemove = item;
                    break;
                }
            }
            if (toRemove != null) {
                if (deleteFile && toRemove.filePath != null) {
                    try {
                        File file = new File(toRemove.filePath);
                        if (file.exists()) file.delete();
                    } catch (Exception ignored) {}
                }
                cancelNotification(toRemove);
                historyList.remove(toRemove);
                saveHistory();
                notifyCancelled(toRemove);
            }
        }
    }

    public void clearCompletedDownloads() {
        List<DownloadItem> remaining = new ArrayList<>();
        for (DownloadItem item : historyList) {
            if (!"COMPLETED".equals(item.status) && !"FAILED".equals(item.status) && !"CANCELLED".equals(item.status)) {
                remaining.add(item);
            }
        }
        historyList.clear();
        historyList.addAll(remaining);
        saveHistory();
    }

    public String getDownloadsJson() {
        JSONArray arr = new JSONArray();
        synchronized (historyList) {
            for (DownloadItem item : historyList) {
                arr.put(item.toJson());
            }
        }
        return arr.toString();
    }

    public boolean openFile(String id) {
        DownloadTask activeTask = activeTasks.get(id);
        DownloadItem target = activeTask != null ? activeTask.item : null;
        if (target == null) {
            for (DownloadItem item : historyList) {
                if (item.id.equals(id)) {
                    target = item;
                    break;
                }
            }
        }
        if (target == null || target.filePath == null) {
            for (DownloadItem item : historyList) {
                if (id.equals(item.filePath) || id.equals(item.fileName)) {
                    target = item;
                    break;
                }
            }
        }
        if (target == null || target.filePath == null) {
            Toast.makeText(context, "Download record not found", Toast.LENGTH_SHORT).show();
            return false;
        }

        File file = new File(target.filePath);
        if (!file.exists()) {
            Toast.makeText(context, "File not found on device storage", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            boolean isApk = file.getName().toLowerCase().endsWith(".apk");

            // On Android 8.0+, check if Caspian is allowed to install unknown apps
            if (isApk && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.getPackageManager().canRequestPackageInstalls()) {
                    Intent allowIntent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                            Uri.parse("package:" + context.getPackageName()));
                    allowIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(allowIntent);
                    Toast.makeText(context, "Please allow Caspian to install unknown apps, then tap Install again", Toast.LENGTH_LONG).show();
                    return false;
                }
            }

            Uri contentUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            String mime = target.mimeType;
            if (isApk) {
                mime = "application/vnd.android.package-archive";
            } else if (mime == null || mime.isEmpty() || "*/*".equals(mime)) {
                mime = getMimeTypeFromExtension(file.getName());
            }
            intent.setDataAndType(contentUri, mime);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error opening downloaded file", e);
            try {
                // Fallback to generic action view
                Uri contentUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
                Intent fallback = new Intent(Intent.ACTION_VIEW);
                fallback.setDataAndType(contentUri, "*/*");
                fallback.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(fallback);
                return true;
            } catch (Exception e2) {
                Toast.makeText(context, "Cannot open file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return false;
            }
        }
    }

    public boolean shareFile(String id) {
        DownloadTask activeTask = activeTasks.get(id);
        DownloadItem target = activeTask != null ? activeTask.item : null;
        if (target == null) {
            for (DownloadItem item : historyList) {
                if (item.id.equals(id)) {
                    target = item;
                    break;
                }
            }
        }
        if (target == null || target.filePath == null) {
            for (DownloadItem item : historyList) {
                if (id.equals(item.filePath) || id.equals(item.fileName)) {
                    target = item;
                    break;
                }
            }
        }
        if (target == null || target.filePath == null) return false;

        File file = new File(target.filePath);
        if (!file.exists()) {
            Toast.makeText(context, "File not found", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            Uri contentUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
            Intent intent = new Intent(Intent.ACTION_SEND);
            String mime = target.mimeType;
            if (file.getName().toLowerCase().endsWith(".apk")) {
                mime = "application/vnd.android.package-archive";
            } else if (mime == null || mime.isEmpty() || "*/*".equals(mime)) {
                mime = getMimeTypeFromExtension(file.getName());
            }
            intent.setType(mime);
            intent.putExtra(Intent.EXTRA_STREAM, contentUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            Intent chooser = Intent.createChooser(intent, "Share " + target.fileName);
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(chooser);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error sharing file", e);
            Toast.makeText(context, "Cannot share file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public void openDownloadsFolder() {
        try {
            Intent intent = new Intent(android.app.DownloadManager.ACTION_VIEW_DOWNLOADS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath());
                intent.setDataAndType(uri, "*/*");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } catch (Exception ignored) {}
        }
    }

    // -------------------------------------------------------------
    // Helper & Utility Methods
    // -------------------------------------------------------------

    private String resolveFileName(String url, String contentDisposition, String mimeType) {
        String fileName = null;

        // 1. Try Content-Disposition filename* or filename
        if (contentDisposition != null) {
            Pattern pStar = Pattern.compile("filename\\*=(?:UTF-8'')?([^;\\n]+)", Pattern.CASE_INSENSITIVE);
            Matcher mStar = pStar.matcher(contentDisposition);
            if (mStar.find()) {
                try {
                    fileName = URLDecoder.decode(mStar.group(1).replace("\"", ""), StandardCharsets.UTF_8.name());
                } catch (Exception ignored) {}
            }

            if (fileName == null || fileName.isEmpty()) {
                Pattern pRegular = Pattern.compile("filename=\"?([^\";\\n]+)\"?", Pattern.CASE_INSENSITIVE);
                Matcher mReg = pRegular.matcher(contentDisposition);
                if (mReg.find()) {
                    fileName = mReg.group(1).trim();
                }
            }
        }

        // 2. URL path fallback
        if (fileName == null || fileName.isEmpty()) {
            try {
                Uri parsed = Uri.parse(url);
                String lastPath = parsed.getLastPathSegment();
                if (lastPath != null && !lastPath.isEmpty()) {
                    fileName = URLDecoder.decode(lastPath, StandardCharsets.UTF_8.name());
                }
            } catch (Exception ignored) {}
        }

        // 3. Fallback to URLUtil.guessFileName
        if (fileName == null || fileName.isEmpty() || fileName.contains("?")) {
            fileName = URLUtil.guessFileName(url, contentDisposition, mimeType);
        }

        // Sanitize illegal filesystem characters
        fileName = fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
        return fileName;
    }

    private File getDestinationFile(String fileName, boolean isResume) {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (!downloadsDir.exists()) downloadsDir.mkdirs();

        File file = new File(downloadsDir, fileName);
        if (isResume || !file.exists()) {
            return file;
        }

        String baseName = fileName;
        String ext = "";
        int dotIdx = fileName.lastIndexOf('.');
        if (dotIdx > 0) {
            baseName = fileName.substring(0, dotIdx);
            ext = fileName.substring(dotIdx);
        }

        int count = 1;
        while (file.exists()) {
            file = new File(downloadsDir, baseName + " (" + count + ")" + ext);
            count++;
        }
        return file;
    }

    public static String getMimeTypeFromExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot >= 0) {
            String ext = fileName.substring(dot + 1).toLowerCase();
            String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
            if (mime != null) return mime;
            if (ext.equals("apk")) return "application/vnd.android.package-archive";
            if (ext.equals("pdf")) return "application/pdf";
            if (ext.equals("mp4")) return "video/mp4";
            if (ext.equals("mp3")) return "audio/mpeg";
            if (ext.equals("zip")) return "application/zip";
        }
        return "*/*";
    }

    // -------------------------------------------------------------
    // Notifications & UI Dispatch
    // -------------------------------------------------------------

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Caspian Downloads",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Shows active and completed file downloads in Caspian Flow");
            channel.setShowBadge(true);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void updateNotification(DownloadItem item) {
        try {
            int progress = item.totalBytes > 0 ? (int) ((item.downloadedBytes * 100) / item.totalBytes) : 0;
            boolean indeterminate = item.totalBytes <= 0;

            String subText = formatBytes(item.downloadedBytes);
            if (item.totalBytes > 0) {
                subText += " / " + formatBytes(item.totalBytes);
            }
            if (item.speedBytesPerSec > 0) {
                subText += " • " + formatBytes(item.speedBytesPerSec) + "/s";
            }

            NotificationCompat.Builder b = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.stat_sys_download)
                    .setContentTitle(item.fileName != null ? item.fileName : "Downloading file...")
                    .setContentText(subText)
                    .setProgress(100, progress, indeterminate)
                    .setOngoing(true)
                    .setOnlyAlertOnce(true)
                    .setPriority(NotificationCompat.PRIORITY_LOW);

            if (notificationManager != null) {
                notificationManager.notify(item.notificationId, b.build());
            }
        } catch (Exception ignored) {}
    }

    private void showCompletionNotification(DownloadItem item) {
        try {
            cancelNotification(item);

            File file = new File(item.filePath != null ? item.filePath : "");
            PendingIntent openPending = null;
            if (file.exists()) {
                Uri contentUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
                Intent openIntent = new Intent(Intent.ACTION_VIEW);
                String mime = item.mimeType;
                if (file.getName().toLowerCase().endsWith(".apk")) {
                    mime = "application/vnd.android.package-archive";
                } else if (mime == null || mime.isEmpty() || "*/*".equals(mime)) {
                    mime = getMimeTypeFromExtension(file.getName());
                }
                openIntent.setDataAndType(contentUri, mime);
                openIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                openIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                openPending = PendingIntent.getActivity(context, item.notificationId, openIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0));
            }

            NotificationCompat.Builder b = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.stat_sys_download_done)
                    .setContentTitle("Download Complete")
                    .setContentText(item.fileName + " (" + formatBytes(item.downloadedBytes) + ")")
                    .setAutoCancel(true)
                    .setOngoing(false)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            if (openPending != null) {
                b.setContentIntent(openPending);
            }

            if (notificationManager != null) {
                notificationManager.notify(item.notificationId, b.build());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing completion notification", e);
        }
    }

    private void showFailedNotification(DownloadItem item) {
        try {
            NotificationCompat.Builder b = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.stat_notify_error)
                    .setContentTitle("Download Failed")
                    .setContentText(item.fileName + ": " + (item.error != null ? item.error : "Network error"))
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            if (notificationManager != null) {
                notificationManager.notify(item.notificationId, b.build());
            }
        } catch (Exception ignored) {}
    }

    private void cancelNotification(DownloadItem item) {
        try {
            if (notificationManager != null) {
                notificationManager.cancel(item.notificationId);
            }
        } catch (Exception ignored) {}
    }

    private String formatBytes(long bytes) {
        if (bytes <= 0) return "0 B";
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }

    private void notifyStarted(DownloadItem item) {
        mainHandler.post(() -> {
            if (downloadListener != null) downloadListener.onDownloadStarted(item);
        });
    }

    private void notifyProgress(DownloadItem item) {
        mainHandler.post(() -> {
            if (downloadListener != null) downloadListener.onDownloadProgress(item);
        });
    }

    private void notifyCompleted(DownloadItem item) {
        mainHandler.post(() -> {
            if (downloadListener != null) downloadListener.onDownloadCompleted(item);
        });
    }

    private void notifyFailed(DownloadItem item, String error) {
        mainHandler.post(() -> {
            if (downloadListener != null) downloadListener.onDownloadFailed(item, error);
        });
    }

    private void notifyCancelled(DownloadItem item) {
        mainHandler.post(() -> {
            if (downloadListener != null) downloadListener.onDownloadCancelled(item);
        });
    }

    // -------------------------------------------------------------
    // History Persistence (caspian_downloads.json)
    // -------------------------------------------------------------

    private void saveHistory() {
        try {
            JSONArray arr = new JSONArray();
            synchronized (historyList) {
                for (DownloadItem item : historyList) {
                    arr.put(item.toJson());
                }
            }
            File file = new File(context.getFilesDir(), HISTORY_FILE);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(arr.toString().getBytes(StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to save download history", e);
        }
    }

    private void loadHistory() {
        try {
            File file = new File(context.getFilesDir(), HISTORY_FILE);
            if (!file.exists()) return;

            byte[] bytes = new byte[(int) file.length()];
            try (FileInputStream fis = new FileInputStream(file)) {
                fis.read(bytes);
            }
            String json = new String(bytes, StandardCharsets.UTF_8);
            JSONArray arr = new JSONArray(json);
            historyList.clear();
            for (int i = 0; i < arr.length(); i++) {
                DownloadItem item = DownloadItem.fromJson(arr.getJSONObject(i));
                // If it was left downloading when app killed, mark as paused
                if ("DOWNLOADING".equals(item.status)) {
                    item.status = "PAUSED";
                }
                historyList.add(item);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to load download history", e);
        }
    }
}
