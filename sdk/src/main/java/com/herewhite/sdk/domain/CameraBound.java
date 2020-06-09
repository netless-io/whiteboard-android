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
     * 最大缩放比例，不传则不会限制最大比例
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
     * 最小缩放比例，不传则不会限制最小比例
     *
     * @param minContentMode {@link ContentModeConfig}
     */
    public void setMinContentMode(ContentModeConfig minContentMode) {
        this.minContentMode = minContentMode;
    }

    public Double getDamping() {
        return damping;
    }

    /**
     *
     * 阻力参数
     *
     * 越出边界时手势的阻力（范围 0.0 ~ 1.0）
     * 使用多指触碰改变视角时，如果越出边界。该值越大，感受到的阻力越大。
     * 当取 0.0 时，完全感受不到阻力；当取 1.0 时，则无法移出便捷。
     * 取中间值，则感受介乎两者之间。
     * @param damping the damping
     */
    public void setDamping(Double damping) {
        this.damping = damping;
    }

    private Double damping;
    private Double centerX;
    private Double centerY;
    private Double width;
    private Double height;
    private ContentModeConfig maxContentMode;
    private ContentModeConfig minContentMode;

    public CameraBound() {
        super();
    }

    /**
     * 效果类似 sdkConfig 删除的 zoomMinScale， zoomMaxScale 效果
     * @param miniScale
     * @param maxScale
     */
    public CameraBound(Double miniScale, Double maxScale) {
        this();
        ContentModeConfig miniConfig = new ContentModeConfig();
        miniConfig.setScale(miniScale);
        this.minContentMode = miniConfig;

        ContentModeConfig maxConfig = new ContentModeConfig();
        miniConfig.setScale(maxScale);
        this.maxContentMode = maxConfig;

    }
}
