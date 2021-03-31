package com.herewhite.sdk.domain;

/**
 * 视野范围配置类。
 *
 * @since 2.2.0
 */
public class CameraConfig extends WhiteObject {

    /**
     * 获取视野调整时的动画模式。
     *
     * @return 视野调整时的动画模式，详见 {@link AnimationMode}。
     * @since 2.3.2
     */
    public AnimationMode getAnimationMode() {
        return animationMode;
    }

    /**
     * 设置视野调整时的动画模式。
     *
     * @param animationMode 视野调整时的动画模式，详见 {@link AnimationMode}。
     * @since 2.3.2
     */
    public void setAnimationMode(AnimationMode animationMode) {
        this.animationMode = animationMode;
    }

    private AnimationMode animationMode;

    /**
     * 获取视野范围中心点在白板内部坐标系中的 X 轴坐标。
     *
     * @return 视野范围中心点在白板内部坐标系中的 X 轴坐标。
     */
    public Double getCenterX() {
        return centerX;
    }

    /**
     * 设置视野范围中心点在白板内部坐标系中的 X 轴坐标。
     *
     * @param centerX 视野范围中心点在白板内部坐标系中的 X 轴坐标。
     */
    public void setCenterX(Double centerX) {
        this.centerX = centerX;
    }

    /**
     * 获取视野范围中心点在白板内部坐标系中的 Y 轴坐标。
     *
     * @return 视野范围中心点在白板内部坐标系中的 Y 轴坐标。
     */
    public Double getCenterY() {
        return centerY;
    }

    /**
     * 设置视野范围中心点在白板内部坐标系中的 Y 轴坐标。
     *
     * @param centerY 视野范围中心点在白板内部坐标系中的 Y 轴坐标。
     */
    public void setCenterY(Double centerY) {
        this.centerY = centerY;
    }

    /**
     * 获取视野范围的缩放比例。
     *
     * @return 视野范围的缩放比例。
     */
    public Double getScale() {
        return scale;
    }

    /**
     * 设置视野范围的缩放比例。
     *
     * @param scale 视野范围的缩放比例。
     */
    public void setScale(Double scale) {
        this.scale = scale;
    }

    private Double centerX;
    private Double centerY;
    private Double scale;
}
