package com.pdfescroto.ui;

import com.pdfescroto.command.DeleteAnnotationCommand;
import com.pdfescroto.command.UndoManager;
import com.pdfescroto.model.*;
import com.pdfescroto.service.CoordinateMapper;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Canvas that renders the current PDF page image and overlays interactive
 * annotation handles for the select, text, checkbox, and image tools.
 *
 * <p>Mouse interaction is wired in Task 12 by overriding the protected
 * {@code onMousePressed}, {@code onMouseDragged}, and {@code onMouseReleased}
 * hooks. This class handles rendering only.</p>
 */
public class PdfCanvas extends Canvas {

    private final PdfDocument     document;
    private final UndoManager     undoManager;
    private final PropertiesPanel propertiesPanel;

    private PdfPage    currentPage;
    private int        currentPageIndex    = 0;
    private double     scale               = 1.0;
    private Tool       activeTool          = Tool.SELECT;
    private Annotation selectedAnnotation;

    // Drag/create state (used in Task 12)
    private boolean    isDragging;
    private double     dragStartX, dragStartY;
    private double     annotStartX, annotStartY;
    private boolean    isCreating;
    private double     createStartX, createStartY;
    private Annotation creatingAnnotation;

    /** Side length of each corner selection handle square, in canvas pixels. */
    protected static final double HANDLE_SIZE = 8.0;

    /**
     * Creates the canvas for the given document, wired to the shared undo manager
     * and the properties panel that should be updated on selection changes.
     *
     * @param document the currently open PDF document
     * @param undoManager the shared undo/redo manager
     * @param propertiesPanel the properties panel to notify when an annotation is selected
     */
    public PdfCanvas(PdfDocument document, UndoManager undoManager, PropertiesPanel propertiesPanel) {
        super(800, 1000);
        this.document        = document;
        this.undoManager     = undoManager;
        this.propertiesPanel = propertiesPanel;
        goToPage(0);
        setupMouseHandlers();
    }

    // ---- Page navigation ----

    /**
     * Navigates to the specified page, resets the selection, and redraws.
     *
     * @param index the zero-based page index; out-of-range values are ignored
     */
    public void goToPage(int index) {
        if (index < 0 || index >= document.getPages().size()) return;
        currentPageIndex   = index;
        currentPage        = document.getPages().get(index);
        selectedAnnotation = null;
        propertiesPanel.showAnnotation(null);
        resizeCanvasToPage();
        redraw();
    }

    private void resizeCanvasToPage() {
        if (currentPage == null) return;
        setWidth(currentPage.getPageWidthPt()  * scale);
        setHeight(currentPage.getPageHeightPt() * scale);
    }

    // ---- Rendering ----

    /**
     * Clears the canvas and repaints the current page image together with all
     * annotation overlays and (if applicable) the in-progress creation ghost.
     */
    public void redraw() {
        if (currentPage == null) return;
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());

        // Draw PDF page image
        var img = currentPage.getRenderedImage();
        if (img != null) gc.drawImage(img, 0, 0, getWidth(), getHeight());

        // Draw annotations
        var mapper = mapper();
        for (var annotation : currentPage.getAnnotations()) {
            drawAnnotation(gc, annotation, mapper, annotation == selectedAnnotation);
        }

        // Draw in-progress creation overlay
        if (isCreating && creatingAnnotation != null) {
            drawAnnotation(gc, creatingAnnotation, mapper, false);
        }
    }

    private void drawAnnotation(GraphicsContext gc, Annotation a,
                                CoordinateMapper mapper, boolean selected) {
        double cx = mapper.pdfXToCanvas(a.getX());
        double cy = mapper.pdfYToCanvasTop(a.getY(), a.getHeight());
        double cw = mapper.pdfDimToCanvas(a.getWidth());
        double ch = mapper.pdfDimToCanvas(a.getHeight());

        if (a instanceof TextAnnotation ta) {
            gc.setFill(Color.rgb(255, 255, 255, 0.05));
            gc.fillRect(cx, cy, cw, ch);
            gc.setStroke(selected ? Color.DODGERBLUE : Color.rgb(74, 123, 189, 0.8));
            gc.setLineWidth(selected ? 2.0 : 1.0);
            gc.strokeRect(cx, cy, cw, ch);
            gc.setFill(Color.BLACK);
            gc.setFont(Font.font(ta.getFontSize() * scale));
            gc.fillText(ta.getText(), cx + 3, cy + ta.getFontSize() * scale, cw - 6);

        } else if (a instanceof CheckboxAnnotation ca) {
            gc.setStroke(selected ? Color.DODGERBLUE : Color.rgb(46, 125, 50, 0.9));
            gc.setLineWidth(selected ? 2.0 : 1.5);
            gc.strokeRect(cx, cy, cw, ch);
            if (ca.isChecked()) {
                gc.setStroke(Color.rgb(46, 125, 50));
                gc.setLineWidth(2.0);
                // Draw a checkmark inside
                gc.strokeLine(cx + 3, cy + ch * 0.55, cx + cw * 0.4, cy + ch - 3);
                gc.strokeLine(cx + cw * 0.4, cy + ch - 3, cx + cw - 3, cy + 3);
            }

        } else if (a instanceof ImageAnnotation ia) {
            if (ia.getFxImage() != null) {
                gc.drawImage(ia.getFxImage(), cx, cy, cw, ch);
            } else {
                gc.setFill(Color.rgb(230, 81, 0, 0.1));
                gc.fillRect(cx, cy, cw, ch);
                gc.setStroke(Color.rgb(230, 81, 0, 0.8));
                gc.setLineWidth(1.0);
                gc.strokeRect(cx, cy, cw, ch);
            }
        }

        if (selected) drawHandles(gc, cx, cy, cw, ch);
    }

    private void drawHandles(GraphicsContext gc, double cx, double cy, double cw, double ch) {
        gc.setFill(Color.DODGERBLUE);
        double h = HANDLE_SIZE;
        gc.fillRect(cx - h / 2,      cy - h / 2,      h, h);
        gc.fillRect(cx + cw - h / 2, cy - h / 2,      h, h);
        gc.fillRect(cx - h / 2,      cy + ch - h / 2, h, h);
        gc.fillRect(cx + cw - h / 2, cy + ch - h / 2, h, h);
    }

    // ---- Mouse handler setup (overridden in Task 12) ----

    private void setupMouseHandlers() {
        setOnMousePressed(e  -> onMousePressed(e.getX(),  e.getY(),  e.isPrimaryButtonDown()));
        setOnMouseDragged(e  -> onMouseDragged(e.getX(),  e.getY()));
        setOnMouseReleased(e -> onMouseReleased(e.getX(), e.getY()));
    }

    /**
     * Invoked when a mouse button is pressed on the canvas.
     * Subclasses override this to implement tool-specific drag and selection logic.
     *
     * @param cx      canvas X coordinate of the press
     * @param cy      canvas Y coordinate of the press
     * @param primary {@code true} if the primary (left) mouse button is down
     */
    protected void onMousePressed(double cx, double cy, boolean primary)  {}

    /**
     * Invoked while the mouse is dragged across the canvas.
     *
     * @param cx canvas X coordinate
     * @param cy canvas Y coordinate
     */
    protected void onMouseDragged(double cx, double cy)                   {}

    /**
     * Invoked when a mouse button is released on the canvas.
     *
     * @param cx canvas X coordinate of the release
     * @param cy canvas Y coordinate of the release
     */
    protected void onMouseReleased(double cx, double cy)                  {}

    // ---- Actions ----

    /**
     * Deletes the currently selected annotation by executing a
     * {@link DeleteAnnotationCommand} through the undo manager.
     */
    public void deleteSelected() {
        if (selectedAnnotation == null || currentPage == null) return;
        undoManager.execute(new DeleteAnnotationCommand(currentPage, selectedAnnotation));
        selectedAnnotation = null;
        propertiesPanel.showAnnotation(null);
        redraw();
    }

    /**
     * Switches the active editing tool.
     *
     * @param tool the tool to activate
     */
    public void setActiveTool(Tool tool) { this.activeTool = tool; }

    // ---- Protected accessors for Task 12 mouse logic ----

    /**
     * Returns a {@link CoordinateMapper} calibrated to the current page and scale.
     *
     * @return coordinate mapper for the current view state
     */
    protected CoordinateMapper mapper() {
        return new CoordinateMapper(currentPage.getPageHeightPt(), scale);
    }

    /** @return the currently displayed page */
    protected PdfPage       getCurrentPage()            { return currentPage; }

    /** @return the currently active tool */
    protected Tool          getActiveTool()             { return activeTool; }

    /** @return the currently selected annotation, or {@code null} */
    protected Annotation    getSelectedAnnotation()     { return selectedAnnotation; }

    /**
     * Selects the given annotation and notifies the properties panel.
     *
     * @param a the annotation to select, or {@code null} to clear selection
     */
    protected void          setSelectedAnnotation(Annotation a) {
        selectedAnnotation = a;
        propertiesPanel.showAnnotation(a);
    }

    /** @return the shared undo manager */
    protected UndoManager   getUndoManager()            { return undoManager; }

    /** @return the properties panel wired to this canvas */
    protected PropertiesPanel getPropertiesPanel()      { return propertiesPanel; }

    /** @return {@code true} if an annotation is currently being drawn */
    protected boolean       isCreating()                { return isCreating; }

    /**
     * Sets the in-progress creation flag.
     *
     * @param b {@code true} while drawing a new annotation
     */
    protected void          setCreating(boolean b)      { isCreating = b; }

    /** @return the canvas X coordinate where the current drag began */
    protected double        getDragStartX()             { return dragStartX; }

    /** @return the canvas Y coordinate where the current drag began */
    protected double        getDragStartY()             { return dragStartY; }

    /** @return the PDF X coordinate of the annotation at drag start */
    protected double        getAnnotStartX()            { return annotStartX; }

    /** @return the PDF Y coordinate of the annotation at drag start */
    protected double        getAnnotStartY()            { return annotStartY; }

    /** @return the canvas X coordinate where the current creation drag began */
    protected double        getCreateStartX()           { return createStartX; }

    /** @return the canvas Y coordinate where the current creation drag began */
    protected double        getCreateStartY()           { return createStartY; }

    /** @return the annotation being drawn, or {@code null} */
    protected Annotation    getCreatingAnnotation()     { return creatingAnnotation; }

    /**
     * Records the canvas coordinates where the current drag started.
     *
     * @param x canvas X
     * @param y canvas Y
     */
    protected void          setDragStart(double x, double y)    { dragStartX = x; dragStartY = y; }

    /**
     * Records the PDF-space position of the annotation at the start of a drag.
     *
     * @param x PDF X
     * @param y PDF Y
     */
    protected void          setAnnotStart(double x, double y)   { annotStartX = x; annotStartY = y; }

    /**
     * Records the canvas coordinates where the current creation drag started.
     *
     * @param x canvas X
     * @param y canvas Y
     */
    protected void          setCreateStart(double x, double y)  { createStartX = x; createStartY = y; }

    /**
     * Sets the annotation instance being drawn during a creation drag.
     *
     * @param a the partially constructed annotation
     */
    protected void          setCreatingAnnotation(Annotation a) { creatingAnnotation = a; }

    /**
     * Sets the drag-in-progress flag.
     *
     * @param b {@code true} while a drag move is active
     */
    protected void          setIsDragging(boolean b)            { isDragging = b; }

    /** @return {@code true} while a drag move is active */
    protected boolean       getIsDragging()                     { return isDragging; }
}
