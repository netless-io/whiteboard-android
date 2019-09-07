package com.herewhite.sdk.domain;

/**
 * 视野范围描述类
 * @since 2.5.0
 */
public class CameraBound extends WhiteObject {
    public Double getCenterX() {
        return centerX;
    }

    /**
     * 基础视野中心点，默认 0
     *
     * @param centerX the center x
     */
    public void setCenterX(Double centerX) {
        this.centerX = centerX;
    }

    public Double getCenterY() {
        return centerY;
    }

    /**
     * 基础视野中心点，默认 0
     *
     * @param centerY the center y
     */
    public void setCenterY(Double centerY) {
        this.centerY = centerY;
    }

    public Double getWidth() {
        return width;
    }

    /**
     * 基础视野宽度，不传则为无穷
     *
     * 配合 {@link #setMinContentMode(ContentModeConfig)} {@link #setMinContentMode(ContentModeConfig)} 使用，
     * 用来描述，最大最小缩放比例。
     *
     * @param width the width
     */
    public void setWidth(Double width) {
        this.width = width;
    }

    public Double getHeight() {
        return height;
    }

    /**
     * 基础视野高度，不传则为无穷
     *
     * 配合 {@link #setMinContentMode(ContentModeConfig)} {@link #setMinContentMode(ContentModeConfig)} 使用，
     * 用来描述，最大最小缩放比例。
     *
     * @param height the height
     */
    public void setHeight(Double height) {
        this.height = height;
    }

    public ContentModeConfig getMaxContentMode() {
        return maxContentMode;
    }

    /**
     * 最大缩放比例，不传则不会限制最大比例，或者跟随 {@link com.herewhite.sdk.WhiteSdkConfiguration#setZoomMaxScale(double)}
     *
     * @param maxContentMode {@link ContentModeConfig}
     */
    public void setMaxContentMode(ContentModeConfig maxContentMode) {
        this.maxContentMode = maxContentMode;
    }

    public ContentModeConfig getMinContentMode() {
        return minContentMode;
    }

    /**
     * 最小缩放比例，不传则不会限制最小比例，或者跟随 {@link com.herewhite.sdk.WhiteSdkConfiguration#setZoomMinScale(double)}
     *
     * @param minContentMode {@link ContentModeConfig}
     */
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
