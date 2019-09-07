package com.herewhite.sdk.domain;


import android.widget.ImageView.ScaleType;

import com.google.gson.annotations.SerializedName;

/**
 * 视野缩放比例描述类
 * @since 2.5.0
 */
public class ContentModeConfig extends WhiteObject {

    public ContentModeConfig() {
        scale = 1d;
        space = 0d;
        mode = ScaleMode.CENTER;
    }

    public enum ScaleMode {
        /** 基于白板 zoomScale 的缩放比例,默认处理 */
        @SerializedName("Scale")
        CENTER,
        /** 与 {@link android.widget.ImageView.ScaleType#CENTER_INSIDE} 相似，按比例缩放，将设置的宽高范围，铺满视野 */
        @SerializedName("AspectFit")
        CENTER_INSIDE,
        /** 与 AspectFit 相似。处理时的宽高，为 基准宽高 * scale */
        @SerializedName("AspectFitScale")
        CENTER_INSIDE_SCALE,
        /** 与 AspectFit 相似。处理时的宽高，为 基准宽高 + space */
        @SerializedName("AspectFitSpace")
        CENTER_INSIDE_SPACE,
        /** 与 {@link android.widget.ImageView.ScaleType#CENTER_CROP} 相似，按比例缩放，视野内容会在设置的宽高范围内 */
        @SerializedName("AspectFill")
        CENTER_CROP,
        /** 与 AspectFill 相似，处理时的宽高，为 基准宽高 + space */
        @SerializedName("AspectFillScale")
        CENTER_CROP_SPACE,
    }

    public Double getScale() {
        return scale;
    }

    /**
     * 缩放比例，默认 1
     *
     * 当 缩放模式 {@link #getMode()} 为 {@link ScaleMode#CENTER} {@link ScaleMode#CENTER_INSIDE_SCALE}
     * {@link ScaleMode#CENTER_INSIDE_SCALE} 时，该属性有效。
     *
     * @param scale the scale
     */
    public void setScale(Double scale) {
        this.scale = scale;
    }

    public Double getSpace() {
        return space;
    }

    /**
     * 相对于基准视野范围额外在两边多出来的空间，默认 0
     *
     * 当 缩放模式 {@link #getMode()} 为 {@link ScaleMode#CENTER_CROP_SPACE} {@link ScaleMode#CENTER_CROP_SPACE}
     * 时，该属性有效。
     *
     * @param space the space
     */
    public void setSpace(Double space) {
        this.space = space;
    }

    public ScaleMode getMode() {
        return mode;
    }

    /**
     * 设置缩放模式，默认 {@link ScaleMode#CENTER}
     *
     * @param mode the mode
     */
    public void setMode(ScaleMode mode) {
        this.mode = mode;
    }

    private Double scale;
    private Double space;
    private ScaleMode mode;
}
