package com.caspian.betad;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SearchSuggestionService {

    public interface SuggestionCallback {
        void onSuggestionsReady(String query, List<String> suggestions);
    }

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());
    private static String lastQuery = "";

    public static void fetchSuggestions(String query, SuggestionCallback callback) {
        if (query == null || query.trim().isEmpty()) {
            if (callback != null) callback.onSuggestionsReady(query, new ArrayList<>());
            return;
        }

        final String trimmedQuery = query.trim();
        lastQuery = trimmedQuery;

        executor.execute(() -> {
            List<String> results = new ArrayList<>();
            HttpURLConnection conn = null;
            try {
                String encoded = URLEncoder.encode(trimmedQuery, StandardCharsets.UTF_8.name());
                URL url = new URL("https://suggestqueries.google.com/complete/search?client=chrome&q=" + encoded);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(1500);
                conn.setReadTimeout(1500);
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36");

                if (conn.getResponseCode() == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    JSONArray jsonArray = new JSONArray(response.toString());
                    if (jsonArray.length() > 1) {
                        JSONArray suggestionsArray = jsonArray.getJSONArray(1);
                        for (int i = 0; i < Math.min(6, suggestionsArray.length()); i++) {
                            results.add(suggestionsArray.getString(i));
                        }
                    }
                }
            } catch (Exception ignored) {
            } finally {
                if (conn != null) conn.disconnect();
            }

            mainHandler.post(() -> {
                if (trimmedQuery.equals(lastQuery) && callback != null) {
                    callback.onSuggestionsReady(trimmedQuery, results);
                }
            });
        });
    }
}
