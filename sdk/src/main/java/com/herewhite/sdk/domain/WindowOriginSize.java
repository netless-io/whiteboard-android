package com.herewhite.sdk.domain;

/** MainView reference size used by WindowManager originSize mode. */
public class WindowOriginSize extends WhiteObject {
    private Double width;
    private Double height;

    public WindowOriginSize() {
    }

    public WindowOriginSize(double width, double height) {
        this.width = width;
        this.height = height;
    }

    public Double getWidth() { return width; }
    public WindowOriginSize setWidth(Double width) { this.width = width; return this; }
    public Double getHeight() { return height; }
    public WindowOriginSize setHeight(Double height) { this.height = height; return this; }
}
