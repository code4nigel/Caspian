package com.caspian.betac;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.os.Environment;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Native PDF Generator for Caspian Flow.
 * Generates beautifully formatted, multi-page vector PDF documents directly on-device
 * supporting KaTeX math formulas, code snippets, tables, diagrams, callouts, and images.
 */
public class CaspianPdfExporter {

    private static final String TAG = "CaspianPdfExporter";

    // Standard A4 dimensions in typographic points (72 DPI)
    public static final int PAGE_WIDTH = 595;
    public static final int PAGE_HEIGHT = 842;

    public static final float MARGIN_LEFT = 36f;
    public static final float MARGIN_RIGHT = 559f;
    public static final float MARGIN_TOP = 44f;
    public static final float MARGIN_BOTTOM = 798f;
    public static final float CONTENT_WIDTH = MARGIN_RIGHT - MARGIN_LEFT; // 523pt

    public interface OnPdfGeneratedCallback {
        void onSuccess(File pdfFile);
        void onError(Throwable error);
    }

    /**
     * Renders a rich HTML document (with KaTeX, Prism code blocks, tables, diagrams) into a multi-page A4 PDF.
     */
    public static void renderHtmlToPdf(Activity activity, String htmlContent, String jobName, File targetFile, OnPdfGeneratedCallback callback) {
        if (activity == null || htmlContent == null) {
            if (callback != null) callback.onError(new IllegalArgumentException("Activity or HTML is null"));
            return;
        }

        activity.runOnUiThread(() -> {
            try {
                WebView webView = new WebView(activity);
                WebSettings settings = webView.getSettings();
                settings.setJavaScriptEnabled(true);
                settings.setDomStorageEnabled(true);
                settings.setLoadsImagesAutomatically(true);

                // Standard A4 width in px at 96 DPI: 794px, height: 1123px
                final int webWidth = 794;
                final float a4Ratio = 842f / 595f; // ~1.4151
                final int pageHeightPx = (int) Math.round(webWidth * a4Ratio); // ~1124px

                webView.layout(0, 0, webWidth, pageHeightPx);

                webView.setWebViewClient(new WebViewClient() {
                    private boolean isFinished = false;

                    @Override
                    public void onPageFinished(WebView view, String url) {
                        if (isFinished) return;
                        isFinished = true;

                        // Give KaTeX and math auto-render script time to complete typesetting
                        view.postDelayed(() -> {
                            try {
                                view.measure(
                                        View.MeasureSpec.makeMeasureSpec(webWidth, View.MeasureSpec.EXACTLY),
                                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                                );
                                int totalHeight = view.getMeasuredHeight();
                                if (totalHeight <= 0) {
                                    totalHeight = (int) (view.getContentHeight() * view.getScale());
                                }
                                if (totalHeight <= 0) totalHeight = pageHeightPx;

                                view.layout(0, 0, webWidth, totalHeight);

                                int pageCount = (int) Math.ceil((double) totalHeight / pageHeightPx);
                                if (pageCount < 1) pageCount = 1;

                                PdfDocument pdfDocument = new PdfDocument();
                                float scale = 595f / webWidth;

                                for (int i = 0; i < pageCount; i++) {
                                    PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, i + 1).create();
                                    PdfDocument.Page page = pdfDocument.startPage(pageInfo);
                                    Canvas canvas = page.getCanvas();

                                    canvas.scale(scale, scale);
                                    canvas.translate(0, -i * pageHeightPx);
                                    view.draw(canvas);

                                    pdfDocument.finishPage(page);
                                }

                                File parent = targetFile.getParentFile();
                                if (parent != null && !parent.exists()) parent.mkdirs();

                                try (FileOutputStream fos = new FileOutputStream(targetFile)) {
                                    pdfDocument.writeTo(fos);
                                    fos.flush();
                                }
                                pdfDocument.close();

                                try { view.destroy(); } catch (Throwable ignored) {}

                                if (callback != null) {
                                    activity.runOnUiThread(() -> callback.onSuccess(targetFile));
                                }
                            } catch (Throwable t) {
                                Log.e(TAG, "Error slicing WebView to PDF", t);
                                try { view.destroy(); } catch (Throwable ignored) {}
                                if (callback != null) {
                                    activity.runOnUiThread(() -> callback.onError(t));
                                }
                            }
                        }, 500);
                    }
                });

                webView.loadDataWithBaseURL("https://cdn.jsdelivr.net/", htmlContent, "text/html", "utf-8", null);

            } catch (Throwable t) {
                Log.e(TAG, "renderHtmlToPdf error", t);
                if (callback != null) callback.onError(t);
            }
        });
    }

    /**
     * Builds complete, rich HTML document with KaTeX, code formatting, tables, diagrams, and callout styles.
     */
    public static String buildRichHtmlDocument(String title, JSONArray turnsArray, String service, String scope, String dateStr) {
        StringBuilder turnsHtml = new StringBuilder();
        String detectionService = service != null ? service : "chatgpt";

        try {
            if (turnsArray != null) {
                for (int i = 0; i < turnsArray.length(); i++) {
                    JSONObject obj = turnsArray.getJSONObject(i);
                    int idx = obj.optInt("index", i + 1);
                    String html = obj.optString("html", obj.optString("text", ""));
                    String author = obj.optString("author", (idx % 2 != 0) ? "user" : "assistant");
                    String turnService = obj.optString("service", detectionService);
                    detectionService = turnService;

                    boolean isUser = "user".equalsIgnoreCase(author);
                    boolean isGemini = "gemini".equalsIgnoreCase(turnService);

                    // Filter by scope
                    if ("ai_only".equalsIgnoreCase(scope) && isUser) {
                        continue;
                    }
                    if ("user_only".equalsIgnoreCase(scope) && !isUser) {
                        continue;
                    }

                    String bgStyle = isUser ? "background: #f8fafc; border: 1px solid #e2e8f0;" : "background: #ffffff; border: 1px solid #cbd5e1;";
                    String badgeBg = isUser ? (isGemini ? "background: #153d6f; color: #ffffff;" : "background: #1d4ed8; color: #ffffff;")
                            : "background: #047857; color: #ffffff;";
                    String senderLabel = isUser ? "User Prompt" : (isGemini ? "Gemini Response" : "ChatGPT Response");

                    turnsHtml.append("<div class=\"turn-card\" style=\"margin-bottom: 24px; border-radius: 12px; ").append(bgStyle)
                            .append(" padding: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.04); page-break-inside: avoid; break-inside: avoid;\">")
                            .append("<div style=\"display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; border-bottom: 1px solid #ececf1; padding-bottom: 8px;\">")
                            .append("<span style=\"font-weight: 700; font-size: 11px; padding: 4px 10px; border-radius: 6px; text-transform: uppercase; letter-spacing: 0.5px; ").append(badgeBg).append("\">").append(senderLabel).append(" #").append(idx).append("</span>")
                            .append("<span style=\"font-size: 10px; color: #8e8ea0; font-weight: 600;\">Caspian Flow</span>")
                            .append("</div>")
                            .append("<div class=\"chat-turn-content\" style=\"font-size: 14px; line-height: 1.7; color: #1e293b; font-family: 'Inter', -apple-system, sans-serif;\">")
                            .append(html)
                            .append("</div></div>");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error formatting turns HTML", e);
        }

        String badgeTitle = "gemini".equalsIgnoreCase(detectionService) ? "Caspian Flow (Gemini)" : "Caspian Flow (ChatGPT)";
        if ("ai_only".equalsIgnoreCase(scope)) badgeTitle += " • AI Only";
        else if ("user_only".equalsIgnoreCase(scope)) badgeTitle += " • Prompts Only";

        return "<!DOCTYPE html><html><head><meta charset=\"utf-8\"><title>" + title + "</title>" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "<style>" +
                "@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@400;500;600&display=swap');" +
                "@import url('https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/katex.min.css');" +
                "* { box-sizing: border-box; }" +
                "body { font-family: 'Inter', -apple-system, sans-serif; padding: 36px 44px; background: #ffffff; color: #1e293b; max-width: 850px; margin: 0 auto; -webkit-print-color-adjust: exact; }" +
                "h1 { font-size: 22px; font-weight: 700; color: #0f172a; margin: 0 0 8px 0; font-family: 'Inter', sans-serif; }" +
                ".doc-header { border-bottom: 2px solid #1B4264; padding-bottom: 14px; margin-bottom: 24px; display: flex; justify-content: space-between; align-items: center; }" +
                ".doc-meta { font-size: 11px; color: #64748b; font-weight: 500; }" +
                // Code Blocks
                "pre { background: #1e1e2e !important; color: #cdd6f4 !important; padding: 14px 16px; border-radius: 8px; overflow-x: auto; font-family: 'JetBrains Mono', monospace; font-size: 12.5px; line-height: 1.55; margin: 14px 0; border: 1px solid #313244; page-break-inside: avoid; break-inside: avoid; }" +
                "code { font-family: 'JetBrains Mono', monospace; font-size: 12.5px; background: rgba(175,184,193,0.18); padding: 2px 6px; border-radius: 4px; color: #0f172a; }" +
                "pre code { background: transparent !important; padding: 0; color: inherit; }" +
                // Formulas & Math
                ".katex-display { background: #f8fafc; padding: 12px 16px; border-radius: 8px; border: 1px solid #e2e8f0; overflow-x: auto; margin: 14px 0; page-break-inside: avoid; break-inside: avoid; text-align: center; }" +
                ".katex { font-size: 1.08em; }" +
                // Tables
                "table { width: 100%; border-collapse: collapse; margin: 16px 0; font-size: 13px; border-radius: 8px; overflow: hidden; border: 1px solid #cbd5e1; page-break-inside: avoid; break-inside: avoid; }" +
                "th { background: #f1f5f9; font-weight: 700; color: #0f172a; padding: 10px 14px; border: 1px solid #cbd5e1; text-align: left; }" +
                "td { padding: 9px 14px; border: 1px solid #e2e8f0; color: #334155; }" +
                "tr:nth-child(even) { background: #f8fafc; }" +
                // Callouts & Blockquotes
                "blockquote { border-left: 4px solid #1B4264; background: #f8fafc; margin: 14px 0; padding: 12px 18px; border-radius: 0 8px 8px 0; color: #475569; font-style: normal; page-break-inside: avoid; break-inside: avoid; }" +
                // Images & Diagrams
                "img { max-width: 100%; height: auto; border-radius: 8px; margin: 12px 0; box-shadow: 0 4px 12px rgba(0,0,0,0.08); page-break-inside: avoid; break-inside: avoid; }" +
                "svg { max-width: 100%; height: auto; margin: 12px 0; }" +
                ".diagram-container { text-align: center; margin: 14px 0; }" +
                "@media print { body { padding: 20px; } .turn-card, pre, table, .katex-display, blockquote { page-break-inside: avoid !important; break-inside: avoid !important; } }" +
                "</style>" +
                "<script src=\"https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/katex.min.js\"></script>" +
                "<script src=\"https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/contrib/auto-render.min.js\"></script>" +
                "<script>" +
                "document.addEventListener('DOMContentLoaded', function() {" +
                "  if (window.renderMathInElement) {" +
                "    renderMathInElement(document.body, {" +
                "      delimiters: [" +
                "        {left: '$$', right: '$$', display: true}," +
                "        {left: '$', right: '$', display: false}," +
                "        {left: '\\\\(', right: '\\\\)', display: false}," +
                "        {left: '\\\\[', right: '\\\\]', display: true}" +
                "      ]," +
                "      throwOnError: false" +
                "    });" +
                "  }" +
                "});" +
                "</script>" +
                "</head><body>" +
                "<div class=\"doc-header\"><div><h1>" + title + "</h1><div class=\"doc-meta\">Exported via Caspian Flow &bull; " + dateStr + "</div></div>" +
                "<div style=\"font-size: 11px; font-weight: 700; color: #1B4264; border: 1.5px solid #1B4264; padding: 4px 10px; border-radius: 6px; letter-spacing: 0.5px;\">" + badgeTitle + "</div></div>" +
                turnsHtml.toString() + "</body></html>";
    }

    /**
     * Fallback vector PDF generator (when headless WebView is unavailable).
     */
    public static File exportConversationToPdf(Context context, String title, JSONArray turnsArray, String service, String scope, String customFileName) {
        if (context == null) return null;

        PdfDocument pdfDocument = new PdfDocument();
        int pageIndex = 1;

        try {
            final int COLOR_SLATE_900 = 0xFF0F172A;
            final int COLOR_SLATE_700 = 0xFF334155;
            final int COLOR_SLATE_500 = 0xFF64748B;
            final int COLOR_SLATE_200 = 0xFFE2E8F0;
            final int COLOR_SLATE_50  = 0xFFF8FAFC;
            final int COLOR_BLUE_700  = 0xFF1D4ED8;
            final int COLOR_EMERALD_700 = 0xFF047857;
            final int COLOR_NAVY_800  = 0xFF153D6F;

            TextPaint titlePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            titlePaint.setColor(COLOR_SLATE_900);
            titlePaint.setTextSize(16f);
            titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

            TextPaint subPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            subPaint.setColor(COLOR_SLATE_500);
            subPaint.setTextSize(8.5f);

            TextPaint badgeTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            badgeTextPaint.setColor(Color.WHITE);
            badgeTextPaint.setTextSize(8f);
            badgeTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

            TextPaint bodyPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            bodyPaint.setColor(COLOR_SLATE_700);
            bodyPaint.setTextSize(9.5f);

            Paint badgeBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            badgeBgPaint.setStyle(Paint.Style.FILL);

            Paint cardBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            cardBgPaint.setStyle(Paint.Style.FILL);

            Paint cardStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            cardStrokePaint.setStyle(Paint.Style.STROKE);
            cardStrokePaint.setStrokeWidth(0.75f);
            cardStrokePaint.setColor(COLOR_SLATE_200);

            Paint dividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            dividerPaint.setStyle(Paint.Style.STROKE);
            dividerPaint.setStrokeWidth(1f);
            dividerPaint.setColor(COLOR_SLATE_200);

            Paint chipBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            chipBgPaint.setStyle(Paint.Style.FILL);
            chipBgPaint.setColor(COLOR_NAVY_800);

            TextPaint chipTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            chipTextPaint.setColor(Color.WHITE);
            chipTextPaint.setTextSize(7.5f);
            chipTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageIndex).create();
            PdfDocument.Page page = pdfDocument.startPage(pageInfo);
            Canvas canvas = page.getCanvas();

            float currentY = MARGIN_TOP;

            // Top Chip
            RectF chipRect = new RectF(MARGIN_LEFT, currentY, MARGIN_LEFT + 74f, currentY + 16f);
            canvas.drawRoundRect(chipRect, 4f, 4f, chipBgPaint);
            canvas.drawText("CASPIAN FLOW", MARGIN_LEFT + 8f, currentY + 11.5f, chipTextPaint);
            currentY += 24f;

            // Title
            String safeDocTitle = (title != null && !title.trim().isEmpty()) ? title : "AI Study Transcript";
            StaticLayout titleLayout = createStaticLayout(safeDocTitle, titlePaint, (int) CONTENT_WIDTH);
            canvas.save();
            canvas.translate(MARGIN_LEFT, currentY);
            titleLayout.draw(canvas);
            canvas.restore();
            currentY += titleLayout.getHeight() + 4f;

            // Subtitle
            String dateStr = new SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(new Date());
            String serviceName = (service != null && "gemini".equalsIgnoreCase(service)) ? "Google Gemini" : "OpenAI ChatGPT";
            String metaText = "Conversation Transcript • " + dateStr + " • " + serviceName;
            if ("ai_only".equalsIgnoreCase(scope)) metaText += " (AI Responses Only)";
            else if ("user_only".equalsIgnoreCase(scope)) metaText += " (Prompts Only)";
            canvas.drawText(metaText, MARGIN_LEFT, currentY + 8f, subPaint);
            currentY += 16f;

            // Divider
            canvas.drawLine(MARGIN_LEFT, currentY, MARGIN_RIGHT, currentY, dividerPaint);
            currentY += 16f;

            // Render turns
            if (turnsArray != null && turnsArray.length() > 0) {
                for (int i = 0; i < turnsArray.length(); i++) {
                    JSONObject turn = turnsArray.getJSONObject(i);
                    int idx = turn.optInt("index", i + 1);
                    String author = turn.optString("author", (idx % 2 != 0) ? "user" : "assistant");
                    String turnText = turn.optString("text", "");
                    String turnService = turn.optString("service", service);

                    boolean isUser = "user".equalsIgnoreCase(author);
                    boolean isGemini = "gemini".equalsIgnoreCase(turnService);

                    if ("ai_only".equalsIgnoreCase(scope) && isUser) continue;
                    if ("user_only".equalsIgnoreCase(scope) && !isUser) continue;

                    String badgeLabel = isUser ? ("USER PROMPT #" + idx)
                            : (isGemini ? ("GEMINI RESPONSE #" + idx) : ("CHATGPT RESPONSE #" + idx));
                    int badgeColor = isUser ? COLOR_BLUE_700 : (isGemini ? COLOR_NAVY_800 : COLOR_EMERALD_700);
                    int cardBgColor = isUser ? COLOR_SLATE_50 : Color.WHITE;

                    String cleanBody = cleanMarkdownForPrint(turnText);
                    float innerCardPadding = 10f;
                    float cardWidth = CONTENT_WIDTH;
                    float innerTextWidth = cardWidth - (innerCardPadding * 2f);

                    StaticLayout textLayout = createStaticLayout(cleanBody, bodyPaint, (int) innerTextWidth);
                    int totalLines = textLayout.getLineCount();

                    float badgeWidth = badgeTextPaint.measureText(badgeLabel) + 14f;
                    float headerHeight = 24f;

                    int startLine = 0;
                    while (startLine < totalLines) {
                        float availableHeight = MARGIN_BOTTOM - currentY - innerCardPadding;
                        if (availableHeight < 40f) {
                            drawFooter(canvas, pageIndex, subPaint);
                            pdfDocument.finishPage(page);
                            pageIndex++;
                            pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageIndex).create();
                            page = pdfDocument.startPage(pageInfo);
                            canvas = page.getCanvas();
                            currentY = MARGIN_TOP;
                            availableHeight = MARGIN_BOTTOM - currentY - innerCardPadding;
                        }

                        float startLineTop = textLayout.getLineTop(startLine);
                        int endLine = startLine;
                        float headerAllowance = (startLine == 0) ? headerHeight : 0f;

                        while (endLine < totalLines &&
                                (textLayout.getLineBottom(endLine) - startLineTop + headerAllowance) <= availableHeight) {
                            endLine++;
                        }
                        if (endLine == startLine) {
                            endLine = startLine + 1;
                        }

                        float chunkTextHeight = textLayout.getLineBottom(endLine - 1) - startLineTop;
                        float chunkCardHeight = chunkTextHeight + headerAllowance + (innerCardPadding * 2f);

                        RectF cardRect = new RectF(MARGIN_LEFT, currentY, MARGIN_RIGHT, currentY + chunkCardHeight);
                        cardBgPaint.setColor(cardBgColor);
                        canvas.drawRoundRect(cardRect, 7f, 7f, cardBgPaint);
                        canvas.drawRoundRect(cardRect, 7f, 7f, cardStrokePaint);

                        float drawY = currentY + innerCardPadding;

                        if (startLine == 0) {
                            badgeBgPaint.setColor(badgeColor);
                            RectF badgeRect = new RectF(MARGIN_LEFT + innerCardPadding, drawY,
                                    MARGIN_LEFT + innerCardPadding + badgeWidth, drawY + 14f);
                            canvas.drawRoundRect(badgeRect, 3.5f, 3.5f, badgeBgPaint);
                            canvas.drawText(badgeLabel, MARGIN_LEFT + innerCardPadding + 7f, drawY + 10.5f, badgeTextPaint);
                            drawY += headerHeight;
                        }

                        canvas.save();
                        canvas.clipRect(MARGIN_LEFT + innerCardPadding, drawY,
                                MARGIN_RIGHT - innerCardPadding, drawY + chunkTextHeight);
                        canvas.translate(MARGIN_LEFT + innerCardPadding, drawY - startLineTop);
                        textLayout.draw(canvas);
                        canvas.restore();

                        currentY += chunkCardHeight + 10f;
                        startLine = endLine;

                        if (startLine < totalLines) {
                            drawFooter(canvas, pageIndex, subPaint);
                            pdfDocument.finishPage(page);
                            pageIndex++;
                            pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageIndex).create();
                            page = pdfDocument.startPage(pageInfo);
                            canvas = page.getCanvas();
                            currentY = MARGIN_TOP;
                        }
                    }
                }
            } else {
                canvas.drawText("No conversational turns available to display.", MARGIN_LEFT, currentY + 20f, bodyPaint);
            }

            drawFooter(canvas, pageIndex, subPaint);
            pdfDocument.finishPage(page);

            String finalName = (customFileName != null && !customFileName.trim().isEmpty()) ? customFileName : safeDocTitle;
            return savePdfDocument(context, pdfDocument, finalName);

        } catch (Exception e) {
            Log.e(TAG, "Failed to generate native PDF", e);
            try {
                pdfDocument.close();
            } catch (Throwable ignored) {}
            return null;
        }
    }

    public static File exportConversationToPdf(Context context, String title, JSONArray turnsArray, String service) {
        return exportConversationToPdf(context, title, turnsArray, service, "both", null);
    }

    /**
     * Exports a general web page text content to a clean native PDF document.
     */
    public static File exportWebPageToPdf(Context context, String title, String url, String pageText) {
        if (context == null) return null;

        PdfDocument pdfDocument = new PdfDocument();
        int pageIndex = 1;

        try {
            TextPaint titlePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            titlePaint.setColor(0xFF0F172A);
            titlePaint.setTextSize(16f);
            titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

            TextPaint subPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            subPaint.setColor(0xFF64748B);
            subPaint.setTextSize(8.5f);

            TextPaint bodyPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            bodyPaint.setColor(0xFF334155);
            bodyPaint.setTextSize(9.5f);

            Paint dividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            dividerPaint.setStyle(Paint.Style.STROKE);
            dividerPaint.setStrokeWidth(1f);
            dividerPaint.setColor(0xFFE2E8F0);

            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageIndex).create();
            PdfDocument.Page page = pdfDocument.startPage(pageInfo);
            Canvas canvas = page.getCanvas();

            float currentY = MARGIN_TOP;

            String safeDocTitle = (title != null && !title.trim().isEmpty()) ? title : "Web Page Document";
            StaticLayout titleLayout = createStaticLayout(safeDocTitle, titlePaint, (int) CONTENT_WIDTH);
            canvas.save();
            canvas.translate(MARGIN_LEFT, currentY);
            titleLayout.draw(canvas);
            canvas.restore();
            currentY += titleLayout.getHeight() + 4f;

            String dateStr = new SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(new Date());
            String meta = "Exported from " + (url != null ? url : "Caspian Flow") + " • " + dateStr;
            canvas.drawText(meta, MARGIN_LEFT, currentY + 8f, subPaint);
            currentY += 16f;

            canvas.drawLine(MARGIN_LEFT, currentY, MARGIN_RIGHT, currentY, dividerPaint);
            currentY += 16f;

            String cleanText = (pageText != null) ? cleanMarkdownForPrint(pageText) : "No text content.";
            StaticLayout textLayout = createStaticLayout(cleanText, bodyPaint, (int) CONTENT_WIDTH);
            int totalLines = textLayout.getLineCount();

            int startLine = 0;
            while (startLine < totalLines) {
                float availableHeight = MARGIN_BOTTOM - currentY;
                if (availableHeight < 40f) {
                    drawFooter(canvas, pageIndex, subPaint);
                    pdfDocument.finishPage(page);
                    pageIndex++;
                    pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageIndex).create();
                    page = pdfDocument.startPage(pageInfo);
                    canvas = page.getCanvas();
                    currentY = MARGIN_TOP;
                    availableHeight = MARGIN_BOTTOM - currentY;
                }

                float startLineTop = textLayout.getLineTop(startLine);
                int endLine = startLine;
                while (endLine < totalLines && (textLayout.getLineBottom(endLine) - startLineTop) <= availableHeight) {
                    endLine++;
                }
                if (endLine == startLine) {
                    endLine = startLine + 1;
                }

                float chunkHeight = textLayout.getLineBottom(endLine - 1) - startLineTop;

                canvas.save();
                canvas.clipRect(MARGIN_LEFT, currentY, MARGIN_RIGHT, currentY + chunkHeight);
                canvas.translate(MARGIN_LEFT, currentY - startLineTop);
                textLayout.draw(canvas);
                canvas.restore();

                currentY += chunkHeight + 10f;
                startLine = endLine;

                if (startLine < totalLines) {
                    drawFooter(canvas, pageIndex, subPaint);
                    pdfDocument.finishPage(page);
                    pageIndex++;
                    pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageIndex).create();
                    page = pdfDocument.startPage(pageInfo);
                    canvas = page.getCanvas();
                    currentY = MARGIN_TOP;
                }
            }

            drawFooter(canvas, pageIndex, subPaint);
            pdfDocument.finishPage(page);

            return savePdfDocument(context, pdfDocument, safeDocTitle);

        } catch (Exception e) {
            Log.e(TAG, "Failed to export web page to PDF", e);
            try {
                pdfDocument.close();
            } catch (Throwable ignored) {}
            return null;
        }
    }

    private static void drawFooter(Canvas canvas, int pageIndex, TextPaint footerPaint) {
        float footerY = MARGIN_BOTTOM + 18f;
        canvas.drawText("Caspian Flow Study Document", MARGIN_LEFT, footerY, footerPaint);
        String pageStr = "Page " + pageIndex;
        float pageStrWidth = footerPaint.measureText(pageStr);
        canvas.drawText(pageStr, MARGIN_RIGHT - pageStrWidth, footerY, footerPaint);
    }

    private static StaticLayout createStaticLayout(CharSequence text, TextPaint paint, int width) {
        if (width <= 0) width = 100;
        if (text == null) text = "";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return StaticLayout.Builder.obtain(text, 0, text.length(), paint, width)
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(0f, 1.25f)
                    .setIncludePad(true)
                    .build();
        } else {
            return new StaticLayout(text, paint, width, Layout.Alignment.ALIGN_NORMAL, 1.25f, 0f, true);
        }
    }

    private static String cleanMarkdownForPrint(String md) {
        if (md == null) return "";
        String text = md.replace("\r\n", "\n");
        text = text.replaceAll("(?m)^#{1,6}\\s+", "");
        text = text.replaceAll("```[a-zA-Z0-9_-]*\n", "\n[Code Block]\n");
        text = text.replaceAll("```", "");
        return text.trim();
    }

    public static File getExportDir(Context context) {
        try {
            File exportDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Caspian/Export");
            if (!exportDir.exists()) exportDir.mkdirs();
            return exportDir;
        } catch (Throwable t) {
            File appDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "Export");
            if (!appDir.exists()) appDir.mkdirs();
            return appDir;
        }
    }

    public static File savePdfDocument(Context context, PdfDocument pdfDocument, String title) {
        try {
            String safeTitle = title.replaceAll("[^a-zA-Z0-9._-]", "_");
            if (safeTitle.length() > 50) safeTitle = safeTitle.substring(0, 50);
            if (!safeTitle.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                safeTitle = safeTitle + "_" + timestamp + ".pdf";
            }

            File exportDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Caspian/Export");
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }

            File targetFile = new File(exportDir, safeTitle);
            FileOutputStream fos = new FileOutputStream(targetFile);
            pdfDocument.writeTo(fos);
            fos.flush();
            fos.close();
            pdfDocument.close();

            Log.i(TAG, "Successfully generated native PDF at: " + targetFile.getAbsolutePath());
            return targetFile;
        } catch (Exception e) {
            Log.w(TAG, "Failed to save to public Downloads, trying app internal storage", e);
            try {
                File appDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "Export");
                if (!appDir.exists()) appDir.mkdirs();
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                File targetFile = new File(appDir, "Caspian_Export_" + timestamp + ".pdf");
                FileOutputStream fos = new FileOutputStream(targetFile);
                pdfDocument.writeTo(fos);
                fos.flush();
                fos.close();
                pdfDocument.close();
                return targetFile;
            } catch (Exception ex) {
                Log.e(TAG, "Fatal error saving PDF to app storage", ex);
                try { pdfDocument.close(); } catch (Throwable ignored) {}
                return null;
            }
        }
    }
}
