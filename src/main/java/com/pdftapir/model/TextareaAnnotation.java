package com.pdftapir.model;

/**
 * A multi-line text block annotation. Text wraps at word boundaries within the
 * bounding box by default; wrapping can be disabled. Supports background fill,
 * border visibility, vertical alignment, and font color.
 */
public class TextareaAnnotation extends Annotation {

    private String  content        = "";
    private float   fontSize       = 12f;
    private boolean bold           = false;
    private boolean italic         = false;
    private String  textAlign      = "LEFT";
    private String  fontColor      = "#000000";
    private boolean wrap           = true;
    private String  backgroundFill = "transparent";
    private boolean showBorder     = true;
    private String  verticalAlign  = "TOP";

    public TextareaAnnotation(double x, double y, double width, double height) {
        super(x, y, width, height);
    }

    public String  getContent()               { return content; }
    public void    setContent(String c)        { this.content = c == null ? "" : c; }

    public float   getFontSize()              { return fontSize; }
    public void    setFontSize(float s)        { this.fontSize = s; }

    public boolean isBold()                   { return bold; }
    public void    setBold(boolean b)          { this.bold = b; }

    public boolean isItalic()                 { return italic; }
    public void    setItalic(boolean i)        { this.italic = i; }

    public String  getTextAlign()             { return textAlign; }
    public void    setTextAlign(String a)      { this.textAlign = a == null ? "LEFT" : a; }

    public String  getFontColor()             { return fontColor; }
    public void    setFontColor(String c)      { this.fontColor = c == null ? "#000000" : c; }

    public boolean isWrap()                   { return wrap; }
    public void    setWrap(boolean w)          { this.wrap = w; }

    public String  getBackgroundFill()        { return backgroundFill; }
    public void    setBackgroundFill(String f) { this.backgroundFill = f == null ? "transparent" : f; }

    public boolean isShowBorder()             { return showBorder; }
    public void    setShowBorder(boolean b)   { this.showBorder = b; }

    public String  getVerticalAlign()         { return verticalAlign; }
    public void    setVerticalAlign(String v)  { this.verticalAlign = v == null ? "TOP" : v; }
}
