package com.herewhite.sdk.domain;

/**
 * Created by buhe on 2018/8/15.
 */

public class PptPage {
    private String src;
    private Double width;
    private Double height;

    public PptPage(String src, Double width, Double height) {
        this.src = src;
        this.width = width;
        this.height = height;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
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
}
