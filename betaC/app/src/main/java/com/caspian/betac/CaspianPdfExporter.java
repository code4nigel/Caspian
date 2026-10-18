package com.caspian.betac;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
            currentY += 14f;

            // Code block styling
            Paint codeCardBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            codeCardBgPaint.setStyle(Paint.Style.FILL);
            codeCardBgPaint.setColor(0xFF1E1E2E);

            Paint codeHeaderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            codeHeaderPaint.setStyle(Paint.Style.FILL);
            codeHeaderPaint.setColor(0xFF262638);

            TextPaint codeLangPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            codeLangPaint.setColor(0xFF8E8EA0);
            codeLangPaint.setTextSize(7.5f);
            codeLangPaint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));

            TextPaint codeTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            codeTextPaint.setColor(0xFFCDD6F4);
            codeTextPaint.setTextSize(8f);
            codeTextPaint.setTypeface(Typeface.MONOSPACE);

            // Math styling
            Paint mathCardBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mathCardBgPaint.setStyle(Paint.Style.FILL);
            mathCardBgPaint.setColor(0xFFF8FAFC);

            Paint mathAccentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mathAccentPaint.setStyle(Paint.Style.FILL);
            mathAccentPaint.setColor(COLOR_NAVY_800);

            TextPaint mathTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            mathTextPaint.setColor(COLOR_SLATE_900);
            mathTextPaint.setTextSize(9f);
            mathTextPaint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));

            // Table styling
            Paint tableHeaderBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            tableHeaderBgPaint.setStyle(Paint.Style.FILL);
            tableHeaderBgPaint.setColor(0xFFF1F5F9);

            Paint tableRowEvenBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            tableRowEvenBgPaint.setStyle(Paint.Style.FILL);
            tableRowEvenBgPaint.setColor(Color.WHITE);

            Paint tableRowOddBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            tableRowOddBgPaint.setStyle(Paint.Style.FILL);
            tableRowOddBgPaint.setColor(0xFFF8FAFC);

            Paint tableGridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            tableGridPaint.setStyle(Paint.Style.STROKE);
            tableGridPaint.setStrokeWidth(0.5f);
            tableGridPaint.setColor(COLOR_SLATE_200);

            TextPaint tableHeaderPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            tableHeaderPaint.setColor(COLOR_SLATE_900);
            tableHeaderPaint.setTextSize(8f);
            tableHeaderPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

            TextPaint tableCellPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            tableCellPaint.setColor(COLOR_SLATE_700);
            tableCellPaint.setTextSize(8f);

            // Quote styling
            Paint quoteBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            quoteBarPaint.setStyle(Paint.Style.FILL);
            quoteBarPaint.setColor(COLOR_NAVY_800);

            TextPaint quoteTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            quoteTextPaint.setColor(COLOR_SLATE_700);
            quoteTextPaint.setTextSize(9f);
            quoteTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC));

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

                    // Ensure minimum space for badge
                    if (currentY > MARGIN_BOTTOM - 60f) {
                        drawFooter(canvas, pageIndex, subPaint);
                        pdfDocument.finishPage(page);
                        pageIndex++;
                        pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageIndex).create();
                        page = pdfDocument.startPage(pageInfo);
                        canvas = page.getCanvas();
                        currentY = MARGIN_TOP;
                    }

                    // Draw Sender Badge
                    float badgeWidth = badgeTextPaint.measureText(badgeLabel) + 14f;
                    badgeBgPaint.setColor(badgeColor);
                    RectF badgeRect = new RectF(MARGIN_LEFT, currentY, MARGIN_LEFT + badgeWidth, currentY + 15f);
                    canvas.drawRoundRect(badgeRect, 3.5f, 3.5f, badgeBgPaint);
                    canvas.drawText(badgeLabel, MARGIN_LEFT + 7f, currentY + 11f, badgeTextPaint);
                    currentY += 21f;

                    // Parse turn into structured blocks
                    List<FormattedBlock> blocks = parseBlocks(turnText);
                    for (FormattedBlock block : blocks) {
                        if (block.type == FormattedBlock.TYPE_TEXT) {
                            String cleanText = cleanMarkdownForPrint(block.content);
                            StaticLayout tl = createStaticLayout(cleanText, bodyPaint, (int) CONTENT_WIDTH);
                            int lines = tl.getLineCount();
                            int sLine = 0;
                            while (sLine < lines) {
                                float avail = MARGIN_BOTTOM - currentY;
                                if (avail < 25f) {
                                    drawFooter(canvas, pageIndex, subPaint);
                                    pdfDocument.finishPage(page);
                                    pageIndex++;
                                    pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageIndex).create();
                                    page = pdfDocument.startPage(pageInfo);
                                    canvas = page.getCanvas();
                                    currentY = MARGIN_TOP;
                                    avail = MARGIN_BOTTOM - currentY;
                                }
                                float sTop = tl.getLineTop(sLine);
                                int eLine = sLine;
                                while (eLine < lines && (tl.getLineBottom(eLine) - sTop) <= avail) {
                                    eLine++;
                                }
                                if (eLine == sLine) eLine = sLine + 1;
                                float chunkH = tl.getLineBottom(eLine - 1) - sTop;

                                canvas.save();
                                canvas.clipRect(MARGIN_LEFT, currentY, MARGIN_RIGHT, currentY + chunkH);
                                canvas.translate(MARGIN_LEFT, currentY - sTop);
                                tl.draw(canvas);
                                canvas.restore();

                                currentY += chunkH + 6f;
                                sLine = eLine;
                            }
                        } else if (block.type == FormattedBlock.TYPE_CODE) {
                            StaticLayout cl = createStaticLayout(block.content, codeTextPaint, (int) (CONTENT_WIDTH - 16f));
                            int cLines = cl.getLineCount();
                            int csLine = 0;
                            while (csLine < cLines) {
                                float avail = MARGIN_BOTTOM - currentY - 20f;
                                if (avail < 30f) {
                                    drawFooter(canvas, pageIndex, subPaint);
                                    pdfDocument.finishPage(page);
                                    pageIndex++;
                                    pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageIndex).create();
                                    page = pdfDocument.startPage(pageInfo);
                                    canvas = page.getCanvas();
                                    currentY = MARGIN_TOP;
                                    avail = MARGIN_BOTTOM - currentY - 20f;
                                }
                                float csTop = cl.getLineTop(csLine);
                                int ceLine = csLine;
                                while (ceLine < cLines && (cl.getLineBottom(ceLine) - csTop) <= avail) {
                                    ceLine++;
                                }
                                if (ceLine == csLine) ceLine = csLine + 1;
                                float cchunkH = cl.getLineBottom(ceLine - 1) - csTop;
                                float totalBoxH = cchunkH + (csLine == 0 ? 18f : 8f) + 8f;

                                RectF cBox = new RectF(MARGIN_LEFT, currentY, MARGIN_RIGHT, currentY + totalBoxH);
                                canvas.drawRoundRect(cBox, 5f, 5f, codeCardBgPaint);

                                float textDrawY = currentY + 6f;
                                if (csLine == 0) {
                                    RectF hBar = new RectF(MARGIN_LEFT, currentY, MARGIN_RIGHT, currentY + 16f);
                                    canvas.drawRoundRect(hBar, 5f, 5f, codeHeaderPaint);
                                    canvas.drawText(block.language.isEmpty() ? "CODE" : block.language, MARGIN_LEFT + 8f, currentY + 11.5f, codeLangPaint);
                                    textDrawY = currentY + 22f;
                                }

                                canvas.save();
                                canvas.clipRect(MARGIN_LEFT + 8f, textDrawY, MARGIN_RIGHT - 8f, textDrawY + cchunkH);
                                canvas.translate(MARGIN_LEFT + 8f, textDrawY - csTop);
                                cl.draw(canvas);
                                canvas.restore();

                                currentY += totalBoxH + 8f;
                                csLine = ceLine;
                            }
                        } else if (block.type == FormattedBlock.TYPE_TABLE) {
                            if (!block.tableRows.isEmpty()) {
                                int cols = block.tableRows.get(0).length;
                                if (cols > 0) {
                                    float colW = CONTENT_WIDTH / cols;
                                    float rowH = 18f;
                                    for (int r = 0; r < block.tableRows.size(); r++) {
                                        if (currentY + rowH > MARGIN_BOTTOM - 20f) {
                                            drawFooter(canvas, pageIndex, subPaint);
                                            pdfDocument.finishPage(page);
                                            pageIndex++;
                                            pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageIndex).create();
                                            page = pdfDocument.startPage(pageInfo);
                                            canvas = page.getCanvas();
                                            currentY = MARGIN_TOP;
                                        }

                                        boolean isHeader = (r == 0);
                                        RectF rRect = new RectF(MARGIN_LEFT, currentY, MARGIN_RIGHT, currentY + rowH);
                                        canvas.drawRect(rRect, isHeader ? tableHeaderBgPaint : (r % 2 == 0 ? tableRowEvenBgPaint : tableRowOddBgPaint));
                                        canvas.drawRect(rRect, tableGridPaint);

                                        String[] rowCells = block.tableRows.get(r);
                                        for (int c = 0; c < rowCells.length && c < cols; c++) {
                                            float cx = MARGIN_LEFT + (c * colW);
                                            canvas.drawLine(cx, currentY, cx, currentY + rowH, tableGridPaint);
                                            canvas.save();
                                            canvas.clipRect(cx + 4f, currentY, cx + colW - 4f, currentY + rowH);
                                            canvas.drawText(rowCells[c], cx + 4f, currentY + 12.5f, isHeader ? tableHeaderPaint : tableCellPaint);
                                            canvas.restore();
                                        }
                                        currentY += rowH;
                                    }
                                    currentY += 8f;
                                }
                            }
                        } else if (block.type == FormattedBlock.TYPE_MATH) {
                            StaticLayout ml = createStaticLayout(block.content, mathTextPaint, (int) (CONTENT_WIDTH - 24f));
                            float mH = ml.getHeight() + 14f;
                            if (currentY + mH > MARGIN_BOTTOM - 20f) {
                                drawFooter(canvas, pageIndex, subPaint);
                                pdfDocument.finishPage(page);
                                pageIndex++;
                                pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageIndex).create();
                                page = pdfDocument.startPage(pageInfo);
                                canvas = page.getCanvas();
                                currentY = MARGIN_TOP;
                            }
                            RectF mBox = new RectF(MARGIN_LEFT, currentY, MARGIN_RIGHT, currentY + mH);
                            canvas.drawRoundRect(mBox, 4f, 4f, mathCardBgPaint);
                            canvas.drawRoundRect(mBox, 4f, 4f, cardStrokePaint);
                            canvas.drawRect(MARGIN_LEFT, currentY, MARGIN_LEFT + 3.5f, currentY + mH, mathAccentPaint);

                            canvas.save();
                            canvas.translate(MARGIN_LEFT + 12f, currentY + 7f);
                            ml.draw(canvas);
                            canvas.restore();

                            currentY += mH + 8f;
                        } else if (block.type == FormattedBlock.TYPE_QUOTE) {
                            StaticLayout ql = createStaticLayout(block.content, quoteTextPaint, (int) (CONTENT_WIDTH - 20f));
                            float qH = ql.getHeight() + 8f;
                            if (currentY + qH > MARGIN_BOTTOM - 20f) {
                                drawFooter(canvas, pageIndex, subPaint);
                                pdfDocument.finishPage(page);
                                pageIndex++;
                                pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageIndex).create();
                                page = pdfDocument.startPage(pageInfo);
                                canvas = page.getCanvas();
                                currentY = MARGIN_TOP;
                            }
                            canvas.drawRect(MARGIN_LEFT, currentY, MARGIN_LEFT + 3f, currentY + qH, quoteBarPaint);
                            canvas.save();
                            canvas.translate(MARGIN_LEFT + 10f, currentY + 4f);
                            ql.draw(canvas);
                            canvas.restore();
                            currentY += qH + 8f;
                        }
                    }

                    // Render turn images if present
                    JSONArray imgs = turn.optJSONArray("images");
                    if (imgs != null && imgs.length() > 0) {
                        for (int imgIdx = 0; imgIdx < imgs.length(); imgIdx++) {
                            String b64 = imgs.optString(imgIdx, null);
                            Bitmap bmp = decodeBase64Bitmap(b64);
                            if (bmp != null) {
                                float imgW = Math.min(bmp.getWidth(), CONTENT_WIDTH);
                                float imgH = (bmp.getHeight() * imgW) / bmp.getWidth();
                                if (imgH > 350f) {
                                    imgW = (imgW * 350f) / imgH;
                                    imgH = 350f;
                                }
                                if (currentY + imgH > MARGIN_BOTTOM - 20f) {
                                    drawFooter(canvas, pageIndex, subPaint);
                                    pdfDocument.finishPage(page);
                                    pageIndex++;
                                    pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageIndex).create();
                                    page = pdfDocument.startPage(pageInfo);
                                    canvas = page.getCanvas();
                                    currentY = MARGIN_TOP;
                                }
                                float imgLeft = MARGIN_LEFT + (CONTENT_WIDTH - imgW) / 2f;
                                Rect srcR = new Rect(0, 0, bmp.getWidth(), bmp.getHeight());
                                RectF dstR = new RectF(imgLeft, currentY, imgLeft + imgW, currentY + imgH);
                                canvas.drawBitmap(bmp, srcR, dstR, null);
                                currentY += imgH + 10f;
                            }
                        }
                    }

                    currentY += 12f;         }
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

    public static class FormattedBlock {
        public static final int TYPE_TEXT = 0;
        public static final int TYPE_CODE = 1;
        public static final int TYPE_TABLE = 2;
        public static final int TYPE_MATH = 3;
        public static final int TYPE_QUOTE = 4;

        public int type;
        public String language = "";
        public String content = "";
        public List<String[]> tableRows = new ArrayList<>();
    }

    public static List<FormattedBlock> parseBlocks(String rawText) {
        List<FormattedBlock> blocks = new ArrayList<>();
        if (rawText == null || rawText.trim().isEmpty()) return blocks;

        String[] lines = rawText.replace("\r\n", "\n").split("\n");
        StringBuilder currentText = new StringBuilder();
        boolean inCode = false;
        String codeLang = "";
        StringBuilder codeContent = new StringBuilder();

        boolean inTable = false;
        List<String[]> currentTable = new ArrayList<>();

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            if (line.trim().startsWith("```")) {
                if (inCode) {
                    FormattedBlock b = new FormattedBlock();
                    b.type = FormattedBlock.TYPE_CODE;
                    b.language = codeLang.isEmpty() ? "CODE" : codeLang.toUpperCase(Locale.ROOT);
                    b.content = codeContent.toString().trim();
                    blocks.add(b);
                    codeContent.setLength(0);
                    codeLang = "";
                    inCode = false;
                } else {
                    if (currentText.length() > 0) {
                        FormattedBlock b = new FormattedBlock();
                        b.type = FormattedBlock.TYPE_TEXT;
                        b.content = currentText.toString().trim();
                        blocks.add(b);
                        currentText.setLength(0);
                    }
                    inCode = true;
                    codeLang = line.trim().substring(3).trim();
                }
                continue;
            }

            if (inCode) {
                codeContent.append(line).append("\n");
                continue;
            }

            if (line.trim().startsWith("|") && line.trim().endsWith("|")) {
                if (!inTable) {
                    if (currentText.length() > 0) {
                        FormattedBlock b = new FormattedBlock();
                        b.type = FormattedBlock.TYPE_TEXT;
                        b.content = currentText.toString().trim();
                        blocks.add(b);
                        currentText.setLength(0);
                    }
                    inTable = true;
                    currentTable = new ArrayList<>();
                }
                if (!line.replaceAll("[|:\\-\\s]", "").isEmpty()) {
                    String[] cells = line.split("\\|");
                    List<String> cleanCells = new ArrayList<>();
                    for (int c = 1; c < cells.length; c++) {
                        cleanCells.add(cells[c].trim());
                    }
                    if (!cleanCells.isEmpty()) {
                        currentTable.add(cleanCells.toArray(new String[0]));
                    }
                }
                continue;
            } else if (inTable) {
                if (!currentTable.isEmpty()) {
                    FormattedBlock b = new FormattedBlock();
                    b.type = FormattedBlock.TYPE_TABLE;
                    b.tableRows = currentTable;
                    blocks.add(b);
                }
                inTable = false;
            }

            if (line.trim().startsWith("$$") || line.trim().startsWith("\\[")) {
                if (currentText.length() > 0) {
                    FormattedBlock b = new FormattedBlock();
                    b.type = FormattedBlock.TYPE_TEXT;
                    b.content = currentText.toString().trim();
                    blocks.add(b);
                    currentText.setLength(0);
                }
                FormattedBlock b = new FormattedBlock();
                b.type = FormattedBlock.TYPE_MATH;
                b.content = line.replaceAll("^(\\$\\$|\\\\\\[)", "").replaceAll("(\\$\\$|\\\\\\])$", "").trim();
                blocks.add(b);
                continue;
            }

            if (line.trim().startsWith(">")) {
                if (currentText.length() > 0) {
                    FormattedBlock b = new FormattedBlock();
                    b.type = FormattedBlock.TYPE_TEXT;
                    b.content = currentText.toString().trim();
                    blocks.add(b);
                    currentText.setLength(0);
                }
                FormattedBlock b = new FormattedBlock();
                b.type = FormattedBlock.TYPE_QUOTE;
                b.content = line.replaceFirst("^>\\s*", "").trim();
                blocks.add(b);
                continue;
            }

            currentText.append(line).append("\n");
        }

        if (inCode && codeContent.length() > 0) {
            FormattedBlock b = new FormattedBlock();
            b.type = FormattedBlock.TYPE_CODE;
            b.language = codeLang.isEmpty() ? "CODE" : codeLang.toUpperCase(Locale.ROOT);
            b.content = codeContent.toString().trim();
            blocks.add(b);
        } else if (inTable && !currentTable.isEmpty()) {
            FormattedBlock b = new FormattedBlock();
            b.type = FormattedBlock.TYPE_TABLE;
            b.tableRows = currentTable;
            blocks.add(b);
        } else if (currentText.length() > 0) {
            FormattedBlock b = new FormattedBlock();
            b.type = FormattedBlock.TYPE_TEXT;
            b.content = currentText.toString().trim();
            blocks.add(b);
        }

        return blocks;
    }

    public static Bitmap decodeBase64Bitmap(String b64) {
        try {
            if (b64 == null || b64.isEmpty()) return null;
            String clean = b64.contains(",") ? b64.substring(b64.indexOf(",") + 1) : b64;
            byte[] bytes = Base64.decode(clean, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (Throwable t) {
            return null;
        }
    }

    private static String cleanMarkdownForPrint(String md) {
        if (md == null) return "";
        String text = md.replace("\r\n", "\n");
        text = text.replaceAll("(?m)^### (.*)$", "• $1");
        text = text.replaceAll("(?m)^## (.*)$", "$1");
        text = text.replaceAll("(?m)^# (.*)$", "$1");
        text = text.replaceAll("\\*\\*(.*?)\\*\\*", "$1");
        text = text.replaceAll("\\*(.*?)\\*", "$1");
        text = text.replaceAll("`([^`]+)`", "$1");
        text = text.replaceAll("(?m)^\\s*[-*]\\s+", "• ");
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
                safeTitle = safeTitle + ".pdf";
            }

            File cacheDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "Export");
            if (!cacheDir.exists()) cacheDir.mkdirs();
            File cachedFile = new File(cacheDir, safeTitle);
            try (FileOutputStream fos = new FileOutputStream(cachedFile)) {
                pdfDocument.writeTo(fos);
                fos.flush();
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    android.content.ContentValues values = new android.content.ContentValues();
                    values.put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, safeTitle);
                    values.put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
                    values.put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Caspian/Export");
                    Uri uri = context.getContentResolver().insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                    if (uri != null) {
                        try (InputStream in = new FileInputStream(cachedFile);
                             OutputStream out = context.getContentResolver().openOutputStream(uri)) {
                            if (out != null) {
                                byte[] buf = new byte[8192];
                                int len;
                                while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
                                out.flush();
                            }
                        }
                    }
                } catch (Throwable t) {
                    Log.w(TAG, "MediaStore save fallback", t);
                }
            } else {
                try {
                    File pubDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Caspian/Export");
                    if (!pubDir.exists()) pubDir.mkdirs();
                    File pubFile = new File(pubDir, safeTitle);
                    try (InputStream in = new FileInputStream(cachedFile);
                         OutputStream out = new FileOutputStream(pubFile)) {
                        byte[] buf = new byte[8192];
                        int len;
                        while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
                        out.flush();
                    }
                } catch (Throwable ignored) {}
            }

            pdfDocument.close();
            return cachedFile;
        } catch (Exception e) {
            Log.e(TAG, "Error saving PDF", e);
            try { pdfDocument.close(); } catch (Throwable ignored) {}
            return null;
        }
    }
}
