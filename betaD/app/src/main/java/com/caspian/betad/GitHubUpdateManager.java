package com.caspian.betad;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

import androidx.core.content.FileProvider;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Manages checking for releases on GitHub, parsing changelogs, downloading APKs, and 1-tap in-app installation.
 */
public class GitHubUpdateManager {
    private static final String TAG = "GitHubUpdateManager";
    private static final String GITHUB_RELEASES_API = "https://api.github.com/repos/code4nigel/Caspian/releases";
    private static final String PREF_NAME = "caspian_update_prefs";
    private static final String KEY_LAST_CHECK_TIME = "last_update_check_time";

    private final Context context;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public static class UpdateInfo {
        public final boolean hasUpdate;
        public final String tagName;
        public final String cleanVersion;
        public final String releaseTitle;
        public final String changelogBody;
        public final String apkDownloadUrl;
        public final String apkFileName;
        public final long apkSize;
        public final String publishedAt;

        public UpdateInfo(boolean hasUpdate, String tagName, String cleanVersion, String releaseTitle,
                          String changelogBody, String apkDownloadUrl, String apkFileName, long apkSize, String publishedAt) {
            this.hasUpdate = hasUpdate;
            this.tagName = tagName;
            this.cleanVersion = cleanVersion;
            this.releaseTitle = releaseTitle;
            this.changelogBody = changelogBody;
            this.apkDownloadUrl = apkDownloadUrl;
            this.apkFileName = apkFileName;
            this.apkSize = apkSize;
            this.publishedAt = publishedAt;
        }

        public JSONObject toJson() {
            try {
                JSONObject obj = new JSONObject();
                obj.put("hasUpdate", hasUpdate);
                obj.put("tagName", tagName != null ? tagName : "");
                obj.put("cleanVersion", cleanVersion != null ? cleanVersion : "");
                obj.put("releaseTitle", releaseTitle != null ? releaseTitle : "");
                obj.put("changelogBody", changelogBody != null ? changelogBody : "");
                obj.put("apkDownloadUrl", apkDownloadUrl != null ? apkDownloadUrl : "");
                obj.put("apkFileName", apkFileName != null ? apkFileName : "");
                obj.put("apkSize", apkSize);
                obj.put("publishedAt", publishedAt != null ? publishedAt : "");
                return obj;
            } catch (Exception e) {
                return new JSONObject();
            }
        }
    }

    public interface UpdateCheckCallback {
        void onResult(UpdateInfo info);
        void onError(String message);
    }

    public interface DownloadCallback {
        void onProgress(int percent, long downloadedBytes, long totalBytes);
        void onComplete(File apkFile);
        void onError(String message);
    }

    public GitHubUpdateManager(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * Checks for updates.
     * @param isManual true if triggered by user button; false if automatic on startup (throttled to 1 check per 24 hours).
     */
    public void checkForUpdates(boolean isManual, UpdateCheckCallback callback) {
        if (!isManual) {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            long lastCheck = prefs.getLong(KEY_LAST_CHECK_TIME, 0);
            long now = System.currentTimeMillis();
            // Throttle automatic checks to at least 4 hours
            if (now - lastCheck < 4 * 60 * 60 * 1000) {
                if (callback != null) {
                    mainHandler.post(() -> callback.onResult(new UpdateInfo(false, null, null, null, null, null, null, 0, null)));
                }
                return;
            }
        }

        executor.execute(() -> {
            UpdateInfo resolvedInfo = null;
            String lastError = null;

            // Strategy 1: GitHub REST API
            try {
                resolvedInfo = checkViaRestApi();
            } catch (Exception e) {
                lastError = e.getMessage();
                Log.w(TAG, "GitHub REST API failed, attempting Atom feed fallback: " + e.getMessage());
            }

            // Strategy 2: Fallback to GitHub Releases Atom Feed (Zero Rate Limit)
            if (resolvedInfo == null) {
                try {
                    resolvedInfo = checkViaAtomFeed();
                } catch (Exception e) {
                    lastError = (lastError != null ? lastError + " | " : "") + e.getMessage();
                    Log.e(TAG, "GitHub Atom feed fallback failed: " + e.getMessage());
                }
            }

            if (resolvedInfo != null) {
                // Record successful check timestamp
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                        .edit()
                        .putLong(KEY_LAST_CHECK_TIME, System.currentTimeMillis())
                        .apply();

                final UpdateInfo finalInfo = resolvedInfo;
                mainHandler.post(() -> {
                    if (callback != null) callback.onResult(finalInfo);
                });
            } else {
                final String finalErr = lastError != null ? lastError : "Could not fetch releases";
                mainHandler.post(() -> {
                    if (callback != null) callback.onError(finalErr);
                });
            }
        });
    }

    private UpdateInfo checkViaRestApi() throws Exception {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(GITHUB_RELEASES_API);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Caspian-Flow-App");
            conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new Exception("HTTP " + responseCode);
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();

            JSONArray releases = new JSONArray(sb.toString());
            JSONObject latestFlowRelease = null;
            String apkUrl = null;
            String apkName = null;
            long apkSize = 0;

            for (int i = 0; i < releases.length(); i++) {
                JSONObject rel = releases.getJSONObject(i);
                if (rel.optBoolean("draft", false)) continue;

                String tag = rel.optString("tag_name", "").trim();
                String title = rel.optString("name", "").trim();
                String tagLower = tag.toLowerCase();
                String titleLower = title.toLowerCase();

                boolean isOtherVariant = tagLower.contains("mobile")
                        || tagLower.contains("beta-a")
                        || tagLower.contains("beta-b")
                        || tagLower.contains("extension");

                boolean isFlowVariant = (tagLower.contains("flow") || tagLower.contains("betac") || titleLower.contains("flow") || titleLower.contains("beta c")) && !isOtherVariant;

                if (!isFlowVariant) continue;

                JSONArray assets = rel.optJSONArray("assets");
                if (assets != null) {
                    for (int j = 0; j < assets.length(); j++) {
                        JSONObject asset = assets.getJSONObject(j);
                        String name = asset.optString("name", "");
                        String nameLower = name.toLowerCase();
                        if (nameLower.endsWith(".apk") && (nameLower.contains("flow") || nameLower.contains("betac"))) {
                            apkUrl = asset.optString("browser_download_url", "");
                            apkName = name;
                            apkSize = asset.optLong("size", 0);
                            latestFlowRelease = rel;
                            break;
                        }
                    }
                }

                if (latestFlowRelease != null) break;
            }

            if (latestFlowRelease == null) return null;

            String tagName = latestFlowRelease.optString("tag_name", "");
            String releaseTitle = latestFlowRelease.optString("name", tagName);
            String changelogBody = latestFlowRelease.optString("body", "No changelog provided.");
            String publishedAt = latestFlowRelease.optString("published_at", "");

            String cleanVersion = extractCleanVersion(tagName);
            String currentVersion = "1.1.34-BetaC";
            try {
                currentVersion = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            } catch (Exception ignored) {}

            boolean hasUpdate = isNewerVersion(cleanVersion, currentVersion);

            return new UpdateInfo(
                    hasUpdate,
                    tagName,
                    cleanVersion,
                    releaseTitle,
                    changelogBody,
                    apkUrl,
                    apkName,
                    apkSize,
                    publishedAt
            );
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private UpdateInfo checkViaAtomFeed() throws Exception {
        HttpURLConnection conn = null;
        try {
            URL url = new URL("https://github.com/code4nigel/Caspian/releases.atom");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Caspian-Flow-App");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new Exception("Atom Feed HTTP " + responseCode);
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();

            String xml = sb.toString();

            // Extract tags
            Pattern tagPattern = Pattern.compile("/releases/tag/([^\"'<>\\s]+)");
            Matcher matcher = tagPattern.matcher(xml);

            String matchedTag = null;
            while (matcher.find()) {
                String tag = matcher.group(1).trim();
                String tagLower = tag.toLowerCase();

                boolean isOther = tagLower.contains("mobile") || tagLower.contains("beta-a") || tagLower.contains("beta-b") || tagLower.contains("extension");
                boolean isFlow = (tagLower.contains("flow") || tagLower.contains("betac")) && !isOther;

                if (isFlow) {
                    matchedTag = tag;
                    break;
                }
            }

            if (matchedTag == null) return null;

            String cleanVersion = extractCleanVersion(matchedTag);
            String currentVersion = "1.1.34-BetaC";
            try {
                currentVersion = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            } catch (Exception ignored) {}

            boolean hasUpdate = isNewerVersion(cleanVersion, currentVersion);
            String apkUrl = "https://github.com/code4nigel/Caspian/releases/download/" + matchedTag + "/" + matchedTag + ".apk";
            String apkName = matchedTag + ".apk";

            return new UpdateInfo(
                    hasUpdate,
                    matchedTag,
                    cleanVersion,
                    matchedTag,
                    "Direct release from GitHub Atom feed.",
                    apkUrl,
                    apkName,
                    0,
                    ""
            );
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    /**
     * Downloads APK directly into cache and invokes installation callback.
     */
    public void downloadApk(String downloadUrl, String apkFileName, DownloadCallback callback) {
        executor.execute(() -> {
            HttpURLConnection conn = null;
            InputStream is = null;
            FileOutputStream fos = null;
            try {
                URL currentUrl = new URL(downloadUrl);
                int redirectCount = 0;
                boolean connected = false;

                while (redirectCount < 10) {
                    conn = (HttpURLConnection) currentUrl.openConnection();
                    conn.setInstanceFollowRedirects(false);
                    conn.setRequestProperty("User-Agent", "Caspian-Flow-App");
                    conn.setRequestProperty("Accept", "*/*");
                    conn.setConnectTimeout(15000);
                    conn.setReadTimeout(30000);

                    try {
                        String cookies = android.webkit.CookieManager.getInstance().getCookie(currentUrl.toString());
                        if (cookies != null && !cookies.isEmpty()) {
                            conn.setRequestProperty("Cookie", cookies);
                        }
                    } catch (Exception ignored) {}

                    conn.connect();
                    int status = conn.getResponseCode();

                    if (status == HttpURLConnection.HTTP_MOVED_TEMP ||
                        status == HttpURLConnection.HTTP_MOVED_PERM ||
                        status == HttpURLConnection.HTTP_SEE_OTHER ||
                        status == 307 || status == 308) {
                        String newUrl = conn.getHeaderField("Location");
                        conn.disconnect();
                        if (newUrl != null && !newUrl.isEmpty()) {
                            currentUrl = new URL(currentUrl, newUrl);
                            redirectCount++;
                            continue;
                        }
                    }

                    if (status >= 200 && status < 300) {
                        connected = true;
                        break;
                    } else {
                        throw new Exception("HTTP " + status + ": " + conn.getResponseMessage());
                    }
                }

                if (!connected) {
                    throw new Exception("Failed to connect after redirects");
                }

                long totalBytes = conn.getContentLengthLong();
                is = conn.getInputStream();

                cleanOldApks(context);
                File cacheDir = context.getExternalCacheDir() != null ? context.getExternalCacheDir() : context.getCacheDir();
                File outputFile = new File(cacheDir, apkFileName != null ? apkFileName : "caspian_flow_update.apk");
                fos = new FileOutputStream(outputFile);

                byte[] buffer = new byte[65536]; // 64 KB buffer
                long downloadedBytes = 0;
                int read;
                long lastProgressUpdate = 0;

                while ((read = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, read);
                    downloadedBytes += read;

                    long now = System.currentTimeMillis();
                    if (now - lastProgressUpdate > 100 || downloadedBytes == totalBytes) {
                        lastProgressUpdate = now;
                        int percent = totalBytes > 0 ? (int) ((downloadedBytes * 100) / totalBytes) : -1;
                        long current = downloadedBytes;
                        mainHandler.post(() -> {
                            if (callback != null) callback.onProgress(percent, current, totalBytes);
                        });
                    }
                }

                fos.flush();
                fos.close();
                fos = null;

                mainHandler.post(() -> {
                    if (callback != null) callback.onComplete(outputFile);
                });

            } catch (Exception e) {
                Log.e(TAG, "Error downloading APK update", e);
                mainHandler.post(() -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
            } finally {
                try {
                    if (is != null) is.close();
                    if (fos != null) fos.close();
                    if (conn != null) conn.disconnect();
                } catch (Exception ignored) {}
            }
        });
    }

    /**
     * Prompts the native Android package installer to install the downloaded APK.
     */
    public boolean installApk(Activity activity, File apkFile) {
        if (apkFile == null || !apkFile.exists()) {
            Log.e(TAG, "APK file does not exist");
            return false;
        }

        try {
            // Check unknown sources permission on Android 8.0+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!activity.getPackageManager().canRequestPackageInstalls()) {
                    Intent allowIntent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                            Uri.parse("package:" + activity.getPackageName()));
                    activity.startActivity(allowIntent);
                    return false;
                }
            }

            Uri apkUri = FileProvider.getUriForFile(
                    activity,
                    activity.getPackageName() + ".fileprovider",
                    apkFile
            );

            Intent installIntent = new Intent(Intent.ACTION_VIEW);
            installIntent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(installIntent);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error initiating package installation", e);
            return false;
        }
    }

    public static String extractCleanVersion(String tag) {
        if (tag == null) return "0.0.0";
        Pattern pattern = Pattern.compile("([0-9]+\\.[0-9]+\\.[0-9]+(?:-[A-Za-z0-9]+)?)");
        Matcher matcher = pattern.matcher(tag);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return tag.replace("Caspian-Flow-", "").replace("v", "").trim();
    }

    public static boolean isNewerVersion(String remoteVersion, String localVersion) {
        if (remoteVersion == null || localVersion == null) return false;
        try {
            String cleanRemote = extractNumericSemver(remoteVersion);
            String cleanLocal = extractNumericSemver(localVersion);

            String[] rParts = cleanRemote.split("\\.");
            String[] lParts = cleanLocal.split("\\.");

            int maxLen = Math.max(rParts.length, lParts.length);
            for (int i = 0; i < maxLen; i++) {
                int rNum = (i < rParts.length && !rParts[i].isEmpty()) ? Integer.parseInt(rParts[i]) : 0;
                int lNum = (i < lParts.length && !lParts[i].isEmpty()) ? Integer.parseInt(lParts[i]) : 0;
                if (rNum > lNum) return true;
                if (rNum < lNum) return false;
            }
            return false;
        } catch (Exception e) {
            return !remoteVersion.trim().equalsIgnoreCase(localVersion.trim());
        }
    }

    private static String extractNumericSemver(String version) {
        if (version == null) return "0.0.0";
        Pattern p = Pattern.compile("(\\d+(\\.\\d+)+)");
        Matcher m = p.matcher(version);
        if (m.find()) {
            return m.group(1);
        }
        return version.replaceAll("[^0-9.]", "").trim();
    }

    public static void cleanOldApks(Context context) {
        try {
            File cacheDir = context.getExternalCacheDir() != null ? context.getExternalCacheDir() : context.getCacheDir();
            if (cacheDir != null && cacheDir.exists()) {
                File[] files = cacheDir.listFiles((dir, name) -> name != null && name.toLowerCase().endsWith(".apk"));
                if (files != null) {
                    for (File f : files) {
                        try {
                            if (f.exists()) {
                                f.delete();
                                Log.d(TAG, "Deleted old cached APK: " + f.getName());
                            }
                        } catch (Exception ignored) {}
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error cleaning old APKs: " + e.getMessage());
        }
    }
}
