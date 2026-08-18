package com.caspian.betac;

import android.net.Uri;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class AICommandRouter {

    public enum SearchEngine {
        GOOGLE("Google", "https://www.google.com/search?q="),
        DUCKDUCKGO("DuckDuckGo", "https://duckduckgo.com/?q="),
        BRAVE("Brave", "https://search.brave.com/search?q="),
        BING("Bing", "https://www.bing.com/search?q=");

        public final String name;
        public final String searchUrl;

        SearchEngine(String name, String searchUrl) {
            this.name = name;
            this.searchUrl = searchUrl;
        }
    }

    public static class RouteResult {
        public final String targetUrl;
        public final String service; // "chatgpt", "gemini", "claude", "deepseek", "perplexity", "youtube", "web", "reader"
        public final String promptPayload;
        public final boolean isReaderMode;

        public RouteResult(String targetUrl, String service, String promptPayload, boolean isReaderMode) {
            this.targetUrl = targetUrl;
            this.service = service;
            this.promptPayload = promptPayload;
            this.isReaderMode = isReaderMode;
        }
    }

    public static RouteResult resolve(String rawInput, SearchEngine defaultEngine) {
        if (rawInput == null || rawInput.trim().isEmpty()) {
            return new RouteResult("https://www.google.com", "web", null, false);
        }

        String input = rawInput.trim();
        String lower = input.toLowerCase();

        // 1. AI Routing Prefixes
        if (lower.startsWith("@gpt ") || lower.equals("@gpt")) {
            String prompt = input.length() > 4 ? input.substring(5).trim() : "";
            return new RouteResult("https://chatgpt.com", "chatgpt", prompt, false);
        }

        if (lower.startsWith("@gemini ") || lower.equals("@gemini")) {
            String prompt = input.length() > 7 ? input.substring(8).trim() : "";
            return new RouteResult("https://gemini.google.com/app", "gemini", prompt, false);
        }

        if (lower.startsWith("@claude ") || lower.equals("@claude")) {
            String prompt = input.length() > 7 ? input.substring(8).trim() : "";
            return new RouteResult("https://claude.ai/new", "claude", prompt, false);
        }

        if (lower.startsWith("@deepseek ") || lower.equals("@deepseek")) {
            String prompt = input.length() > 9 ? input.substring(10).trim() : "";
            return new RouteResult("https://chat.deepseek.com", "deepseek", prompt, false);
        }

        if (lower.startsWith("@perplexity ") || lower.startsWith("@pplx ")) {
            int prefixLen = lower.startsWith("@perplexity ") ? 12 : 6;
            String query = input.substring(prefixLen).trim();
            String encoded = encodeParam(query);
            return new RouteResult("https://www.perplexity.ai/search?q=" + encoded, "perplexity", query, false);
        }

        if (lower.startsWith("@yt ") || lower.equals("@yt")) {
            String query = input.length() > 3 ? input.substring(4).trim() : "";
            if (query.isEmpty()) {
                return new RouteResult("https://m.youtube.com", "youtube", null, false);
            }
            return new RouteResult("https://m.youtube.com/results?search_query=" + encodeParam(query), "youtube", query, false);
        }

        if (lower.startsWith("@read ")) {
            String target = input.substring(6).trim();
            if (!target.startsWith("http://") && !target.startsWith("https://")) {
                target = "https://" + target;
            }
            return new RouteResult(target, "reader", null, true);
        }

        // 2. Direct URL Detection
        if (input.startsWith("http://") || input.startsWith("https://") || input.startsWith("file://") || input.startsWith("caspian://")) {
            return new RouteResult(input, detectServiceFromUrl(input), null, false);
        }

        // Hostname with domain extension check (e.g. google.com, github.com/user, news.ycombinator.com)
        if (isLikelyUrl(input)) {
            String target = "https://" + input;
            return new RouteResult(target, detectServiceFromUrl(target), null, false);
        }

        // 3. Fallback to Web Search Engine
        SearchEngine engine = defaultEngine != null ? defaultEngine : SearchEngine.GOOGLE;
        String searchUrl = engine.searchUrl + encodeParam(input);
        return new RouteResult(searchUrl, "web", input, false);
    }

    public static String detectServiceFromUrl(String url) {
        if (url == null) return "web";
        String lower = url.toLowerCase();
        if (lower.contains("chatgpt.com") || lower.contains("chat.openai.com")) return "chatgpt";
        if (lower.contains("gemini.google.com")) return "gemini";
        if (lower.contains("claude.ai")) return "claude";
        if (lower.contains("chat.deepseek.com")) return "deepseek";
        if (lower.contains("perplexity.ai")) return "perplexity";
        if (lower.contains("youtube.com") || lower.contains("youtu.be")) return "youtube";
        if (lower.contains("google.com/search")) return "google_search";
        return "web";
    }

    private static boolean isLikelyUrl(String input) {
        if (input.contains(" ")) return false;
        if (input.startsWith("localhost") || input.matches("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}(:\\d+)?.*")) return true;
        
        int dotIndex = input.indexOf('.');
        if (dotIndex > 0 && dotIndex < input.length() - 1) {
            String tld = input.substring(dotIndex + 1).split("/")[0].split(":")[0];
            return tld.matches("^[a-zA-Z]{2,12}$");
        }
        return false;
    }

    private static String encodeParam(String text) {
        try {
            return URLEncoder.encode(text, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            return text;
        }
    }
}
