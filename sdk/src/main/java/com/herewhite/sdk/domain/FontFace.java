package com.herewhite.sdk.domain;

import com.google.gson.annotations.SerializedName;

/**
 * 字体配置文件，与 CSS 中的 FontFace 属性对应。
 *
 * @since 2.11.2
 */
public class FontFace extends WhiteObject {

    /**
     * @param name 字体名称，需要和 CSS 中 `font-family` 字段的值对应。
     * @param src  字体文件的地址，需要和 CSS 中 `src` 字段的值对应。支持的格式为 `url()`，表示指向远程字体文件位置，例如，`url("https://white-pan.oss-cn-shanghai.aliyuncs.com/Pacifico-Regular.ttf")`。
     *
     */
    public FontFace(String name, String src) {
        this.fontFamily = name;
        this.src = src;
    }

    @SerializedName("font-family")
    private String fontFamily;
    private String src;

    /**
     * 获取字体样式。
     *
     * @return 字体样式。
     */
    public String getFontStyle() {
        return fontStyle;
    }

    /**
     * 设置字体样式。
     *
     * @param fontStyle 字体样式，需要和 CSS 中 `font-style` 字段的值对应，取值包括：
     * - `normal`：（默认）常规。
     * - `italic`：斜体。
     */
    public void setFontStyle(String fontStyle) {
        this.fontStyle = fontStyle;
    }

    /**
     * 获取字体粗细。
     *
     * @return 字体粗细。
     */
    public String getFontWeight() {
        return fontWeight;
    }

    /**
     * 设置字体粗细。
     *
     * @param fontWeight 字体粗细，需要和 CSS 中 `font-weight` 字段的值对应。
     */
    public void setFontWeight(String fontWeight) {
        this.fontWeight = fontWeight;
    }

    /**
     * 获取字体的字符编码范围。
     *
     * @return 字体的字符编码范围
     */
    public String getUnicodeRange() {
        return unicodeRange;
    }

    /**
     * 设置字体的字符编码范围。
     *
     * @param unicodeRange 字体的字符编码范围，需要和 CSS 中 `unicode-range` 字段的值对应。
     */
    public void setUnicodeRange(String unicodeRange) {
        this.unicodeRange = unicodeRange;
    }

    @SerializedName("font-style")
    private String fontStyle;
    @SerializedName("font-weight")
    private String fontWeight;
    @SerializedName("unicode-range")
    private String unicodeRange;
}
