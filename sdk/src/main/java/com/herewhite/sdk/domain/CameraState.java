package com.herewhite.sdk.domain;

/**
 * `CameraState` 类，描述视角状态。
 */
public class CameraState extends WhiteObject {
    private Double centerX;
    private Double centerY;
    private Double scale;

    /**
     * 获取视角的中心点在世界坐标系（以白板初始化时的中心点为原点的坐标系）中的 X 轴坐标。
     *
     * @return 视角的中心点在世界坐标系（以白板初始化时的中心点为原点的坐标系）中的 X 轴坐标。初始值为 `0.0`。
     */
    public Double getCenterX() {
        return centerX;
    }

    /**
     * 获取视角的中心点在世界坐标系（以白板初始化时的中心点为原点的坐标系）中的 Y 轴坐标。
     *
     * @return 视角的中心点在世界坐标系（以白板初始化时的中心点为原点的坐标系）中的 Y 轴坐标。初始值为 `0.0`。
     */
    public Double getCenterY() {
        return centerY;
    }

    /**
     * 获取视角的缩放比例。
     *
     * @return 视角的缩放比例。初始值为 `1.0`。
     */
    public Double getScale() {
        return scale;
    }
}
