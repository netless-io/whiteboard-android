package com.herewhite.sdk.domain;

/**
 * Created by buhe on 2018/8/15.
 */

public class TextareaLine {
    private Double dx;
    private Double dy;
    private String text;
    private Integer textLength;

    public double getDx() {
        return dx;
    }

    public void setDx(double dx) {
        this.dx = dx;
    }

    public double getDy() {
        return dy;
    }

    public void setDy(double dy) {
        this.dy = dy;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getTextLength() {
        return textLength;
    }

    public void setTextLength(int textLength) {
        this.textLength = textLength;
    }
}
