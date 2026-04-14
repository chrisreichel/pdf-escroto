package com.pdfescroto.ui;

import com.pdfescroto.command.UndoManager;
import com.pdfescroto.model.Annotation;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

/**
 * Properties panel stub that will display and allow editing of the currently
 * selected annotation's attributes (position, size, text content, font size, etc.).
 * Full implementation is deferred to a later task; this stub satisfies the
 * compile-time dependency from {@link MainWindow} and {@link PdfCanvas}.
 */
public class PropertiesPanel {

    private final VBox node = new VBox(6);

    /**
     * Creates the properties panel.
     *
     * @param um        the shared undo/redo manager (used when edits are committed)
     * @param onRedraw  callback invoked whenever a property change requires a canvas redraw
     */
    public PropertiesPanel(UndoManager um, Runnable onRedraw) {
        node.getStyleClass().add("properties-panel");
        node.setPrefWidth(170);
    }

    /**
     * Populates the panel with the properties of the given annotation.
     * Pass {@code null} to clear the panel.
     *
     * @param a the annotation whose properties should be displayed, or {@code null}
     */
    public void showAnnotation(Annotation a) {}

    /**
     * Returns the JavaFX node that represents this panel in the scene graph.
     *
     * @return the panel node
     */
    public Node getNode() { return node; }
}
