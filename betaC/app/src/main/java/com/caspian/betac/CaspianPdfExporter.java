package com.caspian.betac;

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
 * with zero reliance on the external Android Print Spooler.
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

    /**
     * Exports a structured conversation to a high-quality PDF document.
     *
     * @param context Application/Activity context
     * @param title Title of the document
     * @param turnsArray Extracted JSON turns array
     * @param service Target AI service ('chatgpt', 'gemini', etc.)
     * @return Generated File reference or null if failed
     */
    public static File exportConversationToPdf(Context context, String title, JSONArray turnsArray, String service) {
        if (context == null) return null;

        PdfDocument pdfDocument = new PdfDocument();
        int pageIndex = 1;

        try {
            // Colors
            final int COLOR_SLATE_900 = 0xFF0F172A;
            final int COLOR_SLATE_700 = 0xFF334155;
            final int COLOR_SLATE_500 = 0xFF64748B;
            final int COLOR_SLATE_400 = 0xFF94A3B8;
            final int COLOR_SLATE_200 = 0xFFE2E8F0;
            final int COLOR_SLATE_100 = 0xFFF1F5F9;
            final int COLOR_SLATE_50  = 0xFFF8FAFC;
            final int COLOR_BLUE_700  = 0xFF1D4ED8;
            final int COLOR_EMERALD_700 = 0xFF047857;
            final int COLOR_NAVY_800  = 0xFF153D6F;

            // Paints
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

            // Start First Page
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageIndex).create();
            PdfDocument.Page page = pdfDocument.startPage(pageInfo);
            Canvas canvas = page.getCanvas();

            float currentY = MARGIN_TOP;

            // Draw Document Header
            // 1. Top Caspian Flow Chip
            RectF chipRect = new RectF(MARGIN_LEFT, currentY, MARGIN_LEFT + 74f, currentY + 16f);
            canvas.drawRoundRect(chipRect, 4f, 4f, chipBgPaint);
            canvas.drawText("CASPIAN FLOW", MARGIN_LEFT + 8f, currentY + 11.5f, chipTextPaint);
            currentY += 24f;

            // 2. Document Title
            String safeDocTitle = (title != null && !title.trim().isEmpty()) ? title : "AI Study Transcript";
            // Word-wrap title if long
            StaticLayout titleLayout = createStaticLayout(safeDocTitle, titlePaint, (int) CONTENT_WIDTH);
            canvas.save();
            canvas.translate(MARGIN_LEFT, currentY);
            titleLayout.draw(canvas);
            canvas.restore();
            currentY += titleLayout.getHeight() + 4f;

            // 3. Subtitle / Metadata
            String dateStr = new SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(new Date());
            String serviceName = (service != null && "gemini".equalsIgnoreCase(service)) ? "Google Gemini" : "OpenAI ChatGPT";
            int turnCount = (turnsArray != null) ? turnsArray.length() : 0;
            String metaText = "Conversation Transcript • " + dateStr + " • " + serviceName + " (" + turnCount + " turns)";
            canvas.drawText(metaText, MARGIN_LEFT, currentY + 8f, subPaint);
            currentY += 16f;

            // 4. Accent Divider
            canvas.drawLine(MARGIN_LEFT, currentY, MARGIN_RIGHT, currentY, dividerPaint);
            currentY += 16f;

            // Render Conversation Turns
            if (turnsArray != null && turnsArray.length() > 0) {
                for (int i = 0; i < turnsArray.length(); i++) {
                    JSONObject turn = turnsArray.getJSONObject(i);
                    int idx = turn.optInt("index", i + 1);
                    String author = turn.optString("author", (idx % 2 != 0) ? "user" : "assistant");
                    String turnText = turn.optString("text", "");
                    String turnService = turn.optString("service", service);

                    boolean isUser = "user".equalsIgnoreCase(author);
                    boolean isGemini = "gemini".equalsIgnoreCase(turnService);

                    // Badge label & colors
                    String badgeLabel = isUser ? ("USER PROMPT #" + idx)
                            : (isGemini ? ("GEMINI RESPONSE #" + idx) : ("CHATGPT RESPONSE #" + idx));
                    int badgeColor = isUser ? COLOR_BLUE_700 : (isGemini ? COLOR_NAVY_800 : COLOR_EMERALD_700);
                    int cardBgColor = isUser ? COLOR_SLATE_50 : Color.WHITE;

                    // Clean and prepare body text
                    String cleanBody = cleanMarkdownForPrint(turnText);
                    float innerCardPadding = 10f;
                    float cardWidth = CONTENT_WIDTH;
                    float innerTextWidth = cardWidth - (innerCardPadding * 2f);

                    StaticLayout textLayout = createStaticLayout(cleanBody, bodyPaint, (int) innerTextWidth);
                    int totalLines = textLayout.getLineCount();

                    float badgeWidth = badgeTextPaint.measureText(badgeLabel) + 14f;
                    float headerHeight = 24f; // badge + spacing

                    // Card Pagination Logic
                    int startLine = 0;
                    while (startLine < totalLines) {
                        float availableHeight = MARGIN_BOTTOM - currentY - innerCardPadding;
                        if (availableHeight < 40f) {
                            // Page full, finish and start fresh page
                            drawFooter(canvas, pageIndex, subPaint);
                            pdfDocument.finishPage(page);
                            pageIndex++;
                            pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageIndex).create();
                            page = pdfDocument.startPage(pageInfo);
                            canvas = page.getCanvas();
                            currentY = MARGIN_TOP;
                            availableHeight = MARGIN_BOTTOM - currentY - innerCardPadding;
                        }

                        // Determine how many lines fit on this page
                        float startLineTop = textLayout.getLineTop(startLine);
                        int endLine = startLine;
                        float headerAllowance = (startLine == 0) ? headerHeight : 0f;

                        while (endLine < totalLines &&
                                (textLayout.getLineBottom(endLine) - startLineTop + headerAllowance) <= availableHeight) {
                            endLine++;
                        }
                        if (endLine == startLine) {
                            endLine = startLine + 1; // Always take at least one line
                        }

                        float chunkTextHeight = textLayout.getLineBottom(endLine - 1) - startLineTop;
                        float chunkCardHeight = chunkTextHeight + headerAllowance + (innerCardPadding * 2f);

                        // Draw Card Background
                        RectF cardRect = new RectF(MARGIN_LEFT, currentY, MARGIN_RIGHT, currentY + chunkCardHeight);
                        cardBgPaint.setColor(cardBgColor);
                        canvas.drawRoundRect(cardRect, 7f, 7f, cardBgPaint);
                        canvas.drawRoundRect(cardRect, 7f, 7f, cardStrokePaint);

                        float drawY = currentY + innerCardPadding;

                        // Draw Badge only on first chunk of this turn
                        if (startLine == 0) {
                            badgeBgPaint.setColor(badgeColor);
                            RectF badgeRect = new RectF(MARGIN_LEFT + innerCardPadding, drawY,
                                    MARGIN_LEFT + innerCardPadding + badgeWidth, drawY + 14f);
                            canvas.drawRoundRect(badgeRect, 3.5f, 3.5f, badgeBgPaint);
                            canvas.drawText(badgeLabel, MARGIN_LEFT + innerCardPadding + 7f, drawY + 10.5f, badgeTextPaint);
                            drawY += headerHeight;
                        }

                        // Draw Text Chunk with clipping
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
                // Empty turns fallback
                canvas.drawText("No conversational turns available to display.", MARGIN_LEFT, currentY + 20f, bodyPaint);
            }

            // Finish Final Page
            drawFooter(canvas, pageIndex, subPaint);
            pdfDocument.finishPage(page);

            // Save PDF File to Storage
            return savePdfDocument(context, pdfDocument, safeDocTitle);

        } catch (Exception e) {
            Log.e(TAG, "Failed to generate native PDF", e);
            try {
                pdfDocument.close();
            } catch (Throwable ignored) {}
            return null;
        }
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
        // Strip out excessive markdown artifacts while preserving readability
        String text = md.replace("\r\n", "\n");
        // Simplify markdown headers
        text = text.replaceAll("(?m)^#{1,6}\\s+", "");
        // Clean code block fences
        text = text.replaceAll("```[a-zA-Z0-9_-]*\n", "\n[Code Block]\n");
        text = text.replaceAll("```", "");
        return text.trim();
    }

    private static File savePdfDocument(Context context, PdfDocument pdfDocument, String title) {
        try {
            String safeTitle = title.replaceAll("[^a-zA-Z0-9._-]", "_");
            if (safeTitle.length() > 40) safeTitle = safeTitle.substring(0, 40);
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = safeTitle + "_" + timestamp + ".pdf";

            File exportDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Caspian/Export");
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }

            File targetFile = new File(exportDir, fileName);
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
