package com.herewhite.sdk.domain;

/**
 * 视角调整配置类
 *
 * @since 2.2.0
 */
public class CameraConfig extends WhiteObject {

    /**
     * 获取动画类型
     *
     * @return {@link AnimationMode}
     * @since 2.3.2
     */
    public AnimationMode getAnimationMode() {
        return animationMode;
    }

    /**
     * 设置动画类型
     *
     * @param animationMode {@link AnimationMode}
     * @since 2.3.2
     */
    public void setAnimationMode(AnimationMode animationMode) {
        this.animationMode = animationMode;
    }

    private AnimationMode animationMode;

    public Double getCenterX() {
        return centerX;
    }

    public void setCenterX(Double centerX) {
        this.centerX = centerX;
    }

    public Double getCenterY() {
        return centerY;
    }

    public void setCenterY(Double centerY) {
        this.centerY = centerY;
    }

    public Double getScale() {
        return scale;
    }

    public void setScale(Double scale) {
        this.scale = scale;
    }

    private Double centerX;
    private Double centerY;
    private Double scale;
}
