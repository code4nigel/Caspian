package com.caspian.betab;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatGPTApiClient {

    public interface StreamCallback {
        void onTokenReceived(String fullAccumulatedText);
        void onError(String errorMessage);
        void onComplete();
    }

    public interface ModelsCallback {
        void onModelsFetched(List<ModelItem> models);
        void onError(String error);
    }

    public static class ModelItem {
        public String slug;
        public String title;
        public String description;

        public ModelItem(String slug, String title, String description) {
            this.slug = slug;
            this.title = title;
            this.description = description;
        }
    }

    private final OkHttpClient client;
    private final Handler mainHandler;

    public ChatGPTApiClient() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    // Dynamic Live Models Discovery directly from chatgpt.com API!
    public void fetchAvailableModels(String sessionToken, ModelsCallback callback) {
        new Thread(() -> {
            try {
                Request request = new Request.Builder()
                        .url("https://chatgpt.com/backend-api/models")
                        .addHeader("Authorization", "Bearer " + sessionToken)
                        .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36")
                        .get()
                        .build();

                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) {
                    mainHandler.post(() -> callback.onError("Failed to fetch models: HTTP " + response.code()));
                    return;
                }

                String jsonStr = response.body().string();
                JSONObject root = new JSONObject(jsonStr);
                JSONArray modelsArr = root.getJSONArray("models");

                List<ModelItem> list = new ArrayList<>();
                for (int i = 0; i < modelsArr.length(); i++) {
                    JSONObject m = modelsArr.getJSONObject(i);
                    String slug = m.optString("slug", "gpt-4o");
                    String title = m.optString("title", slug);
                    String desc = m.optString("description", "");
                    list.add(new ModelItem(slug, title, desc));
                }

                mainHandler.post(() -> callback.onModelsFetched(list));

            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Models discovery error: " + e.getLocalizedMessage()));
            }
        }).start();
    }

    // Live Streaming Message Handler
    public void sendMessage(String sessionToken, String modelSlug, String promptText, StreamCallback callback) {
        new Thread(() -> {
            try {
                JSONObject payload = new JSONObject();
                payload.put("action", "next");
                payload.put("model", modelSlug != null ? modelSlug : "gpt-4o");
                payload.put("parent_message_id", UUID.randomUUID().toString());

                JSONObject messageObj = new JSONObject();
                messageObj.put("id", UUID.randomUUID().toString());
                messageObj.put("role", "user");

                JSONObject contentObj = new JSONObject();
                contentObj.put("content_type", "text");
                JSONArray parts = new JSONArray();
                parts.put(promptText);
                contentObj.put("parts", parts);
                messageObj.put("content", contentObj);

                JSONArray messages = new JSONArray();
                messages.put(messageObj);
                payload.put("messages", messages);

                RequestBody body = RequestBody.create(
                        payload.toString(),
                        MediaType.parse("application/json; charset=utf-8")
                );

                Request request = new Request.Builder()
                        .url("https://chatgpt.com/backend-api/conversation")
                        .addHeader("Authorization", "Bearer " + sessionToken)
                        .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36")
                        .addHeader("Accept", "text/event-stream")
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) {
                    mainHandler.post(() -> callback.onError("Live API error: HTTP " + response.code() + " (" + response.message() + ")"));
                    return;
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(response.body().byteStream()));
                String line;
                StringBuilder accumulatedText = new StringBuilder();

                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data: ")) {
                        String dataStr = line.substring(6).trim();
                        if ("[DONE]".equalsIgnoreCase(dataStr)) {
                            break;
                        }
                        try {
                            JSONObject json = new JSONObject(dataStr);
                            if (json.has("message")) {
                                JSONObject msgObj = json.getJSONObject("message");
                                if (msgObj.has("content")) {
                                    JSONObject content = msgObj.getJSONObject("content");
                                    if (content.has("parts")) {
                                        JSONArray pArr = content.getJSONArray("parts");
                                        if (pArr.length() > 0) {
                                            String chunk = pArr.getString(0);
                                            accumulatedText.setLength(0);
                                            accumulatedText.append(chunk);
                                            String current = accumulatedText.toString();
                                            mainHandler.post(() -> callback.onTokenReceived(current));
                                        }
                                    }
                                }
                            }
                        } catch (Exception ignored) {}
                    }
                }

                reader.close();
                mainHandler.post(callback::onComplete);

            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Network stream exception: " + e.getLocalizedMessage()));
            }
        }).start();
    }
}
