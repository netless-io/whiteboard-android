package com.herewhite.sdk.domain;

public class PresentationViewport extends WhiteObject {
    private Double x;
    private Double y;
    private Double width;
    private Double height;

    public Double getX() { return x; }
    public PresentationViewport setX(Double value) { x = value; return this; }
    public Double getY() { return y; }
    public PresentationViewport setY(Double value) { y = value; return this; }
    public Double getWidth() { return width; }
    public PresentationViewport setWidth(Double value) { width = value; return this; }
    public Double getHeight() { return height; }
    public PresentationViewport setHeight(Double value) { height = value; return this; }
}
