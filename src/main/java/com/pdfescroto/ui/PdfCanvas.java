package com.pdfescroto.ui;

import com.pdfescroto.command.UndoManager;
import com.pdfescroto.model.PdfDocument;
import javafx.scene.canvas.Canvas;

/**
 * Canvas stub that will render the current PDF page and overlay interactive
 * annotation handles for the select, text, checkbox, and image tools.
 * Full implementation is deferred to a later task; this stub satisfies the
 * compile-time dependency from {@link MainWindow} and {@link EditorToolBar}.
 */
public class PdfCanvas extends Canvas {

    /**
     * Creates the canvas for the given document, wired to the shared undo manager
     * and the properties panel that should be updated on selection changes.
     *
     * @param doc the currently open PDF document
     * @param um  the shared undo/redo manager
     * @param pp  the properties panel to notify when an annotation is selected
     */
    public PdfCanvas(PdfDocument doc, UndoManager um, PropertiesPanel pp) {
        super(800, 1000);
    }

    /** Redraws the current page and all annotation overlays. */
    public void redraw() {}

    /** Deletes the currently selected annotation, if any. */
    public void deleteSelected() {}

    /**
     * Switches the active editing tool.
     *
     * @param tool the tool to activate
     */
    public void setActiveTool(Tool tool) {}

    /**
     * Navigates to the specified page.
     *
     * @param index the zero-based page index
     */
    public void goToPage(int index) {}
}
