package com.herewhite.sdk.domain;

/**
 * `CameraConfig` 类，用于配置视角参数。
 *
 * @since 2.2.0
 */
public class CameraConfig extends WhiteObject {

    /**
     * 获取视角调整时的动画模式。
     *
     * @since 2.3.2
     *
     * @return 视角调整时的动画模式，详见 {@link AnimationMode}。
     */
    public AnimationMode getAnimationMode() {
        return animationMode;
    }

    /**
     * 设置视角调整时的动画模式。
     *
     * @since 2.3.2
     *
     * @param animationMode 视角调整时的动画模式，详见 {@link AnimationMode}。
     */
    public void setAnimationMode(AnimationMode animationMode) {
        this.animationMode = animationMode;
    }

    private AnimationMode animationMode;

    /**
     * 获取视角的中心点在世界坐标系（以白板初始化时的中心点为原点的坐标系）中的 X 轴坐标。
     *
     * @return 视角的中心点在世界坐标系中的 X 轴坐标。
     */
    public Double getCenterX() {
        return centerX;
    }

    /**
     * 设置视角的中心点在世界坐标系（以白板初始化时的中心点为原点的坐标系）中的 X 轴坐标。
     *
     * @param centerX 视角的中心点在世界坐标系中的 X 轴坐标。默认值为 0。
     */
    public void setCenterX(Double centerX) {
        this.centerX = centerX;
    }

    /**
     * 获取视角的中心点在世界坐标系（以白板初始化时的中心点为原点的坐标系）中的 Y 轴坐标。
     *
     * @return 视角的中心点在世界坐标系中的 Y 轴坐标。
     */
    public Double getCenterY() {
        return centerY;
    }

    /**
     * 设置视角的中心点在世界坐标系（以白板初始化时的中心点为原点的坐标系）中的 Y 轴坐标。
     *
     * @param centerY 视角的中心点在世界坐标系中的 Y 轴坐标。默认值为 0。
     */
    public void setCenterY(Double centerY) {
        this.centerY = centerY;
    }

    /**
     * 获取视角的缩放比例。
     *
     * @return 视角的缩放比例。
     */
    public Double getScale() {
        return scale;
    }

    /**
     * 设置视角的缩放比例。
     *
     * @param scale 视角的缩放比例。
     */
    public void setScale(Double scale) {
        this.scale = scale;
    }

    private Double centerX;
    private Double centerY;
    private Double scale;
}
