package com.caspian.betab;

public class ChatMessage {
    public static final int TYPE_USER = 1;
    public static final int TYPE_ASSISTANT = 2;

    public String id;
    public int type;
    public String text;
    public String modelService; // "chatgpt" or "gemini"
    public boolean isStreaming;
    public long timestamp;

    public ChatMessage(String id, int type, String text, String modelService, boolean isStreaming) {
        this.id = id;
        this.type = type;
        this.text = text;
        this.modelService = modelService;
        this.isStreaming = isStreaming;
        this.timestamp = System.currentTimeMillis();
    }
}
