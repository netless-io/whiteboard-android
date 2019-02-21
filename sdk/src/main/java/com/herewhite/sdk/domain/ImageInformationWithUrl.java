package com.herewhite.sdk.domain;

public class ImageInformationWithUrl {

    public ImageInformationWithUrl() {
    }

    public ImageInformationWithUrl(Double centerX, Double centerY, Double width, Double height, String url) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.width = width;
        this.height = height;
        this.url = url;
    }

    private Double centerX;
    private Double centerY;
    private Double width;
    private Double height;
    private String url;

    public double getCenterX() {
        return centerX;
    }

    public void setCenterX(double centerX) {
        this.centerX = centerX;
    }

    public double getCenterY() {
        return centerY;
    }

    public void setCenterY(double centerY) {
        this.centerY = centerY;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
