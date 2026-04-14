package com.pdfescroto.service;

import com.pdfescroto.model.*;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.interactive.annotation.*;
import org.apache.pdfbox.pdmodel.interactive.form.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Loads a PDF file into a {@link PdfDocument}, reconstructing any
 * pdf-escroto annotations that were previously written by {@link PdfSaver}.
 * <p>
 * The loader identifies annotations by the tags embedded during save:
 * <ul>
 *   <li>Text annotations: {@link PDAnnotationFreeText} with subject {@code "pdf-escroto-text"}</li>
 *   <li>Checkbox annotations: {@link PDAnnotationWidget} with annotation name {@code "pdf-escroto-checkbox"}</li>
 *   <li>Image annotations: {@link PDAnnotationRubberStamp} with subject {@code "pdf-escroto-image"}</li>
 * </ul>
 */
public class PdfLoader {

    private static final java.util.logging.Logger LOG =
            java.util.logging.Logger.getLogger(PdfLoader.class.getName());

    private final PdfRenderer renderer = new PdfRenderer();

    /**
     * Loads the given PDF file, rendering each page to a {@link javafx.scene.image.WritableImage}
     * and reconstructing any pdf-escroto annotations.
     * <p>
     * If an exception occurs after the {@link PDDocument} is opened, the document is closed
     * before the exception is rethrown to avoid resource leaks.
     *
     * @param file the PDF file to load
     * @return a {@link PdfDocument} containing the loaded pages and annotations
     * @throws IOException if the file cannot be read or parsed
     */
    public PdfDocument load(File file) throws IOException {
        var pdDoc = Loader.loadPDF(file);
        try {
            var pages = new ArrayList<PdfPage>();
            for (int i = 0; i < pdDoc.getNumberOfPages(); i++) {
                var pdPage   = pdDoc.getPage(i);
                var mediaBox = pdPage.getMediaBox();
                var pdfPage  = new PdfPage(i, mediaBox.getWidth(), mediaBox.getHeight());
                try {
                    pdfPage.setRenderedImage(renderer.renderPage(pdDoc, i));
                } catch (Exception e) {
                    // May fail in headless environments (e.g., tests without JavaFX initialized)
                    LOG.warning("Could not render page " + i + ": " + e.getMessage());
                }

                for (var annot : pdPage.getAnnotations()) {
                    parseAnnotation(pdDoc, annot).ifPresent(pdfPage::addAnnotation);
                }
                pages.add(pdfPage);
            }
            return new PdfDocument(pdDoc, pages, file);
        } catch (Exception e) {
            pdDoc.close();
            throw (e instanceof IOException ioe) ? ioe : new IOException("Failed to load PDF", e);
        }
    }

    private Optional<Annotation> parseAnnotation(PDDocument pdDoc, PDAnnotation pdAnnotation) {
        var rect = pdAnnotation.getRectangle();

        // Text annotation: PDAnnotationFreeText with subject TAG_TEXT
        if (pdAnnotation instanceof PDAnnotationFreeText freeText) {
            if (PdfSaver.TAG_TEXT.equals(freeText.getSubject())) {
                var ta = new TextAnnotation(
                        rect.getLowerLeftX(), rect.getLowerLeftY(),
                        rect.getWidth(), rect.getHeight());
                ta.setText(freeText.getContents() != null ? freeText.getContents() : "");
                // Recover font size from title popup field (/T), where writeText stored "fs=<size>"
                String titleBar = freeText.getTitlePopup();
                if (titleBar != null && titleBar.startsWith("fs=")) {
                    try {
                        ta.setFontSize(Float.parseFloat(titleBar.substring(3)));
                    } catch (NumberFormatException ignored) {
                        // malformed title bar — keep default fontSize
                    }
                }
                return Optional.of(ta);
            }
        }

        // Checkbox annotation: PDAnnotationWidget with annotationName TAG_CHECKBOX
        if (pdAnnotation instanceof PDAnnotationWidget widget) {
            if (PdfSaver.TAG_CHECKBOX.equals(widget.getAnnotationName())) {
                var ca = new CheckboxAnnotation(
                        rect.getLowerLeftX(), rect.getLowerLeftY(),
                        rect.getWidth(), rect.getHeight());
                reconstructCheckboxField(pdDoc, widget, ca);
                return Optional.of(ca);
            }
        }

        // Image annotation: PDAnnotationRubberStamp with subject TAG_IMAGE
        if (pdAnnotation instanceof PDAnnotationRubberStamp stamp) {
            if (PdfSaver.TAG_IMAGE.equals(stamp.getSubject())) {
                return Optional.of(new ImageAnnotation(
                        rect.getLowerLeftX(), rect.getLowerLeftY(),
                        rect.getWidth(), rect.getHeight()));
            }
        }

        return Optional.empty();
    }

    /**
     * Looks up the AcroForm field that owns the given widget and populates
     * the checkbox annotation's label and checked state.
     * <p>
     * When a PDF has exactly one widget per field the field and widget share
     * the same COS dictionary (the "merged form" case described in the PDF spec),
     * so the "Parent" entry on the widget is absent. Instead this method
     * reconstructs the {@link PDCheckBox} directly from the widget's own
     * COS dictionary via {@link PDFieldFactory}, which handles both merged
     * and non-merged layouts.
     */
    private void reconstructCheckboxField(PDDocument pdDoc,
                                          PDAnnotationWidget widget,
                                          CheckboxAnnotation ca) {
        var acroForm = pdDoc.getDocumentCatalog().getAcroForm();
        if (acroForm == null) return;

        // First try: the widget COS dict may itself be the merged field dict.
        // PDFieldFactory can reconstruct the typed PDCheckBox from it.
        var widgetDict = widget.getCOSObject();

        // Check for non-merged case: explicit Parent key present
        var parentBase = widgetDict.getDictionaryObject(COSName.PARENT);
        COSDictionary fieldDict = (parentBase instanceof COSDictionary pd) ? pd : widgetDict;

        var field = PDFieldFactory.createField(acroForm, fieldDict, null);
        if (field instanceof PDCheckBox checkbox) {
            String alt = checkbox.getAlternateFieldName();
            ca.setLabel(alt != null ? alt : "");
            try {
                ca.setChecked(checkbox.isChecked());
            } catch (Exception e) {
                // isChecked() should not throw, but guard defensively
            }
        }
    }
}
