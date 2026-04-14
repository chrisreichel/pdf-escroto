package com.pdfescroto.model;

import org.apache.pdfbox.pdmodel.PDDocument;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Represents an open PDF document, wrapping PDFBox's {@link PDDocument} together
 * with the application-level page models and the original source file reference.
 * <p>
 * Implements {@link AutoCloseable} so it can be used in try-with-resources blocks;
 * closing this object closes the underlying {@link PDDocument}.
 */
public class PdfDocument implements AutoCloseable {
    private final PDDocument    pdDocument;
    private final List<PdfPage> pages;
    private       File          sourceFile;

    /**
     * Creates a PdfDocument wrapping the given PDFBox document.
     *
     * @param pdDocument the underlying PDFBox document (must not be {@code null})
     * @param pages      the list of {@link PdfPage} objects, one per page
     * @param sourceFile the file from which the document was loaded, or {@code null} for new documents
     */
    public PdfDocument(PDDocument pdDocument, List<PdfPage> pages, File sourceFile) {
        this.pdDocument = pdDocument;
        this.pages      = pages;
        this.sourceFile = sourceFile;
    }

    /** Returns the underlying PDFBox {@link PDDocument}. */
    public PDDocument    getPdDocument()      { return pdDocument; }

    /** Returns the list of pages in this document. */
    public List<PdfPage> getPages()           { return pages; }

    /** Returns the source {@link File} from which this document was loaded. */
    public File          getSourceFile()      { return sourceFile; }

    /** Sets the source file (e.g. after a Save As operation). */
    public void          setSourceFile(File f){ this.sourceFile = f; }

    /**
     * Closes the underlying {@link PDDocument}, releasing all associated resources.
     *
     * @throws IOException if closing the document fails
     */
    @Override
    public void close() throws IOException { pdDocument.close(); }
}
