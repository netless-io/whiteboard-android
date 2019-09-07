package com.herewhite.sdk.domain;



public class ContentModeConfig {

    public ContentModeConfig() {
        scale = 1d;
        mode = ScaleMode.Scale;
    }

    public enum ScaleMode {
        /** 基于白板 zoomScale 的缩放比例,默认处理 */
        Scale,
        /** 与 UIViewContentModeScaleAspectFit 相似，按比例缩放，将设置的宽高范围，铺满视野 */
        AspectFit,
        /** 与 UIViewContentModeScaleAspectFit 相似，按比例缩放，将设置的 宽高 * scale 的范围，铺满视野 */
        FitScale,
        /** 与 UIViewContentModeScaleAspectFit 相似，按比例缩放，将设置的 宽高 + space 的范围，铺满视野 */
        FitSpace,
        /** 与 UIViewContentModeScaleAspectFill 相似，按比例缩放，视野内容会在设置的宽高范围内 */
        AspectFill,
        /** 与 UIViewContentModeScaleAspectFill 相似，按比例缩放，视野内容会在设置的 宽高 + space 的范围内 */
        AspectFillScale,

    }

    public Double getScale() {
        return scale;
    }

    public void setScale(Double scale) {
        this.scale = scale;
    }

    public Double getSpace() {
        return space;
    }

    public void setSpace(Double space) {
        this.space = space;
    }

    public ScaleMode getMode() {
        return mode;
    }

    public void setMode(ScaleMode mode) {
        this.mode = mode;
    }

    private Double scale;
    private Double space;
    private ScaleMode mode;
}
