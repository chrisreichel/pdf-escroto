package com.pdfescroto.ui;

/**
 * Enumeration of the editing tools available in the toolbar.
 * <ul>
 *   <li>{@link #SELECT} – pointer/selection tool for moving and resizing existing annotations</li>
 *   <li>{@link #TEXT} – places a new {@link com.pdfescroto.model.TextAnnotation}</li>
 *   <li>{@link #CHECKBOX} – places a new {@link com.pdfescroto.model.CheckboxAnnotation}</li>
 *   <li>{@link #IMAGE} – places a new {@link com.pdfescroto.model.ImageAnnotation}</li>
 * </ul>
 */
public enum Tool { SELECT, TEXT, CHECKBOX, IMAGE }
