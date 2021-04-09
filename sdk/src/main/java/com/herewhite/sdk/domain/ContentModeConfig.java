package com.herewhite.sdk.domain;


import com.google.gson.annotations.SerializedName;

/**
 * `ContentModeConfig` 类，设置视角边界的缩放模式和缩放比例。
 *
 * @since 2.5.0
 */
public class ContentModeConfig extends WhiteObject {

    public ContentModeConfig() {
        scale = 1d;
        space = 0d;
        mode = ScaleMode.CENTER;
    }

    /**
     * 视角边界的缩放模式。
     */
    public enum ScaleMode {
        /**
         * （默认）基于设置的 `scale` 缩放视角边界。// TODO 是否保证其在屏幕上居中？
         */
        @SerializedName("Scale")
        CENTER,
        /**
         * 等比例缩放视角边界，使视角边界的长边正好顶住与其垂直的屏幕的两边，以保证在屏幕上完整展示视角边界。
         */
        @SerializedName("AspectFit")
        CENTER_INSIDE,
        /**
         * 等比例缩放视角边界，使视角边界的长边正好顶住与其垂直的屏幕的两边，以保证在屏幕上完整展示视角边界；在此基础上，再将视角边界缩放指定的倍数。
         */
        @SerializedName("AspectFitScale")
        CENTER_INSIDE_SCALE,
        /**
         * 等比例缩放视角边界，使视角边界的长边正好顶住与其垂直的屏幕的两边；在此基础上，在视角边界的四周填充指定的空白空间。
         */
        @SerializedName("AspectFitSpace")
        CENTER_INSIDE_SPACE,
        /**
         * 等比例缩放视角边界，使视角边界的短边正好顶住与其垂直的屏幕的两边，以保证视角边界铺满屏幕。
         */
        @SerializedName("AspectFill")
        CENTER_CROP,
        /**
         * 等比例缩放视角边界，使视角边界的短边正好顶住与其垂直的屏幕的两边，以保证视角边界铺满屏幕；在此基础上再将视角边界缩放指定的倍数。
         *
         */
        @SerializedName("AspectFillScale")
        CENTER_CROP_SPACE,
    }

    /**
     * 获取视角边界的缩放比例。
     *
     * @return 视角边界的缩放比例。
     */
    public Double getScale() {
        return scale;
    }

    /**
     * 设置视角边界的缩放比例。
     *
     * @param scale 视角边界的缩放比例，默认值为 1.0，即保持视角边界的原始大小。
     *
     * @note 该方法仅在以下缩放模式下生效：
     * - {@link ScaleMode#CENTER}
     * - {@link ScaleMode#CENTER_INSIDE}
     * - {@link ScaleMode#CENTER_INSIDE_SCALE}
     * - {@link ScaleMode#CENTER_CROP_SPACE}
     */
    public void setScale(Double scale) {
        this.scale = scale;
    }

    /**
     * 获取在视角边界的四周填充的空白空间。
     *
     * @return 在视角边界的四周填充的空白空间，单位为像素。
     */
    public Double getSpace() {
        return space;
    }

    /**
     * 设置在视角边界的四周填充的空白空间。
     *
     * @note 该方法仅在 {@link ScaleMode#CENTER_INSIDE_SPACE} 缩放模式下生效。
     *
     * @param space 在视角边界的四周填充的空白空间，单位为像素，默认值为 0。
     *
     */
    public void setSpace(Double space) {
        this.space = space;
    }

    /**
     * 获取视角边界的缩放模式。
     *
     * @return 视角边界的缩放模式，详见 {@link ScaleMode ScaleMode}。
     */
    public ScaleMode getMode() {
        return mode;
    }

    /**
     * 设置视角边界的缩放模式。
     *
     * @param mode 视角边界的缩放模式，详见 {@link ScaleMode ScaleMode}。
     */
    public void setMode(ScaleMode mode) {
        this.mode = mode;
    }

    private Double scale;
    private Double space;
    private ScaleMode mode;
}
