package com.herewhite.sdk.domain;

import com.google.gson.annotations.SerializedName;

/**
 * 字体配置文件，与 FontFace 属性 一一对应
 *
 * @since 2.11.2
 */
public class FontFace extends WhiteObject {

    /**
     * @param name 字体名称，匹配时，需要完全一致，对应 CSS 中 Font Family 字段
     * @param src  字体地址链接，对应 CSS 中 src 字段，传入链接类似 url("https://white-pan.oss-cn-shanghai.aliyuncs.com/Pacifico-Regular.ttf")。也可以根据 CSS FontFace 支持的其他格式进行填写。
     */
    public FontFace(String name, String src) {
        this.fontFamily = name;
        this.src = src;
    }

    @SerializedName("font-family")
    private String fontFamily;
    private String src;

    public String getFontStyle() {
        return fontStyle;
    }

    /**
     * @param fontStyle 对应，CSS FontFace 中 font-style 字段
     *                  该值可以为 italic，bold，或者 normal，默认值为 normal
     */
    public void setFontStyle(String fontStyle) {
        this.fontStyle = fontStyle;
    }

    public String getFontWeight() {
        return fontWeight;
    }

    public void setFontWeight(String fontWeight) {
        this.fontWeight = fontWeight;
    }

    public String getUnicodeRange() {
        return unicodeRange;
    }

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
