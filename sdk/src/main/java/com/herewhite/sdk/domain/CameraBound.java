package com.herewhite.sdk.domain;

public class CameraBound extends WhiteObject {
    public Double getCenterX() {
        return centerX;
    }

    public void setCenterX(Double centerX) {
        this.centerX = centerX;
    }

    public Double getCenterY() {
        return centerY;
    }

    public void setCenterY(Double centerY) {
        this.centerY = centerY;
    }

    public Double getWidth() {
        return width;
    }

    public void setWidth(Double width) {
        this.width = width;
    }

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    public ContentModeConfig getMaxContentMode() {
        return maxContentMode;
    }

    public void setMaxContentMode(ContentModeConfig maxContentMode) {
        this.maxContentMode = maxContentMode;
    }

    public ContentModeConfig getMinContentMode() {
        return minContentMode;
    }

    public void setMinContentMode(ContentModeConfig minContentMode) {
        this.minContentMode = minContentMode;
    }

    private Double centerX;
    private Double centerY;
    private Double width;
    private Double height;
    private ContentModeConfig maxContentMode;
    private ContentModeConfig minContentMode;
}
