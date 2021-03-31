package com.herewhite.sdk.domain;

/**
 * Created by buhe on 2018/8/15.
 */

/**
 * `Point` 类，用于描述白板内部坐标系上的点。
 * <p>
 * 白板内部坐标系以白板初始化时的中心点为原点，X 轴正方向向右，Y 轴正方向向下。
 */
public class Point extends WhiteObject {
    private Double x;
    private Double y;

    /**
     * 获取点在白板内部坐标系上的 X 轴坐标。
     *
     * @return X 轴坐标。
     */
    public double getX() {
        return x;
    }

    /**
     * 设置点在白板内部坐标系上的 X 轴坐标。
     *
     * @param x X 轴坐标。
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * 获取点在白板内部坐标系上的 Y 轴坐标。
     *
     * @return Y 轴坐标。
     */
    public double getY() {
        return y;
    }

    /**
     * 设置点在白板内部坐标系上的 Y 轴坐标。
     *
     * @return Y 轴坐标。
     */
    public void setY(double y) {
        this.y = y;
    }

}
