package com.herewhite.sdk.domain;

/**
 * 视觉矩形配置类
 *
 * @since 2.2.0
 */
public class RectangleConfig extends WhiteObject {
    private Double originX;
    private Double originY;
    private Double width;
    private Double height;

    /**
     * 只需宽高，动画的视觉矩形构建方法
     * <p>
     * 固定视角中心为白板初始化时的中点。会根据宽高，计算 originX originY。
     * 适合快速显示完整 ppt 内容。
     *
     * @param width  视觉矩形宽度（实际展示内容的最小宽度）
     * @param height 视觉矩形高度（实际展示内容的最小高度）
     * @param mode   动画参数
     */
    public RectangleConfig(Double width, Double height, AnimationMode mode) {
        this.width = width;
        this.height = height;
        this.originX = -width / 2.0d;
        this.originY = -height / 2.0d;
        this.animationMode = mode;
    }

    /**
     * 只需宽高的视觉矩形构建方法
     * <p>
     * 固定视角中心为白板初始化时的中点。会根据宽高，计算 originX originY。
     * 动画默认为 连续动画 {@link AnimationMode#Continuous}
     * 适合快速显示完整 ppt 内容。
     *
     * @param width  视觉矩形宽度（实际展示内容的最小宽度）
     * @param height 视觉矩形高度（实际展示内容的最小高度）
     */
    public RectangleConfig(Double width, Double height) {
        this(width, height, AnimationMode.Continuous);
    }

    /**
     * 自行配置左上角位置，宽高的构建方法
     * <p>
     * 注意，originX，originY 为白板内部坐标系坐标。白板内部坐标系
     *
     * @param originX the origin x
     * @param originY the origin y
     * @param width   the width
     * @param height  the height
     */
    public RectangleConfig(Double originX, Double originY, Double width, Double height) {
        this(originX, originY, width, height, AnimationMode.Continuous);
    }

    public RectangleConfig(Double originX, Double originY, Double width, Double height, AnimationMode mode) {
        this.originX = originX;
        this.originY = originY;
        this.width = width;
        this.height = height;
        this.animationMode = mode;
    }

    public Double getOriginX() {
        return originX;
    }

    public void setOriginX(Double originX) {
        this.originX = originX;
    }

    public Double getOriginY() {
        return originY;
    }

    public void setOriginY(Double originY) {
        this.originY = originY;
    }

    public Double getWidth() {
        return width;
    }

    public void setWidth(Double width) {
        this.width = width;
    }

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    public AnimationMode getAnimationMode() {
        return animationMode;
    }

    public void setAnimationMode(AnimationMode animationMode) {
        this.animationMode = animationMode;
    }

    private AnimationMode animationMode;
}
