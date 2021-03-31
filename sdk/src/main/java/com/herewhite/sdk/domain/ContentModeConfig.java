package com.herewhite.sdk.domain;


import com.google.gson.annotations.SerializedName;

/**
 * `ContentModeConfig` 类，设置本地用户视野范围的缩放比例。
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
     * 缩放比例与缩放模式。
     */
    public enum ScaleMode {
        /**
         * （默认）基于白板的 `zoomScale` 缩放视野，保证视野在屏幕上居中。
         * Center the image in the view, but perform no scaling.
         */
        @SerializedName("Scale")
        CENTER,
        /**
         * 等比例缩放视野，使视野的长边（宽或高）正好等于屏幕宽或高，并保证视野在屏幕上居中。
         * Scale the image uniformly (maintain the image’s aspect ratio) so that both dimensions (width and height) of the image will be equal to or less than the corresponding dimension of the view (minus padding).
         */
        @SerializedName("AspectFit")
        CENTER_INSIDE,
        /**
         * 根据指定的倍数等比例缩放视野，使视野的长边（宽或高）正好等于屏幕宽或高，并保证视野在屏幕上居中。
         * Compute a scale that will maintain the original src aspect ratio, but will also ensure that src fits entirely inside dst. At least one axis (X or Y) will fit exactly. The result is centered inside dst.
         */
        @SerializedName("AspectFitScale")
        CENTER_INSIDE_SCALE,
        /**
         * 将视野范围的长边（高或宽）拉伸一定的空间，使其正好等于屏幕宽或高，并保证视野在屏幕上居中。
         * <p>
         * Scale the image uniformly (maintain the image’s aspect ratio) so that both dimensions (width and height) of the image will be equal to or larger than the corresponding dimension of the view (minus padding).
         */
        @SerializedName("AspectFitSpace")
        CENTER_INSIDE_SPACE,
        /**
         * 等比例缩放视野，使视野的短边（宽或高）正好等于屏幕宽或高，以铺满整个屏幕，并保证视野在屏幕上居中。
         * <p>
         * Scale the image uniformly (maintain the image’s aspect ratio) so that both dimensions (width and height) of the image will be equal to or larger than the corresponding dimension of the view (minus padding).
         */
        @SerializedName("AspectFill")
        CENTER_CROP,
        /**
         * 将视野范围的短边（高或宽）拉伸一定的空间，使其正好等于屏幕宽或高，以铺满整个屏幕，并保证视野在屏幕上居中。
         */
        @SerializedName("AspectFillScale")
        CENTER_CROP_SPACE,
    }

    /**
     * 获取缩放比例。
     *
     * @return 缩放比例。
     */
    public Double getScale() {
        return scale;
    }

    /**
     * 设置缩放比例。
     *
     * @param scale 缩放比例，默认值为 1，即保持视野范围原始大小。
     * @note 该方法仅在以下缩放模式下生效：
     * - {@link ScaleMode#CENTER}
     * - {@link ScaleMode#CENTER_INSIDE_SCALE}
     * - {@link ScaleMode#CENTER_INSIDE_SCALE}
     */
    public void setScale(Double scale) {
        this.scale = scale;
    }

    /**
     * 获取图像相对于视野范围的剪裁或填充空间。
     *
     * @return 图像相对于视野范围的剪裁或填充空间，单位为像素。
     */
    public Double getSpace() {
        return space;
    }

    /**
     * 设置视野范围的填充空间。
     *
     * @param space 视野范围的填充空间，单位为像素，默认值为 0。
     * @note 该方法仅在以下缩放模式下生效：
     * - {@link ScaleMode#CENTER_INSIDE_SPACE}
     * - {@link ScaleMode#CENTER_CROP_SPACE}
     */
    public void setSpace(Double space) {
        this.space = space;
    }

    /**
     * 获取设置的缩放模式。
     *
     * @return 缩放模式，详见 {@link ScaleMode ScaleMode}。
     */
    public ScaleMode getMode() {
        return mode;
    }

    /**
     * 设置缩放模式。
     *
     * @param mode 缩放模式，详见 {@link ScaleMode ScaleMode}。
     */
    public void setMode(ScaleMode mode) {
        this.mode = mode;
    }

    private Double scale;
    private Double space;
    private ScaleMode mode;
}
