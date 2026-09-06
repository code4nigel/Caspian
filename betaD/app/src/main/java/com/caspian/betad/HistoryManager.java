package com.caspian.betad;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.webkit.CookieManager;
import android.webkit.WebStorage;
import java.util.ArrayList;
import java.util.List;

public class HistoryManager extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "caspian_history.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_HISTORY = "history";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_URL = "url";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    private static HistoryManager instance;

    public static class HistoryEntry {
        public final long id;
        public final String title;
        public final String url;
        public final long timestamp;

        public HistoryEntry(long id, String title, String url, long timestamp) {
            this.id = id;
            this.title = title;
            this.url = url;
            this.timestamp = timestamp;
        }
    }

    public static synchronized HistoryManager getInstance(Context context) {
        if (instance == null) {
            instance = new HistoryManager(context.getApplicationContext());
        }
        return instance;
    }

    private HistoryManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_HISTORY + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_URL + " TEXT, " +
                COLUMN_TIMESTAMP + " INTEGER" +
                ")";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
        onCreate(db);
    }

    public void addEntry(String title, String url) {
        if (url == null || url.trim().isEmpty() || url.startsWith("file://") || url.startsWith("caspian://") || url.startsWith("about:")) {
            return;
        }
        try {
            SQLiteDatabase db = getWritableDatabase();
            // Avoid duplicate consecutive identical URL entries
            db.delete(TABLE_HISTORY, COLUMN_URL + " = ?", new String[]{url});

            ContentValues values = new ContentValues();
            values.put(COLUMN_TITLE, (title != null && !title.trim().isEmpty()) ? title.trim() : url);
            values.put(COLUMN_URL, url.trim());
            values.put(COLUMN_TIMESTAMP, System.currentTimeMillis());
            db.insert(TABLE_HISTORY, null, values);
        } catch (Exception ignored) {}
    }

    public List<HistoryEntry> getHistory(String searchQuery) {
        List<HistoryEntry> entries = new ArrayList<>();
        try {
            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor;
            if (searchQuery != null && !searchQuery.trim().isEmpty()) {
                String query = "%" + searchQuery.trim() + "%";
                cursor = db.query(TABLE_HISTORY,
                        new String[]{COLUMN_ID, COLUMN_TITLE, COLUMN_URL, COLUMN_TIMESTAMP},
                        COLUMN_TITLE + " LIKE ? OR " + COLUMN_URL + " LIKE ?",
                        new String[]{query, query},
                        null, null, COLUMN_TIMESTAMP + " DESC", "100");
            } else {
                cursor = db.query(TABLE_HISTORY,
                        new String[]{COLUMN_ID, COLUMN_TITLE, COLUMN_URL, COLUMN_TIMESTAMP},
                        null, null, null, null, COLUMN_TIMESTAMP + " DESC", "100");
            }

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
                    String url = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_URL));
                    long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP));
                    entries.add(new HistoryEntry(id, title, url, timestamp));
                }
                cursor.close();
            }
        } catch (Exception ignored) {}
        return entries;
    }

    public void deleteEntry(long id) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            db.delete(TABLE_HISTORY, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        } catch (Exception ignored) {}
    }

    public void clearHistorySince(long cutoffTimeMillis) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            db.delete(TABLE_HISTORY, COLUMN_TIMESTAMP + " >= ?", new String[]{String.valueOf(cutoffTimeMillis)});
        } catch (Exception ignored) {}
    }

    public void clearAllHistory() {
        try {
            SQLiteDatabase db = getWritableDatabase();
            db.delete(TABLE_HISTORY, null, null);
        } catch (Exception ignored) {}
    }

    public static void clearCookiesAndCache() {
        try {
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
            WebStorage.getInstance().deleteAllData();
        } catch (Exception ignored) {}
    }
}
