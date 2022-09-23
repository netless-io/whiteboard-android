package com.herewhite.sdk.domain;

/**
 * 图片的位置和尺寸信息及图片的 URL 地址。
 */
public class ImageInformationWithUrl extends WhiteObject {

    private Double centerX;
    private Double centerY;
    private Double width;
    private Double height;
    private String url;
    public ImageInformationWithUrl() {
    }
    /**
     * `ImageInformationWithUrl` 构造方法，用于初始化 `ImageInformationWithUrl` 实例。
     *
     * @param centerX 图片的中心在世界坐标系（以白板初始化时的中心点为原点的坐标系）中的横向坐标。
     * @param centerY 图片的中心在世界坐标系（以白板初始化时的中心点为原点的坐标系）中的纵向坐标。
     * @param width   图片的宽度，单位为像素。
     * @param height  图片的高度，单位为像素。
     * @param url     图片的 URL 地址。必须确保 app 客户端能访问该 URL，否则无法正常展示图片。
     */
    public ImageInformationWithUrl(Double centerX, Double centerY, Double width, Double height, String url) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.width = width;
        this.height = height;
        this.url = url;
    }

    /**
     * 获取图片的中心在世界坐标系（以白板初始化时的中心点为原点的坐标系）中的横向坐标。
     *
     * @return 图片的中心在世界坐标系中的横向坐标。
     */
    public double getCenterX() {
        return centerX;
    }

    /**
     * 设置图片的中心在世界坐标系（以白板初始化时的中心点为原点的坐标系）中的横向坐标。
     *
     * @param centerX 图片的中心在世界坐标系中的横向坐标。
     */
    public void setCenterX(double centerX) {
        this.centerX = centerX;
    }

    /**
     * 获取图片的中心在世界坐标系（以白板初始化时的中心点为原点的坐标系）中的纵向坐标。
     *
     * @return 图片的中心在世界坐标系中的纵向坐标。
     */
    public double getCenterY() {
        return centerY;
    }

    /**
     * 设置图片的中心在世界坐标系（以白板初始化时的中心点为原点的坐标系）中的纵向坐标。
     *
     * @param centerY 图片的中心在世界坐标系中的纵向坐标。
     */
    public void setCenterY(double centerY) {
        this.centerY = centerY;
    }

    /**
     * 获取图片的宽度。
     *
     * @return 图片的宽度，单位为像素。
     */
    public double getWidth() {
        return width;
    }

    /**
     * 设置图片的宽度。
     *
     * @param width 图片的宽度，单位为像素。如果图片的宽度超出视角的边界，用户将看不到超出部分。
     */
    public void setWidth(double width) {
        this.width = width;
    }

    /**
     * 获取图片的高度。
     *
     * @return 图片的高度，单位为像素。
     */
    public double getHeight() {
        return height;
    }

    /**
     * 设置图片的高度。
     *
     * @param height 图片的高度，单位为像素。如果图片的高度超出视角的边界，用户将看不到超出部分。
     */
    public void setHeight(double height) {
        this.height = height;
    }

    /**
     * 获取图片的 URL 地址。
     *
     * @return 图片的 URL 地址。
     */
    public String getUrl() {
        return url;
    }

    /**
     * 设置图片的 URL 地址。
     *
     * @param url 图片的 URL 地址。必须确保 app 客户端能访问该 URL，否则无法正常展示图片。
     */
    public void setUrl(String url) {
        this.url = url;
    }
}
