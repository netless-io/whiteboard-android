package com.herewhite.sdk.domain;

/**
 * `CameraBound` 类，用于设置用户的视野范围。视野范围指白板场景内，用户可以看见的部分。
 *
 * @since 2.5.0
 */
public class CameraBound extends WhiteObject {

    /**
     * 获取用户视野范围的原点在世界坐标系的 x 轴坐标。
     * <p>
     * 世界坐标系指白板内部坐标系，即以白板初始化时的中心点为原点的坐标系。
     *
     * @return 用户视野范围的原点在世界坐标系的 x 轴坐标。
     */
    public Double getCenterX() {
        return centerX;
    }

    /**
     * 设置用户视野范围的原点相对于世界坐标系的 x 轴坐标。
     * <p>
     * 世界坐标系指白板内部坐标系，即以白板初始化时的中心点为原点的坐标系。
     *
     * @param centerX 用户视野范围的原点在世界坐标系的 x 轴坐标。默认值为 0，即与世界坐标系的 x 轴坐标相同。
     */
    public void setCenterX(Double centerX) {
        this.centerX = centerX;
    }

    /**
     * 获取用户视野范围的原点相对于世界坐标系的 y 轴坐标。
     *
     * @return 用户视野范围的原点相对于世界坐标系的 y 轴坐标。
     */
    public Double getCenterY() {
        return centerY;
    }

    /**
     * 设置用户视野范围的原点相对于世界坐标系的 y 轴坐标。
     *
     * @param centerY 用户视野范围的原点相对于世界坐标系的 y 轴坐标。默认值为 0，即与世界坐标系的 y 轴坐标相同。
     */
    public void setCenterY(Double centerY) {
        this.centerY = centerY;
    }

    public Double getWidth() {
        return width;
    }

    /**
     * 设置用户视野范围的宽度。
     * <p>
     * 该方法可以搭配 {@link #setMinContentMode(ContentModeConfig)} 和 {@link #setMaxContentMode(ContentModeConfig)} 使用，
     * 用来限制用户视野范围的边界。
     *
     * @param width 用户视野范围的宽度。如果不设，则表示视野宽度无限制。
     */
    public void setWidth(Double width) {
        this.width = width;
    }

    /**
     * 获取用户视野范围的宽度。
     *
     * @return 用户视野范围的宽度。
     */
    public Double getHeight() {
        return height;
    }

    /**
     * 设置用户视野范围的高度。
     * <p>
     * 该方法可以搭配 {@link #setMinContentMode(ContentModeConfig)} 或 {@link #setMaxContentMode(ContentModeConfig)} 使用，
     * 用来限制用户视野的最小或最大缩放比例。
     *
     * @param height 用户视野范围的高度。如果不设，则表示视野宽度无限制。
     */
    public void setHeight(Double height) {
        this.height = height;
    }


    /**
     * 获取用户视野范围的最大缩放比例。
     *
     * @return 用户视野的最大缩放比例。
     */
    public ContentModeConfig getMaxContentMode() {
        return maxContentMode;
    }

    /**
     * 设置用户视野范围的最大缩放比例。
     *
     * @param maxContentMode 用户视野范围的最大缩放比例，详见 {@link ContentModeConfig ContentModeConfig}。
     */
    public void setMaxContentMode(ContentModeConfig maxContentMode) {
        this.maxContentMode = maxContentMode;
    }


    /**
     * 获取用户视野范围的最小缩放比例。
     *
     * @return 用户视野范围的最小缩放比例。
     */
    public ContentModeConfig getMinContentMode() {
        return minContentMode;
    }

    /**
     * 设置用户视野范围的最小缩放比例。
     *
     * @param minContentMode 用户视野范围的最小缩放比例，详见 {@link ContentModeConfig ContentModeConfig}。如果不设置该参数，则表示
     *                       对用户视野范围的最小缩放比例无限制。
     */
    public void setMinContentMode(ContentModeConfig minContentMode) {
        this.minContentMode = minContentMode;
    }

    /**
     * 获取用户将视野移出视野范围时感受到的阻力。
     *
     * @return 用户将视野移出视野范围时感受到的阻力。
     */
    public Double getDamping() {
        return damping;
    }

    /**
     * 设置用户将视野移出视野范围边界时感受到的阻力。
     * <p>
     * 该方法仅在用户使用手指触碰方式移动视野时生效。
     * 设置为 `0.0` 的时候，可以缩放，但是手松开的时候，视野会弹回去；设置为 `1.0`，完全不能将视野移动超过边界。
     *
     * @param damping 阻力大小，取值范围为 [0.0,1.0]。取值越大，用户感受到的阻力越大。
     *                - 0.0: 设置为 0 时，表示视野缩放完全无阻力；当用户手指离开屏幕时，视野会恢复。
     *                - 1.0: 用户完全无法将视野移出视野范围的边界。
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
     * 初始化视野范围。
     *
     * @param miniScale 视野范围最小缩放比例。
     * @param maxScale  视野范围最大缩放比例。
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
