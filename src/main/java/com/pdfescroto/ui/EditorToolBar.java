package com.pdfescroto.ui;

import com.pdfescroto.command.UndoManager;
import com.pdfescroto.model.PdfDocument;
import javafx.scene.Node;
import javafx.scene.layout.HBox;

/**
 * Toolbar stub that will expose tool-selection buttons and page-navigation controls.
 * Full implementation is deferred to a later task; this stub satisfies the compile-time
 * dependency from {@link MainWindow}.
 */
public class EditorToolBar {

    private final HBox node = new HBox(4);

    /**
     * Creates the toolbar for the given document and undo manager.
     *
     * @param doc the currently open PDF document
     * @param um  the shared undo/redo manager
     */
    public EditorToolBar(PdfDocument doc, UndoManager um) {}

    /**
     * Binds this toolbar to the given canvas so that tool-selection and
     * page-navigation buttons can delegate to it.
     *
     * @param canvas the canvas to bind
     */
    public void bindCanvas(PdfCanvas canvas) {}

    /**
     * Returns the JavaFX node that represents this toolbar in the scene graph.
     *
     * @return the toolbar node
     */
    public Node getNode() { return node; }
}
