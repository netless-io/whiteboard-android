package com.herewhite.sdk.domain;

public class CameraConfig extends WhiteObject {

    public AnimationMode getAnimationMode() {
        return animationMode;
    }

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
