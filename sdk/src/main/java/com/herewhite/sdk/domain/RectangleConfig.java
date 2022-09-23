package com.herewhite.sdk.domain;

/**
 * `RectangleConfig` 类，用于配置白板的视觉矩形。
 *
 * @since 2.2.0
 *
 * 视觉矩形是用户的视角必须容纳的区域。设置好视觉矩形后，SDK 会自动将视角会调整到刚好可以完整展示视觉矩形所表示的范围。
 *
 * 你可以根据要展示的 PPT 幻灯片或图像的尺寸设置视觉矩形，以确保相同的内容在不同尺寸的屏幕上都可以完整显示。
 */
public class RectangleConfig extends WhiteObject {
    private Double originX;
    private Double originY;
    private Double width;
    private Double height;
    private AnimationMode animationMode;

    /**
     * `RectangleConfig` 构造函数。
     * <p>
     * 在该函数中，你需要传入 `width`，`height` 和 `mode`。SDK 会根据你传入 `width` 和 `height` 计算视觉矩形左上角原点
     * 在世界坐标系中的位置 `originX` 和 `originY`, 即 `originX = - width / 2.0d`，`originY = - height / 2.0d`。
     * <p>
     * 该方法适用于需要快速显示完整 PPT 内容的场景。
     *
     * @param width  白板视觉矩形的宽度。视觉矩形的宽度不能小于实际展示内容的宽度，否则用户将看不见超出的部分。
     * @param height 白板视觉矩形的高度。视觉矩形的高度不能小于实际展示内容的宽度，否则用户将看不见超出的部分。
     * @param mode   视觉矩形的动画模式，详见 {@link AnimationMode}。
     */
    public RectangleConfig(Double width, Double height, AnimationMode mode) {
        this.width = width;
        this.height = height;
        this.originX = -width / 2.0d;
        this.originY = -height / 2.0d;
        this.animationMode = mode;
    }

    /**
     * `RectangleConfig` 构造函数。
     * <p>
     * 在该函数中，你需要传入 `width` 和 `height`。SDK 会根据你传入 `width` 和 `height` 计算视觉矩形左上角原点
     * 在世界坐标系中的位置 `originX` 和 `originY`, 即 `originX = - width / 2.0d`，`originY = - height / 2.0d`。
     * <p>
     * 该方法不支持设置动画模式，SDK 会默认将动画模式设置为 `Continuous`，即连续动画的模式。
     * <p>
     * 该方法适用于需要快速显示完整 PPT 内容的场景。
     *
     * @param width  视觉矩形宽度。视觉矩形的宽度不能小于实际展示内容的宽度，否则用户将看不见超出的部分。
     * @param height 视觉矩形高度。视觉矩形的高度不能小于实际展示内容的宽度，否则用户将看不见超出的部分。
     */
    public RectangleConfig(Double width, Double height) {
        this(width, height, AnimationMode.Continuous);
    }

    /**
     * `RectangleConfig` 构造函数。
     * <p>
     * 在该函数中，你需要传入 `originX`、`originY`、`width` 和 `height`。SDK 会根据你传入的 `originX`、`originY`、`width` 和 `height` 确定视觉矩形在世界坐标系（即世界坐标系）中的位置和大小。
     * <p>
     * 该方法不支持设置动画模式，而是使用默认的连续动画 `Continuous` 模式。
     *
     * @param originX 视觉矩形左上角原点在世界坐标系（以白板初始化时的中心点为原点的坐标系）中的 X 轴坐标。
     * @param originY 视觉矩形左上角原点在世界坐标系（以白板初始化时的中心点为原点的坐标系）中的 Y 轴坐标。
     * @param width   视觉矩形的宽度。视觉矩形的宽度不能小于实际展示内容的宽度，否则用户将看不见超出的部分。
     * @param height  视觉矩形的高度。视觉矩形的高度不能小于实际展示内容的宽度，否则用户将看不见超出的部分。
     */
    public RectangleConfig(Double originX, Double originY, Double width, Double height) {
        this(originX, originY, width, height, AnimationMode.Continuous);
    }

    /**
     * `RectangleConfig` 构造函数。
     * <p>
     * 在该函数中，你需要传入 `originX`、`originY`、`width`、`height` 和 `mode`。
     * SDK 会根据你传入的 `originX`、`originY`、`width`、`height` 和 `mode` 确定视觉矩形在世界坐标系（即世界坐标系）中的位置、大小和动画模式。
     *
     * @param originX 视觉矩形左上角原点在世界坐标系（以白板初始化时的中心点为原点的坐标系）中的 X 轴坐标。
     * @param originY 视觉矩形左上角原点在世界坐标系（以白板初始化时的中心点为原点的坐标系）中的 Y 轴坐标。
     * @param width   视觉矩形的宽度。视觉矩形的宽度不能小于实际展示内容的宽度，否则用户将看不见超出的部分。
     * @param height  视觉矩形的高度。视觉矩形的高度不能小于实际展示内容的宽度，否则用户将看不见超出的部分。
     * @param mode    视觉矩形的动画模式，详见 {@link AnimationMode}。
     */
    public RectangleConfig(Double originX, Double originY, Double width, Double height, AnimationMode mode) {
        this.originX = originX;
        this.originY = originY;
        this.width = width;
        this.height = height;
        this.animationMode = mode;
    }

    /**
     * 获取视觉矩形左上角原点在世界坐标系（以白板初始化时的中心点为原点的坐标系）中的 X 轴坐标。
     *
     * @return 视觉矩形左上角原点在世界坐标系（以白板初始化时的中心点为原点的坐标系）中的 X 轴坐标。
     */
    public Double getOriginX() {
        return originX;
    }

    /**
     * 设置视觉矩形左上角原点在世界坐标系（以白板初始化时的中心点为原点的坐标系）中的 X 轴坐标。
     *
     * @param originX 视觉矩形左上角原点在世界坐标系（以白板初始化时的中心点为原点的坐标系）中的 X 轴坐标。
     */
    public void setOriginX(Double originX) {
        this.originX = originX;
    }

    /**
     * 获取视觉矩形左上角原点在世界坐标系（以白板初始化时的中心点为原点的坐标系）中的 Y 轴坐标。
     *
     * @return 视觉矩形左上角原点在世界坐标系（以白板初始化时的中心点为原点的坐标系）中的 Y 轴坐标。
     */
    public Double getOriginY() {
        return originY;
    }

    /**
     * 设置视觉矩形左上角原点在世界坐标系（以白板初始化时的中心点为原点的坐标系）中的 Y 轴坐标。
     *
     * @param originY 视觉矩形左上角原点在世界坐标系（以白板初始化时的中心点为原点的坐标系）中的 Y 轴坐标。
     */
    public void setOriginY(Double originY) {
        this.originY = originY;
    }

    /**
     * 获取视觉矩形的宽度。
     *
     * @return 视觉矩形的宽度。
     */
    public Double getWidth() {
        return width;
    }

    /**
     * 设置视觉矩形的宽度。
     *
     * @param width 视觉矩形的宽度。
     */
    public void setWidth(Double width) {
        this.width = width;
    }

    /**
     * 获取视觉矩形的高度。
     *
     * @return 视觉矩形的高度。
     */
    public Double getHeight() {
        return height;
    }

    /**
     * 设置视觉矩形的高度。
     *
     * @param height 视觉矩形的高度。
     */
    public void setHeight(Double height) {
        this.height = height;
    }

    /**
     * 获取视觉矩形的动画模式。
     *
     * @return 视觉矩形的动画模式，详见 {@link AnimationMode}。
     */
    public AnimationMode getAnimationMode() {
        return animationMode;
    }

    /**
     * 设置视觉矩形的动画模式。
     *
     * @param animationMode 视觉矩形的动画模式，详见 {@link AnimationMode}。
     */
    public void setAnimationMode(AnimationMode animationMode) {
        this.animationMode = animationMode;
    }
}
