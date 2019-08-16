package com.herewhite.sdk.domain;

import com.google.gson.annotations.SerializedName;

/**
 * Created by buhe on 2018/8/15.
 */

public class PptPage extends WhiteObject {

    @SerializedName(value ="src", alternate = {"conversionFileUrl"})
    private String src;
    private Double width;
    private Double height;

    /**
     * Ppt，每个场景只有在初始化时，可以配置。src 图片中心点默认为白板内部坐标原点。
     * 背景图无法拖动，无法改变在白板内部的位置。
     *
     * @param src    背景图 url
     * @param width  在白板中占用的宽度
     * @param height 在白板中占用的高度
     */
    public PptPage(String src, Double width, Double height) {
        this.src = src;
        this.width = width;
        this.height = height;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }
}
