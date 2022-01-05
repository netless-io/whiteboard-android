package com.herewhite.sdk.domain;

// Created by buhe on 2018/8/11.

/**
 * `MemberState` 类，用于设置互动白板实时房间的白板工具状态。
 */
public class MemberState extends WhiteObject {
    private String currentApplianceName;
    private ShapeType shapeType;
    private int[] strokeColor;
    private Double strokeWidth;
    private Double textSize;

    public MemberState() {
    }

    /**
     * 获取互动白板实时房间内当前使用的白板工具名称。
     *
     * @return 互动白板实时房间内当前使用的白板工具名称。
     */
    public String getCurrentApplianceName() {
        return currentApplianceName;
    }

    /**
     * 设置互动白板实时房间内使用的白板工具。
     *
     * @param currentApplianceName 白板工具名称，详见 {@link Appliance}。
     */
    public void setCurrentApplianceName(String currentApplianceName) {
        this.setCurrentApplianceName(currentApplianceName, null);
    }

    /**
     * 设置互动白板实时房间内使用的白板工具。
     *
     * @param currentApplianceName 白板工具名称，详见 {@link Appliance Appliance}。
     * @param shapeType 图形工具，默认值为 `Triangle`，详见 {@link com.herewhite.sdk.domain.ShapeType ShapeType}。
     */
    public void setCurrentApplianceName(String currentApplianceName, ShapeType shapeType) {
        this.currentApplianceName = currentApplianceName;

        if (Appliance.SHAPE.equals(currentApplianceName)) {
            this.shapeType = shapeType != null ? shapeType : ShapeType.Triangle;
        }
    }

    /**
     * 设置图形工具的类型。
     *
     * @since 2.12.26
     *
     * @param shapeType 图形工具的类型。详见 {@link com.herewhite.sdk.domain.ShapeType ShapeType}。
     */
    public void setShapeType(ShapeType shapeType) {
        this.currentApplianceName = Appliance.SHAPE;
        this.shapeType = shapeType;
    }

    /**
     * 获取图形工具的类型。
     *
     * @since 2.12.26
     *
     * @return 图形工具的类型。详见 {@link com.herewhite.sdk.domain.ShapeType ShapeType}。
     */
    public ShapeType getShapeType() {
        return shapeType;
    }

    /**
     * 获取用户设置的线条颜色。
     *
     * @return 线条颜色，为 RGB 格式，例如，[0, 0, 255] 表示蓝色。
     */
    public int[] getStrokeColor() {
        return strokeColor;
    }

    /**
     * 设置线条颜色。
     *
     * @param strokeColor 线条颜色，为 RGB 格式，例如，[0, 0, 255] 表示蓝色。
     */
    public void setStrokeColor(int[] strokeColor) {
        this.strokeColor = strokeColor;
    }


    /**
     * 获取用户设置的线条粗细。
     *
     * @return 线条粗细。
     */
    public double getStrokeWidth() {
        return strokeWidth;
    }

    /**
     * 设置线条粗细。
     *
     * @param strokeWidth 线条粗细。
     */
    public void setStrokeWidth(double strokeWidth) {
        this.strokeWidth = strokeWidth;
    }


    /**
     * 获取用户设置的字体大小。
     *
     * @return 字体大小。
     */
    public double getTextSize() {
        return textSize;
    }

    /**
     * 设置字体大小。
     *
     * @param textSize 字体大小。Chrome 浏览器对于小于 12 的字体会自动调整为 12。
     */
    public void setTextSize(double textSize) {
        this.textSize = textSize;
    }
}
