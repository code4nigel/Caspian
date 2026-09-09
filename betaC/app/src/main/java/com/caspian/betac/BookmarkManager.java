package com.caspian.betac;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BookmarkManager {
    private static final String TAG = "BookmarkManager";
    private static final String PREF_NAME = "caspian_bookmarks_storage";
    private static final String KEY_BOOKMARKS = "bookmarks_json";
    private static final String KEY_FOLDERS = "folders_json";

    public static class BookmarkItem {
        public String id;
        public String title;
        public String url;
        public String folder; // "Default", "DEV & SPECS", "AI PROMPTS", etc.
        public long timestamp;
        public boolean isFavorite;
        public String tags; // Comma-separated or single category tag
        public String faviconB64;

        public BookmarkItem(String id, String title, String url, String folder, long timestamp, boolean isFavorite, String tags) {
            this.id = id;
            this.title = title != null ? title : "Untitled";
            this.url = url != null ? url : "";
            this.folder = folder != null ? folder : "Default";
            this.timestamp = timestamp > 0 ? timestamp : System.currentTimeMillis();
            this.isFavorite = isFavorite;
            this.tags = tags != null ? tags : "";
        }

        public JSONObject toJson() {
            JSONObject obj = new JSONObject();
            try {
                obj.put("id", id);
                obj.put("title", title);
                obj.put("url", url);
                obj.put("folder", folder);
                obj.put("timestamp", timestamp);
                obj.put("isFavorite", isFavorite);
                obj.put("tags", tags);
                if (faviconB64 != null) obj.put("faviconB64", faviconB64);
            } catch (Exception ignored) {}
            return obj;
        }

        public static BookmarkItem fromJson(JSONObject obj) {
            if (obj == null) return null;
            String id = obj.optString("id", "bm_" + System.currentTimeMillis());
            String title = obj.optString("title", "Bookmark");
            String url = obj.optString("url", "");
            String folder = obj.optString("folder", "Default");
            long ts = obj.optLong("timestamp", System.currentTimeMillis());
            boolean fav = obj.optBoolean("isFavorite", false);
            String tags = obj.optString("tags", "");
            BookmarkItem item = new BookmarkItem(id, title, url, folder, ts, fav, tags);
            item.faviconB64 = obj.optString("faviconB64", null);
            return item;
        }
    }

    private final Context context;
    private final SharedPreferences prefs;
    private final List<BookmarkItem> bookmarks = new ArrayList<>();
    private final List<String> folders = new ArrayList<>();

    public BookmarkManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = this.context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        load();
    }

    private synchronized void load() {
        bookmarks.clear();
        folders.clear();
        String bmJson = prefs.getString(KEY_BOOKMARKS, null);
        String fJson = prefs.getString(KEY_FOLDERS, null);

        if (fJson != null) {
            try {
                JSONArray arr = new JSONArray(fJson);
                for (int i = 0; i < arr.length(); i++) {
                    String f = arr.optString(i);
                    if (f != null && !f.trim().isEmpty() && !folders.contains(f.trim())) {
                        folders.add(f.trim());
                    }
                }
            } catch (Exception ignored) {}
        }
        if (folders.isEmpty()) {
            folders.add("DEV & SPECS");
            folders.add("AI PROMPTS");
            folders.add("FAVORITES");
            folders.add("READING LIST");
        }

        if (bmJson != null) {
            try {
                JSONArray arr = new JSONArray(bmJson);
                for (int i = 0; i < arr.length(); i++) {
                    BookmarkItem item = BookmarkItem.fromJson(arr.getJSONObject(i));
                    if (item != null) bookmarks.add(item);
                }
            } catch (Exception ignored) {}
        }

        // Seed initial bookmarks if totally empty
        if (bookmarks.isEmpty()) {
            seedDefaults();
            save();
        }
    }

    private void seedDefaults() {
        long now = System.currentTimeMillis();
        bookmarks.add(new BookmarkItem("bm_1", "Caspian Browser Engine V4 Architecture Spec", "https://notion.so/caspian-team/v4", "DEV & SPECS", now - 172800000L, true, "DEV & SPECS"));
        bookmarks.add(new BookmarkItem("bm_2", "Self-Routing Latent Context in Mini LLMs", "https://arxiv.org/abs/2411.0924", "AI PROMPTS", now - 345600000L, false, "AI PROMPTS"));
        bookmarks.add(new BookmarkItem("bm_3", "Caspian Native Mobile Tokens & HUD Kit", "https://figma.com/@caspian/hud-tokens", "DEV & SPECS", now - 604800000L, true, "DEV & SPECS"));
        bookmarks.add(new BookmarkItem("bm_4", "Subgrid & Fluid Typography Guidelines", "https://tailwindcss.com/docs/subgrid", "DEV & SPECS", now - 864000000L, false, "DEV & SPECS"));
        bookmarks.add(new BookmarkItem("bm_5", "vllm-project/vllm: High-throughput Serving", "https://github.com/vllm-project/vllm", "DEV & SPECS", now - 1209600000L, false, "DEV & SPECS"));
        bookmarks.add(new BookmarkItem("bm_6", "ChatGPT Next-Gen Reasoning Prompt Library", "https://chatgpt.com", "AI PROMPTS", now - 1500000000L, true, "AI PROMPTS"));
    }

    public synchronized void save() {
        try {
            JSONArray bmArr = new JSONArray();
            for (BookmarkItem item : bookmarks) {
                bmArr.put(item.toJson());
            }
            JSONArray fArr = new JSONArray();
            for (String f : folders) {
                fArr.put(f);
            }
            prefs.edit()
                    .putString(KEY_BOOKMARKS, bmArr.toString())
                    .putString(KEY_FOLDERS, fArr.toString())
                    .apply();
        } catch (Exception e) {
            Log.e(TAG, "Error saving bookmarks: " + e.getMessage());
        }
    }

    public synchronized List<BookmarkItem> getAllBookmarks() {
        return new ArrayList<>(bookmarks);
    }

    public synchronized List<String> getFolders() {
        return new ArrayList<>(folders);
    }

    public synchronized void createFolder(String folderName) {
        if (folderName == null) return;
        String clean = folderName.trim();
        if (!clean.isEmpty() && !folders.contains(clean)) {
            folders.add(clean);
            save();
        }
    }

    public synchronized void renameFolder(String oldName, String newName) {
        if (oldName == null || newName == null) return;
        String cleanOld = oldName.trim();
        String cleanNew = newName.trim();
        if (cleanOld.isEmpty() || cleanNew.isEmpty() || cleanOld.equalsIgnoreCase(cleanNew)) return;

        int idx = -1;
        for (int i = 0; i < folders.size(); i++) {
            if (folders.get(i).equalsIgnoreCase(cleanOld)) {
                idx = i;
                break;
            }
        }
        if (idx != -1) {
            folders.set(idx, cleanNew);
        } else {
            folders.add(cleanNew);
        }

        for (BookmarkItem item : bookmarks) {
            if (item.folder != null && item.folder.equalsIgnoreCase(cleanOld)) {
                item.folder = cleanNew;
            }
        }
        save();
    }

    public synchronized void deleteFolder(String folderName) {
        if (folderName == null) return;
        String clean = folderName.trim();
        int idx = -1;
        for (int i = 0; i < folders.size(); i++) {
            if (folders.get(i).equalsIgnoreCase(clean)) {
                idx = i;
                break;
            }
        }
        if (idx != -1) {
            folders.remove(idx);
        }

        for (BookmarkItem item : bookmarks) {
            if (item.folder != null && item.folder.equalsIgnoreCase(clean)) {
                item.folder = "Default";
            }
        }
        save();
    }

    public synchronized void addBookmark(String title, String url, String folder, String tag) {
        if (url == null || url.trim().isEmpty()) return;
        String id = "bm_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
        String f = (folder != null && !folder.trim().isEmpty()) ? folder.trim() : "Default";
        BookmarkItem item = new BookmarkItem(id, title, url, f, System.currentTimeMillis(), false, tag);
        bookmarks.add(0, item);
        if (!f.equals("Default") && !folders.contains(f)) {
            folders.add(f);
        }
        save();
    }

    public synchronized boolean deleteBookmark(String id) {
        if (id == null) return false;
        for (int i = 0; i < bookmarks.size(); i++) {
            if (id.equals(bookmarks.get(i).id)) {
                bookmarks.remove(i);
                save();
                return true;
            }
        }
        return false;
    }

    public synchronized void batchDelete(List<String> ids) {
        if (ids == null || ids.isEmpty()) return;
        Set<String> set = new HashSet<>(ids);
        List<BookmarkItem> toRemove = new ArrayList<>();
        for (BookmarkItem item : bookmarks) {
            if (set.contains(item.id)) toRemove.add(item);
        }
        bookmarks.removeAll(toRemove);
        save();
    }

    public synchronized void toggleFavorite(String id) {
        if (id == null) return;
        for (BookmarkItem item : bookmarks) {
            if (id.equals(item.id)) {
                item.isFavorite = !item.isFavorite;
                save();
                break;
            }
        }
    }

    public synchronized void updateBookmark(String id, String newTitle, String newUrl, String newFolder) {
        if (id == null) return;
        for (BookmarkItem item : bookmarks) {
            if (id.equals(item.id)) {
                if (newTitle != null) item.title = newTitle;
                if (newUrl != null) item.url = newUrl;
                if (newFolder != null) item.folder = newFolder;
                save();
                break;
            }
        }
    }

    // Export Netscape HTML Format (Chrome, Edge, Brave, Firefox, Safari compatible)
    public synchronized void exportNetscapeHtml(OutputStream out) throws Exception {
        OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
        writer.write("<!DOCTYPE NETSCAPE-Bookmark-file-1>\n");
        writer.write("<!-- This is an automatically generated file. It will be read and overwritten. DO NOT EDIT! -->\n");
        writer.write("<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=UTF-8\">\n");
        writer.write("<TITLE>Bookmarks</TITLE>\n");
        writer.write("<H1>Bookmarks</H1>\n");
        writer.write("<DL><p>\n");

        // Group by folder
        Set<String> exportedFolders = new HashSet<>();
        for (String folder : folders) {
            List<BookmarkItem> inFolder = new ArrayList<>();
            for (BookmarkItem b : bookmarks) {
                if (folder.equalsIgnoreCase(b.folder)) inFolder.add(b);
            }
            if (!inFolder.isEmpty()) {
                exportedFolders.add(folder.toLowerCase());
                writer.write("    <DT><H3 ADD_DATE=\"" + (System.currentTimeMillis() / 1000) + "\">" + escapeHtml(folder) + "</H3>\n");
                writer.write("    <DL><p>\n");
                for (BookmarkItem b : inFolder) {
                    writer.write("        <DT><A HREF=\"" + escapeHtml(b.url) + "\" ADD_DATE=\"" + (b.timestamp / 1000) + "\">" + escapeHtml(b.title) + "</A>\n");
                }
                writer.write("    </DL><p>\n");
            }
        }

        // Remaining un-categorized bookmarks
        for (BookmarkItem b : bookmarks) {
            if (b.folder == null || !exportedFolders.contains(b.folder.toLowerCase())) {
                writer.write("    <DT><A HREF=\"" + escapeHtml(b.url) + "\" ADD_DATE=\"" + (b.timestamp / 1000) + "\">" + escapeHtml(b.title) + "</A>\n");
            }
        }

        writer.write("</DL><p>\n");
        writer.flush();
    }

    // Export Caspian settings & Bookmarks backup JSON
    public synchronized void exportSettingsJson(OutputStream out, Context ctx) throws Exception {
        JSONObject root = new JSONObject();
        root.put("app", "Caspian Flow");
        root.put("version", "1.2.42-BetaC");
        root.put("exportTime", System.currentTimeMillis());

        // Bookmarks
        JSONArray bmArr = new JSONArray();
        for (BookmarkItem b : bookmarks) {
            bmArr.put(b.toJson());
        }
        root.put("bookmarks", bmArr);

        // Folders
        JSONArray fArr = new JSONArray();
        for (String f : folders) fArr.put(f);
        root.put("folders", fArr);

        // Caspian General Settings
        SharedPreferences appPrefs = ctx.getSharedPreferences("caspian_prefs", Context.MODE_PRIVATE);
        JSONObject settingsObj = new JSONObject();
        for (String k : appPrefs.getAll().keySet()) {
            Object v = appPrefs.getAll().get(k);
            settingsObj.put(k, v);
        }
        root.put("settings", settingsObj);

        OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
        writer.write(root.toString(2));
        writer.flush();
    }

    // Import from Netscape HTML or generic HTML bookmark export
    public synchronized int importFromHtml(InputStream in) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        String html = sb.toString();

        // Pattern matching for <A HREF="url"...>Title</A>
        Pattern p = Pattern.compile("<a\\s+(?:[^>]*?\\s+)?href=([\"'])(.*?)\\1[^>]*>(.*?)<\\/a>", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(html);
        int count = 0;
        long now = System.currentTimeMillis();
        while (m.find()) {
            String url = m.group(2);
            String title = m.group(3);
            if (url != null && !url.trim().isEmpty()) {
                String cleanTitle = (title != null && !title.trim().isEmpty()) ? stripHtml(title.trim()) : url;
                String id = "bm_imp_" + (now + count);
                BookmarkItem item = new BookmarkItem(id, cleanTitle, url.trim(), "Imported", now, false, "Imported");
                bookmarks.add(0, item);
                count++;
            }
        }
        if (count > 0) {
            if (!folders.contains("Imported")) folders.add("Imported");
            save();
        }
        return count;
    }

    // Import from JSON format
    public synchronized int importFromJson(InputStream in) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        String jsonStr = sb.toString();
        JSONObject root = new JSONObject(jsonStr);
        int count = 0;
        if (root.has("bookmarks")) {
            JSONArray arr = root.getJSONArray("bookmarks");
            for (int i = 0; i < arr.length(); i++) {
                BookmarkItem item = BookmarkItem.fromJson(arr.getJSONObject(i));
                if (item != null && item.url != null && !item.url.isEmpty()) {
                    bookmarks.add(0, item);
                    if (item.folder != null && !folders.contains(item.folder)) {
                        folders.add(item.folder);
                    }
                    count++;
                }
            }
            if (root.has("folders")) {
                JSONArray fArr = root.getJSONArray("folders");
                for (int j = 0; j < fArr.length(); j++) {
                    String f = fArr.getString(j);
                    if (f != null && !folders.contains(f)) folders.add(f);
                }
            }
            save();
        }
        return count;
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String stripHtml(String html) {
        if (html == null) return "";
        return html.replaceAll("<[^>]*>", "").replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"");
    }
}
