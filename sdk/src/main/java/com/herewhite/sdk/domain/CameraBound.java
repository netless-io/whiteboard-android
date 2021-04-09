package com.herewhite.sdk.domain;

/**
 * `CameraBound` 类，用于设置视角边界。
 *
 * 视角边界指白板场景内，用户可以移动视角的范围。当视角超出视角边界时，视角会被拉回。
 *
 * @since 2.5.0
 */
public class CameraBound extends WhiteObject {

    /**
     * 获取视角边界的中心点在世界坐标系（以白板初始化时的中心点为原点的坐标系）中的 X 轴坐标。
     *
     * @return 视角边界的中心点在世界坐标系中的 X 轴坐标。
     */
    public Double getCenterX() {
        return centerX;
    }

    /**
     * 设置视角边界的中心点在世界坐标系（以白板初始化时的中心点为原点的坐标系）中的 X 轴坐标。
     *
     * @param centerX 视角边界的中心点在世界坐标系的 X 轴坐标。默认值为 0.0。
     */
    public void setCenterX(Double centerX) {
        this.centerX = centerX;
    }

    /**
     * 获取视角边界的中心点在世界坐标系（以白板初始化时的中心点为原点的坐标系）中的 Y 轴坐标。
     *
     * @return 视角边界的中心点在世界坐标系中的 Y 轴坐标。
     */
    public Double getCenterY() {
        return centerY;
    }

    /**
     * 设置视角边界的中心点在世界坐标系（以白板初始化时的中心点为原点的坐标系）中的 Y 轴坐标。
     *
     * @param centerY 视角边界的中心点在世界坐标系中的 Y 轴坐标。默认值为 0.0。
     */
    public void setCenterY(Double centerY) {
        this.centerY = centerY;
    }

    /**
     * 获取视角边界的宽度。
     *
     * @return 视角边界的宽度，单位为像素。
     */
    public Double getWidth() {
        return width;
    }

    /**
     * 设置视角边界的宽度。
     *
     * @param width 视角边界的宽度，单位为像素。如果不填，则表示无限制。
     */
    public void setWidth(Double width) {
        this.width = width;
    }

    /**
     * 获取视角边界的宽度。
     *
     * @return 视角边界的宽度，单位为像素。
     */
    public Double getHeight() {
        return height;
    }

    /**
     * 设置视角边界的高度。
     *
     * @param height 视角边界的高度，单位为像素。如果不填，则表示无限制。
     */
    public void setHeight(Double height) {
        this.height = height;
    }


    /**
     * 获取视角边界的缩放模式和最大缩放比例。
     *
     * @return 视角边界的缩放模式和最大缩放比例，详见 {@link ContentModeConfig ContentModeConfig}。
     */
    public ContentModeConfig getMaxContentMode() {
        return maxContentMode;
    }

    /**
     * 设置视角边界的缩放模式和最大缩放比例。
     *
     * @param maxContentMode 视角边界的最大缩放比例，详见 {@link ContentModeConfig ContentModeConfig}。
     */
    public void setMaxContentMode(ContentModeConfig maxContentMode) {
        this.maxContentMode = maxContentMode;
    }


    /**
     * 获取视角边界的缩放模式和最小缩放比例。
     *
     * @return 视角边界的缩放模式和最小缩放比例，详见 {@link ContentModeConfig ContentModeConfig}。
     */
    public ContentModeConfig getMinContentMode() {
        return minContentMode;
    }

    /**
     * 设置视角边界的缩放模式和最小缩放比例。
     *
     * @param minContentMode 视角边界的缩放模式和最小缩放比例，详见 {@link ContentModeConfig ContentModeConfig}。
     */
    public void setMinContentMode(ContentModeConfig minContentMode) {
        this.minContentMode = minContentMode;
    }

    /**
     * 获取用户将视角移出视角边界时感受到的阻力。
     *
     * @return 用户将视角移出视角边界时感受到的阻力。
     */
    public Double getDamping() {
        return damping;
    }

    /**
     * 设置用户将视角移出视角边界时感受到的阻力。
     *
     * @param damping 阻力大小，取值范围为 [0.0,1.0]。取值越大，用户感受到的阻力越大。
     *                - `0.0`: 用户将视角移出视角边界时，完全感受不到阻力，但当其手指离开屏幕时，视角会恢复到原位。
     *                - `1.0`: 用户完全无法将视角移出视角边界。
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
     * 初始化视角边界。
     *
     * @param miniScale 视角边界的最小缩放比例。
     * @param maxScale  视角边界的最大缩放比例。
     */
    public CameraBound(Double miniScale, Double maxScale) {
        this();
        ContentModeConfig miniConfig = new ContentModeConfig();
        miniConfig.setScale(miniScale);
        this.minContentMode = miniConfig;

        ContentModeConfig maxConfig = new ContentModeConfig();
        maxConfig.setScale(maxScale);
        this.maxContentMode = maxConfig;

    }
}
