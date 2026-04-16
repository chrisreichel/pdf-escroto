package com.pdftapir.service;

import com.pdftapir.model.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.IOException;

/**
 * Draws pdf-tapir model annotations directly into page content streams.
 * The resulting PDF has fixed visible content rather than movable PDF annotations.
 */
class PdfAnnotationFlattener {

    private static final PDType1Font HELVETICA             = new PDType1Font(FontName.HELVETICA);
    private static final PDType1Font HELVETICA_BOLD        = new PDType1Font(FontName.HELVETICA_BOLD);
    private static final PDType1Font HELVETICA_OBLIQUE     = new PDType1Font(FontName.HELVETICA_OBLIQUE);
    private static final PDType1Font HELVETICA_BOLD_OBLIQUE = new PDType1Font(FontName.HELVETICA_BOLD_OBLIQUE);

    void flatten(PDDocument targetDocument, PdfDocument sourceDocument) throws IOException {
        for (var page : sourceDocument.getPages()) {
            var pdPage = targetDocument.getPage(page.getPageIndex());
            try (var content = new PDPageContentStream(targetDocument, pdPage,
                    PDPageContentStream.AppendMode.APPEND, true, true)) {
                for (var annotation : page.getAnnotations()) {
                    if (annotation instanceof TextAnnotation ta) {
                        drawText(content, ta);
                    } else if (annotation instanceof TextareaAnnotation txa) {
                        drawTextarea(content, txa);
                    } else if (annotation instanceof CheckboxAnnotation ca) {
                        drawCheckbox(content, ca);
                    } else if (annotation instanceof ImageAnnotation ia) {
                        drawImage(targetDocument, content, ia);
                    }
                }
            }
        }
    }

    private void drawText(PDPageContentStream content, TextAnnotation ta) throws IOException {
        PDType1Font font     = pickFont(ta.isBold(), ta.isItalic());
        float       fontSize = ta.getFontSize();
        float       annotX   = (float) ta.getX();
        float       annotY   = (float) ta.getY();
        float       annotW   = (float) ta.getWidth();
        float       annotH   = (float) ta.getHeight();
        String      align    = ta.getTextAlign() == null ? "LEFT" : ta.getTextAlign();
        float       leading  = fontSize * 1.2f;
        String[]    lines    = safeText(ta.getText()).split("\\R", -1);

        float[] rgb = parseRgb(ta.getFontColor());
        content.saveGraphicsState();
        content.setNonStrokingColor(rgb[0], rgb[1], rgb[2]);

        for (int i = 0; i < lines.length; i++) {
            String line  = lines[i];
            float lineW  = safeStringWidth(font, line, fontSize);
            float lineX  = switch (align) {
                case "CENTER" -> annotX + (annotW - lineW) / 2f;
                case "RIGHT"  -> annotX + annotW - lineW - 3f;
                default       -> annotX + 3f;
            };
            float lineY  = annotY + annotH - fontSize - (i * leading);

            content.beginText();
            content.setFont(font, fontSize);
            content.newLineAtOffset(lineX, lineY);
            content.showText(line);
            content.endText();
        }

        content.restoreGraphicsState();
    }

    private void drawTextarea(PDPageContentStream content, TextareaAnnotation txa) throws IOException {
        float annotX = (float) txa.getX();
        float annotY = (float) txa.getY();
        float annotW = (float) txa.getWidth();
        float annotH = (float) txa.getHeight();
        float padding = 3f;

        content.saveGraphicsState();

        // Background fill
        if (!txa.getBackgroundFill().equalsIgnoreCase("transparent")) {
            float[] fillRgb = parseRgb(txa.getBackgroundFill());
            content.setNonStrokingColor(fillRgb[0], fillRgb[1], fillRgb[2]);
            content.addRect(annotX, annotY, annotW, annotH);
            content.fill();
        }

        // Border
        if (txa.isShowBorder()) {
            content.setStrokingColor(0f, 0f, 0f);
            content.setLineWidth(1f);
            content.addRect(annotX, annotY, annotW, annotH);
            content.stroke();
        }

        // Text
        PDType1Font font     = pickFont(txa.isBold(), txa.isItalic());
        float       fontSize = txa.getFontSize();
        float       leading  = fontSize * 1.3f;
        String      align    = txa.getTextAlign() == null ? "LEFT" : txa.getTextAlign();
        float       maxW     = annotW - padding * 2;

        // Build lines (wrap or no-wrap)
        java.util.List<String> lines = txa.isWrap()
                ? wrapLines(safeText(txa.getContent()), font, fontSize, maxW)
                : noWrapLines(safeText(txa.getContent()));

        // Vertical alignment offset
        float blockH = lines.size() * leading;
        float startY;
        switch (txa.getVerticalAlign()) {
            case "MIDDLE" -> startY = annotY + annotH - (annotH - blockH) / 2f - fontSize;
            case "BOTTOM" -> startY = annotY + padding + blockH - fontSize;
            default       -> startY = annotY + annotH - fontSize - padding;
        }

        // Font color
        float[] rgb = parseRgb(txa.getFontColor());
        content.setNonStrokingColor(rgb[0], rgb[1], rgb[2]);

        for (int i = 0; i < lines.size(); i++) {
            String line  = lines.get(i);
            float lineW  = safeStringWidth(font, line, fontSize);
            float lineX  = switch (align) {
                case "CENTER" -> annotX + (annotW - lineW) / 2f;
                case "RIGHT"  -> annotX + annotW - lineW - padding;
                default       -> annotX + padding;
            };
            float lineY = startY - i * leading;
            if (lineY < annotY) break; // clipped

            content.beginText();
            content.setFont(font, fontSize);
            content.newLineAtOffset(lineX, lineY);
            content.showText(line);
            content.endText();
        }

        content.restoreGraphicsState();
    }

    private java.util.List<String> wrapLines(String text, PDType1Font font, float fontSize, float maxW) throws IOException {
        var result = new java.util.ArrayList<String>();
        for (String paragraph : text.split("\\R", -1)) {
            String[] words = paragraph.split("\\s+", -1);
            var lineBuilder = new StringBuilder();
            for (String word : words) {
                if (word.isEmpty()) continue;
                String candidate = lineBuilder.isEmpty() ? word : lineBuilder + " " + word;
                if (safeStringWidth(font, candidate, fontSize) <= maxW) {
                    lineBuilder = new StringBuilder(candidate);
                } else {
                    if (!lineBuilder.isEmpty()) result.add(lineBuilder.toString());
                    lineBuilder = new StringBuilder(word);
                }
            }
            result.add(lineBuilder.toString());
        }
        return result;
    }

    private java.util.List<String> noWrapLines(String text) {
        return java.util.Arrays.asList(text.split("\\R", -1));
    }

    private PDType1Font pickFont(boolean bold, boolean italic) {
        if (bold && italic) return HELVETICA_BOLD_OBLIQUE;
        if (bold)           return HELVETICA_BOLD;
        if (italic)         return HELVETICA_OBLIQUE;
        return HELVETICA;
    }

    private float safeStringWidth(PDType1Font font, String text, float fontSize) {
        try {
            return font.getStringWidth(text) / 1000f * fontSize;
        } catch (Exception e) {
            return 0f;
        }
    }

    private void drawCheckbox(PDPageContentStream content, CheckboxAnnotation ca) throws IOException {
        float x = (float) ca.getX();
        float y = (float) ca.getY();
        float w = (float) ca.getWidth();
        float h = (float) ca.getHeight();

        content.saveGraphicsState();
        if (!ca.isBorderless()) {
            content.setStrokingColor(0f, 0f, 0f);
            content.setLineWidth(1f);
            content.addRect(x, y, w, h);
            content.stroke();
        }

        if (ca.isChecked()) {
            float[] rgb = parseRgb(ca.getCheckmarkColor());
            content.setStrokingColor(rgb[0], rgb[1], rgb[2]);
            content.setLineWidth(Math.max(1.5f, Math.min(w, h) * 0.12f));
            content.moveTo(x + w * 0.18f, y + h * 0.5f);
            content.lineTo(x + w * 0.42f, y + h * 0.22f);
            content.lineTo(x + w * 0.82f, y + h * 0.78f);
            content.stroke();
        }
        content.restoreGraphicsState();
    }

    private void drawImage(PDDocument document, PDPageContentStream content, ImageAnnotation ia) throws IOException {
        byte[] imageData = ia.getImageData();
        if (imageData == null || imageData.length == 0) return;
        var pdImage = PDImageXObject.createFromByteArray(document, imageData, "pdf-tapir-image");
        content.drawImage(pdImage, (float) ia.getX(), (float) ia.getY(),
                (float) ia.getWidth(), (float) ia.getHeight());
    }

    private float[] parseRgb(String hex) {
        if (hex == null || !hex.matches("^#?[0-9a-fA-F]{6}$")) {
            return new float[]{0f, 0f, 0f};
        }
        String normalized = hex.startsWith("#") ? hex.substring(1) : hex;
        return new float[]{
                Integer.parseInt(normalized.substring(0, 2), 16) / 255f,
                Integer.parseInt(normalized.substring(2, 4), 16) / 255f,
                Integer.parseInt(normalized.substring(4, 6), 16) / 255f
        };
    }

    private String safeText(String text) {
        if (text == null) return "";
        // Replace tabs with spaces and strip other control characters that are
        // not in Helvetica's WinAnsiEncoding (anything below 0x20 except \r and \n).
        StringBuilder sb = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\t') {
                sb.append("    "); // expand tab to 4 spaces
            } else if (c == '\r' || c == '\n') {
                sb.append(c);     // keep newlines for line-splitting
            } else if (c < 0x20 || c == 0x7F) {
                // drop other control characters
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
