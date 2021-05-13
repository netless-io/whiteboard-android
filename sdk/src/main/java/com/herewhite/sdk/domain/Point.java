package com.herewhite.sdk.domain;

/**
 * Created by buhe on 2018/8/15.
 */

/**
 * `Point` 类，用于描述世界坐标系（以白板初始化时的中心点为原点的坐标系）中的点。
 */
public class Point extends WhiteObject {
    private Double x;
    private Double y;

    /**
     * 获取点在世界坐标系中的 X 轴坐标。
     *
     * @return 点在世界坐标系中的 X 轴坐标。
     */
    public double getX() {
        return x;
    }

    /**
     * 设置点在世界坐标系中的 X 轴坐标。
     *
     * @param x 点在世界坐标系中的 X 轴坐标。
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * 获取点在世界坐标系中的 Y 轴坐标。
     *
     * @return 点在世界坐标系中的 Y 轴坐标
     */
    public double getY() {
        return y;
    }

    /**
     * 设置点在世界坐标系中的 Y 轴坐标。
     *
     * @param y 点在世界坐标系中的 Y 轴坐标。
     */
    public void setY(double y) {
        this.y = y;
    }

}
