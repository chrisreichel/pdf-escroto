package com.pdfescroto.model;

import javafx.scene.image.Image;

/**
 * An annotation that places an image on a PDF page.
 * <p>
 * {@code imageData} holds the raw bytes of the image file and is used when
 * serializing the annotation back to PDF. {@code fxImage} is a cached
 * {@link Image} used for fast canvas rendering; the two fields are populated
 * independently by the service layer.
 */
public class ImageAnnotation extends Annotation {
    private byte[] imageData; // raw bytes of the image file
    private Image  fxImage;   // cached JavaFX image for canvas drawing

    /**
     * Creates an ImageAnnotation with the given bounding rectangle.
     *
     * @param x      lower-left x in PDF points
     * @param y      lower-left y in PDF points
     * @param width  width in PDF points
     * @param height height in PDF points
     */
    public ImageAnnotation(double x, double y, double width, double height) {
        super(x, y, width, height);
    }

    /** Returns the raw image bytes used for PDF serialization. */
    public byte[] getImageData()         { return imageData; }

    /** Sets the raw image bytes used for PDF serialization. */
    public void   setImageData(byte[] d) { this.imageData = d; }

    /** Returns the cached JavaFX {@link Image} used for canvas rendering. */
    public Image  getFxImage()           { return fxImage; }

    /** Sets the cached JavaFX {@link Image} used for canvas rendering. */
    public void   setFxImage(Image img)  { this.fxImage = img; }
}
