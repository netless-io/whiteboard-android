package com.herewhite.sdk.domain;

/**
 * Created by buhe on 2018/8/15.
 */

public class TextareaBox {
    private double scale;
    private double width;
    private double height;
    private String originalText;
    private TextareaLine[] textLines;

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public String getOriginalText() {
        return originalText;
    }

    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }

    public TextareaLine[] getTextLines() {
        return textLines;
    }

    public void setTextLines(TextareaLine[] textLines) {
        this.textLines = textLines;
    }
}
